package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiDraw;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Original-client style option list with 16-pixel slots separated by 5-pixel gaps. */
public final class GuiOptionList extends AbstractGuiElement {
    public record Entry(Supplier<String> label, BooleanSupplier value, Consumer<Boolean> setter) {}

    private static final float SLOT_HEIGHT = 16f;
    private static final float ROW_PITCH = 21f;
    private static final float TOGGLE_X = 174f;
    private static final float TOGGLE_DRAW_X = 175f;
    private static final float TOGGLE_SLOT_SIZE = 16f;
    private static final float LABEL_X = 0f;
    private static final float LABEL_MAX_WIDTH = 167f;
    private final float width;
    private final float height;
    private final BitmapFont font;
    private final TextureRegion selection;
    private final TextureRegion on;
    private final TextureRegion onHover;
    private final TextureRegion off;
    private final TextureRegion offHover;
    private final List<Entry> entries;
    private final GlyphLayout labelLayout = new GlyphLayout();
    private int firstVisible;
    private int hovered = -1;
    private int pressed = -1;

    public GuiOptionList(float x, float y, float width, float height, BitmapFont font,
                         TextureRegion selection, TextureRegion on, TextureRegion onHover,
                         TextureRegion off, TextureRegion offHover, List<Entry> entries) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.font = font;
        this.selection = selection;
        this.on = on;
        this.onHover = onHover != null ? onHover : on;
        this.off = off;
        this.offHover = offHover != null ? offHover : off;
        this.entries = List.copyOf(entries);
    }

    @Override
    public void render(SpriteBatch batch) {
        int visible = visibleRows();
        for (int row = 0; row < visible && firstVisible + row < entries.size(); row++) {
            int index = firstVisible + row;
            float rowY = y + row * ROW_PITCH;
            if (index == hovered && selection != null) {
                GuiDraw.drawRegionFlipped(batch, selection, x - 2f, rowY);
            }
            Entry entry = entries.get(index);
            Color previous = new Color(font.getColor());
            font.setColor(Color.valueOf("DCDCDC"));
            String label = entry.label().get();
            float previousScaleX = font.getData().scaleX;
            float previousScaleY = font.getData().scaleY;
            labelLayout.setText(font, label);
            boolean scaledToFit = false;
            if (labelLayout.width > LABEL_MAX_WIDTH) {
                float scale = LABEL_MAX_WIDTH / labelLayout.width;
                font.getData().setScale(previousScaleX * scale, previousScaleY * scale);
                labelLayout.setText(font, label);
                scaledToFit = true;
            }
            font.draw(batch, labelLayout, x + LABEL_X, rowY + (scaledToFit ? 1f : 0f));
            font.getData().setScale(previousScaleX, previousScaleY);
            font.setColor(previous);
            TextureRegion toggle = entry.value().getAsBoolean()
                    ? (index == hovered ? onHover : on)
                    : (index == hovered ? offHover : off);
            if (toggle != null) {
                float toggleX = x + TOGGLE_DRAW_X;
                GuiDraw.drawRegionFlipped(batch, toggle, toggleX, rowY);
            }
        }
    }

    @Override
    public void onTouchDown(float screenX, float screenY) {
        pressed = toggleRowAt(screenX, screenY);
    }

    @Override
    public void onTouchUp(float screenX, float screenY) {
        int row = toggleRowAt(screenX, screenY);
        if (row >= 0 && row == pressed) {
            Entry entry = entries.get(row);
            entry.setter().accept(!entry.value().getAsBoolean());
        }
        pressed = -1;
    }

    @Override
    public void onMouseMove(float screenX, float screenY) {
        hovered = rowAt(screenX, screenY);
    }

    public void scroll(float amountY) {
        int max = Math.max(0, entries.size() - visibleRows());
        firstVisible = Math.max(0, Math.min(max, firstVisible + (amountY > 0 ? 1 : -1)));
    }

    private int rowAt(float screenX, float screenY) {
        if (!contains(screenX, screenY)) return -1;
        float relativeY = screenY - y;
        int row = (int) (relativeY / ROW_PITCH);
        if (relativeY - row * ROW_PITCH >= SLOT_HEIGHT) return -1;
        int index = firstVisible + row;
        return index < entries.size() ? index : -1;
    }

    private int toggleRowAt(float screenX, float screenY) {
        if (screenX < x + TOGGLE_X || screenX > x + TOGGLE_X + TOGGLE_SLOT_SIZE) return -1;
        return rowAt(screenX, screenY);
    }

    private int visibleRows() {
        return Math.max(1, 1 + (int) ((height - 1f) / ROW_PITCH));
    }

    @Override public float getWidth() { return width; }
    @Override public float getHeight() { return height; }
}
