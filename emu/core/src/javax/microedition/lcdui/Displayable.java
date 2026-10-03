package javax.microedition.lcdui;

/** Emulator implementation of the MIDP Displayable base class. */
public abstract class Displayable {
    private String title;
    CommandListener listener;

    Displayable() {}

    public String getTitle() { return title; }
    public void setTitle(String s) { title = s; }
    public void addCommand(Command c) {}
    public void removeCommand(Command c) {}
    public void setCommandListener(CommandListener l) { listener = l; }
    public boolean isShown() { return EmuHost.current == this; }
    public int getWidth() { return EmuHost.width; }
    public int getHeight() { return EmuHost.height; }
    protected void sizeChanged(int w, int h) {}

    void emuShow() {}
    void emuHide() {}
}
