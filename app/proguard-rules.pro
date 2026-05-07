# ───────────────────────────────────────────────────────────────────
# SpanishApp / HablaRu — ProGuard / R8 rules for release builds.
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
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class com.spanishapp.widget.** { *; }

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

# Снятие предупреждений по платформам
-dontwarn java.lang.management.**
-dontwarn org.codehaus.mojo.**
-dontwarn javax.naming.**
