package com.spanishapp.radio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.radio.data.Country
import com.spanishapp.radio.data.Genre
import com.spanishapp.radio.data.Station
import com.spanishapp.radio.data.StationRepository
import com.spanishapp.radio.data.toStation
import com.spanishapp.radio.player.RadioPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RadioViewModel @Inject constructor(
    private val player: RadioPlayerController,
    private val favoritesDao: com.spanishapp.radio.data.RadioFavoriteDao,
    private val catalogDao: com.spanishapp.radio.data.RadioCatalogDao,
    private val catalogRepo: com.spanishapp.radio.data.RadioCatalogRepository,
    private val listeningDao: com.spanishapp.radio.data.RadioListeningDao,
    private val blocklistPrefs: com.spanishapp.radio.data.RadioBlocklistPrefs,
) : ViewModel() {

    /** Сколько минут прослушано всего — для бэйджа в Profile. */
    val totalListeningMinutes: StateFlow<Long> = listeningDao.observeTotalSeconds()
        .map { it / 60 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    /** Состояние подбора станций. */
    enum class DiscoveryState { IDLE, LOADING, READY, ERROR }

    private val _discoveryState = MutableStateFlow(DiscoveryState.IDLE)
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState.asStateFlow()

    val discoveryProgress: StateFlow<Float> = catalogRepo.progress
    val discoveryFoundCount: StateFlow<Int> = catalogRepo.foundCount

    /** Текущий этап discovery — для подписи в баннере. */
    val discoveryStage: StateFlow<com.spanishapp.radio.data.RadioCatalogRepository.DiscoveryStage> =
        catalogRepo.stage

    /** Сообщение об ошибке поиска (null если всё ок). UI показывает баннер с retry. */
    val discoveryError: StateFlow<String?> = catalogRepo.lastErrorMessage

    fun dismissError() { catalogRepo.clearError() }

    /** Тап по кнопке ↻ — пересоздать каталог с нуля. */
    fun refreshCatalog() {
        viewModelScope.launch {
            _discoveryState.value = DiscoveryState.LOADING
            val count = catalogRepo.discoverAndCache()
            // Cache fallback: если discovery вернул 0 (сеть/API упали),
            // но в БД ЕЩЁ ЕСТЬ предыдущий кэш — не считаем это ошибкой.
            // Юзер продолжает слушать что слушал, без вспышки красного баннера.
            val hasAnyCache = catalogDao.count() > 0
            _discoveryState.value = when {
                count > 0 -> DiscoveryState.READY
                hasAnyCache -> DiscoveryState.READY  // живём со старым кэшем
                else -> DiscoveryState.ERROR
            }
            reloadStations()
        }
    }

    /**
     * Тап по тайлу «+ Найти ещё» — дозапросить ещё ~20 станций
     * и добавить к существующему каталогу. Не очищает кэш.
     */
    fun discoverMore() {
        viewModelScope.launch {
            _discoveryState.value = DiscoveryState.LOADING
            val added = catalogRepo.discoverMore(20)
            _discoveryState.value = if (added > 0) DiscoveryState.READY else DiscoveryState.ERROR
            reloadStations()
        }
    }

    private suspend fun reloadStations() {
        val cached = catalogDao.getAll()
        if (cached.isNotEmpty()) {
            _stations.value = cached
                .filter { it.country == _country.value.name }
                .map { it.toStation() }
            val current = currentStation.value
            if (current == null || _stations.value.none { it.id == current.id }) {
                _stations.value.firstOrNull()?.let { tuneToStation(it) }
            }
        }
    }

    val favoriteIds: StateFlow<Set<String>> = favoritesDao.observeAllIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch {
            if (favoriteIds.value.contains(stationId)) {
                favoritesDao.remove(stationId)
            } else {
                favoritesDao.add(stationId)
            }
        }
    }

    // ────────────────────── Country + stations ──────────────────────

    /**
     * Стартовая страна — если уже играет станция (зашли через mini-player
     * с главной), берём её страну. Иначе дефолт Spain.
     *
     * Раньше: всегда стартовали со Spain, юзер слушал Аргентину,
     * тапал mini-player → возвращался почему-то в Spain.
     */
    private val initialCountry = player.currentStation.value?.country ?: Country.SPAIN

    private val _country = MutableStateFlow(initialCountry)
    val country: StateFlow<Country> = _country.asStateFlow()

    private val _stations = MutableStateFlow(StationRepository.getStationsForCountry(initialCountry))
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    /** Активная станция — берётся из Singleton-плеера, чтобы пережить смену экранов. */
    val currentStation: StateFlow<Station?> = player.currentStation

    val isPlaying: StateFlow<Boolean> = player.isPlaying
    val hasError: StateFlow<Boolean> = player.hasError

    /** Детальное состояние (IDLE/BUFFERING/PLAYING/PAUSED/ENDED/ERROR). */
    val playbackState: StateFlow<com.spanishapp.radio.player.RadioPlaybackState> = player.playbackState

    /** Что сейчас играет (ICY metadata). null если поток метаданные не отдаёт. */
    val nowPlaying: StateFlow<String?> = player.nowPlaying

    // ────────────────────── Filters ──────────────────────

    /**
     * Выбранные жанры (multi-select). Пустое множество = «все жанры».
     */
    private val _selectedGenres = MutableStateFlow<Set<Genre>>(emptySet())
    val selectedGenres: StateFlow<Set<Genre>> = _selectedGenres.asStateFlow()

    /** Тогл «только избранные». */
    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    /**
     * Persistent blocklist «мёртвых» станций. Хранится в DataStore через
     * RadioBlocklistPrefs, TTL 48ч. После TTL станция возвращается в
     * карусель — даём шанс восстановиться (URL мог временно флапать).
     *
     * Раньше (v1.11.1) был in-memory only → после рестарта приложения
     * мёртвые станции возвращались и auto-skip сжигал 7 сек на каждый.
     */
    val blockedStationIds: StateFlow<Set<String>> = blocklistPrefs.activeIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /**
     * Защита от бесконечного цикла auto-skip — если ВСЕ станции мертвы
     * (общий сетевой отвал), нельзя бесконечно переключаться. Лимит
     * 5 авто-скипов в минуту, потом плеер просто останавливается.
     */
    private val deadSkipCount = java.util.concurrent.atomic.AtomicInteger(0)

    companion object {
        /** Лимит авто-скипов мёртвых станций за минуту (защита от loop). */
        private const val MAX_DEAD_SKIPS_PER_WINDOW = 5

        /**
         * Timestamp релиза v1.11.2 (2026-05-17 00:00 UTC) — когда исправили
         * genre mapping. Каталоги с lastFetchedAt < этой даты считаются
         * old-schema и форсируются на re-discovery. После refresh
         * lastFetched будет now() → проверка больше не срабатывает.
         */
        private const val SCHEMA_V_1_11_2_RELEASE_MS = 1778976000000L
    }

    fun toggleGenreFilter(genre: Genre) {
        val current = _selectedGenres.value
        _selectedGenres.value = if (genre in current) current - genre else current + genre
    }

    fun toggleFavoritesFilter() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun clearFilters() {
        _selectedGenres.value = emptySet()
        _showOnlyFavorites.value = false
    }

    /**
     * Отфильтрованный список станций под текущие чипы.
     * Если ничего не подходит — отдаём пустой список (UI покажет hint).
     */
    /**
     * Чистое combine без side-effect. Раньше тут был
     * `player.setStationContext(...)` — anti-pattern, side-effect в data
     * transformation срабатывал на каждое изменение любого из 5 источников
     * даже если результат тот же. Вынесен в отдельный launch ниже + distinctUntilChanged.
     */
    val displayedStations: StateFlow<List<Station>> = combine(
        _stations, _selectedGenres, _showOnlyFavorites, favoriteIds, blockedStationIds,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val stations = values[0] as List<Station>
        @Suppress("UNCHECKED_CAST")
        val genres = values[1] as Set<Genre>
        val onlyFav = values[2] as Boolean
        @Suppress("UNCHECKED_CAST")
        val favs = values[3] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val blocked = values[4] as Set<String>

        stations.filter { st ->
            val genreOk = genres.isEmpty() || st.genre in genres
            val favOk = !onlyFav || st.id in favs
            val notBlocked = st.id !in blocked
            genreOk && favOk && notBlocked
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _stations.value)

    // ────────────────────── Init ──────────────────────

    init {
        // Трекер listening sessions — пишем в БД когда сессия заканчивается.
        // ApplicationScope (не viewModelScope) чтобы запись прошла даже после
        // уничтожения ViewModel при смене экрана.
        player.onSessionEnded = { startedAt, endedAt, stationId ->
            @Suppress("OPT_IN_USAGE")
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                listeningDao.insert(startedAt, endedAt, stationId)
            }
        }

        // Станция признана мёртвой (3 auto-reconnect не помогли) →
        // персистим в DataStore (TTL 48ч) + переключаемся на следующую рабочую.
        // Защита от бесконечного цикла: skipsThisSecond не более 5/сек.
        player.onStationDead = { dead ->
            viewModelScope.launch {
                blocklistPrefs.block(dead.id)
                // Подождём пока activeIds flow обновится, потом найдём кандидата
                val currentBlocked = blocklistPrefs.activeIds.first()
                val available = _stations.value.filter {
                    it.id != dead.id && it.id !in currentBlocked
                }
                val candidate = available.firstOrNull()
                if (candidate != null && deadSkipCount.incrementAndGet() <= MAX_DEAD_SKIPS_PER_WINDOW) {
                    player.play(candidate)
                }
                // Если все станции мертвы — просто остановимся, не зацикливаемся
            }
        }
        // Каждую минуту обнуляем счётчик авто-скипов
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000)
                deadSkipCount.set(0)
            }
        }

        viewModelScope.launch {
            val hasCache = catalogDao.count() > 0
            val isFresh = catalogRepo.isCacheFresh()

            // v1.11.2 migration: catalog с genre mapping per-tag (NEWS, SPORTS,
            // CULTURE правильно категоризированы). Проверяем lastFetchedAt против
            // даты релиза v1.11.2 — это устойчивее чем проверять «есть ли SPORTS»
            // (в каком-то регионе sports-тегированных станций может не быть вообще,
            // и тогда тот check срабатывал бы forever loop при каждом открытии).
            val lastFetched = catalogDao.lastFetchedAt() ?: 0L
            val needsSchemaRefresh = hasCache && lastFetched < SCHEMA_V_1_11_2_RELEASE_MS

            if (hasCache) reloadStations()

            // Первая станция — только если ничего не играет.
            if (player.currentStation.value == null) {
                _stations.value.firstOrNull()?.let { tuneToStation(it) }
            }

            // Авто-discovery если кэш пустой / устарел / старая схема
            if (!hasCache || !isFresh || needsSchemaRefresh) {
                _discoveryState.value = DiscoveryState.LOADING
                val count = catalogRepo.discoverAndCache()
                // Cache fallback — если discovery провалился, но кэш всё же
                // остался от предыдущего раза, продолжаем работать с ним
                _discoveryState.value = when {
                    count > 0 -> DiscoveryState.READY
                    hasCache -> DiscoveryState.READY
                    else -> DiscoveryState.ERROR
                }
                reloadStations()
            } else {
                _discoveryState.value = DiscoveryState.READY
            }
        }

        // Авто-skip убран — теперь обрабатывается через player.onStationDead
        // ПОСЛЕ того как auto-reconnect (v1.11.0) исчерпал 3 попытки.

        // Пробрасываем отфильтрованный список в плеер для notification skip
        // и mini-player navigation. distinctUntilChanged защищает от лишних
        // re-set'ов когда тот же список переходит туда-сюда через combine.
        viewModelScope.launch {
            displayedStations
                .distinctUntilChanged { old, new -> old.map { it.id } == new.map { it.id } }
                .collect { filtered ->
                    val context = filtered.ifEmpty {
                        _stations.value.filter { it.id !in blockedStationIds.value }
                    }
                    player.setStationContext(context)
                }
        }
    }

    fun selectCountry(country: Country) {
        if (_country.value == country) return
        _country.value = country
        viewModelScope.launch {
            val cached = catalogDao.getAll().filter { it.country == country.name }
            _stations.value = if (cached.isNotEmpty()) cached.map { it.toStation() }
                              else StationRepository.getStationsForCountry(country)
            _stations.value.firstOrNull()?.let { tuneToStation(it) }
        }
    }

    /** Делегируем контроллеру — контекст уже синхронизирован в displayedStations. */
    fun nextStation() = player.nextStation()
    fun previousStation() = player.previousStation()

    /** Вернуть mini-player на не-радио-экраны если юзер его скрыл свайпом. */
    fun resetMiniPlayerVisibility() = player.showMiniPlayer()

    private fun tuneToStation(station: Station) {
        player.play(station)
    }

    /** Тап по карточке станции — мгновенно переключаем (UI обновится через StateFlow). */
    fun tuneToStationDirect(station: Station) {
        tuneToStation(station)
    }

    fun togglePlayback() {
        if (player.isPlaying.value) player.pause() else player.resume()
    }

    // Не вызываем player.release() — Singleton живёт дольше ViewModel.

    /**
     * При уничтожении VM обнуляем callbacks на Singleton player.
     * Иначе lambda захватывает this@RadioViewModel → старый VM висит в памяти
     * пока player жив (а он Singleton, всегда). При rotation × N открытий →
     * N утечек VM. Видно через LeakCanary / Profiler heap dump.
     */
    override fun onCleared() {
        player.onSessionEnded = null
        player.onStationDead = null
        super.onCleared()
    }
}
