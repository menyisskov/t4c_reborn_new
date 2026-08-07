package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinWriter;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Draws the level-up effect and merges its frames into {@code sprites.bin}.
 *
 * <p>The original library ships no elevation effect, so the frames are generated rather than
 * extracted: a golden column rising from the ground, a halo blooming around the character and
 * motes of light converging upward. It follows the conventions {@code SpellRenderer} relies on —
 * frames named {@code <effect>-<letter>}, drawn from the sprite's top-left corner with
 * {@code drawOffset1} placing them relative to the character's feet.
 *
 * <p>Run once to (re)generate the asset; the game itself only reads the merged binary.
 */
public final class LevelUpSpriteSeed {

    /** Must match {@code SpellRenderer.LEVEL_UP_EFFECT} without its trailing dash. */
    private static final String EFFECT_NAME = "LevelUpClaude";
    private static final List<String> REPLACED_EFFECT_PREFIXES = List.of(
            "levelupcodex-", "levelupclaude-", "spelllevelup-");
    private static final int FRAME_COUNT = 18;
    private static final int WIDTH = 96;
    private static final int HEIGHT = 128;

    /** Vertical travel of the effect, from the feet upward. Kept clear of the top edge so the
     * beam and the highest motes fade out rather than being clipped by the frame. */
    private static final int RISE = 100;

    /**
     * Horizontal shift applied to the draw offset, on top of centring the sprite.
     *
     * <p>The character's anchor is not the middle of an effect sprite: measuring {@code
     * HealingSpell-}'s large frames, every one sits about 14 px to the right of its geometric
     * centre. Centring on {@code -WIDTH / 2} alone therefore draws the effect too far left.
     */
    private static final int ANCHOR_X = 17;

    /**
     * Vertical shift applied to the draw offset. Positive values push the effect down the screen,
     * negative values lift it up.
     *
     * <p>Deliberately independent of {@link #GROUND_Y}: the drawing baseline is fixed inside the
     * frame, so this constant only moves the finished sprite. Deriving both from one value would
     * make them cancel out and leave the effect motionless.
     */
    private static final int ANCHOR_Y = 12;

    /** Baseline the effect is drawn from, in frame coordinates: the character's feet. */
    private static final float GROUND_Y = HEIGHT - 8f;

    private static final Color CORE = new Color(255, 252, 214);
    private static final Color GOLD = new Color(255, 206, 92);
    private static final Color DEEP = new Color(214, 138, 20);

    private LevelUpSpriteSeed() {
    }

    public static void main(String[] args) throws Exception {
        List<SpriteBinWriter.Entry> entries = new ArrayList<>();
        for (int i = 0; i < FRAME_COUNT; i++) {
            BufferedImage frame = renderFrame(i / (float) (FRAME_COUNT - 1));
            // Centred on the character and anchored at its feet, like the original spell effects.
            int offsetX = -WIDTH / 2 + ANCHOR_X;
            // Places the drawing baseline on the character's feet, then applies the manual shift.
            int offsetY = -Math.round(GROUND_Y) + ANCHOR_Y;
            entries.add(new SpriteBinWriter.Entry(frameName(i), WIDTH, HEIGHT,
                    offsetX, offsetY, offsetX, offsetY, frame));
        }
        SpriteBinWriter.replaceMatching(Path.of("assets/sprites/sprites.bin"), entries,
                LevelUpSpriteSeed::belongsToReplacedEffect);
        System.out.println("Seeded " + entries.size() + " level-up sprites");
    }

    private static boolean belongsToReplacedEffect(String spriteName) {
        if (spriteName == null) return false;
        String lower = spriteName.toLowerCase(Locale.ROOT);
        return REPLACED_EFFECT_PREFIXES.stream().anyMatch(lower::startsWith);
    }

    /** {@code LevelUpClaude-a}, {@code LevelUpClaude-b}, … as parsed by {@code SpellRenderer}. */
    private static String frameName(int index) {
        return EFFECT_NAME + "-" + (char) ('a' + index);
    }

    /**
     * Renders one frame of the animation.
     *
     * @param t progress in {@code [0,1]}, from the first flash to the last fading mote
     */
    private static BufferedImage renderFrame(float t) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // The effect swells quickly then fades out over the tail of the animation.
        float fade = t < 0.75f ? 1f : 1f - (t - 0.75f) / 0.25f;

        drawGroundHalo(g, t, fade);
        drawColumn(g, t, fade);
        drawMotes(g, t, fade);
        drawBurst(g, t, fade);

        g.dispose();
        return image;
    }

    /** Expanding ring of light on the ground, brightest as the column takes off. */
    private static void drawGroundHalo(Graphics2D g, float t, float fade) {
        float grow = Math.min(1f, t / 0.5f);
        float radius = 8f + grow * 34f;
        float alpha = fade * (1f - grow * 0.55f);
        if (alpha <= 0f) return;

        float cx = WIDTH / 2f;
        float cy = GROUND_Y;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha)));
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(GOLD);
        // Flattened to sit on the isometric ground plane rather than facing the camera.
        g.draw(new Ellipse2D.Float(cx - radius, cy - radius / 2.6f, radius * 2, radius / 1.3f));
        g.setColor(CORE);
        g.setStroke(new BasicStroke(1f));
        g.draw(new Ellipse2D.Float(cx - radius * 0.7f, cy - radius / 3.7f,
                radius * 1.4f, radius / 1.85f));
    }

    /** The column of light itself, rising and tapering as it climbs. */
    private static void drawColumn(Graphics2D g, float t, float fade) {
        float rise = ease(Math.min(1f, t / 0.7f));
        float top = GROUND_Y - rise * RISE;
        float bottom = GROUND_Y;
        if (bottom - top < 1f) return;

        float cx = WIDTH / 2f;
        // Widest at mid-animation, then drawn in as the light gathers upward.
        float halfWidth = 13f * (float) Math.sin(Math.min(1f, t / 0.85f) * Math.PI) + 3f;

        // Filled as a tapered body rather than stacked lines: a beam, with no banding.
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(fade * 0.55f)));
        g.setPaint(new LinearGradientPaint(new Point2D.Float(0, bottom), new Point2D.Float(0, top),
                new float[] {0f, 0.55f, 1f},
                new Color[] {GOLD, DEEP, new Color(DEEP.getRed(), DEEP.getGreen(), DEEP.getBlue(), 0)}));
        float topHalf = halfWidth * 0.35f;
        Path2D.Float body = new Path2D.Float();
        body.moveTo(cx - halfWidth, bottom);
        body.lineTo(cx + halfWidth, bottom);
        body.lineTo(cx + topHalf, top);
        body.lineTo(cx - topHalf, top);
        body.closePath();
        g.fill(body);
        g.setPaint(null);

        // Bright core running up the middle of the beam.
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(fade * 0.85f)));
        g.setColor(CORE);
        g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Float(cx, bottom, cx, top));
    }

    /**
     * Motes of light spiralling up around the column. The seed is fixed so the animation is
     * reproducible: regenerating the asset must not reshuffle the sparks.
     */
    private static void drawMotes(Graphics2D g, float t, float fade) {
        Random random = new Random(20240607L);
        float cx = WIDTH / 2f;
        float bottom = GROUND_Y;

        for (int i = 0; i < 22; i++) {
            float phase = random.nextFloat();
            float radius = 10f + random.nextFloat() * 26f;
            float speed = 0.75f + random.nextFloat() * 0.5f;
            float turns = 1.4f + random.nextFloat();

            // Each mote starts at its own moment and rides the column to the top.
            float local = (t - phase * 0.35f) * speed;
            if (local <= 0f || local >= 1f) continue;

            float angle = (float) (local * turns * 2 * Math.PI + phase * 2 * Math.PI);
            // The spiral narrows as the mote climbs, converging over the character.
            float spread = radius * (1f - local * 0.7f);
            float x = cx + (float) Math.cos(angle) * spread;
            float y = bottom - local * RISE;
            float size = 1.5f + (1f - local) * 2.2f;
            float alpha = fade * (1f - local) * 0.95f;
            if (alpha <= 0f) continue;

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha)));
            g.setColor(i % 3 == 0 ? CORE : GOLD);
            g.fill(new Ellipse2D.Float(x - size, y - size, size * 2, size * 2));
        }
    }

    /** Radial flash over the character, peaking once the column is fully grown. */
    private static void drawBurst(Graphics2D g, float t, float fade) {
        if (t < 0.45f) return;
        float local = (t - 0.45f) / 0.55f;
        float alpha = fade * (float) Math.sin(local * Math.PI) * 0.8f;
        if (alpha <= 0.01f) return;

        float radius = 16f + local * 26f;
        float cx = WIDTH / 2f;
        float cy = GROUND_Y - RISE * 0.55f;

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha)));
        g.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), radius,
                new float[] {0f, 0.45f, 1f},
                new Color[] {CORE, GOLD, new Color(DEEP.getRed(), DEEP.getGreen(), DEEP.getBlue(), 0)}));
        g.fill(new Ellipse2D.Float(cx - radius, cy - radius, radius * 2, radius * 2));
        g.setPaint(null);
    }

    /** Smooth start and end, so the rise does not begin or stop abruptly. */
    private static float ease(float t) {
        return t * t * (3f - 2f * t);
    }

    private static float clamp(float alpha) {
        return Math.max(0f, Math.min(1f, alpha));
    }
}
