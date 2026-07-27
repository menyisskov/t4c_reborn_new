package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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
}
