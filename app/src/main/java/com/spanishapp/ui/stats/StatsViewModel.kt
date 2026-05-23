package com.spanishapp.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.*
import com.spanishapp.data.db.entity.*
import com.spanishapp.data.prefs.StatsPreferences
import com.spanishapp.radio.data.RadioListeningDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════
//  PERIOD
// ═══════════════════════════════════════════════════════════
enum class StatsPeriod { DAY, WEEK, MONTH, M3, M6, YEAR }

internal fun StatsPeriod.startDate(today: LocalDate): LocalDate = when (this) {
    StatsPeriod.DAY   -> today
    StatsPeriod.WEEK  -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    StatsPeriod.MONTH -> today.withDayOfMonth(1)
    StatsPeriod.M3    -> today.minusDays(89)
    StatsPeriod.M6    -> today.minusDays(179)
    StatsPeriod.YEAR  -> today.minusDays(364)
}

internal fun StatsPeriod.lengthDays(today: LocalDate): Int = when (this) {
    StatsPeriod.DAY   -> 1
    StatsPeriod.WEEK  -> 7
    StatsPeriod.MONTH -> today.lengthOfMonth()
    StatsPeriod.M3    -> 90
    StatsPeriod.M6    -> 180
    StatsPeriod.YEAR  -> 365
}

internal fun LocalDate.toMillisStartOfDay(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

// ═══════════════════════════════════════════════════════════
//  UI STATE
// ═══════════════════════════════════════════════════════════
data class StatsUi(
    val loading: Boolean = true,
    val period: StatsPeriod = StatsPeriod.WEEK,
    val periodLabel: String = "",
    // Hero
    val totalXp: Int = 0,
    val totalMinutes: Int = 0,
    val activeDays: Int = 0,
    val periodLengthDays: Int = 7,
    val deltaPct: Int = 0,
    val deltaXp: Int = 0,
    // Rings (goal targets)
    val xpGoal: Int = 1,
    val minutesGoal: Int = 1,
    // Series (day-level points)
    val series: List<DayPoint> = emptyList(),
    val bestDay: DayPoint? = null,
    val worstDay: DayPoint? = null,
    // Breakdown
    val breakdown: ActivityBreakdown = ActivityBreakdown(),
    // Mistakes & weak words
    val topMistakes: List<MistakeRow> = emptyList(),
    val topWeak: List<WeakRow> = emptyList(),
    // Progress
    val totalXpAllTime: Int = 0,
    val lessonsCompleted: Int = 0,
    val lessonsTotal: Int = 254,
    val lessonsDelta: Int = 0,
    val wordsLearned: Int = 0,
    val wordsInProgress: Int = 0,
    val wordsUntouched: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val skillRating: Int = 0,
    val peakSkillRating: Int = 0,
    val leagueIndex: Int = 0,
    val nextLeagueAt: Int = 0,
    val newAchievements: List<AchRow> = emptyList(),
    val insightText: String = "",
)

data class DayPoint(
    val date: LocalDate,
    val xp: Int,
    val minutes: Int,
)

data class ActivityBreakdown(
    val lessonsMin: Int = 0,
    val lessonsCount: Int = 0,
    val flashcardsMin: Int = 0,
    val flashcardsCount: Int = 0,
    val flashcardsAvgPct: Int = 0,
    val gamesMin: Int = 0,
    val gamesLevels: Int = 0,
    val radioMin: Int = 0,
    val booksMin: Int = 0,
    val booksCount: Int = 0,
    val chatMin: Int = 0,
    val chatMessages: Int = 0,
) {
    val totalMin: Int get() = lessonsMin + flashcardsMin + gamesMin + radioMin + booksMin + chatMin
    fun pct(value: Int): Int = if (totalMin <= 0) 0 else (value * 100 / totalMin)
}

data class MistakeRow(
    val itemMain: String,
    val itemHint: String,
    val attempts: Int,
)

data class WeakRow(
    val spanish: String,
    val russian: String,
)

data class AchRow(
    val id: String,
    val title: String,
    val description: String,
    val medal: String,
)

// ═══════════════════════════════════════════════════════════
//  VIEW MODEL
// ═══════════════════════════════════════════════════════════
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsPrefs: StatsPreferences,
    private val userProgressDao: UserProgressDao,
    private val dailyXpDao: DailyXpDao,
    private val lessonProgressDao: LessonProgressDao,
    private val lessonCompletionHistoryDao: LessonCompletionHistoryDao,
    private val libroProgressDao: LibroProgressDao,
    private val flashcardSetProgressDao: FlashcardSetProgressDao,
    private val gameLevelProgressDao: GameLevelProgressDao,
    private val gameMistakesDao: GameMistakesDao,
    private val wordDao: WordDao,
    private val achievementDao: AchievementDao,
    private val radioListeningDao: RadioListeningDao,
    private val chatMessageDao: ChatMessageDao,
    private val activityTimeLogDao: com.spanishapp.data.db.dao.ActivityTimeLogDao,
) : ViewModel() {

    private val periodFlow: StateFlow<StatsPeriod> = statsPrefs.period
        .map { runCatching { StatsPeriod.valueOf(it) }.getOrDefault(StatsPeriod.WEEK) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, StatsPeriod.WEEK)

    fun setPeriod(p: StatsPeriod) {
        viewModelScope.launch { statsPrefs.setPeriod(p.name) }
    }

    val state: StateFlow<StatsUi> = periodFlow.flatMapLatest { period ->
        val today = LocalDate.now()
        val periodStart = period.startDate(today)
        val periodStartMs = periodStart.toMillisStartOfDay()
        val periodLen = period.lengthDays(today)
        val prevStart = periodStart.minusDays(periodLen.toLong())

        // 12 источников — Flow<*>. Кладём через combine с массивом.
        combine(
            listOf(
                userProgressDao.getProgress(),
                dailyXpDao.observeAll(),
                lessonProgressDao.getAllCompletedKeys(),
                // ⚠ Считаем повторы — каждое прохождение урока (даже повторное)
                // имеет свою строку в lesson_completion_history. Это даёт честный
                // breakdown.lessonsCount «за период», включая review-сессии.
                lessonCompletionHistoryDao.observeCountSince(periodStartMs),
                gameLevelProgressDao.observeCountSince(periodStartMs),
                chatMessageDao.observeCountSince(periodStartMs),
                libroProgressDao.getAll(),
                flashcardSetProgressDao.observeAll(),
                wordDao.learnedCount(),
                wordDao.inProgressCount(),
                wordDao.untouchedCount(),
                wordDao.getWeakWords(),
                achievementDao.getAll(),
                radioListeningDao.observeSecondsSince(periodStartMs),
                topMistakesFlow(periodStartMs),
                // Реальные минуты per-activity из activity_time_log (v27).
                // Заменили эмпирику lessonsCount*7/setsCount*5/etc. на честный SUM.
                activityTimeLogDao.observeMinutesSince("LESSON", periodStartMs),
                activityTimeLogDao.observeMinutesSince("FLASHCARDS", periodStartMs),
                activityTimeLogDao.observeMinutesSince("GAME", periodStartMs),
                activityTimeLogDao.observeMinutesSince("BOOK", periodStartMs),
                activityTimeLogDao.observeMinutesSince("CHAT", periodStartMs),
            )
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val progress         = args[0]  as? UserProgressEntity
            @Suppress("UNCHECKED_CAST")
            val dailyXp          = args[1]  as List<DailyXpEntity>
            @Suppress("UNCHECKED_CAST")
            val lessonKeys       = args[2]  as List<String>
            val lessonsInPeriod  = args[3]  as Int
            val gameLevelsPeriod = args[4]  as Int
            val chatMsgsPeriod   = args[5]  as Int
            @Suppress("UNCHECKED_CAST")
            val libros           = args[6]  as List<LibroProgressEntity>
            @Suppress("UNCHECKED_CAST")
            val flashSets        = args[7]  as List<FlashcardSetProgressEntity>
            val learnedCount     = args[8]  as Int
            val inProgress       = args[9]  as Int
            val untouched        = args[10] as Int
            @Suppress("UNCHECKED_CAST")
            val weak             = args[11] as List<WordEntity>
            @Suppress("UNCHECKED_CAST")
            val achievements     = args[12] as List<AchievementEntity>
            val radioSecs        = args[13] as Long
            @Suppress("UNCHECKED_CAST")
            val mistakes         = args[14] as List<MistakeRow>
            val lessonMin        = (args[15] as Long).toInt()
            val flashcardsMin    = (args[16] as Long).toInt()
            val gameMin          = (args[17] as Long).toInt()
            val bookMin          = (args[18] as Long).toInt()
            val chatMin          = (args[19] as Long).toInt()

            buildUi(
                period, today, periodStart, periodStartMs, periodLen, prevStart,
                progress, dailyXp, lessonKeys, lessonsInPeriod, gameLevelsPeriod,
                chatMsgsPeriod, libros, flashSets, learnedCount, inProgress, untouched,
                weak, achievements, radioSecs, mistakes,
                lessonMin, flashcardsMin, gameMin, bookMin, chatMin,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUi())

    /**
     * Топ-5 ошибок по всем 4 играм, не старше указанного момента.
     * Реактивно: подписан на observeAll каждой игры — любая новая ошибка
     * сразу пере-эмитит топ без поллинга.
     */
    private fun topMistakesFlow(since: Long): Flow<List<MistakeRow>> = combine(
        gameMistakesDao.observeAll("articles"),
        gameMistakesDao.observeAll("speed"),
        gameMistakesDao.observeAll("palabra_maestra"),
        gameMistakesDao.observeAll("math"),
    ) { a, b, c, d ->
        (a + b + c + d)
            .filter { it.lastSeenAt >= since }
            .sortedByDescending { it.attempts }
            .take(5)
            .map { MistakeRow(it.displayMain.ifBlank { it.itemId }, it.displayHint, it.attempts) }
    }
}

// ═══════════════════════════════════════════════════════════
//  Pure builder — собирает StatsUi из всех источников.
//  Вынесен из ViewModel чтобы можно было покрыть unit-тестом.
// ═══════════════════════════════════════════════════════════
@Suppress("LongParameterList")
internal fun buildUi(
    period: StatsPeriod,
    today: LocalDate,
    periodStart: LocalDate,
    periodStartMs: Long,
    periodLen: Int,
    prevStart: LocalDate,
    progress: UserProgressEntity?,
    dailyXp: List<DailyXpEntity>,
    lessonKeys: List<String>,
    lessonsInPeriod: Int,
    gameLevelsPeriod: Int,
    chatMsgsPeriod: Int,
    libros: List<LibroProgressEntity>,
    flashSets: List<FlashcardSetProgressEntity>,
    learnedCount: Int,
    wordsInProgress: Int,
    wordsUntouched: Int,
    weak: List<WordEntity>,
    achievements: List<AchievementEntity>,
    radioSecs: Long,
    mistakes: List<MistakeRow>,
    // Реальные минуты per-activity из activity_time_log (v27).
    // Если 0 — экран был открыт меньше 5 сек (фильтр в TrackActivity).
    lessonMin: Int = 0,
    flashcardsMin: Int = 0,
    gameMin: Int = 0,
    bookMin: Int = 0,
    chatMin: Int = 0,
): StatsUi {
    val byDay = dailyXp.associateBy { it.day }
    val series = (0 until periodLen).map { offset ->
        val date = periodStart.plusDays(offset.toLong())
        if (date.isAfter(today)) {
            DayPoint(date, 0, 0)
        } else {
            val row = byDay[date.toString()]
            DayPoint(date, row?.xp ?: 0, row?.minutes ?: 0)
        }
    }

    val totalXp = series.sumOf { it.xp }
    val totalMin = series.sumOf { it.minutes }
    val activeDays = series.count { it.xp > 0 || it.minutes > 0 }

    val prevRows = dailyXp.filter {
        val d = runCatching { LocalDate.parse(it.day) }.getOrNull() ?: return@filter false
        !d.isBefore(prevStart) && d.isBefore(periodStart)
    }
    val prevTotalXp = prevRows.sumOf { it.xp }
    val deltaPct = if (prevTotalXp <= 0) {
        if (totalXp > 0) 100 else 0
    } else {
        ((totalXp - prevTotalXp) * 100 / prevTotalXp)
    }

    val bestDay = series.maxByOrNull { it.xp }?.takeIf { it.xp > 0 }
    val worstDay = if (period == StatsPeriod.WEEK)
        series.filter { !it.date.isAfter(today) }.minByOrNull { it.xp }
    else null

    val dailyGoalMin = progress?.dailyGoalMinutes ?: 10
    val minutesGoal = (dailyGoalMin * periodLen).coerceAtLeast(1)
    val xpGoal = (minutesGoal * 9).coerceAtLeast(1)

    // ── Breakdown (минуты по активностям) ──
    // ⚠ Минуты per-activity берём из РЕАЛЬНЫХ таймеров activity_time_log
    // (v27) — каждый учебный экран пишет (started_at, ended_at) при выходе.
    // Эмпирический baseline lessonsCount*7 / setsCount*5 удалён —
    // теперь breakdown точен до секунды.
    val setsInPeriod = flashSets.filter { it.completedAt >= periodStartMs }
    val avgPct = if (setsInPeriod.isEmpty()) 0 else setsInPeriod.sumOf { it.bestPercent } / setsInPeriod.size
    val librosInPeriod = libros.count { it.completedAt >= periodStartMs }
    val radioMin = (radioSecs / 60).toInt()

    val breakdown = ActivityBreakdown(
        lessonsMin       = lessonMin,
        lessonsCount     = lessonsInPeriod,
        flashcardsMin    = flashcardsMin,
        flashcardsCount  = setsInPeriod.size,
        flashcardsAvgPct = avgPct,
        gamesMin         = gameMin,
        gamesLevels      = gameLevelsPeriod,
        radioMin         = radioMin,
        booksMin         = bookMin,
        booksCount       = librosInPeriod,
        chatMin          = chatMin,
        chatMessages     = chatMsgsPeriod,
    )

    val topWeak = weak.take(5).map { WeakRow(it.spanish, it.russian) }

    val newAch = achievements.filter { it.isUnlocked && it.unlockedAt >= periodStartMs }
        .sortedByDescending { it.unlockedAt }
        .take(6)
        .map { ach ->
            val medal = when {
                ach.xpReward >= 200 -> "🥇"
                ach.xpReward >= 100 -> "🥈"
                else                 -> "🥉"
            }
            AchRow(ach.id, ach.titleRu, ach.descriptionRu, medal)
        }

    val rating = progress?.skillRating ?: 0
    val insight = buildInsight(period, series, bestDay, worstDay, totalXp, deltaPct)
    val label = formatPeriodLabel(period, periodStart, today)

    return StatsUi(
        loading = false,
        period = period,
        periodLabel = label,
        totalXp = totalXp,
        totalMinutes = totalMin,
        activeDays = activeDays,
        periodLengthDays = periodLen,
        deltaPct = deltaPct,
        deltaXp = totalXp - prevTotalXp,
        xpGoal = xpGoal,
        minutesGoal = minutesGoal,
        series = series,
        bestDay = bestDay,
        worstDay = worstDay,
        breakdown = breakdown,
        topMistakes = mistakes,
        topWeak = topWeak,
        totalXpAllTime = progress?.totalXp ?: 0,
        lessonsCompleted = lessonKeys.size,
        lessonsDelta = lessonsInPeriod,
        wordsLearned = learnedCount,
        wordsInProgress = wordsInProgress,
        wordsUntouched = wordsUntouched,
        currentStreak = progress?.currentStreak ?: 0,
        longestStreak = progress?.longestStreak ?: 0,
        skillRating = rating,
        peakSkillRating = progress?.peakSkillRating ?: 0,
        leagueIndex = leagueIndexFor(rating),
        nextLeagueAt = nextLeagueThreshold(rating),
        newAchievements = newAch,
        insightText = insight,
    )
}

// ═══════════════════════════════════════════════════════════
//  Pure helpers
// ═══════════════════════════════════════════════════════════
internal fun leagueIndexFor(rating: Int): Int = when {
    rating < 100   -> 0
    rating < 300   -> 1
    rating < 600   -> 2
    rating < 1000  -> 3
    rating < 1500  -> 4
    rating < 2100  -> 5
    rating < 2800  -> 6
    else           -> 7
}

internal fun nextLeagueThreshold(rating: Int): Int = when {
    rating < 100   -> 100
    rating < 300   -> 300
    rating < 600   -> 600
    rating < 1000  -> 1000
    rating < 1500  -> 1500
    rating < 2100  -> 2100
    rating < 2800  -> 2800
    else           -> rating
}

internal val LEAGUE_NAMES_RU = listOf(
    "Aldea perdida", "Santiago de Compostela", "Bilbao", "Zaragoza",
    "Valencia", "Sevilla", "Barcelona", "Madrid",
)

private fun formatPeriodLabel(period: StatsPeriod, start: LocalDate, today: LocalDate): String =
    when (period) {
        StatsPeriod.DAY   -> "${start.dayOfMonth} ${monthShort(start.monthValue)}"
        StatsPeriod.WEEK  -> {
            val end = start.plusDays(6)
            if (start.monthValue == end.monthValue) "${start.dayOfMonth}–${end.dayOfMonth} ${monthShort(start.monthValue)}"
            else "${start.dayOfMonth} ${monthShort(start.monthValue)} – ${end.dayOfMonth} ${monthShort(end.monthValue)}"
        }
        StatsPeriod.MONTH -> "${monthShort(start.monthValue).replaceFirstChar { it.uppercase() }} ${start.year}"
        StatsPeriod.M3    -> "${monthShort(start.monthValue)} – ${monthShort(today.monthValue)} ${today.year}"
        StatsPeriod.M6    -> "${monthShort(start.monthValue)} – ${monthShort(today.monthValue)} ${today.year}"
        StatsPeriod.YEAR  -> "${monthShort(start.monthValue)} ${start.year} – ${monthShort(today.monthValue)} ${today.year}"
    }

private fun monthShort(m: Int): String = when (m) {
    1 -> "янв"; 2 -> "фев"; 3 -> "мар"; 4 -> "апр"; 5 -> "май"; 6 -> "июн"
    7 -> "июл"; 8 -> "авг"; 9 -> "сен"; 10 -> "окт"; 11 -> "ноя"; 12 -> "дек"
    else -> ""
}

internal fun buildInsight(
    period: StatsPeriod,
    series: List<DayPoint>,
    best: DayPoint?,
    worst: DayPoint?,
    totalXp: Int,
    deltaPct: Int,
): String {
    if (series.isEmpty() || totalXp == 0) {
        return "Начни заниматься — здесь появится твоя статистика и подсказки."
    }
    val today = LocalDate.now()
    val missed = series.count { it.xp == 0 && !it.date.isAfter(today) }
    return when {
        period == StatsPeriod.WEEK && worst != null && worst.xp == 0 ->
            "Ты пропустил ${dayShortRu(worst.date.dayOfWeek)}. Лучший день — ${best?.let { dayShortRu(it.date.dayOfWeek) } ?: "—"}, ${best?.xp ?: 0} XP. Так держать!"
        deltaPct >= 10 ->
            "Прогресс растёт: +$deltaPct% к прошлому периоду. Продолжай в том же темпе!"
        deltaPct <= -10 ->
            "Темп просел на ${-deltaPct}%. Попробуй вернуться к ежедневным 10 минутам."
        missed == 0 ->
            "Стабильно без пропусков. Топ-форма!"
        else ->
            "За период: $totalXp XP, ${series.count { it.xp > 0 }} активных дней. Двигайся дальше!"
    }
}

private fun dayShortRu(d: DayOfWeek): String = when (d) {
    DayOfWeek.MONDAY -> "понедельник"
    DayOfWeek.TUESDAY -> "вторник"
    DayOfWeek.WEDNESDAY -> "среду"
    DayOfWeek.THURSDAY -> "четверг"
    DayOfWeek.FRIDAY -> "пятницу"
    DayOfWeek.SATURDAY -> "субботу"
    DayOfWeek.SUNDAY -> "воскресенье"
}
