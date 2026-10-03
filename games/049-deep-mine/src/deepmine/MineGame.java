package deepmine;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Deep Mine: drill down through dirt and rock, haul ore back to the
 * surface shop, upgrade your pod and reach the ancient core at the bottom.
 */
public class MineGame extends Game {
    private static final int C = 12, R = 64, SURF = 2;
    // tiles: 0 air, 1 dirt, 2 rock (undiggable), 3..8 ores, 9 gas, 10 lava, 11 core, 12 shop floor
    private static final String[] ORE = { "", "", "", "Coal", "Iron", "Silver", "Gold", "Ruby", "Diamond" };
    private static final int[] ORE_VAL = { 0, 0, 0, 6, 14, 32, 65, 130, 260 };
    private static final int[] ORE_COL = { 0, 0, 0, 0x212121, 0xBF8A6A, 0xCFD8DC, 0xFFD54F, 0xE53935, 0x80DEEA };
    private static final String[] UPG = { "Drill", "Fuel tank", "Cargo bay", "Hull" };

    private final byte[] t = new byte[C * R];
    private int px, py, dx, dy, prog, drillT, drillNeed;
    private int fuel, maxFuel, hull, maxHull, money, fallDist;
    private final int[] cargo = new int[9];
    private int cargoN, cargoMax;
    private final int[] up = new int[4];
    private boolean shopOpen;
    private int shopSel, msgT, ts;
    private String msg = "";

    protected String name() { return "Deep Mine"; }

    protected String[] help() {
        return new String[] {
            "Pilot a drilling pod into the earth. Drill left, right and down with 4/6/8; fly upward with 2 (it burns fuel).",
            "Collect ore and sell it at the shop on the surface (press 5 there), where you can also refuel, repair and upgrade your drill, fuel tank, cargo bay and hull.",
            "Grey rock can't be drilled. Gas pockets and lava damage the hull, and long falls hurt. Run out of fuel underground and you're stranded.",
            "Reach the ancient core at the very bottom to win.",
            "- Controls",
            "4/6/8: drive / drill  2: fly up",
            "5: shop (at the surface)",
        };
    }

    protected String formatScore(int s) { return "$" + s; }

    protected int accent() { return 0xF4A261; }

    protected void newGame() {
        for (int y = 0; y < R; y++) {
            for (int x = 0; x < C; x++) {
                byte v;
                if (y < SURF) v = 0;
                else if (y == SURF && x >= C - 4) v = 12;
                else {
                    int d = y - SURF;
                    int r = Rnd.nextInt(1000);
                    v = 1;
                    if (r < 60 + d * 3) v = 2;
                    else if (r < 140) v = (byte) oreFor(d);
                    else if (d > 15 && r < 160) v = 9;
                    else if (d > 35 && r < 175) v = 10;
                    if (y == SURF) v = 1;
                }
                t[y * C + x] = v;
            }
        }
        t[(R - 2) * C + C / 2] = 11;
        for (int y = R - 1; y < R; y++) for (int x = 0; x < C; x++) t[y * C + x] = 2;
        // guarantee a diggable column so the core is always reachable
        int col = C / 2;
        for (int y = SURF; y < R - 2; y++) if (t[y * C + col] == 2) t[y * C + col] = 1;
        px = 2;
        py = SURF - 1;
        dx = dy = prog = 0;
        up[0] = up[1] = up[2] = up[3] = 0;
        applyUpgrades();
        fuel = maxFuel;
        hull = maxHull;
        money = 20;
        cargoN = 0;
        for (int i = 0; i < 9; i++) cargo[i] = 0;
        shopOpen = false;
        say("Dig deep! The shop is to the right.");
    }

    private int oreFor(int d) {
        int max = Math.min(8, 3 + d / 9);
        int min = Math.max(3, max - 2);
        return Rnd.range(min, max);
    }

    private void applyUpgrades() {
        maxFuel = 300 + up[1] * 250;
        maxHull = 60 + up[3] * 40;
        cargoMax = 8 + up[2] * 6;
    }

    private int upCost(int k) { return 120 + up[k] * up[k] * 180 + k * 20; }

    private void say(String s) {
        msg = s;
        msgT = 50;
    }

    private int tile(int x, int y) {
        if (x < 0 || x >= C || y >= R) return 2;
        if (y < 0) return 0;
        return t[y * C + x];
    }

    private boolean solid(int x, int y) {
        int v = tile(x, y);
        return v != 0;
    }

    protected void update() {
        if (msgT > 0) msgT--;
        if (shopOpen) {
            shop();
            return;
        }
        if (prog > 0) {
            if (++prog >= 4) {
                px += dx;
                py += dy;
                prog = 0;
                arrive();
            }
            return;
        }
        if (drillT > 0) {
            fuel--;
            if (++drillT >= drillNeed) {
                int v = tile(px + dx, py + dy);
                collect(v);
                t[(py + dy) * C + px + dx] = 0;
                drillT = 0;
                prog = 1;
            }
            return;
        }
        // gravity
        if (!solid(px, py + 1) && (held & K_UP) == 0) {
            dx = 0;
            dy = 1;
            prog = 2;
            fallDist++;
            return;
        }
        if (solid(px, py + 1) && fallDist > 3) {
            int dmg = (fallDist - 3) * 6;
            hull -= dmg;
            say("Hard landing! -" + dmg);
            Sfx.bad();
        }
        if (solid(px, py + 1)) fallDist = 0;
        if (py < SURF && px >= C - 4 && (pressed & K_FIRE) != 0) {
            openShop();
            return;
        }
        int ddx = 0, ddy = 0;
        if ((held & K_LEFT) != 0) ddx = -1;
        else if ((held & K_RIGHT) != 0) ddx = 1;
        else if ((held & K_DOWN) != 0) ddy = 1;
        else if ((held & K_UP) != 0) ddy = -1;
        if (ddx == 0 && ddy == 0) return;
        int target = tile(px + ddx, py + ddy);
        if (ddy == -1) {
            if (!solid(px, py - 1) && py > 0) {
                fuel -= 2;
                dx = 0;
                dy = -1;
                prog = 1;
                fallDist = 0;
            }
        } else if (target == 0) {
            fuel--;
            dx = ddx;
            dy = ddy;
            prog = 1;
        } else if (target == 2 || target == 12) {
            if ((pressed & (K_LEFT | K_RIGHT | K_DOWN)) != 0) Sfx.tone(40, 20);
        } else if (ddy >= 0) {
            if (!solid(px, py + 1) && ddx != 0) return; // can't drill sideways while falling
            dx = ddx;
            dy = ddy;
            drillT = 1;
            int hard = target == 1 ? 6 : (target >= 3 && target <= 8 ? 6 + target : 8);
            drillNeed = Math.max(3, hard - up[0] * 2);
            Sfx.tone(35 + target, 20);
        }
        if (fuel <= 0) {
            fuel = 0;
            if (py >= SURF) {
                headline = "OUT OF FUEL";
                score = money;
                endGame(false);
            }
        }
    }

    private void collect(int v) {
        if (v >= 3 && v <= 8) {
            if (cargoN < cargoMax) {
                cargo[v]++;
                cargoN++;
                say("+1 " + ORE[v]);
                Sfx.good();
            } else {
                say("Cargo full! Head home.");
            }
        } else if (v == 9) {
            hull -= 15;
            say("Gas pocket! -15 hull");
            Sfx.bad();
        } else if (v == 10) {
            hull -= 30;
            say("LAVA! -30 hull");
            Sfx.bad();
        } else if (v == 11) {
            score = money + 5000;
            headline = "CORE FOUND!";
            endGame(true);
        }
        if (hull <= 0) {
            hull = 0;
            score = money;
            headline = "POD DESTROYED";
            endGame(false);
        }
    }

    private void arrive() {
        if (py < SURF) score = Math.max(score, money);
    }

    private void openShop() {
        int earned = 0;
        for (int i = 3; i <= 8; i++) {
            earned += cargo[i] * ORE_VAL[i];
            cargo[i] = 0;
        }
        cargoN = 0;
        money += earned;
        score = money;
        shopOpen = true;
        shopSel = 0;
        if (earned > 0) {
            say("Sold ore for $" + earned);
            Sfx.win();
        }
    }

    private void shop() {
        int items = 6;
        if ((pressed & K_UP) != 0) shopSel = (shopSel + items - 1) % items;
        if ((pressed & K_DOWN) != 0) shopSel = (shopSel + 1) % items;
        if ((pressed & (K_STAR | K_POUND | K_NUM0)) != 0) shopOpen = false;
        if ((pressed & K_FIRE) == 0) return;
        if (shopSel == 0) { // refuel
            int need = (maxFuel - fuel + 9) / 10;
            int pay = Math.min(need, money);
            fuel = Math.min(maxFuel, fuel + pay * 10);
            money -= pay;
            Sfx.click();
        } else if (shopSel == 1) { // repair
            int need = (maxHull - hull) * 2;
            int pay = Math.min(need, money);
            hull = Math.min(maxHull, hull + pay / 2);
            money -= pay;
            Sfx.click();
        } else if (shopSel <= 5 && shopSel >= 2 && shopSel < 6) {
            int k = shopSel - 2;
            if (up[k] < 3 && money >= upCost(k)) {
                money -= upCost(k);
                up[k]++;
                applyUpgrades();
                Sfx.good();
            } else Sfx.bad();
        }
        score = Math.max(score, money);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        ts = Math.max(8, Math.min(W / C, (H - hud * 2) / 9));
        int ox = (W - ts * C) / 2;
        int viewRows = (H - hud * 2) / ts + 2;
        int camY = (py * ts + dy * ts * prog / 4) - (H - hud * 2) / 3;
        if (camY < -SURF * ts) camY = -SURF * ts;
        int oy = hud - camY;
        int depthCol = Math.min(255, Math.max(0, py * 4));
        g.setColor(Gfx.mix(0x81D4FA, 0x1A0F0A, depthCol));
        g.fillRect(0, 0, W, H);
        int r0 = Math.max(0, camY / ts - 1);
        for (int y = r0; y < Math.min(R, r0 + viewRows + 1); y++) {
            for (int x = 0; x < C; x++) {
                int v = t[y * C + x];
                int X = ox + x * ts, Y = oy + y * ts;
                if (v == 0) {
                    if (y >= SURF) {
                        g.setColor(0x2B1A10);
                        g.fillRect(X, Y, ts, ts);
                    }
                    continue;
                }
                switch (v) {
                    case 1:
                        g.setColor(y == SURF ? 0x689F38 : (y < 20 ? 0x8D5A2B : (y < 40 ? 0x6D4C41 : 0x4E342E)));
                        g.fillRect(X, Y, ts, ts);
                        g.setColor(0x5D3A1A);
                        g.fillRect(X + (x * 5 + y * 3) % (ts - 2), Y + (x * 3 + y * 7) % (ts - 2), 2, 1);
                        break;
                    case 2:
                        Gfx.bevel(g, X, Y, ts, ts, 0x757575);
                        break;
                    case 9:
                        g.setColor(0x6D4C41);
                        g.fillRect(X, Y, ts, ts);
                        g.setColor(0xAED581);
                        g.fillRect(X + ts / 3, Y + ts / 3, 2, 2);
                        break;
                    case 10:
                        g.setColor((clock / 4 + x) % 2 == 0 ? 0xFF3D00 : 0xFF6E40);
                        g.fillRect(X, Y, ts, ts);
                        break;
                    case 11:
                        g.setColor(0x4E342E);
                        g.fillRect(X, Y, ts, ts);
                        g.setColor((clock & 4) == 0 ? 0xE040FB : 0x7C4DFF);
                        Gfx.disc(g, X + ts / 2, Y + ts / 2, ts / 2 - 1);
                        break;
                    case 12:
                        g.setColor(0x9E9E9E);
                        g.fillRect(X, Y, ts, ts);
                        break;
                    default:
                        g.setColor(0x6D4C41);
                        g.fillRect(X, Y, ts, ts);
                        g.setColor(ORE_COL[v]);
                        Gfx.disc(g, X + ts / 3, Y + ts / 3, Math.max(1, ts / 6));
                        Gfx.disc(g, X + ts * 2 / 3, Y + ts * 3 / 5, Math.max(1, ts / 6));
                        break;
                }
            }
        }
        // shop building
        int sx = ox + (C - 4) * ts, sy = oy + SURF * ts;
        g.setColor(0x1565C0);
        g.fillRect(sx + 2, sy - ts * 2, ts * 4 - 4, ts * 2);
        g.setColor(0xFFEB3B);
        g.setFont(Gfx.SMALL_B);
        g.drawString("SHOP", sx + ts * 2, sy - ts * 2 + 2, Gfx.TC);
        // pod
        int X = ox + px * ts + dx * ts * prog / 4, Y = oy + py * ts + dy * ts * prog / 4;
        if (drillT > 0 && (clock & 1) == 0) X += 1;
        g.setColor(0xFFB300);
        g.fillRoundRect(X + 1, Y + 2, ts - 2, ts - 4, 4, 4);
        g.setColor(0x80DEEA);
        g.fillRect(X + ts / 3, Y + 3, ts / 3, ts / 4);
        g.setColor(0x9E9E9E);
        if (drillT > 0) {
            int tx = X + ts / 2 + dx * ts / 2, ty = Y + ts / 2 + dy * ts / 2;
            g.fillTriangle(X + ts / 2 + dx * ts / 4 - dy * 3, Y + ts / 2 + dy * ts / 4 - dx * 3, X + ts / 2 + dx * ts / 4 + dy * 3, Y + ts / 2 + dy * ts / 4 + dx * 3, tx, ty);
        } else {
            g.fillRect(X + ts / 2 - 2, Y + ts - 3, 4, 3);
        }
        if ((held & K_UP) != 0 && !shopOpen) {
            g.setColor((clock & 1) == 0 ? 0xFF6D00 : 0xFFD180);
            g.fillTriangle(X + ts / 3, Y + ts, X + ts * 2 / 3, Y + ts, X + ts / 2, Y + ts + ts / 2);
        }
        // HUD
        g.setColor(0x1A0F0A);
        g.fillRect(0, 0, W, hud);
        g.fillRect(0, H - hud, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString("$" + money, 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString(Math.max(0, (py - SURF + 1) * 10) + "m", W - 2, 1, Gfx.TR);
        int bw = W / 4;
        Gfx.bar(g, W / 2 - bw - 2, 3, bw, hud - 6, fuel, maxFuel, fuel < maxFuel / 4 ? 0xFF5252 : 0xFFB300, 0x3E2723);
        Gfx.bar(g, W / 2 + 2, 3, bw, hud - 6, hull, maxHull, 0x66BB6A, 0x3E2723);
        g.setFont(Gfx.SMALL);
        g.setColor(0xD7CCC8);
        g.drawString(msgT > 0 ? msg : "Cargo " + cargoN + "/" + cargoMax, W / 2, H - hud + 1, Gfx.TC);
        if (shopOpen) drawShop(g);
    }

    private void drawShop(Graphics g) {
        int lh = Gfx.SMALL.getHeight() + 3;
        int bw = W - 12, bh = lh * 7 + 6;
        int bx = 6, by = (H - bh) / 2;
        Gfx.panel(g, bx, by, bw, bh, 0x0D1B2A, 0xFFB300);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFB300);
        g.drawString("SHOP  $" + money, W / 2, by + 3, Gfx.TC);
        String[] rows = new String[6];
        rows[0] = "Refuel  ($1 per 10)";
        rows[1] = "Repair  ($2 per pt)";
        for (int k = 0; k < 4; k++) rows[k + 2] = UPG[k] + " L" + up[k] + (up[k] >= 3 ? "  MAX" : "  $" + upCost(k));
        g.setFont(Gfx.SMALL);
        for (int i = 0; i < 6; i++) {
            int y = by + 3 + lh * (i + 1);
            if (i == shopSel) {
                g.setColor(0xFFB300);
                g.fillRect(bx + 3, y - 1, bw - 6, lh);
            }
            g.setColor(i == shopSel ? 0x000000 : 0xFFFFFF);
            g.drawString(rows[i], bx + 6, y, Gfx.TL);
        }
        g.setColor(0x90A4AE);
        g.drawString("5 buy  0 leave", W / 2, by + bh - lh + 1, Gfx.TC);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(8, h / 4);
        for (int r = 0; r < h / s; r++) {
            for (int c = 0; c < w / s; c++) {
                int v = (r * 7 + c * 5) % 9;
                g.setColor(r == 0 ? 0x689F38 : (v == 0 ? 0x757575 : 0x8D5A2B));
                g.fillRect(x + c * s, y + r * s, s, s);
                if (v == 3 && r > 0) {
                    g.setColor(0xFFD54F);
                    Gfx.disc(g, x + c * s + s / 2, y + r * s + s / 2, s / 5);
                }
            }
        }
        int depth = (clock / 6) % Math.max(1, h / s);
        g.setColor(0x2B1A10);
        g.fillRect(x + w / 2 - s / 2, y, s, depth * s + s);
        g.setColor(0xFFB300);
        g.fillRoundRect(x + w / 2 - s / 2 + 1, y + depth * s + 1, s - 2, s - 2, 4, 4);
    }
}
