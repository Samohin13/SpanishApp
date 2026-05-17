# EASPEAK App — UI Kit

High-fidelity React/JSX recreation of the EASPEAK Android app, framed inside the Android device shell.

## Files

- `index.html` — interactive click-thru mounting point. Boot lands on Onboarding → Home → Course detail → Flashcard session → Profile.
- `Shared.jsx` — palette, gradients, icons, primitives (`StatPill`, `CefrBadge`, `Chip`, `BottomNav`, `ScreenHeader`).
- `OnboardingScreen.jsx` — welcome + name entry + level pick (3-page pager).
- `HomeScreen.jsx` — header w/ avatar + stat pills, greeting, streak card, word-of-day card, course cards (A1–B2).
- `CourseDetailScreen.jsx` — Path-to-Madrid card on top + roadmap of `TopicCard`s with expandable lesson rows.
- `FlashcardScreen.jsx` — front/back flip card + review buttons (Забыл / Помню / Легко) + session header.
- `ProfileScreen.jsx` — Mi Perfil header + LeagueBadge + stats grid + category mastery list.

## Visual sources

All values lifted directly from `app/src/main/java/com/spanishapp/ui/...`:

- `home/HomeScreen.kt` — course/topic cards, stat pills, premium sheet, streak card.
- `flashcards/FlashcardsScreen.kt` — flip mechanics, review buttons.
- `profile/ProfileScreen.kt` — league path, category ratings.
- `theme/Theme.kt` — `AppColors` and `AppTypography`.
- `components/Components.kt` — `BottomNavigationBar` and `XpBar`.

## Notes

- We swap Material Icons → Lucide CDN (closest stroke match). Documented in root `README.md → ICONOGRAPHY`.
- Spring animations are approximated with CSS easing; flashcard flip uses a CSS 3D transform.
- TTS, real auth, and league promotion dialogs are placeholders — visual only.
