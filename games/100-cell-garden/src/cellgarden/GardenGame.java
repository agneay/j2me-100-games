package cellgarden;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Cell Garden: a cellular-automaton puzzle toy. Plant a limited number of
 * seeds, then let the garden grow by Life-like rules for a fixed number of
 * generations. Living cells that sit on fertile (gold) tiles at the end
 * score points; a sandbox mode lets you play freely.
 */
public class GardenGame extends Game {
    private static final int GW = 16, GH = 16;
    private final boolean[][] cell = new boolean[GH][GW], nxt = new boolean[GH][GW];
    private final boolean[][] fertile = new boolean[GH][GW];
    private int cx, cy, seeds, gen, gens, round, growing, growT, best, peak;
    private boolean running;

    protected String name() { return "Cell Garden"; }

    protected String[] help() {
        return new String[] {
            "A garden that grows itself. Each generation, an empty plot with exactly 3 living neighbours sprouts, and a plant with 2 or 3 neighbours survives; all others wither.",
            "Challenge: plant your seeds, then press 0 to grow. After the set number of generations, every plant standing on a gold plot scores. Five gardens per game.",
            "Sandbox: unlimited seeds, grow and pause as you like.",
            "- Controls",
            "2/4/6/8: move",
            "5: plant / remove seed",
            "0: grow (Sandbox: start / stop)",
            "1: single step (Sandbox)",
            "#: clear (Sandbox)",
        };
    }

    protected String[] modes() { return new String[] { "Challenge", "Sandbox" }; }

    protected int accent() { return 0x8BC34A; }

    protected void newGame() {
        round = 0;
        cx = GW / 2;
        cy = GH / 2;
        if (mode == 1) {
            clear();
            for (int r = 0; r < GH; r++) for (int c = 0; c < GW; c++) fertile[r][c] = false;
            seeds = 999;
            running = false;
        } else nextRound();
    }

    private void clear() {
        for (int r = 0; r < GH; r++) for (int c = 0; c < GW; c++) cell[r][c] = false;
        gen = 0;
    }

    private void nextRound() {
        round++;
        clear();
        for (int r = 0; r < GH; r++) for (int c = 0; c < GW; c++) fertile[r][c] = false;
        // a few fertile patches
        int patches = 2 + round / 2;
        for (int k = 0; k < patches; k++) {
            int pr = Rnd.range(2, GH - 4), pc = Rnd.range(2, GW - 4);
            int w = Rnd.range(2, 4), h = Rnd.range(2, 4);
            for (int r = pr; r < pr + h; r++) for (int c = pc; c < pc + w; c++) fertile[r][c] = true;
        }
        seeds = Math.max(5, 12 - round);
        gens = 10 + round * 4;
        growing = 0;
        running = false;
        peak = 0;
    }

    private int liveCount() {
        int n = 0;
        for (int r = 0; r < GH; r++) for (int c = 0; c < GW; c++) if (cell[r][c]) n++;
        return n;
    }

    private void step() {
        for (int r = 0; r < GH; r++) for (int c = 0; c < GW; c++) {
            int n = 0;
            for (int dr = -1; dr <= 1; dr++) for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr, cc = c + dc;
                if (rr >= 0 && rr < GH && cc >= 0 && cc < GW && cell[rr][cc]) n++;
            }
            nxt[r][c] = cell[r][c] ? (n == 2 || n == 3) : n == 3;
        }
        for (int r = 0; r < GH; r++) for (int c = 0; c < GW; c++) cell[r][c] = nxt[r][c];
        gen++;
        peak = Math.max(peak, liveCount());
    }

    private int harvest() {
        int n = 0;
        for (int r = 0; r < GH; r++) for (int c = 0; c < GW; c++) if (cell[r][c] && fertile[r][c]) n++;
        return n;
    }

    protected void update() {
        if (mode == 0 && growing > 0) {
            if (++growT >= 3) {
                growT = 0;
                step();
                Sfx.tone(60 + liveCount() % 24, 15);
                if (gen >= gens || liveCount() == 0) {
                    int h = harvest();
                    score += h * 10 + peak;
                    best = h;
                    growing = 0;
                    running = true; // waiting for 5 to continue
                    if (h > 0) Sfx.good();
                    else Sfx.bad();
                }
            }
            return;
        }
        if (mode == 0 && running) {
            if ((pressed & K_FIRE) != 0) {
                if (round >= 5) {
                    headline = "GARDENER'S SCORE " + score;
                    endGame(score > 0);
                } else nextRound();
            }
            return;
        }
        if ((pressed & K_UP) != 0) cy = (cy + GH - 1) % GH;
        if ((pressed & K_DOWN) != 0) cy = (cy + 1) % GH;
        if ((pressed & K_LEFT) != 0) cx = (cx + GW - 1) % GW;
        if ((pressed & K_RIGHT) != 0) cx = (cx + 1) % GW;
        if ((pressed & K_FIRE) != 0) {
            if (cell[cy][cx]) {
                cell[cy][cx] = false;
                seeds++;
                Sfx.click();
            } else if (seeds > 0) {
                cell[cy][cx] = true;
                seeds--;
                Sfx.click();
            } else Sfx.bad();
        }
        if (mode == 0) {
            if (digit(0) && liveCount() > 0) {
                growing = 1;
                growT = 0;
                peak = liveCount();
            }
        } else {
            if (digit(0)) running = !running;
            if (digit(1)) step();
            if ((pressed & K_POUND) != 0) clear();
            if (running && frame % 3 == 0) step();
            score = Math.max(score, liveCount());
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int fh = Gfx.SMALL.getHeight();
        int s = Math.max(4, Math.min((W - 4) / GW, (H - hud * 2 - fh - 4) / GH));
        int ox = (W - s * GW) / 2, oy = hud + 2;
        g.setColor(0x1B1410);
        g.fillRect(0, 0, W, H);
        for (int r = 0; r < GH; r++) for (int c = 0; c < GW; c++) {
            int x = ox + c * s, y = oy + r * s;
            g.setColor(fertile[r][c] ? 0x8D6E1F : ((r + c) % 2 == 0 ? 0x4E342E : 0x452E28));
            g.fillRect(x, y, s, s);
            if (cell[r][c]) {
                boolean scoring = fertile[r][c];
                g.setColor(scoring ? 0xC6FF00 : 0x66BB6A);
                g.fillRect(x + 1, y + 1, s - 2, s - 2);
                if (s >= 6) {
                    g.setColor(scoring ? 0xF4FF81 : 0xA5D6A7);
                    g.fillRect(x + s / 2 - 1, y + 1, 2, s / 2);
                }
            }
        }
        if (growing == 0 && !(mode == 0 && running)) {
            g.setColor(0xFFFFFF);
            g.drawRect(ox + cx * s - 1, oy + cy * s - 1, s + 1, s + 1);
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xC5E1A5);
        if (mode == 0) {
            g.drawString((W < 160 ? "Plot " : "Garden ") + round + "/5", 2, 1, Gfx.TL);
            g.drawString("Seeds " + seeds, W - 2, 1, Gfx.TR);
            String status;
            if (growing > 0) status = "Gen " + gen + "/" + gens + "  alive " + liveCount();
            else if (running) status = "Harvest " + best + "  5: next";
            else status = gens + " gens  0: grow";
            Gfx.text(g, status, W / 2, H - fh - 2, Gfx.TC, Gfx.SMALL, 0xDCE775);
        } else {
            g.drawString("Gen " + gen, 2, 1, Gfx.TL);
            g.drawString("Alive " + liveCount(), W - 2, 1, Gfx.TR);
            Gfx.text(g, running ? "0 pause" : "0 run 1 step # clear", W / 2, H - fh - 2, Gfx.TC, Gfx.SMALL, 0xDCE775);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        // a glider walking across
        int s = Math.max(3, h / 7);
        int[][] frames = {
            { 1, 0, 2, 1, 0, 2, 1, 2, 2, 2 },
            { 0, 1, 2, 1, 2, 2, 1, 2, 1, 3 },
            { 2, 1, 0, 2, 2, 2, 1, 3, 2, 3 },
            { 0, 1, 2, 2, 1, 3, 2, 2, 2, 3 },
        };
        int f = (clock / 6) % 4, shift = (clock / 24) % Math.max(1, w / s - 4);
        int[] p = frames[f];
        for (int k = 0; k < 10; k += 2) {
            g.setColor(0xC6FF00);
            g.fillRect(x + (p[k] + shift) * s, y + p[k + 1] * s + h / 4, s - 1, s - 1);
        }
    }
}
