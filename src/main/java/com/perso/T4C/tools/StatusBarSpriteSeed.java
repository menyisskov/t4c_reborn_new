package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Renders the HP / mana / XP status bars procedurally instead of downscaling a bitmap sheet.
 *
 * <p>The legacy {@code GUI_BackChStat_*} sprites were authored at HUD size, so any resize showed
 * hard stair-stepped edges. These are drawn as antialiased vector shapes at {@link #SUPERSAMPLE}
 * times the HUD size, which keeps the rounded caps and the gradient smooth at every scale.
 */
public final class StatusBarSpriteSeed {
    /** HUD size of the HP and mana bars, in logical pixels. */
    private static final int BAR_WIDTH = 134;
    private static final int BAR_HEIGHT = 18;
    /** HUD size of the experience bar, which is much wider and flatter. */
    private static final int XP_WIDTH = 894;
    private static final int XP_HEIGHT = 14;
    /** Rendering factor: the sprite is stored this many times larger than its HUD size. */
    private static final int SUPERSAMPLE = 4;

    /** Health runs deep red to yellow-green, matching the original T4C gradient. */
    private static final Color[] HEALTH = {
            new Color(0x8E1410), new Color(0xD8321C), new Color(0xE8A317),
            new Color(0xC6D42A), new Color(0x53C22B)};
    private static final Color[] MANA = {
            new Color(0x0B2C6B), new Color(0x1B62C4), new Color(0x37A6F0), new Color(0x9AD9FF)};
    private static final Color[] EXPERIENCE = {
            new Color(0x5B2E86), new Color(0x9B4FD0), new Color(0xD7A2FF)};

    private static final Color TRACK_TOP = new Color(0x14, 0x11, 0x0D, 255);
    private static final Color TRACK_BOTTOM = new Color(0x3A, 0x33, 0x28, 255);
    private static final Color TRACK_BORDER = new Color(0x08, 0x07, 0x05, 220);

    private StatusBarSpriteSeed() {}

    public static void main(String[] args) throws Exception {
        Path spriteDir = Path.of("assets/sprites");
        Path pngDir = spriteDir.resolve("png");
        Files.createDirectories(pngDir);

        BufferedImage empty = renderTrack(BAR_WIDTH, BAR_HEIGHT);
        BufferedImage health = renderFill(BAR_WIDTH, BAR_HEIGHT, HEALTH);
        BufferedImage mana = renderFill(BAR_WIDTH, BAR_HEIGHT, MANA);
        BufferedImage experience = renderFill(XP_WIDTH, XP_HEIGHT, EXPERIENCE);

        List<SpriteBinWriter.Entry> entries = List.of(
                entry("GUI_BackChStat_Empty", empty),
                entry("GUI_BackChStat_HP", health),
                entry("GUI_BackChStat_MP", mana),
                entry("GUI_BackChStat_XP", experience));

        for (SpriteBinWriter.Entry e : entries) {
            ImageIO.write(e.image(), "png", pngDir.resolve(e.name() + ".png").toFile());
        }
        SpriteBinWriter.merge(spriteDir.resolve("sprites.bin"), entries);
        System.out.println("Seeded " + entries.size() + " smooth status bar sprites");
    }

    /** The empty groove drawn behind a bar: dark inset with a soft top-to-bottom lift. */
    private static BufferedImage renderTrack(int width, int height) {
        int w = width * SUPERSAMPLE;
        int h = height * SUPERSAMPLE;
        BufferedImage image = newImage(w, h);
        Graphics2D g = graphics(image);
        g.setPaint(TRACK_BORDER);
        g.fill(new Rectangle2D.Float(0, 0, w, h));

        float inset = SUPERSAMPLE;
        g.setPaint(new GradientPaint(0, inset, TRACK_TOP, 0, h - inset, TRACK_BOTTOM));
        g.fill(new Rectangle2D.Float(inset, inset, w - 2 * inset, h - 2 * inset));
        g.dispose();
        return image;
    }

    /**
     * A fill bar. It is drawn as a full-width sprite; the HUD reveals a left-hand slice of it,
     * so the gradient must run along the whole width rather than being scaled per frame.
     */
    private static BufferedImage renderFill(int width, int height, Color[] ramp) {
        int w = width * SUPERSAMPLE;
        int h = height * SUPERSAMPLE;
        BufferedImage image = newImage(w, h);
        Graphics2D g = graphics(image);
        float inset = SUPERSAMPLE;
        Rectangle2D body = new Rectangle2D.Float(inset, inset, w - 2 * inset, h - 2 * inset);

        g.setPaint(new LinearGradientPaint(new Point2D.Float(inset, 0),
                new Point2D.Float(w - inset, 0), fractions(ramp.length), ramp,
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fill(body);

        // Vertical shading: a highlight across the upper third and a shadow at the bottom edge,
        // which is what reads as "glossy" rather than flat.
        g.setPaint(new LinearGradientPaint(new Point2D.Float(0, inset),
                new Point2D.Float(0, h - inset),
                new float[]{0f, 0.35f, 0.55f, 1f},
                new Color[]{new Color(255, 255, 255, 110), new Color(255, 255, 255, 40),
                        new Color(0, 0, 0, 0), new Color(0, 0, 0, 90)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fill(body);
        g.dispose();
        return image;
    }

    /** Evenly spaced gradient stops for a ramp of {@code count} colours. */
    private static float[] fractions(int count) {
        float[] fractions = new float[count];
        for (int i = 0; i < count; i++) {
            fractions[i] = i / (float) (count - 1);
        }
        return fractions;
    }

    private static BufferedImage newImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    private static Graphics2D graphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        return g;
    }

    private static SpriteBinWriter.Entry entry(String name, BufferedImage image) {
        return new SpriteBinWriter.Entry(name, image.getWidth(), image.getHeight(), 0, 0,
                image.getWidth(), image.getHeight(), image);
    }
}
