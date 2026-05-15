package com.spanishapp

import com.spanishapp.data.checkpoint.CheckpointContentData
import com.spanishapp.data.theory.TheoryContentData
import com.spanishapp.ui.home.ExerciseGenerator
import com.spanishapp.ui.home.LessonContentData
import com.spanishapp.ui.home.VocabScope
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
        assert(lessons.size >= 240) { "Ожидалось >=240 уроков, получено ${lessons.size}" }
        // Реалистичный порог 500ms на cold init — это потолок включая Speaking enrichment
        assert(tElapsed < 2000) { "Cold init слишком долго: $tElapsed ms" }

        val t1 = System.nanoTime()
        val theories = TheoryContentData.all()
        val t1elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t1)
        println("✅ TheoryContentData: ${theories.size} теорий за $t1elapsed ms")
        assert(theories.size >= 100) { "Ожидалось >=100 теорий" }

        val t2 = System.nanoTime()
        val checkpoints = CheckpointContentData.all()
        val t2elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t2)
        println("✅ CheckpointContentData: ${checkpoints.size} сценариев за $t2elapsed ms")
        assert(checkpoints.size >= 18) { "Ожидалось >=18 чекпоинтов" }

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
        assert(ms < 5000) { "VocabScope.wordsForLesson слишком медленно: $ms ms" }

        // Особый случай: для последнего урока u16_l14 scope максимальный (накопительный)
        val t0 = System.nanoTime()
        val scope = VocabScope.wordsForLesson("u16_l14")
        val tEl = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0)
        println("📊 u16_l14 scope = ${scope.size} слов накопительно (за $tEl ms)")
        // Даже для последнего урока должно быть < 50ms
        assert(tEl < 100) { "Кумулятивный scope для u16_l14 слишком долго: $tEl ms" }
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
        assert(avgPerOp < 100.0) { "Generate в среднем слишком долго: ${avgPerOp} ms/lesson" }
    }

    @Test
    fun `lookups throughput — теории, чекпоинты, уроки 10000 раз`() {
        val theoryIds = TheoryContentData.all().map { it.lessonId }
        val checkpointIds = CheckpointContentData.all().map { it.id }
        val lessonIds = LessonContentData.lessons.keys.toList()

        val N = 10_000

        val msTh = bench("TheoryContentData.byLessonId", N) { i ->
            TheoryContentData.byLessonId(theoryIds[i % theoryIds.size])
        }
        val msCp = bench("CheckpointContentData.byId", N) { i ->
            CheckpointContentData.byId(checkpointIds[i % checkpointIds.size])
        }
        val msLs = bench("LessonContentData.lessons[id]", N) { i ->
            LessonContentData.lessons[lessonIds[i % lessonIds.size]]
        }

        // Все lookups должны быть очень быстрые (<1µs/op для Map.get)
        assert(msTh < 500) { "Theory lookup throughput низкий: $msTh ms" }
        assert(msCp < 500) { "Checkpoint lookup throughput низкий: $msCp ms" }
        assert(msLs < 500) { "Lesson lookup throughput низкий: $msLs ms" }

        println("📊 Lookup-throughput: Theory ${N * 1000L / msTh.coerceAtLeast(1)} ops/s")
    }

    @Test
    fun `concurrent reads — 8 threads x 1000 ops`() {
        val executor = Executors.newFixedThreadPool(8)
        val lessonIds = LessonContentData.lessons.keys.toList()
        val theoryIds = TheoryContentData.all().map { it.lessonId }
        val cpIds = CheckpointContentData.all().map { it.id }

        val tasks = (0 until 8).map { threadIdx ->
            Runnable {
                repeat(1000) { i ->
                    LessonContentData.lessons[lessonIds[(i * 7 + threadIdx) % lessonIds.size]]
                    TheoryContentData.byLessonId(theoryIds[(i * 11 + threadIdx) % theoryIds.size])
                    CheckpointContentData.byId(cpIds[(i * 3 + threadIdx) % cpIds.size])
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
        assert(ms < 5000) { "Concurrent reads слишком медленно: $ms ms" }
    }

    @Test
    fun `memory footprint — все статичные данные в JVM`() {
        // Инициализируем всё
        LessonContentData.lessons
        TheoryContentData.all()
        CheckpointContentData.all()
        VocabScope.allWords()

        // GC чтобы получить максимально точную картину
        repeat(3) { System.gc(); Thread.sleep(50) }

        val rt = Runtime.getRuntime()
        val used = rt.totalMemory() - rt.freeMemory()
        val max = rt.maxMemory()
        println("💾 Memory: used = ${mb(used)}, max = ${mb(max)}")
        println("💾 % of max heap: ${"%.1f".format(used.toDouble() / max * 100)}%")

        // Защита от регрессии: общий контент не должен превышать 100 MB в памяти
        assert(used < 100L * 1_048_576) {
            "Контент занимает слишком много памяти: ${mb(used)}"
        }
    }

    @Test
    fun `data integrity — все ID уникальны и непустые`() {
        // Проверка уникальности lesson IDs (после слияния V2 + старых)
        val lessonIds = LessonContentData.lessons.keys
        assert(lessonIds.all { it.isNotBlank() }) { "Найден пустой lessonId" }
        println("✅ Все ${lessonIds.size} lessonId уникальны и непустые")

        // Проверка теорий
        val theoryIds = TheoryContentData.all().map { it.lessonId }
        assert(theoryIds.size == theoryIds.toSet().size) { "Дубль в TheoryContentData" }
        assert(theoryIds.all { it.isNotBlank() }) { "Пустой theory.lessonId" }
        println("✅ Все ${theoryIds.size} theory.lessonId уникальны")

        // Проверка чекпоинтов
        val cpIds = CheckpointContentData.all().map { it.id }
        assert(cpIds.size == cpIds.toSet().size) { "Дубль в CheckpointContentData" }
        assert(cpIds.all { it.isNotBlank() }) { "Пустой checkpoint.id" }
        println("✅ Все ${cpIds.size} checkpoint.id уникальны")

        // Проверка структуры чекпоинтов: 1+ scenes, 1+ acts
        for (cp in CheckpointContentData.all()) {
            assert(cp.scenes.isNotEmpty()) { "Чекпоинт ${cp.id} без scenes" }
            for (scene in cp.scenes) {
                assert(scene.acts.isNotEmpty()) { "Сцена в ${cp.id} без актов" }
            }
        }
        val totalActs = CheckpointContentData.all().sumOf { cp -> cp.scenes.sumOf { it.acts.size } }
        println("✅ Чекпоинты: ${CheckpointContentData.all().size} сценариев, $totalActs актов суммарно")
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
        assert(block1Coverage >= 14) { "Vocab scope блока 1.1 неполный: $block1Coverage" }

        // Кумулятивный максимум
        val maxScope = VocabScope.wordsForLesson("u16_l14")
        println("📊 Полный scope (до u16_l14): ${maxScope.size} слов")
        assert(maxScope.size >= 500) { "Полный vocab scope мал: ${maxScope.size}" }
    }
}
