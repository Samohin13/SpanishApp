package com.spanishapp.ui.settings

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.data.prefs.AppPreferences
import com.spanishapp.data.prefs.ThemeMode
import com.spanishapp.data.repository.AuthRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _isPhotoLoading = MutableStateFlow(false)
    val isPhotoLoading = _isPhotoLoading.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError = _nameError.asStateFlow()

    val userName = authRepository.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Estudiante")
    val userPhotoUrl = authRepository.userPhotoUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateName(name: String) {
        val error = AuthValidator.getNameError(name)
        if (error != null) {
            _nameError.value = error
            return
        }

        viewModelScope.launch {
            try {
                authRepository.setUserName(name)
                // Firestore sync
                auth.currentUser?.let { user ->
                    db.collection("users").document(user.uid).update("name", name).await()
                }
                _nameError.value = null
            } catch (e: Exception) {
                _nameError.value = "Ошибка сохранения"
            }
        }
    }

    fun clearNameError() {
        _nameError.value = null
    }

    fun uploadProfilePhoto(bitmap: Bitmap) {
        val currentUser = auth.currentUser ?: return
        _isPhotoLoading.value = true

        viewModelScope.launch {
            try {
                // 1. Сжатие и обработка (Стандарт: WebP, 500x500)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                val data = baos.toByteArray()

                // 2. Загрузка в Firebase Storage
                val storageRef = storage.reference.child("avatars/${currentUser.uid}.jpg")
                storageRef.putBytes(data).await()
                
                // 3. Получение URL и сохранение
                val downloadUrl = storageRef.downloadUrl.await().toString()
                authRepository.setUserPhotoUrl(downloadUrl)
                
                // 4. Обновление в Firestore для синхронизации
                db.collection("users")
                    .document(currentUser.uid)
                    .update("photoUrl", downloadUrl)
                    .await()

            } catch (e: Exception) {
                // Handle error
            } finally {
                _isPhotoLoading.value = false
            }
        }
    }

    val progress: StateFlow<UserProgressEntity> = userProgressDao.getProgress()
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProgressEntity())

    val ttsEnabled = appPreferences.ttsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val soundEffects = appPreferences.soundEffectsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val bgMusic = appPreferences.bgMusicEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val vibration = appPreferences.vibrationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val reminders = appPreferences.remindersEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val themeMode = appPreferences.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.AUTO)
    val fontSize = appPreferences.fontSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MEDIUM")

    fun toggleTts(enabled: Boolean) = viewModelScope.launch { appPreferences.setTtsEnabled(enabled) }
    fun toggleSoundEffects(enabled: Boolean) = viewModelScope.launch { appPreferences.setSoundEffectsEnabled(enabled) }
    fun toggleBgMusic(enabled: Boolean) = viewModelScope.launch { appPreferences.setBgMusicEnabled(enabled) }
    fun toggleVibration(enabled: Boolean) = viewModelScope.launch { appPreferences.setVibrationEnabled(enabled) }
    fun toggleReminders(enabled: Boolean) = viewModelScope.launch { appPreferences.setRemindersEnabled(enabled) }
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { appPreferences.setThemeMode(mode) }
    fun setFontSize(size: String) = viewModelScope.launch { appPreferences.setFontSize(size) }

    fun logout() = viewModelScope.launch { authRepository.setLoggedIn(false) }
    fun deleteAccount() = viewModelScope.launch { authRepository.setLoggedIn(false) }
    fun resetProgress() = viewModelScope.launch { userProgressDao.update(UserProgressEntity()) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = hiltViewModel()
) {
    val progress by vm.progress.collectAsStateWithLifecycle()
    val isPhotoLoading by vm.isPhotoLoading.collectAsStateWithLifecycle()
    val userPhotoUrl by vm.userPhotoUrl.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            vm.uploadProfilePhoto(bitmap)
        }
    }
    var showNameDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // ── Профиль ──
            SettingsSection("Профиль") {
                SettingsItem(
                    icon = Icons.Default.PhotoCamera,
                    title = "Изменить фото профиля",
                    summary = if (isPhotoLoading) "Загрузка..." else "Обновить аватар",
                    onClick = { photoPickerLauncher.launch("image/*") }
                )
                SettingsItem(Icons.Default.Edit, "Изменить имя и никнейм", progress.displayName) {
                    showNameDialog = true
                }
                SettingsItem(Icons.Default.BarChart, "Статистика прогресса") { navController.navigate("achievements") }
            }

            // ── Уведомления ──
            SettingsSection("Уведомления") {
                SettingsSwitchItem(Icons.Default.Notifications, "Напоминания о занятиях", reminders) { vm.toggleReminders(it) }
                SettingsItem(Icons.Default.Timeline, "Ежедневные уведомления и стрики")
            }

            // ── Звук и вибрация ──
            SettingsSection("Звук и вибрация") {
                SettingsSwitchItem(Icons.AutoMirrored.Filled.VolumeUp, "Эффекты звуков", soundEffects) { vm.toggleSoundEffects(it) }
                SettingsSwitchItem(Icons.Default.RecordVoiceOver, "Голос диктора (TTS)", ttsEnabled) { vm.toggleTts(it) }
                SettingsSwitchItem(Icons.Default.MusicNote, "Музыка на фоне", bgMusic) { vm.toggleBgMusic(it) }
                SettingsSwitchItem(Icons.Default.Vibration, "Вибрация и тактильная отдача", vibration) { vm.toggleVibration(it) }
            }

            // ── Внешний вид ──
            SettingsSection("Внешний вид") {
                SettingsItem(Icons.Default.Palette, "Тёмная / светлая тема", themeMode.name)
                SettingsItem(Icons.Default.TextFields, "Размер шрифта", fontSize)
            }

            // ── Языки ──
            SettingsSection("Языки") {
                SettingsItem(Icons.Default.Language, "Язык интерфейса", "Русский")
                SettingsItem(Icons.Default.Translate, "Изучаемый язык", "Испанский")
            }

            // ── Управление аккаунтом ──
            SettingsSection("Управление аккаунтом") {
                SettingsItem(Icons.AutoMirrored.Filled.Logout, "Выйти из аккаунта", textColor = MaterialTheme.colorScheme.error) {
                    showLogoutDialog = true
                }
                SettingsItem(Icons.Default.DeleteForever, "Удалить аккаунт", textColor = MaterialTheme.colorScheme.error) {
                    showDeleteDialog = true
                }
            }

            // ── Подписка ──
            SettingsSection("Подписка") {
                SettingsItem(Icons.Default.Star, "Управление подпиской", "Бесплатный план")
                SettingsItem(Icons.Default.Restore, "Восстановление покупок")
            }

            // ── Конфиденциальность ──
            SettingsSection("Конфиденциальность и данные") {
                SettingsItem(Icons.Default.PrivacyTip, "Политика конфиденциальности") {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                    context.startActivity(intent)
                }
                SettingsItem(Icons.Default.Description, "Условия использования")
            }

            // ── О приложении ──
            SettingsSection("О приложении") {
                SettingsItem(Icons.Default.Info, "Версия приложения", "1.4.1")
                SettingsItem(Icons.AutoMirrored.Filled.Help, "Помощь и поддержка")
            }

            // ── Дополнительно ──
            SettingsSection("Дополнительно") {
                SettingsItem(Icons.Default.Leaderboard, "Лидерборды и соцсети") { navController.navigate("achievements") }
                SettingsItem(Icons.Default.Refresh, "Сброс прогресса", textColor = MaterialTheme.colorScheme.error) {
                    showResetDialog = true
                }
                SettingsItem(Icons.Default.Download, "Экспорт данных")
            }
        }
    }

    // ── Dialogs ──
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход") },
            text = { Text("Вы уверены, что хотите выйти из аккаунта?") },
            confirmButton = {
                Button(onClick = { vm.logout(); showLogoutDialog = false }) { Text("Выйти") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление аккаунта") },
            text = { Text("Это действие нельзя отменить. Все ваши данные будут удалены навсегда. Вы уверены?") },
            confirmButton = {
                Button(
                    onClick = { vm.deleteAccount(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сброс прогресса") },
            text = { Text("Весь ваш прогресс обучения будет удален навсегда. Вы уверены?") },
            confirmButton = {
                Button(
                    onClick = { vm.resetProgress(); showResetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Сбросить") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showNameDialog) {
        var tempName by remember { mutableStateOf(progress.displayName) }
        val nameError by vm.nameError.collectAsStateWithLifecycle()

        AlertDialog(
            onDismissRequest = { 
                showNameDialog = false
                vm.clearNameError()
            },
            title = { Text("Изменить имя") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { 
                            tempName = it
                            vm.clearNameError()
                        },
                        label = { Text("Ваше имя") },
                        isError = nameError != null,
                        supportingText = { if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { 
                    vm.updateName(tempName)
                    if (AuthValidator.getNameError(tempName) == null) {
                        showNameDialog = false
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showNameDialog = false
                    vm.clearNameError()
                }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    summary: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = if (textColor == MaterialTheme.colorScheme.error) textColor else MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor)
            if (summary != null) {
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
        if (onClick != null) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = Color.LightGray)
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
