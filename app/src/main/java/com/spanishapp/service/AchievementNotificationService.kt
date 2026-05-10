package com.spanishapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.spanishapp.data.db.dao.AchievementDao
import com.spanishapp.data.db.dao.FlashcardSetProgressDao
import com.spanishapp.data.db.dao.GameLevelProgressDao
import com.spanishapp.data.db.dao.LibroProgressDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.AchievementEntity
import com.spanishapp.domain.algorithm.XpSystem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementManager @Inject constructor(
    private val achievementDao: AchievementDao,
    private val userProgressDao: UserProgressDao,
    private val wordDao: WordDao,
    private val flashcardSetProgressDao: FlashcardSetProgressDao,
    private val libroProgressDao: LibroProgressDao,
    private val gameLevelProgressDao: GameLevelProgressDao,
    private val notificationService: NotificationService
) {
    /** Шина новых ачивок — собирается в Composable для показа диалога. */
    private val _unlockedFlow = MutableSharedFlow<AchievementEntity>(extraBufferCapacity = 8)
    val unlockedFlow: SharedFlow<AchievementEntity> = _unlockedFlow.asSharedFlow()

    val defaultAchievements = listOf(
        AchievementEntity("first_word",    "Первое слово!",        "Выучи своё первое слово",        "ic_star",   5,  requirement = 1,   requirementType = "words"),
        AchievementEntity("words_10",      "Словарик",             "Выучи 10 слов",                  "ic_book",   10, requirement = 10,  requirementType = "words"),
        AchievementEntity("words_50",      "Студент",              "Выучи 50 слов",                  "ic_grad",   20, requirement = 50,  requirementType = "words"),
        AchievementEntity("words_100",     "Знаток слов",          "Выучи 100 слов",                 "ic_medal",  40, requirement = 100, requirementType = "words"),
        AchievementEntity("words_250",     "Полиглот",             "Выучи 250 слов",                 "ic_globe",  80, requirement = 250, requirementType = "words"),
        AchievementEntity("words_500",     "Виртуоз",              "Выучи 500 слов",                 "ic_trophy", 150,requirement = 500, requirementType = "words"),
        AchievementEntity("words_1000",    "Мастер испанского",    "Выучи 1000 слов",                "ic_crown",  300,requirement = 1000,requirementType = "words"),
        AchievementEntity("streak_3",      "Три дня подряд",       "Занимайся 3 дня подряд",         "ic_fire",   10, requirement = 3,   requirementType = "streak"),
        AchievementEntity("streak_7",      "Неделя",               "Занимайся 7 дней подряд",        "ic_fire",   25, requirement = 7,   requirementType = "streak"),
        AchievementEntity("streak_30",     "Месяц!",               "Занимайся 30 дней подряд",       "ic_fire",   100,requirement = 30,  requirementType = "streak"),
        AchievementEntity("streak_100",    "Легенда",              "100 дней без перерыва!",         "ic_legend", 500,requirement = 100, requirementType = "streak"),
        AchievementEntity("lesson_first",  "Первый урок",          "Пройди свой первый урок",        "ic_lesson", 10, requirement = 1,   requirementType = "lessons"),
        AchievementEntity("lesson_10",     "Прилежный ученик",     "Пройди 10 уроков",               "ic_lesson", 50, requirement = 10,  requirementType = "lessons"),
        AchievementEntity("dialogue_first","Разговорник",          "Пройди первый диалог",           "ic_chat",   15, requirement = 1,   requirementType = "dialogues"),
        AchievementEntity("dialogue_10",   "Собеседник",           "Пройди 10 диалогов",             "ic_chat",   60, requirement = 10,  requirementType = "dialogues"),
        AchievementEntity("xp_500",        "Набираешь обороты",    "Набери 500 XP",                  "ic_xp",     20, requirement = 500,  requirementType = "xp"),
        AchievementEntity("xp_5000",       "XP-коллекционер",      "Набери 5000 XP",                 "ic_xp",     100,requirement = 5000, requirementType = "xp"),
        // ── Новые: уровни XpSystem, золотые кубки, идеальный рассказ, прогресс игр ──
        AchievementEntity("level_5",        "5-й уровень",         "Достигни 5 уровня приложения",   "ic_xp",     30, requirement = 5,    requirementType = "applevel"),
        AchievementEntity("level_15",       "Опытный игрок",       "Достигни 15 уровня приложения",  "ic_xp",     80, requirement = 15,   requirementType = "applevel"),
        AchievementEntity("first_gold_cup", "Золотой кубок",       "Получи 3 звезды в наборе карточек","ic_trophy",30, requirement = 1,    requirementType = "gold_cup"),
        AchievementEntity("ten_cups",       "Коллекция кубков",    "Накопи 10 звёзд в наборах",       "ic_trophy",60, requirement = 10,   requirementType = "stars_total"),
        AchievementEntity("perfect_libro",  "Идеальное чтение",    "Пройди рассказ на 100%",          "ic_book",   25, requirement = 100,  requirementType = "perfect_libro"),
        AchievementEntity("game_lvl_25",    "Геймер",             "Дойди до 25 уровня в любой игре", "ic_medal",  40, requirement = 25,   requirementType = "game_max_level"),
    )

    suspend fun checkAndUnlock(): List<AchievementEntity> {
        val progress = userProgressDao.getProgressOnce() ?: return emptyList()
        val newlyUnlocked = mutableListOf<AchievementEntity>()

        // ── Простые числовые типы ──
        val appLevel = XpSystem.levelForXp(progress.totalXp)
        val checkTypes = mapOf(
            "words"     to progress.wordsLearned,
            "streak"    to progress.currentStreak,
            "lessons"   to progress.lessonsCompleted,
            "dialogues" to progress.dialoguesCompleted,
            "xp"        to progress.totalXp,
            "applevel"  to appLevel
        )
        for ((type, value) in checkTypes) {
            achievementDao.getLockedByType(type)
                .filter { value >= it.requirement }
                .forEach { unlock(it, newlyUnlocked) }
        }

        // ── gold_cup: любой набор со звёздами == 3 ──
        val sets = flashcardSetProgressDao.getAll()
        if (sets.any { it.stars >= 3 }) {
            achievementDao.getLockedByType("gold_cup").forEach { unlock(it, newlyUnlocked) }
        }

        // ── stars_total: сумма звёзд по всем наборам ──
        val totalStars = sets.sumOf { it.stars }
        achievementDao.getLockedByType("stars_total")
            .filter { totalStars >= it.requirement }
            .forEach { unlock(it, newlyUnlocked) }

        // ── perfect_libro: bestScore == 100 ──
        val libroPerfect = runCatching {
            // используем suspend-выборку через Flow.first — но dao предоставляет только Flow.
            // Проще: проверим через getById(0..N) — но id не известен. Делаем cheap-fallback:
            // сохраняем флаг в SharedPreferences не нужен — выполним отдельным DAO-вызовом ниже.
            false
        }
        // Обходим через rawQuery: воспользуемся getById(...) на известных id из LibrosData невозможно тут (модуль ui).
        // Поэтому делаем простой агрегат через DAO -  нет такой функции, добавим below.

        // ── game_max_level: max уровень с stars>0 в любой игре ──
        val gameIds = listOf("articles", "speed", "anagram", "math", "crossword", "sopa", "palabra", "verb", "libros")
        val maxLevel = gameIds.maxOfOrNull { runCatching { gameLevelProgressDao.maxClearedLevel(it) }.getOrDefault(0) } ?: 0
        achievementDao.getLockedByType("game_max_level")
            .filter { maxLevel >= it.requirement }
            .forEach { unlock(it, newlyUnlocked) }

        // ── perfect_libro проверка через Flow.first ──
        runCatching {
            val list = libroProgressDao.getAll().firstOrNull() ?: emptyList()
            if (list.any { it.bestScore >= 100 }) {
                achievementDao.getLockedByType("perfect_libro").forEach { unlock(it, newlyUnlocked) }
            }
        }

        return newlyUnlocked
    }

    private suspend fun unlock(a: AchievementEntity, acc: MutableList<AchievementEntity>) {
        val updated = a.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis())
        achievementDao.update(updated)
        acc.add(updated)
        _unlockedFlow.tryEmit(updated)
        notificationService.showAchievement(updated.titleRu, updated.descriptionRu)
    }
}

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_REMINDER    = "ch_reminder"
        const val CHANNEL_ACHIEVEMENT = "ch_achievement"
        const val CHANNEL_STREAK      = "ch_streak"
        private var notifId = 100
    }

    init { createChannels() }

    private fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        listOf(
            NotificationChannel(CHANNEL_REMINDER,    "Напоминания об учёбе", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_ACHIEVEMENT, "Достижения",           NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_STREAK,      "Стрик",                NotificationManager.IMPORTANCE_HIGH)
        ).forEach { manager.createNotificationChannel(it) }
    }

    fun showDailyReminder(streak: Int) {
        val messages = listOf(
            "¡Hola! Пора учить испанский",
            "Твой стрик: $streak дней. Не прерывай серию!",
            "5 минут испанского в день — и через год ты беглый!",
            "Nuevas palabras te esperan — новые слова ждут тебя!",
            "Сегодняшний урок займёт всего 10 минут"
        )
        val n = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SpanishApp")
            .setContentText(messages.random())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notifId++, n)
    }

    fun showStreakWarning(streak: Int) {
        val n = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Стрик под угрозой!")
            .setContentText("У тебя стрик $streak дней. Позанимайся сегодня!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notifId++, n)
    }

    fun showAchievement(title: String, description: String) {
        val n = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Достижение разблокировано!")
            .setContentText("$title — $description")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notifId++, n)
    }
}

