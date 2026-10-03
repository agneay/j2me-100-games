package lemonstand;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Lemon Stand: a 14-day lemonade business. Each morning read the forecast,
 * set the recipe strength, price and batch size, then watch the day play out.
 * Money is held in cents.
 */
public class LemonGame extends Game {
    private static final String[] SKY = { "Sunny", "Hot", "Cloudy", "Rainy", "Heatwave" };
    private static final int[] SKY_DEMAND = { 100, 140, 70, 35, 190 };
    private static final int DAYS = 14;

    private int day, cash, price, cups, sweet, sky, forecast, row, phase, tick, sold, made, customers, served, rep;
    private int dayIncome, dayCost, walker, walkerX, walkerBuys;
    private String event = "";
    private static final int PLAN = 0, OPEN = 1, REPORT = 2;

    protected String name() { return "Lemon Stand"; }

    protected String[] help() {
        return new String[] {
            "Run a lemonade stand for 14 days. Each morning check the forecast, choose recipe strength, price and how many cups to make, then open up.",
            "Hot days bring crowds; rain keeps people home. Strong, fairly priced lemonade builds your reputation, which brings more customers later. Unsold cups are thrown away.",
            "Finish with as much cash as you can.",
            "- Controls",
            "2/8: choose setting",
            "4/6: change value",
            "5: open the stand / next day",
        };
    }

    protected int accent() { return 0xFFEB3B; }

    protected void newGame() {
        day = 1;
        cash = 2000;
        price = 50;
        cups = 30;
        sweet = 2;
        rep = 50;
        row = 0;
        forecast = Rnd.nextInt(3);
        phase = PLAN;
    }

    private int cupCost() {
        return 8 + sweet * 4;
    }

    private void openStand() {
        int cost = cups * cupCost();
        if (cost > cash) {
            Sfx.bad();
            event = "Not enough cash";
            return;
        }
        cash -= cost;
        dayCost = cost;
        made = cups;
        sold = 0;
        dayIncome = 0;
        // actual weather usually matches the forecast
        sky = Rnd.chance(75) ? forecast : shiftSky(forecast);
        int base = SKY_DEMAND[sky] * (60 + rep) / 100;
        // price sensitivity: demand falls off above ~50c, faster when it's cold
        int pf = 100 - (price - 40) * (sky == 1 || sky == 4 ? 1 : 2);
        customers = Math.max(2, base * Math.max(5, pf) / 100 / 2 + Rnd.nextInt(6));
        served = 0;
        tick = 0;
        walker = 0;
        walkerX = -10;
        phase = OPEN;
    }

    private int shiftSky(int f) {
        int d = Rnd.chance(50) ? 1 : -1;
        int[] order = { 3, 2, 0, 1, 4 };
        int i = 0;
        for (int k = 0; k < 5; k++) if (order[k] == f) i = k;
        i = Math.max(0, Math.min(4, i + d));
        return order[i];
    }

    private void closeDay() {
        // reputation: rewards strong lemonade at fair prices and not running out
        int taste = sweet == 2 ? 6 : (sweet == 3 ? 4 : (sweet == 1 ? 0 : -6));
        int fair = price <= 60 ? 4 : (price <= 90 ? 0 : -6);
        int short_ = served > sold ? -3 : 1;
        rep = Math.max(0, Math.min(100, rep + taste + fair + short_));
        phase = REPORT;
        Sfx.play(new int[] { 72, 120, 76, 120, 79, 200 });
    }

    protected void update() {
        if (phase == PLAN) {
            if ((pressed & K_UP) != 0) row = (row + 2) % 3;
            if ((pressed & K_DOWN) != 0) row = (row + 1) % 3;
            int dv = (pressed & K_LEFT) != 0 ? -1 : ((pressed & K_RIGHT) != 0 ? 1 : 0);
            if (dv == 0 && (held & (K_LEFT | K_RIGHT)) != 0 && frame % 3 == 0 && row != 0) dv = (held & K_LEFT) != 0 ? -1 : 1;
            if (dv != 0) {
                if (row == 0) sweet = Math.max(0, Math.min(4, sweet + dv));
                else if (row == 1) price = Math.max(10, Math.min(200, price + dv * 5));
                else cups = Math.max(0, Math.min(200, cups + dv * 5));
                event = "";
                Sfx.click();
            }
            if ((pressed & K_FIRE) != 0) openStand();
        } else if (phase == OPEN) {
            tick++;
            if (walker == 0) {
                if (served >= customers || tick > 400) {
                    closeDay();
                    return;
                }
                walker = 1;
                walkerX = -8;
                walkerBuys = sold < made ? 1 : 0;
                served++;
            } else {
                walkerX += Math.max(2, W / 40);
                if (walker == 1 && walkerX >= W / 2 - 8) {
                    walker = 2;
                    if (walkerBuys == 1) {
                        sold++;
                        cash += price;
                        dayIncome += price;
                        score = cash;
                        Sfx.tone(84, 15);
                    }
                }
                if (walkerX > W + 8) walker = 0;
            }
            if ((pressed & K_FIRE) != 0) {
                // fast-forward the rest of the day
                while (served < customers) {
                    served++;
                    if (sold < made) {
                        sold++;
                        cash += price;
                        dayIncome += price;
                    }
                }
                walker = 0;
                score = cash;
            }
        } else {
            if ((pressed & K_FIRE) != 0) {
                day++;
                if (day > DAYS) {
                    score = cash;
                    headline = cash > 2000 ? "PROFIT $" + money(cash - 2000) : "LOSS $" + money(2000 - cash);
                    endGame(cash > 2000);
                    return;
                }
                if (cash < 8) {
                    score = cash;
                    headline = "BANKRUPT";
                    endGame(false);
                    return;
                }
                forecast = Rnd.nextInt(5);
                if (cups * cupCost() > cash) cups = cash / cupCost() / 5 * 5;
                phase = PLAN;
            }
        }
    }

    private static String money(int c) {
        int d = c / 100, r = c % 100;
        return d + "." + (r < 10 ? "0" : "") + r;
    }

    protected boolean hasScore() { return true; }

    protected String formatScore(int s) { return "$" + money(s); }

    private void drawSky(Graphics g, int s, int x, int y, int r) {
        if (s == 3) {
            g.setColor(0x90A4AE);
            g.fillArc(x - r, y - r / 2, r * 2, r, 0, 360);
            g.setColor(0x4FC3F7);
            for (int k = -1; k <= 1; k++) g.drawLine(x + k * r / 2, y + r / 2 + 1, x + k * r / 2 - 2, y + r);
        } else if (s == 2) {
            g.setColor(0xFFD54F);
            Gfx.disc(g, x - r / 3, y - r / 3, r / 2);
            g.setColor(0xCFD8DC);
            g.fillArc(x - r, y - r / 2, r * 2, r, 0, 360);
        } else {
            g.setColor(s == 4 ? 0xFF7043 : (s == 1 ? 0xFFA726 : 0xFFD54F));
            Gfx.disc(g, x, y, r / 2 + (s == 4 ? 2 : 0));
            for (int a = 0; a < 8; a++) {
                int dx = (a == 0 || a == 1 || a == 7) ? 1 : ((a == 3 || a == 4 || a == 5) ? -1 : 0);
                int dy = (a == 1 || a == 2 || a == 3) ? 1 : ((a == 5 || a == 6 || a == 7) ? -1 : 0);
                g.drawLine(x + dx * (r / 2 + 2), y + dy * (r / 2 + 2), x + dx * r, y + dy * r);
            }
        }
    }

    private void drawStand(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x8D6E63);
        g.fillRect(x, y + h / 3, w, h * 2 / 3);
        g.setColor(0xFFF176);
        g.fillRect(x - 2, y, w + 4, h / 3);
        for (int k = 0; k < w + 4; k += 8) {
            g.setColor(0xFFFFFF);
            g.fillRect(x - 2 + k, y, 4, h / 3);
        }
        g.setColor(0x5D4037);
        g.fillRect(x + 2, y + h / 3, 2, h * 2 / 3);
        g.fillRect(x + w - 4, y + h / 3, 2, h * 2 / 3);
        g.setColor(0xFFEB3B);
        g.fillRect(x + w / 2 - 3, y + h / 3 - 6, 6, 6);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int fh = Gfx.SMALL.getHeight();
        g.setColor(phase == OPEN && sky == 3 ? 0x78909C : 0x81D4FA);
        g.fillRect(0, 0, W, H);
        g.setColor(0x7CB342);
        g.fillRect(0, H * 2 / 3, W, H / 3);
        g.setColor(0x000000);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFEB3B);
        g.drawString("Day " + Math.min(day, DAYS) + "/" + DAYS, 2, 1, Gfx.TL);
        g.setColor(0xA5D6A7);
        g.drawString("$" + money(cash), W - 2, 1, Gfx.TR);
        int sw = Math.max(30, W / 3), sh = sw * 2 / 3;
        int sx = W / 2 - sw / 2, sy = H * 2 / 3 - sh + 2;
        if (phase == PLAN) {
            drawSky(g, forecast, W - 16, hud + 14, 10);
            Gfx.text(g, "Forecast: " + SKY[forecast], 4, hud + 4, Gfx.TL, Gfx.SMALL_B, 0x0D47A1);
            Gfx.text(g, "Reputation " + rep + "%", 4, hud + 4 + fh, Gfx.TL, Gfx.SMALL, 0x0D47A1);
            int py = hud + fh * 3;
            int rh = fh + 6;
            String[] labels = { "Lemon:sugar", "Price", "Cups" };
            String[] sweets = { "Sour", "Tart", "Balanced", "Sweet", "Syrupy" };
            String[] vals = { sweets[sweet], money(price), String.valueOf(cups) };
            for (int k = 0; k < 3; k++) {
                int y = py + k * rh;
                Gfx.panel(g, 4, y, W - 8, rh - 2, k == row ? 0xFFF59D : 0xFFFDE7, k == row ? 0xF57F17 : 0xBDBDBD);
                Gfx.text(g, labels[k], 8, y + 2, Gfx.TL, Gfx.SMALL, 0x5D4037);
                Gfx.text(g, (k == row ? "< " : "") + vals[k] + (k == row ? " >" : ""), W - 8, y + 2, Gfx.TR, Gfx.SMALL_B, 0x3E2723);
            }
            int cost = cups * cupCost();
            int cy = py + rh * 3 + 2;
            Gfx.text(g, "Cost $" + money(cost) + " (" + cupCost() + "c/cup)", W / 2, cy, Gfx.TC, Gfx.SMALL, cost > cash ? 0xC62828 : 0x1B5E20);
            Gfx.text(g, event.length() > 0 ? event : "5: open the stand", W / 2, H - fh - 2, Gfx.TC, Gfx.SMALL_B, 0x33691E);
            return;
        }
        drawSky(g, sky, W - 16, hud + 14, 10);
        drawStand(g, sx, sy, sw, sh);
        if (phase == OPEN && walker != 0) {
            int wy = H * 2 / 3 + 6;
            g.setColor(0x5C6BC0 + (served * 0x2311) % 0x404040);
            g.fillRect(walkerX - 3, wy, 6, 10);
            g.setColor(0xFFCC80);
            Gfx.disc(g, walkerX, wy - 3, 3);
            if (walker == 2 && walkerBuys == 1) {
                g.setColor(0xFFF176);
                g.fillRect(walkerX + 3, wy + 2, 3, 4);
            }
        }
        Gfx.text(g, SKY[sky] + " day", 4, hud + 4, Gfx.TL, Gfx.SMALL_B, 0x0D47A1);
        Gfx.text(g, "Sold " + sold + "/" + made, 4, hud + 4 + fh, Gfx.TL, Gfx.SMALL, 0x0D47A1);
        if (phase == OPEN) {
            Gfx.text(g, "5: skip to evening", W / 2, H - fh - 2, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
        } else {
            int bw = W - 16, bh = fh * 5 + 8, by = H - bh - 4;
            Gfx.panel(g, 8, by, bw, bh, 0xFFFDE7, 0xF9A825);
            Gfx.text(g, "Evening report", W / 2, by + 2, Gfx.TC, Gfx.SMALL_B, 0x5D4037);
            Gfx.text(g, "Income $" + money(dayIncome), 12, by + 2 + fh, Gfx.TL, Gfx.SMALL, 0x2E7D32);
            Gfx.text(g, "Costs $" + money(dayCost), 12, by + 2 + fh * 2, Gfx.TL, Gfx.SMALL, 0xC62828);
            int profit = dayIncome - dayCost;
            Gfx.text(g, (profit >= 0 ? "Profit $" : "Loss $") + money(Math.abs(profit)), 12, by + 2 + fh * 3, Gfx.TL, Gfx.SMALL_B, profit >= 0 ? 0x2E7D32 : 0xC62828);
            Gfx.text(g, served > sold ? "Sold out! " + (served - sold) + " missed" : "Rep " + rep + "%  5: next", 12, by + 2 + fh * 4, Gfx.TL, Gfx.SMALL, 0x5D4037);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int sw = Math.min(w / 2, h * 3 / 2), sh = sw * 2 / 3;
        drawStand(g, x + w / 2 - sw / 2, y + h - sh, sw, sh);
        drawSky(g, (clock / 30) % 5 == 3 ? 0 : (clock / 30) % 5, x + w - 12, y + 10, 8);
    }
}
