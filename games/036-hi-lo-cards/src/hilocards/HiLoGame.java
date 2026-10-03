package hilocards;

import gamekit.Cards;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Hi-Lo Cards: climb a ladder of seven cards by guessing whether the next
 * card is higher or lower. Bank your winnings or risk them for more.
 */
public class HiLoGame extends Game {
    private static final int LADDER = 7, ROUNDS = 10;
    private static final int[] PRIZE = { 0, 10, 20, 40, 80, 160, 320, 1000 };

    private final int[] deck = new int[52];
    private int deckPos;
    private final int[] ladder = new int[LADDER + 1];
    private int step, round, phase, reveal, swapsLeft, lastGuess;
    private String msg = "";
    private static final int GUESS = 0, REVEAL = 1, BUST = 2, BANKED = 3;

    protected String name() { return "Hi-Lo Cards"; }

    protected String[] help() {
        return new String[] {
            "Each round deals a ladder of cards. Guess whether the next card will be HIGHER or LOWER than the current one. Aces are high; an equal card counts as a correct guess.",
            "Every correct guess climbs one rung and doubles the prize. Bank at any time to keep the prize, or keep going - one wrong guess loses the round's prize. Reach the top for 1000.",
            "Once per round you may swap an awkward middle card for a new one. The odds shown use the cards still in the deck.",
            "- Controls",
            "2: higher  8: lower",
            "5: bank prize  #: swap card",
        };
    }

    protected int accent() { return 0x43AA8B; }

    protected void newGame() {
        round = 0;
        Cards.shuffle(deck);
        deckPos = 0;
        startRound();
    }

    private int drawCard() {
        if (deckPos >= 52) {
            Cards.shuffle(deck);
            deckPos = 0;
        }
        return deck[deckPos++];
    }

    private void startRound() {
        round++;
        step = 0;
        swapsLeft = 1;
        ladder[0] = drawCard();
        phase = GUESS;
        msg = "Round " + round + "/" + ROUNDS;
    }

    private static int value(int c) {
        int r = Cards.rank(c);
        return r == 0 ? 14 : r + 1;
    }

    /** Chance (percent) the next card is higher-or-equal, from the remaining deck. */
    private int higherOdds() {
        int v = value(ladder[step]), hi = 0, tot = 0;
        for (int i = deckPos; i < 52; i++) {
            tot++;
            if (value(deck[i]) >= v) hi++;
        }
        return tot == 0 ? 50 : hi * 100 / tot;
    }

    private int lowerOdds() {
        int v = value(ladder[step]), lo = 0, tot = 0;
        for (int i = deckPos; i < 52; i++) {
            tot++;
            if (value(deck[i]) <= v) lo++;
        }
        return tot == 0 ? 50 : lo * 100 / tot;
    }

    protected void update() {
        if (reveal > 0) {
            reveal--;
            return;
        }
        switch (phase) {
            case GUESS: {
                int guess = 0;
                if ((pressed & K_UP) != 0) guess = 1;
                if ((pressed & K_DOWN) != 0) guess = -1;
                if (guess != 0) {
                    lastGuess = guess;
                    int next = drawCard();
                    ladder[step + 1] = next;
                    int a = value(ladder[step]), b = value(next);
                    boolean ok = guess > 0 ? b >= a : b <= a;
                    reveal = 8;
                    if (ok) {
                        step++;
                        Sfx.good();
                        if (step == LADDER) {
                            score += PRIZE[LADDER];
                            msg = "TOP OF THE LADDER! +" + PRIZE[LADDER];
                            phase = BANKED;
                            Sfx.win();
                        } else {
                            msg = "Correct! Prize " + PRIZE[step];
                        }
                    } else {
                        msg = "Wrong - prize lost";
                        phase = BUST;
                        Sfx.bad();
                    }
                } else if ((pressed & K_FIRE) != 0 && step > 0) {
                    score += PRIZE[step];
                    msg = "Banked " + PRIZE[step];
                    phase = BANKED;
                    Sfx.tone(88, 60);
                } else if ((pressed & K_POUND) != 0 && swapsLeft > 0 && step > 0) {
                    swapsLeft--;
                    ladder[step] = drawCard();
                    msg = "Card swapped";
                    Sfx.click();
                }
                break;
            }
            default:
                if ((pressed & K_FIRE) != 0) {
                    if (round >= ROUNDS) {
                        endGame(score > 0);
                        return;
                    }
                    startRound();
                }
                break;
        }
    }

    protected void draw(Graphics g) {
        Cards.felt(g, W, H);
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x0E3B1F);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString("Bank " + score, 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString("R" + round + "/" + ROUNDS, W - 2, 1, Gfx.TR);
        // ladder of slots
        int cw = Math.max(12, Math.min((W - 6) / LADDER - 2, (H / 3) * 3 / 4));
        int ch = Cards.heightFor(cw);
        int lx = (W - LADDER * (cw + 2)) / 2;
        int ly = hud + 4;
        for (int i = 0; i < LADDER; i++) {
            int x = lx + i * (cw + 2);
            if (i + 1 <= step) Cards.draw(g, ladder[i + 1], x, ly, cw, ch, true);
            else if (i + 1 == step + 1 && phase == BUST) Cards.draw(g, ladder[step + 1], x, ly, cw, ch, true);
            else Cards.drawBack(g, x, ly, cw, ch);
            g.setFont(Gfx.SMALL);
            g.setColor(i < step ? 0xFFD54F : 0xBFE6CC);
            String p = String.valueOf(PRIZE[i + 1]);
            if (Gfx.SMALL.stringWidth(p) > cw + 1) p = p.length() > 3 ? "1k" : p;
            g.drawString(p, x + cw / 2, ly + ch + 1, Gfx.TC);
        }
        // current card, big; odds on the left, keys on the right
        int by = ly + ch + hud + 2;
        int bw = Math.max(20, Math.min(W / 3, (H - by - hud * 2 - 4) * 3 / 4));
        int bh = Cards.heightFor(bw);
        int bx = (W - bw) / 2;
        Cards.draw(g, ladder[step], bx, by, bw, bh, true);
        if (phase == GUESS) {
            g.setFont(Gfx.SMALL_B);
            g.setColor(0x9CFF9C);
            g.drawString("2 HI", bx + bw + 4, by + 2, Gfx.TL);
            g.setColor(0xFF9C9C);
            g.drawString("8 LO", bx + bw + 4, by + bh - Gfx.SMALL_B.getHeight(), Gfx.TL);
            g.setFont(Gfx.SMALL);
            g.setColor(0xFFFFFF);
            g.drawString(higherOdds() + "%", bx - 4, by + 2, Gfx.TR);
            g.drawString(lowerOdds() + "%", bx - 4, by + bh - Gfx.SMALL.getHeight(), Gfx.TR);
            String opts = (step > 0 ? "5 bank " + PRIZE[step] : "") + (swapsLeft > 0 && step > 0 ? "  # swap" : "");
            Gfx.text(g, reveal > 0 ? msg : opts, W / 2, H - hud, Gfx.TC, Gfx.SMALL, 0xBFE6CC);
        } else {
            Gfx.text(g, msg, W / 2, by + bh + 2, Gfx.TC, Gfx.SMALL_B, phase == BUST ? 0xFF8A80 : 0xFFD54F);
            Gfx.text(g, round >= ROUNDS ? "5: finish" : "5: next round", W / 2, H - hud, Gfx.TC, Gfx.SMALL, 0xBFE6CC);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int cw = Math.max(14, Math.min(w / 5, h * 3 / 5));
        int ch = Cards.heightFor(cw);
        int k = (clock / 20) % 13;
        Cards.draw(g, k, x + w / 2 - cw - 4, y + (h - ch) / 2, cw, ch, true);
        Cards.draw(g, 13 + (k + 3) % 13, x + w / 2 + 4, y + (h - ch) / 2, cw, ch, (clock / 10) % 2 == 0);
        g.setColor(0xFFD54F);
        g.fillTriangle(x + w / 2, y + 2, x + w / 2 - 4, y + 8, x + w / 2 + 4, y + 8);
        g.fillTriangle(x + w / 2, y + h - 2, x + w / 2 - 4, y + h - 8, x + w / 2 + 4, y + h - 8);
    }
}
