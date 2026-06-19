package com.spanishapp.ui.theory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.data.theory.TheoryContent
import com.spanishapp.data.theory.TheoryContentData

/**
 * Библиотека всех написанных теорий — открывается из Profile «📖 Теория».
 *
 * Группирует теории по CEFR-уровню. Помечает те что уже прочитаны.
 * Тап → открывает TheoryReaderScreen для конкретного lessonId.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryLibraryScreen(
    navController: NavHostController,
    vm: TheoryLibraryViewModel = hiltViewModel(),
) {
    val readIds by vm.readLessonIds.collectAsStateWithLifecycle()
    val all = remember { TheoryContentData.all() }
    val byLevel = remember(all) { all.groupBy { it.cefr } }
    val levelOrder = listOf("A1", "A2", "B1", "B2")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("📖 Теория", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(
                            "${all.size} карточек · ${readIds.size} прочитано",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        }
    ) { padding ->
        if (all.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Теории появятся здесь по мере добавления.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            levelOrder.forEach { level ->
                val cards = byLevel[level].orEmpty()
                if (cards.isEmpty()) return@forEach

                item(key = "header_$level") {
                    // v1.15.0 P1: цвет уровня из единой палитры CefrColors
                    val levelColor = com.spanishapp.ui.theme.CefrColors.forLevel(level)
                    Text(
                        "Уровень $level · ${cards.size} карточек",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }

                // v1.25.90: PRO gate. A1 = free, A2/B1/B2 = PRO.
                // Uses the same rememberIsProState() helper as games + grammar.
                val isPro by com.spanishapp.ui.games.common.rememberIsProState()
                items(cards, key = { it.lessonId }) { card ->
                    val locked = card.cefr != "A1" && !isPro
                    TheoryLibraryCard(
                        card = card,
                        isRead = card.lessonId in readIds,
                        locked = locked,
                        onClick = {
                            if (locked) {
                                navController.navigate("paywall") { launchSingleTop = true }
                            } else {
                                navController.navigate("theory/${card.lessonId}")
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TheoryLibraryCard(
    card: TheoryContent,
    isRead: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isRead)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isRead) Color(0xFF4CAF50).copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(card.emoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    card.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (card.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        card.subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱ ${card.readMinutes} мин", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    when {
                        locked -> {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFF9500),
                                modifier = Modifier.size(10.dp),
                            )
                            Spacer(Modifier.width(2.dp))
                            Text("PRO", fontSize = 10.sp, color = Color(0xFFFF9500), fontWeight = FontWeight.Bold)
                        }
                        isRead -> {
                            Text("✅ прочитано", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                        }
                        else -> {
                            Text("· ${card.lessonId}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            Text("→", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
