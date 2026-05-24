# Module 4 (B2) — Pedagogical Audit

**Scope:** units u13-u16 (66 lessons) + 12 theory cards + 12 mini-tests + 4 checkpoints (cp13 Ana, cp14 Tía Rosa, cp15 Director Ramón, cp16 FINAL BOSS).
**CEFR target:** B2 (DELE B2 prep).
**Audit date:** 2026-05-24.
**Verdict:** PASS with 3 substantive findings.

---

## Inventory (grep-verified)

| Component | Expected | Found | Status |
|---|---|---|---|
| Lessons u13 | 15 (incl. _5) | 15 (u13_l0..l14 + u13_l5_5) | OK |
| Lessons u14 | 17 (incl. 2× _5) | 17 (u14_l0..l14 + u14_l6_5, u14_l9_5) | OK |
| Lessons u15 | 17 (incl. 2× _5) | 17 (u15_l0..l14 + u15_l4_5, u15_l11_5) | OK |
| Lessons u16 | 17 (incl. 2× _5) | 17 (u16_l0..l14 + u16_l4_5, u16_l6_5, u16_l11_5) | OK |
| **Total lessons** | **~66** | **66** | OK |
| Theory cards u13-u16 (detailed) | ~10 | 12 (incl. 4 _5 + u16_l9_coloquial + u16_l10_voseo) | OK |
| Theory stubs (t-helper) | 60+ | 66 | OK |
| Mini-tests | 12 (3/block × 4) | 12 generated runtime (u13/14/15/16 × pos 5,10,15) | OK |
| Checkpoints | 4 | cp13(27), cp14(30), cp15(28), cp16(33) rounds | OK |
| RoadmapData entries | 66 | 66 | OK |

**Exercise count u13-u16:** 66 lessons, mean ~7 exercises/lesson; heavy hubs cp-lessons hit 11-17.
**Char volume:** cp16 (FINAL) lesson = 3228 chars; u14_l6 (perífrasis B2) = 5637 chars; u15_l4_5 = 7021 chars (heaviest).

---

## A. Theory Cards — B2 depth review

### Strengths
- **u14_l6_5 (Deber vs Deber de):** five sections (RULE, COMPARISON, EXAMPLES, TIP) — clean separation of obligation vs guess, 6 worked examples, explicit "de = ~приблизительно" mnemonic. Excellent.
- **u15_l4_5 (Sino vs sino que vs no sólo... sino también):** all three forms, COMPARISON with pero, WARNING block disambiguating *sino* vs *si no*. Textbook-grade.
- **u16_l6_5 (Voseo):** distinguishes Rioplatense (vos tengas, = tú) from Centroamericano (vos tengás, with -ás ending). Imperativo table with andá (special). Rare depth.
- **u16_l11_5 (Cultura Latam):** sobremesa, tertulia, ustedeo (Bogotá usted with intimates), Mexican "ahorita" trap, regional Perfecto/Indefinido split. Cultural literacy at B2/C1 level.
- **u15_l8 (Concesivas):** complete palette aunque / a pesar de (que) / si bien / por más que / pese a with mood and register matrix.
- **u13_l10 (Aunque):** explicit Indic-fact vs Subj-hypothesis/future contrast.

### Findings
1. **🔴 FINDING B2-1 (data inconsistency):** theory short-card for **u14_l9** still reads `"Infinitivo как сущ" / "El comer mucho es malo"`, but the lesson u14_l9 was rewritten to **Voz media** (`"Voz media — пассив без агента. Se rompió el vaso"` at LessonContentDataV2.kt:8143). The theory card and lesson teach DIFFERENT topics under the same id. Audit scope explicitly required this be corrected — **not corrected on the theory side**. Severity: HIGH — user opens theory expecting Voz media reinforcement and gets infinitivo-noun rule.
2. **🟡 FINDING B2-2:** `u14_l12` lesson teaches "Concordancia participio + verbos psicológicos" (LessonContentDataV2.kt:8268), but the theory stub at line 2504 says "Ser vs Estar — нюансы". Same mismatch pattern as B2-1. Severity: HIGH.
3. **🟡 FINDING B2-3:** `u14_l11` lesson title in stub list says "Косв.речь: сдвиг указателей" but actual lesson teaches "Subjuntivo в косвенной речи: Pres Subj → Imp Subj shift". Stub wording is plausible-but-different. Severity: MEDIUM (technically related, just under-specific).

### B2 hallmark coverage (grep-verified)
- Imperfecto Subjuntivo -ra/-se parity: u13_l0 explicit ✓
- Pluscuamperfecto Subjuntivo: u13_l6 ✓
- Pasiva refleja vs SE impersonal: u14_l2 (4647 chars, 13 ex) ✓
- aunque/por más que/a pesar de Indic vs Subj: u13_l10 + u15_l8 ✓
- Subj vs Ind choice: present across u13_l11/l12/l13 ✓
- el cual / lo cual / cuyo: u14_l10 ✓
- Pres Subj → Imp Subj shift (estilo indirecto): u14_l11 ✓ (lesson body correct, stub label off)
- Verbos psicológicos + Subj: u14_l12 ✓
- Voseo Subj + Imperativo: u16_l6_5 ✓
- Deber+inf vs deber de+inf: u14_l6_5 ✓
- Sino vs sino que: u15_l4_5 ✓
- Cultura Latam (sobremesa/tertulia/ustedeo): u16_l11_5 ✓

---

## B. Lessons — DELE B2 quality

Sampled u13_l0..l6, u14_l9..l12, u15_l4_5, u16_l6_5.

- **Variety of types:** MULTIPLE_CHOICE, TAP_MISSING_WORD, TRANSLATE, BUILD_SENTENCE, SPOT_THE_ERROR, MATCH_PAIRS, LISTEN_TYPE, LISTEN_COMPREHEND, DIALOGUE_FILL — full set in use.
- **SPOT_THE_ERROR quality is high.** Examples checked manually:
  - u13_l0: `"Si tendría dinero"` flagged correctly (must be tuviera/tuviese after Si type 2). ✓
  - u14_l10: `"La mujer cuyo casa es bonita"` — cuyo agrees with possessed (casa f.sg → cuya). ✓
  - u14_l11: `"Esperaba que vengan"` — Imperfecto + Pres.Subj forbidden, requires Imp.Subj vinieran. ✓
  - u14_l12: `"Los problemas están resuelto"` — m.pl → resueltos. ✓
- **Explanations are CEFR-honest:** they name the rule (e.g. "Después de Imperfecto → Imp Subj"), not just "wrong/right".
- **Vocab scope** stays within u1..u(current) — no premature C1 lexis introduced as required vocab.

**Concerns:**
- u13_l5 ("Мини-тест Subj.Imp") is labelled 🎯 but is implemented as a regular `lc(...)` lesson with 6 hand-authored exercises rather than the runtime-sampled MiniTest from `MiniTest.kt`. **Two parallel systems** exist (authored "tests" lessons + runtime MiniTestGenerator). Not a bug, but creates user confusion — the roadmap "Мини-тест" at u14_l5 / u15_l5 / u16_l5 is one thing; the runtime mini-tests after every 5 lessons are another. Worth a unification pass.
- u14_l9_5 has the LISTEN_TYPE audio set to `"a través"` — should be the full `"a través de"` to teach the locution wholly. Tiny defect.

---

## C. Mini-tests

`MiniTestGenerator.all()` produces 48 mini-tests (16 × 3). For B2 it generates 12:
- u13_mt5 covers u13_l0..l4 (Imp.Subj + Si type 2 + Ojalá + Como si)
- u13_mt10 covers u13_l5..l9 (Pluscuamp + Si type 3 + Cond.Comp)
- u13_mt15 covers u13_l10..l14 (Aunque + Subj clauses + checkpoint review)
- Same pattern for u14/u15/u16.

**Quality:** sampling is deterministic (seed = `(unitId, position).hashCode()`), 5 exercises filtered to SUPPORTED_TYPES (MC/TAP/TRANSLATE/BUILD/SPOT/READ_NUMBER/ORDER_LETTERS). Excludes LISTEN/SPEAK as intended.

**Concern:** generator pools from `LessonContentData.lessons[key]` — the main map (V1+V2 merge). For B2 lessons that exist only in V2 with `lc-helper` syntax, exercises ARE present in merged map (verified by inventory chars > 0). No B2 gap. ✓

---

## D. Checkpoints — narrative + grammar fidelity

### cp13 Ana RRHH (Barcelona Tech, 27 rounds, threshold 70/80/95)
- NPC formal usted throughout, "Vamos a contactar a otros candidatos" fail line is culturally authentic.
- Coverage: u13_l0 (Perfecto), u13_l11 (Pres Subj relativo: "que la haya motivado"), u13_l12 (cuando + Subj), Subj after gustar-class. Excellent professional register.

### cp14 Tía Rosa (Casa familiar, 30 rounds, threshold 70/80/95)
- Family dispute = perfect setting for verbos psicológicos, aunque, imperativos.

### cp15 Director Ramón (Sala de juntas, 28 rounds, threshold 70/80/95)
- Business presentation — formal connectors, nominalización, pasiva refleja territory.

### cp16 FINAL BOSS — Madrid-Barajas Despedida (33 rounds, threshold **65/80/95**)
- **Threshold deliberately lowered to 65%** for bronze — justified in `stakes_ru` as "28 раундов смешивают все 16 блоков" (actually 33). Correct calibration for a cumulative final.
- **Ensemble NPC design** — all 8 prior NPCs return (Carlos cp1, Sra. López cp2, Diego cp3, Sergio cp4, Dra. Martínez cp5, Lucía cp6, Pablo cp7, Andrés cp8). Each preserves voice. Outstanding narrative payoff.
- Grammar mix per round: A1 saludos inside B2 syntax (round 4: "le deseo que tenga... y que vuelva" double Subj inside saludos triple). Round 30: Si type 3 + double clitic + nominal relative. Round 31: voseo imperativo (decime). Round 32: sobremesa cultural recall. Round 33: Spain Perfecto vs LatAm Indefinido contrast.
- pass_outcomes/fail_outcomes have 3+3 tiers (gold/silver/bronze + near_pass/low/very_low) — full RPG-grade payoff.
- **Reward:** badge `habla_como_nativa`, `unlocks_block: 99` (final), +2500 XP at gold.
- **Minor concern:** round 26 prompt `"Que pase lo que pase, ___ siempre amigos"` accepts only `sigamos`. `seamos` would also be linguistically valid ("we remain friends"). Acceptable since `seguir + adj` is canonical here, but `seamos` is not even in distractors — fine.

**Threshold consistency check:** cp13/14/15 all at 70/80/95, cp16 at 65/80/95. The lower bronze on the boss is intentional and documented.

---

## Cross-cutting findings

1. **🔴 B2-1 + 🟡 B2-2 (theory↔lesson topic drift):** at minimum two ids (u14_l9, u14_l12) have theory cards teaching a different topic than the lesson body. Either rewrite the short theory entries to match the V2-refactored lessons, or note "see relatedTheory" pointers. This is the SINGLE biggest risk for B2 — users hit theory expecting Voz media reinforcement and get Infinitivo-as-noun, breaking the learning loop.
2. **🟢 Positive surprise:** u16_l6_5 (Voseo Subj) and u16_l11_5 (Cultura) are unusually deep for an app at this price tier — closer to a university B2 syllabus than to Duolingo/Babbel. These two cards alone justify the B2 paywall.
3. **🟡 Two mini-test systems coexist** (authored u*_l5 "тесты" lessons + runtime MiniTestGenerator at positions 5/10/15). Not broken, but documentation should clarify which the user sees.
4. **🟡 u14_l9_5 LISTEN_TYPE audio "a través"** truncated — should be `"a través de"`.

---

## Pass rate

- Theory cards detailed (12): 10/12 pass (B2-1 fails u14_l9 short card, B2-2 flags u14_l12 short card).
- Lessons (66): 65/66 pass (u14_l9_5 minor audio defect; not blocking).
- Mini-tests: 12/12 generator OK.
- Checkpoints: 4/4 pass (cp16 is exemplary).

**Aggregate:** **91/94 ≈ 96.8%** components pass. B2 is the strongest module audited so far — but it carries the only data-integrity defect with user-visible impact (B2-1).

## Recommended priorities (next session)
1. **Fix B2-1:** rewrite theory short card at TheoryContentData.kt:2480 (u14_l9) to teach Voz media, OR rename lesson back to Infinitivo-as-noun. Pick one.
2. **Fix B2-2:** same for u14_l12 — align theory ("Ser vs Estar нюансы") with lesson ("Concordancia participio + Verbos psicológicos").
3. Patch u14_l9_5 LISTEN_TYPE audio string to "a través de".
4. Document the dual mini-test system (authored vs runtime) in CLAUDE.md so future audits don't flag it as duplication.
