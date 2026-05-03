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
                _errorEvent.emit("Ошибка сохранения в облако: ${e.localizedMessage}")
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
    val bgMusic = appPreferences.bgMusicEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val vibration = appPreferences.vibrationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val reminders = appPreferences.remindersEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val themeMode = appPreferences.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.AUTO)
    val fontSize = appPreferences.fontSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MEDIUM")
    fun toggleTts(e: Boolean) = viewModelScope.launch { appPreferences.setTtsEnabled(e) }
    fun toggleSoundEffects(e: Boolean) = viewModelScope.launch { appPreferences.setSoundEffectsEnabled(e) }
    fun toggleBgMusic(e: Boolean) = viewModelScope.launch { appPreferences.setBgMusicEnabled(e) }
    fun toggleVibration(e: Boolean) = viewModelScope.launch { appPreferences.setVibrationEnabled(e) }
    fun toggleReminders(e: Boolean) = viewModelScope.launch { appPreferences.setRemindersEnabled(e) }
    fun setThemeMode(m: ThemeMode) = viewModelScope.launch { appPreferences.setThemeMode(m) }
    fun setFontSize(s: String) = viewModelScope.launch { appPreferences.setFontSize(s) }
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
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val soundEffects by vm.soundEffects.collectAsStateWithLifecycle()
    val ttsEnabled by vm.ttsEnabled.collectAsStateWithLifecycle()
    val bgMusic by vm.bgMusic.collectAsStateWithLifecycle()
    val vibration by vm.vibration.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val fontSize by vm.fontSize.collectAsStateWithLifecycle()
    
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.errorEvent.collect { error -> Toast.makeText(context, error, Toast.LENGTH_LONG).show() }
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
            Toast.makeText(context, "Разрешите доступ к камере в настройках", Toast.LENGTH_SHORT).show()
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
                            contentDescription = "Аватар",
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
            SettingsSection("Профиль") {
                SettingsItem(Icons.Default.Edit, "Изменить имя", progress.displayName) { showNameDialog = true }
                SettingsItem(Icons.Default.BarChart, "Статистика") { navController.navigate("achievements") }
            }

            SettingsSection("Уведомления") {
                SettingsSwitchItem(Icons.Default.Notifications, "Напоминания", reminders) { vm.toggleReminders(it) }
            }

            SettingsSection("Звук и вибрация") {
                SettingsSwitchItem(Icons.AutoMirrored.Filled.VolumeUp, "Звуковые эффекты", soundEffects) { vm.toggleSoundEffects(it) }
                SettingsSwitchItem(Icons.Default.RecordVoiceOver, "Диктор (TTS)", ttsEnabled) { vm.toggleTts(it) }
                SettingsSwitchItem(Icons.Default.Vibration, "Вибрация", vibration) { vm.toggleVibration(it) }
            }

            SettingsSection("Аккаунт") {
                SettingsItem(Icons.AutoMirrored.Filled.Logout, "Выйти", textColor = MaterialTheme.colorScheme.error) { showLogoutDialog = true }
                SettingsItem(Icons.Default.DeleteForever, "Удалить аккаунт", textColor = MaterialTheme.colorScheme.error) { showDeleteDialog = true }
            }
        }
    }

    // ── Диалоги ──
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход") },
            text = { Text("Выйти из аккаунта?") },
            confirmButton = { Button(onClick = { vm.logout(); showLogoutDialog = false }) { Text("Выйти") } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Отмена") } }
        )
    }

    if (showNameDialog) {
        var tempName by remember { mutableStateOf(progress.displayName) }
        val nameError by vm.nameError.collectAsStateWithLifecycle()
        AlertDialog(
            onDismissRequest = { showNameDialog = false; vm.clearNameError() },
            title = { Text("Изменить имя") },
            text = {
                OutlinedTextField(
                    value = tempName, onValueChange = { tempName = it; vm.clearNameError() },
                    label = { Text("Имя") }, isError = nameError != null,
                    supportingText = { if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { 
                    vm.updateName(tempName)
                    if (AuthValidator.getNameError(tempName) == null) showNameDialog = false
                }) { Text("Сохранить") }
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
            if (summary != null) Text(summary, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        if (onClick != null) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = Color.LightGray)
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
