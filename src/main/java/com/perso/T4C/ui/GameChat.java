package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiBoxedInteraction;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;
import com.perso.T4C.helper.SpriteLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class GameChat extends InputAdapter {
  private static final int MAX_ENTRIES = 500;
  private static final int MAX_HISTORY = 50;
  private static final int MAX_TEXT_LENGTH = 256;
  private static final float BAR_WIDTH = 934f;
  private static final float BAR_HEIGHT = 166f;
  private static final String GENERATED_BACKGROUND_PATH = "assets/ui/bottom-hud-frame-v9.png";
  private static final float LEFT_CAP_WIDTH = 8f;
  private static final float LOG_X = 16f;
  private static final float LOG_Y = 9f;
  private static final float LOG_WIDTH = 569f;
  private static final float LOG_HEIGHT = 77f;
  private static final float LOG_TEXT_INSET_Y = 4f;
  private static final float INPUT_X = 21f;
  private static final float INPUT_Y = 97f;
  private static final float INPUT_WIDTH = 557f;
  private static final float INPUT_HEIGHT = 17f;
  private static final float LINE_HEIGHT = 16f;
  private static final float SCROLL_THUMB_X = 639f;
  private static final float SCROLL_THUMB_TOP_Y = 44f;
  private static final float SCROLL_THUMB_BOTTOM_Y = 82f;
  private static final int WHEEL_SCROLL_LINES = 3;
  private static final Color SYSTEM = SystemMessage.MESSAGE_COLOR;
  private static final Color LOCAL = Color.valueOf("E6D8BC");
  private static final Color SELECTION = Color.valueOf("3B73B9CC");
  private static final float INPUT_PADDING = 2f;
  private static final float KEY_REPEAT_DELAY = 0.38f;
  private static final float KEY_REPEAT_INTERVAL = 0.045f;

  private record Entry(String text, Color color) {}

  private final BitmapFont font;
  private final GlyphLayout layout = new GlyphLayout();
  private final List<Entry> entries = new ArrayList<>();
  private final List<String> history = new ArrayList<>();
  private final StringBuilder input = new StringBuilder();
  private final Predicate<String> submitHandler;
  private Function<String, List<String>> autocompleteProvider = ignored -> List.of();
  private String autocompleteCompletedText;
  private List<String> autocompleteMatches = List.of();
  private int autocompleteIndex;
  private boolean active;
  private boolean visible = true;
  private boolean suppressNextTypedEnter;
  private int cursor;
  private int selectionAnchor = -1;
  private String undoInput;
  private int undoCursor;
  private int undoSelectionAnchor;
  private boolean undoAvailable;
  private int historyIndex;
  private int scroll;
  private int maximumScroll;
  private final GuiBoxedInteraction boxedInteraction = new GuiBoxedInteraction();
  private final ChatZone chatZone = new ChatZone();
  private final ChatZone inputZone = new ChatZone();
  private Texture generatedBackground;
  private final Texture selectionTexture;
  private float inputScrollX;
  private int repeatedHorizontalKey = -1;
  private float keyRepeatCountdown;

  public GameChat(Predicate<String> submitHandler) {
    this.submitHandler = submitHandler == null ? text -> false : submitHandler;
    font = FontManager.getInstance().getNpcDialogFont(LOCAL);
    Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixel.setColor(Color.WHITE);
    pixel.fill();
    selectionTexture = new Texture(pixel);
    pixel.dispose();
    historyIndex = history.size();
    if (Gdx.files.internal(GENERATED_BACKGROUND_PATH).exists()) {
      generatedBackground = new Texture(Gdx.files.internal(GENERATED_BACKGROUND_PATH));
      generatedBackground.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }
  }

  public void setAutocompleteProvider(Function<String, List<String>> provider) {
    autocompleteProvider = provider == null ? ignored -> List.of() : provider;
  }

  public boolean isActive() {
    return active;
  }

  public void addSystemMessage(String message) {
    add(message, SYSTEM);
    appendLog(message);
  }

  public void addLocalMessage(String speaker, String message) {
    String prefix = speaker == null || speaker.isBlank() ? "" : speaker + " : ";
    add(prefix + message, LOCAL);
    if (com.perso.T4C.config.GamePreferencesStore.get().isLogPlayerMessages()) {
      appendLog(prefix + message);
    }
  }

  public void addNpcMessage(String speaker, String message) {
    String prefix = speaker == null || speaker.isBlank() ? "" : speaker + " : ";
    add(prefix + message, SYSTEM);
    if (com.perso.T4C.config.GamePreferencesStore.get().isLogNpcMessages()) {
      appendLog(prefix + message);
    }
  }

  private static void appendLog(String message) {
    var preferences = com.perso.T4C.config.GamePreferencesStore.get();
    if (!preferences.isChatLogging() || message == null || message.isBlank()) return;
    try {
      Path path =
          Path.of(System.getProperty("user.dir"), preferences.getChatLogFilename()).normalize();
      Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
      if (!path.toAbsolutePath().startsWith(root)) return;
      String line =
          "["
              + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              + "] "
              + message
              + System.lineSeparator();
      Files.writeString(
          path, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (Exception ignored) {
    }
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
    chatZone.setPosition(x + LOG_X * scale, y + LOG_Y * scale);
    chatZone.setSize(LOG_WIDTH * scale, LOG_HEIGHT * scale);
    inputZone.setPosition(x + INPUT_X * scale, y + INPUT_Y * scale);
    inputZone.setSize(INPUT_WIDTH * scale, INPUT_HEIGHT * scale);
    drawBackground(batch, x, y, scale);
    GuiBoxedItem.drawDebugBorder(
        batch, chatZone.getX(), chatZone.getY(), chatZone.getWidth(), chatZone.getHeight());
    GuiBoxedItem.drawDebugBorder(
        batch, inputZone.getX(), inputZone.getY(), inputZone.getWidth(), inputZone.getHeight());
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
        updateHorizontalKeyRepeat();
        normalizeEditorState();
        float inputX = inputZone.getX();
        float inputY = inputZone.getY();
        float inputWidth = inputZone.getWidth();
        beginScissor(
            batch, inputX, inputY, inputWidth, INPUT_HEIGHT * scale, hudCamera.viewportHeight);
        String prompt = "> " + input;
        font.setColor(LOCAL);
        layout.setText(font, prompt);
        float inputTextY = inputY + (inputZone.getHeight() - layout.height) / 2f;
        keepCursorVisible(scale, inputWidth);
        float textX = inputX + INPUT_PADDING * scale - inputScrollX;
        drawSelection(batch, textX, inputTextY, scale);
        font.draw(batch, prompt, textX, inputTextY);
        if ((System.currentTimeMillis() / 350L & 1L) == 0L) {
          String beforeCursor = "> " + input.substring(0, cursor);
          layout.setText(font, beforeCursor);
          font.draw(batch, "|", textX + layout.width, inputTextY);
        }
        endScissor(batch);
      }
    } finally {
      font.getData().setScale(oldScaleX, oldScaleY);
      font.setColor(Color.WHITE);
    }
  }

  public boolean boxedTouchDown(float x, float y) {
    return boxedInteraction.touchDown(List.of(chatZone, inputZone), x, y);
  }

  public boolean boxedMouseMoved(float x, float y) {
    return boxedInteraction.dragged(x, y);
  }

  public boolean boxedTouchUp() {
    return boxedInteraction.touchUp();
  }

  public boolean touchDown(float screenX, float screenY) {
    float x = screenX;
    float y = screenY;
    if (!active || !inputZone.contains(x, y)) return false;
    cursor = cursorAt(x);
    selectionAnchor = cursor;
    return true;
  }

  public boolean touchDragged(float screenX, float screenY) {
    if (selectionAnchor < 0) return false;
    cursor = cursorAt(screenX);
    return true;
  }

  public boolean touchUp() {
    if (selectionAnchor < 0) return false;
    if (selectionAnchor == cursor) selectionAnchor = -1;
    return true;
  }

  private int cursorAt(float x) {
    float scale = Math.min(1f, Gdx.graphics.getWidth() / BAR_WIDTH);
    float localX = Math.max(0, x - inputZone.getX() - INPUT_PADDING * scale + inputScrollX);
    float best = Float.MAX_VALUE;
    int result = 0;
    for (int i = 0; i <= input.length(); i++) {
      layout.setText(font, "> " + input.substring(0, i));
      float distance = Math.abs(layout.width * scale - localX);
      if (distance < best) {
        best = distance;
        result = i;
      }
    }
    return result;
  }

  private void keepCursorVisible(float scale, float inputWidth) {
    layout.setText(font, "> " + input.substring(0, cursor));
    float cursorX = layout.width;
    float usableWidth = Math.max(1f, inputWidth - INPUT_PADDING * 2f * scale);
    float margin = 4f * scale;
    if (cursorX - inputScrollX > usableWidth - margin) {
      inputScrollX = cursorX - usableWidth + margin;
    } else if (cursorX - inputScrollX < margin) {
      inputScrollX = Math.max(0f, cursorX - margin);
    }
    layout.setText(font, "> " + input);
    inputScrollX = Math.min(inputScrollX, Math.max(0f, layout.width - usableWidth));
  }

  private void drawSelection(SpriteBatch batch, float textX, float textY, float scale) {
    normalizeEditorState();
    int start = selectionStart();
    int end = selectionEnd();
    if (start == end) return;
    layout.setText(font, "> " + input.substring(0, start));
    float startX = layout.width;
    layout.setText(font, input.substring(start, end));
    float width = Math.max(scale, layout.width);
    Color oldColor = new Color(batch.getColor());
    batch.setColor(SELECTION);
    batch.draw(
        selectionTexture,
        textX + startX,
        inputZone.getY() + 1f * scale,
        width,
        Math.max(1f, inputZone.getHeight() - 2f * scale));
    batch.setColor(oldColor);
  }

  private int selectionStart() {
    int safeCursor = clampTextIndex(cursor);
    return selectionAnchor < 0 ? safeCursor : Math.min(clampTextIndex(selectionAnchor), safeCursor);
  }

  private int selectionEnd() {
    int safeCursor = clampTextIndex(cursor);
    return selectionAnchor < 0 ? safeCursor : Math.max(clampTextIndex(selectionAnchor), safeCursor);
  }

  private int clampTextIndex(int index) {
    return Math.max(0, Math.min(index, input.length()));
  }

  private void normalizeEditorState() {
    cursor = clampTextIndex(cursor);
    if (selectionAnchor >= 0) selectionAnchor = clampTextIndex(selectionAnchor);
  }

  private void clearSelection() {
    selectionAnchor = -1;
  }

  private void deleteSelection() {
    int start = selectionStart(), end = selectionEnd();
    if (start != end) {
      input.delete(start, end);
      cursor = start;
      clearSelection();
    }
  }

  private static final class ChatZone implements GuiResizable {
    private float x, y, w, h;

    public void render(SpriteBatch b) {}

    public boolean contains(float sx, float sy) {
      return sx >= x && sx <= x + w && sy >= y && sy <= y + h;
    }

    public void setPosition(float x, float y) {
      this.x = x;
      this.y = y;
    }

    public float getX() {
      return x;
    }

    public float getY() {
      return y;
    }

    public GuiResizable setSize(float w, float h) {
      this.w = Math.max(8, w);
      this.h = Math.max(8, h);
      return this;
    }

    public float getWidth() {
      return w;
    }

    public float getHeight() {
      return h;
    }
  }

  private void drawBackground(SpriteBatch batch, float x, float y, float scale) {
    if (generatedBackground != null) {
      GuiDraw.withOverlayAlpha(
          batch,
          () ->
              batch.draw(
                  generatedBackground,
                  x,
                  y,
                  BAR_WIDTH * scale,
                  BAR_HEIGHT * scale,
                  0,
                  0,
                  generatedBackground.getWidth(),
                  generatedBackground.getHeight(),
                  false,
                  true));
      return;
    }
    SpriteLoader loader = SpriteLoader.getInstance();
    TextureRegion left = null;
    TextureRegion background = null;
    try {
      left = loader.getRegionFromSpriteName("GUI_backChatLeft");
      background = loader.getRegionFromSpriteName("GUI_backChat");
    } catch (GameException ignored) {
    }
    if (left != null) {
      GuiDraw.drawOverlayRegionFlipped(
          batch, left, x, y, LEFT_CAP_WIDTH * scale, BAR_HEIGHT * scale);
    }
    if (background != null) {
      GuiDraw.drawOverlayRegionFlipped(
          batch,
          background,
          x + LEFT_CAP_WIDTH * scale,
          y,
          (BAR_WIDTH - LEFT_CAP_WIDTH) * scale,
          BAR_HEIGHT * scale);
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
    float thumbY = SCROLL_THUMB_BOTTOM_Y + (SCROLL_THUMB_TOP_Y - SCROLL_THUMB_BOTTOM_Y) * ratio;
    GuiDraw.drawRegionFlipped(
        batch,
        thumb,
        x + SCROLL_THUMB_X * scale,
        y + thumbY * scale,
        thumb.getRegionWidth() * scale,
        thumb.getRegionHeight() * scale);
    GuiBoxedItem.drawDebugBorder(
        batch,
        x + SCROLL_THUMB_X * scale,
        y + SCROLL_THUMB_TOP_Y * scale,
        thumb.getRegionWidth() * scale,
        (SCROLL_THUMB_BOTTOM_Y - SCROLL_THUMB_TOP_Y + thumb.getRegionHeight()) * scale);
  }

  private static void beginScissor(
      SpriteBatch batch, float x, float y, float width, float height, float screenHeight) {
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
      for (String paragraph : entry.text().split("\\R")) {
        Entry line = new Entry(paragraph, entry.color());
        layout.setText(font, paragraph);
        if (layout.width <= width) result.add(line);
        else wrap(line, width, result);
      }
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
      // Chat sits ahead of the GUI in the input chain; an open window that uses Enter or
      // Page Up/Down itself (fast travel, spell book, storage search) gets those keys first.
      if ((isEnter(keycode) || keycode == Input.Keys.PAGE_UP || keycode == Input.Keys.PAGE_DOWN)
          && com.perso.T4C.gui.core.GuiManager.isOpen()
          && com.perso.T4C.gui.core.GuiManager.onKeyDown(keycode)) {
        return true;
      }
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
    normalizeEditorState();
    boolean shift =
        Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    boolean ctrl =
        Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    if (ctrl && keycode == Input.Keys.Z) {
      undo();
      return true;
    }
    if (ctrl && keycode == Input.Keys.A) {
      selectionAnchor = 0;
      cursor = input.length();
      return true;
    }
    if (ctrl && keycode == Input.Keys.C) {
      copySelection();
      return true;
    }
    if (ctrl && keycode == Input.Keys.X) {
      if (hasSelection()) {
        saveUndo();
        copySelection();
        deleteSelection();
      }
      return true;
    }
    if (ctrl && keycode == Input.Keys.V) {
      pasteClipboard();
      return true;
    }
    if ((keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT)
        && repeatedHorizontalKey == keycode) return true;
    if (shift
        && (keycode == Input.Keys.LEFT
            || keycode == Input.Keys.RIGHT
            || keycode == Input.Keys.HOME
            || keycode == Input.Keys.END)
        && selectionAnchor < 0) {
      selectionAnchor = cursor;
    }
    if (keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
      beginHorizontalKeyRepeat(keycode);
    }
    if (keycode == Input.Keys.BACKSPACE) {
      beginHorizontalKeyRepeat(keycode);
    }
    switch (keycode) {
      case Input.Keys.TAB -> autocomplete();
      case Input.Keys.ESCAPE -> {
        active = false;
        repeatedHorizontalKey = -1;
        clearSelection();
      }
      case Input.Keys.LEFT -> cursor = ctrl ? previousWord(cursor) : Math.max(0, cursor - 1);
      case Input.Keys.RIGHT ->
          cursor = ctrl ? nextWord(cursor) : Math.min(input.length(), cursor + 1);
      case Input.Keys.HOME -> cursor = 0;
      case Input.Keys.END -> cursor = input.length();
      case Input.Keys.BACKSPACE -> {
        saveUndo();
        if (hasSelection()) deleteSelection();
        else if (cursor > 0) {
          int start = ctrl ? previousWord(cursor) : cursor - 1;
          input.delete(start, cursor);
          cursor = start;
        }
      }
      case Input.Keys.FORWARD_DEL -> {
        saveUndo();
        if (hasSelection()) deleteSelection();
        else if (cursor < input.length()) {
          int end = ctrl ? nextWord(cursor) : cursor + 1;
          input.delete(cursor, end);
        }
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
    if (!shift
        && (keycode == Input.Keys.LEFT
            || keycode == Input.Keys.RIGHT
            || keycode == Input.Keys.HOME
            || keycode == Input.Keys.END)) clearSelection();
    return true;
  }

  private void autocomplete() {
    if (cursor != input.length() || hasSelection()) return;
    String text = input.toString();
    java.util.regex.Matcher matcher =
        java.util.regex.Pattern.compile(
                "^\\.summon\\s+(npc|monster)\\s+(.+)$", java.util.regex.Pattern.CASE_INSENSITIVE)
            .matcher(text);
    if (!matcher.matches()) return;
    String prefix = matcher.group(2);
    if (!text.equals(autocompleteCompletedText)) {
      List<String> candidates = autocompleteProvider.apply(matcher.group(1));
      if (candidates == null) return;
      String lower = prefix.toLowerCase(java.util.Locale.ROOT);
      autocompleteMatches =
          candidates.stream()
              .filter(
                  match ->
                      match != null && match.toLowerCase(java.util.Locale.ROOT).startsWith(lower))
              .distinct()
              .toList();
      autocompleteIndex = 0;
    } else if (autocompleteMatches.isEmpty()) {
      return;
    } else {
      autocompleteIndex = (autocompleteIndex + 1) % autocompleteMatches.size();
    }
    if (autocompleteMatches.isEmpty()) return;
    String match = autocompleteMatches.get(autocompleteIndex);
    saveUndo();
    input.replace(text.length() - prefix.length(), text.length(), match);
    cursor = input.length();
    autocompleteCompletedText = input.toString();
  }

  @Override
  public boolean keyUp(int keycode) {
    if (keycode == repeatedHorizontalKey) repeatedHorizontalKey = -1;
    return active
        && (keycode == Input.Keys.LEFT
            || keycode == Input.Keys.RIGHT
            || keycode == Input.Keys.BACKSPACE);
  }

  private void beginHorizontalKeyRepeat(int keycode) {
    if (repeatedHorizontalKey != keycode) {
      repeatedHorizontalKey = keycode;
      keyRepeatCountdown = KEY_REPEAT_DELAY;
    }
  }

  private void updateHorizontalKeyRepeat() {
    if (repeatedHorizontalKey < 0 || !Gdx.input.isKeyPressed(repeatedHorizontalKey)) {
      repeatedHorizontalKey = -1;
      return;
    }
    keyRepeatCountdown -= Math.min(Gdx.graphics.getDeltaTime(), 0.1f);
    while (keyRepeatCountdown <= 0f) {
      if (repeatedHorizontalKey == Input.Keys.BACKSPACE) {
        if (cursor > 0 && !hasSelection()) {
          saveUndo();
          input.deleteCharAt(--cursor);
        }
        keyRepeatCountdown += KEY_REPEAT_INTERVAL;
        continue;
      }
      boolean shift =
          Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
              || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
      boolean ctrl =
          Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
              || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
      if (shift && selectionAnchor < 0) selectionAnchor = cursor;
      cursor =
          repeatedHorizontalKey == Input.Keys.LEFT
              ? (ctrl ? previousWord(cursor) : Math.max(0, cursor - 1))
              : (ctrl ? nextWord(cursor) : Math.min(input.length(), cursor + 1));
      if (!shift) clearSelection();
      keyRepeatCountdown += KEY_REPEAT_INTERVAL;
    }
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
    normalizeEditorState();
    if (character >= 32
        && character != 127
        && (input.length() < MAX_TEXT_LENGTH || hasSelection())) {
      saveUndo();
      deleteSelection();
      if (input.length() < MAX_TEXT_LENGTH) input.insert(cursor++, character);
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
      scroll = Math.max(0, Math.min(maximumScroll, scroll + direction * WHEEL_SCROLL_LINES));
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
    inputScrollX = 0f;
    clearSelection();
    historyIndex = history.size();
    active = false;
    repeatedHorizontalKey = -1;
  }

  private void moveHistory(int direction) {
    if (history.isEmpty()) return;
    historyIndex = Math.max(0, Math.min(history.size(), historyIndex + direction));
    input.setLength(0);
    if (historyIndex < history.size()) input.append(history.get(historyIndex));
    cursor = input.length();
    clearSelection();
  }

  private void copySelection() {
    if (hasSelection())
      Gdx.app.getClipboard().setContents(input.substring(selectionStart(), selectionEnd()));
  }

  private boolean hasSelection() {
    return selectionStart() != selectionEnd();
  }

  private int previousWord(int from) {
    int position = clampTextIndex(from);
    while (position > 0 && Character.isWhitespace(input.charAt(position - 1))) position--;
    while (position > 0 && !Character.isWhitespace(input.charAt(position - 1))) position--;
    return position;
  }

  private int nextWord(int from) {
    int position = clampTextIndex(from);
    while (position < input.length() && !Character.isWhitespace(input.charAt(position))) position++;
    while (position < input.length() && Character.isWhitespace(input.charAt(position))) position++;
    return position;
  }

  private void pasteClipboard() {
    String pasted = Gdx.app.getClipboard().getContents();
    if (pasted == null || pasted.isEmpty()) return;
    pasted = pasted.replaceAll("[\\r\\n]", " ");
    saveUndo();
    deleteSelection();
    int count = Math.min(pasted.length(), MAX_TEXT_LENGTH - input.length());
    input.insert(cursor, pasted, 0, count);
    cursor += count;
  }

  private void saveUndo() {
    undoInput = input.toString();
    undoCursor = cursor;
    undoSelectionAnchor = selectionAnchor;
    undoAvailable = true;
  }

  private void undo() {
    if (!undoAvailable) return;
    input.setLength(0);
    input.append(undoInput);
    cursor = undoCursor;
    selectionAnchor = undoSelectionAnchor;
    normalizeEditorState();
    undoAvailable = false;
  }

  public void dispose() {
    if (generatedBackground != null) generatedBackground.dispose();
    selectionTexture.dispose();
  }
}
