package gravityflip;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Gravity Flip: a one-button runner in a corridor. You can't jump - you
 * flip gravity and fall to the ceiling (or back to the floor).
 * Each column of the corridor stores floor/ceiling state:
 * 0 solid, 1 gap, 2 spikes.
 */
public class FlipGame extends Game {
    private static final int COLS = 64;
    private final byte[] floor = new byte[COLS], ceil = new byte[COLS];
    private int head, scrollPx, unit, speed, top, bottom, genIndex;
    private int py, vy, grav = 1, lastSafe; // grav +1 = down
    private int dist, flips, deadT;
    private boolean onSurface;

    protected String name() { return "Gravity Flip"; }

    protected String[] help() {
        return new String[] {
            "You run automatically down an endless corridor. You can't jump - instead press 5 to flip gravity and fall up to the ceiling (or back down).",
            "Avoid holes and spikes in the floor and ceiling. You can only flip while standing on a surface. The pace keeps rising.",
            "- Controls",
            "5, 2 or 8: flip gravity",
        };
    }

    protected int accent() { return 0xFF4081; }

    protected void newGame() {
        layout();
        for (int i = 0; i < COLS; i++) {
            floor[i] = 0;
            ceil[i] = 0;
        }
        head = 0;
        scrollPx = 0;
        speed = 256 * unit / 9;
        lastSafe = 0;
        dist = 0;
        for (int i = 12; i < COLS; i++) gen(i, i);
        genIndex = COLS;
        py = (bottom - unit) << 8;
        vy = 0;
        grav = 1;
        dist = 0;
        flips = 0;
        deadT = 0;
    }

    private void layout() {
        unit = Math.max(6, Math.min(W / 12, H / 10));
        int hud = Gfx.SMALL.getHeight() + 2;
        int corridor = unit * 6;
        top = hud + (H - hud - corridor) / 2;
        bottom = top + corridor;
    }

    /** Generate ring slot col (absolute column i) so at least one surface is always safe. */
    private void gen(int i, int col) {
        int difficulty = Math.min(45, 10 + dist / 400);
        byte f = 0, c = 0;
        if (Rnd.nextInt(100) < difficulty) {
            boolean hitFloor = Rnd.chance(50);
            byte kind = (byte) (Rnd.chance(50) ? 1 : 2);
            if (hitFloor) f = kind; else c = kind;
        }
        // keep hazard runs short and never block both sides
        int prev = (col + COLS - 1) % COLS;
        if (floor[prev] != 0 && ceil[prev] != 0) { f = 0; c = 0; }
        if (f != 0 && ceil[prev] != 0) f = 0;
        if (c != 0 && floor[prev] != 0) c = 0;
        if (i - lastSafe < 3) { f = 0; c = 0; }
        floor[col] = f;
        ceil[col] = c;
        if (f != 0 || c != 0) lastSafe = i + 2;
    }

    private int colAtPlayer() {
        int px = W / 4;
        return (head + (px + scrollPx) / unit) % COLS;
    }

    protected void update() {
        layout();
        if (deadT > 0) {
            if (--deadT == 0) endGame(false);
            return;
        }
        speed += 1;
        scrollPx += speed >> 8;
        dist += speed >> 8;
        while (scrollPx >= unit) {
            scrollPx -= unit;
            int old = head;
            head = (head + 1) % COLS;
            gen(genIndex++, old); // the slot that scrolled off becomes the new right edge
        }
        score = dist / unit;
        if ((tapped & (K_FIRE | K_UP | K_DOWN)) != 0 && onSurface) {
            grav = -grav;
            onSurface = false;
            flips++;
            Sfx.tone(grav > 0 ? 60 : 72, 25);
        }
        vy += grav * (unit << 8) / 12;
        int max = (unit << 8) * 2 / 3;
        if (vy > max) vy = max;
        if (vy < -max) vy = -max;
        py += vy;
        int c = colAtPlayer();
        onSurface = false;
        int feet = py >> 8;
        if (grav > 0 && feet + unit >= bottom) {
            if (floor[c] == 1) {
                if (feet > H) die("FELL OUT");
            } else {
                py = (bottom - unit) << 8;
                vy = 0;
                onSurface = true;
                if (floor[c] == 2) die("SPIKED!");
            }
        } else if (grav < 0 && feet <= top) {
            if (ceil[c] == 1) {
                if (feet < -unit) die("FLOATED AWAY");
            } else {
                py = top << 8;
                vy = 0;
                onSurface = true;
                if (ceil[c] == 2) die("SPIKED!");
            }
        }
    }

    private void die(String why) {
        if (deadT > 0) return;
        headline = why;
        deadT = 20;
        Sfx.bad();
    }

    protected void draw(Graphics g) {
        layout();
        g.setColor(0x120A1E);
        g.fillRect(0, 0, W, H);
        g.setColor(0x1D1030);
        g.fillRect(0, top, W, bottom - top);
        int n = W / unit + 2;
        for (int k = 0; k < n; k++) {
            int col = (head + k) % COLS;
            int x = k * unit - scrollPx;
            drawSurface(g, x, bottom, floor[col], true);
            drawSurface(g, x, top - unit, ceil[col], false);
        }
        // runner
        int px = W / 4, y = py >> 8;
        if (deadT == 0 || (clock & 2) == 0) {
            g.setColor(0xFF4081);
            g.fillRect(px - unit / 3, y + unit / 4, unit * 2 / 3, unit / 2);
            g.setColor(0xFFFFFF);
            int headY = grav > 0 ? y + unit / 8 : y + unit * 5 / 8;
            Gfx.disc(g, px, headY, Math.max(2, unit / 5));
            g.setColor(0xFF80AB);
            int legY = grav > 0 ? y + unit * 3 / 4 : y;
            int step = ((dist / 4) & 1) == 0 ? 2 : -2;
            g.fillRect(px - 2 + step, legY, 2, unit / 4);
            g.fillRect(px + 1 - step, legY, 2, unit / 4);
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(score + " m", 2, 1, Gfx.TL);
        g.setColor(0xFF80AB);
        g.drawString("flips " + flips, W - 2, 1, Gfx.TR);
    }

    private void drawSurface(Graphics g, int x, int y, int kind, boolean isFloor) {
        if (kind == 1) return;
        g.setColor(0x7C4DFF);
        g.fillRect(x, y, unit, unit);
        g.setColor(0xB388FF);
        g.fillRect(x, isFloor ? y : y + unit - 2, unit, 2);
        if (kind == 2) {
            g.setColor(0xFFEB3B);
            int base = isFloor ? y : y + unit;
            int tip = isFloor ? y - unit / 2 : y + unit + unit / 2;
            g.fillTriangle(x, base, x + unit / 2, tip, x + unit, base);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x7C4DFF);
        g.fillRect(x, y, w, 4);
        g.fillRect(x, y + h - 4, w, 4);
        int t = clock % 40;
        int yy = t < 20 ? y + 4 + (h - 16) * t / 20 : y + 4 + (h - 16) * (40 - t) / 20;
        g.setColor(0xFF4081);
        g.fillRect(x + w / 3, yy, 8, 8);
    }
}
