package com.spanishapp.service

import android.content.Context
import androidx.room.Room
import androidx.work.*
import com.spanishapp.BuildConfig
import com.spanishapp.data.db.AppDatabase
import com.spanishapp.domain.algorithm.LeagueResolver
import com.spanishapp.domain.algorithm.SkillRatingSystem
import java.util.concurrent.TimeUnit

/**
 * Раз в сутки применяет затухание к skillRating, если пользователь не занимался
 * больше grace-периода. Без Hilt (как DailyReminderWorker) — простой Worker.
 */
class RatingDecayWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "rating_decay_work"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RatingDecayWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(2, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        // Открываем БД напрямую — без Hilt, чтобы избежать KSP-проблем
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "spanish_app.db")
            .addMigrations(
                AppDatabase.MIGRATION_1_2,  AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,  AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,  AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,  AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14,AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16,AppDatabase.MIGRATION_16_17,
                AppDatabase.MIGRATION_17_18,AppDatabase.MIGRATION_18_19,
                AppDatabase.MIGRATION_19_20,AppDatabase.MIGRATION_20_21,
                AppDatabase.MIGRATION_21_22, AppDatabase.MIGRATION_22_23,
                AppDatabase.MIGRATION_24_25,
                AppDatabase.MIGRATION_23_24,
                AppDatabase.MIGRATION_25_26,
                AppDatabase.MIGRATION_26_27,
                AppDatabase.MIGRATION_27_28,
                AppDatabase.MIGRATION_28_29,
            )
            .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration() }
            .build()

        return try {
            val dao = db.userProgressDao()
            val progress = dao.getProgressOnce() ?: return Result.success()
            val now = System.currentTimeMillis()
            val newRating = SkillRatingSystem.applyDecay(
                currentRating = progress.skillRating,
                peakRating = progress.peakSkillRating,
                lastUpdateMs = if (progress.lastRatingUpdate > 0) progress.lastRatingUpdate else progress.lastStudyDate,
                nowMs = now
            )
            if (newRating != progress.skillRating) {
                val newLeague = LeagueResolver.fromRating(newRating)
                dao.updateSkillRating(newRating, newLeague.tier, now)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        } finally {
            db.close()
        }
    }
}
