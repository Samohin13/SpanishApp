package com.spanishapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.spanishapp.data.repository.AuthRepository
import com.spanishapp.util.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isRegistered: Boolean = false,
    val isLoggedIn: Boolean? = null,
    val userLevel: String? = null,
    val userName: String? = null,
    val userAge: Int? = null,
    val userReason: String? = null,
    val onboardingCompleted: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val currentUser = auth.currentUser
        viewModelScope.launch {
            authRepository.setLoggedIn(currentUser != null)
            if (currentUser != null) {
                syncUserDataFromFirestore(currentUser.uid)
            }
        }

        viewModelScope.launch {
            authRepository.isLoggedIn.collect { loggedIn ->
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
            }
        }
        viewModelScope.launch {
            authRepository.userLevel.collect { level ->
                _uiState.update { it.copy(userLevel = level) }
            }
        }
        viewModelScope.launch {
            authRepository.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }
        viewModelScope.launch {
            authRepository.userAge.collect { age ->
                _uiState.update { it.copy(userAge = age) }
            }
        }
        viewModelScope.launch {
            authRepository.userReason.collect { reason ->
                _uiState.update { it.copy(userReason = reason) }
            }
        }
        viewModelScope.launch {
            authRepository.onboardingCompleted.collect { completed ->
                _uiState.update { it.copy(onboardingCompleted = completed) }
            }
        }
    }

    private suspend fun syncUserDataFromFirestore(uid: String) {
        try {
            val document = db.collection("users").document(uid).get().await()
            if (document.exists()) {
                val name = document.getString("name")
                val age = document.getLong("age")?.toInt()
                val reason = document.getString("reason")
                val level = document.getString("level")
                
                name?.let { authRepository.setUserName(it) }
                age?.let { authRepository.setUserAge(it) }
                reason?.let { authRepository.setUserReason(it) }
                level?.let { authRepository.setUserLevel(it) }
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    private fun saveUserDataToFirestore() {
        val currentUser = auth.currentUser ?: return
        val data = mapOf(
            "name" to uiState.value.userName,
            "age" to uiState.value.userAge,
            "reason" to uiState.value.userReason,
            "level" to uiState.value.userLevel,
            "updatedAt" to System.currentTimeMillis()
        )
        
        viewModelScope.launch {
            try {
                db.collection("users").document(currentUser.uid).set(data).await()
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun register(email: String, pass: String) {
        val emailErr = AuthValidator.getEmailError(email)
        val passErr = AuthValidator.getPasswordError(pass)

        if (emailErr != null || passErr != null) {
            _uiState.update { it.copy(emailError = emailErr, passwordError = passErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailError = null, passwordError = null, generalError = null) }
            try {
                auth.createUserWithEmailAndPassword(email, pass).await()
                authRepository.setLoggedIn(true)
                _uiState.update { it.copy(isLoading = false, isRegistered = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, generalError = e.localizedMessage) }
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(generalError = "Заполните все поля") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                authRepository.setLoggedIn(true)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, generalError = e.localizedMessage) }
            }
        }
    }

    fun resetPassword(email: String) {
        if (!AuthValidator.isValidEmail(email)) {
            _uiState.update { it.copy(emailError = "Неверный формат email") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.update { it.copy(isLoading = false, generalError = "Инструкции отправлены на почту") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, generalError = e.localizedMessage) }
            }
        }
    }

    fun logout() {
        auth.signOut()
        viewModelScope.launch {
            authRepository.setLoggedIn(false)
        }
    }

    fun selectLevel(level: String) {
        viewModelScope.launch {
            authRepository.setUserLevel(level)
            saveUserDataToFirestore()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            authRepository.setOnboardingCompleted(true)
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            authRepository.setUserName(name)
            saveUserDataToFirestore()
        }
    }

    fun updateAge(age: Int) {
        viewModelScope.launch {
            authRepository.setUserAge(age)
            saveUserDataToFirestore()
        }
    }

    fun updateReason(reason: String) {
        viewModelScope.launch {
            authRepository.setUserReason(reason)
            saveUserDataToFirestore()
        }
    }

    fun socialLogin(error: String) {
        _uiState.update { it.copy(generalError = error, isLoading = false) }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                if (result.user != null) {
                    authRepository.setLoggedIn(true)
                    syncUserDataFromFirestore(result.user!!.uid)
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, generalError = "Google Auth Error: ${e.localizedMessage}") }
            }
        }
    }

    fun clearErrors() {
        _uiState.update { it.copy(emailError = null, passwordError = null, generalError = null) }
    }
}
