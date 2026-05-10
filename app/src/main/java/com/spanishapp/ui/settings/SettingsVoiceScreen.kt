package com.spanishapp.ui.settings

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.domain.voice.FriendlyVoice
import com.spanishapp.domain.voice.Gender
import com.spanishapp.domain.voice.VoiceCatalog
import com.spanishapp.domain.voice.VoicePackInstaller
import com.spanishapp.R
import com.spanishapp.ui.components.rememberSpanishTts

private val Purple   = Color(0xFF7C4DFF)
private val LightBg  = Color(0xFFF5F5F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsVoiceScreen(
    navController: NavHostController,
    viewModel: SettingsVoiceViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val ctx = LocalContext.current
    val tts = rememberSpanishTts()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Список доступных испанских голосов перечитывается при каждом возврате на экран
    var voices by remember { mutableStateOf<List<FriendlyVoice>>(emptyList()) }

    LaunchedEffect(tts) {
        if (tts != null) {
            voices = VoicePackInstaller.spanishVoices(tts)
                .sortedByDescending { VoiceCatalog.rank(it) }
                .map { VoiceCatalog.toFriendly(it) }
        }
    }

    val hdInstalled = remember(voices) { voices.any { it.isHighQuality } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { com.spanishapp.ui.components.AnimatedScreenTitle(text = "🔊 " + stringResource(R.string.voice_announcer), fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
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
            // ── Баннер: установка HD-пакета ────────────────────────
            item {
                VoicePackBanner(
                    hdInstalled = hdInstalled,
                    voicesCount = voices.size,
                    onInstall   = { VoicePackInstaller.launchInstaller(ctx) },
                    onManage    = { VoicePackInstaller.openTtsSettings(ctx) }
                )
            }

            // ── Заголовок списка ───────────────────────────────────
            item {
                Text(
                    text       = if (voices.isEmpty()) stringResource(R.string.voice_not_found) else stringResource(R.string.voice_available, voices.size),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier   = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // ── Голоса сгруппированные по региону ──────────────────
            val grouped = voices.groupBy { it.region }
            grouped.forEach { (region, regionVoices) ->
                item {
                    val flag = regionVoices.first().flag
                    Text(
                        text       = "$flag  $region",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }
                items(regionVoices, key = { it.systemName }) { voice ->
                    VoiceCard(
                        voice      = voice,
                        isSelected = settings.selectedVoiceName == voice.systemName,
                        onSelect   = { viewModel.selectVoice(voice.systemName) },
                        onPreview  = {
                            tts?.let { engine ->
                                val systemVoice = engine.voices?.firstOrNull { it.name == voice.systemName }
                                if (systemVoice != null) engine.voice = systemVoice
                                engine.setSpeechRate(settings.rate)
                                engine.setPitch(settings.pitch)
                                engine.speak(
                                    "Hola, soy ${voice.displayName}. Aprende español conmigo.",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "preview_${voice.systemName}"
                                )
                            }
                        }
                    )
                }
            }

            // ── Скорость и тон ─────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape    = RoundedCornerShape(16.dp),
                    color    = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.voice_rate), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.voice_rate_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Slider(
                            value         = settings.rate,
                            onValueChange = { viewModel.setRate(it) },
                            valueRange    = 0.5f..1.5f,
                            steps         = 9
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.voice_pitch), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.voice_pitch_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Slider(
                            value         = settings.pitch,
                            onValueChange = { viewModel.setPitch(it) },
                            valueRange    = 0.7f..1.4f,
                            steps         = 6
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun VoicePackBanner(
    hdInstalled: Boolean,
    voicesCount: Int,
    onInstall: () -> Unit,
    onManage: () -> Unit
) {
    val containerColor = if (hdInstalled) Color(0xFFE8F5E9) else Purple.copy(alpha = 0.10f)
    val tint           = if (hdInstalled) Color(0xFF2E7D32) else Purple

    Surface(
        shape    = RoundedCornerShape(18.dp),
        color    = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hdInstalled) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint     = tint,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text       = if (hdInstalled) stringResource(R.string.voice_hd_installed) else stringResource(R.string.voice_pack),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = tint
                    )
                    Text(
                        text     = if (hdInstalled)
                                       stringResource(R.string.voice_found_count, voicesCount)
                                   else
                                       stringResource(R.string.voice_pack_promo),
                        fontSize = 13.sp,
                        color    = tint.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (!hdInstalled) {
                Text(
                    text     = stringResource(R.string.voice_pack_size),
                    fontSize = 12.sp,
                    color    = tint.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Button(
                    onClick  = onInstall,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Purple)
                ) {
                    Text(stringResource(R.string.voice_install_pack), fontWeight = FontWeight.ExtraBold)
                }
            } else {
                OutlinedButton(
                    onClick  = onManage,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.voice_manage_in_android), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun VoiceCard(
    voice: FriendlyVoice,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    val border = if (isSelected) BorderStroke(2.dp, Purple) else null
    val genderEmoji = when (voice.gender) {
        Gender.FEMALE -> "👩"
        Gender.MALE   -> "👨"
        Gender.UNKNOWN -> "🗣"
    }
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = if (isSelected) Purple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .let { if (border != null) it.border(border, RoundedCornerShape(14.dp)) else it }
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(genderEmoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(voice.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (voice.isNeural) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFE082)
                        ) {
                            Text(
                                "HD",
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color(0xFF8B6500)
                            )
                        }
                    }
                }
                Text(
                    text     = if (voice.gender == Gender.FEMALE) stringResource(R.string.voice_female)
                               else if (voice.gender == Gender.MALE) stringResource(R.string.voice_male)
                               else stringResource(R.string.voice_neutral),
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onPreview, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.voice_preview),
                    tint = Purple,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.voice_selected), tint = Purple)
            }
        }
    }
}
