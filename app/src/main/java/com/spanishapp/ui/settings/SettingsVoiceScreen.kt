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

data class VoiceOption(
    val name: String,             // TTS Voice.name ("" = Авто/по умолчанию)
    val displayLabel: String,
    val sublabel: String
)

private const val SAMPLE_TEXT = "Hola, me llamo María. Aprender español es divertido."

@HiltViewModel
class SettingsVoiceViewModel @Inject constructor(
    private val voicePrefs: VoicePreferences,
    private val tts: SpanishTts
) : ViewModel() {

    val settings: StateFlow<VoiceSettings> = run {
        val flow = MutableStateFlow(VoiceSettings())
        viewModelScope.launch {
            voicePrefs.settings.collect { flow.value = it }
        }
        flow.asStateFlow()
    }

    val isTtsReady: StateFlow<Boolean> = tts.isReady

    private val _voices = MutableStateFlow<List<VoiceOption>>(emptyList())
    val voices: StateFlow<List<VoiceOption>> = _voices.asStateFlow()

    fun refreshVoices() {
        val list = tts.availableSpanishVoices().map { it.toOption() }
        _voices.value = listOf(
            VoiceOption("", "Авто (по умолчанию)", "Системный голос")
        ) + list
    }

    fun selectVoice(name: String) = viewModelScope.launch {
        voicePrefs.setVoiceName(name.takeIf { it.isNotEmpty() })
    }

    fun setRate(r: Float) = viewModelScope.launch { voicePrefs.setRate(r) }
    fun setPitch(p: Float) = viewModelScope.launch { voicePrefs.setPitch(p) }

    fun preview() {
        tts.speak(SAMPLE_TEXT)
    }

    fun reset() = viewModelScope.launch { voicePrefs.resetToDefaults() }
}

private fun Voice.toOption(): VoiceOption {
    val lower = name.lowercase()
    val gender = when {
        lower.contains("female") || lower.endsWith("-f") || lower.contains("-eea-") || lower.contains("-eeb-") -> "женский"
        lower.contains("male")   || lower.endsWith("-m") || lower.contains("-eec-") || lower.contains("-eed-") -> "мужской"
        else -> ""
    }
    val country = locale?.displayCountry?.takeIf { it.isNotBlank() } ?: "ES"
    val quality = if (isNetworkConnectionRequired) "сетевой" else "офлайн"
    val sub = listOfNotNull(
        country,
        gender.takeIf { it.isNotBlank() },
        quality
    ).joinToString(" · ")
    return VoiceOption(
        name = name,
        displayLabel = name,
        sublabel = sub
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsVoiceScreen(
    navController: NavHostController,
    viewModel: SettingsVoiceViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val voices by viewModel.voices.collectAsState()
    val isReady by viewModel.isTtsReady.collectAsState()

    LaunchedEffect(isReady) {
        if (isReady) viewModel.refreshVoices()
    }

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
                    TextButton(onClick = viewModel::reset) {
                        Text("Сброс")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Section("Голос") {
                    if (!isReady) {
                        Text(
                            "Загружаем список голосов...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (voices.size <= 1) {
                        Text(
                            "На этом устройстве доступен только системный голос.\n" +
                                "Установи дополнительные голоса в настройках Android " +
                                "(Настройки → Язык → Синтез речи → Google → Установить).",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        voices.forEach { opt ->
                            VoiceRow(
                                option = opt,
                                selected = (settings.voiceName ?: "") == opt.name,
                                onClick = { viewModel.selectVoice(opt.name) }
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Section("Скорость речи") {
                    SliderRow(
                        valueLabel = "${"%.2f".format(settings.speechRate)}x",
                        value = settings.speechRate,
                        range = 0.5f..1.5f,
                        steps = 9,
                        onChange = viewModel::setRate
                    )
                }

                Spacer(Modifier.height(20.dp))

                Section("Высота тона") {
                    SliderRow(
                        valueLabel = "${"%.2f".format(settings.pitch)}",
                        value = settings.pitch,
                        range = 0.5f..2.0f,
                        steps = 14,
                        onChange = viewModel::setPitch
                    )
                }
            }

            Surface(
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = viewModel::preview,
                    enabled = isReady,
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

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        content()
    }
}

@Composable
private fun VoiceRow(
    option: VoiceOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (selected) 3.dp else 1.dp,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    option.displayLabel,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (option.sublabel.isNotBlank()) {
                    Text(
                        option.sublabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SliderRow(
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(range.start.let { "%.1fx".format(it) }, style = MaterialTheme.typography.bodySmall)
            Text(
                valueLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(range.endInclusive.let { "%.1fx".format(it) }, style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            steps = steps
        )
    }
}
