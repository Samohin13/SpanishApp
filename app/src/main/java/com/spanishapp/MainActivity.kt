package com.spanishapp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.spanishapp.util.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.spanishapp.ui.theme.SpanishAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var appLockManager: AppLockManager
    @Inject lateinit var appPreferences: AppPreferences

    /**
     * Применяем выбранный пользователем язык UI ДО создания UI.
     * При изменении в Settings вызываем activity.recreate() —
     * attachBaseContext перечитает свежее значение из DataStore.
     *
     * runBlocking здесь оправдан: это одноразовое чтение одного флага
     * на главной нити при старте Activity, и без него мы не знаем,
     * какой язык применять к ресурсам Compose.
     */
    override fun attachBaseContext(newBase: Context) {
        // На раннем этапе Hilt ещё не доступен — собираем DataStore
        // через AppPreferences с обычным ApplicationContext.
        val lang = runBlocking {
            runCatching {
                AppPreferences(newBase.applicationContext).uiLanguage.first()
            }.getOrDefault("system")
        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { splashHoldOpen }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
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
                        // Ждём завершения seedIfNeeded() — на первом запуске
                        // показываем splash-overlay с прогрессом. Splash
                        // системы тоже держим до тех пор.
                        val app = applicationContext as SpanishApp
                        val seedReady by app.seedReady.collectAsStateWithLifecycle()
                        LaunchedEffect(seedReady) {
                            if (seedReady) splashHoldOpen = false
                        }
                        if (seedReady) {
                            SpanishAppRoot()
                        } else {
                            FirstLaunchLoadingOverlay()
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
        }
    }
}

/**
 * Splash-overlay для первого запуска — показывается пока seedIfNeeded()
 * заполняет БД (10К слов + глаголы + рассказы, 2-5 сек на старых устройствах).
 *
 * Без overlay юзер видит белый экран, думает «зависло» и закрывает.
 */
@Composable
private fun FirstLaunchLoadingOverlay() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = androidx.compose.ui.Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
            )
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(20.dp))
            androidx.compose.material3.Text(
                "ESPEAK",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(8.dp))
            androidx.compose.material3.Text(
                "Готовим словарь...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SpanishAppRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val showBottomBar = currentRoute in listOf(
        "home", "games", "dictionary", "profile", "radio",
        "grammar", "achievements", "weak_words",
        "conjugation", "quiz", "dialogues", "settings", "pronunciation"
    ) || currentRoute.startsWith("flashcards")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                androidx.compose.foundation.layout.Column {
                    // Mini-player над BottomBar — показывается когда играет радио
                    // и юзер НЕ на экране радио (где уже есть полный плеер).
                    com.spanishapp.radio.ui.RadioMiniPlayer(
                        isOnRadioScreen = currentRoute == "radio",
                        onClick = {
                            // Открыть полный экран радио
                            navController.navigate("radio") {
                                launchSingleTop = true
                            }
                        }
                    )
                    SpanishBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        // Bottom-bar tap behaviour:
                        // 1. If the target route is already in the back stack,
                        //    pop to it (cheaper, preserves its state, and
                        //    avoids the previous launchSingleTop + restoreState
                        //    combo which silently no-op'd Profile/Settings →
                        //    Home).
                        // 2. Otherwise navigate normally — pop everything
                        //    above home so we don't grow the stack across
                        //    horizontal tab moves.
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
                )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Navigation.SpanishNavHost(
                navController = navController,
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
            )
            // Глобальный оверлей для разблокировки достижений
            com.spanishapp.ui.components.AchievementUnlockHost()
            // Глобальный "+N ⭐" попап при изменении рейтинга
            com.spanishapp.ui.components.RatingPopupHost()
        }
    }
}
