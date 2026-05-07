# EASPEAK Design System

> **EASPEAK** — Russian-language Android app for learning Spanish (CEFR A1 → B2). Kotlin + Jetpack Compose, with an AI tutor (Claude), spaced-repetition flashcards, mini-games, and a "Path to Madrid" league/leaderboard system.

The brand mark on the splash screen reads **EASPEAK** (a play on _español_ + _speak_); internal Android `app_name` is `HablaRu`. We use **EASPEAK** as the canonical product name in this design system.

---

## Sources

- **GitHub:** `Samohin13/SpanishApp` @ `master` (private). Imported subset under `app/`.
- **Theme / tokens:** `app/src/main/java/com/spanishapp/ui/theme/Theme.kt` (object `AppColors` + `AppTypography`).
- **Components:** `app/src/main/java/com/spanishapp/ui/components/Components.kt` (bottom nav, XP bar).
- **Reference screens read for visual patterns:** `home/HomeScreen.kt`, `flashcards/FlashcardsScreen.kt`, `profile/ProfileScreen.kt`, `dictionary/DictionaryScreen.kt`, `onboarding/OnboardingScreen.kt`, `games/LibrosScreen.kt`, `leaderboard/LeaderboardScreen.kt`.
- **Logo:** `app/src/main/res/drawable/ic_splash_logo.png` (copied to `assets/logo.png`).
- **Copy tone:** read directly off the screens (Russian UI, Spanish accents like _Tarjetas_, _¡Hola!_, _Mi Perfil_).

The app was built solo by the developer (Samohin13). Target audience is Russian speakers (children and adults), beginners through B2.

---

## Index

| File / folder            | What's there                                                                          |
| ------------------------ | ------------------------------------------------------------------------------------- |
| `README.md`              | This file — context, content rules, visual foundations, iconography                   |
| `SKILL.md`               | Agent-Skill manifest so this folder is portable to Claude Code                        |
| `colors_and_type.css`    | All CSS vars: colors, type scale, radii, spacing, shadows, gradients                  |
| `assets/`                | Logo + any raster assets pulled from the app                                          |
| `preview/`               | Small HTML cards (~700×N) shown on the Design System tab                              |
| `ui_kits/espeak_app/`    | High-fidelity React/JSX recreation of EASPEAK app screens                              |

---

## Products

There is **one product**: the EASPEAK Android app. No marketing site, no docs site, no separate web product. Everything in `ui_kits/` mocks the mobile app inside an Android device frame.

---

## CONTENT FUNDAMENTALS

EASPEAK is a **Russian-language UI** for learning **Spanish**. The bilingual nature defines the entire voice.

### Language

- **Primary UI language: Russian.** All system copy — buttons, navigation, errors, headings — is Russian.
- **Spanish accents are kept untranslated** as part of the brand. Examples pulled directly from the codebase:
  - Tab labels: `Главная` (Home), `Игры` (Games), **`Tarjetas`** (Flashcards — kept Spanish), `Словарь` (Dictionary), `Профиль` (Profile).
  - Profile screen title: **`Mi Perfil`**.
  - Onboarding hello: **`¡Hola!`** at 80sp, then "Добро пожаловать…" below.
  - Feature list teaser: `🃏 Tarjetas с интервальным повторением`, `🎮 Juegos: артикли, скорость, анаграммы`.
- **Inverted exclamation/question marks (`¡`, `¿`) are required** wherever Spanish appears. They are also used as a parser signal in code (`DictionaryViewModel.isPhrase`).
- Cyrillic and Latin sit next to each other freely — never italicize one to "other" it.

### Tone & address form

- **Тыcanье** (informal "ты"). The app talks to the user like a friend, never with formal `Вы`.
  - `Привет! 👋` / `Как тебя зовут?` / `Это имя будет отображаться в твоём профиле` / `¡Hola, $name! Будем учиться вместе 🎉`.
  - Encouragement on completion: `Отличная работа!`, `Ещё раз`.
- **Imperatives are direct, not soft.** `Показать перевод`, `Начать обучение →`, `Попробовать бесплатно 7 дней`.
- **Self-talk in stats** (1st-person), e.g. `✓ Сегодня занимался $X / $Y мин`, `✓ Уже практиковал` — the app speaks _as_ the user reporting their progress.

### Casing

- Russian sentences use **sentence case** ("Слово дня", not "Слово Дня").
- CEFR level codes are always uppercase: `A1`, `A2`, `B1`, `B2`.
- Spanish proper nouns keep Spanish capitalization (`Madrid`, `Sevilla`, `Aldea perdida`).
- Section labels in pill/badge form are sometimes Latin lowercase from icon-pack convention (`vocab`, `grammar`, `phrase`, `content`) but rendered with a Russian word + emoji on screen (`📖 Теория`, `✏️ Практика`).

### Emoji usage

**Emoji are core to the visual system, not decoration.** They appear in:

- Tab bar greetings: `Привет! 👋`
- Stats pills: `✨` for XP, `🔥` for streak, `🎁`, `✅`, `🤖` in premium sheet, `📚 ⏱ 🔥` in profile stats.
- Course cards: each CEFR level gets its own emoji icon (`🚀 A1`, `🌍 A2`, `📚 B1`, `🎓 B2`). These are rendered at **40sp** as the dominant illustration.
- Roadmap blocks: each block has an emoji avatar in a 44dp white-on-color circle.
- Lesson type tags: `📖 Теория`, `✏️ Практика`, `🎧` listening game, `📚` Libros.
- League / Path-to-Madrid: city emoji + region (e.g. `🏰 Aldea perdida`, `🏛️ Madrid`).
- Country flags on the leaderboard: ISO → flag emoji.
- Spanish flag `🇪🇸` is the welcome screen mascot at 80sp.

**No custom illustration system, no Lottie character mascot.** Emoji + a few inline `Canvas` drawings (e.g. mastery flags) carry the personality. Treat emoji as first-class UI elements with their own sizes (typically 14sp inside pills, 22–26sp in headers, 40–80sp as feature icons).

### Numbers & units

- Russian abbreviations: `мин` (minutes), `дней подряд`, `блок`, `уровень`.
- Currency in premium sheet uses `$` (USD): `$4.99`, `$29.99` — no localized formatting yet.
- Streak / XP shown as bare numbers next to a label-emoji: `🔥 12`, `✨ 1240`.

### Examples from the codebase (verbatim)

```
"Продолжай изучать испанский язык"
"Начать обучение →"
"Заблокировано"
"Ещё не занимался сегодня"
"✓ Сегодня занимался  $X / $Y мин"
"7 дней бесплатного триала"
"ИИ-репетитор на Claude"
"Следующая остановка: 🏛️ Madrid"
"Будем учиться вместе 🎉"
"Отличная работа!"
"Забыл" / "Помню" / "Легко"   (SM-2 review buttons)
```

Avoid: corporate "we", marketing superlatives, exclamation overload outside celebratory moments.

---

## VISUAL FOUNDATIONS

### Palette

- **Primary: purple `#7B2FBE`.** Used for the active bottom-nav, primary CTAs, level avatars, "Tarjetas" branding, and the gradient first-stop on the XP bar. The palette also includes a light variant (`#9C4FDC`) and two pale tints (`#F3E8FF`, `#EDE0F8`) for pill backgrounds.
- **Pink `#E040FB`** is _only_ the second stop of the signature `--grad-xp` gradient. It rarely stands alone.
- **Gold `#FF9500` + Orange `#FF6B00`** are the stats palette — XP sparkles (gold) and streak fire (orange). They share a pale background `#FFF3E0` when used in pill form, and a gold→orange linear gradient on the streak progress bar.
- **CEFR colors** are categorical, not brand-aligned: A1=green, A2=blue, B1=orange, B2=purple. They appear on level badges and as the dominant tint for course cards (`--course-a1` etc.).
- **Lesson-type chips** use a low-saturation tinted-bg/strong-fg pattern (vocab=green, grammar=blue, phrase=purple, content=indigo, practice=orange).
- **Theme is light only.** Theme.kt has dark-named legacy aliases (`BgDeep`, `Surface1`) but they all point at light values; no functional dark theme ships.

### Type

- Single family — Android system sans (Roboto). For previews we substitute **Inter**. Display weights go up to **ExtraBold (800)** with **letter-spacing -1px** at the largest size.
- Scale is the standard Material3 `Typography` set, slightly heavier than default: `displayLarge` is 34sp / 800. `displayMedium` is 28sp / 700. `bodyLarge` is 16sp / 400. The smallest label (`labelSmall` 11sp / 700) carries 0.5sp tracking and is typically used as an all-caps stat label.
- Numerals are emphatic — streak, XP and skill-rating numbers all jump to **28sp ExtraBold** in their cards, paired with a small descriptive label.

### Backgrounds

- **No images, no full-bleed photography, no patterns, no textures.** The app is a flat surface play.
- Two background tones:
  - **Pure white `#FFFFFF`** — header strip, card surfaces.
  - **Cool light gray `#F0F0F5` / `#F8F8FA`** — page wrappers (`SpanishBackground`, `BgGray`).
- The only "decorative" surfaces are **horizontal gradient course-card headers**, where the card's accent color is paired with a 0.72α version of itself (`accentColor → accentColor.copy(.72f)`).
- Onboarding gets a **vertical gradient** of `Terracotta.copy(.15f) → background` — soft, nearly invisible, only at the top.
- Locked / disabled states use a flat gray gradient `#DDDDDD → #CCCCCC`.

### Animation

- **Spring physics over duration-based easing.** Bottom-nav icons scale to 1.08 with `Spring.DampingRatioMediumBouncy`; flashcard flip uses `Spring.DampingRatioLowBouncy + StiffnessLow`.
- **`tween(200)` for color crossfades** (selected/unselected nav).
- **Lottie** is used for one moment only — the success animation at end-of-flashcard-session (`lottie.host` URL, 160dp).
- **Infinite transitions** drive the streak flame pulse: 1.0 → 1.12 scale, 700ms `FastOutSlowInEasing`, reverse repeat.
- **AnimatedContent** with horizontal slide + fade for onboarding pagination, scale+fade for review-button reveal.

### Hover / press / focus states

- Bottom nav uses `clickable(indication = null)` — **no ripple**, just the spring-scale + color crossfade.
- Cards use **`shadowElevation` 1–6dp** as the press affordance; locked cards drop to 2dp.
- Buttons use Material3 default ripple (no override).
- Selected radio rows on onboarding tint the surface to `Teal.copy(.12f)` and switch to a colored border.

### Borders & dividers

- Hairlines are **0.5dp / 0.5px** (`HorizontalDivider thickness = 0.5.dp` above bottom nav).
- Thicker borders are **1.5dp** on white-on-white "month" pricing cards (border `#E5E5EA`).
- Default border color: **`#E5E5EA`** (`AppColors.BorderColor`).

### Shadows / elevation

- Card shadows are **purple-tinted** in spec: `spotColor = accentColor.copy(.35f)` at 6dp for active cards, 2dp for locked. We translate that to `0 6px 24px rgba(123,47,190,0.18)` for the design system preview.
- `tonalElevation` is rarely used outside Material3 surfaces; primary depth comes from `shadowElevation`.
- Rows inside expanded cards get a single 1dp shadow (`shadowElevation = 1.dp`).

### Capsules vs. protection gradients

- EASPEAK uses **opaque pills**, not blur or scrim layers. The header on home is white-on-white with a hairline divider; nothing floats over content with a gradient mask.
- Stats pills are `--stat-gold-bg` (`#FFF3E0`) with a 1dp colored border and a small emoji-prefix.

### Layout rules

- **14dp horizontal page padding** for full-bleed cards; **20dp** for inner content.
- Cards are **`fillMaxWidth().padding(horizontal = 14.dp)`** — never edge-to-edge.
- Bottom nav is **62dp** tall + nav-bar inset.
- Course/topic cards have a **100dp gradient header** + a body of variable height; the header is the dominant visual.
- All cards are 20dp radius. The Path-to-Madrid card uses 28dp.

### Transparency & blur

- Used sparingly. Only places:
  - White circle behind topic emoji on a colored header: `Color.White.copy(alpha = 0.25f)`.
  - Locked icons: white at 0.8 alpha on disabled gradient.
  - Tag chips: `accentColor.copy(.12f)` for the soft fill.
- **No `BackdropFilter` / `blur` modifiers anywhere.** The system reads as crisp, not glassy.

### Imagery / photography

- **None ships.** Coil is wired up for user avatar photos only (Google Sign-In photoUrl). Word-of-day, course cards, and topic blocks are emoji + type. The `assets/word_images/` folder has ~280 hand-generated word illustrations (PNG ~1.5MB each), but these aren't a brand visual system — they're per-word vocabulary props.

### Corner radii summary

- Buttons / fields: `12px` (Material3 `Shapes.small`).
- Sub-lesson rows, fields: `14px`.
- Generic cards: `16px` (Material3 `Shapes.medium`).
- Topic / streak / word-of-day / course cards: **`20px`** (the signature radius).
- Path-to-Madrid card: `28px`.
- Pills / badges: `8px` for chips, full pill (`9999px`) for stats.
- Shapes.large = `24px` for sheets.

### Card anatomy

A "topic card" is the most distinctive component:

```
┌──────────────────────────────────────────┐  20dp radius
│  [colored gradient header, 100dp tall]   │  shadow-card-lg
│   emoji-avatar  Block #N                 │
│                 BLOCK TITLE         A1   │
│                                          │
├──────────────────────────────────────────┤
│ description (13sp gray)                  │
│                                          │
│ ━━━━━━━━━━━━━━━━━━━━━ 75% [accent]      │
│                                          │
│ "Начать обучение →" (12sp accent bold)   │
└──────────────────────────────────────────┘
```

Locked variant: header swaps to gray gradient, body text dims to `gray@.55`, and the CTA becomes "Заблокировано" with a lock icon.

---

## ICONOGRAPHY

### Primary system: Material Icons (Compose)

The app uses `androidx.compose.material.icons` exclusively for line-art icons — `Outlined.*` for inactive states, `Filled.*` for active. Examples pulled from the bottom nav alone:

| Route        | Outline                          | Filled                         | Label       |
| ------------ | -------------------------------- | ------------------------------ | ----------- |
| `home`       | `Outlined.Home`                  | `Filled.Home`                  | Главная     |
| `games`      | `Outlined.Gamepad`               | `Filled.Gamepad`               | Игры        |
| `flashcards` | `Outlined.Style`                 | `Filled.Style`                 | Tarjetas    |
| `dictionary` | `AutoMirrored.Outlined.MenuBook` | `AutoMirrored.Filled.MenuBook` | Словарь     |
| `profile`    | `Outlined.Person`                | `Filled.Person`                | Профиль     |

Other Material icons spotted: `Lock`, `ExpandLess/More`, `Check`, `ChevronRight`, `Close`, `Settings`, `Stars`, `EmojiEvents`, `EmojiFlags`, `Leaderboard`, `Person`, `Error`, `CheckCircle`, `AutoAwesome`. **Stroke is consistent (Material 24dp)**; sizes vary 15–24dp.

In the web preview we substitute **Lucide** (CDN), which has the same 24×24 stroke geometry. **FLAG: substitution.** Where a Lucide name doesn't match (e.g. `EmojiEvents`), we pick the closest visual match.

### Secondary system: Emoji as illustration

Emoji are doing a lot of the iconography work too — see the CONTENT FUNDAMENTALS section. Treat them as part of the icon system, with sizes 14sp (inline), 22sp (small avatar in colored circle), 40sp (course feature), up to 80sp (welcome).

### Custom drawing

Mastery rating "Spanish flag" indicators (0–5) are rendered with `Canvas` directly in `RatingComponents.kt` — no raster assets. We do **not** recreate this in CSS; we substitute filled/outline flag emoji `🇪🇸` for previews.

### Logo

The single brand asset is `assets/logo.png` — a 512×512 square with the wordmark **EASPEAK** in white ExtraBold sans on an **orange→deep-orange diagonal gradient** (`#FF9500` → `#FF5722`), with a soft drop-shadow. This is **the only orange surface in the entire system** — the rest is purple-led. Treat the logo as a stand-alone artifact; it does not seed an "orange" UI direction.

### What we do **not** use

- No icon font (no Font Awesome, no IcoMoon).
- No SVG sprite sheet.
- No custom illustration set (no isometric scenes, no character mascot).
- No PNG icons in the UI (the `word_images/` folder is per-vocab-word reference imagery, not iconography).

---

## Notes / caveats for the reader

- This system documents the app **as it exists in the master branch on 2026-05-04** — actively developed by one author, still light on a formalized brand system. We surface the de-facto patterns, not aspirational ones.
- The **`AppColors` object has many legacy aliases** (`Olive`, `Terracotta`, `Ochre`, `Teal`, …) all pointing back to the purple/gold spectrum after the recent "Figma-style purple" redesign (commits `d742d6b`, `998447e`). Use the canonical names (`Purple`, `Gold`, `Orange`) and ignore the aliases.
- The bilingual Russian/Spanish copy is intentional — keep both languages visible side by side; do not translate one to match the other.
