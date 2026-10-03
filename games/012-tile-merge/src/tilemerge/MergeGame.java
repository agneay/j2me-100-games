package tilemerge;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/** Tile Merge: slide numbered tiles; equal tiles merge into their sum. */
public class MergeGame extends Game {
    private static final int[] SIZES = { 4, 3, 5 };
    private static final int[] TARGET = { 11, 8, 12 }; // 2048, 256, 4096 as powers of two
    private static final int[] COLORS = { 0xCDC1B4, 0xEEE4DA, 0xEDE0C8, 0xF2B179, 0xF59563, 0xF67C5F, 0xF65E3B,
            0xEDCF72, 0xEDCC61, 0xEDC850, 0xEDC53F, 0xEDC22E, 0x3C3A32, 0x2E2C26, 0x22201C };

    private int n;
    private int[] grid, prev;
    private final int[] snapshot = new int[25];
    private int prevScore;
    private boolean canUndo;
    // animation
    private final int[] mvFrom = new int[25], mvTo = new int[25], mvVal = new int[25];
    private int moves, anim;
    private final boolean[] pop = new boolean[25];
    private int spawned = -1;

    protected String name() { return "Tile Merge"; }

    protected String[] help() {
        return new String[] {
            "Slide all tiles in one direction. Two tiles with the same number that touch merge into one with their sum.",
            "A new 2 (sometimes a 4) appears after every move. Build the target tile: 2048 on the classic 4x4 board.",
            "Made a mistake? Press 0 to take back your last move.",
            "- Controls",
            "2/4/6/8: slide up / left / right / down",
            "0: undo last move",
        };
    }

    protected String[] modes() { return new String[] { "4x4 to 2048", "3x3 to 256", "5x5 to 4096" }; }

    protected int accent() { return 0xEDC22E; }

    protected void newGame() {
        n = SIZES[mode];
        grid = new int[n * n];
        prev = new int[n * n];
        canUndo = false;
        anim = 0;
        for (int i = 0; i < 25; i++) pop[i] = false;
        spawn();
        spawn();
    }

    private void spawn() {
        int free = 0;
        for (int i = 0; i < n * n; i++) if (grid[i] == 0) free++;
        if (free == 0) return;
        int k = Rnd.nextInt(free);
        for (int i = 0; i < n * n; i++) {
            if (grid[i] == 0 && k-- == 0) {
                grid[i] = Rnd.chance(90) ? 1 : 2;
                spawned = i;
                return;
            }
        }
    }

    /** Slide in direction (dx, dy). Returns true if anything moved. */
    private boolean slide(int dx, int dy) {
        moves = 0;
        for (int i = 0; i < n * n; i++) pop[i] = false;
        boolean moved = false;
        int gained = 0;
        for (int line = 0; line < n; line++) {
            // walk cells from the destination edge backwards
            int target = -1, targetVal = 0;
            boolean targetMerged = false;
            for (int k = 0; k < n; k++) {
                int x, y;
                if (dx != 0) { x = dx > 0 ? n - 1 - k : k; y = line; } else { x = line; y = dy > 0 ? n - 1 - k : k; }
                int i = y * n + x;
                int v = grid[i];
                if (v == 0) continue;
                int destPos;
                if (target >= 0 && targetVal == v && !targetMerged) {
                    destPos = target;
                    grid[i] = 0;
                    grid[destPos] = v + 1;
                    targetVal = v + 1;
                    targetMerged = true;
                    pop[destPos] = true;
                    gained += 1 << (v + 1);
                } else {
                    int next = target < 0 ? 0 : posIndex(target, dx, dy) + 1;
                    int tx, ty;
                    if (dx != 0) { tx = dx > 0 ? n - 1 - next : next; ty = line; } else { tx = line; ty = dy > 0 ? n - 1 - next : next; }
                    destPos = ty * n + tx;
                    grid[i] = 0;
                    grid[destPos] = v;
                    target = destPos;
                    targetVal = v;
                    targetMerged = false;
                }
                if (destPos != i) moved = true;
                mvFrom[moves] = i;
                mvTo[moves] = destPos;
                mvVal[moves] = v;
                moves++;
            }
        }
        score += gained;
        if (gained >= 128) Sfx.good(); else if (gained > 0) Sfx.tone(76, 20);
        return moved;
    }

    /** Position of cell index along the slide direction (0 = destination edge). */
    private int posIndex(int idx, int dx, int dy) {
        int x = idx % n, y = idx / n;
        if (dx > 0) return n - 1 - x;
        if (dx < 0) return x;
        if (dy > 0) return n - 1 - y;
        return y;
    }

    private boolean canMove() {
        for (int i = 0; i < n * n; i++) {
            if (grid[i] == 0) return true;
            int x = i % n, y = i / n;
            if (x + 1 < n && grid[i + 1] == grid[i]) return true;
            if (y + 1 < n && grid[i + n] == grid[i]) return true;
        }
        return false;
    }

    protected void update() {
        if (anim > 0) {
            anim--;
            return;
        }
        if (digit(0) && canUndo) {
            System.arraycopy(prev, 0, grid, 0, n * n);
            score = prevScore;
            canUndo = false;
            moves = 0;
            spawned = -1;
            Sfx.click();
            return;
        }
        int dx = 0, dy = 0;
        if ((pressed & K_LEFT) != 0) dx = -1;
        else if ((pressed & K_RIGHT) != 0) dx = 1;
        else if ((pressed & K_UP) != 0) dy = -1;
        else if ((pressed & K_DOWN) != 0) dy = 1;
        if (dx == 0 && dy == 0) return;
        System.arraycopy(grid, 0, snapshot, 0, n * n);
        int snapScore = score;
        if (slide(dx, dy)) {
            System.arraycopy(snapshot, 0, prev, 0, n * n);
            prevScore = snapScore;
            canUndo = true;
            spawn();
            anim = 3;
            for (int i = 0; i < n * n; i++) {
                if (grid[i] >= TARGET[mode]) {
                    endGame(true);
                    return;
                }
            }
            if (!canMove()) endGame(false);
        } else {
            moves = 0;
        }
    }

    private void tile(Graphics g, int x, int y, int s, int v, int grow) {
        int c = COLORS[Math.min(v, COLORS.length - 1)];
        int g2 = grow;
        g.setColor(c);
        g.fillRoundRect(x - g2, y - g2, s + g2 * 2, s + g2 * 2, s / 5, s / 5);
        if (v == 0) return;
        String t = String.valueOf(1 << v);
        Font f = Gfx.LARGE.stringWidth(t) <= s - 4 ? Gfx.LARGE : (Gfx.MEDIUM.stringWidth(t) <= s - 3 ? Gfx.MEDIUM : Gfx.SMALL_B);
        g.setFont(f);
        g.setColor(v <= 2 ? 0x776E65 : 0xF9F6F2);
        g.drawString(t, x + s / 2 + 1, y + (s - f.getBaselinePosition()) / 2, Gfx.TC);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 4;
        int size = Math.min(W - 6, H - hud * 2);
        int gap = Math.max(2, size / (n * 12));
        int s = (size - gap * (n + 1)) / n;
        size = s * n + gap * (n + 1);
        int ox = (W - size) / 2, oy = hud + (H - hud * 2 - size) / 2;
        g.setColor(0xFAF8EF);
        g.fillRect(0, 0, W, H);
        g.setColor(0xBBADA0);
        g.fillRoundRect(ox, oy, size, size, 6, 6);
        for (int i = 0; i < n * n; i++) tile(g, ox + gap + (i % n) * (s + gap), oy + gap + (i / n) * (s + gap), s, 0, 0);
        if (anim > 0) {
            int t = 3 - anim; // 0..2
            for (int k = 0; k < moves; k++) {
                int fx = mvFrom[k] % n, fy = mvFrom[k] / n, tx = mvTo[k] % n, ty = mvTo[k] / n;
                int x = fx * (s + gap) + (tx - fx) * (s + gap) * (t + 1) / 3;
                int y = fy * (s + gap) + (ty - fy) * (s + gap) * (t + 1) / 3;
                tile(g, ox + gap + x, oy + gap + y, s, mvVal[k], 0);
            }
        } else {
            for (int i = 0; i < n * n; i++) {
                if (grid[i] == 0) continue;
                int grow = (pop[i] && clock % 4 < 2) ? Math.max(1, gap / 2) : 0;
                tile(g, ox + gap + (i % n) * (s + gap), oy + gap + (i / n) * (s + gap), s, grid[i], grow);
            }
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x776E65);
        g.drawString("SCORE " + score, 3, 2, Gfx.TL);
        g.drawString("BEST " + Math.max(best(), score), W - 3, 2, Gfx.TR);
        g.setFont(Gfx.SMALL);
        g.setColor(0x9E948A);
        g.drawString(canUndo ? "0: undo" : "2/4/6/8 slide", W / 2, H - hud + 3, Gfx.TC);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(10, Math.min(h - 4, w / 5));
        int vals[] = { 1, 2, 3, 11 };
        int x0 = x + (w - 4 * (s + 3)) / 2;
        for (int i = 0; i < 4; i++) {
            int bob = ((clock / 6 + i) % 4 == 0) ? 2 : 0;
            tile(g, x0 + i * (s + 3), y + (h - s) / 2 - bob, s, vals[i], 0);
        }
    }
}
