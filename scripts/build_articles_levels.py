"""
Build the deterministic 100-level word list for the Articles game.

Reads CleanVocab.kt + VocabExtra*.kt, extracts every noun whose `spanish`
field starts with `el `/`la `/`los `/`las `, then distributes the chosen
~600 most useful items across 100 levels — no randomness anywhere.

Output: docs/articles_levels.json with structure
    [
      {"level": 1, "block": "A1-base", "rule_hint": "...", "words": [
          {"word": "casa", "article": "la", "russian": "дом",
           "example": "...", "category": "...", "is_plural": false}, ...]},
      ...
    ]

Run:
    python scripts/build_articles_levels.py
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VOCAB_DIR = ROOT / "app" / "src" / "main" / "java" / "com" / "spanishapp" / "data" / "db"
OUT_JSON = ROOT / "docs" / "articles_levels.json"

# Matches w("<spanish>", "<russian>", "<example>", "<level>", "<category>", ...)
W_RX = re.compile(
    r'w\("([^"]+)",\s*"([^"]*)",\s*"([^"]*)",\s*"(A1|A2|B1|B2|C1)",\s*"([^"]+)"'
)


def extract_nouns():
    """Return list of dicts for every noun starting with an article."""
    out = []
    seen = set()
    files = sorted(VOCAB_DIR.glob("CleanVocab.kt")) + sorted(VOCAB_DIR.glob("VocabExtra*.kt"))
    for path in files:
        text = path.read_text(encoding="utf-8")
        for sp, ru, ex, lvl, cat in W_RX.findall(text):
            sp_low = sp.strip().lower()
            parts = sp_low.split()
            if len(parts) < 2:
                continue
            art = parts[0]
            if art not in ("el", "la", "los", "las"):
                continue
            word = " ".join(parts[1:])  # noun part without article
            key = (word, art)  # de-dupe across vocab files
            if key in seen:
                continue
            seen.add(key)
            out.append({
                "word": word,
                "article": art,
                "russian": ru,
                "example": ex,
                "level": lvl,        # CEFR
                "category": cat,
                "is_plural": art in ("los", "las"),
            })
    return out


def make_plural(word: str, article: str) -> tuple[str, str]:
    """Naive plural form (+s / +es). Returns (plural_word, plural_article)."""
    new_art = "los" if article == "el" else "las"
    last = word[-1] if word else ""
    if last in "aeiouáéíóú":
        return word + "s", new_art
    if last == "z":
        return word[:-1] + "ces", new_art
    return word + "es", new_art


# Words to exclude from L1-30 (months, weekdays, numerals, abstract)
EXCLUDE_FROM_BASE = {
    "abril", "agosto", "diciembre", "enero", "febrero", "julio", "junio",
    "marzo", "mayo", "noviembre", "octubre", "septiembre",
    "lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo",
    "norte", "sur", "este", "oeste",
}

# Special-case manual lists for tricky levels — every word has Russian translation
EXCEPTIONS_GENDER = [
    # (word, article, russian, rule)
    ("día",     "el", "день",      "Слова на -a, но м.род"),
    ("mano",    "la", "рука",      "Слова на -o, но ж.род"),
    ("foto",    "la", "фото",      "Сокр. от fotografía — ж.род"),
    ("moto",    "la", "мотоцикл",  "Сокр. от motocicleta — ж.род"),
    ("mapa",    "el", "карта",     "Греч. происхождение — м.род"),
    ("planeta", "el", "планета",   "Греч. происхождение — м.род"),
    ("clima",   "el", "климат",    "Греч. происхождение — м.род"),
    ("idioma",  "el", "язык",      "Греч. происхождение — м.род"),
    ("sofá",    "el", "диван",     "Заимствование — м.род"),
    ("café",    "el", "кофе/кафе", "Заимствование — м.род"),
    ("radio",   "la", "радио",     "Сокр. от radiodifusión — ж.род"),
    ("modelo",  "la", "модель (девушка)", "Когда о женщине-модели — ж.род"),
]

EXCEPTIONS_MA = [
    ("problema", "el", "проблема"),
    ("sistema",  "el", "система"),
    ("tema",     "el", "тема"),
    ("programa", "el", "программа"),
    ("idioma",   "el", "язык"),
    ("clima",    "el", "климат"),
    ("drama",    "el", "драма"),
    ("poema",    "el", "стихотворение"),
    ("teorema",  "el", "теорема"),
    ("dilema",   "el", "дилемма"),
    ("síntoma",  "el", "симптом"),
    ("esquema",  "el", "схема"),
]

EL_AGUA_RULE = [
    ("agua",   "el", "вода"),
    ("alma",   "el", "душа"),
    ("águila", "el", "орёл"),
    ("hambre", "el", "голод"),
    ("hacha",  "el", "топор"),
    ("aula",   "el", "аудитория"),
    ("área",   "el", "область"),
    ("arma",   "el", "оружие"),
    ("ala",    "el", "крыло"),
    ("ave",    "el", "птица"),
]


def pick_n(pool, n):
    """Take first n items from pool, mutate pool by removing them."""
    head = pool[:n]
    del pool[:n]
    return head


def build_levels(nouns):
    """Distribute nouns across 100 levels."""
    # Pre-bucket by CEFR; skip already-plural entries; skip months/weekdays for base levels
    by_lvl = {"A1": [], "A2": [], "B1": [], "B2": [], "C1": []}
    for n in nouns:
        if n["is_plural"]:
            continue
        if n["word"] in EXCLUDE_FROM_BASE:
            continue
        # Skip multi-word entries like "el aceite de oliva" — keep simple
        if " " in n["word"]:
            continue
        by_lvl[n["level"]].append(n)

    # Stable sort: keep order from CleanVocab — assumed roughly by usefulness
    levels = []

    # ── Block 1 (1-15): A1 base, sg only, 10 words/level = 150 words ──
    for i, lv in enumerate(range(1, 16)):
        words = pick_n(by_lvl["A1"], 10)
        levels.append({
            "level": lv, "block": "A1-base",
            "rule_hint": "EL — мужской род (-o), LA — женский (-a). Слушай окончание слова.",
            "words": [_round(w) for w in words],
        })

    # ── Block 2 (16-30): plurals of A1 base, 10 words/level ──
    # Re-take same A1 words from levels 1-15 and pluralize
    a1_used = []
    for L in levels[:15]:
        a1_used.extend(L["words"])
    for i, lv in enumerate(range(16, 31)):
        chunk = a1_used[(i)*10:(i+1)*10]
        plural_words = []
        for w in chunk:
            pl_word, pl_art = make_plural(w["word"], w["article"])
            plural_words.append({**w, "word": pl_word, "article": pl_art, "is_plural": True})
        levels.append({
            "level": lv, "block": "A1-plural",
            "rule_hint": "Множественное число: +s после гласной, +es после согласной. EL→LOS, LA→LAS.",
            "words": plural_words,
        })

    # ── Block 3 (31-50): A2 sg + plural mixed, 10 words/level = 200 words ──
    for i, lv in enumerate(range(31, 51)):
        words = pick_n(by_lvl["A2"], 10)
        # mix in 3 plurals (every 3rd word)
        for k in range(2, len(words), 3):
            pl_word, pl_art = make_plural(words[k]["word"], words[k]["article"])
            words[k] = {**words[k], "word": pl_word, "article": pl_art, "is_plural": True}
        levels.append({
            "level": lv, "block": "A2",
            "rule_hint": "Расширение лексики A2. Все 4 артикля в перемешку.",
            "words": [_round(w) for w in words],
        })

    # ── Block 4 (51-65): exceptions ──
    # 51-55: gender exceptions (repeat 12 entries × ~4 to fill 50)
    pool = []
    while len(pool) < 50:
        pool.extend(EXCEPTIONS_GENDER)
    for i, lv in enumerate(range(51, 56)):
        sel = pool[i*10:(i+1)*10]
        words = [{"word": w, "article": a, "russian": ru,
                  "example": "", "category": "exception-gender", "is_plural": False,
                  "rule": hint} for w, a, ru, hint in sel]
        levels.append({"level": lv, "block": "exceptions-gender",
                       "rule_hint": "Окончание обманывает: запоминай по списку.",
                       "words": words})

    # 56-60: -ma masculine
    pool = []
    while len(pool) < 50:
        pool.extend(EXCEPTIONS_MA)
    for i, lv in enumerate(range(56, 61)):
        sel = pool[i*10:(i+1)*10]
        words = [{"word": w, "article": a, "russian": ru,
                  "example": "", "category": "exception-ma", "is_plural": False}
                 for w, a, ru in sel]
        levels.append({"level": lv, "block": "exceptions-ma",
                       "rule_hint": "Слова на -ma греч. происхождения — м.род: el problema, el tema.",
                       "words": words})

    # 61-65: el agua rule
    pool = []
    while len(pool) < 50:
        pool.extend(EL_AGUA_RULE)
    for i, lv in enumerate(range(61, 66)):
        sel = pool[i*10:(i+1)*10]
        words = [{"word": w, "article": a, "russian": ru,
                  "example": "", "category": "el-agua-rule", "is_plural": False}
                 for w, a, ru in sel]
        levels.append({"level": lv, "block": "el-agua-rule",
                       "rule_hint": "Перед ударным а-/ha- ставим EL даже у ж.рода: el agua.",
                       "words": words})

    # ── Block 5 (66-80): B1, mix sg+pl, 10 words/level = 150 ──
    for i, lv in enumerate(range(66, 81)):
        words = pick_n(by_lvl["B1"], 10)
        for k in range(2, len(words), 3):
            pl_word, pl_art = make_plural(words[k]["word"], words[k]["article"])
            words[k] = {**words[k], "word": pl_word, "article": pl_art, "is_plural": True}
        levels.append({
            "level": lv, "block": "B1",
            "rule_hint": "B1: абстрактные существительные. Все 4 артикля.",
            "words": [_round(w) for w in words],
        })

    # ── Block 6 (81-90): irregular plurals — 81-85 sg, 86-90 pl ──
    irregular_pl = [
        ("lápiz", "el", "lápices", "los", "карандаш"),
        ("vez",   "la", "veces",   "las", "раз"),
        ("pez",   "el", "peces",   "los", "рыба"),
        ("luz",   "la", "luces",   "las", "свет"),
        ("voz",   "la", "voces",   "las", "голос"),
        ("cruz",  "la", "cruces",  "las", "крест"),
        ("nariz", "la", "narices", "las", "нос"),
        ("raíz",  "la", "raíces",  "las", "корень"),
        ("juez",  "el", "jueces",  "los", "судья"),
        ("rey",   "el", "reyes",   "los", "король"),
        ("ley",   "la", "leyes",   "las", "закон"),
        ("café",  "el", "cafés",   "los", "кафе"),
        ("sofá",  "el", "sofás",   "los", "диван"),
        ("paz",   "la", "paces",   "las", "мир"),
        ("nuez",  "la", "nueces",  "las", "орех"),
        ("arroz", "el", "arroces", "los", "рис"),
        ("mes",   "el", "meses",   "los", "месяц"),
        ("dios",  "el", "dioses",  "los", "бог"),
        ("autobús","el","autobuses","los","автобус"),
        ("flor",  "la", "flores",  "las", "цветок"),
        ("ciudad","la", "ciudades","las", "город"),
        ("pared", "la", "paredes", "las", "стена"),
        ("color", "el", "colores", "los", "цвет"),
        ("dolor", "el", "dolores", "los", "боль"),
        ("autor", "el", "autores", "los", "автор"),
    ]
    # 81-85: sg form; 86-90: pl form
    for i, lv in enumerate(range(81, 86)):
        sel = irregular_pl[i*10 % len(irregular_pl):]
        # cycle if needed
        cycled = (irregular_pl + irregular_pl)[i*10:(i+1)*10]
        words = [{"word": sg, "article": sa, "russian": ru,
                  "example": "", "category": "irregular-plural", "is_plural": False}
                 for sg, sa, _, _, ru in cycled]
        levels.append({"level": lv, "block": "irregular-plurals-sg",
                       "rule_hint": "Сначала запомни ед.число этих особых слов.",
                       "words": words})
    for i, lv in enumerate(range(86, 91)):
        cycled = (irregular_pl + irregular_pl)[i*10:(i+1)*10]
        words = [{"word": pl, "article": pa, "russian": ru,
                  "example": "", "category": "irregular-plural", "is_plural": True}
                 for _, _, pl, pa, ru in cycled]
        levels.append({"level": lv, "block": "irregular-plurals-pl",
                       "rule_hint": "Z→CES, Y→YES, согласная→+ES. Запомни множественное!",
                       "words": words})

    # ── Block 7 (91-95): B2 advanced ──
    for i, lv in enumerate(range(91, 96)):
        words = pick_n(by_lvl["B2"], 10)
        for k in range(2, len(words), 3):
            pl_word, pl_art = make_plural(words[k]["word"], words[k]["article"])
            words[k] = {**words[k], "word": pl_word, "article": pl_art, "is_plural": True}
        levels.append({"level": lv, "block": "B2",
                       "rule_hint": "B2: продвинутая лексика. Все 4 артикля.",
                       "words": [_round(w) for w in words]})

    # ── Block 8 (96-100): final exam — meaning-changing pairs ──
    # Each pair contributes 2 entries (el-version, la-version). 25 pairs = 50 entries.
    meaning_changing = [
        ("capital",   "el", "капитал (деньги)"),
        ("capital",   "la", "столица (город)"),
        ("orden",     "el", "порядок"),
        ("orden",     "la", "приказ"),
        ("frente",    "el", "фронт (война)"),
        ("frente",    "la", "лоб"),
        ("cura",      "el", "священник"),
        ("cura",      "la", "лечение"),
        ("guía",      "el", "гид (мужчина)"),
        ("guía",      "la", "путеводитель"),
        ("policía",   "el", "полицейский"),
        ("policía",   "la", "полиция"),
        ("coma",      "el", "кома (болезнь)"),
        ("coma",      "la", "запятая"),
        ("cólera",    "el", "холера"),
        ("cólera",    "la", "гнев"),
        ("corte",     "el", "порез"),
        ("corte",     "la", "королевский двор"),
        ("editorial", "el", "передовица"),
        ("editorial", "la", "издательство"),
        ("final",     "el", "конец"),
        ("final",     "la", "финал (спорт.)"),
        ("margen",    "el", "поле (страницы)"),
        ("margen",    "la", "берег (реки)"),
        ("mañana",    "el", "будущее"),
        ("mañana",    "la", "утро"),
        ("papa",      "el", "папа римский"),
        ("papa",      "la", "картошка"),
        ("parte",     "el", "донесение"),
        ("parte",     "la", "часть"),
        ("pendiente", "el", "серьга"),
        ("pendiente", "la", "склон"),
        ("cometa",    "el", "комета"),
        ("cometa",    "la", "воздушный змей"),
        ("modelo",    "el", "модель/образец"),
        ("modelo",    "la", "модель (девушка)"),
        ("orden",     "el", "порядок"),
        ("orden",     "la", "приказ"),
        ("vista",     "el", "слушание (суд)"),
        ("vista",     "la", "вид"),
        ("clave",     "el", "клавишник"),
        ("clave",     "la", "ключ/код"),
        ("manga",     "el", "манга (комикс)"),
        ("manga",     "la", "рукав"),
        ("radio",     "el", "радиус"),
        ("radio",     "la", "радио"),
        ("trompeta",  "el", "трубач"),
        ("trompeta",  "la", "труба"),
        ("vocal",     "el", "член комиссии"),
        ("vocal",     "la", "гласная буква"),
    ]
    for i, lv in enumerate(range(96, 101)):
        sel = meaning_changing[i*10:(i+1)*10]
        words = [{"word": w, "article": a, "russian": ru,
                  "example": "", "category": "meaning-changing", "is_plural": False}
                 for w, a, ru in sel]
        levels.append({"level": lv, "block": "meaning-changing-FINAL",
                       "rule_hint": "Артикль меняет смысл: el capital (деньги) ≠ la capital (столица).",
                       "words": words})

    return levels


def _round(w):
    """Strip internal sort fields from a vocab record for JSON output."""
    return {
        "word": w["word"],
        "article": w["article"],
        "russian": w.get("russian", ""),
        "example": w.get("example", ""),
        "category": w.get("category", ""),
        "is_plural": w.get("is_plural", False),
    }


def main():
    nouns = extract_nouns()
    print(f"Extracted {len(nouns)} unique nouns with article from vocab")
    by_lvl = {}
    for n in nouns:
        by_lvl.setdefault(n["level"], 0)
        by_lvl[n["level"]] += 1
    print(f"  By CEFR: {by_lvl}")

    levels = build_levels(nouns)

    # Stats
    total_rounds = sum(len(L["words"]) for L in levels)
    by_block = {}
    for L in levels:
        by_block.setdefault(L["block"], 0)
        by_block[L["block"]] += len(L["words"])
    print(f"\nGenerated {len(levels)} levels, {total_rounds} total rounds")
    print("By block:")
    for k, v in by_block.items():
        print(f"  {k}: {v} rounds")

    OUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    OUT_JSON.write_text(
        json.dumps(levels, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    print(f"\nWrote {OUT_JSON.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
