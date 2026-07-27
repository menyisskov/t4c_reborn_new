package com.perso.T4C.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public final class EditorPanelChrome {
    public static final float BUTTON_HEIGHT = 30f;

    private EditorPanelChrome() {
    }

    public static void overlay(ShapeRenderer renderer, float screenWidth, float screenHeight) {
        renderer.setColor(EditorTheme.APP_OVERLAY);
        renderer.rect(0, 0, screenWidth, screenHeight);
    }

    public static void panel(ShapeRenderer renderer, Rectangle bounds, Color accent, float headerHeight) {
        renderer.setColor(EditorTheme.PANEL_DARK_2);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        renderer.setColor(EditorTheme.PANEL_DARK);
        renderer.rect(bounds.x, bounds.y + bounds.height - headerHeight, bounds.width, headerHeight);
        renderer.setColor(accent);
        renderer.rect(bounds.x, bounds.y + bounds.height - 4f, bounds.width, 4f);
    }

    public static void surface(ShapeRenderer renderer, Rectangle bounds) {
        renderer.setColor(EditorTheme.SURFACE);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public static void textField(ShapeRenderer renderer, Rectangle bounds, boolean active) {
        renderer.setColor(active ? EditorTheme.SURFACE_HOVER : EditorTheme.SURFACE);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public static void dropdownTrigger(ShapeRenderer renderer, Rectangle bounds, boolean open) {
        renderer.setColor(open ? EditorTheme.SURFACE_HOVER : EditorTheme.SURFACE);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public static void checkbox(ShapeRenderer renderer, Rectangle bounds, boolean checked) {
        renderer.setColor(checked ? EditorTheme.BLUE : EditorTheme.PANEL_DARK);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public static void darkSurface(ShapeRenderer renderer, Rectangle bounds) {
        renderer.setColor(EditorTheme.PANEL_DARK);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public static void selectedRow(ShapeRenderer renderer, Rectangle listBounds, int localRow, float rowHeight) {
        float rowY = listBounds.y + listBounds.height - (localRow + 1) * rowHeight;
        renderer.setColor(EditorTheme.BLUE);
        renderer.rect(listBounds.x + 2f, rowY + 1f, listBounds.width - 4f, rowHeight - 2f);
    }

    public static void selectedRow(ShapeRenderer renderer, Rectangle bounds) {
        renderer.setColor(EditorTheme.BLUE);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public static void button(ShapeRenderer renderer, Rectangle bounds, boolean active, boolean disabled) {
        if (disabled) {
            renderer.setColor(0.24f, 0.29f, 0.34f, 0.92f);
        } else {
            renderer.setColor(active ? EditorTheme.BLUE : new Color(0.22f, 0.32f, 0.42f, 1f));
        }
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public static void scrollbar(ShapeRenderer renderer, Rectangle track, Rectangle thumb) {
        renderer.setColor(0.06f, 0.08f, 0.11f, 0.95f);
        renderer.rect(track.x, track.y, track.width, track.height);
        renderer.setColor(0.75f, 0.82f, 0.9f, 0.92f);
        renderer.rect(thumb.x, thumb.y, thumb.width, thumb.height);
    }

    public static void border(ShapeRenderer renderer, Rectangle bounds) {
        renderer.setColor(EditorTheme.BORDER);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public static void buttonText(SpriteBatch batch, BitmapFont font, Rectangle bounds, String label, Color color) {
        GlyphLayout layout = new GlyphLayout(font, label);
        font.setColor(color.a < 1f ? color : EditorTheme.TEXT_LIGHT);
        font.draw(batch, label, bounds.x + (bounds.width - layout.width) * 0.5f,
                bounds.y + (bounds.height + layout.height) * 0.5f + 1f);
    }

    public static String fitText(BitmapFont font, GlyphLayout layout, String text, float maxWidth) {
        if (text == null) {
            return "";
        }
        layout.setText(font, text);
        if (layout.width <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int end = text.length();
        while (end > 0) {
            String candidate = text.substring(0, end).trim() + suffix;
            layout.setText(font, candidate);
            if (layout.width <= maxWidth) {
                return candidate;
            }
            end--;
        }
        return suffix;
    }
}
