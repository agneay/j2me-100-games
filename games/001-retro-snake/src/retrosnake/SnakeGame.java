package retrosnake;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/** Retro Snake: eat, grow, don't bite yourself. Three wall modes. */
public class SnakeGame extends Game {
    private static final int[] DX = { 0, 1, 0, -1 };
    private static final int[] DY = { -1, 0, 1, 0 };

    private int cell, cols, rows, ox, oy, hud;
    private int[] body;      // ring buffer of x | (y << 8)
    private int headIdx, len, grow;
    private int dir;
    private final int[] queue = new int[2];
    private int queued;
    private boolean[] wall;
    private int food, bonus, bonusTimer, moveWait, speed, eaten;

    protected String name() { return "Retro Snake"; }

    protected String[] help() {
        return new String[] {
            "Steer the snake to eat the red apples. Each apple makes you longer and faster.",
            "Gold stars appear now and then: grab them before they fade for a big bonus.",
            "Hitting yourself ends the game. In Classic and Maze modes the walls are deadly too; in Wrap mode you come out the other side.",
            "- Controls",
            "2/4/6/8 or joystick: turn",
        };
    }

    protected String[] modes() { return new String[] { "Classic", "Wrap", "Maze" }; }

    protected int accent() { return 0x44DD55; }

    protected void newGame() {
        hud = Gfx.SMALL.getHeight() + 2;
        cell = Math.max(4, Math.min(W / 20, (H - hud) / 20));
        cols = (W - 2) / cell;
        rows = (H - hud - 2) / cell;
        ox = (W - cols * cell) / 2;
        oy = hud + (H - hud - rows * cell) / 2;
        body = new int[cols * rows];
        wall = new boolean[cols * rows];
        if (mode == 2) buildMaze();
        len = 3;
        headIdx = 2;
        int sx = cols / 4, sy = rows / 2;
        for (int i = 0; i < 3; i++) body[i] = (sx + i) | (sy << 8);
        dir = 1;
        queued = 0;
        grow = 0;
        speed = 5;
        eaten = 0;
        moveWait = speed;
        bonus = -1;
        placeFood();
    }

    private void buildMaze() {
        int midY = rows / 2;
        for (int x = cols / 5; x < cols * 4 / 5; x++) {
            if (Math.abs(x - cols / 2) > 2) {
                wall[x + (rows / 5) * cols] = true;
                wall[x + (rows - 1 - rows / 5) * cols] = true;
            }
        }
        for (int y = midY - rows / 6; y <= midY + rows / 6; y++) {
            wall[cols / 2 + y * cols] = true;
        }
    }

    private boolean occupied(int x, int y) {
        if (wall[x + y * cols]) return true;
        for (int i = 0; i < len; i++) {
            int p = body[(headIdx - i + body.length) % body.length];
            if ((p & 255) == x && (p >> 8) == y) return true;
        }
        return false;
    }

    private int freeCell() {
        for (int tries = 0; tries < 500; tries++) {
            int x = Rnd.nextInt(cols), y = Rnd.nextInt(rows);
            if (!occupied(x, y) && (x | (y << 8)) != food && (x | (y << 8)) != bonus) return x | (y << 8);
        }
        return -1;
    }

    private void placeFood() {
        food = freeCell();
    }

    protected void update() {
        // buffer up to two turns so quick taps are not lost
        int want = -1;
        if ((pressed & K_UP) != 0) want = 0;
        else if ((pressed & K_RIGHT) != 0) want = 1;
        else if ((pressed & K_DOWN) != 0) want = 2;
        else if ((pressed & K_LEFT) != 0) want = 3;
        if (want >= 0 && queued < 2) {
            int last = queued > 0 ? queue[queued - 1] : dir;
            if (want != last && want != ((last + 2) & 3)) queue[queued++] = want;
        }
        if (bonus >= 0 && --bonusTimer <= 0) bonus = -1;
        if (--moveWait > 0) return;
        moveWait = speed;
        if (queued > 0) {
            dir = queue[0];
            queue[0] = queue[1];
            queued--;
        }
        int head = body[headIdx];
        int nx = (head & 255) + DX[dir], ny = (head >> 8) + DY[dir];
        if (mode == 1) {
            nx = (nx + cols) % cols;
            ny = (ny + rows) % rows;
        } else if (nx < 0 || ny < 0 || nx >= cols || ny >= rows) {
            crash();
            return;
        }
        // moving into the current tail cell is fine unless we're growing
        boolean hit = wall[nx + ny * cols];
        for (int i = 0; i < len - (grow > 0 ? 0 : 1) && !hit; i++) {
            int p = body[(headIdx - i + body.length) % body.length];
            if ((p & 255) == nx && (p >> 8) == ny) hit = true;
        }
        if (hit) {
            crash();
            return;
        }
        headIdx = (headIdx + 1) % body.length;
        body[headIdx] = nx | (ny << 8);
        if (grow > 0) {
            grow--;
            len++;
        }
        int np = nx | (ny << 8);
        if (np == food) {
            grow += 2;
            eaten++;
            score += 10 + (6 - speed) * 2;
            Sfx.good();
            if (eaten % 4 == 0 && speed > 1) speed--;
            if (eaten % 5 == 0 && bonus < 0) {
                bonus = freeCell();
                bonusTimer = 120;
            }
            placeFood();
            if (food < 0) {
                endGame(true);
            }
        } else if (np == bonus) {
            score += 50;
            bonus = -1;
            Sfx.tone(88, 60);
        }
    }

    private void crash() {
        Sfx.bad();
        endGame(false);
    }

    protected void draw(Graphics g) {
        g.setColor(0x0E1A10);
        g.fillRect(0, 0, W, H);
        // playfield
        g.setColor(0x16301A);
        g.fillRect(ox, oy, cols * cell, rows * cell);
        g.setColor(0x1B3A20);
        for (int y = 0; y < rows; y++) {
            for (int x = (y & 1); x < cols; x += 2) g.fillRect(ox + x * cell, oy + y * cell, cell, cell);
        }
        if (mode != 1) {
            g.setColor(0x7A8C6A);
            g.drawRect(ox - 1, oy - 1, cols * cell + 1, rows * cell + 1);
        }
        for (int i = 0; i < wall.length; i++) {
            if (wall[i]) Gfx.bevel(g, ox + (i % cols) * cell, oy + (i / cols) * cell, cell, cell, 0x707A88);
        }
        if (food >= 0) {
            int fx = ox + (food & 255) * cell, fy = oy + (food >> 8) * cell;
            g.setColor(0xE83A3A);
            g.fillArc(fx, fy + 1, cell, cell - 1, 0, 360);
            g.setColor(0x55CC44);
            g.fillRect(fx + cell / 2, fy, Math.max(1, cell / 4), Math.max(1, cell / 3));
        }
        if (bonus >= 0 && (bonusTimer > 30 || (bonusTimer & 2) == 0)) {
            int bx = ox + (bonus & 255) * cell, by = oy + (bonus >> 8) * cell;
            g.setColor(0xFFD933);
            g.fillTriangle(bx + cell / 2, by, bx, by + cell - 1, bx + cell - 1, by + cell - 1);
            g.fillTriangle(bx, by + cell / 3, bx + cell - 1, by + cell / 3, bx + cell / 2, by + cell);
        }
        for (int i = len - 1; i >= 0; i--) {
            int p = body[(headIdx - i + body.length) % body.length];
            int px = ox + (p & 255) * cell, py = oy + (p >> 8) * cell;
            int c = i == 0 ? 0x9CFF6A : ((i & 1) == 0 ? 0x3FBF3F : 0x35A835);
            Gfx.bevel(g, px, py, cell, cell, c);
            if (i == 0 && cell >= 5) {
                g.setColor(0x102010);
                int ex = DY[dir] != 0 ? 1 : 0, ey = DX[dir] != 0 ? 1 : 0;
                int cx = px + cell / 2 + DX[dir] * cell / 4, cy = py + cell / 2 + DY[dir] * cell / 4;
                int sp = Math.max(1, cell / 4);
                g.fillRect(cx - ex * sp, cy - ey * sp, 1, 1);
                g.fillRect(cx + ex * sp, cy + ey * sp, 1, 1);
            }
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xCCFFCC);
        g.drawString("SCORE " + score, 2, 1, Gfx.TL);
        g.setColor(0x88AA88);
        g.drawString("LEN " + (len + grow), W - 2, 1, Gfx.TR);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(5, Math.min(10, h / 5));
        int n = Math.min(12, w / s - 2);
        int x0 = x + (w - n * s) / 2;
        for (int i = 0; i < n; i++) {
            int dy = gamekit.FMath.sin((clock * 10 - i * 22) & 255) * (h / 4) >> 10;
            Gfx.bevel(g, x0 + i * s, y + h / 2 + dy - s / 2, s, s, i == n - 1 ? 0x9CFF6A : 0x3FBF3F);
        }
        g.setColor(0xE83A3A);
        g.fillArc(x0 + n * s + 2, y + h / 2 - s / 2, s, s, 0, 360);
    }
}
