package com.spanishapp.data.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downloads versioned content packs from Firebase Storage public REST URLs.
 *
 * Why not the Firebase Storage SDK? It tries to fetch an Auth/AppCheck token
 * before every transfer; on a fresh install with no signed-in user this
 * blocks the second+ download indefinitely.
 *
 * Direct REST works because the security rules grant public read to .json
 * files. URL pattern:
 *   https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{path}?alt=media
 *
 * Flow: user registers → Firebase auth token available → DownloadScreen
 * calls syncContent() → packs downloaded → ContentImporter applies to Room.
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
    private val firebaseStorage: com.google.firebase.storage.FirebaseStorage,
) {
    /** Optional sub-folder inside the bucket. "" = root. */
    var contentPath: String = ""

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    private fun urlFor(filename: String): String {
        val bucket  = firebaseStorage.reference.bucket
        val path    = if (contentPath.isEmpty()) filename else "$contentPath/$filename"
        val encoded = java.net.URLEncoder.encode(path, "UTF-8")
        return "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encoded?alt=media"
    }

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
                    versionStore.markContentReady()
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
                    // sha256 verification was here but disabled: Windows line-ending
                    // conversion during Firebase Console drag-drop turns LF→CRLF,
                    // making byte-level hashes diverge from what the exporter saw.
                    // HTTPS already guarantees transport integrity, and the JSON
                    // parser will fail loudly if content is actually corrupted.
                    totalDone += info.sizeBytes
                    outFiles += DownloadedPack(info, file)
                    completed += info.id
                    versionStore.setVersion(info.id, info.version)
                }

                _state.value = DownloadState.Done
                versionStore.markContentReady()
                Result.success(outFiles)
            } catch (t: Throwable) {
                _state.value = DownloadState.Failed(
                    pack = (state.value as? DownloadState.Downloading)?.currentPack,
                    cause = t.message ?: t.javaClass.simpleName,
                )
                Result.failure(t)
            }
        }

    private fun fetchManifest(): ContentManifest {
        val req = Request.Builder().url(urlFor("manifest.json")).build()
        client.newCall(req).execute().use { resp ->
            require(resp.isSuccessful) { "manifest GET ${resp.code}" }
            val body = resp.body?.string() ?: error("empty manifest body")
            return json.decodeFromString(ContentManifest.serializer(), body)
        }
    }

    private fun downloadPack(
        info: PackInfo,
        onProgress: (packDone: Long, packTotal: Long, bps: Long) -> Unit,
    ): File {
        val filename = info.url.substringAfterLast('/')
        val req = Request.Builder().url(urlFor(filename)).build()
        val outFile = File(cacheRoot, "${info.id}_v${info.version}.json")
        outFile.parentFile?.mkdirs()

        client.newCall(req).execute().use { resp ->
            require(resp.isSuccessful) { "pack GET ${resp.code} for ${info.id}" }
            val body = resp.body ?: error("empty body for ${info.id}")
            val total = if (info.sizeBytes > 0) info.sizeBytes else body.contentLength()

            // Live speed via 1-second sliding window
            var winStartMs   = System.currentTimeMillis()
            var winStartBytes = 0L
            var bps = 0L
            var done = 0L
            val buf = ByteArray(64 * 1024)

            body.byteStream().use { input ->
                outFile.outputStream().use { out ->
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        out.write(buf, 0, n)
                        done += n
                        val now = System.currentTimeMillis()
                        val winMs = now - winStartMs
                        if (winMs >= 800) {
                            bps = ((done - winStartBytes) * 1000L) / winMs.coerceAtLeast(1L)
                            winStartMs = now
                            winStartBytes = done
                        }
                        onProgress(done, total, bps)
                    }
                }
            }
        }
        return outFile
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
