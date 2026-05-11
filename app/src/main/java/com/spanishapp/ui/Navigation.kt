package com.spanishapp.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.spanishapp.ui.flashcards.FlashcardDirection
import com.spanishapp.ui.flashcards.FlashcardsScreen
import com.spanishapp.ui.flashcards.FlashcardsSetupScreen
import com.spanishapp.ui.flashcards.PracticeScreen
import com.spanishapp.ui.chat.AiChatScreen
import com.spanishapp.ui.conjugation.ConjugationScreen
import com.spanishapp.ui.games.*
import com.spanishapp.ui.dictionary.DictionaryScreen
import com.spanishapp.ui.dictionary.WeakWordsScreen
import com.spanishapp.ui.home.HomeScreen
import com.spanishapp.ui.home.CourseDetailScreen
import com.spanishapp.ui.home.LessonContentScreen
import com.spanishapp.ui.home.LessonIntroScreen
import com.spanishapp.ui.home.LessonIntroViewModel
import com.spanishapp.ui.home.LessonSessionScreen
import com.spanishapp.ui.grammar.GrammarScreen
import com.spanishapp.ui.quiz.QuizScreen
import com.spanishapp.ui.profile.AchievementsScreen
import com.spanishapp.ui.profile.ProfileScreen
import com.spanishapp.ui.profile.RatingScreen
import com.spanishapp.ui.leaderboard.LeaderboardScreen
import com.spanishapp.ui.settings.SettingsScreen
import com.spanishapp.ui.pronunciation.PronunciationScreen
import com.spanishapp.ui.dialogues.DialoguesScreen
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
import com.spanishapp.ui.auth.AppLockScreen
import com.spanishapp.data.prefs.AppLockPreferences
import com.spanishapp.service.AppLockManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel-затычка для считывания AppLock состояния прямо в NavHost.
 * Нужно чтобы определить стартовый экран (home или app_lock).
 */
@HiltViewModel
class AppLockGateViewModel @Inject constructor(
    val appLockPreferences: AppLockPreferences,
    val appLockManager: AppLockManager
) : ViewModel() {
    val state: StateFlow<AppLockGateState> = combine(
        appLockPreferences.isEnabled,
        appLockManager.isUnlocked
    ) { enabled, unlocked ->
        AppLockGateState(enabled = enabled, unlocked = unlocked)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppLockGateState())
}

data class AppLockGateState(val enabled: Boolean = false, val unlocked: Boolean = false) {
    val shouldShowLock: Boolean get() = enabled && !unlocked
}

object Navigation {

    @Composable
    fun SpanishNavHost(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        authViewModel: AuthViewModel = hiltViewModel(),
        appLockGate: AppLockGateViewModel = hiltViewModel(),
        contentGate: com.spanishapp.ui.onboarding.ContentReadyGate = hiltViewModel(),
    ) {
        val authState by authViewModel.uiState.collectAsStateWithLifecycle()
        val lockState by appLockGate.state.collectAsState()
        val contentReady by contentGate.ready.collectAsState()

        // ── Onboarding-first gate ──
        // Auth + onboarding run BEFORE the content download. Only after the
        // user finishes onboarding do we force the Download Screen if packs
        // aren't ready. This lets new users see the brand/registration flow
        // first, exactly like a typical mobile game.
        val initialStartDest = remember(
            authState.isLoggedIn,
            authState.onboardingCompleted,
            lockState.shouldShowLock,
            contentReady,
        ) {
            when {
                contentReady == null -> null            // still reading DataStore
                authState.isLoggedIn == null -> null    // still loading auth
                authState.isLoggedIn == true -> {
                    if (authState.onboardingCompleted) {
                        // Onboarding done → if content not ready, gate to download;
                        // otherwise normal home/lock flow.
                        when {
                            contentReady == false -> "download"
                            lockState.shouldShowLock -> "app_lock"
                            else -> "home"
                        }
                    } else {
                        // Still in onboarding — let them finish first
                        when {
                            authState.userName == null -> "name_entry"
                            authState.userAge == null -> "age_selection"
                            authState.userReason == null -> "reason_selection"
                            authState.userLevel == null -> "level_selection"
                            contentReady == false -> "download"
                            else -> "home"
                        }
                    }
                }
                else -> "welcome"
            }
        }

        if (initialStartDest == null) return

        // Content-readiness gate. Only fires when contentReady actually
        // flips to false (e.g. cache cleared mid-session). The first-launch
        // case is handled by initialStartDest above. Keying the effect on
        // backstack changes was interfering with legitimate nav transitions
        // (Home → Profile → tap Home in bottom bar = no return).
        LaunchedEffect(contentReady) {
            if (contentReady == false) {
                val current = navController.currentDestination?.route
                if (current != null && current != "download") {
                    navController.navigate("download") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
        }

        // Bottom-bar destinations get the Material "Fade Through" pattern
        // (fade + slight scale) because they're peer-level — slide-horizontal
        // would imply a forward/back relationship which doesn't apply.
        // Everything else keeps the original Shared Axis X slide+fade.
        val bottomBarRoutes = setOf("home", "games", "flashcards", "dictionary", "profile")
        fun NavBackStackEntry.baseRoute(): String? =
            destination.route?.substringBefore('?')?.substringBefore('/')
        fun isPeerNav(from: NavBackStackEntry, to: NavBackStackEntry): Boolean =
            from.baseRoute() in bottomBarRoutes && to.baseRoute() in bottomBarRoutes

        NavHost(
            navController = navController,
            startDestination = initialStartDest,
            modifier = modifier,
            enterTransition = {
                if (isPeerNav(initialState, targetState)) {
                    // Material "Fade Through" — incoming half: 210ms after a 90ms gap
                    fadeIn(tween(durationMillis = 210, delayMillis = 90)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(durationMillis = 210, delayMillis = 90)
                    )
                } else {
                    slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300))
                }
            },
            exitTransition = {
                if (isPeerNav(initialState, targetState)) {
                    // Material "Fade Through" — outgoing half: quick 90ms fade+shrink
                    fadeOut(tween(durationMillis = 90)) +
                    scaleOut(targetScale = 0.92f, animationSpec = tween(durationMillis = 90))
                } else {
                    slideOutHorizontally(tween(300)) { -it / 6 } + fadeOut(tween(220))
                }
            },
            popEnterTransition = {
                if (isPeerNav(initialState, targetState)) {
                    fadeIn(tween(durationMillis = 210, delayMillis = 90)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(durationMillis = 210, delayMillis = 90)
                    )
                } else {
                    slideInHorizontally(tween(300)) { -it / 6 } + fadeIn(tween(300))
                }
            },
            popExitTransition = {
                if (isPeerNav(initialState, targetState)) {
                    fadeOut(tween(durationMillis = 90)) +
                    scaleOut(targetScale = 0.92f, animationSpec = tween(durationMillis = 90))
                } else {
                    slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(220))
                }
            }
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
            composable(
                "placement_result/{level}",
                arguments = listOf(navArgument("level") { type = NavType.StringType })
            ) { backStackEntry ->
                val level = backStackEntry.arguments?.getString("level") ?: "A1"
                PlacementResultScreen(navController, level)
            }
            composable("level_selection") { LevelSelectionScreen(navController) }

            // ── Биометрический замок ───────────────────────────
            composable("app_lock") { AppLockScreen(navController) }

            // ── Главная ───────────────────────────────────────
            composable("home") { HomeScreen(navController) }

            composable(
                "course_detail/{courseLevel}",
                arguments = listOf(navArgument("courseLevel") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseLevel = backStackEntry.arguments?.getString("courseLevel") ?: "A1"
                CourseDetailScreen(navController, courseLevel)
            }

            // ── Игры ─────────────────────────────────────────
            composable("games") { GamesScreen(navController) }
            composable("game_articles") { ArticlesGameScreen(navController) }
            composable("game_speed") { SpeedGameScreen(navController) }
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

            // ── Интерактивная сессия урока (теория + упражнения + победа) ──
            composable(
                "lesson_session/{unitId}/{lessonIndex}",
                arguments = listOf(
                    navArgument("unitId") { type = NavType.IntType },
                    navArgument("lessonIndex") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val unitId      = backStackEntry.arguments?.getInt("unitId") ?: 1
                val lessonIndex = backStackEntry.arguments?.getInt("lessonIndex") ?: 0
                val vm: LessonIntroViewModel = hiltViewModel()
                LessonSessionScreen(navController, unitId, lessonIndex, vm)
            }

            // ── Карточки ──────────────────────────────────────
            composable(
                "flashcards?type={type}&level={level}",
                arguments = listOf(
                    navArgument("type") { defaultValue = "all" },
                    navArgument("level") { defaultValue = "A1" }
                )
            ) { FlashcardsSetupScreen(navController) }

            composable("practice") { PracticeScreen(navController) }

            composable(
                "flashcards_session?level={level}&category={category}&direction={direction}&setId={setId}",
                arguments = listOf(
                    navArgument("level") { defaultValue = "A1" },
                    navArgument("category") { defaultValue = "all" },
                    navArgument("direction") { defaultValue = FlashcardDirection.ES_TO_RU.name },
                    navArgument("setId") { defaultValue = "" }
                )
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val level = args?.getString("level") ?: "A1"
                val category = args?.getString("category") ?: "all"
                val setId = args?.getString("setId").orEmpty()
                val direction = runCatching {
                    FlashcardDirection.valueOf(args?.getString("direction") ?: "ES_TO_RU")
                }.getOrDefault(FlashcardDirection.ES_TO_RU)
                FlashcardsScreen(
                    navController = navController,
                    level = level,
                    category = category,
                    direction = direction,
                    setId = setId.ifBlank { null }
                )
            }

            // ── Спряжения ─────────────────────────────────────
            composable(
                "conjugation?verb={verb}",
                arguments = listOf(navArgument("verb") { defaultValue = "" })
            ) { ConjugationScreen(navController) }

            composable("conjugation_quiz") { VerbTrainingScreen(navController) }

            // ── Диалоги ───────────────────────────────────────
            // Detail-экран не нужен: DialoguesScreen раскрывает реплики inline + TTS.
            composable("dialogues") { DialoguesScreen(navController) }

            // ── Грамматика ────────────────────────────────────
            // Detail-экран не нужен: GrammarScreen раскрывает урок inline.
            composable("grammar") { GrammarScreen(navController) }

            // ── ИИ-чат ───────────────────────────────────────
            // Theme picker (replaces direct entry into AI chat).
            composable("ai_chat_sessions") {
                com.spanishapp.ui.chat.ChatSessionsScreen(navController)
            }
            composable(
                "ai_chat?sessionId={sessionId}",
                arguments = listOf(navArgument("sessionId") { defaultValue = "default" })
            ) { AiChatScreen(navController) }
            // Backward-compat: legacy "ai_chat" without args still opens default session.
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
            composable("settings_voice") { com.spanishapp.ui.settings.SettingsVoiceScreen(navController) }
            composable("download") {
                com.spanishapp.ui.onboarding.DownloadScreen(
                    onFinished = {
                        // After successful download, route to the user's
                        // appropriate next screen instead of just popping back.
                        val nextRoute = when {
                            authState.isLoggedIn != true -> "welcome"
                            authState.onboardingCompleted -> "home"
                            authState.userName == null   -> "name_entry"
                            authState.userAge == null    -> "age_selection"
                            authState.userReason == null -> "reason_selection"
                            authState.userLevel == null  -> "level_selection"
                            else -> "home"
                        }
                        navController.navigate(nextRoute) {
                            popUpTo("download") { inclusive = true }
                        }
                    }
                )
            }

            // ── Рейтинг / Лиги / Лидерборд ───────────────────
            composable("rating_full")  { RatingScreen(navController) }
            composable("leaderboard")  { LeaderboardScreen(navController) }
            composable("weekly_league") { com.spanishapp.ui.leaderboard.WeeklyLeagueScreen(navController) }

            // ── Словарь ───────────────────────────────────────
            composable("dictionary")  { DictionaryScreen(navController) }
            composable("weak_words")  { WeakWordsScreen(navController) }
        }
    }
}
