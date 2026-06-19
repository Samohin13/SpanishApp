package com.spanishapp.ui.grammar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.LessonDao
import com.spanishapp.data.db.entity.LessonEntity
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class GrammarViewModel @Inject constructor(
    private val lessonDao: LessonDao
) : ViewModel() {

    private val _level = MutableStateFlow("A1")
    val level: StateFlow<String> = _level.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val lessons: StateFlow<List<LessonEntity>> = _level
        .flatMapLatest { lessonDao.getByLevel(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLevel(l: String) { _level.value = l }

    fun markCompleted(lesson: LessonEntity) = viewModelScope.launch {
        lessonDao.update(lesson.copy(
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        ))
    }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarScreen(
    navController: NavHostController,
    vm: GrammarViewModel = hiltViewModel()
) {
    val level   by vm.level.collectAsStateWithLifecycle()
    val lessons by vm.lessons.collectAsStateWithLifecycle()
    val levels  = listOf("A1", "A2", "B1", "B2")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        androidx.compose.ui.res.stringResource(com.spanishapp.R.string.title_grammar),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Level tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(levels) { lvl ->
                    FilterChip(
                        selected = level == lvl,
                        onClick  = { vm.setLevel(lvl) },
                        label    = { Text(lvl) }
                    )
                }
            }

            if (lessons.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚧", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.gram_coming_soon, level),
                             style = MaterialTheme.typography.bodyLarge,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // v1.25.90: PRO gate. A1 = free (15 lessons), A2/B1/B2 = PRO (60 lessons).
                // Uses the same rememberIsProState() helper as the 6 game screens.
                val isPro by com.spanishapp.ui.games.common.rememberIsProState()
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(lessons, key = { it.id }) { lesson ->
                        val locked = lesson.level != "A1" && !isPro
                        LessonCard(
                            lesson    = lesson,
                            locked    = locked,
                            onComplete = { vm.markCompleted(lesson) },
                            onPaywall  = { navController.navigate("paywall") { launchSingleTop = true } },
                            modifier   = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

// ── Lesson card ───────────────────────────────────────────────

@Composable
private fun LessonCard(
    lesson: LessonEntity,
    locked: Boolean,
    onComplete: () -> Unit,
    onPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val content  = remember(lesson.contentJson) {
        runCatching { JSONObject(lesson.contentJson) }.getOrNull()
    }

    com.spanishapp.ui.components.PressableCard(
        onClick = {
            // v1.25.90 PRO gate: locked cards не раскрываются — тап ведёт на paywall.
            if (locked) onPaywall() else expanded = !expanded
        },
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (lesson.isCompleted)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(lesson.title, style = MaterialTheme.typography.titleMedium,
                             fontWeight = FontWeight.Bold)
                        if (lesson.isCompleted) {
                            Icon(Icons.Default.CheckCircle, null,
                                 tint = AppColors.Teal, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(lesson.topic, style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        if (locked) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = AppColors.Gold,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.GoldDark
                            )
                        } else {
                            XpChip(lesson.xpReward)
                        }
                    }
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                     null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Content
            if (expanded && content != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(12.dp))

                // Theory
                content.optString("theory").takeIf { it.isNotBlank() }?.let { theory ->
                    InfoBlock("📖 Теория", theory, AppColors.Info)
                    Spacer(Modifier.height(10.dp))
                }

                // Rules
                val rulesArr = content.optJSONArray("rules")
                if (rulesArr != null && rulesArr.length() > 0) {
                    Text("📐 Правила", style = MaterialTheme.typography.labelLarge,
                         fontWeight = FontWeight.Bold, color = AppColors.Teal)
                    Spacer(Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.Teal.copy(alpha = 0.06f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until rulesArr.length()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("·", color = AppColors.Teal, fontWeight = FontWeight.Bold)
                                // v1.13.5: MarkdownText рендерит **bold** правильно
                                com.spanishapp.ui.components.MarkdownText(
                                    rulesArr.getString(i),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // Tip
                content.optString("tip").takeIf { it.isNotBlank() }?.let { tip ->
                    InfoBlock("💡 Совет", tip, AppColors.Gold)
                    Spacer(Modifier.height(10.dp))
                }

                // Examples
                val exArr = content.optJSONArray("examples")
                if (exArr != null && exArr.length() > 0) {
                    Text("✍️ Примеры", style = MaterialTheme.typography.labelLarge,
                         fontWeight = FontWeight.Bold,
                         color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 0 until exArr.length()) {
                            val ex = exArr.getJSONObject(i)
                            Surface(shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    com.spanishapp.ui.components.MarkdownText(
                                        ex.optString("es"), fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium)
                                    com.spanishapp.ui.components.MarkdownText(
                                        ex.optString("ru"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                if (!lesson.isCompleted && !locked) {
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("✅  Урок пройден +${lesson.xpReward} XP") }
                }
            }
        }
    }
}

@Composable
private fun InfoBlock(label: String, text: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.07f))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge,
             fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(4.dp))
        // v1.13.5: MarkdownText рендерит **bold** правильно
        com.spanishapp.ui.components.MarkdownText(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun XpChip(xp: Int) {
    Surface(shape = RoundedCornerShape(6.dp), color = AppColors.Gold.copy(alpha = 0.15f)) {
        Text("+$xp XP", modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
             style = MaterialTheme.typography.labelSmall,
             color = AppColors.GoldDark, fontWeight = FontWeight.Bold)
    }
}
