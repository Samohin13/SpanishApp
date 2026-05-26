package com.spanishapp.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.spanishapp.data.db.dao.ActivityTimeLogDao
import com.spanishapp.data.db.entity.ActivityTimeLogEntity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

/**
 * Типы учебной активности для per-activity timer log.
 *
 * Радио НЕ включено — у него уже есть отдельный таймер
 * (radio_listening_session) пишущий точные секунды каждой сессии.
 */
enum class ActivityType {
    LESSON,      // LessonSessionScreen, LessonContentScreen
    FLASHCARDS,  // FlashcardsScreen, PracticeScreen
    GAME,        // 6 экранов игр (Articles, Speed, Math, Sopa, Palabra, Crossword, Verb, Libros-game)
    BOOK,        // LibroReadScreen
}

/**
 * Composable-хук: пишет ActivityTimeLogEntity при выходе с экрана.
 * Использовать в начале каждого учебного Composable:
 *
 *   @Composable fun LessonSessionScreen(...) {
 *       TrackActivity(ActivityType.LESSON)
 *       ...
 *   }
 *
 * Реализация — DisposableEffect, в onDispose пишет сессию через
 * GlobalScope (чтобы переживала смерть Composable) и Hilt EntryPoint
 * (чтобы не плодить параметры).
 */
@Composable
fun TrackActivity(type: ActivityType) {
    val context = LocalContext.current.applicationContext
    DisposableEffect(type) {
        val startedAt = System.currentTimeMillis()
        onDispose {
            val endedAt = System.currentTimeMillis()
            val duration = endedAt - startedAt
            // Игнорируем микро-сессии (<5 сек) — пользователь зашёл и тут же вышел,
            // это шум который раздуёт таблицу и не отразит реальную активность.
            if (duration < 5_000) return@onDispose
            val dao = EntryPointAccessors
                .fromApplication(context, ActivityTrackerEntryPoint::class.java)
                .activityTimeLogDao()
            ActivityTrackerScope.launch {
                runCatching {
                    dao.insert(
                        ActivityTimeLogEntity(
                            activityType = type.name,
                            startedAt    = startedAt,
                            endedAt      = endedAt,
                        )
                    )
                }
            }
        }
    }
}

/** Hilt EntryPoint — нужен чтобы достать DAO из Composable без @Inject ViewModel. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ActivityTrackerEntryPoint {
    fun activityTimeLogDao(): ActivityTimeLogDao
}

/**
 * Application-scoped coroutine scope — переживает смерть любого
 * Composable/ViewModel. Используется только этим трекером, чтобы запись
 * сессии гарантированно дошла до БД даже если юзер быстро выходит.
 */
private val ActivityTrackerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
