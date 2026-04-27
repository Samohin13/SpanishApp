package com.spanishapp.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val title: String,
    val description: String,
    val icon: String,
    val isLocked: Boolean = false,
    val progress: Float = 0f,
    val color: Color
)

// ═══════════════════════════════════════════════════════════════
//  HOME SCREEN (ROADMAP) — Путь обучения
// ═══════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state     by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic    = LocalHapticFeedback.current

    // Пример данных дорожной карты (в будущем можно вынести в ViewModel)
    val roadmapUnits = listOf(
        RoadmapUnit("Введение", "Основы и приветствия", "👋", false, 1f, MaterialTheme.colorScheme.primary),
        RoadmapUnit("Семья", "Кто есть кто", "🏠", false, 0.4f, MaterialTheme.colorScheme.secondary),
        RoadmapUnit("Ресторан", "Заказ еды и напитков", "🥘", true, 0f, MaterialTheme.colorScheme.tertiary),
        RoadmapUnit("Путешествие", "В аэропорту и отеле", "✈️", true, 0f, MaterialTheme.colorScheme.primary),
        RoadmapUnit("Работа", "Профессии и офис", "💼", true, 0f, MaterialTheme.colorScheme.secondary)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { 
            SpanishBottomBar(
                currentRoute = "home", 
                onNavigate = { navController.navigate(it) }
            ) 
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── ХЕДЕР С ПРОГРЕССОМ ──────────────────────────────
            item {
                HomeHeader(
                    displayName = state.displayName,
                    onProfileClick = { navController.navigate("profile") }
                )
            }

            // ── ДОРОЖНАЯ КАРТА (ROADMAP) ────────────────────────
            itemsIndexed(roadmapUnits) { index, unit ->
                RoadmapNode(
                    unit = unit,
                    isFirst = index == 0,
                    isLast = index == roadmapUnits.size - 1,
                    onNodeClick = {
                        if (!unit.isLocked) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("flashcards") 
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    displayName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "¡Hola, ${if (displayName.isNotEmpty()) displayName else "Estudiante"}!",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Твой путь к испанскому языку",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Surface(
            modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onProfileClick),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null)
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
    val primaryColor = if (unit.isLocked) MaterialTheme.colorScheme.outlineVariant else unit.color
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Линия и точка
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            // Верхняя линия
            if (!isFirst) {
                Canvas(modifier = Modifier.height(20.dp).width(2.dp)) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.5f),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 4.dp.toPx(),
                        pathEffect = if (unit.isLocked) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                    )
                }
            } else {
                Spacer(Modifier.height(20.dp))
            }

            // Круг с иконкой
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(if (unit.isLocked) 0.dp else 8.dp, CircleShape, spotColor = primaryColor)
                    .clip(CircleShape)
                    .background(if (unit.isLocked) MaterialTheme.colorScheme.surfaceVariant else primaryColor)
                    .clickable(enabled = !unit.isLocked, onClick = onNodeClick),
                contentAlignment = Alignment.Center
            ) {
                if (unit.isLocked) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                } else {
                    Text(unit.icon, fontSize = 28.sp)
                }
                
                // Прогресс вокруг круга
                if (!unit.isLocked && unit.progress < 1f) {
                    CircularProgressIndicator(
                        progress = { unit.progress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White.copy(alpha = 0.8f),
                        strokeWidth = 3.dp,
                    )
                }
            }

            // Нижняя линия
            if (!isLast) {
                Canvas(modifier = Modifier.height(60.dp).width(2.dp)) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.5f),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 4.dp.toPx(),
                        pathEffect = if (unit.isLocked) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // Карточка описания
        Card(
            modifier = Modifier
                .padding(top = 10.dp)
                .weight(1f)
                .clickable(enabled = !unit.isLocked, onClick = onNodeClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (unit.isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(if (unit.isLocked) 0.dp else 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    unit.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (unit.isLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    unit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!unit.isLocked) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (unit.progress >= 1f) "Завершено ✨" else "Продолжить →",
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
