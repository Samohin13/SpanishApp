package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.ConjugationDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.ConjugationEntity
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VerbTrainingMode { CHOICE, ASSEMBLY, INPUT }
enum class VerbGroup { REGULAR, STEM, IRREGULAR }

data class VerbWorkoutConfig(
    val selectedTenses: Set<String> = setOf("presente"),
    val groups: Set<VerbGroup> = setOf(VerbGroup.REGULAR, VerbGroup.IRREGULAR, VerbGroup.STEM),
    val reflexive: Set<Boolean> = setOf(true, false), // true = reflexive, false = non-reflexive
    val limitType: String = "все", // "топ-50", "топ-100", "топ-200", "все", "свой список"
    val mode: VerbTrainingMode = VerbTrainingMode.CHOICE,
    val selfCheck: Boolean = false,
    val isVoseo: Boolean = false,
    val hasTimer: Boolean = false,
    val timerValue: Int = 1 // minutes
)

data class VerbQuestion(
    val conjugation: ConjugationEntity,
    val pronounIndex: Int,
    val correctAnswer: String,
    val options: List<String> = emptyList(),
    val userValue: String = "",
    val isChecked: Boolean = false,
    val isCorrect: Boolean? = null,
    val allUserValues: List<String> = List(6) { "" },
    val allChecked: Boolean = false,
    val allResults: List<Boolean?> = List(6) { null }
)

data class VerbTrainingState(
    val config: VerbWorkoutConfig = VerbWorkoutConfig(),
    val questions: List<VerbQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val showSetup: Boolean = true,
    val timeLeftSeconds: Int = 0
)

@HiltViewModel
class VerbViewModel @Inject constructor(
    private val conjugationDao: ConjugationDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(VerbTrainingState())
    val state = _state.asStateFlow()

    private val pronouns = listOf("yo", "tú", "él/ella", "nosotros", "vosotros", "ellos")
    private val pronounsVoseo = listOf("yo", "vos", "él/ella", "nosotros", "vosotros", "ellos")

    private var timerJob: Job? = null

    fun updateConfig(config: VerbWorkoutConfig) {
        _state.value = _state.value.copy(config = config)
    }

    fun startTraining() {
        viewModelScope.launch {
            val config = _state.value.config
            val allConjugations = conjugationDao.getAll()
            
            // 1. Filter by tense
            var filtered = allConjugations.filter { it.tense in config.selectedTenses }

            // 2. Filter by group (regular/irregular/stem)
            // Note: In our current DB, ConjugationEntity only has isIrregular. 
            // We'd need WordEntity.verbSubtype for "stem" check, but for now we'll use isIrregular.
            filtered = filtered.filter { conj ->
                if (conj.isIrregular) VerbGroup.IRREGULAR in config.groups else VerbGroup.REGULAR in config.groups
            }

            // 3. Filter by reflexivity
            filtered = filtered.filter { conj ->
                val isReflexive = conj.verb.lowercase().endsWith("se")
                (isReflexive && true in config.reflexive) || (!isReflexive && false in config.reflexive)
            }

            // 4. Apply Limit
            val limit = when(config.limitType) {
                "топ-50" -> 50
                "топ-100" -> 100
                "топ-200" -> 200
                else -> 1000
            }
            
            val finalPool = filtered.shuffled().take(limit)

            val questions = finalPool.map { conj ->
                val pIdx = if (config.mode == VerbTrainingMode.ASSEMBLY) 0 else (0..5).random()
                val forms = listOf(conj.yo, conj.tu, conj.el, conj.nosotros, conj.vosotros, conj.ellos)
                var correct = forms[pIdx]
                
                if (config.isVoseo && pIdx == 1 && (conj.tense == "presente" || conj.tense == "imperativo")) {
                    correct = convertToVoseo(conj.verb, conj.tense)
                }

                val options = if (config.mode == VerbTrainingMode.CHOICE) {
                    (allConjugations.shuffled().take(3).map { 
                        listOf(it.yo, it.tu, it.el, it.nosotros, it.vosotros, it.ellos).random() 
                    } + correct).shuffled()
                } else emptyList()

                VerbQuestion(conj, pIdx, correct, options)
            }

            _state.value = _state.value.copy(
                questions = questions,
                currentIndex = 0,
                score = 0,
                isGameOver = false,
                showSetup = false,
                timeLeftSeconds = if (config.hasTimer) config.timerValue * 60 else 0
            )

            if (config.hasTimer) startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeftSeconds > 0) {
                delay(1000)
                _state.value = _state.value.copy(timeLeftSeconds = _state.value.timeLeftSeconds - 1)
            }
            finishTraining()
        }
    }

    private fun convertToVoseo(verb: String, tense: String): String {
        return when {
            verb.endsWith("ar") -> verb.dropLast(2) + "ás"
            verb.endsWith("er") -> verb.dropLast(2) + "és"
            verb.endsWith("ir") -> verb.dropLast(2) + "ís"
            else -> verb
        }
    }

    fun submitAnswer(value: String) {
        val s = _state.value
        val q = s.questions[s.currentIndex]
        val isCorrect = value.trim().lowercase() == q.correctAnswer.lowercase()
        
        val updatedQuestions = s.questions.toMutableList()
        updatedQuestions[s.currentIndex] = q.copy(
            userValue = value,
            isChecked = true,
            isCorrect = isCorrect
        )

        _state.value = s.copy(
            questions = updatedQuestions,
            score = if (isCorrect) s.score + 1 else s.score
        )

        if (isCorrect) tts.speak(q.correctAnswer)
    }

    fun submitAssembly(values: List<String>) {
        val s = _state.value
        val q = s.questions[s.currentIndex]
        val correctForms = listOf(q.conjugation.yo, q.conjugation.tu, q.conjugation.el, q.conjugation.nosotros, q.conjugation.vosotros, q.conjugation.ellos)
        
        val results = values.mapIndexed { index, v -> v.trim().lowercase() == correctForms[index].lowercase() }
        val correctCount = results.count { it }
        
        val updatedQuestions = s.questions.toMutableList()
        updatedQuestions[s.currentIndex] = q.copy(
            allUserValues = values,
            allChecked = true,
            allResults = results
        )

        _state.value = s.copy(
            questions = updatedQuestions,
            score = s.score + (if (correctCount == 6) 1 else 0)
        )
    }

    fun nextQuestion() {
        val s = _state.value
        if (s.currentIndex + 1 >= s.questions.size) {
            finishTraining()
        } else {
            _state.value = s.copy(currentIndex = s.currentIndex + 1)
        }
    }

    private fun finishTraining() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isGameOver = true)
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            userProgressDao.update(p.copy(totalXp = p.totalXp + (_state.value.score * 10)))
            achievementManager.checkAndUnlock()
        }
    }

    fun getPronoun(index: Int): String {
        return if (_state.value.config.isVoseo) pronounsVoseo[index] else pronouns[index]
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
