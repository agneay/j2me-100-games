package galaxyconquest;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Galaxy Conquest: a real-time planet-capture strategy game. Owned planets
 * build ships; send half a planet's fleet to reinforce or invade another.
 */
public class GalaxyGame extends Game {
    private static final int MAXP = 12, MAXF = 40;
    private static final int[] OWNER_COL = { 0x90A4AE, 0x42A5F5, 0xEF5350 };

    private final int[] px = new int[MAXP], py = new int[MAXP], pr = new int[MAXP], owner = new int[MAXP], ships = new int[MAXP], prod = new int[MAXP];
    private int np;
    private final int[] fx = new int[MAXF], fy = new int[MAXF], ftx = new int[MAXF], fn = new int[MAXF], fo = new int[MAXF], ft = new int[MAXF];
    private final boolean[] fon = new boolean[MAXF];
    private int cur, src = -1, aiT, top;

    protected String name() { return "Galaxy Conquest"; }

    protected String[] help() {
        return new String[] {
            "Conquer the galaxy in real time. Your planets (blue) build ships; bigger planets build faster.",
            "Move the cursor to one of your planets and press 5, then move to a target and press 5 again: half its ships fly there. Ships landing on an enemy or neutral planet fight its defenders; if more arrive than defend, the planet is yours.",
            "Wipe out every red planet and fleet to win.",
            "- Controls",
            "2/4/6/8: jump between planets",
            "5: choose source, then target",
            "0: cancel",
        };
    }

    protected String[] modes() { return new String[] { "Cadet", "Admiral" }; }

    protected int accent() { return 0x42A5F5; }

    protected void newGame() {
        top = Gfx.SMALL.getHeight() + 6;
        np = W >= 170 ? 12 : 10;
        int minR = Math.max(5, W / 28);
        int placed = 0, tries = 0;
        while (placed < np && tries < 500) {
            tries++;
            int r = minR + Rnd.nextInt(minR);
            int x = Rnd.range(r + 4, W - r - 4), y = Rnd.range(top + r + 2, H - r - 4);
            boolean ok = true;
            for (int k = 0; k < placed; k++) {
                int dx = px[k] - x, dy = py[k] - y;
                if (dx * dx + dy * dy < (pr[k] + r + 8) * (pr[k] + r + 8)) ok = false;
            }
            if (!ok) continue;
            px[placed] = x;
            py[placed] = y;
            pr[placed] = r;
            owner[placed] = 0;
            ships[placed] = 5 + Rnd.nextInt(15);
            prod[placed] = r;
            placed++;
        }
        np = placed;
        // home planets: leftmost for the player, rightmost for the CPU
        int me = 0, them = 0;
        for (int i = 0; i < np; i++) {
            if (px[i] < px[me]) me = i;
            if (px[i] > px[them]) them = i;
        }
        owner[me] = 1;
        ships[me] = 30;
        owner[them] = 2;
        ships[them] = mode == 0 ? 25 : 40;
        cur = me;
        src = -1;
        for (int i = 0; i < MAXF; i++) fon[i] = false;
        aiT = 60;
    }

    private void send(int from, int to, int who) {
        if (from == to || ships[from] < 2) return;
        int n = ships[from] / 2;
        for (int i = 0; i < MAXF; i++) {
            if (fon[i]) continue;
            fon[i] = true;
            ships[from] -= n;
            fn[i] = n;
            fo[i] = who;
            ftx[i] = to;
            fx[i] = px[from] << 8;
            fy[i] = py[from] << 8;
            ft[i] = from;
            Sfx.tone(who == 1 ? 72 : 55, 25);
            return;
        }
    }

    private int nearestInDir(int from, int dx, int dy) {
        int best = -1, bestD = 1 << 30;
        for (int i = 0; i < np; i++) {
            if (i == from) continue;
            int vx = px[i] - px[from], vy = py[i] - py[from];
            int along = vx * dx + vy * dy;
            int across = Math.abs(vx * dy - vy * dx);
            if (along <= 0 || across > along * 2) continue;
            int d = vx * vx + vy * vy + across * across;
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best < 0 ? from : best;
    }

    protected void update() {
        // production
        for (int i = 0; i < np; i++) {
            if (owner[i] != 0 && frame % Math.max(6, 40 - prod[i] * 2) == 0 && ships[i] < 999) ships[i]++;
        }
        // fleets
        for (int i = 0; i < MAXF; i++) {
            if (!fon[i]) continue;
            int t = ftx[i];
            int dx = (px[t] << 8) - fx[i], dy = (py[t] << 8) - fy[i];
            int d = FMath.dist(dx >> 8, dy >> 8);
            int sp = Math.max(1, W / 90);
            if (d <= sp + pr[t] / 2) {
                fon[i] = false;
                arrive(t, fo[i], fn[i]);
            } else {
                fx[i] += (dx / Math.max(1, d)) * sp;
                fy[i] += (dy / Math.max(1, d)) * sp;
            }
        }
        // input
        int dx = 0, dy = 0;
        if ((pressed & K_LEFT) != 0) dx = -1;
        else if ((pressed & K_RIGHT) != 0) dx = 1;
        else if ((pressed & K_UP) != 0) dy = -1;
        else if ((pressed & K_DOWN) != 0) dy = 1;
        if (dx != 0 || dy != 0) {
            cur = nearestInDir(cur, dx, dy);
            Sfx.click();
        }
        if (digit(0)) src = -1;
        if ((pressed & K_FIRE) != 0) {
            if (src < 0) {
                if (owner[cur] == 1) src = cur;
                else Sfx.bad();
            } else {
                if (cur != src) send(src, cur, 1);
                src = -1;
            }
        }
        ai();
        // victory check
        int mine = 0, theirs = 0;
        for (int i = 0; i < np; i++) {
            if (owner[i] == 1) mine++;
            if (owner[i] == 2) theirs++;
        }
        for (int i = 0; i < MAXF; i++) if (fon[i]) { if (fo[i] == 1) mine++; else theirs++; }
        score = 0;
        for (int i = 0; i < np; i++) if (owner[i] == 1) score += 50 + ships[i];
        if (theirs == 0) {
            score += 1000 - Math.min(900, frame / 20);
            headline = "GALAXY CONQUERED";
            endGame(true);
        } else if (mine == 0) {
            headline = "DEFEATED";
            endGame(false);
        }
    }

    private void arrive(int t, int who, int n) {
        if (owner[t] == who) {
            ships[t] += n;
            return;
        }
        ships[t] -= n;
        if (ships[t] < 0) {
            ships[t] = -ships[t];
            owner[t] = who;
            if (who == 1) Sfx.good(); else Sfx.bad();
        } else {
            Sfx.hit();
        }
    }

    private void ai() {
        if (--aiT > 0) return;
        aiT = mode == 0 ? 70 : 40;
        // strongest CPU planet attacks the best-value target it can beat
        int from = -1;
        for (int i = 0; i < np; i++) if (owner[i] == 2 && (from < 0 || ships[i] > ships[from])) from = i;
        if (from < 0 || ships[from] < 8) return;
        int best = -1, bestV = -1 << 30;
        for (int i = 0; i < np; i++) {
            if (owner[i] == 2) continue;
            int dx = px[i] - px[from], dy = py[i] - py[from];
            int dist = FMath.dist(dx, dy);
            int need = ships[i] + 2 + dist / 20;
            if (ships[from] / 2 <= need && mode == 0) continue;
            if (ships[from] / 2 <= ships[i] + 1) continue;
            int v = prod[i] * 20 - ships[i] * 3 - dist / 2 + (owner[i] == 1 ? 25 : 0);
            if (v > bestV) {
                bestV = v;
                best = i;
            }
        }
        if (best >= 0) send(from, best, 2);
        // reinforce: weak CPU planets ask for help on Admiral
        if (mode == 1 && Rnd.chance(30)) {
            int weak = -1;
            for (int i = 0; i < np; i++) if (owner[i] == 2 && (weak < 0 || ships[i] < ships[weak])) weak = i;
            if (weak >= 0 && weak != from && ships[from] > 30) send(from, weak, 2);
        }
    }

    protected void draw(Graphics g) {
        g.setColor(0x03040C);
        g.fillRect(0, 0, W, H);
        g.setColor(0x1C2340);
        for (int k = 0; k < 40; k++) g.fillRect((k * 71 + 13) % W, (k * 37 + 7) % H, 1, 1);
        if (src >= 0) {
            g.setColor(0x42A5F5);
            g.setStrokeStyle(Graphics.DOTTED);
            g.drawLine(px[src], py[src], px[cur], py[cur]);
            g.setStrokeStyle(Graphics.SOLID);
        }
        for (int i = 0; i < np; i++) {
            g.setColor(OWNER_COL[owner[i]]);
            Gfx.disc(g, px[i], py[i], pr[i]);
            g.setColor(Gfx.shade(OWNER_COL[owner[i]], 40));
            Gfx.disc(g, px[i] - pr[i] / 3, py[i] - pr[i] / 3, Math.max(1, pr[i] / 3));
            g.setFont(Gfx.SMALL_B);
            g.setColor(0xFFFFFF);
            g.drawString(String.valueOf(ships[i]), px[i] + 1, py[i] - 3, Gfx.TC);
            if (i == src) {
                g.setColor(0xFFEB3B);
                Gfx.ring(g, px[i], py[i], pr[i] + 3);
            }
        }
        g.setColor((clock & 4) == 0 ? 0xFFFFFF : 0x80DEEA);
        Gfx.ring(g, px[cur], py[cur], pr[cur] + 2);
        for (int i = 0; i < MAXF; i++) {
            if (!fon[i]) continue;
            g.setColor(OWNER_COL[fo[i]]);
            int x = fx[i] >> 8, y = fy[i] >> 8;
            g.fillTriangle(x, y - 3, x - 3, y + 2, x + 3, y + 2);
            g.setFont(Gfx.SMALL);
            g.drawString(String.valueOf(fn[i]), x + 4, y - 4, Gfx.TL);
        }
        int mine = 0, theirs = 0;
        for (int i = 0; i < np; i++) {
            if (owner[i] == 1) mine += ships[i];
            if (owner[i] == 2) theirs += ships[i];
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x42A5F5);
        g.drawString("You " + mine, 2, 1, Gfx.TL);
        g.setColor(0xEF5350);
        g.drawString(theirs + " CPU", W - 2, 1, Gfx.TR);
        int bw = W / 3;
        int total = Math.max(1, mine + theirs);
        g.setColor(0xEF5350);
        g.fillRect(W / 2 - bw / 2, 4, bw, 4);
        g.setColor(0x42A5F5);
        g.fillRect(W / 2 - bw / 2, 4, bw * mine / total, 4);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int r = Math.max(5, h / 5);
        g.setColor(0x42A5F5);
        Gfx.disc(g, x + w / 5, y + h / 2, r);
        g.setColor(0xEF5350);
        Gfx.disc(g, x + w * 4 / 5, y + h / 2, r);
        g.setColor(0x90A4AE);
        Gfx.disc(g, x + w / 2, y + h / 4, r * 2 / 3);
        int t = (clock * 2) % (w * 3 / 5);
        g.setColor(0x42A5F5);
        g.fillTriangle(x + w / 5 + t, y + h / 2 - 3, x + w / 5 + t - 3, y + h / 2 + 2, x + w / 5 + t + 3, y + h / 2 + 2);
    }
}
