package tinycity;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Tiny City: zone a town on a 12x12 map. Zones grow only when they touch a
 * road and are within reach of a power plant; residents need jobs and jobs
 * need residents. Taxes pay for upkeep.
 */
public class CityGame extends Game {
    private static final int N = 12;
    private static final int EMPTY = 0, ROAD = 1, RES = 2, COM = 3, IND = 4, POWER = 5, PARK = 6, WATER = 7;
    private static final String[] TOOL = { "Bulldoze", "Road", "Homes", "Shops", "Factory", "Power plant", "Park" };
    private static final int[] COST = { 1, 10, 20, 30, 30, 120, 15 };
    private static final int[] COL = { 0x7CB342, 0x616161, 0x66BB6A, 0x42A5F5, 0xFFB300, 0xE53935, 0x2E7D32, 0x1E88E5 };

    private final byte[] t = new byte[N * N];
    private final byte[] lvl = new byte[N * N];
    private final boolean[] powered = new boolean[N * N];
    private int cur = N * N / 2, tool = 1, money, month, pop, jobs, monthT, msgT;
    private String msg = "";

    protected String name() { return "Tiny City"; }

    protected String[] help() {
        return new String[] {
            "Build a town. Zone homes (green), shops (blue) and factories (yellow); they grow by themselves when they touch a road and are within 4 squares of a power plant.",
            "Homes need jobs from shops and factories; shops and factories need workers. Parks next to homes help them grow; factories next to homes hold them back.",
            "Each month you collect taxes and pay road and plant upkeep. Reach 2000 citizens within 10 years.",
            "- Controls",
            "2/4/6/8: move  5: build",
            "0 or #: next tool  1: previous tool",
        };
    }

    protected String[] modes() { return new String[] { "10-year goal", "Sandbox" }; }

    protected int accent() { return 0x42A5F5; }

    protected void newGame() {
        for (int i = 0; i < N * N; i++) {
            t[i] = EMPTY;
            lvl[i] = 0;
        }
        // a river along one side
        int x = Rnd.range(2, N - 3);
        for (int y = 0; y < N; y++) {
            t[y * N + x] = WATER;
            if (Rnd.chance(40)) x = Math.max(1, Math.min(N - 2, x + Rnd.range(-1, 1)));
        }
        money = mode == 1 ? 99999 : 600;
        month = 0;
        monthT = 0;
        pop = jobs = 0;
        tool = 1;
        cur = N * N / 2;
        say("Lay roads, then zones and power");
    }

    private void say(String s) {
        msg = s;
        msgT = 50;
    }

    private boolean nearRoad(int i) {
        int x = i % N, y = i / N;
        return (x > 0 && t[i - 1] == ROAD) || (x < N - 1 && t[i + 1] == ROAD) || (y > 0 && t[i - N] == ROAD) || (y < N - 1 && t[i + N] == ROAD);
    }

    private int countNear(int i, int type, int r) {
        int x0 = i % N, y0 = i / N, n = 0;
        for (int y = Math.max(0, y0 - r); y <= Math.min(N - 1, y0 + r); y++)
            for (int x = Math.max(0, x0 - r); x <= Math.min(N - 1, x0 + r); x++) if (t[y * N + x] == type) n++;
        return n;
    }

    private void computePower() {
        for (int i = 0; i < N * N; i++) powered[i] = false;
        for (int p = 0; p < N * N; p++) {
            if (t[p] != POWER) continue;
            int px = p % N, py = p / N;
            for (int y = Math.max(0, py - 4); y <= Math.min(N - 1, py + 4); y++)
                for (int x = Math.max(0, px - 4); x <= Math.min(N - 1, px + 4); x++) powered[y * N + x] = true;
        }
    }

    private void monthTick() {
        month++;
        computePower();
        pop = 0;
        jobs = 0;
        for (int i = 0; i < N * N; i++) {
            if (t[i] == RES) pop += lvl[i] * 40;
            if (t[i] == COM) jobs += lvl[i] * 25;
            if (t[i] == IND) jobs += lvl[i] * 35;
        }
        int resDemand = jobs - pop * 6 / 10 + 40;
        int jobDemand = pop * 7 / 10 - jobs + 20;
        for (int i = 0; i < N * N; i++) {
            int z = t[i];
            if (z != RES && z != COM && z != IND) continue;
            boolean ok = nearRoad(i) && powered[i];
            if (!ok) {
                if (lvl[i] > 0 && Rnd.chance(20)) lvl[i]--;
                continue;
            }
            int demand = z == RES ? resDemand : jobDemand;
            int chance = demand > 0 ? 35 : 5;
            if (z == RES) chance += countNear(i, PARK, 1) * 10 - countNear(i, IND, 1) * 12;
            if (z == COM) chance += countNear(i, RES, 2) * 3;
            if (Rnd.nextInt(100) < chance && lvl[i] < 3) lvl[i]++;
            else if (demand < -60 && lvl[i] > 1 && Rnd.chance(15)) lvl[i]--;
        }
        int roads = 0, plants = 0;
        for (int i = 0; i < N * N; i++) {
            if (t[i] == ROAD) roads++;
            if (t[i] == POWER) plants++;
        }
        int tax = pop / 8 + jobs / 12;
        int upkeep = roads / 2 + plants * 6;
        if (mode == 0) money += tax - upkeep;
        score = pop;
        if (mode == 0) {
            if (pop >= 2000) {
                headline = "THRIVING CITY!";
                score = pop + Math.max(0, 120 - month) * 10;
                endGame(true);
            } else if (month >= 120) {
                headline = "10 YEARS UP";
                endGame(false);
            } else if (money < -200) {
                headline = "BANKRUPT";
                endGame(false);
            }
        }
    }

    protected void update() {
        if (msgT > 0) msgT--;
        if (++monthT >= 60) {
            monthT = 0;
            monthTick();
            if (state != PLAY) return;
        }
        int x = cur % N, y = cur / N;
        if ((pressed & K_LEFT) != 0) x = (x + N - 1) % N;
        if ((pressed & K_RIGHT) != 0) x = (x + 1) % N;
        if ((pressed & K_UP) != 0) y = (y + N - 1) % N;
        if ((pressed & K_DOWN) != 0) y = (y + 1) % N;
        cur = y * N + x;
        if ((pressed & (K_NUM0 | K_POUND)) != 0) { tool = (tool + 1) % TOOL.length; Sfx.click(); }
        if (digit(1)) { tool = (tool + TOOL.length - 1) % TOOL.length; Sfx.click(); }
        if ((pressed & K_FIRE) != 0) build();
    }

    private void build() {
        int cell = t[cur];
        if (cell == WATER && tool != ROAD) { say("Can't build on water"); Sfx.bad(); return; }
        if (tool == 0) {
            if (cell == EMPTY || cell == WATER) return;
            t[cur] = EMPTY;
            lvl[cur] = 0;
            money -= COST[0];
            Sfx.tone(45, 30);
            return;
        }
        if (cell != EMPTY && !(cell == WATER && tool == ROAD)) { say("Bulldoze first"); Sfx.bad(); return; }
        int cost = COST[tool] * (cell == WATER ? 4 : 1); // bridges cost more
        if (money < cost) { say("Not enough money"); Sfx.bad(); return; }
        money -= cost;
        t[cur] = (byte) (tool == 1 ? ROAD : (tool == 2 ? RES : (tool == 3 ? COM : (tool == 4 ? IND : (tool == 5 ? POWER : PARK)))));
        lvl[cur] = 0;
        computePower();
        Sfx.tone(70, 20);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int s = Math.max(6, Math.min(W / N, (H - hud * 3) / N));
        int ox = (W - s * N) / 2, oy = hud * 2 + (H - hud * 3 - s * N) / 2;
        g.setColor(0x1B2A1B);
        g.fillRect(0, 0, W, H);
        for (int i = 0; i < N * N; i++) {
            int x = ox + (i % N) * s, y = oy + (i / N) * s;
            int z = t[i];
            g.setColor(COL[EMPTY]);
            g.fillRect(x, y, s, s);
            if (z == EMPTY) continue;
            if (z == ROAD) {
                g.setColor(COL[ROAD]);
                g.fillRect(x, y, s, s);
                g.setColor(0xFFEB3B);
                g.fillRect(x + s / 2, y + s / 2, 1, 1);
            } else if (z == WATER) {
                g.setColor(((i + clock / 10) & 1) == 0 ? 0x1E88E5 : 0x1976D2);
                g.fillRect(x, y, s, s);
            } else if (z == POWER) {
                Gfx.bevel(g, x, y, s, s, 0x9E9E9E);
                g.setColor(COL[POWER]);
                g.fillRect(x + s / 3, y + 1, s / 4, s / 2);
                if ((clock & 8) == 0) {
                    g.setColor(0xBDBDBD);
                    Gfx.disc(g, x + s / 2, y, 2);
                }
            } else if (z == PARK) {
                g.setColor(COL[PARK]);
                Gfx.disc(g, x + s / 3, y + s / 2, s / 4);
                Gfx.disc(g, x + s * 2 / 3, y + s / 3, s / 4);
            } else {
                int l = lvl[i];
                g.setColor(Gfx.shade(COL[z], -40));
                g.drawRect(x + 1, y + 1, s - 3, s - 3);
                if (l > 0) {
                    int bh = s * l / 3 - 2;
                    Gfx.bevel(g, x + 2, y + s - 1 - bh, s - 4, bh, COL[z]);
                }
                if (!powered[i] && (clock & 16) == 0) {
                    g.setColor(0xFFEB3B);
                    g.fillRect(x + s - 3, y + 1, 2, 3);
                }
            }
        }
        int cx = ox + (cur % N) * s, cy = oy + (cur / N) * s;
        g.setColor((clock & 4) == 0 ? 0xFFFFFF : 0x000000);
        g.drawRect(cx, cy, s - 1, s - 1);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString(mode == 1 ? "Sandbox" : "$" + money, 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString("Pop " + pop, W - 2, 1, Gfx.TR);
        g.setFont(Gfx.SMALL);
        g.setColor(0xB0BEC5);
        g.drawString("Y" + (month / 12 + 1) + " M" + (month % 12 + 1) + "  jobs " + jobs, W / 2, hud, Gfx.TC);
        int ty = H - hud;
        g.setColor(tool == 0 ? 0x8D6E63 : COL[tool == 1 ? ROAD : (tool == 2 ? RES : (tool == 3 ? COM : (tool == 4 ? IND : (tool == 5 ? POWER : PARK))))]);
        g.fillRect(2, ty + 2, 6, 6);
        g.setColor(0xFFFFFF);
        g.drawString(msgT > 0 ? msg : TOOL[tool] + " $" + COST[tool] + "  (0: tool)", 11, ty, Gfx.TL);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(6, h / 3);
        int n = w / s;
        for (int k = 0; k < n; k++) {
            int hh = ((k * 7 + 3) % 5 + 1) * h / 6;
            int grow = Math.min(hh, (clock * 2 + k * 9) % (h * 2));
            Gfx.bevel(g, x + k * s + 1, y + h - grow, s - 2, grow, COL[2 + k % 3]);
        }
    }
}
