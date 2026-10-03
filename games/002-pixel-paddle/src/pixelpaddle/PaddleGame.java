package pixelpaddle;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/** Pixel Paddle: vertical table tennis against the CPU. First to 7 wins. */
public class PaddleGame extends Game {
    private static final int WIN_POINTS = 7;

    private int top, bottom, pw, ph, bs;
    private int px, cx;          // paddle centres, <<8
    private int bx, by, vx, vy;  // ball, <<8
    private int speed;           // ball speed, <<8 px per tick
    private int pPts, cPts, rally, hits;
    private boolean serving;
    private boolean playerServes;
    private int serveTimer, flash, cpuErr;

    protected String name() { return "Pixel Paddle"; }

    protected String[] help() {
        return new String[] {
            "Table tennis for one thumb. You defend the bottom of the table, the CPU defends the top.",
            "Where the ball hits your paddle decides its angle: hit it with the edge for a sharp shot. Every return speeds the ball up.",
            "First to 7 points wins.",
            "- Controls",
            "4/6: move paddle",
            "5: serve",
        };
    }

    protected String[] modes() { return new String[] { "Easy", "Normal", "Hard" }; }

    protected int accent() { return 0x33BBFF; }

    protected void newGame() {
        layout();
        pPts = cPts = 0;
        hits = 0;
        playerServes = true;
        px = cx = (W / 2) << 8;
        serve();
    }

    private void layout() {
        int hud = Gfx.SMALL.getHeight() + 2;
        top = hud;
        bottom = H - 1;
        pw = Math.max(16, W / 5);
        ph = Math.max(3, H / 60);
        bs = Math.max(3, W / 40);
    }

    private void serve() {
        serving = true;
        serveTimer = 0;
        rally = 0;
        speed = Math.max(2 << 8, (H << 8) / 70);
        cpuErr = Rnd.range(-pw / 3, pw / 3);
    }

    private int playerY() { return bottom - ph - 4; }
    private int cpuY() { return top + 4; }

    protected void update() {
        layout();
        int move = Math.max(2 << 8, (W << 8) / 40);
        if ((held & K_LEFT) != 0) px -= move;
        if ((held & K_RIGHT) != 0) px += move;
        px = clampPaddle(px);
        if (flash > 0) flash--;

        if (serving) {
            serveTimer++;
            if (playerServes) {
                bx = px;
                by = (playerY() - bs - 1) << 8;
                if ((pressed & K_FIRE) != 0 || serveTimer > 100) launch(-1);
            } else {
                bx = cx;
                by = (cpuY() + ph + 1) << 8;
                if (serveTimer > 25) launch(1);
            }
            moveCpu(true);
            return;
        }
        moveCpu(false);
        bx += vx;
        by += vy;
        int half = (bs << 8) / 2;
        if (bx - half < 0) { bx = half; vx = -vx; Sfx.tone(70, 15); }
        if (bx + half > (W << 8)) { bx = (W << 8) - half; vx = -vx; Sfx.tone(70, 15); }

        int ballTop = (by >> 8) - bs / 2, ballBot = ballTop + bs;
        if (vy > 0 && ballBot >= playerY() && ballTop <= playerY() + ph && Math.abs(bx - px) <= ((pw / 2 + bs / 2) << 8)) {
            bounce(px, -1);
            hits++;
            score += 5;
        } else if (vy < 0 && ballTop <= cpuY() + ph && ballBot >= cpuY() && Math.abs(bx - cx) <= ((pw / 2 + bs / 2) << 8)) {
            bounce(cx, 1);
            cpuErr = Rnd.range(-pw / 2, pw / 2) * (3 - mode) / 2;
        }
        if (ballTop > bottom) point(false);
        else if (ballBot < top) point(true);
    }

    private int clampPaddle(int x) {
        int min = (pw / 2) << 8, max = (W - pw / 2) << 8;
        return x < min ? min : (x > max ? max : x);
    }

    private void moveCpu(boolean idle) {
        int target;
        if (idle || vy > 0) {
            target = (W / 2) << 8;
        } else {
            // predict where the ball reaches the CPU paddle, with wall reflections
            int t = (((by >> 8) - cpuY()) << 8) / Math.max(1, -vy); // ticks until it arrives
            long x = bx + (long) vx * t;
            long span = (long) W << 8;
            x %= span * 2;
            if (x < 0) x += span * 2;
            if (x > span) x = span * 2 - x;
            target = (int) x + (cpuErr << 8);
        }
        int maxStep = ((W << 8) / 60) * (2 + mode) / 2;
        int d = target - cx;
        if (d > maxStep) d = maxStep;
        if (d < -maxStep) d = -maxStep;
        cx = clampPaddle(cx + d);
    }

    private void launch(int dir) {
        serving = false;
        int ang = Rnd.range(-60, 60);
        vx = speed * ang / 160;
        vy = dir * speed;
        Sfx.tone(76, 25);
    }

    private void bounce(int paddleX, int dir) {
        rally++;
        int off = (bx - paddleX) / Math.max(1, pw / 2); // -256..256
        if (off > 256) off = 256;
        if (off < -256) off = -256;
        speed = speed * 105 / 100;
        int maxSpeed = (H << 8) / 25;
        if (speed > maxSpeed) speed = maxSpeed;
        vx = speed * off / 256;
        int vyMag = speed - Math.abs(vx) / 3;
        if (vyMag < speed / 2) vyMag = speed / 2;
        vy = dir * vyMag;
        Sfx.tone(dir < 0 ? 79 : 72, 20);
    }

    private void point(boolean player) {
        if (player) {
            pPts++;
            score += 100;
            Sfx.good();
        } else {
            cPts++;
            Sfx.bad();
        }
        flash = 12;
        playerServes = !player;
        if (pPts >= WIN_POINTS) {
            score += 300 + mode * 200;
            endGame(true);
        } else if (cPts >= WIN_POINTS) {
            endGame(false);
        } else {
            serve();
        }
    }

    protected void draw(Graphics g) {
        g.setColor(flash > 0 && (flash & 2) != 0 ? 0x16304A : 0x0A1A2A);
        g.fillRect(0, 0, W, H);
        g.setColor(0x1D3B5A);
        int mid = (top + bottom) / 2;
        for (int x = 2; x < W; x += 8) g.fillRect(x, mid, 4, 1);
        g.drawRect(0, top, W - 1, bottom - top);
        // scores
        g.setFont(Gfx.LARGE);
        g.setColor(0x24476B);
        g.drawString(String.valueOf(cPts), W - 6, mid - Gfx.LARGE.getHeight() - 4, Gfx.TR);
        g.drawString(String.valueOf(pPts), W - 6, mid + 5, Gfx.TR);
        // paddles
        Gfx.bevel(g, (cx >> 8) - pw / 2, cpuY(), pw, ph, 0xFF5566);
        Gfx.bevel(g, (px >> 8) - pw / 2, playerY(), pw, ph, 0x55DDFF);
        // ball
        g.setColor(0xFFFFFF);
        g.fillRect((bx >> 8) - bs / 2, (by >> 8) - bs / 2, bs, bs);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFF8899);
        g.drawString("CPU " + cPts, 2, 1, Gfx.TL);
        g.setColor(0x88E0FF);
        g.drawString("YOU " + pPts, W - 2, 1, Gfx.TR);
        if (serving && playerServes) Gfx.hint(g, "5: serve", W, playerY() - 4);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int p = Math.max(16, w / 5);
        int t = clock % 60;
        int ballY = t < 30 ? y + 6 + (h - 16) * t / 30 : y + 6 + (h - 16) * (60 - t) / 30;
        int ballX = x + w / 2 + (gamekit.FMath.sin(clock * 5) * (w / 3) >> 10);
        Gfx.bevel(g, ballX - p / 2, y, p, 4, 0xFF5566);
        Gfx.bevel(g, x + w / 2 + (gamekit.FMath.sin(clock * 5 + 40) * (w / 3) >> 10) - p / 2, y + h - 4, p, 4, 0x55DDFF);
        g.setColor(0xFFFFFF);
        g.fillRect(ballX - 2, ballY, 4, 4);
    }
}
