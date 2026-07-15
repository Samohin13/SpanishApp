package com.spanishapp.domain.games

import com.spanishapp.data.db.dao.GameLevelProgressDao
import com.spanishapp.data.db.entity.GameLevelProgressEntity
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * v1.23.2: снимок состояния всех уровней игры — для LevelMapScreen.
 * Получается из одного DB query (observeForGame), все 3 поля derived.
 */
data class GameLevelOverview(
    val progress: Map<Int, GameLevelProgressEntity>,
    val nextLevel: Int,
    val totalStars: Int,
)

/**
 * Идентификаторы игр (используются как `game_id` в game_level_progress).
 */
object GameId {
    const val ARTICLES  = "articles"
    const val SPEED     = "speed"
    const val VERBOS    = "verbos"
    const val SOPA      = "sopa"
    const val PALABRA   = "palabra"
    const val MATH      = "math"
    const val CROSSWORD = "crossword"
    const val FRASE     = "frase"   // v1.27: Frase Loca — собери фразу
    const val OIDO      = "oido"    // v1.27: El Oído — тренажёр слуха
    // libros — не трогаем (там свой libro_progress)
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
    private val dao: GameLevelProgressDao,
    private val hintBank: com.spanishapp.service.HintBankManager? = null,
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
     * v1.23.2 (audit Bug 11): единый Flow всех трёх значений из одного DB
     * query. Заменяет 3 sequential `getProgressMap()` + `nextLevel()` +
     * `totalStars()` которые на LevelMapScreen вызывали UX flicker (грид
     * сначала рисовался пустым, потом перерисовывался).
     *
     * Бонус: реактивный — когда юзер прошёл уровень в LevelSession, Room
     * эмитит новый список → грид обновляется автоматически без необходимости
     * pop'а и LaunchedEffect refresh.
     */
    fun observeOverview(gameId: String): kotlinx.coroutines.flow.Flow<GameLevelOverview> =
        dao.observeForGame(gameId).map { entries ->
            val map = entries.associateBy { it.levelNum }
            val maxCleared = entries
                .filter { it.stars > 0 }
                .maxOfOrNull { it.levelNum } ?: 0
            GameLevelOverview(
                progress = map,
                nextLevel = (maxCleared + 1).coerceAtMost(100),
                totalStars = entries.sumOf { it.stars },
            )
        }

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
        // v1.16.0: +5 💡 за 3 звезды (только при первом достижении 3* — newStars
        // = 3 И existing.stars < 3, чтоб не давать повторно при перепрохождении).
        if (newStars == 3 && (existing?.stars ?: 0) < 3) {
            hintBank?.award(50, com.spanishapp.service.HintEarnReason.GAME_3_STARS)
        }
        return stars
    }

    /**
     * Сохранить уровень по прямому числу звёзд (1..3). Используется играми,
     * которые считают звёзды не из процента точности, а из штрафов (Crucigrama).
     * Звезды никогда не понижаются — берётся MAX от существующего.
     */
    suspend fun completeLevelByStars(gameId: String, level: Int, stars: Int): Int {
        val clampedStars = stars.coerceIn(0, 3)
        val existing = dao.getOne(gameId, level)
        val newStars = maxOf(existing?.stars ?: 0, clampedStars)
        // bestScore хранит «эквивалентный процент» для UI-показа.
        val equivalentPercent = when (newStars) {
            3 -> 100
            2 -> 80
            1 -> 60
            else -> 0
        }
        val newBest = maxOf(existing?.bestScore ?: 0, equivalentPercent)
        dao.upsert(
            GameLevelProgressEntity(
                gameId      = gameId,
                levelNum    = level,
                stars       = newStars,
                bestScore   = newBest,
                completedAt = System.currentTimeMillis()
            )
        )
        // v1.16.0: +5 💡 за первое 3-звёздочное прохождение
        if (newStars == 3 && (existing?.stars ?: 0) < 3) {
            hintBank?.award(50, com.spanishapp.service.HintEarnReason.GAME_3_STARS)
        }
        return newStars
    }
}
