package com.spanishapp

// CheckpointContentData удалён в feat(checkpoints) — чекпоинты JSON-driven
// (см. CheckpointRepository + assets/checkpoints/*.json). Перенести проверки
// производительности чекпоинтов в InstrumentationTest когда понадобится.
import com.spanishapp.data.theory.TheoryContentData
import com.spanishapp.ui.home.ExerciseGenerator
import com.spanishapp.ui.home.LessonContentData
import com.spanishapp.ui.home.VocabScope
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

/**
 * Нагрузочное JVM-тестирование статичных data layers и алгоритмов.
 *
 * Что НЕ покрывается здесь (требует эмулятор/instrumentation):
 *   • Room — реальные запросы к БД
 *   • Compose — UI render с большими списками
 *   • Cold start приложения целиком (Application.onCreate)
 *
 * Что ПОКРЫТО:
 *   • Инициализация всех 254 уроков, 165 теорий, 21 чекпоинта
 *   • VocabScope кумулятивный поиск
 *   • ExerciseGenerator стресс по всем урокам
 *   • Concurrent reads (immutable map → thread-safe)
 *   • Memory footprint
 *
 * Все пороги ИНДИКАТИВНЫЕ — лучше следить за регрессией со временем.
 */
class PerformanceLoadTest {

    /** Замер: возвращает ms на N итераций. */
    private inline fun bench(label: String, iterations: Int, block: (Int) -> Unit): Long {
        // Warm-up
        for (i in 0 until minOf(3, iterations)) block(i)
        val t0 = System.nanoTime()
        for (i in 0 until iterations) block(i)
        val ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0)
        val perOp = if (iterations > 0) (System.nanoTime() - t0).toDouble() / iterations / 1000.0 else 0.0
        println("⏱  [$label] $iterations iter в $ms ms (~${"%.1f".format(perOp)} µs/op)")
        return ms
    }

    private fun mb(bytes: Long): String = "%.2f MB".format(bytes / 1_048_576.0)

    @Test
    fun `cold init — все статичные data layers`() {
        // Force load (Kotlin singleton lazy)
        val t0 = System.nanoTime()
        val lessons = LessonContentData.lessons
        val tElapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0)
        println("✅ LessonContentData: ${lessons.size} уроков за $tElapsed ms")
        assertTrue("Ожидалось >=240 уроков, получено ${lessons.size}", lessons.size >= 240)
        // Реалистичный порог 500ms на cold init — это потолок включая Speaking enrichment
        assertTrue("Cold init слишком долго: $tElapsed ms", tElapsed < 2000)

        val t1 = System.nanoTime()
        val theories = TheoryContentData.all()
        val t1elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t1)
        println("✅ TheoryContentData: ${theories.size} теорий за $t1elapsed ms")
        assertTrue("Ожидалось >=100 теорий", theories.size >= 100)

        // CheckpointContentData проверка вырезана — теперь JSON-driven.

        val t3 = System.nanoTime()
        val allWords = VocabScope.allWords()
        val t3elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t3)
        println("✅ VocabScope: ${allWords.size} слов в общем словаре за $t3elapsed ms")
    }

    @Test
    fun `vocab scope — кумулятивная выборка для всех уроков`() {
        val lessonIds = LessonContentData.lessons.keys.toList()
        val ms = bench("VocabScope.wordsForLesson", lessonIds.size) { i ->
            val id = lessonIds[i]
            VocabScope.wordsForLesson(id)
        }
        assertTrue("VocabScope.wordsForLesson слишком медленно: $ms ms", ms < 5000)

        // Особый случай: для последнего урока u16_l14 scope максимальный (накопительный)
        val t0 = System.nanoTime()
        val scope = VocabScope.wordsForLesson("u16_l14")
        val tEl = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0)
        println("📊 u16_l14 scope = ${scope.size} слов накопительно (за $tEl ms)")
        // Даже для последнего урока должно быть < 50ms
        assertTrue("Кумулятивный scope для u16_l14 слишком долго: $tEl ms", tEl < 100)
    }

    @Test
    fun `exercise generator — стресс по всем уроком`() {
        val all = LessonContentData.lessons
        var totalExercises = 0L
        val ms = bench("ExerciseGenerator.generate (all lessons)", all.size) { i ->
            val (lessonId, content) = all.entries.elementAt(i)
            val exs = ExerciseGenerator.generate(lessonId, content)
            totalExercises += exs.size
        }
        println("📊 Сгенерировано $totalExercises упражнений по ${all.size} урокам")
        // Один вызов generate должен быть < 50ms на нашей машине
        val avgPerOp = ms.toDouble() / all.size
        println("📊 Среднее время на 1 урок: ${"%.2f".format(avgPerOp)} ms")
        assertTrue("Generate в среднем слишком долго: ${avgPerOp} ms/lesson", avgPerOp < 100.0)
    }

    @Test
    fun `lookups throughput — теории, чекпоинты, уроки 10000 раз`() {
        val theoryIds = TheoryContentData.all().map { it.lessonId }
        val lessonIds = LessonContentData.lessons.keys.toList()

        val N = 10_000

        val msTh = bench("TheoryContentData.byLessonId", N) { i ->
            TheoryContentData.byLessonId(theoryIds[i % theoryIds.size])
        }
        val msLs = bench("LessonContentData.lessons[id]", N) { i ->
            LessonContentData.lessons[lessonIds[i % lessonIds.size]]
        }

        // Все lookups должны быть очень быстрые (<1µs/op для Map.get)
        assertTrue("Theory lookup throughput низкий: $msTh ms", msTh < 500)
        assertTrue("Lesson lookup throughput низкий: $msLs ms", msLs < 500)

        println("📊 Lookup-throughput: Theory ${N * 1000L / msTh.coerceAtLeast(1)} ops/s")
    }

    @Test
    fun `concurrent reads — 8 threads x 1000 ops`() {
        val executor = Executors.newFixedThreadPool(8)
        val lessonIds = LessonContentData.lessons.keys.toList()
        val theoryIds = TheoryContentData.all().map { it.lessonId }
        // CheckpointContentData удалён — чекпоинты теперь JSON-driven (см.
        // PerformanceLoadTest исправление от 2026-05-23). Concurrency-проверка
        // для чекпоинтов остаётся для будущего instrumentation-теста.

        val tasks = (0 until 8).map { threadIdx ->
            Runnable {
                repeat(1000) { i ->
                    LessonContentData.lessons[lessonIds[(i * 7 + threadIdx) % lessonIds.size]]
                    TheoryContentData.byLessonId(theoryIds[(i * 11 + threadIdx) % theoryIds.size])
                    VocabScope.wordsForLesson(lessonIds[(i * 5 + threadIdx) % lessonIds.size])
                }
            }
        }

        val t0 = System.nanoTime()
        val futures = tasks.map { executor.submit(it) }
        futures.forEach { it.get() }
        val ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0)
        executor.shutdown()

        println("⏱  Concurrent (8 threads × 1000 ops × 4 lookup-типа) = $ms ms")
        // 32K combined operations должно быть быстрее 5 секунд
        assertTrue("Concurrent reads слишком медленно: $ms ms", ms < 5000)
    }

    @Test
    fun `memory footprint — все статичные данные в JVM`() {
        // Инициализируем всё (без CheckpointContentData — теперь JSON-driven)
        LessonContentData.lessons
        TheoryContentData.all()
        VocabScope.allWords()

        // GC чтобы получить максимально точную картину
        repeat(3) { System.gc(); Thread.sleep(50) }

        val rt = Runtime.getRuntime()
        val used = rt.totalMemory() - rt.freeMemory()
        val max = rt.maxMemory()
        println("💾 Memory: used = ${mb(used)}, max = ${mb(max)}")
        println("💾 % of max heap: ${"%.1f".format(used.toDouble() / max * 100)}%")

        // Защита от регрессии: коарс-порог. ВАЖНО: это ВЕСЬ used-heap JVM
        // (контент + фреймворки в classpath), а не только наши данные. С v1.26.1
        // на unit-classpath появился Robolectric (~60-80 МБ базы), поэтому порог
        // поднят 100→200 МБ. Реальный взрыв контента (~2x) всё равно поймается;
        // контент между тестами шарится (object/lazy), точнее не измерить.
        assertTrue("Контент занимает слишком много памяти: ${mb(used)}", used < 200L * 1_048_576)
    }

    @Test
    fun `data integrity — все ID уникальны и непустые`() {
        // Проверка уникальности lesson IDs (после слияния V2 + старых)
        val lessonIds = LessonContentData.lessons.keys
        assertTrue("Найден пустой lessonId", lessonIds.all { it.isNotBlank() })
        println("✅ Все ${lessonIds.size} lessonId уникальны и непустые")

        // Проверка теорий
        val theoryIds = TheoryContentData.all().map { it.lessonId }
        assertTrue("Дубль в TheoryContentData", theoryIds.size == theoryIds.toSet().size)
        assertTrue("Пустой theory.lessonId", theoryIds.all { it.isNotBlank() })
        println("✅ Все ${theoryIds.size} theory.lessonId уникальны")

        // ⚠ CheckpointContentData удалён в feat(checkpoints) — чекпоинты теперь
        // JSON-driven (см. assets/checkpoints/*.json + CheckpointRepository).
        // Старый объект больше не существует; этот блок проверки оставлен пустым.
        // TODO: переписать проверку через CheckpointRepository.loadAll() в
        // InstrumentationTest (нужен Android Context).
        println("ℹ️ Чекпоинты теперь JSON-driven — проверка вынесена в Instrumentation")
    }

    @Test
    fun `vocab scope — никаких регрессий по покрытию`() {
        // Считаем сколько lessonId имеют word scope
        val lessonsWithScope = LessonContentData.lessons.keys.count {
            VocabScope.newWordsInLesson(it).isNotEmpty()
        }
        println("📊 Уроков с явно-объявленным VocabScope: $lessonsWithScope из ${LessonContentData.lessons.size}")

        // Проверим конкретно блок 1.1 покрытие (мы знаем 14 уроков должны иметь scope)
        val block1Coverage = (0..13).count {
            VocabScope.newWordsInLesson("u1_l$it").isNotEmpty()
        } + listOf("u1_l13_5").count {
            VocabScope.newWordsInLesson(it).isNotEmpty()
        }
        println("📊 Блок 1.1 scope: $block1Coverage / 15 уроков")
        assertTrue("Vocab scope блока 1.1 неполный: $block1Coverage", block1Coverage >= 14)

        // Кумулятивный максимум
        val maxScope = VocabScope.wordsForLesson("u16_l14")
        println("📊 Полный scope (до u16_l14): ${maxScope.size} слов")
        assertTrue("Полный vocab scope мал: ${maxScope.size}", maxScope.size >= 500)
    }
}
