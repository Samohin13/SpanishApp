/**
 * ESPEAK Gemini + TTS Proxy (Cloudflare Worker) v3
 *
 * Многослойная защита:
 *   1. X-App-Secret           — фильтр случайных гостей (любой APK имеет)
 *   2. Firebase ID Token      — JWT валидация, только зарегистрированные юзеры
 *   3. Rate-limit per IP      — макс N запросов / IP / окно
 *   4. Model fallback         — при 429/503 пробуем другие модели Gemini
 *
 * Endpoints:
 *   POST /v1beta/models/{model}:{action}  — Gemini Chat (как было)
 *   POST /tts                              — Google Cloud Text-to-Speech (v3, добавлен 1.22.6)
 *
 * Env vars (Cloudflare Dashboard → Settings → Variables and Secrets):
 *   • GEMINI_API_KEY      — Google AI Studio ключ (для Gemini)
 *   • GOOGLE_TTS_API_KEY  — Cloud Text-to-Speech API ключ (для /tts; можно
 *                           использовать тот же что GEMINI_API_KEY если
 *                           включены оба API на одном проекте)
 *   • APP_SECRET          — общий секрет (тот же что AI_PROXY_SECRET в Android)
 *   • FIREBASE_PROJECT    — "spanishapp-35092" (для валидации ID Token)
 *   • RATE_LIMIT          — макс запросов на IP за минуту (по умолчанию 20)
 */

const FALLBACK_MODELS = [
  "gemini-flash-latest",
  "gemini-2.0-flash-exp",
  "gemini-1.5-flash",
];

const GEMINI_HOST = "https://generativelanguage.googleapis.com";

// In-memory rate-limit (per Worker isolate). Не идеально, но проще KV
// и достаточно для отсечки tо-the-moon-абьюза.
const rateLimitMap = new Map(); // ip → { count, windowStart }

// Кеш Firebase публичных ключей (обновляются ~раз в сутки)
let firebaseKeysCache = { keys: null, fetchedAt: 0 };

// ─────────────────────────────────────────────────────────────────

async function getFirebaseKeys() {
  const now = Date.now();
  // Кеш 1 час
  if (firebaseKeysCache.keys && now - firebaseKeysCache.fetchedAt < 3600 * 1000) {
    return firebaseKeysCache.keys;
  }
  const resp = await fetch(
    "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"
  );
  const keys = await resp.json();
  firebaseKeysCache = { keys, fetchedAt: now };
  return keys;
}

/** Base64URL → ArrayBuffer */
function b64uToBuf(b64u) {
  const b64 = b64u.replace(/-/g, "+").replace(/_/g, "/").padEnd(
    b64u.length + ((4 - (b64u.length % 4)) % 4),
    "="
  );
  const bin = atob(b64);
  const buf = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) buf[i] = bin.charCodeAt(i);
  return buf.buffer;
}

/** Конвертирует PEM (x509) → ArrayBuffer для importKey */
function pemToBuf(pem) {
  const body = pem
    .replace(/-----BEGIN [^-]+-----/g, "")
    .replace(/-----END [^-]+-----/g, "")
    .replace(/\s+/g, "");
  return b64uToBuf(body.replace(/\+/g, "-").replace(/\//g, "_"));
}

async function verifyFirebaseToken(token, projectId) {
  const parts = token.split(".");
  if (parts.length !== 3) throw new Error("malformed token");
  const [headerB64, payloadB64, sigB64] = parts;

  const header = JSON.parse(new TextDecoder().decode(b64uToBuf(headerB64)));
  const payload = JSON.parse(new TextDecoder().decode(b64uToBuf(payloadB64)));

  // Базовые проверки claim'ов
  const now = Math.floor(Date.now() / 1000);
  if (!payload.exp || payload.exp < now) throw new Error("token expired");
  if (!payload.iat || payload.iat > now + 60) throw new Error("token from future");
  if (payload.aud !== projectId) throw new Error("wrong aud");
  if (payload.iss !== `https://securetoken.google.com/${projectId}`) throw new Error("wrong iss");
  if (!payload.sub) throw new Error("no sub");

  if (header.alg !== "RS256") throw new Error("alg must be RS256");
  if (!header.kid) throw new Error("no kid");

  // Получаем публичный ключ Firebase для этого kid
  const keys = await getFirebaseKeys();
  const pemKey = keys[header.kid];
  if (!pemKey) throw new Error(`unknown kid: ${header.kid}`);

  // Импортируем x509 cert
  const certBuf = pemToBuf(pemKey);
  const key = await crypto.subtle.importKey(
    "spki",
    // X.509 cert format requires extracting SPKI — но Workers WebCrypto
    // не парсит сам cert, поэтому здесь упрощение: pemKey уже SPKI?
    // На практике для Firebase x509 нужен parsing... используем
    // упрощённый путь через JWKS (см. fallback ниже).
    certBuf,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["verify"]
  ).catch(() => null);

  if (!key) {
    // Fallback: используем JWKS endpoint (даёт сразу JWK формат)
    const jwksResp = await fetch(
      `https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com`
    );
    const jwks = await jwksResp.json();
    const jwk = (jwks.keys || []).find((k) => k.kid === header.kid);
    if (!jwk) throw new Error("kid not in JWKS");
    const importedKey = await crypto.subtle.importKey(
      "jwk",
      jwk,
      { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
      false,
      ["verify"]
    );
    const sigBuf = b64uToBuf(sigB64);
    const data = new TextEncoder().encode(`${headerB64}.${payloadB64}`);
    const ok = await crypto.subtle.verify(
      "RSASSA-PKCS1-v1_5",
      importedKey,
      sigBuf,
      data
    );
    if (!ok) throw new Error("signature invalid");
    return payload;
  }

  const sigBuf = b64uToBuf(sigB64);
  const data = new TextEncoder().encode(`${headerB64}.${payloadB64}`);
  const ok = await crypto.subtle.verify(
    "RSASSA-PKCS1-v1_5",
    key,
    sigBuf,
    data
  );
  if (!ok) throw new Error("signature invalid");
  return payload;
}

function checkRateLimit(ip, limit) {
  const now = Date.now();
  const WINDOW_MS = 60 * 1000; // 1 минута
  const entry = rateLimitMap.get(ip);
  if (!entry || now - entry.windowStart > WINDOW_MS) {
    rateLimitMap.set(ip, { count: 1, windowStart: now });
    return true;
  }
  entry.count++;
  if (entry.count > limit) return false;
  return true;
}

// ─────────────────────────────────────────────────────────────────

export default {
  async fetch(request, env, ctx) {
    // CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, X-App-Secret, Authorization",
        },
      });
    }

    if (request.method !== "POST") {
      return new Response("Method Not Allowed", { status: 405 });
    }

    // ── Уровень 1: X-App-Secret ───────────────────────────────
    const incoming = request.headers.get("X-App-Secret") || "";
    if (!env.APP_SECRET || incoming !== env.APP_SECRET) {
      return new Response("Forbidden: bad X-App-Secret", { status: 403 });
    }

    // ── Уровень 2: Firebase ID Token ──────────────────────────
    // Опционально — если FIREBASE_PROJECT не задан в env, пропускаем
    // проверку (для постепенной миграции).
    // v3 (1.22.6): /tts endpoint пропускает Firebase-проверку.
    // Премиум-голоса вызываются часто (каждое слово в флэшкартах/играх),
    // токены живут 1 час и требуют рефреша — это усложнение без выигрыша.
    // X-App-Secret + rate-limit + локальный кэш аудио в приложении
    // достаточны для отсечки злоупотреблений.
    const isTtsEndpoint = new URL(request.url).pathname === "/tts";
    if (env.FIREBASE_PROJECT && !isTtsEndpoint) {
      const authHeader = request.headers.get("Authorization") || "";
      const token = authHeader.replace(/^Bearer\s+/i, "").trim();
      if (!token) {
        return new Response("Unauthorized: missing Firebase token", { status: 401 });
      }
      try {
        const payload = await verifyFirebaseToken(token, env.FIREBASE_PROJECT);
        // payload.sub = Firebase UID — можно использовать для per-user rate-limit
      } catch (e) {
        return new Response(`Unauthorized: ${e.message}`, { status: 401 });
      }
    }

    // ── Уровень 3: Rate-limit per IP ──────────────────────────
    const ip = request.headers.get("CF-Connecting-IP") || "unknown";
    const limit = parseInt(env.RATE_LIMIT || "20", 10);
    if (!checkRateLimit(ip, limit)) {
      return new Response(
        JSON.stringify({
          error: { code: 429, message: `Rate limit exceeded: ${limit} req/min` },
        }),
        { status: 429, headers: { "Content-Type": "application/json", "Retry-After": "60" } }
      );
    }

    // ── Парсим path ───────────────────────────────────────────
    const url = new URL(request.url);

    // ── /tts endpoint (v3, 1.22.6) — Google Cloud Text-to-Speech ──
    if (url.pathname === "/tts") {
      return handleTts(request, env);
    }

    const match = url.pathname.match(/^\/v1beta\/models\/([^:]+):(\w+)/);
    if (!match) {
      return new Response("Bad request path", { status: 400 });
    }
    const requestedModel = match[1];
    const action = match[2];

    const body = await request.text();

    // ── Уровень 4: Model fallback при 429/503 ─────────────────
    const tried = [];
    const models = [requestedModel, ...FALLBACK_MODELS.filter((m) => m !== requestedModel)];

    for (const model of models) {
      const isStream = action === "streamGenerateContent";
      const targetUrl =
        `${GEMINI_HOST}/v1beta/models/${model}:${action}` +
        `?key=${env.GEMINI_API_KEY}` +
        (isStream ? `&alt=sse` : ``);

      const upstreamResp = await fetch(targetUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body,
      });

      if (upstreamResp.status === 429 || upstreamResp.status === 503) {
        tried.push(`${model}:${upstreamResp.status}`);
        continue;
      }

      const respHeaders = new Headers(upstreamResp.headers);
      respHeaders.set("Access-Control-Allow-Origin", "*");
      respHeaders.set("X-ESPEAK-Model", model);
      if (tried.length) respHeaders.set("X-ESPEAK-Tried", tried.join(","));

      return new Response(upstreamResp.body, {
        status: upstreamResp.status,
        statusText: upstreamResp.statusText,
        headers: respHeaders,
      });
    }

    return new Response(
      JSON.stringify({
        error: { code: 429, message: `All models rate-limited: ${tried.join(", ")}` },
      }),
      { status: 429, headers: { "Content-Type": "application/json" } }
    );
  },
};

// ─────────────────────────────────────────────────────────────────
// /tts — Google Cloud Text-to-Speech proxy
// ─────────────────────────────────────────────────────────────────
//
// Принимает: { text, voice, speed, pitch }
//   voice — id из PremiumVoiceCatalog (Android), напр. "es-ES-Neural2-D"
//           или "ru-RU-Wavenet-A". Языковой код извлекается из id.
//   speed — 0.25 .. 4.0 (умножитель темпа)
//   pitch — -20.0 .. 20.0 (полутонов)
//
// Возвращает: audio/mpeg (mp3-байты) или 4xx с JSON ошибки.
//
// Использует Google Cloud Text-to-Speech REST API:
//   https://texttospeech.googleapis.com/v1/text:synthesize?key=KEY
async function handleTts(request, env) {
  const ttsKey = env.GOOGLE_TTS_API_KEY || env.GEMINI_API_KEY;
  if (!ttsKey) {
    return new Response(
      JSON.stringify({ error: "GOOGLE_TTS_API_KEY not configured" }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }

  let payload;
  try {
    payload = await request.json();
  } catch {
    return new Response(
      JSON.stringify({ error: "Bad JSON body" }),
      { status: 400, headers: { "Content-Type": "application/json" } }
    );
  }

  const text = (payload.text || "").toString().trim();
  if (!text) {
    return new Response(
      JSON.stringify({ error: "Missing text" }),
      { status: 400, headers: { "Content-Type": "application/json" } }
    );
  }

  // voice id вида "es-ES-Neural2-D" → languageCode = "es-ES"
  const voiceName = (payload.voice || "es-ES-Neural2-D").toString();
  const langMatch = voiceName.match(/^([a-z]{2})-([A-Z]{2})/);
  const languageCode = langMatch ? `${langMatch[1]}-${langMatch[2]}` : "es-ES";

  const speed = clamp(Number(payload.speed) || 1.0, 0.25, 4.0);
  const pitch = clamp(Number(payload.pitch) || 0, -20.0, 20.0);

  const ttsBody = {
    input: { text: text.slice(0, 5000) }, // hard cap чтобы не сжечь квоту на одном запросе
    voice: { languageCode, name: voiceName },
    audioConfig: {
      audioEncoding: "MP3",
      speakingRate: speed,
      pitch: pitch,
    },
  };

  const ttsUrl = `https://texttospeech.googleapis.com/v1/text:synthesize?key=${ttsKey}`;
  const upstream = await fetch(ttsUrl, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(ttsBody),
  });

  if (!upstream.ok) {
    const errText = await upstream.text();
    return new Response(
      JSON.stringify({
        error: `Cloud TTS HTTP ${upstream.status}`,
        details: errText.slice(0, 500),
      }),
      {
        status: upstream.status,
        headers: { "Content-Type": "application/json" },
      }
    );
  }

  const data = await upstream.json();
  const audioBase64 = data.audioContent;
  if (!audioBase64) {
    return new Response(
      JSON.stringify({ error: "Cloud TTS returned no audioContent" }),
      { status: 502, headers: { "Content-Type": "application/json" } }
    );
  }

  // base64 → bytes
  const bin = atob(audioBase64);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);

  return new Response(bytes, {
    status: 200,
    headers: {
      "Content-Type": "audio/mpeg",
      "Cache-Control": "public, max-age=86400",
      "Access-Control-Allow-Origin": "*",
    },
  });
}

function clamp(v, lo, hi) {
  return Math.min(hi, Math.max(lo, v));
}
