package com.spanishapp.ui.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.spanishapp.R
import com.spanishapp.data.repository.AuthRepository
import com.spanishapp.util.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val successMessage: String? = null,
    val isRegistered: Boolean = false,
    val isLoggedIn: Boolean? = null,
    val userLevel: String? = null,
    val userName: String? = null,
    val userAge: Int? = null,
    val userReason: String? = null,
    val onboardingCompleted: Boolean = false,
    val acceptedTerms: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: com.spanishapp.data.repository.SyncRepository,
    // v1.25.88: sync displayName в user_progress для leaderboard
    private val userProgressDao: com.spanishapp.data.db.dao.UserProgressDao,
    // v1.25.98 (audit auth-H2): wipe локальных данных при входе в ДРУГОЙ аккаунт.
    private val userDataWiper: com.spanishapp.service.UserDataWiper,
    // v1.25.98 (adversarial review): владелец локального прогресса + шлагбаум
    // upload'ов на время сверки с облаком.
    private val accountSyncGuard: com.spanishapp.service.AccountSyncGuard,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * v1.25.98 (adversarial review): единая сверка локальных данных с аккаунтом
     * после УСПЕШНОГО входа/регистрации/link. Вызывать с uid результата
     * (для link — это сохранённый анонимный uid).
     *
     * Модель (как в account-based приложениях):
     *  1. beginAccountSwitch() — блокируем upload'ы ДО любых действий. Это
     *     закрывает гонку: между resolve signIn (currentUser уже = новый uid)
     *     и завершением сверки конкурентный uploadAll (onStop force-upload,
     *     RatingUpdater) мог записать локальные данные/нули в облачный док
     *     нового юзера НАВСЕГДА (SetOptions.merge перетирает поля).
     *  2. Если owner (владелец локальных данных) — ДРУГОЙ uid → это чужие
     *     данные на общем устройстве → wipe. Работает и для link: если на
     *     устройстве оставались данные другого реального аккаунта (logout не
     *     стирает Room), анонимная сессия «унаследовала» их — стираем.
     *     owner == null (свежий гость) или owner == uid (свой re-login) → НЕ
     *     стираем: прогресс гостя/юзера сохраняется. Это чинит H1
     *     (регистрация больше не уничтожает собственный прогресс гостя).
     *  3. downloadAll() при УСПЕХЕ вызывает completeAccountSwitch(uid) внутри
     *     (owner=uid, upload разблокирован). При ЛЮБОЙ ошибке шлагбаум
     *     остаётся закрыт → ни один upload не занулит облако (чинит H2);
     *     ретрай на следующем логине/ручном синке.
     *  4. Только после успешной сверки выгружаем локальный прогресс (напр.
     *     наработанный гостем до регистрации) в облако.
     */
    private suspend fun reconcileAfterAuth(uid: String) {
        val owner = accountSyncGuard.ownerUid()
        accountSyncGuard.beginAccountSwitch()

        if (owner != null && owner != uid) {
            Log.i("AuthViewModel", "Account switch ($owner -> $uid): wiping foreign local data")
            val wiped = runCatching { userDataWiper.wipeAll() }
                .onFailure { Log.e("AuthViewModel", "wipe failed on account switch", it) }
                .isSuccess
            if (!wiped) {
                // Шлагбаум остаётся закрыт — ретрай на следующем логине.
                return
            }
        }

        val downloaded = syncRepository.downloadAll().isSuccess
        if (downloaded) {
            runCatching { syncRepository.uploadAll(force = true) }
        } else if (owner == null || owner == uid) {
            // Скачать не удалось, но чужого облака клобберить нечем (свежий
            // владелец / свой re-login) — claim + upload, чтобы приложение не
            // осталось навсегда с заблокированным синком.
            accountSyncGuard.completeAccountSwitch(uid)
            runCatching { syncRepository.uploadAll(force = true) }
        }
        // else: switch + download упал → остаёмся заблокированы, ретрай.
    }

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
                if (!currentUser.isAnonymous) {
                    val uid = currentUser.uid
                    when {
                        // v1.25.98 (adversarial review): seed маркера владельца
                        // для юзеров, уже залогиненных на момент апдейта — иначе
                        // ПЕРВАЯ смена аккаунта после апдейта прошла бы без защиты.
                        accountSyncGuard.ownerUid() == null ->
                            accountSyncGuard.setOwner(uid)
                        // Незавершённая сверка (download упал при switch и
                        // upload остался заблокирован) переживает перезапуск в
                        // persisted-сессии — дочиниваем на старте, иначе синк
                        // навсегда заблокирован до явного ре-логина.
                        !accountSyncGuard.isUploadAllowed(uid) ->
                            reconcileAfterAuth(uid)
                    }
                }
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
            Log.w("AuthViewModel", "syncUserDataFromFirestore failed", e)
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
                Log.w("AuthViewModel", "saveUserDataToFirestore failed", e)
            }
        }
    }

    fun register(email: String, pass: String, confirmPass: String) {
        val emailErr = AuthValidator.getEmailError(email)
        val passErr = AuthValidator.getPasswordError(pass)
        val confirmErr = when {
            confirmPass.isBlank() -> appContext.getString(R.string.auth_error_repeat_password)
            confirmPass != pass -> appContext.getString(R.string.auth_register_passwords_mismatch)
            else -> null
        }
        val termsErr = if (!_uiState.value.acceptedTerms)
            appContext.getString(R.string.auth_error_policy_required)
        else null

        if (emailErr != null || passErr != null || confirmErr != null || termsErr != null) {
            _uiState.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passErr,
                    confirmPasswordError = confirmErr,
                    generalError = termsErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null,
                    generalError = null
                )
            }
            try {
                // v1.25.98 (audit auth-H1): стандартный флоу «как у всех»
                // (Duolingo/Firebase-приложения): если юзер уже занимается под
                // анонимным Firebase-аккаунтом — ПРИВЯЗЫВАЕМ email к нему
                // (linkWithCredential), uid сохраняется → облачный прогресс,
                // PRO-верификация и лидерборд остаются за тем же uid. Раньше
                // createUser заменял юзера → анонимные облачные данные
                // осиротевали (ghost-дубли лидерборда).
                val anon = auth.currentUser?.takeIf { it.isAnonymous }
                val user = if (anon != null) {
                    val cred = com.google.firebase.auth.EmailAuthProvider
                        .getCredential(email, pass)
                    anon.linkWithCredential(cred).await().user
                } else {
                    auth.createUserWithEmailAndPassword(email, pass).await().user
                }
                authRepository.setLoggedIn(true)
                if (user != null) {
                    // Индустриальный стандарт: письмо-подтверждение email
                    // (ссылка от Firebase). Не блокирует вход — как у других.
                    runCatching { user.sendEmailVerification().await() }
                        .onFailure { Log.w("AuthViewModel", "sendEmailVerification failed", it) }
                    // Сверка локальных данных с аккаунтом + безопасная выгрузка
                    // прогресса гостя (см. reconcileAfterAuth).
                    reconcileAfterAuth(user.uid)
                }
                _uiState.update { it.copy(isLoading = false, isRegistered = true) }
            } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                // Email уже зарегистрирован — стандартная ошибка, юзеру
                // предлагается войти (та же семантика, что была у createUser).
                Log.w("AuthViewModel", "register: email already in use", e)
                _uiState.update { it.copy(isLoading = false, generalError = e.localizedMessage) }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "register failed", e)
                _uiState.update { it.copy(isLoading = false, generalError = e.localizedMessage) }
            }
        }
    }

    fun setAcceptedTerms(accepted: Boolean) {
        _uiState.update { it.copy(acceptedTerms = accepted, generalError = null) }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(generalError = appContext.getString(R.string.auth_error_fill_all_fields)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                authRepository.setLoggedIn(true)
                // v1.25.90: тянем профиль из Firestore + локальный прогресс из облака
                // (зеркало loginWithGoogle). Раньше email-вход после переустановки
                // оставлял auth_prefs пустыми → юзер уходил на onboarding и
                // перезаписывал свои Firestore-данные.
                if (result.user != null) {
                    // Сначала профиль (имя/уровень) — до сверки прогресса.
                    syncUserDataFromFirestore(result.user!!.uid)
                    // v1.25.98: единая безопасная сверка (wipe при switch,
                    // блокировка upload'ов до успешного download, merge по MAX
                    // на каждом логине — убран гейт isLocalEmpty).
                    reconcileAfterAuth(result.user!!.uid)
                }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "login failed", e)
                _uiState.update { it.copy(isLoading = false, generalError = e.localizedMessage) }
            }
        }
    }

    fun resetPassword(email: String) {
        if (!AuthValidator.isValidEmail(email)) {
            _uiState.update { it.copy(emailError = appContext.getString(R.string.auth_error_invalid_email)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null, successMessage = null) }
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = appContext.getString(R.string.auth_forgot_success_template, email)
                    )
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "resetPassword failed", e)
                _uiState.update { it.copy(isLoading = false, generalError = e.localizedMessage) }
            }
        }
    }

    fun consumeSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
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
            // v1.25.88: ОБЯЗАТЕЛЬНО синхронизируем в user_progress.displayName.
            // v1.25.90: targeted UPDATE (Daos.kt:458) вместо copy()+update() —
            // иначе lost-update race с RatingUpdater, который пишет в rating/xp
            // колонки параллельно. Failure → Crashlytics, не silent swallow.
            runCatching {
                userProgressDao.updateDisplayName(name)
            }.onFailure { e ->
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[AuthViewModel] updateDisplayName failed", e))
            }
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
                // v1.25.98 (audit auth-H1): анонимный юзер + вход через Google —
                // сначала пробуем ПРИВЯЗАТЬ Google к текущему анонимному uid
                // (прогресс сохраняется за тем же uid). Если этот Google-аккаунт
                // уже существует в Firebase (collision) — обычный вход: как у
                // других приложений, аккаунт из облака побеждает, а локальные
                // данные предыдущего владельца снимает reconcileAfterAuth.
                val anon = auth.currentUser?.takeIf { it.isAnonymous }
                val result = if (anon != null) {
                    try {
                        anon.linkWithCredential(credential).await()
                    } catch (_: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        auth.signInWithCredential(credential).await()
                    }
                } else {
                    auth.signInWithCredential(credential).await()
                }
                if (result.user != null) {
                    authRepository.setLoggedIn(true)
                    syncUserDataFromFirestore(result.user!!.uid)
                    // v1.25.98: единая безопасная сверка (см. reconcileAfterAuth).
                    reconcileAfterAuth(result.user!!.uid)
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "loginWithGoogle failed", e)
                _uiState.update { it.copy(isLoading = false, generalError = "Google Auth Error: ${e.localizedMessage}") }
            }
        }
    }

    fun clearErrors() {
        _uiState.update {
            it.copy(
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                generalError = null
            )
        }
    }
}
