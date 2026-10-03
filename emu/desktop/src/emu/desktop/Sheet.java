package emu.desktop;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Contact sheet for reviewing screenshots: Sheet out.png columns img1.png img2.png ... */
public final class Sheet {
    private Sheet() {}

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File out = new File(args[0]);
        int cols = Integer.parseInt(args[1]);
        int n = args.length - 2;
        BufferedImage[] imgs = new BufferedImage[n];
        int cw = 0, ch = 0;
        for (int i = 0; i < n; i++) {
            imgs[i] = ImageIO.read(new File(args[i + 2]));
            if (imgs[i] == null) continue;
            cw = Math.max(cw, imgs[i].getWidth());
            ch = Math.max(ch, imgs[i].getHeight());
        }
        int rows = (n + cols - 1) / cols, pad = 4;
        BufferedImage sheet = new BufferedImage(cols * (cw + pad) + pad, rows * (ch + pad) + pad, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x303030));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        for (int i = 0; i < n; i++) {
            if (imgs[i] == null) continue;
            g.drawImage(imgs[i], pad + (i % cols) * (cw + pad), pad + (i / cols) * (ch + pad), null);
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
    }
}
