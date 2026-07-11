package com.spanishapp.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * v1.25.98 (audit auth-H2 + adversarial review): владелец локального прогресса
 * и «шлагбаум» синка при смене аккаунта.
 *
 * Модель (стандартная для account-based приложений):
 *   локальные данные принадлежат ровно ОДНОМУ uid («owner»). Смена владельца =
 *   wipe локальных данных → download нового аккаунта. Пока download не прошёл
 *   успешно, ВСЕ upload'ы заблокированы — иначе гонка (onStop force-upload,
 *   RatingUpdater fire-and-forget) может записать нули или чужие данные в
 *   облачный док нового юзера НАВСЕГДА (SetOptions.merge перетирает поля).
 *
 * Хранение — отдельный SharedPreferences (НЕ auth_prefs: тот целиком чистится
 * при logout, а маркер должен переживать logout, чтобы поймать вход другого
 * юзера). Флаг блокировки персистентный: смерть процесса между wipe и
 * download не открывает шлагбаум.
 */
@Singleton
class AccountSyncGuard @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("progress_owner", Context.MODE_PRIVATE)

    /** uid владельца локальных данных, null = данные «ничьи» (fresh install). */
    fun ownerUid(): String? = prefs.getString("uid", null)

    fun setOwner(uid: String) {
        prefs.edit().putString("uid", uid).apply()
    }

    /** Полный сброс — deleteAccount / logout (данные стёрты, владельца нет). */
    fun clear() {
        prefs.edit().clear().apply()
    }

    /**
     * Начало смены аккаунта: блокируем upload'ы ДО wipe. Первая операция
     * после успешного signIn — чтобы конкурирующие upload'ы (onStop,
     * RatingUpdater) не успели снять снапшот старых данных под новым uid.
     */
    fun beginAccountSwitch() {
        prefs.edit().putBoolean("upload_blocked", true).apply()
    }

    /** Успешный download нового аккаунта → данные консистентны, открываем синк. */
    fun completeAccountSwitch(newOwnerUid: String) {
        prefs.edit()
            .putString("uid", newOwnerUid)
            .putBoolean("upload_blocked", false)
            .apply()
    }

    /**
     * Можно ли выгружать локальные данные под [currentUid]?
     * Блокируем если: (а) идёт незавершённая смена аккаунта (wipe прошёл,
     * download ещё нет), или (б) локальные данные принадлежат ДРУГОМУ uid
     * (auth уже переключился, wipe ещё не добежал — самая опасная гонка).
     */
    fun isUploadAllowed(currentUid: String): Boolean {
        if (prefs.getBoolean("upload_blocked", false)) return false
        val owner = ownerUid()
        return owner == null || owner == currentUid
    }
}
