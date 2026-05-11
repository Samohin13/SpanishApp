"""1024x500 Play Store feature graphic v3 — premium minimal.

Single tasteful pattern: Spanish city skyline silhouette at the bottom
edge (same motif as the in-app SpanishCitiesWatermark on HomeScreen).
No scattered icons — premium = restraint.
"""
from svglib.svglib import svg2rlg
from reportlab.graphics import renderPM
from PIL import Image, ImageDraw, ImageFont
import io, os

W, H = 1024, 500

BULL_SVG = '<?xml version="1.0" encoding="UTF-8"?>\n<svg xmlns="http://www.w3.org/2000/svg" width="380" height="380" viewBox="0 0 512 512"><rect width="512" height="512" fill="#FF00FF"/><path fill="#FFFFFF" d="M30.882,30.14s33.525,-10.81 92.088,19.64c60.996,31.72 85.628,116.77 132.906,117.86c47.283,-1.09 71.92,-86.14 132.912,-117.858c58.558,-30.45 92.088,-19.643 92.088,-19.643v85.483s-38.062,-2.453 -58.934,13.507c-15.165,11.593 -45.23,54.296 -71.375,80.08c38.867,27.833 63.966,71.877 63.966,121.45c0,84.162 -72.343,152.39 -161.587,152.39S91.353,414.821 91.353,330.659c0,-51.03 26.6,-96.205 67.432,-123.865c-25.558,-25.957 -54.263,-66.43 -68.965,-77.67c-20.877,-15.96 -58.938,-13.506 -58.938,-13.506zM179.45,330.49c0,40.01 32.98,72.44 73.664,72.44s73.67,-32.435 73.67,-72.44s-32.98,-72.44 -73.67,-72.44c-40.688,0 -73.664,32.436 -73.664,72.44"/></svg>'

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

# Background gradient
img = Image.new('RGB', (W, H), (255, 107, 53))
draw = ImageDraw.Draw(img)
top, bottom = (255, 107, 53), (217, 82, 28)
for y in range(H):
    t = y / (H - 1)
    r = int(top[0] + (bottom[0] - top[0]) * t)
    g = int(top[1] + (bottom[1] - top[1]) * t)
    b = int(top[2] + (bottom[2] - top[2]) * t)
    draw.line([(0, y), (W, y)], fill=(r, g, b))

# ── Single decorative element: Spanish city skyline silhouette ──
# Drawn into the bottom strip with a soft top fade so it blends into
# the gradient. Same motif as in-app SpanishCitiesWatermark on Home.
skyline = Image.new('RGBA', (W, H), (0, 0, 0, 0))
sdraw = ImageDraw.Draw(skyline)

# Layer 1: Madrid-style block buildings + central dome (left third)
def madrid_skyline(x_off, scale=1.0):
    s = scale
    base_y = H - 30
    # blocks (x_frac, height)
    blocks = [
        (0.00, 80), (0.12, 110), (0.26, 65),
        (0.38, 130), (0.52, 75), (0.66, 95), (0.80, 60),
    ]
    block_w = int(40 * s)
    for x_frac, h in blocks:
        h = int(h * s)
        bx = x_off + int(x_frac * 350 * s)
        sdraw.rectangle([bx, base_y - h, bx + block_w, base_y],
                        fill=(255, 255, 255, 38))
    # central dome
    dome_cx = x_off + int(0.38 * 350 * s) + block_w // 2
    dome_cy = base_y - int(130 * s) - int(15 * s)
    dr = int(22 * s)
    sdraw.ellipse([dome_cx - dr, dome_cy - dr, dome_cx + dr, dome_cy + dr],
                  fill=(255, 255, 255, 38))

# Layer 2: Barcelona-style tall thin spires (centre)
def barcelona_skyline(x_off, scale=1.0):
    s = scale
    base_y = H - 30
    spires = [(0.10, 100), (0.22, 130), (0.34, 145), (0.46, 120), (0.58, 95)]
    spire_w = int(20 * s)
    for x_frac, h in spires:
        h = int(h * s)
        bx = x_off + int(x_frac * 280 * s)
        # body
        sdraw.rectangle([bx, base_y - h + 8, bx + spire_w, base_y],
                        fill=(255, 255, 255, 38))
        # pointed tip
        sdraw.polygon([
            (bx, base_y - h + 8),
            (bx + spire_w // 2, base_y - h - 5),
            (bx + spire_w, base_y - h + 8),
        ], fill=(255, 255, 255, 38))

# Layer 3: Sevilla-style rounded buildings + Giralda tower (right)
def sevilla_skyline(x_off, scale=1.0):
    s = scale
    base_y = H - 30
    blocks = [(0.00, 60), (0.20, 80), (0.40, 65), (0.78, 75)]
    bw = int(30 * s)
    for x_frac, h in blocks:
        h = int(h * s)
        bx = x_off + int(x_frac * 250 * s)
        # rounded top — half-circle on rectangle
        sdraw.rectangle([bx, base_y - h + bw // 2, bx + bw, base_y],
                        fill=(255, 255, 255, 38))
        sdraw.ellipse([bx, base_y - h, bx + bw, base_y - h + bw],
                      fill=(255, 255, 255, 38))
    # Giralda tower
    tw = int(34 * s)
    th = int(150 * s)
    tx = x_off + int(0.55 * 250 * s)
    sdraw.rectangle([tx, base_y - th, tx + tw, base_y],
                    fill=(255, 255, 255, 38))
    # tower dome
    dx = tx + tw // 2
    dy = base_y - th - 8
    dr = int(14 * s)
    sdraw.ellipse([dx - dr, dy - dr, dx + dr, dy + dr],
                  fill=(255, 255, 255, 38))

# Place 3 skylines spaced across width
madrid_skyline(20, scale=0.95)
barcelona_skyline(380, scale=0.95)
sevilla_skyline(720, scale=0.95)

# Soft top→bottom fade applied to the skyline layer so the building
# tops melt into the orange gradient instead of cutting hard.
fade = Image.new('L', (W, H), 0)
fdraw = ImageDraw.Draw(fade)
for y in range(H):
    # 0 above the skyline area, ramping to 255 at the bottom
    if y < 280:
        fdraw.line([(0, y), (W, y)], fill=0)
    else:
        v = int(min(255, (y - 280) / (H - 280) * 320))
        fdraw.line([(0, y), (W, y)], fill=v)
# Use the fade as multiplier on the skyline alpha
sk_data = skyline.split()
sk_alpha = sk_data[3]
new_alpha = Image.eval(sk_alpha, lambda a: a)  # keep
masked = Image.composite(skyline,
                         Image.new('RGBA', (W, H), (0, 0, 0, 0)),
                         fade)
img = Image.alpha_composite(img.convert('RGBA'), masked).convert('RGB')

# Soft top-right glow
overlay = Image.new('RGBA', (W, H), (0, 0, 0, 0))
odraw = ImageDraw.Draw(overlay)
for r in range(420, 100, -10):
    alpha = int(8 - r * 0.012)
    if alpha <= 0: continue
    odraw.ellipse([W - 250 - r, -120 - r, W - 250 + r, -120 + r],
                  fill=(255, 230, 200, alpha))
img = Image.alpha_composite(img.convert('RGBA'), overlay).convert('RGB')

# Bull
bull_x = 70
bull_y = (H - bull.height) // 2 - 15
img.paste(bull, (bull_x, bull_y), bull)

# Text
def find_font(candidates):
    for path in candidates:
        if os.path.exists(path): return path
    return None

font_bold = find_font(['C:/Windows/Fonts/arialbd.ttf', 'C:/Windows/Fonts/seguibl.ttf'])
font_reg  = find_font(['C:/Windows/Fonts/arial.ttf', 'C:/Windows/Fonts/segoeui.ttf'])
font_brand   = ImageFont.truetype(font_bold, 110)
font_tagline = ImageFont.truetype(font_bold, 44)
font_feats   = ImageFont.truetype(font_reg, 24)

draw = ImageDraw.Draw(img)
text_x = bull_x + bull.width + 40
y = H // 2 - 110
draw.text((text_x, y), "ESPEAK", fill=(255, 255, 255), font=font_brand)
draw.text((text_x, y + 130), "Испанский с нуля", fill=(255, 255, 255), font=font_tagline)
draw.text((text_x, y + 190), "Карточки  ·  Игры  ·  AI-репетитор",
          fill=(255, 240, 230), font=font_feats)

out = 'docs/play_assets/feature_1024x500.png'
img.save(out, 'PNG', optimize=True)
os.remove(bull_marker_path)
print('Wrote', out, os.path.getsize(out), 'bytes')
