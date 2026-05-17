package com.spanishapp.radio.player

/**
 * Глобальный координатор между TTS-плеером (уроки) и радио-плеером.
 *
 * Когда юзер запускает упражнение с TTS, а радио играет в фоне — слышно
 * оба сразу. Это плохой UX. Решение: единая точка PauseForTts(), которую
 * вызывает speakSpanish() перед каждым TTS-вызовом.
 *
 * Радио ставится на паузу, юзер не теряет состояние плеера — может
 * возобновить из mini-player'а тапом ▶.
 *
 * Holder инициализируется в SpanishApp.onCreate() через AppEntryPoint.
 */
object RadioCoordinator {
    @Volatile
    private var player: RadioPlayerController? = null

    fun setPlayer(controller: RadioPlayerController) {
        player = controller
    }

    /** Вызывается из TextToSpeech.speakSpanish() — радио на паузу. */
    fun pauseForTts() {
        val p = player ?: return
        if (p.isPlaying.value) {
            p.pause()
        }
    }
}
