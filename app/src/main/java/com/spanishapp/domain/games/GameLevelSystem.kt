package com.spanishapp.domain.games

import com.spanishapp.data.db.dao.GameLevelProgressDao
import com.spanishapp.data.db.entity.GameLevelProgressEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Идентификаторы игр (используются как `game_id` в game_level_progress).
 */
object GameId {
    const val ARTICLES = "articles"
    const val SPEED    = "speed"
    const val VERBOS   = "verbos"
    const val SOPA     = "sopa"
    const val PALABRA  = "palabra"
    const val MATH     = "math"
    const val ANAGRAM  = "anagram"
    // crossword использует свою собственную систему 100 уровней
    // libros — не трогаем
}

/**
 * Параметры одного уровня — одинаковая шкала для всех игр.
 * Конкретное применение этих параметров — на усмотрение каждой игры.
 */
data class LevelParams(
    val level: Int,                 // 1..100
    val cefr: List<String>,         // например ["A1"] или ["B2","C1"]
    val rounds: Int,                // сколько раундов в одной попытке
    val timePerRoundSec: Float,     // секунд на раунд (0 = без таймера)
    val hintsAllowed: Int,          // сколько подсказок разрешено
    val mistakePenalty: Boolean,    // штрафовать ли за ошибки
    val mode: LevelMode             // декоративная метка для UI
)

enum class LevelMode { TUTORIAL, EASY, NORMAL, HARD, EXPERT, MASTER }

/**
 * Глобальная сетка сложности 1→100 — единая для всех игр.
 * Реализация каждой игры решает, как именно учесть эти параметры.
 */
object LevelDifficulty {
    fun forLevel(level: Int): LevelParams {
        val l = level.coerceIn(1, 100)
        return when {
            l <= 10  -> LevelParams(l, listOf("A1"),       rounds = 8,  timePerRoundSec = 0f,  hintsAllowed = 3, mistakePenalty = false, mode = LevelMode.TUTORIAL)
            l <= 25  -> LevelParams(l, listOf("A1","A2"),  rounds = 10, timePerRoundSec = 12f, hintsAllowed = 2, mistakePenalty = false, mode = LevelMode.EASY)
            l <= 40  -> LevelParams(l, listOf("A2"),       rounds = 10, timePerRoundSec = 9f,  hintsAllowed = 2, mistakePenalty = false, mode = LevelMode.EASY)
            l <= 55  -> LevelParams(l, listOf("A2","B1"),  rounds = 12, timePerRoundSec = 7f,  hintsAllowed = 1, mistakePenalty = true,  mode = LevelMode.NORMAL)
            l <= 70  -> LevelParams(l, listOf("B1"),       rounds = 12, timePerRoundSec = 6f,  hintsAllowed = 1, mistakePenalty = true,  mode = LevelMode.HARD)
            l <= 85  -> LevelParams(l, listOf("B1","B2"),  rounds = 14, timePerRoundSec = 5f,  hintsAllowed = 0, mistakePenalty = true,  mode = LevelMode.EXPERT)
            else     -> LevelParams(l, listOf("B2","C1"),  rounds = 15, timePerRoundSec = 4f,  hintsAllowed = 0, mistakePenalty = true,  mode = LevelMode.MASTER)
        }
    }

    /** Сколько звёзд за процент правильных ответов. */
    fun starsForScore(percent: Int): Int = when {
        percent >= 90 -> 3
        percent >= 70 -> 2
        percent >= 50 -> 1
        else          -> 0
    }
}

/**
 * Менеджер прогресса уровней — единая точка для всех игр.
 * Хранит результаты, считает звёзды, отвечает за линейный анлок.
 */
@Singleton
class GameLevelManager @Inject constructor(
    private val dao: GameLevelProgressDao
) {
    /** Доступен ли уровень для игры. Уровень 1 всегда открыт; N+1 — если N пройден. */
    suspend fun isUnlocked(gameId: String, level: Int): Boolean {
        if (level <= 1) return true
        val maxCleared = dao.maxClearedLevel(gameId)
        return level <= maxCleared + 1
    }

    /** Текущий «фронтир» — следующий доступный для прохождения уровень. */
    suspend fun nextLevel(gameId: String): Int =
        (dao.maxClearedLevel(gameId) + 1).coerceAtMost(100)

    /** Карта level → (звёзды, лучший процент). Заодно сообщает, открыт ли уровень. */
    suspend fun getProgressMap(gameId: String): Map<Int, GameLevelProgressEntity> =
        dao.getForGame(gameId).associateBy { it.levelNum }

    /** Суммарное число звёзд по игре (0..300). */
    suspend fun totalStars(gameId: String): Int = dao.totalStars(gameId)

    /**
     * Сохранить результат прохождения уровня. Возвращает заработанные звёзды.
     * Не понижает результат — берётся MAX от существующего.
     */
    suspend fun completeLevel(gameId: String, level: Int, percent: Int): Int {
        val stars = LevelDifficulty.starsForScore(percent)
        val existing = dao.getOne(gameId, level)
        val newStars = maxOf(existing?.stars ?: 0, stars)
        val newBest  = maxOf(existing?.bestScore ?: 0, percent)
        dao.upsert(
            GameLevelProgressEntity(
                gameId      = gameId,
                levelNum    = level,
                stars       = newStars,
                bestScore   = newBest,
                completedAt = System.currentTimeMillis()
            )
        )
        return stars
    }
}
