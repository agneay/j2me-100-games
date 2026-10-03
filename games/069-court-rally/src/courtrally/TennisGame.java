package courtrally;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Court Rally: top-down tennis against the CPU. Move along the baseline,
 * swing as the ball arrives; hitting early pulls it left, late pushes it right.
 */
public class TennisGame extends Game {
    private static final String[] PTS = { "0", "15", "30", "40" };
    private int cl, cr, ct, cb; // court bounds
    private int px, cx;         // player / cpu x (<<8)
    private int bx, by, bz, vx, vy, vz; // ball (<<8), z = height
    private int myPts, cpuPts, myGames, cpuGames, server, serveT, swingT, bounces, lastHitter, rally;
    private boolean live;
    private String call = "";
    private int callT;

    protected String name() { return "Court Rally"; }

    protected String[] help() {
        return new String[] {
            "Tennis from above. You play the near side. Move left and right to reach the ball and press 5 to swing as it reaches you.",
            "Timing aims the shot: swing early to pull it cross-court one way, late to push it the other. A ball may bounce once before you hit it; twice and the point is lost.",
            "Standard scoring: 15, 30, 40, game (win by two). First to 3 games wins the set.",
            "- Controls",
            "4/6: move  5: swing / serve",
            "2: lob (high, slow)",
        };
    }

    protected String[] modes() { return new String[] { "Club", "Pro" }; }

    protected int accent() { return 0x52B788; }

    protected void newGame() {
        myPts = cpuPts = myGames = cpuGames = 0;
        server = 0;
        layout();
        px = cx = (W / 2) << 8;
        newPoint();
    }

    private void layout() {
        int hud = Gfx.SMALL.getHeight() + 2;
        cl = W / 8;
        cr = W - W / 8;
        ct = hud + 10;
        cb = H - 10;
    }

    private int myY() { return cb - 4; }
    private int cpuY() { return ct + 4; }

    private void newPoint() {
        live = false;
        serveT = 30;
        bounces = 0;
        rally = 0;
        if (server == 0) { bx = px; by = (myY() - 4) << 8; } else { bx = cx; by = (cpuY() + 4) << 8; }
        bz = 6 << 8;
        vx = vy = vz = 0;
    }

    private void hit(int fromPlayer, int timing, boolean lob) {
        int t = lob ? 34 : (mode == 1 ? 20 : 24) - Math.min(6, rally / 3);
        int targetX = (W / 2 + timing * (cr - cl) / 7 + Rnd.range(-6, 6)) << 8;
        targetX = Math.max((cl + 4) << 8, Math.min((cr - 4) << 8, targetX));
        int targetY = fromPlayer == 1 ? (ct + (cb - ct) / 4) << 8 : (cb - (cb - ct) / 4) << 8;
        vx = (targetX - bx) / t;
        vy = (targetY - by) / t;
        // flight so the first bounce lands at the target
        vz = (lob ? 6 : 3) << 8 >> 2;
        bz = 6 << 8;
        lastHitter = fromPlayer;
        bounces = 0;
        live = true;
        rally++;
        Sfx.tone(fromPlayer == 1 ? 76 : 70, 20);
    }

    protected void update() {
        layout();
        if (callT > 0) callT--;
        int sp = Math.max(2, W / 45) << 8;
        if ((held & K_LEFT) != 0) px -= sp;
        if ((held & K_RIGHT) != 0) px += sp;
        px = Math.max((cl - 6) << 8, Math.min((cr + 6) << 8, px));
        if (swingT > 0) swingT--;
        if (!live) {
            if (server == 0) {
                bx = px;
                if ((pressed & K_FIRE) != 0 || --serveT < -60) hit(1, Rnd.range(-2, 2), false);
            } else if (--serveT <= 0) {
                hit(0, Rnd.range(-3, 3), false);
            }
            moveCpu();
            return;
        }
        bx += vx;
        by += vy;
        vz -= 18;
        bz += vz;
        if (bz <= 0) {
            bz = 0;
            vz = -vz * 6 / 10;
            bounces++;
            Sfx.tone(50, 10);
            int x = bx >> 8, y = by >> 8;
            if (bounces == 1) {
                boolean in = x >= cl && x <= cr && (lastHitter == 1 ? y <= (ct + cb) / 2 && y >= ct : y >= (ct + cb) / 2 && y <= cb);
                if (!in) { point(lastHitter == 0, "OUT!"); return; }
            } else if (bounces >= 2) {
                point(lastHitter == 1, "DOUBLE BOUNCE");
                return;
            }
        }
        // player swing
        int y = by >> 8;
        if ((pressed & (K_FIRE | K_UP)) != 0 && swingT == 0) swingT = 6;
        if (lastHitter == 0 && swingT > 0 && Math.abs(y - myY()) < 12 && Math.abs((bx - px) >> 8) < W / 9 + 4) {
            int timing = (myY() - y) / 3; // early (ball still far) = positive
            hit(1, Math.max(-3, Math.min(3, timing)), (pressed & K_UP) != 0 || (held & K_UP) != 0);
            swingT = 0;
        }
        moveCpu();
        if (lastHitter == 1 && Math.abs(y - cpuY()) < 8 && bounces <= 1) {
            int reach = W / 9 + (mode == 1 ? 6 : 2);
            if (Math.abs((bx - cx) >> 8) < reach && Rnd.chance(mode == 1 ? 93 : 82)) hit(0, Rnd.range(-3, 3), Rnd.chance(15));
        }
        if (y < ct - 20) point(true, "WINNER!");
        else if (y > cb + 20) point(false, "Past you!");
    }

    private void moveCpu() {
        int target = (W / 2) << 8;
        if (live && lastHitter == 1 && vy < 0) {
            int ticks = (by - (cpuY() << 8)) / -vy; // ticks until the ball reaches the CPU's line
            target = bx + vx * Math.max(0, ticks);
        }
        int sp = (Math.max(2, W / 50) << 8) * (mode == 1 ? 12 : 9) / 10;
        int d = target - cx;
        if (d > sp) d = sp;
        if (d < -sp) d = -sp;
        cx += d;
    }

    private void point(boolean mine, String why) {
        live = false;
        call = why;
        callT = 30;
        if (mine) { myPts++; Sfx.good(); } else { cpuPts++; Sfx.bad(); }
        if (myPts >= 4 && myPts - cpuPts >= 2) { myGames++; myPts = cpuPts = 0; server ^= 1; call = "GAME!"; }
        else if (cpuPts >= 4 && cpuPts - myPts >= 2) { cpuGames++; myPts = cpuPts = 0; server ^= 1; call = "CPU game"; }
        score = myGames * 100 + myPts * 10;
        if (myGames >= 3) { headline = "SET " + myGames + "-" + cpuGames; score += 300; endGame(true); return; }
        if (cpuGames >= 3) { headline = "LOST " + myGames + "-" + cpuGames; endGame(false); return; }
        newPoint();
    }

    private String scoreText() {
        if (myPts >= 3 && cpuPts >= 3) return myPts == cpuPts ? "Deuce" : (myPts > cpuPts ? "Adv you" : "Adv CPU");
        return PTS[Math.min(3, myPts)] + "-" + PTS[Math.min(3, cpuPts)];
    }

    protected void draw(Graphics g) {
        layout();
        g.setColor(0x2E7D5B);
        g.fillRect(0, 0, W, H);
        g.setColor(0x3A86B5);
        g.fillRect(cl, ct, cr - cl, cb - ct);
        g.setColor(0xFFFFFF);
        g.drawRect(cl, ct, cr - cl, cb - ct);
        g.drawLine(W / 2, ct + (cb - ct) / 4, W / 2, cb - (cb - ct) / 4);
        g.drawLine(cl, ct + (cb - ct) / 4, cr, ct + (cb - ct) / 4);
        g.drawLine(cl, cb - (cb - ct) / 4, cr, cb - (cb - ct) / 4);
        g.setColor(0xECEFF1);
        g.fillRect(cl - 4, (ct + cb) / 2 - 1, cr - cl + 8, 2);
        int pw = W / 9;
        g.setColor(0xE53935);
        g.fillRect((cx >> 8) - 3, cpuY() - 6, 7, 8);
        g.setColor(0xFFCC80);
        Gfx.disc(g, cx >> 8, cpuY() - 8, 3);
        g.setColor(0xFDD835);
        g.fillRect((px >> 8) - 3, myY() - 6, 7, 8);
        g.setColor(0xFFCC80);
        Gfx.disc(g, px >> 8, myY() - 8, 3);
        g.setColor(0x6D4C41);
        int sw = swingT > 0 ? pw : pw / 2;
        g.drawLine((px >> 8) + 3, myY() - 3, (px >> 8) + 3 + sw / 2, myY() - 3 - (swingT > 0 ? 4 : 0));
        g.setColor(0x1B3A2A);
        Gfx.disc(g, bx >> 8, by >> 8, 2);
        g.setColor(0xDCE775);
        Gfx.disc(g, bx >> 8, (by - bz / 2) >> 8, 2 + (bz >> 11));
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString("You " + myGames + "  CPU " + cpuGames, 2, 1, Gfx.TL);
        g.setColor(0xFFEB3B);
        g.drawString(scoreText(), W - 2, 1, Gfx.TR);
        if (callT > 0) Gfx.shadowText(g, call, W / 2, H / 2 - 12, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
        if (!live && server == 0) Gfx.hint(g, "5: serve", W, H - 12);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x3A86B5);
        g.fillRect(x + w / 6, y, w * 2 / 3, h);
        g.setColor(0xFFFFFF);
        g.drawRect(x + w / 6, y, w * 2 / 3, h);
        g.fillRect(x + w / 6 - 2, y + h / 2, w * 2 / 3 + 4, 1);
        int t = clock % 40;
        int by2 = t < 20 ? y + h * t / 20 : y + h * (40 - t) / 20;
        g.setColor(0xDCE775);
        Gfx.disc(g, x + w / 3 + t * w / 120, by2, 2);
    }
}
