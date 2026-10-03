#!/usr/bin/env python3
"""Compose the 1200x630 Open Graph image website/src/assets/og.png from game
screenshots. Optional helper (needs Pillow); the output is committed so the
site build itself stays standard-library only.

usage: python tools/make_og.py
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402

W, H = 1200, 630


def font(size):
    for name in ("consolab.ttf", "DejaVuSansMono-Bold.ttf", "Menlo.ttc", "arialbd.ttf"):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def main():
    games = catalog.load_games()
    img = Image.new("RGB", (W, H), (7, 9, 15))
    d = ImageDraw.Draw(img)
    for x in range(0, W, 32):
        d.line([(x, 0), (x, H)], fill=(14, 18, 28))
    for y in range(0, H, 32):
        d.line([(0, y), (W, y)], fill=(14, 18, 28))
    picks = [g for g in games if g["featured"]][:12]
    tw, th = 132, 156
    x0, y0 = 600, 40
    for i, g in enumerate(picks):
        p = os.path.join(g["dir"], "media", "title.png")
        if not os.path.exists(p):
            continue
        shot = Image.open(p).convert("RGB").resize((tw, th), Image.NEAREST)
        x = x0 + (i % 4) * (tw + 12)
        y = y0 + (i // 4) * (th + 30)
        d.rectangle([x - 2, y - 2, x + tw + 1, y + th + 1], outline=(36, 52, 90))
        img.paste(shot, (x, y))
    d.text((48, 150), "J2ME", font=font(64), fill=(230, 236, 255))
    d.text((48, 230), "100 GAMES", font=font(72), fill=(77, 255, 154))
    d.text((48, 330), "Original retro games for", font=font(30), fill=(154, 168, 204))
    d.text((48, 370), "Java ME button phones", font=font(30), fill=(154, 168, 204))
    d.text((48, 450), "JAR + JAD  |  CLDC 1.0 / MIDP 2.0", font=font(24), fill=(255, 194, 77))
    d.text((48, 490), "browser demos for every game", font=font(24), fill=(255, 194, 77))
    out = os.path.join(catalog.ROOT, "website", "src", "assets", "og.png")
    img.save(out, optimize=True)
    print("wrote", out)


if __name__ == "__main__":
    main()
