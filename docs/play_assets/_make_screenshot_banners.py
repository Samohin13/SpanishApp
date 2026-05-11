"""Premium glassmorphism feature banners for Play Store screenshots.

For each of 9 screenshots, produces a promo version with a centered
frosted-glass card containing punchy marketing copy in Montserrat.
"""
from PIL import Image, ImageDraw, ImageFilter, ImageFont
import os

SRC = 'docs/play_assets/screenshots'
DST = 'docs/play_assets/screenshots_promo'
os.makedirs(DST, exist_ok=True)

FONT_BLACK  = 'docs/play_assets/_fonts/Montserrat-Black.ttf'
FONT_MEDIUM = 'docs/play_assets/_fonts/Montserrat-Medium.ttf'

# (filename, eyebrow, headline, subtitle)
CAPTIONS = [
    ('Screenshot_20260511_094841_ESPEAK.jpg',
     'ИГРЫ',
     'Учи играя',
     '6 авторских игр · тренажёр глаголов · 100 рассказов'),
    ('Screenshot_20260511_094848_ESPEAK.jpg',
     'СЛОВАРЬ',
     '5000+ слов\nв твоём кармане',
     'Учи · слушай · собирай списки · делись'),
    ('Screenshot_20260511_095005_ESPEAK.jpg',
     'ГЛАВНАЯ',
     'Один экран —\nвесь твой испанский',
     'Урок · слово дня · рейтинг · цель'),
    ('Screenshot_20260511_095017_ESPEAK.jpg',
     'LIBROS',
     'Читай. Слушай.\nГовори как носитель.',
     '100 рассказов · озвучка · проверка произношения'),
    ('Screenshot_20260511_095026_ESPEAK.jpg',
     'CRUCIGRAMA',
     'Кроссворды,\nкоторые качают рейтинг',
     '100 уровней растущей сложности'),
    ('Screenshot_20260511_095034_ESPEAK.jpg',
     'CÁLCULO',
     'Числа на слух —\nкак носитель',
     'Тренируй устный счёт · 100 уровней'),
    ('Screenshot_20260511_095044_ESPEAK.jpg',
     'VERBOS',
     'Победи спряжения',
     '6 времён · 1000+ форм · 4 режима тренировки'),
    ('Screenshot_20260511_095051_ESPEAK.jpg',
     'КАРТОЧКИ',
     'Запомнить навсегда —\nреально',
     'Алгоритм SM-2 · повторяй умно, а не часто'),
]


def fit_font(text, font_path, max_width, start_size, min_size=18):
    """Largest size where every line fits max_width."""
    size = start_size
    while size > min_size:
        f = ImageFont.truetype(font_path, size)
        if all(f.getbbox(line)[2] - f.getbbox(line)[0] <= max_width
               for line in text.split('\n')):
            return f
        size -= 2
    return ImageFont.truetype(font_path, min_size)


def add_glass_banner(img: Image.Image, eyebrow: str,
                     headline: str, subtitle: str) -> Image.Image:
    img = img.convert('RGB')
    W, H = img.size

    # ── Geometry: centered card, ~88% width, height grows to fit text ──
    margin   = int(W * 0.06)
    banner_w = W - margin * 2
    banner_h = int(H * 0.26)             # taller so it feels premium
    banner_x = margin
    banner_y = (H - banner_h) // 2       # vertical center
    radius   = int(banner_h * 0.10)

    # Rounded mask
    mask = Image.new('L', (banner_w, banner_h), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, banner_w, banner_h], radius=radius, fill=255)

    # 1. Backdrop blur (the glass)
    region  = img.crop((banner_x, banner_y,
                        banner_x + banner_w, banner_y + banner_h)).convert('RGBA')
    blurred = region.filter(ImageFilter.GaussianBlur(26))

    # 2. Dark vertical gradient tint — denser glass per user feedback
    tint = Image.new('RGBA', (banner_w, banner_h), (0, 0, 0, 0))
    tdraw = ImageDraw.Draw(tint)
    for y in range(banner_h):
        t = y / banner_h
        a = int(190 + 25 * t)            # 190 → 215 (was 140 → 175)
        tdraw.line([(0, y), (banner_w, y)], fill=(8, 10, 16, a))
    panel = Image.alpha_composite(blurred, tint)

    # 3. Diagonal top-left sheen — premium glass sweep
    sheen = Image.new('RGBA', (banner_w, banner_h), (0, 0, 0, 0))
    sdr = ImageDraw.Draw(sheen)
    sdr.polygon([
        (0, 0),
        (int(banner_w * 0.55), 0),
        (int(banner_w * 0.18), banner_h),
        (0, banner_h),
    ], fill=(255, 255, 255, 28))
    sheen = sheen.filter(ImageFilter.GaussianBlur(35))
    panel = Image.alpha_composite(panel, sheen)

    # 4. Inner top highlight — bright thin band right under the border
    hl = Image.new('RGBA', (banner_w, banner_h), (0, 0, 0, 0))
    ImageDraw.Draw(hl).rounded_rectangle(
        [4, 2, banner_w - 4, int(banner_h * 0.10)],
        radius=radius // 2, fill=(255, 255, 255, 35))
    hl = hl.filter(ImageFilter.GaussianBlur(4))
    panel = Image.alpha_composite(panel, hl)

    # 5. Double border — outer hairline + inner hairline (luxe stacked feel)
    border = Image.new('RGBA', (banner_w, banner_h), (0, 0, 0, 0))
    bdr = ImageDraw.Draw(border)
    bdr.rounded_rectangle(
        [1, 1, banner_w - 2, banner_h - 2],
        radius=radius, outline=(255, 255, 255, 130), width=2)
    bdr.rounded_rectangle(
        [6, 6, banner_w - 7, banner_h - 7],
        radius=max(radius - 5, 6), outline=(255, 255, 255, 30), width=1)
    panel = Image.alpha_composite(panel, border)

    # Clip panel to rounded mask
    panel.putalpha(mask)

    # 5. Drop shadow on full canvas
    shadow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(
        [banner_x + 4, banner_y + 18,
         banner_x + banner_w + 4, banner_y + banner_h + 18],
        radius=radius, fill=(0, 0, 0, 150))
    shadow = shadow.filter(ImageFilter.GaussianBlur(30))

    out = Image.alpha_composite(img.convert('RGBA'), shadow)
    panel_canvas = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    panel_canvas.paste(panel, (banner_x, banner_y))
    out = Image.alpha_composite(out, panel_canvas)

    # ── Typography ──
    pad_x  = int(banner_w * 0.07)
    inner_w = banner_w - pad_x * 2

    # Eyebrow: small, tracked, orange — like a section label
    f_eyebrow = ImageFont.truetype(FONT_BLACK, int(banner_h * 0.075))
    # Headline: huge, black weight
    f_head    = fit_font(headline, FONT_BLACK,  inner_w,
                         int(banner_h * 0.21), min_size=28)
    # Subtitle: medium weight
    f_sub     = fit_font(subtitle, FONT_MEDIUM, inner_w,
                         int(banner_h * 0.085), min_size=18)

    draw = ImageDraw.Draw(out)

    # Letter-space the eyebrow manually
    eyebrow_spaced = ' '.join(list(eyebrow))
    eb_bbox = draw.textbbox((0, 0), eyebrow_spaced, font=f_eyebrow)
    eb_h    = eb_bbox[3] - eb_bbox[1]

    head_lines = headline.split('\n')
    head_metrics = [draw.textbbox((0, 0), ln, font=f_head) for ln in head_lines]
    head_line_h  = max(b[3] - b[1] for b in head_metrics)
    head_total_h = head_line_h * len(head_lines) + int(head_line_h * 0.18) * (len(head_lines) - 1)

    sub_bbox = draw.textbbox((0, 0), subtitle, font=f_sub)
    sub_h    = sub_bbox[3] - sub_bbox[1]

    gap_eb_head   = int(banner_h * 0.06)
    gap_head_sub  = int(banner_h * 0.08)
    block_h = eb_h + gap_eb_head + head_total_h + gap_head_sub + sub_h
    block_y = banner_y + (banner_h - block_h) // 2

    text_x = banner_x + pad_x

    # 1) eyebrow (warm orange)
    draw.text((text_x, block_y - eb_bbox[1]), eyebrow_spaced,
              font=f_eyebrow, fill=(255, 140, 70))

    # 2) headline (white) — drop shadow + main
    y_cursor = block_y + eb_h + gap_eb_head
    for ln, bbx in zip(head_lines, head_metrics):
        draw.text((text_x + 2, y_cursor - bbx[1] + 3), ln,
                  font=f_head, fill=(0, 0, 0, 180))
        draw.text((text_x,     y_cursor - bbx[1]),     ln,
                  font=f_head, fill=(255, 255, 255))
        y_cursor += head_line_h + int(head_line_h * 0.18)

    # 3) subtitle (warm light)
    sub_y = block_y + eb_h + gap_eb_head + head_total_h + gap_head_sub
    draw.text((text_x, sub_y - sub_bbox[1]), subtitle,
              font=f_sub, fill=(235, 215, 195))

    return out.convert('RGB')


# ── Run ──
for fname, eb, head, sub in CAPTIONS:
    src = os.path.join(SRC, fname)
    if not os.path.exists(src):
        print('SKIP', fname); continue
    out = add_glass_banner(Image.open(src), eb, head, sub)
    dst = os.path.join(DST, fname.replace('.jpg', '_promo.jpg'))
    out.save(dst, 'JPEG', quality=92, optimize=True)
    print('Wrote', dst)
