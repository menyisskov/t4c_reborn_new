package com.perso.T4C.gui.widget;

import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.audio.SoundManager;

import java.util.Objects;
/**
 * Class representing GuiButton.
 */

public class GuiButton extends AbstractGuiElement implements GuiResizable {
    private final TextureRegion normal;
    private final TextureRegion hover;
    private final TextureRegion pressed;
    private final Runnable callback;
    private boolean isHovered;
    private boolean isPressed;
    private boolean visible = true;
    private boolean enabled = true;
    private float renderedWidth;
    private float renderedHeight;
    private float labelVerticalOffset;
    private com.badlogic.gdx.graphics.g2d.BitmapFont labelFont;
    private java.util.function.Supplier<String> labelText;
    private final com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
    private static final float LABEL_PAD = 2f;

    /** Optional text centered on the button, drawn over the sprite. */
    public GuiButton withLabel(com.badlogic.gdx.graphics.g2d.BitmapFont font, java.util.function.Supplier<String> text) {
        this.labelFont = font;
        this.labelText = text;
        return this;
    }

    /** Applies a small optical correction to the label baseline when a font has unusual metrics. */
    public GuiButton withLabelVerticalOffset(float offset) {
        this.labelVerticalOffset = offset;
        return this;
    }

    public GuiButton(TextureRegion normal, TextureRegion hover, TextureRegion pressed, float x, float y, Runnable callback) {
        super(x, y);
        this.normal = Objects.requireNonNull(normal);
        this.hover = Objects.requireNonNull(hover);
        this.pressed = Objects.requireNonNull(pressed);
        this.callback = callback;
        this.renderedWidth = normal.getRegionWidth();
        this.renderedHeight = normal.getRegionHeight();
    }

    @Override
    public GuiButton setSize(float width, float height) {
        this.renderedWidth = Math.max(1f, width);
        this.renderedHeight = Math.max(1f, height);
        return this;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            isHovered = false;
            isPressed = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!visible) {
            return;
        }
        TextureRegion region;
        if (!enabled) {
            region = normal;
        } else if (isPressed) {
            region = pressed;
        } else if (isHovered) {
            region = hover;
        } else {
            region = normal;
        }
        GuiDraw.drawRegionFlipped(batch, region, x, y, renderedWidth, renderedHeight);
        if (labelFont != null && labelText != null) {
            String text = labelText.get();
            if (text != null && !text.isEmpty()) {
                float w = renderedWidth;
                float h = renderedHeight;
                float prevScaleX = labelFont.getData().scaleX;
                float prevScaleY = labelFont.getData().scaleY;
                layout.setText(labelFont, text);
                float maxW = w - 2f * LABEL_PAD;
                float maxH = h - 2f * LABEL_PAD;
                if (layout.width > maxW || layout.height > maxH) {
                    float scale = Math.min(maxW / layout.width, maxH / layout.height);
                    labelFont.getData().setScale(prevScaleX * scale, prevScaleY * scale);
                    layout.setText(labelFont, text);
                }
                labelFont.draw(batch, layout,
                        x + (w - layout.width) / 2f,
                        y + (h - layout.height) / 2f + labelVerticalOffset);
                labelFont.getData().setScale(prevScaleX, prevScaleY);
            }
        }
        // Drawn after the sprite: the button fills its whole zone and would
        // cover a border drawn underneath.
        GuiBoxedItem.drawDebugBorder(batch, x, y, renderedWidth, renderedHeight);
    }

    public void onTouchDown(float screenX, float screenY) {
        isHovered = visible && enabled && contains(screenX, screenY);
        isPressed = isHovered;
    }

    public void onTouchUp(float screenX, float screenY) {
        if (visible && enabled && isPressed && contains(screenX, screenY) && callback != null) {
            SoundManager.interfaceSound("Generic pickup item.wav");
            callback.run();
        }
        isPressed = false;
        isHovered = visible && contains(screenX, screenY);
    }

    public void onMouseMove(float screenX, float screenY) {
        if (!isPressed) {
            isHovered = visible && contains(screenX, screenY);
        }
    }

    public boolean contains(float screenX, float screenY) {
        return visible && containsBox(screenX, screenY);
    }

    @Override
    public float getWidth() {
        return renderedWidth;
    }

    @Override
    public float getHeight() {
        return renderedHeight;
    }

}
