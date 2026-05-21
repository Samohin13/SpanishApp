package com.spanishapp.games

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * Тесты целостности ассетов игры «Артикли».
 *
 * Проверяет articles_levels.json и word_images PNG/WebP файлы:
 *   • JSON корректно парсится
 *   • 100 уровней × 10 слов
 *   • Артикль валиден (el/la/los/las)
 *   • is_plural согласован с артиклем
 *   • Для каждого слова есть картинка PNG или WebP
 *   • Имена файлов соответствуют stripAccents(word.lowercase())
 *
 * Тест читает реальные файлы из app/src/main/assets/ — это unit-тест уровня
 * data-integrity, а не Android instrumentation. Запуск:
 *   ./gradlew :app:testDebugUnitTest --tests "com.spanishapp.games.ArticlesAssetsTest"
 */
class ArticlesAssetsTest {

    private val assetsDir = File("src/main/assets")
    private val levelsJson = File(assetsDir, "articles_levels.json")
    private val imagesDir = File(assetsDir, "word_images")

    private val validArticles = setOf("el", "la", "los", "las")
    private val pngHeader = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    private val riffHeader = byteArrayOf(0x52, 0x49, 0x46, 0x46)  // "RIFF"

    /** Тот же алгоритм что в ArticlesGameScreen.stripAccents. */
    private fun stripAccents(s: String): String {
        val n = Normalizer.normalize(s, Normalizer.Form.NFD)
        return n.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    private fun loadLevels(): JSONArray {
        assertTrue("Файл $levelsJson должен существовать", levelsJson.exists())
        return JSONArray(levelsJson.readText(Charsets.UTF_8))
    }

    private fun isValidImage(file: File): Boolean {
        if (!file.exists() || file.length() < 500) return false
        val head = file.inputStream().use { it.readNBytes(12) }
        if (head.size < 12) return false
        // PNG
        if (head.copyOfRange(0, 8).contentEquals(pngHeader)) return true
        // WebP: RIFF....WEBP
        if (head.copyOfRange(0, 4).contentEquals(riffHeader) &&
            head.copyOfRange(8, 12).toString(Charsets.US_ASCII) == "WEBP") return true
        return false
    }

    // ────────────────────────────────────────────────────────────

    @Test
    fun `json existed and parsed`() {
        val arr = loadLevels()
        assertEquals("Должно быть 100 уровней", 100, arr.length())
    }

    @Test
    fun `every level has 10 words`() {
        val arr = loadLevels()
        for (i in 0 until arr.length()) {
            val lvl = arr.getJSONObject(i)
            val num = lvl.getInt("level")
            val words = lvl.getJSONArray("words")
            assertEquals("Level $num должен иметь 10 слов", 10, words.length())
        }
    }

    @Test
    fun `level numbers are 1 to 100 unique`() {
        val arr = loadLevels()
        val nums = (0 until arr.length()).map { arr.getJSONObject(it).getInt("level") }
        assertEquals("100 уникальных номеров уровней", 100, nums.toSet().size)
        assertEquals(1, nums.min())
        assertEquals(100, nums.max())
    }

    @Test
    fun `every word has required fields`() {
        val arr = loadLevels()
        for (i in 0 until arr.length()) {
            val lvl = arr.getJSONObject(i)
            val words = lvl.getJSONArray("words")
            for (j in 0 until words.length()) {
                val w = words.getJSONObject(j)
                val lvlNum = lvl.getInt("level")
                assertTrue("Level $lvlNum pos $j: word",   w.optString("word").isNotBlank())
                assertTrue("Level $lvlNum pos $j: article",w.optString("article").isNotBlank())
                assertTrue("Level $lvlNum pos $j: russian",w.optString("russian").isNotBlank())
            }
        }
    }

    @Test
    fun `every article is valid el la los las`() {
        val arr = loadLevels()
        for (i in 0 until arr.length()) {
            val lvl = arr.getJSONObject(i)
            val words = lvl.getJSONArray("words")
            for (j in 0 until words.length()) {
                val w = words.getJSONObject(j)
                val article = w.getString("article")
                assertTrue(
                    "Level ${lvl.getInt("level")} pos $j: '$article' не в {el,la,los,las}",
                    article in validArticles
                )
            }
        }
    }

    @Test
    fun `is_plural agrees with article`() {
        val arr = loadLevels()
        for (i in 0 until arr.length()) {
            val lvl = arr.getJSONObject(i)
            val words = lvl.getJSONArray("words")
            for (j in 0 until words.length()) {
                val w = words.getJSONObject(j)
                val article = w.getString("article")
                val isPlural = w.optBoolean("is_plural", false)
                if (isPlural) {
                    assertTrue(
                        "Level ${lvl.getInt("level")} pos $j: is_plural=true но article=$article",
                        article == "los" || article == "las"
                    )
                } else {
                    assertTrue(
                        "Level ${lvl.getInt("level")} pos $j: is_plural=false но article=$article",
                        article == "el" || article == "la"
                    )
                }
            }
        }
    }

    @Test
    fun `every word has corresponding image file`() {
        val arr = loadLevels()
        val missing = mutableListOf<String>()
        val invalid = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        for (i in 0 until arr.length()) {
            val lvl = arr.getJSONObject(i)
            val words = lvl.getJSONArray("words")
            for (j in 0 until words.length()) {
                val word = words.getJSONObject(j).getString("word")
                val filename = stripAccents(word.lowercase())
                if (filename in seen) continue
                seen += filename
                val file = File(imagesDir, "$filename.png")
                if (!file.exists()) {
                    missing += "$word ($filename.png)"
                } else if (!isValidImage(file)) {
                    invalid += "$word ($filename.png)"
                }
            }
        }

        assertTrue(
            "Не хватает картинок (${missing.size}): ${missing.take(10)}",
            missing.isEmpty()
        )
        assertTrue(
            "Невалидные картинки (${invalid.size}): ${invalid.take(10)}",
            invalid.isEmpty()
        )
    }

    @Test
    fun `image files are reasonably sized after compression`() {
        val arr = loadLevels()
        val tooSmall = mutableListOf<String>()
        val tooBig = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        for (i in 0 until arr.length()) {
            val lvl = arr.getJSONObject(i)
            val words = lvl.getJSONArray("words")
            for (j in 0 until words.length()) {
                val word = words.getJSONObject(j).getString("word")
                val filename = stripAccents(word.lowercase())
                if (filename in seen) continue
                seen += filename
                val file = File(imagesDir, "$filename.png")
                if (!file.exists()) continue
                val size = file.length()
                if (size < 1024) tooSmall += "$filename=${size}B"
                if (size > 500 * 1024) tooBig += "$filename=${size / 1024}KB"
            }
        }

        // После compress_word_images.py все картинки должны быть < 500 KB
        assertTrue(
            "Слишком большие картинки (${tooBig.size}): ${tooBig.take(5)} — запусти compress_word_images.py",
            tooBig.isEmpty()
        )
        // Слишком маленькие = битые
        assertTrue(
            "Слишком маленькие/битые картинки (${tooSmall.size}): ${tooSmall.take(5)}",
            tooSmall.isEmpty()
        )
    }
}
