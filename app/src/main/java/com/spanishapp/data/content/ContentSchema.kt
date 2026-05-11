package com.spanishapp.data.content

import kotlinx.serialization.Serializable

/**
 * Wire format for content packs hosted on Firebase Storage.
 *
 * The manifest lives at `content/manifest.json`. Each pack is a separate
 * JSON file at `content/{pack.id}_v{pack.version}.json`. The downloader
 * compares manifest versions against locally-cached versions and pulls
 * only what changed.
 *
 * Keep these classes flat and primitive — they are the public contract
 * with the CDN and must stay back-compat-friendly. Add fields as nullable
 * defaults; never remove or rename.
 */

@Serializable
data class ContentManifest(
    val schemaVersion: Int = 1,
    val packs: List<PackInfo>
)

@Serializable
data class PackInfo(
    val id: String,            // "core" | "lessons_a1" | "libros_b2" | ...
    val version: Int,          // bumps when contents change
    val url: String,           // absolute URL to the pack JSON (or zip)
    val sizeBytes: Long,
    val sha256: String,        // integrity check after download
    val required: Boolean = true,   // false = optional/lazy pack
    val displayName: String = id    // shown in the download UI
)

// ── Pack payloads ─────────────────────────────────────────────

@Serializable
data class WordsPack(
    val words: List<WordRecord>
)

@Serializable
data class WordRecord(
    val es: String,
    val ru: String,
    val example: String = "",
    val level: String,         // A1 | A2 | B1 | B2
    val category: String = "general",
    val type: String = "noun"  // noun | verb | adj | phrase | ...
)

@Serializable
data class LessonsPack(
    val lessons: List<LessonRecord>
)

@Serializable
data class LessonRecord(
    val id: String,            // "u1_l0", "u5_l3" ...
    val intro: String,
    val sections: List<SectionRecord>,
    val exercises: List<ExerciseRecord> = emptyList()
)

@Serializable
data class SectionRecord(
    val heading: String,
    val items: List<ItemRecord>
)

@Serializable
data class ItemRecord(
    val left: String,
    val right: String,
    val note: String = ""
)

@Serializable
data class ExerciseRecord(
    val type: String,          // MULTIPLE_CHOICE | FILL_BLANK | ...
    val instruction: String,
    val question: String,
    val hint: String = "",
    val options: List<String> = emptyList(),
    val words: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String = ""
)

@Serializable
data class LibrosPack(
    val stories: List<StoryRecord>
)

@Serializable
data class StoryRecord(
    val id: Int,
    val level: String,
    val title: String,
    val theme: String = "",
    val text: String,
    val questions: List<QuestionRecord>
)

@Serializable
data class QuestionRecord(
    val q: String,
    val options: List<String>,
    val correctIndex: Int
)
