package com.spanishapp.service

import com.spanishapp.data.db.entity.UserProgressEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * v1.25.98: единая точка полной очистки пользовательских данных в Room +
 * голосовых файлов. Используется:
 *  • SettingsViewModel — Reset Progress / Delete Account
 *  • AuthViewModel — вход в ДРУГОЙ аккаунт на том же устройстве (раньше
 *    локальный прогресс юзера A мержился в облачный док юзера B навсегда —
 *    audit auth-H2).
 *
 * Seed-таблицы (words/conjugations/lessons/dialogues) сохраняют строки,
 * но их per-row статистика обнуляется.
 */
@Singleton
class UserDataWiper @Inject constructor(
    private val wordDao: com.spanishapp.data.db.dao.WordDao,
    private val achievementDao: com.spanishapp.data.db.dao.AchievementDao,
    private val libroProgressDao: com.spanishapp.data.db.dao.LibroProgressDao,
    private val flashcardSetProgressDao: com.spanishapp.data.db.dao.FlashcardSetProgressDao,
    private val lessonProgressDao: com.spanishapp.data.db.dao.LessonProgressDao,
    private val dailyXpDao: com.spanishapp.data.db.dao.DailyXpDao,
    private val gameLevelProgressDao: com.spanishapp.data.db.dao.GameLevelProgressDao,
    private val articleGameDao: com.spanishapp.data.db.dao.ArticleGameDao,
    private val wordListDao: com.spanishapp.data.db.dao.WordListDao,
    private val recentSearchDao: com.spanishapp.data.db.dao.RecentSearchDao,
    private val weeklyLeagueDao: com.spanishapp.data.db.dao.WeeklyLeagueDao,
    private val userProgressDao: com.spanishapp.data.db.dao.UserProgressDao,
    private val chatMessageDao: com.spanishapp.data.db.dao.ChatMessageDao,
    private val theoryProgressDao: com.spanishapp.data.db.dao.TheoryProgressDao,
    private val gameMistakesDao: com.spanishapp.data.db.dao.GameMistakesDao,
    private val activityTimeLogDao: com.spanishapp.data.db.dao.ActivityTimeLogDao,
    private val lessonCompletionHistoryDao: com.spanishapp.data.db.dao.LessonCompletionHistoryDao,
    private val userVocabStateDao: com.spanishapp.data.db.dao.UserVocabStateDao,
    private val weakVerbDao: com.spanishapp.data.db.dao.WeakVerbDao,
    private val voiceMessageStorage: VoiceMessageStorage,
) {
    suspend fun wipeAll() {
        wordDao.resetAllStats()
        achievementDao.resetAll()
        libroProgressDao.deleteAll()
        flashcardSetProgressDao.deleteAll()
        lessonProgressDao.deleteAll()
        dailyXpDao.deleteAll()
        gameLevelProgressDao.deleteAll()
        articleGameDao.deleteAllProgress()
        wordListDao.deleteAllEntries()
        wordListDao.deleteAllLists()
        recentSearchDao.deleteAll()
        weeklyLeagueDao.deleteAll()
        chatMessageDao.deleteAll()
        runCatching { voiceMessageStorage.deleteAll() }
        theoryProgressDao.deleteAll()
        gameMistakesDao.deleteAll()
        activityTimeLogDao.deleteAll()
        lessonCompletionHistoryDao.deleteAll()
        userVocabStateDao.deleteAll()
        weakVerbDao.deleteAll()
        userProgressDao.update(UserProgressEntity())
    }
}
