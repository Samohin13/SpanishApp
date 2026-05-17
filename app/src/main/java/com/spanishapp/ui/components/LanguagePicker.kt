package com.spanishapp.ui.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.R
import com.spanishapp.data.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Standalone VM exposing the UI language preference.
 *
 * Lives outside any specific screen so the LanguagePickerButton can sit on
 * Welcome / Settings / wherever without dragging in unrelated dependencies.
 */
@HiltViewModel
class LanguagePickerViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {
    val current = prefs.uiLanguage.stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    /**
     * Persist the new language and call [onWritten] only after the write
     * actually completes. The caller will then trigger Activity.recreate(),
     * which re-runs attachBaseContext() and reads the FRESH value from
     * DataStore. If we recreated immediately after a fire-and-forget
     * launch, the recreate would race the write and read the OLD value —
     * which is exactly what was happening on Welcome.
     */
    fun set(code: String, onWritten: () -> Unit) {
        viewModelScope.launch {
            prefs.setUiLanguage(code)
            com.spanishapp.service.Analytics.languageChanged(code)
            onWritten()
        }
    }
}

/**
 * Compact circular flag button. Tap → 4-language dialog.
 *
 * Used on screens where a non-Russian user needs to switch UI language
 * before going further (Welcome, Settings).
 */
@Composable
fun LanguagePickerButton(
    modifier: Modifier = Modifier,
    vm: LanguagePickerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val current by vm.current.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val flag = when (current) {
        "ru" -> "🇷🇺"
        "en" -> "🇬🇧"
        "uk" -> "🇺🇦"
        "es" -> "🇪🇸"
        else -> "🌐"
    }

    Surface(
        modifier = modifier
            .size(44.dp)
            .clickable { showDialog = true },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(flag, fontSize = 22.sp)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.settings_language_ui)) },
            text = {
                Column {
                    val options = listOf(
                        Triple("ru", "🇷🇺", "Русский"),
                        Triple("en", "🇬🇧", "English"),
                        Triple("uk", "🇺🇦", "Українська"),
                        Triple("es", "🇪🇸", "Español"),
                    )
                    options.forEach { (code, fl, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (code != current) {
                                        // Hide dialog first so the user doesn't
                                        // see it during the brief recreate frame.
                                        showDialog = false
                                        vm.set(code) {
                                            // Runs after DataStore write commits —
                                            // recreate() now reads the fresh value.
                                            activity?.recreate()
                                        }
                                    } else {
                                        showDialog = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = code == current, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(fl, fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.btn_close))
                }
            },
        )
    }
}
