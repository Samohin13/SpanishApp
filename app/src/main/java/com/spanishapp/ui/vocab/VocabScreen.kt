package com.spanishapp.ui.vocab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.spanishapp.data.db.entity.UserVocabStateEntity

// ═══════════════════════════════════════════════════════════
//  PALETTE — стиль как у StatsScreen (тёмная тема)
// ═══════════════════════════════════════════════════════════
private val Bg           = Color(0xFF0B0D12)
private val SurfaceBg    = Color(0xFF161922)
private val Surface2     = Color(0xFF1F2330)
private val Surface3     = Color(0xFF2A2F3E)
private val TextPri      = Color(0xFFF4F6FB)
private val TextDim      = Color(0xFF9AA3B7)
private val TextMute     = Color(0xFF6B7388)
private val OrangeC      = Color(0xFFFF8A3D)
private val BlueC        = Color(0xFF4EA1FF)
private val GreenC       = Color(0xFF4ADE80)
private val PurpleC      = Color(0xFFA78BFA)
private val YellowC      = Color(0xFFFACC15)
private val RedC         = Color(0xFFF87171)

@Composable
fun VocabScreen(
    navController: NavController,
    viewModel: VocabViewModel = hiltViewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val forgotten by viewModel.forgotten.collectAsStateWithLifecycle()

    Scaffold(containerColor = Bg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            AppBar(onBack = { navController.popBackStack() })

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (!ui.loaded || ui.totalKnown == 0) {
                    EmptyState()
                } else {
                    HeroCard(ui)
                    Spacer(Modifier.height(14.dp))

                    SectionTitle("📚 По уровням CEFR")
                    CefrRingsCard(ui)
                    Spacer(Modifier.height(10.dp))

                    SectionTitle("🧱 Состояние знания")
                    StatusBreakdownCard(ui)
                    Spacer(Modifier.height(10.dp))

                    if (ui.topUsed.isNotEmpty()) {
                        SectionTitle("🔥 Любимые слова")
                        TopUsedCard(ui.topUsed)
                        Spacer(Modifier.height(10.dp))
                    }

                    if (forgotten.isNotEmpty()) {
                        SectionTitle("💡 Подзабыл")
                        ForgottenCard(forgotten)
                        Spacer(Modifier.height(10.dp))
                    }

                    InfoCard()
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  APP BAR
// ═══════════════════════════════════════════════════════════
@Composable
private fun AppBar(onBack: () -> Unit) {
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
            Text("Мой словарный запас", color = TextPri, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text("Личная коллекция выученных слов", color = TextDim, fontSize = 12.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  EMPTY
// ═══════════════════════════════════════════════════════════
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, bottom = 60.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📚", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Здесь будет твой словарь",
                color = TextPri,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Пройди пару уроков, повтори флэшкарты\nили отправь сообщение в чат —\nслова появятся через минуту.",
                color = TextDim,
                fontSize = 13.sp,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  HERO
// ═══════════════════════════════════════════════════════════
@Composable
private fun HeroCard(ui: VocabUi) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceBg)
            .padding(20.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("🏆", fontSize = 32.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "${ui.totalKnown}",
                color = TextPri,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
            )
            Text("слов в твоём словаре", color = TextDim, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ui.addedThisWeek > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenC.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "+${ui.addedThisWeek} за неделю",
                            color = GreenC,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BlueC.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Уровень: ${ui.estimatedCefr}",
                        color = BlueC,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  CEFR RINGS
// ═══════════════════════════════════════════════════════════
@Composable
private fun CefrRingsCard(ui: VocabUi) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceBg)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CefrRow("A1", ui.cefrA1, 250, GreenC)
            CefrRow("A2", ui.cefrA2, 400, BlueC)
            CefrRow("B1", ui.cefrB1, 600, PurpleC)
            CefrRow("B2", ui.cefrB2, 800, OrangeC)
        }
    }
}

@Composable
private fun CefrRow(level: String, count: Int, total: Int, color: Color) {
    val pct = (count.toFloat() / total).coerceIn(0f, 1f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            level,
            color = TextPri,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Surface3),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "$count / $total",
            color = TextDim,
            fontSize = 12.sp,
            modifier = Modifier.width(72.dp),
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  STATUS BREAKDOWN
// ═══════════════════════════════════════════════════════════
@Composable
private fun StatusBreakdownCard(ui: VocabUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusCell("⭐", ui.mastered, "Закреп", GreenC, Modifier.weight(1f))
        StatusCell("✍️", ui.producing, "Использует", BlueC, Modifier.weight(1f))
        StatusCell("📚", ui.learning, "В работе", OrangeC, Modifier.weight(1f))
        StatusCell("👁", ui.seen, "Видел", PurpleC, Modifier.weight(1f))
    }
}

@Composable
private fun StatusCell(icon: String, count: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceBg)
            .padding(12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "$count",
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(label, color = TextDim, fontSize = 11.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  TOP USED
// ═══════════════════════════════════════════════════════════
@Composable
private fun TopUsedCard(words: List<UserVocabStateEntity>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceBg)
            .padding(16.dp),
    ) {
        Column {
            words.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { word ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface2)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                word.word,
                                color = TextPri,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "×${word.usageCount}",
                                color = OrangeC,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  FORGOTTEN
// ═══════════════════════════════════════════════════════════
@Composable
private fun ForgottenCard(words: List<UserVocabStateEntity>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceBg)
            .padding(16.dp),
    ) {
        Column {
            Text(
                "Слова которые ты не видел больше 30 дней.",
                color = TextDim,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(10.dp))
            words.take(8).forEach { w ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("◯", color = YellowC, fontSize = 10.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        w.word,
                        color = TextPri,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    w.cefr?.let {
                        Text(it, color = TextMute, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  INFO
// ═══════════════════════════════════════════════════════════
@Composable
private fun InfoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceBg)
            .padding(14.dp),
    ) {
        Column {
            Text("ℹ️ Как считается", color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Алгоритм собирает сигналы из всех твоих активностей — уроков, " +
                "флэшкарт SM-2, чата с AI, чтения. Чем больше способов ты " +
                "встретил слово и чем активнее используешь, тем выше его статус. " +
                "Обновляется раз в сутки в фоне.",
                color = TextDim,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = TextPri,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 8.dp),
    )
}

