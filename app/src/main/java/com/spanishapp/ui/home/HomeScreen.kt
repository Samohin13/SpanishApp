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
//  HOME SCREEN (ROADMAP) — Современный путь обучения
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
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
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
        }
    }
}

@Composable
private fun HomeTopBar(
    xp: Int,
    streak: Int,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .height(52.dp)
                .fillMaxWidth(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TopBarStat(icon = "✨", value = "$xp", color = Color(0xFFF9A825))
                    TopBarStat(icon = "🔥", value = "$streak", color = Color(0xFFC62828))
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(34.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun TopBarStat(icon: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
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
    val activeColor = RoadmapData.units[0].color 

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isFirst) {
            Box(modifier = Modifier.height(16.dp).width(3.dp).background(accentColor.copy(alpha = 0.2f), CircleShape))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .shadow(
                    elevation = if (isExpanded) 16.dp else 4.dp, 
                    shape = RoundedCornerShape(28.dp), 
                    spotColor = if (!unit.isLocked) accentColor.copy(alpha = 0.3f * pulse) else Color.Transparent
                ),
            shape = RoundedCornerShape(28.dp),
            color = if (unit.isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
            border = if (isExpanded) borderStroke(2.dp, accentColor.copy(alpha = 0.4f)) else null,
            onClick = onToggleExpand,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .then(
                                if (!unit.isLocked) {
                                    Modifier.shadow(elevation = 10.dp * pulse, shape = CircleShape, spotColor = accentColor, ambientColor = accentColor)
                                } else Modifier
                            )
                            .clip(CircleShape)
                            .background(if (unit.isLocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (unit.isLocked) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        } else {
                            Text(unit.icon, fontSize = 30.sp)
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
                        Text("РАЗДЕЛ ${unit.id}", style = MaterialTheme.typography.labelSmall, color = activeColor, fontWeight = FontWeight.ExtraBold)
                        Text(unit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text(unit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }

                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(8.dp))
                        unit.lessons.forEach { lesson ->
                            LessonRow(lesson = lesson, color = unit.color, onClick = { onStartLesson(lesson) }, enabled = !unit.isLocked)
                        }
                    }
                }
            }
        }

        if (!isLast) {
            Box(modifier = Modifier.height(24.dp).width(3.dp).background(accentColor.copy(alpha = 0.2f), CircleShape))
        }
    }
}

@Composable
private fun LessonRow(lesson: RoadmapLesson, color: Color, onClick: () -> Unit, enabled: Boolean) {
    Surface(
        onClick = if(enabled) onClick else {{}},
        modifier = Modifier.fillMaxWidth().alpha(if(enabled) 1f else 0.5f),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                modifier = Modifier.size(36.dp).clip(CircleShape).background(if (lesson.isCompleted) color else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (lesson.isCompleted) Icons.Default.Check else icon,
                    null,
                    tint = if (lesson.isCompleted) Color.White else if(enabled) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(lesson.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            if(enabled) {
                Icon(Icons.Default.PlayArrow, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
