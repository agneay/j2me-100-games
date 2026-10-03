package landgrab;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Land Grab: territory capture. Each turn pick a colour: your territory turns
 * that colour and swallows every touching tile of it. Own the most land.
 */
public class LandGame extends Game {
    private static final int N = 11, COLORS = 6;
    private static final int[] COL = { 0xE53935, 0xFDD835, 0x43A047, 0x1E88E5, 0x8E24AA, 0xFB8C00 };

    private final int[] c = new int[N * N];
    private final int[] owner = new int[N * N]; // 0 none, 1 you, 2 cpu
    private final int[] stack = new int[N * N];
    private final int[] tmpOwner = new int[N * N];
    private int pick, turn, wait, mine, theirs, lastGain;

    protected String name() { return "Land Grab"; }

    protected String[] help() {
        return new String[] {
            "You start in the bottom-left corner, the CPU in the top-right. Each turn choose a colour: your whole territory becomes that colour and captures every neighbouring tile of the same colour.",
            "You can't pick your own current colour or the CPU's. Grab more than half the board to win.",
            "- Controls",
            "1-6: choose colour directly",
            "4/6: highlight colour  5: choose",
        };
    }

    protected String[] modes() { return new String[] { "vs CPU", "vs Clever CPU" }; }

    protected String formatScore(int s) { return s + " tiles"; }

    protected int accent() { return 0x2A9D8F; }

    protected void newGame() {
        for (int i = 0; i < N * N; i++) {
            c[i] = Rnd.nextInt(COLORS);
            owner[i] = 0;
        }
        int me = (N - 1) * N, cpu = N - 1;
        if (c[cpu] == c[me]) c[cpu] = (c[me] + 1) % COLORS;
        owner[me] = 1;
        owner[cpu] = 2;
        grow(owner, 1, c[me]);
        grow(owner, 2, c[cpu]);
        turn = 1;
        pick = (c[me] + 1) % COLORS;
        if (pick == c[cpu]) pick = (pick + 1) % COLORS;
        count();
    }

    private int colorOf(int p) {
        for (int i = 0; i < N * N; i++) if (owner[i] == p) return c[i];
        return -1;
    }

    /** Recolour player p's region to col, then flood-capture; returns tiles gained. */
    private int grow(int[] own, int p, int col) {
        int before = 0, sp = 0;
        for (int i = 0; i < N * N; i++) {
            if (own[i] == p) {
                before++;
                stack[sp++] = i;
                if (own == owner) c[i] = col;
            }
        }
        while (sp > 0) {
            int i = stack[--sp];
            int x = i % N, y = i / N;
            for (int d = 0; d < 4; d++) {
                int nx = x + (d == 0 ? 1 : (d == 1 ? -1 : 0)), ny = y + (d == 2 ? 1 : (d == 3 ? -1 : 0));
                if (nx < 0 || ny < 0 || nx >= N || ny >= N) continue;
                int j = ny * N + nx;
                if (own[j] == 0 && c[j] == col) {
                    own[j] = p;
                    stack[sp++] = j;
                }
            }
        }
        int after = 0;
        for (int i = 0; i < N * N; i++) if (own[i] == p) after++;
        return after - before;
    }

    private int trialGain(int p, int col) {
        System.arraycopy(owner, 0, tmpOwner, 0, N * N);
        return grow(tmpOwner, p, col);
    }

    private int cpuChoice() {
        int myCol = colorOf(2), yourCol = colorOf(1);
        int best = -1000, bestCol = 0;
        for (int col = 0; col < COLORS; col++) {
            if (col == myCol || col == yourCol) continue;
            int gain = trialGain(2, col);
            int v = gain * 10;
            if (mode == 1) {
                // also deny: how much would the player gain from this colour next turn?
                int deny = trialGain(1, col);
                v += deny * 4;
            }
            v += Rnd.nextInt(3);
            if (v > best) {
                best = v;
                bestCol = col;
            }
        }
        return bestCol;
    }

    private void count() {
        mine = theirs = 0;
        for (int i = 0; i < N * N; i++) {
            if (owner[i] == 1) mine++;
            else if (owner[i] == 2) theirs++;
        }
    }

    private void play(int p, int col) {
        lastGain = grow(owner, p, col);
        count();
        Sfx.tone(p == 1 ? 72 : 60, 30);
        score = mine;
        int half = N * N / 2;
        if (mine > half || theirs > half || mine + theirs == N * N) {
            headline = mine > theirs ? "LAND OWNER!" : (mine == theirs ? "DRAW" : "CPU WINS");
            endGame(mine > theirs);
            return;
        }
        turn = 3 - turn;
    }

    protected void update() {
        int myCol = colorOf(1), cpuCol = colorOf(2);
        if (turn == 2) {
            if (++wait < 12) return;
            wait = 0;
            play(2, cpuChoice());
            return;
        }
        int d = digitPressed();
        if (d >= 1 && d <= COLORS) {
            if (d - 1 == myCol || d - 1 == cpuCol) Sfx.bad();
            else play(1, d - 1);
            return;
        }
        if ((pressed & (K_LEFT | K_RIGHT)) != 0) {
            int step = (pressed & K_LEFT) != 0 ? COLORS - 1 : 1;
            do {
                pick = (pick + step) % COLORS;
            } while (pick == myCol || pick == cpuCol);
        }
        if (pick == myCol || pick == cpuCol) pick = (pick + 1) % COLORS;
        if ((pressed & K_FIRE) != 0 && d < 0) play(1, pick);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int btn = Math.max(12, Math.min(W / 7, H / 9));
        int s = Math.max(6, Math.min(W - 4, H - hud - btn - 8) / N);
        int ox = (W - s * N) / 2, oy = hud + 2;
        g.setColor(0x101820);
        g.fillRect(0, 0, W, H);
        for (int i = 0; i < N * N; i++) {
            int x = ox + (i % N) * s, y = oy + (i / N) * s;
            g.setColor(owner[i] == 0 ? Gfx.shade(COL[c[i]], -35) : COL[c[i]]);
            g.fillRect(x, y, s, s);
            if (owner[i] != 0) {
                g.setColor(owner[i] == 1 ? 0xFFFFFF : 0x000000);
                if (s >= 8) g.fillRect(x + s / 2, y + s / 2, 1, 1);
            }
        }
        g.setColor(0xFFFFFF);
        g.drawRect(ox - 1, oy - 1, s * N + 1, s * N + 1);
        int myCol = colorOf(1), cpuCol = colorOf(2);
        int by = H - btn - 2;
        int bx0 = (W - COLORS * (btn + 2)) / 2;
        for (int k = 0; k < COLORS; k++) {
            int x = bx0 + k * (btn + 2);
            boolean blocked = k == myCol || k == cpuCol;
            g.setColor(blocked ? Gfx.shade(COL[k], -70) : COL[k]);
            g.fillRect(x, by, btn, btn);
            if (k == pick && turn == 1 && state == PLAY) {
                g.setColor(0xFFFFFF);
                g.drawRect(x - 1, by - 1, btn + 1, btn + 1);
                g.drawRect(x - 2, by - 2, btn + 3, btn + 3);
            }
            g.setFont(Gfx.SMALL_B);
            g.setColor(blocked ? 0x555555 : 0x000000);
            g.drawString(String.valueOf(k + 1), x + btn / 2 + 1, by + (btn - 7) / 2, Gfx.TC);
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString("You " + mine, 2, 1, Gfx.TL);
        g.drawString(theirs + " CPU", W - 2, 1, Gfx.TR);
        g.setFont(Gfx.SMALL);
        g.setColor(0x80CBC4);
        g.drawString(turn == 1 ? "your turn" : "CPU...", W / 2, 1, Gfx.TC);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(6, Math.min(h / 4, w / 12));
        int cols = w / s, rows = h / s;
        int ox = x + (w - cols * s) / 2, oy = y + (h - rows * s) / 2;
        int reach = (clock / 3) % (cols + rows);
        for (int r = 0; r < rows; r++) {
            for (int k = 0; k < cols; k++) {
                int v = (k * 7 + r * 13) % COLORS;
                boolean mineT = (k + (rows - 1 - r)) < reach / 2;
                g.setColor(mineT ? COL[(reach / 4) % COLORS] : Gfx.shade(COL[v], -35));
                g.fillRect(ox + k * s, oy + r * s, s, s);
            }
        }
    }
}
