package com.perso.T4C.gui.widget;

import com.perso.T4C.gui.core.GuiBoxedItem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.config.GameConstants;

import java.util.function.Supplier;

/**
 * A label centered both horizontally and vertically inside a fixed rectangle.
 *
 * The rectangle is the layout unit: position/size it over a background plaque
 * and the text stays centered whatever its length or the active language.
 * With {@link GameConstants#DEBUG_GUI_TEXT_BOUNDS} enabled the zone border is
 * drawn so it can be tuned (Ctrl+drag logs the box's top-left offset).
 */
public class GuiBoxedText extends GuiText {
    /** Horizontal alignment of the text inside the box. */
    public enum Align { LEFT, CENTER, RIGHT }

    private static final float PAD_X = 2f;
    private static final float PAD_Y = 1f;

    private final float width;
    private final float height;
    private Align align = Align.CENTER;
    private boolean shrinkToFit = false;

    public GuiBoxedText(BitmapFont font, float x, float y, float width, float height,
                        Supplier<String> text, Supplier<Color> color) {
        super(font, x, y, text, color);
        this.width = width;
        this.height = height;
    }

    /** Horizontal alignment inside the box (default CENTER). Returns this for chaining. */
    public GuiBoxedText align(Align align) {
        this.align = align;
        return this;
    }

    /** Scales the font down when the text would overflow the box. Returns this for chaining. */
    public GuiBoxedText shrinkToFit() {
        this.shrinkToFit = true;
        return this;
    }

    @Override
    public void render(SpriteBatch batch) {
        GuiBoxedItem.drawDebugBorder(batch, getX(), getY(), width, height);
        String text = textSupplier.get();
        if (text == null || text.isEmpty()) {
            return;
        }
        float prevScaleX = font.getData().scaleX;
        float prevScaleY = font.getData().scaleY;
        GlyphLayout layout = new GlyphLayout(font, text);
        float maxW = width - 2f * PAD_X;
        float maxH = height - 2f * PAD_Y;
        if (shrinkToFit && layout.width > 0f && layout.height > 0f
                && (layout.width > maxW || layout.height > maxH)) {
            float scale = Math.min(maxW / layout.width, maxH / layout.height);
            font.getData().setScale(prevScaleX * scale, prevScaleY * scale);
            layout = new GlyphLayout(font, text);
        }
        float tx = switch (align) {
            case LEFT -> getX() + PAD_X;
            case RIGHT -> getX() + width - PAD_X - layout.width;
            case CENTER -> getX() + (width - layout.width) / 2f;
        };
        float ty = getY() + (height - layout.height) / 2f;
        Color previous = new Color(font.getColor());
        if (colorSupplier != null) {
            font.setColor(colorSupplier.get());
        }
        font.draw(batch, text, tx, ty);
        font.setColor(previous);
        font.getData().setScale(prevScaleX, prevScaleY);
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
    public boolean contains(float screenX, float screenY) {
        return containsBox(screenX, screenY);
    }
}
