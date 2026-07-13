package com.spanishapp.data.repository

import android.content.Context
import android.telephony.TelephonyManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.prefs.CountryPreferences
import com.spanishapp.domain.algorithm.LeagueResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class LeaderboardEntry(
    val uid: String,
    val nickname: String,
    val country: String,    // ISO-2: KZ, RU, ES, FR, ...
    val skillRating: Int,
    val peakRating: Int,
    val league: Int,
    val updatedAt: Long
)

data class LeaderboardData(
    val country: String,
    val countryRows: List<LeaderboardEntry>,
    val worldRows: List<LeaderboardEntry>,
    val myCountryRank: Int?,    // null если пользователь не в опт-ине
    val myWorldRank: Int?,
    val myUid: String?,
    val countryTotal: Int,
    val worldTotal: Int,
    val countriesAggregate: List<CountryAggregate> = emptyList(),
    val worldCountriesCount: Int = 0,
    val myCountryWorldRank: Int? = null,   // позиция моей страны в countries leaderboard
)

/**
 * Агрегат рейтинга по стране — для «соревнования стран» на табе Мир.
 * Считается клиентски из worldRows (топ-100 юзеров).
 */
data class CountryAggregate(
    val iso: String,
    val playerCount: Int,
    val totalXp: Long,     // сумма skillRating
    val avgXp: Int,        // средний skillRating
)

@Serializable
private data class CountryIsResponse(val country: String? = null)

@Singleton
class LeaderboardRepository @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val countryPrefs: CountryPreferences,
    @ApplicationContext private val context: Context
) {

    private val auth: FirebaseAuth get() = Firebase.auth
    private val db: FirebaseFirestore get() = Firebase.firestore

    private val collection get() = db.collection("leaderboard")

    /** Отдельный клиент для IP-API — 4 сек на всё (быстрый fallback, не блокируем UX). */
    private val ipClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .callTimeout(4, TimeUnit.SECONDS)
            .build()
    }
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Sync-версия определения страны для legacy-вызовов (не дёргает сеть).
     * Использует только TelephonyManager + Locale + cached pref + override.
     * Для первого запроса предпочтительнее `deviceCountryCodeAsync()`.
     */
    fun deviceCountryCode(): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val net = tm?.networkCountryIso?.uppercase().orEmpty()
        if (net.length == 2) return net
        val sim = tm?.simCountryIso?.uppercase().orEmpty()
        if (sim.length == 2) return sim
        return Locale.getDefault().country.uppercase().ifBlank { "XX" }
    }

    /**
     * Async-версия с полной цепочкой fallback'ов:
     *
     *  1) **Override** — если юзер выбрал страну вручную через picker (DataStore)
     *  2) **Network ISO** — TelephonyManager.networkCountryIso (страна оператора)
     *  3) **SIM ISO** — TelephonyManager.simCountryIso (страна SIM)
     *  4) **IP-API** — https://api.country.is (HTTP fallback, 4 сек)
     *  5) **Locale** — Locale.getDefault().country (язык системы)
     *  6) **Cached** — последний успешный detect из DataStore
     *  7) **"XX"** — unknown
     *
     * При успехе IP-API результат кэшируется в DataStore для быстрых
     * последующих запросов в offline.
     */
    suspend fun deviceCountryCodeAsync(): String {
        // 1) User override — высший приоритет
        val override = countryPrefs.overrideIso.first()
        if (override.length == 2) return override

        // 2-3) Telephony
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val net = tm?.networkCountryIso?.uppercase().orEmpty()
        if (net.length == 2) {
            countryPrefs.setCached(net)
            return net
        }
        val sim = tm?.simCountryIso?.uppercase().orEmpty()
        if (sim.length == 2) {
            countryPrefs.setCached(sim)
            return sim
        }

        // 4) IP-API — спасает WiFi-only устройства без SIM (популярный кейс
        //    в Юго-Восточной Азии где юзер сидит на WiFi). Завернули в
        //    withTimeoutOrNull чтобы коллизия с UI thread не съела ANR.
        val ip = withTimeoutOrNull(4_500L) {
            runCatching {
                val req = Request.Builder().url("https://api.country.is").build()
                ipClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@runCatching null
                    val body = resp.body?.string() ?: return@runCatching null
                    json.decodeFromString<CountryIsResponse>(body).country?.uppercase()
                }
            }.getOrNull()
        }
        if (ip != null && ip.length == 2) {
            countryPrefs.setCached(ip)
            return ip
        }

        // 5) Locale (язык системы — последняя автоматическая надежда)
        val loc = Locale.getDefault().country.uppercase()
        if (loc.length == 2) {
            countryPrefs.setCached(loc)
            return loc
        }

        // 6) Cached — последний раз когда что-то работало
        val cached = countryPrefs.cachedIso.first()
        if (cached.length == 2) return cached

        // 7) Совсем ничего не нашли
        return "XX"
    }

    /** Возвращает uid анонимного пользователя — авторизует если нужно. */
    private suspend fun ensureAuth(): String {
        val cur = auth.currentUser
        if (cur != null) return cur.uid
        return auth.signInAnonymously().await().user?.uid
            ?: throw IllegalStateException("Anonymous auth failed")
    }

    /**
     * Записывает / обновляет свою запись в Firestore.
     * Не чаще раза в 30 секунд (rate-limit на стороне клиента).
     */
    private var lastSyncTs = 0L

    suspend fun syncSelf(force: Boolean = false): Boolean {
        val now = System.currentTimeMillis()
        if (!force && now - lastSyncTs < 30_000L) return false

        val progress = userProgressDao.getProgressOnce() ?: return false
        if (!progress.leaderboardOptIn) return false

        // v1.26.1 (Model B): в рейтинг пишут только зарегистрированные. Гость
        // (анонимный аккаунт или вовсе без currentUser) не создаёт запись —
        // иначе ghost-дубли лидерборда от анонимных uid. Участие требует
        // регистрации (см. гейт в LeaderboardViewModel/WeeklyLeagueViewModel).
        val cur = auth.currentUser
        if (cur == null || cur.isAnonymous) return false

        val uid = ensureAuth()
        // ⚠ Используем АСИНХРОННУЮ версию detect — это даёт IP-API fallback
        // для WiFi-only устройств. Если упадёт сеть — вернётся "XX", но
        // в Firestore запишется хотя бы хоть что-то осмысленное.
        val data = mapOf(
            "nickname" to progress.displayName.ifBlank { "Estudiante" },
            "country" to deviceCountryCodeAsync(),
            "skillRating" to progress.skillRating,
            "peakRating" to progress.peakSkillRating,
            "league" to progress.currentLeague,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        collection.document(uid).set(data, SetOptions.merge()).await()
        lastSyncTs = now
        return true
    }

    /** Удалить свою запись (при выходе из лидерборда). */
    suspend fun deleteSelf() {
        val cur = auth.currentUser ?: return
        try {
            collection.document(cur.uid).delete().await()
        } catch (_: Exception) {}
    }

    /** Загрузить топы и свои ранги. */
    suspend fun fetch(limit: Int = 100): LeaderboardData {
        val country = deviceCountryCodeAsync()
        val myUid = auth.currentUser?.uid

        // Firestore возвращает данные отсортированными по skillRating DESC.
        // При одинаковом skillRating порядок недетерминирован — поэтому
        // делаем вторичную сортировку в Kotlin (см. comparator ниже).
        // ⚠ Композитный orderBy(skillRating, updatedAt) в Firestore требует
        // создания composite index вручную в Console — мы избегаем этого
        // дополнительного шага для юзера и сортируем клиентски.
        val world = collection
            .orderBy("skillRating", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await()

        val countryQuery = collection
            .whereEqualTo("country", country)
            .orderBy("skillRating", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await()

        // Тай-брейкер: при равном skillRating — тот, кто РАНЬШЕ зарегистрировался,
        // выше. Это даёт детерминированный визуальный порядок, согласованный
        // с подсчётом myRank ниже.
        val tieBreakComparator = compareByDescending<LeaderboardEntry> { it.skillRating }
            .thenBy { it.updatedAt }

        val worldRows = world.documents.mapNotNull { it.toEntry() }.sortedWith(tieBreakComparator)
        val countryRows = countryQuery.documents.mapNotNull { it.toEntry() }.sortedWith(tieBreakComparator)

        // ── My ranks ──
        // Считаем согласованно с визуальным порядком: rank = индекс в
        // отсортированном массиве + 1. Так гарантируем что «Ты #1» и медаль
        // 🥇 у одного и того же юзера.
        val myProgress = userProgressDao.getProgressOnce()
        val optedIn = myProgress?.leaderboardOptIn == true

        val myCountryRank = if (myUid != null && optedIn) {
            val idx = countryRows.indexOfFirst { it.uid == myUid }
            if (idx >= 0) idx + 1
            else fallbackRankCountry(country, myProgress?.skillRating ?: 0)
        } else null

        val myWorldRank = if (myUid != null && optedIn) {
            val idx = worldRows.indexOfFirst { it.uid == myUid }
            if (idx >= 0) idx + 1
            else fallbackRankWorld(myProgress?.skillRating ?: 0)
        } else null

        // Общее число (приблизительно)
        val countryTotal = try {
            collection.whereEqualTo("country", country).count().get(AggregateSource.SERVER).await().count.toInt()
        } catch (_: Exception) { countryRows.size }

        val worldTotal = try {
            collection.count().get(AggregateSource.SERVER).await().count.toInt()
        } catch (_: Exception) { worldRows.size }

        // ── Countries aggregate (для таба «Мир — соревнование стран») ──
        // Клиентский group-by по worldRows (топ-100). При росте сообщества
        // переедет в Cloud Function с предрасчётом, сейчас достаточно.
        val countriesAggregate = worldRows
            .groupBy { it.country.ifBlank { "XX" } }
            .map { (iso, rows) ->
                val totalXp = rows.sumOf { it.skillRating.toLong() }
                val avg = if (rows.isEmpty()) 0 else (totalXp / rows.size).toInt()
                CountryAggregate(
                    iso = iso,
                    playerCount = rows.size,
                    totalXp = totalXp,
                    avgXp = avg,
                )
            }
            .sortedByDescending { it.totalXp }
        val myCountryWorldRank = countriesAggregate.indexOfFirst { it.iso == country }.takeIf { it >= 0 }?.plus(1)
        val worldCountriesCount = countriesAggregate.size

        return LeaderboardData(
            country = country,
            countryRows = countryRows,
            worldRows = worldRows,
            myCountryRank = myCountryRank,
            myWorldRank = myWorldRank,
            myUid = myUid,
            countryTotal = countryTotal,
            worldTotal = worldTotal,
            countriesAggregate = countriesAggregate,
            worldCountriesCount = worldCountriesCount,
            myCountryWorldRank = myCountryWorldRank,
        )
    }

    /** Fallback подсчёт когда юзер не в первых N (>100). Считаем сколько
     *  юзеров с большим рейтингом + 1. Тай-брейкера тут нет — но это
     *  приближение для случая когда юзер далеко за топ-100. */
    private suspend fun fallbackRankCountry(country: String, myRating: Int): Int? = try {
        val agg = collection
            .whereEqualTo("country", country)
            .whereGreaterThan("skillRating", myRating)
            .count().get(AggregateSource.SERVER).await()
        agg.count.toInt() + 1
    } catch (_: Exception) { null }

    private suspend fun fallbackRankWorld(myRating: Int): Int? = try {
        val agg = collection
            .whereGreaterThan("skillRating", myRating)
            .count().get(AggregateSource.SERVER).await()
        agg.count.toInt() + 1
    } catch (_: Exception) { null }

    private fun com.google.firebase.firestore.DocumentSnapshot.toEntry(): LeaderboardEntry? {
        val rating = getLong("skillRating")?.toInt() ?: return null
        val nickname = getString("nickname") ?: "Estudiante"
        val country = getString("country") ?: "XX"
        val peak = getLong("peakRating")?.toInt() ?: rating
        val league = getLong("league")?.toInt() ?: LeagueResolver.fromRating(rating).tier
        val updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: 0L
        return LeaderboardEntry(
            uid = id,
            nickname = nickname,
            country = country,
            skillRating = rating,
            peakRating = peak,
            league = league,
            updatedAt = updatedAt
        )
    }
}
