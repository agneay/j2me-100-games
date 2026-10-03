package dinerrush;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Diner Rush: a time-management restaurant. The dining room is laid out like
 * the keypad: tables 1-6 sit on keys 1-6, the kitchen hatch on key 8.
 * Press a key and your waiter hurries there to take orders, serve and clear.
 */
public class DinerGame extends Game {
    private static final int T = 6;
    // table states
    private static final int EMPTY = 0, READING = 1, WANTS_ORDER = 2, WAITING_FOOD = 3, EATING = 4, DIRTY = 5;
    private static final int[] DISH_COL = { 0xFFB300, 0xE53935, 0x43A047, 0x8D6E63 };

    private final int[] st = new int[T], timer = new int[T], patience = new int[T], dish = new int[T], cooking = new int[T];
    // cooking: 0 none, 1 on notepad, 2 cooking (timer), 3 ready at pass, 4 in hands
    private final int[] cookT = new int[T];
    private int wx, wy, target = -1, money, strikes, dayT, spawnT, served, carryN, combo;
    private String pop = "";
    private int popT;

    protected String name() { return "Diner Rush"; }

    protected String[] help() {
        return new String[] {
            "Run a busy diner single-handed. The room is laid out like your keypad: tables 1-6 are on keys 1-6 and the kitchen hatch is key 8 (or 0).",
            "Press a table's number to walk there. Take orders when guests raise their hands (!), drop orders at the kitchen, pick up finished dishes there (two at a time), serve them, and clear tables after guests leave.",
            "Every guest has a patience bar. Let three guests walk out angry and you're done. Survive the 3-minute shift and earn tips for quick service.",
            "- Controls",
            "1-6: go to table",
            "8 or 0: go to the kitchen",
        };
    }

    protected String formatScore(int s) { return "$" + s; }

    protected int accent() { return 0xF4A261; }

    protected void newGame() {
        for (int i = 0; i < T; i++) {
            st[i] = EMPTY;
            cooking[i] = 0;
        }
        money = 0;
        strikes = 0;
        dayT = 180 * 20;
        spawnT = 20;
        served = 0;
        combo = 0;
        target = -1;
        wx = stationX(6);
        wy = stationY(6);
    }

    // station 0-5 tables, 6 kitchen
    private int stationX(int s) {
        if (s == 6) return W / 2;
        return W / 6 + (s % 3) * W / 3;
    }

    private int stationY(int s) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int area = H - hud;
        if (s == 6) return hud + area * 85 / 100;
        return hud + area * (s < 3 ? 22 : 54) / 100;
    }

    protected void update() {
        if (popT > 0) popT--;
        if (--dayT <= 0) {
            headline = "SHIFT OVER";
            score = money;
            endGame(true);
            return;
        }
        int d = digitPressed();
        if (d >= 1 && d <= 6) target = d - 1;
        else if (d == 8 || d == 0) target = 6;
        // walk
        if (target >= 0) {
            int tx = stationX(target), ty = stationY(target);
            int sp = Math.max(2, W / 40);
            if (Math.abs(tx - wx) <= sp && Math.abs(ty - wy) <= sp) {
                wx = tx;
                wy = ty;
                arrive(target);
                target = -1;
            } else {
                if (wx < tx) wx += Math.min(sp, tx - wx); else if (wx > tx) wx -= Math.min(sp, wx - tx);
                if (wy < ty) wy += Math.min(sp, ty - wy); else if (wy > ty) wy -= Math.min(sp, wy - ty);
            }
        }
        // guests
        int rate = Math.max(60, 160 - (180 * 20 - dayT) / 40);
        if (--spawnT <= 0) {
            spawnT = rate + Rnd.nextInt(60);
            for (int k = 0; k < 12; k++) {
                int i = Rnd.nextInt(T);
                if (st[i] == EMPTY) {
                    st[i] = READING;
                    timer[i] = Rnd.range(40, 90);
                    patience[i] = 400;
                    dish[i] = Rnd.nextInt(DISH_COL.length);
                    Sfx.tone(80, 20);
                    break;
                }
            }
        }
        for (int i = 0; i < T; i++) {
            if (cooking[i] == 2 && --cookT[i] <= 0) {
                cooking[i] = 3;
                Sfx.tone(90, 30);
            }
            switch (st[i]) {
                case READING:
                    if (--timer[i] <= 0) st[i] = WANTS_ORDER;
                    break;
                case WANTS_ORDER:
                case WAITING_FOOD:
                    patience[i] -= st[i] == WANTS_ORDER ? 2 : 1;
                    if (patience[i] <= 0) walkOut(i);
                    break;
                case EATING:
                    if (--timer[i] <= 0) {
                        st[i] = DIRTY;
                        int tip = 5 + patience[i] / 40;
                        money += 10 + tip;
                        show("+$" + (10 + tip), i);
                        Sfx.good();
                    }
                    break;
                default:
                    break;
            }
        }
        score = money;
    }

    private void walkOut(int i) {
        st[i] = EMPTY;
        if (cooking[i] == 4) carryN--;
        cooking[i] = 0;
        strikes++;
        combo = 0;
        show("Stormed out!", i);
        Sfx.bad();
        if (strikes >= 3) {
            headline = "3 ANGRY GUESTS";
            score = money;
            endGame(false);
        }
    }

    private void show(String s, int i) {
        pop = s;
        popT = 25;
    }

    private void arrive(int s) {
        if (s == 6) {
            int sent = 0, picked = 0;
            for (int i = 0; i < T; i++) {
                if (cooking[i] == 1) {
                    cooking[i] = 2;
                    cookT[i] = Rnd.range(60, 120);
                    sent++;
                }
            }
            for (int i = 0; i < T && carryN < 2; i++) {
                if (cooking[i] == 3) {
                    cooking[i] = 4;
                    carryN++;
                    picked++;
                }
            }
            if (sent + picked > 0) Sfx.click();
            return;
        }
        int i = s;
        if (st[i] == WANTS_ORDER) {
            st[i] = WAITING_FOOD;
            cooking[i] = 1;
            patience[i] = Math.min(400, patience[i] + 120);
            Sfx.tone(76, 25);
        } else if (st[i] == WAITING_FOOD && cooking[i] == 4) {
            cooking[i] = 0;
            carryN--;
            st[i] = EATING;
            timer[i] = Rnd.range(80, 140);
            served++;
            combo++;
            if (combo % 5 == 0) {
                money += 25;
                show("Streak bonus +$25", i);
            }
            Sfx.good();
        } else if (st[i] == DIRTY) {
            st[i] = EMPTY;
            money += 2;
            Sfx.tone(70, 20);
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x6D4C41);
        g.fillRect(0, 0, W, H);
        g.setColor(0x795548);
        for (int y = hud; y < H; y += 12) for (int x = ((y / 12) & 1) * 12; x < W; x += 24) g.fillRect(x, y, 12, 12);
        int tw = Math.max(18, W / 5), th = Math.max(12, H / 9);
        for (int i = 0; i < T; i++) {
            int x = stationX(i), y = stationY(i);
            Gfx.bevel(g, x - tw / 2, y - th / 2, tw, th, 0xFFF8E1);
            g.setFont(Gfx.SMALL_B);
            g.setColor(0x5D4037);
            g.drawString(String.valueOf(i + 1), x - tw / 2 + 2, y - th / 2 + 1, Gfx.TL);
            if (st[i] != EMPTY && st[i] != DIRTY) {
                // guest
                g.setColor(0x1976D2 + i * 0x101000);
                g.fillRect(x + tw / 4 - 3, y - th / 2 - 8, 7, 7);
                g.setColor(0xFFCC80);
                Gfx.disc(g, x + tw / 4, y - th / 2 - 11, 3);
                int bw = tw - 4;
                if (st[i] == WANTS_ORDER || st[i] == WAITING_FOOD) {
                    int c = patience[i] > 200 ? 0x66BB6A : (patience[i] > 100 ? 0xFFB300 : 0xE53935);
                    Gfx.bar(g, x - bw / 2, y + th / 2 + 2, bw, 3, patience[i], 400, c, 0x3E2723);
                }
                if (st[i] == WANTS_ORDER) {
                    g.setColor((clock & 4) == 0 ? 0xFFEB3B : 0xFFFFFF);
                    g.setFont(Gfx.SMALL_B);
                    g.drawString("!", x + tw / 4 + 6, y - th / 2 - 16, Gfx.TL);
                }
                if (st[i] == READING) {
                    g.setColor(0xFFFFFF);
                    g.fillRect(x - 3, y - 3, 6, 5);
                }
                if (st[i] == EATING || (st[i] == WAITING_FOOD && cooking[i] == 0)) {
                    g.setColor(DISH_COL[dish[i]]);
                    Gfx.disc(g, x - 3, y, 3);
                }
                if (st[i] == WAITING_FOOD) {
                    g.setColor(DISH_COL[dish[i]]);
                    g.drawRect(x - 5, y - 3, 6, 5);
                }
            } else if (st[i] == DIRTY) {
                g.setColor(0x9E9E9E);
                Gfx.disc(g, x - 4, y, 3);
                Gfx.disc(g, x + 3, y + 1, 2);
            }
        }
        // kitchen hatch
        int kx = stationX(6), ky = stationY(6);
        Gfx.bevel(g, kx - W / 3, ky - th / 2, W * 2 / 3, th, 0x90A4AE);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x263238);
        g.drawString("8 KITCHEN", kx - W / 3 + 3, ky - th / 2 + 1, Gfx.TL);
        int slot = 0;
        for (int i = 0; i < T; i++) {
            if (cooking[i] == 2 || cooking[i] == 3) {
                int x = kx + W / 3 - 8 - slot * 9;
                g.setColor(cooking[i] == 3 ? DISH_COL[dish[i]] : 0x546E7A);
                Gfx.disc(g, x, ky + 2, 3);
                if (cooking[i] == 2 && (clock & 4) == 0) {
                    g.setColor(0xFFFFFF);
                    g.fillRect(x, ky - 5, 1, 2);
                }
                slot++;
            }
        }
        // waiter
        g.setColor(0x212121);
        g.fillRect(wx - 3, wy - 6, 7, 9);
        g.setColor(0xFFFFFF);
        g.fillRect(wx - 1, wy - 6, 3, 9);
        g.setColor(0xFFCC80);
        Gfx.disc(g, wx, wy - 9, 3);
        int hands = 0;
        for (int i = 0; i < T; i++) {
            if (cooking[i] == 4) {
                g.setColor(DISH_COL[dish[i]]);
                Gfx.disc(g, wx + (hands == 0 ? -6 : 6), wy - 4, 3);
                hands++;
            }
        }
        int pad = 0;
        for (int i = 0; i < T; i++) if (cooking[i] == 1) pad++;
        // HUD
        g.setColor(0x3E2723);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString("$" + money, 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString(Gfx.time(dayT, tickMs), W / 2, 1, Gfx.TC);
        g.setColor(0xFF5252);
        for (int k = 0; k < 3; k++) {
            if (k < strikes) g.fillRect(W - 8 - k * 8, 3, 6, 6);
            else g.drawRect(W - 8 - k * 8, 3, 5, 5);
        }
        if (pad > 0) Gfx.text(g, pad + " order" + (pad > 1 ? "s" : "") + " to kitchen", W / 2, H - Gfx.SMALL.getHeight() - 1, Gfx.TC, Gfx.SMALL, 0xFFE0B2);
        if (popT > 0) Gfx.shadowText(g, pop, W / 2, hud + 2, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        for (int k = 0; k < 3; k++) {
            int tx = x + w / 6 + k * w / 3;
            Gfx.bevel(g, tx - 10, y + h / 2, 20, 10, 0xFFF8E1);
            g.setColor(DISH_COL[k]);
            Gfx.disc(g, tx, y + h / 2 + 4, 3);
        }
        int wxp = x + (clock * 2) % w;
        g.setColor(0x212121);
        g.fillRect(wxp - 3, y + h / 2 - 12, 7, 9);
        g.setColor(0xFFCC80);
        Gfx.disc(g, wxp, y + h / 2 - 15, 3);
    }
}
