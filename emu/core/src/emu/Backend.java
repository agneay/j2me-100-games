package emu;

/** Host services the emulated MIDP runtime needs (desktop harness or browser). */
public interface Backend {
    /** Show a finished frame. pixels are 0xAARRGGBB, w*h long. */
    void present(int[] pixels, int w, int h);

    /** The MIDlet called notifyDestroyed(). */
    void exit();

    /** Load all records of a record store, or null if it does not exist. */
    byte[][] loadStore(String name);

    /** Persist all records of a record store (null entries = deleted ids). */
    void saveStore(String name, byte[][] records);

    /** Remove a record store. */
    void deleteStore(String name);

    /** Play a tone (MIDI note 0..127) for ms milliseconds at volume 0..100. */
    void tone(int note, int ms, int volume);

    /** JAD / manifest attribute lookup, may return null. */
    String appProperty(String key);

    /**
     * Poll one pending key event from the host, encoded as
     * ((keyCode + 1000) << 2) | type, or -1 when none is pending.
     */
    int pollKey();
}
