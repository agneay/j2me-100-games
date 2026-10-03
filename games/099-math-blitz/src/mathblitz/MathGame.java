package mathblitz;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Math Blitz: mental arithmetic on the number pad. Answers are checked as
 * soon as enough digits are typed, so no confirm key is needed. Sprint is a
 * 60 second dash; Survival gives a shrinking timer per question and three
 * lives.
 */
public class MathGame extends Game {
    private int a, b, op, answer, typed, typedLen, level, streak, timeLeft, qTime, qLimit, lives, solved, flashT, flashOk;
    private String lastQ = "";

    protected String name() { return "Math Blitz"; }

    protected String[] help() {
        return new String[] {
            "Solve sums as fast as you can by typing the answer on the number keys. The answer is checked as soon as you have typed enough digits.",
            "Sprint: 60 seconds, each correct answer adds a second. Survival: every question has its own timer that gets shorter; three misses and you are out.",
            "Questions get harder as your streak grows.",
            "- Controls",
            "0-9: type answer",
            "#: clear typed digits",
        };
    }

    protected String[] modes() { return new String[] { "Sprint", "Survival" }; }

    protected int accent() { return 0x00E676; }

    protected void newGame() {
        level = 1;
        streak = 0;
        solved = 0;
        lives = 3;
        timeLeft = 1200;
        qLimit = 200;
        next();
    }

    private void next() {
        int max = 5 + level * 4;
        op = Rnd.nextInt(level < 3 ? 2 : (level < 6 ? 3 : 4));
        switch (op) {
            case 0:
                a = Rnd.range(1, max);
                b = Rnd.range(1, max);
                answer = a + b;
                break;
            case 1:
                a = Rnd.range(2, max);
                b = Rnd.range(1, a);
                answer = a - b;
                break;
            case 2:
                a = Rnd.range(2, Math.min(12, 3 + level));
                b = Rnd.range(2, 10);
                answer = a * b;
                break;
            default:
                b = Rnd.range(2, 10);
                answer = Rnd.range(2, Math.min(12, 2 + level));
                a = answer * b;
                break;
        }
        typed = 0;
        typedLen = 0;
        qTime = 0;
    }

    private int digits(int v) {
        int n = 1;
        while (v >= 10) {
            v /= 10;
            n++;
        }
        return n;
    }

    private static final String OPS = "+-x/";

    private String question() {
        return a + " " + OPS.charAt(op) + " " + b;
    }

    protected void update() {
        if (flashT > 0) flashT--;
        qTime++;
        if (mode == 0) {
            if (--timeLeft <= 0) {
                headline = solved + " SOLVED";
                endGame(solved > 0);
                return;
            }
        } else if (qTime > qLimit) {
            miss();
            return;
        }
        if ((pressed & K_POUND) != 0) {
            typed = 0;
            typedLen = 0;
        }
        int d = digitPressed();
        if (d < 0) return;
        if (typedLen == 0 && d == 0 && answer != 0) {
            Sfx.bad();
            return;
        }
        typed = typed * 10 + d;
        typedLen++;
        Sfx.click();
        if (typedLen >= digits(answer)) {
            if (typed == answer) {
                solved++;
                streak++;
                score += 10 * level + Math.max(0, 40 - qTime / 2);
                if (mode == 0) timeLeft += 20;
                if (streak % 4 == 0) level++;
                qLimit = Math.max(60, 200 - solved * 5);
                flashOk = 1;
                Sfx.good();
            } else {
                flashOk = 0;
                Sfx.bad();
                streak = 0;
                level = Math.max(1, level - 1);
                if (mode == 1) {
                    lastQ = question() + " = " + answer;
                    flashT = 12;
                    loseLife();
                    return;
                }
            }
            lastQ = question() + " = " + answer;
            flashT = 12;
            next();
        }
    }

    private void miss() {
        lastQ = question() + " = " + answer;
        flashOk = 0;
        flashT = 12;
        streak = 0;
        Sfx.bad();
        loseLife();
    }

    private void loseLife() {
        if (--lives <= 0) {
            headline = solved + " SOLVED";
            endGame(solved >= 10);
            return;
        }
        next();
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int fh = Gfx.SMALL.getHeight();
        g.setColor(0x0B1F14);
        g.fillRect(0, 0, W, H);
        g.setColor(0x123322);
        for (int x = 0; x < W; x += 12) g.drawLine(x, hud, x, H);
        for (int y = hud; y < H; y += 12) g.drawLine(0, y, W, y);
        g.setColor(0x000000);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x00E676);
        g.drawString(String.valueOf(score), 2, 1, Gfx.TL);
        if (mode == 0) {
            g.setColor(timeLeft < 200 ? 0xFF5252 : 0xFFFFFF);
            g.drawString(Gfx.time(timeLeft, tickMs), W - 2, 1, Gfx.TR);
        } else {
            g.setColor(0xFF5252);
            String h = "";
            for (int k = 0; k < lives; k++) h += "<3 ";
            g.drawString(h.trim(), W - 2, 1, Gfx.TR);
        }
        Gfx.text(g, "Level " + level + "  streak " + streak, W / 2, hud + 2, Gfx.TC, Gfx.SMALL, 0x69F0AE);
        String q = question() + " =";
        int qy = H / 2 - Gfx.LARGE.getHeight() - 4;
        Gfx.shadowText(g, q, W / 2, qy, Gfx.TC, Gfx.fit(q, W - 8), 0xFFFFFF, 0x004D40);
        // answer boxes
        int n = digits(answer);
        int bw = Math.max(12, Gfx.LARGE.charWidth('0') + 6), bh = Gfx.LARGE.getHeight() + 4;
        int bx = W / 2 - (n * (bw + 3)) / 2, by = H / 2 + 2;
        String t = typedLen > 0 ? String.valueOf(typed) : "";
        for (int k = 0; k < n; k++) {
            Gfx.panel(g, bx + k * (bw + 3), by, bw, bh, 0x1B5E20, k == typedLen ? 0xFFFFFF : 0x00C853);
            if (k < t.length()) Gfx.text(g, String.valueOf(t.charAt(k)), bx + k * (bw + 3) + bw / 2, by + 2, Gfx.TC, Gfx.LARGE, 0xFFFFFF);
        }
        if (mode == 1) Gfx.bar(g, 8, by + bh + 6, W - 16, 4, qLimit - qTime, qLimit, 0xFFD740, 0x263238);
        if (flashT > 0) Gfx.text(g, lastQ, W / 2, H - fh * 2 - 4, Gfx.TC, Gfx.SMALL_B, flashOk == 1 ? 0x69F0AE : 0xFF8A80);
        Gfx.text(g, "#: clear", W / 2, H - fh - 1, Gfx.TC, Gfx.SMALL, 0x4CAF50);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        String[] s = { "7+5", "9x3", "48/6", "15-8" };
        String cur = s[(clock / 20) % 4];
        Gfx.shadowText(g, cur, x + w / 2, y + (h - Gfx.LARGE.getHeight()) / 2, Gfx.TC, Gfx.LARGE, 0x00E676, 0x004D40);
    }
}
