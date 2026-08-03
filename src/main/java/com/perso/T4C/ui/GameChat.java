package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiResizable;
import com.perso.T4C.gui.core.GuiBoxedInteraction;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.helper.SpriteLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * In-game chat adapted from the original client's main-bar chat.
 * Enter opens/submits it, Escape closes it and Page Up/Down scroll the log.
 */
public final class GameChat extends InputAdapter {
    private static final int MAX_ENTRIES = 500;
    private static final int MAX_HISTORY = 50;
    private static final int MAX_TEXT_LENGTH = 256;
    private static final float BAR_WIDTH = 1024f;
    private static final float BAR_HEIGHT = 150f;
    private static final String GENERATED_BACKGROUND_PATH = "assets/ui/bottom-hud-frame-v1.png";
    private static final float LEFT_CAP_WIDTH = 8f;
    private static final float LOG_X = 14f;
    private static final float LOG_Y = 10f;
    private static final float LOG_WIDTH = 569f;
    private static final float LOG_HEIGHT = 60f;
    private static final float LOG_TEXT_INSET_Y = 4f;
    private static final float INPUT_X = 21f;
    private static final float INPUT_Y = 81f;
    private static final float INPUT_WIDTH = 557f;
    private static final float INPUT_HEIGHT = 17f;
    private static final float LINE_HEIGHT = 16f;
    private static final float SCROLL_THUMB_X = 639f;
    private static final float SCROLL_THUMB_TOP_Y = 28f;
    private static final float SCROLL_THUMB_BOTTOM_Y = 66f;
    private static final int WHEEL_SCROLL_LINES = 3;
    private static final Color SYSTEM = SystemMessage.MESSAGE_COLOR;
    private static final Color LOCAL = Color.valueOf("E6D8BC");

    private record Entry(String text, Color color) {}

    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final List<Entry> entries = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private final StringBuilder input = new StringBuilder();
    private final Predicate<String> submitHandler;
    private boolean active;
    private boolean visible = true;
    private boolean suppressNextTypedEnter;
    private int cursor;
    private int historyIndex;
    private int scroll;
    private int maximumScroll;
    private final GuiBoxedInteraction boxedInteraction = new GuiBoxedInteraction();
    private final ChatZone chatZone = new ChatZone();
    private final ChatZone inputZone = new ChatZone();
    private boolean chatLayoutInitialized;
    private Texture generatedBackground;

    public GameChat(Predicate<String> submitHandler) {
        this.submitHandler = submitHandler == null ? text -> false : submitHandler;
        font = FontManager.getInstance().getNpcDialogFont(LOCAL);
        historyIndex = history.size();
        if (Gdx.files.internal(GENERATED_BACKGROUND_PATH).exists()) {
            generatedBackground = new Texture(Gdx.files.internal(GENERATED_BACKGROUND_PATH));
            generatedBackground.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
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
        float scale = Math.min(1f, hudCamera.viewportWidth / BAR_WIDTH);
        float barWidth = BAR_WIDTH * scale;
        float x = (hudCamera.viewportWidth - barWidth) * 0.5f;
        float y = hudCamera.viewportHeight - BAR_HEIGHT * scale;
        if (!chatLayoutInitialized) {
            chatZone.setPosition(x + LOG_X * scale, y + LOG_Y * scale);
            chatZone.setSize(LOG_WIDTH * scale, LOG_HEIGHT * scale);
            inputZone.setPosition(x + INPUT_X * scale, y + INPUT_Y * scale);
            inputZone.setSize(INPUT_WIDTH * scale, INPUT_HEIGHT * scale);
            chatLayoutInitialized = true;
        }

        drawBackground(batch, x, y, scale);
        GuiBoxedItem.drawDebugBorder(batch, chatZone.getX(), chatZone.getY(), chatZone.getWidth(), chatZone.getHeight());
        GuiBoxedItem.drawDebugBorder(batch, inputZone.getX(), inputZone.getY(), inputZone.getWidth(), inputZone.getHeight());

        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        font.getData().setScale(oldScaleX * scale, oldScaleY * scale);
        try {
            float logX = chatZone.getX();
            float logY = chatZone.getY();
            float logWidth = chatZone.getWidth();
            float logHeight = chatZone.getHeight();
            List<Entry> lines = visualLines(logWidth);
            int visibleLines = Math.max(1, (int) (LOG_HEIGHT / LINE_HEIGHT));
            maximumScroll = Math.max(0, lines.size() - visibleLines);
            scroll = Math.max(0, Math.min(scroll, maximumScroll));
            int last = lines.size() - scroll;
            int first = Math.max(0, last - visibleLines);

            beginScissor(batch, logX, logY, logWidth, logHeight, hudCamera.viewportHeight);
            float textY = logY + LOG_TEXT_INSET_Y * scale;
            for (int i = first; i < last; i++) {
                Entry line = lines.get(i);
                font.setColor(line.color());
                font.draw(batch, line.text(), logX, textY);
                textY += LINE_HEIGHT * scale;
            }
            endScissor(batch);
            drawScrollThumb(batch, x, y, scale);

            if (active) {
                float inputX = inputZone.getX();
                float inputY = inputZone.getY();
                float inputWidth = inputZone.getWidth();
                beginScissor(batch, inputX, inputY, inputWidth,
                        INPUT_HEIGHT * scale, hudCamera.viewportHeight);
                String prompt = "> " + input;
                font.setColor(LOCAL);
                layout.setText(font, prompt);
                float inputTextY = inputY + (inputZone.getHeight() - layout.height) / 2f;
                font.draw(batch, prompt, inputX, inputTextY);
                if ((System.currentTimeMillis() / 350L & 1L) == 0L) {
                    String beforeCursor = "> " + input.substring(0, cursor);
                    layout.setText(font, beforeCursor);
                    font.draw(batch, "|", inputX + layout.width, inputTextY);
                }
                endScissor(batch);
            }
        } finally {
            font.getData().setScale(oldScaleX, oldScaleY);
            font.setColor(Color.WHITE);
        }
    }

    public boolean boxedTouchDown(float x, float y) { return boxedInteraction.touchDown(List.of(chatZone, inputZone), x, y); }
    public boolean boxedMouseMoved(float x, float y) { return boxedInteraction.dragged(x, y); }
    public boolean boxedTouchUp() { return boxedInteraction.touchUp(); }

    private static final class ChatZone implements GuiResizable {
        private float x, y, w, h;
        public void render(SpriteBatch b) { }
        public boolean contains(float sx, float sy) { return sx >= x && sx <= x + w && sy >= y && sy <= y + h; }
        public void setPosition(float x, float y) { this.x=x; this.y=y; }
        public float getX(){return x;} public float getY(){return y;}
        public GuiResizable setSize(float w,float h){this.w=Math.max(8,w);this.h=Math.max(8,h);return this;}
        public float getWidth(){return w;} public float getHeight(){return h;}
    }

    private void drawBackground(SpriteBatch batch, float x, float y, float scale) {
        if (generatedBackground != null) {
            GuiDraw.withOverlayAlpha(batch, () -> batch.draw(generatedBackground, x, y,
                    BAR_WIDTH * scale, BAR_HEIGHT * scale, 0, 0,
                    generatedBackground.getWidth(), generatedBackground.getHeight(), false, true));
            return;
        }
        SpriteLoader loader = SpriteLoader.getInstance();
        TextureRegion left = null;
        TextureRegion background = null;
        try {
            left = loader.getRegionFromSpriteName("GUI_backChatLeft");
            background = loader.getRegionFromSpriteName("GUI_backChat");
        } catch (GameException ignored) {
            // Text remains usable when the optional GUI sprites cannot be decoded.
        }
        if (left != null) {
            GuiDraw.drawOverlayRegionFlipped(batch, left, x, y,
                    LEFT_CAP_WIDTH * scale, BAR_HEIGHT * scale);
        }
        if (background != null) {
            GuiDraw.drawOverlayRegionFlipped(batch, background, x + LEFT_CAP_WIDTH * scale, y,
                    (BAR_WIDTH - LEFT_CAP_WIDTH) * scale, BAR_HEIGHT * scale);
        }
    }

    private void drawScrollThumb(SpriteBatch batch, float x, float y, float scale) {
        TextureRegion thumb;
        try {
            thumb = SpriteLoader.getInstance().getRegionFromSpriteName("GUI_ScrollTick");
        } catch (GameException ignored) {
            return;
        }
        if (thumb == null) return;
        float ratio = maximumScroll == 0 ? 0f : scroll / (float) maximumScroll;
        float thumbY = SCROLL_THUMB_BOTTOM_Y
                + (SCROLL_THUMB_TOP_Y - SCROLL_THUMB_BOTTOM_Y) * ratio;
        GuiDraw.drawRegionFlipped(batch, thumb,
                x + SCROLL_THUMB_X * scale, y + thumbY * scale,
                thumb.getRegionWidth() * scale, thumb.getRegionHeight() * scale);
        GuiBoxedItem.drawDebugBorder(batch,
                x + SCROLL_THUMB_X * scale,
                y + SCROLL_THUMB_TOP_Y * scale,
                thumb.getRegionWidth() * scale,
                (SCROLL_THUMB_BOTTOM_Y - SCROLL_THUMB_TOP_Y + thumb.getRegionHeight()) * scale);
    }

    private static void beginScissor(SpriteBatch batch, float x, float y, float width,
                                     float height, float screenHeight) {
        batch.flush();
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(
                Math.max(0, Math.round(x)),
                Math.max(0, Math.round(screenHeight - y - height)),
                Math.max(0, Math.round(width)),
                Math.max(0, Math.round(height)));
    }

    private static void endScissor(SpriteBatch batch) {
        batch.flush();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
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

    @Override
    public boolean scrolled(float amountX, float amountY) {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float scale = Math.min(1f, width / BAR_WIDTH);
        float x = (width - BAR_WIDTH * scale) * 0.5f;
        float y = height - BAR_HEIGHT * scale;
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        if (mouseX < x + LOG_X * scale
                || mouseX > x + (LOG_X + LOG_WIDTH) * scale
                || mouseY < y + LOG_Y * scale
                || mouseY > y + (LOG_Y + LOG_HEIGHT) * scale) {
            return false;
        }
        int direction = amountY < 0f ? 1 : amountY > 0f ? -1 : 0;
        if (direction != 0) {
            scroll = Math.max(0, Math.min(maximumScroll,
                    scroll + direction * WHEEL_SCROLL_LINES));
        }
        return true;
    }

    private static boolean isEnter(int keycode) {
        return keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER;
    }

    private void submit() {
        String text = input.toString().trim();
        if (!text.isEmpty()) {
            boolean consumed = submitHandler.test(text);
            if (!consumed) {
                addLocalMessage("Vous", text);
            }
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
        if (generatedBackground != null) generatedBackground.dispose();
    }
}
