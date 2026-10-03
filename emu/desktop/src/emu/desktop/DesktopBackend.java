package emu.desktop;

import emu.Backend;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Headless backend: keeps the last frame, records frames, stores RMS in memory. */
public class DesktopBackend implements Backend {
    private final Map<String, byte[][]> stores = new HashMap<>();
    private final Map<String, String> props;
    private int[] last;
    private int w, h;
    private volatile boolean exited;
    private int tones;
    private List<int[]> recording;
    private int recordEvery = 2, recordCount, recordMax = 200;

    public DesktopBackend(Map<String, String> props) {
        this.props = props;
    }

    @Override
    public synchronized void present(int[] pixels, int w, int h) {
        if (last == null || last.length != pixels.length) last = new int[pixels.length];
        System.arraycopy(pixels, 0, last, 0, pixels.length);
        this.w = w;
        this.h = h;
        if (recording != null && recording.size() < recordMax && (recordCount++ % recordEvery) == 0) {
            recording.add(pixels.clone());
        }
    }

    public synchronized int[] lastFrame() {
        return last == null ? null : last.clone();
    }

    public synchronized void startRecording(int every, int max) {
        recording = new ArrayList<>();
        recordEvery = Math.max(1, every);
        recordMax = max;
        recordCount = 0;
    }

    public synchronized List<int[]> stopRecording() {
        List<int[]> r = recording;
        recording = null;
        return r;
    }

    public int width() { return w; }
    public int height() { return h; }
    public boolean exited() { return exited; }
    public int tones() { return tones; }

    @Override public void exit() { exited = true; }
    @Override public byte[][] loadStore(String name) { return stores.get(name); }
    @Override public void saveStore(String name, byte[][] records) { stores.put(name, records); }
    @Override public void deleteStore(String name) { stores.remove(name); }
    @Override public void tone(int note, int ms, int volume) { tones++; }
    @Override public String appProperty(String key) { return props.get(key); }
    @Override public int pollKey() { return -1; }
}
