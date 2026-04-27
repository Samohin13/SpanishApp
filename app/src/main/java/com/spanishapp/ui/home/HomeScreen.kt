package com.spanishapp.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
    val type: String, // "vocab", "grammar", "quiz"
    val category: String = "general",
    val isCompleted: Boolean = false
)

// ═══════════════════════════════════════════════════════════════
//  HOME SCREEN (ROADMAP) — Путь обучения
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

    // РЕАЛЬНЫЙ КОНТЕНТ ДЛЯ A1
    val roadmapUnits = remember(state.learnedCount, state.totalXp) {
        listOf(
            RoadmapUnit("1", "¡Hola!", "Приветствия и база", "👋", false, (state.learnedCount / 10f).coerceAtMost(1f), Color(0xFF4CAF50), 
                listOf(
                    RoadmapLesson("Первые слова", "vocab", "familia_personas", state.learnedCount > 5),
                    RoadmapLesson("Артикли: el, la", "grammar", "general", state.totalXp > 50),
                    RoadmapLesson("Тест: Основы", "quiz", "general", state.totalXp > 100)
                )),
            RoadmapUnit("2", "Mi Familia", "Семья и описание", "🏠", state.learnedCount < 10, (state.learnedCount / 30f).coerceAtMost(1f), Color(0xFFFF9800),
                listOf(
                    RoadmapLesson("Члены семьи", "vocab", "familia_personas"),
                    RoadmapLesson("Глагол Ser", "grammar", "verbs"),
                    RoadmapLesson("Тест: Семья", "quiz", "familia_personas")
                )),
            RoadmapUnit("3", "En el Café", "Заказ еды", "☕", state.learnedCount < 25, 0f, Color(0xFFE91E63),
                listOf(
                    RoadmapLesson("Еда и напитки", "vocab", "comida_bebida"),
                    RoadmapLesson("Глагол Estar", "grammar", "verbs"),
                    RoadmapLesson("Диалог: В кафе", "quiz", "comida_bebida")
                )),
            RoadmapUnit("4", "De Compras", "Шоппинг и одежда", "🛍️", true, 0f, Color(0xFF2196F3)),
            RoadmapUnit("5", "Mi Rutina", "Мой день", "⏰", true, 0f, Color(0xFF9C27B0))
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { 
            SpanishBottomBar(
                currentRoute = "home", 
                onNavigate = { navController.navigate(it) }
            ) 
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    HomeHeader(
                        displayName = state.displayName,
                        totalXp = state.totalXp,
                        onProfileClick = { navController.navigate("profile") }
                    )
                }

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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, Color.Transparent)))
            )
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
                    // Передаем параметры в intro, чтобы потом запустить нужную категорию
                    navController.navigate("lesson_intro/${lesson.title}/${lesson.type}?category=${lesson.category}")
                }
            )
        }
    }
}

@Composable
private fun HomeHeader(
    displayName: String,
    totalXp: Int,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "¡Hola, ${if (displayName.isNotEmpty()) displayName else "Estudiante"}!",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Stars, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$totalXp XP • Уровень A1",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Surface(
                modifier = Modifier.size(52.dp).clip(CircleShape).clickable(onClick = onProfileClick),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (displayName.isNotEmpty()) displayName[0].toString() else "E", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }
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
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(80.dp)
        ) {
            if (!isFirst) {
                Box(modifier = Modifier.height(30.dp).width(4.dp).background(accentColor.copy(alpha = 0.3f), CircleShape))
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(if (unit.isLocked) 0.dp else 12.dp, CircleShape, spotColor = accentColor)
                    .clip(CircleShape)
                    .background(if (unit.isLocked) MaterialTheme.colorScheme.surfaceVariant else accentColor)
                    .clickable(enabled = !unit.isLocked, onClick = onNodeClick),
                contentAlignment = Alignment.Center
            ) {
                if (unit.isLocked) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                } else {
                    Text(unit.icon, fontSize = 32.sp)
                    CircularProgressIndicator(
                        progress = { unit.progress },
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                        color = Color.White,
                        strokeWidth = 4.dp,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }

            if (!isLast) {
                Box(modifier = Modifier.height(50.dp).width(4.dp).background(accentColor.copy(alpha = 0.3f), CircleShape))
            }
        }

        Column(
            modifier = Modifier
                .padding(top = 12.dp)
                .weight(1f)
        ) {
            Text(
                unit.title,
                style = MaterialTheme.typography.headlineLarge,
                color = if (unit.isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                unit.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UnitDetailsContent(
    unit: RoadmapUnit,
    onStartLesson: (RoadmapLesson) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(unit.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(unit.icon, fontSize = 28.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(unit.title, style = MaterialTheme.typography.headlineLarge)
                Text("Раздел ${unit.id} • ${unit.description}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(32.dp))
        
        Text("УРОКИ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (lesson.isCompleted) null else androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when(lesson.type) {
                "vocab" -> Icons.Default.MenuBook
                "grammar" -> Icons.Default.Extension
                else -> Icons.Default.Quiz
            }
            
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(if (lesson.isCompleted) color else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.isCompleted) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(icon, null, tint = color)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(lesson.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Icon(Icons.Default.PlayArrow, null, tint = color)
        }
    }
}
