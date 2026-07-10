package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.ConjugationDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WeakVerbDao
import com.spanishapp.data.db.entity.ConjugationEntity
import com.spanishapp.data.db.entity.WeakVerbEntity
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 4 режима тренажёра — как у Espato. */
enum class VerbTrainingMode(val title: String, val desc: String) {
    CONJUGAR("Conjugar", "Время + лицо + инфинитив → форма"),
    INVERSO ("Inverso",  "Форма → инфинитив + лицо + время"),
    HUECO   ("Hueco",    "Заполни пропуск во фразе"),
    AUDITIVO("Auditivo", "Услышь форму и запиши")
}

enum class VerbGroup { REGULAR, IRREGULAR, REFLEXIVE }

data class VerbWorkoutConfig(
    val mode: VerbTrainingMode = VerbTrainingMode.CONJUGAR,
    val selectedTenses: Set<String> = setOf("presente"),
    val groups: Set<VerbGroup> = setOf(VerbGroup.REGULAR, VerbGroup.IRREGULAR),
    val includeReflexive: Boolean = true,
    val isVoseo: Boolean = false,
    val acceptNoAccent: Boolean = true,
    val questionCount: Int = 20,
    /** Highest frequency-tier to include. 1 = top-50, 5 = full list (~850). */
    val maxTier: Int = 2,
)

/** Один вопрос в сессии. Поля используются по-разному в зависимости от режима. */
data class VerbQuestion(
    val conjugation: ConjugationEntity,
    val pronounIndex: Int,                  // 0..5
    val correctAnswer: String,              // правильная форма
    // Inverso:
    val pickedInfinitive: String? = null,   // что показано (для Inverso)
    // Hueco:
    val sentenceTemplate: String = "",      // фраза с ___
    // Auditivo:
    val ttsText: String = "",               // что озвучить
    // Состояние ответа
    val userInput: String = "",
    val isChecked: Boolean = false,
    val isCorrect: Boolean? = null,
    val nearMiss: Boolean = false,          // правильно, но без акцентов
    // Inverso: пользователь выбрал
    val pickedInf: String = "",
    val pickedTense: String = "",
    val pickedPronounIdx: Int = -1,
    // v1.25.78 VERB-2: правило для показа после ошибки (типа "e→ie в 1,2,3,6 лицах")
    val explanation: String = "",
)

data class VerbTrainingState(
    val config: VerbWorkoutConfig = VerbWorkoutConfig(),
    val questions: List<VerbQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val nearMissCount: Int = 0,
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val showSetup: Boolean = true
) {
    val total: Int get() = questions.size.coerceAtLeast(1)
}

@HiltViewModel
class VerbViewModel @Inject constructor(
    private val conjugationDao: ConjugationDao,
    private val wordDao: com.spanishapp.data.db.dao.WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts,
    private val ratingUpdater: RatingUpdater,
    private val weakVerbDao: WeakVerbDao,  // v1.25.78 VERB-4
) : ViewModel() {

    private val _state = MutableStateFlow(VerbTrainingState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null

    private val pronouns = listOf("yo", "tú", "él/ella", "nosotros", "vosotros", "ellos")
    private val pronounsVoseo = listOf("yo", "vos", "él/ella", "nosotros", "vosotros", "ellos")
    private val allTenses = listOf(
        "presente", "preterito", "imperfecto", "futuro",
        "condicional", "subjuntivo",
        // v1.25.78: B2 времена (Subj. Imperfecto + Imperativo)
        "subjuntivo_imperfecto", "imperativo",
    )

    fun updateConfig(c: VerbWorkoutConfig) {
        _state.value = _state.value.copy(config = c)
    }

    /**
     * v1.25.78 VERB-3: Recommended workout по CEFR уровню юзера.
     * Прогрессивно открывает времена и tier'ы:
     *  - A1: presente only + Tier 1 (топ-50 глаголов)
     *  - A2: + preterito + imperfecto + Tier 2 (топ-100)
     *  - B1: + futuro + condicional + subjuntivo + Tier 3 (топ-200)
     *  - B2: + subjuntivo_imperfecto + imperativo + Tier 4 (топ-350)
     *
     * Применяется на кнопку «Рекомендуемая тренировка» в setup-экране.
     */
    fun applyRecommended(cefr: String) {
        val cfg = when (cefr.uppercase()) {
            "A1" -> VerbWorkoutConfig(
                selectedTenses = setOf("presente"),
                groups = setOf(VerbGroup.REGULAR, VerbGroup.IRREGULAR),
                includeReflexive = false,  // меньше сложности для новичка
                questionCount = 15,
                maxTier = 1,
            )
            "A2" -> VerbWorkoutConfig(
                selectedTenses = setOf("presente", "preterito", "imperfecto"),
                groups = setOf(VerbGroup.REGULAR, VerbGroup.IRREGULAR),
                includeReflexive = true,
                questionCount = 20,
                maxTier = 2,
            )
            "B1" -> VerbWorkoutConfig(
                selectedTenses = setOf("presente", "preterito", "imperfecto", "futuro", "condicional", "subjuntivo"),
                groups = setOf(VerbGroup.REGULAR, VerbGroup.IRREGULAR),
                includeReflexive = true,
                questionCount = 25,
                maxTier = 3,
            )
            "B2" -> VerbWorkoutConfig(
                selectedTenses = setOf(
                    "presente", "preterito", "imperfecto", "futuro",
                    "condicional", "subjuntivo",
                    "subjuntivo_imperfecto", "imperativo",
                ),
                groups = setOf(VerbGroup.REGULAR, VerbGroup.IRREGULAR),
                includeReflexive = true,
                questionCount = 30,
                maxTier = 4,
            )
            else -> _state.value.config  // unknown level → не трогаем
        }
        _state.value = _state.value.copy(config = cfg)
    }

    fun openSetup() {
        timerJob?.cancel()
        _state.value = VerbTrainingState(config = _state.value.config)
    }

    fun startTraining() {
        viewModelScope.launch {
            val cfg = _state.value.config

            // Eligible verbs from the frequency bank, capped by maxTier.
            // Every verb here is guaranteed conjugable: either AUTHORED
            // (served from conjugations DB) or covered by the rules engine.
            val bankVerbs = com.spanishapp.domain.algorithm.SpanishVerbBank
                .verbsUpToTier(cfg.maxTier)
            val bankInfinitives = bankVerbs.map { it.infinitive.lowercase() }.toSet()

            val allAuthored = conjugationDao.getAll()
            val authoredByName = allAuthored.groupBy { it.verb.lowercase() }

            // 1) Authored rows for verbs directly in scope
            val direct = allAuthored.filter { it.verb.lowercase() in bankInfinitives }

            // 2) Compound rows — verbs like "mantener" derive from "tener" by
            //    prepending the prefix to every form of the parent's row.
            val compound = buildList {
                for (info in bankVerbs) {
                    if (info.kind != com.spanishapp.domain.algorithm.VerbKind.AUTHORED) continue
                    if (info.infinitive.lowercase() in authoredByName) continue  // already direct
                    val (prefix, parent) =
                        com.spanishapp.domain.algorithm.SpanishConjugator
                            .detectCompound(info.infinitive) ?: continue
                    val parentRows = authoredByName[parent] ?: continue
                    parentRows
                        .filter { it.tense in cfg.selectedTenses }
                        .forEach { p ->
                            add(p.copy(
                                id = 0,
                                verb = info.infinitive,
                                yo = prefix + p.yo,
                                tu = prefix + p.tu,
                                el = prefix + p.el,
                                nosotros = prefix + p.nosotros,
                                vosotros = prefix + p.vosotros,
                                ellos = prefix + p.ellos,
                            ))
                        }
                }
            }

            // 3) Generated rows for everything else in scope (rules engine)
            val knownKeys = (direct + compound).map { it.verb.lowercase() to it.tense }.toSet()
            val generated = buildList {
                for (info in bankVerbs) {
                    if (info.kind == com.spanishapp.domain.algorithm.VerbKind.AUTHORED) continue
                    for (tense in cfg.selectedTenses) {
                        if (info.infinitive.lowercase() to tense in knownKeys) continue
                        com.spanishapp.domain.algorithm.SpanishConjugator
                            .conjugate(info.infinitive, tense)
                            ?.let { add(it) }
                    }
                }
            }

            val all = direct + compound + generated

            // Фильтрация
            var pool = all.filter { it.tense in cfg.selectedTenses }
            pool = pool.filter { c ->
                val isReflex = c.verb.lowercase().endsWith("se")
                val passReflex = if (isReflex) cfg.includeReflexive else true
                val passGroup =
                    (c.isIrregular && VerbGroup.IRREGULAR in cfg.groups) ||
                    (!c.isIrregular && VerbGroup.REGULAR in cfg.groups)
                passReflex && passGroup
            }
            if (pool.isEmpty()) return@launch

            // Сборка вопросов
            val pickedConjs = pool.shuffled().take(cfg.questionCount.coerceAtMost(pool.size))
            val questions = pickedConjs.map { c ->
                // v1.25.78: Imperativo не имеет yo формы → исключаем pronounIndex=0
                val pIdx = if (c.tense == "imperativo") (1..5).random() else (0..5).random()
                var correct = formAt(c, pIdx)
                if (cfg.isVoseo && pIdx == 1 && (c.tense == "presente"))
                    correct = convertToVoseo(c.verb)

                when (cfg.mode) {
                    VerbTrainingMode.CONJUGAR -> VerbQuestion(
                        conjugation   = c,
                        pronounIndex  = pIdx,
                        correctAnswer = correct
                    )
                    VerbTrainingMode.INVERSO -> VerbQuestion(
                        conjugation   = c,
                        pronounIndex  = pIdx,
                        correctAnswer = correct,
                        pickedInfinitive = c.verb
                    )
                    VerbTrainingMode.HUECO -> VerbQuestion(
                        conjugation   = c,
                        pronounIndex  = pIdx,
                        correctAnswer = correct,
                        sentenceTemplate = templateFor(c.tense, pIdx, cfg.isVoseo)
                    )
                    VerbTrainingMode.AUDITIVO -> VerbQuestion(
                        conjugation   = c,
                        pronounIndex  = pIdx,
                        correctAnswer = correct,
                        ttsText       = correct
                    )
                }
            }

            _state.value = _state.value.copy(
                questions   = questions,
                currentIndex = 0,
                correctCount = 0,
                nearMissCount = 0,
                score        = 0,
                isGameOver   = false,
                showSetup    = false
            )

            // Озвучить первую форму если режим Auditivo
            if (cfg.mode == VerbTrainingMode.AUDITIVO) {
                tts.speak(questions.first().ttsText)
            }
        }
    }

    /** Повторить произнесение (для режима Auditivo). */
    fun replayAudio() {
        val q = _state.value.questions.getOrNull(_state.value.currentIndex) ?: return
        if (_state.value.config.mode == VerbTrainingMode.AUDITIVO && q.ttsText.isNotBlank()) {
            tts.speak(q.ttsText)
        }
    }

    /** Озвучить инфинитив (полезная подсказка в любом режиме). */
    fun playInfinitive() {
        val q = _state.value.questions.getOrNull(_state.value.currentIndex) ?: return
        tts.speak(q.conjugation.verb)
    }

    /**
     * Проверка ответа в режимах CONJUGAR / HUECO / AUDITIVO (текстовый ввод формы).
     */
    fun submitAnswer(input: String) {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.isChecked) return

        val u = input.trim().lowercase()
        val c = q.correctAnswer.lowercase()
        val (correct, near) = when {
            u == c                                      -> true to false
            s.config.acceptNoAccent &&
                stripAccents(u) == stripAccents(c)      -> true to true   // без акцентов
            else                                        -> false to false
        }

        // v1.25.78 VERB-2: генерируем explanation для показа после ошибки
        val explanation = if (!correct) explainVerb(q.conjugation.verb, q.conjugation.tense) else ""

        val updated = q.copy(
            userInput = input,
            isChecked = true,
            isCorrect = correct,
            nearMiss  = near,
            explanation = explanation,
        )
        val deltaScore = when {
            correct && !near -> 10
            correct && near  -> 5
            else             -> 0
        }

        replaceCurrent(updated)
        _state.value = _state.value.copy(
            correctCount  = s.correctCount  + (if (correct && !near) 1 else 0),
            nearMissCount = s.nearMissCount + (if (correct && near) 1 else 0),
            score         = s.score + deltaScore
        )
        viewModelScope.launch {
            ratingUpdater.applyGameAnswer(correct)
            // v1.25.78 VERB-4: запись/удаление weak verb
            recordVerbResult(q, correct && !near)
        }

        if (correct) tts.speak(q.correctAnswer)
    }

    /**
     * v1.25.78 VERB-4: трекинг weak verbs.
     *  - При ошибке: upsert с error_count++ (или новая запись)
     *  - При успехе: delete из пула если был
     */
    private suspend fun recordVerbResult(q: VerbQuestion, isCorrect: Boolean) {
        val key = "${q.conjugation.verb}|${q.conjugation.tense}|${q.pronounIndex}"
        if (isCorrect) {
            weakVerbDao.deleteByKey(key)
        } else {
            // v1.25.97 FIX (audit): сперва UPDATE error_count+1; INSERT только
            // если строки ещё не было. Раньше REPLACE каждый раз писал count=1 —
            // сортировка topWeak по error_count не работала.
            val ts = System.currentTimeMillis()
            val updated = weakVerbDao.bumpErrorCount(key, ts)
            if (updated == 0) {
                weakVerbDao.upsert(
                    WeakVerbEntity(
                        key = key,
                        verb = q.conjugation.verb,
                        tense = q.conjugation.tense,
                        pronounIndex = q.pronounIndex,
                        correctForm = q.correctAnswer,
                        errorCount = 1,
                        lastErrorAt = ts,
                    )
                )
            }
        }
    }

    /**
     * Проверка ответа в режиме INVERSO — пользователь выбрал инфинитив, время и лицо.
     */
    fun submitInverso(inf: String, tense: String, pronounIdx: Int) {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.isChecked) return

        val correct = inf == q.conjugation.verb &&
                      tense == q.conjugation.tense &&
                      pronounIdx == q.pronounIndex

        val updated = q.copy(
            isChecked = true,
            isCorrect = correct,
            pickedInf = inf,
            pickedTense = tense,
            pickedPronounIdx = pronounIdx
        )
        replaceCurrent(updated)
        _state.value = _state.value.copy(
            correctCount = s.correctCount + (if (correct) 1 else 0),
            score        = s.score + (if (correct) 10 else 0)
        )
        viewModelScope.launch {
            ratingUpdater.applyGameAnswer(correct)
            recordVerbResult(q, correct)  // v1.25.78 VERB-4
        }
        if (correct) tts.speak(q.correctAnswer)
    }

    fun nextQuestion() {
        val s = _state.value
        if (s.currentIndex + 1 >= s.questions.size) {
            finishTraining()
        } else {
            _state.value = s.copy(currentIndex = s.currentIndex + 1)
            // Авто-озвучка для Auditivo
            if (s.config.mode == VerbTrainingMode.AUDITIVO) {
                _state.value.questions.getOrNull(s.currentIndex + 1)?.let { tts.speak(it.ttsText) }
            }
        }
    }

    private fun finishTraining() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isGameOver = true)
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            userProgressDao.update(p.copy(totalXp = p.totalXp + (_state.value.score)))
            achievementManager.checkAndUnlock()
        }
    }

    fun getPronoun(index: Int): String =
        if (_state.value.config.isVoseo) pronounsVoseo[index] else pronouns[index]

    /** Доступные времена/глаголы для UI выбора в Inverso. */
    fun availableTenses(): List<String> = allTenses

    // ── Helpers ─────────────────────────────────────────────

    private fun replaceCurrent(q: VerbQuestion) {
        val list = _state.value.questions.toMutableList()
        list[_state.value.currentIndex] = q
        _state.value = _state.value.copy(questions = list)
    }

    private fun formAt(c: ConjugationEntity, idx: Int): String =
        listOf(c.yo, c.tu, c.el, c.nosotros, c.vosotros, c.ellos)[idx]

    /**
     * v1.25.78 VERB-2: explanation после ошибки в Conjugar/Hueco/Auditivo.
     * Берёт VerbKind глагола из SpanishVerbBank → переводит в человеческое
     * правило через SpanishConjugator.explainKind. Если глагол не в банке —
     * fallback на короткое сообщение про данное время.
     */
    private fun explainVerb(verb: String, tense: String): String {
        val info = com.spanishapp.domain.algorithm.SpanishVerbBank
            .byInfinitive[verb.trim().lowercase()]
        val kindExplain = info?.let {
            com.spanishapp.domain.algorithm.SpanishConjugator.explainKind(it.kind, tense)
        } ?: "Этот глагол спрягается по особому шаблону."
        val tenseHint = when (tense) {
            "presente" -> "Настоящее время (Presente)."
            "preterito" -> "Простое прошедшее (Pretérito Indefinido). Описывает завершённое действие."
            "imperfecto" -> "Имперфект — описывает фон, привычки в прошлом."
            "futuro" -> "Будущее простое (Futuro Simple)."
            "condicional" -> "Условное наклонение (Condicional)."
            "subjuntivo" -> "Subjuntivo Presente — используется после que в подчинённых предложениях, после глаголов желания/сомнения."
            "subjuntivo_imperfecto" -> "Subjuntivo Imperfecto — используется в Si type 2 (Si tuviera...), после Ojalá, como si."
            "imperativo" -> "Imperativo (повелительное) — приказ, просьба. yo формы нет."
            else -> ""
        }
        return if (tenseHint.isNotEmpty()) "$tenseHint\n\n$kindExplain" else kindExplain
    }

    private fun convertToVoseo(verb: String): String = when {
        verb.endsWith("ar") -> verb.dropLast(2) + "ás"
        verb.endsWith("er") -> verb.dropLast(2) + "és"
        verb.endsWith("ir") -> verb.dropLast(2) + "ís"
        else -> verb
    }

    private fun stripAccents(s: String): String =
        s.replace('á','a').replace('é','e').replace('í','i')
         .replace('ó','o').replace('ú','u').replace('ü','u')
         .replace('ñ','n')

    /**
     * Шаблон фразы для режима HUECO (с подстановкой местоимения и контекста).
     */
    private fun templateFor(tense: String, pIdx: Int, isVoseo: Boolean): String {
        val pron = (if (isVoseo) pronounsVoseo else pronouns)[pIdx]
        return when (tense) {
            "presente"    -> "$pron ___ todos los días."
            "preterito"   -> "Ayer $pron ___."
            "imperfecto"  -> "Antes $pron siempre ___."
            "futuro"      -> "Mañana $pron ___."
            "condicional" -> "Si pudiera, $pron ___."
            "subjuntivo"  -> "Espero que $pron ___."
            // v1.25.78: новые B2 времена
            "subjuntivo_imperfecto" -> "Si $pron ___, todo sería distinto."
            "imperativo"  -> when (pIdx) {
                1 -> "¡___ tú!"                  // tú
                2 -> "¡___ usted, por favor!"   // usted
                3 -> "¡___ nosotros juntos!"     // nosotros
                4 -> "¡___ vosotros ahora!"     // vosotros
                5 -> "¡___ ustedes, por favor!" // ustedes
                else -> "¡___!"                  // yo не существует в imperativo
            }
            else          -> "$pron ___."
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
