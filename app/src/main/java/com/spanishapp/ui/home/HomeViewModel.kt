package com.spanishapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.*
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.domain.algorithm.AdaptiveLearning
import com.spanishapp.domain.algorithm.StreakManager
import com.spanishapp.domain.algorithm.XpSystem
import com.spanishapp.service.AchievementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val wordDao: WordDao,
    private val lessonDao: LessonDao,
    private val dailyWordDao: DailyWordDao,
    private val achievementManager: AchievementManager
) : ViewModel() {

    // ── UI State ──────────────────────────────────────────────
    val uiState: StateFlow<HomeUiState> = combine(
        userProgressDao.getProgress(),
        wordDao.getDueWords(),
        wordDao.learnedCount(),
        lessonDao.getNextLessons()
    ) { progress, dueWords, learnedCount, nextLessons ->

        val p = progress ?: UserProgressEntity()
        val plan = AdaptiveLearning.planSession(
            dueWordsCount       = dueWords.size,
            dailyGoalMinutes    = p.dailyGoalMinutes,
            currentLevel        = p.currentLevel,
            studiedTodayMinutes = todayStudyMinutes(p),
            weakWordsCount      = 0
        )
        val shouldLevelUp = AdaptiveLearning.shouldLevelUp(
            wordsLearned      = p.wordsLearned,
            lessonsCompleted  = p.lessonsCompleted,
            currentLevel      = p.currentLevel
        )

        HomeUiState(
            displayName         = p.displayName,
            totalXp             = p.totalXp,
            appLevel            = XpSystem.levelForXp(p.totalXp),
            levelProgress       = XpSystem.progressToNextLevel(p.totalXp),
            currentStreak       = p.currentStreak,
            longestStreak       = p.longestStreak,
            wordsLearned        = p.wordsLearned,
            learnedCount        = learnedCount,
            dueWordsCount       = dueWords.size,
            dailyGoalMinutes    = p.dailyGoalMinutes,
            todayMinutes        = todayStudyMinutes(p),
            nextLessons         = nextLessons.map { it.title },
            sessionPlan         = plan,
            spanishLevel        = p.currentLevel,
            shouldLevelUp       = shouldLevelUp,
            isLoading           = false
        )
    }
        .catch { emit(HomeUiState(isLoading = false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    // ── Daily word of the day ─────────────────────────────────
    val wordOfTheDay: StateFlow<WordOfDay?> = flow {
        val today = LocalDate.now().toString()
        val daily = dailyWordDao.getForDate(today)
        if (daily != null) {
            val word = wordDao.getById(daily.wordId)
            if (word != null) emit(WordOfDay(word.spanish, word.russian, word.example, daily.wasPracticed))
        } else {
            emit(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Update streak on session start ────────────────────────
    fun onSessionStarted() {
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            val (newStreak, bonus) = StreakManager.calculateStreak(p.lastStudyDate, p.currentStreak)

            userProgressDao.update(
                p.copy(
                    currentStreak  = newStreak,
                    longestStreak  = maxOf(p.longestStreak, newStreak),
                    lastStudyDate  = System.currentTimeMillis(),
                    totalXp        = p.totalXp + bonus
                )
            )

            // Check achievements after any progress update
            achievementManager.checkAndUnlock()
        }
    }

    private fun todayStudyMinutes(p: UserProgressEntity): Int {
        val todayStart = LocalDate.now().toEpochDay() * 86_400_000L
        return if (p.lastStudyDate >= todayStart) {
            // We store total minutes but not per-day; use a simple estimate
            // In production you'd track a per-day session table
            minOf(p.totalStudyMinutes % 1440, p.dailyGoalMinutes)
        } else 0
    }
}

// ─────────────────────────────────────────────────────────────
// UI State data classes
// ─────────────────────────────────────────────────────────────
data class HomeUiState(
    val displayName: String = "",
    val totalXp: Int = 0,
    val appLevel: Int = 1,
    val levelProgress: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val wordsLearned: Int = 0,
    val learnedCount: Int = 0,
    val dueWordsCount: Int = 0,
    val dailyGoalMinutes: Int = 10,
    val todayMinutes: Int = 0,
    val nextLessons: List<String> = emptyList(),
    val sessionPlan: AdaptiveLearning.SessionPlan = AdaptiveLearning.SessionPlan(5,5,false,false,10),
    val spanishLevel: String = "A1",
    val shouldLevelUp: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class WordOfDay(
    val spanish: String,
    val russian: String,
    val example: String,
    val wasPracticed: Boolean
)