package lostkeep;

import gamekit.Game;
import gamekit.Gfx;
import gamekit.Sfx;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Lost Keep: a graphical text adventure. Explore a ruined keep through a
 * menu of context actions (no typing needed), solve the item puzzles and
 * return the gem to the statue on the tower.
 */
public class KeepGame extends Game {
    private static final int N = 0, E = 1, S = 2, Wd = 3, U = 4, D = 5;
    private static final String[] DIR = { "north", "east", "south", "west", "up", "down" };
    private static final String[] ROOM = { "Courtyard", "Gatehouse", "Great Hall", "Kitchen", "Library", "Tower Stair",
            "Cellar", "Crypt", "Armoury", "Tower Top" };
    private static final String[] DESC = {
        "Weeds crack the flagstones of a ruined courtyard. The keep looms to the north; a gatehouse lies east.",
        "A collapsed gatehouse. Rusted chains hang from the portcullis winch.",
        "A vast hall with a long-dead hearth. Tattered banners hang from the rafters.",
        "Pots and pans lie scattered. Embers still glow in the old stove. A hatch leads down.",
        "Shelves of mouldering books. High above, a balcony leads north.",
        "A spiral stair winds up into the tower.",
        "Wine racks and cobwebs. A cold draught blows from a crack in the east wall.",
        "Stone coffins line the walls. Beyond them, something glitters in an alcove.",
        "Empty weapon racks line the walls - all but one.",
        "Wind howls around a stone statue holding an empty setting in its hands.",
    };
    // exits[room][dir] = target room or -1
    private static final int[][] EXITS = {
        { 2, 1, -1, -1, -1, -1 }, { -1, -1, -1, 0, -1, -1 }, { 5, 3, 0, 4, -1, -1 }, { -1, -1, -1, 2, -1, 6 },
        { 8, 2, -1, -1, -1, -1 }, { -1, -1, 2, -1, 9, -1 }, { -1, 7, -1, -1, 3, -1 }, { -1, -1, -1, 6, -1, -1 },
        { -1, -1, 4, -1, -1, -1 }, { -1, -1, -1, -1, -1, 5 } };
    private static final String[] ITEM = { "lamp", "rusty key", "bread", "rope", "old book", "silver sword", "gem" };
    private static final int[] ITEM_START = { 1, 3, 3, 6, 4, 8, 7 };

    private final int[] itemRoom = new int[ITEM.length]; // -1 = carried, -2 = used up
    private int room, moves;
    private boolean lampLit, libraryOpen, dogFed, ropeTied, ghostGone;
    private String message = "";
    private final Vector actions = new Vector();
    private final Vector actCode = new Vector();
    private int sel, top;
    private Vector wrapped;
    private int wrapW = -1;
    private String wrapText = "";

    protected String name() { return "Lost Keep"; }

    protected String[] help() {
        return new String[] {
            "A text adventure you play with the keypad. Each turn choose an action from the menu: look around, go somewhere, take things or use the items you carry.",
            "Explore the ruined keep, solve its little puzzles and restore what was taken from the tower. Fewer moves earns a better score.",
            "Stuck? Read everything, and try using items in different places.",
            "- Controls",
            "2/8: choose action",
            "5: do it",
        };
    }

    protected int accent() { return 0x9B5DE5; }

    protected void newGame() {
        for (int i = 0; i < ITEM.length; i++) itemRoom[i] = ITEM_START[i];
        room = 0;
        moves = 0;
        lampLit = libraryOpen = dogFed = ropeTied = ghostGone = false;
        message = "You wake in the courtyard of a ruined keep.";
        rebuild();
    }

    private boolean carrying(int it) { return itemRoom[it] == -1; }

    private boolean dark() { return room == 6 && !lampLit; }

    private void add(String label, int code) {
        actions.addElement(label);
        actCode.addElement(new Integer(code));
    }

    private void rebuild() {
        actions.removeAllElements();
        actCode.removeAllElements();
        add("Look around", 0);
        for (int d = 0; d < 6; d++) if (EXITS[room][d] >= 0) add("Go " + DIR[d], 100 + d);
        if (!dark()) {
            for (int i = 0; i < ITEM.length; i++) if (itemRoom[i] == room) add("Take " + ITEM[i], 200 + i);
        }
        for (int i = 0; i < ITEM.length; i++) if (carrying(i)) add("Use " + ITEM[i], 300 + i);
        if (sel >= actions.size()) sel = actions.size() - 1;
        if (sel < 0) sel = 0;
        wrapW = -1;
    }

    private void act(int code) {
        moves++;
        if (code == 0) {
            message = dark() ? "It is pitch dark. You can't see a thing." : DESC[room] + roomExtra();
        } else if (code < 200) {
            go(code - 100);
        } else if (code < 300) {
            take(code - 200);
        } else {
            use(code - 300);
        }
        rebuild();
    }

    private String roomExtra() {
        StringBuffer sb = new StringBuffer();
        if (room == 2 && !dogFed) sb.append(" A scrawny hound growls at the stairway to the north.");
        if (room == 7 && !ghostGone) sb.append(" A ghostly knight bars the way to the alcove.");
        if (room == 4 && !ropeTied) sb.append(" The balcony is out of reach.");
        if (room == 3 && lampLit) sb.append(" You lit your lamp here.");
        for (int i = 0; i < ITEM.length; i++) if (itemRoom[i] == room) sb.append(" You see a ").append(ITEM[i]).append(".");
        return sb.toString();
    }

    private void go(int d) {
        int target = EXITS[room][d];
        if (room == 2 && d == Wd && !libraryOpen) { message = "The library door is locked."; Sfx.bad(); return; }
        if (room == 2 && d == N && !dogFed) { message = "The hound snarls and won't let you pass."; Sfx.bad(); return; }
        if (room == 4 && d == N && !ropeTied) { message = "The balcony is too high to reach."; Sfx.bad(); return; }
        room = target;
        Sfx.click();
        message = dark() ? "You climb down into darkness." : ROOM[room] + ". " + DESC[room] + roomExtra();
    }

    private void take(int it) {
        if (it == 6 && !ghostGone) {
            message = "The ghostly knight raises its blade. You back away.";
            Sfx.bad();
            return;
        }
        itemRoom[it] = -1;
        message = "You take the " + ITEM[it] + ".";
        Sfx.good();
    }

    private void use(int it) {
        switch (it) {
            case 0:
                if (room == 3 && !lampLit) { lampLit = true; message = "You light the lamp from the stove's embers."; Sfx.good(); }
                else message = lampLit ? "The lamp is already lit." : "You have nothing to light it with.";
                break;
            case 1:
                if (room == 2 && !libraryOpen) { libraryOpen = true; itemRoom[1] = -2; message = "The rusty key turns. The library door creaks open to the west."; Sfx.good(); }
                else message = "There's no lock here.";
                break;
            case 2:
                if (room == 2 && !dogFed) { dogFed = true; itemRoom[2] = -2; message = "The hound wolfs down the bread and trots off happily."; Sfx.good(); }
                else message = "You aren't hungry.";
                break;
            case 3:
                if (room == 4 && !ropeTied) { ropeTied = true; itemRoom[3] = -2; message = "You lasso a carved post. Now you can climb to the balcony."; Sfx.good(); }
                else message = "Nothing to tie it to here.";
                break;
            case 4:
                message = "The book reads: 'The restless dead fear only silver.'";
                break;
            case 5:
                if (room == 7 && !ghostGone) { ghostGone = true; message = "You raise the silver sword. The ghost knight bows and fades away."; Sfx.good(); }
                else message = "You swing the sword at nothing.";
                break;
            default:
                if (room == 9) {
                    itemRoom[6] = -2;
                    message = "The gem clicks into place. Light floods the keep!";
                    score = Math.max(100, 1000 - moves * 8);
                    headline = "KEEP RESTORED!";
                    endGame(true);
                    return;
                }
                message = "The gem sparkles in your hand.";
                break;
        }
    }

    protected void update() {
        int n = actions.size();
        if ((pressed & K_UP) != 0) sel = (sel + n - 1) % n;
        if ((pressed & K_DOWN) != 0) sel = (sel + 1) % n;
        if ((pressed & K_FIRE) != 0) act(((Integer) actCode.elementAt(sel)).intValue());
        score = Math.max(100, 1000 - moves * 8);
    }

    // ------------------------------------------------------------- drawing

    protected void draw(Graphics g) {
        int lh = Gfx.SMALL.getHeight() + 1;
        int menuRows = 3;
        int menuH = lh * menuRows + 4;
        int sceneH = Math.max(30, H * 36 / 100);
        int textY = sceneH + 2, textH = H - menuH - textY - 2;
        g.setColor(0x0B0614);
        g.fillRect(0, 0, W, H);
        drawScene(g, 0, 0, W, sceneH);
        g.setFont(Gfx.SMALL_B);
        g.setColor(0x000000);
        g.fillRect(0, 0, Gfx.SMALL_B.stringWidth(ROOM[room]) + 6, lh + 1);
        g.setColor(0xFFD54F);
        g.drawString(ROOM[room], 3, 1, Gfx.TL);
        if (wrapW != W || !wrapText.equals(message)) {
            wrapped = Gfx.wrap(message, Gfx.SMALL, W - 8);
            wrapW = W;
            wrapText = message;
        }
        g.setFont(Gfx.SMALL);
        g.setColor(0xE1D5F5);
        int maxLines = Math.max(1, textH / lh);
        int start = Math.max(0, wrapped.size() - maxLines);
        for (int i = start; i < wrapped.size(); i++) g.drawString((String) wrapped.elementAt(i), 4, textY + (i - start) * lh, Gfx.TL);
        int my = H - menuH;
        g.setColor(0x1D1233);
        g.fillRect(0, my, W, menuH);
        if (sel < top) top = sel;
        if (sel >= top + menuRows) top = sel - menuRows + 1;
        for (int i = 0; i < menuRows && top + i < actions.size(); i++) {
            int y = my + 2 + i * lh;
            boolean on = top + i == sel;
            if (on) {
                g.setColor(0x9B5DE5);
                g.fillRect(2, y - 1, W - 4, lh);
            }
            g.setColor(on ? 0xFFFFFF : 0xB39DDB);
            g.drawString((String) actions.elementAt(top + i), 6, y, Gfx.TL);
        }
        if (actions.size() > menuRows) {
            g.setColor(0x7E57C2);
            int bh = menuH * menuRows / actions.size();
            g.fillRect(W - 3, my + (menuH - bh) * top / Math.max(1, actions.size() - menuRows), 2, bh);
        }
    }

    private void drawScene(Graphics g, int x, int y, int w, int h) {
        if (dark()) {
            g.setColor(0x000000);
            g.fillRect(x, y, w, h);
            g.setColor(0x222222);
            Gfx.disc(g, x + w / 3, y + h / 2, 2);
            Gfx.disc(g, x + w / 3 + 6, y + h / 2, 2);
            return;
        }
        boolean outside = room == 0 || room == 1 || room == 9;
        g.setColor(outside ? (room == 9 ? 0x37474F : 0x5C6BC0) : 0x2A1F33);
        g.fillRect(x, y, w, h);
        int floorY = y + h * 3 / 4;
        g.setColor(outside ? 0x4E5D3A : 0x3B2F2F);
        g.fillRect(x, floorY, w, h - (floorY - y));
        switch (room) {
            case 0:
                g.setColor(0x6D6D6D);
                g.fillRect(x + w / 3, y + h / 6, w / 3, floorY - y - h / 6);
                g.fillRect(x + w / 3 - 4, y + h / 6 - 6, 8, 8);
                g.fillRect(x + w * 2 / 3 - 4, y + h / 6 - 6, 8, 8);
                g.setColor(0x1A1A1A);
                g.fillRoundRect(x + w / 2 - 6, floorY - h / 3, 12, h / 3, 10, 10);
                g.setColor(0x7CB342);
                for (int k = 0; k < 6; k++) g.drawLine(x + k * w / 6 + 4, floorY + 3, x + k * w / 6 + 6, floorY - 1);
                break;
            case 1:
                g.setColor(0x757575);
                g.fillRect(x + w / 5, y + h / 4, w * 3 / 5, floorY - y - h / 4);
                g.setColor(0x3E2723);
                for (int k = 0; k < 5; k++) g.fillRect(x + w / 5 + 4 + k * w / 9, y + h / 4 + 4, 2, floorY - y - h / 4 - 4);
                break;
            case 2:
                g.setColor(0x5D4037);
                for (int k = 1; k < 4; k++) g.fillRect(x + k * w / 4 - 3, y + 6, 6, floorY - y - 6);
                g.setColor(0xB71C1C);
                g.fillTriangle(x + w / 4 + 4, y + 6, x + w / 4 + 14, y + 6, x + w / 4 + 9, y + h / 2);
                g.fillTriangle(x + w / 2 + 4, y + 6, x + w / 2 + 14, y + 6, x + w / 2 + 9, y + h / 2);
                if (!dogFed) dog(g, x + w * 4 / 5, floorY);
                break;
            case 3:
                g.setColor(0x424242);
                g.fillRect(x + w / 6, floorY - h / 3, w / 4, h / 3);
                g.setColor((clock & 4) == 0 ? 0xFF6F00 : 0xFFA000);
                g.fillRect(x + w / 6 + 4, floorY - h / 6, w / 4 - 8, h / 8);
                g.setColor(0x9E9E9E);
                g.fillArc(x + w / 2, floorY - 8, 14, 8, 0, 360);
                g.setColor(0x3E2723);
                g.fillRect(x + w * 3 / 4, floorY + 2, w / 6, 4);
                break;
            case 4:
                for (int r = 0; r < 3; r++) {
                    g.setColor(0x5D4037);
                    g.fillRect(x + 6, y + 8 + r * h / 5, w / 2, 3);
                    for (int k = 0; k < 8; k++) {
                        g.setColor(0x8D6E63 + ((k * 7 + r) % 3) * 0x101010);
                        g.fillRect(x + 8 + k * w / 18, y + 8 + r * h / 5 - 8, w / 22, 8);
                    }
                }
                g.setColor(0x6D4C41);
                g.fillRect(x + w * 2 / 3, y + 6, w / 4, 4);
                if (ropeTied) {
                    g.setColor(0xD7CCC8);
                    g.drawLine(x + w * 3 / 4, y + 10, x + w * 3 / 4, floorY);
                }
                break;
            case 5:
                g.setColor(0x616161);
                for (int k = 0; k < 6; k++) g.fillRect(x + w / 3 + (k % 2) * w / 8, floorY - (k + 1) * h / 9, w / 4, 3);
                break;
            case 6:
                g.setColor(0x4E342E);
                for (int k = 0; k < 4; k++) Gfx.disc(g, x + 12 + k * w / 5, floorY - 8, 8);
                g.setColor(0x9E9E9E);
                g.drawLine(x + w - 10, y + 6, x + w - 14, floorY);
                break;
            case 7:
                g.setColor(0x757575);
                g.fillRect(x + 6, floorY - 10, w / 3, 10);
                g.fillRect(x + w / 2, floorY - 10, w / 3, 10);
                if (!ghostGone) {
                    int bob = (clock / 6) & 1;
                    g.setColor(0xB2EBF2);
                    g.fillRoundRect(x + w * 2 / 3, y + h / 4 + bob, w / 8, h / 2, 6, 6);
                    g.setColor(0x006064);
                    g.fillRect(x + w * 2 / 3 + 3, y + h / 4 + 6 + bob, 2, 2);
                    g.fillRect(x + w * 2 / 3 + 8, y + h / 4 + 6 + bob, 2, 2);
                }
                if (itemRoom[6] == 7) {
                    g.setColor((clock & 4) == 0 ? 0x00E5FF : 0x84FFFF);
                    g.fillTriangle(x + w - 12, y + h / 3, x + w - 16, y + h / 3 + 5, x + w - 8, y + h / 3 + 5);
                }
                break;
            case 8:
                g.setColor(0x5D4037);
                for (int k = 0; k < 4; k++) g.fillRect(x + 10 + k * w / 4, y + h / 4, 3, floorY - y - h / 4);
                if (itemRoom[5] == 8) {
                    g.setColor(0xE0E0E0);
                    g.fillRect(x + w / 2 + 6, y + h / 4, 2, h / 2);
                    g.fillRect(x + w / 2 + 3, y + h / 4 + h / 2 - 6, 8, 2);
                }
                break;
            default:
                g.setColor(0x9E9E9E);
                g.fillRect(x + w / 2 - 6, floorY - h / 2, 12, h / 2);
                Gfx.disc(g, x + w / 2, floorY - h / 2 - 5, 6);
                g.setColor(0xECEFF1);
                for (int k = 0; k < 4; k++) g.drawLine(x + (k * 37 + clock * 3) % w, y + 6 + k * 5, x + (k * 37 + clock * 3) % w + 6, y + 6 + k * 5);
                break;
        }
    }

    private void dog(Graphics g, int x, int floorY) {
        g.setColor(0x8D6E63);
        g.fillRect(x - 8, floorY - 8, 14, 6);
        g.fillRect(x + 4, floorY - 12, 6, 6);
        g.fillRect(x - 7, floorY - 2, 2, 2);
        g.fillRect(x + 3, floorY - 2, 2, 2);
        g.setColor(0x000000);
        g.fillRect(x + 8, floorY - 10, 1, 1);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x283593);
        g.fillRect(x, y, w, h);
        g.setColor(0xFFF59D);
        Gfx.disc(g, x + w - 14, y + 10, 5);
        g.setColor(0x212121);
        g.fillRect(x + w / 3, y + h / 4, w / 3, h * 3 / 4);
        g.fillRect(x + w / 2 - 6, y + 2, 12, h / 4);
        for (int k = 0; k < 4; k++) g.fillRect(x + w / 3 + k * w / 10, y + h / 4 - 4, w / 20, 4);
        g.setColor((clock & 8) == 0 ? 0xFFCA28 : 0xFFA000);
        g.fillRect(x + w / 2 - 2, y + 8, 4, 5);
    }
}
