package com.spanishapp.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val cefrLevel: String = "A1",
    val isLocked: Boolean = true,
    val progress: Float = 0f,
    val color: Color,
    val lessons: List<RoadmapLesson> = emptyList()
)

data class RoadmapLesson(
    val title: String,
    val type: String,
    val category: String = "general",
    val isCompleted: Boolean = false
)

// ═══════════════════════════════════════════════════════════════
//  Palette
// ═══════════════════════════════════════════════════════════════

private val Purple      = Color(0xFF7B2FBE)
private val Pink        = Color(0xFFE040FB)
private val GoldColor   = Color(0xFFFF9500)
private val OrangeColor = Color(0xFFFF6B00)
private val TextMain    = Color(0xFF1A1A1A)
private val TextGray    = Color(0xFF8E8E93)
private val LockGray    = Color(0xFFC7C7CC)
private val BgGray      = Color(0xFFF0F0F5)

// ═══════════════════════════════════════════════════════════════
//  HOME SCREEN
// ═══════════════════════════════════════════════════════════════

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
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Header ─────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Purple)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { navController.navigate("profile") }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill("✨", "${state.totalXp}", Color(0xFFFFF3E0), GoldColor, GoldColor.copy(.3f))
                    StatPill("🔥", "${state.currentStreak}", Color(0xFFFFF3E0), OrangeColor, OrangeColor.copy(.3f))
                }
            }
        }

        // ── Greeting ───────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 20.dp)
            ) {
                Text("Привет! 👋", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Spacer(Modifier.height(2.dp))
                Text("Продолжай изучать испанский язык", fontSize = 15.sp, color = TextGray)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

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
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── Stat pill ──────────────────────────────────────────────────

@Composable
private fun StatPill(emoji: String, value: String, bgColor: Color, textColor: Color, borderColor: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  TOPIC CARD  —  главная карточка блока
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TopicCard(
    unit: RoadmapUnit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLessonClick: (Int) -> Unit
) {
    val accentColor   = if (unit.isLocked) LockGray else unit.color
    val completedCount = unit.lessons.count { it.isCompleted }
    val totalCount     = unit.lessons.size

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(
                elevation = if (unit.isLocked) 2.dp else 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = accentColor.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
    ) {
        Column {

            // ── Цветная шапка ──────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        if (unit.isLocked)
                            Brush.horizontalGradient(listOf(Color(0xFFDDDDDD), Color(0xFFCCCCCC)))
                        else
                            Brush.horizontalGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.72f))
                            )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Большой эмодзи-иконка блока
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(unit.icon, fontSize = 22.sp)
                        }

                        Column {
                            // Номер блока
                            Text(
                                text = "Блок ${unit.id}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.80f)
                            )
                            // Название блока
                            Text(
                                text = unit.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Правая часть шапки: CEFR + замок/кол-во
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CefrBadge(unit.cefrLevel)
                        if (unit.isLocked) {
                            Icon(Icons.Default.Lock, null, tint = Color.White.copy(.8f), modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                "$completedCount/$totalCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ── Тело карточки ──────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {

                // Описание блока
                Text(
                    text = unit.description,
                    fontSize = 13.sp,
                    color = if (unit.isLocked) TextGray.copy(.6f) else TextGray,
                    maxLines = 2,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(12.dp))

                // Прогресс-бар + стрелка
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Прогресс-трек
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                    ) {
                        if (!unit.isLocked && unit.progress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(unit.progress)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(accentColor, accentColor.copy(.7f)))
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    // Процент или "Заблокировано"
                    if (unit.isLocked) {
                        Text("Заблокировано", fontSize = 11.sp, color = LockGray)
                    } else {
                        Text(
                            "${(unit.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    // Стрелка раскрытия
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint     = if (unit.isLocked) LockGray else accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // ── Развёрнутые уроки ──────────────────────────
                AnimatedVisibility(
                    visible = isExpanded,
                    enter   = expandVertically(animationSpec = tween(220)) + fadeIn(tween(220)),
                    exit    = shrinkVertically(animationSpec = tween(180)) + fadeOut(tween(180))
                ) {
                    Column(
                        modifier = Modifier.padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = accentColor.copy(.12f))
                        Spacer(Modifier.height(2.dp))

                        unit.lessons.forEachIndexed { idx, lesson ->
                            SubLessonRow(
                                number    = idx + 1,
                                lesson    = lesson,
                                isLocked  = unit.isLocked,
                                unitColor = accentColor,
                                onClick   = { onLessonClick(idx) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── CEFR badge ────────────────────────────────────────────────

@Composable
private fun CefrBadge(level: String) {
    val (bg, text) = when (level) {
        "A1" -> Color(0xFF2E7D32) to Color.White
        "A2" -> Color(0xFF0277BD) to Color.White
        "B1" -> Color(0xFFE65100) to Color.White
        "B2" -> Color(0xFF6A1B9A) to Color.White
        else -> Color(0xFF37474F) to Color.White
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg.copy(alpha = 0.85f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(level, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = text)
    }
}

// ═══════════════════════════════════════════════════════════════
//  SUB-LESSON ROW  —  строка урока внутри блока
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SubLessonRow(
    number: Int,
    lesson: RoadmapLesson,
    isLocked: Boolean,
    unitColor: Color,
    onClick: () -> Unit
) {
    val typeEmoji = when (lesson.type) {
        "vocab"   -> "📚"
        "grammar" -> "📖"
        "phrase"  -> "💬"
        else      -> "🎯"  // quiz
    }
    val typeLabel = when (lesson.type) {
        "vocab"   -> "Слова"
        "grammar" -> "Грамматика"
        "phrase"  -> "Фразы"
        else      -> "Тест"
    }
    val typeBg = when (lesson.type) {
        "vocab"   -> Color(0xFFE8F5E9)
        "grammar" -> Color(0xFFE3F2FD)
        "phrase"  -> Color(0xFFF3E5F5)
        else      -> Color(0xFFFFF3E0)
    }
    val typeTextColor = when (lesson.type) {
        "vocab"   -> Color(0xFF2E7D32)
        "grammar" -> Color(0xFF0277BD)
        "phrase"  -> Color(0xFF6A1B9A)
        else      -> Color(0xFFE65100)
    }

    Surface(
        onClick   = if (!isLocked) onClick else {{}},
        modifier  = Modifier.fillMaxWidth(),
        color     = if (isLocked) Color(0xFFF7F7F7) else Color(0xFFFAFAFF),
        shape     = RoundedCornerShape(14.dp),
        shadowElevation = if (isLocked) 0.dp else 1.dp
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круг с номером / галочкой
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            lesson.isCompleted -> unitColor
                            isLocked           -> Color(0xFFE0E0E0)
                            else               -> unitColor.copy(alpha = 0.12f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.isCompleted) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text       = "$number",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isLocked) LockGray else unitColor
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Название урока
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = lesson.title,
                    fontSize   = 14.sp,
                    fontWeight = if (lesson.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color      = if (isLocked) TextGray.copy(.55f) else TextMain,
                    maxLines   = 2,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.width(10.dp))

            // Тип урока — цветной чип
            if (!isLocked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "$typeEmoji $typeLabel",
                        fontSize  = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color     = typeTextColor
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            // Правый значок
            when {
                isLocked           -> Icon(Icons.Default.Lock, null, tint = LockGray, modifier = Modifier.size(15.dp))
                lesson.isCompleted -> {} // галочка уже в круге
                else               -> Icon(Icons.Default.ChevronRight, null, tint = unitColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}
