package com.spanishapp.domain.voice

/**
 * Маппинг NPC чекпоинтов → конкретный Google TTS голос.
 *
 * v1.22.20: разные NPC говорят разными голосами для иммерсии.
 * Мужские персонажи — Carlos/Pablo voices. Женские — Lucía/Sofía.
 * Боссам модулей (Director Ramón, Tía Rosa) — особые тембры.
 *
 * Все voice id из [PremiumVoiceCatalog.ES_VOICES] — гарантированно
 * доступны в Cloudflare TTS proxy.
 */
object NpcVoiceMap {

    /**
     * Голос для конкретного NPC. Если NPC не размечен — возвращает
     * null → SpanishTts использует выбранный юзером в настройках.
     */
    fun voiceFor(npcId: String): String? = byNpc[npcId]

    private val byNpc: Map<String, String> = mapOf(
        // ── Мужчины ───────────────────────────────────────────
        "carlos"         to PremiumVoiceCatalog.ES_POLYGLOT_1,   // премиум, строгий офицер
        "diego"          to PremiumVoiceCatalog.ES_NEURAL2_B,    // молодой официант
        "sergio"         to PremiumVoiceCatalog.ES_NEURAL2_B,    // молодой друг
        "pablo"          to PremiumVoiceCatalog.ES_POLYGLOT_1,   // HR — формальный
        "andres"         to PremiumVoiceCatalog.ES_NEURAL2_B,    // парень со свидания
        "hans"           to PremiumVoiceCatalog.ES_NEURAL2_B,    // турист (молодой)
        "director_ramon" to PremiumVoiceCatalog.ES_POLYGLOT_1,   // директор — глубокий
        "ensemble"       to PremiumVoiceCatalog.ES_POLYGLOT_1,   // финал — Carlos за всех

        // ── Женщины ───────────────────────────────────────────
        "sra_lopez"      to PremiumVoiceCatalog.ES_WAVENET_C,    // хозяйка — мягкая, чуть ниже
        "dra_martinez"   to PremiumVoiceCatalog.ES_WAVENET_C,    // врач — спокойная
        "carmen"         to PremiumVoiceCatalog.ES_NEURAL2_D,    // продавщица — живая
        "carmen_rec"     to PremiumVoiceCatalog.ES_NEURAL2_D,    // ресепшен (та же)
        "lucia"          to PremiumVoiceCatalog.ES_NEURAL2_D,    // подруга — энергичная
        "marta"          to PremiumVoiceCatalog.ES_NEURAL2_D,    // киноманка — бодрая
        "ana"            to PremiumVoiceCatalog.ES_WAVENET_C,    // HR — профессиональная
        "tia_rosa"       to PremiumVoiceCatalog.ES_WAVENET_C,    // тётя — драматичная, мягкая
    )
}
