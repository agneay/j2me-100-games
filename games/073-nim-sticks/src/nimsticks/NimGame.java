package nimsticks;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Nim Sticks: take any number of sticks from a single row. In the normal
 * game whoever takes the last stick wins; in misere they lose.
 * The expert CPU plays perfectly using the nim-sum (XOR) strategy.
 */
public class NimGame extends Game {
    private final int[] rows = new int[5];
    private int nRows, row, take = 1, turn, wait, lastRow = -1, lastTake;
    private String note = "";

    protected String name() { return "Nim Sticks"; }

    protected String[] help() {
        return new String[] {
            "Players take turns removing sticks. On your turn take one or more sticks, all from the same row.",
            "Normal: whoever takes the very last stick wins. Misere: whoever takes the last stick loses!",
            "The expert CPU never makes a mistake - but there is always a way to beat it from the right position. Hint: think in binary.",
            "- Controls",
            "2/8: choose row  4/6: how many",
            "5: take sticks",
        };
    }

    protected String[] modes() { return new String[] { "Normal vs CPU", "Misere vs CPU", "Normal vs Expert", "Misere vs Expert" }; }

    protected boolean hasScore() { return false; }

    protected String footer() { return "Won " + saved[0] + "  Lost " + saved[1]; }

    protected int accent() { return 0xD4A373; }

    private boolean misere() { return mode == 1 || mode == 3; }

    private boolean expert() { return mode >= 2; }

    protected void newGame() {
        nRows = 4;
        rows[0] = 1;
        rows[1] = 3;
        rows[2] = 5;
        rows[3] = 7;
        if (Rnd.chance(40)) {
            nRows = Rnd.range(3, 5);
            for (int i = 0; i < nRows; i++) rows[i] = Rnd.range(1, 8);
        }
        saved[5] ^= 1;
        turn = saved[5];
        row = 0;
        take = 1;
        note = turn == 0 ? "Your turn" : "CPU starts";
        lastRow = -1;
    }

    private int total() {
        int t = 0;
        for (int i = 0; i < nRows; i++) t += rows[i];
        return t;
    }

    private int nimSum() {
        int x = 0;
        for (int i = 0; i < nRows; i++) x ^= rows[i];
        return x;
    }

    private void cpuMove() {
        int r = -1, k = 0;
        boolean smart = expert() || Rnd.chance(55);
        if (smart) {
            int big = 0;
            for (int i = 0; i < nRows; i++) if (rows[i] > 1) big++;
            if (misere() && big <= 1) {
                // endgame of misere: leave an odd number of single-stick rows
                int ones = 0, bigRow = -1;
                for (int i = 0; i < nRows; i++) {
                    if (rows[i] == 1) ones++;
                    if (rows[i] > 1) bigRow = i;
                }
                if (bigRow >= 0) {
                    r = bigRow;
                    k = ones % 2 == 0 ? rows[bigRow] - 1 : rows[bigRow];
                }
            }
            if (r < 0) {
                int x = nimSum();
                if (x != 0) {
                    for (int i = 0; i < nRows; i++) {
                        int target = rows[i] ^ x;
                        if (target < rows[i]) {
                            r = i;
                            k = rows[i] - target;
                            break;
                        }
                    }
                }
            }
        }
        if (r < 0 || k <= 0) {
            do r = Rnd.nextInt(nRows); while (rows[r] == 0);
            k = Rnd.range(1, rows[r]);
        }
        apply(r, k);
    }

    private void apply(int r, int k) {
        rows[r] -= k;
        lastRow = r;
        lastTake = k;
        Sfx.tone(turn == 0 ? 72 : 60, 30);
        if (total() == 0) {
            boolean moverWins = !misere();
            boolean playerWins = (turn == 0) == moverWins;
            saved[playerWins ? 0 : 1]++;
            headline = playerWins ? "YOU WIN!" : "CPU WINS";
            endGame(playerWins);
            return;
        }
        turn = 1 - turn;
        note = turn == 0 ? "CPU took " + k + " from row " + (r + 1) : "";
    }

    protected void update() {
        if (turn == 1) {
            if (++wait < 20) return;
            wait = 0;
            cpuMove();
            return;
        }
        if ((pressed & K_UP) != 0) row = (row + nRows - 1) % nRows;
        if ((pressed & K_DOWN) != 0) row = (row + 1) % nRows;
        if (rows[row] == 0) {
            for (int k = 0; k < nRows; k++) if (rows[(row + k) % nRows] > 0) { row = (row + k) % nRows; break; }
        }
        if ((pressed & K_LEFT) != 0 && take > 1) take--;
        if ((pressed & K_RIGHT) != 0 && take < rows[row]) take++;
        if (take > rows[row]) take = rows[row];
        if (take < 1) take = 1;
        if ((pressed & K_FIRE) != 0) {
            apply(row, take);
            take = 1;
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x2B1D14);
        g.fillRect(0, 0, W, H);
        int rh = (H - hud * 3) / Math.max(1, nRows);
        int sw = Math.max(3, Math.min(W / 18, 6));
        for (int r = 0; r < nRows; r++) {
            int y = hud * 2 + r * rh;
            if (r == row && turn == 0 && state == PLAY) {
                g.setColor(0x4E342E);
                g.fillRect(0, y, W, rh);
            }
            int n = rows[r];
            int x0 = (W - 8 * sw * 2) / 2;
            for (int k = 0; k < n; k++) {
                int x = x0 + k * sw * 2;
                boolean marked = r == row && turn == 0 && k >= n - take;
                g.setColor(marked ? 0xFF7043 : 0xE9C46A);
                g.fillRect(x, y + 3, sw, rh - 8);
                g.setColor(0xD62828);
                g.fillRect(x, y + 3, sw, Math.max(2, sw));
            }
            if (r == lastRow && turn == 0) {
                g.setColor(0x8D6E63);
                for (int k = 0; k < lastTake; k++) g.drawRect(x0 + (n + k) * sw * 2, y + 3, sw - 1, rh - 9);
            }
            g.setFont(Gfx.SMALL);
            g.setColor(0xBCAAA4);
            g.drawString(String.valueOf(r + 1), 3, y + rh / 2 - 4, Gfx.TL);
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFE0B2);
        g.drawString(misere() ? "Last stick LOSES" : "Last stick WINS", W / 2, 1, Gfx.TC);
        String t = turn == 0 ? "Take " + take + " from row " + (row + 1) : "CPU thinking...";
        Gfx.text(g, note.length() > 0 && turn == 0 && frame % 80 < 40 ? note : t, W / 2, H - hud, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int[] demo = { 1, 3, 5 };
        int rh = h / 3;
        for (int r = 0; r < 3; r++) {
            for (int k = 0; k < demo[r]; k++) {
                g.setColor((clock / 10) % 6 == r * 2 && k == demo[r] - 1 ? 0xFF7043 : 0xE9C46A);
                g.fillRect(x + w / 2 - demo[r] * 4 + k * 8, y + r * rh + 2, 3, rh - 4);
            }
        }
    }
}
