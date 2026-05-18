# 🐛 BUGS — ESPEAK Bug Tracker

> Единый журнал багов. Ведётся Claude. Юзер только читает.
> Формат: ID — Priority — Area — Title — Status — Commit (если фикш).

**Последний апдейт:** 2026-05-18

---

## Legend

| Priority | Meaning |
|---|---|
| 🔴 P0 — Critical | Краш / блокер сборки / релиз невозможен |
| 🟠 P1 — High | Юзер видит, ломает UX / падают тесты |
| 🟡 P2 — Medium | Заметно при внимательном осмотре, не блокер |
| 🟢 P3 — Low | Косметика, edge cases, nice-to-have |

| Status | Meaning |
|---|---|
| 🔴 OPEN | Не починен |
| 🟡 IN PROGRESS | В работе сейчас |
| 🟣 DESIGN NEEDED | Решение за владельцем (дизайн/scope) |
| ✅ FIXED | Починен, ссылка на коммит |
| 🧊 WONTFIX | Сознательно не чиним (причина в записи) |

---

## 🔴 OPEN

### Startup / Runtime

### Code quality (обнаружено в полном аудите 2026-05-18)

#### BUG-015 — 🟠 P1 — API — Unsafe JSON parsing в AiChatRepository
- **Файлы:** [AiChatRepository.kt:183-189](app/src/main/java/com/spanishapp/data/repository/AiChatRepository.kt:183), [AiChatRepository.kt:323-329](app/src/main/java/com/spanishapp/data/repository/AiChatRepository.kt:323)
- **Симптом:** 7x `!!` подряд при парсинге Gemini response. Если API вернёт пустой `candidates` array или изменит структуру — NPE / IndexOutOfBoundsException.
- **Mitigated by:** внешний `try/catch(Exception)` (line 204, 332) — крах перехватится, юзер увидит generic error.
- **Risk:** функциональный (юзер не получит ответ), не crash.
- **Fix:** заменить `!!` на safe access `?.` + `?:` с fallback или kotlinx.serialization type-safe data classes
- **Status:** OPEN (не блокер, после релиза)

#### BUG-016 — 🟡 P2 — Code — GlobalScope.launch в 2 ViewModels
- **Файлы:**
  - [RadioViewModel.kt:240](app/src/main/java/com/spanishapp/radio/ui/RadioViewModel.kt:240) — IO работа в `onSessionEnded`
  - [LibrosViewModel.kt:89](app/src/main/java/com/spanishapp/ui/games/LibrosViewModel.kt:89) — **намеренно** (комментарий объясняет — гарантия записи прогресса при мгновенном уходе с экрана)
- **Симптом:** GlobalScope обходит lifecycle — fire-and-forget. Технически утечка, но осознанная в Libros.
- **Action:** Libros оставить с комментарием, Radio — проверить нужна ли. **Не блокер.**
- **Status:** OPEN (после релиза)

#### BUG-017 — 🟡 P2 — Lifecycle — 55 экранов используют collectAsState вместо collectAsStateWithLifecycle
- **Файлы:** AiChatScreen, ConjugationScreen, DictionaryScreen + 50+ других
- **Симптом:** Flow продолжает собирать данные когда экран в background. Не утечка памяти (state cancels на recompose) но избыточная работа.
- **Fix:** Massive find-replace `collectAsState(` → `collectAsStateWithLifecycle(`. ~30 мин работы.
- **Status:** OPEN (после релиза, batch refactor)

#### BUG-018 — 🟢 P3 — Security — google-services.json не в .gitignore
- **Симптом:** файл закоммичен в git (содержит только public IDs, не секреты)
- **Best practice:** Firebase Console генерирует новый каждый раз, безопаснее не коммитить
- **Risk:** низкий (нет secrets), но засоряет diff на каждом изменении Firebase project settings
- **Status:** OPEN (косметика)

#### BUG-019 — 🟢 P3 — UI — LaunchedEffect(Unit) без keys в 4 файлах
- **Файлы:** StaggeredEntrance:34, CompletionBadge:95, MemoryMatchPairsInput:182, AppLockScreen:117
- **Симптом:** Анимации могут рестартовать на parent recomposition. В текущем коде работает корректно.
- **Status:** OPEN (косметика, документировать или починить)

#### BUG-013 — 🔴 P0 — Runtime — ANR на запуске после pull v1.17.2
- **Симптом:** Android показывает "Приложение ESPEAK не отвечает" с кнопками "Закрыть/Подождать"
- **Скриншот:** на экране CourseDetailScreen (Курс B1), но юзер говорит ANR при запуске с нуля
- **Когда началось:** 2026-05-18, сразу после `git pull` v1.17.2 (cd9ed19..237a9b0)
- **Устройство:** планшет (видно по скриншоту, разрешение ~800px width, dark theme)
- **Подозреваемые источники (в порядке вероятности):**
  1. `MainActivity.attachBaseContext` runBlocking DataStore (CLAUDE.md known IMPORTANT)
  2. `databaseSeeder.seedIfNeeded()` если БД была очищена при pull (10K+ слов на старом CPU = 5+ сек)
  3. HomeViewModel загрузка roadmap + progress
  4. CourseDetailScreen рендер 16+ уроков с progress lookup
  5. Какой-то Hilt heavy graph init
- **НЕ от моих изменений в v1.17.2 напрямую:**
  - Все мои фиксы — Composable scope, не startup path
  - Drawable add — compile-time
  - Но регрессия по времени связана с pull → нужен Logcat
- **Action:**
  1. Запросить Logcat у юзера в момент ANR
  2. Stack trace главного потока покажет точку блокировки
  3. После root cause — фиксить async/await
- **Status:** в работе

### Tests (предсуществующие, обнаружены 2026-05-18)

#### BUG-014 — 🟠 P1 — Firebase Storage — Avatar upload permission denied [FIXED v1.17.3]
- **Симптом:** при попытке загрузить аватар через SettingsScreen появляется toast "Ошибка сохранения в облако: User does not have permission to access this object"
- **Root cause:** **рассинхрон путей и bucket'ов** между двумя экранами:
  - `SettingsScreen.kt:235` → bucket `gs://spanishapp-35092.firebasestorage.app` + путь `avatars/{uid}.jpg`
  - `ProfileScreen.kt:164` → default bucket + путь `users/{uid}/avatar.jpg`
  - Storage rules в Firebase Console: `users/{uid}/*` (CLAUDE.md §10)
  - SettingsScreen path **не подпадал** под rules → permission denied
- **Fix:**
  - SettingsScreen перешёл на default bucket (`FirebaseStorage.getInstance()`)
  - Upload path → `users/{uid}/avatar.jpg`
  - Delete path (при удалении аккаунта) → `users/{uid}/avatar.jpg`
- **Status:** ✅ FIXED в v1.17.3


#### BUG-001 — 🟠 P1 — Tests — SkillRatingSystemTest 3 fails [FIXED v1.17.4]
- **Файл:** `app/src/test/java/com/spanishapp/SkillRatingSystemTest.kt`
- **Failures:**
  - `decayKicksInAfterGracePeriod` (RatingSystemTest.kt:59)
  - `decayDoesNotGoBelowPeakBuffer` (RatingSystemTest.kt:69)
  - +1 ещё
- **Симптом:** rating decay не срабатывает как ожидается
- **Когда появилось:** после v1.16.0 / v1.17.0 (CLAUDE.md 2026-05-17 утверждал 236/236 зелёные)
- **Блокирует:** `./gradlew preRelease` (нельзя собрать AAB через гейт)
- **Action:** диагностировать — изменилась логика RatingUpdater или сами тесты устарели

#### BUG-002 — 🟠 P1 — Tests — LeagueResolverTest 3 fails [FIXED v1.17.4]
- **Failures:**
  - `ratingThresholdsAreContiguous`
  - `progressInLeagueRespectsBounds`
  - `startingRatingMapsToAldea`
- **Когда:** то же что BUG-001
- **Блокирует:** preRelease
- **Action:** связан с BUG-001 (rating thresholds могли быть изменены)

#### BUG-003 — 🟠 P1 — Tests — LocalizationIntegrityTest 2 fails [FIXED v1.17.4]
- **Failures:**
  - `every_translatable_ru_key_exists_in_all_other_locales` (LocalizationIntegrityTest.kt:82)
  - `locale_key_counts_are_balanced` (LocalizationIntegrityTest.kt:279)
- **Симптом:** в `values/strings.xml` добавлены новые RU ключи, но не продублированы в `values-en/`, `values-uk/`, `values-es/`
- **Когда:** после v1.16.0 (Hint Bank) или v1.17.0 (Light theme) — добавляли новые UI элементы
- **Блокирует:** preRelease
- **Action:** найти расхождение через `diff`, добавить недостающие переводы (по правилам, локализация контента заморожена, но UI ключи должны быть)

---

## 🟣 DESIGN NEEDED

#### BUG-004 — 🟡 P2 — UI — sceneGradientFor() пастельные градиенты в dark theme
- **Файл:** [ChatBubble.kt:253-280](app/src/main/java/com/spanishapp/ui/components/ChatBubble.kt:253)
- **Симптом:** функция возвращает пастельные градиенты для тематических сцен диалогов (🍽 ресторан beige, 🏨 отель light blue, 🚖 такси mint, и др.). В dark theme выглядят чужеродными светлыми пятнами.
- **Используется в:** CheckpointSessionScreen, DialogueFillInput
- **Trade-off:**
  - **Оставить как есть** — нарушает консистентность темы, но сохраняет иммерсивную ассоциацию с местом (ресторан = тёплый, отель = холодный)
  - **Затемнить** с сохранением hue — потеря яркости/настроения
  - **Скрыть в dark**, заменить на нейтральный `surfaceContainer` — потеря тематичности
- **Action:** обсудить с владельцем продукта

---

## ✅ FIXED (последние 30 дней)

### v1.17.4 — Release readiness: tests + локализация (2026-05-18)

| ID | Priority | Title |
|---|---|---|
| BUG-001 | 🟠 P1 | SkillRatingSystemTest 2 fails — тесты под старую rating v1, обновлены под v2 (start=0, decay 5/8/12, FLOOR=0) |
| BUG-002 | 🟠 P1 | LeagueResolverTest 3 fails — пороги переписаны под v2 (Aldea 0-99, ..., Madrid 2800+) |
| BUG-003 | 🟠 P1 | LocalizationIntegrityTest 2 fails — добавлены 18 переводов hint_* и widget_* в en/uk/es |

Тесты: 178/185 → **185/185 PASS** ✅

### v1.17.3 — Firebase Storage avatar fix (b41735d, 2026-05-18)

| ID | Priority | Title | File |
|---|---|---|---|
| BUG-014 | 🟠 P1 | Avatar upload permission denied — путь синхронизирован `users/{uid}/avatar.jpg` | [SettingsScreen.kt:237](app/src/main/java/com/spanishapp/ui/settings/SettingsScreen.kt:237) |

### v1.17.2 — Light theme полировка (cd9ed19, 2026-05-18)

| ID | Priority | Title | File:Line |
|---|---|---|---|
| BUG-005 | 🔴 P0 | `ic_notification_trophy.xml` отсутствовал — master не компилировался | [drawable/ic_notification_trophy.xml](app/src/main/res/drawable/ic_notification_trophy.xml) (создан) |
| BUG-006 | 🟠 P1 | LessonContent hardcoded `#F5F5F8` background | [LessonContentScreen.kt:136](app/src/main/java/com/spanishapp/ui/home/LessonContentScreen.kt:136) |
| BUG-007 | 🟠 P1 | LessonContent hardcoded `Color.White` Surface | [LessonContentScreen.kt:194](app/src/main/java/com/spanishapp/ui/home/LessonContentScreen.kt:194) |
| BUG-008 | 🟡 P2 | LessonContent hardcoded `#F0F0F0` divider | [LessonContentScreen.kt:202](app/src/main/java/com/spanishapp/ui/home/LessonContentScreen.kt:202) |
| BUG-009 | 🟡 P2 | PlacementTest tip-карточка hardcoded beige | [PlacementTestScreen.kt:348](app/src/main/java/com/spanishapp/ui/auth/PlacementTestScreen.kt:348) |
| BUG-010 | 🟡 P2 | Onboarding GDPR карточка hardcoded peach | [OnboardingScreens.kt:199](app/src/main/java/com/spanishapp/ui/auth/OnboardingScreens.kt:199) |
| BUG-011 | 🟢 P3 | VoiceInstallDialog inner Surface hardcoded `#F5F5F8` | [VoiceInstallDialog.kt:109](app/src/main/java/com/spanishapp/ui/components/VoiceInstallDialog.kt:109) |
| BUG-012 | 🟢 P3 | SettingsVoice `LightBg` dead code | [SettingsVoiceScreen.kt:40](app/src/main/java/com/spanishapp/ui/settings/SettingsVoiceScreen.kt:40) |

### v1.17.1 — Light theme iOS systemGrouped (b2c7acb, 2026-05-18)
- Карточки не сливались с фоном — переход на iOS systemGroupedBackground паттерн
- SpanishCitiesWatermark alpha скорректирован для light
- Station carousel border/text theme-aware

### v1.17.0 — Premium light theme (017860d, 2026-05-18)
- Полная палитра Apple HIG × Material 3
- AppPalette.kt — единый источник theme-aware цветов

---

## 📋 ROADMAP (из CLAUDE.md, не баги — фичи)

См. [CLAUDE.md §8 Roadmap](CLAUDE.md) для актуального списка. Кратко:

**Высокий приоритет:**
- 14 V2-only уроков (`_5` суффикс) не показаны в RoadmapData.kt (~30 мин)
- 22 IMPORTANT из audit 2026-05-17 (rememberSaveable, popUpTo safety, etc.)

**Средний:**
- Sleep timer радио, Lockscreen artwork, Recently played, Voice EQ, Listening streak, Radio achievements, Android Auto

**Бизнес:**
- Google Play Billing для PRO (план в CLAUDE.md §9)

---

## Как пользоваться

- **Найти все открытые баги:** Ctrl+F → "🔴 OPEN" в этом файле
- **Найти что починили недавно:** "✅ FIXED" + последняя версия
- **Добавить новый баг:** Claude добавляет автоматически при обнаружении
- **Запросить статус:** скажи Claude — "что в BUGS.md по теме X"
