package com.spanishapp.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.domain.voice.TutorPersonality
import com.spanishapp.domain.voice.VoiceGender
import com.spanishapp.R

private val BrandOrange = Color(0xFFFF7A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsVoiceScreen(
    navController: NavHostController,
    viewModel: SettingsVoiceViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val personality by viewModel.personality.collectAsStateWithLifecycle()
    val voiceGender by viewModel.voiceGender.collectAsStateWithLifecycle()
    val voiceSpeedMultiplier by viewModel.voiceSpeedMultiplier.collectAsStateWithLifecycle()
    val premiumReady by viewModel.isPremiumTtsReady.collectAsStateWithLifecycle()
    val previewPlaying by viewModel.isPreviewPlaying.collectAsStateWithLifecycle()

    // Останавливаем preview при выходе с экрана
    DisposableEffect(Unit) {
        onDispose { viewModel.stopPreview() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.voice_announcer),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Заголовок: характер репетитора ─────────────────────
            item {
                Column(Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)) {
                    Text(
                        "Характер репетитора",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Выберите тон, голос и темп для AI-уроков. " +
                                if (premiumReady) "Премиум HD-голоса Google."
                                else "⚠ Подключение TTS не настроено — используется системный голос.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // ── Сегментный переключатель пола голоса ───────────────
            item {
                GenderSegmented(
                    selected = voiceGender,
                    onSelect = { g ->
                        viewModel.selectVoiceGender(g)
                        // мгновенно повторим preview если играет
                        if (previewPlaying) {
                            viewModel.stopPreview()
                            viewModel.previewPersonality(personality)
                        }
                    }
                )
            }

            // ── 4 карточки характеров ──────────────────────────────
            items(TutorPersonality.entries.toTypedArray(), key = { it.id }) { p ->
                PersonalityCard(
                    personality = p,
                    gender = voiceGender,
                    isSelected = personality.id == p.id,
                    isPlaying = previewPlaying && personality.id == p.id,
                    premiumReady = premiumReady,
                    onSelect = { viewModel.selectPersonality(p) },
                    onPreview = {
                        if (previewPlaying && personality.id == p.id) {
                            viewModel.stopPreview()
                        } else {
                            viewModel.selectPersonality(p)
                            viewModel.previewPersonality(p)
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── Скорость речи (применяется к premium TTS) ──────────
            item {
                SpeedSliderCard(
                    multiplier = voiceSpeedMultiplier,
                    onChange = { viewModel.setVoiceSpeedMultiplier(it) },
                    onPreview = {
                        if (previewPlaying) viewModel.stopPreview()
                        else viewModel.previewPersonality(personality)
                    },
                    isPlaying = previewPlaying,
                    premiumReady = premiumReady,
                )
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun SpeedSliderCard(
    multiplier: Float,
    onChange: (Float) -> Unit,
    onPreview: () -> Unit,
    isPlaying: Boolean,
    premiumReady: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Скорость речи",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${(multiplier * 100).toInt()}% от базовой скорости персонажа",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (multiplier != 1.0f) {
                    TextButton(onClick = { onChange(1.0f) }) {
                        Text("Сброс", fontSize = 12.sp, color = BrandOrange)
                    }
                }
            }
            Slider(
                value = multiplier,
                onValueChange = onChange,
                valueRange = 0.5f..1.5f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = BrandOrange,
                    activeTrackColor = BrandOrange,
                )
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0.5×", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("1.0×", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("1.5×", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onPreview,
                enabled = premiumReady,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) MaterialTheme.colorScheme.surfaceContainerHighest else BrandOrange,
                    contentColor = if (isPlaying) MaterialTheme.colorScheme.onSurface else Color.White
                )
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isPlaying) "Стоп" else "Прослушать с этой скоростью",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PersonalityCard(
    personality: TutorPersonality,
    gender: VoiceGender,
    isSelected: Boolean,
    isPlaying: Boolean,
    premiumReady: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
) {
    val borderColor = if (isSelected) BrandOrange else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val containerColor = if (isSelected)
        BrandOrange.copy(alpha = 0.08f)
    else
        MaterialTheme.colorScheme.surface

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(personality.emoji, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            personality.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrandOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        personality.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Voice meta chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "ES: ${shortVoiceLabel(personality.esVoice(gender))} · " +
                                "RU: ${shortVoiceLabel(personality.ruVoice(gender))} · " +
                                "темп ${"%.2f".format(personality.speed)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onPreview,
                    enabled = premiumReady,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) MaterialTheme.colorScheme.surfaceContainerHighest else BrandOrange,
                        contentColor = if (isPlaying) MaterialTheme.colorScheme.onSurface else Color.White
                    ),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isPlaying) "Стоп" else "Прослушать",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/** Преобразует "es-ES-Neural2-D" → "Neural2-D". */
private fun shortVoiceLabel(fullVoice: String): String {
    val parts = fullVoice.split('-')
    return if (parts.size >= 4) parts.drop(2).joinToString("-") else fullVoice
}

@Composable
private fun GenderSegmented(
    selected: VoiceGender,
    onSelect: (VoiceGender) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(4.dp)) {
            VoiceGender.entries.forEach { g ->
                val isSel = g == selected
                val emoji = if (g == VoiceGender.FEMALE) "👩" else "👨"
                val label = if (g == VoiceGender.FEMALE) "Женский" else "Мужской"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) BrandOrange else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(g) }
                ) {
                    Row(
                        Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            label,
                            fontSize = 14.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

