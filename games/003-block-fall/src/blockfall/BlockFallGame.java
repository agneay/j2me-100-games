package blockfall;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/** Block Fall: falling tetromino puzzle with Marathon and Sprint-40 modes. */
public class BlockFallGame extends Game {
    private static final int BW = 10, BH = 20;

    /** 7 pieces x 4 rotations, 4x4 masks (bit 15 = top-left). */
    private static final int[] SHAPES = {
        0x0F00, 0x2222, 0x00F0, 0x4444, // I
        0x6600, 0x6600, 0x6600, 0x6600, // O
        0x4E00, 0x4640, 0x0E40, 0x4C40, // T
        0x6C00, 0x4620, 0x06C0, 0x8C40, // S
        0xC600, 0x2640, 0x0C60, 0x4C80, // Z
        0x8E00, 0x6440, 0x0E20, 0x44C0, // J
        0x2E00, 0x4460, 0x0E80, 0xC440, // L
    };
    private static final int[] COLORS = { 0x33DDEE, 0xF2D33A, 0xB65CF0, 0x55D655, 0xF0504A, 0x4A7BF0, 0xF39A2E };
    private static final int[] GRAVITY = { 16, 14, 12, 10, 8, 7, 6, 5, 4, 3, 3, 2, 2, 2, 1 };
    private static final int[] LINE_PTS = { 0, 40, 100, 300, 1200 };
    private static final int[] KICKS = { 0, -1, 1, -2, 2 };

    private final byte[] board = new byte[BW * BH]; // 0 empty, else color+1
    private final int[] bag = new int[7];
    private int bagPos = 7;
    private int piece, rot, pxp, pyp, next;
    private int fallTimer, lockTimer, lockMoves, dasTimer;
    private int lines, level;
    private int clearMask, clearTimer;
    private int cell, ox, oy, sideX;

    protected String name() { return "Block Fall"; }

    protected String[] help() {
        return new String[] {
            "Guide the falling blocks to build complete rows. Full rows vanish and score points; clearing several at once scores far more.",
            "Marathon: play as long as you can, the speed rises every 10 lines. Sprint 40: clear 40 lines as fast as possible.",
            "- Controls",
            "4/6: move  8: soft drop",
            "2 or 5: rotate right  1: rotate left",
            "0 or #: hard drop",
        };
    }

    protected String[] modes() { return new String[] { "Marathon", "Sprint 40" }; }

    protected boolean lowerIsBetter() { return mode == 1; }

    protected String formatScore(int s) { return mode == 1 ? Gfx.time(s, tickMs) : String.valueOf(s); }

    protected int accent() { return 0xB65CF0; }

    protected void newGame() {
        for (int i = 0; i < board.length; i++) board[i] = 0;
        bagPos = 7;
        lines = level = 0;
        clearMask = 0;
        next = draw();
        spawn();
    }

    private int draw() {
        if (bagPos >= 7) {
            for (int i = 0; i < 7; i++) bag[i] = i;
            Rnd.shuffle(bag);
            bagPos = 0;
        }
        return bag[bagPos++];
    }

    private void spawn() {
        piece = next;
        next = draw();
        rot = 0;
        pxp = 3;
        pyp = piece == 0 ? -1 : 0;
        fallTimer = 0;
        lockTimer = 0;
        lockMoves = 0;
        if (!fits(piece, rot, pxp, pyp)) {
            if (mode == 1) score = 0;
            endGame(false);
        }
    }

    private boolean fits(int p, int r, int x, int y) {
        int m = SHAPES[p * 4 + r];
        for (int i = 0; i < 16; i++) {
            if ((m & (0x8000 >> i)) == 0) continue;
            int cx = x + (i & 3), cy = y + (i >> 2);
            if (cx < 0 || cx >= BW || cy >= BH) return false;
            if (cy >= 0 && board[cy * BW + cx] != 0) return false;
        }
        return true;
    }

    private void rotate(int dir) {
        int nr = (rot + dir) & 3;
        for (int k = 0; k < KICKS.length; k++) {
            if (fits(piece, nr, pxp + KICKS[k], pyp)) {
                rot = nr;
                pxp += KICKS[k];
                touchLock();
                Sfx.click();
                return;
            }
        }
        if (fits(piece, nr, pxp, pyp - 1)) {
            rot = nr;
            pyp--;
            touchLock();
        }
    }

    private void touchLock() {
        if (lockTimer > 0 && lockMoves < 12) {
            lockTimer = 0;
            lockMoves++;
        }
    }

    private void shift(int dx) {
        if (fits(piece, rot, pxp + dx, pyp)) {
            pxp += dx;
            touchLock();
        }
    }

    protected void update() {
        if (mode == 1) score = frame;
        if (clearTimer > 0) {
            if (--clearTimer == 0) collapse();
            return;
        }
        int dx = 0;
        if ((tapped & K_LEFT) != 0) { dx = -1; dasTimer = 0; }
        else if ((tapped & K_RIGHT) != 0) { dx = 1; dasTimer = 0; }
        else if ((held & (K_LEFT | K_RIGHT)) != 0) {
            dasTimer++;
            if (dasTimer > 4 && (dasTimer & 1) == 0) dx = (held & K_LEFT) != 0 ? -1 : 1;
        } else {
            dasTimer = 0;
        }
        if (dx != 0) shift(dx);
        if ((tapped & (K_UP | K_FIRE | (K_NUM0 << 3))) != 0) rotate(1);
        if ((tapped & (K_NUM0 << 1)) != 0) rotate(3);
        if ((tapped & (K_NUM0 | K_POUND)) != 0) {
            int d = 0;
            while (fits(piece, rot, pxp, pyp + 1)) { pyp++; d++; }
            if (mode == 0) score += d * 2;
            lock();
            return;
        }
        int g = GRAVITY[Math.min(level, GRAVITY.length - 1)];
        boolean soft = (held & K_DOWN) != 0;
        if (soft) g = 1;
        if (fits(piece, rot, pxp, pyp + 1)) {
            lockTimer = 0;
            if (++fallTimer >= g) {
                fallTimer = 0;
                pyp++;
                if (soft && mode == 0) score++;
            }
        } else if (++lockTimer > 10) {
            lock();
        }
    }

    private void lock() {
        int m = SHAPES[piece * 4 + rot];
        boolean above = false;
        for (int i = 0; i < 16; i++) {
            if ((m & (0x8000 >> i)) == 0) continue;
            int cx = pxp + (i & 3), cy = pyp + (i >> 2);
            if (cy < 0) above = true;
            else board[cy * BW + cx] = (byte) (piece + 1);
        }
        Sfx.hit();
        if (above) {
            if (mode == 1) score = 0;
            endGame(false);
            return;
        }
        clearMask = 0;
        int n = 0;
        for (int y = 0; y < BH; y++) {
            boolean full = true;
            for (int x = 0; x < BW && full; x++) if (board[y * BW + x] == 0) full = false;
            if (full) {
                clearMask |= 1 << y;
                n++;
            }
        }
        if (n > 0) {
            if (mode == 0) score += LINE_PTS[n] * (level + 1);
            lines += n;
            clearTimer = 6;
            if (n == 4) Sfx.win(); else Sfx.good();
        } else {
            spawn();
        }
    }

    private void collapse() {
        int dst = BH - 1;
        for (int y = BH - 1; y >= 0; y--) {
            if ((clearMask & (1 << y)) != 0) continue;
            if (dst != y) System.arraycopy(board, y * BW, board, dst * BW, BW);
            dst--;
        }
        for (int y = dst; y >= 0; y--) for (int x = 0; x < BW; x++) board[y * BW + x] = 0;
        clearMask = 0;
        level = lines / 10;
        if (mode == 1 && lines >= 40) {
            score = frame;
            endGame(true);
            return;
        }
        spawn();
    }

    private void layout() {
        cell = Math.max(4, Math.min((H - 2) / BH, (W - 4) / (BW + 5)));
        int bw = cell * BW;
        ox = Math.max(1, (W - bw - cell * 5) / 2);
        oy = (H - cell * BH) / 2;
        sideX = ox + bw + Math.max(3, cell / 2);
    }

    private void block(Graphics g, int x, int y, int c, int s) {
        Gfx.bevel(g, x, y, s, s, c);
    }

    protected void draw(Graphics g) {
        layout();
        g.setColor(0x0C0A18);
        g.fillRect(0, 0, W, H);
        g.setColor(0x16122A);
        g.fillRect(ox, oy, cell * BW, cell * BH);
        g.setColor(0x1E1938);
        for (int x = 1; x < BW; x++) g.drawLine(ox + x * cell, oy, ox + x * cell, oy + cell * BH - 1);
        g.setColor(0x6C5CA8);
        g.drawRect(ox - 1, oy - 1, cell * BW + 1, cell * BH + 1);
        for (int y = 0; y < BH; y++) {
            boolean clearing = (clearMask & (1 << y)) != 0;
            for (int x = 0; x < BW; x++) {
                int v = board[y * BW + x];
                if (v == 0) continue;
                int c = clearing ? ((clearTimer & 1) == 0 ? 0xFFFFFF : COLORS[v - 1]) : COLORS[v - 1];
                block(g, ox + x * cell, oy + y * cell, c, cell);
            }
        }
        if (clearTimer == 0 && state != OVER) {
            int gy = pyp;
            while (fits(piece, rot, pxp, gy + 1)) gy++;
            int m = SHAPES[piece * 4 + rot];
            g.setColor(Gfx.shade(COLORS[piece], -55));
            for (int i = 0; i < 16; i++) {
                if ((m & (0x8000 >> i)) == 0) continue;
                int cy = gy + (i >> 2);
                if (cy >= 0) g.drawRect(ox + (pxp + (i & 3)) * cell, oy + cy * cell, cell - 1, cell - 1);
            }
            for (int i = 0; i < 16; i++) {
                if ((m & (0x8000 >> i)) == 0) continue;
                int cy = pyp + (i >> 2);
                if (cy >= 0) block(g, ox + (pxp + (i & 3)) * cell, oy + cy * cell, COLORS[piece], cell);
            }
        }
        // sidebar
        Font f = Gfx.SMALL;
        int lh = f.getHeight();
        int y = oy;
        Gfx.text(g, "NEXT", sideX, y, Gfx.TL, f, 0x9F90D8);
        y += lh + 2;
        int ns = Math.max(3, cell * 3 / 4);
        int m = SHAPES[next * 4];
        for (int i = 0; i < 16; i++) {
            if ((m & (0x8000 >> i)) != 0) block(g, sideX + (i & 3) * ns, y + (i >> 2) * ns, COLORS[next], ns);
        }
        y += ns * 3 + 4;
        String[] labels = { mode == 1 ? "TIME" : "SCORE", "LINES", "LEVEL" };
        String[] vals = { mode == 1 ? Gfx.time(frame, tickMs) : String.valueOf(score),
                mode == 1 ? lines + "/40" : String.valueOf(lines), String.valueOf(level + 1) };
        for (int i = 0; i < 3; i++) {
            Gfx.text(g, labels[i], sideX, y, Gfx.TL, f, 0x9F90D8);
            Gfx.text(g, vals[i], sideX, y + lh, Gfx.TL, Gfx.SMALL_B, 0xFFFFFF);
            y += lh * 2 + 3;
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(5, Math.min(12, h / 5));
        int cols = Math.min(10, w / s);
        int x0 = x + (w - cols * s) / 2;
        int fall = (clock / 2) % (h / s + 4);
        for (int i = 0; i < cols; i++) {
            int stack = 1 + ((i * 7 + 3) % 3);
            for (int j = 0; j < stack; j++) block(g, x0 + i * s, y + h - (j + 1) * s, COLORS[(i + j) % 7], s);
        }
        int m = SHAPES[8];
        for (int i = 0; i < 16; i++) {
            int yy = y + (fall - 3 + (i >> 2)) * s;
            if ((m & (0x8000 >> i)) != 0 && yy >= y && yy < y + h - 3 * s) block(g, x0 + (3 + (i & 3)) * s, yy, COLORS[2], s);
        }
    }
}
