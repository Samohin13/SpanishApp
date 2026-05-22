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
import com.spanishapp.domain.algorithm.LeagueResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.Locale
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
    val worldTotal: Int
)

@Singleton
class LeaderboardRepository @Inject constructor(
    private val userProgressDao: UserProgressDao,
    @ApplicationContext private val context: Context
) {

    private val auth: FirebaseAuth get() = Firebase.auth
    private val db: FirebaseFirestore get() = Firebase.firestore

    private val collection get() = db.collection("leaderboard")

    /**
     * Реальный ISO-код страны устройства. Приоритет:
     * 1) Network-страна (где физически зарегистрирован в сети — самое точное:
     *    KZ-житель с RU-симкой в роуминге получит "kz")
     * 2) SIM-страна (где выпущена SIM — fallback при отсутствии сети)
     * 3) Locale (язык системы — последняя надежда, может врать)
     */
    fun deviceCountryCode(): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val net = tm?.networkCountryIso?.uppercase().orEmpty()
        if (net.length == 2) return net
        val sim = tm?.simCountryIso?.uppercase().orEmpty()
        if (sim.length == 2) return sim
        return Locale.getDefault().country.uppercase().ifBlank { "XX" }
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

        // v1.21.1: убрана totalXp==0 фильтрация.
        // Старая логика удаляла запись юзера если он opt-in но ещё не играл,
        // что ломало закрытое тестирование (тестеры не появлялись в таблице).
        // С v1.1.0 skillRating стартует с 0, а не с 1000 → опасность «фейкового
        // топ-игрока с дефолтным рейтингом» снята автоматически. Любой
        // opt-in юзер появляется в таблице снизу (rating=0) и поднимается
        // по мере игры.

        val uid = ensureAuth()
        val data = mapOf(
            "nickname" to progress.displayName.ifBlank { "Estudiante" },
            "country" to deviceCountryCode(),
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
        val country = deviceCountryCode()
        val myUid = auth.currentUser?.uid

        val world = collection
            .orderBy("skillRating", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await()

        val countryQuery = collection
            .whereEqualTo("country", country)
            .orderBy("skillRating", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await()

        val worldRows = world.documents.mapNotNull { it.toEntry() }
        val countryRows = countryQuery.documents.mapNotNull { it.toEntry() }

        // Свои ранги через aggregate count, если пользователь зарегистрирован
        val myProgress = userProgressDao.getProgressOnce()
        val myRating = myProgress?.skillRating ?: 0

        val myCountryRank = if (myUid != null && myProgress?.leaderboardOptIn == true) {
            try {
                val q = collection
                    .whereEqualTo("country", country)
                    .whereGreaterThan("skillRating", myRating)
                val agg = q.count().get(AggregateSource.SERVER).await()
                (agg.count.toInt() + 1)
            } catch (_: Exception) { null }
        } else null

        val myWorldRank = if (myUid != null && myProgress?.leaderboardOptIn == true) {
            try {
                val q = collection.whereGreaterThan("skillRating", myRating)
                val agg = q.count().get(AggregateSource.SERVER).await()
                (agg.count.toInt() + 1)
            } catch (_: Exception) { null }
        } else null

        // Общее число (приблизительно)
        val countryTotal = try {
            collection.whereEqualTo("country", country).count().get(AggregateSource.SERVER).await().count.toInt()
        } catch (_: Exception) { countryRows.size }

        val worldTotal = try {
            collection.count().get(AggregateSource.SERVER).await().count.toInt()
        } catch (_: Exception) { worldRows.size }

        return LeaderboardData(
            country = country,
            countryRows = countryRows,
            worldRows = worldRows,
            myCountryRank = myCountryRank,
            myWorldRank = myWorldRank,
            myUid = myUid,
            countryTotal = countryTotal,
            worldTotal = worldTotal
        )
    }

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
