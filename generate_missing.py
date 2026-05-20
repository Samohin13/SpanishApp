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
BASE = (
    "realistic photo, cinematic, natural white balance true to the scene, "
    "large clear subject filling the frame, easy to understand at a glance, "
    "no environment, no text"
)


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


def load_needed_words() -> list[tuple[str, str]]:
    """Возвращает [(filename, spanish_word_with_accents), ...] уникально."""
    data = json.loads(LEVELS_JSON.read_text(encoding="utf-8"))
    seen: dict[str, str] = {}
    for level in data:
        for w in level["words"]:
            word = w["word"].strip()
            filename = strip_accents(word)
            if filename not in seen:
                seen[filename] = word
    return [(fn, w) for fn, w in seen.items()]


def generic_prompt(spanish_word: str) -> str:
    """Generic prompt для слов без кастомного варианта."""
    base = strip_accents(spanish_word)
    return (
        f"a clear photograph of a single {base} (Spanish: {spanish_word}), "
        f"centered in frame, soft natural daylight, plain neutral background"
    )


def generate_one(api_key: str, prompt: str) -> bytes | None:
    full = f"{prompt}, {BASE}"
    try:
        resp = requests.post(
            API_URL,
            headers={
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json",
            },
            json={
                "prompt": full,
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
    ap.add_argument("--throttle", type=float, default=0.4,
                    help="пауза между запросами в секундах")
    args = ap.parse_args()

    IMAGES_DIR.mkdir(parents=True, exist_ok=True)
    existing = {f.stem for f in IMAGES_DIR.iterdir() if f.suffix == ".png"}
    curated = load_curated_prompts()
    needed = load_needed_words()

    missing = [(fn, sp) for fn, sp in needed if fn not in existing]
    have_curated = sum(1 for fn, _ in missing if fn in curated)

    print(f"📊 Статистика:")
    print(f"   Слов нужно для игры:       {len(needed)}")
    print(f"   Картинок уже есть:         {len(existing & {fn for fn, _ in needed})} (нужных)")
    print(f"   Картинок есть, но не нужно:{len(existing - {fn for fn, _ in needed})} (мусор)")
    print(f"   Картинок не хватает:       {len(missing)}")
    print(f"   Из них с custom-prompt:    {have_curated}")
    print(f"   Из них с generic-prompt:   {len(missing) - have_curated}")
    print(f"   Стоимость:                 ~${len(missing) * 0.04:.2f}")
    print()

    if not args.go:
        print("Dry-run. Запусти с --go чтобы реально сгенерить.")
        print("Сначала тест: --go --limit 5")
        return

    if args.limit:
        missing = missing[: args.limit]
        print(f"⚠️  Ограничено первыми {len(missing)} словами\n")

    api_key = read_api_key()
    ok = err = 0
    t0 = time.time()
    for i, (filename, spanish) in enumerate(missing, 1):
        if filename in curated:
            _, prompt = curated[filename]
            tag = "custom"
        else:
            prompt = generic_prompt(spanish)
            tag = "generic"

        target = IMAGES_DIR / f"{filename}.png"
        print(f"[{i:3}/{len(missing)}] 🎨 {spanish:25} ({tag})")
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
