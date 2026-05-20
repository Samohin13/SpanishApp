// ────────────────────────────────────────────────────────────────
//  ESPEAK — Gemini + TTS proxy for Cloudflare Workers
//  Hides API keys from the Android APK + adds rate limiting.
//
//  Endpoints:
//   POST /v1beta/models/<model>:generateContent
//   POST /v1beta/models/<model>:streamGenerateContent
//   POST /tts  — Google Cloud Text-to-Speech (v1.18.17)
//
//  Auth: X-App-Secret header (rejects unknown callers)
//  Rate limit: 30 req/min/IP, 300/day/IP, 5000/day global
//
//  Secrets in Cloudflare:
//    GEMINI_KEY     — Generative Language API key
//    GOOGLE_TTS_KEY — Cloud Text-to-Speech API key (v1.18.17)
//    APP_SECRET     — shared secret (matches AI_PROXY_SECRET in app)
// ────────────────────────────────────────────────────────────────

const GEMINI_HOST = "https://generativelanguage.googleapis.com";
const TTS_HOST = "https://texttospeech.googleapis.com";

const ALLOWED_MODELS = [
  "gemini-1.5-flash",
  "gemini-1.5-flash-latest",
  "gemini-1.5-flash-002",
  "gemini-1.5-pro",
  "gemini-1.5-pro-latest",
  "gemini-2.0-flash",
  "gemini-2.0-flash-001",
  "gemini-2.0-flash-exp",
  "gemini-2.0-flash-lite",
  "gemini-2.5-flash",
  "gemini-2.5-flash-preview",
  "gemini-2.5-flash-lite",
  "gemini-2.5-pro",
  "gemini-flash-latest",
  "gemini-flash-lite-latest",
];

// v1.18.31: Google Cloud TTS — единственный провайдер (Edge TTS блокируется
// Microsoft для Cloudflare Worker IPs). 8 production голосов + legacy список.
const ALLOWED_TTS_VOICES = [
  // 8 production голосов (используются в PremiumVoiceCatalog)
  "es-ES-Polyglot-1", "es-ES-Neural2-B", "es-ES-Neural2-D", "es-ES-Wavenet-C",
  "ru-RU-Wavenet-A", "ru-RU-Wavenet-B", "ru-RU-Wavenet-C", "ru-RU-Wavenet-D",
  // Legacy Google voices — для старых кэшей
  "es-ES-Neural2-A", "es-ES-Neural2-C", "es-ES-Neural2-E", "es-ES-Neural2-F",
  "es-ES-Studio-C", "es-ES-Studio-F",
  "es-ES-Wavenet-B", "es-ES-Wavenet-D",
  "es-ES-Standard-A", "es-ES-Standard-B",
  "es-ES-Standard-C", "es-ES-Standard-D",
  "ru-RU-Wavenet-E",
  "ru-RU-Standard-A", "ru-RU-Standard-B",
  "ru-RU-Standard-C", "ru-RU-Standard-D", "ru-RU-Standard-E",
  "es-US-Neural2-A", "es-US-Neural2-B", "es-US-Neural2-C",
];

// ── Rate limits ──
const RPM_PER_IP = 30;
const DAILY_PER_IP = 300;
const DAILY_GLOBAL = 5000;

const ipRpmBucket = new Map();
const ipDailyBucket = new Map();
let globalDayKey = "";
let globalDayCount = 0;

function todayKey() {
  return new Date().toISOString().slice(0, 10);
}

function checkRateLimits(ip) {
  const now = Date.now();
  const day = todayKey();

  if (globalDayKey !== day) {
    globalDayKey = day;
    globalDayCount = 0;
  }
  if (globalDayCount >= DAILY_GLOBAL) {
    return { ok: false, status: 429, error: "Global daily quota exceeded — try tomorrow" };
  }

  const rpmEntry = ipRpmBucket.get(ip);
  if (!rpmEntry || now - rpmEntry.windowStart > 60_000) {
    ipRpmBucket.set(ip, { windowStart: now, count: 1 });
  } else {
    rpmEntry.count += 1;
    if (rpmEntry.count > RPM_PER_IP) {
      return { ok: false, status: 429, error: `Rate limit (${RPM_PER_IP} rpm) exceeded` };
    }
  }

  const dailyEntry = ipDailyBucket.get(ip);
  if (!dailyEntry || dailyEntry.day !== day) {
    ipDailyBucket.set(ip, { day, count: 1 });
  } else {
    dailyEntry.count += 1;
    if (dailyEntry.count > DAILY_PER_IP) {
      return { ok: false, status: 429, error: `Daily limit (${DAILY_PER_IP}/day) exceeded` };
    }
  }

  globalDayCount += 1;
  return { ok: true };
}

function safeEquals(a, b) {
  if (typeof a !== "string" || typeof b !== "string") return false;
  if (a.length !== b.length) return false;
  let mismatch = 0;
  for (let i = 0; i < a.length; i++) {
    mismatch |= a.charCodeAt(i) ^ b.charCodeAt(i);
  }
  return mismatch === 0;
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: corsHeaders() });
    }
    if (request.method !== "POST") {
      return jsonError(405, "Method not allowed");
    }

    // ── Shared secret check ──
    if (env.APP_SECRET && env.APP_SECRET.length > 0) {
      const sent = request.headers.get("X-App-Secret") || "";
      if (!safeEquals(sent, env.APP_SECRET)) {
        return jsonError(403, "Forbidden");
      }
    }

    const url = new URL(request.url);

    // v1.18.17: TTS endpoint
    if (url.pathname === "/tts" || url.pathname === "/tts/") {
      return handleTts(request, env);
    }

    // Gemini endpoints
    const match = url.pathname.match(
      /^\/v1beta\/models\/([a-z0-9.\-]+):(stream)?generateContent\/?$/i,
    );
    if (!match) return jsonError(404, "Unknown endpoint");

    const model = match[1];
    const isStream = !!match[2];
    if (!ALLOWED_MODELS.includes(model)) {
      return jsonError(400, `Model ${model} not allowed`);
    }

    const ip = request.headers.get("CF-Connecting-IP") || "anon";
    const limit = checkRateLimits(ip);
    if (!limit.ok) return jsonError(limit.status, limit.error);

    if (!env.GEMINI_KEY) {
      return jsonError(500, "Proxy not configured: missing GEMINI_KEY");
    }

    const verb = isStream ? "streamGenerateContent" : "generateContent";
    const sseSuffix = isStream ? "&alt=sse" : "";
    const upstream = await fetch(
      `${GEMINI_HOST}/v1beta/models/${model}:${verb}?key=${env.GEMINI_KEY}${sseSuffix}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: await request.text(),
      },
    );

    if (isStream) {
      return new Response(upstream.body, {
        status: upstream.status,
        headers: {
          "Content-Type": "text/event-stream",
          "Cache-Control": "no-cache",
          ...corsHeaders(),
        },
      });
    }

    const text = await upstream.text();
    return new Response(text, {
      status: upstream.status,
      headers: { "Content-Type": "application/json", ...corsHeaders() },
    });
  },
};

// ────────────────────────────────────────────────────────────────
//  TTS — Google Cloud Text-to-Speech proxy (v1.18.17)
// ────────────────────────────────────────────────────────────────
//
// Request: POST /tts
//   Body: { "text": "Hola...", "voice": "es-ES-Neural2-A", "speed": 1.0 }
//   Header: X-App-Secret
//
// Response: audio/mpeg (MP3 bytes)
async function handleTts(request, env) {
  const ip = request.headers.get("CF-Connecting-IP") || "anon";
  const limit = checkRateLimits(ip);
  if (!limit.ok) return jsonError(limit.status, limit.error);

  if (!env.GOOGLE_TTS_KEY) {
    return jsonError(500, "TTS not configured: missing GOOGLE_TTS_KEY");
  }

  let body;
  try { body = await request.json(); }
  catch { return jsonError(400, "Bad JSON body"); }

  const text = (body.text || "").trim();
  if (!text) return jsonError(400, "Missing text");
  if (text.length > 2000) return jsonError(400, "Text too long (max 2000 chars)");

  const voice = body.voice || "es-ES-Neural2-A";
  if (!ALLOWED_TTS_VOICES.includes(voice)) {
    return jsonError(400, `Voice ${voice} not allowed`);
  }
  const speed = Math.max(0.5, Math.min(2.0, Number(body.speed) || 1.0));
  const pitch = Math.max(-20, Math.min(20, Number(body.pitch) || 0));

  // languageCode = первые 5 символов voice (es-ES / ru-RU / es-US)
  const languageCode = voice.substring(0, 5);

  const ttsBody = {
    input: { text: text },
    voice: { languageCode: languageCode, name: voice },
    audioConfig: { audioEncoding: "MP3", speakingRate: speed, pitch: pitch },
  };

  const upstream = await fetch(
    `${TTS_HOST}/v1/text:synthesize?key=${env.GOOGLE_TTS_KEY}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(ttsBody),
    },
  );

  if (!upstream.ok) {
    const errText = await upstream.text();
    return jsonError(upstream.status, `TTS failed: ${errText.substring(0, 200)}`);
  }

  // Google TTS возвращает {"audioContent": "<base64>"} — декодируем в bytes.
  const json = await upstream.json();
  if (!json.audioContent) {
    return jsonError(500, "TTS response missing audioContent");
  }
  const binary = atob(json.audioContent);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);

  return new Response(bytes, {
    status: 200,
    headers: {
      "Content-Type": "audio/mpeg",
      "Cache-Control": "public, max-age=86400",
      ...corsHeaders(),
    },
  });
}

function corsHeaders() {
  return {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, X-App-Secret",
    "Access-Control-Max-Age": "86400",
  };
}

function jsonError(status, message) {
  return new Response(JSON.stringify({ error: message }), {
    status,
    headers: { "Content-Type": "application/json", ...corsHeaders() },
  });
}
