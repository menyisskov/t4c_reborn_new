package com.perso.T4C.editor.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * Checkbox control with an optional text label rendered to its right.
 *
 * Usage:
 * <pre>
 *   EditorCheckbox chk = new EditorCheckbox("Activer", false)
 *       .withFont(font)
 *       .onChange(v -> this.enabled = v);
 *   chk.setBounds(x, y, 160, 22);
 * </pre>
 *
 * The box itself is always square ({@code bounds.height × bounds.height});
 * the label is drawn to the right of the box.
 */
public class EditorCheckbox extends EditorControl {

    private static final float BOX_PADDING = 3f;

    private String label;
    @Getter private boolean checked;
    private BitmapFont font;
    private Consumer<Boolean> onChange;

    private final Rectangle boxBounds = new Rectangle();

    public EditorCheckbox(String label, boolean initialValue) {
        this.label   = label;
        this.checked = initialValue;
    }

    public EditorCheckbox withFont(BitmapFont font) {
        this.font = font;
        return this;
    }

    public EditorCheckbox onChange(Consumer<Boolean> consumer) {
        this.onChange = consumer;
        return this;
    }

    public EditorCheckbox setChecked(boolean value) {
        this.checked = value;
        return this;
    }

    public EditorCheckbox withLabel(String label) {
        this.label = label;
        return this;
    }

    // ── Rendering ─────────────────────────────────────────────────────────

    @Override
    public void render(SpriteBatch batch, ShapeRenderer sr) {
        if (!visible) return;

        float boxSize = bounds.height;
        boxBounds.set(bounds.x, bounds.y, boxSize, boxSize);

        // Box fill + check mark via EditorPanelChrome
        sr.begin(ShapeRenderer.ShapeType.Filled);
        EditorPanelChrome.checkbox(sr, boxBounds, checked);
        sr.end();

        // Box border
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(hovered ? EditorTheme.BORDER_ACTIVE : EditorTheme.BORDER);
        sr.rect(boxBounds.x, boxBounds.y, boxBounds.width, boxBounds.height);
        sr.end();

        // Check mark (✓) drawn as lines when checked
        if (checked) {
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(EditorTheme.TEXT_LIGHT);
            float cx = boxBounds.x + BOX_PADDING;
            float cy = boxBounds.y + BOX_PADDING;
            float cw = boxBounds.width  - BOX_PADDING * 2;
            float ch = boxBounds.height - BOX_PADDING * 2;
            // Down-left stroke
            sr.line(cx, cy + ch * 0.5f, cx + cw * 0.35f, cy);
            // Up-right stroke
            sr.line(cx + cw * 0.35f, cy, cx + cw, cy + ch);
            sr.end();
        }

        // Label
        if (font != null && label != null && !label.isEmpty()) {
            batch.begin();
            font.setColor(enabled ? EditorTheme.TEXT : EditorTheme.TEXT_MUTED);
            float labelX = boxBounds.x + boxSize + 8f;
            float labelY = boxBounds.y + boxBounds.height * 0.5f + font.getCapHeight() * 0.5f;
            font.draw(batch, label, labelX, labelY);
            batch.end();
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────

    @Override
    public boolean handleClick(int screenX, int screenY, int button) {
        if (!visible || !enabled) return false;
        if (!bounds.contains(screenX, screenY)) return false;
        checked = !checked;
        if (onChange != null) onChange.accept(checked);
        return true;
    }
}
