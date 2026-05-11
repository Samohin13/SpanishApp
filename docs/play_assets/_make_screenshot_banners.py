"""Adds glassmorphism feature banners to Play Store screenshots.

For each of 9 screenshots in docs/play_assets/screenshots/, generates
a promo version in docs/play_assets/screenshots_promo/ with a frosted
glass overlay near the top describing the feature.

Glassmorphism = backdrop-blur + semi-transparent fill + thin border +
subtle drop shadow. Implemented via PIL: blur the region under the
panel, paste it back, overlay a translucent rounded rect, draw text.
"""
from PIL import Image, ImageDraw, ImageFilter, ImageFont
import os

SRC = 'docs/play_assets/screenshots'
DST = 'docs/play_assets/screenshots_promo'
os.makedirs(DST, exist_ok=True)

# (filename, title, subtitle)
CAPTIONS = [
    ('Screenshot_20260511_094841_ESPEAK.jpg',
     '8 мини-игр',
     'Артикли · спряжения · кроссворд · ещё 5'),
    ('Screenshot_20260511_094848_ESPEAK.jpg',
     '5000+ слов с озвучкой',
     'Поиск, фразы, личные списки'),
    ('Screenshot_20260511_095005_ESPEAK.jpg',
     'Твой день — в одном экране',
     'Урок · слово дня · рейтинг · цель'),
    ('Screenshot_20260511_095017_ESPEAK.jpg',
     '100 рассказов с тестами',
     'Читай — и сразу проверяй понимание'),
    ('Screenshot_20260511_095026_ESPEAK.jpg',
     'Кроссворды на испанском',
     '100 уровней растущей сложности'),
    ('Screenshot_20260511_095034_ESPEAK.jpg',
     'Числа на слух',
     'Тренируй устный счёт по-испански'),
    ('Screenshot_20260511_095044_ESPEAK.jpg',
     'Спряжения 160 глаголов',
     '4 режима: Conjugar · Inverso · Hueco · Auditivo'),
    ('Screenshot_20260511_095051_ESPEAK.jpg',
     'Умные карточки SM-2',
     'Учишь только то, что начал забывать'),
    ('Screenshot_20260511_095101_ESPEAK.jpg',
     'Перевод и пример в один тап',
     'С нативной озвучкой'),
]

# ── Font discovery ──
def find_font(candidates):
    for p in candidates:
        if os.path.exists(p):
            return p
    return None

FONT_BOLD = find_font([
    'C:/Windows/Fonts/seguibl.ttf',     # Segoe UI Black
    'C:/Windows/Fonts/arialbd.ttf',
])
FONT_REG = find_font([
    'C:/Windows/Fonts/segoeui.ttf',
    'C:/Windows/Fonts/arial.ttf',
])

# ── Glass panel renderer ──
def fit_font(text, font_path, max_width, start_size, min_size=14):
    """Pick the largest font size <= start_size such that text fits max_width."""
    size = start_size
    while size > min_size:
        f = ImageFont.truetype(font_path, size)
        bbox = f.getbbox(text)
        if bbox[2] - bbox[0] <= max_width:
            return f
        size -= 2
    return ImageFont.truetype(font_path, min_size)


def add_glass_banner(img: Image.Image, title: str, subtitle: str) -> Image.Image:
    """Returns a copy of img with a frosted glass banner near the bottom
    (just above the bottom navigation bar)."""
    img = img.convert('RGB')
    W, H = img.size

    # Banner geometry — sit ABOVE the bottom nav, full screen width-ish.
    margin = int(W * 0.05)
    banner_w = W - margin * 2
    banner_h = int(H * 0.15)
    banner_x = margin
    # Bottom nav is ~10% of H. Leave a 4% gap above it.
    banner_y = H - int(H * 0.10) - banner_h - int(H * 0.04)
    radius = int(banner_h * 0.20)

    # ── Build panel as one RGBA layer, then alpha_composite onto image ──

    # Rounded mask for the panel shape
    mask = Image.new('L', (banner_w, banner_h), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, banner_w, banner_h], radius=radius, fill=255)

    # 1. Blurred backdrop (the actual "glass" effect): take the region under
    #    the banner from the original screenshot, heavy blur, clip to mask.
    region = img.crop((banner_x, banner_y,
                       banner_x + banner_w, banner_y + banner_h)).convert('RGBA')
    blurred = region.filter(ImageFilter.GaussianBlur(22))

    # 2. Dark tint composited ONTO the blurred region (this is the trick:
    #    use alpha_composite so the tint blends, not replaces).
    tint = Image.new('RGBA', (banner_w, banner_h), (10, 12, 18, 165))
    panel_rgba = Image.alpha_composite(blurred, tint)

    # 3. Subtle top sheen — composite, not paste.
    sheen = Image.new('RGBA', (banner_w, banner_h), (0, 0, 0, 0))
    ImageDraw.Draw(sheen).rounded_rectangle(
        [0, 0, banner_w, int(banner_h * 0.45)],
        radius=radius, fill=(255, 255, 255, 18))
    sheen = sheen.filter(ImageFilter.GaussianBlur(10))
    panel_rgba = Image.alpha_composite(panel_rgba, sheen)

    # 4. Thin border for the glass edge
    border = Image.new('RGBA', (banner_w, banner_h), (0, 0, 0, 0))
    ImageDraw.Draw(border).rounded_rectangle(
        [1, 1, banner_w - 2, banner_h - 2],
        radius=radius, outline=(255, 255, 255, 110), width=2)
    panel_rgba = Image.alpha_composite(panel_rgba, border)

    # 5. Apply rounded mask to panel's alpha channel (so corners are clipped)
    r_ch, g_ch, b_ch, a_ch = panel_rgba.split()
    a_ch = ImageDraw.Draw(Image.new('L', (banner_w, banner_h), 0))  # noqa
    a_full = Image.new('L', (banner_w, banner_h), 255)
    # Multiply mask × full = mask
    panel_rgba.putalpha(mask)

    # 6. Drop shadow under panel
    shadow_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(shadow_layer).rounded_rectangle(
        [banner_x + 4, banner_y + 14,
         banner_x + banner_w + 4, banner_y + banner_h + 14],
        radius=radius, fill=(0, 0, 0, 130))
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(22))

    out = Image.alpha_composite(img.convert('RGBA'), shadow_layer)

    # 7. Composite the panel onto the output (NOT paste — composite blends
    #    using the panel's own alpha, no transparency leaks into JPEG).
    panel_canvas = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    panel_canvas.paste(panel_rgba, (banner_x, banner_y))
    out = Image.alpha_composite(out, panel_canvas)

    # 7. Text — auto-shrink to fit width
    text_pad_x = int(banner_w * 0.06)
    inner_w = banner_w - text_pad_x * 2

    title_start = int(banner_h * 0.32)
    sub_start   = int(banner_h * 0.18)
    f_title = fit_font(title,    FONT_BOLD, inner_w, title_start, min_size=22)
    f_sub   = fit_font(subtitle, FONT_REG,  inner_w, sub_start,   min_size=16)

    draw = ImageDraw.Draw(out)
    t_bbox = draw.textbbox((0, 0), title, font=f_title)
    s_bbox = draw.textbbox((0, 0), subtitle, font=f_sub)
    t_h = t_bbox[3] - t_bbox[1]
    s_h = s_bbox[3] - s_bbox[1]
    gap = int(banner_h * 0.10)
    block_h = t_h + gap + s_h
    text_y = banner_y + (banner_h - block_h) // 2 - t_bbox[1]
    text_x = banner_x + text_pad_x

    # title shadow + text (white)
    draw.text((text_x + 2, text_y + 3), title, font=f_title, fill=(0, 0, 0, 160))
    draw.text((text_x, text_y), title, font=f_title, fill=(255, 255, 255))

    # subtitle (warm orange — brand accent)
    draw.text((text_x, text_y + t_h + gap), subtitle,
              font=f_sub, fill=(255, 170, 110))

    return out.convert('RGB')


# ── Run ──
for fname, title, sub in CAPTIONS:
    src_path = os.path.join(SRC, fname)
    if not os.path.exists(src_path):
        print('SKIP missing', fname); continue
    img = Image.open(src_path)
    out = add_glass_banner(img, title, sub)
    dst_path = os.path.join(DST, fname.replace('.jpg', '_promo.jpg'))
    out.save(dst_path, 'JPEG', quality=92, optimize=True)
    print('Wrote', dst_path)
