package com.spanishapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.spanishapp.util.LocaleHelper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spanishapp.data.prefs.AppPreferences
import com.spanishapp.service.AppLockManager
import com.spanishapp.ui.Navigation
import com.spanishapp.ui.components.SpanishBackground
import com.spanishapp.ui.components.SpanishBottomBar
import com.spanishapp.ui.onboarding.OnboardingPrefs
import com.spanishapp.ui.onboarding.OnboardingScreen
import com.spanishapp.ui.theme.SpanishAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var appLockManager: AppLockManager
    @Inject lateinit var appPreferences: AppPreferences
    @Inject lateinit var onboardingPrefs: OnboardingPrefs
    @Inject lateinit var appUpdateChecker: com.spanishapp.service.AppUpdateChecker
    @Inject lateinit var syncRepository: com.spanishapp.data.repository.SyncRepository

    /**
     * Применяем выбранный пользователем язык UI ДО создания UI.
     * При изменении в Settings вызываем activity.recreate() —
     * attachBaseContext перечитает свежее значение из SharedPreferences cache.
     *
     * v1.17.5: больше НЕТ runBlocking на DataStore. Используем синхронный
     * SharedPreferences-кэш (instant read, memory-mapped). Это убирает
     * потенциальный ANR-источник на cold start (DataStore мог занимать
     * 200-500ms при холодной инициализации).
     *
     * Канонический источник — DataStore. Кэш обновляется автоматически
     * через setUiLanguage() и bootstrapLanguageCache() (см. AppPreferences).
     */
    override fun attachBaseContext(newBase: Context) {
        val lang = AppPreferences.cachedUiLanguage(newBase.applicationContext)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, lang))
    }

    /** Keeps the system Splash visible until early prefs finish loading AND
     *  database seeding is done. На первом запуске seed может занять 2-5 сек
     *  на старых устройствах (10К+ слов) — без splash юзер видит белый экран
     *  и может закрыть приложение. */
    private var splashHoldOpen = true

    /**
     * Android 13+ требует runtime-разрешение POST_NOTIFICATIONS — без него
     * ВСЕ наши push молча игнорируются (daily reminder, WoD-напоминание,
     * achievements). Регистрируем launcher до setContent чтобы не нарушать
     * lifecycle, запрашиваем один раз при первом старте.
     */
    private val notificationPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Результат не блокирует UX — даже если юзер отказал, приложение
        // работает. Просто пуши не приходят (юзер сам потом включит в Settings).
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return  // < Android 13 — ничего не нужно
        val perm = "android.permission.POST_NOTIFICATIONS"
        val granted = ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermLauncher.launch(perm)
        }
    }

    // v1.14.0: target от виджета (Dictionary / Radio).
    // Используем mutableStateOf чтобы SpanishAppRoot реагировал на
    // onNewIntent (юзер тапнул виджет когда app уже открыто).
    private val widgetTarget = androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(com.spanishapp.widget.WidgetIntents.EXTRA_NAV_TARGET)
            ?.let { widgetTarget.value = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { splashHoldOpen }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        widgetTarget.value = intent
            ?.getStringExtra(com.spanishapp.widget.WidgetIntents.EXTRA_NAV_TARGET)
        setContent {
            val fontSize by appPreferences.fontSize.collectAsStateWithLifecycle(
                initialValue = "MEDIUM"
            )
            // Theme is always system-controlled — no in-app toggle.
            val darkTheme = isSystemInDarkTheme()
            val fontScale = when (fontSize) {
                "SMALL" -> 0.9f
                "LARGE" -> 1.15f
                else -> 1.0f
            }
            val baseDensity = LocalDensity.current
            // v1.12.0 Phase 0: WindowSizeClass для tablet-first adaptive layouts.
            // Рассчитывается один раз на reconfigure, провайдится через CompositionLocal,
            // используется в com.spanishapp.ui.adaptive.* утилитах.
            @OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)
            val windowSizeClass = androidx.compose.material3.windowsizeclass
                .calculateWindowSizeClass(this@MainActivity)
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = baseDensity.density,
                    fontScale = baseDensity.fontScale * fontScale
                ),
                com.spanishapp.ui.adaptive.LocalWindowSizeClass provides windowSizeClass,
            ) {
                SpanishAppTheme(darkTheme = darkTheme) {
                    SpanishBackground {
                        // ── Splash animation gate ─────────────────────
                        // Каждый запуск показываем кинематографичный
                        // splash (~10 сек): падающие испанские буквы →
                        // мозаика ESPEAK → конверг 14 «специальных» →
                        // BAM → логотип-бык. См. EspeakSplashAnimation.
                        //
                        // Splash играет ВСЕГДА. Готовность БД (seed)
                        // ждём параллельно — обычно seed успевает до
                        // окончания анимации. Если seed дольше —
                        // финальный кадр (логотип) держится до seedReady.
                        val app = applicationContext as SpanishApp
                        val seedReady by app.seedReady.collectAsStateWithLifecycle()
                        // v1.25.98 FIX (audit nav-H3): rememberSaveable — раньше
                        // ротация/смена темы пересоздавала Activity и 5-секундный
                        // splash проигрывался ЗАНОВО поверх текущего экрана.
                        var splashAnimDone by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

                        // System splash скрываем как только Compose начал
                        // рисовать наш Splash (через первый recomposition).
                        LaunchedEffect(Unit) { splashHoldOpen = false }

                        val readyForHome = splashAnimDone && seedReady
                        if (!readyForHome) {
                            com.spanishapp.ui.splash.EspeakSplashAnimation(
                                onComplete = { splashAnimDone = true }
                            )
                        } else {
                            // v1.22.27: онбординг ОТКЛЮЧЁН по решению владельца.
                            // OnboardingScreen + OnboardingViewModel + OnboardingPrefs
                            // остаются в коде на случай если потребуется вернуть.
                            // Юзер после splash + seed сразу попадает на главный экран.
                            SpanishAppRoot(widgetTarget = widgetTarget)

                            // v1.22.30: плашка «доступно обновление» (минимальная).
                            // Запрос в Play Store делается в onResume() — здесь
                            // только показываем диалог если результат пришёл.
                            val updateState by appUpdateChecker.updateInfo
                                .collectAsStateWithLifecycle()
                            val available =
                                updateState as? com.spanishapp.service.AppUpdateChecker.UpdateState.Available
                            if (available != null) {
                                com.spanishapp.ui.components.UpdatePromptDialog(
                                    availableVersion = available.availableVersion,
                                    onLater = { appUpdateChecker.dismissForSession() },
                                    onUpdate = {
                                        appUpdateChecker.startFlexibleUpdate(this@MainActivity, available.info)
                                        appUpdateChecker.dismissForSession()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * При уходе в background — сбрасываем флаг разблокировки.
     * При следующем запуске Navigation сам отправит на app_lock,
     * если App Lock включён в настройках.
     */
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            appLockManager.lock()
            // v1.25.98 (lifecycle): страховочный upload прогресса при уходе в
            // фон. Основной синк — debounced 60s на каждом ответе, но юзер,
            // прошедший урок и сразу свернувший/убивший приложение, мог не
            // успеть в окно дебаунса. Запускаем на app-scope (переживает
            // destroy Activity), force=true обходит дебаунс.
            (applicationContext as? SpanishApp)?.appScope?.launch {
                runCatching { syncRepository.uploadAll(force = true) }
            }
        }
    }

    /**
     * v1.22.30: проверка Play Store на наличие обновления.
     * Триггерим в onResume, потому что юзер мог вернуться после
     * долгого отсутствия — версия в Play могла обновиться. Идемпотентно.
     * В debug-сборке возвращает NoUpdate без сетевого запроса.
     */
    override fun onResume() {
        super.onResume()
        runCatching { appUpdateChecker.checkForUpdate() }
    }
}

/**
 * Splash-overlay для первого запуска — показывается пока seedIfNeeded()
 * заполняет БД (10К слов + глаголы + рассказы, 2-5 сек на старых устройствах).
 *
 * Без overlay юзер видит белый экран, думает «зависло» и закрывает.
 */
/**
 * v1.13.2: переделан под dark splash theme (themes.xml).
 * Тёмный фон + крупная оранжевая иконка-кружок + бренд + индикатор.
 * Выглядит как продолжение system splash, а не как другой экран.
 */
@Composable
private fun FirstLaunchLoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F11)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Оранжевый круг с быком (бренд)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6B35)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = com.spanishapp.R.drawable.ic_bull),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            androidx.compose.material3.Text(
                "ESPEAK",
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                color = Color.White,
            )
            Spacer(Modifier.height(16.dp))
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = Color(0xFFFF6B35),
                strokeWidth = 3.dp,
            )
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.Text(
                "Готовим словарь...",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
fun SpanishAppRoot(
    widgetTarget: androidx.compose.runtime.MutableState<String?> =
        androidx.compose.runtime.mutableStateOf(null),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    // v1.14.0: реагируем на widget deep link (Dictionary / Radio).
    // Когда widgetTarget меняется — навигируем и сбрасываем (чтобы повторное
    // открытие виджета снова сработало).
    // v1.25.98 FIX (audit nav-H1): диплинки виджетов/пушей ОБХОДИЛИ био-замок —
    // навигация летела поверх app_lock сразу после composition. Теперь target
    // держится до разблокировки (эффект перезапустится при смене lock-state).
    val lockGate: com.spanishapp.ui.AppLockGateViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
    val lockState by lockGate.state.collectAsStateWithLifecycle()
    LaunchedEffect(widgetTarget.value, lockState.shouldShowLock) {
        val target = widgetTarget.value ?: return@LaunchedEffect
        if (lockState.shouldShowLock) return@LaunchedEffect  // ждём разблокировки
        if (target.isNotBlank() && target != currentRoute) {
            navController.navigate(target) {
                popUpTo("home") { inclusive = false; saveState = true }
                launchSingleTop = true
            }
        }
        widgetTarget.value = null
    }

    val showBottomBar = currentRoute in listOf(
        "home", "games", "dictionary", "profile", "radio",
        "grammar", "achievements", "weak_words",
        "conjugation", "quiz", "dialogues", "settings", "pronunciation"
    ) || currentRoute.startsWith("flashcards")

    // v1.12.0 Phase 1: Adaptive navigation — BottomBar на телефоне,
    // NavigationRail на планшете. Логика навигации идентична.
    val handleNavigate: (String) -> Unit = { route ->
        // Bottom-bar/rail tap behaviour:
        // 1. If the target route is already in the back stack,
        //    pop to it (cheaper, preserves its state, and
        //    avoids the previous launchSingleTop + restoreState
        //    combo which silently no-op'd Profile/Settings → Home).
        // 2. Otherwise navigate normally — pop everything above home
        //    so we don't grow the stack across horizontal tab moves.
        val popped = navController.popBackStack(
            route = route, inclusive = false
        )
        if (!popped) {
            navController.navigate(route) {
                popUpTo("home") {
                    saveState = true
                    inclusive = false
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    val miniPlayerOverlay: @Composable () -> Unit = {
        // Mini-player показывается когда играет радио и юзер НЕ на радио-экране.
        // На Compact — отрисовывается над BottomBar.
        // На Medium/Expanded — над NavigationRail в левой колонке.
        com.spanishapp.radio.ui.RadioMiniPlayer(
            isOnRadioScreen = currentRoute == "radio",
            onClick = {
                navController.navigate("radio") {
                    launchSingleTop = true
                }
            }
        )
    }

    com.spanishapp.ui.adaptive.AdaptiveScaffold(
        modifier = Modifier.fillMaxSize(),
        showNavigation = showBottomBar,
        topOverlay = miniPlayerOverlay,
        bottomBar = {
            SpanishBottomBar(currentRoute = currentRoute, onNavigate = handleNavigate)
        },
        navigationRail = {
            com.spanishapp.ui.components.SpanishNavigationRail(
                currentRoute = currentRoute,
                onNavigate = handleNavigate,
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // v1.18.54: padding + consume — дочерние экраны корректно
            // считают ime/safeDrawing inset'ы.
            Navigation.SpanishNavHost(
                navController = navController,
                modifier = Modifier
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .consumeWindowInsets(
                        androidx.compose.foundation.layout.PaddingValues(
                            bottom = paddingValues.calculateBottomPadding()
                        )
                    ),
            )
            // Глобальный оверлей для разблокировки достижений
            com.spanishapp.ui.components.AchievementUnlockHost()
            // v1.25.5: баннер "❄ Стрик сохранён" когда срабатывает freeze
            com.spanishapp.ui.components.StreakFreezePopupHost()
            // Глобальный "+N ⭐" попап при изменении рейтинга
            com.spanishapp.ui.components.RatingPopupHost()
        }
    }
}
