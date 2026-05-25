package com.spanishapp.service

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.spanishapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Проверка наличия обновления в Play Store. На старте приложения
 * запрашивает AppUpdateManager. Если новая версия доступна — exposed
 * через [updateInfo] StateFlow, UI показывает простую плашку:
 *
 *     ┌──────────────────────────────┐
 *     │  Доступно обновление         │
 *     │  Версия 1.22.31              │
 *     │  [ Позже ]  [ Обновить ]     │
 *     └──────────────────────────────┘
 *
 * Используем FLEXIBLE update — фоновая загрузка, юзер может закрыть
 * диалог и продолжить пользоваться. По завершении скачивания
 * приложение перезапускается.
 *
 * Работает только когда APK установлен из Play Store. В debug-сборках
 * со sideload — Manager вернёт UPDATE_NOT_AVAILABLE.
 */
@Singleton
class AppUpdateChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    private val _updateInfo = MutableStateFlow<UpdateState>(UpdateState.Unknown)
    val updateInfo: StateFlow<UpdateState> = _updateInfo.asStateFlow()

    /**
     * Запросить статус обновления. Вызывать в Activity.onResume.
     * Идемпотентно — повторный вызов перезаписывает текущий state.
     */
    fun checkForUpdate() {
        // В debug-сборках манагер бесполезен — приложение установлено через ADB,
        // нет связи с Play Store. Не дёргаем чтобы не ловить ошибку.
        if (BuildConfig.DEBUG) {
            _updateInfo.value = UpdateState.NoUpdate
            return
        }
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                _updateInfo.value = when (info.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> UpdateState.Available(
                        availableVersion = info.availableVersionCode(),
                        info = info,
                    )
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                        UpdateState.InProgress
                    else -> UpdateState.NoUpdate
                }
            }
            .addOnFailureListener {
                _updateInfo.value = UpdateState.NoUpdate
            }
    }

    /**
     * Запустить flexible update flow. UI вызывает после тапа «Обновить».
     * Play Store откроет свой UI прогресса скачивания.
     */
    fun startFlexibleUpdate(activity: Activity, info: AppUpdateInfo) {
        runCatching {
            manager.startUpdateFlowForResult(
                info,
                activity,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                REQUEST_CODE,
            )
        }
    }

    /**
     * Юзер тапнул «Позже» — больше в текущей сессии не показываем.
     * При следующем cold start checkForUpdate() снова вернёт Available.
     */
    fun dismissForSession() {
        _updateInfo.value = UpdateState.NoUpdate
    }

    sealed class UpdateState {
        /** До первого checkForUpdate(). */
        object Unknown : UpdateState()
        /** Обновлений нет / debug / ошибка. */
        object NoUpdate : UpdateState()
        /** Уже идёт скачивание (developer-triggered). */
        object InProgress : UpdateState()
        /** Есть обновление — UI показывает плашку. */
        data class Available(
            val availableVersion: Int,
            val info: AppUpdateInfo,
        ) : UpdateState()
    }

    companion object {
        const val REQUEST_CODE = 7711
    }
}
