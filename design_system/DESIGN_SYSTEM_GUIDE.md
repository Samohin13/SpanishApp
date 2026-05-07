# 🎨 EASPEAK Design System — Полный гайд

Добро пожаловать в дизайн-систему приложения **EASPEAK**! Эта папка содержит всё необходимое для понимания визуального языка и архитектуры приложения.

---

## 📂 Содержимое папки

### 📖 Документация (начни отсюда!)

| Файл | Описание |
|---|---|
| **README.md** | Официальная документация из Figma (268 строк) |
| **EASPEAK_Structure_Overview.md** | 🔥 **НАЧНИ ОТСЮДА** — полный обзор архитектуры |
| **EASPEAK_Components_Guide.md** | Детальное описание каждого компонента и экрана |

### 🎨 Дизайн-токены

| Файл | Для чего |
|---|---|
| **colors_and_type.css** | Все CSS-переменные (цвета, типография, spacing, shadows) |
| **SKILL.md** | Agent-skill манифест для Claude Code |

### 🖼️ Ресурсы

| Папка | Содержит |
|---|---|
| **assets/** | Логотипы и изображения (logo.png, logo-soundwave.png) |
| **brand/** | Бренд-гайды (AppIcon.html, PlayStoreScreenshots.html) |
| **preview/** | HTML-превью компонентов для браузера (index.html + 15+ страниц) |
| **ui_kits/espeak_app/** | React/JSX-версия экранов для веб-демонстрации |

---

## 🚀 Как начать разработку?

### 1️⃣ Прочитай документацию (5 минут)

```bash
# Открой эти файлы в этом порядке:
1. EASPEAK_Structure_Overview.md      ← архитектура
2. EASPEAK_Components_Guide.md        ← компоненты
3. README.md                          ← подробные детали
```

### 2️⃣ Изучи дизайн-токены (3 минуты)

Открой `colors_and_type.css` и найди там:
- **Цвета:** `--brand-purple`, `--stat-gold`, `--cefr-*`
- **Типография:** `--t-display-lg`, `--t-body-md`, и т.д.
- **Spacing:** `--space-4` (14px), `--space-7` (20px)
- **Radii:** `--radius-2xl` (20px — основной)
- **Shadows:** `--shadow-card-lg` (фиолетовый оттенок)
- **Градиенты:** `--grad-xp`, `--grad-streak`

### 3️⃣ Посмотри превью компонентов (в браузере)

```bash
# Открой в браузере:
preview/index.html
```

Там ты увидишь:
- Палитру всех цветов
- Типографские стили
- Компоненты (кнопки, карточки, навигация)
- Spacing и radii

### 4️⃣ Погляди React-версию экранов

```bash
# Открой в браузере:
ui_kits/espeak_app/index.html
```

Там полная React-версия всех экранов приложения!

### 5️⃣ Начни кодить свой компонент

Используй шаблоны из `EASPEAK_Components_Guide.md`:

**Kotlin (Jetpack Compose):**
```kotlin
@Composable
fun MyComponent(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)  // --space-4
            .clip(RoundedCornerShape(20.dp)),  // --radius-2xl
        color = AppColors.SurfaceCard,
        shadowElevation = 6.dp  // --shadow-card-lg
    ) {
        Column(modifier = Modifier.padding(20.dp)) {  // --space-7
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.TextPrimary
            )
        }
    }
}
```

**React (JSX):**
```jsx
export function MyComponent({ title }) {
    return (
        <div style={{
            maxWidth: '100%',
            padding: '0 var(--space-4)',
            borderRadius: 'var(--radius-2xl)',
            boxShadow: 'var(--shadow-card-lg)',
            backgroundColor: 'var(--surface-card)'
        }}>
            <h4 className="t-title-lg">{title}</h4>
        </div>
    );
}
```

---

## 📊 Структура приложения (8 основных экранов)

```
EASPEAK App
├── 🏠 HomeScreen         — курсы, статистика, лидерборд
├── 🃏 FlashcardsScreen   — флэшкарты с SM-2 повторениями
├── 📖 DictionaryScreen   — словарь с примерами
├── 🎮 GamesScreen        — мини-игры (Libros)
├── 🏆 LeaderboardScreen  — Path to Madrid лидерборд
├── 👤 ProfileScreen      — профиль, статистика, премиум
├── ⚙️ SettingsScreen     — язык, уведомления, данные
└── 🎊 OnboardingScreen   — приветствие, выбор уровня
```

Полные макеты всех экранов смотри в `EASPEAK_Components_Guide.md`

---

## 🎨 Ключевые правила дизайна

### Цветовая палитра

| Назначение | Цвет | Значение |
|---|---|---|
| 💜 Основной (CTA, nav) | Purple | `#7B2FBE` |
| ✨ XP, sparkles | Gold | `#FF9500` |
| 🔥 Streak, огонь | Orange | `#FF6B00` |
| 🟢 A1 (Beginner) | Green | `#2E7D32` |
| 🔵 A2 (Elementary) | Blue | `#0277BD` |
| 🟠 B1 (Intermediate) | Orange | `#E65100` |
| 🟣 B2 (Upper) | Purple | `#6A1B9A` |

### Типография

- **Шрифт:** Inter (веб) / Roboto (Android)
- **Весовой диапазон:** 400–800
- **Основные стили:**
  - `displayLarge` (34px / 800) — главные заголовки
  - `titleLarge` (18px / 700) — заголовки экранов
  - `bodyLarge` (16px / 400) — основной текст
  - `labelSmall` (11px / 700) — мелкие метки

### Spacing

- `--space-4` (14px) — паддинг карточек
- `--space-7` (20px) — внутренний паддинг контента
- `--space-5` (16px), `--space-6` (18px) — прочее

### Radius

- `--radius-2xl` (20px) — **основной радиус карточек** ⭐
- `--radius-md` (12px) — кнопки, поля
- `--radius-pill` (9999px) — stats pills

### Shadows

- Все тени **фиолетово-окрашены** (фирменный цвет)
- `--shadow-card-lg` (6dp) — активные карточки
- `--shadow-card` (4dp) — обычные карточки
- `--shadow-locked` (2dp) — неактивные

### Анимация

- **Spring physics** вместо duration-based easing
- Spring параметры: `DampingRatioMediumBouncy`, `StiffnessHigh`
- Masштаб при клике: 1.08x

### Иконография

- Material Icons (Material Design, 24dp)
- Emoji как 1-класс UI элементы (14–80sp)
- Никаких собственных иллюстраций (только emoji)

---

## 🗣️ Правила копирайта

### Языки

✅ **Основной язык:** русский (система, кнопки, ошибки)
✅ **Испанские акценты:** оставляются на испанском (`Tarjetas`, `Mi Perfil`, `¡Hola!`)

### Тон

✅ **Informal** — "ты" вместо "Вы"
✅ **Прямые команды** — `Начать обучение`, `Показать перевод`
✅ **Позитивный** — поощрение и похвала

### Примеры

```
"¡Hola! Добро пожаловать…"
"Продолжай изучать испанский язык"
"Начать обучение →"
"✓ Сегодня занимался 30 / 60 мин"
"Следующая остановка: 🏛️ Madrid"
"ИИ-репетитор на Claude"
```

---

## 📦 Как скопировать компонент в новый проект?

### Для Kotlin/Jetpack Compose:

1. Открой `app/src/main/java/com/spanishapp/ui/components/Components.kt`
2. Скопируй нужный компонент
3. Используй токены из `app/src/main/java/com/spanishapp/ui/theme/Theme.kt`
4. Следуй правилам spacing, type, color из документации

### Для React/JSX:

1. Открой `ui_kits/espeak_app/Shared.jsx`
2. Скопируй компонент
3. Используй CSS-переменные из `colors_and_type.css`
4. Импортируй в свой проект

---

## ✅ Чек-лист для нового экрана

При создании нового экрана убедись, что:

- [ ] Используешь `--space-4` для паддинга карточек
- [ ] Используешь `--radius-2xl: 20px` для основных карточек
- [ ] Все цвета из палитры
- [ ] Типография из `.t-*` классов
- [ ] Иконы Material Icons или emoji
- [ ] Копирайт на русском, испанские акценты остаются
- [ ] Informal tone (тыканье)
- [ ] Spring-анимация вместо tween
- [ ] Shadow: фиолетово-окрашенная
- [ ] Bottom Nav (если основной экран)

---

## 🔗 Полезные файлы для копирования

```
Основное (обязательно скопируй):
├── colors_and_type.css           ← все дизайн-токены
├── app/src/main/java/.../theme/Theme.kt    ← токены Kotlin
├── app/src/main/java/.../components/Components.kt  ← компоненты
└── preview/index.html            ← система компонентов

React версия (если нужна веб-демонстрация):
└── ui_kits/espeak_app/           ← все React экраны
```

---

## 📚 Дополнительные ресурсы

- **Material Design 3:** https://m3.material.io/
- **Jetpack Compose docs:** https://developer.android.com/jetpack/compose
- **Figma дизайн-система:** это именно отсюда импортирована документация

---

## 🎯 Заключение

**EASPEAK** — это мастер-класс по дизайн-системам:

- ✅ Одна палитра (purple-first)
- ✅ Один продукт (никаких отвлечений)
- ✅ Строгие правила (spacing, type, color)
- ✅ Двуязычный дизайн (русский + испанский)
- ✅ Emoji-first подход
- ✅ Полностью документирована

**Используй эту систему для масштабирования под новые фичи, локализацию или веб-версию.**

---

**Вопросы?** Смотри:
1. `EASPEAK_Structure_Overview.md` — общее понимание
2. `EASPEAK_Components_Guide.md` — конкретные компоненты
3. `README.md` — детальные правила

Happy coding! 🚀
