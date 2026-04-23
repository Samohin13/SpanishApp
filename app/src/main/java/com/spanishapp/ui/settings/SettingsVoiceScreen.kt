package com.spanishapp.ui.settings

import android.speech.tts.Voice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.prefs.VoicePreferences
import com.spanishapp.data.prefs.VoiceSettings
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SAMPLE_TEXT = "Hola, ¿cómo estás? Me llamo Carlos. Aprender español es muy divertido."

// ── ViewModel ────────────────────────────────────────────────

@HiltViewModel
class SettingsVoiceViewModel @Inject constructor(
    private val voicePrefs: VoicePreferences,
    private val tts: SpanishTts
) : ViewModel() {

    val settings: StateFlow<VoiceSettings> = run {
        val flow = MutableStateFlow(VoiceSettings())
        viewModelScope.launch { voicePrefs.settings.collect { flow.value = it } }
        flow.asStateFlow()
    }

    val isTtsReady: StateFlow<Boolean> = tts.isReady

    data class VoiceItem(
        val voice: Voice,
        val displayName: String,   // "Женский голос 1" / "Мужской голос 2"
        val detail: String,        // "Испания · Офлайн"
        val isMale: Boolean
    )

    private val _voices = MutableStateFlow<List<VoiceItem>>(emptyList())
    val voices: StateFlow<List<VoiceItem>> = _voices.asStateFlow()

    fun loadVoices() {
        val all = tts.availableSpanishVoices()
        if (all.isEmpty()) { _voices.value = emptyList(); return }

        // Sort: highest quality first, then alphabetical
        val sorted = all.sortedWith(
            compareByDescending<Voice> { it.quality }.thenBy { it.name }
        )

        val female = sorted.filter { classifyGender(it) == Gender.FEMALE }
        val male   = sorted.filter { classifyGender(it) == Gender.MALE }
        val other  = sorted.filter { classifyGender(it) == Gender.UNKNOWN }

        // Up to 3 female + 3 male; fill with "other" if a gender is missing
        val pickedFemale = female.take(3)
        val pickedMale   = male.take(3)
        val remaining    = 6 - pickedFemale.size - pickedMale.size
        val pickedOther  = other.take(remaining.coerceAtLeast(0))

        val items = mutableListOf<VoiceItem>()

        pickedFemale.forEachIndexed { i, v ->
            items += VoiceItem(v, "Женский голос ${i + 1}", voiceDetail(v), isMale = false)
        }
        pickedMale.forEachIndexed { i, v ->
            items += VoiceItem(v, "Мужской голос ${i + 1}", voiceDetail(v), isMale = true)
        }
        pickedOther.forEachIndexed { i, v ->
            items += VoiceItem(v, "Голос ${pickedFemale.size + pickedMale.size + i + 1}", voiceDetail(v), isMale = false)
        }

        _voices.value = items
    }

    fun selectVoice(voiceName: String?) = viewModelScope.launch {
        voicePrefs.setVoiceName(voiceName)
    }

    fun previewCurrent() {
        val s = settings.value
        tts.speakNow(SAMPLE_TEXT, s.voiceName, s.speechRate, s.pitch)
    }

    fun previewVoice(voiceName: String?) {
        val s = settings.value
        tts.speakNow(SAMPLE_TEXT, voiceName, s.speechRate, s.pitch)
    }

    fun setRate(r: Float) = viewModelScope.launch { voicePrefs.setRate(r) }
    fun setPitch(p: Float) = viewModelScope.launch { voicePrefs.setPitch(p) }

    fun reset() = viewModelScope.launch { voicePrefs.resetToDefaults() }

    private fun voiceDetail(v: Voice): String {
        val country = v.locale?.displayCountry?.takeIf { it.isNotBlank() } ?: v.locale?.language ?: "?"
        val network = if (v.isNetworkConnectionRequired) "Сеть" else "Офлайн"
        return "$country · $network"
    }

    private enum class Gender { FEMALE, MALE, UNKNOWN }

    private val FEMALE_CODES = setOf("sfea","sfeb","sfef","sfeg","eea","eeb","eef","eeg","esc","esf","esh","esi")
    private val MALE_CODES   = setOf("sfec","sfed","sfeh","sfei","eec","eed","eeh","eei","esd","esg","esj","esk")

    private fun classifyGender(v: Voice): Gender {
        val n = v.name.lowercase()
        if (n.contains("female")) return Gender.FEMALE
        if (n.contains("male"))   return Gender.MALE
        val code = Regex("-x-([a-z]+)-").find(n)?.groupValues?.getOrNull(1) ?: return Gender.UNKNOWN
        return when {
            FEMALE_CODES.any { code.startsWith(it) } -> Gender.FEMALE
            MALE_CODES.any   { code.startsWith(it) } -> Gender.MALE
            else -> Gender.UNKNOWN
        }
    }
}

// ── Screen ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsVoiceScreen(
    navController: NavHostController,
    vm: SettingsVoiceViewModel = hiltViewModel()
) {
    val settings by vm.settings.collectAsState()
    val isReady  by vm.isTtsReady.collectAsState()
    val voices   by vm.voices.collectAsState()

    LaunchedEffect(isReady) { if (isReady) vm.loadVoices() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Голос и озвучка") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    TextButton(onClick = vm::reset) { Text("Сброс") }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // ── Voice list ──────────────────────────────
                SectionHeader("Голос")

                // Auto option
                VoiceRow(
                    name    = "Авто (по умолчанию)",
                    detail  = "Системный выбор",
                    selected = settings.voiceName == null,
                    onSelect = { vm.selectVoice(null) },
                    onPreview = { vm.previewVoice(null) }
                )

                if (!isReady) {
                    Text(
                        "Загружаем голоса…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else if (voices.isEmpty()) {
                    Text(
                        "Испанские голоса не найдены. Установи их через Настройки Android → Язык → Синтез речи → Google.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    voices.forEach { item ->
                        VoiceRow(
                            name     = item.displayName,
                            detail   = item.detail,
                            selected = settings.voiceName == item.voice.name,
                            onSelect = {
                                vm.selectVoice(item.voice.name)
                                vm.previewVoice(item.voice.name)
                            },
                            onPreview = { vm.previewVoice(item.voice.name) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // ── Rate slider ─────────────────────────────
                SectionHeader("Скорость речи")
                SliderRow(
                    value      = settings.speechRate,
                    range      = 0.5f..1.5f,
                    steps      = 9,
                    leftLabel  = "0.5×",
                    rightLabel = "1.5×",
                    centerLabel = "${"%.1f".format(settings.speechRate)}×",
                    onChange   = vm::setRate
                )

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // ── Pitch slider ────────────────────────────
                SectionHeader("Высота тона")
                SliderRow(
                    value      = settings.pitch,
                    range      = 0.5f..2.0f,
                    steps      = 14,
                    leftLabel  = "0.5",
                    rightLabel = "2.0",
                    centerLabel = "%.1f".format(settings.pitch),
                    onChange   = vm::setPitch
                )

                Spacer(Modifier.height(16.dp))
            }

            // ── Sticky bottom button ────────────────────────
            Surface(tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = vm::previewCurrent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Прослушать пример", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

// ── Composables ───────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style     = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color     = MaterialTheme.colorScheme.primary,
        modifier  = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun VoiceRow(
    name: String,
    detail: String,
    selected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick  = onSelect
        )
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text(name, style = MaterialTheme.typography.bodyLarge)
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onPreview) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Прослушать",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SliderRow(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    leftLabel: String,
    rightLabel: String,
    centerLabel: String,
    onChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(leftLabel,   style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(centerLabel, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary)
            Text(rightLabel,  style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value        = value,
            onValueChange = onChange,
            valueRange   = range,
            steps        = steps,
            modifier     = Modifier.fillMaxWidth()
        )
    }
}
