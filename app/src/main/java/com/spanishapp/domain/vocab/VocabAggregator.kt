package com.spanishapp.domain.vocab

import com.spanishapp.data.db.entity.UserVocabStateEntity
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.tanh

/**
 * v1.25.28 — pure-function алгоритм агрегации словарного запаса.
 *
 * Берёт сигналы из нескольких источников и сводит к ОДНОМУ
 * UserVocabStateEntity с computed score и status.
 *
 * См. docs/VOCAB_TRACKING_PLAN.md (формула + рекомендации).
 *
 * Алгоритм НЕ async — это чистая математика. async — в Worker'е
 * который читает источники.
 */
object VocabAggregator {

    /** Статусы знания слова — ordered weak → strong. */
    enum class Status {
        UNKNOWN,    // никогда не встречал
        SEEN,       // видел в уроке/чтении, но не закреплено
        LEARNING,   // в SM-2 пуле, EF растёт
        PRODUCING,  // использует сам в чате
        MASTERED,   // EF≥2.5 + usage≥10
    }

    /** Входной сигнал для одного слова. */
    data class Signals(
        val word: String,                  // lowercase
        val wordId: Int? = null,
        val cefr: String? = null,          // A1/A2/B1/B2/null
        /** SM-2 EF (1.3 — 3.0+). 0 если не во флэшкартах. */
        val sm2EaseFactor: Float = 0f,
        /** SM-2 repetitions count (сколько раз повторено успешно). */
        val sm2Repetitions: Int = 0,
        /** WordEntity.totalReviews — всего рев. */
        val totalReviews: Int = 0,
        /** WordEntity.correctReviews — успешных рев. */
        val correctReviews: Int = 0,
        /** WordEntity.isLearned (final marker SM-2). */
        val isLearned: Boolean = false,
        /** Сколько раз юзер сам написал в чате. */
        val chatUsageCount: Int = 0,
        /** Сколько раз AI поправил это слово. */
        val correctionsCount: Int = 0,
        /** Виделось в уроке (lesson content). */
        val seenInLesson: Boolean = false,
        /** Виделось в Libros (passive reading). */
        val seenInLibro: Boolean = false,
        /** Когда последний раз контакт со словом (any source). */
        val lastSeenAt: Long = 0L,
    )

    /**
     * Главная функция: input signals → output entity.
     *
     * Если все сигналы пусты → null (нечего записывать в БД).
     */
    fun aggregate(signals: Signals, now: Long = System.currentTimeMillis()): UserVocabStateEntity? {
        val score = computeScore(signals, now)
        val status = computeStatus(signals, score)
        // Не пишем UNKNOWN — нет смысла загромождать БД словами которые
        // юзер вообще не встречал.
        if (status == Status.UNKNOWN) return null

        return UserVocabStateEntity(
            word = signals.word,
            wordId = signals.wordId,
            cefr = signals.cefr,
            status = status.name,
            score = score,
            usageCount = signals.chatUsageCount,
            correctionsCount = signals.correctionsCount,
            flashcardEf = signals.sm2EaseFactor,
            lastSeenAt = signals.lastSeenAt,
            updatedAt = now,
        )
    }

    /**
     * Score formula (0.0 — 1.0):
     *   0.30 * (EF/2.5)              — насколько твёрдо помнит (SM-2)
     * + 0.35 * tanh(usage/5)         — насколько активно использует (вес повышен —
     *                                  без SM-2 одно усиленное использование тоже
     *                                  должно давать PRODUCING)
     * + 0.20 * tanh(lessonExp/3)     — сколько встречал в пассивных источниках
     * + 0.10 * recencyFactor         — насколько недавно (exp decay 30 дней)
     * - 0.10 * (corrections/max(usage,1))  — штраф за ошибки
     *
     * Возвращаем coerceIn(0,1).
     */
    internal fun computeScore(s: Signals, now: Long): Float {
        // 1. SM-2 strength (0..1). EF range практически 1.3-3.0, нормализуем к 2.5
        val efNormalized = if (s.sm2EaseFactor > 0f) {
            (s.sm2EaseFactor / 2.5f).coerceIn(0f, 1.2f)  // лучшие слова могут чуть >1
        } else 0f
        val efComponent = 0.30f * efNormalized

        // 2. Active usage (chat) — вес 0.35
        val usageComponent = if (s.chatUsageCount > 0) {
            0.35f * tanh(s.chatUsageCount / 5f)
        } else 0f

        // 3. Lesson exposure — пассивные источники. seenInLesson || seenInLibro.
        //   Тут используем totalReviews + бинарные флаги.
        val passiveExp = (if (s.seenInLesson) 1 else 0) +
                        (if (s.seenInLibro) 1 else 0) +
                        (s.totalReviews / 2)  // каждая 2-я ревизия добавляет к экспозиции
        val passiveComponent = 0.20f * tanh(passiveExp / 3f)

        // 4. Recency — exp decay по времени с lastSeenAt (30 дней половина).
        val recencyComponent = if (s.lastSeenAt > 0L) {
            val daysSinceSeen = (now - s.lastSeenAt) / DAY_MS
            // exp(-x/30): через 30 дней = 0.37, через 60 = 0.14, через 90 = 0.05
            val decay = exp(-daysSinceSeen.toDouble() / 30.0).toFloat()
            0.10f * decay
        } else 0f

        // 5. Penalty за ошибки (corrections / max(usage, 1))
        val errorRate = if (s.chatUsageCount > 0) {
            s.correctionsCount.toFloat() / max(s.chatUsageCount, 1)
        } else 0f
        val penaltyComponent = -0.10f * errorRate.coerceIn(0f, 1f)

        val raw = efComponent + usageComponent + passiveComponent + recencyComponent + penaltyComponent
        return raw.coerceIn(0f, 1f)
    }

    /**
     * Status decision tree.
     *
     * Иерархия (top → bottom):
     *  - MASTERED: EF≥2.5, isLearned, и (usage≥10 ИЛИ correctReviews≥8)
     *  - PRODUCING: usage≥3 (юзер активно использует) и score≥0.45
     *  - LEARNING: в SM-2 пуле (totalReviews>0) или score≥0.30
     *  - SEEN: видел где-то (lesson/libro) или score>0
     *  - UNKNOWN: ничего из выше
     */
    internal fun computeStatus(s: Signals, score: Float): Status {
        val masteredByFlashcards = s.isLearned && s.sm2EaseFactor >= 2.5f &&
            (s.chatUsageCount >= 10 || s.correctReviews >= 8)
        val masteredByUsage = s.chatUsageCount >= 15 && score >= 0.80f
        if (masteredByFlashcards || masteredByUsage) return Status.MASTERED

        if (s.chatUsageCount >= 3 && score >= 0.45f) return Status.PRODUCING

        if (s.totalReviews > 0 || score >= 0.30f) return Status.LEARNING

        if (s.seenInLesson || s.seenInLibro || score > 0f) return Status.SEEN

        return Status.UNKNOWN
    }

    private const val DAY_MS = 24L * 60 * 60 * 1000
}
