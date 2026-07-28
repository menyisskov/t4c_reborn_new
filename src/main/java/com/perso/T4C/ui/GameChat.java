package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * In-game chat adapted from the original client's main-bar chat.
 * Enter opens/submits it, Escape closes it and Page Up/Down scroll the log.
 */
public final class GameChat extends InputAdapter {
    private static final int MAX_ENTRIES = 500;
    private static final int MAX_HISTORY = 50;
    private static final int MAX_TEXT_LENGTH = 256;
    private static final float WIDTH = 482f;
    private static final float LOG_HEIGHT = 120f;
    private static final float INPUT_HEIGHT = 25f;
    private static final float MARGIN = 12f;
    private static final Color BACKGROUND = new Color(0.025f, 0.035f, 0.045f, 0.42f);
    private static final Color INPUT_BACKGROUND = new Color(0.04f, 0.055f, 0.07f, 0.58f);
    private static final Color BORDER = new Color(0.42f, 0.36f, 0.25f, 0.65f);
    private static final Color SYSTEM = Color.valueOf("8DE300");
    private static final Color LOCAL = Color.valueOf("E6D8BC");

    private record Entry(String text, Color color) {}

    private final BitmapFont font;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final GlyphLayout layout = new GlyphLayout();
    private final List<Entry> entries = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private final StringBuilder input = new StringBuilder();
    private final Consumer<String> submitHandler;
    private boolean active;
    private boolean visible = true;
    private boolean suppressNextTypedEnter;
    private int cursor;
    private int historyIndex;
    private int scroll;

    public GameChat(Consumer<String> submitHandler) {
        this.submitHandler = submitHandler == null ? text -> { } : submitHandler;
        font = FontManager.getInstance().getNpcDialogFont(LOCAL);
        historyIndex = history.size();
    }

    public boolean isActive() {
        return active;
    }

    public void addSystemMessage(String message) {
        add(message, SYSTEM);
    }

    public void addLocalMessage(String speaker, String message) {
        String prefix = speaker == null || speaker.isBlank() ? "" : speaker + " : ";
        add(prefix + message, LOCAL);
    }

    private void add(String message, Color color) {
        if (message == null || message.isBlank()) return;
        entries.add(new Entry(message, new Color(color)));
        if (entries.size() > MAX_ENTRIES) entries.remove(0);
        scroll = 0;
    }

    public void render(SpriteBatch batch, OrthographicCamera hudCamera) {
        if (!visible || batch == null || hudCamera == null) return;
        float screenHeight = hudCamera.viewportHeight;
        float x = Math.max(MARGIN, (hudCamera.viewportWidth - WIDTH) / 2f);
        float logY = Math.max(MARGIN, screenHeight - LOG_HEIGHT - INPUT_HEIGHT - MARGIN);
        float totalHeight = LOG_HEIGHT + (active ? INPUT_HEIGHT : 0f);

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(hudCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(BACKGROUND);
        shapes.rect(x, logY, WIDTH, LOG_HEIGHT);
        if (active) {
            shapes.setColor(INPUT_BACKGROUND);
            shapes.rect(x, logY + LOG_HEIGHT, WIDTH, INPUT_HEIGHT);
        }
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BORDER);
        shapes.rect(x, logY, WIDTH, totalHeight);
        if (active) shapes.line(x, logY + LOG_HEIGHT, x + WIDTH, logY + LOG_HEIGHT);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();

        List<Entry> lines = visualLines(WIDTH - 16f);
        int visibleLines = Math.max(1, (int) ((LOG_HEIGHT - 8f) / 16f));
        int maximumScroll = Math.max(0, lines.size() - visibleLines);
        scroll = Math.max(0, Math.min(scroll, maximumScroll));
        int last = lines.size() - scroll;
        int first = Math.max(0, last - visibleLines);
        float y = logY + 6f;
        for (int i = first; i < last; i++) {
            Entry line = lines.get(i);
            font.setColor(line.color());
            font.draw(batch, line.text(), x + 8f, y);
            y += 16f;
        }

        if (active) {
            String prompt = "> " + input;
            font.setColor(LOCAL);
            font.draw(batch, prompt, x + 8f, logY + LOG_HEIGHT + 5f);
            if ((System.currentTimeMillis() / 350L & 1L) == 0L) {
                String beforeCursor = "> " + input.substring(0, cursor);
                layout.setText(font, beforeCursor);
                font.draw(batch, "|", x + 8f + layout.width, logY + LOG_HEIGHT + 5f);
            }
        }
        font.setColor(Color.WHITE);
    }

    private List<Entry> visualLines(float width) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : entries) {
            layout.setText(font, entry.text(), entry.color(), width, Align.left, true);
            String[] wrapped = layout.runs.isEmpty()
                    ? new String[]{entry.text()}
                    : entry.text().split("\\R");
            if (wrapped.length == 1 && layout.height <= 17f) {
                result.add(entry);
                continue;
            }
            wrap(entry, width, result);
        }
        return result;
    }

    private void wrap(Entry entry, float width, List<Entry> output) {
        String remaining = entry.text();
        while (!remaining.isEmpty()) {
            int fit = remaining.length();
            while (fit > 1) {
                layout.setText(font, remaining.substring(0, fit));
                if (layout.width <= width) break;
                fit--;
            }
            if (fit < remaining.length()) {
                int space = remaining.lastIndexOf(' ', fit);
                if (space > 0) fit = space;
            }
            output.add(new Entry(remaining.substring(0, fit).stripTrailing(), entry.color()));
            remaining = remaining.substring(fit).stripLeading();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!active) {
            if (isEnter(keycode)) {
                active = true;
                visible = true;
                suppressNextTypedEnter = true;
                return true;
            }
            if (keycode == Input.Keys.PAGE_UP || keycode == Input.Keys.PAGE_DOWN) {
                visible = true;
                scroll = Math.max(0, scroll + (keycode == Input.Keys.PAGE_UP ? 3 : -3));
                return true;
            }
            return false;
        }
        switch (keycode) {
            case Input.Keys.ESCAPE -> active = false;
            case Input.Keys.LEFT -> cursor = Math.max(0, cursor - 1);
            case Input.Keys.RIGHT -> cursor = Math.min(input.length(), cursor + 1);
            case Input.Keys.HOME -> cursor = 0;
            case Input.Keys.END -> cursor = input.length();
            case Input.Keys.BACKSPACE -> {
                if (cursor > 0) input.deleteCharAt(--cursor);
            }
            case Input.Keys.FORWARD_DEL -> {
                if (cursor < input.length()) input.deleteCharAt(cursor);
            }
            case Input.Keys.UP -> moveHistory(-1);
            case Input.Keys.DOWN -> moveHistory(1);
            case Input.Keys.PAGE_UP -> scroll += 3;
            case Input.Keys.PAGE_DOWN -> scroll = Math.max(0, scroll - 3);
            default -> {
                if (isEnter(keycode)) {
                    suppressNextTypedEnter = true;
                    submit();
                }
            }
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        if (character == '\r' || character == '\n') {
            if (suppressNextTypedEnter) {
                suppressNextTypedEnter = false;
                return true;
            }
            if (!active) {
                active = true;
                visible = true;
            }
            return true;
        }
        if (!active) return false;
        if (character >= 32 && character != 127 && input.length() < MAX_TEXT_LENGTH) {
            input.insert(cursor++, character);
        }
        return true;
    }

    private static boolean isEnter(int keycode) {
        return keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER;
    }

    private void submit() {
        String text = input.toString().trim();
        if (!text.isEmpty()) {
            addLocalMessage("Vous", text);
            submitHandler.accept(text);
            if (history.isEmpty() || !history.get(history.size() - 1).equals(text)) {
                history.add(text);
                if (history.size() > MAX_HISTORY) history.remove(0);
            }
        }
        input.setLength(0);
        cursor = 0;
        historyIndex = history.size();
        active = false;
    }

    private void moveHistory(int direction) {
        if (history.isEmpty()) return;
        historyIndex = Math.max(0, Math.min(history.size(), historyIndex + direction));
        input.setLength(0);
        if (historyIndex < history.size()) input.append(history.get(historyIndex));
        cursor = input.length();
    }

    public void dispose() {
        shapes.dispose();
    }
}
