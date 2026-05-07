---
name: espeak-design
description: Use this skill to generate well-branded interfaces and assets for EASPEAK — a Russian-language Android app for learning Spanish (CEFR A1 → B2) — either for production or throwaway prototypes/mocks. Contains essential design guidelines, colors, type, fonts, assets, and UI kit components for prototyping.
user-invocable: true
---

Read the README.md file within this skill, and explore the other available files.

If creating visual artifacts (slides, mocks, throwaway prototypes, etc), copy assets out and create static HTML files for the user to view. If working on production code, you can copy assets and read the rules here to become an expert in designing with this brand.

If the user invokes this skill without any other guidance, ask them what they want to build or design, ask some questions, and act as an expert designer who outputs HTML artifacts _or_ production code, depending on the need.

## Quick map of this skill

- `README.md` — context, content fundamentals, visual foundations, iconography. Read this first.
- `colors_and_type.css` — every CSS var (colors / type / radii / spacing / shadows / gradients).
- `assets/` — logo (`logo.png`).
- `preview/*.html` — small spec cards (color, type, components) for visual reference.
- `ui_kits/espeak_app/` — JSX recreation of the app (Home, Course detail, Flashcards, Profile, Onboarding) inside an Android frame.
- `app/` — read-only subset of the original Kotlin/Compose source (theme, components, screens) for ground-truth.

## Working principles

- **Bilingual is the brand.** Russian UI copy with Spanish accents (`Tarjetas`, `Mi Perfil`, `¡Hola!`). Never translate one to match the other.
- **Use "ты", not "Вы".** Direct imperatives, no marketing fluff.
- **Purple `#7B2FBE` is the hero.** Gold/orange is for stats only. Logo is the only orange surface.
- **Light theme only.** No dark mode ships.
- **Emoji are first-class iconography.** Sized 14sp inline → 80sp as feature. Material Icons (filled vs outlined) carry the rest. We substitute Lucide on the web.
- **Cards = 20px radius, purple-tinted shadow.** Course/topic cards get a 100dp/72dp gradient header.
