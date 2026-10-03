package racehome;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Race Home: a cross-and-circle race game. Roll the die, bring tokens out of
 * base with a 6, race them once round the 28-square track and up your home
 * lane. Land on a rival to send them back to base.
 */
public class RaceGame extends Game {
    private static final int TRACK = 28, LANE = 4, TOK = 3, FINISH = TRACK + LANE; // position 0..31, -1 base, 32 done
    private static final int[] COL = { 0xFDD835, 0xE53935, 0x43A047, 0x1E88E5 };
    private static final String[] NAMES = { "You", "Red", "Green", "Blue" };
    private final int[][] pos = new int[4][TOK];
    private int players, turn, die, phase, sel, wait, sixes;
    private String note = "";
    private static final int ROLL = 0, PICK = 1, ANIM = 2;

    protected String name() { return "Race Home"; }

    protected String[] help() {
        return new String[] {
            "Get all three of your yellow tokens round the board and into your home lane before the others.",
            "Press 5 to roll. A 6 brings a token out of base and earns another roll. Choose which token to move with 4/6 and press 5.",
            "Land exactly on a rival to send it back to its base. Starred squares are safe. Tokens enter the home lane after one full lap.",
            "- Controls",
            "5: roll / move  4/6: choose token",
        };
    }

    protected String[] modes() { return new String[] { "vs 1 CPU", "vs 3 CPUs" }; }

    protected int accent() { return 0xFDD835; }

    protected void newGame() {
        players = mode == 0 ? 2 : 4;
        for (int p = 0; p < 4; p++) for (int t = 0; t < TOK; t++) pos[p][t] = -1;
        turn = 0;
        phase = ROLL;
        die = 0;
        note = "Press 5 to roll";
    }

    private int start(int p) { return (players == 2 ? p * 2 : p) * 7; }

    /** Absolute track square of player p's token at relative position r (0..27). */
    private int abs(int p, int r) { return (start(p) + r) % TRACK; }

    private boolean safe(int square) { return square % 7 == 0 || square % 7 == 4; }

    private boolean canMove(int p, int t) {
        int r = pos[p][t];
        if (r == FINISH) return false;
        if (r < 0) return die == 6;
        return true;
    }

    private boolean anyMove(int p) {
        for (int t = 0; t < TOK; t++) if (canMove(p, t)) return true;
        return false;
    }

    private void move(int p, int t) {
        int r = pos[p][t];
        if (r < 0) r = 0;
        else r = Math.min(FINISH, r + die);
        pos[p][t] = r;
        Sfx.tone(60 + p * 4, 25);
        if (r < TRACK) {
            int sq = abs(p, r);
            if (!safe(sq)) {
                for (int q = 0; q < players; q++) {
                    if (q == p) continue;
                    for (int k = 0; k < TOK; k++) {
                        int rq = pos[q][k];
                        if (rq >= 0 && rq < TRACK && abs(q, rq) == sq) {
                            pos[q][k] = -1;
                            note = NAMES[p] + " captures " + NAMES[q] + "!";
                            if (p == 0) { score += 30; Sfx.good(); } else Sfx.bad();
                        }
                    }
                }
            }
        } else if (r == FINISH && p == 0) {
            score += 50;
            Sfx.good();
        }
        int done = 0;
        for (int k = 0; k < TOK; k++) if (pos[p][k] == FINISH) done++;
        if (done == TOK) {
            if (p == 0) {
                score += 300;
                headline = "ALL HOME!";
                endGame(true);
            } else {
                headline = NAMES[p] + " WINS";
                endGame(false);
            }
            return;
        }
        endTurn();
    }

    private void endTurn() {
        if (die == 6 && sixes < 2) {
            sixes++;
            phase = ROLL;
            if (turn == 0) note = "Six! Roll again";
            return;
        }
        sixes = 0;
        turn = (turn + 1) % players;
        phase = ROLL;
        wait = 0;
        if (turn == 0) note = "Your roll";
    }

    private void roll() {
        die = Rnd.range(1, 6);
        Sfx.tone(80, 20);
        if (!anyMove(turn)) {
            if (turn == 0) note = "Rolled " + die + " - no move";
            endTurn();
            return;
        }
        phase = PICK;
        sel = 0;
        while (!canMove(turn, sel)) sel = (sel + 1) % TOK;
        if (turn == 0) note = "Rolled " + die + " - pick token";
    }

    private int cpuChoice(int p) {
        int best = -1, bestV = -1000;
        for (int t = 0; t < TOK; t++) {
            if (!canMove(p, t)) continue;
            int r = pos[p][t];
            int nr = r < 0 ? 0 : Math.min(FINISH, r + die);
            int v = nr;
            if (r < 0) v += 40;
            if (nr == FINISH) v += 60;
            if (nr < TRACK) {
                int sq = abs(p, nr);
                for (int q = 0; q < players; q++) {
                    if (q == p) continue;
                    for (int k = 0; k < TOK; k++) {
                        int rq = pos[q][k];
                        if (rq >= 0 && rq < TRACK && abs(q, rq) == sq && !safe(sq)) v += 100;
                    }
                }
                if (safe(sq)) v += 8;
            }
            if (v > bestV) {
                bestV = v;
                best = t;
            }
        }
        return best;
    }

    protected void update() {
        if (turn != 0) {
            if (++wait < 12) return;
            wait = 0;
            if (phase == ROLL) roll();
            else if (phase == PICK) move(turn, cpuChoice(turn));
            return;
        }
        if (phase == ROLL) {
            if ((pressed & K_FIRE) != 0) roll();
        } else if (phase == PICK) {
            if ((pressed & K_RIGHT) != 0) do sel = (sel + 1) % TOK; while (!canMove(0, sel));
            if ((pressed & K_LEFT) != 0) do sel = (sel + TOK - 1) % TOK; while (!canMove(0, sel));
            if ((pressed & K_FIRE) != 0) move(0, sel);
        }
    }

    /** Home lanes run diagonally inward from each corner start square. */
    private static int laneDX(int s) { return s == 0 || s == 21 ? 1 : -1; }

    private static int laneDY(int s) { return s == 0 || s == 7 ? 1 : -1; }

    /** Screen position of track square sq on the perimeter of a 8x8 grid. */
    private int sqX(int sq, int ox, int c) {
        if (sq < 7) return ox + sq * c;
        if (sq < 14) return ox + 7 * c;
        if (sq < 21) return ox + (21 - sq) * c;
        return ox;
    }

    private int sqY(int sq, int oy, int c) {
        if (sq < 7) return oy;
        if (sq < 14) return oy + (sq - 7) * c;
        if (sq < 21) return oy + 7 * c;
        return oy + (28 - sq) * c;
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int c = Math.max(8, Math.min(W - 4, H - hud * 3) / 8);
        int ox = (W - c * 8) / 2, oy = hud + (H - hud * 3 - c * 8) / 2;
        g.setColor(0x263238);
        g.fillRect(0, 0, W, H);
        g.setColor(0x37474F);
        g.fillRect(ox + c, oy + c, c * 6, c * 6);
        for (int sq = 0; sq < TRACK; sq++) {
            int x = sqX(sq, ox, c), y = sqY(sq, oy, c);
            int owner = -1;
            for (int p = 0; p < players; p++) if (start(p) == sq) owner = p;
            g.setColor(owner >= 0 ? Gfx.shade(COL[owner], -20) : 0xECEFF1);
            g.fillRect(x + 1, y + 1, c - 2, c - 2);
            if (safe(sq) && owner < 0) {
                g.setColor(0xB0BEC5);
                g.drawString("*", x + c / 2, y + 1, Gfx.TC);
            }
        }
        // home lanes toward the centre
        for (int p = 0; p < players; p++) {
            int s = start(p);
            int sx = sqX(s, ox, c), sy = sqY(s, oy, c);
            int dx = laneDX(s), dy = laneDY(s);
            for (int k = 1; k <= LANE; k++) {
                g.setColor(Gfx.shade(COL[p], -45));
                g.fillRect(sx + dx * k * c * 3 / 4 + 2, sy + dy * k * c * 3 / 4 + 2, c - 4, c - 4);
            }
        }
        // tokens
        for (int p = 0; p < players; p++) {
            for (int t = 0; t < TOK; t++) {
                int r = pos[p][t];
                int x, y;
                if (r < 0) {
                    // base: corner area inside the board
                    x = ox + c + (p % 2) * c * 4 + t * c / 2 + c / 2;
                    y = oy + c + (p / 2) * c * 4 + c;
                    if (players == 2) { x = ox + c + p * c * 4 + t * c / 2 + c / 2; y = oy + c + p * c * 3 + c; }
                } else if (r >= TRACK) {
                    int s = start(p);
                    int k = r - TRACK + 1;
                    int dx = laneDX(s), dy = laneDY(s);
                    x = sqX(s, ox, c) + dx * k * c * 3 / 4 + c / 2;
                    y = sqY(s, oy, c) + dy * k * c * 3 / 4 + c / 2;
                } else {
                    int sq = abs(p, r);
                    x = sqX(sq, ox, c) + c / 2 + (t - 1) * 2;
                    y = sqY(sq, oy, c) + c / 2;
                }
                g.setColor(0x000000);
                Gfx.disc(g, x, y + 1, c / 3);
                g.setColor(COL[p]);
                Gfx.disc(g, x, y, c / 3);
                if (p == 0 && turn == 0 && phase == PICK && t == sel) {
                    g.setColor((clock & 4) == 0 ? 0xFFFFFF : 0x000000);
                    Gfx.ring(g, x, y, c / 3 + 2);
                }
            }
        }
        // die
        int dsz = Math.max(12, c);
        int dx0 = W / 2 - dsz / 2, dy0 = oy + c * 4 - dsz / 2;
        g.setColor(0xFFFFFF);
        g.fillRoundRect(dx0, dy0, dsz, dsz, 4, 4);
        g.setColor(0x000000);
        if (die > 0) g.drawString(String.valueOf(die), W / 2, dy0 + (dsz - 7) / 2, Gfx.TC);
        g.setFont(Gfx.SMALL_B);
        g.setColor(COL[turn]);
        g.drawString(NAMES[turn] + "'s turn", 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString(String.valueOf(score), W - 2, 1, Gfx.TR);
        Gfx.text(g, note, W / 2, H - hud, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int d = 1 + (clock / 6) % 6;
        int s = Math.max(12, h - 6);
        g.setColor(0xFFFFFF);
        g.fillRoundRect(x + w / 2 - s / 2, y + 3, s, s, 6, 6);
        g.setColor(0x000000);
        g.setFont(Gfx.MEDIUM);
        g.drawString(String.valueOf(d), x + w / 2, y + 3 + (s - 8) / 2, Gfx.TC);
        for (int p = 0; p < 4; p++) {
            g.setColor(COL[p]);
            Gfx.disc(g, x + 8 + p * 10, y + h - 6, 4);
        }
    }
}
