package com.spanishapp

import android.app.Application
import com.spanishapp.data.db.DatabaseSeeder
import com.spanishapp.data.prefs.AppPreferences
import com.spanishapp.service.DailyReminderWorker
import com.spanishapp.service.RatingDecayWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SpanishApp : Application() {

    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var appPreferences: AppPreferences

    /** Радио-плеер инжектится для регистрации в RadioCoordinator (TTS↔Radio mutex). */
    @Inject lateinit var radioPlayerController: com.spanishapp.radio.player.RadioPlayerController

    /** v1.18.24: premium TTS — регистрируем в глобальном роутере чтобы
     *  TextToSpeech.speakSpanish() мог автоматически роутить в premium. */
    @Inject lateinit var remoteTtsService: com.spanishapp.service.RemoteTtsService

    /** v1.22.31: Mixkit UI sounds.
     *  v1.23.1 (audit Bug 5): теперь через dagger.Lazy чтобы Hilt НЕ
     *  создавал UiSoundPlayer eager на main thread в Application.onCreate.
     *  Прежнее eager-инжектирование вызывало вклад в ANR при холодном
     *  старте (SoundPool.Builder().build() — нативная инициализация
     *  AudioTrack/MediaPlayer пул потоков). Теперь .get() вызывается
     *  в фоне внутри appScope.launch ниже. */
    @Inject lateinit var uiSoundPlayerLazy: dagger.Lazy<com.spanishapp.service.UiSoundPlayer>

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Готов ли databaseSeeder.seedIfNeeded() — на первом старте seeding
     * 10К+ слов + 1300 глаголов + 100 рассказов занимает несколько секунд
     * на старых устройствах. MainActivity подписывается и держит splash
     * с прогрессом до true. На последующих запусках seedIfNeeded
     * отрабатывает мгновенно (всё уже в БД).
     */
    private val _seedReady = MutableStateFlow(false)
    val seedReady: StateFlow<Boolean> = _seedReady.asStateFlow()

    override fun onCreate() {
        super.onCreate()

        // Регистрируем радио-плеер в координаторе:
        // когда speakSpanish() вызывается из урока, радио ставится на паузу.
        runCatching {
            com.spanishapp.radio.player.RadioCoordinator.setPlayer(radioPlayerController)
        }.onFailure { e ->
            runCatching {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[SpanishApp] RadioCoordinator init failed", e))
            }
        }

        // v1.18.24: Регистрируем premium TTS в глобальном роутере. После этого
        // TextToSpeech.speakSpanish() во ВСЕХ экранах автоматически идёт
        // через Google Cloud TTS с выбранным TutorPersonality.
        com.spanishapp.service.AppTtsRouter.register(remoteTtsService)

        // ВСЕ background-инициализации обёрнуты в try/catch.
        // Цель: даже если что-то падает (миграция, seeder, worker scheduling),
        // приложение всё равно стартует и юзер видит UI. Стектрейс пишется
        // в Crashlytics для диагностики без полного crash.
        appScope.launch {
            runCatching {
                val enabled = appPreferences.remindersEnabled.first()
                if (enabled) {
                    val hour = appPreferences.reminderHour.first()
                    val minute = appPreferences.reminderMinute.first()
                    DailyReminderWorker.schedule(this@SpanishApp, hour, minute)
                }
            }.onFailure { e ->
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[SpanishApp] DailyReminderWorker scheduling failed", e))
            }
        }
        runCatching { RatingDecayWorker.schedule(this) }.onFailure { e ->
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException("[SpanishApp] RatingDecayWorker scheduling failed", e))
        }
        runCatching {
            com.spanishapp.radio.player.RadioCatalogRefreshWorker.schedule(this)
        }.onFailure { e ->
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException("[SpanishApp] RadioCatalogRefreshWorker scheduling failed", e))
        }
        appScope.launch {
            runCatching {
                databaseSeeder.seedIfNeeded()
            }.onFailure { e ->
                // КРИТИЧНО: даже если seeder упал, отпускаем splash чтобы юзер
                // не застрял на загрузке. Может быть partial seed но это лучше
                // чем зависший splash.
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[SpanishApp] seedIfNeeded FAILED", e))
            }
            _seedReady.value = true
        }

        // v1.17.5: синкаем SharedPreferences-кэш UI-языка из DataStore.
        // attachBaseContext() читает из кэша синхронно (без runBlocking).
        // Этот bootstrap гарантирует что после первого запуска кэш содержит
        // канонический выбор юзера для следующих cold start.
        appScope.launch {
            runCatching { appPreferences.bootstrapLanguageCache() }
                .onFailure { e ->
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                        .recordException(RuntimeException("[SpanishApp] bootstrapLanguageCache FAILED", e))
                }
        }

        // v1.23.1 (audit Bug 5): triggerим инициализацию UiSoundPlayer
        // в фоне — НЕ блокирует Application.onCreate на main thread.
        // SoundPool сам внутри грузится через Dispatchers.IO.
        appScope.launch {
            runCatching { uiSoundPlayerLazy.get() }
                .onFailure { e ->
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                        .recordException(RuntimeException("[SpanishApp] UiSoundPlayer init FAILED", e))
                }
        }
    }
}
