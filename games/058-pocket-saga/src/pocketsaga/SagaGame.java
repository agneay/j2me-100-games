package pocketsaga;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Pocket Saga: a tiny console-style RPG. Walk the overworld, fight random
 * battles, rest and shop in towns, raid the cave and defeat the Dark Knight.
 */
public class SagaGame extends Game {
    private static final String[] MAP = {
        "^^^^^^^^^^^^^^^^^^^^^^^^",
        "^..........^^^^...fffK.^",
        "^.T........^^^...ffff..^",
        "^....fff...^^^..fffff..^",
        "^...ffff...^^^...ff....^",
        "^...fff.........fff..^^^",
        "^..............ff....^^^",
        "^~~~~~=~~~~~~~~~~~=~~~~^",
        "^..........ff..........^",
        "^...ff....ffff...T.....^",
        "^..ffff....ff..........^",
        "^...ff.........^^^.....^",
        "^..............^C......^",
        "^.S............^^^.....^",
        "^^^^^^^^^^^^^^^^^^^^^^^^",
    };
    private static final String[] FOE = { "Slime", "Bat", "Wolf", "Goblin", "Skeleton", "Ogre", "Wraith", "Dark Knight" };
    private static final int[] FHP = { 8, 10, 16, 22, 30, 45, 40, 140 }, FATK = { 3, 4, 6, 8, 10, 13, 14, 18 }, FDEF = { 0, 1, 2, 3, 5, 6, 7, 9 };
    private static final int[] FXP = { 3, 4, 7, 10, 15, 24, 28, 0 }, FGOLD = { 4, 5, 8, 12, 16, 25, 30, 0 };
    private static final int[] FCOL = { 0x66BB6A, 0x7E57C2, 0x8D6E63, 0x9CCC65, 0xEEEEEE, 0xBCAAA4, 0x80DEEA, 0x37474F };
    private static final int WALK = 0, BATTLE = 1, TOWN = 2, MSG = 3;
    private static final String[] TOWN_ITEMS = { "Rest at inn", "Potion", "Sword +2", "Armour +2", "Leave" };

    private int cols, rows, px, py, mode2, sel, msgT;
    private int hp, maxHp, mp, maxMp, atk, def, lvl, xp, gold, potions, swordLv, armorLv, caveStage;
    private boolean caveDone, heroSword;
    private int foe, foeHp, foeMax, foeShake, heroShake;
    private String msg = "", msg2 = "";
    private Image hero;
    private int heroScale;

    protected String name() { return "Pocket Saga"; }

    protected String[] help() {
        return new String[] {
            "Explore the kingdom, grow stronger and defeat the Dark Knight in the castle to the north-east.",
            "Monsters lurk in grass and especially in forests. Towns (T) have an inn and a shop. The cave (C) holds a legendary sword guarded by three fights in a row.",
            "In battle choose Attack, Magic (heal yourself or cast Fire, costs MP), Potion or Run.",
            "- Controls",
            "2/4/6/8: walk / choose",
            "5: confirm",
        };
    }

    protected int accent() { return 0x9B5DE5; }

    protected void newGame() {
        rows = MAP.length;
        cols = MAP[0].length();
        for (int y = 0; y < rows; y++) for (int x = 0; x < cols; x++) if (MAP[y].charAt(x) == 'S') { px = x; py = y; }
        hp = maxHp = 30;
        mp = maxMp = 10;
        atk = 5;
        def = 1;
        lvl = 1;
        xp = 0;
        gold = 20;
        potions = 2;
        swordLv = armorLv = 0;
        caveStage = 0;
        caveDone = heroSword = false;
        mode2 = WALK;
        say("Defeat the Dark Knight!", "The castle lies north-east.");
    }

    private char tile(int x, int y) {
        if (x < 0 || y < 0 || x >= cols || y >= rows) return '^';
        return MAP[y].charAt(x);
    }

    private void say(String a, String b) {
        msg = a;
        msg2 = b;
        msgT = 60;
    }

    private int xpNeed() { return lvl * 12; }

    protected void update() {
        if (msgT > 0) msgT--;
        if (foeShake > 0) foeShake--;
        if (heroShake > 0) heroShake--;
        switch (mode2) {
            case WALK: walk(); break;
            case BATTLE: battle(); break;
            case TOWN: town(); break;
            default:
                if ((pressed & K_FIRE) != 0 || msgT == 0) mode2 = WALK;
                break;
        }
    }

    private void walk() {
        int dx = 0, dy = 0;
        if ((pressed & K_LEFT) != 0) dx = -1;
        else if ((pressed & K_RIGHT) != 0) dx = 1;
        else if ((pressed & K_UP) != 0) dy = -1;
        else if ((pressed & K_DOWN) != 0) dy = 1;
        if (dx == 0 && dy == 0) return;
        char t = tile(px + dx, py + dy);
        if (t == '^' || t == '~') {
            Sfx.tone(40, 15);
            return;
        }
        px += dx;
        py += dy;
        if (t == 'T') {
            mode2 = TOWN;
            sel = 0;
            Sfx.good();
            return;
        }
        if (t == 'C' && !caveDone) {
            caveStage = 1;
            startBattle(Math.min(6, 4 + caveStage / 2));
            say("The cave is crawling", "with monsters!");
            return;
        }
        if (t == 'K') {
            startBattle(7);
            say("The Dark Knight", "draws his blade!");
            return;
        }
        int chance = t == 'f' ? 14 : (t == '=' ? 3 : 7);
        if (Rnd.nextInt(100) < chance) {
            int dist = Math.abs(px - 2) + Math.abs(py - 13);
            int tier = Math.min(6, dist / 6 + (t == 'f' ? 1 : 0) + Rnd.range(-1, 0));
            startBattle(Math.max(0, tier));
        }
    }

    private void startBattle(int f) {
        foe = f;
        foeMax = foeHp = FHP[f] + (f < 7 ? lvl : 0);
        mode2 = BATTLE;
        sel = 0;
        if (f < 7) say("A " + FOE[f] + " appears!", "");
        Sfx.tone(55, 60);
    }

    private void battle() {
        if ((pressed & (K_UP | K_DOWN)) != 0) sel ^= 2;
        if ((pressed & (K_LEFT | K_RIGHT)) != 0) sel ^= 1;
        if ((pressed & K_FIRE) == 0) return;
        String a = "", b = "";
        if (sel == 0) {
            int dmg = Math.max(1, atk + Rnd.range(0, 3) - FDEF[foe]);
            foeHp -= dmg;
            foeShake = 8;
            a = "You hit for " + dmg + ".";
            Sfx.hit();
        } else if (sel == 1) {
            if (mp < 4) {
                say("Not enough MP!", "");
                Sfx.bad();
                return;
            }
            mp -= 4;
            if (hp < maxHp / 2) {
                int heal = 12 + lvl * 3;
                hp = Math.min(maxHp, hp + heal);
                a = "Heal: +" + heal + " HP.";
            } else {
                int dmg = 8 + lvl * 3 + Rnd.nextInt(5);
                foeHp -= dmg;
                foeShake = 8;
                a = "Fire! " + dmg + " damage.";
            }
            Sfx.good();
        } else if (sel == 2) {
            if (potions <= 0) {
                say("No potions left!", "");
                Sfx.bad();
                return;
            }
            potions--;
            hp = Math.min(maxHp, hp + 25);
            a = "You drink a potion.";
            Sfx.good();
        } else {
            if (foe >= 6 || caveStage > 0 || Rnd.chance(35)) {
                if (foe >= 6 || caveStage > 0) {
                    a = "You can't escape!";
                } else {
                    a = "Couldn't get away!";
                }
            } else {
                mode2 = WALK;
                say("You fled.", "");
                return;
            }
        }
        if (foeHp <= 0) {
            win();
            return;
        }
        int edmg = Math.max(0, FATK[foe] + Rnd.range(-1, 2) - def);
        if (Rnd.chance(10)) edmg = 0;
        hp -= edmg;
        heroShake = 8;
        b = edmg > 0 ? FOE[foe] + " hits you: " + edmg : FOE[foe] + " misses!";
        say(a, b);
        if (hp <= 0) {
            hp = 0;
            headline = "DEFEATED";
            score = lvl * 100 + gold;
            endGame(false);
        }
    }

    private void win() {
        if (foe == 7) {
            score = lvl * 100 + gold + 2000;
            headline = "THE KINGDOM IS SAVED";
            endGame(true);
            return;
        }
        xp += FXP[foe];
        gold += FGOLD[foe];
        String b = "+" + FXP[foe] + " XP, +" + FGOLD[foe] + " gold";
        while (xp >= xpNeed()) {
            xp -= xpNeed();
            lvl++;
            maxHp += 8;
            maxMp += 3;
            atk += 2;
            def += 1;
            hp = maxHp;
            mp = maxMp;
            b = "LEVEL UP! Now level " + lvl;
            Sfx.win();
        }
        if (caveStage > 0) {
            caveStage++;
            if (caveStage <= 3) {
                startBattle(Math.min(6, 4 + caveStage / 2));
                say("Another monster attacks!", b);
                return;
            }
            caveStage = 0;
            caveDone = true;
            heroSword = true;
            atk += 6;
            say("You found the Hero Sword!", "Attack +6");
            mode2 = MSG;
            msgT = 80;
            return;
        }
        say(FOE[foe] + " defeated!", b);
        mode2 = MSG;
        msgT = 40;
        score = lvl * 100 + gold;
    }

    private void town() {
        int n = TOWN_ITEMS.length;
        if ((pressed & K_UP) != 0) sel = (sel + n - 1) % n;
        if ((pressed & K_DOWN) != 0) sel = (sel + 1) % n;
        if ((pressed & K_FIRE) == 0) return;
        int[] cost = { 10, 15, 40 + swordLv * 60, 35 + armorLv * 50, 0 };
        if (sel == 4) {
            mode2 = WALK;
            return;
        }
        if (gold < cost[sel]) {
            say("Not enough gold.", "");
            Sfx.bad();
            return;
        }
        gold -= cost[sel];
        if (sel == 0) { hp = maxHp; mp = maxMp; say("You feel rested.", ""); }
        else if (sel == 1) { potions++; say("Bought a potion.", ""); }
        else if (sel == 2) { swordLv++; atk += 2; say("Sword sharpened!", "Attack +2"); }
        else { armorLv++; def += 2; say("Armour reinforced!", "Defence +2"); }
        Sfx.good();
    }

    // ------------------------------------------------------------- drawing

    private void buildHero(int sc) {
        heroScale = sc;
        hero = Gfx.sprite(new String[] { "..111...", ".12221..", ".13331..", "..131...", ".14441.5", "1.444.15", "..4.4..5", ".11.11.." },
                new int[] { 0, 0x1A1A1A, 0xFFB300, 0xFFCC80, 0x5E35B1, 0xE0E0E0 }, sc);
    }

    private void drawTile(Graphics g, char t, int x, int y, int s) {
        switch (t) {
            case '^':
                g.setColor(0x6D6D6D);
                g.fillRect(x, y, s, s);
                g.setColor(0x9E9E9E);
                g.fillTriangle(x + s / 2, y + 1, x + 1, y + s - 1, x + s - 1, y + s - 1);
                g.setColor(0xFAFAFA);
                g.fillTriangle(x + s / 2, y + 1, x + s / 2 - s / 6, y + s / 3, x + s / 2 + s / 6, y + s / 3);
                break;
            case '~':
                g.setColor(((x / s + y / s + clock / 8) & 1) == 0 ? 0x1E88E5 : 0x1976D2);
                g.fillRect(x, y, s, s);
                break;
            case 'f':
                g.setColor(0x558B2F);
                g.fillRect(x, y, s, s);
                g.setColor(0x2E7D32);
                g.fillTriangle(x + s / 2, y + 1, x + 1, y + s - 2, x + s - 1, y + s - 2);
                break;
            case '=':
                g.setColor(0xA1887F);
                g.fillRect(x, y, s, s);
                g.setColor(0x6D4C41);
                g.drawLine(x, y + 1, x + s - 1, y + 1);
                g.drawLine(x, y + s - 2, x + s - 1, y + s - 2);
                break;
            case 'T':
                g.setColor(0x7CB342);
                g.fillRect(x, y, s, s);
                g.setColor(0xD84315);
                g.fillTriangle(x + s / 2, y + 1, x + 1, y + s / 2, x + s - 1, y + s / 2);
                g.setColor(0xFFE0B2);
                g.fillRect(x + 2, y + s / 2, s - 4, s / 2 - 1);
                break;
            case 'C':
                g.setColor(0x6D6D6D);
                g.fillRect(x, y, s, s);
                g.setColor(0x000000);
                g.fillArc(x + 2, y + s / 3, s - 4, s, 0, 180);
                break;
            case 'K':
                g.setColor(0x424242);
                g.fillRect(x + 1, y + s / 3, s - 2, s * 2 / 3);
                g.fillRect(x + 1, y + 1, s / 4, s / 3);
                g.fillRect(x + s - 1 - s / 4, y + 1, s / 4, s / 3);
                g.setColor(0xB71C1C);
                g.fillRect(x + s / 2 - 1, y, 2, s / 3);
                break;
            default:
                g.setColor(0x7CB342);
                g.fillRect(x, y, s, s);
                g.setColor(0x689F38);
                g.fillRect(x + s / 3, y + s / 2, 1, 2);
                break;
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x000000);
        g.fillRect(0, 0, W, H);
        if (mode2 == BATTLE) {
            drawBattle(g, hud);
            return;
        }
        int s = Math.max(8, Math.min(W / 11, (H - hud * 3) / 9));
        int vc = W / s + 1, vr = (H - hud * 3) / s + 1;
        int camX = px - vc / 2, camY = py - vr / 2;
        int ox = (W - vc * s) / 2, oy = hud;
        for (int y = 0; y < vr; y++) for (int x = 0; x < vc; x++) drawTile(g, tile(camX + x, camY + y), ox + x * s, oy + y * s, s);
        int sc = Math.max(1, s / 9);
        if (hero == null || heroScale != sc) buildHero(sc);
        g.drawImage(hero, ox + (px - camX) * s + s / 2, oy + (py - camY) * s + s / 2, Gfx.CC);
        drawStatus(g, hud);
        if (mode2 == TOWN) drawTown(g);
        else if (msgT > 0 || mode2 == MSG) {
            int by = H - hud * 2;
            g.setColor(0x1A1233);
            g.fillRect(0, by, W, hud * 2);
            Gfx.text(g, msg, W / 2, by + 1, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF);
            Gfx.text(g, msg2, W / 2, by + hud, Gfx.TC, Gfx.SMALL, 0xD1C4E9);
        }
    }

    private void drawStatus(Graphics g, int hud) {
        g.setColor(0x1A1233);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFF8A80);
        g.drawString("HP" + hp + "/" + maxHp, 2, 1, Gfx.TL);
        g.setColor(0x80D8FF);
        g.drawString("MP" + mp, W / 2, 1, Gfx.TC);
        g.setColor(0xFFD54F);
        g.drawString("L" + lvl + " $" + gold, W - 2, 1, Gfx.TR);
    }

    private void drawTown(Graphics g) {
        int lh = Gfx.SMALL.getHeight() + 3;
        int bh = lh * (TOWN_ITEMS.length + 1) + 6, bw = W - 16;
        int bx = 8, by = (H - bh) / 2;
        Gfx.panel(g, bx, by, bw, bh, 0x1A1233, 0x9B5DE5);
        int[] cost = { 10, 15, 40 + swordLv * 60, 35 + armorLv * 50, 0 };
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString("TOWN  $" + gold, W / 2, by + 3, Gfx.TC);
        g.setFont(Gfx.SMALL);
        for (int i = 0; i < TOWN_ITEMS.length; i++) {
            int y = by + 3 + lh * (i + 1);
            if (i == sel) {
                g.setColor(0x9B5DE5);
                g.fillRect(bx + 3, y - 1, bw - 6, lh);
            }
            g.setColor(0xFFFFFF);
            g.drawString(TOWN_ITEMS[i], bx + 6, y, Gfx.TL);
            if (cost[i] > 0) g.drawString("$" + cost[i], bx + bw - 6, y, Gfx.TR);
        }
        if (msgT > 0) Gfx.hint(g, msg, W, H);
    }

    private void drawBattle(Graphics g, int hud) {
        g.setColor(0x120B24);
        g.fillRect(0, 0, W, H);
        g.setColor(0x2A1B4D);
        g.fillArc(W / 4, H / 3, W / 2, H / 8, 0, 360);
        int s = Math.max(2, Math.min(W, H) / 30) * (foe == 7 ? 2 : 1);
        int ex = W / 2 + (foeShake > 0 && (foeShake & 2) != 0 ? 3 : 0), ey = H / 3 + H / 16;
        // monster built from simple shapes, tinted per foe
        g.setColor(FCOL[foe]);
        g.fillArc(ex - s * 5, ey - s * 8, s * 10, s * 9, 0, 360);
        if (foe == 1 || foe == 6) {
            g.fillTriangle(ex - s * 5, ey - s * 5, ex - s * 10, ey - s * 8, ex - s * 6, ey - s * 2);
            g.fillTriangle(ex + s * 5, ey - s * 5, ex + s * 10, ey - s * 8, ex + s * 6, ey - s * 2);
        }
        if (foe == 2 || foe == 3 || foe == 5 || foe == 7) {
            g.fillTriangle(ex - s * 4, ey - s * 7, ex - s * 2, ey - s * 11, ex - s, ey - s * 7);
            g.fillTriangle(ex + s * 4, ey - s * 7, ex + s * 2, ey - s * 11, ex + s, ey - s * 7);
        }
        g.setColor(foe == 7 ? 0xFF1744 : 0x000000);
        g.fillRect(ex - s * 2 - s / 2, ey - s * 5, s * 2, s * 2);
        g.fillRect(ex + s / 2, ey - s * 5, s * 2, s * 2);
        Gfx.bar(g, W / 4, hud + 4, W / 2, 5, foeHp, foeMax, 0xFF5252, 0x37474F);
        Gfx.text(g, FOE[foe], W / 2, hud + 10, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF);
        drawStatus(g, hud);
        int my = H - hud * 4;
        g.setColor(0x1A1233);
        g.fillRect(0, my, W, hud * 4);
        Gfx.text(g, msg, W / 2, my + 1, Gfx.TC, Gfx.SMALL, heroShake > 0 ? 0xFF8A80 : 0xFFFFFF);
        Gfx.text(g, msg2, W / 2, my + hud, Gfx.TC, Gfx.SMALL, 0xD1C4E9);
        String[] opt = { "Attack", "Magic", "Potion x" + potions, "Run" };
        for (int k = 0; k < 4; k++) {
            int x = 2 + (k % 2) * (W / 2), y = my + hud * 2 + (k / 2) * hud;
            if (k == sel) {
                g.setColor(0x9B5DE5);
                g.fillRect(x, y - 1, W / 2 - 4, hud);
            }
            Gfx.text(g, opt[k], x + 4, y, Gfx.TL, Gfx.SMALL_B, 0xFFFFFF);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(8, h / 3);
        for (int k = 0; k < w / s; k++) {
            drawTile(g, k % 4 == 1 ? 'f' : (k == w / s - 1 ? 'K' : '.'), x + k * s, y + h - s, s);
            drawTile(g, '^', x + k * s, y + h - s * 2, s);
        }
        int sc = Math.max(1, s / 9);
        if (hero == null || heroScale != sc) buildHero(sc);
        g.drawImage(hero, x + (clock / 4) % w, y + h - s / 2, Gfx.CC);
    }
}
