package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiDraw;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Original-client style two-column option list with 16-pixel rows. */
public final class GuiOptionList extends AbstractGuiElement {
    public record Entry(Supplier<String> label, BooleanSupplier value, Consumer<Boolean> setter) {}

    private static final float ROW_HEIGHT = 16f;
    private static final float TOGGLE_X = 177f;
    private final float width;
    private final float height;
    private final BitmapFont font;
    private final TextureRegion selection;
    private final TextureRegion on;
    private final TextureRegion onHover;
    private final TextureRegion off;
    private final TextureRegion offHover;
    private final List<Entry> entries;
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
            float rowY = y + row * ROW_HEIGHT;
            if (index == hovered && selection != null) {
                GuiDraw.drawRegionFlipped(batch, selection, x - 2f, rowY, 171f, ROW_HEIGHT);
            }
            Entry entry = entries.get(index);
            Color previous = new Color(font.getColor());
            font.setColor(Color.valueOf("DCDCDC"));
            font.draw(batch, entry.label().get(), x + 2f, rowY + 1f);
            font.setColor(previous);
            TextureRegion toggle = entry.value().getAsBoolean()
                    ? (index == hovered ? onHover : on)
                    : (index == hovered ? offHover : off);
            if (toggle != null) GuiDraw.drawRegionFlipped(batch, toggle, x + TOGGLE_X, rowY);
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
        int index = firstVisible + (int) ((screenY - y) / ROW_HEIGHT);
        return index < entries.size() ? index : -1;
    }

    private int toggleRowAt(float screenX, float screenY) {
        if (screenX < x + TOGGLE_X || screenX > x + TOGGLE_X + 16f) return -1;
        return rowAt(screenX, screenY);
    }

    private int visibleRows() {
        return Math.max(1, (int) (height / ROW_HEIGHT));
    }

    @Override public float getWidth() { return width; }
    @Override public float getHeight() { return height; }
}
