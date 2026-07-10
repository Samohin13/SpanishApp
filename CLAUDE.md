# SpanishApp / ESPEAK — единый источник правды

> Android-приложение для изучения испанского языка русскоязычными пользователями (CEFR A1→B2).
> **Версия:** v1.25.95 (versionCode 197). Канон диалекта: **Spain Madrid**.
> **Последний апдейт документа:** 2026-06-19.

> ⚠️ **Документ частично stale.** Header + §1 «Текущее состояние» + §12 «Версии релизов»
> обновлены до v1.25.95. Остальные секции (§2 Контент-инвентарь, §4 БД, §8 Roadmap,
> § «🆕 v1.25.x batch») описывают код от v1.25.6 / Room v24 / 23 миграций — реальность
> v1.25.95 / Room v32 / 31 миграция / 27 entities / 24 DAO. Подробности дрифта в
> [audit-доке]. Не цитируй числа из § ниже без перепроверки grep'ом.

## 🆕 Что произошло за v1.25.89 → v1.25.95 (краткая хронология)

- **v1.25.89** — убран Android Auto (закрытие Play Console reject «No items» в AA browse).
- **v1.25.90** — 6 групп блокеров: email-login Firestore sync, logout/deleteAccount
  очищает auth_prefs целиком, SubscriptionVerifier больше не silent revoke PRO на
  transient errors (defense-in-depth + PlayBillingManager.isDefinitelyInvalid),
  v1.25.88 displayName backfill починен (sentinel "Estudiante"), Grammar+Theory PRO gate,
  Dialogues +40 XP, RoadmapData u14/u15 B2 контент маппинг (3 урока u14 + 6 уроков u15
  показывали не тот V2 content).
- **v1.25.91** — Tier-1 educational fixes (15): u5_l8 «Hizo un error»→«Cometió un error»
  (CRITICAL calque), u6_l9 leísmo SPOT_THE_ERROR correctAnswer flipped, u8_l5 cyrillic
  «ир»→latin «ir», TheoryContentData «БЛЯР»→«БЛАР», + 11 LISTEN_PICK duplicate option
  fixes, missing comma «por favor», la mano «рука/кисть», etc.
- **v1.25.92** — Tier-2 (11): u11_l4 «el día CUANDO fui»→«en que fui», u13_l10 non-word
  «lleguas»→«llegaras», u14_l6_5 «debe haber»→«debe DE haber», u15_l1 «Le ruego solucionar»
  →«Le ruego que solucione», u15_l14 «agradezco POR»→«agradezco SU», Grammar A2
  Comparativo age vs size, Grammar A2 «tuve/tuvo» tip fix, и др.
- **v1.25.93** — Phase 2 finish: Theory dialect Spain (Z как [θ] вместо [с] во всех
  карточках, greetings tabla до la comida ~14-15ч, Adiós-as-greeting overstatement
  убран); u6_l2 ОГРОМНОЕ расширение Indef vs Imperf (interruption + 9 semantic shifts
  sabía/supe + 5 новых упражнений — главный A2 gap для русских); u8_l4 Type-2 conditional
  помечен recognition-only; u14_l11 estilo indirecto добавлены Indicative shifts +
  Pres reporting + deixis + 5 новых упражнений (biggest B2 gap); Grammar id=17
  Subj Imperfecto перенесён B2→B1 (CEFR ordering break).
- **v1.25.94** — Phase B audit: ConjugationData2 cocinar/preferir critical typos с
  пробелами внутри слов (trainer отвергал правильные ответы!), oír/oir lookup
  mismatch в VerbBank, vocab fixes (mañana без ñ, trigre→tigre, diciembre перевод
  был «diciembre», 5 wrong genders rascacielos/castaña/almohadilla/triple/merengue),
  Libro #81 quiz AI editing artifact «...espera, sí podría» убран.
- **v1.25.95** — Tier 1 technical audit fixes (date locale, fake PerformanceLoadTest
  assertions, broken androidTest package name).
- **v1.25.97-98 (2026-07-10, ветка → master)** — БОЛЬШОЙ мульти-аудит (6 агентов:
  security / billing / курс / рейтинги+награды / игры / книги+TTS) + фиксы:
  - **Security**: 0 critical/high. Worker verify-purchase: uid из Firebase-токена
    (был спуфинг PRO из body — ⚠️ ТРЕБУЕТ ДЕПЛОЯ ВОРКЕРА). SubscriptionVerifier
    fail-closed в release.
  - **PRO-гейты закрыты**: диалоги A2+, книги через прямые маршруты, кнопка
    «Дальше» в 6 играх (FREE_GAME_LEVELS=10), lesson_intro/lesson_session
    (onboarding deep-link), кроссворд «уровень 101».
  - **XP-целостность**: addXpAndWords больше не инкрементит lessons_completed
    (каждый XP-источник давал фейковый «+1 урок»); grammar/achievements XP
    реально начисляются (были фантомными); checkpoint/minitest XP только
    first-pass; words_learned = переход isLearned (не каждый ответ); weekly XP
    после cooldown; Firestore rating fallback ?:0 (был 1000).
  - **Курс**: u10 сдвиг 5 уроков починен (id u10_l10..l14); 2 невыигрываемых
    упражнения; чекпоинты пишутся в lesson_progress (юнит больше не 15/16);
    cp-маппинг cp1..cp16 (был cp1-4 → A2+ чекпоинты автопроходились на «Поехали»).
  - **Verbos**: генератор больше НЕ УЧИТ неправильным формам — орфография
    g→j/c→z/gu→g из написания инфинитива + stem-сдвиги в -zar/-gar/-car
    (dirijo/cojo/venzo/empiece/almuerce); 17 ре-типизаций банка; jugar/avergonzar
    → AUTHORED; +18 тестов SpanishConjugatorTest.
  - **Игры**: Math «Работа над ошибками» реанимирована (была мертва), Articles
    дубликация таблицы на уровнях 41+, Palabra ошибки в Stats, weak-verbs счётчик.
  - **TTS**: offline = локальный fallback вместо тишины (onAllFailed);
    атомарный кэш mp3; es-ES приоритет в авто-выборе голоса; quiz-опции книг
    шафлятся (70% ответов были «B»).
  - **Rating decay честный**: якорь = последняя активность, дельта между
    прогонами воркера (была вечная -5/3дня); zero-delta ответы обновляют таймштамп.
  - **AI-чат PRO-only** подтверждён решением владельца, AiChatLimiter (50/день)
    удалён как мёртвый код.
  - Отдельная ветка `claude/compliance-targetsdk36-billing8`: targetSdk 36 +
    Billing 8.0.0 (Play deadline 31 авг 2026) — НЕ мержить без device-теста покупок.
  - **Волна 2 аудита** (радио/воркеры/виджеты, флэшкарты/словарь/произношение/
    теория, auth/синк/настройки, навигация/l10n/старт) — 17 фиксов: краш
    course_a1 после WoD-квиза; GDPR (users/{uid}/state/main не удалялся,
    чат+голосовые переживали Delete Account); био-замок запирал навсегда
    (+DEVICE_CREDENTIAL, sign-out снимает замок); скоринг произношения
    (артикли/пунктуация обеих сторон); 12 неправильных родов в словаре
    (+obsoleteKeys purge в сидере); «claro=светлый» в сете приветствий;
    restart set/weak-сессий; _5-уроки в облачном restore; ротация сбрасывала
    урок/placement/сплэш (rememberSaveable); диплинки обходили замок;
    ContentSyncWorker не был запланирован (OTA мёртв); reminder без tap-intent.
  - **Волна 2 — отложенный бэклог** (задача #6): radio session-запись умирает
    с VM (мимо daily mission №5); теория — 91 карта затенена дублями ключей,
    8 уроков показывают ЧУЖУЮ тему (нужен topic-mapping); поиск словаря не
    находит кириллицу с заглавной/без акцентов; anonymous→permanent linking
    отсутствует (ghost-дубли лидерборда); Room-загрязнение между аккаунтами
    на общем устройстве; ~1420 русских литералов вне frozen-модулей (paywall
    74 — топ приоритет); weekly-лига промоутит неактивных в мелких когортах.

⚠️ Educational backlog: ~120 medium/low findings из Phase 1+2 audit'ов (verb imperativo
data для 60 AUTHORED — imperativo для правильных теперь генерится корректно, для
AUTHORED пропускается; vocab ~30 entries cleanup, libros CEFR re-leveling,
pronunciation redesign, theory ~190 duplicate Map keys).

## 🆕 v1.25.x Chat / Voice / Billing batch (2026-05-28..29)

Самый большой sprint после v1.22 — переработка чата и подписки.

### Voice messages (v1.25.0)
- Полный stack: VoiceRecorder (MediaRecorder AAC-LC m4a 64kbps mono 22050Hz)
  + VoiceMessageStorage (filesDir/voice_messages/) + VoicePlayer (MediaPlayer)
- ChatMessageEntity + audioPath: String?, audioDurationMs: Long
- Room v27 → v28 — `MIGRATION_27_28` зарегистрирована в 6 местах
- ChatComposer recording overlay: ✗ cancel | ● pulse dot | mm:ss | waveform
- ActionButton: tap во время recording → stopAndSend
- VoiceMessagePlayer composable в bubble: play/pause + progress + duration
- Permission flow: RECORD_AUDIO через rememberLauncherForActivityResult

### Per-scenario characters (v1.25.1)
ChatScenario + welcomeEs, welcomeRu, characterName.
Каждый сценарий = живой персонаж:
- **Carlos** (Travel) — гид Madrid, vale/venga/tío
- **Marta** (Restaurant) — официантка, paella/jamón/sangría
- **Sr. López** (Interview) — HR Telefónica/BBVA, CV/sueldo
- **Lucía** (Shopping) — Zara dependiente, talla/probador
- **Dr. Ramírez** (Doctor) — médico, síntomas/receta (только language practice)

System prompt: "Ты — $characterName, дружелюбный собеседник из Мадрида".
WelcomeBubble показывает welcomeEs + welcomeRu из сценария.

### Glide-typing polish (v1.25.2)
- Row 3 (z-m / ячсмитьбю) теперь в glide
- First-tap rollback: valueAtDown snapshot, накладывается matched word
- Visual trail (Canvas drawLine) с alpha gradient 0.15→0.8 за курсором

### Audit IMPORTANT (v1.25.3)
- rememberSaveable на Login/Register/ForgotPassword email+password
- Lint --release: 1 Error fixed (SuspiciousIndentation в AiChatScreen)

### Google Play Billing (v1.25.4)
- libs.versions.toml: billing = "7.1.1"
- PlayBillingManager: connect/queryProducts/launchPurchase/restore/acknowledge
- PaywallViewModel.startPurchase(activity) — реальный launchBillingFlow
- SubscriptionPreferences.setPro(active) — production setter
- SpanishApp.onCreate → playBillingManager.start() в runCatching

**Play Console setup нужен:** product `espeak_pro` + base plans
`monthly` ($4.99) + `yearly` ($34.99) + Internal testing track.

### StreakFreezePopup + restore purchases (v1.25.5)
- StreakFreezePopupHost — баннер "❄ Стрик сохранён!" когда freeze срабатывает
  (раньше юзер не знал что freeze тратится)
- Settings → Премиум → "Восстановить покупки" (для переустановки app)

### Radio batch (v1.25.6)
- RadioViewModel.listeningStreak: consecutive days с активным прослушиванием
- RadioPlayerService: Equalizer для voice EQ (boost 800Hz-3kHz при включении)
- ~~Android Auto~~ — добавлены manifest-декларации, но без `MediaLibraryService` /
  browse-tree. Google отклонил v1.25.88 (versionCode 190) с reject «No items»
  в AA browse. В **v1.25.89** интеграция полностью убрана из manifest +
  удалён `res/xml/automotive_app_desc.xml`. Когда будем возвращать —
  нужна полноценная `MediaLibraryService` с `onGetLibraryRoot` +
  `onGetChildren`, возвращающими browseable nodes (Stations / Favorites /
  Genres) → playable `MediaItem` со `setIsBrowsable(true)` для категорий.

## 🆕 Chat AI rewrite (v1.24.x, более ранний этой же сессии)

### Структура
- AiChatScreen + AiChatViewModel + AiChatRepository (Gemini Flash через Cloudflare Worker)
- ChatArchiveScreen + ChatArchiveViewModel + DAO `observeSessionsMeta`
- SpanishKeyboard (1100+ LOC) — full custom Compose клавиатура
- KeyboardLogic — pure functions (60 unit tests)
- GlideMatcher + WordSuggester + UserWordFrequency + KeyboardLogic

### Клавиатура features (v1.24.x → v1.25.x)
- Fire-on-DOWN отклик (Gboard-стиль) через detectTapGestures(onPress)
- rememberUpdatedState на все callbacks (фикс stale closure после 3 chars)
- Custom blinking cursor через onTextLayout (readOnly BasicTextField не рисует свой)
- Continuous accent gesture: hold → slide → release (iOS-стиль)
- Swipe-cursor на space + tap-without-drag = пробел
- Caps lock через double-tap shift
- Auto-cap after . ! ?
- Double-space → period (iOS standard)
- Long-press globe → меню раскладок ES/RU/NUM
- Word frequency learning (DataStore JSON, in-memory cache, top 500 words)
- Suggestions: 3 чипа, user-learned + static dict
- Smart quick chips (Объясни проще / Дай пример / Дай упражнение / etc.)
- Glide-typing MVP (Levenshtein + frequency boost, row 1-2-3)

### Архив + сценарии
- 6 сценариев в ChatScenarios (default + 5 PRO)
- Auto-scroll к активному chip в ScenarioStrip
- PRO-gating: free + PRO chip → paywall

## 78+ unit tests
- KeyboardLogicTest: 60 (insertAt, backspaceChar, moveCursor, shouldAutoCapAfter,
  applyShift, ES/RU/NUM rows, accents, симуляции, double-space)
- GlideMatcherTest: 18 (levenshtein, dedupeConsecutive, matchBestWord,
  topMatches, freq boost, симуляции)

## 🆕 Stats v2 — точная интеграция данных (2026-05-23)

Закрыл 3 из 4 слабостей интеграции Stats screen (одна оказалась
ложной — `LibrosViewModel.markOpened/saveResult` уже пишут локально).

**Что поправлено:**

1. **Слабость 4 — разбивка `isLearned` на градацию** (вместо одной цифры
   «слов выучено» теперь 3 метрики):
   - `wordDao.inProgressCount()` — `repetitions>0 && !isLearned`
   - `wordDao.untouchedCount()` — `total_reviews=0`
   - В Stats: 3-сегментная плашка «Закреплено · В работе · Не тронуто»

2. **Слабость 1 — повторы уроков** (раньше counts только первое прохождение):
   - Новая таблица `lesson_completion_history` (миграция v25→v26)
   - Каждое прохождение урока (включая повторы) пишет строку в history
   - Stats считает breakdown.lessonsCount через эту таблицу, не через
     `lesson_progress` (тот остаётся для ачивок — primary key = lesson_key)

3. **Слабость 2 — реальные минуты per-activity** (самая большая работа):
   - Новая таблица `activity_time_log` (миграция v26→v27)
   - Composable-хук `TrackActivity(type)` в [ActivityTimeTracker.kt](app/src/main/java/com/spanishapp/service/ActivityTimeTracker.kt) —
     `DisposableEffect.onDispose` пишет сессию через Hilt EntryPoint
   - Фильтр: сессии <5 сек игнорируются (шум)
   - Подключено к 11 экранам: LessonSession, Flashcards, AiChat, LibroRead,
     7 игровых экранов (Articles, Speed, Math, Palabra, Sopa, Crossword, Verb)
   - В StatsViewModel breakdown.lessonsMin/flashcardsMin/gamesMin/booksMin/chatMin
     теперь читаются из реальных `activity_time_log`, не из эмпирики
     `lessonsCount*7`. Точность — до секунды.

**Миграции v26 и v27 зарегистрированы в 6 местах:** AppModule, RatingDecayWorker,
ContentSyncWorker, RadioCatalogRefreshWorker, WordOfDayWidget, StreakFlameWidget.

**Слабость 3 — false positive:** аудит ошибочно сказал что `libro_progress`
обновляется только через cloud sync. На самом деле [LibrosViewModel.kt:89-122](app/src/main/java/com/spanishapp/ui/games/LibrosViewModel.kt:89)
пишет `dao.upsert` локально мгновенно в `markOpened` и `saveResult`.

---

## 🆕 Stats / Insights screen (2026-05-23)

Карточка «📊 ЭТА НЕДЕЛЯ» на главной (HomeScreen) и плитка «Эта неделя» в
ProfileScreen теперь **кликабельны** → открывают новый экран
[stats](app/src/main/java/com/spanishapp/ui/stats/StatsScreen.kt).

- Сегментный переключатель: День · Неделя · Месяц · 3М · 6М · Год
- Hero (XP за период + delta vs прошл.), 3 кольца Activity (XP/мин/дни),
  графики (bar для недели, calendar dots для месяца, line+area для 3М/6М/года),
  breakdown «на что ушло время», топ-5 ошибок (game_mistakes) + слабые слова
  (SM-2 weak pool), прогресс/лига, новые ачивки, AI-подсказка.
- Стилистика — строго по [docs/mockups/stats_screen.html](docs/mockups/stats_screen.html)
  (тёмная тема, акценты #FF8A3D / #4EA1FF / #4ADE80 / #A78BFA).
- Период запоминается в DataStore `stats_prefs`.
- 2 новых DAO-запроса: `LessonProgressDao.observeCountSince`,
  `GameLevelProgressDao.observeCountSince`, `ChatMessageDao.observeCountSince`.
- ViewModel: [StatsViewModel.kt](app/src/main/java/com/spanishapp/ui/stats/StatsViewModel.kt) —
  combine из 13 источников, pure `buildUi()` helper покрыт 7 unit-тестами
  ([StatsHelpersTest.kt](app/src/test/java/com/spanishapp/StatsHelpersTest.kt)).
- ⚠ **Локализация заморожена** — все строки экрана сейчас русские в коде
  (по аналогии с radio модулем, ~120 литералов). Перенос в strings.xml ×4
  языка — отдельной задачей фазы 2 (см. § 8 пункт «Локализация контента»).

---

## 📍 1. Текущее состояние

### Identity
- `applicationId = "com.espeak.app"` (изначально был `com.spanishapp`, сменён в v1.0.0 из-за конфликта в Play Store)
- `versionCode = 197`, `versionName = "1.25.95"` (актуально 2026-06-19)
- `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`
  - ⚠ Play Console потребует targetSdk=36 к августу 2026 для обновлений
- Подписан собственным release.keystore (alias **ESPEAK**), V2-signed

### Технический стек
- **Язык:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material3 (composeBom 2024.12.01)
- **Архитектура:** MVVM + Clean (ui / domain / data) — claim; **аудит 2026-06-19** показал
  что 21 ViewModel инжектят 59 DAO напрямую (Repository pattern violation; god VMs
  HomeViewModel 11 DAOs, StatsViewModel 12 DAOs).
- **DI:** Hilt 2.51.1 — 3 dagger.Lazy маскируют Hilt cycles (PlayBillingManager → Verifier,
  XpTracker → LeaderboardRepo, StreakService → AchievementManager).
- **БД:** Room 2.6.1 — **version=32**, **31 миграция**, **27 entities**, **24 DAO**
  (отличается от ниже §4 — та секция stale)
- **Навигация:** Navigation Compose 2.8.4
- **Async:** Coroutines 1.9.0 + Flow
- **Медиа:** Media3 1.4.1 (ExoPlayer + MediaSession + HLS)
- **WorkManager:** **5** worker'ов: DailyReminder, RatingDecay, ContentSync,
  RadioCatalogRefresh, VocabAggregator
- **Виджет:** Glance AppWidget (5 виджетов: WordOfDay, DictionarySearch,
  MissionTracker, Radio, StreakFlame)
- **HTTP:** OkHttp 4.12.0
- **Storage:** DataStore (8+ preferences-файлов)
- **ИИ:** Gemini Flash API через Cloudflare Worker proxy
- **Firebase:** Auth (Anonymous + Google), Firestore, Storage, Analytics, Crashlytics
- **Billing:** Google Play Billing Library 7 — ✅ реализован (v1.25.4+)

### Кодовая база (grep-факты, обновлено 2026-06-19)
- **~58** Composable Screen-файлов
- **~53** @HiltViewModel-аннотированных классов
- **27** Room entities (CleanVocab + WeeklyLeagueStateEntity + RecentSearchEntity +
  GameMistakeEntity + LessonCompletionEventEntity + ActivityTimeLogEntity +
  UserVocabStateEntity + WeakVerbEntity + остальные)
- **24** Room DAOs
- **31** Room миграции (v1→v32)
- **5** WorkManager worker'ов
- **~315** @Test (24 файла) — НО **0 Room migration tests** (главный риск!), **0 billing
  tests**, **0 ViewModel tests**, **0 DAO tests**. testImplementation объявлены mockk +
  turbine + coroutines-test но не используются.

### Локализация UI
- `values/`: 900 строк (ru — основной)
- `values-en/`: 896 строк
- `values-uk/`: 900 строк
- `values-es/`: 900 строк
- ⚠ Контент уроков **только на русском** — UI переводится, контент нет

---

## 📦 2. Контент-инвентарь (verified by grep)

### Уроки
- **254** уникальных lesson ID всего:
  - **V1** (`LessonContentData.kt`): 240 IDs
  - **V2** (`LessonContentDataV2.kt`): **253 IDs** ← ВЕСЬ КУРС переписан по xlsx за 2026-05-15
- V2 использует **два синтаксиса**:
  - `"id" to LessonContent(...)` — детальный (А1 блок, ~63 урока)
  - `"id" to lc(...)` — компактный helper (A2/B1/B2, ~190 уроков)
- Распределение V2 по unit'ам: u1=15, u2=15, u3=17, u4=16, u5=16, u6=16, u7=16,
  u8=15, u9=16, u10=15, u11=16, u12=16, u13=16, u14=16, u15=16, u16=16
- Merge: `V1 + V2` (Map.plus — V2 перетирает V1). Эффект: **юзер видит V2 контент** для всех уроков
- **260** RoadmapLesson в `RoadmapData.kt` — включает 19 `_5` уроков-вставок
  через параметр `id = "uX_lY_5"`. Контент и навигация синхронизированы
  (verified diff 2026-05-29). ✅

### Libros (книги/рассказы)
- **100/100** рассказов в `LibrosData.kt`:
  - A1: 25 шт
  - A2: 25 шт
  - B1: 15 шт
  - B2: 35 шт
- Каждый рассказ + 4 quiz-вопроса, перевод по long-press, sm2-стиль

### Словарь
- **12616 записей всего** (до dedup):
  - `CleanVocab.kt`: **4712** слов
  - `BasicsVocab.kt`: **1189** слов
  - `VocabExtra1-12.kt`: суммарно **5300** слов (диапазон 294-557 на файл)
  - `assets/spanish_vocab.json`: **1415** слов (подключены через `loadJsonVocab()` в DatabaseSeeder)
- **~10086 уникальных после `distinctBy { spanish.trim().lowercase() }`** в `DatabaseSeeder.seedWords()`
- Старое утверждение «1300 пересечений после dedup» — это **разница 12616 - 10086 = 2530 дубликатов** (правильное число)

### Спряжение глаголов
- **159 глаголов** с **полными авторскими таблицами 6 времён** (ConjugationData.kt + ConjugationData2.kt + ConjugationData3.kt — все 6 времён × 159 = 954 ConjugationEntity записей)
- **1327 глаголов** в `SpanishVerbBank` (5 тиров, для тренажёра):
  - Tier 1 (топ-50): 50
  - Tier 2 (51-100): 50
  - Tier 3 (101-200): 102
  - Tier 4 (201-350): 149
  - Tier 5 (351-850 полный список): 976
- 159 с таблицами помечены как `AUTHORED`, остальные генерируются `SpanishConjugator` по правилам (REGULAR_AR/ER/IR, STEM_E_IE, etc.)

### Грамматика
- **75 уроков**: A1×15, A2×20, B1×20, B2×20
- Грамматический трекинг (GrammarScreen)

### Игры
- **6 мини-игр** в `ui/games/` (хаб: GamesScreen):
  1. ArticlesGameScreen — артикли el/la/un/una (100 уровней через `GameLevelManager`)
  2. SpeedGameScreen — на скорость
  3. SopaGameScreen — поиск слов
  4. PalabraMaestraScreen — палач
  5. MathGameScreen — испанские числа
  6. CrosswordGameScreen — кроссворд (100 уровней + zoom/pan)
- Отдельные модули (не «игры» по UX, хотя живут в `ui/games/`):
  - VerbTrainingScreen — тренажёр спряжения (1327 глаголов)
  - LibrosScreen / LibroReadScreen — 100 рассказов (книги/чтение)

### Theory cards
- **16** теория-карточек в `TheoryContentData.kt` (не 10 как раньше писали)
- Расширились дальше блока A1.1
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
- Android Auto support (попытка v1.25.6 откачана в v1.25.89 — нужен полный
  MediaLibraryService с browse tree, не просто intent-filter)
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

- **Worker URL:** `espeak-gemini-proxy.es-espeak13.workers.dev`
- **Модель:** `gemini-flash-latest` (auto-alias на актуальную бесплатную)
- **Системный промпт:** дружелюбный репетитор для русскоязычных A1/A2, короткие ответы (4-5 строк), корректировки в формате `CORRECTIONS_JSON:[...]`
- **История:** 20 последних сообщений из Room (`chat_messages`)
- **Доступ:** AI-чат **PRO-only** (с v1.25.73). Free-юзеры видят paywall на входе в
  `AiChatScreen`, воркер отдаёт 403 «Chat is PRO-only» для не-PRO uid. Клиентский
  лимитер 50/день (`AiChatLimiter`) удалён в v1.25.97 как мёртвый код. Для PRO — безлимит.
- **Безопасность v1.11.7:**
  - В release `BuildConfig.GEMINI_API_KEY` пустой (не запекается)
  - `AiChatRepository.apiUrl()` и `GeminiTranslator.apiUrl()` крашат с `IllegalArgumentException` если в release нет proxy
  - Worker валидирует `X-App-Secret` (ENV `APP_SECRET`) — random callers получают 403
  - `humanizeError()` — понятные сообщения вместо raw JSON

---

## 🚀 8. Roadmap (что РЕАЛЬНО не сделано)

### Высокий приоритет
1. ~~**Локализация контента уроков** на en/uk/es~~ — **🧊 ЗАМОРОЖЕНО** (2026-05-17 решение владельца).
   Приложение остаётся русскоязычным по контенту. UI на 4 языках работает,
   но контент только русский. Не возвращаемся к задаче до явной отмены
   заморозки.
2. ~~**V2 курса (xlsx) рефакторинг**~~ — ✅ **ЗАКРЫТО** (2026-05-15).
   Весь курс переписан по xlsx за 1 день: 253 урока из 254 (только 1 урок
   только в V1). Все блоки A1.1 → B2.4 покрыты. Я ошибочно считал 49/240
   из-за неправильного grep — V2 использует два синтаксиса (LessonContent
   + lc-helper), искал только первый. Извинения.
3. ~~**14 V2-only уроков (`_5` суффикс) НЕ в roadmap**~~ — ✅ ЗАКРЫТО (verified
   2026-05-29). По факту 19 уроков-вставок, все привязаны к RoadmapData через
   `id = "uX_lY_5"` параметр. LessonIntroViewModel использует этот id для
   контент-lookup. Изначальный отчёт CLAUDE.md был устаревшим.
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
10. **Android Auto** через `MediaLibraryService` + browse tree (Stations /
    Favorites / Genres). НЕ просто intent-filter — нужен реальный
    `onGetLibraryRoot` + `onGetChildren` с browseable нодами. Прежняя
    попытка v1.25.6 откачана в v1.25.89 (см. §6). Эффорт ~4-6ч + тестирование
    в Android Auto Desktop Head Unit.
11. ~~**Локализация radio модуля**~~ — 🧊 заморожено вместе с локализацией контента (см. #1)
12. **CLAUDE.md периодический sync с кодом**

### Низкий / отложено
13. Whisper транскрипция (holdback — $1000/мес)
14. Share station deep link
15. «T» button флип карточки → перевод (обсуждали, отменили)
16. **Google Play Billing для PRO** — план есть, не реализовано
17. Озвучка Libros актёрами
18. **«Мой словарный запас»** — агрегация всех сигналов обучения в единое
    представление (X слов знаешь, breakdown по CEFR, mastered/learning/weak).
    Дизайн-док готов: [docs/VOCAB_TRACKING_PLAN.md](docs/VOCAB_TRACKING_PLAN.md).
    Юзер одобрил концепт 2026-05-29, реализацию отложил. Эффорт ~1.5 дня.
    Триггер для начала: явное "делай словарь" от юзера.

---

## 💎 9. Монетизация (план v2.0, утверждён 2026-05-14)

### Бесплатно
- Уроки A1 (60 шт), грамматика A1, диалоги A1, книги A1
- Спряжение A1, игры первые 10 уровней каждой
- Карточки SM-2 для слов A1
- **Словарь полностью** (10086 слов — handbook-функция)
- **Pronunciation полностью** (motor skill, не зависит от уровня)
- ~~AI Chat — 50 запросов/день~~ → AI-чат теперь **PRO-only** (с v1.25.73), в free нет
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
- AI Chat — **эксклюзивно PRO, безлимит** (free-юзерам чат недоступен)

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
- `LibroTextHelpersTest` (20)
- `ExerciseGeneratorTest`, `CrosswordTest`, `RatingSystemTest`, ...
- `LocalizationIntegrityTest` ✅ (раньше падал — починен в v1.11.7 добавлением `nav_radio` + `bento_goal_radio` в en/uk/es)
- **Всего 236/236 зелёные** (verified `./gradlew testDebugUnitTest --rerun-tasks`)

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
| 1.11.7 | 65 | 10 critical из audit + AAB подан в Play |
| **v1.12 – v1.25.6** | **~66-181** | Tablet adaptive, V2 курс, Stats v2, Theory расширение, AI chat rewrite, Voice messages (потом reverted в v1.25.7), Glide-typing (удалён в v1.25.44), Android Auto (удалён в v1.25.89), Billing v1.25.4, ... (детали выпали из этой таблицы — см. git log) |
| 1.25.84 | 186 | bump после v1.25.83 |
| 1.25.86 | 188 | Локальный fix билда (не зарелизен) |
| 1.25.87 | 189 | Auto-enroll в leaderboard + фикс кривой рамки |
| 1.25.88 | 190 | Leaderboard nicknames — sync displayName из Onboarding |
| 1.25.89 | 191 | Убран Android Auto — фикс Play Console reject |
| 1.25.90 | 192 | 6 групп блокеров (auth, billing, B2 content, PRO, Dialogues XP) |
| 1.25.91 | 193 | 15 Tier-1 educational (calques, broken exercises, cyrillic) |
| 1.25.92 | 194 | 11 Tier-2 educational (B1/B2 + Grammar) |
| 1.25.93 | 195 | Theory dialect Spain + A2 Indef/Imperf big + B2 estilo indirecto + Grammar id=17→B1 |
| 1.25.94 | 196 | Phase B audit: verbs typos, vocab gender/spelling, libros AI artifact |
| **1.25.95** | **197** | **Текущая** — Tier-1 technical audit (date locale, fake test asserts, broken androidTest) |

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
- **AI proxy:** https://espeak-gemini-proxy.es-espeak13.workers.dev/

---

## 🗂 16. Структура документации

После аудита 2026-05-17 актуальная картина:

### Активные .md (5, в git)
- **`CLAUDE.md`** (этот файл) — единый источник правды
- `PRIVACY_POLICY.md` — публичная политика для Play Console (legal)
- `LICENSES.md` — open-source licenses
- `index.md` — GitHub Pages landing
- `backend/cloudflare-worker/README.md` — документация AI proxy

### Архив `docs/archive/` (6, в git)
- `docs/archive/design_system/*` — design system от 2026-05-08 (устаревший
  стек, упоминает Claude/EASPEAK вместо Gemini/ESPEAK). Не используется в
  коде, но сохранён как референс. См. `docs/archive/README.md`.

### Untracked локально (11, НЕ в git — фактически на GitHub их нет)
- `docs/qa/*` (8) — формальная QA-инфраструктура (test cases, smoke, release
  checklist, beta tester guide и др.). Существует только локально.
- `.github/ISSUE_TEMPLATE/*` (3) — templates для GitHub Issues. **GitHub
  Issues их не использует** т.к. не закоммичены.
- **TODO:** решить — коммитить или удалять.

### Архив worktree (28, в `.claude/worktrees/stoic-cohen-8019d1/`)
Snapshot старой версии проекта от прошлых сессий Claude Code. Содержит
дубликаты + 18 файлов которые удалены в консолидации (PLAN, CHECKLIST,
README_NEW, LEARNING_GUIDE, docs/ADS_PLAN, и др.). Не отображается в
обычном git, не мешает workspace. Можно удалить через
`git worktree remove .claude/worktrees/stoic-cohen-8019d1`.

### История консолидации (2026-05-17)
Удалено 18 .md (содержимое поглощено CLAUDE.md):
PLAN.md, CHECKLIST.md, README_NEW.md, LEARNING_GUIDE.md, radio.md, docs/ADS_PLAN.md, docs/articles_game_design.md, docs/AUDIT_REPORT.md, docs/DONATIONS_PLAN.md, docs/LESSON_EXERCISES_PLAN.md, docs/MINDMAP.md, docs/MONETIZATION_PLAN.md, docs/PLAY_CONSOLE_CHEATSHEET.md, docs/PLAY_STORE_LISTING.md, docs/PUBLISH_PRIVACY_POLICY.md, docs/RELEASE_CHECKLIST.md, docs/SCREENS.md, docs/play_assets/SCREENSHOTS_GUIDE.md.
