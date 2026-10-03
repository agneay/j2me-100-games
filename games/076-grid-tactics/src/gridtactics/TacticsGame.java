package gridtactics;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Grid Tactics: turn-based squad battles on an 8x8 field. Each of your units
 * may move and then attack once per turn. Trees give cover, rocks block.
 */
public class TacticsGame extends Game {
    private static final int N = 8, MAXU = 12;
    private static final String[] KIND = { "Soldier", "Archer", "Knight" };
    private static final int[] MOVE = { 3, 2, 4 }, ATK = { 5, 4, 6 }, RANGE = { 1, 3, 1 }, HP = { 14, 9, 12 };

    private final byte[] terrain = new byte[N * N]; // 0 grass, 1 tree, 2 rock
    private final int[] ux = new int[MAXU], uy = new int[MAXU], uk = new int[MAXU], uhp = new int[MAXU], side = new int[MAXU];
    private final boolean[] moved = new boolean[MAXU], acted = new boolean[MAXU], alive = new boolean[MAXU];
    private final int[] dist = new int[N * N], queue = new int[N * N];
    private int nu, cur, sel = -1, phase, battle, aiIdx, aiWait, flashU = -1, flashT;
    private String msg = "";
    private static final int PICK = 0, MOVE_TO = 1, TARGET = 2, ENEMY = 3;

    protected String name() { return "Grid Tactics"; }

    protected String[] help() {
        return new String[] {
            "Command a squad of blue units against the red army. On your turn each unit may move, then attack an enemy in range.",
            "Soldiers are sturdy, Archers shoot from 3 squares away, Knights move far and hit hard. Units standing in trees take less damage; rocks block movement.",
            "Select a unit (5), pick a highlighted square to move to (5), then pick a target (5) or press 0 to skip the attack. Press # to end your turn. Win three battles.",
            "- Controls",
            "2/4/6/8: move cursor  5: select",
            "0: cancel / skip  #: end turn",
        };
    }

    protected int accent() { return 0x2A9D8F; }

    protected void newGame() {
        battle = 0;
        startBattle();
    }

    private void startBattle() {
        for (int i = 0; i < N * N; i++) {
            int r = Rnd.nextInt(100);
            terrain[i] = (byte) (r < 12 ? 1 : (r < 18 ? 2 : 0));
        }
        nu = 0;
        int[] mine = { 0, 1, 2, 0 };
        for (int k = 0; k < 4; k++) addUnit(k * 2 + 1 - (k > 1 ? 1 : 0), 7, mine[k], 0);
        int enemies = 3 + battle;
        for (int k = 0; k < enemies; k++) addUnit((k * 3 + 1) % N, k < 4 ? 0 : 1, k % 3, 1);
        for (int i = 0; i < nu; i++) terrain[uy[i] * N + ux[i]] = 0;
        newTurn();
        cur = 7 * N + 3;
        msg = "Battle " + (battle + 1) + " - your turn";
    }

    private void addUnit(int x, int y, int kind, int s) {
        ux[nu] = x;
        uy[nu] = y;
        uk[nu] = kind;
        uhp[nu] = HP[kind] + (s == 1 ? battle * 2 : 0);
        side[nu] = s;
        alive[nu] = true;
        nu++;
    }

    private void newTurn() {
        for (int i = 0; i < nu; i++) {
            moved[i] = false;
            acted[i] = false;
        }
        phase = PICK;
        sel = -1;
    }

    private int unitAt(int x, int y) {
        for (int i = 0; i < nu; i++) if (alive[i] && ux[i] == x && uy[i] == y) return i;
        return -1;
    }

    /** BFS movement range for unit u into dist[] (-1 = unreachable). */
    private void reach(int u) {
        for (int i = 0; i < N * N; i++) dist[i] = -1;
        int head = 0, tail = 0, s = uy[u] * N + ux[u];
        dist[s] = 0;
        queue[tail++] = s;
        while (head < tail) {
            int c = queue[head++];
            if (dist[c] >= MOVE[uk[u]]) continue;
            int x = c % N, y = c / N;
            for (int d = 0; d < 4; d++) {
                int nx = x + (d == 0 ? 1 : (d == 1 ? -1 : 0)), ny = y + (d == 2 ? 1 : (d == 3 ? -1 : 0));
                if (nx < 0 || ny < 0 || nx >= N || ny >= N) continue;
                int j = ny * N + nx;
                if (dist[j] >= 0 || terrain[j] == 2) continue;
                int o = unitAt(nx, ny);
                if (o >= 0 && side[o] != side[u]) continue;
                dist[j] = dist[c] + 1;
                queue[tail++] = j;
            }
        }
    }

    private boolean inRange(int a, int b) {
        int d = Math.abs(ux[a] - ux[b]) + Math.abs(uy[a] - uy[b]);
        return d >= 1 && d <= RANGE[uk[a]];
    }

    private void attack(int a, int b) {
        int dmg = ATK[uk[a]] + Rnd.range(0, 2);
        if (terrain[uy[b] * N + ux[b]] == 1) dmg = dmg * 2 / 3;
        uhp[b] -= dmg;
        flashU = b;
        flashT = 10;
        msg = KIND[uk[a]] + " hits " + KIND[uk[b]] + " for " + dmg;
        Sfx.hit();
        if (uhp[b] <= 0) {
            alive[b] = false;
            msg = KIND[uk[b]] + " defeated!";
            if (side[b] == 1) score += 50;
        }
        acted[a] = true;
        checkEnd();
    }

    private boolean anyAlive(int s) {
        for (int i = 0; i < nu; i++) if (alive[i] && side[i] == s) return true;
        return false;
    }

    private void checkEnd() {
        if (!anyAlive(1)) {
            battle++;
            score += 300;
            Sfx.win();
            if (battle >= 3) {
                headline = "CAMPAIGN WON!";
                endGame(true);
            } else startBattle();
        } else if (!anyAlive(0)) {
            headline = "SQUAD LOST";
            endGame(false);
        }
    }

    protected void update() {
        if (flashT > 0) flashT--;
        if (phase == ENEMY) {
            enemyStep();
            return;
        }
        int x = cur % N, y = cur / N;
        if ((pressed & K_LEFT) != 0) x = (x + N - 1) % N;
        if ((pressed & K_RIGHT) != 0) x = (x + 1) % N;
        if ((pressed & K_UP) != 0) y = (y + N - 1) % N;
        if ((pressed & K_DOWN) != 0) y = (y + 1) % N;
        cur = y * N + x;
        if ((pressed & K_POUND) != 0) {
            phase = ENEMY;
            aiIdx = 0;
            aiWait = 10;
            msg = "Enemy turn";
            return;
        }
        if (digit(0)) {
            if (phase == TARGET && sel >= 0) { acted[sel] = true; phase = PICK; sel = -1; }
            else { phase = PICK; sel = -1; }
            return;
        }
        if ((pressed & K_FIRE) == 0) return;
        int u = unitAt(x, y);
        if (phase == PICK) {
            if (u >= 0 && side[u] == 0 && !(moved[u] && acted[u])) {
                sel = u;
                if (!moved[u]) { reach(u); phase = MOVE_TO; }
                else phase = TARGET;
                Sfx.click();
            } else Sfx.bad();
        } else if (phase == MOVE_TO) {
            if (dist[cur] >= 0 && (u < 0 || u == sel)) {
                ux[sel] = x;
                uy[sel] = y;
                moved[sel] = true;
                phase = TARGET;
                Sfx.click();
                boolean any = false;
                for (int i = 0; i < nu; i++) if (alive[i] && side[i] == 1 && inRange(sel, i)) any = true;
                if (!any) { acted[sel] = true; phase = PICK; sel = -1; }
            } else Sfx.bad();
        } else if (phase == TARGET) {
            if (u >= 0 && side[u] == 1 && inRange(sel, u)) {
                attack(sel, u);
                phase = PICK;
                sel = -1;
            } else Sfx.bad();
        }
        boolean allDone = true;
        for (int i = 0; i < nu; i++) if (alive[i] && side[i] == 0 && !(moved[i] && acted[i])) allDone = false;
        if (allDone && state == PLAY && phase == PICK) {
            phase = ENEMY;
            aiIdx = 0;
            aiWait = 15;
            msg = "Enemy turn";
        }
    }

    private void enemyStep() {
        if (--aiWait > 0) return;
        aiWait = 12;
        while (aiIdx < nu && (!alive[aiIdx] || side[aiIdx] != 1)) aiIdx++;
        if (aiIdx >= nu) {
            newTurn();
            msg = "Your turn";
            return;
        }
        int u = aiIdx++;
        // target the weakest reachable player unit, else move toward the nearest
        reach(u);
        int bestCell = uy[u] * N + ux[u], bestScore = -100000, bestTarget = -1;
        for (int c = 0; c < N * N; c++) {
            if (dist[c] < 0) continue;
            int o = unitAt(c % N, c / N);
            if (o >= 0 && o != u) continue;
            int ox = ux[u], oy = uy[u];
            ux[u] = c % N;
            uy[u] = c / N;
            for (int t = 0; t < nu; t++) {
                if (!alive[t] || side[t] != 0) continue;
                int sc;
                if (inRange(u, t)) sc = 1000 - uhp[t] * 10 + (terrain[c] == 1 ? 5 : 0);
                else sc = -(Math.abs(ux[u] - ux[t]) + Math.abs(uy[u] - uy[t])) * 10;
                if (sc > bestScore) {
                    bestScore = sc;
                    bestCell = c;
                    bestTarget = inRange(u, t) ? t : -1;
                }
            }
            ux[u] = ox;
            uy[u] = oy;
        }
        ux[u] = bestCell % N;
        uy[u] = bestCell / N;
        if (bestTarget >= 0) attack(u, bestTarget);
    }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int s = Math.max(8, Math.min(W - 4, H - hud * 3) / N);
        int ox = (W - s * N) / 2, oy = hud + (H - hud * 3 - s * N) / 2;
        g.setColor(0x1B2A1B);
        g.fillRect(0, 0, W, H);
        for (int i = 0; i < N * N; i++) {
            int x = ox + (i % N) * s, y = oy + (i / N) * s;
            g.setColor(((i % N) + (i / N)) % 2 == 0 ? 0x7CB342 : 0x73A83E);
            g.fillRect(x, y, s, s);
            if (terrain[i] == 1) {
                g.setColor(0x2E7D32);
                g.fillTriangle(x + s / 2, y + 1, x + 2, y + s - 2, x + s - 2, y + s - 2);
            } else if (terrain[i] == 2) {
                Gfx.bevel(g, x + 1, y + 2, s - 2, s - 3, 0x8D8D8D);
            }
            if (phase == MOVE_TO && dist[i] >= 0) {
                g.setColor(0x64B5F6);
                g.drawRect(x + 1, y + 1, s - 3, s - 3);
            }
        }
        for (int i = 0; i < nu; i++) {
            if (!alive[i]) continue;
            int x = ox + ux[i] * s, y = oy + uy[i] * s;
            boolean fl = i == flashU && flashT > 0 && (flashT & 2) != 0;
            int col = fl ? 0xFFFFFF : (side[i] == 0 ? 0x1E88E5 : 0xE53935);
            if (side[i] == 0 && moved[i] && acted[i]) col = 0x5C7FA0;
            g.setColor(col);
            g.fillRoundRect(x + 2, y + 2, s - 4, s - 4, 4, 4);
            g.setFont(Gfx.SMALL_B);
            g.setColor(0xFFFFFF);
            g.drawChar(KIND[uk[i]].charAt(0), x + s / 2 + 1, y + (s - 7) / 2 - 1, Gfx.TC);
            g.setColor(0x000000);
            g.fillRect(x + 2, y + s - 3, s - 4, 2);
            g.setColor(0x76FF03);
            g.fillRect(x + 2, y + s - 3, Math.max(1, (s - 4) * uhp[i] / (HP[uk[i]] + (side[i] == 1 ? battle * 2 : 0))), 2);
            if (phase == TARGET && sel >= 0 && side[i] == 1 && inRange(sel, i)) {
                g.setColor(0xFFEB3B);
                g.drawRect(x, y, s - 1, s - 1);
            }
        }
        if (phase != ENEMY) {
            g.setColor((clock & 4) == 0 ? 0xFFFFFF : 0x000000);
            g.drawRect(ox + (cur % N) * s, oy + (cur / N) * s, s - 1, s - 1);
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString("Battle " + (battle + 1) + "/3", 2, 1, Gfx.TL);
        g.setColor(0xFFD54F);
        g.drawString(String.valueOf(score), W - 2, 1, Gfx.TR);
        int u = unitAt(cur % N, cur / N);
        String info = u >= 0 ? (side[u] == 0 ? "Your " : "Enemy ") + KIND[uk[u]] + " HP " + uhp[u] : msg;
        Gfx.text(g, info, W / 2, H - hud * 2, Gfx.TC, Gfx.SMALL, 0xFFFFFF);
        Gfx.text(g, phase == MOVE_TO ? "5: move here" : (phase == TARGET ? "5: attack  0: skip" : "5 select  # end turn"), W / 2, H - hud, Gfx.TC, Gfx.SMALL, 0xA5D6A7);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int s = Math.max(8, Math.min(h / 2, w / 8));
        for (int k = 0; k < 6; k++) {
            g.setColor(k < 3 ? 0x1E88E5 : 0xE53935);
            int off = k < 3 ? (clock / 10) % 3 : -(clock / 10) % 3;
            g.fillRoundRect(x + w / 2 + (k - 3) * s + off * 2 + 1, y + h / 2 - s / 2, s - 2, s - 2, 4, 4);
        }
    }
}
