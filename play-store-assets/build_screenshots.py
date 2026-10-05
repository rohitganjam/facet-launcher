#!/usr/bin/env python3
"""Build the Play Store phone screenshots (1080x1920) from raw emulator captures.

Each slide = logo + tracked eyebrow label + headline + the raw capture (status bar
cropped off) in a rounded, outlined frame. Raw captures live in raw/ (1080x2400,
taken on the emulator); output goes to screenshots/.

Usage (from play-store-assets/):
    python3 build_screenshots.py            # build all slides
    python3 build_screenshots.py home       # build one slide by raw name
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

HERE = Path(__file__).parent
CANVAS = (1080, 1920)
BG = (244, 245, 247)
EYEBROW = (44, 104, 96)
HEADLINE = (30, 48, 72)
FRAME = (28, 36, 48)
FONT = "/System/Library/Fonts/HelveticaNeue.ttc"
BOLD = 1
STATUS_BAR_PX = 66  # cropped off every raw capture
PHONE_W = 680
PHONE_TOP = 400
PHONE_H = 1470
MAX_PHONE_W = 820
RADIUS = 44
LOGO = 64

# (raw name, output name, eyebrow, headline[, crop_top])
# crop_top replaces the status-bar crop and scales the phone up to fill the slide height.
SLIDES = [
    ("facets", "01_facets", "FACETS", "Switch your whole setup in one swipe"),
    ("home", "02_home", "HOME", "A launcher built around how you use your phone"),
    ("clock_styles", "03_clock_styles", "HOME & CLOCK", "30+ clock styles, fully re-themeable"),
    ("drawer", "04_drawer", "APP DRAWER", "List or grid, your call"),
    ("search", "05_search", "SEARCH", "Find an app or a contact, then act in one tap"),
    ("widgets", "06_widgets", "WIDGETS", "Widgets have their own home"),
    ("folders", "07_folders", "FOLDERS", "Group apps your way"),
    ("appearance", "08_appearance", "APPEARANCE", "Your launcher, your look", 468),
]


def tracked(draw, cx, top, text, font, fill, tracking=4):
    widths = [draw.textlength(c, font=font) for c in text]
    x = cx - (sum(widths) + tracking * (len(text) - 1)) / 2
    for c, w in zip(text, widths):
        draw.text((x, top), c, font=font, fill=fill)
        x += w + tracking


def _greedy(draw, text, font, max_w):
    lines, cur = [], ""
    for word in text.split():
        trial = f"{cur} {word}".strip()
        if draw.textlength(trial, font=font) <= max_w or not cur:
            cur = trial
        else:
            lines.append(cur)
            cur = word
    return lines + [cur]


def wrap(draw, text, font, max_w):
    """Greedy wrap, then tighten the width so lines come out balanced."""
    n = len(_greedy(draw, text, font, max_w))
    best = max_w
    while best > 200 and len(_greedy(draw, text, font, best - 10)) == n:
        best -= 10
    return _greedy(draw, text, font, best)


def build(raw: str, out: str, eyebrow: str, headline: str, crop_top: int | None = None) -> None:
    img = Image.new("RGB", CANVAS, BG)
    d = ImageDraw.Draw(img)

    logo = Image.open(HERE / "icon-512.png").convert("RGBA").resize((LOGO, LOGO), Image.LANCZOS)
    img.paste(logo, ((CANVAS[0] - LOGO) // 2, 52), logo)

    tracked(d, CANVAS[0] / 2, 142, eyebrow, ImageFont.truetype(FONT, 28, index=BOLD), EYEBROW)

    font = ImageFont.truetype(FONT, 72, index=BOLD)
    y = 196
    for line in wrap(d, headline, font, CANVAS[0] - 140):
        d.text(((CANVAS[0] - d.textlength(line, font=font)) / 2, y), line, font=font, fill=HEADLINE)
        y += 84

    shot = Image.open(HERE / "raw" / f"{raw}.png").convert("RGB")
    shot = shot.crop((0, crop_top or STATUS_BAR_PX, shot.width, shot.height))
    phone_w = PHONE_W if crop_top is None else min(MAX_PHONE_W, round(PHONE_H * shot.width / shot.height))
    ph = round(phone_w * shot.height / shot.width)
    shot = shot.resize((phone_w, ph), Image.LANCZOS)
    left = (CANVAS[0] - phone_w) // 2

    mask = Image.new("L", (phone_w, ph), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, phone_w - 1, ph - 1], radius=RADIUS, fill=255)

    shadow = Image.new("L", CANVAS, 0)
    ImageDraw.Draw(shadow).rounded_rectangle(
        [left, PHONE_TOP + 10, left + phone_w, PHONE_TOP + ph + 10], radius=RADIUS, fill=70
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(14))
    img.paste(Image.new("RGB", CANVAS, (20, 28, 40)), (0, 0), shadow)

    img.paste(shot, (left, PHONE_TOP), mask)
    ImageDraw.Draw(img).rounded_rectangle(
        [left - 1, PHONE_TOP - 1, left + phone_w, PHONE_TOP + ph], radius=RADIUS, outline=FRAME, width=4
    )

    path = HERE / "screenshots" / f"{out}.png"
    path.parent.mkdir(exist_ok=True)
    img.save(path)
    print(f"wrote {path.relative_to(HERE)} (phone bottom y={PHONE_TOP + ph})")


if __name__ == "__main__":
    only = set(sys.argv[1:])
    for raw, out, eyebrow, headline, *crop in SLIDES:
        if not only or raw in only:
            build(raw, out, eyebrow, headline, *crop)
