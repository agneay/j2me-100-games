package noughtsgrid;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Noughts Grid: noughts and crosses where keys 1-9 are the board.
 * Modes: Classic 3x3 (CPU easy / perfect, 2 players) and Big 5x5 (four in a row).
 */
public class NoughtsGame extends Game {
    private int n, need;
    private int[] b;
    private int turn, cur, moves, aiWait, winStart = -1, winStep;
    private int animCell = -1, animT;

    protected String name() { return "Noughts Grid"; }

    protected String[] help() {
        return new String[] {
            "Take turns placing X and O. Three in a row (horizontal, vertical or diagonal) wins on the classic board; on the 5x5 board you need four.",
            "On the 3x3 board the keypad IS the board: press 1-9 to play in that square. Or move the cursor with the joystick and press 5.",
            "The perfect CPU never loses - can you force a draw?",
            "- Controls",
            "1-9: play square (3x3)",
            "2/4/6/8 + 5: cursor play",
        };
    }

    protected String[] modes() { return new String[] { "3x3 vs CPU Easy", "3x3 vs CPU Perfect", "3x3 2 Players", "5x5 vs CPU", "5x5 2 Players" }; }

    protected boolean hasScore() { return false; }

    protected String footer() { return "Won " + saved[0] + " Lost " + saved[1] + " Draw " + saved[2]; }

    protected int accent() { return 0xA47551; }

    private boolean cpu() { return mode != 2 && mode != 4; }

    protected void newGame() {
        n = mode >= 3 ? 5 : 3;
        need = n == 3 ? 3 : 4;
        b = new int[n * n];
        moves = 0;
        saved[4] ^= 1;
        turn = 1;
        if (cpu() && saved[4] == 1) turn = 2; // CPU (O) starts every other game
        cur = n * n / 2;
        winStart = -1;
        aiWait = 0;
    }

    private int winner() {
        for (int i = 0; i < n * n; i++) {
            int p = b[i];
            if (p == 0) continue;
            int x = i % n, y = i / n;
            int[][] dirs = DIRS;
            for (int d = 0; d < 4; d++) {
                int dx = dirs[d][0], dy = dirs[d][1], k = 1;
                while (k < need) {
                    int xx = x + dx * k, yy = y + dy * k;
                    if (xx < 0 || yy < 0 || xx >= n || yy >= n || b[yy * n + xx] != p) break;
                    k++;
                }
                if (k == need) {
                    winStart = i;
                    winStep = dy * n + dx;
                    return p;
                }
            }
        }
        return 0;
    }

    private static final int[] TITLE_ORDER = { 4, 0, 8, 2, 6, 1, 7, 3, 5 };
    private static final int[][] DIRS = { { 1, 0 }, { 0, 1 }, { 1, 1 }, { -1, 1 } };

    // ---------------------------------------------------------------- AI

    private int minimax(int p, int depth) {
        int w = quietWinner();
        if (w != 0) return w == 2 ? 10 - depth : depth - 10;
        if (moves + depth == n * n) return 0;
        int best = p == 2 ? -100 : 100;
        for (int i = 0; i < n * n; i++) {
            if (b[i] != 0) continue;
            b[i] = p;
            int v = minimax(3 - p, depth + 1);
            b[i] = 0;
            if (p == 2 ? v > best : v < best) best = v;
        }
        return best;
    }

    private int quietWinner() {
        int save = winStart, step = winStep;
        int w = winner();
        winStart = save;
        winStep = step;
        return w;
    }

    private int lineScore(int i, int p) {
        // heuristic for 5x5: longest own run through i plus blocking value
        int x = i % n, y = i / n, best = 0;
        for (int d = 0; d < 4; d++) {
            int dx = DIRS[d][0], dy = DIRS[d][1];
            int run = 1;
            for (int s = -1; s <= 1; s += 2) {
                for (int k = 1; k < need; k++) {
                    int xx = x + dx * k * s, yy = y + dy * k * s;
                    if (xx < 0 || yy < 0 || xx >= n || yy >= n || b[yy * n + xx] != p) break;
                    run++;
                }
            }
            best = Math.max(best, run);
        }
        return best;
    }

    private int cpuMove() {
        if (n == 3) {
            if (mode == 0 && Rnd.chance(45)) {
                int k;
                do k = Rnd.nextInt(9); while (b[k] != 0);
                return k;
            }
            int best = -1000, bestI = -1;
            for (int i = 0; i < 9; i++) {
                if (b[i] != 0) continue;
                b[i] = 2;
                int v = minimax(1, 1);
                b[i] = 0;
                if (v > best || (v == best && Rnd.chance(40))) {
                    best = v;
                    bestI = i;
                }
            }
            return bestI;
        }
        int best = -1, bestI = -1;
        for (int i = 0; i < n * n; i++) {
            if (b[i] != 0) continue;
            b[i] = 2;
            int mine = lineScore(i, 2);
            b[i] = 1;
            int theirs = lineScore(i, 1);
            b[i] = 0;
            int v = (mine >= need ? 1000 : mine * 10) + (theirs >= need ? 500 : theirs * 9);
            int x = i % n, y = i / n;
            v += 4 - Math.abs(x - 2) - Math.abs(y - 2);
            v = v * 4 + Rnd.nextInt(4);
            if (v > best) {
                best = v;
                bestI = i;
            }
        }
        return bestI;
    }

    // ------------------------------------------------------------ flow

    private void place(int i) {
        if (b[i] != 0) {
            Sfx.bad();
            return;
        }
        b[i] = turn;
        moves++;
        animCell = i;
        animT = 0;
        Sfx.tone(turn == 1 ? 72 : 64, 30);
        int w = winner();
        if (w != 0) {
            boolean human = !cpu() || w == 1;
            if (cpu()) saved[human ? 0 : 1]++;
            headline = cpu() ? (human ? "YOU WIN!" : "CPU WINS") : (w == 1 ? "X WINS!" : "O WINS!");
            endGame(human);
            return;
        }
        if (moves == n * n) {
            if (cpu()) saved[2]++;
            headline = "DRAW";
            won = false;
            endGame(false);
            return;
        }
        turn = 3 - turn;
    }

    protected void update() {
        if (animT < 6) animT++;
        if (cpu() && turn == 2) {
            if (++aiWait < 8) return;
            aiWait = 0;
            place(cpuMove());
            return;
        }
        int d = digitPressed();
        if (n == 3 && d >= 1) {
            cur = d - 1;
            place(cur);
            return;
        }
        if (n == 5 || d < 0) {
            int x = cur % n, y = cur / n;
            if ((pressed & K_LEFT) != 0) x = (x + n - 1) % n;
            if ((pressed & K_RIGHT) != 0) x = (x + 1) % n;
            if ((pressed & K_UP) != 0) y = (y + n - 1) % n;
            if ((pressed & K_DOWN) != 0) y = (y + 1) % n;
            cur = y * n + x;
            if ((pressed & K_FIRE) != 0 && (n == 5 || d < 0)) place(cur);
        }
    }

    private void mark(Graphics g, int p, int x, int y, int s, int t) {
        int m = s / 5;
        int len = (s - 2 * m) * t / 6;
        if (p == 1) {
            g.setColor(0x4FC3F7);
            for (int k = -1; k <= 1; k++) {
                g.drawLine(x + m + k, y + m, x + m + len + k, y + m + len);
                g.drawLine(x + s - m + k, y + m, x + s - m - len + k, y + m + len);
            }
        } else {
            g.setColor(0xFF8A65);
            int r = (s / 2 - m) * t / 6;
            Gfx.ring(g, x + s / 2, y + s / 2, r);
            Gfx.ring(g, x + s / 2, y + s / 2, Math.max(0, r - 1));
            Gfx.ring(g, x + s / 2, y + s / 2, Math.max(0, r - 2));
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int size = Math.min(W - 8, H - hud * 3);
        int s = size / n;
        size = s * n;
        int ox = (W - size) / 2, oy = hud * 2 + (H - hud * 3 - size) / 2;
        g.setColor(0x1C1A24);
        g.fillRect(0, 0, W, H);
        for (int i = 0; i < n * n; i++) {
            int x = ox + (i % n) * s, y = oy + (i / n) * s;
            if (n == 3 && b[i] == 0) {
                g.setFont(Gfx.SMALL);
                g.setColor(0x3A3646);
                g.drawString(String.valueOf(i + 1), x + 3, y + 2, Gfx.TL);
            }
            if (b[i] != 0) mark(g, b[i], x, y, s, i == animCell ? animT : 6);
        }
        g.setColor(0xD7CCC8);
        for (int k = 1; k < n; k++) {
            g.fillRect(ox + k * s - 1, oy + 2, 2, size - 4);
            g.fillRect(ox + 2, oy + k * s - 1, size - 4, 2);
        }
        if (winStart >= 0) {
            g.setColor(0xFFEB3B);
            int x0 = ox + (winStart % n) * s + s / 2, y0 = oy + (winStart / n) * s + s / 2;
            int e = winStart + winStep * (need - 1);
            int x1 = ox + (e % n) * s + s / 2, y1 = oy + (e / n) * s + s / 2;
            g.drawLine(x0, y0, x1, y1);
            g.drawLine(x0 + 1, y0, x1 + 1, y1);
        }
        if (state == PLAY && !(cpu() && turn == 2)) {
            g.setColor((clock & 4) == 0 ? 0xFFFFFF : 0x9E9E9E);
            g.drawRect(ox + (cur % n) * s + 2, oy + (cur / n) * s + 2, s - 5, s - 5);
        }
        String t;
        if (state == OVER) t = "";
        else if (cpu()) t = turn == 1 ? "Your move (X)" : "CPU thinking...";
        else t = turn == 1 ? "X to move" : "O to move";
        Gfx.text(g, t, W / 2, hud / 2, Gfx.TC, Gfx.SMALL_B, turn == 1 ? 0x4FC3F7 : 0xFF8A65);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(8, Math.min(h, w) / 3);
        int ox = x + (w - s * 3) / 2, oy = y + (h - s * 3) / 2;
        g.setColor(0xD7CCC8);
        for (int k = 1; k < 3; k++) {
            g.fillRect(ox + k * s - 1, oy, 2, s * 3);
            g.fillRect(ox, oy + k * s - 1, s * 3, 2);
        }
        int shown = (clock / 12) % 10;
        for (int k = 0; k < Math.min(shown, 9); k++) {
            int i = TITLE_ORDER[k];
            mark(g, k % 2 == 0 ? 1 : 2, ox + (i % 3) * s, oy + (i / 3) * s, s, 6);
        }
    }
}
