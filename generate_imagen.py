"""Генерация картинок для игры «Артикли» через Google Imagen 3.

Использует GEMINI_KEY из local.properties (тот же ключ для Gemini и Imagen).
Endpoint: generativelanguage.googleapis.com/v1beta/models/imagen-3.0-...

Imagen 3 — лучше Recraft/Pollinations на следовании промптам, особенно
для конкретных объектов. Бесплатный тир ограничен (обычно 100/день).

Запуск:
  python generate_imagen.py            # dry-run, показывает план
  python generate_imagen.py --go       # реальная генерация
  python generate_imagen.py --go --limit 5  # тест на 5 словах
"""
from __future__ import annotations
import argparse
import base64
import json
import os
import sys
import time
import unicodedata
from pathlib import Path
import requests  # type: ignore

ROOT = Path(__file__).resolve().parent
LEVELS = ROOT / "app" / "src" / "main" / "assets" / "articles_levels.json"
DIR = ROOT / "app" / "src" / "main" / "assets" / "word_images"
LOCAL_PROPS = ROOT / "local.properties"

# Imagen 4 endpoint (Google AI Studio). Доступные модели:
#   imagen-4.0-fast-generate-001    — быстрый, дешевый
#   imagen-4.0-generate-001         — стандарт
#   imagen-4.0-ultra-generate-001   — лучшее качество
IMAGEN_URL = (
    "https://generativelanguage.googleapis.com/v1beta/models/"
    "imagen-4.0-fast-generate-001:predict"
)


def strip(s: str) -> str:
    return "".join(c for c in unicodedata.normalize("NFD", s.lower())
                   if unicodedata.category(c) != "Mn")


def read_key() -> str:
    k = os.environ.get("GEMINI_KEY", "").strip()
    if k:
        return k
    if LOCAL_PROPS.exists():
        for line in LOCAL_PROPS.read_text(encoding="utf-8-sig").splitlines():
            line = line.strip().lstrip("﻿")
            if line.startswith("GEMINI_KEY="):
                return line.split("=", 1)[1].strip()
    sys.exit("GEMINI_KEY не найден в env или local.properties")


def build_prompt(meta: dict) -> str:
    article = meta["article"]
    word = meta["word"]
    russian = meta["russian"]
    return (
        f"A clean, simple photograph of {russian} (Spanish: {article} {word}). "
        f"Single clear subject, fully visible, not cropped, centered, fills 70% of frame. "
        f"NO people, NO hands, NO phones or screens, NO picture frames, NO text or logos. "
        f"Plain neutral background, soft natural light, photorealistic. "
        f"The image must instantly and unambiguously represent {russian}."
    )


def generate_one(key: str, prompt: str) -> bytes | None:
    payload = {
        "instances": [{"prompt": prompt}],
        "parameters": {
            "sampleCount": 1,
            "aspectRatio": "1:1",
            "personGeneration": "dont_allow",  # ключевой запрет людей
        },
    }
    try:
        r = requests.post(
            f"{IMAGEN_URL}?key={key}",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=180,
        )
        if r.status_code != 200:
            print(f"    ❌ HTTP {r.status_code}: {r.text[:200]}")
            return None
        data = r.json()
        preds = data.get("predictions", [])
        if not preds:
            print(f"    ❌ no predictions: {r.text[:200]}")
            return None
        b64 = preds[0].get("bytesBase64Encoded", "")
        if not b64:
            print(f"    ❌ no image bytes")
            return None
        return base64.b64decode(b64)
    except Exception as e:
        print(f"    ❌ {e}")
        return None


def load_needed():
    data = json.loads(LEVELS.read_text(encoding="utf-8"))
    seen = {}
    for lvl in data:
        for w in lvl["words"]:
            fn = strip(w["word"])
            if fn not in seen:
                seen[fn] = {
                    "filename": fn,
                    "word": w["word"],
                    "article": w["article"],
                    "russian": w.get("russian", ""),
                    "is_plural": w.get("is_plural", False),
                }
    return list(seen.values())


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--go", action="store_true")
    ap.add_argument("--limit", type=int, default=0)
    ap.add_argument("--throttle", type=float, default=1.5)
    args = ap.parse_args()

    DIR.mkdir(parents=True, exist_ok=True)
    have = {f.stem for f in DIR.iterdir() if f.suffix == ".png"}
    needed = load_needed()
    missing = [m for m in needed if m["filename"] not in have]

    print(f"Слов в игре: {len(needed)}")
    print(f"Уже есть: {len(have & {m['filename'] for m in needed})}")
    print(f"Не хватает: {len(missing)}")
    print(f"~$ Imagen free tier: бесплатно до квоты (~100/день),")
    print(f"  paid после ~$0.03-0.04/картинка")
    print()

    if args.limit:
        missing = missing[: args.limit]
        print(f"⚠️  Ограничено первыми {len(missing)} словами\n")

    if not args.go:
        print("Dry-run. --go для генерации.")
        return

    key = read_key()
    ok = err = quota = 0
    t0 = time.time()
    for i, meta in enumerate(missing, 1):
        target = DIR / f"{meta['filename']}.png"
        if target.exists():
            continue
        print(f"[{i:4}/{len(missing)}] 🎨 {meta['article']} {meta['word']:25} = {meta['russian']}")
        png = generate_one(key, build_prompt(meta))
        if png:
            target.write_bytes(png)
            print(f"          ✅ {len(png)//1024} KB · ETA "
                  f"{(time.time()-t0)/i*(len(missing)-i)/60:.1f} мин")
            ok += 1
        else:
            err += 1
            # Stop on persistent quota errors
            if err > 3 and ok == 0:
                print("Похоже квота закончилась или ключ невалидный — стоп.")
                break
        time.sleep(args.throttle)
    print(f"\n✅ Сделано: {ok}  ❌ Ошибок: {err}")


if __name__ == "__main__":
    main()
