"""1024x500 Play Store feature graphic v7.

Adjustments per user:
  • Move text DOWN (more space from bull)
  • Lower phone contrast & shadow intensity
  • Replace architecture with the most famous Spanish landmarks:
    Sagrada Familia + Casa Batlló + Casa Milà + Giralda — all
    visible, none cut off
"""
from svglib.svglib import svg2rlg
from reportlab.graphics import renderPM
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import io, os

W, H = 1024, 500

BULL_SVG = '<?xml version="1.0" encoding="UTF-8"?>\n<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" viewBox="0 0 512 512"><rect width="512" height="512" fill="#FF00FF"/><path fill="#FFFFFF" d="M30.882,30.14s33.525,-10.81 92.088,19.64c60.996,31.72 85.628,116.77 132.906,117.86c47.283,-1.09 71.92,-86.14 132.912,-117.858c58.558,-30.45 92.088,-19.643 92.088,-19.643v85.483s-38.062,-2.453 -58.934,13.507c-15.165,11.593 -45.23,54.296 -71.375,80.08c38.867,27.833 63.966,71.877 63.966,121.45c0,84.162 -72.343,152.39 -161.587,152.39S91.353,414.821 91.353,330.659c0,-51.03 26.6,-96.205 67.432,-123.865c-25.558,-25.957 -54.263,-66.43 -68.965,-77.67c-20.877,-15.96 -58.938,-13.506 -58.938,-13.506zM179.45,330.49c0,40.01 32.98,72.44 73.664,72.44s73.67,-32.435 73.67,-72.44s-32.98,-72.44 -73.67,-72.44c-40.688,0 -73.664,32.436 -73.664,72.44"/></svg>'

drawing = svg2rlg(io.BytesIO(BULL_SVG.encode('utf-8')))
bull_marker_path = 'docs/play_assets/_bull_marker_tmp.png'
renderPM.drawToFile(drawing, bull_marker_path, fmt='PNG', dpi=72)
bull = Image.open(bull_marker_path).convert('RGBA')
data = list(bull.getdata())
new_data = []
for r, g, b, a in data:
    if r > b * 0.9 and g < 200 and abs(r - b) < 80 and r > 150 and b > 150 and g < r * 0.7:
        new_data.append((0, 0, 0, 0))
    else:
        new_data.append((r, g, b, a))
bull.putdata(new_data)

# Background — diagonal gradient
img = Image.new('RGBA', (W, H), (217, 82, 28, 255))
draw = ImageDraw.Draw(img)
top_color = (255, 130, 70); bot_color = (180, 60, 20)
for y in range(H):
    for x_seg in range(0, W, 4):
        dx = (W - x_seg) / W; dy = y / H
        t = max(0.0, min(1.0, dx * 0.55 + dy * 0.45))
        r = int(top_color[0] + (bot_color[0] - top_color[0]) * t)
        g = int(top_color[1] + (bot_color[1] - top_color[1]) * t)
        b = int(top_color[2] + (bot_color[2] - top_color[2]) * t)
        draw.rectangle([x_seg, y, x_seg + 4, y + 1], fill=(r, g, b))

# Sun-glow
glow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
gdraw = ImageDraw.Draw(glow)
for r in range(700, 80, -8):
    alpha = max(0, int(28 - r * 0.038))
    if alpha <= 0: continue
    gdraw.ellipse([W - 200 - r, -50 - r, W - 200 + r, -50 + r],
                  fill=(255, 240, 200, alpha))
glow = glow.filter(ImageFilter.GaussianBlur(8))
img = Image.alpha_composite(img, glow)

# ── Architecture: 4 famous landmarks, all visible ──
arch = Image.new('RGBA', (W, H), (0, 0, 0, 0))
adraw = ImageDraw.Draw(arch)
A = (0, 0, 0, 70)         # lighter per user feedback (was 130)
A_LIGHT = (0, 0, 0, 45)
base_y = H - 8

def sagrada_familia(x, scale=1.0):
    """THE Sagrada Familia — 8 pointed spires of varying heights with crosses."""
    s = scale
    spires = [(0, 130), (22, 165), (44, 195), (66, 215),
              (88, 215), (110, 190), (132, 160), (154, 125)]
    sw = int(20 * s)
    for dx, h in spires:
        h = int(h * s)
        bx = x + int(dx * s)
        # tapered body
        adraw.polygon([
            (bx, base_y),
            (bx + sw, base_y),
            (bx + int(sw * 0.85), base_y - h + 12),
            (bx + int(sw * 0.15), base_y - h + 12),
        ], fill=A)
        # pointed tip
        adraw.polygon([
            (bx + int(sw * 0.15), base_y - h + 12),
            (bx + sw // 2, base_y - h - 6),
            (bx + int(sw * 0.85), base_y - h + 12),
        ], fill=A)
        # cross on top
        cy = base_y - h - 6
        adraw.rectangle([bx + sw // 2 - 1, cy - 12, bx + sw // 2 + 1, cy], fill=A)
        adraw.rectangle([bx + sw // 2 - 5, cy - 8, bx + sw // 2 + 5, cy - 6], fill=A)
        # 3 small arched windows on body (Gaudí detail)
        for j in range(3):
            wy = base_y - 25 - j * int(h * 0.20)
            adraw.ellipse([bx + 4, wy - 3, bx + sw - 4, wy + 1], fill=A_LIGHT)
    # base nave
    adraw.rectangle([x - 8, base_y - int(40 * s),
                     x + int(174 * s) + 8, base_y], fill=A)

def casa_batllo(x, scale=1.0):
    """Casa Batlló (Gaudí) — wavy dragon-spine roof + onion-bulb tower."""
    s = scale
    body_w = int(130 * s); body_h = int(110 * s)
    bx = x; by = base_y - body_h
    # Wavy roofline (dragon spine = 6 arches)
    facade = [(bx, base_y), (bx, by + 18)]
    arches = 6
    for i in range(arches + 1):
        wx = bx + int(i * body_w / arches)
        wy = by + (10 if i % 2 == 0 else -2)
        facade.append((wx, wy))
    facade.append((bx + body_w, by + 18))
    facade.append((bx + body_w, base_y))
    adraw.polygon(facade, fill=A)
    # Central tower with onion-bulb
    tcx = bx + body_w // 2
    tw = int(20 * s); th = int(48 * s)
    adraw.rectangle([tcx - tw // 2, by - th, tcx + tw // 2, by + 5], fill=A)
    bulb_r = int(16 * s)
    adraw.ellipse([tcx - bulb_r, by - th - bulb_r,
                   tcx + bulb_r, by - th + bulb_r // 2], fill=A)
    # cross on bulb
    adraw.rectangle([tcx - 1, by - th - bulb_r - 14, tcx + 1, by - th - bulb_r], fill=A)
    # 3 skull-bone balcony shapes on facade
    for i in range(3):
        wx = bx + int((i + 0.5) * body_w / 3)
        wy = by + int(50 * s)
        adraw.ellipse([wx - 11, wy - 6, wx + 11, wy + 8], fill=A_LIGHT)

def casa_mila(x, scale=1.0):
    """Casa Milà (La Pedrera) — undulating wavy facade + chimneys."""
    s = scale
    body_w = int(115 * s); body_h = int(95 * s)
    bx = x; by = base_y - body_h
    # 4 wavy bands stacked
    bands = 4
    for band in range(bands):
        band_y = by + int(band * body_h / bands)
        pts = [(bx, band_y + 12)]
        steps = 10
        for i in range(steps + 1):
            px = bx + int(i * body_w / steps)
            offset = 6 if i % 2 == 0 else -2
            pts.append((px, band_y + 6 + offset))
        pts.append((bx + body_w, band_y + 12))
        pts.append((bx + body_w, band_y + int(body_h / bands) + 4))
        pts.append((bx, band_y + int(body_h / bands) + 4))
        adraw.polygon(pts, fill=A if band % 2 == 0 else A_LIGHT)
    # solid base
    adraw.rectangle([bx, by + int(body_h * 0.7), bx + body_w, base_y], fill=A)
    # 3 chimney sculptures on roof
    for i in range(3):
        cx = bx + int((i + 0.5) * body_w / 3)
        cy_top = by - int(22 * s)
        adraw.polygon([
            (cx - 5, by + 5), (cx + 5, by + 5),
            (cx + 4, cy_top + 5), (cx - 4, cy_top + 5),
        ], fill=A)
        adraw.ellipse([cx - 7, cy_top - 5, cx + 7, cy_top + 7], fill=A)

def giralda(x, scale=1.0):
    """Sevilla Giralda — tall thin tower + bell-stage + spire."""
    s = scale
    tw = int(34 * s); th = int(180 * s)
    adraw.rectangle([x, base_y - th, x + tw, base_y], fill=A)
    bs_w = int(26 * s); bs_h = int(28 * s)
    bx = x + (tw - bs_w) // 2
    adraw.rectangle([bx, base_y - th - bs_h, bx + bs_w, base_y - th + 2], fill=A)
    sp_w = int(18 * s); sp_h = int(28 * s)
    sx = x + (tw - sp_w) // 2
    adraw.polygon([
        (sx, base_y - th - bs_h),
        (sx + sp_w // 2, base_y - th - bs_h - sp_h),
        (sx + sp_w, base_y - th - bs_h),
    ], fill=A)
    cy = base_y - th - bs_h - sp_h
    adraw.rectangle([sx + sp_w // 2 - 1, cy - 14, sx + sp_w // 2 + 1, cy], fill=A)
    # arched windows on tower
    for j in range(4):
        wy = base_y - 30 - j * int(35 * s)
        adraw.ellipse([x + 6, wy - 3, x + tw - 6, wy + 2], fill=A_LIGHT)

# Additional landmarks for full-width composition
def windmill(x, scale=1.0):
    """La Mancha windmill (Don Quixote) — cylindrical tower + 4 sails."""
    s = scale
    tw = int(28 * s); th = int(70 * s)
    adraw.polygon([(x, base_y), (x + tw, base_y),
                   (x + tw - 4, base_y - th), (x + 4, base_y - th)], fill=A)
    adraw.polygon([(x - 4, base_y - th), (x + tw + 4, base_y - th),
                   (x + tw // 2, base_y - th - 22)], fill=A)
    cx = x + tw // 2; cy = base_y - th + 8
    sail_l = int(38 * s)
    import math
    for ang in (0, 1.5708, 0.7854, 2.3562):
        ex = cx + int(sail_l * math.cos(ang))
        ey = cy - int(sail_l * math.sin(ang))
        adraw.line([(cx, cy), (ex, ey)], fill=A, width=int(3 * s))

def aqueduct(x, scale=1.0):
    """Segovia Roman aqueduct — 2-tier arches."""
    s = scale
    arch_w = int(28 * s); arch_h = int(38 * s)
    base = base_y
    for i in range(5):
        bx = x + i * arch_w
        adraw.rectangle([bx, base - arch_h, bx + 6, base], fill=A)
    adraw.rectangle([x, base - arch_h - 8, x + 5 * arch_w + 6, base - arch_h], fill=A)
    arch_h2 = int(60 * s)
    for i in range(4):
        bx = x + 4 + i * arch_w
        adraw.rectangle([bx, base - arch_h - 8 - arch_h2, bx + 6, base - arch_h - 8], fill=A)
    adraw.rectangle([x, base - arch_h - 8 - arch_h2 - 6,
                     x + 4 * arch_w + 10, base - arch_h - 8 - arch_h2], fill=A)

def plaza_de_espana(x, scale=1.0):
    """Plaza de España (Sevilla) — long building + 2 towers."""
    s = scale
    body_w = int(170 * s); body_h = int(40 * s)
    bx = x; by = base_y - body_h
    adraw.rounded_rectangle([bx, by, bx + body_w, base_y],
                            radius=int(20 * s), fill=A)
    tw = int(24 * s); th = int(95 * s)
    adraw.rectangle([bx, base_y - th, bx + tw, base_y], fill=A)
    adraw.rectangle([bx + body_w - tw, base_y - th, bx + body_w, base_y], fill=A)
    for tower_x in (bx, bx + body_w - tw):
        adraw.polygon([(tower_x - 2, base_y - th),
                       (tower_x + tw // 2, base_y - th - 14),
                       (tower_x + tw + 2, base_y - th)], fill=A)

def alhambra_fortress(x, scale=1.0):
    """Alhambra — fortified walls + central tall tower."""
    s = scale
    adraw.rectangle([x, base_y - int(45 * s), x + int(140 * s), base_y], fill=A)
    bw = int(8 * s)
    for i in range(0, int(140 * s), bw * 2):
        adraw.rectangle([x + i, base_y - int(45 * s) - 6,
                         x + i + bw, base_y - int(45 * s)], fill=A)
    tw = int(30 * s); th = int(100 * s)
    tx = x + int(70 * s) - tw // 2
    adraw.rectangle([tx, base_y - th, tx + tw, base_y], fill=A)
    for i in range(0, tw, 6):
        adraw.rectangle([tx + i, base_y - th - 5, tx + i + 4, base_y - th], fill=A)

def bullring(x, scale=1.0):
    """Plaza de Toros silhouette."""
    s = scale
    rw = int(120 * s); rh = int(50 * s)
    adraw.ellipse([x, base_y - rh * 2, x + rw, base_y], fill=A)
    adraw.rectangle([x, base_y - rh, x + rw, base_y], fill=A)
    cw = int(8 * s)
    for i in range(int(rw * 0.2), int(rw * 0.8), cw * 2):
        adraw.rectangle([x + i, base_y - rh - 8, x + i + cw, base_y - rh - 2], fill=A_LIGHT)

# ── Compose architecture across FULL 1024 width ──
# Avoid the phone area (760-1024) by placing tighter landmarks there.
windmill(20, scale=0.85)
casa_batllo(75, scale=0.75)
sagrada_familia(190, scale=0.95)         # HERO — Sagrada centre-left
casa_mila(395, scale=0.75)
plaza_de_espana(495, scale=0.75)
giralda(660, scale=0.85)
aqueduct(720, scale=0.65)
bullring(870, scale=0.55)
alhambra_fortress(960, scale=0.5)

# Soft top fade
fade = Image.new('L', (W, H), 0)
fdraw = ImageDraw.Draw(fade)
for y in range(H):
    if y < 280:
        fdraw.line([(0, y), (W, y)], fill=0)
    else:
        v = int(min(255, (y - 280) / (H - 280) * 320))
        fdraw.line([(0, y), (W, y)], fill=v)
arch_masked = Image.composite(arch, Image.new('RGBA', (W, H), (0, 0, 0, 0)), fade)
img = Image.alpha_composite(img, arch_masked)

# ── Phone mockup — softer shadows ──────────────────────────
SHOT = 'docs/play_assets/screenshots/Screenshot_20260511_095005_ESPEAK.jpg'
shot = Image.open(SHOT).convert('RGBA')
ph_h = 410
ph_w = int(ph_h * (shot.width / shot.height))
shot_resized = shot.resize((ph_w, ph_h), Image.LANCZOS)

PHONE_W = ph_w + 18
PHONE_H = ph_h + 18

def build_phone():
    pad = 60
    canvas = Image.new('RGBA', (PHONE_W + pad * 2, PHONE_H + pad * 2), (0, 0, 0, 0))
    # Softer outer shadow (was 170 alpha → 90)
    s1 = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    s1d = ImageDraw.Draw(s1)
    s1d.rounded_rectangle([pad + 6, pad + 14, pad + PHONE_W + 6, pad + PHONE_H + 14],
                           radius=44, fill=(0, 0, 0, 90))
    s1 = s1.filter(ImageFilter.GaussianBlur(28))
    s2 = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    s2d = ImageDraw.Draw(s2)
    s2d.rounded_rectangle([pad + 3, pad + 5, pad + PHONE_W + 3, pad + PHONE_H + 5],
                           radius=44, fill=(0, 0, 0, 65))
    s2 = s2.filter(ImageFilter.GaussianBlur(10))
    canvas = Image.alpha_composite(canvas, s1)
    canvas = Image.alpha_composite(canvas, s2)
    body = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    bdraw = ImageDraw.Draw(body)
    # Lower body contrast — was (18,18,22) → (40,40,46) softer dark grey
    bdraw.rounded_rectangle([pad, pad, pad + PHONE_W, pad + PHONE_H],
                            radius=46, fill=(40, 40, 46, 255))
    bdraw.rounded_rectangle([pad + 1, pad + 1, pad + PHONE_W - 1, pad + PHONE_H - 1],
                            radius=45, outline=(255, 255, 255, 25), width=2)
    canvas = Image.alpha_composite(canvas, body)
    mask = Image.new('L', (ph_w, ph_h), 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.rounded_rectangle([0, 0, ph_w - 1, ph_h - 1], radius=36, fill=255)
    canvas.paste(shot_resized, (pad + 9, pad + 9), mask)
    glass = Image.new('RGBA', (ph_w, ph_h), (0, 0, 0, 0))
    gdr = ImageDraw.Draw(glass)
    gdr.polygon([(0, 0), (ph_w * 0.6, 0), (ph_w * 0.25, ph_h), (0, ph_h * 0.7)],
                fill=(255, 255, 255, 24))
    glass = glass.filter(ImageFilter.GaussianBlur(20))
    glass_masked = Image.composite(glass,
                                    Image.new('RGBA', glass.size, (0, 0, 0, 0)),
                                    mask)
    canvas.paste(glass_masked, (pad + 9, pad + 9), glass_masked)
    return canvas

phone_full = build_phone()
phone_full = phone_full.rotate(6, resample=Image.BICUBIC, expand=True)

phone_x = W - phone_full.width + 20
phone_y = (H - phone_full.height) // 2
img.paste(phone_full, (phone_x, phone_y), phone_full)

# ── Bull (300px) with cast shadow ──────────────────────────
def with_shadow(im, ox=12, oy=18, blur=18, alpha=120):
    pad = blur * 2 + 30
    canvas = Image.new('RGBA', (im.width + pad * 2, im.height + pad * 2), (0, 0, 0, 0))
    shadow = Image.new('RGBA', im.size, (0, 0, 0, 0))
    shadow_data = []
    for r, g, b, a in im.getdata():
        shadow_data.append((0, 0, 0, min(a, alpha)))
    shadow.putdata(shadow_data)
    blurred = shadow.filter(ImageFilter.GaussianBlur(blur))
    canvas.paste(blurred, (pad + ox, pad + oy), blurred)
    canvas.paste(im, (pad, pad), im)
    return canvas

bull_with_shadow = with_shadow(bull, ox=10, oy=20, blur=20, alpha=150)
bull_x = 20
bull_y = (H - bull_with_shadow.height) // 2
img.paste(bull_with_shadow, (bull_x, bull_y), bull_with_shadow)

# Bottom vignette
vig = Image.new('RGBA', (W, H), (0, 0, 0, 0))
vdr = ImageDraw.Draw(vig)
for y in range(int(H * 0.65), H):
    t = (y - H * 0.65) / (H - H * 0.65)
    vdr.line([(0, y), (W, y)], fill=(0, 0, 0, int(70 * t)))
img = Image.alpha_composite(img, vig)

# ── Text — moved down per user feedback ───────────────────
def find_font(candidates):
    for path in candidates:
        if os.path.exists(path): return path
    return None
font_bold = find_font(['C:/Windows/Fonts/arialbd.ttf', 'C:/Windows/Fonts/seguibl.ttf'])
font_reg  = find_font(['C:/Windows/Fonts/arial.ttf', 'C:/Windows/Fonts/segoeui.ttf'])
font_brand   = ImageFont.truetype(font_bold, 110)
font_tagline = ImageFont.truetype(font_bold, 40)
font_feats   = ImageFont.truetype(font_reg, 22)

text_x = 350
y = H // 2 - 60       # was -95; lower per user feedback

shadow_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
sd = ImageDraw.Draw(shadow_layer)
sd.text((text_x + 4, y + 5), "ESPEAK", fill=(0, 0, 0, 130), font=font_brand)
sd.text((text_x + 3, y + 130 + 4), "Испанский с нуля", fill=(0, 0, 0, 100), font=font_tagline)
shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(4))
img = Image.alpha_composite(img, shadow_layer)

draw = ImageDraw.Draw(img)
draw.text((text_x, y), "ESPEAK", fill=(255, 255, 255), font=font_brand)
draw.text((text_x, y + 130), "Испанский с нуля", fill=(255, 255, 255), font=font_tagline)
draw.text((text_x, y + 184), "Карточки  ·  Игры  ·  AI",
          fill=(255, 240, 230), font=font_feats)

out = 'docs/play_assets/feature_1024x500.png'
img.convert('RGB').save(out, 'PNG', optimize=True)
os.remove(bull_marker_path)
print('Wrote', out, os.path.getsize(out), 'bytes')
