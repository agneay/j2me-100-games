package javax.microedition.lcdui;

import emu.PixelFont;

/**
 * Emulator Font backed by the project's 5x7 pixel font.
 * SMALL = 1x plain, MEDIUM = 1x with heavier strokes, LARGE = 2x.
 * Real handsets use their own fonts, so games must always measure text.
 */
public final class Font {
    public static final int FACE_SYSTEM = 0, FACE_MONOSPACE = 32, FACE_PROPORTIONAL = 64;
    public static final int STYLE_PLAIN = 0, STYLE_BOLD = 1, STYLE_ITALIC = 2, STYLE_UNDERLINED = 4;
    public static final int SIZE_SMALL = 8, SIZE_MEDIUM = 0, SIZE_LARGE = 16;
    public static final int FONT_STATIC_TEXT = 0, FONT_INPUT_TEXT = 1;

    private static final Font DEFAULT = new Font(FACE_SYSTEM, STYLE_PLAIN, SIZE_MEDIUM);

    private final int face, style, size;
    final int scale;   // pixel scale of glyphs
    final int heavy;   // extra horizontal smear for bold strokes
    final int advance; // per-character advance
    final int ascent, height;

    private Font(int face, int style, int size) {
        this.face = face;
        this.style = style;
        this.size = size;
        scale = size == SIZE_LARGE ? 2 : 1;
        boolean bold = (style & STYLE_BOLD) != 0 || size == SIZE_MEDIUM;
        heavy = bold ? 1 : 0;
        advance = (PixelFont.GW + 1) * scale + heavy;
        ascent = 7 * scale;
        height = (PixelFont.GH + 1) * scale + (size == SIZE_SMALL ? 0 : 1);
    }

    public static Font getDefaultFont() { return DEFAULT; }

    public static Font getFont(int specifier) {
        if (specifier != FONT_STATIC_TEXT && specifier != FONT_INPUT_TEXT) throw new IllegalArgumentException();
        return DEFAULT;
    }

    public static Font getFont(int face, int style, int size) {
        if (face != FACE_SYSTEM && face != FACE_MONOSPACE && face != FACE_PROPORTIONAL) throw new IllegalArgumentException();
        if ((style & ~7) != 0) throw new IllegalArgumentException();
        if (size != SIZE_SMALL && size != SIZE_MEDIUM && size != SIZE_LARGE) throw new IllegalArgumentException();
        return new Font(face, style, size);
    }

    public int getFace() { return face; }
    public int getStyle() { return style; }
    public int getSize() { return size; }
    public boolean isPlain() { return style == STYLE_PLAIN; }
    public boolean isBold() { return (style & STYLE_BOLD) != 0; }
    public boolean isItalic() { return (style & STYLE_ITALIC) != 0; }
    public boolean isUnderlined() { return (style & STYLE_UNDERLINED) != 0; }
    public int getHeight() { return height; }
    public int getBaselinePosition() { return ascent; }
    public int charWidth(char ch) { return advance; }
    public int charsWidth(char[] ch, int offset, int length) { return length * advance; }
    public int stringWidth(String str) { return str.length() * advance; }
    public int substringWidth(String str, int offset, int len) { return len * advance; }
}
