package boxclaim;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Box Claim: dots and boxes. Draw lines between dots; completing the fourth
 * side of a box claims it and earns another turn.
 * The board is a doubled grid: dots at (even, even), boxes at (odd, odd),
 * lines at the remaining cells.
 */
public class BoxGame extends Game {
    private int n;          // boxes per side
    private int g;          // doubled grid size = 2n + 1
    private byte[] cell;    // lines: 0/1, boxes: 0 none, 1 you, 2 cpu
    private int cx = 1, cy = 0, turn, wait, mine, theirs, lastLine = -1;
    private final int[] tmpBoxes = new int[64];

    protected String name() { return "Box Claim"; }

    protected String[] help() {
        return new String[] {
            "Take turns drawing one line between two neighbouring dots. Draw the fourth side of a box to claim it - and then you must move again.",
            "Avoid drawing the third side of a box, or your opponent will take it (and often a whole chain of boxes after it).",
            "Whoever claims the most boxes wins.",
            "- Controls",
            "2/4/6/8: move between lines",
            "5: draw the highlighted line",
        };
    }

    protected String[] modes() { return new String[] { "4x4 vs CPU", "5x5 vs CPU", "5x5 vs Smart CPU", "4x4 2 Players" }; }

    protected boolean hasScore() { return mode != 3; }

    protected String formatScore(int s) { return s + " boxes"; }

    protected int accent() { return 0x2A9D8F; }

    private boolean cpu() { return mode != 3; }

    protected void newGame() {
        n = (mode == 0 || mode == 3) ? 4 : 5;
        g = n * 2 + 1;
        cell = new byte[g * g];
        cx = 1;
        cy = 0;
        turn = 1;
        mine = theirs = 0;
        lastLine = -1;
    }

    private boolean isLine(int x, int y) {
        return ((x + y) & 1) == 1;
    }

    private int sides(int bx, int by) {
        return cell[(by - 1) * g + bx] + cell[(by + 1) * g + bx] + cell[by * g + bx - 1] + cell[by * g + bx + 1];
    }

    /** Draw a line; returns how many boxes it completed (claimed for player p). */
    private int draw(int x, int y, int p) {
        cell[y * g + x] = 1;
        int got = 0;
        if ((y & 1) == 0) { // horizontal line: boxes above and below
            if (y > 0 && sides(x, y - 1) == 4) { cell[(y - 1) * g + x] = (byte) p; got++; }
            if (y < g - 1 && sides(x, y + 1) == 4) { cell[(y + 1) * g + x] = (byte) p; got++; }
        } else {
            if (x > 0 && sides(x - 1, y) == 4) { cell[y * g + x - 1] = (byte) p; got++; }
            if (x < g - 1 && sides(x + 1, y) == 4) { cell[y * g + x + 1] = (byte) p; got++; }
        }
        return got;
    }

    private void undraw(int x, int y) {
        // only undo trial claims (9); real claims (1/2) must survive
        cell[y * g + x] = 0;
        if ((y & 1) == 0) {
            if (y > 0 && cell[(y - 1) * g + x] == 9) cell[(y - 1) * g + x] = 0;
            if (y < g - 1 && cell[(y + 1) * g + x] == 9) cell[(y + 1) * g + x] = 0;
        } else {
            if (x > 0 && cell[y * g + x - 1] == 9) cell[y * g + x - 1] = 0;
            if (x < g - 1 && cell[y * g + x + 1] == 9) cell[y * g + x + 1] = 0;
        }
    }

    /** Would this line give the opponent a box (create a box with three sides)? */
    private boolean givesBox(int x, int y) {
        cell[y * g + x] = 1;
        boolean bad = false;
        if ((y & 1) == 0) {
            if (y > 0 && cell[(y - 1) * g + x] == 0 && sides(x, y - 1) == 3) bad = true;
            if (y < g - 1 && cell[(y + 1) * g + x] == 0 && sides(x, y + 1) == 3) bad = true;
        } else {
            if (x > 0 && cell[y * g + x - 1] == 0 && sides(x - 1, y) == 3) bad = true;
            if (x < g - 1 && cell[y * g + x + 1] == 0 && sides(x + 1, y) == 3) bad = true;
        }
        cell[y * g + x] = 0;
        return bad;
    }

    /** Boxes the opponent could greedily chain after this line (smart CPU). */
    private int chainCost(int x, int y) {
        byte[] save = new byte[cell.length];
        System.arraycopy(cell, 0, save, 0, cell.length);
        draw(x, y, 9);
        int taken = 0;
        boolean again = true;
        while (again) {
            again = false;
            for (int yy = 0; yy < g && !again; yy++) {
                for (int xx = 0; xx < g && !again; xx++) {
                    if (!isLine(xx, yy) || cell[yy * g + xx] != 0) continue;
                    int got = draw(xx, yy, 9);
                    if (got > 0) {
                        taken += got;
                        again = true;
                    } else {
                        cell[yy * g + xx] = 0;
                    }
                }
            }
        }
        System.arraycopy(save, 0, cell, 0, cell.length);
        return taken;
    }

    private int cpuMove() {
        // 1. take any box
        for (int y = 0; y < g; y++) for (int x = 0; x < g; x++) {
            if (!isLine(x, y) || cell[y * g + x] != 0) continue;
            int got = draw(x, y, 9);
            undraw(x, y);
            if (got > 0) return y * g + x;
        }
        // 2. safe move
        int safe = 0;
        for (int y = 0; y < g; y++) for (int x = 0; x < g; x++) {
            if (isLine(x, y) && cell[y * g + x] == 0 && !givesBox(x, y)) tmpBoxes[safe++ % 64] = y * g + x;
        }
        if (safe > 0) return tmpBoxes[Rnd.nextInt(Math.min(safe, 64))];
        // 3. least damaging move
        int best = -1, bestCost = 1000;
        for (int y = 0; y < g; y++) for (int x = 0; x < g; x++) {
            if (!isLine(x, y) || cell[y * g + x] != 0) continue;
            int cost = mode == 2 ? chainCost(x, y) : Rnd.nextInt(5);
            if (cost < bestCost) {
                bestCost = cost;
                best = y * g + x;
            }
        }
        return best;
    }

    private void move(int x, int y) {
        int got = draw(x, y, turn);
        lastLine = y * g + x;
        if (turn == 1) mine += got; else theirs += got;
        if (got > 0) Sfx.good(); else Sfx.click();
        if (mine + theirs == n * n) {
            score = mine;
            if (mine == theirs) {
                headline = "DRAW";
                endGame(false);
            } else if (cpu()) {
                headline = mine > theirs ? "YOU WIN " + mine + "-" + theirs : "CPU WINS " + theirs + "-" + mine;
                endGame(mine > theirs);
            } else {
                headline = (mine > theirs ? "BLUE" : "RED") + " WINS";
                endGame(true);
            }
            return;
        }
        if (got == 0) turn = 3 - turn;
    }

    protected void update() {
        if (cpu() && turn == 2) {
            if (++wait < 7) return;
            wait = 0;
            int m = cpuMove();
            if (m >= 0) move(m % g, m / g);
            return;
        }
        if ((pressed & K_RIGHT) != 0) { cx += 2; if (cx >= g) cx = (cy & 1) == 0 ? 1 : 0; }
        if ((pressed & K_LEFT) != 0) { cx -= 2; if (cx < 0) cx = (cy & 1) == 0 ? g - 2 : g - 1; }
        if ((pressed & K_DOWN) != 0) { cy = (cy + 1) % g; cx = fix(cx, cy); }
        if ((pressed & K_UP) != 0) { cy = (cy + g - 1) % g; cx = fix(cx, cy); }
        if ((pressed & K_FIRE) != 0) {
            if (cell[cy * g + cx] == 0) move(cx, cy);
            else Sfx.bad();
        }
    }

    /** After a vertical move, put the cursor on the nearest line cell of the new row. */
    private int fix(int x, int y) {
        if (isLine(x, y)) return x;
        return x + 1 < g ? x + 1 : x - 1;
    }

    protected void draw(Graphics gr) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int size = Math.min(W - 10, H - hud * 2 - 6);
        int s = size / n;
        int ox = (W - s * n) / 2, oy = hud + (H - hud * 2 - s * n) / 2;
        gr.setColor(0x0F2027);
        gr.fillRect(0, 0, W, H);
        int lw = Math.max(2, s / 10);
        for (int y = 0; y < g; y++) {
            for (int x = 0; x < g; x++) {
                int v = cell[y * g + x];
                int px = ox + x * s / 2, py = oy + y * s / 2;
                if ((x & 1) == 1 && (y & 1) == 1) {
                    if (v != 0) {
                        gr.setColor(v == 1 ? 0x1E5F99 : 0x9B2C2C);
                        gr.fillRect(px - s / 2 + lw, py - s / 2 + lw, s - lw * 2, s - lw * 2);
                        gr.setFont(Gfx.SMALL_B);
                        gr.setColor(0xFFFFFF);
                        gr.drawString(v == 1 ? (cpu() ? "Y" : "B") : (cpu() ? "C" : "R"), px + 1, py - 4, Gfx.TC);
                    }
                } else if (isLine(x, y)) {
                    boolean sel = x == cx && y == cy && state == PLAY && !(cpu() && turn == 2);
                    if (v != 0) gr.setColor(y * g + x == lastLine ? 0xFFEB3B : 0xE0E0E0);
                    else if (sel) gr.setColor((clock & 4) == 0 ? (turn == 1 ? 0x4FC3F7 : 0xEF5350) : 0x546E7A);
                    else continue;
                    if ((y & 1) == 0) gr.fillRect(px - s / 2 + 2, py - lw / 2, s - 4, lw);
                    else gr.fillRect(px - lw / 2, py - s / 2 + 2, lw, s - 4);
                }
            }
        }
        gr.setColor(0xB2DFDB);
        for (int y = 0; y < g; y += 2) for (int x = 0; x < g; x += 2) gr.fillRect(ox + x * s / 2 - 1, oy + y * s / 2 - 1, 3, 3);
        gr.setFont(Gfx.SMALL_B);
        gr.setColor(0x4FC3F7);
        gr.drawString((cpu() ? "You " : "Blue ") + mine, 2, 1, Gfx.TL);
        gr.setColor(0xEF5350);
        gr.drawString(theirs + (cpu() ? " CPU" : " Red"), W - 2, 1, Gfx.TR);
        if (state == PLAY) {
            gr.setFont(Gfx.SMALL);
            gr.setColor(0x80CBC4);
            gr.drawString(turn == 1 ? (cpu() ? "your turn" : "blue's turn") : (cpu() ? "CPU..." : "red's turn"), W / 2, H - hud + 1, Gfx.TC);
        }
    }

    protected void drawTitleArt(Graphics gr, int x, int y, int w, int h) {
        int s = Math.max(10, Math.min(h - 4, w / 4));
        int ox = x + (w - s * 3) / 2, oy = y + (h - s) / 2;
        int t = (clock / 8) % 8;
        gr.setColor(0x1E5F99);
        if (t >= 4) gr.fillRect(ox + 2, oy + 2, s - 3, s - 3);
        gr.setColor(0xE0E0E0);
        if (t >= 0) gr.fillRect(ox, oy, s, 2);
        if (t >= 1) gr.fillRect(ox, oy, 2, s);
        if (t >= 2) gr.fillRect(ox, oy + s - 1, s, 2);
        if (t >= 3) gr.fillRect(ox + s - 1, oy, 2, s);
        gr.fillRect(ox + s, oy, s * 2, 2);
        gr.setColor(0xB2DFDB);
        for (int k = 0; k <= 3; k++) {
            gr.fillRect(ox + k * s - 1, oy - 1, 3, 3);
            gr.fillRect(ox + k * s - 1, oy + s - 1, 3, 3);
        }
    }
}
