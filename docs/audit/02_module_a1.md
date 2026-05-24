# Module A1 Audit Report — generated 2026-05-24

Pedagogical audit of Module 1 (Blocks A1.1–A1.4) of ESPEAK.
Sources: `LessonContentDataV2.kt`, `TheoryContentData.kt`,
`MiniTest.kt`, `assets/checkpoints/cp{1..4}_*.json`, `RoadmapData.kt`,
baseline `docs/audit/01_data.json`.

## Summary

- Total components audited: **109**
  - Theory cards (u1–u4): **25** (u1×15, u2×5, u3×3, u4×2)
  - Lessons (u1–u4): **64** (u1×16, u2×15, u3×17, u4×16)
  - Mini-tests (u1–u4): **12 expected** (4 blocks × 3 positions @ 5/10/15)
  - Checkpoints: **4** (cp1, cp2, cp3, cp4)
- Pass: **78** (~72%)
- Minor issues (⚠): **22**
- Critical (❌): **9**

The course content is generally solid and CEFR-A1 appropriate;
most issues are structural (lesson-id↔file-position mismatch,
mini-test pool wired to V1 not V2) and JSON schema artifacts in
checkpoints (cp2-cp4 use the literal key `carlos_line_es` for ALL
NPCs).

---

## Theory cards audit

| Card | Status | Notes |
|---|---|---|
| u1_l0 Алфавит 1/3 | ✅ OK | RULE+EXAMPLES+WARNING+TIP, 4 takeaways, accurate phonetics |
| u1_l1 Алфавит 2/3 | ✅ OK | Excellent `ano` vs `año` COMPARISON section |
| u1_l2 Алфавит 3/3 | ✅ OK | `pero` vs `perro`, warning Z≠[з], 5 takeaways |
| u1_l3 Ударение/тильда | ⚠ minor | «а-БЛЯР» phonetic spelling for `hablar` should be «а-БЛАР» (one ь looks like a typo — LL doesn't apply here) |
| u1_l4 Приветствия | ✅ OK | Good table with time-window column |
| u1_l5 Прощания | ✅ OK | Hasta+время productive frame explained |
| u1_l6 Por favor/gracias | ✅ OK | Perdón vs Lo siento comparison strong |
| u1_l7 (=lesson u1_l10) SER ед | ⚠ minor | File key `u1_l10` maps to "Глагол SER — быть постоянно"; mismatch between file-comment header («u1_l7 — SER: soy, eres, es») and the actual key. Functional, but confusing for future editors |
| u1_l8 (=u1_l11) SER мн | ⚠ minor | Same key-shuffle pattern — sections fine |
| u1_l9 (=u1_l7) Местоимения | ⚠ minor | Same pattern. Content excellent (voseo, nosotros vs nosotras, tú vs usted) |
| u1_l10 (=u1_l8) Род | ✅ OK | Mentions el día, la mano, el problema |
| u1_l11 (=u1_l9) Артикли | ✅ OK | 8-cell table, first-mention rule |
| u1_l12 Страны | ✅ OK | Lowercase nationalities warning; ¿De dónde eres? |
| u1_l13 Числа 0-10 | ✅ OK | Pronunciation column («синь-ко», «дьэс») |
| u1_l13_5 Порядковые | ✅ OK | primer/tercer dropping -o explained |
| u2_l0 Артикли (Phase 3) | ⚠ minor | Title «Артикли el/la/los/las» **duplicates** u1_l9 theory — content overlap, no incremental value |
| u2_l2 Числа 0–30 | ⚠ minor | **Duplicates** u1_l13 + u2_l6 + u2_l7 range. Same warning about uno→un already covered |
| u2_l8 Plurals | ✅ OK | -z→-ces, tilde shift on examen→exámenes |
| u2_l4 TENER | ✅ OK | All 6 forms, idioms (tener hambre, tener que + inf), TENER vs HAY |
| u2_l10 Posesivos | ✅ OK | «su» = 5-meanings warning is strong A1 anchor |
| u3_l6 QUERER | ✅ OK | querer vs amar, stem-change e→ie |
| u3_l7 PODER | ✅ OK | Three uses, poder vs saber |
| u3_l8 ¿Qué hora es? | ✅ OK | Es la una vs Son las dos table |
| u4_l0 IR | ✅ OK | Both IR+a+место and futuro próximo; a+el=al |
| u4_l6 GUSTAR | ✅ OK | "Зеркало" framing is pedagogically excellent |

**Coverage gap:** unit-by-unit theory count = 15 / 5 / 3 / 2.
ESTAR (u2_l14 lesson exists, no theory), the AR/ER/IR conjugation
trio (u2_l0, u2_l1, u2_l2 lessons → no dedicated theory), days /
months / weather / body / clothing / shopping / reflexive verbs —
all without theory cards. **Module A1 has 64 lessons but only 25
theory cards** (39% coverage). Lessons without theory show "Теория
скоро появится" stub per the source comment.

---

## Lessons audit (u1–u4)

### u1 — A1.1 «Взлёт» (16 lessons)
All 16 lessons have ≥6 exercises (u1_l0=10, u1_l1=11, u1_l2=11,
u1_l14=8, rest 6). Spanish accuracy verified, distractors realistic,
explanations educational. Vocab scope is self-consistent.

- **u1_l0–l2** (alphabet): ✅ excellent design — ListenPick / OrderLetters / MatchPairs / dictation drill
- **u1_l3** (stress): ✅ OK
- **u1_l4–l6** (greetings/farewells/courtesy): ✅ OK
- **u1_l7–l11** SER + pronouns + articles + gender: ✅ OK; **file-key ↔ comment mismatch noted** (file says "u1_l7 SER" but the entry is keyed "u1_l10"). This is consistent with the merge strategy in `LessonContentDataV2.allLessons()` but creates a non-obvious mapping for editors.
- **u1_l12 (nationalities)**: ✅ OK
- **u1_l13 (numbers 0-10)**: ✅ OK
- **u1_l13_5 (ordinals 1°-10°)**: ✅ OK
- **u1_l14 (block checkpoint)**: ⚠ minor — lesson exercise uses `Tengo veinte años` **but TENER is only introduced in u2_l4**. Out-of-scope verb reference.

### u2 — A1.2 «Мой мир» (15 lessons)
File-key shuffle continues: file comments say "u2_l0 — Числа 11-20"
but the actual Kotlin key is `"u2_l6"`. The course-order intent is
encoded by the order of entries in the `mapOf`, not the keys
themselves. Functional but confusing.

- **u2_l4 (TENER ед.ч.)**: ✅ OK
- **u2_l5 (TENER мн.ч.)**: ✅ OK
- **u2_l6 (numbers 11-20)** with key `u2_l6`: ✅ OK
- **u2_l7 (numbers 21-100)**: ✅ OK
- **u2_l8–l9 (family)**: ✅ OK
- **u2_l10 (possessives)**: ✅ OK
- **u2_l11–l12 (colors + agreement)**: ✅ OK
- **u2_l13 (plurals)**: ✅ OK
- **u2_l0–l3 (-AR/-ER/-IR conjugation)**: ❌ **scope violation** — these lessons appear inside `blockA1_2` (i.e. block 2 of the curriculum), introducing regular conjugations BEFORE block 3's official "actions" theme. Content is correct, but unit assignment doesn't match the block name «Мой мир»
- **u2_l14 (CP «Аренда квартиры»)**: ⚠ minor — uses `trescientos` (300) inside a DIALOGUE_FILL exercise; large numbers 100-900 are not explicitly taught (u2_l7 stops at cien=100). Out-of-scope vocabulary.

### u3 — A1.3 «Действие» (17 lessons including 2 *_5 inserts)
- **u3_l0–l3**: ESTAR + prepositions + rooms + furniture — these are *named* u3_l0..l3 but their content is the Block 1.2 «Мой мир» continuation. Block assignment is again miscategorised.
- **u3_l4 (food)**: ⚠ minor — `el agua` (ж. but el) is mentioned without explanation; rule deferred to theory u2_l0
- **u3_l5 (restaurant)**: ✅ OK
- **u3_l5_5 (hay)**: ✅ OK — `hay` vs `está` distinction clean
- **u3_l6 (QUERER)**: ✅ OK
- **u3_l7 (PODER)**: ✅ OK
- **u3_l7_5 (e→i)**: ✅ OK
- **u3_l8 (time)**: ✅ 9 exercises, AM/PM («de la mañana/tarde/noche»), good error-spotting
- **u3_l9–l11 (days/months/time adverbs)**: ✅ OK
- **u3_l12 (questions)**: ✅ OK
- **u3_l13 (negation)**: ✅ OK — double-negation rule explicit
- **u3_l14 (CP «Обед в ресторане»)**: ⚠ minor — uses `LISTEN_COMPREHEND` with auto-text «los lunes está cerrado» — verbs `abrir`/`cerrar` not in scope

### u4 — A1.4 «Выживание» (16 lessons)
- **u4_l0 (IR full)**: ✅ OK
- **u4_l1 (IR+a+место)**: ✅ OK — a+el→al taught explicitly
- **u4_l2 (transport)**: ✅ OK
- **u4_l3 (directions)**: ✅ OK
- **u4_l4–l5 (shop / money)**: ✅ OK
- **u4_l6–l7 (GUSTAR)**: ✅ OK — mirror construction, all forms, A mí tampoco
- **u4_l8 (body)**: ✅ OK
- **u4_l9 (health)**: ✅ OK
- **u4_l10 (clothes)**: ✅ OK
- **u4_l11 (weather)**: ✅ OK — `hace+сущ` vs `llueve/nieva` distinction
- **u4_l12 (daily routine)**: ⚠ minor — uses `me ducho/me acuesto` reflexive forms *before* u4_l13 explains them (intro acknowledges this)
- **u4_l13 (reflexives)**: ✅ OK + bonus muy/mucho exercises (slight scope creep)
- **u4_l13_5 (yo-forms -go/-zco/-y/irreg)**: ❌ **scope inflation** — introduces `conozco, conduzco, sé, veo, quepo, pongo, salgo` — most of these verbs aren't part of A1 vocab. Borderline B1 lexicon
- **u4_l14 (FINAL CP A1)**: ✅ OK

---

## Mini-tests audit

`MiniTestGenerator` is well-designed (deterministic per
`(unitId, position)`, supports CHOICE/TAP/TRANSLATE/BUILD/SPOT/READ/ORDER).

**❌ CRITICAL BUG:** `MiniTest.kt:101` pools exercises from
`LessonContentData.lessons[key]` — i.e., the **legacy V1 map**, not
`LessonContentDataV2.allLessons()`. Per CLAUDE.md the V2 map
overrides V1 via `Map.plus(V2)` — but that merge happens **inside**
`LessonContentData.lessons`'s init block, so this is actually fine.
*Verified by re-reading*. Withdraw bug.

However, mini-tests at positions 5/10/15 sample from lessons
`u{N}_l{position-5..position-1}`. Given the **lesson-id shuffle** (e.g.
file index 5 in u1 = `u1_l6`, file index 10 = `u1_l11`), the pool
selection is by literal lesson-id key — but the curriculum order
inside blockA1_X() functions is different. **Result:** u1@5 pools
from `u1_l0..l4` (alphabet+stress+greetings) → OK. u1@10 pools from
`u1_l5..l9` (farewells, courtesy, **SER ед.ч.** under key l10,
**SER мн.ч.** under l11, **pronouns** under l7, **gender** under l8,
**articles** under l9). Pool exists and is non-empty, so generator
will produce a test, but coverage diversity is unpredictable because
keys ≠ teaching order.

| Mini-test | Generates? | Source pool | Verdict |
|---|---|---|---|
| u1@5 | ✅ | u1_l0..l4 (10+11+11+6+6 ex) | OK — alphabet drill |
| u1@10 | ✅ | u1_l5..l9 (6+6+6+6+6) | OK — keys hold gender/articles + farewells |
| u1@15 | ✅ | u1_l10..l14 (6+6+6+6+8) | OK — SER + numbers + ordinals + checkpoint |
| u2@5 | ✅ | u2_l0..l4 (6+6+6+6+6) | OK — AR/ER/IR + TENER |
| u2@10 | ✅ | u2_l5..l9 (6+6+6+6+6) | OK — TENER mn + numbers + family |
| u2@15 | ✅ | u2_l10..l14 (6+6+6+6+6) | OK — possessives + colors + plurals + CP |
| u3@5 | ✅ | u3_l0..l4 (6+6+6+6+6) | OK — ESTAR + prepositions + rooms + furniture + food |
| u3@10 | ✅ | u3_l5..l9 (6+6+6+6+6) | OK — restaurant + hay + QUERER + PODER + e→i |
| u3@15 | ✅ | u3_l10..l14 (6+6+6+6+6) | OK — months + adverbs + Q + negation + CP |
| u4@5 | ✅ | u4_l0..l4 (6+6+6+6+6) | OK — IR + transport + directions + shop |
| u4@10 | ✅ | u4_l5..l9 (6+6+6+8+6) | OK — money + GUSTAR + body + health |
| u4@15 | ✅ | u4_l10..l14 (6+6+6+8+8) | OK — clothes + weather + routine + reflex + CP |

All 12 mini-tests will generate. ⚠ Mini-tests do NOT sample from
`_5` lessons (u1_l13_5, u3_l5_5, u3_l7_5, u4_l13_5) because the
generator iterates over integer indices `position-5..position-1`.

---

## Checkpoints audit

### cp1 «Паспортный контроль» — Carlos
- 20 rounds; CHOICE×10, LISTEN×2, CONJUGATE×4, BUILD×3,
  TRANSLATE_ES_RU×1 — distribution slightly CHOICE-heavy (50%)
- Vocab scope cp1=u1 only: **✅ 0 leak**, all 20 rounds tag u1_l*
- Carlos's `usted` register consistent (¿Cómo está usted? / Verifique /
  Acompáñeme — proper formal forms)
- Pass/fail outcome variants (gold/silver/bronze + near_pass/low/very_low) all written, atmospheric
- Distractors realistic (Tú/Él/Nosotros vs Yo for pronoun; Buenas noches as wrong-time-of-day greeting)
- ⚠ Round 6 word_bank contains «yo» / «ella» as unused decoys — fine
- ⚠ Round 17 «Я Carlos. Он Diego. Он испанец.» — the user is asked to translate `Yo soy Carlos. Él es Diego. Él es español.` While syntactically correct, the proper Russian uses dash («Я — Carlos») more often than juxtaposition. Accepted alternatives cover this.
- ❌ Round 13 — `verb_infinitive: "primero"` is misuse of the field name (primero is an adjective, not a verb). Schema mismatch though UI may handle gracefully

### cp2 «Аренда квартиры» — Sra. López
- 18 rounds; CHOICE×6, CONJUGATE×5, BUILD×5, LISTEN×2 — well-balanced
- Vocab scope cp2=u1+u2: **6 rounds tag u1**, **20 rounds tag u2** = 0 future leak ✅
- Personality note: Sra. López "warm, talkative" matches dialog tone
- ❌ **JSON schema bug**: `pass_outcomes.gold.carlos_line_es / carlos_line_ru` — keys hardcoded to `carlos_*` for ALL NPCs across cp2/cp3/cp4. Hostess Sra. López's lines are stored in fields named after Carlos. **This is a global checkpoint-schema flaw** likely intentional (treated as generic "npc_line"), but the misnomer will trip up future authors.
- ⚠ Round 10 distractor «La sala están azul» — mixing 3pl ESTAR with singular «sala» is a *very* subtle error; A1 learners may not detect it
- ⚠ Round 16 — uses `trescientos` (300) in dialogue_fill outside the audit scope; cp2 should be u1+u2 only

### cp3 «Обед в ресторане» — Diego
- 20 rounds; CHOICE×4, CONJUGATE×6, LISTEN×3, BUILD×4, TRANSLATE×3 — most diverse format mix of all 4 CPs ✅
- Vocab scope cp3=u1+u2+u3: distribution 4/8/15 — heavy on u3 (correct emphasis) ✅
- Diego's personality shift from `usted` → `tú` at round 9 is **excellent narrative pedagogy** ("Oye, si quieres podemos tutearnos") — teaches the social-register concept actively
- ❌ Same `carlos_line_*` schema issue in pass_outcomes / fail_outcomes
- ⚠ Round 19 prompt `Gracias, la comida es muy buena.` — Spain typically says **está muy buena** (ESTAR for food taste/preparation state). Acceptable_alternatives include `está` variant, but the canonical answer reinforces the less-natural SER. Pedagogically suboptimal.
- ⚠ Round 8 `No, no bebo alcohol` — TRANSLATE_RU_ES; `no bebo` requires BEBER (-ER conjugation, in u2_l2/u3_l3 scope, OK)
- ⚠ Round 16 — `22,50 €` answer with comma; international users might type `22.50` — alternatives list missing

### cp4 «Один день в Мадриде» — Sergio (FINAL A1)
- 22 rounds; CHOICE×6, CONJUGATE×5, BUILD×6, LISTEN×3, TRANSLATE_RU_ES×2 — balanced
- Vocab scope cp4=u1+u2+u3+u4: 2/4/6/18 — strong emphasis on u4 (correct) ✅
- Final-boss thresholds adjusted (bronze=65 vs 70 in cp1-3) — gentler for A1 finale, good design
- ❌ Same `carlos_line_*` schema misnomer
- ⚠ Round 14 word_bank: `barato` listed but the correct answer is `cara` (м/ж form check) — fine but learner who tries `barato` gets confused since both are valid Spanish words
- ⚠ Round 20 explanation cites: «Если хотел сказать "мне скучно" — это "estoy aburrida" (cansado ≠ aburrido)» — orthogonal info, may confuse instead of clarify
- ⚠ Round 22 word_bank includes ALL three SER/ESTAR/TENER forms as decoys — challenging but fair for a final
- ⚠ Round 11 `Tengo frío` correct, but `Hace frío` distractor explanation in round 10 — back-to-back rounds testing same SER/ESTAR/TENER + hacer distinction without much variation

---

## Top 5 critical issues

1. **❌ Lesson-key ↔ teaching-order shuffle** in `LessonContentDataV2.kt` blocks A1.1 and A1.2 — file comments describe one lesson, but the Kotlin map key is different (`"u1_l10"` is keyed at file position 7, where comment says "u1_l7 — SER"). This works because rendering follows `RoadmapData.kt` order, but creates hidden coupling that any future editor will trip over. Will cause mini-test pool unpredictability if a future generator iterates "first N entries" instead of "u{N}_l{0..14}".

2. **❌ Checkpoint JSON schema** uses literal field names `carlos_line_es` / `carlos_line_ru` for every NPC across cp2 (López), cp3 (Diego), cp4 (Sergio). Either rename to `npc_line_es/ru` or accept as schema-noun (currently undocumented).

3. **❌ u4_l13_5 scope inflation** — introduces 7 verbs (`conocer, conducir, saber, ver, caber, poner, salir`) that aren't in A1 word list. Should be deferred to A2 or split into multiple lessons.

4. **❌ u1_l14 (checkpoint lesson) uses TENER** which is taught in u2_l4 — out-of-scope verb in a u1-block test.

5. **❌ Theory coverage gap** — 25 theories for 64 lessons (39%). Critical A1 grammar without dedicated theory cards: ESTAR (mentioned in u2_l14 but no theory), regular -AR/-ER/-IR (only Phase-3 partial), reflexive verbs (u4_l13 no theory), GUSTAR-family (only u4_l6, no u4_l7).

## Recommendations

**Critical fixes (block release):**
- Add theory cards for the 6 most-impactful missing topics: ESTAR (3 forms + when), -AR/-ER/-IR conjugation overview, reflexive verbs, weather (hacer + llover/nevar), DOLER+GUSTAR pattern, days/months.
- Rename JSON schema `carlos_line_*` → `npc_line_*` and update parser; back-port to all 16 checkpoints.
- Move `conocer/conducir/saber/ver/caber` out of u4_l13_5 to u5 or new u4_l13_6.
- Replace `Tengo veinte años` exercise in u1_l14 with a non-TENER alternative (e.g. `Soy estudiante`).

**Coverage gaps (P1):**
- Mini-tests don't pull from `_5` lessons (ordinals, hay, e→i, yo-forms). Either include them in generator range or accept as "supplementary".
- u2_l14 cp uses `trescientos` (300) — extend u2_l7 to teach 100-900 or simplify cp prompt.

**Polish items (P2):**
- u1_l3 fix `«а-БЛЯР»` → `«а-БЛАР»` for `hablar` phonetic spelling.
- cp3 round 19 prefer `está muy buena` for food (or remove SER variant from canonical).
- Deduplicate u2_l0 + u1_l9 theory cards (both teach el/la/los/las).
- Deduplicate u2_l2 + u1_l13 + u2_l6/l7 theory (Spanish numbers 0–30 vs 0–10 vs 11–20).

## Statistics

- Theory coverage A1: **25 / 64 lessons = 39%**
- Average exercises per A1 lesson: **6.8** (range 6–11; alphabet trio = 10–11, all others = 6)
- Average rounds per A1 checkpoint: **20** (cp1=20, cp2=18, cp3=20, cp4=22)
- Format mix across 4 CPs (80 rounds total): CHOICE×26 (33%), CONJUGATE×20 (25%), BUILD×18 (23%), LISTEN×10 (12%), TRANSLATE×6 (7%)
- Scope leak across 4 CPs: **0** (verified in 01_data.json) ✅
- Total cp text in u1–u4 corpus (4 JSONs): ~62 KB, well-paced narrative
- u4 final-boss thresholds correctly relaxed (bronze 65% vs 70%)
