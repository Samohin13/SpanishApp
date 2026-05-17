# SpanishApp / ESPEAK — Android приложение для изучения испанского языка

> Этот файл — **живая память проекта**. Обновляется каждые 30–60 минут работы.
> Не перезаписывать целиком, а структурированно дополнять.
> Последнее обновление: **2026-05-17, сессия 20 (v1.11.7 — security audit + AAB build)**

---

# 📍 CURRENT STATE v1.11.7 (2026-05-17)

**Версия:** v1.11.7, versionCode 65. AAB собран (38 МБ), подан на закрытое тестирование Play.

**Содержит:** 5 critical security fixes + 2 critical DB fixes + 2 critical concurrency fixes + radio production polish (v1.6.0→1.11.7).

## ✅ Что работает

**Контент:**
- 10086 уникальных слов (CleanVocab + 12 ext + JSON-asset + BasicsVocab — все подключены ✓)
- 240 уроков (16 unit × 15) с auto-generated упражнениями (Phase 1-3 движка)
- 1300+ глаголов спряжения, 159 с полными таблицами
- 100 рассказов Libros (A1 готовы, A2-B2 в развитии)
- 10 теория-карточек блока 1.1 + библиотека
- 75 уроков грамматики (A1×15, A2×20, B1×20, B2×20)
- 8 мини-игр (6 в GamesScreen + Verbos + Libros)
- 23 достижения (bronze/silver/gold через xpReward)

**Системы:**
- SM-2 интервальное повторение
- SkillRating v2 (старт 0, decay progressive, peakRating сохраняется)
- 8 лиг «Путь до Мадрида» с реактивным UI
- Weekly Leagues (Firestore cohorts, опт-ин)
- Лидерборд (страна + Мир, auto-fallback)
- Daily Mission (4 цели + 5-я «5 мин радио»)
- Streak с freezes, видимы на главной
- AI Chat (Gemini Flash через Cloudflare Worker, 50/день, humanized errors)
- Биометрия для входа в PRO зоны
- Темы (Light/Dark/Auto), 4 локали UI (ru/en/uk/es)
- Виджет «Слово дня»
- 📻 **Радио v1.11.5** — production-ready (см. radio.md)

**Инфраструктура:**
- Room v24 (24 миграции, все прописаны в 5 местах: AppModule + 3 Worker + Widget)
- DataStore: voice_prefs, app_lock_prefs, content_versions, radio_blocklist (TTL 48ч), и др.
- WorkManager: RatingDecay (daily), RadioCatalogRefresh (weekly, UNMETERED+batteryNotLow), ContentSync, DailyReminder
- Firebase Auth (Anonymous + Google), Firestore, Storage (5MB+image MIME guards), Crashlytics, Analytics
- Cloudflare Worker proxy для AI (`espeak-gemini-proxy.bravochief21.workers.dev`)
- 65 unit-тестов (включая 40 для радио)
- Coverage через Kover, preRelease task для CI-like локальной проверки

## ❌ Что РЕАЛЬНО не сделано (актуальный backlog)

**Высокий приоритет:**
1. **Локализация контента уроков** — UI переведён на en/uk/es, но контент только русский → бардак при смене языка
2. **Контент A2-B2 расширение** — Libros 25/100, многие уроки заглушки
3. **22 IMPORTANT из аудита 2026-05-17** — best practices, не critical
   (rememberSaveable формы, popUpTo("home") fix, MainActivity runBlocking, и др.)

**Средний приоритет:**
4. **Sleep timer для радио** (~1.5ч работы)
5. **Lockscreen artwork radio** (gradient bitmap, ~1.5ч)
6. **Recently played карусель радио** (~1.5ч)
7. **Listening streak** (отдельный от learning streak)
8. **Achievements за радио** («1 час», «10 часов», «100 слов»)
9. **Android Auto support** через MediaBrowserService (~2ч)
10. **Локализация radio модуля** (~478 русских литералов × 3 локали)
11. **CLAUDE.md sync с production** — этот файл, периодически)

**Низкий / отложено:**
12. **Whisper транскрипция** — Holdback (платно, $1000/мес)
13. **Share station deep link**
14. **«T» button флип карточки** → перевод (обсуждали — отменили)
15. **Google Play Billing для PRO subscription** — план есть, не реализовано
16. **Озвучка Libros** профессиональными актёрами

## 🔒 Security state (после audit 2026-05-17)

**Критичные — все закрыто в v1.11.7:**
- ✓ GEMINI_API_KEY больше не в release APK (только debug BuildConfig)
- ✓ Cloudflare Worker secret enforced (X-App-Secret header)
- ✓ Firebase Storage rules: size 5MB + image/* MIME (apply'нуто в Console)
- ✓ radioWordCatchDao() bomb обезврежен
- ✓ fallbackToDestructiveMigration только в debug (5 мест)
- ✓ RadioViewModel callbacks leak fixed
- ✓ SpeechRecognizer cancellation race fixed

**Что осталось из audit (IMPORTANT):**
- ANTHROPIC_API_KEY поле было dead code — удалено в v1.11.7
- ProfileScreen Log.d с downloadUrl — gated в v1.11.7
- network_security_config — не имеет HTTPS-only allowlist для samohin13.github.io

---

## 📻 Radio v1.10.0 — production polish (сессия 20, после фикса v1.9.1)

Пакет «обязательное + рекомендуемое для media-app». Закрывает Google Play
требования к media-приложениям + улучшает UX до уровня Spotify/Apple Music.

### Что добавлено (5 пунктов + security)

**1. Audio focus handling (KZ KZ требование Google Play)**
- `AudioManager.requestAudioFocus()` при `play()` с USAGE_MEDIA
- Обработка `AUDIOFOCUS_LOSS` (постоянная) → пауза
- Обработка `AUDIOFOCUS_LOSS_TRANSIENT` (звонок) → пауза + auto-resume при `GAIN`
- Обработка `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK` → ducking (volume 0.25)
- API 26+ через `AudioFocusRequest`, ниже — legacy API с deprecation suppress

**2. Headphone unplug → auto-pause**
- `ExoPlayer.Builder().setHandleAudioBecomingNoisy(true)` — одна строка, Media3 сам слушает `ACTION_AUDIO_BECOMING_NOISY`
- Выдернули наушники → не орёт через динамик в людном месте

**3. Buffering indicator (детальный playback state)**
- Новый enum `RadioPlaybackState`: IDLE / BUFFERING / PLAYING / PAUSED / ENDED / ERROR
- StateFlow в контроллере, обновляется в `onPlaybackStateChanged` + `onIsPlayingChanged`
- UI новый `StatePill` диспетчер вместо одного `LivePill`:
  - PLAYING → LIVE (зелёный пульс)
  - BUFFERING → LOADING (жёлтая бегущая точка)
  - PAUSED / IDLE → static label
  - ERROR → красный pill

**4. ICY metadata «Сейчас играет [track]»**
- `Player.Listener.onMediaMetadataChanged` ловит StreamTitle из Icecast/Shoutcast потоков
- Media3 парсит ICY headers автоматом — поле `MediaMetadata.title`
- StateFlow `nowPlaying` в контроллере + ViewModel
- UI: под названием станции «🎵 Enrique Iglesias - Bailamos» (приоритетнее programме)
- Sanitizer: убирает control chars / RTL spoofing / zero-width / типичный noise (unknown, no title, "-")
- Лимит 120 символов (защита от OOM на вредном потоке)

**5. WorkManager weekly refresh**
- `RadioCatalogRefreshWorker` — раз в 7 дней (initial delay 24h)
- Constraints: UNMETERED (Wi-Fi only) + battery not low
- Без участия юзера каталог пересобирается → открываешь радио = свежее
- Schedule в `SpanishApp.onCreate()` через `enqueueUniquePeriodicWork(KEEP)`

**6. Security: URL whitelist + Network Security Config**
- `isSafeStreamUrl()` фильтрует pool в `fetchPool` — только http/https/rtsp
- Блокирует: javascript:, file://, content://, data:, ftp:, malformed
- `network_security_config.xml`: cleartext **разрешён по дефолту** (нужен для half of radio streams без HTTPS), но **запрещён** для критичных API:
  - radio-browser.info mirrors
  - api.country.is
  - workers.dev (наш AI proxy)
  - Firebase (firestore/storage/auth/crashlytics)
  - Anthropic / Google AI fallback
- Подключён в манифесте `android:networkSecurityConfig`
- При MITM на публичном Wi-Fi: атакующий не сможет подменить ответ Firebase или naszego API

### Unit tests (22 новых)
- `RadioSanitizeTest` — 11 тестов: null/blank, длина, control chars, RTL override, zero-width, unknown noise, испанские символы, эмодзи
- `RadioUrlSafetyTest` — 11 тестов: http/https/rtsp allow, case-insensitive, javascript/file/content/data/ftp deny, malformed, реальные URL из каталога

### Что НЕ сделано (отложено на v1.11.0)
- Sleep timer (заснуть под радио)
- Lockscreen artwork (gradient bitmap вместо просто текста)
- Voice EQ preset для TALK станций
- Share station deep link
- Recently played карусель
- Android Auto support через MediaBrowserService
- Custom URL input (для энтузиастов)

### Версия
- versionCode 47 → **48**, versionName 1.9.1 → **1.10.0**

---

## 📻 Radio v1.9.0 → 1.9.1 — фоновое воспроизведение + редизайн (сессия 20)

## 📻 Radio v1.9.0 — фоновое воспроизведение + чистый редизайн (сессия 20)

Критичный фикс + большой UX-апгрейд по итогам тестирования v1.8.x.

### Что починили
**Lock screen / фоновое воспроизведение** — была архитектурная ошибка:
- Было: 2 разных ExoPlayer (один в `RadioPlayerController`, второй пустой в `RadioPlayerService`)
- MediaSession была привязана к пустому player'у → система не видела что играет
- Не запускался foreground service → Android убивал процесс при блокировке
- Lock screen / шторка не показывали media controls

Стало:
- ОДИН `ExoPlayer` в контроллере, к нему привязан `MediaSession` через Hilt EntryPoint
- `RadioPlayerController.play()` стартует `ContextCompat.startForegroundService(RadioPlayerService)`
- В `MediaItem.MediaMetadata` передаём `title=station.name`, `artist=country+genre`
- `onTaskRemoved` НЕ убивает сервис пока играет (поведение как у Spotify)
- Permissions уже были: `FOREGROUND_SERVICE_MEDIA_PLAYBACK` + `WAKE_LOCK`

### Что убрали по запросу владельца
- WordCatchCard (кнопка «Поймал слово!») — слишком игровой элемент, не вписывается в концепцию пассивного слушания
- Из ProfileScreen → тайл «СЛОВ ПОЙМАЛ» (остались «прослушано» + «открыть»)
- Из RadioViewModel: `catchWord()`, `totalCaughtWords`
- DI: `provideRadioWordCatchDao` (DAO и таблица в БД остались — без миграции вниз)

### Что добавили: новый layout RadioScreen
- **Без vertical scroll** — всё помещается на одном экране как Spotify Now Playing
- TopBar (Material иконки): back ← + title + refresh ↻
- Country chips (3 шт)
- **Filter chips row** (6 шт, multi-select): 🎵 Музыка · 🎙 Разговор · 📰 Новости · ⚽ Спорт · 🎭 Культура · ⭐ Избранное
- Hero (компактный 70% ширины, aspect 1:1) с LIVE/PAUSED/ERROR pill
- Station info: name + program + 3 tags (страна/уровень/жанр)
- Controls: ♥ ⏮ ▶ ⏭ (все Material icons, не эмодзи)
- Bottom carousel: станции + последний тайл «+ Найти ещё» с pulse-анимацией при загрузке

### Что добавили: discoverMore() в RadioCatalogRepository
- Тап на «+ Найти ещё» → `discoverMore(20)` — дозапросить станций без очистки кэша
- ID станций = stable `hashCode(url)` → `INSERT OR REPLACE` дедуплицирует
- Старая балансировка 24+8+8 осталась только для первичного `discoverAndCache()`

### Версия
- versionCode 45 → **46**, versionName 1.8.4 → **1.9.0**

### Что НЕ сделано (на потом)
- WorkManager weekly catalog refresh (сейчас при входе если кэш > 7 дн)
- Auto-discovery silent failures (юзер сообщал «ничего не подбирает» — нужно диагностировать)
- Listening streak (отдельный от learning streak)
- Achievements за радио («1 час», «10 часов»)
- Lockscreen artwork (сейчас только текст + accent цвет — добавить gradient bitmap)
- Transcript / shownotes — для обучения нужно показать что играет (название трека)

---

## 📻 Radio epic — v1.6.0 → 1.8.4 (сессия 19, 2026-05-16…17)

Большая фича — испаноязычное радио прямо в приложении. Интегрирован
сторонний проект RadioTuner (com.example.radiotuner) → перенесён под
`com.spanishapp.radio.*` и серьёзно переработан под нашу обучающую
аудиторию + стилистику.

### Что появилось
| Версия | Что |
|---|---|
| 1.6.0 | База: 40 испаноязычных станций (24 ES + 8 MX + 8 AR), ExoPlayer Media3, MediaSessionService, HapticManager (TICK/CLICK/HEAVY_CLICK), 5-я нав-кнопка 📻, Tuner wheel + Frequency dial |
| 1.6.1 | P0 фиксы: хаотичные скачки станций, колесо визуально крутится, Singleton player (выживает между экранами), Mini-player над BottomBar |
| 1.6.2 | URL verification через radio-browser.info (31/40 verified), Favorites Room таблица + ⭐ кнопка, StationInfoCard под controls |
| 1.6.3 | Замена 9 мёртвых станций живыми из API (Flamenco Radio 320 kbps добавлен!) |
| 1.6.4 | Spotify/Apple Music визуал: hero artwork с country-gradient, большая 72dp play-кнопка с linearGradient, station tags, mini-player без border |
| 1.6.5 | 8 broken URL заменены живыми (полный probe через HEAD), status/nav bar insets фикс |
| 1.6.6 | **Убрал колесо tuner** (по запросу владельца) → горизонтальная карусель станций + авто-skip при ошибке потока |
| 1.6.7 | 13 geo-blocked заменены на global-CDN потоки (streamtheworld, zeno.fm — работают из не-EU стран) |
| **1.7.0** | **Auto-discovery**: при первом заходе на радио → ip-API определяет страну → radio-browser API ищет испаноязычные станции → PROBE каждого URL → балансирует 24+8+8 → кэширует в Room (24h). UI: «🔍 Подбираем станции… 47%» с progress |
| 1.8.0 | Обучающие фичи: listening time tracker (radio_listening_session), «💬 Поймал слово!» кнопка с +5 XP бейджем, debounce 1 сек, scale-pulse анимация |
| 1.8.1 | Stats секция в ProfileScreen «📻 МОЁ РАДИО» (минуты прослушано / слов поймал / → открыть) |
| 1.8.2 | Featured-карточка на HomeScreen — реактивно показывает LIVE-станцию или приглашение «Слушай живой эфир» |
| 1.8.3 | 5-я Daily mission «5 мин радио» (300+ секунд за день) с smart-routing в /radio |
| 1.8.4 | TTS↔Radio coordination — радио на паузу когда играет TTS в уроке (RadioCoordinator object + hook в speakSpanish()) |

### Структура модуля
```
com.spanishapp.radio/
├── data/
│   ├── Station.kt (Country / Genre / CefrLevel enums + data class)
│   ├── StationRepository.kt (40 hardcoded fallback)
│   ├── RadioFavoriteEntity + DAO (Room таблица)
│   ├── RadioCatalogEntity + DAO (dynamic catalog cache)
│   ├── RadioCatalogRepository (API + probe + cache)
│   └── RadioStatsEntity (listening_session + word_catch)
├── player/
│   ├── RadioPlayerController.kt (ExoPlayer wrapper, Singleton)
│   ├── RadioPlayerService.kt (MediaSessionService для фона)
│   ├── HapticManager.kt (detent haptics — TICK/CLICK/HEAVY_CLICK)
│   └── RadioCoordinator.kt (TTS↔Radio mutex)
└── ui/
    ├── RadioScreen.kt (главный экран: hero + controls + carousel)
    ├── RadioViewModel.kt @HiltViewModel
    ├── RadioMiniPlayer.kt (overlay над BottomBar)
    ├── HomeRadioCard.kt (featured-карточка на главной)
    └── (всё в стиле ESPEAK: orange Accent + dark surfaceVariant)
```

### Версии БД
v22: radio_favorites
v23: radio_catalog (dynamic stations from API)
v24: radio_listening_session + radio_word_catch (stats)

### Что НЕ сделано (актуально на v1.11.7)
- ✅ ~~WorkManager weekly catalog refresh~~ → сделано в v1.10.0
- ✅ ~~HomeRadioCard~~ → удалена в v1.11.0 (juzер запросил)
- ⚠ Listening streak (отдельный от learning streak) — не сделано
- ⚠ Achievements за радио («1 час», «10 часов», «100 слов») — не сделано
- ⚠ Sleep timer + Lockscreen artwork + Recently played + Share — отложенный nice-to-have
- ⚠ Android Auto support через MediaBrowserService — отложено
- ⚠ Whisper транскрипция — на holdback ($1000/мес ongoing cost)
- ⚠ Локализация ~478 русских литералов в radio/* — большой scope

---

## 📖 Theory cards Phase 1 (v1.2.0, сессия 18, 2026-05-15)

Запущена новая система **теория-карточки 1-к-1 с практическими уроками**.
Под каждым уроком — отдельный справочник: правила, таблицы, примеры с TTS,
мнемоники, сравнения. Юзер сам решает: читать перед практикой или прыгать сразу
в упражнения.

### Что добавлено
| Файл | Назначение |
|---|---|
| `data/theory/TheoryContent.kt` | data classes: TheoryContent, TheorySection (8 типов), TheoryTable, TheoryExample, TheoryComparison |
| `data/theory/TheoryContentData.kt` | Singleton-реестр + 10 теорий блока 1.1 (u1_l0..u1_l9) |
| `data/db/entity/TheoryProgressEntity` | Room-таблица `theory_progress` (lessonId PK, first/lastReadAt, readCount) |
| `data/db/dao/TheoryProgressDao.kt` | observeAll/getOne/observeReadCount/markRead (idempotent INSERT OR REPLACE) |
| `data/db/AppDatabase.kt` | version=21, MIGRATION_20_21 (создаёт theory_progress + index) |
| `ui/theory/TheoryReaderViewModel.kt` | загрузка контента + markRead |
| `ui/theory/TheoryReaderScreen.kt` | 8 рендереров секций + TTS на примерах + кнопка «Прочитал» |
| `ui/theory/TheoryLibraryScreen.kt` + VM | библиотека всех теорий, группировка по CEFR |

### Интеграция
- **LessonIntroScreen**: над rewards-блоком появляется карточка «📖 Теория к уроку»
  с эмодзи + заголовок + «⏱ N мин чтения», тап → `theory/{lessonId}`. Если
  теории для этого урока ещё не написано — карточка просто не рисуется.
- **ProfileScreen**: новая секция «Справочник» → `theory_library` (библиотека
  всех 10+ карточек, сгруппированы по CEFR, с пометкой ✅ для прочитанных).
- **Navigation.kt**: 2 новых маршрута — `theory/{lessonId}` и `theory_library`.
- **MIGRATION_20_21** прописана везде где напрямую открывается БД:
  AppModule, RatingDecayWorker, ContentSyncWorker, WordOfDayWidget.

### Контент 10 теорий (блок 1.1)
u1_l0 «5 гласных» · u1_l1 «B/V/D/G — три коварных согласных» ·
u1_l2 «H/J/Ñ/RR» · u1_l3 «Ударение и тильда» · u1_l4 «Приветствия» ·
u1_l5 «Прощания» · u1_l6 «Por favor / gracias / perdón» ·
u1_l7 «SER — soy/eres/es» · u1_l8 «SER — somos/sois/son» ·
u1_l9 «Личные местоимения».

Каждая карточка: 3-7 секций (RULE/TABLE/EXAMPLES/MNEMONIC/TIP/WARNING/COMPARISON)
+ 4-5 keyTakeaways в финале. Среднее время чтения 3-5 мин.

### Версия: 1.1.1 → **1.2.0** (versionCode 18→19)
Сборка: `./gradlew :app:compileDebugKotlin` BUILD SUCCESSFUL.
Дальше — Phase 2: 60 теорий для всего A1 (u2..u4 блоки) + интеграция
в LessonSession (показ «уже прочитано / освежить» рядом с упражнениями).

## 🧪 QA Infrastructure (сессия 17, 2026-05-15)

Полная QA-инфраструктура установлена:

**GitHub:**
- 10 labels (P0/P1/P2/P3 + bug/enhancement/tech-debt/qa + needs-repro/blocked)
- 3 milestones (v1.1.0, v1.1.1, v2.0.0)
- 3 issue templates (.github/ISSUE_TEMPLATE/)
- 16 known bugs импортированы как Issues #4-#19, все закрыты

**Документация в `docs/qa/`:**
- `TEST_STRATEGY.md` — общая стратегия (test pyramid, coverage goals, severity)
- `TEST_CASES.md` — 110 формальных тест-кейсов по 13 категориям
- `MANUAL_QA_GUIDE.md` — алгоритм по ISTQB (для тебя как QA)
- `SMOKE_TEST.md` — 15 P0-кейсов на 10 минут
- `RELEASE_CHECKLIST.md` — что проверить перед каждым AAB
- `BUG_REPORT_TEMPLATE.md` — формат записи багов (formal + Telegram)
- `BETA_TESTER_GUIDE.md` — для новых тестеров с Boosty
- `GITHUB_WORKFLOW.md` — как пользоваться Issues

**Авто-тесты:** 65 unit-тестов в 8 файлах (все проходят):
- `SkillRatingSystemV2Test` (16) — новые формулы 1.1.0
- `LeagueResolverTest` (14) — лиги с 0
- `Migration18To19Test` (5) — миграции v1→v20
- `AchievementCatalogTest` (8) — tier-mapping
- `StreakManagerTest` (8) — daily streak логика
- `WordOfDayStreakLogicTest` (5) — WoD streak
- `XpSystemTest` (5)
- `AiChatLimiterTest` (4)

**Gradle инфраструктура:**
- Kover plugin → `./gradlew :app:koverHtmlReportDebug` → coverage HTML
- `./gradlew preRelease` — lint + test + bundleRelease + coverage в одной команде

## 📊 Контент-инвентарь (актуально на 2026-05-14)

**Словарь:**
- `BasicsVocab.kt` — 1188 слов (A1-фундамент)
- `CleanVocab.kt` — 4712 слов (основной дедуплицированный)
- `VocabExtra1-12.kt` — 5288 слов (12 файлов расширений)
- **Всего записей: 11 188** → после dedup: **10 086 уникальных слов** ✅
- ⚠ `assets/spanish_vocab.json` (1415 слов) **НЕ подключён в DatabaseSeeder.seedWords()** — мёртвый ассет 1.4 МБ. TODO: подключить.

**Глаголы:** 1300+ в верб-тренажёре (`ConjugationData.kt + 2 + 3`), 159 с полными таблицами спряжения 6 времён.

**Уроки:** 240 (16 unit'ов × 15 уроков), A1-B2 покрытие.

**Игры:** 6 мини-игр (Articulos, Speed, Sopa, Palabra, Math, Crucigrama) + Verbos (тренажёр) + Libros (библиотека на 100 рассказов).

**Достижения:** 23 шт (бронза/серебро/золото — НО семантика медалей-vs-кубков сейчас сломана, см. backlog).

**База данных:** version=19, миграции 1→19 все прописаны в AppModule + Worker'ах + Widget'е.

## 🐛 BACKLOG тестера 2026-05-14 (АРХИВ — 15/16 закрыто)

> Этот список из ранней сессии тестирования. Сохранён как history record.
> Актуальный backlog — в секции «CURRENT STATE» наверху файла.

### Критичные баги
1. ✅ **Lesson count 1→3** — fixed в v1.0.10
2. ✅ **Photo upload в Firebase Storage** — fixed в v1.0.10
3. ✅ **Libros в Daily Mission** — fixed в v1.0.10
4. ✅ **POST_NOTIFICATIONS Android 13+** — fixed в v1.0.10 (`MainActivity.kt:82-95`)
5. ✅ **`spanish_vocab.json` не подключён** — fixed в v1.0.10 (`DatabaseSeeder.kt:149` `loadJsonVocab()`)

### UX/логика
6. ✅ **Уровень в Settings → read-only** — fixed в v1.1.0
7. ✅ **Дневная цель в onboarding** — fixed в v1.1.0 (отдельный экран)
8. ✅ **«Путь до Мадрида» прозрачнее** — fixed в v1.1.0
9. ✅ **Стартовый рейтинг 1000 → 0 + новые лиги + активная decay** — fixed в v1.1.0 (SkillRatingSystem v2)
10. ✅ **Лидерборд auto-fallback на «Мир»** — fixed в v1.1.0
11. ✅ **Streak freezes видимы** — fixed в v1.1.0 (❄N иконка на главной)
12. ✅ **AI Chat лимит индикатор** — fixed в v1.1.0 (AiChatLimiter, «осталось 47/50»)
13. ✅ **Reminder «Проверить сейчас»** — fixed в v1.1.0 (Settings)
14. ⚠ **Локализация контента уроков** — UI переведён, контент только русский. **РЕАЛЬНО НЕ СДЕЛАНО**, большой объём
15. ✅ **Cold start splash** — fixed в v1.1.0

### Редизайн
16. ✅ **Достижения пересмотрены** — fixed в v1.1.0 (bronze/silver/gold через xpReward, единая 🏆 иконка)

## 💎 ПЛАН МОНЕТИЗАЦИИ v2.0 (утверждено 2026-05-14)

### Бесплатно (для всех):
- Уроки A1 (60 шт), грамматика A1, диалоги A1, книги A1
- Спряжение A1 (базовые глаголы)
- Игры — **первые 10 уровней каждой** (60 уровней из 600)
- Карточки SM-2 для слов A1
- **Словарь полностью** — все 10 086 слов
- **Pronunciation полностью** — все уровни (motor skill, не зависит от уровня)
- AI Chat — **50 запросов/день**
- Слово дня + WoD-стрик + push
- Достижения, лидерборд, лиги
- Виджет, темы, био-замок

### 💎 PRO (платная подписка):
- Уроки A2 + B1 + B2 (180 шт)
- Грамматика A2 + B1 + B2
- Диалоги A2 + B1 + B2
- Книги A2 + B1 + B2 (75 рассказов из 100)
- **Полное спряжение** (1300+ глаголов, 159 с таблицами 6 времён)
- **Все 100 уровней каждой игры** (540 уровней разблокируется)
- Карточки SM-2 для слов A2/B1/B2
- AI Chat — **безлимит запросов**

### 💰 Цены (через Google Play Billing с auto-pricing по странам):
- **Месяц: $4.99** (~450₽ / ~2200₸ / 4.99€)
- **Год: $34.99** (~3150₽ / ~17000₸ / 34.99€) — экономия 42% vs месячной
- **Trial: 7 дней бесплатно PRO** для новых юзеров (auto-cancel если не понравилось)

### Listening (холд):
- Игра удалена в сессии 9, **не возрождаем сейчас**
- Возвращаемся когда будет ресурс на профессиональную озвучку (живые голоса носителей)
- Если делаем — встроим в PRO как премиум-фичу

## 📦 Версии релизов

| Версия | versionCode | Что |
|---|---|---|
| 1.0.0 | 6 | Первый релиз в Play (закрытый альфа) |
| 1.0.5 | 11 | Краш на старте (FeatureTourGate race) — отозван |
| 1.0.6 | 12 | Фикс краша + fallbackToDestructiveMigration в release |
| 1.0.7 | 13 | Фикс иконки (убран FeatureTour автопоказ) |
| 1.0.8 | 14 | A+B+C: ProGuard для Glance, async виджет, 8 Analytics events |
| 1.0.9 | 15 | Обновлён логотип (premium iOS-style) — был на review |
| 1.0.10 | 16 | 5 критичных багов: lesson count, photo, libros mission, push perm, +1415 слов JSON |
| 1.1.0 | 17 | Большой батч из 11 фиксов «Качество и баланс» |
| 1.2.0 | 19 | Theory cards Phase 1 (10 теорий блока 1.1, библиотека) |
| 1.6.0 | 26 | Radio launch (40 станций, ExoPlayer, MediaSessionService) |
| 1.6.x | 27-31 | Radio polish: фиксы, auto-discovery, Spotify-стиль |
| 1.7.0 | 32 | Radio auto-discovery (ip-API + radio-browser + probe + кэш) |
| 1.8.x | 33-37 | Listening tracker, daily mission «5 мин радио», TTS↔Radio mutex |
| 1.9.0 | 41 | Radio чистый редизайн (single-screen, фильтр-чипы, без «Поймал слово!») |
| 1.9.1 | 42 | Foreground service crash fix + discovery diagnostics |
| 1.10.0 | 48 | Audio focus, headphone unplug, ICY metadata, WorkManager weekly, URL whitelist |
| 1.10.1-4 | 49-52 | Lock screen + canonical Media3 refactor (MediaController) |
| 1.10.5-9 | 53-57 | Brand dedup, landscape, mini-player фиксы |
| 1.11.0 | 58 | Top-5 polish (auto-reconnect, haptic, loudness norm, animations) |
| 1.11.1-5 | 59-63 | Dead stations blocklist, genre mapping fix, swipe-to-hide mini |
| 1.11.6 | 64 | AI Chat security: Gemini proxy enforced + humanized errors |
| **1.11.7** | **65** | **Текущая** — 10 critical из аудита: security/DB/concurrency. AAB готов. |

## 🚀 Что в 1.1.0 (сессия 16, автономная)

Большой батч 11 фиксов одной выкладкой по запросу владельца:

| # | Что | Файл |
|---|---|---|
| 6 | Уровень в Settings → read-only + кнопка «Пересдать тест» | `SettingsScreen.kt` |
| 7 | Дневная цель — новый экран в onboarding (между reason и knowledge_check) | `DailyGoalSelectionScreen.kt` |
| 8 | «Путь до Мадрида» — конкретное «осталось +N очков» + объяснение откуда очки | `ProfileScreen.kt` |
| 9 | **Рейтинг с 0** — `SkillRatingSystem` v2 (старт 0, активная decay, новые лиги). Миграция v19→v20 обнуляет всем тестерам | `LearningAlgorithms.kt`, `AppDatabase.kt` MIGRATION_19_20 |
| 10 | Лидерборд — auto-fallback на «Мир» если в стране < 5 юзеров | `LeaderboardScreen.kt` |
| 11 | Streak freezes — иконка ❄N рядом со 🔥 на главной | `HomeScreen.kt` StatsBar |
| 12 | AI Chat лимит — клиентский счётчик 50/день + индикатор «осталось 47/50» | `AiChatLimiter.kt`, `AiChatScreen.kt` |
| 13 | Время напоминания — кнопка «Проверить сейчас» в Settings | `SettingsScreen.kt`, `DailyReminderWorker.fireOnce` |
| 14 | Локализация контента — warning «контент только русский» в language picker | `SettingsScreen.kt` |
| 15 | Cold start splash — overlay «Готовим словарь...» пока seedIfNeeded | `MainActivity.kt`, `SpanishApp.kt` |
| 16 | Достижения 23 шт — пересмотрены: bronze/silver/gold через xpReward, единая иконка 🏆, новые названия + UPDATE meta при апгрейде сохраняет unlock-флаги | `AchievementNotificationService.kt`, `AchievementsScreen.kt`, `AchievementDao.updateMetaById` |

Новые лиги после рейтинг-сброса:
```
1. Aldea perdida          0   – 99
2. Santiago de Compostela  100 – 299
3. Bilbao                  300 – 599
4. Zaragoza                600 – 999
5. Valencia               1000 – 1499
6. Sevilla                1500 – 2099
7. Barcelona              2100 – 2799
8. Madrid                 2800+
```

Decay v2: progressive — 1-5 дней по -5/день, 6-12 по -8/день, 13+ по -12/день.
Floor убран — можно вылететь обратно в Aldea (peakRating сохраняется как «личный рекорд»).

---

## 14. Сессия 14 — безопасность + Play Console регистрация (2026-05-12)

### Сделано

**A. Аудит безопасности** — найдено и исправлено:
- ✅ `AI_PROXY_URL` прописан в `local.properties` → ключ Gemini больше не попадает в APK
- ✅ HTTP-логирование отключено в release (`AppModule.kt`)
- ✅ `WordOfDayWidgetReceiver` получил `android:permission="android.permission.BIND_APPWIDGET"`
- ✅ Firestore rules: добавлена защита `weekly_cohorts/{cohortId}/members/{uid}`
- ✅ Все вхождения `HablaRu` → `ESPEAK` в комментариях
- ✅ `ContentSyncWorker` — передаёт `FirebaseStorage.getInstance()` в ContentDownloader
- ✅ Firebase Storage rules: добавлен `match /content/{file} { allow read: if true; }`

**B. Версия приложения**
- `versionCode = 6`, `versionName = "1.0.0"` — первый публичный релиз

**C. Play Console**
- Аккаунт разработчика ESPEAK (`es.espeak13@gmail.com`) создан
- Google проверяет личность (1-3 дня) — ждём письмо
- После одобрения: создать приложение → Internal Testing → залить AAB

**D. Cloudflare Worker**
- `espeak-gemini-proxy.bravochief21.workers.dev` — уже задеплоен
- Все AI-запросы идут через него → ключ скрыт от APK

### Коммиты сессии 14
| Коммит | Что |
|---|---|
| `abd2c4d` | fix: ContentDownloader URL bug (полный путь пакета) |
| `25cc2d8` | fix: ContentSyncWorker передаёт FirebaseStorage |
| `25ee073` | security: 3 уязвимости (logging, widget, firestore) |
| `ec68b94` | chore: HablaRu → ESPEAK во всех комментариях |
| `5fe9460` | chore: версия 1.0.0 (versionCode 6) |

---

## 13. Сессия 13 — Play assets + движок вариативности упражнений (2026-05-11)

Очень длинная сессия. Финальные Play Store ассеты + крупный апгрейд контентной части.

### A. Play Store assets (готовы к загрузке)
- Иконка 512×512 (`docs/play_assets/icon_512.png`) — кремовый бык на оранжевом градиенте, мягкая тень, sat-балансированный
- Feature graphic 1024×500 — тот же бренд + телефон + 9 ландмарок Испании
- 8 промо-скринов в `docs/play_assets/screenshots_promo/` — glassmorphism-баннер по центру, Montserrat Black + Medium, авто-word-wrap, выровненные размеры шрифтов на всех баннерах
- Скрипты: `_make_icon_v2.py`, `_make_feature_v8.py`, `_make_screenshot_banners.py`
- Шрифты: `docs/play_assets/_fonts/Montserrat-{Black,Medium}.ttf`

### B. Content Delivery System — Phase 0 (схема готова, downloader пока без Firebase)
- `data/content/ContentSchema.kt` — @Serializable wire-format для manifest + words/lessons/libros пакетов
- `data/content/ContentDownloader.kt` — скелет с DownloadState flow для UI (TODO бодики до Firebase-upload)
- `data/content/ContentVersionStore.kt` — DataStore для версий пакетов
- **JSON-экспортёр** работает (`ContentPackExporter` JUnit test): из in-app данных создаёт 10 файлов в `docs/content_packs/` (1.9 МБ total, sha256 + размеры в manifest)

### C. ⭐ Phase 1+2+3: ExerciseGenerator — массовый апгрейд уроков

**До**: 240 уроков × 99% multiple choice. Скучно, retention падает.
**После**: 212 уроков (88%) получают авто-генерированные упражнения, **800 новых** через 7 типов:

| Тип | Шт | Описание |
|---|---|---|
| LISTEN_PICK | 288 | TTS играет → тапнуть нужное из 4 написанных |
| BUILD_SENTENCE | 198 | Дано на ru → собрать es из тайлов |
| MATCH_PAIRS | 114 | 4-6 пар (es↔ru) соединить |
| ORDER_LETTERS | 69 | Анаграмма из букв |
| LISTEN_TYPE | 42 | Диктант: TTS → напечатай |
| TAP_MISSING_WORD | 38 | Артикль ___ noun + 3 чипа |
| TRANSLATE | 51 | Переведи на испанский (печатанием) |

**Архитектура**:
- `ui/home/LessonExercise.kt` — добавлены типы LISTEN_PICK, ORDER_LETTERS, MATCH_PAIRS, TAP_MISSING_WORD, LISTEN_TYPE + поля audioText, pairs
- `ui/home/ExerciseGenerator.kt` — 7 веток генерации из `LessonContent`, seed = hash(lessonId) → детерминизм в рамках сессии, свежие в следующей
- `ui/home/LessonSessionScreen.kt` — 5 новых рендереров: ListenPickInput, OrderLettersInput, MatchPairsInput (+ PairChip), TapMissingWordInput, ListenAndTypeInput
- Подключение: `val exercises = content.exercises + ExerciseGenerator.generate(lessonKey, content)` в LessonSession
- Локализация: новые строки `ls_tap_letters_below`, `ls_tap_to_replay`, `ls_listen_and_type` во всех 4 языках

**Тесты**: `ExerciseGeneratorTest` — все 800 упражнений валидны (correct в options, audioText не пустой, pairs уникальные, лимиты длины), детерминизм проверен.

### D. Решения (зафиксированы в `docs/LESSON_EXERCISES_PLAN.md`)
1. Авто-Translate — только одиночные слова, фразы только авторские ✓
2. SpotTheError — вручную (300 авторских вариантов) — TODO в Phase 4
3. ConjugationGrid — полная таблица 6 ячеек — TODO в Phase 4
4. Sealed-class — compat-shim, perf не страдает (не делал — пока хватило data class extension)
5. Первый запуск — real downloader с MB/s progress (TODO когда пакеты на Firebase)
6. Хостинг — Firebase Storage `spanishapp-35092.firebasestorage.app`

### Что осталось (Phase 4-5)
- ConjugationGrid renderer ✅ (сделано)
- SpotTheError — авторить 300 ошибочных вариантов (потенциально через LLM)
- BuildSentenceWithDistractors — был отменён по запросу владельца
- ReorderWords + DragToFillBlanks (B1/B2) — TODO
- CategorySort (drag/drop) — TODO
- Sealed-class рефакторинг — отложен
- Подключение Firebase Storage → выбран более простой путь GitHub Pages ✅ (сделано)

### E. ⭐ Финальная авто-сессия (после изначальной)

**A. Daily Mission** — расширил «Цель дня» с 3 до 4 пунктов (добавлен «Урок дня»), тап на тайл теперь умно ведёт к незавершённой задаче (lesson → course/a1_1 → flashcards → libros). LessonProgressDao.anyCompletedSince(since) — новый Query.

**B. Type Badges** — каждое упражнение в LessonSession теперь открывается с маленьким accent-pill: эмодзи + короткий лейбл типа (✏️ Выбор, 🔊 Аудио, 🔗 Пары, 📊 Спряжение, итд.). Визуальная идентичность для всех 11 типов.

**C. Phase 0 finish** — реальная OTA-доставка контента:
- Контент-пакеты опубликованы на gh-pages → `https://samohin13.github.io/SpanishApp/content_packs/manifest.json` (HTTP 200, 1.9 МБ)
- ContentDownloader полностью реализован (OkHttp streaming, MB/s callback, sha256 verify, версионный diff через ContentVersionStore)
- DownloadScreen — Compose UI на оранжевом градиенте, big-percent + per-pack прогресс + retry
- Hilt-провайдер для cacheRoot, маршрут `download` в Navigation, кнопка в Settings → «Загрузить обновления контента»
- Auto-trigger на первом запуске пока НЕ включён — opt-in из Settings (безопасный rollout)

### Коммиты финальной авто-сессии
| Коммит | Что |
|---|---|
| `889660f` | Откат обманок в BuildSentence (по запросу владельца) |
| `eb7d0d9` | Интерливание авторских + генерированных упражнений |
| `f25ef76` | docs: статус Phase 1-5 + per-CEFR coverage |
| `37026ea` | A: Daily Mission — 4 цели + smart routing |
| `40bbf5d` | B: visual badges на 11 типах |
| `465a1cd` (gh-pages) | Контент-пакеты опубликованы |
| `afc51ac` | C: Phase 0 finish — реальный downloader + UI |

### Коммиты сессии 13
| Коммит | Что |
|---|---|
| `a614652`-`77dd2e4` | Play assets iter (icon + feature graphic полировка) |
| `052bfa3`-`fc1b6a0` | Промо-скрины: glassmorphism, copywriting iter |
| `a27dfd7`-`671726b` | Промо-скрины: финальные размеры + word-wrap |
| `b3a8ec9`, `6f32f69` | docs/LESSON_EXERCISES_PLAN.md (план) |
| `7271786`, `eed00c6` | Content schema + exporter + 10 JSON packs |
| `573494d`, `024aa70` | Phase 1: ListenPick + Anagram + generator (+357) |
| `b2f6792`, `7f2f1ce` | Phase 2: MatchPairs + Article (+152) |
| `0f47fc2` | Phase 2: BuildSentence (+198) |
| `836e508` | Phase 3: ListenType + Translate (+93) |

---

> Предыдущее: **2026-05-11, сессия 12 (премиум-редизайн + рейтинг + контент-аудит)**

## 12. Сессия 12 — премиум-редизайн + рейтинг + контент-аудит (2026-05-11)

Очень длинная сессия. Финальный полировочный/контентный заход перед публикацией в Play.

### Главные блоки

| # | Тема | Коммиты |
|---|---|---|
| A | **Premium-редизайн UI**: HomeScreen bento с per-tile тематическими watermark, profile с premium-tile + chalice-кубком, цвета по семействам, course pills outline, edge-to-edge bottom sheet, аватары единый стиль, city watermark в полосу 130dp с soft fade, course block headers увеличены | `cd15745`, `bbc3721`, `d35fd1b`, `c7dc9a9`, `9c1cf02`, `ace8b6b`, `ddbca98`, `cebddb2`, `3d2832a`, `6e674e5`, `7d103e1`, `c946ffa`, `1f2d0ce`, `4e702db`, `e96eac2`, `ea0291f`, `c72c932`, `d828e18`, `44da9b4`, `92875c3`, `120b957`, `680ddcd`, `b8808b4`, `a9a8b00`, `8f67870`, `a41b630`, `3940a8f`, `a661e70`, `5f92355` (~30 правок) |
| B | **Bottom-bar Fade Through transition** (Material spec) — peer detection через base route, slide+fade для глубокой навигации остался | `1443bf2`, `f2302e0` |
| C | **Рейтинговая система v2**: tier-aware K (Aldea ±12 → Madrid ±2), promo resistance ×0.5 за 30 пунктов до тира, daily cap +40, per-word 24h cooldown (DB v17), animated «+N ⭐» popup глобально, bottom sheet «Как работает рейтинг», hooked Practice (был баг — рейтинг не считался) | `3fe27ed`, `12679a4` |
| D | **Weekly Leagues** (Duolingo-style) — DB v18 + WeeklyLeagueService + Firestore cohorts (uid hash bucketing) + RatingUpdater hook + WeeklyLeagueScreen + tab в LeaderboardScreen. Опт-ин. | `7764ea8` |
| E | **Pre-Play hardening**: Cloudflare Worker harden (X-App-Secret + per-IP daily cap + global daily cap), AAB сборка готова (31 МБ, V2-signed), Play Store guide в docs/PLAY_STORE_LISTING.md | `5353006` |
| F | **Контент**: +20 verb сетов × 4 verbs (80 verbs), +12 themed sets × 8 words (96 words), +17 supporting words в BasicsVocab | `2cc2a9f` |
| G | **Локализация**: full UA + ES translation (670 keys каждый), values-uk/ + values-es/ заполнены | `fdb09bc` |
| H | **UX мелочь**: TTS autoplay в Practice LISTENING (уже было), AnswerSoundPlayer (correct/wrong beeps via ToneGenerator), 100% confetti burst в CompletionBadge через Compose Canvas (60 particles) | `5213a8e` |
| I | **🚨 Critical content fix + audit**: subagent нашёл что **вся A2-вертикаль (60 уроков!) была недоступна** — id `a2_1..a2_4` в RoadmapData не совпадал с ключами `u5_l*..u8_l*` в LessonContentData (CourseDetailScreen гасил клики). Фикс: переименование a2_1..4 → 5..8. Также: 1105 broken word references в 112 из 131 set'ов → +1122 WordEntity в BasicsVocab.kt с реальными переводами; 0 broken refs после фикса. Полный отчёт: `docs/AUDIT_REPORT.md` | `01aa106` |

### Firestore rules опубликованы
В Firebase Console добавлены production rules для `leaderboard/{uid}`, `users/{uid}/state/...`, `weekly_cohorts/{cohortId}/members/{uid}`. Старые «test mode» правила (allow read/write до 2 июня 2026) убраны.

### Что осталось на стороне пользователя
- Заплатить $25 в Play Console
- Сделать иконку 512×512 (через Studio Image Asset)
- Сделать feature graphic 1024×500 (Figma/Canva)
- Снять 6-8 скриншотов с телефона
- Залить `app/build/outputs/bundle/release/app-release.aab` в Internal Testing → Production
- Добавить `AI_PROXY_SECRET` в `local.properties` + `APP_SECRET` в Cloudflare Worker env

### Build state
`./gradlew :app:assembleDebug` → BUILD SUCCESSFUL.
`./gradlew :app:bundleRelease` → BUILD SUCCESSFUL, AAB 31 МБ V2-signed.
DB version 18 (с двумя миграциями за сессию: 14→15 recent_searches, 15→16 daily_rating_gain, 16→17 last_rating_at, 17→18 weekly_league_state).

---

> Предыдущее: **2026-05-10, сессия 11 (UX-фидбэк sweep, 7 фаз)**

## 11. Сессия 11 — UX-фидбэк sweep (2026-05-10)

Пользователь прислал документ с 17 пунктами фидбэка после теста на телефоне. Разбито на 7 фаз, каждая отдельным коммитом в master:

| Фаза | Коммит | Что |
|---|---|---|
| 1 | `67c869c` | **Practice fix**: новый `getPracticePool` (3-bucket fallback weak→shaky→reviewed) — фикс пустого экрана у новых юзеров. Добавлен `LinearProgressIndicator` (раньше его НЕ БЫЛО, был только текст «1/20»). Тайтл-строка получила «✅ N ❌ N» tally. |
| 2 | `3c2a159` | **AnimatedScreenTitle** — переиспользуемый Composable (`ui/components/AnimatedScreenTitle.kt`): slide-in + letter-stagger + emoji bounce. Раскат на 10 экранов: PlacementTest, LevelSelection, Profile, Achievements, Rating, Dictionary, WeakWords, Grammar, Leaderboard, Practice. |
| 3 | `f1f115c` | **HomeScreen redesign**: `HorizontalPager` для курсов с peek + scale, time-of-day greeting + 8 ротируемых мотиваций, цвета A1/A2/B1/B2 ярче, diagonal gradient, новый `SpanishCitiesWatermark` (Madrid/Barcelona/Sevilla skylines) на фон, убраны «(N блоков)», title 28sp ExtraBold, фикс дубля «Блок 1». |
| 4 | `e6676a7` | **Color audit**: PlacementTest selected-state переписан, LevelSelection — hero-карточки с brand-градиентами, Crucigrama icon↔levels color sync, GameLevelCell получил `accent: Color`, LessonIntro теперь использует `unit.color` а не Material primary. |
| 6 | `3b4dde1` | **Profile**: фото-пикер (`PickVisualMedia`, без runtime permission) → Firebase Storage `users/{uid}/avatar.jpg` → автообновление в HomeScreen header. Hero-блок (96dp avatar + nick + лига + skill rating 32sp), 3 counter-pills, LeagueProgressCard с анимированной заливкой, MiniStatsCard (слов/уроков/макс. серия). |
| 7 | `bf57990` | **CompletionBadge** (`ui/components/CompletionBadge.kt`): круглая медаль через Canvas, gradient-кольцо, цветной фон по точности (Gold≥90 / Silver≥70 / Bronze≥50 / Steel<50), 4 звезды, лента «¡COMPLETADO!». Подключено на 3 финальных экранах: Practice, Flashcards, LessonSession. Кубковая система TrophyTier в SetRow карточках НЕ тронута — это разные вещи. |
| 5 | `e690702` | **TTS tap-anywhere**: новый `Modifier.tappableForSpeak()` — тап по всей карточке озвучивает, иконка динамика остаётся как visual cue. Применён в WeakWords, Dictionary detail, Practice header, VerbTraining audio card, Pronunciation. AiChat (уже tap-word) и LibroRead (уже long-press) не тронуты. |

**Новые компоненты в `ui/components/`**:
- `AnimatedScreenTitle.kt` — анимация заголовков
- `CompletionBadge.kt` — медаль завершения
- `SpanishCitiesWatermark.kt` (под `ui/home/`) — фоновый паттерн

**Новый DAO**: `WordDao.getPracticePool(limit)` — UNION ALL по 3 bucket'ам.

**Build**: `./gradlew :app:assembleDebug` BUILD SUCCESSFUL после всех 7 фаз. APK работоспособен.

**Что осталось из фидбэка** (отложено как полировка):
- 🟡 Pre-existing deprecation warnings (AutoMirrored icons) — не из новых правок
- 🟡 Финальный sweep размеров элементов — частично сделан в Phase 7 (типографика финальных экранов унифицирована)

---

> Последнее обновление: **2026-05-10, сессия 10 (release-blocker верификация)**

## 10. Сессия 10 — release-blocker верификация (2026-05-10)

Проверены 3 «🔴» из PLAN.md — оказалось, фактически уже сделано в прошлых сессиях:

- ✅ **APK размер**: release-сборка `app-release.apk` = **16 МБ** (R8 + shrinkResources). Лимит Google Play 150 МБ — с огромным запасом. Заметка в §9 про «207 МБ из-за word_images» **устарела** — сейчас вся папка `assets/word_images/` = 1.6 МБ (150 PNG, ~10 КБ средний).
- ✅ **release.keystore**: существует (`/release.keystore`, 2584 B, alias `ESPEAK`). `keystore.properties` заполнен реальными значениями, в `.gitignore`. Подпись APK V2 проверена через `apksigner verify`: `CN=mr.Samokhin, OU=ESPEAK, C=RU`.
- ✅ **Privacy Policy URL**: `https://samohin13.github.io/SpanishApp/PRIVACY_POLICY` уже прописан в `strings.xml` (`privacy_policy_url`, `terms_url`), кнопки в `SettingsScreen` работают через `Intent.ACTION_VIEW`.

### Что **ещё** осталось руками:
- ✅ ~~Заменить email~~ — заменено на `es.espeak13@gmail.com` (коммит `c1c76d7`).
- ✅ ~~GitHub Pages~~ — live на https://samohin13.github.io/SpanishApp/PRIVACY_POLICY.html. Pages обслуживается с **orphan-ветки `gh-pages`** (на master 228 МБ истории — Pages-runner падал на checkout). На master также лежат `PRIVACY_POLICY.html`, `index.html`, `.nojekyll`.

### Расширение контента грамматики (коммит `2ef5a00`):
- A1: 4 → **15** уроков (+11): un/una/hay, mi/tu/su, este/ese/aquel, tener + tener que, no/nada/nunca, qué/dónde/cómo, plural, согласование, gustar, ir + a + inf.
- A2: 12 → **20** (+8): Indefinido неправильных, se lo, muy/mucho, saber/conocer, предлоги, e→ie/o→ue, acabar de.
- B1: 10 → **20** (+10): Subjuntivo (желание/эмоция/сомнение), отрицательный императив, por/para тонкости, ojalá, ponerse/volverse/hacerse, llevar+gerundio.
- B2: 9 → **20** (+11): Condicional Compuesto, Subjuntivo Perf/Pluscuamperfecto, перифразы (acabar/llevar/tener+part), безличные se, идиомы, el cual/cuyo.
- **Итого 35 → 75 уроков**. Сидятся через `INSERT OR IGNORE` (id 36-75), миграций не нужно.

## 9. Сессия 9 — pre-release аудит и чистка (2026-05-07)

**Ветка**: `claude/eager-shaw-a2c9d7`. См. полный план в [PLAN.md](PLAN.md).

### Сделано в этой сессии:
- ✅ **Имя приложения** унифицировано: `ESPEAK` → `@string/app_name` (ESPEAK) в манифесте.
- ✅ **CAMERA permission** удалён (не использовался).
- ✅ **backup_rules.xml + data_extraction_rules.xml** заполнены — auth-токены исключены, БД и preferences включены.
- ✅ **`fallbackToDestructiveMigration()`** теперь только в debug-сборке (`AppModule.kt`).
- ✅ **Мёртвые маршруты** `dialogue/{id}` и `grammar/{id}` (рисовали 🚧) удалены — фактический контент уже inline в DialoguesScreen и GrammarScreen.
- ✅ **Двойной импорт `ui.games.*`** в Navigation.kt убран.
- ✅ **AnagramsGameScreen** добавлена в GamesScreen (раньше была доступна только через deeplink).
- ✅ **RatingUpdater подключён ко всем играм**: добавлен метод `applyGameAnswer(correct: Boolean)` в `RatingUpdater.kt`. Подключено в: ArticlesViewModel, SpeedViewModel, AnagramViewModel, MathViewModel, CrosswordViewModel, SopaViewModel, PalabraMaestraViewModel, VerbViewModel. Раньше работало только для Flashcards и Libros.
- ✅ **Listening assets удалены** — 1089 mp3 (~16 МБ) + sentences.json + SentencesRepository.kt. Игры не было, ассеты были мёртвым грузом.
- ✅ **ProGuard правила** прописаны в `proguard-rules.pro`: Room, Hilt, Firebase, Serialization, Compose, Glance, WorkManager, Lottie, Coil.
- ✅ **`isMinifyEnabled = true` + `isShrinkResources = true`** для release.
- ✅ **Signing config skeleton** в `app/build.gradle.kts` через `keystore.properties` (есть `keystore.properties.example`, реальный файл в `.gitignore`).
- ✅ **PRIVACY_POLICY.md** создан (шаблон, заменить email и URL перед публикацией).
- ✅ **Ссылка на Privacy Policy** добавлена в `SettingsScreen` (открывает GitHub URL).

### Что осталось (см. PLAN.md):
- 🔴 **APK 207 МБ из-за `assets/word_images/`** — 150 PNG по ~1.4 МБ каждая. **Превысит лимит Google Play 150 МБ**. Нужно сжать через squoosh.app / tinify / ImageMagick (целевой средний 30–50 КБ).
- 🔴 **API-ключ Gemini в plain-text BuildConfig** — нужен backend-прокси (Cloud Function / Worker).
- 🔴 **Создать release.keystore** через Android Studio + заполнить `keystore.properties`.
- 🔴 **Опубликовать Privacy Policy** на публичный URL и обновить ссылку в SettingsScreen.
- 🟡 Грамматика A2/B1/B2 — всего 9 уроков на 4 уровня.
- 🟡 Локализация — только русский, всё захардкожено в Compose.

### Реальное состояние (контрастно с предыдущими секциями):
- AppDatabase **version=11** (а не 9 как в §8.1).
- AI-чат на самом деле **Gemini 1.5 Flash**, не Claude (`AiChatRepository.kt:25`).
- **22 блока контента** уроков (не 60 микро-уроков).
- **50 рассказов Libros** (не 25).
- **~159 глаголов спряжения** (не 20).
- **8 игр** в GamesScreen (после добавления Anagrams) + универсальная система 100 уровней через `GameLevelManager`.

## 8.1. Рейтинговая система (новое в сессии 8)

**Ветка**: `claude/add-rating-system-7k9ox`. Состоит из четырёх частей:

### A) Skill Rating (общее число, ELO-подобный)
- Хранится в `UserProgressEntity.skillRating` (Int, default = 1000), `peakSkillRating`, `lastRatingUpdate`.
- Старт **1000**, пол **800**, потолок **3000**. Реализация: `SkillRatingSystem` в `LearningAlgorithms.kt`.
- При каждом ответе: правильно на сложном (низкий EF) → больший прирост; ошибка на лёгком → больший минус.
- Затухание: после 3 дней грейса −2/день, не ниже max(800, peak−200). Применяется `RatingDecayWorker` ежедневно.

### B) Mastery по категориям (испанские флаги 0–5)
- Не хранится в БД — считается на лету из `WordEntity` (поля `correctReviews`, `totalReviews`, `isLearned`).
- Новый DAO-метод: `WordDao.getCategoryStats()` → `List<CategoryStatsRow>`.
- Формула: `score = 0.6*coverage + 0.4*accuracy` (если ревью≥5), флаги по порогам [0.10, 0.30, 0.50, 0.75, 0.90].
- Composable `SpanishFlagRating(filled, of=5)` рисует флаги через `Canvas` (без растровых ассетов).

### C) Лиги «Путь до Мадрида» (8 ступеней)
- 1 Aldea perdida → 2 Santiago de Compostela → 3 Bilbao → 4 Zaragoza → 5 Valencia → 6 Sevilla → 7 Barcelona → **8 Madrid**.
- Пороги по skillRating: 0/1100/1300/1500/1700/1900/2100/2300+.
- Хранится `currentLeague` и `peakLeague` в `UserProgressEntity`.
- Composable `LeaguePromotionDialog` показывается после каждого ответа, если лига выросла.
- Объект `LeagueResolver` в `LearningAlgorithms.kt`: `fromRating(r)`, `next(l)`, `progressInLeague(r)`.

### D) Лидерборд (Firebase Anonymous Auth + Firestore)
- Коллекция `leaderboard/{uid}`: `{nickname, country, skillRating, peakRating, league, updatedAt}`.
- Страна — авто по `Locale.getDefault().country` (KZ/RU/FR/...). Маппинг ISO→русское название → `domain/rating/CountryNames.kt`.
- Две вкладки: своя страна (динамический заголовок) / Мир. Подиум топ-3, sticky-self.
- Опт-ин диалог при первом заходе. `setLeaderboardOptIn` в `UserProgressDao`. Можно выйти.
- Запросы: `orderBy(skillRating).limit(100)`. Свой ранг через `Aggregate.count()`.
- Файлы: `LeaderboardRepository`, `LeaderboardViewModel`, `LeaderboardScreen`.

### Ключевые файлы Rating-системы
| Файл | Что делает |
|---|---|
| `domain/algorithm/LearningAlgorithms.kt` | `SkillRatingSystem`, `MasteryRating`, `League`, `LeagueResolver` |
| `domain/algorithm/RatingUpdater.kt` | `applyAnswer(easeFactor, quality): LeaguePromotion?` — общая точка |
| `domain/rating/CountryNames.kt` | ISO-код → русское название + эмодзи флага (~50 стран) |
| `service/RatingDecayWorker.kt` | Раз в сутки применяет затухание |
| `data/db/AppDatabase.kt` | version=9, MIGRATION_8_9 (6 ALTER TABLE) |
| `data/repository/LeaderboardRepository.kt` | Firebase Auth + Firestore queries |
| `ui/components/RatingComponents.kt` | `SpanishFlagRating`, `LeagueBadge`, `LeaguePromotionDialog`, `LeaguePath` |
| `ui/profile/ProfileScreen.kt` | + 3 карточки (Путь до Мадрида, Skill Rating, Прогресс по темам) |
| `ui/profile/RatingScreen.kt` | НОВЫЙ — все 58 категорий с флагами + сортировка |
| `ui/leaderboard/LeaderboardScreen.kt` | НОВЫЙ — табы «своя страна»/«Мир», подиум, опт-ин |

### Подключение к тренировкам
- `FlashcardsViewModel` — после `SM2.review` вызывает `RatingUpdater.applyAnswer(word.easeFactor, quality)`.
- `LibrosViewModel.saveResult` — по одному applyAnswer за каждый правильный/неправильный.
- `MutableSharedFlow<LeaguePromotion>` в каждой ViewModel → собирается в Screen → показывает диалог.

### Что нужно от пользователя для онлайн-лидерборда
- `app/google-services.json` уже в репозитории.
- Firebase Console → создать Firestore Database в production mode.
- Firestore Rules:
  ```
  match /leaderboard/{uid} {
    allow read: if true;
    allow write: if request.auth != null && request.auth.uid == uid;
  }
  ```
- Включить Anonymous Authentication в Firebase Console → Authentication → Sign-in method.

---

## 0. Быстрое резюме «где мы остановились»

**Последний коммит: `83cde7b` — ветка `master`**

**Что работает прямо сейчас (всё закоммичено):**
- **Исправлены предупреждения сборки**: отключен Jetifier, обновлены иконки, подавлены deprecation-варнинги в настройках.
- **Очистка зависимостей**: версии вынесены в TOML, убраны хардкод-строки в gradle.
- **Исправление БД**: добавлен индекс для word_id (устранено KSP-предупреждение).
- **Crucigrama** — 100 уровней, зум, pan, тесты (CrosswordTest.kt)
- Приложение собирается, запускается на телефоне
- Карточки (SM-2), карусель категорий, уровни A1–B2 с замками
- **SettingsVoice**: 8 персонажей, ползунки, диагностический баннер
- **AI Chat** включён в навигацию
- **Словарь 5000+ слов** + DictionaryScreen v2 (вкладки, пользовательские списки)
- **Room version = 24** (история резюме — версия 7 устарела, см. CURRENT STATE наверху)
- **Listening game 🎧**: 1089 предложений Tatoeba (УДАЛЕНО в сессии 9 — ассеты убраны, см. §9)
- **Libros 📚** — 8-я игра, полностью реализована:
  - 25 рассказов уровня A1 (LibrosData.kt), 4 вопроса на каждый
  - Фильтр по уровню (A1/A2/B1/B2), карточки с DifficultyDots
  - Экран чтения → тест (3/4 для зачёта) → результат
  - Room-таблица `libro_progress` (версия 7, MIGRATION_6_7)
  - **Перевод по зажатию**: зажать слово → снизу выезжает тёмный бокс с переводом + словами предложения
  - Unit-тесты: LibroTextHelpersTest (20 тестов, 0 ошибок)
- **Домашний экран**: 60 микро-уроков A1, 4 блока × 15 уроков
  - Блок 1 «Взлёт» (фиолетовый), Блок 2 «Мой мир» (бирюзовый)
  - Блок 3 «Действие» (зелёный), Блок 4 «Выживание» (оранжевый)
  - LessonContentData полностью переписан (ключи u1_l0 … u4_l13)
  - Placeholder 🚧 для уроков без контента (кнопка «Отметить как пройденный»)

**Следующие задачи (этот roadmap УСТАРЕЛ — см. CURRENT STATE наверху):**
1. ✅ ~~Streak / Home счётчик~~ — давно сделано, freezes тоже видимы
2. ✅ ~~Word of Day~~ — есть на HomeScreen + виджет
3. ✅ ~~WeakWords экран~~ — есть
4. ✅ ~~AI Chat живой~~ — есть (Gemini Flash через Cloudflare proxy)
5. ⚠ **Libros A2/B1/B2** — частично (25 A1 + остальные в развитии)

---

## 1. Обзор проекта
Полнофункциональное Android-приложение на Kotlin + Jetpack Compose для изучения испанского языка русскоязычными пользователями. Включает ИИ-репетитора на базе Google Gemini Flash (через Cloudflare Worker proxy).

**Целевая аудитория:** новички (дети и взрослые), доходящие до уровня B2.
**Методика:** CEFR + современная лексика + обучение в контексте (короткие примеры) + озвучка.

## 2. Технический стек
- **Язык:** Kotlin
- **UI:** Jetpack Compose + Material3
- **Архитектура:** MVVM + Clean Architecture
- **DI:** Hilt
- **БД:** Room (SQLite, офлайн)
- **Навигация:** Navigation Compose
- **Асинхронность:** Coroutines + Flow
- **ИИ:** Google Gemini Flash API (через Cloudflare Worker proxy `espeak-gemini-proxy.bravochief21.workers.dev`)
  — старая запись «Anthropic Claude» устарела, переключились на Gemini в сессии 12-13
- **Фоновые задачи:** WorkManager (RatingDecay, RadioCatalogRefresh, ContentSync, DailyReminder)
- **Виджет:** Glance AppWidget («Слово дня»)
- **HTTP:** OkHttp
- **Хранилище настроек:** DataStore (voice_prefs, app_lock_prefs, radio_blocklist, content_versions, ...)
- **Медиа:** Media3 ExoPlayer + MediaSession (для радио)
- **minSdk:** 26, **targetSdk:** 35, **compileSdk:** 35

## 3. Структура проекта
```
app/src/main/java/com/spanishapp/
├── MainActivity.kt                         — точка входа + SpanishAppRoot (nav + bottom bar)
├── SpanishApp.kt                           — Application класс (@HiltAndroidApp), вызывает seedIfNeeded
├── data/
│   ├── db/
│   │   ├── dao/Daos.kt                     — 8 DAO интерфейсов
│   │   ├── entity/Entities.kt              — 8 Room сущностей (с @ColumnInfo)
│   │   ├── AppDatabase.kt                  — Room база данных (version = 1)
│   │   ├── DatabaseSeeder.kt               — засев БД из assets/spanish_vocab.json + ModernVocab
│   │   └── ModernVocab.kt                  — ✨ NEW: ~55 современных/разговорных слов
│   └── repository/
│       ├── AiChatRepository.kt             — Gemini Flash через Cloudflare Worker + история чата
│       └── ConjugationData.kt              — 20 глаголов × 6 времён
├── di/AppModule.kt                         — Hilt DI модуль (Room + OkHttp)
├── domain/algorithm/LearningAlgorithms.kt  — SM-2, XpSystem, StreakManager, AdaptiveLearning
├── service/
│   ├── AchievementNotificationService.kt   — 17 достижений + WorkManager + уведомления
│   └── SpeechServices.kt                   — TTS (es-ES) + SpeechRecognizer
├── ui/
│   ├── Navigation.kt                       — навигационный граф (ВАЖНО: путь ui/, не корень java/)
│   ├── components/Components.kt            — общие UI компоненты + SpanishBottomBar + AppColors
│   ├── home/
│   │   ├── HomeScreen.kt                   — главный экран
│   │   └── HomeViewModel.kt                — логика главного экрана
│   ├── flashcards/                         — ✨ NEW: экраны карточек
│   │   ├── FlashcardsViewModel.kt          — состояние сессии, SM-2, XP
│   │   ├── FlashcardsSetupScreen.kt        — выбор параметров сессии
│   │   └── FlashcardsScreen.kt             — карточка с flip-анимацией
│   └── theme/
│       ├── Theme.kt                        — Material3 тема
│       ├── Color.kt                        — AppColors (терракота и др.)
│       └── Type.kt                         — типография
└── widget/WordOfDayWidget.kt               — виджет рабочего стола (Glance)
```

## 4. База данных (Room)

### Таблицы
| Таблица | Описание |
|---|---|
| `words` | 1084 слова из JSON + ~55 из ModernVocab = ~1139 |
| `conjugations` | Спряжения 20 глаголов × 6 времён |
| `lessons` | Уроки грамматики по уровням A1/A2/B1 |
| `dialogues` | Ситуационные диалоги |
| `user_progress` | Прогресс пользователя (XP, стрик, уровень) |
| `chat_messages` | История чата с ИИ |
| `achievements` | 17 достижений |
| `daily_words` | Слово дня |

### Важно: ColumnInfo аннотации
Все camelCase поля требуют `@ColumnInfo(name = "snake_case")`:
```kotlin
@ColumnInfo(name = "next_review") val nextReview: Long = 0L
@ColumnInfo(name = "word_type") val wordType: String = "noun"
@ColumnInfo(name = "is_learned") val isLearned: Boolean = false
@ColumnInfo(name = "total_reviews") val totalReviews: Int = 0
@ColumnInfo(name = "correct_reviews") val correctReviews: Int = 0
@ColumnInfo(name = "ease_factor") val easeFactor: Float = 2.5f
@ColumnInfo(name = "audio_url") val audioUrl: String = ""
@PrimaryKey @ColumnInfo(name = "user_id") val userId: Int = 1
```

### DAO-расширения для Flashcards (уже добавлены)
```kotlin
suspend fun getDueForSession(level, category, limit, now): List<WordEntity>    // повторение
suspend fun getNewForSession(level, category, limit): List<WordEntity>         // новые
suspend fun getWeakForSession(category, limit): List<WordEntity>               // слабые
suspend fun categoriesForLevel(level): List<String>                            // для UI селектора
```

## 5. Алгоритмы

### SM-2 (интервальное повторение)
- Hard (quality 2) → сброс repetitions, интервал 1 день
- Good (quality 4) → интервал × easeFactor
- Easy (quality 5) → интервал × easeFactor + бонус EF
- `isLearned = true` при repetitions >= 3

### XP система
- Слово правильно (Good): +5 XP
- Слово легко (Easy): +10 XP
- Урок пройден: +25 XP
- Диалог (100%): +40 XP
- Дневная цель: +15 XP
- Бонус стрика: +2 × дней (макс 60)
- 30 уровней (таблица порогов в `XpSystem.LEVEL_THRESHOLDS`)

### Стрик
- Consecutive day → streak + 1
- Пропуск дня → streak = 1
- Предупреждение при 20+ часах без занятий

## 6. Методика обучения (CEFR + современный подход)

Согласована с пользователем 2026-04-22:

| Уровень | Словарь | Грамматика | Темы |
|---|---|---|---|
| **A1** | ~500 слов | Presente Indicativo, ser/estar/tener | семья, еда, числа, быт |
| **A2** | 1 000–1 500 | Pretérito Perfecto/Indefinido, рефлексивные | путешествия, покупки, эмоции |
| **B1** | 2 500–3 000 | Subjuntivo Presente, условные | мнения, работа, абстрактные темы |
| **B2** | ~4 000 | все Subjuntivo, сложные конструкции | идиомы, СМИ, деловой язык |
| **C1** | ~8 000 | стилистика, регионализмы | литература |
| **C2** | 16 000+ | нюансы | профессиональный язык |

### Принципы (договорённости с пользователем)
1. **Вариативность направлений:** ES→RU, RU→ES, Смешанный — пользователь выбирает
2. **Современная лексика:** классика + `app`, `wifi`, `selfie`, `chatear`, `guay`, `tío` и т.п.
3. **Слова в контексте:** к каждому слову — короткий (5–8 слов) разговорный пример употребления
4. **Озвучка:** TTS с выбором мужской/женский голос — отдельный экран настроек (ещё не реализован)
5. **Для детей** (6–12): тематические блоки без грамматической терминологии, короткие сессии 5–10 мин
6. **Для взрослых:** CEFR + ситуативные модули (работа, путешествия)

## 7. ИИ-репетитор (Gemini Flash API через Cloudflare Worker)
- Модель: `gemini-flash-latest` (Google-managed alias на актуальную бесплатную Flash)
- **Архитектура:** UI → AiChatRepository → Cloudflare Worker proxy (X-App-Secret) → Gemini API
- Worker URL: `espeak-gemini-proxy.bravochief21.workers.dev`
- Системный промпт: дружелюбный репетитор для русскоязычных A1/A2, короткие ответы (4-5 строк), исправления формата CORRECTIONS_JSON:[...]
- История: последние 20 сообщений из Room
- Лимит юзера: 50 запросов/день через `AiChatLimiter`
- Release-сборка **обязана** иметь `AI_PROXY_URL` (иначе `require(BuildConfig.DEBUG)` крашит)
- Debug-сборка может использовать direct call с `GEMINI_KEY` из local.properties
- Старое (Claude API) → переключились в сессии 12-13 на Gemini Flash (стоит дешевле, квота больше)

## 8. Экраны приложения

| Экран | Статус | Описание |
|---|---|---|
| HomeScreen | ✅ Реализован | Дашборд: стрик, XP, план дня, слово дня |
| **FlashcardsSetupScreen** | ✅ **Реализован (эта сессия)** | Выбор уровня, категории, направления, «только слабые» |
| **FlashcardsScreen** | ✅ **Реализован (эта сессия)** | Карточка с flip-анимацией, TTS, SM-2 оценка |
| SettingsVoice | 🔜 **Следующий шаг** | Выбор пола голоса, скорости TTS, сохранение в DataStore |
| ConjugationScreen | 🚧 Заглушка | Таблицы спряжений (данные в ConjugationData готовы) |
| DialogueScreen | 🚧 Заглушка | Ситуационные диалоги с TTS/STT |
| AiChatScreen | 🚧 Заглушка | Чат с Claude (репозиторий готов) |
| GrammarScreen | 🚧 Заглушка | Уроки грамматики |
| PronunciationScreen | 🚧 Заглушка | Тренажёр произношения (STT уже есть) |
| QuizScreen | 🚧 Заглушка | Тесты |
| ProfileScreen | 🚧 Заглушка | Профиль и статистика |
| AchievementsScreen | 🚧 Заглушка | 17 достижений |
| DictionaryScreen | 🚧 Заглушка | Поиск по словарю |
| WeakWordsScreen | 🚧 Заглушка | Слова с точностью < 60% |
| SettingsScreen | 🚧 Заглушка | Общие настройки |

## 9. Gradle

### libs.versions.toml (ключевые версии)
```toml
agp = "8.7.3"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
hilt = "2.51.1"
room = "2.6.1"
composeBom = "2024.12.01"
```

### gradle.properties (обязательно)
```properties
android.useAndroidX=true
android.enableJetifier=true
```

## 10. Словарь

### Классика: assets/spanish_vocab.json (уже в репозитории)
```
Существительные: 530
Глаголы:         225
Прилагательные:  188
Фразы:           141
Итого:          1084
```
Категории: семья, тело, еда, дом, одежда, транспорт, профессии, природа, животные, город, время, образование, работа, здоровье, спорт, технологии, эмоции.

### ✨ Современная лексика: `data/db/ModernVocab.kt` (~55 слов)
Категории: `tecnologia`, `redes_sociales`, `comunicacion`, `entretenimiento`, `expresiones` (сленг), `trabajo`, `finanzas`, `compras`.
Примеры: `la app`, `el wifi`, `la historia` (сторис), `el seguidor`, `chatear`, `subir`, `peli`, `serie`, `guay`, `molar`, `tío/tía`, `flipar`, `currar`, `la pasta` (деньги), `el teletrabajo`, `la videollamada`.
Каждое слово с короткими разговорными примерами.

## 11. Известные решённые проблемы
1. ✅ `@ColumnInfo` обязателен для всех camelCase полей в Room
2. ✅ `UserProgressDao` — запросы без `WHERE user_id = 1` (используй `LIMIT 1`)
3. ✅ `getAllSessions()` — `suspend fun`, не `Flow` (Room не может `Flow<List<String>>`)
4. ✅ `WordOfDayWidget` — упрощённая версия без сложных Glance импортов
5. ✅ `AchievementNotificationService` — `android.R.drawable.ic_dialog_info` вместо кастомных
6. ✅ `SpeechServices` — удалена строка `EXTRA_ONLY_RETURN_LANGUAGE_RESULTS`
7. ✅ `gradle.properties` — `useAndroidX=true` и `enableJetifier=true`
8. ✅ `AndroidManifest.xml` — `android:name=".SpanishApp"` в `<application>`
9. ✅ **Navigation.kt существовал в двух местах** (`java/Navigation.kt` и `ui/Navigation.kt`) — активный путь `ui/Navigation.kt`. Переименование завершено коммитом 78ebfb3.
10. ✅ **seedIfNeeded() не вызывался** — добавлен вызов в `SpanishApp.onCreate()` (в последнем незакоммиченном коде на момент паузы)
11. ✅ **Radio-кнопки направления нельзя было нажать целиком** — исправлено `Modifier.clickable` на всю Surface
12. ✅ **TextSelectionColors / LocalTextSelectionColors** — НЕ из `material3.*`, импорт должен быть явным или не использовать вовсе
13. ✅ **detectTapGestures(onLongPress) внутри verticalScroll** — не работает, скролл забирает жест. Решение: убрать verticalScroll с родителя
14. ✅ **TranslationBanner внутри verticalScroll** — уходит за экран при прокрутке. Решение: Box-оверлей с Alignment.BottomCenter (или TopCenter)
15. ✅ **BasicTextField(readOnly=true)** — первый тап даёт фокус без onValueChange, перевод требует второго тапа. Не использовать для перевода
16. ✅ **scope.launch внутри pointerInput** — краши при recomposition. Использовать только detectTapGestures без сторонних корутин

## 12. История коммитов (GitHub: Samohin13/SpanishApp)

| SHA | Ветка | Описание |
|---|---|---|
| 0754758 | - | first commit |
| bf19edb | - | Initial project structure — packages and empty files |
| 2447f5c | - | Add source code to all files |
| 428083d | - | Add source code to all files |
| ba2890d | master (старый) | Add source code to all files |
| f8c805b | master, SaveGitHub | Add Flashcards feature (setup + SRS session + TTS) |
| 78ebfb3 | master, SaveGitHub | Wire Flashcards into ui/Navigation.kt + user edits |
| f7c557d | SaveGitHub | Dark mode: near-black base + vivid accents, card shadows |
| 998447e | SaveGitHub | Apply Oliva palette: green primary, orange accent, cream bg |
| 5175600 | SaveGitHub | Expand dictionary to 5100+ words: add 3 vocab packs |
| 9af0e6f | SaveGitHub/master | Dictionary: custom word lists + word detail cards + dedup |
| 52c60e2 | SaveGitHub/master | Add Listening game: 1089 Tatoeba sentences + native audio (12.6 MB) |
| 8a8eacb | master | Redesign Flashcards screen to match Figma |
| d742d6b | master | Full redesign: light theme + purple accent (Figma style) |
| (сессия 7) | master | 60-lesson A1 catalog: 4 blocks × 15 micro-lessons |
| (сессия 7) | master | Libros game: 25 A1 stories + quiz + Room progress |
| 0dc2c39 | master | Fix Libros word translation: overlay banner + verified tap detection |
| c6c8c72 | master | Libros: long-press → bottom translation box (no scroll conflict) |

### Ветки на GitHub
- **master** — основная, тут работаем
- **SaveGitHub** — бэкап-ветка (остаётся для отката по просьбе пользователя)

## 13. Следующие шаги (roadmap)

### Immediate (следующая сессия)
1. Проверить что Libros перевод по зажатию работает на телефоне (последний коммит c6c8c72)
2. **Streak на HomeScreen** — счётчик дней подряд
3. **Word of Day** — слово дня на HomeScreen с озвучкой

### Средний приоритет
1. **Libros A2/B1/B2** — добавить рассказы 26–100 в LibrosData.kt (сейчас 25 × A1)
2. **WeakWords** — экран слабых слов (DAO уже готов)
3. **AI Chat** — живой экран чата с Claude (репозиторий готов)

### Коммит 3 — базовые заглушечные экраны в живые
Порядок по ценности для пользователя:
1. **Dictionary + Weak Words** — просмотр всех слов, фильтры, поиск
2. **Conjugation + ConjugationQuiz** — данные готовы
3. **Dialogues** + `dialogue/{id}` — с TTS/STT
4. **Grammar** + `grammar/{id}`
5. **AiChat** — репозиторий готов
6. **Profile + Achievements + Settings** — статистика
7. **Pronunciation** — через STT

### Идеи на потом
- **Облачная синхронизация** через `syncToken` в `UserProgressEntity`
- **Дневной план**: планировщик ровно под доступное время (адаптивно)
- **Геймификация**: лиги, рейтинги с друзьями
- **Офлайн-аудио**: предзагрузка озвучки для слов (вместо онлайн TTS)
- **Разделение интерфейса** для детей vs взрослых (визуальный стиль)
- **ChatGPT-режим**: голосовой диалог полностью (STT → Claude → TTS)
- **Импорт словарей** из Anki / CSV
- **Виджет** с ежедневным словом — расширить до быстрого опроса прямо из виджета

## 14. Правила работы с пользователем (важно!)

Закреплено в memory/:

1. **Пользователь — новичок** в Android/Git/сборке. Всегда объяснять **пошагово**, где кликать в IDE или какую команду вставлять в терминал, какой результат ожидать.
2. **Коммитить каждую итерацию** правок в git автоматически — не ждать разрешения. Пушить в `origin/master` (и `SaveGitHub` для бэкапа).
3. **Резервные копии (обязательно в конце каждой сессии):**
   - Переместить `SaveGitHub` на текущий master: `git checkout SaveGitHub && git merge master --ff-only && git push origin SaveGitHub && git checkout master`
   - Когда пользователь подтверждает «всё работает на телефоне» — создать датированный тег: `git tag -a stable-YYYY-MM-DD -m "описание" && git push origin stable-YYYY-MM-DD`
   - Текущий стабильный тег: `stable-2026-05-04`
4. **Память**: `C:\Users\bravo\.claude\projects\C--Users-bravo-AndroidStudioProjects-SpanishApp2\memory\`:
   - `user_level.md` — новичок
   - `feedback_explain_steps.md` — пошаговые инструкции
   - `feedback_always_commit.md` — коммит после каждой правки
   - `MEMORY.md` — индекс

## 15. Как возобновить работу после паузы

1. **Прочитать этот CLAUDE.md целиком** — тут весь контекст
2. Проверить статус: `git log --oneline -5` — последний коммит `c6c8c72`
3. Следующая задача — **Streak на HomeScreen** (или Libros A2/B1/B2 рассказы)

### Ключевые файлы Libros (сессия 7)
| Файл | Что делает |
|---|---|
| `ui/games/LibrosData.kt` | 25 рассказов A1, `PASS_CORRECT=3`, `getById()`, `getByLevel()` |
| `ui/games/LibrosScreen.kt` | Список рассказов: фильтр по уровню, карточки с DifficultyDots |
| `ui/games/LibroReadScreen.kt` | Чтение → тест → результат; зажатие слова → перевод снизу |
| `ui/games/LibrosViewModel.kt` | `lookupWord()` — ищет слово + слова предложения в WordDao |
| `data/db/entity/Entities.kt` | `LibroProgressEntity` (libro_progress) |
| `data/db/dao/Daos.kt` | `LibroProgressDao` |
| `data/db/AppDatabase.kt` | version=7, MIGRATION_6_7 |
| `test/.../LibroTextHelpersTest.kt` | 20 unit-тестов для extractWordAt / extractSentenceAt |

### Важные технические решения Libros (чтобы не повторять ошибки)
- **`detectTapGestures(onLongPress)` конфликтует с `verticalScroll`** — решение: убрать `verticalScroll` с внешнего Column, оставить только внутри текстовой карточки
- **Бокс перевода должен быть Box-оверлеем** (`Alignment.BottomCenter`), а НЕ внутри скролла
- **`BasicTextField(readOnly=true)`** требует 2 тапа (первый — фокус) — не использовать для перевода
- **`scope.launch` внутри `pointerInput`** — не использовать, вызывает краши при recomposition
- **`withTimeoutOrNull` внутри `awaitEachGesture`** — ненадёжно, `verticalScroll` отменяет через cancel

### Ключевые файлы SettingsVoice (сессия 2)
| Файл | Что делает |
|---|---|
| `data/prefs/VoicePersona.kt` | 8 персонажей, VoiceSlot, VoiceCategory, питчи/скорость |
| `data/prefs/VoicePreferences.kt` | DataStore "voice_prefs" — personaId, voiceName, rate, pitch |
| `service/SpeechServices.kt` | speakNow() для preview, applyCurrent() при изменении настроек |
| `ui/settings/VoiceSlotResolver.kt` | classifyStrict() — честный подсчёт мужских/женских голосов |
| `ui/settings/SettingsVoiceScreen.kt` | UI: PersonaCard, TunePersonaSheet, WarningBanner, DiagnosticsBanner |
| `ui/settings/SettingsScreen.kt` | Хаб настроек → навигация в settings_voice |
| `ui/Navigation.kt` | Маршруты settings, settings_voice |

## 16. Регламент обновлений этого файла

- **Каждые 30–60 минут активной работы** — дополнять раздел «быстрое резюме», добавлять решённые проблемы, обновлять roadmap
- **После каждого коммита** — добавить строку в таблицу «История коммитов»
- **При появлении новой договорённости с пользователем** — зафиксировать в разделе «Методика» или «Правила работы»
- **Никогда не перезаписывать целиком** — только дополнять и реструктурировать по смыслу
