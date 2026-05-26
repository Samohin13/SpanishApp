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
import com.spanishapp.ui.auth.FeatureTourScreen
import com.spanishapp.ui.auth.DailyGoalSelectionScreen
import com.spanishapp.data.prefs.AppLockPreferences
import com.spanishapp.service.AppLockManager
import androidx.compose.runtime.LaunchedEffect

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
    ) {
        val authState by authViewModel.uiState.collectAsStateWithLifecycle()
        val lockState by appLockGate.state.collectAsStateWithLifecycle()

        // ── Start destination ──
        // OTA content gate is DISABLED for v1 release — built-in content from
        // DatabaseSeeder + ModernVocab + LessonContentData + LibrosData covers
        // every screen. The OTA pipeline (ContentDownloader, ContentImporter,
        // ContentVersionStore, DownloadScreen) is still in the repo and works
        // standalone, but it is NOT wired into the startup path so a Firebase
        // Storage misconfiguration can never block app launch again.
        // Re-enable after release once the upload pipeline is properly tested.
        val initialStartDest = remember(
            authState.isLoggedIn,
            authState.onboardingCompleted,
            lockState.shouldShowLock,
        ) {
            when {
                authState.isLoggedIn == null -> null
                authState.isLoggedIn == true -> {
                    if (authState.onboardingCompleted) {
                        when {
                            lockState.shouldShowLock -> "app_lock"
                            else -> "home"
                        }
                    } else {
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
                    // Material "Fade Through" — incoming half: 200ms after a 70ms gap
                    fadeIn(tween(durationMillis = 200, delayMillis = 70)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(durationMillis = 200, delayMillis = 70)
                    )
                } else {
                    // Deep navigation: 220ms — snappy but not jarring
                    slideInHorizontally(tween(220)) { it / 3 } + fadeIn(tween(220))
                }
            },
            exitTransition = {
                if (isPeerNav(initialState, targetState)) {
                    // Material "Fade Through" — outgoing half
                    fadeOut(tween(durationMillis = 70)) +
                    scaleOut(targetScale = 0.92f, animationSpec = tween(durationMillis = 70))
                } else {
                    slideOutHorizontally(tween(220)) { -it / 6 } + fadeOut(tween(180))
                }
            },
            popEnterTransition = {
                if (isPeerNav(initialState, targetState)) {
                    fadeIn(tween(durationMillis = 200, delayMillis = 70)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(durationMillis = 200, delayMillis = 70)
                    )
                } else {
                    slideInHorizontally(tween(220)) { -it / 6 } + fadeIn(tween(220))
                }
            },
            popExitTransition = {
                if (isPeerNav(initialState, targetState)) {
                    fadeOut(tween(durationMillis = 70)) +
                    scaleOut(targetScale = 0.92f, animationSpec = tween(durationMillis = 70))
                } else {
                    slideOutHorizontally(tween(220)) { it / 3 } + fadeOut(tween(180))
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
            composable("daily_goal_selection") { DailyGoalSelectionScreen(navController) }
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

            // ── Feature-tour: 3-экранная карусель, показывается ровно
            //    один раз после прохождения auth-онбординга. После
            //    закрытия флаг featureTourSeen остаётся в DataStore и
            //    защищает от повторного показа.
            composable("feature_tour") { FeatureTourScreen(navController) }

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

            // ── Радио (новая фича 1.6.0) ───────────────────
            composable("radio") {
                com.spanishapp.radio.ui.RadioScreen(navController)
            }

            // ── Игры ─────────────────────────────────────────
            composable("games") { GamesScreen(navController) }
            composable("game_articles") {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    com.spanishapp.service.Analytics.gameStarted("articles")
                }
                ArticlesGameScreen(navController)
            }
            composable("game_speed") {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    com.spanishapp.service.Analytics.gameStarted("speed")
                }
                SpeedGameScreen(navController)
            }
            composable("game_math") {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    com.spanishapp.service.Analytics.gameStarted("math")
                }
                MathGameScreen(navController)
            }
            composable("game_crossword") {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    com.spanishapp.service.Analytics.gameStarted("crossword")
                }
                CrosswordGameScreen(navController)
            }
            composable("game_sopa") {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    com.spanishapp.service.Analytics.gameStarted("sopa")
                }
                SopaGameScreen(navController)
            }
            composable("game_palabra") {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    com.spanishapp.service.Analytics.gameStarted("palabra_maestra")
                }
                PalabraMaestraScreen(navController)
            }
            composable("game_libros") {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    com.spanishapp.service.Analytics.gameStarted("libros")
                }
                LibrosScreen(navController)
            }
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
                "flashcards_session?level={level}&category={category}&direction={direction}&setId={setId}&weak={weak}",
                arguments = listOf(
                    navArgument("level") { defaultValue = "A1" },
                    navArgument("category") { defaultValue = "all" },
                    navArgument("direction") { defaultValue = FlashcardDirection.ES_TO_RU.name },
                    navArgument("setId") { defaultValue = "" },
                    navArgument("weak") { defaultValue = "false" },
                )
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val level = args?.getString("level") ?: "A1"
                val category = args?.getString("category") ?: "all"
                val setId = args?.getString("setId").orEmpty()
                val weak = args?.getString("weak") == "true"
                val direction = runCatching {
                    FlashcardDirection.valueOf(args?.getString("direction") ?: "ES_TO_RU")
                }.getOrDefault(FlashcardDirection.ES_TO_RU)
                FlashcardsScreen(
                    navController = navController,
                    level = level,
                    category = category,
                    direction = direction,
                    setId = setId.ifBlank { null },
                    weakOnly = weak,
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
            composable("stats")        { com.spanishapp.ui.stats.StatsScreen(navController) }
            composable("settings")     { SettingsScreen(navController) }
            composable("settings_voice") { com.spanishapp.ui.settings.SettingsVoiceScreen(navController) }
            // ── Paywall PRO ─────────────────────────────────
            composable("paywall")      { com.spanishapp.ui.paywall.PaywallScreen(navController) }
            // OTA download route DISABLED for v1 — see comment near initialStartDest.
            // DownloadScreen + DownloadViewModel kept in source but unreachable.

            // ── Мини-тесты (между уроками, каждый 5-й шаг) ───
            composable(
                "minitest/{unitId}/{position}",
                arguments = listOf(
                    navArgument("unitId") { type = NavType.StringType },
                    navArgument("position") { type = NavType.IntType },
                )
            ) { entry ->
                val unitId = entry.arguments?.getString("unitId") ?: "1"
                val position = entry.arguments?.getInt("position") ?: 5
                com.spanishapp.ui.minitest.MiniTestScreen(navController, unitId, position)
            }

            // ── Чекпоинты A1 (v1.22.9, 4 блока × 1 CP) ───────
            composable(
                "checkpoint/{cpId}",
                arguments = listOf(androidx.navigation.navArgument("cpId") {
                    type = androidx.navigation.NavType.StringType
                })
            ) { entry ->
                val cpId = entry.arguments?.getString("cpId") ?: "cp1"
                com.spanishapp.ui.checkpoint.CheckpointScreen(navController, cpId)
            }

            // ── Share milestone progress (Spotify-Wrapped-style) ─────
            // Открывается из ResultView чекпоинта при passing (gold/silver/
            // bronze). 6 query-аргументов нужны потому что CheckpointState
            // живёт только in-memory в CheckpointViewModel.
            composable(
                "share/{cpId}/{tier}/{percent}/{xp}/{rounds}/{minutes}",
                arguments = listOf(
                    navArgument("cpId")    { type = NavType.StringType },
                    navArgument("tier")    { type = NavType.StringType },
                    navArgument("percent") { type = NavType.IntType },
                    navArgument("xp")      { type = NavType.IntType },
                    navArgument("rounds")  { type = NavType.IntType },
                    navArgument("minutes") { type = NavType.IntType },
                )
            ) { entry ->
                val args = com.spanishapp.ui.share.ShareArgs(
                    cpId = entry.arguments?.getString("cpId").orEmpty(),
                    tier = entry.arguments?.getString("tier").orEmpty(),
                    percent = entry.arguments?.getInt("percent") ?: 0,
                    xp = entry.arguments?.getInt("xp") ?: 0,
                    totalRounds = entry.arguments?.getInt("rounds") ?: 0,
                    timeMinutes = entry.arguments?.getInt("minutes") ?: 1,
                )
                com.spanishapp.ui.share.ShareProgressScreen(navController, args)
            }

            // ── Рейтинг / Лиги / Лидерборд ───────────────────
            composable("rating_full")  { RatingScreen(navController) }
            composable("leaderboard")  { LeaderboardScreen(navController) }
            composable("weekly_league") { com.spanishapp.ui.leaderboard.WeeklyLeagueScreen(navController) }

            // ── Словарь ───────────────────────────────────────
            composable("dictionary")  { DictionaryScreen(navController) }
            composable("weak_words")  { WeakWordsScreen(navController) }

            // ── Теория-карточки (грамматический справочник) ──
            composable(
                "theory/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) {
                com.spanishapp.ui.theory.TheoryReaderScreen(navController)
            }
            composable("theory_library") {
                com.spanishapp.ui.theory.TheoryLibraryScreen(navController)
            }

            // (старый checkpoint route с 18-актной системой удалён в v1.22.9 —
            // заменён на новую сцену-driven архитектуру выше: checkpoint/{cpId})
        }
    }
}
