# SpanishApp — План разработки и анализ готовности

> **Обновлено:** 2026-05-07 после полного аудита кода (ветка `claude/eager-shaw-a2c9d7`).
> Этот файл — единый источник правды о состоянии проекта.
> Отмечай выполненное `[x]`. Не перезаписывай — дополняй.

---

## 0. TL;DR — где мы сейчас

**Что работает в живом виде:**
- Аутентификация Firebase (Welcome / Register / Login / Forgot password) + 4-шаговый онбординг (имя → возраст → причина → уровень или placement-test).
- Главный экран с курсами A1/A2/B1/B2, стриком, словом дня, XP-баром.
- 22 блока контента уроков (`LessonContentData.kt`, ~16k строк) + автоматическая инъекция SPEAKING-упражнений.
- Flashcards с SM-2, направления ES↔RU, TTS.
- 8 игр в `GamesScreen`: Artículos (100 уровней), Rápido, Verbos (VerbTraining), Sopa de Letras, Palabra Maestra, Cálculo, Crucigrama (100 уровней + zoom/pan), Libros (50 рассказов).
- Conjugation (~159 глаголов × 6 времён) + ConjugationQuiz.
- Dictionary + Weak Words.
- Pronunciation (TTS + STT + score).
- Quiz, Profile, Achievements, Settings, SettingsVoice.
- AI-чат (на самом деле **Gemini 1.5 Flash**, не Claude — см. §3.6).
- Рейтинговая система: Skill Rating (ELO 1000), mastery по категориям (флаги 0–5), 8 лиг «Путь до Мадрида», лидерборд (Firebase Anonymous Auth + Firestore).
- DailyReminderWorker (19:00) + RatingDecayWorker.
- Glance-виджет «Слово дня» (receiver на месте).
- ~1400 слов в `assets/spanish_vocab.json` + большой CleanVocab + VocabExtra1..12 (цель 10k).
- TTS (es-ES, 8 персонажей с настройками).

**Чего нет совсем:**
- Listening-игры (но 1089 mp3 + sentences.json лежат в assets — мёртвый груз).
- Privacy Policy (страница, ссылка).
- Signing config для release (блокатор Google Play).
- Локализации на другие языки (всё захардкожено в Compose).
- Своего backend-прокси для AI (ключ Gemini попадает в APK plain-text).

**Заглушки (рисуют 🚧 имя экрана):**
- `dialogue/{id}` — детальный экран диалога (`Navigation.kt:247`).
- `grammar/{id}` — детальный экран урока грамматики (`Navigation.kt:254`).

---

## 1. ✅ Готово (живой код, проверено в файлах)

### 1.1. Аутентификация и онбординг
- [x] **Firebase Auth** — email/пароль, Google sign-in, anonymous для лидерборда (`AuthRepository.kt`).
- [x] **WelcomeScreen / RegisterScreen / LoginScreen / ForgotPasswordScreen**.
- [x] **Онбординг 4 шага**: NameEntry → AgeSelection → ReasonSelection → KnowledgeCheck → (PlacementTest → PlacementResult) или LevelSelection.
- [x] **Стартовый маршрут** вычисляется из `AuthViewModel` (Navigation.kt:78–110).

### 1.2. Главный экран и уроки
- [x] **HomeScreen** — стрик, XP, слово дня, карточки A1/A2/B1/B2.
- [x] **CourseDetailScreen** — список уроков курса.
- [x] **LessonIntro / LessonContent / LessonSession** — полный движок уроков (теория + упражнения + speaking-инжект через `enrichWithSpeaking`).
- [x] **22 блока контента** в `LessonContentData.kt` (изначально планировалось 60 микро-уроков A1, выросло в полноценный курс).

### 1.3. Карточки и словарь
- [x] **FlashcardsSetup / FlashcardsScreen** — flip-анимация, SM-2, TTS, выбор уровня/категории/направления, режим «только слабые».
- [x] **DictionaryScreen** — поиск, фильтры по уровню/типу слова, пользовательские списки (`word_lists`, `word_list_entries`).
- [x] **WeakWordsScreen** — слова с точностью < 60%.
- [x] **DAO-методы** для сессий: `getDueForSession`, `getNewForSession`, `getWeakForSession`, `categoriesForLevel`.

### 1.4. Игры (8 шт. в `GamesScreen`)
- [x] **Artículos** — el/la/un/una, 100 уровней (`assets/articles_levels.json` + `ArticleGameDao`).
- [x] **Rápido** — 4 варианта, таймер, бонус за скорость.
- [x] **Verbos** (VerbTraining) — тренировка спряжений, тоже подключена как `conjugation_quiz`.
- [x] **Sopa de Letras** — поиск слов в сетке.
- [x] **Palabra Maestra** — игра в слова.
- [x] **Cálculo** — испанские числительные, `NumberToSpanish.kt`.
- [x] **Crucigrama** — 100 уровней, zoom/pan, тесты `CrosswordTest.kt`.
- [x] **Libros** — 50 рассказов (не 25, как указано в CLAUDE.md), фильтр по уровню, чтение → тест (3 из 4) → результат, перевод по long-press.
- [x] **Универсальные компоненты**: `LevelMapScreen`, `LevelCompleteSheet`, `GameAnimations` + `GameLevelSystem` + таблица `game_level_progress`.

### 1.5. Спряжения
- [x] **ConjugationData / 2 / 3** — ~159 глаголов × 6 времён (Presente, Pretérito Indefinido, Pretérito Imperfecto, Futuro, Condicional, Subjuntivo Presente).
- [x] **ConjugationScreen** — раскрываемые таблицы.

### 1.6. Грамматика и диалоги
- [x] **GrammarScreen** — список карточек.
- [x] **DialoguesScreen** — список с TTS.
- [x] **9 уроков грамматики** в `GrammarContent.kt` (мало для 4 уровней — см. §2.2).
- [x] **15 диалогов** в `DialogueContent.kt`.

### 1.7. AI-чат
- [x] **AiChatScreen + AiChatViewModel + AiChatRepository** — реально живой чат, история в Room (`chat_messages`).
- [x] **Системный промпт** на исправление ошибок (CORRECTIONS_JSON).
- [x] **Отдельный grammar-check endpoint**.

### 1.8. Произношение, тесты, профиль
- [x] **PronunciationScreen** — TTS + STT + скоринг.
- [x] **QuizScreen** — 10 вопросов, 4 варианта, результаты.
- [x] **ProfileScreen** — карточки рейтинга, путь до Мадрида, прогресс по темам.
- [x] **AchievementsScreen** — 17 достижений.
- [x] **SettingsScreen + SettingsVoiceScreen** — 8 персонажей TTS, ползунки скорости/тона, диагностический баннер.

### 1.9. Рейтинговая система
- [x] **SkillRating** (ELO, 1000 старт, 800–3000) в `LearningAlgorithms.kt`.
- [x] **MasteryRating** — флаги 0–5 на лету из `WordEntity`.
- [x] **8 лиг** «Путь до Мадрида»: Aldea perdida → Santiago → Bilbao → Zaragoza → Valencia → Sevilla → Barcelona → Madrid.
- [x] **LeaguePromotionDialog** показывается после ответа.
- [x] **RatingDecayWorker** — раз в сутки, затухание после 3 дней грейса.
- [x] **LeaderboardScreen** — две вкладки (своя страна / Мир), подиум топ-3, sticky-self, опт-ин.
- [x] **Firebase Anonymous Auth + Firestore** для лидерборда.
- [x] **CountryNames.kt** — ISO → русское название + эмодзи флага (~50 стран).
- [x] **RatingUpdater подключён** в FlashcardsViewModel и LibrosViewModel.

### 1.10. Сервисы и фон
- [x] **DailyReminderWorker** (19:00) + permission `POST_NOTIFICATIONS`.
- [x] **RatingDecayWorker** — ежедневное затухание.
- [x] **WordOfDayWidget** + `WordOfDayWidgetReceiver` (Glance) — receiver на месте.
- [x] **DatabaseSeeder** — `seedIfNeeded()` вызывается в `SpanishApp.onCreate()`.

### 1.11. Контент-данные
- [x] **assets/spanish_vocab.json** — ~1415 слов.
- [x] **CleanVocab + VocabExtra1..12** — большой объём встроенных слов (цель 10 000, `VOCAB_TARGET = 10000`).
- [x] **assets/word_images/** — 150 PNG.
- [x] **assets/articles_levels.json** — данные Artículos.
- [x] **assets/sentences.json + sentences_audio/** — 1089 mp3 (не используются, см. §2.3).

### 1.12. Дизайн-система (последние коммиты)
- [x] **Полная типографика** (8e26f3d).
- [x] **CEFR-градиенты, cool-gray bg, near-black text** (e5196e0).
- [x] **Полный design system** (a2f71ba).
- [x] **Палитра «Sunset over Barcelona»** (6013a41).

---

## 2. 🔴 Блокаторы релиза в Google Play

> Эти пункты обязательно закрыть до загрузки в Play.

- [ ] **2.1. Signing config отсутствует** — `app/build.gradle.kts:37–45` нет `signingConfigs`. Без него нельзя собрать release-AAB.
  - Создать `release.keystore` через Android Studio → Build → Generate Signed Bundle.
  - Положить пароль в `local.properties` (`KEYSTORE_PASSWORD=...`, `KEY_PASSWORD=...`, `KEY_ALIAS=...`).
  - Добавить блок `signingConfigs.release { ... }` в `build.gradle.kts` и `signingConfig = signingConfigs.getByName("release")` в `buildTypes.release`.
- [x] **2.2. API-ключ Gemini в plain-text BuildConfig** — _Worker готов в `backend/cloudflare-worker/`._
  - Worker (Cloudflare) с проверкой Firebase ID-token уже написан и готов к деплою.
  - **Что осталось пользователю**: выполнить инструкции в `backend/cloudflare-worker/README.md` (5 минут) + переключить `AiChatRepository.kt` на новый URL после деплоя.
- [ ] **2.3. Имя приложения несогласовано** — в `AndroidManifest.xml:18,27` стоит `ESPEAK`, в `strings.xml` — `HablaRu`. На Play и иконке будут разные надписи.
  - Решить финальное название.
  - Заменить на `@string/app_name` везде в манифесте.
- [ ] **2.4. Privacy Policy** — обязательно для Google Play, особенно из-за RECORD_AUDIO, INTERNET, Firebase Auth, аналитики.
  - Создать страницу (например, GitHub Pages / Notion-публичная).
  - Добавить ссылку в `SettingsScreen` и в Play Console.
- [ ] **2.5. Premium-логика отключена** — A2/B1/B2 курсы кликабельны, `isLocked = false` хардкод в:
  - `HomeScreen.kt:205, 219, 715`
  - `HomeViewModel.kt:105`
  - `LevelSelectionScreen.kt:37, 87, 116, 147, 174` (5 TODO)
  - Решение: либо подключить реальную проверку премиума (биллинг), либо удалить весь premium-код и оставить всё бесплатным.
- [ ] **2.6. Лишний permission CAMERA** в `AndroidManifest.xml`. Не используется в коде.
  - Удалить, иначе Google Play потребует обоснование «зачем камера».
- [ ] **2.7. `fallbackToDestructiveMigration()`** в `AppModule.kt:38` — на проде потеря всех данных пользователя при ошибке миграции.
  - Убрать. Проверить, что все 10 миграций (1→2…10→11) корректны.
- [ ] **2.8. Заглушки для `dialogue/{id}` и `grammar/{id}`** (`Navigation.kt:247, 254`) — кликаешь по диалогу/уроку грамматики и видишь «🚧 dialogue/{id}».
  - Реализовать детальные экраны (см. §3.1, §3.2).

---

## 3. 🟡 Серьёзные проблемы и пробелы (некритично, но желательно до релиза)

### 3.1. Диалоги
- [ ] **DialogueDetailScreen** — детальный экран реплик с TTS (сейчас 🚧).
- [ ] Расширить с 15 до 50+ ситуационных диалогов (рестораны, аэропорт, врач, аренда, такси и т.д.).

### 3.2. Грамматика
- [ ] **GrammarLessonScreen** — детальный урок (сейчас 🚧).
- [ ] Грамматика A2/B1/B2 — сейчас в `GrammarContent.kt` всего 9 уроков, в основном A1.
  - Минимум: 5 уроков на каждый уровень = 20+ всего.

### 3.3. Listening-игра — мёртвый груз
- [ ] В assets лежат **1089 mp3 + sentences.json** (~15 МБ APK), но кода `Listening*Screen` нет, в `GamesScreen.GAMES` нет.
  - Решение А: вернуть игру (восстановить из git history `git log --diff-filter=D -- "*Listening*"`).
  - Решение Б: удалить assets и сэкономить 15 МБ APK.

### 3.4. Анаграммы скрыты
- [ ] **AnagramsGameScreen** реализована, маршрут `game_anagrams` зарегистрирован, но НЕ показана в `GamesScreen.GAMES` (Navigation.kt доступ только по deeplink).
  - Добавить в список игр или удалить из Navigation.

### 3.5. Расхождение CLAUDE.md и реальности
- [ ] **AI-чат использует Gemini 1.5 Flash, не Claude** (`AiChatRepository.kt:25`). В CLAUDE.md написано `claude-sonnet-4-20250514`.
- [ ] **22 блока уроков**, не 60 микро-уроков как в CLAUDE.md.
- [ ] **50 рассказов Libros**, не 25.
- [ ] **~159 глаголов спряжения**, не 20.
- [ ] **AppDatabase version=11**, не 9.
  - Обновить CLAUDE.md.

### 3.6. RatingUpdater подключён только в 2 из 8+ игр
- [ ] В `RatingUpdater.applyAnswer` зовут только `FlashcardsViewModel` и `LibrosViewModel`. Остальные не дают XP/skill rating:
  - SpeedGameViewModel
  - ArticleGameViewModel
  - CrosswordGameViewModel
  - AnagramsGameViewModel
  - MathGameViewModel
  - SopaGameViewModel
  - PalabraMaestraViewModel
  - VerbTrainingViewModel (= ConjugationQuiz)
  - QuizViewModel
  - PronunciationViewModel
  - LessonSessionViewModel (упражнения в уроках)
  - Это значит лиги растут только от карточек и рассказов.

### 3.7. Безопасность и хранение
- [ ] `android:allowBackup="true"` + пустые `data_extraction_rules.xml` / `backup_rules.xml` — auto-backup всех данных, в т.ч. Firebase Auth токенов.
  - Прописать конкретные правила или выключить.
- [ ] **Нет ProGuard/R8 правил** (`proguard-rules.pro` пустой), `isMinifyEnabled = false` для release.
  - Включить minify, добавить `-keep` для Room entities, Hilt, Firebase, kotlinx-serialization, Gemini API parsing.
  - Это сократит APK на 30–50%.

### 3.8. Локализация (опционально, но важно для рынка)
- [ ] **Только русский UI**, всё захардкожено в Compose (нет `stringResource(R.string...)`).
  - Если хотите выйти на 10 языков (RU/EN/UK/IT/KO/DE/PT/TR/FR/JA) — нужен системный рефакторинг: вынести все строки в `res/values-*/strings.xml`.
  - Это ~3–5 дней работы + перевод.

### 3.9. Тулинг
- [ ] **Двойной импорт `ui.games.*`** в `Navigation.kt:29, 34` (мелкое предупреждение).
- [ ] **Несоответствие `targetSdk=35` и `tools:targetApi="34"`** в манифесте — некритично.

---

## 4. 🟢 Контент — расширение (после релиза или паралельно)

### 4.1. Словарь
- [ ] Цель 10 000 слов (`VOCAB_TARGET = 10000` в `DatabaseSeeder.kt:26`). Сейчас ~1400 в JSON + большой CleanVocab/VocabExtra. Проверить реальный итог: запустить приложение, посмотреть `WordDao.count()`.

### 4.2. Спряжения
- [ ] Топ-300 / топ-500 глаголов (сейчас ~159). 6 времён покрыто.
- [ ] Добавить Pluscuamperfecto, Subjuntivo Imperfecto/Perfecto.

### 4.3. Рассказы Libros
- [ ] Сейчас 50 (все A1?). Добавить A2, B1, B2 — цель 100+.

### 4.4. Уроки
- [ ] 22 блока — это уже много, но проверить покрытие тем и баланс A1/A2/B1/B2.

### 4.5. Картинки слов
- [ ] 150 PNG в `assets/word_images/`. Цель — иметь иконку для всех 10 000 слов, иначе бесполезно.
  - Варианты: купить пакет Flaticon/Noun Project / AI-генерация / Wikipedia-Coil / убрать визуал.

---

## 5. 🟢 Полировка (UX и второстепенные)

- [ ] **Splash screen** анимация (есть `androidx.splashscreen` — настроен ли реально?).
- [ ] **Тумблер звука** (глобальный on/off TTS в Settings).
- [ ] **Авто-тема** по времени суток (сейчас ручное).
- [ ] **Поделиться приложением** (Intent.ACTION_SEND).
- [ ] **Smart-уведомления** — не только в 19:00, а адаптивно под время занятий пользователя.
- [ ] **Конспекты/заметки к урокам** (пользователь пишет свои заметки).
- [ ] **Импорт слов из Anki / CSV**.
- [ ] **Экспорт прогресса в PDF/CSV**.
- [ ] **Режим «экзамен»** — симуляция DELE A1/A2/B1/B2.
- [ ] **Режим для детей** (упрощённый интерфейс).

---

## 6. 💡 Идеи на потом

- [ ] **Голосовой диалог с ИИ** (STT → Claude/Gemini → TTS полностью).
- [ ] **Подкасты/видео с субтитрами** (A1→B2, интерактивные).
- [ ] **Облачная синхронизация** прогресса между устройствами (Firestore).
- [ ] **Лиги между друзьями** (приватные комнаты лидерборда).
- [ ] **Офлайн TTS** через предзагруженные mp3.
- [ ] **Яндекс SpeechKit** — живые испанские голоса вместо Android TTS.

---

## 7. 📋 Готовность к Google Play — чек-лист

| Пункт | Статус | Примечание |
|---|---|---|
| 64-bit | ✅ | Pure Kotlin, без NDK |
| App Bundle (AAB) | ✅ | Дефолт Gradle Plugin |
| min SDK 26 / target 35 | ✅ | |
| `applicationId = com.spanishapp` | ✅ | |
| `versionCode=5 / versionName=1.4` | ✅ | Обновить перед загрузкой |
| **Signing config** | 🔴 | **ОТСУТСТВУЕТ — БЛОКАТОР** |
| **Имя приложения** | 🔴 | ESPEAK vs HablaRu — конфликт |
| **Иконка** | 🟡 | adaptive icon есть, проверить визуал |
| **Privacy Policy** | 🔴 | Нет страницы и ссылки |
| **API-ключи в APK** | 🔴 | Gemini ключ в plain-text |
| **Permissions** | 🟡 | CAMERA лишний |
| **`allowBackup`** | 🟡 | true без правил |
| **ProGuard/minify** | 🟡 | Off, APK будет толстый |
| **`fallbackToDestructiveMigration`** | 🔴 | Потеря данных на проде |
| **Firebase Console: Firestore Rules** | 🟡 | Нужно настроить вручную |
| **Firebase Console: Anonymous Auth включён** | 🟡 | Проверить |
| **Локализация** | 🟡 | Только русский |
| **Скриншоты + описание для Play** | 🔴 | Нет |
| **Тестирование на реальных устройствах** | 🟡 | Нужен полный прогон |

---

## 8. 📝 Заметки

- Ветка: `master` (она же `SaveGitHub` — для бэкапа). Текущая рабочая: `claude/eager-shaw-a2c9d7`.
- Архитектура: MVVM + Hilt + Room (v=11) + Compose + Firebase.
- minSdk: 26, targetSdk: 35.
- API-ключи: `local.properties` → `ANTHROPIC_KEY`, `GEMINI_KEY` (используется только Gemini).
- Firebase project: `spanishapp-35092` (`google-services.json` есть).
- Стабильный тег: `stable-2026-05-04`.

---

## 9. 🎯 Рекомендуемый порядок действий до релиза

**Неделя 1 — Блокаторы Google Play (§2):**
1. Решить финальное имя приложения, привести в порядок strings.xml/манифест.
2. Сгенерировать release.keystore + настроить signingConfig.
3. Удалить permission CAMERA.
4. Убрать `fallbackToDestructiveMigration` (или оставить только для debug).
5. Создать Privacy Policy (любой простой шаблон) + ссылка в Settings.
6. Решить судьбу A2/B1/B2 курсов: либо подключить biling/премиум, либо удалить premium-код.

**Неделя 2 — Безопасность (§3.7) и заглушки (§2.8):**
7. Поднять Cloud Function / Cloudflare Worker как прокси для Gemini, ключ убрать из APK.
8. Настроить Firebase App Check.
9. Реализовать DialogueDetailScreen и GrammarLessonScreen.
10. Настроить ProGuard правила, включить minify в release.

**Неделя 3 — Чистка и контент (§3, §4):**
11. Убрать или включить Listening-игру (1089 mp3 = 15 МБ).
12. Добавить Anagrams в GamesScreen.
13. Подключить RatingUpdater во все ViewModels игр.
14. Настроить Firestore Rules в Console.
15. Доработать `data_extraction_rules.xml` или `allowBackup=false`.

**Неделя 4 — Релиз:**
16. Скриншоты + описание для Play (короткое + полное).
17. Иконка и feature graphic.
18. Тестовый трек (Internal testing) → Closed → Production.
19. Обновить CLAUDE.md под реальное состояние.
