package tenpin;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Ten Pin: ten-frame bowling with real strike/spare scoring. The ball and
 * pins are circles with simple momentum-transfer collisions.
 */
public class BowlingGame extends Game {
    private static final int POSITION = 0, AIM = 1, POWER = 2, ROLL = 3, RESULT = 4;
    private static final int[] PIN_ROW = { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3 };
    private static final int[] PIN_COL = { 0, -1, 1, -2, 0, 2, -3, -1, 1, 3 }; // half-spacing units

    private int laneX, laneW, laneTop, laneBot, rb, rp;
    private int phase, startX, aim, aimDir, power, powerDir, timer;
    private int bx, by, bvx, bvy;
    private boolean gutter;
    private final int[] px = new int[10], py = new int[10], pvx = new int[10], pvy = new int[10], px0 = new int[10], py0 = new int[10];
    private final boolean[] standing = new boolean[10], down = new boolean[10];
    private final int[] rolls = new int[21];
    private int nRolls, frameNo, rollInFrame;
    private String note = "";

    protected String name() { return "Ten Pin"; }

    protected String[] help() {
        return new String[] {
            "Ten frames of bowling. Each frame you get two balls to knock down all ten pins.",
            "Step 1: slide left or right to choose where you release. Step 2: stop the swinging aim arrow. Step 3: stop the power meter. A strong, well-aimed ball into the pocket just right of the head pin scatters the most pins.",
            "Strike (all ten with the first ball) scores 10 plus your next two balls; a spare scores 10 plus the next ball. Perfect game: 300.",
            "- Controls",
            "4/6: position  5: confirm each step",
        };
    }

    protected int accent() { return 0x52B788; }

    protected void newGame() {
        nRolls = 0;
        frameNo = 0;
        rollInFrame = 0;
        layout();
        rackPins(true);
        startTurn();
    }

    private void layout() {
        laneW = Math.max(40, Math.min(W * 55 / 100, (H * 40) / 100));
        laneX = (W - laneW) / 2;
        laneTop = Gfx.SMALL.getHeight() * 2 + 8;
        laneBot = H - 6;
        rb = Math.max(3, laneW / 11);
        rp = Math.max(2, laneW / 18);
    }

    private void rackPins(boolean all) {
        int spacing = laneW * 2 / 13; // half-spacing unit
        for (int i = 0; i < 10; i++) {
            px0[i] = (laneX + laneW / 2 + PIN_COL[i] * spacing) << 8;
            py0[i] = (laneTop + rp * 2 + (3 - PIN_ROW[i]) * spacing * 3 / 2) << 8;
            if (all) {
                standing[i] = true;
                down[i] = false;
            }
            if (standing[i]) {
                px[i] = px0[i];
                py[i] = py0[i];
            }
            pvx[i] = pvy[i] = 0;
        }
    }

    private void startTurn() {
        phase = POSITION;
        startX = laneX + laneW / 2;
        aim = 0;
        aimDir = 1;
        note = "Frame " + (frameNo + 1) + " - ball " + (rollInFrame + 1);
    }

    protected void update() {
        layout();
        switch (phase) {
            case POSITION:
                if ((held & K_LEFT) != 0) startX = Math.max(laneX + rb, startX - 2);
                if ((held & K_RIGHT) != 0) startX = Math.min(laneX + laneW - rb, startX + 2);
                if ((pressed & K_FIRE) != 0) phase = AIM;
                break;
            case AIM:
                aim += aimDir * 2;
                if (aim > 20 || aim < -20) aimDir = -aimDir;
                if ((pressed & K_FIRE) != 0) {
                    phase = POWER;
                    power = 0;
                    powerDir = 1;
                }
                break;
            case POWER:
                power += powerDir * 4;
                if (power >= 100) { power = 100; powerDir = -1; }
                if (power <= 0) { power = 0; powerDir = 1; }
                if ((pressed & K_FIRE) != 0) release();
                break;
            case ROLL:
                simulate();
                break;
            default:
                if (--timer <= 0 || (pressed & K_FIRE) != 0) nextBall();
                break;
        }
    }

    private void release() {
        phase = ROLL;
        bx = startX << 8;
        by = (laneBot - rb - 2) << 8;
        int sp = ((laneBot - laneTop) << 8) / (36 - power / 5);
        int a = 192 + aim;
        bvx = FMath.cos(a) * sp >> 10;
        bvy = FMath.sin(a) * sp >> 10;
        gutter = false;
        timer = 0;
        Sfx.tone(40, 80);
    }

    private void simulate() {
        timer++;
        bx += bvx;
        by += bvy;
        int bxp = bx >> 8;
        if (!gutter && (bxp - rb < laneX || bxp + rb > laneX + laneW)) {
            gutter = true;
            bvx = 0;
            bx = (bxp < laneX + laneW / 2 ? laneX - rb : laneX + laneW + rb) << 8;
            Sfx.tone(35, 120);
        }
        for (int i = 0; i < 10; i++) {
            if (!standing[i]) continue;
            if (!gutter) collide(i, true);
            for (int j = i + 1; j < 10; j++) if (standing[j]) collidePins(i, j);
        }
        for (int i = 0; i < 10; i++) {
            if (!standing[i]) continue;
            px[i] += pvx[i];
            py[i] += pvy[i];
            pvx[i] = pvx[i] * 15 / 16;
            pvy[i] = pvy[i] * 15 / 16;
            int dx = (px[i] - px0[i]) >> 8, dy = (py[i] - py0[i]) >> 8;
            if (dx * dx + dy * dy > rp * rp || (px[i] >> 8) < laneX || (px[i] >> 8) > laneX + laneW) down[i] = true;
        }
        boolean moving = false;
        for (int i = 0; i < 10; i++) if (standing[i] && (Math.abs(pvx[i]) > 20 || Math.abs(pvy[i]) > 20)) moving = true;
        if (((by >> 8) < laneTop - rb * 2 && !moving) || timer > 120) finishRoll();
    }

    private void collide(int i, boolean ball) {
        int dx = px[i] - bx, dy = py[i] - by;
        int r = (rb + rp) << 8;
        if (Math.abs(dx) > r || Math.abs(dy) > r) return;
        int d = FMath.sqrt((dx >> 4) * (dx >> 4) + (dy >> 4) * (dy >> 4)) << 4;
        if (d >= r || d == 0) return;
        // push the pin along the contact normal with the ball's speed
        int sp = FMath.sqrt((bvx >> 4) * (bvx >> 4) + (bvy >> 4) * (bvy >> 4)) << 4;
        pvx[i] = (int) ((long) dx * sp / d) * 11 / 10 + Rnd.range(-30, 30);
        pvy[i] = (int) ((long) dy * sp / d) * 11 / 10;
        px[i] = bx + (int) ((long) dx * r / d);
        py[i] = by + (int) ((long) dy * r / d);
        bvx = bvx * 92 / 100 - pvx[i] / 12;
        bvy = bvy * 95 / 100;
        Sfx.tone(70 + i, 15);
    }

    private void collidePins(int i, int j) {
        int dx = px[j] - px[i], dy = py[j] - py[i];
        int r = (rp * 2) << 8;
        if (Math.abs(dx) > r || Math.abs(dy) > r) return;
        int d = FMath.sqrt((dx >> 4) * (dx >> 4) + (dy >> 4) * (dy >> 4)) << 4;
        if (d >= r || d == 0) return;
        int vi = Math.abs(pvx[i]) + Math.abs(pvy[i]), vj = Math.abs(pvx[j]) + Math.abs(pvy[j]);
        if (vi < 40 && vj < 40) return;
        int sp = Math.max(vi, vj) * 3 / 4;
        int nx = (int) ((long) dx * sp / d), ny = (int) ((long) dy * sp / d);
        if (vi >= vj) {
            pvx[j] += nx;
            pvy[j] += ny;
            pvx[i] = pvx[i] / 2;
            pvy[i] = pvy[i] / 2;
        } else {
            pvx[i] -= nx;
            pvy[i] -= ny;
            pvx[j] = pvx[j] / 2;
            pvy[j] = pvy[j] / 2;
        }
    }

    private void finishRoll() {
        int knocked = 0;
        for (int i = 0; i < 10; i++) {
            if (standing[i] && down[i]) {
                standing[i] = false;
                knocked++;
            }
        }
        rolls[nRolls++] = knocked;
        int left = 0;
        for (int i = 0; i < 10; i++) if (standing[i]) left++;
        boolean strike = rollInFrame == 0 && left == 0;
        boolean spare = rollInFrame == 1 && left == 0;
        if (frameNo == 9 && rollInFrame == 2) spare = false;
        note = gutter && knocked == 0 ? "Gutter ball" : (strike ? "STRIKE!" : (spare ? "SPARE!" : knocked + " pins"));
        if (strike) Sfx.win();
        else if (spare) Sfx.good();
        phase = RESULT;
        timer = 30;
        score = total();
    }

    private void nextBall() {
        int left = 0;
        for (int i = 0; i < 10; i++) if (standing[i]) left++;
        if (frameNo < 9) {
            if (rollInFrame == 0 && left > 0) {
                rollInFrame = 1;
                rackPins(false);
            } else {
                frameNo++;
                rollInFrame = 0;
                rackPins(true);
            }
        } else {
            // tenth frame: up to three balls
            int f0 = rolls[nRolls - rollInFrame - 1];
            if (rollInFrame == 0) {
                rollInFrame = 1;
                rackPins(left == 0);
            } else if (rollInFrame == 1 && (f0 == 10 || f0 + rolls[nRolls - 1] == 10)) {
                rollInFrame = 2;
                rackPins(left == 0);
            } else {
                score = total();
                headline = score + " POINTS";
                endGame(score >= 100);
                return;
            }
        }
        startTurn();
    }

    private int total() {
        int t = 0, r = 0;
        for (int f = 0; f < 10 && r < nRolls; f++) {
            if (rolls[r] == 10) {
                t += 10 + (r + 1 < nRolls ? rolls[r + 1] : 0) + (r + 2 < nRolls ? rolls[r + 2] : 0);
                r++;
            } else if (r + 1 < nRolls && rolls[r] + rolls[r + 1] == 10) {
                t += 10 + (r + 2 < nRolls ? rolls[r + 2] : 0);
                r += 2;
            } else {
                t += rolls[r] + (r + 1 < nRolls ? rolls[r + 1] : 0);
                r += 2;
            }
        }
        return t;
    }

    protected void draw(Graphics g) {
        layout();
        g.setColor(0x1A1A2E);
        g.fillRect(0, 0, W, H);
        g.setColor(0x2B2B44);
        g.fillRect(laneX - rb * 2, laneTop - rp * 2, laneW + rb * 4, laneBot - laneTop + rp * 2);
        g.setColor(0xDEB887);
        g.fillRect(laneX, laneTop - rp * 2, laneW, laneBot - laneTop + rp * 2);
        g.setColor(0xCFA36D);
        for (int x = laneX + laneW / 8; x < laneX + laneW; x += laneW / 8) g.drawLine(x, laneTop - rp * 2, x, laneBot);
        g.setColor(0x8B5A2B);
        int arrowsY = laneTop + (laneBot - laneTop) * 2 / 3;
        for (int k = 1; k < 6; k++) {
            int ax = laneX + k * laneW / 6;
            g.fillTriangle(ax, arrowsY - 4, ax - 2, arrowsY, ax + 2, arrowsY);
        }
        for (int i = 0; i < 10; i++) {
            if (!standing[i] && !(phase == ROLL || phase == RESULT)) continue;
            if (!standing[i]) continue;
            int x = px[i] >> 8, y = py[i] >> 8;
            boolean fallen = down[i];
            g.setColor(fallen ? 0xBDBDBD : 0xFFFFFF);
            Gfx.disc(g, x, y, rp);
            g.setColor(0xD32F2F);
            g.fillRect(x - rp + 1, y - 1, rp * 2 - 1, 2);
        }
        if (phase >= ROLL || phase == POWER || phase == AIM || phase == POSITION) {
            int x = phase >= ROLL ? bx >> 8 : startX, y = phase >= ROLL ? by >> 8 : laneBot - rb - 2;
            if (phase != RESULT) {
                g.setColor(0x283593);
                Gfx.disc(g, x, y, rb);
                g.setColor(0x7986CB);
                Gfx.disc(g, x - rb / 3, y - rb / 3, Math.max(1, rb / 3));
            }
            if (phase == AIM || phase == POWER) {
                int a = 192 + aim, len = (laneBot - laneTop) / 3;
                g.setColor(0xFFEB3B);
                g.setStrokeStyle(Graphics.DOTTED);
                g.drawLine(x, y, x + (FMath.cos(a) * len >> 10), y + (FMath.sin(a) * len >> 10));
                g.setStrokeStyle(Graphics.SOLID);
            }
        }
        if (phase == POWER) Gfx.bar(g, laneX + laneW + rb * 2 + 2, laneTop, 6, laneBot - laneTop, power, 100, 0xFF7043, 0x263238);
        drawSheet(g);
        Gfx.text(g, phase == POSITION ? "4/6 move, 5 ok" : (phase == AIM ? "5: set aim" : (phase == POWER ? "5: power" : note)),
                W / 2, Gfx.SMALL.getHeight() + 3, Gfx.TC, Gfx.SMALL_B, 0xFFEB3B);
    }

    private void drawSheet(Graphics g) {
        int hgt = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x0D0D1A);
        g.fillRect(0, 0, W, hgt);
        g.setFont(Gfx.SMALL);
        int fw = W / 11;
        int r = 0;
        for (int f = 0; f < 10; f++) {
            int x = f * fw;
            g.setColor(f == frameNo ? 0x3949AB : 0x1F1F3A);
            g.fillRect(x + 1, 1, fw - 1, hgt - 2);
            String mark = "";
            if (r < nRolls) {
                if (rolls[r] == 10 && f < 9) { mark = "X"; r++; }
                else if (r + 1 < nRolls && rolls[r] + rolls[r + 1] == 10 && f < 9) { mark = rolls[r] + "/"; r += 2; }
                else if (f < 9) { mark = rolls[r] + (r + 1 < nRolls ? "" + rolls[r + 1] : ""); r += 2; }
                else {
                    // tenth frame: up to three marks
                    for (int k = r; k < nRolls; k++) {
                        int v = rolls[k];
                        boolean spareMark = k > r && rolls[k - 1] != 10 && rolls[k - 1] + v == 10;
                        mark += v == 10 ? "X" : (spareMark ? "/" : String.valueOf(v));
                    }
                    r = nRolls;
                }
            }
            g.setColor(0xFFFFFF);
            if (fw >= 12) g.drawString(mark.length() > 3 ? mark.substring(0, 3) : mark, x + fw / 2, 1, Gfx.TC);
        }
        g.setColor(0xFFD54F);
        g.drawString(String.valueOf(total()), W - 2, 1, Gfx.TR);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int rr = Math.max(3, Math.min(h / 8, w / 16));
        int t = clock % 60;
        for (int i = 0; i < 10; i++) {
            int pxx = x + w / 2 + PIN_COL[i] * rr * 2, pyy = y + rr + (3 - PIN_ROW[i]) * rr * 2;
            if (t > 40 && i % 3 == 0) pxx += (t - 40) * (PIN_COL[i] >= 0 ? 1 : -1);
            g.setColor(0xFFFFFF);
            Gfx.disc(g, pxx, pyy, rr);
            g.setColor(0xD32F2F);
            g.fillRect(pxx - rr + 1, pyy, rr * 2 - 1, 1);
        }
        g.setColor(0x283593);
        Gfx.disc(g, x + w / 2, y + h - rr - (h - rr * 2) * Math.min(t, 40) / 40, rr + 2);
    }
}
