package fruitcatcher;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Fruit Catcher: catch falling fruit in your basket, dodge the bombs. */
public class CatcherGame extends Game {
    private static final int MAXF = 12;
    // kinds: 0 apple 1 banana 2 grapes 3 melon 4 golden star 5 bomb 6 magnet 7 clock
    private static final int[] PTS = { 10, 15, 20, 30, 100, 0, 0, 0 };
    private static final String[][] ART = {
        { "....1...", "...1....", ".22.22..", "2222222.", "2322222.", "2222222.", ".22222..", "..222..." },
        { "......1.", ".....22.", "....222.", "...222..", "..222...", ".222....", "222.....", "22......" },
        { "...1....", "..222...", ".22222..", ".22222..", "..222...", "..222...", "...2....", "........" },
        { "..1111..", ".122221.", "12322321", "12222221", "12322321", "12222221", ".122221.", "..1111.." },
        { "...22...", "...22...", "22222222", ".222222.", "..2222..", ".222222.", ".22..22.", "2......2" },
        { "....3...", "...3....", "..1111..", ".111111.", "11211111", "11111111", ".111111.", "..1111.." },
        { "11....11", "22....22", "22....22", "22....22", "22....22", ".222222.", "..2222..", "........" },
        { "..1111..", ".122221.", "12222321", "12223221", "12232221", "12222221", ".122221.", "..1111.." },
    };
    private static final int[][] PAL = {
        { 0, 0x5D4037, 0xE53935, 0xFFCDD2 }, { 0, 0x5D4037, 0xFFEB3B, 0 }, { 0, 0x2E7D32, 0x8E24AA, 0 },
        { 0, 0x1B5E20, 0x43A047, 0x1B5E20 }, { 0, 0, 0xFFD600, 0 }, { 0, 0x212121, 0xFFFFFF, 0xFF6D00 },
        { 0, 0xB0BEC5, 0xE53935, 0 }, { 0, 0x37474F, 0xECEFF1, 0x37474F },
    };

    private final int[] fx = new int[MAXF], fy = new int[MAXF], fv = new int[MAXF], fk = new int[MAXF];
    private final boolean[] fon = new boolean[MAXF];
    private int bx, bw, lives, drops, combo, spawnT, level, magnet, slow, popT, popX, popY;
    private String pop = "";
    private Image[] img;
    private int imgScale;

    protected String name() { return "Fruit Catcher"; }

    protected String[] help() {
        return new String[] {
            "Move the basket to catch the falling fruit. Golden stars are worth 100. Catch fruit in a row to build a combo multiplier.",
            "Avoid the bombs: each one costs a heart. Letting 10 pieces of fruit hit the ground also ends the game.",
            "Magnets make your basket wider; clocks slow everything down for a while.",
            "- Controls",
            "4/6: move basket (hold)",
        };
    }

    protected int accent() { return 0xFFB300; }

    protected void newGame() {
        for (int i = 0; i < MAXF; i++) fon[i] = false;
        bx = (W / 2) << 8;
        lives = 3;
        drops = 0;
        combo = 0;
        level = 0;
        magnet = slow = 0;
        spawnT = 10;
    }

    private void sprites() {
        int sc = Math.max(1, W / 70);
        if (img != null && imgScale == sc) return;
        imgScale = sc;
        img = new Image[ART.length];
        for (int i = 0; i < ART.length; i++) img[i] = Gfx.sprite(ART[i], PAL[i], sc);
    }

    protected void update() {
        sprites();
        level = frame / 400;
        bw = W / 4 + (magnet > 0 ? W / 6 : 0);
        int sp = (W << 8) / 28;
        if ((held & K_LEFT) != 0) bx -= sp;
        if ((held & K_RIGHT) != 0) bx += sp;
        bx = Math.max((bw / 2) << 8, Math.min((W - bw / 2) << 8, bx));
        if (magnet > 0) magnet--;
        if (slow > 0) slow--;
        if (popT > 0) popT--;
        if (--spawnT <= 0) {
            spawn();
            spawnT = Math.max(7, 26 - level * 2) + Rnd.nextInt(8);
        }
        int basketY = H - Math.max(14, H / 9);
        int s = 8 * imgScale;
        for (int i = 0; i < MAXF; i++) {
            if (!fon[i]) continue;
            fy[i] += slow > 0 ? fv[i] / 2 : fv[i];
            int y = fy[i] >> 8;
            if (y + s >= basketY && y < basketY + 4 && Math.abs(fx[i] - (bx >> 8)) < bw / 2 + s / 3) {
                fon[i] = false;
                catchIt(fk[i], fx[i], basketY);
            } else if (y > H) {
                fon[i] = false;
                if (fk[i] < 5) {
                    drops++;
                    combo = 0;
                    Sfx.tone(45, 30);
                    if (drops >= 10) {
                        headline = "TOO MANY DROPS";
                        endGame(false);
                        return;
                    }
                }
            }
        }
    }

    private void catchIt(int k, int x, int y) {
        if (k == 5) {
            lives--;
            combo = 0;
            show("BOOM!", x, y);
            Sfx.bad();
            if (lives <= 0) {
                headline = "KABOOM!";
                endGame(false);
            }
            return;
        }
        if (k == 6) { magnet = 240; show("WIDE!", x, y); Sfx.good(); return; }
        if (k == 7) { slow = 200; show("SLOW-MO", x, y); Sfx.good(); return; }
        combo++;
        int mult = 1 + combo / 5;
        score += PTS[k] * mult;
        show("+" + PTS[k] * mult, x, y);
        Sfx.tone(72 + Math.min(combo, 20), 25);
    }

    private void show(String s, int x, int y) {
        pop = s;
        popX = x;
        popY = y - 12;
        popT = 14;
    }

    private void spawn() {
        for (int i = 0; i < MAXF; i++) {
            if (fon[i]) continue;
            int r = Rnd.nextInt(100);
            int bombChance = Math.min(30, 8 + level * 3);
            int k;
            if (r < bombChance) k = 5;
            else if (r < bombChance + 3) k = 4;
            else if (r < bombChance + 5) k = 6;
            else if (r < bombChance + 7) k = 7;
            else k = Rnd.nextInt(4);
            fk[i] = k;
            fx[i] = Rnd.range(8, W - 8);
            fy[i] = -16 << 8;
            fv[i] = (H << 8) / Rnd.range(70, 110) * (10 + level) / 10;
            fon[i] = true;
            return;
        }
    }

    protected void draw(Graphics g) {
        sprites();
        for (int i = 0; i < 6; i++) {
            g.setColor(Gfx.mix(0x4FC3F7, 0xB3E5FC, i * 42));
            g.fillRect(0, i * H / 6, W, H / 6 + 1);
        }
        int basketY = H - Math.max(14, H / 9);
        g.setColor(0x7CB342);
        g.fillRect(0, basketY + 8, W, H - basketY - 8);
        g.setColor(0x558B2F);
        for (int x = 0; x < W; x += 6) g.drawLine(x, basketY + 8, x + 2, basketY + 5);
        for (int i = 0; i < MAXF; i++) {
            if (!fon[i]) continue;
            g.drawImage(img[fk[i]], fx[i], fy[i] >> 8, Gfx.TC);
        }
        // basket
        int x0 = (bx >> 8) - bw / 2;
        g.setColor(0x8D6E63);
        g.fillTriangle(x0, basketY, x0 + bw, basketY, x0 + bw - 4, basketY + 10);
        g.fillTriangle(x0, basketY, x0 + bw - 4, basketY + 10, x0 + 4, basketY + 10);
        g.setColor(0x5D4037);
        for (int k = x0 + 4; k < x0 + bw - 4; k += 5) g.drawLine(k, basketY + 1, k + 1, basketY + 9);
        g.drawLine(x0, basketY, x0 + bw, basketY);
        if (magnet > 0) {
            g.setColor(0xE53935);
            g.drawRect(x0 - 1, basketY - 2, bw + 1, 13);
        }
        if (popT > 0) Gfx.shadowText(g, pop, popX, popY - (14 - popT), Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x000000);
        g.drawString(String.valueOf(score), 3, 2, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString(String.valueOf(score), 2, 1, Gfx.TL);
        g.setColor(0xE53935);
        for (int i = 0; i < lives; i++) Gfx.disc(g, W - 6 - i * 9, 6, 3);
        g.setFont(Gfx.SMALL);
        g.setColor(0x33691E);
        g.drawString("drops " + drops + "/10", W - 2, 12, Gfx.TR);
        if (combo >= 5) Gfx.text(g, "x" + (1 + combo / 5), W / 2, 1, Gfx.TC, Gfx.SMALL_B, 0xFFEB3B);
        if (slow > 0) Gfx.text(g, "SLOW", 2, 12, Gfx.TL, Gfx.SMALL, 0x1A237E);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        sprites();
        for (int k = 0; k < 4; k++) {
            int yy = y + ((clock * 2 + k * h / 2) % (h + 10)) - 10;
            g.drawImage(img[(k * 3) % 5], x + (k + 1) * w / 5, yy, Gfx.TC);
        }
        g.setColor(0x8D6E63);
        int bwid = w / 4, bxx = x + w / 2 - bwid / 2 + (gamekit.FMath.sin(clock * 4) * (w / 4) >> 10);
        g.fillRect(bxx, y + h - 8, bwid, 8);
    }
}
