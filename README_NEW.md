# EASPEAK — Spanish Learning App (Android)

> **EASPEAK** — Russian-language Android app for learning Spanish (CEFR A1 → B2). Built with Kotlin + Jetpack Compose, featuring AI tutor (Claude), spaced-repetition flashcards, mini-games, and "Path to Madrid" leaderboard system.

🇷🇺 **Russian UI** | 🇪🇸 **Spanish content** | 🤖 **Claude AI tutor** | 🎮 **Gamified learning**

---

## 🚀 Quick Start

### Prerequisites

- **Android Studio** (Giraffe or newer)
- **Java 17+** (comes with Android Studio)
- **Git**
- **(Optional) Claude API key** for AI tutor feature

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/Samohin13/SpanishApp.git
cd SpanishApp

# Open in Android Studio
# File → Open → select this folder

# Build & Run
# Build → Make Project
# Run → Run 'app' (select your device or emulator)
```

### First Run

1. **Create an emulator:**
   - Device: Pixel 5 or newer (min API 28)
   - System Image: Android 13+

2. **Build the app:**
   ```bash
   ./gradlew build
   ```

3. **Run on device/emulator:**
   ```bash
   ./gradlew installDebug
   ```

---

## 📂 Project Structure

```
SpanishApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/spanishapp/
│   │   │   ├── MainActivity.kt                  ← Entry point
│   │   │   ├── ui/
│   │   │   │   ├── theme/                       ← Design tokens
│   │   │   │   │   ├── Theme.kt                ← Colors + Typography
│   │   │   │   │   └── Type.kt
│   │   │   │   ├── components/                  ← Reusable components
│   │   │   │   │   └── Components.kt
│   │   │   │   └── [screens]/                   ← 8 main screens
│   │   │   │       ├── home/HomeScreen.kt
│   │   │   │       ├── flashcards/FlashcardsScreen.kt
│   │   │   │       ├── dictionary/DictionaryScreen.kt
│   │   │   │       ├── games/LibrosScreen.kt
│   │   │   │       ├── leaderboard/LeaderboardScreen.kt
│   │   │   │       ├── profile/ProfileScreen.kt
│   │   │   │       ├── settings/SettingsScreen.kt
│   │   │   │       └── onboarding/OnboardingScreen.kt
│   │   └── res/
│   │       ├── drawable/                        ← Icons, logos
│   │       └── values/                          ← Strings, themes
│   └── build.gradle.kts                         ← App config
│
├── design_system/                               ← 🎨 **NEW: Design System**
│   ├── DESIGN_SYSTEM_GUIDE.md                   ← Start here!
│   ├── README.md                                ← Official docs
│   ├── EASPEAK_Structure_Overview.md            ← Architecture guide
│   ├── EASPEAK_Components_Guide.md              ← Component reference
│   ├── colors_and_type.css                      ← Design tokens
│   ├── preview/                                 ← HTML component preview
│   ├── ui_kits/espeak_app/                      ← React version
│   └── assets/ & brand/                         ← Logo & branding
│
├── docs/                                        ← Articles & levels data
├── gradle/                                      ← Gradle wrapper
├── build.gradle.kts                             ← Root config
└── settings.gradle.kts                          ← Module settings
```

---

## 🎨 Design System (NEW!)

**EASPEAK now includes a complete design system!**

Start here: **`design_system/DESIGN_SYSTEM_GUIDE.md`** ← 🔥 Begin here!

### What's inside:

1. **📖 Documentation**
   - `EASPEAK_Structure_Overview.md` — Full architecture guide
   - `EASPEAK_Components_Guide.md` — Component reference with layouts
   - `README.md` — Original Figma documentation

2. **🎨 Design Tokens**
   - `colors_and_type.css` — All CSS variables (colors, type, spacing, shadows, gradients)

3. **🖼️ Resources**
   - `preview/` — HTML component previews (open in browser)
   - `ui_kits/espeak_app/` — React/JSX version of all screens
   - `assets/` — Logos and icons
   - `brand/` — Branding guidelines

### Key Design Principles

- **Color:** Purple-first palette (`#7B2FBE`), gold accent (`#FF9500`)
- **Type:** Inter/Roboto, weights 400–800, Material3 scale
- **Spacing:** 14px (card padding), 20px (content padding)
- **Radius:** 20px (main cards), 12px (buttons)
- **Icons:** Material Icons + Emoji (emoji are 1st-class UI elements)
- **Animation:** Spring physics (not duration-based)
- **Language:** Russian UI + Spanish accents (both visible!)
- **Tone:** Informal (ты, not Вы), direct imperatives

---

## 📱 Screens (8 Main Routes)

| Screen | Route | Purpose |
|---|---|---|
| 🏠 **Home** | `home/` | Courses, stats, "Word of the Day", Path to Madrid |
| 🃏 **Flashcards** | `flashcards/` | SM-2 spaced repetition cards |
| 📖 **Dictionary** | `dictionary/` | Word search, examples, translations |
| 🎮 **Games** | `games/` | Mini-games (articles, speed, anagrams) |
| 🏆 **Leaderboard** | `leaderboard/` | Path to Madrid seasonal rankings |
| 👤 **Profile** | `profile/` | User stats, premium, AI tutor |
| ⚙️ **Settings** | `settings/` | Language, notifications, data |
| 🎊 **Onboarding** | `onboarding/` | Welcome, name input, level selection |

Bottom navigation routes: Home, Games, Tarjetas (Flashcards), Dictionary, Profile.

---

## 🔧 Key Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| **Jetpack Compose** | 1.6+ | Modern UI framework |
| **Material3** | Latest | Material Design 3 components |
| **Coil** | 2.5+ | Image loading (for avatars) |
| **Firebase** | BOM 32+ | Auth, Realtime DB, Analytics |
| **Kotlin Coroutines** | 1.7+ | Async/background tasks |
| **Room** | 2.5+ | Local database (flashcard cache) |
| **Lottie** | 6.0+ | Success animation (SM-2 completion) |

See `gradle/libs.versions.toml` for full version catalog.

---

## 🎯 Features

### Learning
- 📚 **CEFR Levels:** A1 (Beginner) → B2 (Upper-Intermediate)
- 🃏 **Spaced Repetition:** SM-2 algorithm for flashcards
- 📖 **Word of the Day:** Fresh vocabulary daily
- 🎧 **Pronunciation:** TTS + example sentences

### Gamification
- 🔥 **Streaks:** Daily practice counter
- ✨ **XP System:** Experience points per lesson
- 🏆 **Path to Madrid:** City-based leaderboard (replace #1 global rank)
- 🏅 **Mastery Ratings:** 0–5 stars per word (SM-2 review states)

### Content
- 📚 ~2,000 vocabulary words (A1–B2)
- 📋 Grammar rules + practice drills
- 🎮 4+ mini-games (articles, speed, anagrams, conjugations)
- 📰 Short articles for reading practice

### Premium (Claude AI Tutor)
- 🤖 AI-powered pronunciation feedback
- 💬 Conversational practice with Claude
- 📊 Detailed learning analytics
- 🎁 +500 XP daily bonus

---

## 🛠️ Development

### Architecture

- **Pattern:** MVVM (Model-View-ViewModel)
- **State Management:** `StateFlow` + `ViewModel`
- **UI:** Jetpack Compose (100% declarative)
- **Data:** Firebase Realtime DB (cloud) + Room (local cache)
- **Navigation:** Compose Navigation with type-safe arguments

### Key Files

```kotlin
// App entry point
MainActivity.kt → NavHost → BottomNav + Screens

// Design tokens
ui/theme/Theme.kt → AppColors + AppTypography

// Reusable components
ui/components/Components.kt → BottomNavBar, XPBar, CourseCard, etc.

// VM pattern example
ui/home/HomeViewModel.kt
ui/home/HomeScreen.kt
```

### Building Custom Components

See `design_system/EASPEAK_Components_Guide.md` for:
- Component anatomy
- Kotlin Compose templates
- React JSX templates
- Design checklist

### Adding New Features

1. **Create screen folder:** `app/src/main/java/com/spanishapp/ui/myfeature/`
2. **Add ViewModel:** `MyFeatureViewModel.kt`
3. **Add Screen composable:** `MyFeatureScreen.kt`
4. **Register in Navigation:** `MainActivity.kt` (NavHost)
5. **Use design tokens:** `AppColors.*`, `AppTypography.*`
6. **Follow spacing rules:** `14.dp` (card padding), `20.dp` (content padding)

---

## 🌐 i18n (Internationalization)

- **UI Language:** Russian only (system strings, buttons, errors)
- **Content Language:** Spanish (vocabulary, examples, explanations)
- **Bilingual:** Russian + Spanish appear side-by-side intentionally

Future: Support for other languages (but keep Spanish + UI language bilingual).

---

## 🧪 Testing

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (on device/emulator)
./gradlew connectedAndroidTest

# Build & lint
./gradlew lint
```

---

## 📊 Firebase Setup

The app uses Firebase for:
- **Authentication:** Google Sign-In
- **Realtime Database:** User progress, leaderboard
- **Cloud Functions:** Optional (future)

### Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing)
3. Add Android app (package: `com.spanishapp`)
4. Download `google-services.json`
5. Place it in `app/`
6. Enable:
   - ✅ Google Sign-In (Authentication)
   - ✅ Realtime Database
   - ✅ Analytics (optional)

---

## 🚀 Deployment

### Building Release APK

```bash
# Build release APK
./gradlew assembleRelease

# Sign with keystore (configure first)
# Output: app/release/app-release.apk
```

### Publish to Google Play

1. Build signed release APK (see above)
2. Go to [Google Play Console](https://play.google.com/console)
3. Create app → Upload APK → Fill details → Submit

See `CHECKLIST.md` & `PLAN.md` for detailed roadmap.

---

## 📖 Documentation

| File | Purpose |
|---|---|
| **design_system/DESIGN_SYSTEM_GUIDE.md** | 🔥 Start here for design! |
| **design_system/README.md** | Original Figma design docs |
| **CHECKLIST.md** | Feature checklist & roadmap |
| **PLAN.md** | High-level roadmap |
| **LEARNING_GUIDE.md** | Setup & learning resources |
| **CLAUDE.md** | Claude AI integration details |

---

## 🐛 Troubleshooting

### Build fails with "Could not find com.google.firebase:firebase-bom"

→ Check internet connection, sync Gradle files (File → Sync Now)

### Emulator crashes on startup

→ Use Pixel 5+ emulator, API 30+, enable VT-x in BIOS (Windows)

### Firebase connection fails

→ Check `google-services.json` is in `app/`, verify Firebase project exists

### Design tokens not working

→ Make sure you're using `AppColors.*` and `AppTypography.*` from `Theme.kt`

---

## 📞 Support

- **Developer:** Samohin13
- **Issues:** https://github.com/Samohin13/SpanishApp/issues
- **Design System:** See `design_system/` folder

---

## 📜 License

This project is private. Modify for personal use only.

---

## 🎉 Next Steps

1. **Read design docs:** `design_system/DESIGN_SYSTEM_GUIDE.md`
2. **Clone & build:** `./gradlew build`
3. **Run on emulator:** `./gradlew installDebug`
4. **Explore the codebase:** Start with `MainActivity.kt`
5. **Add features:** Follow the MVVM + Compose patterns
6. **Deploy:** Build release APK → Google Play

---

**Happy coding! Удачи в разработке! 🚀**
