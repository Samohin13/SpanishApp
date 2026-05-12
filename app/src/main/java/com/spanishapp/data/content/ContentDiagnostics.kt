package com.spanishapp.data.content

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Read-only inspector for the local OTA cache.
 *
 * Used by Settings to show "N packs · X MB" so the owner can verify at a
 * glance that the download flow actually wrote files to disk — without
 * digging into adb shell or reading DataStore by hand.
 */
@Singleton
class ContentDiagnostics @Inject constructor(
    private val cacheRoot: File,
) {
    data class Pack(val name: String, val sizeBytes: Long, val modifiedAt: Long)

    data class Snapshot(
        val packs: List<Pack>,
        val totalBytes: Long,
    ) {
        val isEmpty: Boolean get() = packs.isEmpty()
    }

    fun snapshot(): Snapshot {
        val files = cacheRoot.listFiles { f -> f.isFile && f.name.endsWith(".json") }
            ?: emptyArray()
        val packs = files
            .sortedBy { it.name }
            .map { Pack(it.name, it.length(), it.lastModified()) }
        return Snapshot(packs = packs, totalBytes = packs.sumOf { it.sizeBytes })
    }

    companion object {
        fun formatBytes(b: Long): String = when {
            b < 1024 -> "$b Б"
            b < 1024 * 1024 -> "%.0f КБ".format(b / 1024.0)
            else -> "%.2f МБ".format(b / (1024.0 * 1024.0))
        }
    }
}
