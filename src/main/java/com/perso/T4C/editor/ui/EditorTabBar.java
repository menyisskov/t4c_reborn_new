package com.perso.T4C.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Horizontal tab bar. Each tab has a label and an optional selection callback.
 * Tabs are laid out automatically across the full width of the component bounds.
 *
 * Usage:
 * <pre>
 *   EditorTabBar tabs = new EditorTabBar()
 *       .addTab("Edit")
 *       .addTab("Preview")
 *       .accentColor(EditorTheme.ORANGE)
 *       .onSelect(idx -> activeTab = idx);
 *   tabs.setBounds(x, y, w, TAB_HEIGHT);
 * </pre>
 */
public class EditorTabBar extends EditorControl {

    private static final float ACTIVE_ALPHA   = 0.85f;
    private static final float INACTIVE_ALPHA = 0.38f;

    private final List<String> labels = new ArrayList<>();
    private final List<Rectangle> tabRects = new ArrayList<>();
    private BitmapFont font;
    private Color accent = EditorTheme.BLUE;
    private int selectedIndex = 0;
    private IntConsumer onSelect;

    public EditorTabBar addTab(String label) {
        labels.add(label);
        tabRects.add(new Rectangle());
        return this;
    }

    public EditorTabBar accentColor(Color color) {
        this.accent = color;
        return this;
    }

    public EditorTabBar withFont(BitmapFont font) {
        this.font = font;
        return this;
    }

    public EditorTabBar onSelect(IntConsumer consumer) {
        this.onSelect = consumer;
        return this;
    }

    public int selectedIndex() { return selectedIndex; }

    public EditorTabBar select(int index) {
        if (index >= 0 && index < labels.size()) {
            selectedIndex = index;
            if (onSelect != null) onSelect.accept(selectedIndex);
        }
        return this;
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer sr) {
        if (!visible || labels.isEmpty()) return;
        layoutTabs();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < tabRects.size(); i++) {
            Rectangle r = tabRects.get(i);
            float alpha = (i == selectedIndex) ? ACTIVE_ALPHA : INACTIVE_ALPHA;
            sr.setColor(accent.r, accent.g, accent.b, alpha);
            sr.rect(r.x, r.y, r.width, r.height);
        }
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(EditorTheme.BORDER);
        for (Rectangle r : tabRects) {
            sr.rect(r.x, r.y, r.width, r.height);
        }
        sr.end();

        if (font == null) return;
        batch.begin();
        for (int i = 0; i < labels.size(); i++) {
            Color textColor = (i == selectedIndex) ? EditorTheme.TEXT_LIGHT : EditorTheme.TEXT_MUTED;
            EditorPanelChrome.buttonText(batch, font, tabRects.get(i), labels.get(i), textColor);
        }
        batch.end();
    }

    @Override
    public boolean handleClick(int screenX, int screenY, int button) {
        if (!visible || !enabled) return false;
        for (int i = 0; i < tabRects.size(); i++) {
            if (tabRects.get(i).contains(screenX, screenY)) {
                selectedIndex = i;
                if (onSelect != null) onSelect.accept(i);
                return true;
            }
        }
        return false;
    }

    private void layoutTabs() {
        int n = labels.size();
        if (n == 0) return;
        float tabW = bounds.width / n;
        for (int i = 0; i < n; i++) {
            tabRects.get(i).set(bounds.x + i * tabW, bounds.y, tabW, bounds.height);
        }
    }
}
