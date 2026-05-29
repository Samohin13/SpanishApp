/**
 * ESPEAK Gemini + TTS Proxy v4 (clean rewrite, без fallback bullshit)
 *
 * Защита (без изменений от v3):
 *   1. X-App-Secret           — фильтр случайных гостей
 *   2. Firebase ID Token      — JWT валидация (валидируется через JWKS)
 *   3. Rate-limit per IP      — 20 req/min default
 *
 * Endpoints:
 *   POST /v1beta/models/{model}:generateContent — Gemini Chat
 *   POST /tts                                    — Google Cloud TTS
 *
 * Env vars (Cloudflare Dashboard → Settings → Variables and Secrets):
 *   • GEMINI_API_KEY      — AI Studio key (для Gemini)
 *   • GOOGLE_TTS_API_KEY  — Cloud TTS key (опционально, fallback на GEMINI_API_KEY)
 *   • APP_SECRET          — общий секрет (= AI_PROXY_SECRET в Android)
 *   • FIREBASE_PROJECT    — "spanishapp-35092" (для валидации ID Token)
 *   • RATE_LIMIT          — макс запросов/IP/мин (default 20)
 *
 * Что убрано из v3:
 *   - Model fallback (gemini-flash-latest → 2.0-flash-exp → 1.5-flash):
 *     если основная модель 429/503, лучше явная 429 в ответе чем непредсказуемые
 *     fallback'и на возможно несуществующие модели. App контролирует MODEL constant.
 *   - ALLOWED_MODELS whitelist: бесполезный — Google API сам валидирует.
 */

const GEMINI_HOST = "https://generativelanguage.googleapis.com";
const TTS_HOST = "https://texttospeech.googleapis.com";

// In-memory rate-limit per Worker isolate
const rateLimitMap = new Map();
let firebaseKeysCache = { keys: null, fetchedAt: 0 };

// ─────────────────────────────────────────────────────────────────

function checkRateLimit(ip, limit) {
  const now = Date.now();
  const WINDOW_MS = 60 * 1000;
  const entry = rateLimitMap.get(ip);
  if (!entry || now - entry.windowStart > WINDOW_MS) {
    rateLimitMap.set(ip, { count: 1, windowStart: now });
    return true;
  }
  entry.count++;
  return entry.count <= limit;
}

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

async function getFirebaseJwks() {
  const now = Date.now();
  if (firebaseKeysCache.keys && now - firebaseKeysCache.fetchedAt < 3600 * 1000) {
    return firebaseKeysCache.keys;
  }
  const resp = await fetch(
    "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com"
  );
  const jwks = await resp.json();
  firebaseKeysCache = { keys: jwks.keys || [], fetchedAt: now };
  return firebaseKeysCache.keys;
}

async function verifyFirebaseToken(token, projectId) {
  const parts = token.split(".");
  if (parts.length !== 3) throw new Error("malformed token");
  const [headerB64, payloadB64, sigB64] = parts;

  const header = JSON.parse(new TextDecoder().decode(b64uToBuf(headerB64)));
  const payload = JSON.parse(new TextDecoder().decode(b64uToBuf(payloadB64)));

  const now = Math.floor(Date.now() / 1000);
  if (!payload.exp || payload.exp < now) throw new Error("token expired");
  if (!payload.iat || payload.iat > now + 60) throw new Error("token from future");
  if (payload.aud !== projectId) throw new Error("wrong aud");
  if (payload.iss !== `https://securetoken.google.com/${projectId}`) throw new Error("wrong iss");
  if (!payload.sub) throw new Error("no sub");
  if (header.alg !== "RS256") throw new Error("alg must be RS256");
  if (!header.kid) throw new Error("no kid");

  const keys = await getFirebaseJwks();
  const jwk = keys.find((k) => k.kid === header.kid);
  if (!jwk) throw new Error(`unknown kid: ${header.kid}`);

  const cryptoKey = await crypto.subtle.importKey(
    "jwk",
    jwk,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["verify"]
  );

  const sigBuf = b64uToBuf(sigB64);
  const data = new TextEncoder().encode(`${headerB64}.${payloadB64}`);
  const ok = await crypto.subtle.verify("RSASSA-PKCS1-v1_5", cryptoKey, sigBuf, data);
  if (!ok) throw new Error("signature invalid");
  return payload;
}

function clamp(v, lo, hi) {
  return Math.min(hi, Math.max(lo, v));
}

async function buildTtsCacheKey(text, voice, speed, pitch) {
  const raw = `${voice}|${speed}|${pitch}|${text}`;
  const hashBuf = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(raw));
  const hashHex = Array.from(new Uint8Array(hashBuf))
    .map((b) => b.toString(16).padStart(2, "0")).join("");
  return new Request(`https://espeak-tts-cache.local/v1/${hashHex}`, { method: "GET" });
}

// ─────────────────────────────────────────────────────────────────

export default {
  async fetch(request, env) {
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

    // ── Level 1: X-App-Secret ───
    const incoming = request.headers.get("X-App-Secret") || "";
    if (!env.APP_SECRET || incoming !== env.APP_SECRET) {
      return new Response("Forbidden: bad X-App-Secret", { status: 403 });
    }

    const url = new URL(request.url);
    const isTtsEndpoint = url.pathname === "/tts";

    // ── Level 2: Firebase ID Token (skip for TTS) ───
    if (env.FIREBASE_PROJECT && !isTtsEndpoint) {
      const authHeader = request.headers.get("Authorization") || "";
      const token = authHeader.replace(/^Bearer\s+/i, "").trim();
      if (!token) {
        return new Response("Unauthorized: missing Firebase token", { status: 401 });
      }
      try {
        await verifyFirebaseToken(token, env.FIREBASE_PROJECT);
      } catch (e) {
        return new Response(`Unauthorized: ${e.message}`, { status: 401 });
      }
    }

    // ── Level 3: Rate limit per IP ───
    const ip = request.headers.get("CF-Connecting-IP") || "unknown";
    const limit = parseInt(env.RATE_LIMIT || "20", 10);
    if (!checkRateLimit(ip, limit)) {
      return new Response(
        JSON.stringify({ error: { code: 429, message: `Rate limit exceeded: ${limit} req/min` } }),
        { status: 429, headers: { "Content-Type": "application/json", "Retry-After": "60" } }
      );
    }

    // ── /tts endpoint ───
    if (isTtsEndpoint) {
      return handleTts(request, env);
    }

    // ── Gemini proxy (без fallback — простая прямая трансляция) ───
    const match = url.pathname.match(/^\/v1beta\/models\/([^:]+):(\w+)/);
    if (!match) {
      return new Response("Bad request path", { status: 400 });
    }
    const model = match[1];
    const action = match[2];
    const body = await request.text();

    const isStream = action === "streamGenerateContent";
    const targetUrl =
      `${GEMINI_HOST}/v1beta/models/${model}:${action}?key=${env.GEMINI_API_KEY}` +
      (isStream ? `&alt=sse` : ``);

    const upstreamResp = await fetch(targetUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body,
    });

    const respHeaders = new Headers(upstreamResp.headers);
    respHeaders.set("Access-Control-Allow-Origin", "*");
    respHeaders.set("X-ESPEAK-Model", model);

    return new Response(upstreamResp.body, {
      status: upstreamResp.status,
      statusText: upstreamResp.statusText,
      headers: respHeaders,
    });
  },
};

// ─────────────────────────────────────────────────────────────────
// /tts — Google Cloud Text-to-Speech proxy with edge cache
// ─────────────────────────────────────────────────────────────────
async function handleTts(request, env) {
  const ttsKey = env.GOOGLE_TTS_API_KEY || env.GEMINI_API_KEY;
  if (!ttsKey) {
    return new Response(
      JSON.stringify({ error: "TTS key not configured" }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }

  let payload;
  try {
    payload = await request.json();
  } catch {
    return new Response(JSON.stringify({ error: "Bad JSON body" }),
      { status: 400, headers: { "Content-Type": "application/json" } });
  }

  const text = (payload.text || "").toString().trim();
  if (!text) {
    return new Response(JSON.stringify({ error: "Missing text" }),
      { status: 400, headers: { "Content-Type": "application/json" } });
  }

  const voiceName = (payload.voice || "es-ES-Neural2-D").toString();
  const langMatch = voiceName.match(/^([a-z]{2})-([A-Z]{2})/);
  const languageCode = langMatch ? `${langMatch[1]}-${langMatch[2]}` : "es-ES";
  const speed = clamp(Number(payload.speed) || 1.0, 0.25, 4.0);
  const pitch = clamp(Number(payload.pitch) || 0, -20.0, 20.0);

  // Edge cache lookup
  const cacheKey = await buildTtsCacheKey(text, voiceName, speed, pitch);
  const cache = caches.default;
  const cachedResp = await cache.match(cacheKey);
  if (cachedResp) {
    const h = new Headers(cachedResp.headers);
    h.set("X-ESPEAK-Cache", "HIT");
    return new Response(cachedResp.body, { status: cachedResp.status, headers: h });
  }

  const ttsBody = {
    input: { text: text.slice(0, 5000) },
    voice: { languageCode, name: voiceName },
    audioConfig: { audioEncoding: "MP3", speakingRate: speed, pitch },
  };

  const upstream = await fetch(
    `${TTS_HOST}/v1/text:synthesize?key=${ttsKey}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(ttsBody),
    }
  );

  if (!upstream.ok) {
    const errText = await upstream.text();
    return new Response(
      JSON.stringify({ error: `Cloud TTS HTTP ${upstream.status}`, details: errText.slice(0, 500) }),
      { status: upstream.status, headers: { "Content-Type": "application/json" } }
    );
  }

  const data = await upstream.json();
  const audioBase64 = data.audioContent;
  if (!audioBase64) {
    return new Response(JSON.stringify({ error: "Cloud TTS returned no audioContent" }),
      { status: 502, headers: { "Content-Type": "application/json" } });
  }

  const bin = atob(audioBase64);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);

  const resp = new Response(bytes, {
    status: 200,
    headers: {
      "Content-Type": "audio/mpeg",
      "Cache-Control": "public, max-age=604800, immutable",
      "Access-Control-Allow-Origin": "*",
      "X-ESPEAK-Cache": "MISS",
    },
  });
  await cache.put(cacheKey, resp.clone());
  return resp;
}
