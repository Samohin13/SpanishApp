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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.spanishapp.ui.games.*
import com.spanishapp.ui.games.LibrosScreen
import com.spanishapp.ui.games.LibroReadScreen
import com.spanishapp.ui.dictionary.DictionaryScreen
import com.spanishapp.ui.dictionary.WeakWordsScreen
import com.spanishapp.ui.games.*
import com.spanishapp.ui.home.HomeScreen
import com.spanishapp.ui.home.LessonContentScreen
import com.spanishapp.ui.home.LessonIntroScreen
import com.spanishapp.ui.home.LessonIntroViewModel
import com.spanishapp.ui.grammar.GrammarScreen
import com.spanishapp.ui.quiz.QuizScreen
import com.spanishapp.ui.profile.AchievementsScreen
import com.spanishapp.ui.profile.ProfileScreen
import com.spanishapp.ui.settings.SettingsScreen
import com.spanishapp.ui.pronunciation.PronunciationScreen
import com.spanishapp.ui.dialogues.DialoguesScreen
import com.spanishapp.ui.onboarding.OnboardingScreen
import com.spanishapp.ui.auth.WelcomeScreen
import com.spanishapp.ui.auth.RegisterScreen
import com.spanishapp.ui.auth.LoginScreen
import com.spanishapp.ui.auth.ForgotPasswordScreen
import com.spanishapp.ui.auth.NameEntryScreen
import com.spanishapp.ui.auth.AgeSelectionScreen
import com.spanishapp.ui.auth.ReasonSelectionScreen
import com.spanishapp.ui.auth.KnowledgeCheckScreen
import com.spanishapp.ui.auth.LevelSelectionScreen
import com.spanishapp.ui.auth.PlacementTestScreen
import com.spanishapp.ui.auth.PlacementResultScreen
import com.spanishapp.ui.auth.AuthViewModel

object Navigation {

    @Composable
    fun SpanishNavHost(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        authViewModel: AuthViewModel = hiltViewModel()
    ) {
        val authState by authViewModel.uiState.collectAsStateWithLifecycle()
        
        // Используем remember, чтобы зафиксировать начальный экран только ПРИ ПЕРВОМ определении состояния
        // Это предотвратит "полеты" экранов при обновлении userName, age и т.д.
        val initialStartDest = remember(authState.isLoggedIn, authState.onboardingCompleted) {
            when {
                authState.isLoggedIn == null -> null // Еще грузимся
                authState.isLoggedIn == true -> {
                    if (authState.onboardingCompleted) {
                        "home"
                    } else {
                        // Если залогинен, но онбординг не закончен, проверяем где остановились
                        when {
                            authState.userName == null -> "name_entry"
                            authState.userAge == null -> "age_selection"
                            authState.userReason == null -> "reason_selection"
                            authState.userLevel == null -> "level_selection"
                            else -> "home"
                        }
                    }
                }
                else -> "welcome"
            }
        }

        if (initialStartDest == null) return

        NavHost(
            navController = navController,
            startDestination = initialStartDest,
            modifier = modifier,
            enterTransition = { slideInHorizontally(tween(260)) { it / 5 } + fadeIn(tween(260)) },
            exitTransition = { slideOutHorizontally(tween(260)) { -it / 5 } + fadeOut(tween(260)) },
            popEnterTransition = { slideInHorizontally(tween(260)) { -it / 5 } + fadeIn(tween(260)) },
            popExitTransition = { slideOutHorizontally(tween(260)) { it / 5 } + fadeOut(tween(260)) }
        ) {
            // ── Авторизация ──────────────────────────────────
            composable("welcome") { WelcomeScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("forgot_password") { ForgotPasswordScreen(navController) }
            
            // ── Онбординг ─────────────────────────────────────
            composable("name_entry") { NameEntryScreen(navController) }
            composable("age_selection") { AgeSelectionScreen(navController) }
            composable("reason_selection") { ReasonSelectionScreen(navController) }
            composable("knowledge_check") { KnowledgeCheckScreen(navController) }
            composable("placement_test") { PlacementTestScreen(navController) }
            composable("placement_result") { PlacementResultScreen(navController) }
            composable("level_selection") { LevelSelectionScreen(navController) }

            // ── Главная ───────────────────────────────────────
            composable("home") { HomeScreen(navController) }

            // ── Игры ─────────────────────────────────────────
            composable("games") { GamesScreen(navController) }
            composable("game_articles") { ArticlesGameScreen(navController) }
            composable("game_speed") { SpeedGameScreen(navController) }
            composable("game_anagrams") { AnagramsGameScreen(navController) }
            composable("game_math") { MathGameScreen(navController) }
            composable("game_crossword") { CrosswordGameScreen(navController) }
            composable("game_sopa") { SopaGameScreen(navController) }
            composable("game_palabra") { PalabraMaestraScreen(navController) }
            composable("game_libros") { LibrosScreen(navController) }
            composable(
                "libro/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 1
                val vm: LibrosViewModel = hiltViewModel()
                LibroReadScreen(navController, id, vm)
            }

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

            // ── Теоретический урок (алфавит, артикли, время...) ──
            composable(
                "lesson_content/{unitId}/{lessonIndex}",
                arguments = listOf(
                    navArgument("unitId") { type = NavType.IntType },
                    navArgument("lessonIndex") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val unitId      = backStackEntry.arguments?.getInt("unitId") ?: 1
                val lessonIndex = backStackEntry.arguments?.getInt("lessonIndex") ?: 0
                val vm: LessonIntroViewModel = hiltViewModel()
                LessonContentScreen(navController, unitId, lessonIndex, vm)
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

            composable("conjugation_quiz") { VerbTrainingScreen(navController) }

            // ── Диалоги ───────────────────────────────────────
            composable("dialogues") { DialoguesScreen(navController) }
            composable(
                "dialogue/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { Placeholder("Диалог") }

            // ── Грамматика ────────────────────────────────────
            composable("grammar") { GrammarScreen(navController) }
            composable(
                "grammar/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
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

            // ── Профиль / Достижения / Настройки ─────────────
            composable("profile")      { ProfileScreen(navController) }
            composable("achievements") { AchievementsScreen(navController) }
            composable("settings")     { SettingsScreen(navController) }
            composable("settings_voice") { Placeholder("Настройка голоса") }

            // ── Словарь ───────────────────────────────────────
            composable("dictionary")  { DictionaryScreen(navController) }
            composable("weak_words")  { WeakWordsScreen(navController) }
        }
    }

    @Composable
    private fun Placeholder(name: String) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("🚧  $name", style = MaterialTheme.typography.titleMedium)
        }
    }
}
