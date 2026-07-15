package com.spanishapp.service

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
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
 * диалог и продолжить пользоваться.
 *
 * v1.26.2 FIX (критический): FLEXIBLE update НЕ устанавливается сам —
 * после скачивания ОБЯЗАТЕЛЕН вызов [AppUpdateManager.completeUpdate].
 * Раньше его не было: пакет скачивался и висел, при повторном «Обновить»
 * Play выдавал «приложение не может установиться/обновиться». Теперь
 * [InstallStateUpdatedListener] ловит DOWNLOADED → state [UpdateState.Downloaded]
 * → UI показывает «Перезапустить» → [completeUpdate].
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

    // v1.26.2: слушатель прогресса flexible-загрузки. Singleton живёт весь
    // процесс — регистрируем один раз, не отписываемся.
    private val installListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> _updateInfo.value = UpdateState.Downloaded
            InstallStatus.FAILED,
            InstallStatus.CANCELED   -> _updateInfo.value = UpdateState.NoUpdate
            else -> Unit   // DOWNLOADING/PENDING/INSTALLING — UI не дёргаем
        }
    }

    init {
        if (!BuildConfig.DEBUG) manager.registerListener(installListener)
    }

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
                _updateInfo.value = when {
                    // v1.26.2 FIX: пакет уже скачан (в этой сессии или прошлой,
                    // процесс мог перезапуститься) — сразу предлагаем установку,
                    // НЕ заводим второй download (он и давал ошибку Play).
                    info.installStatus() == InstallStatus.DOWNLOADED ->
                        UpdateState.Downloaded
                    // Загрузка уже идёт — не показываем «Доступно обновление» повторно.
                    info.installStatus() == InstallStatus.DOWNLOADING ->
                        UpdateState.InProgress
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ->
                        UpdateState.Available(
                            availableVersion = info.availableVersionCode(),
                            info = info,
                        )
                    info.updateAvailability() ==
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
     * v1.26.2: установить скачанное обновление. Перезапускает приложение —
     * UI обязан спросить юзера («Перезапустить?») перед вызовом.
     */
    fun completeUpdate() {
        runCatching { manager.completeUpdate() }
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
        /** v1.26.2: пакет скачан — UI предлагает «Перезапустить» → completeUpdate(). */
        object Downloaded : UpdateState()
    }

    companion object {
        const val REQUEST_CODE = 7711
    }
}
