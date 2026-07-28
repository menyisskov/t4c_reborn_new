package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;
import com.perso.T4C.gui.core.GuiSprites;

/** Boxed full-map view. */
public final class GuiWorldMap extends AbstractGuiElement implements GuiResizable {
    private final TextureRegion illustratedMap;
    private float width;
    private float height;

    public GuiWorldMap(float x, float y, float width, float height) {
        super(x, y);
        this.illustratedMap = GuiSprites.load("GUI_MapAlthea");
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (illustratedMap != null) {
            GuiDraw.drawRegionFlipped(batch, illustratedMap, x, y, width, height);
        }
        GuiBoxedItem.drawDebugBorder(batch, x, y, width, height);
    }

    public void dispose() {
        // GUI sprite textures are owned by GuiSprites/SpriteLoader.
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public GuiWorldMap setSize(float width, float height) {
        this.width = Math.max(8f, width);
        this.height = Math.max(8f, height);
        return this;
    }
}
