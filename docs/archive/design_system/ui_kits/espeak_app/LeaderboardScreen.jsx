// LeaderboardScreen.jsx — Path-to-Madrid league + ranks
function LeaderboardScreen({ user, onBack }) {
  const me = { rank: 14, name: user?.name || "Ты", xp: 1240, isMe: true };
  const others = [
    { rank: 1,  name: "Maria",     xp: 4820, country: "🇪🇸" },
    { rank: 2,  name: "Дмитрий",   xp: 3640, country: "🇷🇺" },
    { rank: 3,  name: "Carlos",    xp: 3210, country: "🇲🇽" },
    { rank: 4,  name: "Анна",      xp: 2870, country: "🇷🇺" },
    { rank: 5,  name: "Sofia",     xp: 2450, country: "🇦🇷" },
    { rank: 12, name: "Игорь",     xp: 1380, country: "🇷🇺" },
    { rank: 13, name: "Lucia",     xp: 1290, country: "🇪🇸" },
    me,
    { rank: 15, name: "Pedro",     xp: 1180, country: "🇨🇴" },
    { rank: 16, name: "Светлана",  xp: 1090, country: "🇷🇺" },
  ];

  return (
    <div style={{width:"100%", height:"100%", background:EASPEAK.bgGray, overflowY:"auto", paddingBottom:24}}>
      {/* Header */}
      <div style={{
        background:`linear-gradient(135deg, ${EASPEAK.purple}, ${EASPEAK.pink})`,
        padding:"14px 16px 24px", color:"#fff",
      }}>
        <div style={{display:"flex", alignItems:"center", gap:6}}>
          <button onClick={onBack} style={{
            width:40, height:40, border:0, background:"transparent", color:"#fff",
            cursor:"pointer", display:"flex", alignItems:"center", justifyContent:"center",
          }}><IconChevronLeft size={22}/></button>
          <div style={{flex:1, fontSize:13, fontWeight:600, opacity:.85,
                       textTransform:"uppercase", letterSpacing:.5}}>Рейтинг</div>
        </div>

        {/* Path-to-Madrid */}
        <div style={{marginTop:6}}>
          <div style={{fontSize:22, fontWeight:800}}>🥈 Серебряная лига</div>
          <div style={{fontSize:13, opacity:.85, marginTop:2}}>
            До золота: 380 XP · 3 дня
          </div>
          <div style={{height:6, borderRadius:3, marginTop:10,
                       background:"rgba(255,255,255,.25)"}}>
            <div style={{height:"100%", width:"68%",
                         background:"#fff", borderRadius:3}}/>
          </div>
        </div>
      </div>

      {/* Top 3 podium */}
      <div style={{margin:"-14px 14px 0",
                   background:"#fff", borderRadius:20,
                   padding:"16px 14px",
                   boxShadow:"0 6px 24px rgba(123,47,190,.18)",
                   display:"grid", gridTemplateColumns:"1fr 1fr 1fr", gap:10,
                   alignItems:"end"}}>
        {[
          { rank:2, name:"Дмитрий", xp:3640, country:"🇷🇺", h:74 },
          { rank:1, name:"Maria",   xp:4820, country:"🇪🇸", h:96, gold:true },
          { rank:3, name:"Carlos",  xp:3210, country:"🇲🇽", h:60 },
        ].map(p => (
          <div key={p.rank} style={{display:"flex", flexDirection:"column", alignItems:"center", gap:6}}>
            <div style={{
              width:54, height:54, borderRadius:9999,
              background: p.gold
                ? `linear-gradient(135deg, ${EASPEAK.gold}, ${EASPEAK.orange})`
                : EASPEAK.purplePale,
              color: p.gold ? "#fff" : EASPEAK.purple,
              display:"flex", alignItems:"center", justifyContent:"center",
              fontSize:20, fontWeight:800,
              border: p.gold ? "0" : `2px solid ${EASPEAK.purple}`,
            }}>{p.name[0]}</div>
            <div style={{fontSize:13, fontWeight:700, color:EASPEAK.textMain,
                         maxWidth:80, overflow:"hidden", textOverflow:"ellipsis",
                         whiteSpace:"nowrap"}}>{p.name}</div>
            <div style={{fontSize:11, color:EASPEAK.gold, fontWeight:700}}>{p.xp} XP</div>
            <div style={{
              width:"100%", height:p.h,
              borderRadius:"10px 10px 0 0",
              background: p.gold
                ? `linear-gradient(180deg, ${EASPEAK.gold}, ${EASPEAK.orange})`
                : `linear-gradient(180deg, ${EASPEAK.purpleLt}, ${EASPEAK.purple})`,
              color:"#fff", fontWeight:800, fontSize:24,
              display:"flex", alignItems:"flex-start", justifyContent:"center",
              paddingTop:8,
            }}>{p.rank}</div>
          </div>
        ))}
      </div>

      {/* List */}
      <div style={{margin:"16px 14px 0", background:"#fff", borderRadius:20,
                   padding:"4px 6px",
                   boxShadow:"0 4px 16px rgba(0,0,0,.04)"}}>
        {others.filter(p => p.rank > 3 || p.isMe).map((p, i, arr) => (
          <div key={p.rank} style={{
            display:"flex", alignItems:"center", gap:12,
            padding:"12px 12px",
            borderTop: i === 0 ? "0" : `1px solid ${EASPEAK.border}80`,
            background: p.isMe ? `${EASPEAK.purple}0F` : "transparent",
            borderRadius: p.isMe ? 14 : 0,
          }}>
            <div style={{width:32, fontSize:14, fontWeight:700,
                         color:p.isMe ? EASPEAK.purple : EASPEAK.textGray,
                         textAlign:"center"}}>{p.rank}</div>
            <div style={{
              width:36, height:36, borderRadius:9999,
              background: p.isMe ? EASPEAK.purple : EASPEAK.purplePale,
              color: p.isMe ? "#fff" : EASPEAK.purple,
              display:"flex", alignItems:"center", justifyContent:"center",
              fontSize:14, fontWeight:700,
            }}>{p.name[0]}</div>
            <div style={{flex:1, fontSize:14, fontWeight:p.isMe ? 700 : 600,
                         color:EASPEAK.textMain,
                         display:"flex", alignItems:"center", gap:6}}>
              {p.name} <span style={{fontSize:14}}>{p.country || "🌍"}</span>
            </div>
            <div style={{fontSize:13, fontWeight:700, color:EASPEAK.gold,
                         display:"flex", alignItems:"center", gap:4}}>
              <IconSparkles size={14}/>{p.xp}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

window.LeaderboardScreen = LeaderboardScreen;
