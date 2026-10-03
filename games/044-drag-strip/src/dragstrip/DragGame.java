package dragstrip;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Drag Strip: quarter-mile drag races decided by launch and shift timing.
 * Win prize money, upgrade the car, beat five rivals.
 */
public class DragGame extends Game {
    private static final int GARAGE = 0, STAGING = 1, RACE = 2, FINISH = 3;
    private static final String[] RIVAL = { "Rusty", "Lena", "Kofi", "Mika", "The Baron" };
    private static final int[] RIVAL_SKILL = { 55, 65, 74, 83, 92 };
    private static final String[] PART = { "Engine", "Gearbox", "Tyres" };
    private static final int MAX_RPM = 8000, DIST = 4000; // decimetres

    private int phase, race, cash, sel;
    private final int[] lvl = new int[3];
    // race state
    private int rpm, gear, speed, dist, rSpeed, rDist, rGear, rRpm, countdown, shiftMsgT, launchQ;
    private String shiftMsg = "";
    private int raceTime, rTime;
    private Image car, rivalCar;

    protected String name() { return "Drag Strip"; }

    protected String[] help() {
        return new String[] {
            "Quarter-mile drag racing against five rivals of rising skill.",
            "Hold 5 during the countdown to rev the engine. The car launches when the last light comes on: the closer the needle is to 5800 rpm at that moment, the better your launch.",
            "During the race press 5 (or 6) to shift up when the needle reaches the green band. Shift too early and you bog down; too late and you hit the limiter.",
            "Win prize money and spend it in the garage on a better engine, a quicker gearbox and grippier tyres.",
            "- Controls",
            "5 (hold): rev at the start",
            "5 or 6: shift up",
            "Garage: 2/8 choose, 5 buy, # race",
        };
    }

    protected String formatScore(int s) { return "$" + s; }

    protected int accent() { return 0xE63946; }

    protected void newGame() {
        race = 0;
        cash = 200;
        lvl[0] = lvl[1] = lvl[2] = 0;
        phase = GARAGE;
        sel = 0;
        score = 0;
        if (car == null) {
            String[] art = { "....1111111.....", "...122222221....", "..12233322221...", "1122222222222211", "1222222222222222", ".14441111114441.", "..444......444.." };
            car = Gfx.sprite(art, new int[] { 0, 0x1A1A1A, 0xE63946, 0x90CAF9, 0x424242 }, 1);
            rivalCar = Gfx.sprite(art, new int[] { 0, 0x1A1A1A, 0x2A9D8F, 0x90CAF9, 0x424242 }, 1);
        }
    }

    private int cost(int p) { return 150 + lvl[p] * 200 + p * 30; }

    private void stage() {
        phase = STAGING;
        countdown = 60;
        rpm = 900;
        gear = 1;
        speed = 0;
        dist = 0;
        rSpeed = rDist = 0;
        rGear = 1;
        rRpm = 900;
        raceTime = rTime = 0;
        launchQ = 0;
    }

    private int gearRatio(int gr) { return 6 - gr; } // lower gears pull harder

    protected void update() {
        if (shiftMsgT > 0) shiftMsgT--;
        switch (phase) {
            case GARAGE:
                if ((pressed & K_UP) != 0) sel = (sel + 2) % 3;
                if ((pressed & K_DOWN) != 0) sel = (sel + 1) % 3;
                if ((pressed & K_FIRE) != 0) {
                    if (lvl[sel] < 4 && cash >= cost(sel)) {
                        cash -= cost(sel);
                        lvl[sel]++;
                        Sfx.good();
                    } else Sfx.bad();
                }
                if ((pressed & K_POUND) != 0) stage();
                break;
            case STAGING:
                countdown--;
                boolean rev = (held & (K_FIRE | (K_NUM0 << 6))) != 0;
                rpm += rev ? 380 : -250;
                rpm = FMath.clamp(rpm, 900, MAX_RPM);
                if (countdown <= 0) {
                    // launch quality: best around 5000-6500 rpm, improved by tyres
                    int ideal = 5800;
                    int err = Math.abs(rpm - ideal);
                    launchQ = Math.max(0, 100 - err / 30) + lvl[2] * 6;
                    speed = 40 + launchQ * (3 + lvl[2]) / 4;
                    phase = RACE;
                    shiftMsg = launchQ > 80 ? "PERFECT LAUNCH" : (launchQ > 50 ? "Good launch" : "Wheelspin!");
                    shiftMsgT = 25;
                    Sfx.tone(72, 80);
                }
                break;
            case RACE:
                race();
                break;
            default:
                if ((pressed & K_FIRE) != 0) {
                    if (race >= RIVAL.length) {
                        headline = "CHAMPION!";
                        score = cash;
                        endGame(true);
                        return;
                    }
                    phase = GARAGE;
                }
                break;
        }
    }

    private void race() {
        raceTime++;
        if ((pressed & (K_FIRE | (K_NUM0 << 6) | K_RIGHT)) != 0 && gear < 5) {
            int band = 6200 - lvl[1] * 100;
            if (rpm >= band && rpm <= 7600) { shiftMsg = "PERFECT SHIFT"; speed += 30 + lvl[1] * 8; Sfx.tone(84, 30); }
            else if (rpm < band - 1200) { shiftMsg = "Too early!"; speed -= 20; Sfx.tone(50, 30); }
            else { shiftMsg = "Good shift"; Sfx.tone(76, 25); }
            shiftMsgT = 18;
            gear++;
            rpm = Math.max(2500, rpm * gearRatio(gear) / gearRatio(gear - 1) - 600 + lvl[1] * 150);
        }
        // power curve peaks near 6500 rpm
        int torque = 100 - Math.abs(rpm - 6500) / 70;
        if (rpm >= MAX_RPM - 50) torque = 10; // limiter
        int accel = torque * (10 + lvl[0] * 3) * gearRatio(gear) / 140;
        speed += Math.max(0, accel) / 10 - speed / 400;
        rpm += accel * 3 + 40;
        rpm = Math.min(MAX_RPM, rpm);
        if (rpm >= MAX_RPM) { rpm = MAX_RPM - 300; speed -= 4; }
        dist += speed / 10;
        // rival: consistent but limited by skill
        int skill = RIVAL_SKILL[race];
        rTime++;
        if (rTime == 1) rSpeed = 40 + skill * 3 / 4;
        int rTorque = 100 - Math.abs(rRpm - 6500) / 70;
        int rAccel = rTorque * (10 + race * 3) * gearRatio(rGear) / 140 * skill / 100;
        rSpeed += Math.max(0, rAccel) / 10 - rSpeed / 400;
        rRpm += rAccel * 3 + 40;
        if (rRpm > 6600 + Rnd.range(-400, 400) && rGear < 5) {
            rGear++;
            rRpm = Math.max(2500, rRpm * gearRatio(rGear) / gearRatio(rGear - 1) - 600);
            if (Rnd.chance(skill)) rSpeed += 20;
        }
        rRpm = Math.min(MAX_RPM - 100, rRpm);
        rDist += rSpeed / 10;
        if (dist >= DIST || rDist >= DIST) finish();
        if ((frame & 3) == 0) Sfx.tone(24 + rpm / 220, 30);
    }

    private void finish() {
        phase = FINISH;
        boolean win = dist >= rDist;
        if (win) {
            int prize = 250 + race * 150;
            cash += prize;
            score += prize;
            shiftMsg = "YOU WIN! +$" + prize;
            race++;
            Sfx.win();
        } else {
            cash += 60;
            shiftMsg = RIVAL[race] + " wins. +$60";
            Sfx.lose();
        }
        shiftMsgT = 9999;
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x10121A);
        g.fillRect(0, 0, W, H);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString("$" + cash, 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString("Race " + Math.min(race + 1, RIVAL.length) + "/" + RIVAL.length, W - 2, 1, Gfx.TR);
        if (phase == GARAGE) {
            drawGarage(g, hud);
            return;
        }
        // track: two lanes
        int laneH = Math.max(18, H / 7);
        int y1 = H / 3, y2 = y1 + laneH + 4;
        g.setColor(0x37474F);
        g.fillRect(0, y1 - 4, W, laneH * 2 + 12);
        g.setColor(0xECEFF1);
        int scroll = (dist / 4) % 16;
        for (int x = -scroll; x < W; x += 16) g.fillRect(x, y1 + laneH + 1, 8, 2);
        // finish line
        int finishX = W * 3 / 4 + (DIST - dist) / 8;
        if (finishX < W) {
            for (int k = 0; k < (laneH * 2 + 8) / 3; k++) {
                g.setColor(k % 2 == 0 ? 0xFFFFFF : 0x000000);
                g.fillRect(finishX, y1 - 2 + k * 3, 3, 3);
            }
        }
        int myX = W / 4;
        int rivalX = myX + (rDist - dist) / 8;
        g.drawImage(rivalCar, rivalX, y1 + laneH / 2, Graphics.HCENTER | Graphics.VCENTER);
        g.drawImage(car, myX, y2 + laneH / 2, Graphics.HCENTER | Graphics.VCENTER);
        g.setFont(Gfx.SMALL);
        g.setColor(0x80CBC4);
        g.drawString(RIVAL[Math.min(race, RIVAL.length - 1)], 2, y1 - Gfx.SMALL.getHeight() - 4, Gfx.TL);
        // lights
        if (phase == STAGING) {
            int lit = countdown > 40 ? 0 : (countdown > 20 ? 1 : 2);
            for (int k = 0; k < 3; k++) {
                g.setColor(k <= lit ? 0xFFB300 : 0x3E2723);
                Gfx.disc(g, W / 2 - 12 + k * 12, hud + 10, 4);
            }
            Gfx.text(g, "Hold 5 to rev", W / 2, hud + 18, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
        } else if (phase == RACE && raceTime < 15) {
            g.setColor(0x00E676);
            Gfx.disc(g, W / 2, hud + 10, 5);
        }
        // tachometer
        int r = Math.max(14, Math.min(W / 5, (H - y2 - laneH) / 2 - 4));
        int cx = W / 3, cy = H - r - 4;
        g.setColor(0x212121);
        Gfx.disc(g, cx, cy, r);
        g.setColor(0x2E7D32);
        g.fillArc(cx - r + 2, cy - r + 2, (r - 2) * 2, (r - 2) * 2, 360 - (135 + 7600 * 270 / MAX_RPM) + 360, (7600 - 6200 + lvl[1] * 100) * 270 / MAX_RPM);
        g.setColor(0xC62828);
        g.fillArc(cx - r + 2, cy - r + 2, (r - 2) * 2, (r - 2) * 2, 360 - (135 + 270) + 360, 400 * 270 / MAX_RPM);
        g.setColor(0x212121);
        Gfx.disc(g, cx, cy, r - 5);
        int na = 135 + rpm * 270 / MAX_RPM; // degrees clockwise from +x on screen
        int a256 = na * 256 / 360;
        g.setColor(0xFFFFFF);
        g.drawLine(cx, cy, cx + (FMath.cos(a256) * (r - 3) >> 10), cy + (FMath.sin(a256) * (r - 3) >> 10));
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFEB3B);
        g.drawString("G" + gear, cx, cy + r / 3, Gfx.TC);
        g.setColor(0xFFFFFF);
        g.drawString((speed * 72 / 100) + " km/h", W * 3 / 4, cy - Gfx.SMALL.getHeight(), Gfx.TC);
        Gfx.bar(g, W / 2 + 4, cy + 4, W / 2 - 10, 5, Math.min(dist, DIST), DIST, 0xE63946, 0x37474F);
        Gfx.bar(g, W / 2 + 4, cy + 11, W / 2 - 10, 5, Math.min(rDist, DIST), DIST, 0x2A9D8F, 0x37474F);
        if (shiftMsgT > 0) Gfx.shadowText(g, shiftMsg, W / 2, y1 - Gfx.SMALL.getHeight() * 2 - 6, Gfx.TC, Gfx.SMALL_B, 0xFFEB3B, 0x000000);
        if (phase == FINISH) Gfx.text(g, "5: continue", W / 2, y2 + laneH + 8, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
    }

    private void drawGarage(Graphics g, int hud) {
        Gfx.text(g, "GARAGE", W / 2, hud + 2, Gfx.TC, Gfx.MEDIUM, 0xE63946);
        int lh = Gfx.SMALL.getHeight() + 6;
        int y0 = hud + Gfx.MEDIUM.getHeight() + 8;
        for (int p = 0; p < 3; p++) {
            int y = y0 + p * lh * 2;
            Gfx.panel(g, 4, y, W - 8, lh * 2 - 4, p == sel ? 0x3A1F24 : 0x1C1F2A, p == sel ? 0xE63946 : 0x37474F);
            g.setFont(Gfx.SMALL_B);
            g.setColor(0xFFFFFF);
            g.drawString(PART[p], 8, y + 3, Gfx.TL);
            for (int k = 0; k < 4; k++) {
                g.setColor(k < lvl[p] ? 0xFFD54F : 0x37474F);
                g.fillRect(8 + k * 9, y + lh, 7, 5);
            }
            g.setFont(Gfx.SMALL);
            g.setColor(lvl[p] >= 4 ? 0x9E9E9E : (cash >= cost(p) ? 0xA5D6A7 : 0xEF9A9A));
            g.drawString(lvl[p] >= 4 ? "MAX" : "$" + cost(p), W - 8, y + 3, Gfx.TR);
        }
        g.drawImage(car, W / 2, y0 + lh * 6 + 4, Graphics.HCENTER | Graphics.TOP);
        Gfx.text(g, "Next: " + RIVAL[Math.min(race, RIVAL.length - 1)], W / 2, H - Gfx.SMALL.getHeight() * 2 - 4, Gfx.TC, Gfx.SMALL, 0x80CBC4);
        Gfx.hint(g, "5 buy  # race!", W, H);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        if (car == null) newGame();
        g.setColor(0x37474F);
        g.fillRect(x, y + h / 2 - 2, w, h / 2);
        int t = (clock * 3) % (w + 40);
        g.drawImage(rivalCar, x + t - 20, y + h / 2 + 4, Graphics.HCENTER | Graphics.TOP);
        g.drawImage(car, x + (t * 11 / 10) % (w + 40) - 20, y + h - 10, Graphics.HCENTER | Graphics.TOP);
    }
}
