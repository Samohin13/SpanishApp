package com.spanishapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Integrity check for the 4 locales (ru, en, uk, es).
 *
 * Catches the three classes of bug that production users hit but
 * compile + lint silently let through:
 *   1. Missing translation — key exists in default ru/strings.xml but
 *      not in en/uk/es. Falls back to Russian for non-Russian users.
 *   2. Mismatched format specifiers — e.g. ru has «%1$s», en has «%s».
 *      Crashes at runtime with IllegalFormatException when Android
 *      tries to substitute a parameter.
 *   3. Empty/whitespace value — locale defines the key but with no text.
 *      Renders blank in UI.
 *
 * Run via:  ./gradlew :app:testDebugUnitTest --tests LocalizationIntegrityTest
 */
class LocalizationIntegrityTest {

    private data class StringEntry(val name: String, val value: String, val translatable: Boolean)

    /** Parse a strings.xml into a map name → entry. */
    private fun parse(path: String): Map<String, StringEntry> {
        val text = File(path).readText()
        // Match <string name="x" [translatable="false"]>VALUE</string> across lines.
        // Multiline mode handles values that contain literal \n line breaks.
        val regex = Regex(
            pattern = """<string\s+name="([^"]+)"([^>]*)>([\s\S]*?)</string>""",
            options = setOf(RegexOption.MULTILINE)
        )
        val out = mutableMapOf<String, StringEntry>()
        for (m in regex.findAll(text)) {
            val name = m.groupValues[1]
            val attrs = m.groupValues[2]
            val raw = m.groupValues[3]
            val translatable = !attrs.contains("""translatable="false"""")
            out[name] = StringEntry(name, raw, translatable)
        }
        return out
    }

    /** Project-relative paths. Works whether tests run from project root or app/. */
    private fun stringsPath(locale: String): String {
        val candidates = listOf(
            "app/src/main/res/$locale/strings.xml",
            "src/main/res/$locale/strings.xml",
        )
        return candidates.first { File(it).exists() }
    }

    private val ru by lazy { parse(stringsPath("values")) }
    private val en by lazy { parse(stringsPath("values-en")) }
    private val uk by lazy { parse(stringsPath("values-uk")) }
    private val es by lazy { parse(stringsPath("values-es")) }

    private val locales: Map<String, Map<String, StringEntry>>
        get() = mapOf("en" to en, "uk" to uk, "es" to es)

    // ─────────────────────────────────────────────────────────────────
    // 1. Missing translations: every translatable key in ru must exist
    //    in every other locale.
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun every_translatable_ru_key_exists_in_all_other_locales() {
        val translatableRuKeys = ru.values.filter { it.translatable }.map { it.name }.toSet()
        val problems = mutableListOf<String>()

        for ((locale, map) in locales) {
            val missing = translatableRuKeys - map.keys
            if (missing.isNotEmpty()) {
                problems += "[$locale] missing ${missing.size} keys: ${missing.sorted().joinToString(", ")}"
            }
        }

        if (problems.isNotEmpty()) {
            fail("Localization gaps detected:\n" + problems.joinToString("\n"))
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. No locale should have keys that don't exist in the default
    //    ru locale — those are dead translations, code can't reach them.
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun no_orphan_keys_in_other_locales() {
        val ruKeys = ru.keys
        val problems = mutableListOf<String>()

        for ((locale, map) in locales) {
            val orphans = map.keys - ruKeys
            if (orphans.isNotEmpty()) {
                problems += "[$locale] has ${orphans.size} orphan keys (not in ru): ${orphans.sorted().joinToString(", ")}"
            }
        }

        if (problems.isNotEmpty()) {
            fail("Orphan translations detected:\n" + problems.joinToString("\n"))
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. Format specifiers must match across locales for the same key.
    //    Otherwise String.format() crashes when Android tries to substitute
    //    arguments (e.g. ru="%1$d дней", en="N days" → MissingFormatArgument).
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun format_specifiers_match_across_locales() {
        // Captures patterns like %1$s, %1$d, %2$s, plain %s, plain %d.
        // Sorted set of normalized specifiers must be equal for the same key.
        val specRegex = Regex("""%(?:\d+\$)?[sdfox]""")

        val problems = mutableListOf<String>()
        for ((name, ruEntry) in ru) {
            if (!ruEntry.translatable) continue
            val ruSpecs = specRegex.findAll(ruEntry.value).map { it.value }.toList().sorted()

            for ((locale, map) in locales) {
                val other = map[name] ?: continue   // missing already reported above
                val otherSpecs = specRegex.findAll(other.value).map { it.value }.toList().sorted()
                if (ruSpecs != otherSpecs) {
                    problems += "[$locale.$name] ru=$ruSpecs vs ${locale}=$otherSpecs"
                }
            }
        }

        if (problems.isNotEmpty()) {
            fail("Format-specifier mismatches (will crash at runtime):\n" +
                problems.joinToString("\n"))
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 4. No value should be blank/whitespace-only — that renders empty
    //    text in UI, which looks like a broken translation.
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun no_blank_string_values() {
        val problems = mutableListOf<String>()
        val allLocales = mapOf("ru" to ru, "en" to en, "uk" to uk, "es" to es)
        for ((locale, map) in allLocales) {
            for ((name, entry) in map) {
                if (entry.value.isBlank()) {
                    problems += "[$locale.$name] blank"
                }
            }
        }
        if (problems.isNotEmpty()) {
            fail("Blank string values:\n" + problems.joinToString("\n"))
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 5. Sanity: each locale should have a reasonable number of keys
    //    (catches accidental file truncation / wholesale deletion).
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun each_locale_has_minimum_string_count() {
        val minimum = 600  // we shipped ~810 after Wave 3
        assertTrue("ru has only ${ru.size} keys, expected ≥ $minimum", ru.size >= minimum)
        assertTrue("en has only ${en.size} keys, expected ≥ $minimum", en.size >= minimum)
        assertTrue("uk has only ${uk.size} keys, expected ≥ $minimum", uk.size >= minimum)
        assertTrue("es has only ${es.size} keys, expected ≥ $minimum", es.size >= minimum)
    }

    // ─────────────────────────────────────────────────────────────────
    // 6. The 4 keys we deliberately don't translate (URLs, OAuth client
    //    ID, app name) should be marked translatable="false" in default
    //    ru — otherwise other locales need them too and the missing-key
    //    test would (rightly) fail.
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun untranslatable_technical_keys_are_marked_correctly() {
        val technicalKeys = setOf(
            "default_web_client_id",
            "privacy_policy_url",
            "terms_url",
        )
        for (key in technicalKeys) {
            val entry = ru[key]
            assertTrue("Missing key $key in default ru locale", entry != null)
            assertTrue(
                "Key '$key' should be translatable=\"false\" in ru/strings.xml",
                entry!!.translatable.not()
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 7. Wave-1 critical-path keys must be present in all locales.
    //    These cover the first-session flow; if any is missing,
    //    a non-Russian user will see Russian text.
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun wave1_auth_keys_translated_in_all_locales() {
        val wave1Keys = listOf(
            "auth_login_title", "auth_register_title", "auth_forgot_title",
            "auth_onboarding_name_title", "auth_onboarding_age_title",
            "auth_onboarding_reason_title", "auth_onboarding_level_title",
            "auth_onboarding_next", "auth_level_select_title",
            "auth_level_select_confirm", "auth_back",
            "auth_register_passwords_mismatch", "auth_register_passwords_match",
            "auth_placement_start",
        )
        assertAllPresent(wave1Keys, "Wave 1 auth")
    }

    @Test
    fun wave2_home_practice_flashcards_keys_translated() {
        val wave2Keys = listOf(
            "home_continue_label", "home_tile_lesson_title",
            "bento_label_book", "bento_label_rating", "bento_label_dictionary",
            "bento_label_goal", "bento_goal_lesson",
            "practice_title", "practice_correct_next", "practice_check",
            "flashcards_title", "flashcards_action_know",
            "flashcards_action_forgot", "flashcards_tap_to_check",
            "weekly_league_title", "weekly_league_join",
            "level_complete_perfect", "level_complete_next",
        )
        assertAllPresent(wave2Keys, "Wave 2 home/practice/flashcards")
    }

    @Test
    fun wave3_profile_verb_lesson_keys_translated() {
        val wave3Keys = listOf(
            "profile_title", "profile_section_activity",
            "profile_section_path_madrid", "profile_section_stats",
            "profile_section_achievements", "rating_dialog_title",
            "leaderboard_tab_week",
            "verb_section_level", "verb_level_1", "verb_level_5",
            "exercise_type_choice", "exercise_type_listen_pick",
            "exercise_type_match_pairs", "exercise_type_conjugation",
        )
        assertAllPresent(wave3Keys, "Wave 3 profile/verb/lesson")
    }

    @Test
    fun support_keys_translated() {
        val supportKeys = listOf(
            "settings_section_support",
            "settings_support_title",
            "settings_support_summary",
        )
        assertAllPresent(supportKeys, "Boosty support card")
    }

    private fun assertAllPresent(keys: List<String>, group: String) {
        val problems = mutableListOf<String>()
        for (key in keys) {
            for ((locale, map) in mapOf("ru" to ru) + locales) {
                if (map[key] == null) {
                    problems += "[$locale] missing $key"
                } else if (map[key]!!.value.isBlank()) {
                    problems += "[$locale.$key] blank"
                }
            }
        }
        if (problems.isNotEmpty()) {
            fail("$group keys broken:\n" + problems.joinToString("\n"))
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 8. Locale balance — en/uk/es should be within a reasonable
    //    range of ru's key count. If es has only half of ru's keys,
    //    something is wrong.
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun locale_key_counts_are_balanced() {
        val ruCount = ru.size
        val tolerance = 10  // allow up to 10 missing keys (URLs etc.)
        for ((locale, map) in locales) {
            val diff = ruCount - map.size
            assertTrue(
                "[$locale] has ${map.size} keys, ru has $ruCount (diff=$diff > tolerance=$tolerance)",
                diff <= tolerance
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 9. Same key, same logical type (template vs literal). If ru ends
    //    with "..." but en doesn't, that's stylistic and fine. But if ru
    //    contains "%1$s" placeholder and the translation has no "%", the
    //    runtime call to getString(R.id, arg) silently drops the arg.
    //    This is partly covered by test 3, but here we check specifically
    //    for "ru has placeholder, locale doesn't" asymmetry.
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun placeholder_strings_keep_placeholders_in_all_locales() {
        val problems = mutableListOf<String>()
        for ((name, ruEntry) in ru) {
            if (!ruEntry.translatable) continue
            val ruHasPlaceholder = ruEntry.value.contains("%")
            if (!ruHasPlaceholder) continue
            for ((locale, map) in locales) {
                val other = map[name] ?: continue
                if (!other.value.contains("%")) {
                    problems += "[$locale.$name] ru has placeholder but locale doesn't: '${other.value}'"
                }
            }
        }
        if (problems.isNotEmpty()) {
            fail("Lost placeholders (silently breaks String.format):\n" +
                problems.joinToString("\n"))
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 10. Print summary on success. Useful to see at-a-glance how many
    //     keys exist in each locale.
    // ─────────────────────────────────────────────────────────────────
    @Test
    fun summary() {
        println("─".repeat(50))
        println("Localization summary:")
        println("  ru (default) : ${ru.size} keys")
        println("  en           : ${en.size} keys")
        println("  uk           : ${uk.size} keys")
        println("  es           : ${es.size} keys")
        println("─".repeat(50))
        // Always passes — diagnostic only.
        assertEquals(true, true)
    }
}
