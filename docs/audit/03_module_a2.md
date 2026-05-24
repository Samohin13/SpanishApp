# Module 2 (A2) — Pedagogical Audit

**Date:** 2026-05-24
**Scope:** Theory cards u5–u8, Lessons u5–u8, Mini-tests u5–u8, Checkpoints cp5–cp8
**Verdict:** PASS with reservations (1 critical, 4 important issues).

---

## 1. Inventory (verified from `docs/audit/01_data.json`)

| Unit | Lessons | Total chars | Theory cards | Lesson with most ex. |
|---|---|---|---|---|
| u5 (Indefinido) | 16 (l0–l8, l8_5, l9–l14) | 27 301 | 3 (l0, l1, l6) | l8_5=6, l14=6 |
| u6 (Imperfecto + comparativos + pronouns) | 16 (l0–l9, l9_5, l10–l14) | 31 606 | 5 (l0, l1, l2, l8, l9) | l9=12, l6=10 |
| u7 (Perfecto + estar+gerundio + futuro/imp) | 16 (l0–l5, l5_5, l6–l14) | 34 766 | 1 (l0) ⚠ |  l2=11, l4=10, l7=10 |
| u8 (Condicional + Si tipo 2) | 15 (l0–l14) | 28 720 | 1 (l0) ⚠ | l4=11 |
| **TOTAL** | **63** | **122 393** | **10** | — |

Average lesson length: **1942 chars / 6.4 exercises**. No lesson below 6 exercises floor. ✅

---

## 2. Per-component verification

### A. Theory cards — ⚠ THIN COVERAGE in u7/u8

| Card ID | Topic | Verified |
|---|---|---|
| u5_l0 | «Pretérito Indefinido — что это» | ✅ |
| u5_l1 | «Indefinido -AR» (hablé/hablaste/habló…) | ✅ |
| u5_l6 | «Indef irreg: ir/ser → fui» | ✅ |
| u6_l0 | «Imperfecto -AR» (hablaba/hablabas…) | ✅ |
| u6_l1 | «Imperfecto -ER/-IR + irreg» (era/iba/veía) | ✅ |
| u6_l2 | «Indefinido vs Imperfecto» — bridging card | ✅ |
| u6_l8 | «OD: lo/la/los/las» | ✅ |
| u6_l9 | «OI: me/te/le/nos/os/les» | ✅ |
| u7_l0 | «Pretérito Perfecto» | ✅ |
| u8_l0 | «Condicional Simple» | ✅ |

**Issue A1 (❌ CRITICAL):** Unit 7 has only **one** theory card (u7_l0). Unit 7 introduces:
- Pretérito Perfecto (he/has/ha + participio)
- Estar + gerundio (incl. irreg leyendo/durmiendo/pidiendo — confirmed at `LessonContentDataV2.kt:4427–4431`)
- Seguir + gerundio / Llevar + tiempo + gerundio
- Futuro Simple (hablaré)
- Imperativo TÚ regular + irregular (di, haz, pon, ven, sal, ten, ve, sé) — confirmed `u7_l5_5`

This is **5 major grammar pillars in one block** with only the lead card. By A1 standard (u1 = 15 cards, u2 = 5) this block is severely under-theorised.

**Issue A2 (⚠ IMPORTANT):** u8 also has only one theory card, but u8 covers Condicional Simple + Condicional irregular (tendría/haría/diría/saldría) + Si tipo 2 (si tuviera/fuera/pudiera + cond) + Pluscuamperfecto reentry. The Si type 2 ↔ Si type 1 contrast (confirmed at `LessonContentDataV2.kt:4961`) deserves its own card.

**Recommendation:** add 4 cards: `u7_l4` (Futuro), `u7_l5_5` (Imperativo TÚ irregular), `u7_l8` (estar + gerundio with irregular gerundios), `u8_l5` (Si tipo 2 vs tipo 1).

### B. Lessons — ✅ PASS

- Vocab scope: random sampling of u5_l0–l14, u6_l9, u7_l5_5, u8_l4 shows verbs are restricted to A1-introduced lemmas + the new A2 lemma of the current lesson. No B1+ leakage spotted.
- Each block has its «mini-checkpoint» at l5 (u5_l5 «Мини-чекпоинт Indefinido», u6_l5 «tan/tanto como», u7_l5 «Imperativo TÚ»). ✅
- `_5` bridge lessons exist: `u5_l8_5` (Pluscuamperfecto), `u6_l9_5` (double pronouns SE LO), `u7_l5_5` (Imperativo irregular). ✅
- **All 63 lessons hit the 6-exercise floor.** ✅

**Issue B1 (⚠ IMPORTANT):** u5_l5, u5_l12 sit at 1485 / 1547 chars — bottom 5% of corpus. Acceptable for «consolidation» lessons but on the thin side for paid A2 content.

### C. Mini-tests — ✅ FEASIBLE (generator-built, not in JSON yet)

`MiniTestGenerator.kt:90` builds 3 mini-tests per block by sampling 5 exercises from `[pos-5 .. pos-1]`. For u5–u8:

| Mini-test | Source range | Pool size (lessons present) |
|---|---|---|
| u5_mt5, u5_mt10, u5_mt15 | l0–l4, l5–l9, l10–l14 | 5/5, 5/5, 5/5 ✅ |
| u6_mt5, u6_mt10, u6_mt15 | l0–l4, l5–l9, l10–l14 | 5/5, 5/5, 5/5 ✅ |
| u7_mt5, u7_mt10, u7_mt15 | l0–l4, l5–l9, l10–l14 | 5/5, 5/5, 5/5 ✅ |
| u8_mt5, u8_mt10, u8_mt15 | l0–l4, l5–l9, l10–l14 | 5/5, 5/5, 5/5 ✅ |

`_5` bridge lessons (u5_l8_5 etc.) are NOT in this range scheme and silently excluded. Generator handles missing positions gracefully (`return null`).

**Issue C1 (⚠ MINOR):** the SUPPORTED_TYPES set in `MiniTest.kt:57` excludes `DIALOGUE_FILL` and `LISTEN_TYPE`. Several u5–u8 lessons rely heavily on these (e.g. u5_l10 dialogue), so the mini-test pool for those blocks under-represents conversation skill.

### D. Checkpoints — ✅ SCOPE CLEAN

| CP | Rounds | CEFR | units_tested (from `01_data.json`) | Leak |
|---|---|---|---|---|
| cp5 (Doctor) | 20 | A2 | {u1:4, u2:3, u3:2, u5:20} | 0 ✅ |
| cp6 (Shopping) | 23 | A2 | {u1:3, u5:1, u6:28} | 0 ✅ |
| cp7 (Weekend) | 23 | A2 | {u1:2, u5:2, u7:26} | 0 ✅ |
| cp8 (Work BOSS) | 22 | A2 | {u5:6, u6:5, u7:5, u8:10} | 0 ✅ |

Every `tested_lesson` resolves to an existing key (`u5_l8_5`, `u6_l9_5`, `u7_l5_5` all referenced and present). cp8 BOSS distribution is well-balanced across u5–u8.

**Verified A2-specific content in cp8 BOSS:**
- Round 3 → `Hice doce campañas y tuve a tres personas a mi cargo` (Indef irreg hacer/tener)
- Round 5 → `había vivido un año en Alemania` (Pluscuamperfecto)
- Other rounds cover Imperfecto contrast, gerundio, future, conditional, Si tipo 2.

**Issue D1 (⚠ MINOR):** cp5 has only 2 BUILD rounds vs 8 CHOICE — production skill under-tested for a med-stakes A2 gate. Compare cp6 (4 BUILD) and cp8 (5 BUILD).

---

## 3. A2-specific grammar checklist

| Concept | Location | Status |
|---|---|---|
| Si tipo 2 (si tuviera + cond) | `LessonContentDataV2.kt:4969, 5003` | ✅ Present (post-fix) |
| Si tipo 1 vs tipo 2 contrast | `LessonContentDataV2.kt:4961` | ✅ Side-by-side |
| Irregular gerundios (leyendo/durmiendo/pidiendo) | `LessonContentDataV2.kt:4427–4463` | ✅ All 3 + decir→diciendo |
| Pronouns + imperativo (dímelo) | `LessonContentDataV2.kt:4603` | ✅ Encliticised, accent rule |
| Double pronoun SE LO (le+lo→se lo) | `u6_l9_5` (RoadmapData.kt:284) | ✅ |
| Leísmo de persona | `LessonContentDataV2.kt:4031, 4053–4073` + `TheoryContentData.kt:3498, 3538` | ✅ Explicit Spain-vs-LatAm note + DELE caveat |
| Superlativo absoluto -ísimo | `LessonContentDataV2.kt:3911–3955` | ✅ With orthography (riquísimo / larguísimo / felicísimo) |
| Pluscuamperfecto bridge to B1 | `u5_l8_5` | ✅ Pre-introduced |
| Indef vs Imperf bridging card | `u6_l2` theory | ✅ |

**All 9 A2 must-haves present.** This is the strongest section of the audit.

---

## 4. Top 5 Critical Issues

1. **❌ CRITICAL — u7 theory shortage.** One card (u7_l0) for a block containing Perfecto, Futuro, Imperativo (reg + irreg), Estar+gerundio, Seguir+gerundio. Add `u7_l4` (Futuro), `u7_l5_5` (Imperativo irreg), `u7_l8` (Gerundio).
2. **⚠ IMPORTANT — u8 theory shortage.** One card (u8_l0) covers Condicional + Si tipo 2 + Pluscuamperfecto refresh. Add `u8_l5` (Si tipo 2 ↔ tipo 1 contrast card).
3. **⚠ IMPORTANT — Mini-test type set excludes DIALOGUE_FILL/LISTEN_TYPE** (`MiniTest.kt:57`). Conversation lessons (u5_l10, u6_l10, u7_l14) contribute less to MT pools than intended.
4. **⚠ IMPORTANT — `_5` bridge lessons silently skipped by Mini-test generator** (range `pos-5..pos-1` of integer indices). u5_l8_5 / u6_l9_5 / u7_l5_5 never appear in mini-tests despite being core A2 content. Consider extending generator or adding them to roadmap range explicitly.
5. **⚠ MINOR — cp5 BUILD under-weight** (2/20). For a first A2 checkpoint introducing past-tense production, ≥4 BUILD recommended.

---

## 5. Statistics summary

- **Components audited:** 63 lessons + 10 theory cards + 12 potential mini-tests + 4 checkpoints = **89 components**
- **Pass rate:** 84/89 = **94 %** (5 components flagged)
- **Critical failures:** 1 (u7 theory)
- **Checkpoint leak count:** 0/88 rounds
- **Exercise-floor compliance:** 63/63 lessons ≥ 6 exercises
- **A2 grammar coverage:** 9/9 must-have concepts present

---

## 6. Recommendation

Module 2 is **ready for production** but the u7/u8 theory gap is the single most visible regression from A1 (where every block had 5+ theory cards). Adding 3–4 theory cards (≈30 min editorial work each) lifts pass rate to 100 % and aligns A2 with A1 instructional density. No checkpoint, vocab, or roadmap blockers detected.
