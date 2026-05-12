package com.spanishapp.ui.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.spanishapp.R
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun WelcomeScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val privacyUrl = stringResource(R.string.privacy_policy_url)
    val termsUrl = stringResource(R.string.terms_url)

    val openLink: (String) -> Unit = { url ->
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = tween(400)) +
                slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 8 })
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top spacer (для центрирования контента) ──
        Spacer(Modifier.height(0.dp))

        // ── Центральный блок ─────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }

            Text(
                "¡Hola!",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.welcome_subtitle),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            if (state.generalError != null) {
                Text(
                    state.generalError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { navController.navigate("register") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !state.isLoading
            ) {
                Text(stringResource(R.string.btn_start_learning), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !state.isLoading
            ) {
                Text(stringResource(R.string.welcome_already_have_account), fontSize = 18.sp)
            }

            Spacer(Modifier.height(40.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Text(
                    stringResource(R.string.welcome_login_with),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }

            Spacer(Modifier.height(20.dp))

            GoogleSignInButton(viewModel = viewModel, enabled = !state.isLoading)
        }

        // ── Footer: ссылки на политику ────────────────────────────
        val termsPrefix = stringResource(com.spanishapp.R.string.auth_welcome_terms_prefix)
        val privacyLabel = stringResource(com.spanishapp.R.string.auth_privacy_policy)
        val termsAnd = stringResource(com.spanishapp.R.string.auth_welcome_terms_and)
        val termsLabel = stringResource(com.spanishapp.R.string.auth_terms_of_use)
        val termsSuffix = stringResource(com.spanishapp.R.string.auth_welcome_terms_suffix)
        val footer = buildAnnotatedString {
            append(termsPrefix)
            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                append(privacyLabel)
            }
            append(termsAnd)
            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                append(termsLabel)
            }
            append(termsSuffix)
        }
        Text(
            text = footer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { openLink(privacyUrl) },
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 18.sp
        )
    }
    }
}

@Composable
fun SocialLoginButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier
            .size(60.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
