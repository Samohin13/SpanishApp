# 📱 ESPEAK — карта всех экранов (текущее состояние)

> Обновлено: 2026-05-08 после серии полировки.
> Всего экранов: **43** (после удаления Anagrams + старого OnboardingScreen).
>
> **Условные обозначения:**
> - ✅ — Полностью готов (код полный, нет TODO, тёмная тема, все toggles работают).
> - ⚠️ — Работает, но есть незакрытые мелочи или дефицит контента.
> - ❓ — Зависит от внешних факторов (Firebase Console, Worker, голоса на устройстве).

---

## 🔐 Аутентификация и онбординг

| Статус | Экран | Маршрут |
|:---:|---|---|
| ✅ | **WelcomeScreen** — ¡Hola! + 2 кнопки + Google + footer Privacy | `welcome` |
| ✅ | **RegisterScreen** — email/пароль + confirm + indicator + terms checkbox | `register` |
| ✅ | **LoginScreen** — clearErrors на edit, общий Google | `login` |
| ✅ | **ForgotPasswordScreen** — Snackbar + auto back | `forgot_password` |
| ✅ | **NameEntryScreen** — валидация AuthValidator + лимит 20 + counter | `name_entry` |
| ✅ | **AgeSelectionScreen** — slider + GDPR-плашка для < 13 | `age_selection` |
| ✅ | **ReasonSelectionScreen** — verticalScroll | `reason_selection` |
| ✅ | **KnowledgeCheckScreen** — TopAppBar + динамический счётчик вопросов | `knowledge_check` |
| ✅ | **PlacementTestScreen + PlacementResult** — TopAppBar + abort dialog + счётчик ✓ | `placement_test` / `placement_result/{level}` |
| ✅ | **LevelSelectionScreen** — без фейковых цен, честный AlertDialog «скоро» | `level_selection` |
| ✅ | **AppLockScreen** — биометрический замок (отпечаток / Face ID) | `app_lock` |

---

## 🏠 Главная и уроки

| Статус | Экран | Маршрут |
|:---:|---|---|
| ✅ | **HomeScreen** — 4 курса + word of day с TTS примера + streak | `home` |
| ✅ | **CourseDetailScreen** — список юнитов курса | `course_detail/{level}` |
| ✅ | **LessonIntroScreen** — превью урока + tonal indicator | `lesson_intro/{u}/{l}` |
| ✅ | **LessonContentScreen** — теория | `lesson_content/{u}/{l}` |
| ✅ | **LessonSessionScreen** — упражнения + speaking + RatingUpdater подключён | `lesson_session/{u}/{l}` |

---

## 🎮 Игры (8)

| Статус | Экран | Маршрут |
|:---:|---|---|
| ✅ | **GamesScreen** — хаб 8 игр | `games` |
| ✅ | **ArticlesGameScreen** — Artículos, 100 уровней | `game_articles` |
| ✅ | **SpeedGameScreen** — Rápido | `game_speed` |
| ✅ | **MathGameScreen** — Cálculo | `game_math` |
| ✅ | **CrosswordGameScreen** — Crucigrama, 100 уровней + zoom + испан. клавиатура | `game_crossword` |
| ✅ | **SopaGameScreen** — Sopa de Letras | `game_sopa` |
| ✅ | **PalabraMaestraScreen** — орфография | `game_palabra` |
| ✅ | **VerbTrainingScreen** — спряжения | `conjugation_quiz` |
| ⚠️ | **LibrosScreen + LibroReadScreen** — 50 рассказов (все A1, нужно A2/B1/B2) | `game_libros`, `libro/{id}` |
| ✅ | **LevelMapScreen** (общий компонент) | (используется внутри игр) |

LibroReadScreen теперь имеет **Gemini-fallback** для редких слов вне БД.

---

## 📚 Карточки и спряжения

| Статус | Экран | Маршрут |
|:---:|---|---|
| ✅ | **FlashcardsSetupScreen** — настройка сессии | `flashcards?type=&level=` |
| ✅ | **FlashcardsScreen** — flip + SM-2 + **swipe-жесты** (←/↑/→) + TTS | `flashcards_session?...` |
| ✅ | **ConjugationScreen** — фильтр Все/Правильные/Неправильные + ⚡ маркер | `conjugation?verb=` |

---

## 🎓 Грамматика и диалоги

| Статус | Экран | Маршрут |
|:---:|---|---|
| ⚠️ | **GrammarScreen** — список с inline (только 9 уроков, A2/B1/B2 пусто) | `grammar` |
| ⚠️ | **DialoguesScreen** — 15 диалогов с TTS (мало) | `dialogues` |

---

## 💬 AI-чат

| Статус | Экран | Маршрут |
|:---:|---|---|
| ⚠️❓ | **AiChatScreen** — Gemini + **карточки исправлений** (✏ original → corrected → объяснение) | `ai_chat` |

⚠️ — пока ключ Gemini в plain-text APK (для production: задеплоить Cloudflare Worker, см. `backend/cloudflare-worker/`).

---

## 📖 Словарь и тесты

| Статус | Экран | Маршрут |
|:---:|---|---|
| ✅ | **DictionaryScreen** — поиск, фильтры, пользовательские списки | `dictionary` |
| ✅ | **WeakWordsScreen** — слова с точностью < 60% | `weak_words` |
| ✅ | **QuizScreen** — 10 вопросов + RatingUpdater | `quiz?type=` |

---

## 🎙️ Произношение

| Статус | Экран | Маршрут |
|:---:|---|---|
| ⚠️❓ | **PronunciationScreen** — TTS + STT + score (примитивный, не по фонемам) | `pronunciation` |

---

## 👤 Профиль и рейтинг

| Статус | Экран | Маршрут |
|:---:|---|---|
| ✅ | **ProfileScreen** — **живой график XP по дням** + 3 карточки рейтинга | `profile` |
| ✅ | **AchievementsScreen** — 17 ачивок + анимация появления + **NEW** badge для свежих | `achievements` |
| ✅ | **RatingScreen** — 58 категорий с флагами 0–5 | `rating_full` |
| ❓ | **LeaderboardScreen** — Firestore топ-100 (нужны Anonymous Auth + Rules в Firebase Console) | `leaderboard` |

---

## ⚙️ Настройки

| Статус | Экран | Маршрут |
|:---:|---|---|
| ✅ | **SettingsScreen** — все toggles реально работают (TTS, sound, vibration, тема, биометрия) + время уведомления | `settings` |
| ✅❓ | **SettingsVoiceScreen** — выбор TTS-голоса (зависит от установленных на устройстве) | `settings_voice` |

---

# 📊 Сводка

| Категория | Кол-во |
|---|---:|
| ✅ **Готово полностью** | **35 экранов** |
| ⚠️ **Контент / частичные** | **5 экранов** |
| ❓ **Зависит от внешних настроек** | **3 экрана** |
| 🔴 **Сломано/мёртвый код** | **0 экранов** |
| **ВСЕГО** | **43 экрана** |

---

# 🎯 Что осталось закрыть для 100%

## ⚠️ Контент (не код)
- [ ] **Грамматика A2/B1/B2** — расписать минимум 20 уроков (сейчас 9, в основном A1).
- [ ] **Диалоги** — расширить 15 → 50 ситуационных.
- [ ] **Libros A2/B1/B2** — все 50 рассказов A1.
- [ ] **PlacementTest** — расширить пул вопросов (есть только 8).

## ❓ Внешние настройки (10–30 минут каждое)
- [ ] **Firebase Console** — включить Anonymous Auth + опубликовать Firestore Rules (`backend/firestore.rules`).
- [ ] **Cloudflare Worker** — задеплоить (`backend/cloudflare-worker/`) + переключить AiChat на новый URL (текущий plain-text Gemini ключ в APK).
- [ ] **Privacy Policy** — опубликовать `PRIVACY_POLICY.md` на GitHub Pages (`docs/PUBLISH_PRIVACY_POLICY.md`) + обновить URL в `SettingsScreen`.

## 🟢 Идеи к улучшению (после релиза)
- Pronunciation Assessment через Azure (платно, $1/час).
- Gemini-фолбэк для AI-чата на свой backend (через Cloudflare Worker).
- Smart-уведомления (адаптивное время, не статичное).
- Расширение биометрии до 7 экранов с haptic — сделано в этой сессии ✅.

---

# ✅ Что точно готово к релизу

После 20+ коммитов сегодня закрыто:
- 35 ✅ экранов корректно работают на свет/тёмной теме
- AppLock биометрия / TTS / Sound / Vibration / Тема / Время уведомления — все toggles реальны
- AI-чат с карточками исправлений
- Libros с Gemini-fallback для редких слов
- Анимации + swipe-жесты в карточках + графики XP
- Подписанный AAB 31 МБ, debug APK 15.6 МБ
- Тёмная тема на 20+ экранах

Оставшееся блокирует релиз только косвенно: контент можно расширять патчами, внешние настройки — 30 минут работы в Firebase/Cloudflare Console.
