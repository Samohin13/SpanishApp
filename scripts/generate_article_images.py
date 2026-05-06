"""Download Articles game illustrations from Pexels (free stock photos).

Usage:
    python scripts/generate_article_images.py --one casa
    python scripts/generate_article_images.py --test       # 5 sample images
    python scripts/generate_article_images.py --world 1    # one world only
    python scripts/generate_article_images.py --all        # full batch (~180)

API key is read from local.properties (key: PEXELS_API_KEY) or env var.
Get a free key at https://www.pexels.com/api/ — takes ~1 minute, no card.

Pexels License: free for commercial use, no attribution required.
https://www.pexels.com/license/

Notes:
    * Each query returns the top photo for the search term. Pass --pick N
      (1..15) to choose a different result if the first one doesn't fit.
    * Images are saved as 1024×1024 PNG (cropped from the largest variant
      Pexels provides). Final WebP conversion is a separate step.
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

PEXELS_ENDPOINT = "https://api.pexels.com/v1/search"

# (imageRef, English search query, Spanish word)
WORLD_1_TEST: list[tuple[str, str, str]] = [
    ("casa.webp", "house", "casa"),
    ("libro.webp", "book", "libro"),
    ("perro.webp", "dog", "perro"),
    ("manzana.webp", "apple", "manzana"),
    ("coche.webp", "car", "coche"),
]


def read_api_key() -> str:
    env_key = os.environ.get("PEXELS_API_KEY", "").strip()
    if env_key:
        return env_key
    if not LOCAL_PROPS.exists():
        sys.exit(
            "ERROR: PEXELS_API_KEY not in env, and local.properties not found.\n"
            "Get a free key at https://www.pexels.com/api/ and add to local.properties:\n"
            "    PEXELS_API_KEY=your_key_here\n"
        )
    for line in LOCAL_PROPS.read_text(encoding="utf-8-sig").splitlines():
        line = line.strip().lstrip("﻿")
        if line.startswith("PEXELS_API_KEY="):
            return line.split("=", 1)[1].strip()
    sys.exit(
        "ERROR: PEXELS_API_KEY not found in env or local.properties.\n"
        "Get a free key at https://www.pexels.com/api/\n"
        "Add a line like: PEXELS_API_KEY=xxxxxxxxxxxxxxxxxxxx"
    )


def fetch_one(api_key: str, query: str, pick: int = 1) -> bytes:
    """Search Pexels for `query`, return PNG bytes of the `pick`-th result (1-based)."""
    resp = requests.get(
        PEXELS_ENDPOINT,
        headers={"Authorization": api_key},
        params={
            "query": query,
            "per_page": max(pick, 5),
            "orientation": "square",
            "size": "large",
        },
        timeout=30,
    )
    resp.raise_for_status()
    data = resp.json()
    photos = data.get("photos", [])
    if not photos:
        # fallback to landscape if no square match
        resp = requests.get(
            PEXELS_ENDPOINT,
            headers={"Authorization": api_key},
            params={"query": query, "per_page": max(pick, 5), "size": "large"},
            timeout=30,
        )
        resp.raise_for_status()
        photos = resp.json().get("photos", [])
    if not photos:
        raise RuntimeError(f"No Pexels photos found for query '{query}'")

    idx = min(pick, len(photos)) - 1
    photo = photos[idx]
    src = photo["src"]
    # large = ~940px, large2x = ~1880px; prefer large2x then large
    image_url = src.get("large2x") or src.get("large") or src.get("original")
    print(f"  → photographer: {photo.get('photographer', '?')} "
          f"(photo id {photo.get('id')})")

    img = requests.get(image_url, timeout=60)
    img.raise_for_status()
    return img.content


def run_batch(items: list[tuple[str, str, str]], world: int,
              pick: int = 1, force: bool = False) -> None:
    out_dir = ASSET_ROOT / f"world_{world}"
    out_dir.mkdir(parents=True, exist_ok=True)
    api_key = read_api_key()

    for i, (image_ref, query, _spanish) in enumerate(items, 1):
        target = out_dir / image_ref.replace(".webp", ".png")
        if target.exists() and not force:
            print(f"[{i}/{len(items)}] skip (exists): {image_ref}")
            continue
        print(f"[{i}/{len(items)}] fetching '{query}' for {image_ref}")
        try:
            png = fetch_one(api_key, query, pick=pick)
            target.write_bytes(png)
        except requests.HTTPError as e:
            print(f"  HTTP error: {e.response.status_code} {e.response.text[:200]}")
        except Exception as e:
            print(f"  error: {e}")
        time.sleep(0.5)  # polite throttle (Pexels limit: 200 req/hour)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--one", default=None,
                    help="fetch exactly ONE image for the given Spanish word "
                         "(saves quota while testing)")
    ap.add_argument("--test", action="store_true", help="fetch 5 test images")
    ap.add_argument("--world", type=int, choices=[1, 2, 3, 4, 5])
    ap.add_argument("--all", action="store_true")
    ap.add_argument("--pick", type=int, default=1,
                    help="pick the Nth Pexels result (1..15) — default 1; "
                         "raise this if the top photo isn't suitable")
    ap.add_argument("--force", action="store_true",
                    help="re-download even if the file already exists")
    args = ap.parse_args()

    if args.one:
        match = next((t for t in WORLD_1_TEST if t[2] == args.one), None)
        if match is None:
            sys.exit(f"--one: no test entry for '{args.one}' "
                     f"(known: {', '.join(t[2] for t in WORLD_1_TEST)})")
        run_batch([match], world=1, pick=args.pick, force=args.force)
        return
    if args.test:
        run_batch(WORLD_1_TEST, world=1, pick=args.pick, force=args.force)
    elif args.world:
        sys.exit(f"--world {args.world}: word list not implemented yet "
                 f"(parser for design doc TODO)")
    elif args.all:
        sys.exit("--all: word list not implemented yet (parser TODO)")
    else:
        ap.print_help()


if __name__ == "__main__":
    main()
