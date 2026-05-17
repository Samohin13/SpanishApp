// Shared.jsx — EASPEAK shared primitives, colors, icons
// Loaded as Babel; exposes globals via window.

const EASPEAK = {
  // Sunset palette · approved
  // Legacy keys (purple/purpleLt/purplePale/pink) kept for back-compat —
  // they now point at sunset hues so every screen recolors at once.
  purple:    "#FF6B35",   // Orange · primary CTAs, nav, streak
  purpleLt:  "#FF8B5C",   // Orange-light · gradient stops
  purplePale:"#FFF1E6",   // Peach · pale tint backgrounds
  pink:      "#D62867",   // Magenta · accents, premium gradient end
  gold:      "#FFB400",   // Sun · XP, level
  orange:    "#FF6B35",   // alias to primary
  ocean:     "#264653",   // Headlines · text-1
  coral:     "#D62867",   // alias to magenta
  textMain:  "#264653",
  textGray:  "#8A8A93",
  textSoft:  "#5E5E66",
  lockGray:  "#C7C7CC",
  bgGray:    "#FAFAFC",
  bgGrayLt:  "#FFF8F2",
  border:    "#E8E5E0",
  white:     "#FFFFFF",
  statBg:    "#FFF1E6",
  // CEFR
  a1:        "#7C4DFF",
  a1Badge:   "#2E7D32",
  a2:        "#00BCD4",
  a2Badge:   "#0277BD",
  b1:        "#4CAF50",
  b1Badge:   "#E65100",
  b2:        "#6A1B9A",
  b2Badge:   "#6A1B9A",
};

// CEFR badge — colored 6px-radius block
function CefrBadge({ level }) {
  const map = {
    A1: EASPEAK.a1Badge, A2: EASPEAK.a2Badge,
    B1: EASPEAK.b1Badge, B2: EASPEAK.b2Badge,
  };
  const bg = map[level] || "#37474F";
  return (
    <span style={{
      display:"inline-block",
      background:bg + "D9", // 0.85α
      color:"#fff",
      fontSize:11, fontWeight:800, letterSpacing:.3,
      padding:"3px 9px", borderRadius:6,
    }}>{level}</span>
  );
}

// Stat pill — Lucide icon + bold value
function StatPill({ icon="sparkles", value, color }) {
  const Icon = icon === "flame" ? IconFlame : IconSparkles;
  return (
    <span style={{
      display:"inline-flex", alignItems:"center", gap:5,
      background:EASPEAK.statBg, color,
      padding:"6px 11px 6px 9px", borderRadius:9999,
      border:`1px solid ${color}4D`, // 0.3α
      fontSize:15, fontWeight:700,
      lineHeight:1,
    }}>
      <Icon size={15}/>
      {value}
    </span>
  );
}

// Lesson type chip — Lucide icon + label
function Chip({ kind="vocab", children }) {
  const palette = {
    vocab:    ["#E8F5E9", "#2E7D32", IconBookOpen,    "Теория"],
    grammar:  ["#E3F2FD", "#0277BD", IconType,        "Грамматика"],
    phrase:   ["#F3E5F5", "#6A1B9A", IconMessageCircle,"Фраза"],
    content:  ["#E8EAF6", "#283593", IconLayers,      "Контент"],
    practice: ["#FFF3E0", "#E65100", IconPencil,      "Практика"],
  };
  const [bg, color, Icon, defaultLabel] = palette[kind] || palette.vocab;
  return (
    <span style={{
      display:"inline-flex", alignItems:"center", gap:4,
      background:bg, color,
      fontSize:11, fontWeight:600,
      padding:"4px 9px 4px 7px", borderRadius:8,
      lineHeight:1.4,
    }}>
      <Icon size={12}/>{children || defaultLabel}
    </span>
  );
}

// Bottom nav — 5 tabs with spring-scale, haptic feedback, gliding pill, ripple
function BottomNav({ active, onChange }) {
  const tabs = [
    { id:"home",    label:"Главная",  icon:NavHome },
    { id:"games",   label:"Игры",     icon:NavGames },
    { id:"cards",   label:"Tarjetas", icon:NavCards },
    { id:"dict",    label:"Словарь",  icon:NavDict },
    { id:"profile", label:"Профиль",  icon:NavPerson },
  ];
  const [pressed, setPressed] = React.useState(null);
  const [ripple, setRipple]   = React.useState(null);
  const [bumpKey, setBumpKey] = React.useState(0);
  const activeIdx = Math.max(0, tabs.findIndex(t => t.id === active));

  const haptic = (style="light") => {
    // Web Vibration API — on iOS Safari this is a no-op; on Android it actually buzzes.
    // We also surface a visual haptic so the simulator feels right on desktop.
    try {
      if (navigator.vibrate) navigator.vibrate(style === "heavy" ? 18 : style === "medium" ? 10 : 6);
    } catch(e){}
  };

  const handleTap = (id, e) => {
    if (id === active) {
      // Re-tap on active — bounce + medium haptic
      haptic("medium"); setBumpKey(k => k+1); return;
    }
    haptic("light");
    setBumpKey(k => k+1);
    if (e && e.currentTarget) {
      const r = e.currentTarget.getBoundingClientRect();
      setRipple({ id, x: (e.clientX||r.left+r.width/2) - r.left, y: (e.clientY||r.top+r.height/2) - r.top, k: Date.now() });
      setTimeout(() => setRipple(null), 480);
    }
    onChange && onChange(id);
  };

  return (
    <div style={{
      width:"100%", height:64, position:"relative",
      background:EASPEAK.white,
      borderTop:`0.5px solid ${EASPEAK.border}`,
      display:"flex",
      boxShadow:"0 -4px 16px rgba(15, 4, 24, .04)",
    }}>
      {/* gliding pill behind active tab */}
      <div style={{
        position:"absolute", top:6, left:0,
        width:`${100/tabs.length}%`, height:44,
        transform:`translateX(${activeIdx*100}%)`,
        transition:"transform 460ms cubic-bezier(.34,1.4,.5,1)",
        display:"flex", justifyContent:"center", alignItems:"center",
        pointerEvents:"none",
      }}>
        <div style={{
          width:48, height:38, borderRadius:14,
          background:`linear-gradient(180deg, ${EASPEAK.purple}14 0%, ${EASPEAK.purple}07 100%)`,
          border:`1px solid ${EASPEAK.purple}22`,
        }}/>
      </div>
      {tabs.map((t, i) => {
        const isActive = t.id === active;
        const isPressed = pressed === t.id;
        const Icon = t.icon;
        // Spring scale: pressed → 0.88, active+just-tapped → 1.18 then settle 1.06
        const scale = isPressed ? 0.88 : (isActive ? 1.06 : 1);
        return (
          <div key={t.id}
            onPointerDown={() => setPressed(t.id)}
            onPointerUp={() => setPressed(null)}
            onPointerLeave={() => setPressed(null)}
            onClick={(e) => handleTap(t.id, e)}
            style={{
              flex:1, position:"relative", overflow:"hidden",
              display:"flex", flexDirection:"column",
              alignItems:"center", justifyContent:"center", gap:3,
              color: isActive ? EASPEAK.purple : "#9A9AA1",
              fontSize:10.5, fontWeight: isActive ? 700 : 500,
              cursor:"pointer", userSelect:"none",
              WebkitTapHighlightColor:"transparent",
            }}>
            {/* ripple */}
            {ripple && ripple.id === t.id && (
              <span key={ripple.k} style={{
                position:"absolute", left:ripple.x, top:ripple.y,
                width:8, height:8, borderRadius:"50%",
                background:`${EASPEAK.purple}33`,
                transform:"translate(-50%,-50%) scale(1)",
                animation:"espeak-ripple 460ms cubic-bezier(.2,.7,.3,1) forwards",
                pointerEvents:"none",
              }}/>
            )}
            <div
              key={isActive ? `act-${bumpKey}` : `idle-${t.id}`}
              style={{
                transform:`scale(${scale})`,
                transition:"transform 360ms cubic-bezier(.34,1.56,.5,1), color 200ms",
                animation: isActive ? "espeak-bump 460ms cubic-bezier(.34,1.56,.5,1)" : "none",
                display:"flex", alignItems:"center", justifyContent:"center",
                height:26,
              }}>
              <Icon filled={isActive} size={24}/>
            </div>
            <div style={{
              transform: isActive ? "translateY(0)" : "translateY(1px)",
              opacity: isActive ? 1 : 0.85,
              transition:"all 240ms",
              letterSpacing: isActive ? "0.01em" : 0,
            }}>{t.label}</div>
          </div>
        );
      })}
      <style>{`
        @keyframes espeak-bump {
          0%   { transform: scale(0.85); }
          45%  { transform: scale(1.22); }
          70%  { transform: scale(0.97); }
          100% { transform: scale(1.06); }
        }
        @keyframes espeak-ripple {
          0%   { width:8px; height:8px; opacity:.55; }
          100% { width:120px; height:120px; opacity:0; }
        }
      `}</style>
    </div>
  );
}

// ── Icons (Lucide-style strokes) ────────────────────────────
function NavHome({ filled, size=24 }) {
  return filled
    ? <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor"><path d="M12 3l9 8h-3v9h-4v-6h-4v6H6v-9H3z"/></svg>
    : <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 11l9-8 9 8M5 10v10h5v-6h4v6h5V10"/></svg>;
}
function NavGames({ filled, size=24 }) {
  return filled
    ? <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor"><rect x="3" y="7" width="18" height="11" rx="3"/><circle cx="9" cy="12.5" r="1.4" fill="#fff"/><rect x="6" y="11" width="6" height="3" rx="1.2" fill="#fff" opacity=".0"/><circle cx="15" cy="12" r="1" fill="#fff"/><circle cx="17.5" cy="13.5" r="1" fill="#fff"/></svg>
    : <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="7" width="18" height="11" rx="3"/><path d="M8 11v3M6 12.5h4M15 12h.01M17.5 13.5h.01"/></svg>;
}
function NavCards({ filled, size=24 }) {
  return filled
    ? <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor"><rect x="4" y="4" width="13" height="17" rx="2"/><path d="M19 6h2v15H8" stroke="currentColor" strokeWidth="2" fill="none"/></svg>
    : <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"><rect x="4" y="4" width="13" height="17" rx="2"/><path d="M8 1h11v15"/></svg>;
}
function NavDict({ filled, size=24 }) {
  return filled
    ? <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor"><path d="M4 4h7v16H4zM13 4h7v16h-7z"/></svg>
    : <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M4 4h7v16H4zM13 4h7v16h-7z"/></svg>;
}
function NavPerson({ filled, size=24 }) {
  return filled
    ? <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 4-7 8-7s8 3 8 7"/></svg>
    : <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 4-7 8-7s8 3 8 7"/></svg>;
}

function IconClose({ size=24 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M6 6l12 12M18 6L6 18"/></svg>;
}
function IconChevronLeft({ size=24 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M15 18l-6-6 6-6"/></svg>;
}
function IconChevronRight({ size=18 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 18l6-6-6-6"/></svg>;
}
function IconChevronDown({ size=22 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M6 9l6 6 6-6"/></svg>;
}
function IconLock({ size=18 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="5" y="11" width="14" height="9" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/></svg>;
}
function IconCheck({ size=18 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12l5 5 9-11"/></svg>;
}
function IconSettings({ size=22 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9 1.65 1.65 0 0 0 4.27 7.18l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.6 1.65 1.65 0 0 0 10 3.09V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>;
}
function IconVolume({ size=22 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M11 5L6 9H2v6h4l5 4z"/><path d="M15.5 8.5a5 5 0 0 1 0 7M19 5a9 9 0 0 1 0 14"/></svg>;
}
function IconTrophy({ size=20 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M17 4h3v3a3 3 0 0 1-3 3M7 4H4v3a3 3 0 0 0 3 3"/></svg>;
}
function IconFlame({ size=22 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 17c1.5 0 2.5-.75 2.5-2.5 0-1-.5-1.75-1-2.5C12 11 11 9.5 11 8c-2 1.5-3 3.5-3 5.5 0 .35.05.69.5 1z"/><path d="M14 9.5c.5 1 1 2 1 3.5C15 16 13 18 12 18.5c-3 1-7-1-7-5.5 0-3.5 3-6.5 5-9 1 2 4 3.5 4 5.5z"/></svg>;
}
function IconSparkles({ size=22 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 3l1.7 4.5L18 9l-4.3 1.5L12 15l-1.7-4.5L6 9l4.3-1.5z"/><path d="M19 15l.8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8z"/></svg>;
}
function IconBookOpen({ size=22 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M2 4h6a4 4 0 0 1 4 4v12a3 3 0 0 0-3-3H2zM22 4h-6a4 4 0 0 0-4 4v12a3 3 0 0 1 3-3h7z"/></svg>;
}
function IconType({ size=18 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 7V5h16v2M9 20h6M12 5v15"/></svg>;
}
function IconMessageCircle({ size=18 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 11.5a8.4 8.4 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.4 8.4 0 0 1-3.8-.9L3 21l1.9-5.7a8.4 8.4 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.4 8.4 0 0 1 3.8-.9h.5a8.5 8.5 0 0 1 8 8z"/></svg>;
}
function IconLayers({ size=18 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 2l9 5-9 5-9-5z"/><path d="M3 17l9 5 9-5M3 12l9 5 9-5"/></svg>;
}
function IconPencil({ size=14 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20h9M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z"/></svg>;
}
function IconRocket({ size=40 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M4.5 16.5a4.5 4.5 0 0 0 3 3L9 17l-2-2zM12 15l-3-3a22 22 0 0 1 5-9 12 12 0 0 1 7 7 22 22 0 0 1-9 5z"/><circle cx="15" cy="9" r="1.5"/><path d="M9 12H5l3-4h3M12 15v4l4-3"/></svg>;
}
function IconGlobe({ size=40 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15 15 0 0 1 0 20 15 15 0 0 1 0-20z"/></svg>;
}
function IconBooks({ size=40 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M4 4h4v16H4zM10 4h4v16h-4zM16.5 5.5l3.5 1-3.5 14-3.5-1z"/></svg>;
}
function IconGraduation({ size=40 }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M2 10l10-5 10 5-10 5z"/><path d="M6 12v5a8 4 0 0 0 12 0v-5M22 10v6"/></svg>;
}

Object.assign(window, {
  EASPEAK,
  CefrBadge, StatPill, Chip, BottomNav,
  IconClose, IconChevronLeft, IconChevronRight, IconChevronDown,
  IconLock, IconCheck, IconSettings, IconVolume, IconTrophy,
  IconFlame, IconSparkles, IconBookOpen, IconType, IconMessageCircle,
  IconLayers, IconPencil, IconRocket, IconGlobe, IconBooks, IconGraduation,
});
