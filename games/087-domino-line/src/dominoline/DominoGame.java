package dominoline;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Domino Line: draw dominoes against the CPU with a double-six set. Match a
 * tile to either open end of the line; draw from the boneyard if you can't.
 * Tiles are encoded a*7+b with a <= b.
 */
public class DominoGame extends Game {
    private final int[] bone = new int[28];
    private int boneN;
    private final int[][] hand = new int[2][28];
    private final int[] handN = new int[2];
    private final int[] lineL = new int[28], lineR = new int[28];
    private int lineN, leftEnd, rightEnd, turn, sel, wait, passes;
    private String note = "";

    protected String name() { return "Domino Line"; }

    protected String[] help() {
        return new String[] {
            "Play dominoes from your hand onto either end of the line. A tile fits if one of its halves matches the number at that end.",
            "If you can't play, press # to draw from the boneyard; when it's empty you pass. First to play all their tiles wins and scores the pips left in the other hand.",
            "- Controls",
            "4/6: choose tile",
            "1: play on left end  3: right end",
            "5: play on whichever end fits",
            "#: draw / pass",
        };
    }

    protected int accent() { return 0xECEFF1; }

    protected void newGame() {
        int k = 0;
        for (int a = 0; a <= 6; a++) for (int b = a; b <= 6; b++) bone[k++] = a * 7 + b;
        for (int i = 27; i > 0; i--) {
            int j = Rnd.nextInt(i + 1);
            int t = bone[i];
            bone[i] = bone[j];
            bone[j] = t;
        }
        boneN = 28;
        for (int p = 0; p < 2; p++) {
            handN[p] = 0;
            for (int i = 0; i < 7; i++) hand[p][handN[p]++] = bone[--boneN];
        }
        lineN = 0;
        // highest double starts
        int starter = 0, startIdx = -1, bestD = -1;
        for (int p = 0; p < 2; p++) for (int i = 0; i < handN[p]; i++) {
            int t = hand[p][i];
            if (t / 7 == t % 7 && t / 7 > bestD) { bestD = t / 7; starter = p; startIdx = i; }
        }
        if (startIdx < 0) { starter = 0; startIdx = 0; }
        int t = hand[starter][startIdx];
        hand[starter][startIdx] = hand[starter][--handN[starter]];
        lineL[0] = t / 7;
        lineR[0] = t % 7;
        lineN = 1;
        leftEnd = t / 7;
        rightEnd = t % 7;
        turn = 1 - starter;
        sel = 0;
        passes = 0;
        note = starter == 0 ? "You opened" : "CPU opened";
    }

    private boolean fits(int t, int end) {
        return t / 7 == end || t % 7 == end;
    }

    private void play(int p, int idx, boolean left) {
        int t = hand[p][idx];
        int a = t / 7, b = t % 7;
        hand[p][idx] = hand[p][--handN[p]];
        if (left) {
            int outer = a == leftEnd ? b : a;
            for (int i = lineN; i > 0; i--) { lineL[i] = lineL[i - 1]; lineR[i] = lineR[i - 1]; }
            lineL[0] = outer;
            lineR[0] = leftEnd;
            leftEnd = outer;
        } else {
            int outer = a == rightEnd ? b : a;
            lineL[lineN] = rightEnd;
            lineR[lineN] = outer;
            rightEnd = outer;
        }
        lineN++;
        passes = 0;
        Sfx.tone(p == 0 ? 72 : 62, 25);
        if (handN[p] == 0) {
            int pips = 0;
            for (int i = 0; i < handN[1 - p]; i++) pips += hand[1 - p][i] / 7 + hand[1 - p][i] % 7;
            if (p == 0) {
                score = pips + 50;
                headline = "DOMINO! +" + pips;
                endGame(true);
            } else {
                headline = "CPU DOMINOES";
                endGame(false);
            }
            return;
        }
        turn = 1 - p;
    }

    private boolean canPlay(int p) {
        for (int i = 0; i < handN[p]; i++) if (fits(hand[p][i], leftEnd) || fits(hand[p][i], rightEnd)) return true;
        return false;
    }

    private void blocked() {
        int me = 0, them = 0;
        for (int i = 0; i < handN[0]; i++) me += hand[0][i] / 7 + hand[0][i] % 7;
        for (int i = 0; i < handN[1]; i++) them += hand[1][i] / 7 + hand[1][i] % 7;
        score = Math.max(0, them - me);
        headline = "BLOCKED " + me + " v " + them;
        endGame(me < them);
    }

    protected void update() {
        if (turn == 1) {
            if (++wait < 15) return;
            wait = 0;
            int bestI = -1, bestV = -1;
            boolean bestLeft = true;
            for (int i = 0; i < handN[1]; i++) {
                int t = hand[1][i], v = t / 7 + t % 7 + (t / 7 == t % 7 ? 5 : 0);
                if (fits(t, leftEnd) && v > bestV) { bestV = v; bestI = i; bestLeft = true; }
                if (fits(t, rightEnd) && v > bestV) { bestV = v; bestI = i; bestLeft = false; }
            }
            if (bestI >= 0) {
                play(1, bestI, bestLeft);
                note = "CPU played";
            } else if (boneN > 0) {
                hand[1][handN[1]++] = bone[--boneN];
                note = "CPU draws";
            } else {
                passes++;
                note = "CPU passes";
                turn = 0;
                if (passes >= 2) blocked();
            }
            return;
        }
        int n = handN[0];
        if (sel >= n) sel = n - 1;
        int d = digitPressed();
        if (d < 0 || d == 4 || d == 6) {
            if ((pressed & K_LEFT) != 0) sel = (sel + n - 1) % n;
            if ((pressed & K_RIGHT) != 0) sel = (sel + 1) % n;
        }
        int t = hand[0][sel];
        if (digit(1) || digit(3) || ((pressed & K_FIRE) != 0)) {
            boolean wantLeft = digit(1) || ((pressed & K_FIRE) != 0 && fits(t, leftEnd));
            boolean ok = wantLeft ? fits(t, leftEnd) : fits(t, rightEnd);
            if (ok) play(0, sel, wantLeft);
            else {
                Sfx.bad();
                note = "Doesn't fit there";
            }
        } else if ((pressed & K_POUND) != 0) {
            if (canPlay(0)) {
                note = "A tile fits - play it";
                Sfx.bad();
            } else if (boneN > 0) {
                hand[0][handN[0]++] = bone[--boneN];
                sel = handN[0] - 1;
                note = "You draw";
                Sfx.click();
            } else {
                passes++;
                turn = 1;
                note = "You pass";
                if (passes >= 2) blocked();
            }
        }
    }

    private void tile(Graphics g, int a, int b, int x, int y, int w, int h, boolean vertical, boolean hi) {
        g.setColor(hi ? 0xFFF59D : 0xFAFAFA);
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(0x424242);
        g.drawRoundRect(x, y, w - 1, h - 1, 4, 4);
        if (vertical) {
            g.drawLine(x + 2, y + h / 2, x + w - 3, y + h / 2);
            pips(g, a, x, y, w, h / 2);
            pips(g, b, x, y + h / 2, w, h / 2);
        } else {
            g.drawLine(x + w / 2, y + 2, x + w / 2, y + h - 3);
            pips(g, a, x, y, w / 2, h);
            pips(g, b, x + w / 2, y, w / 2, h);
        }
    }

    private void pips(Graphics g, int n, int x, int y, int w, int h) {
        g.setColor(0x212121);
        int cx = x + w / 2, cy = y + h / 2, ox = w / 4, oy = h / 4;
        if (n % 2 == 1) g.fillRect(cx - 1, cy - 1, 2, 2);
        if (n >= 2) { g.fillRect(cx - ox - 1, cy - oy - 1, 2, 2); g.fillRect(cx + ox - 1, cy + oy - 1, 2, 2); }
        if (n >= 4) { g.fillRect(cx + ox - 1, cy - oy - 1, 2, 2); g.fillRect(cx - ox - 1, cy + oy - 1, 2, 2); }
        if (n == 6) { g.fillRect(cx - ox - 1, cy - 1, 2, 2); g.fillRect(cx + ox - 1, cy - 1, 2, 2); }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x1B5E20);
        g.fillRect(0, 0, W, H);
        int tw = Math.max(18, W / 7), th = tw / 2;
        // line: show as many tiles as fit, centred on the middle of the line
        int fit = Math.max(1, (W - 4) / (tw + 1));
        int start = Math.max(0, lineN / 2 - fit / 2), end = Math.min(lineN, start + fit);
        start = Math.max(0, end - fit);
        int lx = (W - (end - start) * (tw + 1)) / 2, ly = H / 2 - th;
        for (int i = start; i < end; i++) tile(g, lineL[i], lineR[i], lx + (i - start) * (tw + 1), ly, tw, th, false, false);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFEB3B);
        g.drawString("1:" + leftEnd, 2, ly - Gfx.SMALL.getHeight() - 2, Gfx.TL);
        g.drawString("3:" + rightEnd, W - 2, ly - Gfx.SMALL.getHeight() - 2, Gfx.TR);
        if (start > 0) g.drawString("<", 0, ly + 2, Gfx.TL);
        if (end < lineN) g.drawString(">", W - 1, ly + 2, Gfx.TR);
        // hand
        int n = handN[0];
        int hw = Math.max(10, Math.min(tw / 2 + 2, (W - 4) / Math.max(1, n) - 2)), hh = hw * 2;
        int hx = (W - n * (hw + 2)) / 2, hy = H - hh - hud - 6;
        for (int i = 0; i < n; i++) {
            int t = hand[0][i];
            boolean on = i == sel && turn == 0;
            tile(g, t / 7, t % 7, hx + i * (hw + 2), hy - (on ? 4 : 0), hw, hh, true, on && (fits(t, leftEnd) || fits(t, rightEnd)));
        }
        g.setColor(0xFFFFFF);
        g.drawString("CPU " + handN[1], 2, 1, Gfx.TL);
        g.drawString("Yard " + boneN, W - 2, 1, Gfx.TR);
        Gfx.text(g, note, W / 2, H - hud, Gfx.TC, Gfx.SMALL, 0xC8E6C9);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int tw = Math.max(16, Math.min(w / 4, h));
        for (int k = 0; k < 3; k++) {
            int a = (clock / 10 + k) % 7, b = (clock / 10 + k + 1) % 7;
            tile(g, a, b, x + w / 2 - tw * 3 / 2 + k * tw, y + h / 2 - tw / 4, tw - 1, tw / 2, false, false);
        }
    }
}
