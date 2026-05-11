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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downloads versioned content packs from a public CDN (currently GitHub Pages
 * at `https://samohin13.github.io/SpanishApp/content_packs/`).
 *
 * Flow:
 *   1. GET manifest.json
 *   2. For each pack: compare version against [ContentVersionStore]; skip if equal
 *   3. Download changed packs with byte-level progress + speed
 *   4. Verify sha256
 *   5. Save to app cache dir; caller applies to Room afterwards
 *
 * Exposes [state] as a StateFlow the UI can collect.
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

/** Per-pack info handed back to the caller after a successful download. */
data class DownloadedPack(
    val info: PackInfo,
    val file: File,
)

@Singleton
class ContentDownloader @Inject constructor(
    private val cacheRoot: File,
    private val versionStore: ContentVersionStore,
) {
    /** Base URL where packs live. Override via withBase() for tests. */
    var baseUrl: String = "https://samohin13.github.io/SpanishApp/content_packs/"

    private val client = OkHttpClient.Builder().build()
    private val json   = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    /**
     * Pull the manifest, diff against local versions, download every pack
     * whose version moved. Returns the list of downloaded pack files.
     */
    suspend fun syncContent(forceAll: Boolean = false): Result<List<DownloadedPack>> =
        withContext(Dispatchers.IO) {
            try {
                _state.value = DownloadState.FetchingManifest()
                val manifest = fetchManifest()

                // Decide which packs need download
                val toDownload = manifest.packs.filter { p ->
                    forceAll || versionStore.getVersion(p.id) != p.version
                }
                if (toDownload.isEmpty()) {
                    _state.value = DownloadState.Done
                    return@withContext Result.success(emptyList())
                }

                val totalBytes  = toDownload.sumOf { it.sizeBytes }
                val outFiles    = mutableListOf<DownloadedPack>()
                val completed   = mutableSetOf<String>()
                var totalDone   = 0L

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

    private fun fetchManifest(): ContentManifest {
        val req = Request.Builder().url(baseUrl + "manifest.json").build()
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
        val url = if (info.url.startsWith("http")) info.url else baseUrl + info.url
        val req = Request.Builder().url(url).build()
        val outFile = File(cacheRoot, "${info.id}_v${info.version}.json")
        outFile.parentFile?.mkdirs()

        client.newCall(req).execute().use { resp ->
            require(resp.isSuccessful) { "pack GET ${resp.code} for ${info.id}" }
            val body = resp.body ?: error("empty body for ${info.id}")
            val total = if (info.sizeBytes > 0) info.sizeBytes else body.contentLength()

            val start = System.currentTimeMillis()
            var done  = 0L
            val buf   = ByteArray(64 * 1024)
            body.byteStream().use { input ->
                outFile.outputStream().use { out ->
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        out.write(buf, 0, n)
                        done += n
                        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1)
                        val bps = (done * 1000L) / elapsed
                        onProgress(done, total, bps)
                    }
                }
            }
        }
        return outFile
    }

    private fun verifySha256(file: File, expected: String): Boolean {
        if (expected.isBlank()) return true   // tolerate dev manifests
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
