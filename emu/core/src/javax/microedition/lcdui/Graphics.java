package javax.microedition.lcdui;

import emu.PixelFont;

/**
 * Emulator Graphics: a small software rasterizer over an ARGB int array.
 * Follows MIDP pixel conventions (drawRect covers w+1 x h+1, fillRect w x h)
 * and validates anchors strictly so layout bugs surface in testing.
 */
public class Graphics {
    public static final int HCENTER = 1, VCENTER = 2, LEFT = 4, RIGHT = 8, TOP = 16, BOTTOM = 32, BASELINE = 64;
    public static final int SOLID = 0, DOTTED = 1;

    private final int[] px;
    private final int sw, sh;
    private int tx, ty;
    private int cx0, cy0, cx1, cy1;
    private int color;
    private int stroke = SOLID;
    private Font font = Font.getDefaultFont();

    Graphics(int[] px, int w, int h) {
        this.px = px;
        this.sw = w;
        this.sh = h;
        cx1 = w;
        cy1 = h;
    }

    // ---------------------------------------------------------------- state

    public void translate(int x, int y) { tx += x; ty += y; }
    public int getTranslateX() { return tx; }
    public int getTranslateY() { return ty; }

    public int getColor() { return color; }
    public int getRedComponent() { return (color >> 16) & 0xFF; }
    public int getGreenComponent() { return (color >> 8) & 0xFF; }
    public int getBlueComponent() { return color & 0xFF; }
    public void setColor(int rgb) { color = rgb & 0xFFFFFF; }

    public void setColor(int r, int g, int b) {
        if ((r | g | b) < 0 || r > 255 || g > 255 || b > 255) throw new IllegalArgumentException();
        color = (r << 16) | (g << 8) | b;
    }

    public int getGrayScale() { return (getRedComponent() * 30 + getGreenComponent() * 59 + getBlueComponent() * 11) / 100; }
    public void setGrayScale(int v) { setColor(v, v, v); }
    public Font getFont() { return font; }
    public void setFont(Font f) { font = f == null ? Font.getDefaultFont() : f; }
    public int getStrokeStyle() { return stroke; }

    public void setStrokeStyle(int style) {
        if (style != SOLID && style != DOTTED) throw new IllegalArgumentException();
        stroke = style;
    }

    public int getDisplayColor(int c) { return c & 0xFFFFFF; }

    public int getClipX() { return cx0 - tx; }
    public int getClipY() { return cy0 - ty; }
    public int getClipWidth() { return cx1 - cx0; }
    public int getClipHeight() { return cy1 - cy0; }

    public void setClip(int x, int y, int w, int h) {
        x += tx;
        y += ty;
        cx0 = Math.max(0, x);
        cy0 = Math.max(0, y);
        cx1 = Math.min(sw, x + w);
        cy1 = Math.min(sh, y + h);
        if (cx1 < cx0) cx1 = cx0;
        if (cy1 < cy0) cy1 = cy0;
    }

    public void clipRect(int x, int y, int w, int h) {
        x += tx;
        y += ty;
        cx0 = Math.max(cx0, x);
        cy0 = Math.max(cy0, y);
        cx1 = Math.min(cx1, x + w);
        cy1 = Math.min(cy1, y + h);
        if (cx1 < cx0) cx1 = cx0;
        if (cy1 < cy0) cy1 = cy0;
    }

    // ------------------------------------------------------------ primitives

    private void pset(int x, int y) {
        if (x >= cx0 && x < cx1 && y >= cy0 && y < cy1) px[y * sw + x] = 0xFF000000 | color;
    }

    private void hspan(int x0, int x1, int y) { // absolute, inclusive
        if (y < cy0 || y >= cy1) return;
        if (x0 > x1) { int t = x0; x0 = x1; x1 = t; }
        if (x0 < cx0) x0 = cx0;
        if (x1 >= cx1) x1 = cx1 - 1;
        int c = 0xFF000000 | color;
        int o = y * sw;
        for (int x = x0; x <= x1; x++) px[o + x] = c;
    }

    public void fillRect(int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        x += tx;
        y += ty;
        int y1 = Math.min(cy1, y + h);
        for (int yy = Math.max(cy0, y); yy < y1; yy++) hspan(x, x + w - 1, yy);
    }

    public void drawRect(int x, int y, int w, int h) {
        if (w < 0 || h < 0) return;
        drawLine(x, y, x + w, y);
        drawLine(x, y + h, x + w, y + h);
        drawLine(x, y, x, y + h);
        drawLine(x + w, y, x + w, y + h);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        x1 += tx; y1 += ty; x2 += tx; y2 += ty;
        int dx = Math.abs(x2 - x1), dy = -Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx + dy, n = 0;
        while (true) {
            if (stroke == SOLID || (n & 2) == 0) pset(x1, y1);
            n++;
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x1 += sx; }
            if (e2 <= dx) { err += dx; y1 += sy; }
        }
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        x1 += tx; y1 += ty; x2 += tx; y2 += ty; x3 += tx; y3 += ty;
        int minY = Math.max(cy0, Math.min(y1, Math.min(y2, y3)));
        int maxY = Math.min(cy1 - 1, Math.max(y1, Math.max(y2, y3)));
        for (int y = minY; y <= maxY; y++) {
            double yc = y + 0.5;
            double lo = Double.MAX_VALUE, hi = -Double.MAX_VALUE;
            double[] xs = { x1, x2, x3, x1 };
            double[] ys = { y1, y2, y3, y1 };
            for (int i = 0; i < 3; i++) {
                double ya = ys[i], yb = ys[i + 1];
                if ((yc >= ya && yc <= yb) || (yc >= yb && yc <= ya)) {
                    double xi = ya == yb ? Math.min(xs[i], xs[i + 1]) : xs[i] + (yc - ya) * (xs[i + 1] - xs[i]) / (yb - ya);
                    if (ya == yb) {
                        lo = Math.min(lo, Math.min(xs[i], xs[i + 1]));
                        hi = Math.max(hi, Math.max(xs[i], xs[i + 1]));
                    } else {
                        lo = Math.min(lo, xi);
                        hi = Math.max(hi, xi);
                    }
                }
            }
            if (lo <= hi) {
                int a = (int) Math.round(lo), b = (int) Math.round(hi);
                if (b <= a) b = a + 1;
                hspan(a, b - 1, y);
            }
        }
        // make degenerate / thin triangles visible like most handsets do
        pset(x1, y1); pset(x2, y2); pset(x3, y3);
    }

    private static boolean inArc(double ang, int start, int arc) {
        if (arc >= 360 || arc <= -360) return true;
        if (arc < 0) { start += arc; arc = -arc; }
        double a = ang - start;
        a %= 360;
        if (a < 0) a += 360;
        return a <= arc;
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        if (w <= 0 || h <= 0 || arcAngle == 0) return;
        x += tx;
        y += ty;
        double rx = w / 2.0, ry = h / 2.0, cxd = x + rx, cyd = y + ry;
        for (int yy = Math.max(cy0, y); yy < Math.min(cy1, y + h); yy++) {
            double dy = (yy + 0.5 - cyd) / ry;
            if (dy * dy > 1) continue;
            for (int xx = Math.max(cx0, x); xx < Math.min(cx1, x + w); xx++) {
                double dx = (xx + 0.5 - cxd) / rx;
                if (dx * dx + dy * dy <= 1.0) {
                    double ang = Math.toDegrees(Math.atan2(-(yy + 0.5 - cyd) * rx / ry, xx + 0.5 - cxd));
                    if (inArc(ang, startAngle, arcAngle)) px[yy * sw + xx] = 0xFF000000 | color;
                }
            }
        }
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        if (w < 0 || h < 0 || arcAngle == 0) return;
        double rx = w / 2.0, ry = h / 2.0, cxd = x + rx, cyd = y + ry;
        int steps = Math.max(16, (int) ((rx + ry) * 4));
        int lastX = Integer.MIN_VALUE, lastY = 0;
        for (int i = 0; i <= steps; i++) {
            double t = Math.toRadians(360.0 * i / steps);
            double deg = Math.toDegrees(t);
            if (!inArc(deg, startAngle, arcAngle)) continue;
            int px0 = (int) Math.round(cxd + Math.cos(t) * rx);
            int py0 = (int) Math.round(cyd - Math.sin(t) * ry);
            if (px0 != lastX || py0 != lastY) pset(px0 + tx, py0 + ty);
            lastX = px0;
            lastY = py0;
        }
    }

    public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) {
        if (w <= 0 || h <= 0) return;
        double rx = Math.min(Math.abs(aw), w) / 2.0, ry = Math.min(Math.abs(ah), h) / 2.0;
        for (int j = 0; j < h; j++) {
            double inset = 0;
            double d = -1;
            if (j < ry) d = ry - (j + 0.5);
            else if (j >= h - ry) d = (j + 0.5) - (h - ry);
            if (d > 0 && ry > 0) {
                double f = d / ry;
                inset = rx - rx * Math.sqrt(Math.max(0, 1 - f * f));
            }
            int in = (int) Math.round(inset);
            if (w - 2 * in > 0) hspan(x + tx + in, x + tx + w - 1 - in, y + ty + j);
        }
    }

    public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) {
        if (w < 0 || h < 0) return;
        int rx = Math.min(Math.abs(aw), w) / 2, ry = Math.min(Math.abs(ah), h) / 2;
        drawLine(x + rx, y, x + w - rx, y);
        drawLine(x + rx, y + h, x + w - rx, y + h);
        drawLine(x, y + ry, x, y + h - ry);
        drawLine(x + w, y + ry, x + w, y + h - ry);
        if (rx > 0 && ry > 0) {
            drawArc(x, y, 2 * rx, 2 * ry, 90, 90);
            drawArc(x + w - 2 * rx, y, 2 * rx, 2 * ry, 0, 90);
            drawArc(x, y + h - 2 * ry, 2 * rx, 2 * ry, 180, 90);
            drawArc(x + w - 2 * rx, y + h - 2 * ry, 2 * rx, 2 * ry, 270, 90);
        }
    }

    // ------------------------------------------------------------------ text

    private static void checkTextAnchor(int a) {
        if (a == 0) return;
        int hz = a & (LEFT | RIGHT | HCENTER), vt = a & (TOP | BOTTOM | BASELINE);
        if ((a & ~(LEFT | RIGHT | HCENTER | TOP | BOTTOM | BASELINE)) != 0
                || Integer.bitCount(hz) != 1 || Integer.bitCount(vt) != 1) {
            throw new IllegalArgumentException("bad text anchor " + a);
        }
    }

    public void drawString(String str, int x, int y, int anchor) {
        drawSubstring(str, 0, str.length(), x, y, anchor);
    }

    public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
        checkTextAnchor(anchor);
        if (anchor == 0) anchor = TOP | LEFT;
        int w = len * font.advance;
        if ((anchor & HCENTER) != 0) x -= w / 2;
        else if ((anchor & RIGHT) != 0) x -= w;
        if ((anchor & BASELINE) != 0) y -= font.ascent;
        else if ((anchor & BOTTOM) != 0) y -= font.height;
        x += tx;
        y += ty;
        for (int i = 0; i < len; i++) {
            glyph(str.charAt(offset + i), x, y);
            x += font.advance;
        }
        if (font.isUnderlined()) hspan(x - w, x - 1, y + font.ascent + 1);
    }

    public void drawChar(char c, int x, int y, int anchor) {
        drawString(String.valueOf(c), x, y, anchor);
    }

    public void drawChars(char[] data, int offset, int length, int x, int y, int anchor) {
        drawString(new String(data, offset, length), x, y, anchor);
    }

    private void glyph(char c, int x, int y) {
        int s = font.scale;
        for (int r = 0; r < PixelFont.GH; r++) {
            int bits = PixelFont.row(c, r);
            if (bits == 0) continue;
            for (int col = 0; col < PixelFont.GW; col++) {
                if ((bits & (1 << (PixelFont.GW - 1 - col))) == 0) continue;
                int gx = x + col * s, gy = y + r * s;
                for (int yy = 0; yy < s; yy++) hspan(gx, gx + s - 1 + font.heavy, gy + yy);
            }
        }
    }

    // ---------------------------------------------------------------- images

    private static void checkImageAnchor(int a) {
        if (a == 0) return;
        int hz = a & (LEFT | RIGHT | HCENTER), vt = a & (TOP | BOTTOM | VCENTER);
        if ((a & ~(LEFT | RIGHT | HCENTER | TOP | BOTTOM | VCENTER)) != 0
                || Integer.bitCount(hz) != 1 || Integer.bitCount(vt) != 1) {
            throw new IllegalArgumentException("bad image anchor " + a);
        }
    }

    public void drawImage(Image img, int x, int y, int anchor) {
        drawRegion(img, 0, 0, img.w, img.h, 0, x, y, anchor);
    }

    public void drawRegion(Image src, int xs, int ys, int w, int h, int transform, int xd, int yd, int anchor) {
        if (src == null) throw new NullPointerException();
        if (xs < 0 || ys < 0 || w < 0 || h < 0 || xs + w > src.w || ys + h > src.h) throw new IllegalArgumentException();
        if (transform < 0 || transform > 7) throw new IllegalArgumentException();
        checkImageAnchor(anchor);
        if (anchor == 0) anchor = TOP | LEFT;
        boolean swap = (transform & 4) != 0;
        int tw = swap ? h : w, th = swap ? w : h;
        if ((anchor & HCENTER) != 0) xd -= tw / 2;
        else if ((anchor & RIGHT) != 0) xd -= tw;
        if ((anchor & VCENTER) != 0) yd -= th / 2;
        else if ((anchor & BOTTOM) != 0) yd -= th;
        blit(src, xs, ys, w, h, transform, px, sw, sh, xd + tx, yd + ty, cx0, cy0, cx1, cy1, true);
    }

    static void blit(Image src, int xs, int ys, int w, int h, int transform, int[] dst, int dw, int dh,
                     int dx, int dy, int c0x, int c0y, int c1x, int c1y, boolean blend) {
        boolean swap = (transform & 4) != 0;
        int tw = swap ? h : w, th = swap ? w : h;
        for (int j = 0; j < th; j++) {
            int y = dy + j;
            if (y < c0y || y >= c1y) continue;
            for (int i = 0; i < tw; i++) {
                int x = dx + i;
                if (x < c0x || x >= c1x) continue;
                int u, v;
                switch (transform) {
                    case 0: u = i; v = j; break;
                    case 2: u = w - 1 - i; v = j; break;
                    case 1: u = i; v = h - 1 - j; break;
                    case 3: u = w - 1 - i; v = h - 1 - j; break;
                    case 5: u = j; v = h - 1 - i; break;
                    case 6: u = w - 1 - j; v = i; break;
                    case 7: u = w - 1 - j; v = h - 1 - i; break;
                    default: u = j; v = i; break; // 4: MIRROR_ROT270
                }
                int c = src.data[(ys + v) * src.w + xs + u];
                int o = y * dw + x;
                if (!blend) { dst[o] = c; continue; }
                dst[o] = over(c, dst[o]);
            }
        }
    }

    private static int over(int c, int d) {
        int a = c >>> 24;
        if (a == 255) return c;
        if (a == 0) return d;
        int ia = 255 - a;
        int r = (((c >> 16) & 0xFF) * a + ((d >> 16) & 0xFF) * ia) / 255;
        int g = (((c >> 8) & 0xFF) * a + ((d >> 8) & 0xFF) * ia) / 255;
        int b = ((c & 0xFF) * a + (d & 0xFF) * ia) / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public void drawRGB(int[] rgb, int offset, int scanlength, int x, int y, int width, int height, boolean processAlpha) {
        x += tx;
        y += ty;
        for (int j = 0; j < height; j++) {
            int yy = y + j;
            if (yy < cy0 || yy >= cy1) continue;
            for (int i = 0; i < width; i++) {
                int xx = x + i;
                if (xx < cx0 || xx >= cx1) continue;
                int c = rgb[offset + j * scanlength + i];
                px[yy * sw + xx] = processAlpha ? over(c, px[yy * sw + xx]) : (c | 0xFF000000);
            }
        }
    }

    public void copyArea(int xs, int ys, int w, int h, int xd, int yd, int anchor) {
        throw new IllegalStateException("copyArea is not supported by the emulator");
    }
}
