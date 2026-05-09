import json

with open('app/src/main/assets/spanish_vocab.json', encoding='utf-8') as f:
    data = json.load(f)

nouns = data.get('nouns', [])
print(f"Существительных всего: {len(nouns)}")
if nouns:
    print(f"Пример: {json.dumps(nouns[0], ensure_ascii=False)}")

el_la = [w for w in nouns if str(w.get('spanish','')).strip().lower().startswith(('el ', 'la '))]
print(f"\nС артиклем el/la: {len(el_la)}")

single = [w for w in el_la if len(str(w.get('spanish','')).strip().split()) == 2]
print(f"Одиночных слов (для игры): {len(single)}")

print("\nПервые 20:")
for w in single[:20]:
    sp = w.get('spanish','')
    ru = w.get('russian','')
    print(f"  {sp} = {ru}")
