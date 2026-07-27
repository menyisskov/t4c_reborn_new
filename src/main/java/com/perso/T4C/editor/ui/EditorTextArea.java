package com.perso.T4C.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-line text area control. Supports keyboard input, word-wrap, and vertical scrolling.
 * Lines are stored as a List&lt;StringBuilder&gt;. The whole content can be retrieved via
 * {@link #getText()} (lines joined with '\n') or manipulated per-line via {@link #lines()}.
 */
public class EditorTextArea extends EditorControl {

    private static final float PADDING    = 8f;
    private static final float LINE_GAP   = 4f;
    private static final float SCROLL_W   = 10f;

    private final List<StringBuilder> lines = new ArrayList<>();
    private BitmapFont font;
    @Getter @Setter private boolean active;
    private String placeholder = "";

    /** Current cursor position: line index and char index within that line. */
    private int cursorLine = 0;
    private int cursorCol  = 0;
    /** First visible line (scroll offset). */
    private int scrollLine = 0;

    private final GlyphLayout layout = new GlyphLayout();

    public EditorTextArea() {
        lines.add(new StringBuilder());
    }

    public EditorTextArea withFont(BitmapFont font) {
        this.font = font;
        return this;
    }

    public EditorTextArea placeholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        return this;
    }

    /** Returns all lines joined with '\n'. */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    /** Replaces the entire content (splits on '\n'). */
    public void setText(String text) {
        lines.clear();
        if (text == null || text.isEmpty()) {
            lines.add(new StringBuilder());
        } else {
            for (String line : text.split("\n", -1)) {
                lines.add(new StringBuilder(line));
            }
        }
        cursorLine = 0;
        cursorCol  = 0;
        scrollLine = 0;
    }

    public List<StringBuilder> lines() { return lines; }

    // ── Rendering ─────────────────────────────────────────────────────────

    @Override
    public void render(SpriteBatch batch, ShapeRenderer sr) {
        if (!visible) return;

        // Background + border
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(active ? EditorTheme.SURFACE_HOVER : EditorTheme.SURFACE);
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(active ? EditorTheme.BORDER_ACTIVE : EditorTheme.BORDER);
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        sr.end();

        if (font == null) return;

        float lineH = font.getLineHeight() + LINE_GAP;
        int visibleLines = Math.max(1, (int) ((bounds.height - PADDING * 2) / lineH));

        // Show placeholder when empty and inactive
        boolean isEmpty = lines.size() == 1 && lines.get(0).length() == 0;
        if (isEmpty && !active) {
            batch.begin();
            font.setColor(EditorTheme.TEXT_FAINT);
            font.draw(batch, placeholder,
                    bounds.x + PADDING,
                    bounds.y + bounds.height - PADDING);
            batch.end();
            return;
        }

        // Clamp scroll
        int maxScroll = Math.max(0, lines.size() - visibleLines);
        scrollLine = Math.min(scrollLine, maxScroll);

        float textMaxW = bounds.width - PADDING * 2 - SCROLL_W - 4f;
        float startY   = bounds.y + bounds.height - PADDING;

        batch.begin();
        for (int i = scrollLine; i < lines.size() && i < scrollLine + visibleLines; i++) {
            String lineText = lines.get(i).toString();
            String fitted   = EditorPanelChrome.fitText(font, layout, lineText, textMaxW);
            float  y        = startY - (i - scrollLine) * lineH;

            font.setColor(EditorTheme.TEXT);
            font.draw(batch, fitted, bounds.x + PADDING, y);

            // Cursor
            if (active && i == cursorLine) {
                int col = Math.min(cursorCol, lineText.length());
                String before = lineText.substring(0, col);
                layout.setText(font, before);
                float cx = bounds.x + PADDING + layout.width;
                float cy = y - font.getDescent();
                font.setColor(EditorTheme.TEXT);
                font.draw(batch, "|", cx, cy + font.getLineHeight());
            }
        }
        batch.end();

        // Scrollbar
        if (lines.size() > visibleLines) {
            float trackH = bounds.height - PADDING * 2;
            float thumbH = Math.max(16f, trackH * visibleLines / lines.size());
            float thumbY = bounds.y + PADDING + (trackH - thumbH)
                    * (1f - (float) scrollLine / maxScroll);
            float trackX = bounds.x + bounds.width - SCROLL_W - 2f;

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(EditorTheme.PANEL_DARK_2);
            sr.rect(trackX, bounds.y + PADDING, SCROLL_W, trackH);
            sr.setColor(EditorTheme.BORDER);
            sr.rect(trackX, thumbY, SCROLL_W, thumbH);
            sr.end();
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────

    @Override
    public boolean handleClick(int screenX, int screenY, int button) {
        if (!visible || !enabled) return false;
        active = bounds.contains(screenX, screenY);
        if (active && font != null) {
            // Move cursor to the clicked line
            float lineH  = font.getLineHeight() + LINE_GAP;
            float relY   = bounds.y + bounds.height - PADDING - screenY;
            int clickedLine = scrollLine + (int) (relY / lineH);
            clickedLine = Math.max(0, Math.min(clickedLine, lines.size() - 1));
            cursorLine  = clickedLine;
            cursorCol   = lines.get(cursorLine).length(); // end of line for simplicity
        }
        return active;
    }

    @Override
    public boolean handleScroll(float amount) {
        if (!visible || !active) return false;
        scrollLine = Math.max(0, scrollLine + (int) amount);
        return true;
    }

    @Override
    public boolean handleKeyDown(int keycode) {
        if (!visible || !active) return false;
        switch (keycode) {
            case Input.Keys.ENTER -> {
                // Split current line at cursor
                StringBuilder current = lines.get(cursorLine);
                String tail = current.substring(cursorCol);
                current.delete(cursorCol, current.length());
                lines.add(cursorLine + 1, new StringBuilder(tail));
                cursorLine++;
                cursorCol = 0;
                ensureCursorVisible();
                return true;
            }
            case Input.Keys.BACKSPACE -> {
                if (cursorCol > 0) {
                    lines.get(cursorLine).deleteCharAt(cursorCol - 1);
                    cursorCol--;
                } else if (cursorLine > 0) {
                    // Merge with previous line
                    StringBuilder prev = lines.get(cursorLine - 1);
                    int prevLen = prev.length();
                    prev.append(lines.get(cursorLine));
                    lines.remove(cursorLine);
                    cursorLine--;
                    cursorCol = prevLen;
                }
                ensureCursorVisible();
                return true;
            }
            case Input.Keys.FORWARD_DEL -> {
                StringBuilder cur = lines.get(cursorLine);
                if (cursorCol < cur.length()) {
                    cur.deleteCharAt(cursorCol);
                } else if (cursorLine < lines.size() - 1) {
                    cur.append(lines.get(cursorLine + 1));
                    lines.remove(cursorLine + 1);
                }
                return true;
            }
            case Input.Keys.LEFT -> {
                if (cursorCol > 0) {
                    cursorCol--;
                } else if (cursorLine > 0) {
                    cursorLine--;
                    cursorCol = lines.get(cursorLine).length();
                }
                ensureCursorVisible();
                return true;
            }
            case Input.Keys.RIGHT -> {
                StringBuilder cur = lines.get(cursorLine);
                if (cursorCol < cur.length()) {
                    cursorCol++;
                } else if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    cursorCol = 0;
                }
                ensureCursorVisible();
                return true;
            }
            case Input.Keys.UP -> {
                if (cursorLine > 0) {
                    cursorLine--;
                    cursorCol = Math.min(cursorCol, lines.get(cursorLine).length());
                }
                ensureCursorVisible();
                return true;
            }
            case Input.Keys.DOWN -> {
                if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    cursorCol = Math.min(cursorCol, lines.get(cursorLine).length());
                }
                ensureCursorVisible();
                return true;
            }
            case Input.Keys.HOME -> { cursorCol = 0; return true; }
            case Input.Keys.END  -> { cursorCol = lines.get(cursorLine).length(); return true; }
        }
        return false;
    }

    @Override
    public boolean handleKeyTyped(char character) {
        if (!visible || !active) return false;
        // Ignore control characters (handled by handleKeyDown)
        if (character < 32 || character == 127) return false;
        lines.get(cursorLine).insert(cursorCol, character);
        cursorCol++;
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void ensureCursorVisible() {
        if (font == null) return;
        float lineH = font.getLineHeight() + LINE_GAP;
        int visibleLines = Math.max(1, (int) ((bounds.height - PADDING * 2) / lineH));
        if (cursorLine < scrollLine) {
            scrollLine = cursorLine;
        } else if (cursorLine >= scrollLine + visibleLines) {
            scrollLine = cursorLine - visibleLines + 1;
        }
    }
}
