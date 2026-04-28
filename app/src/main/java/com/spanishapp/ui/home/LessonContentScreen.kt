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
    val lessonKey = "u${unitId}_l${lessonIndex}"
    val unit    = remember(unitId) { RoadmapData.units.getOrNull(unitId - 1) }
    val lesson  = remember(unit, lessonIndex) { unit?.lessons?.getOrNull(lessonIndex) }
    val content = LessonContentData.lessons[lessonKey]

    // Если нет статичного контента — идём назад (не должно случиться при правильном роутинге)
    if (content == null || unit == null || lesson == null) {
        navController.popBackStack()
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
                            text = "Блок ${unit.id} · ${unit.cefrLevel}",
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
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Button(
                    onClick = {
                        if (!isMarked) {
                            isMarked = true
                            viewModel.markLessonComplete(unitId, lessonIndex)
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    if (isMarked) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isMarked) "Готово!" else "Понятно! (+15 XP)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = Color(0xFFF5F5F8)
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
                        Text(
                            text = content.intro,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF1A1A1A)
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
        Text(
            text = section.heading,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        // Строки таблицы
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column {
                section.items.forEachIndexed { idx, item ->
                    ContentRow(item = item, accentColor = accentColor)
                    if (idx < section.items.lastIndex) {
                        HorizontalDivider(
                            color = Color(0xFFF0F0F0),
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
            Text(
                text = item.left,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        Spacer(Modifier.width(12.dp))

        // Правая часть — перевод / пояснение + пример
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.right,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            if (item.note.isNotBlank()) {
                Text(
                    text = item.note,
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
