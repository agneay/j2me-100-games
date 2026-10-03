package caveescape;

import gamekit.Body;
import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Sfx;
import gamekit.TileMap;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Cave Escape: a flip-screen exploration platformer. Six connected caverns,
 * five crystals, one key, one locked door and an exit.
 */
public class CaveGame extends Game {
    private static final int RC = 16, RR = 12, CRYSTALS = 5;
    private static final String[][] ROOMS = {
        { "################", "#              #", "#              #", "# C            #", "#===   ===     #", "#              #",
          "#              #", "#    ===    ===#", "#S         b    ", "#               ", "#####HH#########", "#####HH#########" },
        { "################", "#              #", "#   C     K    #", "#  ===   ===   #", "#              #", "#      ==      #",
          "#              #", "#  ===    ===  #", "         b      ", "                ", "####^^^##^^^####", "################" },
        { "################", "#              #", "#            C #", "#           ===#", "#              #", "#        ===   #",
          "#              #", "#     ===      #", "   b           #", "               #", "#######HH#######", "#######HH#######" },
        { "#####HH#########", "#    HH        #", "#    HH        #", "#    HH        #", "#    HH   ===  #", "#    HH        #",
          "#    HH  b     #", "#    HH     ===#", "#    HH         ", "# C  HH         ", "################", "################" },
        { "################", "#              #", "#     C        #", "#    ====      #", "#              #", "#  ===    ===  #",
          "#              #", "#      ==      #", "         b      ", "                ", "###^^######^^###", "################" },
        { "#######HH#######", "#      HH      #", "#      HH      #", "#      HH      #", "#      HH      #", "#      HH  b   #",
          "#      HH      #", "#      HH  #####", "       HH  D  X#", "       HH  D   #", "################", "################" },
    };

    private final TileMap[] maps = new TileMap[6];
    private final int[] batX = new int[6], batY = new int[6], batPhase = new int[6];
    private final boolean[] batAlive = new boolean[6];
    private int room, ts, ox, oy, lives, crystals, entryX, entryY, deadT, facing = 1, coyote, jumpBuf, msgT;
    private boolean key, climbing;
    private String msg = "";
    private final Body p = new Body();
    private Image hero, bat;
    private int spriteTs;

    protected String name() { return "Cave Escape"; }

    protected String[] help() {
        return new String[] {
            "You are trapped in a cave of six caverns. Collect all five crystals, find the key that opens the iron door, and reach the glowing exit.",
            "Walk off the edge of the screen to move to the next cavern. Climb ladders with 2 and 8. Bats and spikes cost a life.",
            "- Controls",
            "4/6: walk  5: jump",
            "2/8: climb ladders",
            "1/3: jump left / right",
        };
    }

    protected boolean lowerIsBetter() { return true; }

    protected String formatScore(int s) { return Gfx.time(s, tickMs); }

    protected int accent() { return 0xFF9F1C; }

    protected void newGame() {
        for (int r = 0; r < 6; r++) {
            maps[r] = new TileMap(ROOMS[r]);
            maps[r].solids = "#D";
            maps[r].oneWay = "=H";
            batAlive[r] = false;
            int b = maps[r].find('b');
            if (b >= 0) {
                maps[r].set(b & 0xFFFF, b >> 16, ' ');
                batX[r] = b & 0xFFFF;
                batY[r] = b >> 16;
                batPhase[r] = r * 40;
                batAlive[r] = true;
            }
        }
        room = 0;
        lives = 3;
        crystals = 0;
        key = false;
        layout();
        int s = maps[0].find('S');
        maps[0].set(s & 0xFFFF, s >> 16, ' ');
        p.w = ts * 5 / 8;
        p.h = ts * 7 / 8;
        entryX = (s & 0xFFFF) * ts;
        entryY = (s >> 16) * ts + ts - p.h;
        p.place(entryX, entryY);
        deadT = 0;
        msg = "Find 5 crystals!";
        msgT = 60;
    }

    private void layout() {
        int hud = Gfx.SMALL.getHeight() + 2;
        ts = Math.max(6, Math.min(W / RC, (H - hud) / RR));
        ox = (W - ts * RC) / 2;
        oy = hud + (H - hud - ts * RR) / 2;
        for (int r = 0; r < 6; r++) if (maps[r] != null) maps[r].ts = ts;
        if (hero == null || spriteTs != ts) {
            spriteTs = ts;
            int sc = Math.max(1, ts / 9);
            hero = Gfx.sprite(new String[] { "..1111..", ".122221.", ".133331.", "..1331..", ".444444.", "34444443", "..4..4..", ".55..55." },
                    new int[] { 0, 0x3E2723, 0xFFB300, 0xFFCC80, 0x2E7D32, 0x4E342E }, sc);
            bat = Gfx.sprite(new String[] { "1......1", "11.11.11", "11122111", ".113311.", "..1111..", "...11..." },
                    new int[] { 0, 0x4A148C, 0x7B1FA2, 0xFFEB3B }, sc);
        }
    }

    private int batPx(int r) {
        return batX[r] * ts + (FMath.sin(batPhase[r]) * ts * 3 >> 10);
    }

    private int batPy(int r) {
        return batY[r] * ts + (FMath.sin(batPhase[r] * 2) * ts >> 11);
    }

    protected void update() {
        layout();
        if (msgT > 0) msgT--;
        if (deadT > 0) {
            if (--deadT == 0) {
                if (lives <= 0) {
                    score = 0;
                    endGame(false);
                    return;
                }
                p.place(entryX, entryY);
            }
            return;
        }
        TileMap m = maps[room];
        boolean onLadder = m.get(m.tile(p.cx()), m.tile(p.cy())) == 'H' || m.get(m.tile(p.cx()), m.tile(p.py() + p.h)) == 'H';
        boolean up = (held & K_UP) != 0, down = (held & K_DOWN) != 0;
        if (onLadder && (up || down)) climbing = true;
        if (!onLadder) climbing = false;
        int run = (ts << 8) * 28 / 100;
        boolean left = (held & (K_LEFT | (K_NUM0 << 1))) != 0, right = (held & (K_RIGHT | (K_NUM0 << 3))) != 0;
        if (left && !right) { p.vx = -run; facing = -1; }
        else if (right && !left) { p.vx = run; facing = 1; }
        else p.vx = 0;
        if (climbing) {
            p.vy = up ? -(ts << 8) / 5 : (down ? (ts << 8) / 5 : 0);
            p.dropThrough = true;
            if (up || down) {
                int lc = m.tile(p.cx());
                p.x = ((lc * ts + ts / 2 - p.w / 2) << 8); // snap to ladder centre
                p.vx = 0;
            }
        } else {
            p.vy += (ts << 8) / 9;
            if (p.vy > (ts << 8) * 85 / 100) p.vy = (ts << 8) * 85 / 100;
        }
        if ((tapped & (K_FIRE | (K_NUM0 << 1) | (K_NUM0 << 3))) != 0) jumpBuf = 4;
        else if (jumpBuf > 0) jumpBuf--;
        if (p.ground || climbing) coyote = 4;
        else if (coyote > 0) coyote--;
        if (jumpBuf > 0 && coyote > 0) {
            p.vy = -(ts << 8) * 90 / 100;
            climbing = false;
            jumpBuf = 0;
            coyote = 0;
            Sfx.tone(72, 20);
        }
        if (down && !climbing) p.dropThrough = true;
        p.move(m);
        // room transitions
        int roomX = room % 3, roomY = room / 3;
        if (p.cx() < 0 && roomX > 0) enter(room - 1, RC * ts - p.w - 1, p.py());
        else if (p.cx() >= RC * ts && roomX < 2) enter(room + 1, 1, p.py());
        else if (p.py() + p.h / 2 >= RR * ts && roomY == 0) enter(room + 3, p.px(), 0);
        else if (p.py() + p.h / 2 < 0 && roomY == 1) enter(room - 3, p.px(), RR * ts - p.h - 1);
        tiles();
        if (state != PLAY) return;
        if (batAlive[room]) {
            batPhase[room] = (batPhase[room] + 2) & 255;
            int bx = batPx(room), by = batPy(room);
            if (Math.abs(bx + ts / 2 - p.cx()) < ts * 2 / 3 && Math.abs(by + ts / 2 - p.cy()) < ts * 2 / 3) die();
        }
        score = frame;
    }

    private void enter(int r, int x, int y) {
        room = r;
        maps[r].ts = ts;
        p.x = x << 8;
        p.y = y << 8;
        entryX = x;
        entryY = y;
        Sfx.click();
    }

    private void tiles() {
        TileMap m = maps[room];
        int c0 = m.tile(p.px() + 1), c1 = m.tile(p.px() + p.w - 2), r0 = m.tile(p.py() + 1), r1 = m.tile(p.py() + p.h - 1);
        for (int r = r0; r <= r1; r++) {
            for (int c = c0; c <= c1; c++) {
                char ch = m.get(c, r);
                if (ch == 'C') {
                    m.set(c, r, ' ');
                    crystals++;
                    msg = crystals + " / " + CRYSTALS + " crystals";
                    msgT = 40;
                    Sfx.good();
                } else if (ch == 'K') {
                    m.set(c, r, ' ');
                    key = true;
                    msg = "Got the key!";
                    msgT = 40;
                    Sfx.good();
                } else if (ch == '^') {
                    die();
                    return;
                } else if (ch == 'X') {
                    if (crystals >= CRYSTALS) {
                        score = frame;
                        headline = "ESCAPED!";
                        endGame(true);
                        return;
                    } else if (msgT == 0) {
                        msg = "Need all 5 crystals";
                        msgT = 40;
                    }
                }
            }
        }
        // door: touching it with the key opens it
        if (key) {
            int side = facing > 0 ? m.tile(p.px() + p.w) : m.tile(p.px() - 1);
            for (int r = r0; r <= r1; r++) {
                if (m.get(side, r) == 'D') {
                    for (int rr = 0; rr < RR; rr++) if (m.get(side, rr) == 'D') m.set(side, rr, ' ');
                    msg = "The door creaks open";
                    msgT = 40;
                    Sfx.tone(40, 120);
                }
            }
        }
    }

    private void die() {
        if (deadT > 0) return;
        lives--;
        deadT = 24;
        Sfx.bad();
    }

    protected void draw(Graphics g) {
        layout();
        TileMap m = maps[room];
        g.setColor(0x120C08);
        g.fillRect(0, 0, W, H);
        g.setColor(0x1E1510);
        g.fillRect(ox, oy, RC * ts, RR * ts);
        for (int r = 0; r < RR; r++) {
            for (int c = 0; c < RC; c++) {
                char ch = m.get(c, r);
                int x = ox + c * ts, y = oy + r * ts;
                switch (ch) {
                    case '#':
                        g.setColor(((c * 7 + r * 3) % 5) == 0 ? 0x5D4037 : 0x4E342E);
                        g.fillRect(x, y, ts, ts);
                        if (!m.isSolid(c, r - 1) && r > 0) {
                            g.setColor(0x795548);
                            g.fillRect(x, y, ts, 2);
                        }
                        break;
                    case '=':
                        g.setColor(0x8D6E63);
                        g.fillRect(x, y, ts, Math.max(2, ts / 4));
                        break;
                    case 'H':
                        g.setColor(0xA1887F);
                        g.fillRect(x + ts / 5, y, 2, ts);
                        g.fillRect(x + ts - ts / 5 - 2, y, 2, ts);
                        for (int k = 0; k < ts; k += Math.max(3, ts / 3)) g.fillRect(x + ts / 5, y + k, ts * 3 / 5, 1);
                        break;
                    case '^':
                        g.setColor(0xBDBDBD);
                        g.fillTriangle(x, y + ts, x + ts / 4, y + ts / 3, x + ts / 2, y + ts);
                        g.fillTriangle(x + ts / 2, y + ts, x + ts * 3 / 4, y + ts / 3, x + ts, y + ts);
                        break;
                    case 'C': {
                        int glow = (clock / 4 + c) % 3;
                        g.setColor(glow == 0 ? 0x80DEEA : 0x26C6DA);
                        g.fillTriangle(x + ts / 2, y + 1, x + 2, y + ts / 2, x + ts / 2, y + ts - 1);
                        g.setColor(0x00838F);
                        g.fillTriangle(x + ts / 2, y + 1, x + ts - 2, y + ts / 2, x + ts / 2, y + ts - 1);
                        break;
                    }
                    case 'K':
                        g.setColor(0xFFD54F);
                        Gfx.ring(g, x + ts / 3, y + ts / 2, ts / 4);
                        g.fillRect(x + ts / 2, y + ts / 2, ts / 2 - 1, 2);
                        g.fillRect(x + ts - 3, y + ts / 2, 2, ts / 4);
                        break;
                    case 'D':
                        g.setColor(0x546E7A);
                        g.fillRect(x + 1, y, ts - 2, ts);
                        g.setColor(0x37474F);
                        g.drawLine(x + ts / 2, y, x + ts / 2, y + ts);
                        g.setColor(0xFFD54F);
                        g.fillRect(x + ts / 2 - 1, y + ts / 2, 2, 2);
                        break;
                    case 'X':
                        g.setColor((clock & 4) == 0 ? 0xFFF59D : 0xFFEE58);
                        g.fillRoundRect(x, y - ts, ts, ts * 2, ts / 2, ts / 2);
                        break;
                    default:
                        break;
                }
            }
        }
        if (batAlive[room]) {
            int f = (clock / 3) & 1;
            g.drawRegion(bat, 0, 0, bat.getWidth(), bat.getHeight(), f == 0 ? 0 : 1, ox + batPx(room) + ts / 2, oy + batPy(room) + ts / 2, Gfx.CC);
        }
        if (deadT == 0 || (clock & 2) == 0) {
            g.drawRegion(hero, 0, 0, hero.getWidth(), hero.getHeight(), facing < 0 ? 2 : 0,
                    ox + p.px() + p.w / 2, oy + p.py() + p.h, Graphics.HCENTER | Graphics.BOTTOM);
        }
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x80DEEA);
        g.drawString("C " + crystals + "/" + CRYSTALS, 2, 1, Gfx.TL);
        g.setColor(key ? 0xFFD54F : 0x5D4037);
        g.drawString("KEY", W / 2 - 14, 1, Gfx.TL);
        g.setColor(0xFF8A80);
        g.drawString("x" + lives, W - 2, 1, Gfx.TR);
        g.setFont(Gfx.SMALL);
        g.setColor(0xBCAAA4);
        g.drawString("R" + (room + 1), W / 2 + 14, 1, Gfx.TL);
        if (msgT > 0) Gfx.shadowText(g, msg, W / 2, oy + ts, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        ts = Math.max(8, h / 4);
        int sc = Math.max(1, ts / 9);
        if (hero == null || spriteTs != -sc) {
            spriteTs = -sc;
            hero = Gfx.sprite(new String[] { "..1111..", ".122221.", ".133331.", "..1331..", ".444444.", "34444443", "..4..4..", ".55..55." },
                    new int[] { 0, 0x3E2723, 0xFFB300, 0xFFCC80, 0x2E7D32, 0x4E342E }, sc);
        }
        g.setColor(0x4E342E);
        g.fillRect(x, y + h - ts / 2, w, ts / 2);
        g.fillTriangle(x, y, x + w / 4, y, x, y + h / 2);
        g.fillTriangle(x + w, y, x + w * 3 / 4, y, x + w, y + h / 2);
        int glow = (clock / 4) % 3;
        g.setColor(glow == 0 ? 0x80DEEA : 0x26C6DA);
        g.fillTriangle(x + w * 3 / 4, y + h / 3, x + w * 3 / 4 - 5, y + h / 2, x + w * 3 / 4, y + h * 2 / 3);
        g.fillTriangle(x + w * 3 / 4, y + h / 3, x + w * 3 / 4 + 5, y + h / 2, x + w * 3 / 4, y + h * 2 / 3);
        g.drawImage(hero, x + w / 4 + (clock % 40), y + h - ts / 2, Graphics.HCENTER | Graphics.BOTTOM);
    }
}
