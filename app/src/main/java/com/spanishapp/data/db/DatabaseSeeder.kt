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
import org.json.JSONObject
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
        // Порог для досева. Реальное уникальное кол-во слов ~4000.
        // Если в БД меньше этого — запустить досев.
        const val VOCAB_TARGET = 3800

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
        if (existing.size >= 100) return
        
        val levels = (1..100).map { id ->
            ArticleLevelProgressEntity(
                levelId = id,
                stars = 0,
                isUnlocked = id == 1,
                bestScore = 0
            )
        }
        levels.forEach { dao.upsertProgress(it) }
    }

    // ── Vocabulary from assets/spanish_vocab.json ────────────
    // Использует IGNORE-стратегию вставки — безопасно вызывать повторно.
    // Досевает слова если в БД меньше чем VOCAB_TARGET.
    private suspend fun seedWords() {
        val current = db.wordDao().getCount()
        if (current >= VOCAB_TARGET) return

        val json = context.assets.open("spanish_vocab.json")
            .bufferedReader().readText()
        val root = JSONObject(json)
        val words = mutableListOf<WordEntity>()

        fun parseSection(key: String, type: String) {
            if (!root.has(key)) return
            val arr = root.getJSONArray(key)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                words += WordEntity(
                    spanish  = obj.getString("spanish"),
                    russian  = obj.getString("russian"),
                    example  = obj.optString("example", ""),
                    level    = obj.optString("level", "A1"),
                    category = obj.optString("category", "general"),
                    wordType = type
                )
            }
        }

        parseSection("nouns",        "noun")
        parseSection("verbs",        "verb")
        parseSection("adjectives",   "adjective")
        parseSection("phrases",      "phrase")
        parseSection("adverbs",      "adverb")
        parseSection("prepositions", "preposition")

        words += ModernVocab.entries
        words += ExtendedVocab.entries
        words += VocabExpansion1.entries
        words += VocabExpansion2.entries
        words += VocabExpansion3.entries

        // Дедупликация по испанскому слову (сохраняем первое вхождение)
        val unique = words.distinctBy { it.spanish.trim().lowercase() }

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
    private suspend fun seedLessons() {
        if (db.lessonDao().getCount() > 0) return
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
