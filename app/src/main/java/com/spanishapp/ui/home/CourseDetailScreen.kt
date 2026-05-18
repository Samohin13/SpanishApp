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
                        if (!unit.isLocked && unit.id.toIntOrNull() != null) {
                            navController.navigate("lesson_intro/${unit.id}/$lessonIndex") {
                                // Use substituted route — popUpTo matches concrete
                                // back-stack entries, not template patterns.
                                popUpTo("course_detail/$courseLevel") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        // Для preview-юнитов A2/B1/B2 (id не int) клик игнорируется —
                        // контент ещё в разработке.
                    },
                    onPremiumClick = { /* premium убран — все курсы открыты */ }
                )
            }
        }
    }
}
