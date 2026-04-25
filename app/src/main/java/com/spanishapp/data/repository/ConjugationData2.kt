package com.spanishapp.data.repository

import com.spanishapp.data.db.entity.ConjugationEntity

/**
 * Глаголы 22–100 (топ-100 испанских глаголов).
 * Тех же 6 времён: presente, preterito, imperfecto, futuro, condicional, subjuntivo
 */
object ConjugationData2 {

    fun getAll(): List<ConjugationEntity> = buildList {

        fun add(
            verb: String, tense: String,
            yo: String, tu: String, el: String,
            nos: String, vos: String, ellos: String,
            irregular: Boolean = false, note: String = ""
        ) = add(ConjugationEntity(
            verb=verb, tense=tense,
            yo=yo, tu=tu, el=el,
            nosotros=nos, vosotros=vos, ellos=ellos,
            isIrregular=irregular, note=note
        ))

        // ── ABRIR (открывать) ─────────────────────────────────
        add("abrir","presente","abro","abres","abre","abrimos","abrís","abren")
        add("abrir","preterito","abrí","abriste","abrió","abrimos","abristeis","abrieron")
        add("abrir","imperfecto","abría","abrías","abría","abríamos","abríais","abrían")
        add("abrir","futuro","abriré","abrirás","abrirá","abriremos","abriréis","abrirán")
        add("abrir","condicional","abriría","abrirías","abriría","abriríamos","abriríais","abrirían")
        add("abrir","subjuntivo","abra","abras","abra","abramos","abráis","abran")

        // ── ANDAR (ходить пешком) ─────────────────────────────
        add("andar","presente","ando","andas","anda","andamos","andáis","andan")
        add("andar","preterito","anduve","anduviste","anduvo","anduvimos","anduvisteis","anduvieron",true)
        add("andar","imperfecto","andaba","andabas","andaba","andábamos","andabais","andaban")
        add("andar","futuro","andaré","andarás","andará","andaremos","andaréis","andarán")
        add("andar","condicional","andaría","andarías","andaría","andaríamos","andaríais","andarían")
        add("andar","subjuntivo","ande","andes","ande","andemos","andéis","anden")

        // ── AYUDAR (помогать) ─────────────────────────────────
        add("ayudar","presente","ayudo","ayudas","ayuda","ayudamos","ayudáis","ayudan")
        add("ayudar","preterito","ayudé","ayudaste","ayudó","ayudamos","ayudasteis","ayudaron")
        add("ayudar","imperfecto","ayudaba","ayudabas","ayudaba","ayudábamos","ayudabais","ayudaban")
        add("ayudar","futuro","ayudaré","ayudarás","ayudará","ayudaremos","ayudaréis","ayudarán")
        add("ayudar","condicional","ayudaría","ayudarías","ayudaría","ayudaríamos","ayudaríais","ayudarían")
        add("ayudar","subjuntivo","ayude","ayudes","ayude","ayudemos","ayudéis","ayuden")

        // ── BUSCAR (искать) ───────────────────────────────────
        add("buscar","presente","busco","buscas","busca","buscamos","buscáis","buscan")
        add("buscar","preterito","busqué","buscaste","buscó","buscamos","buscasteis","buscaron",true,"c→qu ante e")
        add("buscar","imperfecto","buscaba","buscabas","buscaba","buscábamos","buscabais","buscaban")
        add("buscar","futuro","buscaré","buscarás","buscará","buscaremos","buscaréis","buscarán")
        add("buscar","condicional","buscaría","buscarías","buscaría","buscaríamos","buscaríais","buscarían")
        add("buscar","subjuntivo","busque","busques","busque","busquemos","busquéis","busquen")

        // ── CAMINAR (идти/ходить) ─────────────────────────────
        add("caminar","presente","camino","caminas","camina","caminamos","camináis","caminan")
        add("caminar","preterito","caminé","caminaste","caminó","caminamos","caminasteis","caminaron")
        add("caminar","imperfecto","caminaba","caminabas","caminaba","caminábamos","caminabais","caminaban")
        add("caminar","futuro","caminaré","caminarás","caminará","caminaremos","caminaréis","caminarán")
        add("caminar","condicional","caminaría","caminarías","caminaría","caminaríamos","caminaríais","caminarían")
        add("caminar","subjuntivo","camine","camines","camine","caminemos","caminéis","caminen")

        // ── CANTAR (петь) ─────────────────────────────────────
        add("cantar","presente","canto","cantas","canta","cantamos","cantáis","cantan")
        add("cantar","preterito","canté","cantaste","cantó","cantamos","cantasteis","cantaron")
        add("cantar","imperfecto","cantaba","cantabas","cantaba","cantábamos","cantabais","cantaban")
        add("cantar","futuro","cantaré","cantarás","cantará","cantaremos","cantaréis","cantarán")
        add("cantar","condicional","cantaría","cantarías","cantaría","cantaríamos","cantaríais","cantarían")
        add("cantar","subjuntivo","cante","cantes","cante","cantemos","cantéis","canten")

        // ── CERRAR (закрывать) e→ie ───────────────────────────
        add("cerrar","presente","cierro","cierras","cierra","cerramos","cerráis","cierran",true,"e→ie")
        add("cerrar","preterito","cerré","cerraste","cerró","cerramos","cerrasteis","cerraron")
        add("cerrar","imperfecto","cerraba","cerrabas","cerraba","cerrábamos","cerrabais","cerraban")
        add("cerrar","futuro","cerraré","cerrarás","cerrará","cerraremos","cerraréis","cerrarán")
        add("cerrar","condicional","cerraría","cerrarías","cerraría","cerraríamos","cerraríais","cerrarían")
        add("cerrar","subjuntivo","cierre","cierres","cierre","cerremos","cerréis","cierren",true)

        // ── COCINAR (готовить еду) ────────────────────────────
        add("cocinar","presente","cocino","cocinas","cocina","cocinamos","cocináis","cocinan")
        add("cocinar","preterito","cociné","cocinaste","cocinó","cocinamos","cocinasteis","cocinaron")
        add("cocinar","imperfecto","cocinaba","cocinabas","cocinaba","cocinábamos","cocinabais","cocinaban")
        add("cocinar","futuro","cocinaré","cocinarás","cocinará","cocinaremos","cocinaréis","cocinarán")
        add("cocinar","condicional","cocinaría","cocinarías","cocinaría","cocinaríamos","cocinaríais","cocinarían")
        add("cocinar","subjuntivo","cocine","cocines","cocine","cocinemos","coci néis","cocinen")

        // ── COMPRAR (покупать) ────────────────────────────────
        add("comprar","presente","compro","compras","compra","compramos","compráis","compran")
        add("comprar","preterito","compré","compraste","compró","compramos","comprasteis","compraron")
        add("comprar","imperfecto","compraba","comprabas","compraba","comprábamos","comprabais","compraban")
        add("comprar","futuro","compraré","comprarás","comprará","compraremos","compraréis","comprarán")
        add("comprar","condicional","compraría","comprarías","compraría","compraríamos","compraríais","comprarían")
        add("comprar","subjuntivo","compre","compres","compre","compremos","compréis","compren")

        // ── CONDUCIR (водить) ─────────────────────────────────
        add("conducir","presente","conduzco","conduces","conduce","conducimos","conducís","conducen",true,"1sg -zco")
        add("conducir","preterito","conduje","condujiste","condujo","condujimos","condujisteis","condujeron",true)
        add("conducir","imperfecto","conducía","conducías","conducía","conducíamos","conducíais","conducían")
        add("conducir","futuro","conduciré","conducirás","conducirá","conduciremos","conduciréis","conducirán")
        add("conducir","condicional","conduciría","conducirías","conduciría","conduciríamos","conduciríais","conducirían")
        add("conducir","subjuntivo","conduzca","conduzcas","conduzca","conduzcamos","conduzcáis","conduzcan",true)

        // ── CONSTRUIR (строить) ───────────────────────────────
        add("construir","presente","construyo","construyes","construye","construimos","construís","construyen",true,"y insertion")
        add("construir","preterito","construí","construiste","construyó","construimos","construisteis","construyeron",true)
        add("construir","imperfecto","construía","construías","construía","construíamos","construíais","construían")
        add("construir","futuro","construiré","construirás","construirá","construiremos","construiréis","construirán")
        add("construir","condicional","construiría","construirías","construiría","construiríamos","construiríais","construirían")
        add("construir","subjuntivo","construya","construyas","construya","construyamos","construyáis","construyan",true)

        // ── CORRER (бежать) ───────────────────────────────────
        add("correr","presente","corro","corres","corre","corremos","corréis","corren")
        add("correr","preterito","corrí","corriste","corrió","corrimos","corristeis","corrieron")
        add("correr","imperfecto","corría","corrías","corría","corríamos","corríais","corrían")
        add("correr","futuro","correré","correrás","correrá","correremos","correréis","correrán")
        add("correr","condicional","correría","correrías","correría","correríamos","correríais","correrían")
        add("correr","subjuntivo","corra","corras","corra","corramos","corráis","corran")

        // ── COSTAR (стоить) o→ue ─────────────────────────────
        add("costar","presente","cuesto","cuestas","cuesta","costamos","costáis","cuestan",true,"o→ue")
        add("costar","preterito","costé","costaste","costó","costamos","costasteis","costaron")
        add("costar","imperfecto","costaba","costabas","costaba","costábamos","costabais","costaban")
        add("costar","futuro","costaré","costarás","costará","costaremos","costaréis","costarán")
        add("costar","condicional","costaría","costarías","costaría","costaríamos","costaríais","costarían")
        add("costar","subjuntivo","cueste","cuestes","cueste","costemos","costéis","cuesten",true)

        // ── CREER (верить/думать) ─────────────────────────────
        add("creer","presente","creo","crees","cree","creemos","creéis","creen")
        add("creer","preterito","creí","creíste","creyó","creímos","creísteis","creyeron",true,"y en 3sg/pl")
        add("creer","imperfecto","creía","creías","creía","creíamos","creíais","creían")
        add("creer","futuro","creeré","creerás","creerá","creeremos","creeréis","creerán")
        add("creer","condicional","creería","creerías","creería","creeríamos","creeríais","creerían")
        add("creer","subjuntivo","crea","creas","crea","creamos","creáis","crean")

        // ── DEJAR (оставлять/бросать) ─────────────────────────
        add("dejar","presente","dejo","dejas","deja","dejamos","dejáis","dejan")
        add("dejar","preterito","dejé","dejaste","dejó","dejamos","dejasteis","dejaron")
        add("dejar","imperfecto","dejaba","dejabas","dejaba","dejábamos","dejabais","dejaban")
        add("dejar","futuro","dejaré","dejarás","dejará","dejaremos","dejaréis","dejarán")
        add("dejar","condicional","dejaría","dejarías","dejaría","dejaríamos","dejaríais","dejarían")
        add("dejar","subjuntivo","deje","dejes","deje","dejemos","dejéis","dejen")

        // ── EMPEZAR (начинать) e→ie ───────────────────────────
        add("empezar","presente","empiezo","empiezas","empieza","empezamos","empezáis","empiezan",true,"e→ie, z→c")
        add("empezar","preterito","empecé","empezaste","empezó","empezamos","empezasteis","empezaron",true)
        add("empezar","imperfecto","empezaba","empezabas","empezaba","empezábamos","empezabais","empezaban")
        add("empezar","futuro","empezaré","empezarás","empezará","empezaremos","empezaréis","empezarán")
        add("empezar","condicional","empezaría","empezarías","empezaría","empezaríamos","empezaríais","empezarían")
        add("empezar","subjuntivo","empiece","empieces","empiece","empecemos","empecéis","empiecen",true)

        // ── ENCONTRAR (находить) o→ue ─────────────────────────
        add("encontrar","presente","encuentro","encuentras","encuentra","encontramos","encontráis","encuentran",true,"o→ue")
        add("encontrar","preterito","encontré","encontraste","encontró","encontramos","encontrasteis","encontraron")
        add("encontrar","imperfecto","encontraba","encontrabas","encontraba","encontrábamos","encontrabais","encontraban")
        add("encontrar","futuro","encontraré","encontrarás","encontrará","encontraremos","encontraréis","encontrarán")
        add("encontrar","condicional","encontraría","encontrarías","encontraría","encontraríamos","encontraríais","encontrarían")
        add("encontrar","subjuntivo","encuentre","encuentres","encuentre","encontremos","encontréis","encuentren",true)

        // ── ENTENDER (понимать) e→ie ──────────────────────────
        add("entender","presente","entiendo","entiendes","entiende","entendemos","entendéis","entienden",true,"e→ie")
        add("entender","preterito","entendí","entendiste","entendió","entendimos","entendisteis","entendieron")
        add("entender","imperfecto","entendía","entendías","entendía","entendíamos","entendíais","entendían")
        add("entender","futuro","entenderé","entenderás","entenderá","entenderemos","entenderéis","entenderán")
        add("entender","condicional","entendería","entenderías","entendería","entenderíamos","entenderíais","entenderían")
        add("entender","subjuntivo","entienda","entiendas","entienda","entendamos","entendáis","entiendan",true)

        // ── ESCRIBIR (писать) ─────────────────────────────────
        add("escribir","presente","escribo","escribes","escribe","escribimos","escribís","escriben")
        add("escribir","preterito","escribí","escribiste","escribió","escribimos","escribisteis","escribieron")
        add("escribir","imperfecto","escribía","escribías","escribía","escribíamos","escribíais","escribían")
        add("escribir","futuro","escribiré","escribirás","escribirá","escribiremos","escribiréis","escribirán")
        add("escribir","condicional","escribiría","escribirías","escribiría","escribiríamos","escribiríais","escribirían")
        add("escribir","subjuntivo","escriba","escribas","escriba","escribamos","escribáis","escriban")

        // ── ESCUCHAR (слушать) ────────────────────────────────
        add("escuchar","presente","escucho","escuchas","escucha","escuchamos","escucháis","escuchan")
        add("escuchar","preterito","escuché","escuchaste","escuchó","escuchamos","escuchasteis","escucharon")
        add("escuchar","imperfecto","escuchaba","escuchabas","escuchaba","escuchábamos","escuchabais","escuchaban")
        add("escuchar","futuro","escucharé","escucharás","escuchará","escucharemos","escucharéis","escucharán")
        add("escuchar","condicional","escucharía","escucharías","escucharía","escucharíamos","escucharíais","escucharían")
        add("escuchar","subjuntivo","escuche","escuches","escuche","escuchemos","escuchéis","escuchen")

        // ── ESPERAR (ждать/надеяться) ─────────────────────────
        add("esperar","presente","espero","esperas","espera","esperamos","esperáis","esperan")
        add("esperar","preterito","esperé","esperaste","esperó","esperamos","esperasteis","esperaron")
        add("esperar","imperfecto","esperaba","esperabas","esperaba","esperábamos","esperabais","esperaban")
        add("esperar","futuro","esperaré","esperarás","esperará","esperaremos","esperaréis","esperarán")
        add("esperar","condicional","esperaría","esperarías","esperaría","esperaríamos","esperaríais","esperarían")
        add("esperar","subjuntivo","espere","esperes","espere","esperemos","esperéis","esperen")

        // ── ESTUDIAR (учиться) ────────────────────────────────
        add("estudiar","presente","estudio","estudias","estudia","estudiamos","estudiáis","estudian")
        add("estudiar","preterito","estudié","estudiaste","estudió","estudiamos","estudiasteis","estudiaron")
        add("estudiar","imperfecto","estudiaba","estudiabas","estudiaba","estudiábamos","estudiabais","estudiaban")
        add("estudiar","futuro","estudiaré","estudiarás","estudiará","estudiaremos","estudiaréis","estudiarán")
        add("estudiar","condicional","estudiaría","estudiarías","estudiaría","estudiaríamos","estudiaríais","estudiarían")
        add("estudiar","subjuntivo","estudie","estudies","estudie","estudiemos","estudiéis","estudien")

        // ── GANAR (выигрывать/зарабатывать) ──────────────────
        add("ganar","presente","gano","ganas","gana","ganamos","ganáis","ganan")
        add("ganar","preterito","gané","ganaste","ganó","ganamos","ganasteis","ganaron")
        add("ganar","imperfecto","ganaba","ganabas","ganaba","ganábamos","ganabais","ganaban")
        add("ganar","futuro","ganaré","ganarás","ganará","ganaremos","ganaréis","ganarán")
        add("ganar","condicional","ganaría","ganarías","ganaría","ganaríamos","ganaríais","ganarían")
        add("ganar","subjuntivo","gane","ganes","gane","ganemos","ganéis","ganen")

        // ── GUSTAR (нравиться) ────────────────────────────────
        add("gustar","presente","gusto","gustas","gusta","gustamos","gustáis","gustan")
        add("gustar","preterito","gusté","gustaste","gustó","gustamos","gustasteis","gustaron")
        add("gustar","imperfecto","gustaba","gustabas","gustaba","gustábamos","gustabais","gustaban")
        add("gustar","futuro","gustaré","gustarás","gustará","gustaremos","gustaréis","gustarán")
        add("gustar","condicional","gustaría","gustarías","gustaría","gustaríamos","gustaríais","gustarían")
        add("gustar","subjuntivo","guste","gustes","guste","gustemos","gustéis","gusten")

        // ── HABLAR (говорить) ─────────────────────────────────
        // (уже есть в ConjugationData, пропускаем)

        // ── JUGAR (играть) u→ue ───────────────────────────────
        add("jugar","presente","juego","juegas","juega","jugamos","jugáis","juegan",true,"u→ue")
        add("jugar","preterito","jugué","jugaste","jugó","jugamos","jugasteis","jugaron",true,"g→gu")
        add("jugar","imperfecto","jugaba","jugabas","jugaba","jugábamos","jugabais","jugaban")
        add("jugar","futuro","jugaré","jugarás","jugará","jugaremos","jugaréis","jugarán")
        add("jugar","condicional","jugaría","jugarías","jugaría","jugaríamos","jugaríais","jugarían")
        add("jugar","subjuntivo","juegue","juegues","juegue","juguemos","juguéis","jueguen",true)

        // ── LEER (читать) ─────────────────────────────────────
        add("leer","presente","leo","lees","lee","leemos","leéis","leen")
        add("leer","preterito","leí","leíste","leyó","leímos","leísteis","leyeron",true,"y en 3sg/pl")
        add("leer","imperfecto","leía","leías","leía","leíamos","leíais","leían")
        add("leer","futuro","leeré","leerás","leerá","leeremos","leeréis","leerán")
        add("leer","condicional","leería","leerías","leería","leeríamos","leeríais","leerían")
        add("leer","subjuntivo","lea","leas","lea","leamos","leáis","lean")

        // ── LLAMAR (звать/звонить) ────────────────────────────
        add("llamar","presente","llamo","llamas","llama","llamamos","llamáis","llaman")
        add("llamar","preterito","llamé","llamaste","llamó","llamamos","llamasteis","llamaron")
        add("llamar","imperfecto","llamaba","llamabas","llamaba","llamábamos","llamabais","llamaban")
        add("llamar","futuro","llamaré","llamarás","llamará","llamaremos","llamaréis","llamarán")
        add("llamar","condicional","llamaría","llamarías","llamaría","llamaríamos","llamaríais","llamarían")
        add("llamar","subjuntivo","llame","llames","llame","llamemos","llaméis","llamen")

        // ── LLEGAR (приходить/приезжать) ─────────────────────
        add("llegar","presente","llego","llegas","llega","llegamos","llegáis","llegan")
        add("llegar","preterito","llegué","llegaste","llegó","llegamos","llegasteis","llegaron",true,"g→gu")
        add("llegar","imperfecto","llegaba","llegabas","llegaba","llegábamos","llegabais","llegaban")
        add("llegar","futuro","llegaré","llegarás","llegará","llegaremos","llegaréis","llegarán")
        add("llegar","condicional","llegaría","llegarías","llegaría","llegaríamos","llegaríais","llegarían")
        add("llegar","subjuntivo","llegue","llegues","llegue","lleguemos","lleguéis","lleguen",true)

        // ── LLEVAR (нести/носить) ─────────────────────────────
        add("llevar","presente","llevo","llevas","lleva","llevamos","lleváis","llevan")
        add("llevar","preterito","llevé","llevaste","llevó","llevamos","llevasteis","llevaron")
        add("llevar","imperfecto","llevaba","llevabas","llevaba","llevábamos","llevabais","llevaban")
        add("llevar","futuro","llevaré","llevarás","llevará","llevaremos","llevaréis","llevarán")
        add("llevar","condicional","llevaría","llevarías","llevaría","llevaríamos","llevaríais","llevarían")
        add("llevar","subjuntivo","lleve","lleves","lleve","llevemos","llevéis","lleven")

        // ── MIRAR (смотреть) ──────────────────────────────────
        add("mirar","presente","miro","miras","mira","miramos","miráis","miran")
        add("mirar","preterito","miré","miraste","miró","miramos","mirasteis","miraron")
        add("mirar","imperfecto","miraba","mirabas","miraba","mirábamos","mirabais","miraban")
        add("mirar","futuro","miraré","mirarás","mirará","miraremos","miraréis","mirarán")
        add("mirar","condicional","miraría","mirarías","miraría","miraríamos","miraríais","mirarían")
        add("mirar","subjuntivo","mire","mires","mire","miremos","miréis","miren")

        // ── MORIR (умирать) o→ue ─────────────────────────────
        add("morir","presente","muero","mueres","muere","morimos","morís","mueren",true,"o→ue")
        add("morir","preterito","morí","moriste","murió","morimos","moristeis","murieron",true,"o→u en 3")
        add("morir","imperfecto","moría","morías","moría","moríamos","moríais","morían")
        add("morir","futuro","moriré","morirás","morirá","moriremos","moriréis","morirán")
        add("morir","condicional","moriría","morirías","moriría","moriríamos","moriríais","morirían")
        add("morir","subjuntivo","muera","mueras","muera","muramos","muráis","mueran",true)

        // ── MOSTRAR (показывать) o→ue ─────────────────────────
        add("mostrar","presente","muestro","muestras","muestra","mostramos","mostráis","muestran",true,"o→ue")
        add("mostrar","preterito","mostré","mostraste","mostró","mostramos","mostrasteis","mostraron")
        add("mostrar","imperfecto","mostraba","mostrabas","mostraba","mostrábamos","mostrabais","mostraban")
        add("mostrar","futuro","mostraré","mostrarás","mostrará","mostraremos","mostraréis","mostrarán")
        add("mostrar","condicional","mostraría","mostrarías","mostraría","mostraríamos","mostraríais","mostrarían")
        add("mostrar","subjuntivo","muestre","muestres","muestre","mostremos","mostréis","muestren",true)

        // ── NECESITAR (нуждаться) ─────────────────────────────
        add("necesitar","presente","necesito","necesitas","necesita","necesitamos","necesitáis","necesitan")
        add("necesitar","preterito","necesité","necesitaste","necesitó","necesitamos","necesitasteis","necesitaron")
        add("necesitar","imperfecto","necesitaba","necesitabas","necesitaba","necesitábamos","necesitabais","necesitaban")
        add("necesitar","futuro","necesitaré","necesitarás","necesitará","necesitaremos","necesitaréis","necesitarán")
        add("necesitar","condicional","necesitaría","necesitarías","necesitaría","necesitaríamos","necesitaríais","necesitarían")
        add("necesitar","subjuntivo","necesite","necesites","necesite","necesitemos","necesitéis","necesiten")

        // ── OÍR (слышать) ─────────────────────────────────────
        add("oír","presente","oigo","oyes","oye","oímos","oís","oyen",true,"1sg -go, y insertion")
        add("oír","preterito","oí","oíste","oyó","oímos","oísteis","oyeron",true)
        add("oír","imperfecto","oía","oías","oía","oíamos","oíais","oían")
        add("oír","futuro","oiré","oirás","oirá","oiremos","oiréis","oirán")
        add("oír","condicional","oiría","oirías","oiría","oiríamos","oiríais","oirían")
        add("oír","subjuntivo","oiga","oigas","oiga","oigamos","oigáis","oigan",true)

        // ── OLVIDAR (забывать) ────────────────────────────────
        add("olvidar","presente","olvido","olvidas","olvida","olvidamos","olvidáis","olvidan")
        add("olvidar","preterito","olvidé","olvidaste","olvidó","olvidamos","olvidasteis","olvidaron")
        add("olvidar","imperfecto","olvidaba","olvidabas","olvidaba","olvidábamos","olvidabais","olvidaban")
        add("olvidar","futuro","olvidaré","olvidarás","olvidará","olvidaremos","olvidaréis","olvidarán")
        add("olvidar","condicional","olvidaría","olvidarías","olvidaría","olvidaríamos","olvidaríais","olvidarían")
        add("olvidar","subjuntivo","olvide","olvides","olvide","olvidemos","olvidéis","olviden")

        // ── PAGAR (платить) ───────────────────────────────────
        add("pagar","presente","pago","pagas","paga","pagamos","pagáis","pagan")
        add("pagar","preterito","pagué","pagaste","pagó","pagamos","pagasteis","pagaron",true,"g→gu")
        add("pagar","imperfecto","pagaba","pagabas","pagaba","pagábamos","pagabais","pagaban")
        add("pagar","futuro","pagaré","pagarás","pagará","pagaremos","pagaréis","pagarán")
        add("pagar","condicional","pagaría","pagarías","pagaría","pagaríamos","pagaríais","pagarían")
        add("pagar","subjuntivo","pague","pagues","pague","paguemos","paguéis","paguen",true)

        // ── PASAR (проходить/случаться) ───────────────────────
        add("pasar","presente","paso","pasas","pasa","pasamos","pasáis","pasan")
        add("pasar","preterito","pasé","pasaste","pasó","pasamos","pasasteis","pasaron")
        add("pasar","imperfecto","pasaba","pasabas","pasaba","pasábamos","pasabais","pasaban")
        add("pasar","futuro","pasaré","pasarás","pasará","pasaremos","pasaréis","pasarán")
        add("pasar","condicional","pasaría","pasarías","pasaría","pasaríamos","pasaríais","pasarían")
        add("pasar","subjuntivo","pase","pases","pase","pasemos","paséis","pasen")

        // ── PENSAR (думать) e→ie ──────────────────────────────
        add("pensar","presente","pienso","piensas","piensa","pensamos","pensáis","piensan",true,"e→ie")
        add("pensar","preterito","pensé","pensaste","pensó","pensamos","pensasteis","pensaron")
        add("pensar","imperfecto","pensaba","pensabas","pensaba","pensábamos","pensabais","pensaban")
        add("pensar","futuro","pensaré","pensarás","pensará","pensaremos","pensaréis","pensarán")
        add("pensar","condicional","pensaría","pensarías","pensaría","pensaríamos","pensaríais","pensarían")
        add("pensar","subjuntivo","piense","pienses","piense","pensemos","penséis","piensen",true)

        // ── PERDER (терять) e→ie ──────────────────────────────
        add("perder","presente","pierdo","pierdes","pierde","perdemos","perdéis","pierden",true,"e→ie")
        add("perder","preterito","perdí","perdiste","perdió","perdimos","perdisteis","perdieron")
        add("perder","imperfecto","perdía","perdías","perdía","perdíamos","perdíais","perdían")
        add("perder","futuro","perderé","perderás","perderá","perderemos","perderéis","perderán")
        add("perder","condicional","perdería","perderías","perdería","perderíamos","perderíais","perderían")
        add("perder","subjuntivo","pierda","pierdas","pierda","perdamos","perdáis","pierdan",true)

        // ── PREFERIR (предпочитать) e→ie/i ───────────────────
        add("preferir","presente","prefiero","prefieres","prefiere","preferimos","preferís","prefieren",true,"e→ie")
        add("preferir","preterito","preferí","preferiste","prefirió","preferimos","preferisteis","prefirieron",true,"e→i en 3")
        add("preferir","imperfecto","prefería","preferías","prefería","prefería mos","preferíais","preferían")
        add("preferir","futuro","preferiré","preferirás","preferirá","preferiremos","preferiréis","preferirán")
        add("preferir","condicional","preferiría","preferirías","preferiría","preferiríamos","preferiríais","preferirían")
        add("preferir","subjuntivo","prefiera","prefieras","prefiera","prefiramos","prefiráis","prefieran",true)

        // ── RECORDAR (вспоминать) o→ue ───────────────────────
        add("recordar","presente","recuerdo","recuerdas","recuerda","recordamos","recordáis","recuerdan",true,"o→ue")
        add("recordar","preterito","recordé","recordaste","recordó","recordamos","recordasteis","recordaron")
        add("recordar","imperfecto","recordaba","recordabas","recordaba","recordábamos","recordabais","recordaban")
        add("recordar","futuro","recordaré","recordarás","recordará","recordaremos","recordaréis","recordarán")
        add("recordar","condicional","recordaría","recordarías","recordaría","recordaríamos","recordaríais","recordarían")
        add("recordar","subjuntivo","recuerde","recuerdes","recuerde","recordemos","recordéis","recuerden",true)

        // ── REGRESAR (возвращаться) ───────────────────────────
        add("regresar","presente","regreso","regresas","regresa","regresamos","regresáis","regresan")
        add("regresar","preterito","regresé","regresaste","regresó","regresamos","regresasteis","regresaron")
        add("regresar","imperfecto","regresaba","regresabas","regresaba","regresábamos","regresabais","regresaban")
        add("regresar","futuro","regresaré","regresarás","regresará","regresaremos","regresaréis","regresarán")
        add("regresar","condicional","regresaría","regresarías","regresaría","regresaríamos","regresaríais","regresarían")
        add("regresar","subjuntivo","regrese","regreses","regrese","regresemos","regreséis","regresen")

        // ── SALIR (выходить) ──────────────────────────────────
        // (уже есть в ConjugationData, пропускаем)

        // ── SEGUIR (следовать/продолжать) e→i ────────────────
        add("seguir","presente","sigo","sigues","sigue","seguimos","seguís","siguen",true,"e→i, g→gu")
        add("seguir","preterito","seguí","seguiste","siguió","seguimos","seguisteis","siguieron",true)
        add("seguir","imperfecto","seguía","seguías","seguía","seguíamos","seguíais","seguían")
        add("seguir","futuro","seguiré","seguirás","seguirá","seguiremos","seguiréis","seguirán")
        add("seguir","condicional","seguiría","seguirías","seguiría","seguiríamos","seguiríais","seguirían")
        add("seguir","subjuntivo","siga","sigas","siga","sigamos","sigáis","sigan",true)

        // ── SENTIR (чувствовать) e→ie/i ───────────────────────
        add("sentir","presente","siento","sientes","siente","sentimos","sentís","sienten",true,"e→ie")
        add("sentir","preterito","sentí","sentiste","sintió","sentimos","sentisteis","sintieron",true,"e→i en 3")
        add("sentir","imperfecto","sentía","sentías","sentía","sentíamos","sentíais","sentían")
        add("sentir","futuro","sentiré","sentirás","sentirá","sentiremos","sentiréis","sentirán")
        add("sentir","condicional","sentiría","sentirías","sentiría","sentiríamos","sentiríais","sentirían")
        add("sentir","subjuntivo","sienta","sientas","sienta","sintamos","sintáis","sientan",true)

        // ── SUBIR (подниматься) ───────────────────────────────
        add("subir","presente","subo","subes","sube","subimos","subís","suben")
        add("subir","preterito","subí","subiste","subió","subimos","subisteis","subieron")
        add("subir","imperfecto","subía","subías","subía","subíamos","subíais","subían")
        add("subir","futuro","subiré","subirás","subirá","subiremos","subiréis","subirán")
        add("subir","condicional","subiría","subirías","subiría","subiríamos","subiríais","subirían")
        add("subir","subjuntivo","suba","subas","suba","subamos","subáis","suban")

        // ── TERMINAR (заканчивать) ────────────────────────────
        add("terminar","presente","termino","terminas","termina","terminamos","termináis","terminan")
        add("terminar","preterito","terminé","terminaste","terminó","terminamos","terminasteis","terminaron")
        add("terminar","imperfecto","terminaba","terminabas","terminaba","terminábamos","terminabais","terminaban")
        add("terminar","futuro","terminaré","terminarás","terminará","terminaremos","terminaréis","terminarán")
        add("terminar","condicional","terminaría","terminarías","terminaría","terminaríamos","terminaríais","terminarían")
        add("terminar","subjuntivo","termine","termines","termine","terminemos","terminéis","terminen")

        // ── TOCAR (трогать/играть на инстр.) ─────────────────
        add("tocar","presente","toco","tocas","toca","tocamos","tocáis","tocan")
        add("tocar","preterito","toqué","tocaste","tocó","tocamos","tocasteis","tocaron",true,"c→qu")
        add("tocar","imperfecto","tocaba","tocabas","tocaba","tocábamos","tocabais","tocaban")
        add("tocar","futuro","tocaré","tocarás","tocará","tocaremos","tocaréis","tocarán")
        add("tocar","condicional","tocaría","tocarías","tocaría","tocaríamos","tocaríais","tocarían")
        add("tocar","subjuntivo","toque","toques","toque","toquemos","toquéis","toquen",true)

        // ── TOMAR (брать/пить) ────────────────────────────────
        add("tomar","presente","tomo","tomas","toma","tomamos","tomáis","toman")
        add("tomar","preterito","tomé","tomaste","tomó","tomamos","tomasteis","tomaron")
        add("tomar","imperfecto","tomaba","tomabas","tomaba","tomábamos","tomabais","tomaban")
        add("tomar","futuro","tomaré","tomarás","tomará","tomaremos","tomaréis","tomarán")
        add("tomar","condicional","tomaría","tomarías","tomaría","tomaríamos","tomaríais","tomarían")
        add("tomar","subjuntivo","tome","tomes","tome","tomemos","toméis","tomen")

        // ── TRABAJAR (работать) ───────────────────────────────
        add("trabajar","presente","trabajo","trabajas","trabaja","trabajamos","trabajáis","trabajan")
        add("trabajar","preterito","trabajé","trabajaste","trabajó","trabajamos","trabajasteis","trabajaron")
        add("trabajar","imperfecto","trabajaba","trabajabas","trabajaba","trabajábamos","trabajabais","trabajaban")
        add("trabajar","futuro","trabajaré","trabajarás","trabajará","trabajaremos","trabajaréis","trabajarán")
        add("trabajar","condicional","trabajaría","trabajarías","trabajaría","trabajaríamos","trabajaríais","trabajarían")
        add("trabajar","subjuntivo","trabaje","trabajes","trabaje","trabajemos","trabajéis","trabajen")

        // ── USAR (использовать) ───────────────────────────────
        add("usar","presente","uso","usas","usa","usamos","usáis","usan")
        add("usar","preterito","usé","usaste","usó","usamos","usasteis","usaron")
        add("usar","imperfecto","usaba","usabas","usaba","usábamos","usabais","usaban")
        add("usar","futuro","usaré","usarás","usará","usaremos","usaréis","usarán")
        add("usar","condicional","usaría","usarías","usaría","usaríamos","usaríais","usarían")
        add("usar","subjuntivo","use","uses","use","usemos","uséis","usen")

        // ── VENDER (продавать) ────────────────────────────────
        add("vender","presente","vendo","vendes","vende","vendemos","vendéis","venden")
        add("vender","preterito","vendí","vendiste","vendió","vendimos","vendisteis","vendieron")
        add("vender","imperfecto","vendía","vendías","vendía","vendíamos","vendíais","vendían")
        add("vender","futuro","venderé","venderás","venderá","venderemos","venderéis","venderán")
        add("vender","condicional","vendería","venderías","vendería","venderíamos","venderíais","venderían")
        add("vender","subjuntivo","venda","vendas","venda","vendamos","vendáis","vendan")

        // ── VIAJAR (путешествовать) ───────────────────────────
        add("viajar","presente","viajo","viajas","viaja","viajamos","viajáis","viajan")
        add("viajar","preterito","viajé","viajaste","viajó","viajamos","viajasteis","viajaron")
        add("viajar","imperfecto","viajaba","viajabas","viajaba","viajábamos","viajabais","viajaban")
        add("viajar","futuro","viajaré","viajarás","viajará","viajaremos","viajaréis","viajarán")
        add("viajar","condicional","viajaría","viajarías","viajaría","viajaríamos","viajaríais","viajarían")
        add("viajar","subjuntivo","viaje","viajes","viaje","viajemos","viajéis","viajen")

        // ── VOLVER (возвращаться) o→ue ────────────────────────
        add("volver","presente","vuelvo","vuelves","vuelve","volvemos","volvéis","vuelven",true,"o→ue")
        add("volver","preterito","volví","volviste","volvió","volvimos","volvisteis","volvieron")
        add("volver","imperfecto","volvía","volvías","volvía","volvíamos","volvíais","volvían")
        add("volver","futuro","volveré","volverás","volverá","volveremos","volveréis","volverán")
        add("volver","condicional","volvería","volverías","volvería","volveríamos","volveríais","volverían")
        add("volver","subjuntivo","vuelva","vuelvas","vuelva","volvamos","volváis","vuelvan",true)
    }
}
