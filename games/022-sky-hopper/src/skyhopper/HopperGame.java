package skyhopper;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Sky Hopper: bounce ever upward from platform to platform. Don't fall. */
public class HopperGame extends Game {
    private static final int MAXP = 24;
    private static final int NORMAL = 0, MOVING = 1, CRUMBLE = 2, SPRING = 3, CLOUD = 4;
    private static final int[] PCOLOR = { 0x66BB6A, 0x42A5F5, 0xA1887F, 0x66BB6A, 0xECEFF1 };

    private final int[] plx = new int[MAXP], ply = new int[MAXP], plt = new int[MAXP], plv = new int[MAXP];
    private final boolean[] plOn = new boolean[MAXP], star = new boolean[MAXP];
    private final int[] crumble = new int[MAXP];
    private int x, y, vx, vy;      // player, <<8, world y grows downward (negative = higher)
    private int camY, topY, maxH, pw, ph, platW, platH, facing = 1, squash;
    private Image body;
    private int bodyScale;

    protected String name() { return "Sky Hopper"; }

    protected String[] help() {
        return new String[] {
            "Your hopper bounces automatically whenever it lands on a platform. Steer left and right to land on the next one and climb as high as you can.",
            "Blue platforms slide, brown ones crumble under you, white clouds vanish after one bounce and red springs launch you sky-high. Grab stars for bonus points.",
            "Leave one side of the screen to wrap around to the other.",
            "- Controls",
            "4/6: steer left / right",
        };
    }

    protected int accent() { return 0x4FC3F7; }

    private void layout() {
        pw = Math.max(8, W / 14);
        ph = pw * 5 / 4;
        platW = Math.max(18, W / 5);
        platH = Math.max(3, W / 40);
        int sc = Math.max(1, pw / 8);
        if (body == null || bodyScale != sc) {
            bodyScale = sc;
            body = Gfx.sprite(new String[] { "..1111..", ".122221.", "12322321", "12222221", "12444421", ".122221.", "..1..1..", ".11..11." },
                    new int[] { 0, 0x263238, 0xFFB74D, 0xFFFFFF, 0xE65100 }, sc);
        }
    }

    protected void newGame() {
        layout();
        for (int i = 0; i < MAXP; i++) plOn[i] = false;
        x = (W / 2) << 8;
        y = (H - ph - 10) << 8;
        vx = 0;
        vy = -jumpV();
        camY = 0;
        maxH = 0;
        // ground platform plus a starting ladder
        add(W / 2 - platW / 2, H - 8, NORMAL);
        topY = H - 8;
        while (topY > -H) spawnNext();
    }

    private int jumpV() { return (H << 8) / 20; }

    private int gravity() { return (H << 8) / 560; }

    private void add(int px, int py, int t) {
        for (int i = 0; i < MAXP; i++) {
            if (plOn[i]) continue;
            plOn[i] = true;
            plx[i] = px << 8;
            ply[i] = py;
            plt[i] = t;
            plv[i] = t == MOVING ? (Rnd.chance(50) ? 1 : -1) * ((W << 8) / 160 + maxH / 40) : 0;
            crumble[i] = 0;
            star[i] = Rnd.chance(12) && t != CRUMBLE;
            return;
        }
    }

    private void spawnNext() {
        int height = -topY;
        // max jump height is v^2 / 2g = H * 560 / 800 = 0.7 H; keep gaps below that
        int gapMax = Math.min(H * 55 / 100, H / 6 + height / 50);
        int gap = Rnd.range(H / 9, Math.max(H / 9 + 1, gapMax));
        topY -= gap;
        int t = NORMAL;
        int r = Rnd.nextInt(100);
        int diff = Math.min(60, height / 60);
        if (r < diff / 2) t = MOVING;
        else if (r < diff) t = CRUMBLE;
        else if (r < diff + 8) t = SPRING;
        else if (r < diff + 18) t = CLOUD;
        add(Rnd.nextInt(W - platW), topY, t);
        // a crumbling platform is never the only way up
        if (t == CRUMBLE) add(Rnd.nextInt(W - platW), topY - Rnd.range(H / 14, H / 8), NORMAL);
    }

    protected void update() {
        layout();
        int acc = (W << 8) / 300, max = (W << 8) / 45;
        if ((held & K_LEFT) != 0) { vx -= acc; facing = -1; }
        else if ((held & K_RIGHT) != 0) { vx += acc; facing = 1; }
        else vx = vx * 7 / 8;
        if (vx > max) vx = max;
        if (vx < -max) vx = -max;
        x += vx;
        if ((x >> 8) < -pw / 2) x += W << 8;
        if ((x >> 8) > W - pw / 2) x -= W << 8;
        int prevBottom = (y >> 8) + ph;
        vy += gravity();
        y += vy;
        if (squash > 0) squash--;
        if (vy > 0) {
            int bottom = (y >> 8) + ph;
            for (int i = 0; i < MAXP; i++) {
                if (!plOn[i] || crumble[i] > 0) continue;
                int px = plx[i] >> 8;
                if (prevBottom <= ply[i] && bottom >= ply[i] && (x >> 8) + pw > px + 2 && (x >> 8) < px + platW - 2) {
                    land(i);
                    break;
                }
            }
        }
        for (int i = 0; i < MAXP; i++) {
            if (!plOn[i]) continue;
            if (plt[i] == MOVING) {
                plx[i] += plv[i];
                if ((plx[i] >> 8) < 0 || (plx[i] >> 8) > W - platW) plv[i] = -plv[i];
            }
            if (crumble[i] > 0 && ++crumble[i] > 12) plOn[i] = false;
            if (star[i] && Math.abs((x >> 8) + pw / 2 - ((plx[i] >> 8) + platW / 2)) < pw
                    && Math.abs((y >> 8) + ph / 2 - (ply[i] - pw)) < pw) {
                star[i] = false;
                score += 50;
                Sfx.tone(90, 30);
            }
            if (ply[i] - camY > H + 20) plOn[i] = false;
        }
        // camera follows upward only
        int screenY = (y >> 8) - camY;
        if (screenY < H / 3) camY = (y >> 8) - H / 3;
        int height = (H - 10 - (y >> 8));
        if (height > maxH) maxH = height;
        while (topY > camY - H / 2) spawnNext();
        score = Math.max(score, maxH / 4);
        if ((y >> 8) - camY > H + ph) {
            Sfx.lose();
            headline = "FELL!";
            endGame(false);
        }
    }

    private void land(int i) {
        squash = 4;
        switch (plt[i]) {
            case CRUMBLE:
                crumble[i] = 1;
                Sfx.tone(45, 40);
                return;
            case SPRING:
                vy = -jumpV() * 17 / 10;
                Sfx.tone(88, 50);
                return;
            case CLOUD:
                plOn[i] = false;
                break;
            default:
                break;
        }
        vy = -jumpV();
        Sfx.tone(70, 20);
    }

    protected void draw(Graphics g) {
        layout();
        int band = Math.min(255, maxH / 20);
        int sky = Gfx.mix(0x81D4FA, 0x0D1B3E, band);
        g.setColor(sky);
        g.fillRect(0, 0, W, H);
        if (band > 120) {
            g.setColor(0xFFFFFF);
            for (int k = 0; k < 25; k++) g.fillRect((k * 67 + 5) % W, ((k * 113) - camY / 6) % H + (((k * 113) - camY / 6) % H < 0 ? H : 0), 1, 1);
        }
        // far clouds parallax
        g.setColor(Gfx.mix(sky, 0xFFFFFF, 60));
        for (int k = 0; k < 4; k++) {
            int cy = ((k * H / 4) - camY / 4) % H;
            if (cy < 0) cy += H;
            g.fillRoundRect((k * 53) % W - 10, cy, W / 3, H / 18, 10, 10);
        }
        for (int i = 0; i < MAXP; i++) {
            if (!plOn[i]) continue;
            int px = plx[i] >> 8, py = ply[i] - camY;
            if (py < -10 || py > H + 10) continue;
            int c = PCOLOR[plt[i]];
            if (crumble[i] > 0) {
                g.setColor(c);
                g.fillRect(px, py + crumble[i], platW / 2 - 1, platH);
                g.fillRect(px + platW / 2 + 1, py + crumble[i] * 2, platW / 2 - 1, platH);
                continue;
            }
            if (plt[i] == CLOUD) {
                g.setColor(c);
                g.fillRoundRect(px, py - 1, platW, platH + 2, 8, 8);
            } else {
                Gfx.bevel(g, px, py, platW, platH, c);
                if (plt[i] == CRUMBLE) {
                    g.setColor(0x5D4037);
                    g.drawLine(px + platW / 3, py, px + platW / 3 + 2, py + platH);
                    g.drawLine(px + platW * 2 / 3, py, px + platW * 2 / 3 - 2, py + platH);
                }
            }
            if (plt[i] == SPRING) {
                g.setColor(0xE53935);
                g.fillRect(px + platW / 2 - 3, py - 4, 7, 4);
                g.setColor(0xBDBDBD);
                g.drawLine(px + platW / 2 - 2, py - 1, px + platW / 2 + 2, py - 3);
            }
            if (star[i]) {
                int sx = px + platW / 2, sy = py - pw;
                g.setColor(0xFFEB3B);
                g.fillTriangle(sx, sy - 4, sx - 4, sy + 3, sx + 4, sy + 3);
                g.fillTriangle(sx, sy + 5, sx - 4, sy - 1, sx + 4, sy - 1);
            }
        }
        int sx = x >> 8, sy = (y >> 8) - camY;
        g.drawRegion(body, 0, 0, body.getWidth(), body.getHeight(), facing < 0 ? 2 : 0, sx + pw / 2, sy + ph + (squash > 0 ? 1 : 0), Graphics.HCENTER | Graphics.BOTTOM);
        if (sx < pw) g.drawRegion(body, 0, 0, body.getWidth(), body.getHeight(), facing < 0 ? 2 : 0, sx + W + pw / 2, sy + ph, Graphics.HCENTER | Graphics.BOTTOM);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x000000);
        g.drawString(score + " m", W / 2 + 1, 2, Gfx.TC);
        g.setColor(0xFFFFFF);
        g.drawString(score + " m", W / 2, 1, Gfx.TC);
    }

    protected void drawTitleArt(Graphics g, int x0, int y0, int w, int h) {
        layout();
        g.setColor(0x81D4FA);
        g.fillRect(x0, y0, w, h);
        int pwid = Math.max(18, w / 4);
        for (int k = 0; k < 3; k++) Gfx.bevel(g, x0 + (k * w / 3) + 4, y0 + h - 6 - k * h / 4, pwid, 4, PCOLOR[k]);
        int t = clock % 24;
        int hop = t * (24 - t) * h / 300;
        g.drawImage(body, x0 + w / 2, y0 + h - 8 - hop, Graphics.HCENTER | Graphics.BOTTOM);
    }
}
