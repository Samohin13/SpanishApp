// GamesScreen.jsx — mini-game catalog
function GamesScreen() {
  const games = [
    { id:"match",   icon:"🃏", title:"Подбери пару",     desc:"Соедини слово и перевод",          xp:30, color:EASPEAK.purple,  best:"00:42" },
    { id:"hear",    icon:"🎧", title:"Угадай на слух",   desc:"Послушай и выбери правильный вариант", xp:40, color:EASPEAK.pink,    best:"8/10"   },
    { id:"flag",    icon:"🇪🇸", title:"Кубики Реала",     desc:"Собери испанскую фразу из кубиков", xp:50, color:EASPEAK.gold,    best:"12 ур." },
    { id:"speed",   icon:"⚡",  title:"Скоростной перевод", desc:"60 секунд — сколько успеешь",     xp:60, color:EASPEAK.coral,   best:"24 слов" },
    { id:"sentence",icon:"🧩", title:"Собери фразу",      desc:"Расставь слова в правильном порядке", xp:35, color:EASPEAK.purple, best:"15/20"  },
    { id:"verb",    icon:"⚙️", title:"Спряжение глаголов", desc:"Назови форму глагола",            xp:45, color:EASPEAK.pink,    best:"новая"  },
  ];

  return (
    <div style={{width:"100%", height:"100%", background:EASPEAK.bgGray, overflowY:"auto", paddingBottom:24}}>
      {/* Header */}
      <div style={{
        background:`linear-gradient(135deg, ${EASPEAK.pink}, ${EASPEAK.purple})`,
        padding:"18px 16px 32px", color:"#fff",
      }}>
        <div style={{fontSize:13, fontWeight:600, opacity:.85,
                     textTransform:"uppercase", letterSpacing:.5}}>Игры</div>
        <div style={{fontSize:24, fontWeight:800, marginTop:4}}>
          Учись играя
        </div>
        <div style={{fontSize:13, opacity:.85, marginTop:2}}>
          Каждая игра даёт XP и поднимает в рейтинге
        </div>
      </div>

      {/* Daily challenge banner */}
      <div style={{margin:"-18px 14px 0", padding:"14px 14px",
                   background:"#fff", borderRadius:20,
                   boxShadow:"0 6px 20px rgba(123,47,190,.15)",
                   display:"flex", alignItems:"center", gap:12}}>
        <div style={{
          width:54, height:54, borderRadius:14,
          background:`linear-gradient(135deg, ${EASPEAK.gold}, ${EASPEAK.orange})`,
          color:"#fff", display:"flex", alignItems:"center", justifyContent:"center",
          fontSize:24,
        }}>🏆</div>
        <div style={{flex:1}}>
          <div style={{fontSize:11, fontWeight:700, color:EASPEAK.gold,
                       textTransform:"uppercase", letterSpacing:.5}}>Челлендж дня</div>
          <div style={{fontSize:15, fontWeight:700, color:EASPEAK.textMain, marginTop:2}}>
            Скоростной перевод
          </div>
          <div style={{fontSize:12, color:EASPEAK.textGray, marginTop:1}}>
            +100 XP · осталось 8ч 24м
          </div>
        </div>
        <button style={{
          padding:"8px 14px", border:0, borderRadius:9999,
          background:EASPEAK.purple, color:"#fff", fontSize:13, fontWeight:700,
          fontFamily:"inherit", cursor:"pointer",
        }}>Играть</button>
      </div>

      {/* Game grid */}
      <div style={{padding:"14px 14px 0",
                   display:"grid", gridTemplateColumns:"1fr 1fr", gap:10}}>
        {games.map(g => (
          <div key={g.id} style={{
            background:"#fff", borderRadius:18,
            padding:"14px 12px",
            boxShadow:"0 4px 14px rgba(0,0,0,.04)",
            border:`1px solid ${EASPEAK.border}80`,
            cursor:"pointer",
          }}>
            <div style={{
              width:48, height:48, borderRadius:12,
              background:`${g.color}1A`,
              display:"flex", alignItems:"center", justifyContent:"center",
              fontSize:24,
            }}>{g.icon}</div>
            <div style={{fontSize:14, fontWeight:700, color:EASPEAK.textMain,
                         marginTop:10, lineHeight:1.2}}>{g.title}</div>
            <div style={{fontSize:11, color:EASPEAK.textGray, marginTop:3,
                         lineHeight:1.3, minHeight:28}}>{g.desc}</div>
            <div style={{display:"flex", alignItems:"center",
                         justifyContent:"space-between", marginTop:8}}>
              <span style={{fontSize:11, fontWeight:700, color:EASPEAK.gold,
                            display:"inline-flex", alignItems:"center", gap:3}}>
                <IconSparkles size={11}/>+{g.xp}
              </span>
              <span style={{fontSize:10, color:EASPEAK.textGray}}>{g.best}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

window.GamesScreen = GamesScreen;
