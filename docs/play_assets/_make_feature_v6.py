"""1024x500 Play Store feature graphic v6.

Adjustments from v5 per user feedback:
  • Bull bigger (300px)
  • Bull does NOT overlap text
  • Text big — letter K of ESPEAK overlaps the phone slightly (the look
    user liked from v4)
  • Spanish architecture silhouette across the bottom strip
  • All v5 depth/shadow effects retained
"""
from svglib.svglib import svg2rlg
from reportlab.graphics import renderPM
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import io, os

W, H = 1024, 500

# ── Bull silhouette — 300px ────────────────────────────────
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

# ── Background — diagonal gradient ─────────────────────────
img = Image.new('RGBA', (W, H), (217, 82, 28, 255))
draw = ImageDraw.Draw(img)
top_color = (255, 130, 70)
bot_color = (180, 60, 20)
for y in range(H):
    for x_seg in range(0, W, 4):
        dx = (W - x_seg) / W
        dy = y / H
        t = max(0.0, min(1.0, dx * 0.55 + dy * 0.45))
        r = int(top_color[0] + (bot_color[0] - top_color[0]) * t)
        g = int(top_color[1] + (bot_color[1] - top_color[1]) * t)
        b = int(top_color[2] + (bot_color[2] - top_color[2]) * t)
        draw.rectangle([x_seg, y, x_seg + 4, y + 1], fill=(r, g, b))

# ── Sun-glow top-right ─────────────────────────────────────
glow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
gdraw = ImageDraw.Draw(glow)
sun_x, sun_y = W - 200, -50
for r in range(700, 80, -8):
    alpha = max(0, int(28 - r * 0.038))
    if alpha <= 0: continue
    gdraw.ellipse([sun_x - r, sun_y - r, sun_x + r, sun_y + r],
                  fill=(255, 240, 200, alpha))
glow = glow.filter(ImageFilter.GaussianBlur(8))
img = Image.alpha_composite(img, glow)

# ── Spanish architecture silhouette along bottom ───────────
arch = Image.new('RGBA', (W, H), (0, 0, 0, 0))
adraw = ImageDraw.Draw(arch)
A = (0, 0, 0, 100)         # dark silhouette for clear shape definition
A_LIGHT = (0, 0, 0, 60)

base_y = H - 8

def palacio(x, scale=1.0):
    """Madrid Royal Palace style — long block with central dome + 4 corner towers."""
    s = scale
    body_w = int(180 * s); body_h = int(70 * s)
    bx = x; by = base_y - body_h
    # main block
    adraw.rectangle([bx, by, bx + body_w, base_y], fill=A)
    # corner towers (slightly taller)
    tw = int(20 * s); th = int(95 * s)
    adraw.rectangle([bx, base_y - th, bx + tw, base_y], fill=A)
    adraw.rectangle([bx + body_w - tw, base_y - th, bx + body_w, base_y], fill=A)
    # central dome
    dome_w = int(50 * s); dome_h = int(40 * s)
    dx = bx + body_w // 2
    adraw.rectangle([dx - dome_w // 2, by - dome_h // 2,
                     dx + dome_w // 2, by + 4], fill=A)
    adraw.ellipse([dx - dome_w // 2, by - dome_h,
                   dx + dome_w // 2, by + 6], fill=A)
    # tiny spire on dome
    sp_w = int(4 * s); sp_h = int(20 * s)
    adraw.rectangle([dx - sp_w // 2, by - dome_h - sp_h,
                     dx + sp_w // 2, by - dome_h + 2], fill=A)

def sagrada(x, scale=1.0):
    """Sagrada Familia — cluster of tall spires with crosses on top."""
    s = scale
    spires = [(0, 110), (28, 140), (56, 165), (84, 145), (112, 115)]
    sw = int(18 * s)
    for dx, h in spires:
        h = int(h * s)
        bx = x + int(dx * s)
        # body
        adraw.rectangle([bx, base_y - h + 6, bx + sw, base_y], fill=A)
        # pointed tip
        adraw.polygon([
            (bx, base_y - h + 6),
            (bx + sw // 2, base_y - h - 4),
            (bx + sw, base_y - h + 6),
        ], fill=A)
        # tiny cross on top
        cy = base_y - h - 4
        adraw.rectangle([bx + sw // 2 - 1, cy - 8, bx + sw // 2 + 1, cy], fill=A)
        adraw.rectangle([bx + sw // 2 - 4, cy - 5, bx + sw // 2 + 4, cy - 4], fill=A)

def alhambra(x, scale=1.0):
    """Alhambra-style: rectangular fortified blocks with battlements."""
    s = scale
    walls = [(0, 60, 130), (130, 90, 90), (220, 70, 110)]
    for dx, h, w in walls:
        bx = x + int(dx * s); bh = int(h * s); bw = int(w * s)
        adraw.rectangle([bx, base_y - bh, bx + bw, base_y], fill=A)
        # battlements (small rect on top)
        bt_w = int(10 * s); bt_h = int(8 * s)
        for i in range(0, bw, bt_w * 2):
            adraw.rectangle([bx + i, base_y - bh - bt_h,
                             bx + i + bt_w, base_y - bh], fill=A)

def giralda(x, scale=1.0):
    """Sevilla Giralda — tall thin tower with bell-stage + spire."""
    s = scale
    tw = int(38 * s); th = int(160 * s)
    # main tower
    adraw.rectangle([x, base_y - th, x + tw, base_y], fill=A)
    # bell-stage (slightly narrower block on top)
    bs_w = int(28 * s); bs_h = int(32 * s)
    bx = x + (tw - bs_w) // 2
    adraw.rectangle([bx, base_y - th - bs_h, bx + bs_w, base_y - th + 2], fill=A)
    # spire (triangle)
    sp_w = int(20 * s); sp_h = int(30 * s)
    sx = x + (tw - sp_w) // 2
    adraw.polygon([
        (sx, base_y - th - bs_h),
        (sx + sp_w // 2, base_y - th - bs_h - sp_h),
        (sx + sp_w, base_y - th - bs_h),
    ], fill=A)
    # tiny weather-vane
    cy = base_y - th - bs_h - sp_h
    adraw.rectangle([sx + sp_w // 2 - 1, cy - 12, sx + sp_w // 2 + 1, cy], fill=A)

# Compose architecture across bottom
palacio(20, scale=0.85)
sagrada(220, scale=0.95)
alhambra(420, scale=0.85)
giralda(660, scale=0.90)
palacio(720, scale=0.7)
sagrada(900, scale=0.7)

# Soft top fade so buildings melt into the gradient
fade = Image.new('L', (W, H), 0)
fdraw = ImageDraw.Draw(fade)
for y in range(H):
    if y < 320:
        fdraw.line([(0, y), (W, y)], fill=0)
    else:
        v = int(min(255, (y - 320) / (H - 320) * 320))
        fdraw.line([(0, y), (W, y)], fill=v)
arch_masked = Image.composite(arch,
                               Image.new('RGBA', (W, H), (0, 0, 0, 0)),
                               fade)
img = Image.alpha_composite(img, arch_masked)

# ── Phone mockup with screenshot ───────────────────────────
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
    # outer shadow
    s1 = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    s1d = ImageDraw.Draw(s1)
    s1d.rounded_rectangle([pad + 8, pad + 18, pad + PHONE_W + 8, pad + PHONE_H + 18],
                           radius=44, fill=(0, 0, 0, 170))
    s1 = s1.filter(ImageFilter.GaussianBlur(30))
    s2 = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    s2d = ImageDraw.Draw(s2)
    s2d.rounded_rectangle([pad + 4, pad + 6, pad + PHONE_W + 4, pad + PHONE_H + 6],
                           radius=44, fill=(0, 0, 0, 130))
    s2 = s2.filter(ImageFilter.GaussianBlur(10))
    canvas = Image.alpha_composite(canvas, s1)
    canvas = Image.alpha_composite(canvas, s2)
    # body
    body = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    bdraw = ImageDraw.Draw(body)
    bdraw.rounded_rectangle([pad, pad, pad + PHONE_W, pad + PHONE_H],
                            radius=46, fill=(18, 18, 22, 255))
    bdraw.rounded_rectangle([pad + 1, pad + 1, pad + PHONE_W - 1, pad + PHONE_H - 1],
                            radius=45, outline=(255, 255, 255, 35), width=2)
    canvas = Image.alpha_composite(canvas, body)
    # screen
    mask = Image.new('L', (ph_w, ph_h), 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.rounded_rectangle([0, 0, ph_w - 1, ph_h - 1], radius=36, fill=255)
    canvas.paste(shot_resized, (pad + 9, pad + 9), mask)
    # glass reflection
    glass = Image.new('RGBA', (ph_w, ph_h), (0, 0, 0, 0))
    gdr = ImageDraw.Draw(glass)
    gdr.polygon([(0, 0), (ph_w * 0.6, 0), (ph_w * 0.25, ph_h), (0, ph_h * 0.7)],
                fill=(255, 255, 255, 30))
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

# ── Bull with cast shadow — bigger now (300px), left edge ─
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

# ── Text — overlaps phone slightly. Bull MUST NOT touch text. ─
# Bull renders 300px wide at x=20, so its right edge = 320 (with shadow ~ 380).
# Text starts at x=350 → safe gap from bull.
# ESPEAK at 110sp will end around x≈700 — that intentionally clips into the phone.
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
y = H // 2 - 95

# Drop shadow under text
shadow_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
sd = ImageDraw.Draw(shadow_layer)
sd.text((text_x + 4, y + 5), "ESPEAK", fill=(0, 0, 0, 130), font=font_brand)
sd.text((text_x + 3, y + 130 + 4), "Испанский с нуля", fill=(0, 0, 0, 100), font=font_tagline)
shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(4))
img = Image.alpha_composite(img, shadow_layer)

# Foreground text
draw = ImageDraw.Draw(img)
draw.text((text_x, y), "ESPEAK", fill=(255, 255, 255), font=font_brand)
draw.text((text_x, y + 130), "Испанский с нуля", fill=(255, 255, 255), font=font_tagline)
draw.text((text_x, y + 184), "Карточки  ·  Игры  ·  AI",
          fill=(255, 240, 230), font=font_feats)

out = 'docs/play_assets/feature_1024x500.png'
img.convert('RGB').save(out, 'PNG', optimize=True)
os.remove(bull_marker_path)
print('Wrote', out, os.path.getsize(out), 'bytes')
