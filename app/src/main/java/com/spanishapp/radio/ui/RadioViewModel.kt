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
            _discoveryState.value = if (count > 0) DiscoveryState.READY else DiscoveryState.ERROR
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

    private val _country = MutableStateFlow(Country.SPAIN)
    val country: StateFlow<Country> = _country.asStateFlow()

    private val _stations = MutableStateFlow(StationRepository.getStationsForCountry(Country.SPAIN))
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
    val displayedStations: StateFlow<List<Station>> = combine(
        _stations, _selectedGenres, _showOnlyFavorites, favoriteIds,
    ) { stations, genres, onlyFav, favs ->
        val filtered = stations.filter { st ->
            val genreOk = genres.isEmpty() || st.genre in genres
            val favOk = !onlyFav || st.id in favs
            genreOk && favOk
        }
        // Синхронизируем контекст с плеером — mini-player будет skip-ать
        // по тому же отфильтрованному списку что юзер видит
        player.setStationContext(filtered.ifEmpty { stations })
        filtered
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

        viewModelScope.launch {
            val hasCache = catalogDao.count() > 0
            val isFresh = catalogRepo.isCacheFresh()

            if (hasCache) reloadStations()

            // Первая станция — только если ничего не играет.
            if (player.currentStation.value == null) {
                _stations.value.firstOrNull()?.let { tuneToStation(it) }
            }

            // Авто-discovery если кэш пустой / устарел
            if (!hasCache || !isFresh) {
                _discoveryState.value = DiscoveryState.LOADING
                val count = catalogRepo.discoverAndCache()
                _discoveryState.value = if (count > 0) DiscoveryState.READY else DiscoveryState.ERROR
                reloadStations()
            } else {
                _discoveryState.value = DiscoveryState.READY
            }
        }

        // Авто-skip при ошибке потока
        viewModelScope.launch {
            player.hasError.collect { isError ->
                if (isError) {
                    kotlinx.coroutines.delay(2000)
                    if (player.hasError.value) nextStation()
                }
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
}
