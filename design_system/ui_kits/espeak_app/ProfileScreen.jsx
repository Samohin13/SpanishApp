// ProfileScreen.jsx — Mi Perfil
function ProfileScreen({ user, onBack }) {
  const cats = [
    { key:"verbs",   icon:"⚡", label:"Глаголы",     flags:4, total:120, learned:78 },
    { key:"food",    icon:"🍽️", label:"Еда",        flags:3, total:80,  learned:42 },
    { key:"travel",  icon:"✈️", label:"Путешествия", flags:3, total:60,  learned:31 },
    { key:"family",  icon:"👨‍👩‍👧", label:"Семья",   flags:5, total:40, learned:38 },
    { key:"numbers", icon:"🔢", label:"Числа",       flags:5, total:30, learned:30 },
  ];

  return (
    <div style={{width:"100%", height:"100%", background:EASPEAK.bgGray, overflowY:"auto", paddingBottom:32}}>
      {/* Header gradient */}
      <div style={{
        background:`linear-gradient(135deg, ${EASPEAK.purple}, ${EASPEAK.pink})`,
        padding:"18px 18px 28px", color:"#fff",
      }}>
        <div style={{display:"flex", alignItems:"center", gap:6}}>
          <button onClick={onBack} style={{
            width:36, height:36, border:0, background:"transparent",
            color:"#fff", cursor:"pointer",
            display:"flex", alignItems:"center", justifyContent:"center"}}>
            <IconChevronLeft size={22}/>
          </button>
          <div style={{flex:1, fontSize:13, fontWeight:600, opacity:.85,
                       textTransform:"uppercase", letterSpacing:.5}}>Mi Perfil</div>
          <div style={{color:"#fff", padding:8}}><IconSettings size={20}/></div>
        </div>

        <div style={{display:"flex", alignItems:"center", gap:14, marginTop:14}}>
          <div style={{width:72, height:72, borderRadius:9999,
                       background:"rgba(255,255,255,.25)",
                       display:"flex", alignItems:"center", justifyContent:"center",
                       fontSize:30, fontWeight:800}}>
            {(user?.name || "?")[0].toUpperCase()}
          </div>
          <div style={{flex:1}}>
            <div style={{fontSize:22, fontWeight:800}}>{user?.name || "Друг"}</div>
            <div style={{fontSize:13, opacity:.85, marginTop:2}}>Уровень 7 · 1240 XP</div>
            <div style={{height:6, borderRadius:3, marginTop:8,
                         background:"rgba(255,255,255,.25)", overflow:"hidden"}}>
              <div style={{height:"100%", width:"62%",
                           background:"#fff", borderRadius:3}}/>
            </div>
          </div>
        </div>
      </div>

      {/* League badge */}
      <div style={{margin:"-14px 14px 0",
                   background:"#fff", borderRadius:20, padding:"12px 14px",
                   boxShadow:"0 6px 24px rgba(123,47,190,.18)",
                   display:"flex", alignItems:"center", gap:12}}>
        <div style={{fontSize:34}}>🥈</div>
        <div style={{flex:1}}>
          <div style={{fontSize:11, fontWeight:700, color:EASPEAK.textGray,
                       textTransform:"uppercase", letterSpacing:.5}}>Лига</div>
          <div style={{fontSize:16, fontWeight:800, color:EASPEAK.textMain}}>Серебряная</div>
          <div style={{fontSize:12, color:EASPEAK.textGray, marginTop:2}}>
            Следующая остановка: 🌅 Sevilla
          </div>
        </div>
        <div style={{color:EASPEAK.gold, display:"flex", alignItems:"center", gap:4,
                     fontSize:14, fontWeight:700}}>
          <IconTrophy size={20}/> #14
        </div>
      </div>

      {/* Stats row */}
      <div style={{display:"grid", gridTemplateColumns:"1fr 1fr 1fr", gap:10,
                   margin:"14px 14px 0"}}>
        <StatTile emoji="📚" value="148" label="Слов выучено"/>
        <StatTile emoji="⏱" value="14ч"  label="Общее время"/>
        <StatTile emoji="🔥" value="12"  label="Рекорд серии"/>
      </div>

      {/* Category mastery */}
      <div style={{padding:"22px 18px 8px", fontSize:11, fontWeight:700,
                   letterSpacing:.5, textTransform:"uppercase",
                   color:EASPEAK.textGray}}>Мастерство по темам</div>

      <div style={{margin:"0 14px", background:"#fff", borderRadius:20,
                   padding:"4px 6px",
                   boxShadow:"0 4px 16px rgba(0,0,0,.04)"}}>
        {cats.map((c, i) => (
          <div key={c.key} style={{
            display:"flex", alignItems:"center", gap:12,
            padding:"12px 12px",
            borderTop: i === 0 ? "0" : `1px solid ${EASPEAK.border}80`,
          }}>
            <div style={{width:36, height:36, borderRadius:9999,
                         background:EASPEAK.purplePale,
                         display:"flex", alignItems:"center", justifyContent:"center",
                         fontSize:18}}>{c.icon}</div>
            <div style={{flex:1}}>
              <div style={{fontSize:14, fontWeight:700, color:EASPEAK.textMain}}>{c.label}</div>
              <div style={{fontSize:11, color:EASPEAK.textGray}}>
                {c.learned} из {c.total} слов
              </div>
            </div>
            <div style={{display:"flex", gap:2, fontSize:14, letterSpacing:1}}>
              {[1,2,3,4,5].map(n => (
                <span key={n} style={{
                  filter: n > c.flags ? "grayscale(1) opacity(.3)" : "none",
                }}>🇪🇸</span>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function StatTile({ emoji, value, label }) {
  return (
    <div style={{
      background:"#fff", borderRadius:16, padding:"12px 10px",
      textAlign:"center",
      boxShadow:"0 4px 12px rgba(0,0,0,.04)",
    }}>
      <div style={{fontSize:22}}>{emoji}</div>
      <div style={{fontSize:20, fontWeight:800, color:EASPEAK.textMain, marginTop:2}}>{value}</div>
      <div style={{fontSize:10, color:EASPEAK.textGray, fontWeight:500, marginTop:2}}>{label}</div>
    </div>
  );
}

window.ProfileScreen = ProfileScreen;
