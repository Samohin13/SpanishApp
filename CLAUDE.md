# SpanishApp — Android приложение для изучения испанского языка

> Этот файл — **живая память проекта**. Обновляется каждые 30–60 минут работы.
> Не перезаписывать целиком, а структурированно дополнять.
> Последнее обновление: **2026-04-30, сессия 7 (Libros + перевод по зажатию)**

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
- **Room version = 7**, миграции 1→7 все прописаны
- **Listening game 🎧**: 1089 предложений Tatoeba
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

**Следующие задачи (roadmap):**
1. **Streak / Home**: счётчик серии на главном экране
2. **Word of Day**: слово дня с озвучкой на HomeScreen
3. **WeakWords**: экран слабых слов (данные уже в DAO)
4. **AI Chat**: живой экран чата с Claude
5. **Libros A2/B1/B2**: добавить рассказы 26–100 (сейчас только 25 × A1)

---

## 1. Обзор проекта
Полнофункциональное Android-приложение на Kotlin + Jetpack Compose для изучения испанского языка русскоязычными пользователями. Включает ИИ-репетитора на базе Claude API.

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
- **ИИ:** Anthropic Claude API (`claude-sonnet-4-20250514`)
- **Фоновые задачи:** WorkManager
- **Виджет:** Glance AppWidget
- **HTTP:** OkHttp
- **Хранилище настроек:** DataStore (будет использован для SettingsVoice)
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
│       ├── AiChatRepository.kt             — Claude API + история чата
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

## 7. ИИ-репетитор (Claude API)
- Модель: `claude-sonnet-4-20250514`
- Системный промпт: отвечает на испанском, переводит трудные слова в [скобках], исправляет ошибки
- История: последние 20 сообщений из Room
- Проверка грамматики: отдельный endpoint → JSON с исправлениями
- API ключ: `local.properties` → `ANTHROPIC_KEY=sk-ant-...`

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
3. **Память**: `C:\Users\bravo\.claude\projects\C--Users-bravo-AndroidStudioProjects-SpanishApp2\memory\`:
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
