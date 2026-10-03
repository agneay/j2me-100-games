package spotkick;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Spot Kick: a penalty shoot-out. The goal is split into a 3x3 grid that
 * matches keys 1-9: pick a corner to shoot, or guess where to dive.
 */
public class SpotKickGame extends Game {
    private static final int AIM = 0, POWER = 1, SHOT = 2, SHOT_RES = 3, SAVE_WAIT = 4, SAVE_SHOT = 5, SAVE_RES = 6;

    private int phase, aim = 5, power, powerDir, anim, keeperZone, shotZone, dive;
    private int round, myGoals, cpuGoals, kicksMe, kicksCpu;
    private boolean lastGoal, missed;
    private String resultText = "";
    private int resultColor;

    protected String name() { return "Spot Kick"; }

    protected String[] help() {
        return new String[] {
            "A penalty shoot-out against the CPU: five kicks each, then sudden death.",
            "Shooting: the goal is a 3x3 grid laid out like your keypad. Press 1-9 to aim (or move with the joystick and press 5), then stop the power bar with 5. Green is perfect; too hard and the ball flies over, too soft and it's easy to save.",
            "Saving: as the striker runs up, press 1-9 to dive to that part of the goal.",
            "- Controls",
            "1-9: aim / dive",
            "5: confirm aim, stop power",
        };
    }

    protected String[] modes() { return new String[] { "Amateur", "Pro", "Legend" }; }

    protected int accent() { return 0x52B788; }

    protected void newGame() {
        round = myGoals = cpuGoals = kicksMe = kicksCpu = 0;
        startShoot();
    }

    private void startShoot() {
        phase = AIM;
        aim = 5;
    }

    private void startSave() {
        phase = SAVE_WAIT;
        anim = 0;
        dive = 5;
        int r = Rnd.nextInt(100);
        shotZone = r < 60 ? (Rnd.chance(50) ? (Rnd.chance(50) ? 1 : 3) : (Rnd.chance(50) ? 7 : 9)) : 1 + Rnd.nextInt(9);
    }

    private static int col(int z) { return (z - 1) % 3; }
    private static int row(int z) { return (z - 1) / 3; }

    protected void update() {
        int d = digitPressed();
        switch (phase) {
            case AIM:
                if (d >= 1) {
                    aim = d;
                    phase = POWER;
                    power = 0;
                    powerDir = 1;
                } else {
                    int c = col(aim), r = row(aim);
                    if ((pressed & K_LEFT) != 0 && c > 0) c--;
                    if ((pressed & K_RIGHT) != 0 && c < 2) c++;
                    if ((pressed & K_UP) != 0 && r > 0) r--;
                    if ((pressed & K_DOWN) != 0 && r < 2) r++;
                    aim = r * 3 + c + 1;
                    if ((pressed & K_FIRE) != 0) { phase = POWER; power = 0; powerDir = 1; }
                }
                break;
            case POWER:
                power += powerDir * (4 + mode * 2);
                if (power >= 100) { power = 100; powerDir = -1; }
                if (power <= 0) { power = 0; powerDir = 1; }
                if ((pressed & K_FIRE) != 0 || d == 5) shoot();
                break;
            case SHOT:
            case SAVE_SHOT:
                if (++anim >= 12) resolve();
                break;
            case SHOT_RES:
            case SAVE_RES:
                if (++anim > 30 || ((pressed & K_FIRE) != 0 && anim > 8)) next();
                break;
            default: // SAVE_WAIT
                anim++;
                if (d >= 1) dive = d;
                else {
                    int c = col(dive), r = row(dive);
                    if ((pressed & K_LEFT) != 0 && c > 0) c--;
                    if ((pressed & K_RIGHT) != 0 && c < 2) c++;
                    if ((pressed & K_UP) != 0 && r > 0) r--;
                    if ((pressed & K_DOWN) != 0 && r < 2) r++;
                    dive = r * 3 + c + 1;
                }
                if (anim >= 34 - mode * 6) {
                    phase = SAVE_SHOT;
                    anim = 0;
                    Sfx.tone(55, 40);
                }
                break;
        }
    }

    private void shoot() {
        phase = SHOT;
        anim = 0;
        shotZone = aim;
        int read = 20 + mode * 15;
        keeperZone = Rnd.chance(read) ? aim : 1 + Rnd.nextInt(9);
        if (Rnd.chance(read / 2)) keeperZone = row(keeperZone) * 3 + col(aim) + 1;
        Sfx.tone(50, 50);
    }

    private void resolve() {
        boolean saved;
        if (phase == SHOT) {
            missed = power > 94 || (power > 86 && row(shotZone) == 0);
            if (missed) saved = false;
            else if (power < 30) saved = Math.abs(col(keeperZone) - col(shotZone)) <= 1;
            else if (keeperZone == shotZone) saved = !(shotZone != 5 && power > 78 && Rnd.chance(45));
            else saved = col(keeperZone) == col(shotZone) && row(shotZone) == 1 && Rnd.chance(40);
            lastGoal = !missed && !saved;
            kicksMe++;
            if (lastGoal) {
                myGoals++;
                score += 100;
                resultText = "GOAL!";
                resultColor = 0x76FF03;
                Sfx.good();
            } else {
                resultText = missed ? "Over the bar!" : "Saved!";
                resultColor = 0xFF5252;
                Sfx.bad();
            }
            phase = SHOT_RES;
        } else {
            missed = Rnd.chance(8);
            boolean sameRowNeighbour = row(dive) == row(shotZone) && Math.abs(col(dive) - col(shotZone)) == 1;
            saved = !missed && (dive == shotZone || (sameRowNeighbour && Rnd.chance(25 - mode * 5)));
            kicksCpu++;
            if (!missed && !saved) {
                cpuGoals++;
                resultText = "They score.";
                resultColor = 0xFF5252;
                Sfx.bad();
            } else {
                score += 100;
                resultText = missed ? "Wide! Lucky." : "GREAT SAVE!";
                resultColor = 0x76FF03;
                Sfx.good();
            }
            keeperZone = dive;
            phase = SAVE_RES;
        }
        anim = 0;
    }

    private void next() {
        // decided?
        int left = 5;
        if (kicksMe <= 5 && kicksCpu <= 5) {
            int myLeft = left - kicksMe, cpuLeft = left - kicksCpu;
            if (myGoals > cpuGoals + cpuLeft || cpuGoals > myGoals + myLeft) { finish(); return; }
        } else if (kicksMe == kicksCpu && myGoals != cpuGoals) {
            finish();
            return;
        }
        if (kicksMe == 5 && kicksCpu == 5 && myGoals != cpuGoals) { finish(); return; }
        if (phase == SHOT_RES) startSave();
        else {
            round++;
            startShoot();
        }
    }

    private void finish() {
        boolean win = myGoals > cpuGoals;
        if (win) score += 500 + mode * 250;
        headline = myGoals + " - " + cpuGoals + (win ? " WIN!" : " LOSS");
        endGame(win);
    }

    // ------------------------------------------------------------- drawing

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        // pitch
        for (int i = 0; i < 8; i++) {
            g.setColor(i % 2 == 0 ? 0x2E8B3E : 0x339944);
            g.fillRect(0, hud + i * (H - hud) / 8, W, (H - hud) / 8 + 1);
        }
        int gw = W * 80 / 100, gh = gw * 2 / 5;
        int gx = (W - gw) / 2, gy = hud + Math.max(6, (H - hud) / 7);
        // net
        g.setColor(0x1F5C2A);
        g.fillRect(gx, gy, gw, gh);
        g.setColor(0x9FC4A6);
        for (int x = gx; x < gx + gw; x += 5) g.drawLine(x, gy, x, gy + gh);
        for (int y = gy; y < gy + gh; y += 5) g.drawLine(gx, y, gx + gw, y);
        // grid overlay during aim/save
        boolean showGrid = phase == AIM || phase == SAVE_WAIT;
        if (showGrid) {
            int sel = phase == AIM ? aim : dive;
            for (int z = 1; z <= 9; z++) {
                int zx = gx + col(z) * gw / 3, zy = gy + row(z) * gh / 3;
                if (z == sel) {
                    g.setColor(phase == AIM ? 0xFFEB3B : 0x4FC3F7);
                    g.drawRect(zx + 1, zy + 1, gw / 3 - 3, gh / 3 - 3);
                }
                g.setFont(Gfx.SMALL_B);
                g.setColor(z == sel ? 0xFFFFFF : 0xC8E6C9);
                g.drawString(String.valueOf(z), zx + gw / 6 + 1, zy + gh / 6 - 4, Gfx.TC);
            }
        }
        g.setColor(0xFFFFFF);
        g.fillRect(gx - 3, gy - 3, gw + 6, 3);
        g.fillRect(gx - 3, gy - 3, 3, gh + 3);
        g.fillRect(gx + gw, gy - 3, 3, gh + 3);
        g.drawLine(0, gy + gh, W, gy + gh);
        // keeper
        boolean diving = phase == SHOT || phase == SHOT_RES || phase == SAVE_SHOT || phase == SAVE_RES;
        int kz = phase == SAVE_SHOT || phase == SAVE_RES ? dive : (diving ? keeperZone : 5);
        int t = diving ? Math.min(12, phase == SHOT || phase == SAVE_SHOT ? anim : 12) : 0;
        int kx0 = gx + gw / 2, ky0 = gy + gh * 2 / 3;
        int kx1 = gx + col(kz) * gw / 3 + gw / 6, ky1 = gy + row(kz) * gh / 3 + gh / 6;
        int kx = kx0 + (kx1 - kx0) * t / 12, ky = ky0 + (ky1 - ky0) * t / 12;
        int kw = Math.max(6, gw / 9), kh = Math.max(10, gh / 2);
        boolean myKeeper = phase >= SAVE_WAIT;
        g.setColor(myKeeper ? 0x1E88E5 : 0xFDD835);
        g.fillRect(kx - kw / 2, ky - kh / 3, kw, kh * 2 / 3);
        g.setColor(0xFFCC80);
        Gfx.disc(g, kx, ky - kh / 3 - kw / 3, kw / 3 + 1);
        g.setColor(myKeeper ? 0x1E88E5 : 0xFDD835);
        int armSpread = t > 0 ? kw : kw / 2;
        g.fillRect(kx - kw / 2 - armSpread, ky - kh / 3, armSpread, 3);
        g.fillRect(kx + kw / 2, ky - kh / 3, armSpread, 3);
        // ball
        int spotX = W / 2, spotY = H - Math.max(16, H / 6);
        int bz = phase == SHOT || phase == SHOT_RES ? shotZone : (phase == SAVE_SHOT || phase == SAVE_RES ? shotZone : 0);
        int bx = spotX, by = spotY, br = Math.max(3, W / 30);
        if (bz > 0) {
            int tt = phase == SHOT || phase == SAVE_SHOT ? anim : 12;
            int tx = gx + col(bz) * gw / 3 + gw / 6, ty = gy + row(bz) * gh / 3 + gh / 6;
            if (missed && (phase == SHOT_RES || phase == SAVE_RES || tt > 8)) ty = gy - gh / 2;
            bx = spotX + (tx - spotX) * tt / 12;
            by = spotY + (ty - spotY) * tt / 12 - (tt * (12 - tt)) * gh / 200;
            br = Math.max(2, br - tt * br / 24);
        }
        g.setColor(0x000000);
        g.fillArc(bx - br, spotY + br - 2, br * 2, br, 0, 360);
        g.setColor(0xFFFFFF);
        Gfx.disc(g, bx, by, br);
        g.setColor(0x222222);
        g.fillRect(bx - 1, by - 1, 2, 2);
        // striker during save phase
        if (phase == SAVE_WAIT) {
            int run = Math.min(anim, 30);
            int sx = spotX - W / 4 + run * W / 4 / 30, sy = spotY + 6;
            g.setColor(0xE53935);
            g.fillRect(sx - 3, sy - 14, 6, 9);
            g.setColor(0xFFCC80);
            Gfx.disc(g, sx, sy - 17, 3);
            g.setColor(0xFFFFFF);
            g.fillRect(sx - 3, sy - 5, 2, 5);
            g.fillRect(sx + 1, sy - 5, 2, 5);
        }
        // power bar
        if (phase == POWER) {
            int bw = W * 2 / 3, bh = 8, bxx = (W - bw) / 2, byy = H - 14;
            g.setColor(0x222222);
            g.fillRect(bxx, byy, bw, bh);
            g.setColor(0x43A047);
            g.fillRect(bxx + bw * 55 / 100, byy, bw * 30 / 100, bh);
            g.setColor(0xE53935);
            g.fillRect(bxx + bw * 94 / 100, byy, bw * 6 / 100, bh);
            g.setColor(0xFFFFFF);
            g.fillRect(bxx + bw * power / 100 - 1, byy - 2, 3, bh + 4);
        }
        // scoreboard
        g.setColor(0x0B2E13);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString("YOU " + myGoals, 2, 1, Gfx.TL);
        g.drawString(cpuGoals + " CPU", W - 2, 1, Gfx.TR);
        g.setFont(Gfx.SMALL);
        g.setColor(0xA5D6A7);
        String sub = phase == AIM ? "Aim: 1-9" : (phase == POWER ? "5: shoot!" : (phase == SAVE_WAIT ? "Dive: 1-9" : "Kick " + (Math.max(kicksMe, kicksCpu))));
        g.drawString(sub, W / 2, 1, Gfx.TC);
        if (phase == SHOT_RES || phase == SAVE_RES) {
            Gfx.shadowText(g, resultText, W / 2, gy + gh + 6, Gfx.TC, Gfx.fit(resultText, W - 8), resultColor, 0x000000);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int gw = Math.min(w * 4 / 5, h * 2), gh = Math.min(h - 6, gw / 2);
        int gx = x + (w - gw) / 2, gy = y + 2;
        g.setColor(0x1F5C2A);
        g.fillRect(gx, gy, gw, gh);
        g.setColor(0xFFFFFF);
        g.drawRect(gx, gy, gw, gh);
        int t = clock % 30;
        int bx = x + w / 2 + (gw / 3) * t / 30, by = y + h - (h - gh / 3) * t / 30;
        Gfx.disc(g, bx, by, Math.max(2, 5 - t / 8));
    }
}
