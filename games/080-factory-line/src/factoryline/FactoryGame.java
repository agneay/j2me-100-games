package factoryline;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Factory Line: parts ride a conveyor past three chutes. Open the right
 * chute (keys 1-3) as each part passes so it drops into the bin of its
 * colour; grey scrap should ride to the end of the belt.
 */
public class FactoryGame extends Game {
    private static final int MAXI = 12;
    private static final int[] COL = { 0xE53935, 0x43A047, 0x1E88E5, 0x9E9E9E };
    private final int[] ix = new int[MAXI], ic = new int[MAXI], ishape = new int[MAXI], idrop = new int[MAXI], ibin = new int[MAXI];
    private final boolean[] ion = new boolean[MAXI];
    private final boolean[] gate = new boolean[3];
    private int speed, spawnT, strikes, sorted, beltY, chuteW, combo, flashBin = -1, flashT;
    private boolean flashBad;

    protected String name() { return "Factory Line"; }

    protected String[] help() {
        return new String[] {
            "Parts roll along the conveyor belt. Each chute below drops into a bin: red, green, blue from left to right. Grey scrap must ride all the way to the end.",
            "Press 1, 2 or 3 to open or close that chute. A part over an open chute falls in. Wrong bin? That's a strike - three strikes and the line stops.",
            "The belt speeds up as you go.",
            "- Controls",
            "1 / 2 / 3: open or close chute",
        };
    }

    protected int accent() { return 0xFFB300; }

    protected void newGame() {
        for (int i = 0; i < MAXI; i++) ion[i] = false;
        for (int k = 0; k < 3; k++) gate[k] = false;
        speed = 256 + 64;
        spawnT = 10;
        strikes = 0;
        sorted = 0;
        combo = 0;
    }

    private int chuteX(int k) { return W * (k * 2 + 2) / 9; }

    protected void update() {
        int hud = Gfx.SMALL.getHeight() + 2;
        beltY = hud + (H - hud) / 3;
        chuteW = Math.max(10, W / 9);
        if (flashT > 0) flashT--;
        for (int k = 0; k < 3; k++) if (digit(k + 1)) { gate[k] = !gate[k]; Sfx.click(); }
        if (--spawnT <= 0) {
            for (int i = 0; i < MAXI; i++) {
                if (ion[i]) continue;
                ion[i] = true;
                ix[i] = -8 << 8;
                ic[i] = Rnd.chance(20) ? 3 : Rnd.nextInt(3);
                ishape[i] = Rnd.nextInt(3);
                idrop[i] = 0;
                ibin[i] = -1;
                break;
            }
            spawnT = Math.max(12, 45 - sorted / 2) + Rnd.nextInt(20);
        }
        for (int i = 0; i < MAXI; i++) {
            if (!ion[i]) continue;
            if (idrop[i] > 0) {
                idrop[i] += 3;
                if (idrop[i] > (H - beltY) / 2) { land(i, ibin[i]); ion[i] = false; }
                continue;
            }
            ix[i] += speed;
            int x = ix[i] >> 8;
            for (int k = 0; k < 3; k++) {
                if (gate[k] && Math.abs(x - chuteX(k)) < chuteW / 3) {
                    idrop[i] = 1;
                    ibin[i] = k;
                    ix[i] = chuteX(k) << 8;
                }
            }
            if (x > W + 6) {
                land(i, 3);
                ion[i] = false;
            }
        }
        speed = 256 + 64 + sorted * 6;
    }

    private void land(int i, int bin) {
        flashBin = bin;
        flashT = 10;
        if (ic[i] == bin) {
            sorted++;
            combo++;
            score += 10 + Math.min(combo, 20);
            flashBad = false;
            Sfx.tone(76 + bin * 3, 20);
        } else {
            strikes++;
            combo = 0;
            flashBad = true;
            Sfx.bad();
            if (strikes >= 3) {
                headline = "LINE STOPPED";
                endGame(false);
            }
        }
    }

    private void part(Graphics g, int shape, int c, int x, int y, int s) {
        g.setColor(c);
        if (shape == 0) Gfx.disc(g, x, y, s / 2);
        else if (shape == 1) g.fillRect(x - s / 2, y - s / 2, s, s);
        else g.fillTriangle(x, y - s / 2, x - s / 2, y + s / 2, x + s / 2, y + s / 2);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        beltY = hud + (H - hud) / 3;
        chuteW = Math.max(10, W / 9);
        g.setColor(0x263238);
        g.fillRect(0, 0, W, H);
        int s = Math.max(6, W / 18);
        // belt
        g.setColor(0x424242);
        g.fillRect(0, beltY, W, s / 2 + 2);
        g.setColor(0x616161);
        int off = (frame * (speed >> 8)) % 8;
        for (int x = -off; x < W; x += 8) g.fillRect(x, beltY + 1, 3, s / 2);
        // chutes and bins
        int binY = H - s * 2 - 4;
        for (int k = 0; k < 4; k++) {
            int x = k < 3 ? chuteX(k) : W - chuteW / 2 - 2;
            int c = COL[k];
            g.setColor(flashBin == k && flashT > 0 ? (flashBad ? 0xFF1744 : 0xFFFFFF) : Gfx.shade(c, -30));
            g.fillRect(x - chuteW / 2, binY, chuteW, s * 2);
            g.setColor(c);
            g.drawRect(x - chuteW / 2, binY, chuteW - 1, s * 2 - 1);
            if (k < 3) {
                g.setColor(0x546E7A);
                g.fillRect(x - 1, beltY + s / 2 + 2, 1, binY - beltY - s / 2 - 2);
                g.fillRect(x + chuteW / 2 - 2, beltY + s / 2 + 2, 1, binY - beltY - s / 2 - 2);
                g.fillRect(x - chuteW / 2 + 1, beltY + s / 2 + 2, 1, binY - beltY - s / 2 - 2);
                g.setColor(gate[k] ? 0x76FF03 : 0xFF5252);
                g.fillRect(x - chuteW / 2, beltY + s / 2 + 2, chuteW, 3);
                g.setFont(Gfx.SMALL_B);
                g.setColor(0xFFFFFF);
                g.drawString(String.valueOf(k + 1), x, binY + s / 2, Gfx.TC);
            }
        }
        for (int i = 0; i < MAXI; i++) {
            if (!ion[i]) continue;
            part(g, ishape[i], COL[ic[i]], ix[i] >> 8, beltY - s / 2 + idrop[i] * 2, s);
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFB300);
        g.drawString(String.valueOf(score), 2, 1, Gfx.TL);
        g.setColor(0xFF5252);
        for (int k = 0; k < 3; k++) {
            if (k < strikes) g.fillRect(W - 8 - k * 8, 3, 6, 6);
            else g.drawRect(W - 8 - k * 8, 3, 5, 5);
        }
        g.setFont(Gfx.SMALL);
        g.setColor(0xB0BEC5);
        g.drawString("sorted " + sorted, W / 2, 1, Gfx.TC);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x424242);
        g.fillRect(x, y + h / 2, w, 5);
        for (int k = 0; k < 4; k++) {
            int px = x + ((clock * 2 + k * w / 4) % w);
            part(g, k % 3, COL[k], px, y + h / 2 - 4, 7);
        }
    }
}
