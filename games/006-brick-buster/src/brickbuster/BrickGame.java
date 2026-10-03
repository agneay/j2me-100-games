package brickbuster;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/** Brick Buster: paddle-and-ball brick breaker with power-ups and 8 levels. */
public class BrickGame extends Game {
    private static final int COLS = 8, ROWS = 8, MAXB = 3, MAXC = 4;
    private static final String[][] LEVELS = {
        { "........", "11111111", "22222222", "33333333", "11*11*11", "22222222", "........", "........" },
        { "1......1", "21....12", "321..123", "3*2112*3", "33222233", "#......#", "........", "........" },
        { "..3333..", ".322223.", "32*11*23", "32111123", ".322223.", "..3333..", "........", "........" },
        { "#1#1#1#1", "1*1*1*1*", "22222222", "#.#..#.#", "33333333", "1111*111", "........", "........" },
        { "3.3.3.3.", ".2.2.2.2", "1*1*1*1*", ".3.3.3.3", "2.2.2.2.", ".1.1.1.1", "........", "........" },
        { "33333333", "3######3", "3*2222*3", "3#1111#3", "3#1**1#3", "3######3", "........", "........" },
        { "1.2.3.2.", "*1.2.3.2", "1*1.2.3.", "21*1.2.3", "321*1.2.", "3321*1.2", "........", "........" },
        { "########", "33333333", "2*2*2*2*", "11111111", "33333333", "2*2*2*2*", "1#1#1#1#", "........" },
    };
    private static final int[] PROBE_X = { 0, -1, 1, 0, 0 };
    private static final int[] PROBE_Y = { 0, 0, 0, -1, 1 };
    private static final int[] HIT_COLORS = { 0, 0x4FC3F7, 0x81C784, 0xFFB74D };

    private final byte[] bricks = new byte[COLS * ROWS]; // 0 none, 1-3 hits, 9 steel
    private final boolean[] bonus = new boolean[COLS * ROWS];
    private final int[] bx = new int[MAXB], by = new int[MAXB], vx = new int[MAXB], vy = new int[MAXB];
    private final boolean[] alive = new boolean[MAXB];
    private final int[] cx = new int[MAXC], cy = new int[MAXC], ct = new int[MAXC];
    private int level, lives, px, pw, wideTimer, slowTimer, speed;
    private boolean stuck;
    private int top, brickW, brickH, bs, paddleY, gridX;

    protected String name() { return "Brick Buster"; }

    protected String[] help() {
        return new String[] {
            "Knock out every coloured brick with the ball. Darker bricks need several hits; steel bricks never break.",
            "The ball's angle depends on where it hits the paddle. Don't let it fall past you: you have 3 balls.",
            "Bricks marked with a star drop a capsule: W wide paddle, M multi-ball, S slow ball, + extra life.",
            "- Controls",
            "4/6: move paddle",
            "5: launch ball",
        };
    }

    protected int accent() { return 0xFFB74D; }

    protected void newGame() {
        level = 0;
        lives = 3;
        loadLevel();
    }

    private void layout() {
        int hud = Gfx.SMALL.getHeight() + 2;
        top = hud + 2;
        brickW = (W - 2) / COLS;
        gridX = (W - brickW * COLS) / 2;
        brickH = Math.max(4, H / 28);
        bs = Math.max(3, W / 45);
        paddleY = H - Math.max(8, H / 18);
    }

    private void loadLevel() {
        layout();
        String[] lv = LEVELS[level % LEVELS.length];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char ch = lv[r].charAt(c);
                int i = r * COLS + c;
                bonus[i] = ch == '*';
                bricks[i] = (byte) (ch == '#' ? 9 : (ch == '*' ? 1 : (ch >= '1' && ch <= '3' ? ch - '0' : 0)));
            }
        }
        for (int i = 0; i < MAXC; i++) ct[i] = 0;
        wideTimer = slowTimer = 0;
        px = (W / 2) << 8;
        resetBall();
    }

    private void resetBall() {
        for (int i = 0; i < MAXB; i++) alive[i] = false;
        alive[0] = true;
        stuck = true;
        speed = ((H << 8) / 75) * (100 + (level / LEVELS.length) * 15 + Math.min(level, 7) * 3) / 100;
    }

    private int ballSpeed() {
        return slowTimer > 0 ? speed * 2 / 3 : speed;
    }

    protected void update() {
        layout();
        pw = (wideTimer > 0 ? W * 3 / 10 : W / 5);
        int move = (W << 8) / 28;
        if ((held & K_LEFT) != 0) px -= move;
        if ((held & K_RIGHT) != 0) px += move;
        int minX = (pw / 2) << 8, maxX = (W - pw / 2) << 8;
        if (px < minX) px = minX;
        if (px > maxX) px = maxX;
        if (wideTimer > 0) wideTimer--;
        if (slowTimer > 0) slowTimer--;

        if (stuck) {
            bx[0] = px;
            by[0] = (paddleY - bs) << 8;
            if ((pressed & K_FIRE) != 0) {
                stuck = false;
                vx[0] = ballSpeed() / 3;
                vy[0] = -ballSpeed();
                Sfx.tone(76, 20);
            }
        } else {
            int n = 0;
            for (int i = 0; i < MAXB; i++) {
                if (!alive[i]) continue;
                stepBall(i);
                if (alive[i]) n++;
            }
            if (n == 0) {
                lives--;
                Sfx.bad();
                if (lives <= 0) {
                    endGame(false);
                    return;
                }
                resetBall();
            }
        }
        updateCapsules();
        boolean left = false;
        for (int i = 0; i < bricks.length; i++) if (bricks[i] > 0 && bricks[i] < 9) left = true;
        if (!left) {
            score += 500 + lives * 100;
            level++;
            Sfx.win();
            if (level >= LEVELS.length * 2) {
                endGame(true);
                return;
            }
            loadLevel();
        }
    }

    private void stepBall(int i) {
        // normalise speed (multi-ball and slow pick-ups change it)
        int sp = ballSpeed();
        if (Math.abs(vy[i]) != sp) {
            vx[i] = vx[i] * sp / Math.max(1, Math.abs(vy[i]));
            vy[i] = vy[i] > 0 ? sp : -sp;
        }
        int nx = bx[i] + vx[i], ny = by[i] + vy[i];
        int r = bs / 2;
        if ((nx >> 8) - r < 0) { nx = r << 8; vx[i] = Math.abs(vx[i]); Sfx.tone(70, 10); }
        if ((nx >> 8) + r > W) { nx = (W - r) << 8; vx[i] = -Math.abs(vx[i]); Sfx.tone(70, 10); }
        if ((ny >> 8) - r < top) { ny = (top + r) << 8; vy[i] = Math.abs(vy[i]); Sfx.tone(70, 10); }
        // bricks: probe the ball centre and its four edges
        for (int k = 0; k < 5; k++) {
            int qx = (nx >> 8) + PROBE_X[k] * r, qy = (ny >> 8) + PROBE_Y[k] * r;
            int c = (qx - gridX) / brickW, rr = (qy - top - brickH) / brickH;
            if (qx < gridX || qy < top + brickH || c < 0 || c >= COLS || rr < 0 || rr >= ROWS) continue;
            int idx = rr * COLS + c;
            if (bricks[idx] == 0) continue;
            // reflect on the axis we came through
            int ox = (bx[i] >> 8) + PROBE_X[k] * r;
            if (ox >= gridX && (ox - gridX) / brickW != c) vx[i] = -vx[i];
            else vy[i] = -vy[i];
            hitBrick(idx);
            nx = bx[i] + vx[i];
            ny = by[i] + vy[i];
            break;
        }
        // paddle
        int pl = (px >> 8) - pw / 2;
        if (vy[i] > 0 && (ny >> 8) + r >= paddleY && (by[i] >> 8) + r <= paddleY + 2
                && (nx >> 8) >= pl - r && (nx >> 8) <= pl + pw + r) {
            int off = ((nx - px) * 2) / Math.max(1, pw); // about -256..256
            if (off > 230) off = 230;
            if (off < -230) off = -230;
            vy[i] = -ballSpeed();
            vx[i] = ballSpeed() * off / 180;
            ny = (paddleY - r) << 8;
            Sfx.tone(64, 15);
        }
        bx[i] = nx;
        by[i] = ny;
        if ((ny >> 8) - r > H) alive[i] = false;
    }

    private void hitBrick(int idx) {
        if (bricks[idx] == 9) {
            Sfx.tone(90, 15);
            return;
        }
        bricks[idx]--;
        score += 10;
        if (bricks[idx] == 0) {
            score += 15;
            Sfx.tone(84, 20);
            if (bonus[idx]) {
                bonus[idx] = false;
                for (int k = 0; k < MAXC; k++) {
                    if (ct[k] == 0) {
                        ct[k] = 1 + Rnd.nextInt(4);
                        cx[k] = gridX + (idx % COLS) * brickW + brickW / 2;
                        cy[k] = (top + brickH + (idx / COLS) * brickH) << 8;
                        break;
                    }
                }
            }
        } else {
            Sfx.tone(78, 15);
        }
    }

    private void updateCapsules() {
        for (int k = 0; k < MAXC; k++) {
            if (ct[k] == 0) continue;
            cy[k] += (H << 8) / 160;
            int y = cy[k] >> 8;
            int pl = (px >> 8) - pw / 2;
            if (y + 4 >= paddleY && y <= paddleY + 4 && cx[k] >= pl - 4 && cx[k] <= pl + pw + 4) {
                apply(ct[k]);
                ct[k] = 0;
            } else if (y > H) {
                ct[k] = 0;
            }
        }
    }

    private void apply(int t) {
        score += 50;
        Sfx.good();
        if (t == 1) wideTimer = 400;
        else if (t == 3) slowTimer = 300;
        else if (t == 4) lives = Math.min(5, lives + 1);
        else if (!stuck) {
            int src = 0;
            for (int i = 0; i < MAXB; i++) if (alive[i]) src = i;
            for (int i = 0; i < MAXB; i++) {
                if (!alive[i]) {
                    alive[i] = true;
                    bx[i] = bx[src];
                    by[i] = by[src];
                    vx[i] = i == 1 ? -ballSpeed() / 2 : ballSpeed() / 2;
                    vy[i] = -ballSpeed();
                }
            }
        }
    }

    protected void draw(Graphics g) {
        layout();
        pw = (wideTimer > 0 ? W * 3 / 10 : W / 5);
        g.setColor(0x10131C);
        g.fillRect(0, 0, W, H);
        g.setColor(0x171C28);
        for (int y = top; y < H; y += 6) g.drawLine(0, y, W, y);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int v = bricks[r * COLS + c];
                if (v == 0) continue;
                int x = gridX + c * brickW, y = top + brickH + r * brickH;
                int col = v == 9 ? 0x8A94A6 : HIT_COLORS[v];
                Gfx.bevel(g, x, y, brickW - 1, brickH - 1, col);
                if (bonus[r * COLS + c]) {
                    g.setColor(0xFFFFFF);
                    g.fillRect(x + brickW / 2 - 1, y + brickH / 2 - 1, 2, 2);
                }
            }
        }
        String caps = " WMS+";
        for (int k = 0; k < MAXC; k++) {
            if (ct[k] == 0) continue;
            int y = cy[k] >> 8;
            g.setColor(0xF06292);
            g.fillRoundRect(cx[k] - 5, y, 11, 8, 4, 4);
            g.setFont(Gfx.SMALL);
            g.setColor(0xFFFFFF);
            g.drawChar(caps.charAt(ct[k]), cx[k] + 1, y, Gfx.TC);
        }
        Gfx.bevel(g, (px >> 8) - pw / 2, paddleY, pw, Math.max(3, bs), wideTimer > 0 ? 0xFFD54F : 0xE0E0E0);
        g.setColor(0xFFFFFF);
        for (int i = 0; i < MAXB; i++) {
            if (alive[i]) Gfx.disc(g, bx[i] >> 8, by[i] >> 8, bs / 2 + 1);
        }
        g.setColor(0x0B0E15);
        g.fillRect(0, 0, W, top - 1);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFE0B2);
        g.drawString(String.valueOf(score), 2, 1, Gfx.TL);
        g.drawString("L" + (level + 1), W / 2, 1, Gfx.TC);
        g.setColor(0xFF8A80);
        for (int i = 0; i < lives; i++) Gfx.disc(g, W - 5 - i * 7, 5, 2);
        if (stuck) Gfx.hint(g, "5: launch", W, paddleY - 2);
    }
}
