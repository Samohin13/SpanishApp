package com.spanishapp.ui.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val privacyUrl = stringResource(R.string.privacy_policy_url)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) {
            navController.navigate("name_entry") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.spanishapp.R.string.auth_register_title), fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.spanishapp.R.string.auth_back))
                    }
                }
            )
        }
    ) { padding ->
        val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(animationSpec = tween(400)) +
                    slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 8 })
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(com.spanishapp.R.string.auth_register_subtitle),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // ── Email ────────────────────────────────────────────
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearErrors() },
                label = { Text(stringResource(R.string.auth_email)) },
                placeholder = { Text("example@mail.com") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.emailError != null,
                supportingText = { if (state.emailError != null) Text(state.emailError!!) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                leadingIcon = { Icon(Icons.Default.Email, null) },
                singleLine = true
            )

            // ── Пароль ────────────────────────────────────────────
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearErrors() },
                label = { Text(stringResource(R.string.auth_password)) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.passwordError != null,
                supportingText = { if (state.passwordError != null) Text(state.passwordError!!) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, "Toggle password visibility")
                    }
                },
                singleLine = true
            )

            // ── Live-индикатор силы пароля ────────────────────────
            if (password.isNotEmpty()) {
                PasswordStrengthIndicator(password = password)
            }

            // ── Подтверждение пароля ──────────────────────────────
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; viewModel.clearErrors() },
                label = { Text(stringResource(R.string.auth_password_confirm)) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.confirmPasswordError != null ||
                    (confirmPassword.isNotEmpty() && confirmPassword != password),
                supportingText = {
                    when {
                        state.confirmPasswordError != null -> Text(state.confirmPasswordError!!)
                        confirmPassword.isNotEmpty() && confirmPassword != password ->
                            Text(stringResource(com.spanishapp.R.string.auth_register_passwords_mismatch))
                        confirmPassword.isNotEmpty() && confirmPassword == password ->
                            Text(stringResource(com.spanishapp.R.string.auth_register_passwords_match), color = Color(0xFF2E7D32))
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                singleLine = true
            )

            // ── Чекбокс «Я согласен с Privacy Policy» ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.acceptedTerms,
                    onCheckedChange = { viewModel.setAcceptedTerms(it) }
                )
                val agreePrefix = stringResource(com.spanishapp.R.string.auth_register_terms_agree_prefix)
                val policyLabel = stringResource(com.spanishapp.R.string.auth_privacy_policy)
                val annotated = buildAnnotatedString {
                    append(agreePrefix)
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(policyLabel)
                    }
                }
                Text(
                    text = annotated,
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    lineHeight = 18.sp
                )
                IconButton(onClick = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))
                    }
                }) {
                    Icon(Icons.Default.OpenInNew, contentDescription = stringResource(com.spanishapp.R.string.auth_register_open_policy_cd), modifier = Modifier.size(18.dp))
                }
            }

            if (state.generalError != null) {
                Text(state.generalError!!, color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = { viewModel.register(email, password, confirmPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.auth_register), fontSize = 18.sp)
                }
            }

            TextButton(onClick = {
                // Pop back to welcome — otherwise register↔login bounce
                // would grow the back stack unboundedly.
                navController.navigate("login") {
                    popUpTo("welcome")
                    launchSingleTop = true
                }
            }) {
                Text(stringResource(com.spanishapp.R.string.auth_register_have_account))
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Text(
                    stringResource(com.spanishapp.R.string.auth_login_or_with),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }

            Spacer(Modifier.height(8.dp))

            GoogleSignInButton(viewModel = viewModel, iconSize = 20, enabled = !state.isLoading)
        }
        }
    }
}

/**
 * 3 чекмарка под полем пароля: ≥8 символов, цифра, буква.
 */
@Composable
private fun PasswordStrengthIndicator(password: String) {
    val hasLength = password.length >= 8
    val hasDigit = password.any { it.isDigit() }
    val hasLetter = password.any { it.isLetter() }

    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        StrengthCheck(passed = hasLength, label = stringResource(com.spanishapp.R.string.auth_register_strength_length))
        StrengthCheck(passed = hasDigit, label = stringResource(com.spanishapp.R.string.auth_register_strength_digit))
        StrengthCheck(passed = hasLetter, label = stringResource(com.spanishapp.R.string.auth_register_strength_letter))
    }
}

@Composable
private fun StrengthCheck(passed: Boolean, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (passed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (passed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
