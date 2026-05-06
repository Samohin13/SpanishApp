"""
Генерация оставшихся слов из словаря.
Промпт строится автоматически по слову + русский перевод.
Запускать батч за батчем: python generate_batch.py 1
"""

import json, os, sys, time, unicodedata, requests

API_KEY = "sQGtknJ2GqNCnMJMBptWekBnqiT0Rtxr1frd2LSW46BpthPoLOLNJ0HSLYS7p8dt"
API_URL = "https://external.api.recraft.ai/v1/images/generations"
OUT_DIR = r"app\src\main\assets\word_images"
BATCH_SIZE = 50

BASE = ("realistic photo, cinematic, natural white balance true to the scene, "
        "large clear subject filling the frame, easy to understand at a glance, no text")

# Умные подсказки по категориям
CATEGORY_HINTS = {
    "familia_personas": "person portrait, natural home environment",
    "cuerpo":           "human body part close-up, neutral soft light",
    "ropa_accesorios":  "clothing item clearly visible, neutral light",
    "comida_bebida":    "food item on a table, natural kitchen light",
    "animales":         "animal in natural environment, natural light",
    "casa_hogar":       "home interior item, natural indoor light",
    "transporte":       "vehicle on road or in environment, natural light",
    "naturaleza":       "nature scene, natural outdoor light",
    "ciudad_lugares":   "urban location or building, natural daylight",
    "trabajo_profesion":"person at work, natural work environment light",
    "educacion":        "school or study related, natural light",
    "deportes":         "sport activity or equipment, bright natural light",
    "tecnologia":       "tech device or gadget, neutral modern light",
    "salud_medicina":   "medical or health related scene, clean neutral light",
    "emociones":        "person showing emotion clearly, natural light",
    "tiempo_clima":     "weather or nature scene, natural outdoor light",
    "colores_formas":   "object showing color or shape, neutral light",
    "musica_arte":      "musical instrument or art, warm creative light",
    "viajes":           "travel scene or location, natural daylight",
    "compras":          "shopping scene or item, natural light",
}

def strip_accents(s):
    return ''.join(c for c in unicodedata.normalize('NFD', s)
                   if unicodedata.category(c) != 'Mn')

def build_prompt(word, russian, category):
    hint = CATEGORY_HINTS.get(category, "natural environment, natural light")
    # Берём первый вариант перевода (до / или ,)
    ru_clean = russian.split('/')[0].split(',')[0].strip()
    return f"{word}, {ru_clean}, {hint}"

def load_missing():
    with open('app/src/main/assets/spanish_vocab.json', encoding='utf-8') as f:
        data = json.load(f)
    nouns = data.get('nouns', [])
    single = [w for w in nouns
              if str(w.get('spanish','')).strip().lower().startswith(('el ', 'la '))
              and len(str(w.get('spanish','')).strip().split()) == 2]

    existing = {f[:-4] for f in os.listdir(OUT_DIR) if f.endswith('.png')}

    missing = []
    for w in single:
        sp    = w.get('spanish','').strip()
        word  = sp.split()[1].lower()
        fname = strip_accents(word)
        if fname not in existing:
            missing.append({
                "filename": fname,
                "word":     word,
                "russian":  w.get('russian',''),
                "category": w.get('category',''),
            })
    return missing

def generate(prompt):
    try:
        r = requests.post(API_URL,
            headers={"Authorization": f"Bearer {API_KEY}", "Content-Type": "application/json"},
            json={"prompt": f"{prompt}, {BASE}",
                  "style": "realistic_image", "substyle": "natural_light",
                  "n": 1, "size": "1024x1024"},
            timeout=60)
        if r.status_code == 200:
            return r.json()["data"][0]["url"]
        print(f"    HTTP {r.status_code}: {r.text[:120]}")
        return None
    except Exception as e:
        print(f"    ERROR: {e}")
        return None

def download(url, path):
    img = requests.get(url, timeout=30)
    with open(path, 'wb') as f:
        f.write(img.content)

def main():
    batch_num = int(sys.argv[1]) if len(sys.argv) > 1 else 1
    os.makedirs(OUT_DIR, exist_ok=True)

    missing = load_missing()
    total_missing = len(missing)

    start = (batch_num - 1) * BATCH_SIZE
    end   = start + BATCH_SIZE
    batch = missing[start:end]

    if not batch:
        print(f"Батч {batch_num} пуст. Всего осталось: {total_missing}")
        return

    total_batches = (total_missing + BATCH_SIZE - 1) // BATCH_SIZE
    print(f"Батч {batch_num}/{total_batches}  |  слов в батче: {len(batch)}  |  осталось всего: {total_missing}")
    print(f"{'='*55}")

    ok = err = 0
    for i, w in enumerate(batch, 1):
        path   = os.path.join(OUT_DIR, f"{w['filename']}.png")
        prompt = build_prompt(w['word'], w['russian'], w['category'])
        print(f"[{i:2}/{len(batch)}] {w['word']:20} ({w['russian'][:25]})")

        url = generate(prompt)
        if url:
            download(url, path)
            kb = os.path.getsize(path) // 1024
            print(f"        ✅ {kb} KB")
            ok += 1
        else:
            print(f"        ❌ пропущено")
            err += 1

        time.sleep(0.4)

    remaining_after = total_missing - ok
    print(f"\n{'='*55}")
    print(f"✅ {ok}  ❌ {err}  |  осталось после батча: {remaining_after}")
    if batch_num < total_batches:
        print(f"Следующий: python generate_batch.py {batch_num + 1}")
    else:
        print("🎉 Все слова сгенерированы!")

if __name__ == "__main__":
    main()
