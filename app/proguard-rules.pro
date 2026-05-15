# ───────────────────────────────────────────────────────────────────
# ESPEAK — ProGuard / R8 rules for release builds.
# Generated 2026-05-07.
# ───────────────────────────────────────────────────────────────────

# Сохраняем имя файла и номер строки для читаемых стек-трейсов в Crashlytics.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,Exceptions,InnerClasses,EnclosingMethod

# ── Kotlin ─────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keep class kotlin.coroutines.** { *; }

# ── Kotlinx Serialization ──────────────────────────────────────────
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep,includedescriptorclasses class com.spanishapp.**$$serializer { *; }
-keepclassmembers class com.spanishapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.spanishapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# ── Room ───────────────────────────────────────────────────────────
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class com.spanishapp.data.db.entity.** { *; }
-keep class com.spanishapp.data.db.dao.** { *; }

# ── Hilt / Dagger ──────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.** { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-dontwarn dagger.**

# ── OkHttp / Retrofit ──────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# ── Firebase / Crashlytics / Auth / Firestore ──────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firestore data-классы — структурно сериализуются через рефлексию.
-keepclassmembers class com.spanishapp.data.repository.** {
    public <init>(...);
    public *** get*();
    public *** set*(***);
    public <fields>;
}
-keep class com.spanishapp.data.repository.LeaderboardEntry { *; }

# ── Compose ────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── Glance / Widgets ───────────────────────────────────────────────
# Glance использует рефлексию для composition + RemoteViews. Без keep всех
# androidx.glance.* классов R8 их вырезает → виджет крашится при загрузке
# с надписью «Не удалось загрузить виджет» на главном экране.
-keep class androidx.glance.** { *; }
-keepclassmembers class androidx.glance.** { *; }
-dontwarn androidx.glance.**
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class com.spanishapp.widget.** { *; }
# Glance Composable-функции: имена и сигнатуры нужны в рантайме.
-keepclassmembers class com.spanishapp.widget.** {
    @androidx.compose.runtime.Composable <methods>;
}
# DataStore Preferences (Glance state persistence)
-keep class androidx.datastore.*.** { *; }
-dontwarn androidx.datastore.**

# ── WorkManager ────────────────────────────────────────────────────
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class com.spanishapp.service.** { *; }

# ── Lottie / Coil ──────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**
-keep class coil.** { *; }
-dontwarn coil.**

# ── Image cropper ──────────────────────────────────────────────────
-keep class com.canhub.cropper.** { *; }
-dontwarn com.canhub.cropper.**

# ── Domain / алгоритмы и доменные модели ───────────────────────────
-keep class com.spanishapp.domain.algorithm.** { *; }
-keep class com.spanishapp.domain.rating.** { *; }
-keep class com.spanishapp.domain.games.** { *; }

# ── Игры: Compose-композиции иногда теряются при R8 ────────────────
-keep class com.spanishapp.ui.games.** { *; }
-keep class com.spanishapp.ui.flashcards.** { *; }

# ── Курс v2.0 (1.3.0): theory + checkpoint + V2 контент ────────────
# R8 без этих rules ломает релизную сборку — статичные singleton-объекты
# с data classes теряют поля/companion, NavController.savedStateHandle
# не может прочитать args, Compose-композиции в Reader/Session экранах
# теряются при минификации.
-keep class com.spanishapp.data.theory.** { *; }
-keep class com.spanishapp.data.checkpoint.** { *; }
-keep class com.spanishapp.ui.theory.** { *; }
-keep class com.spanishapp.ui.checkpoint.** { *; }
-keep class com.spanishapp.ui.home.LessonContentDataV2 { *; }
-keep class com.spanishapp.ui.home.VocabScope { *; }
-keep class com.spanishapp.ui.home.VocabScope$ScopeWord { *; }
-keep class com.spanishapp.ui.home.LessonContent { *; }
-keep class com.spanishapp.ui.home.LessonSection { *; }
-keep class com.spanishapp.ui.home.LessonItem { *; }
-keep class com.spanishapp.ui.home.Exercise { *; }
-keep class com.spanishapp.ui.home.ExerciseType { *; }
-keep class com.spanishapp.ui.home.ExercisePlan { *; }
# Enum с values()/valueOf() через рефлексию — must be kept
-keepclassmembers enum com.spanishapp.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Снятие предупреждений по платформам
-dontwarn java.lang.management.**
-dontwarn org.codehaus.mojo.**
-dontwarn javax.naming.**
