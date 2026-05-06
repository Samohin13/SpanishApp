"""Generate Articles game illustrations via Recraft V3 API.

Usage:
    python scripts/generate_article_images.py --test       # 5 sample images
    python scripts/generate_article_images.py --world 1    # one world only
    python scripts/generate_article_images.py --all        # full batch (~173)

API key is read from local.properties (key: RECRAFT_API_KEY).
Generated PNGs are saved to app/src/main/assets/article_images/.
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
    "clean product photography of a single {subject}, "
    "pure plain white seamless background, "
    "full subject visible centered in frame, "
    "soft even studio lighting, sharp focus throughout entire subject, "
    "no environment, no props, no surface, no text, "
    "photorealistic, encyclopedia-style catalog photo"
)
RECRAFT_STYLE = "realistic_image"

# imageRef → English prompt subject
IMAGE_PROMPTS: dict[str, str] = {
    # ── World 1 · Countable objects ──────────────────────────────────
    "libro":        "hardcover book",
    "mesa":         "wooden table",
    "silla":        "chair",
    "cama":         "bed with pillows",
    "puerta":       "wooden door",
    "ventana":      "window",
    "casa":         "house",
    "coche":        "car",
    "tren":         "train",
    "avion":        "airplane",
    "barco":        "sailboat",
    "perro":        "dog",
    "gato":         "cat",
    "caballo":      "horse",
    "pajaro":       "bird",
    "manzana":      "red apple",
    "naranja":      "orange fruit",
    "platano":      "banana",
    "pan":          "bread loaf",
    "queso":        "cheese wheel",
    "nino":         "young boy",
    "nina":         "young girl",
    "hombre":       "adult man",
    "mujer":        "adult woman",
    "amigo":        "friendly person waving",
    "arbol":        "tree",
    "flor":         "flower",
    "sol":          "sun",
    "luna":         "crescent moon",
    "estrella":     "star",
    "rio":          "river",
    "montana":      "mountain",
    "nube":         "cloud",
    "ojo":          "human eye closeup",
    "boca":         "human mouth",
    "pierna":       "human leg",
    "brazo":        "human arm",
    "pie":          "human foot",
    "dedo":         "human finger",
    "oreja":        "human ear",
    "nariz":        "human nose",
    "calle":        "city street",
    "plaza":        "town square plaza with fountain",
    "parque":       "park with green trees",
    "escuela":      "school building",
    "hospital":     "hospital building with red cross",
    "tienda":       "small shop store front",
    "iglesia":      "church building",
    "museo":        "museum building with columns",
    "reloj":        "wall clock",
    "ciudad":       "city skyline",
    "universidad":  "university building",
    "cancion":      "musical note",
    "estacion":     "train station building",
    "paz":          "white dove",
    "luz":          "glowing lightbulb",
    "flor2":        "colorful flower bouquet",
    "color":        "color wheel palette",
    "amor":         "red heart",
    "dolor":        "person holding head in pain",
    "pez":          "fish",
    "arbol2":       "oak tree",
    "sal":          "salt shaker",
    "miel":         "honey jar with honeycomb",
    "reloj2":       "wristwatch",
    "dia":          "sunny day sky",
    "problema":     "question mark symbol",
    "tema":         "notepad with pen",
    "sistema":      "connected gears mechanism",
    "mapa":         "folded map",
    "clima":        "weather icon sun and cloud",
    "mano":         "open human hand",
    "foto":         "photograph frame",
    "moto":         "motorcycle",
    "radio":        "vintage radio device",
    "agua":         "glass of clear water",
    "alma":         "glowing spirit silhouette",
    "aguila":       "eagle bird",
    "hambre":       "empty bowl",
    "arma":         "sword",
    # ── World 2 · Abstract concepts ──────────────────────────────────
    "amor_abs":     "glowing heart symbol love",
    "libertad":     "bird flying free open sky",
    "felicidad":    "happy smiley face",
    "tristeza":     "sad face with tears",
    "miedo":        "scared ghost",
    "esperanza":    "small green sprout growing",
    "suerte":       "four leaf clover",
    "vida":         "green leaf",
    "muerte":       "skull",
    "tiempo":       "hourglass with sand",
    "paciencia":    "person meditating lotus pose",
    "alegria":      "jumping person arms raised",
    "ira":          "flame fire anger",
    "sueno":        "sleeping moon with stars",
    "realidad":     "magnifying glass",
    "verdad":       "scale of justice balance",
    "mentira":      "pinocchio long nose",
    "justicia":     "blindfolded justice statue",
    "belleza":      "rose flower",
    "fuerza":       "flexed strong arm muscle",
    "valor":        "shield with star",
    "sabiduria":    "wise owl",
    "ignorancia":   "blindfold",
    "cultura":      "classical greek building",
    "religion":     "praying hands",
    "politica":     "government capitol building",
    "dilema":       "fork in the road sign",
    "trauma":       "broken heart",
    # ── World 3 · Days and time ───────────────────────────────────────
    "dia_lunes":    "Monday calendar page",
    "dia_martes":   "Tuesday calendar page",
    "dia_miercoles":"Wednesday calendar page",
    "dia_jueves":   "Thursday calendar page",
    "dia_viernes":  "Friday calendar page",
    "dia_sabado":   "Saturday calendar page",
    "dia_domingo":  "Sunday calendar page",
    "hora_01":      "analog clock showing 1 o clock",
    "hora_02":      "analog clock showing 2 o clock",
    "hora_03":      "analog clock showing 3 o clock",
    "hora_04":      "analog clock showing 4 o clock",
    "hora_05":      "analog clock showing 5 o clock",
    "hora_06":      "analog clock showing 6 o clock",
    "hora_07":      "analog clock showing 7 o clock",
    "hora_08":      "analog clock showing 8 o clock",
    "hora_09":      "analog clock showing 9 o clock",
    "hora_10":      "analog clock showing 10 o clock",
    "hora_11":      "analog clock showing 11 o clock",
    "hora_12":      "analog clock showing 12 o clock",
    "manana":       "beautiful sunrise morning",
    "tarde":        "golden afternoon sun",
    "noche":        "night sky with stars",
    "semana":       "weekly calendar planner",
    "mes":          "monthly calendar page",
    "ano":          "yearly calendar",
    # ── World 4 · Titles and ranks ────────────────────────────────────
    "doctor":       "male doctor in white coat with stethoscope",
    "doctora":      "female doctor in white coat with stethoscope",
    "profesor":     "male teacher at blackboard",
    "profesora":    "female teacher at blackboard",
    "senor":        "gentleman in business suit",
    "senora":       "elegant woman",
    "presidente":   "person at presidential podium",
    "ingeniero":    "engineer with hard hat and blueprints",
    "abogada":      "female lawyer with briefcase",
    "rey":          "king with golden crown and robe",
    "capitan":      "ship captain with hat",
    "maestra":      "female school teacher",
    "don":          "distinguished elderly gentleman",
    "dona":         "distinguished elderly woman",
    "papa":         "pope with white papal hat",
    "general":      "military general with medals and uniform",
    # ── World 5 · Substances and categories ──────────────────────────
    "cafe":         "cup of hot coffee",
    "musica":       "musical notes",
    "oro":          "gold bar shiny",
    "leche":        "glass of milk",
    "vino":         "red wine bottle",
    "azucar":       "sugar bowl",
    "arroz":        "bowl of white rice",
    "aceite":       "olive oil bottle",
    "plata":        "silver bar",
    "hierro":       "iron metal ingot",
    "madera":       "wooden plank",
    "plastico":     "plastic material sheet",
    "cristal":      "glass crystal",
    "papel":        "stack of paper sheets",
    "algodon":      "cotton ball fluffy",
    "lana":         "ball of wool yarn",
    "cuero":        "brown leather piece",
    "cobre":        "copper wire",
    "fuego":        "fire flame",
    "aire":         "wind blowing leaves",
    "tierra":       "soil earth dirt",
    "gasolina":     "gasoline fuel pump nozzle",
    "harina":       "flour bag open",
}

# Which imageRefs belong to each world (in generation order)
WORLD_IMAGES: dict[int, list[str]] = {
    1: [
        "libro", "mesa", "silla", "cama", "puerta", "ventana", "casa",
        "coche", "tren", "avion", "barco",
        "perro", "gato", "caballo", "pajaro",
        "manzana", "naranja", "platano", "pan", "queso",
        "nino", "nina", "hombre", "mujer", "amigo",
        "arbol", "flor", "sol", "luna", "estrella", "rio", "montana", "nube",
        "ojo", "boca", "pierna", "brazo", "pie", "dedo", "oreja", "nariz",
        "calle", "plaza", "parque", "escuela", "hospital", "tienda",
        "iglesia", "museo", "reloj",
        "ciudad", "universidad", "cancion", "estacion", "paz", "luz",
        "flor2", "color", "amor", "dolor", "pez", "arbol2", "sal", "miel", "reloj2",
        "dia", "problema", "tema", "sistema", "mapa", "clima",
        "mano", "foto", "moto", "radio",
        "agua", "alma", "aguila", "hambre", "arma",
    ],
    2: [
        "amor_abs", "libertad", "felicidad", "tristeza", "miedo",
        "esperanza", "suerte", "vida", "muerte", "tiempo", "paciencia",
        "alegria", "ira", "sueno", "realidad", "verdad", "mentira",
        "justicia", "belleza", "fuerza", "valor",
        "sabiduria", "ignorancia", "cultura", "religion", "politica",
        "dilema", "trauma",
    ],
    3: [
        "dia_lunes", "dia_martes", "dia_miercoles", "dia_jueves",
        "dia_viernes", "dia_sabado", "dia_domingo",
        "hora_01", "hora_02", "hora_03", "hora_04", "hora_05", "hora_06",
        "hora_07", "hora_08", "hora_09", "hora_10", "hora_11", "hora_12",
        "manana", "tarde", "noche", "semana", "mes", "ano",
    ],
    4: [
        "doctor", "doctora", "profesor", "profesora", "senor", "senora",
        "presidente", "ingeniero", "abogada", "rey", "capitan", "maestra",
        "don", "dona", "papa", "general",
    ],
    5: [
        "cafe", "musica", "oro", "leche", "vino", "azucar", "arroz",
        "aceite", "plata", "hierro", "madera", "plastico", "cristal",
        "papel", "algodon", "lana", "cuero", "cobre",
        "fuego", "aire", "tierra", "gasolina", "harina",
    ],
}

WORLD_1_TEST: list[str] = ["casa", "libro", "perro", "manzana", "coche"]


def read_api_key() -> str:
    env_key = os.environ.get("RECRAFT_API_KEY", "").strip()
    if env_key:
        return env_key
    if not LOCAL_PROPS.exists():
        sys.exit(
            "ERROR: RECRAFT_API_KEY not in env, and local.properties not found.\n"
            "Either set the env var or create local.properties with:\n"
            "    RECRAFT_API_KEY=your_key_here\n"
        )
    for line in LOCAL_PROPS.read_text(encoding="utf-8-sig").splitlines():
        line = line.strip()
        if line.startswith("RECRAFT_API_KEY="):
            return line.split("=", 1)[1].strip()
    sys.exit("ERROR: RECRAFT_API_KEY not found in local.properties.")


def generate_one(api_key: str, prompt_word: str, style_id: str | None) -> bytes:
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


def run_batch(refs: list[str], style_id: str | None, force: bool = False) -> None:
    ASSET_ROOT.mkdir(parents=True, exist_ok=True)
    api_key = read_api_key()
    total = len(refs)
    for i, ref in enumerate(refs, 1):
        target = ASSET_ROOT / f"{ref}.png"
        if target.exists() and not force:
            print(f"[{i}/{total}] skip (exists): {ref}.png")
            continue
        prompt = IMAGE_PROMPTS.get(ref, ref.replace("_", " "))
        print(f"[{i}/{total}] generating {ref}.png  (subject={prompt})")
        try:
            png = generate_one(api_key, prompt, style_id)
            target.write_bytes(png)
        except requests.HTTPError as e:
            print(f"  HTTP error: {e.response.status_code} {e.response.text[:200]}")
        except Exception as e:
            print(f"  error: {e}")
        time.sleep(1)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--test", action="store_true", help="generate 5 test images")
    ap.add_argument("--world", type=int, choices=[1, 2, 3, 4, 5])
    ap.add_argument("--all", action="store_true", help="generate all ~173 images")
    ap.add_argument("--style-id", default=None,
                    help="Recraft style_id for visual consistency across batches")
    ap.add_argument("--force", action="store_true",
                    help="re-generate even if file already exists")
    args = ap.parse_args()

    if args.test:
        run_batch(WORLD_1_TEST, style_id=args.style_id, force=args.force)
    elif args.world:
        run_batch(WORLD_IMAGES[args.world], style_id=args.style_id, force=args.force)
    elif getattr(args, "all"):
        all_refs: list[str] = []
        seen: set[str] = set()
        for world_refs in WORLD_IMAGES.values():
            for ref in world_refs:
                if ref not in seen:
                    all_refs.append(ref)
                    seen.add(ref)
        run_batch(all_refs, style_id=args.style_id, force=args.force)
    else:
        ap.print_help()


if __name__ == "__main__":
    main()
