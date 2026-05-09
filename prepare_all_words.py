"""
Готовит полный список всех 585 существительных из словаря
и выводит те, для которых ещё нет картинки.
"""
import json, os, unicodedata

def strip_accents(s):
    return ''.join(c for c in unicodedata.normalize('NFD', s)
                   if unicodedata.category(c) != 'Mn')

with open('app/src/main/assets/spanish_vocab.json', encoding='utf-8') as f:
    data = json.load(f)

nouns = data.get('nouns', [])
single = [w for w in nouns
          if str(w.get('spanish','')).strip().lower().startswith(('el ', 'la '))
          and len(str(w.get('spanish','')).strip().split()) == 2]

OUT_DIR = r'app\src\main\assets\word_images'
os.makedirs(OUT_DIR, exist_ok=True)

existing = {f[:-4] for f in os.listdir(OUT_DIR) if f.endswith('.png')}

missing = []
for w in single:
    sp = w.get('spanish','').strip()
    parts = sp.split()
    word = parts[1].lower()
    filename = strip_accents(word)
    ru = w.get('russian','')
    if filename not in existing:
        missing.append((filename, word, ru))

print(f"Всего слов: {len(single)}")
print(f"Уже есть картинок: {len(existing)}")
print(f"Нужно сгенерировать: {len(missing)}")
print()

# Разбиваем на батчи по 50
batch_size = 50
for i in range(0, len(missing), batch_size):
    batch = missing[i:i+batch_size]
    num = i//batch_size + 1
    print(f"--- Батч {num} ({len(batch)} слов) ---")
    for fn, word, ru in batch:
        print(f"  {fn:20} | {word:20} | {ru}")
    print()
