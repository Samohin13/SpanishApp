// SettingsScreen.jsx — preferences, account, support
function SettingsScreen({ user, onBack }) {
  const [notif, setNotif] = React.useState(true);
  const [sound, setSound] = React.useState(true);
  const [haptics, setHaptics] = React.useState(false);
  const [dark, setDark] = React.useState(false);

  const Section = ({ title, children }) => (
    <div style={{margin:"18px 14px 0"}}>
      <div style={{fontSize:11, fontWeight:700, color:EASPEAK.textGray,
                   textTransform:"uppercase", letterSpacing:.5,
                   padding:"0 6px 8px"}}>{title}</div>
      <div style={{background:"#fff", borderRadius:18,
                   boxShadow:"0 2px 10px rgba(0,0,0,.03)",
                   overflow:"hidden"}}>
        {children}
      </div>
    </div>
  );

  const Row = ({ icon, color, label, sub, right, last }) => (
    <div style={{
      display:"flex", alignItems:"center", gap:12,
      padding:"13px 14px",
      borderBottom: last ? "0" : `1px solid ${EASPEAK.border}80`,
    }}>
      <div style={{
        width:34, height:34, borderRadius:9,
        background:`${color}1A`, color,
        display:"flex", alignItems:"center", justifyContent:"center",
      }}>{icon}</div>
      <div style={{flex:1}}>
        <div style={{fontSize:14, fontWeight:600, color:EASPEAK.textMain}}>{label}</div>
        {sub && <div style={{fontSize:12, color:EASPEAK.textGray, marginTop:1}}>{sub}</div>}
      </div>
      {right}
    </div>
  );

  const Toggle = ({ on, onChange }) => (
    <button onClick={() => onChange(!on)} style={{
      width:44, height:26, borderRadius:9999, border:0,
      background: on ? EASPEAK.purple : EASPEAK.border,
      position:"relative", cursor:"pointer", padding:0,
      transition:"background .2s",
    }}>
      <div style={{
        position:"absolute", top:3, left: on ? 21 : 3,
        width:20, height:20, borderRadius:9999, background:"#fff",
        boxShadow:"0 2px 4px rgba(0,0,0,.2)", transition:"left .2s",
      }}/>
    </button>
  );

  const Chevron = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke={EASPEAK.textGray} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
  );

  return (
    <div style={{width:"100%", height:"100%", background:EASPEAK.bgGray, overflowY:"auto", paddingBottom:24}}>
      {/* Header */}
      <div style={{background:"#fff", padding:"14px 8px",
                   display:"flex", alignItems:"center", gap:6,
                   borderBottom:`1px solid ${EASPEAK.border}80`}}>
        <button onClick={onBack} style={{
          width:40, height:40, border:0, background:"transparent",
          color:EASPEAK.textMain, cursor:"pointer",
          display:"flex", alignItems:"center", justifyContent:"center",
        }}><IconChevronLeft size={22}/></button>
        <div style={{fontSize:18, fontWeight:700, color:EASPEAK.textMain}}>Настройки</div>
      </div>

      {/* Profile card */}
      <div style={{margin:"14px 14px 0",
                   background:`linear-gradient(135deg, ${EASPEAK.purple}, ${EASPEAK.pink})`,
                   borderRadius:20, padding:"16px",
                   color:"#fff", display:"flex", alignItems:"center", gap:12}}>
        <div style={{
          width:54, height:54, borderRadius:9999,
          background:"rgba(255,255,255,.2)",
          display:"flex", alignItems:"center", justifyContent:"center",
          fontSize:22, fontWeight:800,
        }}>{(user?.name || "Т")[0]}</div>
        <div style={{flex:1}}>
          <div style={{fontSize:16, fontWeight:700}}>{user?.name || "Ты"}</div>
          <div style={{fontSize:12, opacity:.85, marginTop:2}}>
            🥈 Серебряная лига · 1 240 XP
          </div>
        </div>
        <button style={{
          padding:"8px 12px", border:"1.5px solid rgba(255,255,255,.5)",
          borderRadius:9999, background:"transparent", color:"#fff",
          fontSize:12, fontWeight:600, fontFamily:"inherit", cursor:"pointer",
        }}>Изменить</button>
      </div>

      <Section title="Обучение">
        <Row icon={<IconBook size={18}/>}  color={EASPEAK.purple}
             label="Цель в день" sub="20 минут" right={<Chevron/>}/>
        <Row icon={<IconFlame size={18}/>} color={EASPEAK.coral}
             label="Напоминания об ударе" sub="20:00 каждый день"
             right={<Toggle on={notif} onChange={setNotif}/>}/>
        <Row icon={<IconVolume size={18}/>} color={EASPEAK.pink}
             label="Звук" right={<Toggle on={sound} onChange={setSound}/>}/>
        <Row icon={<IconSparkles size={18}/>} color={EASPEAK.gold}
             label="Вибрация" right={<Toggle on={haptics} onChange={setHaptics}/>} last/>
      </Section>

      <Section title="Аккаунт">
        <Row icon={<IconUser size={18}/>}    color={EASPEAK.purple}
             label="Профиль" sub="Имя, аватар, страна" right={<Chevron/>}/>
        <Row icon={<IconCrown size={18}/>}   color={EASPEAK.gold}
             label="EASPEAK Premium" sub="Без рекламы и без лимитов"
             right={<span style={{
               padding:"4px 10px", borderRadius:9999,
               background:`linear-gradient(135deg, ${EASPEAK.gold}, ${EASPEAK.orange})`,
               color:"#fff", fontSize:11, fontWeight:700,
             }}>PRO</span>}/>
        <Row icon={<span style={{fontSize:16}}>🌙</span>} color={EASPEAK.purple}
             label="Тёмная тема" right={<Toggle on={dark} onChange={setDark}/>} last/>
      </Section>

      <Section title="Поддержка">
        <Row icon={<span style={{fontSize:14, fontWeight:700}}>?</span>} color={EASPEAK.pink}
             label="Помощь и FAQ" right={<Chevron/>}/>
        <Row icon={<span style={{fontSize:14}}>✉</span>} color={EASPEAK.purple}
             label="Связаться с нами" sub="hola@easpeak.app" right={<Chevron/>}/>
        <Row icon={<span style={{fontSize:14}}>★</span>} color={EASPEAK.gold}
             label="Оценить приложение" right={<Chevron/>} last/>
      </Section>

      <div style={{textAlign:"center", padding:"24px 16px 8px",
                   fontSize:12, color:EASPEAK.textGray}}>
        EASPEAK · v1.4.2
      </div>
      <div style={{textAlign:"center", padding:"0 16px 8px",
                   fontSize:13, color:EASPEAK.coral, fontWeight:600}}>
        Выйти из аккаунта
      </div>
    </div>
  );
}

window.SettingsScreen = SettingsScreen;
