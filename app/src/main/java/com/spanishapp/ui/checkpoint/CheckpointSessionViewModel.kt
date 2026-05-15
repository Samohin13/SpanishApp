package com.spanishapp.ui.checkpoint

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.checkpoint.CheckpointAct
import com.spanishapp.data.checkpoint.CheckpointContent
import com.spanishapp.data.checkpoint.CheckpointContentData
import com.spanishapp.data.db.dao.LessonProgressDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.LessonProgressEntity
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.domain.algorithm.XpSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для CheckpointSession.
 *
 * Управляет:
 *   • Текущая сцена и акт (3 сцены × 6 актов = 18 шагов)
 *   • Подсчёт правильных ответов
 *   • Отметка прогресса в lesson_progress (как обычный урок)
 *   • Бонус XP / достижение в финале
 */
@HiltViewModel
class CheckpointSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lessonProgressDao: LessonProgressDao,
    private val userProgressDao: UserProgressDao,
    private val ratingUpdater: RatingUpdater,
) : ViewModel() {

    private val checkpointId: String = savedStateHandle.get<String>("checkpointId") ?: ""

    private val _state = MutableStateFlow(CheckpointSessionState())
    val state: StateFlow<CheckpointSessionState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        val content = CheckpointContentData.byId(checkpointId)
        if (content == null) {
            _state.value = CheckpointSessionState(notFound = true)
            return
        }
        _state.value = CheckpointSessionState(
            content = content,
            sceneIndex = 0,
            actIndex = 0,
            correctCount = 0,
            wrongCount = 0,
            answered = false,
            finished = false,
        )
    }

    /** Текущий акт. Null если уроки кончились. */
    val currentAct: CheckpointAct?
        get() {
            val s = _state.value
            val c = s.content ?: return null
            val scene = c.scenes.getOrNull(s.sceneIndex) ?: return null
            return scene.acts.getOrNull(s.actIndex)
        }

    /** Записать ответ юзера. */
    fun submitAnswer(isCorrect: Boolean) {
        val s = _state.value
        _state.value = s.copy(
            answered = true,
            correctCount = if (isCorrect) s.correctCount + 1 else s.correctCount,
            wrongCount = if (!isCorrect) s.wrongCount + 1 else s.wrongCount,
            lastAnswerCorrect = isCorrect,
        )
    }

    /** Перейти к следующему акту. Если кончились — финал. */
    fun nextAct() {
        val s = _state.value
        val c = s.content ?: return
        val curScene = c.scenes.getOrNull(s.sceneIndex) ?: return

        if (s.actIndex + 1 < curScene.acts.size) {
            _state.value = s.copy(actIndex = s.actIndex + 1, answered = false, lastAnswerCorrect = null)
        } else if (s.sceneIndex + 1 < c.scenes.size) {
            _state.value = s.copy(
                sceneIndex = s.sceneIndex + 1, actIndex = 0,
                answered = false, lastAnswerCorrect = null,
            )
        } else {
            // Финал — записываем прогресс
            finishCheckpoint()
        }
    }

    private fun finishCheckpoint() {
        val s = _state.value
        val c = s.content ?: return
        viewModelScope.launch {
            // Отметить как пройденный урок
            val now = System.currentTimeMillis()
            val accuracy = if (s.correctCount + s.wrongCount > 0)
                s.correctCount.toFloat() / (s.correctCount + s.wrongCount) else 0f
            val passed = accuracy >= 0.7f  // 70%+ = пройдено

            // unitId/lessonIndex парсятся из ID типа u4_l14
            val parts = c.id.removePrefix("u").split("_l")
            val unitId = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val lessonIndex = parts.getOrNull(1)?.toIntOrNull() ?: 0

            if (passed) {
                lessonProgressDao.markComplete(
                    LessonProgressEntity(
                        lessonKey = c.id,
                        unitId = unitId,
                        lessonIndex = lessonIndex,
                        completedAt = now,
                    )
                )
            }

            // XP бонус (только если passed)
            if (passed) {
                val xp = c.bonusXp + (s.correctCount * 5)
                userProgressDao.addXpAndWords(xp, 0)
                // Рейтинг — каждый правильный = applyAnswer
                repeat(s.correctCount) {
                    ratingUpdater.applyAnswer(easeFactor = 2.0f, quality = 4)
                }
            }

            _state.value = s.copy(
                finished = true,
                passed = passed,
                accuracyPercent = (accuracy * 100).toInt(),
                xpEarned = if (passed) c.bonusXp + s.correctCount * 5 else 0,
            )
        }
    }
}

data class CheckpointSessionState(
    val content: CheckpointContent? = null,
    val sceneIndex: Int = 0,
    val actIndex: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val answered: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val finished: Boolean = false,
    val passed: Boolean = false,
    val accuracyPercent: Int = 0,
    val xpEarned: Int = 0,
    val notFound: Boolean = false,
)
