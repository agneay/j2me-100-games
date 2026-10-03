package checkpointrally;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Checkpoint Rally: a top-down time trial. Drive a free-rotating rally car
 * through numbered checkpoints in order, two laps, on a scrolling map.
 */
public class RallyGame extends Game {
    private static final int MW = 40, MH = 40, LAPS = 2;
    private static final int[][] TRACKS = {
        { 6, 6, 20, 4, 33, 7, 35, 18, 28, 26, 33, 34, 18, 35, 8, 31, 12, 20, 4, 14 },
        { 5, 5, 34, 5, 34, 14, 14, 14, 14, 23, 34, 23, 34, 34, 6, 34, 6, 25, 5, 15 },
    };
    private static final int UNIT = 256; // sub-pixel per tile pixel unit handled via ts

    private final byte[] map = new byte[MW * MH]; // 0 grass, 1 road, 2 tree, 3 water
    private int[] cps;
    private int ncp, next, lap;
    private int cx, cy, ang, speed; // car position in 1/256 tile units
    private int ts, bestLap, lapStart, flash;
    private String flashText = "";

    protected String name() { return "Checkpoint Rally"; }

    protected String[] help() {
        return new String[] {
            "A rally time trial. Drive through the numbered checkpoints in order - the arrow always points to the next one. Complete two laps as fast as you can.",
            "The dirt road is fastest. Grass slows you down, water slows you a lot and trees stop you dead.",
            "Steering works best while moving; brake to tighten your turns.",
            "- Controls",
            "2 or 5: accelerate  8: brake / reverse",
            "4/6: steer",
        };
    }

    protected String[] modes() { return new String[] { "Forest stage", "Lakeside stage" }; }

    protected boolean lowerIsBetter() { return true; }

    protected String formatScore(int s) { return Gfx.time(s, tickMs); }

    protected int accent() { return 0xE63946; }

    private void thickLine(int x0, int y0, int x1, int y1) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0)) * 2;
        for (int i = 0; i <= steps; i++) {
            int x = x0 + (x1 - x0) * i / Math.max(1, steps), y = y0 + (y1 - y0) * i / Math.max(1, steps);
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int xx = x + dx, yy = y + dy;
                    if (xx >= 0 && yy >= 0 && xx < MW && yy < MH) map[yy * MW + xx] = 1;
                }
            }
        }
    }

    protected void newGame() {
        int[] t = TRACKS[mode];
        cps = t;
        ncp = t.length / 2;
        for (int i = 0; i < map.length; i++) {
            int x = i % MW, y = i / MW;
            int h = (x * 7919 + y * 104729 + mode * 31) % 23;
            map[i] = (byte) (h == 0 || h == 5 ? 2 : (mode == 1 && ((x / 6 + y / 7) % 5 == 0) && h < 9 ? 3 : 0));
            if (x == 0 || y == 0 || x == MW - 1 || y == MH - 1) map[i] = 2;
        }
        for (int i = 0; i < ncp; i++) {
            int j = (i + 1) % ncp;
            thickLine(t[i * 2], t[i * 2 + 1], t[j * 2], t[j * 2 + 1]);
        }
        cx = t[0] * UNIT + UNIT / 2;
        cy = t[1] * UNIT + UNIT / 2;
        ang = FMath.atan2(t[3] - t[1], t[2] - t[0]);
        speed = 0;
        next = 1;
        lap = 0;
        lapStart = 0;
        bestLap = 0;
        flash = 0;
    }

    private int tileAt(int x, int y) {
        int tx = x / UNIT, ty = y / UNIT;
        if (x < 0 || y < 0 || tx >= MW || ty >= MH) return 2;
        return map[ty * MW + tx];
    }

    protected void update() {
        int surface = tileAt(cx, cy);
        int max = surface == 1 ? 80 : (surface == 3 ? 18 : 36);
        boolean gas = (held & (K_UP | K_FIRE)) != 0, brake = (held & K_DOWN) != 0;
        if (gas) speed += 3;
        else if (brake) speed -= 3;
        else speed = speed * 31 / 32;
        if (speed > max) speed -= 3;
        if (speed < -16) speed = -16;
        int turn = Math.min(6, Math.abs(speed) / 8 + (Math.abs(speed) > 2 ? 2 : 0));
        if (speed < 0) turn = -turn;
        if ((held & K_LEFT) != 0) ang = (ang - turn) & 255;
        if ((held & K_RIGHT) != 0) ang = (ang + turn) & 255;
        int nx = cx + (FMath.cos(ang) * speed >> 10), ny = cy + (FMath.sin(ang) * speed >> 10);
        if (tileAt(nx, ny) == 2) {
            speed = -speed / 3;
            Sfx.tone(40, 40);
        } else {
            cx = nx;
            cy = ny;
        }
        // checkpoint
        int px = cps[next * 2] * UNIT + UNIT / 2, py = cps[next * 2 + 1] * UNIT + UNIT / 2;
        int dx = (cx - px) / 16, dy = (cy - py) / 16;
        if (dx * dx + dy * dy < (UNIT * 2 / 16) * (UNIT * 2 / 16)) {
            if (next == 0) {
                lap++;
                int lt = frame - lapStart;
                if (bestLap == 0 || lt < bestLap) bestLap = lt;
                lapStart = frame;
                flashText = lap >= LAPS ? "FINISH!" : "LAP " + Gfx.time(lt, tickMs);
                if (lap >= LAPS) {
                    score = frame;
                    Sfx.win();
                    headline = "FINISHED " + Gfx.time(frame, tickMs);
                    endGame(true);
                    return;
                }
                Sfx.good();
            } else {
                flashText = "CHECKPOINT " + next;
                Sfx.tone(84, 40);
            }
            flash = 25;
            next = (next + 1) % ncp;
        }
        if (flash > 0) flash--;
        if (frame > 20 * 60 * 5) {
            headline = "TIME OUT";
            endGame(false);
        }
        if ((frame & 3) == 0 && speed != 0) Sfx.tone(28 + Math.abs(speed) / 2, 25);
    }

    protected void draw(Graphics g) {
        ts = Math.max(10, Math.min(W, H) / 9);
        int camX = cx * ts / UNIT - W / 2, camY = cy * ts / UNIT - H / 2;
        int c0 = Math.max(0, camX / ts), r0 = Math.max(0, camY / ts);
        g.setColor(0x33691E);
        g.fillRect(0, 0, W, H);
        for (int r = r0; r <= Math.min(MH - 1, (camY + H) / ts); r++) {
            for (int c = c0; c <= Math.min(MW - 1, (camX + W) / ts); c++) {
                int v = map[r * MW + c];
                int x = c * ts - camX, y = r * ts - camY;
                switch (v) {
                    case 1:
                        g.setColor(((c + r) & 1) == 0 ? 0xC8A26B : 0xBF9860);
                        g.fillRect(x, y, ts, ts);
                        break;
                    case 2:
                        g.setColor(0x558B2F);
                        g.fillRect(x, y, ts, ts);
                        g.setColor(0x1B5E20);
                        Gfx.disc(g, x + ts / 2, y + ts / 2, ts / 2 - 1);
                        g.setColor(0x2E7D32);
                        Gfx.disc(g, x + ts / 2 - 1, y + ts / 2 - 1, ts / 4);
                        break;
                    case 3:
                        g.setColor(((c + r + clock / 8) & 1) == 0 ? 0x1E88E5 : 0x1976D2);
                        g.fillRect(x, y, ts, ts);
                        break;
                    default:
                        g.setColor(((c * 3 + r) % 4) == 0 ? 0x689F38 : 0x7CB342);
                        g.fillRect(x, y, ts, ts);
                        break;
                }
            }
        }
        // checkpoints
        for (int i = 0; i < ncp; i++) {
            int x = cps[i * 2] * ts + ts / 2 - camX, y = cps[i * 2 + 1] * ts + ts / 2 - camY;
            if (x < -ts * 2 || y < -ts * 2 || x > W + ts * 2 || y > H + ts * 2) continue;
            boolean isNext = i == next;
            g.setColor(isNext ? ((clock & 4) == 0 ? 0xFFEB3B : 0xFF9800) : 0xFFFFFF);
            g.fillRect(x - ts * 3 / 2, y - 1, 3, 3);
            g.fillRect(x + ts * 3 / 2 - 3, y - 1, 3, 3);
            g.setFont(Gfx.SMALL_B);
            g.drawString(i == 0 ? "S" : String.valueOf(i), x, y - ts, Gfx.TC);
        }
        drawCar(g, W / 2, H / 2);
        // arrow to next checkpoint
        int tx = cps[next * 2] * UNIT + UNIT / 2 - cx, ty = cps[next * 2 + 1] * UNIT + UNIT / 2 - cy;
        int a = FMath.atan2(ty, tx);
        int ax = W / 2 + (FMath.cos(a) * (ts * 2) >> 10), ay = H / 2 + (FMath.sin(a) * (ts * 2) >> 10);
        int s = Math.max(4, ts / 3);
        g.setColor(0xFFEB3B);
        g.fillTriangle(ax + (FMath.cos(a) * s >> 10), ay + (FMath.sin(a) * s >> 10),
                ax + (FMath.cos(a + 96) * s >> 10), ay + (FMath.sin(a + 96) * s >> 10),
                ax + (FMath.cos(a - 96) * s >> 10), ay + (FMath.sin(a - 96) * s >> 10));
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x000000);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(Gfx.time(frame, tickMs), 2, 1, Gfx.TL);
        g.setColor(0xFFEB3B);
        g.drawString("Lap " + Math.min(lap + 1, LAPS) + "/" + LAPS + " CP" + next, W - 2, 1, Gfx.TR);
        if (flash > 0) Gfx.shadowText(g, flashText, W / 2, hud + 4, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
    }

    private void drawCar(Graphics g, int x, int y) {
        int l = Math.max(5, ts * 2 / 3), w = Math.max(3, ts * 2 / 5);
        int c = FMath.cos(ang), s = FMath.sin(ang);
        // corners: front/back along heading, left/right across it
        int fx = c * l >> 11, fy = s * l >> 11, sx = -s * w >> 11, sy = c * w >> 11;
        int x1 = x + fx + sx, y1 = y + fy + sy, x2 = x + fx - sx, y2 = y + fy - sy;
        int x3 = x - fx - sx, y3 = y - fy - sy, x4 = x - fx + sx, y4 = y - fy + sy;
        g.setColor(0x000000);
        g.fillTriangle(x1 + 1, y1 + 1, x2 + 1, y2 + 1, x3 + 1, y3 + 1);
        g.fillTriangle(x1 + 1, y1 + 1, x3 + 1, y3 + 1, x4 + 1, y4 + 1);
        g.setColor(0xE63946);
        g.fillTriangle(x1, y1, x2, y2, x3, y3);
        g.fillTriangle(x1, y1, x3, y3, x4, y4);
        g.setColor(0xA8DADC);
        g.drawLine(x + fx / 3 + sx * 2 / 3, y + fy / 3 + sy * 2 / 3, x + fx / 3 - sx * 2 / 3, y + fy / 3 - sy * 2 / 3);
        g.setColor(0xF1FAEE);
        g.drawLine(x - fx / 2, y - fy / 2, x + fx / 2, y + fy / 2);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x7CB342);
        g.fillRect(x, y, w, h);
        g.setColor(0xC8A26B);
        g.fillRoundRect(x + 6, y + 4, w - 12, h - 8, h / 2, h / 2);
        g.setColor(0x7CB342);
        g.fillRoundRect(x + 6 + h / 4, y + 4 + h / 4, w - 12 - h / 2, h - 8 - h / 2, h / 4, h / 4);
        ts = Math.max(10, h / 4);
        int t = clock * 3;
        int rx = (w - 12 - h / 4) / 2, ry = (h - 8 - h / 4) / 2;
        ang = (t + 64) & 255;
        drawCar(g, x + w / 2 + (FMath.cos(t) * rx >> 10), y + h / 2 + (FMath.sin(t) * ry >> 10));
    }
}
