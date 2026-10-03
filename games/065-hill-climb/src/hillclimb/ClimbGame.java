package hillclimb;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Hill Climb: drive a little 4x4 over endless hills. Balance throttle and
 * brake on the ground, tilt in the air, land on your wheels and keep fuelled.
 */
public class ClimbGame extends Game {
    private int seed;
    private int cx, cy, vx, vy, ang, angV; // car centre (<<8), velocity, body angle (0..255)
    private boolean ground, crashed;
    private int fuel, coins, crashT, nextFuel, nextCoin, best;
    private final int[] coinX = new int[12];
    private final boolean[] coinOn = new boolean[12];
    private int fuelX;
    private boolean fuelOn;

    protected String name() { return "Hill Climb"; }

    protected String[] help() {
        return new String[] {
            "Drive as far as you can over endless hills. Gas pushes you forward and tips the car back; braking tips it forward.",
            "On jumps, tilt the car with 4/6 so you land on your wheels - land on your roof and it's over. Grab fuel cans before the tank runs dry, and coins for points.",
            "- Controls",
            "6 or 2: gas   4 or 8: brake / reverse",
            "In the air: 4/6 tilt",
        };
    }

    protected int accent() { return 0xE63946; }

    protected void newGame() {
        seed = Rnd.nextInt(10000);
        cx = 40 << 8;
        cy = terrain(40) << 8;
        vx = vy = 0;
        ang = 0;
        angV = 0;
        ground = true;
        crashed = false;
        crashT = 0;
        fuel = 1000;
        coins = 0;
        nextFuel = 400;
        nextCoin = 150;
        fuelOn = false;
        for (int i = 0; i < coinX.length; i++) coinOn[i] = false;
    }

    /** Ground height (world units, y grows downward) at x. */
    private int terrain(int x) {
        int amp = 20 + Math.min(70, x / 60);
        int a = FMath.sin(((x + seed) / 3) & 255) * amp >> 10;
        int b = FMath.sin(((x * 3 + seed * 7) / 7) & 255) * (amp / 2) >> 10;
        int c = FMath.sin(((x + seed * 3) / 11) & 255) * (amp * 2) >> 10;
        return 200 + a + b + c - x / 40;
    }

    private int slopeAngle(int x) {
        int dx = 10;
        return FMath.atan2(terrain(x + dx) - terrain(x - dx), dx * 2);
    }

    protected void update() {
        if (crashT > 0) {
            if (--crashT == 0) endGame(false);
            return;
        }
        boolean gas = (held & (K_RIGHT | K_UP)) != 0 && fuel > 0, brake = (held & (K_LEFT | K_DOWN)) != 0;
        int x = cx >> 8;
        if (ground) {
            int s = slopeAngle(x);
            // gravity along the slope, engine and rolling friction
            vx += FMath.sin(s) * 22 >> 10;
            if (gas) {
                vx += 18;
                fuel--;
            }
            if (brake) vx -= vx > 0 ? 20 : 10;
            vx = vx * 252 / 256;
            if (vx < -400) vx = -400;
            if (vx > 1100) vx = 1100;
            int nx = cx + vx;
            int gy = terrain(nx >> 8) << 8;
            // take off when the ground falls away faster than gravity can follow
            int expectVy = gy - cy;
            if (expectVy > 360 && vx > 300) {
                ground = false;
                vy = (cy - (terrain((cx - vx) >> 8) << 8)) / 2;
                if (vy > 0) vy = 0;
            } else {
                cx = nx;
                cy = gy;
                vy = 0;
                int target = slopeAngle(cx >> 8);
                int diff = ((target - ang + 128) & 255) - 128;
                ang = (ang + diff / 2) & 255;
                if (gas) ang = (ang - 1) & 255;
            }
        } else {
            vy += 40;
            cx += vx;
            cy += vy;
            if ((held & K_LEFT) != 0) angV -= 1;
            if ((held & K_RIGHT) != 0) angV += 1;
            angV = FMath.clamp(angV, -6, 6);
            ang = (ang + angV) & 255;
            int gy = terrain(cx >> 8) << 8;
            if (cy >= gy) {
                cy = gy;
                int s = slopeAngle(cx >> 8);
                int diff = Math.abs(((s - ang + 128) & 255) - 128);
                if (diff > 52) {
                    crashT = 30;
                    headline = "FLIPPED!";
                    Sfx.bad();
                    return;
                }
                ground = true;
                angV = 0;
                Sfx.tone(40, 30);
                if (diff < 12) score += 20; // clean landing bonus
            }
        }
        int dist = (cx >> 8) / 10;
        if (dist > best) best = dist;
        score = Math.max(score, best + coins * 10);
        // pickups
        int px = cx >> 8;
        if (!fuelOn && px + 260 > nextFuel) {
            fuelOn = true;
            fuelX = nextFuel;
            nextFuel += 500 + (px / 30);
        }
        if (fuelOn && Math.abs(fuelX - px) < 12) {
            fuelOn = false;
            fuel = 1000;
            Sfx.good();
        }
        if (fuelOn && fuelX < px - 200) fuelOn = false;
        if (px + 260 > nextCoin) {
            for (int i = 0; i < coinX.length; i++) {
                if (!coinOn[i]) {
                    coinOn[i] = true;
                    coinX[i] = nextCoin;
                    break;
                }
            }
            nextCoin += 40 + Rnd.nextInt(60);
        }
        for (int i = 0; i < coinX.length; i++) {
            if (!coinOn[i]) continue;
            if (Math.abs(coinX[i] - px) < 9 && Math.abs((terrain(coinX[i]) - 14) - (cy >> 8)) < 22) {
                coinOn[i] = false;
                coins++;
                Sfx.tone(88, 20);
            } else if (coinX[i] < px - 200) coinOn[i] = false;
        }
        if (fuel <= 0 && ground && Math.abs(vx) < 20) {
            headline = "OUT OF FUEL";
            endGame(false);
        }
        if (gas && (frame & 3) == 0) Sfx.tone(30 + Math.abs(vx) / 60, 25);
    }

    protected void draw(Graphics g) {
        int camX = (cx >> 8) - W / 3, camY = (cy >> 8) - H * 55 / 100;
        for (int i = 0; i < 6; i++) {
            g.setColor(Gfx.mix(0x4FC3F7, 0xE1F5FE, i * 45));
            g.fillRect(0, i * H / 6, W, H / 6 + 1);
        }
        g.setColor(0xB0BEC5);
        for (int x = 0; x < W; x += 4) {
            int h = (FMath.sin(((x + camX / 4) * 2) & 255) * 18 >> 10) + 30;
            g.fillRect(x, H / 2 - h + 20, 4, h + H);
        }
        for (int x = 0; x < W; x += 2) {
            int wy = terrain(camX + x) - camY;
            g.setColor(0x7CB342);
            g.fillRect(x, wy, 2, 4);
            g.setColor(0x8D6E63);
            g.fillRect(x, wy + 4, 2, H);
        }
        for (int i = 0; i < coinX.length; i++) {
            if (!coinOn[i]) continue;
            int x = coinX[i] - camX, y = terrain(coinX[i]) - 14 - camY;
            g.setColor(0xFFD54F);
            Gfx.disc(g, x, y, 3);
        }
        if (fuelOn) {
            int x = fuelX - camX, y = terrain(fuelX) - camY;
            g.setColor(0xE53935);
            g.fillRect(x - 4, y - 12, 8, 11);
            g.setColor(0xFFFFFF);
            g.drawString("F", x, y - 12, Gfx.TC);
        }
        drawCar(g, (cx >> 8) - camX, (cy >> 8) - camY);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x0D47A1);
        g.drawString(best + " m", 2, 1, Gfx.TL);
        g.setColor(0xF57F17);
        g.drawString("$" + coins, W - 2, 1, Gfx.TR);
        Gfx.bar(g, W / 2 - W / 6, 3, W / 3, 6, fuel, 1000, fuel < 200 ? 0xFF5252 : 0x66BB6A, 0x37474F);
    }

    /** Fill a rectangle of half-size hw x hh centred on (x, y), rotated by ang. */
    private void rotRect(Graphics g, int x, int y, int hw, int hh) {
        int c = FMath.cos(ang), s = FMath.sin(ang);
        int ax = c * hw >> 10, ay = s * hw >> 10;   // along the car
        int bx = -s * hh >> 10, by = c * hh >> 10;  // across the car (towards the ground)
        int x1 = x - ax - bx, y1 = y - ay - by, x2 = x + ax - bx, y2 = y + ay - by;
        int x3 = x + ax + bx, y3 = y + ay + by, x4 = x - ax + bx, y4 = y - ay + by;
        g.fillTriangle(x1, y1, x2, y2, x3, y3);
        g.fillTriangle(x1, y1, x3, y3, x4, y4);
    }

    private void drawCar(Graphics g, int x, int y) {
        int c = FMath.cos(ang), s = FMath.sin(ang);
        int L = Math.max(8, W / 18), wr = Math.max(3, L / 3);
        // "up" relative to the car body
        int upx = s, upy = -c;
        int bodyX = x + (upx * (wr + 2) >> 10), bodyY = y + (upy * (wr + 2) >> 10);
        g.setColor(0xE63946);
        rotRect(g, bodyX, bodyY, L + 2, Math.max(2, wr * 2 / 3));
        int cabX = bodyX + (upx * wr >> 10) - (c * L / 4 >> 10), cabY = bodyY + (upy * wr >> 10) - (s * L / 4 >> 10);
        g.setColor(0xB71C1C);
        rotRect(g, cabX, cabY, L / 2, Math.max(2, wr * 2 / 3));
        g.setColor(0x90CAF9);
        rotRect(g, cabX + (c * 2 >> 10), cabY + (s * 2 >> 10), L / 3, Math.max(1, wr / 3));
        int wx1 = x - (c * L >> 10), wy1 = y - (s * L >> 10);
        int wx2 = x + (c * L >> 10), wy2 = y + (s * L >> 10);
        g.setColor(0x212121);
        Gfx.disc(g, wx1 + (upx * wr >> 10) / 2, wy1 + (upy * wr >> 10) / 2, wr);
        Gfx.disc(g, wx2 + (upx * wr >> 10) / 2, wy2 + (upy * wr >> 10) / 2, wr);
        g.setColor(0x9E9E9E);
        Gfx.disc(g, wx1 + (upx * wr >> 10) / 2, wy1 + (upy * wr >> 10) / 2, Math.max(1, wr / 2));
        Gfx.disc(g, wx2 + (upx * wr >> 10) / 2, wy2 + (upy * wr >> 10) / 2, Math.max(1, wr / 2));
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x7CB342);
        for (int k = 0; k < w; k += 2) {
            int hh = (FMath.sin((k * 2 + clock * 2) & 255) * (h / 4) >> 10) + h / 2;
            g.fillRect(x + k, y + hh, 2, h - hh);
        }
        int carX = w / 3;
        int gy = (FMath.sin((carX * 2 + clock * 2) & 255) * (h / 4) >> 10) + h / 2;
        g.setColor(0xE63946);
        g.fillRect(x + carX - 8, y + gy - 10, 16, 6);
        g.setColor(0x212121);
        Gfx.disc(g, x + carX - 5, y + gy - 3, 3);
        Gfx.disc(g, x + carX + 5, y + gy - 3, 3);
    }
}
