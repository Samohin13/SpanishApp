"""
Умный генератор недостающих картинок для игры «Артикли».

Что делает:
  1. Читает actual список слов из app/src/main/assets/articles_levels.json
  2. Смотрит что уже есть в app/src/main/assets/word_images/*.png
  3. Для каждого недостающего слова:
     • если есть кастомный prompt в generate_images.py (импорт WORDS) — берёт его
     • иначе — генерит generic prompt по шаблону
  4. Запрашивает Recraft V3 API (~$0.04 за картинку)

Запуск:
  python generate_missing.py            # сначала dry-run, показывает план
  python generate_missing.py --go       # реально запускает генерацию
  python generate_missing.py --go --limit 10   # только первые 10 (тест)

API ключ: RECRAFT_API_KEY из env var или local.properties.
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
import time
import unicodedata
from pathlib import Path

import requests  # type: ignore

ROOT = Path(__file__).resolve().parent
LEVELS_JSON = ROOT / "app" / "src" / "main" / "assets" / "articles_levels.json"
IMAGES_DIR = ROOT / "app" / "src" / "main" / "assets" / "word_images"
GEN_SCRIPT = ROOT / "generate_images.py"
LOCAL_PROPS = ROOT / "local.properties"

API_URL = "https://external.api.recraft.ai/v1/images/generations"
# BASE убран — теперь весь стиль внутри smart_prompt (был лимит 1000 символов API)


def strip_accents(s: str) -> str:
    return "".join(
        c for c in unicodedata.normalize("NFD", s.lower())
        if unicodedata.category(c) != "Mn"
    )


def read_api_key() -> str:
    key = os.environ.get("RECRAFT_API_KEY", "").strip()
    if key:
        return key
    if LOCAL_PROPS.exists():
        for line in LOCAL_PROPS.read_text(encoding="utf-8-sig").splitlines():
            line = line.strip().lstrip("﻿")
            if line.startswith("RECRAFT_API_KEY="):
                return line.split("=", 1)[1].strip()
    sys.exit(
        "ERROR: RECRAFT_API_KEY не найден.\n"
        "Установите env var или добавьте в local.properties:\n"
        "  RECRAFT_API_KEY=re_xxx"
    )


def load_curated_prompts() -> dict[str, tuple[str, str]]:
    """Парсит generate_images.py чтобы извлечь WORDS = [(filename, word, prompt), ...].

    Возвращает {filename: (word, prompt)}.
    """
    text = GEN_SCRIPT.read_text(encoding="utf-8")
    # Простой regex по структуре («filename», «word», «prompt»)
    pattern = re.compile(
        r'\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*,\s*"([^"]+)"\s*\)'
    )
    result: dict[str, tuple[str, str]] = {}
    for m in pattern.finditer(text):
        filename, word, prompt = m.group(1), m.group(2), m.group(3)
        # Только записи внутри WORDS-блока (между WORDS = [ и закрывающей ])
        result[filename] = (word, prompt)
    return result


def load_needed_words() -> list[dict]:
    """Возвращает список dict'ов с full context: word, article, russian, plural."""
    data = json.loads(LEVELS_JSON.read_text(encoding="utf-8"))
    seen: dict[str, dict] = {}
    for level in data:
        for w in level["words"]:
            word = w["word"].strip()
            filename = strip_accents(word)
            if filename not in seen:
                seen[filename] = {
                    "filename": filename,
                    "word": word,
                    "article": w.get("article", ""),
                    "russian": w.get("russian", "").strip(),
                    "is_plural": w.get("is_plural", False),
                }
    return list(seen.values())


# Сцены для слов где simple перевод недостаточен или AI путается.
# Каждая сцена даёт иконическое, моментально узнаваемое представление.
# Формат: filename → "English scene description"
SCENES: dict[str, str] = {
    # Уже протестированные (4-я итерация)
    "cura":           "an open first aid kit on a wooden table with bandages and medicine bottles, top-down view, no people",
    "clave":          "a single shiny house key with a small keychain, hanging on a hook",
    "coma":           "a giant white comma punctuation symbol on a plain pastel background",
    "final":          "a golden championship sports trophy with a star on top, on a podium",
    "parte":          "a single slice of pie separated from the rest of the pie",
    "ejemplo":        "a single red apple separated from a row of green apples, isolated by a red arrow",
    "abastecimiento": "delivery trucks loaded with crates being unloaded at a warehouse loading dock",
    "almacen":        "interior of a large industrial warehouse with tall shelves full of cardboard boxes",
    "modelo":         "a young female fashion model walking on a brightly lit runway during a fashion show",
    # Самые частые абстракции
    "problema":       "a tangled knot of colorful ropes on a plain white background",
    "oportunidad":    "an open door with bright sunlight streaming through, hopeful atmosphere",
    "decision":       "a fork in a country road, two paths diverging, signpost in the middle",
    "idea":           "a glowing yellow light bulb on a plain background, lit up brightly",
    "experiencia":    "an elderly mountain climber at a summit, weathered face, looking confident",
    "diferencia":     "two identical apples side by side, one red and one green, on white background",
    "razon":          "a chess player thinking deeply, hand on chin, pondering the next move",
    "futuro":         "a futuristic city skyline at sunset with flying vehicles",
    "pasado":         "an old vintage sepia photograph in a wooden frame, antique style",
    "presente":       "a wrapped gift box with a red bow on a white surface",
    "vida":           "a single green plant sprout growing from soil, sunlight from above",
    "amor":           "a single red heart shape, glossy, on a soft pink background",
    "muerte":         "a single black skull on a plain gray background, simple silhouette",
    "paz":            "a white dove flying with an olive branch in its beak, blue sky",
    "libertad":       "a bird flying free from an open cage against a blue sky",
    "tiempo":         "a vintage round wall clock with roman numerals, simple background",
    "lugar":          "a red location pin marker stuck on a paper map",
    "forma":          "wooden geometric shapes — cube, sphere, pyramid — arranged on white",
    "tipo":           "three different bottles arranged in a row, each a different shape",
    "nivel":          "a yellow construction spirit level tool with a bubble, on white",
    "manera":         "a winding road through countryside seen from above, single path",
    "causa":          "a domino effect — first domino falling toward a row of dominoes",
    "efecto":         "a stone dropped into water creating concentric ripples",
    "resultado":      "a final test paper with a big red A+ grade and a checkmark",
    "caso":           "a magnifying glass on top of an open detective file with photos",
    "verdad":         "an open book with the word TRUTH highlighted (use generic symbol of truth instead)",
    "mentira":        "a long Pinocchio-style wooden nose, isolated on white",
}


def smart_prompt(meta: dict) -> str:
    """Иконический textbook prompt: артикль + scene или русский перевод."""
    article = meta["article"]
    word = meta["word"]
    russian = meta["russian"]
    filename = meta["filename"]

    # Берём явную сцену если есть, иначе строим из русского перевода
    scene = SCENES.get(filename)
    if not scene:
        plural = "several " if meta["is_plural"] else "a single "
        scene = f"{plural}{russian} (the Spanish word «{article} {word}»)"

    return (
        f"Simple iconic photograph: {scene}. "
        f"Anyone seeing this must instantly understand «{russian}». "
        f"Single subject, fully visible, NOT cropped, centered, fills 70% of frame. "
        f"NO hands holding it, NO phones/tablets/screens, NO picture frames, NO foreground objects, NO text, NO logos. "
        f"Plain background, soft natural light, sharp focus, photorealistic."
    )


def generate_one(api_key: str, prompt: str) -> bytes | None:
    try:
        resp = requests.post(
            API_URL,
            headers={
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json",
            },
            json={
                "prompt": prompt,
                "style": "realistic_image",
                "substyle": "natural_light",
                "n": 1,
                "size": "1024x1024",
            },
            timeout=120,
        )
        if resp.status_code != 200:
            print(f"    ❌ HTTP {resp.status_code}: {resp.text[:200]}")
            return None
        url = resp.json()["data"][0]["url"]
        img = requests.get(url, timeout=120)
        img.raise_for_status()
        return img.content
    except Exception as e:
        print(f"    ❌ {e}")
        return None


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--go", action="store_true",
                    help="реально генерить (без флага — dry-run)")
    ap.add_argument("--limit", type=int, default=0,
                    help="ограничить количество картинок (для теста)")
    ap.add_argument("--regen", action="store_true",
                    help="перегенерить 9 тестовых слов (cura, clave, coma и др.)")
    ap.add_argument("--throttle", type=float, default=0.4,
                    help="пауза между запросами в секундах")
    args = ap.parse_args()

    IMAGES_DIR.mkdir(parents=True, exist_ok=True)
    existing = {f.stem for f in IMAGES_DIR.iterdir() if f.suffix == ".png"}
    needed = load_needed_words()
    needed_filenames = {m["filename"] for m in needed}

    missing = [m for m in needed if m["filename"] not in existing]

    print(f"📊 Статистика:")
    print(f"   Слов нужно для игры:       {len(needed)}")
    print(f"   Картинок уже есть:         {len(existing & needed_filenames)} (нужных)")
    print(f"   Картинок есть, но не нужно:{len(existing - needed_filenames)} (мусор)")
    print(f"   Картинок не хватает:       {len(missing)}")
    print(f"   Стоимость:                 ~${len(missing) * 0.04:.2f}")
    print()

    # Список конкретных слов для теста (включая ранее неудачные)
    test_words = ["cura", "clave", "coma", "final", "parte", "ejemplo", "abastecimiento", "almacen", "modelo"]
    if args.limit and not args.regen:
        missing = missing[: args.limit]
    elif args.regen:
        # Регенерим только то что в test_words (даже если файл уже есть)
        missing = [m for m in needed if m["filename"] in test_words]
        print(f"♻️  REGEN: пере-генерация {len(missing)} тестовых слов с новым prompt\n")

    if not args.go:
        print("Dry-run. Запусти с --go чтобы реально сгенерить.")
        print("Тест нового prompt: --go --regen")
        return

    api_key = read_api_key()
    ok = err = 0
    t0 = time.time()
    for i, meta in enumerate(missing, 1):
        filename = meta["filename"]
        spanish = f"{meta['article']} {meta['word']}"
        prompt = smart_prompt(meta)

        target = IMAGES_DIR / f"{filename}.png"
        print(f"[{i:3}/{len(missing)}] 🎨 {spanish:25} = {meta['russian']}")
        png = generate_one(api_key, prompt)
        if png:
            target.write_bytes(png)
            kb = len(png) // 1024
            elapsed = time.time() - t0
            avg = elapsed / i
            eta = avg * (len(missing) - i) / 60
            print(f"         ✅ {kb} KB · ETA {eta:.1f} мин")
            ok += 1
        else:
            err += 1
        time.sleep(args.throttle)

    print(f"\n{'='*50}")
    print(f"✅ Новых: {ok}   ❌ Ошибок: {err}")
    print(f"Папка: {IMAGES_DIR}")


if __name__ == "__main__":
    main()
