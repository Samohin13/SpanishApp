# ESPEAK — Cloudflare Worker proxy для Gemini

Зачем нужен: **API-ключ Gemini не должен быть в скомпилированном APK** (любой декомпилирует и достанет). Worker держит ключ у себя и проксирует запросы, проверяя Firebase ID-token аутентифицированных пользователей.

## Что делает Worker

1. Принимает `POST /chat` или `POST /grammar` от Android-приложения.
2. Проверяет заголовок `Authorization: Bearer <Firebase ID Token>` через Firebase JWKS — без валидного токена возвращает 401.
3. Проксирует запрос на `generativelanguage.googleapis.com` с ключом из секретов Worker.
4. Возвращает ответ Gemini как есть.

## Бесплатный тариф

Cloudflare Workers Free: **100 000 запросов/день**, 10 мс CPU/запрос. Этого хватит на тысячи активных пользователей. Если упрёшься — `$5/мес = 10M`.

## Установка (5 минут)

### 1. Установи wrangler CLI

```powershell
npm install -g wrangler
```

### 2. Авторизуйся в Cloudflare

```powershell
wrangler login
```

(откроется браузер; зарегистрируй бесплатный аккаунт если нет)

### 3. Поставь зависимости

```powershell
cd backend\cloudflare-worker
npm install
```

### 4. Задай секреты

```powershell
wrangler secret put GEMINI_API_KEY
# вставь ключ Google AI Studio (https://aistudio.google.com/app/apikey)

wrangler secret put FIREBASE_PROJECT_ID
# вставь spanishapp-35092 (см. google-services.json)
```

### 5. Деплой

```powershell
wrangler deploy
```

В выводе будет URL вроде `https://ESPEAK-ai-proxy.YOUR-USERNAME.workers.dev` — это твой proxy.

### 6. (опционально) Custom domain

В Cloudflare Dashboard → Workers → Routes — привяжи к своему домену, чтобы было `https://ai.ESPEAK.app/chat`.

## Переключение Android-приложения на прокси

После деплоя нужно обновить `AiChatRepository.kt` чтобы он ходил на твой Worker вместо прямого вызова Gemini.

### Шаг 1. Добавить URL в build.gradle.kts

```kotlin
defaultConfig {
    // ... существующее ...
    val proxyUrl = localProps.getProperty("AI_PROXY_URL") ?: ""
    buildConfigField("String", "AI_PROXY_URL", "\"$proxyUrl\"")
}
```

### Шаг 2. В local.properties

```properties
AI_PROXY_URL=https://ESPEAK-ai-proxy.YOUR-USERNAME.workers.dev
```

### Шаг 3. В AiChatRepository.kt

Заменить две функции:

```kotlin
private fun apiUrl() = BuildConfig.AI_PROXY_URL  // вместо прямого Gemini URL

// + добавить header в Request.Builder:
val idToken = FirebaseAuth.getInstance().currentUser
    ?.getIdToken(false)?.await()?.token
    ?: throw Exception("Not authenticated")

val request = Request.Builder()
    .url("${BuildConfig.AI_PROXY_URL}/chat")  // или /grammar
    .post(body)
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer $idToken")
    .build()
```

### Шаг 4. УБРАТЬ из `defaultConfig` строки с `GEMINI_API_KEY`

И **отозвать старый ключ** в Google AI Studio (ребайнди в личном кабинете) — даже если он уже в каких-то APK, он перестанет работать.

## Локальная разработка

```powershell
wrangler dev
```

Запустит локальный сервер на `http://localhost:8787`. В local.properties Android приложения временно меняешь `AI_PROXY_URL=http://10.0.2.2:8787` (для эмулятора).

## Логи и метрики

```powershell
wrangler tail
```

Стрим логов в реальном времени. Видишь все запросы, ошибки авторизации и т.п.

В Cloudflare Dashboard → Workers → твой Worker → Metrics: графики вызовов, времени, ошибок.

## Безопасность

- ✅ API-ключ Gemini больше не в APK
- ✅ Только аутентифицированные Firebase-юзеры могут дёргать AI (без email/Google sign-in или Anonymous Auth — 401)
- ⚠️ Установи rate-limit в Cloudflare Dashboard → Security → Rate Limiting (например 60 req/min на IP) чтобы один юзер не выжрал твою квоту Gemini.
- ⚠️ Подумай о Firebase App Check — дополнительная защита от bot-атак на твой Worker. См. [docs](https://firebase.google.com/docs/app-check).
