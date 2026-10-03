package monsterduel;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Monster Duel: raise one monster through eight trainer battles.
 * Six original species, six elements with a strengths/weaknesses chart.
 */
public class DuelGame extends Game {
    // elements: 0 fire 1 water 2 grass 3 rock 4 spark 5 shade
    private static final String[] SPECIES = { "Emberpup", "Ripplefin", "Thornbud", "Pebblehog", "Zapbulb", "Gloomfin" };
    private static final int[] ELEMENT = { 0, 1, 2, 3, 4, 5 };
    private static final int[] BODY = { 0, 1, 2, 0, 2, 1 }; // 0 pup, 1 fin, 2 bulb
    private static final int[] COL = { 0xFF7043, 0x29B6F6, 0x66BB6A, 0x9E9E9E, 0xFFEE58, 0x7E57C2 };
    private static final int[] ACC = { 0xFFD54F, 0xB3E5FC, 0xF06292, 0x616161, 0xFF7043, 0xB39DDB };
    private static final int[] BHP = { 39, 44, 45, 50, 35, 40 }, BATK = { 52, 48, 49, 45, 55, 50 }, BDEF = { 43, 50, 49, 60, 40, 45 }, BSPD = { 65, 45, 45, 30, 75, 60 };
    private static final String[] ELEM_NAME = { "Fire", "Water", "Grass", "Rock", "Spark", "Shade" };
    private static final String[] TYPE_MOVE = { "Ember", "Jet Spray", "Leaf Cut", "Rock Toss", "Zap", "Hex" };
    private static final String[] SPECIAL = { "Blaze", "Tidal Wave", "Bloom Storm", "Landslide", "Thunder", "Nightmare" };
    /** EFFECT[attacker][defender]: 2 = strong, 1 = normal, 0 = weak */
    private static final int[][] EFFECT = {
        { 0, 0, 2, 0, 1, 1 }, { 2, 0, 0, 2, 1, 1 }, { 0, 2, 0, 2, 1, 1 },
        { 2, 1, 0, 1, 2, 1 }, { 1, 2, 0, 0, 0, 1 }, { 1, 1, 1, 1, 2, 0 } };
    /** Trainer teams: species*100 + level, 0 = empty. */
    private static final int[][] TRAINERS = {
        { 304 }, { 105, 5 }, { 207, 406 }, { 309, 109 }, { 511, 211, 10 },
        { 413, 313 }, { 115, 215, 515 }, { 18, 118, 218 } };
    private static final String[] TRAINER_NAME = { "Rookie Tam", "Fisher Ola", "Gardener Bo", "Hiker Rex", "Mystic Nia", "Engineer Vi", "Ace Kirra", "Champion Sol" };

    private static final String[][] ART = {
        { "............", ".11......11.", ".121....121.", ".1222222221.", "122322223221", "122222222221",
          "122244442221", ".1222222221.", "..12222221..", "..12111121..", "..121..121..", "..11....11.." },
        { "............", "....1111....", "..11222211..", ".1222222221.", "12232222221.", "12222222221.",
          ".1244222221.", "..11222211..", "....1111.11.", ".........121", "..........1.", "............" },
        { "....1..1....", "...121121...", "....1221....", "...122221...", "..12222221..", ".1223223221.",
          ".1222222221.", ".1222442221.", "..12222221..", "...122221...", "..11.11.11..", "............" },
    };

    private static final int MENU = 0, MSG = 1, SWITCH = 2;

    // player monster
    private int pSpecies, pLevel, pXp, pHp, pMaxHp, pAtk, pDef, pSpd, pGuard, pSpecialPP, potions;
    // enemy
    private int trainer, eIdx, eSpecies, eLevel, eHp, eMaxHp, eAtk, eDef, eSpd, eGuard;
    private int phase, sel;
    private final String[] queue = new String[8];
    private int qHead, qTail, msgTimer, shake, eShake;
    private boolean enemyFainted, playerFainted;
    private Image[] sprites = new Image[6];
    private int spriteScale;

    protected String name() { return "Monster Duel"; }

    protected String[] help() {
        return new String[] {
            "Choose a partner on the title screen, then battle eight trainers in a row. Beat the Champion to win.",
            "Each turn pick a move: Tackle, your element attack, Guard (halves damage for two turns) or, from level 8, a powerful special move with 3 uses per battle.",
            "Elements: Fire beats Grass, Grass beats Water and Rock, Water beats Fire and Rock, Rock beats Fire and Spark, Spark beats Water, Shade beats Spark. Strong hits do double damage, weak ones half.",
            "Winning earns experience. Your monster heals fully after each trainer, and you get 3 potions per trainer.",
            "- Controls",
            "2/4/6/8: choose move  5: confirm",
            "0: use potion",
        };
    }

    protected String[] modes() { return new String[] { "Emberpup (Fire)", "Ripplefin (Water)", "Thornbud (Grass)" }; }

    protected int accent() { return 0x9B5DE5; }

    protected void newGame() {
        pSpecies = mode;
        pLevel = 5;
        pXp = 0;
        stats(true);
        pHp = pMaxHp;
        trainer = 0;
        startTrainer();
    }

    private static int stat(int base, int lvl) { return 5 + base * lvl / 25; }

    private void stats(boolean player) {
        if (player) {
            pMaxHp = 10 + lvl(BHP[pSpecies], pLevel) + pLevel;
            pAtk = stat(BATK[pSpecies], pLevel) + 2;
            pDef = stat(BDEF[pSpecies], pLevel) + 2;
            pSpd = stat(BSPD[pSpecies], pLevel);
        } else {
            eMaxHp = 10 + lvl(BHP[eSpecies], eLevel) + eLevel;
            eAtk = stat(BATK[eSpecies], eLevel);
            eDef = stat(BDEF[eSpecies], eLevel);
            eSpd = stat(BSPD[eSpecies], eLevel);
        }
    }

    private static int lvl(int base, int l) { return base * l * 2 / 25; }

    private void startTrainer() {
        eIdx = 0;
        potions = 3;
        pSpecialPP = 3;
        pHp = pMaxHp;
        pGuard = 0;
        qHead = qTail = 0;
        loadEnemy();
        say(TRAINER_NAME[trainer] + " wants to battle!");
        say(TRAINER_NAME[trainer] + " sends out " + SPECIES[eSpecies] + "!");
    }

    private void loadEnemy() {
        int v = TRAINERS[trainer][eIdx];
        eSpecies = v / 100;
        eLevel = v % 100;
        stats(false);
        eHp = eMaxHp;
        eGuard = 0;
        enemyFainted = false;
    }

    private void say(String s) {
        queue[qTail] = s;
        qTail = (qTail + 1) % queue.length;
        if (phase != MSG) {
            phase = MSG;
            msgTimer = 0;
        }
    }

    private String moveName(int m, int species) {
        switch (m) {
            case 0: return "Tackle";
            case 1: return TYPE_MOVE[ELEMENT[species]];
            case 2: return "Guard";
            default: return SPECIAL[ELEMENT[species]];
        }
    }

    private int damage(int m, int atkSpecies, int lvl, int atk, int defSpecies, int def, int guard) {
        int pow = m == 0 ? 40 : (m == 1 ? 60 : 90);
        int dmg = ((2 * lvl / 5 + 2) * pow * atk / Math.max(1, def)) / 50 + 2;
        if (m != 0) {
            int e = EFFECT[ELEMENT[atkSpecies]][ELEMENT[defSpecies]];
            if (e == 2) dmg *= 2;
            else if (e == 0) dmg /= 2;
            if (ELEMENT[atkSpecies] == ELEMENT[pSpecies] && atkSpecies == pSpecies) dmg = dmg * 5 / 4;
        }
        dmg = dmg * Rnd.range(85, 100) / 100;
        if (guard > 0) dmg /= 2;
        return Math.max(1, dmg);
    }

    private void effectText(int m, int a, int d) {
        if (m == 0 || m == 2) return;
        int e = EFFECT[ELEMENT[a]][ELEMENT[d]];
        if (e == 2) say("It's super effective!");
        else if (e == 0) say("It's not very effective...");
    }

    private void playerAction(int m) {
        if (m == 3 && (pLevel < 8 || pSpecialPP <= 0)) {
            Sfx.bad();
            return;
        }
        int em = enemyChoice();
        boolean playerFirst = pSpd >= eSpd || m == 2;
        if (em == 2 && m != 2) playerFirst = false;
        if (playerFirst) {
            doPlayer(m);
            if (!enemyFainted) doEnemy(em);
        } else {
            doEnemy(em);
            if (!playerFainted) doPlayer(m);
        }
        if (pGuard > 0) pGuard--;
        if (eGuard > 0) eGuard--;
    }

    private int enemyChoice() {
        if (eHp < eMaxHp / 3 && eGuard == 0 && Rnd.chance(25)) return 2;
        int e = EFFECT[ELEMENT[eSpecies]][ELEMENT[pSpecies]];
        if (e == 0 && Rnd.chance(70)) return 0;
        if (eLevel >= 12 && Rnd.chance(20)) return 3;
        return Rnd.chance(75) ? 1 : 0;
    }

    private void doPlayer(int m) {
        say(SPECIES[pSpecies] + " used " + moveName(m, pSpecies) + "!");
        if (m == 2) {
            pGuard = 2;
            return;
        }
        if (m == 3) pSpecialPP--;
        int d = damage(m, pSpecies, pLevel, pAtk, eSpecies, eDef, eGuard);
        eHp -= d;
        eShake = 8;
        Sfx.hit();
        effectText(m, pSpecies, eSpecies);
        if (eHp <= 0) {
            eHp = 0;
            enemyFainted = true;
            say(SPECIES[eSpecies] + " fainted!");
            int gain = eLevel * 4 + 4;
            pXp += gain;
            say("Gained " + gain + " XP.");
            while (pXp >= pLevel * 10) {
                pXp -= pLevel * 10;
                pLevel++;
                int oldMax = pMaxHp;
                stats(true);
                pHp += pMaxHp - oldMax;
                say(SPECIES[pSpecies] + " grew to level " + pLevel + "!");
                if (pLevel == 8) say("Learned " + SPECIAL[ELEMENT[pSpecies]] + "!");
            }
        }
    }

    private void doEnemy(int m) {
        say("Foe " + SPECIES[eSpecies] + " used " + moveName(m, eSpecies) + "!");
        if (m == 2) {
            eGuard = 2;
            return;
        }
        int d = damage(m, eSpecies, eLevel, eAtk, pSpecies, pDef, pGuard);
        pHp -= d;
        shake = 8;
        Sfx.tone(52, 40);
        effectText(m, eSpecies, pSpecies);
        if (pHp <= 0) {
            pHp = 0;
            playerFainted = true;
            say(SPECIES[pSpecies] + " fainted...");
        }
    }

    protected void update() {
        if (shake > 0) shake--;
        if (eShake > 0) eShake--;
        if (phase == MSG) {
            msgTimer++;
            if (msgTimer > 24 || ((pressed & K_FIRE) != 0 && msgTimer > 4)) {
                qHead = (qHead + 1) % queue.length;
                msgTimer = 0;
                if (qHead == qTail) afterMessages();
            }
            return;
        }
        if ((pressed & K_UP) != 0 || (pressed & K_DOWN) != 0) sel ^= 2;
        if ((pressed & K_LEFT) != 0 || (pressed & K_RIGHT) != 0) sel ^= 1;
        if (digit(0)) {
            if (potions > 0 && pHp < pMaxHp) {
                potions--;
                pHp = Math.min(pMaxHp, pHp + pMaxHp / 2);
                say("You used a potion.");
                Sfx.good();
                int em = enemyChoice();
                doEnemy(em);
            } else Sfx.bad();
            return;
        }
        if ((pressed & K_FIRE) != 0) playerAction(sel);
    }

    private void afterMessages() {
        phase = MENU;
        if (playerFainted) {
            score = trainer * 100 + pLevel * 20;
            headline = "DEFEATED";
            endGame(false);
            return;
        }
        if (enemyFainted) {
            eIdx++;
            if (eIdx < TRAINERS[trainer].length) {
                loadEnemy();
                say(TRAINER_NAME[trainer] + " sends out " + SPECIES[eSpecies] + "!");
            } else {
                trainer++;
                score = trainer * 100 + pLevel * 20;
                Sfx.win();
                if (trainer >= TRAINERS.length) {
                    headline = "CHAMPION!";
                    endGame(true);
                    return;
                }
                startTrainer();
            }
        }
    }

    private Image sprite(int sp, int scale) {
        if (spriteScale != scale) {
            sprites = new Image[6];
            spriteScale = scale;
        }
        if (sprites[sp] == null) sprites[sp] = Gfx.sprite(ART[BODY[sp]], new int[] { 0, 0x1A1A1A, COL[sp], 0x111111, ACC[sp] }, scale);
        return sprites[sp];
    }

    private void hpBox(Graphics g, int x, int y, int w, String nm, int lvl, int hp, int max, boolean showNum) {
        Gfx.panel(g, x, y, w, Gfx.SMALL.getHeight() * 2 + 6, 0xF5F5F5, 0x424242);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x212121);
        g.drawString(nm, x + 3, y + 2, Gfx.TL);
        g.drawString("L" + lvl, x + w - 3, y + 2, Gfx.TR);
        int by = y + Gfx.SMALL.getHeight() + 3;
        int bw = w - 8 - (showNum ? Gfx.SMALL.stringWidth("999") + 2 : 0);
        int c = hp * 2 > max ? 0x43A047 : (hp * 5 > max ? 0xFBC02D : 0xE53935);
        Gfx.bar(g, x + 3, by, bw, 4, hp, max, c, 0x424242);
        if (showNum) {
            g.setFont(Gfx.SMALL);
            g.drawString(String.valueOf(hp), x + w - 3, by - 2, Gfx.TR);
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight();
        int menuH = hud * 2 + 10;
        int fieldH = H - menuH;
        g.setColor(0xDCEDC8);
        g.fillRect(0, 0, W, fieldH);
        g.setColor(0xAED581);
        g.fillArc(W / 2, fieldH * 30 / 100, W / 2 - 4, fieldH / 6, 0, 360);
        g.fillArc(4, fieldH * 78 / 100, W / 2, fieldH / 6, 0, 360);
        int sc = Math.max(1, Math.min(W / 50, fieldH / 30));
        if (!enemyFainted || (clock & 2) == 0) {
            Image e = sprite(eSpecies, sc);
            int ex = W * 3 / 4 + (eShake > 0 && (eShake & 2) != 0 ? 2 : 0);
            g.drawImage(e, ex, fieldH * 30 / 100 + fieldH / 12, Graphics.HCENTER | Graphics.BOTTOM);
        }
        Image p = sprite(pSpecies, sc + 1);
        int px = W / 4 + (shake > 0 && (shake & 2) != 0 ? 2 : 0);
        g.drawRegion(p, 0, 0, p.getWidth(), p.getHeight(), 2, px, fieldH * 78 / 100 + fieldH / 12, Graphics.HCENTER | Graphics.BOTTOM);
        int bw = Math.min(W / 2 + 10, Gfx.SMALL_B.stringWidth("Ripplefin L18") + 12);
        hpBox(g, 3, 3, bw, SPECIES[eSpecies], eLevel, eHp, eMaxHp, false);
        hpBox(g, W - bw - 3, fieldH * 52 / 100, bw, SPECIES[pSpecies], pLevel, pHp, pMaxHp, true);
        g.setFont(Gfx.SMALL);
        g.setColor(0x33691E);
        g.drawString("Trainer " + (trainer + 1) + "/8", 3, fieldH * 52 / 100 + hud / 2, Gfx.TL);
        // bottom panel
        int y = fieldH;
        g.setColor(0x263238);
        g.fillRect(0, y, W, menuH);
        if (phase == MSG && qHead != qTail) {
            String m = queue[qHead];
            java.util.Vector lines = Gfx.wrap(m, Gfx.SMALL_B, W - 8);
            g.setFont(Gfx.SMALL_B);
            g.setColor(0xFFFFFF);
            for (int i = 0; i < lines.size() && i < 2; i++) g.drawString((String) lines.elementAt(i), 4, y + 4 + i * (hud + 2), Gfx.TL);
        } else {
            for (int k = 0; k < 4; k++) {
                int bx = 2 + (k % 2) * (W / 2), by2 = y + 3 + (k / 2) * (hud + 3);
                boolean locked = k == 3 && pLevel < 8;
                String label = locked ? "---" : moveName(k, pSpecies) + (k == 3 ? " " + pSpecialPP : "");
                if (k == sel) {
                    g.setColor(0xFFD54F);
                    g.fillRect(bx, by2 - 1, W / 2 - 4, hud + 2);
                }
                g.setFont(Gfx.SMALL_B);
                g.setColor(k == sel ? 0x212121 : (locked ? 0x607D8B : 0xFFFFFF));
                g.drawString(label, bx + 3, by2, Gfx.TL);
            }
            g.setFont(Gfx.SMALL);
            g.setColor(0x90A4AE);
            g.drawString("0: potion x" + potions + "   " + ELEM_NAME[ELEMENT[pSpecies]], W / 2, y + menuH - hud - 1, Gfx.TC);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int sc = Math.max(1, Math.min(h / 14, w / 40));
        Image a = sprite(mode, sc);
        Image b = sprite((clock / 30) % 3 + 3, sc);
        int hop = (clock / 4) % 4 == 0 ? 2 : 0;
        g.drawRegion(a, 0, 0, a.getWidth(), a.getHeight(), 2, x + w / 4, y + h - hop, Graphics.HCENTER | Graphics.BOTTOM);
        g.drawImage(b, x + w * 3 / 4, y + h, Graphics.HCENTER | Graphics.BOTTOM);
        Gfx.text(g, "VS", x + w / 2, y + h / 2 - 4, Gfx.TC, Gfx.SMALL_B, 0xFFD54F);
    }
}
