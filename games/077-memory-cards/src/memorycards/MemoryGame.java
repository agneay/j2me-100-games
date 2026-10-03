package memorycards;

import gamekit.Cards;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Memory Cards: turn over two cards at a time and find matching pairs.
 * Solo against the clock, or a duel with a CPU whose memory is imperfect.
 */
public class MemoryGame extends Game {
    private int cols, rows, n;
    private int[] card;
    private boolean[] up, gone;
    private boolean[] seen; // what the CPU remembers
    private int cur, first = -1, second = -1, showT, turn, mine, theirs, flips, wait;

    protected String name() { return "Memory Cards"; }

    protected String[] help() {
        return new String[] {
            "All cards start face down. Turn over two: if they match, you keep the pair; if not, they flip back - remember where they were!",
            "Solo: clear the board in as few turns as possible. Duel: take turns with the CPU; a match earns another go. The CPU remembers some of what it sees...",
            "- Controls",
            "2/4/6/8: move  5: turn card",
        };
    }

    protected String[] modes() { return new String[] { "Solo 4x4", "Solo 5x6", "Duel vs CPU" }; }

    protected boolean lowerIsBetter() { return mode < 2; }

    protected String formatScore(int s) { return mode < 2 ? s + " turns" : s + " pairs"; }

    protected int accent() { return 0x43AA8B; }

    protected void newGame() {
        cols = mode == 0 ? 4 : 5;
        rows = mode == 0 ? 4 : (mode == 1 ? 6 : 4);
        n = cols * rows;
        card = new int[n];
        up = new boolean[n];
        gone = new boolean[n];
        seen = new boolean[n];
        int[] pool = new int[52];
        Cards.shuffle(pool);
        for (int i = 0; i < n / 2; i++) {
            card[i * 2] = pool[i];
            card[i * 2 + 1] = pool[i];
        }
        for (int i = n - 1; i > 0; i--) {
            int j = Rnd.nextInt(i + 1);
            int t = card[i];
            card[i] = card[j];
            card[j] = t;
        }
        cur = 0;
        first = second = -1;
        turn = 0;
        mine = theirs = flips = 0;
    }

    private void flip(int i) {
        if (gone[i] || up[i]) return;
        up[i] = true;
        if (mode == 2 && Rnd.chance(70)) seen[i] = true;
        Sfx.click();
        if (first < 0) first = i;
        else {
            second = i;
            showT = 20;
            flips++;
        }
    }

    private void resolve() {
        boolean match = card[first] == card[second];
        if (match) {
            gone[first] = gone[second] = true;
            if (turn == 0) mine++; else theirs++;
            Sfx.good();
        } else {
            up[first] = up[second] = false;
            if (mode == 2) turn = 1 - turn;
        }
        first = second = -1;
        int left = 0;
        for (int i = 0; i < n; i++) if (!gone[i]) left++;
        if (left == 0) {
            if (mode < 2) {
                score = flips;
                endGame(true);
            } else {
                score = mine;
                headline = mine > theirs ? "YOU WIN " + mine + "-" + theirs : (mine == theirs ? "DRAW" : "CPU WINS " + theirs + "-" + mine);
                endGame(mine > theirs);
            }
        }
    }

    private int cpuPick() {
        // known pair?
        if (first < 0) {
            for (int a = 0; a < n; a++) {
                if (gone[a] || !seen[a]) continue;
                for (int b = a + 1; b < n; b++) if (!gone[b] && seen[b] && card[a] == card[b]) return a;
            }
        } else {
            for (int b = 0; b < n; b++) if (b != first && !gone[b] && seen[b] && card[b] == card[first]) return b;
        }
        int pick;
        int tries = 0;
        do {
            pick = Rnd.nextInt(n);
        } while ((gone[pick] || up[pick] || (seen[pick] && tries++ < 30)));
        return pick;
    }

    protected void update() {
        if (showT > 0) {
            if (--showT == 0) resolve();
            return;
        }
        if (mode == 2 && turn == 1) {
            if (++wait < 14) return;
            wait = 0;
            int p = cpuPick();
            cur = p;
            flip(p);
            return;
        }
        int x = cur % cols, y = cur / cols;
        if ((pressed & K_LEFT) != 0) x = (x + cols - 1) % cols;
        if ((pressed & K_RIGHT) != 0) x = (x + 1) % cols;
        if ((pressed & K_UP) != 0) y = (y + rows - 1) % rows;
        if ((pressed & K_DOWN) != 0) y = (y + 1) % rows;
        cur = y * cols + x;
        if ((pressed & K_FIRE) != 0) {
            if (gone[cur] || up[cur]) Sfx.bad();
            else flip(cur);
        }
    }

    protected void draw(Graphics g) {
        Cards.felt(g, W, H);
        int hud = Gfx.SMALL.getHeight() + 2;
        int cw = Math.min((W - 4) / cols - 2, ((H - hud * 2) / rows - 2) * 3 / 4);
        int ch = Cards.heightFor(cw);
        int ox = (W - cols * (cw + 2)) / 2, oy = hud + (H - hud * 2 - rows * (ch + 2)) / 2;
        for (int i = 0; i < n; i++) {
            int x = ox + (i % cols) * (cw + 2), y = oy + (i / cols) * (ch + 2);
            if (gone[i]) {
                Cards.drawSlot(g, x, y, cw, ch);
                continue;
            }
            Cards.draw(g, card[i], x, y, cw, ch, up[i]);
            if (i == cur && showT == 0 && !(mode == 2 && turn == 1)) {
                g.setColor((clock & 4) == 0 ? 0xFFEB3B : 0xFFFFFF);
                g.drawRect(x - 1, y - 1, cw + 1, ch + 1);
            }
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        if (mode < 2) {
            g.drawString("Turns " + flips, 2, 1, Gfx.TL);
            g.drawString(Gfx.time(frame, tickMs), W - 2, 1, Gfx.TR);
        } else {
            g.setColor(turn == 0 ? 0xFFEB3B : 0xFFFFFF);
            g.drawString("You " + mine, 2, 1, Gfx.TL);
            g.setColor(turn == 1 ? 0xFFEB3B : 0xFFFFFF);
            g.drawString("CPU " + theirs, W - 2, 1, Gfx.TR);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int cw = Math.max(12, Math.min(w / 6, h * 3 / 5));
        int ch = Cards.heightFor(cw);
        int x0 = x + (w - cw * 4 - 6) / 2;
        int k = (clock / 15) % 4;
        for (int i = 0; i < 4; i++) Cards.draw(g, i < 2 ? 26 : 38, x0 + i * (cw + 2), y + (h - ch) / 2, cw, ch, i == k || i == (k + 2) % 4);
    }
}
