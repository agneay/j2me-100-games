package emu.desktop;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.EmuHost;
import javax.microedition.midlet.MIDlet;

/**
 * Headless smoke-test runner. Loads a game's JAR exactly as built for phones
 * (the classes are the CLDC/MIDP bytecode), runs it on the emulated MIDP
 * runtime at several screen sizes, drives it with a key script, captures
 * screenshots/GIFs and reports crashes.
 *
 * This is NOT a certified Java ME emulator: it implements the subset of
 * MIDP used by this project. Passing here means "runs on our reference
 * runtime", which the docs label "Harness verified".
 *
 * Script tokens (space separated):
 *   0-9 * # U D L R F SL SR   tap a key (U/D/L/R/F = joystick)
 *   wNNN                       wait NNN ms
 *   hK:NNN                     hold key K for NNN ms
 *   rand:NNN                   random gameplay keys for NNN ms
 *   shot:NAME                  save NAME.png
 *   gif:start / gif:stop:NAME  record frames / write NAME.gif
 */
public final class Harness {
    public static final String DEFAULT_SCRIPT =
            "w600 shot:title # w250 shot:help 5 w200 5 w500 shot:start rand:2500 shot:play * w250 shot:pause 5 w150 rand:2500 shot:play2";

    private final Map<String, String> jad;
    private final File jar;
    private final Random random = new Random(1234);
    private final List<String> uncaught = Collections.synchronizedList(new ArrayList<>());

    private Harness(File jadFile) throws IOException {
        jad = readJad(jadFile);
        jar = new File(jadFile.getParentFile(), jad.get("MIDlet-Jar-URL"));
        if (!jar.isFile()) throw new IOException("jar not found: " + jar);
    }

    static Map<String, String> readJad(File f) throws IOException {
        Map<String, String> m = new LinkedHashMap<>();
        for (String line : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
            int i = line.indexOf(':');
            if (i > 0) m.put(line.substring(0, i).trim(), line.substring(i + 1).trim());
        }
        return m;
    }

    private static final class Result {
        String size;
        boolean ok = true;
        long frames;
        int tones;
        String error;
    }

    private Result run(int w, int h, String script, File outDir) throws Exception {
        Result r = new Result();
        r.size = w + "x" + h;
        DesktopBackend be = new DesktopBackend(jad);
        EmuHost.init(be, w, h);
        String cls = jad.get("MIDlet-1").split(",")[2].trim();
        // the loader is deliberately left open: the game thread may still load classes while shutting down
        URLClassLoader cl = new URLClassLoader(new URL[] { jar.toURI().toURL() }, Harness.class.getClassLoader());
        {
            MIDlet m = (MIDlet) cl.loadClass(cls).getDeclaredConstructor().newInstance();
            MIDlet.emuStart(m);
            outDir.mkdirs();
            for (String tok : script.trim().split("\\s+")) {
                exec(tok, be, outDir, w, h);
                String crash = crashOf(cl);
                if (crash != null || !uncaught.isEmpty()) break;
                if (be.exited()) break;
            }
            long f0 = EmuHost.frames();
            Thread.sleep(200);
            r.frames = EmuHost.frames();
            if (r.frames == f0 && !be.exited()) {
                r.ok = false;
                r.error = "game loop stalled (no frames painted)";
            }
            String crash = crashOf(cl);
            if (crash != null) {
                r.ok = false;
                r.error = "crash: " + crash;
            }
            if (!uncaught.isEmpty()) {
                r.ok = false;
                r.error = "uncaught: " + uncaught.get(0);
                uncaught.clear();
            }
            int[] px = be.lastFrame();
            if (r.ok && (px == null || uniform(px))) {
                r.ok = false;
                r.error = "blank screen";
            }
            r.tones = be.tones();
            MIDlet.emuDestroy(m);
            Thread.sleep(120);
        }
        return r;
    }

    private static boolean uniform(int[] px) {
        for (int p : px) if (p != px[0]) return false;
        return true;
    }

    private static String crashOf(ClassLoader cl) {
        try {
            Class<?> g = Class.forName("gamekit.Game", false, cl);
            Object v = g.getField("crashed").get(null);
            return (String) v;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static int code(String k) {
        switch (k) {
            case "*": return Canvas.KEY_STAR;
            case "#": return Canvas.KEY_POUND;
            case "U": return Canvas.EMU_KEY_UP;
            case "D": return Canvas.EMU_KEY_DOWN;
            case "L": return Canvas.EMU_KEY_LEFT;
            case "R": return Canvas.EMU_KEY_RIGHT;
            case "F": return Canvas.EMU_KEY_SELECT;
            case "SL": return Canvas.EMU_SOFT_LEFT;
            case "SR": return Canvas.EMU_SOFT_RIGHT;
            default:
                if (k.length() == 1 && Character.isDigit(k.charAt(0))) return k.charAt(0);
                throw new IllegalArgumentException("unknown key " + k);
        }
    }

    private void tap(int code, int holdMs) throws InterruptedException {
        EmuHost.postKey(code, EmuHost.KEY_PRESS);
        Thread.sleep(holdMs);
        EmuHost.postKey(code, EmuHost.KEY_RELEASE);
    }

    private void exec(String tok, DesktopBackend be, File outDir, int w, int h) throws Exception {
        if (tok.startsWith("shot:")) {
            Thread.sleep(60);
            writePng(be.lastFrame(), w, h, new File(outDir, tok.substring(5) + ".png"));
        } else if (tok.equals("gif:start")) {
            be.startRecording(2, 160);
        } else if (tok.startsWith("gif:stop:")) {
            List<int[]> frames = be.stopRecording();
            if (frames != null && !frames.isEmpty()) {
                GifWriter.write(frames, w, h, 100, new File(outDir, tok.substring(9) + ".gif"));
            }
        } else if (tok.startsWith("rand:")) {
            long end = System.currentTimeMillis() + Long.parseLong(tok.substring(5));
            String[] keys = { "2", "4", "6", "8", "5", "2", "4", "6", "8", "5", "1", "3", "7", "9", "0", "#" };
            while (System.currentTimeMillis() < end) {
                String k = keys[random.nextInt(keys.length)];
                tap(code(k), 40 + random.nextInt(120));
                Thread.sleep(40 + random.nextInt(160));
            }
        } else if (tok.startsWith("w")) {
            Thread.sleep(Long.parseLong(tok.substring(1)));
        } else if (tok.startsWith("h") && tok.contains(":")) {
            int c = tok.indexOf(':');
            tap(code(tok.substring(1, c)), Integer.parseInt(tok.substring(c + 1)));
        } else {
            tap(code(tok), 60);
            Thread.sleep(90);
        }
    }

    static void writePng(int[] px, int w, int h, File f) throws IOException {
        if (px == null) return;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, px, 0, w);
        ImageIO.write(img, "png", f);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        String jadPath = null, out = "build/test", script = DEFAULT_SCRIPT, report = null;
        String sizes = "128x128,128x160,176x208,176x220,240x320";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--jad": jadPath = args[++i]; break;
                case "--out": out = args[++i]; break;
                case "--sizes": sizes = args[++i]; break;
                case "--script": script = args[++i]; if (script.equals("default")) script = DEFAULT_SCRIPT; break;
                case "--report": report = args[++i]; break;
                default: throw new IllegalArgumentException("unknown arg " + args[i]);
            }
        }
        if (jadPath == null) {
            System.err.println("usage: Harness --jad X.jad [--sizes WxH,...] [--out dir] [--script s] [--report file]");
            System.exit(2);
        }
        Harness hs = new Harness(new File(jadPath));
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            hs.uncaught.add(e.toString());
            e.printStackTrace();
        });
        StringBuilder json = new StringBuilder("{\"game\":\"" + hs.jad.get("MIDlet-Name") + "\",\"results\":[");
        boolean allOk = true;
        String[] list = sizes.split(",");
        for (int i = 0; i < list.length; i++) {
            String[] wh = list[i].trim().split("x");
            int w = Integer.parseInt(wh[0]), h = Integer.parseInt(wh[1]);
            Result r = hs.run(w, h, script, new File(out, w + "x" + h));
            allOk &= r.ok;
            System.out.println((r.ok ? "PASS " : "FAIL ") + hs.jad.get("MIDlet-Name") + " @" + r.size
                    + " frames=" + r.frames + " tones=" + r.tones + (r.error != null ? " " + r.error : ""));
            if (i > 0) json.append(',');
            json.append("{\"size\":\"").append(r.size).append("\",\"ok\":").append(r.ok)
                    .append(",\"frames\":").append(r.frames)
                    .append(",\"error\":").append(r.error == null ? "null" : "\"" + r.error.replace("\"", "'") + "\"")
                    .append('}');
        }
        json.append("],\"ok\":").append(allOk).append('}');
        if (report != null) {
            File rf = new File(report);
            if (rf.getParentFile() != null) rf.getParentFile().mkdirs();
            Files.write(rf.toPath(), json.toString().getBytes(StandardCharsets.UTF_8));
        }
        System.exit(allOk ? 0 : 1);
    }
}
