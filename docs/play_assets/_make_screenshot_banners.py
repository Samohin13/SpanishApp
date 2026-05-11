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


def pick_uniform_sizes(captions, inner_w, head_start, sub_start):
    """Find one headline size + one subtitle size that fit ALL captions.
    Returns (head_size, sub_size). Uniform sizes = consistent visual weight
    across the whole screenshot set."""
    head_size = head_start
    while head_size > 20:
        f = ImageFont.truetype(FONT_BLACK, head_size)
        if all(f.getbbox(ln)[2] - f.getbbox(ln)[0] <= inner_w
               for _, _, head, _ in captions
               for ln in head.split('\n')):
            break
        head_size -= 2

    sub_size = sub_start
    while sub_size > 14:
        f = ImageFont.truetype(FONT_MEDIUM, sub_size)
        if all(f.getbbox(sub)[2] - f.getbbox(sub)[0] <= inner_w
               for _, _, _, sub in captions):
            break
        sub_size -= 2

    return head_size, sub_size


def add_glass_banner(img: Image.Image, eyebrow: str,
                     headline: str, subtitle: str,
                     head_size: int, sub_size: int) -> Image.Image:
    img = img.convert('RGB')
    W, H = img.size

    # ── Geometry: compact card — wide for big text, short so text fills it
    margin   = int(W * 0.07)             # wide card → wide inner_w → big text
    banner_w = W - margin * 2
    banner_h = int(H * 0.19)             # back to compact
    banner_x = margin
    banner_y = (H - banner_h) // 2
    radius   = int(banner_h * 0.11)

    # Rounded mask
    mask = Image.new('L', (banner_w, banner_h), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, banner_w, banner_h], radius=radius, fill=255)

    # 1. Backdrop blur (the glass)
    region  = img.crop((banner_x, banner_y,
                        banner_x + banner_w, banner_y + banner_h)).convert('RGBA')
    blurred = region.filter(ImageFilter.GaussianBlur(14))   # softer blur, more shape recognizable

    # 2. Dark vertical gradient tint — denser glass per user feedback
    tint = Image.new('RGBA', (banner_w, banner_h), (0, 0, 0, 0))
    tdraw = ImageDraw.Draw(tint)
    for y in range(banner_h):
        t = y / banner_h
        a = int(105 + 35 * t)            # 105 → 140 — see-through glass
        tdraw.line([(0, y), (banner_w, y)], fill=(8, 10, 16, a))
    panel = Image.alpha_composite(blurred, tint)

    # 3. Single hairline border — clean, uniform glass (no inner bands)
    border = Image.new('RGBA', (banner_w, banner_h), (0, 0, 0, 0))
    ImageDraw.Draw(border).rounded_rectangle(
        [1, 1, banner_w - 2, banner_h - 2],
        radius=radius, outline=(255, 255, 255, 130), width=2)
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
    pad_x  = int(banner_w * 0.045)       # tighter padding → more text width
    inner_w = banner_w - pad_x * 2

    # Uniform sizes across all banners (set by caller)
    f_eyebrow = ImageFont.truetype(FONT_BLACK, int(banner_h * 0.075))
    f_head    = ImageFont.truetype(FONT_BLACK,  head_size)
    f_sub     = ImageFont.truetype(FONT_MEDIUM, sub_size)

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

    # ── Tight group: eyebrow → headline → subtitle, centered vertically ──
    # Premium ad pattern: text reads as ONE block, not three floating zones.
    text_x = banner_x + pad_x

    gap_eb_head  = int(f_head.size  * 0.40)   # eyebrow → headline
    gap_head_sub = int(f_head.size  * 0.55)   # headline → subtitle
    line_gap     = int(head_line_h  * 0.10)   # between headline lines

    head_block_h = head_line_h * len(head_lines) + line_gap * (len(head_lines) - 1)
    block_h = eb_h + gap_eb_head + head_block_h + gap_head_sub + sub_h
    block_y = banner_y + (banner_h - block_h) // 2

    # 1) eyebrow (warm orange)
    eyebrow_y = block_y
    draw.text((text_x, eyebrow_y - eb_bbox[1]), eyebrow_spaced,
              font=f_eyebrow, fill=(255, 140, 70))

    # 2) headline — soft dark glow then white type on top
    head_y_top = eyebrow_y + eb_h + gap_eb_head
    glow_layer = Image.new('RGBA', out.size, (0, 0, 0, 0))
    glow_draw  = ImageDraw.Draw(glow_layer)
    y_cursor = head_y_top
    for ln, bbx in zip(head_lines, head_metrics):
        glow_draw.text((text_x, y_cursor - bbx[1]), ln,
                       font=f_head, fill=(0, 0, 0, 220))
        y_cursor += head_line_h + line_gap
    glow_layer = glow_layer.filter(ImageFilter.GaussianBlur(6))
    out = Image.alpha_composite(out, glow_layer)
    draw = ImageDraw.Draw(out)
    y_cursor = head_y_top
    for ln, bbx in zip(head_lines, head_metrics):
        draw.text((text_x, y_cursor - bbx[1]), ln,
                  font=f_head, fill=(255, 255, 255))
        y_cursor += head_line_h + line_gap

    # 3) subtitle (warm light)
    sub_y_top = head_y_top + head_block_h + gap_head_sub
    draw.text((text_x, sub_y_top - sub_bbox[1]), subtitle,
              font=f_sub, fill=(235, 215, 195))

    return out.convert('RGB')


# ── Determine UNIFORM font sizes once, using the first screenshot's width ──
_probe = Image.open(os.path.join(SRC, CAPTIONS[0][0]))
_W, _H = _probe.size
_margin   = int(_W * 0.04)
_banner_w = _W - _margin * 2
_pad_x    = int(_banner_w * 0.045)
_inner_w  = _banner_w - _pad_x * 2
_banner_h = int(_H * 0.26)

HEAD_SIZE, SUB_SIZE = pick_uniform_sizes(
    CAPTIONS, _inner_w,
    head_start=int(_banner_h * 0.22),
    sub_start=int(_banner_h * 0.085),
)
print(f'Uniform sizes: headline={HEAD_SIZE}px  subtitle={SUB_SIZE}px')

# ── Run ──
for fname, eb, head, sub in CAPTIONS:
    src = os.path.join(SRC, fname)
    if not os.path.exists(src):
        print('SKIP', fname); continue
    out = add_glass_banner(Image.open(src), eb, head, sub,
                           HEAD_SIZE, SUB_SIZE)
    dst = os.path.join(DST, fname.replace('.jpg', '_promo.jpg'))
    out.save(dst, 'JPEG', quality=92, optimize=True)
    print('Wrote', dst)
