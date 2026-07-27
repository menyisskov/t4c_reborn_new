package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.config.GameConstants;

/**
 * Shared debug-zone helper for every boxed GUI element (texts, sprites,
 * buttons, equipment slots). Draws the tunable red 2px border around a
 * {left, top, width, height} zone when {@link GameConstants#DEBUG_GUI_TEXT_BOUNDS}
 * is enabled, and owns the 1×1 white texture used to draw it.
 */
public final class GuiBoxedItem {

    private static final float BORDER_THICKNESS = 2f;

    private static Texture pixel;

    private GuiBoxedItem() {
    }

    /** Draws the red debug border around the zone when the debug flag is on. */
    public static void drawDebugBorder(SpriteBatch batch, float x, float y, float width, float height) {
        if (!GameConstants.DEBUG_GUI_TEXT_BOUNDS || width <= 0f || height <= 0f) {
            return;
        }
        Texture px = pixel();
        float t = BORDER_THICKNESS;
        Color previous = new Color(batch.getColor());
        batch.setColor(Color.RED);
        batch.draw(px, x, y, width, t);
        batch.draw(px, x, y + height - t, width, t);
        batch.draw(px, x, y, t, height);
        batch.draw(px, x + width - t, y, t, height);
        batch.setColor(previous);
    }

    /** Shared 1×1 white texture for debug-zone borders. */
    public static Texture pixel() {
        if (pixel == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(Color.WHITE);
            pm.fill();
            pixel = new Texture(pm);
            pm.dispose();
        }
        return pixel;
    }
}
