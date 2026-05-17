package com.spanishapp.di

import android.content.Context
import androidx.room.Room
import com.spanishapp.BuildConfig
import com.spanishapp.data.db.*
import com.spanishapp.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Room Database ─────────────────────────────────────────
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val builder = Room.databaseBuilder(context, AppDatabase::class.java, "spanish_app.db")
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16,
                AppDatabase.MIGRATION_16_17,
                AppDatabase.MIGRATION_17_18,
                AppDatabase.MIGRATION_18_19,
                AppDatabase.MIGRATION_19_20,
                AppDatabase.MIGRATION_20_21,
                AppDatabase.MIGRATION_21_22,
                AppDatabase.MIGRATION_22_23,
                AppDatabase.MIGRATION_23_24,
            )
        // fallbackToDestructiveMigration ТОЛЬКО в debug. Раньше было всегда —
        // любая будущая ошибка миграции в release молча wipe'ала весь прогресс
        // юзеров (XP, streak, флэшкарты). Теперь release крашит — это лучше:
        // увидим crash в Crashlytics и сможем выкатить migration fix, а не
        // потерять данные половины аудитории.
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration()
        }
        return builder.build()
    }

    @Provides fun provideWordDao(db: AppDatabase): WordDao = db.wordDao()
    @Provides fun provideConjugationDao(db: AppDatabase): ConjugationDao = db.conjugationDao()
    @Provides fun provideLessonDao(db: AppDatabase): LessonDao = db.lessonDao()
    @Provides fun provideDialogueDao(db: AppDatabase): DialogueDao = db.dialogueDao()
    @Provides fun provideUserProgressDao(db: AppDatabase): UserProgressDao = db.userProgressDao()
    @Provides fun provideChatMessageDao(db: AppDatabase): ChatMessageDao = db.chatMessageDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideDailyWordDao(db: AppDatabase): DailyWordDao = db.dailyWordDao()
    @Provides fun provideWordListDao(db: AppDatabase): WordListDao = db.wordListDao()
    @Provides fun provideArticleGameDao(db: AppDatabase): ArticleGameDao = db.articleGameDao()
    @Provides fun provideLessonProgressDao(db: AppDatabase): LessonProgressDao = db.lessonProgressDao()
    @Provides fun provideLibroProgressDao(db: AppDatabase): LibroProgressDao = db.libroProgressDao()
    @Provides fun provideGameLevelProgressDao(db: AppDatabase): GameLevelProgressDao = db.gameLevelProgressDao()
    @Provides fun provideDailyXpDao(db: AppDatabase): DailyXpDao = db.dailyXpDao()
    @Provides fun provideFlashcardSetProgressDao(db: AppDatabase): FlashcardSetProgressDao = db.flashcardSetProgressDao()
    @Provides fun provideRecentSearchDao(db: AppDatabase): RecentSearchDao = db.recentSearchDao()
    @Provides fun provideWeeklyLeagueDao(db: AppDatabase): WeeklyLeagueDao = db.weeklyLeagueDao()
    @Provides fun provideWodHistoryDao(db: AppDatabase): WodHistoryDao = db.wodHistoryDao()
    @Provides fun provideTheoryProgressDao(db: AppDatabase): com.spanishapp.data.db.dao.TheoryProgressDao = db.theoryProgressDao()
    @Provides fun provideRadioFavoriteDao(db: AppDatabase): com.spanishapp.radio.data.RadioFavoriteDao = db.radioFavoriteDao()
    @Provides fun provideRadioCatalogDao(db: AppDatabase): com.spanishapp.radio.data.RadioCatalogDao = db.radioCatalogDao()
    @Provides fun provideRadioListeningDao(db: AppDatabase): com.spanishapp.radio.data.RadioListeningDao = db.radioListeningDao()
    // RadioWordCatchDao убран в v1.9.0 (фича «Поймал слово» вырезана).
    // Таблица оставлена в БД для совместимости — без миграции вниз.

    // ── Radio player (Singleton — выживает между экранами) ───
    @Provides
    @Singleton
    fun provideRadioPlayerController(
        @ApplicationContext context: Context
    ): com.spanishapp.radio.player.RadioPlayerController =
        com.spanishapp.radio.player.RadioPlayerController(context)

    // ── Firebase Firestore ─────────────────────────────────────
    @Provides
    @Singleton
    fun provideFirestore(): com.google.firebase.firestore.FirebaseFirestore =
        com.google.firebase.firestore.FirebaseFirestore.getInstance()

    // ── Firebase Storage (content packs + avatars) ─────────────
    @Provides
    @Singleton
    fun provideFirebaseStorage(): com.google.firebase.storage.FirebaseStorage =
        com.google.firebase.storage.FirebaseStorage.getInstance()

    // ── Content downloader (gh-pages CDN) ─────────────────────
    @Provides
    @Singleton
    fun provideContentCacheRoot(@ApplicationContext context: Context): java.io.File =
        java.io.File(context.filesDir, "content_packs").apply { mkdirs() }

    // ContentDownloader uses @Inject constructor and is auto-provided.

    // ── OkHttp  (Anthropic API) ────────────────────────────────
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    // Never log in release — URLs may contain API keys as query params.
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                            else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
}

// ─────────────────────────────────────────────────────────────
// Room Database definition
// ─────────────────────────────────────────────────────────────
// app/src/main/java/com/spanishapp/data/db/AppDatabase.kt

/*
@Database(
    entities = [
        WordEntity::class,
        ConjugationEntity::class,
        LessonEntity::class,
        DialogueEntity::class,
        UserProgressEntity::class,
        ChatMessageEntity::class,
        AchievementEntity::class,
        DailyWordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun conjugationDao(): ConjugationDao
    abstract fun lessonDao(): LessonDao
    abstract fun dialogueDao(): DialogueDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyWordDao(): DailyWordDao
}
*/