package com.spanishapp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.spanishapp.ui.home.HomeScreen

object Navigation {

    @Composable
    fun SpanishNavHost(
        navController: NavHostController,
        modifier: Modifier = Modifier
    ) {
        NavHost(
            navController    = navController,
            startDestination = "home",
            modifier         = modifier,
            enterTransition  = { slideInHorizontally(tween(260)) { it/5 } + fadeIn(tween(260)) },
            exitTransition   = { slideOutHorizontally(tween(260)) { -it/5 } + fadeOut(tween(260)) },
            popEnterTransition  = { slideInHorizontally(tween(260)) { -it/5 } + fadeIn(tween(260)) },
            popExitTransition   = { slideOutHorizontally(tween(260)) { it/5 } + fadeOut(tween(260)) }
        ) {
            composable("home") { HomeScreen(navController) }

            composable("flashcards?type={type}&level={level}",
                arguments = listOf(
                    navArgument("type")  { defaultValue = "all" },
                    navArgument("level") { defaultValue = "A1" }
                )
            ) { Placeholder("Карточки") }

            composable("conjugation?verb={verb}",
                arguments = listOf(navArgument("verb") { defaultValue = "" })
            ) { Placeholder("Спряжения") }

            composable("conjugation_quiz") { Placeholder("Викторина спряжений") }
            composable("dialogues")        { Placeholder("Диалоги") }
            composable("dialogue/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { Placeholder("Диалог") }

            composable("grammar")          { Placeholder("Грамматика") }
            composable("grammar/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { Placeholder("Урок грамматики") }

            composable("ai_chat")          { Placeholder("ИИ-репетитор") }
            composable("pronunciation")    { Placeholder("Произношение") }
            composable("quiz?type={type}",
                arguments = listOf(navArgument("type") { defaultValue = "mixed" })
            ) { Placeholder("Тест") }

            composable("profile")          { Placeholder("Профиль") }
            composable("achievements")     { Placeholder("Достижения") }
            composable("settings")         { Placeholder("Настройки") }
            composable("dictionary")       { Placeholder("Словарь") }
            composable("weak_words")       { Placeholder("Слабые слова") }
        }
    }

    @Composable
    private fun Placeholder(name: String) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("🚧  $name", style = MaterialTheme.typography.titleMedium)
        }
    }
}