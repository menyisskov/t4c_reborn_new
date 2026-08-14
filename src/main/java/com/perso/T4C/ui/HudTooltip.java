package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Shared HUD tooltip renderer using the game's black popup style.
 */
public class HudTooltip {
    private static final float PADDING = 7f;
    private static final float MAX_TEXT_WIDTH = 360f;
    private static final long DEFAULT_DURATION_MILLIS = 5000L;
    private static final float CURSOR_OFFSET_X = 18f;
    private static final float CURSOR_OFFSET_Y = 18f;

    private final BitmapFont font;
    private final Texture background;
    private final GlyphLayout layout = new GlyphLayout();

    private String text;
    private float x;
    private float y;
    private long untilMillis;

    public HudTooltip() {
        this.font = new BitmapFont();
        this.font.getData().setScale(1f, -1f);
        this.font.setUseIntegerPositions(false);
        this.font.setColor(Color.WHITE);
        FontManager.getInstance().registerExternalFont(this.font);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.background = new Texture(pixmap);
        pixmap.dispose();
    }

    public Texture getBackground() {
        return background;
    }

    public void show(String text, float screenX, float screenY) {
        show(text, screenX, screenY, DEFAULT_DURATION_MILLIS);
    }

    public void show(String text, float screenX, float screenY, long durationMillis) {
        if (text == null || text.isEmpty()) {
            clear();
            return;
        }
        this.text = text;
        this.x = screenX + CURSOR_OFFSET_X;
        this.y = screenY + CURSOR_OFFSET_Y;
        this.untilMillis = TimeUtils.millis() + durationMillis;
    }

    public boolean isVisible() {
        return text != null && TimeUtils.millis() <= untilMillis;
    }

    public void clear() {
        text = null;
        untilMillis = 0L;
    }

    public void render(SpriteBatch batch) {
        if (!isVisible()) {
            clear();
            return;
        }
        render(batch, text);
    }

    public void render(SpriteBatch batch, String overrideText) {
        if (!isVisible() || overrideText == null || overrideText.isEmpty()) {
            clear();
            return;
        }
        layout.setText(font, overrideText, Color.WHITE, MAX_TEXT_WIDTH, Align.left, true);
        float boxW = layout.width + PADDING * 2f;
        float textH = Math.max(Math.abs(layout.height),
                Math.max(1, layout.runs.size) * Math.abs(font.getLineHeight()));
        float boxH = textH + PADDING * 2f;
        float boxX = Math.min(x, Gdx.graphics.getWidth() - boxW - 2f);
        float boxY = Math.min(y, Gdx.graphics.getHeight() - boxH - 2f);

        Color previousBatchColor = new Color(batch.getColor());
        batch.setColor(0f, 0f, 0f, 1f);
        batch.draw(background, boxX, boxY, boxW, boxH);
        batch.setColor(previousBatchColor);
        font.draw(batch, layout, boxX + PADDING, boxY + PADDING);
    }

    public void dispose() {
        FontManager.getInstance().unregisterExternalFont(font);
        font.dispose();
        background.dispose();
    }
}
