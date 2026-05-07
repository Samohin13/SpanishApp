#!/usr/bin/env python3
"""
Сжатие assets/word_images/ для уменьшения размера APK.
- Уменьшает разрешение до max 256px по длинной стороне
- Конвертирует в WebP lossy quality=80 с сохранением альфы
- Имя файла оставляет тем же (.png), потому что код в ArticlesGameScreen грузит по .png

Запуск:
    python scripts/compress_word_images.py
"""
from __future__ import annotations
import os
import sys
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app" / "src" / "main" / "assets" / "word_images"
MAX_SIDE = 256
QUALITY = 80


def compress_one(path: Path) -> tuple[int, int]:
    orig_size = path.stat().st_size
    img = Image.open(path).convert("RGBA")
    w, h = img.size
    if max(w, h) > MAX_SIDE:
        if w >= h:
            new_w = MAX_SIDE
            new_h = int(h * MAX_SIDE / w)
        else:
            new_h = MAX_SIDE
            new_w = int(w * MAX_SIDE / h)
        img = img.resize((new_w, new_h), Image.LANCZOS)
    # Сохраняем как WebP, но с расширением .png — Android умеет читать любой формат
    # из assets, ArticlesGameScreen грузит через AssetManager и Coil/BitmapFactory.
    # На всякий случай делаем lossy WebP с альфой.
    tmp = path.with_suffix(".webp.tmp")
    img.save(tmp, format="WEBP", quality=QUALITY, method=6)
    tmp.replace(path)
    new_size = path.stat().st_size
    return orig_size, new_size


def main() -> int:
    if not SRC.exists():
        print(f"Не найдено: {SRC}", file=sys.stderr)
        return 1
    files = sorted(SRC.glob("*.png"))
    print(f"Найдено {len(files)} файлов в {SRC}")
    total_old = total_new = 0
    for i, p in enumerate(files, 1):
        try:
            old, new = compress_one(p)
            total_old += old
            total_new += new
            if i % 25 == 0 or i == len(files):
                print(f"  [{i}/{len(files)}] {p.name}: {old//1024} KB → {new//1024} KB")
        except Exception as e:
            print(f"  ✗ {p.name}: {e}", file=sys.stderr)
    print()
    print(f"Итого: {total_old/1024/1024:.1f} MB → {total_new/1024/1024:.1f} MB "
          f"(экономия {(1 - total_new/total_old)*100:.1f}%)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
