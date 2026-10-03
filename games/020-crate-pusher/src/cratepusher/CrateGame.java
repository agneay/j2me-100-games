package cratepusher;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Crate Pusher: push every crate onto a target. 20 generated, solver-checked levels. */
public class CrateGame extends Game {
    private static final int MAX_UNDO = 600;

    private int level, cols, rows;
    private char[] cells; // '#', ' ', '.'
    private boolean[] crate;
    private int px, py, moves, pushes, solvedTimer;
    private final int[] undo = new int[MAX_UNDO]; // dir | (pushed << 2)
    private int undoN;
    private Image man;
    private int manScale;
    private static final int[] DX = { 0, 1, 0, -1 }, DY = { -1, 0, 1, 0 };

    protected String name() { return "Crate Pusher"; }

    protected String[] help() {
        return new String[] {
            "Push every crate onto a target square. You can only push (never pull), and only one crate at a time, so think before you shove one into a corner.",
            "Every level was checked by a solver; the number shown as 'par' is the fewest pushes possible.",
            "Undo as many moves as you like with 0, or restart the level with #. Solved levels unlock the next one; pick any unlocked level on the title screen.",
            "- Controls",
            "2/4/6/8: walk / push",
            "0: undo  #: restart level",
        };
    }

    protected String[] modes() {
        int unlocked = Math.min(Levels.DATA.length, Math.max(1, saved[0] + 1));
        String[] m = new String[unlocked];
        for (int i = 0; i < unlocked; i++) m[i] = "Level " + (i + 1);
        return m;
    }

    protected boolean hasScore() { return false; }

    protected String footer() { return "Solved " + Math.min(saved[0], Levels.DATA.length) + " / " + Levels.DATA.length; }

    protected int accent() { return 0xC68B4E; }

    protected void newGame() {
        level = mode;
        load();
    }

    private void load() {
        String[] d = Levels.DATA[level];
        rows = d.length;
        cols = d[0].length();
        cells = new char[cols * rows];
        crate = new boolean[cols * rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                char c = d[y].charAt(x);
                int i = y * cols + x;
                cells[i] = (c == '#') ? '#' : ((c == '.' || c == '*' || c == '+') ? '.' : ' ');
                crate[i] = c == '$' || c == '*';
                if (c == '@' || c == '+') {
                    px = x;
                    py = y;
                }
            }
        }
        moves = pushes = undoN = 0;
        solvedTimer = 0;
    }

    private boolean solved() {
        for (int i = 0; i < cells.length; i++) if (crate[i] && cells[i] != '.') return false;
        return true;
    }

    private void tryMove(int d) {
        int nx = px + DX[d], ny = py + DY[d];
        int ni = ny * cols + nx;
        if (cells[ni] == '#') return;
        boolean push = false;
        if (crate[ni]) {
            int bx = nx + DX[d], by = ny + DY[d];
            int bi = by * cols + bx;
            if (cells[bi] == '#' || crate[bi]) {
                Sfx.tone(40, 20);
                return;
            }
            crate[ni] = false;
            crate[bi] = true;
            push = true;
            pushes++;
            Sfx.tone(cells[bi] == '.' ? 84 : 55, 25);
        }
        px = nx;
        py = ny;
        moves++;
        if (undoN == MAX_UNDO) {
            System.arraycopy(undo, 1, undo, 0, MAX_UNDO - 1);
            undoN--;
        }
        undo[undoN++] = d | (push ? 4 : 0);
        if (solved()) {
            solvedTimer = 40;
            if (saved[0] < level + 1) saved[0] = level + 1;
            persist();
            Sfx.win();
        }
    }

    private void undoMove() {
        if (undoN == 0) return;
        int u = undo[--undoN];
        int d = u & 3;
        if ((u & 4) != 0) {
            int bx = px + DX[d], by = py + DY[d];
            crate[by * cols + bx] = false;
            crate[py * cols + px] = true;
            pushes--;
        }
        px -= DX[d];
        py -= DY[d];
        moves--;
        Sfx.click();
    }

    protected void update() {
        if (solvedTimer > 0) {
            if (--solvedTimer == 0 || (pressed & K_FIRE) != 0) {
                solvedTimer = 0;
                if (level + 1 >= Levels.DATA.length) {
                    headline = "ALL SOLVED!";
                    endGame(true);
                } else {
                    level++;
                    mode = Math.min(level, modes().length - 1);
                    load();
                }
            }
            return;
        }
        if ((pressed & K_UP) != 0) tryMove(0);
        else if ((pressed & K_RIGHT) != 0) tryMove(1);
        else if ((pressed & K_DOWN) != 0) tryMove(2);
        else if ((pressed & K_LEFT) != 0) tryMove(3);
        else if (digit(0)) undoMove();
        else if ((pressed & K_POUND) != 0) {
            load();
            Sfx.click();
        }
    }

    private void buildMan(int sc) {
        manScale = sc;
        man = Gfx.sprite(new String[] { "..111...", ".12221..", ".13331..", "..131...", ".14441..", "1.444.1.", "..4.4...", ".11.11.." },
                new int[] { 0, 0x2D1B0E, 0x1565C0, 0xFFCC80, 0xEF6C00 }, sc);
    }

    private void drawCell(Graphics g, int i, int x, int y, int s) {
        char c = cells[i];
        if (c == '#') {
            g.setColor(0x6D4C41);
            g.fillRect(x, y, s, s);
            g.setColor(0x5D4037);
            g.drawLine(x, y + s / 2, x + s - 1, y + s / 2);
            g.drawLine(x + s / 2, y, x + s / 2, y + s / 2);
            g.drawLine(x + s / 4, y + s / 2, x + s / 4, y + s - 1);
            g.setColor(0x8D6E63);
            g.drawLine(x, y, x + s - 1, y);
            return;
        }
        g.setColor(0x2B2520);
        g.fillRect(x, y, s, s);
        if (c == '.') {
            g.setColor(0xFFC107);
            int r = Math.max(2, s / 4);
            g.fillTriangle(x + s / 2, y + s / 2 - r, x + s / 2 - r, y + s / 2, x + s / 2 + r, y + s / 2);
            g.fillTriangle(x + s / 2, y + s / 2 + r, x + s / 2 - r, y + s / 2, x + s / 2 + r, y + s / 2);
        }
        if (crate[i]) {
            int m = Math.max(1, s / 10);
            boolean on = c == '.';
            Gfx.bevel(g, x + m, y + m, s - 2 * m, s - 2 * m, on ? 0x7CB342 : 0xC68B4E);
            g.setColor(on ? 0x558B2F : 0x8D5524);
            g.drawLine(x + m + 1, y + m + 1, x + s - m - 2, y + s - m - 2);
            g.drawLine(x + s - m - 2, y + m + 1, x + m + 1, y + s - m - 2);
            g.drawRect(x + m + 1, y + m + 1, s - 2 * m - 3, s - 2 * m - 3);
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int s = Math.max(6, Math.min(W / cols, (H - hud * 2) / rows));
        int ox = (W - s * cols) / 2, oy = hud + (H - hud * 2 - s * rows) / 2;
        int sc = Math.max(1, s / 9);
        if (man == null || manScale != sc) buildMan(sc);
        g.setColor(0x15110E);
        g.fillRect(0, 0, W, H);
        for (int i = 0; i < cells.length; i++) drawCell(g, i, ox + (i % cols) * s, oy + (i / cols) * s, s);
        g.drawImage(man, ox + px * s + s / 2, oy + py * s + s / 2, Gfx.CC);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFE0B2);
        g.drawString("Level " + (level + 1), 2, 1, Gfx.TL);
        g.drawString("Par " + Levels.PAR[level], W - 2, 1, Gfx.TR);
        g.setFont(Gfx.SMALL);
        g.setColor(0xBCAAA4);
        g.drawString("Moves " + moves + "  Pushes " + pushes, W / 2, H - hud + 1, Gfx.TC);
        if (solvedTimer > 0) {
            String t = "SOLVED!";
            Gfx.panel(g, 6, H / 2 - hud * 2, W - 12, hud * 3, 0x1B2B16, 0x7CB342);
            Gfx.text(g, t, W / 2, H / 2 - hud * 2 + 3, Gfx.TC, Gfx.fit(t, W - 20), 0x9CCC65);
            Gfx.text(g, pushes <= Levels.PAR[level] ? "Perfect - par pushes!" : pushes + " pushes (par " + Levels.PAR[level] + ")",
                    W / 2, H / 2 + 1, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(8, Math.min(h - 2, w / 7));
        int sc = Math.max(1, s / 9);
        if (man == null || manScale != sc) buildMan(sc);
        int n = Math.min(6, w / s);
        int x0 = x + (w - n * s) / 2, yy = y + (h - s) / 2;
        int t = (clock / 8) % (n - 2);
        for (int k = 0; k < n; k++) {
            g.setColor(0x2B2520);
            g.fillRect(x0 + k * s, yy, s, s);
        }
        g.setColor(0xFFC107);
        g.fillRect(x0 + (n - 1) * s + s / 3, yy + s / 3, s / 3, s / 3);
        g.drawImage(man, x0 + t * s + s / 2, yy + s / 2, Gfx.CC);
        Gfx.bevel(g, x0 + (t + 1) * s + 1, yy + 1, s - 2, s - 2, 0xC68B4E);
    }
}
