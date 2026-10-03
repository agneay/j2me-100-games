package snowslalom;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Snow Slalom: carve down the mountain through flag gates. Pointing straight
 * down is fastest; each missed gate adds a five-second penalty.
 */
public class SlalomGame extends Game {
    private static final int GATES = 25, MAXT = 30;
    private static final int[] DIRX = { -3, -2, 0, 2, 3 };   // lateral speed per heading
    private static final int[] DIRY = { 2, 3, 4, 3, 2 };     // downhill speed per heading (relative)

    private final int[] gx = new int[GATES], gy = new int[GATES], gw = new int[GATES];
    private final boolean[] gPassed = new boolean[GATES], gMissed = new boolean[GATES];
    private final int[] tx = new int[MAXT], ty = new int[MAXT];
    private int heading = 2, sx, worldY, speed, nextGate, penalty, crashT, missed, finishY;
    private int trailN;
    private final int[] trX = new int[24], trY = new int[24];

    protected String name() { return "Snow Slalom"; }

    protected String[] help() {
        return new String[] {
            "Ski down through 25 gates. Pass between each pair of flags; every missed gate adds 5 seconds.",
            "Your skis have five angles. Straight down is fastest, wide carves scrub off speed. Trees and rocks send you tumbling.",
            "Freeride: no gates, just dodge the forest for as long as you can.",
            "- Controls",
            "4/6: turn skis",
            "8: tuck (faster)  2: snowplough (slower)",
        };
    }

    protected String[] modes() { return new String[] { "Giant slalom", "Slalom", "Freeride" }; }

    protected boolean lowerIsBetter() { return mode < 2; }

    protected String formatScore(int s) { return mode < 2 ? Gfx.time(s, tickMs) : s + " m"; }

    protected int accent() { return 0x4FC3F7; }

    protected void newGame() {
        sx = (W / 2) << 8;
        worldY = 0;
        speed = 0;
        heading = 2;
        nextGate = 0;
        penalty = 0;
        missed = 0;
        crashT = 0;
        trailN = 0;
        int y = H;
        int width = mode == 0 ? W / 3 : W / 4;
        int x = W / 2;
        for (int i = 0; i < GATES; i++) {
            y += H * 2 / 5 + Rnd.nextInt(H / 5);
            x = Math.max(width / 2 + 10, Math.min(W - width / 2 - 10, x + Rnd.range(-W / 3, W / 3)));
            gx[i] = x;
            gy[i] = y;
            gw[i] = width;
            gPassed[i] = gMissed[i] = false;
        }
        finishY = y + H / 3;
        for (int i = 0; i < MAXT; i++) placeTree(i, H + Rnd.nextInt(finishY));
    }

    private void placeTree(int i, int y) {
        for (int tries = 0; tries < 20; tries++) {
            int x = Rnd.nextInt(W);
            boolean clear = true;
            if (mode < 2) {
                for (int k = 0; k < GATES; k++) {
                    if (Math.abs(gy[k] - y) < H / 6 && Math.abs(gx[k] - x) < gw[k] / 2 + 12) clear = false;
                }
            }
            if (clear) {
                tx[i] = x;
                ty[i] = y;
                return;
            }
        }
        tx[i] = -100;
        ty[i] = y;
    }

    protected void update() {
        if ((pressed & K_LEFT) != 0 && heading > 0) heading--;
        if ((pressed & K_RIGHT) != 0 && heading < 4) heading++;
        int base = (H << 8) / 220;
        int target = base * DIRY[heading] / 2;
        if ((held & K_DOWN) != 0) target = target * 13 / 10;
        if ((held & K_UP) != 0) target = target / 2;
        if (crashT > 0) {
            crashT--;
            target = 0;
        }
        speed += (target - speed) / 8;
        worldY += speed >> 8;
        sx += DIRX[heading] * speed / 6;
        if ((sx >> 8) < 4) sx = 4 << 8;
        if ((sx >> 8) > W - 4) sx = (W - 4) << 8;
        int px = sx >> 8, pyWorld = worldY + H / 3;
        if ((frame & 1) == 0) {
            trX[trailN % 24] = px;
            trY[trailN % 24] = pyWorld;
            trailN++;
        }
        // gates
        if (mode < 2) {
            while (nextGate < GATES && gy[nextGate] <= pyWorld) {
                int i = nextGate;
                if (Math.abs(px - gx[i]) <= gw[i] / 2) {
                    gPassed[i] = true;
                    Sfx.tone(84, 20);
                } else {
                    gMissed[i] = true;
                    missed++;
                    penalty += 5 * 20;
                    Sfx.bad();
                }
                nextGate++;
            }
            if (pyWorld >= finishY) {
                score = frame + penalty;
                headline = "FINISH " + Gfx.time(score, tickMs);
                endGame(true);
                return;
            }
        } else {
            score = pyWorld / 10;
            for (int i = 0; i < MAXT; i++) if (ty[i] < worldY - 20) placeTree(i, worldY + H + Rnd.nextInt(H));
        }
        // trees
        if (crashT == 0) {
            for (int i = 0; i < MAXT; i++) {
                if (Math.abs(tx[i] - px) < 5 && Math.abs(ty[i] - pyWorld) < 6) {
                    crashT = 25;
                    speed = 0;
                    Sfx.bad();
                    if (mode == 2) {
                        headline = "WIPEOUT!";
                        endGame(false);
                        return;
                    }
                }
            }
        }
        if ((frame & 7) == 0 && speed > 0) Sfx.tone(30 + (speed >> 7), 20);
    }

    protected void draw(Graphics g) {
        g.setColor(0xF4F8FC);
        g.fillRect(0, 0, W, H);
        g.setColor(0xDCE8F2);
        for (int k = 0; k < 20; k++) {
            int y = ((k * 53 - worldY) % H + H) % H;
            g.fillRect((k * 37) % W, y, 6, 1);
        }
        // ski tracks
        g.setColor(0xB0C4D8);
        int n = Math.min(trailN, 24);
        for (int k = 0; k < n; k++) {
            int y = trY[k] - worldY;
            g.fillRect(trX[k] - 2, y, 1, 2);
            g.fillRect(trX[k] + 1, y, 1, 2);
        }
        if (mode < 2) {
            for (int i = 0; i < GATES; i++) {
                int y = gy[i] - worldY;
                if (y < -10 || y > H + 10) continue;
                int c = gMissed[i] ? 0x9E9E9E : (i % 2 == 0 ? 0xE53935 : 0x1E88E5);
                for (int side = -1; side <= 1; side += 2) {
                    int x = gx[i] + side * gw[i] / 2;
                    g.setColor(0x424242);
                    g.fillRect(x, y - 10, 1, 10);
                    g.setColor(c);
                    g.fillTriangle(x + 1, y - 10, x + 1, y - 5, x + 1 + side * -5, y - 8);
                }
                if (gPassed[i]) {
                    g.setColor(0x66BB6A);
                    g.fillRect(gx[i] - 2, y - 2, 4, 2);
                }
            }
            int fy = finishY - worldY;
            if (fy > -10 && fy < H + 10) {
                for (int x = 0; x < W; x += 6) {
                    g.setColor(((x / 6) & 1) == 0 ? 0x000000 : 0xFFFFFF);
                    g.fillRect(x, fy, 6, 3);
                }
            }
        }
        for (int i = 0; i < MAXT; i++) {
            int y = ty[i] - worldY;
            if (y < -14 || y > H + 4) continue;
            int x = tx[i];
            g.setColor(0x2E7D32);
            g.fillTriangle(x, y - 14, x - 6, y - 2, x + 6, y - 2);
            g.setColor(0x1B5E20);
            g.fillTriangle(x, y - 9, x - 7, y + 1, x + 7, y + 1);
            g.setColor(0x5D4037);
            g.fillRect(x - 1, y + 1, 2, 3);
        }
        // skier
        int px = sx >> 8, py = H / 3;
        if (crashT > 0 && (clock & 2) == 0) py += 1;
        g.setColor(0x212121);
        int lean = DIRX[heading];
        g.drawLine(px - 3 + lean, py + 3, px - 3 - lean, py - 4);
        g.drawLine(px + 3 + lean, py + 3, px + 3 - lean, py - 4);
        g.setColor(0xD32F2F);
        g.fillRect(px - 2, py - 7, 5, 6);
        g.setColor(0xFFCC80);
        Gfx.disc(g, px, py - 9, 2);
        g.setColor(0x1565C0);
        g.fillRect(px - 2, py - 12, 5, 2);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x0D47A1);
        if (mode < 2) {
            g.drawString(Gfx.time(frame + penalty, tickMs), 2, 1, Gfx.TL);
            g.setColor(missed > 0 ? 0xD32F2F : 0x2E7D32);
            g.drawString("Gate " + Math.min(nextGate + 1, GATES) + "/" + GATES, W - 2, 1, Gfx.TR);
        } else {
            g.drawString(score + " m", 2, 1, Gfx.TL);
        }
        g.setFont(Gfx.SMALL);
        g.setColor(0x546E7A);
        g.drawString((speed * 4 >> 8) + " km/h", W - 2, Gfx.SMALL.getHeight() + 1, Gfx.TR);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0xF4F8FC);
        g.fillRect(x, y, w, h);
        int t = clock % 60;
        int sxp = x + w / 2 + (gamekit.FMath.sin(clock * 6) * (w / 4) >> 10);
        g.setColor(0xD32F2F);
        g.fillTriangle(x + w / 4, y + 4, x + w / 4, y + 10, x + w / 4 + 6, y + 7);
        g.setColor(0x1E88E5);
        g.fillTriangle(x + w * 3 / 4, y + h / 2, x + w * 3 / 4, y + h / 2 + 6, x + w * 3 / 4 - 6, y + h / 2 + 3);
        g.setColor(0xD32F2F);
        g.fillRect(sxp - 2, y + h / 2 - 3 + t / 20, 5, 6);
        g.setColor(0x212121);
        g.drawLine(sxp - 3, y + h / 2 + 4, sxp + 3, y + h / 2 + 4);
    }
}
