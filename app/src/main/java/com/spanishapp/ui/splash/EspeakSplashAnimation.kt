package com.spanishapp.ui.splash

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Кинематографичный splash приложения ESPEAK.
 *
 * Сценарий (~10 сек):
 *  1. **Hold (0–0.55s)**: сетка из ~1600 мелких испанских букв пульсирует
 *     цветом и периодически меняет символ. Среди них — 14 «особых» в
 *     разных углах экрана. Юзер не выделяет их визуально.
 *  2. **Fall (0.55–7s)**: буквы начинают падать с задержками. Часть из
 *     них помечены как «filler» — у каждой кластер из ~6 слотов внутри
 *     ESPEAK. Долетев до слота, filler «рассыпается» в пиксели слова.
 *     Остальные буквы пролетают мимо вниз и исчезают.
 *  3. **Reveal (7–7.45s)**: когда мозаика слова собрана, пиксели гаснут
 *     и сверху плавно проявляется ГЛАДКИЙ шрифт ESPEAK.
 *  4. **Converge (7.95–8.75s)**: 14 особых букв летят в центр верха экрана.
 *  5. **BAM (8.75–9.5s)**: белая радиальная вспышка + расширяющееся
 *     кольцо → проявляется силуэт быка (логотип бренда).
 *
 * Tap по экрану — мгновенный fast-forward к финалу.
 *
 * Реализация: `Canvas` + `drawIntoCanvas { nativeCanvas.drawText(...) }`
 * для производительности на ~1600 текстовых элементов в кадре.
 */

private const val BULL_PATH = "M30.882,30.14c0,0 33.525,-10.81 92.088,19.64c60.996,31.72 85.628,116.77 132.906,117.86c47.283,-1.09 71.92,-86.14 132.912,-117.858c58.558,-30.45 92.088,-19.643 92.088,-19.643v85.483s-38.062,-2.453 -58.934,13.507c-15.165,11.593 -45.23,54.296 -71.375,80.08c38.867,27.833 63.966,71.877 63.966,121.45c0,84.162 -72.343,152.39 -161.587,152.39S91.353,414.821 91.353,330.659c0,-51.03 26.6,-96.205 67.432,-123.865c-25.558,-25.957 -54.263,-66.43 -68.965,-77.67c-20.877,-15.96 -58.938,-13.506 -58.938,-13.506zM179.45,330.49c0,40.01 32.98,72.44 73.664,72.44s73.67,-32.435 73.67,-72.44s-32.98,-72.44 -73.67,-72.44c-40.688,0 -73.664,32.436 -73.664,72.44"

private val CHARS = "ESPAÑOLñáéíóúü¿¡ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

private fun randChar(): Char = CHARS[Random.nextInt(CHARS.size)]
private fun ease(k: Float): Float = if (k < 0.5f) 2f * k * k else 1f - (-2f * k + 2f).let { it * it } / 2f

private const val HOLD_SEC = 0.55f
private const val FALL_DUR = 6.5f             // максимальная длительность падения
private const val WORD_REVEAL_DUR = 0.45f
private const val CONV_DELAY = 0.5f
private const val CONV_DUR = 0.8f
private const val BAM_FLASH_DUR = 0.25f
private const val BAM_RING_DUR = 0.5f
private const val BAM_LOGO_DUR = 0.6f
private const val TOTAL_DUR = 10.0f

/** Слот-пиксель внутри гладкого ESPEAK. Заполняется когда filler-буква долетает. */
private data class WordSlot(val x: Float, val y: Float, var filled: Boolean)

/** Одна буква пула. Большинство — «extra» (падает мимо), некоторые — «filler»
 *  (имеют кластер слотов и при падении заполняют их), 14 — «special»
 *  (не падают, в конце летят в центр для «бам»-эффекта). */
private class Letter(
    val initX: Float,
    val initY: Float,
    var x: Float,
    var y: Float,
    var ch: Char,
    var phase: Float,
    var role: Role,
    var speed: Float,
    var fallDelay: Float,
    var settled: Boolean = false,
    var gone: Boolean = false,
    var anchorX: Float = 0f,    // для special — куда лететь после конверги
    var anchorY: Float = 0f,
    var slot: WordSlot? = null, // для filler — финальная позиция (нижний слот кластера)
    var cluster: List<WordSlot> = emptyList(),
) {
    enum class Role { EXTRA, FILLER, SPECIAL }
}

private class SplashState(
    val width: Float,
    val height: Float,
) {
    val glyphSize: Float = width * 0.0357f      // ~20 на 560
    val wordFontSize: Float = width * 0.232f    // ~130 на 560
    val slotSize: Float = width * 0.0107f       // ~6
    val pixSize: Float = width * 0.0125f        // ~7
    val wordCenterY: Float = height * 0.72f
    val bullCenterY: Float = height * 0.30f

    val pool: MutableList<Letter>
    val specials: MutableList<Letter>
    val slots: MutableList<WordSlot>

    var fillDoneAt: Float = -1f
    var boomAt: Float = -1f
    var settledCount: Int = 0
    var totalFillers: Int = 0

    init {
        // ── Pool: сетка букв на весь экран ──
        val cols = (width / glyphSize).toInt() + 1
        val rows = (height / glyphSize).toInt() + 1
        val list = ArrayList<Letter>(cols * rows)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = c * glyphSize + glyphSize / 2f
                val cy = r * glyphSize + glyphSize / 2f
                list.add(
                    Letter(
                        initX = cx, initY = cy, x = cx, y = cy,
                        ch = randChar(),
                        phase = Random.nextFloat() * 6f,
                        role = Letter.Role.EXTRA,
                        speed = 300f + Random.nextFloat() * 240f,
                        fallDelay = Random.nextFloat() * 0.7f,
                    )
                )
            }
        }
        pool = list

        // ── Slots: пиксели внутри отрендеренного «ESPEAK» ──
        slots = computeWordSlots()

        // ── Specials: ~14 букв в разных зонах экрана + якоря на быке ──
        specials = ArrayList()
        val zones = listOf(
            0.12f to 0.08f, 0.88f to 0.12f, 0.50f to 0.05f,
            0.08f to 0.30f, 0.92f to 0.34f, 0.28f to 0.20f,
            0.72f to 0.22f, 0.06f to 0.55f, 0.94f to 0.60f,
            0.20f to 0.66f, 0.80f to 0.70f, 0.40f to 0.86f,
            0.62f to 0.90f, 0.14f to 0.92f,
        )
        val bullAnchors = computeBullAnchors(0.60f, width / 2f, bullCenterY, 30).shuffled().take(zones.size)
        val taken = HashSet<Letter>()
        zones.forEachIndexed { idx, (zx, zy) ->
            val targetX = width * zx
            val targetY = height * zy
            // Берём ближайшую extra-букву к точке зоны
            var best: Letter? = null
            var bestDist = Float.MAX_VALUE
            for (p in pool) {
                if (p in taken || p.role != Letter.Role.EXTRA) continue
                val d = abs(p.x - targetX) + abs(p.y - targetY)
                if (d < bestDist) { bestDist = d; best = p }
            }
            best?.let { letter ->
                taken.add(letter)
                letter.role = Letter.Role.SPECIAL
                val a = bullAnchors.getOrNull(idx)
                letter.anchorX = a?.first ?: width / 2f
                letter.anchorY = a?.second ?: bullCenterY
                specials.add(letter)
            }
        }

        // ── Fillers: каждой летящей букве — кластер из нескольких слотов ──
        val free = pool.filter { it.role == Letter.Role.EXTRA }.shuffled().toMutableList()
        val targetFillerCount = min(free.size, 220)
        val clusterSize = max(1, (slots.size + targetFillerCount - 1) / targetFillerCount)
        // Сортируем слоты снизу-вверх, чтобы слово «росло» с фундамента
        slots.sortByDescending { it.y }
        var si = 0
        var fi = 0
        while (si < slots.size && fi < free.size) {
            val letter = free[fi++]
            letter.role = Letter.Role.FILLER
            val cluster = ArrayList<WordSlot>(clusterSize)
            var k = 0
            while (k < clusterSize && si < slots.size) {
                cluster.add(slots[si++])
                k++
            }
            letter.cluster = cluster
            letter.slot = cluster.lastOrNull() ?: cluster.firstOrNull()
            letter.speed = 380f + Random.nextFloat() * 220f
            val order = si.toFloat() / slots.size.toFloat()
            letter.fallDelay = order * 1.4f + Random.nextFloat() * 0.3f
        }
        // Оставшимся extra тоже даём fall параметры
        for (p in pool) {
            if (p.role == Letter.Role.EXTRA) {
                p.speed = 320f + Random.nextFloat() * 220f
                p.fallDelay = Random.nextFloat() * 0.8f
            }
        }
        totalFillers = pool.count { it.role == Letter.Role.FILLER }
    }

    /** Рендерит ESPEAK в off-screen Bitmap и собирает массив непрозрачных пикселей. */
    private fun computeWordSlots(): MutableList<WordSlot> {
        val w = width.toInt().coerceAtLeast(1)
        val h = height.toInt().coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        val canvas = android.graphics.Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = wordFontSize
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val fm = paint.fontMetrics
        val centerY = wordCenterY - (fm.ascent + fm.descent) / 2f
        canvas.drawText("ESPEAK", width / 2f, centerY, paint)
        val step = slotSize.toInt().coerceAtLeast(2)
        val out = ArrayList<WordSlot>(8000)
        val pixels = IntArray(w)
        var y = 0
        while (y < h) {
            bmp.getPixels(pixels, 0, w, 0, y, w, 1)
            var x = 0
            while (x < w) {
                val a = (pixels[x] ushr 24) and 0xFF
                if (a > 128) out.add(WordSlot(x.toFloat() + step / 2f, y.toFloat() + step / 2f, false))
                x += step
            }
            y += step
        }
        bmp.recycle()
        return out
    }

    /** Рендерит bull path в Bitmap и возвращает точки где path непрозрачен (для anchors). */
    private fun computeBullAnchors(scale: Float, cx: Float, cy: Float, step: Int): List<Pair<Float, Float>> {
        val w = width.toInt().coerceAtLeast(1)
        val h = height.toInt().coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        val canvas = android.graphics.Canvas(bmp)
        val composePath = PathParser().parsePathString(BULL_PATH).toPath()
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(scale, scale)
        canvas.translate(-256f, -256f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
        canvas.drawPath(composePath.asAndroidPath(), paint)
        canvas.restore()
        val out = ArrayList<Pair<Float, Float>>(64)
        val pixels = IntArray(w)
        var y = 0
        while (y < h) {
            bmp.getPixels(pixels, 0, w, 0, y, w, 1)
            var x = 0
            while (x < w) {
                val a = (pixels[x] ushr 24) and 0xFF
                if (a > 128) out.add(x.toFloat() to y.toFloat())
                x += step
            }
            y += step
        }
        bmp.recycle()
        return out
    }
}

/**
 * Главный composable splash-анимации.
 *
 * @param onComplete вызывается когда анимация полностью отыграла (или юзер тапнул).
 *                   До этого момента splash overlay'ит весь экран.
 */
@Composable
fun EspeakSplashAnimation(onComplete: () -> Unit) {
    var time by remember { mutableFloatStateOf(0f) }
    var fastForward by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(false) }

    val state = remember { mutableStateOf<SplashState?>(null) }
    val bullPath = remember { PathParser().parsePathString(BULL_PATH).toPath().asAndroidPath() }

    // ── Paints (cached) ──
    val fallPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER; color = 0xFF7A3410.toInt()
        typeface = Typeface.MONOSPACE
    } }
    val fieldPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER; typeface = Typeface.MONOSPACE
    } }
    val specialPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER; color = 0xFF5C2608.toInt()
        typeface = Typeface.MONOSPACE
        setShadowLayer(10f, 0f, 0f, 0xFFFF6B35.toInt())
    } }
    val wordPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER; color = 0xFFFFFFFF.toInt()
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        setShadowLayer(16f, 0f, 0f, 0x40000000.toInt())
    } }
    val pixelPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        setShadowLayer(8f, 0f, 0f, 0x40000000.toInt())
    } }
    val bullPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        setShadowLayer(26f, 0f, 0f, 0xFFFF6B35.toInt())
    } }

    LaunchedEffect(Unit) {
        val startNs = System.nanoTime()
        while (!completed) {
            withFrameNanos { now ->
                val t = (now - startNs) / 1_000_000_000f
                time = if (fastForward) min(TOTAL_DUR, t + 8f) else t
                if (time >= TOTAL_DUR) {
                    completed = true
                }
            }
        }
        onComplete()
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { fastForward = true }
            }
    ) {
        val W = size.width
        val H = size.height
        if (state.value == null || state.value!!.width != W || state.value!!.height != H) {
            state.value = SplashState(W, H)
        }
        drawSplashFrame(
            t = time,
            st = state.value!!,
            fallPaint = fallPaint,
            fieldPaint = fieldPaint,
            specialPaint = specialPaint,
            wordPaint = wordPaint,
            pixelPaint = pixelPaint,
            bullPaint = bullPaint,
            bullPath = bullPath,
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  RENDER
// ═══════════════════════════════════════════════════════════
private fun DrawScope.drawSplashFrame(
    t: Float,
    st: SplashState,
    fallPaint: Paint,
    fieldPaint: Paint,
    specialPaint: Paint,
    wordPaint: Paint,
    pixelPaint: Paint,
    bullPaint: Paint,
    bullPath: android.graphics.Path,
) {
    val W = size.width
    val H = size.height

    // ── Фон: вертикальный оранжевый градиент ──
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0f to Color(0xFFFF7A45),
                0.5f to Color(0xFFFF6B35),
                1f to Color(0xFFF0571F),
            )
        )
    )

    val falling = t > HOLD_SEC
    val ft = t - HOLD_SEC

    // ── Pool letters (background + filler + extra fall) ──
    drawIntoCanvas { composeCanvas ->
        val ac: android.graphics.Canvas = composeCanvas.nativeCanvas
        fieldPaint.textSize = st.glyphSize
        fallPaint.textSize = st.glyphSize
        val baselineOffset = st.glyphSize * 0.35f

        for (p in st.pool) {
            if (p.role == Letter.Role.SPECIAL) continue
            if (p.gone || p.settled) continue

            // Изредка меняем символ — «дыхание»
            if (Random.nextFloat() < 0.06f) p.ch = randChar()

            if (!falling || ft < p.fallDelay) {
                // Hold-фаза: пульс альфы и цвета
                val b = 0.4f + abs(sin(t * 3f + p.phase)) * 0.35f
                fieldPaint.alpha = (b * 255f).toInt().coerceIn(0, 255)
                fieldPaint.color = fieldColor(b)
                ac.drawText(p.ch.toString(), p.x, p.y + baselineOffset, fieldPaint)
                continue
            }

            // Падение
            p.y += p.speed * 0.016f   // ~60 fps assumption (визуально стабильно)
            if (Random.nextFloat() < 0.04f) p.ch = randChar()

            if (p.role == Letter.Role.FILLER) {
                val slot = p.slot
                if (slot != null && p.y >= slot.y) {
                    // Долетела → заполняем кластер пикселей слова
                    for (s in p.cluster) s.filled = true
                    p.settled = true
                    st.settledCount++
                    continue
                }
                fallPaint.alpha = 242
                fallPaint.color = 0xFF7A3410.toInt()
                ac.drawText(p.ch.toString(), p.x, p.y + baselineOffset, fallPaint)
                continue
            }

            if (p.y > H + 20f) { p.gone = true; continue }
            fieldPaint.alpha = 128
            fieldPaint.color = fieldColor(0.5f)
            ac.drawText(p.ch.toString(), p.x, p.y + baselineOffset, fieldPaint)
        }

        // ── Пиксели слова (мозаика). Гасим когда слово собрано. ──
        if (st.fillDoneAt < 0f && falling && st.settledCount >= st.totalFillers) {
            st.fillDoneAt = t
        }
        val pixAlpha = if (st.fillDoneAt > 0f) {
            (1f - min(1f, (t - st.fillDoneAt) / WORD_REVEAL_DUR)).coerceIn(0f, 1f)
        } else 1f
        pixelPaint.alpha = (pixAlpha * 255f).toInt().coerceIn(0, 255)
        val pix = st.pixSize
        for (s in st.slots) {
            if (s.filled) {
                ac.drawRect(s.x - pix / 2f, s.y - pix / 2f, s.x + pix / 2f, s.y + pix / 2f, pixelPaint)
            }
        }

        // ── Гладкий ESPEAK сверху ──
        if (st.fillDoneAt > 0f) {
            val smoothK = min(1f, (t - st.fillDoneAt) / WORD_REVEAL_DUR)
            wordPaint.textSize = st.wordFontSize
            wordPaint.alpha = (smoothK * 255f).toInt().coerceIn(0, 255)
            val fm = wordPaint.fontMetrics
            val baseline = st.wordCenterY - (fm.ascent + fm.descent) / 2f
            ac.drawText("ESPEAK", W / 2f, baseline, wordPaint)
        }

        // ── Special letters: hold → converge → BAM → logo ──
        val convStart = if (st.fillDoneAt > 0f) st.fillDoneAt + CONV_DELAY else Float.MAX_VALUE
        specialPaint.textSize = st.glyphSize
        fieldPaint.textSize = st.glyphSize
        var allConv = true
        for (p in st.specials) {
            if (t < convStart && Random.nextFloat() < 0.06f) p.ch = randChar()
            if (t < convStart) {
                allConv = false
                val b = 0.4f + abs(sin(t * 3f + p.phase)) * 0.35f
                fieldPaint.alpha = (b * 255f).toInt().coerceIn(0, 255)
                fieldPaint.color = fieldColor(b)
                ac.drawText(p.ch.toString(), p.x, p.y + baselineOffset, fieldPaint)
            } else {
                val k = min(1f, (t - convStart) / CONV_DUR)
                if (k < 1f) allConv = false
                val e = ease(k)
                val cx = p.x + (W / 2f - p.x) * e
                val cy = p.y + (st.bullCenterY - p.y) * e
                val alpha = (max(0f, 1f - max(0f, (k - 0.85f) / 0.15f)) * 255f).toInt().coerceIn(0, 255)
                specialPaint.alpha = alpha
                ac.drawText(p.ch.toString(), cx, cy + baselineOffset, specialPaint)
            }
        }

        // ── BAM ──
        if (st.fillDoneAt > 0f && allConv && st.boomAt < 0f && t > convStart + CONV_DUR * 0.9f) {
            st.boomAt = t
        }
        if (st.boomAt > 0f) {
            val bt = t - st.boomAt
            // Радиальная вспышка
            if (bt < BAM_FLASH_DUR) {
                val r = 120f + bt * 500f
                val alphaPct = max(0f, 1f - bt / BAM_FLASH_DUR)
                drawCircle(
                    color = Color.White.copy(alpha = alphaPct),
                    radius = r,
                    center = androidx.compose.ui.geometry.Offset(W / 2f, st.bullCenterY),
                )
            }
            // Кольцо
            if (bt < BAM_RING_DUR) {
                val sk = bt / BAM_RING_DUR
                drawCircle(
                    color = Color.White.copy(alpha = 1f - sk),
                    radius = sk * (W * 0.46f),
                    center = androidx.compose.ui.geometry.Offset(W / 2f, st.bullCenterY),
                    style = Stroke(width = 8f * (1f - sk)),
                )
            }
            // Логотип-бык
            val lk = ((bt - 0.12f) / BAM_LOGO_DUR).coerceIn(0f, 1f)
            if (lk > 0f) {
                val k = ease(lk)
                val bullAlpha = (k * 255f).toInt().coerceIn(0, 255)
                val sc = (st.width / 512f) * 0.60f * (0.7f + k * 0.3f)
                bullPaint.alpha = bullAlpha
                ac.save()
                ac.translate(W / 2f, st.bullCenterY)
                ac.scale(sc, sc)
                ac.translate(-256f, -256f)
                ac.drawPath(bullPath, bullPaint)
                ac.restore()
            }
        }
    }

    // ── Виньетка ──
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0x59783A0A)),
            center = androidx.compose.ui.geometry.Offset(W / 2f, H * 0.5f),
            radius = H * 0.7f,
        )
    )
}

private fun fieldColor(b: Float): Int = when {
    b > 0.7f -> 0xFF5C2608.toInt()   // dark burnt orange
    b > 0.4f -> 0xFF8A4418.toInt()
    else     -> 0xFFB5683A.toInt()
}
