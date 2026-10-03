package orbitjump;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Orbit Jump: your little ship circles a planet on its own. Press 5 to
 * break orbit and fly off along the tangent - get caught by the next planet.
 */
public class OrbitGame extends Game {
    private static final int MAXP = 12;
    private static final int[] PCOL = { 0xFF7043, 0x26C6DA, 0x9CCC65, 0xAB47BC, 0xFFCA28, 0xEC407A };

    private final int[] plx = new int[MAXP], ply = new int[MAXP], plr = new int[MAXP], plc = new int[MAXP], pdir = new int[MAXP], pmove = new int[MAXP];
    private final boolean[] star = new boolean[MAXP], visited = new boolean[MAXP];
    private int cur, ang, orbitR, sx, sy, vx, vy, camY, topY, lives, reached, flyT, trailN;
    private boolean flying;
    private final int[] trailX = new int[16], trailY = new int[16];

    protected String name() { return "Orbit Jump"; }

    protected String[] help() {
        return new String[] {
            "Your ship orbits a planet automatically. Press 5 to break orbit: it flies off in a straight line along its path.",
            "Fly close to another planet and its gravity captures you into a new orbit. Keep hopping upward, collecting the stars beside each planet.",
            "Miss everything and drift off screen and you lose a ship. Later planets drift sideways and spin faster.",
            "- Controls",
            "5: launch",
        };
    }

    protected int accent() { return 0x26C6DA; }

    protected void newGame() {
        for (int i = 0; i < MAXP; i++) plr[i] = 0;
        lives = 3;
        reached = 0;
        topY = H * 3 / 4;
        spawnPlanet(0, W / 2, topY, Math.max(10, W / 10));
        for (int i = 1; i < MAXP; i++) spawnNext(i);
        cur = 0;
        visited[0] = true;
        orbitR = plr[0] + Math.max(8, W / 16);
        ang = 0;
        flying = false;
        camY = 0;
        trailN = 0;
    }

    private void spawnPlanet(int i, int x, int y, int r) {
        plx[i] = x << 8;
        ply[i] = y;
        plr[i] = r;
        plc[i] = PCOL[Rnd.nextInt(PCOL.length)];
        pdir[i] = Rnd.chance(50) ? 1 : -1;
        pmove[i] = reached > 6 && Rnd.chance(40) ? (Rnd.chance(50) ? 1 : -1) * (40 + reached * 3) : 0;
        star[i] = true;
        visited[i] = false;
    }

    private void spawnNext(int i) {
        int gap = Math.max(H / 4, H / 3 - reached) + Rnd.nextInt(H / 8);
        topY -= gap;
        int r = Math.max(8, W / 14 + Rnd.nextInt(W / 14));
        spawnPlanet(i, Rnd.range(r + 10, W - r - 10), topY, r);
    }

    protected void update() {
        // drifting planets
        for (int i = 0; i < MAXP; i++) {
            if (pmove[i] == 0) continue;
            plx[i] += pmove[i];
            if ((plx[i] >> 8) < plr[i] + 6 || (plx[i] >> 8) > W - plr[i] - 6) pmove[i] = -pmove[i];
        }
        if (!flying) {
            int spd = 4 + Math.min(6, reached / 4);
            ang = (ang + pdir[cur] * spd) & 255;
            sx = plx[cur] + FMath.cos(ang) * orbitR / 4;
            sy = (ply[cur] << 8) + FMath.sin(ang) * orbitR / 4;
            if ((pressed & K_FIRE) != 0) {
                flying = true;
                flyT = 0;
                int sp = (H << 8) / 40;
                int ta = (ang + pdir[cur] * 64) & 255; // tangent
                vx = FMath.cos(ta) * sp >> 10;
                vy = FMath.sin(ta) * sp >> 10;
                Sfx.tone(72, 30);
            }
        } else {
            flyT++;
            sx += vx;
            sy += vy;
            if (flyT % 2 == 0) {
                trailX[trailN % 16] = sx >> 8;
                trailY[trailN % 16] = sy >> 8;
                trailN++;
            }
            for (int i = 0; i < MAXP; i++) {
                if (plr[i] == 0 || (i == cur && flyT < 10)) continue;
                int dx = (sx >> 8) - (plx[i] >> 8), dy = (sy >> 8) - ply[i];
                int capture = plr[i] + Math.max(10, W / 12);
                if (dx * dx + dy * dy <= capture * capture) {
                    landOn(i, dx, dy);
                    break;
                }
                // stars sit just outside each planet
                if (star[i]) {
                    int stx = (plx[i] >> 8) + plr[i] + 10, sty = ply[i] - plr[i] - 6;
                    if (Math.abs((sx >> 8) - stx) < 7 && Math.abs((sy >> 8) - sty) < 7) {
                        star[i] = false;
                        score += 25;
                        Sfx.tone(90, 25);
                    }
                }
            }
            int screenY = (sy >> 8) - camY;
            if ((sx >> 8) < -10 || (sx >> 8) > W + 10 || screenY > H + 10 || screenY < -H) lost();
        }
        // camera: keep current planet in the lower part of the screen
        int target = ply[cur] - H * 2 / 3;
        camY += (target - camY) / 6;
    }

    private void landOn(int i, int dx, int dy) {
        flying = false;
        trailN = 0;
        cur = i;
        orbitR = plr[i] + Math.max(8, W / 16);
        ang = FMath.atan2(dy, dx);
        if (!visited[i]) {
            visited[i] = true;
            reached++;
            score += 10 + reached;
            Sfx.good();
            recycle();
        } else {
            Sfx.click();
        }
    }

    private void recycle() {
        // replace planets far below the camera with new ones above
        for (int i = 0; i < MAXP; i++) {
            if (i == cur) continue;
            if (ply[i] - camY > H * 2) spawnNext(i);
        }
    }

    private void lost() {
        lives--;
        Sfx.bad();
        flying = false;
        trailN = 0;
        if (lives <= 0) {
            headline = reached + " PLANETS";
            endGame(false);
        }
    }

    protected void draw(Graphics g) {
        g.setColor(0x050816);
        g.fillRect(0, 0, W, H);
        g.setColor(0x2A2F55);
        for (int k = 0; k < 40; k++) {
            int y = ((k * 97 - camY / 3) % H + H) % H;
            g.fillRect((k * 53) % W, y, 1, 1);
        }
        for (int i = 0; i < MAXP; i++) {
            if (plr[i] == 0) continue;
            int x = plx[i] >> 8, y = ply[i] - camY, r = plr[i];
            if (y < -r * 3 || y > H + r * 3) continue;
            g.setColor(0x1A2040);
            Gfx.ring(g, x, y, r + Math.max(10, W / 12));
            g.setColor(plc[i]);
            Gfx.disc(g, x, y, r);
            g.setColor(Gfx.shade(plc[i], 35));
            Gfx.disc(g, x - r / 3, y - r / 3, r / 3);
            if (visited[i]) {
                g.setColor(0xFFFFFF);
                g.fillRect(x - 1, y - r - 4, 2, 4);
            }
            if (star[i]) {
                int stx = x + r + 10, sty = y - r - 6;
                g.setColor((clock & 4) == 0 ? 0xFFEB3B : 0xFFF59D);
                g.fillTriangle(stx, sty - 4, stx - 4, sty + 3, stx + 4, sty + 3);
                g.fillTriangle(stx, sty + 5, stx - 4, sty - 1, stx + 4, sty - 1);
            }
        }
        g.setColor(0x4DD0E1);
        int n = Math.min(trailN, 16);
        for (int k = 0; k < n; k++) g.fillRect(trailX[k], trailY[k] - camY, 1, 1);
        int x = sx >> 8, y = (sy >> 8) - camY;
        g.setColor(0xFFFFFF);
        Gfx.disc(g, x, y, 3);
        g.setColor(0x26C6DA);
        g.fillRect(x - 1, y - 1, 2, 2);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(String.valueOf(score), 2, 1, Gfx.TL);
        g.setColor(0x26C6DA);
        g.drawString("x" + lives, W - 2, 1, Gfx.TR);
        if (!flying && reached == 0 && frame < 120) Gfx.hint(g, "5: launch!", W, H);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int r = Math.max(6, h / 6);
        g.setColor(0xFF7043);
        Gfx.disc(g, x + w / 3, y + h * 2 / 3, r);
        g.setColor(0x26C6DA);
        Gfx.disc(g, x + w * 2 / 3, y + h / 3, r);
        int a = (clock * 6) & 255;
        int or = r + 8;
        g.setColor(0xFFFFFF);
        Gfx.disc(g, x + w / 3 + (FMath.cos(a) * or >> 10), y + h * 2 / 3 + (FMath.sin(a) * or >> 10), 2);
    }
}
