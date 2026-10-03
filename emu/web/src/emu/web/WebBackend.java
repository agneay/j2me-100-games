package emu.web;

import emu.Backend;
import javax.microedition.lcdui.EmuHost;
import javax.microedition.midlet.MIDlet;
import org.teavm.jso.JSBody;

/**
 * Browser backend for the TeaVM-compiled demos. The hosting page provides a
 * small window.j2me object (see website/assets/phone.js) that draws frames,
 * queues key events, plays tones and persists record stores in localStorage.
 */
public final class WebBackend implements Backend {

    public static void run(MIDlet m) {
        EmuHost.init(new WebBackend(), screenW(), screenH());
        try {
            MIDlet.emuStart(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JSBody(script = "return (window.j2me && window.j2me.width) || 176;")
    private static native int screenW();

    @JSBody(script = "return (window.j2me && window.j2me.height) || 208;")
    private static native int screenH();

    @JSBody(params = { "px", "w", "h" }, script = "window.j2me.present(px, w, h);")
    private static native void present0(int[] px, int w, int h);

    @JSBody(script = "if (window.j2me.exit) window.j2me.exit();")
    private static native void exit0();

    @JSBody(params = { "k" }, script = "var v = window.localStorage ? window.localStorage.getItem(window.j2me.store + k) : null; return v;")
    private static native String load0(String key);

    @JSBody(params = { "k", "v" }, script = "try { window.localStorage.setItem(window.j2me.store + k, v); } catch (e) {}")
    private static native void save0(String key, String value);

    @JSBody(params = { "k" }, script = "try { window.localStorage.removeItem(window.j2me.store + k); } catch (e) {}")
    private static native void delete0(String key);

    @JSBody(params = { "n", "ms", "v" }, script = "if (window.j2me.tone) window.j2me.tone(n, ms, v);")
    private static native void tone0(int note, int ms, int vol);

    @JSBody(params = { "k" }, script = "var p = window.j2me.props; return p && p[k] !== undefined ? String(p[k]) : null;")
    private static native String prop0(String key);

    @JSBody(script = "var q = window.j2me.keys; return q && q.length ? q.shift() : -1;")
    private static native int poll0();

    public void present(int[] pixels, int w, int h) { present0(pixels, w, h); }
    public void exit() { exit0(); }
    public void tone(int note, int ms, int volume) { tone0(note, ms, volume); }
    public String appProperty(String key) { return prop0(key); }
    public int pollKey() { return poll0(); }
    public void deleteStore(String name) { delete0(name); }

    private static final String HEX = "0123456789abcdef";

    public byte[][] loadStore(String name) {
        String s = load0(name);
        if (s == null) return null;
        if (s.length() == 0) return new byte[0][];
        int n = 1;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '|') n++;
        byte[][] out = new byte[n][];
        int start = 0;
        for (int r = 0; r < n; r++) {
            int end = s.indexOf('|', start);
            if (end < 0) end = s.length();
            String part = s.substring(start, end);
            if (!part.equals("~")) {
                byte[] b = new byte[part.length() / 2];
                for (int i = 0; i < b.length; i++) {
                    b[i] = (byte) ((HEX.indexOf(part.charAt(2 * i)) << 4) | HEX.indexOf(part.charAt(2 * i + 1)));
                }
                out[r] = b;
            }
            start = end + 1;
        }
        return out;
    }

    public void saveStore(String name, byte[][] records) {
        StringBuffer sb = new StringBuffer();
        for (int r = 0; r < records.length; r++) {
            if (r > 0) sb.append('|');
            byte[] b = records[r];
            if (b == null) {
                sb.append('~');
                continue;
            }
            for (int i = 0; i < b.length; i++) {
                sb.append(HEX.charAt((b[i] >> 4) & 15)).append(HEX.charAt(b[i] & 15));
            }
        }
        save0(name, sb.toString());
    }
}
