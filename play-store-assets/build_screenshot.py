#!/usr/bin/env python3
"""Refresh one Play Store marketing screenshot after a raw device capture changes.

Rather than rebuilding the whole composed image (headline, background art, logo)
from scratch, this starts from the *existing* composite PNG and only touches the
two things that actually change when a screen's copy is renamed:

1. The phone-content rectangle — replaced wholesale with the new raw screenshot,
   rounded to match.
2. The small uppercase "eyebrow" label above the headline (e.g. "PROFILES") —
   erased and redrawn with the new word, in the same font/size/color/position.

Box coordinates below were measured directly off the existing composites
(play-store-assets/screenshots/02_profiles.png, 06_hub.png) via pixel scanning —
see chat history for the measurement script. They are NOT derived from a shared
template; each slide's phone box was sized independently, so a new slide needs its
own measured entry in SLIDES.

Usage:
    python3 build_screenshot.py --slide profiles --raw /path/to/raw_screenshot.png \
        --base play-store-assets/screenshots/02_profiles.png \
        --out play-store-assets/screenshots/02_facets.png --word FACETS

    python3 build_screenshot.py --slide hub --raw /path/to/raw_screenshot.png \
        --base play-store-assets/screenshots/06_hub.png \
        --out play-store-assets/screenshots/06_widgets.png --word WIDGETS
"""
import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

BG_COLOR = (244, 245, 247)
EYEBROW_COLOR = (44, 104, 96)
FONT_PATH = "/System/Library/Fonts/HelveticaNeue.ttc"
FONT_BOLD_INDEX = 1  # ('Helvetica Neue', 'Bold') -- verified via ImageFont.getname()
LETTER_TRACKING = 4  # px, matches the existing labels' tracked-out uppercase look

# left, top, width, height of the phone-content rectangle within the 1080x1920 canvas.
SLIDES = {
    "profiles": {
        "box": (204, 388, 671, 1492),
        "eyebrow_bbox": (470, 146, 596, 165),  # old "PROFILES" label extent
        "corner_radius_ratio": 0.047,
    },
    "hub": {
        "box": (188, 314, 704, 1566),
        "eyebrow_bbox": (444, 146, 614, 165),  # old "WIDGET HUB" label extent
        "corner_radius_ratio": 0.047,
    },
}


def rounded_mask(size: tuple[int, int], radius: int) -> Image.Image:
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size[0] - 1, size[1] - 1], radius=radius, fill=255)
    return mask


def draw_tracked_text(draw: ImageDraw.ImageDraw, center_x: float, top_y: float, text: str, font, fill, tracking: int):
    widths = [draw.textlength(ch, font=font) for ch in text]
    total = sum(widths) + tracking * (len(text) - 1)
    x = center_x - total / 2
    for ch, wch in zip(text, widths):
        draw.text((x, top_y), ch, font=font, fill=fill)
        x += wch + tracking


def build(slide: str, raw_path: Path, base_path: Path, out_path: Path, word: str) -> None:
    spec = SLIDES[slide]
    left, top, box_w, box_h = spec["box"]
    ex0, ey0, ex1, ey1 = spec["eyebrow_bbox"]

    base = Image.open(base_path).convert("RGB")

    # 1. Replace the phone content wholesale.
    shot = Image.open(raw_path).convert("RGB").resize((box_w, box_h), Image.LANCZOS)
    radius = round(spec["corner_radius_ratio"] * box_w)
    base.paste(shot, (left, top), rounded_mask((box_w, box_h), radius))

    # 2. Erase + redraw the eyebrow label.
    draw = ImageDraw.Draw(base)
    pad_x, pad_y = 60, 6
    draw.rectangle([ex0 - pad_x, ey0 - pad_y, ex1 + pad_x, ey1 + pad_y], fill=BG_COLOR)
    font_size = round((ey1 - ey0) / 0.72)
    font = ImageFont.truetype(FONT_PATH, font_size, index=FONT_BOLD_INDEX)
    draw_tracked_text(draw, (ex0 + ex1) / 2, ey0 - 2, word.upper(), font, EYEBROW_COLOR, LETTER_TRACKING)

    out_path.parent.mkdir(parents=True, exist_ok=True)
    base.save(out_path)
    print(f"wrote {out_path}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--slide", required=True, choices=sorted(SLIDES))
    parser.add_argument("--raw", required=True, type=Path, help="freshly captured raw device screenshot")
    parser.add_argument("--base", required=True, type=Path, help="existing composite PNG to start from")
    parser.add_argument("--out", required=True, type=Path)
    parser.add_argument("--word", required=True, help="new eyebrow label text, e.g. FACETS")
    args = parser.parse_args()
    build(args.slide, args.raw, args.base, args.out, args.word)
