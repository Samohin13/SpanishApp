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
                AppDatabase.MIGRATION_29_30,
                AppDatabase.MIGRATION_30_31,
                AppDatabase.MIGRATION_31_32,
            )
            .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration() }
            .build()

        return try {
            val dao = db.userProgressDao()
            val progress = dao.getProgressOnce() ?: return Result.success()
            val now = System.currentTimeMillis()

            // v1.25.98 FIX (audit xp-H4): якорь = последняя АКТИВНОСТЬ юзера,
            // и decay-запись его НЕ трогает. Раньше воркер после каждого
            // применения ставил last_rating_update = now → «дней простоя»
            // обнулялись, прогрессивная шкала (-5/-8/-12 в день) никогда не
            // эскалировала: фактический decay был -5 раз в 3 дня навсегда.
            //
            // Чтобы кумулятивный штраф не вычитался повторно при каждом
            // ежедневном прогоне, применяем ДЕЛЬТУ: penalty(сейчас) −
            // penalty(на момент прошлого прогона). Время прошлого прогона —
            // в SharedPreferences (воркер без Hilt).
            val anchor = if (progress.lastRatingUpdate > 0) progress.lastRatingUpdate
                         else progress.lastStudyDate
            val prefs = applicationContext.getSharedPreferences("rating_decay", Context.MODE_PRIVATE)
            val lastRun = prefs.getLong("last_run_ms", 0L)

            val penaltyNow = cumulativePenalty(anchor, now)
            val penaltyAtLastRun = if (lastRun > anchor) cumulativePenalty(anchor, lastRun) else 0
            val delta = (penaltyNow - penaltyAtLastRun).coerceAtLeast(0)

            if (delta > 0 && progress.skillRating > 0) {
                val newRating = (progress.skillRating - delta).coerceAtLeast(0)
                val newLeague = LeagueResolver.fromRating(newRating)
                // ts = anchor: сохраняем якорь последней активности.
                dao.updateSkillRating(newRating, newLeague.tier, anchor)
            }
            prefs.edit().putLong("last_run_ms", now).apply()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        } finally {
            db.close()
        }
    }

    /**
     * Суммарный штраф за простой от [anchorMs] до [nowMs] по прогрессивной
     * шкале SkillRatingSystem (2 дня grace, потом -5/-8/-12 в день).
     * Делегирует в applyDecay c рейтингом 0 базой — извлекаем чистый штраф.
     */
    private fun cumulativePenalty(anchorMs: Long, nowMs: Long): Int {
        if (anchorMs <= 0L) return 0
        // applyDecay(current=X) возвращает max(FLOOR, X - penalty).
        // Берём большую базу, чтобы floor не срезал, и вычитаем.
        val base = 1_000_000
        return base - SkillRatingSystem.applyDecay(
            currentRating = base,
            peakRating = base,
            lastUpdateMs = anchorMs,
            nowMs = nowMs,
        )
    }
}
