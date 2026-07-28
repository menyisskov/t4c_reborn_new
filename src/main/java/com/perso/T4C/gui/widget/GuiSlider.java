package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/** Horizontal slider with click and drag support. */
public class GuiSlider extends AbstractGuiElement implements GuiResizable {
    private final TextureRegion track;
    private final TextureRegion thumb;
    private float width;
    private float height;
    private final DoubleSupplier value;
    private final DoubleConsumer setter;
    private boolean dragging;

    public GuiSlider(TextureRegion track, TextureRegion thumb, float x, float y, float width,
                     DoubleSupplier value, DoubleConsumer setter) {
        super(x, y);
        this.track = track;
        this.thumb = thumb;
        this.width = width;
        this.height = track != null ? track.getRegionHeight() : 20f;
        this.value = value;
        this.setter = setter;
    }

    /** Resizes the boxed zone used for rendering, hit-testing and Ctrl+drag tuning. */
    public GuiSlider boxed(float width, float height) {
        this.width = Math.max(1f, width);
        this.height = Math.max(1f, height);
        return this;
    }

    @Override
    public GuiSlider setSize(float width, float height) {
        return boxed(width, height);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (track != null) {
            float trackHeight = Math.min(height, track.getRegionHeight());
            GuiDraw.drawRegionFlipped(batch, track, x, y + (height - trackHeight) / 2f, width, trackHeight);
        }
        if (thumb != null) {
            float travel = Math.max(0f, width - thumb.getRegionWidth());
            float thumbX = x + travel * clamp((float) value.getAsDouble());
            GuiDraw.drawRegionFlipped(batch, thumb, thumbX,
                    y + (height - thumb.getRegionHeight()) / 2f);
        }
        GuiBoxedItem.drawDebugBorder(batch, x, y, width, height);
    }

    @Override
    public void onTouchDown(float screenX, float screenY) {
        dragging = contains(screenX, screenY);
        if (dragging) update(screenX);
    }

    @Override
    public void onTouchUp(float screenX, float screenY) {
        if (dragging) update(screenX);
        dragging = false;
    }

    @Override
    public void onMouseMove(float screenX, float screenY) {
        if (dragging) update(screenX);
    }

    private void update(float screenX) {
        setter.accept(clamp((screenX - x) / width));
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    @Override public float getWidth() { return width; }
    @Override public float getHeight() { return height; }
}
