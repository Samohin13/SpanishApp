# 📱 EASPEAK Design System — Полный обзор структуры

## 🎯 Что это?

**EASPEAK** — это полнофункциональная русскоязычная Android-приложение для изучения испанского языка (уровни CEFR A1→B2), построенное на **Kotlin + Jetpack Compose**. Это не просто UI-набор, а **полная проектная структура** с дизайн-системой, компонентами, экранами и всем необходимым для масштабирования.

---

## 📁 Архитектура папок

```
EASPEAK_Design_System/
├── 📄 README.md                    ← Главная документация (268 строк!)
├── 📄 SKILL.md                     ← Agent-skill манифест
├── 📄 colors_and_type.css          ← Все CSS-переменные дизайн-системы
│
├── 📦 app/                         ← Исходный код Android-приложения
│   └── src/main/java/com/spanishapp/
│       ├── MainActivity.kt         ← точка входа
│       └── ui/
│           ├── theme/              ← дизайн-токены
│           │   ├── Theme.kt        ← AppColors + AppTypography
│           │   └── Type.kt
│           ├── components/         ← переиспользуемые компоненты
│           │   └── Components.kt   ← bottom nav, XP bar, и т.д.
│           └── [screens]/          ← 8 основных экранов приложения
│               ├── home/
│               ├── flashcards/
│               ├── games/
│               ├── dictionary/
│               ├── leaderboard/
│               ├── profile/
│               ├── settings/
│               └── onboarding/
│
├── 📁 assets/                      ← Логотип и растровые ресурсы
│   ├── logo.png (512×512)
│   └── logo-soundwave.png
│
├── 🎨 preview/                     ← HTML-превью для Figma/дизайн-системы
│   ├── index.html                  ← главная страница системы
│   ├── colors-brand.html           ← палитра
│   ├── components-*.html           ← 10+ компонентов
│   ├── type-*.html                 ← типография
│   ├── spacing.html, shadows.html, gradients.html
│   └── ...
│
├── 🎬 ui_kits/espeak_app/          ← React/JSX-версия экранов для веб
│   ├── android-frame.jsx
│   ├── ios-frame.jsx
│   ├── index.html
│   ├── HomeScreen.jsx
│   ├── FlashcardScreen.jsx
│   ├── DictionaryScreen.jsx
│   ├── LeaderboardScreen.jsx
│   ├── ProfileScreen.jsx
│   ├── SettingsScreen.jsx
│   ├── OnboardingScreen.jsx
│   ├── GamesScreen.jsx
│   ├── CourseDetailScreen.jsx
│   ├── PremiumSheet.jsx
│   └── Shared.jsx                  ← общие компоненты React
│
├── 🏢 brand/                       ← бренд-гайды
│   ├── AppIcon.html
│   ├── PlayStoreScreenshots.html
│   └── app-icon-source.png
│
├── 📤 uploads/                     ← примеры и скриншоты
└── 📤 export_*/                    ← версионирование (export_v2, export_for_github)
```

---

## 🎨 Дизайн-система: ключевые компоненты

### 1️⃣ **Цветовая палитра**

| Назначение | Переменная | Цвет | Использование |
|---|---|---|---|
| **Основной** | `--brand-purple` | `#7B2FBE` | CTA, активная нав, лидерборд |
| **Акцент 1** | `--stat-gold` | `#FF9500` | XP, sparkles, логотип |
| **Акцент 2** | `--stat-orange` | `#FF6B00` | огонь стрика, прогресс |
| **Градиент XP** | `--grad-xp` | purple → pink | полоса опыта |

**CEFR-уровни:**
- A1: 🟢 зелёный (`#2E7D32`)
- A2: 🔵 синий (`#0277BD`)
- B1: 🟠 оранжевый (`#E65100`)
- B2: 🟣 фиолетовый (`#6A1B9A`)

### 2️⃣ **Типография**

Используется **Inter (для веб) / Roboto (для Android)**, одна семейство, вес от 400 до 800:

| Класс | Размер | Вес | Применение |
|---|---|---|---|
| `t-display-lg` | 34px | 800 | главный заголовок (onboarding) |
| `t-display-md` | 28px | 700 | крупные заголовки |
| `t-headline-lg` | 24px | 700 | заголовки экранов |
| `t-title-md` | 16px | 600 | заголовки карточек |
| `t-body-lg` | 16px | 400 | основной текст |
| `t-label-sm` | 11px | 700 | мелкие метки (все caps) |

### 3️⃣ **Компоненты**

#### Карточки (Topic Card)
```
┌─────────────────────┐
│ [gradient header]   │  100dp, 20px radius
│ emoji | Название   │
│       A1            │
├─────────────────────┤
│ Описание...         │
│ ████████ 75%        │  progress bar
│ Начать обучение →   │  CTA (bold, accent)
└─────────────────────┘
```

#### Bottom Navigation
- 5 табов: `Главная`, `Игры`, `Tarjetas`, `Словарь`, `Профиль`
- Spring-анимация (1.08 scale, `DampingRatioMediumBouncy`)
- Иконки Material Icons (Outlined ↔ Filled)

#### Stats Pills
- Фон: `--stat-gold-bg` (`#FFF3E0`)
- Содержит: emoji + число (28sp ExtraBold)
- Примеры: `✨ 1240 XP`, `🔥 12 дней`

#### Buttons
- `--radius-md`: 12px
- Material3 ripple (нет override)
- Sizes: 12sp label

### 4️⃣ **Spacing & Radius**

| Токен | Значение | Использование |
|---|---|---|
| `--space-4` | 14px | горизонтальный паддинг карточек |
| `--space-7` | 20px | внутренний паддинг контента |
| `--radius-2xl` | 20px | основной радиус карточек ⭐ |
| `--radius-4xl` | 28px | Path-to-Madrid карточка |
| `--radius-pill` | 9999px | stats pills |

### 5️⃣ **Тени (Shadows)**

```css
--shadow-card:    0 4px 16px rgba(123, 47, 190, 0.10);    /* стандарт */
--shadow-card-lg: 0 6px 24px rgba(123, 47, 190, 0.18);    /* активная */
--shadow-locked:  0 2px 8px rgba(0, 0, 0, 0.06);          /* disabled */
```
💜 Все тени **фиолетово-окрашены** (фирменный цвет)

---

## 📱 Экраны приложения

### 1. **HomeScreen** — Главная
- 📋 Карточки курсов по уровням CEFR
- ⭐ слово дня (Word of the Day)
- 🔥 статистика (XP, streak)
- 🎯 "Path to Madrid" лидерборд

### 2. **OnboardingScreen** — Онбординг
- 🎨 `¡Hola!` с испанским флагом (80sp emoji)
- 🔘 выбор имени
- 🎯 выбор уровня начала

### 3. **FlashcardsScreen** — Флэшкарты
- 🃏 карточки с интервальным повторением (SM-2)
- ❌ кнопки: "Забыл", "Помню", "Легко"
- ✨ успешная анимация в конце

### 4. **DictionaryScreen** — Словарь
- 🔍 поиск слов
- 🏷️ теги по типам (vocabulary, grammar, phrases)
- 🎧 произношение
- 🖼️ иллюстрация слова

### 5. **GamesScreen (Libros)** — Игры
- 🎮 мини-игры на испанском
- 📊 прогресс по типам

### 6. **LeaderboardScreen** — Лидерборд
- 🏆 Path to Madrid (города вместо обычного рейтинга)
- 🌍 флаги стран
- 📈 ХП и стрики

### 7. **ProfileScreen** — Профиль
- 👤 аватар (Google Sign-In)
- 📊 статистика: дни, часы, слова
- ⚙️ настройки
- 💎 Премиум (AI-репетитор на Claude)

### 8. **SettingsScreen** — Настройки
- 🌐 язык
- 🔔 уведомления
- 🎨 тема
- 📝 о приложении

---

## 🎬 Web-версия (React UI Kit)

В папке `ui_kits/espeak_app/` находятся **React/JSX-компоненты**, которые повторяют всё приложение:

- `android-frame.jsx` — Android device frame
- `ios-frame.jsx` — iOS device frame
- Все экраны как `.jsx` файлы
- `Shared.jsx` — общие компоненты (кнопки, карточки, наv)

**Назначение:** Показывать дизайн-систему в браузере, эксперт-ревью, прототипирование.

---

## 🗣️ Копирайтинг и тон

### Языковые правила:
✅ **Основной язык:** русский (система, кнопки, ошибки)
✅ **Испанские акценты:** оставляются на испанском (`Tarjetas`, `Mi Perfil`, `¡Hola!`)
✅ **Тыканье:** informal "ты", как друг
✅ **Прямые императивы:** `Начать обучение`, `Попробовать бесплатно 7 дней`

### Примеры копирайта:
```
"¡Hola! Добро пожаловать…"
"Продолжай изучать испанский язык"
"Начать обучение →"
"✓ Сегодня занимался 30 / 60 мин"
"Следующая остановка: 🏛️ Madrid"
"ИИ-репетитор на Claude"
```

---

## 🎯 Ключевые визуальные принципы

| Принцип | Правило |
|---|---|
| **Emoji** | 1-й класс UI элементы, не просто декор (14–80sp) |
| **Палитра** | Светлая тема, никаких изображений, плоский дизайн |
| **Анимация** | Spring-physics (не duration-based) |
| **Иконы** | Material Icons (Material Design 24dp) |
| **Пустое пространство** | 14–20dp паддинг, 20px радиус — стандарт |
| **Рипли** | No ripple на bottom-nav, только color crossfade |

---

## 📊 Статистика архива

| Метрика | Значение |
|---|---|
| Общий размер | ~2.9 GB (в основном изображения) |
| Файлов | 50+ |
| Экранов | 8 основных |
| Компонентов | 10+ переиспользуемых |
| CSS-переменных | 50+ |
| Цветов в палитре | 25+ |

---

## ✨ Как использовать эту структуру?

### Для новых экранов:
1. Скопируй структуру из `app/src/main/java/com/spanishapp/ui/[screen]/`
2. Используй токены из `Theme.kt`
3. Применяй компоненты из `Components.kt`
4. Следуй правилам спейсинга и типографики

### Для компонентов:
1. Смотри `Components.kt` для примеров
2. Используй Material Icons + emoji
3. Придерживайся `--radius-2xl: 20px` для карточек

### Для копирайта:
1. Помни про русский+испанский микс
2. Используй informal tone (`ты`)
3. Добавляй emoji где логично

### Для дизайна:
1. Все цвета в `colors_and_type.css`
2. All spacing в `--space-*` переменных
3. Все типографские стили в `.t-*` классах

---

## 🔗 Полезные файлы для копирования

```
Скопируй эти файлы в новый проект:

✅ colors_and_type.css           — весь дизайн в CSS
✅ app/src/main/java/.../theme/Theme.kt    — токены Kotlin
✅ app/src/main/java/.../components/Components.kt  — готовые компоненты
✅ preview/index.html            — главная страница системы
✅ ui_kits/espeak_app/Shared.jsx — React компоненты
```

---

## 📝 Резюме

**EASPEAK** — это не просто красивое приложение, а **мастер-класс по дизайн-системам**:

- 🎯 **Одна палитра** (purple-first, gold accent)
- 📱 **Один продукт** (никаких отвлечений)
- 🎨 **Строгие правила** spacing, type, color
- 🌍 **Двуязычный дизайн** (русский + испанский)
- ✨ **Emoji-first** подход к иконографии
- 📊 **Fully documented** (README на 268 строк!)

Идеально для масштабирования под новые фичи, локализацию или веб-версию.

