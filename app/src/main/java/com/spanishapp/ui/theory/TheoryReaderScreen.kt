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
import com.spanishapp.ui.components.speakSpanish

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
                onPlayAudio = { text -> tts?.speakSpanish(text, "theory_${text.hashCode()}") },
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
        TheorySectionType.TABLE -> section.table?.let { TableSection(section.heading, it, onPlayAudio) }
        TheorySectionType.EXAMPLES -> ExamplesSection(section.heading, section.examples, onPlayAudio)
        TheorySectionType.MNEMONIC -> MnemonicSection(section.body)
        TheorySectionType.TIP -> TipSection(section.body)
        TheorySectionType.WARNING -> WarningSection(section.body)
        TheorySectionType.COMPARISON -> section.comparison?.let { ComparisonSection(section.heading, it) }
    }
}

// v1.13.5: helper для adaptive font size теории.
// Юзер: "теорию можно сделать чуть крупнее для удобного прочтения,
// мелко, приходится напрягаться". На планшете +4sp ко всем.
@Composable
private fun theoryFont(base: Int): androidx.compose.ui.unit.TextUnit =
    (if (com.spanishapp.ui.adaptive.isWideScreen()) base + 4 else base).sp

@Composable
private fun theoryLine(base: Int): androidx.compose.ui.unit.TextUnit =
    (if (com.spanishapp.ui.adaptive.isWideScreen()) base + 6 else base).sp

@Composable
private fun TextSection(heading: String, body: String) {
    Column {
        if (heading.isNotBlank()) {
            com.spanishapp.ui.components.MarkdownText(
                heading, fontSize = theoryFont(16), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
        }
        com.spanishapp.ui.components.MarkdownText(
            body, fontSize = theoryFont(14), lineHeight = theoryLine(20))
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
                com.spanishapp.ui.components.MarkdownText(
                    "🧠 $heading",
                    fontSize = theoryFont(13),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(6.dp))
            }
            com.spanishapp.ui.components.MarkdownText(
                body, fontSize = theoryFont(15), lineHeight = theoryLine(22),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TableSection(heading: String, table: TheoryTable, onPlayAudio: (String) -> Unit) {
    // v1.13.7: добавлена озвучка для строк таблицы. Юзер: «в первом
    // уроке (буквы) каждую букву можно прослушать, а в числах нет».
    // Источник TTS — highlighted column (обычно испанское слово).
    // Тап в любом месте row = play. Speaker icon — visual cue.
    val ttsColumnIdx = table.highlightedColumns.firstOrNull() ?: 1
    val ttsEnabled = ttsColumnIdx < (table.headers.size.takeIf { it > 0 } ?: 0).coerceAtLeast(0)

    Column {
        if (heading.isNotBlank()) {
            com.spanishapp.ui.components.MarkdownText(
                "📊 $heading", fontSize = theoryFont(14), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(8.dp)) {
                // Headers
                Row(verticalAlignment = Alignment.CenterVertically) {
                    table.headers.forEachIndexed { i, h ->
                        com.spanishapp.ui.components.MarkdownText(
                            h,
                            modifier = Modifier.weight(1f).padding(6.dp),
                            fontSize = theoryFont(12),
                            fontWeight = FontWeight.Bold,
                            color = if (i in table.highlightedColumns)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Spacer под speaker icon column
                    if (ttsEnabled) Spacer(Modifier.width(40.dp))
                }
                HorizontalDivider()
                // Rows
                table.rows.forEachIndexed { ri, row ->
                    val ttsText = row.getOrNull(ttsColumnIdx)?.takeIf { it.isNotBlank() }
                    Row(
                        modifier = Modifier
                            .background(
                                if (ri % 2 == 0) Color.Transparent
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                            .let { mod ->
                                if (ttsText != null) mod.clickable { onPlayAudio(ttsText) }
                                else mod
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        row.forEachIndexed { i, cell ->
                            com.spanishapp.ui.components.MarkdownText(
                                cell,
                                modifier = Modifier.weight(1f).padding(6.dp),
                                fontSize = theoryFont(14),
                                fontWeight = if (i in table.highlightedColumns)
                                    FontWeight.Bold else FontWeight.Normal,
                                color = if (i in table.highlightedColumns)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (ttsEnabled) {
                            IconButton(
                                onClick = { ttsText?.let(onPlayAudio) },
                                enabled = ttsText != null,
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Произнести",
                                    tint = if (ttsText != null) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
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
        com.spanishapp.ui.components.MarkdownText(
            "🗣 ${heading.ifBlank { "Примеры" }}", fontSize = theoryFont(14), fontWeight = FontWeight.Bold)
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
                            com.spanishapp.ui.components.MarkdownText(
                                ex.spanish, fontSize = theoryFont(15), fontWeight = FontWeight.SemiBold)
                            com.spanishapp.ui.components.MarkdownText(
                                ex.russian, fontSize = theoryFont(13),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (ex.note.isNotBlank()) {
                                com.spanishapp.ui.components.MarkdownText(
                                    ex.note, fontSize = theoryFont(11),
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
            com.spanishapp.ui.components.MarkdownText(
                body, fontSize = theoryFont(14), lineHeight = theoryLine(20),
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
            com.spanishapp.ui.components.MarkdownText(
                body, fontSize = theoryFont(13), lineHeight = theoryLine(18),
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
            com.spanishapp.ui.components.MarkdownText(
                body, fontSize = theoryFont(13), lineHeight = theoryLine(18),
                color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
private fun ComparisonSection(heading: String, comp: TheoryComparison) {
    Column {
        if (heading.isNotBlank()) {
            com.spanishapp.ui.components.MarkdownText(
                heading, fontSize = theoryFont(14), fontWeight = FontWeight.Bold)
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
                    com.spanishapp.ui.components.MarkdownText(
                        comp.leftHeader, fontSize = theoryFont(13), fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    comp.pairs.forEach { (l, _) ->
                        com.spanishapp.ui.components.MarkdownText(
                            "• $l", fontSize = theoryFont(13), modifier = Modifier.padding(vertical = 2.dp))
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
                    com.spanishapp.ui.components.MarkdownText(
                        comp.rightHeader, fontSize = theoryFont(13), fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.height(6.dp))
                    comp.pairs.forEach { (_, r) ->
                        com.spanishapp.ui.components.MarkdownText(
                            "• $r", fontSize = theoryFont(13), modifier = Modifier.padding(vertical = 2.dp))
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
                fontSize = theoryFont(14), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.height(8.dp))
            takeaways.forEach { t ->
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text("✓ ", fontSize = theoryFont(14), color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                    com.spanishapp.ui.components.MarkdownText(
                        t, fontSize = theoryFont(13), lineHeight = theoryLine(18),
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
