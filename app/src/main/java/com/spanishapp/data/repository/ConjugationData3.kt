package com.spanishapp.data.repository

import com.spanishapp.data.db.entity.ConjugationEntity

/**
 * Глаголы 77–200 (топ-200 испанских глаголов).
 * Те же 6 времён: presente, preterito, imperfecto, futuro, condicional, subjuntivo.
 */
object ConjugationData3 {

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

        // ════════════════════════════════════════════════════
        // ACEPTAR (принимать) — regular -ar
        // ════════════════════════════════════════════════════
        add("aceptar","presente","acepto","aceptas","acepta","aceptamos","aceptáis","aceptan")
        add("aceptar","preterito","acepté","aceptaste","aceptó","aceptamos","aceptasteis","aceptaron")
        add("aceptar","imperfecto","aceptaba","aceptabas","aceptaba","aceptábamos","aceptabais","aceptaban")
        add("aceptar","futuro","aceptaré","aceptarás","aceptará","aceptaremos","aceptaréis","aceptarán")
        add("aceptar","condicional","aceptaría","aceptarías","aceptaría","aceptaríamos","aceptaríais","aceptarían")
        add("aceptar","subjuntivo","acepte","aceptes","acepte","aceptemos","aceptéis","acepten")

        // ════════════════════════════════════════════════════
        // AMAR (любить) — regular -ar
        // ════════════════════════════════════════════════════
        add("amar","presente","amo","amas","ama","amamos","amáis","aman")
        add("amar","preterito","amé","amaste","amó","amamos","amasteis","amaron")
        add("amar","imperfecto","amaba","amabas","amaba","amábamos","amabais","amaban")
        add("amar","futuro","amaré","amarás","amará","amaremos","amaréis","amarán")
        add("amar","condicional","amaría","amarías","amaría","amaríamos","amaríais","amarían")
        add("amar","subjuntivo","ame","ames","ame","amemos","améis","amen")

        // ════════════════════════════════════════════════════
        // APRENDER (учиться/учить) — regular -er
        // ════════════════════════════════════════════════════
        add("aprender","presente","aprendo","aprendes","aprende","aprendemos","aprendéis","aprenden")
        add("aprender","preterito","aprendí","aprendiste","aprendió","aprendimos","aprendisteis","aprendieron")
        add("aprender","imperfecto","aprendía","aprendías","aprendía","aprendíamos","aprendíais","aprendían")
        add("aprender","futuro","aprenderé","aprenderás","aprenderá","aprenderemos","aprenderéis","aprenderán")
        add("aprender","condicional","aprendería","aprenderías","aprendería","aprenderíamos","aprenderíais","aprenderían")
        add("aprender","subjuntivo","aprenda","aprendas","aprenda","aprendamos","aprendáis","aprendan")

        // ════════════════════════════════════════════════════
        // AÑADIR (добавлять) — regular -ir
        // ════════════════════════════════════════════════════
        add("añadir","presente","añado","añades","añade","añadimos","añadís","añaden")
        add("añadir","preterito","añadí","añadiste","añadió","añadimos","añadisteis","añadieron")
        add("añadir","imperfecto","añadía","añadías","añadía","añadíamos","añadíais","añadían")
        add("añadir","futuro","añadiré","añadirás","añadirá","añadiremos","añadiréis","añadirán")
        add("añadir","condicional","añadiría","añadirías","añadiría","añadiríamos","añadiríais","añadirían")
        add("añadir","subjuntivo","añada","añadas","añada","añadamos","añadáis","añadan")

        // ════════════════════════════════════════════════════
        // BAJAR (спускаться/снижать) — regular -ar
        // ════════════════════════════════════════════════════
        add("bajar","presente","bajo","bajas","baja","bajamos","bajáis","bajan")
        add("bajar","preterito","bajé","bajaste","bajó","bajamos","bajasteis","bajaron")
        add("bajar","imperfecto","bajaba","bajabas","bajaba","bajábamos","bajabais","bajaban")
        add("bajar","futuro","bajaré","bajarás","bajará","bajaremos","bajaréis","bajarán")
        add("bajar","condicional","bajaría","bajarías","bajaría","bajaríamos","bajaríais","bajarían")
        add("bajar","subjuntivo","baje","bajes","baje","bajemos","bajéis","bajen")

        // ════════════════════════════════════════════════════
        // BEBER (пить) — regular -er
        // ════════════════════════════════════════════════════
        add("beber","presente","bebo","bebes","bebe","bebemos","bebéis","beben")
        add("beber","preterito","bebí","bebiste","bebió","bebimos","bebisteis","bebieron")
        add("beber","imperfecto","bebía","bebías","bebía","bebíamos","bebíais","bebían")
        add("beber","futuro","beberé","beberás","beberá","beberemos","beberéis","beberán")
        add("beber","condicional","bebería","beberías","bebería","beberíamos","beberíais","beberían")
        add("beber","subjuntivo","beba","bebas","beba","bebamos","bebáis","beban")

        // ════════════════════════════════════════════════════
        // CAER (падать) — irregular yo: caigo
        // ════════════════════════════════════════════════════
        add("caer","presente","caigo","caes","cae","caemos","caéis","caen",true,"1sg: caigo")
        add("caer","preterito","caí","caíste","cayó","caímos","caísteis","cayeron",true,"3sg/pl: y")
        add("caer","imperfecto","caía","caías","caía","caíamos","caíais","caían")
        add("caer","futuro","caeré","caerás","caerá","caeremos","caeréis","caerán")
        add("caer","condicional","caería","caerías","caería","caeríamos","caeríais","caerían")
        add("caer","subjuntivo","caiga","caigas","caiga","caigamos","caigáis","caigan",true)

        // ════════════════════════════════════════════════════
        // CAMBIAR (менять) — regular -ar
        // ════════════════════════════════════════════════════
        add("cambiar","presente","cambio","cambias","cambia","cambiamos","cambiáis","cambian")
        add("cambiar","preterito","cambié","cambiaste","cambió","cambiamos","cambiasteis","cambiaron")
        add("cambiar","imperfecto","cambiaba","cambiabas","cambiaba","cambiábamos","cambiabais","cambiaban")
        add("cambiar","futuro","cambiaré","cambiarás","cambiará","cambiaremos","cambiaréis","cambiarán")
        add("cambiar","condicional","cambiaría","cambiarías","cambiaría","cambiaríamos","cambiaríais","cambiarían")
        add("cambiar","subjuntivo","cambie","cambies","cambie","cambiemos","cambiéis","cambien")

        // ════════════════════════════════════════════════════
        // CENAR (ужинать) — regular -ar
        // ════════════════════════════════════════════════════
        add("cenar","presente","ceno","cenas","cena","cenamos","cenáis","cenan")
        add("cenar","preterito","cené","cenaste","cenó","cenamos","cenasteis","cenaron")
        add("cenar","imperfecto","cenaba","cenabas","cenaba","cenábamos","cenabais","cenaban")
        add("cenar","futuro","cenaré","cenarás","cenará","cenaremos","cenaréis","cenarán")
        add("cenar","condicional","cenaría","cenarías","cenaría","cenaríamos","cenaríais","cenarían")
        add("cenar","subjuntivo","cene","cenes","cene","cenemos","cenéis","cenen")

        // ════════════════════════════════════════════════════
        // COMPARTIR (делиться) — regular -ir
        // ════════════════════════════════════════════════════
        add("compartir","presente","comparto","compartes","comparte","compartimos","compartís","comparten")
        add("compartir","preterito","compartí","compartiste","compartió","compartimos","compartisteis","compartieron")
        add("compartir","imperfecto","compartía","compartías","compartía","compartíamos","compartíais","compartían")
        add("compartir","futuro","compartiré","compartirás","compartirá","compartiremos","compartiréis","compartirán")
        add("compartir","condicional","compartiría","compartirías","compartiría","compartiríamos","compartiríais","compartirían")
        add("compartir","subjuntivo","comparta","compartas","comparta","compartamos","compartáis","compartan")

        // ════════════════════════════════════════════════════
        // COMENZAR (начинать) — e→ie, c→c ante e
        // ════════════════════════════════════════════════════
        add("comenzar","presente","comienzo","comienzas","comienza","comenzamos","comenzáis","comienzan",true,"e→ie")
        add("comenzar","preterito","comencé","comenzaste","comenzó","comenzamos","comenzasteis","comenzaron",true,"c→c ante e")
        add("comenzar","imperfecto","comenzaba","comenzabas","comenzaba","comenzábamos","comenzabais","comenzaban")
        add("comenzar","futuro","comenzaré","comenzarás","comenzará","comenzaremos","comenzaréis","comenzarán")
        add("comenzar","condicional","comenzaría","comenzarías","comenzaría","comenzaríamos","comenzaríais","comenzarían")
        add("comenzar","subjuntivo","comience","comiences","comience","comencemos","comencéis","comiencen",true)

        // ════════════════════════════════════════════════════
        // COMPRENDER (понимать) — regular -er
        // ════════════════════════════════════════════════════
        add("comprender","presente","comprendo","comprendes","comprende","comprendemos","comprendéis","comprenden")
        add("comprender","preterito","comprendí","comprendiste","comprendió","comprendimos","comprendisteis","comprendieron")
        add("comprender","imperfecto","comprendía","comprendías","comprendía","comprendíamos","comprendíais","comprendían")
        add("comprender","futuro","comprenderé","comprenderás","comprenderá","comprenderemos","comprenderéis","comprenderán")
        add("comprender","condicional","comprendería","comprenderías","comprendería","comprenderíamos","comprenderíais","comprenderían")
        add("comprender","subjuntivo","comprenda","comprendas","comprenda","comprendamos","comprendáis","comprendan")

        // ════════════════════════════════════════════════════
        // CONSEGUIR (добиться/достать) — e→i, gu→g ante a,o
        // ════════════════════════════════════════════════════
        add("conseguir","presente","consigo","consigues","consigue","conseguimos","conseguís","consiguen",true,"e→i")
        add("conseguir","preterito","conseguí","conseguiste","consiguió","conseguimos","conseguisteis","consiguieron",true)
        add("conseguir","imperfecto","conseguía","conseguías","conseguía","conseguíamos","conseguíais","conseguían")
        add("conseguir","futuro","conseguiré","conseguirás","conseguirá","conseguiremos","conseguiréis","conseguirán")
        add("conseguir","condicional","conseguiría","conseguirías","conseguiría","conseguiríamos","conseguiríais","conseguirían")
        add("conseguir","subjuntivo","consiga","consigas","consiga","consigamos","consigáis","consigan",true)

        // ════════════════════════════════════════════════════
        // CONTESTAR (отвечать) — regular -ar
        // ════════════════════════════════════════════════════
        add("contestar","presente","contesto","contestas","contesta","contestamos","contestáis","contestan")
        add("contestar","preterito","contesté","contestaste","contestó","contestamos","contestasteis","contestaron")
        add("contestar","imperfecto","contestaba","contestabas","contestaba","contestábamos","contestabais","contestaban")
        add("contestar","futuro","contestaré","contestarás","contestará","contestaremos","contestaréis","contestarán")
        add("contestar","condicional","contestaría","contestarías","contestaría","contestaríamos","contestaríais","contestarían")
        add("contestar","subjuntivo","conteste","contestes","conteste","contestemos","contestéis","contesten")

        // ════════════════════════════════════════════════════
        // CONTINUAR (продолжать) — regular -ar (u→ú en acento)
        // ════════════════════════════════════════════════════
        add("continuar","presente","continúo","continúas","continúa","continuamos","continuáis","continúan",true,"u→ú tónica")
        add("continuar","preterito","continué","continuaste","continuó","continuamos","continuasteis","continuaron")
        add("continuar","imperfecto","continuaba","continuabas","continuaba","continuábamos","continuabais","continuaban")
        add("continuar","futuro","continuaré","continuarás","continuará","continuaremos","continuaréis","continuarán")
        add("continuar","condicional","continuaría","continuarías","continuaría","continuaríamos","continuaríais","continuarían")
        add("continuar","subjuntivo","continúe","continúes","continúe","continuemos","continuéis","continúen",true)

        // ════════════════════════════════════════════════════
        // CORTAR (резать) — regular -ar
        // ════════════════════════════════════════════════════
        add("cortar","presente","corto","cortas","corta","cortamos","cortáis","cortan")
        add("cortar","preterito","corté","cortaste","cortó","cortamos","cortasteis","cortaron")
        add("cortar","imperfecto","cortaba","cortabas","cortaba","cortábamos","cortabais","cortaban")
        add("cortar","futuro","cortaré","cortarás","cortará","cortaremos","cortaréis","cortarán")
        add("cortar","condicional","cortaría","cortarías","cortaría","cortaríamos","cortaríais","cortarían")
        add("cortar","subjuntivo","corte","cortes","corte","cortemos","cortéis","corten")

        // ════════════════════════════════════════════════════
        // CREAR (создавать) — regular -ar
        // ════════════════════════════════════════════════════
        add("crear","presente","creo","creas","crea","creamos","creáis","crean")
        add("crear","preterito","creé","creaste","creó","creamos","creasteis","crearon")
        add("crear","imperfecto","creaba","creabas","creaba","creábamos","creabais","creaban")
        add("crear","futuro","crearé","crearás","creará","crearemos","crearéis","crearán")
        add("crear","condicional","crearía","crearías","crearía","crearíamos","crearíais","crearían")
        add("crear","subjuntivo","cree","crees","cree","creemos","creéis","creen")

        // ════════════════════════════════════════════════════
        // CRECER (расти) — c→zc ante a,o
        // ════════════════════════════════════════════════════
        add("crecer","presente","crezco","creces","crece","crecemos","crecéis","crecen",true,"1sg: c→zc")
        add("crecer","preterito","crecí","creciste","creció","crecimos","crecisteis","crecieron")
        add("crecer","imperfecto","crecía","crecías","crecía","crecíamos","crecíais","crecían")
        add("crecer","futuro","creceré","crecerás","crecerá","creceremos","creceréis","crecerán")
        add("crecer","condicional","crecería","crecerías","crecería","creceríamos","creceríais","crecerían")
        add("crecer","subjuntivo","crezca","crezcas","crezca","crezcamos","crezcáis","crezcan",true)

        // ════════════════════════════════════════════════════
        // CUMPLIR (исполнять/исполняться) — regular -ir
        // ════════════════════════════════════════════════════
        add("cumplir","presente","cumplo","cumples","cumple","cumplimos","cumplís","cumplen")
        add("cumplir","preterito","cumplí","cumpliste","cumplió","cumplimos","cumplisteis","cumplieron")
        add("cumplir","imperfecto","cumplía","cumplías","cumplía","cumplíamos","cumplíais","cumplían")
        add("cumplir","futuro","cumpliré","cumplirás","cumplirá","cumpliremos","cumpliréis","cumplirán")
        add("cumplir","condicional","cumpliría","cumplirías","cumpliría","cumpliríamos","cumpliríais","cumplirían")
        add("cumplir","subjuntivo","cumpla","cumplas","cumpla","cumplamos","cumpláis","cumplan")

        // ════════════════════════════════════════════════════
        // DECIDIR (решать) — regular -ir
        // ════════════════════════════════════════════════════
        add("decidir","presente","decido","decides","decide","decidimos","decidís","deciden")
        add("decidir","preterito","decidí","decidiste","decidió","decidimos","decidisteis","decidieron")
        add("decidir","imperfecto","decidía","decidías","decidía","decidíamos","decidíais","decidían")
        add("decidir","futuro","decidiré","decidirás","decidirá","decidiremos","decidiréis","decidirán")
        add("decidir","condicional","decidiría","decidirías","decidiría","decidiríamos","decidiríais","decidirían")
        add("decidir","subjuntivo","decida","decidas","decida","decidamos","decidáis","decidan")

        // ════════════════════════════════════════════════════
        // DESCANSAR (отдыхать) — regular -ar
        // ════════════════════════════════════════════════════
        add("descansar","presente","descanso","descansas","descansa","descansamos","descansáis","descansan")
        add("descansar","preterito","descansé","descansaste","descansó","descansamos","descansasteis","descansaron")
        add("descansar","imperfecto","descansaba","descansabas","descansaba","descansábamos","descansabais","descansaban")
        add("descansar","futuro","descansaré","descansarás","descansará","descansaremos","descansaréis","descansarán")
        add("descansar","condicional","descansaría","descansarías","descansaría","descansaríamos","descansaríais","descansarían")
        add("descansar","subjuntivo","descanse","descanses","descanse","descansemos","descanséis","descansen")

        // ════════════════════════════════════════════════════
        // DESCUBRIR (открывать/обнаруживать) — regular -ir (participio: descubierto)
        // ════════════════════════════════════════════════════
        add("descubrir","presente","descubro","descubres","descubre","descubrimos","descubrís","descubren")
        add("descubrir","preterito","descubrí","descubriste","descubrió","descubrimos","descubristeis","descubrieron")
        add("descubrir","imperfecto","descubría","descubrías","descubría","descubríamos","descubríais","descubrían")
        add("descubrir","futuro","descubriré","descubrirás","descubrirá","descubriremos","descubriréis","descubrirán")
        add("descubrir","condicional","descubriría","descubrirías","descubriría","descubriríamos","descubriríais","descubrirían")
        add("descubrir","subjuntivo","descubra","descubras","descubra","descubramos","descubráis","descubran")

        // ════════════════════════════════════════════════════
        // DESEAR (желать) — regular -ar
        // ════════════════════════════════════════════════════
        add("desear","presente","deseo","deseas","desea","deseamos","deseáis","desean")
        add("desear","preterito","deseé","deseaste","deseó","deseamos","deseasteis","desearon")
        add("desear","imperfecto","deseaba","deseabas","deseaba","deseábamos","deseabais","deseaban")
        add("desear","futuro","desearé","desearás","deseará","desearemos","desearéis","desearán")
        add("desear","condicional","desearía","desearías","desearía","desearíamos","desearíais","desearían")
        add("desear","subjuntivo","desee","desees","desee","deseemos","deseéis","deseen")

        // ════════════════════════════════════════════════════
        // DISFRUTAR (наслаждаться) — regular -ar
        // ════════════════════════════════════════════════════
        add("disfrutar","presente","disfruto","disfrutas","disfruta","disfrutamos","disfrutáis","disfrutan")
        add("disfrutar","preterito","disfruté","disfrutaste","disfrutó","disfrutamos","disfrutasteis","disfrutaron")
        add("disfrutar","imperfecto","disfrutaba","disfrutabas","disfrutaba","disfrutábamos","disfrutabais","disfrutaban")
        add("disfrutar","futuro","disfrutaré","disfrutarás","disfrutará","disfrutaremos","disfrutaréis","disfrutarán")
        add("disfrutar","condicional","disfrutaría","disfrutarías","disfrutaría","disfrutaríamos","disfrutaríais","disfrutarían")
        add("disfrutar","subjuntivo","disfrute","disfrutes","disfrute","disfrutemos","disfrutéis","disfruten")

        // ════════════════════════════════════════════════════
        // DURAR (длиться) — regular -ar
        // ════════════════════════════════════════════════════
        add("durar","presente","duro","duras","dura","duramos","duráis","duran")
        add("durar","preterito","duré","duraste","duró","duramos","durasteis","duraron")
        add("durar","imperfecto","duraba","durabas","duraba","durábamos","durabais","duraban")
        add("durar","futuro","duraré","durarás","durará","duraremos","duraréis","durarán")
        add("durar","condicional","duraría","durarías","duraría","duraríamos","duraríais","durarían")
        add("durar","subjuntivo","dure","dures","dure","duremos","duréis","duren")

        // ════════════════════════════════════════════════════
        // ELEGIR (выбирать) — e→i, g→j ante a,o
        // ════════════════════════════════════════════════════
        add("elegir","presente","elijo","eliges","elige","elegimos","elegís","eligen",true,"e→i, 1sg g→j")
        add("elegir","preterito","elegí","elegiste","eligió","elegimos","elegisteis","eligieron",true,"e→i 3sg/pl")
        add("elegir","imperfecto","elegía","elegías","elegía","elegíamos","elegíais","elegían")
        add("elegir","futuro","elegiré","elegirás","elegirá","elegiremos","elegiréis","elegirán")
        add("elegir","condicional","elegiría","elegirías","elegiría","elegiríamos","elegiríais","elegirían")
        add("elegir","subjuntivo","elija","elijas","elija","elijamos","elijáis","elijan",true)

        // ════════════════════════════════════════════════════
        // ENSEÑAR (учить/преподавать) — regular -ar
        // ════════════════════════════════════════════════════
        add("enseñar","presente","enseño","enseñas","enseña","enseñamos","enseñáis","enseñan")
        add("enseñar","preterito","enseñé","enseñaste","enseñó","enseñamos","enseñasteis","enseñaron")
        add("enseñar","imperfecto","enseñaba","enseñabas","enseñaba","enseñábamos","enseñabais","enseñaban")
        add("enseñar","futuro","enseñaré","enseñarás","enseñará","enseñaremos","enseñaréis","enseñarán")
        add("enseñar","condicional","enseñaría","enseñarías","enseñaría","enseñaríamos","enseñaríais","enseñarían")
        add("enseñar","subjuntivo","enseñe","enseñes","enseñe","enseñemos","enseñéis","enseñen")

        // ════════════════════════════════════════════════════
        // ENTRAR (входить) — regular -ar
        // ════════════════════════════════════════════════════
        add("entrar","presente","entro","entras","entra","entramos","entráis","entran")
        add("entrar","preterito","entré","entraste","entró","entramos","entrasteis","entraron")
        add("entrar","imperfecto","entraba","entrabas","entraba","entrábamos","entrabais","entraban")
        add("entrar","futuro","entraré","entrarás","entrará","entraremos","entraréis","entrarán")
        add("entrar","condicional","entraría","entrarías","entraría","entraríamos","entraríais","entrarían")
        add("entrar","subjuntivo","entre","entres","entre","entremos","entréis","entren")

        // ════════════════════════════════════════════════════
        // ENVIAR (отправлять) — i→í tónica
        // ════════════════════════════════════════════════════
        add("enviar","presente","envío","envías","envía","enviamos","enviáis","envían",true,"i→í tónica")
        add("enviar","preterito","envié","enviaste","envió","enviamos","enviasteis","enviaron")
        add("enviar","imperfecto","enviaba","enviabas","enviaba","enviábamos","enviabais","enviaban")
        add("enviar","futuro","enviaré","enviarás","enviará","enviaremos","enviaréis","enviarán")
        add("enviar","condicional","enviaría","enviarías","enviaría","enviaríamos","enviaríais","enviarían")
        add("enviar","subjuntivo","envíe","envíes","envíe","enviemos","enviéis","envíen",true)

        // ════════════════════════════════════════════════════
        // EXISTIR (существовать) — regular -ir
        // ════════════════════════════════════════════════════
        add("existir","presente","existo","existes","existe","existimos","existís","existen")
        add("existir","preterito","existí","exististe","existió","existimos","exististeis","existieron")
        add("existir","imperfecto","existía","existías","existía","existíamos","existíais","existían")
        add("existir","futuro","existiré","existirás","existirá","existiremos","existiréis","existirán")
        add("existir","condicional","existiría","existirías","existiría","existiríamos","existiríais","existirían")
        add("existir","subjuntivo","exista","existas","exista","existamos","existáis","existan")

        // ════════════════════════════════════════════════════
        // EXPLICAR (объяснять) — c→qu ante e
        // ════════════════════════════════════════════════════
        add("explicar","presente","explico","explicas","explica","explicamos","explicáis","explican")
        add("explicar","preterito","expliqué","explicaste","explicó","explicamos","explicasteis","explicaron",true,"c→qu ante e")
        add("explicar","imperfecto","explicaba","explicabas","explicaba","explicábamos","explicabais","explicaban")
        add("explicar","futuro","explicaré","explicarás","explicará","explicaremos","explicaréis","explicarán")
        add("explicar","condicional","explicaría","explicarías","explicaría","explicaríamos","explicaríais","explicarían")
        add("explicar","subjuntivo","explique","expliques","explique","expliquemos","expliquéis","expliquen",true)

        // ════════════════════════════════════════════════════
        // FUNCIONAR (работать/функционировать) — regular -ar
        // ════════════════════════════════════════════════════
        add("funcionar","presente","funciono","funcionas","funciona","funcionamos","funcionáis","funcionan")
        add("funcionar","preterito","funcioné","funcionaste","funcionó","funcionamos","funcionasteis","funcionaron")
        add("funcionar","imperfecto","funcionaba","funcionabas","funcionaba","funcionábamos","funcionabais","funcionaban")
        add("funcionar","futuro","funcionaré","funcionarás","funcionará","funcionaremos","funcionaréis","funcionarán")
        add("funcionar","condicional","funcionaría","funcionarías","funcionaría","funcionaríamos","funcionaríais","funcionarían")
        add("funcionar","subjuntivo","funcione","funciones","funcione","funcionemos","funcionéis","funcionen")

        // ════════════════════════════════════════════════════
        // GASTAR (тратить) — regular -ar
        // ════════════════════════════════════════════════════
        add("gastar","presente","gasto","gastas","gasta","gastamos","gastáis","gastan")
        add("gastar","preterito","gasté","gastaste","gastó","gastamos","gastasteis","gastaron")
        add("gastar","imperfecto","gastaba","gastabas","gastaba","gastábamos","gastabais","gastaban")
        add("gastar","futuro","gastaré","gastarás","gastará","gastaremos","gastaréis","gastarán")
        add("gastar","condicional","gastaría","gastarías","gastaría","gastaríamos","gastaríais","gastarían")
        add("gastar","subjuntivo","gaste","gastes","gaste","gastemos","gastéis","gasten")

        // ════════════════════════════════════════════════════
        // GRITAR (кричать) — regular -ar
        // ════════════════════════════════════════════════════
        add("gritar","presente","grito","gritas","grita","gritamos","gritáis","gritan")
        add("gritar","preterito","grité","gritaste","gritó","gritamos","gritasteis","gritaron")
        add("gritar","imperfecto","gritaba","gritabas","gritaba","gritábamos","gritabais","gritaban")
        add("gritar","futuro","gritaré","gritarás","gritará","gritaremos","gritaréis","gritarán")
        add("gritar","condicional","gritaría","gritarías","gritaría","gritaríamos","gritaríais","gritarían")
        add("gritar","subjuntivo","grite","grites","grite","gritemos","gritéis","griten")

        // ════════════════════════════════════════════════════
        // GUARDAR (хранить/беречь) — regular -ar
        // ════════════════════════════════════════════════════
        add("guardar","presente","guardo","guardas","guarda","guardamos","guardáis","guardan")
        add("guardar","preterito","guardé","guardaste","guardó","guardamos","guardasteis","guardaron")
        add("guardar","imperfecto","guardaba","guardabas","guardaba","guardábamos","guardabais","guardaban")
        add("guardar","futuro","guardaré","guardarás","guardará","guardaremos","guardaréis","guardarán")
        add("guardar","condicional","guardaría","guardarías","guardaría","guardaríamos","guardaríais","guardarían")
        add("guardar","subjuntivo","guarde","guardes","guarde","guardemos","guardéis","guarden")

        // ════════════════════════════════════════════════════
        // IMAGINAR (представлять/воображать) — regular -ar
        // ════════════════════════════════════════════════════
        add("imaginar","presente","imagino","imaginas","imagina","imaginamos","imagináis","imaginan")
        add("imaginar","preterito","imaginé","imaginaste","imaginó","imaginamos","imaginasteis","imaginaron")
        add("imaginar","imperfecto","imaginaba","imaginabas","imaginaba","imaginábamos","imaginabais","imaginaban")
        add("imaginar","futuro","imaginaré","imaginarás","imaginará","imaginaremos","imaginaréis","imaginarán")
        add("imaginar","condicional","imaginaría","imaginarías","imaginaría","imaginaríamos","imaginaríais","imaginarían")
        add("imaginar","subjuntivo","imagine","imagines","imagine","imaginemos","imaginéis","imaginen")

        // ════════════════════════════════════════════════════
        // INTENTAR (пытаться) — regular -ar
        // ════════════════════════════════════════════════════
        add("intentar","presente","intento","intentas","intenta","intentamos","intentáis","intentan")
        add("intentar","preterito","intenté","intentaste","intentó","intentamos","intentasteis","intentaron")
        add("intentar","imperfecto","intentaba","intentabas","intentaba","intentábamos","intentabais","intentaban")
        add("intentar","futuro","intentaré","intentarás","intentará","intentaremos","intentaréis","intentarán")
        add("intentar","condicional","intentaría","intentarías","intentaría","intentaríamos","intentaríais","intentarían")
        add("intentar","subjuntivo","intente","intentes","intente","intentemos","intentéis","intenten")

        // ════════════════════════════════════════════════════
        // INVITAR (приглашать) — regular -ar
        // ════════════════════════════════════════════════════
        add("invitar","presente","invito","invitas","invita","invitamos","invitáis","invitan")
        add("invitar","preterito","invité","invitaste","invitó","invitamos","invitasteis","invitaron")
        add("invitar","imperfecto","invitaba","invitabas","invitaba","invitábamos","invitabais","invitaban")
        add("invitar","futuro","invitaré","invitarás","invitará","invitaremos","invitaréis","invitarán")
        add("invitar","condicional","invitaría","invitarías","invitaría","invitaríamos","invitaríais","invitarían")
        add("invitar","subjuntivo","invite","invites","invite","invitemos","invitéis","inviten")

        // ════════════════════════════════════════════════════
        // LAVAR (мыть) — regular -ar
        // ════════════════════════════════════════════════════
        add("lavar","presente","lavo","lavas","lava","lavamos","laváis","lavan")
        add("lavar","preterito","lavé","lavaste","lavó","lavamos","lavasteis","lavaron")
        add("lavar","imperfecto","lavaba","lavabas","lavaba","lavábamos","lavabais","lavaban")
        add("lavar","futuro","lavaré","lavarás","lavará","lavaremos","lavaréis","lavarán")
        add("lavar","condicional","lavaría","lavarías","lavaría","lavaríamos","lavaríais","lavarían")
        add("lavar","subjuntivo","lave","laves","lave","lavemos","lavéis","laven")

        // ════════════════════════════════════════════════════
        // LIMPIAR (убирать/чистить) — regular -ar
        // ════════════════════════════════════════════════════
        add("limpiar","presente","limpio","limpias","limpia","limpiamos","limpiáis","limpian")
        add("limpiar","preterito","limpié","limpiaste","limpió","limpiamos","limpiasteis","limpiaron")
        add("limpiar","imperfecto","limpiaba","limpiabas","limpiaba","limpiábamos","limpiabais","limpiaban")
        add("limpiar","futuro","limpiaré","limpiarás","limpiará","limpiaremos","limpiaréis","limpiarán")
        add("limpiar","condicional","limpiaría","limpiarías","limpiaría","limpiaríamos","limpiaríais","limpiarían")
        add("limpiar","subjuntivo","limpie","limpies","limpie","limpiemos","limpiéis","limpien")

        // ════════════════════════════════════════════════════
        // LLORAR (плакать) — regular -ar
        // ════════════════════════════════════════════════════
        add("llorar","presente","lloro","lloras","llora","lloramos","lloráis","lloran")
        add("llorar","preterito","lloré","lloraste","lloró","lloramos","llorasteis","lloraron")
        add("llorar","imperfecto","lloraba","llorabas","lloraba","llorábamos","llorabais","lloraban")
        add("llorar","futuro","lloraré","llorarás","llorará","lloraremos","lloraréis","llorarán")
        add("llorar","condicional","lloraría","llorarías","lloraría","lloraríamos","lloraríais","llorarían")
        add("llorar","subjuntivo","llore","llores","llore","lloremos","lloréis","lloren")

        // ════════════════════════════════════════════════════
        // LOGRAR (достигать/удаваться) — regular -ar
        // ════════════════════════════════════════════════════
        add("lograr","presente","logro","logras","logra","logramos","lográis","logran")
        add("lograr","preterito","logré","lograste","logró","logramos","lograsteis","lograron")
        add("lograr","imperfecto","lograba","lograbas","lograba","lográbamos","lograbais","lograban")
        add("lograr","futuro","lograré","lograrás","logrará","lograremos","lograréis","lograrán")
        add("lograr","condicional","lograría","lograrías","lograría","lograríamos","lograríais","lograrían")
        add("lograr","subjuntivo","logre","logres","logre","logremos","logréis","logren")

        // ════════════════════════════════════════════════════
        // MANTENER (поддерживать) — como tener
        // ════════════════════════════════════════════════════
        add("mantener","presente","mantengo","mantienes","mantiene","mantenemos","mantenéis","mantienen",true,"e→ie, 1sg -go")
        add("mantener","preterito","mantuve","mantuviste","mantuvo","mantuvimos","mantuvisteis","mantuvieron",true)
        add("mantener","imperfecto","mantenía","mantenías","mantenía","manteníamos","manteníais","mantenían")
        add("mantener","futuro","mantendré","mantendrás","mantendrá","mantendremos","mantendréis","mantendrán",true,"stem: mantendr-")
        add("mantener","condicional","mantendría","mantendrías","mantendría","mantendríamos","mantendríais","mantendrían",true)
        add("mantener","subjuntivo","mantenga","mantengas","mantenga","mantengamos","mantengáis","mantengan",true)

        // ════════════════════════════════════════════════════
        // MEJORAR (улучшать) — regular -ar
        // ════════════════════════════════════════════════════
        add("mejorar","presente","mejoro","mejoras","mejora","mejoramos","mejoráis","mejoran")
        add("mejorar","preterito","mejoré","mejoraste","mejoró","mejoramos","mejorasteis","mejoraron")
        add("mejorar","imperfecto","mejoraba","mejorabas","mejoraba","mejorábamos","mejorabais","mejoraban")
        add("mejorar","futuro","mejoraré","mejorarás","mejorará","mejoraremos","mejoraréis","mejorarán")
        add("mejorar","condicional","mejoraría","mejorarías","mejoraría","mejoraríamos","mejoraríais","mejorarían")
        add("mejorar","subjuntivo","mejore","mejores","mejore","mejoremos","mejoréis","mejoren")

        // ════════════════════════════════════════════════════
        // NACER (рождаться) — c→zc ante a,o
        // ════════════════════════════════════════════════════
        add("nacer","presente","nazco","naces","nace","nacemos","nacéis","nacen",true,"1sg: nazco")
        add("nacer","preterito","nací","naciste","nació","nacimos","nacisteis","nacieron")
        add("nacer","imperfecto","nacía","nacías","nacía","nacíamos","nacíais","nacían")
        add("nacer","futuro","naceré","nacerás","nacerá","naceremos","naceréis","nacerán")
        add("nacer","condicional","nacería","nacerías","nacería","naceríamos","naceríais","nacerían")
        add("nacer","subjuntivo","nazca","nazcas","nazca","nazcamos","nazcáis","nazcan",true)

        // ════════════════════════════════════════════════════
        // NADAR (плавать) — regular -ar
        // ════════════════════════════════════════════════════
        add("nadar","presente","nado","nadas","nada","nadamos","nadáis","nadan")
        add("nadar","preterito","nadé","nadaste","nadó","nadamos","nadasteis","nadaron")
        add("nadar","imperfecto","nadaba","nadabas","nadaba","nadábamos","nadabais","nadaban")
        add("nadar","futuro","nadaré","nadarás","nadará","nadaremos","nadaréis","nadarán")
        add("nadar","condicional","nadaría","nadarías","nadaría","nadaríamos","nadaríais","nadarían")
        add("nadar","subjuntivo","nade","nades","nade","nademos","nadéis","naden")

        // ════════════════════════════════════════════════════
        // OBTENER (получать) — como tener
        // ════════════════════════════════════════════════════
        add("obtener","presente","obtengo","obtienes","obtiene","obtenemos","obtenéis","obtienen",true)
        add("obtener","preterito","obtuve","obtuviste","obtuvo","obtuvimos","obtuvisteis","obtuvieron",true)
        add("obtener","imperfecto","obtenía","obtenías","obtenía","obteníamos","obteníais","obtenían")
        add("obtener","futuro","obtendré","obtendrás","obtendrá","obtendremos","obtendréis","obtendrán",true)
        add("obtener","condicional","obtendría","obtendrías","obtendría","obtendríamos","obtendríais","obtendrían",true)
        add("obtener","subjuntivo","obtenga","obtengas","obtenga","obtengamos","obtengáis","obtengan",true)

        // ════════════════════════════════════════════════════
        // OFRECER (предлагать) — c→zc ante a,o
        // ════════════════════════════════════════════════════
        add("ofrecer","presente","ofrezco","ofreces","ofrece","ofrecemos","ofrecéis","ofrecen",true,"1sg: ofrezco")
        add("ofrecer","preterito","ofrecí","ofreciste","ofreció","ofrecimos","ofrecisteis","ofrecieron")
        add("ofrecer","imperfecto","ofrecía","ofrecías","ofrecía","ofrecíamos","ofrecíais","ofrecían")
        add("ofrecer","futuro","ofreceré","ofrecerás","ofrecerá","ofreceremos","ofreceréis","ofrecerán")
        add("ofrecer","condicional","ofrecería","ofrecerías","ofrecería","ofreceríamos","ofreceríais","ofrecerían")
        add("ofrecer","subjuntivo","ofrezca","ofrezcas","ofrezca","ofrezcamos","ofrezcáis","ofrezcan",true)

        // ════════════════════════════════════════════════════
        // OPINAR (считать/иметь мнение) — regular -ar
        // ════════════════════════════════════════════════════
        add("opinar","presente","opino","opinas","opina","opinamos","opináis","opinan")
        add("opinar","preterito","opiné","opinaste","opinó","opinamos","opinasteis","opinaron")
        add("opinar","imperfecto","opinaba","opinabas","opinaba","opinábamos","opinabais","opinaban")
        add("opinar","futuro","opinaré","opinarás","opinará","opinaremos","opinaréis","opinarán")
        add("opinar","condicional","opinaría","opinarías","opinaría","opinaríamos","opinaríais","opinarían")
        add("opinar","subjuntivo","opine","opines","opine","opinemos","opinéis","opinen")

        // ════════════════════════════════════════════════════
        // ORGANIZAR (организовывать) — z→c ante e
        // ════════════════════════════════════════════════════
        add("organizar","presente","organizo","organizas","organiza","organizamos","organizáis","organizan")
        add("organizar","preterito","organicé","organizaste","organizó","organizamos","organizasteis","organizaron",true,"z→c ante e")
        add("organizar","imperfecto","organizaba","organizabas","organizaba","organizábamos","organizabais","organizaban")
        add("organizar","futuro","organizaré","organizarás","organizará","organizaremos","organizaréis","organizarán")
        add("organizar","condicional","organizaría","organizarías","organizaría","organizaríamos","organizaríais","organizarían")
        add("organizar","subjuntivo","organice","organices","organice","organicemos","organicéis","organicen",true)

        // ════════════════════════════════════════════════════
        // PARTIR (уезжать/делить) — regular -ir
        // ════════════════════════════════════════════════════
        add("partir","presente","parto","partes","parte","partimos","partís","parten")
        add("partir","preterito","partí","partiste","partió","partimos","partisteis","partieron")
        add("partir","imperfecto","partía","partías","partía","partíamos","partíais","partían")
        add("partir","futuro","partiré","partirás","partirá","partiremos","partiréis","partirán")
        add("partir","condicional","partiría","partirías","partiría","partiríamos","partiríais","partirían")
        add("partir","subjuntivo","parta","partas","parta","partamos","partáis","partan")

        // ════════════════════════════════════════════════════
        // PERMITIR (позволять) — regular -ir
        // ════════════════════════════════════════════════════
        add("permitir","presente","permito","permites","permite","permitimos","permitís","permiten")
        add("permitir","preterito","permití","permitiste","permitió","permitimos","permitisteis","permitieron")
        add("permitir","imperfecto","permitía","permitías","permitía","permitíamos","permitíais","permitían")
        add("permitir","futuro","permitiré","permitirás","permitirá","permitiremos","permitiréis","permitirán")
        add("permitir","condicional","permitiría","permitirías","permitiría","permitiríamos","permitiríais","permitirían")
        add("permitir","subjuntivo","permita","permitas","permita","permitamos","permitáis","permitan")

        // ════════════════════════════════════════════════════
        // PRACTICAR (практиковать) — c→qu ante e
        // ════════════════════════════════════════════════════
        add("practicar","presente","practico","practicas","practica","practicamos","practicáis","practican")
        add("practicar","preterito","practiqué","practicaste","practicó","practicamos","practicasteis","practicaron",true,"c→qu ante e")
        add("practicar","imperfecto","practicaba","practicabas","practicaba","practicábamos","practicabais","practicaban")
        add("practicar","futuro","practicaré","practicarás","practicará","practicaremos","practicaréis","practicarán")
        add("practicar","condicional","practicaría","practicarías","practicaría","practicaríamos","practicaríais","practicarían")
        add("practicar","subjuntivo","practique","practiques","practique","practiquemos","practiquéis","practiquen",true)

        // ════════════════════════════════════════════════════
        // PREPARAR (готовить) — regular -ar
        // ════════════════════════════════════════════════════
        add("preparar","presente","preparo","preparas","prepara","preparamos","preparáis","preparan")
        add("preparar","preterito","preparé","preparaste","preparó","preparamos","preparasteis","prepararon")
        add("preparar","imperfecto","preparaba","preparabas","preparaba","preparábamos","preparabais","preparaban")
        add("preparar","futuro","prepararé","prepararás","preparará","prepararemos","prepararéis","prepararán")
        add("preparar","condicional","prepararía","prepararías","prepararía","prepararíamos","prepararíais","prepararían")
        add("preparar","subjuntivo","prepare","prepares","prepare","preparemos","preparéis","preparen")

        // ════════════════════════════════════════════════════
        // PREGUNTAR (спрашивать) — regular -ar
        // ════════════════════════════════════════════════════
        add("preguntar","presente","pregunto","preguntas","pregunta","preguntamos","preguntáis","preguntan")
        add("preguntar","preterito","pregunté","preguntaste","preguntó","preguntamos","preguntasteis","preguntaron")
        add("preguntar","imperfecto","preguntaba","preguntabas","preguntaba","preguntábamos","preguntabais","preguntaban")
        add("preguntar","futuro","preguntaré","preguntarás","preguntará","preguntaremos","preguntaréis","preguntarán")
        add("preguntar","condicional","preguntaría","preguntarías","preguntaría","preguntaríamos","preguntaríais","preguntarían")
        add("preguntar","subjuntivo","pregunte","preguntes","pregunte","preguntemos","preguntéis","pregunten")

        // ════════════════════════════════════════════════════
        // PROBAR (пробовать/доказывать) — o→ue
        // ════════════════════════════════════════════════════
        add("probar","presente","pruebo","pruebas","prueba","probamos","probáis","prueban",true,"o→ue")
        add("probar","preterito","probé","probaste","probó","probamos","probasteis","probaron")
        add("probar","imperfecto","probaba","probabas","probaba","probábamos","probabais","probaban")
        add("probar","futuro","probaré","probarás","probará","probaremos","probaréis","probarán")
        add("probar","condicional","probaría","probarías","probaría","probaríamos","probaríais","probarían")
        add("probar","subjuntivo","pruebe","pruebes","pruebe","probemos","probéis","prueben",true)

        // ════════════════════════════════════════════════════
        // PRODUCIR (производить) — c→zc, preterito irregular
        // ════════════════════════════════════════════════════
        add("producir","presente","produzco","produces","produce","producimos","producís","producen",true,"1sg: produzco")
        add("producir","preterito","produje","produjiste","produjo","produjimos","produjisteis","produjeron",true,"stem: produj-")
        add("producir","imperfecto","producía","producías","producía","producíamos","producíais","producían")
        add("producir","futuro","produciré","producirás","producirá","produciremos","produciréis","producirán")
        add("producir","condicional","produciría","producirías","produciría","produciríamos","produciríais","producirían")
        add("producir","subjuntivo","produzca","produzcas","produzca","produzcamos","produzcáis","produzcan",true)

        // ════════════════════════════════════════════════════
        // QUEDAR (оставаться) — regular -ar
        // ════════════════════════════════════════════════════
        add("quedar","presente","quedo","quedas","queda","quedamos","quedáis","quedan")
        add("quedar","preterito","quedé","quedaste","quedó","quedamos","quedasteis","quedaron")
        add("quedar","imperfecto","quedaba","quedabas","quedaba","quedábamos","quedabais","quedaban")
        add("quedar","futuro","quedaré","quedarás","quedará","quedaremos","quedaréis","quedarán")
        add("quedar","condicional","quedaría","quedarías","quedaría","quedaríamos","quedaríais","quedarían")
        add("quedar","subjuntivo","quede","quedes","quede","quedemos","quedéis","queden")

        // ════════════════════════════════════════════════════
        // RECIBIR (получать) — regular -ir
        // ════════════════════════════════════════════════════
        add("recibir","presente","recibo","recibes","recibe","recibimos","recibís","reciben")
        add("recibir","preterito","recibí","recibiste","recibió","recibimos","recibisteis","recibieron")
        add("recibir","imperfecto","recibía","recibías","recibía","recibíamos","recibíais","recibían")
        add("recibir","futuro","recibiré","recibirás","recibirá","recibiremos","recibiréis","recibirán")
        add("recibir","condicional","recibiría","recibirías","recibiría","recibiríamos","recibiríais","recibirían")
        add("recibir","subjuntivo","reciba","recibas","reciba","recibamos","recibáis","reciban")

        // ════════════════════════════════════════════════════
        // REPETIR (повторять) — e→i
        // ════════════════════════════════════════════════════
        add("repetir","presente","repito","repites","repite","repetimos","repetís","repiten",true,"e→i")
        add("repetir","preterito","repetí","repetiste","repitió","repetimos","repetisteis","repitieron",true,"e→i 3sg/pl")
        add("repetir","imperfecto","repetía","repetías","repetía","repetíamos","repetíais","repetían")
        add("repetir","futuro","repetiré","repetirás","repetirá","repetiremos","repetiréis","repetirán")
        add("repetir","condicional","repetiría","repetirías","repetiría","repetiríamos","repetiríais","repetirían")
        add("repetir","subjuntivo","repita","repitas","repita","repitamos","repitáis","repitan",true)

        // ════════════════════════════════════════════════════
        // RESOLVER (решать) — o→ue, participio: resuelto
        // ════════════════════════════════════════════════════
        add("resolver","presente","resuelvo","resuelves","resuelve","resolvemos","resolvéis","resuelven",true,"o→ue")
        add("resolver","preterito","resolví","resolviste","resolvió","resolvimos","resolvisteis","resolvieron")
        add("resolver","imperfecto","resolvía","resolvías","resolvía","resolvíamos","resolvíais","resolvían")
        add("resolver","futuro","resolveré","resolverás","resolverá","resolveremos","resolveréis","resolverán")
        add("resolver","condicional","resolvería","resolverías","resolvería","resolveríamos","resolveríais","resolverían")
        add("resolver","subjuntivo","resuelva","resuelvas","resuelva","resolvamos","resolváis","resuelvan",true)

        // ════════════════════════════════════════════════════
        // RESPONDER (отвечать) — regular -er
        // ════════════════════════════════════════════════════
        add("responder","presente","respondo","respondes","responde","respondemos","respondéis","responden")
        add("responder","preterito","respondí","respondiste","respondió","respondimos","respondisteis","respondieron")
        add("responder","imperfecto","respondía","respondías","respondía","respondíamos","respondíais","respondían")
        add("responder","futuro","responderé","responderás","responderá","responderemos","responderéis","responderán")
        add("responder","condicional","respondería","responderías","respondería","responderíamos","responderíais","responderían")
        add("responder","subjuntivo","responda","respondas","responda","respondamos","respondáis","respondan")

        // ════════════════════════════════════════════════════
        // ROMPER (ломать) — regular -er, participio: roto
        // ════════════════════════════════════════════════════
        add("romper","presente","rompo","rompes","rompe","rompemos","rompéis","rompen")
        add("romper","preterito","rompí","rompiste","rompió","rompimos","rompisteis","rompieron")
        add("romper","imperfecto","rompía","rompías","rompía","rompíamos","rompíais","rompían")
        add("romper","futuro","romperé","romperás","romperá","romperemos","romperéis","romperán")
        add("romper","condicional","rompería","romperías","rompería","romperíamos","romperíais","romperían")
        add("romper","subjuntivo","rompa","rompas","rompa","rompamos","rompáis","rompan")

        // ════════════════════════════════════════════════════
        // SACAR (вынимать/брать) — c→qu ante e
        // ════════════════════════════════════════════════════
        add("sacar","presente","saco","sacas","saca","sacamos","sacáis","sacan")
        add("sacar","preterito","saqué","sacaste","sacó","sacamos","sacasteis","sacaron",true,"c→qu ante e")
        add("sacar","imperfecto","sacaba","sacabas","sacaba","sacábamos","sacabais","sacaban")
        add("sacar","futuro","sacaré","sacarás","sacará","sacaremos","sacaréis","sacarán")
        add("sacar","condicional","sacaría","sacarías","sacaría","sacaríamos","sacaríais","sacarían")
        add("sacar","subjuntivo","saque","saques","saque","saquemos","saquéis","saquen",true)

        // ════════════════════════════════════════════════════
        // SERVIR (служить/подавать) — e→i
        // ════════════════════════════════════════════════════
        add("servir","presente","sirvo","sirves","sirve","servimos","servís","sirven",true,"e→i")
        add("servir","preterito","serví","serviste","sirvió","servimos","servisteis","sirvieron",true,"e→i 3sg/pl")
        add("servir","imperfecto","servía","servías","servía","servíamos","servíais","servían")
        add("servir","futuro","serviré","servirás","servirá","serviremos","serviréis","servirán")
        add("servir","condicional","serviría","servirías","serviría","serviríamos","serviríais","servirían")
        add("servir","subjuntivo","sirva","sirvas","sirva","sirvamos","sirváis","sirvan",true)

        // ════════════════════════════════════════════════════
        // SUPONER (предполагать) — como poner
        // ════════════════════════════════════════════════════
        add("suponer","presente","supongo","supones","supone","suponemos","suponéis","suponen",true,"1sg: supongo")
        add("suponer","preterito","supuse","supusiste","supuso","supusimos","supusisteis","supusieron",true)
        add("suponer","imperfecto","suponía","suponías","suponía","suponíamos","suponíais","suponían")
        add("suponer","futuro","supondré","supondrás","supondrá","supondremos","supondréis","supondrán",true,"stem: supondr-")
        add("suponer","condicional","supondría","supondrías","supondría","supondríamos","supondríais","supondrían",true)
        add("suponer","subjuntivo","suponga","supongas","suponga","supongamos","supongáis","supongan",true)

        // ════════════════════════════════════════════════════
        // TRAER (приносить) — 1sg: traigo, preterito irregular
        // ════════════════════════════════════════════════════
        add("traer","presente","traigo","traes","trae","traemos","traéis","traen",true,"1sg: traigo")
        add("traer","preterito","traje","trajiste","trajo","trajimos","trajisteis","trajeron",true,"stem: traj-")
        add("traer","imperfecto","traía","traías","traía","traíamos","traíais","traían")
        add("traer","futuro","traeré","traerás","traerá","traeremos","traeréis","traerán")
        add("traer","condicional","traería","traerías","traería","traeríamos","traeríais","traerían")
        add("traer","subjuntivo","traiga","traigas","traiga","traigamos","traigáis","traigan",true)

        // ════════════════════════════════════════════════════
        // TRADUCIR (переводить) — c→zc, preterito: traduj-
        // ════════════════════════════════════════════════════
        add("traducir","presente","traduzco","traduces","traduce","traducimos","traducís","traducen",true,"1sg: traduzco")
        add("traducir","preterito","traduje","tradujiste","tradujo","tradujimos","tradujisteis","tradujeron",true,"stem: traduj-")
        add("traducir","imperfecto","traducía","traducías","traducía","traducíamos","traducíais","traducían")
        add("traducir","futuro","traduciré","traducirás","traducirá","traduciremos","traduciréis","traducirán")
        add("traducir","condicional","traduciría","traducirías","traduciría","traduciríamos","traduciríais","traducirían")
        add("traducir","subjuntivo","traduzca","traduzcas","traduzca","traduzcamos","traduzcáis","traduzcan",true)

        // ════════════════════════════════════════════════════
        // TRATAR (обращаться/лечить/пытаться) — regular -ar
        // ════════════════════════════════════════════════════
        add("tratar","presente","trato","tratas","trata","tratamos","tratáis","tratan")
        add("tratar","preterito","traté","trataste","trató","tratamos","tratasteis","trataron")
        add("tratar","imperfecto","trataba","tratabas","trataba","tratábamos","tratabais","trataban")
        add("tratar","futuro","trataré","tratarás","tratará","trataremos","trataréis","tratarán")
        add("tratar","condicional","trataría","tratarías","trataría","trataríamos","trataríais","tratarían")
        add("tratar","subjuntivo","trate","trates","trate","tratemos","tratéis","traten")

        // ════════════════════════════════════════════════════
        // UNIR (объединять) — regular -ir
        // ════════════════════════════════════════════════════
        add("unir","presente","uno","unes","une","unimos","unís","unen")
        add("unir","preterito","uní","uniste","unió","unimos","unisteis","unieron")
        add("unir","imperfecto","unía","unías","unía","uníamos","uníais","unían")
        add("unir","futuro","uniré","unirás","unirá","uniremos","uniréis","unirán")
        add("unir","condicional","uniría","unirías","uniría","uniríamos","uniríais","unirían")
        add("unir","subjuntivo","una","unas","una","unamos","unáis","unan")

        // ════════════════════════════════════════════════════
        // UTILIZAR (использовать) — z→c ante e
        // ════════════════════════════════════════════════════
        add("utilizar","presente","utilizo","utilizas","utiliza","utilizamos","utilizáis","utilizan")
        add("utilizar","preterito","utilicé","utilizaste","utilizó","utilizamos","utilizasteis","utilizaron",true,"z→c ante e")
        add("utilizar","imperfecto","utilizaba","utilizabas","utilizaba","utilizábamos","utilizabais","utilizaban")
        add("utilizar","futuro","utilizaré","utilizarás","utilizará","utilizaremos","utilizaréis","utilizarán")
        add("utilizar","condicional","utilizaría","utilizarías","utilizaría","utilizaríamos","utilizaríais","utilizarían")
        add("utilizar","subjuntivo","utilice","utilices","utilice","utilicemos","utilicéis","utilicen",true)

        // ════════════════════════════════════════════════════
        // VALORAR (ценить/оценивать) — regular -ar
        // ════════════════════════════════════════════════════
        add("valorar","presente","valoro","valoras","valora","valoramos","valoráis","valoran")
        add("valorar","preterito","valoré","valoraste","valoró","valoramos","valorasteis","valoraron")
        add("valorar","imperfecto","valoraba","valorabas","valoraba","valorábamos","valorabais","valoraban")
        add("valorar","futuro","valoraré","valorarás","valorará","valoraremos","valoraréis","valorarán")
        add("valorar","condicional","valoraría","valorarías","valoraría","valoraríamos","valoraríais","valorarían")
        add("valorar","subjuntivo","valore","valores","valore","valoremos","valoréis","valoren")

        // ════════════════════════════════════════════════════
        // VISITAR (посещать) — regular -ar
        // ════════════════════════════════════════════════════
        add("visitar","presente","visito","visitas","visita","visitamos","visitáis","visitan")
        add("visitar","preterito","visité","visitaste","visitó","visitamos","visitasteis","visitaron")
        add("visitar","imperfecto","visitaba","visitabas","visitaba","visitábamos","visitabais","visitaban")
        add("visitar","futuro","visitaré","visitarás","visitará","visitaremos","visitaréis","visitarán")
        add("visitar","condicional","visitaría","visitarías","visitaría","visitaríamos","visitaríais","visitarían")
        add("visitar","subjuntivo","visite","visites","visite","visitemos","visitéis","visiten")

        // ════════════════════════════════════════════════════
        // VOLAR (летать/лететь) — o→ue
        // ════════════════════════════════════════════════════
        add("volar","presente","vuelo","vuelas","vuela","volamos","voláis","vuelan",true,"o→ue")
        add("volar","preterito","volé","volaste","voló","volamos","volasteis","volaron")
        add("volar","imperfecto","volaba","volabas","volaba","volábamos","volabais","volaban")
        add("volar","futuro","volaré","volarás","volará","volaremos","volaréis","volarán")
        add("volar","condicional","volaría","volarías","volaría","volaríamos","volaríais","volarían")
        add("volar","subjuntivo","vuele","vueles","vuele","volemos","voléis","vuelen",true)

        // ════════════════════════════════════════════════════
        // PROTEGER (защищать) — g→j ante a,o
        // ════════════════════════════════════════════════════
        add("proteger","presente","protejo","proteges","protege","protegemos","protegéis","protegen",true,"1sg: g→j")
        add("proteger","preterito","protegí","protegiste","protegió","protegimos","protegisteis","protegieron")
        add("proteger","imperfecto","protegía","protegías","protegía","protegíamos","protegíais","protegían")
        add("proteger","futuro","protegeré","protegerás","protegerá","protegeremos","protegeréis","protegerán")
        add("proteger","condicional","protegería","protegerías","protegería","protegeríamos","protegeríais","protegerían")
        add("proteger","subjuntivo","proteja","protejas","proteja","protejamos","protejáis","protejan",true)

        // ════════════════════════════════════════════════════
        // REÍR (смеяться) — e→í, irregular
        // ════════════════════════════════════════════════════
        add("reír","presente","río","ríes","ríe","reímos","reís","ríen",true,"e→í tónica")
        add("reír","preterito","reí","reíste","rió","reímos","reísteis","rieron",true)
        add("reír","imperfecto","reía","reías","reía","reíamos","reíais","reían")
        add("reír","futuro","reiré","reirás","reirá","reiremos","reiréis","reirán")
        add("reír","condicional","reiría","reirías","reiría","reiríamos","reiríais","reirían")
        add("reír","subjuntivo","ría","rías","ría","riamos","riáis","rían",true)

        // ════════════════════════════════════════════════════
        // SONREÍR (улыбаться) — como reír
        // ════════════════════════════════════════════════════
        add("sonreír","presente","sonrío","sonríes","sonríe","sonreímos","sonreís","sonríen",true)
        add("sonreír","preterito","sonreí","sonreíste","sonrió","sonreímos","sonreísteis","sonrieron",true)
        add("sonreír","imperfecto","sonreía","sonreías","sonreía","sonreíamos","sonreíais","sonreían")
        add("sonreír","futuro","sonreiré","sonreirás","sonreirá","sonreiremos","sonreiréis","sonreirán")
        add("sonreír","condicional","sonreiría","sonreirías","sonreiría","sonreiríamos","sonreiríais","sonreirían")
        add("sonreír","subjuntivo","sonría","sonrías","sonría","sonriamos","sonriáis","sonrían",true)

        // ════════════════════════════════════════════════════
        // DESAYUNAR (завтракать) — regular -ar
        // ════════════════════════════════════════════════════
        add("desayunar","presente","desayuno","desayunas","desayuna","desayunamos","desayunáis","desayunan")
        add("desayunar","preterito","desayuné","desayunaste","desayunó","desayunamos","desayunasteis","desayunaron")
        add("desayunar","imperfecto","desayunaba","desayunabas","desayunaba","desayunábamos","desayunabais","desayunaban")
        add("desayunar","futuro","desayunaré","desayunarás","desayunará","desayunaremos","desayunaréis","desayunarán")
        add("desayunar","condicional","desayunaría","desayunarías","desayunaría","desayunaríamos","desayunaríais","desayunarían")
        add("desayunar","subjuntivo","desayune","desayunes","desayune","desayunemos","desayunéis","desayunen")

        // ════════════════════════════════════════════════════
        // LLENAR (наполнять) — regular -ar
        // ════════════════════════════════════════════════════
        add("llenar","presente","lleno","llenas","llena","llenamos","llenáis","llenan")
        add("llenar","preterito","llené","llenaste","llenó","llenamos","llenasteis","llenaron")
        add("llenar","imperfecto","llenaba","llenabas","llenaba","llenábamos","llenabais","llenaban")
        add("llenar","futuro","llenaré","llenarás","llenará","llenaremos","llenaréis","llenarán")
        add("llenar","condicional","llenaría","llenarías","llenaría","llenaríamos","llenaríais","llenarían")
        add("llenar","subjuntivo","llene","llenes","llene","llenemos","llenéis","llenen")

        // ════════════════════════════════════════════════════
        // COMPRENDER (понимать) — уже добавлен выше — пропускаем
        // ════════════════════════════════════════════════════

        // ════════════════════════════════════════════════════
        // PRESENTAR (представлять) — regular -ar
        // ════════════════════════════════════════════════════
        add("presentar","presente","presento","presentas","presenta","presentamos","presentáis","presentan")
        add("presentar","preterito","presenté","presentaste","presentó","presentamos","presentasteis","presentaron")
        add("presentar","imperfecto","presentaba","presentabas","presentaba","presentábamos","presentabais","presentaban")
        add("presentar","futuro","presentaré","presentarás","presentará","presentaremos","presentaréis","presentarán")
        add("presentar","condicional","presentaría","presentarías","presentaría","presentaríamos","presentaríais","presentarían")
        add("presentar","subjuntivo","presente","presentes","presente","presentemos","presentéis","presenten")

        // ════════════════════════════════════════════════════
        // PUBLICAR (публиковать) — c→qu ante e
        // ════════════════════════════════════════════════════
        add("publicar","presente","publico","publicas","publica","publicamos","publicáis","publican")
        add("publicar","preterito","publiqué","publicaste","publicó","publicamos","publicasteis","publicaron",true,"c→qu ante e")
        add("publicar","imperfecto","publicaba","publicabas","publicaba","publicábamos","publicabais","publicaban")
        add("publicar","futuro","publicaré","publicarás","publicará","publicaremos","publicaréis","publicarán")
        add("publicar","condicional","publicaría","publicarías","publicaría","publicaríamos","publicaríais","publicarían")
        add("publicar","subjuntivo","publique","publiques","publique","publiquemos","publiquéis","publiquen",true)

        // ════════════════════════════════════════════════════
        // DISCUTIR (обсуждать/спорить) — regular -ir
        // ════════════════════════════════════════════════════
        add("discutir","presente","discuto","discutes","discute","discutimos","discutís","discuten")
        add("discutir","preterito","discutí","discutiste","discutió","discutimos","discutisteis","discutieron")
        add("discutir","imperfecto","discutía","discutías","discutía","discutíamos","discutíais","discutían")
        add("discutir","futuro","discutiré","discutirás","discutirá","discutiremos","discutiréis","discutirán")
        add("discutir","condicional","discutiría","discutirías","discutiría","discutiríamos","discutiríais","discutirían")
        add("discutir","subjuntivo","discuta","discutas","discuta","discutamos","discutáis","discutan")

        // ════════════════════════════════════════════════════
        // INSISTIR (настаивать) — regular -ir
        // ════════════════════════════════════════════════════
        add("insistir","presente","insisto","insistes","insiste","insistimos","insistís","insisten")
        add("insistir","preterito","insistí","insististe","insistió","insistimos","insististeis","insistieron")
        add("insistir","imperfecto","insistía","insistías","insistía","insistíamos","insistíais","insistían")
        add("insistir","futuro","insistiré","insistirás","insistirá","insistiremos","insistiréis","insistirán")
        add("insistir","condicional","insistiría","insistirías","insistiría","insistiríamos","insistiríais","insistirían")
        add("insistir","subjuntivo","insista","insistas","insista","insistamos","insistáis","insistan")

        // ════════════════════════════════════════════════════
        // MOLESTAR (раздражать/беспокоить) — regular -ar
        // ════════════════════════════════════════════════════
        add("molestar","presente","molesto","molestas","molesta","molestamos","molestáis","molestan")
        add("molestar","preterito","molesté","molestaste","molestó","molestamos","molestasteis","molestaron")
        add("molestar","imperfecto","molestaba","molestabas","molestaba","molestábamos","molestabais","molestaban")
        add("molestar","futuro","molestaré","molestarás","molestará","molestaremos","molestaréis","molestarán")
        add("molestar","condicional","molestaría","molestarías","molestaría","molestaríamos","molestaríais","molestarían")
        add("molestar","subjuntivo","moleste","molestes","moleste","molestemos","molestéis","molesten")

        // ════════════════════════════════════════════════════
        // ECHAR (бросать/добавлять) — regular -ar
        // ════════════════════════════════════════════════════
        add("echar","presente","echo","echas","echa","echamos","echáis","echan")
        add("echar","preterito","eché","echaste","echó","echamos","echasteis","echaron")
        add("echar","imperfecto","echaba","echabas","echaba","echábamos","echabais","echaban")
        add("echar","futuro","echaré","echarás","echará","echaremos","echaréis","echarán")
        add("echar","condicional","echaría","echarías","echaría","echaríamos","echaríais","echarían")
        add("echar","subjuntivo","eche","eches","eche","echemos","echéis","echen")
    }
}
