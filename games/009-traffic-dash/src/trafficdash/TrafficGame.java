package trafficdash;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Traffic Dash: weave through highway traffic, grab fuel, chain near-misses. */
public class TrafficGame extends Game {
    private static final int MAXC = 10;
    private static final int[] CAR_COLORS = { 0x2E86DE, 0x10AC84, 0xF368E0, 0xFF9F43, 0x8395A7, 0xEE5253 };

    private int lanes, roadX, roadW, laneW, carW, carH, playerY;
    private int lane, px;              // player lane and x (<<8)
    private int speed, minSpeed, maxSpeed;
    private int fuel, dist, scroll, hearts, invuln;
    private final int[] cl = new int[MAXC], cy = new int[MAXC], cs = new int[MAXC], cc = new int[MAXC];
    private final int[] cxp = new int[MAXC], target = new int[MAXC], blink = new int[MAXC];
    private final boolean[] used = new boolean[MAXC], passed = new boolean[MAXC], isFuel = new boolean[MAXC];
    private int combo, popTimer;
    private String pop = "";
    private Image[] sprites;
    private Image playerImg;
    private int spriteW;

    protected String name() { return "Traffic Dash"; }

    protected String[] help() {
        return new String[] {
            "Race down a busy highway. Switch lanes to avoid the slower traffic; cars ahead sometimes signal and change lane.",
            "Your fuel drains constantly: drive through green fuel cans to refill. Passing close to a car is a near miss and builds a score combo.",
            "Three crashes and you're out.",
            "- Controls",
            "4/6: change lane",
            "2: accelerate  8: brake",
        };
    }

    protected String[] modes() { return new String[] { "Normal", "Rush hour" }; }

    protected int accent() { return 0xEE5253; }

    private void layout() {
        lanes = W >= 160 ? 4 : 3;
        roadW = W * 82 / 100;
        laneW = roadW / lanes;
        roadW = laneW * lanes;
        roadX = (W - roadW) / 2;
        carW = laneW * 62 / 100;
        carH = carW * 7 / 4;
        playerY = H - carH - Math.max(6, H / 20);
        if (sprites == null || spriteW != carW) buildSprites();
    }

    private void buildSprites() {
        spriteW = carW;
        int sc = Math.max(1, carW / 8);
        String[] art = { "..1111..", ".122221.", ".133331.", "11222211", "12222221", "12222221",
                ".122221.", ".122221.", ".144441.", "11222211", "12222221", ".155551." };
        sprites = new Image[CAR_COLORS.length];
        for (int i = 0; i < CAR_COLORS.length; i++) {
            sprites[i] = Gfx.sprite(art, new int[] { 0, 0x1B1B1B, CAR_COLORS[i], 0x9AD0F5, 0x5A6B7C, 0xFF4040 }, sc);
        }
        playerImg = Gfx.sprite(art, new int[] { 0, 0x1B1B1B, 0xFECA57, 0x9AD0F5, 0x5A6B7C, 0xFF4040 }, sc);
    }

    private int laneX(int l) {
        return roadX + l * laneW + laneW / 2;
    }

    protected void newGame() {
        layout();
        lane = lanes / 2;
        px = laneX(lane) << 8;
        minSpeed = (H << 8) / 120;
        maxSpeed = (H << 8) / 22;
        speed = (H << 8) / 50;
        fuel = 1000;
        dist = 0;
        hearts = 3;
        invuln = 0;
        combo = 0;
        for (int i = 0; i < MAXC; i++) used[i] = false;
    }

    protected void update() {
        layout();
        if ((tapped & K_LEFT) != 0 && lane > 0) { lane--; Sfx.click(); }
        if ((tapped & K_RIGHT) != 0 && lane < lanes - 1) { lane++; Sfx.click(); }
        int tx = laneX(lane) << 8;
        px += (tx - px) / 3;
        if ((held & K_UP) != 0) speed += maxSpeed / 60;
        else if ((held & K_DOWN) != 0) speed -= maxSpeed / 25;
        else speed += maxSpeed / 400;
        speed = Math.max(minSpeed, Math.min(maxSpeed, speed));
        dist += speed >> 8;
        scroll = (scroll + (speed >> 8)) % 32;
        score = dist / 8 + combo;
        fuel -= 1 + speed * 2 / maxSpeed;
        if (fuel <= 0) {
            headline = "OUT OF FUEL";
            endGame(false);
            return;
        }
        if (invuln > 0) invuln--;
        if (popTimer > 0) popTimer--;
        spawn();
        int pxl = px >> 8;
        for (int i = 0; i < MAXC; i++) {
            if (!used[i]) continue;
            cy[i] += speed - cs[i];
            if (blink[i] > 0 && --blink[i] == 0) cl[i] = target[i];
            int goal = laneX(cl[i]) << 8;
            cxp[i] += (goal - cxp[i]) / 6;
            int y = cy[i] >> 8;
            if (y > H + carH || y < -carH * 6) { used[i] = false; continue; }
            int x = cxp[i] >> 8;
            boolean hitX = Math.abs(x - pxl) < carW - 2, hitY = Math.abs(y - playerY) < carH - 2;
            if (hitX && hitY) {
                if (isFuel[i]) {
                    used[i] = false;
                    fuel = Math.min(1000, fuel + 350);
                    score += 20;
                    Sfx.good();
                    show("FUEL +");
                } else if (invuln == 0) {
                    crash(i);
                    if (state != PLAY) return;
                }
            } else if (!passed[i] && !isFuel[i] && y > playerY + carH) {
                passed[i] = true;
                if (Math.abs(x - pxl) < laneW + carW / 2) {
                    combo += 25 + combo / 10;
                    Sfx.tone(86, 25);
                    show("NEAR MISS!");
                }
            }
        }
    }

    private void show(String s) {
        pop = s;
        popTimer = 15;
    }

    private void crash(int i) {
        hearts--;
        used[i] = false;
        invuln = 40;
        speed = minSpeed;
        combo = combo / 2;
        Sfx.bad();
        if (hearts <= 0) {
            headline = "WRECKED!";
            endGame(false);
        }
    }

    private void spawn() {
        int gap = mode == 1 ? 18 : 28;
        if (frame % Math.max(6, gap - dist / 2000) != 0) return;
        int l = Rnd.nextInt(lanes);
        // keep at least one lane open near the spawn line
        int blocked = 0;
        for (int i = 0; i < MAXC; i++) if (used[i] && (cy[i] >> 8) < carH * 2) blocked |= 1 << cl[i];
        blocked |= 1 << l;
        if (blocked == (1 << lanes) - 1) return;
        for (int i = 0; i < MAXC; i++) {
            if (used[i]) continue;
            used[i] = true;
            passed[i] = false;
            isFuel[i] = Rnd.chance(fuel < 400 ? 22 : 9);
            cl[i] = target[i] = l;
            cxp[i] = laneX(l) << 8;
            cy[i] = -carH << 8;
            cs[i] = isFuel[i] ? 0 : minSpeed + Rnd.nextInt(Math.max(1, speed - minSpeed));
            cc[i] = Rnd.nextInt(CAR_COLORS.length);
            blink[i] = 0;
            if (!isFuel[i] && dist > 1500 && Rnd.chance(mode == 1 ? 35 : 20)) {
                int nl = l + (Rnd.chance(50) ? 1 : -1);
                if (nl >= 0 && nl < lanes) {
                    target[i] = nl;
                    blink[i] = 25;
                }
            }
            return;
        }
    }

    protected void draw(Graphics g) {
        layout();
        g.setColor(0x2D5A27);
        g.fillRect(0, 0, W, H);
        g.setColor(0x3C7A33);
        for (int y = -32 + scroll; y < H; y += 32) {
            g.fillRect(2, y, roadX - 6, 8);
            g.fillRect(roadX + roadW + 4, y + 16, W - roadX - roadW - 6, 8);
        }
        g.setColor(0x3B3F46);
        g.fillRect(roadX, 0, roadW, H);
        g.setColor(0xF5F5F5);
        g.fillRect(roadX - 2, 0, 2, H);
        g.fillRect(roadX + roadW, 0, 2, H);
        g.setColor(0xD8D8D8);
        for (int l = 1; l < lanes; l++) {
            int x = roadX + l * laneW;
            for (int y = -32 + scroll; y < H; y += 32) g.fillRect(x - 1, y, 2, 14);
        }
        for (int i = 0; i < MAXC; i++) {
            if (!used[i]) continue;
            int x = cxp[i] >> 8, y = cy[i] >> 8;
            if (isFuel[i]) {
                int s = carW * 2 / 3;
                Gfx.bevel(g, x - s / 2, y + carH / 2 - s / 2, s, s, 0x2ECC71);
                g.setFont(Gfx.SMALL_B);
                g.setColor(0xFFFFFF);
                g.drawString("F", x + 1, y + carH / 2 - 4, Gfx.TC);
            } else {
                g.drawImage(sprites[cc[i]], x, y, Gfx.TC);
                if (blink[i] > 0 && (blink[i] & 4) != 0) {
                    g.setColor(0xFFB300);
                    int bx = target[i] > cl[i] ? x + carW / 2 - 2 : x - carW / 2;
                    g.fillRect(bx, y + carH - 4, 3, 3);
                    g.fillRect(bx, y + 1, 3, 3);
                }
            }
        }
        if (invuln == 0 || (clock & 2) == 0) g.drawImage(playerImg, px >> 8, playerY, Gfx.TC);
        // HUD
        int hud = Gfx.SMALL.getHeight() + 3;
        g.setColor(0x111418);
        g.fillRect(0, 0, W, hud);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(String.valueOf(score), 2, 1, Gfx.TL);
        g.setColor(0xFF6B6B);
        for (int i = 0; i < hearts; i++) g.fillRect(W - 7 - i * 7, 3, 5, 5);
        int fw = W / 3;
        Gfx.bar(g, W / 2 - fw / 2, 2, fw, hud - 5, fuel, 1000, fuel < 250 ? 0xFF5252 : 0x2ECC71, 0x263238);
        g.setFont(Gfx.SMALL);
        g.setColor(0xB0BEC5);
        g.drawString((speed * 3 >> 8) + " km/h", W - 2, hud + 1, Gfx.TR);
        if (popTimer > 0) Gfx.shadowText(g, pop, W / 2, H / 3, Gfx.TC, Gfx.SMALL_B, 0xFFEB3B, 0x000000);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        carW = Math.max(10, Math.min(w / 5, h * 4 / 7));
        carH = carW * 7 / 4;
        if (sprites == null || spriteW != carW) buildSprites();
        g.setColor(0x3B3F46);
        g.fillRect(x, y, w, h);
        g.setColor(0xD8D8D8);
        int off = (clock * 4) % 24;
        for (int k = 1; k < 3; k++) for (int yy = y - 24 + off; yy < y + h; yy += 24) g.fillRect(x + k * w / 3, Math.max(y, yy), 2, 10);
        g.setClip(x, y, w, h);
        g.drawImage(playerImg, x + w / 6, y + h - carH, Gfx.TC);
        g.drawImage(sprites[0], x + w / 2, y + ((clock * 2) % (h + carH)) - carH, Gfx.TC);
        g.drawImage(sprites[3], x + w * 5 / 6, y + ((clock * 3 + 40) % (h + carH)) - carH, Gfx.TC);
    }
}
