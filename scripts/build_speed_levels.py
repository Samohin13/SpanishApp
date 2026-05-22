"""Генератор детерминированной структуры уровней для игры Rapido (Speed).

Создаёт app/src/main/assets/speed_levels.json:
  100 уровней × 10 уникальных слов = 1000 слов.
  Каждое слово: word, russian, distractors (3 уникальных перевода).
  Прогрессия CEFR + длины (как в palabra_levels.json).

Источник: spanish_vocab.json + Kotlin vocab.
"""
import json
import random
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VOCAB_JSON = ROOT / "app" / "src" / "main" / "assets" / "spanish_vocab.json"
VOCAB_DIR = ROOT / "app" / "src" / "main" / "java" / "com" / "spanishapp" / "data" / "db"
OUTPUT = ROOT / "app" / "src" / "main" / "assets" / "speed_levels.json"


def strip_accents(s: str) -> str:
    return "".join(
        c for c in unicodedata.normalize("NFD", s.lower())
        if unicodedata.category(c) != "Mn"
    )


def collect_words() -> dict[str, dict]:
    """Возвращает: word → {word, russian, level}."""
    pool: dict[str, dict] = {}

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

    for w in data.get("verbs", []):
        sp = w["spanish"].strip().lower()
        if " " in sp or "/" in sp: continue
        if sp not in pool:
            pool[sp] = {"word": sp, "russian": w["russian"].strip(), "level": w.get("level", "A1")}

    for w in data.get("adjectives", []):
        sp = w["spanish"].strip().lower()
        if " " in sp: continue
        if sp not in pool:
            pool[sp] = {"word": sp, "russian": w["russian"].strip(), "level": w.get("level", "A1")}

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


CEFR_RANK = {"A1": 0, "A2": 1, "B1": 2, "B2": 3, "C1": 4}


def difficulty_key(w: dict) -> tuple:
    word = w["word"]
    lvl_rank = CEFR_RANK.get(w["level"], 2)
    length = len(word)
    has_accent = any(c in word for c in "áéíóúñü")
    return (lvl_rank, length, -int(has_accent), strip_accents(word))


def short_russian(ru: str) -> str:
    """Берём первое значение из «капуста, кочан» → «капуста»."""
    s = ru.split(",")[0].split("/")[0].strip()
    return s[:30]  # обрезаем сверхдлинные для UI


def cefr_for_level(n: int) -> str:
    return ("A1" if n <= 25 else
            "A2" if n <= 50 else
            "B1" if n <= 80 else
            "B2")


def main():
    pool = collect_words()
    print(f"Собрано слов в пуле: {len(pool)}")

    filtered = [
        w for w in pool.values()
        if 3 <= len(w["word"]) <= 12   # на скорость берём чуть короче
        and re.match(r"^[a-záéíóúñü]+$", w["word"])
        and w["russian"]
        and len(w["russian"]) <= 50
    ]
    print(f"После фильтрации: {len(filtered)}")

    # Группируем по длине
    by_length: dict[int, list] = {}
    for w in filtered:
        by_length.setdefault(len(w["word"]), []).append(w)
    for lst in by_length.values():
        lst.sort(key=difficulty_key)

    # Полосы прогрессии (как в palabra, но короче — Speed читает быстро)
    bands = [
        (1, 10,   [3, 4, 5]),
        (11, 25,  [4, 5, 6]),
        (26, 40,  [5, 6, 7]),
        (41, 55,  [6, 7, 8]),
        (56, 70,  [7, 8, 9]),
        (71, 85,  [8, 9, 10]),
        (86, 100, [9, 10, 11, 12]),
    ]

    used_words: set[str] = set()
    levels = [None] * 100
    all_russians: list[str] = []  # для отвлечений

    for (from_lvl, to_lvl, lens) in bands:
        needed = (to_lvl - from_lvl + 1) * 10
        candidates = []
        for ln in lens:
            for w in by_length.get(ln, []):
                if w["word"] not in used_words:
                    candidates.append(w)
        if len(candidates) < needed:
            for ln in sorted(by_length.keys()):
                if ln in lens: continue
                for w in by_length[ln]:
                    if w["word"] not in used_words:
                        candidates.append(w)
                if len(candidates) >= needed: break
        chosen = candidates[:needed]
        for w in chosen:
            used_words.add(w["word"])
            all_russians.append(short_russian(w["russian"]))

        rng = random.Random(from_lvl * 1000 + to_lvl + 7)
        rng.shuffle(chosen)

        for i, lvl_num in enumerate(range(from_lvl, to_lvl + 1)):
            chunk = chosen[i * 10:(i + 1) * 10]
            # Распределяем по уровням — пока без дистракторов (добавим ниже)
            levels[lvl_num - 1] = {
                "level": lvl_num,
                "cefr": cefr_for_level(lvl_num),
                "_chunk": chunk,
            }

    # Дедуп пула переводов для дистракторов
    unique_russians = sorted(set(r for r in all_russians if r))

    # Генерируем дистракторы детерминированно: seed зависит от уровня и индекса
    for lvl in levels:
        lvl_num = lvl["level"]
        words_out = []
        for idx, w in enumerate(lvl["_chunk"]):
            correct_ru = short_russian(w["russian"])
            rng = random.Random(lvl_num * 10_000 + idx * 11 + 3)
            # Сэмплируем кандидатов в дистракторы, исключая правильный
            candidates = [r for r in unique_russians if r != correct_ru]
            distractors = rng.sample(candidates, 3) if len(candidates) >= 3 else candidates[:3]
            words_out.append({
                "word": w["word"],
                "russian": correct_ru,
                "distractors": distractors,
            })
        lvl["words"] = words_out
        del lvl["_chunk"]

    OUTPUT.write_text(json.dumps(levels, ensure_ascii=False, indent=2), encoding="utf-8")
    total = sum(len(lvl["words"]) for lvl in levels)
    unique = len({w["word"] for lvl in levels for w in lvl["words"]})
    print(f"✅ Записано {len(levels)} уровней, {total} слот, {unique} уникальных слов")

    for lvl_num in [1, 25, 50, 75, 100]:
        lvl = levels[lvl_num - 1]
        sample = [w["word"] for w in lvl["words"]]
        print(f"  Level {lvl_num} ({lvl['cefr']}): {sample}")


if __name__ == "__main__":
    main()
