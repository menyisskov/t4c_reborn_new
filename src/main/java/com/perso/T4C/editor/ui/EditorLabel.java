package com.perso.T4C.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Non-interactive text label. Supports several semantic levels (H1–H3, body, muted)
 * that map to different colors and spacing, while sharing the same BitmapFont.
 *
 * Usage:
 * <pre>
 *   EditorLabel lbl = new EditorLabel("Clan Relations", EditorLabel.Style.H1)
 *       .withFont(font);
 *   lbl.setBounds(x, y, w, 24);
 * </pre>
 */
public class EditorLabel extends EditorComponent {

    public enum Style {
        H1,    // large title — TEXT_LIGHT, full opacity
        H2,    // section header — TEXT_LIGHT, slightly muted
        H3,    // sub-header — TEXT_FAINT
        BODY,  // normal body — TEXT
        MUTED, // secondary info — TEXT_MUTED
    }

    private String text;
    private Style style;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    public EditorLabel(String text, Style style) {
        this.text  = text;
        this.style = style;
    }

    public EditorLabel withFont(BitmapFont font) {
        this.font = font;
        return this;
    }

    public EditorLabel withText(String text) {
        this.text = text;
        return this;
    }

    public EditorLabel withStyle(Style style) {
        this.style = style;
        return this;
    }

    public String getText()  { return text; }
    public Style  getStyle() { return style; }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer sr) {
        if (!visible || font == null || text == null || text.isEmpty()) return;

        Color color = switch (style) {
            case H1    -> EditorTheme.TEXT_LIGHT;
            case H2    -> EditorTheme.TEXT_LIGHT;
            case H3    -> EditorTheme.TEXT_FAINT;
            case BODY  -> EditorTheme.TEXT;
            case MUTED -> EditorTheme.TEXT_MUTED;
        };

        float maxW = bounds.width;
        String fitted = EditorPanelChrome.fitText(font, layout, text, maxW);

        // Vertical centering within bounds
        layout.setText(font, fitted);
        float ty = bounds.y + (bounds.height + layout.height) * 0.5f;

        batch.begin();
        font.setColor(color);
        font.draw(batch, fitted, bounds.x, ty);
        batch.end();
    }
}
