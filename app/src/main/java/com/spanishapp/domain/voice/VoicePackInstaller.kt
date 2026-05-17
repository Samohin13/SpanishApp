package com.spanishapp.domain.voice

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Toast

object VoicePackInstaller {

    /** Список всех испанских голосов, установленных на устройстве. */
    fun spanishVoices(tts: TextToSpeech): List<android.speech.tts.Voice> {
        val voices = tts.voices ?: return emptyList()
        return voices.filter { v ->
            v.locale.language == "es" &&
            !v.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
        }
    }

    /**
     * 1.1.1 fix: топ-7 HD голосов **только Spain (Castilian)** для UX.
     * Раньше показывали 18 голосов вперемешку (US/Латино + Spain + не-HD)
     * — юзер тонул в выборе, дубликаты, не было гарантии HD-качества.
     *
     * Алгоритм:
     *   1. Берём только es_ES (кастильский — наша целевая аудитория)
     *   2. Только HD (quality >= 400) ИЛИ neural
     *   3. Дедупликация по comma-name (одинаковые displayName/gender)
     *   4. Лимит 7 (Apple HIG: ≤ 7 опций для комфортного выбора)
     */
    fun topSpanishVoices(tts: TextToSpeech, limit: Int = 7): List<android.speech.tts.Voice> {
        val all = spanishVoices(tts)
        // Только Spain Castilian
        val castilian = all.filter { it.locale.country.equals("ES", ignoreCase = true) }
        // Только HD
        val hdOnly = castilian.filter { v ->
            val name = v.name.lowercase()
            v.quality >= 400 ||
            name.contains("network") ||
            name.contains("wavenet") ||
            name.contains("neural")  ||
            name.contains("hd")
        }
        // Дедупликация по «base name» (отрезаем суффиксы -local/-network/-1/-2)
        val seen = mutableSetOf<String>()
        return hdOnly
            .sortedByDescending { it.quality }
            .filter { v ->
                val base = baseName(v.name)
                if (seen.contains(base)) false else { seen.add(base); true }
            }
            .take(limit)
    }

    /**
     * Извлекает «корневое» имя из системного — убирает суффиксы которые
     * различают локальный/сетевой вариант одного голоса.
     * Пример: "es-es-x-eef-network", "es-es-x-eef-local" → оба → "es-es-x-eef"
     */
    private fun baseName(name: String): String {
        val lower = name.lowercase()
        return lower
            .removeSuffix("-network")
            .removeSuffix("-local")
            .removeSuffix("-1")
            .removeSuffix("-2")
            .removeSuffix("-3")
    }

    /** True, если установлен хотя бы один высококачественный или neural испанский голос. */
    fun isHdInstalled(tts: TextToSpeech): Boolean {
        return spanishVoices(tts).any { v ->
            val name = v.name.lowercase()
            v.quality >= 400 ||                       // HIGH или VERY_HIGH
            name.contains("network") ||
            name.contains("wavenet") ||
            name.contains("neural")  ||
            name.contains("hd")
        }
    }

    /** Открывает системный диалог установки голосовых пакетов. */
    fun launchInstaller(context: Context) {
        val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Плоский fallback — открыть общие настройки TTS
            openTtsSettings(context)
        }
    }

    /** Открывает настройки синтеза речи в системе. */
    fun openTtsSettings(context: Context) {
        val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Последний fallback — общие языковые настройки
            try {
                context.startActivity(
                    Intent(Settings.ACTION_LOCALE_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            } catch (e2: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "Не удалось открыть настройки. Зайди вручную: Настройки → Язык → Синтез речи.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /** Открывает Google Play на странице приложения Google TTS, если оно не установлено. */
    fun openGoogleTtsInPlayStore(context: Context) {
        val pkg = "com.google.android.tts"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            )
        }
    }
}
