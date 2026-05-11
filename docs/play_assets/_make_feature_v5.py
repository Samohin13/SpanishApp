"""1024x500 Play Store feature graphic v5 — depth, shadows, volume.

Same composition as v4 (bull+text left, phone mockup right) but with:
  • Dramatic radial light from top-right (sun-glow)
  • Soft cast shadow under the bull
  • Deeper, multi-layer drop shadow under the phone
  • Glass screen reflection (diagonal highlight)
  • Subtle text shadow under ESPEAK
  • Bottom vignette for grounding
"""
from svglib.svglib import svg2rlg
from reportlab.graphics import renderPM
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import io, os

W, H = 1024, 500

# ── Bull silhouette ────────────────────────────────────────
BULL_SVG = '<?xml version="1.0" encoding="UTF-8"?>\n<svg xmlns="http://www.w3.org/2000/svg" width="220" height="220" viewBox="0 0 512 512"><rect width="512" height="512" fill="#FF00FF"/><path fill="#FFFFFF" d="M30.882,30.14s33.525,-10.81 92.088,19.64c60.996,31.72 85.628,116.77 132.906,117.86c47.283,-1.09 71.92,-86.14 132.912,-117.858c58.558,-30.45 92.088,-19.643 92.088,-19.643v85.483s-38.062,-2.453 -58.934,13.507c-15.165,11.593 -45.23,54.296 -71.375,80.08c38.867,27.833 63.966,71.877 63.966,121.45c0,84.162 -72.343,152.39 -161.587,152.39S91.353,414.821 91.353,330.659c0,-51.03 26.6,-96.205 67.432,-123.865c-25.558,-25.957 -54.263,-66.43 -68.965,-77.67c-20.877,-15.96 -58.938,-13.506 -58.938,-13.506zM179.45,330.49c0,40.01 32.98,72.44 73.664,72.44s73.67,-32.435 73.67,-72.44s-32.98,-72.44 -73.67,-72.44c-40.688,0 -73.664,32.436 -73.664,72.44"/></svg>'

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

# ── Background — diagonal gradient FROM bright top-right TO dark bottom-left
img = Image.new('RGBA', (W, H), (217, 82, 28, 255))
draw = ImageDraw.Draw(img)
# Compute per-pixel from a diagonal gradient
top_color    = (255, 130, 70)    # warm bright
bot_color    = (180, 60, 20)     # deep darker
for y in range(H):
    for x_seg in range(0, W, 4):  # step 4 for speed; will fill rect
        # Distance from top-right corner (0..max)
        dx = (W - x_seg) / W
        dy = y / H
        t = (dx * 0.55 + dy * 0.45)
        t = max(0.0, min(1.0, t))
        r = int(top_color[0] + (bot_color[0] - top_color[0]) * t)
        g = int(top_color[1] + (bot_color[1] - top_color[1]) * t)
        b = int(top_color[2] + (bot_color[2] - top_color[2]) * t)
        draw.rectangle([x_seg, y, x_seg + 4, y + 1], fill=(r, g, b))

# ── Sun-glow from top-right ────────────────────────────────
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

# ── Phone mockup with screenshot — deeper shadow ──────────
SHOT = 'docs/play_assets/screenshots/Screenshot_20260511_095005_ESPEAK.jpg'
shot = Image.open(SHOT).convert('RGBA')
ph_h = 380
ph_w = int(ph_h * (shot.width / shot.height))
shot_resized = shot.resize((ph_w, ph_h), Image.LANCZOS)

PHONE_W = ph_w + 18
PHONE_H = ph_h + 18

# Composite phone with multi-layer shadow + glass reflection
def build_phone():
    pad = 60
    canvas = Image.new('RGBA', (PHONE_W + pad * 2, PHONE_H + pad * 2), (0, 0, 0, 0))

    # Layer 1: large soft shadow
    s1 = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    s1d = ImageDraw.Draw(s1)
    s1d.rounded_rectangle([pad + 8, pad + 16, pad + PHONE_W + 8, pad + PHONE_H + 16],
                           radius=44, fill=(0, 0, 0, 160))
    s1 = s1.filter(ImageFilter.GaussianBlur(28))

    # Layer 2: closer shadow
    s2 = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    s2d = ImageDraw.Draw(s2)
    s2d.rounded_rectangle([pad + 4, pad + 6, pad + PHONE_W + 4, pad + PHONE_H + 6],
                           radius=44, fill=(0, 0, 0, 120))
    s2 = s2.filter(ImageFilter.GaussianBlur(10))

    canvas = Image.alpha_composite(canvas, s1)
    canvas = Image.alpha_composite(canvas, s2)

    # Phone body — slightly off-black with bezel highlight
    body = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    bdraw = ImageDraw.Draw(body)
    bdraw.rounded_rectangle([pad, pad, pad + PHONE_W, pad + PHONE_H],
                            radius=46, fill=(18, 18, 22, 255))
    # Inner bezel highlight (subtle white border)
    bdraw.rounded_rectangle([pad + 1, pad + 1, pad + PHONE_W - 1, pad + PHONE_H - 1],
                            radius=45, outline=(255, 255, 255, 35), width=2)
    canvas = Image.alpha_composite(canvas, body)

    # Screen with rounded mask
    mask = Image.new('L', (ph_w, ph_h), 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.rounded_rectangle([0, 0, ph_w - 1, ph_h - 1], radius=36, fill=255)
    canvas.paste(shot_resized, (pad + 9, pad + 9), mask)

    # Glass reflection — diagonal soft highlight on the screen surface
    glass = Image.new('RGBA', (ph_w, ph_h), (0, 0, 0, 0))
    gdr = ImageDraw.Draw(glass)
    gdr.polygon([
        (0, 0), (ph_w * 0.6, 0),
        (ph_w * 0.25, ph_h), (0, ph_h * 0.7)
    ], fill=(255, 255, 255, 30))
    glass = glass.filter(ImageFilter.GaussianBlur(20))
    glass_masked = Image.composite(glass, Image.new('RGBA', glass.size, (0, 0, 0, 0)), mask)
    canvas.paste(glass_masked, (pad + 9, pad + 9), glass_masked)

    return canvas

phone_full = build_phone()
phone_full = phone_full.rotate(6, resample=Image.BICUBIC, expand=True)

phone_x = W - phone_full.width + 10
phone_y = (H - phone_full.height) // 2
img.paste(phone_full, (phone_x, phone_y), phone_full)

# ── Bull with cast shadow ──────────────────────────────────
def with_shadow(im, ox=12, oy=18, blur=18, alpha=120):
    """Returns RGBA canvas with blurred shadow behind im."""
    pad = blur * 2 + 30
    canvas = Image.new('RGBA', (im.width + pad * 2, im.height + pad * 2), (0, 0, 0, 0))
    # shadow = black version of bull alpha
    shadow = Image.new('RGBA', im.size, (0, 0, 0, 0))
    shadow_data = []
    for r, g, b, a in im.getdata():
        shadow_data.append((0, 0, 0, min(a, alpha)))
    shadow.putdata(shadow_data)
    blurred = shadow.filter(ImageFilter.GaussianBlur(blur))
    canvas.paste(blurred, (pad + ox, pad + oy), blurred)
    canvas.paste(im, (pad, pad), im)
    return canvas

bull_with_shadow = with_shadow(bull, ox=8, oy=16, blur=16, alpha=140)
bull_x = 40
bull_y = (H - bull_with_shadow.height) // 2
img.paste(bull_with_shadow, (bull_x, bull_y), bull_with_shadow)

# ── Bottom vignette for grounding ─────────────────────────
vig = Image.new('RGBA', (W, H), (0, 0, 0, 0))
vdr = ImageDraw.Draw(vig)
for y in range(int(H * 0.65), H):
    t = (y - H * 0.65) / (H - H * 0.65)
    a = int(70 * t)
    vdr.line([(0, y), (W, y)], fill=(0, 0, 0, a))
img = Image.alpha_composite(img, vig)

# ── Text with subtle drop shadow ──────────────────────────
def find_font(candidates):
    for path in candidates:
        if os.path.exists(path): return path
    return None

font_bold = find_font(['C:/Windows/Fonts/arialbd.ttf', 'C:/Windows/Fonts/seguibl.ttf'])
font_reg  = find_font(['C:/Windows/Fonts/arial.ttf', 'C:/Windows/Fonts/segoeui.ttf'])
font_brand   = ImageFont.truetype(font_bold, 78)
font_tagline = ImageFont.truetype(font_bold, 32)
font_feats   = ImageFont.truetype(font_reg, 20)

text_x = 290              # explicit position — clears the bull (40-260 area)
y = H // 2 - 70

# Draw shadow first (offset, slightly transparent)
shadow_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
sd = ImageDraw.Draw(shadow_layer)
sd.text((text_x + 3, y + 4), "ESPEAK", fill=(0, 0, 0, 110), font=font_brand)
sd.text((text_x + 2, y + 92 + 3), "Испанский с нуля", fill=(0, 0, 0, 90), font=font_tagline)
shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(3))
img = Image.alpha_composite(img, shadow_layer)

# Foreground text
draw = ImageDraw.Draw(img)
draw.text((text_x, y), "ESPEAK", fill=(255, 255, 255), font=font_brand)
draw.text((text_x, y + 92), "Испанский с нуля", fill=(255, 255, 255), font=font_tagline)
draw.text((text_x, y + 138), "Карточки  ·  Игры  ·  AI",
          fill=(255, 240, 230), font=font_feats)

out = 'docs/play_assets/feature_1024x500.png'
img.convert('RGB').save(out, 'PNG', optimize=True)
os.remove(bull_marker_path)
print('Wrote', out, os.path.getsize(out), 'bytes')
