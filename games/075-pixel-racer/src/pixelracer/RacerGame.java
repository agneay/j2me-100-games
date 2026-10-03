package pixelracer;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Pixel Racer: three-lap circuit races against three AI drivers, seen from
 * above. The AI follows the racing line through the track's waypoints.
 */
public class RacerGame extends Game {
    private static final int MW = 36, MH = 36, CARS = 4, LAPS = 3, UNIT = 256;
    private static final int[][] TRACKS = {
        { 6, 6, 29, 6, 31, 10, 31, 27, 27, 30, 18, 30, 15, 22, 9, 22, 6, 18 },
        { 5, 8, 14, 4, 22, 9, 30, 4, 32, 14, 24, 19, 31, 26, 26, 32, 10, 31, 4, 22, 9, 15 },
    };
    private static final int[] CAR_COL = { 0xFFEB3B, 0xE53935, 0x1E88E5, 0x43A047 };

    private final byte[] road = new byte[MW * MH];
    private int[] wp;
    private int nwp;
    private final int[] x = new int[CARS], y = new int[CARS], ang = new int[CARS], spd = new int[CARS], next = new int[CARS], lap = new int[CARS], progress = new int[CARS], maxSpd = new int[CARS];
    private final boolean[] done = new boolean[CARS];
    private int countdown, finishedOrder, myPlace;

    protected String name() { return "Pixel Racer"; }

    protected String[] help() {
        return new String[] {
            "Three laps against three rival cars. You are the yellow car.",
            "Stay on the tarmac: the grass slows you right down. Brake before tight corners - steering is sharper at lower speed. Bumping rivals slows you both.",
            "Finish first to win; you score points for your position.",
            "- Controls",
            "2 or 5: accelerate  8: brake",
            "4/6: steer",
        };
    }

    protected String[] modes() { return new String[] { "Oval GP", "Twisty GP" }; }

    protected int accent() { return 0xFFEB3B; }

    private void line(int x0, int y0, int x1, int y1) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0)) * 2;
        for (int i = 0; i <= steps; i++) {
            int cx = x0 + (x1 - x0) * i / Math.max(1, steps), cy = y0 + (y1 - y0) * i / Math.max(1, steps);
            for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                int xx = cx + dx, yy = cy + dy;
                if (xx >= 0 && yy >= 0 && xx < MW && yy < MH) road[yy * MW + xx] = 1;
            }
        }
    }

    protected void newGame() {
        wp = TRACKS[mode];
        nwp = wp.length / 2;
        for (int i = 0; i < road.length; i++) road[i] = 0;
        for (int i = 0; i < nwp; i++) {
            int j = (i + 1) % nwp;
            line(wp[i * 2], wp[i * 2 + 1], wp[j * 2], wp[j * 2 + 1]);
        }
        int a0 = FMath.atan2(wp[3] - wp[1], wp[2] - wp[0]);
        for (int c = 0; c < CARS; c++) {
            // grid: two rows behind the start line
            int back = 1 + c / 2, side = (c % 2) * 2 - 1;
            x[c] = (wp[0] * UNIT + UNIT / 2) - (FMath.cos(a0) * back * UNIT >> 10) + (FMath.sin(a0) * side * UNIT / 2 >> 10);
            y[c] = (wp[1] * UNIT + UNIT / 2) - (FMath.sin(a0) * back * UNIT >> 10) - (FMath.cos(a0) * side * UNIT / 2 >> 10);
            ang[c] = a0;
            spd[c] = 0;
            next[c] = 1;
            lap[c] = 0;
            progress[c] = 0;
            done[c] = false;
            maxSpd[c] = c == 0 ? 70 : 60 + c * 2 + Rnd.nextInt(4);
        }
        countdown = 60;
        finishedOrder = 0;
        myPlace = 0;
    }

    private boolean onRoad(int px, int py) {
        int tx = px / UNIT, ty = py / UNIT;
        return tx >= 0 && ty >= 0 && tx < MW && ty < MH && road[ty * MW + tx] == 1;
    }

    protected void update() {
        if (countdown > 0) {
            countdown--;
            if (countdown % 20 == 0) Sfx.tone(countdown == 0 ? 84 : 60, 60);
            return;
        }
        for (int c = 0; c < CARS; c++) {
            if (done[c]) { spd[c] = spd[c] * 9 / 10; move(c); continue; }
            if (c == 0) drivePlayer();
            else driveAI(c);
            move(c);
            checkpoint(c);
        }
        // bumping
        for (int a = 0; a < CARS; a++) for (int b = a + 1; b < CARS; b++) {
            int dx = x[a] - x[b], dy = y[a] - y[b];
            if (Math.abs(dx) < UNIT * 2 / 3 && Math.abs(dy) < UNIT * 2 / 3) {
                x[a] += dx / 4;
                y[a] += dy / 4;
                x[b] -= dx / 4;
                y[b] -= dy / 4;
                spd[a] = spd[a] * 8 / 10;
                spd[b] = spd[b] * 8 / 10;
                if (a == 0 || b == 0) Sfx.tone(40, 20);
            }
        }
        if ((frame & 3) == 0 && !done[0]) Sfx.tone(28 + spd[0] / 3, 25);
    }

    private void drivePlayer() {
        boolean gas = (held & (K_UP | K_FIRE)) != 0, brake = (held & K_DOWN) != 0;
        int max = onRoad(x[0], y[0]) ? maxSpd[0] : 22;
        if (gas) spd[0] += 3;
        else if (brake) spd[0] -= 5;
        else spd[0] -= 1;
        if (spd[0] > max) spd[0] -= 4;
        if (spd[0] < 0) spd[0] = 0;
        int turn = spd[0] > 50 ? 4 : (spd[0] > 10 ? 6 : 3);
        if ((held & K_LEFT) != 0) ang[0] = (ang[0] - turn) & 255;
        if ((held & K_RIGHT) != 0) ang[0] = (ang[0] + turn) & 255;
    }

    private void driveAI(int c) {
        int tx = wp[next[c] * 2] * UNIT + UNIT / 2, ty = wp[next[c] * 2 + 1] * UNIT + UNIT / 2;
        int want = FMath.atan2(ty - y[c], tx - x[c]);
        int diff = ((want - ang[c] + 128) & 255) - 128;
        ang[c] = (ang[c] + FMath.clamp(diff, -5, 5)) & 255;
        int max = maxSpd[c] - Math.abs(diff) / 3;
        if (!onRoad(x[c], y[c])) max = 22;
        if (spd[c] < max) spd[c] += 3;
        else spd[c] -= 3;
    }

    private void move(int c) {
        x[c] += FMath.cos(ang[c]) * spd[c] >> 10;
        y[c] += FMath.sin(ang[c]) * spd[c] >> 10;
        x[c] = FMath.clamp(x[c], UNIT, (MW - 1) * UNIT);
        y[c] = FMath.clamp(y[c], UNIT, (MH - 1) * UNIT);
    }

    private void checkpoint(int c) {
        int tx = wp[next[c] * 2] * UNIT + UNIT / 2, ty = wp[next[c] * 2 + 1] * UNIT + UNIT / 2;
        int dx = (x[c] - tx) / 16, dy = (y[c] - ty) / 16;
        if (dx * dx + dy * dy < (UNIT * 5 / 2 / 16) * (UNIT * 5 / 2 / 16)) {
            progress[c]++;
            if (next[c] == 0) {
                lap[c]++;
                if (lap[c] >= LAPS) {
                    done[c] = true;
                    finishedOrder++;
                    if (c == 0) finish(finishedOrder);
                    else Sfx.tone(50, 30);
                } else if (c == 0) Sfx.good();
            }
            next[c] = (next[c] + 1) % nwp;
        }
    }

    private void finish(int place) {
        myPlace = place;
        score = (5 - place) * 250 + Math.max(0, 3000 - frame) / 10;
        headline = place == 1 ? "1ST PLACE!" : (place == 2 ? "2ND PLACE" : (place == 3 ? "3RD PLACE" : "4TH PLACE"));
        endGame(place == 1);
    }

    private int place() {
        int p = 1;
        for (int c = 1; c < CARS; c++) if (progress[c] > progress[0]) p++;
        return p;
    }

    protected void draw(Graphics g) {
        int ts = Math.max(8, Math.min(W, H) / 10);
        int camX = x[0] * ts / UNIT - W / 2, camY = y[0] * ts / UNIT - H / 2;
        g.setColor(0x2E7D32);
        g.fillRect(0, 0, W, H);
        int c0 = Math.max(0, camX / ts), r0 = Math.max(0, camY / ts);
        for (int r = r0; r <= Math.min(MH - 1, (camY + H) / ts); r++) {
            for (int c = c0; c <= Math.min(MW - 1, (camX + W) / ts); c++) {
                int px = c * ts - camX, py = r * ts - camY;
                if (road[r * MW + c] == 1) {
                    g.setColor(0x546E7A);
                    g.fillRect(px, py, ts, ts);
                    boolean edge = (c > 0 && road[r * MW + c - 1] == 0) || (c < MW - 1 && road[r * MW + c + 1] == 0)
                            || (r > 0 && road[(r - 1) * MW + c] == 0) || (r < MH - 1 && road[(r + 1) * MW + c] == 0);
                    if (edge) {
                        g.setColor(((c + r) & 1) == 0 ? 0xE53935 : 0xFFFFFF);
                        g.fillRect(px + ts / 3, py + ts / 3, ts / 3, ts / 3);
                    }
                } else if (((c * 5 + r * 3) % 11) == 0) {
                    g.setColor(0x1B5E20);
                    Gfx.disc(g, px + ts / 2, py + ts / 2, ts / 3);
                }
            }
        }
        // start line
        int sx = (wp[0] * UNIT + UNIT / 2) * ts / UNIT - camX, sy = (wp[1] * UNIT + UNIT / 2) * ts / UNIT - camY;
        g.setColor(0xFFFFFF);
        g.fillRect(sx - 2, sy - ts, 4, ts * 2);
        for (int c = CARS - 1; c >= 0; c--) drawCar(g, x[c] * ts / UNIT - camX, y[c] * ts / UNIT - camY, ang[c], ts, CAR_COL[c]);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x000000);
        g.fillRect(0, 0, W, Gfx.SMALL.getHeight() + 2);
        g.setColor(0xFFEB3B);
        g.drawString("P" + place() + "/4", 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString("Lap " + Math.min(lap[0] + 1, LAPS) + "/" + LAPS, W / 2, 1, Gfx.TC);
        g.drawString(Gfx.time(frame, tickMs), W - 2, 1, Gfx.TR);
        if (countdown > 0) Gfx.shadowText(g, String.valueOf(countdown / 20 + 1), W / 2, H / 3, Gfx.TC, Gfx.LARGE, 0xFFEB3B, 0x000000);
        else if (frame < 20) Gfx.shadowText(g, "GO!", W / 2, H / 3, Gfx.TC, Gfx.LARGE, 0x76FF03, 0x000000);
    }

    private void drawCar(Graphics g, int px, int py, int a, int ts, int col) {
        int l = Math.max(4, ts * 2 / 5), w = Math.max(2, ts / 4);
        int c = FMath.cos(a), s = FMath.sin(a);
        int fx = c * l >> 10, fy = s * l >> 10, sx = -s * w >> 10, sy = c * w >> 10;
        g.setColor(col);
        g.fillTriangle(px + fx + sx, py + fy + sy, px + fx - sx, py + fy - sy, px - fx - sx, py - fy - sy);
        g.fillTriangle(px + fx + sx, py + fy + sy, px - fx - sx, py - fy - sy, px - fx + sx, py - fy + sy);
        g.setColor(0x212121);
        g.drawLine(px + fx / 3 + sx, py + fy / 3 + sy, px + fx / 3 - sx, py + fy / 3 - sy);
    }

    protected void drawTitleArt(Graphics g, int xx, int yy, int w, int h) {
        g.setColor(0x546E7A);
        g.fillRoundRect(xx + 6, yy + 2, w - 12, h - 4, h, h);
        g.setColor(0x2E7D32);
        g.fillRoundRect(xx + 6 + h / 3, yy + 2 + h / 3, w - 12 - h * 2 / 3, h - 4 - h * 2 / 3, h / 3, h / 3);
        for (int c = 0; c < 3; c++) {
            int t = (clock * 2 + c * 30) & 255;
            int rx = (w - 12 - h / 3) / 2, ry = (h - 4 - h / 3) / 2;
            int px = xx + w / 2 + (FMath.cos(t) * rx >> 10), py = yy + h / 2 + (FMath.sin(t) * ry >> 10);
            g.setColor(CAR_COL[c]);
            g.fillRect(px - 3, py - 2, 6, 4);
        }
    }
}
