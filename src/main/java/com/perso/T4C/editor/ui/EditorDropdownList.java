package com.perso.T4C.editor.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditorDropdownList<T> extends EditorControl {
    private static final long AUTOCOMPLETE_RESET_MILLIS = 900L;

    private final List<T> items = new ArrayList<>();
    private Consumer<T> selectionConsumer;
    private Function<T, String> labelProvider = value -> value == null ? "" : value.toString();
    private int selectedIndex = -1;
    private int scrollOffset;
    private int visibleRows = 6;
    private float rowHeight = 22f;
    private boolean open;
    private final Rectangle scrollTrackBounds = new Rectangle();
    private final Rectangle scrollThumbBounds = new Rectangle();
    private final GlyphLayout layout = new GlyphLayout();
    private final StringBuilder autocompleteText = new StringBuilder();
    private long lastAutocompleteMillis;

    public EditorDropdownList<T> setItems(List<T> values) {
        items.clear();
        if (values != null) {
            items.addAll(values);
        }
        selectedIndex = items.isEmpty() ? -1 : Math.min(Math.max(0, selectedIndex), items.size() - 1);
        return this;
    }

    public EditorDropdownList<T> onSelection(Consumer<T> consumer) {
        this.selectionConsumer = consumer;
        return this;
    }

    public EditorDropdownList<T> labelProvider(Function<T, String> labelProvider) {
        this.labelProvider = labelProvider == null ? value -> value == null ? "" : value.toString() : labelProvider;
        return this;
    }

    public List<T> items() {
        return items;
    }

    public T selectedItem() {
        return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : null;
    }

    public void select(int index) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        selectedIndex = index;
        ensureVisible(index);
        if (selectionConsumer != null) {
            selectionConsumer.accept(items.get(index));
        }
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public void clearSelection() {
        selectedIndex = -1;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        if (!open) {
            clearAutocomplete();
        }
    }

    public EditorDropdownList<T> visibleRows(int visibleRows) {
        this.visibleRows = Math.max(1, visibleRows);
        return this;
    }

    public EditorDropdownList<T> rowHeight(float rowHeight) {
        this.rowHeight = Math.max(16f, rowHeight);
        return this;
    }

    public int scrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = clampScrollOffset(scrollOffset);
    }

    public int visibleRows() {
        return visibleRows;
    }

    public Rectangle scrollTrackBounds() {
        return scrollTrackBounds;
    }

    public Rectangle scrollThumbBounds() {
        return scrollThumbBounds;
    }

    public void renderDropdown(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
        if (!open || !visible) {
            return;
        }
        layoutScrollBar();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(EditorTheme.SURFACE);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        int displayedRows = displayedRows();
        int end = Math.min(items.size(), scrollOffset + displayedRows);
        for (int i = scrollOffset; i < end; i++) {
            if (i == selectedIndex) {
                float rowY = rowY(i);
                shapeRenderer.setColor(EditorTheme.SURFACE_HOVER);
                shapeRenderer.rect(bounds.x + 2f, rowY + 1f, bounds.width - scrollbarReserve() - 4f, rowHeight - 2f);
                shapeRenderer.setColor(EditorTheme.SURFACE);
            }
        }

        if (items.size() > displayedRows) {
            EditorPanelChrome.scrollbar(shapeRenderer, scrollTrackBounds, scrollThumbBounds);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        EditorPanelChrome.border(shapeRenderer, bounds);
        shapeRenderer.end();

        batch.begin();
        int textEnd = Math.min(items.size(), scrollOffset + displayedRows);
        float textY = bounds.y + bounds.height - 8f;
        float textWidth = bounds.width - scrollbarReserve() - 16f;
        for (int i = scrollOffset; i < textEnd; i++) {
            font.setColor(i == selectedIndex ? EditorTheme.BLUE : EditorTheme.TEXT);
            font.draw(batch, EditorPanelChrome.fitText(font, layout, labelProvider.apply(items.get(i)), textWidth),
                    bounds.x + 8f, textY);
            textY -= rowHeight;
        }
        batch.end();
    }

    private void layoutScrollBar() {
        scrollOffset = clampScrollOffset(scrollOffset);
        scrollTrackBounds.set(bounds.x + bounds.width - 12f, bounds.y + 3f, 8f, bounds.height - 6f);
        int rows = displayedRows();
        int maxOffset = Math.max(0, items.size() - rows);
        float thumbHeight = items.isEmpty() ? scrollTrackBounds.height
                : Math.max(24f, scrollTrackBounds.height * (rows / (float) Math.max(rows, items.size())));
        float thumbTravel = Math.max(0f, scrollTrackBounds.height - thumbHeight);
        float thumbY = scrollTrackBounds.y + thumbTravel;
        if (maxOffset > 0) {
            thumbY -= thumbTravel * (scrollOffset / (float) maxOffset);
        }
        scrollThumbBounds.set(scrollTrackBounds.x, thumbY, scrollTrackBounds.width, thumbHeight);
    }

    private int clampScrollOffset(int value) {
        return Math.max(0, Math.min(Math.max(0, items.size() - displayedRows()), value));
    }

    private float rowY(int itemIndex) {
        return bounds.y + bounds.height - (itemIndex - scrollOffset + 1) * rowHeight;
    }

    @Override
    public boolean handleClick(int screenX, int screenY, int button) {
        if (!open || !visible) return false;
        if (!bounds.contains(screenX, screenY)) return false;
        if (items.size() > displayedRows() && scrollTrackBounds.contains(screenX, screenY)) {
            int maxOffset = Math.max(0, items.size() - displayedRows());
            if (maxOffset > 0) {
                float ratio = (scrollTrackBounds.y + scrollTrackBounds.height - screenY) / scrollTrackBounds.height;
                scrollOffset = clampScrollOffset(Math.round(ratio * maxOffset));
            }
            return true;
        }
        int row = (int) ((bounds.y + bounds.height - screenY) / rowHeight);
        if (row < 0 || row >= displayedRows()) {
            return true;
        }
        int idx = scrollOffset + row;
        if (idx >= 0 && idx < items.size()) {
            select(idx);
        }
        return true;
    }

    @Override
    public boolean handleKeyTyped(char character) {
        if (!open || !visible || !enabled) {
            return false;
        }
        if (character == '\r' || character == '\n') {
            setOpen(false);
            return true;
        }
        if (character == '\b') {
            if (autocompleteText.length() > 0) {
                autocompleteText.deleteCharAt(autocompleteText.length() - 1);
                lastAutocompleteMillis = TimeUtils.millis();
                selectAutocompleteMatch();
            }
            return true;
        }
        if (character < 32 || character == 127) {
            return true;
        }
        long now = TimeUtils.millis();
        if (now - lastAutocompleteMillis > AUTOCOMPLETE_RESET_MILLIS) {
            autocompleteText.setLength(0);
        }
        autocompleteText.append(character);
        lastAutocompleteMillis = now;
        selectAutocompleteMatch();
        return true;
    }

    public String autocompleteText() {
        return autocompleteText.toString();
    }

    private void selectAutocompleteMatch() {
        if (autocompleteText.length() == 0 || items.isEmpty()) {
            return;
        }
        String query = normalize(autocompleteText.toString());
        int containsMatch = -1;
        for (int i = 0; i < items.size(); i++) {
            String label = normalize(labelProvider.apply(items.get(i)));
            if (label.startsWith(query)) {
                select(i);
                return;
            }
            if (containsMatch < 0 && label.contains(query)) {
                containsMatch = i;
            }
        }
        if (containsMatch >= 0) {
            select(containsMatch);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private void ensureVisible(int index) {
        if (index < 0) {
            return;
        }
        if (index < scrollOffset) {
            scrollOffset = index;
        } else if (index >= scrollOffset + displayedRows()) {
            scrollOffset = index - displayedRows() + 1;
        }
        scrollOffset = clampScrollOffset(scrollOffset);
    }

    private void clearAutocomplete() {
        autocompleteText.setLength(0);
        lastAutocompleteMillis = 0L;
    }

    private float scrollbarReserve() {
        return items.size() > displayedRows() ? 16f : 0f;
    }

    private int displayedRows() {
        int rowsByHeight = bounds.height <= 0f ? visibleRows : (int) ((bounds.height - 4f) / rowHeight);
        return Math.max(1, Math.min(visibleRows, rowsByHeight));
    }

}
