# 🏗️ EASPEAK Компоненты и экраны — детальный гайд

## 🎯 Иерархия приложения

```
┌─────────────────────────────────────────────────────────────┐
│                    MAIN ACTIVITY                            │
│              (Navigation Host Container)                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
       ┌───────────────┼───────────────┐
       │               │               │
   ┌───▼──────┐   ┌───▼──────┐   ┌───▼──────┐
   │  Bottom  │   │  NavHost │   │ Top Bar  │
   │    Nav   │   │ (Screens)│   │ (Header) │
   └────────┬─┘   └──┬────┬──┘   └──────────┘
            │        │    │
      ┌─────┼────────┼────┼─────────────────┐
      │     │        │    │                 │
   [Home] [Games] [Tarjetas] [Dictionary] [Profile]
```

---

## 🔧 Встроенные компоненты (Components.kt)

### 1. **BottomNavigation**

```kotlin
@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit)
```

**Характеристики:**
- Высота: `62.dp` + nav-bar inset
- 5 табов: Home, Games, Tarjetas, Dictionary, Profile
- Иконки: Material Icons (Outlined ↔ Filled transition)
- Анимация: Spring(damping = Medium, stiffness = Spring.StiffnessHigh)
- Масштаб при клике: 1.08x
- Цвет активного таба: `--brand-purple`

```
┌───────────────────────────────────────────────────────┐
│  Главная   Игры   Tarjetas   Словарь   Профиль       │
│    🏠  👾     🃏     📖          👤                   │
│ (active)                                              │
└───────────────────────────────────────────────────────┘
```

### 2. **XP Progress Bar**

```kotlin
@Composable
fun XPProgressBar(currentXP: Int, nextLevelXP: Int)
```

**Характеристики:**
- Градиент: purple → pink (`--grad-xp`)
- Высота: 8.dp
- Радиус: 4.dp (скругленные концы)
- Анимация: `animateFloatAsState` (200ms tween)

```
Лев. XP: 1240   ████████░░ 75%   Сл. уровень: 1650
                └─ gradient purple→pink
```

### 3. **CourseCard**

```kotlin
@Composable
fun CourseCard(
    level: String,        // "A1", "A2", "B1", "B2"
    emoji: String,        // "🚀", "🌍", "📚", "🎓"
    isUnlocked: Boolean,
    progress: Float,      // 0..1
    onStartClick: () -> Unit
)
```

**Структура:**
```
┌─────────────────────────────────┐
│ [Gradient Header - 100.dp]      │  Course Color (A1=green, etc)
│   🚀      Блок 1                │  Emoji: 40sp
│          Испанский для начинающих │  Title + description
│          A1                       │
├─────────────────────────────────┤
│ Описание уровня...               │  Body text (13sp gray)
│                                 │
│ ████████░░░ 75% [accent]         │  Progress bar
│                                 │
│ Начать обучение →                │  CTA: 12sp bold accent
└─────────────────────────────────┘
```

**Состояния:**
- **Unlocked**: color header, enabled CTA
- **Locked**: gray gradient header, "Заблокировано" CTA, lock icon

### 4. **TopicCard**

```kotlin
@Composable
fun TopicCard(
    blockNum: Int,
    title: String,
    description: String,
    progress: Float,
    isLocked: Boolean
)
```

То же, что CourseCard, но меньше (для внутри уровня).

### 5. **FlashcardFlip**

```kotlin
@Composable
fun FlashcardFlip(spanish: String, russian: String, isFlipped: Boolean)
```

**Анимация:**
- Rotate: Spring(dampingRatio = Low, stiffness = Low)
- Duration: ~600ms flip time
- Содержит `AnimatedContent` для текста

### 6. **ReviewButtons (SM-2)**

```kotlin
@Composable
fun ReviewButtons(onReview: (ReviewType) -> Unit)
```

3 кнопки (вертикально):
- ❌ **Забыл** — вернуть в очередь
- ✅ **Помню** — нормально
- 🟢 **Легко** — увеличить интервал

**Анимация:** scale+fade reveal с `AnimatedContent`

### 7. **StatsPill**

```kotlin
@Composable
fun StatsPill(emoji: String, value: String, label: String)
```

```
┌─────────────────────┐
│  ✨  1240           │  Background: --stat-gold-bg
│      XP сегодня      │  Value: 28sp ExtraBold
└─────────────────────┘  Label: 11sp secondary
```

### 8. **LessonTypeChip**

```kotlin
@Composable
fun LessonTypeChip(type: String)  // "vocab", "grammar", "phrase", etc
```

```
┌──────────────────┐
│ 📖 Теория        │  Background: --tag-vocab-bg
│                  │  Text: --tag-vocab-fg
└──────────────────┘
```

### 9. **WordCard (Dictionary)**

```kotlin
@Composable
fun WordCard(
    word: String,
    transcription: String,
    translation: String,
    examples: List<String>,
    imageUrl: String?
)
```

- Содержит изображение (если есть)
- Примеры в expandable list
- Кнопка добавления в flashcards

### 10. **LeaderboardRow**

```kotlin
@Composable
fun LeaderboardRow(
    rank: Int,
    name: String,
    flag: String,      // "🇪🇸"
    xp: Int,
    isCurrentUser: Boolean
)
```

Стиль: light tint на текущего пользователя (`Teal.copy(.12f)`)

---

## 📱 Макеты экранов

### **HomeScreen**

```
┌─────────────────────────────────┐
│ Главная 👋                       │  Header (14dp pad)
├─────────────────────────────────┤
│                                 │
│  [Stats Pills Row]              │  ✨ 1240 XP | 🔥 12 дней
│  ✓ Сегодня занимался 30 / 60 мин│  Secondary stat
│                                 │
├─────────────────────────────────┤
│ КУРСЫ                           │  Section title (headline-md)
│                                 │
│ ┌──────────────────────────────┐│  CourseCard × 4
│ │ 🚀 A1 (Beginner)            ││
│ │ Испанский с нуля             ││
│ │ ████░░░ 25%                  ││
│ └──────────────────────────────┘│
│                                 │
│ ┌──────────────────────────────┐│
│ │ 🌍 A2 (Elementary)           ││
│ │ ... (locked)                 ││
│ └──────────────────────────────┘│
│                                 │
├─────────────────────────────────┤
│ WORD OF THE DAY                 │  Card with 🎁 emoji
│ ┌──────────────────────────────┐│
│ │ 📚 gato (кот)               ││
│ │ Frequency: 1240 / 10000      ││
│ │ [Показать перевод]           ││
│ └──────────────────────────────┘│
│                                 │
├─────────────────────────────────┤
│ PATH TO MADRID (Leaderboard)    │  Season card
│ Следующая остановка:            │
│ 🏰 Aldea perdida (Ваше место: 5)│
│ ████████░░░ 340 / 500 XP        │
│                                 │
│ [Увидеть лидерборд]             │  CTA
│                                 │
└─────────────────────────────────┘
                 │
         ┌───────┴────────┐
         │   BOTTOM NAV   │
         │ Home | ≈ | ≈ | ≈│
         └────────────────┘
```

### **OnboardingScreen**

```
┌─────────────────────────────────┐
│                                 │
│          🇪🇸                     │  Spanish flag (80sp emoji)
│                                 │
│          ¡Hola!                 │  Title (display-lg)
│                                 │
│   Добро пожаловать в ESPEAK     │  Subtitle (body-lg)
│   Приложение для изучения       │
│   испанского языка              │
│                                 │
├─────────────────────────────────┤
│ ПАГИНАЦИЯ (AnimatedContent)     │
│                                 │
│ 📝 Как тебя зовут?              │  Page 1: Ввод имени
│ [________________]              │
│                                 │
│     [← Назад] [Далее →]         │  Navigation buttons
│                                 │
└─────────────────────────────────┘
```

(На вторую страницу: выбор уровня A1/A2/B1/B2 с radio buttons)

### **FlashcardsScreen**

```
┌─────────────────────────────────┐
│ Tarjetas                         │
│                                 │
│      Карточка 3 / 20            │  Progress counter
│                                 │
├─────────────────────────────────┤
│  (Flipped animation)            │  Card body
│                                 │
│         GATO                    │  Spanish word (34sp)
│                                 │
│      ← Tap to flip →            │  Hint (gray, secondary)
│                                 │
├─────────────────────────────────┤
│  КОТ                            │  Russian translation
│                                 │
│  Толковый словарь:             │
│  Четвероногое млекопитающее...  │  Definition (body-md)
│                                 │
│  Примеры:                       │
│  - El gato es amigable          │
│                                 │
├─────────────────────────────────┤
│                                 │
│  [Забыл]  [Помню]  [Легко]      │  SM-2 Buttons
│                                 │
│                                 │
│       Progress bar              │  ████░░ 15/20
│       Gold gradient              │
│                                 │
└─────────────────────────────────┘
         [Bottom Nav]
```

### **DictionaryScreen**

```
┌─────────────────────────────────┐
│ Словарь                         │
│ [🔍 Поиск слова...]             │  Search field
├─────────────────────────────────┤
│ ┌─ ФИЛЬТРЫ ─────────────────────┐│
│ │ All | 📖 Vocab | ✏️ Grammar   ││  Chip row
│ │      | 📕 Phrase | 📚 Content ││
│ └───────────────────────────────┘│
│                                 │
│ РЕЗУЛЬТАТЫ:                     │  Results list
│                                 │
│ gato (га́то) — кот             │  Word + transcription + translation
│ ════════════════════════════════│  Divider
│ Испанский существительное       │  Part of speech (tag)
│ Частота: 1240 / 10000          │  Frequency info
│                                 │
│ Примеры:                        │
│ - Mi gato duerme todo el día    │  Example with translation
│   (Мой кот спит целый день)    │
│                                 │
│ [+ Добавить в Tarjetas]         │  Action button
│                                 │
├─────────────────────────────────┤
│ [Word image if available]       │  Optional illustration (1.5MB each)
│                                 │
└─────────────────────────────────┘
```

### **GamesScreen (Libros)**

```
┌─────────────────────────────────┐
│ Игры                            │
│                                 │
│ 🎮 МИНИ-ИГРЫ                   │  Section header
│                                 │
│ ┌──────────────────────────────┐│  Game card
│ │ 📖 Артикли                  ││  Type + name
│ │ Практикуй свободное владение ││  Description
│ │ ████░░░ 25%                  ││  Progress
│ │ [Начать →]                   ││  CTA
│ └──────────────────────────────┘│
│                                 │
│ ┌──────────────────────────────┐│
│ │ ⏱️  Скорость                  ││
│ │ Отвечай на вопросы быстро    ││
│ │ ░░░░░░░░░░ 0%                ││
│ │ [Заблокировано]              ││  (if locked)
│ └──────────────────────────────┘│
│                                 │
│ 🎓 КУРСЫ                        │  Another section
│ ...                             │
│                                 │
└─────────────────────────────────┘
```

### **LeaderboardScreen**

```
┌─────────────────────────────────┐
│ Лидерборд                       │
│ Сезон: Весна 2026               │  Header
├─────────────────────────────────┤
│ 🏆 ПУТЬ К МАДРИДУ              │  Title
│                                 │
│ Текущая позиция:                │
│ 🏰 Aldea perdida (Ваше место: 5)│  Current city
│ ██████░░░░ 340 / 500 XP        │  Progress to next city
│                                 │
├─────────────────────────────────┤
│ TOP 10:                         │  Rankings
│                                 │
│ 1. 🇪🇸 José García             │  Rank + flag + name
│    ✨ 5,240 XP                  │  Stats
│                                 │
│ 2. 🇷🇺 Иван Петров             │
│    ✨ 4,980 XP                  │
│                                 │
│ 5. 🇪🇸 Tú (ты) — HighlightedRow │  Current user (teal tint)
│    ✨ 3,240 XP                  │
│                                 │
│ ...                             │
│                                 │
├─────────────────────────────────┤
│ [Обновить] [Пригласить друзей] │  Actions
│                                 │
└─────────────────────────────────┘
```

### **ProfileScreen**

```
┌─────────────────────────────────┐
│ Mi Perfil                       │  Spanish header 😎
├─────────────────────────────────┤
│                                 │
│          [👤 Avatar]            │  User photo (from Google Sign-In)
│          Ivan Petrov            │  Name (title-lg)
│                                 │
├─────────────────────────────────┤
│ СТАТИСТИКА                      │  Section header
│                                 │
│ ┌────────┐ ┌────────┐ ┌────────┐│
│ │ 📚 42  │ │ ⏱️ 128 │ │ 📖 640 ││  Stats pills (3 columns)
│ │ уровни │ │ часов  │ │ слов   ││
│ └────────┘ └────────┘ └────────┘│
│                                 │
│ ✓ Сегодня занимался 30 / 60 мин│  Daily summary
│ ✓ Уже практиковал               │  Streak info
│ 🔥 12 дней подряд               │
│                                 │
├─────────────────────────────────┤
│ ПРЕМИУМ                         │  Premium section
│                                 │
│ 🤖 ИИ-репетитор на Claude      │  Feature (✨ locked)
│ 📊 Детальная аналитика         │  Feature (✨ locked)
│ 🎁 +500 XP в день              │  Feature (✨ locked)
│                                 │
│ [Попробовать бесплатно 7 дней] │  Premium CTA
│                                 │
├─────────────────────────────────┤
│ ОПЦИИ                           │  Settings section
│ ⚙️  Настройки                   │  Link
│ 📧 О приложении                 │  Link
│ 🔗 Пригласить друзей            │  Link
│                                 │
└─────────────────────────────────┘
```

### **SettingsScreen**

```
┌─────────────────────────────────┐
│ Настройки                       │
├─────────────────────────────────┤
│                                 │
│ ЯЗЫК И РЕГИОН                   │  Section
│ ├─ Язык приложения              │  Toggle: Русский / English
│ ├─ Испанский диалект            │  Radio: Европейский / Мексиканский
│ │                              │
├─────────────────────────────────┤
│ УВЕДОМЛЕНИЯ                     │  Section
│ ├─ Ежедневные напоминания      │  Toggle: ON / OFF
│ │  Время: [14:00]              │  Time picker
│ │                              │
├─────────────────────────────────┤
│ ОБУЧЕНИЕ                        │  Section
│ ├─ Сложность                    │  Radio: Лёгкая / Нормальная / Сложная
│ ├─ Интервал повторений          │  Radio: Узкий / Стандартный / Широкий
│ │                              │
├─────────────────────────────────┤
│ ДАННЫЕ                          │  Section
│ ├─ [Экспортировать прогресс]   │  Button
│ ├─ [Сбросить весь прогресс]    │  Destructive button
│ │                              │
├─────────────────────────────────┤
│ О ПРИЛОЖЕНИИ                    │  Section
│ ├─ Версия: 1.2.4                │  Info
│ ├─ Последнее обновление: 2026-05-01 │
│                                 │
└─────────────────────────────────┘
```

---

## 🔑 Ключевые анимации

| Компонент | Тип | Параметры |
|---|---|---|
| **Bottom Nav** | Scale | Spring.DampingRatioMediumBouncy, 1.08x |
| **XP Bar** | Color crossfade | Tween(200ms) |
| **Flashcard** | Rotate flip | Spring.DampingRatioLowBouncy, StiffnessLow, 600ms |
| **Review buttons** | Scale+fade | AnimatedContent, 300ms |
| **Success end-screen** | Lottie | 160dp, 2s loop, 1× playback |
| **Streak flame** | Pulse | 1.0 → 1.12 scale, 700ms, reverse repeat |
| **Onboarding** | Slide+fade | Horizontal, 300ms |

---

## 🎨 Копирайт по экранам

### HomeScreen
```
Header:       "Главная 👋"
Section 1:    "КУРСЫ"
Section 2:    "СЛОВО ДНЯ"
CTA:          "Начать обучение →"
Stat phrase:  "✓ Сегодня занимался 30 / 60 мин"
Leaderboard:  "Следующая остановка: 🏛️ Madrid"
              "Ваше место: 5"
```

### OnboardingScreen
```
Main title:   "¡Hola!"
Subtitle:     "Добро пожаловать в ESPEAK"
              "Приложение для изучения испанского языка"
Step 1:       "Как тебя зовут?"
Step 2:       "Выбери свой уровень"
              "A1 — Beginner", "A2 — Elementary", etc
Final CTA:    "Будем учиться вместе 🎉"
```

### FlashcardsScreen
```
Title:        "Tarjetas"
Counter:      "Карточка 3 / 20"
Hint:         "← Tap to flip →"
Buttons:      ["❌ Забыл"] ["✅ Помню"] ["🟢 Легко"]
Complete:     "Отличная работа! 🎉"
```

### ProfileScreen
```
Title:        "Mi Perfil"
Stats labels: "уровни", "часов", "слов"
Feature:      "🤖 ИИ-репетитор на Claude"
CTA:          "Попробовать бесплатно 7 дней"
              "$4.99/месяц или $29.99/год"
```

---

## 📦 Как кодировать новый компонент?

### Шаблон Kotlin (Jetpack Compose):

```kotlin
@Composable
fun MyComponent(
    title: String,
    isActive: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(20.dp))  // --radius-2xl
            .clickable { onClick() },
        color = AppColors.SurfaceCard,
        shadowElevation = if (isActive) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),  // --space-7
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,  // --t-title-lg
                color = AppColors.TextPrimary
            )
            // ...
        }
    }
}
```

### Шаблон React (JSX):

```jsx
export function MyComponent({ title, isActive = true, onClick = () => {} }) {
    return (
        <div
            onClick={onClick}
            style={{
                maxWidth: '100%',
                padding: '0 var(--space-4)',
                borderRadius: 'var(--radius-2xl)',
                backgroundColor: 'var(--surface-card)',
                boxShadow: isActive ? 'var(--shadow-card-lg)' : 'var(--shadow-card)',
                padding: 'var(--space-7)',
            }}
        >
            <h4 className="t-title-lg">{title}</h4>
            {/* ... */}
        </div>
    );
}
```

---

## ✅ Чек-лист для нового экрана

- [ ] Используешь `--space-4` для паддинга карточек
- [ ] Используешь `--radius-2xl: 20px` для основных карточек
- [ ] Все цвета из `colors_and_type.css`
- [ ] Типография из `.t-*` классов
- [ ] Иконы Material Icons (Material) или emoji
- [ ] Bottom Nav включен (если это основной экран)
- [ ] Копирайт на русском, испанские акценты остаются
- [ ] Informal tone (тыканье)
- [ ] Spring-анимация вместо tween
- [ ] Shadow: purple-tinted

---

## 🎯 Заключение

Все компоненты EASPEAK построены на трёх столпах:
1. **Strict design tokens** (в `colors_and_type.css`)
2. **Reusable component library** (в `Components.kt`)
3. **Consistent copywriting** (русский + испанский)

Соблюдай эти принципы → получишь масштабируемую систему.

