package com.spanishapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.spanishapp.R
import com.spanishapp.data.db.dao.AchievementDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.AchievementEntity
import com.spanishapp.domain.algorithm.StreakManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ═════════════════════════════════════════════════════════════
//  ACHIEVEMENT MANAGER  —  check and unlock achievements
// ═════════════════════════════════════════════════════════════
@Singleton
class AchievementManager @Inject constructor(
    private val achievementDao: AchievementDao,
    private val userProgressDao: UserProgressDao,
    private val wordDao: WordDao,
    private val notificationService: NotificationService
) {
    // Default achievements seeded on first launch
    val defaultAchievements = listOf(
        AchievementEntity("first_word",    "Первое слово!",        "Выучи своё первое слово",        "ic_star",   5,  requirement = 1,   requirementType = "words"),
        AchievementEntity("words_10",      "Словарик",             "Выучи 10 слов",                  "ic_book",   10, requirement = 10,  requirementType = "words"),
        AchievementEntity("words_50",      "Студент",              "Выучи 50 слов",                  "ic_graduate",20,requirement = 50,  requirementType = "words"),
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

// ═════════════════════════════════════════════════════════════
//  NOTIFICATION SERVICE
// ═════════════════════════════════════════════════════════════
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
            NotificationChannel(CHANNEL_REMINDER,    "Напоминания об учёбе", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Ежедневные напоминания о занятиях" },
            NotificationChannel(CHANNEL_ACHIEVEMENT, "Достижения",           NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Уведомления о новых достижениях" },
            NotificationChannel(CHANNEL_STREAK,      "Стрик",                NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Предупреждения о потере стрика" }
        ).forEach { manager.createNotificationChannel(it) }
    }

    fun showDailyReminder(streak: Int) {
        val messages = listOf(
            "¡Hola! Пора учить испанский 🇪🇸",
            "Твой стрик: $streak ${streakEmoji(streak)}. Не прерывай серию!",
            "5 минут испанского в день — и через год ты беглый! 🚀",
            "Nuevas palabras te esperan — новые слова ждут тебя!",
            "¡Ánimo! Сегодняшний урок займёт всего 10 минут 💪"
        )

        val n = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("SpanishApp")
            .setContentText(messages.random())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(notifId++, n)
    }

    fun showStreakWarning(streak: Int) {
        val n = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(R.drawable.ic_fire)
            .setContentTitle("⚠️ Стрик под угрозой!")
            .setContentText("У тебя стрик $streak дней. Позанимайся сегодня, чтобы не потерять его!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(notifId++, n)
    }

    fun showAchievement(title: String, description: String) {
        val n = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENT)
            .setSmallIcon(R.drawable.ic_trophy)
            .setContentTitle("🏆 Достижение разблокировано!")
            .setContentText("$title — $description")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(notifId++, n)
    }

    private fun streakEmoji(streak: Int) = when {
        streak >= 100 -> "🔥🔥🔥"
        streak >= 30  -> "🔥🔥"
        streak >= 7   -> "🔥"
        else          -> "✨"
    }
}

// ═════════════════════════════════════════════════════════════
//  DAILY REMINDER WORKER  (WorkManager)
// ═════════════════════════════════════════════════════════════
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userProgressDao: UserProgressDao,
    private val notificationService: NotificationService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val progress = userProgressDao.getProgressOnce() ?: return Result.success()

        // Check if user already studied today
        val todayStart = LocalDate.now().toEpochDay() * 86_400_000L
        val studiedToday = progress.lastStudyDate >= todayStart

        if (!studiedToday) {
            if (StreakManager.isStreakAtRisk(progress.lastStudyDate) && progress.currentStreak > 2) {
                notificationService.showStreakWarning(progress.currentStreak)
            } else {
                notificationService.showDailyReminder(progress.currentStreak)
            }
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setInitialDelay(1, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}