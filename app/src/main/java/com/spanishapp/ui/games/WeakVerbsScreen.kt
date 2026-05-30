package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.WeakVerbDao
import com.spanishapp.data.db.entity.WeakVerbEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * v1.25.78 VERB-4: экран слабых глаголов.
 *
 * Показывает все глагольные формы где юзер ошибался в Verbos-тренажёре.
 * Пользователь может:
 *  - Удалить отдельную запись (например, понял правило, не хочет в пуле)
 *  - Запустить «Тренировку слабых» — отдельная сессия только из этих форм
 *  - Очистить весь список
 *
 * Записи попадают сюда из [VerbViewModel.recordVerbResult] при неверном
 * ответе. При правильном ответе на ту же форму запись удаляется.
 */
@HiltViewModel
class WeakVerbsViewModel @Inject constructor(
    private val dao: WeakVerbDao,
) : ViewModel() {

    val items: StateFlow<List<WeakVerbEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(key: String) {
        viewModelScope.launch { dao.deleteByKey(key) }
    }

    fun clearAll() {
        viewModelScope.launch { dao.deleteAll() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeakVerbsScreen(
    navController: NavHostController,
    vm: WeakVerbsViewModel = hiltViewModel(),
) {
    val items by vm.items.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Слабые глаголы") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = { vm.clearAll() }) {
                            Text("Очистить", color = Color(0xFFF87171))
                        }
                    }
                },
            )
        },
    ) { pad ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💪", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Слабых глаголов пока нет",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Когда ошибёшься в тренажёре спряжения — глагол попадёт сюда. При правильном повторении автоматически удалится.",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                item {
                    Text(
                        "${items.size} ${pluralForms(items.size)}",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                items(items, key = { it.key }) { entry ->
                    WeakVerbRow(entry, onDelete = { vm.delete(entry.key) })
                }
            }
        }
    }
}

@Composable
private fun WeakVerbRow(entry: WeakVerbEntity, onDelete: () -> Unit) {
    val pronouns = listOf("yo", "tú", "él/ella", "nosotros", "vosotros", "ellos")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1A1A20),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A36)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.verb,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE5E5E5),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${prettyTenseShort(entry.tense)} · ${pronouns.getOrNull(entry.pronounIndex) ?: "?"}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "→ ${entry.correctForm}",
                    fontSize = 14.sp,
                    color = Color(0xFF4ADE80),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFF666666))
            }
        }
    }
}

private fun prettyTenseShort(t: String): String = when (t) {
    "presente" -> "Presente"
    "preterito" -> "Indefinido"
    "imperfecto" -> "Imperfecto"
    "futuro" -> "Futuro"
    "condicional" -> "Condicional"
    "subjuntivo" -> "Subjuntivo"
    "subjuntivo_imperfecto" -> "Subj. Imperf."
    "imperativo" -> "Imperativo"
    else -> t
}

private fun pluralForms(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..14 -> "глаголов"
        mod10 == 1 -> "глагол"
        mod10 in 2..4 -> "глагола"
        else -> "глаголов"
    }
}
