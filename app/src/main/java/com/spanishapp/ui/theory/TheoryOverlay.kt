package com.spanishapp.ui.theory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.data.theory.TheoryComparison
import com.spanishapp.data.theory.TheoryContent
import com.spanishapp.data.theory.TheoryExample
import com.spanishapp.data.theory.TheorySection
import com.spanishapp.data.theory.TheorySectionType
import com.spanishapp.data.theory.TheoryTable

/**
 * Полноэкранный overlay с теорией внутри LessonSession.
 *
 * Визуально совпадает со стилем TheoryReaderScreen (теория из профиля),
 * чтобы юзер видел один и тот же дизайн где бы он ни открывал теорию.
 *
 * Открывается:
 *   • Автоматически когда юзер входит в новый урок (если для урока есть теория)
 *   • По тапу на иконку 📖 в TopBar
 *
 * Закрывается крестиком X в правом верхнем углу.
 */
@Composable
fun TheoryOverlay(
    theoryContent: TheoryContent,
    onClose: () -> Unit,
    onTtsSpeak: (String) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Минималистичная шапка как в TheoryReaderScreen
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть теорию")
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${theoryContent.emoji} Теория",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                        Text(
                            "⏱ ${theoryContent.readMinutes} мин · ${theoryContent.cefr}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Контент — тот же ReaderContent что и в TheoryReaderScreen
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Большой заголовок + subtitle
                item {
                    Column {
                        Text(
                            theoryContent.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (theoryContent.subtitle.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                theoryContent.subtitle,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Секции
                items(theoryContent.sections) { section ->
                    SectionView(section, onTtsSpeak)
                }

                // Takeaways
                if (theoryContent.keyTakeaways.isNotEmpty()) {
                    item {
                        TakeawaysCard(theoryContent.keyTakeaways)
                    }
                }

                // Кнопка «Понятно»
                item {
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            "Понятно ✓",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Renderers — точная копия из TheoryReaderScreen для визуального
//  совпадения двух экранов теории.
// ═══════════════════════════════════════════════════════════════════════

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
            Text(
                body,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
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
                                fontSize = 13.sp,
                                fontWeight = if (i in table.highlightedColumns) FontWeight.Bold else FontWeight.Normal,
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
private fun ExamplesSection(
    heading: String,
    examples: List<TheoryExample>,
    onPlayAudio: (String) -> Unit,
) {
    Column {
        Text("🗣 ${heading.ifBlank { "Примеры" }}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            examples.forEach { ex ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayAudio(ex.spanish) },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(ex.spanish, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                ex.russian,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (ex.note.isNotBlank()) {
                                Text(
                                    ex.note,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                        }
                        Icon(
                            Icons.Default.VolumeUp,
                            "Произнести",
                            tint = MaterialTheme.colorScheme.primary,
                        )
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
        color = Color(0xFFFFF59D),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp)) {
            Text("💡", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                body,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6D4C00),
            )
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
            Text(
                body,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
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
            Text(
                body,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
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
            Surface(
                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f),
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text(
                        comp.leftHeader,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    comp.pairs.forEach { (l, _) ->
                        Text("• $l", fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f),
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text(
                        comp.rightHeader,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
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
            Text(
                "📝 Что важно запомнить",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            takeaways.forEach { t ->
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text(
                        "✓ ",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        t,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}
