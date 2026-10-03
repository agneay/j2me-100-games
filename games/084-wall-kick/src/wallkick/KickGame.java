package wallkick;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Wall Kick: climb a shaft by kicking off one wall to the other. You slide
 * slowly down whichever wall you cling to; spikes line some stretches, and
 * the screen keeps rising.
 */
public class KickGame extends Game {
    private static final int SEGS = 48;
    private final boolean[] spikeL = new boolean[SEGS], spikeR = new boolean[SEGS];
    private final int[] gemSide = new int[SEGS]; // 0 none, 1 left, 2 right
    private int segH, wallW, side, py, jumpT, jumpFrom, camY, rise, deadT, height, gems;

    protected String name() { return "Wall Kick"; }

    protected String[] help() {
        return new String[] {
            "Climb an endless shaft by wall-jumping. You cling to one wall and slowly slide down; press 5 to kick off and leap up to the other wall.",
            "Avoid the spikes on the walls - time your jumps so you land above or below them. Grab gems for bonus points. The screen keeps rising, so don't dawdle!",
            "- Controls",
            "5 or 2: wall jump",
        };
    }

    protected int accent() { return 0x7C4DFF; }

    protected void newGame() {
        segH = Math.max(10, H / 12);
        wallW = Math.max(6, W / 10);
        for (int i = 0; i < SEGS; i++) gen(i);
        for (int i = 0; i < 8; i++) { spikeL[i] = spikeR[i] = false; spikeL[SEGS - 1 - i] = spikeR[SEGS - 1 - i] = false; }
        side = 0;
        py = -(segH * 3) << 8; // start in segment 3, inside the spike-free stretch
        camY = py - ((H * 2 / 3) << 8);
        jumpT = 0;
        rise = 64;
        deadT = 0;
        height = 0;
        gems = 0;
    }

    private void gen(int i) {
        int diff = Math.min(35, 10 + height / 300);
        spikeL[i] = Rnd.nextInt(100) < diff;
        spikeR[i] = !spikeL[i] && Rnd.nextInt(100) < diff;
        gemSide[i] = Rnd.chance(15) ? (spikeL[i] ? 2 : 1) : 0;
    }

    /** Segment index covering world y (world y decreases upward). */
    private int segAt(int worldY) {
        int s = (-worldY) / segH;
        return ((s % SEGS) + SEGS) % SEGS;
    }

    protected void update() {
        if (deadT > 0) {
            if (--deadT == 0) endGame(false);
            return;
        }
        if (jumpT > 0) {
            jumpT--;
            py -= (segH << 8) * 2 / 9;
            if (jumpT == 0) {
                side = 1 - jumpFrom;
                Sfx.tone(60, 15);
            }
        } else {
            py += 40; // slide down the wall
            if ((tapped & (K_FIRE | K_UP)) != 0) {
                jumpT = 9;
                jumpFrom = side;
                Sfx.tone(76, 20);
            }
        }
        // camera rises steadily, faster over time
        rise = 64 + height / 40;
        camY -= rise;
        int screenY = (py >> 8) - (camY >> 8);
        if (screenY < H / 3) camY = py - ((H / 3) << 8);
        int h = -(py >> 8) / 2;
        if (h > height) {
            height = h;
            score = height / 4 + gems * 25;
        }
        // regenerate segments that scrolled far below
        int bottomSeg = segAt((camY >> 8) + H + segH * 4);
        gen(bottomSeg);
        if (jumpT == 0) {
            int s = segAt(py >> 8);
            boolean spike = side == 0 ? spikeL[s] : spikeR[s];
            if (spike) die("SPIKED!");
            if (gemSide[s] == side + 1) {
                gemSide[s] = 0;
                gems++;
                score += 25;
                Sfx.good();
            }
        }
        if ((py >> 8) - (camY >> 8) > H + 10) die("FELL BEHIND");
    }

    private void die(String why) {
        if (deadT > 0) return;
        headline = why;
        deadT = 25;
        Sfx.bad();
    }

    protected void draw(Graphics g) {
        g.setColor(0x120A2A);
        g.fillRect(0, 0, W, H);
        int cam = camY >> 8;
        int first = (cam / segH) - 1;
        for (int k = first; k < first + H / segH + 3; k++) {
            int wy = k * segH;
            int y = wy - cam;
            int s = segAt(wy);
            g.setColor((k & 1) == 0 ? 0x4527A0 : 0x512DA8);
            g.fillRect(0, y, wallW, segH);
            g.fillRect(W - wallW, y, wallW, segH);
            g.setColor(0xE0E0E0);
            if (spikeL[s]) for (int t = 0; t < segH; t += 5) g.fillTriangle(wallW, y + t, wallW, y + t + 5, wallW + 5, y + t + 2);
            if (spikeR[s]) for (int t = 0; t < segH; t += 5) g.fillTriangle(W - wallW, y + t, W - wallW, y + t + 5, W - wallW - 5, y + t + 2);
            if (gemSide[s] != 0) {
                int gx = gemSide[s] == 1 ? wallW + 6 : W - wallW - 6;
                g.setColor(0x00E5FF);
                g.fillTriangle(gx, y + 2, gx - 3, y + segH / 2, gx + 3, y + segH / 2);
            }
        }
        int sz = Math.max(5, W / 20);
        int x;
        if (jumpT > 0) {
            int t = 9 - jumpT;
            int x0 = jumpFrom == 0 ? wallW + sz / 2 : W - wallW - sz / 2, x1 = jumpFrom == 0 ? W - wallW - sz / 2 : wallW + sz / 2;
            x = x0 + (x1 - x0) * t / 9;
        } else x = side == 0 ? wallW + sz / 2 : W - wallW - sz / 2;
        int y = (py >> 8) - cam;
        if (deadT == 0 || (clock & 2) == 0) {
            g.setColor(0xFFAB40);
            g.fillRect(x - sz / 2, y - sz, sz, sz);
            g.setColor(0xFFFFFF);
            g.fillRect(x + (side == 0 && jumpT == 0 ? 1 : -2), y - sz + 2, 2, 2);
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(height / 2 + " m", 2, 1, Gfx.TL);
        g.setColor(0x00E5FF);
        g.drawString("gems " + gems, W - 2, 1, Gfx.TR);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x4527A0);
        g.fillRect(x, y, 8, h);
        g.fillRect(x + w - 8, y, 8, h);
        int t = clock % 30;
        int px = t < 15 ? x + 8 + (w - 22) * t / 15 : x + w - 14 - (w - 22) * (t - 15) / 15;
        g.setColor(0xFFAB40);
        g.fillRect(px, y + h - 8 - (t % 15) * 2, 6, 6);
    }
}
