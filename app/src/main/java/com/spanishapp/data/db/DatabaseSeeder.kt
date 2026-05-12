package com.spanishapp.data.db

import android.content.Context
import com.spanishapp.data.db.entity.*
import com.spanishapp.data.repository.ConjugationData
import com.spanishapp.data.repository.ConjugationData2
import com.spanishapp.data.repository.ConjugationData3
import com.spanishapp.service.AchievementManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val achievementManager: AchievementManager
) {
    companion object {
        // Порог для досева — CleanVocab + расширения (целимся в 10 000).
        const val VOCAB_TARGET = 10000

        // ── Полностью неправильные глаголы ────────────────────
        val IRREGULAR_VERBS = setOf(
            "ser","estar","ir","haber","ver","dar","saber",
            "tener","poder","querer","poner","venir","decir","hacer","traer",
            "salir","caer","caber","valer","oír","reír","freír","asir",
            "obtener","mantener","contener","retener","detener","sostener",
            "componer","proponer","exponer","disponer","oponer","suponer","imponer","reponer",
            "contradecir","predecir","bendecir","maldecir",
            "construir","destruir","incluir","excluir","contribuir","distribuir",
            "disminuir","influir","constituir","sustituir","atribuir","concluir","huir","instruir",
            "satisfacer","deshacer","rehacer","contraer","distraer","extraer","abstraer","atraer",
            "proveer","leer","creer","sobresalir","intervenir","convenir","prevenir","provenir",
            "entretener","abstraerse","distraerse"
        )

        // ── Глаголы с изменением корня (отклоняющиеся) ────────
        val STEM_VERBS = setOf(
            // e→ie
            "entender","perder","encender","defender","extender",
            "sentir","preferir","mentir","convertir","divertir","sugerir","requerir",
            "advertir","herir","consentir","referir","hervir","invertir",
            "pensar","empezar","comenzar","cerrar","calentar","despertar",
            "recomendar","atravesar","confesar","negar","sentar","regar","sembrar",
            "enterrar","gobernar","plegar","apretar","tropezar","nevar",
            // o→ue
            "dormir","volver","encontrar","contar","recordar","costar","mostrar",
            "mover","resolver","devolver","llover","soler","probar","volar","rogar",
            "oler","morder","envolver","revolver","apostar","almorzar","colgar",
            "demostrar","consolar","comprobar","renovar","torcer","absolver",
            // e→i
            "pedir","repetir","seguir","servir","elegir","conseguir","perseguir",
            "vestir","medir","sonreír","corregir","competir","impedir","gemir",
            "rendir","teñir","ceñir","fregar",
            // u→ue
            "jugar"
        )
    }

    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        seedWords()
        seedConjugations()
        seedUserProgress()
        seedAchievements()
        seedLessons()
        seedDailyWord()
        seedDialogues()
        seedArticleGameProgress()
    }

    private suspend fun seedArticleGameProgress() {
        val dao = db.articleGameDao()
        val existing = dao.getAllProgress().firstOrNull() ?: emptyList()
        if (existing.isNotEmpty()) return
        
        val levels = listOf("A1", "A2", "B1", "B2", "C1").map { level ->
            ArticleLevelProgressEntity(
                levelId = level,
                stars = 0,
                isUnlocked = level == "A1",
                bestScore = 0
            )
        }
        levels.forEach { dao.upsertProgress(it) }
    }

    // ── Vocabulary: единый источник CleanVocab (дедуплицированный) ─────
    // Использует IGNORE-стратегию вставки — безопасно вызывать повторно.
    private suspend fun seedWords() {
        // Гард VOCAB_TARGET убран: insert ниже идемпотентный (existingSet +
        // OnConflictStrategy.IGNORE), безопасно запускать на каждом старте.
        // Это нужно потому что:
        //  1) BasicsVocab/Extras могут расширяться между билдами
        //  2) Android Auto-Backup мог восстановить устаревшую БД из облака —
        //     даже после uninstall+reinstall (исправлено через exclude в
        //     backup_rules.xml + data_extraction_rules.xml для новых юзеров,
        //     но старые юзеры всё ещё с устаревшим бэкапом).

        // Дедуплицированный набор: CleanVocab + расширения
        val all = CleanVocab.entries + VocabExtra1.entries + VocabExtra2.entries + VocabExtra3.entries + VocabExtra4.entries + VocabExtra5.entries + VocabExtra6.entries + VocabExtra7.entries + VocabExtra8.entries + VocabExtra9.entries + VocabExtra10.entries + VocabExtra11.entries + VocabExtra12.entries + BasicsVocab.entries
        val unique = all.distinctBy { it.spanish.trim().lowercase() }

        // Пометить неправильные и отклоняющиеся глаголы
        val marked = unique.map { word ->
            if (word.wordType == "verb") {
                when (word.spanish.trim().lowercase()) {
                    in IRREGULAR_VERBS -> word.copy(verbSubtype = "irregular")
                    in STEM_VERBS      -> word.copy(verbSubtype = "stem")
                    else               -> word
                }
            } else word
        }

        // Отфильтровать слова, которые уже есть в БД (по нижнему регистру)
        val existingSet = db.wordDao().getAllSpanishLower().toHashSet()
        val newOnly = marked.filter { it.spanish.trim().lowercase() !in existingSet }

        if (newOnly.isNotEmpty()) db.wordDao().insertAll(newOnly)

        // ── Translation patches (idempotent UPDATE) ───────────────
        // Applied on every launch to fix incorrect translations that slipped
        // into the DB via the old IGNORE-strategy inserts. Safe to add more.
        val patches = listOf(
            "Adiós"    to "До свидания",   // was "Пока / До свидания" — confused with "Hasta luego"
            "Perdón"   to "Простите",       // was "Извините" — that's Disculpe
            "Disculpe" to "Извините"        // was "Простите" — roles were swapped
        )
        patches.forEach { (es, ru) -> db.wordDao().patchRussian(es, ru) }
    }

    // ── Conjugation tables ────────────────────────────────────
    private suspend fun seedConjugations() {
        if (db.conjugationDao().getCount() > 0) return
        db.conjugationDao().insertAll(ConjugationData.getAll() + ConjugationData2.getAll() + ConjugationData3.getAll())
    }

    // ── Default user profile ──────────────────────────────────
    private suspend fun seedUserProgress() {
        val existing = db.userProgressDao().getProgressOnce()
        if (existing != null) return
        db.userProgressDao().insert(
            UserProgressEntity(
                displayName      = "Estudiante",
                dailyGoalMinutes = 10,
                currentLevel     = "A1"
            )
        )
    }

    // ── Achievements ──────────────────────────────────────────
    private suspend fun seedAchievements() {
        if (db.achievementDao().getCount() > 0) return
        db.achievementDao().insertAll(achievementManager.defaultAchievements)
    }

    // ── Grammar lessons ───────────────────────────────────────
    // Idempotent: insertAll uses OnConflictStrategy.IGNORE, so existing rows
    // (and their isCompleted flags) are preserved while any newly-added lesson
    // IDs from GrammarContent are appended on every app launch.
    private suspend fun seedLessons() {
        db.lessonDao().insertAll(GrammarContent.getAll())
    }

    // ── Dialogues ─────────────────────────────────────────────
    private suspend fun seedDialogues() {
        if (db.dialogueDao().getCount() > 0) return
        db.dialogueDao().insertAll(DialogueContent.getAll())
    }

    // ── Word of the day ───────────────────────────────────────
    private suspend fun seedDailyWord() {
        val today = LocalDate.now().toString()
        if (db.dailyWordDao().getForDate(today) != null) return

        val a1Words = db.wordDao().getA1WordIds()
        if (a1Words.isEmpty()) return
        val dayOfYear = LocalDate.now().dayOfYear
        val wordId = a1Words[dayOfYear % a1Words.size]
        db.dailyWordDao().upsert(DailyWordEntity(date = today, wordId = wordId))
    }
}
