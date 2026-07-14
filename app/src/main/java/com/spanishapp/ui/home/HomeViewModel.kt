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
//
// Return @StringRes Ints so the Composable resolves them through
// the current locale — otherwise non-Russian users would see Russian
// greetings even after switching language.

@androidx.annotation.StringRes
internal fun greetingResFor(time: LocalTime): Int {
    val h = time.hour
    return when {
        h in 5..10  -> com.spanishapp.R.string.home_greeting_morning
        h in 11..16 -> com.spanishapp.R.string.home_greeting_afternoon
        h in 17..21 -> com.spanishapp.R.string.home_greeting_evening
        else        -> com.spanishapp.R.string.home_greeting_night
    }
}

private val MOTIVATION_RES = listOf(
    com.spanishapp.R.string.home_motivation_1,
    com.spanishapp.R.string.home_motivation_2,
    com.spanishapp.R.string.home_motivation_3,
    com.spanishapp.R.string.home_motivation_4,
    com.spanishapp.R.string.home_motivation_5,
    com.spanishapp.R.string.home_motivation_6,
    com.spanishapp.R.string.home_motivation_7,
)

@androidx.annotation.StringRes
internal fun motivationResFor(date: LocalDate): Int {
    val idx = (date.toEpochDay().mod(MOTIVATION_RES.size.toLong())).toInt()
    return MOTIVATION_RES[idx]
}

/** v1.26.1: результат стадии «Произнеси» WoD-квиза — score 0..100 либо error. */
data class WodSpeakResult(val score: Int? = null, val error: String? = null)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext
    private val appContext: android.content.Context,
    private val userProgressDao: UserProgressDao,
    private val wordDao: WordDao,
    private val lessonDao: LessonDao,
    private val dailyWordDao: DailyWordDao,
    private val lessonProgressDao: LessonProgressDao,
    private val dailyXpDao: com.spanishapp.data.db.dao.DailyXpDao,
    private val libroProgressDao: LibroProgressDao,
    private val flashcardSetProgressDao: FlashcardSetProgressDao,
    private val recentSearchDao: RecentSearchDao,
    private val wodHistoryDao: WodHistoryDao,
    private val achievementManager: AchievementManager,
    private val authRepository: AuthRepository,
    private val radioListeningDao: com.spanishapp.radio.data.RadioListeningDao,
    private val xpTracker: com.spanishapp.service.XpTracker,
    private val miniTestPreferences: com.spanishapp.data.prefs.MiniTestPreferences,
    private val uiSound: com.spanishapp.service.UiSoundPlayer,
    private val subscriptionManager: com.spanishapp.service.SubscriptionManager,
    // v1.26.1: стадия «Произнеси» в квизе Слова дня.
    private val stt: com.spanishapp.service.SpanishSpeechRecognizer,
    // v1.26.1: RU-перевод примера для стадии «Фраза» (тот же переводчик, что
    // long-press в книгах).
    private val translator: com.spanishapp.data.repository.GeminiTranslator,
) : ViewModel() {

    /** Кэш переводов примеров: wordId → русский перевод (null не кэшируем). */
    private val exampleRuCache = mutableMapOf<Int, String>()

    /**
     * v1.26.1: перевод примера на русский для стадии «Фраза». Сначала юзер
     * видит/слышит фразу на родном языке, потом собирает испанскую. Offline
     * или ошибка → null (UI показывает fallback-подсказку со словом).
     */
    suspend fun translateExampleRu(wordId: Int, example: String): String? {
        exampleRuCache[wordId]?.let { return it }
        val ru = runCatching { translator.translateSentence(example) }
            .getOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        if (ru != null) exampleRuCache[wordId] = ru
        return ru
    }

    /** v1.23.0: PRO state — для показа/скрытия pro-bento promo-карточки. */
    val isPro: StateFlow<Boolean> = subscriptionManager.isProActive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** v1.26.1: живой текст распознавания для стадии «Произнеси» (WoD-квиз). */
    val sttPartial: StateFlow<String> = stt.partialText

    /**
     * v1.26.1: слушает микрофон и оценивает произнесённое слово против [target] —
     * та же фонетическая оценка, что на экране «Произношение» (все альтернативы
     * распознавания + biasing-подсказка).
     */
    suspend fun listenAndScorePronunciation(target: String): WodSpeakResult {
        return when (val r = stt.listenOnce(biasStrings = listOf(target))) {
            is com.spanishapp.service.SpeechResult.Success -> {
                val candidates = (r.alternatives.map { it.first } + r.text).distinct()
                val best = candidates.maxOf {
                    com.spanishapp.ui.pronunciation.pronunciationScore(it, target)
                }
                WodSpeakResult(score = best)
            }
            is com.spanishapp.service.SpeechResult.Error -> WodSpeakResult(
                error = when {
                    r.isSilence -> "Не слышу — нажми и говори"
                    r.message == "no_match" -> "Не разобрал. Попробуй ещё раз"
                    else -> r.message
                }
            )
            com.spanishapp.service.SpeechResult.Cancelled -> WodSpeakResult()
        }
    }

    /** Snapshot of mini-test ids the user has passed. Drives ✅ badges in
     * the lesson list (CourseDetailScreen). */
    val passedMiniTestIds: StateFlow<Set<String>> =
        miniTestPreferences.passedIds.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet(),
        )

    // ── UI State ──────────────────────────────────────────────
    val uiState: StateFlow<HomeUiState> = combine(
        userProgressDao.getProgress(),
        wordDao.getDueWords(),
        wordDao.learnedCount(),
        lessonDao.getNextLessons(),
        lessonProgressDao.getAllCompletedKeys(),
        authRepository.userPhotoUrl,
        dailyXpDao.observeSince(LocalDate.now().toString()),
        // v1.23.0: onStart{emit(false)} критично — без него combine
        // блокирует первое emission'а uiState до завершения disk-read'а
        // DataStore. Это вызывало ANR на холодном старте (Bug 1 audit).
        subscriptionManager.isProActive.onStart { emit(false) },
    ) { args ->
        val progress = args[0] as? UserProgressEntity
        val dueWords = args[1] as List<WordEntity>
        val learnedCount = args[2] as Int
        val nextLessons = args[3] as List<LessonEntity>
        val completedKeysList = args[4] as List<String>
        val photoUrl = args[5] as? String
        @Suppress("UNCHECKED_CAST")
        val todayXpRows = args[6] as List<com.spanishapp.data.db.entity.DailyXpEntity>
        val proActive = args[7] as Boolean

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

        val roadmapUnits = buildRoadmapUnits(completedKeys, proActive)

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
            wodStreak        = p.wodStreak,
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
                        val key = unit.lessons[idx].id ?: "u${unitId}_l${idx}"
                        key !in done
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
    //
    // v1.23.3 (audit Bug 22): раньше `flow { emit(...) }` эмитил ОДИН раз
    // на подписку. Если у нового юзера pool пустой при старте, weakSampleWord
    // оставался null навсегда в рамках сессии, даже когда юзер выучил слова
    // и pool наполнился. Теперь подписка на learnedCount() — каждое
    // изменение прогресса триггерит re-fetch pool'а.
    val weakSampleWord: StateFlow<WordEntity?> =
        wordDao.learnedCount()
            .map { wordDao.getPracticePool(5).randomOrNull() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
    val dailyGoals: StateFlow<DailyGoals> = run {
        val todayStart = LocalDate.now().atStartOfDay()
            .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        combine(
            flashcardSetProgressDao.observeAll(),
            libroProgressDao.getAll(),
            flow {
                val today = LocalDate.now().toString()
                emit(dailyWordDao.getForDate(today)?.wasPracticed == true)
            },
            radioListeningDao.observeSecondsSince(todayStart),
        ) { sets, libros, wodPracticed, radioSecondsToday ->
            val setDoneToday = sets.any { it.completedAt >= todayStart && it.stars > 0 }
            val libroToday   = libros.any { it.completedAt >= todayStart }
            val lessonToday  = lessonProgressDao.anyCompletedSince(todayStart)
            DailyGoals(
                lessonCompleted       = lessonToday,
                flashcardSetCompleted = setDoneToday,
                bookPageRead          = libroToday,
                wordOfDaySolved       = wodPracticed,
                radioListened         = radioSecondsToday >= 300,  // 5 минут
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyGoals())
    }

    private fun buildRoadmapUnits(completedKeys: Set<String>, proActive: Boolean): List<RoadmapUnit> {
        // v1.23.0: gate A2+ контента по PRO. Для free A2/B1/B2 блоки
        // показываются с замочком, тап на них ведёт в paywall.
        return RoadmapData.units.map { unit ->
            val unitId = unit.id.toIntOrNull()
            val isProGated = unit.cefrLevel != "A1" && !proActive

            // Non-numeric IDs = A2/B1/B2 preview blocks (content not ready yet).
            // Show them as visually unlocked so the user can browse lesson titles,
            // but don't track real progress yet.
            if (unitId == null) {
                return@map unit.copy(
                    isLocked = isProGated,
                    progress = 0f,
                    lessons  = unit.lessons.map { it.copy(isCompleted = false) }
                )
            }

            // A1 — всегда разблокировано. A2/B1/B2 — только если PRO.
            val unlocked = !isProGated

            val lessonsWithProgress = unit.lessons.mapIndexed { idx, lesson ->
                val key = lesson.id ?: "u${unitId}_l${idx}"
                lesson.copy(isCompleted = key in completedKeys)
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

    /**
     * v1.17.8 (fix: WoD пропадал с главной после смены даты):
     * - Раньше `flow { emit() }` читал DailyWord **один раз** при subscribe.
     *   Если seedDailyWord() ещё не закончился → emit null → карточка
     *   скрыта НАВСЕГДА в этом lifecycle (даже после insert).
     * - Также после смены даты (юзер не закрывал app) WoD не пересоздавался,
     *   потому что seedIfNeeded() вызывается только в SpanishApp.onCreate.
     *
     * Новая реализация:
     *  1. ensureDailyWordExists() в init — создаёт WoD на сегодня если нет.
     *     Гарантирует наличие при любом возврате юзера на главную.
     *  2. Reactive Flow через observeForDate(today) — UI обновляется
     *     автоматически когда seeder/ensure вставит запись.
     */
    init {
        viewModelScope.launch {
            ensureDailyWordExists()
        }
    }

    private suspend fun ensureDailyWordExists() {
        val today = LocalDate.now().toString()
        if (dailyWordDao.getForDate(today) != null) return
        val a1Ids = wordDao.getA1WordIds()
        if (a1Ids.isEmpty()) return
        val wordId = a1Ids[LocalDate.now().dayOfYear % a1Ids.size]
        dailyWordDao.upsert(
            com.spanishapp.data.db.entity.DailyWordEntity(
                date = today,
                wordId = wordId
            )
        )
    }

    val wordOfTheDay: StateFlow<WordOfDay?> = dailyWordDao
        .observeForDate(LocalDate.now().toString())
        .map { daily ->
            if (daily == null) return@map null
            val word = wordDao.getById(daily.wordId) ?: return@map null
            WordOfDay(
                spanish      = word.spanish,
                russian      = word.russian,
                example      = word.example,
                wasPracticed = daily.wasPracticed,
                wordId       = word.id,
                level        = word.level,
                category     = word.category
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Mark today's WoD as practised — called once after the user finishes the quiz flow.
     *
     * Side-effects:
     *   1. daily_words.was_practiced = 1
     *   2. INSERT into wod_history (для коллекции «Слов дня» и стат-виджета)
     *   3. Пересчёт wod_streak:
     *        — сегодня уже отмечено → ничего не трогаем (idempotent на повторный заход)
     *        — последняя отметка была вчера → streak + 1
     *        — иначе → streak = 1 (новая серия)
     */
    fun markWordOfDayPractised() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayStr = today.toString()

            dailyWordDao.markPracticed(todayStr)

            val word = wordOfTheDay.value ?: return@launch
            val progress = userProgressDao.getProgressOnce() ?: return@launch

            val lastDate = if (progress.wodLastDate > 0L) {
                java.time.Instant.ofEpochMilli(progress.wodLastDate)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
            } else null

            // Idempotent: повторный заход в течение того же дня не должен ни писать в историю,
            // ни инкрементить серию.
            if (lastDate == today) return@launch

            // История — отдельный insert: 1 запись = 1 закрепление в день.
            wodHistoryDao.insert(
                com.spanishapp.data.db.entity.WodHistoryEntity(
                    wordId  = word.wordId,
                    spanish = word.spanish,
                    russian = word.russian,
                    level   = word.level,
                )
            )

            val newStreak = when {
                lastDate == null                           -> 1
                lastDate == today.minusDays(1)             -> progress.wodStreak + 1
                else                                       -> 1
            }

            userProgressDao.updateWodStreak(
                streak     = newStreak,
                lastDateMs = System.currentTimeMillis()
            )

            // v1.22.16: XP за первое закрепление WoD сегодня. Раньше WoD
            // влиял только на streak, без XP — закрепил → нет вознаграждения.
            xpTracker.add(xp = XpSystem.WOD_FIRST_TODAY, words = 0)

            // Точечное повторение через 1 час — пик кривой забывания.
            com.spanishapp.service.WoDReminderWorker.scheduleInOneHour(
                context = appContext,
                spanish = word.spanish,
                russian = word.russian,
            )

            // Analytics: первое успешное закрепление за сегодня.
            com.spanishapp.service.Analytics.wodCompleted(level = word.level)
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

            // Analytics: серия прервалась — старый стрик упал до 1 (не +1).
            // Это самый ценный сигнал: показывает в какой день недели юзеры
            // перестают заниматься (можно адаптировать пуш-уведомления).
            if (newStreak == 1 && p.currentStreak > 1) {
                com.spanishapp.service.Analytics.streakLost(daysWas = p.currentStreak)
            }

            userProgressDao.update(
                p.copy(
                    currentStreak = newStreak,
                    longestStreak = maxOf(p.longestStreak, newStreak),
                    lastStudyDate = System.currentTimeMillis(),
                    totalXp       = p.totalXp + bonus
                )
            )

            // SFX: streak вырос (+1 день) — короткий «огонь». Бьём один
            // раз в день при первом входе в сессию. Не бьём при потере
            // (newStreak==1 & old>1) — это grief, не reward.
            if (newStreak > p.currentStreak) {
                uiSound.play(com.spanishapp.service.UiSoundPlayer.Sound.STREAK)
            }

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
    val skillRating: Int = 0,   // v1.1.0: старт с 0 (было 1000)
    val currentLeague: Int = 1,
    val wodStreak: Int = 0,
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

/** Five-checkbox daily mission used by the bento "Цель дня" tile. */
data class DailyGoals(
    val lessonCompleted: Boolean = false,           // closed a roadmap lesson today
    val flashcardSetCompleted: Boolean = false,
    val bookPageRead: Boolean = false,
    val wordOfDaySolved: Boolean = false,
    val radioListened: Boolean = false,             // listened to radio 5+ min today
) {
    val completedCount: Int get() =
        listOf(lessonCompleted, flashcardSetCompleted, bookPageRead, wordOfDaySolved, radioListened).count { it }
    val total: Int = 5
    val allDone: Boolean get() = completedCount == total

    /** First unfinished goal — drives the smart "tap to do next" navigation. */
    fun nextRoute(): String? = when {
        !lessonCompleted        -> "course_detail/A1"
        !flashcardSetCompleted  -> "flashcards"
        !bookPageRead           -> "game_libros"
        !wordOfDaySolved        -> "home"
        !radioListened          -> "radio"
        else                    -> null
    }
}
