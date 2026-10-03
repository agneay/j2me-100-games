package kingdomledger;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Kingdom Ledger: rule a small kingdom for ten years. Each year decide how
 * much land to trade, how much grain to feed your people and how many acres
 * to sow. Harvests, rats, plague and newcomers do the rest.
 */
public class LedgerGame extends Game {
    private static final int YEARS = 10, PLAN = 0, REPORT = 1;
    private static final String[] FIELD = { "Buy/sell land", "Feed people", "Sow acres", "End the year" };

    private int year, people, grain, land, price;
    private int trade, feed, sow, sel, phase;
    private int totalStarved, starvedPct;
    private final Vector report = new Vector();

    protected String name() { return "Kingdom Ledger"; }

    protected String[] help() {
        return new String[] {
            "You rule a small kingdom for ten years. Every year make three decisions:",
            "Land: buy or sell acres at this year's price (in bushels of grain). Feed: each person needs 20 bushels a year or they starve. Sow: each acre needs 1 bushel of seed, and each person can work 10 acres.",
            "Harvests vary, rats raid the granary and plague may strike. Starve too many in one year and you will be overthrown.",
            "At the end you are judged on land per person and how many died.",
            "- Controls",
            "2/8: choose line",
            "4/6: change by 1  1/3: by 10",
            "5 on 'End the year' (or #)",
        };
    }

    protected int accent() { return 0xC9A227; }

    protected void newGame() {
        year = 1;
        people = 100;
        grain = 2800;
        land = 1000;
        totalStarved = 0;
        newPrice();
        startPlan();
    }

    private void newPrice() {
        price = Rnd.range(17, 26);
    }

    private void startPlan() {
        phase = PLAN;
        trade = 0;
        feed = Math.min(grain, people * 20);
        sow = Math.min(Math.min(land, people * 10), Math.max(0, grain - feed));
        sel = 0;
    }

    private int grainLeft() {
        return grain - trade * price - feed - sow;
    }

    private void adjust(int field, int delta) {
        if (field == 0) {
            int nt = trade + delta;
            if (land + nt < 0) return;
            trade = nt;
        } else if (field == 1) {
            feed = Math.max(0, feed + delta * 10);
        } else if (field == 2) {
            sow = Math.max(0, Math.min(Math.min(land + trade, people * 10), sow + delta * 10));
        }
        Sfx.click();
    }

    protected void update() {
        if (phase == REPORT) {
            if ((pressed & (K_FIRE | K_POUND)) != 0) {
                if (state != PLAY) return;
                year++;
                if (year > YEARS) {
                    finish();
                    return;
                }
                startPlan();
            }
            return;
        }
        if ((pressed & K_UP) != 0) sel = (sel + 3) % 4;
        if ((pressed & K_DOWN) != 0) sel = (sel + 1) % 4;
        int d = 0;
        if ((pressed & K_RIGHT) != 0) d = 1;
        if ((pressed & K_LEFT) != 0) d = -1;
        if (digit(3)) d = 10;
        if (digit(1)) d = -10;
        if (d != 0 && sel < 3) adjust(sel, d);
        if (((pressed & K_FIRE) != 0 && sel == 3) || (pressed & K_POUND) != 0) {
            if (grainLeft() < 0) {
                Sfx.bad();
                return;
            }
            endYear();
        }
    }

    private void endYear() {
        report.removeAllElements();
        land += trade;
        grain = grainLeft();
        int fed = feed / 20;
        int starved = Math.max(0, people - fed);
        int yieldPer = Rnd.range(1, 6);
        int harvest = sow * yieldPer;
        grain += harvest;
        report.addElement("Year " + year + " report");
        report.addElement("Harvest: " + yieldPer + " per acre, " + harvest + " bushels.");
        if (Rnd.chance(35)) {
            int eaten = grain * Rnd.range(10, 30) / 100;
            grain -= eaten;
            report.addElement("Rats ate " + eaten + " bushels!");
        }
        starvedPct = people > 0 ? starved * 100 / people : 0;
        people -= starved;
        totalStarved += starved;
        if (starved > 0) report.addElement(starved + " people starved.");
        if (starvedPct > 45) {
            phase = REPORT;
            headline = "OVERTHROWN";
            score = 0;
            report.addElement("The people rise up and depose you!");
            Sfx.lose();
            endGame(false);
            return;
        }
        int newcomers = starved > 0 ? 0 : (land * 2 / 100 + grain / 200) / 2 + Rnd.range(1, 5);
        people += newcomers;
        if (newcomers > 0) report.addElement(newcomers + " newcomers arrived.");
        if (Rnd.chance(12)) {
            people /= 2;
            report.addElement("A plague killed half the people!");
            Sfx.bad();
        }
        report.addElement("Population " + people + ", land " + land + ", grain " + grain + ".");
        newPrice();
        report.addElement("Land now costs " + price + " bushels/acre.");
        phase = REPORT;
        if (people <= 0) {
            headline = "KINGDOM EMPTY";
            endGame(false);
            return;
        }
        Sfx.good();
    }

    private void finish() {
        int perPerson = land / Math.max(1, people);
        int avgStarve = totalStarved * 100 / Math.max(1, people * YEARS);
        score = Math.max(0, perPerson * 50 + people * 2 - totalStarved * 3);
        if (avgStarve <= 3 && perPerson >= 10) headline = "A LEGENDARY RULER";
        else if (avgStarve <= 10 && perPerson >= 9) headline = "A FAIR RULER";
        else headline = "A POOR RULER";
        endGame(perPerson >= 9);
    }

    protected void draw(Graphics g) {
        g.setColor(0x1E1408);
        g.fillRect(0, 0, W, H);
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x5D1F1F);
        g.fillRect(0, 0, W, hud + 2);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xC9A227);
        g.drawString("Year " + Math.min(year, YEARS) + "/" + YEARS, W / 2, 1, Gfx.TC);
        int lh = Gfx.SMALL.getHeight() + 2;
        int y = hud + 6;
        String[] stat = { "People", "Grain", "Land", "Price" };
        int[] val = { people, grain, land, price };
        g.setFont(Gfx.SMALL);
        for (int i = 0; i < 4; i++) {
            int x = (i % 2) * (W / 2) + 4, yy = y + (i / 2) * lh;
            g.setColor(0xBCAAA4);
            g.drawString(stat[i], x, yy, Gfx.TL);
            g.setColor(0xFFF3CD);
            g.drawString(String.valueOf(val[i]), x + W / 2 - 8, yy, Gfx.TR);
        }
        y += lh * 2 + 4;
        g.setColor(0x6D4C41);
        g.drawLine(4, y - 2, W - 4, y - 2);
        if (phase == REPORT) {
            g.setFont(Gfx.SMALL);
            for (int i = 0; i < report.size(); i++) {
                Vector lines = Gfx.wrap((String) report.elementAt(i), Gfx.SMALL, W - 8);
                for (int k = 0; k < lines.size(); k++) {
                    g.setColor(i == 0 ? 0xC9A227 : 0xFFF3CD);
                    g.drawString((String) lines.elementAt(k), 4, y, Gfx.TL);
                    y += lh - 1;
                }
            }
            Gfx.hint(g, "5: continue", W, H);
            return;
        }
        int[] fv = { trade, feed, sow };
        for (int i = 0; i < 4; i++) {
            int yy = y + i * (lh + 3);
            boolean on = i == sel;
            Gfx.panel(g, 3, yy, W - 6, lh + 1, on ? 0x4E342E : 0x2B1D10, on ? 0xC9A227 : 0x4E342E);
            g.setFont(Gfx.SMALL_B);
            g.setColor(on ? 0xFFFFFF : 0xD7CCC8);
            g.drawString(FIELD[i], 6, yy + 1, Gfx.TL);
            if (i < 3) {
                g.setColor(0xFFD54F);
                String v = (i == 0 && fv[0] > 0 ? "+" : "") + fv[i];
                g.drawString(v, W - 6, yy + 1, Gfx.TR);
            }
        }
        y += 4 * (lh + 3) + 2;
        int left = grainLeft();
        g.setFont(Gfx.SMALL);
        g.setColor(left < 0 ? 0xFF5252 : 0xA5D6A7);
        g.drawString("Grain left: " + left, W / 2, y, Gfx.TC);
        g.setColor(0x8D6E63);
        int need = people * 20;
        g.drawString(feed < need ? "Food for " + feed / 20 + " of " + people : "Everyone is fed", W / 2, y + lh, Gfx.TC);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int cx = x + w / 2, base = y + h;
        g.setColor(0x6D4C41);
        g.fillRect(cx - w / 4, base - h / 2, w / 2, h / 2);
        g.fillRect(cx - w / 4 - 4, base - h * 3 / 4, 10, h * 3 / 4);
        g.fillRect(cx + w / 4 - 6, base - h * 3 / 4, 10, h * 3 / 4);
        g.setColor(0x3E2723);
        g.fillRoundRect(cx - 5, base - h / 4, 10, h / 4, 6, 6);
        g.setColor(0xC62828);
        int wave = (clock / 5) & 1;
        g.fillTriangle(cx - w / 4 + 1, base - h * 3 / 4, cx - w / 4 + 1, base - h * 3 / 4 + 5, cx - w / 4 + 8 + wave, base - h * 3 / 4 + 2);
        g.setColor(0xC9A227);
        for (int k = 0; k < 5; k++) g.fillRect(x + 4 + k * 5, base - 3 - (k % 2) * 2, 4, 3 + (k % 2) * 2);
    }
}
