package emu.desktop;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/** Writes looping animated GIFs with the JDK's ImageIO GIF encoder. */
final class GifWriter {
    private GifWriter() {}

    static void write(List<int[]> frames, int w, int h, int delayMs, File out) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        out.delete();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            for (int i = 0; i < frames.size(); i++) {
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                img.setRGB(0, 0, w, h, frames.get(i), 0, w);
                ImageWriteParam param = writer.getDefaultWriteParam();
                IIOMetadata meta = writer.getDefaultImageMetadata(
                        ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_INDEXED), param);
                String fmt = meta.getNativeMetadataFormatName();
                IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(fmt);
                IIOMetadataNode gce = child(root, "GraphicControlExtension");
                gce.setAttribute("disposalMethod", "none");
                gce.setAttribute("userInputFlag", "FALSE");
                gce.setAttribute("transparentColorFlag", "FALSE");
                gce.setAttribute("delayTime", Integer.toString(delayMs / 10));
                gce.setAttribute("transparentColorIndex", "0");
                if (i == 0) {
                    IIOMetadataNode apps = child(root, "ApplicationExtensions");
                    IIOMetadataNode app = new IIOMetadataNode("ApplicationExtension");
                    app.setAttribute("applicationID", "NETSCAPE");
                    app.setAttribute("authenticationCode", "2.0");
                    app.setUserObject(new byte[] { 1, 0, 0 });
                    apps.appendChild(app);
                }
                meta.setFromTree(fmt, root);
                writer.writeToSequence(new IIOImage(img, null, meta), param);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    private static IIOMetadataNode child(IIOMetadataNode root, String name) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(name)) return (IIOMetadataNode) root.item(i);
        }
        IIOMetadataNode n = new IIOMetadataNode(name);
        root.appendChild(n);
        return n;
    }
}
