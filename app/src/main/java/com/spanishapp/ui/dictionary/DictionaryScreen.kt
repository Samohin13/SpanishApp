package com.spanishapp.ui.dictionary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.spanishapp.R
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import android.content.Intent
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
import com.spanishapp.ui.components.tappableForSpeak
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
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
    private val wordDao: WordListDao,
    private val wDao: WordDao,
    private val tts: SpanishTts
) : ViewModel() {

    // ── Поиск ─────────────────────────────────────────────────
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // ── История поиска (последние 10 запросов) ─────────────────
    private val historyPrefs = appContext.getSharedPreferences("dict_search_history", android.content.Context.MODE_PRIVATE)
    private val _searchHistory = MutableStateFlow(loadHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private fun loadHistory(): List<String> =
        historyPrefs.getString("queries", "")
            ?.split("\n")
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    fun addToHistory(q: String) {
        val trimmed = q.trim()
        if (trimmed.length < 2) return
        val updated = (listOf(trimmed) + _searchHistory.value.filter { !it.equals(trimmed, ignoreCase = true) }).take(10)
        _searchHistory.value = updated
        historyPrefs.edit().putString("queries", updated.joinToString("\n")).apply()
    }

    fun removeFromHistory(q: String) {
        val updated = _searchHistory.value.filter { it != q }
        _searchHistory.value = updated
        historyPrefs.edit().putString("queries", updated.joinToString("\n")).apply()
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
        historyPrefs.edit().remove("queries").apply()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val _allWords: StateFlow<List<WordEntity>> = _query
        .debounce(200)
        .flatMapLatest { q ->
            if (q.length >= 2) wDao.search(q) else wDao.getAllWords()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wordsOnly: StateFlow<List<WordEntity>> = _allWords
        .map { list -> list.filter { !isPhrase(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val phrases: StateFlow<List<WordEntity>> = _allWords
        .map { list -> list.filter { isPhrase(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun isPhrase(w: WordEntity): Boolean {
        if (w.wordType == "phrase") return true
        val s = w.spanish.trim()
        // Восклицательные / вопросительные конструкции — это фразы
        if (s.contains('¿') || s.contains('¡') || s.contains('?') || s.contains('!')) return true
        // Считаем «слова» в испанской части, отбрасывая ведущий артикль
        val tokens = s.split(' ').filter { it.isNotBlank() }
        val articles = setOf("el","la","los","las","un","una","unos","unas")
        val core = if (tokens.isNotEmpty() && tokens[0].lowercase() in articles) tokens.drop(1) else tokens
        // 3+ значимых слова → это фраза, а не «слово»
        return core.size >= 3
    }

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
    val query         by vm.query.collectAsState()
    val wordsOnly     by vm.wordsOnly.collectAsState()
    val phrases       by vm.phrases.collectAsState()
    val myLists       by vm.myLists.collectAsState()
    val listWords     by vm.listWords.collectAsState()
    val selectedId    by vm.selectedListId.collectAsState()
    val membership    by vm.wordListMembership.collectAsState()
    val searchHistory by vm.searchHistory.collectAsState()

    // Вкладки: 0 = Все слова, 1 = Фразы, 2 = Мои списки
    var tab by remember { mutableIntStateOf(0) }

    // Bottom sheets
    var wordDetail by remember { mutableStateOf<WordEntity?>(null) }
    var addToListWord by remember { mutableStateOf<WordEntity?>(null) }
    var showCreateList by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    com.spanishapp.ui.components.AnimatedScreenTitle(
                        text = "📖 " + (if (selectedId == null) stringResource(R.string.dict_title) else myLists.firstOrNull { it.id == selectedId }?.name ?: stringResource(R.string.dict_list)),
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
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
                        text = { Text(stringResource(R.string.dict_tab_all)) })
                    Tab(selected = tab == 1, onClick = { tab = 1 },
                        text = { Text(stringResource(R.string.dict_tab_phrases)) })
                    Tab(selected = tab == 2, onClick = { tab = 2 },
                        text = { Text(stringResource(R.string.dict_tab_my_lists)) })
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
                        query         = query,
                        words         = wordsOnly,
                        membership    = membership,
                        searchHistory = searchHistory,
                        onQuery       = vm::setQuery,
                        onSubmitSearch = vm::addToHistory,
                        onClearHistory = vm::clearSearchHistory,
                        onRemoveHistoryItem = vm::removeFromHistory,
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
                // ── Фразы ─────────────────────────────────
                tab == 1 -> {
                    AllWordsContent(
                        query         = query,
                        words         = phrases,
                        membership    = membership,
                        searchHistory = searchHistory,
                        onQuery       = vm::setQuery,
                        onSubmitSearch = vm::addToHistory,
                        onClearHistory = vm::clearSearchHistory,
                        onRemoveHistoryItem = vm::removeFromHistory,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AllWordsContent(
    query: String,
    words: List<WordEntity>,
    membership: Map<Int, List<Int>>,
    searchHistory: List<String>,
    onQuery: (String) -> Unit,
    onSubmitSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    onRemoveHistoryItem: (String) -> Unit,
    onWordClick: (WordEntity) -> Unit,
    onAddToList: (WordEntity) -> Unit,
    onSpeak: (WordEntity) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showIndex = query.isEmpty() && words.size > 30
    var historyExpanded by remember { mutableStateOf(false) }

    // Группировка по букве + индекс позиции буквы в LazyColumn (учётом sticky-заголовков)
    data class Group(val letter: Char, val words: List<WordEntity>)
    val grouped = remember(words, showIndex) {
        if (!showIndex) emptyList()
        else words.groupBy { firstSpanishLetter(it.spanish) }
            .map { (letter, ws) -> Group(letter, ws) }
    }
    // Map: letter → индекс sticky-заголовка в LazyColumn
    val letterIndex = remember(grouped) {
        val map = LinkedHashMap<Char, Int>()
        var idx = 0
        for (g in grouped) {
            map[g.letter] = idx
            idx += 1 + g.words.size  // header + слова
        }
        map
    }

    Column(Modifier.fillMaxSize()) {
        // ── Поиск + история ───────────────────────────────
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value       = query,
                onValueChange = { v ->
                    onQuery(v)
                    historyExpanded = false
                },
                placeholder = { Text(stringResource(R.string.dict_search_placeholder)) },
                leadingIcon = {
                    IconButton(
                        onClick = {
                            if (searchHistory.isNotEmpty()) historyExpanded = !historyExpanded
                        },
                        modifier = Modifier.size(40.dp)
                    ) { Icon(Icons.Default.Search, null) }
                },
                trailingIcon = {
                    Row {
                        AnimatedVisibility(query.isNotEmpty()) {
                            IconButton(onClick = {
                                if (query.isNotBlank()) onSubmitSearch(query)
                                onQuery("")
                            }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                        if (query.isEmpty() && searchHistory.isNotEmpty()) {
                            IconButton(onClick = { historyExpanded = !historyExpanded }) {
                                Icon(
                                    if (historyExpanded) Icons.Default.ArrowDropUp
                                    else Icons.Default.ArrowDropDown, null)
                            }
                        }
                    }
                },
                singleLine = true,
                shape      = RoundedCornerShape(14.dp),
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )

            DropdownMenu(
                expanded = historyExpanded && searchHistory.isNotEmpty(),
                onDismissRequest = { historyExpanded = false },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .heightIn(max = 320.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.dict_recent_search),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = {
                        onClearHistory()
                        historyExpanded = false
                    }) {
                        Text(stringResource(R.string.dict_clear), style = MaterialTheme.typography.labelSmall)
                    }
                }
                HorizontalDivider()
                searchHistory.forEach { q ->
                    DropdownMenuItem(
                        text = { Text(q) },
                        leadingIcon = { Icon(Icons.Default.History, null,
                            modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onRemoveHistoryItem(q) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        onClick = {
                            onQuery(q)
                            historyExpanded = false
                        }
                    )
                }
            }
        }

        // ── Список слов ───────────────────────────────────
        Row(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                contentPadding     = PaddingValues(start = 16.dp, end = if (showIndex) 4.dp else 16.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (showIndex) {
                    // С группами и sticky-заголовками
                    grouped.forEach { group ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = group.letter.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        items(group.words, key = { it.id }) { word ->
                            WordRow(
                                word        = word,
                                query       = query,
                                isInAnyList = !membership[word.id].isNullOrEmpty(),
                                onWordClick = { onWordClick(word) },
                                onAddToList = { onAddToList(word) },
                                onSpeak     = { onSpeak(word) }
                            )
                        }
                    }
                } else {
                    // Поиск активен или мало слов — без группировки
                    items(words, key = { it.id }) { word ->
                        WordRow(
                            word        = word,
                            query       = query,
                            isInAnyList = !membership[word.id].isNullOrEmpty(),
                            onWordClick = { onWordClick(word) },
                            onAddToList = { onAddToList(word) },
                            onSpeak     = { onSpeak(word) }
                        )
                    }
                }
            }

            if (showIndex) {
                AlphabetIndexBar(
                    available = letterIndex.keys,
                    onLetter  = { letter ->
                        letterIndex[letter]?.let { idx ->
                            scope.launch { listState.scrollToItem(idx) }
                        }
                    }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  АЛФАВИТНЫЙ ИНДЕКС-БАР
// ══════════════════════════════════════════════════════════════

private val SPANISH_ALPHABET = listOf(
    'A','B','C','D','E','F','G','H','I','J','K','L','M',
    'N','Ñ','O','P','Q','R','S','T','U','V','W','X','Y','Z'
)

private fun firstSpanishLetter(spanish: String): Char {
    val s = spanish.trim()
    val tokens = s.split(' ').filter { it.isNotBlank() }
    val articles = setOf("el","la","los","las","un","una","unos","unas")
    val core = if (tokens.isNotEmpty() && tokens[0].lowercase() in articles)
        tokens.drop(1).joinToString(" ")
    else s
    val c = core.firstOrNull()?.uppercaseChar() ?: return '#'
    return when (c) {
        'Á' -> 'A'; 'É' -> 'E'; 'Í' -> 'I'; 'Ó' -> 'O'; 'Ú' -> 'U'; 'Ü' -> 'U'
        else -> c
    }
}

@Composable
private fun AlphabetIndexBar(
    available: Set<Char>,
    onLetter: (Char) -> Unit
) {
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    val density = LocalDensity.current

    Box(
        Modifier
            .width(24.dp)
            .fillMaxHeight()
            .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .pointerInput(available) {
                val handle: (Float) -> Unit = { y ->
                    val idx = ((y / size.height) * SPANISH_ALPHABET.size)
                        .toInt().coerceIn(0, SPANISH_ALPHABET.size - 1)
                    val letter = SPANISH_ALPHABET[idx]
                    if (letter in available && letter != activeLetter) {
                        activeLetter = letter
                        onLetter(letter)
                    }
                }
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        if (change.pressed) {
                            handle(change.position.y)
                        } else if (activeLetter != null) {
                            activeLetter = null
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (c in SPANISH_ALPHABET) {
                val isAvailable = c in available
                val isActive = c == activeLetter
                Text(
                    text = c.toString(),
                    fontSize = 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isActive       -> MaterialTheme.colorScheme.primary
                        isAvailable    -> MaterialTheme.colorScheme.onSurfaceVariant
                        else           -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                    }
                )
            }
        }

        // Большой "пузырь" с активной буквой слева от бара
        activeLetter?.let { letter ->
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-56).dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
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
            Text(stringResource(R.string.dict_lists_count, lists.size), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (lists.size < 20) {
                FilledTonalButton(
                    onClick      = onCreateList,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.dict_new_list))
                }
            }
        }

        if (lists.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                       verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📋", fontSize = 48.sp)
                    Text(stringResource(R.string.dict_no_lists), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.dict_no_lists_hint),
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
            title = { Text(stringResource(R.string.dict_rename_list)) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.dict_name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (text.isNotBlank()) onRenameList(list, text)
                    renaming = null
                }) { Text(stringResource(R.string.dict_save)) }
            },
            dismissButton = {
                TextButton(onClick = { renaming = null }) { Text(stringResource(R.string.dict_cancel)) }
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
    val context = LocalContext.current
    var exportMenu by remember { mutableStateOf(false) }
    val shareTitle = stringResource(R.string.dict_share_list)

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.dict_words_count, words.size),
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (words.isNotEmpty()) {
                Box {
                    IconButton(onClick = { exportMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Share, stringResource(R.string.dict_export_cd),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(
                        expanded = exportMenu,
                        onDismissRequest = { exportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.dict_export_txt)) },
                            leadingIcon = { Icon(Icons.Default.Description, null) },
                            onClick = {
                                exportMenu = false
                                shareAsText(context, exportAsTxt(words), "spanish_words.txt", shareTitle)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.dict_export_csv)) },
                            leadingIcon = { Icon(Icons.Default.TableChart, null) },
                            onClick = {
                                exportMenu = false
                                shareAsText(context, exportAsCsv(words), "spanish_words.csv", shareTitle)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.dict_export_pdf)) },
                            leadingIcon = { Icon(Icons.Default.Print, null) },
                            onClick = {
                                exportMenu = false
                                shareAsText(context, exportAsTxt(words), "spanish_words.txt", shareTitle)
                            }
                        )
                    }
                }
            }
        }

        if (words.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                       verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📝", fontSize = 48.sp)
                    Text(stringResource(R.string.dict_list_empty), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.dict_list_empty_hint),
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
    query: String = "",
    isInAnyList: Boolean,
    showRemove: Boolean = false,
    onWordClick: () -> Unit,
    onAddToList: () -> Unit,
    onRemove: (() -> Unit)? = null,
    onSpeak: () -> Unit
) {
    val highlightColor = MaterialTheme.colorScheme.primary
    com.spanishapp.ui.components.PressableCard(
        onClick = onWordClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier          = Modifier.padding(start = 10.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Индикатор прогресса + уровень (всё в одной колонке слева)
            ProgressLevelIndicator(word)
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Испанское слово + пометки
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = highlightMatch(word.spanish, query, highlightColor),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    when (word.verbSubtype) {
                        "irregular" -> VerbBadge(stringResource(R.string.dict_badge_irregular), Color(0xFFE11D48))
                        "stem"      -> VerbBadge(stringResource(R.string.dict_badge_stem), Color(0xFFD97706))
                    }
                    if (word.category.startsWith("adult_")) {
                        VerbBadge("18+", Color(0xFF9C27B0))
                    }
                }
                Text(
                    text = highlightMatch(word.russian, query, highlightColor),
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
    val color = levelColor(level)
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

private fun levelColor(level: String): Color = when (level) {
    "A1" -> Color(0xFF059669)
    "A2" -> Color(0xFF0284C7)
    "B1" -> Color(0xFFD97706)
    "B2" -> Color(0xFFF05A28)
    "C1" -> Color(0xFF7C3AED)
    else -> Color(0xFF9CA3AF)
}

/**
 * Прогресс изучения слова:
 * 🟢 выучено  · isLearned == true
 * 🔴 слабое   · totalReviews >= 3 && correctRatio < 0.6
 * 🟡 в процессе · totalReviews > 0
 * ⚪ новое   · totalReviews == 0
 */
private enum class WordProgress(val color: Color, val label: String) {
    LEARNED(Color(0xFF22C55E), "выучено"),
    WEAK(Color(0xFFEF4444), "слабое"),
    IN_PROGRESS(Color(0xFFF59E0B), "учу"),
    NEW(Color(0xFFCBD5E1), "новое")
}

private fun progressOf(word: WordEntity): WordProgress {
    if (word.isLearned) return WordProgress.LEARNED
    if (word.totalReviews == 0) return WordProgress.NEW
    val ratio = word.correctReviews.toFloat() / word.totalReviews
    if (word.totalReviews >= 3 && ratio < 0.6f) return WordProgress.WEAK
    return WordProgress.IN_PROGRESS
}

@Composable
private fun ProgressLevelIndicator(word: WordEntity) {
    val progress = progressOf(word)
    val lvlColor = levelColor(word.level)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.width(28.dp)
    ) {
        // Большой круг прогресса
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(progress.color)
        )
        // Уровень
        Text(
            word.level,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = lvlColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Подсветка вхождения query в text (без учёта регистра).
 */
@Composable
private fun highlightMatch(text: String, query: String, color: Color): AnnotatedString {
    val q = query.trim()
    if (q.length < 2) return AnnotatedString(text)
    val lowerText = text.lowercase()
    val lowerQ = q.lowercase()
    if (lowerQ !in lowerText) return AnnotatedString(text)

    return buildAnnotatedString {
        var start = 0
        var i = lowerText.indexOf(lowerQ)
        while (i >= 0) {
            append(text.substring(start, i))
            withStyle(
                SpanStyle(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    background = color.copy(alpha = 0.15f)
                )
            ) {
                append(text.substring(i, i + lowerQ.length))
            }
            start = i + lowerQ.length
            i = lowerText.indexOf(lowerQ, start)
        }
        append(text.substring(start))
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

    com.spanishapp.ui.components.PressableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer
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
                    stringResource(R.string.dict_words_count, list.wordCount),
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
                        text     = { Text(stringResource(R.string.dict_rename)) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick  = { showMenu = false; onRename() }
                    )
                    DropdownMenuItem(
                        text     = { Text(stringResource(R.string.dict_delete), color = MaterialTheme.colorScheme.error) },
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .tappableForSpeak(onSpeak)
                    .padding(8.dp)) {
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
            Text(stringResource(R.string.dict_translation), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
            Text(word.russian, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium)

            // Пример
            if (word.example.isNotBlank()) {
                Text(stringResource(R.string.dict_example), style = MaterialTheme.typography.labelMedium,
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
                Text(stringResource(R.string.dict_add_to_list), style = MaterialTheme.typography.labelMedium,
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
                    StatItem(stringResource(R.string.dict_stat_reviews), "${word.totalReviews}")
                    StatItem(stringResource(R.string.dict_stat_accuracy), "$acc%")
                    if (word.isLearned) StatItem(stringResource(R.string.dict_stat_status), stringResource(R.string.dict_stat_status_learned))
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

            Text(stringResource(R.string.dict_add_word_to_list, word.spanish),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)

            if (myLists.isEmpty()) {
                Text(stringResource(R.string.dict_no_lists_yet),
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
                                if (full) Text(stringResource(R.string.dict_list_full),
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
                    Text(stringResource(R.string.dict_create_new_list))
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
        title   = { Text(stringResource(R.string.dict_new_list)) },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (error) {
                    Text(stringResource(R.string.dict_max_lists_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)
                } else {
                    OutlinedTextField(
                        value         = name,
                        onValueChange = { if (it.length <= 30) name = it },
                        label         = { Text(stringResource(R.string.dict_name_label)) },
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
                ) { Text(stringResource(R.string.dict_create)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dict_cancel)) }
        }
    )
}

// ══════════════════════════════════════════════════════════════
//  ЭКСПОРТ СПИСКА
// ══════════════════════════════════════════════════════════════

private fun exportAsTxt(words: List<WordEntity>): String = buildString {
    appendLine("Список слов · ${words.size}")
    appendLine("─".repeat(40))
    words.forEach { w ->
        appendLine("${w.spanish} — ${w.russian}")
        if (w.example.isNotBlank()) appendLine("   ${w.example}")
        appendLine("   [${w.level} · ${w.category}]")
        appendLine()
    }
}

private fun exportAsCsv(words: List<WordEntity>): String = buildString {
    appendLine("spanish,russian,example,level,category,type")
    words.forEach { w ->
        append(csvEscape(w.spanish)); append(',')
        append(csvEscape(w.russian)); append(',')
        append(csvEscape(w.example)); append(',')
        append(csvEscape(w.level)); append(',')
        append(csvEscape(w.category)); append(',')
        append(csvEscape(w.wordType))
        appendLine()
    }
}

private fun csvEscape(s: String): String {
    val needsEscape = s.contains(',') || s.contains('"') || s.contains('\n')
    return if (needsEscape) "\"${s.replace("\"", "\"\"")}\"" else s
}

private fun shareAsText(context: android.content.Context, text: String, subject: String, chooserTitle: String = subject) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
