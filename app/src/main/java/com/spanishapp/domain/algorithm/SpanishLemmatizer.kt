package com.spanishapp.domain.algorithm

/**
 * Generates candidate lemmas (base forms) for an inflected Spanish word.
 * Returns candidates ordered by likelihood so callers can try them in sequence.
 */
object SpanishLemmatizer {

    // Manually-curated map of irregular forms → infinitive
    private val irregularMap: Map<String, String> by lazy { buildIrregularMap() }

    fun candidates(raw: String): List<String> {
        val w = raw.lowercase().trim()
        val wN = stripAccents(w)            // accent-stripped version

        val result = LinkedHashSet<String>()
        result.add(w)
        result.add(wN)

        // 1. Irregular verb forms (accent-aware and accent-stripped lookups)
        irregularMap[w]?.let { result.add(it) }
        irregularMap[wN]?.let { result.add(it) }

        // ser/ir share preterite — add both infinitives
        if (wN in listOf("fui","fuiste","fue","fuimos","fuisteis","fueron")) {
            result.add("ir"); result.add("ser")
        }

        // 2. Regular verb suffix stripping
        result.addAll(verbCandidates(wN))

        // 3. Noun / adjective inflection
        result.addAll(nounAdjCandidates(wN))

        return result.toList()
    }

    // ─────────────────────────────────────────────────────────────

    private fun stripAccents(s: String): String =
        s.replace('á','a').replace('é','e').replace('í','i')
         .replace('ó','o').replace('ú','u').replace('ü','u')
         .replace('ñ','n')

    private fun verbCandidates(w: String): List<String> {
        val c = mutableListOf<String>()
        fun ar(stem: String) { if (stem.length >= 2) c.add("${stem}ar") }
        fun er(stem: String) { if (stem.length >= 2) c.add("${stem}er") }
        fun ir(stem: String) { if (stem.length >= 2) c.add("${stem}ir") }

        // Gerunds
        if (w.endsWith("ando"))  { ar(w.dropLast(4)) }
        if (w.endsWith("iendo")) { er(w.dropLast(5)); ir(w.dropLast(5)) }
        if (w.endsWith("yendo")) { er(w.dropLast(5)); ir(w.dropLast(5)) }

        // Past participles
        if (w.endsWith("ado"))   { ar(w.dropLast(3)) }
        if (w.endsWith("ido"))   { er(w.dropLast(3)); ir(w.dropLast(3)) }

        // Preterite -ar
        if (w.endsWith("e") && w.length > 3)  ar(w.dropLast(1))   // hable
        if (w.endsWith("aste"))  ar(w.dropLast(4))
        if (w.endsWith("o") && w.length > 3)  { ar(w.dropLast(1)); er(w.dropLast(1)); ir(w.dropLast(1)) }
        if (w.endsWith("amos"))  { ar(w.dropLast(4)); ir(w.dropLast(4)) }
        if (w.endsWith("asteis")) ar(w.dropLast(6))
        if (w.endsWith("aron"))  ar(w.dropLast(4))

        // Imperfect -ar
        if (w.endsWith("aba"))   ar(w.dropLast(3))
        if (w.endsWith("abas"))  ar(w.dropLast(4))
        if (w.endsWith("abamos")) ar(w.dropLast(6))
        if (w.endsWith("aban"))  ar(w.dropLast(4))

        // Present -ar
        if (w.endsWith("as") && !w.endsWith("ias")) ar(w.dropLast(2))
        if (w.endsWith("an") && !w.endsWith("ian")) { ar(w.dropLast(2)); er(w.dropLast(2)); ir(w.dropLast(2)) }
        if (w.endsWith("ais"))   ar(w.dropLast(3))

        // Preterite / imperfect -er/-ir
        if (w.endsWith("iste"))   { er(w.dropLast(4)); ir(w.dropLast(4)) }
        if (w.endsWith("io"))     { er(w.dropLast(2)); ir(w.dropLast(2)) }
        if (w.endsWith("imos"))   { ir(w.dropLast(4)); er(w.dropLast(4)) }
        if (w.endsWith("isteis")) { er(w.dropLast(6)); ir(w.dropLast(6)) }
        if (w.endsWith("ieron")) { er(w.dropLast(5)); ir(w.dropLast(5)) }

        // Imperfect -er/-ir
        if (w.endsWith("ia"))    { er(w.dropLast(2)); ir(w.dropLast(2)) }
        if (w.endsWith("ias"))   { er(w.dropLast(3)); ir(w.dropLast(3)) }
        if (w.endsWith("iamos")) { er(w.dropLast(5)); ir(w.dropLast(5)) }
        if (w.endsWith("ian"))   { er(w.dropLast(3)); ir(w.dropLast(3)) }

        // Present -er/-ir
        if (w.endsWith("emos"))  er(w.dropLast(4))
        if (w.endsWith("eis"))   er(w.dropLast(3))
        if (w.endsWith("en"))    { er(w.dropLast(2)); ir(w.dropLast(2)) }
        if (w.endsWith("es") && w.length > 3 && !w.endsWith("ares")) {
            er(w.dropLast(2)); ir(w.dropLast(2))
        }

        // Subjunctive -ar (stem + e endings)
        if (w.endsWith("en") && w.length > 3)  ar(w.dropLast(2))

        return c
    }

    private fun nounAdjCandidates(w: String): List<String> {
        val c = mutableListOf<String>()
        // Plural → singular
        if (w.endsWith("os") && w.length > 3)  { c.add(w.dropLast(1)); c.add(w.dropLast(2) + "a") }
        if (w.endsWith("as") && w.length > 3)  { c.add(w.dropLast(1)); c.add(w.dropLast(2) + "o") }
        if (w.endsWith("es") && w.length > 3)  c.add(w.dropLast(2))   // ciudades→ciudad
        // Feminine → masculine
        if (w.endsWith("a") && w.length > 3)   c.add(w.dropLast(1) + "o")
        return c
    }

    // ─── Irregular verb forms table ───────────────────────────────

    private fun buildIrregularMap(): Map<String, String> {
        val m = HashMap<String, String>()

        fun add(inf: String, vararg forms: String) {
            for (f in forms) m[f] = inf
        }

        add("ser",
            "soy","eres","es","somos","sois","son",
            "era","eras","éramos","erais","eran",
            "seré","serás","será","seremos","seréis","serán",
            "sería","serías","seríamos","seríais","serían",
            "sea","seas","seamos","seáis","sean",
            // without accents
            "eramos","seais","sere","seras","sera","seremos","sereis","seran",
            "seria","serias","seriamos","seriais","serian"
        )
        add("estar",
            "estoy","estás","está","estamos","estáis","están",
            "estuve","estuviste","estuvo","estuvimos","estuvisteis","estuvieron",
            "estaba","estabas","estábamos","estabais","estaban",
            "esté","estés","estemos","estéis","estén",
            // without accents
            "estas","esta","estais","estan","estabamos","este","estes","esteis","esten"
        )
        add("tener",
            "tengo","tienes","tiene","tenemos","tenéis","tienen",
            "tuve","tuviste","tuvo","tuvimos","tuvisteis","tuvieron",
            "tenía","tenías","teníamos","teníais","tenían",
            "tendré","tendrás","tendrá","tendremos","tendréis","tendrán",
            "tendría","tendrías","tendríamos","tendríais","tendrían",
            "tenga","tengas","tengamos","tengáis","tengan",
            // without accents
            "teneis","tenia","tenias","teniamos","teniais","tenian",
            "tendre","tendras","tendra","tendremos","tendreis","tendran",
            "tendria","tendrias","tendriamos","tendriais","tendrian",
            "tengais"
        )
        add("haber",
            "he","has","ha","hemos","habéis","han",
            "hube","hubiste","hubo","hubimos","hubisteis","hubieron",
            "había","habías","habíamos","habíais","habían",
            "habré","habrás","habrá","habremos","habréis","habrán",
            "habría","habrías","habríamos","habríais","habrían",
            "haya","hayas","hayamos","hayáis","hayan",
            // without accents
            "habeis","habia","habias","habiamos","habiais","habian",
            "habre","habras","habra","habremos","habreis","habran",
            "habria","habrias","habriamos","habriais","habrian",
            "hayais"
        )
        add("ir",
            "voy","vas","va","vamos","vais","van",
            "iba","ibas","íbamos","ibais","iban",
            "iré","irás","irá","iremos","iréis","irán",
            "iría","irías","iríamos","iríais","irían",
            "vaya","vayas","vayamos","vayáis","vayan",
            // without accents
            "ibamos","ire","iras","ira","iremos","ireis","iran",
            "iria","irias","iriamos","iriais","irian",
            "vayais"
        )
        add("hacer",
            "hago","haces","hace","hacemos","hacéis","hacen",
            "hice","hiciste","hizo","hicimos","hicisteis","hicieron",
            "hacía","hacías","hacíamos","hacíais","hacían",
            "haré","harás","hará","haremos","haréis","harán",
            "haría","harías","haríamos","haríais","harían",
            "haga","hagas","hagamos","hagáis","hagan",
            // without accents
            "haceis","hacia","hacias","haciamos","haciais","hacian",
            "hare","haras","hara","haremos","hareis","haran",
            "haria","harias","hariamos","hariais","harian",
            "hagais"
        )
        add("decir",
            "digo","dices","dice","decimos","decís","dicen",
            "dije","dijiste","dijo","dijimos","dijisteis","dijeron",
            "decía","decías","decíamos","decíais","decían",
            "diré","dirás","dirá","diremos","diréis","dirán",
            "diría","dirías","diríamos","diríais","dirían",
            "diga","digas","digamos","digáis","digan",
            // without accents
            "decis","decia","decias","deciamos","deciais","decian",
            "dire","diras","dira","diremos","direis","diran",
            "diria","dirias","diriamos","diriais","dirian",
            "digais"
        )
        add("poder",
            "puedo","puedes","puede","podemos","podéis","pueden",
            "pude","pudiste","pudo","pudimos","pudisteis","pudieron",
            "podía","podías","podíamos","podíais","podían",
            "podré","podrás","podrá","podremos","podréis","podrán",
            "podría","podrías","podríamos","podríais","podrían",
            "pueda","puedas","podamos","podáis","puedan",
            // without accents
            "podeis","podia","podias","podiamos","podiais","podian",
            "podre","podras","podra","podremos","podreis","podran",
            "podria","podrias","podriamos","podriais","podrian",
            "podais"
        )
        add("querer",
            "quiero","quieres","quiere","queremos","queréis","quieren",
            "quise","quisiste","quiso","quisimos","quisisteis","quisieron",
            "quería","querías","queríamos","queríais","querían",
            "querré","querrás","querrá","querremos","querréis","querrán",
            "querría","querrías","querríamos","querríais","querrían",
            "quiera","quieras","queramos","queráis","quieran",
            // without accents
            "quereis","queria","querias","queriamos","queriais","querian",
            "querre","querras","querra","querremos","querreis","querran",
            "querria","querrias","querriamos","querriais","querrian",
            "querais"
        )
        add("venir",
            "vengo","vienes","viene","venimos","venís","vienen",
            "vine","viniste","vino","vinimos","vinisteis","vinieron",
            "venía","venías","veníamos","veníais","venían",
            "vendré","vendrás","vendrá","vendremos","vendréis","vendrán",
            "vendría","vendrías","vendríamos","vendríais","vendrían",
            "venga","vengas","vengamos","vengáis","vengan",
            // without accents
            "venis","venia","venias","veniamos","veniais","venian",
            "vendre","vendras","vendra","vendremos","vendreis","vendran",
            "vendria","vendrias","vendriamos","vendriais","vendrian",
            "vengais"
        )
        add("ver",
            "veo","ves","ve","vemos","veis","ven",
            "vi","viste","vio","vimos","visteis","vieron",
            "veía","veías","veíamos","veíais","veían",
            "veré","verás","verá","veremos","veréis","verán",
            "vería","verías","veríamos","veríais","verían",
            "vea","veas","veamos","veáis","vean",
            // without accents
            "veia","veias","veiamos","veiais","veian",
            "vere","veras","vera","veremos","vereis","veran",
            "veria","verias","veriamos","veriais","verian",
            "veais"
        )
        add("saber",
            "sé","sabes","sabe","sabemos","sabéis","saben",
            "supe","supiste","supo","supimos","supisteis","supieron",
            "sabía","sabías","sabíamos","sabíais","sabían",
            "sabré","sabrás","sabrá","sabremos","sabréis","sabrán",
            "sabría","sabrías","sabríamos","sabríais","sabrían",
            "sepa","sepas","sepamos","sepáis","sepan",
            // without accents
            "se","sabeis","sabia","sabias","sabiamos","sabiais","sabian",
            "sabre","sabras","sabra","sabremos","sabreis","sabran",
            "sabria","sabrias","sabriamos","sabriais","sabrian",
            "sepais"
        )
        add("poner",
            "pongo","pones","pone","ponemos","ponéis","ponen",
            "puse","pusiste","puso","pusimos","pusisteis","pusieron",
            "ponía","ponías","poníamos","poníais","ponían",
            "pondré","pondrás","pondrá","pondremos","pondréis","pondrán",
            "pondría","pondrías","pondríamos","pondríais","pondrían",
            "ponga","pongas","pongamos","pongáis","pongan",
            // without accents
            "poneis","ponia","ponias","poniamos","poniais","poniam",
            "pondre","pondras","pondra","pondremos","pondreis","pondran",
            "pondria","pondrias","pondriamos","pondriais","pondrian",
            "pongais"
        )
        add("salir",
            "salgo","sales","sale","salimos","salís","salen",
            "salí","saliste","salió","salisteis","salieron",
            "salía","salías","salíamos","salíais","salían",
            "saldré","saldrás","saldrá","saldremos","saldréis","saldrán",
            "saldría","saldrías","saldríamos","saldríais","saldrían",
            "salga","salgas","salgamos","salgáis","salgan",
            // without accents
            "salis","sali","salio","salia","salias","saliamos","saliais","salian",
            "saldre","saldras","saldra","saldremos","saldreis","saldran",
            "saldria","saldrias","saldriamos","saldriais","saldrian",
            "salgais"
        )
        add("dar",
            "doy","das","da","damos","dais","dan",
            "di","diste","dio","dimos","disteis","dieron",
            "daba","dabas","dábamos","dabais","daban",
            "daré","darás","dará","daremos","daréis","darán",
            "daría","darías","daríamos","daríais","darían",
            "dé","des","demos","deis","den",
            // without accents
            "dabamos","dare","daras","dara","daremos","dareis","daran",
            "daria","darias","dariamos","dariais","darian"
        )
        add("traer",
            "traigo","traes","trae","traemos","traéis","traen",
            "traje","trajiste","trajo","trajimos","trajisteis","trajeron",
            "traía","traías","traíamos","traíais","traían",
            "traeré","traerás","traerá","traeremos","traeréis","traerán",
            "traiga","traigas","traigamos","traigáis","traigan",
            // without accents
            "traeis","traia","traias","traiamos","traiais","traian",
            "traere","traeras","traera","traeremos","traereis","traeran",
            "traigais"
        )
        add("conocer",
            "conozco","conoces","conoce","conocemos","conocéis","conocen",
            "conocí","conociste","conoció","conocimos","conocisteis","conocieron",
            "conocía","conocías","conocíamos","conocíais","conocían",
            "conozca","conozcas","conozcamos","conozcáis","conozcan",
            // without accents
            "conoceis","conoci","conocio","conocia","conocias","conocemos","conociais","conocian",
            "conozcais"
        )
        add("pedir",
            "pido","pides","pide","pedimos","pedís","piden",
            "pedí","pediste","pidió","pedisteis","pidieron",
            "pedía","pedías","pedíamos","pedíais","pedían",
            "pida","pidas","pidamos","pidáis","pidan",
            // without accents
            "pedis","pedi","pidio","pedia","pedias","pediamos","pediais","pedian",
            "pidais"
        )
        add("seguir",
            "sigo","sigues","sigue","seguimos","seguís","siguen",
            "seguí","seguiste","siguió","seguimos","seguisteis","siguieron",
            "seguía","seguías","seguíamos","seguíais","seguían",
            "siga","sigas","sigamos","sigáis","sigan",
            // without accents
            "seguis","segui","siguio","seguia","seguias","seguiamos","seguiais","seguian",
            "sigais"
        )
        add("sentir",
            "siento","sientes","siente","sentimos","sentís","sienten",
            "sentí","sentiste","sintió","sentimos","sentisteis","sintieron",
            "sentía","sentías","sentíamos","sentíais","sentían",
            "sienta","sientas","sintamos","sintáis","sientan",
            // without accents
            "sentis","senti","sintio","sentia","sentias","sentiamos","sentiais","sentian",
            "sintais"
        )
        add("dormir",
            "duermo","duermes","duerme","dormimos","dormís","duermen",
            "dormí","dormiste","durmió","dormimos","dormisteis","durmieron",
            "dormía","dormías","dormíamos","dormíais","dormían",
            "duerma","duermas","durmamos","durmáis","duerman",
            // without accents
            "dormis","dormi","durmio","dormia","dormias","dormiamos","dormiais","dormian",
            "durmais"
        )
        add("jugar",
            "juego","juegas","juega","jugamos","jugáis","juegan",
            "jugué","jugaste","jugó","jugamos","jugasteis","jugaron",
            "jugaba","jugabas","jugábamos","jugabais","jugaban",
            "juegue","juegues","juguemos","juguéis","jueguen",
            // without accents
            "jugais","jugue","jugo","jugabamos","jugueis"
        )
        add("volver",
            "vuelvo","vuelves","vuelve","volvemos","volvéis","vuelven",
            "volví","volviste","volvió","volvimos","volvisteis","volvieron",
            "volvía","volvías","volvíamos","volvíais","volvían",
            "vuelva","vuelvas","volvamos","volváis","vuelvan",
            // without accents
            "volvei","volvi","volvio","volvia","volvias","volviamos","volviais","volvian",
            "volvais"
        )
        add("encontrar",
            "encuentro","encuentras","encuentra","encontramos","encontráis","encuentran",
            "encontré","encontraste","encontró","encontramos","encontrasteis","encontraron",
            "encontraba","encontrabas","encontrábamos","encontrabais","encontraban",
            "encuentre","encuentres","encontremos","encontréis","encuentren",
            // without accents
            "encontrais","encontre","encontro","encontrabamos","encontreis"
        )
        add("llevar",
            "llevo","llevas","lleva","llevamos","lleváis","llevan",
            "llevé","llevaste","llevó","llevamos","llevasteis","llevaron",
            "llevaba","llevabas","llevábamos","llevabais","llevaban",
            // without accents
            "llevais","lleve","llevo","llevabamos"
        )
        add("empezar",
            "empiezo","empiezas","empieza","empezamos","empezáis","empiezan",
            "empecé","empezaste","empezó","empezamos","empezasteis","empezaron",
            "empezaba","empezabas","empezábamos","empezabais","empezaban",
            "empiece","empieces","empecemos","empecéis","empiecen",
            // without accents
            "empezais","empece","empezo","empezabamos","empeceis"
        )
        add("hablar",
            "hablo","hablas","habla","hablamos","habláis","hablan",
            "hablé","hablaste","habló","hablamos","hablasteis","hablaron",
            "hablaba","hablabas","hablábamos","hablabais","hablaban",
            // without accents
            "hablais","hable","hablo","hablabamos"
        )
        add("comer",
            "como","comes","come","comemos","coméis","comen",
            "comí","comiste","comió","comimos","comisteis","comieron",
            "comía","comías","comíamos","comíais","comían",
            // without accents
            "comeis","comi","comio","comia","comias","comiamos","comiais","comian"
        )
        add("vivir",
            "vivo","vives","vive","vivimos","vivís","viven",
            "viví","viviste","vivió","vivimos","vivisteis","vivieron",
            "vivía","vivías","vivíamos","vivíais","vivían",
            // without accents
            "vivis","vivi","vivio","vivia","vivias","viviamos","viviais","vivian"
        )
        add("llegar",
            "llego","llegas","llega","llegamos","llegáis","llegan",
            "llegué","llegaste","llegó","llegamos","llegasteis","llegaron",
            "llegaba","llegabas","llegábamos","llegabais","llegaban",
            // without accents
            "llegais","llegue","llego","llegabamos"
        )
        add("caminar",
            "camino","caminas","camina","caminamos","camináis","caminan",
            "caminé","caminaste","caminó","caminamos","caminasteis","caminaron",
            "caminaba","caminabas","caminábamos","caminabais","caminaban"
        )
        add("trabajar",
            "trabajo","trabajas","trabaja","trabajamos","trabajáis","trabajan",
            "trabajé","trabajaste","trabajó","trabajamos","trabajasteis","trabajaron",
            "trabajaba","trabajabas","trabajábamos","trabajabais","trabajaban"
        )
        add("comprar",
            "compro","compras","compra","compramos","compráis","compran",
            "compré","compraste","compró","compramos","comprasteis","compraron",
            "compraba","comprabas","comprábamos","comprabais","compraban"
        )
        add("escribir",
            "escribo","escribes","escribe","escribimos","escribís","escriben",
            "escribí","escribiste","escribió","escribimos","escribisteis","escribieron",
            "escribía","escribías","escribíamos","escribíais","escribían",
            "escriba","escribas","escribamos","escribáis","escriban"
        )
        add("leer",
            "leo","lees","lee","leemos","leéis","leen",
            "leí","leíste","leyó","leímos","leísteis","leyeron",
            "leía","leías","leíamos","leíais","leían"
        )
        add("abrir",
            "abro","abres","abre","abrimos","abrís","abren",
            "abrí","abriste","abrió","abrimos","abristeis","abrieron",
            "abría","abrías","abríamos","abríais","abrían",
            "abierto"
        )
        add("cerrar",
            "cierro","cierras","cierra","cerramos","cerráis","cierran",
            "cerré","cerraste","cerró","cerramos","cerrasteis","cerraron",
            "cerraba","cerrabas","cerrábamos","cerrabais","cerraban"
        )
        add("entrar",
            "entro","entras","entra","entramos","entráis","entran",
            "entré","entraste","entró","entramos","entrasteis","entraron",
            "entraba","entrabas","entrábamos","entrabais","entraban"
        )
        add("mirar",
            "miro","miras","mira","miramos","miráis","miran",
            "miré","miraste","miró","miramos","mirasteis","miraron",
            "miraba","mirabas","mirábamos","mirabais","miraban"
        )
        add("escuchar",
            "escucho","escuchas","escucha","escuchamos","escucháis","escuchan",
            "escuché","escuchaste","escuchó","escuchamos","escuchasteis","escucharon",
            "escuchaba","escuchabas","escuchábamos","escuchabais","escuchaban"
        )
        add("ayudar",
            "ayudo","ayudas","ayuda","ayudamos","ayudáis","ayudan",
            "ayudé","ayudaste","ayudó","ayudamos","ayudasteis","ayudaron",
            "ayudaba","ayudabas","ayudábamos","ayudabais","ayudaban"
        )
        add("necesitar",
            "necesito","necesitas","necesita","necesitamos","necesitáis","necesitan",
            "necesité","necesitaste","necesitó","necesitamos","necesitasteis","necesitaron",
            "necesitaba","necesitabas","necesitábamos","necesitabais","necesitaban"
        )
        add("pensar",
            "pienso","piensas","piensa","pensamos","pensáis","piensan",
            "pensé","pensaste","pensó","pensamos","pensasteis","pensaron",
            "pensaba","pensabas","pensábamos","pensabais","pensaban"
        )
        add("esperar",
            "espero","esperas","espera","esperamos","esperáis","esperan",
            "esperé","esperaste","esperó","esperamos","esperasteis","esperaron",
            "esperaba","esperabas","esperábamos","esperabais","esperaban"
        )
        add("preguntar",
            "pregunto","preguntas","pregunta","preguntamos","preguntáis","preguntan",
            "pregunté","preguntaste","preguntó","preguntamos","preguntasteis","preguntaron",
            "preguntaba","preguntabas","preguntábamos","preguntabais","preguntaban"
        )
        add("contestar",
            "contesto","contestas","contesta","contestamos","contestáis","contestan",
            "contesté","contestaste","contestó","contestamos","contestasteis","contestaron",
            "contestaba","contestabas","contestábamos","contestabais","contestaban"
        )
        add("preparar",
            "preparo","preparas","prepara","preparamos","preparáis","preparan",
            "preparé","preparaste","preparó","preparamos","preparasteis","prepararon",
            "preparaba","preparabas","preparábamos","preparabais","preparaban"
        )
        add("cocinar",
            "cocino","cocinas","cocina","cocinamos","cocináis","cocinan",
            "cociné","cocinaste","cocinó","cocinamos","cocinasteis","cocinaron",
            "cocinaba","cocinabas","cocinábamos","cocinabais","cocinaban"
        )
        add("beber",
            "bebo","bebes","bebe","bebemos","bebéis","beben",
            "bebí","bebiste","bebió","bebimos","bebisteis","bebieron",
            "bebía","bebías","bebíamos","bebíais","bebían"
        )
        add("correr",
            "corro","corres","corre","corremos","corréis","corren",
            "corrí","corriste","corrió","corrimos","corristeis","corrieron",
            "corría","corrías","corríamos","corríais","corrían"
        )
        add("subir",
            "subo","subes","sube","subimos","subís","suben",
            "subí","subiste","subió","subimos","subisteis","subieron",
            "subía","subías","subíamos","subíais","subían"
        )
        add("bajar",
            "bajo","bajas","baja","bajamos","bajáis","bajan",
            "bajé","bajaste","bajó","bajamos","bajasteis","bajaron",
            "bajaba","bajabas","bajábamos","bajabais","bajaban"
        )
        add("pasar",
            "paso","pasas","pasa","pasamos","pasáis","pasan",
            "pasé","pasaste","pasó","pasamos","pasasteis","pasaron",
            "pasaba","pasabas","pasábamos","pasabais","pasaban"
        )
        add("llamar",
            "llamo","llamas","llama","llamamos","llamáis","llaman",
            "llamé","llamaste","llamó","llamamos","llamasteis","llamaron",
            "llamaba","llamabas","llamábamos","llamabais","llamaban"
        )
        add("usar",
            "uso","usas","usa","usamos","usáis","usan",
            "usé","usaste","usó","usamos","usasteis","usaron",
            "usaba","usabas","usábamos","usabais","usaban"
        )
        add("gustar",
            "gusta","gustan","gustaba","gustaban","gustó","gustaron",
            "gusté","gustaste","gustasteis"
        )
        add("quedar",
            "quedo","quedas","queda","quedamos","quedáis","quedan",
            "quedé","quedaste","quedó","quedamos","quedasteis","quedaron",
            "quedaba","quedabas","quedábamos","quedabais","quedaban"
        )

        return m
    }
}
