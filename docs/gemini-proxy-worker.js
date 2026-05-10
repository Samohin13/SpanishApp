// ────────────────────────────────────────────────────────────────
//  ESPEAK — Gemini API proxy for Cloudflare Workers
//  Hides the GEMINI_KEY from the Android APK so it cannot be
//  extracted and abused.
//
//  Hardening (v2):
//   • Shared secret in X-App-Secret header (rejects unknown callers)
//   • Per-IP rate limit  : 30 req/min  (in-memory bucket)
//   • Per-IP daily cap   : 300 req/day (per worker isolate)
//   • Global daily cap   : 5000 req/day across all users (in-memory)
//   • Allowed-models whitelist
//
// ────────────────────────────────────────────────────────────────
//  Deploy:
//    1. https://dash.cloudflare.com/ → Workers & Pages → Create
//    2. Pick "Hello World" template, name it `espeak-gemini-proxy`
//    3. Paste this entire file as the worker code
//    4. Settings → Variables → "Add variable" (Encrypt = ON):
//         GEMINI_KEY  = <your real Google Generative Language API key>
//         APP_SECRET  = <random 32-char string — paste same one to
//                       local.properties as AI_PROXY_SECRET>
//    5. Deploy. Copy the public URL, e.g.:
//         https://espeak-gemini-proxy.<your-account>.workers.dev
//    6. local.properties:
//         AI_PROXY_URL=https://espeak-gemini-proxy.<your-account>.workers.dev
//         AI_PROXY_SECRET=<same random string as APP_SECRET>
//    7. Rebuild the app.
//
//  How to generate a random secret:
//    Linux/Mac:  openssl rand -hex 32
//    PowerShell: -join ((48..57)+(97..122) | Get-Random -Count 32 | % {[char]$_})
//    Or use:     https://www.random.org/strings/
// ────────────────────────────────────────────────────────────────

const GEMINI_HOST = "https://generativelanguage.googleapis.com";

const ALLOWED_MODELS = [
  "gemini-1.5-flash",
  "gemini-1.5-flash-latest",
  "gemini-1.5-flash-002",
  "gemini-1.5-pro",
  "gemini-1.5-pro-latest",
  "gemini-2.0-flash",
  "gemini-2.0-flash-001",
  "gemini-2.0-flash-exp",
  "gemini-2.5-flash",
  "gemini-2.5-flash-preview",
  "gemini-flash-latest",
];

// ── Rate limits ──
const RPM_PER_IP = 30;            // 30 requests per minute per IP
const DAILY_PER_IP = 300;         // 300 requests per day per IP
const DAILY_GLOBAL = 5000;        // 5000 requests per day across ALL users
                                  // (protects your Gemini free-tier quota)

// In-memory state — per worker isolate. Cloudflare may spin up multiple
// isolates so these counters are an *approximate* floor, not exact.
// For exact cross-isolate counters use Cloudflare KV or Durable Objects.
const ipRpmBucket = new Map();    // ip → { windowStart, count }
const ipDailyBucket = new Map();  // ip → { day, count }
let globalDayKey = "";
let globalDayCount = 0;

function todayKey() {
  return new Date().toISOString().slice(0, 10);   // "YYYY-MM-DD" UTC
}

function checkRateLimits(ip) {
  const now = Date.now();
  const day = todayKey();

  // Reset global counter on day boundary
  if (globalDayKey !== day) {
    globalDayKey = day;
    globalDayCount = 0;
  }
  if (globalDayCount >= DAILY_GLOBAL) {
    return { ok: false, status: 429, error: "Global daily quota exceeded — try tomorrow" };
  }

  // Per-IP per-minute
  const rpmEntry = ipRpmBucket.get(ip);
  if (!rpmEntry || now - rpmEntry.windowStart > 60_000) {
    ipRpmBucket.set(ip, { windowStart: now, count: 1 });
  } else {
    rpmEntry.count += 1;
    if (rpmEntry.count > RPM_PER_IP) {
      return { ok: false, status: 429, error: `Rate limit (${RPM_PER_IP} rpm) exceeded` };
    }
  }

  // Per-IP per-day
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

// Constant-time string compare to defeat timing attacks on the secret.
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
    // If APP_SECRET is configured on Cloudflare, every request MUST include
    // the matching X-App-Secret header. If APP_SECRET is empty, the check
    // is skipped (back-compat with old deployments).
    if (env.APP_SECRET && env.APP_SECRET.length > 0) {
      const sent = request.headers.get("X-App-Secret") || "";
      if (!safeEquals(sent, env.APP_SECRET)) {
        return jsonError(403, "Forbidden");
      }
    }

    const url = new URL(request.url);
    // Expected paths:
    //   /v1beta/models/<model>:generateContent       (one-shot)
    //   /v1beta/models/<model>:streamGenerateContent (Server-Sent Events stream)
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

    // Forward body as-is to Gemini, appending the secret key as a query param.
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
