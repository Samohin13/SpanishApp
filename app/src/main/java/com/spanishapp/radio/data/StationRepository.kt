package com.spanishapp.radio.data

/**
 * 40 испаноязычных радиостанций для ESPEAK:
 * - 60% Испания (24): новости/разговор + поп/музыка + культура
 * - 20% Мексика (8): музыка + 2 разговорных
 * - 20% Аргентина (8): музыка + 2 разговорных
 *
 * URL потоков получены через radio-browser.info API + сверены с
 * официальными сайтами вещателей. Большинство — mp3/aac прямые потоки,
 * некоторые — HLS (.m3u8).
 *
 * Если поток сломан, ExoPlayer выкинет PlayerException — обработать
 * в RadioPlayerController + показать «Станция временно недоступна».
 */
object StationRepository {

    // ════════════════════════════════════════════════════════════════
    //  ИСПАНИЯ — 24 станции
    // ════════════════════════════════════════════════════════════════

    private val spainStations = listOf(
        // ─── РАЗГОВОР / НОВОСТИ (10) ──────────────────────────────
        Station(
            id = "es_ser", shortCode = "SER", name = "Cadena SER",
            program = "Hoy por Hoy", frequency = 88.7f,
            country = Country.SPAIN, genre = Genre.TALK, level = CefrLevel.B1,
            streamUrl = "http://playerservices.streamtheworld.com/api/livestream-redirect/CADENASER.mp3"
        ),
        Station(
            id = "es_rne1", shortCode = "RNE", name = "RNE Radio 1",
            program = "Las Mañanas", frequency = 88.2f,
            country = Country.SPAIN, genre = Genre.NEWS, level = CefrLevel.B1,
            streamUrl = "https://dispatcher.rndfnk.com/crtve/rne1/gal/mp3/high"
        ),
        Station(
            id = "es_cope", shortCode = "COPE", name = "Cadena COPE",
            program = "Herrera en COPE", frequency = 100.7f,
            country = Country.SPAIN, genre = Genre.TALK, level = CefrLevel.B2,
            streamUrl = "http://flucast28-h-cloud.flumotion.com/cope/net1.mp3"
        ),
        Station(
            id = "es_onda", shortCode = "ONDA", name = "Onda Cero",
            program = "Más de Uno", frequency = 95.4f,
            country = Country.SPAIN, genre = Genre.TALK, level = CefrLevel.B1,
            streamUrl = "https://atres-live.ondacero.es/live/ondacero/bitrate_1.m3u8"
        ),
        Station(
            id = "es_esradio", shortCode = "EsR", name = "EsRadio",
            program = "Es la Mañana", frequency = 99.0f,
            country = Country.SPAIN, genre = Genre.TALK, level = CefrLevel.B2,
            streamUrl = "https://azura.abcorp.es/listen/esradio_granada/radio.mp3"
        ),
        Station(
            id = "es_intereconomia", shortCode = "iEc", name = "Intereconomía",
            program = "Económica", frequency = 95.1f,
            country = Country.SPAIN, genre = Genre.NEWS, level = CefrLevel.B2,
            streamUrl = "https://intereconomia.emitironline.com/"
        ),
        Station(
            id = "es_marca", shortCode = "MAR", name = "Radio Marca",
            program = "Спорт-новости", frequency = 103.5f,
            country = Country.SPAIN, genre = Genre.SPORTS, level = CefrLevel.B1,
            streamUrl = "https://www.marca.com/radio/geoblock/getSrcApp.php?dial=Nacional&dist=radiomarcaweb"
        ),
        Station(
            id = "es_cope_deportes", shortCode = "C+D", name = "Radio Marca Sport",
            program = "Спорт-программы", frequency = 99.5f,
            country = Country.SPAIN, genre = Genre.SPORTS, level = CefrLevel.B2,
            streamUrl = "http://flucast28-h-cloud.flumotion.com/cope/net1.mp3"
        ),
        Station(
            id = "es_rne_cultura", shortCode = "RN5", name = "Radio 5 Noticias",
            program = "Сводки 24/7", frequency = 90.3f,
            country = Country.SPAIN, genre = Genre.NEWS, level = CefrLevel.B2,
            streamUrl = "https://f141.rndfnk.com/star/crtve/rne5/gra/mp3/128/stream.mp3?cid=01GEPXW032X3Y2SXM6M4X4F1X4&sid=2ar1MBXpFkcnL6WuaxAXO1hEITa&token=L_nTm1RjUems0IjZMMQ_ZzFu3XQ_1_faFzfSM3OR5t8&tvf=06JeYFmsqRdmMTQxLnJuZGZuay5jb20"
        ),
        Station(
            id = "es_rne_clasica", shortCode = "RNC", name = "Radio Clásica",
            program = "Классика", frequency = 96.5f,
            country = Country.SPAIN, genre = Genre.CULTURE, level = CefrLevel.B1,
            streamUrl = "https://f121.rndfnk.com/star/crtve/rnerc/main/mp3/128/stream.mp3?cid=01GEM633TKTAHEB3GYTNAEZB90&sid=3DJW7d1vUQTr2MlFDR5fBHG2joq&token=cCVkCc78Nf0V3xu63dhQYE5HKMw3WnJ9HKWaNSk-MUQ&tvf=pCEh3arNrBhmMTIxLnJuZGZuay5jb20"
        ),

        // ─── МУЗЫКА / ПОП (10) ──────────────────────────────────────
        Station(
            id = "es_los40", shortCode = "40", name = "Los 40 Principales",
            program = "Top Hits", frequency = 93.9f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/Los40.mp3"
        ),
        Station(
            id = "es_dial", shortCode = "DIA", name = "Cadena Dial",
            program = "Lo mejor en español", frequency = 91.7f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "http://playerservices.streamtheworld.com/api/livestream-redirect/CADENADIAL.mp3"
        ),
        Station(
            id = "es_cadena100", shortCode = "100", name = "Cadena 100",
            program = "Pop/Rock", frequency = 99.5f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "http://cadena100-streamers-mp3.flumotion.com/cope/cadena100.mp3"
        ),
        Station(
            id = "es_kissfm", shortCode = "KIS", name = "Kiss FM",
            program = "Pop hits", frequency = 102.7f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://bbkissfm.kissfmradio.cires21.com/bbkissfm.mp3"
        ),
        Station(
            id = "es_europafm", shortCode = "EUR", name = "Europa FM",
            program = "Hits", frequency = 91.0f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://stream.zeno.fm/se76qau1hc9uv"
        ),
        Station(
            id = "es_maximafm", shortCode = "MAX", name = "Máxima FM",
            program = "Dance", frequency = 89.2f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://en-directo.frequence-radio.com/amp/redirect.php?radio=maxima-fm"
        ),
        Station(
            id = "es_locafm", shortCode = "LOC", name = "Loca FM",
            program = "Electronic/Dance", frequency = 92.6f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "http://s3.we4stream.com:8045/liv"
        ),
        Station(
            id = "es_m80", shortCode = "M80", name = "Oldies Radio",
            program = "80s / 90s / 2000s", frequency = 89.5f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "http://radio.latina.red:8000/oldiesradiotime.mp3"
        ),
        Station(
            id = "es_hitfm", shortCode = "HIT", name = "Hit FM",
            program = "Pop Hits", frequency = 102.0f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://bbhitfm.kissfmradio.cires21.com/bbhitfm.mp3"
        ),
        Station(
            id = "es_rockfm", shortCode = "ROC", name = "Rock FM",
            program = "Rock clásico", frequency = 102.5f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "http://flucast31-h-cloud.flumotion.com/cope/rockfm-low.mp3"
        ),

        // ─── КУЛЬТУРА / СПЕЦИАЛЬНОЕ (4) ─────────────────────────
        Station(
            id = "es_radio3", shortCode = "R3", name = "Radio 3",
            program = "Alternative / Indie", frequency = 93.1f,
            country = Country.SPAIN, genre = Genre.CULTURE, level = CefrLevel.B2,
            streamUrl = "https://rtvelivestream.rtve.es/rtvesec/rne/rne_r3_main.m3u8"
        ),
        Station(
            id = "es_ole", shortCode = "OLÉ", name = "Flamenco Radio",
            program = "Flamenco · Copla", frequency = 105.2f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://rtva-live-radio.flumotion.com/rtva/flamenco.mp3"
        ),
        Station(
            id = "es_dial_latino", shortCode = "DiL", name = "Latina FM",
            program = "Latin music España", frequency = 91.7f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://radiostreaming.online:8002/stream?type=http&nocache=8"
        ),
        Station(
            id = "es_serlatino", shortCode = "SeL", name = "Tropical FM",
            program = "Tropical hits", frequency = 94.0f,
            country = Country.SPAIN, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "http://streaming5.elitecomunicacion.es:8030/live.mp3"
        ),
    )

    // ════════════════════════════════════════════════════════════════
    //  МЕКСИКА — 8 станций
    // ════════════════════════════════════════════════════════════════

    private val mexicoStations = listOf(
        Station(
            id = "mx_los40", shortCode = "40", name = "Los 40 Principales MX",
            program = "Top Hits Morelia", frequency = 101.7f,
            country = Country.MEXICO, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "http://27063.live.streamtheworld.com/LOS40_MEXICO_SC"
        ),
        Station(
            id = "mx_exa", shortCode = "EXA", name = "Exa FM",
            program = "Top en español/inglés", frequency = 104.9f,
            country = Country.MEXICO, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "http://18213.live.streamtheworld.com/XHEXA_SC"
        ),
        Station(
            id = "mx_joya", shortCode = "JOY", name = "Romántica 1380",
            program = "Baladas románticas CDMX", frequency = 93.7f,
            country = Country.MEXICO, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://cdn1.onstream.audio/proxy/xeco/stream"
        ),
        Station(
            id = "mx_mix", shortCode = "MIX", name = "Mix 80s 90s",
            program = "Pop ретро Mexico", frequency = 106.5f,
            country = Country.MEXICO, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://18443.live.streamtheworld.com:443/XHDFMFMAAC.aac"
        ),
        Station(
            id = "mx_banda", shortCode = "BND", name = "Banda 93.3 MTY",
            program = "Banda y Norteña", frequency = 97.7f,
            country = Country.MEXICO, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/XHQQ_FMAAC.aac"
        ),
        Station(
            id = "mx_beat", shortCode = "BET", name = "Beat 100.9",
            program = "Dance / Electronic", frequency = 100.9f,
            country = Country.MEXICO, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/XHSONFMAAC.aac"
        ),
        Station(
            id = "mx_wradio", shortCode = "W", name = "W Radio México",
            program = "Así las cosas", frequency = 96.9f,
            country = Country.MEXICO, genre = Genre.TALK, level = CefrLevel.B1,
            streamUrl = "https://streaming.servicioswebmx.com/8248/stream"
        ),
        Station(
            id = "mx_imagen", shortCode = "IMG", name = "Imagen Radio",
            program = "Ciro Gómez Leyva", frequency = 90.5f,
            country = Country.MEXICO, genre = Genre.NEWS, level = CefrLevel.B2,
            streamUrl = "https://playradio.mx/proxy/imagen?mp=/stream"
        ),
    )

    // ════════════════════════════════════════════════════════════════
    //  АРГЕНТИНА — 8 станций
    // ════════════════════════════════════════════════════════════════

    private val argentinaStations = listOf(
        Station(
            id = "ar_la100", shortCode = "100", name = "La 100",
            program = "Pop hits", frequency = 99.9f,
            country = Country.ARGENTINA, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/FM999_56.mp3"
        ),
        Station(
            id = "ar_pop", shortCode = "POP", name = "Pop Radio",
            program = "Pop / hits", frequency = 101.5f,
            country = Country.ARGENTINA, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://popradio.stweb.tv/popradio/live/playlist.m3u8"
        ),
        Station(
            id = "ar_aspen", shortCode = "ASP", name = "Aspen 102.3",
            program = "Classic rock/pop", frequency = 102.3f,
            country = Country.ARGENTINA, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/ASPEN.mp3"
        ),
        Station(
            id = "ar_vorterix", shortCode = "VOR", name = "Vorterix",
            program = "Rock argentino", frequency = 92.1f,
            country = Country.ARGENTINA, genre = Genre.MUSIC, level = CefrLevel.B1,
            streamUrl = "https://ice2.edge-apps.net/radio1_high-20057.audio"
        ),
        Station(
            id = "ar_disney", shortCode = "DSN", name = "Radio Disney",
            program = "Pop / Family", frequency = 94.3f,
            country = Country.ARGENTINA, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/DISNEY_ARG_BA_ADP.aac"
        ),
        Station(
            id = "ar_metro", shortCode = "MET", name = "Metro 95.1",
            program = "Dance", frequency = 95.1f,
            country = Country.ARGENTINA, genre = Genre.MUSIC, level = CefrLevel.A2,
            streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/METRO.mp3"
        ),
        Station(
            id = "ar_mitre", shortCode = "MIT", name = "Radio Mitre",
            program = "Lanata sin filtro", frequency = 100.3f,
            country = Country.ARGENTINA, genre = Genre.TALK, level = CefrLevel.B2,
            streamUrl = "https://sc.host-live.com:10810/stream"
        ),
        Station(
            id = "ar_continental", shortCode = "CNT", name = "Continental",
            program = "Tarde para todo", frequency = 104.3f,
            country = Country.ARGENTINA, genre = Genre.TALK, level = CefrLevel.B2,
            streamUrl = "https://radios.solumedia.com:10815/stream"
        ),
    )

    fun getStationsForCountry(country: Country): List<Station> = when (country) {
        Country.SPAIN -> spainStations
        Country.MEXICO -> mexicoStations
        Country.ARGENTINA -> argentinaStations
    }

    fun getAllStations(): List<Station> = spainStations + mexicoStations + argentinaStations

    /** Найти ближайшую станцию к указанной частоте в указанной стране. */
    fun nearestStation(frequency: Float, country: Country): Station? =
        getStationsForCountry(country).minByOrNull { kotlin.math.abs(it.frequency - frequency) }
}
