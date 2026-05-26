package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.spanishapp.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

// v1.14.2: единая палитра CEFR (см. ui/theme/CefrColors.kt).
// Раньше Libros имели свой набор (A1=green, A2=blue, B1=orange,
// B2=purple) — рассинхрон с Flashcards и общим брендом ESPEAK.
// Юзер: "почему эта логика рушится в книгах?"
private val LibrosPurple = com.spanishapp.ui.theme.CefrColors.Default
private val LevelColors = mapOf(
    "A1" to com.spanishapp.ui.theme.CefrColors.A1,
    "A2" to com.spanishapp.ui.theme.CefrColors.A2,
    "B1" to com.spanishapp.ui.theme.CefrColors.B1,
    "B2" to com.spanishapp.ui.theme.CefrColors.B2,
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
                            stringResource(R.string.libros_read_progress, readCount, totalCount),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.libros_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        // v1.12.4: 1/2/2 cols (было 1/2/3 — при 3 cols текст рвался
        // по 3 символа в карточке row-style #N + title + dots + topic).
        val cols = com.spanishapp.ui.adaptive.adaptiveColumns(compact = 1, medium = 2, expanded = 2)
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Фильтр по уровню
            item(span = { GridItemSpan(maxLineSpan) }) {
                val allLabel = stringResource(R.string.libros_filter_all)
                LazyRow(
                    contentPadding = PaddingValues(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("all", "A1", "A2", "B1", "B2")
                    items(filters) { level ->
                        val selected = filter == level
                        val color = if (level == "all") LibrosPurple
                                    else LevelColors[level] ?: LibrosPurple
                        val display = if (level == "all") allLabel else level
                        FilterChip(
                            selected = selected,
                            onClick = { vm.setFilter(level) },
                            label = { Text(display, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.libros_empty_for_level, filter),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                gridItems(items, key = { it.libro.id }) { item ->
                    LibroCard(
                        item = item,
                        onClick = {
                            // v1.23.6: A2/B1/B2 книги для free → paywall.
                            if (item.isProLocked) {
                                navController.navigate("paywall") { launchSingleTop = true }
                            } else {
                                // 1.1.1 fix: дублируем markOpened **до** навигации.
                                vm.markOpened(item.libro.id)
                                navController.navigate("libro/${item.libro.id}")
                            }
                        }
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

    // v1.15.1: ultra-compact (единый стиль с Tarjetas 72dp)
    val isWide = com.spanishapp.ui.adaptive.isWideScreen()
    val cardMinHeight = if (isWide) 68.dp else 72.dp
    val numberSize = if (isWide) 36.dp else 36.dp
    val numberFont = if (isWide) 13.sp else 13.sp
    val titleFont = if (isWide) 14.sp else 14.sp
    val topicFont = if (isWide) 11.sp else 11.sp
    val buttonFont = if (isWide) 11.sp else 11.sp
    val cardPadding = if (isWide) 10.dp else 10.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = cardMinHeight)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер
            Box(
                modifier = Modifier
                    .size(numberSize)
                    .clip(RoundedCornerShape(12.dp))
                    .background(levelColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#${libro.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = numberFont,
                    color = levelColor
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        libro.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFont,
                        color = MaterialTheme.colorScheme.onSurface
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
                    Text(libro.topic, fontSize = topicFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (item.isCompleted) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.libros_best_score, item.bestScore),
                        fontSize = 11.sp,
                        color = Color(0xFF43A047)
                    )
                }
            }

            // Кнопка — для PRO-locked показываем 💎 PRO вместо «Читать»
            val proPrimary = Color(0xFFFF8A3D)
            if (item.isProLocked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(proPrimary)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "💎 PRO",
                        color = Color.White,
                        fontSize = buttonFont,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (item.isCompleted) Color(0xFFE8F5E9) else levelColor)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (item.isCompleted) stringResource(R.string.libros_repeat) else stringResource(R.string.libros_read),
                        color = if (item.isCompleted) Color(0xFF2E7D32) else Color.White,
                        fontSize = buttonFont,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
                        else MaterialTheme.colorScheme.outline
                    )
            )
        }
    }
}
