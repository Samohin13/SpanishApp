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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

private const val SAMPLE_TEXT = "Hola, ¿cómo estás? Aprender español es muy divertido."

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

    fun selectAndPreview(persona: VoicePersona) = viewModelScope.launch {
        voicePrefs.setVoiceName(persona.cloudVoiceName)
        tts.speakNow(SAMPLE_TEXT, persona.cloudVoiceName, 1.0f, 1.0f)
    }

    fun reset() = viewModelScope.launch { voicePrefs.setVoiceName(null) }
}

// ── Screen ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsVoiceScreen(
    navController: NavHostController,
    vm: SettingsVoiceViewModel = hiltViewModel()
) {
    val settings by vm.settings.collectAsState()

    val female = VoicePersonas.ALL.filter { !it.isMale }
    val male   = VoicePersonas.ALL.filter {  it.isMale }
    val rows   = maxOf(female.size, male.size)

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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    "Выбери голос",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "Нажми — выбрать и услышать",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(rows) { i ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            female.getOrNull(i)?.let { p ->
                                VoiceCard(
                                    persona  = p,
                                    selected = settings.voiceName == p.cloudVoiceName,
                                    modifier = Modifier.weight(1f),
                                    onClick  = { vm.selectAndPreview(p) }
                                )
                            } ?: Spacer(Modifier.weight(1f))

                            male.getOrNull(i)?.let { p ->
                                VoiceCard(
                                    persona  = p,
                                    selected = settings.voiceName == p.cloudVoiceName,
                                    modifier = Modifier.weight(1f),
                                    onClick  = { vm.selectAndPreview(p) }
                                )
                            } ?: Spacer(Modifier.weight(1f))
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
    }
}

// ── Card ─────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VoiceCard(
    persona: VoicePersona,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg     = if (selected) MaterialTheme.colorScheme.primaryContainer
                 else MaterialTheme.colorScheme.surface
    val fg     = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                 else MaterialTheme.colorScheme.onSurface
    val border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null

    Surface(
        shape          = RoundedCornerShape(20.dp),
        color          = bg,
        tonalElevation = if (selected) 4.dp else 0.dp,
        border         = border,
        modifier       = modifier
            .heightIn(min = 100.dp)
            .combinedClickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                persona.displayName,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = fg
            )
        }
    }
}
