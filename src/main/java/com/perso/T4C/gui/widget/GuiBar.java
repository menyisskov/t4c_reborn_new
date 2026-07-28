package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;

import java.util.function.Supplier;

/** Reusable boxed bar component used for HP, mana and XP. */
public class GuiBar extends AbstractGuiElement implements GuiResizable {
    private final TextureRegion empty;
    private final TextureRegion fill;
    private final Supplier<Float> percent;
    private Supplier<String> value;
    private float width;
    private float height;

    public GuiBar(TextureRegion empty, TextureRegion fill, float x, float y,
                  float width, float height, Supplier<Float> percent) {
        super(x, y);
        this.empty = empty;
        this.fill = fill;
        this.width = width;
        this.height = height;
        this.percent = percent;
    }

    public GuiBar withValue(Supplier<String> value) { this.value = value; return this; }

    @Override public void render(SpriteBatch batch) {
        if (empty != null) GuiDraw.drawRegionFlipped(batch, empty, x, y, width, height);
        if (fill != null) {
            float amount = Math.max(0f, Math.min(1f, percent.get()));
            if (amount > 0f) GuiDraw.drawRegionFlipped(batch, fill, x, y, width * amount, height);
        }
        GuiBoxedItem.drawDebugBorder(batch, x, y, width, height);
    }

    @Override public GuiBar setSize(float width, float height) {
        this.width = Math.max(1f, width); this.height = Math.max(1f, height); return this;
    }
    @Override public float getWidth() { return width; }
    @Override public float getHeight() { return height; }
    public Supplier<String> getValue() { return value; }
}
