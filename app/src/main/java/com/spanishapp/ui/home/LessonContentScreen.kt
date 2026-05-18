package com.spanishapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonContentScreen(
    navController: NavHostController,
    unitId: Int,
    lessonIndex: Int,
    viewModel: LessonIntroViewModel
) {
    com.spanishapp.ui.components.TrackStudyMinutes()
    val lessonKey = "u${unitId}_l${lessonIndex}"
    val unit    = remember(unitId) { RoadmapData.units.getOrNull(unitId - 1) }
    val lesson  = remember(unit, lessonIndex) { unit?.lessons?.getOrNull(lessonIndex) }
    val content = LessonContentData.lessons[lessonKey]

    if (unit == null || lesson == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    if (content == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(lesson.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚧", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_in_development_title), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_in_development_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = {
                        viewModel.markLessonComplete(unitId, lessonIndex)
                        navController.popBackStack()
                    }) { Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_mark_as_done)) }
                }
            }
        }
        return
    }

    val accentColor = unit.color

    var isMarked by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_block_label, unit.id, unit.cefrLevel),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lesson.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                Button(
                    onClick = {
                        if (!isMarked) {
                            isMarked = true
                            viewModel.markLessonComplete(unitId, lessonIndex)
                        }
                        navController.popBackStack()
                    },
                    // v1.15.0 P2: adaptive height (56 → 72dp на планшете)
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(if (com.spanishapp.ui.adaptive.isWideScreen()) 72.dp else 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    if (isMarked) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isMarked) androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_done) else androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_understood),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Вводный блок
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = accentColor.copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(unit.icon, fontSize = 28.sp)
                        // v1.15.0 P1: MarkdownText рендерит **bold** правильно
                        com.spanishapp.ui.components.MarkdownText(
                            text = content.intro,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Секции с содержимым
            items(content.sections) { section ->
                ContentSection(section = section, accentColor = accentColor)
            }
        }
    }
}

// ── Секция контента ───────────────────────────────────────────

@Composable
private fun ContentSection(section: LessonSection, accentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

        // Заголовок секции
        com.spanishapp.ui.components.MarkdownText(
            text = section.heading,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        // Строки таблицы
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column {
                section.items.forEachIndexed { idx, item ->
                    ContentRow(item = item, accentColor = accentColor)
                    if (idx < section.items.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Строка внутри секции ──────────────────────────────────────

@Composable
private fun ContentRow(item: LessonItem, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Левая часть — буква / правило
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.10f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .widthIn(min = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            com.spanishapp.ui.components.MarkdownText(
                text = item.left,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        Spacer(Modifier.width(12.dp))

        // Правая часть — перевод / пояснение + пример
        Column(modifier = Modifier.weight(1f)) {
            com.spanishapp.ui.components.MarkdownText(
                text = item.right,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (item.note.isNotBlank()) {
                com.spanishapp.ui.components.MarkdownText(
                    text = item.note,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
