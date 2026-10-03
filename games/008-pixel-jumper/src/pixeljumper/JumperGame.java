package pixeljumper;

import gamekit.Body;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import gamekit.TileMap;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Pixel Jumper: side-scrolling platformer. Eight levels are built from a
 * fixed seed by a generator that only emits jumps the player can make.
 */
public class JumperGame extends Game {
    private static final int ROWS = 12, MAXW = 240, LEVELS = 8, MAXE = 40;

    private TileMap map;
    private int endCol;
    private final Body p = new Body();
    private final Body[] en = new Body[MAXE];
    private final boolean[] enAlive = new boolean[MAXE];
    private int nEn;
    private int level, lives, coins, facing = 1;
    private int coyote, jumpBuf, invuln, deadTimer, clearTimer;
    private boolean cut;
    private int safeX, safeY, camX, camY, ts, hud;
    private Image hero, heroJump, blob;
    private int spriteTs;

    protected String name() { return "Pixel Jumper"; }

    protected String[] help() {
        return new String[] {
            "Run and jump through eight side-scrolling levels to reach the flag at the end of each one.",
            "Collect coins (100 coins = extra life), bounce on springs, and stomp walking blobs by landing on them. Spikes and pits cost a life.",
            "Hold the jump key for a higher jump.",
            "- Controls",
            "4/6: run   2 or 5: jump",
            "1/3: jump left / right",
            "8: drop through wooden platforms",
        };
    }

    protected int accent() { return 0xFF9F1C; }

    protected void newGame() {
        level = 0;
        lives = 3;
        coins = 0;
        for (int i = 0; i < MAXE; i++) if (en[i] == null) en[i] = new Body();
        buildLevel();
    }

    // ----------------------------------------------------------- generator

    private int g; // current ground top row
    private int c; // current column

    private void ground(int len) {
        for (int i = 0; i < len && c < MAXW; i++, c++) {
            for (int r = g; r < ROWS; r++) map.set(c, r, '#');
        }
    }

    private void buildLevel() {
        Rnd.seed(1000 + level * 7919);
        map = new TileMap(MAXW, ROWS);
        map.solids = "#";
        map.oneWay = "=";
        c = 0;
        g = 9;
        ground(7);
        map.set(2, g - 1, 'S');
        int target = Math.min(MAXW - 20, 70 + level * 14);
        while (c < target) {
            int t = Rnd.nextInt(level == 0 ? 4 : 7);
            switch (t) {
                case 0: { // flat with coins and maybe an enemy
                    int len = Rnd.range(4, 7), start = c;
                    ground(len);
                    if (Rnd.chance(60)) for (int i = start + 1; i < c - 1; i++) map.set(i, g - 2, 'o');
                    if (len >= 5 && Rnd.chance(30 + level * 8)) map.set(start + len / 2, g - 1, 'E');
                    break;
                }
                case 1: { // pit with spikes
                    int gw = level >= 2 && Rnd.chance(40) ? 3 : 2;
                    for (int i = 0; i < gw; i++, c++) map.set(c, ROWS - 1, '^');
                    int ng = g + Rnd.range(gw == 3 ? 0 : -1, 1);
                    g = Math.max(5, Math.min(10, ng));
                    ground(3);
                    break;
                }
                case 2: { // step up or down
                    int dh = Rnd.range(1, 2) * (Rnd.chance(50) ? -1 : 1);
                    g = Math.max(5, Math.min(10, g + dh));
                    ground(4);
                    break;
                }
                case 3: { // hop over a wide pit using a floating platform
                    int gw = Rnd.range(5, 6);
                    int mid = c + gw / 2 - 1;
                    for (int i = 0; i < gw; i++, c++) map.set(c, ROWS - 1, '^');
                    int pr = g - 2;
                    for (int i = 0; i < 2; i++) {
                        map.set(mid + i, pr, '=');
                        map.set(mid + i, pr - 1, 'o');
                    }
                    ground(3);
                    break;
                }
                case 4: { // spike patch on the ground
                    int start = c;
                    ground(6);
                    map.set(start + 2, g - 1, '^');
                    if (level >= 3 && Rnd.chance(50)) map.set(start + 3, g - 1, '^');
                    break;
                }
                case 5: { // spring to a high ledge
                    if (g < 8) { ground(3); break; }
                    int start = c;
                    ground(3);
                    map.set(start + 1, g - 1, 'B');
                    g = Math.max(4, g - 4);
                    int s2 = c;
                    ground(4);
                    map.set(s2 + 1, g - 1, 'o');
                    map.set(s2 + 2, g - 1, 'o');
                    break;
                }
                default: { // enemy pair on a long run
                    int start = c;
                    ground(8);
                    map.set(start + 2, g - 1, 'E');
                    map.set(start + 6, g - 1, 'E');
                    break;
                }
            }
        }
        ground(4);
        map.set(c, g - 1, 'F');
        ground(5);
        endCol = c;
        for (int i = c; i < MAXW; i++) for (int r = 0; r < ROWS; r++) map.set(i, r, '#');
        layout();
        // enemies become bodies
        nEn = 0;
        for (int i = 0; i < map.t.length; i++) {
            if (map.t[i] == 'E' && nEn < MAXE) {
                Body e = en[nEn];
                e.w = ts * 3 / 4;
                e.h = ts * 5 / 8;
                e.place((i % MAXW) * ts, (i / MAXW) * ts + ts - e.h);
                e.vx = -(ts << 8) / 24;
                enAlive[nEn++] = true;
                map.t[i] = ' ';
            }
        }
        int s = map.find('S');
        map.set(s & 0xFFFF, s >> 16, ' ');
        p.w = ts * 5 / 8;
        p.h = ts * 7 / 8;
        safeX = (s & 0xFFFF) * ts;
        safeY = (s >> 16) * ts + ts - p.h;
        p.place(safeX, safeY);
        deadTimer = clearTimer = 0;
        invuln = 20;
        Rnd.seed(System.currentTimeMillis());
    }

    private void layout() {
        hud = Gfx.SMALL.getHeight() + 2;
        ts = Math.max(8, Math.min((H - hud) / ROWS, W / 14));
        if (map != null) map.ts = ts;
        if (hero == null || spriteTs != ts) buildSprites();
    }

    private void buildSprites() {
        spriteTs = ts;
        int sc = Math.max(1, ts / 9);
        int[] pal = { 0, 0x222034, 0xFF9F1C, 0xFFE0BD, 0x3F7FD9, 0xFFFFFF };
        hero = Gfx.sprite(new String[] {
            "..2222..", ".222222.", ".1335351", ".133333.", "..3333..", ".444444.", "34444443", "..4..4..", ".11..11." }, pal, sc);
        heroJump = Gfx.sprite(new String[] {
            "..2222..", ".222222.", ".1335351", ".133333.", "3.3333.3", ".444444.", "..4444..", ".4....4.", "11....11" }, pal, sc);
        int[] bp = { 0, 0x2B1B3D, 0x8E44AD, 0xFFFFFF, 0xC39BD3 };
        blob = Gfx.sprite(new String[] { "..2222..", ".224422.", "23322332", "23122312", "22222222", "22222222", ".1.11.1." }, bp, sc);
    }

    // -------------------------------------------------------------- play

    protected void update() {
        layout();
        if (clearTimer > 0) {
            if (--clearTimer == 0) {
                level++;
                if (level >= LEVELS) {
                    score += lives * 500;
                    endGame(true);
                } else {
                    buildLevel();
                }
            }
            return;
        }
        if (deadTimer > 0) {
            if (--deadTimer == 0) respawn();
            return;
        }
        int run = (ts << 8) * 30 / 100;
        int accel = run / 4;
        boolean left = (held & (K_LEFT | (K_NUM0 << 1))) != 0, right = (held & (K_RIGHT | (K_NUM0 << 3))) != 0;
        if (left && !right) { p.vx = Math.max(-run, p.vx - accel); facing = -1; }
        else if (right && !left) { p.vx = Math.min(run, p.vx + accel); facing = 1; }
        else p.vx = p.vx * (p.ground ? 1 : 3) / 4;
        if ((tapped & (K_UP | K_FIRE | (K_NUM0 << 1) | (K_NUM0 << 3))) != 0) jumpBuf = 4;
        else if (jumpBuf > 0) jumpBuf--;
        if (p.ground) coyote = 4;
        else if (coyote > 0) coyote--;
        int jumpV = (ts << 8) * 90 / 100;
        if (jumpBuf > 0 && coyote > 0) {
            p.vy = -jumpV;
            jumpBuf = 0;
            coyote = 0;
            cut = false;
            Sfx.tone(72, 25);
        }
        boolean holdJump = (held & (K_UP | K_FIRE | (K_NUM0 << 1) | (K_NUM0 << 3))) != 0;
        if (p.vy < 0 && !holdJump && !cut) {
            p.vy = p.vy * 5 / 10;
            cut = true;
        }
        if ((pressed & K_DOWN) != 0) p.dropThrough = true;
        p.vy += (ts << 8) / 9;
        int maxFall = (ts << 8) * 85 / 100;
        if (p.vy > maxFall) p.vy = maxFall;
        p.move(map);
        if (p.ground && invuln == 0) {
            int under = map.get(map.tile(p.cx()), map.tile(p.py() + p.h));
            if (under == '#' && frame % 10 == 0) {
                safeX = p.px();
                safeY = p.py();
            }
        }
        if (invuln > 0) invuln--;
        tiles();
        enemies();
        if (p.py() > ROWS * ts) die();
    }

    private void tiles() {
        int c0 = map.tile(p.px() + 1), c1 = map.tile(p.px() + p.w - 2);
        int r0 = map.tile(p.py() + 1), r1 = map.tile(p.py() + p.h - 1);
        for (int r = r0; r <= r1; r++) {
            for (int cc = c0; cc <= c1; cc++) {
                char ch = map.get(cc, r);
                if (ch == 'o') {
                    map.set(cc, r, ' ');
                    coins++;
                    score += 10;
                    Sfx.tone(88, 20);
                    if (coins % 100 == 0) lives++;
                } else if (ch == '^') {
                    die();
                    return;
                } else if (ch == 'F') {
                    score += 250 + level * 50;
                    clearTimer = 30;
                    Sfx.win();
                    return;
                } else if (ch == 'B' && p.vy >= 0) {
                    p.vy = -(ts << 8) * 145 / 100;
                    cut = true;
                    Sfx.tone(84, 40);
                }
            }
        }
    }

    private void enemies() {
        int speed = (ts << 8) / 24 + level * 6;
        for (int i = 0; i < nEn; i++) {
            if (!enAlive[i]) continue;
            Body e = en[i];
            if (Math.abs(e.cx() - p.cx()) > W * 2) continue;
            // turn at walls and ledges
            int ahead = e.vx < 0 ? e.px() - 1 : e.px() + e.w;
            int footRow = map.tile(e.py() + e.h);
            boolean ledge = !map.isSolid(map.tile(ahead), footRow);
            if (e.wallLeft || e.wallRight || (e.ground && ledge)) e.vx = e.vx < 0 ? speed : -speed;
            if (e.vx == 0) e.vx = -speed;
            e.vy += (ts << 8) / 9;
            e.move(map);
            if (e.py() > ROWS * ts) enAlive[i] = false;
            if (!p.overlaps(e)) continue;
            if (p.vy > 0 && p.py() + p.h - e.py() < e.h / 2 + ts / 4) {
                enAlive[i] = false;
                p.vy = -(ts << 8) * 6 / 10;
                score += 50;
                Sfx.tone(60, 40);
            } else if (invuln == 0) {
                die();
                return;
            }
        }
    }

    private void die() {
        if (deadTimer > 0) return;
        Sfx.bad();
        lives--;
        deadTimer = 20;
    }

    private void respawn() {
        if (lives <= 0) {
            endGame(false);
            return;
        }
        p.place(safeX, safeY);
        invuln = 40;
    }

    // ----------------------------------------------------------- drawing

    protected void draw(Graphics g) {
        layout();
        int viewH = H - hud;
        camX = Math.max(0, Math.min(p.cx() - W / 3, endCol * ts - W + ts * 2));
        int mapH = ROWS * ts;
        camY = mapH <= viewH ? -(viewH - mapH) : Math.max(0, Math.min(p.cy() - viewH / 2, mapH - viewH));
        // sky
        for (int i = 0; i < 8; i++) {
            g.setColor(Gfx.mix(0x3A7BD5, 0xA8D8FF, i * 32));
            g.fillRect(0, hud + i * viewH / 8, W, viewH / 8 + 1);
        }
        // parallax hills
        g.setColor(0x7CC47C);
        for (int x = 0; x < W; x += 3) {
            int wx = (x + camX / 3);
            int hh = (gamekit.FMath.sin(wx & 255) * ts >> 10) + (gamekit.FMath.sin((wx * 3) & 255) * ts >> 11) + ts * 3;
            g.fillRect(x, H - hh - ts, 3, hh + ts);
        }
        int oy = hud - camY;
        int c0 = camX / ts, c1 = Math.min(MAXW - 1, (camX + W) / ts);
        for (int r = 0; r < ROWS; r++) {
            int y = oy + r * ts;
            if (y + ts < hud || y > H) continue;
            for (int cc = c0; cc <= c1; cc++) {
                char ch = map.get(cc, r);
                if (ch == ' ') continue;
                int x = cc * ts - camX;
                drawTile(g, ch, x, y, cc, r);
            }
        }
        for (int i = 0; i < nEn; i++) {
            if (!enAlive[i]) continue;
            Body e = en[i];
            int x = e.px() - camX;
            if (x < -ts || x > W) continue;
            g.drawImage(blob, x + e.w / 2, oy + e.py() + e.h, Graphics.HCENTER | Graphics.BOTTOM);
        }
        if (deadTimer == 0 && (invuln == 0 || (clock & 2) == 0)) {
            Image im = p.ground ? hero : heroJump;
            int x = p.px() - camX + p.w / 2, y = oy + p.py() + p.h;
            g.drawRegion(im, 0, 0, im.getWidth(), im.getHeight(), facing < 0 ? 2 : 0, x, y, Graphics.HCENTER | Graphics.BOTTOM);
        } else if (deadTimer > 0) {
            g.setColor(0xFFFFFF);
            Gfx.ring(g, p.cx() - camX, oy + p.cy(), (20 - deadTimer) * ts / 8);
        }
        g.setColor(0x1D2B53);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString("o" + coins, 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString("LV" + (level + 1) + "  " + score, W / 2, 1, Gfx.TC);
        g.setColor(0xFF6B6B);
        g.drawString("x" + lives, W - 2, 1, Gfx.TR);
        if (clearTimer > 0) Gfx.shadowText(g, "LEVEL CLEAR!", W / 2, H / 2 - 8, Gfx.TC, Gfx.fit("LEVEL CLEAR!", W - 8), 0xFFFFFF, 0x000000);
    }

    private void drawTile(Graphics g, char ch, int x, int y, int cc, int r) {
        switch (ch) {
            case '#': {
                boolean top = !map.isSolid(cc, r - 1);
                g.setColor(0x8B5A2B);
                g.fillRect(x, y, ts, ts);
                g.setColor(0x6E4520);
                g.fillRect(x + ts / 4, y + ts / 2, 2, 2);
                g.fillRect(x + ts * 3 / 4, y + ts / 4 + 2, 2, 2);
                if (top) {
                    g.setColor(0x4CAF50);
                    g.fillRect(x, y, ts, Math.max(2, ts / 4));
                    g.setColor(0x81C784);
                    g.fillRect(x, y, ts, 1);
                }
                break;
            }
            case '=':
                Gfx.bevel(g, x, y, ts, Math.max(3, ts / 3), 0xC08040);
                break;
            case '^':
                g.setColor(0xD0D4DC);
                g.fillTriangle(x, y + ts, x + ts / 4, y + ts / 3, x + ts / 2, y + ts);
                g.fillTriangle(x + ts / 2, y + ts, x + ts * 3 / 4, y + ts / 3, x + ts, y + ts);
                break;
            case 'o': {
                int wob = Math.abs(gamekit.FMath.sin((clock * 12 + cc * 30) & 255)) * (ts / 3) >> 10;
                g.setColor(0xFFD54F);
                g.fillArc(x + ts / 2 - wob - 1, y + ts / 4, (wob + 1) * 2, ts / 2, 0, 360);
                g.setColor(0xFFA000);
                g.drawArc(x + ts / 2 - wob - 1, y + ts / 4, (wob + 1) * 2, ts / 2, 0, 360);
                break;
            }
            case 'F':
                g.setColor(0xEEEEEE);
                g.fillRect(x + ts / 4, y - ts * 2, 2, ts * 3);
                g.setColor(0xE53935);
                g.fillTriangle(x + ts / 4 + 2, y - ts * 2, x + ts / 4 + 2, y - ts, x + ts, y - ts * 3 / 2);
                break;
            case 'B':
                g.setColor(0x555555);
                g.fillRect(x + 2, y + ts - 3, ts - 4, 3);
                g.setColor(0xE53935);
                for (int k = 0; k < 3; k++) g.drawLine(x + 3, y + ts - 4 - k * ts / 6, x + ts - 4, y + ts - 4 - k * ts / 6 - 1);
                g.fillRect(x + 1, y + ts / 3, ts - 2, 3);
                break;
            default:
                break;
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        ts = Math.max(8, Math.min(h / 3, 16));
        if (hero == null || spriteTs != ts) buildSprites();
        g.setColor(0x4CAF50);
        g.fillRect(x, y + h - ts / 2, w, ts / 2);
        int t = clock % 40;
        int jump = t < 20 ? t * (20 - t) * ts / 60 : 0;
        int hx = x + (clock * 2) % (w + 20) - 10;
        g.drawImage(t < 20 ? heroJump : hero, hx, y + h - ts / 2 - jump, Graphics.HCENTER | Graphics.BOTTOM);
        g.drawImage(blob, x + w - ((clock * 3 / 2) % (w + 20)) + 10, y + h - ts / 2, Graphics.HCENTER | Graphics.BOTTOM);
    }
}
