package com.spanishapp

import com.spanishapp.data.content.ContentManifest
import com.spanishapp.data.content.ExerciseRecord
import com.spanishapp.data.content.ItemRecord
import com.spanishapp.data.content.LessonRecord
import com.spanishapp.data.content.LessonsPack
import com.spanishapp.data.content.LibrosPack
import com.spanishapp.data.content.PackInfo
import com.spanishapp.data.content.QuestionRecord
import com.spanishapp.data.content.SectionRecord
import com.spanishapp.data.content.StoryRecord
import com.spanishapp.data.content.WordRecord
import com.spanishapp.data.content.WordsPack
import com.spanishapp.data.db.BasicsVocab
import com.spanishapp.data.db.CleanVocab
import com.spanishapp.data.db.VocabExtra1
import com.spanishapp.data.db.VocabExtra2
import com.spanishapp.data.db.VocabExtra3
import com.spanishapp.data.db.VocabExtra4
import com.spanishapp.data.db.VocabExtra5
import com.spanishapp.data.db.VocabExtra6
import com.spanishapp.data.db.VocabExtra7
import com.spanishapp.data.db.VocabExtra8
import com.spanishapp.data.db.VocabExtra9
import com.spanishapp.data.db.VocabExtra10
import com.spanishapp.data.db.VocabExtra11
import com.spanishapp.data.db.VocabExtra12
import com.spanishapp.ui.games.LibrosData
import com.spanishapp.ui.home.Exercise
import com.spanishapp.ui.home.LessonContentData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/**
 * One-shot exporter: dumps the in-app content (BasicsVocab, LessonContentData,
 * LibrosData) into versioned JSON packs under `docs/content_packs/`.
 *
 * Re-run after content edits:
 *   ./gradlew :app:testDebugUnitTest --tests "com.spanishapp.ContentPackExporter"
 *
 * Not run by default in regular CI — only on demand. Output is checked in so
 * upload to Firebase Storage is a separate explicit step.
 */
class ContentPackExporter {

    private val packsDir = File("../docs/content_packs").also { it.mkdirs() }
    private val json = Json {
        prettyPrint = true
        encodeDefaults = false
    }

    // ── Pack versions (bump when content changes) ─────────────────
    private val CORE_V       = 1
    private val LESSONS_V    = 1
    private val LIBROS_V     = 1

    @Test
    fun exportAll() {
        val infos = mutableListOf<PackInfo>()

        // ── 1. Core vocabulary — full union of every vocab source
        //    (same logic as DatabaseSeeder.kt). Deduplicate by Spanish lemma
        //    so the pack matches what the app actually stores. ──
        val all = CleanVocab.entries +
            VocabExtra1.entries + VocabExtra2.entries + VocabExtra3.entries +
            VocabExtra4.entries + VocabExtra5.entries + VocabExtra6.entries +
            VocabExtra7.entries + VocabExtra8.entries + VocabExtra9.entries +
            VocabExtra10.entries + VocabExtra11.entries + VocabExtra12.entries +
            BasicsVocab.entries

        val words = all
            .distinctBy { it.spanish.trim().lowercase() }
            .map { w ->
                WordRecord(
                    es = w.spanish,
                    ru = w.russian,
                    example = w.example,
                    level = w.level,
                    category = w.category,
                    type = w.wordType,
                )
            }
        infos += writePack(
            id = "core",
            version = CORE_V,
            displayName = "Базовый словарь",
            payload = WordsPack(words),
            serializer = WordsPack.serializer(),
        )

        // ── 2. Lessons split by CEFR level ──
        for (level in listOf("A1", "A2", "B1", "B2")) {
            val lessons = lessonsForLevel(level)
            if (lessons.isEmpty()) continue
            infos += writePack(
                id = "lessons_${level.lowercase()}",
                version = LESSONS_V,
                displayName = "Уроки $level",
                payload = LessonsPack(lessons),
                serializer = LessonsPack.serializer(),
            )
        }

        // ── 3. Libros split by level ──
        for (level in listOf("A1", "A2", "B1", "B2")) {
            val stories = LibrosData.all
                .filter { it.level == level }
                .map { l ->
                    StoryRecord(
                        id = l.id,
                        level = l.level,
                        title = l.title,
                        theme = l.topic,
                        text = l.text,
                        questions = l.questions.map { q ->
                            QuestionRecord(q.question, q.options, q.correctIndex)
                        },
                    )
                }
            if (stories.isEmpty()) continue
            infos += writePack(
                id = "libros_${level.lowercase()}",
                version = LIBROS_V,
                displayName = "Рассказы $level",
                payload = LibrosPack(stories),
                serializer = LibrosPack.serializer(),
            )
        }

        // ── 4. Manifest ──
        val manifest = ContentManifest(schemaVersion = 1, packs = infos)
        val manifestFile = File(packsDir, "manifest.json")
        manifestFile.writeText(json.encodeToString(ContentManifest.serializer(), manifest))
        println("Wrote ${manifestFile.absolutePath}")
        println("Total packs: ${infos.size}")
        println("Total size: ${infos.sumOf { it.sizeBytes }} bytes")
    }

    // ── Helpers ───────────────────────────────────────────────────

    private fun lessonsForLevel(level: String): List<LessonRecord> {
        // Lesson ids encode the unit number: "u1_l0".
        // Units 1–4 = A1, 5–8 = A2, 9–12 = B1, 13–16 = B2, 17–22 = additional
        val unitsInLevel = when (level) {
            "A1" -> 1..4
            "A2" -> 5..8
            "B1" -> 9..12
            "B2" -> 13..22
            else -> return emptyList()
        }
        return LessonContentData.lessons
            .filter { (id, _) ->
                val unit = unitFrom(id) ?: return@filter false
                unit in unitsInLevel
            }
            .map { (id, content) ->
                LessonRecord(
                    id = id,
                    intro = content.intro,
                    sections = content.sections.map { s ->
                        SectionRecord(
                            heading = s.heading,
                            items = s.items.map { it_ ->
                                ItemRecord(it_.left, it_.right, it_.note)
                            },
                        )
                    },
                    exercises = content.exercises.map { e ->
                        ExerciseRecord(
                            type = e.type.name,
                            instruction = e.instruction,
                            question = e.question,
                            hint = e.hint,
                            options = e.options,
                            words = e.words,
                            correctAnswer = e.correctAnswer,
                            explanation = e.explanation,
                        )
                    },
                )
            }
            .sortedBy { it.id }
    }

    private fun unitFrom(lessonId: String): Int? {
        val m = Regex("""^u(\d+)_l\d+$""").find(lessonId) ?: return null
        return m.groupValues[1].toInt()
    }

    private inline fun <reified T> writePack(
        id: String,
        version: Int,
        displayName: String,
        payload: T,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): PackInfo {
        val filename = "${id}_v${version}.json"
        val file = File(packsDir, filename)
        val text = json.encodeToString(serializer, payload)
        file.writeText(text)
        val bytes = text.toByteArray(Charsets.UTF_8)
        val sha = sha256(bytes)
        println("Wrote ${file.absolutePath}  ${bytes.size} bytes  sha256=${sha.take(12)}…")
        return PackInfo(
            id = id,
            version = version,
            url = "content/$filename",          // relative — resolved against bucket root
            sizeBytes = bytes.size.toLong(),
            sha256 = sha,
            required = true,
            displayName = displayName,
        )
    }

    private fun sha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
