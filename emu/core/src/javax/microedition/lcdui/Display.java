package javax.microedition.lcdui;

import javax.microedition.midlet.MIDlet;

/** Emulator implementation of MIDP Display (single display). */
public class Display {
    public static final int LIST_ELEMENT = 1, CHOICE_GROUP_ELEMENT = 2, ALERT = 3;
    public static final int COLOR_BACKGROUND = 0, COLOR_FOREGROUND = 1, COLOR_HIGHLIGHTED_BACKGROUND = 2,
            COLOR_HIGHLIGHTED_FOREGROUND = 3, COLOR_BORDER = 4, COLOR_HIGHLIGHTED_BORDER = 5;

    private static Display instance;

    private Display() {}

    public static Display getDisplay(MIDlet m) {
        if (instance == null) instance = new Display();
        return instance;
    }

    public Displayable getCurrent() { return EmuHost.current; }

    public void setCurrent(Displayable next) {
        synchronized (EmuHost.LOCK) {
            Displayable old = EmuHost.current;
            if (old == next) return;
            if (old != null) old.emuHide();
            EmuHost.current = next;
            if (next != null) next.emuShow();
            EmuHost.dirty = true;
        }
    }

    public boolean isColor() { return true; }
    public int numColors() { return 65536; }
    public int numAlphaLevels() { return 256; }
    public boolean vibrate(int ms) { return true; }
    public boolean flashBacklight(int ms) { return true; }
    public int getColor(int which) { return which == COLOR_BACKGROUND ? 0xFFFFFF : 0; }
    public void callSerially(Runnable r) { r.run(); }
}
