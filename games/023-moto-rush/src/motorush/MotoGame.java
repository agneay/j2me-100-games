package motorush;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Moto Rush: a pseudo-3D road racer. Curves and hills come from a segment
 * list projected with integer maths; beat the clock between checkpoints.
 */
public class MotoGame extends Game {
    private static final int SEG = 200, NSEG = 600, DRAW = 50, ROAD = 1800, CAM_H = 900, CHECK_EVERY = 120;
    private static final int MAXCAR = 8;

    private final byte[] curve = new byte[NSEG];
    private final short[] hill = new short[NSEG];
    private final byte[] deco = new byte[NSEG];
    private int pos;          // world z of camera
    private int speed;        // world units per tick
    private int maxSpeed;
    private int playerX;      // -1000..1000 = road edges
    private int timeLeft, dist, nextCheck, steer, crashT, checkMsg;
    private final int[] carZ = new int[MAXCAR], carX = new int[MAXCAR], carSpd = new int[MAXCAR], carCol = new int[MAXCAR];
    // per-frame projection cache
    private final int[] pX = new int[DRAW + 1], pY = new int[DRAW + 1], pW = new int[DRAW + 1], pClip = new int[DRAW + 1], pZ = new int[DRAW + 1];
    private Image[] bike;
    private int bikeScale;
    private static final int[] CAR_COLORS = { 0xE53935, 0x1E88E5, 0xFDD835, 0x8E24AA, 0x00897B, 0xF4511E };

    protected String name() { return "Moto Rush"; }

    protected String[] help() {
        return new String[] {
            "Race your motorbike down a winding, rolling highway. Reach each checkpoint before the timer runs out to earn more time.",
            "Curves push you towards the outside, so lean into them. Riding on the grass slows you down; hitting a car sends you sliding.",
            "- Controls",
            "2 or 5: throttle  8: brake",
            "4/6: steer",
        };
    }

    protected String[] modes() { return new String[] { "Coast road", "Mountain pass" }; }

    protected int accent() { return 0xE63946; }

    private void buildTrack() {
        Rnd.seed(77 + mode * 1000);
        int i = 0;
        while (i < NSEG) {
            int len = Rnd.range(20, 60);
            int c = Rnd.chance(35) ? 0 : Rnd.range(-3 - mode, 3 + mode);
            for (int k = 0; k < len && i < NSEG; k++, i++) {
                // ease in and out of curves
                int ease = k < 8 ? k : (len - k < 8 ? len - k : 8);
                curve[i] = (byte) (c * ease / 8);
            }
        }
        int amp = mode == 0 ? 600 : 1500;
        for (i = 0; i < NSEG; i++) {
            hill[i] = (short) ((FMath.sin((i * 256 / 75) & 255) * amp >> 10) + (FMath.sin((i * 256 / 31) & 255) * amp / 3 >> 10));
            deco[i] = (byte) (i % 7 == 0 ? (Rnd.chance(50) ? 1 : 2) : (i % 11 == 0 ? 3 : 0));
        }
        Rnd.seed(System.currentTimeMillis());
    }

    protected void newGame() {
        buildTrack();
        pos = 0;
        speed = 0;
        maxSpeed = SEG * 7 / 10;
        playerX = 0;
        timeLeft = 40 * 20;
        dist = 0;
        nextCheck = CHECK_EVERY;
        crashT = 0;
        for (int k = 0; k < MAXCAR; k++) {
            carZ[k] = (20 + k * 60 + Rnd.nextInt(30)) * SEG;
            carX[k] = Rnd.range(-700, 700);
            carSpd[k] = SEG * Rnd.range(25, 45) / 100;
            carCol[k] = Rnd.nextInt(CAR_COLORS.length);
        }
    }

    private int segAt(int z) {
        int s = (z / SEG) % NSEG;
        return s < 0 ? s + NSEG : s;
    }

    protected void update() {
        boolean gas = (held & (K_UP | K_FIRE)) != 0, brake = (held & K_DOWN) != 0;
        if (crashT > 0) {
            crashT--;
            speed = speed * 9 / 10;
        } else if (gas) speed += maxSpeed / 40;
        else if (brake) speed -= maxSpeed / 15;
        else speed -= maxSpeed / 200;
        boolean offroad = playerX < -1000 || playerX > 1000;
        int cap = offroad ? maxSpeed / 3 : maxSpeed;
        if (speed > cap) speed -= maxSpeed / 25;
        if (speed < 0) speed = 0;
        steer = 0;
        if ((held & K_LEFT) != 0) steer = -1;
        if ((held & K_RIGHT) != 0) steer = 1;
        int ratio = speed * 1000 / maxSpeed; // 0..1000
        playerX += steer * (25 + ratio / 25);
        int seg = segAt(pos + CAM_H);
        playerX -= curve[seg] * ratio / 70; // centrifugal drift
        playerX = FMath.clamp(playerX, -2200, 2200);
        pos += speed;
        dist += speed;
        if (pos >= NSEG * SEG) pos -= NSEG * SEG;
        // traffic
        for (int k = 0; k < MAXCAR; k++) {
            carZ[k] += carSpd[k];
            int rel = carZ[k] - pos;
            if (rel < -SEG * 3) {
                carZ[k] += NSEG * SEG / 2 + Rnd.nextInt(SEG * 40);
                carX[k] = Rnd.range(-700, 700);
            }
            rel = carZ[k] - pos;
            if (rel < 0) rel += NSEG * SEG;
            if (crashT == 0 && rel > CAM_H - SEG / 2 && rel < CAM_H + SEG / 2 && Math.abs(carX[k] - playerX) < 330 && speed > carSpd[k]) {
                crashT = 25;
                speed = carSpd[k] / 2;
                playerX += playerX < carX[k] ? -300 : 300;
                Sfx.bad();
            }
        }
        if (dist / SEG >= nextCheck) {
            nextCheck += CHECK_EVERY;
            timeLeft += (22 - mode * 3) * 20;
            score += 500;
            checkMsg = 40;
            Sfx.good();
        }
        if (checkMsg > 0) checkMsg--;
        score = Math.max(score, dist / SEG * 10 + (nextCheck / CHECK_EVERY - 1) * 500);
        if (--timeLeft <= 0) {
            headline = "TIME UP";
            endGame(false);
        }
        if ((frame & 3) == 0 && speed > 0) Sfx.tone(30 + ratio / 40, 30);
    }

    private void buildBike(int sc) {
        bikeScale = sc;
        String[][] art = {
            { "....11....", "...1221...", "...1331...", "..144441..", ".14444441.", ".15444451.", "..155551..", "...1661...", "...1661...", "....11...." },
            { "...11.....", "..1221....", "..1331....", ".144441...", "144444411.", "15444451..", ".155551...", "..1661....", "...1661...", "....11...." },
        };
        int[] pal = { 0, 0x1A1A1A, 0xE53935, 0x90CAF9, 0x37474F, 0x263238, 0x616161 };
        bike = new Image[2];
        for (int i = 0; i < 2; i++) bike[i] = Gfx.sprite(art[i], pal, sc);
    }

    protected void draw(Graphics g) {
        int horizon = H * 45 / 100;
        // sky and distant hills
        g.setColor(mode == 0 ? 0x64B5F6 : 0xFF8A65);
        g.fillRect(0, 0, W, horizon);
        g.setColor(mode == 0 ? 0x90CAF9 : 0xFFAB91);
        g.fillRect(0, horizon * 2 / 3, W, horizon / 3);
        int seg0 = segAt(pos);
        int skyShift = (pos / SEG) % W;
        g.setColor(mode == 0 ? 0x2E7D32 : 0x6D4C41);
        for (int x = 0; x < W; x += 4) {
            int hh = 6 + (Math.abs(FMath.sin(((x + skyShift) * 3) & 255)) * (horizon / 5) >> 10);
            g.fillRect(x, horizon - hh, 4, hh);
        }
        g.setColor(0x388E3C);
        g.fillRect(0, horizon, W, H - horizon);
        // project segments near -> far
        int camY = CAM_H + hill[segAt(pos + CAM_H)];
        int camX = playerX * ROAD / 1000;
        int xoff = 0, dx = 0;
        int partial = pos % SEG;
        int maxY = H;
        for (int n = 0; n <= DRAW; n++) {
            int s = (seg0 + n) % NSEG;
            int z = n * SEG - partial;
            if (z < 1) z = 1;
            int wy = hill[s];
            int sx = W / 2 + (int) ((long) (xoff - camX) * W * 45 / (100L * z));
            int sy = horizon + (int) ((long) (camY - wy) * W * 45 / (100L * z));
            int sw = (int) ((long) ROAD * W * 45 / (100L * z));
            pX[n] = sx;
            pY[n] = sy;
            pW[n] = sw;
            pZ[n] = z;
            pClip[n] = maxY;
            xoff += dx;
            dx += curve[s] * 3;
            if (n == 0) continue;
            int y1 = pY[n - 1], y2 = sy;
            if (y2 >= maxY || y1 <= y2) continue;
            boolean dark = ((s / 3) & 1) == 0;
            int top = Math.max(y2, 0), bot = Math.min(y1, maxY);
            g.setColor(dark ? 0x2E7D32 : 0x43A047);
            g.fillRect(0, top, W, bot - top);
            quad(g, pX[n - 1], y1, pW[n - 1] * 115 / 100, sx, y2, sw * 115 / 100, dark ? 0xE53935 : 0xFAFAFA, maxY);
            quad(g, pX[n - 1], y1, pW[n - 1], sx, y2, sw, dark ? 0x616161 : 0x6B6B6B, maxY);
            if (dark) quad(g, pX[n - 1], y1, pW[n - 1] / 30 + 1, sx, y2, sw / 30 + 1, 0xFFFFFF, maxY);
            maxY = y2;
        }
        // roadside decoration and traffic, far -> near
        for (int n = DRAW; n >= 1; n--) {
            int s = (seg0 + n) % NSEG;
            int sc = pW[n];
            if (sc < 2 || pY[n] > H) continue;
            g.setClip(0, 0, W, pClip[n]);
            if (deco[s] != 0) {
                for (int side = -1; side <= 1; side += 2) {
                    int x = pX[n] + side * sc * 14 / 10;
                    if (deco[s] == 3) {
                        g.setColor(0xFFFFFF);
                        g.fillRect(x - sc / 12, pY[n] - sc / 3, Math.max(1, sc / 6), sc / 3);
                    } else {
                        int th = sc * (deco[s] == 1 ? 9 : 6) / 10;
                        g.setColor(0x5D4037);
                        g.fillRect(x - Math.max(1, sc / 40), pY[n] - th / 2, Math.max(2, sc / 20), th / 2);
                        g.setColor(deco[s] == 1 ? 0x1B5E20 : 0x33691E);
                        g.fillTriangle(x, pY[n] - th, x - sc / 5, pY[n] - th / 3, x + sc / 5, pY[n] - th / 3);
                    }
                }
            }
            for (int k = 0; k < MAXCAR; k++) {
                int rel = carZ[k] - pos;
                if (rel < 0) rel += NSEG * SEG;
                if (rel / SEG != n) continue;
                int cw = sc * 45 / 100, ch = cw * 2 / 3;
                int cx = pX[n] + carX[k] * sc / 1000;
                g.setColor(CAR_COLORS[carCol[k]]);
                g.fillRect(cx - cw / 2, pY[n] - ch, cw, ch);
                g.setColor(0x212121);
                g.fillRect(cx - cw / 2, pY[n] - ch / 3, cw / 4, ch / 3);
                g.fillRect(cx + cw / 4, pY[n] - ch / 3, cw / 4, ch / 3);
                g.setColor(0xFFCDD2);
                g.fillRect(cx - cw / 2 + 1, pY[n] - ch * 2 / 3, Math.max(1, cw / 6), Math.max(1, ch / 6));
                g.fillRect(cx + cw / 2 - 1 - Math.max(1, cw / 6), pY[n] - ch * 2 / 3, Math.max(1, cw / 6), Math.max(1, ch / 6));
            }
        }
        g.setClip(0, 0, W, H);
        // bike
        int sc = Math.max(1, W / 55);
        if (bike == null || bikeScale != sc) buildBike(sc);
        int bob = (crashT > 0 && (clock & 2) != 0) ? 2 : ((frame / 2) & 1);
        int bx = W / 2, by = H - 4 - bob;
        if (steer == 0) g.drawImage(bike[0], bx, by, Graphics.HCENTER | Graphics.BOTTOM);
        else g.drawRegion(bike[1], 0, 0, bike[1].getWidth(), bike[1].getHeight(), steer < 0 ? 0 : 2, bx, by, Graphics.HCENTER | Graphics.BOTTOM);
        // HUD
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x000000);
        g.fillRect(0, 0, W, Gfx.SMALL.getHeight() + 2);
        g.setColor(timeLeft < 200 && (clock & 4) == 0 ? 0xFF5252 : 0xFFEB3B);
        g.drawString("TIME " + (timeLeft / 20), 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString((speed * 300 / maxSpeed) + " km/h", W - 2, 1, Gfx.TR);
        g.setFont(Gfx.SMALL);
        g.setColor(0xFFFFFF);
        g.drawString(String.valueOf(score), W / 2, Gfx.SMALL.getHeight() + 3, Gfx.TC);
        if (checkMsg > 0) Gfx.shadowText(g, "CHECKPOINT!", W / 2, H / 4, Gfx.TC, Gfx.fit("CHECKPOINT!", W - 8), 0xFFEB3B, 0x000000);
    }

    /** Fill the trapezoid between two projected road slices, clipped below maxY. */
    private void quad(Graphics g, int x1, int y1, int w1, int x2, int y2, int w2, int color, int maxY) {
        g.setColor(color);
        g.setClip(0, 0, W, maxY);
        g.fillTriangle(x1 - w1, y1, x1 + w1, y1, x2 + w2, y2);
        g.fillTriangle(x1 - w1, y1, x2 + w2, y2, x2 - w2, y2);
        g.setClip(0, 0, W, H);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x43A047);
        g.fillRect(x, y + h / 3, w, h * 2 / 3);
        g.setColor(0x64B5F6);
        g.fillRect(x, y, w, h / 3);
        int cx = x + w / 2 + (FMath.sin(clock * 3) * (w / 6) >> 10);
        g.setColor(0x616161);
        g.fillTriangle(cx - 2, y + h / 3, cx + 2, y + h / 3, x + w * 4 / 5, y + h);
        g.fillTriangle(cx - 2, y + h / 3, x + w * 4 / 5, y + h, x + w / 5, y + h);
        g.setColor(0xFFFFFF);
        int dash = (clock * 2) % 8;
        for (int k = 0; k < 4; k++) {
            int t = k * 8 + dash;
            int yy = y + h / 3 + (h * 2 / 3) * t * t / 1024;
            if (yy < y + h) g.fillRect(cx + (x + w / 2 - cx) * t / 32 - 1, yy, 2, 1 + t / 8);
        }
        int sc = Math.max(1, w / 60);
        if (bike == null || bikeScale != sc) buildBike(sc);
        g.drawImage(bike[0], x + w / 2, y + h, Graphics.HCENTER | Graphics.BOTTOM);
    }
}
