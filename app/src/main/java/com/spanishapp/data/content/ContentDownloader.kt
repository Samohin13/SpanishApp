package com.spanishapp.data.content

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

/**
 * Downloads content packs from Firebase Storage with live progress.
 *
 * On first launch and on subsequent app starts, the caller:
 *   1. fetches the manifest (cheap, <2 KB)
 *   2. compares each pack's version against locally-cached versions
 *   3. downloads any pack whose version moved
 *   4. parses + writes the payload into Room via the per-pack seeders
 *
 * Progress is exposed as a StateFlow<DownloadState> the UI can collect.
 */

sealed interface DownloadState {
    object Idle : DownloadState
    data class FetchingManifest(val attempt: Int = 1) : DownloadState
    data class Downloading(
        val currentPack: String,
        val packIndex: Int,
        val packCount: Int,
        val packBytesDone: Long,
        val packBytesTotal: Long,
        val totalBytesDone: Long,
        val totalBytesTotal: Long,
        val bytesPerSecond: Long,
        val completedPacks: Set<String>
    ) : DownloadState
    data class Finalizing(val pack: String) : DownloadState
    object Done : DownloadState
    data class Failed(val pack: String?, val cause: String) : DownloadState
}

@Singleton
class ContentDownloader @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val cacheRoot: File,
    private val versionStore: ContentVersionStore,
) {
    private val client = OkHttpClient.Builder().build()
    private val json   = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    /**
     * Pull the manifest from Firebase Storage, diff against local versions,
     * and download every pack whose version moved. Suspends until done.
     */
    suspend fun syncContent(forceAll: Boolean = false): Result<List<DownloadedPack>> =
        withContext(Dispatchers.IO) {
            // TODO Phase 0 step 3: implement manifest fetch + per-pack download
            // with progress callbacks. Skeleton only for this commit.
            _state.value = DownloadState.Idle
            Result.success(emptyList())
        }

    // ── Internals (to be filled in next commit) ────────────────────

    private suspend fun fetchManifest(): ContentManifest =
        TODO("fetch content/manifest.json via Firebase Storage SDK")

    private suspend fun downloadPack(
        info: PackInfo,
        onProgress: (bytesRead: Long, total: Long, bps: Long) -> Unit
    ): File = TODO("HTTP GET with okhttp, stream to cacheRoot, report progress")

    private fun verifySha256(file: File, expected: String): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                digest.update(buf, 0, n)
            }
        }
        val hex = digest.digest().joinToString("") { "%02x".format(it) }
        return hex.equals(expected, ignoreCase = true)
    }
}

/** One pack that finished downloading and is ready to be applied to Room. */
data class DownloadedPack(
    val info: PackInfo,
    val file: File,
)
