package com.perso.T4C.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Draws an explicit diagnostic tile when a sprite name cannot be resolved. */
final class MissingTileRenderer {
    private static TextureRegion pixel;
    private static BitmapFont font;
    private static GlyphLayout layout;

    private MissingTileRenderer() {}

    static void draw(SpriteBatch batch, String id, float x, float y) {
        if (pixel == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(1f, 0.2f, 0.1f, 1f); p.drawPixel(0, 0);
            pixel = new TextureRegion(new Texture(p)); p.dispose();
            font = new BitmapFont();
            font.setColor(1f, 0.95f, 0.15f, 1f);
            font.getData().setScale(0.68f, -0.68f);
            font.setUseIntegerPositions(false);
            layout = new GlyphLayout();
        }
        String displayId = id != null && id.startsWith("0x") ? id.substring(2) : id;
        batch.setColor(0f, 0f, 0f, 1f);
        layout.setText(font, displayId);
        float textX = x + 1f;
        float textY = y + 2f;
        font.draw(batch, displayId, textX, textY);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
