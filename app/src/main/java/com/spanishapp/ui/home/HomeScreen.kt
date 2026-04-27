package com.spanishapp.ui.home

import androidx.compose.animation.*
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
//  HOME SCREEN (ROADMAP) — Современный путь обучения
// ═══════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state     by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic    = LocalHapticFeedback.current
    
    // Храним ID развернутого блока
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

    SpanishBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HomeTopBar(
                    xp = state.totalXp,
                    streak = state.currentStreak,
                    onProfileClick = { navController.navigate("profile") }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(roadmapUnits) { index, unit ->
                        RoadmapNode(
                            unit = unit,
                            isFirst = index == 0,
                            isLast = index == roadmapUnits.size - 1,
                            isExpanded = expandedUnitId == unit.id,
                            onToggleExpand = {
                                if (!unit.isLocked) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    expandedUnitId = if (expandedUnitId == unit.id) null else unit.id
                                }
                            },
                            onStartLesson = { lesson ->
                                navController.navigate("lesson_intro/${lesson.title}/${lesson.type}?category=${lesson.category}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    xp: Int,
    streak: Int,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(56.dp)
            .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TopBarStat(icon = "✨", value = "$xp", color = Color(0xFFF9A825))
                TopBarStat(icon = "🔥", value = "$streak", color = Color(0xFFC62828))
            }

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TopBarStat(icon: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(color.copy(alpha = 0.08f), RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(icon, fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
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
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isFirst) {
            Box(modifier = Modifier.height(16.dp).width(3.dp).background(accentColor.copy(alpha = 0.2f), CircleShape))
        }

        // Основная карточка блока
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .shadow(if (isExpanded) 16.dp else 4.dp, RoundedCornerShape(28.dp), spotColor = accentColor.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(28.dp),
            color = if (unit.isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.White,
            border = if (isExpanded) borderStroke(2.dp, accentColor.copy(alpha = 0.5f)) else null,
            onClick = onToggleExpand
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Круглая иконка с прогрессом
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (unit.isLocked) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        } else {
                            Text(unit.icon, fontSize = 32.sp)
                            CircularProgressIndicator(
                                progress = { unit.progress },
                                modifier = Modifier.fillMaxSize(),
                                color = accentColor,
                                strokeWidth = 5.dp,
                                trackColor = accentColor.copy(alpha = 0.1f)
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("BLOQUE ${unit.id}", style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.ExtraBold)
                        Text(unit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text(unit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }

                    // Стрелочка разворота
                    if (!unit.isLocked) {
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Список уроков (раскрывающийся)
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))
                        unit.lessons.forEach { lesson ->
                            LessonRow(lesson = lesson, color = unit.color, onClick = { onStartLesson(lesson) })
                        }
                    }
                }
            }
        }

        if (!isLast) {
            Box(modifier = Modifier.height(16.dp).width(3.dp).background(accentColor.copy(alpha = 0.2f), CircleShape))
        }
    }
}

@Composable
private fun LessonRow(lesson: RoadmapLesson, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when(lesson.type) {
                "vocab" -> Icons.Default.MenuBook
                "grammar" -> Icons.Default.Extension
                "phrase" -> Icons.Default.ChatBubble
                else -> Icons.Default.Quiz
            }
            
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(if (lesson.isCompleted) color else Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (lesson.isCompleted) Icons.Default.Check else icon,
                    null,
                    tint = if (lesson.isCompleted) Color.White else color,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(lesson.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Icon(Icons.Default.PlayArrow, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
