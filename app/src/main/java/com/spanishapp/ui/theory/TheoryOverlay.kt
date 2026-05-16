package com.spanishapp.ui.theory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.data.theory.TheoryContent
import com.spanishapp.data.theory.TheorySection
import com.spanishapp.data.theory.TheorySectionType

/**
 * Полноэкранный overlay с теорией внутри LessonSession.
 *
 * Открывается:
 *   • Автоматически когда юзер входит в новый урок (если для урока есть теория)
 *   • По тапу на иконку 📖 в TopBar
 *
 * Закрывается ТОЛЬКО крестиком X в правом верхнем углу.
 * Сохраняется поверх всего урока — не дублирует Scaffold/TopBar.
 *
 * Это упрощённая версия TheoryReaderScreen без своего navController —
 * предназначена для embed-режима внутри LessonSession.
 */
@Composable
fun TheoryOverlay(
    theoryContent: TheoryContent,
    onClose: () -> Unit,
    onTtsSpeak: (String) -> Unit = {},
) {
    val terra = Color(0xFFFF5722)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header overlay — заголовок + кнопка X
            Surface(
                color = terra,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(theoryContent.emoji, fontSize = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "📖 Теория к уроку",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                        Text(
                            theoryContent.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Закрыть теорию",
                            tint = Color.White,
                        )
                    }
                }
            }

            // Контент теории
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // Эмодзи + subtitle вверху
                if (theoryContent.subtitle.isNotBlank()) {
                    Text(
                        theoryContent.subtitle,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    )
                }

                // Все секции
                theoryContent.sections.forEach { section ->
                    SectionRenderer(section, terra, onTtsSpeak)
                    Spacer(Modifier.height(12.dp))
                }

                // Takeaways
                if (theoryContent.keyTakeaways.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = terra.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "📝 Что важно запомнить",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = terra,
                            )
                            Spacer(Modifier.height(8.dp))
                            theoryContent.keyTakeaways.forEach { item ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text("• ", color = terra, fontWeight = FontWeight.Bold)
                                    Text(item, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // CTA закрыть
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = terra,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClose),
                ) {
                    Text(
                        "Понятно ✓",
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Renderers для разных типов секций
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun SectionRenderer(
    section: TheorySection,
    accentColor: Color,
    onTtsSpeak: (String) -> Unit,
) {
    when (section.type) {
        TheorySectionType.RULE -> RuleSection(section, accentColor)
        TheorySectionType.TEXT -> TextSection(section, accentColor)
        TheorySectionType.TABLE -> TableSection(section, accentColor)
        TheorySectionType.EXAMPLES -> ExamplesSection(section, accentColor, onTtsSpeak)
        TheorySectionType.MNEMONIC -> MnemonicSection(section, accentColor)
        TheorySectionType.TIP -> TipSection(section)
        TheorySectionType.WARNING -> WarningSection(section)
        TheorySectionType.COMPARISON -> ComparisonSection(section, accentColor)
    }
}

/**
 * Универсальная тёмная плашка с цветной левой полосой.
 * Используется для RULE/MNEMONIC/TIP/WARNING — все они визуально похожи,
 * различаются только акцентным цветом полосы + заголовка.
 */
@Composable
private fun AccentCard(
    accent: Color,
    heading: String,
    body: String,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2A2A2A),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(Modifier.padding(14.dp)) {
                if (heading.isNotBlank()) {
                    Text(
                        heading,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    body,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFE8E8E8),
                )
            }
        }
    }
}

@Composable
private fun RuleSection(section: TheorySection, accentColor: Color) {
    AccentCard(accent = accentColor, heading = section.heading, body = section.body)
}

@Composable
private fun TextSection(section: TheorySection, accentColor: Color) {
    Column {
        if (section.heading.isNotBlank()) {
            Text(
                section.heading,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
            Spacer(Modifier.height(6.dp))
        }
        Text(section.body, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun TableSection(section: TheorySection, accentColor: Color) {
    val table = section.table ?: return
    Column {
        if (section.heading.isNotBlank()) {
            Text(
                section.heading,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(8.dp)) {
                // Header row
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    table.headers.forEachIndexed { i, h ->
                        Text(
                            h,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                // Data rows
                table.rows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        row.forEachIndexed { i, cell ->
                            Text(
                                cell,
                                fontSize = 13.sp,
                                fontWeight = if (i in table.highlightedColumns) FontWeight.Bold else FontWeight.Normal,
                                color = if (i in table.highlightedColumns) accentColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
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
    section: TheorySection,
    accentColor: Color,
    onTtsSpeak: (String) -> Unit,
) {
    Column {
        if (section.heading.isNotBlank()) {
            Text(
                section.heading,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        section.examples.forEach { ex ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { onTtsSpeak(ex.spanish) },  // ← вся карточка кликабельна
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 🔊 иконка слева — визуальный сигнал что можно тапнуть
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🔊", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ex.spanish, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            ex.russian,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (ex.note.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                ex.note,
                                fontSize = 11.sp,
                                color = accentColor.copy(alpha = 0.85f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MnemonicSection(section: TheorySection, accentColor: Color) {
    AccentCard(
        accent = Color(0xFFFFC107),
        heading = "🧠 ${section.heading.ifBlank { "Запомни" }}",
        body = section.body,
    )
}

@Composable
private fun TipSection(section: TheorySection) {
    AccentCard(
        accent = Color(0xFF4CAF50),
        heading = section.heading.ifBlank { "💡 Лайфхак" },
        body = section.body,
    )
}

@Composable
private fun WarningSection(section: TheorySection) {
    AccentCard(
        accent = Color(0xFFEF5350),
        heading = section.heading.ifBlank { "⚠ Внимание" },
        body = section.body,
    )
}

@Composable
private fun ComparisonSection(section: TheorySection, accentColor: Color) {
    val comp = section.comparison ?: return
    Column {
        if (section.heading.isNotBlank()) {
            Text(
                section.heading,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF2A2A2A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF42A5F5).copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f).padding(end = 4.dp),
            ) {
                Column(Modifier.padding(8.dp)) {
                    Text(comp.leftHeader, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF64B5F6))
                    Spacer(Modifier.height(4.dp))
                    comp.pairs.forEach { (left, _) ->
                        Text(
                            left,
                            fontSize = 12.sp,
                            color = Color(0xFFE8E8E8),
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF2A2A2A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            ) {
                Column(Modifier.padding(8.dp)) {
                    Text(comp.rightHeader, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D))
                    Spacer(Modifier.height(4.dp))
                    comp.pairs.forEach { (_, right) ->
                        Text(
                            right,
                            fontSize = 12.sp,
                            color = Color(0xFFE8E8E8),
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
