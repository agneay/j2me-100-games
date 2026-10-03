package javax.microedition.midlet;

import javax.microedition.lcdui.EmuHost;

/** Emulator implementation of the MIDlet lifecycle base class. */
public abstract class MIDlet {
    protected MIDlet() {}

    protected abstract void startApp() throws MIDletStateChangeException;
    protected abstract void pauseApp();
    protected abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;

    public final void notifyDestroyed() {
        if (EmuHost.backend != null) EmuHost.backend.exit();
    }

    public final void notifyPaused() {}
    public final void resumeRequest() {}

    public final String getAppProperty(String key) {
        return EmuHost.backend == null ? null : EmuHost.backend.appProperty(key);
    }

    public final boolean platformRequest(String url) { return false; }
    public final int checkPermission(String permission) { return 0; }

    /** Emulator entry point (not MIDP API): start the MIDlet. */
    public static void emuStart(MIDlet m) throws MIDletStateChangeException { m.startApp(); }

    /** Emulator entry point (not MIDP API): pause the MIDlet. */
    public static void emuPause(MIDlet m) { m.pauseApp(); }

    /** Emulator entry point (not MIDP API): destroy the MIDlet. */
    public static void emuDestroy(MIDlet m) throws MIDletStateChangeException { m.destroyApp(true); }
}
