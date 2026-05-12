package com.spanishapp.service

import android.content.Context
import androidx.room.Room
import androidx.work.*
import com.spanishapp.data.content.ContentDownloader
import com.spanishapp.data.content.ContentImporter
import com.spanishapp.data.content.ContentVersionStore
import com.spanishapp.data.db.AppDatabase
import kotlinx.coroutines.CancellationException
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Background content sync — runs once per day on Wi-Fi only.
 *
 * Industry-standard approach (same as Duolingo / Babbel):
 *   1. Fetch manifest.json from GitHub Pages CDN.
 *   2. Compare pack versions with locally stored versions (DataStore).
 *   3. Download only changed packs (delta sync).
 *   4. Apply new words to Room DB via ContentImporter (IGNORE strategy —
 *      existing SM-2 progress is never touched).
 *   5. Everything is silent — no loading screen, no user interaction.
 *
 * Uses the same no-Hilt pattern as RatingDecayWorker to avoid KSP issues.
 */
class ContentSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx       = applicationContext
        val cacheRoot = File(ctx.filesDir, "content_packs").apply { mkdirs() }

        // Build dependencies manually (no Hilt in workers — same as RatingDecayWorker)
        val versionStore = ContentVersionStore(ctx)
        val downloader   = ContentDownloader(
            cacheRoot,
            versionStore,
            com.google.firebase.storage.FirebaseStorage.getInstance(),
        )

        return try {
            val packs = downloader.syncContent().getOrThrow()

            if (packs.isNotEmpty()) {
                // Open DB directly — same pattern as RatingDecayWorker
                val db = Room.databaseBuilder(ctx, AppDatabase::class.java, "spanish_app.db")
                    .addMigrations(
                        AppDatabase.MIGRATION_1_2,  AppDatabase.MIGRATION_2_3,
                        AppDatabase.MIGRATION_3_4,  AppDatabase.MIGRATION_4_5,
                        AppDatabase.MIGRATION_5_6,  AppDatabase.MIGRATION_6_7,
                        AppDatabase.MIGRATION_7_8,  AppDatabase.MIGRATION_8_9,
                        AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11,
                        AppDatabase.MIGRATION_11_12,AppDatabase.MIGRATION_12_13,
                        AppDatabase.MIGRATION_13_14,AppDatabase.MIGRATION_14_15,
                        AppDatabase.MIGRATION_15_16,AppDatabase.MIGRATION_16_17,
                        AppDatabase.MIGRATION_17_18
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                try {
                    val importer = ContentImporter(db.wordDao())
                    packs.forEach { importer.apply(it) }
                } finally {
                    db.close()
                }
            }

            Result.success()
        } catch (e: CancellationException) {
            throw e                                      // never swallow coroutine cancellation
        } catch (e: Exception) {
            // Exponential back-off: retry up to 3× then give up until next day
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "content_sync_daily"

        /**
         * Schedule daily background sync.
         * Uses KEEP policy so re-scheduling on every launch doesn't reset the timer.
         * Constraints: Wi-Fi only + battery not low — user never notices.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)   // Wi-Fi / Ethernet only
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<ContentSyncWorker>(
                24, TimeUnit.HOURS,
                4,  TimeUnit.HOURS    // flex window — OS picks exact time for battery efficiency
            )
                .setConstraints(constraints)
                .setInitialDelay(15, TimeUnit.MINUTES)   // don't hit CDN on cold app start
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /** One-shot sync — called from Settings «Проверить обновления». */
        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<ContentSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
