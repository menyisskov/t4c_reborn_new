package com.perso.T4C.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditorListBox<T> extends EditorControl {
    private final List<T> items = new ArrayList<>();
    private Function<T, String> labelProvider = v -> v == null ? "" : v.toString();
    private Function<Integer, Color> colorProvider = null;
    private Consumer<T> selectionConsumer;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private float rowHeight = 22f;
    private boolean darkBackground = false;
    private final Rectangle scrollTrackBounds = new Rectangle();
    private final Rectangle scrollThumbBounds = new Rectangle();
    private final GlyphLayout layout = new GlyphLayout();

    public EditorListBox<T> setItems(List<T> values) {
        items.clear();
        if (values != null) items.addAll(values);
        selectedIndex = items.isEmpty() ? -1 : Math.min(Math.max(selectedIndex, -1), items.size() - 1);
        scrollOffset = clamp(scrollOffset);
        return this;
    }

    public List<T> items() { return items; }

    public EditorListBox<T> labelProvider(Function<T, String> fn) {
        this.labelProvider = fn == null ? v -> v == null ? "" : v.toString() : fn;
        return this;
    }

    public EditorListBox<T> darkBackground() {
        this.darkBackground = true;
        return this;
    }

    /** Optional per-row color override; receives the item index, returns null to use default. */
    public EditorListBox<T> colorProvider(Function<Integer, Color> fn) {
        this.colorProvider = fn;
        return this;
    }

    public EditorListBox<T> onSelection(Consumer<T> consumer) {
        this.selectionConsumer = consumer;
        return this;
    }

    public EditorListBox<T> rowHeight(float h) {
        this.rowHeight = Math.max(14f, h);
        return this;
    }

    public T selectedItem() {
        return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : null;
    }

    public int selectedIndex() { return selectedIndex; }

    public void select(int index) {
        if (index < 0 || index >= items.size()) return;
        selectedIndex = index;
        ensureVisible(selectedIndex);
        if (selectionConsumer != null) selectionConsumer.accept(items.get(index));
    }

    public void clearSelection() { selectedIndex = -1; }

    public int scrollOffset() { return scrollOffset; }

    public void setScrollOffset(int v) { scrollOffset = clamp(v); }

    public void scroll(int delta) { scrollOffset = clamp(scrollOffset + delta); }

    public void render(SpriteBatch batch, ShapeRenderer sr, BitmapFont font) {
        if (!visible) return;
        layoutScrollBar();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(darkBackground ? EditorTheme.SURFACE_DARK : EditorTheme.SURFACE);
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        int visibleRows = visibleRows();
        int end = Math.min(items.size(), scrollOffset + visibleRows);
        for (int i = scrollOffset; i < end; i++) {
            boolean highlighted = colorProvider != null ? colorProvider.apply(i) != null : i == selectedIndex;
            if (highlighted) {
                float ry = rowY(i);
                sr.setColor(darkBackground ? EditorTheme.PANEL_DARK_2 : EditorTheme.SURFACE_HOVER);
                sr.rect(bounds.x + 2f, ry + 1f, bounds.width - scrollbarReserve() - 4f, rowHeight - 2f);
            }
        }
        if (needsScrollbar()) EditorPanelChrome.scrollbar(sr, scrollTrackBounds, scrollThumbBounds);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        EditorPanelChrome.border(sr, bounds);
        sr.end();

        batch.begin();
        float textWidth = bounds.width - scrollbarReserve() - 16f;
        for (int i = scrollOffset; i < end; i++) {
            Color c = colorProvider != null ? colorProvider.apply(i) : null;
            Color defaultText = darkBackground ? EditorTheme.TEXT_LIGHT : EditorTheme.TEXT;
            font.setColor(c != null ? c : (i == selectedIndex ? EditorTheme.BLUE : defaultText));
            font.draw(batch,
                    EditorPanelChrome.fitText(font, layout, labelProvider.apply(items.get(i)), textWidth),
                    bounds.x + 8f, rowY(i) + rowHeight - 6f);
        }
        batch.end();
    }

    @Override
    public boolean handleClick(int screenX, int screenY, int button) {
        if (!visible || !enabled || !bounds.contains(screenX, screenY)) return false;
        if (needsScrollbar() && scrollTrackBounds.contains(screenX, screenY)) {
            int maxOffset = Math.max(0, items.size() - visibleRows());
            if (maxOffset > 0) {
                float ratio = (scrollTrackBounds.y + scrollTrackBounds.height - screenY) / scrollTrackBounds.height;
                scrollOffset = clamp(Math.round(ratio * maxOffset));
            }
            return true;
        }
        int row = (int) ((bounds.y + bounds.height - screenY) / rowHeight);
        int idx = scrollOffset + row;
        if (idx >= 0 && idx < items.size()) select(idx);
        return true;
    }

    private float rowY(int i) {
        return bounds.y + bounds.height - (i - scrollOffset + 1) * rowHeight;
    }

    private int visibleRows() {
        return Math.max(1, (int) (bounds.height / rowHeight));
    }

    private boolean needsScrollbar() {
        return items.size() > visibleRows();
    }

    private float scrollbarReserve() {
        return needsScrollbar() ? 16f : 0f;
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(Math.max(0, items.size() - visibleRows()), v));
    }

    public void ensureVisible(int idx) {
        int vis = visibleRows();
        if (idx < scrollOffset) scrollOffset = idx;
        else if (idx >= scrollOffset + vis) scrollOffset = idx - vis + 1;
        scrollOffset = clamp(scrollOffset);
    }

    private void layoutScrollBar() {
        scrollOffset = clamp(scrollOffset);
        scrollTrackBounds.set(bounds.x + bounds.width - 12f, bounds.y + 3f, 8f, bounds.height - 6f);
        int maxOffset = Math.max(0, items.size() - visibleRows());
        float thumbH = items.isEmpty() ? scrollTrackBounds.height
                : Math.max(20f, scrollTrackBounds.height * (visibleRows() / (float) Math.max(visibleRows(), items.size())));
        float thumbTravel = Math.max(0f, scrollTrackBounds.height - thumbH);
        float thumbY = scrollTrackBounds.y + thumbTravel;
        if (maxOffset > 0) thumbY -= thumbTravel * (scrollOffset / (float) maxOffset);
        scrollThumbBounds.set(scrollTrackBounds.x, thumbY, scrollTrackBounds.width, thumbH);
    }
}
