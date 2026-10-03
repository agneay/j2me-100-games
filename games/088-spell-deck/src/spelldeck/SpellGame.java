package spelldeck;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Spell Deck: a deck-building duel. Each turn you draw four spell cards and
 * have three mana to cast them. Enemies show their next move. After each
 * victory, add a new card to your deck.
 */
public class SpellGame extends Game {
    private static final String[] CARD = { "Strike", "Fireball", "Ward", "Mend", "Drain", "Frost", "Spark", "Barrier" };
    private static final int[] COST = { 1, 2, 1, 1, 2, 2, 0, 2 };
    private static final String[] DESC = { "Deal 4", "Deal 9", "Block 5", "Heal 5", "Deal 4, heal 4", "Deal 3, enemy skips turn", "Deal 2", "Block 12" };
    private static final int[] CCOL = { 0xE57373, 0xFF7043, 0x64B5F6, 0x81C784, 0xBA68C8, 0x4DD0E1, 0xFFF176, 0x90A4AE };
    private static final String[] FOE = { "Imp", "Goblin Mage", "Stone Golem", "Wraith", "Lich King" };
    private static final int[] FOE_HP = { 22, 30, 45, 40, 70 };
    private static final int[] FOE_ATK = { 5, 7, 6, 9, 11 };

    private final int[] deck = new int[30];
    private int deckN;
    private final int[] pile = new int[30];
    private int pileN;
    private final int[] hand = new int[4];
    private int handN, sel, mana, hp, maxHp, block, fight, foeHp, foeMax, foeBlock, intent, intentVal, frozen;
    private int phase, reward0, reward1, reward2, rsel, wait;
    private String log = "";
    private static final int PLAYER = 0, ENEMY = 1, REWARD = 2;

    protected String name() { return "Spell Deck"; }

    protected String[] help() {
        return new String[] {
            "Battle five foes with a deck of spell cards. Each turn you draw four cards and have 3 mana. Cast cards by paying their cost, then end your turn.",
            "Watch the foe's intent: a sword means it will attack for that much, a shield means it will block. Ward and Barrier block damage until your next turn.",
            "Win a fight and choose a new card to add to your deck.",
            "- Controls",
            "4/6: choose card  5: cast",
            "#/0: end turn",
        };
    }

    protected int accent() { return 0xBA68C8; }

    protected void newGame() {
        deckN = 0;
        int[] starter = { 0, 0, 0, 0, 2, 2, 3, 1, 6, 6 };
        for (int i = 0; i < starter.length; i++) deck[deckN++] = starter[i];
        maxHp = hp = 40;
        fight = 0;
        startFight();
    }

    private void startFight() {
        foeMax = foeHp = FOE_HP[fight];
        foeBlock = 0;
        frozen = 0;
        block = 0;
        pileN = 0;
        for (int i = 0; i < deckN; i++) pile[pileN++] = deck[i];
        shufflePile();
        pickIntent();
        startTurn();
        log = FOE[fight] + " appears!";
    }

    private void shufflePile() {
        for (int i = pileN - 1; i > 0; i--) {
            int j = Rnd.nextInt(i + 1);
            int t = pile[i];
            pile[i] = pile[j];
            pile[j] = t;
        }
    }

    private void startTurn() {
        phase = PLAYER;
        mana = 3;
        block = 0;
        handN = 0;
        for (int k = 0; k < 4; k++) {
            if (pileN == 0) {
                for (int i = 0; i < deckN; i++) pile[pileN++] = deck[i];
                shufflePile();
            }
            hand[handN++] = pile[--pileN];
        }
        sel = 0;
    }

    private void pickIntent() {
        if (fight >= 2 && Rnd.chance(30)) {
            intent = 1;
            intentVal = 6 + fight * 2;
        } else {
            intent = 0;
            intentVal = FOE_ATK[fight] + Rnd.range(-1, 2);
        }
    }

    private void hitFoe(int d) {
        int absorbed = Math.min(foeBlock, d);
        foeBlock -= absorbed;
        foeHp -= d - absorbed;
    }

    private void cast(int idx) {
        int c = hand[idx];
        if (COST[c] > mana) {
            Sfx.bad();
            log = "Not enough mana";
            return;
        }
        mana -= COST[c];
        hand[idx] = hand[--handN];
        if (sel >= handN) sel = Math.max(0, handN - 1);
        switch (c) {
            case 0: hitFoe(4); break;
            case 1: hitFoe(9); break;
            case 2: block += 5; break;
            case 3: hp = Math.min(maxHp, hp + 5); break;
            case 4: hitFoe(4); hp = Math.min(maxHp, hp + 4); break;
            case 5: hitFoe(3); frozen = 1; break;
            case 6: hitFoe(2); break;
            default: block += 12; break;
        }
        log = "You cast " + CARD[c];
        Sfx.tone(70 + c * 2, 30);
        if (foeHp <= 0) win();
    }

    private void win() {
        score += 100 + hp;
        fight++;
        Sfx.win();
        if (fight >= FOE.length) {
            headline = "ARCHMAGE!";
            endGame(true);
            return;
        }
        phase = REWARD;
        reward0 = 1 + Rnd.nextInt(7);
        do reward1 = 1 + Rnd.nextInt(7); while (reward1 == reward0);
        do reward2 = 1 + Rnd.nextInt(7); while (reward2 == reward0 || reward2 == reward1);
        rsel = 0;
        hp = Math.min(maxHp, hp + 10);
    }

    private void enemyTurn() {
        if (frozen > 0) {
            frozen = 0;
            log = FOE[fight] + " is frozen!";
        } else if (intent == 0) {
            int dmg = Math.max(0, intentVal - block);
            hp -= dmg;
            log = FOE[fight] + " hits for " + dmg;
            Sfx.bad();
        } else {
            foeBlock += intentVal;
            log = FOE[fight] + " shields " + intentVal;
        }
        if (hp <= 0) {
            hp = 0;
            headline = "DEFEATED BY " + FOE[fight].toUpperCase();
            endGame(false);
            return;
        }
        pickIntent();
        startTurn();
    }

    protected void update() {
        if (phase == REWARD) {
            if ((pressed & K_LEFT) != 0) rsel = (rsel + 2) % 3;
            if ((pressed & K_RIGHT) != 0) rsel = (rsel + 1) % 3;
            if ((pressed & K_FIRE) != 0) {
                if (deckN < deck.length) deck[deckN++] = rsel == 0 ? reward0 : (rsel == 1 ? reward1 : reward2);
                startFight();
            }
            return;
        }
        if (phase == ENEMY) {
            if (++wait > 15) {
                wait = 0;
                enemyTurn();
            }
            return;
        }
        int d = digitPressed();
        if (handN > 0) {
            if ((pressed & K_LEFT) != 0) sel = (sel + handN - 1) % handN;
            if ((pressed & K_RIGHT) != 0) sel = (sel + 1) % handN;
            if ((pressed & K_FIRE) != 0) cast(sel);
            if (phase != PLAYER || state != PLAY) return;
        }
        if ((pressed & K_POUND) != 0 || d == 0) {
            phase = ENEMY;
            wait = 0;
        }
    }

    private void card(Graphics g, int c, int x, int y, int w, int h, boolean on) {
        Gfx.panel(g, x, y, w, h, on ? Gfx.shade(CCOL[c], -20) : 0x2A1F3D, CCOL[c]);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        String nm = CARD[c];
        if (Gfx.SMALL_B.stringWidth(nm) > w - 4) nm = nm.substring(0, Math.max(1, (w - 4) / Gfx.SMALL_B.charWidth('W')));
        g.drawString(nm, x + w / 2, y + 2, Gfx.TC);
        g.setColor(0xFFD54F);
        g.drawString(String.valueOf(COST[c]), x + 3, y + h - Gfx.SMALL.getHeight() - 1, Gfx.TL);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setColor(0x150F22);
        g.fillRect(0, 0, W, H);
        if (phase == REWARD) {
            Gfx.text(g, "Choose a new card", W / 2, hud, Gfx.TC, Gfx.MEDIUM, 0xFFD54F);
            int cw = (W - 16) / 3, ch = cw * 4 / 3;
            int[] r = { reward0, reward1, reward2 };
            for (int k = 0; k < 3; k++) card(g, r[k], 4 + k * (cw + 4), H / 3, cw, ch, k == rsel);
            Gfx.text(g, DESC[r[rsel]], W / 2, H / 3 + ch + 6, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
            return;
        }
        // foe
        int fy = hud + 4;
        int fs = Math.max(16, Math.min(W / 3, H / 5));
        g.setColor(fight == 4 ? 0x7E57C2 : 0x8D6E63);
        g.fillRoundRect(W / 2 - fs / 2, fy, fs, fs, fs / 3, fs / 3);
        g.setColor(frozen > 0 ? 0x80DEEA : 0xFF1744);
        g.fillRect(W / 2 - fs / 4, fy + fs / 3, fs / 6, fs / 6);
        g.fillRect(W / 2 + fs / 8, fy + fs / 3, fs / 6, fs / 6);
        Gfx.bar(g, W / 4, fy + fs + 3, W / 2, 5, foeHp, foeMax, 0xEF5350, 0x37474F);
        g.setFont(Gfx.SMALL);
        g.setColor(0xFFFFFF);
        g.drawString(FOE[fight] + " " + foeHp + (foeBlock > 0 ? " [" + foeBlock + "]" : ""), W / 2, fy + fs + 9, Gfx.TC);
        g.setColor(intent == 0 ? 0xFF8A80 : 0x90CAF9);
        g.drawString(intent == 0 ? "intends: attack " + intentVal : "intends: block " + intentVal, W / 2, fy + fs + 9 + Gfx.SMALL.getHeight(), Gfx.TC);
        // player
        int py = H / 2 + Gfx.SMALL.getHeight();
        Gfx.bar(g, 4, py, W / 2, 5, hp, maxHp, 0x66BB6A, 0x37474F);
        g.setColor(0xFFFFFF);
        g.drawString("HP " + hp + (block > 0 ? "  block " + block : ""), 4, py + 6, Gfx.TL);
        g.setColor(0xFFD54F);
        for (int k = 0; k < 3; k++) {
            if (k < mana) g.fillArc(W - 12 - k * 10, py, 8, 8, 0, 360);
            else g.drawArc(W - 12 - k * 10, py, 8, 8, 0, 360);
        }
        // hand
        int cw = Math.max(20, (W - 10) / 4 - 2), ch = Math.min(cw * 4 / 3, H - py - hud * 2 - 10);
        int hy = H - ch - hud;
        for (int k = 0; k < handN; k++) card(g, hand[k], 4 + k * (cw + 2), hy - (k == sel ? 3 : 0), cw, ch, k == sel);
        if (handN > 0) Gfx.text(g, DESC[hand[sel]], W / 2, hy - Gfx.SMALL.getHeight() - 4, Gfx.TC, Gfx.SMALL, 0xE1BEE7);
        Gfx.text(g, phase == ENEMY ? log : "# end turn   " + log, W / 2, H - hud + 1, Gfx.TC, Gfx.SMALL, 0xB39DDB);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString("Fight " + (fight + 1) + "/5", 2, 1, Gfx.TL);
        g.drawString("Deck " + deckN, W - 2, 1, Gfx.TR);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int cw = Math.max(14, Math.min(w / 5, h * 3 / 4));
        for (int k = 0; k < 3; k++) {
            int c = (clock / 20 + k) % CARD.length;
            int lift = (clock / 6) % 3 == k ? 3 : 0;
            card(g, c, x + w / 2 - cw * 3 / 2 + k * cw, y + (h - cw * 4 / 3) / 2 - lift, cw - 2, cw * 4 / 3, false);
        }
    }
}
