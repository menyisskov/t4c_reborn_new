package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinWriter;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the looping aura shown under a meditating player: an arcane circle of runes
 * lying flat on the ground, wrapped in a soft mana-coloured glow.
 *
 * <p>The frames are the same drawing at evenly spaced rotations, so playing them in order
 * reads as one slowly turning circle. Because the ring is radially symmetric, the last
 * frame lands exactly one rune-step before the first and the loop has no visible seam:
 * frame {@code i} is rotated by {@code i / FRAME_COUNT} of the angle between two runes.
 *
 * <p>The circle is squashed vertically by {@link #ISOMETRIC_SQUASH} so it sits in the
 * game's 2:1 isometric ground plane rather than facing the camera.
 */
public final class MeditationSpriteSeed {

    /** Base name of the generated family; frames are suffixed {@code -a}, {@code -b}, … */
    private static final String SPRITE_BASE = "MeditationAura";
    /** Number of frames in the loop. */
    private static final int FRAME_COUNT = 16;
    /** Runes evenly spaced around the ring. */
    private static final int RUNE_COUNT = 12;
    /** On-screen size of one frame, in logical pixels. */
    private static final int FRAME_WIDTH = 96;
    private static final int FRAME_HEIGHT = 56;
    /** Rendering factor: drawn this many times larger, so the arcs stay smooth when scaled. */
    private static final int SUPERSAMPLE = 4;
    /** Vertical flattening that puts the circle in the isometric ground plane. */
    private static final float ISOMETRIC_SQUASH = 0.5f;

    // Mana-blue through arcane violet, matching the spell palette of the HUD's mana bar.
    // The glow is a faint haze: the runes and rings must stay the brightest thing on screen,
    // and the ground has to remain visible through the circle.
    private static final Color GLOW_CORE = new Color(0xB9, 0x8C, 0xFF, 46);
    private static final Color GLOW_EDGE = new Color(0x4A, 0x2E, 0xA8, 0);
    private static final Color RING_BRIGHT = new Color(0xD5, 0xC2, 0xFF, 235);
    private static final Color RING_DIM = new Color(0x7B, 0x5A, 0xE0, 170);
    private static final Color RUNE_COLOR = new Color(0xE6, 0xDC, 0xFF, 240);

    private MeditationSpriteSeed() {}

    public static void main(String[] args) throws Exception {
        Path spriteDir = Path.of("assets/sprites");
        Path pngDir = spriteDir.resolve("png");
        Files.createDirectories(pngDir);

        List<SpriteBinWriter.Entry> entries = new ArrayList<>();
        for (int frame = 0; frame < FRAME_COUNT; frame++) {
            // One full rune-step spread across the loop: the ring turns slowly and seamlessly.
            double angle = (2 * Math.PI / RUNE_COUNT) * frame / (double) FRAME_COUNT;
            String name = SPRITE_BASE + "-" + (char) ('a' + frame);
            entries.add(entry(name, renderFrame(angle)));
        }

        for (SpriteBinWriter.Entry e : entries) {
            ImageIO.write(e.image(), "png", pngDir.resolve(e.name() + ".png").toFile());
        }
        SpriteBinWriter.merge(spriteDir.resolve("sprites.bin"), entries);
        System.out.println("Seeded " + entries.size() + " meditation aura frames");
    }

    /** One frame of the aura, with the runic ring rotated by {@code angle} radians. */
    private static java.awt.image.BufferedImage renderFrame(double angle) {
        int w = FRAME_WIDTH * SUPERSAMPLE;
        int h = FRAME_HEIGHT * SUPERSAMPLE;
        java.awt.image.BufferedImage image =
                new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);

        float cx = w / 2f;
        float cy = h / 2f;
        float radius = w / 2f - 2f * SUPERSAMPLE;

        drawGlow(g, cx, cy, radius);

        // Everything below is drawn in the flattened ground plane.
        AffineTransform previous = g.getTransform();
        g.translate(cx, cy);
        g.scale(1d, ISOMETRIC_SQUASH);
        g.rotate(angle);

        drawRing(g, radius, 1f, RING_BRIGHT, 2f);
        drawRing(g, radius * 0.72f, 1f, RING_DIM, 1.5f);
        drawRing(g, radius * 0.30f, 1f, RING_DIM, 1f);
        drawRunes(g, radius * 0.86f);

        g.setTransform(previous);
        g.dispose();
        return image;
    }

    /** Soft radial haze that makes the circle read as light rather than paint. */
    private static void drawGlow(Graphics2D g, float cx, float cy, float radius) {
        float glowRadius = radius * 1.05f;
        g.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), glowRadius,
                new float[]{0f, 0.55f, 1f},
                new Color[]{GLOW_CORE, new Color(0x6A, 0x46, 0xC8, 30), GLOW_EDGE}));
        g.fill(new Ellipse2D.Float(cx - glowRadius, cy - glowRadius * ISOMETRIC_SQUASH,
                glowRadius * 2f, glowRadius * 2f * ISOMETRIC_SQUASH));
    }

    /** A single concentric circle of the arcane diagram. */
    private static void drawRing(Graphics2D g, float radius, float scale, Color color, float weight) {
        g.setPaint(color);
        g.setStroke(new BasicStroke(weight * SUPERSAMPLE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float r = radius * scale;
        g.draw(new Ellipse2D.Float(-r, -r, r * 2f, r * 2f));
    }

    /** Angular rune glyphs sitting on the ring, each turned to face outwards. */
    private static void drawRunes(Graphics2D g, float radius) {
        float runeSize = radius * 0.14f;
        g.setPaint(RUNE_COLOR);
        g.setStroke(new BasicStroke(1.2f * SUPERSAMPLE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < RUNE_COUNT; i++) {
            double theta = 2 * Math.PI * i / RUNE_COUNT;
            AffineTransform beforeRune = g.getTransform();
            g.translate(Math.cos(theta) * radius, Math.sin(theta) * radius);
            g.rotate(theta + Math.PI / 2);
            g.draw(runeGlyph(i, runeSize));
            g.setTransform(beforeRune);
        }
    }

    /**
     * A small angular glyph. The shape varies with the index so the ring reads as a line
     * of distinct symbols rather than one mark repeated twelve times.
     */
    private static Path2D runeGlyph(int index, float size) {
        Path2D.Float path = new Path2D.Float();
        float half = size / 2f;
        switch (index % 4) {
            case 0 -> { // vertical bar with a crossing stroke
                path.moveTo(0, -half);
                path.lineTo(0, half);
                path.moveTo(-half * 0.6f, -half * 0.2f);
                path.lineTo(half * 0.6f, -half * 0.2f);
            }
            case 1 -> { // arrowhead
                path.moveTo(-half * 0.6f, half * 0.5f);
                path.lineTo(0, -half * 0.6f);
                path.lineTo(half * 0.6f, half * 0.5f);
            }
            case 2 -> { // diamond
                path.moveTo(0, -half);
                path.lineTo(half * 0.55f, 0);
                path.lineTo(0, half);
                path.lineTo(-half * 0.55f, 0);
                path.closePath();
            }
            default -> { // forked stroke
                path.moveTo(0, half);
                path.lineTo(0, -half * 0.2f);
                path.moveTo(0, -half * 0.2f);
                path.lineTo(-half * 0.5f, -half);
                path.moveTo(0, -half * 0.2f);
                path.lineTo(half * 0.5f, -half);
            }
        }
        return path;
    }

    private static Graphics2D graphics(java.awt.image.BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        return g;
    }

    private static SpriteBinWriter.Entry entry(String name, java.awt.image.BufferedImage image) {
        return new SpriteBinWriter.Entry(name, image.getWidth(), image.getHeight(), 0, 0,
                image.getWidth(), image.getHeight(), image);
    }
}
