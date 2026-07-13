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
            // v1.26.1: единый список — AppDatabase.ALL_MIGRATIONS.
            .addMigrations(*AppDatabase.ALL_MIGRATIONS)
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
