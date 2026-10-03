package siegelanes;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Siege Lanes: two castles, three lanes. Spend gold to send troops up a
 * lane (keys 1-3); troops fight whatever they meet and batter the enemy
 * castle if they get through.
 */
public class SiegeGame extends Game {
    private static final int MAXU = 40;
    private static final String[] TYPE = { "Spear", "Archer", "Knight" };
    private static final int[] COST = { 10, 15, 30 }, HP = { 10, 6, 24 }, DMG = { 3, 2, 5 }, RANGE = { 1, 4, 1 }, SPD = { 3, 2, 2 };

    private final int[] ul = new int[MAXU], uy = new int[MAXU], ut = new int[MAXU], uhp = new int[MAXU], us = new int[MAXU], ucd = new int[MAXU];
    private final boolean[] uon = new boolean[MAXU];
    private int gold, cpuGold, myHp, cpuHp, type, cpuT, laneLen;

    protected String name() { return "Siege Lanes"; }

    protected String[] help() {
        return new String[] {
            "Your castle is at the bottom, the enemy's at the top, joined by three lanes. Gold trickles in; spend it to send troops up a lane.",
            "Spearmen are cheap, Archers attack from range, Knights are tough and hit hard. Troops fight enemies they meet; any that reach the far castle damage it.",
            "Destroy the enemy castle before yours falls.",
            "- Controls",
            "4/6: choose troop type",
            "1 / 2 / 3: send to left / middle / right lane",
        };
    }

    protected String[] modes() { return new String[] { "Squire", "Warlord" }; }

    protected int accent() { return 0x2A9D8F; }

    protected void newGame() {
        for (int i = 0; i < MAXU; i++) uon[i] = false;
        gold = 30;
        cpuGold = 30;
        myHp = cpuHp = 100;
        type = 0;
        cpuT = 60;
        laneLen = 1000;
    }

    private void spawn(int side, int lane, int t) {
        for (int i = 0; i < MAXU; i++) {
            if (uon[i]) continue;
            uon[i] = true;
            us[i] = side;
            ul[i] = lane;
            ut[i] = t;
            uhp[i] = HP[t];
            uy[i] = side == 0 ? 0 : laneLen;
            ucd[i] = 0;
            return;
        }
    }

    protected void update() {
        if (frame % 10 == 0) {
            gold += 2;
            cpuGold += mode == 0 ? 2 : 3;
        }
        if ((pressed & K_LEFT) != 0 && (pressed & K_DIGITS) == 0) type = (type + 2) % 3;
        if ((pressed & K_RIGHT) != 0 && (pressed & K_DIGITS) == 0) type = (type + 1) % 3;
        int d = digitPressed();
        if (d == 4) type = (type + 2) % 3;
        if (d == 6) type = (type + 1) % 3;
        if (d >= 1 && d <= 3) {
            if (gold >= COST[type]) {
                gold -= COST[type];
                spawn(0, d - 1, type);
                Sfx.click();
            } else Sfx.bad();
        }
        // CPU: answers the lane where you are strongest, otherwise pushes
        if (--cpuT <= 0) {
            cpuT = mode == 0 ? 40 : 25;
            int[] mine = new int[3];
            for (int i = 0; i < MAXU; i++) if (uon[i] && us[i] == 0) mine[ul[i]] += HP[ut[i]];
            int lane = Rnd.nextInt(3);
            for (int l = 0; l < 3; l++) if (mine[l] > mine[lane]) lane = l;
            int t = cpuGold >= 30 && Rnd.chance(40) ? 2 : (cpuGold >= 15 && Rnd.chance(40) ? 1 : 0);
            if (cpuGold >= COST[t]) {
                cpuGold -= COST[t];
                spawn(1, lane, t);
            }
        }
        // movement and combat
        for (int i = 0; i < MAXU; i++) {
            if (!uon[i]) continue;
            if (ucd[i] > 0) ucd[i]--;
            int target = -1, best = 99999;
            for (int j = 0; j < MAXU; j++) {
                if (!uon[j] || us[j] == us[i] || ul[j] != ul[i]) continue;
                int dist = us[i] == 0 ? uy[j] - uy[i] : uy[i] - uy[j];
                if (dist >= -20 && dist < best) { best = dist; target = j; }
            }
            int reach = RANGE[ut[i]] * 40;
            if (target >= 0 && best <= reach) {
                if (ucd[i] == 0) {
                    uhp[target] -= DMG[ut[i]];
                    ucd[i] = 12;
                    if (uhp[target] <= 0) {
                        uon[target] = false;
                        if (us[target] == 1) { score += 5; gold += 3; }
                    }
                }
            } else {
                uy[i] += (us[i] == 0 ? 1 : -1) * SPD[ut[i]] * 3;
            }
            if (us[i] == 0 && uy[i] >= laneLen) {
                cpuHp -= DMG[ut[i]] * 2;
                uon[i] = false;
                score += 10;
                Sfx.good();
            } else if (us[i] == 1 && uy[i] <= 0) {
                myHp -= DMG[ut[i]] * 2;
                uon[i] = false;
                Sfx.bad();
            }
        }
        if (cpuHp <= 0) {
            score += 500 + myHp * 5;
            headline = "SIEGE WON!";
            endGame(true);
        } else if (myHp <= 0) {
            headline = "CASTLE FALLEN";
            endGame(false);
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int castleH = Math.max(10, H / 12);
        int top = hud + castleH, bot = H - hud - castleH;
        g.setColor(0x33691E);
        g.fillRect(0, 0, W, H);
        int lw = W / 3;
        for (int l = 0; l < 3; l++) {
            g.setColor(0xA1887F);
            g.fillRect(l * lw + lw / 4, top, lw / 2, bot - top);
            g.setFont(Gfx.SMALL_B);
            g.setColor(0xFFFFFF);
            g.drawString(String.valueOf(l + 1), l * lw + lw / 2, bot + 1, Gfx.TC);
        }
        g.setColor(0x8E2424);
        g.fillRect(0, hud, W, castleH);
        g.setColor(0x1565C0);
        g.fillRect(0, bot + Gfx.SMALL.getHeight(), W, castleH);
        for (int k = 0; k < W; k += 8) {
            g.setColor(0x8E2424);
            g.fillRect(k, hud + castleH, 4, 3);
            g.setColor(0x1565C0);
            g.fillRect(k, bot + Gfx.SMALL.getHeight() - 3, 4, 3);
        }
        for (int i = 0; i < MAXU; i++) {
            if (!uon[i]) continue;
            int x = ul[i] * lw + lw / 2;
            int y = bot - (uy[i] * (bot - top) / laneLen);
            int s = ut[i] == 2 ? 5 : 3;
            g.setColor(us[i] == 0 ? 0x42A5F5 : 0xEF5350);
            if (ut[i] == 1) g.fillTriangle(x, y - s - 1, x - s, y + s, x + s, y + s);
            else g.fillRect(x - s, y - s, s * 2, s * 2);
            g.setColor(0xFFFFFF);
            g.fillRect(x - s, y + s + 1, Math.max(1, s * 2 * uhp[i] / HP[ut[i]]), 1);
        }
        g.setColor(0x000000);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString("$" + gold, 2, 1, Gfx.TL);
        Gfx.bar(g, W / 4, hud + castleH / 2 - 2, W / 2, 4, cpuHp, 100, 0xFFCDD2, 0x4E1010);
        Gfx.bar(g, W / 4, bot + Gfx.SMALL.getHeight() + castleH / 2 - 2, W / 2, 4, myHp, 100, 0xBBDEFB, 0x0D2A4E);
        g.setColor(0xFFFFFF);
        g.drawString(TYPE[type] + " $" + COST[type], W - 2, 1, Gfx.TR);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        for (int l = 0; l < 3; l++) {
            g.setColor(0xA1887F);
            g.fillRect(x + l * w / 3 + w / 12, y, w / 6, h);
            int py = y + h - ((clock * 2 + l * 13) % h);
            g.setColor(0x42A5F5);
            g.fillRect(x + l * w / 3 + w / 6 - 3, py, 6, 6);
        }
    }
}
