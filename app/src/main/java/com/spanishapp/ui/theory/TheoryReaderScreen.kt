package com.spanishapp.ui.theory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.data.theory.TheoryContent
import com.spanishapp.data.theory.TheorySection
import com.spanishapp.data.theory.TheorySectionType
import com.spanishapp.data.theory.TheoryTable
import com.spanishapp.data.theory.TheoryExample
import com.spanishapp.data.theory.TheoryComparison
import com.spanishapp.ui.components.rememberSpanishTts

/**
 * Экран чтения теории-карточки.
 *
 * Открывается по маршруту `theory/{lessonId}`.
 * Может быть запущен:
 *   • Из карточки «📖 Теория к уроку» в LessonSession
 *   • Из раздела «Справка» в Profile
 *   • Из глубокой ссылки (futur. SRS push)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryReaderScreen(
    navController: NavHostController,
    vm: TheoryReaderViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val tts = rememberSpanishTts()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.content?.let { "${it.emoji} Теория" } ?: "Теория",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                        state.content?.let {
                            Text(
                                "⏱ ${it.readMinutes} мин · ${it.cefr}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        }
    ) { padding ->
        when {
            state.notFound -> NotFoundContent(state.lessonId, Modifier.padding(padding))
            state.content == null -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> ReaderContent(
                content = state.content!!,
                isRead = state.isAlreadyRead,
                onMarkRead = vm::markRead,
                onPlayAudio = { text -> tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "theory_${text.hashCode()}") },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ReaderContent(
    content: TheoryContent,
    isRead: Boolean,
    onMarkRead: () -> Unit,
    onPlayAudio: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Заголовок
        item {
            Column {
                Text(
                    content.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (content.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        content.subtitle,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Секции
        items(content.sections) { section ->
            SectionView(section, onPlayAudio)
        }

        // Ключевые тейк-эвэи
        if (content.keyTakeaways.isNotEmpty()) {
            item {
                TakeawaysCard(content.keyTakeaways)
            }
        }

        // Кнопка «Я понял!»
        item {
            MarkReadButton(isRead, onMarkRead)
        }

        // Связанные теории
        if (content.relatedTheory.isNotEmpty()) {
            item {
                Text(
                    "👉 Связанные темы",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // TODO: список переходов
            }
        }
    }
}

@Composable
private fun SectionView(section: TheorySection, onPlayAudio: (String) -> Unit) {
    when (section.type) {
        TheorySectionType.TEXT -> TextSection(section.heading, section.body)
        TheorySectionType.RULE -> RuleSection(section.heading, section.body)
        TheorySectionType.TABLE -> section.table?.let { TableSection(section.heading, it) }
        TheorySectionType.EXAMPLES -> ExamplesSection(section.heading, section.examples, onPlayAudio)
        TheorySectionType.MNEMONIC -> MnemonicSection(section.body)
        TheorySectionType.TIP -> TipSection(section.body)
        TheorySectionType.WARNING -> WarningSection(section.body)
        TheorySectionType.COMPARISON -> section.comparison?.let { ComparisonSection(section.heading, it) }
    }
}

@Composable
private fun TextSection(heading: String, body: String) {
    Column {
        if (heading.isNotBlank()) {
            Text(heading, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
        }
        Text(body, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun RuleSection(heading: String, body: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            if (heading.isNotBlank()) {
                Text(
                    "🧠 $heading",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(body, fontSize = 15.sp, lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TableSection(heading: String, table: TheoryTable) {
    Column {
        if (heading.isNotBlank()) {
            Text("📊 $heading", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(8.dp)) {
                // Headers
                Row {
                    table.headers.forEachIndexed { i, h ->
                        Text(
                            h,
                            modifier = Modifier.weight(1f).padding(6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (i in table.highlightedColumns)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider()
                // Rows
                table.rows.forEachIndexed { ri, row ->
                    Row(
                        modifier = Modifier.background(
                            if (ri % 2 == 0) Color.Transparent
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        row.forEachIndexed { i, cell ->
                            Text(
                                cell,
                                modifier = Modifier.weight(1f).padding(6.dp),
                                fontSize = 14.sp,
                                fontWeight = if (i in table.highlightedColumns)
                                    FontWeight.Bold else FontWeight.Normal,
                                color = if (i in table.highlightedColumns)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamplesSection(heading: String, examples: List<TheoryExample>, onPlayAudio: (String) -> Unit) {
    Column {
        Text("🗣 ${heading.ifBlank { "Примеры" }}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            examples.forEach { ex ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth().clickable { onPlayAudio(ex.spanish) },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(ex.spanish, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(ex.russian, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (ex.note.isNotBlank()) {
                                Text(ex.note, fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                        Icon(Icons.Default.VolumeUp, "Произнести",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MnemonicSection(body: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF59D),  // янтарный
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp)) {
            Text("💡", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Text(body, fontSize = 14.sp, lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6D4C00))
        }
    }
}

@Composable
private fun TipSection(body: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp)) {
            Text("💡", fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Text(body, fontSize = 13.sp, lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

@Composable
private fun WarningSection(body: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp)) {
            Text("⚠", fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Text(body, fontSize = 13.sp, lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
private fun ComparisonSection(heading: String, comp: TheoryComparison) {
    Column {
        if (heading.isNotBlank()) {
            Text(heading, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            // Левая колонка
            Surface(
                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f),
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text(comp.leftHeader, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    comp.pairs.forEach { (l, _) ->
                        Text("• $l", fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
            // Правая колонка
            Surface(
                shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f),
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text(comp.rightHeader, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.height(6.dp))
                    comp.pairs.forEach { (_, r) ->
                        Text("• $r", fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TakeawaysCard(takeaways: List<String>) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("📝 Что важно запомнить",
                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.height(8.dp))
            takeaways.forEach { t ->
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text("✓ ", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                    Text(t, fontSize = 13.sp, lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable
private fun MarkReadButton(isRead: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRead) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.primary
        ),
    ) {
        Text(
            if (isRead) "✓ Прочитано — освежить" else "Я понял! ✓",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NotFoundContent(lessonId: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("📖", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("Теория для этого урока ещё в работе",
            fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("Урок $lessonId — справка появится в одном из ближайших обновлений.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
