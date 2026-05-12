"""240x150 covers for 3 Boosty tiers — editorial minimalism, no illustrations.

Design system:
  - Premium gradient per tier (no cartoon scenes)
  - ESPEAK bull mascot in corner, small
  - Tier number top-right (01 / 02 / 03)
  - Big tier name as the hero, serif or heavy sans
  - Thin accent line in brand orange
  - Russian subtitle + price at bottom, small caps tracking
"""
from svglib.svglib import svg2rlg
from reportlab.graphics import renderPM
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import numpy as np
import io, os

# Render at 4× resolution for crisp text + smooth gradient on HiDPI displays.
# Boosty downscales to 240×150 on the page, but stores the original for retina.
SCALE = 4
W, H = 240 * SCALE, 150 * SCALE


def s(v):
    """Scale a pixel value to high-res canvas."""
    return int(v * SCALE)

BULL_SVG = '<?xml version="1.0" encoding="UTF-8"?>\n<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" viewBox="0 0 512 512"><rect width="512" height="512" fill="#FF00FF"/><path fill="#FFFFFF" d="M30.882,30.14s33.525,-10.81 92.088,19.64c60.996,31.72 85.628,116.77 132.906,117.86c47.283,-1.09 71.92,-86.14 132.912,-117.858c58.558,-30.45 92.088,-19.643 92.088,-19.643v85.483s-38.062,-2.453 -58.934,13.507c-15.165,11.593 -45.23,54.296 -71.375,80.08c38.867,27.833 63.966,71.877 63.966,121.45c0,84.162 -72.343,152.39 -161.587,152.39S91.353,414.821 91.353,330.659c0,-51.03 26.6,-96.205 67.432,-123.865c-25.558,-25.957 -54.263,-66.43 -68.965,-77.67c-20.877,-15.96 -58.938,-13.506 -58.938,-13.506zM179.45,330.49c0,40.01 32.98,72.44 73.664,72.44s73.67,-32.435 73.67,-72.44s-32.98,-72.44 -73.67,-72.44c-40.688,0 -73.664,32.436 -73.664,72.44"/></svg>'


def find_font(candidates):
    for p in candidates:
        if os.path.exists(p): return p
    return None

# Prefer Georgia / Constantia for editorial serif feel.
# Fall back to heavy sans for the body if serif not available.
font_serif_bold = find_font([
    'C:/Windows/Fonts/georgiab.ttf',
    'C:/Windows/Fonts/constanb.ttf',
    'C:/Windows/Fonts/timesbd.ttf',
])
font_serif_italic = find_font([
    'C:/Windows/Fonts/georgiai.ttf',
    'C:/Windows/Fonts/constani.ttf',
])
font_sans_black = find_font([
    'C:/Windows/Fonts/seguibl.ttf',
    'C:/Windows/Fonts/arialbd.ttf',
])
font_sans_med = find_font([
    'C:/Windows/Fonts/seguisb.ttf',
    'C:/Windows/Fonts/arial.ttf',
])
font_sans_reg = find_font([
    'C:/Windows/Fonts/arial.ttf',
    'C:/Windows/Fonts/segoeui.ttf',
])


def make_bull(fill):
    drawing = svg2rlg(io.BytesIO(BULL_SVG.encode('utf-8')))
    tmp = 'docs/play_assets/_bull_tmp.png'
    renderPM.drawToFile(drawing, tmp, fmt='PNG', dpi=72)
    bull = Image.open(tmp).convert('RGBA')
    data = list(bull.getdata())
    out = []
    for r, g, b, a in data:
        if r > b * 0.9 and g < 200 and abs(r - b) < 80 and r > 150 and b > 150 and g < r * 0.7:
            out.append((0, 0, 0, 0))
        else:
            out.append((*fill, a))
    bull.putdata(out)
    os.remove(tmp)
    return bull


def diag_gradient(top_left, bottom_right):
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    dx = xx / W; dy = yy / H
    t = np.clip(dx * 0.45 + dy * 0.55, 0.0, 1.0)
    r = (top_left[0] + (bottom_right[0] - top_left[0]) * t).astype(np.uint8)
    g = (top_left[1] + (bottom_right[1] - top_left[1]) * t).astype(np.uint8)
    b = (top_left[2] + (bottom_right[2] - top_left[2]) * t).astype(np.uint8)
    a = np.full_like(r, 255)
    return Image.fromarray(np.dstack([r, g, b, a]), 'RGBA')


def grain(img, amount=8):
    """Subtle film grain for paper-like texture."""
    rng = np.random.default_rng(0)
    noise = rng.integers(-amount, amount, size=(H, W), dtype=np.int16)
    arr = np.array(img).astype(np.int16)
    arr[:, :, :3] = np.clip(arr[:, :, :3] + noise[:, :, None], 0, 255)
    return Image.fromarray(arr.astype(np.uint8), 'RGBA')


def render(name_es, name_ru, price, tier_num,
           bg_top, bg_bot, ink, accent, bull_color, output,
           italic_name=False):
    img = diag_gradient(bg_top, bg_bot)
    img = grain(img, amount=4)

    draw = ImageDraw.Draw(img)

    # ── Top-left: tiny ESPEAK label + bull ──
    bull = make_bull(bull_color)
    bull.thumbnail((s(18), s(18)), Image.LANCZOS)
    img.paste(bull, (s(14), s(12)), bull)

    f_label = ImageFont.truetype(font_sans_med, s(9))
    draw.text((s(36), s(16)), "E S P E A K", fill=ink + (220,), font=f_label,
              spacing=s(2))

    # ── Top-right: tier number, large, ghosted ──
    f_num = ImageFont.truetype(font_serif_bold, s(24))
    num_text = tier_num
    nb = draw.textbbox((0, 0), num_text, font=f_num)
    nw = nb[2] - nb[0]
    draw.text((W - nw - s(16), s(10)), num_text, fill=ink + (110,), font=f_num)

    # ── Thin accent line under header ──
    draw.line([(s(14), s(38)), (W - s(14), s(38))],
              fill=accent + (110,), width=max(1, s(1)))

    # ── Hero: tier name — auto-fit so it always fits ──
    title_font_path = font_serif_italic if italic_name else font_serif_bold
    max_w = W - s(28) - s(16)
    size = 40
    while size > 22:
        f_title = ImageFont.truetype(title_font_path, s(size))
        tb = draw.textbbox((0, 0), name_es, font=f_title)
        if (tb[2] - tb[0]) <= max_w:
            break
        size -= 2
    title_y = s(50) + (s(40) - s(size)) // 3
    draw.text((s(14), title_y), name_es, fill=ink + (255,), font=f_title)

    # ── Bottom: subtitle (uppercase, tracked) + price aligned right ──
    f_sub = ImageFont.truetype(font_sans_med, s(9))
    sub_text = name_ru.upper()
    spaced = "  ".join(list(sub_text))
    draw.text((s(14), H - s(22)), spaced, fill=ink + (190,), font=f_sub)

    f_price = ImageFont.truetype(font_serif_bold, s(14))
    price_text = f"₽{price}"
    pb = draw.textbbox((0, 0), price_text, font=f_price)
    pw = pb[2] - pb[0]
    draw.text((W - pw - s(14), H - s(28)), price_text,
              fill=accent + (255,), font=f_price)
    # /mes mark
    f_mes = ImageFont.truetype(font_sans_reg, s(8))
    mes_text = "/ mes"
    mb = draw.textbbox((0, 0), mes_text, font=f_mes)
    mw = mb[2] - mb[0]
    draw.text((W - mw - s(14), H - s(11)), mes_text,
              fill=ink + (150,), font=f_mes)

    img.convert('RGB').save(output, 'PNG', optimize=True)
    print('Wrote', output, f'{W}×{H}', os.path.getsize(output), 'bytes')


# ── TIER 1: HOLA — cream paper, terracotta ink ──
# Warm, welcoming, like a Spanish café menu.
render(
    name_es="Hola.",
    name_ru="первый шаг",
    price=150,
    tier_num="01",
    bg_top=(247, 234, 215),       # warm cream
    bg_bot=(232, 207, 178),       # pale clay
    ink=(82, 38, 22),             # deep terracotta ink
    accent=(195, 80, 40),         # brand orange
    bull_color=(82, 38, 22),      # same as ink
    output="docs/play_assets/boosty_cover_hola.png",
    italic_name=True,
)

# ── TIER 2: COMPAÑERO — terracotta paper, cream ink ──
# Inverted of Hola: warm earth tones, midday Andalusia.
render(
    name_es="Compañero.",
    name_ru="идём вместе",
    price=350,
    tier_num="02",
    bg_top=(178, 78, 50),
    bg_bot=(120, 42, 28),
    ink=(247, 234, 215),
    accent=(255, 195, 110),
    bull_color=(247, 234, 215),
    output="docs/play_assets/boosty_cover_companero.png",
    italic_name=False,
)

# ── TIER 3: MECENAS — deep navy, gold ink, premium ──
# Editorial luxury: think Financial Times "Premium" tier.
render(
    name_es="Mecenas.",
    name_ru="опора проекта",
    price=800,
    tier_num="03",
    bg_top=(26, 32, 50),
    bg_bot=(12, 14, 25),
    ink=(238, 222, 188),
    accent=(212, 165, 90),
    bull_color=(212, 165, 90),
    output="docs/play_assets/boosty_cover_mecenas.png",
    italic_name=True,
)
