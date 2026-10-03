package minesweep;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/** Mine Sweep: clear the field without uncovering a mine. First reveal is always safe. */
public class MineGame extends Game {
    private static final int[] COLS = { 8, 12, 16 };
    private static final int[] ROWS = { 8, 12, 16 };
    private static final int[] MINES = { 10, 24, 44 };
    private static final int[] NUM_COLORS = { 0, 0x3355FF, 0x22AA22, 0xEE3333, 0x2222AA, 0xAA2222, 0x229999, 0x222222, 0x777777 };

    private int cols, rows, mines;
    private boolean[] mine;
    private byte[] adj;
    private byte[] st; // 0 hidden, 1 open, 2 flag
    private int[] stack;
    private int cx, cy, opened, flags;
    private boolean placed;
    private int startFrame, boom = -1;
    private int cell, ox, oy, camX, camY, hud;

    protected String name() { return "Mine Sweep"; }

    protected String[] help() {
        return new String[] {
            "Uncover every square that does not hide a mine. A number tells how many mines touch that square (including diagonals).",
            "Flag squares you are sure are mines. Pressing 5 on an opened number whose mines are all flagged opens its remaining neighbours.",
            "Your first square is always safe. Finish fast for a better time.",
            "- Controls",
            "2/4/6/8: move  1/3/7/9: diagonal",
            "5: open square",
            "0 or #: flag / unflag",
        };
    }

    protected String[] modes() { return new String[] { "Easy 8x8", "Medium 12x12", "Hard 16x16" }; }

    protected boolean lowerIsBetter() { return true; }

    protected String formatScore(int s) { return Gfx.time(s, tickMs); }

    protected int accent() { return 0x8899AA; }

    protected void newGame() {
        cols = COLS[mode];
        rows = ROWS[mode];
        mines = MINES[mode];
        int n = cols * rows;
        mine = new boolean[n];
        adj = new byte[n];
        st = new byte[n];
        stack = new int[n];
        cx = cols / 2;
        cy = rows / 2;
        opened = flags = 0;
        placed = false;
        boom = -1;
        startFrame = 0;
    }

    private void placeMines(int safe) {
        int sx = safe % cols, sy = safe / cols;
        int left = mines;
        while (left > 0) {
            int i = Rnd.nextInt(cols * rows);
            int x = i % cols, y = i / cols;
            if (mine[i] || (Math.abs(x - sx) <= 1 && Math.abs(y - sy) <= 1)) continue;
            mine[i] = true;
            left--;
        }
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int c = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && ny >= 0 && nx < cols && ny < rows && mine[ny * cols + nx]) c++;
                    }
                }
                adj[y * cols + x] = (byte) c;
            }
        }
        placed = true;
        startFrame = frame;
    }

    /** Open a square; flood-fills zeros. Returns false if a mine was hit. */
    private boolean open(int start) {
        if (st[start] != 0) return true;
        if (mine[start]) {
            boom = start;
            return false;
        }
        int sp = 0;
        stack[sp++] = start;
        st[start] = 1;
        opened++;
        while (sp > 0) {
            int i = stack[--sp];
            if (adj[i] != 0) continue;
            int x = i % cols, y = i / cols;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int nx = x + dx, ny = y + dy;
                    if (nx < 0 || ny < 0 || nx >= cols || ny >= rows) continue;
                    int j = ny * cols + nx;
                    if (st[j] == 0 && !mine[j]) {
                        st[j] = 1;
                        opened++;
                        stack[sp++] = j;
                    }
                }
            }
        }
        return true;
    }

    private boolean chord(int i) {
        int x = i % cols, y = i / cols, f = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && ny >= 0 && nx < cols && ny < rows && st[ny * cols + nx] == 2) f++;
            }
        }
        if (f != adj[i]) return true;
        boolean ok = true;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && ny >= 0 && nx < cols && ny < rows) ok &= open(ny * cols + nx);
            }
        }
        return ok;
    }

    protected void update() {
        int dx = 0, dy = 0;
        if ((pressed & K_LEFT) != 0) dx = -1;
        if ((pressed & K_RIGHT) != 0) dx = 1;
        if ((pressed & K_UP) != 0) dy = -1;
        if ((pressed & K_DOWN) != 0) dy = 1;
        if (digit(1)) { dx = -1; dy = -1; }
        if (digit(3)) { dx = 1; dy = -1; }
        if (digit(7)) { dx = -1; dy = 1; }
        if (digit(9)) { dx = 1; dy = 1; }
        cx = (cx + dx + cols) % cols;
        cy = (cy + dy + rows) % rows;
        int i = cy * cols + cx;
        if ((pressed & (K_NUM0 | K_POUND)) != 0 && st[i] != 1) {
            st[i] = (byte) (st[i] == 2 ? 0 : 2);
            flags += st[i] == 2 ? 1 : -1;
            Sfx.click();
        } else if ((pressed & K_FIRE) != 0 && st[i] != 2) {
            if (!placed) placeMines(i);
            boolean ok = st[i] == 1 ? chord(i) : open(i);
            if (!ok) {
                Sfx.bad();
                headline = "BOOM!";
                for (int k = 0; k < mine.length; k++) if (mine[k] && st[k] != 2) st[k] = 1;
                score = 0;
                endGame(false);
                return;
            }
            Sfx.tone(80, 15);
            if (opened == cols * rows - mines) {
                score = Math.max(1, frame - startFrame);
                for (int k = 0; k < mine.length; k++) if (mine[k]) st[k] = 2;
                endGame(true);
            }
        }
    }

    private void layout() {
        hud = Gfx.SMALL.getHeight() + 3;
        cell = Math.max(9, Math.min(W / cols, (H - hud - 2) / rows));
        int gw = cell * cols, gh = cell * rows;
        int vw = W, vh = H - hud;
        if (gw <= vw) { ox = (vw - gw) / 2; camX = 0; } else {
            ox = 0;
            camX = FMathClamp(cx * cell + cell / 2 - vw / 2, 0, gw - vw);
        }
        if (gh <= vh) { oy = hud + (vh - gh) / 2; camY = 0; } else {
            oy = hud;
            camY = FMathClamp(cy * cell + cell / 2 - vh / 2, 0, gh - vh);
        }
    }

    private static int FMathClamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    protected void draw(Graphics g) {
        layout();
        g.setColor(0x1A1F26);
        g.fillRect(0, 0, W, H);
        Font nf = cell >= 14 ? Gfx.MEDIUM : Gfx.SMALL_B;
        g.setClip(0, hud, W, H - hud);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int i = y * cols + x;
                int px = ox + x * cell - camX, py = oy + y * cell - camY;
                if (px + cell < 0 || py + cell < hud || px > W || py > H) continue;
                if (st[i] == 1) {
                    g.setColor(i == boom ? 0xDD2222 : 0xC9CED6);
                    g.fillRect(px, py, cell, cell);
                    g.setColor(0x9AA2AE);
                    g.drawRect(px, py, cell - 1, cell - 1);
                    if (mine[i]) {
                        g.setColor(0x111111);
                        Gfx.disc(g, px + cell / 2, py + cell / 2, cell / 3);
                        g.setColor(0xFFFFFF);
                        g.fillRect(px + cell / 2 - 2, py + cell / 2 - 2, 2, 2);
                    } else if (adj[i] > 0) {
                        g.setFont(nf);
                        g.setColor(NUM_COLORS[adj[i]]);
                        g.drawString(String.valueOf(adj[i]), px + cell / 2 + 1, py + (cell - nf.getBaselinePosition()) / 2, Gfx.TC);
                    }
                } else {
                    Gfx.bevel(g, px, py, cell, cell, 0x6E7C8E);
                    if (st[i] == 2) {
                        boolean wrong = state == OVER && !won && !mine[i];
                        g.setColor(0x333333);
                        g.fillRect(px + cell / 2, py + 2, 1, cell - 4);
                        g.setColor(wrong ? 0xFFFF00 : 0xEE2222);
                        g.fillTriangle(px + cell / 2, py + 2, px + cell / 2, py + cell / 2, px + cell / 2 - cell / 3, py + cell / 3);
                    }
                }
            }
        }
        if (state == PLAY || state == PAUSE) {
            int px = ox + cx * cell - camX, py = oy + cy * cell - camY;
            g.setColor((clock & 4) == 0 ? 0xFFDD00 : 0xFF8800);
            g.drawRect(px, py, cell - 1, cell - 1);
            g.drawRect(px + 1, py + 1, cell - 3, cell - 3);
        }
        g.setClip(0, 0, W, H);
        g.setColor(0x0E1116);
        g.fillRect(0, 0, W, hud - 1);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFF6655);
        g.drawString("MINES " + (mines - flags), 2, 1, Gfx.TL);
        g.setColor(0xDDE4EE);
        int t = placed ? (state == PLAY ? frame - startFrame : score) : 0;
        g.drawString(Gfx.time(t, tickMs), W - 2, 1, Gfx.TR);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int c = Math.max(8, Math.min(14, h / 4));
        int n = Math.min(7, w / c);
        int x0 = x + (w - n * c) / 2, y0 = y + (h - 3 * c) / 2;
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < n; i++) {
                int k = (i * 5 + j * 3 + clock / 8) % 9;
                if (k < 4) Gfx.bevel(g, x0 + i * c, y0 + j * c, c, c, 0x6E7C8E);
                else {
                    g.setColor(0xC9CED6);
                    g.fillRect(x0 + i * c, y0 + j * c, c, c);
                    if (k < 8) {
                        g.setFont(Gfx.SMALL_B);
                        g.setColor(NUM_COLORS[k - 3]);
                        g.drawString(String.valueOf(k - 3), x0 + i * c + c / 2 + 1, y0 + j * c + (c - 7) / 2, Gfx.TC);
                    }
                }
            }
        }
    }
}
