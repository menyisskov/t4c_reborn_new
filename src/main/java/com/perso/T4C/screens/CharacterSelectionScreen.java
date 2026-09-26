package com.perso.T4C.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.perso.T4C.MyGame;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.CharacterCreationRules;
import com.perso.T4C.helper.LocalCharacterStore;
import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class CharacterSelectionScreen extends InputAdapter implements Screen {
  private static final float PANEL_ALPHA = 180f / 255f;
  private static final int VISIBLE_ROWS = 4;
  private static final long DOUBLE_CLICK_MS = 400L;
  private static final float CARET_BLINK_SECONDS = 0.5f;
  private static final float HOVER_ALPHA = 0.45f;

  // Class-picker layout, in Q_Back's own 628x384 panel space (y grows downwards).
  private static final float CLASS_TITLE_Y = 34f;
  private static final float CLASS_LIST_X = 18f;
  private static final float CLASS_LIST_Y = 60f;
  private static final float CLASS_ROW_PITCH = 30f;
  private static final float CLASS_ROW_WIDTH = 256f;
  private static final float CLASS_ROW_HEIGHT = 23f;
  private static final float PREVIEW_CENTER_X = 344f;
  private static final float PREVIEW_FEET_Y = 262f;
  private static final float STATS_X = 440f;
  private static final float STATS_VALUE_RIGHT = 606f;
  private static final float STATS_Y = 72f;
  private static final float STATS_ROW_PITCH = 22f;
  private static final float CLASS_DESCRIPTION_Y = 292f;
  private static final float CLASS_DESCRIPTION_WIDTH = 460f;
  private static final float CLASS_ERROR_Y = 348f;
  private static final float CLASS_BUTTON_X = 494f;
  private static final float CLASS_CREATE_Y = 270f;
  private static final float CLASS_REROLL_Y = 302f;
  private static final float CLASS_BACK_Y = 334f;
  private static final float BUTTON_WIDTH = 116f;
  private static final float BUTTON_HEIGHT = 27f;
  private static final float PANEL_INSET = 14f;
  /** hintBand is only 60% opaque; stacking it hides the panel art underneath. */
  private static final int SCRIM_PASSES = 3;
  private final MyGame game;
  private final SpriteBatch batch;
  private final OrthographicCamera camera = new OrthographicCamera();
  private final ScreenViewport viewport;
  private final Vector2 pointer = new Vector2();
  private final GlyphLayout layout = new GlyphLayout();
  private final Random random = new Random();
  private final TextureRegion background;
  private final TextureRegion title;
  private final TextureRegion panel;
  private final TextureRegion panelExtension;
  private final TextureRegion largeNormal;
  private final TextureRegion largeHover;
  private final TextureRegion largeDisabled;
  private final TextureRegion smallNormal;
  private final TextureRegion smallHover;
  private final TextureRegion rowHighlight;
  private final TextureRegion upNormal;
  private final TextureRegion upHover;
  private final TextureRegion downNormal;
  private final TextureRegion downHover;
  private final TextureRegion questionPanel;
  private final BitmapFont buttonFont;
  private final BitmapFont smallButtonFont;
  private final BitmapFont listFont;
  private final BitmapFont goldFont;
  private final BitmapFont whiteFont;
  private final BitmapFont redFont;
  private final BitmapFont grayFont;
  private final BitmapFont questionFont;
  private final BitmapFont hintFont;
  private final Texture hintBand;
  private List<LocalCharacterStore.CharacterSlot> characters = List.of();
  private List<Summary> summaries = List.of();
  private int lastClickedIndex = -1;
  private long lastClickAtMs;
  private float elapsed;
  private int selected;
  private int firstVisible;
  private Mode mode = Mode.SELECT;
  private String pendingName = "";
  private String pendingGender = LocalCharacterStore.MALE;
  private String errorMessage;
  private int selectedClass;
  private CharacterCreationRules.Stats rolledStats;
  private final CharacterPreview preview = new CharacterPreview();

  public CharacterSelectionScreen(MyGame game) {
    this.game = game;
    this.batch = game.batch;
    camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    viewport = new ScreenViewport(camera);
    background = loadSprite("Back01_1280");
    title = loadSprite("Connect_Title2");
    panel = loadSprite("PS_Back");
    panelExtension = loadSprite("PS_BackDown");
    largeNormal = loadSprite("PS_BtnN");
    largeHover = loadSprite("PS_BtnH");
    largeDisabled = loadSprite("PS_BtnD");
    smallNormal = loadSprite("PS_SmallBtnN");
    smallHover = loadSprite("PS_SmallBtnH");
    rowHighlight = loadSprite("PS_Over");
    upNormal = loadSprite("PS_SBtnUPN");
    upHover = loadSprite("PS_SBtnUPH");
    downNormal = loadSprite("PS_SBtnDNN");
    downHover = loadSprite("PS_SBtnDNH");
    questionPanel = loadSprite("Q_Back");
    FontManager fonts = FontManager.getInstance();
    buttonFont = fonts.getT4CBeaulieuFont(17, Color.BLACK);
    smallButtonFont = fonts.getT4CBeaulieuFont(15, Color.BLACK);
    listFont = fonts.getT4CBeaulieuFont(17, Color.WHITE);
    goldFont = fonts.getT4CBeaulieuFont(17, new Color(222 / 255f, 158 / 255f, 0f, 1f));
    whiteFont = fonts.getT4CBeaulieuFont(17, Color.WHITE);
    redFont = fonts.getT4CBeaulieuFont(17, new Color(0.63f, 0.08f, 0.08f, 1f));
    grayFont = fonts.getT4CBeaulieuFont(17, Color.GRAY);
    questionFont = fonts.getT4CBeaulieuFont(19, new Color(222 / 255f, 158 / 255f, 0f, 1f));
    hintFont = fonts.getT4CBeaulieuFont(15, Color.LIGHT_GRAY);
    hintBand = createHintBand();
    refreshCharacters();
    selectLastPlayed();
    if (characters.isEmpty() && errorMessage == null) {
      startCreation();
    }
  }

  private static TextureRegion loadSprite(String name) {
    try {
      TextureRegion region = SpriteLoader.getInstance().getRegionFromSpriteName(name);
      if (region == null) {
        throw new IllegalStateException("Missing character-selection sprite: " + name);
      }
      return region;
    } catch (Exception e) {
      throw new IllegalStateException("Unable to load character-selection sprite: " + name, e);
    }
  }

  @Override
  public void render(float delta) {
    elapsed += delta;
    updatePointer(Gdx.input.getX(), Gdx.input.getY());
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    GuiDraw.drawRegionFlipped(
        batch, background, 0f, 0f, camera.viewportWidth, camera.viewportHeight);
    float titleY = titleOffset();
    GuiDraw.drawRegionFlipped(
        batch, title, (camera.viewportWidth - title.getRegionWidth()) / 2f, titleY);
    switch (mode) {
      case SELECT, NAME, SEX, DELETE_CONFIRM -> drawCharacterPanel();
      case CLASS -> drawClassPanel(titleY + title.getRegionHeight() + 20f);
    }
    drawKeyHints();
    batch.end();
  }

  private void drawCharacterPanel() {
    float x = panelX();
    float y = panelY();
    drawTranslucent(panel, x, y);
    boolean hasDetails = mode == Mode.SELECT && !characters.isEmpty();
    boolean selectError = mode == Mode.SELECT && errorMessage != null;
    if (mode == Mode.NAME
        || mode == Mode.SEX
        || mode == Mode.DELETE_CONFIRM
        || hasDetails
        || selectError) {
      drawTranslucent(panelExtension, x, y + panel.getRegionHeight());
    }
    if ((mode == Mode.NAME || (selectError && hasDetails)) && errorMessage != null) {
      drawTranslucent(
          panelExtension, x, y + panel.getRegionHeight() + panelExtension.getRegionHeight());
    }
    drawCentered(goldFont, I18n.key("character.column.name"), x + 16f, y + 44f, 191f);
    drawCentered(goldFont, I18n.key("character.column.level"), x + 215f, y + 44f, 55f);
    float rowY = y + 76f;
    int hovered = mode == Mode.SELECT ? rowAtPointer(x, y) : -1;
    for (int row = 0; row < VISIBLE_ROWS; row++) {
      int index = firstVisible + row;
      if (index >= characters.size()) break;
      if (index == selected) {
        GuiDraw.drawRegionFlipped(batch, rowHighlight, x + 16f, rowY);
      } else if (index == hovered) {
        drawWithAlpha(rowHighlight, x + 16f, rowY, HOVER_ALPHA);
      }
      LocalCharacterStore.CharacterSlot slot = characters.get(index);
      listFont.draw(batch, (index + 1) + ". " + slot.name(), x + 26f, rowY + 3f);
      listFont.draw(batch, Integer.toString(summaries.get(index).level()), x + 221f, rowY + 3f);
      rowY += 30f;
    }
    if (mode == Mode.SELECT) {
      drawSelectionControls(x, y);
      if (hasDetails) {
        drawCentered(goldFont, detailsLine(summaries.get(selected)), x + 6f, y + 244f, 441f);
      }
      if (selectError) {
        drawCentered(redFont, errorMessage, x + 6f, y + (hasDetails ? 309f : 244f), 441f);
      }
    } else if (mode == Mode.DELETE_CONFIRM) {
      goldFont.draw(
          batch,
          I18n.message("character.delete.confirm", selectedSlot().name()),
          x + 16f,
          y + 244f);
      drawSmallButton(x + 289f, y + 243f, I18n.key("character.yes"), true);
      drawSmallButton(x + 367f, y + 243f, I18n.key("character.no"), true);
    } else if (mode == Mode.NAME) {
      goldFont.draw(batch, I18n.key("character.name.prompt"), x + 16f, y + 244f);
      boolean caretOn = (int) (elapsed / CARET_BLINK_SECONDS) % 2 == 0;
      whiteFont.draw(batch, pendingName + (caretOn ? "_" : ""), x + 301f, y + 244f);
      if (errorMessage != null) {
        drawCentered(redFont, errorMessage, x + 6f, y + 309f, 441f);
      }
    } else if (mode == Mode.SEX) {
      goldFont.draw(batch, I18n.message("character.gender.prompt", pendingName), x + 16f, y + 244f);
      drawSmallButton(
          x + 289f,
          y + 243f,
          I18n.key("character.gender.male"),
          true,
          LocalCharacterStore.MALE.equals(pendingGender));
      drawSmallButton(
          x + 367f,
          y + 243f,
          I18n.key("character.gender.female"),
          true,
          LocalCharacterStore.FEMALE.equals(pendingGender));
    }
  }

  private void drawSelectionControls(float x, float y) {
    boolean hasCharacter = !characters.isEmpty();
    drawLargeButton(x + 318f, y + 67f, I18n.key("character.enter"), hasCharacter);
    if (characters.size() < LocalCharacterStore.MAX_CHARACTERS) {
      drawLargeButton(x + 318f, y + 99f, I18n.key("character.create"), true);
    }
    drawLargeButton(x + 318f, y + 131f, I18n.key("character.delete"), hasCharacter);
    drawLargeButton(x + 318f, y + 177f, I18n.key("character.quit"), true);
    float upX = x + 278f;
    float upY = y + 76f;
    GuiDraw.drawRegionFlipped(batch, contains(upX, upY, 20f, 21f) ? upHover : upNormal, upX, upY);
    float downY = y + 170f;
    GuiDraw.drawRegionFlipped(
        batch, contains(upX, downY, 20f, 21f) ? downHover : downNormal, upX, downY);
  }

  private void drawClassPanel(float preferredY) {
    float x = classPanelX();
    float y = classPanelY(preferredY);
    drawTranslucent(questionPanel, x, y);
    // Q_Back's art carries the old questionnaire's five answer-row dividers. They cut straight
    // across this two-column layout, so black the panel's interior out and draw on a clean slate;
    // the ornate frame around the edge is what we actually want from the sprite.
    for (int pass = 0; pass < SCRIM_PASSES; pass++) {
      batch.draw(
          hintBand,
          x + PANEL_INSET,
          y + PANEL_INSET,
          questionPanel.getRegionWidth() - PANEL_INSET * 2f,
          questionPanel.getRegionHeight() - PANEL_INSET * 2f);
    }
    drawCentered(
        questionFont,
        I18n.key("character.class.title"),
        x,
        y + CLASS_TITLE_Y,
        questionPanel.getRegionWidth());

    CharacterClass[] classes = CharacterClass.values();
    int hovered = classRowAtPointer(x, y);
    for (int row = 0; row < classes.length; row++) {
      float rowY = y + CLASS_LIST_Y + row * CLASS_ROW_PITCH;
      if (row == selectedClass) {
        GuiDraw.drawRegionFlipped(batch, rowHighlight, x + CLASS_LIST_X, rowY);
      } else if (row == hovered) {
        drawWithAlpha(rowHighlight, x + CLASS_LIST_X, rowY, HOVER_ALPHA);
      }
      BitmapFont font = row == selectedClass ? goldFont : listFont;
      font.draw(batch, I18n.key(classes[row].nameKey()), x + CLASS_LIST_X + 10f, rowY + 3f);
    }

    preview.render(batch, x + PREVIEW_CENTER_X, y + PREVIEW_FEET_Y);
    drawClassStats(x, y);

    grayFont.draw(
        batch,
        I18n.key(classes[selectedClass].descriptionKey()),
        x + CLASS_LIST_X,
        y + CLASS_DESCRIPTION_Y,
        CLASS_DESCRIPTION_WIDTH,
        Align.left,
        true);

    drawLargeButton(x + CLASS_BUTTON_X, y + CLASS_CREATE_Y, I18n.key("character.create"), true);
    drawLargeButton(
        x + CLASS_BUTTON_X, y + CLASS_REROLL_Y, I18n.key("character.stats.reroll"), true);
    drawLargeButton(x + CLASS_BUTTON_X, y + CLASS_BACK_Y, I18n.key("character.back"), true);
    if (errorMessage != null) {
      redFont.draw(
          batch,
          errorMessage,
          x + CLASS_LIST_X,
          y + CLASS_ERROR_Y,
          CLASS_DESCRIPTION_WIDTH,
          Align.left,
          true);
    }
  }

  private void drawClassStats(float x, float y) {
    String[] labels = {
      I18n.key("character.stats.strength"),
      I18n.key("character.stats.endurance"),
      I18n.key("character.stats.dexterity"),
      I18n.key("character.stats.wisdom"),
      I18n.key("character.stats.intelligence"),
      I18n.key("character.stats.hp"),
      I18n.key("character.stats.mana")
    };
    int[] values = {
      rolledStats.strength(),
      rolledStats.endurance(),
      rolledStats.dexterity(),
      rolledStats.wisdom(),
      rolledStats.intelligence(),
      rolledStats.maxHp(),
      rolledStats.maxMana()
    };
    for (int i = 0; i < labels.length; i++) {
      float rowY = y + STATS_Y + i * STATS_ROW_PITCH;
      // The last two rows are the only ones "Reroll" touches, so they get the gold treatment.
      BitmapFont valueFont = i >= labels.length - 2 ? goldFont : whiteFont;
      grayFont.draw(batch, labels[i], x + STATS_X, rowY);
      layout.setText(valueFont, Integer.toString(values[i]));
      valueFont.draw(batch, layout, x + STATS_VALUE_RIGHT - layout.width, rowY);
    }
  }

  private void drawLargeButton(float x, float y, String text, boolean enabled) {
    TextureRegion region =
        !enabled
            ? largeDisabled
            : contains(x, y, largeNormal.getRegionWidth(), largeNormal.getRegionHeight())
                ? largeHover
                : largeNormal;
    GuiDraw.drawRegionFlipped(batch, region, x, y);
    if (enabled)
      drawButtonTextCentered(
          buttonFont, text, x, y, largeNormal.getRegionWidth(), largeNormal.getRegionHeight());
  }

  private void drawSmallButton(float x, float y, String text, boolean enabled) {
    drawSmallButton(x, y, text, enabled, false);
  }

  private void drawSmallButton(float x, float y, String text, boolean enabled, boolean selected) {
    TextureRegion region =
        enabled
                && (selected
                    || contains(x, y, smallNormal.getRegionWidth(), smallNormal.getRegionHeight()))
            ? smallHover
            : smallNormal;
    GuiDraw.drawRegionFlipped(batch, region, x, y);
    if (enabled)
      drawButtonTextCentered(
          smallButtonFont, text, x, y, smallNormal.getRegionWidth(), smallNormal.getRegionHeight());
  }

  private void drawButtonTextCentered(
      BitmapFont font, String text, float x, float y, float width, float height) {
    layout.setText(font, text);
    font.draw(batch, layout, x + (width - layout.width) / 2f, y + (height - layout.height) / 2f);
  }

  private void drawCentered(BitmapFont font, String text, float x, float y, float width) {
    layout.setText(font, text);
    font.draw(batch, layout, x + (width - layout.width) / 2f, y);
  }

  private void drawTranslucent(TextureRegion region, float x, float y) {
    drawWithAlpha(region, x, y, PANEL_ALPHA);
  }

  private void drawWithAlpha(TextureRegion region, float x, float y, float alpha) {
    Color previous = new Color(batch.getColor());
    batch.setColor(previous.r, previous.g, previous.b, previous.a * alpha);
    GuiDraw.drawRegionFlipped(batch, region, x, y);
    batch.setColor(previous);
  }

  private void drawKeyHints() {
    String key =
        switch (mode) {
          case SELECT -> characters.isEmpty() ? null : "character.hints.select";
          case NAME -> "character.hints.name";
          case SEX -> "character.hints.gender";
          case DELETE_CONFIRM -> "character.hints.delete";
          case CLASS -> "character.hints.class";
        };
    if (key == null) return;
    float bandY = camera.viewportHeight - 44f;
    batch.draw(hintBand, 0f, bandY, camera.viewportWidth, 32f);
    drawCentered(hintFont, I18n.key(key), 0f, bandY + 8f, camera.viewportWidth);
  }

  private static Texture createHintBand() {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(0f, 0f, 0f, 0.6f);
    pixmap.fill();
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  private String detailsLine(Summary summary) {
    String gender =
        I18n.key(
            LocalCharacterStore.FEMALE.equals(summary.gender())
                ? "character.gender.female"
                : "character.gender.male");
    // Characters created before T4C-0059 have no class on file; they just show one field fewer.
    String characterClass = selectedSlot().characterClass();
    String classLabel =
        characterClass == null
            ? ""
            : I18n.key(CharacterClass.byId(characterClass).nameKey()) + "     ";
    return classLabel
        + gender
        + "     "
        + I18n.message("character.details.rebirths", summary.rebirths())
        + "     "
        + I18n.message(
            "character.details.gold", String.format(java.util.Locale.ROOT, "%,d", summary.gold()));
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointerId, int button) {
    if (button != Input.Buttons.LEFT) return false;
    updatePointer(screenX, screenY);
    click();
    return true;
  }

  private void click() {
    switch (mode) {
      case SELECT -> clickSelection();
      case DELETE_CONFIRM -> clickDeleteConfirmation();
      case SEX -> clickGender();
      case CLASS -> clickClassPanel();
      case NAME -> {}
    }
  }

  private void clickSelection() {
    float x = panelX();
    float y = panelY();
    int clickedRow = rowAtPointer(x, y);
    if (clickedRow >= 0) {
      long now = System.currentTimeMillis();
      boolean doubleClick =
          clickedRow == lastClickedIndex && now - lastClickAtMs <= DOUBLE_CLICK_MS;
      lastClickedIndex = clickedRow;
      lastClickAtMs = now;
      selected = clickedRow;
      errorMessage = null;
      playButtonSound();
      if (doubleClick) enterSelectedCharacter();
      return;
    }
    if (contains(x + 278f, y + 76f, 20f, 21f)) {
      moveSelection(-1);
    } else if (contains(x + 278f, y + 170f, 20f, 21f)) {
      moveSelection(1);
    } else if (!characters.isEmpty() && contains(x + 318f, y + 67f, 116f, 27f)) {
      playButtonSound();
      enterSelectedCharacter();
    } else if (characters.size() < LocalCharacterStore.MAX_CHARACTERS
        && contains(x + 318f, y + 99f, 116f, 27f)) {
      playButtonSound();
      startCreation();
    } else if (!characters.isEmpty() && contains(x + 318f, y + 131f, 116f, 27f)) {
      playButtonSound();
      mode = Mode.DELETE_CONFIRM;
    } else if (contains(x + 318f, y + 177f, 116f, 27f)) {
      Gdx.app.exit();
    }
  }

  private int rowAtPointer(float x, float y) {
    for (int row = 0; row < VISIBLE_ROWS; row++) {
      int index = firstVisible + row;
      if (index < characters.size() && contains(x + 16f, y + 76f + row * 30f, 256f, 23f)) {
        return index;
      }
    }
    return -1;
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    if (mode != Mode.SELECT || amountY == 0f) return false;
    moveSelection(amountY > 0f ? 1 : -1);
    return true;
  }

  private void clickDeleteConfirmation() {
    float x = panelX();
    float y = panelY();
    if (contains(x + 289f, y + 243f, 72f, 27f)) {
      playButtonSound();
      deleteSelectedCharacter();
    } else if (contains(x + 367f, y + 243f, 72f, 27f)) {
      playButtonSound();
      mode = Mode.SELECT;
    }
  }

  private void clickGender() {
    float x = panelX();
    float y = panelY();
    if (contains(x + 289f, y + 243f, 72f, 27f)) {
      pendingGender = LocalCharacterStore.MALE;
      playButtonSound();
      startClassSelection();
    } else if (contains(x + 367f, y + 243f, 72f, 27f)) {
      pendingGender = LocalCharacterStore.FEMALE;
      playButtonSound();
      startClassSelection();
    }
  }

  private void clickClassPanel() {
    float x = classPanelX();
    float y = classPanelY(titleOffset() + title.getRegionHeight() + 20f);
    int clickedRow = classRowAtPointer(x, y);
    if (clickedRow >= 0) {
      selectClass(clickedRow);
      return;
    }
    if (contains(x + CLASS_BUTTON_X, y + CLASS_CREATE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
      playButtonSound();
      finishCreation();
    } else if (contains(x + CLASS_BUTTON_X, y + CLASS_REROLL_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
      playButtonSound();
      rolledStats = CharacterCreationRules.rerollVitals(rolledStats, random);
    } else if (contains(x + CLASS_BUTTON_X, y + CLASS_BACK_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
      playButtonSound();
      cancelCreation();
    }
  }

  private int classRowAtPointer(float x, float y) {
    for (int row = 0; row < CharacterClass.values().length; row++) {
      float rowY = y + CLASS_LIST_Y + row * CLASS_ROW_PITCH;
      if (contains(x + CLASS_LIST_X, rowY, CLASS_ROW_WIDTH, CLASS_ROW_HEIGHT)) return row;
    }
    return -1;
  }

  private void selectClass(int row) {
    if (row == selectedClass) return;
    selectedClass = row;
    errorMessage = null;
    playButtonSound();
    rollSelectedClass();
  }

  /** Re-rolls stats for the selected class and re-dresses the preview figure. */
  private void rollSelectedClass() {
    CharacterClass chosen = CharacterClass.values()[selectedClass];
    rolledStats = CharacterCreationRules.roll(chosen, random);
    preview.dress(chosen, pendingGender);
  }

  private float classPanelX() {
    return (camera.viewportWidth - questionPanel.getRegionWidth()) / 2f;
  }

  private float classPanelY(float preferredY) {
    return Math.max(
        0f, Math.min(preferredY, camera.viewportHeight - questionPanel.getRegionHeight()));
  }

  @Override
  public boolean keyDown(int keycode) {
    switch (mode) {
      case SELECT -> handleSelectionKey(keycode);
      case NAME -> handleNameKey(keycode);
      case SEX -> handleGenderKey(keycode);
      case DELETE_CONFIRM -> handleDeleteKey(keycode);
      case CLASS -> handleClassKey(keycode);
    }
    return true;
  }

  @Override
  public boolean keyTyped(char character) {
    if (mode != Mode.NAME || Character.isISOControl(character)) return false;
    if (pendingName.length() < CharacterCreationRules.MAX_NAME_LENGTH
        && (Character.isLetter(character)
            || character == ' '
            || character == '-'
            || character == '\'')) {
      pendingName += character;
      errorMessage = null;
    }
    return true;
  }

  private void handleSelectionKey(int keycode) {
    if (keycode == Input.Keys.UP) moveSelection(-1);
    else if (keycode == Input.Keys.DOWN) moveSelection(1);
    else if (keycode == Input.Keys.ENTER && !characters.isEmpty()) enterSelectedCharacter();
    else if (keycode == Input.Keys.INSERT && characters.size() < LocalCharacterStore.MAX_CHARACTERS)
      startCreation();
    else if (keycode == Input.Keys.FORWARD_DEL && !characters.isEmpty()) mode = Mode.DELETE_CONFIRM;
    else if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
    else if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
      int index = keycode - Input.Keys.NUM_1;
      if (index < characters.size()) selected = index;
    }
  }

  private void handleNameKey(int keycode) {
    if (keycode == Input.Keys.BACKSPACE && !pendingName.isEmpty()) {
      pendingName = pendingName.substring(0, pendingName.length() - 1);
      errorMessage = null;
    } else if (keycode == Input.Keys.ENTER) {
      validateNameAndContinue();
    } else if (keycode == Input.Keys.ESCAPE) {
      cancelCreation();
    }
  }

  private void handleGenderKey(int keycode) {
    if (keycode == Input.Keys.LEFT || keycode == Input.Keys.M) {
      pendingGender = LocalCharacterStore.MALE;
    } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.F) {
      pendingGender = LocalCharacterStore.FEMALE;
    } else if (keycode == Input.Keys.ENTER) {
      startClassSelection();
    } else if (keycode == Input.Keys.ESCAPE) {
      mode = Mode.NAME;
    }
  }

  private void handleDeleteKey(int keycode) {
    if (keycode == Input.Keys.ENTER || keycode == Input.Keys.Y || keycode == Input.Keys.O) {
      deleteSelectedCharacter();
    } else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.N) {
      mode = Mode.SELECT;
    }
  }

  private void handleClassKey(int keycode) {
    int classCount = CharacterClass.values().length;
    if (keycode == Input.Keys.UP) {
      selectClass((selectedClass + classCount - 1) % classCount);
    } else if (keycode == Input.Keys.DOWN) {
      selectClass((selectedClass + 1) % classCount);
    } else if (keycode == Input.Keys.ENTER) {
      finishCreation();
    } else if (keycode == Input.Keys.R) {
      rolledStats = CharacterCreationRules.rerollVitals(rolledStats, random);
    } else if (keycode == Input.Keys.ESCAPE) {
      cancelCreation();
    } else if (keycode >= Input.Keys.NUM_1 && keycode < Input.Keys.NUM_1 + classCount) {
      selectClass(keycode - Input.Keys.NUM_1);
    }
  }

  private void startCreation() {
    pendingName = "";
    pendingGender = LocalCharacterStore.MALE;
    errorMessage = null;
    mode = Mode.NAME;
  }

  private void validateNameAndContinue() {
    pendingName = CharacterCreationRules.normalizeName(pendingName);
    if (!CharacterCreationRules.isValidName(pendingName)) {
      errorMessage = I18n.key("character.name.invalid");
      return;
    }
    if (characters.stream().anyMatch(slot -> slot.name().equalsIgnoreCase(pendingName))) {
      errorMessage = I18n.key("character.name.exists");
      return;
    }
    errorMessage = null;
    mode = Mode.SEX;
  }

  private void startClassSelection() {
    selectedClass = 0;
    errorMessage = null;
    rollSelectedClass();
    mode = Mode.CLASS;
  }

  private void finishCreation() {
    try {
      LocalCharacterStore.CharacterSlot slot =
          LocalCharacterStore.create(
              pendingName, pendingGender, CharacterClass.values()[selectedClass], rolledStats);
      refreshCharacters();
      selected = Math.max(0, characters.indexOf(slot));
      enterCharacter(slot);
    } catch (Exception e) {
      errorMessage = I18n.key("character.create.failed") + ": " + e.getMessage();
    }
  }

  private void cancelCreation() {
    errorMessage = null;
    mode = Mode.SELECT;
  }

  private void deleteSelectedCharacter() {
    if (characters.isEmpty()) return;
    try {
      LocalCharacterStore.delete(selectedSlot());
      refreshCharacters();
      selected = Math.min(selected, Math.max(0, characters.size() - 1));
      firstVisible = Math.min(firstVisible, Math.max(0, characters.size() - VISIBLE_ROWS));
      if (characters.isEmpty()) startCreation();
      else mode = Mode.SELECT;
    } catch (IOException e) {
      errorMessage = I18n.key("character.delete.failed") + ": " + e.getMessage();
      mode = Mode.SELECT;
    }
  }

  private void enterSelectedCharacter() {
    if (!characters.isEmpty()) enterCharacter(selectedSlot());
  }

  private void enterCharacter(LocalCharacterStore.CharacterSlot slot) {
    try {
      LocalCharacterStore.activate(slot);
      rememberLastPlayed(slot);
      game.setScreen(new CharacterLoadingScreen(game, this));
    } catch (Exception e) {
      showLoadError(e);
    }
  }

  void showLoadError(Exception error) {
    String detail = error.getMessage();
    errorMessage =
        I18n.key("character.load.failed")
            + (detail == null || detail.isBlank() ? "" : ": " + detail);
    mode = Mode.SELECT;
  }

  private static void rememberLastPlayed(LocalCharacterStore.CharacterSlot slot) {
    var preferences = GamePreferencesStore.get();
    if (slot.id().equals(preferences.getLastCharacterId())) return;
    preferences.setLastCharacterId(slot.id());
    GamePreferencesStore.save();
  }

  private void selectLastPlayed() {
    String lastId = GamePreferencesStore.get().getLastCharacterId();
    if (lastId == null) return;
    for (int i = 0; i < characters.size(); i++) {
      if (lastId.equals(characters.get(i).id())) {
        selected = i;
        firstVisible = Math.max(0, Math.min(i, characters.size() - VISIBLE_ROWS));
        return;
      }
    }
  }

  private void moveSelection(int direction) {
    if (characters.isEmpty()) return;
    selected = Math.max(0, Math.min(characters.size() - 1, selected + direction));
    if (selected < firstVisible) firstVisible = selected;
    if (selected >= firstVisible + VISIBLE_ROWS) firstVisible = selected - VISIBLE_ROWS + 1;
    playButtonSound();
  }

  private void refreshCharacters() {
    try {
      characters = LocalCharacterStore.list();
      List<Summary> loaded = new ArrayList<>(characters.size());
      for (LocalCharacterStore.CharacterSlot slot : characters) {
        loaded.add(Summary.of(slot, LocalCharacterStore.loadState(slot)));
      }
      summaries = List.copyOf(loaded);
      selected = Math.min(selected, Math.max(0, characters.size() - 1));
      errorMessage = null;
    } catch (IOException e) {
      characters = List.of();
      summaries = List.of();
      errorMessage = I18n.key("character.roster.failed") + ": " + e.getMessage();
    }
  }

  private LocalCharacterStore.CharacterSlot selectedSlot() {
    return characters.get(selected);
  }

  private float panelX() {
    return (camera.viewportWidth - panel.getRegionWidth()) / 2f;
  }

  private float panelY() {
    return (camera.viewportHeight - panel.getRegionHeight()) / 2f + 30f;
  }

  private float titleOffset() {
    return camera.viewportHeight == 720f ? 40f : camera.viewportHeight == 900f ? 60f : 20f;
  }

  private boolean contains(float x, float y, float width, float height) {
    return pointer.x >= x && pointer.x <= x + width && pointer.y >= y && pointer.y <= y + height;
  }

  private void updatePointer(float screenX, float screenY) {
    pointer.set(screenX, screenY);
    viewport.unproject(pointer);
  }

  private void playButtonSound() {
    SoundManager.interfaceSound("Button release sound.wav");
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor(this);
  }

  @Override
  public void hide() {
    if (Gdx.input.getInputProcessor() == this) Gdx.input.setInputProcessor(null);
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void dispose() {
    hintBand.dispose();
    preview.dispose();
  }

  private enum Mode {
    SELECT,
    NAME,
    SEX,
    DELETE_CONFIRM,
    CLASS
  }

  /** What the roster shows for a character, read once from its save file. */
  record Summary(int level, int rebirths, int gold, String gender) {
    static Summary of(LocalCharacterStore.CharacterSlot slot, PlayerStateDto state) {
      if (state == null) return new Summary(1, 0, 0, slot.gender());
      // Saves above the level cap are clamped when loaded into the game; show what you'll get.
      int level = Math.max(1, Math.min(GameConstants.MAX_PLAYER_LEVEL, state.level));
      String gender = state.gender != null ? state.gender : slot.gender();
      int rebirths = Math.max(0, Math.min(GameConstants.REBIRTH_MAX_REMORTS, state.rebirthCount));
      return new Summary(level, rebirths, Math.max(0, state.gold), gender);
    }
  }
}
