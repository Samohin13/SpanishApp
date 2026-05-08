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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.spanishapp.data.prefs.ThemeMode
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
    private val appLockManager: AppLockManager
) : ViewModel() {

    val appLockEnabled = appLockPreferences.isEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val biometricUsable: Boolean get() = appLockManager.biometricAvailability().isUsable

    fun setAppLockEnabled(enabled: Boolean) = viewModelScope.launch {
        appLockPreferences.setEnabled(enabled)
        // Включая впервые — считаем что эта сессия уже разблокирована,
        // чтобы юзер не отправился на app_lock экран сразу же после клика.
        if (enabled) appLockManager.markUnlocked()
    }

    private val storage = FirebaseStorage.getInstance("gs://spanishapp-35092.firebasestorage.app")
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

                // Путь в Storage (упрощенный)
                val storageRef = storage.reference.child("avatars/${currentUser.uid}.jpg")
                
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
    val themeMode = appPreferences.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.AUTO)
    val fontSize = appPreferences.fontSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MEDIUM")
    val uiLanguage = appPreferences.uiLanguage.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    fun setUiLanguage(lang: String) = viewModelScope.launch { appPreferences.setUiLanguage(lang) }
    fun toggleTts(e: Boolean) = viewModelScope.launch { appPreferences.setTtsEnabled(e) }
    fun toggleSoundEffects(e: Boolean) = viewModelScope.launch { appPreferences.setSoundEffectsEnabled(e) }
    fun toggleVibration(e: Boolean) = viewModelScope.launch { appPreferences.setVibrationEnabled(e) }

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

    fun setThemeMode(m: ThemeMode) = viewModelScope.launch { appPreferences.setThemeMode(m) }
    fun setFontSize(s: String) = viewModelScope.launch { appPreferences.setFontSize(s) }
    fun logout() = viewModelScope.launch { authRepository.setLoggedIn(false) }
    fun deleteAccount() = viewModelScope.launch { authRepository.setLoggedIn(false) }
    fun resetProgress() = viewModelScope.launch { userProgressDao.update(UserProgressEntity()) }

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
    val progress by vm.progress.collectAsStateWithLifecycle()
    val userName by vm.userName.collectAsStateWithLifecycle()
    val isPhotoLoading by vm.isPhotoLoading.collectAsStateWithLifecycle()
    val userPhotoUrl by vm.userPhotoUrl.collectAsStateWithLifecycle()
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val soundEffects by vm.soundEffects.collectAsStateWithLifecycle()
    val ttsEnabled by vm.ttsEnabled.collectAsStateWithLifecycle()
    val vibration by vm.vibration.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val fontSize by vm.fontSize.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
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

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
                cropImageLauncher.launch(CropImageContractOptions(null, CropImageOptions(
                    imageSourceIncludeGallery = true, imageSourceIncludeCamera = true,
                    guidelines = CropImageView.Guidelines.ON, aspectRatioX = 1, aspectRatioY = 1,
                    fixAspectRatio = true, cropShape = CropImageView.CropShape.OVAL
                )))
        } else {
            Toast.makeText(context, context.getString(R.string.set_camera_perm_hint), Toast.LENGTH_SHORT).show()
        }
    }

    var showNameDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(bottom = 32.dp)
        ) {
            // ── Шапка профиля ──
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        if (isPhotoLoading) {
                            Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(40.dp)) }
                        }
                        
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(userPhotoUrl ?: "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y")
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.DISABLED) // Чтобы сразу видеть новое фото
                                .build(),
                            contentDescription = stringResource(R.string.set_avatar_cd),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = { 
                            val status = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (status == PackageManager.PERMISSION_GRANTED) {
                                cropImageLauncher.launch(CropImageContractOptions(null, CropImageOptions(
                                    imageSourceIncludeGallery = true, imageSourceIncludeCamera = true,
                                    guidelines = CropImageView.Guidelines.ON, aspectRatioX = 1, aspectRatioY = 1,
                                    fixAspectRatio = true, cropShape = CropImageView.CropShape.OVAL
                                )))
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
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
            SettingsSection(stringResource(R.string.settings_section_profile)) {
                SettingsItem(Icons.Default.Edit, stringResource(R.string.settings_change_name), userName?.takeIf { it.isNotBlank() } ?: progress.displayName) { showNameDialog = true }
                SettingsItem(Icons.Default.Translate, stringResource(R.string.set_spanish_level), when(progress.currentLevel) {
                    "A1" -> stringResource(R.string.set_level_a1)
                    "A2" -> stringResource(R.string.set_level_a2)
                    "B1" -> stringResource(R.string.set_level_b1)
                    "B2" -> stringResource(R.string.set_level_b2)
                    else -> progress.currentLevel
                }) { showLevelDialog = true }
                SettingsItem(Icons.Default.Timer, stringResource(R.string.settings_daily_goal), "${progress.dailyGoalMinutes} ${stringResource(R.string.settings_minutes_short)}") { showGoalDialog = true }
                SettingsItem(Icons.Default.BarChart, stringResource(R.string.set_progress_stats)) { navController.navigate("achievements") }
            }

            val reminderHour by vm.reminderHour.collectAsStateWithLifecycle()
            val reminderMinute by vm.reminderMinute.collectAsStateWithLifecycle()
            SettingsSection(stringResource(R.string.settings_section_notifications)) {
                SettingsSwitchItem(
                    Icons.Default.Notifications,
                    stringResource(R.string.set_reminders),
                    reminders
                ) { vm.toggleReminders(context, it) }
                if (reminders) {
                    SettingsItem(
                        Icons.Default.AccessTime,
                        stringResource(R.string.set_reminder_time),
                        "%02d:%02d".format(reminderHour, reminderMinute)
                    ) {
                        // Системный TimePickerDialog — без зависимостей и без
                        // экспериментальных Material3 API.
                        android.app.TimePickerDialog(
                            context,
                            { _, h, m -> vm.setReminderTime(context, h, m) },
                            reminderHour,
                            reminderMinute,
                            true
                        ).show()
                    }
                }
            }

            SettingsSection(stringResource(R.string.settings_section_sound)) {
                SettingsSwitchItem(Icons.AutoMirrored.Filled.VolumeUp, stringResource(R.string.settings_sound_effects), soundEffects) { vm.toggleSoundEffects(it) }
                SettingsSwitchItem(Icons.Default.RecordVoiceOver, stringResource(R.string.settings_voice_announcer), ttsEnabled) { vm.toggleTts(it) }
                SettingsSwitchItem(Icons.Default.Vibration, stringResource(R.string.set_vibration), vibration) { vm.toggleVibration(it) }
                SettingsItem(Icons.Default.InterpreterMode, stringResource(R.string.set_voice_setup)) { navController.navigate("settings_voice") }
            }

            SettingsSection(stringResource(R.string.settings_section_appearance)) {
                val themeLabel = when(themeMode) {
                    ThemeMode.AUTO -> stringResource(R.string.set_theme_system)
                    ThemeMode.LIGHT -> stringResource(R.string.set_theme_light)
                    ThemeMode.DARK -> stringResource(R.string.set_theme_dark)
                }
                val fontLabel = when(fontSize) {
                    "SMALL" -> stringResource(R.string.set_font_small)
                    "MEDIUM" -> stringResource(R.string.set_font_medium)
                    "LARGE" -> stringResource(R.string.set_font_large)
                    else -> fontSize
                }
                SettingsItem(Icons.Default.Palette, stringResource(R.string.set_theme_setting), themeLabel) { showThemeDialog = true }
                SettingsItem(Icons.Default.TextFields, stringResource(R.string.set_font_title), fontLabel) { showFontDialog = true }
            }

            SettingsSection(stringResource(R.string.settings_section_languages)) {
                val uiLang by vm.uiLanguage.collectAsStateWithLifecycle()
                val uiLangLabel = when (uiLang) {
                    "ru" -> stringResource(R.string.set_lang_ru)
                    "en" -> stringResource(R.string.set_lang_en)
                    else -> stringResource(R.string.set_lang_system)
                }
                SettingsItem(Icons.Default.Language, stringResource(R.string.settings_language_ui), uiLangLabel) {
                    showLanguageDialog = true
                }
                SettingsItem(Icons.Default.Public, stringResource(R.string.settings_language_target), stringResource(R.string.settings_target_spanish)) { /* Пока только один язык */ }
            }

            SettingsSection(stringResource(R.string.settings_section_subscription)) {
                SettingsItem(Icons.Default.Star, stringResource(R.string.settings_subscription_manage)) { /* Открыть маркет или экран оплаты */ }
                SettingsItem(Icons.Default.Restore, stringResource(R.string.settings_subscription_restore)) { /* Логика восстановления */ }
            }

            SettingsSection(stringResource(R.string.settings_section_help)) {
                SettingsItem(Icons.AutoMirrored.Filled.HelpOutline, stringResource(R.string.settings_help_center)) { /* Ссылка на FAQ или поддержку */ }
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

            SettingsSection(stringResource(R.string.settings_section_privacy)) {
                SettingsItem(Icons.Default.PrivacyTip, stringResource(R.string.settings_privacy_policy)) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://github.com/Samohin13/SpanishApp/blob/master/PRIVACY_POLICY.md"
                    ))
                    runCatching { context.startActivity(intent) }
                }
                SettingsItem(Icons.Default.Description, stringResource(R.string.settings_terms)) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://github.com/Samohin13/SpanishApp/blob/master/PRIVACY_POLICY.md"
                    ))
                    runCatching { context.startActivity(intent) }
                }
                SettingsItem(Icons.Default.FileUpload, stringResource(R.string.settings_export)) { /* Логика экспорта */ }
            }

            SettingsSection(stringResource(R.string.settings_section_other)) {
                SettingsItem(Icons.Default.Leaderboard, stringResource(R.string.settings_leaderboards)) {
                    navController.navigate("leaderboard")
                }
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

            // ── О приложении ──
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.set_about_text),
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

    if (showThemeDialog) {
        val themeAuto = stringResource(R.string.set_theme_system)
        val themeLight = stringResource(R.string.set_theme_light)
        val themeDark = stringResource(R.string.set_theme_dark)
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.set_theme_title)) },
            text = {
                Column {
                    ThemeMode.values().forEach { mode ->
                        val label = when(mode) {
                            ThemeMode.AUTO -> themeAuto
                            ThemeMode.LIGHT -> themeLight
                            ThemeMode.DARK -> themeDark
                        }
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.setThemeMode(mode); showThemeDialog = false }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = themeMode == mode, onClick = { vm.setThemeMode(mode); showThemeDialog = false })
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {}
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
        val levels = listOf(
            "A1" to stringResource(R.string.set_level_a1_short),
            "A2" to stringResource(R.string.set_level_a2_short),
            "B1" to stringResource(R.string.set_level_b1_short),
            "B2" to stringResource(R.string.set_level_b2_short)
        )
        AlertDialog(
            onDismissRequest = { showLevelDialog = false },
            title = { Text(stringResource(R.string.set_spanish_level)) },
            text = {
                Column {
                    levels.forEach { (key, label) ->
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.updateLevel(key); showLevelDialog = false }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = progress.currentLevel == key, onClick = { vm.updateLevel(key); showLevelDialog = false })
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {}
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
                    val options = listOf(
                        "system" to stringResource(R.string.set_lang_system),
                        "ru" to stringResource(R.string.set_lang_ru),
                        "en" to stringResource(R.string.set_lang_en)
                    )
                    options.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (code != uiLang) {
                                        vm.setUiLanguage(code)
                                        showLanguageDialog = false
                                        // Перезапускаем активность чтобы перечитать локаль.
                                        activity?.recreate()
                                    } else {
                                        showLanguageDialog = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = code == uiLang, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(label)
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
                OutlinedTextField(
                    value = tempName, onValueChange = { tempName = it; vm.clearNameError() },
                    label = { Text(stringResource(R.string.set_dlg_name_label)) }, isError = nameError != null,
                    supportingText = { if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
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
    Row(modifier = Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
    Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
