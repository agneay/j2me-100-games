package morsesignal;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Morse Signal: use the 5 key as a telegraph key. In Send mode tap short
 * for a dot and hold for a dash to key the shown words; in Receive mode
 * read a flashing lamp and pick the right letter. 90 seconds per round.
 */
public class MorseGame extends Game {
    private static final String[] CODE = {
        ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--",
        "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..",
    };
    private static final String[] WORDS = {
        "SOS", "HI", "CQ", "TEST", "RADIO", "MORSE", "SIGNAL", "KEY", "SHIP", "NEWS", "ECHO", "WAVE",
        "STORM", "LIGHT", "TOWER", "COAST", "PORT", "HELP", "OVER", "OUT", "COPY", "DOT", "DASH", "WIRE",
    };
    private static final int TIME = 1800; // 90 s at 50 ms

    private String word = "", keyed = "";
    private int pos, downT, idleT, timeLeft, combo, wrongT;
    private boolean down;
    // receive mode
    private int target, answerSel, playT, lampOn, reveal, rounds;
    private boolean playing;
    private final int[] choice = new int[4];
    private final int[] seqOn = new int[8], seqLen = new int[1];

    protected String name() { return "Morse Signal"; }

    protected String[] help() {
        return new String[] {
            "Learn Morse code with your keypad. You have 90 seconds per round.",
            "Send: key each letter of the word with the 5 key. A quick tap is a dot, a longer press is a dash. Pause briefly to finish a letter. The code for the current letter is shown.",
            "Receive: watch the lamp and listen. Short flash = dot, long flash = dash. Pick the letter with keys 1 to 4; 0 repeats the signal.",
            "- Controls",
            "5: telegraph key (Send)",
            "1-4: choose letter (Receive)",
            "0: replay signal (Receive)",
        };
    }

    protected String[] modes() { return new String[] { "Send", "Receive" }; }

    protected int accent() { return 0xFFC107; }

    protected void newGame() {
        timeLeft = TIME;
        combo = 0;
        rounds = 0;
        if (mode == 0) nextWord();
        else nextLetter();
    }

    private void nextWord() {
        word = WORDS[Rnd.nextInt(WORDS.length)];
        pos = 0;
        keyed = "";
        down = false;
    }

    private void nextLetter() {
        target = Rnd.nextInt(26);
        choice[0] = target;
        for (int k = 1; k < 4; k++) {
            int c;
            boolean dup;
            do {
                c = Rnd.nextInt(26);
                dup = false;
                for (int j = 0; j < k; j++) if (choice[j] == c) dup = true;
            } while (dup);
            choice[k] = c;
        }
        Rnd.shuffle(choice);
        reveal = 0;
        startPlayback();
    }

    private void startPlayback() {
        String c = CODE[target];
        seqLen[0] = c.length();
        for (int k = 0; k < c.length(); k++) seqOn[k] = c.charAt(k) == '.' ? 3 : 9;
        playing = true;
        playT = -6;
        lampOn = 0;
    }

    protected void update() {
        if (--timeLeft <= 0) {
            headline = "TIME! " + score + " POINTS";
            endGame(score > 0);
            return;
        }
        if (wrongT > 0) wrongT--;
        if (mode == 0) updateSend();
        else updateReceive();
    }

    private void updateSend() {
        boolean isDown = (held & K_FIRE) != 0 || (pressed & K_FIRE) != 0;
        if (isDown) {
            if (!down) {
                down = true;
                downT = 0;
                Sfx.tone(81, 40);
            }
            downT++;
            idleT = 0;
            if (downT % 3 == 0) Sfx.tone(81, 150);
        } else {
            if (down) {
                down = false;
                keyed += downT <= 3 ? "." : "-";
                if (keyed.length() > 5) keyed = keyed.substring(1);
            }
            idleT++;
            if (keyed.length() > 0 && idleT > 12) commitLetter();
        }
    }

    private void commitLetter() {
        int want = word.charAt(pos) - 'A';
        if (keyed.equals(CODE[want])) {
            pos++;
            combo++;
            score += 10 + Math.min(combo, 10);
            Sfx.good();
            if (pos >= word.length()) {
                score += word.length() * 5;
                Sfx.win();
                nextWord();
                return;
            }
        } else {
            combo = 0;
            wrongT = 15;
            Sfx.bad();
        }
        keyed = "";
    }

    private void updateReceive() {
        if (playing) {
            playT++;
            int t = playT;
            lampOn = 0;
            boolean done = t >= 0;
            if (t >= 0) {
                for (int k = 0; k < seqLen[0]; k++) {
                    if (t < seqOn[k]) {
                        lampOn = 1;
                        if (t == 0) Sfx.tone(81, seqOn[k] * 50);
                        done = false;
                        break;
                    }
                    t -= seqOn[k];
                    if (t < 3) {
                        done = false;
                        break;
                    }
                    t -= 3;
                }
            }
            playing = !done;
        }
        if (reveal > 0) {
            if (--reveal == 0) {
                rounds++;
                nextLetter();
            }
            return;
        }
        if (digit(0)) startPlayback();
        int d = digitPressed();
        if (d >= 1 && d <= 4) {
            if (choice[d - 1] == target) {
                combo++;
                score += 10 + Math.min(combo, 10) + (playing ? 5 : 0);
                Sfx.good();
            } else {
                combo = 0;
                wrongT = 15;
                Sfx.bad();
            }
            answerSel = d - 1;
            reveal = 15;
        }
    }

    private void drawCode(Graphics g, String c, int cx, int y, int unit, int col) {
        int w = 0;
        for (int k = 0; k < c.length(); k++) w += (c.charAt(k) == '.' ? unit : unit * 3) + unit;
        int x = cx - w / 2;
        g.setColor(col);
        for (int k = 0; k < c.length(); k++) {
            int len = c.charAt(k) == '.' ? unit : unit * 3;
            if (c.charAt(k) == '.') Gfx.disc(g, x + unit / 2, y + unit / 2, unit / 2);
            else g.fillRect(x, y, len, unit);
            x += len + unit;
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int fh = Gfx.SMALL.getHeight();
        g.setColor(wrongT > 0 && (wrongT & 2) != 0 ? 0x3E1010 : 0x1B1B14);
        g.fillRect(0, 0, W, H);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFC107);
        g.drawString(Gfx.time(timeLeft, tickMs), 2, 1, Gfx.TL);
        g.drawString(String.valueOf(score), W - 2, 1, Gfx.TR);
        int unit = Math.max(4, W / 30);
        if (mode == 0) {
            // word with progress
            int lw = Gfx.LARGE.charWidth('W') + 2;
            int x = W / 2 - word.length() * lw / 2;
            for (int k = 0; k < word.length(); k++) {
                int col = k < pos ? 0x66BB6A : (k == pos ? 0xFFFFFF : 0x757575);
                Gfx.text(g, String.valueOf(word.charAt(k)), x + k * lw + lw / 2, hud + 6, Gfx.TC, Gfx.LARGE, col);
            }
            int want = word.charAt(pos) - 'A';
            int cy = hud + 10 + Gfx.LARGE.getHeight();
            Gfx.text(g, word.charAt(pos) + " is", W / 2, cy, Gfx.TC, Gfx.SMALL, 0xBDBDBD);
            drawCode(g, CODE[want], W / 2, cy + fh + 3, unit, 0xFFC107);
            // keyed so far
            int ky = H / 2 + unit * 2;
            Gfx.text(g, "you:", W / 2, ky - fh - 2, Gfx.TC, Gfx.SMALL, 0x9E9E9E);
            drawCode(g, keyed, W / 2, ky, unit, 0x4FC3F7);
            // key graphic
            int kw = W / 3, kh = Math.max(8, H / 14), kx = W / 2 - kw / 2, kyy = H - kh - fh - 8;
            g.setColor(0x5D4037);
            g.fillRect(kx - 4, kyy + kh, kw + 8, 4);
            g.setColor(down ? 0xFFB300 : 0xB0BEC5);
            g.fillRect(kx, kyy + (down ? 3 : 0), kw, kh - 3);
            g.setColor(0x212121);
            Gfx.disc(g, kx + kw - 4, kyy + (down ? 3 : 0) + kh / 2 - 2, 3);
            Gfx.text(g, combo > 1 ? "combo x" + combo : "tap . hold -", W / 2, H - fh - 2, Gfx.TC, Gfx.SMALL, 0x9E9E9E);
        } else {
            int lr = Math.max(10, Math.min(W, H) / 7);
            int ly = hud + lr + 6;
            g.setColor(0x424242);
            Gfx.disc(g, W / 2, ly, lr + 3);
            g.setColor(lampOn > 0 ? 0xFFEB3B : 0x5D4037);
            Gfx.disc(g, W / 2, ly, lr);
            if (lampOn > 0) {
                g.setColor(0xFFFDE7);
                Gfx.disc(g, W / 2 - lr / 3, ly - lr / 3, lr / 3);
            }
            int by = ly + lr + 8;
            int bw = (W - 10) / 2, bh = Math.max(fh + 4, (H - by - fh - 8) / 2 - 2);
            for (int k = 0; k < 4; k++) {
                int x = 4 + (k % 2) * (bw + 2), y = by + (k / 2) * (bh + 2);
                int fill = 0x263238;
                if (reveal > 0 && choice[k] == target) fill = 0x2E7D32;
                else if (reveal > 0 && k == answerSel) fill = 0xB71C1C;
                Gfx.panel(g, x, y, bw, bh, fill, 0xFFC107);
                Gfx.text(g, (k + 1) + ":" + (char) ('A' + choice[k]), x + 4, y + 2, Gfx.TL, Gfx.MEDIUM, 0xFFFFFF);
                if (reveal > 0) drawCode(g, CODE[choice[k]], x + bw * 2 / 3, y + bh / 2 - 2, Math.max(3, unit * 2 / 3), 0xFFE082);
            }
            Gfx.text(g, "0: replay   letters " + rounds, W / 2, H - fh - 2, Gfx.TC, Gfx.SMALL, 0x9E9E9E);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        String sos = "...---...";
        int unit = Math.max(3, w / 40);
        int t = (clock / 4) % (sos.length() + 4);
        int cx = x + w / 2;
        int total = 0;
        for (int k = 0; k < sos.length(); k++) total += (sos.charAt(k) == '.' ? unit : unit * 3) + unit;
        int px = cx - total / 2;
        for (int k = 0; k < sos.length(); k++) {
            int len = sos.charAt(k) == '.' ? unit : unit * 3;
            g.setColor(k == t ? 0xFFFFFF : 0xFFC107);
            g.fillRect(px, y + h / 2 - unit / 2, len, unit);
            px += len + unit;
        }
    }
}
