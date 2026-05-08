package com.spanishapp.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.service.AppLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val appLockManager: AppLockManager,
    private val authRepository: com.spanishapp.data.repository.AuthRepository
) : ViewModel() {

    fun unlock() {
        appLockManager.markUnlocked()
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.setLoggedIn(false)
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        }
    }
}

@Composable
fun AppLockScreen(
    navController: NavHostController,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var promptShown by remember { mutableStateOf(false) }

    fun showBiometricPrompt() {
        val act = activity ?: run {
            errorMessage = "Ошибка: не удалось показать биометрию"
            return
        }
        val executor = ContextCompat.getMainExecutor(act)
        val prompt = BiometricPrompt(
            act,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.unlock()
                    navController.navigate("home") {
                        popUpTo("app_lock") { inclusive = true }
                        launchSingleTop = true
                    }
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> {
                            errorMessage = null  // юзер сам отменил, без сообщения
                        }
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            errorMessage = "Слишком много попыток. Подожди или войди заново."
                        }
                        else -> {
                            errorMessage = errString.toString()
                        }
                    }
                }
                override fun onAuthenticationFailed() {
                    errorMessage = "Не распознано. Попробуй ещё раз."
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Разблокировать ESPEAK")
            .setSubtitle("Используй отпечаток или лицо")
            .setNegativeButtonText("Войти заново")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()
        prompt.authenticate(info)
    }

    LaunchedEffect(Unit) {
        if (!promptShown) {
            promptShown = true
            showBiometricPrompt()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Приложение заблокировано",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Разблокируй биометрией, чтобы продолжить",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                errorMessage!!,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = { showBiometricPrompt() },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Fingerprint, null)
            Spacer(Modifier.width(8.dp))
            Text("Разблокировать", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = {
                viewModel.signOut()
                navController.navigate("welcome") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти из аккаунта", color = MaterialTheme.colorScheme.error)
        }
    }
}
