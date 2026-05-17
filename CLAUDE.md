# SpanishApp / ESPEAK — единый источник правды

> Android-приложение для изучения испанского языка русскоязычными пользователями (CEFR A1→B2).
> **Версия:** v1.11.7 (versionCode 65), AAB подан на закрытое тестирование Play.
> **Последний апдейт документа:** 2026-05-17. Все цифры верифицированы grep-проверкой кода.

---

## 📍 1. Текущее состояние

### Identity
- `applicationId = "com.espeak.app"` (изначально был `com.spanishapp`, сменён в v1.0.0 из-за конфликта в Play Store)
- `versionCode = 65`, `versionName = "1.11.7"`
- `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`
- Подписан собственным release.keystore (alias **ESPEAK**), V2-signed

### Технический стек
- **Язык:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material3 (composeBom 2024.12.01)
- **Архитектура:** MVVM + Clean (ui / domain / data)
- **DI:** Hilt 2.51.1
- **БД:** Room 2.6.1 (version=24, 23 миграции, 25 entities, 23 DAO)
- **Навигация:** Navigation Compose 2.8.4
- **Async:** Coroutines 1.9.0 + Flow
- **Медиа:** Media3 1.4.1 (ExoPlayer + MediaSession + HLS)
- **WorkManager:** 5 worker'ов фоновых задач
- **Виджет:** Glance AppWidget
- **HTTP:** OkHttp 4.12.0
- **Storage:** DataStore (8+ preferences-файлов)
- **ИИ:** Gemini Flash API через Cloudflare Worker proxy
- **Firebase:** Auth (Anonymous + Google), Firestore, Storage, Analytics, Crashlytics

### Кодовая база (grep-факты)
- **54** Composable Screens
- **43** ViewModels (@HiltViewModel)
- **25** Room entities
- **23** Room DAOs
- **23** Room миграции (v1→v24)
- **5** WorkManager worker'ов: DailyReminder, RatingDecay, ContentSync, RadioCatalogRefresh, [+1]
- **236** unit-тестов в 26 файлах

### Локализация UI
- `values/`: 900 строк (ru — основной)
- `values-en/`: 896 строк
- `values-uk/`: 900 строк
- `values-es/`: 900 строк
- ⚠ Контент уроков **только на русском** — UI переводится, контент нет

---

## 📦 2. Контент-инвентарь (verified by grep)

### Уроки
- **254** уникальных lesson ID в коде:
  - **240** в `LessonContentData.kt` (V1, базовый набор 16 unit × 15)
  - **+14 новых** в `LessonContentDataV2.kt` (суффикс `_5` — промежуточные:
    `u1_l13_5`, `u3_l5_5`, `u3_l7_5`, `u4_l13_5`, `u5_l8_5`, `u6_l9_5`,
    `u7_l5_5`, `u9_l11_5`, `u11_l5_5`, `u12_l9_5`, `u13_l5_5`, `u14_l9_5`,
    `u15_l11_5`, `u16_l4_5`)
  - **+49 overrides** в V2 (переписка существующих под xlsx-курс)
- **240** RoadmapLesson в `RoadmapData.kt` — ⚠ roadmap НЕ ЗНАЕТ про 14 новых
  V2-уроков. Контент есть, но юзер их в курсе не видит. **TODO: добавить
  в roadmap либо подтвердить что доступны через checkpoint-разблокировки.**

### Libros (книги/рассказы)
- **100/100** рассказов в `LibrosData.kt`:
  - A1: 25 шт
  - A2: 25 шт
  - B1: 15 шт
  - B2: 35 шт
- Каждый рассказ + 4 quiz-вопроса, перевод по long-press, sm2-стиль

### Словарь
- **10086** уникальных слов после dedup из:
  - `CleanVocab.kt` (4764 строк) — основной деduplicated
  - `BasicsVocab.kt` (1229 строк) — A1 фундамент
  - `VocabExtra1-12.kt` — 12 файлов расширений
  - `assets/spanish_vocab.json` — 1415 слов (подключены в v1.0.10)

### Спряжение глаголов
- **~1300** глаголов в `ConjugationData.kt` + `ConjugationData2.kt` + `ConjugationData3.kt`
- **159** с полными таблицами 6 времён
- Verb trainer (5 тиров × 6 времён в backlog для PRO)

### Грамматика
- **75 уроков**: A1×15, A2×20, B1×20, B2×20
- Грамматический трекинг (GrammarScreen)

### Игры
- **8 экранов** в `ui/games/`:
  1. ArticlesGameScreen — артикли el/la/un/una (100 уровней через `GameLevelManager`)
  2. SpeedGameScreen — на скорость
  3. SopaGameScreen — поиск слов
  4. PalabraMaestraScreen — палач
  5. MathGameScreen — испанские числа
  6. CrosswordGameScreen — кроссворд (100 уровней + zoom/pan)
  7. VerbTrainingScreen — тренажёр глаголов
  8. LibrosScreen / LibroReadScreen — книги
- Хаб: GamesScreen

### Theory cards
- **10** теория-карточек блока 1.1 (u1_l0..u1_l9)
- TheoryReader + TheoryLibrary
- ~3-5 мин чтения каждая

### Достижения
- **23** достижения в `AchievementNotificationService.defaultAchievements` (bronze/silver/gold через xpReward)
- Единая иконка 🏆 + auto-meta refresh при апгрейде

### Радио (см. §6)
- 40 hardcoded fallback + dynamic auto-discovery
- 3 страны (ES/MX/AR)

---

## 🏗 3. Структура проекта

```
app/src/main/java/com/spanishapp/
├── MainActivity.kt                       — entry + SpanishAppRoot (nav + bottom bar)
├── SpanishApp.kt                         — Application (@HiltAndroidApp)
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt                — v24, 23 миграции
│   │   ├── DatabaseSeeder.kt             — seedIfNeeded, JSON + Kotlin данные
│   │   ├── BasicsVocab.kt / CleanVocab.kt — словари
│   │   ├── VocabExtra1..12.kt
│   │   ├── ModernVocab.kt                — современная лексика
│   │   ├── dao/Daos.kt                   — 23 DAO интерфейса
│   │   └── entity/Entities.kt            — 25 Room сущностей
│   ├── content/                          — Content delivery (Firebase Storage → CDN gh-pages)
│   ├── prefs/                            — DataStore: VoicePreferences, AppPreferences, etc.
│   ├── repository/
│   │   ├── AiChatRepository.kt           — Gemini Flash через Cloudflare proxy
│   │   ├── AuthRepository.kt             — Firebase Auth
│   │   ├── GeminiTranslator.kt           — fallback переводчик
│   │   ├── ContentDownloader.kt          — OTA content packs
│   │   └── ConjugationData*.kt
│   └── theory/                           — TheoryContent + Data
├── di/AppModule.kt                       — Hilt providers
├── domain/algorithm/
│   ├── LearningAlgorithms.kt             — SM-2, XpSystem, SkillRating v2, LeagueResolver
│   └── RatingUpdater.kt
├── radio/                                — модуль радио (см. §6)
├── service/
│   ├── DailyReminderWorker.kt
│   ├── RatingDecayWorker.kt
│   ├── ContentSyncWorker.kt
│   ├── AchievementNotificationService.kt — 23 ачивки
│   └── SpeechServices.kt                 — TTS + STT
├── ui/                                   — 54 Screens, 43 ViewModels
│   ├── Navigation.kt                     — NavHost graph
│   ├── auth/                             — Welcome/Register/Login/Onboarding
│   ├── home/                             — HomeScreen, LessonContent, RoadmapData
│   ├── flashcards/                       — Flashcards SM-2
│   ├── games/                            — 8 игр
│   ├── chat/                             — AI Chat
│   ├── radio/                            — UI радио
│   ├── theory/                           — Theory cards
│   ├── profile/                          — Profile, Avatar
│   ├── leaderboard/                      — Leaderboard, WeeklyLeague
│   ├── settings/                         — Settings, SettingsVoice
│   ├── dictionary/                       — Dictionary, WeakWords
│   ├── pronunciation/                    — Произношение через STT
│   └── components/                       — общие Composable (BottomBar, AppColors, etc.)
└── widget/WordOfDayWidget.kt             — Glance widget «Слово дня»
```

---

## 🗄 4. База данных (Room v24)

### Версии и миграции
| Версия | Что добавлено |
|---|---|
| v1 → v7 | Базовые таблицы (words, conjugations, lessons, dialogues, user_progress, chat_messages, achievements, daily_words, word_lists, article_game, lesson_progress, libro_progress) |
| v8 → v11 | Game level progress, daily XP, flashcard sets |
| v12 → v14 | Recent searches, dialogue, content sync |
| v15 → v18 | Rating system v2 (daily_rating_gain, last_rating_at, weekly_league_state) |
| v19 → v20 | Skill rating сброс на 0, новые лиги |
| v21 | Theory progress |
| v22 → v24 | Radio favorites, catalog, listening sessions |

### Сущности (25)
WordEntity, ConjugationEntity, LessonEntity, DialogueEntity, UserProgressEntity, ChatMessageEntity, AchievementEntity, DailyWordEntity, WordListEntity, ArticleGameProgressEntity, LessonProgressEntity, LibroProgressEntity, GameLevelProgressEntity, DailyXpEntity, FlashcardSetProgressEntity, RecentSearchEntity, WeeklyLeagueStateEntity, WodHistoryEntity, TheoryProgressEntity, RadioFavoriteEntity, RadioCatalogEntity, RadioListeningSessionEntity, RadioWordCatchEntity *(legacy, без DAO)*

### Миграции зарегистрированы в 5 местах
- `AppModule.provideDatabase` (главный)
- `RatingDecayWorker.doWork`
- `ContentSyncWorker.doWork`
- `WordOfDayWidget.kt`
- `RadioCatalogRefreshWorker.doWork`

**Важно:** при добавлении новой миграции обновить все 5. **`fallbackToDestructiveMigration` ТОЛЬКО в debug** (с v1.11.7).

### DataStore preferences
- `voice_prefs` — TTS settings (8 персонажей, rate, pitch)
- `app_preferences` — общие настройки
- `app_lock_prefs` — биометрия
- `auth_prefs` — Firebase auth state
- `radio_blocklist` — мёртвые станции (TTL 48ч, JSON map)
- `content_versions` — версии content packs
- `ai_chat_limit` — счётчик 50/день
- ... (см. `data/prefs/`)

---

## 🎓 5. Системы обучения

### SM-2 алгоритм (флэшкарты)
- Hard (q=2): сброс repetitions, interval=1d
- Good (q=4): interval × easeFactor
- Easy (q=5): interval × easeFactor + bonus EF
- `isLearned=true` при repetitions ≥ 3
- 3-bucket fallback pool: weak → shaky → reviewed

### XP система
- Word правильно (Good): +5 XP
- Word easy: +10 XP
- Урок: +25 XP, Диалог: +40 XP, Дневная цель: +15 XP
- Streak бонус: +2 × дней (max 60)
- 30 уровней (`XpSystem.LEVEL_THRESHOLDS`)

### SkillRating v2 (с v1.1.0)
- Старт **0** (раньше 1000, сменили в v1.1.0)
- Tier-aware K: Aldea ±12 → Madrid ±2
- Promo resistance × 0.5 за 30 пунктов до тира
- Daily cap +40
- Per-word 24h cooldown
- Progressive decay: 1-5 дней по -5/день, 6-12 по -8/день, 13+ по -12/день
- Floor: peakRating сохраняется как личный рекорд

### Лиги «Путь до Мадрида» (8)
0-99 Aldea perdida → 100-299 Santiago de Compostela → 300-599 Bilbao → 600-999 Zaragoza → 1000-1499 Valencia → 1500-2099 Sevilla → 2100-2799 Barcelona → 2800+ Madrid

### Weekly Leagues
- Firestore cohorts (uid hash bucketing)
- Опт-ин через диалог
- Tab в LeaderboardScreen

### Streak
- Consecutive day → +1, miss → 1, warning при 20+ часах
- Streak freezes (❄N иконка на главной)

### Daily Mission (5 целей)
1. Урок дня
2. Флэшкарты ≥ 1 сет
3. Книга — прочитана глава
4. Word of Day разгадано
5. 5 мин радио (≥300 сек)

---

## 📻 6. Радио (модуль production-ready)

### Архитектура (canonical Media3 с v1.10.4)
```
UI (RadioScreen / RadioMiniPlayer)
  → RadioViewModel (@HiltViewModel)
    → RadioPlayerController (Singleton, facade c MediaController)
      → [Binder] → RadioPlayerService (MediaSessionService)
                     → ExoPlayer + MediaSession (владелец)
```

### Что работает
- Lock screen + шторка с media controls (⏮ ⏯ ⏭)
- Audio focus (звонки, ducking), headphone unplug, wake mode NETWORK
- Auto-reconnect (3 попытки с backoff 1s/2s/4s)
- Dead station auto-blocklist (TTL 48ч в DataStore)
- Auto-skip на следующую рабочую (max 5/min — защита от loop)
- Discovery с прогрессом stages (DETECTING_COUNTRY → FETCHING_CATALOG → PROBING)
- Brand dedup (max 2 на сеть: 2 SER, 2 RNE, ...)
- 6 фильтр-чипов: Music/Talk/News/Sports/Culture/Favorites (multi-select)
- ICY metadata через `Player.Listener.onMetadata` (IcyInfo + ID3 TextInformationFrame)
- Loudness normalization +4dB (LoudnessEnhancer audio effect)
- Hero animated 3-stop gradient + iOS-glass карточки
- Haptic + press-scale на кнопках
- Swipe-to-hide mini-player (не stop, auto-show при возврате в радио)
- Mini-player tap → возврат в страну играющей станции
- Notification skip prev/next через multi-item playlist
- Landscape adaptive (hero 160dp vs 240dp)
- WorkManager weekly catalog refresh (UNMETERED + batteryNotLow)
- 40 unit-тестов (sanitize ICY, URL safety, brand dedup, blocklist TTL)

### Не сделано (отложенный backlog)
- Sleep timer
- Lockscreen artwork (gradient bitmap)
- Recently played карусель
- Listening streak отдельный
- Achievements за радио
- Android Auto support
- Whisper транскрипция (holdback — $1000/мес ongoing cost)
- Локализация ~478 русских литералов в `radio/*` на en/uk/es

### Файлы
```
radio/
├── data/
│   ├── Station.kt                — Country (ES/MX/AR), Genre, CefrLevel, data class
│   ├── StationRepository.kt      — 40 hardcoded fallback
│   ├── RadioFavoriteEntity       — Room таблица избранного
│   ├── RadioCatalogEntity        — dynamic catalog cache
│   ├── RadioCatalogRepository    — API + probe + brand dedup
│   ├── RadioBlocklistPrefs       — DataStore persistent blocklist (TTL 48ч)
│   └── RadioListeningEntity      — listening time tracker
├── player/
│   ├── RadioPlayerService.kt     — MediaSessionService, владеет ExoPlayer
│   ├── RadioPlayerController.kt  — facade c MediaController
│   ├── RadioCatalogRefreshWorker.kt — weekly refresh
│   ├── RadioCoordinator.kt       — TTS↔Radio mutex
│   └── HapticManager.kt
└── ui/
    ├── RadioScreen.kt            — главный экран (no scroll, single-view)
    ├── RadioViewModel.kt
    └── RadioMiniPlayer.kt        — global overlay над BottomBar
```

---

## 🤖 7. ИИ-репетитор (Gemini Flash + Cloudflare Worker)

### Архитектура
```
UI → AiChatRepository → Cloudflare Worker proxy
                          → X-App-Secret header
                          → Gemini Flash API
```

- **Worker URL:** `espeak-gemini-proxy.bravochief21.workers.dev`
- **Модель:** `gemini-flash-latest` (auto-alias на актуальную бесплатную)
- **Системный промпт:** дружелюбный репетитор для русскоязычных A1/A2, короткие ответы (4-5 строк), корректировки в формате `CORRECTIONS_JSON:[...]`
- **История:** 20 последних сообщений из Room (`chat_messages`)
- **Лимит:** 50 запросов/день через `AiChatLimiter` (DataStore)
- **Безопасность v1.11.7:**
  - В release `BuildConfig.GEMINI_API_KEY` пустой (не запекается)
  - `AiChatRepository.apiUrl()` и `GeminiTranslator.apiUrl()` крашат с `IllegalArgumentException` если в release нет proxy
  - Worker валидирует `X-App-Secret` (ENV `APP_SECRET`) — random callers получают 403
  - `humanizeError()` — понятные сообщения вместо raw JSON

---

## 🚀 8. Roadmap (что РЕАЛЬНО не сделано)

### Высокий приоритет
1. **Локализация контента уроков** на en/uk/es — UI переведён, контент русский (большой scope)
2. **V2 курса (xlsx) рефакторинг** — 49/240 уроков переписаны под xlsx, остальные на V1 (тоже валидны, V2 — улучшение качества)
3. **14 V2-only уроков (`_5` суффикс) НЕ в roadmap** — есть в `LessonContentDataV2.kt`,
   `TheoryContentData.kt`, `VocabScope.kt` (все 3 уровня контента готовы), но
   юзер их не видит т.к. `RoadmapData.kt` ещё имеет 240 пунктов. Нужно добавить
   `RoadmapLesson` для каждого из 14 промежуточных уроков, либо удалить
   контент если не планируется
4. **22 IMPORTANT из audit 2026-05-17** (best practices):
   - `rememberSaveable` на формах (потеря ввода при rotation)
   - `popUpTo("home")` safe-fallback
   - `MainActivity` runBlocking DataStore → async
   - Cloudflare Worker repo sync с production
   - 16 более мелких

### Средний приоритет
4. **Sleep timer радио** (~1.5ч)
5. **Lockscreen artwork радио** (~1.5ч)
6. **Recently played карусель** (~1.5ч)
7. **Voice EQ для TALK станций** (boost mid)
8. **Listening streak** отдельный от learning
9. **Achievements за радио** («1 час», «10 часов»)
10. **Android Auto** через MediaBrowserService (~2ч)
11. **Локализация radio модуля** (~478 строк × 3 локали)
12. **CLAUDE.md периодический sync с кодом**

### Низкий / отложено
13. Whisper транскрипция (holdback — $1000/мес)
14. Share station deep link
15. «T» button флип карточки → перевод (обсуждали, отменили)
16. **Google Play Billing для PRO** — план есть, не реализовано
17. Озвучка Libros актёрами

---

## 💎 9. Монетизация (план v2.0, утверждён 2026-05-14)

### Бесплатно
- Уроки A1 (60 шт), грамматика A1, диалоги A1, книги A1
- Спряжение A1, игры первые 10 уровней каждой
- Карточки SM-2 для слов A1
- **Словарь полностью** (10086 слов — handbook-функция)
- **Pronunciation полностью** (motor skill, не зависит от уровня)
- AI Chat — 50 запросов/день
- Слово дня + WoD-streak + push
- Достижения, лидерборд, лиги, weekly leagues
- Виджет, темы, био-замок
- **Радио полностью**

### 💎 PRO (план)
- Уроки A2 + B1 + B2 (180 шт)
- Грамматика A2 + B1 + B2
- Диалоги A2 + B1 + B2
- Книги A2 + B1 + B2 (75 рассказов из 100)
- Полное спряжение (1300+ глаголов, 159 с таблицами 6 времён)
- Все 100 уровней каждой игры
- Карточки SM-2 для слов A2/B1/B2
- AI Chat — **безлимит**

### Цены (через Google Play Billing с auto-pricing)
- **Месяц:** $4.99 (~450₽ / ~2200₸ / 4.99€)
- **Год:** $34.99 (~3150₽ / ~17000₸ / 34.99€) — экономия 42%
- **Trial:** 7 дней бесплатно PRO

### Реализация
- ❌ Не реализовано — Google Play Billing Library 7 + gate содержимого + restore purchases

---

## 🔒 10. Безопасность (после аудита 2026-05-17, фиксы в v1.11.7)

### Закрыто
- ✅ GEMINI_API_KEY больше не в release APK (только debug BuildConfig)
- ✅ Cloudflare Worker secret enforced (X-App-Secret)
- ✅ Firebase Storage rules: size 5MB + image/* MIME (applied в Console)
- ✅ ANTHROPIC_API_KEY (dead code) удалён из BuildConfig
- ✅ ProfileScreen Log.d с downloadUrl → if (BuildConfig.DEBUG)
- ✅ GeminiTranslator получил release-guard
- ✅ `radioWordCatchDao()` (Hilt bomb) обезврежен
- ✅ `fallbackToDestructiveMigration` только в debug (5 мест)
- ✅ RadioViewModel callback leak fixed (`onCleared` nullable)
- ✅ SpeechRecognizer cancellation race fixed (AtomicBoolean guard)

### Permissions (manifest)
INTERNET, ACCESS_NETWORK_STATE, RECORD_AUDIO (runtime), POST_NOTIFICATIONS (runtime API 33+), VIBRATE, FOREGROUND_SERVICE, FOREGROUND_SERVICE_MEDIA_PLAYBACK, WAKE_LOCK, MODIFY_AUDIO_SETTINGS.
Comment'нуто: CAMERA (убрана т.к. PickVisualMedia без permission).

### Network security
`res/xml/network_security_config.xml`:
- cleartext РАЗРЕШЁН by default (нужен для радио потоков без HTTPS)
- ЗАПРЕЩЁН для критичных API (radio-browser, country.is, workers.dev, Firebase, Anthropic, Google AI)

### Firebase Rules
- **Firestore:** строгие, uid-bound write, schema validation, default-deny
- **Storage:** users/{uid}/* — 5MB + image MIME, контент-пакеты публичны на read
- **Auth:** Anonymous + Google провайдеры включены

### Privacy
- PRIVACY_POLICY.md публичный (gh-pages: https://samohin13.github.io/SpanishApp/PRIVACY_POLICY.html)
- Crashlytics включён, без user-PII
- Delete-account flow есть (`SettingsScreen.kt:348`) — GDPR-friendly

---

## 🧪 11. Тесты и QA

### Unit tests (236 в 26 файлах)
Ключевые suite:
- `RadioSanitizeTest` (11) — ICY metadata sanitization
- `RadioUrlSafetyTest` (11) — URL whitelist
- `RadioBrandDedupTest` (10) — max 2 на сеть
- `RadioBlocklistTtlTest` (7) — TTL filtering
- `SkillRatingSystemV2Test` (16)
- `LeagueResolverTest` (14)
- `Migration18To19Test` (5)
- `AchievementCatalogTest` (8)
- `StreakManagerTest` (8)
- `WordOfDayStreakLogicTest` (5)
- `XpSystemTest` (5)
- `AiChatLimiterTest` (4)
- `LibroTextHelpersTest` (20)
- `ExerciseGeneratorTest`, `CrosswordTest`, `RatingSystemTest`, ...
- `LocalizationIntegrityTest` ⚠ 1 known failure (incomplete translations, не блокер)

### Запуск
```bash
./gradlew :app:testDebugUnitTest                      # все
./gradlew :app:testDebugUnitTest --tests "com.spanishapp.Radio*"  # только радио
./gradlew :app:koverHtmlReportDebug                   # coverage HTML
./gradlew :app:preRelease                             # lint + tests + AAB + coverage
```

### QA-инфраструктура (docs/qa/)
- TEST_STRATEGY.md, TEST_CASES.md (110 кейсов), SMOKE_TEST.md (15 P0 кейсов / 10 мин), RELEASE_CHECKLIST.md, MANUAL_QA_GUIDE.md, BUG_REPORT_TEMPLATE.md, BETA_TESTER_GUIDE.md, GITHUB_WORKFLOW.md
- GitHub Issues с labels (P0/P1/P2/P3 + bug/enhancement/tech-debt), 3 milestones, 3 issue templates

---

## 📦 12. Версии релизов

| Версия | versionCode | Что |
|---|---|---|
| 1.0.0 | 6 | Первый релиз в Play (закрытая альфа) |
| 1.0.5 | 11 | Краш на старте — отозван |
| 1.0.6-1.0.10 | 12-16 | Иконка, ProGuard, async виджет, 5 критичных багов (lesson count, photo, libros, push perm, vocab JSON) |
| 1.1.0 | 17 | Большой батч 11 фиксов «Качество и баланс» |
| 1.2.0 | 19 | Theory cards Phase 1 (10 теорий) |
| 1.6.0 | 26 | Radio launch (40 станций, ExoPlayer, MediaSession) |
| 1.6.1-1.6.7 | 27-31 | Radio фиксы и Spotify-стиль |
| 1.7.0 | 32 | Radio auto-discovery (ip-API + radio-browser + probe) |
| 1.8.0-1.8.4 | 33-37 | Listening tracker, daily mission, TTS↔Radio mutex |
| 1.9.0-1.9.1 | 41-42 | Radio редизайн + foreground service crash fix |
| 1.10.0-1.10.9 | 48-57 | Audio focus, ICY metadata, Media3 refactor, brand dedup |
| 1.11.0-1.11.5 | 58-63 | Top-5 polish (haptic, loudness, animations, blocklist persist) |
| 1.11.6 | 64 | AI Chat security (Gemini proxy enforced) |
| **1.11.7** | **65** | **Текущая** — 10 critical из audit + AAB подан в Play |

---

## 🔧 13. Полезные команды

```bash
# Сборка
./gradlew :app:assembleDebug              # debug APK
./gradlew :app:bundleRelease              # release AAB
./gradlew :app:preRelease                 # lint + tests + AAB + coverage

# Тесты
./gradlew :app:testDebugUnitTest
./gradlew :app:koverHtmlReportDebug       # → app/build/reports/kover/html/index.html

# Проверка
./gradlew :app:lintRelease                # → app/build/reports/lint-results-release.html
./gradlew :app:compileDebugKotlin
./gradlew :app:compileReleaseKotlin

# Git workflow
git add . && git commit -m "message"
git push origin master
```

### Ключевые пути
- AAB: `app/build/outputs/bundle/release/app-release.aab`
- Lint report: `app/build/reports/lint-results-release.html`
- Coverage: `app/build/reports/kover/html/index.html`
- Crashlytics console: Firebase Console → проект ESPEAK → Crashlytics
- Cloudflare Worker: https://dash.cloudflare.com → `espeak-gemini-proxy`
- API keys: https://aistudio.google.com/app/apikey

---

## 📜 14. Правила работы с пользователем

Закреплено в memory:

1. **Пользователь — новичок** в Android/Git. Пошаговые инструкции, где кликать в IDE, какие команды в терминал.
2. **Коммитить каждую итерацию** автоматически — не ждать разрешения. Push в `origin/master`.
3. **Проверять код ПЕРЕД вопросом юзеру** — Grep + Read до того как уточнять у юзера про настройки/интеграции.
4. **Память:** `C:\Users\bravo\.claude\projects\C--Users-bravo-AndroidStudioProjects-SpanishApp2\memory\`
5. **Имя приложения:** ESPEAK (не HablaRu, не SpanishApp). package = `com.spanishapp` (namespace), applicationId = `com.espeak.app`.
6. **AAB подпись:** `release.keystore` + `keystore.properties` (в .gitignore)

---

## 🌐 15. Внешние ресурсы

- **GitHub:** Samohin13/SpanishApp (branch: master)
- **Play Console:** es.espeak13@gmail.com
- **GitHub Pages:** https://samohin13.github.io/SpanishApp/ (Privacy Policy, gh-pages branch)
- **Content packs CDN:** https://samohin13.github.io/SpanishApp/content_packs/manifest.json
- **AI proxy:** https://espeak-gemini-proxy.bravochief21.workers.dev/

---

## 🗂 16. Структура документации

После консолидации (2026-05-17) в репе остались только essential .md:

- **`CLAUDE.md`** (этот файл) — единый источник правды
- `PRIVACY_POLICY.md` — публичная страница для Play Console (legal)
- `PRIVACY_POLICY.html` — HTML-зеркало для gh-pages
- `LICENSES.md` — open-source licenses
- `index.md` — GitHub Pages landing
- `backend/cloudflare-worker/README.md` — отдельный backend проект
- `design_system/*.md` (6) — отдельный design system project
- `docs/qa/*.md` (8) — формальная QA инфраструктура
- `.github/ISSUE_TEMPLATE/*.md` (3) — GitHub Issues templates

Удалено в этой консолидации (содержимое поглощено CLAUDE.md):
PLAN.md, CHECKLIST.md, README_NEW.md, LEARNING_GUIDE.md, radio.md, docs/ADS_PLAN.md, docs/articles_game_design.md, docs/AUDIT_REPORT.md, docs/DONATIONS_PLAN.md, docs/LESSON_EXERCISES_PLAN.md, docs/MINDMAP.md, docs/MONETIZATION_PLAN.md, docs/PLAY_CONSOLE_CHEATSHEET.md, docs/PLAY_STORE_LISTING.md, docs/PUBLISH_PRIVACY_POLICY.md, docs/RELEASE_CHECKLIST.md, docs/SCREENS.md, docs/play_assets/SCREENSHOTS_GUIDE.md.
