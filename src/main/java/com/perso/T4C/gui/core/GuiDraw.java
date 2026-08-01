package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.config.GamePreferencesStore;

public final class GuiDraw {
    private GuiDraw() {
    }

    /** Dessine la région à sa taille native, y-flippée (atlas GUI en y-down). */
    public static void drawRegionFlipped(SpriteBatch batch, TextureRegion region, float x, float y) {
        drawRegionFlipped(batch, region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    /** Idem, avec une taille de destination explicite. */
    public static void drawRegionFlipped(SpriteBatch batch, TextureRegion region,
                                         float x, float y, float width, float height) {
        batch.draw(region.getTexture(), x, y, width, height,
                region.getRegionX(), region.getRegionY(),
                region.getRegionWidth(), region.getRegionHeight(), false, true);
    }

    /** Converts a percentage to a LibGDX alpha, safely clamped to [0, 1]. */
    public static float percentageToAlpha(float percentage) {
        if (!Float.isFinite(percentage)) return 0f;
        return Math.max(0f, Math.min(1f, percentage / 100f));
    }

    /** Current configured opacity for decorative in-game interface backgrounds. */
    public static float overlayAlpha() {
        return percentageToAlpha(GamePreferencesStore.get().getOverlayPercentage());
    }

    /** Draws a flipped region using the interface opacity, then restores the batch tint. */
    public static void drawOverlayRegionFlipped(SpriteBatch batch, TextureRegion region,
                                                float x, float y) {
        drawOverlayRegionFlipped(batch, region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    /** Draws a resized flipped region using the interface opacity, then restores the batch tint. */
    public static void drawOverlayRegionFlipped(SpriteBatch batch, TextureRegion region,
                                                float x, float y, float width, float height) {
        withOverlayAlpha(batch, () -> drawRegionFlipped(batch, region, x, y, width, height));
    }

    /** Runs a background draw with interface opacity and restores the previous batch tint. */
    public static void withOverlayAlpha(SpriteBatch batch, Runnable draw) {
        Color previous = new Color(batch.getColor());
        batch.setColor(previous.r, previous.g, previous.b, previous.a * overlayAlpha());
        try {
            draw.run();
        } finally {
            batch.setColor(previous);
        }
    }
}
