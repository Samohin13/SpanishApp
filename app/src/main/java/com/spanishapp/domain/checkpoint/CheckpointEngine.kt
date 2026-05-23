package com.spanishapp.domain.checkpoint

import java.util.Locale
import javax.inject.Inject

/**
 * Stateless engine для оценки ответов и переходов между раундами.
 * Состояние держится в ViewModel, engine только обрабатывает.
 */
class CheckpointEngine @Inject constructor() {

    /**
     * Проверяет ответ юзера для текущего раунда. Возвращает true если правильно.
     * Для TRANSLATE использует список acceptable_alternatives (нечёткое сравнение).
     */
    fun checkAnswer(round: CheckpointRound, userAnswer: String): Boolean {
        val normalized = normalize(userAnswer)
        val correct = normalize(round.correctAnswer)
        if (normalized == correct) return true

        // Для TRANSLATE — проверяем альтернативы
        if (round.format == RoundFormat.TRANSLATE_RU_ES ||
            round.format == RoundFormat.TRANSLATE_ES_RU) {
            return round.acceptableAlternatives.any { normalize(it) == normalized }
        }
        return false
    }

    /**
     * Обработка ответа в раунде. Возвращает новое состояние:
     *  - currentRoundIndex увеличен
     *  - answers пополнен
     *  - sympathyStars уменьшен если ошибка
     *  - isFinished если последний раунд
     *  - outcome если isFinished
     */
    fun submitAnswer(
        state: CheckpointState,
        userAnswer: String,
        timeMs: Long,
    ): CheckpointState {
        val round = state.currentRound ?: return state
        val isCorrect = checkAnswer(round, userAnswer)

        val newAnswer = RoundAnswer(
            roundIndex = state.currentRoundIndex,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            timeMs = timeMs,
        )
        val newAnswers = state.answers + newAnswer
        val newSympathy = if (isCorrect) state.sympathyStars
                         else (state.sympathyStars - 1).coerceAtLeast(0)
        val nextIndex = state.currentRoundIndex + 1
        val finished = nextIndex >= state.totalRounds || newSympathy == 0

        return if (finished) {
            val percent = if (newAnswers.isEmpty()) 0
                         else (newAnswers.count { it.isCorrect } * 100) / newAnswers.size
            state.copy(
                currentRoundIndex = nextIndex,
                answers = newAnswers,
                sympathyStars = newSympathy,
                isFinished = true,
                outcome = computeOutcome(state.data, percent, newAnswers),
            )
        } else {
            state.copy(
                currentRoundIndex = nextIndex,
                answers = newAnswers,
                sympathyStars = newSympathy,
            )
        }
    }

    /**
     * Расчёт финального исхода. 6 уровней:
     *  Pass: gold ≥ goldPercent, silver ≥ silverPercent, bronze ≥ bronzePercent
     *  Fail: near_pass 50-69, low 30-49, very_low 0-29
     */
    private fun computeOutcome(
        data: CheckpointData,
        percent: Int,
        answers: List<RoundAnswer>,
    ): CheckpointOutcome {
        val t = data.thresholds
        return when {
            percent >= t.goldPercent -> {
                val od = data.passOutcomes["gold"] ?: data.passOutcomes.values.first()
                CheckpointOutcome.Pass(
                    percent = percent,
                    tier = "gold",
                    outcomeData = od,
                    xpAwarded = data.rewards.bronzeXp + data.rewards.goldXpBonus,
                    badgeAwarded = data.rewards.badgeId,
                    unlocksBlock = data.rewards.unlocksBlock,
                )
            }
            percent >= t.silverPercent -> {
                val od = data.passOutcomes["silver"] ?: data.passOutcomes.values.first()
                CheckpointOutcome.Pass(
                    percent = percent,
                    tier = "silver",
                    outcomeData = od,
                    xpAwarded = data.rewards.bronzeXp + data.rewards.silverXpBonus,
                    badgeAwarded = data.rewards.badgeId,
                    unlocksBlock = data.rewards.unlocksBlock,
                )
            }
            percent >= t.bronzePercent -> {
                val od = data.passOutcomes["bronze"] ?: data.passOutcomes.values.first()
                CheckpointOutcome.Pass(
                    percent = percent,
                    tier = "bronze",
                    outcomeData = od,
                    xpAwarded = data.rewards.bronzeXp,
                    badgeAwarded = data.rewards.badgeId,
                    unlocksBlock = data.rewards.unlocksBlock,
                )
            }
            percent >= 50 -> {
                val od = data.failOutcomes["near_pass"] ?: data.failOutcomes.values.first()
                CheckpointOutcome.Fail(
                    percent = percent,
                    tier = "near_pass",
                    outcomeData = od,
                    canRetryWithRatingCost = true,
                    cooldownUntilMs = System.currentTimeMillis() + COOLDOWN_24H,
                    weakLessons = extractWeakLessons(data, answers),
                )
            }
            percent >= 30 -> {
                val od = data.failOutcomes["low"] ?: data.failOutcomes.values.first()
                CheckpointOutcome.Fail(
                    percent = percent,
                    tier = "low",
                    outcomeData = od,
                    canRetryWithRatingCost = true,
                    cooldownUntilMs = System.currentTimeMillis() + COOLDOWN_24H,
                    weakLessons = extractWeakLessons(data, answers),
                )
            }
            else -> {
                val od = data.failOutcomes["very_low"] ?: data.failOutcomes.values.first()
                CheckpointOutcome.Fail(
                    percent = percent,
                    tier = "very_low",
                    outcomeData = od,
                    canRetryWithRatingCost = false,    // совсем плохо — только повторять уроки
                    cooldownUntilMs = System.currentTimeMillis() + COOLDOWN_24H,
                    weakLessons = extractWeakLessons(data, answers),
                )
            }
        }
    }

    /** Сборка списка уроков на повтор — из tested_lesson в ошибочных раундах. */
    private fun extractWeakLessons(data: CheckpointData, answers: List<RoundAnswer>): List<String> {
        val wrong = answers.filter { !it.isCorrect }
        return wrong.mapNotNull { a -> data.rounds.getOrNull(a.roundIndex)?.testedLesson }
            .filter { it.isNotBlank() }
            .distinct()
    }

    /** Нормализация ответа для сравнения: lowercase, trim, без пунктуации, single-space. */
    private fun normalize(text: String): String =
        text.lowercase(Locale("es", "ES"))
            .replace(Regex("[¿?¡!.,;:\"'()]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

    companion object {
        const val COOLDOWN_24H = 24L * 60L * 60L * 1000L
    }
}
