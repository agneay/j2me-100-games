package hoopshot;

import gamekit.FMath;
import gamekit.Game;
import gamekit.Gfx;
import gamekit.Rnd;
import gamekit.Sfx;
import javax.microedition.lcdui.Graphics;

/**
 * Hoop Shot: a 60-second basketball shoot-out. Lock the angle, lock the
 * power, and let gravity, the rim and the backboard do the rest.
 */
public class HoopGame extends Game {
    private static final int AIM = 0, POWER = 1, FLY = 2;
    private int phase, angle, angDir, power, powDir;
    private int bx, by, vx, vy, br;           // ball, <<8
    private int shooterX, floorY, rimX, rimY, rimW, boardX;
    private int timeLeft, streak, made, shots, spot, flash;
    private boolean scored, passedTop;
    private String flashText = "";

    protected String name() { return "Hoop Shot"; }

    protected String[] help() {
        return new String[] {
            "Sink as many baskets as you can in 60 seconds.",
            "Press 5 to stop the swinging aim arrow, then 5 again to stop the power meter. The ball bounces off the rim and the backboard, so banked shots count too.",
            "Long shots are worth 3. Three baskets in a row and you're on fire: double points until you miss.",
            "- Controls",
            "5: lock aim, then lock power",
        };
    }

    protected int accent() { return 0xFF7043; }

    protected void newGame() {
        timeLeft = 60 * 20;
        streak = made = shots = 0;
        spot = 0;
        layout();
        ready();
    }

    private void layout() {
        floorY = H - Math.max(10, H / 12);
        boardX = W - Math.max(8, W / 14);
        rimW = Math.max(14, W / 9);
        rimX = boardX - rimW;
        rimY = floorY - H * 45 / 100;
        br = Math.max(3, W / 36);
    }

    private void ready() {
        int[] spots = { 35, 22, 48, 15, 30 };
        shooterX = W * spots[spot % spots.length] / 100;
        phase = AIM;
        angle = 30;
        angDir = 1;
        bx = shooterX << 8;
        by = (floorY - H / 5) << 8;
        scored = false;
        passedTop = false;
    }

    private boolean threePointer() {
        return rimX - shooterX > W * 55 / 100;
    }

    protected void update() {
        layout();
        if (flash > 0) flash--;
        if (--timeLeft <= 0 && phase != FLY) {
            score = Math.max(score, 0);
            headline = made + " BASKETS";
            endGame(true);
            return;
        }
        switch (phase) {
            case AIM:
                angle += angDir * 2;
                if (angle > 70 || angle < 25) angDir = -angDir;
                if ((pressed & K_FIRE) != 0) {
                    phase = POWER;
                    power = 0;
                    powDir = 1;
                }
                break;
            case POWER:
                power += powDir * 3;
                if (power >= 100) { power = 100; powDir = -1; }
                if (power <= 0) { power = 0; powDir = 1; }
                if ((pressed & K_FIRE) != 0) shoot();
                break;
            default:
                fly();
                break;
        }
    }

    private void shoot() {
        phase = FLY;
        shots++;
        int dist = rimX - shooterX;
        int sp = ((dist + H / 2) << 8) / 28 * (60 + power) / 110;
        int a256 = 256 - angle * 256 / 360; // up and to the right
        vx = FMath.cos(a256) * sp >> 10;
        vy = FMath.sin(a256) * sp >> 10;
        Sfx.tone(60, 30);
    }

    private void fly() {
        int g = (H << 8) / 900;
        for (int step = 0; step < 2; step++) {
            int prevY = by >> 8;
            vy += g / 2;
            bx += vx / 2;
            by += vy / 2;
            int x = bx >> 8, y = by >> 8;
            // backboard
            if (x + br >= boardX && y > rimY - H / 5 && y < rimY + 4 && vx > 0) {
                bx = (boardX - br) << 8;
                vx = -vx * 6 / 10;
                Sfx.tone(45, 20);
            }
            // rim points (front and back)
            bounceOff(rimX, rimY);
            bounceOff(boardX - 2, rimY);
            // scoring: ball centre passes downward through the hoop opening
            if (y < rimY - br) passedTop = true;
            // crossing the rim plane downward between the rim points
            if (!scored && passedTop && vy > 0 && prevY < rimY && y >= rimY && x > rimX + 1 && x < boardX - 1) {
                scored = true;
                made++;
                streak++;
                int pts = threePointer() ? 3 : 2;
                if (streak >= 3) pts *= 2;
                score += pts;
                flashText = streak >= 3 ? "ON FIRE! +" + pts : (threePointer() ? "THREE! +3" : "SWISH +2");
                flash = 25;
                Sfx.good();
            }
            if (y + br >= floorY) {
                by = (floorY - br) << 8;
                vy = -vy * 5 / 10;
                vx = vx * 8 / 10;
                Sfx.tone(35, 20);
            }
        }
        int x = bx >> 8;
        if ((Math.abs(vy) < 80 && (by >> 8) >= floorY - br - 1) || x < -20 || x > W + 20) {
            if (!scored) {
                streak = 0;
                flashText = "Miss";
                flash = 15;
            } else {
                spot++;
            }
            ready();
        }
    }

    private void bounceOff(int px, int py) {
        int dx = bx - (px << 8), dy = by - (py << 8);
        int r = (br + 1) << 8;
        if (Math.abs(dx) > r || Math.abs(dy) > r) return;
        int d = FMath.sqrt((dx >> 4) * (dx >> 4) + (dy >> 4) * (dy >> 4)) << 4;
        if (d >= r || d == 0) return;
        // reflect velocity about the contact normal
        int nx = (int) ((long) dx * 1024 / d), ny = (int) ((long) dy * 1024 / d);
        int dot = (int) (((long) vx * nx + (long) vy * ny) >> 10);
        if (dot >= 0) return;
        vx -= (int) ((long) 2 * dot * nx >> 10);
        vy -= (int) ((long) 2 * dot * ny >> 10);
        vx = vx * 7 / 10;
        vy = vy * 7 / 10;
        bx = (px << 8) + (int) ((long) nx * r >> 10);
        by = (py << 8) + (int) ((long) ny * r >> 10);
        Sfx.tone(70, 15);
    }

    protected void draw(Graphics g) {
        layout();
        g.setColor(0x1A237E);
        g.fillRect(0, 0, W, floorY);
        g.setColor(0x283593);
        for (int k = 0; k < 5; k++) g.fillRect(0, floorY - (k + 1) * H / 8, W, 1);
        g.setColor(0xC68642);
        g.fillRect(0, floorY, W, H - floorY);
        g.setColor(0xA0522D);
        for (int x = 0; x < W; x += 10) g.drawLine(x, floorY, x, H);
        g.setColor(0xFFFFFF);
        int threeX = rimX - W * 55 / 100;
        g.drawLine(threeX, floorY, threeX, floorY + 3);
        // hoop
        g.setColor(0xECEFF1);
        g.fillRect(boardX, rimY - H / 5, 3, H / 5 + 6);
        g.setColor(0x757575);
        g.fillRect(boardX + 3, rimY - 2, W - boardX, 3);
        g.fillRect(W - 3, rimY, 3, floorY - rimY);
        g.setColor(0xFFFFFF);
        for (int k = 0; k <= 4; k++) {
            int nx = rimX + k * rimW / 4;
            g.drawLine(nx, rimY, rimX + rimW / 4 + k * rimW / 8, rimY + rimW * 2 / 3);
        }
        g.setColor(0xFF5722);
        g.fillRect(rimX, rimY - 1, rimW, 3);
        // shooter
        int sx = shooterX, sy = floorY;
        g.setColor(0x1565C0);
        g.fillRect(sx - 4, sy - 22, 8, 12);
        g.setColor(0x6D4C41);
        Gfx.disc(g, sx, sy - 26, 4);
        g.setColor(0xFFFFFF);
        g.fillRect(sx - 4, sy - 10, 3, 10);
        g.fillRect(sx + 1, sy - 10, 3, 10);
        if (phase != FLY) {
            int a256 = 256 - angle * 256 / 360;
            int len = Math.max(16, W / 6);
            g.setColor(0xFFEB3B);
            g.setStrokeStyle(Graphics.DOTTED);
            g.drawLine(bx >> 8, by >> 8, (bx >> 8) + (FMath.cos(a256) * len >> 10), (by >> 8) + (FMath.sin(a256) * len >> 10));
            g.setStrokeStyle(Graphics.SOLID);
        }
        // ball
        int x = bx >> 8, y = by >> 8;
        g.setColor(0xFF7043);
        Gfx.disc(g, x, y, br);
        g.setColor(0x4E342E);
        g.drawLine(x - br, y, x + br, y);
        g.drawLine(x, y - br, x, y + br);
        if (phase == POWER) Gfx.bar(g, 4, floorY - H / 2, 6, H / 3, power, 100, 0xFF7043, 0x0D1442);
        int hud = Gfx.SMALL.getHeight() + 2;
        g.setFont(Gfx.SMALL_B);
        g.setColor(0xFFFFFF);
        g.drawString(score + " pts", 2, 1, Gfx.TL);
        g.setColor(timeLeft < 200 ? 0xFF5252 : 0xFFEB3B);
        g.drawString(timeLeft / 20 + "s", W - 2, 1, Gfx.TR);
        g.setFont(Gfx.SMALL);
        g.setColor(0x9FA8DA);
        g.drawString(made + "/" + shots, W / 2, 1, Gfx.TC);
        if (streak >= 3) Gfx.text(g, "ON FIRE", W / 2, hud, Gfx.TC, Gfx.SMALL_B, (clock & 2) == 0 ? 0xFF5722 : 0xFFEB3B);
        if (flash > 0) Gfx.shadowText(g, flashText, W / 2, H / 3, Gfx.TC, Gfx.SMALL_B, 0xFFFFFF, 0x000000);
    }

    protected void drawTitleArt(Graphics g, int x, int y, int w, int h) {
        g.setColor(0xFF5722);
        g.fillRect(x + w * 3 / 4, y + h / 3, w / 6, 2);
        g.setColor(0xECEFF1);
        g.fillRect(x + w * 3 / 4 + w / 6, y, 2, h / 2);
        int t = clock % 40;
        int bxx = x + w / 6 + (w * 2 / 3) * t / 40, byy = y + h - 4 - (t * (40 - t)) * h / 400;
        g.setColor(0xFF7043);
        Gfx.disc(g, bxx, byy, 4);
    }
}
