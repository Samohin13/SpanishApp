package com.spanishapp.ui.paywall

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

/**
 * v1.23.0: Paywall PRO — реализация по docs/mockups/paywall_final_swipe.html.
 *
 * Архитектура:
 *  - 5 страниц через HorizontalPager (Hero · Numbers · Features · Compare · Progress)
 *  - Фиксированный верх: ✕ + точки прогресса
 *  - Фиксированный низ: urgency + 2 plan-карточки Monthly/Year + CTA + trust
 *
 * Все цвета, шрифты, отступы взяты из HTML-мокапа.
 */

// === Дизайн-токены из мокапа (CSS :root) ===
private val BgColor = Color(0xFF0E0E12)
private val SurfaceColor = Color(0xFF1A1A20)
private val Surface2Color = Color(0xFF22222A)
private val BorderColor = Color(0xFF2C2C36)
private val TextColor = Color(0xFFECECEC)
private val TextDim = Color(0xFF8E8E93)
private val PrimaryOrange = Color(0xFFFF8A3D)
private val PrimaryOrange2 = Color(0xFFFF6A1A)
private val A2Blue = Color(0xFF4EA1FF)
private val B1Green = Color(0xFF4ADE80)
private val B2Pink = Color(0xFFF472B6)
private val GoldColor = Color(0xFFFFD27A)
private val GoodGreen = Color(0xFF4ADE80)

// === v1.23.1 (audit fix Bug 2): hoist статичные списки на file-level
// чтобы не аллоцировать на каждый recomposition. HorizontalPager
// держит 2-3 соседних страницы скомпонованными — без hoist'а
// списки пересоздавались 100+ раз в секунду при свайпе. ===

private data class StatItem(val num: String, val lbl: String, val sub: String)
private val PAYWALL_STATS = listOf(
    StatItem("180", "уроков", "A2 · B1 · B2"),
    StatItem("75", "историй", "A2 · B1 · B2"),
    StatItem("1327", "глаголов", "все времена"),
    StatItem("100", "уровней игр", "все 6 игр"),
    StatItem("∞", "AI-репетитор", "сейчас 50/день"),
    StatItem("10К", "слов в карточках", "по всем уровням"),
)

private data class FeatItem(val icon: String, val title: String, val dim: String)
private val PAYWALL_FEATS = listOf(
    FeatItem("🎓", "Все уроки до B2", "180 новых уроков · грамматика · диалоги"),
    FeatItem("📚", "Все 100 историй", "Художественное чтение всех уровней"),
    FeatItem("🔥", "1327 глаголов", "Все формы прошедшего, будущего, сослагательного"),
    FeatItem("🎯", "Все 100 уровней игр", "Сейчас открыты только первые 10 каждой"),
    FeatItem("🃏", "Умные карточки слов", "Все 10 000 слов с интервальным повторением"),
    FeatItem("🤖", "ИИ-репетитор без лимита", "Сейчас 50 сообщений в день — будет ∞"),
)

private data class CompareRow(val icon: String, val cat: String, val free: String, val pro: String)
private val PAYWALL_COMPARE_ROWS = listOf(
    CompareRow("🎓", "Уроки", "60 (A1)", "240 (A1→B2)"),
    CompareRow("🧠", "Грамматика", "15 (A1)", "75 (все)"),
    CompareRow("💬", "Диалоги", "A1", "все уровни"),
    CompareRow("📚", "Книги", "25 (A1)", "100 (все)"),
    CompareRow("🔥", "Спряжение", "базовое", "1327 глаг."),
    CompareRow("🎯", "Игры", "10 уровней", "100 уровней"),
    CompareRow("🃏", "SM-2 карт.", "A1 слова", "все ~10K"),
    CompareRow("🤖", "AI Chat", "50/день", "∞ безлимит"),
    CompareRow("📖", "Словарь", "✓", "✓"),
    CompareRow("📻", "Радио", "✓", "✓"),
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PaywallScreen(navController: NavHostController) {
    val vm: PaywallViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = 0) { 5 }

    // v1.23.1 (audit Bug 10): слушаем purchased event через LaunchedEffect —
    // навигация только в alive lifecycle, никаких race conditions.
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is PurchaseEvent.Purchased -> navController.popBackStack()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // === FIXED TOP ===
            PaywallTopBar(
                currentPage = pagerState.currentPage,
                onClose = { navController.popBackStack() }
            )

            // === SWIPEABLE PAGES ===
            HorizontalPager(
                state = pagerState,
                // v1.23.2 (audit Bug 17): keep 1 page on each side composed
                // → плавный свайп без задержки на композицию соседней страницы
                // (главное где это заметно: переход 1→2, тяжёлый PageNumbers
                // с 6 stat-карточками успевает скомпоноваться заранее).
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                when (page) {
                    0 -> PageHero()
                    1 -> PageNumbers()
                    2 -> PageFeatures()
                    3 -> PageCompare()
                    4 -> PageProgress()
                }
            }

            // === FIXED BOTTOM ===
            PaywallBottomBar(
                selectedPlan = state.selectedPlan,
                isLoading = state.isLoading,
                onSelectPlan = vm::selectPlan,
                onPurchase = { vm.startPurchase() }
            )
        }
    }
}

@Composable
private fun PaywallTopBar(currentPage: Int, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 18.dp, end = 18.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Премиум", color = TextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextDim)
            }
        }
        Spacer(Modifier.height(12.dp))
        // Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(5) { i ->
                val isActive = i == currentPage
                val width by animateDpAsState(if (isActive) 22.dp else 6.dp, label = "dot_w_$i")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = width, height = 6.dp)
                        .clip(if (isActive) RoundedCornerShape(3.dp) else CircleShape)
                        .background(if (isActive) PrimaryOrange else Color.White.copy(alpha = 0.15f))
                )
            }
        }
    }
}

// ============== PAGE 1: HERO + SOCIAL ==============
// v1.23.1 (audit Bug 7/16): единая стратегия с verticalScroll вместо
// weight-Spacer'ов (фрагильный layout, может клипать контент).
@Composable
private fun PageHero() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        // Hero block — большой, центрированный
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1F1A28), Color(0xFF14141A))
                    )
                )
                .border(1.dp, PrimaryOrange.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
                .padding(horizontal = 22.dp, vertical = 32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                // PRO tag pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .border(1.dp, PrimaryOrange, RoundedCornerShape(99.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        "✦ ESPEAK · PREMIUM ✦",
                        color = PrimaryOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.5.sp
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text("💎", fontSize = 88.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    buildAnnotatedString {
                        append("Открой ")
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.linearGradient(listOf(PrimaryOrange, GoldColor))
                        )) { append("весь") }
                        append("\nиспанский")
                    },
                    color = TextColor,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 42.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "От алфавита до свободной речи —\nвсё в одной подписке",
                    color = TextDim,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Social proof card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvatarStack()
                Column {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = TextColor)) {
                                append("12 847")
                            }
                            append(" учеников уже PRO")
                        },
                        color = TextColor, fontSize = 15.sp
                    )
                    Text("★★★★★ 4.8 · Google Play",
                        color = GoldColor, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AvatarStack() {
    Row {
        val colors = listOf(PrimaryOrange, A2Blue, Color(0xFFA78BFA), B1Green)
        colors.forEachIndexed { i, c ->
            Box(
                modifier = Modifier
                    .offset(x = (-(i * 8)).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(c)
                    .border(2.dp, SurfaceColor, CircleShape)
            )
        }
    }
}

// ============== PAGE 2: NUMBERS ==============
// v1.23.1 (audit Bug 7): единая layout-стратегия — verticalScroll
// на всех страницах вместо weight-Spacer'ов (mixed подход вызывал
// multiple measure passes).
@Composable
private fun PageNumbers() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "📊 Что ты получишь",
            color = TextColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Сейчас доступны только 60 уроков уровня A1.\nС PRO открывается всё остальное:",
            color = TextDim, fontSize = 14.sp, lineHeight = 20.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        // Grid 2 cols × 3 rows — обычный for вместо chunked+forEach
        // (chunked() аллоцирует List на каждый recompose).
        for (rowIndex in 0 until 3) {
            val left = PAYWALL_STATS[rowIndex * 2]
            val right = PAYWALL_STATS[rowIndex * 2 + 1]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(left.num, left.lbl, left.sub, modifier = Modifier.weight(1f))
                StatCard(right.num, right.lbl, right.sub, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(num: String, lbl: String, sub: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(listOf(SurfaceColor, Surface2Color))
            )
            .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
    ) {
        // Top accent strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Brush.horizontalGradient(listOf(PrimaryOrange, GoldColor)))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(num, color = PrimaryOrange, fontSize = 44.sp,
                fontWeight = FontWeight.Black, letterSpacing = (-2).sp)
            Spacer(Modifier.height(8.dp))
            Text(lbl, color = TextColor, fontSize = 13.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Text(sub, color = TextDim, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

// ============== PAGE 3: FEATURES ==============
@Composable
private fun PageFeatures() {
    // v1.23.1 (audit Bug 2/7): использовать file-level PAYWALL_FEATS +
    // verticalScroll вместо weight-Spacer'ов.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text("✨ Что входит", color = TextColor, fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
        Text("Все шесть направлений без ограничений",
            color = TextDim, fontSize = 14.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        for (f in PAYWALL_FEATS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceColor)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(PrimaryOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(f.icon, fontSize = 22.sp) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(f.title, color = TextColor, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold)
                    Text(f.dim, color = TextDim, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ============== PAGE 4: COMPARE FREE vs PRO ==============
// v1.23.1 (audit Bug 2): использует file-level PAYWALL_COMPARE_ROWS.
@Composable
private fun PageCompare() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text("⚖️ Free vs PRO", color = TextColor, fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        Text("Что именно меняется при подписке",
            color = TextDim, fontSize = 14.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceColor)
                .border(1.dp, BorderColor, RoundedCornerShape(18.dp))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2Color)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("КОНТЕНТ", color = TextDim, fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f))
                Text("FREE", color = TextDim, fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
                    modifier = Modifier.width(80.dp))
                Text("PRO 💎", color = PrimaryOrange, fontSize = 12.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 1.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.width(80.dp))
            }
            // v1.23.2 (audit Bug 13): фиксированная ширина для FREE/PRO
            // колонок вместо weight nesting. Раньше 10 строк × 3 weighted
            // children + nested Row(weight=1.4f) → каждый row дважды
            // measured (unconstrained + constrained). Теперь категория
            // (icon+text) занимает weight(1f), free и pro — фиксированные
            // 80dp каждая. Один measure pass per row.
            PAYWALL_COMPARE_ROWS.forEachIndexed { i, r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(r.icon, fontSize = 16.sp)
                    Text(r.cat, color = TextColor, fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f))
                    Text(r.free, color = TextDim, fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(80.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryOrange.copy(alpha = 0.1f))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(r.pro, color = PrimaryOrange, fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center)
                    }
                }
                if (i < PAYWALL_COMPARE_ROWS.size - 1) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                }
            }
            // Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryOrange.copy(alpha = 0.06f))
                    .padding(10.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        append("Словарь, радио, произношение, ачивки, виджет ")
                        withStyle(SpanStyle(color = PrimaryOrange, fontWeight = FontWeight.Bold)) {
                            append("бесплатны навсегда")
                        }
                    },
                    color = TextDim, fontSize = 10.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ============== PAGE 5: PROGRESS + TESTIMONIAL ==============
@Composable
private fun PageProgress() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Progress card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
                Text("📈 КУДА ТЫ ДОЙДЁШЬ", color = GoldColor, fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(4.dp))
                Text("Free застрянет на A1. С PRO дойдёшь до уверенного B2.",
                    color = TextColor, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                ProgressRow("FREE", 0.2f, "A1", isPro = false)
                Spacer(Modifier.height(12.dp))
                ProgressRow("PRO", 1.0f, "B2", isPro = true)
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("A1", "A2", "B1", "B2").forEach {
                        Text(it, color = TextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Testimonial
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(listOf(
                        PrimaryOrange.copy(alpha = 0.08f),
                        GoldColor.copy(alpha = 0.04f)
                    ))
                )
                .border(1.dp, PrimaryOrange.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "«За 4 месяца с PRO дошла до B1 и впервые свободно говорила с испанцем в Барселоне. AI-чат — лучшая фича.»",
                    color = TextColor, fontSize = 13.sp,
                    fontStyle = FontStyle.Italic, lineHeight = 19.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(A2Blue))
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = TextColor, fontWeight = FontWeight.Bold)) {
                                append("Мария К.")
                            }
                            withStyle(SpanStyle(color = TextDim)) {
                                append(" · PRO с января")
                            }
                        },
                        fontSize = 11.sp
                    )
                }
            }
        }

        Text(
            "👇 Жми «Начать» внизу — 7 дней бесплатно",
            color = GoldColor, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
    }
}

@Composable
private fun ProgressRow(label: String, fillRatio: Float, value: String, isPro: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, color = if (isPro) PrimaryOrange else TextDim,
            fontSize = 12.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillRatio)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        if (isPro)
                            Brush.horizontalGradient(listOf(PrimaryOrange, GoldColor))
                        else Brush.horizontalGradient(listOf(Color(0xFF5A5A5A), Color(0xFF5A5A5A)))
                    )
            )
        }
        Text(value, color = TextColor, fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
    }
}

// ============== FIXED BOTTOM ==============
@Composable
private fun PaywallBottomBar(
    selectedPlan: PaywallPlan,
    isLoading: Boolean,
    onSelectPlan: (PaywallPlan) -> Unit,
    onPurchase: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgColor)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        // v1.23.1 (audit Bug 6): убран фейковый «02д 14ч 38м» countdown —
        // Google Play Policy 4.4 запрещает deceptive urgency timers без
        // реального источника. Если решим вернуть скидку — нужен реальный
        // timestamp в SubscriptionPreferences (offerExpiresAt) с настоящим
        // обратным отсчётом. Сейчас оставлен только статичный badge.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryOrange.copy(alpha = 0.10f))
                .border(1.dp, PrimaryOrange.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💎 7 дней бесплатно · затем выгода 42% за год",
                color = PrimaryOrange, fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(10.dp))

        // Plan cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlanCard(
                title = "Месяц",
                price = "\$4.99",
                per = "в месяц",
                note = "гибко · без обязательств",
                isActive = selectedPlan == PaywallPlan.MONTH,
                badge = null,
                onClick = { onSelectPlan(PaywallPlan.MONTH) },
                modifier = Modifier.weight(1f)
            )
            PlanCard(
                title = "Год",
                price = "\$34.99",
                per = "\$2.92/мес",
                oldPer = "\$59.88",
                note = "выгода \$24.89",
                isActive = selectedPlan == PaywallPlan.YEAR,
                badge = "−42%",
                onClick = { onSelectPlan(PaywallPlan.YEAR) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))

        // CTA
        Button(
            onClick = onPurchase,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = PrimaryOrange.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(PrimaryOrange, PrimaryOrange2))),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        if (selectedPlan == PaywallPlan.YEAR)
                            "Начать 7 дней бесплатно"
                        else "Начать за \$4.99/мес",
                        color = Color.White, fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (selectedPlan == PaywallPlan.YEAR)
                "далее \$34.99/год · отмена в Google Play"
            else "списание ежемесячно · отмена в Google Play",
            color = TextDim, fontSize = 10.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Trust
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Отмена 1 кликом", "Возврат 7 дней", "Без сюрпризов").forEach {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Default.Check, null,
                        modifier = Modifier.size(10.dp), tint = GoodGreen)
                    Text(it, color = TextDim, fontSize = 9.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    per: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    oldPer: String? = null,
    note: String? = null,
    badge: String? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) PrimaryOrange.copy(alpha = 0.08f) else SurfaceColor)
            .border(
                width = 1.5.dp,
                color = if (isActive) PrimaryOrange else BorderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Column {
            Text(title.uppercase(), color = TextDim, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            if (isActive) {
                Text(
                    price,
                    style = androidx.compose.material3.LocalTextStyle.current.copy(
                        brush = Brush.linearGradient(listOf(PrimaryOrange, GoldColor)),
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = (-1).sp,
                    )
                )
            } else {
                Text(price, color = TextColor, fontSize = 22.sp,
                    fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (oldPer != null) {
                    Text(oldPer, color = Color(0xFF5A5A5A), fontSize = 10.sp,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                }
                Text(per, color = TextDim, fontSize = 10.sp)
            }
            if (note != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    buildAnnotatedString {
                        if (note.startsWith("выгода")) {
                            withStyle(SpanStyle(color = GoodGreen, fontWeight = FontWeight.Bold)) {
                                append(note)
                            }
                        } else {
                            withStyle(SpanStyle(color = TextDim)) { append(note) }
                        }
                    },
                    fontSize = 9.sp, lineHeight = 12.sp
                )
            }
        }
        // Badge
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-8).dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(PrimaryOrange)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(badge, color = Color.White, fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            }
        }
    }
}
