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

// v1.18.29: голоса разделены на 2 провайдера:
//  • Edge TTS (Azure Neural quality, free unofficial endpoint) — основной
//  • Google Cloud TTS — fallback
//
// Воркер маршрутизирует по voice name: если в имени есть "Neural"
// или "Multilingual" → Edge TTS, иначе → Google.
const ALLOWED_TTS_VOICES = [
  // ─── Edge TTS (Azure Neural) — production голоса ───────────────
  "ru-RU-DmitryNeural", "ru-RU-SvetlanaNeural", "ru-RU-DariyaNeural",
  "en-US-AndrewMultilingualNeural",  // multilingual мужской (говорит по-русски)
  "es-ES-AlvaroNeural", "es-ES-ElviraNeural",
  "es-ES-DarioNeural", "es-ES-XimenaNeural",
  // ─── Google Cloud TTS — fallback / legacy ──────────────────────
  "es-ES-Neural2-A", "es-ES-Neural2-B", "es-ES-Neural2-C",
  "es-ES-Neural2-D", "es-ES-Neural2-E", "es-ES-Neural2-F",
  "es-ES-Studio-C", "es-ES-Studio-F",
  "es-ES-Wavenet-B", "es-ES-Wavenet-C", "es-ES-Wavenet-D",
  "es-ES-Standard-A", "es-ES-Standard-B",
  "es-ES-Standard-C", "es-ES-Standard-D",
  "es-ES-Polyglot-1",
  "ru-RU-Wavenet-A", "ru-RU-Wavenet-B",
  "ru-RU-Wavenet-C", "ru-RU-Wavenet-D", "ru-RU-Wavenet-E",
  "ru-RU-Standard-A", "ru-RU-Standard-B",
  "ru-RU-Standard-C", "ru-RU-Standard-D", "ru-RU-Standard-E",
  "es-US-Neural2-A", "es-US-Neural2-B", "es-US-Neural2-C",
];

// Edge TTS использует Azure Neural voices через free unofficial endpoint
// который использует Microsoft Edge для "Read aloud" feature.
function isEdgeTtsVoice(voice) {
  return voice.includes("Neural") || voice.includes("Multilingual");
}

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

  // v1.18.29: route Azure Neural voices → Edge TTS (free unofficial endpoint)
  if (isEdgeTtsVoice(voice)) {
    try {
      const mp3 = await edgeTtsSynthesize(text, voice, speed, pitch);
      return new Response(mp3, {
        status: 200,
        headers: {
          "Content-Type": "audio/mpeg",
          "Cache-Control": "public, max-age=86400",
          ...corsHeaders(),
        },
      });
    } catch (e) {
      // Edge TTS failure → fallback на Google если возможно
      return jsonError(502, `Edge TTS failed: ${(e.message || e).toString().substring(0, 200)}`);
    }
  }

  // ─── Google Cloud TTS path ─────────────────────────────────────
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

// ────────────────────────────────────────────────────────────────
//  Edge TTS — Azure Neural через unofficial Microsoft endpoint
//  (тот же что использует Edge браузер для "Read aloud" feature)
// ────────────────────────────────────────────────────────────────
//
// Бесплатно, без авторизации. Качество = Azure Neural (премиум).
// Риск: Microsoft теоретически может прикрыть, в этом случае upstream
// вернёт ошибку и клиент сделает fallback на Google.

const EDGE_TTS_TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4";

// v1.18.30: Sec-MS-GEC token — Microsoft anti-abuse в 2024 году
// требует подписанный SHA-256 от timestamp + секрета.
async function generateSecMsGec() {
  // Windows file time: 100-нс интервалов с 1601-01-01
  let ticks = Math.floor((Date.now() / 1000 + 11644473600) * 10000000);
  // Round down to nearest 5 minutes (3000000000 ticks)
  ticks -= ticks % 3000000000;
  const input = `${ticks}${EDGE_TTS_TOKEN}`;
  const encoder = new TextEncoder();
  const hashBuffer = await crypto.subtle.digest("SHA-256", encoder.encode(input));
  return Array.from(new Uint8Array(hashBuffer))
    .map(b => b.toString(16).padStart(2, "0"))
    .join("")
    .toUpperCase();
}

async function edgeTtsSynthesize(text, voice, speed, pitch) {
  const secMsGec = await generateSecMsGec();
  // Cloudflare Workers fetch использует https:// + Upgrade header для WS.
  const url = `https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?TrustedClientToken=${EDGE_TTS_TOKEN}&Sec-MS-GEC=${secMsGec}&Sec-MS-GEC-Version=1-130.0.2849.68`;

  const resp = await fetch(url, {
    headers: {
      "Upgrade": "websocket",
      "Pragma": "no-cache",
      "Cache-Control": "no-cache",
      "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0",
      "Accept-Encoding": "gzip, deflate, br",
      "Accept-Language": "en-US,en;q=0.9",
      "Origin": "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold",
    },
  });
  if (resp.status !== 101) {
    throw new Error(`WS upgrade failed: HTTP ${resp.status}`);
  }
  const ws = resp.webSocket;
  if (!ws) throw new Error("WS not available in response");
  ws.accept();

  const requestId = crypto.randomUUID().replace(/-/g, "");
  const timestamp = new Date().toString();

  return await new Promise((resolve, reject) => {
    const audioChunks = [];
    let timeoutId = setTimeout(() => {
      try { ws.close(); } catch {}
      reject(new Error("Edge TTS timeout (15s)"));
    }, 15000);

    ws.addEventListener("message", async (event) => {
      try {
        if (typeof event.data === "string") {
          // Текстовое сообщение с заголовками — проверяем end-of-turn
          if (event.data.includes("Path:turn.end")) {
            clearTimeout(timeoutId);
            try { ws.close(); } catch {}
            if (audioChunks.length === 0) {
              reject(new Error("Edge TTS returned no audio"));
              return;
            }
            // Конкатенация всех chunks в один Uint8Array
            const totalSize = audioChunks.reduce((s, c) => s + c.length, 0);
            const result = new Uint8Array(totalSize);
            let offset = 0;
            for (const chunk of audioChunks) {
              result.set(chunk, offset);
              offset += chunk.length;
            }
            resolve(result);
          }
        } else {
          // Бинарное сообщение — аудио chunk с header'ом
          const buffer = event.data instanceof ArrayBuffer
            ? event.data
            : await event.data.arrayBuffer();
          const view = new DataView(buffer);
          // Первые 2 байта — длина заголовка (big-endian)
          if (buffer.byteLength < 2) return;
          const headerLength = view.getUint16(0, false);
          if (buffer.byteLength <= 2 + headerLength) return;
          const audioPart = new Uint8Array(buffer, 2 + headerLength);
          audioChunks.push(audioPart);
        }
      } catch (e) {
        clearTimeout(timeoutId);
        try { ws.close(); } catch {}
        reject(e);
      }
    });

    ws.addEventListener("close", () => {
      clearTimeout(timeoutId);
      if (audioChunks.length === 0) {
        reject(new Error("Edge TTS WS closed without audio"));
      }
    });

    ws.addEventListener("error", (e) => {
      clearTimeout(timeoutId);
      try { ws.close(); } catch {}
      reject(new Error(`Edge TTS WS error: ${e.message || "unknown"}`));
    });

    // 1) Send speech config
    const configMsg =
      `X-Timestamp:${timestamp}\r\n` +
      `Content-Type:application/json; charset=utf-8\r\n` +
      `Path:speech.config\r\n\r\n` +
      `{"context":{"synthesis":{"audio":{"metadataoptions":{"sentenceBoundaryEnabled":"false","wordBoundaryEnabled":"false"},"outputFormat":"audio-24khz-48kbitrate-mono-mp3"}}}}`;
    ws.send(configMsg);

    // 2) Send SSML request
    const ratePercent = Math.round((speed - 1.0) * 100);
    const rateStr = ratePercent >= 0 ? `+${ratePercent}%` : `${ratePercent}%`;
    const pitchHz = Math.round(pitch * 10); // semitones → roughly Hz
    const pitchStr = pitchHz >= 0 ? `+${pitchHz}Hz` : `${pitchHz}Hz`;
    const lang = voice.substring(0, 5);
    const ssml =
      `<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='${lang}'>` +
      `<voice name='${voice}'>` +
      `<prosody rate='${rateStr}' pitch='${pitchStr}'>${escapeXml(text)}</prosody>` +
      `</voice></speak>`;
    const ssmlMsg =
      `X-RequestId:${requestId}\r\n` +
      `Content-Type:application/ssml+xml\r\n` +
      `X-Timestamp:${timestamp}\r\n` +
      `Path:ssml\r\n\r\n` +
      ssml;
    ws.send(ssmlMsg);
  });
}

function escapeXml(s) {
  return s
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&apos;");
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
