package com.spanishapp.domain.checkpoint

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные одного чекпоинта (CP1..CP4). Грузятся из
 * `assets/checkpoints/cpN_<cefr>_<theme>.json`.
 *
 * Структура соответствует cp1_a1_passport.json. Каждый чекпоинт = одна
 * сцена + один или несколько NPC + 16-25 раундов фиксированного контента.
 *
 * v1.22.9: чекпоинты — финальный экзамен блока курса. Pass открывает
 * следующий блок, fail задерживает на границе (стори-механика).
 */
@Serializable
data class CheckpointData(
    val id: String,                              // "cp1" .. "cp4"
    val cefr: String,                            // "A1", "A2", "B1", "B2"
    val block: Int,                              // 1..4
    @SerialName("title_ru") val titleRu: String,
    @SerialName("title_es") val titleEs: String,
    @SerialName("description_ru") val descriptionRu: String,
    @SerialName("stakes_ru") val stakesRu: String = "",
    @SerialName("intro_text_ru") val introTextRu: String = "",
    val scene: CheckpointScene,
    val npc: CheckpointNpc,
    @SerialName("user_character") val userCharacter: UserCharacter,
    val thresholds: CheckpointThresholds,
    val rewards: CheckpointRewards,
    val rounds: List<CheckpointRound>,
    @SerialName("pass_outcomes") val passOutcomes: Map<String, OutcomeData>,
    @SerialName("fail_outcomes") val failOutcomes: Map<String, OutcomeData>,
)

@Serializable
data class CheckpointScene(
    val name: String,
    val type: String,                            // "passport_control", "cafe", etc.
    val city: String,
    @SerialName("background_image") val backgroundImage: String,
    @SerialName("ambient_sound") val ambientSound: String = "",
)

@Serializable
data class CheckpointNpc(
    val id: String,
    val name: String,
    @SerialName("role_ru") val roleRu: String,
    @SerialName("role_es") val roleEs: String,
    @SerialName("voice_id") val voiceId: String,
    @SerialName("portrait_image") val portraitImage: String,
    val personality: String = "",
)

@Serializable
data class UserCharacter(
    @SerialName("default_name") val defaultName: String,
    val gender: String = "neutral",              // "masculine", "feminine", "neutral"
    val nationality: String = "",
    val country: String = "",
    val note: String = "",
)

@Serializable
data class CheckpointThresholds(
    @SerialName("bronze_percent") val bronzePercent: Int = 70,
    @SerialName("silver_percent") val silverPercent: Int = 80,
    @SerialName("gold_percent") val goldPercent: Int = 95,
)

@Serializable
data class CheckpointRewards(
    @SerialName("bronze_xp") val bronzeXp: Int = 250,
    @SerialName("silver_xp_bonus") val silverXpBonus: Int = 50,
    @SerialName("gold_xp_bonus") val goldXpBonus: Int = 150,
    @SerialName("badge_id") val badgeId: String,
    @SerialName("badge_name_ru") val badgeNameRu: String,
    @SerialName("unlocks_block") val unlocksBlock: Int,
)

/**
 * Один раунд внутри чекпоинта. Все поля кроме `round`, `format`, `topic_ru`
 * опциональны — каждый формат использует свой набор.
 */
@Serializable
data class CheckpointRound(
    val round: Int,
    val format: RoundFormat,
    @SerialName("topic_ru") val topicRu: String,
    @SerialName("tested_grammar") val testedGrammar: String = "",
    @SerialName("tested_lesson") val testedLesson: String = "",

    // Реплика NPC (есть почти у всех форматов кроме TRANSLATE_ES_RU)
    @SerialName("npc_line_es") val npcLineEs: String? = null,
    @SerialName("npc_line_ru") val npcLineRu: String? = null,
    @SerialName("audio_only") val audioOnly: Boolean = false,

    // Инструкция юзеру
    @SerialName("prompt_ru") val promptRu: String = "",

    // CHOICE / LISTEN: правильный ответ + 3 distractor'а
    @SerialName("correct_answer") val correctAnswer: String,
    val distractors: List<String> = emptyList(),

    // TRANSLATE: дополнительные приемлемые варианты
    @SerialName("acceptable_alternatives") val acceptableAlternatives: List<String> = emptyList(),
    @SerialName("prompt_text_ru") val promptTextRu: String = "",
    @SerialName("prompt_text_es") val promptTextEs: String = "",
    @SerialName("hint_ru") val hintRu: String = "",

    // BUILD: банк слов
    @SerialName("word_bank") val wordBank: List<String> = emptyList(),

    // CONJUGATE: шаблон предложения + глагол
    @SerialName("sentence_template") val sentenceTemplate: String = "",
    @SerialName("verb_infinitive") val verbInfinitive: String = "",

    // LISTEN: что показывать после ответа
    @SerialName("translation_after_answer_ru") val translationAfterAnswerRu: String = "",

    // Объяснение при провале
    @SerialName("explanation_on_fail_ru") val explanationOnFailRu: String = "",

    // Реакции NPC на правильный/неправильный ответ
    @SerialName("carlos_reaction_correct_ru") val reactionCorrectRu: String = "",
    @SerialName("carlos_reaction_wrong_ru") val reactionWrongRu: String = "",
)

@Serializable
enum class RoundFormat {
    @SerialName("CHOICE") CHOICE,
    @SerialName("BUILD") BUILD,
    @SerialName("CONJUGATE") CONJUGATE,
    @SerialName("LISTEN") LISTEN,
    @SerialName("TRANSLATE_RU_ES") TRANSLATE_RU_ES,
    @SerialName("TRANSLATE_ES_RU") TRANSLATE_ES_RU,
    @SerialName("VOICE") VOICE,                  // опционально, для будущего
}

/**
 * Один из 6 исходов: 3 уровня pass (gold/silver/bronze) + 3 уровня fail
 * (near_pass / low / very_low). Ключи в map — эти строки.
 */
@Serializable
data class OutcomeData(
    @SerialName("carlos_line_es") val npcLineEs: String,
    @SerialName("carlos_line_ru") val npcLineRu: String,
    @SerialName("scene_description_ru") val sceneDescriptionRu: String = "",
    @SerialName("stakes_ru") val stakesRu: String = "",
)

/**
 * Текущее состояние прохождения чекпоинта (in-memory + сохраняется в Room
 * между раундами для resume-функции).
 */
data class CheckpointState(
    val data: CheckpointData,
    val currentRoundIndex: Int = 0,
    val answers: List<RoundAnswer> = emptyList(),
    val sympathyStars: Int = 5,                  // 5..0, уменьшается с ошибками
    val isFinished: Boolean = false,
    val outcome: CheckpointOutcome? = null,
) {
    val currentRound: CheckpointRound?
        get() = data.rounds.getOrNull(currentRoundIndex)

    val correctCount: Int get() = answers.count { it.isCorrect }
    val totalRounds: Int get() = data.rounds.size
    val progressFraction: Float
        get() = if (totalRounds == 0) 0f else (currentRoundIndex.toFloat() / totalRounds)

    val accuracyPercent: Int
        get() = if (answers.isEmpty()) 0 else (correctCount * 100) / answers.size
}

data class RoundAnswer(
    val roundIndex: Int,
    val userAnswer: String,
    val isCorrect: Boolean,
    val timeMs: Long,
)

/**
 * Финальный результат прохождения.
 */
sealed class CheckpointOutcome {
    abstract val percent: Int
    abstract val tier: String                    // "gold"/"silver"/"bronze"/"near_pass"/"low"/"very_low"
    abstract val outcomeData: OutcomeData

    data class Pass(
        override val percent: Int,
        override val tier: String,
        override val outcomeData: OutcomeData,
        val xpAwarded: Int,
        val badgeAwarded: String,
        val unlocksBlock: Int,
    ) : CheckpointOutcome()

    data class Fail(
        override val percent: Int,
        override val tier: String,
        override val outcomeData: OutcomeData,
        val canRetryWithRatingCost: Boolean,     // если рейтинг >= 50
        val cooldownUntilMs: Long,               // 24ч от сейчас
        val weakLessons: List<String>,           // lesson ids для повтора
    ) : CheckpointOutcome()
}
