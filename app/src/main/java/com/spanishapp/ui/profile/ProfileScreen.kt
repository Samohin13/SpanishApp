package com.spanishapp.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val haptic = LocalHapticFeedback.current

    val appLevel  = XpSystem.levelForXp(p.totalXp)
    val progress  = XpSystem.progressToNextLevel(p.totalXp)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = { Text("Мой Профиль", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, null)
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
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Premium Avatar Section ──────────────────────
            ProfileHeader(
                name = p.displayName.ifBlank { "Estudiante" },
                level = p.currentLevel,
                appLevel = appLevel,
                progress = progress
            )

            Spacer(Modifier.height(24.dp))

            // ── Weekly Activity (Visual Graph) ──────────────
            WeeklyActivityChart(
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Key Stats Row ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    value = "${state.learnedCount}",
                    label = "Слов",
                    icon = "📚",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    value = "${p.currentStreak}",
                    label = "Дня",
                    icon = "🔥",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    value = "${p.totalStudyMinutes}",
                    label = "Мин",
                    icon = "⏱",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Achievements Preview ────────────────────────
            AchievementsSection(
                unlocked = state.unlockedAchievements,
                total = state.totalAchievements,
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate("achievements") 
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String, level: String, appLevel: Int, progress: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            // Level Badge on Avatar
            Box(
                modifier = Modifier
                    .offset(x = (-4).dp, y = (-4).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(level, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(name, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold)
        Text("Уровень $appLevel", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(16.dp))
        
        // Progress to next level
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text("Следующий уровень через ${( (1f-progress)*100).toInt()} XP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, icon: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WeeklyActivityChart(modifier: Modifier = Modifier) {
    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val activity = listOf(0.4f, 0.8f, 0.6f, 1f, 0.3f, 0.5f, 0.7f) // Dummy data

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Активность за неделю", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                activity.forEachIndexed { index, value ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(value)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                    )
                                )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(days[index], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsSection(unlocked: Int, total: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Достижения", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Разблокировано $unlocked из $total", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = 180f))
        }
    }
}
