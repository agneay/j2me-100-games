package spacetrader;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Space Trader: buy low, sell high across six planets in thirty days. Each
 * planet produces some goods cheaply and pays well for others; prices drift
 * daily and random events can spike them. Travel costs fuel and time.
 */
public class TraderGame extends Game {
    private static final String[] PLANET = { "Terra", "Mars", "Ceres", "Io", "Titan", "Nova" };
    private static final int[] PX = { 20, 55, 80, 35, 70, 90 }, PY = { 50, 25, 70, 85, 15, 45 };
    private static final String[] GOOD = { "Food", "Ore", "Water", "Tech", "Medic" };
    private static final int[] BASE = { 20, 35, 15, 120, 80 };
    // production (+) or demand (-) per planet and good, in percent of base
    private static final int[][] BIAS = {
        { -40, 30, -20, 20, 10 },
        { 30, -40, 40, -10, 20 },
        { 40, -30, 30, 30, 0 },
        { 20, 10, -40, 40, 30 },
        { 30, 20, 0, -30, 40 },
        { 0, 40, 30, -20, -40 },
    };
    private static final int DAYS = 30;

    private final int[][] price = new int[6][5];
    private final int[] cargo = new int[5];
    private int at, cash, fuel, day, bay, row, mode2, dest, debt;
    private String news = "";
    private static final int MARKET = 0, MAP = 1;

    protected String name() { return "Space Trader"; }

    protected String[] help() {
        return new String[] {
            "You have 30 days, a small freighter and a loan to repay. Buy goods where they are cheap and sell them where they are dear.",
            "Each planet produces some goods (cheap) and needs others (expensive). Travelling costs fuel and days; fuel is cheaper on some worlds. News reports can send prices soaring.",
            "Your score is your cash minus the loan at the end of day 30.",
            "- Controls",
            "2/8: choose good",
            "6: buy one  4: sell one",
            "1: sell all  3: buy max",
            "5: open star map / travel",
            "0: buy fuel  9: upgrade hold",
        };
    }

    protected int accent() { return 0x7C4DFF; }

    protected void newGame() {
        cash = 500;
        debt = 1000;
        cash += debt;
        fuel = 10;
        bay = 20;
        day = 1;
        at = 0;
        row = 0;
        mode2 = MARKET;
        for (int g = 0; g < 5; g++) cargo[g] = 0;
        for (int p = 0; p < 6; p++) for (int g = 0; g < 5; g++) price[p][g] = BASE[g] * (100 + BIAS[p][g]) / 100;
        drift();
        news = "Loan of $1000 granted";
    }

    private void drift() {
        for (int p = 0; p < 6; p++) for (int g = 0; g < 5; g++) {
            int target = BASE[g] * (100 + BIAS[p][g]) / 100;
            int v = price[p][g] + (target - price[p][g]) / 3 + Rnd.range(-BASE[g] / 8, BASE[g] / 8 + 1);
            price[p][g] = Math.max(2, v);
        }
    }

    private int used() {
        int n = 0;
        for (int g = 0; g < 5; g++) n += cargo[g];
        return n;
    }

    private int dist(int a, int b) {
        int dx = PX[a] - PX[b], dy = PY[a] - PY[b];
        int d = Math.abs(dx) + Math.abs(dy);
        return Math.max(1, d / 25);
    }

    private int fuelPrice() { return at == 3 || at == 2 ? 6 : 10; }

    private void travel(int to) {
        int d = dist(at, to);
        if (fuel < d) {
            news = "Not enough fuel!";
            Sfx.bad();
            return;
        }
        fuel -= d;
        day += d;
        at = to;
        for (int k = 0; k < d; k++) drift();
        mode2 = MARKET;
        news = "Arrived at " + PLANET[at];
        Sfx.good();
        event();
        if (day > DAYS) finish();
    }

    private void event() {
        int r = Rnd.nextInt(100);
        if (r < 12) {
            int g = Rnd.nextInt(5), p = Rnd.nextInt(6);
            price[p][g] = price[p][g] * 2;
            news = GOOD[g] + " shortage on " + PLANET[p] + "!";
        } else if (r < 20) {
            int g = Rnd.nextInt(5), p = Rnd.nextInt(6);
            price[p][g] = Math.max(2, price[p][g] / 2);
            news = GOOD[g] + " glut on " + PLANET[p];
        } else if (r < 27 && used() > 0) {
            int g;
            do g = Rnd.nextInt(5); while (cargo[g] == 0);
            int lost = (cargo[g] + 1) / 2;
            cargo[g] -= lost;
            news = "Pirates took " + lost + " " + GOOD[g];
            Sfx.bad();
        } else if (r < 32) {
            int found = 20 + Rnd.nextInt(60);
            cash += found;
            news = "Salvaged a wreck: +$" + found;
        }
    }

    private void finish() {
        int worth = cash - debt;
        for (int g = 0; g < 5; g++) worth += cargo[g] * price[at][g] / 2;
        score = Math.max(0, worth);
        headline = worth > 500 ? "TRADE BARON! $" + worth : (worth > 0 ? "SOLVENT $" + worth : "IN DEBT");
        endGame(worth > 500);
    }

    protected void update() {
        if (mode2 == MAP) {
            if ((pressed & (K_UP | K_LEFT)) != 0) dest = (dest + 5) % 6;
            if ((pressed & (K_DOWN | K_RIGHT)) != 0) dest = (dest + 1) % 6;
            if ((pressed & K_FIRE) != 0) {
                if (dest == at) mode2 = MARKET;
                else travel(dest);
            }
            if (digit(0)) mode2 = MARKET;
            return;
        }
        if ((pressed & K_UP) != 0) row = (row + 4) % 5;
        if ((pressed & K_DOWN) != 0) row = (row + 1) % 5;
        int p = price[at][row];
        if ((pressed & K_RIGHT) != 0 || digit(3)) {
            int n = digit(3) ? Math.min(bay - used(), cash / p) : 1;
            if (n > 0 && used() < bay && cash >= p) {
                cargo[row] += n;
                cash -= n * p;
                Sfx.click();
            } else Sfx.bad();
        }
        if ((pressed & K_LEFT) != 0 || digit(1)) {
            int n = digit(1) ? cargo[row] : Math.min(1, cargo[row]);
            if (n > 0) {
                cargo[row] -= n;
                cash += n * p;
                Sfx.tone(80, 20);
            } else Sfx.bad();
        }
        if (digit(0)) {
            if (cash >= fuelPrice() && fuel < 20) {
                cash -= fuelPrice();
                fuel++;
                Sfx.click();
            } else Sfx.bad();
        }
        if (digit(9)) {
            int cost = bay * 15;
            if (cash >= cost && bay < 60) {
                cash -= cost;
                bay += 10;
                news = "Hold expanded to " + bay;
                Sfx.good();
            } else {
                news = "Hold upgrade $" + cost;
                Sfx.bad();
            }
        }
        if ((pressed & K_FIRE) != 0) {
            mode2 = MAP;
            dest = at;
        }
        score = Math.max(0, cash - debt);
        if (fuel == 0 && cash < fuelPrice() && used() == 0) {
            news = "Stranded!";
            finish();
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int fh = Gfx.SMALL.getHeight();
        g.setColor(0x0A0420);
        g.fillRect(0, 0, W, H);
        g.setColor(0x4527A0);
        for (int k = 0; k < 30; k++) g.fillRect((k * 53) % W, (k * 37 + 11) % H, 1, 1);
        g.setColor(0x000000);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xB388FF);
        g.drawString("Day " + Math.min(day, DAYS) + "/" + DAYS, 2, 1, Gfx.TL);
        g.setColor(0x69F0AE);
        g.drawString("$" + cash, W - 2, 1, Gfx.TR);
        if (mode2 == MAP) {
            int mx = 6, my = hud + 4, mw = W - 12, mh = H - hud - fh * 3 - 10;
            Gfx.panel(g, mx, my, mw, mh, 0x120A30, 0x7C4DFF);
            for (int p = 0; p < 6; p++) {
                int x = mx + PX[p] * mw / 100, y = my + 4 + PY[p] * (mh - 12) / 100;
                if (p == dest && p != at) {
                    g.setColor(fuel >= dist(at, p) ? 0x69F0AE : 0xFF5252);
                    g.drawLine(mx + PX[at] * mw / 100, my + 4 + PY[at] * (mh - 12) / 100, x, y);
                }
                g.setColor(p == at ? 0xFFD740 : 0x80D8FF);
                Gfx.disc(g, x, y, p == dest ? 4 : 3);
                Gfx.text(g, PLANET[p], x, y + 4, Gfx.TC, Gfx.SMALL, p == dest ? 0xFFFFFF : 0x9FA8DA);
            }
            int ty = H - fh * 3 - 4;
            Gfx.text(g, dest == at ? (W < 160 ? "Here: " : "You are here: ") + PLANET[at] : PLANET[dest] + ": " + dist(at, dest) + (W < 160 ? "d" : " days") + ", " + dist(at, dest) + " fuel", W / 2, ty, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF);
            Gfx.text(g, (W < 160 ? "Fuel " + fuel + " 5:go 0:back" : "Fuel " + fuel + "   5: go   0: back"), W / 2, ty + fh + 2, Gfx.TC, Gfx.SMALL, 0xB0BEC5);
            Gfx.text(g, news, W / 2, ty + fh * 2 + 3, Gfx.TC, Gfx.SMALL, 0xFFAB40);
            return;
        }
        Gfx.text(g, W < 160 ? PLANET[at] : PLANET[at] + " market", 4, hud + 2, Gfx.TL, Gfx.SMALL_B, 0xFFD740);
        Gfx.text(g, "Hold " + used() + "/" + bay, W - 4, hud + 2, Gfx.TR, Gfx.SMALL, 0xB0BEC5);
        int ry = hud + fh + 6;
        int rh = fh + 3;
        int cA = W / 2, cB = W - 6;
        Gfx.text(g, "price", cA, ry, Gfx.TR, Gfx.SMALL, 0x7986CB);
        Gfx.text(g, "have", cB, ry, Gfx.TR, Gfx.SMALL, 0x7986CB);
        ry += fh + 1;
        for (int k = 0; k < 5; k++) {
            int y = ry + k * rh;
            if (k == row) {
                g.setColor(0x311B92);
                g.fillRect(2, y - 1, W - 4, rh);
            }
            int base = BASE[k];
            int pc = price[at][k];
            int col = pc < base * 80 / 100 ? 0x69F0AE : (pc > base * 125 / 100 ? 0xFF8A80 : 0xFFFFFF);
            Gfx.text(g, GOOD[k], 6, y, Gfx.TL, Gfx.SMALL, 0xE8EAF6);
            Gfx.text(g, "$" + pc, cA, y, Gfx.TR, Gfx.SMALL_B, col);
            Gfx.text(g, String.valueOf(cargo[k]), cB, y, Gfx.TR, Gfx.SMALL, 0xE8EAF6);
        }
        int by = ry + rh * 5 + 3;
        Gfx.text(g, W < 160 ? "Fuel " + fuel + " @ $" + fuelPrice() : "Fuel " + fuel + " ($" + fuelPrice() + ")  Loan $" + debt, W / 2, by, Gfx.TC, Gfx.SMALL, 0xB0BEC5);
        if (by + fh * 3 + 4 <= H) Gfx.text(g, "6 buy 4 sell 5 map", W / 2, by + fh + 2, Gfx.TC, Gfx.SMALL, 0x7986CB);
        Gfx.text(g, news, W / 2, H - fh - 1, Gfx.TC, Gfx.SMALL, 0xFFAB40);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x80D8FF);
        Gfx.disc(g, x + w / 5, y + h / 2, Math.max(4, h / 4));
        g.setColor(0xFF8A65);
        Gfx.disc(g, x + w * 4 / 5, y + h / 3, Math.max(3, h / 5));
        int t = clock % 50;
        int sx = x + w / 5 + t * (w * 3 / 5) / 50, sy = y + h / 2 - t * (h / 6) / 50;
        g.setColor(0xFFFFFF);
        g.fillTriangle(sx + 5, sy, sx - 3, sy - 3, sx - 3, sy + 3);
        g.setColor(0xFFAB40);
        g.fillRect(sx - 5, sy - 1, 2, 2);
    }
}
