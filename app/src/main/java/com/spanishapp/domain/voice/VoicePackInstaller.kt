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
