package com.spanishapp.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.spanishapp.ui.flashcards.FlashcardDirection
import com.spanishapp.ui.flashcards.FlashcardsScreen
import com.spanishapp.ui.flashcards.FlashcardsSetupScreen
import com.spanishapp.ui.chat.AiChatScreen
import com.spanishapp.ui.conjugation.ConjugationScreen
import com.spanishapp.ui.dictionary.DictionaryScreen
import com.spanishapp.ui.dictionary.WeakWordsScreen
import com.spanishapp.ui.home.HomeScreen
import com.spanishapp.ui.profile.AchievementsScreen
import com.spanishapp.ui.profile.ProfileScreen

object Navigation {

    @Composable
    fun SpanishNavHost(
        navController: NavHostController,
        modifier: Modifier = Modifier.Companion
    ) {
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = modifier,
            enterTransition = { slideInHorizontally(tween(260)) { it / 5 } + fadeIn(tween(260)) },
            exitTransition = { slideOutHorizontally(tween(260)) { -it / 5 } + fadeOut(tween(260)) },
            popEnterTransition = { slideInHorizontally(tween(260)) { -it / 5 } + fadeIn(tween(260)) },
            popExitTransition = { slideOutHorizontally(tween(260)) { it / 5 } + fadeOut(tween(260)) }
        ) {
            composable("home") { HomeScreen(navController) }

            composable(
                "flashcards?type={type}&level={level}",
                arguments = listOf(
                    navArgument("type") { defaultValue = "all" },
                    navArgument("level") { defaultValue = "A1" }
                )
            ) { FlashcardsSetupScreen(navController) }

            composable(
                "flashcards_session?level={level}&category={category}&direction={direction}&weak={weak}",
                arguments = listOf(
                    navArgument("level") { defaultValue = "A1" },
                    navArgument("category") { defaultValue = "all" },
                    navArgument("direction") { defaultValue = FlashcardDirection.ES_TO_RU.name },
                    navArgument("weak") { defaultValue = "false" }
                )
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val level = args?.getString("level") ?: "A1"
                val category = args?.getString("category") ?: "all"
                val direction = runCatching {
                    FlashcardDirection.valueOf(args?.getString("direction") ?: "ES_TO_RU")
                }.getOrDefault(FlashcardDirection.ES_TO_RU)
                val weak = (args?.getString("weak") ?: "false").toBoolean()
                FlashcardsScreen(
                    navController = navController,
                    level = level,
                    category = category,
                    direction = direction,
                    onlyWeak = weak
                )
            }

            composable(
                "conjugation?verb={verb}",
                arguments = listOf(navArgument("verb") { defaultValue = "" })
            ) { ConjugationScreen(navController) }

            composable("conjugation_quiz") { Placeholder("Викторина спряжений") }
            composable("dialogues") { Placeholder("Диалоги") }
            composable(
                "dialogue/{id}",
                arguments = listOf(navArgument("id") { type = NavType.Companion.IntType })
            ) { Placeholder("Диалог") }

            composable("grammar") { Placeholder("Грамматика") }
            composable(
                "grammar/{id}",
                arguments = listOf(navArgument("id") { type = NavType.Companion.IntType })
            ) { Placeholder("Урок грамматики") }

            composable("ai_chat") { AiChatScreen(navController) }
            composable("pronunciation") { Placeholder("Произношение") }
            composable(
                "quiz?type={type}",
                arguments = listOf(navArgument("type") { defaultValue = "mixed" })
            ) { Placeholder("Тест") }

            composable("profile") { ProfileScreen(navController) }
            composable("achievements") { AchievementsScreen(navController) }
            composable("settings") { Placeholder("Настройки") }
            composable("dictionary") { DictionaryScreen(navController) }
            composable("weak_words") { WeakWordsScreen(navController) }
        }
    }

    @Composable
    private fun Placeholder(name: String) {
        Box(Modifier.Companion.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
            Text("🚧  $name", style = MaterialTheme.typography.titleMedium)
        }
    }
}