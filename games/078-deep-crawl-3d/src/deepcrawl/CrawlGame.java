package deepcrawl;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Deep Crawl 3D: a first-person grid dungeon in the old step-and-turn
 * style. Find the three rune stones and the stairs out of each maze,
 * fighting the slimes that block the corridors.
 */
public class CrawlGame extends Game {
    private static final int[] DX = { 0, 1, 0, -1 }, DY = { -1, 0, 1, 0 };
    private static final int[] SCALE = { 100, 62, 38, 24, 15, 9 }; // percent of half-width per depth
    private int n;
    private boolean[] wall;
    private byte[] thing; // 0 none, 1 rune, 2 slime, 3 stairs
    private boolean[] seen;
    private int px, py, dir, floor, runes, hp, steps, bump, mapView, fightT, slimeHp;

    protected String name() { return "Deep Crawl 3D"; }

    protected String[] help() {
        return new String[] {
            "You explore a maze in first person, one step at a time. Collect the three glowing rune stones on each floor, then find the stairs to go deeper.",
            "Green slimes block corridors: walk into one to fight it (each step into it is an attack). Fights cost health; standing still slowly heals you.",
            "Press # to look at your map of everywhere you've been. Clear three floors to escape.",
            "- Controls",
            "2: step forward  8: step back",
            "4/6: turn  1/3: sidestep",
            "#: map",
        };
    }

    protected int accent() { return 0x9B5DE5; }

    protected void newGame() {
        floor = 0;
        hp = 30;
        steps = 0;
        buildFloor();
    }

    private void buildFloor() {
        n = 13 + floor * 2;
        wall = new boolean[n * n];
        thing = new byte[n * n];
        seen = new boolean[n * n];
        for (int i = 0; i < n * n; i++) wall[i] = true;
        // recursive-backtracker maze on odd cells (iterative)
        int[] stack = new int[n * n];
        int sp = 0;
        stack[sp++] = n + 1;
        wall[n + 1] = false;
        while (sp > 0) {
            int c = stack[sp - 1];
            int cx = c % n, cy = c / n;
            int start = Rnd.nextInt(4), chosen = -1;
            for (int k = 0; k < 4; k++) {
                int d = (start + k) & 3;
                int nx = cx + DX[d] * 2, ny = cy + DY[d] * 2;
                if (nx > 0 && ny > 0 && nx < n - 1 && ny < n - 1 && wall[ny * n + nx]) { chosen = d; break; }
            }
            if (chosen < 0) { sp--; continue; }
            wall[(cy + DY[chosen]) * n + cx + DX[chosen]] = false;
            wall[(cy + DY[chosen] * 2) * n + cx + DX[chosen] * 2] = false;
            stack[sp++] = (cy + DY[chosen] * 2) * n + cx + DX[chosen] * 2;
        }
        // a few extra openings make loops
        for (int k = 0; k < n; k++) {
            int x = Rnd.range(1, n - 2), y = Rnd.range(1, n - 2);
            if ((x + y) % 2 == 1) wall[y * n + x] = false;
        }
        px = 1;
        py = 1;
        dir = 1;
        place((byte) 1, 3);
        place((byte) 2, 3 + floor * 2);
        place((byte) 3, 1);
        runes = 0;
        mapView = 0;
        fightT = 0;
        reveal();
    }

    private void place(byte what, int count) {
        for (int k = 0; k < count; k++) {
            int i;
            do {
                i = Rnd.nextInt(n * n);
            } while (wall[i] || thing[i] != 0 || (Math.abs(i % n - px) + Math.abs(i / n - py)) < n / 2);
            thing[i] = what;
        }
    }

    private boolean isWall(int x, int y) {
        return x < 0 || y < 0 || x >= n || y >= n || wall[y * n + x];
    }

    private void reveal() {
        for (int d = 0; d < 4; d++) {
            int x = px + DX[dir] * d, y = py + DY[dir] * d;
            if (isWall(x, y)) break;
            seen[y * n + x] = true;
        }
    }

    private void step(int d) {
        int nx = px + DX[d], ny = py + DY[d];
        if (isWall(nx, ny)) {
            bump = 6;
            Sfx.tone(40, 30);
            return;
        }
        int i = ny * n + nx;
        if (thing[i] == 2) {
            if (slimeHp <= 0) slimeHp = 6 + floor * 3;
            int dmg = Rnd.range(3, 6);
            slimeHp -= dmg;
            hp -= Rnd.range(1, 3 + floor);
            fightT = 8;
            Sfx.hit();
            if (slimeHp <= 0) {
                thing[i] = 0;
                score += 40;
                Sfx.good();
            }
            if (hp <= 0) {
                headline = "SLIMED ON FLOOR " + (floor + 1);
                endGame(false);
            }
            return;
        }
        px = nx;
        py = ny;
        steps++;
        Sfx.tone(50 + (steps & 3), 15);
        if (thing[i] == 1) {
            thing[i] = 0;
            runes++;
            score += 100;
            Sfx.good();
        } else if (thing[i] == 3) {
            if (runes >= 3) {
                floor++;
                score += 300;
                Sfx.win();
                if (floor >= 3) {
                    score += Math.max(0, 2000 - steps * 3);
                    headline = "ESCAPED THE DEPTHS!";
                    endGame(true);
                    return;
                }
                buildFloor();
                return;
            }
        }
        reveal();
    }

    protected void update() {
        if (bump > 0) bump--;
        if (fightT > 0) fightT--;
        if ((pressed & K_POUND) != 0) mapView ^= 1;
        if (mapView == 1) {
            if ((pressed & (K_FIRE | K_STAR)) != 0) mapView = 0;
            return;
        }
        if ((pressed & K_UP) != 0) step(dir);
        else if ((pressed & K_DOWN) != 0) step((dir + 2) & 3);
        else if ((pressed & K_LEFT) != 0) { dir = (dir + 3) & 3; reveal(); Sfx.click(); }
        else if ((pressed & K_RIGHT) != 0) { dir = (dir + 1) & 3; reveal(); Sfx.click(); }
        else if (digit(1)) step((dir + 3) & 3);
        else if (digit(3)) step((dir + 1) & 3);
        if (frame % 40 == 0 && hp < 30) hp++;
    }

    private int half(int d, int hw) { return hw * SCALE[Math.min(d, SCALE.length - 1)] / 100; }

    protected void draw(Graphics g) {
        int hud = Gfx.SMALL.getHeight() + 2;
        int vy0 = hud, vh = H - hud * 2;
        int cx = W / 2, cy = vy0 + vh / 2;
        int hw = W / 2, aspect = vh * 100 / W; // vertical half-size relative to horizontal
        if (mapView == 1) {
            drawMap(g, hud);
            return;
        }
        // ceiling and floor
        g.setColor(0x1A1226);
        g.fillRect(0, vy0, W, vh / 2);
        g.setColor(0x2B2233);
        g.fillRect(0, cy, W, vh / 2);
        // left of the facing direction: (DY, -DX) with screen-up = north
        int lx = DY[dir], ly = -DX[dir];
        for (int d = 4; d >= 0; d--) {
            int x = px + DX[dir] * d, y = py + DY[dir] * d;
            if (isWall(x, y)) continue;
            int near = half(d, hw), far = half(d + 1, hw);
            int nearV = near * aspect / 100, farV = far * aspect / 100;
            int shade = 0xA1887F;
            int c = Gfx.mix(shade, 0x120C18, d * 50);
            int cs = Gfx.mix(0x8D6E63, 0x120C18, d * 50);
            // left side
            if (isWall(x + lx, y + ly)) {
                g.setColor(cs);
                g.fillTriangle(cx - near, cy - nearV, cx - far, cy - farV, cx - far, cy + farV);
                g.fillTriangle(cx - near, cy - nearV, cx - far, cy + farV, cx - near, cy + nearV);
            } else {
                g.setColor(c);
                g.fillRect(cx - near, cy - farV, near - far, farV * 2);
            }
            // right side
            if (isWall(x - lx, y - ly)) {
                g.setColor(cs);
                g.fillTriangle(cx + near, cy - nearV, cx + far, cy - farV, cx + far, cy + farV);
                g.fillTriangle(cx + near, cy - nearV, cx + far, cy + farV, cx + near, cy + nearV);
            } else {
                g.setColor(c);
                g.fillRect(cx + far, cy - farV, near - far, farV * 2);
            }
            // front wall
            if (isWall(x + DX[dir], y + DY[dir])) {
                g.setColor(c);
                g.fillRect(cx - far, cy - farV, far * 2, farV * 2);
                g.setColor(Gfx.mix(c, 0x000000, 60));
                g.drawRect(cx - far, cy - farV, far * 2, farV * 2);
                g.drawLine(cx - far, cy, cx + far, cy);
            }
            // things in the cell ahead
            if (d >= 1) {
                int t = thing[y * n + x];
                int sz = near * 2 / 3;
                if (t == 1) {
                    g.setColor((clock & 4) == 0 ? 0x80DEEA : 0x26C6DA);
                    g.fillTriangle(cx, cy - sz / 2, cx - sz / 4, cy + nearV / 2, cx + sz / 4, cy + nearV / 2);
                } else if (t == 2) {
                    g.setColor(0x76FF03);
                    g.fillArc(cx - sz / 2, cy + nearV - sz * 2 / 3, sz, sz * 2 / 3, 0, 180);
                    g.fillRect(cx - sz / 2, cy + nearV - sz / 3, sz, sz / 3);
                    g.setColor(0x000000);
                    g.fillRect(cx - sz / 5, cy + nearV - sz / 2, Math.max(1, sz / 10), Math.max(1, sz / 10));
                    g.fillRect(cx + sz / 6, cy + nearV - sz / 2, Math.max(1, sz / 10), Math.max(1, sz / 10));
                } else if (t == 3) {
                    g.setColor(0xFFD54F);
                    for (int k = 0; k < 4; k++) g.fillRect(cx - sz / 2 + k * sz / 8, cy + nearV - (k + 1) * sz / 6, sz - k * sz / 4, sz / 8);
                }
            }
        }
        if (bump > 0) {
            g.setColor(0xFFFFFF);
            g.drawRect(2, vy0 + 2, W - 5, vh - 5);
        }
        if (fightT > 0 && (fightT & 2) != 0) {
            g.setColor(0xFF1744);
            g.drawRect(1, vy0 + 1, W - 3, vh - 3);
        }
        g.setColor(0x120C18);
        g.fillRect(0, 0, W, hud);
        g.fillRect(0, H - hud, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFF8A80);
        g.drawString("HP " + hp, 2, 1, Gfx.TL);
        g.setColor(0x80DEEA);
        g.drawString("Runes " + runes + "/3", W / 2, 1, Gfx.TC);
        g.setColor(0xFFD54F);
        g.drawString("F" + (floor + 1), W - 2, 1, Gfx.TR);
        g.setFont(Gfx.SMALL);
        g.setColor(0xB39DDB);
        g.drawString("Facing " + "NESW".charAt(dir) + "   # map", W / 2, H - hud + 1, Gfx.TC);
    }

    private void drawMap(Graphics g, int hud) {
        g.setColor(0x0B0614);
        g.fillRect(0, 0, W, H);
        int s = Math.max(2, Math.min(W / n, (H - hud * 2) / n));
        int ox = (W - s * n) / 2, oy = hud + (H - hud * 2 - s * n) / 2;
        for (int i = 0; i < n * n; i++) {
            if (!seen[i]) continue;
            int x = ox + (i % n) * s, y = oy + (i / n) * s;
            g.setColor(0x4A3B5C);
            g.fillRect(x, y, s, s);
            if (thing[i] == 1) { g.setColor(0x26C6DA); g.fillRect(x, y, s, s); }
            if (thing[i] == 3) { g.setColor(0xFFD54F); g.fillRect(x, y, s, s); }
            if (thing[i] == 2) { g.setColor(0x76FF03); g.fillRect(x, y, s, s); }
        }
        g.setColor(0xFFFFFF);
        g.fillRect(ox + px * s, oy + py * s, s, s);
        g.setColor(0xFF4081);
        g.fillRect(ox + px * s + s / 2 + DX[dir] * s / 2, oy + py * s + s / 2 + DY[dir] * s / 2, 1, 1);
        Gfx.text(g, "MAP - floor " + (floor + 1), W / 2, 1, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF);
        Gfx.text(g, "# back", W / 2, H - hud, Gfx.TC, Gfx.SMALL, 0xB39DDB);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        int cx = x + w / 2, cy = y + h / 2;
        for (int d = 3; d >= 0; d--) {
            int near = (w / 2) * SCALE[d] / 100, far = (w / 2) * SCALE[d + 1] / 100;
            int nv = near * h / w, fv = far * h / w;
            g.setColor(Gfx.mix(0x8D6E63, 0x120C18, d * 60));
            g.fillTriangle(cx - near, cy - nv, cx - far, cy - fv, cx - far, cy + fv);
            g.fillTriangle(cx - near, cy - nv, cx - far, cy + fv, cx - near, cy + nv);
            g.fillTriangle(cx + near, cy - nv, cx + far, cy - fv, cx + far, cy + fv);
            g.fillTriangle(cx + near, cy - nv, cx + far, cy + fv, cx + near, cy + nv);
        }
        g.setColor((clock & 8) == 0 ? 0x80DEEA : 0x26C6DA);
        Gfx.disc(g, cx, cy, 2);
    }
}
