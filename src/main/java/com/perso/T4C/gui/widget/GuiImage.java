package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;

/** A non-interactive GUI item that draws one sprite at an explicit size. */
public final class GuiImage extends AbstractGuiElement implements GuiResizable {
    private final TextureRegion region;
    private float width;
    private float height;

    public GuiImage(TextureRegion region, float x, float y, float width, float height) {
        super(x, y);
        this.region = region;
        setSize(width, height);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (region != null) GuiDraw.drawRegionFlipped(batch, region, x, y, width, height);
        GuiBoxedItem.drawDebugBorder(batch, x, y, width, height);
    }

    @Override
    public GuiImage setSize(float width, float height) {
        this.width = Math.max(1f, width);
        this.height = Math.max(1f, height);
        return this;
    }

    @Override public float getWidth() { return width; }
    @Override public float getHeight() { return height; }
}
