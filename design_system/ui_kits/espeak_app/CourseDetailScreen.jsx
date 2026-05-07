// CourseDetailScreen.jsx — Path-to-Madrid card + roadmap of TopicCards
function CourseDetailScreen({ level, onBack, onOpenLesson }) {
  const [expandedId, setExpandedId] = React.useState("01");

  const blocks = level === "A1" ? [
    { id:"01", title:"Взлёт", Icon:IconRocket, desc:"Алфавит, приветствие, числа до 20, простые глаголы.",
      progress:.4, locked:false, lessons:[
        { title:"Алфавит и произношение", type:"vocab", isCompleted:true },
        { title:"¡Hola! Знакомство", type:"phrase", isCompleted:true },
        { title:"Глагол ser — быть", type:"grammar", isCompleted:false },
        { title:"Числа от 1 до 20", type:"vocab", isCompleted:false, isPremium:true },
      ]},
    { id:"02", title:"Мой мир", Icon:IconBookOpen, desc:"Семья, дом, работа, повседневная жизнь.",
      progress:0, locked:false, lessons:[] },
    { id:"03", title:"Действие", Icon:IconLayers, desc:"Глаголы, времена, простые предложения.",
      progress:0, locked:true, lessons:[] },
    { id:"04", title:"Выживание", Icon:IconGlobe, desc:"Аэропорт, отель, ресторан, помощь.",
      progress:0, locked:true, lessons:[] },
  ] : [];

  return (
    <div style={{width:"100%", height:"100%", background:EASPEAK.bgGray, overflowY:"auto", paddingBottom:24}}>
      {/* Top bar */}
      <div style={{background:"#fff", padding:"12px 8px 12px 4px",
                   display:"flex", alignItems:"center", gap:6}}>
        <button onClick={onBack} style={{
          width:44, height:44, border:0, background:"transparent",
          color:EASPEAK.textMain, cursor:"pointer",
          display:"flex", alignItems:"center", justifyContent:"center"}}>
          <IconChevronLeft size={24}/>
        </button>
        <div style={{flex:1}}>
          <div style={{fontSize:11, color:EASPEAK.textGray, fontWeight:500}}>Уровень</div>
          <div style={{fontSize:18, fontWeight:800, color:EASPEAK.textMain,
                       display:"flex", alignItems:"center", gap:8}}>
            <CefrBadge level={level}/> Начинающий
          </div>
        </div>
        <div style={{padding:"0 14px", color:EASPEAK.textGray}}>
          <IconSettings size={22}/>
        </div>
      </div>

      <div style={{height:14}}/>

      {/* Path to Madrid */}
      <div style={{margin:"0 14px", background:"#fff", borderRadius:28,
                   overflow:"hidden",
                   boxShadow:"0 6px 24px rgba(123,47,190,.18)"}}>
        <div style={{padding:"16px 18px",
                     background:`linear-gradient(135deg, ${EASPEAK.purple}, ${EASPEAK.pink})`,
                     color:"#fff"}}>
          <div style={{fontSize:11, fontWeight:700, letterSpacing:.5,
                       textTransform:"uppercase", opacity:.85}}>Путь до Мадрида</div>
          <div style={{fontSize:18, fontWeight:800, marginTop:2}}>
            🏰 Aldea perdida → 🏛️ Madrid
          </div>
          <div style={{fontSize:12, opacity:.85, marginTop:4}}>
            Следующая остановка: 🌅 Sevilla
          </div>
          <div style={{height:6, borderRadius:3, marginTop:10,
                       background:"rgba(255,255,255,.25)", overflow:"hidden"}}>
            <div style={{height:"100%", width:"22%",
                         background:"#fff", borderRadius:3}}/>
          </div>
        </div>
      </div>

      <div style={{height:16}}/>

      <div style={{padding:"0 18px 8px", fontSize:11, fontWeight:700,
                   letterSpacing:.5, textTransform:"uppercase",
                   color:EASPEAK.textGray}}>Дорожная карта</div>

      {blocks.map(b => (
        <React.Fragment key={b.id}>
          <TopicCard block={b} levelCode={level}
                     expanded={expandedId === b.id}
                     onToggle={() => setExpandedId(expandedId === b.id ? null : b.id)}
                     onLesson={onOpenLesson}/>
          <div style={{height:12}}/>
        </React.Fragment>
      ))}
    </div>
  );
}

function TopicCard({ block, levelCode, expanded, onToggle, onLesson }) {
  const accent = block.locked ? EASPEAK.lockGray : EASPEAK.a1;
  const completed = block.lessons.filter(l => l.isCompleted).length;
  const total = block.lessons.length;

  return (
    <div style={{margin:"0 14px", background:"#fff", borderRadius:20, overflow:"hidden",
                 boxShadow: block.locked
                   ? "0 2px 10px rgba(0,0,0,.08)"
                   : `0 6px 24px ${accent}59`}}>
      {/* Header */}
      <div onClick={onToggle} style={{
        height:72, padding:"0 18px", color:"#fff", cursor:"pointer",
        display:"flex", alignItems:"center", justifyContent:"space-between",
        background: block.locked
          ? "linear-gradient(90deg,#DDDDDD,#CCCCCC)"
          : `linear-gradient(90deg, ${accent}, ${accent}B8)`,
      }}>
        <div style={{display:"flex", alignItems:"center", gap:12}}>
          <div style={{width:44, height:44, borderRadius:9999,
                       background:"rgba(255,255,255,.25)",
                       color:"#fff",
                       display:"flex", alignItems:"center", justifyContent:"center"}}>
            {block.Icon ? <block.Icon size={24}/> : null}
          </div>
          <div>
            <div style={{fontSize:11, fontWeight:500, opacity:.8}}>Блок {block.id}</div>
            <div style={{fontSize:18, fontWeight:800}}>{block.title}</div>
          </div>
        </div>
        <div style={{display:"flex", flexDirection:"column", alignItems:"flex-end", gap:4}}>
          <CefrBadge level={levelCode}/>
          {block.locked
            ? <div style={{color:"rgba(255,255,255,.8)"}}><IconLock size={18}/></div>
            : <div style={{fontSize:12, fontWeight:700}}>{completed}/{total || 4}</div>}
        </div>
      </div>

      {/* Body */}
      <div style={{padding:"14px 18px"}}>
        <div style={{fontSize:13, color:block.locked ? `${EASPEAK.textGray}99` : EASPEAK.textGray,
                     lineHeight:1.4}}>{block.desc}</div>
        <div style={{display:"flex", alignItems:"center", gap:10, marginTop:10}}>
          <div style={{flex:1, height:7, borderRadius:4,
                       background:`${accent}26`, overflow:"hidden"}}>
            <div style={{height:"100%", width:`${block.progress*100}%`,
                         borderRadius:4,
                         background:`linear-gradient(90deg, ${accent}, ${accent}B8)`}}/>
          </div>
          {block.locked
            ? <span style={{fontSize:11, color:EASPEAK.lockGray}}>Заблокировано</span>
            : <span style={{fontSize:12, fontWeight:600, color:accent}}>
                {Math.round(block.progress*100)}%
              </span>}
          <div style={{color: block.locked ? EASPEAK.lockGray : accent,
                       transform: expanded ? "rotate(180deg)" : "rotate(0)",
                       transition:"transform 220ms"}}>
            <IconChevronDown size={22}/>
          </div>
        </div>

        {/* Expanded lessons */}
        {expanded && !block.locked && (
          <div style={{marginTop:14, display:"flex", flexDirection:"column", gap:8,
                       borderTop:`1px solid ${accent}1F`, paddingTop:12}}>
            {(block.lessons.length ? block.lessons : [
              {title:"Скоро", type:"content"}
            ]).map((l, i) => (
              <SubLessonRow key={i} number={i+1} lesson={l}
                            accent={accent} onClick={() => onLesson && onLesson()}/>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

function SubLessonRow({ number, lesson, accent, onClick }) {
  const locked = lesson.isPremium;
  return (
    <div onClick={locked ? null : onClick} style={{
      background: locked ? "#F7F7F7" : "#FAFAFF",
      borderRadius:14, padding:"12px 14px",
      display:"flex", alignItems:"center", gap:12,
      boxShadow: locked ? "none" : "0 1px 2px rgba(0,0,0,.04)",
      cursor: locked ? "default" : "pointer",
    }}>
      <div style={{
        width:36, height:36, borderRadius:9999,
        display:"flex", alignItems:"center", justifyContent:"center",
        background: lesson.isCompleted ? accent
                  : locked ? "#E0E0E0" : `${accent}1F`,
        color: lesson.isCompleted ? "#fff" : (locked ? EASPEAK.lockGray : accent),
        fontSize:14, fontWeight:700,
      }}>
        {lesson.isCompleted ? <IconCheck size={18}/> : number}
      </div>
      <div style={{flex:1, fontSize:14,
                   fontWeight: lesson.isCompleted ? 400 : 500,
                   color: locked ? `${EASPEAK.textGray}8C` : EASPEAK.textMain,
                   lineHeight:1.3}}>
        {lesson.title}
      </div>
      {!locked && (
        <div style={{display:"flex", flexDirection:"column", gap:4}}>
          <Chip kind={lesson.type}/>
          <Chip kind="practice"/>
        </div>
      )}
      {locked
        ? <div style={{color:EASPEAK.gold}}><IconLock size={15}/></div>
        : !lesson.isCompleted && <div style={{color:accent}}><IconChevronRight size={18}/></div>}
    </div>
  );
}

window.CourseDetailScreen = CourseDetailScreen;
