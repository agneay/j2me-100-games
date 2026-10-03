package echotones;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/** Echo Tones: watch and listen to a growing sequence, then play it back on the keypad. */
public class EchoGame extends Game {
    private static final int[] NOTES = { 0, 60, 62, 64, 67, 69, 72, 74, 76, 79 };
    private static final int[] COLORS = { 0, 0xEF5350, 0x42A5F5, 0x66BB6A, 0xFFCA28, 0xAB47BC, 0x26C6DA, 0xFF7043, 0x8D6E63, 0xEC407A };
    private static final int[] CLASSIC = { 2, 4, 6, 8 };

    private final int[] seq = new int[100];
    private int len, pos, phase, timer, lit = -1, litTimer, flash;
    private static final int SHOW = 0, INPUT = 1, GOOD = 2;

    protected String name() { return "Echo Tones"; }

    protected String[] help() {
        return new String[] {
            "The phone plays a sequence of lights and tones on the keypad. Repeat it by pressing the same keys in the same order.",
            "Each round adds one more step and the playback gets faster. One mistake ends the game.",
            "Classic uses keys 2, 4, 6 and 8. Keypad uses all nine keys. Reverse: play the sequence backwards!",
            "- Controls",
            "Number keys: play a pad",
            "Joystick works for 2/4/6/8 and 5",
        };
    }

    protected String[] modes() { return new String[] { "Classic", "Keypad 9", "Reverse" }; }

    protected int accent() { return 0xEC407A; }

    private boolean nine() { return mode == 1; }

    protected void newGame() {
        len = 0;
        addStep();
    }

    private void addStep() {
        seq[len++] = nine() ? 1 + Rnd.nextInt(9) : CLASSIC[Rnd.nextInt(4)];
        phase = SHOW;
        pos = 0;
        timer = 14;
    }

    private int stepTime() {
        return Math.max(4, 12 - len / 2);
    }

    private void light(int pad, int ticks) {
        lit = pad;
        litTimer = ticks;
        Sfx.tone(NOTES[pad], ticks * tickMs * 8 / 10);
    }

    private int keyPad() {
        int d = digitPressed();
        if (d >= 1) return d;
        if ((pressed & K_UP) != 0) return 2;
        if ((pressed & K_LEFT) != 0) return 4;
        if ((pressed & K_RIGHT) != 0) return 6;
        if ((pressed & K_DOWN) != 0) return 8;
        if ((pressed & K_FIRE) != 0) return 5;
        return -1;
    }

    protected void update() {
        if (litTimer > 0 && --litTimer == 0) lit = -1;
        if (flash > 0) flash--;
        switch (phase) {
            case SHOW:
                if (--timer > 0) break;
                if (pos < len) {
                    light(seq[pos++], stepTime());
                    timer = stepTime() + 3;
                } else {
                    phase = INPUT;
                    pos = 0;
                }
                break;
            case INPUT: {
                int pad = keyPad();
                if (pad < 1) break;
                if (!nine() && pad != 2 && pad != 4 && pad != 6 && pad != 8) break;
                int expect = mode == 2 ? seq[len - 1 - pos] : seq[pos];
                if (pad == expect) {
                    light(pad, 5);
                    pos++;
                    if (pos == len) {
                        score = len;
                        phase = GOOD;
                        timer = 20;
                        flash = 10;
                    }
                } else {
                    lit = expect;
                    litTimer = 40;
                    Sfx.bad();
                    score = len - 1;
                    headline = "WRONG KEY!";
                    endGame(false);
                }
                break;
            }
            default:
                if (--timer <= 0) {
                    if (len >= seq.length) {
                        endGame(true);
                        return;
                    }
                    addStep();
                }
                break;
        }
    }

    private void drawPads(Graphics g, int x0, int y0, int s, int gap, boolean labels) {
        for (int k = 1; k <= 9; k++) {
            boolean active = nine() || k == 2 || k == 4 || k == 6 || k == 8;
            int x = x0 + ((k - 1) % 3) * (s + gap), y = y0 + ((k - 1) / 3) * (s + gap);
            if (!active) {
                g.setColor(0x1B1B26);
                g.fillRoundRect(x, y, s, s, s / 4, s / 4);
                continue;
            }
            int c = COLORS[k];
            boolean on = lit == k;
            g.setColor(on ? Gfx.shade(c, 55) : Gfx.shade(c, -55));
            g.fillRoundRect(x, y, s, s, s / 4, s / 4);
            g.setColor(on ? 0xFFFFFF : Gfx.shade(c, -20));
            g.drawRoundRect(x, y, s - 1, s - 1, s / 4, s / 4);
            if (labels) {
                g.setFont(s >= 30 ? Gfx.LARGE : Gfx.MEDIUM);
                g.setColor(on ? 0x1A1A1A : Gfx.shade(c, 10));
                int fh = s >= 30 ? Gfx.LARGE.getBaselinePosition() : Gfx.MEDIUM.getBaselinePosition();
                g.drawString(String.valueOf(k), x + s / 2 + 1, y + (s - fh) / 2, Gfx.TC);
            }
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(flash > 0 && (flash & 2) != 0 ? 0x1E2A1E : 0x0D0D14);
        g.fillRect(0, 0, W, H);
        int size = Math.min(W - 10, H - hud * 3);
        int gap = Math.max(3, size / 20);
        int s = (size - gap * 2) / 3;
        int x0 = (W - (s * 3 + gap * 2)) / 2, y0 = hud * 2 + (H - hud * 3 - (s * 3 + gap * 2)) / 2;
        drawPads(g, x0, y0, s, gap, true);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString("Length " + len, 2, 1, Gfx.TL);
        g.setColor(0xF48FB1);
        g.drawString(modes()[mode], W - 2, 1, Gfx.TR);
        String st = phase == SHOW ? "Watch & listen..." : (phase == INPUT ? (mode == 2 ? "Play it BACKWARDS" : "Your turn") + " " + pos + "/" + len : "Correct!");
        Gfx.text(g, st, W / 2, hud + 2, Gfx.TC, Gfx.SMALL_B, phase == INPUT ? 0x80DEEA : 0xFFFFFF);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(6, (Math.min(w, h) - 8) / 3);
        lit = 1 + (clock / 6) % 9;
        drawPads(g, x + (w - s * 3 - 4) / 2, y + (h - s * 3 - 4) / 2, s, 2, false);
        lit = -1;
    }
}
