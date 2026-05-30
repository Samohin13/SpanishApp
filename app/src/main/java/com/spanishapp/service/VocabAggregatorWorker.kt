package com.spanishapp.service

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.work.*
import com.spanishapp.BuildConfig
import com.spanishapp.data.db.AppDatabase
import com.spanishapp.data.db.entity.UserVocabStateEntity
import com.spanishapp.domain.vocab.VocabAggregator
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * v1.25.28 — daily worker который пересчитывает user_vocab_state.
 *
 * Источники сигнала:
 *  1. WordEntity (SM-2 state + level/cefr) — основной словарь
 *  2. UserWordFrequency DataStore (chat usage) — что юзер сам пишет
 *
 * Алгоритм:
 *  - Берём ВСЕ studied words из WordEntity
 *  - Берём ВСЕ слова из chat usage
 *  - Для каждого уникального слова строим Signals и aggregate
 *  - Batch upsert в user_vocab_state
 *
 * Pattern как у других workers — без Hilt, открываем Room напрямую,
 * DataStore через extension property.
 */
private val Context.userWordFreqStore by preferencesDataStore("user_word_freq")
private val FREQ_KEY = stringPreferencesKey("word_freq_json")

class VocabAggregatorWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "vocab_aggregator_work"

        /** Зарегистрировать periodic worker. Идемпотентно через KEEP policy. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<VocabAggregatorWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(15, TimeUnit.MINUTES)  // первый прогон вскоре после старта
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        /** Принудительный one-shot — например когда юзер впервые открыл VocabScreen. */
        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<VocabAggregatorWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "${WORK_NAME}_once",
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }

    override suspend fun doWork(): Result {
        val db = openDatabase(applicationContext)
        return try {
            aggregateAndSave(applicationContext, db)
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("VocabAggregator", "Failed", e)
            Result.retry()
        } finally {
            db.close()
        }
    }

    private suspend fun aggregateAndSave(context: Context, db: AppDatabase) {
        val now = System.currentTimeMillis()
        val wordDao = db.wordDao()
        val vocabDao = db.userVocabStateDao()

        // 1. ВСЕ slove которые юзер трогал в flashcards/games
        val studiedWords = wordDao.getAllStudiedWords()

        // 2. Chat usage из UserWordFrequency DataStore
        val chatUsage = loadChatUsage(context)

        // 3. Объединяем уникальные ключи (lowercase spanish)
        val studiedKeys = studiedWords.associateBy { it.spanish.lowercase().trim() }
        val allKeys = studiedKeys.keys + chatUsage.keys

        // 4. Для слов в chat но не в БД — нужно найти их в WordEntity
        val unmatchedChatWords = chatUsage.keys - studiedKeys.keys
        val chatWordsInDb = if (unmatchedChatWords.isNotEmpty()) {
            wordDao.getWordsBySpanishBatch(unmatchedChatWords.toList())
                .associateBy { it.spanish.lowercase().trim() }
        } else emptyMap()

        // 5. Aggregate
        val results = mutableListOf<UserVocabStateEntity>()
        for (word in allKeys) {
            val entity = studiedKeys[word] ?: chatWordsInDb[word]
            val usage = chatUsage[word] ?: 0
            val signals = VocabAggregator.Signals(
                word = word,
                wordId = entity?.id,
                cefr = entity?.level?.takeIf { it.isNotBlank() },
                sm2EaseFactor = entity?.easeFactor ?: 0f,
                sm2Repetitions = entity?.repetitions ?: 0,
                totalReviews = entity?.totalReviews ?: 0,
                correctReviews = entity?.correctReviews ?: 0,
                isLearned = entity?.isLearned ?: false,
                chatUsageCount = usage,
                correctionsCount = 0,  // TODO: парсить из chat_messages corrections JSON в follow-up
                // seenInLesson = слово трогалось в SM-2 → значит было в уроке (proxy для MVP)
                seenInLesson = entity != null && (entity.totalReviews > 0 || entity.isLearned),
                seenInLibro = false,  // TODO follow-up
                lastSeenAt = computeLastSeenAt(entity, usage, now),
            )
            val aggregated = VocabAggregator.aggregate(signals, now)
            if (aggregated != null) results.add(aggregated)
        }

        // 6. Batch upsert
        if (results.isNotEmpty()) {
            vocabDao.upsertAll(results)
        }
        android.util.Log.i("VocabAggregator",
            "Aggregated ${results.size} vocab entries " +
            "(${studiedKeys.size} studied + ${chatUsage.size} chat)")
    }

    /**
     * lastSeenAt: берём max из (nextReview timestamp - interval days как proxy
     * последнего ревью) и (если есть chat usage → now). MVP-простая логика.
     */
    private fun computeLastSeenAt(
        entity: com.spanishapp.data.db.entity.WordEntity?,
        chatUsage: Int,
        now: Long,
    ): Long {
        val fromChat = if (chatUsage > 0) now else 0L
        val fromFlashcards = if (entity != null && entity.nextReview > 0) {
            // nextReview - interval days = время последнего повторения примерно
            entity.nextReview - entity.interval * DAY_MS
        } else 0L
        return maxOf(fromChat, fromFlashcards)
    }

    /**
     * Читаем chat usage напрямую из DataStore (без instance UserWordFrequency).
     * Возвращает {word → count}.
     */
    private suspend fun loadChatUsage(context: Context): Map<String, Int> {
        return try {
            val json = context.userWordFreqStore.data.first()[FREQ_KEY]
                ?: return emptyMap()
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Int>()
            obj.keys().forEach { k -> map[k.lowercase().trim()] = obj.optInt(k, 0) }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun openDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "spanish_app.db")
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
            )
            .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration() }
            .build()
    }
}

private const val DAY_MS = 24L * 60 * 60 * 1000
