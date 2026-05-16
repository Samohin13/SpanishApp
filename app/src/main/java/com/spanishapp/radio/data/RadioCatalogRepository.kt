package com.spanishapp.radio.data

import android.content.Context
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
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /** Прогресс fetch'а для UI: 0..1. */
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    /** Cколько живых найдено в последнем сканировании. */
    private val _foundCount = MutableStateFlow(0)
    val foundCount: StateFlow<Int> = _foundCount.asStateFlow()

    /** Свежесть кэша: < 7 дней. */
    suspend fun isCacheFresh(): Boolean {
        val last = dao.lastFetchedAt() ?: return false
        val age = System.currentTimeMillis() - last
        return age < 7 * 24 * 60 * 60 * 1000L  // 7 дней
    }

    suspend fun hasCache(): Boolean = dao.count() > 0

    /**
     * Главный метод: запросить + probe + закэшировать.
     * Возвращает количество найденных рабочих станций.
     */
    suspend fun discoverAndCache(): Int = withContext(Dispatchers.IO) {
        _progress.value = 0f
        _foundCount.value = 0

        val userCountry = detectUserCountry()

        // Параллельно запрашиваем большие пулы по жанрам/странам вещания
        val rawPool = fetchPool()

        if (rawPool.isEmpty()) {
            return@withContext 0
        }

        // Probe — параллельно по 8 URL за раз
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
            _progress.value = processed.toFloat() / total
            if (working.size >= 40) return@forEach
        }

        // Балансируем: 24 ES + 8 MX + 8 AR из найденных живых
        val final = balanceByCountry(working)

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
        // Пробуем ip-api сначала (надёжнее)
        runCatching {
            val req = Request.Builder().url("https://api.country.is").build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@runCatching null
                    val parsed = json.decodeFromString<CountryIsResponse>(body)
                    return parsed.country
                }
            }
        }
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

        for ((triple, tags) in targets) {
            val (country, genre, level) = triple
            for (tag in tags) {
                val results = queryApi(country.isoCode(), tag) ?: continue
                for (r in results) {
                    val url = r.url_resolved ?: r.url ?: continue
                    if (url in seenUrls) continue
                    if (isGeoBlockedDomain(url)) continue
                    if ((r.bitrate ?: 0) < 48) continue  // отсекаем мусор
                    seenUrls.add(url)
                    pool.add(
                        DiscoveredStation(
                            id = "auto_${pool.size}",
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
            .header("User-Agent", "ESPEAK/1.7.0").build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@runCatching null
            val body = resp.body?.string() ?: return@runCatching null
            json.decodeFromString<List<ApiStation>>(body)
        }
    }.getOrNull()

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
