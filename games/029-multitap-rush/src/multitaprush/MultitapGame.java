package multitaprush;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Multitap Rush: type the falling words with old-school phone multi-tap
 * (2 = ABC, 3 = DEF ... 9 = WXYZ) before they hit the ground.
 */
public class MultitapGame extends Game {
    private static final String[] KEYS = { "", "", "ABC", "DEF", "GHI", "JKL", "MNO", "PQRS", "TUV", "WXYZ" };
    private static final String WORDS = "CAT DOG SUN MAP KEY BOX FUN JAM ZIP WAX OWL SKY FOX HAT INK JET LOW BUS CUP "
            + "GAME TEXT PHONE MENU SNAKE RETRO PIXEL TONE CALL SEND BACK LIGHT MUSIC SOUND STAR MOON NOTE CODE BEAM "
            + "QUIZ WAVE JAZZ ZONE FROG GHOST PLANT RIVER CLOCK HAPPY QUICK BRAVE CHARM DREAM FLASH GIANT JUMPY "
            + "KNIGHT LEMON MAGIC NINJA OCEAN PIANO QUEEN ROBOT SPACE TIGER UNDER VOICE WHALE YACHT ZEBRA "
            + "BUTTON KEYPAD SIGNAL BATTERY MESSAGE CONTACT PROFILE RINGTONE ";

    private static final int MAXW = 6;
    private final String[] word = new String[MAXW];
    private final int[] wx = new int[MAXW], wy = new int[MAXW], typed = new int[MAXW];
    private final boolean[] on = new boolean[MAXW];
    private String[] dict;
    private int target = -1, lives, combo, spawnT, fallSpeed, wordsDone;
    // multitap state
    private int tapKey = -1, tapIdx, tapTimer, errFlash;

    protected String name() { return "Multitap Rush"; }

    protected String[] help() {
        return new String[] {
            "Words fall from the sky. Type them the way texts were typed before touchscreens: press 2 once for A, twice for B, three times for C, and so on.",
            "Smart mode accepts a letter the moment it's right, so speed is all that matters. Classic mode is true multi-tap: pause briefly or press a different key to confirm a letter, and wrong letters cost your combo.",
            "The lowest word is your target. Don't let three words reach the ground.",
            "- Keys",
            "2 ABC  3 DEF  4 GHI",
            "5 JKL  6 MNO  7 PQRS",
            "8 TUV  9 WXYZ",
        };
    }

    protected String[] modes() { return new String[] { "Smart tap", "Classic multitap" }; }

    protected int accent() { return 0x00BBF9; }

    protected int pauseKeys() { return K_STAR | K_SOFT; }

    protected void newGame() {
        if (dict == null) {
            int n = 0;
            for (int i = 0; i < WORDS.length(); i++) if (WORDS.charAt(i) == ' ') n++;
            dict = new String[n];
            int s = 0, k = 0;
            for (int i = 0; i < WORDS.length(); i++) {
                if (WORDS.charAt(i) == ' ') {
                    dict[k++] = WORDS.substring(s, i);
                    s = i + 1;
                }
            }
        }
        for (int i = 0; i < MAXW; i++) on[i] = false;
        lives = 3;
        combo = 0;
        wordsDone = 0;
        spawnT = 0;
        fallSpeed = 10;
        target = -1;
        tapKey = -1;
    }

    private void spawn() {
        for (int i = 0; i < MAXW; i++) {
            if (on[i]) continue;
            int maxLen = 3 + wordsDone / 6;
            String w;
            int guard = 0;
            do {
                w = dict[Rnd.nextInt(dict.length)];
            } while (w.length() > maxLen && guard++ < 50);
            word[i] = w;
            typed[i] = 0;
            int ww = Gfx.SMALL_B.stringWidth(w);
            wx[i] = Rnd.range(2, Math.max(3, W - ww - 2));
            wy[i] = -Gfx.SMALL.getHeight() << 8;
            on[i] = true;
            return;
        }
    }

    private int groundY() {
        return H - padH() - 2;
    }

    private int padH() {
        return (Gfx.SMALL.getHeight() + 1) * 3 + 2;
    }

    private void pickTarget() {
        if (target >= 0 && on[target] && typed[target] > 0) return;
        int best = -1;
        for (int i = 0; i < MAXW; i++) if (on[i] && (best < 0 || wy[i] > wy[best])) best = i;
        target = best;
    }

    private void letter(char c) {
        pickTarget();
        if (target < 0) return;
        String w = word[target];
        if (w.charAt(typed[target]) == c) {
            typed[target]++;
            score += 10;
            Sfx.tone(76 + typed[target], 15);
            if (typed[target] == w.length()) {
                combo++;
                wordsDone++;
                score += w.length() * 10 * Math.min(combo, 10);
                on[target] = false;
                target = -1;
                Sfx.good();
                fallSpeed = 10 + wordsDone * 2;
            }
        } else {
            combo = 0;
            errFlash = 6;
            Sfx.bad();
        }
    }

    private void commitTap() {
        if (tapKey < 0) return;
        char c = KEYS[tapKey].charAt(tapIdx);
        tapKey = -1;
        letter(c);
    }

    protected void update() {
        if (errFlash > 0) errFlash--;
        int d = digitPressed();
        if (d >= 2) {
            pickTarget();
            if (mode == 0) {
                // smart: cycle within the key, accept as soon as it matches
                if (tapKey != d) {
                    tapKey = d;
                    tapIdx = 0;
                } else {
                    tapIdx = (tapIdx + 1) % KEYS[d].length();
                }
                tapTimer = 0;
                if (target >= 0) {
                    char need = word[target].charAt(typed[target]);
                    if (KEYS[d].indexOf(need) < 0) {
                        tapKey = -1;
                        combo = 0;
                        errFlash = 6;
                        Sfx.bad();
                    } else if (KEYS[d].charAt(tapIdx) == need) {
                        tapKey = -1;
                        letter(need);
                    } else {
                        Sfx.click();
                    }
                }
            } else {
                if (tapKey >= 0 && tapKey != d) commitTap();
                if (tapKey == d) tapIdx = (tapIdx + 1) % KEYS[d].length();
                else {
                    tapKey = d;
                    tapIdx = 0;
                }
                tapTimer = 0;
                Sfx.click();
            }
        } else if (d == 0 || (pressed & K_POUND) != 0) {
            commitTap();
        }
        if (tapKey >= 0 && mode == 1 && ++tapTimer > 14) commitTap();
        if (--spawnT <= 0) {
            spawn();
            spawnT = Math.max(25, 90 - wordsDone * 3);
        }
        int gy = groundY();
        for (int i = 0; i < MAXW; i++) {
            if (!on[i]) continue;
            wy[i] += fallSpeed * (H << 8) / 22000;
            if ((wy[i] >> 8) + Gfx.SMALL_B.getHeight() > gy) {
                on[i] = false;
                if (target == i) target = -1;
                lives--;
                combo = 0;
                Sfx.bad();
                if (lives <= 0) {
                    headline = wordsDone + " WORDS";
                    endGame(false);
                    return;
                }
            }
        }
    }

    protected void draw(Graphics g) {
        g.setColor(errFlash > 0 ? 0x2A0A10 : 0x0A1424);
        g.fillRect(0, 0, W, H);
        int gy = groundY();
        g.setColor(0x16314F);
        for (int y = 12; y < gy; y += 14) g.drawLine(0, y, W, y);
        Font f = Gfx.SMALL_B;
        pickTarget();
        for (int i = 0; i < MAXW; i++) {
            if (!on[i]) continue;
            int x = wx[i], y = wy[i] >> 8;
            String w = word[i];
            boolean tgt = i == target;
            if (tgt) {
                g.setColor(0x123A5E);
                g.fillRect(x - 2, y - 1, f.stringWidth(w) + 4, f.getHeight() + 1);
            }
            g.setFont(f);
            g.setColor(0x4CAF50);
            g.drawSubstring(w, 0, typed[i], x, y, Gfx.TL);
            g.setColor(tgt ? 0xFFFFFF : 0x90A4AE);
            g.drawSubstring(w, typed[i], w.length() - typed[i], x + f.substringWidth(w, 0, typed[i]), y, Gfx.TL);
        }
        g.setColor(0xEF5350);
        g.drawLine(0, gy, W, gy);
        // keypad legend
        int ph = padH(), py = H - ph;
        g.setColor(0x0E1B2E);
        g.fillRect(0, py, W, ph);
        int cw = W / 3, rh = Gfx.SMALL.getHeight() + 1;
        g.setFont(Gfx.SMALL);
        for (int k = 1; k <= 9; k++) {
            int cx = ((k - 1) % 3) * cw, cy = py + 1 + ((k - 1) / 3) * rh;
            boolean active = tapKey == k;
            if (active) {
                g.setColor(0x00BBF9);
                g.fillRect(cx + 1, cy, cw - 2, rh);
            }
            g.setColor(active ? 0x000000 : (k == 1 ? 0x37474F : 0xB0BEC5));
            String label = k + (KEYS[k].length() > 0 ? " " + KEYS[k] : "");
            if (k == 1) label = "*:menu";
            g.drawString(label, cx + cw / 2, cy, Gfx.TC);
        }
        // HUD
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(String.valueOf(score), 2, 1, Gfx.TL);
        g.setColor(0xFF8A80);
        g.drawString("x" + lives, W - 2, 1, Gfx.TR);
        if (combo > 1) {
            g.setColor(0xFFD54F);
            g.drawString("COMBO " + combo, W / 2, 1, Gfx.TC);
        }
        if (tapKey >= 0) {
            String cyc = KEYS[tapKey];
            int x = W / 2 - f.stringWidth(cyc) / 2;
            for (int i = 0; i < cyc.length(); i++) {
                g.setColor(i == tapIdx ? 0xFFEB3B : 0x546E7A);
                g.drawChar(cyc.charAt(i), x + i * f.charWidth('W'), gy - f.getHeight() - 2, Gfx.TL);
            }
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        String demo = "RETRO";
        int t = (clock / 8) % (demo.length() + 4);
        Font f = Gfx.LARGE.stringWidth(demo) < w ? Gfx.LARGE : Gfx.MEDIUM;
        int tx = x + (w - f.stringWidth(demo)) / 2;
        g.setFont(f);
        for (int i = 0; i < demo.length(); i++) {
            g.setColor(i < t ? 0x4CAF50 : 0x546E7A);
            g.drawChar(demo.charAt(i), tx + i * f.charWidth('W'), y + (h - f.getHeight()) / 2, Gfx.TL);
        }
        g.setFont(Gfx.SMALL);
        g.setColor(0x90A4AE);
        g.drawString("7777 = S", x + w / 2, y + h - Gfx.SMALL.getHeight(), Gfx.TC);
    }
}
