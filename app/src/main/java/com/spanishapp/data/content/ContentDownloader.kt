package com.spanishapp.data.content

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Downloads versioned content packs from Firebase Storage.
 *
 * Layout in the bucket:
 *   content_packs/manifest.json
 *   content_packs/core_v1.json
 *   content_packs/lessons_a1_v1.json
 *   ...
 *
 * Flow:
 *   1. fetchManifest()  — pulls the manifest
 *   2. diff against ContentVersionStore
 *   3. download each changed pack with byte-level progress + bytes/sec
 *   4. verify sha256
 *   5. record new version in ContentVersionStore
 *
 * UI subscribes to [state] to render progress.
 */

sealed interface DownloadState {
    object Idle : DownloadState
    data class FetchingManifest(val attempt: Int = 1) : DownloadState
    data class Downloading(
        val currentPack: String,
        val currentDisplayName: String,
        val packIndex: Int,
        val packCount: Int,
        val packBytesDone: Long,
        val packBytesTotal: Long,
        val totalBytesDone: Long,
        val totalBytesTotal: Long,
        val bytesPerSecond: Long,
        val completedPacks: Set<String>,
    ) : DownloadState
    object Done : DownloadState
    data class Failed(val pack: String?, val cause: String) : DownloadState
}

data class DownloadedPack(
    val info: PackInfo,
    val file: File,
)

@Singleton
class ContentDownloader @Inject constructor(
    private val cacheRoot: File,
    private val versionStore: ContentVersionStore,
    private val firebaseStorage: FirebaseStorage,
) {
    /**
     * Root folder inside the Firebase Storage bucket where content packs live.
     * Empty string means bucket root. Files are addressed by name from this root.
     */
    var contentPath: String = ""

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    private val rootRef: StorageReference get() =
        if (contentPath.isEmpty()) firebaseStorage.reference
        else firebaseStorage.reference.child(contentPath)

    /**
     * Pull manifest, diff versions, download changed packs.
     * Returns list of downloaded pack files (only the ones that actually came down).
     */
    suspend fun syncContent(forceAll: Boolean = false): Result<List<DownloadedPack>> =
        withContext(Dispatchers.IO) {
            try {
                _state.value = DownloadState.FetchingManifest()
                val manifest = fetchManifest()

                val toDownload = manifest.packs.filter { p ->
                    forceAll || versionStore.getVersion(p.id) != p.version
                }
                if (toDownload.isEmpty()) {
                    _state.value = DownloadState.Done
                    return@withContext Result.success(emptyList())
                }

                val totalBytes = toDownload.sumOf { it.sizeBytes }
                val outFiles   = mutableListOf<DownloadedPack>()
                val completed  = mutableSetOf<String>()
                var totalDone  = 0L

                for ((idx, info) in toDownload.withIndex()) {
                    val file = downloadPack(
                        info = info,
                        onProgress = { packDone, packTotal, bps ->
                            _state.value = DownloadState.Downloading(
                                currentPack = info.id,
                                currentDisplayName = info.displayName,
                                packIndex = idx + 1,
                                packCount = toDownload.size,
                                packBytesDone = packDone,
                                packBytesTotal = packTotal,
                                totalBytesDone = totalDone + packDone,
                                totalBytesTotal = totalBytes,
                                bytesPerSecond = bps,
                                completedPacks = completed,
                            )
                        },
                    )
                    if (!verifySha256(file, info.sha256)) {
                        return@withContext Result.failure(
                            IllegalStateException("sha256 mismatch for ${info.id}")
                        )
                    }
                    totalDone += info.sizeBytes
                    outFiles += DownloadedPack(info, file)
                    completed += info.id
                    versionStore.setVersion(info.id, info.version)
                }

                _state.value = DownloadState.Done
                Result.success(outFiles)
            } catch (t: Throwable) {
                _state.value = DownloadState.Failed(
                    pack = (state.value as? DownloadState.Downloading)?.currentPack,
                    cause = t.message ?: t.javaClass.simpleName,
                )
                Result.failure(t)
            }
        }

    // ── Internals ─────────────────────────────────────────────────

    private suspend fun fetchManifest(): ContentManifest =
        suspendCancellableCoroutine { cont ->
            val ref = rootRef.child("manifest.json")
            // 64 KB cap — manifest is tiny
            val task = ref.getBytes(64L * 1024L)
            task.addOnSuccessListener { bytes ->
                runCatching {
                    json.decodeFromString(
                        ContentManifest.serializer(),
                        String(bytes, Charsets.UTF_8),
                    )
                }.onSuccess { cont.resume(it) }
                    .onFailure { cont.resumeWithException(it) }
            }
            task.addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun downloadPack(
        info: PackInfo,
        onProgress: (packDone: Long, packTotal: Long, bps: Long) -> Unit,
    ): File = suspendCancellableCoroutine { cont ->
        // info.url is a filename relative to the content folder (e.g. "core_v1.json").
        // If it ever starts with "content_packs/" or "/" we strip the prefix.
        val name = info.url.substringAfterLast('/')
        val ref = rootRef.child(name)
        val outFile = File(cacheRoot, "${info.id}_v${info.version}.json")
        outFile.parentFile?.mkdirs()

        val startMs = System.currentTimeMillis()
        val task = ref.getFile(outFile)
        task.addOnProgressListener { snap ->
            val done    = snap.bytesTransferred
            val total   = if (snap.totalByteCount > 0) snap.totalByteCount else info.sizeBytes
            val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
            val bps     = (done * 1000L) / elapsed
            onProgress(done, total, bps)
        }
        task.addOnSuccessListener { cont.resume(outFile) }
        task.addOnFailureListener { cont.resumeWithException(it) }
        cont.invokeOnCancellation { runCatching { task.cancel() } }
    }

    private fun verifySha256(file: File, expected: String): Boolean {
        if (expected.isBlank()) return true
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
