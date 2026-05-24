package com.spanishapp.ui.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Pure-Canvas рендер share-картинки 1080×1920 для Instagram Stories / WhatsApp.
 *
 * Сознательно НЕ через Compose-to-bitmap (captureToImage хрупкий, требует
 * визуализированного экрана). Здесь — детерминированный offscreen Canvas,
 * который можно вызвать из background thread.
 *
 * Юридически: никаких слов «certificate»/«diploma» — только «модуль закрыт»,
 * «N раундов», «X минут». См. [ProgressShareData] доку.
 */
object ProgressImageRenderer {

    private const val W = 1080
    private const val H = 1920

    private const val PLAY_URL = "https://play.google.com/store/apps/details?id=com.espeak.app"

    // Палитра — синхронно с гайдом дизайна (оранж/тёмный фон + tier-цвета)
    private const val COLOR_BG_TOP = 0xFFFF6B1A.toInt()
    private const val COLOR_BG_BOTTOM = 0xFF18191C.toInt()
    private const val COLOR_ACCENT = 0xFFFF6B1A.toInt()
    private const val COLOR_CARD_BG = 0x33FFFFFF
    private const val COLOR_CARD_BORDER = 0x44FFFFFF

    private fun tierColor(tier: String): Int = when (tier) {
        "gold" -> 0xFFF4B400.toInt()
        "silver" -> 0xFFC0C0C0.toInt()
        else -> 0xFFCD7F32.toInt()      // bronze (и любой fallback)
    }

    private fun tierEmoji(tier: String): String = when (tier) {
        "gold" -> "🥇"
        "silver" -> "🥈"
        else -> "🥉"
    }

    private fun tierLabelRu(tier: String): String = when (tier) {
        "gold" -> "GOLD"
        "silver" -> "SILVER"
        else -> "BRONZE"
    }

    /**
     * Главная точка входа — синхронная (быстрая, чисто Canvas-операции).
     * Из ViewModel вызывать с Dispatchers.Default чтобы не блокировать UI.
     */
    fun render(data: ProgressShareData): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        drawBackground(c)
        drawBrandHeader(c)
        drawTitle(c, data)
        drawTierHero(c, data)
        drawStatsCard(c, data)
        drawMarketingTag(c, data)
        drawQrAndFooter(c)

        return bmp
    }

    // ── Слои ────────────────────────────────────────────────────────────

    private fun drawBackground(c: Canvas) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader = LinearGradient(
            0f, 0f, 0f, H.toFloat(),
            intArrayOf(COLOR_BG_TOP, 0xFF8B2D0E.toInt(), COLOR_BG_BOTTOM),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, W.toFloat(), H.toFloat(), p)

        // Лёгкий blob-акцент в верхнем правом углу
        val blob = Paint(Paint.ANTI_ALIAS_FLAG)
        blob.color = 0x33FFFFFF
        c.drawCircle(W * 0.95f, 200f, 320f, blob)
    }

    private fun drawBrandHeader(c: Canvas) {
        val flag = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        c.drawText("🇪🇸", 70f, 160f, flag)

        val brand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.12f
        }
        c.drawText("ESPEAK", 170f, 160f, brand)
    }

    private fun drawTitle(c: Canvas, data: ProgressShareData) {
        val h1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 88f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = -0.02f
            textAlign = Paint.Align.CENTER
        }
        val title = if (data.isModuleFinal) {
            "МОДУЛЬ ${data.cefr} ЗАКРЫТ!"
        } else {
            "ЧЕКПОИНТ ПРОЙДЕН!"
        }
        c.drawText(title, W / 2f, 380f, h1)

        val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFE0CC.toInt()
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        // Кавычки + checkmark — название чекпоинта (испанская сцена).
        val subText = "«${data.cpTitle}» ✓"
        c.drawText(ellipsize(subText, sub, W - 160f), W / 2f, 450f, sub)
    }

    private fun drawTierHero(c: Canvas, data: ProgressShareData) {
        // Большая эмодзи-медаль
        val emoji = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 280f
            textAlign = Paint.Align.CENTER
        }
        c.drawText(tierEmoji(data.tier), W / 2f, 800f, emoji)

        // Tier label
        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tierColor(data.tier)
            textSize = 84f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.18f
            textAlign = Paint.Align.CENTER
        }
        c.drawText(tierLabelRu(data.tier), W / 2f, 900f, label)
    }

    private fun drawStatsCard(c: Canvas, data: ProgressShareData) {
        val left = 100f
        val right = W - 100f
        val top = 970f
        val bottom = 1380f
        val r = 36f

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_CARD_BG
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_CARD_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val rect = RectF(left, top, right, bottom)
        c.drawRoundRect(rect, r, r, cardPaint)
        c.drawRoundRect(rect, r, r, borderPaint)

        // 4 строки статистики
        val rows = listOf(
            "🎯" to "${data.accuracy}% точность",
            "🔁" to "${data.totalRounds} раундов",
            "⏱" to "${data.timeMinutes} мин",
            "⭐" to "+${data.xpEarned} XP",
        )

        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 56f
        }
        val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var y = top + 90f
        for ((emoji, text) in rows) {
            c.drawText(emoji, left + 50f, y, emojiPaint)
            c.drawText(text, left + 140f, y, txtPaint)
            y += 90f
        }
    }

    private fun drawMarketingTag(c: Canvas, data: ProgressShareData) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFEEEEEE.toInt()
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        c.drawText("${data.userName} изучает испанский с ESPEAK", W / 2f, 1490f, p)
        c.drawText("Присоединяйся:", W / 2f, 1546f, p)
    }

    private fun drawQrAndFooter(c: Canvas) {
        val qrSize = 280
        val qrLeft = (W - qrSize) / 2f
        val qrTop = 1590f

        val qrBmp = generateQr(PLAY_URL, qrSize)
        // Белая подложка под QR (для контраста на тёмном фоне)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val pad = 16f
        val bgRect = RectF(qrLeft - pad, qrTop - pad, qrLeft + qrSize + pad, qrTop + qrSize + pad)
        c.drawRoundRect(bgRect, 16f, 16f, bg)
        c.drawBitmap(qrBmp, qrLeft, qrTop, null)

        val foot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_ACCENT
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.04f
        }
        c.drawText("espeak.app", W / 2f, qrTop + qrSize + 90f, foot)
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun generateQr(content: String, sizePx: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 0,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    /** Обрезает строку с многоточием если не влезает в ширину. */
    private fun ellipsize(text: String, paint: Paint, maxWidthPx: Float): String {
        if (paint.measureText(text) <= maxWidthPx) return text
        var end = text.length
        while (end > 1 && paint.measureText(text.substring(0, end) + "…") > maxWidthPx) {
            end--
        }
        return text.substring(0, end) + "…"
    }
}
