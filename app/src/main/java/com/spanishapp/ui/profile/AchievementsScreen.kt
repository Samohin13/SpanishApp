package com.spanishapp.ui.profile

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
    val achievements by vm.achievements.collectAsState()

    val unlocked = achievements.filter { it.isUnlocked }
    val locked   = achievements.filter { !it.isUnlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Достижения") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Summary ──────────────────────────────────────
            item {
                AchievementSummary(
                    unlocked = unlocked.size,
                    total    = achievements.size,
                    totalXp  = unlocked.sumOf { it.xpReward }
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Unlocked ─────────────────────────────────────
            if (unlocked.isNotEmpty()) {
                item {
                    SectionLabel("🏆 Получены (${unlocked.size})")
                }
                items(unlocked, key = { it.id }) { a ->
                    AchievementCard(a, unlocked = true)
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // ── Locked ───────────────────────────────────────
            item { SectionLabel("🔒 Ещё не получены (${locked.size})") }
            items(locked, key = { it.id }) { a ->
                AchievementCard(a, unlocked = false)
            }
        }
    }
}

// ── Composables ───────────────────────────────────────────────

@Composable
private fun AchievementSummary(unlocked: Int, total: Int, totalXp: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.Gold.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("🏅", "$unlocked / $total", "достижений")
            SummaryItem("⭐", "+$totalXp", "XP получено")
            val pct = if (total > 0) (unlocked * 100 / total) else 0
            SummaryItem("📊", "$pct%", "прогресс")
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

@Composable
private fun AchievementCard(a: AchievementEntity, unlocked: Boolean) {
    val icon = when {
        a.requirementType == "streak"    -> "🔥"
        a.requirementType == "words"     -> "📚"
        a.requirementType == "lessons"   -> "📖"
        a.requirementType == "dialogues" -> "💬"
        a.requirementType == "xp"        -> "⭐"
        else -> "🏅"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (unlocked)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (unlocked) 0.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (unlocked) AppColors.Gold.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    if (unlocked) icon else "🔒",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    a.titleRu,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (unlocked) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    a.descriptionRu,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // XP badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (unlocked) AppColors.Gold.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "+${a.xpReward} XP",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) AppColors.GoldDark
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
