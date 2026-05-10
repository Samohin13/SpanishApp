package com.spanishapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.spanishapp.data.db.dao.FlashcardSetProgressDao
import com.spanishapp.data.db.dao.GameLevelProgressDao
import com.spanishapp.data.db.dao.LessonProgressDao
import com.spanishapp.data.db.dao.LibroProgressDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.FlashcardSetProgressEntity
import com.spanishapp.data.db.entity.GameLevelProgressEntity
import com.spanishapp.data.db.entity.LessonProgressEntity
import com.spanishapp.data.db.entity.LibroProgressEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Минимально жизнеспособная синхронизация прогресса пользователя через Firestore.
 *
 * Структура документа `users/{uid}/state/main`:
 * ```
 * {
 *   userProgress: { totalXp, currentStreak, longestStreak, wordsLearned, ... },
 *   flashcardSets: [{ setId, stars, bestPercent, completedAt }, ...],
 *   libros:        [{ libroId, isCompleted, bestScore, completedAt }, ...],
 *   gameLevels:    [{ gameId, levelNum, stars, bestScore, completedAt }, ...],
 *   lessons:       ["u1_l0", "u1_l1", ...],
 *   updatedAt: <ms>
 * }
 * ```
 *
 * Не realtime, без conflict resolution: upload-on-change + download-on-login.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val flashcardSetProgressDao: FlashcardSetProgressDao,
    private val libroProgressDao: LibroProgressDao,
    private val gameLevelProgressDao: GameLevelProgressDao,
    private val lessonProgressDao: LessonProgressDao,
    private val wordDao: WordDao
) {
    private val auth: FirebaseAuth get() = Firebase.auth
    private val db: FirebaseFirestore get() = Firebase.firestore

    @Volatile private var lastUploadMs: Long = 0L
    private val UPLOAD_DEBOUNCE_MS = 60_000L

    private fun uid(): String? = auth.currentUser?.uid

    private fun userDoc() = uid()?.let { db.collection("users").document(it).collection("state").document("main") }

    /** Можно вызывать fire-and-forget из любого ViewModelScope. Дебаунс 1 раз/мин. */
    suspend fun uploadAll(force: Boolean = false): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        if (!force && now - lastUploadMs < UPLOAD_DEBOUNCE_MS) return@runCatching
        val doc = userDoc() ?: error("not signed in")

        val progress = userProgressDao.getProgressOnce() ?: error("no user_progress")
        val sets = flashcardSetProgressDao.getAll()
        val libros = libroProgressDao.getAll().firstOrNull() ?: emptyList()
        val gameIds = listOf("articles", "speed", "anagram", "math", "crossword", "sopa", "palabra", "verb", "libros")
        val gameLevels = gameIds.flatMap { runCatching { gameLevelProgressDao.getForGame(it) }.getOrDefault(emptyList()) }
        val lessons = lessonProgressDao.getAllCompletedKeys().firstOrNull() ?: emptyList()

        val payload = mapOf(
            "userProgress" to mapOf(
                "totalXp" to progress.totalXp,
                "currentStreak" to progress.currentStreak,
                "longestStreak" to progress.longestStreak,
                "wordsLearned" to progress.wordsLearned,
                "lessonsCompleted" to progress.lessonsCompleted,
                "dialoguesCompleted" to progress.dialoguesCompleted,
                "totalStudyMinutes" to progress.totalStudyMinutes,
                "currentLevel" to progress.currentLevel,
                "skillRating" to progress.skillRating,
                "peakSkillRating" to progress.peakSkillRating,
                "currentLeague" to progress.currentLeague,
                "displayName" to progress.displayName,
                "dailyGoalMinutes" to progress.dailyGoalMinutes,
                "lastStreakUpdateDate" to progress.lastStreakUpdateDate,
                "streakFreezesAvailable" to progress.streakFreezesAvailable
            ),
            "flashcardSets" to sets.map {
                mapOf("setId" to it.setId, "stars" to it.stars, "bestPercent" to it.bestPercent, "completedAt" to it.completedAt)
            },
            "libros" to libros.map {
                mapOf("libroId" to it.libroId, "isCompleted" to it.isCompleted, "bestScore" to it.bestScore, "completedAt" to it.completedAt)
            },
            "gameLevels" to gameLevels.map {
                mapOf("gameId" to it.gameId, "levelNum" to it.levelNum, "stars" to it.stars, "bestScore" to it.bestScore, "completedAt" to it.completedAt)
            },
            "lessons" to lessons,
            "updatedAt" to now
        )

        doc.set(payload, SetOptions.merge()).await()
        lastUploadMs = now
    }

    /** Слить документ из облака в локальную БД (мердж по MAX). */
    suspend fun downloadAll(): Result<Int> = runCatching {
        val doc = userDoc() ?: error("not signed in")
        val snapshot = doc.get().await()
        if (!snapshot.exists()) return@runCatching 0

        var applied = 0

        // userProgress merge: бери MAX по числовым полям
        @Suppress("UNCHECKED_CAST")
        val up = snapshot.get("userProgress") as? Map<String, Any?>
        if (up != null) {
            val cur = userProgressDao.getProgressOnce()
            if (cur != null) {
                val merged = cur.copy(
                    totalXp = max(cur.totalXp, (up["totalXp"] as? Number)?.toInt() ?: 0),
                    currentStreak = max(cur.currentStreak, (up["currentStreak"] as? Number)?.toInt() ?: 0),
                    longestStreak = max(cur.longestStreak, (up["longestStreak"] as? Number)?.toInt() ?: 0),
                    wordsLearned = max(cur.wordsLearned, (up["wordsLearned"] as? Number)?.toInt() ?: 0),
                    lessonsCompleted = max(cur.lessonsCompleted, (up["lessonsCompleted"] as? Number)?.toInt() ?: 0),
                    skillRating = max(cur.skillRating, (up["skillRating"] as? Number)?.toInt() ?: 1000),
                    peakSkillRating = max(cur.peakSkillRating, (up["peakSkillRating"] as? Number)?.toInt() ?: 1000),
                    currentLeague = max(cur.currentLeague, (up["currentLeague"] as? Number)?.toInt() ?: 1)
                )
                userProgressDao.update(merged)
                applied++
            }
        }

        // flashcardSets merge
        @Suppress("UNCHECKED_CAST")
        val sets = snapshot.get("flashcardSets") as? List<Map<String, Any?>> ?: emptyList()
        sets.forEach { row ->
            val id = row["setId"] as? String ?: return@forEach
            val cloudStars = (row["stars"] as? Number)?.toInt() ?: 0
            val cloudPct = (row["bestPercent"] as? Number)?.toInt() ?: 0
            val cloudTs = (row["completedAt"] as? Number)?.toLong() ?: 0L
            val existing = flashcardSetProgressDao.getOne(id)
            val merged = FlashcardSetProgressEntity(
                setId = id,
                stars = max(existing?.stars ?: 0, cloudStars),
                bestPercent = max(existing?.bestPercent ?: 0, cloudPct),
                completedAt = max(existing?.completedAt ?: 0L, cloudTs)
            )
            flashcardSetProgressDao.upsert(merged)
            applied++
        }

        // libros merge
        @Suppress("UNCHECKED_CAST")
        val libros = snapshot.get("libros") as? List<Map<String, Any?>> ?: emptyList()
        libros.forEach { row ->
            val id = (row["libroId"] as? Number)?.toInt() ?: return@forEach
            val existing = libroProgressDao.getById(id)
            val merged = LibroProgressEntity(
                libroId = id,
                isCompleted = (existing?.isCompleted ?: false) || (row["isCompleted"] as? Boolean ?: false),
                bestScore = max(existing?.bestScore ?: 0, (row["bestScore"] as? Number)?.toInt() ?: 0),
                completedAt = max(existing?.completedAt ?: 0L, (row["completedAt"] as? Number)?.toLong() ?: 0L)
            )
            libroProgressDao.upsert(merged)
            applied++
        }

        // gameLevels merge
        @Suppress("UNCHECKED_CAST")
        val games = snapshot.get("gameLevels") as? List<Map<String, Any?>> ?: emptyList()
        games.forEach { row ->
            val gid = row["gameId"] as? String ?: return@forEach
            val lvl = (row["levelNum"] as? Number)?.toInt() ?: return@forEach
            val existing = gameLevelProgressDao.getOne(gid, lvl)
            val merged = GameLevelProgressEntity(
                gameId = gid,
                levelNum = lvl,
                stars = max(existing?.stars ?: 0, (row["stars"] as? Number)?.toInt() ?: 0),
                bestScore = max(existing?.bestScore ?: 0, (row["bestScore"] as? Number)?.toInt() ?: 0),
                completedAt = max(existing?.completedAt ?: 0L, (row["completedAt"] as? Number)?.toLong() ?: 0L)
            )
            gameLevelProgressDao.upsert(merged)
            applied++
        }

        // lessons merge
        @Suppress("UNCHECKED_CAST")
        val lessons = snapshot.get("lessons") as? List<String> ?: emptyList()
        lessons.forEach { key ->
            val parts = key.removePrefix("u").split("_l")
            if (parts.size == 2) {
                val unitId = parts[0].toIntOrNull() ?: return@forEach
                val lessonIndex = parts[1].toIntOrNull() ?: return@forEach
                lessonProgressDao.markComplete(LessonProgressEntity(key, unitId, lessonIndex))
                applied++
            }
        }

        applied
    }

    /** Простая эвристика: считается ли локальная БД "пустой" (есть смысл скачивать). */
    suspend fun isLocalEmpty(): Boolean = runCatching {
        val p = userProgressDao.getProgressOnce()
        val xp = p?.totalXp ?: 0
        val sets = flashcardSetProgressDao.getAll().sumOf { it.stars }
        xp < 50 && sets == 0
    }.getOrDefault(true)
}
