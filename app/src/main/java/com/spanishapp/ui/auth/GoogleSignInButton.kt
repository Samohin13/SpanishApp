package com.spanishapp.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.spanishapp.R
import com.spanishapp.ui.components.BrandIcons

/**
 * Общий onClick для Google Sign-In: настраивает ActivityResultLauncher и
 * возвращает лямбду запуска. Переиспользуется круглой [GoogleSignInButton] и
 * полноширинной [GoogleSignInFullButton] — чтобы логика была в одном месте.
 */
@Composable
private fun rememberGoogleSignInOnClick(viewModel: AuthViewModel): () -> Unit {
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

    return {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        // Принудительно показываем выбор аккаунта (на случай если юзер уже залогинен другим аккаунтом).
        client.signOut().addOnCompleteListener {
            launcher.launch(client.signInIntent)
        }
    }
}

/**
 * Круглая Google-кнопка (иконка) для Login / Register экранов и как
 * вторичный вариант.
 */
@Composable
fun GoogleSignInButton(
    viewModel: AuthViewModel,
    iconSize: Int = 24,
    enabled: Boolean = true,
) {
    val onClick = rememberGoogleSignInOnClick(viewModel)
    SocialLoginButton(
        onClick = { if (enabled) onClick() },
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

/**
 * v1.26.1: полноширинная Google-кнопка «Продолжить с Google» — главный вариант
 * входа на Welcome (1 тап → облако + рейтинг). Светлый контейнер = максимально
 * заметна на тёмном фоне + узнаваемый Google-стиль.
 */
@Composable
fun GoogleSignInFullButton(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val onClick = rememberGoogleSignInOnClick(viewModel)
    Button(
        onClick = { if (enabled) onClick() },
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1F1F1F),
            disabledContainerColor = Color.White.copy(alpha = 0.5f),
            disabledContentColor = Color(0xFF1F1F1F).copy(alpha = 0.5f),
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = BrandIcons.Google,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.btn_continue_google),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
