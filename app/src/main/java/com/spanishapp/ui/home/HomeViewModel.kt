package com.spanishapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.*
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.data.db.entity.LessonEntity
import com.spanishapp.data.db.entity.LibroProgressEntity
import com.spanishapp.data.db.entity.FlashcardSetProgressEntity
import com.spanishapp.data.repository.AuthRepository
import com.spanishapp.domain.algorithm.AdaptiveLearning
import com.spanishapp.domain.algorithm.StreakManager
import com.spanishapp.domain.algorithm.XpSystem
import com.spanishapp.service.AchievementManager
import com.spanishapp.ui.flashcards.FlashcardSet
import com.spanishapp.ui.flashcards.FlashcardSetData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

// ── Daily-rotating greeting helpers ─────────────────────────────
// Time-of-day driven greeting + a deterministic daily motivation.
// Both update once per real day so the home screen feels alive without
// being random-on-every-recomposition.

private val MOTIVATIONS = listOf(
    "Каждое слово — шаг ближе к Испании 🇪🇸",
    "Маленький прогресс лучше нулевого",
    "Mañana = завтра. Hoy = сегодня. Делай сегодня.",
    "5 минут в день меняют всё",
    "Tu español está mejorando 💪",
    "Practica hoy, brilla mañana",
    "Полиглоты не рождаются — они тренируются",
    "Ещё один день — ещё одно слово"
)

internal fun greetingFor(time: LocalTime): String {
    val h = time.hour
    return when {
        h in 5..10  -> "Доброе утро, готов учиться?"
        h in 11..16 -> "Добрый день! Время для испанского"
        h in 17..21 -> "Добрый вечер, продолжаем?"
        else        -> "Ночные занятия — ¡vamos!"
    }
}

internal fun motivationFor(date: LocalDate): String {
    val idx = (date.toEpochDay().mod(MOTIVATIONS.size.toLong())).toInt()
    return MOTIVATIONS[idx]
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val wordDao: WordDao,
    private val lessonDao: LessonDao,
    private val dailyWordDao: DailyWordDao,
    private val lessonProgressDao: LessonProgressDao,
    private val dailyXpDao: com.spanishapp.data.db.dao.DailyXpDao,
    private val libroProgressDao: LibroProgressDao,
    private val flashcardSetProgressDao: FlashcardSetProgressDao,
    private val recentSearchDao: RecentSearchDao,
    private val achievementManager: AchievementManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── UI State ──────────────────────────────────────────────
    val uiState: StateFlow<HomeUiState> = combine(
        userProgressDao.getProgress(),
        wordDao.getDueWords(),
        wordDao.learnedCount(),
        lessonDao.getNextLessons(),
        lessonProgressDao.getAllCompletedKeys(),
        authRepository.userPhotoUrl,
        dailyXpDao.observeSince(LocalDate.now().toString())
    ) { args ->
        val progress = args[0] as? UserProgressEntity
        val dueWords = args[1] as List<WordEntity>
        val learnedCount = args[2] as Int
        val nextLessons = args[3] as List<LessonEntity>
        val completedKeysList = args[4] as List<String>
        val photoUrl = args[5] as? String
        @Suppress("UNCHECKED_CAST")
        val todayXpRows = args[6] as List<com.spanishapp.data.db.entity.DailyXpEntity>

        val completedKeys = completedKeysList.toSet()
        val p = progress ?: UserProgressEntity()
        val todayMinutes = todayXpRows.firstOrNull()?.minutes ?: 0
        val todayXp = todayXpRows.firstOrNull()?.xp ?: 0

        val plan = AdaptiveLearning.planSession(
            dueWordsCount       = dueWords.size,
            dailyGoalMinutes    = p.dailyGoalMinutes,
            currentLevel        = p.currentLevel,
            studiedTodayMinutes = todayMinutes,
            weakWordsCount      = 0
        )
        val shouldLevelUp = AdaptiveLearning.shouldLevelUp(
            wordsLearned     = p.wordsLearned,
            lessonsCompleted = p.lessonsCompleted,
            currentLevel     = p.currentLevel
        )

        val roadmapUnits = buildRoadmapUnits(completedKeys)

        HomeUiState(
            displayName      = p.displayName,
            totalXp          = p.totalXp,
            appLevel         = XpSystem.levelForXp(p.totalXp),
            levelProgress    = XpSystem.progressToNextLevel(p.totalXp),
            currentStreak    = p.currentStreak,
            longestStreak    = p.longestStreak,
            wordsLearned     = p.wordsLearned,
            learnedCount     = learnedCount,
            dueWordsCount    = dueWords.size,
            dailyGoalMinutes = p.dailyGoalMinutes,
            todayMinutes     = todayMinutes,
            todayXp          = todayXp,
            streakFreezes    = p.streakFreezesAvailable,
            studiedToday     = todayMinutes > 0 || p.lastStreakUpdateDate == LocalDate.now().toString(),
            nextLessons      = nextLessons.map { it.title },
            sessionPlan      = plan,
            spanishLevel     = p.currentLevel,
            shouldLevelUp    = shouldLevelUp,
            roadmapUnits     = roadmapUnits,
            userPhotoUrl     = photoUrl,
            skillRating      = p.skillRating,
            currentLeague    = p.currentLeague,
            isLoading        = false
        )
    }
        .catch { emit(HomeUiState(isLoading = false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    // ── Continue Pager: last lesson the user left unfinished ──
    // We surface the next not-yet-completed lesson from the canonical
    // RoadmapData ordering, paired with its unit's title for context.
    val lastLessonInProgress: StateFlow<ContinueLesson?> =
        lessonProgressDao.getAllCompletedKeys()
            .map { keys ->
                val done = keys.toSet()
                RoadmapData.units.firstNotNullOfOrNull { unit ->
                    val unitId = unit.id.toIntOrNull() ?: return@firstNotNullOfOrNull null
                    val nextIdx = unit.lessons.indices.firstOrNull { idx ->
                        "u${unitId}_l${idx}" !in done
                    } ?: return@firstNotNullOfOrNull null
                    ContinueLesson(
                        unitId       = unitId,
                        lessonIndex  = nextIdx,
                        unitTitle    = unit.title,
                        lessonTitle  = unit.lessons[nextIdx].title
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Continue Pager: most recently touched but unfinished libro ──
    // libro_progress doesn't track a separate "last_read_at", so we
    // use the existing completedAt timestamp (which the screen already
    // bumps on quiz attempts) as a proxy for "most recent activity".
    val lastBookInProgress: StateFlow<LibroProgressEntity?> =
        libroProgressDao.getAll()
            .map { rows ->
                rows.filter { !it.isCompleted }
                    .maxByOrNull { it.completedAt }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Continue Pager: next flashcard set to attack ──
    // Walks FlashcardSetData in declared order, returning the first
    // set the user hasn't earned 3 stars on yet.
    val nextIncompleteSet: StateFlow<FlashcardSet?> =
        flashcardSetProgressDao.observeAll()
            .map { rows ->
                val mastered = rows.filter { it.stars >= 3 }.map { it.setId }.toSet()
                FlashcardSetData.all.firstOrNull { it.id !in mastered }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Continue Pager: a random weak word for a quick refresher ──
    // Pulled from the broader Practice pool (see WordDao#getPracticePool)
    // so brand-new users still see *something* instead of an empty card.
    val weakSampleWord: StateFlow<WordEntity?> = flow {
        emit(wordDao.getPracticePool(5).randomOrNull())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Recent dictionary searches for the bento tile ──
    val recentWords: StateFlow<List<WordEntity>> =
        recentSearchDao.observeRecentWords(5)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Weekly heat-strip — minutes per day, Mon..Sun, exactly 7 entries ──
    val weeklyMinutes: StateFlow<List<Int>> =
        dailyXpDao.observeAll()
            .map { rows ->
                val today = LocalDate.now()
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val byDay = rows.associateBy { it.day }
                (0..6).map { offset ->
                    val day = monday.plusDays(offset.toLong()).toString()
                    byDay[day]?.minutes ?: 0
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { 0 })

    // ── Daily goals (3 simple boolean checks, derived live) ──
    // Goal #1: at least one flashcard set completed today.
    // Goal #2: any libro touched today (proxied by completedAt).
    // Goal #3: word-of-day quiz solved (DailyWordEntity.wasPracticed).
    val dailyGoals: StateFlow<DailyGoals> = combine(
        flashcardSetProgressDao.observeAll(),
        libroProgressDao.getAll(),
        flow {
            val today = LocalDate.now().toString()
            emit(dailyWordDao.getForDate(today)?.wasPracticed == true)
        }
    ) { sets, libros, wodPracticed ->
        val todayStart = LocalDate.now().atStartOfDay()
            .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val setDoneToday = sets.any { it.completedAt >= todayStart && it.stars > 0 }
        val libroToday   = libros.any { it.completedAt >= todayStart }
        DailyGoals(
            flashcardSetCompleted = setDoneToday,
            bookPageRead          = libroToday,
            wordOfDaySolved       = wodPracticed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyGoals())

    private fun buildRoadmapUnits(completedKeys: Set<String>): List<RoadmapUnit> {
        return RoadmapData.units.map { unit ->
            val unitId = unit.id.toIntOrNull()

            // Non-numeric IDs = A2/B1/B2 preview blocks (content not ready yet).
            // Show them as visually unlocked so the user can browse lesson titles,
            // but don't track real progress yet.
            if (unitId == null) {
                return@map unit.copy(
                    isLocked = false,
                    progress = 0f,
                    lessons  = unit.lessons.map { it.copy(isCompleted = false) }
                )
            }

            // Все юниты разблокированы — premium-логика убрана.
            val unlocked = true

            val lessonsWithProgress = unit.lessons.mapIndexed { idx, lesson ->
                lesson.copy(isCompleted = "u${unitId}_l${idx}" in completedKeys)
            }

            val completedCount = lessonsWithProgress.count { it.isCompleted }
            val progressFraction = if (unit.lessons.isNotEmpty())
                completedCount.toFloat() / unit.lessons.size else 0f

            unit.copy(
                isLocked = !unlocked,
                progress = if (unlocked) progressFraction else 0f,
                lessons  = lessonsWithProgress
            )
        }
    }

    val wordOfTheDay: StateFlow<WordOfDay?> = flow {
        val today = LocalDate.now().toString()
        val daily = dailyWordDao.getForDate(today)
        if (daily != null) {
            val word = wordDao.getById(daily.wordId)
            if (word != null) emit(
                WordOfDay(
                    spanish      = word.spanish,
                    russian      = word.russian,
                    example      = word.example,
                    wasPracticed = daily.wasPracticed,
                    wordId       = word.id,
                    level        = word.level,
                    category     = word.category
                )
            )
        } else {
            emit(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Mark today's WoD as practised — called after the user clears any quiz mode. */
    fun markWordOfDayPractised() {
        viewModelScope.launch {
            dailyWordDao.markPracticed(LocalDate.now().toString())
        }
    }

    /** Build a 4-button distractor set for the WoD quiz. */
    suspend fun loadDistractors(word: WordOfDay): List<WordEntity> {
        val sameCat = wordDao.randomDistractorsSameCategory(
            level     = word.level,
            category  = word.category,
            excludeId = word.wordId,
            limit     = 3
        )
        if (sameCat.size >= 3) return sameCat
        val padded = sameCat + wordDao.randomDistractors(
            level     = word.level,
            excludeId = word.wordId,
            limit     = 3 - sameCat.size
        )
        return padded.distinctBy { it.id }.take(3)
    }

    /** Random word for the Quick Actions "🎯" pill. */
    suspend fun pickRandomWord(): WordEntity? =
        wordDao.getRandomWords(1).firstOrNull()

    fun onSessionStarted() {
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            val (newStreak, bonus) = StreakManager.calculateStreak(p.lastStudyDate, p.currentStreak)

            userProgressDao.update(
                p.copy(
                    currentStreak = newStreak,
                    longestStreak = maxOf(p.longestStreak, newStreak),
                    lastStudyDate = System.currentTimeMillis(),
                    totalXp       = p.totalXp + bonus
                )
            )

            achievementManager.checkAndUnlock()
        }
    }

}

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
    val todayXp: Int = 0,
    val streakFreezes: Int = 2,
    val studiedToday: Boolean = false,
    val nextLessons: List<String> = emptyList(),
    val sessionPlan: AdaptiveLearning.SessionPlan = AdaptiveLearning.SessionPlan(5, 5, false, false, 10),
    val spanishLevel: String = "A1",
    val shouldLevelUp: Boolean = false,
    val roadmapUnits: List<RoadmapUnit> = emptyList(),
    val userPhotoUrl: String? = null,
    val skillRating: Int = 1000,
    val currentLeague: Int = 1,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class WordOfDay(
    val spanish: String,
    val russian: String,
    val example: String,
    val wasPracticed: Boolean,
    val wordId: Int = 0,
    val level: String = "A1",
    val category: String = "general"
)

/** Lightweight DTO for the "Continue → Урок" page in the Home pager. */
data class ContinueLesson(
    val unitId: Int,
    val lessonIndex: Int,
    val unitTitle: String,
    val lessonTitle: String
)

/** Three-checkbox daily goal used by the bento "Цель дня" tile. */
data class DailyGoals(
    val flashcardSetCompleted: Boolean = false,
    val bookPageRead: Boolean = false,
    val wordOfDaySolved: Boolean = false
) {
    val completedCount: Int get() = listOf(flashcardSetCompleted, bookPageRead, wordOfDaySolved).count { it }
    val allDone: Boolean get() = completedCount == 3
}
