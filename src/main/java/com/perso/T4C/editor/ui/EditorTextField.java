package com.perso.T4C.editor.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.Getter;
import lombok.Setter;

public class EditorTextField extends EditorControl {
    @Getter
    private final StringBuilder text;
    private BitmapFont font;
    @Getter
    @Setter
    private boolean active;
    private String placeholder = "";
    private final GlyphLayout layout = new GlyphLayout();

    public EditorTextField(StringBuilder text) {
        this.text = text;
    }

    public EditorTextField withFont(BitmapFont font) {
        this.font = font;
        return this;
    }

    public EditorTextField placeholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        return this;
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        EditorPanelChrome.textField(sr, bounds, active);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        EditorPanelChrome.border(sr, bounds);
        sr.end();

        if (font == null) return;

        batch.begin();
        String display = text.length() > 0 ? text.toString() : placeholder;
        float maxW = bounds.width - 16f;
        font.setColor(text.length() > 0 ? EditorTheme.TEXT : EditorTheme.TEXT_FAINT);
        font.draw(batch, EditorPanelChrome.fitText(font, layout, display, maxW),
                bounds.x + 8f, bounds.y + bounds.height * 0.5f + 6f);
        batch.end();
    }

    @Override
    public boolean handleClick(int screenX, int screenY, int button) {
        if (!visible || !enabled) return false;
        boolean hit = bounds.contains(screenX, screenY);
        active = hit;
        return hit;
    }
}
