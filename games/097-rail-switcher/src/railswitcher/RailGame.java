package railswitcher;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Rail Switcher: the keypad is a signal box. Seven points form a binary
 * tree of track (switch k = (1 << level) + index, so keys 1..7 map to them in
 * reading order); trains must be routed to the station of their colour.
 */
public class RailGame extends Game {
    private static final int LEVELS = 3, MAXT = 10;
    private static final int[] COLS = { 0xEF5350, 0x42A5F5, 0x66BB6A, 0xFFCA28 };

    private final int[] sw = new int[8];
    private final int[] station = new int[8];
    private final int[] tl = new int[MAXT], ti = new int[MAXT], tt = new int[MAXT], tc = new int[MAXT];
    private final int[] tpl = new int[MAXT], tpi = new int[MAXT];
    private final boolean[] ton = new boolean[MAXT];
    private int spawnT, spawnGap, speed, misses, delivered, flashK, flashT, flashOk;

    protected String name() { return "Rail Switcher"; }

    protected String[] help() {
        return new String[] {
            "You run the signal box. Trains enter from the left and must reach the station of their own colour on the right.",
            "Keys 1 to 7 flip the seven sets of points: 1 is the first junction, 2 and 3 the next pair, 4 to 7 the last four. Each points sends trains up or down.",
            "Three wrong deliveries and you are fired. Trains get faster as you go.",
            "- Controls",
            "1-7: flip points",
        };
    }

    protected String[] modes() { return new String[] { "Branch line", "Main line" }; }

    protected int accent() { return 0x66BB6A; }

    protected void newGame() {
        for (int k = 0; k < 8; k++) {
            sw[k] = 0;
            station[k] = k / 2;
        }
        Rnd.shuffle(station);
        for (int i = 0; i < MAXT; i++) ton[i] = false;
        spawnT = 10;
        spawnGap = mode == 0 ? 80 : 60;
        speed = mode == 0 ? 6 : 8;
        misses = 0;
        delivered = 0;
    }

    private int nodeX(int l) {
        return W * (l + 1) / (LEVELS + 2);
    }

    private int top() { return Gfx.SMALL.getHeight() + 4; }

    private int nodeY(int l, int i) {
        int h = H - top() - Gfx.SMALL.getHeight() - 4;
        return top() + (2 * i + 1) * h / (2 << l);
    }

    protected void update() {
        if (flashT > 0) flashT--;
        int d = digitPressed();
        if (d >= 1 && d <= 7) {
            sw[d] ^= 1;
            Sfx.click();
        }
        if (--spawnT <= 0) {
            for (int i = 0; i < MAXT; i++) {
                if (ton[i]) continue;
                ton[i] = true;
                tl[i] = 0;
                ti[i] = 0;
                tpl[i] = -1;
                tpi[i] = 0;
                tt[i] = 0;
                tc[i] = Rnd.nextInt(4);
                break;
            }
            spawnT = spawnGap;
        }
        for (int i = 0; i < MAXT; i++) {
            if (!ton[i]) continue;
            tt[i] += speed;
            if (tt[i] < 256) continue;
            tt[i] = 0;
            if (tl[i] == LEVELS) {
                arrive(i);
                continue;
            }
            int key = (1 << tl[i]) + ti[i];
            tpl[i] = tl[i];
            tpi[i] = ti[i];
            ti[i] = ti[i] * 2 + sw[key];
            tl[i]++;
        }
    }

    private void arrive(int i) {
        ton[i] = false;
        flashK = ti[i];
        flashT = 10;
        if (station[ti[i]] == tc[i]) {
            delivered++;
            score += 10 + delivered;
            flashOk = 1;
            Sfx.good();
            if (delivered % 5 == 0) {
                speed++;
                spawnGap = Math.max(18, spawnGap - 6);
            }
        } else {
            misses++;
            flashOk = 0;
            Sfx.bad();
            if (misses >= 3) {
                headline = delivered + " TRAINS DELIVERED";
                endGame(false);
            }
        }
    }

    private int px(int l) {
        return l < 0 ? 0 : (l > LEVELS ? W - 6 : nodeX(l));
    }

    protected void draw(Graphics g) {
        g.setColor(0x263238);
        g.fillRect(0, 0, W, H);
        int stX = nodeX(LEVELS);
        // track
        g.setColor(0x8D6E63);
        g.drawLine(0, nodeY(0, 0), nodeX(0), nodeY(0, 0));
        for (int l = 0; l < LEVELS; l++) {
            for (int i = 0; i < (1 << l); i++) {
                int key = (1 << l) + i;
                for (int c = 0; c < 2; c++) {
                    boolean active = sw[key] == c;
                    g.setColor(active ? 0xD7CCC8 : 0x4E342E);
                    g.drawLine(nodeX(l), nodeY(l, i), nodeX(l + 1), nodeY(l + 1, i * 2 + c));
                    if (active) g.drawLine(nodeX(l), nodeY(l, i) + 1, nodeX(l + 1), nodeY(l + 1, i * 2 + c) + 1);
                }
            }
        }
        // points labels
        for (int l = 0; l < LEVELS; l++) for (int i = 0; i < (1 << l); i++) {
            int key = (1 << l) + i;
            int x = nodeX(l), y = nodeY(l, i);
            Gfx.panel(g, x - 5, y - 5, 11, 11, 0x37474F, 0xFFFFFF);
            Gfx.text(g, String.valueOf(key), x, y - Gfx.SMALL_B.getHeight() / 2, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF);
        }
        // stations
        int sh = Math.max(4, (H - top()) / 20);
        for (int k = 0; k < 8; k++) {
            int y = nodeY(LEVELS, k);
            g.setColor(COLS[station[k]]);
            g.fillRect(stX, y - sh, W - stX - 2, sh * 2);
            if (flashT > 0 && flashK == k) {
                g.setColor(flashOk == 1 ? 0xFFFFFF : 0x000000);
                g.drawRect(stX - 1, y - sh - 1, W - stX, sh * 2 + 1);
            }
        }
        // trains
        for (int i = 0; i < MAXT; i++) {
            if (!ton[i]) continue;
            int x0, y0;
            if (tpl[i] < 0) {
                x0 = 0;
                y0 = nodeY(0, 0);
            } else {
                x0 = nodeX(tpl[i]);
                y0 = nodeY(tpl[i], tpi[i]);
            }
            int x1 = px(tl[i]), y1 = nodeY(tl[i], ti[i]);
            int x = x0 + (x1 - x0) * tt[i] / 256, y = y0 + (y1 - y0) * tt[i] / 256;
            g.setColor(0x000000);
            g.fillRect(x - 4, y - 3, 9, 7);
            g.setColor(COLS[tc[i]]);
            g.fillRect(x - 3, y - 2, 7, 5);
        }
        g.setColor(0x000000);
        g.fillRect(0, 0, W, Gfx.SMALL.getHeight() + 2);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString("Delivered " + delivered, 2, 1, Gfx.TL);
        g.setColor(0xFF8A80);
        String m = "";
        for (int k = 0; k < 3; k++) m += k < misses ? "X" : "-";
        g.drawString(m, W - 2, 1, Gfx.TR);
        Gfx.text(g, "keys 1-7 flip points", W / 2, H - Gfx.SMALL.getHeight() - 1, Gfx.TC, Gfx.SMALL, 0x90A4AE);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int x0 = x + w / 6, x1 = x + w / 2, x2 = x + w * 5 / 6, ym = y + h / 2;
        int up = (clock / 20) % 2;
        g.setColor(up == 0 ? 0xD7CCC8 : 0x4E342E);
        g.drawLine(x1, ym, x2, y + 4);
        g.setColor(up == 1 ? 0xD7CCC8 : 0x4E342E);
        g.drawLine(x1, ym, x2, y + h - 4);
        g.setColor(0xD7CCC8);
        g.drawLine(x0, ym, x1, ym);
        g.setColor(COLS[0]);
        g.fillRect(x2, y + 1, 8, 6);
        g.setColor(COLS[1]);
        g.fillRect(x2, y + h - 7, 8, 6);
        int t = clock % 20;
        g.setColor(COLS[up]);
        g.fillRect(x0 + t * (x1 - x0) / 20 - 3, ym - 2, 7, 5);
    }
}
