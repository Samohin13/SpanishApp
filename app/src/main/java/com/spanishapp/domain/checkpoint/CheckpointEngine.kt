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

        // Свободный ввод (TRANSLATE / VOICE) — fuzzy match. Допуск Levenshtein
        // = max(1, len/8), не более 3. Это прощает: мелкие опечатки в TRANSLATE,
        // шумы STT в VOICE (типа «soy de Rusia» vs «sois de Rusia»).
        if (round.format == RoundFormat.TRANSLATE_RU_ES ||
            round.format == RoundFormat.TRANSLATE_ES_RU ||
            round.format == RoundFormat.VOICE
        ) {
            val candidates = (listOf(correct) +
                round.acceptableAlternatives.map { normalize(it) })
                .filter { it.isNotBlank() }
            if (candidates.any { it == normalized }) return true
            val tolerance = (correct.length / 8).coerceIn(1, 3)
            return candidates.any { levenshtein(it, normalized) <= tolerance }
        }
        return false
    }

    /** Levenshtein distance — для fuzzy-сравнения в TRANSLATE / VOICE. */
    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    curr[j - 1] + 1,    // insertion
                    prev[j] + 1,        // deletion
                    prev[j - 1] + cost, // substitution
                )
            }
            System.arraycopy(curr, 0, prev, 0, curr.size)
        }
        return prev[b.length]
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
        // v1.22.18: убран early exit на sympathy=0. Настоящий экзамен идёт
        // до последнего раунда независимо от ошибок — это педагогически
        // правильно (DELE / TOEFL / IELTS все так работают). Sympathy stars
        // только визуальная индикация недовольства NPC, не game-over.
        // Раньше юзер с 5 ошибками выкидывался после 5 раундов из 22.
        val finished = nextIndex >= state.totalRounds

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
