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
    private val notificationService: NotificationService,
    private val hintBank: HintBankManager,
    // v1.25.97 (audit): XP за ачивку реально начисляется (раньше UI показывал
    // «+N XP», но totalXp не менялся — награда была фантомной). Lazy — единый
    // паттерн защиты от Hilt-цикла (XpTracker → LeaderboardRepo → ...).
    private val xpTracker: dagger.Lazy<XpTracker>,
) {
    /** Шина новых ачивок — собирается в Composable для показа диалога. */
    private val _unlockedFlow = MutableSharedFlow<AchievementEntity>(extraBufferCapacity = 8)
    val unlockedFlow: SharedFlow<AchievementEntity> = _unlockedFlow.asSharedFlow()

    /**
     * 23 достижения, переписанных в 1.1.0. Раньше каша из ic_star/ic_book/
     * ic_medal/ic_trophy/ic_crown/ic_chat и т.д. — юзер не понимал
     * иерархию: «Золотой кубок» имел иконку медали, бронзовая медаль
     * выдавалась за лёгкое первое слово.
     *
     * Новая система: ОДНА семантика — кубок 🏆. Tier (бронза/серебро/
     * золото) определяется в UI через xpReward:
     *   bronze:  5-19   xp — первый шаг, легко
     *   silver:  20-79  xp — упорство
     *   gold:    80+    xp — реальное достижение
     *
     * Названия и описания пересмотрены — стало понятно по СМЫСЛУ что
     * легко а что сложно.
     */
    val defaultAchievements = listOf(
        // ── 🥉 БРОНЗА — первые шаги (8 штук) ──────────────────────
        AchievementEntity("first_word",    "Первое слово",         "Выучи своё первое испанское слово",     "ic_trophy", 5,  requirement = 1,    requirementType = "words"),
        AchievementEntity("words_10",      "Словарный запас",      "10 слов в копилке",                     "ic_trophy", 10, requirement = 10,   requirementType = "words"),
        AchievementEntity("streak_3",      "Уже привычка",         "3 дня занятий подряд",                  "ic_trophy", 10, requirement = 3,    requirementType = "streak"),
        AchievementEntity("lesson_first",  "Первый урок",          "Пройди свой первый урок",               "ic_trophy", 10, requirement = 1,    requirementType = "lessons"),
        AchievementEntity("dialogue_first","Первый диалог",        "Освой первый диалог",                   "ic_trophy", 15, requirement = 1,    requirementType = "dialogues"),
        AchievementEntity("xp_500",        "Старт",                "Набери первые 500 XP",                  "ic_trophy", 15, requirement = 500,  requirementType = "xp"),
        AchievementEntity("level_5",       "5 уровень",            "Достигни 5 уровня приложения",          "ic_trophy", 15, requirement = 5,    requirementType = "applevel"),
        AchievementEntity("first_gold_cup","Первая 3-звезда",      "Пройди набор карточек на 3 звезды",     "ic_trophy", 15, requirement = 1,    requirementType = "gold_cup"),

        // ── 🥈 СЕРЕБРО — упорство (10 штук) ──────────────────────
        AchievementEntity("words_50",      "Прилежный",            "50 выученных слов",                     "ic_trophy", 25, requirement = 50,   requirementType = "words"),
        AchievementEntity("words_100",     "Знаток слов",          "100 выученных слов",                    "ic_trophy", 40, requirement = 100,  requirementType = "words"),
        AchievementEntity("streak_7",      "Неделя огня",          "7 дней без перерыва",                   "ic_trophy", 30, requirement = 7,    requirementType = "streak"),
        AchievementEntity("lesson_10",     "Десять уроков",        "Пройди 10 уроков",                      "ic_trophy", 50, requirement = 10,   requirementType = "lessons"),
        AchievementEntity("dialogue_10",   "Собеседник",           "10 диалогов позади",                    "ic_trophy", 60, requirement = 10,   requirementType = "dialogues"),
        AchievementEntity("xp_5000",       "XP-коллекционер",      "5 000 XP в копилке",                    "ic_trophy", 60, requirement = 5000, requirementType = "xp"),
        AchievementEntity("level_15",      "Опытный",              "Достигни 15 уровня",                    "ic_trophy", 60, requirement = 15,   requirementType = "applevel"),
        AchievementEntity("ten_cups",      "Коллекция звёзд",      "10 звёзд в наборах карточек",           "ic_trophy", 50, requirement = 10,   requirementType = "stars_total"),
        AchievementEntity("perfect_libro", "Идеальное чтение",     "Пройди любой рассказ на 100%",          "ic_trophy", 30, requirement = 100,  requirementType = "perfect_libro"),
        AchievementEntity("game_lvl_25",   "Геймер",               "25-й уровень в любой мини-игре",        "ic_trophy", 50, requirement = 25,   requirementType = "game_max_level"),

        // ── 🥇 ЗОЛОТО — редкое достижение (5 штук) ────────────────
        AchievementEntity("words_250",     "Полиглот",             "250 выученных слов — впечатляет",       "ic_trophy", 100, requirement = 250,  requirementType = "words"),
        AchievementEntity("words_500",     "Виртуоз",              "500 слов — половина пути к B2",         "ic_trophy", 200, requirement = 500,  requirementType = "words"),
        AchievementEntity("words_1000",    "Мастер испанского",    "1 000 слов — уровень носителя",         "ic_trophy", 400, requirement = 1000, requirementType = "words"),
        AchievementEntity("streak_30",     "Месячный марафон",     "30 дней подряд — стальная дисциплина",  "ic_trophy", 150, requirement = 30,   requirementType = "streak"),
        AchievementEntity("streak_100",    "Легенда",              "100 дней без перерыва — невероятно!",   "ic_trophy", 500, requirement = 100,  requirementType = "streak"),
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
        // v1.25.97 (audit): атомарный unlock — конкурентные checkAndUnlock
        // (RatingUpdater per-answer + StreakService + ViewModels) оба читали
        // is_unlocked=0 → двойная нотификация/награды. Теперь побеждает один.
        val ts = System.currentTimeMillis()
        val rows = achievementDao.unlockIfLocked(a.id, ts)
        if (rows == 0) return  // уже разблокирована конкурентным вызовом
        val updated = a.copy(isUnlocked = true, unlockedAt = ts)
        acc.add(updated)
        _unlockedFlow.tryEmit(updated)
        notificationService.showAchievement(updated.titleRu, updated.descriptionRu)
        // v1.16.0: +5 💡 за разблокировку достижения
        hintBank.award(5, HintEarnReason.ACHIEVEMENT)
        // v1.25.97 (audit): и обещанный XP — раньше не начислялся вообще.
        if (a.xpReward > 0) {
            runCatching { xpTracker.get().add(xp = a.xpReward, words = 0) }
        }
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

    /** True iff Android 13+ POST_NOTIFICATIONS is granted (or pre-13). */
    private fun canPostNotifications(): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return true
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        listOf(
            NotificationChannel(CHANNEL_REMINDER,    "Напоминания об учёбе", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_ACHIEVEMENT, "Достижения",           NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_STREAK,      "Стрик",                NotificationManager.IMPORTANCE_HIGH)
        ).forEach { manager.createNotificationChannel(it) }
    }

    fun showDailyReminder(streak: Int) {
        if (!canPostNotifications()) return
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
        if (!canPostNotifications()) return
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
        // Android 13+ requires POST_NOTIFICATIONS; the achievement itself is
        // still unlocked in Room. Silent skip avoids SecurityException on
        // stricter OEM builds.
        if (!canPostNotifications()) return

        // 1.1.1 fix: тестер сообщил что push выглядит дешево, лезет в
        // статус-бар, не свайпается. Чиним:
        //  • setSmallIcon → собственный ic_notification_trophy (silhouette
        //    кубка вместо стандартной серой info-иконки)
        //  • setColor → orange brand для подкрашивания иконки
        //  • setStyle(BigText) → описание не обрезается
        //  • setContentIntent → тап открывает Achievements экран
        //  • setVisibility(PUBLIC) → видно на lock-screen
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = launchIntent?.let {
            android.app.PendingIntent.getActivity(
                context,
                notifId,
                it,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENT)
            .setSmallIcon(com.spanishapp.R.drawable.ic_notification_trophy)
            .setColor(android.graphics.Color.parseColor("#FF6B35"))   // brand orange
            .setContentTitle("🏆 Достижение разблокировано!")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title\n$description"))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)

        if (pendingIntent != null) builder.setContentIntent(pendingIntent)

        context.getSystemService(NotificationManager::class.java).notify(notifId++, builder.build())
    }
}

