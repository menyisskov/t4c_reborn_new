package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinWriter;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Builds the compact HUD bars from the layered transparent sprite sheet and packs them. */
public final class HudBarSpriteSeed {
    private static final int WIDTH = 106;
    private static final int HEIGHT = 11;

    private HudBarSpriteSeed() { }

    public static void main(String[] args) throws Exception {
        BufferedImage sheet = ImageIO.read(Path.of(
                "assets/sprites/png/t4c-status-bars-hd-spritesheet-v3-flat.png").toFile());
        if (sheet == null) throw new IllegalArgumentException("Invalid HUD sprite sheet");

        BufferedImage frame = crop(sheet, 20, 20, 560, 56);
        BufferedImage background = crop(sheet, 684, 31, 432, 34);
        BufferedImage scaledFrame = scale(frame);
        BufferedImage scaledBackground = scale(background);
        BufferedImage hp = scale(crop(sheet, 1284, 31, 432, 34));
        BufferedImage mp = scale(crop(sheet, 1284, 127, 432, 34));
        BufferedImage xp = scale(crop(sheet, 1284, 223, 432, 34));

        Path pngDir = Path.of("assets/sprites/png");
        Files.createDirectories(pngDir);
        write(pngDir, "64kHudBarFrameV4", scaledFrame);
        write(pngDir, "64kHudBarBackgroundV4", scaledBackground);
        write(pngDir, "64kHudBarHealthV4Flat", hp);
        write(pngDir, "64kHudBarManaV4Flat", mp);
        write(pngDir, "64kHudBarExperienceV4Flat", xp);

        List<SpriteBinWriter.Entry> entries = List.of(
                entry("64kHudBarFrameV4", scaledFrame), entry("64kHudBarBackgroundV4", scaledBackground),
                entry("64kHudBarHealthV4Flat", hp),
                entry("64kHudBarManaV4Flat", mp), entry("64kHudBarExperienceV4Flat", xp));
        SpriteBinWriter.merge(Path.of("assets/sprites/sprites.bin"), entries);
        System.out.println("Seeded " + entries.size() + " flat-color HUD bar sprites");
    }

    private static BufferedImage crop(BufferedImage source, int x, int y, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                result.setRGB(px, py, source.getRGB(x + px, y + py));
            }
        }
        return result;
    }

    private static BufferedImage scale(BufferedImage source) {
        BufferedImage result = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(source, 0, 0, WIDTH, HEIGHT, null);
        graphics.dispose();
        return result;
    }

    private static void write(Path directory, String name, BufferedImage image) throws Exception {
        ImageIO.write(image, "png", directory.resolve(name + ".png").toFile());
    }

    private static SpriteBinWriter.Entry entry(String name, BufferedImage image) {
        return new SpriteBinWriter.Entry(name, image.getWidth(), image.getHeight(), 0, 0,
                image.getWidth(), image.getHeight(), image);
    }
}
