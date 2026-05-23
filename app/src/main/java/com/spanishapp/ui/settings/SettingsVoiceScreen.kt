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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.voice.PremiumVoiceCatalog

private val BrandOrange = Color(0xFFFF7A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsVoiceScreen(
    navController: NavHostController,
    viewModel: SettingsVoiceViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val selectedRuVoice by viewModel.selectedRuVoice.collectAsStateWithLifecycle()
    val selectedEsVoice by viewModel.selectedEsVoice.collectAsStateWithLifecycle()
    val voiceSpeedMultiplier by viewModel.voiceSpeedMultiplier.collectAsStateWithLifecycle()
    val premiumReady by viewModel.isPremiumTtsReady.collectAsStateWithLifecycle()
    val previewPlaying by viewModel.isPreviewPlaying.collectAsStateWithLifecycle()
    val previewLoading by viewModel.isPreviewLoading.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var playingVoiceId by remember { mutableStateOf<String?>(null) }

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                if (!premiumReady) {
                    Text(
                        "⚠ Premium TTS не настроен — голоса будут не доступны",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // ── 🇷🇺 Русский голос ────────────────────────────────────
            item {
                SectionHeader("🇷🇺 Русский голос")
            }
            items(PremiumVoiceCatalog.RU_VOICES, key = { it.id }) { voice ->
                VoiceCard(
                    voice = voice,
                    isSelected = selectedRuVoice == voice.id,
                    isPlaying = previewPlaying && playingVoiceId == voice.id,
                    isLoading = previewLoading && playingVoiceId == voice.id,
                    enabled = premiumReady,
                    onSelect = { viewModel.selectRuVoice(voice.id) },
                    onPreview = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (previewPlaying && playingVoiceId == voice.id) {
                            viewModel.stopPreview()
                            playingVoiceId = null
                        } else {
                            playingVoiceId = voice.id
                            viewModel.previewVoice(voice.id)
                        }
                    }
                )
            }

            // ── 🇪🇸 Испанский голос ──────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SectionHeader("🇪🇸 Испанский голос")
            }
            items(PremiumVoiceCatalog.ES_VOICES, key = { it.id }) { voice ->
                VoiceCard(
                    voice = voice,
                    isSelected = selectedEsVoice == voice.id,
                    isPlaying = previewPlaying && playingVoiceId == voice.id,
                    isLoading = previewLoading && playingVoiceId == voice.id,
                    enabled = premiumReady,
                    onSelect = { viewModel.selectEsVoice(voice.id) },
                    onPreview = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (previewPlaying && playingVoiceId == voice.id) {
                            viewModel.stopPreview()
                            playingVoiceId = null
                        } else {
                            playingVoiceId = voice.id
                            viewModel.previewVoice(voice.id)
                        }
                    }
                )
            }

            // ── Скорость речи ────────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SpeedSliderCard(
                    multiplier = voiceSpeedMultiplier,
                    onChange = { viewModel.setVoiceSpeedMultiplier(it) },
                )
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun VoiceCard(
    voice: PremiumVoiceCatalog.Voice,
    isSelected: Boolean,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    enabled: Boolean,
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
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onSelect)
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(16.dp))
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (voice.isMale) "👨" else "👩",
                fontSize = 28.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        voice.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BrandOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    voice.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            IconButton(
                onClick = onPreview,
                enabled = enabled && !isLoading,
                modifier = Modifier.size(40.dp)
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = BrandOrange,
                    )
                    isPlaying -> Icon(
                        Icons.Default.Stop,
                        contentDescription = "Стоп",
                        tint = BrandOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    else -> Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Прослушать",
                        tint = BrandOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedSliderCard(
    multiplier: Float,
    onChange: (Float) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
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
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${(multiplier * 100).toInt()}%",
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
        }
    }
}

