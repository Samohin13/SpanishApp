package com.spanishapp.radio.data

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RadioDiscovery"

/** Максимум станций одного «бренда» (Cadena SER, RNE...) в каталоге. */
private const val MAX_PER_BRAND = 2

/** Допустимые схемы URL потока. */
private val SAFE_SCHEMES = setOf("http", "https", "rtsp")

/**
 * Нормализованный «brand key» из названия станции — для дедупликации
 * по семьям. Берём первые 2 значимых слова в lowercase.
 *
 * Примеры:
 *   "Cadena SER España"       → "cadena ser"
 *   "Cadena SER Radio Madrid" → "cadena ser"
 *   "RNE Radio 1"             → "rne radio"
 *   "Los 40 Principales"      → "los 40"
 *   "Caracol"                 → "caracol"
 *
 * Visible for testing.
 */
internal fun brandKey(name: String): String {
    val clean = name.lowercase()
        .replace(Regex("[^\\p{L}0-9\\s]"), " ")   // буквы (вкл. диакритику) + цифры
        .replace(Regex("\\s+"), " ").trim()
    val parts = clean.split(" ").filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> ""
        parts.size == 1 -> parts[0]
        else -> "${parts[0]} ${parts[1]}"
    }
}

/**
 * Из списка станций оставляет не более MAX_PER_BRAND штук на бренд.
 * Это даёт юзеру вариативность брендов вместо нескольких региональных
 * вариантов одной сети (например 5 «Cadena SER» в разных городах).
 *
 * Порядок сохраняется — приоритет первым найденным (обычно более популярным).
 *
 * Visible for testing.
 */
internal fun deduplicateByBrand(
    stations: List<DiscoveredStation>,
    maxPerBrand: Int = MAX_PER_BRAND,
): List<DiscoveredStation> {
    val brandCount = mutableMapOf<String, Int>()
    return stations.filter { st ->
        val brand = brandKey(st.name)
        val count = brandCount.getOrDefault(brand, 0)
        if (count < maxPerBrand) {
            brandCount[brand] = count + 1
            true
        } else false
    }
}

/**
 * URL-схема whitelist. radio-browser.info — user-submitted каталог,
 * теоретически может вернуть `javascript:`, `file:`, `content:` и т.п.
 * ExoPlayer корректно их обрабатывает, но мы лучше явно отсечём
 * чтобы исключить любые сюрпризы с локальным контентом.
 *
 * Visible for testing — поэтому top-level internal.
 */
internal fun isSafeStreamUrl(url: String): Boolean {
    val scheme = runCatching { java.net.URI(url).scheme?.lowercase() }.getOrNull()
    return scheme in SAFE_SCHEMES
}

/**
 * Auto-discovery радиостанций под страну юзера:
 * 1. Определяем страну юзера через ip-API (если нет интернета — через Locale).
 * 2. Запрашиваем radio-browser.info: испаноязычные станции по жанрам.
 * 3. ПРОБИМ каждый URL — открываем, читаем 2KB, проверяем что отвечает.
 * 4. Кэшируем 40 живых в Room (RadioCatalogEntity).
 * 5. UI читает из кэша. Перепроверка раз в неделю через WorkManager.
 */
@Singleton
class RadioCatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: RadioCatalogDao,
) {
    /** Клиент для API-запросов (radio-browser, country.is) — JSON, более долгий таймаут. */
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    /**
     * Отдельный клиент для probe — 5 сек на ВСЁ.
     * Если стрим за 5 сек не отдал первые байты — он мёртвый.
     * Раньше использовали один клиент с 12с таймаутом → probe 100 URL
     * занимал до 2.5 минут (12с × 100/8 параллельных).
     */
    private val probeClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.SECONDS)  // жёсткий лимит на всю операцию
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /** Сообщение об ошибке если discovery вернул 0 станций. UI его показывает. */
    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    fun clearError() { _lastErrorMessage.value = null }

    /** Лог + Crashlytics breadcrumb для диагностики цепочки auto-discovery. */
    private fun trace(msg: String) {
        Log.d(TAG, msg)
        runCatching {
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().log("[$TAG] $msg")
        }
    }

    private fun reportError(stage: String, t: Throwable? = null) {
        val msg = "[$TAG] $stage failed${t?.message?.let { ": $it" } ?: ""}"
        Log.w(TAG, msg, t)
        runCatching {
            val fc = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
            if (t != null) fc.recordException(RuntimeException(msg, t)) else fc.log(msg)
        }
    }

    /** Прогресс fetch'а для UI: 0..1. */
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    /** Cколько живых найдено в последнем сканировании. */
    private val _foundCount = MutableStateFlow(0)
    val foundCount: StateFlow<Int> = _foundCount.asStateFlow()

    /**
     * Текущий этап discovery — для информативного баннера в UI.
     * Раньше юзер видел просто «0%» во время медленного fetchPool (до минуты)
     * и думал что всё зависло.
     */
    enum class DiscoveryStage { IDLE, DETECTING_COUNTRY, FETCHING_CATALOG, PROBING, DONE }
    private val _stage = MutableStateFlow(DiscoveryStage.IDLE)
    val stage: StateFlow<DiscoveryStage> = _stage.asStateFlow()

    /** Свежесть кэша: < 7 дней. */
    suspend fun isCacheFresh(): Boolean {
        val last = dao.lastFetchedAt() ?: return false
        val age = System.currentTimeMillis() - last
        return age < 7 * 24 * 60 * 60 * 1000L  // 7 дней
    }

    suspend fun hasCache(): Boolean = dao.count() > 0

    /**
     * Дозапросить ещё станций и добавить к существующему каталогу
     * БЕЗ очистки. Используется по тапу «+ Найти ещё».
     *
     * Дубликаты автоматически отсеиваются — id станции = stable hash от URL,
     * INSERT OR REPLACE в DAO обновит запись если URL уже есть.
     *
     * @return сколько НОВЫХ станций добавилось (не считая обновлённых).
     */
    suspend fun discoverMore(targetCount: Int = 20): Int = withContext(Dispatchers.IO) {
        _progress.value = 0f

        // Дозапрос ищет НОВЫЕ БРЕНДЫ. Юзер тапнул «Найти ещё» — он хочет
        // разнообразия, а не очередной региональный вариант существующих.
        // Раньше (v1.10.5) фильтровали по MAX_PER_BRAND=2, но это давало
        // фактически 0 новых если все основные сети уже заполнены —
        // юзер видел «ничего не добавляется».
        //
        // Теперь: фильтруем по «есть ли бренд уже в кэше». Если SER уже
        // есть — все новые SER скипаются, ищем COPE/Onda/Europa FM итд.
        val existing = dao.getAll()
        val existingUrls = existing.map { it.streamUrl }.toSet()
        val existingBrands = existing.map { brandKey(it.name) }.toSet()

        val userCountry = detectUserCountry()
        val rawPool = fetchPool()
            .filter { it.streamUrl !in existingUrls }
            .filter { brandKey(it.name) !in existingBrands }  // только НОВЫЕ бренды
        if (rawPool.isEmpty()) {
            _lastErrorMessage.value = "Все доступные сети уже в каталоге"
            _progress.value = 1f
            return@withContext 0
        }

        val total = rawPool.size
        val working = java.util.Collections.synchronizedList(mutableListOf<DiscoveredStation>())
        val processed = java.util.concurrent.atomic.AtomicInteger(0)

        run probeLoop@ {
            rawPool.chunked(8).forEach { batch ->
                coroutineScope {
                    batch.map { st ->
                        async {
                            if (probe(st.streamUrl)) {
                                working.add(st)
                            }
                            val p = processed.incrementAndGet()
                            _progress.value = p.toFloat() / total
                        }
                    }.awaitAll()
                }
                if (working.size >= targetCount) return@probeLoop
            }
        }

        val now = System.currentTimeMillis()
        working.take(targetCount).forEach { st ->
            dao.upsert(
                stationId = st.id,
                shortCode = st.shortCode,
                name = st.name,
                program = st.program,
                frequency = st.frequency,
                country = st.country.name,
                genre = st.genre.name,
                level = st.level.name,
                streamUrl = st.streamUrl,
                bitrate = st.bitrate,
                userCountry = userCountry,
                fetchedAt = now,
            )
        }
        _foundCount.value = working.size
        _progress.value = 1f
        working.size.coerceAtMost(targetCount)
    }

    /**
     * Главный метод: запросить + probe + закэшировать (с очисткой).
     * Возвращает количество найденных рабочих станций.
     */
    suspend fun discoverAndCache(): Int = withContext(Dispatchers.IO) {
        _progress.value = 0f
        _foundCount.value = 0
        _lastErrorMessage.value = null

        trace("discoverAndCache: START")
        _stage.value = DiscoveryStage.DETECTING_COUNTRY
        _progress.value = 0.05f
        val userCountry = detectUserCountry()
        trace("user country = $userCountry")
        _progress.value = 0.10f

        _stage.value = DiscoveryStage.FETCHING_CATALOG
        val rawPool = fetchPool()
        trace("fetchPool returned ${rawPool.size} candidates")
        _progress.value = 0.30f

        if (rawPool.isEmpty()) {
            reportError("fetchPool returned 0 (radio-browser API down или rate-limit)")
            _lastErrorMessage.value = "Не удалось получить список станций. Проверь интернет."
            _progress.value = 1f
            return@withContext 0
        }

        // Probe — параллельно по 8 URL за раз. Прогресс: 0.30 → 1.0
        // - per-probe прогресс через AtomicInteger (плавная анимация вместо рывков по 8%)
        // - реальный break когда нашли 40+ живых (раньше return@forEach был continue!)
        _stage.value = DiscoveryStage.PROBING
        val total = rawPool.size
        val working = java.util.Collections.synchronizedList(mutableListOf<DiscoveredStation>())
        val processed = java.util.concurrent.atomic.AtomicInteger(0)

        run probeLoop@ {
            rawPool.chunked(8).forEach { batch ->
                coroutineScope {
                    batch.map { st ->
                        async {
                            if (probe(st.streamUrl)) {
                                working.add(st)
                                _foundCount.value = working.size
                            }
                            val p = processed.incrementAndGet()
                            _progress.value = 0.30f + 0.70f * (p.toFloat() / total)
                        }
                    }.awaitAll()
                }
                // Реальный break — return@probeLoop выходит ИЗ всего forEach
                if (working.size >= 40) return@probeLoop
            }
        }

        trace("probe: ${working.size}/${rawPool.size} живых из пула")

        if (working.isEmpty()) {
            reportError("probe: 0 живых из ${rawPool.size} (все URL не отвечают — возможно блокировка провайдера)")
            _lastErrorMessage.value = "Все станции недоступны из твоей сети. Попробуй мобильный интернет или VPN."
            _progress.value = 1f
            return@withContext 0
        }

        // Балансируем: 24 ES + 8 MX + 8 AR
        val final = balanceByCountry(working)
        trace("balanced: ${final.size} (ES=${final.count { it.country == Country.SPAIN }} MX=${final.count { it.country == Country.MEXICO }} AR=${final.count { it.country == Country.ARGENTINA }})")

        // Сохраняем в Room
        dao.clear()
        val now = System.currentTimeMillis()
        final.forEachIndexed { idx, st ->
            dao.upsert(
                stationId = st.id,
                shortCode = st.shortCode,
                name = st.name,
                program = st.program,
                frequency = st.frequency,
                country = st.country.name,
                genre = st.genre.name,
                level = st.level.name,
                streamUrl = st.streamUrl,
                bitrate = st.bitrate,
                userCountry = userCountry,
                fetchedAt = now,
            )
        }

        _foundCount.value = final.size
        _progress.value = 1f
        _stage.value = DiscoveryStage.DONE
        final.size
    }

    /**
     * Берём 24 ES + 8 MX + 8 AR из пула.
     * Внутри каждой страны применяем dedup по бренду — максимум 2
     * станции на «семью» (Cadena SER, RNE, COPE...). Это даёт юзеру
     * вариативность брендов вместо 5 региональных вариантов одной сети.
     */
    private fun balanceByCountry(pool: List<DiscoveredStation>): List<DiscoveredStation> {
        val es = deduplicateByBrand(pool.filter { it.country == Country.SPAIN }).take(24)
        val mx = deduplicateByBrand(pool.filter { it.country == Country.MEXICO }).take(8)
        val ar = deduplicateByBrand(pool.filter { it.country == Country.ARGENTINA }).take(8)
        return es + mx + ar
    }

    private fun detectUserCountry(): String {
        runCatching {
            val req = Request.Builder().url("https://api.country.is").build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@runCatching null
                    val parsed = json.decodeFromString<CountryIsResponse>(body)
                    return parsed.country
                } else {
                    reportError("country.is HTTP ${resp.code}")
                }
            }
        }.onFailure { reportError("country.is network", it) }
        // Fallback на Locale
        return Locale.getDefault().country.ifEmpty { "??" }
    }

    /**
     * Запросить большой пул кандидатов из radio-browser.info по тегам.
     *
     * Genre выводим из САМОГО ТЕГА (tagToGenre map), а не из группы.
     * Раньше: `"news"` группировался с `"talk"` → genre=TALK для обоих,
     * фильтр Новости в UI ничего не показывал. Теперь news → Genre.NEWS,
     * sports → Genre.SPORTS, и т.д.
     *
     * Каждая страна запрашивается по своему набору тегов (региональная
     * специфика — sports/futbol в LATAM популярнее чем в Spain итд).
     */
    private fun fetchPool(): List<DiscoveredStation> {
        val countryTags = listOf(
            // Spain — европейский набор: news, talk, классика, pop/rock, спорт
            Country.SPAIN to listOf(
                "news", "talk", "sports",
                "pop", "rock", "dance", "indie",
                "classical", "culture", "jazz",
            ),
            // Mexico — больше LATAM-специфики
            Country.MEXICO to listOf(
                "news", "talk", "sports",
                "pop", "banda", "romantic", "regional", "salsa",
                "culture",
            ),
            // Argentina — tango, rock nacional, futbol
            Country.ARGENTINA to listOf(
                "news", "talk", "sports",
                "pop", "rock", "tango",
                "culture", "jazz",
            ),
        )

        val pool = mutableListOf<DiscoveredStation>()
        val seenUrls = mutableSetOf<String>()
        val totalQueries = countryTags.sumOf { it.second.size }
        var doneQueries = 0

        for ((country, tags) in countryTags) {
            for (tag in tags) {
                val results = queryApi(country.isoCode(), tag)
                doneQueries++
                // 0.10 → 0.30 на fetchPool стадии (живой progress per query)
                _progress.value = 0.10f + 0.20f * (doneQueries.toFloat() / totalQueries)
                if (results == null) continue
                val genre = tagToGenre(tag)
                val level = tagToCefr(tag)
                for (r in results) {
                    val url = r.url_resolved ?: r.url ?: continue
                    if (url in seenUrls) continue
                    if (!isSafeStreamUrl(url)) continue
                    if (isGeoBlockedDomain(url)) continue
                    if ((r.bitrate ?: 0) < 48) continue
                    seenUrls.add(url)
                    pool.add(
                        DiscoveredStation(
                            id = "auto_${url.hashCode().toUInt().toString(16)}",
                            shortCode = shortCodeFromName(r.name ?: "?"),
                            name = (r.name ?: "?").trim().take(40),
                            program = tag.replaceFirstChar { it.uppercase() },
                            frequency = estimateFreq(pool.size, country),
                            country = country,
                            genre = genre,
                            level = level,
                            streamUrl = url,
                            bitrate = r.bitrate ?: 0,
                        )
                    )
                }
                if (pool.size >= 120) return pool
            }
        }
        return pool
    }

    /** Tag из radio-browser API → наш Genre enum. Источник истины. */
    private fun tagToGenre(tag: String): Genre = when (tag.lowercase()) {
        "news" -> Genre.NEWS
        "talk", "discussion" -> Genre.TALK
        "sports", "sport", "futbol" -> Genre.SPORTS
        "culture", "classical", "jazz", "opera" -> Genre.CULTURE
        else -> Genre.MUSIC  // pop/rock/dance/indie/banda/etc — всё музыка
    }

    /** Tag → CEFR-сложность контента (talk сложнее музыки для понимания). */
    private fun tagToCefr(tag: String): CefrLevel = when (tag.lowercase()) {
        "news" -> CefrLevel.B2          // дикторы говорят быстро, лексика
        "talk", "discussion" -> CefrLevel.B1
        "sports", "sport", "futbol" -> CefrLevel.B1
        "culture", "classical" -> CefrLevel.B1
        "jazz" -> CefrLevel.A2
        else -> CefrLevel.A2  // музыка — самый простой контент
    }

    private fun queryApi(countryCode: String, tag: String): List<ApiStation>? = runCatching {
        val url = "https://de1.api.radio-browser.info/json/stations/search" +
                "?countrycode=$countryCode&tag=$tag&hidebroken=true" +
                "&order=clickcount&reverse=true&limit=15"
        val req = Request.Builder().url(url)
            .header("User-Agent", "ESPEAK/1.9.1").build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                reportError("queryApi $countryCode/$tag HTTP ${resp.code}")
                return@runCatching null
            }
            val body = resp.body?.string() ?: return@runCatching null
            val list = json.decodeFromString<List<ApiStation>>(body)
            trace("queryApi $countryCode/$tag → ${list.size} результатов")
            list
        }
    }.onFailure { reportError("queryApi $countryCode/$tag exception", it) }.getOrNull()

    private fun probe(url: String): Boolean = runCatching {
        val req = Request.Builder().url(url)
            .header("User-Agent", "ESPEAK/1.10.2")
            .header("Range", "bytes=0-2048")
            .build()
        probeClient.newCall(req).execute().use { resp: Response ->
            if (!resp.isSuccessful && resp.code != 206) return@runCatching false
            val bytes = resp.body?.bytes()?.size ?: 0
            bytes > 100
        }
    }.getOrElse { false }

    private fun isGeoBlockedDomain(url: String): Boolean {
        val blocked = listOf(
            "flumotion.com", "rndfnk.com", "cires21.com",
            "ondacero.es", "atres-live", "marca.com",
        )
        return blocked.any { it in url }
    }

    private fun shortCodeFromName(name: String): String {
        val cleaned = name.replace(Regex("[^A-Za-z0-9 ]"), "").trim()
        val words = cleaned.split(" ").filter { it.isNotEmpty() }
        return when {
            words.isEmpty() -> "???"
            words.size == 1 -> words[0].take(4).uppercase()
            else -> words.take(3).joinToString("") { it.take(1) }.uppercase()
        }.take(4)
    }

    private fun estimateFreq(index: Int, country: Country): Float {
        // Раскладываем по 87.5-108.0 равномерно
        val base = 87.5f
        val step = 0.6f
        return base + (index * step) % 20.5f
    }
}

private fun Country.isoCode(): String = when (this) {
    Country.SPAIN -> "ES"
    Country.MEXICO -> "MX"
    Country.ARGENTINA -> "AR"
}

@Serializable
private data class ApiStation(
    val name: String? = null,
    val url: String? = null,
    val url_resolved: String? = null,
    val bitrate: Int? = null,
    val codec: String? = null,
    val clickcount: Int? = null,
)

@Serializable
private data class CountryIsResponse(
    val country: String,
)

data class DiscoveredStation(
    val id: String,
    val shortCode: String,
    val name: String,
    val program: String,
    val frequency: Float,
    val country: Country,
    val genre: Genre,
    val level: CefrLevel,
    val streamUrl: String,
    val bitrate: Int,
)
