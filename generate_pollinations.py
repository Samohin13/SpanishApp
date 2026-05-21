"""Fallback генератор для слов которые не сгенерил Recraft (закончились кредиты).
Использует Pollinations.ai — бесплатно, без ключа, FLUX modelo.
"""
import json, os, time, unicodedata, urllib.parse, sys
from pathlib import Path
import requests  # type: ignore

ROOT = Path(__file__).resolve().parent
LEVELS = ROOT / "app/src/main/assets/articles_levels.json"
DIR = ROOT / "app/src/main/assets/word_images"

def strip(s):
    return "".join(c for c in unicodedata.normalize("NFD", s.lower())
                   if unicodedata.category(c) != "Mn")

# Те же SCENES из generate_missing.py для consistency
SCENES = {}

def prompt_for(meta):
    article = meta["article"]
    word = meta["word"]
    russian = meta["russian"]
    plural = "several " if meta.get("is_plural") else "a single "
    subject = SCENES.get(strip(word), f"{plural}{russian} (Spanish: {article} {word})")
    return (
        f"Simple iconic photograph: {subject}. "
        f"Anyone seeing this must instantly understand «{russian}». "
        f"Single subject, fully visible, centered, fills 70% of frame. "
        f"NO hands, NO phones/tablets/screens, NO frames, NO text, NO logos. "
        f"Plain background, soft natural light, photorealistic."
    )

def fetch(prompt: str) -> bytes | None:
    enc = urllib.parse.quote(prompt)
    url = f"https://image.pollinations.ai/prompt/{enc}?width=1024&height=1024&nologo=true&model=flux"
    try:
        r = requests.get(url, timeout=180)
        if r.status_code == 200 and len(r.content) > 5000:
            return r.content
        print(f"    ❌ HTTP {r.status_code}, size={len(r.content)}")
    except Exception as e:
        print(f"    ❌ {e}")
    return None

def main():
    data = json.loads(LEVELS.read_text(encoding="utf-8"))
    needed = {}
    for lvl in data:
        for w in lvl["words"]:
            fn = strip(w["word"])
            if fn not in needed:
                needed[fn] = w
    have = {f.stem for f in DIR.iterdir() if f.suffix == ".png"}
    missing = [(fn, needed[fn]) for fn in needed if fn not in have]
    if not missing:
        print("Нет недостающих слов.")
        return
    print(f"Pollinations.ai — генерация {len(missing)} картинок\n")
    ok = err = 0
    for i, (fn, meta) in enumerate(missing, 1):
        spanish = f"{meta['article']} {meta['word']}"
        print(f"[{i:2}/{len(missing)}] 🎨 {spanish:25} = {meta['russian']}")
        png = fetch(prompt_for(meta))
        if png:
            (DIR / f"{fn}.png").write_bytes(png)
            print(f"         ✅ {len(png)//1024} KB")
            ok += 1
        else:
            err += 1
        time.sleep(2)  # вежливый rate limit
    print(f"\n✅ Сделано: {ok}   ❌ Ошибок: {err}")

if __name__ == "__main__":
    main()
