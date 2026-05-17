// DictionaryScreen.jsx — search + categories + word list
function DictionaryScreen() {
  const [query, setQuery] = React.useState("");
  const [active, setActive] = React.useState("all");

  const categories = [
    { id:"all",     label:"Все",          n:148 },
    { id:"verbs",   label:"Глаголы",      n:78 },
    { id:"food",    label:"Еда",          n:42 },
    { id:"travel",  label:"Путешествия",  n:31 },
    { id:"family",  label:"Семья",        n:38 },
  ];

  const words = [
    { es:"aprender",  ru:"учить, изучать",  cat:"verbs",  flags:3, phrase:false },
    { es:"comer",     ru:"есть, кушать",    cat:"verbs",  flags:5, phrase:false },
    { es:"el desayuno", ru:"завтрак",       cat:"food",   flags:4, phrase:false },
    { es:"¡Buen provecho!", ru:"Приятного аппетита!", cat:"food", flags:2, phrase:true },
    { es:"viajar",    ru:"путешествовать",  cat:"travel", flags:3, phrase:false },
    { es:"el aeropuerto", ru:"аэропорт",    cat:"travel", flags:4, phrase:false },
    { es:"la familia", ru:"семья",          cat:"family", flags:5, phrase:false },
    { es:"¿Cómo estás?", ru:"Как дела?",    cat:"family", flags:5, phrase:true },
  ];

  const visible = words.filter(w =>
    (active === "all" || w.cat === active) &&
    (query === "" || w.es.toLowerCase().includes(query.toLowerCase())
                  || w.ru.toLowerCase().includes(query.toLowerCase()))
  );

  return (
    <div style={{width:"100%", height:"100%", background:EASPEAK.bgGray, overflowY:"auto", paddingBottom:24}}>
      {/* Header */}
      <div style={{background:"#fff", padding:"14px 16px 12px"}}>
        <div style={{fontSize:24, fontWeight:800, color:EASPEAK.textMain}}>Словарь</div>
        <div style={{fontSize:13, color:EASPEAK.textGray, marginTop:2}}>
          148 слов · 24 фразы
        </div>
        <div style={{marginTop:12, position:"relative"}}>
          <div style={{position:"absolute", left:14, top:14, color:EASPEAK.textGray}}>
            <IconSearch size={20}/>
          </div>
          <input
            value={query}
            onChange={e => setQuery(e.target.value)}
            placeholder="Поиск слова или перевода"
            style={{
              width:"100%", height:48, paddingLeft:42, paddingRight:14,
              border:`1.5px solid ${EASPEAK.border}`,
              borderRadius:14, fontSize:14, fontFamily:"inherit",
              background:EASPEAK.bgGrayLt, outline:"none",
              color:EASPEAK.textMain, boxSizing:"border-box",
            }}/>
        </div>
      </div>

      {/* Category tabs */}
      <div style={{padding:"12px 14px 0", display:"flex", gap:6, overflowX:"auto",
                   scrollbarWidth:"none"}}>
        {categories.map(c => {
          const sel = c.id === active;
          return (
            <button key={c.id} onClick={() => setActive(c.id)} style={{
              flex:"0 0 auto",
              padding:"8px 14px", borderRadius:9999,
              border: sel ? "0" : `1px solid ${EASPEAK.border}`,
              background: sel ? EASPEAK.purple : "#fff",
              color: sel ? "#fff" : EASPEAK.textMain,
              fontSize:13, fontWeight:600, fontFamily:"inherit",
              cursor:"pointer", whiteSpace:"nowrap",
            }}>{c.label} <span style={{opacity:.7}}>·{c.n}</span></button>
          );
        })}
      </div>

      {/* Word list */}
      <div style={{margin:"14px 14px 0", background:"#fff", borderRadius:20,
                   padding:"4px 6px",
                   boxShadow:"0 4px 16px rgba(0,0,0,.04)"}}>
        {visible.map((w, i) => (
          <div key={i} style={{
            display:"flex", alignItems:"center", gap:12,
            padding:"12px 10px",
            borderTop: i === 0 ? "0" : `1px solid ${EASPEAK.border}80`,
          }}>
            <div style={{flex:1}}>
              <div style={{fontSize:15, fontWeight:700, color:EASPEAK.textMain,
                           display:"flex", alignItems:"center", gap:6}}>
                {w.es}
                {w.phrase && <span style={{
                  fontSize:9, fontWeight:700, padding:"2px 6px", borderRadius:4,
                  background:`${EASPEAK.purple}1F`, color:EASPEAK.purple,
                  textTransform:"uppercase", letterSpacing:.3,
                }}>фраза</span>}
              </div>
              <div style={{fontSize:13, color:EASPEAK.textGray, marginTop:2}}>{w.ru}</div>
            </div>
            <div style={{display:"flex", gap:1, fontSize:11}}>
              {[1,2,3,4,5].map(n => (
                <span key={n} style={{
                  filter: n > w.flags ? "grayscale(1) opacity(.3)" : "none",
                }}>🇪🇸</span>
              ))}
            </div>
            <button style={{
              width:36, height:36, border:0, borderRadius:9999,
              background:`${EASPEAK.purple}14`, color:EASPEAK.purple,
              cursor:"pointer", display:"flex",
              alignItems:"center", justifyContent:"center",
            }}><IconVolume size={18}/></button>
          </div>
        ))}
        {visible.length === 0 && (
          <div style={{padding:"32px 16px", textAlign:"center",
                       fontSize:14, color:EASPEAK.textGray}}>
            Ничего не найдено
          </div>
        )}
      </div>
    </div>
  );
}

function IconSearch({ size=20 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>;
}

window.DictionaryScreen = DictionaryScreen;
