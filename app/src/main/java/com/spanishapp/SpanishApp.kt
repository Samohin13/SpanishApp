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

    /** v1.25.4: Google Play Billing — реальные PRO подписки. */
    @Inject lateinit var playBillingManager: com.spanishapp.service.PlayBillingManager

    /** v1.25.80: в debug сборках PRO включается автоматически на старте app —
     *  чтоб разработчик не тыкал toggle в Settings вручную при каждой
     *  переустановке. В release не выполняется (BuildConfig.DEBUG=false). */
    @Inject lateinit var subscriptionPrefs: com.spanishapp.data.prefs.SubscriptionPreferences

    /** v1.25.88: backfill displayName из auth_prefs → user_progress на старте.
     *  Все существующие тестеры были безымянные в leaderboard ("Estudiante")
     *  потому что имя писалось только в auth_prefs, а Leaderboard читает
     *  из user_progress.displayName. */
    @Inject lateinit var authRepository: com.spanishapp.data.repository.AuthRepository
    @Inject lateinit var userProgressDao: com.spanishapp.data.db.dao.UserProgressDao

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
        // v1.25.28: daily vocab aggregation (см. docs/VOCAB_TRACKING_PLAN.md)
        runCatching {
            com.spanishapp.service.VocabAggregatorWorker.schedule(this)
        }.onFailure { e ->
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException("[SpanishApp] VocabAggregatorWorker scheduling failed", e))
        }
        runCatching {
            com.spanishapp.radio.player.RadioCatalogRefreshWorker.schedule(this)
        }.onFailure { e ->
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException("[SpanishApp] RadioCatalogRefreshWorker scheduling failed", e))
        }
        // v1.25.4: подключаемся к Google Play Billing. Это инициирует
        // запрос детальей подписки + restore existing purchases →
        // isPro state из SubscriptionPreferences автоматически синхронизируется.
        // v1.25.76 SEC-1: restorePurchases() внутри start() сам триггерит
        // handlePurchase() для каждой активной подписки → каждая идёт через
        // SubscriptionVerifier на server-side validation. Дополнительный
        // periodic refresh не нужен — Play сам сообщает свежее состояние.
        runCatching { playBillingManager.start() }.onFailure { e ->
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException("[SpanishApp] PlayBilling start failed", e))
        }
        // v1.25.80: debug-сборка → PRO автоматически включён на старте.
        // Облегчает разработку: после переустановки app сразу доступен весь
        // PRO контент без ручного toggle в Settings → Premium. Не работает
        // в release — BuildConfig.DEBUG=false, ветка не выполняется.
        if (BuildConfig.DEBUG) {
            appScope.launch {
                runCatching {
                    subscriptionPrefs.setPro(true)
                    // v1.25.81: также проставляем verifiedAt чтобы SEC-1
                    // grace check сработал (snap.isPro && (verifiedAt==0
                    // или age<30d) → true). Дополнительная страховка.
                    subscriptionPrefs.setProVerified(true, System.currentTimeMillis())
                    android.util.Log.d("SpanishApp", "DEBUG auto-PRO activated")
                }.onFailure {
                    android.util.Log.e("SpanishApp", "DEBUG auto-PRO failed", it)
                }
            }
        }
        // v1.25.88: backfill displayName — fix для существующих тестеров.
        // Если в auth_prefs есть имя (из Onboarding), а user_progress.displayName
        // пустой — копируем имя в user_progress, чтобы leaderboard показывал
        // настоящее имя юзера а не "Estudiante".
        appScope.launch {
            runCatching {
                val authName = authRepository.userName.first().orEmpty()
                if (authName.isNotBlank()) {
                    val progress = userProgressDao.getProgressOnce()
                    // v1.25.90: seed-default "Estudiante" тоже считается «не задано»,
                    // иначе backfill v1.25.88 был мёртвым кодом для всех существующих
                    // тестеров. Targeted UPDATE (Daos.kt:458) — без lost-update race.
                    if (progress != null && (progress.displayName.isBlank() || progress.displayName == "Estudiante")) {
                        userProgressDao.updateDisplayName(authName)
                        android.util.Log.d("SpanishApp", "Backfilled displayName='$authName' into user_progress")
                    }
                }
            }.onFailure { e ->
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[SpanishApp] displayName backfill failed", e))
            }
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
