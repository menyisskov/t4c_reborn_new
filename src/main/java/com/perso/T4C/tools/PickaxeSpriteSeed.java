package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.SpriteBinWriter;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Builds the Pickaxe overlay set from AxeDestruction's animation geometry. */
public final class PickaxeSpriteSeed {
    private static final double SCALE = 0.5d;
    private static final Pattern SOURCE = Pattern.compile(
            "AxeDestruction(?:A)?(?:000|045|090|135|180)-[a-m]|Ground_AxeDestruction|Inv_AxeDestruction");
    private static final Color OUTLINE = new Color(28, 29, 35, 255);
    private static final Color HANDLE = new Color(67, 68, 76, 255);
    private static final Color HANDLE_LIGHT = new Color(151, 151, 157, 255);
    private static final Color STEEL_DARK = new Color(91, 93, 101, 255);
    private static final Color STEEL = new Color(190, 191, 196, 255);
    private static final Color STEEL_LIGHT = new Color(245, 245, 243, 255);

    private PickaxeSpriteSeed() {}

    public static void main(String[] args) throws Exception {
        Path spriteDir = Path.of("assets/sprites");
        Path pngDir = spriteDir.resolve("png");
        Files.createDirectories(pngDir);

        List<SpriteBinWriter.Entry> entries = new ArrayList<>();
        SpriteBinIO.readAll(spriteDir, "sprites", packed -> {
            if (!SOURCE.matcher(packed.name()).matches()) return;
            try {
                BufferedImage source = ImageIO.read(new ByteArrayInputStream(packed.png()));
                BufferedImage pickaxe = renderPickaxe(source);
                String name = targetName(packed.name());
                ImageIO.write(pickaxe, "png", pngDir.resolve(name + ".png").toFile());
                entries.add(new SpriteBinWriter.Entry(name, packed.width(), packed.height(),
                        packed.off1X(), packed.off1Y(), packed.off2X(), packed.off2Y(), pickaxe));
            } catch (Exception e) {
                throw new IllegalStateException("Cannot generate pickaxe from " + packed.name(), e);
            }
        });

        if (entries.size() != 112) {
            throw new IllegalStateException("Expected 112 AxeDestruction references, found " + entries.size());
        }
        SpriteBinWriter.merge(spriteDir.resolve("sprites.bin"), entries);
        System.out.println("Seeded " + entries.size() + " Pickaxe sprites");
    }

    private static String targetName(String source) {
        if (source.equals("Ground_AxeDestruction")) return "Ground_Pickaxe";
        if (source.equals("Inv_AxeDestruction")) return "Inv_Pickaxe";
        return source.replaceFirst("AxeDestruction", "Pickaxe");
    }

    private static BufferedImage renderPickaxe(BufferedImage source) {
        Geometry geometry = geometry(source);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        // The handle endpoint is the character's grip. Keep it fixed while shortening the
        // tool, otherwise the whole overlay drifts away from the hand.
        double handleX = geometry.handleX;
        double handleY = geometry.handleY;
        double headX = handleX + (geometry.headX - handleX) * SCALE;
        double headY = handleY + (geometry.headY - handleY) * SCALE;
        double span = geometry.span * SCALE;
        float shaftOuter = (float) Math.max(1, Math.min(3, span / 24));
        drawLine(g, OUTLINE, shaftOuter + 1, handleX, handleY, headX, headY);
        drawLine(g, HANDLE, shaftOuter, handleX, handleY, headX, headY);
        drawLine(g, HANDLE_LIGHT, 1, handleX + geometry.px, handleY + geometry.py,
                headX + geometry.px, headY + geometry.py);

        double pointLength = Math.max(5, Math.min(25, span * 0.31));
        double pollLength = Math.max(3, Math.min(12, span * 0.14));
        double curve = Math.max(1, pointLength * 0.18);
        double pointX = headX + geometry.px * pointLength - geometry.ux * curve;
        double pointY = headY + geometry.py * pointLength - geometry.uy * curve;
        double pollX = headX - geometry.px * pollLength;
        double pollY = headY - geometry.py * pollLength;
        float headOuter = (float) Math.max(2, Math.min(5, span / 15));

        drawLine(g, OUTLINE, headOuter + 2, pointX, pointY, headX, headY);
        drawLine(g, OUTLINE, headOuter + 2, headX, headY, pollX, pollY);
        drawLine(g, STEEL_DARK, headOuter, pointX, pointY, headX, headY);
        drawLine(g, STEEL, headOuter, headX, headY, pollX, pollY);
        drawLine(g, STEEL_LIGHT, 1, pointX, pointY, headX, headY);
        drawLine(g, STEEL_LIGHT, 1, headX, headY, pollX, pollY);
        g.dispose();
        return result;
    }

    private static void drawLine(Graphics2D g, Color color, float width,
                                 double x1, double y1, double x2, double y2) {
        g.setColor(color);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.drawLine((int) Math.round(x1), (int) Math.round(y1), (int) Math.round(x2), (int) Math.round(y2));
    }

    private static Geometry geometry(BufferedImage image) {
        List<double[]> pixels = new ArrayList<>();
        double cx = 0, cy = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) < 32) continue;
                pixels.add(new double[]{x, y});
                cx += x; cy += y;
            }
        }
        if (pixels.isEmpty()) throw new IllegalArgumentException("Empty reference sprite");
        cx /= pixels.size(); cy /= pixels.size();
        double xx = 0, xy = 0, yy = 0;
        for (double[] p : pixels) {
            double dx = p[0] - cx, dy = p[1] - cy;
            xx += dx * dx; xy += dx * dy; yy += dy * dy;
        }
        double angle = 0.5 * Math.atan2(2 * xy, xx - yy);
        double ux = Math.cos(angle), uy = Math.sin(angle);
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (double[] p : pixels) {
            double t = (p[0] - cx) * ux + (p[1] - cy) * uy;
            min = Math.min(min, t); max = Math.max(max, t);
        }
        double edge = Math.max(2, (max - min) * 0.22);
        int minCount = 0, maxCount = 0;
        for (double[] p : pixels) {
            double t = (p[0] - cx) * ux + (p[1] - cy) * uy;
            if (t < min + edge) minCount++;
            if (t > max - edge) maxCount++;
        }
        // The broad axe blade identifies the tool-head endpoint.
        if (minCount > maxCount) { ux = -ux; uy = -uy; double oldMin = min; min = -max; max = -oldMin; }
        double span = max - min;
        double handleX = cx + ux * min, handleY = cy + uy * min;
        double headX = 0, headY = 0, weight = 0;
        for (double[] p : pixels) {
            double t = (p[0] - cx) * ux + (p[1] - cy) * uy;
            if (t > max - Math.max(2, span * 0.18)) {
                headX += p[0]; headY += p[1]; weight++;
            }
        }
        if (weight == 0) { headX = cx + ux * max; headY = cy + uy * max; }
        else { headX /= weight; headY /= weight; }
        return new Geometry(handleX, handleY, headX, headY, ux, uy, -uy, ux, span);
    }

    private record Geometry(double handleX, double handleY, double headX, double headY,
                            double ux, double uy, double px, double py, double span) {}
}
