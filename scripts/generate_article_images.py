"""Generate Articles game illustrations via Recraft V3 API.

Usage:
    python scripts/generate_article_images.py --test       # 5 sample images
    python scripts/generate_article_images.py --world 1    # one world only
    python scripts/generate_article_images.py --all        # full batch (~180)

API key is read from local.properties (key: RECRAFT_API_KEY) and is never
echoed to stdout. Generated PNGs are saved to
app/src/main/assets/article_images/world_<N>/<imageRef>.

Pricing reference (verify on recraft.ai/pricing):
    Recraft V3 raster_illustration ≈ 1 credit/image ≈ $0.04
    Full batch of ~180 images ≈ $7-8

Notes:
    * The list of (word, world, imageRef, prompt_word) is loaded from
      docs/articles_game_design.md. For the first iteration we hardcode a
      small WORLD_1_TEST list — replace with the parser once the design
      doc is final.
    * Images are saved as 1024×1024 PNG; final WebP 256×256 conversion is
      a separate step (see scripts/compress_images.sh, TODO).
"""
from __future__ import annotations

import argparse
import os
import sys
import time
from pathlib import Path

import requests  # type: ignore

ROOT = Path(__file__).resolve().parent.parent
LOCAL_PROPS = ROOT / "local.properties"
ASSET_ROOT = ROOT / "app" / "src" / "main" / "assets" / "article_images"

RECRAFT_ENDPOINT = "https://external.api.recraft.ai/v1/images/generations"

PROMPT_TEMPLATE = (
    "professional studio photograph of a {subject}, soft natural lighting, "
    "plain light neutral background, sharp focus, high detail, "
    "centered composition, no text, no people unless the subject itself "
    "is a person, photorealistic"
)
RECRAFT_STYLE = "realistic_image"

WORLD_1_TEST: list[tuple[str, str, str]] = [
    # (imageRef, prompt_word_en, spanish_word)
    ("casa.webp", "house", "casa"),
    ("libro.webp", "book", "libro"),
    ("perro.webp", "dog", "perro"),
    ("manzana.webp", "apple", "manzana"),
    ("coche.webp", "car", "coche"),
]


def read_api_key() -> str:
    env_key = os.environ.get("RECRAFT_API_KEY", "").strip()
    if env_key:
        return env_key
    if not LOCAL_PROPS.exists():
        sys.exit(
            "ERROR: RECRAFT_API_KEY not in env, and local.properties not found.\n"
            "Either set the env var or create local.properties with:\n"
            "    RECRAFT_API_KEY=your_new_key_here\n"
        )
    for line in LOCAL_PROPS.read_text(encoding="utf-8-sig").splitlines():
        line = line.strip().lstrip("﻿")
        if line.startswith("RECRAFT_API_KEY="):
            return line.split("=", 1)[1].strip()
    sys.exit(
        "ERROR: RECRAFT_API_KEY not found in env or local.properties.\n"
        "Add a line like: RECRAFT_API_KEY=re_xxx"
    )


def generate_one(api_key: str, prompt_word: str, style_id: str | None) -> bytes:
    """Send one prompt, return PNG bytes."""
    payload: dict = {
        "prompt": PROMPT_TEMPLATE.format(subject=prompt_word),
        "model": "recraftv3",
        "style": RECRAFT_STYLE,
        "size": "1024x1024",
        "n": 1,
        "response_format": "url",
    }
    if style_id:
        payload["style_id"] = style_id
        payload.pop("style", None)

    resp = requests.post(
        RECRAFT_ENDPOINT,
        headers={"Authorization": f"Bearer {api_key}"},
        json=payload,
        timeout=120,
    )
    resp.raise_for_status()
    image_url = resp.json()["data"][0]["url"]

    img = requests.get(image_url, timeout=120)
    img.raise_for_status()
    return img.content


def run_batch(items: list[tuple[str, str, str]], world: int,
              style_id: str | None, force: bool = False) -> None:
    out_dir = ASSET_ROOT / f"world_{world}"
    out_dir.mkdir(parents=True, exist_ok=True)
    api_key = read_api_key()

    for i, (image_ref, prompt_word, _spanish) in enumerate(items, 1):
        target = out_dir / image_ref.replace(".webp", ".png")
        if target.exists() and not force:
            print(f"[{i}/{len(items)}] skip (exists): {image_ref}")
            continue
        print(f"[{i}/{len(items)}] generating {image_ref} (subject={prompt_word})")
        try:
            png = generate_one(api_key, prompt_word, style_id)
            target.write_bytes(png)
        except requests.HTTPError as e:
            print(f"  HTTP error: {e.response.status_code} {e.response.text[:200]}")
        except Exception as e:
            print(f"  error: {e}")
        time.sleep(1)  # polite throttle


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--test", action="store_true", help="generate 5 test images")
    ap.add_argument("--world", type=int, choices=[1, 2, 3, 4, 5])
    ap.add_argument("--all", action="store_true")
    ap.add_argument("--style-id", default=None,
                    help="Recraft style_id for cross-batch consistency "
                         "(create one via /v1/styles, then reuse)")
    ap.add_argument("--force", action="store_true",
                    help="re-generate images even if files already exist")
    args = ap.parse_args()

    if args.test:
        run_batch(WORLD_1_TEST, world=1, style_id=args.style_id, force=args.force)
    elif args.world:
        # TODO: load full per-world list from docs/articles_game_design.md
        sys.exit(f"--world {args.world}: word list not implemented yet "
                 f"(parser for design doc TODO)")
    elif args.all:
        sys.exit("--all: word list not implemented yet (parser TODO)")
    else:
        ap.print_help()


if __name__ == "__main__":
    main()
