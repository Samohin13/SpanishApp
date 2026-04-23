package com.spanishapp.ui.settings

import androidx.compose.foundation.BorderStroke
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
import com.spanishapp.data.prefs.VoiceCategory
import com.spanishapp.data.prefs.VoicePersona
import com.spanishapp.data.prefs.VoicePersonas
import com.spanishapp.data.prefs.VoicePreferences
import com.spanishapp.data.prefs.VoiceSettings
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SAMPLE_TEXT = "Hola, ¿qué tal? Aprender español es divertido."

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

    private val _voiceCount = MutableStateFlow(0)
    val voiceCount: StateFlow<Int> = _voiceCount.asStateFlow()

    fun refreshVoices() {
        _voiceCount.value = tts.availableSpanishVoices().size
    }

    fun selectPersona(persona: VoicePersona) = viewModelScope.launch {
        val resolved = VoiceSlotResolver.resolve(persona.slot, tts.availableSpanishVoices())
        voicePrefs.selectPersona(persona.id, resolved?.name)
        // Speak a preview right after selection so user hears the persona immediately
        tts.speak(SAMPLE_TEXT)
    }

    fun setRate(r: Float) = viewModelScope.launch { voicePrefs.setRate(r) }
    fun setPitch(p: Float) = viewModelScope.launch { voicePrefs.setPitch(p) }

    fun preview() {
        tts.speak(SAMPLE_TEXT)
    }

    fun resetToPersonaDefaults() = viewModelScope.launch {
        val persona = VoicePersonas.byId(settings.value.personaId)
        voicePrefs.setRate(persona.rate)
        voicePrefs.setPitch(persona.pitch)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsVoiceScreen(
    navController: NavHostController,
    viewModel: SettingsVoiceViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val isReady by viewModel.isTtsReady.collectAsState()
    val voiceCount by viewModel.voiceCount.collectAsState()

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
                if (!isReady) {
                    InfoBanner("Загружаем синтез речи...")
                } else if (voiceCount == 0) {
                    InfoBanner(
                        "На устройстве не установлены испанские голоса.\n" +
                            "Настройки Android → Язык → Синтез речи → Google → Установить язык → Español."
                    )
                } else if (voiceCount < 2) {
                    InfoBanner(
                        "Доступен только один испанский голос — персонажи звучат " +
                            "одним голосом с разной высотой тона. Установи дополнительные голоса " +
                            "в настройках Android для большего разнообразия."
                    )
                }

                VoiceCategory.values().forEach { cat ->
                    CategoryBlock(
                        category = cat,
                        personas = VoicePersonas.ALL.filter { it.category == cat },
                        selectedId = settings.personaId,
                        onPersonaClick = viewModel::selectPersona
                    )
                    Spacer(Modifier.height(16.dp))
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                Text(
                    "Тонкая настройка",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Корректируй скорость и тембр поверх выбранного персонажа.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                SliderRow(
                    label = "Скорость",
                    valueLabel = "${"%.2f".format(settings.speechRate)}x",
                    value = settings.speechRate,
                    range = 0.5f..1.5f,
                    steps = 9,
                    onChange = viewModel::setRate
                )
                Spacer(Modifier.height(12.dp))
                SliderRow(
                    label = "Тон",
                    valueLabel = "%.2f".format(settings.pitch),
                    value = settings.pitch,
                    range = 0.5f..2.0f,
                    steps = 14,
                    onChange = viewModel::setPitch
                )

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = viewModel::resetToPersonaDefaults) {
                    Text("Сбросить к параметрам персонажа")
                }
            }

            Surface(tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
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
private fun InfoBanner(text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun CategoryBlock(
    category: VoiceCategory,
    personas: List<VoicePersona>,
    selectedId: String,
    onPersonaClick: (VoicePersona) -> Unit
) {
    Text(
        category.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        personas.forEach { p ->
            PersonaCard(
                persona = p,
                selected = p.id == selectedId,
                modifier = Modifier.weight(1f),
                onClick = { onPersonaClick(p) }
            )
        }
    }
}

@Composable
private fun PersonaCard(
    persona: VoicePersona,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer
             else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
             else MaterialTheme.colorScheme.onSurface
    val border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        tonalElevation = if (selected) 4.dp else 0.dp,
        border = border,
        modifier = modifier
            .heightIn(min = 84.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                persona.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = fg
            )
            Spacer(Modifier.height(2.dp))
            Text(
                persona.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) fg else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                valueLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            steps = steps
        )
    }
}
