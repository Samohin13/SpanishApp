// OnboardingScreen.jsx — 3-page pager: hello → name → level
function OnboardingScreen({ onFinish }) {
  const [page, setPage] = React.useState(0);
  const [name, setName] = React.useState("");
  const [level, setLevel] = React.useState("A1");

  const go = (n) => setPage(p => Math.max(0, Math.min(2, p + n)));
  const finish = () => onFinish && onFinish({ name: name || "Друг", level });

  return (
    <div style={{
      width:"100%", height:"100%",
      background:`linear-gradient(180deg, rgba(255,107,0,.10), ${EASPEAK.white} 240px, ${EASPEAK.white})`,
      display:"flex", flexDirection:"column",
      paddingTop:24,
    }}>
      {/* Page indicator */}
      <div style={{display:"flex", justifyContent:"center", gap:6, padding:"8px 0 12px"}}>
        {[0,1,2].map(i => (
          <div key={i} style={{
            width: i===page ? 24 : 6, height:6, borderRadius:3,
            background: i===page ? EASPEAK.purple : "#E0E0E0",
            transition:"width 240ms",
          }}/>
        ))}
      </div>

      <div style={{flex:1, display:"flex", flexDirection:"column",
                   alignItems:"center", justifyContent:"center",
                   padding:"0 28px", textAlign:"center"}}>
        {page === 0 && (
          <>
            <div style={{fontSize:80, lineHeight:1, marginBottom:16}}>🇪🇸</div>
            <div style={{fontSize:36, fontWeight:800, color:EASPEAK.textMain,
                         letterSpacing:-1, lineHeight:1.05, marginBottom:6}}>¡Hola!</div>
            <div style={{fontSize:22, fontWeight:700, color:EASPEAK.textMain, marginBottom:14}}>
              Добро пожаловать в EASPEAK
            </div>
            <div style={{fontSize:15, color:EASPEAK.textGray, lineHeight:1.5, maxWidth:280}}>
              Выучи испанский с микро-уроками, флэшкартами и ИИ-репетитором.
            </div>
          </>
        )}
        {page === 1 && (
          <>
            <div style={{fontSize:48, marginBottom:14}}>👋</div>
            <div style={{fontSize:24, fontWeight:800, color:EASPEAK.textMain, marginBottom:6}}>
              Как тебя зовут?
            </div>
            <div style={{fontSize:13, color:EASPEAK.textGray, marginBottom:24, maxWidth:260}}>
              Это имя будет отображаться в твоём профиле
            </div>
            <input
              autoFocus
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="Твоё имя"
              style={{
                width:"100%", maxWidth:280, height:54,
                border:`1.5px solid ${EASPEAK.border}`,
                borderRadius:14, padding:"0 16px",
                fontSize:16, fontFamily:"inherit",
                background:"#fff", outline:"none",
                color:EASPEAK.textMain,
              }}/>
          </>
        )}
        {page === 2 && (
          <>
            <div style={{fontSize:48, marginBottom:10}}>📈</div>
            <div style={{fontSize:22, fontWeight:800, color:EASPEAK.textMain, marginBottom:4}}>
              Какой у тебя уровень?
            </div>
            <div style={{fontSize:13, color:EASPEAK.textGray, marginBottom:18}}>
              Можно изменить позже в настройках
            </div>
            <div style={{display:"flex", flexDirection:"column", gap:10, width:"100%", maxWidth:280}}>
              {[
                ["A1","🚀 Начинающий","Только начинаю"],
                ["A2","🌍 Элементарный","Знаю базу"],
                ["B1","📚 Средний","Уверенно общаюсь"],
                ["B2","🎓 Выше среднего","Свободно владею"],
              ].map(([lv,t,sub]) => {
                const sel = lv === level;
                return (
                  <div key={lv} onClick={() => setLevel(lv)} style={{
                    display:"flex", alignItems:"center", gap:12,
                    padding:"14px 16px", borderRadius:14, cursor:"pointer",
                    background: sel ? "rgba(123,47,190,.12)" : "#fff",
                    border:`1.5px solid ${sel ? EASPEAK.purple : EASPEAK.border}`,
                    textAlign:"left",
                  }}>
                    <CefrBadge level={lv}/>
                    <div style={{flex:1}}>
                      <div style={{fontSize:14, fontWeight:700, color:EASPEAK.textMain}}>{t}</div>
                      <div style={{fontSize:11, color:EASPEAK.textGray}}>{sub}</div>
                    </div>
                    {sel && <div style={{color:EASPEAK.purple}}><IconCheck size={20}/></div>}
                  </div>
                );
              })}
            </div>
          </>
        )}
      </div>

      {/* Bottom buttons */}
      <div style={{padding:"16px 24px 28px", display:"flex", gap:10}}>
        {page > 0 && (
          <button onClick={() => go(-1)} style={{
            flex:"0 0 auto", padding:"0 20px", height:54,
            border:`1.5px solid ${EASPEAK.border}`,
            borderRadius:14, background:"#fff",
            fontSize:15, fontWeight:700, color:EASPEAK.textMain,
            fontFamily:"inherit", cursor:"pointer",
          }}>Назад</button>
        )}
        <button onClick={() => page === 2 ? finish() : go(+1)} style={{
          flex:1, height:54, border:0, borderRadius:14,
          background:EASPEAK.purple, color:"#fff",
          fontSize:16, fontWeight:700, fontFamily:"inherit",
          cursor:"pointer",
        }}>{page === 2 ? "Начать обучение →" : "Далее →"}</button>
      </div>
    </div>
  );
}

window.OnboardingScreen = OnboardingScreen;
