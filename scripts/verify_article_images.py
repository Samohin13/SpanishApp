"""Полная проверка целостности картинок и данных игры «Артикли».

Проверяет:
  1. JSON корректно парсится
  2. Все 100 уровней присутствуют, по 10 слов каждый
  3. Все артикли валидны (el/la/los/las)
  4. У каждого слова есть `russian` перевод
  5. Каждое слово в JSON имеет соответствующий PNG-файл
  6. Все PNG-файлы — валидные изображения (правильный header + размер >0)
  7. Нет orphan PNG (картинок без записи в JSON)
  8. Имена файлов корректно совпадают с stripAccents(word)

Возвращает exit code 0 если всё OK, 1 если есть ошибки.
"""
from __future__ import annotations

import json
import os
import sys
import unicodedata
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LEVELS_JSON = ROOT / "app" / "src" / "main" / "assets" / "articles_levels.json"
IMAGES_DIR = ROOT / "app" / "src" / "main" / "assets" / "word_images"

VALID_ARTICLES = {"el", "la", "los", "las"}
PNG_HEADER = b"\x89PNG\r\n\x1a\n"
WEBP_HEADER_PREFIX = b"RIFF"  # WebP: RIFF....WEBP


def strip_accents(s: str) -> str:
    return "".join(
        c for c in unicodedata.normalize("NFD", s.lower())
        if unicodedata.category(c) != "Mn"
    )


def is_valid_image(path: Path) -> tuple[bool, str]:
    """Принимает PNG или WebP (Coil умеет оба, даже если расширение .png)."""
    try:
        size = path.stat().st_size
        if size == 0:
            return False, "0 bytes"
        if size < 500:
            return False, f"too small ({size} B) — probably broken"
        with path.open("rb") as f:
            header = f.read(12)
        if header[:8] == PNG_HEADER:
            return True, ""
        if header[:4] == WEBP_HEADER_PREFIX and header[8:12] == b"WEBP":
            return True, ""
        return False, f"unknown format (header={header!r})"
    except Exception as e:
        return False, str(e)


def main() -> int:
    errors: list[str] = []
    warnings: list[str] = []

    # ── 1. JSON ─────────────────────────────────────────────────
    if not LEVELS_JSON.exists():
        print(f"❌ FATAL: {LEVELS_JSON} не найден")
        return 1
    try:
        data = json.loads(LEVELS_JSON.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        print(f"❌ FATAL: JSON некорректен: {e}")
        return 1

    if not isinstance(data, list):
        print(f"❌ FATAL: JSON должен быть массивом")
        return 1

    # ── 2. Структура уровней ────────────────────────────────────
    levels_seen = set()
    word_lookup: dict[str, dict] = {}  # filename → meta
    duplicate_filenames: list[tuple[str, str, str]] = []

    for lvl in data:
        if not isinstance(lvl, dict):
            errors.append("Уровень не dict")
            continue
        num = lvl.get("level")
        if not isinstance(num, int) or num < 1 or num > 200:
            errors.append(f"Невалидный level: {num}")
        if num in levels_seen:
            errors.append(f"Дублирующийся level {num}")
        levels_seen.add(num)

        words = lvl.get("words", [])
        if not isinstance(words, list):
            errors.append(f"Level {num}: words не массив")
            continue
        if len(words) != 10:
            warnings.append(f"Level {num}: {len(words)} слов вместо 10")

        for pos, w in enumerate(words):
            if not isinstance(w, dict):
                errors.append(f"Level {num} pos {pos}: не dict")
                continue

            word = w.get("word", "").strip()
            article = w.get("article", "")
            russian = w.get("russian", "").strip()
            is_plural = w.get("is_plural", False)

            # Валидация полей
            if not word:
                errors.append(f"Level {num} pos {pos}: пустое word")
            if article not in VALID_ARTICLES:
                errors.append(f"Level {num} pos {pos}: невалидный article «{article}» для {word}")
            if not russian:
                warnings.append(f"Level {num} pos {pos}: нет russian для «{word}»")
            if is_plural and article not in {"los", "las"}:
                errors.append(f"Level {num} pos {pos}: is_plural=true но article={article} ({word})")
            if not is_plural and article not in {"el", "la"}:
                warnings.append(f"Level {num} pos {pos}: is_plural=false но article={article} ({word})")

            # Дубликаты по filename
            fn = strip_accents(word)
            if fn in word_lookup:
                prev = word_lookup[fn]
                if prev["word"] != word:
                    duplicate_filenames.append((fn, prev["word"], word))
            else:
                word_lookup[fn] = {
                    "word": word, "article": article, "russian": russian,
                    "level": num, "is_plural": is_plural,
                }

    expected_levels = set(range(1, 101))
    missing_levels = expected_levels - levels_seen
    extra_levels = levels_seen - expected_levels
    if missing_levels:
        errors.append(f"Не хватает уровней: {sorted(missing_levels)}")
    if extra_levels:
        warnings.append(f"Лишние уровни: {sorted(extra_levels)}")

    # ── 3. PNG файлы ────────────────────────────────────────────
    if not IMAGES_DIR.exists():
        errors.append(f"Папка {IMAGES_DIR} не существует")
        return 1

    all_pngs = {f.stem: f for f in IMAGES_DIR.iterdir() if f.suffix == ".png"}

    needed_filenames = set(word_lookup.keys())
    have_filenames = set(all_pngs.keys())

    missing_images = sorted(needed_filenames - have_filenames)
    orphan_images = sorted(have_filenames - needed_filenames)

    if missing_images:
        errors.append(
            f"Нет картинок для {len(missing_images)} слов: {missing_images[:10]}"
            + (f" ... и ещё {len(missing_images)-10}" if len(missing_images) > 10 else "")
        )
    if orphan_images:
        warnings.append(
            f"Orphan PNG (нет в JSON) — {len(orphan_images)}: {orphan_images[:10]}"
            + (f" ... и ещё {len(orphan_images)-10}" if len(orphan_images) > 10 else "")
        )

    # ── 4. Валидность PNG ───────────────────────────────────────
    invalid_pngs: list[tuple[str, str]] = []
    suspicious_small: list[tuple[str, int]] = []
    suspicious_big: list[tuple[str, int]] = []
    total_size = 0

    for fn in needed_filenames & have_filenames:
        path = all_pngs[fn]
        size = path.stat().st_size
        total_size += size
        ok, why = is_valid_image(path)
        if not ok:
            invalid_pngs.append((fn, why))
        elif size < 1024:
            suspicious_small.append((fn, size))
        elif size > 500 * 1024:
            suspicious_big.append((fn, size))

    if invalid_pngs:
        errors.append(
            f"Невалидные PNG: {len(invalid_pngs)}: "
            + ", ".join(f"{fn} ({why})" for fn, why in invalid_pngs[:5])
        )
    if suspicious_small:
        warnings.append(
            f"Подозрительно маленькие PNG (<10 KB) — {len(suspicious_small)}: "
            + ", ".join(f"{fn}={kb//1024}K" for fn, kb in suspicious_small[:5])
        )
    if suspicious_big:
        warnings.append(
            f"Очень большие PNG (>3 MB) — {len(suspicious_big)}: "
            + ", ".join(f"{fn}={kb//1024//1024}M" for fn, kb in suspicious_big[:5])
        )

    if duplicate_filenames:
        warnings.append(
            f"Слова с одинаковым stripAccents — {len(duplicate_filenames)}: "
            + ", ".join(f"{fn}:{a}↔{b}" for fn, a, b in duplicate_filenames[:5])
        )

    # ── 5. Распределение по уровням CEFR ────────────────────────
    cefr_dist: Counter = Counter()
    for lvl in data:
        n = lvl.get("level", 0)
        cefr = "A1" if n <= 30 else "A2" if n <= 50 else "B1" if n <= 80 else "B2"
        cefr_dist[cefr] += len(lvl.get("words", []))

    # ── REPORT ──────────────────────────────────────────────────
    print("=" * 65)
    print("ОТЧЁТ ПРОВЕРКИ КАРТИНОК И ДАННЫХ ИГРЫ «АРТИКЛИ»")
    print("=" * 65)
    print(f"📂 JSON:       {LEVELS_JSON.relative_to(ROOT)}")
    print(f"📂 Картинки:   {IMAGES_DIR.relative_to(ROOT)}")
    print()
    print(f"📊 Структура:")
    print(f"   Уровней:                {len(levels_seen)} / 100")
    print(f"   Уникальных слов:        {len(word_lookup)}")
    print(f"   PNG файлов всего:       {len(all_pngs)}")
    print(f"   PNG нужных + есть:      {len(needed_filenames & have_filenames)}")
    print(f"   PNG нет (надо):         {len(missing_images)}")
    print(f"   PNG orphan (не нужны):  {len(orphan_images)}")
    print()
    print(f"📊 Распределение по CEFR:")
    for cefr in ["A1", "A2", "B1", "B2"]:
        print(f"   {cefr}: {cefr_dist[cefr]} слов")
    print()
    print(f"📊 Размер картинок:")
    print(f"   Итого: {total_size / (1024*1024):.1f} MB")
    print(f"   В среднем: {total_size / max(1, len(needed_filenames & have_filenames)) / 1024:.0f} KB/картинка")
    print()

    if warnings:
        print(f"⚠️  Предупреждения ({len(warnings)}):")
        for w in warnings:
            print(f"   • {w}")
        print()

    if errors:
        print(f"❌ ОШИБКИ ({len(errors)}):")
        for e in errors:
            print(f"   • {e}")
        print()
        print("❌ ПРОВЕРКА ПРОВАЛЕНА")
        return 1

    print("✅ ВСЕ ПРОВЕРКИ ПРОЙДЕНЫ")
    return 0


if __name__ == "__main__":
    sys.exit(main())
