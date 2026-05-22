"""Генератор детерминированной структуры уровней для игры Palabra Maestra.

Создаёт app/src/main/assets/palabra_levels.json:
  100 уровней × 10 уникальных слов = 1000 слов.
  Прогрессия по длине + CEFR (короче и проще → длиннее и сложнее).
  Слова берутся из spanish_vocab.json + всех Kotlin vocab файлов.
"""
import json
import os
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VOCAB_JSON = ROOT / "app" / "src" / "main" / "assets" / "spanish_vocab.json"
VOCAB_DIR = ROOT / "app" / "src" / "main" / "java" / "com" / "spanishapp" / "data" / "db"
OUTPUT = ROOT / "app" / "src" / "main" / "assets" / "palabra_levels.json"


def strip_accents(s: str) -> str:
    return "".join(
        c for c in unicodedata.normalize("NFD", s.lower())
        if unicodedata.category(c) != "Mn"
    )


def collect_words() -> dict[str, dict]:
    """Возвращает: word → {russian, level}."""
    pool: dict[str, dict] = {}

    # 1. spanish_vocab.json (с явным CEFR)
    data = json.loads(VOCAB_JSON.read_text(encoding="utf-8"))
    for w in data["nouns"]:
        sp = w["spanish"].strip().lower()
        parts = sp.split()
        if len(parts) == 2 and parts[0] in ("el", "la"):
            word = parts[1]
            if word not in pool:
                pool[word] = {
                    "word": word,
                    "russian": w["russian"].strip(),
                    "level": w.get("level", "A1"),
                }

    # 2. Все verbs из JSON тоже добавим (без артикля, в инфинитиве)
    for w in data.get("verbs", []):
        sp = w["spanish"].strip().lower()
        # Берём только однословные глаголы
        if " " in sp or "/" in sp: continue
        if sp not in pool:
            pool[sp] = {
                "word": sp,
                "russian": w["russian"].strip(),
                "level": w.get("level", "A1"),
            }

    # 3. Прилагательные тоже
    for w in data.get("adjectives", []):
        sp = w["spanish"].strip().lower()
        if " " in sp: continue
        if sp not in pool:
            pool[sp] = {
                "word": sp,
                "russian": w["russian"].strip(),
                "level": w.get("level", "A1"),
            }

    # 4. Kotlin vocab — добавляем оставшиеся
    files = ["BasicsVocab.kt", "CleanVocab.kt"] + [f"VocabExtra{i}.kt" for i in range(1, 13)]
    pat = re.compile(r'"(?:el|la)\s+([a-záéíóúñü]+)"[^"]*?"([а-яёА-ЯЁ ,/\-\(\)\.]+)"')
    for fname in files:
        path = VOCAB_DIR / fname
        if not path.exists(): continue
        text = path.read_text(encoding="utf-8")
        for m in pat.finditer(text):
            word = m.group(1).lower()
            ru = m.group(2).strip()
            if 3 <= len(word) <= 15 and word not in pool:
                pool[word] = {"word": word, "russian": ru, "level": "B1"}

    return pool


# CEFR rank для сортировки
CEFR_RANK = {"A1": 0, "A2": 1, "B1": 2, "B2": 3, "C1": 4}


def difficulty_key(w: dict) -> tuple:
    """Сложность: CEFR + длина + алфавит (для детерминизма)."""
    word = w["word"]
    lvl_rank = CEFR_RANK.get(w["level"], 2)
    length = len(word)
    # Дополнительные пенальти за акценты, удвоенные буквы (для разнообразия)
    has_accent = any(c in word for c in "áéíóúñü")
    has_double = any(word[i] == word[i+1] for i in range(len(word)-1))
    return (lvl_rank, length, -int(has_accent), -int(has_double), strip_accents(word))


def main():
    pool = collect_words()
    print(f"Собрано слов в пуле: {len(pool)}")

    # Очистка
    filtered = [
        w for w in pool.values()
        if 3 <= len(w["word"]) <= 15
        and re.match(r"^[a-záéíóúñü]+$", w["word"])
        and w["russian"]
        and len(w["russian"]) <= 50
    ]
    print(f"После фильтрации: {len(filtered)}")

    # Группируем по длине (для прогрессии: короткое → лёгкое, длинное → сложное)
    by_length: dict[int, list] = {}
    for w in filtered:
        by_length.setdefault(len(w["word"]), []).append(w)
    # Внутри каждой длины — сортируем по CEFR + алфавит (детерминизм)
    for lst in by_length.values():
        lst.sort(key=difficulty_key)

    # ── Распределение по 100 уровням ─────────────────────────────
    # Цель: каждые 10 уровней (=100 слов) — одна полоса длины
    # Уровень 1-10 (tutorial A1): длина 3-5
    # 11-25 (easy A1+): длина 4-6
    # 26-50 (A2): длина 5-7
    # 51-80 (B1): длина 7-10
    # 81-100 (B2): длина 10-15
    bands = [
        # (level_from, level_to, lengths_preferred_order)
        (1, 10,   [3, 4, 5]),
        (11, 25,  [4, 5, 6]),
        (26, 40,  [5, 6, 7]),
        (41, 55,  [6, 7, 8]),
        (56, 70,  [7, 8, 9]),
        (71, 85,  [8, 9, 10, 11]),
        (86, 100, [10, 11, 12, 13, 14, 15]),
    ]

    used_words: set[str] = set()
    levels = [None] * 100

    for (from_lvl, to_lvl, lens) in bands:
        # Сколько слов нужно: (to_lvl - from_lvl + 1) × 10
        needed = (to_lvl - from_lvl + 1) * 10
        # Собираем кандидатов из предпочтительных длин
        candidates = []
        for ln in lens:
            for w in by_length.get(ln, []):
                if w["word"] not in used_words:
                    candidates.append(w)
        # Если не хватает — добавим из соседних длин (расширяемся)
        if len(candidates) < needed:
            for ln in sorted(by_length.keys()):
                if ln in lens: continue
                for w in by_length[ln]:
                    if w["word"] not in used_words:
                        candidates.append(w)
                if len(candidates) >= needed: break
        # Берём первые `needed`
        chosen = candidates[:needed]
        for w in chosen:
            used_words.add(w["word"])
        # Детерминированное перемешивание: чтобы в одном уровне не было
        # 10 слов на «co-», а была хорошая случайная подборка.
        # Сид = (from_lvl, to_lvl) — каждый бенд имеет свой стабильный шафл.
        import random as _r
        rng = _r.Random(from_lvl * 1000 + to_lvl)
        rng.shuffle(chosen)
        # Распределяем по уровням этой полосы (по 10 на уровень)
        for i, lvl_num in enumerate(range(from_lvl, to_lvl + 1)):
            chunk = chosen[i * 10:(i + 1) * 10]
            levels[lvl_num - 1] = {
                "level": lvl_num,
                "cefr": cefr_for_level(lvl_num),
                "words": [
                    {"word": w["word"], "russian": w["russian"]}
                    for w in chunk
                ],
            }

    OUTPUT.write_text(json.dumps(levels, ensure_ascii=False, indent=2), encoding="utf-8")
    total = sum(len(lvl["words"]) for lvl in levels)
    unique = len({w["word"] for lvl in levels for w in lvl["words"]})
    print(f"✅ Записано {len(levels)} уровней, {total} слот, {unique} уникальных")

    # Превью
    for lvl_num in [1, 25, 50, 75, 100]:
        lvl = levels[lvl_num - 1]
        sample = [w["word"] for w in lvl["words"]]
        print(f"  Level {lvl_num} ({lvl['cefr']}): {sample}")


def cefr_for_level(n: int) -> str:
    return ("A1" if n <= 25 else
            "A2" if n <= 50 else
            "B1" if n <= 80 else
            "B2")


if __name__ == "__main__":
    main()
