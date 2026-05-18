package com.spanishapp.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.spanishapp.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.domain.voice.VoicePackInstaller
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.spanishapp.data.prefs.VoicePreferences
import com.spanishapp.data.prefs.VoiceSettings
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface VoiceInstallEntryPoint {
    fun voicePreferences(): VoicePreferences
}

/**
 * Показывается один раз: при первом использовании TTS, если HD-пакет
 * ещё не установлен и пользователь не отметил «больше не показывать».
 */
@Composable
fun VoiceInstallPromptHost(tts: TextToSpeech?) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember(ctx) {
        EntryPointAccessors.fromApplication(
            ctx.applicationContext,
            VoiceInstallEntryPoint::class.java
        ).voicePreferences()
    }
    val settings by prefs.settings.collectAsState(initial = VoiceSettings())

    var show by remember { mutableStateOf(false) }

    LaunchedEffect(tts, settings.seenInstallPrompt) {
        if (tts != null && !settings.seenInstallPrompt) {
            // Дадим TTS пару моментов на инициализацию голосов
            kotlinx.coroutines.delay(500)
            if (!VoicePackInstaller.isHdInstalled(tts)) {
                show = true
            }
        }
    }

    if (show) {
        VoiceInstallDialog(
            onInstall = {
                scope.launch { prefs.markPromptSeen() }
                show = false
                VoicePackInstaller.launchInstaller(ctx)
            },
            onDismiss = {
                scope.launch { prefs.markPromptSeen() }
                show = false
            }
        )
    }
}

@Composable
private fun VoiceInstallDialog(
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.RecordVoiceOver,
                contentDescription = null,
                tint = Color(0xFF7C4DFF),
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                stringResource(R.string.voice_install_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.voice_install_body),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.voice_install_pack_title), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(stringResource(R.string.voice_install_pack_line1), fontSize = 12.sp)
                        Text(stringResource(R.string.voice_install_pack_line2), fontSize = 12.sp)
                        Text(stringResource(R.string.voice_install_pack_line3), fontSize = 12.sp)
                        Text(stringResource(R.string.voice_install_pack_line4), fontSize = 12.sp, color = Color(0xFF8E8E93))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.voice_install_footer),
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onInstall,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
            ) {
                Text(stringResource(R.string.voice_install_confirm), fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.voice_install_later)) }
        }
    )
}
