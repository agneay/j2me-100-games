package javax.microedition.lcdui;

/** Emulator implementation of MIDP Canvas. Paints into the shared screen. */
public abstract class Canvas extends Displayable {
    public static final int UP = 1, DOWN = 6, LEFT = 2, RIGHT = 5, FIRE = 8;
    public static final int GAME_A = 9, GAME_B = 10, GAME_C = 11, GAME_D = 12;
    public static final int KEY_NUM0 = 48, KEY_NUM1 = 49, KEY_NUM2 = 50, KEY_NUM3 = 51, KEY_NUM4 = 52,
            KEY_NUM5 = 53, KEY_NUM6 = 54, KEY_NUM7 = 55, KEY_NUM8 = 56, KEY_NUM9 = 57;
    public static final int KEY_STAR = 42, KEY_POUND = 35;

    /** Arrow / select / soft key codes used by the emulator (Nokia convention). */
    public static final int EMU_KEY_UP = -1, EMU_KEY_DOWN = -2, EMU_KEY_LEFT = -3, EMU_KEY_RIGHT = -4,
            EMU_KEY_SELECT = -5, EMU_SOFT_LEFT = -6, EMU_SOFT_RIGHT = -7;

    protected Canvas() {}

    public int getGameAction(int keyCode) {
        switch (keyCode) {
            case EMU_KEY_UP: case KEY_NUM2: return UP;
            case EMU_KEY_DOWN: case KEY_NUM8: return DOWN;
            case EMU_KEY_LEFT: case KEY_NUM4: return LEFT;
            case EMU_KEY_RIGHT: case KEY_NUM6: return RIGHT;
            case EMU_KEY_SELECT: case KEY_NUM5: return FIRE;
            case KEY_NUM1: return GAME_A;
            case KEY_NUM3: return GAME_B;
            case KEY_NUM7: return GAME_C;
            case KEY_NUM9: return GAME_D;
            default: return 0;
        }
    }

    public int getKeyCode(int gameAction) {
        switch (gameAction) {
            case UP: return EMU_KEY_UP;
            case DOWN: return EMU_KEY_DOWN;
            case LEFT: return EMU_KEY_LEFT;
            case RIGHT: return EMU_KEY_RIGHT;
            case FIRE: return EMU_KEY_SELECT;
            case GAME_A: return KEY_NUM1;
            case GAME_B: return KEY_NUM3;
            case GAME_C: return KEY_NUM7;
            case GAME_D: return KEY_NUM9;
            default: throw new IllegalArgumentException();
        }
    }

    public String getKeyName(int keyCode) {
        if (keyCode >= KEY_NUM0 && keyCode <= KEY_NUM9) return String.valueOf((char) keyCode);
        if (keyCode == KEY_STAR) return "*";
        if (keyCode == KEY_POUND) return "#";
        switch (keyCode) {
            case EMU_KEY_UP: return "Up";
            case EMU_KEY_DOWN: return "Down";
            case EMU_KEY_LEFT: return "Left";
            case EMU_KEY_RIGHT: return "Right";
            case EMU_KEY_SELECT: return "Select";
            case EMU_SOFT_LEFT: return "Soft1";
            case EMU_SOFT_RIGHT: return "Soft2";
            default: return "Key" + keyCode;
        }
    }

    public void setFullScreenMode(boolean mode) {}
    public boolean isDoubleBuffered() { return true; }
    public boolean hasPointerEvents() { return false; }
    public boolean hasPointerMotionEvents() { return false; }
    public boolean hasRepeatEvents() { return true; }

    public final void repaint() {
        EmuHost.dirty = true;
    }

    public final void repaint(int x, int y, int w, int h) {
        EmuHost.dirty = true;
    }

    public final void serviceRepaints() {
        if (EmuHost.current == this) EmuHost.service();
    }

    protected abstract void paint(Graphics g);

    protected void keyPressed(int keyCode) {}
    protected void keyReleased(int keyCode) {}
    protected void keyRepeated(int keyCode) {}
    protected void pointerPressed(int x, int y) {}
    protected void pointerReleased(int x, int y) {}
    protected void pointerDragged(int x, int y) {}
    protected void showNotify() {}
    protected void hideNotify() {}

    void emuShow() { showNotify(); }
    void emuHide() { hideNotify(); }
}
