package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Профиль AI-репетитора, который пользователь настраивает один раз.
 *
 * Хранит:
 *  - name: как зовут наставника (по умолчанию "Tutor")
 *  - avatar: emoji-аватарка (по умолчанию 🤖 — нейтральный)
 *  - configured: завершил ли юзер первичную настройку
 *
 * Используется в:
 *  - AiChatScreen (header — имя/аватар)
 *  - AiChatRepository (system prompt подставляет имя)
 *  - AiChatViewModel (welcome message)
 */
private val Context.tutorPrefsStore by preferencesDataStore(name = "tutor_profile")

data class TutorProfile(
    val name: String = "Tutor",
    val avatar: String = "🤖",
    val configured: Boolean = false,
)

@Singleton
class TutorProfilePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val NAME = stringPreferencesKey("tutor_name")
    private val AVATAR = stringPreferencesKey("tutor_avatar")
    private val CONFIGURED = booleanPreferencesKey("tutor_configured")

    val profile: Flow<TutorProfile> = context.tutorPrefsStore.data.map { p ->
        TutorProfile(
            name = p[NAME] ?: "Tutor",
            avatar = p[AVATAR] ?: "🤖",
            configured = p[CONFIGURED] ?: false,
        )
    }

    suspend fun save(name: String, avatar: String) {
        context.tutorPrefsStore.edit {
            it[NAME] = name.trim().ifBlank { "Tutor" }
            it[AVATAR] = avatar
            it[CONFIGURED] = true
        }
    }

    /** Список доступных emoji-аватарок (нейтральные + разные). */
    companion object {
        val AVATARS = listOf(
            "🤖", "🦉", "🐱", "🦊", "🐼", "🐰", "🦄",
            "👩‍🏫", "👨‍🏫", "🧙", "🌟", "🌵",
        )
    }
}
