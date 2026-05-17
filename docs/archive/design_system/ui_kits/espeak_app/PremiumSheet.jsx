// PremiumSheet.jsx — paywall modal
function PremiumSheet({ onClose }) {
  const [plan, setPlan] = React.useState("year");

  const features = [
    { icon:"♾", title:"Без лимитов на уроки",      sub:"Учись сколько хочешь" },
    { icon:"🚫", title:"Никакой рекламы",            sub:"Полная концентрация" },
    { icon:"🔥", title:"Заморозка ударов",           sub:"Пропускай дни без потерь" },
    { icon:"🎯", title:"Персональный план",          sub:"Адаптивные уроки" },
    { icon:"📊", title:"Расширенная статистика",     sub:"Видишь свой прогресс глубже" },
  ];

  const plans = [
    { id:"month",   label:"Месяц",  price:"299 ₽",   sub:"за месяц",  badge:null },
    { id:"year",    label:"Год",    price:"1 990 ₽", sub:"166 ₽/мес", badge:"−45%" },
  ];

  return (
    <div style={{
      position:"absolute", inset:0,
      background:"rgba(0,0,0,.55)",
      display:"flex", alignItems:"flex-end", justifyContent:"center",
      zIndex:50,
    }} onClick={onClose}>
      <div onClick={e => e.stopPropagation()} style={{
        width:"100%", background:"#fff",
        borderRadius:"24px 24px 0 0",
        padding:"10px 18px 24px",
        maxHeight:"92%", overflowY:"auto",
      }}>
        {/* Drag handle */}
        <div style={{width:40, height:4, borderRadius:2,
                     background:EASPEAK.border, margin:"0 auto 10px"}}/>

        {/* Hero */}
        <div style={{textAlign:"center", padding:"6px 8px 16px"}}>
          <div style={{
            width:70, height:70, margin:"0 auto",
            borderRadius:18,
            background:`linear-gradient(135deg, ${EASPEAK.gold}, ${EASPEAK.orange})`,
            color:"#fff", display:"flex", alignItems:"center", justifyContent:"center",
            fontSize:36, boxShadow:`0 8px 24px ${EASPEAK.gold}66`,
          }}><IconCrown size={36}/></div>
          <div style={{fontSize:24, fontWeight:800, color:EASPEAK.textMain, marginTop:12}}>
            EASPEAK Premium
          </div>
          <div style={{fontSize:14, color:EASPEAK.textGray, marginTop:4}}>
            Прокачай испанский на максимум
          </div>
        </div>

        {/* Features */}
        <div style={{display:"flex", flexDirection:"column", gap:10}}>
          {features.map((f, i) => (
            <div key={i} style={{
              display:"flex", alignItems:"center", gap:12,
              padding:"10px 12px",
              background:EASPEAK.bgGrayLt, borderRadius:14,
            }}>
              <div style={{
                width:38, height:38, borderRadius:10,
                background:"#fff",
                display:"flex", alignItems:"center", justifyContent:"center",
                fontSize:18, color:EASPEAK.purple,
              }}>{f.icon}</div>
              <div style={{flex:1}}>
                <div style={{fontSize:14, fontWeight:700, color:EASPEAK.textMain}}>{f.title}</div>
                <div style={{fontSize:12, color:EASPEAK.textGray, marginTop:1}}>{f.sub}</div>
              </div>
              <div style={{color:EASPEAK.purple}}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
              </div>
            </div>
          ))}
        </div>

        {/* Plan selector */}
        <div style={{display:"grid", gridTemplateColumns:"1fr 1fr", gap:10, marginTop:18}}>
          {plans.map(p => {
            const sel = p.id === plan;
            return (
              <button key={p.id} onClick={() => setPlan(p.id)} style={{
                position:"relative",
                padding:"14px 12px",
                border: sel ? `2px solid ${EASPEAK.purple}` : `1.5px solid ${EASPEAK.border}`,
                borderRadius:16,
                background: sel ? `${EASPEAK.purple}0F` : "#fff",
                fontFamily:"inherit", cursor:"pointer", textAlign:"left",
              }}>
                {p.badge && (
                  <div style={{
                    position:"absolute", top:-9, right:10,
                    padding:"3px 10px", borderRadius:9999,
                    background:`linear-gradient(135deg, ${EASPEAK.coral}, ${EASPEAK.pink})`,
                    color:"#fff", fontSize:10, fontWeight:800,
                    letterSpacing:.3,
                  }}>{p.badge}</div>
                )}
                <div style={{fontSize:13, fontWeight:600, color:EASPEAK.textGray}}>{p.label}</div>
                <div style={{fontSize:20, fontWeight:800, color:EASPEAK.textMain, marginTop:4}}>{p.price}</div>
                <div style={{fontSize:11, color:EASPEAK.textGray, marginTop:2}}>{p.sub}</div>
              </button>
            );
          })}
        </div>

        {/* CTA */}
        <button style={{
          width:"100%", height:54, marginTop:14,
          border:0, borderRadius:16,
          background:`linear-gradient(135deg, ${EASPEAK.purple}, ${EASPEAK.pink})`,
          color:"#fff", fontSize:16, fontWeight:800, fontFamily:"inherit",
          cursor:"pointer",
          boxShadow:`0 8px 22px ${EASPEAK.purple}55`,
        }}>Начать 7 дней бесплатно</button>
        <div style={{textAlign:"center", marginTop:10, fontSize:11, color:EASPEAK.textGray}}>
          Затем 1 990 ₽/год · отмена в любой момент
        </div>
      </div>
    </div>
  );
}

window.PremiumSheet = PremiumSheet;
