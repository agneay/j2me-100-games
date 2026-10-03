package javax.microedition.lcdui;

import emu.Backend;

/**
 * Emulator-only glue (not part of MIDP). Owns the screen buffer, the key
 * event queue and the current Displayable. Key events posted by the host
 * are delivered on the game's own thread inside repaint/serviceRepaints,
 * which keeps the emulator single-threaded from the game's point of view.
 */
public final class EmuHost {
    public static final int KEY_PRESS = 0, KEY_RELEASE = 1, KEY_REPEAT = 2;

    public static Backend backend;
    public static final Object LOCK = new Object();

    static int width = 176, height = 208;
    static int[] screen;
    static Displayable current;
    static boolean dirty = true;

    private static final int QN = 64;
    private static final int[] qCode = new int[QN];
    private static final int[] qType = new int[QN];
    private static int qHead, qTail;
    private static long frames;

    private EmuHost() {}

    public static void init(Backend b, int w, int h) {
        backend = b;
        width = w;
        height = h;
        screen = new int[w * h];
        for (int i = 0; i < screen.length; i++) screen[i] = 0xFF000000;
        current = null;
        dirty = true;
        frames = 0;
        synchronized (qCode) {
            qHead = qTail = 0;
        }
    }

    public static int width() { return width; }
    public static int height() { return height; }
    public static int[] screen() { return screen; }
    public static long frames() { return frames; }
    public static Displayable current() { return current; }

    /** Thread-safe: queue a key event for delivery on the game thread. */
    public static void postKey(int code, int type) {
        synchronized (qCode) {
            int next = (qTail + 1) % QN;
            if (next == qHead) return; // queue full: drop
            qCode[qTail] = code;
            qType[qTail] = type;
            qTail = next;
        }
    }

    static void deliverKeys() {
        if (backend != null) {
            for (int v = backend.pollKey(); v >= 0; v = backend.pollKey()) postKey((v >> 2) - 1000, v & 3);
        }
        while (true) {
            int code, type;
            synchronized (qCode) {
                if (qHead == qTail) return;
                code = qCode[qHead];
                type = qType[qHead];
                qHead = (qHead + 1) % QN;
            }
            Displayable d = current;
            if (d instanceof Canvas) {
                Canvas c = (Canvas) d;
                if (type == KEY_PRESS) c.keyPressed(code);
                else if (type == KEY_RELEASE) c.keyReleased(code);
                else c.keyRepeated(code);
            }
        }
    }

    /** Deliver pending keys and paint if a repaint was requested. */
    public static void service() {
        synchronized (LOCK) {
            deliverKeys();
            if (dirty) {
                dirty = false;
                paintNow();
            }
        }
    }

    static void paintNow() {
        Displayable d = current;
        if (d instanceof Canvas && screen != null) {
            Graphics g = new Graphics(screen, width, height);
            try {
                ((Canvas) d).paint(g);
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
            frames++;
            if (backend != null) backend.present(screen, width, height);
        }
    }
}
