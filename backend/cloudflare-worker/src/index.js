/**
 * HablaRu — Cloudflare Worker proxy для Gemini API.
 *
 * Зачем нужен: API-ключ Gemini не должен попадать в скомпилированный APK
 * (его легко достать декомпиляцией). Worker держит ключ у себя и проксирует
 * запросы, проверяя Firebase ID Token пользователя.
 *
 * Endpoints:
 *   POST /chat     — обычный чат (потребляет history + system_instruction)
 *   POST /grammar  — grammar-check (json-ответ)
 *
 * Auth: Заголовок `Authorization: Bearer <FIREBASE_ID_TOKEN>`.
 *       Worker проверяет подпись токена через Firebase JWKS.
 *
 * Secrets (wrangler secret put <NAME>):
 *   GEMINI_API_KEY         — настоящий ключ Google AI Studio
 *   FIREBASE_PROJECT_ID    — например "spanishapp-35092"
 *
 * Rate limit: настроить через Cloudflare dashboard — например 60 req/min/IP.
 */

const SYSTEM_PROMPT = `Eres un tutor de español amigable y paciente para hablantes de ruso.

REGLAS:
1. Responde SIEMPRE en español, pero incluye traducción al ruso entre [corchetes] para palabras difíciles.
2. Si el usuario escribe en ruso, responde primero en español y luego explica en ruso.
3. Corrige los errores gramaticales del usuario de forma AMABLE:
   - Primero valida lo que dijo bien.
   - Luego muestra la versión corregida con ✏️
   - Explica brevemente el error en ruso.
4. Adapta el nivel: si el usuario parece principiante (A1/A2), usa frases simples.
5. Haz preguntas para mantener la conversación activa.
6. Al final de cada respuesta, incluye una "Palabra del día" relevante al tema.

FORMATO DE CORRECCIÓN (JSON al final del mensaje si hay errores):
CORRECTIONS_JSON:[{"original":"texto con error","corrected":"texto correcto","explanation":"объяснение на русском"}]`;

const GEMINI_MODEL = "gemini-1.5-flash";

export default {
  async fetch(request, env, ctx) {
    if (request.method === "OPTIONS") return cors();
    if (request.method !== "POST") return json({ error: "POST only" }, 405);

    // 1. Аутентификация — Firebase ID token
    const auth = request.headers.get("Authorization") || "";
    const m = auth.match(/^Bearer\s+(.+)$/i);
    if (!m) return json({ error: "Missing Bearer token" }, 401);
    const idToken = m[1];

    const verified = await verifyFirebaseToken(idToken, env.FIREBASE_PROJECT_ID);
    if (!verified.ok) return json({ error: `Invalid token: ${verified.reason}` }, 401);

    // 2. Маршрутизация
    const url = new URL(request.url);
    const route = url.pathname.replace(/\/$/, "");
    let payload;
    try { payload = await request.json(); }
    catch { return json({ error: "Bad JSON body" }, 400); }

    let body;
    if (route === "/chat") body = buildChatBody(payload.messages || [], true);
    else if (route === "/grammar") body = buildChatBody(payload.messages || [], false);
    else return json({ error: "Unknown route" }, 404);

    // 3. Прокси на Gemini
    const geminiUrl =
      `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent` +
      `?key=${env.GEMINI_API_KEY}`;

    const r = await fetch(geminiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const raw = await r.text();
    return new Response(raw, {
      status: r.status,
      headers: {
        "Content-Type": "application/json",
        ...corsHeaders(),
      },
    });
  },
};

// ─── Builders ──────────────────────────────────────────────────────

function buildChatBody(messages, withSystem) {
  const contents = messages.map((m) => ({
    role: m.role === "assistant" ? "model" : "user",
    parts: [{ text: m.content }],
  }));
  const obj = {
    contents,
    generationConfig: { temperature: 0.7, topK: 40, topP: 0.95, maxOutputTokens: 1024 },
  };
  if (withSystem) {
    // Gemini REST: parts must be an array. Object form silently drops on stricter validation.
    obj.system_instruction = { parts: [{ text: SYSTEM_PROMPT }] };
  }
  return obj;
}

// ─── Firebase ID token verification (JWKS) ─────────────────────────

const JWKS_URL = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";
let cachedKeys = null;
let cachedAt = 0;

async function fetchJwks() {
  // Cache 1 hour
  if (cachedKeys && Date.now() - cachedAt < 3600_000) return cachedKeys;
  const r = await fetch(JWKS_URL);
  const data = await r.json();
  cachedKeys = data;
  cachedAt = Date.now();
  return data;
}

async function verifyFirebaseToken(token, projectId) {
  try {
    const [headerB64, payloadB64, sigB64] = token.split(".");
    if (!sigB64) return { ok: false, reason: "malformed" };
    const header = JSON.parse(atob(headerB64));
    const payload = JSON.parse(atob(payloadB64));

    // Check claims
    const now = Math.floor(Date.now() / 1000);
    if (payload.exp < now) return { ok: false, reason: "expired" };
    if (payload.iat > now + 60) return { ok: false, reason: "issued in future" };
    if (payload.aud !== projectId) return { ok: false, reason: "bad aud" };
    if (payload.iss !== `https://securetoken.google.com/${projectId}`)
      return { ok: false, reason: "bad iss" };
    if (!payload.sub) return { ok: false, reason: "no sub" };

    // Verify signature
    const keys = await fetchJwks();
    const certPem = keys[header.kid];
    if (!certPem) return { ok: false, reason: "unknown kid" };

    const cryptoKey = await importRsaPublicKey(certPem);
    const sig = base64UrlToBytes(sigB64);
    const data = new TextEncoder().encode(`${headerB64}.${payloadB64}`);
    const valid = await crypto.subtle.verify(
      { name: "RSASSA-PKCS1-v1_5" },
      cryptoKey,
      sig,
      data
    );
    if (!valid) return { ok: false, reason: "bad signature" };

    return { ok: true, uid: payload.sub };
  } catch (e) {
    return { ok: false, reason: `parse error: ${e.message}` };
  }
}

async function importRsaPublicKey(certPem) {
  // Cert is X.509 PEM. Extract DER, then we need the SPKI (public key) part.
  // Workers Crypto can import "spki" if we strip the cert wrapper.
  // Simpler: use jwks.json instead of x509 — but Firebase only exposes x509 via JWKS_URL.
  // Workaround: use Web Crypto's importKey('jwk') after extracting from cert.
  // For simplicity here, we use the x509 endpoint and extract via custom parser.
  const der = pemToDer(certPem);
  const spki = extractSpkiFromCert(der);
  return crypto.subtle.importKey(
    "spki",
    spki,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["verify"]
  );
}

function pemToDer(pem) {
  const b64 = pem
    .replace(/-----BEGIN CERTIFICATE-----/g, "")
    .replace(/-----END CERTIFICATE-----/g, "")
    .replace(/\s/g, "");
  return base64ToBytes(b64);
}

// Минимальный парсер X.509 для извлечения SubjectPublicKeyInfo.
// Пропускает первые поля сертификата до tbsCertificate.subjectPublicKeyInfo.
// Реализация ASN.1 DER упрощённая — рассчитана на сертификаты Google.
function extractSpkiFromCert(der) {
  // Parse outer SEQUENCE
  let p = 0;
  if (der[p++] !== 0x30) throw new Error("bad cert: no outer SEQ");
  p += readLen(der, p).bytes;
  // tbsCertificate SEQ
  if (der[p++] !== 0x30) throw new Error("no tbsCertificate");
  p += readLen(der, p).bytes;
  // Optional version [0]
  if (der[p] === 0xa0) {
    p++;
    const l = readLen(der, p);
    p += l.bytes + l.value;
  }
  // serialNumber INTEGER
  p = skipTlv(der, p);
  // signature AlgorithmIdentifier
  p = skipTlv(der, p);
  // issuer Name
  p = skipTlv(der, p);
  // validity
  p = skipTlv(der, p);
  // subject Name
  p = skipTlv(der, p);
  // subjectPublicKeyInfo — это и есть SPKI!
  const start = p;
  p = skipTlv(der, p);
  return der.slice(start, p);
}

function readLen(der, p) {
  const first = der[p];
  if (first < 0x80) return { value: first, bytes: 1 };
  const n = first & 0x7f;
  let val = 0;
  for (let i = 1; i <= n; i++) val = (val << 8) | der[p + i];
  return { value: val, bytes: n + 1 };
}
function skipTlv(der, p) {
  p++; // tag
  const l = readLen(der, p);
  return p + l.bytes + l.value;
}
function base64ToBytes(b64) {
  const bin = atob(b64);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
}
function base64UrlToBytes(b64u) {
  const b64 = b64u.replace(/-/g, "+").replace(/_/g, "/") + "===".slice((b64u.length + 3) % 4);
  return base64ToBytes(b64);
}

// ─── HTTP helpers ──────────────────────────────────────────────────

function corsHeaders() {
  return {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, Authorization",
  };
}
function cors() {
  return new Response(null, { status: 204, headers: corsHeaders() });
}
function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "Content-Type": "application/json", ...corsHeaders() },
  });
}
