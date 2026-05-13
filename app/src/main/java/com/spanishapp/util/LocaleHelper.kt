package com.spanishapp.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Применяет выбранный пользователем язык UI к контексту.
 * Используется через `attachBaseContext()` в MainActivity.
 *
 * Поддерживаемые значения:
 * - "ru" / "en" / "uk" / "es" — принудительно установить локаль
 * - "system" / null / любое другое — оставить системную локаль
 *
 * Важно: при добавлении новой локали — НЕ забыть прописать её здесь,
 * иначе ресурсы strings.xml будут, а сама смена языка не сработает.
 */
object LocaleHelper {

    fun applyLocale(base: Context, lang: String?): Context {
        if (lang.isNullOrBlank() || lang == "system") return base
        val locale = when (lang) {
            "ru" -> Locale("ru")
            "en" -> Locale("en")
            "uk" -> Locale("uk")
            "es" -> Locale("es")
            else -> return base
        }
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
