@file:Suppress("DEPRECATION")
package com.spanishapp.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.spanishapp.R
import com.spanishapp.ui.adaptive.adaptiveContentWidth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.data.prefs.AppLockPreferences
import com.spanishapp.data.prefs.AppPreferences
import com.spanishapp.data.repository.AuthRepository
import com.spanishapp.service.AppLockManager
import com.spanishapp.util.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val appPreferences: AppPreferences,
    private val authRepository: AuthRepository,
    private val appLockPreferences: AppLockPreferences,
    private val appLockManager: AppLockManager,
    private val vibrationHelper: com.spanishapp.service.VibrationHelper,
    private val syncRepository: com.spanishapp.data.repository.SyncRepository,
    private val wordDao: com.spanishapp.data.db.dao.WordDao,
    private val achievementDao: com.spanishapp.data.db.dao.AchievementDao,
    private val libroProgressDao: com.spanishapp.data.db.dao.LibroProgressDao,
    private val flashcardSetProgressDao: com.spanishapp.data.db.dao.FlashcardSetProgressDao,
    private val lessonProgressDao: com.spanishapp.data.db.dao.LessonProgressDao,
    private val chatMessageDao: com.spanishapp.data.db.dao.ChatMessageDao,
    private val dailyXpDao: com.spanishapp.data.db.dao.DailyXpDao,
    private val gameLevelProgressDao: com.spanishapp.data.db.dao.GameLevelProgressDao,
    private val articleGameDao: com.spanishapp.data.db.dao.ArticleGameDao,
    private val wordListDao: com.spanishapp.data.db.dao.WordListDao,
    private val recentSearchDao: com.spanishapp.data.db.dao.RecentSearchDao,
    private val weeklyLeagueDao: com.spanishapp.data.db.dao.WeeklyLeagueDao,
    private val subscriptionManager: com.spanishapp.service.SubscriptionManager,
) : ViewModel() {

    /** v1.23.6: для debug-toggle и отображения текущего статуса. */
    val isPro: kotlinx.coroutines.flow.StateFlow<Boolean> = subscriptionManager.isProActive
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** v1.23.6: debug-toggle для тестирования PRO-gating. */
    fun debugSetPro(enabled: Boolean) = viewModelScope.launch {
        subscriptionManager.debugSetPro(enabled)
    }

    /**
     * Wipes every piece of user-generated state in Room. Seed tables (words,
     * conjugations, lessons, dialogues) keep their rows but their per-row
     * study stats are cleared. Called from Reset Progress and Delete Account.
     */
    private suspend fun wipeAllUserData() {
        wordDao.resetAllStats()
        achievementDao.resetAll()
        libroProgressDao.deleteAll()
        flashcardSetProgressDao.deleteAll()
        lessonProgressDao.deleteAll()
        chatMessageDao.deleteAll()
        dailyXpDao.deleteAll()
        gameLevelProgressDao.deleteAll()
        articleGameDao.deleteAllProgress()
        wordListDao.deleteAllEntries()
        wordListDao.deleteAllLists()
        recentSearchDao.deleteAll()
        weeklyLeagueDao.deleteAll()
        userProgressDao.update(UserProgressEntity())
    }

    /** Результат синхронизации — для понятных Toast-сообщений. */
    enum class SyncResult { SUCCESS, NO_INTERNET, AUTH_FAILED, FAILED }

    /**
     * 1.1.1 fix: раньше требовалась авторизация (Google Sign-In).
     * Теперь — auto sign-in **anonymously** если юзер не залогинен.
     * Anonymous-аккаунт даёт uid в Firebase → достаточно для синхронизации
     * прогресса между устройствами одного юзера через recover-flow.
     */
    suspend fun syncNow(): SyncResult {
        // 1. Гарантируем наличие uid — авто-логин если нужно
        if (FirebaseAuth.getInstance().currentUser == null) {
            val authOk = runCatching {
                FirebaseAuth.getInstance().signInAnonymously().await()
            }.isSuccess
            if (!authOk) return SyncResult.AUTH_FAILED
        }
        // 2. Upload + Download
        val up = syncRepository.uploadAll(force = true)
        val down = syncRepository.downloadAll()
        return when {
            up.isSuccess && down.isSuccess -> SyncResult.SUCCESS
            // Network errors часто содержат "UNAVAILABLE" / "network" в message
            up.exceptionOrNull()?.message?.contains("network", true) == true ||
            down.exceptionOrNull()?.message?.contains("network", true) == true ->
                SyncResult.NO_INTERNET
            else -> {
                runCatching {
                    val e = up.exceptionOrNull() ?: down.exceptionOrNull() ?: return@runCatching
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                        .recordException(e)
                }
                SyncResult.FAILED
            }
        }
    }

    val appLockEnabled = appLockPreferences.isEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val biometricUsable: Boolean get() = appLockManager.biometricAvailability().isUsable

    fun setAppLockEnabled(enabled: Boolean) = viewModelScope.launch {
        appLockPreferences.setEnabled(enabled)
        // Включая впервые — считаем что эта сессия уже разблокирована,
        // чтобы юзер не отправился на app_lock экран сразу же после клика.
        if (enabled) appLockManager.markUnlocked()
    }

    // v1.17.3: default bucket + users/{uid}/* путь — синхрон с ProfileScreen
    // и Storage rules в Firebase Console (см. CLAUDE.md §10). Раньше был
    // отдельный bucket "firebasestorage.app" и путь "avatars/{uid}.jpg" —
    // не подпадал под rules `users/{uid}/*` → permission denied.
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _isPhotoLoading = MutableStateFlow(false)
    val isPhotoLoading = _isPhotoLoading.asStateFlow()

    // Локальное временное фото для мгновенного отображения (как в топ приложениях)
    private val _localPhotoUri = MutableStateFlow<Uri?>(null)
    
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError = _nameError.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    val userName = authRepository.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Estudiante")
    
    // Объединяем URL из репозитория и локальный URI для мгновенного эффекта
    val userPhotoUrl = combine(authRepository.userPhotoUrl, _localPhotoUri) { remote, local ->
        local?.toString() ?: remote
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun uploadProfilePhoto(bitmap: Bitmap, localUri: Uri) {
        _localPhotoUri.value = localUri // Показываем сразу!
        _isPhotoLoading.value = true

        viewModelScope.launch {
            try {
                // Обеспечиваем вход (многие игры делают так)
                var currentUser = auth.currentUser
                if (currentUser == null) {
                    currentUser = auth.signInAnonymously().await().user
                }
                
                if (currentUser == null) throw Exception("Auth failed")

                // Сжатие
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                val data = baos.toByteArray()

                // v1.17.3: путь users/{uid}/avatar.jpg синхронизирован с
                // ProfileScreen и Storage rules `users/{uid}/*` в Firebase Console.
                val storageRef = storage.reference.child("users/${currentUser.uid}/avatar.jpg")
                
                // Загрузка
                storageRef.putBytes(data).await()
                
                // Получение ссылки
                val downloadUrl = storageRef.downloadUrl.await().toString()
                
                // Сохранение в настройки и БД
                authRepository.setUserPhotoUrl(downloadUrl)
                db.collection("users").document(currentUser.uid)
                    .set(mapOf("photoUrl" to downloadUrl), com.google.firebase.firestore.SetOptions.merge())
                
                Log.d("SettingsVM", "Profile photo updated successfully")

            } catch (e: Exception) {
                Log.e("SettingsVM", "Upload failed", e)
                _localPhotoUri.value = null // Откатываем картинку в случае ошибки
                _errorEvent.emit("upload_error|${e.localizedMessage ?: ""}")
            } finally {
                _isPhotoLoading.value = false
            }
        }
    }

    // --- Остальные методы ---
    fun updateName(name: String) {
        val error = AuthValidator.getNameError(name)
        if (error != null) { _nameError.value = error; return }
        viewModelScope.launch {
            try {
                authRepository.setUserName(name)
                auth.currentUser?.let { db.collection("users").document(it.uid).update("name", name).await() }
                _nameError.value = null
            } catch (e: Exception) { _nameError.value = "Ошибка сохранения" }
        }
    }
    fun clearNameError() { _nameError.value = null }
    val progress: StateFlow<UserProgressEntity> = userProgressDao.getProgress().filterNotNull().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProgressEntity())
    val ttsEnabled = appPreferences.ttsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val soundEffects = appPreferences.soundEffectsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val vibration = appPreferences.vibrationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val reminders = appPreferences.remindersEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val reminderHour = appPreferences.reminderHour.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 19)
    val reminderMinute = appPreferences.reminderMinute.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val fontSize = appPreferences.fontSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MEDIUM")
    val uiLanguage = appPreferences.uiLanguage.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    /**
     * Persist the new UI language and call [onWritten] AFTER the DataStore
     * write completes. The caller will then trigger Activity.recreate() —
     * if we fire-and-forget, recreate races the write and the new locale
     * never applies (this was the bug on Welcome's flag picker too).
     */
    fun setUiLanguage(lang: String, onWritten: () -> Unit = {}) {
        viewModelScope.launch {
            appPreferences.setUiLanguage(lang)
            onWritten()
        }
    }
    fun toggleTts(e: Boolean) = viewModelScope.launch { appPreferences.setTtsEnabled(e) }
    fun toggleSoundEffects(e: Boolean) = viewModelScope.launch { appPreferences.setSoundEffectsEnabled(e) }
    fun toggleVibration(e: Boolean) = viewModelScope.launch {
        appPreferences.setVibrationEnabled(e)
        if (e) vibrationHelper.tick(70)  // small confirmation tick when turning ON
    }

    fun toggleReminders(context: android.content.Context, enabled: Boolean) = viewModelScope.launch {
        appPreferences.setRemindersEnabled(enabled)
        if (enabled) {
            com.spanishapp.service.DailyReminderWorker.schedule(
                context,
                appPreferences.reminderHour.first(),
                appPreferences.reminderMinute.first()
            )
        } else {
            com.spanishapp.service.DailyReminderWorker.cancel(context)
        }
    }

    fun setReminderTime(context: android.content.Context, hour: Int, minute: Int) = viewModelScope.launch {
        appPreferences.setReminderTime(hour, minute)
        // Перепланируем worker на новое время — REPLACE policy внутри schedule().
        if (appPreferences.remindersEnabled.first()) {
            com.spanishapp.service.DailyReminderWorker.schedule(context, hour, minute)
        }
    }

    /**
     * «Проверить напоминание сейчас» — мгновенно показывает тестовое
     * push-уведомление. Защита от ситуации «настроил время, но push не
     * приходят» (POST_NOTIFICATIONS не дано / Do-Not-Disturb / админ
     * лаунчера блокирует канал). Юзер сразу видит работает оно или нет.
     */
    fun sendTestReminder(context: android.content.Context) = viewModelScope.launch {
        com.spanishapp.service.DailyReminderWorker.fireOnce(context)
    }

    fun setFontSize(s: String) = viewModelScope.launch { appPreferences.setFontSize(s) }
    fun logout() = viewModelScope.launch {
        // Sign out from Firebase first; the leaderboard/sync code keys off the
        // current uid, so we must drop it before flipping the local flag.
        runCatching { FirebaseAuth.getInstance().signOut() }
        runCatching {
            authRepository.clearUserPhoto()
            authRepository.setUserName("")
        }
        authRepository.setLoggedIn(false)
    }

    /**
     * Permanently deletes the user's account: Firestore document, Firebase Storage avatar,
     * Firebase Auth user, and all local Room/DataStore state. Best-effort: logs and
     * continues if any remote step fails (so user is never stuck unable to delete).
     */
    fun deleteAccount() = viewModelScope.launch {
        val user = auth.currentUser
        val uid = user?.uid

        // 1. Firestore profile doc
        if (uid != null) {
            runCatching { db.collection("users").document(uid).delete().await() }
                .onFailure { Log.w("SettingsVM", "Firestore delete failed", it) }
            runCatching { db.collection("leaderboard").document(uid).delete().await() }
                .onFailure { Log.w("SettingsVM", "Leaderboard delete failed", it) }
        }

        // 2. Storage avatar
        if (uid != null) {
            runCatching { storage.reference.child("users/$uid/avatar.jpg").delete().await() }
                .onFailure { Log.w("SettingsVM", "Avatar delete failed", it) }
        }

        // 3. Firebase Auth user (may need recent re-auth — if it fails we still log out)
        runCatching { user?.delete()?.await() }
            .onFailure { Log.w("SettingsVM", "Auth.delete failed (recent login may be required)", it) }

        // 4. Local Room: wipe everything user-generated, not just user_progress.
        runCatching { wipeAllUserData() }

        // 5. DataStore: clear name, photo, level, login flag
        runCatching {
            authRepository.setUserName("")
            authRepository.clearUserPhoto()
            authRepository.setUserLevel("A1")
            authRepository.setLoggedIn(false)
        }
    }

    fun resetProgress() = viewModelScope.launch { wipeAllUserData() }

    fun updateLevel(level: String) = viewModelScope.launch {
        val p = progress.value
        userProgressDao.update(p.copy(currentLevel = level))
        authRepository.setUserLevel(level)
    }

    fun updateGoal(minutes: Int) = viewModelScope.launch {
        val p = progress.value
        userProgressDao.update(p.copy(dailyGoalMinutes = minutes))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { com.spanishapp.service.Analytics.settingsOpened() }
    val progress by vm.progress.collectAsStateWithLifecycle()
    val userName by vm.userName.collectAsStateWithLifecycle()
    val isPhotoLoading by vm.isPhotoLoading.collectAsStateWithLifecycle()
    val userPhotoUrl by vm.userPhotoUrl.collectAsStateWithLifecycle()
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val soundEffects by vm.soundEffects.collectAsStateWithLifecycle()
    val ttsEnabled by vm.ttsEnabled.collectAsStateWithLifecycle()
    val vibration by vm.vibration.collectAsStateWithLifecycle()
    val fontSize by vm.fontSize.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val uploadErrorPrefix = stringResource(R.string.set_upload_error, "")
    val uploadErrorTpl = stringResource(R.string.set_upload_error, "::ERR::")

    LaunchedEffect(Unit) {
        vm.errorEvent.collect { error ->
            val text = if (error.startsWith("upload_error|")) {
                val detail = error.removePrefix("upload_error|")
                uploadErrorTpl.replace("::ERR::", detail)
            } else error
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }
    
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            uri?.let {
                val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
                if (bitmap != null) vm.uploadProfilePhoto(bitmap, it)
            }
        }
    }

    var showNameDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // v1.15.0 P2: adaptive font для TopAppBar (18 → 22sp на планшете)
    val isWideSettings = com.spanishapp.ui.adaptive.isWideScreen()
    val settingsTitleFont = if (isWideSettings) 22.sp else 18.sp
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings), fontWeight = FontWeight.SemiBold, fontSize = settingsTitleFont) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back))
                    }
                }
            )
        }
    ) { padding ->
        // v1.15.0 P2: cap 720dp на планшете чтобы settings не растягивались
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .adaptiveContentWidth()
                .padding(bottom = 32.dp)
        ) {
            // ── Шапка профиля ──
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    // Same neutral palette as Profile screen avatar — was a
                    // milky-grey gravatar fallback on surfaceVariant. Now:
                    // dark surfaceContainerHighest base + 2dp primary ring +
                    // a Person glyph on the foreground when no photo set.
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                CircleShape
                            ),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shadowElevation = 4.dp
                    ) {
                        when {
                            isPhotoLoading -> Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) { CircularProgressIndicator(modifier = Modifier.size(40.dp)) }

                            userPhotoUrl != null -> AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(userPhotoUrl)
                                    .crossfade(true)
                                    .diskCachePolicy(CachePolicy.DISABLED)
                                    .build(),
                                contentDescription = stringResource(R.string.set_avatar_cd),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )

                            else -> Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = stringResource(R.string.set_avatar_cd),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            // Launch the picker directly. Cropper shows a chooser (Gallery/Camera);
                            // it handles the CAMERA permission prompt itself if the user picks Camera.
                            // Gallery via SAF needs no runtime permission on modern Android.
                            cropImageLauncher.launch(
                                CropImageContractOptions(
                                    null,
                                    CropImageOptions(
                                        imageSourceIncludeGallery = true,
                                        imageSourceIncludeCamera = true,
                                        guidelines = CropImageView.Guidelines.ON,
                                        aspectRatioX = 1, aspectRatioY = 1,
                                        fixAspectRatio = true,
                                        cropShape = CropImageView.CropShape.OVAL
                                    )
                                )
                            )
                        },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // ── Секции настроек ──

            // Премиум подписка — самый верх. Ссылка на paywall либо
            // «управление подпиской» если уже PRO.
            SettingsSection("Премиум") {
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "💎 ESPEAK PRO",
                    summary = "Открой A2 + B1 + B2, безлимит AI, 1327 глаголов",
                ) {
                    navController.navigate("paywall") { launchSingleTop = true }
                }
                // v1.23.6: Debug-toggle для тестирования PRO-gating до
                // интеграции Google Play Billing (Фаза 5). Только в debug-
                // сборках — в release будет скрыт.
                if (com.spanishapp.BuildConfig.DEBUG) {
                    val isProDebug by vm.isPro.collectAsStateWithLifecycle()
                    SettingsSwitchItem(
                        icon = Icons.Default.BugReport,
                        title = "🐛 DEBUG: PRO активен",
                        checked = isProDebug,
                        onCheckedChange = { vm.debugSetPro(it) },
                    )
                }
            }

            // Поддержка автора — обычным пунктом, без выделения цветом.
            // Открывает Boosty в браузере.
            SettingsSection(stringResource(R.string.settings_section_support)) {
                val boostyUrl = "https://boosty.to/espeak"
                SettingsItem(
                    icon = Icons.Default.Favorite,
                    title = stringResource(R.string.settings_support_title),
                    summary = stringResource(R.string.settings_support_summary),
                ) {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(boostyUrl))
                        )
                    }
                }
            }

            SettingsSection(stringResource(R.string.settings_section_profile)) {
                SettingsItem(Icons.Default.Edit, stringResource(R.string.settings_change_name), userName?.takeIf { it.isNotBlank() } ?: progress.displayName) { showNameDialog = true }
                // Уровень — read-only с возможностью пересдать тест.
                // Раньше тап открывал диалог где юзер мог самовольно
                // поменять с A1 на C2 — это ломало адаптивность системы.
                // Теперь тап показывает диалог: «Уровень определяется
                // тестом. Хочешь пересдать?» → запуск placement-test.
                SettingsItem(
                    Icons.Default.Translate,
                    stringResource(R.string.set_spanish_level),
                    when(progress.currentLevel) {
                        "A1" -> stringResource(R.string.set_level_a1)
                        "A2" -> stringResource(R.string.set_level_a2)
                        "B1" -> stringResource(R.string.set_level_b1)
                        "B2" -> stringResource(R.string.set_level_b2)
                        else -> progress.currentLevel
                    }
                ) { showLevelDialog = true }
                SettingsItem(Icons.Default.Timer, stringResource(R.string.settings_daily_goal), "${progress.dailyGoalMinutes} ${stringResource(R.string.settings_minutes_short)}") { showGoalDialog = true }
                SettingsItem(Icons.Default.BarChart, stringResource(R.string.set_progress_stats)) { navController.navigate("achievements") }
                // ── Sync Now (Phase 4) ──
                // 1.1.1: понятные сообщения по результату sync вместо
                // generic "не удалось". Всегда сначала пытаемся auto sign-in.
                val syncSuccess = stringResource(R.string.settings_sync_success)
                val syncError = stringResource(R.string.settings_sync_error)
                SettingsItem(Icons.Default.Sync, stringResource(R.string.settings_sync_now)) {
                    scope.launch {
                        val msg = when (vm.syncNow()) {
                            SettingsViewModel.SyncResult.SUCCESS      -> syncSuccess
                            SettingsViewModel.SyncResult.NO_INTERNET  -> "Нет интернета — попробуйте позже"
                            SettingsViewModel.SyncResult.AUTH_FAILED  -> "Не удалось войти в Firebase. Проверьте интернет."
                            SettingsViewModel.SyncResult.FAILED       -> syncError
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val reminderHour by vm.reminderHour.collectAsStateWithLifecycle()
            val reminderMinute by vm.reminderMinute.collectAsStateWithLifecycle()

            // Android 13+ requires runtime POST_NOTIFICATIONS permission;
            // without it, scheduled reminders are silently suppressed.
            val notifPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) vm.toggleReminders(context, true)
                else Toast.makeText(
                    context,
                    context.getString(R.string.set_notif_perm_denied),
                    Toast.LENGTH_LONG
                ).show()
            }

            SettingsSection(stringResource(R.string.settings_section_notifications)) {
                SettingsSwitchItem(
                    Icons.Default.Notifications,
                    stringResource(R.string.set_reminders),
                    reminders
                ) { newValue ->
                    if (!newValue) {
                        vm.toggleReminders(context, false)
                    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val granted = ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) vm.toggleReminders(context, true)
                        else notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.toggleReminders(context, true)
                    }
                }
                var showTimePicker by remember { mutableStateOf(false) }
                if (reminders) {
                    SettingsItem(
                        Icons.Default.AccessTime,
                        stringResource(R.string.set_reminder_time),
                        "%02d:%02d".format(reminderHour, reminderMinute)
                    ) {
                        showTimePicker = true
                    }
                    // Кнопка диагностики: «Проверить напоминание» — мгновенный
                    // тестовый push. Помогает юзеру убедиться что разрешения
                    // даны, канал не заблокирован и время выставлено правильно.
                    SettingsItem(
                        Icons.Default.NotificationsActive,
                        "Проверить напоминание",
                        "Получить тестовое уведомление сейчас"
                    ) {
                        vm.sendTestReminder(context)
                    }
                }
                if (showTimePicker) {
                    TimePickerSheet(
                        initialHour = reminderHour,
                        initialMinute = reminderMinute,
                        onConfirm = { h, m ->
                            vm.setReminderTime(context, h, m)
                            showTimePicker = false
                        },
                        onDismiss = { showTimePicker = false }
                    )
                }
            }

            SettingsSection(stringResource(R.string.settings_section_sound)) {
                SettingsSwitchItem(Icons.Default.VolumeUp, stringResource(R.string.settings_sound_effects), soundEffects) { vm.toggleSoundEffects(it) }
                SettingsSwitchItem(Icons.Default.RecordVoiceOver, stringResource(R.string.settings_voice_announcer), ttsEnabled) { vm.toggleTts(it) }
                SettingsSwitchItem(Icons.Default.Vibration, stringResource(R.string.set_vibration), vibration) { vm.toggleVibration(it) }
                SettingsItem(Icons.Default.InterpreterMode, stringResource(R.string.set_voice_setup)) {
                    navController.navigate("settings_voice") { launchSingleTop = true }
                }
            }

            SettingsSection(stringResource(R.string.settings_section_appearance)) {
                val fontLabel = when(fontSize) {
                    "SMALL" -> stringResource(R.string.set_font_small)
                    "MEDIUM" -> stringResource(R.string.set_font_medium)
                    "LARGE" -> stringResource(R.string.set_font_large)
                    else -> fontSize
                }
                SettingsItem(Icons.Default.TextFields, stringResource(R.string.set_font_title), fontLabel) { showFontDialog = true }
            }

            SettingsSection(stringResource(R.string.settings_section_languages)) {
                val uiLang by vm.uiLanguage.collectAsStateWithLifecycle()
                val uiLangLabel = when (uiLang) {
                    "ru" -> stringResource(R.string.set_lang_ru)
                    "en" -> stringResource(R.string.set_lang_en)
                    "uk" -> "Українська"
                    "es" -> "Español"
                    else -> stringResource(R.string.set_lang_system)
                }
                SettingsItem(Icons.Default.Language, stringResource(R.string.settings_language_ui), uiLangLabel) {
                    showLanguageDialog = true
                }
                // Target language is currently always Spanish — show as info row, no click.
                SettingsItem(Icons.Default.Public, stringResource(R.string.settings_language_target), stringResource(R.string.settings_target_spanish))
            }

            // Subscription / Help-center sections removed — no implementation yet.
            // Re-add when monetization or FAQ flow is built.

            SettingsSection(stringResource(R.string.settings_section_help)) {
                val emailSubject = stringResource(R.string.set_email_subject)
                val emailChooser = stringResource(R.string.set_email_chooser)
                SettingsItem(Icons.Default.MailOutline, stringResource(R.string.settings_contact)) {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@spanishapp.com")
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                    }
                    context.startActivity(Intent.createChooser(intent, emailChooser))
                }
            }

            // ── Биометрический замок ─────────────────────────────
            val appLockOn by vm.appLockEnabled.collectAsStateWithLifecycle()
            if (vm.biometricUsable) {
                SettingsSection(stringResource(R.string.settings_section_security)) {
                    SettingsSwitchItem(
                        icon = Icons.Default.Fingerprint,
                        title = stringResource(R.string.set_protect_biometric),
                        checked = appLockOn,
                        onCheckedChange = { vm.setAppLockEnabled(it) }
                    )
                    if (appLockOn) {
                        Text(
                            stringResource(R.string.set_biometric_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            val privacyUrl = stringResource(R.string.privacy_policy_url)
            val termsUrl = stringResource(R.string.terms_url)
            SettingsSection(stringResource(R.string.settings_section_privacy)) {
                SettingsItem(Icons.Default.PrivacyTip, stringResource(R.string.settings_privacy_policy)) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl))
                    runCatching { context.startActivity(intent) }
                }
                SettingsItem(Icons.Default.Description, stringResource(R.string.settings_terms)) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl))
                    runCatching { context.startActivity(intent) }
                }
                // Export data removed — feature not implemented yet.
            }

            SettingsSection(stringResource(R.string.settings_section_other)) {
                SettingsItem(Icons.Default.Leaderboard, stringResource(R.string.settings_leaderboards)) {
                    navController.navigate("leaderboard") { launchSingleTop = true }
                }
                // OTA «Загрузить обновления контента» убрана в v1 — см. Navigation.kt
                SettingsItem(Icons.Default.Refresh, stringResource(R.string.settings_reset_progress)) { showResetDialog = true }
                val shareTextTpl = stringResource(R.string.set_share_text, "https://github.com/Samohin13/SpanishApp")
                val shareChooser = stringResource(R.string.set_share_chooser)
                SettingsItem(Icons.Default.Share, stringResource(R.string.settings_share)) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareTextTpl)
                    }
                    runCatching {
                        context.startActivity(Intent.createChooser(intent, shareChooser))
                    }
                }
            }

            SettingsSection(stringResource(R.string.settings_section_account)) {
                SettingsItem(Icons.AutoMirrored.Filled.Logout, stringResource(R.string.settings_logout), textColor = MaterialTheme.colorScheme.error) { showLogoutDialog = true }
                SettingsItem(Icons.Default.DeleteForever, stringResource(R.string.set_delete_account_full), textColor = MaterialTheme.colorScheme.error) { showDeleteDialog = true }
            }

            // ── О приложении ──────────────────────────────────────
            SettingsSection(stringResource(R.string.set_about_section)) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.set_about_version),
                    summary = "${com.spanishapp.BuildConfig.VERSION_NAME} (build ${com.spanishapp.BuildConfig.VERSION_CODE})"
                )
            }

            // ── Подвал ────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.set_about_made_with_love),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    // ── Диалоги ──
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.set_dlg_logout_title)) },
            text = { Text(stringResource(R.string.set_dlg_logout_text)) },
            confirmButton = { Button(onClick = { vm.logout(); showLogoutDialog = false }) { Text(stringResource(R.string.set_dlg_logout_confirm)) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.btn_cancel)) } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.set_dlg_delete_title)) },
            text = { Text(stringResource(R.string.set_dlg_delete_text)) },
            confirmButton = {
                Button(
                    onClick = { vm.deleteAccount(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.set_dlg_delete_confirm), color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.btn_cancel)) } }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.set_dlg_reset_title)) },
            text = { Text(stringResource(R.string.set_dlg_reset_text)) },
            confirmButton = {
                Button(
                    onClick = { vm.resetProgress(); showResetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.set_dlg_reset_confirm), color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.btn_cancel)) } }
        )
    }

    if (showFontDialog) {
        val sizes = listOf(
            "SMALL" to stringResource(R.string.set_font_small),
            "MEDIUM" to stringResource(R.string.set_font_medium),
            "LARGE" to stringResource(R.string.set_font_large)
        )
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text(stringResource(R.string.set_font_title)) },
            text = {
                Column {
                    sizes.forEach { (key, label) ->
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.setFontSize(key); showFontDialog = false }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = fontSize == key, onClick = { vm.setFontSize(key); showFontDialog = false })
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLevelDialog) {
        // Read-only диалог: показывает текущий уровень + предлагает
        // пересдать placement-test. Раньше юзер мог самовольно поставить
        // C2 при нулевом прогрессе → ломалась адаптивность контента.
        val levelLabel = when(progress.currentLevel) {
            "A1" -> stringResource(R.string.set_level_a1)
            "A2" -> stringResource(R.string.set_level_a2)
            "B1" -> stringResource(R.string.set_level_b1)
            "B2" -> stringResource(R.string.set_level_b2)
            else -> progress.currentLevel
        }
        AlertDialog(
            onDismissRequest = { showLevelDialog = false },
            title = { Text(stringResource(R.string.set_spanish_level)) },
            text = {
                Column {
                    Text(
                        "Твой уровень: $levelLabel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Уровень определяется автоматически на основе " +
                        "пройденных уроков и упражнений. Если хочешь " +
                        "переопределить — пройди тест уровня заново.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showLevelDialog = false
                    navController.navigate("placement_test") { launchSingleTop = true }
                }) {
                    Text("Пересдать тест")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLevelDialog = false }) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    if (showGoalDialog) {
        val goals = listOf(5, 10, 15, 20, 30)
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text(stringResource(R.string.set_dlg_goal_title)) },
            text = {
                Column {
                    goals.forEach { mins ->
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.updateGoal(mins); showGoalDialog = false }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = progress.dailyGoalMinutes == mins, onClick = { vm.updateGoal(mins); showGoalDialog = false })
                            Text(stringResource(R.string.set_dlg_goal_minutes, mins), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLanguageDialog) {
        val uiLang by vm.uiLanguage.collectAsStateWithLifecycle()
        val activity = (context as? android.app.Activity)
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language_ui)) },
            text = {
                Column {
                    // ВАЖНО для не-русскоязычных юзеров: контент (уроки, слова,
                    // рассказы) сейчас полностью на русском. Меняется только
                    // язык интерфейса. Без этого предупреждения юзер выбирает
                    // English/Українська/Español → видит «битый» опыт (меню
                    // английское, контент русский) → разочарование, удаление.
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text("⚠", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Контент уроков сейчас только на русском. " +
                                "Перевод на другие языки в работе.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                    // 4 explicit languages with flag emojis. "System" option
                    // dropped — explicit choice is clearer and lets users with
                    // Spanish system locale still pick Russian for the UI.
                    // Note: ua/es resource folders fall back to en/ru if not
                    // translated yet (full translation is a separate task).
                    val options = listOf(
                        Triple("ru", "🇷🇺", "Русский"),
                        Triple("en", "🇬🇧", "English"),
                        Triple("uk", "🇺🇦", "Українська"),
                        Triple("es", "🇪🇸", "Español")
                    )
                    options.forEach { (code, flag, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (code != uiLang) {
                                        showLanguageDialog = false
                                        vm.setUiLanguage(code) {
                                            // Runs only after DataStore commit
                                            // → attachBaseContext reads fresh value.
                                            activity?.recreate()
                                        }
                                    } else {
                                        showLanguageDialog = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = code == uiLang, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(flag, fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    if (showNameDialog) {
        var tempName by remember { mutableStateOf(userName?.takeIf { it.isNotBlank() } ?: progress.displayName) }
        val nameError by vm.nameError.collectAsStateWithLifecycle()
        AlertDialog(
            onDismissRequest = { showNameDialog = false; vm.clearNameError() },
            title = { Text(stringResource(R.string.set_dlg_name_title)) },
            text = {
                // v1.15.0 P0: Box с imePadding гарантирует что клавиатура
                // не перекроет TextField даже если AlertDialog не обрабатывает ime автоматически.
                Box(modifier = Modifier.imePadding()) {
                    OutlinedTextField(
                        value = tempName, onValueChange = { tempName = it; vm.clearNameError() },
                        label = { Text(stringResource(R.string.set_dlg_name_label)) }, isError = nameError != null,
                        supportingText = { if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error) },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.updateName(tempName)
                    if (AuthValidator.getNameError(tempName) == null) showNameDialog = false
                }) { Text(stringResource(R.string.set_dlg_name_save)) }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, summary: String? = null, textColor: Color = MaterialTheme.colorScheme.onSurface, onClick: (() -> Unit)? = null) {
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()
    Row(modifier = Modifier.fillMaxWidth().clickable(enabled = onClick != null) {
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        onClick?.invoke()
    }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = if (textColor == MaterialTheme.colorScheme.error) textColor else MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor)
            if (summary != null) Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onClick != null) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()
    val wrappedChange: (Boolean) -> Unit = {
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        onCheckedChange(it)
    }
    Row(modifier = Modifier.fillMaxWidth().clickable { wrappedChange(!checked) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        // v1.22.32: явные цвета OFF-состояния. Дефолтная M3 палитра
        // в dark theme делает unchecked Switch почти невидимым (track
        // сливается с surfaceVariant). Видимый светло-серый track +
        // ярко-белый thumb даёт явное визуальное отличие OFF/ON.
        Switch(
            checked = checked,
            onCheckedChange = wrappedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
                uncheckedTrackColor = androidx.compose.ui.graphics.Color(0xFF5A5A5A),
                uncheckedBorderColor = androidx.compose.ui.graphics.Color(0xFF7A7A7A),
            )
        )
    }
}

/** Material3 time picker wrapped in an AlertDialog — fully theme-aware. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerSheet(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
        title = { Text(stringResource(R.string.set_reminder_time)) },
        text = {
            // Center the time picker so it fits on small screens.
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = state)
            }
        }
    )
}

