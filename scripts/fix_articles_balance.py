"""
fix_articles_balance.py
Читает articles_levels.json, перераспределяет слова по уровням
так чтобы в каждом уровне был баланс el/la (и los/las на уровнях 16+).
Запускать из корня проекта:
    python scripts/fix_articles_balance.py
"""
import json
import random
from pathlib import Path

JSON_PATH = Path("app/src/main/assets/articles_levels.json")

# ── Загрузить текущий JSON ────────────────────────────────────
with open(JSON_PATH, "r", encoding="utf-8") as f:
    data = json.load(f)

# ── Собрать все уникальные слова по артиклю ───────────────────
by_article: dict[str, list] = {"el": [], "la": [], "los": [], "las": []}
seen: set[str] = set()

for level_obj in data:
    for w in level_obj.get("words", []):
        key = w.get("article", "") + "|" + w.get("word", "")
        if key in seen:
            continue
        seen.add(key)
        article = w.get("article", "")
        if article in by_article:
            by_article[article].append({
                "word":      w["word"],
                "article":   article,
                "russian":   w.get("russian", ""),
                "is_plural": w.get("is_plural", False),
            })

# Перемешать каждый бакет с фиксированным сидом (воспроизводимо)
rng = random.Random(42)
for bucket in by_article.values():
    rng.shuffle(bucket)

print("Слова по артиклям:")
for art, words in by_article.items():
    print(f"  {art}: {len(words)}")

# ── Конфиги уровней ───────────────────────────────────────────
# (block, rule_hint, {el, la, los, las} — кол-во слов каждого типа)
CONFIGS = []

A1_HINT   = "EL — мужской род (-o), LA — женский (-a). Слушай окончание слова."
PLUR_HINT = "Множественное число: el→los, la→las. Добавь -s или -es."
A2_HINT   = "Запомни артикль каждого слова — не всегда предсказуемо!"
EXC_HINT  = "Исключения: el agua, el día, la mano, la foto — учи отдельно."
B1_HINT   = "Суффиксы -ción/-sión → la; -aje/-or → el. Есть исключения!"
B2_HINT   = "Слова на -ma/-pa/-ta греческого происхождения часто мужского рода."

for lvl in range(1, 16):     # 1-15  A1-base    5el + 5la
    CONFIGS.append(("A1-base",    A1_HINT,   {"el": 5, "la": 5, "los": 0, "las": 0}))
for lvl in range(16, 31):    # 16-30 A1-plural  3+3+2+2
    CONFIGS.append(("A1-plural",  PLUR_HINT, {"el": 3, "la": 3, "los": 2, "las": 2}))
for lvl in range(31, 51):    # 31-50 A2
    CONFIGS.append(("A2",         A2_HINT,   {"el": 3, "la": 3, "los": 2, "las": 2}))
for lvl in range(51, 61):    # 51-60 исключения
    CONFIGS.append(("exceptions", EXC_HINT,  {"el": 3, "la": 3, "los": 2, "las": 2}))
for lvl in range(61, 81):    # 61-80 B1
    CONFIGS.append(("B1",         B1_HINT,   {"el": 3, "la": 3, "los": 2, "las": 2}))
for lvl in range(81, 101):   # 81-100 B2
    CONFIGS.append(("B2",         B2_HINT,   {"el": 3, "la": 3, "los": 2, "las": 2}))

# ── Указатели в каждом баките (с зацикливанием) ───────────────
ptrs = {"el": 0, "la": 0, "los": 0, "las": 0}

def next_word(article: str) -> dict:
    bucket = by_article[article]
    if not bucket:
        return {"word": article, "article": article, "russian": "", "is_plural": article in ("los","las")}
    w = bucket[ptrs[article] % len(bucket)].copy()
    ptrs[article] += 1
    return w

# ── Генерация уровней ─────────────────────────────────────────
result = []
for i, (block, rule_hint, dist) in enumerate(CONFIGS):
    level_num = i + 1
    words = []
    for art, count in dist.items():
        for _ in range(count):
            words.append(next_word(art))

    # Детерминированный шафл внутри уровня
    level_rng = random.Random(level_num * 7919)
    level_rng.shuffle(words)

    for pos, w in enumerate(words):
        w["position"] = pos

    result.append({
        "level":     level_num,
        "block":     block,
        "rule_hint": rule_hint,
        "words":     words,
    })

# ── Сохранить ─────────────────────────────────────────────────
with open(JSON_PATH, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print(f"\n✅ Готово! {len(result)} уровней записано в {JSON_PATH}")
print(f"   el использовано: {ptrs['el']}  la: {ptrs['la']}  los: {ptrs['los']}  las: {ptrs['las']}")

# Проверка первых 3 уровней
print("\nПроверка (первые 3 уровня):")
for lvl_obj in result[:3]:
    arts = [w["article"] for w in lvl_obj["words"]]
    print(f"  Level {lvl_obj['level']} ({lvl_obj['block']}): {arts}")
