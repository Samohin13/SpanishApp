package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

private val LibrosPurple = Color(0xFF7B2FBE)
private val LevelColors = mapOf(
    "A1" to Color(0xFF43A047),
    "A2" to Color(0xFF1E88E5),
    "B1" to Color(0xFFE65100),
    "B2" to Color(0xFF6A1B9A)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrosScreen(
    navController: NavHostController,
    vm: LibrosViewModel = hiltViewModel()
) {
    val items by vm.filteredItems.collectAsStateWithLifecycle()
    val filter by vm.filterLevel.collectAsStateWithLifecycle()
    val allItems by vm.items.collectAsStateWithLifecycle()

    val readCount = allItems.count { it.isCompleted }
    val totalCount = allItems.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Libros 📚", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "Прочитано: $readCount / $totalCount",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F8FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Фильтр по уровню
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Все", "A1", "A2", "B1", "B2")
                    items(filters) { level ->
                        val selected = filter == level
                        val color = if (level == "Все") LibrosPurple
                                    else LevelColors[level] ?: LibrosPurple
                        FilterChip(
                            selected = selected,
                            onClick = { vm.setFilter(level) },
                            label = { Text(level, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Рассказы уровня $filter\nпоявятся в следующем обновлении",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                items(items, key = { it.libro.id }) { item ->
                    LibroCard(
                        item = item,
                        onClick = { navController.navigate("libro/${item.libro.id}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibroCard(item: LibroUiItem, onClick: () -> Unit) {
    val libro = item.libro
    val levelColor = LevelColors[libro.level] ?: LibrosPurple

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(levelColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#${libro.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = levelColor
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        libro.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    if (item.isCompleted) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge уровня
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(levelColor)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(libro.level, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    // Точки сложности
                    DifficultyDots(libro.difficulty)
                    Spacer(Modifier.width(8.dp))
                    Text(libro.topic, fontSize = 12.sp, color = Color(0xFF8E8E93))
                }
                if (item.isCompleted) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Лучший результат: ${item.bestScore}%",
                        fontSize = 11.sp,
                        color = Color(0xFF43A047)
                    )
                }
            }

            // Кнопка
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (item.isCompleted) Color(0xFFE8F5E9) else levelColor)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    if (item.isCompleted) "Повторить" else "Читать",
                    color = if (item.isCompleted) Color(0xFF2E7D32) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun DifficultyDots(difficulty: Int, size: Int = 10) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(5) { index ->
            Box(
                Modifier
                    .size(size.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        if (index < difficulty) Color(0xFFE53935)
                        else Color(0xFFE0E0E0)
                    )
            )
        }
    }
}
