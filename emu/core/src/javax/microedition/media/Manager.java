package javax.microedition.media;

import javax.microedition.lcdui.EmuHost;

/** Emulator implementation of the MIDP 2.0 tone API (players are not emulated). */
public final class Manager {
    public static final String TONE_DEVICE_LOCATOR = "device://tone";

    private Manager() {}

    public static void playTone(int note, int duration, int volume) throws MediaException {
        if (note < 0 || note > 127 || duration <= 0) throw new IllegalArgumentException();
        if (EmuHost.backend != null) EmuHost.backend.tone(note, duration, volume);
    }

    public static String[] getSupportedContentTypes(String protocol) { return new String[0]; }
    public static String[] getSupportedProtocols(String contentType) { return new String[0]; }
}
