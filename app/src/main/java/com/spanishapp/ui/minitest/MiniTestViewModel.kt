package com.spanishapp.ui.minitest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.prefs.MiniTestPreferences
import com.spanishapp.domain.algorithm.XpSystem
import com.spanishapp.domain.minitest.MiniTest
import com.spanishapp.domain.minitest.MiniTestGenerator
import com.spanishapp.service.UiSoundPlayer
import com.spanishapp.service.XpTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Phases of a mini-test session.
 */
enum class MiniTestPhase { INTRO, PLAYING, RESULT }

data class MiniTestUiState(
    val isLoading: Boolean = true,
    val miniTest: MiniTest? = null,
    val phase: MiniTestPhase = MiniTestPhase.INTRO,
    val currentIndex: Int = 0,
    /** answer correctness per question (true = correct), filled as we go. */
    val answers: List<Boolean> = emptyList(),
    /** Set once the result has been persisted (XP awarded, marked passed). */
    val resultPersisted: Boolean = false,
) {
    val total: Int get() = miniTest?.exercises?.size ?: 0
    val correctCount: Int get() = answers.count { it }
    val score: Float
        get() = if (total == 0) 0f else correctCount.toFloat() / total
    val passed: Boolean
        get() = score >= MiniTestGenerator.PASS_THRESHOLD
}

@HiltViewModel
class MiniTestViewModel @Inject constructor(
    private val xpTracker: XpTracker,
    private val miniTestPreferences: MiniTestPreferences,
    private val uiSound: UiSoundPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(MiniTestUiState())
    val state: StateFlow<MiniTestUiState> = _state.asStateFlow()

    /** Load by route args. Call once from the Composable. */
    fun load(unitId: String, position: Int) {
        if (_state.value.miniTest != null) return
        val mt = MiniTestGenerator.generate(unitId, position)
        _state.update {
            it.copy(
                isLoading = false,
                miniTest = mt,
            )
        }
    }

    fun start() {
        _state.update { it.copy(phase = MiniTestPhase.PLAYING, currentIndex = 0, answers = emptyList()) }
    }

    /** Called from the exercise renderer when the user submits an answer. */
    fun submitAnswer(correct: Boolean) {
        val current = _state.value
        val mt = current.miniTest ?: return
        if (current.phase != MiniTestPhase.PLAYING) return

        // SFX: правильный/неправильный звук одновременно с UI feedback.
        uiSound.play(if (correct) UiSoundPlayer.Sound.CORRECT else UiSoundPlayer.Sound.WRONG)

        val nextAnswers = current.answers + correct
        val nextIndex = current.currentIndex + 1
        if (nextIndex >= mt.exercises.size) {
            // Finish the test.
            _state.update {
                it.copy(
                    answers = nextAnswers,
                    currentIndex = nextIndex,
                    phase = MiniTestPhase.RESULT,
                )
            }
            persistResultIfNeeded()
        } else {
            _state.update {
                it.copy(
                    answers = nextAnswers,
                    currentIndex = nextIndex,
                )
            }
        }
    }

    private fun persistResultIfNeeded() {
        val s = _state.value
        if (s.resultPersisted) return
        val mt = s.miniTest ?: return
        if (!s.passed) {
            // Тихий fail — без громкого FAIL звука (mini-test это не
            // экзамен). Просто помечаем persisted.
            _state.update { it.copy(resultPersisted = true) }
            return
        }
        viewModelScope.launch {
            // v1.25.97 FIX (audit M5): XP только за ПЕРВЫЙ pass. markPassed
            // писался, но никогда не читался как гейт — реплей одного мини-теста
            // давал +20 XP каждый раз.
            val alreadyPassed = miniTestPreferences.isPassed(mt.id).first()
            if (!alreadyPassed) {
                xpTracker.add(xp = XpSystem.MINI_TEST_PASSED, words = 0)
                miniTestPreferences.markPassed(mt.id)
            }
            // SFX: mini-test pass — победный аккорд (≥60% правильных).
            // Задержка чтобы не наложиться на последний CORRECT.
            kotlinx.coroutines.delay(500)
            uiSound.play(UiSoundPlayer.Sound.LEVEL_UP)
            _state.update { it.copy(resultPersisted = true) }
        }
    }
}
