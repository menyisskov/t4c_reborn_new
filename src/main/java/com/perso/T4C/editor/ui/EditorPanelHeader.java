package com.perso.T4C.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Panel header bar: dark background, accent stripe on top, title left-aligned,
 * optional status message below the title (muted, smaller).
 *
 * Matches the visual from EditorPanelChrome.panel() header region.
 *
 * Usage:
 * <pre>
 *   EditorPanelHeader header = new EditorPanelHeader("Clan Relations Editor")
 *       .accentColor(EditorTheme.ORANGE)
 *       .withFont(titleFont, statusFont);
 *   header.setBounds(panelX, panelY + panelH - HEADER_H, panelW, HEADER_H);
 *   header.setStatus("Source clan attacks target clan. Saved to clan_relations.bin.");
 * </pre>
 */
public class EditorPanelHeader extends EditorComponent {

    private static final float ACCENT_H   = 4f;
    private static final float PAD_X      = 14f;
    private static final float TITLE_FROM_TOP = 14f;
    private static final float STATUS_GAP = 18f;

    private String title;
    private String status = "";
    private Color accent = EditorTheme.BLUE;
    private BitmapFont titleFont;
    private BitmapFont statusFont;

    public EditorPanelHeader(String title) {
        this.title = title;
    }

    public EditorPanelHeader withTitle(String title) {
        this.title = title;
        return this;
    }

    public EditorPanelHeader accentColor(Color color) {
        this.accent = color;
        return this;
    }

    /** Set both title and status font to the same font. */
    public EditorPanelHeader withFont(BitmapFont font) {
        this.titleFont  = font;
        this.statusFont = font;
        return this;
    }

    /** Set title and status fonts independently. */
    public EditorPanelHeader withFont(BitmapFont titleFont, BitmapFont statusFont) {
        this.titleFont  = titleFont;
        this.statusFont = statusFont;
        return this;
    }

    /** Status line shown below the title (e.g. last operation result). Empty = hidden. */
    public void setStatus(String status) {
        this.status = status == null ? "" : status;
    }

    public String getStatus() { return status; }
    public String getTitle()  { return title; }

    // ── Rendering ─────────────────────────────────────────────────────────

    @Override
    public void render(SpriteBatch batch, ShapeRenderer sr) {
        if (!visible) return;

        // Background
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(EditorTheme.PANEL_DARK);
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        // Accent stripe at top
        sr.setColor(accent);
        sr.rect(bounds.x, bounds.y + bounds.height - ACCENT_H, bounds.width, ACCENT_H);
        sr.end();

        if (titleFont == null) return;

        float textTop = bounds.y + bounds.height - TITLE_FROM_TOP;

        batch.begin();
        // Title
        titleFont.setColor(EditorTheme.TEXT_LIGHT);
        titleFont.draw(batch, title, bounds.x + PAD_X, textTop);

        // Status (muted, below title)
        if (!status.isEmpty() && statusFont != null) {
            statusFont.setColor(EditorTheme.TEXT_MUTED);
            statusFont.draw(batch, status, bounds.x + PAD_X, textTop - STATUS_GAP);
        }
        batch.end();
    }
}
