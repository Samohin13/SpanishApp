package com.spanishapp.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.ui.components.*

// ── Roadmap Data Model ────────────────────────────────────────

data class RoadmapUnit(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val cefrLevel: String = "A1",   // A1 / A2 / B1 / B2
    val isLocked: Boolean = true,   // computed by HomeViewModel from lesson_progress
    val progress: Float = 0f,       // computed by HomeViewModel
    val color: Color,
    val lessons: List<RoadmapLesson> = emptyList()
)

data class RoadmapLesson(
    val title: String,
    val type: String,               // vocab / grammar / quiz / phrase
    val category: String = "general",
    val isCompleted: Boolean = false  // computed by HomeViewModel from lesson_progress
)

// ═══════════════════════════════════════════════════════════════
//  HOME SCREEN — Figma light design
// ═══════════════════════════════════════════════════════════════

private val Purple      = Color(0xFF7B2FBE)
private val PurplePale  = Color(0xFFF3E8FF)
private val Pink        = Color(0xFFE040FB)
private val GoldColor   = Color(0xFFFF9500)
private val OrangeColor = Color(0xFFFF6B00)
private val TextMain    = Color(0xFF1A1A1A)
private val TextGray    = Color(0xFF8E8E93)
private val CardBorder  = Color(0xFFE5E5EA)
private val LockGray    = Color(0xFFC7C7CC)
private val BgGray      = Color(0xFFF8F8FA)
private val SubBg       = Color(0xFFF2F2F7)

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedUnitId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Header row ─────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Profile circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Purple)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { navController.navigate("profile") }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // XP + Streak pills
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill(
                        emoji = "✨",
                        value = "${state.totalXp}",
                        bgColor   = Color(0xFFFFF3E0),
                        textColor = GoldColor,
                        borderColor = GoldColor.copy(alpha = 0.3f)
                    )
                    StatPill(
                        emoji = "🔥",
                        value = "${state.currentStreak}",
                        bgColor   = Color(0xFFFFF3E0),
                        textColor = OrangeColor,
                        borderColor = OrangeColor.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // ── Greeting ───────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp)
            ) {
                Text(
                    "Привет! 👋",
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextMain
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Продолжай изучать испанский язык",
                    fontSize = 15.sp,
                    color    = TextGray
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // ── Topic cards ────────────────────────────────────────
        itemsIndexed(state.roadmapUnits) { _, unit ->
            TopicCard(
                unit       = unit,
                isExpanded = expandedUnitId == unit.id,
                onToggle   = {
                    expandedUnitId = if (expandedUnitId == unit.id) null else unit.id
                },
                onLessonClick = { lessonIndex ->
                    if (!unit.isLocked) {
                        navController.navigate("lesson_intro/${unit.id}/$lessonIndex")
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Stat pill ──────────────────────────────────────────────────

@Composable
private fun StatPill(
    emoji: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    borderColor: Color
) {
    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(
                value,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
        }
    }
}

// ── Topic card ─────────────────────────────────────────────────

@Composable
private fun TopicCard(
    unit: RoadmapUnit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLessonClick: (Int) -> Unit   // lessonIndex
) {
    Surface(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(16.dp),
        color     = Color.White,
        border    = BorderStroke(1.dp, CardBorder),
        onClick   = onToggle
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Title row ─────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = "${unit.id}. ${unit.title}",
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (unit.isLocked) TextGray else TextMain,
                    modifier   = Modifier.weight(1f)
                )
                if (unit.isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint     = LockGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint     = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Progress bar (only for unlocked with progress) ─
            if (!unit.isLocked && unit.progress > 0f) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(PurplePale)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(unit.progress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(Purple, Pink))
                                )
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${(unit.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray
                    )
                }
            }

            // ── Expanded lessons ──────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier            = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    unit.lessons.forEachIndexed { idx, lesson ->
                        SubLessonRow(
                            number   = idx + 1,
                            lesson   = lesson,
                            isLocked = unit.isLocked,
                            onClick  = { onLessonClick(idx) }
                        )
                    }
                }
            }
        }
    }
}

// ── Sub-lesson row ────────────────────────────────────────────

@Composable
private fun SubLessonRow(
    number: Int,
    lesson: RoadmapLesson,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick   = if (!isLocked) onClick else {{}},
        modifier  = Modifier.fillMaxWidth(),
        color     = SubBg,
        shape     = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number circle
            Box(
                modifier         = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (lesson.isCompleted) Purple else CardBorder),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.isCompleted) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else {
                    Text(
                        "$number",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isLocked) LockGray else TextGray
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text     = lesson.title,
                modifier = Modifier.weight(1f),
                style    = MaterialTheme.typography.bodyMedium,
                color    = if (isLocked) TextGray else TextMain
            )

            if (isLocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint     = LockGray,
                    modifier = Modifier.size(14.dp)
                )
            } else if (!lesson.isCompleted) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint     = TextGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
