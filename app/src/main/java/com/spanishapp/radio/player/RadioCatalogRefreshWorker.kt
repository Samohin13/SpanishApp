package com.spanishapp.radio.player

import android.content.Context
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.spanishapp.BuildConfig
import com.spanishapp.data.db.AppDatabase
import com.spanishapp.radio.data.RadioCatalogRepository
import java.util.concurrent.TimeUnit

/**
 * Раз в 7 дней в фоне обновляет каталог радиостанций.
 *
 * Зачем: сейчас каталог обновляется только когда юзер открыл экран радио
 * И прошло >7 дней. Если юзер месяц не заходил → открывает → ждёт пока
 * всё пересоберётся, плюс часть URL'ов за это время умерла.
 *
 * С этим Worker'ом юзер всегда видит свежий каталог, без задержки.
 *
 * Constraints:
 *  - Wi-Fi only (UNMETERED) → не сжигаем мобильный трафик
 *  - Battery не низкая → не работаем когда телефон умирает
 *
 * ExistingPeriodicWorkPolicy.KEEP — если уже запланирован, не пересоздаём.
 */
class RadioCatalogRefreshWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "radio_catalog_refresh"
        private const val INTERVAL_DAYS = 7L
        private const val INITIAL_DELAY_HOURS = 24L

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<RadioCatalogRefreshWorker>(
                INTERVAL_DAYS, TimeUnit.DAYS,
            )
                .setInitialDelay(INITIAL_DELAY_HOURS, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }

    override suspend fun doWork(): Result {
        // Открываем БД напрямую (без Hilt) — паттерн как RatingDecayWorker.
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
                AppDatabase.MIGRATION_21_22,AppDatabase.MIGRATION_22_23,
                AppDatabase.MIGRATION_24_25,
                AppDatabase.MIGRATION_23_24,
                AppDatabase.MIGRATION_25_26,
                AppDatabase.MIGRATION_26_27,
                AppDatabase.MIGRATION_27_28,
                AppDatabase.MIGRATION_28_29,
                AppDatabase.MIGRATION_29_30,
            )
            .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration() }
            .build()

        return try {
            val repo = RadioCatalogRepository(applicationContext, db.radioCatalogDao())
            val count = repo.discoverAndCache()
            if (count > 0) {
                Result.success()
            } else {
                // Discovery вернул 0 — повторим позже (WorkManager сам подберёт интервал).
                Result.retry()
            }
        } catch (e: Exception) {
            runCatching {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[RadioCatalogRefreshWorker] failed", e))
            }
            Result.retry()
        } finally {
            db.close()
        }
    }
}
