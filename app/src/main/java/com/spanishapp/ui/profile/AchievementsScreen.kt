package com.spanishapp.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.AchievementDao
import com.spanishapp.data.db.entity.AchievementEntity
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Достижение считается «свежим», если разблокировано за последние сутки.
private const val FRESH_UNLOCK_WINDOW_MS = 24L * 60L * 60L * 1000L

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val dao: AchievementDao
) : ViewModel() {

    val achievements: StateFlow<List<AchievementEntity>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    navController: NavHostController,
    vm: AchievementsViewModel = hiltViewModel()
) {
    val achievements by vm.achievements.collectAsStateWithLifecycle()

    // Свежеразблокированные — наверх среди unlocked.
    val now = remember(achievements) { System.currentTimeMillis() }
    val unlocked = achievements
        .filter { it.isUnlocked }
        .sortedByDescending { it.unlockedAt }
    val locked   = achievements.filter { !it.isUnlocked }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        androidx.compose.ui.res.stringResource(com.spanishapp.R.string.title_achievements),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        // v1.12.3: grid 1/2/3 колонки. Achievement cards одной высоты —
        // отлично смотрятся в grid на планшете.
        val cols = com.spanishapp.ui.adaptive.adaptiveColumns(compact = 1, medium = 2, expanded = 3)
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── Summary ──────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                AchievementSummary(
                    unlocked = unlocked.size,
                    total    = achievements.size,
                    totalXp  = unlocked.sumOf { it.xpReward }
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Unlocked ─────────────────────────────────────
            if (unlocked.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionLabel(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.achievements_section_unlocked, unlocked.size))
                }
                gridItemsIndexed(
                    items = unlocked,
                    key = { _, a -> a.id }
                ) { index, a ->
                    val isFresh = a.unlockedAt > 0 && (now - a.unlockedAt) < FRESH_UNLOCK_WINDOW_MS
                    AnimatedAchievementCard(
                        achievement = a,
                        unlocked    = true,
                        isFresh     = isFresh,
                        index       = index
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
            }

            // ── Locked ───────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionLabel(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.achievements_section_locked, locked.size))
            }
            gridItemsIndexed(
                items = locked,
                key = { _, a -> a.id }
            ) { index, a ->
                AnimatedAchievementCard(
                    achievement = a,
                    unlocked    = false,
                    isFresh     = false,
                    index       = unlocked.size + index
                )
            }
        }
    }
}

// ── Composables ───────────────────────────────────────────────

@Composable
private fun AchievementSummary(unlocked: Int, total: Int, totalXp: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("🏅", "$unlocked / $total", androidx.compose.ui.res.stringResource(com.spanishapp.R.string.achievements_summary_count))
            SummaryItem("⭐", "+$totalXp", androidx.compose.ui.res.stringResource(com.spanishapp.R.string.achievements_summary_xp_label))
            val pct = if (total > 0) (unlocked * 100 / total) else 0
            SummaryItem("📊", "$pct%", androidx.compose.ui.res.stringResource(com.spanishapp.R.string.achievements_summary_progress))
        }
    }
}

@Composable
private fun SummaryItem(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 24.sp)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

/**
 * Карточка с анимацией появления (slide-in справа + fade).
 * Свежие достижения дополнительно мягко пульсируют.
 */
@Composable
private fun AnimatedAchievementCard(
    achievement: AchievementEntity,
    unlocked: Boolean,
    isFresh: Boolean,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(achievement.id) {
        // Шахматный порядок появления — каждая карточка с лёгкой задержкой.
        kotlinx.coroutines.delay(40L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(animationSpec = tween(300)) { it / 4 } +
                fadeIn(animationSpec = tween(300)) +
                scaleIn(initialScale = 0.95f, animationSpec = tween(300))
    ) {
        AchievementCard(
            a = achievement,
            unlocked = unlocked,
            isFresh = isFresh
        )
    }
}

@Composable
private fun AchievementCard(a: AchievementEntity, unlocked: Boolean, isFresh: Boolean = false) {
    // 1.1.0: единая семантика — все достижения = кубок 🏆.
    // Иерархия (бронза/серебро/золото) определяется через xpReward,
    // не через разные эмодзи — раньше каша иконок (🔥/📚/💬/⭐) сбивала
    // юзера с толку, не было понятно что важнее.
    val icon = "🏆"
    val tier = when {
        a.xpReward >= 80 -> AchievementTier.GOLD
        a.xpReward >= 20 -> AchievementTier.SILVER
        else             -> AchievementTier.BRONZE
    }
    val tierColor = when (tier) {
        AchievementTier.BRONZE -> Color(0xFFCD7F32)   // bronze
        AchievementTier.SILVER -> Color(0xFFB0B0B5)   // silver-grey
        AchievementTier.GOLD   -> AppColors.Gold      // gold (brand)
    }

    // Лёгкая пульсация иконки для свежих достижений.
    val iconScale: Float = if (isFresh) {
        val transition = rememberInfiniteTransition(label = "fresh-pulse")
        val animated by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fresh-pulse"
        )
        animated
    } else 1f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when {
            isFresh  -> AppColors.Gold.copy(alpha = 0.18f)
            unlocked -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else     -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        shadowElevation = if (unlocked) 0.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon box — фон цвета tier (bronze/silver/gold)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (unlocked) tierColor.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.scale(iconScale)
            ) {
                Text(
                    if (unlocked) icon else "🔒",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        a.titleRu,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (unlocked) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isFresh) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppColors.Gold
                        ) {
                            Text(
                                "NEW",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    a.descriptionRu,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // XP badge — цвет tier для unlocked
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (unlocked) tierColor.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "+${a.xpReward} XP",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) tierColor
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private enum class AchievementTier { BRONZE, SILVER, GOLD }
