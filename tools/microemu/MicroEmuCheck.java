import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.microemu.DisplayAccess;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.app.Headless;
import org.microemu.device.DeviceFactory;
import org.microemu.device.j2se.J2SEDeviceDisplay;

/**
 * Smoke test on the third-party MicroEmulator 2.0.4 runtime (not the
 * project's own reference runtime): load the JAD in MicroEmulator's headless
 * mode, wait for the MIDlet's Canvas, press 5 to start a game, feed random
 * keypad input, and fail on uncaught exceptions, a crashed game loop, a
 * MIDlet that never shows a screen, or a blank display. Writes title and
 * gameplay screenshots rendered by MicroEmulator itself.
 *
 * Usage: MicroEmuCheck <jad file URL> <out dir> <seconds>
 */
public class MicroEmuCheck {
    static final ByteArrayOutputStream log = new ByteArrayOutputStream();

    public static void main(String[] args) throws Exception {
        final String url = args[0];
        File out = new File(args[1]);
        int seconds = Integer.parseInt(args[2]);
        out.mkdirs();
        final PrintStream realOut = System.out;
        PrintStream tee = new PrintStream(new OutputStream() {
            public void write(int b) { log.write(b); }
        }, true);
        System.setOut(tee);
        System.setErr(tee);
        String verdict = run(url, out, seconds);
        // Headless CI machines have no sound device: MicroEmulator's own tone generator then logs
        // "No line matching interface SourceDataLine". That is the host, not the game, so drop
        // those lines (and their stack traces) before looking for exceptions.
        String text = withoutAudioErrors(log.toString());
        if (verdict == null && (text.contains("Exception") || text.contains("Error:"))) {
            int i = text.indexOf("Exception");
            if (i < 0) i = text.indexOf("Error:");
            int s = Math.max(0, text.lastIndexOf('\n', Math.max(0, i - 1)));
            int e = text.indexOf('\n', i);
            verdict = "exception logged: " + text.substring(s, e < 0 ? text.length() : e).trim();
        }
        java.nio.file.Files.write(new File(out, "microemu.log").toPath(), log.toByteArray());
        realOut.println(verdict == null ? "PASS" : "FAIL " + verdict);
        realOut.flush();
        Runtime.getRuntime().halt(verdict == null ? 0 : 1);
    }

    static String withoutAudioErrors(String text) {
        StringBuffer out = new StringBuffer();
        boolean skipping = false;
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i];
            if (l.indexOf("SourceDataLine") >= 0 || l.indexOf("javax.sound") >= 0) {
                skipping = true;
                continue;
            }
            String t = l.trim();
            if (skipping && (t.startsWith("at ") || t.startsWith("..."))) continue;
            skipping = false;
            out.append(l).append('\n');
        }
        return out.toString();
    }

    static DisplayAccess display() {
        MIDletAccess ma = MIDletBridge.getMIDletAccess();
        return ma == null ? null : ma.getDisplayAccess();
    }

    static String run(final String url, File out, int seconds) throws Exception {
        Thread t = new Thread(new Runnable() {
            public void run() {
                Headless.main(new String[] { url });
            }
        }, "microemu-main");
        t.setDaemon(true);
        t.start();
        long deadline = System.currentTimeMillis() + 10000;
        Displayable cur = null;
        while (System.currentTimeMillis() < deadline) {
            DisplayAccess da = display();
            if (da != null) cur = da.getCurrent();
            if (cur != null) break;
            Thread.sleep(100);
        }
        if (cur == null) return "MIDlet never set a Displayable";
        if (!(cur instanceof javax.microedition.lcdui.Canvas)) return "first screen is not a Canvas: " + cur.getClass().getName();
        Thread.sleep(1200);
        String blank = shot(new File(out, "microemu-title.png"));
        if (blank != null) return "title screen " + blank;
        Random rnd = new Random(42);
        int[] keys = { 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 35, -1, -2, -3, -4, -5 };
        key(53);
        Thread.sleep(300);
        long end = System.currentTimeMillis() + seconds * 1000L;
        int n = 0;
        while (System.currentTimeMillis() < end) {
            int k = keys[rnd.nextInt(keys.length)];
            if (n % 12 == 0) k = 53;
            DisplayAccess da = display();
            if (da == null) return "display went away";
            da.keyPressed(k);
            Thread.sleep(40 + rnd.nextInt(120));
            da.keyReleased(k);
            Thread.sleep(30 + rnd.nextInt(60));
            n++;
            String c = crashed();
            if (c != null) return "game loop crashed: " + c;
        }
        String c = crashed();
        if (c != null) return "game loop crashed: " + c;
        blank = shot(new File(out, "microemu-play.png"));
        if (blank != null) return "gameplay screen " + blank;
        return null;
    }

    static void key(int k) throws InterruptedException {
        DisplayAccess da = display();
        if (da == null) return;
        da.keyPressed(k);
        Thread.sleep(60);
        da.keyReleased(k);
    }

    static String crashed() {
        try {
            MIDlet m = MIDletBridge.getCurrentMIDlet();
            if (m == null) return null;
            Class g = m.getClass().getClassLoader().loadClass("gamekit.Game");
            Object v = g.getField("crashed").get(null);
            return v == null ? null : v.toString();
        } catch (Throwable e) {
            return null;
        }
    }

    /** Renders the current screen through MicroEmulator's display; returns a problem or null. */
    static String shot(File f) throws Exception {
        J2SEDeviceDisplay dd = (J2SEDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay();
        int w = dd.getFullWidth(), h = dd.getFullHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        dd.paintDisplayable(g, 0, 0, w, h);
        g.dispose();
        ImageIO.write(img, "png", f);
        Set colours = new HashSet();
        for (int y = 0; y < h; y += 2) for (int x = 0; x < w; x += 2) {
            colours.add(Integer.valueOf(img.getRGB(x, y)));
            if (colours.size() > 4) return null;
        }
        return "is blank (" + colours.size() + " colours at " + w + "x" + h + ")";
    }
}
