package pocketcricket;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Pocket Cricket: a run chase. Watch the bowler's line and length, then
 * play your shot direction with the right timing as the ball arrives.
 */
public class CricketGame extends Game {
    private static final int RUNUP = 0, DELIVERY = 1, RESULT = 2;
    private static final String[] LENGTH = { "yorker", "full", "good length", "short" };

    private int phase, t, balls, ballsTotal, wickets, wicketsTotal, runs, target;
    private int line, length, pace, shotDir, shotT, deliverT;
    private String result = "", sub = "";
    private int resultCol, resX, resY, resVX, resVY;
    private boolean shotPlayed;

    protected String name() { return "Pocket Cricket"; }

    protected String[] help() {
        return new String[] {
            "Chase down the target before you run out of balls or wickets.",
            "As the ball reaches you, press a direction to play a shot: 4 leg side, 6 off side, 2 a lofted drive straight back (sixes - but catches!), 8 a safe defensive block.",
            "Timing is everything: too early or too late and you edge it or miss. Leave a straight ball alone and it may hit the stumps!",
            "Watch the length: short balls sit up for big shots, yorkers are hard to get away.",
            "- Controls",
            "4 / 6 / 2: shot direction",
            "8: block",
        };
    }

    protected String[] modes() { return new String[] { "2-over chase", "Super over", "5-over chase" }; }

    protected int accent() { return 0x52B788; }

    protected void newGame() {
        ballsTotal = mode == 1 ? 6 : (mode == 0 ? 12 : 30);
        wicketsTotal = mode == 1 ? 2 : (mode == 0 ? 3 : 5);
        target = mode == 1 ? Rnd.range(14, 19) : (mode == 0 ? Rnd.range(22, 28) : Rnd.range(45, 55));
        balls = wickets = runs = 0;
        nextBall();
    }

    private void nextBall() {
        phase = RUNUP;
        t = 0;
        line = Rnd.range(-1, 1);
        length = Rnd.nextInt(4);
        pace = Rnd.range(0, 2);
        shotPlayed = false;
        shotDir = -1;
    }

    private int travel() { return 26 - pace * 4; }

    protected void update() {
        t++;
        switch (phase) {
            case RUNUP:
                if (t > 22) {
                    phase = DELIVERY;
                    t = 0;
                    Sfx.tone(55, 30);
                }
                break;
            case DELIVERY: {
                int dir = -1;
                if ((pressed & K_LEFT) != 0) dir = 0;
                else if ((pressed & K_RIGHT) != 0) dir = 1;
                else if ((pressed & K_UP) != 0) dir = 2;
                else if ((pressed & K_DOWN) != 0) dir = 3;
                if (dir >= 0 && !shotPlayed) {
                    shotPlayed = true;
                    shotDir = dir;
                    shotT = t;
                    resolve();
                    return;
                }
                if (t > travel() + 4) {
                    shotDir = -1;
                    resolve();
                }
                break;
            }
            default:
                resX += resVX;
                resY += resVY;
                if (t > 40 || ((pressed & K_FIRE) != 0 && t > 10)) {
                    if (runs >= target) {
                        headline = "WON BY " + (wicketsTotal - wickets) + " WKT" + (wicketsTotal - wickets == 1 ? "" : "S");
                        score = runs * 10 + (ballsTotal - balls) * 20;
                        endGame(true);
                        return;
                    }
                    if (balls >= ballsTotal || wickets >= wicketsTotal) {
                        headline = "LOST BY " + (target - runs - 1 < 0 ? 0 : target - runs - 1) + " RUNS";
                        score = runs * 10;
                        endGame(false);
                        return;
                    }
                    nextBall();
                }
                break;
        }
    }

    private void resolve() {
        balls++;
        phase = RESULT;
        int arrive = travel();
        int err = shotDir < 0 ? 99 : Math.abs(shotT - arrive); // 0 = perfect
        boolean straight = line == 0 || (line == -1 && length == 0);
        int got = 0;
        boolean out = false;
        String how = "";
        if (shotDir < 0) {
            if (straight && Rnd.chance(length == 0 ? 70 : 40)) { out = true; how = "BOWLED!"; }
            else how = "Left alone. Dot ball.";
        } else if (shotDir == 3) {
            if (err <= 3) how = Rnd.chance(30) ? "Blocked, quick single!" : "Solid defence.";
            else if (straight && Rnd.chance(35)) { out = true; how = "BOWLED through the gate!"; }
            else how = "Beaten! Dot ball.";
            if (how.startsWith("Blocked")) got = 1;
        } else {
            int quality = err == 0 ? 3 : (err <= 1 ? 2 : (err <= 3 ? 1 : 0));
            if (length == 0) quality = Math.max(0, quality - 1);
            if (length == 3 && quality > 0) quality++;
            // playing to the wrong side of a wide line costs quality
            if ((shotDir == 0 && line == 1) || (shotDir == 1 && line == -1)) quality--;
            boolean lofted = shotDir == 2;
            if (quality <= 0) {
                if (Rnd.chance(lofted ? 45 : 25)) { out = true; how = lofted ? "Skied it... CAUGHT!" : "Edged... CAUGHT behind!"; }
                else if (straight && Rnd.chance(40)) { out = true; how = "Missed it - BOWLED!"; }
                else how = "Swing and a miss.";
            } else if (quality == 1) {
                got = Rnd.range(0, 2);
                how = got == 0 ? "Straight to a fielder." : got + " run" + (got > 1 ? "s" : "") + ".";
                if (lofted && Rnd.chance(30)) { out = true; got = 0; how = "Mistimed... CAUGHT!"; }
            } else if (quality == 2) {
                got = lofted ? (Rnd.chance(40) ? 6 : 2) : (Rnd.chance(50) ? 4 : 2);
                how = got == 6 ? "SIX!" : (got == 4 ? "FOUR!" : "2 runs.");
                if (lofted && got != 6 && Rnd.chance(20)) { out = true; got = 0; how = "Caught on the rope!"; }
            } else {
                got = lofted ? 6 : 4;
                how = got == 6 ? "SIX! Out of the ground!" : "FOUR! Perfect timing.";
            }
        }
        if (out) {
            wickets++;
            resultCol = 0xFF5252;
            Sfx.bad();
        } else {
            runs += got;
            resultCol = got >= 4 ? 0xFFEB3B : 0xFFFFFF;
            if (got >= 4) Sfx.win(); else if (got > 0) Sfx.good();
        }
        result = how;
        sub = runs + "/" + wickets + "  need " + Math.max(0, target - runs) + " off " + (ballsTotal - balls);
        // ball animation for the result
        resX = W / 2 << 8;
        resY = (H * 3 / 4) << 8;
        int dir = shotDir == 0 ? -1 : (shotDir == 1 ? 1 : 0);
        resVX = dir * (W << 8) / (got >= 4 ? 25 : 60);
        resVY = -(H << 8) / (got >= 4 ? 25 : 70);
        if (shotDir < 0 || out) resVX = resVY = 0;
        t = 0;
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x2E7D32);
        g.fillRect(0, 0, W, H);
        g.setColor(0x388E3C);
        for (int k = 0; k < 6; k++) g.fillRect(0, hud + k * (H - hud) / 6, W, (H - hud) / 12);
        // pitch in perspective
        int topY = hud + H / 8, botY = H - H / 10;
        int topW = W / 8, botW = W / 3;
        g.setColor(0xD7C49E);
        g.fillTriangle(W / 2 - topW, topY, W / 2 + topW, topY, W / 2 + botW, botY);
        g.fillTriangle(W / 2 - topW, topY, W / 2 + botW, botY, W / 2 - botW, botY);
        g.setColor(0xFFFFFF);
        g.drawLine(W / 2 - botW + 4, botY - H / 8, W / 2 + botW - 4, botY - H / 8);
        // stumps both ends
        for (int k = -1; k <= 1; k++) {
            g.fillRect(W / 2 + k * 3, botY - H / 7, 1, H / 14);
            g.fillRect(W / 2 + k * 2, topY - 5, 1, 5);
        }
        // bowler
        if (phase == RUNUP) {
            int by = topY - 30 + t;
            g.setColor(0x1565C0);
            g.fillRect(W / 2 + 6 - 2, by, 5, 8);
            g.setColor(0xFFCC80);
            Gfx.disc(g, W / 2 + 6, by - 2, 2);
        }
        // batsman
        int bx = W / 2 - W / 14, byy = botY - H / 8;
        g.setColor(0xFFFFFF);
        g.fillRect(bx - 3, byy - 14, 7, 12);
        g.setColor(0x1A237E);
        Gfx.disc(g, bx, byy - 17, 3);
        g.setColor(0x8D6E63);
        int swing = shotPlayed && phase != RUNUP ? (shotDir == 0 ? -6 : (shotDir == 1 ? 6 : 0)) : 2;
        g.drawLine(bx + 3, byy - 8, bx + 3 + swing, byy - 2 - (shotDir == 2 ? 8 : 0));
        g.drawLine(bx + 4, byy - 8, bx + 4 + swing, byy - 2 - (shotDir == 2 ? 8 : 0));
        // ball in flight
        if (phase == DELIVERY) {
            int arrive = travel();
            int p = Math.min(t, arrive + 4) * 256 / arrive; // 0..256+
            int bounceAt = 256 * (4 - length) / 6 + 40;
            int laneX = line * botW / 3;
            int y = topY + (botY - H / 8 - topY) * p / 256;
            int x = W / 2 + 4 + laneX * p / 256;
            int air = p < bounceAt ? (bounceAt - p) * 14 / 256 : (p - bounceAt) * 6 / 256;
            int r = 1 + p / 120;
            g.setColor(0x000000);
            g.fillRect(x - 1, y, 3, 1);
            g.setColor(0xD32F2F);
            Gfx.disc(g, x, y - air, r);
        } else if (phase == RESULT && (resVX != 0 || resVY != 0)) {
            g.setColor(0xD32F2F);
            Gfx.disc(g, resX >> 8, resY >> 8, 2);
        }
        g.setColor(0x1B5E20);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(runs + "/" + wickets, 2, 1, Gfx.TL);
        g.setColor(0xFFEB3B);
        g.drawString("Tgt " + target, W / 2, 1, Gfx.TC);
        g.setColor(0xC8E6C9);
        g.drawString((ballsTotal - balls) + "b", W - 2, 1, Gfx.TR);
        if (phase == DELIVERY && t < 8) Gfx.text(g, LENGTH[length] + (pace == 2 ? ", quick" : ""), W / 2, hud + 2, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
        if (phase == RESULT) {
            Gfx.shadowText(g, result, W / 2, H / 2 - 8, Gfx.TC, Gfx.fit(result, W - 6) == Gfx.LARGE ? Gfx.MEDIUM : Gfx.SMALL_B, resultCol, 0x000000);
            Gfx.text(g, sub, W / 2, H / 2 + 6, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
        }
        if (phase == RUNUP && balls == 0) Gfx.hint(g, "4 leg  6 off  2 loft  8 block", W, H);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0xD7C49E);
        g.fillRect(x + w / 2 - w / 8, y, w / 4, h);
        g.setColor(0xFFFFFF);
        for (int k = -1; k <= 1; k++) g.fillRect(x + w / 2 + k * 3, y + h - 12, 1, 10);
        int t = clock % 40;
        g.setColor(0xD32F2F);
        if (t < 20) Gfx.disc(g, x + w / 2, y + t * h / 20, 2);
        else Gfx.disc(g, x + w / 2 + (t - 20) * w / 40, y + h - (t - 20) * h / 20, 2);
    }
}
