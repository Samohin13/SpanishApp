package com.spanishapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spanishapp.ui.Navigation
import com.spanishapp.ui.components.SpanishBackground
import com.spanishapp.ui.components.SpanishBottomBar
import com.spanishapp.ui.theme.SpanishAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpanishAppTheme {
                SpanishBackground {
                    SpanishAppRoot()
                }
            }
        }
    }
}

@Composable
fun SpanishAppRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val showBottomBar = currentRoute in listOf(
        "home", "flashcards", "games", "dictionary", "profile",
        "grammar", "achievements", "weak_words",
        "conjugation", "quiz", "dialogues", "settings", "pronunciation"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Сделаем Scaffold прозрачным для анимации фона
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
            modifier = Modifier.padding(paddingValues)
        )
    }
}
