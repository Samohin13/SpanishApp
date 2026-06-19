package com.spanishapp.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.ChatMessageDao
import com.spanishapp.domain.chat.ChatScenario
import com.spanishapp.domain.chat.ChatScenarios
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Архив чатов — список всех сценариев с активными сессиями
 * (где есть хотя бы одно сообщение). Tap на карточку → открывается чат
 * с этим сценарием. Long-press / иконка корзины → удалить сессию.
 *
 * Сценарии БЕЗ сообщений не показываются — это архив активного.
 * Для нового чата с конкретным сценарием юзер открывает чат и
 * выбирает сценарий из ScenarioStrip.
 */

data class ChatArchiveItem(
    val scenario: ChatScenario,
    val msgCount: Int,
    val lastTs: Long,
    val lastPreview: String,
)

@HiltViewModel
class ChatArchiveViewModel @Inject constructor(
    private val dao: ChatMessageDao,
) : ViewModel() {

    val items: StateFlow<List<ChatArchiveItem>> = dao.observeSessionsMeta()
        .map { metas ->
            metas.mapNotNull { meta ->
                val scenario = ChatScenarios.byId(meta.sessionId) ?: return@mapNotNull null
                ChatArchiveItem(
                    scenario = scenario,
                    msgCount = meta.msgCount,
                    lastTs = meta.lastTs,
                    lastPreview = meta.lastContent
                        .substringBefore("⟦RU⟧")
                        .substringBefore("CORRECTIONS_JSON:")
                        .replace(Regex("\\*\\*"), "")
                        .trim()
                        .take(80),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteSession(sessionId: String) {
        viewModelScope.launch { dao.clearSession(sessionId) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatArchiveScreen(
    navController: NavHostController,
    vm: ChatArchiveViewModel = hiltViewModel(),
) {
    val items by vm.items.collectAsStateWithLifecycle()
    val accent = Color(0xFFFF8A3D)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        "Архив чатов",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Архив пуст",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Начни первый чат с ИИ-репетитором",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(items, key = { it.scenario.id }) { item ->
                    ArchiveRow(
                        item = item,
                        accent = accent,
                        onOpen = {
                            // Передаём scenario id обратно в чат через savedStateHandle
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("picked_scenario_id", item.scenario.id)
                            navController.popBackStack()
                        },
                        onDelete = { vm.deleteSession(item.scenario.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveRow(
    item: ChatArchiveItem,
    accent: Color,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.scenario.emoji, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        item.scenario.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        formatTime(item.lastTs),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    item.lastPreview,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${item.msgCount} сообщений",
                    fontSize = 10.5.sp,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun formatTime(ts: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - ts
    // v1.25.95: Locale.getDefault() — для EN/UK/ES юзеров корректные weekday names.
    val today = SimpleDateFormat("HH:mm", Locale.getDefault())
    val withinWeek = SimpleDateFormat("EEEE", Locale.getDefault())
    val older = SimpleDateFormat("d MMM", Locale.getDefault())
    return when {
        diff < 24L * 60 * 60 * 1000 -> today.format(Date(ts))
        diff < 7L * 24 * 60 * 60 * 1000 -> withinWeek.format(Date(ts)).replaceFirstChar { it.uppercase() }
        else -> older.format(Date(ts))
    }
}
