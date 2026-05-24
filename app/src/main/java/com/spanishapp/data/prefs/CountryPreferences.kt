package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.countryDataStore by preferencesDataStore(name = "country_prefs")

private object CountryKeys {
    /** Override страны от пользователя (ISO-2 код). Пусто = использовать auto-detect. */
    val OVERRIDE = stringPreferencesKey("override_iso")
    /** Кэш auto-detect результата (IP-API + Telephony) чтобы не дёргать сеть каждый раз. */
    val CACHED   = stringPreferencesKey("cached_iso")
}

/**
 * Хранит выбор страны пользователем для лидерборда.
 *
 * Приоритет:
 *  • OVERRIDE — если юзер сам выбрал из picker, это и используем
 *  • CACHED   — результат прошлого успешного IP-API/Telephony detect
 *  • runtime detect (см. LeaderboardRepository.deviceCountryCodeAsync)
 */
@Singleton
class CountryPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val overrideIso: Flow<String> = context.countryDataStore.data.map {
        it[CountryKeys.OVERRIDE].orEmpty()
    }

    val cachedIso: Flow<String> = context.countryDataStore.data.map {
        it[CountryKeys.CACHED].orEmpty()
    }

    suspend fun setOverride(iso: String) {
        context.countryDataStore.edit { it[CountryKeys.OVERRIDE] = iso.uppercase() }
    }

    suspend fun clearOverride() {
        context.countryDataStore.edit { it.remove(CountryKeys.OVERRIDE) }
    }

    suspend fun setCached(iso: String) {
        context.countryDataStore.edit { it[CountryKeys.CACHED] = iso.uppercase() }
    }
}
