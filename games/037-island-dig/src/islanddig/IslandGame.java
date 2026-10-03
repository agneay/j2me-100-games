package islanddig;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Island Dig: a treasure hunt. Walk the island with a metal detector,
 * use compass clues wisely and dig up three chests before your energy runs out.
 */
public class IslandGame extends Game {
    private static final int N = 14, CHESTS = 3, MAXC = 4;
    private static final int WATER = 0, SAND = 1, GRASS = 2, PALM = 3, ROCK = 4;

    private final byte[] land = new byte[N * N];
    private final boolean[] dug = new boolean[N * N];
    private final int[] chest = new int[CHESTS];
    private final boolean[] found = new boolean[CHESTS];
    private final int[] crabX = new int[MAXC], crabY = new int[MAXC];
    private int px, py, energy, compass, nFound, signal, arrow = -1, arrowT, msgT;
    private String msg = "";
    private Image hero;

    protected String name() { return "Island Dig"; }

    protected String[] help() {
        return new String[] {
            "Three chests are buried somewhere on the island. Walk around: your metal detector bar fills up the closer you are to a chest you haven't found.",
            "Press 5 to dig where you stand. Press # to read the compass, which points to the nearest chest - but you only get three readings.",
            "Walking costs 1 energy, digging 3. Crabs pinch! Find all three chests before you run out of energy.",
            "- Controls",
            "2/4/6/8: walk",
            "5: dig  #: compass",
        };
    }

    protected String[] modes() { return new String[] { "Normal", "Hard (less energy)" }; }

    protected int accent() { return 0xF4A261; }

    protected void newGame() {
        int c = N / 2;
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                int dx = x - c, dy = y - c;
                int d = dx * dx + dy * dy + Rnd.nextInt(10);
                int v = d > 46 ? WATER : (d > 30 ? SAND : GRASS);
                if (v == GRASS && Rnd.chance(12)) v = PALM;
                if (v != WATER && Rnd.chance(5)) v = ROCK;
                land[y * N + x] = (byte) v;
                dug[y * N + x] = false;
            }
        }
        land[c * N + c] = GRASS;
        floodFrom(c * N + c);
        int open = 0;
        for (int i = 0; i < N * N; i++) if (reach[i]) open++;
        if (open < 30) { // walled-in start: roll a new island
            newGame();
            return;
        }
        for (int k = 0; k < CHESTS; k++) {
            int i;
            do {
                i = Rnd.nextInt(N * N);
            } while (!reach[i] || i == c * N + c || isChest(i, k));
            chest[k] = i;
            found[k] = false;
        }
        for (int k = 0; k < MAXC; k++) {
            int i;
            do {
                i = Rnd.nextInt(N * N);
            } while (land[i] != SAND);
            crabX[k] = i % N;
            crabY[k] = i / N;
        }
        px = py = c;
        land[c * N + c] = GRASS;
        energy = mode == 0 ? 160 : 110;
        compass = 3;
        nFound = 0;
        arrow = -1;
        updateSignal();
        msg = "Find 3 chests!";
        msgT = 40;
    }

    private final boolean[] reach = new boolean[N * N];
    private final int[] stack = new int[N * N];

    /** Mark every walkable tile reachable from the start, so no chest is boxed in. */
    private void floodFrom(int start) {
        for (int i = 0; i < N * N; i++) reach[i] = false;
        int sp = 0;
        stack[sp++] = start;
        reach[start] = true;
        while (sp > 0) {
            int i = stack[--sp];
            int x = i % N, y = i / N;
            for (int d = 0; d < 4; d++) {
                int nx = x + (d == 0 ? 1 : (d == 1 ? -1 : 0)), ny = y + (d == 2 ? 1 : (d == 3 ? -1 : 0));
                if (nx < 0 || ny < 0 || nx >= N || ny >= N) continue;
                int j = ny * N + nx;
                int t = land[j];
                if (reach[j] || t == WATER || t == ROCK || t == PALM) continue;
                reach[j] = true;
                stack[sp++] = j;
            }
        }
    }

    private boolean isChest(int i, int upto) {
        for (int k = 0; k < upto; k++) if (chest[k] == i) return true;
        return false;
    }

    private int nearest() {
        int best = -1, bd = 9999;
        for (int k = 0; k < CHESTS; k++) {
            if (found[k]) continue;
            int dx = chest[k] % N - px, dy = chest[k] / N - py;
            int d = dx * dx + dy * dy;
            if (d < bd) {
                bd = d;
                best = k;
            }
        }
        return best;
    }

    private void updateSignal() {
        int k = nearest();
        if (k < 0) {
            signal = 0;
            return;
        }
        int dx = chest[k] % N - px, dy = chest[k] / N - py;
        int d = FMath.sqrt((dx * dx + dy * dy) * 100);
        signal = Math.max(0, 100 - d * 100 / 70);
        if (signal > 85) Sfx.tone(96, 40);
        else if (signal > 60) Sfx.tone(84, 25);
    }

    protected void update() {
        if (msgT > 0) msgT--;
        if (arrowT > 0) arrowT--;
        int dx = 0, dy = 0;
        if ((pressed & K_LEFT) != 0) dx = -1;
        else if ((pressed & K_RIGHT) != 0) dx = 1;
        else if ((pressed & K_UP) != 0) dy = -1;
        else if ((pressed & K_DOWN) != 0) dy = 1;
        if (dx != 0 || dy != 0) {
            int nx = px + dx, ny = py + dy;
            if (nx >= 0 && ny >= 0 && nx < N && ny < N) {
                int t = land[ny * N + nx];
                if (t == WATER) say("Too deep to wade.");
                else if (t == ROCK || t == PALM) say(t == ROCK ? "A boulder blocks the way." : "A palm tree.");
                else {
                    px = nx;
                    py = ny;
                    energy--;
                    moveCrabs();
                    updateSignal();
                }
            }
        } else if ((pressed & K_FIRE) != 0) {
            dig();
        } else if ((pressed & K_POUND) != 0) {
            if (compass > 0) {
                int k = nearest();
                if (k >= 0) {
                    compass--;
                    arrow = FMath.atan2(chest[k] / N - py, chest[k] % N - px);
                    arrowT = 60;
                    say("The compass needle swings...");
                }
            } else say("The compass is broken.");
        }
        if (energy <= 0) {
            energy = 0;
            headline = "EXHAUSTED";
            endGame(false);
        }
    }

    private void say(String s) {
        msg = s;
        msgT = 40;
    }

    private void dig() {
        int i = py * N + px;
        if (dug[i]) {
            say("Already dug here.");
            return;
        }
        dug[i] = true;
        energy -= 3;
        for (int k = 0; k < CHESTS; k++) {
            if (!found[k] && chest[k] == i) {
                found[k] = true;
                nFound++;
                score += 300 + energy;
                say("TREASURE! " + nFound + "/3");
                Sfx.win();
                updateSignal();
                if (nFound == CHESTS) {
                    score += energy * 3;
                    headline = "ALL TREASURE!";
                    endGame(true);
                }
                return;
            }
        }
        int r = Rnd.nextInt(100);
        if (r < 20) {
            score += 15;
            say("A few old coins. +15");
            Sfx.good();
        } else if (r < 28) {
            energy += 8;
            say("A coconut! +8 energy");
            Sfx.good();
        } else {
            say(r < 60 ? "Just sand." : (r < 80 ? "A rusty can." : "Seashells."));
            Sfx.tone(45, 30);
        }
    }

    private void moveCrabs() {
        for (int k = 0; k < MAXC; k++) {
            int d = Rnd.nextInt(4);
            int nx = crabX[k] + (d == 0 ? 1 : (d == 1 ? -1 : 0)), ny = crabY[k] + (d == 2 ? 1 : (d == 3 ? -1 : 0));
            if (nx >= 0 && ny >= 0 && nx < N && ny < N && land[ny * N + nx] == SAND) {
                crabX[k] = nx;
                crabY[k] = ny;
            }
            if (crabX[k] == px && crabY[k] == py) {
                energy -= 6;
                say("Ouch! A crab pinch. -6");
                Sfx.bad();
            }
        }
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int s = Math.max(6, Math.min(W / N, (H - hud * 3) / N));
        int ox = (W - s * N) / 2, oy = hud * 2 + (H - hud * 3 - s * N) / 2;
        if (hero == null) {
            hero = Gfx.sprite(new String[] { ".1111.", "122221", ".1331.", ".1441.", "144441", ".1..1." },
                    new int[] { 0, 0x3E2723, 0xC62828, 0xFFCC80, 0x1565C0 }, Math.max(1, s / 7));
        }
        g.setColor(0x0D47A1);
        g.fillRect(0, 0, W, H);
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                int i = y * N + x, t = land[i];
                int X = ox + x * s, Y = oy + y * s;
                switch (t) {
                    case WATER:
                        g.setColor(((x + y + clock / 10) & 3) == 0 ? 0x1E88E5 : 0x1565C0);
                        break;
                    case SAND: g.setColor(0xF2D58B); break;
                    default: g.setColor(0x7CB342); break;
                }
                g.fillRect(X, Y, s, s);
                if (t == PALM) {
                    g.setColor(0x6D4C41);
                    g.fillRect(X + s / 2 - 1, Y + s / 3, 2, s * 2 / 3);
                    g.setColor(0x2E7D32);
                    g.fillTriangle(X + s / 2, Y, X, Y + s / 3, X + s, Y + s / 3);
                } else if (t == ROCK) {
                    g.setColor(0x9E9E9E);
                    Gfx.disc(g, X + s / 2, Y + s / 2, s / 2 - 1);
                    g.setColor(0xBDBDBD);
                    Gfx.disc(g, X + s / 2 - 1, Y + s / 2 - 1, s / 5);
                }
                if (dug[i]) {
                    boolean chestHere = false;
                    for (int k = 0; k < CHESTS; k++) if (found[k] && chest[k] == i) chestHere = true;
                    g.setColor(chestHere ? 0xFFB300 : 0x8D6E63);
                    g.fillArc(X + 1, Y + s / 4, s - 2, s / 2, 0, 360);
                }
            }
        }
        g.setColor(0xE64A19);
        for (int k = 0; k < MAXC; k++) {
            int X = ox + crabX[k] * s, Y = oy + crabY[k] * s;
            g.fillArc(X + s / 5, Y + s / 3, s * 3 / 5, s / 2, 0, 360);
            g.fillRect(X + 1, Y + s / 4, 2, 2);
            g.fillRect(X + s - 3, Y + s / 4, 2, 2);
        }
        g.drawImage(hero, ox + px * s + s / 2, oy + py * s + s / 2, Gfx.CC);
        if (arrow >= 0 && arrowT > 0) {
            int cx = ox + px * s + s / 2, cy = oy + py * s + s / 2, len = s * 2;
            g.setColor(0xFFEB3B);
            g.drawLine(cx, cy, cx + (FMath.cos(arrow) * len >> 10), cy + (FMath.sin(arrow) * len >> 10));
            g.fillTriangle(cx + (FMath.cos(arrow) * (len + 4) >> 10), cy + (FMath.sin(arrow) * (len + 4) >> 10),
                    cx + (FMath.cos(arrow + 20) * len >> 10), cy + (FMath.sin(arrow + 20) * len >> 10),
                    cx + (FMath.cos(arrow - 20) * len >> 10), cy + (FMath.sin(arrow - 20) * len >> 10));
        }
        g.setColor(0x002B4E);
        g.fillRect(0, 0, W, hud * 2 - 2);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFD54F);
        g.drawString("$" + score, 2, 1, Gfx.TL);
        g.setColor(0xFFFFFF);
        g.drawString("Chests " + nFound + "/3", W - 2, 1, Gfx.TR);
        Gfx.bar(g, 2, hud, W / 2 - 4, 5, energy, mode == 0 ? 160 : 110, 0x66BB6A, 0x263238);
        Gfx.bar(g, W / 2 + 2, hud, W / 2 - 4, 5, signal, 100, signal > 70 ? 0xFF5252 : 0xFFCA28, 0x263238);
        g.setFont(Gfx.SMALL);
        g.setColor(0xB3E5FC);
        g.drawString(msgT > 0 ? msg : "Compass x" + compass + "  # to use", W / 2, H - hud + 1, Gfx.TC);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x1565C0);
        g.fillRect(x, y, w, h);
        g.setColor(0xF2D58B);
        g.fillArc(x + w / 6, y + h / 5, w * 2 / 3, h * 3 / 4, 0, 360);
        g.setColor(0x7CB342);
        g.fillArc(x + w / 4, y + h / 3, w / 2, h / 2, 0, 360);
        g.setColor(0xD32F2F);
        int cx = x + w / 2 + w / 8, cy = y + h / 2 + 2;
        g.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
        g.drawLine(cx - 4, cy + 4, cx + 4, cy - 4);
        if ((clock & 8) == 0) {
            g.setColor(0xFFEB3B);
            g.drawArc(cx - 7, cy - 7, 14, 14, 0, 360);
        }
    }
}
