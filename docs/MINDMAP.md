# SpanishApp / ESPEAK — карта приложения

> Mermaid mindmap. Откроется автоматически в:
> GitHub, GitLab, Notion, VS Code (плагин Markdown Preview Mermaid Support),
> Obsidian, Cursor, Typora, любой современный markdown-просмотрщик.

## Полная структура

```mermaid
mindmap
  root((ESPEAK))
    Auth и онбординг
      Welcome
      Register
      Login
      ForgotPassword
      Онбординг
        NameEntry
        AgeSelection
        ReasonSelection
        KnowledgeCheck
        PlacementTest
        LevelSelection
    Главная
      HomeScreen
        Стрик
        XP-bar
        Слово дня
        4 курса A1-B2
      CourseDetail
      Уроки
        LessonIntro
        LessonContent
        LessonSession
          Теория
          Упражнения
          Speaking STT
    Игры
      Хаб GamesScreen
      Articulos 100 уровней
      Rapido
      Anagramas
      Calculo
      Crucigrama 100 уровней
      Sopa de Letras
      Palabra Maestra
      Verbos Conjugaciones
      Libros 50 рассказов
        Список
        Чтение
        Тест
    Карточки
      Setup
      SM-2 сессия
        Hard Good Easy
        Flip-анимация
        TTS
    Словарь
      Dictionary поиск
      WeakWords
      Quiz
    Грамматика
      9 уроков
      Inline-раскрытие
    Диалоги
      15 ситуаций
      TTS реплики
    Произношение
      TTS эталон
      STT юзер
      Score
    AI чат
      Gemini 1.5 Flash
      История 20 msgs
      CORRECTIONS_JSON
      Grammar check
    Профиль
      ProfileScreen
        Skill Rating
        Лига
        Прогресс по темам
      AchievementsScreen 17
      RatingScreen 58 категорий
      LeaderboardScreen
        Своя страна
        Мир
    Настройки
      Settings
      SettingsVoice
        8 персонажей
        Скорость
        Тон
    Сервисы фон
      DailyReminder 19:00
      RatingDecay
      WordOfDayWidget
```

---

## Только готовые экраны (зелёные ✅)

```mermaid
mindmap
  root((Готово 27 экранов))
    Auth
      Welcome
      Login
      ForgotPassword
      NameEntry
      KnowledgeCheck
      PlacementTest
    Игры
      GamesScreen
      Articulos
      Rapido
      Anagramas
      Calculo
      Crucigrama
      Sopa
      Palabra
      LibroRead
    Карточки
      FlashcardsSetup
      Flashcards
    Conjugation
    Словарь
      Dictionary
      WeakWords
    Профиль
      ProfileScreen
      AchievementsScreen
      RatingScreen
    Уроки
      LessonIntro
```

---

## Архитектура данных

```mermaid
mindmap
  root((Данные))
    Локально Room v=11
      WordEntity 1400+ слов
      ConjugationEntity 159 глаголов
      LessonEntity 22 блока
      DialogueEntity 15
      UserProgressEntity
        skillRating 1000
        currentLeague
        peakRating
      ChatMessageEntity
      AchievementEntity 17
      DailyWordEntity
      WordList пользовательские
      ArticleLevelProgress
      LessonProgress
      LibroProgress
      GameLevelProgress
    Firebase
      Auth
        Email password
        Google Sign-In
        Anonymous
      Firestore
        leaderboard uid
        users uid
      Crashlytics
      Analytics
    Внешние API
      Gemini 1.5 Flash
      Google AI Studio ключ
      Cloudflare Worker proxy
    Assets
      spanish_vocab.json 1400
      articles_levels.json
      word_images 150 WebP
      Sound effects
```

---

## Roadmap до релиза

```mermaid
mindmap
  root((До релиза))
    Готово
      Сборка APK 15.6MB
      Сборка AAB 31MB
      Подпись V2
      ProGuard rules
      Privacy Policy шаблон
      Cloudflare Worker готов
      Firestore Rules
      Сжатие images 99 процентов
    Сделать тебе
      Тест на телефоне
      Опубликовать Privacy на GitHub Pages
      Firebase Console
        Anonymous Auth
        Firestore Rules paste
      Cloudflare Worker
        wrangler login
        secrets set
        deploy
      Переключить AiChat на Worker URL
      Google Play Console
        Создать аккаунт 25 USD
        Скриншоты
        Store listing
        Upload AAB
    Можно после релиза
      Грамматика A2 B1 B2
      Диалоги 50
      Libros A2 B1 B2
      Локализация EN UK
      Премиум подписка
      Push notifications smart
```

---

## Если нужен .xmind файл

Скажи — сгенерирую и положу в `docs/ESPEAK.xmind`.
Также могу выгрузить в:
- `.mm` (FreeMind/XMind import)
- PNG через mermaid-cli
- Интерактивный HTML через markmap-cli
