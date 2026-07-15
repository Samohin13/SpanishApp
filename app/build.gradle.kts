import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.kover)
}

// ── Kover coverage configuration ─────────────────────────────────────
// Исключаем generated-код (Hilt, KSP, Compose) и UI-сгенерённые классы
// чтобы метрика отражала покрытие НАШЕГО кода.
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*Hilt_*",
                    "*_HiltModules*",
                    "*_Factory",
                    "*_MembersInjector",
                    "*ComposableSingletons*",
                    "*\$\$serializer",
                    "*BuildConfig",
                    "com.spanishapp.di.*",
                    // UI / Compose — для них своя система тестирования
                    "com.spanishapp.ui.theme.*",
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
}

// ── Convenience-task: всё что нужно перед заливкой релиза в Play ──
//
// Запуск: ./gradlew preRelease
// Проверяет:
//   1. Lint (без ошибок)
//   2. Unit tests (все проходят)
//   3. Сборка release AAB (компилируется + R8/ProGuard)
//   4. Coverage report (для информации)
//
// Если упало — НЕ заливать в Play Console.
tasks.register("preRelease") {
    group = "verification"
    description = "Полный набор проверок перед релизом — lint, тесты, AAB. См. docs/qa/RELEASE_CHECKLIST.md"
    dependsOn(
        "lintRelease",
        "testDebugUnitTest",
        "bundleRelease",
        "koverHtmlReportDebug",
    )
    doLast {
        println("\n  ╔═══════════════════════════════════════════════════════════╗")
        println("  ║  preRelease ✓                                             ║")
        println("  ║                                                           ║")
        println("  ║  AAB:        app/build/outputs/bundle/release/            ║")
        println("  ║  Coverage:   app/build/reports/kover/html/index.html      ║")
        println("  ║  Lint:       app/build/reports/lint-results-release.html  ║")
        println("  ║                                                           ║")
        println("  ║  Дальше:     открой docs/qa/RELEASE_CHECKLIST.md          ║")
        println("  ╚═══════════════════════════════════════════════════════════╝\n")
    }
}

// Load local.properties explicitly (project.findProperty doesn't read it reliably)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    // namespace остаётся "com.spanishapp" — это namespace Kotlin/Java
    // пакетов в коде. Менять его означало бы переименовать сотни импортов.
    // applicationId же — это идентификатор приложения в Play Store, он
    // может отличаться. Сменён на "com.espeak.app" т.к. "com.spanishapp"
    // уже зарегистрирован другим разработчиком в Google Play.
    namespace = "com.spanishapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.espeak.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 202
        versionName = "1.27.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Proxy URL for production: hides the API key from the APK.
        // When non-empty, AiChatRepository routes requests through it.
        // Release-сборки ОБЯЗАНЫ иметь этот URL (см. AiChatRepository.apiUrl()).
        val aiProxyUrl = localProps.getProperty("AI_PROXY_URL") ?: ""
        buildConfigField("String", "AI_PROXY_URL", "\"$aiProxyUrl\"")
        // Shared secret sent in X-App-Secret header so the Worker rejects
        // random requests from anyone who discovers the proxy URL. Must
        // match Cloudflare env var APP_SECRET.
        val aiProxySecret = localProps.getProperty("AI_PROXY_SECRET") ?: ""
        buildConfigField("String", "AI_PROXY_SECRET", "\"$aiProxySecret\"")
        // ANTHROPIC_API_KEY удалён в v1.11.7 — поле было dead code (никто не использует),
        // но запекалось в BuildConfig каждого APK = surface для будущей утечки.
        // GEMINI_API_KEY теперь только в debug buildType (см. buildTypes { debug })
        // чтобы release AAB не содержал ключ в скомпилированном виде.
    }

    signingConfigs {
        // Release-keystore: настраивается через keystore.properties (см. README).
        // Если файл отсутствует — release-сборка собирается без подписи (для CI/dev).
        val keystorePropsFile = rootProject.file("keystore.properties")
        if (keystorePropsFile.exists()) {
            create("release") {
                val ksProps = Properties().apply {
                    keystorePropsFile.inputStream().use { load(it) }
                }
                storeFile = rootProject.file(ksProps.getProperty("storeFile") ?: "release.keystore")
                storePassword = ksProps.getProperty("storePassword")
                keyAlias = ksProps.getProperty("keyAlias")
                keyPassword = ksProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Подпишем release только если keystore.properties есть.
            if (rootProject.file("keystore.properties").exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            // Release НЕ содержит GEMINI_API_KEY вообще. Пустая константа
            // → R8 заинлайнит и оптимизирует, ключа в APK не остаётся.
            // AiChatRepository.apiUrl() в release требует AI_PROXY_URL (через
            // require(BuildConfig.DEBUG)), иначе крашит — это намеренная защита.
            buildConfigField("String", "GEMINI_API_KEY", "\"\"")
            // v1.25.82: native debug symbols в AAB.
            // Без этого Play Console показывает warning «Этот объект (App
            // Bundle) содержит нативный код. Рекомендуем загрузить файл с
            // отладочными символами». Зависимости вроде ExoPlayer / Firebase
            // приносят .so файлы — Google хочет их symbol table для anrs.
            // FULL = полные DWARF символы (~10-30 MB в AAB, но Play
            // распакует, в APK не попадает). Чистый AAB без warnings.
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            // Debug-сборка не минифицируется — быстрее и удобнее отлаживать.
            isMinifyEnabled = false
            // v1.20.2 SECURITY: GEMINI_API_KEY больше НЕ запекается даже в debug.
            // Раньше debug-APK содержал ключ в BuildConfig → если debug APK
            // случайно раздавался тестерам, ключ можно было вытащить за минуту
            // через jadx/strings. Так уже потеряли один ключ
            // (AIzaSyBoTl..., Google пометил как leaked).
            //
            // Теперь debug, как и release, ходит ТОЛЬКО через AI_PROXY_URL.
            // Это значит для debug-сборки тоже нужен Cloudflare Worker —
            // что вообще-то правильно, debug должен повторять архитектуру prod.
            buildConfigField("String", "GEMINI_API_KEY", "\"\"")
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // v1.26.1: Robolectric-тестам нужны Android-ресурсы в JVM unit-тестах.
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

// v1.26.1: экспорт схемы Room в app/schemas/ — с этого момента каждая версия БД
// получает JSON-схему, что даёт возможность писать MigrationTestHelper-тесты для
// будущих миграций. Историю (v1..v31) восстановить нельзя — экспорт не включался.
ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Adaptive layouts (v1.12.0 — tablet-first redesign Phase 0)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.animation)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.lottie)
    implementation(libs.coil.compose)
    implementation(libs.image.cropper)
    implementation(libs.androidx.work)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.biometric)

    // Media3 — для радио (ExoPlayer + MediaSession + HLS поддержка)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.session)

    // ZXing core — генерация QR-кода для share-картинки прогресса
    // (Play Store URL внутри изображения, чтобы можно было сосканировать
    // с другого устройства). Pure Java, без UI-зависимостей, ~500KB.
    implementation(libs.zxing.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.google.play.services.auth)
    // v1.22.30: in-app update prompt — лёгкая плашка «обновить до версии X»
    // на старте приложения когда в Play Store есть новая версия.
    implementation(libs.google.play.app.update)
    implementation(libs.google.play.app.update.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    // v1.25.4: Google Play Billing Library 7 — реальные PRO подписки
    implementation(libs.billing.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    // v1.26.1: Robolectric — JVM-эмуляция Android (SharedPreferences, Room in-memory)
    // для тестов AccountSyncGuard и БД. room-testing — MigrationTestHelper.
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.room.testing)
    // org.json настоящий — без него Android stub бросает «not mocked» на JSONArray
    testImplementation("org.json:json:20240303")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}