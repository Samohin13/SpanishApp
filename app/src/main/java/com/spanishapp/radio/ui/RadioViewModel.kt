package com.spanishapp.radio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.radio.data.Country
import com.spanishapp.radio.data.Station
import com.spanishapp.radio.data.StationRepository
import com.spanishapp.radio.player.RadioPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : ViewModel() {

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
        // Запускаем первую станцию ТОЛЬКО если ничего не играет.
        // Если юзер вернулся на экран радио — текущая станция уже задана
        // в Singleton-плеере, и второй раз её перезапускать не надо.
        if (player.currentStation.value == null) {
            playFirstStation()
        } else {
            // Восстанавливаем state из singleton-плеера
            _frequency.value = player.currentStation.value!!.frequency
            _signal.value = SignalStatus.ON_STATION
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
        _stations.value = StationRepository.getStationsForCountry(country)
        playFirstStation()
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
