"""
Генератор изображений для игры Artículos.
Стиль: realistic_image + natural_light
Логика: каждая сцена диктует свою цветовую температуру.
"""

import requests
import os
import sys
import time
import unicodedata


def _read_api_key() -> str:
    key = os.environ.get("RECRAFT_API_KEY", "").strip()
    if key:
        return key
    lp = os.path.join(os.path.dirname(__file__), "local.properties")
    if os.path.exists(lp):
        for line in open(lp, encoding="utf-8-sig"):
            line = line.strip().lstrip("﻿")
            if line.startswith("RECRAFT_API_KEY="):
                return line.split("=", 1)[1].strip()
    sys.exit("ERROR: RECRAFT_API_KEY not found (env var or local.properties)")


API_KEY  = _read_api_key()
API_URL  = "https://external.api.recraft.ai/v1/images/generations"
OUT_DIR  = r"app\src\main\assets\word_images"

BASE = "realistic photo, cinematic, natural white balance true to the scene, large clear subject filling the frame, easy to understand at a glance, no text"

# (filename, español, prompt_сцена)
WORDS = [

    # ══ ЖИВОТНЫЕ ═══════════════════════════════════════════════
    ("gato",        "gato",        "a fluffy cat sitting on a windowsill, cozy warm home light"),
    ("perro",       "perro",       "a happy dog running in a park, natural daylight"),
    ("pajaro",      "pájaro",      "a colorful bird perched on a branch, soft morning light"),
    ("pez",         "pez",         "a bright fish underwater, clear blue water light"),
    ("caballo",     "caballo",     "a horse galloping in an open field, golden hour"),
    ("vaca",        "vaca",        "a cow standing in a green meadow, overcast natural light"),
    ("cerdo",       "cerdo",       "a pig on a farm, natural daylight"),
    ("conejo",      "conejo",      "a white rabbit on grass, soft natural light"),
    ("oso",         "oso",         "a bear in a forest, cool natural forest light"),
    ("leon",        "león",        "a lion resting on savanna, warm golden light"),
    ("tigre",       "tigre",       "a tiger in jungle, dappled cool light"),
    ("elefante",    "elefante",    "an elephant in nature, warm afternoon light"),
    ("mono",        "mono",        "a monkey climbing a tree, warm tropical light"),
    ("serpiente",   "serpiente",   "a snake coiled on a rock, cool natural light"),
    ("mariposa",    "mariposa",    "a butterfly on a flower, bright natural daylight"),
    ("abeja",       "abeja",       "a bee on a flower collecting pollen, bright daylight"),
    ("tortuga",     "tortuga",     "a turtle on a beach near water, warm coastal light"),
    ("delfin",      "delfín",      "a dolphin jumping out of the sea, bright blue water"),
    ("aguila",      "águila",      "an eagle flying high in the sky, cool blue sky"),

    # ══ ЕДА И НАПИТКИ ══════════════════════════════════════════
    ("manzana",     "manzana",     "a red apple on a wooden table, natural warm kitchen light"),
    ("naranja",     "naranja",     "an orange cut in half on a table, bright natural light"),
    ("platano",     "plátano",     "a bunch of bananas, bright neutral daylight"),
    ("uva",         "uva",         "a bunch of grapes on a vine, warm afternoon vineyard light"),
    ("fresa",       "fresa",       "fresh strawberries in a bowl, bright natural light"),
    ("limon",       "limón",       "a lemon cut open on a cutting board, cool bright kitchen light"),
    ("tomate",      "tomate",      "ripe tomatoes on a vine, warm garden light"),
    ("zanahoria",   "zanahoria",   "fresh carrots with greens on a table, neutral kitchen light"),
    ("patata",      "patata",      "potatoes in a rustic basket, warm earthy tones"),
    ("pan",         "pan",         "freshly baked bread loaf on wooden board, warm cozy kitchen light"),
    ("pizza",       "pizza",       "a whole pizza fresh from the oven, warm restaurant light"),
    ("hamburguesa", "hamburguesa", "a juicy burger on a plate, warm restaurant light"),
    ("huevo",       "huevo",       "eggs frying in a pan, warm morning kitchen light"),
    ("queso",       "queso",       "different cheeses on a wooden board, warm soft light"),
    ("leche",       "leche",       "a glass of milk on a table, clean neutral light"),
    ("cafe",        "café",        "a cup of coffee with steam, warm cozy cafe light"),
    ("te",          "té",          "a cup of hot tea with lemon, warm cozy light"),
    ("agua",        "agua",        "a glass of clear water, clean cool neutral light"),
    ("vino",        "vino",        "a glass of red wine on a table, warm evening light"),
    ("helado",      "helado",      "an ice cream cone being held, bright summer daylight"),
    ("pastel",      "pastel",      "a beautiful cake on a table, warm indoor light"),
    ("chocolate",   "chocolate",   "pieces of dark chocolate on a table, warm soft light"),
    ("arroz",       "arroz",       "a bowl of cooked white rice, neutral kitchen light"),
    ("sopa",        "sopa",        "a bowl of hot soup with steam, warm cozy kitchen light"),
    ("ensalada",    "ensalada",    "a fresh green salad in a bowl, bright natural light"),
    ("pollo",       "pollo",       "a roasted chicken on a plate, warm kitchen light"),
    ("carne",       "carne",       "a piece of grilled meat on a grill, warm fire light"),
    ("pescado",     "pescado",     "a whole grilled fish on a plate, warm restaurant light"),

    # ══ ДОМ ════════════════════════════════════════════════════
    ("casa",        "casa",        "a cozy house with a garden, warm afternoon light"),
    ("puerta",      "puerta",      "a wooden front door of a house, natural daylight"),
    ("ventana",     "ventana",     "a window with curtains, soft natural daylight"),
    ("mesa",        "mesa",        "a dining table set for meal, warm home light"),
    ("silla",       "silla",       "a wooden chair in a room, neutral indoor light"),
    ("cama",        "cama",        "a neatly made bed in a bedroom, soft morning light"),
    ("sofa",        "sofá",        "a comfortable sofa in a living room, warm indoor light"),
    ("lampara",     "lámpara",     "a floor lamp turned on in a dark room, warm glow"),
    ("espejo",      "espejo",      "a mirror on a wall in a room, neutral indoor light"),
    ("television",  "televisión",  "a flat screen TV on a wall, cool blue screen glow"),
    ("nevera",      "nevera",      "an open refrigerator full of food, cool white light"),
    ("cocina",      "cocina",      "a modern kitchen with countertops, neutral daylight"),
    ("bano",        "baño",        "a clean bathroom with tiles, cool neutral light"),
    ("escalera",    "escalera",    "a staircase inside a house, natural indoor light"),
    ("jardin",      "jardín",      "a beautiful home garden with flowers, warm sunlight"),
    ("llave",       "llave",       "a key held in a hand, neutral close-up light"),
    ("tejado",      "tejado",      "a rooftop of a house, bright outdoor daylight"),

    # ══ ОДЕЖДА ═════════════════════════════════════════════════
    ("camisa",      "camisa",      "a shirt hanging on a hanger, neutral soft light"),
    ("camiseta",    "camiseta",    "a t-shirt folded on a table, neutral light"),
    ("pantalon",    "pantalón",    "jeans laid on a bed, neutral indoor light"),
    ("vestido",     "vestido",     "a woman wearing a dress outdoors, warm golden hour"),
    ("abrigo",      "abrigo",      "a person wearing a coat in autumn street, cool natural light"),
    ("chaqueta",    "chaqueta",    "a jacket on a chair, neutral indoor light"),
    ("zapato",      "zapato",      "a pair of shoes on a floor, neutral light"),
    ("bota",        "bota",        "leather boots in autumn leaves, cool natural light"),
    ("sombrero",    "sombrero",    "a hat on a person outdoors, warm sunlight"),
    ("gorra",       "gorra",       "a baseball cap on a head, outdoor daylight"),
    ("mochila",     "mochila",     "a backpack on a school bench, natural daylight"),
    ("bolso",       "bolso",       "a handbag held by a woman, neutral light"),
    ("guante",      "guante",      "gloves on hands in winter, cold blue-grey light"),
    ("bufanda",     "bufanda",     "a scarf wrapped around neck in winter, cool light"),
    ("paraguas",    "paraguas",    "a person holding umbrella in the rain, grey rainy light"),

    # ══ ТРАНСПОРТ ══════════════════════════════════════════════
    ("coche",       "coche",       "a car on a city street, neutral daylight"),
    ("autobus",     "autobús",     "a city bus at a bus stop, neutral urban light"),
    ("metro",       "metro",       "a subway train in a station, cool artificial light"),
    ("tren",        "tren",        "a train at a railway station, natural daylight"),
    ("avion",       "avión",       "an airplane seen from below against clear sky, bright daylight"),
    ("barco",       "barco",       "a large ship on the sea, natural daylight"),
    ("bicicleta",   "bicicleta",   "a bicycle parked on a street, natural daylight"),
    ("moto",        "moto",        "a motorcycle on a road, warm afternoon light"),
    ("taxi",        "taxi",        "a yellow taxi on a city street, urban daylight"),
    ("helicoptero", "helicóptero", "a helicopter flying in the sky, bright blue sky light"),
    ("camion",      "camión",      "a large truck on a highway, neutral daylight"),
    ("ambulancia",  "ambulancia",  "an ambulance with lights on, cool urgent light"),

    # ══ ПРИРОДА ════════════════════════════════════════════════
    ("sol",         "sol",         "bright sun in a clear blue sky, intense natural light"),
    ("luna",        "luna",        "a full moon in a dark night sky, cool blue night light"),
    ("estrella",    "estrella",    "a starry night sky over a landscape, cool dark blue light"),
    ("nube",        "nube",        "white clouds in a blue sky, bright daylight"),
    ("lluvia",      "lluvia",      "heavy rain falling on a street, grey cool rainy light"),
    ("nieve",       "nieve",       "snow falling in a winter landscape, cold white light"),
    ("flor",        "flor",        "a bright flower in a garden, warm sunlight"),
    ("arbol",       "árbol",       "a tall tree in a park, natural green daylight"),
    ("montana",     "montaña",     "a majestic mountain with snow peak, cool crisp light"),
    ("mar",         "mar",         "ocean waves on a beach, bright natural coastal light"),
    ("playa",       "playa",       "a sunny beach with sand and sea, warm bright summer light"),
    ("bosque",      "bosque",      "a dense forest with sunbeams, cool green filtered light"),
    ("rio",         "río",         "a river flowing through landscape, natural daylight"),
    ("desierto",    "desierto",    "a vast desert with sand dunes, harsh bright hot light"),
    ("volcan",      "volcán",      "a volcano with smoke, dramatic dark warm light"),
    ("fuego",       "fuego",       "a campfire burning at night, warm orange fire glow"),
    ("piedra",      "piedra",      "rocks and stones on the ground, neutral natural light"),

    # ══ ТЕЛО ЧЕЛОВЕКА ══════════════════════════════════════════
    ("ojo",         "ojo",         "a close-up of a human eye, neutral soft light"),
    ("nariz",       "nariz",       "a close-up of a nose on a face, neutral soft light"),
    ("boca",        "boca",        "a close-up of a smiling mouth, soft neutral light"),
    ("mano",        "mano",        "human hands close-up, neutral soft light"),
    ("pie",         "pie",         "bare feet on a wooden floor, warm indoor light"),
    ("corazon",     "corazón",     "anatomical heart illustration style, neutral light"),
    ("brazo",       "brazo",       "a muscular arm flexing, neutral gym light"),
    ("cabeza",      "cabeza",      "a person's head and face portrait, soft natural light"),
    ("dedo",        "dedo",        "fingers close-up pointing, neutral light"),
    ("rodilla",     "rodilla",     "a person's knee close-up, neutral light"),

    # ══ УЧЁБА И РАБОТА ═════════════════════════════════════════
    ("libro",       "libro",       "an open book on a desk, warm study light"),
    ("lapiz",       "lápiz",       "a pencil on a notebook, neutral desk light"),
    ("boligrafo",   "bolígrafo",   "a pen writing on paper, neutral desk light"),
    ("cuaderno",    "cuaderno",    "an open notebook with writing, warm desk light"),
    ("escuela",     "escuela",     "a school building exterior, bright daylight"),
    ("clase",       "clase",       "a classroom with desks, neutral daylight"),
    ("mochila",     "mochila",     "a school backpack open with books, warm morning light"),
    ("examen",      "examen",      "a student writing an exam at a desk, neutral classroom light"),
    ("diccionario", "diccionario", "a thick dictionary book open, warm study light"),
    ("ordenador",   "ordenador",   "a laptop on a desk with coffee, warm home office light"),
    ("oficina",     "oficina",     "a modern office interior, cool neutral office light"),
    ("medico",      "médico",      "a doctor in white coat with stethoscope, cool clinical light"),
    ("enfermera",   "enfermera",   "a nurse in uniform in a hospital, cool clinical light"),
    ("bombero",     "bombero",     "a firefighter in uniform with equipment, dramatic warm fire light"),
    ("policia",     "policía",     "a police officer in uniform on a street, neutral daylight"),
    ("cocinero",    "cocinero",    "a chef cooking in a professional kitchen, warm kitchen light"),
    ("conductor",   "conductor",   "a person driving a car, natural interior car light"),

    # ══ ТЕХНОЛОГИИ ═════════════════════════════════════════════
    ("telefono",    "teléfono",    "a smartphone in a hand, neutral modern light"),
    ("camara",      "cámara",      "a camera being held, neutral daylight"),
    ("auricular",   "auricular",   "headphones on a table, clean neutral light"),
    ("teclado",     "teclado",     "hands typing on a keyboard, cool office light"),

    # ══ СПОРТ ══════════════════════════════════════════════════
    ("pelota",      "pelota",      "a football on a grass field, bright natural light"),
    ("piscina",     "piscina",     "a swimming pool with clear blue water, bright summer light"),
    ("gimnasio",    "gimnasio",    "inside a gym with equipment, cool artificial light"),
    ("trofeo",      "trofeo",      "a golden trophy cup on a podium, warm spotlight"),
    ("medalla",     "medalla",     "a gold medal close-up, neutral soft light"),
    ("estadio",     "estadio",     "a large football stadium full of fans, bright floodlight"),
    ("bicicleta",   "bicicleta",   "a bicycle on a road, natural daylight"),

    # ══ ЭМОЦИИ И СОСТОЯНИЯ ═════════════════════════════════════
    ("alegria",     "alegría",     "a person laughing joyfully outdoors, warm bright sunlight"),
    ("tristeza",    "tristeza",    "a person sitting alone looking sad, grey cool overcast light"),
    ("miedo",       "miedo",       "a person with scared expression in the dark, cold dramatic light"),
    ("enfado",      "enfado",      "a person with angry frustrated expression, neutral light"),
    ("sorpresa",    "sorpresa",    "a person with shocked surprised face, neutral light"),
    ("amor",        "amor",        "two people embracing, warm golden hour light"),
    ("llanto",      "llanto",      "a person crying with tears, soft neutral light"),
    ("risa",        "risa",        "a group of friends laughing together, warm natural light"),

    # ══ ЗДОРОВЬЕ ═══════════════════════════════════════════════
    ("hospital",    "hospital",    "hospital building exterior with entrance, cool neutral daylight"),
    ("medicina",    "medicina",    "medicine pills and bottles on a table, neutral clean light"),
    ("fiebre",      "fiebre",      "person sick in bed with thermometer, warm but subdued home light"),
    ("dolor",       "dolor",       "person holding head in pain, neutral dramatic light"),
    ("resfriado",   "resfriado",   "person blowing nose with tissue, neutral light"),
    ("gripe",       "gripe",       "person wrapped in blanket on sofa looking sick, warm subdued light"),
    ("garganta",    "garganta",    "sick person in bed with scarf and hot tea on nightstand, warm subdued home light"),
    ("herida",      "herida",      "a bandaged hand close-up, neutral clinical light"),

    # ══ ГОРОД ══════════════════════════════════════════════════
    ("ciudad",      "ciudad",      "a city skyline, neutral urban daylight"),
    ("calle",       "calle",       "a busy city street with people walking, natural urban light"),
    ("parque",      "parque",      "a park with trees and benches, warm sunlight"),
    ("tienda",      "tienda",      "a shop storefront on a street, natural daylight"),
    ("restaurante", "restaurante", "inside a cozy restaurant, warm ambient light"),
    ("mercado",     "mercado",     "a busy market with colorful stalls, bright natural light"),
    ("banco",       "banco",       "bank building exterior, neutral daylight"),
    ("hotel",       "hotel",       "a hotel lobby interior, warm elegant light"),
    ("aeropuerto",  "aeropuerto",  "inside a busy airport terminal, cool artificial light"),
    ("estacion",    "estación",    "a train station with platforms, natural and artificial mix"),
    ("museo",       "museo",       "inside a museum with art on walls, neutral gallery light"),
    ("iglesia",     "iglesia",     "a church building exterior, natural daylight"),
    ("biblioteca",  "biblioteca",  "inside a library with bookshelves, warm soft light"),
    ("supermercado","supermercado","inside a supermarket with shelves, cool artificial light"),

    # ══ СЕМЬЯ ══════════════════════════════════════════════════
    ("madre",       "madre",       "a mother hugging her child, warm natural home light"),
    ("padre",       "padre",       "a father playing with his child outdoors, warm sunlight"),
    ("hijo",        "hijo",        "a young boy playing, warm natural light"),
    ("hija",        "hija",        "a young girl smiling, warm natural light"),
    ("abuelo",      "abuelo",      "an elderly man sitting in a garden, warm afternoon light"),
    ("abuela",      "abuela",      "an elderly woman knitting at home, warm cozy light"),
    ("bebe",        "bebé",        "a baby sleeping in a crib, soft warm nursery light"),
    ("familia",     "familia",     "a family together at a dining table, warm home light"),
    ("boda",        "boda",        "a wedding ceremony, warm romantic light"),
    ("nino",        "niño",        "a young boy running in a park, bright natural daylight"),
    ("nina",        "niña",        "a young girl with a smile, bright natural daylight"),

    # ══ РАЗНОЕ ═════════════════════════════════════════════════
    ("dinero",      "dinero",      "cash money in hands, neutral natural light"),
    ("reloj",       "reloj",       "a wristwatch on a wrist, neutral close-up light"),
    ("telefono",    "teléfono",    "a smartphone on a table, neutral light"),
    ("regalo",      "regalo",      "a wrapped gift box with ribbon, warm festive light"),
    ("fiesta",      "fiesta",      "a birthday party with people and balloons, warm festive light"),
    ("musica",      "música",      "a musician playing guitar on a stage, warm stage light"),
    ("guitarra",    "guitarra",    "a guitar leaning against a wall, warm natural light"),
    ("piano",       "piano",       "a grand piano in a room, elegant natural light"),
    ("pintura",     "pintura",     "a person painting on canvas in a studio, neutral artist light"),
    ("foto",        "foto",        "a camera taking a photo of a landscape, natural daylight"),
    ("carta",       "carta",       "a handwritten letter on a table, warm desk light"),
    ("bandera",     "bandera",     "a flag waving in the wind, bright outdoor daylight"),
    ("corona",      "corona",      "a golden crown on a velvet surface, warm regal light"),
    ("anillo",      "anillo",      "a ring on a finger close-up, soft elegant light"),
    ("vela",        "vela",        "a candle burning in the dark, warm candlelight glow"),
    ("globo",       "globo",       "colorful balloons in the air, bright daylight"),
    ("mapa",        "mapa",        "a world map on a table with a coffee cup, neutral light"),
    ("maleta",      "maleta",      "a suitcase packed and ready, neutral light"),
    ("pasaporte",   "pasaporte",   "a passport on a table, neutral clean light"),
    ("billete",     "billete",     "a plane ticket held in hands, neutral light"),
    ("precio",      "precio",      "a price tag on a product, neutral shop light"),
    ("compra",      "compra",      "shopping bags in hands on a street, natural daylight"),
    ("capricho",    "capricho",    "a toddler lying on floor crying, parent standing nearby, natural home light"),
]


def normalize(word: str) -> str:
    """Убирает ударения: á→a, é→e и т.д."""
    return ''.join(
        c for c in unicodedata.normalize('NFD', word)
        if unicodedata.category(c) != 'Mn'
    )


def generate(prompt: str) -> str | None:
    full_prompt = f"{prompt}, {BASE}"
    try:
        resp = requests.post(
            API_URL,
            headers={"Authorization": f"Bearer {API_KEY}", "Content-Type": "application/json"},
            json={"prompt": full_prompt, "style": "realistic_image",
                  "substyle": "natural_light", "n": 1, "size": "1024x1024"},
            timeout=60,
        )
        if resp.status_code != 200:
            print(f"    ❌ HTTP {resp.status_code}: {resp.text[:150]}")
            return None
        return resp.json()["data"][0]["url"]
    except Exception as e:
        print(f"    ❌ {e}")
        return None


def download(url: str, path: str):
    img = requests.get(url, timeout=30)
    with open(path, "wb") as f:
        f.write(img.content)


def main():
    os.makedirs(OUT_DIR, exist_ok=True)

    # Убираем дубликаты по filename
    seen = set()
    unique = []
    for item in WORDS:
        fname = item[0]
        if fname not in seen:
            seen.add(fname)
            unique.append(item)

    total = len(unique)
    ok = skip = err = 0
    print(f"Генерация {total} изображений...\n")

    for i, (filename, word, prompt) in enumerate(unique, 1):
        path = os.path.join(OUT_DIR, f"{filename}.png")
        if os.path.exists(path):
            print(f"[{i:3}/{total}] ⏭  {word}")
            skip += 1
            continue

        print(f"[{i:3}/{total}] 🎨 {word}")
        url = generate(prompt)
        if url:
            download(url, path)
            size_kb = os.path.getsize(path) // 1024
            print(f"         ✅ {size_kb} KB")
            ok += 1
        else:
            err += 1

        time.sleep(0.4)

    print(f"\n{'='*40}")
    print(f"✅ Новых: {ok}  ⏭ Пропущено: {skip}  ❌ Ошибок: {err}")
    print(f"Папка: {OUT_DIR}")


if __name__ == "__main__":
    main()
