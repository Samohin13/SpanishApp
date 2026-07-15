package com.spanishapp

import com.spanishapp.ui.games.FraseLocaContent
import com.spanishapp.ui.games.FraseLocaEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Frase Loca: движок + целостность авторского банка (20 тем × 12 фраз).
 */
class FraseLocaEngineTest {

    // ── Структура банка ──────────────────────────────────────

    @Test
    fun `bank has exactly 20 themes of 12 phrases each`() {
        assertEquals(20, FraseLocaContent.themes.size)
        FraseLocaContent.themes.forEach { theme ->
            assertEquals("theme ${theme.id}", 12, theme.phrases.size)
        }
    }

    @Test
    fun `theme ids are unique`() {
        val ids = FraseLocaContent.themes.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `every phrase is well-formed`() {
        FraseLocaContent.themes.forEach { theme ->
            theme.phrases.forEach { p ->
                assertTrue("ru blank in ${theme.id}", p.ru.isNotBlank())
                assertTrue("tokens<2: '${p.sentence}'", p.tokens.size >= 2)
                assertTrue(
                    "blank token in '${p.sentence}'",
                    p.tokens.none { it.isBlank() }
                )
                assertTrue("too many traps in '${p.sentence}'", p.traps.size <= 3)
            }
        }
    }

    @Test
    fun `traps never collide with real tokens (case-insensitive)`() {
        FraseLocaContent.themes.forEach { theme ->
            theme.phrases.forEach { p ->
                val tokensLower = p.tokens.map { it.lowercase() }.toSet()
                p.traps.forEach { trap ->
                    assertTrue("blank trap in '${p.sentence}'", trap.word.isNotBlank())
                    assertTrue(
                        "blank explanation for trap '${trap.word}' in '${p.sentence}'",
                        trap.explanation.isNotBlank()
                    )
                    assertTrue(
                        "trap '${trap.word}' collides with token in '${p.sentence}' (${theme.id})",
                        trap.word.lowercase() !in tokensLower
                    )
                }
            }
        }
    }

    @Test
    fun `trap words are single tiles without spaces`() {
        FraseLocaContent.themes.forEach { theme ->
            theme.phrases.forEach { p ->
                p.traps.forEach { trap ->
                    assertTrue(
                        "multi-word trap '${trap.word}' in '${p.sentence}'",
                        !trap.word.trim().contains(' ')
                    )
                }
            }
        }
    }

    @Test
    fun `themes 3+ have traps for levels 11plus`() {
        // Темы с 3-й (уровни 11+) должны нести хотя бы 1 ловушку в
        // большинстве фраз — иначе ярусы с trapLimit>0 играют «вхолостую».
        FraseLocaContent.themes.drop(2).forEach { theme ->
            val withTraps = theme.phrases.count { it.traps.isNotEmpty() }
            assertTrue(
                "theme ${theme.id}: only $withTraps/12 phrases have traps",
                withTraps >= 10
            )
        }
    }

    // ── Движок ───────────────────────────────────────────────

    @Test
    fun `themeForLevel maps 5 levels per theme`() {
        assertEquals(FraseLocaContent.themes[0].id, FraseLocaEngine.themeForLevel(1).id)
        assertEquals(FraseLocaContent.themes[0].id, FraseLocaEngine.themeForLevel(5).id)
        assertEquals(FraseLocaContent.themes[1].id, FraseLocaEngine.themeForLevel(6).id)
        assertEquals(FraseLocaContent.themes[10].id, FraseLocaEngine.themeForLevel(51).id)
        assertEquals(FraseLocaContent.themes[19].id, FraseLocaEngine.themeForLevel(100).id)
    }

    @Test
    fun `trapLimit grows with level`() {
        assertEquals(0, FraseLocaEngine.trapLimitForLevel(1))
        assertEquals(0, FraseLocaEngine.trapLimitForLevel(10))
        assertEquals(1, FraseLocaEngine.trapLimitForLevel(11))
        assertEquals(1, FraseLocaEngine.trapLimitForLevel(25))
        assertEquals(2, FraseLocaEngine.trapLimitForLevel(26))
        assertEquals(2, FraseLocaEngine.trapLimitForLevel(75))
        assertEquals(3, FraseLocaEngine.trapLimitForLevel(76))
        assertEquals(3, FraseLocaEngine.trapLimitForLevel(100))
    }

    @Test
    fun `phrasesForLevel returns roundsForLevel phrases inside theme pool`() {
        for (level in 1..100) {
            val phrases = FraseLocaEngine.phrasesForLevel(level)
            val theme = FraseLocaEngine.themeForLevel(level)
            assertEquals(
                "level $level",
                FraseLocaEngine.roundsForLevel(level),
                phrases.size
            )
            phrases.forEach { p ->
                assertTrue("phrase not from theme at level $level", p in theme.phrases)
            }
        }
    }

    @Test
    fun `first and last level of a theme use different windows`() {
        val first = FraseLocaEngine.phrasesForLevel(11) // тема 3, уровень 1 из 5
        val last = FraseLocaEngine.phrasesForLevel(15)  // тема 3, уровень 5 из 5
        assertTrue("windows should differ", first != last)
    }

    @Test
    fun `adjacent levels never share a phrase`() {
        // v1.27.1, фидбэк владельца: «последние 2 фразы переходят в начало
        // следующего уровня» — соседние уровни не должны делить фразы.
        for (level in 1..99) {
            val sameTheme =
                (level - 1) / FraseLocaEngine.LEVELS_PER_THEME ==
                    level / FraseLocaEngine.LEVELS_PER_THEME
            if (!sameTheme) continue
            val a = FraseLocaEngine.phrasesForLevel(level).map { it.sentence }.toSet()
            val b = FraseLocaEngine.phrasesForLevel(level + 1).map { it.sentence }.toSet()
            assertTrue(
                "levels $level and ${level + 1} share phrases: ${a intersect b}",
                (a intersect b).isEmpty()
            )
        }
    }

    @Test
    fun `no duplicate phrases inside one level`() {
        for (level in 1..100) {
            val sentences = FraseLocaEngine.phrasesForLevel(level).map { it.sentence }
            assertEquals("duplicates at level $level", sentences.size, sentences.distinct().size)
        }
    }

    @Test
    fun `activeTraps respects level limit`() {
        for (level in listOf(1, 10)) {
            FraseLocaEngine.phrasesForLevel(level).forEach { p ->
                assertEquals(0, FraseLocaEngine.activeTraps(p, level).size)
            }
        }
        for (level in listOf(11, 30, 80)) {
            val limit = FraseLocaEngine.trapLimitForLevel(level)
            FraseLocaEngine.phrasesForLevel(level).forEach { p ->
                assertTrue(FraseLocaEngine.activeTraps(p, level).size <= limit)
            }
        }
    }

    @Test
    fun `tilesFor is deterministic and contains all tokens plus traps`() {
        for (level in listOf(1, 26, 77)) {
            FraseLocaEngine.phrasesForLevel(level).forEachIndexed { round, p ->
                val tiles1 = FraseLocaEngine.tilesFor(p, level, round)
                val tiles2 = FraseLocaEngine.tilesFor(p, level, round)
                assertEquals("non-deterministic tiles", tiles1, tiles2)

                val traps = FraseLocaEngine.activeTraps(p, level)
                assertEquals(p.tokens.size + traps.size, tiles1.size)
                p.tokens.forEach { t ->
                    assertTrue("token '$t' missing from tiles", t in tiles1)
                }
                traps.forEach { trap ->
                    assertTrue("trap '${trap.word}' missing from tiles", trap.word in tiles1)
                }
            }
        }
    }

    @Test
    fun `sentence join produces readable spanish`() {
        val phrase = FraseLocaContent.themes[5].phrases
            .first { it.ru.startsWith("Вчера я купил") }
        assertEquals("Ayer compré un coche rojo.", phrase.sentence)
    }
}
