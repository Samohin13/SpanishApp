package com.spanishapp.ui.dictionary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.dao.WordListDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.data.db.entity.WordListEntity
import com.spanishapp.data.db.entity.WordListEntryEntity
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ══════════════════════════════════════════════════════════════
//  VIEW MODEL
// ══════════════════════════════════════════════════════════════

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val wordDao: WordListDao,
    private val wDao: WordDao,
    private val tts: SpanishTts
) : ViewModel() {

    // ── Поиск ─────────────────────────────────────────────────
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val words: StateFlow<List<WordEntity>> = _query
        .debounce(200)
        .flatMapLatest { q ->
            if (q.length >= 2) wDao.search(q) else wDao.getAllWords(limit = 8000)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Мои списки ────────────────────────────────────────────
    val myLists: StateFlow<List<WordListEntity>> = wordDao.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Слова выбранного списка
    private val _selectedListId = MutableStateFlow<Int?>(null)
    val selectedListId: StateFlow<Int?> = _selectedListId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val listWords: StateFlow<List<WordEntity>> = _selectedListId
        .flatMapLatest { id ->
            if (id != null) wordDao.getWordsInList(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ID списков в которых находится данное слово
    private val _wordListMembership = MutableStateFlow<Map<Int, List<Int>>>(emptyMap())
    val wordListMembership: StateFlow<Map<Int, List<Int>>> = _wordListMembership.asStateFlow()

    // ── Действия ──────────────────────────────────────────────
    fun setQuery(q: String) { _query.value = q }
    fun selectList(id: Int?) { _selectedListId.value = id }

    fun speak(word: WordEntity) = viewModelScope.launch { tts.speak(word.spanish) }

    fun createList(name: String) = viewModelScope.launch {
        val count = wordDao.getListCount()
        if (count >= 20) return@launch
        wordDao.insertList(WordListEntity(name = name.trim(), colorIndex = count % LIST_COLORS.size))
    }

    fun deleteList(list: WordListEntity) = viewModelScope.launch {
        wordDao.deleteList(list)
        if (_selectedListId.value == list.id) _selectedListId.value = null
    }

    fun renameList(list: WordListEntity, newName: String) = viewModelScope.launch {
        wordDao.updateList(list.copy(name = newName.trim()))
    }

    fun addWordToList(listId: Int, wordId: Int) = viewModelScope.launch {
        val count = wordDao.countWordsInList(listId)
        if (count >= 150) return@launch
        wordDao.addEntry(WordListEntryEntity(listId = listId, wordId = wordId))
        wordDao.refreshWordCount(listId)
        refreshMembership(wordId)
    }

    fun removeWordFromList(listId: Int, wordId: Int) = viewModelScope.launch {
        wordDao.removeEntry(listId, wordId)
        wordDao.refreshWordCount(listId)
        refreshMembership(wordId)
    }

    fun loadMembership(wordId: Int) = viewModelScope.launch {
        refreshMembership(wordId)
    }

    private suspend fun refreshMembership(wordId: Int) {
        val lists = wordDao.getListIdsForWord(wordId)
        _wordListMembership.update { it + (wordId to lists) }
    }

    companion object {
        val LIST_COLORS = listOf(
            Color(0xFF8BC34A), Color(0xFFF05A28), Color(0xFFF6C445),
            Color(0xFF0284C7), Color(0xFF7C3AED), Color(0xFFE11D48),
            Color(0xFF059669), Color(0xFFD97706)
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  SCREEN
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    navController: NavHostController,
    vm: DictionaryViewModel = hiltViewModel()
) {
    val query      by vm.query.collectAsState()
    val words      by vm.words.collectAsState()
    val myLists    by vm.myLists.collectAsState()
    val listWords  by vm.listWords.collectAsState()
    val selectedId by vm.selectedListId.collectAsState()
    val membership by vm.wordListMembership.collectAsState()

    // Вкладки: 0 = Все слова, 1 = Мои списки
    var tab by remember { mutableIntStateOf(0) }

    // Bottom sheets
    var wordDetail by remember { mutableStateOf<WordEntity?>(null) }
    var addToListWord by remember { mutableStateOf<WordEntity?>(null) }
    var showCreateList by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedId == null) "Словарь" else myLists.firstOrNull { it.id == selectedId }?.name ?: "Список") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedId != null) vm.selectList(null)
                        else navController.popBackStack()
                    }) {
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
            // ── Вкладки ──────────────────────────────────────
            if (selectedId == null) {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 },
                        text = { Text("Все слова") })
                    Tab(selected = tab == 1, onClick = { tab = 1 },
                        text = { Text("Мои списки") })
                }
            }

            when {
                // ── Просмотр списка ───────────────────────
                selectedId != null -> {
                    ListWordsContent(
                        words      = listWords,
                        listId     = selectedId!!,
                        membership = membership,
                        onWordClick  = { w ->
                            vm.loadMembership(w.id)
                            wordDetail = w
                        },
                        onAddToList  = { w ->
                            vm.loadMembership(w.id)
                            addToListWord = w
                        },
                        onRemove     = { vm.removeWordFromList(selectedId!!, it.id) },
                        onSpeak      = { vm.speak(it) }
                    )
                }
                // ── Все слова ─────────────────────────────
                tab == 0 -> {
                    AllWordsContent(
                        query      = query,
                        words      = words,
                        membership = membership,
                        onQuery    = vm::setQuery,
                        onWordClick = { w ->
                            vm.loadMembership(w.id)
                            wordDetail = w
                        },
                        onAddToList = { w ->
                            vm.loadMembership(w.id)
                            addToListWord = w
                        },
                        onSpeak    = { vm.speak(it) }
                    )
                }
                // ── Мои списки ────────────────────────────
                else -> {
                    MyListsContent(
                        lists         = myLists,
                        onSelectList  = { vm.selectList(it.id) },
                        onDeleteList  = { vm.deleteList(it) },
                        onRenameList  = { list, name -> vm.renameList(list, name) },
                        onCreateList  = { showCreateList = true }
                    )
                }
            }
        }
    }

    // ── Карточка слова ────────────────────────────────────────
    wordDetail?.let { word ->
        WordDetailSheet(
            word     = word,
            listIds  = membership[word.id] ?: emptyList(),
            myLists  = myLists,
            onSpeak  = { vm.speak(word) },
            onAddToList   = { listId -> vm.addWordToList(listId, word.id) },
            onRemoveFromList = { listId -> vm.removeWordFromList(listId, word.id) },
            onDismiss = { wordDetail = null }
        )
    }

    // ── Добавить в список ─────────────────────────────────────
    addToListWord?.let { word ->
        AddToListSheet(
            word     = word,
            myLists  = myLists,
            listIds  = membership[word.id] ?: emptyList(),
            onAdd    = { listId -> vm.addWordToList(listId, word.id) },
            onRemove = { listId -> vm.removeWordFromList(listId, word.id) },
            onCreate = { showCreateList = true },
            onDismiss = { addToListWord = null }
        )
    }

    // ── Создать новый список ──────────────────────────────────
    if (showCreateList) {
        CreateListDialog(
            listCount = myLists.size,
            onCreate  = { name ->
                vm.createList(name)
                showCreateList = false
            },
            onDismiss = { showCreateList = false }
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  TAB: ВСЕ СЛОВА
// ══════════════════════════════════════════════════════════════

@Composable
private fun AllWordsContent(
    query: String,
    words: List<WordEntity>,
    membership: Map<Int, List<Int>>,
    onQuery: (String) -> Unit,
    onWordClick: (WordEntity) -> Unit,
    onAddToList: (WordEntity) -> Unit,
    onSpeak: (WordEntity) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value       = query,
            onValueChange = onQuery,
            placeholder = { Text("Поиск…") },
            leadingIcon  = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                AnimatedVisibility(query.isNotEmpty()) {
                    IconButton(onClick = { onQuery("") }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
            singleLine = true,
            shape      = RoundedCornerShape(14.dp),
            modifier   = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        Text(
            "${words.size} слов",
            style  = MaterialTheme.typography.bodySmall,
            color  = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
        )

        LazyColumn(
            contentPadding     = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(words, key = { it.id }) { word ->
                WordRow(
                    word        = word,
                    isInAnyList = !membership[word.id].isNullOrEmpty(),
                    onWordClick = { onWordClick(word) },
                    onAddToList = { onAddToList(word) },
                    onSpeak     = { onSpeak(word) }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TAB: МОИ СПИСКИ
// ══════════════════════════════════════════════════════════════

@Composable
private fun MyListsContent(
    lists: List<WordListEntity>,
    onSelectList: (WordListEntity) -> Unit,
    onDeleteList: (WordListEntity) -> Unit,
    onRenameList: (WordListEntity, String) -> Unit,
    onCreateList: () -> Unit
) {
    var renaming by remember { mutableStateOf<WordListEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        // Заголовок + кнопка создать
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("${lists.size}/20 списков", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (lists.size < 20) {
                FilledTonalButton(
                    onClick      = onCreateList,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Новый список")
                }
            }
        }

        if (lists.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                       verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📋", fontSize = 48.sp)
                    Text("Нет списков", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Создайте список, чтобы собирать слова",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding     = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lists, key = { it.id }) { list ->
                    ListCard(
                        list       = list,
                        onClick    = { onSelectList(list) },
                        onRename   = { renaming = list },
                        onDelete   = { onDeleteList(list) }
                    )
                }
            }
        }
    }

    renaming?.let { list ->
        var text by remember { mutableStateOf(list.name) }
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text("Переименовать список") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Название") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (text.isNotBlank()) onRenameList(list, text)
                    renaming = null
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { renaming = null }) { Text("Отмена") }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  СПИСОК — СОДЕРЖИМОЕ
// ══════════════════════════════════════════════════════════════

@Composable
private fun ListWordsContent(
    words: List<WordEntity>,
    listId: Int,
    membership: Map<Int, List<Int>>,
    onWordClick: (WordEntity) -> Unit,
    onAddToList: (WordEntity) -> Unit,
    onRemove: (WordEntity) -> Unit,
    onSpeak: (WordEntity) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            "${words.size}/150 слов",
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 6.dp)
        )

        if (words.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                       verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📝", fontSize = 48.sp)
                    Text("Список пуст", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Нажмите «+» у слова в разделе «Все слова»",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding     = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(words, key = { it.id }) { word ->
                    WordRow(
                        word        = word,
                        isInAnyList = true,
                        showRemove  = true,
                        onWordClick = { onWordClick(word) },
                        onAddToList = { onAddToList(word) },
                        onRemove    = { onRemove(word) },
                        onSpeak     = { onSpeak(word) }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  КОМПОНЕНТЫ
// ══════════════════════════════════════════════════════════════

@Composable
private fun WordRow(
    word: WordEntity,
    isInAnyList: Boolean,
    showRemove: Boolean = false,
    onWordClick: () -> Unit,
    onAddToList: () -> Unit,
    onRemove: (() -> Unit)? = null,
    onSpeak: () -> Unit
) {
    Surface(
        onClick    = onWordClick,
        shape      = RoundedCornerShape(14.dp),
        color      = MaterialTheme.colorScheme.surfaceContainer,
        modifier   = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Индикатор уровня
            LevelDot(word.level)
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Испанское слово + пометка глагола
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        word.spanish,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    when (word.verbSubtype) {
                        "irregular" -> VerbBadge("неправ.", Color(0xFFE11D48))
                        "stem"      -> VerbBadge("откл.", Color(0xFFD97706))
                    }
                }
                Text(
                    word.russian,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (word.example.isNotBlank()) {
                    Text(
                        word.example,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        maxLines  = 1,
                        overflow  = TextOverflow.Ellipsis,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Озвучка
            IconButton(onClick = onSpeak, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.VolumeUp, null,
                    modifier = Modifier.size(18.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Удалить из списка / добавить в список
            if (showRemove && onRemove != null) {
                IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.RemoveCircleOutline, null,
                        modifier = Modifier.size(18.dp),
                        tint     = MaterialTheme.colorScheme.error)
                }
            } else {
                IconButton(onClick = onAddToList, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (isInAnyList) Icons.Default.PlaylistAddCheck else Icons.Default.PlaylistAdd,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint     = if (isInAnyList) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VerbBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            label,
            style     = MaterialTheme.typography.labelSmall,
            fontSize  = 8.sp,
            color     = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LevelDot(level: String) {
    val color = when (level) {
        "A1" -> Color(0xFF059669)
        "A2" -> Color(0xFF0284C7)
        "B1" -> Color(0xFFD97706)
        "B2" -> Color(0xFFF05A28)
        "C1" -> Color(0xFF7C3AED)
        else -> Color(0xFF9CA3AF)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color))
        Text(level, style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ListCard(
    list: WordListEntity,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val color = DictionaryViewModel.LIST_COLORS[list.colorIndex % DictionaryViewModel.LIST_COLORS.size]
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(16.dp),
        color   = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Цветной круг
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📋", fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(list.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(
                    "${list.wordCount}/150 слов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Прогресс
            if (list.wordCount > 0) {
                CircularProgressIndicator(
                    progress   = { (list.wordCount / 150f).coerceIn(0f, 1f) },
                    modifier   = Modifier.size(28.dp),
                    strokeWidth = 3.dp,
                    color      = color,
                    trackColor = color.copy(alpha = 0.15f)
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, null,
                        modifier = Modifier.size(18.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text     = { Text("Переименовать") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick  = { showMenu = false; onRename() }
                    )
                    DropdownMenuItem(
                        text     = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, null,
                            tint = MaterialTheme.colorScheme.error) },
                        onClick  = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  BOTTOM SHEETS
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordDetailSheet(
    word: WordEntity,
    listIds: List<Int>,
    myLists: List<WordListEntity>,
    onSpeak: () -> Unit,
    onAddToList: (Int) -> Unit,
    onRemoveFromList: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Слово + уровень + озвучка
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(word.spanish,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        LevelDot(word.level)
                        Text(word.level, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("· ${word.wordType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                FilledTonalIconButton(onClick = onSpeak) {
                    Icon(Icons.Default.VolumeUp, null)
                }
            }

            HorizontalDivider()

            // Перевод
            Text("Перевод", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
            Text(word.russian, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium)

            // Пример
            if (word.example.isNotBlank()) {
                Text("Пример", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Surface(shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Text("« ${word.example} »",
                        modifier  = Modifier.padding(12.dp),
                        style     = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color     = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Мои списки
            if (myLists.isNotEmpty()) {
                HorizontalDivider()
                Text("Добавить в список", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                myLists.forEach { list ->
                    val inList = listIds.contains(list.id)
                    val color  = DictionaryViewModel.LIST_COLORS[list.colorIndex % DictionaryViewModel.LIST_COLORS.size]
                    Surface(
                        onClick = {
                            if (inList) onRemoveFromList(list.id) else onAddToList(list.id)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (inList) color.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(list.name, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (inList) FontWeight.SemiBold else FontWeight.Normal)
                            if (inList) Icon(Icons.Default.Check, null,
                                tint = color, modifier = Modifier.size(18.dp))
                            else Icon(Icons.Default.Add, null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Статистика
            if (word.totalReviews > 0) {
                HorizontalDivider()
                val acc = (word.correctReviews * 100f / word.totalReviews).toInt()
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatItem("Повторений", "${word.totalReviews}")
                    StatItem("Точность", "$acc%")
                    if (word.isLearned) StatItem("Статус", "✓ Выучено")
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToListSheet(
    word: WordEntity,
    myLists: List<WordListEntity>,
    listIds: List<Int>,
    onAdd: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("Добавить «${word.spanish}» в список",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)

            if (myLists.isEmpty()) {
                Text("У вас ещё нет списков",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                myLists.forEach { list ->
                    val inList = listIds.contains(list.id)
                    val full   = list.wordCount >= 150 && !inList
                    val color  = DictionaryViewModel.LIST_COLORS[list.colorIndex % DictionaryViewModel.LIST_COLORS.size]
                    Surface(
                        onClick = { if (!full) { if (inList) onRemove(list.id) else onAdd(list.id) } },
                        enabled = !full,
                        shape   = RoundedCornerShape(12.dp),
                        color   = if (inList) color.copy(alpha = 0.12f)
                                  else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(list.name, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (inList) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (full) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurface)
                                if (full) Text("Список заполнен (150/150)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                else Text("${list.wordCount}/150",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (inList) Icon(Icons.Default.Check, null, tint = color,
                                modifier = Modifier.size(20.dp))
                            else if (!full) Icon(Icons.Default.Add, null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (myLists.size < 20) {
                OutlinedButton(onClick = { onDismiss(); onCreate() },
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Создать новый список")
                }
            }
        }
    }
}

@Composable
private fun CreateListDialog(
    listCount: Int,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val error = listCount >= 20

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Новый список") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (error) {
                    Text("Максимум 20 списков. Удалите один, чтобы создать новый.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)
                } else {
                    OutlinedTextField(
                        value         = name,
                        onValueChange = { if (it.length <= 30) name = it },
                        label         = { Text("Название (до 30 символов)") },
                        singleLine    = true,
                        isError       = name.isBlank() && name.isNotEmpty()
                    )
                    Text("${name.length}/30",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            if (!error) {
                TextButton(
                    onClick  = { if (name.isNotBlank()) onCreate(name) },
                    enabled  = name.isNotBlank()
                ) { Text("Создать") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
