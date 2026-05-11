"""512x512 Play Store app icon v2 — premium look.

Adds depth/shadows/gloss to the flat v1 orange square:
  • Diagonal gradient orange→darker orange (top-left → bottom-right)
  • Radial highlight (sun glow) from top-left corner
  • Inner vignette on bottom-right for depth
  • Drop shadow under the bull silhouette
  • Subtle top-edge highlight on the bull (gloss / bevel feel)
"""
from svglib.svglib import svg2rlg
from reportlab.graphics import renderPM
from PIL import Image, ImageDraw, ImageFilter
import io, os

W = H = 512

# ── Bull silhouette (transparent BG via marker-replace) ────
BULL_SVG = '<?xml version="1.0" encoding="UTF-8"?>\n<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512"><rect width="512" height="512" fill="#FF00FF"/><path fill="#FFFFFF" d="M30.882,30.14s33.525,-10.81 92.088,19.64c60.996,31.72 85.628,116.77 132.906,117.86c47.283,-1.09 71.92,-86.14 132.912,-117.858c58.558,-30.45 92.088,-19.643 92.088,-19.643v85.483s-38.062,-2.453 -58.934,13.507c-15.165,11.593 -45.23,54.296 -71.375,80.08c38.867,27.833 63.966,71.877 63.966,121.45c0,84.162 -72.343,152.39 -161.587,152.39S91.353,414.821 91.353,330.659c0,-51.03 26.6,-96.205 67.432,-123.865c-25.558,-25.957 -54.263,-66.43 -68.965,-77.67c-20.877,-15.96 -58.938,-13.506 -58.938,-13.506zM179.45,330.49c0,40.01 32.98,72.44 73.664,72.44s73.67,-32.435 73.67,-72.44s-32.98,-72.44 -73.67,-72.44c-40.688,0 -73.664,32.436 -73.664,72.44"/></svg>'

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

# Scale bull to ~70% of canvas (leave room around edges)
BULL_SIZE = int(W * 0.78)
bull = bull.resize((BULL_SIZE, BULL_SIZE), Image.LANCZOS)

# ── 1. Diagonal gradient background ────────────────────────
img = Image.new('RGB', (W, H), (255, 107, 53))
draw = ImageDraw.Draw(img)
top_color    = (255, 145, 80)    # bright top-left
bot_color    = (190, 70, 25)     # deep bottom-right
for y in range(H):
    for x in range(0, W, 4):
        # Distance from top-left corner (normalized)
        t = ((x / W) * 0.55 + (y / H) * 0.45)
        t = max(0.0, min(1.0, t))
        r = int(top_color[0] + (bot_color[0] - top_color[0]) * t)
        g = int(top_color[1] + (bot_color[1] - top_color[1]) * t)
        b = int(top_color[2] + (bot_color[2] - top_color[2]) * t)
        draw.rectangle([x, y, x + 4, y + 1], fill=(r, g, b))

# ── 2. Soft radial highlight from top-left (sun glow) ──────
glow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
gdraw = ImageDraw.Draw(glow)
sun_x, sun_y = 80, 60
for r in range(450, 60, -8):
    alpha = max(0, int(35 - r * 0.06))
    if alpha <= 0: continue
    gdraw.ellipse([sun_x - r, sun_y - r, sun_x + r, sun_y + r],
                  fill=(255, 245, 220, alpha))
glow = glow.filter(ImageFilter.GaussianBlur(8))
img = Image.alpha_composite(img.convert('RGBA'), glow)

# ── 3. Bottom-right vignette for depth ─────────────────────
vig = Image.new('RGBA', (W, H), (0, 0, 0, 0))
vdr = ImageDraw.Draw(vig)
vig_x, vig_y = W + 30, H + 30
for r in range(380, 100, -8):
    alpha = max(0, int(28 - r * 0.045))
    if alpha <= 0: continue
    vdr.ellipse([vig_x - r, vig_y - r, vig_x + r, vig_y + r],
                fill=(0, 0, 0, alpha))
vig = vig.filter(ImageFilter.GaussianBlur(20))
img = Image.alpha_composite(img, vig)

# ── 4. Drop shadow under bull ──────────────────────────────
SHADOW_OFFSET_X = 6
SHADOW_OFFSET_Y = 14
SHADOW_BLUR = 18

# Build a black version of the bull as a shadow
shadow = Image.new('RGBA', bull.size, (0, 0, 0, 0))
shadow_data = []
for r, g, b, a in bull.getdata():
    shadow_data.append((0, 0, 0, min(int(a * 0.55), 140)))
shadow.putdata(shadow_data)
shadow = shadow.filter(ImageFilter.GaussianBlur(SHADOW_BLUR))

# Position bull centered
bull_x = (W - BULL_SIZE) // 2
bull_y = (H - BULL_SIZE) // 2

img.paste(shadow,
          (bull_x + SHADOW_OFFSET_X, bull_y + SHADOW_OFFSET_Y),
          shadow)

# ── 5. Bull with subtle top-edge highlight ─────────────────
# Create a highlight version: shift bull up 3px and use as semi-
# transparent white glow above the actual bull → fakes a bevel.
highlight = Image.new('RGBA', bull.size, (0, 0, 0, 0))
hl_data = []
for r, g, b, a in bull.getdata():
    if a > 0:
        hl_data.append((255, 255, 255, min(int(a * 0.30), 80)))
    else:
        hl_data.append((0, 0, 0, 0))
highlight.putdata(hl_data)
highlight = highlight.filter(ImageFilter.GaussianBlur(2))
# Paste highlight 3px ABOVE the bull (creates top-edge gloss)
img.paste(highlight, (bull_x, bull_y - 3), highlight)

# Now paste the bull itself
img.paste(bull, (bull_x, bull_y), bull)

# ── 6. Top-half gloss sweep (subtle white diagonal gradient) ─
gloss = Image.new('RGBA', (W, H), (0, 0, 0, 0))
gldr = ImageDraw.Draw(gloss)
gldr.polygon([
    (0, 0),
    (W * 0.65, 0),
    (W * 0.20, H * 0.55),
    (0, H * 0.40)
], fill=(255, 255, 255, 22))
gloss = gloss.filter(ImageFilter.GaussianBlur(40))
img = Image.alpha_composite(img, gloss)

# ── 7. Subtle outer rim highlight (1px white at edges) ──────
# Only the very top edge gets a slight light catch
rim = Image.new('RGBA', (W, H), (0, 0, 0, 0))
rd = ImageDraw.Draw(rim)
rd.rectangle([0, 0, W, 3], fill=(255, 255, 255, 35))
rim = rim.filter(ImageFilter.GaussianBlur(2))
img = Image.alpha_composite(img, rim)

# ── Save ───────────────────────────────────────────────────
out = 'docs/play_assets/icon_512.png'
img.convert('RGB').save(out, 'PNG', optimize=True)
os.remove(bull_marker_path)
print('Wrote', out, os.path.getsize(out), 'bytes', img.size)
