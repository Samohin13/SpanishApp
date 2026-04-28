package com.spanishapp.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val isLocked: Boolean = false,
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
//  HOME SCREEN (ROADMAP) — Чистый путь обучения
// ═══════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state     by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic    = LocalHapticFeedback.current
    
    var expandedUnitId by remember { mutableStateOf<String?>(null) }

    val roadmapUnits = remember(state.learnedCount) {
        RoadmapData.units.map { unit ->
            val unitIndex = unit.id.toInt()
            val unlocked = unitIndex == 1 || (state.learnedCount >= (unitIndex - 1) * 8)
            unit.copy(
                isLocked = !unlocked,
                progress = if (unlocked) (state.learnedCount / (unitIndex * 15f)).coerceIn(0f, 1f) else 0f
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(roadmapUnits) { index, unit ->
                RoadmapNode(
                    unit = unit,
                    isFirst = index == 0,
                    isLast = index == roadmapUnits.size - 1,
                    isExpanded = expandedUnitId == unit.id,
                    onToggleExpand = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        expandedUnitId = if (expandedUnitId == unit.id) null else unit.id
                    },
                    onStartLesson = { lesson ->
                        if (!unit.isLocked) {
                            navController.navigate("lesson_intro/${lesson.title}/${lesson.type}?category=${lesson.category}")
                        }
                    }
                )
            }
        }

        // Floating top bar — без фона, поверх контента
        HomeTopBar(
            xp = state.totalXp,
            streak = state.currentStreak,
            onProfileClick = { navController.navigate("profile") }
        )
    }
}

@Composable
private fun HomeTopBar(
    xp: Int,
    streak: Int,
    onProfileClick: () -> Unit
) {
    // Полностью прозрачный верхний бар
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("✨ $xp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF9A825))
            Text("🔥 $streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC62828))
        }

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.size(36.dp).background(Color(0xFF4CAF50).copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Default.Person, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun RoadmapNode(
    unit: RoadmapUnit,
    isFirst: Boolean,
    isLast: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onStartLesson: (RoadmapLesson) -> Unit
) {
    val accentColor = if (unit.isLocked) MaterialTheme.colorScheme.outlineVariant else unit.color
    // Слово РАЗДЕЛ всегда строго ЗЕЛЕНОЕ
    val labelColor = Color(0xFF4CAF50)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isFirst) {
            Box(modifier = Modifier.height(12.dp).width(2.dp).background(accentColor.copy(alpha = 0.2f), CircleShape))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            onClick = onToggleExpand
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (unit.isLocked) MaterialTheme.colorScheme.surfaceVariant else accentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (unit.isLocked) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        } else {
                            Text(unit.icon, fontSize = 28.sp)
                            CircularProgressIndicator(
                                progress = { unit.progress },
                                modifier = Modifier.fillMaxSize(),
                                color = accentColor,
                                strokeWidth = 4.dp,
                                trackColor = accentColor.copy(alpha = 0.1f)
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "РАЗДЕЛ ${unit.id}",
                            style = MaterialTheme.typography.labelMedium,
                            color = labelColor,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            unit.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            unit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            lineHeight = 18.sp
                        )
                    }

                    // Стрелочка ЕСТЬ У ВСЕХ
                    Icon(
                        Icons.Default.ExpandMore,
                        null,
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        unit.lessons.forEach { lesson ->
                            LessonRow(lesson = lesson, color = unit.color, onClick = { onStartLesson(lesson) }, enabled = !unit.isLocked)
                        }
                    }
                }
            }
        }

        if (!isLast) {
            Box(modifier = Modifier.height(12.dp).width(2.dp).background(accentColor.copy(alpha = 0.2f), CircleShape))
        }
    }
}

@Composable
private fun LessonRow(lesson: RoadmapLesson, color: Color, onClick: () -> Unit, enabled: Boolean) {
    Surface(
        onClick = if(enabled) onClick else {{}},
        modifier = Modifier.fillMaxWidth().alpha(if(enabled) 1f else 0.5f),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(if (lesson.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.isCompleted) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    val icon = when(lesson.type) {
                        "vocab"   -> Icons.Default.MenuBook
                        "grammar" -> Icons.Default.Extension
                        "phrase"  -> Icons.Default.ChatBubble
                        else      -> Icons.Default.Quiz
                    }
                    Icon(icon, null, tint = if(enabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    when (lesson.type) {
                        "vocab"   -> "Лексика"
                        "grammar" -> "Грамматика"
                        "phrase"  -> "Фразы"
                        "quiz"    -> "Тест"
                        else      -> lesson.type
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
            
            if (enabled) {
                Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF4CAF50).copy(alpha = 0.8f), modifier = Modifier.size(22.dp))
            }
        }
    }
}
