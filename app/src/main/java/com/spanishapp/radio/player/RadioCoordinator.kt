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

    /**
     * v1.23.7: Вызывается при заходе в AI Chat. Радио буферизация
     * (особенно у нестабильных live-станций с частым FLUSHING/RESUMING)
     * блокирует main thread спамом MediaCodec событий + Notification
     * rebuilds → ANR в AI Chat. Юзер потом сам ▶ возобновляет если хочет.
     */
    fun pauseForChat() {
        val p = player ?: return
        if (p.isPlaying.value) {
            p.pause()
        }
    }
}
