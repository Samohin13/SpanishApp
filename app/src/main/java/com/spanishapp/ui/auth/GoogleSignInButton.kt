package com.spanishapp.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.spanishapp.R
import com.spanishapp.ui.components.BrandIcons

/**
 * Универсальная Google-кнопка для Welcome / Login / Register экранов.
 *
 * Запускает Google Sign-In через ActivityResultContract, при успехе
 * вызывает [viewModel.loginWithGoogle], при ошибке — записывает
 * её в `generalError` через [viewModel.socialLogin].
 */
@Composable
fun GoogleSignInButton(
    viewModel: AuthViewModel,
    iconSize: Int = 24,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val webClientId = stringResource(R.string.default_web_client_id)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { viewModel.loginWithGoogle(it) }
                ?: viewModel.socialLogin("Google: пустой idToken")
        } catch (e: ApiException) {
            viewModel.socialLogin("Google Error: ${e.statusCode} — ${e.localizedMessage ?: ""}")
        }
    }

    SocialLoginButton(
        onClick = {
            if (!enabled) return@SocialLoginButton
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(context, gso)
            // Принудительно показываем выбор аккаунта (на случай если юзер уже залогинен другим аккаунтом).
            client.signOut().addOnCompleteListener {
                launcher.launch(client.signInIntent)
            }
        },
        content = {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = BrandIcons.Google,
                    contentDescription = "Google",
                    modifier = Modifier.size(iconSize.dp),
                    tint = Color.Unspecified
                )
            }
        }
    )
}
