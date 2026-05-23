package com.spanishapp.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.time.DayOfWeek
import java.time.LocalDate

// ═══════════════════════════════════════════════════════════
//  PALETTE — строго из stats_screen.html
// ═══════════════════════════════════════════════════════════
private val BgRoot       = Color(0xFF05070B)
private val Bg           = Color(0xFF0B0D12)
private val SurfaceBg    = Color(0xFF161922)
private val Surface2     = Color(0xFF1F2330)
private val Surface3     = Color(0xFF2A2F3E)
private val LineCol      = Color(0xFF2D3344)
private val TextPri      = Color(0xFFF4F6FB)
private val TextDim      = Color(0xFF9AA3B7)
private val TextMute     = Color(0xFF6B7388)
private val OrangeC      = Color(0xFFFF8A3D)
private val BlueC        = Color(0xFF4EA1FF)
private val GreenC       = Color(0xFF4ADE80)
private val RedC         = Color(0xFFF87171)
private val YellowC      = Color(0xFFFACC15)
private val PurpleC      = Color(0xFFA78BFA)
private val PinkC        = Color(0xFFF472B6)

// ═══════════════════════════════════════════════════════════
//  ENTRY
// ═══════════════════════════════════════════════════════════
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(containerColor = Bg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            AppBar(
                title = "Инсайты",
                subtitle = ui.periodLabel,
                onBack = { navController.popBackStack() },
            )

            SegmentedPeriod(
                selected = ui.period,
                onSelect = viewModel::setPeriod,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                HeroCard(ui)
                Spacer(Modifier.height(14.dp))
                RingsCard(ui)
                Spacer(Modifier.height(8.dp))

                SectionTitle("📈 Динамика")
                ChartCard(ui)
                Spacer(Modifier.height(8.dp))

                SectionTitle("🎯 На что ушло время")
                BreakdownCard(ui.breakdown)
                Spacer(Modifier.height(8.dp))

                if (ui.topMistakes.isNotEmpty() || ui.topWeak.isNotEmpty()) {
                    SectionTitle("⚠️ Где ошибался чаще всего")
                    MistakesCard(ui.topMistakes, ui.topWeak)
                    Spacer(Modifier.height(8.dp))
                }

                SectionTitle("🚀 Прогресс")
                ProgressCard(ui)
                Spacer(Modifier.height(8.dp))

                if (ui.newAchievements.isNotEmpty()) {
                    SectionTitle("🏆 Новые ачивки")
                    AchRow(ui.newAchievements)
                    Spacer(Modifier.height(8.dp))
                }

                SectionTitle("💡 Подсказка")
                InsightCard(ui.insightText)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  APP BAR
// ═══════════════════════════════════════════════════════════
@Composable
private fun AppBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceBg)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPri)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPri, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = TextDim, fontSize = 12.sp)
            }
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = TextDim)
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  SEGMENTED CONTROL
// ═══════════════════════════════════════════════════════════
@Composable
private fun SegmentedPeriod(
    selected: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        StatsPeriod.DAY   to "День",
        StatsPeriod.WEEK  to "Неделя",
        StatsPeriod.MONTH to "Месяц",
        StatsPeriod.M3    to "3М",
        StatsPeriod.M6    to "6М",
        StatsPeriod.YEAR  to "Год",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items.forEach { (p, label) ->
            val active = p == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (active) Surface3 else Color.Transparent)
                    .clickable { onSelect(p) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = if (active) TextPri else TextMute,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  HERO
// ═══════════════════════════════════════════════════════════
@Composable
private fun HeroCard(ui: StatsUi) {
    val title = when (ui.period) {
        StatsPeriod.DAY   -> "XP сегодня"
        StatsPeriod.WEEK  -> "XP за неделю"
        StatsPeriod.MONTH -> "XP за месяц"
        StatsPeriod.M3    -> "XP за 3 месяца"
        StatsPeriod.M6    -> "XP за полгода"
        StatsPeriod.YEAR  -> "XP за год"
    }
    val deltaPositive = ui.deltaPct >= 0
    val deltaColor = if (deltaPositive) GreenC else RedC
    val deltaPrefix = if (deltaPositive) "↑ +" else "↓ "
    val deltaXpStr = if (deltaPositive) "+${ui.deltaXp}" else "${ui.deltaXp}"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1F2330), Color(0xFF2A2030)),
                    )
                )
                .padding(18.dp)
        ) {
            // Soft radial accent в правом-верхнем углу (как в HTML ::after)
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = OrangeC.copy(alpha = 0.20f),
                    radius = size.minDimension * 0.45f,
                    center = Offset(size.width + 20f, -20f),
                )
            }
            Column {
                Text(
                    title.uppercase(),
                    color = TextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        ui.totalXp.toString(),
                        color = TextPri,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("XP", color = TextDim, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${deltaPrefix}${if (deltaPositive) ui.deltaPct else -ui.deltaPct}% · $deltaXpStr vs прошл.",
                    color = deltaColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "${ui.totalMinutes} мин · ${ui.activeDays} из ${ui.periodLengthDays} активных дней",
                    color = TextMute,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  RINGS
// ═══════════════════════════════════════════════════════════
@Composable
private fun RingsCard(ui: StatsUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceBg,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val xpPct  = if (ui.xpGoal <= 0) 0f else (ui.totalXp.toFloat() / ui.xpGoal)
            val minPct = if (ui.minutesGoal <= 0) 0f else (ui.totalMinutes.toFloat() / ui.minutesGoal)
            val dayPct = if (ui.periodLengthDays <= 0) 0f else (ui.activeDays.toFloat() / ui.periodLengthDays)

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                ActivityRings(xpPct, minPct, dayPct)
                val mainPct = ((xpPct.coerceAtLeast(0f)) * 100).toInt()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$mainPct%", color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("цель", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LegendItem(OrangeC, "XP", "${ui.totalXp} / ${ui.xpGoal}")
                LegendItem(BlueC,   "Минуты", "${ui.totalMinutes} / ${ui.minutesGoal}")
                LegendItem(GreenC,  "Активные дни", "${ui.activeDays} / ${ui.periodLengthDays}")
            }
        }
    }
}

@Composable
private fun ActivityRings(xpPct: Float, minPct: Float, dayPct: Float) {
    Canvas(modifier = Modifier.size(120.dp)) {
        val stroke = 10.dp.toPx()
        val full = 360f
        // Outer (orange) — XP
        drawCircle(
            color = OrangeC.copy(alpha = 0.15f),
            radius = 50.dp.toPx(),
            style = Stroke(width = stroke),
        )
        drawArc(
            color = OrangeC,
            startAngle = -90f,
            sweepAngle = (xpPct.coerceAtMost(1f)) * full,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(100.dp.toPx(), 100.dp.toPx()),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - 50.dp.toPx(), center.y - 50.dp.toPx()
            ),
        )
        // Middle (blue) — Minutes
        drawCircle(
            color = BlueC.copy(alpha = 0.15f),
            radius = 38.dp.toPx(),
            style = Stroke(width = stroke),
        )
        drawArc(
            color = BlueC,
            startAngle = -90f,
            sweepAngle = (minPct.coerceAtMost(1f)) * full,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(76.dp.toPx(), 76.dp.toPx()),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - 38.dp.toPx(), center.y - 38.dp.toPx()
            ),
        )
        // Inner (green) — Active days
        drawCircle(
            color = GreenC.copy(alpha = 0.15f),
            radius = 26.dp.toPx(),
            style = Stroke(width = stroke),
        )
        drawArc(
            color = GreenC,
            startAngle = -90f,
            sweepAngle = (dayPct.coerceAtMost(1f)) * full,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(52.dp.toPx(), 52.dp.toPx()),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - 26.dp.toPx(), center.y - 26.dp.toPx()
            ),
        )
    }
}

@Composable
private fun LegendItem(dot: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(10.dp).clip(CircleShape).background(dot)
        )
        Spacer(Modifier.width(10.dp))
        Text(label, color = TextDim, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(value, color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

// ═══════════════════════════════════════════════════════════
//  CHART CARD — переключатель по периоду
// ═══════════════════════════════════════════════════════════
@Composable
private fun ChartCard(ui: StatsUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceBg,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (ui.period) {
                StatsPeriod.WEEK  -> WeekBarChart(ui.series)
                StatsPeriod.MONTH -> MonthCalendar(ui.series)
                StatsPeriod.DAY   -> DaySingleBar(ui.series.firstOrNull())
                else              -> LineChart(ui.series)
            }
            if (ui.period == StatsPeriod.WEEK && ui.bestDay != null) {
                Spacer(Modifier.height(12.dp))
                VersusStrip(
                    leftLabel = "Лучший день",
                    leftValue = "${dayShortRu(ui.bestDay.date.dayOfWeek)} · ${ui.bestDay.xp} XP",
                    rightLabel = "Пропущено",
                    rightValue = ui.worstDay?.takeIf { it.xp == 0 }?.let { dayShortRu(it.date.dayOfWeek) } ?: "—",
                )
            }
        }
    }
}

// ── BAR CHART (неделя) ──
@Composable
private fun WeekBarChart(series: List<DayPoint>) {
    val maxXp = (series.maxOfOrNull { it.xp } ?: 1).coerceAtLeast(1)
    val today = LocalDate.now()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(top = 8.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        series.forEach { pt ->
            val isToday = pt.date == today
            val zero = pt.xp == 0
            val heightFrac = if (zero) 0f else (pt.xp.toFloat() / maxXp).coerceIn(0.05f, 1f)
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (!zero) {
                        Text(
                            pt.xp.toString(),
                            color = TextDim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = -((heightFrac * 130).dp + 18.dp))
                        )
                    }
                    val barBrush: Brush = when {
                        zero -> androidx.compose.ui.graphics.SolidColor(Surface3)
                        isToday -> Brush.verticalGradient(listOf(BlueC, Color(0xFF1E6FD8)))
                        else -> Brush.verticalGradient(listOf(OrangeC, Color(0xFFFF5722)))
                    }
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight(heightFrac.coerceAtLeast(0.04f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(barBrush)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    dayShort2(pt.date.dayOfWeek),
                    color = if (isToday) BlueC else TextMute,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── SINGLE DAY (один большой бар + минуты) ──
@Composable
private fun DaySingleBar(pt: DayPoint?) {
    if (pt == null) {
        Text("Нет данных", color = TextMute)
        return
    }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text("Сегодня · ${pt.date}", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(80.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(OrangeC, Color(0xFFFF5722)))),
                contentAlignment = Alignment.Center,
            ) {
                Text("${pt.xp} XP", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .height(80.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(BlueC, Color(0xFF1E6FD8)))),
                contentAlignment = Alignment.Center,
            ) {
                Text("${pt.minutes} мин", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ── CALENDAR DOTS (месяц) ──
@Composable
private fun MonthCalendar(series: List<DayPoint>) {
    if (series.isEmpty()) return
    val first = series.first().date
    // Сдвиг по дню недели — 0 = Пн, 6 = Вс
    val leadingBlanks = (first.dayOfWeek.value - 1).coerceAtLeast(0)
    val today = LocalDate.now()
    val maxXp = (series.maxOfOrNull { it.xp } ?: 1).coerceAtLeast(1)

    Column {
        // Heading row (Пн..Вс)
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс").forEach {
                Text(
                    it,
                    color = TextMute,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        // Дни
        val cells = List(leadingBlanks) { null } + series.map { it as DayPoint? }
        cells.chunked(7).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                row.forEach { pt ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 3.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                if (pt == null) Color.Transparent
                                else colorForCalendarCell(pt.xp, maxXp)
                            )
                            .then(
                                if (pt?.date == today)
                                    Modifier.border(2.dp, BlueC, RoundedCornerShape(5.dp))
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (pt != null) {
                            Text(
                                pt.date.dayOfMonth.toString(),
                                color = if (pt.xp >= maxXp * 3 / 4) Color.White else TextMute,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                // Заполнитель для неполных строк
                repeat(7 - row.size) {
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 3.dp))
                }
            }
        }
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Меньше", color = TextMute, fontSize = 10.sp)
            Spacer(Modifier.width(6.dp))
            listOf(
                Surface3,
                OrangeC.copy(alpha = 0.22f),
                OrangeC.copy(alpha = 0.45f),
                OrangeC.copy(alpha = 0.75f),
                OrangeC,
            ).forEach { col ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(14.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(col)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text("Больше", color = TextMute, fontSize = 10.sp)
        }
    }
}

private fun colorForCalendarCell(xp: Int, max: Int): Color {
    if (xp <= 0) return Surface3
    val frac = xp.toFloat() / max
    return when {
        frac < 0.25f -> OrangeC.copy(alpha = 0.22f)
        frac < 0.50f -> OrangeC.copy(alpha = 0.45f)
        frac < 0.75f -> OrangeC.copy(alpha = 0.75f)
        else         -> OrangeC
    }
}

// ── LINE / AREA CHART (3М/6М/Год) ──
@Composable
private fun LineChart(series: List<DayPoint>) {
    if (series.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text("Нет данных", color = TextMute)
        }
        return
    }
    // Сгладим в "недельные" группы для длинных периодов
    val bucketed: List<Int> = when {
        series.size > 60 -> series.chunked(7).map { it.sumOf { p -> p.xp } }
        else -> series.map { it.xp }
    }
    val maxV = (bucketed.maxOrNull() ?: 1).coerceAtLeast(1)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val w = size.width
        val h = size.height
        val padTop = 10f
        val padBot = 30f
        val drawH = h - padTop - padBot

        // grid
        val gridPaint = LineCol
        val dash = PathEffect.dashPathEffect(floatArrayOf(2f, 4f))
        listOf(0.25f, 0.5f, 0.75f).forEach { f ->
            val y = padTop + drawH * f
            drawLine(
                color = gridPaint,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
                pathEffect = dash,
            )
        }

        // area gradient + line
        val n = bucketed.size
        if (n < 2) return@Canvas
        val stepX = w / (n - 1).toFloat()
        val areaPath = Path()
        val linePath = Path()
        bucketed.forEachIndexed { i, v ->
            val x = stepX * i
            val y = padTop + drawH * (1f - v.toFloat() / maxV)
            if (i == 0) {
                areaPath.moveTo(0f, h - padBot)
                areaPath.lineTo(x, y)
                linePath.moveTo(x, y)
            } else {
                areaPath.lineTo(x, y)
                linePath.lineTo(x, y)
            }
        }
        areaPath.lineTo(w, h - padBot)
        areaPath.close()

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(OrangeC.copy(alpha = 0.5f), OrangeC.copy(alpha = 0f)),
                startY = padTop,
                endY = h,
            ),
        )
        drawPath(
            path = linePath,
            color = OrangeC,
            style = Stroke(width = 6f, cap = StrokeCap.Round),
        )
        // Endpoint dot
        val lastX = stepX * (n - 1)
        val lastY = padTop + drawH * (1f - bucketed.last().toFloat() / maxV)
        drawCircle(color = BlueC, radius = 6f, center = Offset(lastX, lastY))
    }
}

// ═══════════════════════════════════════════════════════════
//  VERSUS strip (Best / Missed)
// ═══════════════════════════════════════════════════════════
@Composable
private fun VersusStrip(
    leftLabel: String, leftValue: String,
    rightLabel: String, rightValue: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(leftLabel.uppercase(), color = TextMute, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(2.dp))
            Text(leftValue, color = TextPri, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Text("→", color = GreenC, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(rightLabel.uppercase(), color = TextMute, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(2.dp))
            Text(rightValue, color = TextPri, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  BREAKDOWN
// ═══════════════════════════════════════════════════════════
@Composable
private fun BreakdownCard(b: ActivityBreakdown) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceBg,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            val rows = listOfNotNull(
                BreakRowData("📚", OrangeC, "Уроки", "${b.lessonsCount} завершено", b.lessonsMin, b.pct(b.lessonsMin)),
                BreakRowData("🃏", BlueC,   "Флэшкарты", if (b.flashcardsCount > 0) "${b.flashcardsCount} сетов · ⭐ ср. ${b.flashcardsAvgPct}%" else "—", b.flashcardsMin, b.pct(b.flashcardsMin)),
                BreakRowData("🎮", PurpleC, "Игры", "${b.gamesLevels} уровней", b.gamesMin, b.pct(b.gamesMin)),
                BreakRowData("📻", PinkC,   "Радио", if (b.radioMin > 0) "${b.radioMin} мин эфира" else "—", b.radioMin, b.pct(b.radioMin)),
                BreakRowData("📖", GreenC,  "Книги", if (b.booksCount > 0) "${b.booksCount} прочитано" else "—", b.booksMin, b.pct(b.booksMin)),
                BreakRowData("🤖", YellowC, "ИИ-чат", if (b.chatMessages > 0) "${b.chatMessages} сообщений" else "—", b.chatMin, b.pct(b.chatMin)),
            )
            rows.forEachIndexed { i, r ->
                BreakRow(r, isLast = i == rows.lastIndex)
            }
        }
    }
}

private data class BreakRowData(
    val emoji: String,
    val tint: Color,
    val name: String,
    val sub: String,
    val minutes: Int,
    val pct: Int,
)

@Composable
private fun BreakRow(r: BreakRowData, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(r.tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(r.emoji, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(r.name, color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(r.sub, color = TextDim, fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${r.minutes} мин", color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text("${r.pct}%", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LineCol)
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  MISTAKES & WEAK
// ═══════════════════════════════════════════════════════════
@Composable
private fun MistakesCard(mistakes: List<MistakeRow>, weak: List<WeakRow>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceBg,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            mistakes.forEachIndexed { i, m ->
                WordRow(
                    main = m.itemMain,
                    sub  = m.itemHint.ifBlank { "ошибка в игре" },
                    badgeText = "×${m.attempts}",
                    badgeColor = RedC,
                    isLast = i == mistakes.lastIndex && weak.isEmpty(),
                )
            }
            weak.forEachIndexed { i, w ->
                WordRow(
                    main = w.spanish,
                    sub  = w.russian,
                    badgeText = "SM-2 weak",
                    badgeColor = YellowC,
                    isLast = i == weak.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun WordRow(main: String, sub: String, badgeText: String, badgeColor: Color, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(main, color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (sub.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(sub, color = TextDim, fontSize = 11.sp)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(badgeText, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
    if (!isLast) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LineCol))
    }
}

// ═══════════════════════════════════════════════════════════
//  PROGRESS
// ═══════════════════════════════════════════════════════════
@Composable
private fun ProgressCard(ui: StatsUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceBg,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ProgRow("⭐", "Общий XP", ui.totalXpAllTime.toString(), if (ui.totalXp > 0) "+${ui.totalXp}" else null)
            ProgRow("📚", "Уроки", "${ui.lessonsCompleted} / ${ui.lessonsTotal}", if (ui.lessonsDelta > 0) "+${ui.lessonsDelta}" else null)
            ProgRow("🃏", "Слов выучено", ui.wordsLearned.toString(), null)
            ProgRow("🔥", "Streak", "${ui.currentStreak} дней", "рекорд ${ui.longestStreak}")
            Spacer(Modifier.height(4.dp))
            LeagueChip(ui)
        }
    }
}

@Composable
private fun ProgRow(icon: String, name: String, value: String, delta: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 22.sp, modifier = Modifier.width(32.dp))
        Text(name, color = TextDim, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = TextPri, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (delta != null) {
                Spacer(Modifier.width(6.dp))
                Text(delta, color = GreenC, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LeagueChip(ui: StatsUi) {
    val name = LEAGUE_NAMES_RU.getOrElse(ui.leagueIndex) { "—" }
    val toNext = (ui.nextLeagueAt - ui.skillRating).coerceAtLeast(0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF2A1A3F), Color(0xFF3A2050))))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🏛️", fontSize = 22.sp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(1.dp))
            val sub = if (toNext > 0)
                "до следующей лиги $toNext · пик ${ui.peakSkillRating}"
            else
                "макс. лига · пик ${ui.peakSkillRating}"
            Text(sub, color = TextDim, fontSize = 11.sp)
        }
        Text(ui.skillRating.toString(), color = PurpleC, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

// ═══════════════════════════════════════════════════════════
//  ACHIEVEMENTS row
// ═══════════════════════════════════════════════════════════
@Composable
private fun AchRow(items: List<AchRow>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items.size) { i ->
            val a = items[i]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface2)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .widthIn(min = 170.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(a.medal, fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(a.title, color = TextPri, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Spacer(Modifier.height(2.dp))
                    Text(a.description, color = TextDim, fontSize = 10.sp, maxLines = 1)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  INSIGHT
// ═══════════════════════════════════════════════════════════
@Composable
private fun InsightCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(BlueC.copy(alpha = 0.12f), PurpleC.copy(alpha = 0.12f))))
            .border(1.dp, BlueC.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("🧠", fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Text(text, color = TextPri, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

// ═══════════════════════════════════════════════════════════
//  Section title helper
// ═══════════════════════════════════════════════════════════
@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = TextDim,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 18.dp, bottom = 10.dp),
    )
}

private fun dayShort2(d: DayOfWeek): String = when (d) {
    DayOfWeek.MONDAY -> "Пн"; DayOfWeek.TUESDAY -> "Вт"; DayOfWeek.WEDNESDAY -> "Ср"
    DayOfWeek.THURSDAY -> "Чт"; DayOfWeek.FRIDAY -> "Пт"
    DayOfWeek.SATURDAY -> "Сб"; DayOfWeek.SUNDAY -> "Вс"
}

private fun dayShortRu(d: DayOfWeek): String = when (d) {
    DayOfWeek.MONDAY -> "Пн"; DayOfWeek.TUESDAY -> "Вт"; DayOfWeek.WEDNESDAY -> "Ср"
    DayOfWeek.THURSDAY -> "Чт"; DayOfWeek.FRIDAY -> "Пт"
    DayOfWeek.SATURDAY -> "Сб"; DayOfWeek.SUNDAY -> "Вс"
}
