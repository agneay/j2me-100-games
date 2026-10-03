package lighttrails;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Light Trails: light-cycle duels. Every bike leaves a solid wall behind it;
 * the last bike riding wins the round. First to three rounds.
 */
public class TrailsGame extends Game {
    private static final int[] DX = { 0, 1, 0, -1 }, DY = { -1, 0, 1, 0 };
    private static final int[] COL = { 0x00E5FF, 0xFF4081, 0xFFEA00, 0x76FF03 };

    private int cols, rows, cell, ox, oy;
    private byte[] grid;
    private int[] queue;
    private final int[] bx = new int[4], by = new int[4], bd = new int[4], wins = new int[4];
    private final boolean[] alive = new boolean[4];
    private int bikes, round, pending = -1, boost = 60, roundWait, moveTick;
    private String roundMsg = "";
    private boolean[] seen;

    protected String name() { return "Light Trails"; }

    protected String[] help() {
        return new String[] {
            "Ride your light cycle around the arena. Every bike leaves a solid wall of light behind it. Crash into any wall and you're out of the round.",
            "Be the last rider left to win the round. First to three round wins takes the match.",
            "Hold 5 to boost while your boost bar lasts; it recharges slowly.",
            "- Controls",
            "2/4/6/8: turn",
            "5 (hold): boost",
        };
    }

    protected String[] modes() { return new String[] { "1 rival", "3 rivals" }; }

    protected int accent() { return 0x00E5FF; }

    protected void newGame() {
        bikes = mode == 0 ? 2 : 4;
        for (int i = 0; i < 4; i++) wins[i] = 0;
        round = 0;
        startRound();
    }

    private void startRound() {
        int hud = Gfx.SMALL.getHeight() + 2;
        cell = Math.max(3, W / 44);
        cols = (W - 2) / cell;
        rows = (H - hud - 2) / cell;
        ox = (W - cols * cell) / 2;
        oy = hud + (H - hud - rows * cell) / 2;
        grid = new byte[cols * rows];
        queue = new int[cols * rows];
        seen = new boolean[cols * rows];
        int[][] starts = { { cols / 4, rows / 2, 1 }, { cols * 3 / 4, rows / 2, 3 }, { cols / 2, rows / 5, 2 }, { cols / 2, rows * 4 / 5, 0 } };
        for (int i = 0; i < bikes; i++) {
            bx[i] = starts[i][0];
            by[i] = starts[i][1];
            bd[i] = starts[i][2];
            alive[i] = true;
            grid[by[i] * cols + bx[i]] = (byte) (i + 1);
        }
        pending = -1;
        boost = 60;
        roundWait = 30;
        roundMsg = "Round " + (round + 1);
    }

    private boolean free(int x, int y) {
        return x >= 0 && y >= 0 && x < cols && y < rows && grid[y * cols + x] == 0;
    }

    /** Free cells reachable from (x, y), capped for speed. */
    private int space(int x, int y, int cap) {
        if (!free(x, y)) return 0;
        for (int i = 0; i < seen.length; i++) seen[i] = false;
        int head = 0, tail = 0, n = 0;
        queue[tail++] = y * cols + x;
        seen[y * cols + x] = true;
        while (head < tail && n < cap) {
            int c = queue[head++];
            n++;
            int cx = c % cols, cy = c / cols;
            for (int d = 0; d < 4; d++) {
                int nx = cx + DX[d], ny = cy + DY[d];
                if (!free(nx, ny)) continue;
                int j = ny * cols + nx;
                if (seen[j]) continue;
                seen[j] = true;
                queue[tail++] = j;
            }
        }
        return n;
    }

    private void steerAI(int i) {
        int best = -1, bestDir = bd[i];
        for (int k = -1; k <= 1; k++) {
            int d = (bd[i] + k + 4) & 3;
            int nx = bx[i] + DX[d], ny = by[i] + DY[d];
            int s = space(nx, ny, 160);
            if (s > 0) {
                // prefer going straight, avoid hugging, add a touch of randomness
                s = s * 4 + (k == 0 ? 6 : 0) + Rnd.nextInt(4);
            }
            if (s > best) {
                best = s;
                bestDir = d;
            }
        }
        bd[i] = bestDir;
    }

    protected void update() {
        if (roundWait > 0) {
            roundWait--;
            return;
        }
        int want = -1;
        if ((pressed & K_UP) != 0) want = 0;
        else if ((pressed & K_RIGHT) != 0) want = 1;
        else if ((pressed & K_DOWN) != 0) want = 2;
        else if ((pressed & K_LEFT) != 0) want = 3;
        if (want >= 0 && want != ((bd[0] + 2) & 3)) pending = want;
        boolean boosting = (held & K_FIRE) != 0 && boost > 0;
        if (boosting) boost -= 2;
        else if (boost < 60 && (frame & 3) == 0) boost++;
        moveTick++;
        int steps = boosting ? 2 : 1;
        // everyone moves every other tick; the player can double up when boosting
        for (int s = 0; s < steps; s++) {
            if ((moveTick & 1) == 0 || s > 0) stepAll(s > 0);
            if (state != PLAY) return;
        }
    }

    private void stepAll(boolean playerOnly) {
        if (pending >= 0) {
            bd[0] = pending;
            pending = -1;
        }
        for (int i = 0; i < bikes; i++) {
            if (!alive[i]) continue;
            if (playerOnly && i != 0) continue;
            if (i > 0) steerAI(i);
            int nx = bx[i] + DX[bd[i]], ny = by[i] + DY[bd[i]];
            if (!free(nx, ny)) {
                alive[i] = false;
                Sfx.tone(i == 0 ? 40 : 55, 80);
                continue;
            }
            bx[i] = nx;
            by[i] = ny;
            grid[ny * cols + nx] = (byte) (i + 1);
        }
        // head-on: two bikes on the same cell both crash
        for (int i = 0; i < bikes; i++) for (int j = i + 1; j < bikes; j++) {
            if (alive[i] && alive[j] && bx[i] == bx[j] && by[i] == by[j]) alive[i] = alive[j] = false;
        }
        int left = 0, last = -1;
        for (int i = 0; i < bikes; i++) if (alive[i]) { left++; last = i; }
        if (!alive[0] || left <= 1) endRound(left == 1 ? last : -1);
        else score += 1;
    }

    private void endRound(int winner) {
        round++;
        if (winner >= 0) wins[winner]++;
        if (winner == 0) {
            score += 100;
            roundMsg = "You win the round!";
            Sfx.good();
        } else {
            roundMsg = winner < 0 ? "Draw!" : "Rival wins the round";
            Sfx.bad();
        }
        for (int i = 0; i < bikes; i++) {
            if (wins[i] >= 3) {
                headline = i == 0 ? "CHAMPION!" : "DEFEATED";
                if (i == 0) score += 500;
                endGame(i == 0);
                return;
            }
        }
        startRound();
        roundWait = 40;
    }

    protected void draw(Graphics g) {
        g.setColor(0x020612);
        g.fillRect(0, 0, W, H);
        g.setColor(0x0B1A33);
        for (int x = 0; x <= cols; x += 4) g.drawLine(ox + x * cell, oy, ox + x * cell, oy + rows * cell);
        for (int y = 0; y <= rows; y += 4) g.drawLine(ox, oy + y * cell, ox + cols * cell, oy + y * cell);
        for (int i = 0; i < grid.length; i++) {
            int v = grid[i];
            if (v == 0) continue;
            g.setColor(alive[v - 1] ? COL[v - 1] : Gfx.shade(COL[v - 1], -55));
            g.fillRect(ox + (i % cols) * cell, oy + (i / cols) * cell, cell, cell);
        }
        for (int i = 0; i < bikes; i++) {
            if (!alive[i]) continue;
            g.setColor(0xFFFFFF);
            g.fillRect(ox + bx[i] * cell, oy + by[i] * cell, cell, cell);
        }
        g.setColor(0x1E88E5);
        g.drawRect(ox - 1, oy - 1, cols * cell + 1, rows * cell + 1);
        g.setFont(Gfx.SMALL_B);
        for (int i = 0; i < bikes; i++) {
            g.setColor(COL[i]);
            g.drawString(String.valueOf(wins[i]), 2 + i * 14, 1, Gfx.TL);
        }
        Gfx.bar(g, W - W / 4 - 2, 3, W / 4, Gfx.SMALL.getHeight() - 4, boost, 60, 0x00E5FF, 0x102040);
        if (roundWait > 0) Gfx.shadowText(g, roundMsg, W / 2, H / 2 - 6, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int t = clock % 80;
        g.setColor(0x00E5FF);
        g.fillRect(x + 4, y + h / 3, Math.min(t, 40) * w / 80, 2);
        if (t > 40) g.fillRect(x + 4 + w / 2, y + h / 3, 2, (t - 40) * h / 80);
        g.setColor(0xFF4081);
        g.fillRect(x + w - 4 - Math.min(t, 40) * w / 80, y + h * 2 / 3, Math.min(t, 40) * w / 80, 2);
    }
}
