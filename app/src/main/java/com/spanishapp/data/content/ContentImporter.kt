package com.spanishapp.data.content

import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Applies downloaded content packs to the local Room database.
 *
 * Industry pattern (Duolingo / Babbel style):
 *   • Words   — insertAll with IGNORE strategy. Existing words keep their SM-2
 *               progress intact; only genuinely new Spanish words are added.
 *               Deduplication mirrors DatabaseSeeder: filter by spanish.lowercase().
 *   • Lessons / Libros — content lives in compile-time Kotlin files for now;
 *               skip until a server-side content pipeline is built.
 *
 * This class is intentionally NOT @Singleton so ContentSyncWorker can
 * create it directly without Hilt (same pattern as RatingDecayWorker).
 */
class ContentImporter(private val wordDao: WordDao) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Dispatch by pack ID prefix:
     *   "words_a1", "words_a2", "words_b1", "words_b2", "core", "words_*" → importWords()
     *   anything else → no-op (future: lessons, libros, etc.)
     */
    suspend fun apply(pack: DownloadedPack) = withContext(Dispatchers.IO) {
        val id = pack.info.id.lowercase()
        when {
            id.startsWith("words") || id == "core" -> importWords(pack)
            // Future: id.startsWith("lessons") -> importLessons(pack)
            // Future: id.startsWith("libros")  -> importLibros(pack)
        }
    }

    // ── Words ────────────────────────────────────────────────────

    private suspend fun importWords(pack: DownloadedPack) {
        val data = runCatching {
            json.decodeFromString(WordsPack.serializer(), pack.file.readText())
        }.getOrElse { return }   // malformed JSON — skip silently, worker will retry

        // Deduplicate against existing DB — same logic as DatabaseSeeder.
        val existingSpanish = wordDao.getAllSpanishLower().toHashSet()

        val newEntities = data.words
            .filter { r -> r.es.trim().lowercase() !in existingSpanish }
            .map { r ->
                WordEntity(
                    spanish  = r.es.trim(),
                    russian  = r.ru.trim(),
                    example  = r.example,
                    level    = r.level,
                    category = r.category,
                    wordType = r.type
                )
            }

        if (newEntities.isNotEmpty()) {
            wordDao.insertAll(newEntities)
        }
    }
}
