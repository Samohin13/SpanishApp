package com.spanishapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.AchievementDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.domain.algorithm.XpSystem
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────

data class ProfileUiState(
    val progress: UserProgressEntity = UserProgressEntity(),
    val learnedCount: Int = 0,
    val unlockedAchievements: Int = 0,
    val totalAchievements: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val wordDao: WordDao,
    private val achievementDao: AchievementDao
) : ViewModel() {

    val state: StateFlow<ProfileUiState> = combine(
        userProgressDao.getProgress(),
        wordDao.learnedCount(),
        achievementDao.unlockedCount(),
        achievementDao.getAll().map { it.size }
    ) { progress, learned, unlocked, total ->
        ProfileUiState(
            progress             = progress ?: UserProgressEntity(),
            learnedCount         = learned,
            unlockedAchievements = unlocked,
            totalAchievements    = total
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val p = state.progress

    val appLevel  = XpSystem.levelForXp(p.totalXp)
    val progress  = XpSystem.progressToNextLevel(p.totalXp)
    val xpForNext = XpSystem.xpForNextLevel(p.totalXp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Avatar + name ────────────────────────────────
            AvatarCard(
                name         = p.displayName.ifBlank { "Estudiante" },
                level        = p.currentLevel,
                appLevel     = appLevel,
                totalXp      = p.totalXp,
                progress     = progress,
                xpForNext    = xpForNext
            )

            // ── Streak ──────────────────────────────────────
            StreakCard(current = p.currentStreak, longest = p.longestStreak)

            // ── Stats grid ──────────────────────────────────
            StatsGrid(
                wordsLearned    = state.learnedCount,
                totalMinutes    = p.totalStudyMinutes,
                lessonsCompleted= p.lessonsCompleted,
                achievementsUnlocked = state.unlockedAchievements,
                totalAchievements    = state.totalAchievements
            )

            // ── Navigate to achievements ─────────────────────
            FilledTonalButton(
                onClick = { navController.navigate("achievements") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("🏅  Все достижения (${state.unlockedAchievements}/${state.totalAchievements})")
            }
        }
    }
}

// ── Components ────────────────────────────────────────────────

@Composable
private fun AvatarCard(
    name: String, level: String, appLevel: Int,
    totalXp: Int, progress: Float, xpForNext: Int
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.Terracotta.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                   modifier = Modifier.fillMaxWidth()) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AppColors.Terracotta.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text("👤", fontSize = 40.sp) }

                Spacer(Modifier.height(10.dp))
                Text(name, style = MaterialTheme.typography.headlineSmall,
                     fontWeight = FontWeight.Bold)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    LevelPill(level)
                    Text("Уровень $appLevel",
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("· $totalXp XP",
                         style = MaterialTheme.typography.bodySmall,
                         color = AppColors.Gold,
                         fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AppColors.Gold,
                    trackColor = AppColors.Gold.copy(alpha = 0.15f)
                )
                Text(
                    "До следующего уровня: $xpForNext XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StreakCard(current: Int, longest: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.Gold.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StreakItem("🔥", "$current дн.", "Текущий стрик")
            VerticalDivider(modifier = Modifier.height(48.dp))
            StreakItem("🏆", "$longest дн.", "Рекорд")
        }
    }
}

@Composable
private fun StreakItem(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(icon, fontSize = 28.sp)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatsGrid(
    wordsLearned: Int, totalMinutes: Int,
    lessonsCompleted: Int, achievementsUnlocked: Int, totalAchievements: Int
) {
    val items = listOf(
        Triple("📚", "$wordsLearned", "слов выучено"),
        Triple("⏱", "${totalMinutes} мин", "занятий"),
        Triple("📖", "$lessonsCompleted", "уроков"),
        Triple("🏅", "$achievementsUnlocked/$totalAchievements", "ачивок")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (icon, value, label) ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 1.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(icon, fontSize = 24.sp)
                            Text(value, style = MaterialTheme.typography.titleMedium,
                                 fontWeight = FontWeight.Bold)
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelPill(level: String) {
    val color = when (level) {
        "A1" -> AppColors.Teal; "A2" -> AppColors.Info
        "B1" -> AppColors.Gold; "B2" -> AppColors.Terracotta
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
        Text(level, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
             style = MaterialTheme.typography.labelMedium,
             color = color, fontWeight = FontWeight.Bold)
    }
}
