package bullseye;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Bullseye: target archery. The sight sways with your breathing; hold 0 to
 * steady it briefly, allow for the wind, and loose with 5. Ten arrows over
 * three distances.
 */
public class ArcheryGame extends Game {
    private static final int ARROWS = 12;
    private static final int[] DIST = { 30, 50, 70 };
    private static final int[] RINGS = { 0xFFFFFF, 0xFFFFFF, 0x212121, 0x212121, 0x1E88E5, 0x1E88E5, 0xE53935, 0xE53935, 0xFFC107, 0xFFC107 };
    private int aimX, aimY, sway, breath, wind, arrow, flightT, hitX, hitY, lastPts;
    private final int[] holeX = new int[ARROWS], holeY = new int[ARROWS];
    private int nHoles;
    private String note = "";

    protected String name() { return "Bullseye"; }

    protected String[] help() {
        return new String[] {
            "Twelve arrows at targets 30, 50 and 70 metres away. Move the sight onto the gold and press 5 to shoot.",
            "Your aim sways, more at long range. Hold 0 to hold your breath and steady the sight - but only for a few seconds. The wind flag shows how far the arrow will drift: aim into the wind.",
            "Rings score 10 (gold centre) down to 1.",
            "- Controls",
            "2/4/6/8: aim  5: shoot",
            "0 (hold): hold breath",
        };
    }

    protected int accent() { return 0xFFC107; }

    protected void newGame() {
        arrow = 0;
        nHoles = 0;
        nextArrow();
    }

    private int range() { return DIST[Math.min(2, arrow / 4)]; }

    private void nextArrow() {
        if (arrow % 4 == 0) nHoles = 0;
        aimX = Rnd.range(-30, 30) << 8;
        aimY = Rnd.range(-30, 30) << 8;
        wind = Rnd.range(-3, 3) * (1 + arrow / 4);
        breath = 60;
        flightT = 0;
    }

    private int radius() { return Math.min(W, H) * 38 / 100; }

    protected void update() {
        if (flightT > 0) {
            if (--flightT == 0) {
                arrow++;
                if (arrow >= ARROWS) {
                    headline = score + " POINTS";
                    endGame(score >= 70);
                    return;
                }
                nextArrow();
            }
            return;
        }
        int sp = 3 << 8;
        if ((held & K_LEFT) != 0) aimX -= sp;
        if ((held & K_RIGHT) != 0) aimX += sp;
        if ((held & K_UP) != 0) aimY -= sp;
        if ((held & K_DOWN) != 0) aimY += sp;
        boolean holding = (held & K_NUM0) != 0 && breath > 0;
        if (holding) breath--;
        else if (breath < 60 && (frame & 3) == 0) breath++;
        sway = holding ? 1 : (range() / 10 + (breath < 10 ? 6 : 0));
        aimX += FMath.sin(frame * 5) * sway / 6;
        aimY += FMath.cos(frame * 7) * sway / 7;
        int lim = (radius() + 20) << 8;
        aimX = FMath.clamp(aimX, -lim, lim);
        aimY = FMath.clamp(aimY, -lim, lim);
        if ((pressed & K_FIRE) != 0) shoot();
    }

    private void shoot() {
        int r = radius();
        int drift = wind * r / 40;
        int jitter = range() / 25;
        hitX = (aimX >> 8) + drift + Rnd.range(-jitter, jitter);
        hitY = (aimY >> 8) + Rnd.range(-jitter, jitter) + range() / 30;
        int d = FMath.dist(hitX, hitY);
        int ring = 10 - d * 10 / Math.max(1, r);
        lastPts = Math.max(0, ring);
        score += lastPts;
        holeX[nHoles] = hitX;
        holeY[nHoles] = hitY;
        nHoles++;
        note = lastPts == 10 ? "BULLSEYE!" : (lastPts == 0 ? "Miss" : lastPts + " points");
        flightT = 30;
        Sfx.tone(lastPts >= 9 ? 88 : (lastPts > 0 ? 70 : 40), 50);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x81C784);
        g.fillRect(0, 0, W, H);
        g.setColor(0x9CCC65);
        g.fillRect(0, 0, W, H / 3);
        int cx = W / 2, cy = hud + (H - hud) / 2, r = radius();
        g.setColor(0x6D4C41);
        g.fillRect(cx - 2, cy, 4, H - cy);
        for (int k = 0; k < 10; k++) {
            g.setColor(RINGS[k]);
            Gfx.disc(g, cx, cy, r * (10 - k) / 10);
        }
        g.setColor(0x000000);
        Gfx.ring(g, cx, cy, r);
        for (int i = 0; i < nHoles; i++) {
            g.setColor(0x3E2723);
            g.fillRect(cx + holeX[i] - 1, cy + holeY[i] - 1, 3, 3);
            g.setColor(0xFFFFFF);
            g.fillRect(cx + holeX[i], cy + holeY[i], 1, 1);
        }
        if (flightT == 0) {
            int ax = cx + (aimX >> 8), ay = cy + (aimY >> 8);
            g.setColor(0x000000);
            Gfx.ring(g, ax, ay, 6);
            g.drawLine(ax - 9, ay, ax - 3, ay);
            g.drawLine(ax + 3, ay, ax + 9, ay);
            g.drawLine(ax, ay - 9, ax, ay - 3);
            g.drawLine(ax, ay + 3, ax, ay + 9);
        } else {
            Gfx.shadowText(g, note, W / 2, hud + 2, Gfx.TC, Gfx.SMALL_B, lastPts == 10 ? 0xFFEB3B : 0xFFFFFF, 0x000000);
        }
        // wind flag
        int fx = W - 14, fy = hud + 4;
        g.setColor(0x5D4037);
        g.fillRect(fx, fy, 1, 18);
        g.setColor(0xE53935);
        int len = wind * 3;
        if (len != 0) g.fillTriangle(fx, fy, fx, fy + 6, fx + len, fy + 3 + ((clock & 4) == 0 ? 1 : 0));
        g.setFont(Gfx.SMALL);
        g.setColor(0x1B5E20);
        g.drawString("wind " + (wind > 0 ? "+" : "") + wind, W - 2, fy + 20, Gfx.TR);
        g.setColor(0x000000);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFC107);
        g.drawString(String.valueOf(score), 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString(range() + "m  " + Math.min(arrow + 1, ARROWS) + "/" + ARROWS, W - 2, 1, Gfx.TR);
        Gfx.bar(g, 4, H - 8, W / 3, 4, breath, 60, 0x4FC3F7, 0x263238);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int r = Math.max(6, h / 2 - 2);
        for (int k = 0; k < 5; k++) {
            g.setColor(RINGS[k * 2]);
            Gfx.disc(g, x + w / 2, y + h / 2, r * (5 - k) / 5);
        }
        int t = clock % 30;
        g.setColor(0x5D4037);
        int ax = x + t * w / 60;
        g.drawLine(ax, y + h / 2, ax + 10, y + h / 2);
    }
}
