package com.spanishapp.ui.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.domain.checkpoint.CheckpointRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Готовит и шарит milestone-картинку прохождения чекпоинта.
 *
 * Поток:
 *   load(args) → fetch CheckpointData + UserProgress → build ProgressShareData
 *   share() → render() → FileProvider URI + ACTION_SEND chooser
 *   save() → render() → MediaStore (Pictures/ESPEAK/)
 *
 * Аргументы прохождения (tier, percent, xp, rounds, timeMin) приходят через
 * nav-аргументы из CheckpointScreen.ResultView, потому что CheckpointState
 * хранится in-memory в CheckpointViewModel и теряется при навигации.
 *
 * Юридически: см. [ProgressShareData] — никаких слов «certificate»/«diploma».
 */
@HiltViewModel
class ShareViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val userProgressDao: UserProgressDao,
    private val checkpointRepository: CheckpointRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ShareUiState>(ShareUiState.Loading)
    val state: StateFlow<ShareUiState> = _state.asStateFlow()

    private var cached: ProgressShareData? = null

    fun load(args: ShareArgs) {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) { buildData(args) }
                if (data == null) {
                    _state.value = ShareUiState.Error("Нет данных о прохождении")
                    return@launch
                }
                cached = data
                _state.value = ShareUiState.Ready(data)
            } catch (e: Exception) {
                _state.value = ShareUiState.Error(e.message ?: "Ошибка")
            }
        }
    }

    /**
     * Рендерит → сохраняет во временный файл в cacheDir → запускает
     * системный share-chooser (WhatsApp/Telegram/Instagram/…).
     */
    fun share() {
        val data = cached ?: return
        viewModelScope.launch {
            val uri = withContext(Dispatchers.Default) {
                val bmp = ProgressImageRenderer.render(data)
                val file = saveToCacheFile(bmp, data.cpId)
                bmp.recycle()
                FileProvider.getUriForFile(
                    ctx,
                    ctx.packageName + ".fileprovider",
                    file
                )
            }
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Я изучаю испанский с ESPEAK 🇪🇸 https://play.google.com/store/apps/details?id=com.espeak.app"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(send, "Поделиться").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ctx.startActivity(chooser)
        }
    }

    /**
     * Сохраняет картинку в галерею (Pictures/ESPEAK/) через MediaStore.
     * Работает с API 26+ (на API 28- — добавляет в стандартный Pictures без
     * подкаталога).
     */
    fun save() {
        val data = cached ?: return
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val bmp = ProgressImageRenderer.render(data)
                    val uri = saveToGallery(bmp, data.cpId)
                    bmp.recycle()
                    uri
                }
            }
            _state.value = when {
                result.isSuccess && result.getOrNull() != null ->
                    ShareUiState.Saved(data, "Сохранено в Pictures/ESPEAK/")
                else -> ShareUiState.Ready(data) // молча fallback
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private suspend fun buildData(args: ShareArgs): ProgressShareData? {
        val cp = runCatching { checkpointRepository.getById(args.cpId) }.getOrNull() ?: return null

        val progress = userProgressDao.getProgressOnce()
        val name = progress?.displayName?.takeIf { it.isNotBlank() } ?: "Студент"
        val today = SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(Date())

        return ProgressShareData(
            userName = name,
            cpId = args.cpId,
            cpTitle = cp.titleRu,
            cefr = cp.cefr,
            isModuleFinal = ProgressShareData.isFinalForCpId(args.cpId),
            tier = args.tier,
            accuracy = args.percent,
            xpEarned = args.xp,
            totalRounds = args.totalRounds,
            timeMinutes = args.timeMinutes.coerceAtLeast(1),
            dateLocalized = today,
        )
    }

    private fun saveToCacheFile(bmp: Bitmap, cpId: String): File {
        val dir = File(ctx.cacheDir, "shares").apply { mkdirs() }
        // file_name: espeak_progress_TIMESTAMP.png (НЕ certificate — юр-требование)
        val file = File(dir, "espeak_progress_${cpId}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { fos ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        return file
    }

    private fun saveToGallery(bmp: Bitmap, cpId: String): Uri? {
        val fileName = "espeak_progress_${cpId}_${System.currentTimeMillis()}.png"
        val resolver = ctx.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ESPEAK")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val uri = resolver.insert(collection, values) ?: return null
        resolver.openOutputStream(uri)?.use { os ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        return uri
    }
}

/** Nav-аргументы из CheckpointScreen.ResultView — все примитивы для query-string. */
data class ShareArgs(
    val cpId: String,
    val tier: String,           // "gold"/"silver"/"bronze"
    val percent: Int,
    val xp: Int,
    val totalRounds: Int,
    val timeMinutes: Int,
)

sealed class ShareUiState {
    data object Loading : ShareUiState()
    data class Ready(val data: ProgressShareData) : ShareUiState()
    data class Saved(val data: ProgressShareData, val message: String) : ShareUiState()
    data class Error(val message: String) : ShareUiState()
}
