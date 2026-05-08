package com.spanishapp.service

import android.content.Context
import androidx.biometric.BiometricManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Управляет состоянием биометрического замка приложения в рантайме.
 *
 * - [isUnlocked] — разблокирован ли App Lock в этой сессии. Сбрасывается
 *   в false при возврате приложения из background (через MainActivity.onStop).
 * - [canUseBiometric] — поддерживает ли устройство Class-3 биометрию
 *   и настроен ли отпечаток / лицо.
 */
@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun markUnlocked() { _isUnlocked.value = true }
    fun lock() { _isUnlocked.value = false }

    /**
     * Доступна ли биометрия на устройстве. Используется чтобы показать
     * toggle в Settings только если устройство умеет.
     */
    fun biometricAvailability(): Availability {
        val mgr = BiometricManager.from(context)
        val flags = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        return when (mgr.canAuthenticate(flags)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Availability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Availability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Availability.HwUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Availability.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> Availability.SecurityUpdate
            else -> Availability.Unknown
        }
    }

    enum class Availability {
        Available, NoHardware, HwUnavailable, NoneEnrolled, SecurityUpdate, Unknown;
        val isUsable: Boolean get() = this == Available
    }
}
