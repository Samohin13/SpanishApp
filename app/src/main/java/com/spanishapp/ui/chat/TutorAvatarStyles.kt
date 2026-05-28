package com.spanishapp.ui.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Pro-уровень аватарки наставника — Material иконка + фирменный градиент.
 * Заменяет emoji-аватарки. Стиль ESPEAK: оранжево-тёплая палитра
 * с акцентами разных характеров (огонь / звезда / книга / эко / магия).
 *
 * Каждый стиль = иконка + 2-цветный градиент.
 * Юзер выбирает один в setup-диалоге, ID сохраняется в DataStore.
 */
data class TutorAvatarStyle(
    val id: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val label: String,
)

object TutorAvatarStyles {

    val STYLES = listOf(
        TutorAvatarStyle(
            id = "scholar",
            icon = Icons.Default.School,
            gradient = listOf(Color(0xFFFF8A3D), Color(0xFFFF5722)),
            label = "Учёный",
        ),
        TutorAvatarStyle(
            id = "star",
            icon = Icons.Default.Star,
            gradient = listOf(Color(0xFFFFB85C), Color(0xFFFF9000)),
            label = "Звезда",
        ),
        TutorAvatarStyle(
            id = "fire",
            icon = Icons.Default.LocalFireDepartment,
            gradient = listOf(Color(0xFFFF6B35), Color(0xFFE63946)),
            label = "Огонь",
        ),
        TutorAvatarStyle(
            id = "book",
            icon = Icons.Default.MenuBook,
            gradient = listOf(Color(0xFFD9A56B), Color(0xFFA0522D)),
            label = "Книжник",
        ),
        TutorAvatarStyle(
            id = "translate",
            icon = Icons.Default.Translate,
            gradient = listOf(Color(0xFF4EA1FF), Color(0xFF1E40AF)),
            label = "Переводчик",
        ),
        TutorAvatarStyle(
            id = "psychology",
            icon = Icons.Default.Psychology,
            gradient = listOf(Color(0xFFA78BFA), Color(0xFF6D28D9)),
            label = "Мудрец",
        ),
        TutorAvatarStyle(
            id = "rocket",
            icon = Icons.Default.RocketLaunch,
            gradient = listOf(Color(0xFFFF8A3D), Color(0xFFB91C1C)),
            label = "Ракета",
        ),
        TutorAvatarStyle(
            id = "lightbulb",
            icon = Icons.Default.Lightbulb,
            gradient = listOf(Color(0xFFFCD34D), Color(0xFFD97706)),
            label = "Идея",
        ),
        TutorAvatarStyle(
            id = "diamond",
            icon = Icons.Default.Diamond,
            gradient = listOf(Color(0xFF67E8F9), Color(0xFF0E7490)),
            label = "Бриллиант",
        ),
        TutorAvatarStyle(
            id = "spa",
            icon = Icons.Default.Spa,
            gradient = listOf(Color(0xFF86EFAC), Color(0xFF15803D)),
            label = "Дзен",
        ),
        TutorAvatarStyle(
            id = "eco",
            icon = Icons.Default.Eco,
            gradient = listOf(Color(0xFFA3E635), Color(0xFF3F6212)),
            label = "Эко",
        ),
        TutorAvatarStyle(
            id = "pets",
            icon = Icons.Default.Pets,
            gradient = listOf(Color(0xFFFCA5A5), Color(0xFFB91C1C)),
            label = "Друг",
        ),
        TutorAvatarStyle(
            id = "bolt",
            icon = Icons.Default.Bolt,
            gradient = listOf(Color(0xFFFEF08A), Color(0xFFCA8A04)),
            label = "Молния",
        ),
        TutorAvatarStyle(
            id = "magic",
            icon = Icons.Default.AutoAwesome,
            gradient = listOf(Color(0xFFE0AAFF), Color(0xFF7E22CE)),
            label = "Магия",
        ),
        TutorAvatarStyle(
            id = "moon",
            icon = Icons.Default.NightlightRound,
            gradient = listOf(Color(0xFF94A3B8), Color(0xFF1E293B)),
            label = "Луна",
        ),
        TutorAvatarStyle(
            id = "public",
            icon = Icons.Default.Public,
            gradient = listOf(Color(0xFF5EEAD4), Color(0xFF0F766E)),
            label = "Мир",
        ),
    )

    val DEFAULT_ID = "scholar"

    fun byId(id: String?): TutorAvatarStyle =
        STYLES.firstOrNull { it.id == id }
            // Backwards-compat: старые emoji-юзеры → default scholar
            ?: STYLES.first { it.id == DEFAULT_ID }
}
