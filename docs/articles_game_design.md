# Artículos · дизайн-док переработки игры

> Версия 1 · 2026-05-05 · ветка `claude/review-first-game-gR6dc`
> Статус: на ревью у пользователя
> Цель: превратить тренажёр «el/la» в полноценный курс определённого артикля
> на 100 уровней, разбитых на 5 тематических миров.

---

## 1. Концепция

5 миров × 20 уровней = **100 уровней**. Каждый мир покрывает один из пяти
основных случаев употребления определённого артикля.

| Мир | Тема | Уровни | Кнопки ответа | Ключевое правило |
|---|---|---|---|---|
| 1 | 🧱 Исчисляемые предметы | 1–20 | EL · LA | базовый род + исключения |
| 2 | 💭 Абстрактные понятия | 21–40 | EL · LA | род у абстракций (`-ción → la`, `-ma → el`) |
| 3 | 🕐 Дни и время | 41–60 | EL · LA · LOS · LAS | `el lunes` (один раз) vs `los lunes` (регулярно), часы — всегда `la/las` |
| 4 | 🎩 Титулы и звания | 61–80 | EL · LA · Ø | артикль в 3-м лице, Ø при обращении |
| 5 | 🌾 Вещества и категории | 81–100 | EL · LA · Ø | артикль для категории/обобщения, Ø для неопределённого количества |

Открытие следующего мира — после набора **30⭐ из 60** в предыдущем.

---

## 2. Геймплей

### Структура уровня

1. Первые 3 уровня каждого мира — **TUTORIAL**: карточка с правилом и
   3 примерами перед началом, без таймера, всегда показан `ruleHint`.
2. Уровни 4–10 мира — основной материал, мягкий таймер 9–12с.
3. Уровни 11–15 — добавляются исключения / редкие слова, таймер 5–7с.
4. Уровни 16–18 — контекстные фразы (одно слово ↔ разные ответы), штраф за
   ошибку.
5. Уровни 19–20 — босс-уровни: 15 раундов, 4с, без подсказок, всё перемешано.

### Один раунд

```
┌─────────────────────────────────────┐
│  Уровень 47 / 100      XP: 120      │
│  ▓▓▓▓▓▓▓░░░░░  раунд 6/12           │
│  🔥 серия: 3 (×1.5)                  │
│                                      │
│        [🖼️ картинка слова]           │
│         Nos vemos ___ lunes          │
│                                      │
│  💡 Конкретный день недели → артикль │
│                                      │
│  ┌────────┬─────────┬─────────┐     │
│  │  EL    │   LOS   │    Ø    │     │
│  └────────┴─────────┴─────────┘     │
└─────────────────────────────────────┘
```

После ответа: ✅/❌, TTS озвучивает правильный вариант (включая ошибку),
1 секунду паузы — и следующий раунд.

### Правила

| Правило | Значение |
|---|---|
| XP за раунд | +10 верный, +5 бонус если ответил быстрее половины таймера |
| Серия (streak) | каждые 5 верных подряд → ×0.5 к множителю, потолок ×3 |
| Штраф | начиная с уровня 41: −5 XP за ошибку |
| Подсказка | кнопка 💡 — показывает `ruleHint`, тратит одну из 1–3 разрешённых |
| TTS | озвучивает правильный ответ всегда (и при ошибке) |
| `error_weight` | слова, на которых ошибся, чаще возвращаются в следующих раундах |
| Звёзды | 3⭐ ≥90% · 2⭐ ≥70% · 1⭐ ≥50% · 0⭐ ниже |
| Открытие уровня | минимум 1⭐ на предыдущем |
| Открытие мира | ≥30⭐ из 60 в предыдущем |

---

## 3. Модель данных

### `ArticleWordEntity` (расширение существующей)

```kotlin
@Entity(tableName = "article_words")
data class ArticleWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,                       // "casa", "café"
    val article: String,                    // "el" | "la" | "los" | "las" | "" (Ø)
    @ColumnInfo(name = "case_type")
    val caseType: String,                   // countable | abstract | time | title | substance
    @ColumnInfo(name = "sub_case")
    val subCase: String = "",               // напр. "exception", "address", "category"
    val template: String? = null,           // "Nos vemos ___ lunes" — null если голое слово
    @ColumnInfo(name = "rule_hint")
    val ruleHint: String,                   // текст подсказки
    @ColumnInfo(name = "options")
    val options: String,                    // CSV: "el,la" | "el,la,los,las" | "el,la,"
    @ColumnInfo(name = "image_ref")
    val imageRef: String,                   // имя файла в assets/article_images/
    val level: String,                      // CEFR: A1, A2, B1, B2, C1
    @ColumnInfo(name = "difficulty_tier")
    val difficultyTier: Int = 1,            // 1 (легко) … 5 (босс)
    @ColumnInfo(name = "error_weight")
    var errorWeight: Int = 0
)
```

### Миграция БД

`AppDatabase` version 9 → 10. `MIGRATION_9_10`:

```sql
DROP TABLE article_words;
CREATE TABLE article_words (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    word TEXT NOT NULL,
    article TEXT NOT NULL,
    case_type TEXT NOT NULL,
    sub_case TEXT NOT NULL DEFAULT '',
    template TEXT,
    rule_hint TEXT NOT NULL,
    options TEXT NOT NULL,
    image_ref TEXT NOT NULL,
    level TEXT NOT NULL,
    difficulty_tier INTEGER NOT NULL DEFAULT 1,
    error_weight INTEGER NOT NULL DEFAULT 0
);
```

Старая `article_level_progress` остаётся для совместимости с главным
экраном, но в логике игры не используется — прогресс ведём в
`game_level_progress`.

---

## 4. Контент

См. отдельные секции 4.1–4.5 ниже. Итого **~185 уникальных картинок**.

### 4.1. Мир 1 · Исчисляемые предметы (80 слов)

**Правило для tutorial:**
> Перед предметами, которые можно посчитать, ставим определённый артикль:
> — мужской род (часто оканчивается на `-o`) → **EL**
> — женский род (часто оканчивается на `-a`) → **LA**
>
> Но есть исключения: `el día`, `el problema`, `la mano`, `la foto` —
> их надо запоминать.

Кнопки: **EL · LA**. Шаблон: голое слово (template = null).

#### Tier 1 — базовые «правильные» (уровни 1–5), 25 слов

| Слово | Артикль | Категория | imageRef |
|---|---|---|---|
| libro | el | дом | `libro.webp` |
| mesa | la | дом | `mesa.webp` |
| silla | la | дом | `silla.webp` |
| cama | la | дом | `cama.webp` |
| puerta | la | дом | `puerta.webp` |
| ventana | la | дом | `ventana.webp` |
| casa | la | дом | `casa.webp` |
| coche | el | транспорт | `coche.webp` |
| tren | el | транспорт | `tren.webp` |
| avión | el | транспорт | `avion.webp` |
| barco | el | транспорт | `barco.webp` |
| perro | el | животные | `perro.webp` |
| gato | el | животные | `gato.webp` |
| caballo | el | животные | `caballo.webp` |
| pájaro | el | животные | `pajaro.webp` |
| manzana | la | еда | `manzana.webp` |
| naranja | la | еда | `naranja.webp` |
| plátano | el | еда | `platano.webp` |
| pan | el | еда | `pan.webp` |
| queso | el | еда | `queso.webp` |
| niño | el | люди | `nino.webp` |
| niña | la | люди | `nina.webp` |
| hombre | el | люди | `hombre.webp` |
| mujer | la | люди | `mujer.webp` |
| amigo | el | люди | `amigo.webp` |

Все эти слова `ruleHint = "Базовое правило: -o → el, -a → la"`.

#### Tier 2 — расширение (уровни 6–10), 25 слов

| Слово | Артикль | Категория | imageRef |
|---|---|---|---|
| árbol | el | природа | `arbol.webp` |
| flor | la | природа | `flor.webp` |
| sol | el | природа | `sol.webp` |
| luna | la | природа | `luna.webp` |
| estrella | la | природа | `estrella.webp` |
| río | el | природа | `rio.webp` |
| montaña | la | природа | `montana.webp` |
| nube | la | природа | `nube.webp` |
| ojo | el | тело | `ojo.webp` |
| boca | la | тело | `boca.webp` |
| pierna | la | тело | `pierna.webp` |
| brazo | el | тело | `brazo.webp` |
| pie | el | тело | `pie.webp` |
| dedo | el | тело | `dedo.webp` |
| oreja | la | тело | `oreja.webp` |
| nariz | la | тело | `nariz.webp` |
| calle | la | город | `calle.webp` |
| plaza | la | город | `plaza.webp` |
| parque | el | город | `parque.webp` |
| escuela | la | школа | `escuela.webp` |
| hospital | el | город | `hospital.webp` |
| tienda | la | город | `tienda.webp` |
| iglesia | la | город | `iglesia.webp` |
| museo | el | город | `museo.webp` |
| reloj | el | дом | `reloj.webp` |

#### Tier 3 — менее очевидные (уровни 11–15), 15 слов

Окончания не на `-o/-a`, женские с `-d/-z/-ción`:

| Слово | Артикль | Подсказка-правило | imageRef |
|---|---|---|---|
| ciudad | la | `-dad` → la | `ciudad.webp` |
| universidad | la | `-dad` → la | `universidad.webp` |
| canción | la | `-ción` → la | `cancion.webp` |
| estación | la | `-ción` → la | `estacion.webp` |
| paz | la | `-z` (часто) → la | `paz.webp` |
| luz | la | `-z` (часто) → la | `luz.webp` |
| flor | la | `-or` иногда → la | `flor2.webp` |
| color | el | `-or` (м.) → el | `color.webp` |
| amor | el | `-or` (м.) → el | `amor.webp` |
| dolor | el | `-or` (м.) → el | `dolor.webp` |
| pez | el | `-z` (м.) → el | `pez.webp` |
| árbol | el | `-l` (м.) → el | `arbol2.webp` |
| sal | la | `-l` (искл. ж.) → la | `sal.webp` |
| miel | la | `-l` (искл. ж.) → la | `miel.webp` |
| reloj | el | `-j` (м.) → el | `reloj2.webp` |

#### Tier 4 — исключения (уровни 16–18), 10 слов

Слова, которые ломают шаблон:

| Слово | Артикль | Почему исключение | imageRef |
|---|---|---|---|
| día | el | `-a`, но греч. происхождения → м. | `dia.webp` |
| problema | el | `-ma` греч. → м. | `problema.webp` |
| tema | el | `-ma` греч. → м. | `tema.webp` |
| sistema | el | `-ma` греч. → м. | `sistema.webp` |
| mapa | el | `-a`, но м. | `mapa.webp` |
| clima | el | `-ma` греч. → м. | `clima.webp` |
| mano | la | `-o`, но ж. | `mano.webp` |
| foto | la | сокр. от `fotografía` → ж. | `foto.webp` |
| moto | la | сокр. от `motocicleta` → ж. | `moto.webp` |
| radio | la | сокр. от `radiodifusión` → ж. | `radio.webp` |

#### Tier 5 — boss (уровни 19–20), 5 слов

Слова с ударным `a-/ha-` в начале — формально женские, но используют `el`
в единственном числе:

| Слово | Артикль (ед.) | Правило | imageRef |
|---|---|---|---|
| agua | el | `el agua` (но `las aguas`) — ударное `a-` | `agua.webp` |
| alma | el | ударное `a-` → `el alma` | `alma.webp` |
| águila | el | ударное `á-` → `el águila` | `aguila.webp` |
| hambre | el | ударное `ha-` → `el hambre` | `hambre.webp` |
| arma | el | ударное `a-` → `el arma` | `arma.webp` |

**Итого мир 1: 80 слов, 80 уникальных картинок.**

---

### 4.2. Мир 2 · Абстрактные понятия (30 слов)

**Правило для tutorial:**
> Абстрактные понятия (чувства, состояния, идеи) тоже имеют род. Помогают
> суффиксы:
> — `-ción, -sión, -dad, -tad, -tud, -ez` → **LA**
> — `-ma` (греч.), `-or` (часто) → **EL**

Кнопки: **EL · LA**. Шаблон: голое слово.

#### Tier 1–2 (уровни 21–28), 12 слов

| Слово | Артикль | Правило | imageRef |
|---|---|---|---|
| amor | el | `-or` → el | `amor_abs.webp` |
| paz | la | `-z` → la | `paz.webp` |
| libertad | la | `-tad` → la | `libertad.webp` |
| felicidad | la | `-dad` → la | `felicidad.webp` |
| tristeza | la | `-eza` → la | `tristeza.webp` |
| miedo | el | `-o` → el | `miedo.webp` |
| esperanza | la | `-a` → la | `esperanza.webp` |
| suerte | la | `-e` (ж.) → la | `suerte.webp` |
| vida | la | `-a` → la | `vida.webp` |
| muerte | la | `-e` (ж.) → la | `muerte.webp` |
| tiempo | el | `-o` → el | `tiempo.webp` |
| paciencia | la | `-cia` → la | `paciencia.webp` |

#### Tier 3 (уровни 29–33), 10 слов

| Слово | Артикль | Правило | imageRef |
|---|---|---|---|
| alegría | la | `-ía` → la | `alegria.webp` |
| ira | la | `-a` → la | `ira.webp` |
| sueño | el | `-o` → el | `sueno.webp` |
| realidad | la | `-dad` → la | `realidad.webp` |
| verdad | la | `-dad` → la | `verdad.webp` |
| mentira | la | `-a` → la | `mentira.webp` |
| justicia | la | `-cia` → la | `justicia.webp` |
| belleza | la | `-eza` → la | `belleza.webp` |
| fuerza | la | `-a` → la | `fuerza.webp` |
| valor | el | `-or` → el | `valor.webp` |

#### Tier 4 (уровни 34–37), 5 слов

| Слово | Артикль | Правило | imageRef |
|---|---|---|---|
| sabiduría | la | `-ía` → la | `sabiduria.webp` |
| ignorancia | la | `-cia` → la | `ignorancia.webp` |
| cultura | la | `-a` → la | `cultura.webp` |
| religión | la | `-ión` → la | `religion.webp` |
| política | la | `-a` → la | `politica.webp` |

#### Tier 5 — boss (уровни 38–40), 3 слова

Абстракции-исключения (`-ma` греч.):

| Слово | Артикль | Правило | imageRef |
|---|---|---|---|
| problema | el | `-ma` греч. → el | `problema.webp` (повтор из мира 1) |
| dilema | el | `-ma` греч. → el | `dilema.webp` |
| trauma | el | `-ma` греч. → el | `trauma.webp` |

**Итого мир 2: 30 слов, ~28 новых картинок (`problema` повторно).**

---

### 4.3. Мир 3 · Дни и время (40 фраз / 20 картинок)

**Правило для tutorial:**
> С днями недели и часами артикль обязателен:
> — `el lunes` = «в понедельник» (один раз)
> — `los lunes` = «по понедельникам» (регулярно)
> — Часы всегда **LA / LAS**: `Es la una`, `Son las dos`

Кнопки: **EL · LA · LOS · LAS**.

#### Tier 1 — дни в ед.ч. (уровни 41–45), 7 фраз

Все ответы — **EL**. Картинка: иконка дня недели.

| template | answer | imageRef | ruleHint |
|---|---|---|---|
| `Vengo ___ lunes` | el | `dia_lunes.webp` | Один конкретный день → el |
| `Te veo ___ martes` | el | `dia_martes.webp` | Один день → el |
| `Llego ___ miércoles` | el | `dia_miercoles.webp` | Один день → el |
| `Salgo ___ jueves` | el | `dia_jueves.webp` | Один день → el |
| `Vamos ___ viernes` | el | `dia_viernes.webp` | Один день → el |
| `Trabajo ___ sábado` | el | `dia_sabado.webp` | Один день → el |
| `Descanso ___ domingo` | el | `dia_domingo.webp` | Один день → el |

#### Tier 2 — дни мн.ч. (уровни 46–50), 7 фраз

Все ответы — **LOS**. Картинки переиспользуются.

| template | answer |
|---|---|
| `Estudio ___ lunes` (по понедельникам) | los |
| `Voy al gimnasio ___ martes` | los |
| `Tengo clase ___ miércoles` | los |
| `Salgo ___ jueves con amigos` | los |
| `Trabajo ___ viernes hasta tarde` | los |
| `Veo a mi familia ___ sábados` | los |
| `Descanso ___ domingos` | los |

#### Tier 3 — часы (уровни 51–55), 12 фраз

Час 1 → **LA**, остальные → **LAS**. Картинка — циферблат с нужным
временем.

| template | answer | imageRef |
|---|---|---|
| `Es ___ una de la tarde` | la | `hora_01.webp` |
| `Son ___ dos en punto` | las | `hora_02.webp` |
| `Son ___ tres y media` | las | `hora_03.webp` |
| `Son ___ cuatro menos cuarto` | las | `hora_04.webp` |
| `Son ___ cinco de la mañana` | las | `hora_05.webp` |
| `Son ___ seis y cuarto` | las | `hora_06.webp` |
| `Son ___ siete y diez` | las | `hora_07.webp` |
| `Son ___ ocho de la noche` | las | `hora_08.webp` |
| `Son ___ nueve y media` | las | `hora_09.webp` |
| `Son ___ diez en punto` | las | `hora_10.webp` |
| `Son ___ once y veinte` | las | `hora_11.webp` |
| `Son ___ doce del mediodía` | las | `hora_12.webp` |

#### Tier 4 — части дня и периоды (уровни 56–58), 8 фраз

| template | answer | imageRef | примечание |
|---|---|---|---|
| `Estudio por ___ mañana` | la | `manana.webp` | `por la mañana` |
| `Trabajo por ___ tarde` | la | `tarde.webp` | `por la tarde` |
| `Duermo por ___ noche` | la | `noche.webp` | `por la noche` |
| `Comemos a ___ una` | la | `hora_01.webp` | время с предлогом `a` |
| `Cenamos a ___ ocho` | las | `hora_08.webp` | время с `a` |
| `___ semana pasada fue dura` | la | `semana.webp` | `la semana` |
| `___ mes que viene viajamos` | el | `mes.webp` | `el mes` |
| `___ año pasado aprendí mucho` | el | `ano.webp` | `el año` |

#### Tier 5 — boss (уровни 59–60), 6 смешанных фраз

Без подсказки, мешаем все варианты:

| template | answer |
|---|---|
| `Nos vemos ___ lunes a ___ tres` | el / las (двойной выбор? — на boss-уровне один пропуск, второй задан) |

> **Уточнить с пользователем:** на boss-уровне делаем два пропуска в одной
> фразе или 6 отдельных раундов с разным контекстом?

**Итого мир 3: ~40 фраз, ~20 уникальных картинок.**

---

### 4.4. Мир 4 · Титулы и звания (30 фраз / 15 картинок)

**Правило для tutorial:**
> Когда говорим о человеке с титулом **в третьем лице** — артикль нужен:
>   `El doctor Pérez no vino hoy.`
> Когда **обращаемся напрямую** — артикль НЕ ставим:
>   `Buenos días, doctor Pérez.` — Ø
> Перед именами собственными без титула артикль не ставится: `Pérez vino`.

Кнопки: **EL · LA · Ø**.

#### Tier 1 — 3-е лицо (уровни 61–65), 10 фраз

Все требуют артикль (el/la в зависимости от рода).

| template | answer | imageRef |
|---|---|---|
| `___ doctor Pérez no vino` | el | `doctor.webp` |
| `___ doctora García me atendió` | la | `doctora.webp` |
| `___ profesor López explica bien` | el | `profesor.webp` |
| `___ profesora Ruiz es nueva` | la | `profesora.webp` |
| `___ señor Martínez llamó` | el | `senor.webp` |
| `___ señora Gómez espera` | la | `senora.webp` |
| `___ presidente habló ayer` | el | `presidente.webp` |
| `___ ingeniero Torres lo diseñó` | el | `ingeniero.webp` |
| `___ abogada Vega ganó el caso` | la | `abogada.webp` |
| `___ rey Felipe visitó la ciudad` | el | `rey.webp` |

#### Tier 2 — обращение (уровни 66–70), 10 фраз

Все требуют **Ø**.

| template | answer | imageRef |
|---|---|---|
| `Buenos días, ___ doctor` | Ø | `doctor.webp` |
| `Hola, ___ profesora` | Ø | `profesora.webp` |
| `Disculpe, ___ señor` | Ø | `senor.webp` |
| `Sí, ___ señora` | Ø | `senora.webp` |
| `Por favor, ___ doctor Pérez` | Ø | `doctor.webp` |
| `Mire, ___ profesor López` | Ø | `profesor.webp` |
| `Gracias, ___ doctora` | Ø | `doctora.webp` |
| `Perdón, ___ ingeniero` | Ø | `ingeniero.webp` |
| `Hola, ___ capitán` | Ø | `capitan.webp` |
| `Adiós, ___ maestra` | Ø | `maestra.webp` |

#### Tier 3 — особые случаи (уровни 71–75), 6 фраз

«Don/Doña» и «Papa/Rey» в особых конструкциях.

| template | answer | imageRef | правило |
|---|---|---|---|
| `___ don Juan llegó` | Ø | `don.webp` | перед `don/doña` нет артикля |
| `___ doña Ana cocina bien` | Ø | `dona.webp` | перед `doña` нет артикля |
| `___ papa visitó España` | el | `papa.webp` | титул без обращения |
| `Hablé con ___ rey` | el | `rey.webp` | артикль с титулом |
| `Hola, ___ capitán` | Ø | `capitan.webp` | обращение |
| `___ general Martínez no firmó` | el | `general.webp` | 3-е лицо |

#### Tier 4 — смешанное (уровни 76–78), 4 фразы

Один и тот же титул в разных контекстах (одно слово — разные ответы):

| template | answer |
|---|---|
| `___ doctor Pérez es bueno` (3 лицо) | el |
| `Buenos días, ___ doctor Pérez` (обращение) | Ø |
| `___ profesora Ruiz vino` (3 лицо) | la |
| `Por favor, ___ profesora` (обращение) | Ø |

#### Tier 5 — boss (уровни 79–80)

15 раундов смешанные tier 1–4, без подсказок.

**Итого мир 4: ~30 фраз, ~15 уникальных картинок.**

---

### 4.5. Мир 5 · Вещества и категории (50 фраз / 30 картинок)

**Правило для tutorial:**
> Когда говорим о веществе или категории **как о виде вообще** — нужен
> артикль:
>   `El café es bueno por la mañana.` (кофе как напиток вообще)
> Когда речь о **неопределённом количестве** — артикля нет:
>   `Tomo café cada día.` (какое-то кофе)
> Когда речь о **конкретном** — нужен артикль:
>   `El café que tomé estaba frío.`

Кнопки: **EL · LA · Ø**.

#### Tier 1 — обобщение (артикль) (уровни 81–84), 10 фраз

| template | answer | imageRef |
|---|---|---|
| `___ café es bueno` | el | `cafe.webp` |
| `___ agua es vida` | el | `agua.webp` (повтор) |
| `___ oro es caro` | el | `oro.webp` |
| `___ pan es básico` | el | `pan.webp` (повтор) |
| `___ leche tiene calcio` | la | `leche.webp` |
| `___ vino es saludable con moderación` | el | `vino.webp` |
| `___ amor cura todo` | el | `amor_abs.webp` (повтор) |
| `___ tiempo es oro` | el | `tiempo.webp` (повтор) |
| `___ música es universal` | la | `musica.webp` |
| `___ sal sube la presión` | la | `sal.webp` (повтор) |

#### Tier 2 — неопределённое количество (Ø) (уровни 85–88), 10 фраз

| template | answer | imageRef |
|---|---|---|
| `Tomo ___ café cada día` | Ø | `cafe.webp` |
| `Bebo ___ agua` | Ø | `agua.webp` |
| `Compré ___ oro` | Ø | `oro.webp` |
| `Quiero ___ pan` | Ø | `pan.webp` |
| `Tengo ___ leche en la nevera` | Ø | `leche.webp` |
| `Bebimos ___ vino en la cena` | Ø | `vino.webp` |
| `Necesito ___ sal` | Ø | `sal.webp` |
| `Pongo ___ azúcar al té` | Ø | `azucar.webp` |
| `Hay ___ arroz en la cocina` | Ø | `arroz.webp` |
| `Compro ___ leche` | Ø | `leche.webp` |

#### Tier 3 — конкретное (артикль) (уровни 89–92), 10 фраз

| template | answer | imageRef |
|---|---|---|
| `___ café que tomé estaba frío` | el | `cafe.webp` |
| `___ agua del grifo está sucia` | el | `agua.webp` |
| `Compré ___ vino que recomendaste` | el | `vino.webp` |
| `___ pan de hoy está duro` | el | `pan.webp` |
| `___ leche que compré se cortó` | la | `leche.webp` |
| `___ oro de mi abuela vale mucho` | el | `oro.webp` |
| `___ sal de aquí es marina` | la | `sal.webp` |
| `___ azúcar moreno es mejor` | el | `azucar.webp` |
| `___ arroz de mamá es delicioso` | el | `arroz.webp` |
| `___ aceite de oliva es caro` | el | `aceite.webp` |

#### Tier 4 — материалы и металлы (уровни 93–96), 10 фраз

| template | answer | imageRef |
|---|---|---|
| `___ plata es más barata que el oro` | la | `plata.webp` |
| `___ hierro se oxida` | el | `hierro.webp` |
| `___ madera flota` | la | `madera.webp` |
| `___ plástico contamina` | el | `plastico.webp` |
| `___ cristal es frágil` | el | `cristal.webp` |
| `___ papel viene del árbol` | el | `papel.webp` |
| `___ algodón es suave` | el | `algodon.webp` |
| `___ lana abriga` | la | `lana.webp` |
| `___ cuero es duradero` | el | `cuero.webp` |
| `___ cobre conduce electricidad` | el | `cobre.webp` |

#### Tier 5 — boss (уровни 97–100), 10 смешанных фраз

15 раундов в одном уровне, перемешано tier 1–4 + 4 элемента (`fuego`, `aire`, `tierra`, `agua`):

| template | answer | imageRef |
|---|---|---|
| `___ fuego destruye` | el | `fuego.webp` |
| `Hay ___ fuego en la chimenea` | Ø | `fuego.webp` |
| `___ aire de la montaña es puro` | el | `aire.webp` |
| `Necesito ___ aire fresco` | Ø | `aire.webp` |
| `___ tierra gira alrededor del sol` | la | `tierra.webp` |
| `Compré ___ tierra para las plantas` | Ø | `tierra.webp` |
| `___ gasolina sube de precio` | la | `gasolina.webp` |
| `Pongo ___ gasolina al coche` | Ø | `gasolina.webp` |
| `___ harina se hace de trigo` | la | `harina.webp` |
| `Compro ___ harina` | Ø | `harina.webp` |

**Итого мир 5: ~50 фраз, ~30 новых картинок.**

---

## 5. Сводка по картинкам

| Мир | Уникальных картинок | Повторно используется |
|---|---|---|
| 1 | 80 | — |
| 2 | 28 | `amor`, `problema` повтор из м.1 |
| 3 | 20 | — |
| 4 | 15 | — |
| 5 | 30 | `cafe`, `agua`, `pan`, `vino`, `leche`, `sal` |

**Итого: ~173 уникальных файла.**
Плюс 5 обложек миров (`world_1.webp` … `world_5.webp`) и 3 UI-иконки —
**итого ~181 файл, ~2 МБ к APK.**

---

## 6. Генерация картинок

### Стиль (референс — flat illustration в духе Duolingo / Babbel)

Промпт-шаблон:
```
flat vector illustration of {WORD_EN}, soft pastel background,
minimal style, centered composition, no text, no letters,
simple shapes, rounded corners, child-friendly, clean
```

Где `{WORD_EN}` — английский перевод слова (модели лучше понимают
английский).

### Параметры генерации

- Модель: DALL-E 3 (через OpenAI API) **или** Stable Diffusion XL локально
- Размер: 1024×1024, потом ресайз в 256×256 WebP (q=80)
- Один прогон: ~$20 на DALL-E или 0$ на SD локально (~3 часа)
- Отбраковка: ~10% картинок придётся регенерировать

### Структура ассетов

```
app/src/main/assets/article_images/
├── world_1/
│   ├── libro.webp
│   ├── mesa.webp
│   └── ...
├── world_2/
├── world_3/
├── world_4/
├── world_5/
└── covers/
    ├── world_1.webp
    └── ...
```

Загрузка через **Coil** (`coil-compose`):
```kotlin
AsyncImage(
    model = "file:///android_asset/article_images/world_1/casa.webp",
    contentDescription = word.word,
    modifier = Modifier.size(180.dp)
)
```

---

## 7. Реализация — порядок работы

| Шаг | Что | Когда |
|---|---|---|
| 1 | Дизайн-док (этот файл) | ✅ сейчас |
| 2 | Ревью пользователем + правки | следующий шаг |
| 3 | Генерация ~180 картинок | после ревью |
| 4 | Миграция БД (`MIGRATION_9_10`), обновление `ArticleWordEntity` | код |
| 5 | Сидер контента из этого дизайн-дока (CSV/JSON в `assets/`) | код |
| 6 | UI: динамические кнопки EL/LA/LOS/LAS/Ø, картинка в карточке | код |
| 7 | Tutorial-карточки на старте каждого мира | код |
| 8 | Карта миров (5 шт) на главном экране игры | код |
| 9 | Тесты (ArticleSeederTest, AnswerLogicTest) | код |
| 10 | Полное прохождение пилота на телефоне | проверка |

---

## 8. Открытые вопросы (нужно подтверждение пользователя)

1. **Boss-уровни мира 3** — двойной пропуск в одной фразе или 6 отдельных раундов?
2. **Стиль картинок** — flat vector (как описал) подтверждаем, или другой
   референс? (вариант: 3D-clay как в Memrise, photorealistic)
3. **Локальная SD vs DALL-E** — у тебя есть OpenAI API-ключ, или
   генерируем локально / через бесплатный сервис?
4. **Когда стартуем код** — после полной генерации картинок или можно
   начать миграцию БД параллельно с пустыми `imageRef`?
5. **Совместимость** — старая `article_level_progress` (CEFR-привязка)
   используется в `HomeScreen`? Если да — оставляем legacy-сидер; если
   нет — можно удалить полностью.
