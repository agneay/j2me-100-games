package volleybeach;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Volley Beach: one-on-one beach volleyball seen from the side. Players are
 * half-discs; the ball bounces off them along the line between centres.
 * Positions are 8.8 fixed point in screen pixels.
 */
public class VolleyGame extends Game {
    private static final int TARGET = 7;
    private int ground, netX, netTop, pr, br, gb, gp, jumpV, hitV;
    private int bx, by, bvx, bvy;
    private int ax, ay, avy, cx, cy, cvy; // player (left) and CPU (right)
    private int myPts, cpuPts, serveSide, pause, touches, lastSide, flash;

    protected String name() { return "Volley Beach"; }

    protected String[] help() {
        return new String[] {
            "One-on-one beach volleyball. Keep the ball off the sand on your side of the net and land it on the CPU's side.",
            "The ball bounces off your head and shoulders: where it hits decides where it goes. Jump to spike it over the net.",
            "You may touch the ball at most three times before it must cross. First to 7 points wins.",
            "- Controls",
            "4/6: run",
            "2 or 5: jump",
        };
    }

    protected String[] modes() { return new String[] { "Easy", "Hard" }; }

    protected int accent() { return 0xFFB74D; }

    private void layout() {
        ground = H - Math.max(12, H / 8);
        netX = W / 2;
        netTop = ground - Math.max(24, H / 4);
        pr = Math.max(8, W / 14);
        br = Math.max(4, W / 30);
        gb = H * 3 / 2;
        gp = H * 3;
        int hj = (ground - netTop) * 9 / 10;
        jumpV = -FMath.sqrt(2 * gp * hj * 256);
        hitV = FMath.sqrt(2 * gb * (ground - netTop + pr * 2) * 256);
    }

    protected void newGame() {
        layout();
        myPts = cpuPts = 0;
        serveSide = 0;
        serve();
    }

    private void serve() {
        ax = (W / 4) << 8;
        cx = (W * 3 / 4) << 8;
        ay = cy = ground << 8;
        avy = cvy = 0;
        bx = (serveSide == 0 ? W / 4 : W * 3 / 4) << 8;
        by = (netTop - pr * 2) << 8;
        bvx = bvy = 0;
        pause = 20;
        touches = 0;
        lastSide = serveSide;
    }

    private int gravity() { return gb; }

    private void bump(int px, int py, int side) {
        int dx = (bx - px) >> 8, dy = (by - py) >> 8;
        if (dy > 0) return;
        int min = pr + br;
        int d2 = dx * dx + dy * dy;
        if (d2 >= min * min) return;
        int d = Math.max(1, FMath.sqrt(d2));
        bx = px + ((dx * min / d) << 8);
        by = py + ((dy * min / d) << 8);
        int sp = hitV;
        bvx = dx * sp / d / 2 + (side == 0 ? sp / 3 : -sp / 3);
        bvy = Math.min(-sp * 2 / 3, dy * sp / d);
        if (side != lastSide) touches = 0;
        lastSide = side;
        touches++;
        Sfx.hit();
        if (touches > 3) point(1 - side);
    }

    private void point(int winner) {
        if (winner == 0) {
            myPts++;
            score += 100;
            Sfx.good();
        } else {
            cpuPts++;
            Sfx.bad();
        }
        flash = 20;
        serveSide = winner;
        if (myPts >= TARGET || cpuPts >= TARGET) {
            if (myPts > cpuPts) {
                score += 500 - cpuPts * 40;
                headline = "MATCH " + myPts + "-" + cpuPts;
                endGame(true);
            } else {
                headline = "CPU WINS " + cpuPts + "-" + myPts;
                endGame(false);
            }
            return;
        }
        serve();
    }

    protected void update() {
        if (pr == 0) layout();
        if (flash > 0) flash--;
        int run = Math.max(2, W / 50) << 8;
        int jump = jumpV;
        // player
        if ((held & K_LEFT) != 0) ax -= run;
        if ((held & K_RIGHT) != 0) ax += run;
        ax = FMath.clamp(ax, pr << 8, (netX - pr / 2 - 2) << 8);
        if ((pressed & (K_UP | K_FIRE)) != 0 && ay >= ground << 8) avy = jump;
        ay += avy;
        avy += gp;
        if (ay >= ground << 8) { ay = ground << 8; avy = 0; }
        // CPU: track the predicted landing x on its side
        int target = W * 3 / 4 << 8;
        if (bx > netX << 8 || bvx > 0) {
            int tx = bx, ty = by, tvx = bvx, tvy = bvy;
            for (int k = 0; k < 60 && ty < (ground - pr) << 8; k++) {
                tx += tvx;
                ty += tvy;
                tvy += gravity();
                if (tx > (W - br) << 8 || tx < br << 8) tvx = -tvx;
            }
            target = tx + (pr << 8) / 2;
        }
        int cs = mode == 0 ? run * 3 / 4 : run;
        cx += FMath.clamp(target - cx, -cs, cs);
        cx = FMath.clamp(cx, (netX + pr / 2 + 2) << 8, (W - pr) << 8);
        if (cy >= ground << 8 && bx > netX << 8 && Math.abs(bx - cx) < (pr << 8) && by < cy - (pr * 3 << 8) && bvy > 0 && (mode == 1 || frame % 3 != 0)) cvy = jump * 9 / 10;
        cy += cvy;
        cvy += gp;
        if (cy >= ground << 8) { cy = ground << 8; cvy = 0; }
        if (pause > 0) {
            pause--;
            return;
        }
        // ball
        bvy += gb;
        bx += bvx;
        by += bvy;
        if (bx < br << 8) { bx = br << 8; bvx = -bvx; }
        if (bx > (W - br) << 8) { bx = (W - br) << 8; bvx = -bvx; }
        // net
        int nl = (netX - 2) << 8, nr = (netX + 2) << 8;
        if (bx + (br << 8) > nl && bx - (br << 8) < nr && by + (br << 8) > netTop << 8) {
            if (by < (netTop << 8) && bvy > 0) {
                by = (netTop - br) << 8;
                bvy = -bvy * 3 / 4;
            } else {
                bx = bvx > 0 ? nl - (br << 8) : nr + (br << 8);
                bvx = -bvx * 3 / 4;
            }
        }
        bump(ax, ay, 0);
        bump(cx, cy, 1);
        int maxV = hitV * 5 / 4;
        bvx = FMath.clamp(bvx, -maxV, maxV);
        bvy = FMath.clamp(bvy, -maxV, maxV);
        if (by >= (ground - br) << 8) point(bx < netX << 8 ? 1 : 0);
    }

    private void blob(Graphics g, int x, int y, int col) {
        g.setColor(col);
        g.fillArc(x - pr, y - pr, pr * 2, pr * 2, 0, 180);
        g.setColor(0xFFFFFF);
        g.fillRect(x - pr / 3, y - pr * 2 / 3, Math.max(2, pr / 4), Math.max(2, pr / 4));
        g.fillRect(x + pr / 6, y - pr * 2 / 3, Math.max(2, pr / 4), Math.max(2, pr / 4));
        g.setColor(0x000000);
        g.fillRect(x - pr / 3 + 1, y - pr * 2 / 3 + 1, 1, 1);
        g.fillRect(x + pr / 6 + 1, y - pr * 2 / 3 + 1, 1, 1);
    }

    protected void draw(Graphics g) {
        if (pr == 0) layout();
        g.setColor(0x4FC3F7);
        g.fillRect(0, 0, W, ground);
        g.setColor(0x0288D1);
        g.fillRect(0, ground - Math.max(6, H / 14), W, Math.max(6, H / 14));
        g.setColor(0xFFE082);
        g.fillRect(0, ground, W, H - ground);
        g.setColor(0xFFF176);
        Gfx.disc(g, W - 16, 16 + Gfx.SMALL.getHeight(), 8);
        // net
        g.setColor(0x6D4C41);
        g.fillRect(netX - 1, netTop, 3, ground - netTop);
        g.setColor(0xFFFFFF);
        g.fillRect(netX - 2, netTop, 5, 2);
        blob(g, ax >> 8, ay >> 8, 0x1E88E5);
        blob(g, cx >> 8, cy >> 8, 0xE53935);
        // ball
        g.setColor(0xFFFFFF);
        Gfx.disc(g, bx >> 8, by >> 8, br);
        g.setColor(0xFFA000);
        g.drawLine((bx >> 8) - br + 1, by >> 8, (bx >> 8) + br - 1, by >> 8);
        // shadow
        g.setColor(0xE0B050);
        g.fillRect((bx >> 8) - br, ground + 1, br * 2, 2);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x0D47A1);
        g.drawString("YOU " + myPts, 2, 1, Gfx.TL);
        g.setColor(0xB71C1C);
        g.drawString(cpuPts + " CPU", W - 2, 1, Gfx.TR);
        if (touches > 0 && lastSide == 0) Gfx.text(g, "touch " + touches + "/3", W / 4, ground + 2, Gfx.TC, Gfx.SMALL, 0x5D4037);
        if (flash > 0) Gfx.shadowText(g, "POINT", W / 2, H / 3, Gfx.TC, Gfx.LARGE, serveSide == 0 ? 0x1565C0 : 0xC62828, 0xFFFFFF);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0xFFE082);
        g.fillRect(x, y + h - 6, w, 6);
        g.setColor(0x6D4C41);
        g.fillRect(x + w / 2 - 1, y + h / 3, 3, h * 2 / 3 - 6);
        int t = clock % 40, p = t < 20 ? t : 40 - t;
        int bxp = x + w / 4 + p * (w / 2) / 20;
        int arc = p * (20 - p) * (h - 10) / 100;
        g.setColor(0xFFFFFF);
        Gfx.disc(g, bxp, y + h - 10 - arc, 4);
        int r = Math.max(6, h / 5);
        g.setColor(0x1E88E5);
        g.fillArc(x + w / 4 - r, y + h - 6 - r, r * 2, r * 2, 0, 180);
        g.setColor(0xE53935);
        g.fillArc(x + w * 3 / 4 - r, y + h - 6 - r, r * 2, r * 2, 0, 180);
    }
}
