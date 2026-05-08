// ────────────────────────────────────────────────────────────────
//  ESPEAK — Gemini API proxy for Cloudflare Workers
//  Hides the GEMINI_KEY from the Android APK so it cannot be
//  extracted and abused.
//
//  Deploy:
//    1. Go to https://dash.cloudflare.com/ → Workers & Pages → Create
//    2. Pick "Hello World" template, name it `espeak-gemini-proxy`
//    3. Replace the worker code with the contents of this file
//    4. Settings → Variables → "Add variable" → Encrypt
//         Name:  GEMINI_KEY
//         Value: <your real Google Generative Language API key>
//    5. Deploy. Copy the public URL, e.g.:
//         https://espeak-gemini-proxy.<your-account>.workers.dev
//    6. Paste it into local.properties:
//         AI_PROXY_URL=https://espeak-gemini-proxy.<your-account>.workers.dev
//    7. Rebuild the app. Remove GEMINI_KEY from local.properties on Play
//       release builds (the proxy is the only path).
//
//  Free tier: 100,000 requests/day, 10ms CPU/req — plenty for a
//  Spanish-learning app.
// ────────────────────────────────────────────────────────────────

const GEMINI_HOST = "https://generativelanguage.googleapis.com";

// Allowed Gemini model paths the proxy will forward to.
// Block everything else so the proxy cannot be repurposed by attackers.
const ALLOWED_MODELS = [
  "gemini-1.5-flash",
  "gemini-1.5-pro",
];

// Light per-IP rate limit. The free Workers KV / Durable Objects
// would be more accurate; for a simple in-memory cap we rely on
// Cloudflare's edge isolate restart policy. Refine later if abuse appears.
const RPM_PER_IP = 30;
const ipBucket = new Map();

function rateLimited(ip) {
  const now = Date.now();
  const entry = ipBucket.get(ip);
  if (!entry || now - entry.windowStart > 60_000) {
    ipBucket.set(ip, { windowStart: now, count: 1 });
    return false;
  }
  entry.count += 1;
  return entry.count > RPM_PER_IP;
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: corsHeaders(),
      });
    }

    if (request.method !== "POST") {
      return jsonError(405, "Method not allowed");
    }

    const url = new URL(request.url);
    // Expected path: /v1beta/models/<model>:generateContent
    const match = url.pathname.match(
      /^\/v1beta\/models\/([a-z0-9.\-]+):generateContent\/?$/i,
    );
    if (!match) return jsonError(404, "Unknown endpoint");

    const model = match[1];
    if (!ALLOWED_MODELS.includes(model)) {
      return jsonError(400, `Model ${model} not allowed`);
    }

    const ip = request.headers.get("CF-Connecting-IP") || "anon";
    if (rateLimited(ip)) return jsonError(429, "Rate limit (30 rpm) exceeded");

    if (!env.GEMINI_KEY) {
      return jsonError(500, "Proxy not configured: missing GEMINI_KEY");
    }

    // Forward body as-is to Gemini, appending the secret key as a query param.
    const upstream = await fetch(
      `${GEMINI_HOST}/v1beta/models/${model}:generateContent?key=${env.GEMINI_KEY}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: await request.text(),
      },
    );

    // Mirror upstream response (body + status), strip non-CORS-safe headers.
    const text = await upstream.text();
    return new Response(text, {
      status: upstream.status,
      headers: {
        "Content-Type": "application/json",
        ...corsHeaders(),
      },
    });
  },
};

function corsHeaders() {
  return {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type",
    "Access-Control-Max-Age": "86400",
  };
}

function jsonError(status, message) {
  return new Response(JSON.stringify({ error: message }), {
    status,
    headers: { "Content-Type": "application/json", ...corsHeaders() },
  });
}
