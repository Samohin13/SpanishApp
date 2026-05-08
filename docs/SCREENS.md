# 📱 SpanishApp / ESPEAK — карта всех экранов

> Документ создан 2026-05-08 после полного аудита кода.
>
> **Условные обозначения:**
> - ✅ — Код полный, нет TODO, все зависимости подключены, должен работать.
> - ⚠️ — Работает, но есть незакрытые TODO или мелкие недочёты.
> - 🔴 — Сломан, заглушка, дубликат или мёртвый код.
> - ❓ — Зависит от внешних факторов (настройки Firebase Console, наличие интернета, голосов на устройстве).
>
> ⚠️ **Важно:** галочки выставлены по статическому анализу кода. Реальное поведение на устройстве не тестировалось.

---

## 🔐 Аутентификация и онбординг

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ✅ | **WelcomeScreen** — ¡Hola! + 3 кнопки входа | `welcome` | [WelcomeScreen.kt](../app/src/main/java/com/spanishapp/ui/auth/WelcomeScreen.kt) |
| ⚠️ | **RegisterScreen** — email/пароль (нет валидации сложности пароля, нет confirm-поля) | `register` | [RegisterScreen.kt](../app/src/main/java/com/spanishapp/ui/auth/RegisterScreen.kt) |
| ✅ | **LoginScreen** — вход email/пароль + ссылка на «забыли» | `login` | [LoginScreen.kt](../app/src/main/java/com/spanishapp/ui/auth/LoginScreen.kt) |
| ✅ | **ForgotPasswordScreen** — отправка ссылки на email | `forgot_password` | [ForgotPasswordScreen.kt](../app/src/main/java/com/spanishapp/ui/auth/ForgotPasswordScreen.kt) |
| ✅ | **NameEntryScreen** — ввод имени | `name_entry` | [OnboardingScreens.kt](../app/src/main/java/com/spanishapp/ui/auth/OnboardingScreens.kt) |
| ⚠️ | **AgeSelectionScreen** — возраст (записывается, но нигде не используется) | `age_selection` | [OnboardingScreens.kt](../app/src/main/java/com/spanishapp/ui/auth/OnboardingScreens.kt) |
| ⚠️ | **ReasonSelectionScreen** — причина (записывается, но нигде не используется) | `reason_selection` | [OnboardingScreens.kt](../app/src/main/java/com/spanishapp/ui/auth/OnboardingScreens.kt) |
| ✅ | **KnowledgeCheckScreen** — учил ли раньше | `knowledge_check` | [OnboardingScreens.kt](../app/src/main/java/com/spanishapp/ui/auth/OnboardingScreens.kt) |
| ✅ | **PlacementTestScreen + PlacementResult** — тест на уровень | `placement_test` / `placement_result/{level}` | [PlacementTestScreen.kt](../app/src/main/java/com/spanishapp/ui/auth/PlacementTestScreen.kt) |
| ⚠️ | **LevelSelectionScreen** — ручной выбор уровня (5 TODO про премиум, 🚧 placeholder для платного) | `level_selection` | [LevelSelectionScreen.kt](../app/src/main/java/com/spanishapp/ui/auth/LevelSelectionScreen.kt) |
| 🔴 | **OnboardingScreen** (старый, отдельный файл) — **мёртвый код** ~500 строк, никто не навигирует | `onboarding` (зарегистрирован, не вызывается) | [OnboardingScreen.kt](../app/src/main/java/com/spanishapp/ui/onboarding/OnboardingScreen.kt) |

---

## 🏠 Главная и уроки

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ⚠️ | **HomeScreen** — дашборд (3 TODO про премиум, A2/B1/B2 кликабельны но `unitsCount=0`) | `home` | [HomeScreen.kt](../app/src/main/java/com/spanishapp/ui/home/HomeScreen.kt) |
| ⚠️ | **CourseDetailScreen** — список юнитов курса (для A2/B1/B2 контент почти пустой) | `course_detail/{level}` | [CourseDetailScreen.kt](../app/src/main/java/com/spanishapp/ui/home/CourseDetailScreen.kt) |
| ✅ | **LessonIntroScreen** — превью урока перед началом | `lesson_intro/{u}/{l}` | [LessonIntroScreen.kt](../app/src/main/java/com/spanishapp/ui/home/LessonIntroScreen.kt) |
| ⚠️ | **LessonContentScreen** — теория (🚧 для уроков без контента) | `lesson_content/{u}/{l}` | [LessonContentScreen.kt](../app/src/main/java/com/spanishapp/ui/home/LessonContentScreen.kt) |
| ⚠️ | **LessonSessionScreen** — упражнения + speaking (RatingUpdater не подключён, skillRating не растёт) | `lesson_session/{u}/{l}` | [LessonSessionScreen.kt](../app/src/main/java/com/spanishapp/ui/home/LessonSessionScreen.kt) |

---

## 🎮 Игры

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ✅ | **GamesScreen** — хаб 9 игр | `games` | [GamesScreen.kt](../app/src/main/java/com/spanishapp/ui/games/GamesScreen.kt) |
| ✅ | **ArticlesGameScreen** — Artículos, 100 уровней | `game_articles` | [ArticlesGameScreen.kt](../app/src/main/java/com/spanishapp/ui/games/ArticlesGameScreen.kt) |
| ✅ | **SpeedGameScreen** — Rápido | `game_speed` | [SpeedGameScreen.kt](../app/src/main/java/com/spanishapp/ui/games/SpeedGameScreen.kt) |
| ✅ | **AnagramsGameScreen** — Anagramas | `game_anagrams` | [AnagramsGameScreen.kt](../app/src/main/java/com/spanishapp/ui/games/AnagramsGameScreen.kt) |
| ✅ | **MathGameScreen** — Cálculo | `game_math` | [MathGameScreen.kt](../app/src/main/java/com/spanishapp/ui/games/MathGameScreen.kt) |
| ✅ | **CrosswordGameScreen** — Crucigrama, 100 уровней | `game_crossword` | [CrosswordGameScreen.kt](../app/src/main/java/com/spanishapp/ui/games/CrosswordGameScreen.kt) |
| ✅ | **SopaGameScreen** — Sopa de Letras | `game_sopa` | [SopaGameScreen.kt](../app/src/main/java/com/spanishapp/ui/games/SopaGameScreen.kt) |
| ✅ | **PalabraMaestraScreen** — орфография | `game_palabra` | [PalabraMaestraScreen.kt](../app/src/main/java/com/spanishapp/ui/games/PalabraMaestraScreen.kt) |
| 🔴 | **VerbTrainingScreen** — спряжения **(дубликат ConjugationQuizScreen)** | `conjugation_quiz` | [VerbTrainingScreen.kt](../app/src/main/java/com/spanishapp/ui/games/VerbTrainingScreen.kt) |
| ⚠️ | **LibrosScreen** — список рассказов (все 50 на A1, фильтры A2/B1/B2 покажут пусто) | `game_libros` | [LibrosScreen.kt](../app/src/main/java/com/spanishapp/ui/games/LibrosScreen.kt) |
| ✅ | **LibroReadScreen** — чтение + тест + перевод по long-press | `libro/{id}` | [LibroReadScreen.kt](../app/src/main/java/com/spanishapp/ui/games/LibroReadScreen.kt) |
| ✅ | **LevelMapScreen** (общий компонент) — карта уровней игр | (используется в Articles, Crossword) | [LevelMapScreen.kt](../app/src/main/java/com/spanishapp/ui/games/common/LevelMapScreen.kt) |

---

## 📚 Карточки и спряжения

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ✅ | **FlashcardsSetupScreen** — настройка сессии | `flashcards?type=&level=` | [FlashcardsSetupScreen.kt](../app/src/main/java/com/spanishapp/ui/flashcards/FlashcardsSetupScreen.kt) |
| ✅ | **FlashcardsScreen** — flip-карточка с SM-2 + TTS | `flashcards_session?level=&category=&direction=` | [FlashcardsScreen.kt](../app/src/main/java/com/spanishapp/ui/flashcards/FlashcardsScreen.kt) |
| ✅ | **ConjugationScreen** — таблицы спряжения ~159 глаголов | `conjugation?verb=` | [ConjugationScreen.kt](../app/src/main/java/com/spanishapp/ui/conjugation/ConjugationScreen.kt) |
| 🔴 | **ConjugationQuizScreen** — **дубликат VerbTrainingScreen**, не используется | (нигде не зарегистрирован) | [ConjugationQuizScreen.kt](../app/src/main/java/com/spanishapp/ui/conjugation/ConjugationQuizScreen.kt) |

---

## 🎓 Грамматика и диалоги

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ⚠️ | **GrammarScreen** — список с inline-раскрытием (только 9 уроков, A2/B1/B2 показывают «🚧 скоро появятся») | `grammar` | [GrammarScreen.kt](../app/src/main/java/com/spanishapp/ui/grammar/GrammarScreen.kt) |
| ⚠️ | **DialoguesScreen** — 15 диалогов с TTS (мало диалогов) | `dialogues` | [DialoguesScreen.kt](../app/src/main/java/com/spanishapp/ui/dialogues/DialoguesScreen.kt) |

---

## 💬 AI-чат

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ⚠️❓ | **AiChatScreen** — Gemini-репетитор (нужен Worker для prod, CORRECTIONS_JSON парсится но не показывается красиво) | `ai_chat` | [AiChatScreen.kt](../app/src/main/java/com/spanishapp/ui/chat/AiChatScreen.kt) |

---

## 📖 Словарь и тесты

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ✅ | **DictionaryScreen** — поиск, фильтры, пользовательские списки | `dictionary` | [DictionaryScreen.kt](../app/src/main/java/com/spanishapp/ui/dictionary/DictionaryScreen.kt) |
| ✅ | **WeakWordsScreen** — слова с точностью < 60% | `weak_words` | [WeakWordsScreen.kt](../app/src/main/java/com/spanishapp/ui/dictionary/WeakWordsScreen.kt) |
| ⚠️ | **QuizScreen** — 10 вопросов (RatingUpdater не подключён) | `quiz?type=` | [QuizScreen.kt](../app/src/main/java/com/spanishapp/ui/quiz/QuizScreen.kt) |

---

## 🎙️ Произношение

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ⚠️❓ | **PronunciationScreen** — TTS + STT + score (примитивный скоринг по строкам, не по фонемам) | `pronunciation` | [PronunciationScreen.kt](../app/src/main/java/com/spanishapp/ui/pronunciation/PronunciationScreen.kt) |

---

## 👤 Профиль и рейтинг

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ✅ | **ProfileScreen** — главная карточка юзера, 3 карточки рейтинга | `profile` | [ProfileScreen.kt](../app/src/main/java/com/spanishapp/ui/profile/ProfileScreen.kt) |
| ✅ | **AchievementsScreen** — 17 ачивок | `achievements` | [AchievementsScreen.kt](../app/src/main/java/com/spanishapp/ui/profile/AchievementsScreen.kt) |
| ✅ | **RatingScreen** — все 58 категорий с флагами 0–5 | `rating_full` | [RatingScreen.kt](../app/src/main/java/com/spanishapp/ui/profile/RatingScreen.kt) |
| ❓ | **LeaderboardScreen** — Firestore топ-100 (нужны Anonymous Auth + Firestore Rules в Firebase Console) | `leaderboard` | [LeaderboardScreen.kt](../app/src/main/java/com/spanishapp/ui/leaderboard/LeaderboardScreen.kt) |

---

## ⚙️ Настройки

| Статус | Экран | Маршрут | Файл |
|:---:|---|---|---|
| ⚠️ | **SettingsScreen** — хаб настроек (TODO «Экспорт данных», Privacy URL ведёт на blob GitHub) | `settings` | [SettingsScreen.kt](../app/src/main/java/com/spanishapp/ui/settings/SettingsScreen.kt) |
| ✅❓ | **SettingsVoiceScreen** — выбор голоса TTS (зависит от установленных на устройстве голосов es-ES) | `settings_voice` | [SettingsVoiceScreen.kt](../app/src/main/java/com/spanishapp/ui/settings/SettingsVoiceScreen.kt) |

---

# 📊 Сводка

| Категория | Кол-во |
|---|---:|
| ✅ **Готово полностью** (по коду) | **27 экранов** |
| ⚠️ **Есть мелкие проблемы** (TODO/контент) | **13 экранов** |
| 🔴 **Сломано/мёртвый код/дубликат** | **3 экрана** |
| ❓ **Зависит от внешних настроек** | **4 экрана** (могут пересекаться) |
| **ВСЕГО** | **44 экрана** |

---

# 🎯 Что нужно закрыть до 100% готовности

## 🔴 Удалить (быстро, безопасно)
- [ ] `OnboardingScreen.kt` (старый, мёртвый, ~500 строк)
- [ ] `ConjugationQuizScreen.kt` ИЛИ `VerbTrainingScreen.kt` (дубликат)

## ⚠️ Доделать в коде
- [ ] Подключить **RatingUpdater** в `LessonSessionScreen` и `QuizScreen`.
- [ ] Удалить **5 TODO про премиум** в `LevelSelectionScreen` (или подключить billing).
- [ ] Удалить **3 TODO про премиум** в `HomeScreen` + `HomeViewModel`.
- [ ] **CORRECTIONS_JSON** в AiChat — отрисовать красивыми карточками вместо вырезания.
- [ ] **Экспорт данных** в `SettingsScreen` — реализовать или убрать пункт.
- [ ] **Валидация пароля** в `RegisterScreen` (длина, confirm).

## ⚠️ Контент (расширить)
- [ ] Грамматика A2/B1/B2 (сейчас 9 уроков) — расписать минимум 20.
- [ ] Диалоги (сейчас 15) — расширить до 50.
- [ ] Libros на A2/B1/B2 (сейчас все 50 рассказов A1).
- [ ] PlacementTest — расширить пул вопросов.

## ❓ Внешние настройки
- [ ] Firebase Console: включить Anonymous Auth + опубликовать Firestore Rules.
- [ ] Cloudflare Worker задеплоить + переключить AiChat на Worker.
- [ ] Опубликовать Privacy Policy на GitHub Pages + обновить URL в SettingsScreen.

## 🟢 Идеи к улучшению (после релиза)
- TTS на слово дня в HomeScreen.
- Swipe-жесты в Flashcards.
- Поиск по глаголам в ConjugationScreen.
- Биометрия в LoginScreen.
- График XP по дням в ProfileScreen.
- Кликабельный перевод через Gemini-фолбэк в LibroReadScreen.

---

# ✅ Что точно готово к релизу

- 27 экранов работают в полном объёме (по коду).
- Все 9 игр функциональны и подключены к рейтингу.
- Карточки + Dictionary + WeakWords — образцово.
- Профиль + Achievements + Rating — отлично.
- Сборка release.aab подписана и весит 31 МБ (✅ в лимите Play).

После закрытия двух главных пунктов (удалить мёртвый код, подключить RatingUpdater в 2 ViewModels) — приложение будет на **30/44 ✅ зелёных** + **14 ⚠️/❓ контентных**.

Контентные не блокируют релиз — можно выкатывать v1.0 с тем что есть и расширять контент в обновлениях.
