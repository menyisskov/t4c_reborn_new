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
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.online.OnlineAccountClient;
import com.perso.T4C.online.OnlineConnection.Credentials;
import com.perso.T4C.online.OnlineCreateForm;
import com.perso.T4C.online.OnlineLobbyModel;
import com.perso.T4C.online.OnlineLobbyModel.ReturnPhase;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import com.perso.T4C.online.model.OnlineRoster;
import com.perso.T4C.online.model.OnlineRoster.CharacterSummary;
import com.perso.T4C.screens.OnlineCreateLayout.Target;
import com.perso.T4C.screens.OnlineLobbyLayout.Button;
import com.perso.T4C.ui.FontManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.LongSupplier;

/**
 * Online-only character lobby. It is drawn from the same art, fonts and control geometry as the
 * single-player {@link CharacterSelectionScreen}, but is driven by {@link OnlineLobbyModel} and never
 * touches the local save store. The roster is fetched on a worker thread; rendering never blocks, and
 * the model is only ever read or mutated on the render thread. The account key is held by the model
 * and the page source only; nothing here displays it.
 *
 * <p>Character creation is a second state of the same panel ({@link Mode#CREATE}): a name field, the
 * classes the server accepts, gender and alignment, driven by {@link OnlineCreateForm}. The creation
 * request runs on the worker thread; on success the roster is reloaded with the new character selected.
 */
public final class OnlineLobbyScreen extends InputAdapter implements Screen {
  /** Receives the player's decision. Both callbacks run on the render thread. */
  public interface Listener {
    void play(Credentials credentials);

    void back();
  }

  /** Performs one authenticated creation on a worker thread; implemented by {@link OnlineAccountClient#create}. */
  @FunctionalInterface
  public interface Creator {
    OnlineCharacterCreation.Result create(OnlineCharacterCreation.Request request)
        throws IOException, InterruptedException;
  }

  /** Which state of the panel is showing. */
  enum Mode {
    LIST,
    CREATE
  }

  private static final float CARET_BLINK_SECONDS = 0.5f;
  private static final float PANEL_ALPHA = 1f;
  private static final float HOVER_ALPHA = 0.45f;
  private static final long DOUBLE_CLICK_MS = 400L;
  /** Whole-load ceiling; the account client itself bounds every request to a couple of seconds. */
  private static final long LOAD_DEADLINE_MS = 25_000L;
  /** Never used to confirm; lets a worker run the model's paging/validation without sharing state. */
  private static final String SCRATCH_KEY = "0".repeat(64);

  private final SpriteBatch batch;
  private final OnlineLobbyModel model;
  private final OnlineLobbyModel.PageSource source;
  private final Listener listener;
  private final Creator creator;
  private final LongSupplier clock;
  private final AutoCloseable ownedResource;
  private final OrthographicCamera camera = new OrthographicCamera();
  private final ScreenViewport viewport;
  private final Vector2 pointer = new Vector2();
  private Vector2 forcedPointer;
  private final GlyphLayout layout = new GlyphLayout();
  private final JournalTheme theme = new JournalTheme();
  private final TextureRegion background;
  private final TextureRegion title;
  private final TextureRegion panelFrame;
  private final TextureRegion createFrame;
  private final TextureRegion largeNormal;
  private final TextureRegion largeHover;
  private final TextureRegion largeDisabled;
  private final TextureRegion rowHighlight;
  private final TextureRegion classHighlight;
  private final TextureRegion smallNormal;
  private final TextureRegion smallHover;
  private final TextureRegion upNormal;
  private final TextureRegion upHover;
  private final TextureRegion downNormal;
  private final TextureRegion downHover;
  private final BitmapFont buttonFont;
  private final BitmapFont smallButtonFont;
  private final BitmapFont whiteFont;
  private final BitmapFont listFont;
  private final BitmapFont dimListFont;
  private final BitmapFont goldFont;
  private final BitmapFont redFont;
  private final BitmapFont grayFont;
  private final BitmapFont titleFont;
  private final BitmapFont hintFont;
  private final Texture hintBand;
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(
          r -> {
            Thread t = new Thread(r, "online-lobby-roster");
            t.setDaemon(true);
            return t;
          });

  private final OnlineCreateForm form = new OnlineCreateForm();
  private Mode mode = Mode.LIST;
  private Future<OnlineCharacterCreation.Result> creating;
  private String createError;
  private UUID selectAfterLoad;
  private float elapsed;
  private Future<Map<UUID, OnlineRoster>> pending;
  private long loadStartedMs;
  private boolean everLoaded;
  private String errorMessage;
  private int firstVisible;
  private int lastClickedIndex = -1;
  private long lastClickAtMs;
  private boolean finished;

  public OnlineLobbyScreen(
      SpriteBatch batch,
      OnlineLobbyModel model,
      OnlineLobbyModel.PageSource source,
      Listener listener) {
    this(batch, model, source, null, listener, System::currentTimeMillis, null);
  }

  /**
   * Convenience for the real client: pages come from {@code client}, which the screen closes when it
   * is disposed.
   */
  public static OnlineLobbyScreen forAccountClient(
      SpriteBatch batch,
      OnlineLobbyModel model,
      OnlineAccountClient client,
      Listener listener) {
    return new OnlineLobbyScreen(
        batch, model, client::page, client::create, listener, System::currentTimeMillis, client);
  }

  OnlineLobbyScreen(
      SpriteBatch batch,
      OnlineLobbyModel model,
      OnlineLobbyModel.PageSource source,
      Creator creator,
      Listener listener,
      LongSupplier clock,
      AutoCloseable ownedResource) {
    this.batch = batch;
    this.model = model;
    this.source = source;
    this.creator = creator;
    this.listener = listener;
    this.clock = clock;
    this.ownedResource = ownedResource;
    camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    viewport = new ScreenViewport(camera);
    background = loadSprite("Back01_1280");
    title = loadSprite("Connect_Title2");
    largeNormal = styledSprite("PS_BtnN", false);
    largeHover = styledSprite("PS_BtnH", true);
    largeDisabled = styledSprite("PS_BtnD", false);
    rowHighlight =
        theme.control((int) OnlineLobbyLayout.ROW_WIDTH - 1, loadSprite("PS_Over").getRegionHeight(), true);
    classHighlight =
        theme.control((int) OnlineCreateLayout.CLASS_WIDTH - 1, loadSprite("PS_Over").getRegionHeight(), true);
    smallNormal = styledSprite("PS_SmallBtnN", false);
    smallHover = styledSprite("PS_SmallBtnH", true);
    upNormal = styledSprite("PS_SBtnUPN", false);
    upHover = styledSprite("PS_SBtnUPH", true);
    downNormal = styledSprite("PS_SBtnDNN", false);
    downHover = styledSprite("PS_SBtnDNH", true);
    panelFrame =
        theme.plainFrame((int) OnlineLobbyLayout.PANEL_WIDTH, (int) OnlineLobbyLayout.PANEL_HEIGHT);
    createFrame =
        theme.plainFrame(
            (int) OnlineLobbyLayout.PANEL_WIDTH,
            (int) Math.ceil(OnlineCreateLayout.panelHeight(form.classes().size())));
    FontManager fonts = FontManager.getInstance();
    buttonFont = fonts.getTahomaFont(12, JournalTheme.TEXT, false);
    smallButtonFont = fonts.getTahomaFont(11, JournalTheme.TEXT, false);
    whiteFont = fonts.getTahomaFont(14, Color.WHITE, false);
    listFont = fonts.getTahomaFont(14, JournalTheme.TEXT, false);
    dimListFont = fonts.getTahomaFont(14, JournalTheme.DIM, false);
    goldFont = fonts.getTahomaFont(14, JournalTheme.GOLD, false);
    redFont = fonts.getTahomaFont(13, Color.valueOf("E4A29B"), false);
    grayFont = fonts.getTahomaFont(13, JournalTheme.DIM, false);
    titleFont = fonts.getT4CBeaulieuFont(22, JournalTheme.GOLD);
    hintFont = fonts.getT4CBeaulieuFont(15, Color.LIGHT_GRAY);
    hintBand = createHintBand();
    startLoad();
  }

  private TextureRegion styledSprite(String name, boolean active) {
    TextureRegion source = loadSprite(name);
    return theme.control(source.getRegionWidth(), source.getRegionHeight(), active);
  }

  private static TextureRegion loadSprite(String name) {
    try {
      TextureRegion region = SpriteLoader.getInstance().getRegionFromSpriteName(name);
      if (region == null) throw new IllegalStateException("Missing lobby sprite: " + name);
      return region;
    } catch (Exception e) {
      throw new IllegalStateException("Unable to load lobby sprite: " + name, e);
    }
  }

  // ---------------------------------------------------------------- loading (worker + poll)

  private void startLoad() {
    if (pending != null) return;
    errorMessage = null;
    loadStartedMs = clock.getAsLong();
    pending =
        worker.submit(
            () -> {
              // Run the model's own paging/validation against a scratch model on the worker, keeping
              // only the raw pages; the real model replays them on the render thread, no I/O there.
              Map<UUID, OnlineRoster> pages = new HashMap<>();
              new OnlineLobbyModel(SCRATCH_KEY, null)
                  .load(
                      after -> {
                        OnlineRoster roster = source.page(after);
                        pages.put(after, roster);
                        return roster;
                      });
              return pages;
            });
  }

  private void pollLoad() {
    if (pending == null) return;
    if (!pending.isDone()) {
      if (clock.getAsLong() - loadStartedMs > LOAD_DEADLINE_MS) {
        pending.cancel(true);
        pending = null;
        errorMessage = I18n.key("online.lobby.error.timeout");
      }
      return;
    }
    Future<Map<UUID, OnlineRoster>> done = pending;
    pending = null;
    try {
      Map<UUID, OnlineRoster> pages = done.get();
      model.load(
          after -> {
            OnlineRoster roster = pages.get(after);
            if (roster == null) throw new IOException("Missing roster page");
            return roster;
          });
      everLoaded = true;
      if (selectAfterLoad != null) {
        model.select(selectAfterLoad);
        selectAfterLoad = null;
      }
      firstVisible = OnlineLobbyLayout.scrollToShow(firstVisible, model.selectedIndex(), count());
    } catch (ExecutionException failed) {
      errorMessage = describe(failed.getCause());
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      errorMessage = I18n.key("online.lobby.error.timeout");
    } catch (IOException invalid) {
      errorMessage = describe(invalid);
    } catch (java.util.concurrent.CancellationException cancelled) {
      errorMessage = I18n.key("online.lobby.error.timeout");
    }
  }

  /** Plain-English text for a load failure; never includes the account key or raw exception text. */
  static String describe(Throwable failure) {
    if (failure instanceof OnlineAccountClient.Rejected rejected) {
      int status = rejected.statusCode();
      if (status == 401 || status == 403) return I18n.key("online.lobby.error.rejected_auth");
      if (status == 429 || status == 503) return I18n.key("online.lobby.error.rejected_busy");
      return I18n.message("online.lobby.error.rejected", status);
    }
    if (failure instanceof InterruptedException) return I18n.key("online.lobby.error.timeout");
    return I18n.key("online.lobby.error.unreachable");
  }

  boolean loading() {
    return pending != null;
  }

  boolean loadedOnce() {
    return everLoaded;
  }

  String errorMessage() {
    return errorMessage;
  }

  // ---------------------------------------------------------------- rendering

  @Override
  public void render(float delta) {
    elapsed += delta;
    pollLoad();
    pollCreate();
    updatePointer(Gdx.input.getX(), Gdx.input.getY());
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    GuiDraw.drawRegionFlipped(
        batch, background, 0f, 0f, camera.viewportWidth, camera.viewportHeight);
    GuiDraw.drawRegionFlipped(
        batch, title, (camera.viewportWidth - title.getRegionWidth()) / 2f, titleOffset());
    if (mode == Mode.CREATE) drawCreatePanel();
    else drawPanel();
    drawKeyHints();
    batch.end();
  }

  private void drawPanel() {
    float x = panelX();
    float y = panelY();
    long now = clock.getAsLong();
    drawWithAlpha(panelFrame, x, y, PANEL_ALPHA);
    drawCentered(titleFont, I18n.key("online.lobby.title"), x, y + 12f, OnlineLobbyLayout.PANEL_WIDTH);
    float headerY = y + 44f;
    float rowX = x + OnlineLobbyLayout.ROW_X;
    goldFont.draw(batch, I18n.key("character.column.name"), rowX + OnlineLobbyLayout.NAME_X, headerY);
    goldFont.draw(batch, I18n.key("online.lobby.column.class"), rowX + OnlineLobbyLayout.CLASS_X, headerY);
    goldFont.draw(batch, I18n.key("character.column.level"), rowX + OnlineLobbyLayout.LEVEL_X, headerY);
    goldFont.draw(batch, I18n.key("online.lobby.column.status"), rowX + OnlineLobbyLayout.STATUS_X, headerY);

    var entries = model.entries();
    int selectedIndex = model.selectedIndex();
    int hovered = rowAtPointer(x, y);
    for (int row = 0; row < OnlineLobbyLayout.VISIBLE_ROWS; row++) {
      int index = firstVisible + row;
      if (index >= entries.size()) break;
      float rowY = y + OnlineLobbyLayout.ROW_Y + row * OnlineLobbyLayout.ROW_PITCH;
      theme.well(batch, rowX, rowY, OnlineLobbyLayout.ROW_WIDTH - 1f, 26f);
      if (index == selectedIndex) {
        GuiDraw.drawRegionFlipped(batch, rowHighlight, rowX, rowY);
      } else if (index == hovered) {
        drawWithAlpha(rowHighlight, rowX, rowY, HOVER_ALPHA);
      }
      CharacterSummary c = entries.get(index);
      BitmapFont font = c.level() == null ? dimListFont : listFont;
      float textY = rowY + 3f;
      drawClipped(font, c.name(), rowX + OnlineLobbyLayout.NAME_X, textY, 140f);
      drawClipped(font, classLabel(c.characterClass()), rowX + OnlineLobbyLayout.CLASS_X, textY, 96f);
      font.draw(batch, c.level() == null ? "-" : Integer.toString(c.level()),
          rowX + OnlineLobbyLayout.LEVEL_X, textY);
      drawClipped(font, availabilityLabel(c.availability()), rowX + OnlineLobbyLayout.STATUS_X, textY, 86f);
    }
    drawControls(x, y, now);

    String status = statusLine(now);
    if (status != null) {
      drawCentered(goldFont, status, x, y + OnlineLobbyLayout.STATUS_LINE_Y, OnlineLobbyLayout.PANEL_WIDTH);
    }
    if (errorMessage != null) {
      drawCentered(redFont, errorMessage, x, y + OnlineLobbyLayout.ERROR_LINE_Y, OnlineLobbyLayout.PANEL_WIDTH);
    }
  }

  private void drawControls(float x, float y, long now) {
    boolean busy = loading();
    drawLargeButton(x + OnlineLobbyLayout.BUTTON_X, y + OnlineLobbyLayout.PLAY_Y,
        I18n.key("online.lobby.play"), canPlay(now), Button.PLAY);
    drawLargeButton(x + OnlineLobbyLayout.BUTTON_X, y + OnlineLobbyLayout.REFRESH_Y,
        I18n.key("online.lobby.refresh"), !busy, Button.REFRESH);
    drawLargeButton(x + OnlineLobbyLayout.BUTTON_X, y + OnlineLobbyLayout.CREATE_Y,
        I18n.key("online.lobby.create"), canCreate(), Button.CREATE);
    drawLargeButton(x + OnlineLobbyLayout.BUTTON_X, y + OnlineLobbyLayout.BACK_Y,
        I18n.key("online.lobby.back"), true, Button.BACK);
    float upX = x + OnlineLobbyLayout.ARROW_X;
    float upY = y + OnlineLobbyLayout.ARROW_UP_Y;
    float downY = y + OnlineLobbyLayout.ARROW_DOWN_Y;
    Button over = buttonAtPointer(x, y);
    GuiDraw.drawRegionFlipped(batch, over == Button.UP ? upHover : upNormal, upX, upY);
    drawButtonTextCentered(buttonFont, "↑", upX, upY, OnlineLobbyLayout.ARROW_WIDTH, OnlineLobbyLayout.ARROW_HEIGHT);
    GuiDraw.drawRegionFlipped(batch, over == Button.DOWN ? downHover : downNormal, upX, downY);
    drawButtonTextCentered(buttonFont, "↓", upX, downY, OnlineLobbyLayout.ARROW_WIDTH, OnlineLobbyLayout.ARROW_HEIGHT);
  }

  private void drawLargeButton(float x, float y, String text, boolean enabled, Button which) {
    TextureRegion region =
        !enabled ? largeDisabled : buttonAtPointer(panelX(), panelY()) == which ? largeHover : largeNormal;
    GuiDraw.drawRegionFlipped(batch, region, x, y);
    // The single-player screen leaves disabled buttons blank; online a dimmed label says which one.
    drawButtonTextCentered(
        enabled ? buttonFont : grayFont,
        text,
        x,
        y,
        largeNormal.getRegionWidth(),
        largeNormal.getRegionHeight());
  }

  /** The line under the roster: return-from-game state first, then loading/empty/truncated notes. */
  private String statusLine(long now) {
    ReturnPhase phase = model.phase(now);
    if (phase == ReturnPhase.LEAVE_PENDING) return I18n.key("online.lobby.leave_pending");
    if (phase == ReturnPhase.LEASE_HELD) {
      OptionalLong remaining = model.millisUntilRejoin(now);
      long seconds = OnlineLobbyLayout.secondsCeil(remaining.orElse(0L));
      return I18n.message("online.lobby.lease_held", (int) seconds);
    }
    if (loading()) return I18n.key("online.lobby.loading");
    if (errorMessage != null) return null;
    if (everLoaded && model.entries().isEmpty()) return I18n.key("online.lobby.empty");
    if (model.truncated()) return I18n.message("online.lobby.truncated", OnlineLobbyModel.MAX_ENTRIES);
    return null;
  }

  private void drawKeyHints() {
    float bandY = camera.viewportHeight - 44f;
    batch.draw(hintBand, 0f, bandY, camera.viewportWidth, 32f);
    String hints = mode == Mode.CREATE ? "online.create.hints" : "online.lobby.hints";
    drawCentered(hintFont, I18n.key(hints), 0f, bandY + 8f, camera.viewportWidth);
  }

  private static Texture createHintBand() {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(0f, 0f, 0f, 0.6f);
    pixmap.fill();
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  static String classLabel(String characterClass) {
    if (characterClass == null) return "-";
    for (CharacterClass value : CharacterClass.values())
      if (value.id().equalsIgnoreCase(characterClass)) return I18n.key(value.nameKey());
    return characterClass;
  }

  static String availabilityLabel(String availability) {
    return I18n.key("online.lobby.status." + availability, availability);
  }

  private void drawClipped(BitmapFont font, String text, float x, float y, float width) {
    font.draw(batch, text, x, y, 0, text.length(), width, Align.left, false, "...");
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

  private void drawWithAlpha(TextureRegion region, float x, float y, float alpha) {
    Color previous = new Color(batch.getColor());
    batch.setColor(previous.r, previous.g, previous.b, previous.a * alpha);
    GuiDraw.drawRegionFlipped(batch, region, x, y);
    batch.setColor(previous);
  }

  // ---------------------------------------------------------------- input

  private boolean canPlay(long now) {
    return !loading() && !finished && model.confirm(now).isPresent();
  }

  private int count() {
    return model.entries().size();
  }

  @Override
  public boolean keyDown(int keycode) {
    if (mode == Mode.CREATE) return createKeyDown(keycode);
    if (keycode == Input.Keys.UP) moveSelection(-1);
    else if (keycode == Input.Keys.DOWN) moveSelection(1);
    else if (keycode == Input.Keys.ENTER) play();
    else if (keycode == Input.Keys.R) refresh();
    else if (keycode == Input.Keys.C) openCreate();
    else if (keycode == Input.Keys.ESCAPE) back();
    else return false;
    return true;
  }

  @Override
  public boolean keyTyped(char character) {
    if (mode != Mode.CREATE || Character.isISOControl(character)) return false;
    if (creating == null && form.type(character)) createError = null;
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointerId, int button) {
    if (button != Input.Buttons.LEFT) return false;
    updatePointer(screenX, screenY);
    float x = panelX();
    float y = panelY();
    if (mode == Mode.CREATE) return createTouch(x, y);
    int row = rowAtPointer(x, y);
    if (row >= 0) {
      long now = System.currentTimeMillis();
      boolean doubleClick = row == lastClickedIndex && now - lastClickAtMs <= DOUBLE_CLICK_MS;
      lastClickedIndex = row;
      lastClickAtMs = now;
      model.select(model.entries().get(row).id());
      click();
      if (doubleClick) play();
      return true;
    }
    Button hit = buttonAtPointer(x, y);
    if (hit == null) return true;
    switch (hit) {
      case UP -> moveSelection(-1);
      case DOWN -> moveSelection(1);
      case PLAY -> play();
      case REFRESH -> refresh();
      case CREATE -> openCreate();
      case BACK -> back();
    }
    return true;
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    if (amountY == 0f) return false;
    moveSelection(amountY > 0f ? 1 : -1);
    return true;
  }

  private void moveSelection(int direction) {
    if (count() == 0) return;
    if (direction < 0) model.selectPrevious();
    else model.selectNext();
    firstVisible = OnlineLobbyLayout.scrollToShow(firstVisible, model.selectedIndex(), count());
    click();
  }

  private void play() {
    if (finished || loading()) return;
    var credentials = model.confirm(clock.getAsLong());
    if (credentials.isEmpty()) return;
    finished = true;
    click();
    listener.play(credentials.get());
  }

  // ---------------------------------------------------------------- creation

  boolean canCreate() {
    return creator != null && !finished && !loading() && model.phase(clock.getAsLong()) == ReturnPhase.IDLE;
  }

  Mode mode() {
    return mode;
  }

  OnlineCreateForm form() {
    return form;
  }

  String createError() {
    return createError;
  }

  boolean creatingNow() {
    return creating != null;
  }

  private void openCreate() {
    if (!canCreate()) return;
    mode = Mode.CREATE;
    createError = null;
    errorMessage = null;
    click();
  }

  private void closeCreate() {
    if (creating != null) return;
    mode = Mode.LIST;
    createError = null;
    click();
  }

  private void submitCreate() {
    if (creating != null || creator == null) return;
    var request = form.request();
    if (request == null) {
      createError = form.nameProblem();
      return;
    }
    createError = null;
    click();
    creating = worker.submit(() -> creator.create(request));
  }

  private void pollCreate() {
    if (creating == null || !creating.isDone()) return;
    Future<OnlineCharacterCreation.Result> done = creating;
    creating = null;
    try {
      OnlineCharacterCreation.Result result = done.get();
      selectAfterLoad = result.character().id();
      mode = Mode.LIST;
      createError = null;
      pending = null;
      startLoad();
    } catch (ExecutionException failed) {
      createError = OnlineCreateForm.describe(failed.getCause());
      if (OnlineCreateForm.needsFreshRequest(failed.getCause())) form.freshRequest();
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      createError = I18n.key("online.create.error.timeout");
    } catch (java.util.concurrent.CancellationException cancelled) {
      createError = I18n.key("online.create.error.timeout");
    }
  }

  private boolean createKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) closeCreate();
    else if (keycode == Input.Keys.ENTER) submitCreate();
    else if (creating != null) return true;
    else if (keycode == Input.Keys.BACKSPACE) {
      form.backspace();
      createError = null;
    } else if (keycode == Input.Keys.UP) form.moveClass(-1);
    else if (keycode == Input.Keys.DOWN) form.moveClass(1);
    else if (keycode == Input.Keys.LEFT) form.setGender("male");
    else if (keycode == Input.Keys.RIGHT) form.setGender("female");
    else if (keycode == Input.Keys.TAB) form.setEvil(!form.evil());
    else return false;
    return true;
  }

  private boolean createTouch(float x, float y) {
    if (creating != null) return true;
    int classRow = OnlineCreateLayout.classAt(pointer.x, pointer.y, x, y, form.classes().size());
    if (classRow >= 0) {
      form.selectClass(classRow);
      click();
      return true;
    }
    Target hit = OnlineCreateLayout.targetAt(pointer.x, pointer.y, x, y);
    if (hit == null) return true;
    switch (hit) {
      case MALE -> form.setGender("male");
      case FEMALE -> form.setGender("female");
      case GOOD -> form.setEvil(false);
      case EVIL -> form.setEvil(true);
      case CREATE -> submitCreate();
      case BACK -> closeCreate();
      case NAME -> {}
    }
    return true;
  }

  private void drawCreatePanel() {
    float x = panelX();
    float y = panelY();
    drawWithAlpha(createFrame, x, y, PANEL_ALPHA);
    drawCentered(titleFont, I18n.key("online.create.title"), x, y + 12f, OnlineLobbyLayout.PANEL_WIDTH);
    goldFont.draw(batch, I18n.key("online.create.name"), x + OnlineCreateLayout.LEFT_X, y + OnlineCreateLayout.LABEL_Y);
    float fieldX = x + OnlineCreateLayout.LEFT_X;
    float fieldY = y + OnlineCreateLayout.FIELD_Y;
    theme.well(batch, fieldX, fieldY, OnlineCreateLayout.FIELD_WIDTH, OnlineCreateLayout.FIELD_HEIGHT);
    boolean caretOn = creating == null && (int) (elapsed / CARET_BLINK_SECONDS) % 2 == 0;
    whiteFont.draw(batch, form.name() + (caretOn ? "_" : ""), fieldX + 10f, fieldY + 5f);

    goldFont.draw(batch, I18n.key("online.create.class"), fieldX, y + OnlineCreateLayout.CLASS_LABEL_Y);
    int hovered = creating != null ? -1 : OnlineCreateLayout.classAt(pointer.x, pointer.y, x, y, form.classes().size());
    int rows = form.classes().size();
    for (int row = 0; row < rows; row++) {
      float rowY = y + OnlineCreateLayout.CLASS_Y + row * OnlineCreateLayout.CLASS_PITCH;
      theme.well(batch, fieldX, rowY, OnlineCreateLayout.CLASS_WIDTH, OnlineCreateLayout.CLASS_HEIGHT + 3f);
      if (row == form.classIndex()) GuiDraw.drawRegionFlipped(batch, classHighlight, fieldX, rowY);
      else if (row == hovered) drawWithAlpha(classHighlight, fieldX, rowY, HOVER_ALPHA);
      (row == form.classIndex() ? goldFont : listFont)
          .draw(batch, classLabel(form.classes().get(row)), fieldX + 10f, rowY + 3f);
    }

    float midX = x + OnlineCreateLayout.MID_X;
    goldFont.draw(batch, I18n.key("online.create.gender"), midX, y + OnlineCreateLayout.GENDER_LABEL_Y);
    drawChoice(midX, y + OnlineCreateLayout.GENDER_Y, I18n.key("character.gender.male"), "male".equals(form.gender()), Target.MALE);
    drawChoice(midX + OnlineCreateLayout.SMALL_GAP, y + OnlineCreateLayout.GENDER_Y,
        I18n.key("character.gender.female"), "female".equals(form.gender()), Target.FEMALE);
    goldFont.draw(batch, I18n.key("online.create.alignment"), midX, y + OnlineCreateLayout.ALIGN_LABEL_Y);
    drawChoice(midX, y + OnlineCreateLayout.ALIGN_Y, I18n.key("character.alignment.good"), !form.evil(), Target.GOOD);
    drawChoice(midX + OnlineCreateLayout.SMALL_GAP, y + OnlineCreateLayout.ALIGN_Y,
        I18n.key("character.alignment.evil"), form.evil(), Target.EVIL);

    boolean busy = creating != null;
    drawFormButton(x + OnlineLobbyLayout.BUTTON_X, y + OnlineCreateLayout.CREATE_Y,
        I18n.key("online.create.submit"), form.canSubmit() && !busy, Target.CREATE);
    drawFormButton(x + OnlineLobbyLayout.BUTTON_X, y + OnlineCreateLayout.BACK_Y,
        I18n.key("online.create.back"), !busy, Target.BACK);

    if (busy) {
      drawCentered(goldFont, I18n.key("online.create.working"), x, y + OnlineCreateLayout.statusLineY(rows),
          OnlineLobbyLayout.PANEL_WIDTH);
    }
    if (createError != null) {
      drawCentered(redFont, createError, x, y + OnlineCreateLayout.errorLineY(rows), OnlineLobbyLayout.PANEL_WIDTH);
    }
  }

  private void drawChoice(float x, float y, String text, boolean selected, Target which) {
    boolean over = creating == null && OnlineCreateLayout.targetAt(pointer.x, pointer.y, panelX(), panelY()) == which;
    GuiDraw.drawRegionFlipped(batch, selected || over ? smallHover : smallNormal, x, y);
    drawButtonTextCentered(smallButtonFont, text, x, y, smallNormal.getRegionWidth(), smallNormal.getRegionHeight());
  }

  private void drawFormButton(float x, float y, String text, boolean enabled, Target which) {
    boolean over = OnlineCreateLayout.targetAt(pointer.x, pointer.y, panelX(), panelY()) == which;
    TextureRegion region = !enabled ? largeDisabled : over ? largeHover : largeNormal;
    GuiDraw.drawRegionFlipped(batch, region, x, y);
    drawButtonTextCentered(enabled ? buttonFont : grayFont, text, x, y,
        largeNormal.getRegionWidth(), largeNormal.getRegionHeight());
  }

  private void refresh() {
    if (finished || loading()) return;
    click();
    startLoad();
  }

  private void back() {
    if (finished) return;
    finished = true;
    click();
    listener.back();
  }

  private static void click() {
    try {
      SoundManager.interfaceSound("Button release sound.wav");
    } catch (RuntimeException unavailable) {
      // Audio can be disabled (previews, headless smoke runs); the lobby works without it.
    }
  }

  // ---------------------------------------------------------------- geometry helpers

  private int rowAtPointer(float x, float y) {
    return OnlineLobbyLayout.rowAt(pointer.x, pointer.y, x, y, firstVisible, count());
  }

  private Button buttonAtPointer(float x, float y) {
    return OnlineLobbyLayout.buttonAt(pointer.x, pointer.y, x, y);
  }

  float panelX() {
    return (camera.viewportWidth - OnlineLobbyLayout.PANEL_WIDTH) / 2f;
  }

  float panelY() {
    return (camera.viewportHeight - OnlineLobbyLayout.PANEL_HEIGHT) / 2f + 30f;
  }

  private float titleOffset() {
    return camera.viewportHeight == 720f ? 40f : camera.viewportHeight == 900f ? 60f : 20f;
  }

  /** Test/preview hook: pin the pointer at panel-relative coordinates instead of the mouse. */
  void pinPointerToPanel(float relativeX, float relativeY) {
    forcedPointer = new Vector2(panelX() + relativeX, panelY() + relativeY);
  }

  private void updatePointer(float screenX, float screenY) {
    if (forcedPointer != null) {
      pointer.set(forcedPointer);
      return;
    }
    pointer.set(screenX, screenY);
    viewport.unproject(pointer);
  }

  // ---------------------------------------------------------------- Screen

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  @Override
  public void show() {
    finished = false;
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
    worker.shutdownNow();
    if (pending != null) pending.cancel(true);
    if (ownedResource != null) {
      try {
        ownedResource.close();
      } catch (Exception ignored) {
        // Closing a finished account client has nothing left to report.
      }
    }
    hintBand.dispose();
    theme.dispose();
  }
}
