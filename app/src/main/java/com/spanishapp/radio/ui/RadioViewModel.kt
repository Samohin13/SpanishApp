package com.spanishapp.radio.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.radio.data.Country
import com.spanishapp.radio.data.Station
import com.spanishapp.radio.data.StationRepository
import com.spanishapp.radio.player.RadioPlayerController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Состояние сигнала в зависимости от близости частоты к ближайшей станции.
 */
enum class SignalStatus { ON_STATION, WEAK, NO_SIGNAL }

class RadioViewModel(app: Application) : AndroidViewModel(app) {

    private val player = RadioPlayerController(app)

    // ────────────────────────── State ──────────────────────────

    private val _country = MutableStateFlow(Country.SPAIN)
    val country: StateFlow<Country> = _country.asStateFlow()

    private val _stations = MutableStateFlow(StationRepository.getStationsForCountry(Country.SPAIN))
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    /** Текущая частота, на которой стрелка (плавная, между станциями). */
    private val _frequency = MutableStateFlow(_stations.value.first().frequency)
    val frequency: StateFlow<Float> = _frequency.asStateFlow()

    /** Найдена ли активная станция на текущей частоте. */
    private val _currentStation = MutableStateFlow<Station?>(_stations.value.first())
    val currentStation: StateFlow<Station?> = _currentStation.asStateFlow()

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
        // Первая станция при запуске
        playFirstStation()
    }

    private fun playFirstStation() {
        val first = _stations.value.first()
        _frequency.value = first.frequency
        _currentStation.value = first
        _signal.value = SignalStatus.ON_STATION
        player.play(first)
    }

    fun selectCountry(country: Country) {
        if (_country.value == country) return
        _country.value = country
        _stations.value = StationRepository.getStationsForCountry(country)
        playFirstStation()
    }

    /**
     * Перейти к следующей/предыдущей станции в текущей стране.
     */
    fun nextStation() {
        val list = _stations.value
        val current = _currentStation.value ?: return
        val idx = list.indexOf(current)
        val next = list.getOrNull(idx + 1) ?: list.first()
        tuneToStation(next)
    }

    fun previousStation() {
        val list = _stations.value
        val current = _currentStation.value ?: return
        val idx = list.indexOf(current)
        val prev = list.getOrNull(idx - 1) ?: list.last()
        tuneToStation(prev)
    }

    private fun tuneToStation(station: Station) {
        snapJob?.cancel()
        _frequency.value = station.frequency
        _currentStation.value = station
        _signal.value = SignalStatus.ON_STATION
        player.play(station)
    }

    /**
     * Юзер крутит колесо. Изменяем частоту, обновляем сигнал.
     * delta — положительное вправо (выше частота), отрицательное влево.
     * При остановке прокрутки → авто-snap к ближайшей станции (вызывается в onScrollStop).
     */
    fun onScrollFrequency(delta: Float) {
        snapJob?.cancel()
        val newFreq = (_frequency.value + delta * freqStep)
            .coerceIn(fmMin, fmMax)
        _frequency.value = newFreq

        // Определяем какая станция ближайшая и какой сигнал
        val nearest = StationRepository.nearestStation(newFreq, _country.value)
        val dist = nearest?.let { abs(it.frequency - newFreq) } ?: Float.MAX_VALUE
        val newSignal = when {
            dist <= onStationTolerance -> SignalStatus.ON_STATION
            dist <= weakTolerance -> SignalStatus.WEAK
            else -> SignalStatus.NO_SIGNAL
        }

        _signal.value = newSignal
        // Меняем играющую станцию ТОЛЬКО когда сигнал стал ON_STATION
        // (иначе постоянное переключение потока создало бы заикания)
        if (newSignal == SignalStatus.ON_STATION && nearest != null && nearest != _currentStation.value) {
            _currentStation.value = nearest
            player.play(nearest)
        } else if (newSignal != SignalStatus.ON_STATION) {
            player.pause()
        }
    }

    /**
     * Юзер отпустил колесо. Авто-snap к ближайшей станции с короткой задержкой.
     */
    fun onScrollStop() {
        snapJob?.cancel()
        snapJob = viewModelScope.launch {
            delay(400) // даём 0.4с показать «между станциями», потом snap
            val nearest = StationRepository.nearestStation(_frequency.value, _country.value)
                ?: return@launch
            tuneToStation(nearest)
        }
    }

    fun togglePlayback() {
        if (player.isPlaying.value) player.pause() else player.resume()
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
