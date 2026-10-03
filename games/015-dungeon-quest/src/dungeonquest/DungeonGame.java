package dungeonquest;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Dungeon Quest: a turn-based roguelike. Ten generated floors of rooms and
 * corridors, field of view, bump combat, levelling and a dragon at the bottom.
 */
public class DungeonGame extends Game {
    private static final int MW = 34, MH = 24, MAXM = 18, MAXI = 14, FLOORS = 10;
    private static final int WALL = 0, FLOOR = 1, STAIRS = 2;

    private static final String[] MNAME = { "rat", "bat", "goblin", "skeleton", "orc", "troll", "wraith", "dragon" };
    private static final char[] MCHAR = { 'r', 'b', 'g', 's', 'o', 'T', 'W', 'D' };
    private static final int[] MCOLOR = { 0xB0A090, 0x9575CD, 0x8BC34A, 0xEEEEEE, 0x4CAF50, 0x8D6E63, 0x80DEEA, 0xFF5252 };
    private static final int[] MHP = { 3, 4, 7, 10, 14, 22, 18, 70 };
    private static final int[] MATK = { 1, 2, 3, 4, 5, 7, 6, 10 };
    private static final int[] MDEF = { 0, 0, 1, 2, 2, 3, 3, 4 };
    private static final int[] MXP = { 1, 2, 3, 5, 7, 12, 10, 50 };

    private final byte[] map = new byte[MW * MH];
    private final boolean[] seen = new boolean[MW * MH];
    private final boolean[] vis = new boolean[MW * MH];
    private final int[] mx = new int[MAXM], my = new int[MAXM], mhp = new int[MAXM], mt = new int[MAXM];
    private final boolean[] malive = new boolean[MAXM], mawake = new boolean[MAXM];
    private final int[] ix = new int[MAXI], iy = new int[MAXI], it = new int[MAXI]; // it: 0 none 1 potion 2 gold 3 weapon 4 armor
    private int px, py, hp, maxHp, atk, def, lvl, xp, potions, gold, depth, kills;
    private String msg = "";
    private int msgColor = 0xFFFFFF;
    private boolean showMap;
    private Image hero;
    private int heroScale;

    // room list for generation
    private final int[] rx = new int[10], ry = new int[10], rw = new int[10], rh = new int[10];

    protected String name() { return "Dungeon Quest"; }

    protected String[] help() {
        return new String[] {
            "Descend ten floors of a dungeon and slay the dragon on the last one. Every step is a turn: monsters move only when you do.",
            "Walk into a monster to attack it. Gain experience to level up. Pick up potions (!), gold ($), weapons ( ) ) and armour ( [ ) by walking over them.",
            "Find the stairs (>) and press 5 on them to go deeper.",
            "- Controls",
            "2/4/6/8: move / attack",
            "1/3/7/9: move diagonally",
            "5: wait a turn / take stairs",
            "0: drink a potion  #: map",
        };
    }

    protected int accent() { return 0x9B5DE5; }

    protected void newGame() {
        hp = maxHp = 20;
        atk = 3;
        def = 0;
        lvl = 1;
        xp = 0;
        potions = 1;
        gold = 0;
        depth = 1;
        kills = 0;
        showMap = false;
        genFloor();
        say("Floor 1. Find the stairs down.", 0xFFFFFF);
    }

    private void say(String s, int c) {
        msg = s;
        msgColor = c;
    }

    // --------------------------------------------------------- generation

    private void genFloor() {
        for (int i = 0; i < map.length; i++) {
            map[i] = WALL;
            seen[i] = false;
        }
        int n = 0;
        for (int tries = 0; tries < 200 && n < 9; tries++) {
            int w = Rnd.range(4, 8), h = Rnd.range(3, 6);
            int x = Rnd.range(1, MW - w - 2), y = Rnd.range(1, MH - h - 2);
            boolean ok = true;
            for (int k = 0; k < n && ok; k++) {
                if (x < rx[k] + rw[k] + 1 && rx[k] < x + w + 1 && y < ry[k] + rh[k] + 1 && ry[k] < y + h + 1) ok = false;
            }
            if (!ok) continue;
            rx[n] = x; ry[n] = y; rw[n] = w; rh[n] = h;
            for (int yy = y; yy < y + h; yy++) for (int xx = x; xx < x + w; xx++) map[yy * MW + xx] = FLOOR;
            if (n > 0) corridor(rx[n - 1] + rw[n - 1] / 2, ry[n - 1] + rh[n - 1] / 2, x + w / 2, y + h / 2);
            n++;
        }
        px = rx[0] + rw[0] / 2;
        py = ry[0] + rh[0] / 2;
        int last = n - 1;
        if (depth < FLOORS) map[(ry[last] + rh[last] / 2) * MW + rx[last] + rw[last] / 2] = STAIRS;
        for (int i = 0; i < MAXM; i++) malive[i] = false;
        for (int i = 0; i < MAXI; i++) it[i] = 0;
        int monsters = 4 + depth + (depth / 2);
        for (int k = 0; k < monsters && k < MAXM - 1; k++) {
            int r = 1 + Rnd.nextInt(n - 1);
            int t = Math.min(6, Math.max(0, depth / 2 + Rnd.range(-1, 1)));
            if (depth >= 7 && Rnd.chance(30)) t = 6;
            spawn(k, t, rx[r] + Rnd.nextInt(rw[r]), ry[r] + Rnd.nextInt(rh[r]));
        }
        if (depth == FLOORS) spawn(MAXM - 1, 7, rx[last] + rw[last] / 2, ry[last] + rh[last] / 2);
        for (int k = 0; k < 4 + depth / 2 && k < MAXI; k++) {
            int r = Rnd.nextInt(n);
            int x = rx[r] + Rnd.nextInt(rw[r]), y = ry[r] + Rnd.nextInt(rh[r]);
            if (x == px && y == py) continue;
            ix[k] = x;
            iy[k] = y;
            int roll = Rnd.nextInt(100);
            it[k] = roll < 35 ? 1 : (roll < 75 ? 2 : (roll < 88 ? 3 : 4));
        }
        computeFov();
    }

    private void spawn(int k, int t, int x, int y) {
        if ((x == px && y == py) || map[y * MW + x] == WALL) return;
        malive[k] = true;
        mawake[k] = t == 7;
        mt[k] = t;
        mx[k] = x;
        my[k] = y;
        mhp[k] = MHP[t] + (t < 7 ? depth / 3 : 0);
    }

    private void corridor(int x0, int y0, int x1, int y1) {
        boolean hFirst = Rnd.chance(50);
        int x = x0, y = y0;
        while (x != x1 || y != y1) {
            map[y * MW + x] = FLOOR;
            if (hFirst ? x != x1 : y == y1) x += x1 > x ? 1 : -1;
            else y += y1 > y ? 1 : -1;
        }
        map[y * MW + x] = FLOOR;
    }

    // ---------------------------------------------------------------- FOV

    private void computeFov() {
        for (int i = 0; i < vis.length; i++) vis[i] = false;
        int rad = 6;
        for (int y = py - rad; y <= py + rad; y++) {
            for (int x = px - rad; x <= px + rad; x++) {
                if (x < 0 || y < 0 || x >= MW || y >= MH) continue;
                int dx = x - px, dy = y - py;
                if (dx * dx + dy * dy > rad * rad + 2) continue;
                if (los(px, py, x, y)) {
                    vis[y * MW + x] = true;
                    seen[y * MW + x] = true;
                }
            }
        }
    }

    private boolean los(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
        while (true) {
            if (x0 == x1 && y0 == y1) return true;
            if ((x0 != px || y0 != py) && map[y0 * MW + x0] == WALL) return false;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    // --------------------------------------------------------------- turns

    private int monsterAt(int x, int y) {
        for (int i = 0; i < MAXM; i++) if (malive[i] && mx[i] == x && my[i] == y) return i;
        return -1;
    }

    protected void update() {
        if (showMap) {
            if ((pressed & (K_POUND | K_FIRE | K_NUM0 | K_SOFT)) != 0) showMap = false;
            return;
        }
        if ((pressed & K_POUND) != 0) {
            showMap = true;
            return;
        }
        int dx = 0, dy = 0;
        boolean acted = false;
        if (digit(1)) { dx = -1; dy = -1; }
        else if (digit(3)) { dx = 1; dy = -1; }
        else if (digit(7)) { dx = -1; dy = 1; }
        else if (digit(9)) { dx = 1; dy = 1; }
        else if ((pressed & K_UP) != 0) dy = -1;
        else if ((pressed & K_DOWN) != 0) dy = 1;
        else if ((pressed & K_LEFT) != 0) dx = -1;
        else if ((pressed & K_RIGHT) != 0) dx = 1;
        if (dx != 0 || dy != 0) {
            acted = step(dx, dy);
        } else if ((pressed & K_FIRE) != 0) {
            if (map[py * MW + px] == STAIRS) {
                depth++;
                score += 100;
                Sfx.win();
                genFloor();
                say("Floor " + depth + (depth == FLOORS ? ". The dragon awaits!" : "."), 0xFFD54F);
                return;
            }
            acted = true;
            if (hp < maxHp && Rnd.chance(30)) hp++;
        } else if (digit(0)) {
            if (potions > 0) {
                potions--;
                int heal = 10 + lvl * 2;
                hp = Math.min(maxHp, hp + heal);
                say("You drink a potion (+" + heal + ").", 0x80CBC4);
                Sfx.good();
                acted = true;
            } else {
                say("No potions left.", 0xAAAAAA);
            }
        }
        if (acted) {
            monstersTurn();
            computeFov();
        }
        score = gold + (depth - 1) * 100 + kills * 10 + (lvl - 1) * 50;
    }

    private boolean step(int dx, int dy) {
        int nx = px + dx, ny = py + dy;
        if (nx < 0 || ny < 0 || nx >= MW || ny >= MH || map[ny * MW + nx] == WALL) return false;
        int m = monsterAt(nx, ny);
        if (m >= 0) {
            attack(m);
            return true;
        }
        px = nx;
        py = ny;
        for (int i = 0; i < MAXI; i++) {
            if (it[i] == 0 || ix[i] != px || iy[i] != py) continue;
            switch (it[i]) {
                case 1: potions++; say("You found a potion.", 0x80CBC4); break;
                case 2: { int gg = Rnd.range(5, 15) * depth; gold += gg; say("You found " + gg + " gold.", 0xFFD54F); break; }
                case 3: atk++; say("A better weapon! Attack " + atk + ".", 0xFFAB91); break;
                default: def++; say("Sturdier armour! Defence " + def + ".", 0x90CAF9); break;
            }
            it[i] = 0;
            Sfx.tone(84, 25);
        }
        if (map[py * MW + px] == STAIRS) say("Stairs down. Press 5 to descend.", 0xFFFFFF);
        return true;
    }

    private void attack(int m) {
        int t = mt[m];
        int dmg = Math.max(1, atk + Rnd.range(0, 2) - MDEF[t]);
        mhp[m] -= dmg;
        mawake[m] = true;
        Sfx.hit();
        if (mhp[m] <= 0) {
            malive[m] = false;
            kills++;
            xp += MXP[t];
            say("You slay the " + MNAME[t] + "!", 0xA5D6A7);
            if (t == 7) {
                score += 1000;
                headline = "DRAGON SLAIN!";
                score = gold + depth * 100 + kills * 10 + lvl * 50 + 1000;
                endGame(true);
                return;
            }
            while (xp >= lvl * 10) {
                xp -= lvl * 10;
                lvl++;
                maxHp += 5;
                atk++;
                hp = maxHp;
                say("Level up! You are level " + lvl + ".", 0xFFF176);
                Sfx.good();
            }
        } else {
            say("You hit the " + MNAME[t] + " (" + dmg + ").", 0xFFFFFF);
        }
    }

    private void monstersTurn() {
        for (int i = 0; i < MAXM; i++) {
            if (!malive[i]) continue;
            if (!mawake[i]) {
                if (vis[my[i] * MW + mx[i]]) mawake[i] = true;
                else continue;
            }
            int t = mt[i];
            int dx = FMathSign(px - mx[i]), dy = FMathSign(py - my[i]);
            if (Math.abs(px - mx[i]) <= 1 && Math.abs(py - my[i]) <= 1) {
                int dmg = Math.max(0, MATK[t] + Rnd.range(0, 1) - def);
                if (Rnd.chance(15)) dmg = 0;
                hp -= dmg;
                if (dmg > 0) say("The " + MNAME[t] + " hits you (" + dmg + ").", 0xFF8A80);
                else say("The " + MNAME[t] + " misses.", 0xBBBBBB);
                if (hp <= 0) {
                    hp = 0;
                    headline = "SLAIN BY " + MNAME[t].toUpperCase();
                    Sfx.lose();
                    endGame(false);
                    return;
                }
                continue;
            }
            if (t == 1 && Rnd.chance(40)) { dx = Rnd.range(-1, 1); dy = Rnd.range(-1, 1); }
            if (!tryMove(i, dx, dy) && !tryMove(i, dx, 0)) tryMove(i, 0, dy);
        }
    }

    private static int FMathSign(int v) { return v > 0 ? 1 : (v < 0 ? -1 : 0); }

    private boolean tryMove(int i, int dx, int dy) {
        if (dx == 0 && dy == 0) return false;
        int nx = mx[i] + dx, ny = my[i] + dy;
        if (nx < 0 || ny < 0 || nx >= MW || ny >= MH || map[ny * MW + nx] == WALL) return false;
        if ((nx == px && ny == py) || monsterAt(nx, ny) >= 0) return false;
        mx[i] = nx;
        my[i] = ny;
        return true;
    }

    // ------------------------------------------------------------ drawing

    private void buildHero(int sc) {
        heroScale = sc;
        hero = Gfx.sprite(new String[] {
            "..1111..", ".122221.", ".133331.", "..1331..", ".144441.", "15444415", "..4..4..", ".11..11." },
            new int[] { 0, 0x202020, 0x9E9E9E, 0xFFCC80, 0x5C6BC0, 0xE0E0E0 }, sc);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int cell = Math.max(8, Math.min(W / 11, (H - hud * 3) / 11));
        int vcols = W / cell, vrows = (H - hud * 3) / cell;
        int camX = Math.max(0, Math.min(MW - vcols, px - vcols / 2));
        int camY = Math.max(0, Math.min(MH - vrows, py - vrows / 2));
        int ox = (W - Math.min(vcols, MW) * cell) / 2, oy = hud + 1;
        int sc = Math.max(1, cell / 8);
        if (hero == null || heroScale != sc) buildHero(sc);
        g.setColor(0x0A0710);
        g.fillRect(0, 0, W, H);
        if (showMap) {
            drawOverview(g, hud);
            return;
        }
        for (int vy = 0; vy < vrows && camY + vy < MH; vy++) {
            for (int vx = 0; vx < vcols && camX + vx < MW; vx++) {
                int i = (camY + vy) * MW + camX + vx;
                if (!seen[i]) continue;
                int x = ox + vx * cell, y = oy + vy * cell;
                boolean lit = vis[i];
                if (map[i] == WALL) {
                    g.setColor(lit ? 0x6D5D7E : 0x3A3145);
                    g.fillRect(x, y, cell, cell);
                    g.setColor(lit ? 0x4E425C : 0x2A2333);
                    g.drawLine(x, y + cell / 2, x + cell - 1, y + cell / 2);
                    g.drawLine(x + cell / 2, y, x + cell / 2, y + cell / 2);
                } else {
                    g.setColor(lit ? 0x2B2433 : 0x17131C);
                    g.fillRect(x, y, cell, cell);
                    g.setColor(lit ? 0x3A3145 : 0x201A27);
                    g.fillRect(x + cell / 2, y + cell / 2, 1, 1);
                    if (map[i] == STAIRS) {
                        g.setFont(Gfx.SMALL_B);
                        g.setColor(0xFFFFFF);
                        g.drawString(">", x + cell / 2 + 1, y + (cell - 7) / 2, Gfx.TC);
                    }
                }
            }
        }
        g.setFont(cell >= 14 ? Gfx.MEDIUM : Gfx.SMALL_B);
        int fh = cell >= 14 ? Gfx.MEDIUM.getBaselinePosition() : 7;
        for (int k = 0; k < MAXI; k++) {
            if (it[k] == 0 || !vis[iy[k] * MW + ix[k]]) continue;
            int x = ox + (ix[k] - camX) * cell, y = oy + (iy[k] - camY) * cell;
            if (x < 0 || y < oy || x >= W || y >= oy + vrows * cell) continue;
            char ch = it[k] == 1 ? '!' : (it[k] == 2 ? '$' : (it[k] == 3 ? ')' : '['));
            g.setColor(it[k] == 1 ? 0xFF5252 : (it[k] == 2 ? 0xFFD54F : (it[k] == 3 ? 0xFFAB91 : 0x90CAF9)));
            g.drawChar(ch, x + cell / 2 + 1, y + (cell - fh) / 2, Gfx.TC);
        }
        for (int i = 0; i < MAXM; i++) {
            if (!malive[i] || !vis[my[i] * MW + mx[i]]) continue;
            int x = ox + (mx[i] - camX) * cell, y = oy + (my[i] - camY) * cell;
            if (x < 0 || y < oy || x >= W || y >= oy + vrows * cell) continue;
            g.setColor(MCOLOR[mt[i]]);
            g.drawChar(MCHAR[mt[i]], x + cell / 2 + 1, y + (cell - fh) / 2, Gfx.TC);
            if (mhp[i] < MHP[mt[i]]) {
                g.setColor(0xFF1744);
                g.fillRect(x + 1, y + cell - 2, Math.max(1, (cell - 2) * mhp[i] / (MHP[mt[i]] + depth / 3)), 1);
            }
        }
        g.drawImage(hero, ox + (px - camX) * cell + cell / 2, oy + (py - camY) * cell + cell / 2, Gfx.CC);
        // HUD
        g.setColor(0x1A1224);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFF8A80);
        g.drawString("HP " + hp + "/" + maxHp, 2, 1, Gfx.TL);
        g.setColor(0xFFD54F);
        g.drawString("F" + depth + " L" + lvl, W - 2, 1, Gfx.TR);
        int by = H - hud * 2;
        g.setColor(0x1A1224);
        g.fillRect(0, by, W, hud * 2);
        g.setFont(Gfx.SMALL);
        g.setColor(0xB39DDB);
        g.drawString("ATK " + atk + " DEF " + def + " !" + potions + " $" + gold, W / 2, by + 1, Gfx.TC);
        g.setColor(msgColor);
        String m = msg;
        while (Gfx.SMALL.stringWidth(m) > W - 4 && m.length() > 4) m = m.substring(0, m.length() - 1);
        g.drawString(m, W / 2, by + hud, Gfx.TC);
    }

    private void drawOverview(Graphics g, int hud) {
        int s = Math.max(2, Math.min(W / MW, (H - hud * 2) / MH));
        int ox = (W - s * MW) / 2, oy = hud + (H - hud * 2 - s * MH) / 2;
        for (int i = 0; i < map.length; i++) {
            if (!seen[i]) continue;
            g.setColor(map[i] == WALL ? 0x5A4C6A : (map[i] == STAIRS ? 0xFFFFFF : 0x2B2433));
            g.fillRect(ox + (i % MW) * s, oy + (i / MW) * s, s, s);
        }
        g.setColor(0x4FC3F7);
        g.fillRect(ox + px * s - 1, oy + py * s - 1, s + 2, s + 2);
        Gfx.text(g, "MAP - floor " + depth, W / 2, 1, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF);
        Gfx.text(g, "# or 5: back", W / 2, H - hud, Gfx.TC, Gfx.SMALL, 0xB39DDB);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int cell = Math.max(10, Math.min(h / 3, w / 8));
        int sc = Math.max(1, cell / 8);
        if (hero == null || heroScale != sc) buildHero(sc);
        int n = w / cell;
        int x0 = x + (w - n * cell) / 2, y0 = y + (h - cell * 3) / 2;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < n; c++) {
                boolean wall = r != 1;
                g.setColor(wall ? 0x6D5D7E : 0x2B2433);
                g.fillRect(x0 + c * cell, y0 + r * cell, cell, cell);
            }
        }
        int hx = (clock / 6) % n;
        g.drawImage(hero, x0 + hx * cell + cell / 2, y0 + cell + cell / 2, Gfx.CC);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFF5252);
        g.drawChar('D', x0 + (n - 1) * cell + cell / 2 + 1, y0 + cell + (cell - 7) / 2, Gfx.TC);
    }
}
