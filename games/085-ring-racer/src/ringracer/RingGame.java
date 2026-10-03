package ringracer;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Ring Racer: a behind-the-ship space race through a course of rings.
 * Steer so each ring passes around you; every ring adds time, mines take it.
 * Rings are projected with a simple integer perspective divide.
 */
public class RingGame extends Game {
    private static final int MAXR = 8, FAR = 1600;
    private final int[] rx = new int[MAXR], ry = new int[MAXR], rz = new int[MAXR], kind = new int[MAXR];
    private final boolean[] on = new boolean[MAXR];
    private int sx, sy, speed, timeLeft, rings, streak, nextZ, msgT, tilt;
    private String msg = "";

    protected String name() { return "Ring Racer"; }

    protected String[] help() {
        return new String[] {
            "Race through a course of rings in deep space. Steer your ship so each ring passes around it.",
            "Every ring adds 2 seconds to the clock and builds a streak; red mines cost 5 seconds. The course speeds up as you go.",
            "- Controls",
            "2/4/6/8: steer",
            "5 (hold): boost",
        };
    }

    protected int accent() { return 0x00E5FF; }

    protected void newGame() {
        for (int i = 0; i < MAXR; i++) on[i] = false;
        sx = sy = 0;
        speed = 18;
        timeLeft = 30 * 20;
        rings = streak = 0;
        nextZ = 600;
    }

    private void spawn() {
        for (int i = 0; i < MAXR; i++) {
            if (on[i]) continue;
            on[i] = true;
            int spread = 60 + Math.min(80, rings * 3);
            rx[i] = Rnd.range(-spread, spread);
            ry[i] = Rnd.range(-spread * 2 / 3, spread * 2 / 3);
            rz[i] = FAR;
            kind[i] = rings > 5 && Rnd.chance(25) ? 1 : 0;
            return;
        }
    }

    protected void update() {
        if (msgT > 0) msgT--;
        tilt = 0;
        int st = 5;
        if ((held & K_LEFT) != 0) { sx -= st; tilt = -1; }
        if ((held & K_RIGHT) != 0) { sx += st; tilt = 1; }
        if ((held & K_UP) != 0) sy -= st;
        if ((held & K_DOWN) != 0) sy += st;
        sx = FMath.clamp(sx, -140, 140);
        sy = FMath.clamp(sy, -100, 100);
        int sp = speed + ((held & K_FIRE) != 0 ? 10 : 0);
        nextZ -= sp;
        if (nextZ <= 0) {
            spawn();
            nextZ = Math.max(220, 420 - rings * 5);
        }
        for (int i = 0; i < MAXR; i++) {
            if (!on[i]) continue;
            rz[i] -= sp;
            if (rz[i] <= 0) {
                on[i] = false;
                int dx = rx[i] - sx, dy = ry[i] - sy;
                int d = FMath.dist(dx, dy);
                if (kind[i] == 0) {
                    if (d < 34) {
                        rings++;
                        streak++;
                        timeLeft += 40;
                        score += 10 * Math.min(10, streak);
                        msg = streak > 2 ? "RING x" + streak : "RING!";
                        msgT = 15;
                        Sfx.tone(76 + Math.min(streak, 12), 30);
                        speed = 18 + rings / 4;
                    } else {
                        streak = 0;
                        msg = "Missed";
                        msgT = 15;
                        Sfx.tone(45, 30);
                    }
                } else if (d < 24) {
                    timeLeft -= 100;
                    streak = 0;
                    msg = "MINE! -5s";
                    msgT = 20;
                    Sfx.bad();
                }
            }
        }
        if (--timeLeft <= 0) {
            headline = rings + " RINGS";
            endGame(rings >= 30);
        }
    }

    protected void draw(Graphics g) {
        g.setColor(0x02030A);
        g.fillRect(0, 0, W, H);
        int cx = W / 2, cy = H / 2;
        int f = W * 3 / 4; // focal length
        // star streaks
        g.setColor(0x3949AB);
        for (int k = 0; k < 24; k++) {
            int z = ((k * 73 - frame * speed) % FAR + FAR) % FAR + 40;
            int x = ((k * 97) % 400 - 200), y = ((k * 53) % 300 - 150);
            int px = cx + (x - sx) * f / z, py = cy + (y - sy) * f / z;
            g.fillRect(px, py, 1, 1 + 200 / z);
        }
        // rings far to near, bucketed by depth
        for (int z0 = FAR; z0 > 0; z0 -= 100) {
            for (int i = 0; i < MAXR; i++) {
                if (!on[i] || rz[i] > z0 || rz[i] <= z0 - 100) continue;
                int z = Math.max(20, rz[i]);
                int px = cx + (rx[i] - sx) * f / z, py = cy + (ry[i] - sy) * f / z;
                int r = 36 * f / z;
                if (kind[i] == 0) {
                    g.setColor(z < 300 ? 0x00E5FF : 0x0097A7);
                    Gfx.ring(g, px, py, r);
                    Gfx.ring(g, px, py, Math.max(0, r - 1));
                    if (r > 8) Gfx.ring(g, px, py, r - 2);
                } else {
                    g.setColor(0xFF1744);
                    Gfx.disc(g, px, py, Math.max(1, r / 3));
                    g.setColor(0xFFCDD2);
                    g.fillRect(px - 1, py - r / 3, 2, r * 2 / 3);
                }
            }
        }
        // ship
        int w = Math.max(10, W / 8);
        g.setColor(0xECEFF1);
        g.fillTriangle(cx, cy - w / 3, cx - w / 2, cy + w / 4 + tilt * 3, cx + w / 2, cy + w / 4 - tilt * 3);
        g.setColor(0xFF6E40);
        if ((clock & 1) == 0) g.fillRect(cx - 2, cy + w / 4, 4, 3);
        g.setFont(Gfx.SMALL_B);
        g.setColor(timeLeft < 100 && (clock & 4) == 0 ? 0xFF5252 : 0xFFFFFF);
        g.drawString(Gfx.time(timeLeft, tickMs), 2, 1, Gfx.TL);
        g.setColor(0x00E5FF);
        g.drawString(rings + " rings", W - 2, 1, Gfx.TR);
        if (msgT > 0) Gfx.shadowText(g, msg, cx, H / 4, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int cx = x + w / 2, cy = y + h / 2;
        for (int k = 0; k < 4; k++) {
            int z = ((k * 100 - clock * 8) % 400 + 400) % 400 + 30;
            int r = 20 * h / z;
            g.setColor(0x00E5FF);
            Gfx.ring(g, cx, cy, Math.min(r, h));
        }
    }
}
