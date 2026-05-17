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

/** Допустимые схемы URL потока. */
private val SAFE_SCHEMES = setOf("http", "https", "rtsp")

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
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
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

        val existingUrls = dao.getAll().map { it.streamUrl }.toSet()
        val userCountry = detectUserCountry()
        val rawPool = fetchPool().filter { it.streamUrl !in existingUrls }
        if (rawPool.isEmpty()) {
            _progress.value = 1f
            return@withContext 0
        }

        val total = rawPool.size
        val working = mutableListOf<DiscoveredStation>()
        var processed = 0

        rawPool.chunked(8).forEach { batch ->
            val results = coroutineScope {
                batch.map { st -> async { if (probe(st.streamUrl)) st else null } }.awaitAll()
            }
            results.filterNotNull().forEach { working.add(it) }
            processed += batch.size
            _progress.value = processed.toFloat() / total
            if (working.size >= targetCount) return@forEach
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
        _stage.value = DiscoveryStage.PROBING
        val total = rawPool.size
        val working = mutableListOf<DiscoveredStation>()
        var processed = 0

        rawPool.chunked(8).forEach { batch ->
            val results = coroutineScope {
                batch.map { st ->
                    async { if (probe(st.streamUrl)) st else null }
                }.awaitAll()
            }
            results.filterNotNull().forEach { working.add(it) }
            processed += batch.size
            // Прогресс probe этапа маппим в 0.30 .. 1.0 диапазон
            _progress.value = 0.30f + 0.70f * (processed.toFloat() / total)
            _foundCount.value = working.size
            if (working.size >= 40) return@forEach
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

    /** Берём 24 ES + 8 MX + 8 AR из пула. */
    private fun balanceByCountry(pool: List<DiscoveredStation>): List<DiscoveredStation> {
        val es = pool.filter { it.country == Country.SPAIN }.take(24)
        val mx = pool.filter { it.country == Country.MEXICO }.take(8)
        val ar = pool.filter { it.country == Country.ARGENTINA }.take(8)
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

    /** Запросить большой пул кандидатов из radio-browser.info по жанрам. */
    private fun fetchPool(): List<DiscoveredStation> {
        val targets = listOf(
            Triple(Country.SPAIN, Genre.TALK, CefrLevel.B1) to listOf("talk", "news"),
            Triple(Country.SPAIN, Genre.MUSIC, CefrLevel.A2) to listOf("pop", "rock", "dance"),
            Triple(Country.SPAIN, Genre.CULTURE, CefrLevel.B1) to listOf("classical", "culture"),
            Triple(Country.MEXICO, Genre.MUSIC, CefrLevel.A2) to listOf("pop", "banda", "romantic"),
            Triple(Country.MEXICO, Genre.NEWS, CefrLevel.B2) to listOf("news"),
            Triple(Country.ARGENTINA, Genre.MUSIC, CefrLevel.A2) to listOf("pop", "rock"),
            Triple(Country.ARGENTINA, Genre.TALK, CefrLevel.B2) to listOf("talk"),
        )

        val pool = mutableListOf<DiscoveredStation>()
        val seenUrls = mutableSetOf<String>()
        val totalQueries = targets.sumOf { it.second.size }
        var doneQueries = 0

        for ((triple, tags) in targets) {
            val (country, genre, level) = triple
            for (tag in tags) {
                val results = queryApi(country.isoCode(), tag)
                doneQueries++
                // 0.10 → 0.30 на fetchPool стадии (живой progress per query)
                _progress.value = 0.10f + 0.20f * (doneQueries.toFloat() / totalQueries)
                if (results == null) continue
                for (r in results) {
                    val url = r.url_resolved ?: r.url ?: continue
                    if (url in seenUrls) continue
                    if (!isSafeStreamUrl(url)) continue   // безопасность: только http/https/rtsp
                    if (isGeoBlockedDomain(url)) continue
                    if ((r.bitrate ?: 0) < 48) continue   // отсекаем мусор
                    seenUrls.add(url)
                    pool.add(
                        DiscoveredStation(
                            // Стабильный id из URL — позволяет INSERT OR REPLACE
                            // дедуплицировать при повторных discoverMore() вызовах.
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
                if (pool.size >= 100) return pool
            }
        }
        return pool
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
            .header("User-Agent", "ESPEAK/1.7.0")
            .header("Range", "bytes=0-2048")
            .build()
        client.newCall(req).execute().use { resp: Response ->
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
