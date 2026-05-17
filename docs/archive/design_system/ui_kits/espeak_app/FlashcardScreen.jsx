// FlashcardScreen.jsx — front/back flip card + SM-2 review buttons
function FlashcardScreen({ onExit }) {
  const [flipped, setFlipped] = React.useState(false);
  const [idx, setIdx] = React.useState(3);
  const total = 12;

  const next = () => {
    setFlipped(false);
    setIdx(i => Math.min(total, i+1));
  };

  return (
    <div style={{width:"100%", height:"100%",
                 background:`linear-gradient(180deg, ${EASPEAK.purplePale}, #fff)`,
                 display:"flex", flexDirection:"column"}}>
      {/* Top bar */}
      <div style={{padding:"14px 8px 8px",
                   display:"flex", alignItems:"center", gap:6}}>
        <button onClick={onExit} style={{
          width:44, height:44, border:0, background:"transparent",
          color:EASPEAK.textMain, cursor:"pointer",
          display:"flex", alignItems:"center", justifyContent:"center"}}>
          <IconClose size={22}/>
        </button>
        <div style={{flex:1, textAlign:"center"}}>
          <div style={{fontSize:11, color:EASPEAK.textGray, fontWeight:500}}>Изучение слов</div>
          <div style={{fontSize:14, fontWeight:700, color:EASPEAK.textMain}}>
            {idx} из {total}
          </div>
        </div>
        <div style={{width:44}}/>
      </div>

      {/* Progress */}
      <div style={{padding:"0 24px"}}>
        <div style={{height:4, borderRadius:2,
                     background:`${EASPEAK.purple}1F`, overflow:"hidden"}}>
          <div style={{height:"100%", width:`${(idx/total)*100}%`,
                       background:`linear-gradient(90deg, ${EASPEAK.purple}, ${EASPEAK.pink})`,
                       borderRadius:2,
                       transition:"width 320ms"}}/>
        </div>
      </div>

      {/* Card */}
      <div style={{flex:1, display:"flex", alignItems:"center", justifyContent:"center",
                   padding:"24px", perspective:1200}}>
        <div onClick={() => setFlipped(f => !f)}
             style={{
               width:"100%", maxWidth:320, aspectRatio:"3/4",
               position:"relative",
               transformStyle:"preserve-3d",
               transition:"transform 600ms cubic-bezier(.34,1.56,.64,1)",
               transform: flipped ? "rotateY(180deg)" : "rotateY(0)",
               cursor:"pointer",
             }}>
          {/* Front */}
          <CardFace style={{transform:"rotateY(0)"}}>
            <div style={{fontSize:11, fontWeight:700, color:EASPEAK.purple,
                         textTransform:"uppercase", letterSpacing:.5}}>Español</div>
            <div style={{fontSize:44, fontWeight:800, color:EASPEAK.textMain,
                         marginTop:14, textAlign:"center", letterSpacing:-1}}>
              aprender
            </div>
            <div style={{color:EASPEAK.purple, marginTop:12}}><IconVolume size={28}/></div>
            <div style={{flex:1}}/>
            <div style={{fontSize:13, color:EASPEAK.textGray}}>Нажми, чтобы перевернуть</div>
          </CardFace>
          {/* Back */}
          <CardFace style={{transform:"rotateY(180deg)"}}>
            <div style={{fontSize:11, fontWeight:700, color:EASPEAK.gold,
                         textTransform:"uppercase", letterSpacing:.5}}>Русский</div>
            <div style={{fontSize:32, fontWeight:800, color:EASPEAK.textMain,
                         marginTop:14, textAlign:"center"}}>
              учить, изучать
            </div>
            <div style={{fontSize:13, color:EASPEAK.textGray, marginTop:14, fontStyle:"italic",
                         textAlign:"center", lineHeight:1.5, padding:"0 8px"}}>
              Quiero aprender español.<br/>
              <span style={{color:EASPEAK.textSoft, fontStyle:"normal"}}>
                Я хочу учить испанский.
              </span>
            </div>
          </CardFace>
        </div>
      </div>

      {/* Review buttons or Show */}
      <div style={{padding:"0 20px 28px"}}>
        {!flipped ? (
          <button onClick={() => setFlipped(true)} style={{
            width:"100%", height:54, border:0, borderRadius:20,
            background:EASPEAK.purple, color:"#fff",
            fontSize:18, fontWeight:700, fontFamily:"inherit",
            cursor:"pointer",
            boxShadow:`0 6px 18px ${EASPEAK.purple}40`,
          }}>Показать перевод</button>
        ) : (
          <div style={{display:"flex", gap:8, animation:"riseIn 280ms ease-out both"}}>
            <ReviewBtn label="Забыл" sub="< 1 мин" color="#E53935" onClick={next}/>
            <ReviewBtn label="Помню" sub="3 дня"   color={EASPEAK.purple} onClick={next}/>
            <ReviewBtn label="Легко" sub="2 нед"   color="#2E7D32" onClick={next}/>
          </div>
        )}
      </div>

      <style>{`@keyframes riseIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:translateY(0)}}`}</style>
    </div>
  );
}

function CardFace({ style, children }) {
  return (
    <div style={{
      position:"absolute", inset:0,
      borderRadius:24, background:"#fff",
      boxShadow:"0 12px 40px rgba(123,47,190,.18)",
      backfaceVisibility:"hidden",
      WebkitBackfaceVisibility:"hidden",
      padding:"22px 18px",
      display:"flex", flexDirection:"column", alignItems:"center",
      ...style,
    }}>{children}</div>
  );
}

function ReviewBtn({ label, sub, color, onClick }) {
  return (
    <button onClick={onClick} style={{
      flex:1, height:64, border:0, borderRadius:18,
      background:`${color}14`, color,
      fontFamily:"inherit", cursor:"pointer",
      display:"flex", flexDirection:"column",
      alignItems:"center", justifyContent:"center", gap:2,
      borderTop:`2px solid ${color}`,
    }}>
      <div style={{fontSize:16, fontWeight:700}}>{label}</div>
      <div style={{fontSize:11, opacity:.75}}>{sub}</div>
    </button>
  );
}

window.FlashcardScreen = FlashcardScreen;
