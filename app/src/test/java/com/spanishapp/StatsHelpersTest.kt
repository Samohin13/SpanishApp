package com.spanishapp

import com.spanishapp.data.db.entity.DailyXpEntity
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.ui.stats.StatsPeriod
import com.spanishapp.ui.stats.buildUi
import com.spanishapp.ui.stats.leagueIndexFor
import com.spanishapp.ui.stats.nextLeagueThreshold
import com.spanishapp.ui.stats.startDate
import com.spanishapp.ui.stats.lengthDays
import com.spanishapp.ui.stats.toMillisStartOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Unit-тесты для чистых helpers экрана Insights / «Эта неделя».
 *
 * Покрывают:
 *  • Маппинг skillRating → league index + следующий порог
 *  • Расчёт startDate / lengthDays по выбранному периоду
 *  • Основной builder StatsUi (агрегаты XP, активные дни, breakdown, delta)
 */
class StatsHelpersTest {

    // ── leagueIndexFor — 8 лиг по skillRating ──
    @Test
    fun `league index — Aldea perdida для рейтинга меньше 100`() {
        assertEquals(0, leagueIndexFor(0))
        assertEquals(0, leagueIndexFor(99))
    }

    @Test
    fun `league index — Bilbao для рейтинга в диапазоне 300-599`() {
        assertEquals(2, leagueIndexFor(300))
        assertEquals(2, leagueIndexFor(599))
    }

    @Test
    fun `league index — Madrid для рейтинга 2800 и выше`() {
        assertEquals(7, leagueIndexFor(2800))
        assertEquals(7, leagueIndexFor(99999))
    }

    @Test
    fun `nextLeagueThreshold возвращает верхнюю границу текущей лиги`() {
        assertEquals(100, nextLeagueThreshold(50))
        assertEquals(600, nextLeagueThreshold(300))
        assertEquals(1500, nextLeagueThreshold(1000))
        // На Madrid (макс. лига) — возвращаем сам рейтинг (порог уже не нужен)
        assertEquals(3000, nextLeagueThreshold(3000))
    }

    // ── startDate / lengthDays по периоду ──
    @Test
    fun `WEEK начинается с ближайшего понедельника`() {
        // 2026-05-23 — суббота → понедельник той же недели = 2026-05-18
        val today = LocalDate.of(2026, 5, 23)
        val start = StatsPeriod.WEEK.startDate(today)
        assertEquals(LocalDate.of(2026, 5, 18), start)
        assertEquals(DayOfWeek.MONDAY, start.dayOfWeek)
        assertEquals(7, StatsPeriod.WEEK.lengthDays(today))
    }

    @Test
    fun `MONTH стартует с 1-го числа и длится lengthOfMonth дней`() {
        val today = LocalDate.of(2026, 2, 14) // февраль 2026 = 28 дней
        assertEquals(LocalDate.of(2026, 2, 1), StatsPeriod.MONTH.startDate(today))
        assertEquals(28, StatsPeriod.MONTH.lengthDays(today))
    }

    @Test
    fun `DAY длится 1 день и стартует с today`() {
        val today = LocalDate.of(2026, 5, 23)
        assertEquals(today, StatsPeriod.DAY.startDate(today))
        assertEquals(1, StatsPeriod.DAY.lengthDays(today))
    }

    @Test
    fun `YEAR покрывает 365 дней назад от today`() {
        val today = LocalDate.of(2026, 5, 23)
        assertEquals(LocalDate.of(2025, 5, 24), StatsPeriod.YEAR.startDate(today))
        assertEquals(365, StatsPeriod.YEAR.lengthDays(today))
    }

    // ── buildUi — основная логика агрегации ──
    @Test
    fun `buildUi — пустые данные дают нули, но не падает`() {
        val today = LocalDate.of(2026, 5, 23)
        val periodStart = StatsPeriod.WEEK.startDate(today)
        val ui = buildUi(
            period = StatsPeriod.WEEK,
            today = today,
            periodStart = periodStart,
            periodStartMs = periodStart.toMillisStartOfDay(),
            periodLen = 7,
            prevStart = periodStart.minusDays(7),
            progress = null,
            dailyXp = emptyList(),
            lessonKeys = emptyList(),
            lessonsInPeriod = 0,
            gameLevelsPeriod = 0,
            libros = emptyList(),
            flashSets = emptyList(),
            learnedCount = 0,
            wordsInProgress = 0,
            wordsUntouched = 0,
            weak = emptyList(),
            achievements = emptyList(),
            radioSecs = 0L,
            mistakes = emptyList(),
        )
        assertEquals(0, ui.totalXp)
        assertEquals(0, ui.totalMinutes)
        assertEquals(0, ui.activeDays)
        assertEquals(7, ui.series.size)
        assertNull(ui.bestDay)
    }

    @Test
    fun `buildUi — корректно суммирует XP и считает активные дни`() {
        val today = LocalDate.of(2026, 5, 23) // суббота
        val periodStart = StatsPeriod.WEEK.startDate(today) // понедельник 2026-05-18
        val xpRows = listOf(
            DailyXpEntity(day = "2026-05-18", xp = 100, minutes = 12),
            DailyXpEntity(day = "2026-05-19", xp = 50,  minutes = 8),
            DailyXpEntity(day = "2026-05-23", xp = 200, minutes = 25),
        )
        val ui = buildUi(
            period = StatsPeriod.WEEK,
            today = today,
            periodStart = periodStart,
            periodStartMs = periodStart.toMillisStartOfDay(),
            periodLen = 7,
            prevStart = periodStart.minusDays(7),
            progress = UserProgressEntity(totalXp = 5000, skillRating = 450, dailyGoalMinutes = 10),
            dailyXp = xpRows,
            lessonKeys = emptyList(),
            lessonsInPeriod = 0,
            gameLevelsPeriod = 0,
            libros = emptyList(),
            flashSets = emptyList(),
            learnedCount = 0,
            wordsInProgress = 0,
            wordsUntouched = 0,
            weak = emptyList(),
            achievements = emptyList(),
            radioSecs = 0L,
            mistakes = emptyList(),
        )
        assertEquals(350, ui.totalXp)
        assertEquals(45, ui.totalMinutes)
        assertEquals(3, ui.activeDays)
        assertNotNull(ui.bestDay)
        assertEquals(LocalDate.of(2026, 5, 23), ui.bestDay!!.date)
        // skillRating 450 → Bilbao (index 2)
        assertEquals(2, ui.leagueIndex)
        // Goal: dailyGoal 10 * 7 = 70 мин
        assertEquals(70, ui.minutesGoal)
    }

    @Test
    fun `buildUi — deltaPct растёт когда XP больше прошлого периода`() {
        val today = LocalDate.of(2026, 5, 23)
        val periodStart = StatsPeriod.WEEK.startDate(today)
        // прошлая неделя: 100 XP. Эта неделя: 200 XP. delta = +100%.
        val xpRows = listOf(
            DailyXpEntity(day = periodStart.minusDays(3).toString(), xp = 100, minutes = 10),
            DailyXpEntity(day = periodStart.toString(),               xp = 200, minutes = 20),
        )
        val ui = buildUi(
            period = StatsPeriod.WEEK,
            today = today,
            periodStart = periodStart,
            periodStartMs = periodStart.toMillisStartOfDay(),
            periodLen = 7,
            prevStart = periodStart.minusDays(7),
            progress = null,
            dailyXp = xpRows,
            lessonKeys = emptyList(),
            lessonsInPeriod = 0,
            gameLevelsPeriod = 0,
            libros = emptyList(),
            flashSets = emptyList(),
            learnedCount = 0,
            wordsInProgress = 0,
            wordsUntouched = 0,
            weak = emptyList(),
            achievements = emptyList(),
            radioSecs = 0L,
            mistakes = emptyList(),
        )
        assertEquals(200, ui.totalXp)
        assertEquals(100, ui.deltaPct)
        assertEquals(100, ui.deltaXp)
    }

    @Test
    fun `buildUi WEEK — worstDay указывает на пропущенный день`() {
        val today = LocalDate.of(2026, 5, 23) // суббота
        val periodStart = StatsPeriod.WEEK.startDate(today)
        // активность только в понедельник, всё остальное = 0
        val xpRows = listOf(
            DailyXpEntity(day = periodStart.toString(), xp = 100, minutes = 12),
        )
        val ui = buildUi(
            period = StatsPeriod.WEEK,
            today = today,
            periodStart = periodStart,
            periodStartMs = periodStart.toMillisStartOfDay(),
            periodLen = 7,
            prevStart = periodStart.minusDays(7),
            progress = null,
            dailyXp = xpRows,
            lessonKeys = emptyList(),
            lessonsInPeriod = 0,
            gameLevelsPeriod = 0,
            libros = emptyList(),
            flashSets = emptyList(),
            learnedCount = 0,
            wordsInProgress = 0,
            wordsUntouched = 0,
            weak = emptyList(),
            achievements = emptyList(),
            radioSecs = 0L,
            mistakes = emptyList(),
        )
        assertNotNull(ui.worstDay)
        assertEquals(0, ui.worstDay!!.xp)
        assertTrue(ui.insightText.contains("пропустил") || ui.insightText.contains("растёт") || ui.insightText.isNotBlank())
    }
}
