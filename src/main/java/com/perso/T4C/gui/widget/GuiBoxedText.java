package com.perso.T4C.gui.widget;

import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiResizable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.ui.FontManager;

import java.util.function.Supplier;

/**
 * A label centered both horizontally and vertically inside a fixed rectangle.
 *
 * The rectangle is the layout unit: position/size it over a background plaque
 * and the text stays centered whatever its length or the active language.
 * With {@link GameConstants#DEBUG_GUI_TEXT_BOUNDS} enabled the zone border is
 * drawn so it can be tuned (Ctrl+drag logs the box's top-left offset).
 */
public class GuiBoxedText extends GuiText implements GuiResizable {
    /** Horizontal alignment of the text inside the box. */
    public enum Align { LEFT, CENTER, RIGHT }

    private static final float PAD_X = 2f;
    private static final float PAD_Y = 1f;

    private float width;
    private float height;
    private Align align = Align.CENTER;
    private boolean shrinkToFit = false;
    private boolean wrap = false;

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

    /** Wraps text on multiple lines inside the box instead of shrinking one long line. */
    public GuiBoxedText wrap() {
        this.wrap = true;
        return this;
    }

    @Override
    public GuiBoxedText setSize(float width, float height) {
        this.width = Math.max(1f, width);
        this.height = Math.max(1f, height);
        return this;
    }

    public GuiBoxedText boxed(float width, float height) {
        return setSize(width, height);
    }

    @Override
    public void render(SpriteBatch batch) {
        GuiBoxedItem.drawDebugBorder(batch, getX(), getY(), width, height);
        String text = textSupplier.get();
        if (text == null || text.isEmpty()) {
            return;
        }
        FontManager.getInstance().applyCurrentQuality(font);
        float prevScaleX = font.getData().scaleX;
        float prevScaleY = font.getData().scaleY;
        float maxW = width - 2f * PAD_X;
        float maxH = height - 2f * PAD_Y;
        Color renderColor = colorSupplier != null ? colorSupplier.get() : font.getColor();
        GlyphLayout layout = createLayout(text, maxW, renderColor);
        if (shrinkToFit && layout.width > 0f && layout.height > 0f
                && (layout.width > maxW || layout.height > maxH)) {
            float scale = Math.min(maxW / layout.width, maxH / layout.height);
            font.getData().setScale(prevScaleX * scale, prevScaleY * scale);
            layout = createLayout(text, maxW, renderColor);
        }
        // Wrapped layouts already position every line inside maxW according to
        // their alignment. Applying the single-line offset again would center
        // the text twice and shift centered/right-aligned paragraphs.
        float tx = wrap ? getX() + PAD_X : switch (align) {
            case LEFT -> getX() + PAD_X;
            case RIGHT -> getX() + width - PAD_X - layout.width;
            case CENTER -> getX() + (width - layout.width) / 2f;
        };
        float ty = getY() + (height - layout.height) / 2f;
        Color previous = new Color(font.getColor());
        if (colorSupplier != null) {
            font.setColor(colorSupplier.get());
        }
        if (wrap) {
            font.draw(batch, layout, tx, ty);
        } else {
            font.draw(batch, text, tx, ty);
        }
        font.setColor(previous);
        font.getData().setScale(prevScaleX, prevScaleY);
    }

    private GlyphLayout createLayout(String text, float maxWidth, Color color) {
        if (!wrap) {
            return new GlyphLayout(font, text);
        }
        int libGdxAlign = switch (align) {
            case LEFT -> com.badlogic.gdx.utils.Align.left;
            case RIGHT -> com.badlogic.gdx.utils.Align.right;
            case CENTER -> com.badlogic.gdx.utils.Align.center;
        };
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text, color, Math.max(1f, maxWidth), libGdxAlign, true);
        return layout;
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
