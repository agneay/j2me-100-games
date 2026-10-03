package cornershop;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Corner Shop: buy stock at wholesale, set your prices, then watch the
 * customers come in. Weather, price and reputation drive demand.
 */
public class ShopGame extends Game {
    private static final String[] ITEM = { "Bread", "Milk", "Soda", "Candy", "Ice cream", "Umbrella" };
    private static final int[] COST = { 2, 2, 3, 1, 4, 7 };
    private static final int[] BASE = { 4, 4, 6, 3, 8, 14 };
    private static final int[] SHELF_LIFE = { 2, 3, 99, 99, 1, 99 };
    private static final int[] ICOL = { 0xD7A86E, 0xF5F5F5, 0xE53935, 0xF06292, 0x80DEEA, 0x5C6BC0 };
    private static final String[] WEATHER = { "Sunny", "Hot", "Cloudy", "Rainy" };
    private static final int DAYS = 30, PLAN = 0, OPEN = 1, REPORT = 2, MAXCUST = 6;

    private final int[] stock = new int[6], price = new int[6], buy = new int[6], age = new int[6];
    private int cash, day, weather, phase, row, col, rep, dayTick, served, lost, revenue, spoiled, costToday;
    private final int[] cx = new int[MAXCUST], want = new int[MAXCUST], cstate = new int[MAXCUST], ccol = new int[MAXCUST];
    private String bubble = "";
    private int bubbleT;

    protected String name() { return "Corner Shop"; }

    protected String[] help() {
        return new String[] {
            "Run a corner shop for 30 days. Each morning, buy stock at wholesale cost and set your selling prices, then open the doors.",
            "Demand follows the weather: ice cream sells on hot days, umbrellas when it rains. Overpricing scares customers away; empty shelves hurt your reputation.",
            "Bread, milk and ice cream spoil if they sit on the shelf too long. Turn $50 into as much money as you can - $500 is a great result.",
            "- Controls",
            "2/8: choose item  4/6: change value",
            "#: switch buy / price column",
            "5: open shop (and fast-forward)",
        };
    }

    protected String formatScore(int s) { return "$" + s; }

    protected int accent() { return 0xF4A261; }

    protected void newGame() {
        cash = 50;
        day = 1;
        rep = 50;
        for (int i = 0; i < 6; i++) {
            stock[i] = 0;
            price[i] = BASE[i];
            buy[i] = 0;
            age[i] = 0;
        }
        row = 0;
        col = 0;
        startDay();
    }

    private void startDay() {
        weather = Rnd.nextInt(4);
        phase = PLAN;
        for (int i = 0; i < 6; i++) buy[i] = 0;
    }

    private int planCost() {
        int c = 0;
        for (int i = 0; i < 6; i++) c += buy[i] * COST[i];
        return c;
    }

    /** Probability weight that a customer wants item i today. */
    private int demand(int i) {
        int d = 10;
        if (i == 4) d = weather == 1 ? 40 : (weather == 0 ? 18 : 3);
        if (i == 5) d = weather == 3 ? 40 : (weather == 2 ? 8 : 1);
        if (i == 2 && weather <= 1) d += 10;
        if (i <= 1) d += 6;
        return d;
    }

    private int maxPriceTolerance(int i) {
        return BASE[i] * (130 + rep / 2) / 100;
    }

    protected void update() {
        if (bubbleT > 0) bubbleT--;
        switch (phase) {
            case PLAN:
                if ((pressed & K_UP) != 0) row = (row + 5) % 6;
                if ((pressed & K_DOWN) != 0) row = (row + 1) % 6;
                if ((pressed & K_POUND) != 0) col ^= 1;
                int delta = ((pressed & K_RIGHT) != 0 ? 1 : 0) - ((pressed & K_LEFT) != 0 ? 1 : 0);
                if (delta != 0) {
                    if (col == 0) {
                        int nb = Math.max(0, buy[row] + delta);
                        int extra = (nb - buy[row]) * COST[row];
                        if (planCost() + extra <= cash) buy[row] = nb;
                        else Sfx.bad();
                    } else {
                        price[row] = Math.max(1, Math.min(BASE[row] * 3, price[row] + delta));
                    }
                    Sfx.click();
                }
                if ((pressed & K_FIRE) != 0) openShop();
                break;
            case OPEN:
                simulate((held & K_FIRE) != 0 ? 3 : 1);
                break;
            default:
                if ((pressed & K_FIRE) != 0) {
                    day++;
                    score = cash;
                    if (day > DAYS) {
                        headline = "$" + cash + " AFTER 30 DAYS";
                        endGame(cash >= 200);
                        return;
                    }
                    startDay();
                }
                break;
        }
        score = cash;
    }

    private void openShop() {
        costToday = planCost();
        cash -= costToday;
        for (int i = 0; i < 6; i++) {
            if (buy[i] > 0) {
                if (stock[i] == 0) age[i] = 0;
                stock[i] += buy[i];
            }
        }
        phase = OPEN;
        dayTick = 0;
        served = lost = revenue = 0;
        for (int k = 0; k < MAXCUST; k++) cstate[k] = 0;
        Sfx.tone(84, 60);
    }

    private void simulate(int speed) {
        for (int s = 0; s < speed; s++) {
            dayTick++;
            int traffic = 6 + rep / 10 + (weather == 3 ? -1 : 2);
            if (dayTick < 240 && Rnd.nextInt(100) < traffic) spawnCustomer();
            for (int k = 0; k < MAXCUST; k++) {
                if (cstate[k] == 0) continue;
                if (cstate[k] == 1) { // walking to counter
                    cx[k] += 2;
                    if (cx[k] >= W * 55 / 100) serve(k);
                } else { // leaving
                    cx[k] -= 3;
                    if (cx[k] < -8) cstate[k] = 0;
                }
            }
            if (dayTick >= 300) {
                endDay();
                return;
            }
        }
    }

    private void spawnCustomer() {
        for (int k = 0; k < MAXCUST; k++) {
            if (cstate[k] != 0) continue;
            int total = 0;
            for (int i = 0; i < 6; i++) total += demand(i);
            int r = Rnd.nextInt(total), pick = 0;
            for (int i = 0; i < 6; i++) {
                r -= demand(i);
                if (r < 0) { pick = i; break; }
            }
            want[k] = pick;
            cx[k] = -6;
            cstate[k] = 1;
            ccol[k] = 0x404040 + Rnd.nextInt(0xBFBFBF);
            return;
        }
    }

    private void serve(int k) {
        int i = want[k];
        cstate[k] = 2;
        if (stock[i] == 0) {
            lost++;
            rep = Math.max(0, rep - 2);
            bubble = "No " + ITEM[i] + "?!";
            bubbleT = 20;
            Sfx.tone(45, 30);
            return;
        }
        int tol = maxPriceTolerance(i);
        if (price[i] > tol || (price[i] > BASE[i] && Rnd.nextInt(100) < (price[i] - BASE[i]) * 100 / Math.max(1, tol - BASE[i] + 1))) {
            lost++;
            bubble = "Too pricey!";
            bubbleT = 20;
            Sfx.tone(50, 20);
            return;
        }
        int qty = Math.min(stock[i], 1 + (Rnd.chance(25) ? 1 : 0));
        stock[i] -= qty;
        cash += qty * price[i];
        revenue += qty * price[i];
        served++;
        if (price[i] <= BASE[i]) rep = Math.min(100, rep + 1);
        bubble = "+$" + qty * price[i];
        bubbleT = 12;
        Sfx.tone(88, 20);
    }

    private void endDay() {
        spoiled = 0;
        for (int i = 0; i < 6; i++) {
            if (stock[i] == 0) continue;
            age[i]++;
            if (age[i] >= SHELF_LIFE[i]) {
                spoiled += stock[i];
                stock[i] = 0;
                age[i] = 0;
            }
        }
        phase = REPORT;
        Sfx.good();
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x3E2723);
        g.fillRect(0, 0, W, H);
        g.setColor(0x1B120F);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString("$" + cash, 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString("Day " + day + "/" + DAYS, W / 2, 1, Gfx.TC);
        g.setColor(0x81D4FA);
        g.drawString(WEATHER[weather], W - 2, 1, Gfx.TR);
        if (phase == PLAN) drawPlan(g, hud);
        else drawShop(g, hud);
    }

    private void drawPlan(Graphics g, int hud) {
        int lh = Gfx.SMALL.getHeight() + 3;
        int y0 = hud + lh + 2;
        g.setFont(Gfx.SMALL);
        g.setColor(0xBCAAA4);
        int cStock = W * 46 / 100, cBuy = W * 64 / 100, cPrice = W - 3;
        g.drawString("Item", 3, hud + 2, Gfx.TL);
        g.drawString("Have", cStock, hud + 2, Gfx.TR);
        g.drawString(col == 0 ? "[Buy]" : "Buy", cBuy + 10, hud + 2, Gfx.TR);
        g.drawString(col == 1 ? "[Price]" : "Price", cPrice, hud + 2, Gfx.TR);
        for (int i = 0; i < 6; i++) {
            int y = y0 + i * lh;
            if (i == row) {
                g.setColor(0x5D4037);
                g.fillRect(0, y - 1, W, lh);
            }
            g.setColor(ICOL[i]);
            g.fillRect(3, y + 2, 4, 4);
            g.setFont(Gfx.SMALL);
            g.setColor(0xFFFFFF);
            String nm = ITEM[i];
            if (W < 160 && nm.length() > 6) nm = nm.substring(0, 6);
            g.drawString(nm, 9, y, Gfx.TL);
            g.drawString(String.valueOf(stock[i]), cStock, y, Gfx.TR);
            g.setColor(i == row && col == 0 ? 0xFFEB3B : 0xE0E0E0);
            g.drawString("+" + buy[i], cBuy + 10, y, Gfx.TR);
            int pc = price[i] > maxPriceTolerance(i) ? 0xFF5252 : (price[i] > BASE[i] ? 0xFFCC80 : 0xA5D6A7);
            g.setColor(i == row && col == 1 ? 0xFFEB3B : pc);
            g.drawString("$" + price[i], cPrice, y, Gfx.TR);
        }
        int y = y0 + 6 * lh + 2;
        g.setColor(0xBCAAA4);
        g.drawString("Unit cost $" + COST[row] + "   Bill $" + planCost(), W / 2, y, Gfx.TC);
        g.drawString("Reputation " + rep + "%", W / 2, y + lh, Gfx.TC);
        Gfx.hint(g, "4/6 adjust  # column  5 open", W, H);
    }

    private void drawShop(Graphics g, int hud) {
        int floor = H * 70 / 100;
        g.setColor(0xFFF3E0);
        g.fillRect(0, hud, W, floor - hud);
        g.setColor(0xA1887F);
        g.fillRect(0, floor, W, H - floor);
        // window showing the weather
        int wx = W / 12, wy = hud + 6, ww = W / 4, wh = (floor - hud) / 3;
        g.setColor(weather == 3 ? 0x78909C : (weather == 2 ? 0xB0BEC5 : 0x81D4FA));
        g.fillRect(wx, wy, ww, wh);
        if (weather == 3) {
            g.setColor(0xE3F2FD);
            for (int k = 0; k < 8; k++) g.drawLine(wx + (k * 7 + clock) % ww, wy + (k * 5 + clock * 2) % wh, wx + (k * 7 + clock) % ww - 1, wy + (k * 5 + clock * 2) % wh + 3);
        } else if (weather <= 1) {
            g.setColor(0xFFEB3B);
            Gfx.disc(g, wx + ww / 2, wy + wh / 2, wh / 4);
        }
        g.setColor(0x6D4C41);
        g.drawRect(wx, wy, ww, wh);
        // shelves
        int sx = W * 45 / 100;
        for (int r = 0; r < 3; r++) {
            int y = hud + 8 + r * (floor - hud) / 4;
            g.setColor(0x8D6E63);
            g.fillRect(sx, y + 8, W - sx - 4, 3);
            for (int i = 0; i < 2; i++) {
                int item = r * 2 + i;
                int n = Math.min(stock[item], 6);
                g.setColor(ICOL[item]);
                for (int k = 0; k < n; k++) g.fillRect(sx + i * (W - sx) / 2 + k * 4, y + 2, 3, 6);
            }
        }
        // counter and keeper
        int ctr = W * 60 / 100;
        g.setColor(0x6D4C41);
        g.fillRect(ctr, floor - 14, 12, 14);
        g.setColor(0xFFCC80);
        Gfx.disc(g, ctr + 18, floor - 22, 4);
        g.setColor(0x2E7D32);
        g.fillRect(ctr + 14, floor - 18, 8, 10);
        for (int k = 0; k < MAXCUST; k++) {
            if (cstate[k] == 0) continue;
            int x = cx[k], y = floor - 4;
            g.setColor(ccol[k]);
            g.fillRect(x - 3, y - 12, 6, 8);
            g.setColor(0xFFCC80);
            Gfx.disc(g, x, y - 15, 3);
            g.setColor(0x37474F);
            int step = (clock / 3 + k) & 1;
            g.fillRect(x - 3 + step, y - 4, 2, 4);
            g.fillRect(x + 1 - step, y - 4, 2, 4);
            if (cstate[k] == 2 && want[k] >= 0) {
                g.setColor(ICOL[want[k]]);
                g.fillRect(x + 3, y - 10, 3, 4);
            }
        }
        if (bubbleT > 0) Gfx.shadowText(g, bubble, ctr - 4, floor - 40, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
        if (phase == OPEN) {
            Gfx.bar(g, 4, H - 10, W - 8, 6, dayTick, 300, 0xFFB74D, 0x4E342E);
            g.setFont(Gfx.SMALL);
            g.setColor(0xFFFFFF);
            g.drawString("Served " + served + "  hold 5: faster", W / 2, floor + 2, Gfx.TC);
        } else {
            int bw = W - 16, bh = (Gfx.SMALL.getHeight() + 2) * 6 + 6;
            int by = (H - bh) / 2;
            Gfx.panel(g, 8, by, bw, bh, 0x1B120F, 0xF4A261);
            int lh = Gfx.SMALL.getHeight() + 2;
            g.setFont(Gfx.SMALL_B);
            g.setColor(0xF4A261);
            g.drawString("End of day " + day, W / 2, by + 3, Gfx.TC);
            g.setFont(Gfx.SMALL);
            g.setColor(0xFFFFFF);
            g.drawString("Sales  +$" + revenue, W / 2, by + 3 + lh, Gfx.TC);
            g.drawString("Stock  -$" + costToday, W / 2, by + 3 + lh * 2, Gfx.TC);
            g.drawString("Served " + served + "  lost " + lost, W / 2, by + 3 + lh * 3, Gfx.TC);
            g.drawString(spoiled > 0 ? spoiled + " items spoiled" : "Nothing spoiled", W / 2, by + 3 + lh * 4, Gfx.TC);
            g.setColor(0xFFD54F);
            g.drawString("5: next morning", W / 2, by + 3 + lh * 5, Gfx.TC);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0xFFF3E0);
        g.fillRect(x + w / 4, y + h / 4, w / 2, h * 3 / 4);
        g.setColor(0xE53935);
        for (int k = 0; k < 6; k++) {
            g.setColor(k % 2 == 0 ? 0xE53935 : 0xFFFFFF);
            g.fillRect(x + w / 4 + k * w / 12, y + h / 4 - 6, w / 12, 6);
        }
        g.setColor(0x6D4C41);
        g.fillRect(x + w / 2 - 5, y + h - 14, 10, 14);
        int cxp = x + (clock * 2) % (w + 20) - 10;
        g.setColor(0x1E88E5);
        g.fillRect(cxp - 3, y + h - 12, 6, 8);
        g.setColor(0xFFCC80);
        Gfx.disc(g, cxp, y + h - 15, 3);
    }
}
