package pocketfarm;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/** Pocket Farm: till, plant, water and harvest to earn $1000 in one season. */
public class FarmGame extends Game {
    private static final int FC = 6, FR = 5, SEASON = 28, ENERGY = 14;
    private static final String[] CROP = { "Turnip", "Carrot", "Tomato", "Melon" };
    private static final int[] DAYS = { 3, 5, 6, 8 };
    private static final int[] COST = { 4, 7, 10, 20 };
    private static final int[] PRICE = { 11, 21, 18, 68 };
    private static final int[] REGROW = { 0, 0, 3, 0 };
    private static final int[] FRUIT = { 0xE1BEE7, 0xFF9800, 0xE53935, 0x7CB342 };

    // plot: 0 grass, 1 tilled, 2 growing, 3 ripe
    private final int[] stage = new int[FC * FR], crop = new int[FC * FR], growth = new int[FC * FR], dry = new int[FC * FR];
    private final boolean[] wet = new boolean[FC * FR];
    private final int[] market = new int[4];
    private int cur, seed, money, day, energy, sleepAnim;
    private boolean rainTomorrow, rainedToday;
    private String note = "";

    protected String name() { return "Pocket Farm"; }

    protected String[] help() {
        return new String[] {
            "Turn a weedy field into a profit. Press 5 on a plot to do the next job: till the soil, plant the selected seed, water it, and harvest when ripe. Harvests are sold at today's market price.",
            "Watered crops grow one stage each night. Crops left dry for three days wither. Rain waters everything for free; check the forecast.",
            "Every job uses energy; sleep (#) to start a new day. Earn $1000 within the 28-day season.",
            "Tomatoes regrow after harvest. Melons are slow but valuable.",
            "- Controls",
            "2/4/6/8: move  5: work plot",
            "0: change seed  #: sleep",
        };
    }

    protected String[] modes() { return new String[] { "Season", "Endless" }; }

    protected String formatScore(int s) { return "$" + s; }

    protected int accent() { return 0xF4A261; }

    protected void newGame() {
        for (int i = 0; i < FC * FR; i++) {
            stage[i] = 0;
            wet[i] = false;
            dry[i] = 0;
        }
        cur = FC * 2 + 2;
        seed = 0;
        money = 30;
        day = 1;
        energy = ENERGY;
        rainTomorrow = false;
        rainedToday = false;
        rollMarket();
        note = "Till the soil with 5.";
    }

    private void rollMarket() {
        for (int c = 0; c < 4; c++) market[c] = PRICE[c] * Rnd.range(80, 125) / 100;
    }

    private boolean useEnergy() {
        if (energy <= 0) {
            note = "Too tired. Sleep with #.";
            Sfx.bad();
            return false;
        }
        energy--;
        return true;
    }

    private void work() {
        int i = cur;
        switch (stage[i]) {
            case 0:
                if (useEnergy()) {
                    stage[i] = 1;
                    note = "Soil tilled.";
                    Sfx.tone(50, 30);
                }
                break;
            case 1:
                if (money < COST[seed]) {
                    note = "Not enough money.";
                    Sfx.bad();
                } else if (useEnergy()) {
                    money -= COST[seed];
                    stage[i] = 2;
                    crop[i] = seed;
                    growth[i] = 0;
                    dry[i] = 0;
                    wet[i] = rainedToday;
                    note = CROP[seed] + " planted.";
                    Sfx.tone(67, 30);
                }
                break;
            case 2:
                if (wet[i]) {
                    note = "Already watered.";
                } else if (useEnergy()) {
                    wet[i] = true;
                    note = "Watered.";
                    Sfx.tone(79, 20);
                }
                break;
            default:
                if (useEnergy()) {
                    int earn = market[crop[i]];
                    money += earn;
                    note = CROP[crop[i]] + " sold +$" + earn;
                    Sfx.good();
                    if (REGROW[crop[i]] > 0) {
                        stage[i] = 2;
                        growth[i] = DAYS[crop[i]] - REGROW[crop[i]];
                        wet[i] = false;
                    } else {
                        stage[i] = 1;
                    }
                }
                break;
        }
    }

    private void sleep() {
        int withered = 0;
        for (int i = 0; i < FC * FR; i++) {
            if (stage[i] != 2) continue;
            if (wet[i]) {
                growth[i]++;
                dry[i] = 0;
                if (growth[i] >= DAYS[crop[i]]) stage[i] = 3;
            } else if (++dry[i] >= 3) {
                stage[i] = 1;
                withered++;
            }
        }
        day++;
        energy = ENERGY;
        rainedToday = rainTomorrow;
        rainTomorrow = Rnd.chance(22);
        for (int i = 0; i < FC * FR; i++) wet[i] = rainedToday && stage[i] == 2;
        rollMarket();
        sleepAnim = 20;
        note = withered > 0 ? withered + " crop(s) withered!" : (rainedToday ? "It rained overnight." : "Good morning!");
        Sfx.tone(60, 60);
        score = money;
        if (mode == 0) {
            if (money >= 1000) {
                headline = "HARVEST HERO!";
                endGame(true);
            } else if (day > SEASON) {
                headline = "SEASON OVER";
                endGame(false);
            }
        }
    }

    protected void update() {
        if (sleepAnim > 0) {
            sleepAnim--;
            return;
        }
        int x = cur % FC, y = cur / FC;
        if ((pressed & K_LEFT) != 0) x = (x + FC - 1) % FC;
        if ((pressed & K_RIGHT) != 0) x = (x + 1) % FC;
        if ((pressed & K_UP) != 0) y = (y + FR - 1) % FR;
        if ((pressed & K_DOWN) != 0) y = (y + 1) % FR;
        cur = y * FC + x;
        if ((pressed & K_FIRE) != 0) work();
        if (digit(0)) {
            seed = (seed + 1) % 4;
            Sfx.click();
            note = CROP[seed] + ": $" + COST[seed] + ", " + DAYS[seed] + " days";
        }
        if ((pressed & K_POUND) != 0) sleep();
        score = money;
        if (mode == 1 && money >= 99999) endGame(true);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int cell = Math.max(10, Math.min((W - 4) / FC, (H - hud * 4) / FR));
        int ox = (W - cell * FC) / 2, oy = hud * 2 + (H - hud * 4 - cell * FR) / 2;
        int sky = sleepAnim > 0 ? Gfx.mix(0x101848, 0x7EC8E3, (20 - sleepAnim) * 12) : (rainedToday ? 0x8899AA : 0x7EC8E3);
        g.setColor(sky);
        g.fillRect(0, 0, W, oy);
        g.setColor(0x5DA449);
        g.fillRect(0, oy - 2, W, H - oy + 2);
        // farmhouse
        int fh = Math.min(hud * 2 - 2, oy - hud - 2);
        if (fh > 6) {
            g.setColor(0xB71C1C);
            g.fillRect(W - fh * 2 - 4, oy - fh, fh * 3 / 2, fh);
            g.setColor(0x5D4037);
            g.fillTriangle(W - fh * 2 - 6, oy - fh, W - fh * 5 / 4 - 4, oy - fh * 3 / 2 - 2, W - fh / 2 - 2, oy - fh);
        }
        if (rainedToday && sleepAnim == 0) {
            g.setColor(0xCFE8FF);
            for (int k = 0; k < 20; k++) {
                int rx = (k * 37 + clock * 3) % W, ry = (k * 23 + clock * 7) % H;
                g.drawLine(rx, ry, rx - 1, ry + 3);
            }
        }
        for (int i = 0; i < FC * FR; i++) {
            int x = ox + (i % FC) * cell, y = oy + (i / FC) * cell;
            drawPlot(g, i, x, y, cell);
        }
        int cx = ox + (cur % FC) * cell, cy = oy + (cur / FC) * cell;
        g.setColor((clock & 4) == 0 ? 0xFFFFFF : 0xFFEB3B);
        g.drawRect(cx, cy, cell - 1, cell - 1);
        g.drawRect(cx + 1, cy + 1, cell - 3, cell - 3);
        // HUD
        g.setColor(0x3E2723);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString("Day " + day + (mode == 0 ? "/" + SEASON : ""), 2, 1, Gfx.TL);
        g.setColor(0xFFD54F);
        g.drawString("$" + money, W - 2, 1, Gfx.TR);
        Gfx.bar(g, 4, hud + 2, W / 3, 5, energy, ENERGY, 0x66BB6A, 0x263238);
        g.setFont(Gfx.SMALL);
        g.setColor(0x1A1A1A);
        g.drawString(rainTomorrow ? "Tmrw: rain" : "Tmrw: sun", W / 3 + 8, hud + 1, Gfx.TL);
        int by = H - hud * 2;
        g.setColor(0x3E2723);
        g.fillRect(0, by, W, hud * 2);
        g.setColor(FRUIT[seed]);
        Gfx.disc(g, 7, by + hud / 2 + 1, 3);
        g.setColor(0xFFFFFF);
        g.drawString(CROP[seed] + " $" + COST[seed] + " sells ~$" + market[seed], 13, by + 1, Gfx.TL);
        g.setColor(0xFFE0B2);
        g.drawString(note, W / 2, by + hud, Gfx.TC);
    }

    private void drawPlot(Graphics g, int i, int x, int y, int s) {
        switch (stage[i]) {
            case 0:
                g.setColor(0x6DB356);
                g.fillRect(x + 1, y + 1, s - 2, s - 2);
                g.setColor(0x4E8C3A);
                g.drawLine(x + s / 4, y + s * 3 / 4, x + s / 4 + 1, y + s / 2);
                g.drawLine(x + s * 2 / 3, y + s / 2, x + s * 2 / 3 - 1, y + s / 4);
                return;
            default:
                g.setColor(wet[i] ? 0x5D3A1A : 0x8D5A2B);
                g.fillRect(x + 1, y + 1, s - 2, s - 2);
                g.setColor(wet[i] ? 0x4A2D14 : 0x75491F);
                for (int k = 1; k < 4; k++) g.drawLine(x + 2, y + k * s / 4, x + s - 3, y + k * s / 4);
                break;
        }
        if (stage[i] < 2) return;
        int c = crop[i];
        int cxp = x + s / 2, base = y + s - 3;
        int prog = stage[i] == 3 ? 4 : Math.min(3, growth[i] * 4 / DAYS[c]);
        g.setColor(0x2E7D32);
        if (prog == 0) {
            g.fillRect(cxp - 1, base - 2, 2, 2);
        } else {
            int hgt = s * (prog + 1) / 7;
            g.drawLine(cxp, base, cxp, base - hgt);
            g.fillTriangle(cxp, base - hgt, cxp - s / 4, base - hgt + s / 6, cxp, base - hgt + s / 8);
            g.fillTriangle(cxp, base - hgt / 2, cxp + s / 4, base - hgt / 2 + s / 8, cxp, base - hgt / 2 + s / 6);
        }
        if (stage[i] == 3) {
            g.setColor(FRUIT[c]);
            Gfx.disc(g, cxp + s / 6, base - s / 4, Math.max(2, s / 6 + (c == 3 ? 2 : 0)));
            if ((clock & 8) == 0) {
                g.setColor(0xFFFFFF);
                g.fillRect(cxp + s / 6, base - s / 4 - 1, 1, 1);
            }
        }
        if (dry[i] >= 2 && stage[i] == 2) {
            g.setColor(0xFFB300);
            g.drawString("!", x + 2, y + 1, Gfx.TL);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(10, Math.min(h - 2, w / 6));
        int n = Math.min(6, w / s);
        int x0 = x + (w - n * s) / 2;
        for (int k = 0; k < n; k++) {
            stage[k] = 2 + ((k + clock / 20) % 2);
            crop[k] = k % 4;
            growth[k] = (clock / 10 + k) % DAYS[k % 4];
            wet[k] = k % 2 == 0;
            dry[k] = 0;
            drawPlot(g, k, x0 + k * s, y + (h - s) / 2, s);
        }
    }
}
