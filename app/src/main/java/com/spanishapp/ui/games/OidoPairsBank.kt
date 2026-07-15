package com.spanishapp.ui.games

/**
 * El Oído — минимальные пары. Слова, которые русское ухо путает.
 * Канон: кастильское произношение (Испания) — пары s/z рассчитаны на
 * различение [s] и [θ] (distinción), яркую черту мадридской речи.
 *
 * ВАЖНО: сюда попадают только пары, которые в Испании звучат РАЗНЫМИ.
 * b/v не различаются на слух — таких пар здесь нет. ll/y произносятся
 * одинаково (yeísmo) — пары l/ll различают [l] и [ʝ], а не ll и y.
 */
enum class OidoPairCategory {
    /** Одиночная r против раскатистой rr. */
    R_RR,
    /** Кастильская дистинкция: s [s] против c/z [θ]. */
    S_Z,
    /** Гласные, которые русские «съедают»: e/i, o/a, o/u. */
    VOWELS,
    /** Ударение меняет смысл: hablo / habló. */
    STRESS,
    /** n против ñ. */
    N_ENYE,
    /** l против ll [ʝ]. */
    L_LL,
    /** Межзубное d против r: todo / toro. */
    D_R,
}

data class OidoPair(
    val a: String,
    val ruA: String,
    val b: String,
    val ruB: String,
    val category: OidoPairCategory,
    /** Подсказка после ответа: на что было слушать. */
    val note: String,
)

internal object OidoPairsBank {

    private fun p(a: String, ruA: String, b: String, ruB: String, c: OidoPairCategory, note: String) =
        OidoPair(a, ruA, b, ruB, c, note)

    val pairs: List<OidoPair> = listOf(
        // ── R / RR ──────────────────────────────────────────
        p("pero", "но", "perro", "собака", OidoPairCategory.R_RR,
            "Раскатистая rr дрожит дольше: pe-rrro"),
        p("caro", "дорогой", "carro", "повозка", OidoPairCategory.R_RR,
            "Caro — один удар языка, carro — вибрация"),
        p("pera", "груша", "perra", "собака (она)", OidoPairCategory.R_RR,
            "Perra — долгая раскатистая rr"),
        p("coro", "хор", "corro", "я бегу", OidoPairCategory.R_RR,
            "Corro — рычащая rr: co-rrro"),
        p("cero", "ноль", "cerro", "холм", OidoPairCategory.R_RR,
            "Cerro — двойная rr вибрирует"),
        p("moro", "мавр", "morro", "морда", OidoPairCategory.R_RR,
            "Morro — раскатистое rr"),
        p("foro", "форум", "forro", "подкладка", OidoPairCategory.R_RR,
            "Forro — длинная rr"),
        p("ahora", "сейчас", "ahorra", "он экономит", OidoPairCategory.R_RR,
            "Ahorra — вибрирующая rr в середине"),
        p("quería", "я хотел", "querría", "я хотел бы", OidoPairCategory.R_RR,
            "Querría (condicional) — раскатистая rr!"),
        p("enterado", "в курсе", "enterrado", "закопанный", OidoPairCategory.R_RR,
            "Enterrado — похоронен: рычащая rr"),
        p("vara", "прут", "barra", "стойка, батон", OidoPairCategory.R_RR,
            "Barra — раскатистая rr (b и v звучат одинаково!)"),
        p("mira", "смотри", "mirra", "мирра", OidoPairCategory.R_RR,
            "Mirra — двойная rr"),

        // ── S / Z (кастильская [θ]) ─────────────────────────
        p("casa", "дом", "caza", "охота", OidoPairCategory.S_Z,
            "Caza — межзубный [θ], язык между зубами"),
        p("coser", "шить", "cocer", "варить", OidoPairCategory.S_Z,
            "Cocer — шепелявый [θ]: co-θer"),
        p("sien", "висок", "cien", "сто", OidoPairCategory.S_Z,
            "Cien — [θien], межзубный звук"),
        p("tasa", "ставка", "taza", "чашка", OidoPairCategory.S_Z,
            "Taza — [taθa]: язык между зубами"),
        p("poso", "осадок", "pozo", "колодец", OidoPairCategory.S_Z,
            "Pozo — [poθo]"),
        p("abrasar", "обжигать", "abrazar", "обнимать", OidoPairCategory.S_Z,
            "Abrazar (обнимать) — с межзубным [θ]"),
        p("masa", "тесто", "maza", "булава", OidoPairCategory.S_Z,
            "Maza — [maθa]"),
        p("sima", "пропасть", "cima", "вершина", OidoPairCategory.S_Z,
            "Cima — [θima]: вершина через [θ]"),
        p("siervo", "раб", "ciervo", "олень", OidoPairCategory.S_Z,
            "Ciervo (олень) — [θiervo]"),
        p("sumo", "высший", "zumo", "сок", OidoPairCategory.S_Z,
            "Zumo (сок в Испании!) — [θumo]"),
        p("seta", "гриб", "zeta", "буква Z", OidoPairCategory.S_Z,
            "Zeta — [θeta]"),
        p("rosa", "роза", "roza", "он задевает", OidoPairCategory.S_Z,
            "Roza — [roθa]"),

        // ── Гласные ─────────────────────────────────────────
        p("peso", "вес", "piso", "квартира", OidoPairCategory.VOWELS,
            "Испанские e и i чёткие: pEso — pIso"),
        p("mesa", "стол", "misa", "месса", OidoPairCategory.VOWELS,
            "mEsa — mIsa: не «съедай» гласную"),
        p("puerto", "порт", "puerta", "дверь", OidoPairCategory.VOWELS,
            "Финальная -o/-a меняет слово целиком"),
        p("bolso", "сумка", "bolsa", "пакет", OidoPairCategory.VOWELS,
            "bolsO — сумка, bolsA — пакет"),
        p("suelo", "пол", "suela", "подошва", OidoPairCategory.VOWELS,
            "suelO/suelA — слушай последнюю гласную"),
        p("libro", "книга", "libra", "фунт", OidoPairCategory.VOWELS,
            "librO — книга, librA — фунт"),
        p("cuadro", "картина", "cuadra", "конюшня", OidoPairCategory.VOWELS,
            "Финальная гласная: cuadrO/cuadrA"),
        p("pesa", "гиря", "pisa", "он наступает", OidoPairCategory.VOWELS,
            "pEsa — pIsa: испанские e и i не смешиваются"),

        // ── Ударение ────────────────────────────────────────
        p("hablo", "я говорю", "habló", "он сказал", OidoPairCategory.STRESS,
            "hAblo — я сейчас; hablÓ — он в прошлом"),
        p("compro", "я покупаю", "compró", "он купил", OidoPairCategory.STRESS,
            "Ударение на последний слог = прошлое время"),
        p("canto", "я пою", "cantó", "он спел", OidoPairCategory.STRESS,
            "cantÓ — ударение в конце: прошлое"),
        p("llego", "я прихожу", "llegó", "он пришёл", OidoPairCategory.STRESS,
            "llegÓ — прошлое время"),
        p("termino", "я заканчиваю", "terminó", "он закончил", OidoPairCategory.STRESS,
            "terminÓ — финал слова под ударением"),
        p("pago", "я плачу", "pagó", "он заплатил", OidoPairCategory.STRESS,
            "pagÓ — ударение в конце: прошлое время"),
        p("esta", "эта", "está", "находится", OidoPairCategory.STRESS,
            "estÁ — глагол estar, ударение в конце"),
        p("papa", "картофель", "papá", "папа", OidoPairCategory.STRESS,
            "papÁ — папа; pApa — картошка"),
        p("mama", "грудь", "mamá", "мама", OidoPairCategory.STRESS,
            "mamÁ — мама, ударение на последний слог"),
        p("bebe", "он пьёт", "bebé", "младенец", OidoPairCategory.STRESS,
            "bebÉ — малыш; bEbe — пьёт"),
        p("ingles", "пах (мн.)", "inglés", "английский", OidoPairCategory.STRESS,
            "inglÉs — язык; Ingles — совсем другое!"),
        p("camino", "дорога", "caminó", "он шёл", OidoPairCategory.STRESS,
            "caminÓ — прошлое время глагола"),

        // ── N / Ñ ───────────────────────────────────────────
        p("pena", "печаль", "peña", "скала; компания", OidoPairCategory.N_ENYE,
            "ñ звучит как мягкое «нь»: pe-нья"),
        p("sonar", "звучать", "soñar", "мечтать", OidoPairCategory.N_ENYE,
            "soÑar — мечтать, с мягким нь"),
        p("campana", "колокол", "campaña", "кампания", OidoPairCategory.N_ENYE,
            "campaÑa — мягкое нь"),
        p("una", "одна", "uña", "ноготь", OidoPairCategory.N_ENYE,
            "uÑa — ноготь: у-нья"),
        p("cana", "седой волос", "caña", "тростник; бокал пива", OidoPairCategory.N_ENYE,
            "caÑa — то, что заказывают в баре Мадрида"),
        p("mono", "обезьяна", "moño", "пучок волос", OidoPairCategory.N_ENYE,
            "moÑo — мягкое нь"),

        // ── L / LL ──────────────────────────────────────────
        p("polo", "поло; полюс", "pollo", "курица", OidoPairCategory.L_LL,
            "pollo — [поё]: ll звучит как «й»"),
        p("mala", "плохая", "malla", "сетка", OidoPairCategory.L_LL,
            "malla — [мая]"),
        p("pila", "батарейка", "pilla", "он ловит", OidoPairCategory.L_LL,
            "pilla — [пия]"),
        p("loro", "попугай", "lloro", "я плачу", OidoPairCategory.L_LL,
            "lloro — [ёро]: плачу"),
        p("lave", "чтобы я мыл", "llave", "ключ", OidoPairCategory.L_LL,
            "llave — [яве]: ключ"),
        p("ola", "волна", "olla", "кастрюля", OidoPairCategory.L_LL,
            "olla — [оя]: h в ola не звучит вовсе"),
        p("tala", "вырубка", "talla", "размер", OidoPairCategory.L_LL,
            "talla — [тая]: размер одежды"),

        // ── D / R между гласными ────────────────────────────
        p("todo", "всё", "toro", "бык", OidoPairCategory.D_R,
            "d между гласными — мягкая, почти [ð]; r — удар языка"),
        p("cada", "каждый", "cara", "лицо", OidoPairCategory.D_R,
            "caDa — мягкое d; caRa — короткое r"),
        p("modo", "способ", "moro", "мавр", OidoPairCategory.D_R,
            "moDo/moRo: испанская d нежнее русской"),
        p("mudo", "немой", "muro", "стена", OidoPairCategory.D_R,
            "muDo — немой, muRo — стена"),
        p("seda", "шёлк", "sera", "корзина", OidoPairCategory.D_R,
            "seDa — шёлк: d почти [ð], мягче русской"),
    )

    /** Категории для раннего яруса (26–50) — самые контрастные. */
    val easyCategories = setOf(
        OidoPairCategory.R_RR,
        OidoPairCategory.N_ENYE,
        OidoPairCategory.L_LL,
        OidoPairCategory.VOWELS,
    )
}
