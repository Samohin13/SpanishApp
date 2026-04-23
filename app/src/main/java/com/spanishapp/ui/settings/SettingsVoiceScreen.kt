package com.spanishapp.ui.settings

import android.speech.tts.Voice
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

private const val SAMPLE_TEXT = "Hola, ¿cómo estás? Aprender español es muy divertido."

private val FEMALE_NAMES = listOf("Sofía", "Carmen", "Valentina")
private val MALE_NAMES   = listOf("Pablo",  "Carlos",  "Diego")

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

    data class NamedVoice(
        val voice: Voice,
        val name: String,    // "Sofía", "Pablo" …
        val detail: String,  // "Испания · Офлайн"
        val isMale: Boolean
    )

    private val _voices = MutableStateFlow<List<NamedVoice>>(emptyList())
    val voices: StateFlow<List<NamedVoice>> = _voices.asStateFlow()

    fun loadVoices() {
        val all = tts.availableSpanishVoices()
        if (all.isEmpty()) { _voices.value = emptyList(); return }

        val sorted = all.sortedWith(
            compareByDescending<Voice> { it.quality }.thenBy { it.name }
        )

        val female = sorted.filter { classify(it) == Gender.FEMALE }.take(3)
        val male   = sorted.filter { classify(it) == Gender.MALE   }.take(3)

        val result = mutableListOf<NamedVoice>()
        female.forEachIndexed { i, v ->
            result += NamedVoice(v, FEMALE_NAMES[i], detail(v), isMale = false)
        }
        male.forEachIndexed { i, v ->
            result += NamedVoice(v, MALE_NAMES[i], detail(v), isMale = true)
        }
        _voices.value = result
    }

    fun selectAndPreview(voiceName: String?) = viewModelScope.launch {
        voicePrefs.setVoiceName(voiceName)
        tts.speakNow(SAMPLE_TEXT, voiceName, settings.value.speechRate, settings.value.pitch)
    }

    fun previewCurrent() {
        val s = settings.value
        tts.speakNow(SAMPLE_TEXT, s.voiceName, s.speechRate, s.pitch)
    }

    fun reset() = viewModelScope.launch { voicePrefs.setVoiceName(null) }

    private fun detail(v: Voice): String {
        val country = v.locale?.displayCountry?.takeIf { it.isNotBlank() } ?: "Español"
        val net     = if (v.isNetworkConnectionRequired) "Сеть" else "Офлайн"
        return "$country · $net"
    }

    private enum class Gender { FEMALE, MALE, UNKNOWN }

    private val FEMALE_CODES = setOf("sfea","sfeb","sfef","sfeg","eea","eeb","eef","eeg","esc","esf","esh","esi")
    private val MALE_CODES   = setOf("sfec","sfed","sfeh","sfei","eec","eed","eeh","eei","esd","esg","esj","esk")

    private fun classify(v: Voice): Gender {
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

    // Pair female + male side-by-side: row[i] = (female[i], male[i])
    val female = voices.filter { !it.isMale }
    val male   = voices.filter {  it.isMale }
    val rowCount = maxOf(female.size, male.size)

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

                if (!isReady) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (voices.isEmpty()) {
                    Text(
                        "Испанские голоса не найдены.\nНастройки Android → Язык → Синтез речи → Google → Español.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(rowCount) { i ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                female.getOrNull(i)?.let { nv ->
                                    VoiceCard(
                                        nv        = nv,
                                        selected  = settings.voiceName == nv.voice.name,
                                        modifier  = Modifier.weight(1f),
                                        onClick   = { vm.selectAndPreview(nv.voice.name) }
                                    )
                                } ?: Spacer(Modifier.weight(1f))

                                male.getOrNull(i)?.let { nv ->
                                    VoiceCard(
                                        nv        = nv,
                                        selected  = settings.voiceName == nv.voice.name,
                                        modifier  = Modifier.weight(1f),
                                        onClick   = { vm.selectAndPreview(nv.voice.name) }
                                    )
                                } ?: Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Sticky bottom
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

// ── Card ─────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VoiceCard(
    nv: SettingsVoiceViewModel.NamedVoice,
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
        shape         = RoundedCornerShape(20.dp),
        color         = bg,
        tonalElevation = if (selected) 4.dp else 0.dp,
        border        = border,
        modifier      = modifier
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
                nv.name,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = fg
            )
            Spacer(Modifier.height(4.dp))
            Text(
                nv.detail,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) fg.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
