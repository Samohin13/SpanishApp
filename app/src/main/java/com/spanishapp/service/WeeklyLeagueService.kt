package com.spanishapp.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WeeklyLeagueDao
import com.spanishapp.data.db.entity.WeeklyLeagueStateEntity
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

/**
 * Duolingo-style недельные лиги.
 *
 * Каждый понедельник 00:00 (локальная неделя ISO) opted-in пользователи
 * формируют когорту из ~30 человек, неделю набирают weeklyXP. В конце
 * недели: топ-7 → +1 tier, низ-7 → -1 tier, середина — без изменений.
 *
 * Раннер на клиенте: [ensureCurrentWeek] вызывается на старте app и при
 * каждом ответе. Если неделя сменилась — финализирует прошлую и
 * стартует новую.
 *
 * Tier (1..8) использует те же 8 городов что и skillRating-лиги, но
 * это ПАРАЛЛЕЛЬНАЯ ветка — изменения tier здесь НЕ влияют на skillRating.
 */
@Singleton
class WeeklyLeagueService @Inject constructor(
    private val dao: WeeklyLeagueDao,
    private val userProgressDao: UserProgressDao,
    private val firestore: FirebaseFirestore
) {

    companion object {
        const val COHORT_SIZE = 30
        const val PROMOTE_COUNT = 7
        const val DEMOTE_COUNT = 7
        const val MAX_TIER = 8
        const val MIN_TIER = 1
        const val BUCKETS_PER_TIER = 200
    }

    /**
     * Инкрементит weeklyXP пользователя и подталкивает sync в Firestore.
     * Дёргается из RatingUpdater после каждого ответа.
     */
    suspend fun onXpEarned(xp: Int) {
        if (xp <= 0) return
        ensureCurrentWeek()
        val state = dao.get() ?: return
        if (!state.optedIn) return
        dao.bumpWeekXp(xp)
        runCatching { syncWeekXpToFirestore() }
    }

    /**
     * Идемпотентная проверка текущей недели. Если ISO-понедельник сменился
     * с момента последнего апдейта — финализирует прошлую неделю
     * (применяет promo/demo на основе снапшота когорты в Firestore),
     * сбрасывает weeklyXP и подбирает новую когорту.
     */
    suspend fun ensureCurrentWeek() {
        val state = dao.get() ?: WeeklyLeagueStateEntity().also { dao.upsert(it) }
        if (!state.optedIn) return
        val nowMonday = isoMondayOf(LocalDate.now())
        if (state.currentWeekStart == nowMonday) return

        var newTier = state.currentTier
        if (state.currentWeekStart.isNotEmpty() && state.cohortId.isNotEmpty()) {
            newTier = runCatching { finalizePreviousWeek(state) }.getOrDefault(state.currentTier)
        }
        val newCohort = pickCohort(newTier, nowMonday)
        dao.upsert(state.copy(
            currentTier = newTier,
            currentWeekStart = nowMonday,
            currentWeekXp = 0,
            cohortId = newCohort,
            lastFinalizedWeek = state.currentWeekStart
        ))
        // Зарегистрировать себя в новой когорте.
        runCatching { syncWeekXpToFirestore() }
    }

    /** Снапшот текущей когорты — 30 участников по убыванию weeklyXP. */
    suspend fun getCohortLeaderboard(): List<WeeklyMember> {
        val state = dao.get() ?: return emptyList()
        if (state.cohortId.isEmpty()) return emptyList()
        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        return runCatching {
            val snap = firestore.collection("weekly_cohorts")
                .document(state.cohortId)
                .collection("members")
                .orderBy("weekXp", Query.Direction.DESCENDING)
                .limit(COHORT_SIZE.toLong())
                .get().await()
            snap.documents.map { doc ->
                WeeklyMember(
                    uid = doc.id,
                    nickname = doc.getString("nickname").orEmpty().ifBlank { "Estudiante" },
                    weekXp = (doc.getLong("weekXp") ?: 0L).toInt(),
                    isMe = doc.id == myUid
                )
            }
        }.getOrDefault(emptyList())
    }

    suspend fun setOptedIn(enabled: Boolean) {
        // Гарантируем что строка существует (опт-ин могли тыкнуть до первого touch).
        if (dao.get() == null) dao.upsert(WeeklyLeagueStateEntity())
        dao.setOptedIn(enabled)
        if (enabled) ensureCurrentWeek()
    }

    suspend fun getState(): WeeklyLeagueStateEntity? = dao.get()

    // ── private ──────────────────────────────────────────────

    private fun isoMondayOf(date: LocalDate): String =
        date.with(DayOfWeek.MONDAY).toString()

    /**
     * Детерминированный matchmaking без транзакций. Ключ когорты:
     *   "{weekStart}_t{tier}_b{bucket}" где bucket = uidHash mod 200.
     * Каждый tier разбит на 200 бакетов — даёт стабильный размер когорты
     * и не требует серверного координатора. v1 решение, можно усложнить позже.
     */
    private fun pickCohort(tier: Int, weekStart: String): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return ""
        val bucket = uid.hashCode().absoluteValue % BUCKETS_PER_TIER
        return "${weekStart}_t${tier}_b${bucket}"
    }

    private suspend fun syncWeekXpToFirestore() {
        val state = dao.get() ?: return
        if (state.cohortId.isEmpty()) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val nickname = userProgressDao.getProgressOnce()?.displayName.orEmpty()
            .ifBlank { "Estudiante" }
        firestore.collection("weekly_cohorts").document(state.cohortId)
            .collection("members").document(uid)
            .set(
                mapOf(
                    "uid"       to uid,
                    "weekXp"    to state.currentWeekXp,
                    "tier"      to state.currentTier,
                    "nickname"  to nickname,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
    }

    /** Возвращает новый tier после финализации прошлой когорты. */
    private suspend fun finalizePreviousWeek(state: WeeklyLeagueStateEntity): Int {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return state.currentTier
        val snap = firestore.collection("weekly_cohorts")
            .document(state.cohortId)
            .collection("members")
            .orderBy("weekXp", Query.Direction.DESCENDING)
            .limit(COHORT_SIZE.toLong())
            .get().await()

        val total = snap.size()
        if (total == 0) return state.currentTier
        val rank = snap.documents.indexOfFirst { it.id == uid }
        if (rank < 0) return state.currentTier

        return when {
            rank < PROMOTE_COUNT          -> (state.currentTier + 1).coerceAtMost(MAX_TIER)
            rank >= total - DEMOTE_COUNT  -> (state.currentTier - 1).coerceAtLeast(MIN_TIER)
            else                          -> state.currentTier
        }
    }
}

data class WeeklyMember(
    val uid: String,
    val nickname: String,
    val weekXp: Int,
    val isMe: Boolean
)
