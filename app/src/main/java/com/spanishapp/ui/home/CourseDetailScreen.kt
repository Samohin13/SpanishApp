package com.spanishapp.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavHostController,
    courseLevel: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedUnitId by remember { mutableStateOf<String?>(null) }

    // Mini-test completion snapshot (read-only). Used to render ✅ badges
    // on already-passed mini-tests in the expanded panel.
    val miniTestPassed by viewModel.passedMiniTestIds.collectAsStateWithLifecycle()

    val unitsForCourse = remember(state.roadmapUnits, courseLevel) {
        state.roadmapUnits.filter { it.cefrLevel == courseLevel }
    }

    val courseTitle = when (courseLevel) {
        "A1" -> stringResource(R.string.course_title_a1)
        "A2" -> stringResource(R.string.course_title_a2)
        "B1" -> stringResource(R.string.course_title_b1)
        "B2" -> stringResource(R.string.course_title_b2)
        else -> stringResource(R.string.course_title_default, courseLevel)
    }

    val courseDescription = when (courseLevel) {
        "A1" -> stringResource(R.string.course_desc_a1)
        "A2" -> stringResource(R.string.course_desc_a2)
        "B1" -> stringResource(R.string.course_desc_b1)
        "B2" -> stringResource(R.string.course_desc_b2)
        else -> stringResource(R.string.course_desc_default, courseLevel)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(courseTitle, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Description card ─────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.course_about_tab),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            courseDescription,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // ── Blocks list ──────────────────────────────────────
            itemsIndexed(unitsForCourse) { _, unit ->
                TopicCard(
                    unit = unit,
                    isExpanded = expandedUnitId == unit.id,
                    onToggle = {
                        expandedUnitId = if (expandedUnitId == unit.id) null else unit.id
                    },
                    onLessonClick = { lessonIndex ->
                        // v1.23.0: locked unit (A2+ для free-юзера) → paywall.
                        if (unit.isLocked) {
                            navController.navigate("paywall") { launchSingleTop = true }
                        } else if (unit.id.toIntOrNull() != null) {
                            // v1.22.11: чекпоинты обходят промежуточный
                            // экран «Lesson Intro» (с ракетой и ПОЕХАЛИ) и
                            // открываются сразу в своём собственном intro.
                            val lesson = unit.lessons.getOrNull(lessonIndex)
                            val cpId = checkpointIdForUnitLesson(unit.id, lessonIndex, lesson?.title.orEmpty())
                            if (cpId != null) {
                                navController.navigate("checkpoint/$cpId") {
                                    popUpTo("course_detail/$courseLevel") { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate("lesson_intro/${unit.id}/$lessonIndex") {
                                    // Use substituted route — popUpTo matches concrete
                                    // back-stack entries, not template patterns.
                                    popUpTo("course_detail/$courseLevel") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                        // Для preview-юнитов A2/B1/B2 (id не int) клик игнорируется —
                        // контент ещё в разработке.
                    },
                    onPremiumClick = {
                        // v1.23.0: тап на «PRO» бейдж locked-юнита → paywall.
                        navController.navigate("paywall") { launchSingleTop = true }
                    },
                    onMiniTestClick = { position ->
                        if (unit.isLocked) {
                            navController.navigate("paywall") { launchSingleTop = true }
                        } else if (unit.id.toIntOrNull() != null) {
                            navController.navigate("minitest/${unit.id}/$position") {
                                popUpTo("course_detail/$courseLevel") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    completedMiniTestIds = miniTestPassed,
                )
            }
        }
    }
}

/**
 * v1.22.11: определяет, является ли урок чекпоинтом, и возвращает его CP id.
 * Маппинг:
 *   unit "1" (block A1) + lessonIndex 15 (16-й урок) → cp1
 *   unit "2" + lessonIndex 15 → cp2  (когда будут блоки A2)
 *   и т.д.
 * Также fallback по title — если есть «Чекпоинт» в названии.
 */
private fun checkpointIdForUnitLesson(unitId: String, lessonIndex: Int, title: String): String? {
    // По названию (надёжно) — title содержит «Чекпоинт» (с учётом возможного
    // префикса-эмодзи: «🏁 Чекпоинт ...», «🚩 Чекпоинт...» и пр.)
    if (title.contains("Чекпоинт", ignoreCase = true)) {
        // Маппинг блок → CP id (все 16 блоков курса A1→B2)
        return when (unitId) {
            "1"  -> "cp1"
            "2"  -> "cp2"
            "3"  -> "cp3"
            "4"  -> "cp4"
            "5"  -> "cp5"
            "6"  -> "cp6"
            "7"  -> "cp7"
            "8"  -> "cp8"
            "9"  -> "cp9"
            "10" -> "cp10"
            "11" -> "cp11"
            "12" -> "cp12"
            "13" -> "cp13"
            "14" -> "cp14"
            "15" -> "cp15"
            "16" -> "cp16"
            else -> null
        }
    }
    return null
}
