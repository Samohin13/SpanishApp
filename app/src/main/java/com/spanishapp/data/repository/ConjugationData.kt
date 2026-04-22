package com.spanishapp.data.repository

import com.spanishapp.data.db.entity.ConjugationEntity

/**
 * 50 essential Spanish verbs with full conjugation tables.
 * Tenses: presente, pretérito indefinido, futuro, imperfecto, condicional, subjuntivo presente
 */
object ConjugationData {

    fun getAll(): List<ConjugationEntity> = buildList {

        // ── HELPER ────────────────────────────────────────────
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
        // SER  (быть — постоянно)
        // ════════════════════════════════════════════════════
        add("ser","presente","soy","eres","es","somos","sois","son",true,"completamente irregular")
        add("ser","preterito","fui","fuiste","fue","fuimos","fuisteis","fueron",true)
        add("ser","imperfecto","era","eras","era","éramos","erais","eran",true)
        add("ser","futuro","seré","serás","será","seremos","seréis","serán",false)
        add("ser","condicional","sería","serías","sería","seríamos","seríais","serían",false)
        add("ser","subjuntivo","sea","seas","sea","seamos","seáis","sean",true)

        // ════════════════════════════════════════════════════
        // ESTAR  (быть — временно)
        // ════════════════════════════════════════════════════
        add("estar","presente","estoy","estás","está","estamos","estáis","están",true,"1sg irregular")
        add("estar","preterito","estuve","estuviste","estuvo","estuvimos","estuvisteis","estuvieron",true)
        add("estar","imperfecto","estaba","estabas","estaba","estábamos","estabais","estaban",false)
        add("estar","futuro","estaré","estarás","estará","estaremos","estaréis","estarán",false)
        add("estar","condicional","estaría","estarías","estaría","estaríamos","estaríais","estarían",false)
        add("estar","subjuntivo","esté","estés","esté","estemos","estéis","estén",true)

        // ════════════════════════════════════════════════════
        // TENER  (иметь)
        // ════════════════════════════════════════════════════
        add("tener","presente","tengo","tienes","tiene","tenemos","tenéis","tienen",true,"e→ie, 1sg -go")
        add("tener","preterito","tuve","tuviste","tuvo","tuvimos","tuvisteis","tuvieron",true)
        add("tener","imperfecto","tenía","tenías","tenía","teníamos","teníais","tenían",false)
        add("tener","futuro","tendré","tendrás","tendrá","tendremos","tendréis","tendrán",true,"future stem: tendr-")
        add("tener","condicional","tendría","tendrías","tendría","tendríamos","tendríais","tendrían",true)
        add("tener","subjuntivo","tenga","tengas","tenga","tengamos","tengáis","tengan",true)

        // ════════════════════════════════════════════════════
        // HABER  (вспомогательный)
        // ════════════════════════════════════════════════════
        add("haber","presente","he","has","ha","hemos","habéis","han",true)
        add("haber","preterito","hube","hubiste","hubo","hubimos","hubisteis","hubieron",true)
        add("haber","imperfecto","había","habías","había","habíamos","habíais","habían",false)
        add("haber","futuro","habré","habrás","habrá","habremos","habréis","habrán",true)
        add("haber","condicional","habría","habrías","habría","habríamos","habríais","habrían",true)
        add("haber","subjuntivo","haya","hayas","haya","hayamos","hayáis","hayan",true)

        // ════════════════════════════════════════════════════
        // IR  (идти)
        // ════════════════════════════════════════════════════
        add("ir","presente","voy","vas","va","vamos","vais","van",true,"completamente irregular")
        add("ir","preterito","fui","fuiste","fue","fuimos","fuisteis","fueron",true,"=ser en pretérito")
        add("ir","imperfecto","iba","ibas","iba","íbamos","ibais","iban",true)
        add("ir","futuro","iré","irás","irá","iremos","iréis","irán",false)
        add("ir","condicional","iría","irías","iría","iríamos","iríais","irían",false)
        add("ir","subjuntivo","vaya","vayas","vaya","vayamos","vayáis","vayan",true)

        // ════════════════════════════════════════════════════
        // PODER  (мочь)
        // ════════════════════════════════════════════════════
        add("poder","presente","puedo","puedes","puede","podemos","podéis","pueden",true,"o→ue stem-changing")
        add("poder","preterito","pude","pudiste","pudo","pudimos","pudisteis","pudieron",true)
        add("poder","imperfecto","podía","podías","podía","podíamos","podíais","podían",false)
        add("poder","futuro","podré","podrás","podrá","podremos","podréis","podrán",true,"future stem: podr-")
        add("poder","condicional","podría","podrías","podría","podríamos","podríais","podrían",true)
        add("poder","subjuntivo","pueda","puedas","pueda","podamos","podáis","puedan",true)

        // ════════════════════════════════════════════════════
        // QUERER  (хотеть / любить)
        // ════════════════════════════════════════════════════
        add("querer","presente","quiero","quieres","quiere","queremos","queréis","quieren",true,"e→ie")
        add("querer","preterito","quise","quisiste","quiso","quisimos","quisisteis","quisieron",true)
        add("querer","imperfecto","quería","querías","quería","queríamos","queríais","querían",false)
        add("querer","futuro","querré","querrás","querrá","querremos","querréis","querrán",true,"future stem: querr-")
        add("querer","condicional","querría","querrías","querría","querríamos","querríais","querrían",true)
        add("querer","subjuntivo","quiera","quieras","quiera","queramos","queráis","quieran",true)

        // ════════════════════════════════════════════════════
        // SABER  (знать)
        // ════════════════════════════════════════════════════
        add("saber","presente","sé","sabes","sabe","sabemos","sabéis","saben",true,"1sg irregular: sé")
        add("saber","preterito","supe","supiste","supo","supimos","supisteis","supieron",true)
        add("saber","imperfecto","sabía","sabías","sabía","sabíamos","sabíais","sabían",false)
        add("saber","futuro","sabré","sabrás","sabrá","sabremos","sabréis","sabrán",true,"future stem: sabr-")
        add("saber","condicional","sabría","sabrías","sabría","sabríamos","sabríais","sabrían",true)
        add("saber","subjuntivo","sepa","sepas","sepa","sepamos","sepáis","sepan",true)

        // ════════════════════════════════════════════════════
        // HACER  (делать)
        // ════════════════════════════════════════════════════
        add("hacer","presente","hago","haces","hace","hacemos","hacéis","hacen",true,"1sg: hago")
        add("hacer","preterito","hice","hiciste","hizo","hicimos","hicisteis","hicieron",true)
        add("hacer","imperfecto","hacía","hacías","hacía","hacíamos","hacíais","hacían",false)
        add("hacer","futuro","haré","harás","hará","haremos","haréis","harán",true,"future stem: har-")
        add("hacer","condicional","haría","harías","haría","haríamos","haríais","harían",true)
        add("hacer","subjuntivo","haga","hagas","haga","hagamos","hagáis","hagan",true)

        // ════════════════════════════════════════════════════
        // DECIR  (говорить / сказать)
        // ════════════════════════════════════════════════════
        add("decir","presente","digo","dices","dice","decimos","decís","dicen",true,"e→i, 1sg: digo")
        add("decir","preterito","dije","dijiste","dijo","dijimos","dijisteis","dijeron",true)
        add("decir","imperfecto","decía","decías","decía","decíamos","decíais","decían",false)
        add("decir","futuro","diré","dirás","dirá","diremos","diréis","dirán",true,"future stem: dir-")
        add("decir","condicional","diría","dirías","diría","diríamos","diríais","dirían",true)
        add("decir","subjuntivo","diga","digas","diga","digamos","digáis","digan",true)

        // ════════════════════════════════════════════════════
        // VENIR  (приходить)
        // ════════════════════════════════════════════════════
        add("venir","presente","vengo","vienes","viene","venimos","venís","vienen",true,"e→ie, 1sg: vengo")
        add("venir","preterito","vine","viniste","vino","vinimos","vinisteis","vinieron",true)
        add("venir","imperfecto","venía","venías","venía","veníamos","veníais","venían",false)
        add("venir","futuro","vendré","vendrás","vendrá","vendremos","vendréis","vendrán",true,"future stem: vendr-")
        add("venir","condicional","vendría","vendrías","vendría","vendríamos","vendríais","vendrían",true)
        add("venir","subjuntivo","venga","vengas","venga","vengamos","vengáis","vengan",true)

        // ════════════════════════════════════════════════════
        // PONER  (ставить)
        // ════════════════════════════════════════════════════
        add("poner","presente","pongo","pones","pone","ponemos","ponéis","ponen",true,"1sg: pongo")
        add("poner","preterito","puse","pusiste","puso","pusimos","pusisteis","pusieron",true)
        add("poner","imperfecto","ponía","ponías","ponía","poníamos","poníais","ponían",false)
        add("poner","futuro","pondré","pondrás","pondrá","pondremos","pondréis","pondrán",true,"future stem: pondr-")
        add("poner","condicional","pondría","pondrías","pondría","pondríamos","pondríais","pondrían",true)
        add("poner","subjuntivo","ponga","pongas","ponga","pongamos","pongáis","pongan",true)

        // ════════════════════════════════════════════════════
        // SALIR  (выходить)
        // ════════════════════════════════════════════════════
        add("salir","presente","salgo","sales","sale","salimos","salís","salen",true,"1sg: salgo")
        add("salir","preterito","salí","saliste","salió","salimos","salisteis","salieron",false)
        add("salir","imperfecto","salía","salías","salía","salíamos","salíais","salían",false)
        add("salir","futuro","saldré","saldrás","saldrá","saldremos","saldréis","saldrán",true,"future stem: saldr-")
        add("salir","condicional","saldría","saldrías","saldría","saldríamos","saldríais","saldrían",true)
        add("salir","subjuntivo","salga","salgas","salga","salgamos","salgáis","salgan",true)

        // ════════════════════════════════════════════════════
        // HABLAR  (говорить — regular -AR model)
        // ════════════════════════════════════════════════════
        add("hablar","presente","hablo","hablas","habla","hablamos","habláis","hablan",false,"-AR regular")
        add("hablar","preterito","hablé","hablaste","habló","hablamos","hablasteis","hablaron",false)
        add("hablar","imperfecto","hablaba","hablabas","hablaba","hablábamos","hablabais","hablaban",false)
        add("hablar","futuro","hablaré","hablarás","hablará","hablaremos","hablaréis","hablarán",false)
        add("hablar","condicional","hablaría","hablarías","hablaría","hablaríamos","hablaríais","hablarían",false)
        add("hablar","subjuntivo","hable","hables","hable","hablemos","habléis","hablen",false)

        // ════════════════════════════════════════════════════
        // COMER  (есть — regular -ER model)
        // ════════════════════════════════════════════════════
        add("comer","presente","como","comes","come","comemos","coméis","comen",false,"-ER regular")
        add("comer","preterito","comí","comiste","comió","comimos","comisteis","comieron",false)
        add("comer","imperfecto","comía","comías","comía","comíamos","comíais","comían",false)
        add("comer","futuro","comeré","comerás","comerá","comeremos","comeréis","comerán",false)
        add("comer","condicional","comería","comerías","comería","comeríamos","comeríais","comerían",false)
        add("comer","subjuntivo","coma","comas","coma","comamos","comáis","coman",false)

        // ════════════════════════════════════════════════════
        // VIVIR  (жить — regular -IR model)
        // ════════════════════════════════════════════════════
        add("vivir","presente","vivo","vives","vive","vivimos","vivís","viven",false,"-IR regular")
        add("vivir","preterito","viví","viviste","vivió","vivimos","vivisteis","vivieron",false)
        add("vivir","imperfecto","vivía","vivías","vivía","vivíamos","vivíais","vivían",false)
        add("vivir","futuro","viviré","vivirás","vivirá","viviremos","viviréis","vivirán",false)
        add("vivir","condicional","viviría","vivirías","viviría","viviríamos","viviríais","vivirían",false)
        add("vivir","subjuntivo","viva","vivas","viva","vivamos","viváis","vivan",false)

        // ════════════════════════════════════════════════════
        // DAR  (давать)
        // ════════════════════════════════════════════════════
        add("dar","presente","doy","das","da","damos","dais","dan",true,"1sg: doy")
        add("dar","preterito","di","diste","dio","dimos","disteis","dieron",true,"uses -ER endings")
        add("dar","imperfecto","daba","dabas","daba","dábamos","dabais","daban",false)
        add("dar","futuro","daré","darás","dará","daremos","daréis","darán",false)
        add("dar","condicional","daría","darías","daría","daríamos","daríais","darían",false)
        add("dar","subjuntivo","dé","des","dé","demos","deis","den",true)

        // ════════════════════════════════════════════════════
        // VER  (видеть)
        // ════════════════════════════════════════════════════
        add("ver","presente","veo","ves","ve","vemos","veis","ven",true,"1sg: veo")
        add("ver","preterito","vi","viste","vio","vimos","visteis","vieron",false)
        add("ver","imperfecto","veía","veías","veía","veíamos","veíais","veían",true,"irregular imperfecto")
        add("ver","futuro","veré","verás","verá","veremos","veréis","verán",false)
        add("ver","condicional","vería","verías","vería","veríamos","veríais","verían",false)
        add("ver","subjuntivo","vea","veas","vea","veamos","veáis","vean",true)

        // ════════════════════════════════════════════════════
        // DORMIR  (спать — o→ue/u stem-changing)
        // ════════════════════════════════════════════════════
        add("dormir","presente","duermo","duermes","duerme","dormimos","dormís","duermen",true,"o→ue")
        add("dormir","preterito","dormí","dormiste","durmió","dormimos","dormisteis","durmieron",true,"o→u 3rd person")
        add("dormir","imperfecto","dormía","dormías","dormía","dormíamos","dormíais","dormían",false)
        add("dormir","futuro","dormiré","dormirás","dormirá","dormiremos","dormiréis","dormirán",false)
        add("dormir","condicional","dormiría","dormirías","dormiría","dormiríamos","dormiríais","dormirían",false)
        add("dormir","subjuntivo","duerma","duermas","duerma","durmamos","durmáis","duerman",true)

        // ════════════════════════════════════════════════════
        // PEDIR  (просить — e→i)
        // ════════════════════════════════════════════════════
        add("pedir","presente","pido","pides","pide","pedimos","pedís","piden",true,"e→i")
        add("pedir","preterito","pedí","pediste","pidió","pedimos","pedisteis","pidieron",true,"e→i 3rd")
        add("pedir","imperfecto","pedía","pedías","pedía","pedíamos","pedíais","pedían",false)
        add("pedir","futuro","pediré","pedirás","pedirá","pediremos","pediréis","pedirán",false)
        add("pedir","condicional","pediría","pedirías","pediría","pediríamos","pediríais","pedirían",false)
        add("pedir","subjuntivo","pida","pidas","pida","pidamos","pidáis","pidan",true)

        // ════════════════════════════════════════════════════
        // CONOCER  (знать — 1sg -zco)
        // ════════════════════════════════════════════════════
        add("conocer","presente","conozco","conoces","conoce","conocemos","conocéis","conocen",true,"1sg: conozco (-zco)")
        add("conocer","preterito","conocí","conociste","conoció","conocimos","conocisteis","conocieron",false)
        add("conocer","imperfecto","conocía","conocías","conocía","conocíamos","conocíais","conocían",false)
        add("conocer","futuro","conoceré","conocerás","conocerá","conoceremos","conoceréis","conocerán",false)
        add("conocer","condicional","conocería","conocerías","conocería","conoceríamos","conoceríais","conocerían",false)
        add("conocer","subjuntivo","conozca","conozcas","conozca","conozcamos","conozcáis","conozcan",true)
    }
}