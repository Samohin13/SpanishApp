package com.spanishapp.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * v1.26.1: единый признак «пользователь — гость» (ещё не зарегистрировался).
 * true если анонимный Firebase-аккаунт (isGuest) ИЛИ локальный guestMode (без
 * анонимного аккаунта — Anonymous Auth выключен / офлайн). Сбрасывается при
 * регистрации/входе в реальный аккаунт. Используется баннером Home, пиллом
 * «Гость» в Profile и строкой аккаунта в Settings — один источник правды.
 */
@Composable
fun rememberIsGuest(): State<Boolean> {
    val authViewModel: AuthViewModel = hiltViewModel()
    val state by authViewModel.uiState.collectAsStateWithLifecycle()
    return rememberUpdatedState(state.isGuest || state.guestMode)
}
