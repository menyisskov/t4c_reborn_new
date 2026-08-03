package com.perso.T4C.tools;

import com.perso.T4C.harvest.HerbDefinition;
import com.perso.T4C.harvest.HerbRegistry;
import com.perso.T4C.helper.SpriteBinWriter;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Crops/scales the generated herb art and merges world + inventory sprites. */
public final class HerbSpriteSeed {
    private HerbSpriteSeed() {}

    public static void main(String[] args) throws Exception {
        Path sources = Path.of("assets/sprites/herbs");
        Path pngDir = Path.of("assets/sprites/png");
        Files.createDirectories(pngDir);
        List<SpriteBinWriter.Entry> entries = new ArrayList<>();
        for (HerbDefinition definition : HerbRegistry.load()) {
            ItemDefinition item = ItemRegistry.findByKey(definition.getItemKey());
            if (item == null || item.getAppearanceInventory() == null || item.getAppearanceInventory().isBlank()) {
                throw new IllegalArgumentException("Missing inventory item for herb: " + definition.getId());
            }
            String fileName = definition.getId() + ".png";
            BufferedImage source = ImageIO.read(sources.resolve(fileName).toFile());
            if (source == null) throw new IllegalArgumentException("Invalid herb PNG: " + fileName);
            BufferedImage cropped = cropTransparent(source);
            BufferedImage world = scaleToFit(cropped, 72, 72);
            BufferedImage inventory = scaleToFit(cropped, 30, 30);
            Path worldPng = pngDir.resolve(definition.getWorldSprite() + ".png");
            Path inventoryPng = pngDir.resolve(item.getAppearanceInventory() + ".png");
            ImageIO.write(world, "png", worldPng.toFile());
            ImageIO.write(inventory, "png", inventoryPng.toFile());
            entries.add(entry(definition.getWorldSprite(), world));
            entries.add(entry(item.getAppearanceInventory(), inventory));
        }
        SpriteBinWriter.merge(Path.of("assets/sprites/sprites.bin"), entries);
        System.out.println("Seeded " + entries.size() + " herb sprites");
    }

    private static SpriteBinWriter.Entry entry(String name, BufferedImage image) {
        return new SpriteBinWriter.Entry(name, image.getWidth(), image.getHeight(), 0, 0,
                image.getWidth(), image.getHeight(), image);
    }

    private static BufferedImage cropTransparent(BufferedImage source) {
        int minX = source.getWidth(), minY = source.getHeight(), maxX = -1, maxY = -1;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                if (((source.getRGB(x, y) >>> 24) & 0xFF) < 8) continue;
                minX = Math.min(minX, x); minY = Math.min(minY, y);
                maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) throw new IllegalArgumentException("Herb image is fully transparent");
        int padding = 3;
        minX = Math.max(0, minX - padding); minY = Math.max(0, minY - padding);
        maxX = Math.min(source.getWidth() - 1, maxX + padding);
        maxY = Math.min(source.getHeight() - 1, maxY + padding);
        return source.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private static BufferedImage scaleToFit(BufferedImage source, int maxWidth, int maxHeight) {
        double scale = Math.min((double) maxWidth / source.getWidth(), (double) maxHeight / source.getHeight());
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return result;
    }
}
