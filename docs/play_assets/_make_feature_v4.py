"""1024x500 Play Store feature graphic v4 — clean composition with phone mockup.

Bull + brand text on the left, ONE actual app screenshot on the right framed
in a subtle phone-mockup. Standard Play Store premium pattern (Headway, Tobo,
Duolingo all use this).
"""
from svglib.svglib import svg2rlg
from reportlab.graphics import renderPM
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import io, os

W, H = 1024, 500

# ── Bull silhouette ────────────────────────────────────────
BULL_SVG = '<?xml version="1.0" encoding="UTF-8"?>\n<svg xmlns="http://www.w3.org/2000/svg" width="320" height="320" viewBox="0 0 512 512"><rect width="512" height="512" fill="#FF00FF"/><path fill="#FFFFFF" d="M30.882,30.14s33.525,-10.81 92.088,19.64c60.996,31.72 85.628,116.77 132.906,117.86c47.283,-1.09 71.92,-86.14 132.912,-117.858c58.558,-30.45 92.088,-19.643 92.088,-19.643v85.483s-38.062,-2.453 -58.934,13.507c-15.165,11.593 -45.23,54.296 -71.375,80.08c38.867,27.833 63.966,71.877 63.966,121.45c0,84.162 -72.343,152.39 -161.587,152.39S91.353,414.821 91.353,330.659c0,-51.03 26.6,-96.205 67.432,-123.865c-25.558,-25.957 -54.263,-66.43 -68.965,-77.67c-20.877,-15.96 -58.938,-13.506 -58.938,-13.506zM179.45,330.49c0,40.01 32.98,72.44 73.664,72.44s73.67,-32.435 73.67,-72.44s-32.98,-72.44 -73.67,-72.44c-40.688,0 -73.664,32.436 -73.664,72.44"/></svg>'

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

# ── Background gradient ────────────────────────────────────
img = Image.new('RGBA', (W, H), (255, 107, 53, 255))
draw = ImageDraw.Draw(img)
top, bottom = (255, 107, 53), (217, 82, 28)
for y in range(H):
    t = y / (H - 1)
    r = int(top[0] + (bottom[0] - top[0]) * t)
    g = int(top[1] + (bottom[1] - top[1]) * t)
    b = int(top[2] + (bottom[2] - top[2]) * t)
    draw.line([(0, y), (W, y)], fill=(r, g, b))

# ── Top-right glow for depth ───────────────────────────────
overlay = Image.new('RGBA', (W, H), (0, 0, 0, 0))
odraw = ImageDraw.Draw(overlay)
for r in range(420, 100, -10):
    alpha = int(8 - r * 0.012)
    if alpha <= 0: continue
    odraw.ellipse([W - 250 - r, -120 - r, W - 250 + r, -120 + r],
                  fill=(255, 230, 200, alpha))
img = Image.alpha_composite(img, overlay)

# ── Phone mockup with screenshot on the right ──────────────
SHOT = 'docs/play_assets/screenshots/Screenshot_20260511_095005_ESPEAK.jpg'
shot = Image.open(SHOT).convert('RGBA')
# Phone aspect ~ 9:19.5. Target phone height ~440px, width derived.
ph_h = 440
ph_w = int(ph_h * (shot.width / shot.height))   # preserve aspect
shot_resized = shot.resize((ph_w, ph_h), Image.LANCZOS)

# Phone frame: rounded-rect black bezel + soft shadow
PHONE_W = ph_w + 18    # bezel border
PHONE_H = ph_h + 18
phone = Image.new('RGBA', (PHONE_W + 40, PHONE_H + 40), (0, 0, 0, 0))
pdraw = ImageDraw.Draw(phone)

# Drop shadow — large soft blur
shadow_pad = 20
shadow = Image.new('RGBA', (PHONE_W + 40, PHONE_H + 40), (0, 0, 0, 0))
sdraw = ImageDraw.Draw(shadow)
sdraw.rounded_rectangle([shadow_pad, shadow_pad,
                         PHONE_W + shadow_pad - 1, PHONE_H + shadow_pad - 1],
                        radius=44, fill=(0, 0, 0, 90))
shadow = shadow.filter(ImageFilter.GaussianBlur(14))

# Phone body
pdraw.rounded_rectangle([shadow_pad, shadow_pad,
                         PHONE_W + shadow_pad - 1, PHONE_H + shadow_pad - 1],
                        radius=44, fill=(20, 20, 22, 255))

# Inner screen (rounded mask paste)
mask = Image.new('L', (ph_w, ph_h), 0)
mdraw = ImageDraw.Draw(mask)
mdraw.rounded_rectangle([0, 0, ph_w - 1, ph_h - 1], radius=36, fill=255)
phone.paste(shot_resized, (shadow_pad + 9, shadow_pad + 9), mask)

# Combine shadow + phone
phone_full = Image.alpha_composite(shadow, phone)

# Slight CCW tilt for energy (~6°)
phone_full = phone_full.rotate(6, resample=Image.BICUBIC, expand=True)

# Place phone on the right
phone_x = W - phone_full.width - 30
phone_y = (H - phone_full.height) // 2
img.paste(phone_full, (phone_x, phone_y), phone_full)

# ── Bull on the left (smaller, sits next to text) ─────────
bull_x = 50
bull_y = (H - bull.height) // 2 - 5
img.paste(bull, (bull_x, bull_y), bull)

# ── Text in the middle-left ───────────────────────────────
def find_font(candidates):
    for path in candidates:
        if os.path.exists(path): return path
    return None

font_bold = find_font(['C:/Windows/Fonts/arialbd.ttf', 'C:/Windows/Fonts/seguibl.ttf'])
font_reg  = find_font(['C:/Windows/Fonts/arial.ttf', 'C:/Windows/Fonts/segoeui.ttf'])
font_brand   = ImageFont.truetype(font_bold, 88)
font_tagline = ImageFont.truetype(font_bold, 36)
font_feats   = ImageFont.truetype(font_reg, 22)

draw = ImageDraw.Draw(img)
text_x = bull_x + bull.width + 25
y = H // 2 - 75
draw.text((text_x, y), "ESPEAK", fill=(255, 255, 255), font=font_brand)
draw.text((text_x, y + 105), "Испанский с нуля", fill=(255, 255, 255), font=font_tagline)
draw.text((text_x, y + 155), "Карточки  ·  Игры  ·  AI",
          fill=(255, 240, 230), font=font_feats)

out = 'docs/play_assets/feature_1024x500.png'
img.convert('RGB').save(out, 'PNG', optimize=True)
os.remove(bull_marker_path)
print('Wrote', out, os.path.getsize(out), 'bytes')
