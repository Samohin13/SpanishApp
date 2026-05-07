// HomeScreen.jsx — header, greeting, streak card, word-of-day, course cards
function HomeScreen({ user, onOpenCourse, onOpenProfile }) {
  return (
    <div style={{
      width:"100%", height:"100%",
      background:EASPEAK.bgGray,
      overflowY:"auto",
      paddingBottom:24,
    }}>
      {/* Header */}
      <div style={{background:"#fff", padding:"14px 20px",
                   display:"flex", alignItems:"center",
                   justifyContent:"space-between"}}>
        <div onClick={onOpenProfile} style={{
          width:44, height:44, borderRadius:9999,
          background:EASPEAK.purple, color:"#fff",
          display:"flex", alignItems:"center", justifyContent:"center",
          fontSize:18, fontWeight:700, cursor:"pointer",
          boxShadow:"0 2px 6px rgba(123,47,190,.25)",
        }}>{(user?.name || "?")[0].toUpperCase()}</div>
        <div style={{display:"flex", gap:8}}>
          <StatPill icon="sparkles" value={user?.xp ?? 1240} color={EASPEAK.gold}/>
          <StatPill icon="flame"    value={user?.streak ?? 12} color={EASPEAK.orange}/>
        </div>
      </div>

      {/* Greeting */}
      <div style={{background:"#fff", padding:"2px 20px 20px"}}>
        <div style={{fontSize:26, fontWeight:700, color:EASPEAK.textMain}}>
          Привет, {user?.name || "Друг"}! 👋
        </div>
        <div style={{fontSize:15, color:EASPEAK.textGray, marginTop:2}}>
          Продолжай изучать испанский язык
        </div>
      </div>

      <div style={{height:16}}/>

      {/* Streak card */}
      <StreakCard streak={user?.streak ?? 12} todayMin={8} goalMin={10}/>

      <div style={{height:12}}/>

      {/* Word of day card */}
      <WordOfDayCard/>

      <div style={{height:12}}/>

      {/* Course cards */}
      <CourseCard level="A1" title="Начинающий"     Icon={IconRocket}     color={EASPEAK.a1}
                  subtitle="60 микро-уроков · 4 блока" units={4}
                  onClick={() => onOpenCourse && onOpenCourse("A1")}/>
      <div style={{height:12}}/>
      <CourseCard level="A2" title="Элементарный"   Icon={IconGlobe}      color={EASPEAK.a2}
                  subtitle="60 уроков · 4 блока" units={4}
                  locked onClick={() => onOpenCourse && onOpenCourse("A2")}/>
      <div style={{height:12}}/>
      <CourseCard level="B1" title="Средний"        Icon={IconBooks}      color={EASPEAK.b1}
                  subtitle="скоро · 4 блока" units={4} locked/>
      <div style={{height:12}}/>
      <CourseCard level="B2" title="Выше среднего"  Icon={IconGraduation} color={EASPEAK.b2}
                  subtitle="скоро · 4 блока" units={4} locked/>
    </div>
  );
}

function StreakCard({ streak, todayMin, goalMin }) {
  const pct = Math.min(100, (todayMin / goalMin) * 100);
  return (
    <div style={{margin:"0 14px", background:"#fff", borderRadius:20,
                 padding:"14px 16px", boxShadow:"0 6px 20px rgba(255,107,0,.10)",
                 display:"flex", flexDirection:"column", gap:8}}>
      <div style={{display:"flex", alignItems:"center", gap:12}}>
        <div style={{
          width:44, height:44, borderRadius:9999,
          background:`linear-gradient(135deg, ${EASPEAK.orange}, ${EASPEAK.gold})`,
          color:"#fff",
          display:"flex", alignItems:"center", justifyContent:"center",
          animation:"flame 700ms ease-in-out infinite alternate",
        }}>
          <IconFlame size={24}/>
        </div>
        <div>
          <span style={{fontSize:28, fontWeight:800, color:EASPEAK.orange}}>{streak}</span>
          <span style={{fontSize:14, color:EASPEAK.textSoft, fontWeight:500, marginLeft:6}}>
            дней подряд
          </span>
        </div>
        <div style={{flex:1}}/>
        <div style={{
          background:EASPEAK.statBg, color:EASPEAK.orange,
          fontSize:11, fontWeight:700, padding:"4px 10px", borderRadius:9999,
        }}>Личный рекорд</div>
      </div>
      <div style={{height:7, borderRadius:4,
                   background:"rgba(255,107,0,.12)", overflow:"hidden"}}>
        <div style={{height:"100%", width:`${pct}%`, borderRadius:4,
                     background:`linear-gradient(90deg, ${EASPEAK.orange}, ${EASPEAK.gold})`}}/>
      </div>
      <div style={{fontSize:12, color:"#2E7D32", fontWeight:500,
                   display:"flex", alignItems:"center", gap:4}}>
        <IconCheck size={14}/> Сегодня занимался {todayMin} / {goalMin} мин
      </div>
      <style>{`@keyframes flame{from{transform:scale(1)}to{transform:scale(1.10)}}`}</style>
    </div>
  );
}

function WordOfDayCard() {
  return (
    <div style={{margin:"0 14px", background:"#fff", borderRadius:20,
                 padding:"14px 16px", boxShadow:"0 6px 20px rgba(123,47,190,.10)"}}>
      <div style={{fontSize:11, fontWeight:700, color:EASPEAK.purple,
                   textTransform:"uppercase", letterSpacing:.5, marginBottom:6}}>
        Слово дня
      </div>
      <div style={{display:"flex", alignItems:"baseline", gap:10}}>
        <div style={{fontSize:28, fontWeight:800, color:EASPEAK.textMain}}>aprender</div>
        <div style={{color:EASPEAK.purple, cursor:"pointer"}}><IconVolume size={18}/></div>
      </div>
      <div style={{fontSize:15, color:EASPEAK.textSoft, marginTop:2}}>учить, изучать</div>
      <div style={{fontSize:12, color:EASPEAK.textGray, fontStyle:"italic", marginTop:6,
                   lineHeight:1.45}}>
        Quiero aprender español. — <span style={{color:EASPEAK.textSoft}}>Я хочу учить испанский.</span>
      </div>
    </div>
  );
}

function CourseCard({ level, title, Icon, color, subtitle, units, locked, onClick }) {
  const accent = locked ? EASPEAK.lockGray : color;
  return (
    <div onClick={onClick} style={{
      margin:"0 14px", background:"#fff", borderRadius:20,
      overflow:"hidden", cursor:"pointer",
      boxShadow: locked
        ? `0 2px 8px ${accent}40`
        : `0 6px 24px ${accent}59`,
    }}>
      <div style={{
        height:100, padding:"0 18px", color:"#fff",
        display:"flex", alignItems:"center", justifyContent:"space-between",
        background: locked
          ? "linear-gradient(90deg,#DDDDDD,#CCCCCC)"
          : `linear-gradient(90deg, ${accent}, ${accent}B8)`,
      }}>
        <div>
          <div style={{lineHeight:1, marginBottom:8, color:"rgba(255,255,255,.95)"}}>
            <Icon size={36}/>
          </div>
          <div style={{display:"flex", alignItems:"center", gap:8}}>
            <CefrBadge level={level}/>
            <div style={{fontSize:16, fontWeight:800}}>{title}</div>
          </div>
        </div>
        {locked
          ? <div style={{color:"rgba(255,255,255,.8)"}}><IconLock size={24}/></div>
          : <div style={{textAlign:"right"}}>
              <div style={{fontSize:14, fontWeight:700}}>{units} блоков</div>
              <div style={{fontSize:11, opacity:.8}}>60 уроков</div>
            </div>
        }
      </div>
      <div style={{padding:"14px 18px"}}>
        <div style={{fontSize:13, color:locked ? EASPEAK.textGray + "99" : EASPEAK.textGray,
                     lineHeight:1.4}}>{subtitle}</div>
        <div style={{height:7, borderRadius:4, marginTop:12,
                     background: `${accent}26`}}/>
        <div style={{fontSize:12, fontWeight:600, color:accent, marginTop:8}}>
          {locked ? "Заблокировано" : "Начать обучение →"}
        </div>
      </div>
    </div>
  );
}

window.HomeScreen = HomeScreen;
