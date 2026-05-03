package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun GamesScreen(navController: NavHostController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8FA))
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Juegos 🎮",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                "Адаптивная система обучения в игровом формате",
                fontSize = 16.sp,
                color = Color(0xFF8E8E93)
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            GameCard(
                title = "Artículos",
                description = "Артикли El/La",
                icon = Icons.Default.Category,
                color = Color(0xFF7B2FBE),
                onClick = { navController.navigate("game_articles") }
            )
        }

        item {
            GameCard(
                title = "Rápido",
                description = "Скоростной перевод на время",
                icon = Icons.Default.Timer,
                color = Color(0xFFE040FB),
                onClick = { navController.navigate("game_speed") }
            )
        }

        item {
            GameCard(
                title = "Verbos",
                description = "Профессиональный тренажер спряжений",
                icon = Icons.Default.Translate,
                color = Color(0xFF2196F3),
                onClick = { navController.navigate("conjugation_quiz") }
            )
        }

        item {
            GameCard(
                title = "Sopa de Letras",
                description = "Филворд / Суп из букв",
                icon = Icons.Default.GridOn,
                color = Color(0xFF4CAF50),
                onClick = { navController.navigate("game_sopa") }
            )
        }

        item {
            GameCard(
                title = "Palabra Maestra",
                description = "Мастер слова / Орфография",
                icon = Icons.Default.TextFields,
                color = Color(0xFFFF9500),
                onClick = { navController.navigate("game_palabra") }
            )
        }

        item {
            GameCard(
                title = "Cálculo Auditivo",
                description = "Математический тренажер / Счёт",
                icon = Icons.Default.Calculate,
                color = Color(0xFFF44336),
                onClick = { navController.navigate("game_math") }
            )
        }

        item {
            GameCard(
                title = "Crucigrama",
                description = "Интеллектуальный классический кроссворд",
                icon = Icons.Default.BorderAll,
                color = Color(0xFF26A69A),
                onClick = { navController.navigate("game_crossword") }
            )
        }

        item {
            GameCard(
                title = "Libros",
                description = "Адаптированные рассказы с тестами по уровням",
                icon = Icons.Default.MenuBook,
                color = Color(0xFF7B2FBE),
                onClick = { navController.navigate("game_libros") }
            )
        }
    }
}

@Composable
private fun GameCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    description,
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93),
                    lineHeight = 18.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = Color(0xFFC7C7CC),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
