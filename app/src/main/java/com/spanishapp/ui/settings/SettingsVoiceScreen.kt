package com.spanishapp.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
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

    data class VoiceDiagnostics(
        val total: Int,
        val female: Int,
        val male: Int,
        val unknown: Int
    ) {
        val maleDetected: Boolean get() = male > 0
    }

    private val _diag = MutableStateFlow(VoiceDiagnostics(0, 0, 0, 0))
    val diag: StateFlow<VoiceDiagnostics> = _diag.asStateFlow()

    fun refreshVoices() {
        val voices = tts.availableSpanishVoices()
        val c = VoiceSlotResolver.classifyStrict(voices)
        _diag.value = VoiceDiagnostics(
            total   = voices.size,
            female  = c.female.size,
            male    = c.male.size,
            unknown = c.unknown.size
        )
    }

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
    val isReady by viewModel.isTtsReady.collectAsState()
    val diag by viewModel.diag.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(isReady) { if (isReady) viewModel.refreshVoices() }

    val openTtsSettings = {
        runCatching {
            ctx.startActivity(
                Intent("com.android.settings.TTS_SETTINGS")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.onFailure {
            runCatching {
                ctx.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
        Unit
    }

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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when {
                    !isReady -> InfoBanner("Загружаем синтез речи...")
                    diag.total == 0 -> WarningBanner(
                        title = "Нет испанских голосов",
                        body = "На устройстве не установлен ни один испанский голос. " +
                            "Открой настройки Android TTS → Google → Установить голоса → Español.",
                        actionLabel = "Открыть настройки Android TTS",
                        onAction = openTtsSettings
                    )
                    diag.male == 0 -> WarningBanner(
                        title = "Мужской голос не найден",
                        body = "Найдено голосов: ${diag.total} (женских: ${diag.female}, неопределённых: ${diag.unknown}). " +
                            "Мужские персонажи звучат как женский голос с изменённым тоном — это может показаться роботизированным. " +
                            "Установи мужской испанский голос в настройках Android TTS (Google → Español → варианты с пометкой «male»).",
                        actionLabel = "Открыть настройки Android TTS",
                        onAction = openTtsSettings
                    )
                    diag.total < 2 -> InfoBanner(
                        "Доступен только один испанский голос — персонажи различаются по высоте тона. " +
                            "Установи дополнительные голоса в настройках Android."
                    )
                    else -> DiagnosticsBanner(diag)
                }

                Text(
                    "Нажми на карточку — выбрать и услышать. Удерживай — тонкая настройка.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                VoiceCategory.values().forEach { cat ->
                    CategoryBlock(
                        category = cat,
                        personas = VoicePersonas.ALL.filter { it.category == cat },
                        selectedId = settings.personaId,
                        onPersonaClick = viewModel::selectPersona,
                        onPersonaLongClick = { tunePersona = it }
                    )
                    Spacer(Modifier.height(16.dp))
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
    // Show current rate/pitch only if this is the currently selected persona,
    // otherwise show the persona's defaults as starting point.
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
                "Настройка · ${persona.displayName}",
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
                onChange = {
                    rate = it
                    if (isSelected) onRateChange(it)
                }
            )
            Spacer(Modifier.height(12.dp))
            SliderRow(
                label = "Тон",
                valueLabel = "%.2f".format(pitch),
                value = pitch,
                range = 0.5f..2.0f,
                steps = 14,
                onChange = {
                    pitch = it
                    if (isSelected) onPitchChange(it)
                }
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
                    onClick = {
                        rate = persona.rate
                        pitch = persona.pitch
                        if (isSelected) onReset()
                    },
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
                    "Изменения сохраняются только для выбранного персонажа. " +
                        "Сначала выбери «${persona.displayName}» на экране, затем удерживай для настройки.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DiagnosticsBanner(diag: SettingsVoiceViewModel.VoiceDiagnostics) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            "Найдено голосов: ${diag.total} · женских: ${diag.female} · мужских: ${diag.male}" +
                if (diag.unknown > 0) " · неопределённых: ${diag.unknown}" else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun WarningBanner(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = onAction) {
                Text(actionLabel)
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
    onPersonaClick: (VoicePersona) -> Unit,
    onPersonaLongClick: (VoicePersona) -> Unit
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
                onClick = { onPersonaClick(p) },
                onLongClick = { onPersonaLongClick(p) }
            )
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
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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
