package arenachampion;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Arena Champion: read your opponent's wind-up, block high or low, then
 * punish the opening. Win six bouts; spend glory on upgrades in between.
 */
public class ArenaGame extends Game {
    private static final String[] FOE = { "Brutus", "Selene", "Gorm", "Twin Blades", "The Masked", "Champion Valka" };
    private static final int[] FOE_HP = { 40, 50, 65, 70, 85, 110 };
    private static final int[] FOE_DMG = { 6, 7, 9, 10, 12, 14 };
    private static final int[] FOE_TELL = { 16, 14, 12, 10, 9, 8 }; // wind-up ticks
    private static final String[] UPG = { "Strength +2", "Armour +1", "Stamina +20", "Vitality +15" };
    private static final int IDLE = 0, WINDUP = 1, RECOVER = 2, STUNNED = 3;
    private static final int FIGHT = 0, SHOP = 1, BOUT_OVER = 2;

    private int bout, phase, sel;
    private int hp, maxHp, str, armor, stam, maxStam;
    private int ehp, emax, estate, et, eHigh, eFeint;
    private int block, blockT, swingT, swingHeavy, hitFlash, eHitFlash;
    private String msg = "";
    private int msgT;

    protected String name() { return "Arena Champion"; }

    protected String[] help() {
        return new String[] {
            "Six gladiators stand between you and the title. Watch your opponent: when they raise their weapon high or low (the red arrow), block to match.",
            "A blocked attack leaves them stunned - that's your moment to strike for double damage. Quick strikes are safe; heavy strikes hit three times as hard but cost stamina and leave you open.",
            "After each win, choose an upgrade.",
            "- Controls",
            "2: block high  8: block low",
            "5: quick strike  6: heavy strike",
        };
    }

    protected int accent() { return 0xD4A373; }

    protected void newGame() {
        bout = 0;
        maxHp = hp = 60;
        str = 5;
        armor = 0;
        maxStam = stam = 60;
        startBout();
    }

    private void startBout() {
        emax = ehp = FOE_HP[bout];
        estate = IDLE;
        et = 30;
        phase = FIGHT;
        hp = maxHp;
        stam = maxStam;
        say(FOE[bout] + " enters the arena!");
    }

    private void say(String s) {
        msg = s;
        msgT = 40;
    }

    protected void update() {
        if (msgT > 0) msgT--;
        if (hitFlash > 0) hitFlash--;
        if (eHitFlash > 0) eHitFlash--;
        if (phase == SHOP) {
            if ((pressed & K_UP) != 0) sel = (sel + 3) % 4;
            if ((pressed & K_DOWN) != 0) sel = (sel + 1) % 4;
            if ((pressed & K_FIRE) != 0) {
                if (sel == 0) str += 2;
                else if (sel == 1) armor += 1;
                else if (sel == 2) maxStam += 20;
                else maxHp += 15;
                Sfx.good();
                startBout();
            }
            return;
        }
        if (phase == BOUT_OVER) {
            if ((pressed & K_FIRE) != 0 || msgT == 0) {
                if (bout >= FOE.length) {
                    headline = "ARENA CHAMPION!";
                    endGame(true);
                } else {
                    phase = SHOP;
                    sel = 0;
                }
            }
            return;
        }
        // player input
        if (stam < maxStam && (frame & 1) == 0) stam++;
        if (blockT > 0) blockT--;
        else block = 0;
        if ((pressed & K_UP) != 0) { block = 1; blockT = 8; }
        if ((pressed & K_DOWN) != 0) { block = 2; blockT = 8; }
        if (swingT > 0) {
            if (--swingT == 0) resolveSwing();
        } else if ((pressed & K_FIRE) != 0 && stam >= 8) {
            swingT = 3;
            swingHeavy = 0;
            stam -= 8;
        } else if ((pressed & K_RIGHT) != 0 && stam >= 25) {
            swingT = 7;
            swingHeavy = 1;
            stam -= 25;
        }
        // opponent
        et--;
        switch (estate) {
            case IDLE:
                if (et <= 0) {
                    estate = WINDUP;
                    eHigh = Rnd.chance(50) ? 1 : 2;
                    eFeint = bout >= 3 && Rnd.chance(20 + bout * 5) ? 1 : 0;
                    et = FOE_TELL[bout] + Rnd.range(-2, 2);
                }
                break;
            case WINDUP:
                if (eFeint == 1 && et == FOE_TELL[bout] / 2) {
                    eHigh = 3 - eHigh; // switch high/low halfway
                    eFeint = 2;
                }
                if (et <= 0) strike();
                break;
            case RECOVER:
                if (et <= 0) { estate = IDLE; et = Rnd.range(15, 35 - bout * 2); }
                break;
            default: // STUNNED
                if (et <= 0) { estate = IDLE; et = Rnd.range(10, 25); }
                break;
        }
        score = bout * 200 + (emax - ehp);
    }

    private void strike() {
        if (block == eHigh && blockT > 0) {
            estate = STUNNED;
            et = 22;
            say("Blocked! They're open!");
            Sfx.tone(84, 40);
            return;
        }
        int dmg = Math.max(1, FOE_DMG[bout] - armor + Rnd.range(0, 3));
        if (block != 0 && blockT > 0) dmg = dmg * 2 / 3; // wrong guard still helps a little
        hp -= dmg;
        hitFlash = 8;
        estate = RECOVER;
        et = 12;
        say("You take " + dmg + " damage");
        Sfx.bad();
        if (hp <= 0) {
            hp = 0;
            headline = "FALLEN IN BOUT " + (bout + 1);
            endGame(false);
        }
    }

    private void resolveSwing() {
        int dmg = str + Rnd.range(0, 3);
        if (swingHeavy == 1) dmg *= 3;
        if (estate == STUNNED) dmg *= 2;
        else if (estate == IDLE && Rnd.chance(35 + bout * 6)) {
            say(FOE[bout] + " parries!");
            Sfx.tone(70, 20);
            return;
        } else if (estate == WINDUP && swingHeavy == 0) {
            dmg = dmg / 2; // trading blows mid-swing
        }
        ehp -= dmg;
        eHitFlash = 8;
        say("Hit for " + dmg + (estate == STUNNED ? "! Critical!" : ""));
        Sfx.hit();
        if (estate == WINDUP && swingHeavy == 1) {
            estate = STUNNED; // a heavy blow interrupts the attack
            et = 12;
        }
        if (ehp <= 0) {
            ehp = 0;
            bout++;
            score = bout * 200;
            phase = BOUT_OVER;
            say("VICTORY over " + FOE[bout - 1] + "!");
            msgT = 60;
            Sfx.win();
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x3B2414);
        g.fillRect(0, 0, W, H);
        g.setColor(0x5D3A1A);
        g.fillRect(0, hud * 3, W, H / 3);
        for (int k = 0; k < W; k += 6) {
            g.setColor(((k / 6) & 1) == 0 ? 0x7B5B3A : 0x8C6A45);
            g.fillRect(k, hud * 3 + 4, 4, 5);
        }
        g.setColor(0xD2B48C);
        int floorY = H * 3 / 4;
        g.fillRect(0, floorY, W, H - floorY);
        if (phase == SHOP) {
            drawShop(g, hud);
            return;
        }
        int u = Math.max(6, Math.min(W, H) / 16);
        drawFighter(g, W / 3, floorY, u, 0x1565C0, block, swingT > 0 ? (swingHeavy == 1 ? 2 : 1) : 0, hitFlash, false);
        int eAct = estate == WINDUP ? eHigh : 0;
        drawFighter(g, W * 2 / 3, floorY, u, bout == 5 ? 0xFFD700 : 0x8E2424, estate == STUNNED ? 0 : 0, eAct == 0 ? 0 : 3, eHitFlash, true);
        if (estate == WINDUP) {
            int ax = W * 2 / 3, ay = eHigh == 1 ? floorY - u * 7 : floorY - u * 2;
            g.setColor(0xFF1744);
            if (eHigh == 1) g.fillTriangle(ax - 4, ay, ax + 4, ay, ax, ay - 6);
            else g.fillTriangle(ax - 4, ay, ax + 4, ay, ax, ay + 6);
            Gfx.text(g, eHigh == 1 ? "HIGH" : "LOW", ax, ay + (eHigh == 1 ? -16 : 8), Gfx.TC, Gfx.SMALL_B, 0xFF1744);
        }
        if (estate == STUNNED) Gfx.text(g, "STUNNED", W * 2 / 3, floorY - u * 9, Gfx.TC, Gfx.SMALL_B, 0xFFEB3B);
        // bars
        Gfx.bar(g, 3, 3, W / 2 - 6, 5, hp, maxHp, 0x43A047, 0x263238);
        Gfx.bar(g, 3, 10, W / 2 - 6, 3, stam, maxStam, 0x29B6F6, 0x263238);
        Gfx.bar(g, W / 2 + 3, 3, W / 2 - 6, 5, ehp, emax, 0xE53935, 0x263238);
        g.setFont(Gfx.SMALL);
        g.setColor(0xFFE0B2);
        g.drawString(FOE[Math.min(bout, FOE.length - 1)], W - 3, 14, Gfx.TR);
        g.drawString("Bout " + Math.min(bout + 1, FOE.length) + "/6", 3, 14, Gfx.TL);
        if (msgT > 0) Gfx.shadowText(g, msg, W / 2, H - hud, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
    }

    /** Stick-figure gladiator. guard: 0 none 1 high 2 low; swing: 0 none 1 quick 2 heavy 3 raised. */
    private void drawFighter(Graphics g, int x, int floorY, int u, int col, int guard, int swing, int flash, boolean facingLeft) {
        int dir = facingLeft ? -1 : 1;
        int c = flash > 0 && (flash & 2) != 0 ? 0xFFFFFF : col;
        g.setColor(0xFFCC80);
        Gfx.disc(g, x, floorY - u * 7, u);
        g.setColor(c);
        g.fillRect(x - u, floorY - u * 6, u * 2, u * 3);
        g.setColor(0x5D4037);
        g.fillRect(x - u, floorY - u * 3, u * 2 / 3, u * 3);
        g.fillRect(x + u / 3, floorY - u * 3, u * 2 / 3, u * 3);
        // shield
        g.setColor(0x9E9E9E);
        int sy = guard == 1 ? floorY - u * 8 : (guard == 2 ? floorY - u * 3 : floorY - u * 6);
        g.fillRoundRect(x + dir * u, sy, u, u * 3, u / 2, u / 2);
        // sword
        g.setColor(0xECEFF1);
        int hx = x - dir * u, hy = floorY - u * 5;
        if (swing == 3) g.drawLine(hx, hy, hx - dir * u, hy - u * 4);
        else if (swing > 0) {
            int reach = swing == 2 ? u * 5 : u * 4;
            g.drawLine(hx, hy, hx + dir * reach, hy + u);
            g.drawLine(hx, hy + 1, hx + dir * reach, hy + u + 1);
        } else g.drawLine(hx, hy, hx - dir * u, hy - u * 3);
    }

    private void drawShop(Graphics g, int hud) {
        Gfx.text(g, "Choose a reward", W / 2, hud + 4, Gfx.TC, Gfx.MEDIUM, 0xFFD54F);
        int lh = Gfx.SMALL.getHeight() + 6;
        for (int i = 0; i < 4; i++) {
            int y = hud * 3 + i * (lh + 4);
            Gfx.panel(g, 10, y, W - 20, lh, i == sel ? 0x6D4C41 : 0x3B2414, i == sel ? 0xFFD54F : 0x6D4C41);
            Gfx.text(g, UPG[i], W / 2, y + 3, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF);
        }
        Gfx.text(g, "Next: " + FOE[Math.min(bout, FOE.length - 1)], W / 2, H - hud * 2, Gfx.TC, Gfx.SMALL, 0xFFE0B2);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int u = Math.max(3, h / 9);
        boolean clash = (clock / 10) % 2 == 0;
        drawFighter(g, x + w / 3, y + h, u, 0x1565C0, clash ? 1 : 0, clash ? 0 : 1, 0, false);
        drawFighter(g, x + w * 2 / 3, y + h, u, 0x8E2424, 0, clash ? 3 : 0, 0, true);
    }
}
