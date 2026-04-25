package com.spanishapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.spanishapp.data.db.dao.AchievementDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.AchievementEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementManager @Inject constructor(
    private val achievementDao: AchievementDao,
    private val userProgressDao: UserProgressDao,
    private val wordDao: WordDao,
    private val notificationService: NotificationService
) {
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
    )

    suspend fun checkAndUnlock(): List<AchievementEntity> {
        val progress = userProgressDao.getProgressOnce() ?: return emptyList()
        val newlyUnlocked = mutableListOf<AchievementEntity>()

        val checkTypes = mapOf(
            "words"     to progress.wordsLearned,
            "streak"    to progress.currentStreak,
            "lessons"   to progress.lessonsCompleted,
            "dialogues" to progress.dialoguesCompleted,
            "xp"        to progress.totalXp
        )

        for ((type, value) in checkTypes) {
            val locked = achievementDao.getLockedByType(type)
            locked.filter { value >= it.requirement }.forEach { achievement ->
                achievementDao.update(
                    achievement.copy(
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis()
                    )
                )
                newlyUnlocked.add(achievement)
                notificationService.showAchievement(achievement.titleRu, achievement.descriptionRu)
            }
        }
        return newlyUnlocked
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

