package com.spanishapp.radio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.radio.data.Country
import com.spanishapp.radio.data.Station
import com.spanishapp.radio.data.StationRepository
import com.spanishapp.radio.data.toStation
import com.spanishapp.radio.player.RadioPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

// Hilt + ViewModel + StateFlow imports выше

/**
 * Состояние сигнала в зависимости от близости частоты к ближайшей станции.
 */
enum class SignalStatus { ON_STATION, WEAK, NO_SIGNAL }

@HiltViewModel
class RadioViewModel @Inject constructor(
    private val player: RadioPlayerController,
    private val favoritesDao: com.spanishapp.radio.data.RadioFavoriteDao,
    private val catalogDao: com.spanishapp.radio.data.RadioCatalogDao,
    private val catalogRepo: com.spanishapp.radio.data.RadioCatalogRepository,
) : ViewModel() {

    /** Состояние подбора станций под страну. */
    enum class DiscoveryState { IDLE, LOADING, READY, ERROR }

    private val _discoveryState = MutableStateFlow(DiscoveryState.IDLE)
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState.asStateFlow()

    val discoveryProgress: StateFlow<Float> = catalogRepo.progress
    val discoveryFoundCount: StateFlow<Int> = catalogRepo.foundCount

    /** Запустить ручное обновление каталога (тап по кнопке 🔄). */
    fun refreshCatalog() {
        viewModelScope.launch {
            _discoveryState.value = DiscoveryState.LOADING
            val count = catalogRepo.discoverAndCache()
            _discoveryState.value = if (count > 0) DiscoveryState.READY else DiscoveryState.ERROR
            // Перечитываем станции из каталога
            reloadStations()
        }
    }

    private suspend fun reloadStations() {
        val cached = catalogDao.getAll()
        if (cached.isNotEmpty()) {
            _stations.value = cached
                .filter { it.country == _country.value.name }
                .map { it.toStation() }
            // Если текущая станция не в новом списке — играем первую
            val current = currentStation.value
            if (current == null || _stations.value.none { it.id == current.id }) {
                playFirstStation()
            }
        }
    }

    /** Множество id избранных станций — для UI ⭐ кнопки. */
    val favoriteIds: StateFlow<Set<String>> = favoritesDao.observeAllIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch {
            if (favoriteIds.value.contains(stationId)) {
                favoritesDao.remove(stationId)
            } else {
                favoritesDao.add(stationId)
            }
        }
    }

    // ────────────────────────── State ──────────────────────────

    private val _country = MutableStateFlow(Country.SPAIN)
    val country: StateFlow<Country> = _country.asStateFlow()

    private val _stations = MutableStateFlow(StationRepository.getStationsForCountry(Country.SPAIN))
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    /** Текущая частота, на которой стрелка (плавная, между станциями). */
    private val _frequency = MutableStateFlow(_stations.value.first().frequency)
    val frequency: StateFlow<Float> = _frequency.asStateFlow()

    /** Активная станция — берётся из Singleton-плеера, чтобы пережить смену экранов. */
    val currentStation: StateFlow<Station?> = player.currentStation

    private val _signal = MutableStateFlow(SignalStatus.ON_STATION)
    val signal: StateFlow<SignalStatus> = _signal.asStateFlow()

    val isPlaying: StateFlow<Boolean> = player.isPlaying
    val hasError: StateFlow<Boolean> = player.hasError

    // ────────────────────────── Tuning ──────────────────────────

    /** Минимум FM-диапазона (МГц). */
    val fmMin = 87.5f

    /** Максимум FM-диапазона (МГц). */
    val fmMax = 108.0f

    /** На сколько МГц меняется частота за один тик прокрутки. */
    private val freqStep = 0.1f

    /** Толерантность: ON_STATION если разница ≤ 0.05, WEAK если ≤ 0.2, иначе NO_SIGNAL. */
    private val onStationTolerance = 0.05f
    private val weakTolerance = 0.2f

    private var snapJob: Job? = null

    init {
        // 1. Проверяем кэш каталога. Если есть — используем. Если нет / устарел — запускаем discovery.
        viewModelScope.launch {
            val hasCache = catalogDao.count() > 0
            val isFresh = catalogRepo.isCacheFresh()

            if (hasCache) {
                reloadStations()
            }

            // Запускаем первую станцию ТОЛЬКО если ничего не играет.
            if (player.currentStation.value == null) {
                playFirstStation()
            } else {
                _frequency.value = player.currentStation.value!!.frequency
                _signal.value = SignalStatus.ON_STATION
            }

            // Авто-discovery если кэша нет вообще ИЛИ старый
            if (!hasCache || !isFresh) {
                _discoveryState.value = DiscoveryState.LOADING
                val count = catalogRepo.discoverAndCache()
                _discoveryState.value = if (count > 0) DiscoveryState.READY else DiscoveryState.ERROR
                reloadStations()
            } else {
                _discoveryState.value = DiscoveryState.READY
            }
        }

        // Авто-skip при ошибке потока.
        viewModelScope.launch {
            player.hasError.collect { isError ->
                if (isError) {
                    delay(2000)
                    if (player.hasError.value) {
                        nextStation()
                    }
                }
            }
        }
    }

    private fun playFirstStation() {
        val first = _stations.value.first()
        _frequency.value = first.frequency
        _signal.value = SignalStatus.ON_STATION
        player.play(first)
    }

    fun selectCountry(country: Country) {
        if (_country.value == country) return
        _country.value = country
        // Если есть кэшированный каталог — используем его, иначе хардкод
        viewModelScope.launch {
            val cached = catalogDao.getAll().filter { it.country == country.name }
            _stations.value = if (cached.isNotEmpty()) cached.map { it.toStation() }
                              else StationRepository.getStationsForCountry(country)
            playFirstStation()
        }
    }

    /**
     * Перейти к следующей/предыдущей станции в текущей стране.
     */
    fun nextStation() {
        val list = _stations.value
        val current = currentStation.value ?: return
        val idx = list.indexOf(current)
        val next = list.getOrNull(idx + 1) ?: list.first()
        tuneToStation(next)
    }

    fun previousStation() {
        val list = _stations.value
        val current = currentStation.value ?: return
        val idx = list.indexOf(current)
        val prev = list.getOrNull(idx - 1) ?: list.last()
        tuneToStation(prev)
    }

    private fun tuneToStation(station: Station) {
        snapJob?.cancel()
        _frequency.value = station.frequency
        _signal.value = SignalStatus.ON_STATION
        player.play(station)
    }

    /** Public: переключиться на конкретную станцию (тап по карусели). */
    fun tuneToStationDirect(station: Station) {
        tuneToStation(station)
    }

    /**
     * Юзер крутит колесо. Изменяем ТОЛЬКО частоту и индикатор сигнала.
     * НЕ переключаем играющую станцию пока юзер крутит — это создавало
     * заикания и хаотичный звук при прокрутке через несколько станций
     * подряд. Поток меняем ТОЛЬКО на onScrollStop().
     *
     * Во время скролла плеер ставится на pause (визуально юзер видит
     * частоту/сигнал — это и есть «сканирование без аудио»).
     */
    fun onScrollFrequency(delta: Float) {
        snapJob?.cancel()
        val newFreq = (_frequency.value + delta * freqStep)
            .coerceIn(fmMin, fmMax)
        _frequency.value = newFreq

        // Обновляем индикатор сигнала (для UI)
        val nearest = StationRepository.nearestStation(newFreq, _country.value)
        val dist = nearest?.let { abs(it.frequency - newFreq) } ?: Float.MAX_VALUE
        _signal.value = when {
            dist <= onStationTolerance -> SignalStatus.ON_STATION
            dist <= weakTolerance -> SignalStatus.WEAK
            else -> SignalStatus.NO_SIGNAL
        }

        // Пауза играющей станции пока юзер крутит
        if (player.isPlaying.value) player.pause()
    }

    /**
     * Юзер отпустил колесо. Сразу snap к ближайшей станции (без задержки).
     */
    fun onScrollStop() {
        snapJob?.cancel()
        snapJob = viewModelScope.launch {
            // Минимальная задержка — даём анимации стрелки доехать
            delay(100)
            val nearest = StationRepository.nearestStation(_frequency.value, _country.value)
                ?: return@launch
            tuneToStation(nearest)
        }
    }

    fun togglePlayback() {
        if (player.isPlaying.value) player.pause() else player.resume()
    }

    // Не вызываем player.release() — он Singleton и должен жить дольше ViewModel,
    // чтобы радио продолжало играть когда юзер ушёл с экрана.
}
