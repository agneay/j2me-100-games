package lakefishing;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/** Lake Fishing: cast, wait for a bite, strike, then fight the fish on a tension meter. */
public class FishingGame extends Game {
    private static final String[] FISH = { "Minnow", "Perch", "Bass", "Pike", "Catfish", "Golden carp" };
    private static final int[] FISH_COLOR = { 0xB0BEC5, 0x9CCC65, 0x558B2F, 0x827717, 0x6D4C41, 0xFFC107 };
    private static final int[] MIN_W = { 20, 120, 400, 900, 1500, 800 };   // grams
    private static final int[] MAX_W = { 90, 450, 2200, 5000, 9000, 3000 };
    private static final int[] FIGHT = { 1, 2, 4, 6, 5, 7 };
    private static final int[] VALUE = { 1, 2, 3, 4, 3, 10 };
    private static final int READY = 0, CAST = 1, FLY = 2, WAIT = 3, BITE = 4, REEL = 5, SHOW = 6;

    private int phase, power, powerDir, castDist, bob, waitT, biteT, fish, weight;
    private int line, tension, slack, clockTime, casts, caught;
    private String msg = "";

    protected String name() { return "Lake Fishing"; }

    protected String[] help() {
        return new String[] {
            "Press 5 to swing, and 5 again to cast: the further out you cast, the deeper the water and the bigger (and fussier) the fish.",
            "When the float bobs under, press 5 quickly to strike. Then hold 5 to reel in. Watch the tension: too high and the line snaps, too slack and the fish slips the hook.",
            "Six species live in the lake, including a very rare golden carp. Catch as much as you can before evening.",
            "- Controls",
            "5: cast / strike / reel (hold)",
        };
    }

    protected String[] modes() { return new String[] { "Day trip (4 min)", "Ten casts" }; }

    protected String formatScore(int s) { return s + " pts"; }

    protected String footer() {
        return saved[0] > 0 ? "Record: " + FISH[saved[1]] + " " + saved[0] + "g" : "J2ME Game Archive";
    }

    protected int accent() { return 0x26A69A; }

    protected void newGame() {
        phase = READY;
        clockTime = 0;
        casts = 0;
        caught = 0;
        msg = "Press 5 to cast";
    }

    private int waterTop() { return H * 45 / 100; }

    private int shoreX() { return W / 5; }

    protected void update() {
        clockTime++;
        if (mode == 0 && clockTime > 4 * 60 * 20 && (phase == READY || phase == WAIT)) {
            headline = "SUNSET - " + caught + " FISH";
            endGame(true);
            return;
        }
        switch (phase) {
            case READY:
                if (mode == 1 && casts >= 10) {
                    headline = caught + " FISH CAUGHT";
                    endGame(true);
                    return;
                }
                if ((pressed & K_FIRE) != 0) {
                    phase = CAST;
                    power = 0;
                    powerDir = 1;
                }
                break;
            case CAST:
                power += powerDir * 4;
                if (power >= 100) { power = 100; powerDir = -1; }
                if (power <= 0) { power = 0; powerDir = 1; }
                if ((pressed & K_FIRE) != 0) {
                    phase = FLY;
                    castDist = 10 + power * 90 / 100;
                    bob = 0;
                    casts++;
                    Sfx.tone(80, 30);
                }
                break;
            case FLY:
                bob += 4;
                if (bob >= castDist) {
                    phase = WAIT;
                    waitT = Rnd.range(30, 140);
                    Sfx.tone(55, 20);
                    msg = "Waiting for a bite...";
                }
                break;
            case WAIT:
                if ((pressed & K_FIRE) != 0) {
                    msg = "Too early! Fish spooked.";
                    phase = READY;
                    Sfx.bad();
                    break;
                }
                if (--waitT <= 0) {
                    pickFish();
                    phase = BITE;
                    biteT = 14 - FIGHT[fish];
                    if (biteT < 6) biteT = 6;
                    Sfx.tone(90, 60);
                    msg = "BITE! Press 5!";
                }
                break;
            case BITE:
                if ((pressed & K_FIRE) != 0) {
                    phase = REEL;
                    line = castDist * 10;
                    tension = 40;
                    slack = 0;
                    msg = "Hooked! Hold 5 to reel";
                    Sfx.good();
                } else if (--biteT <= 0) {
                    msg = "It got away...";
                    phase = READY;
                }
                break;
            case REEL:
                reel();
                break;
            default:
                if ((pressed & K_FIRE) != 0) {
                    phase = READY;
                    msg = "Press 5 to cast";
                }
                break;
        }
    }

    private void pickFish() {
        int depth = castDist; // 10..100
        int r = Rnd.nextInt(1000);
        if (r < 8 + depth / 10) fish = 5;
        else if (depth < 35) fish = Rnd.chance(70) ? 0 : 1;
        else if (depth < 65) fish = Rnd.chance(55) ? 1 : 2;
        else fish = Rnd.chance(40) ? 2 : (Rnd.chance(55) ? 3 : 4);
        weight = Rnd.range(MIN_W[fish], MAX_W[fish]);
        if (clockTime > 3 * 60 * 20 && Rnd.chance(30)) weight = weight * 5 / 4; // dusk feeding
    }

    private void reel() {
        boolean reeling = (held & K_FIRE) != 0;
        int pull = FIGHT[fish] * (weight / 400 + 2);
        int surge = Rnd.chance(8 + FIGHT[fish] * 2) ? pull * 3 : 0;
        if (reeling) {
            tension += 3 + pull / 4 + surge / 3;
            line -= 6 + 30 / (FIGHT[fish] + 2);
            if (frame % 4 == 0) Sfx.tone(40 + tension / 4, 20);
        } else {
            tension -= 5;
            line += pull / 6 + surge / 4;
        }
        tension += (surge > 0) ? surge / 2 : 0;
        tension = FMath.clamp(tension, 0, 120);
        if (tension >= 100) {
            msg = "SNAP! The line broke.";
            phase = SHOW;
            fish = -1;
            Sfx.bad();
            return;
        }
        if (tension < 12) slack++;
        else slack = 0;
        if (slack > 30) {
            msg = "Too slack - it slipped off.";
            phase = SHOW;
            fish = -1;
            Sfx.bad();
            return;
        }
        if (line > castDist * 12) line = castDist * 12;
        if (line <= 0) {
            caught++;
            int pts = weight * VALUE[fish] / 50 + 5;
            score += pts;
            msg = FISH[fish] + " " + weight + "g  +" + pts;
            if (weight * VALUE[fish] > saved[0] * VALUE[saved[1]]) {
                saved[0] = weight;
                saved[1] = fish;
                msg = "NEW RECORD! " + msg;
            }
            phase = SHOW;
            Sfx.win();
        }
    }

    protected void draw(Graphics g) {
        int wt = waterTop();
        int day = Math.min(255, clockTime * 256 / (4 * 60 * 20));
        int sky = mode == 0 ? Gfx.mix(0x81D4FA, 0xFF8A65, day) : 0x81D4FA;
        g.setColor(sky);
        g.fillRect(0, 0, W, wt);
        g.setColor(mode == 0 ? Gfx.mix(0xFFF59D, 0xFF7043, day) : 0xFFF59D);
        Gfx.disc(g, W * 3 / 4, wt / 3 + day * wt / 512, Math.max(6, W / 16));
        g.setColor(0x4E7D5B);
        g.fillTriangle(W / 3, wt, W / 2 + W / 6, wt - wt / 4, W, wt);
        g.setColor(0x1565C0);
        g.fillRect(0, wt, W, H - wt);
        g.setColor(0x1976D2);
        for (int y = wt + 4; y < H; y += 6) {
            int off = (clock + y) % 12;
            for (int x = -12 + off; x < W; x += 24) g.fillRect(x, y, 8, 1);
        }
        // depth shading
        g.setColor(0x0D47A1);
        g.fillRect(0, H - (H - wt) / 3, W, (H - wt) / 3);
        // dock and angler
        int sx = shoreX();
        g.setColor(0x6D4C41);
        g.fillRect(0, wt - 4, sx, 5);
        g.setColor(0x4E342E);
        for (int k = 4; k < sx; k += 10) g.fillRect(k, wt, 2, H / 10);
        int ax = sx - 8, ay = wt - 4;
        g.setColor(0x1E88E5);
        g.fillRect(ax - 3, ay - 12, 6, 8);
        g.setColor(0xFFCC80);
        Gfx.disc(g, ax, ay - 15, 3);
        g.setColor(0x795548);
        g.fillRect(ax - 4, ay - 19, 8, 2);
        g.setColor(0x37474F);
        g.fillRect(ax - 3, ay - 4, 2, 4);
        g.fillRect(ax + 1, ay - 4, 2, 4);
        // rod
        int tipX = ax + 14, tipY = ay - 26;
        if (phase == CAST) { tipX = ax - 6 + power / 10; tipY = ay - 30; }
        if (phase == REEL) tipY += tension / 10;
        g.setColor(0x3E2723);
        g.drawLine(ax + 2, ay - 9, tipX, tipY);
        // line and float
        if (phase >= FLY && !(phase == SHOW && fish < 0)) {
            int dist = phase == REEL ? Math.max(0, line / 10) : (phase == SHOW ? 0 : Math.min(bob, castDist));
            int fx = sx + 4 + (W - sx - 10) * dist / 100;
            int fy = wt;
            if (phase == FLY) fy = wt - (castDist - dist) * dist * (wt / 3) / Math.max(1, castDist * castDist / 4);
            if (phase == WAIT) fy = wt + ((clock / 8) & 1);
            if (phase == BITE) fy = wt + 4;
            g.setColor(0xECEFF1);
            g.drawLine(tipX, tipY, fx, fy);
            if (phase != REEL && phase != SHOW) {
                g.setColor(0xFF1744);
                Gfx.disc(g, fx, fy - 1, 2);
                g.setColor(0xFFFFFF);
                g.fillRect(fx - 1, fy - 4, 2, 2);
            }
            if (phase == REEL) {
                int fishY = wt + 6 + (H - wt) * Math.min(castDist, 100) / 200;
                int wig = FMath.sin(clock * 20) * 3 >> 10;
                g.setColor(FISH_COLOR[fish]);
                int len = 8 + weight / 600;
                g.fillArc(fx - len / 2 + wig, fishY - 3, len, 6, 0, 360);
                g.fillTriangle(fx + len / 2 + wig, fishY, fx + len / 2 + 4 + wig, fishY - 3, fx + len / 2 + 4 + wig, fishY + 3);
                g.setColor(0xECEFF1);
                g.drawLine(fx, fy, fx + wig - len / 2, fishY);
            }
        }
        if (phase == SHOW && fish >= 0) {
            g.setColor(FISH_COLOR[fish]);
            int len = 14 + weight / 300;
            g.fillArc(ax + 6, ay - 22, len, 8, 0, 360);
            g.fillTriangle(ax + 6 + len, ay - 18, ax + 10 + len, ay - 22, ax + 10 + len, ay - 14);
        }
        // HUD
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x004D40);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(score + " pts", 2, 1, Gfx.TL);
        g.setColor(0xB2DFDB);
        String right = mode == 0 ? Gfx.time(Math.max(0, 4 * 60 * 20 - clockTime), tickMs) : "Cast " + casts + "/10";
        g.drawString(right, W - 2, 1, Gfx.TR);
        if (phase == CAST) Gfx.bar(g, W / 6, hud + 4, W * 2 / 3, 6, power, 100, 0xFFCA28, 0x263238);
        if (phase == REEL) {
            int bw = W * 2 / 3;
            Gfx.bar(g, W / 6, hud + 4, bw, 6, tension, 100, tension > 80 ? 0xFF1744 : (tension < 15 ? 0x90A4AE : 0x66BB6A), 0x263238);
            g.setColor(0xFFFFFF);
            g.fillRect(W / 6 + bw * 80 / 100, hud + 3, 1, 8);
            g.fillRect(W / 6 + bw * 15 / 100, hud + 3, 1, 8);
        }
        Gfx.hint(g, msg, W, H);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x1565C0);
        g.fillRect(x, y + h / 2, w, h / 2);
        int t = clock % 60;
        int fx = x + w / 2 + (FMath.sin(clock * 4) * (w / 4) >> 10);
        int fy = y + h * 3 / 4;
        g.setColor(FISH_COLOR[(clock / 60) % 6]);
        g.fillArc(fx - 8, fy - 3, 16, 7, 0, 360);
        g.fillTriangle(fx + 8, fy, fx + 13, fy - 4, fx + 13, fy + 4);
        g.setColor(0xFF1744);
        Gfx.disc(g, x + w / 2, y + h / 2 + (t < 30 ? 0 : 2), 2);
        g.setColor(0xECEFF1);
        g.drawLine(x + 6, y + 4, x + w / 2, y + h / 2);
    }
}
