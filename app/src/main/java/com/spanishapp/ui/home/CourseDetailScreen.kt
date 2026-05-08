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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val courseTitle = remember(courseLevel) {
        when (courseLevel) {
            "A1" -> "Курс A1: Начинающий"
            "A2" -> "Курс A2: Элементарный"
            "B1" -> "Курс B1: Средний"
            "B2" -> "Курс B2: Выше среднего"
            else -> "Курс $courseLevel"
        }
    }

    val courseDescription = remember(courseLevel) {
        when (courseLevel) {
            "A1" -> "Основы испанского языка. Научись представляться, говорить о семье и строить простые предложения."
            "A2" -> "Развивай навыки общения. Путешествия, покупки, описание прошлого."
            "B1" -> "Свободное общение. Сложные временные конструкции и субъективное наклонение."
            "B2" -> "Продвинутый уровень. Идиомы, специальная лексика и сложные грамматические структуры."
            else -> "Курс испанского языка уровня $courseLevel"
        }
    }

    Scaffold(
        containerColor = Color(0xFFF0F0F5),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(courseTitle, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "${unitsForCourse.size} блоков",
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F0F5)),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Description card ─────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "О курсе",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            courseDescription,
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93),
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
                                popUpTo("course_detail/{courseLevel}") { inclusive = false }
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
