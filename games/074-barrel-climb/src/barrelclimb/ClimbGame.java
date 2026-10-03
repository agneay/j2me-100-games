package barrelclimb;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Barrel Climb: climb five girders to the top while barrels roll down at
 * you. Jump them for points, climb ladders to get higher.
 */
public class ClimbGame extends Game {
    private static final int G = 5, MAXB = 10;
    private final int[] ladA = new int[G - 1], ladB = new int[G - 1]; // ladder x (percent of width) between girder k and k+1
    private final int[] bx = new int[MAXB], bg = new int[MAXB], bdir = new int[MAXB], bfall = new int[MAXB];
    private final boolean[] bon = new boolean[MAXB], jumped = new boolean[MAXB];
    private int px, pg, climbY, jumpT, facing = 1, lives, level, throwT, deadT, winT;
    private boolean climbing;
    private int gap, bottom, size;

    protected String name() { return "Barrel Climb"; }

    protected String[] help() {
        return new String[] {
            "Climb the ladders up five girders to reach the top while barrels roll down towards you.",
            "Jump over barrels for 100 points each. Barrels drop off the open end of each girder, and sometimes tumble down a ladder!",
            "Reach the top to clear the stage. Barrels get faster each stage.",
            "- Controls",
            "4/6: walk  2/8: climb",
            "5: jump",
        };
    }

    protected int accent() { return 0xFF7043; }

    protected void newGame() {
        lives = 3;
        level = 0;
        startLevel();
    }

    private void layout() {
        int hud = Gfx.SMALL.getHeight() + 4;
        bottom = H - 6;
        gap = (bottom - hud - 12) / G;
        size = Math.max(6, Math.min(gap / 3, W / 16));
    }

    private int girderY(int k) { return bottom - k * gap; }

    private void startLevel() {
        layout();
        for (int k = 0; k < G - 1; k++) {
            // ladders sit away from the open ends
            ladA[k] = k % 2 == 0 ? Rnd.range(60, 82) : Rnd.range(18, 40);
            ladB[k] = Rnd.range(35, 65);
        }
        for (int i = 0; i < MAXB; i++) bon[i] = false;
        px = (W / 10) << 8;
        pg = 0;
        climbing = false;
        jumpT = 0;
        throwT = 30;
        winT = 0;
    }

    private int onLadder(int k) {
        int x = px >> 8;
        if (k < 0 || k >= G - 1) return -1;
        if (Math.abs(x - ladA[k] * W / 100) < size / 2 + 1) return ladA[k] * W / 100;
        if (Math.abs(x - ladB[k] * W / 100) < size / 2 + 1) return ladB[k] * W / 100;
        return -1;
    }

    protected void update() {
        layout();
        if (deadT > 0) {
            if (--deadT == 0) {
                if (lives <= 0) { endGame(false); return; }
                startLevel();
            }
            return;
        }
        if (winT > 0) {
            if (--winT == 0) { level++; startLevel(); }
            return;
        }
        int speed = (W << 8) / 70;
        if (climbing) {
            if ((held & K_UP) != 0) climbY += 2;
            if ((held & K_DOWN) != 0) climbY -= 2;
            if (climbY >= gap) { pg++; climbing = false; climbY = 0; }
            if (climbY <= 0 && (held & K_DOWN) != 0) { climbing = false; climbY = 0; }
        } else {
            if (jumpT > 0) jumpT--;
            if ((held & K_LEFT) != 0) { px -= speed; facing = -1; }
            if ((held & K_RIGHT) != 0) { px += speed; facing = 1; }
            px = Math.max(size << 8, Math.min((W - size) << 8, px));
            if ((pressed & K_FIRE) != 0 && jumpT == 0) { jumpT = 12; Sfx.tone(72, 20); }
            if ((held & K_UP) != 0 && jumpT == 0) {
                int lx = onLadder(pg);
                if (lx >= 0) { climbing = true; climbY = 0; px = lx << 8; }
            }
            if ((held & K_DOWN) != 0 && jumpT == 0 && pg > 0) {
                int lx = onLadder(pg - 1);
                if (lx >= 0) { pg--; climbing = true; climbY = gap - 2; px = lx << 8; }
            }
        }
        if (pg == G - 1 && !climbing) {
            score += 1000 + level * 200;
            winT = 40;
            Sfx.win();
            return;
        }
        // barrels
        if (--throwT <= 0) {
            throwT = Math.max(25, 70 - level * 8) + Rnd.nextInt(40);
            for (int i = 0; i < MAXB; i++) {
                if (bon[i]) continue;
                bon[i] = true;
                bg[i] = G - 1;
                bx[i] = (W / 8) << 8;
                bdir[i] = 1;
                bfall[i] = 0;
                jumped[i] = false;
                break;
            }
        }
        int bspeed = (W << 8) / (90 - Math.min(40, level * 8));
        for (int i = 0; i < MAXB; i++) {
            if (!bon[i]) continue;
            if (bfall[i] > 0) {
                bfall[i] -= 3;
                if (bfall[i] <= 0) bfall[i] = 0;
                continue;
            }
            bx[i] += bdir[i] * bspeed;
            int x = bx[i] >> 8;
            // tumble down a ladder sometimes
            if (bg[i] > 0) {
                int k = bg[i] - 1;
                for (int l = 0; l < 2; l++) {
                    int lx = (l == 0 ? ladA[k] : ladB[k]) * W / 100;
                    if (Math.abs(x - lx) <= (bspeed >> 8) && Rnd.chance(30)) {
                        bg[i]--;
                        bfall[i] = gap;
                        bdir[i] = -bdir[i];
                        jumped[i] = false;
                    }
                }
            }
            if (x > W - size || x < size) {
                if (bg[i] == 0) { bon[i] = false; continue; }
                bg[i]--;
                bfall[i] = gap;
                bdir[i] = -bdir[i];
                jumped[i] = false;
            }
            // collision with the player
            if (bfall[i] == 0 && bg[i] == pg && !climbing) {
                int d = Math.abs(x - (px >> 8));
                int jh = jumpT > 0 ? (jumpT * (12 - jumpT)) * size / 30 : 0;
                if (d < size * 2 / 3 && jh < size * 2 / 3) { die(); return; }
                if (d < size && jh >= size * 2 / 3 && !jumped[i]) {
                    jumped[i] = true;
                    score += 100;
                    Sfx.good();
                }
            }
            if (climbing && bfall[i] > 0 && (bg[i] == pg || bg[i] + 1 == pg) && Math.abs(x - (px >> 8)) < size) { die(); return; }
        }
    }

    private void die() {
        lives--;
        deadT = 30;
        headline = "BONK!";
        Sfx.bad();
    }

    protected void draw(Graphics g) {
        layout();
        g.setColor(0x0D0D1F);
        g.fillRect(0, 0, W, H);
        for (int k = 0; k < G - 1; k++) {
            for (int l = 0; l < 2; l++) {
                int lx = (l == 0 ? ladA[k] : ladB[k]) * W / 100;
                g.setColor(0x26C6DA);
                int y0 = girderY(k + 1), y1 = girderY(k);
                g.fillRect(lx - size / 2, y0, 1, y1 - y0);
                g.fillRect(lx + size / 2, y0, 1, y1 - y0);
                for (int y = y0 + 3; y < y1; y += 4) g.fillRect(lx - size / 2, y, size, 1);
            }
        }
        for (int k = 0; k < G; k++) {
            int y = girderY(k);
            int x0 = k == G - 1 ? 0 : (k % 2 == 1 ? size * 2 : 0);
            int x1 = k == 0 ? W : (k % 2 == 1 ? W : W - size * 2);
            g.setColor(0xE53935);
            g.fillRect(x0, y, x1 - x0, 3);
            g.setColor(0xFF8A80);
            for (int x = x0; x < x1; x += 6) g.drawLine(x, y, x + 3, y + 3);
        }
        // goal
        g.setColor(0xF48FB1);
        g.fillRect(W / 2 - 3, girderY(G - 1) - size - 4, 6, size);
        g.setColor(0xFFCC80);
        Gfx.disc(g, W / 2, girderY(G - 1) - size - 6, 3);
        for (int i = 0; i < MAXB; i++) {
            if (!bon[i]) continue;
            int y = girderY(bg[i]) - size / 2 - 1 - bfall[i];
            g.setColor(0x8D6E63);
            Gfx.disc(g, bx[i] >> 8, y, size / 2);
            g.setColor(0x4E342E);
            int a = (bx[i] >> 9) & 3;
            g.drawLine((bx[i] >> 8) - size / 2 + 1 + a, y, (bx[i] >> 8) + size / 2 - 1 - a, y);
        }
        int jh = jumpT > 0 ? (jumpT * (12 - jumpT)) * size / 30 : 0;
        int y = girderY(pg) - (climbing ? climbY : jh);
        int x = px >> 8;
        if (deadT == 0 || (clock & 2) == 0) {
            g.setColor(0xD32F2F);
            g.fillRect(x - size / 3, y - size, size * 2 / 3, size * 2 / 3);
            g.setColor(0x1565C0);
            g.fillRect(x - size / 3, y - size / 3, size * 2 / 3, size / 3);
            g.setColor(0xFFCC80);
            Gfx.disc(g, x, y - size - 2, Math.max(2, size / 4));
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(String.valueOf(score), 2, 1, Gfx.TL);
        g.setColor(0xFF8A80);
        g.drawString("x" + lives + " S" + (level + 1), W - 2, 1, Gfx.TR);
        if (winT > 0) Gfx.shadowText(g, "STAGE CLEAR!", W / 2, H / 2, Gfx.TC, Gfx.SMALL_B, 0xFFEB3B, 0x000000);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0xE53935);
        g.fillRect(x, y + h / 2, w, 3);
        g.fillRect(x, y + h - 3, w, 3);
        int bxx = x + (clock * 3) % w;
        g.setColor(0x8D6E63);
        Gfx.disc(g, bxx, y + h / 2 - 4, 4);
        int hop = (clock / 4) % 8 < 3 ? 6 : 0;
        g.setColor(0xD32F2F);
        g.fillRect(x + w / 2 - 3, y + h - 12 - hop, 6, 9);
    }
}
