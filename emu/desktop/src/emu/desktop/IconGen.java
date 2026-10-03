package emu.desktop;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Generates a MIDlet icon (PNG) from a game's name and category using the
 * emulator's own rasterizer and pixel font. Usage: IconGen "Name" Category out.png [size]
 */
public final class IconGen {
    private IconGen() {}

    static int categoryColor(String c) {
        switch (c.toLowerCase()) {
            case "arcade": return 0xE0457B;
            case "puzzle": return 0x4C8DFF;
            case "platformer": return 0xFF9F1C;
            case "racing": return 0xE63946;
            case "strategy": return 0x2A9D8F;
            case "board": return 0xA47551;
            case "card": return 0x43AA8B;
            case "rpg": return 0x9B5DE5;
            case "sports": return 0x52B788;
            case "simulation": return 0xF4A261;
            default: return 0x00BBF9;
        }
    }

    static String initials(String name) {
        StringBuilder sb = new StringBuilder();
        for (String p : name.split("[ \\-]+")) {
            if (!p.isEmpty() && Character.isLetterOrDigit(p.charAt(0))) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() == 2) break;
        }
        if (sb.length() == 1 && name.length() > 1) sb.append(Character.toLowerCase(name.charAt(1)));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        String name = args[0], cat = args[1];
        File out = new File(args[2]);
        int size = args.length > 3 ? Integer.parseInt(args[3]) : 24;
        int base = categoryColor(cat);
        Image img = Image.createImage(size, size);
        Graphics g = img.getGraphics();
        g.setColor(0x0B1220);
        g.fillRect(0, 0, size, size);
        for (int y = 1; y < size - 1; y++) {
            int t = y * 100 / size;
            int r = ((base >> 16) & 0xFF) * (130 - t) / 130, gg = ((base >> 8) & 0xFF) * (130 - t) / 130, b = (base & 0xFF) * (130 - t) / 130;
            g.setColor((r << 16) | (gg << 8) | b);
            g.fillRect(1, y, size - 2, 1);
        }
        g.setColor(0xFFFFFF);
        g.drawRect(0, 0, size - 1, size - 1);
        g.setColor(0x0B1220);
        g.fillRect(0, 0, 1, 1);
        g.fillRect(size - 1, 0, 1, 1);
        g.fillRect(0, size - 1, 1, 1);
        g.fillRect(size - 1, size - 1, 1, 1);
        Font f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, size >= 40 ? Font.SIZE_LARGE : Font.SIZE_SMALL);
        String s = initials(name);
        g.setFont(f);
        int ty = (size - f.getBaselinePosition()) / 2;
        g.setColor(0x000000);
        g.drawString(s, size / 2 + 1, ty + 1, Graphics.TOP | Graphics.HCENTER);
        g.setColor(0xFFFFFF);
        g.drawString(s, size / 2, ty, Graphics.TOP | Graphics.HCENTER);
        int[] px = new int[size * size];
        img.getRGB(px, 0, size, 0, 0, size, size);
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, size, size, px, 0, size);
        if (out.getParentFile() != null) out.getParentFile().mkdirs();
        ImageIO.write(bi, "png", out);
    }
}
