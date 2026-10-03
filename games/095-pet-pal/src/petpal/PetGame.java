package petpal;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Pet Pal: raise a little pixel creature from an egg to an adult. Needs
 * drain over time; neglect hurts its health, and a well cared for pet grows
 * into a happier adult form. One in-game day lasts 30 seconds.
 */
public class PetGame extends Game {
    private static final String[] ACT = { "Feed", "Play", "Clean", "Sleep", "Medicine" };
    private static final String[] STAGE = { "Egg", "Baby", "Child", "Teen", "Adult" };
    private static final int DAY = 600; // ticks
    private static final int ADULT_DAY = 8;

    private int hunger, fun, energy, clean, health, care, age, dayT, stage, sel, mess;
    private boolean asleep, sick;
    private int anim, animKind, gameRound, gameWins, lookDir, gameT;
    private boolean playing;
    private String msg = "";

    protected String name() { return "Pet Pal"; }

    protected String[] help() {
        return new String[] {
            "Hatch an egg and raise your pet to adulthood over eight days. Each day lasts about 30 seconds.",
            "Keep its four needs topped up: food, fun, cleanliness and energy. Low needs hurt its health, and a sick pet needs medicine. Too much food or medicine is bad for it too.",
            "Play is a guessing game: guess which way your pet will look.",
            "- Controls",
            "4/6: choose action",
            "5: do it",
            "in Play: 4 / 6 guess left / right",
        };
    }

    protected int accent() { return 0xF48FB1; }

    protected void newGame() {
        hunger = fun = energy = clean = 80;
        health = 100;
        care = 0;
        age = 0;
        dayT = 0;
        stage = 0;
        sel = 0;
        mess = 0;
        asleep = false;
        sick = false;
        playing = false;
        msg = "An egg! Keep it warm...";
    }

    private int clampStat(int v) {
        return v < 0 ? 0 : (v > 100 ? 100 : v);
    }

    protected void update() {
        if (anim > 0) anim--;
        if (playing) {
            updatePlay();
            return;
        }
        // time
        dayT++;
        if (dayT >= DAY) {
            dayT = 0;
            age++;
            int ns = age >= ADULT_DAY ? 4 : (age >= 5 ? 3 : (age >= 2 ? 2 : (age >= 1 ? 1 : 0)));
            if (ns != stage) {
                stage = ns;
                msg = "It grew into a " + STAGE[stage] + "!";
                Sfx.win();
            }
            score = age * 100 + care;
            if (stage == 4) {
                score += health * 5;
                headline = care > age * 40 ? "A HAPPY ADULT!" : "ALL GROWN UP";
                endGame(true);
                return;
            }
        }
        if (stage == 0) {
            if ((pressed & K_FIRE) != 0) {
                anim = 6;
                animKind = 9;
                msg = "*wobble*";
            }
            return;
        }
        // needs drain
        if (frame % 20 == 0) {
            if (asleep) {
                energy += 4;
                hunger -= 1;
                if (energy >= 100) {
                    asleep = false;
                    msg = "Awake and refreshed";
                }
            } else {
                hunger -= 2;
                fun -= 2;
                energy -= 1;
                clean -= mess > 0 ? 3 : 1;
            }
            hunger = clampStat(hunger);
            fun = clampStat(fun);
            energy = clampStat(energy);
            clean = clampStat(clean);
            int low = 0;
            if (hunger < 20) low++;
            if (fun < 20) low++;
            if (clean < 20) low++;
            if (energy < 10) low++;
            if (sick) low += 2;
            if (low == 0 && health < 100) health++;
            health = clampStat(health - low * 2);
            if (low == 0) care++;
            if (!sick && clean < 30 && Rnd.chance(8)) {
                sick = true;
                msg = "Your pet feels ill";
                Sfx.bad();
            }
            if (!asleep && Rnd.chance(4) && mess < 3) mess++;
            if (health <= 0) {
                headline = "YOUR PET RAN AWAY";
                score = age * 100 + care;
                endGame(false);
                return;
            }
        }
        if ((pressed & K_LEFT) != 0) sel = (sel + ACT.length - 1) % ACT.length;
        if ((pressed & K_RIGHT) != 0) sel = (sel + 1) % ACT.length;
        if ((pressed & K_FIRE) != 0) act();
    }

    private void act() {
        if (asleep && sel != 3) {
            msg = "Shh... it's sleeping";
            Sfx.bad();
            return;
        }
        anim = 10;
        animKind = sel;
        switch (sel) {
            case 0:
                if (hunger > 90) {
                    health = clampStat(health - 5);
                    msg = "Too full! Tummy ache";
                    Sfx.bad();
                } else {
                    hunger = clampStat(hunger + 30);
                    mess = Math.min(3, mess + (Rnd.chance(40) ? 1 : 0));
                    msg = "Yum!";
                    Sfx.good();
                }
                break;
            case 1:
                if (energy < 15) {
                    msg = "Too tired to play";
                    Sfx.bad();
                    break;
                }
                playing = true;
                gameRound = 0;
                gameWins = 0;
                gameT = 0;
                lookDir = 0;
                msg = "Which way will it look?";
                break;
            case 2:
                clean = 100;
                mess = 0;
                msg = "Squeaky clean";
                Sfx.good();
                break;
            case 3:
                asleep = !asleep;
                msg = asleep ? "Lights out. Zzz" : "Wakey wakey";
                Sfx.click();
                break;
            default:
                if (sick) {
                    sick = false;
                    msg = "Feeling better!";
                    Sfx.good();
                } else {
                    health = clampStat(health - 10);
                    msg = "It didn't need that";
                    Sfx.bad();
                }
                break;
        }
    }

    private void updatePlay() {
        gameT++;
        if (lookDir != 0) {
            if (gameT > 12) {
                lookDir = 0;
                gameT = 0;
                if (++gameRound >= 5) {
                    playing = false;
                    fun = clampStat(fun + 8 + gameWins * 8);
                    energy = clampStat(energy - 10);
                    care += gameWins;
                    msg = "Played! won " + gameWins + "/5";
                }
            }
            return;
        }
        int guess = (pressed & K_LEFT) != 0 ? -1 : ((pressed & K_RIGHT) != 0 ? 1 : 0);
        if (guess != 0) {
            lookDir = Rnd.chance(50) ? -1 : 1;
            gameT = 0;
            if (guess == lookDir) {
                gameWins++;
                Sfx.good();
            } else Sfx.bad();
        }
    }

    private void drawPet(Graphics g, int cx, int cy, int s, int look) {
        int bob = (clock / 6) % 2;
        if (stage == 0) {
            int wob = anim > 0 && animKind == 9 ? ((anim & 1) == 0 ? -2 : 2) : 0;
            g.setColor(0xFFF8E1);
            g.fillArc(cx - s / 2 + wob, cy - s * 2 / 3, s, s * 4 / 3, 0, 360);
            g.setColor(0xF48FB1);
            g.fillRect(cx - s / 4 + wob, cy - s / 6, s / 6, s / 6);
            g.fillRect(cx + s / 8 + wob, cy + s / 8, s / 6, s / 6);
            return;
        }
        int size = s * (2 + stage) / 6;
        int col = sick ? 0xAED581 : (stage == 4 && care > age * 40 ? 0xBA68C8 : 0xF06292);
        int y = cy - (asleep ? 0 : bob);
        g.setColor(col);
        g.fillArc(cx - size / 2, y - size / 2, size, size, 0, 360);
        if (stage >= 2) {
            g.fillTriangle(cx - size / 2, y - size / 4, cx - size / 3, y - size * 2 / 3, cx - size / 6, y - size / 3);
            g.fillTriangle(cx + size / 2, y - size / 4, cx + size / 3, y - size * 2 / 3, cx + size / 6, y - size / 3);
        }
        int ex = look * size / 8;
        g.setColor(0x000000);
        if (asleep) {
            g.drawLine(cx - size / 4, y - size / 8, cx - size / 8, y - size / 8);
            g.drawLine(cx + size / 8, y - size / 8, cx + size / 4, y - size / 8);
            Gfx.text(g, "z", cx + size / 2, y - size / 2 - (clock / 4) % 6, Gfx.TL, Gfx.SMALL_B, 0x3949AB);
        } else {
            int er = Math.max(1, size / 12);
            Gfx.disc(g, cx - size / 5 + ex, y - size / 8, er);
            Gfx.disc(g, cx + size / 5 + ex, y - size / 8, er);
        }
        boolean happy = fun > 40 && hunger > 30 && !sick;
        int mw = size / 4;
        if (happy) g.drawArc(cx - mw / 2 + ex, y, mw, mw / 2, 180, 180);
        else g.drawArc(cx - mw / 2 + ex, y + mw / 4, mw, mw / 2, 0, 180);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int fh = Gfx.SMALL.getHeight();
        boolean night = asleep;
        g.setColor(night ? 0x1A237E : 0xFCE4EC);
        g.fillRect(0, 0, W, H);
        g.setColor(night ? 0x283593 : 0xF8BBD0);
        g.fillRect(0, H * 3 / 5, W, H * 2 / 5);
        g.setFont(Gfx.SMALL_B);
        g.setColor(night ? 0xFFFFFF : 0x880E4F);
        g.drawString(STAGE[stage] + "  day " + (age + 1), 2, 1, Gfx.TL);
        g.drawString("HP " + health, W - 2, 1, Gfx.TR);
        // bars
        int bw = (W - 10) / 4;
        String[] lab = { "Food", "Fun", "Clean", "Rest" };
        int[] val = { hunger, fun, clean, energy };
        int[] bc = { 0xFF8A65, 0xFFD54F, 0x4FC3F7, 0x9575CD };
        for (int k = 0; k < 4; k++) {
            int x = 2 + k * (bw + 2);
            Gfx.text(g, lab[k], x + bw / 2, hud, Gfx.TC, Gfx.SMALL, night ? 0xC5CAE9 : 0x6A1B4D);
            Gfx.bar(g, x, hud + fh, bw, 4, val[k], 100, val[k] < 20 ? 0xE53935 : bc[k], 0xD7CCC8);
        }
        int s = Math.min(W / 2, H / 3);
        int py = H * 3 / 5 - s / 3;
        int look = playing ? lookDir : 0;
        drawPet(g, W / 2, py, s, look);
        for (int k = 0; k < mess; k++) {
            g.setColor(0x6D4C41);
            Gfx.disc(g, W / 6 + k * W / 10, H * 3 / 5 + 8, 3);
        }
        if (sick) Gfx.text(g, "+", W / 2 + s / 3, py - s / 2, Gfx.TL, Gfx.MEDIUM, 0xD32F2F);
        if (anim > 0 && animKind == 0) {
            g.setColor(0xFF7043);
            Gfx.disc(g, W / 2 - s / 2, py + s / 4 - anim, 3);
        }
        // message and menu
        int my = H - fh * 2 - 6;
        Gfx.text(g, msg, W / 2, my - fh - 2, Gfx.TC, Gfx.SMALL, night ? 0xFFFFFF : 0x4A148C);
        if (playing) {
            Gfx.text(g, "Round " + (gameRound + 1) + "/5  won " + gameWins, W / 2, my + 2, Gfx.TC, Gfx.SMALL_B, 0x4A148C);
            Gfx.text(g, "4 = left    6 = right", W / 2, my + fh + 4, Gfx.TC, Gfx.SMALL, 0x4A148C);
        } else if (stage == 0) {
            Gfx.text(g, "Hatching in " + (DAY - dayT) / 20 + "s", W / 2, my + 2, Gfx.TC, Gfx.SMALL_B, 0x4A148C);
        } else {
            Gfx.panel(g, 4, my, W - 8, fh * 2 + 4, 0xFFFFFF, 0xEC407A);
            Gfx.text(g, "< " + ACT[sel] + " >", W / 2, my + 2, Gfx.TC, Gfx.SMALL_B, 0xAD1457);
            String tip = sel == 0 ? "+food" : sel == 1 ? "+fun, -rest" : sel == 2 ? "clean up" : sel == 3 ? (asleep ? "wake up" : "sleep") : "cures illness";
            Gfx.text(g, tip, W / 2, my + fh + 2, Gfx.TC, Gfx.SMALL, 0x880E4F);
        }
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.min(w / 3, h);
        int cx = x + w / 2, cy = y + h / 2;
        int bob = (clock / 6) % 2;
        g.setColor(0xF06292);
        g.fillArc(cx - s / 2, cy - s / 2 - bob, s, s, 0, 360);
        g.fillTriangle(cx - s / 2, cy - s / 4, cx - s / 3, cy - s * 2 / 3, cx - s / 6, cy - s / 3);
        g.fillTriangle(cx + s / 2, cy - s / 4, cx + s / 3, cy - s * 2 / 3, cx + s / 6, cy - s / 3);
        g.setColor(0x000000);
        Gfx.disc(g, cx - s / 5, cy - s / 8 - bob, Math.max(1, s / 12));
        Gfx.disc(g, cx + s / 5, cy - s / 8 - bob, Math.max(1, s / 12));
        g.drawArc(cx - s / 8, cy - bob, s / 4, s / 8, 180, 180);
    }
}
