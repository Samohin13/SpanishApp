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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.spanishapp.ui.flashcards.FlashcardDirection
import com.spanishapp.ui.flashcards.FlashcardsScreen
import com.spanishapp.ui.flashcards.FlashcardsSetupScreen
import com.spanishapp.ui.games.*
import com.spanishapp.ui.chat.AiChatScreen
import com.spanishapp.ui.conjugation.ConjugationScreen
import com.spanishapp.ui.conjugation.ConjugationQuizScreen
import com.spanishapp.ui.dictionary.DictionaryScreen
import com.spanishapp.ui.dictionary.WeakWordsScreen
import com.spanishapp.ui.home.HomeScreen
import com.spanishapp.ui.home.LessonIntroScreen
import com.spanishapp.ui.home.LessonIntroViewModel
import com.spanishapp.ui.grammar.GrammarScreen
import com.spanishapp.ui.quiz.QuizScreen
import com.spanishapp.ui.profile.AchievementsScreen
import com.spanishapp.ui.profile.ProfileScreen
import com.spanishapp.ui.settings.SettingsScreen
import com.spanishapp.ui.games.GamesScreen
import com.spanishapp.ui.games.ArticlesGameScreen
import com.spanishapp.ui.games.SpeedGameScreen
import com.spanishapp.ui.games.AnagramGameScreen
import com.spanishapp.ui.games.VerbFormGameScreen
import com.spanishapp.ui.games.ListeningGameScreen
import com.spanishapp.ui.pronunciation.PronunciationScreen
import com.spanishapp.ui.dialogues.DialoguesScreen
import com.spanishapp.ui.onboarding.OnboardingScreen

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
            // ── Главная ───────────────────────────────────────
            composable("home") { HomeScreen(navController) }

            composable(
                "lesson_intro/{unitId}/{lessonIndex}",
                arguments = listOf(
                    navArgument("unitId") { type = NavType.IntType },
                    navArgument("lessonIndex") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val unitId      = backStackEntry.arguments?.getInt("unitId") ?: 1
                val lessonIndex = backStackEntry.arguments?.getInt("lessonIndex") ?: 0
                val vm: LessonIntroViewModel = hiltViewModel()
                LessonIntroScreen(navController, unitId, lessonIndex, vm)
            }

            // ── Онбординг ─────────────────────────────────────
            composable("onboarding") { OnboardingScreen(navController) }

            // ── Карточки ──────────────────────────────────────
            composable(
                "flashcards?type={type}&level={level}",
                arguments = listOf(
                    navArgument("type") { defaultValue = "all" },
                    navArgument("level") { defaultValue = "A1" }
                )
            ) { FlashcardsSetupScreen(navController) }

            composable(
                "flashcards_session?level={level}&category={category}&direction={direction}",
                arguments = listOf(
                    navArgument("level") { defaultValue = "A1" },
                    navArgument("category") { defaultValue = "all" },
                    navArgument("direction") { defaultValue = FlashcardDirection.ES_TO_RU.name }
                )
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val level = args?.getString("level") ?: "A1"
                val category = args?.getString("category") ?: "all"
                val direction = runCatching {
                    FlashcardDirection.valueOf(args?.getString("direction") ?: "ES_TO_RU")
                }.getOrDefault(FlashcardDirection.ES_TO_RU)
                FlashcardsScreen(
                    navController = navController,
                    level = level,
                    category = category,
                    direction = direction
                )
            }

            // ── Спряжения ─────────────────────────────────────
            composable(
                "conjugation?verb={verb}",
                arguments = listOf(navArgument("verb") { defaultValue = "" })
            ) { ConjugationScreen(navController) }

            composable("conjugation_quiz") { ConjugationQuizScreen(navController) }

            // ── Диалоги ───────────────────────────────────────
            composable("dialogues") { DialoguesScreen(navController) }
            composable(
                "dialogue/{id}",
                arguments = listOf(navArgument("id") { type = NavType.Companion.IntType })
            ) { Placeholder("Диалог") }

            // ── Грамматика ────────────────────────────────────
            composable("grammar") { GrammarScreen(navController) }
            composable(
                "grammar/{id}",
                arguments = listOf(navArgument("id") { type = NavType.Companion.IntType })
            ) { Placeholder("Урок грамматики") }

            // ── ИИ-чат ───────────────────────────────────────
            composable("ai_chat") { AiChatScreen(navController) }

            // ── Произношение ──────────────────────────────────
            composable("pronunciation") { PronunciationScreen(navController) }

            // ── Тест ─────────────────────────────────────────
            composable(
                "quiz?type={type}",
                arguments = listOf(navArgument("type") { defaultValue = "mixed" })
            ) { QuizScreen(navController) }

            // ── Игры ──────────────────────────────────────────
            composable("games")         { GamesScreen(navController) }
            composable("game_articles") { ArticlesMapScreen(navController) }
            composable(
                "game_articles_session/{levelId}",
                arguments = listOf(navArgument("levelId") { type = NavType.IntType })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
                ArticlesGameScreen(navController, levelId)
            }
            composable("game_speed")      { SpeedGameScreen(navController) }
            composable("game_anagram")    { AnagramGameScreen(navController) }
            composable("game_verb_form")    { VerbFormGameScreen(navController) }
            composable("game_listening")    { ListeningGameScreen(navController) }

            // ── Профиль / Достижения / Настройки ─────────────
            composable("profile")      { ProfileScreen(navController) }
            composable("achievements") { AchievementsScreen(navController) }
            composable("settings")     { SettingsScreen(navController) }

            // ── Словарь ───────────────────────────────────────
            composable("dictionary")  { DictionaryScreen(navController) }
            composable("weak_words")  { WeakWordsScreen(navController) }
        }
    }

    @Composable
    private fun Placeholder(name: String) {
        Box(Modifier.Companion.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
            Text("🚧  $name", style = MaterialTheme.typography.titleMedium)
        }
    }
}
