/**
 * ESPEAK Gemini Proxy (Cloudflare Worker)
 *
 * Прокси для Google Gemini API:
 *   • Проверяет X-App-Secret (защита от случайных гостей)
 *   • Подставляет GEMINI_API_KEY к URL (ключ не светится в APK)
 *   • Автоматический fallback моделей при 429/503 rate-limit:
 *       gemini-flash-latest → gemini-2.0-flash-exp → gemini-1.5-flash
 *   • Поддерживает streamGenerateContent (SSE через Transfer-Encoding: chunked)
 *
 * Env vars (Settings → Variables and Secrets):
 *   • GEMINI_API_KEY = AIza...          (твой Gemini ключ)
 *   • APP_SECRET     = x4O4dIDBG1BV...   (тот же что в local.properties AI_PROXY_SECRET)
 */

const FALLBACK_MODELS = [
  "gemini-flash-latest",
  "gemini-2.0-flash-exp",
  "gemini-1.5-flash",
];

const GEMINI_HOST = "https://generativelanguage.googleapis.com";

export default {
  async fetch(request, env, ctx) {
    // ── 1. CORS preflight (для возможных web-клиентов) ──
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, X-App-Secret",
        },
      });
    }

    // ── 2. Защита: только POST с правильным X-App-Secret ──
    if (request.method !== "POST") {
      return new Response("Method Not Allowed", { status: 405 });
    }
    const incoming = request.headers.get("X-App-Secret") || "";
    if (!env.APP_SECRET || incoming !== env.APP_SECRET) {
      return new Response("Forbidden", { status: 403 });
    }

    // ── 3. Парсим URL: /v1beta/models/<model>:<action> ──
    const url = new URL(request.url);
    const match = url.pathname.match(/^\/v1beta\/models\/([^:]+):(\w+)/);
    if (!match) {
      return new Response("Bad request path. Expected /v1beta/models/<model>:<action>", {
        status: 400,
      });
    }
    const requestedModel = match[1];
    const action = match[2]; // generateContent / streamGenerateContent

    const body = await request.text();

    // ── 4. Пробуем модели по очереди (на 429/503 — fallback) ──
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
        continue; // пробуем следующую модель
      }

      // Возвращаем ответ как есть (включая SSE-streaming)
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

    // Все модели вернули 429/503
    return new Response(
      JSON.stringify({
        error: {
          code: 429,
          message: `All Gemini models rate-limited: ${tried.join(", ")}`,
        },
      }),
      {
        status: 429,
        headers: { "Content-Type": "application/json" },
      }
    );
  },
};
