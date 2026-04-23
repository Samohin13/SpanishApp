package com.spanishapp.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
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

    private fun resolvedVoiceNameFor(persona: VoicePersona): String? =
        VoiceSlotResolver.resolve(persona.slot, tts.availableSpanishVoices())?.name

    fun selectPersona(persona: VoicePersona) = viewModelScope.launch {
        val voiceName = resolvedVoiceNameFor(persona)
        voicePrefs.selectPersona(persona.id, voiceName)
        tts.speakNow(SAMPLE_TEXT, voiceName, persona.rate, persona.pitch)
    }

    fun previewCustom(persona: VoicePersona, rate: Float, pitch: Float) {
        val voiceName = resolvedVoiceNameFor(persona)
        tts.speakNow(SAMPLE_TEXT, voiceName, rate, pitch)
    }

    fun setRate(r: Float) = viewModelScope.launch { voicePrefs.setRate(r) }
    fun setPitch(p: Float) = viewModelScope.launch { voicePrefs.setPitch(p) }

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
    var tunePersona by remember { mutableStateOf<VoicePersona?>(null) }

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
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    "Выбери голос для озвучки",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "Нажми — выбрать и услышать · Удерживай — тонкая настройка",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                val personas = VoicePersonas.ALL
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        personas.getOrNull(0)?.let { p ->
                            PersonaCard(
                                persona = p,
                                selected = p.id == settings.personaId,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.selectPersona(p) },
                                onLongClick = { tunePersona = p }
                            )
                        }
                        personas.getOrNull(1)?.let { p ->
                            PersonaCard(
                                persona = p,
                                selected = p.id == settings.personaId,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.selectPersona(p) },
                                onLongClick = { tunePersona = p }
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        personas.getOrNull(2)?.let { p ->
                            PersonaCard(
                                persona = p,
                                selected = p.id == settings.personaId,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.selectPersona(p) },
                                onLongClick = { tunePersona = p }
                            )
                        }
                        personas.getOrNull(3)?.let { p ->
                            PersonaCard(
                                persona = p,
                                selected = p.id == settings.personaId,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.selectPersona(p) },
                                onLongClick = { tunePersona = p }
                            )
                        }
                    }
                }
            }

            Surface(tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Применить", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        tunePersona?.let { persona ->
            TunePersonaSheet(
                persona = persona,
                settings = settings,
                onDismiss = { tunePersona = null },
                onRateChange = viewModel::setRate,
                onPitchChange = viewModel::setPitch,
                onPreview = { r, p -> viewModel.previewCustom(persona, r, p) },
                onReset = viewModel::resetToPersonaDefaults
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TunePersonaSheet(
    persona: VoicePersona,
    settings: VoiceSettings,
    onDismiss: () -> Unit,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onPreview: (rate: Float, pitch: Float) -> Unit,
    onReset: () -> Unit
) {
    val isSelected = settings.personaId == persona.id
    var rate by remember(persona.id, isSelected) {
        mutableStateOf(if (isSelected) settings.speechRate else persona.rate)
    }
    var pitch by remember(persona.id, isSelected) {
        mutableStateOf(if (isSelected) settings.pitch else persona.pitch)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                persona.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                persona.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            SliderRow(
                label = "Скорость",
                valueLabel = "${"%.2f".format(rate)}x",
                value = rate,
                range = 0.5f..1.5f,
                steps = 9,
                onChange = { rate = it; if (isSelected) onRateChange(it) }
            )
            Spacer(Modifier.height(12.dp))
            SliderRow(
                label = "Тон",
                valueLabel = "%.2f".format(pitch),
                value = pitch,
                range = 0.5f..2.0f,
                steps = 14,
                onChange = { pitch = it; if (isSelected) onPitchChange(it) }
            )

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onPreview(rate, pitch) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Прослушать")
                }
                OutlinedButton(
                    onClick = { rate = persona.rate; pitch = persona.pitch; if (isSelected) onReset() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Restore, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Сброс")
                }
            }

            if (!isSelected) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Сначала выбери «${persona.displayName}», затем удерживай для настройки.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PersonaCard(
    persona: VoicePersona,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bg     = if (selected) MaterialTheme.colorScheme.primaryContainer
                 else MaterialTheme.colorScheme.surface
    val fg     = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                 else MaterialTheme.colorScheme.onSurface
    val border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        tonalElevation = if (selected) 4.dp else 0.dp,
        border = border,
        modifier = modifier
            .heightIn(min = 110.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                persona.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = fg
            )
            Spacer(Modifier.height(6.dp))
            Text(
                persona.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) fg.copy(alpha = 0.75f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
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
        Slider(value = value, onValueChange = onChange, valueRange = range, steps = steps)
    }
}
