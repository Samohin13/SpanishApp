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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state     by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic    = LocalHapticFeedback.current
    
    var selectedUnit by remember { mutableStateOf<RoadmapUnit?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

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
                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(roadmapUnits) { index, unit ->
                    RoadmapNode(
                        unit = unit,
                        isFirst = index == 0,
                        isLast = index == roadmapUnits.size - 1,
                        onNodeClick = {
                            if (!unit.isLocked) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedUnit = unit
                                showSheet = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (showSheet && selectedUnit != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
        ) {
            UnitDetailsContent(
                unit = selectedUnit!!,
                onStartLesson = { lesson ->
                    showSheet = false
                    navController.navigate("lesson_intro/${lesson.title}/${lesson.type}?category=${lesson.category}")
                }
            )
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
    onNodeClick: () -> Unit
) {
    val accentColor = if (unit.isLocked) MaterialTheme.colorScheme.outlineVariant else unit.color
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isFirst) {
            Box(modifier = Modifier.height(24.dp).width(3.dp).background(accentColor.copy(alpha = 0.2f), CircleShape))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().clickable(enabled = !unit.isLocked, onClick = onNodeClick),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(if (unit.isLocked) 0.dp else 12.dp, CircleShape, spotColor = accentColor.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(if (unit.isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.White)
                    .border(2.dp, if (unit.isLocked) Color.Transparent else accentColor.copy(alpha = 0.3f), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (unit.isLocked) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                } else {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(accentColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Text(unit.icon, fontSize = 38.sp)
                    }
                    CircularProgressIndicator(
                        progress = { unit.progress },
                        modifier = Modifier.fillMaxSize(),
                        color = accentColor,
                        strokeWidth = 6.dp,
                        trackColor = accentColor.copy(alpha = 0.1f)
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("BLOQUE ${unit.id}", style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text(unit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = if (unit.isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface)
                Text(unit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }

        if (!isLast) {
            Box(modifier = Modifier.height(32.dp).width(3.dp).background(accentColor.copy(alpha = 0.2f), CircleShape))
        }
    }
}

@Composable
private fun UnitDetailsContent(
    unit: RoadmapUnit,
    onStartLesson: (RoadmapLesson) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(unit.color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text(unit.icon, fontSize = 32.sp)
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Раздел ${unit.id}", style = MaterialTheme.typography.labelMedium, color = unit.color, fontWeight = FontWeight.Bold)
                Text(unit.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("СОДЕРЖАНИЕ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))
        unit.lessons.forEach { lesson ->
            LessonItem(lesson = lesson, color = unit.color, onClick = { onStartLesson(lesson) })
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LessonItem(lesson: RoadmapLesson, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val (icon, typeName) = when(lesson.type) {
                "vocab" -> Icons.Default.MenuBook to "СЛОВА"
                "grammar" -> Icons.Default.Extension to "ГРАММАТИКА"
                "phrase" -> Icons.Default.ChatBubble to "ФРАЗЫ"
                else -> Icons.Default.Quiz to "ТЕСТ"
            }
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(if (lesson.isCompleted) color else Color.White), contentAlignment = Alignment.Center) {
                Icon(if (lesson.isCompleted) Icons.Default.Check else icon, null, tint = if (lesson.isCompleted) Color.White else color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(typeName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
            }
            Icon(Icons.Default.PlayArrow, null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}
