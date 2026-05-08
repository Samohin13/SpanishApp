package com.spanishapp

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.spanishapp.util.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.spanishapp.data.prefs.ThemeMode
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

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by appPreferences.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.AUTO
            )
            val fontSize by appPreferences.fontSize.collectAsStateWithLifecycle(
                initialValue = "MEDIUM"
            )
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.AUTO  -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK  -> true
            }
            val fontScale = when (fontSize) {
                "SMALL" -> 0.9f
                "LARGE" -> 1.15f
                else -> 1.0f
            }
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = baseDensity.density,
                    fontScale = baseDensity.fontScale * fontScale
                )
            ) {
                SpanishAppTheme(darkTheme = darkTheme) {
                    SpanishBackground {
                        SpanishAppRoot()
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

@Composable
fun SpanishAppRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val showBottomBar = currentRoute in listOf(
        "home", "games", "dictionary", "profile",
        "grammar", "achievements", "weak_words",
        "conjugation", "quiz", "dialogues", "settings", "pronunciation"
    ) || currentRoute.startsWith("flashcards")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                SpanishBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Navigation.SpanishNavHost(
            navController = navController,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        )
    }
}
