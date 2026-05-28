package com.spanishapp.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранилище голосовых сообщений. Файлы лежат в filesDir/voice_messages/
 * с UUID-именем + расширением .m4a (AAC-LC в MP4 контейнере — кросс-платформенно).
 *
 * Запись делает [VoiceRecorder], воспроизведение — [VoicePlayer].
 * Storage только генерирует пути и удаляет файлы.
 */
@Singleton
class VoiceMessageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val dir: File by lazy {
        File(context.filesDir, "voice_messages").apply { mkdirs() }
    }

    /** Новый путь для записи. Файл ещё не существует. */
    fun newFilePath(): String {
        val name = "vm_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.m4a"
        return File(dir, name).absolutePath
    }

    /** Безопасное удаление (no-op если файла нет). */
    fun delete(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return runCatching { File(path).delete() }.getOrDefault(false)
    }

    /** Размер всех голосовых сообщений в байтах (для индикатора в Settings). */
    fun totalSizeBytes(): Long {
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /** Удалить все голосовые (для "очистить кеш"). */
    fun deleteAll(): Int {
        val files = dir.listFiles() ?: return 0
        var deleted = 0
        files.forEach { if (it.delete()) deleted++ }
        return deleted
    }
}
