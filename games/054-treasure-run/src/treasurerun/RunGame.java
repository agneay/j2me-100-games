package treasurerun;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Treasure Run: an endless auto-runner through temple ruins. Jump (twice in
 * the air) over pits and crates, slide under beams, grab gems.
 */
public class RunGame extends Game {
    private static final int SEG = 48, MAXO = 24;
    // obstacle kinds: 0 pit, 1 crate, 2 beam (slide under), 3 gem
    private final int[] ox = new int[MAXO], ok = new int[MAXO], oy = new int[MAXO];
    private final boolean[] oon = new boolean[MAXO];
    private int worldX, speed, nextSpawn, groundY, unit;
    private int py, vy, jumps, slideT, gems, deadT, shield;
    private Image runA, runB, slideImg;
    private int imgUnit;

    protected String name() { return "Treasure Run"; }

    protected String[] help() {
        return new String[] {
            "Your explorer runs on their own through endless temple ruins - and keeps getting faster.",
            "Jump over pits and crates (you can jump again in mid-air), and slide under low stone beams. Gems are worth 10 points; the shield orb survives one hit.",
            "- Controls",
            "2 or 5: jump (press again to double jump)",
            "8: slide",
        };
    }

    protected int accent() { return 0xFF9F1C; }

    protected void newGame() {
        layout();
        for (int i = 0; i < MAXO; i++) oon[i] = false;
        worldX = 0;
        speed = unit * 256 / 7;
        nextSpawn = W + unit * 4;
        py = groundY << 8;
        vy = 0;
        jumps = 0;
        slideT = 0;
        gems = 0;
        deadT = 0;
        shield = 0;
    }

    private void layout() {
        unit = Math.max(8, Math.min(W / 12, H / 10));
        groundY = H * 3 / 4;
        if (runA == null || imgUnit != unit) {
            imgUnit = unit;
            int sc = Math.max(1, unit / 8);
            int[] pal = { 0, 0x3E2723, 0xFFCC80, 0xC62828, 0x6D4C41, 0xFFE082 };
            runA = Gfx.sprite(new String[] { "..444...", ".44444..", "..222...", "..2.2...", ".3333...", "2.333.2.", "..3.3...", ".1...1.." }, pal, sc);
            runB = Gfx.sprite(new String[] { "..444...", ".44444..", "..222...", "..2.2...", ".3333...", ".2333.2.", "..33....", "..1.1..." }, pal, sc);
            slideImg = Gfx.sprite(new String[] { "........", "........", "........", "........", "..444...", ".4422333", ".3333311", "........" }, pal, sc);
        }
    }

    private void spawn(int x) {
        int kind;
        int r = Rnd.nextInt(100);
        if (r < 30) kind = 0;
        else if (r < 60) kind = 1;
        else if (r < 80) kind = 2;
        else kind = 3;
        add(x, kind, 0);
        if (kind != 3 && Rnd.chance(50)) {
            // gems arcing over obstacles
            for (int k = 0; k < 3; k++) add(x - unit + k * unit, 3, unit * (k == 1 ? 3 : 2));
        }
        if (Rnd.chance(6)) add(x + unit * 2, 4, unit * 2);
    }

    private void add(int x, int kind, int height) {
        for (int i = 0; i < MAXO; i++) {
            if (oon[i]) continue;
            oon[i] = true;
            ox[i] = x;
            ok[i] = kind;
            oy[i] = height;
            return;
        }
    }

    protected void update() {
        layout();
        if (deadT > 0) {
            if (--deadT == 0) endGame(false);
            return;
        }
        speed += 2;
        int move = speed >> 8;
        worldX += move;
        score = worldX / unit + gems * 10;
        boolean grounded = py >= (groundY << 8) && !overPit();
        if ((tapped & (K_UP | K_FIRE)) != 0 && (grounded || jumps < 2)) {
            vy = -(unit << 8) * 55 / 100;
            jumps = grounded ? 1 : jumps + 1;
            slideT = 0;
            Sfx.tone(72 + jumps * 4, 25);
        }
        if ((pressed & K_DOWN) != 0 && grounded) {
            slideT = 14;
            Sfx.tone(50, 20);
        }
        if (slideT > 0) slideT--;
        vy += (unit << 8) / 22;
        py += vy;
        if (py >= (groundY << 8) && !overPit()) {
            py = groundY << 8;
            vy = 0;
            jumps = 0;
        }
        if ((py >> 8) > H + unit) {
            die("FELL IN A PIT");
            return;
        }
        int pxl = W / 4;
        for (int i = 0; i < MAXO; i++) {
            if (!oon[i]) continue;
            ox[i] -= move;
            if (ox[i] < -unit * 3) {
                oon[i] = false;
                continue;
            }
            int top = (py >> 8) - (slideT > 0 ? unit / 2 : unit);
            int bottom = py >> 8;
            int x0 = ox[i], x1 = ox[i] + (ok[i] == 0 ? unit * 2 : unit);
            boolean xHit = pxl + unit / 2 > x0 + 2 && pxl - unit / 2 < x1 - 2;
            if (!xHit) continue;
            switch (ok[i]) {
                case 1: // crate on the ground
                    if (bottom > groundY - unit + 2) hit(i);
                    break;
                case 2: // beam on a pillar from the ceiling: only sliding gets under it
                    if (top < groundY - unit * 4 / 5) hit(i);
                    break;
                case 3: {
                    int gy = groundY - unit / 2 - oy[i];
                    if (gy > top - unit / 2 && gy < bottom + unit / 2) {
                        oon[i] = false;
                        gems++;
                        Sfx.tone(88, 20);
                    }
                    break;
                }
                case 4: {
                    int gy = groundY - unit / 2 - oy[i];
                    if (gy > top - unit && gy < bottom + unit / 2) {
                        oon[i] = false;
                        shield = 1;
                        Sfx.good();
                    }
                    break;
                }
                default:
                    break;
            }
            if (state != PLAY || deadT > 0) return;
        }
        nextSpawn -= move;
        if (nextSpawn <= W) {
            spawn(W + unit);
            int gapMin = Math.max(unit * 4, unit * 9 - worldX / 400);
            nextSpawn = W + unit + gapMin + Rnd.nextInt(unit * 4);
        }
    }

    private boolean overPit() {
        int pxl = W / 4;
        for (int i = 0; i < MAXO; i++) {
            if (oon[i] && ok[i] == 0 && pxl - unit / 4 > ox[i] && pxl + unit / 4 < ox[i] + unit * 2) return true;
        }
        return false;
    }

    private void hit(int i) {
        if (shield > 0) {
            shield = 0;
            oon[i] = false;
            Sfx.tone(60, 60);
            return;
        }
        die(ok[i] == 2 ? "BONK!" : "TRIPPED!");
    }

    private void die(String why) {
        headline = why;
        deadT = 20;
        Sfx.bad();
    }

    protected void draw(Graphics g) {
        layout();
        for (int i = 0; i < 6; i++) {
            g.setColor(Gfx.mix(0xFFB74D, 0xFFE0B2, i * 40));
            g.fillRect(0, i * groundY / 6, W, groundY / 6 + 1);
        }
        // parallax ruins
        g.setColor(0xD7A86E);
        int off = (worldX / 4) % (unit * 6);
        for (int x = -off; x < W; x += unit * 6) {
            g.fillRect(x, groundY - unit * 4, unit, unit * 4);
            g.fillRect(x + unit * 3, groundY - unit * 3, unit, unit * 3);
            g.fillRect(x - unit / 3, groundY - unit * 4 - 3, unit * 5 / 3, 3);
        }
        g.setColor(0x8D6E63);
        g.fillRect(0, groundY, W, H - groundY);
        g.setColor(0x6D4C41);
        int toff = worldX % unit;
        for (int x = -toff; x < W; x += unit) g.drawLine(x, groundY, x, H);
        g.setColor(0xA1887F);
        g.fillRect(0, groundY, W, 2);
        for (int i = 0; i < MAXO; i++) {
            if (!oon[i]) continue;
            int x = ox[i];
            switch (ok[i]) {
                case 0:
                    g.setColor(0x1B1B1B);
                    g.fillRect(x, groundY, unit * 2, H - groundY);
                    break;
                case 1:
                    Gfx.bevel(g, x, groundY - unit, unit, unit, 0xA1662F);
                    g.setColor(0x6D3D14);
                    g.drawLine(x + 1, groundY - unit + 1, x + unit - 2, groundY - 2);
                    break;
                case 2:
                    Gfx.bevel(g, x - unit / 2, groundY - unit * 2, unit * 2, unit * 6 / 5, 0x9E9E9E);
                    g.setColor(0x757575);
                    g.fillRect(x, 0, unit, groundY - unit * 2);
                    break;
                case 3: {
                    int gy = groundY - unit / 2 - oy[i];
                    g.setColor(0x00E5FF);
                    g.fillTriangle(x + unit / 2, gy - unit / 3, x + unit / 4, gy, x + unit * 3 / 4, gy);
                    g.fillTriangle(x + unit / 2, gy + unit / 3, x + unit / 4, gy, x + unit * 3 / 4, gy);
                    break;
                }
                default: {
                    int gy = groundY - unit / 2 - oy[i];
                    g.setColor((clock & 4) == 0 ? 0x7C4DFF : 0xB388FF);
                    Gfx.disc(g, x + unit / 2, gy, unit / 3);
                    break;
                }
            }
        }
        int pxl = W / 4, y = py >> 8;
        Image im = slideT > 0 ? slideImg : (((worldX / unit) & 1) == 0 ? runA : runB);
        if (deadT == 0 || (clock & 2) == 0) g.drawImage(im, pxl, y, Graphics.HCENTER | Graphics.BOTTOM);
        if (shield > 0) {
            g.setColor(0xB388FF);
            Gfx.ring(g, pxl, y - unit / 2, unit * 2 / 3);
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x3E2723);
        g.drawString(score + "", 2, 1, Gfx.TL);
        g.setColor(0x00838F);
        g.drawString("gems " + gems, W - 2, 1, Gfx.TR);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        unit = Math.max(8, h / 3);
        if (runA == null || imgUnit != unit) {
            imgUnit = -1;
            layout();
        }
        g.setColor(0x8D6E63);
        g.fillRect(x, y + h - 4, w, 4);
        int t = clock % 30;
        int jump = t < 15 ? t * (15 - t) * h / 120 : 0;
        g.drawImage((clock / 3) % 2 == 0 ? runA : runB, x + w / 3, y + h - 4 - jump, Graphics.HCENTER | Graphics.BOTTOM);
        int cx = x + w - (clock * 3) % (w + 20);
        Gfx.bevel(g, cx, y + h - 4 - unit / 2, unit / 2, unit / 2, 0xA1662F);
    }
}
