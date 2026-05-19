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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.domain.voice.FriendlyVoice
import com.spanishapp.domain.voice.TutorPersonality
import com.spanishapp.domain.voice.VoiceCatalog
import com.spanishapp.domain.voice.VoicePackInstaller
import com.spanishapp.R
import com.spanishapp.ui.components.rememberSpanishTts

private val BrandOrange = Color(0xFFFF7A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsVoiceScreen(
    navController: NavHostController,
    viewModel: SettingsVoiceViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val ctx = LocalContext.current
    val tts = rememberSpanishTts()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val personality by viewModel.personality.collectAsStateWithLifecycle()
    val premiumReady by viewModel.isPremiumTtsReady.collectAsStateWithLifecycle()
    val previewPlaying by viewModel.isPreviewPlaying.collectAsStateWithLifecycle()

    var voices by remember { mutableStateOf<List<FriendlyVoice>>(emptyList()) }
    LaunchedEffect(tts) {
        if (tts != null) {
            voices = VoicePackInstaller.topSpanishVoices(tts)
                .sortedByDescending { VoiceCatalog.rank(it) }
                .map { VoiceCatalog.toFriendly(it) }
        }
    }
    val hdInstalled = remember(voices) { voices.any { it.isHighQuality } }

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

            // ── 4 карточки характеров ──────────────────────────────
            items(TutorPersonality.entries.toTypedArray(), key = { it.id }) { p ->
                PersonalityCard(
                    personality = p,
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

            // ── Расширенно: системные настройки TTS (fallback) ─────
            item {
                Text(
                    "Расширенные настройки",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // ── Баннер: HD пакет системного TTS (для fallback) ─────
            item {
                VoicePackBanner(
                    hdInstalled = hdInstalled,
                    voicesCount = voices.size,
                    onInstall = { VoicePackInstaller.launchInstaller(ctx) },
                    onManage = { VoicePackInstaller.openTtsSettings(ctx) }
                )
            }

            // ── Скорость и тон (для системного TTS fallback) ───────
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.voice_rate),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(R.string.voice_rate_hint),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.rate,
                            onValueChange = { viewModel.setRate(it) },
                            valueRange = 0.5f..1.5f,
                            steps = 9
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.voice_pitch),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(R.string.voice_pitch_hint),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.pitch,
                            onValueChange = { viewModel.setPitch(it) },
                            valueRange = 0.7f..1.4f,
                            steps = 6
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun PersonalityCard(
    personality: TutorPersonality,
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
                        text = "ES: ${shortVoiceLabel(personality.esVoice)} · " +
                                "RU: ${shortVoiceLabel(personality.ruVoice)} · " +
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
private fun VoicePackBanner(
    hdInstalled: Boolean,
    voicesCount: Int,
    onInstall: () -> Unit,
    onManage: () -> Unit
) {
    val containerColor = if (hdInstalled)
        Color(0xFFE8F5E9)
    else
        BrandOrange.copy(alpha = 0.10f)
    val tint = if (hdInstalled) Color(0xFF2E7D32) else BrandOrange

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hdInstalled) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (hdInstalled)
                            stringResource(R.string.voice_hd_installed)
                        else
                            stringResource(R.string.voice_pack),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = tint
                    )
                    Text(
                        text = if (hdInstalled)
                            stringResource(R.string.voice_found_count, voicesCount)
                        else
                            stringResource(R.string.voice_pack_promo),
                        fontSize = 13.sp,
                        color = tint.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (!hdInstalled) {
                Text(
                    text = stringResource(R.string.voice_pack_size),
                    fontSize = 12.sp,
                    color = tint.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Button(
                    onClick = onInstall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text(
                        stringResource(R.string.voice_install_pack),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onManage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.voice_manage_in_android),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
