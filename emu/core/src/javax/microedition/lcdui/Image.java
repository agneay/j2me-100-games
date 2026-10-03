package javax.microedition.lcdui;

import java.io.IOException;
import java.io.InputStream;

/** Emulator Image: an ARGB pixel array, mutable or immutable. */
public class Image {
    final int[] data;
    final int w, h;
    final boolean mutable;

    private Image(int w, int h, boolean mutable) {
        if (w <= 0 || h <= 0) throw new IllegalArgumentException();
        this.w = w;
        this.h = h;
        this.mutable = mutable;
        data = new int[w * h];
    }

    public static Image createImage(int width, int height) {
        Image img = new Image(width, height, true);
        for (int i = 0; i < img.data.length; i++) img.data[i] = 0xFFFFFFFF;
        return img;
    }

    public static Image createImage(Image source) {
        if (!source.mutable) return source;
        Image img = new Image(source.w, source.h, false);
        System.arraycopy(source.data, 0, img.data, 0, img.data.length);
        return img;
    }

    public static Image createImage(Image src, int x, int y, int width, int height, int transform) {
        if (x < 0 || y < 0 || x + width > src.w || y + height > src.h) throw new IllegalArgumentException();
        boolean swap = (transform & 4) != 0;
        Image img = new Image(swap ? height : width, swap ? width : height, false);
        Graphics.blit(src, x, y, width, height, transform, img.data, img.w, img.h, 0, 0, 0, 0, img.w, img.h, false);
        return img;
    }

    public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
        Image img = new Image(width, height, false);
        for (int i = 0; i < img.data.length; i++) {
            int c = rgb[i];
            img.data[i] = processAlpha ? c : (c | 0xFF000000);
        }
        return img;
    }

    public static Image createImage(String name) throws IOException {
        throw new IOException("resource images are not supported by the emulator: " + name);
    }

    public static Image createImage(InputStream stream) throws IOException {
        throw new IOException("stream images are not supported by the emulator");
    }

    public static Image createImage(byte[] data, int offset, int length) {
        throw new IllegalArgumentException("encoded images are not supported by the emulator");
    }

    public Graphics getGraphics() {
        if (!mutable) throw new IllegalStateException();
        return new Graphics(data, w, h);
    }

    public int getWidth() { return w; }
    public int getHeight() { return h; }
    public boolean isMutable() { return mutable; }

    public void getRGB(int[] rgb, int offset, int scanlength, int x, int y, int width, int height) {
        if (x < 0 || y < 0 || x + width > w || y + height > h) throw new IllegalArgumentException();
        for (int j = 0; j < height; j++) {
            System.arraycopy(data, (y + j) * w + x, rgb, offset + j * scanlength, width);
        }
    }
}
