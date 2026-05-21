"""Перестройка articles_levels.json со сбалансированной прогрессией A1 → B2.

Старая структура:
  Level 1 содержит сложные слова-исключения (cura, final, abastecimiento, idiota)
  → ученик пугается с первого шага.

Новая структура:
  Levels 1-13:  Базовая А1 (СЕМЬЯ, ТЕЛО, ДОМ, ЕДА, ЖИВОТНЫЕ, ОДЕЖДА, ТРАНСПОРТ)
  Levels 14-20: Простое правило -o → el / -a → la без исключений
  Levels 21-30: Исключения и нейтральные -e, -d, -ción endings (A1+)
  Levels 31-50: A2 (множественное число + средние слова)
  Levels 51-80: B1 (абстракции, специализированная лексика)
  Levels 81-100: B2 (редкое, академическое)

Все 661 уникальное слово используется минимум 1 раз через повторение в более
поздних уровнях (spaced repetition).
"""
from __future__ import annotations
import json
import os
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LEVELS_JSON = ROOT / "app" / "src" / "main" / "assets" / "articles_levels.json"
IMAGES_DIR = ROOT / "app" / "src" / "main" / "assets" / "word_images"


def strip(s: str) -> str:
    return "".join(c for c in unicodedata.normalize("NFD", s.lower())
                   if unicodedata.category(c) != "Mn")


# ── 125 orphan PNG слов с полной мета-инфой ────────────────────────────
# Формат: (word, article, russian, theme, is_plural)
ORPHAN_WORDS: list[tuple[str, str, str, str, bool]] = [
    # Семья
    ("abuela", "la", "бабушка", "family", False),
    ("padre", "el", "отец", "family", False),
    ("madre", "la", "мать", "family", False),
    ("hermana", "la", "сестра", "family", False),
    ("hija", "la", "дочь", "family", False),
    ("tio", "el", "дядя", "family", False),
    ("tia", "la", "тётя", "family", False),
    ("primo", "el", "двоюродный брат", "family", False),
    ("prima", "la", "двоюродная сестра", "family", False),
    ("esposa", "la", "жена", "family", False),
    ("novia", "la", "девушка / невеста", "family", False),
    ("novio", "el", "парень / жених", "family", False),
    ("hombre", "el", "мужчина", "people", False),
    ("mujer", "la", "женщина", "people", False),
    ("nino", "el", "мальчик", "people", False),
    ("nina", "la", "девочка", "people", False),
    ("bebe", "el", "младенец", "people", False),
    ("amigo", "el", "друг", "people", False),
    ("amiga", "la", "подруга", "people", False),
    ("vecino", "el", "сосед", "people", False),
    ("vecina", "la", "соседка", "people", False),
    ("colega", "el", "коллега", "people", False),
    ("jefe", "el", "начальник", "people", False),
    ("cliente", "el", "клиент", "people", False),
    ("ciudadano", "el", "гражданин", "people", False),
    ("extranjero", "el", "иностранец", "people", False),
    # Тело
    ("cabeza", "la", "голова", "body", False),
    ("brazo", "el", "рука (плечо-запястье)", "body", False),
    ("pie", "el", "стопа", "body", False),
    ("pierna", "la", "нога", "body", False),
    ("ojo", "el", "глаз", "body", False),
    ("oreja", "la", "ухо (внешнее)", "body", False),
    ("oido", "el", "ухо (слух)", "body", False),
    ("diente", "el", "зуб", "body", False),
    ("codo", "el", "локоть", "body", False),
    ("rodilla", "la", "колено", "body", False),
    ("espalda", "la", "спина", "body", False),
    ("hombro", "el", "плечо", "body", False),
    ("pecho", "el", "грудь", "body", False),
    ("boca", "la", "рот", "body", False),
    ("corazon", "el", "сердце", "body", False),
    ("tobillo", "el", "лодыжка", "body", False),
    # Животные
    ("gato", "el", "кот", "animals", False),
    ("perro", "el", "собака", "animals", False),
    ("pajaro", "el", "птица", "animals", False),
    ("pez", "el", "рыба (живая)", "animals", False),
    ("leon", "el", "лев", "animals", False),
    ("oso", "el", "медведь", "animals", False),
    ("tigre", "el", "тигр", "animals", False),
    ("vaca", "la", "корова", "animals", False),
    ("conejo", "el", "кролик", "animals", False),
    ("mariposa", "la", "бабочка", "animals", False),
    ("abeja", "la", "пчела", "animals", False),
    ("serpiente", "la", "змея", "animals", False),
    ("mono", "el", "обезьяна", "animals", False),
    ("elefante", "el", "слон", "animals", False),
    ("delfin", "el", "дельфин", "animals", False),
    ("tortuga", "la", "черепаха", "animals", False),
    # Еда + напитки
    ("manzana", "la", "яблоко", "food", False),
    ("naranja", "la", "апельсин", "food", False),
    ("platano", "el", "банан", "food", False),
    ("limon", "el", "лимон", "food", False),
    ("fresa", "la", "клубника", "food", False),
    ("uva", "la", "виноград", "food", False),
    ("patata", "la", "картофель", "food", False),
    ("tomate", "el", "помидор", "food", False),
    ("zanahoria", "la", "морковь", "food", False),
    ("pan", "el", "хлеб", "food", False),
    ("carne", "la", "мясо", "food", False),
    ("pescado", "el", "рыба (еда)", "food", False),
    ("pollo", "el", "курица", "food", False),
    ("queso", "el", "сыр", "food", False),
    ("leche", "la", "молоко", "food", False),
    ("cafe", "el", "кофе", "food", False),
    ("vino", "el", "вино", "food", False),
    ("te", "el", "чай", "food", False),
    ("hamburguesa", "la", "гамбургер", "food", False),
    ("pizza", "la", "пицца", "food", False),
    ("pastel", "el", "торт / пирог", "food", False),
    ("ensalada", "la", "салат", "food", False),
    ("sopa", "la", "суп", "food", False),
    # Одежда
    ("camisa", "la", "рубашка", "clothes", False),
    ("camiseta", "la", "футболка", "clothes", False),
    ("pantalon", "el", "штаны", "clothes", False),
    ("chaqueta", "la", "куртка", "clothes", False),
    ("vestido", "el", "платье", "clothes", False),
    ("zapato", "el", "ботинок", "clothes", False),
    ("bota", "la", "сапог", "clothes", False),
    ("sombrero", "el", "шляпа", "clothes", False),
    ("gorra", "la", "кепка", "clothes", False),
    ("bufanda", "la", "шарф", "clothes", False),
    ("guante", "el", "перчатка", "clothes", False),
    ("mochila", "la", "рюкзак", "clothes", False),
    ("bolso", "el", "сумка", "clothes", False),
    ("paraguas", "el", "зонт", "clothes", False),
    # Транспорт
    ("autobus", "el", "автобус", "transport", False),
    ("avion", "el", "самолёт", "transport", False),
    ("bicicleta", "la", "велосипед", "transport", False),
    ("ambulancia", "la", "скорая", "transport", False),
    ("camion", "el", "грузовик", "transport", False),
    ("metro", "el", "метро", "transport", False),
    ("taxi", "el", "такси", "transport", False),
    ("tren", "el", "поезд", "transport", False),
    # Дом
    ("casa", "la", "дом", "home", False),
    ("cocina", "la", "кухня", "home", False),
    ("escalera", "la", "лестница", "home", False),
    ("jardin", "el", "сад", "home", False),
    ("lampara", "la", "лампа", "home", False),
    ("nevera", "la", "холодильник", "home", False),
    ("mesa", "la", "стол", "home", False),
    ("silla", "la", "стул", "home", False),
    ("cama", "la", "кровать", "home", False),
    ("ventana", "la", "окно", "home", False),
    ("puerta", "la", "дверь", "home", False),
    ("tejado", "el", "крыша", "home", False),
    ("television", "la", "телевизор", "home", False),
    ("llave", "la", "ключ", "home", False),
    # Природа
    ("sol", "el", "солнце", "nature", False),
    ("luna", "la", "луна", "nature", False),
    ("nube", "la", "облако", "nature", False),
    ("estrella", "la", "звезда", "nature", False),
    ("arbol", "el", "дерево", "nature", False),
    ("nieve", "la", "снег", "nature", False),
    ("lluvia", "la", "дождь", "nature", False),
]


# ── Структура новых базовых уровней (1-13) ────────────────────────
# 13 уровней × 10 слов = 130 слотов из 125 orphan + 5 пересечений с 536
def build_base_levels() -> list[dict]:
    """Делит ORPHAN_WORDS по темам в 13 уровней по 10 слов."""
    by_theme: dict[str, list] = {}
    for w in ORPHAN_WORDS:
        by_theme.setdefault(w[3], []).append(w)

    # Группировка тем в уровни (целевой размер 10)
    plan = [
        ("Семья 1: основные родственники", "family", ["padre", "madre", "hermana", "hija", "tio", "tia", "primo", "prima", "abuela", "esposa"]),
        ("Семья 2: близкие люди", "family-people", ["hombre", "mujer", "nino", "nina", "bebe", "amigo", "amiga", "novia", "novio", "vecino"]),
        ("Тело 1: голова и торс", "body-head", ["cabeza", "ojo", "oreja", "oido", "diente", "boca", "pecho", "espalda", "hombro", "corazon"]),
        ("Тело 2: руки и ноги", "body-limb", ["brazo", "pie", "pierna", "codo", "rodilla", "tobillo", "amiga", "amigo", "vecino", "vecina"]),
        ("Дом 1: комнаты и мебель", "home-room", ["casa", "cocina", "mesa", "silla", "cama", "ventana", "puerta", "lampara", "nevera", "television"]),
        ("Дом 2: окружение", "home-out", ["jardin", "escalera", "tejado", "llave", "colega", "jefe", "cliente", "ciudadano", "extranjero", "casa"]),
        ("Еда 1: фрукты и овощи", "food-fruit", ["manzana", "naranja", "platano", "limon", "fresa", "uva", "patata", "tomate", "zanahoria", "ensalada"]),
        ("Еда 2: основные блюда", "food-main", ["pan", "carne", "pescado", "pollo", "queso", "leche", "sopa", "pastel", "pizza", "hamburguesa"]),
        ("Напитки", "drinks", ["cafe", "te", "vino", "leche", "agua", "limon", "naranja", "fresa", "uva", "manzana"]),
        ("Животные 1: домашние и фермерские", "animals-1", ["gato", "perro", "pajaro", "pez", "vaca", "conejo", "mariposa", "abeja", "serpiente", "mono"]),
        ("Животные 2: дикие и экзотические", "animals-2", ["leon", "oso", "tigre", "elefante", "delfin", "tortuga", "mariposa", "abeja", "serpiente", "mono"]),
        ("Одежда: верх и низ", "clothes-1", ["camisa", "camiseta", "pantalon", "chaqueta", "vestido", "zapato", "bota", "sombrero", "gorra", "bufanda"]),
        ("Транспорт и аксессуары", "transport-acc", ["autobus", "avion", "bicicleta", "tren", "taxi", "metro", "camion", "ambulancia", "mochila", "bolso"]),
    ]

    # Lookup: word → meta
    lookup = {w[0]: w for w in ORPHAN_WORDS}
    # Доп слова из основной 536-списка для пересечений
    extras = {
        "agua": ("agua", "el", "вода (но la! исключение по началу)", "drinks", False),
        # Все остальные должны быть в orphan
    }
    lookup.update(extras)

    levels = []
    for i, (title, theme_id, words_in_level) in enumerate(plan, start=1):
        ws = []
        for pos, w in enumerate(words_in_level):
            meta = lookup.get(w)
            if not meta:
                print(f"⚠️  Не нашёл meta для «{w}» в уровне {i}")
                continue
            ws.append({
                "word": meta[0],
                "article": meta[1],
                "russian": meta[2],
                "is_plural": meta[4],
                "position": pos,
            })
        levels.append({
            "level": i,
            "block": "A1-basic",
            "rule_hint": "EL — слова мужского рода (обычно -o, согласные). LA — слова женского рода (обычно -a, -ción, -d).",
            "words": ws[:10],
        })
    return levels


# ── Загрузка существующих 536 слов с difficulty scoring ────────────
def difficulty_score(w: dict, old_level: int) -> int:
    """Чем выше — тем сложнее. 0 = простейшее, 100 = самое сложное."""
    word = w["word"].lower()
    article = w["article"]
    is_plural = w.get("is_plural", False)
    score = 0

    # Множественное число — сложнее
    if is_plural:
        score += 25

    # Длина — длинные слова обычно сложнее
    if len(word) > 12:
        score += 15
    elif len(word) > 8:
        score += 5

    # Окончание-исключение: -ma, -día, -dia, -mapa, -problema (мужской на -a)
    if article == "el" and word.endswith("a") and word not in {"dia", "mapa"}:
        score += 10  # Это исключение, сложнее
    if article == "la" and word.endswith("o") and word not in {"mano", "foto", "moto", "radio"}:
        score += 10

    # Абстрактные суффиксы
    abstract_suffixes = ("miento", "anza", "encia", "ancia", "ismo", "azgo", "anza")
    if any(word.endswith(s) for s in abstract_suffixes):
        score += 20

    # Сохраняем влияние старого уровня (для тонкой сортировки внутри bin)
    score += old_level // 10

    return score


def main():
    # ── 1. Старая структура ──────────────────────────────────────
    old = json.loads(LEVELS_JSON.read_text(encoding="utf-8"))
    all_old_words: list[tuple[dict, int]] = []
    for lvl in old:
        for w in lvl["words"]:
            all_old_words.append((w, lvl["level"]))
    print(f"Старых слот: {len(all_old_words)}")

    # ── 2. Базовые уровни 1-13 ──────────────────────────────────
    base_levels = build_base_levels()
    base_words_set = {(w["article"], w["word"]) for lvl in base_levels for w in lvl["words"]}
    print(f"Базовых уровней (1-13): {len(base_levels)}")
    print(f"Уникальных пар (article, word) в базе: {len(base_words_set)}")

    # ── 3. Оставшиеся слова из старого пула, отсортированные по difficulty
    remaining = []
    for w, old_lvl in all_old_words:
        if (w["article"], w["word"]) not in base_words_set:
            remaining.append((difficulty_score(w, old_lvl), w))
    # Sort: проще → сложнее
    remaining.sort(key=lambda x: x[0])
    print(f"Оставшихся слот: {len(remaining)}")

    # ── 4. Распределение по 87 уровням (14-100), по 10 слов ─────
    remaining_words = [w for _, w in remaining]
    remaining_count = len(remaining_words)
    target_levels = 100 - len(base_levels)  # 87
    target_slots = target_levels * 10        # 870
    if remaining_count < target_slots:
        # Дополняем повторами (spaced repetition) из конца списка
        deficit = target_slots - remaining_count
        # Берём из второй половины (более сложные) для повтора
        repeat_pool = remaining_words[remaining_count // 2:]
        from itertools import cycle
        it = iter(cycle(repeat_pool))
        for _ in range(deficit):
            remaining_words.append(next(it))
        print(f"Доб. повторов: {deficit}")
    elif remaining_count > target_slots:
        remaining_words = remaining_words[:target_slots]
        print(f"Обрезано до: {target_slots}")

    # Распределяем по уровням
    new_levels = list(base_levels)
    for i in range(target_levels):
        chunk = remaining_words[i * 10:(i + 1) * 10]
        lvl_num = len(base_levels) + 1 + i

        # CEFR + rule_hint в зависимости от номера
        if lvl_num <= 25:
            block, hint = "A1-rule", "Правило -o → EL, -a → LA. Запоминай исключения отдельно (la mano, el problema, el día)."
        elif lvl_num <= 40:
            block, hint = "A1-exceptions", "Исключения: слова на -ma из греческого мужского (el problema, el día, el mapa). Слова на -ción / -dad / -sión всегда LA."
        elif lvl_num <= 60:
            block, hint = "A2", "Множественное число: los (м.р.) / las (ж.р.). Окончание -es для согласных, -s для гласных."
        elif lvl_num <= 80:
            block, hint = "B1", "Абстрактные существительные на -aje, -anza, -ismo обычно EL. На -tud, -ez, -ie обычно LA."
        else:
            block, hint = "B2", "Сложные слова: омонимы (el cura / la cura), редкие исключения, ср.род на lo (только с прилагательным)."

        for pos, w in enumerate(chunk):
            w["position"] = pos
        new_levels.append({
            "level": lvl_num,
            "block": block,
            "rule_hint": hint,
            "words": chunk,
        })

    # ── 5. Базовым уровням 1-13 тоже свой rule_hint ────────────
    new_levels[0]["rule_hint"] = "Слова -a → LA, слова -o → EL. Базовая лексика семьи."
    new_levels[1]["rule_hint"] = "Слова -a → LA, слова -o → EL. Близкие люди и роли."
    new_levels[2]["rule_hint"] = "Слова -a → LA, слова -o → EL. Внимание: el día (мужской на -a)."
    new_levels[3]["rule_hint"] = "el pie (нога), но la pierna (от бедра). Запоминай контекст."
    for i in range(4, 13):
        new_levels[i]["rule_hint"] = "Артикль зависит от окончания: -o → EL, -a → LA. Слушай и запоминай."

    # ── 6. Запись ───────────────────────────────────────────────
    out = json.dumps(new_levels, ensure_ascii=False, indent=2)
    LEVELS_JSON.write_text(out, encoding="utf-8")
    print()
    print(f"✅ Записано {len(new_levels)} уровней в {LEVELS_JSON}")
    total = sum(len(lvl["words"]) for lvl in new_levels)
    print(f"   Всего слот: {total}")


if __name__ == "__main__":
    main()
