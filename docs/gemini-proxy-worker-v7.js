/**
 * ESPEAK Gemini + TTS Proxy v6 (security hardened)
 *
 * НОВОЕ в v6:
 *   • SEC-2: rate limit per Firebase UID (а не per IP)
 *           - PRO: 500 chat-запросов/день
 *           - Free: чат вообще запрещён (PRO-only)
 *   • SEC-7: audit logs всех чат-запросов в R2 (90 day retention)
 *   • SEC-1: /verify-purchase endpoint — проверка покупки через
 *           Google Play Developer API, запись в Firestore
 *
 * Setup в Cloudflare dashboard (всё ОДИН раз):
 *   1. R2 → Create bucket → espeak-tts-cache (если ещё нет)
 *   2. R2 → Create bucket → espeak-audit-logs (НОВЫЙ)
 *   3. KV → Create namespace → espeak-rate-limits (НОВЫЙ)
 *   4. KV → Create namespace → espeak-pro-status (НОВЫЙ — кэш PRO статусов)
 *   5. Worker → Settings → Variables:
 *       Bindings:
 *         TTS_CACHE      → R2 → espeak-tts-cache
 *         AUDIT_LOGS     → R2 → espeak-audit-logs
 *         RATE_LIMITS    → KV → espeak-rate-limits
 *         PRO_STATUS     → KV → espeak-pro-status
 *       Secrets:
 *         APP_SECRET             — общий секрет с Android
 *         GEMINI_API_KEY         — Google AI Studio key
 *         GOOGLE_TTS_API_KEY     — Cloud TTS key
 *         GOOGLE_SERVICE_ACCOUNT — JSON service account (для Play API)
 *         FIREBASE_PROJECT       — "spanishapp-35092"
 *   6. Deploy
 *
 * Endpoints:
 *   POST /v1beta/models/:model::generateContent  — Gemini chat (PRO-only)
 *   POST /translate                              — перевод (всем, лимит 300/день)  [v7]
 *   POST /tts                                    — TTS (всем, R2 cached)
 *   POST /verify-purchase                        — Play purchase validation
 *   POST /rtdn                                   — RTDN webhook от Google Play
 */

const GEMINI_HOST = "https://generativelanguage.googleapis.com";
const TTS_HOST = "https://texttospeech.googleapis.com";
const PLAY_API_HOST = "https://androidpublisher.googleapis.com";

let firebaseKeysCache = { keys: null, fetchedAt: 0 };
let googleAccessTokenCache = { token: null, expiresAt: 0 };

// ─────────────────────────────────────────────────────────────────
// Firebase JWT verification
// ─────────────────────────────────────────────────────────────────

function b64uToBuf(b64u) {
  const b64 = b64u.replace(/-/g, "+").replace(/_/g, "/").padEnd(
    b64u.length + ((4 - (b64u.length % 4)) % 4), "="
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
    "jwk", jwk,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false, ["verify"]
  );
  const sigBuf = b64uToBuf(sigB64);
  const data = new TextEncoder().encode(`${headerB64}.${payloadB64}`);
  const ok = await crypto.subtle.verify("RSASSA-PKCS1-v1_5", cryptoKey, sigBuf, data);
  if (!ok) throw new Error("signature invalid");
  return payload;
}

// ─────────────────────────────────────────────────────────────────
// SEC-2: Rate limit per UID + per-day chat quota
// ─────────────────────────────────────────────────────────────────

const CHAT_DAILY_LIMIT_PRO = 500;
const TTS_DAILY_LIMIT_FREE = 1000;
const TTS_DAILY_LIMIT_PRO = 5000;

async function getDailyCount(env, uid, kind) {
  if (!env.RATE_LIMITS) return 0;
  const day = new Date().toISOString().slice(0, 10);
  const key = `${kind}:${uid}:${day}`;
  const v = await env.RATE_LIMITS.get(key);
  return v ? parseInt(v, 10) : 0;
}

async function bumpDailyCount(env, uid, kind) {
  if (!env.RATE_LIMITS) return;
  const day = new Date().toISOString().slice(0, 10);
  const key = `${kind}:${uid}:${day}`;
  const current = await getDailyCount(env, uid, kind);
  // TTL 36ч: переживёт переход через полночь во всех timezone, потом auto-delete
  await env.RATE_LIMITS.put(key, String(current + 1), { expirationTtl: 36 * 3600 });
}

async function isPro(env, uid) {
  if (!env.PRO_STATUS) return false; // fail-closed: без KV никто не PRO
  const cached = await env.PRO_STATUS.get(`pro:${uid}`);
  return cached === "true";
}

// ─────────────────────────────────────────────────────────────────
// SEC-7: Audit logs в R2 (90 day retention via lifecycle)
// ─────────────────────────────────────────────────────────────────

async function writeAuditLog(env, entry) {
  if (!env.AUDIT_LOGS) return;
  try {
    const ts = new Date().toISOString();
    const key = `${ts.slice(0, 10)}/${ts.replace(/[:.]/g, "-")}-${crypto.randomUUID()}.json`;
    await env.AUDIT_LOGS.put(key, JSON.stringify(entry), {
      httpMetadata: { contentType: "application/json" },
    });
  } catch (e) {
    console.error("Audit log write failed:", e.message);
  }
}

// ─────────────────────────────────────────────────────────────────
// SEC-1: /verify-purchase endpoint
//   Body: { purchaseToken, productId, basePlanId, uid }
//   Запрашивает Google Play Developer API, проверяет статус,
//   пишет в PRO_STATUS KV (cache) — на это смотрит isPro().
// ─────────────────────────────────────────────────────────────────

async function getGoogleAccessToken(env) {
  const now = Math.floor(Date.now() / 1000);
  if (googleAccessTokenCache.token && now < googleAccessTokenCache.expiresAt - 60) {
    return googleAccessTokenCache.token;
  }
  if (!env.GOOGLE_SERVICE_ACCOUNT) throw new Error("GOOGLE_SERVICE_ACCOUNT not configured");
  const sa = JSON.parse(env.GOOGLE_SERVICE_ACCOUNT);
  // JWT для service account auth
  const header = { alg: "RS256", typ: "JWT" };
  const claim = {
    iss: sa.client_email,
    scope: "https://www.googleapis.com/auth/androidpublisher",
    aud: "https://oauth2.googleapis.com/token",
    exp: now + 3600,
    iat: now,
  };
  const headerB64 = btoa(JSON.stringify(header))
    .replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
  const claimB64 = btoa(JSON.stringify(claim))
    .replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
  const signingInput = `${headerB64}.${claimB64}`;
  // Импортируем приватный ключ service account
  const pkcs8 = sa.private_key
    .replace(/-----BEGIN PRIVATE KEY-----/, "")
    .replace(/-----END PRIVATE KEY-----/, "")
    .replace(/\s/g, "");
  const keyBuf = Uint8Array.from(atob(pkcs8), (c) => c.charCodeAt(0)).buffer;
  const cryptoKey = await crypto.subtle.importKey(
    "pkcs8", keyBuf,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false, ["sign"]
  );
  const sig = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5", cryptoKey, new TextEncoder().encode(signingInput)
  );
  const sigB64 = btoa(String.fromCharCode(...new Uint8Array(sig)))
    .replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
  const jwt = `${signingInput}.${sigB64}`;
  const resp = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${jwt}`,
  });
  if (!resp.ok) throw new Error(`OAuth token request failed: ${resp.status}`);
  const data = await resp.json();
  googleAccessTokenCache = {
    token: data.access_token,
    expiresAt: now + (data.expires_in || 3600),
  };
  return data.access_token;
}

async function handleVerifyPurchase(request, env, authUid) {
  let payload;
  try { payload = await request.json(); } catch {
    return new Response(JSON.stringify({ error: "Bad JSON" }), { status: 400 });
  }
  const { purchaseToken, productId } = payload;
  // SEC (v1.25.97): PRO entitlement KV key ДОЛЖЕН браться из проверенного
  // Firebase-токена (authUid), НИКОГДА из тела запроса. Раньше здесь читался
  // `uid` из body — любой обладатель app-secret + одного валидного Firebase
  // токена + одного реально активного purchaseToken мог выставить
  // pro:<произвольный_uid>=true, размножив одну подписку на много аккаунтов.
  const uid = authUid;
  if (!purchaseToken || !productId || !uid) {
    return new Response(JSON.stringify({ error: "Missing required fields" }), { status: 400 });
  }
  try {
    const accessToken = await getGoogleAccessToken(env);
    const packageName = "com.espeak.app";
    const url =
      `${PLAY_API_HOST}/androidpublisher/v3/applications/${packageName}` +
      `/purchases/subscriptionsv2/tokens/${encodeURIComponent(purchaseToken)}`;
    const playResp = await fetch(url, {
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    if (!playResp.ok) {
      const errText = await playResp.text();
      return new Response(
        JSON.stringify({ valid: false, error: `Play API ${playResp.status}`, details: errText.slice(0, 300) }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    }
    const sub = await playResp.json();
    // sub.subscriptionState: SUBSCRIPTION_STATE_ACTIVE / IN_GRACE_PERIOD / PAUSED / EXPIRED / CANCELED
    const isActive =
      sub.subscriptionState === "SUBSCRIPTION_STATE_ACTIVE" ||
      sub.subscriptionState === "SUBSCRIPTION_STATE_IN_GRACE_PERIOD";
    // Запись в KV cache (TTL до expiry или 24ч если active)
    const expiryMs = sub.lineItems?.[0]?.expiryTime
      ? new Date(sub.lineItems[0].expiryTime).getTime()
      : Date.now() + 24 * 3600 * 1000;
    const ttl = Math.max(60, Math.floor((expiryMs - Date.now()) / 1000));
    if (env.PRO_STATUS) {
      await env.PRO_STATUS.put(`pro:${uid}`, isActive ? "true" : "false", {
        expirationTtl: Math.min(ttl, 30 * 86400),
      });
      await env.PRO_STATUS.put(`token:${purchaseToken}`, uid, {
        expirationTtl: Math.min(ttl, 30 * 86400),
      });
    }
    return new Response(JSON.stringify({
      valid: isActive,
      state: sub.subscriptionState,
      expiryTime: sub.lineItems?.[0]?.expiryTime,
    }), { status: 200, headers: { "Content-Type": "application/json" } });
  } catch (e) {
    return new Response(JSON.stringify({ valid: false, error: e.message }),
      { status: 200, headers: { "Content-Type": "application/json" } });
  }
}

// ─────────────────────────────────────────────────────────────────
// /rtdn — Real-time Developer Notifications webhook
//   Google Play -> Pub/Sub -> HTTP push -> этот endpoint.
//   Обрабатывает RENEWED / CANCELED / EXPIRED / REVOKED / REFUNDED.
// ─────────────────────────────────────────────────────────────────

async function handleRtdn(request, env) {
  // RTDN не присылает APP_SECRET — проверяем по Pub/Sub OIDC token
  // (упрощённо: trust IP whitelist Pub/Sub или валидация JWT)
  let body;
  try { body = await request.json(); } catch {
    return new Response("Bad JSON", { status: 400 });
  }
  const message = body.message;
  if (!message || !message.data) return new Response("OK", { status: 200 });
  let notif;
  try {
    notif = JSON.parse(atob(message.data));
  } catch {
    return new Response("Bad data", { status: 400 });
  }
  const subNotif = notif.subscriptionNotification;
  if (!subNotif) return new Response("OK (not subscription)", { status: 200 });
  const { purchaseToken, notificationType } = subNotif;
  // Type 3 = CANCELED, 12 = REVOKED, 13 = EXPIRED, 4 = PURCHASED, 2 = RENEWED
  const inactiveTypes = [3, 12, 13];
  if (env.PRO_STATUS) {
    const uid = await env.PRO_STATUS.get(`token:${purchaseToken}`);
    if (uid) {
      if (inactiveTypes.includes(notificationType)) {
        await env.PRO_STATUS.put(`pro:${uid}`, "false", { expirationTtl: 30 * 86400 });
      } else {
        await env.PRO_STATUS.put(`pro:${uid}`, "true", { expirationTtl: 30 * 86400 });
      }
    }
  }
  await writeAuditLog(env, {
    kind: "rtdn",
    type: notificationType,
    purchaseToken: purchaseToken.slice(0, 16) + "...",
    ts: new Date().toISOString(),
  });
  return new Response("OK", { status: 200 });
}

// ─────────────────────────────────────────────────────────────────
// Main fetch handler
// ─────────────────────────────────────────────────────────────────

function clamp(v, lo, hi) { return Math.min(hi, Math.max(lo, v)); }

async function ttsCacheKey(text, voice, speed, pitch) {
  const raw = `${voice}|${speed}|${pitch}|${text}`;
  const hashBuf = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(raw));
  return Array.from(new Uint8Array(hashBuf))
    .map((b) => b.toString(16).padStart(2, "0")).join("");
}

export default {
  async fetch(request, env) {
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

    const url = new URL(request.url);

    // RTDN webhook — без X-App-Secret (Google его не присылает)
    if (url.pathname === "/rtdn") {
      return handleRtdn(request, env);
    }

    // Level 1: X-App-Secret (для всех остальных endpoints)
    const incoming = request.headers.get("X-App-Secret") || "";
    if (!env.APP_SECRET || incoming !== env.APP_SECRET) {
      return new Response("Forbidden: bad X-App-Secret", { status: 403 });
    }

    // v7: /translate — БЕСПЛАТНЫЙ перевод (книги long-press + WoD «Фраза»).
    // Firebase-токен ОПЦИОНАЛЕН (старые клиенты не шлют) — лимит по uid или IP.
    if (url.pathname === "/translate") {
      return handleTranslate(request, env);
    }

    const isTtsEndpoint = url.pathname === "/tts";
    const isVerifyEndpoint = url.pathname === "/verify-purchase";

    // Level 2: Firebase ID Token (обязателен для всех аутентифицированных endpoints,
    // даже TTS — для per-UID rate limit)
    let uid = null;
    if (env.FIREBASE_PROJECT) {
      const authHeader = request.headers.get("Authorization") || "";
      const token = authHeader.replace(/^Bearer\s+/i, "").trim();
      if (!token) {
        return new Response("Unauthorized: missing Firebase token", { status: 401 });
      }
      try {
        const payload = await verifyFirebaseToken(token, env.FIREBASE_PROJECT);
        uid = payload.sub;
      } catch (e) {
        return new Response(`Unauthorized: ${e.message}`, { status: 401 });
      }
    }

    if (isVerifyEndpoint) {
      // uid берётся из проверенного Firebase-токена выше, не из тела запроса.
      if (!uid) {
        return new Response("Unauthorized: verify requires Firebase token", { status: 401 });
      }
      return handleVerifyPurchase(request, env, uid);
    }

    if (isTtsEndpoint) {
      // TTS: лимит per UID (free 1000/day, PRO 5000/day)
      if (uid) {
        const pro = await isPro(env, uid);
        const limit = pro ? TTS_DAILY_LIMIT_PRO : TTS_DAILY_LIMIT_FREE;
        const used = await getDailyCount(env, uid, "tts");
        if (used >= limit) {
          return new Response(
            JSON.stringify({ error: { code: 429, message: `TTS daily limit ${limit}` } }),
            { status: 429, headers: { "Content-Type": "application/json", "Retry-After": "3600" } }
          );
        }
      }
      const ttsResp = await handleTts(request, env);
      // Bump только если это был cache miss (Google API вызывался)
      if (uid && ttsResp.headers.get("X-ESPEAK-Cache") === "MISS") {
        await bumpDailyCount(env, uid, "tts");
      }
      return ttsResp;
    }

    // Gemini chat: ТОЛЬКО PRO, лимит 500/day
    if (!uid) return new Response("Unauthorized", { status: 401 });
    const userIsPro = await isPro(env, uid);
    if (!userIsPro) {
      await writeAuditLog(env, {
        kind: "chat_denied_free",
        uid: uid.slice(0, 8),
        ts: new Date().toISOString(),
      });
      return new Response(
        JSON.stringify({ error: { code: 403, message: "Chat is PRO-only" } }),
        { status: 403, headers: { "Content-Type": "application/json" } }
      );
    }
    const used = await getDailyCount(env, uid, "chat");
    if (used >= CHAT_DAILY_LIMIT_PRO) {
      return new Response(
        JSON.stringify({ error: { code: 429, message: `Daily chat limit ${CHAT_DAILY_LIMIT_PRO}` } }),
        { status: 429, headers: { "Content-Type": "application/json", "Retry-After": "3600" } }
      );
    }

    // Gemini proxy
    const match = url.pathname.match(/^\/v1beta\/models\/([^:]+):(\w+)/);
    if (!match) return new Response("Bad request path", { status: 400 });
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

    await bumpDailyCount(env, uid, "chat");
    // Audit: храним только метаданные, не тело сообщений (privacy)
    await writeAuditLog(env, {
      kind: "chat",
      uid: uid.slice(0, 8),
      model,
      action,
      status: upstreamResp.status,
      bodyBytes: body.length,
      ts: new Date().toISOString(),
    });

    const respHeaders = new Headers(upstreamResp.headers);
    respHeaders.set("Access-Control-Allow-Origin", "*");
    respHeaders.set("X-ESPEAK-Model", model);
    respHeaders.set("X-ESPEAK-DailyUsed", String(used + 1));
    respHeaders.set("X-ESPEAK-DailyLimit", String(CHAT_DAILY_LIMIT_PRO));
    return new Response(upstreamResp.body, {
      status: upstreamResp.status,
      statusText: upstreamResp.statusText,
      headers: respHeaders,
    });
  },
};

// ─────────────────────────────────────────────────────────────────
// v7: /translate — бесплатный узкий перевод. НЕ произвольный прокси:
// принимаем только {contents:[{parts:[{text}]}]}, текст ≤500 симв.,
// модель и generationConfig фиксированы на сервере (maxOutputTokens 80).
// Лимит 300/день per uid (если токен передан) либо per IP.
// ─────────────────────────────────────────────────────────────────
const TRANSLATE_DAILY_LIMIT = 300;

async function handleTranslate(request, env) {
  let limitKey = "ip:" + (request.headers.get("CF-Connecting-IP") || "unknown");
  const authHeader = request.headers.get("Authorization") || "";
  const token = authHeader.replace(/^Bearer\s+/i, "").trim();
  if (token && env.FIREBASE_PROJECT) {
    try {
      limitKey = "uid:" + (await verifyFirebaseToken(token, env.FIREBASE_PROJECT)).sub;
    } catch (_) { /* невалидный токен → остаёмся на IP-лимите */ }
  }

  const used = await getDailyCount(env, limitKey, "translate");
  if (used >= TRANSLATE_DAILY_LIMIT) {
    return new Response(
      JSON.stringify({ error: { code: 429, message: `Translate daily limit ${TRANSLATE_DAILY_LIMIT}` } }),
      { status: 429, headers: { "Content-Type": "application/json", "Retry-After": "3600" } }
    );
  }

  let body;
  try { body = await request.json(); } catch (_) {
    return new Response("Bad JSON", { status: 400 });
  }
  const text = body?.contents?.[0]?.parts?.[0]?.text || "";
  if (!text || typeof text !== "string" || text.length > 500) {
    return new Response("Bad request: text required, ≤500 chars", { status: 400 });
  }

  const payload = {
    contents: [{ parts: [{ text }] }],
    generationConfig: { temperature: 0.2, maxOutputTokens: 80 },
  };
  const upstream = await fetch(
    `${GEMINI_HOST}/v1beta/models/gemini-flash-latest:generateContent?key=${env.GEMINI_API_KEY}`,
    { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) }
  );
  await bumpDailyCount(env, limitKey, "translate");

  const h = new Headers(upstream.headers);
  h.set("Access-Control-Allow-Origin", "*");
  h.set("X-ESPEAK-DailyUsed", String(used + 1));
  h.set("X-ESPEAK-DailyLimit", String(TRANSLATE_DAILY_LIMIT));
  return new Response(upstream.body, { status: upstream.status, headers: h });
}

// ─────────────────────────────────────────────────────────────────
// /tts с R2 permanent cache + edge cache fallback (без изменений от v5)
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

  const hashHex = await ttsCacheKey(text, voiceName, speed, pitch);
  const r2Key = `v1/${hashHex}.mp3`;

  // 1. R2 (permanent)
  if (env.TTS_CACHE) {
    try {
      const cached = await env.TTS_CACHE.get(r2Key);
      if (cached) {
        return new Response(cached.body, {
          status: 200,
          headers: {
            "Content-Type": "audio/mpeg",
            "Cache-Control": "public, max-age=604800, immutable",
            "Access-Control-Allow-Origin": "*",
            "X-ESPEAK-Cache": "R2-HIT",
          },
        });
      }
    } catch (e) {
      console.error("R2 read failed:", e.message);
    }
  }

  // 2. Edge cache
  const edgeCacheKey = new Request(`https://espeak-tts-cache.local/${hashHex}`, { method: "GET" });
  const cache = caches.default;
  const edgeCached = await cache.match(edgeCacheKey);
  if (edgeCached) {
    const h = new Headers(edgeCached.headers);
    h.set("X-ESPEAK-Cache", "EDGE-HIT");
    return new Response(edgeCached.body, { status: edgeCached.status, headers: h });
  }

  // 3. Cache miss — Google
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

  // 4. Save R2 + edge
  if (env.TTS_CACHE) {
    try {
      await env.TTS_CACHE.put(r2Key, bytes, {
        httpMetadata: { contentType: "audio/mpeg" },
      });
    } catch (e) {
      console.error("R2 write failed:", e.message);
    }
  }

  const resp = new Response(bytes, {
    status: 200,
    headers: {
      "Content-Type": "audio/mpeg",
      "Cache-Control": "public, max-age=604800, immutable",
      "Access-Control-Allow-Origin": "*",
      "X-ESPEAK-Cache": "MISS",
    },
  });
  await cache.put(edgeCacheKey, resp.clone());
  return resp;
}
