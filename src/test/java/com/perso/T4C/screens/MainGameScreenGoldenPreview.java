package com.perso.T4C.screens;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.NEW_CHARACTER_START_TILE_X;
import static com.perso.T4C.config.GameConstants.NEW_CHARACTER_START_TILE_Y;
import static com.perso.T4C.config.GameConstants.WINDOW_HEIGHT;
import static com.perso.T4C.config.GameConstants.WINDOW_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perso.T4C.MyGame;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.Inventory;
import com.perso.T4C.gui.screen.SpellBook;
import com.perso.T4C.gui.screen.Statistics;
import com.perso.T4C.helper.GameCursorManager;
import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.PlayerHUD;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Developer preview and golden-image producer (stage 0 of the client unification, not shipped):
 * opens the REAL single-player {@link MainGameScreen} on a fixed fixture character in a real LibGDX
 * window, with no network and no write to the real save directory, and saves PNGs of the world with
 * the HUD, the inventory, the spell book and the statistics screens.
 *
 * <p>Determinism: the fixture save lives in a throw-away directory that becomes {@code user.dir}
 * only after the screen is built, every frame is rendered with a fixed delta of zero (no AI step,
 * no animation clock, no day/night advance) and the day/night hour is fixed in the fixture. Run
 * through {@link MainGameScreenGoldenTest}, or by hand from the repository root: {@code java -cp
 * ... com.perso.T4C.screens.MainGameScreenGoldenPreview <output-directory>}.
 */
public final class MainGameScreenGoldenPreview extends MyGame {
  /** Names of the produced images, in render order. */
  public static final List<String> STATES =
      List.of("world-hud", "inventory", "spell-book", "statistics");

  private final Path outputDir;
  private final Path saveDir;
  private MainGameScreen screen;
  private PlayerHUD hud;
  private int frame;
  private int stage;

  MainGameScreenGoldenPreview(Path outputDir, Path saveDir) {
    this.outputDir = outputDir;
    this.saveDir = saveDir;
  }

  public static void main(String[] args) {
    Path out = Path.of(args.length > 0 ? args[0] : ".").toAbsolutePath();
    Path saves;
    try {
      Files.createDirectories(out);
      saves = Files.createTempDirectory("t4c-golden-save");
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Main game screen golden preview");
    config.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
    config.setResizable(false);
    config.disableAudio(true);
    config.useVsync(false);
    new Lwjgl3Application(new MainGameScreenGoldenPreview(out, saves), config);
  }

  /** The fixed fixture character: same stats, gear, spells and place on every run. */
  static PlayerStateDto fixture() {
    Player p = new Player();
    p.setName("Goldie");
    p.setGender("male");
    p.getCoordinates().setX((NEW_CHARACTER_START_TILE_X + 0.5f) * GRID_W);
    p.getCoordinates().setY((NEW_CHARACTER_START_TILE_Y + 0.5f) * GRID_H);
    p.getCoordinates().setZ(0);
    p.setStrength(40);
    p.setDexterity(30);
    p.setEndurance(35);
    p.setIntelligence(20);
    p.setWisdom(25);
    p.setLevel(12);
    p.setCurrentXp(1200);
    p.setXpToNextLevel(5000);
    p.setGold(1234);
    p.setKarma(10);
    p.setMaxHp(180);
    p.setCurrentHp(150);
    p.setMaxMana(60);
    p.setMana(40);
    p.setStatPoints(3);
    p.setSkillPoints(4);
    p.setSkillLevel("attack", 120);
    p.setSkillLevel("dodge", 80);
    p.setInventory(
        new java.util.ArrayList<>(
            List.of("item.dagger", "item.healing_potion", "item.healing_potion")));
    p.setSpells(new java.util.ArrayList<>(List.of("spell.heal_light", "spell.light")));
    PlayerStateDto state = PlayerStateMapper.fromPlayer(p, 12f);
    state.dayNightHour = 12f;
    state.equipment =
        new java.util.HashMap<>(state.equipment == null ? java.util.Map.of() : state.equipment);
    return state;
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(com.badlogic.gdx.Application.LOG_ERROR);
    com.perso.T4C.monster.json.MonsterJsonLoader.loadAndRegister();
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister();
    batch = new SpriteBatch();
    assetManager = new com.badlogic.gdx.assets.AssetManager();
    cursorManager = new GameCursorManager();
    try {
      com.perso
          .T4C
          .helper
          .SpriteLoader
          .getInstance()
          .loadSpriteBin(com.perso.T4C.config.Paths.SPRITE_BIN);
      customFont =
          com.perso
              .T4C
              .ui
              .FontManager
              .getInstance()
              .getT4CBeaulieuFont(22, com.badlogic.gdx.graphics.Color.WHITE);
      com.perso.T4C.audio.SoundManager.init(assetManager);
      Files.createDirectories(saveDir);
      System.setProperty("user.dir", saveDir.toString());
      PlayerStateStore.setActiveFilename("golden_player_state.json");
      PlayerStateStore.saveOrThrow("golden_player_state.json", fixture());
      screen = new MainGameScreen(this);
      Field field = MainGameScreen.class.getDeclaredField("hud");
      field.setAccessible(true);
      hud = (PlayerHUD) field.get(screen);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    setScreen(screen);
  }

  @Override
  public void render() {
    screen.render(0f);
    frame++;
    if (frame < 8 || frame % 4 != 0) return;
    if (stage >= STATES.size()) {
      probeAutosave();
      Gdx.app.exit();
      return;
    }
    save(STATES.get(stage) + ".png");
    stage++;
    switch (stage) {
      case 1 -> GuiManager.open(new Inventory(screen.getPlayer(), hud));
      case 2 -> GuiManager.open(new SpellBook(screen.getPlayer(), hud));
      case 3 -> GuiManager.open(new Statistics(screen.getPlayer(), hud));
      default -> {}
    }
  }

  /**
   * Characterization of the autosave cadence: with no save file present, 14 simulated seconds must
   * not write one and 16 must (the interval is 15 s). Runs after the screenshots, so it cannot
   * disturb them. Results go to {@code autosave.txt} for {@link MainGameScreenGoldenTest}.
   */
  private void probeAutosave() {
    String result;
    try {
      Path saveFile = saveDir.resolve("golden_player_state.json");
      Files.deleteIfExists(saveFile);
      GuiManager.close();
      screen.render(1f);
      boolean earlyWrite = Files.exists(saveFile);
      screen.render(13f);
      boolean afterFourteen = Files.exists(saveFile);
      screen.render(2f);
      boolean afterSixteen = Files.exists(saveFile);
      PlayerStateDto reloaded = afterSixteen ? PlayerStateStore.load() : null;
      result =
          String.join(
              System.lineSeparator(),
              "earlyWrite=" + earlyWrite,
              "afterFourteen=" + afterFourteen,
              "afterSixteen=" + afterSixteen,
              "reloadedName=" + (reloaded == null ? "" : reloaded.name),
              "reloadedLevel=" + (reloaded == null ? 0 : reloaded.level),
              "reloadedGold=" + (reloaded == null ? 0 : reloaded.gold));
    } catch (Throwable failure) {
      result = "error=" + failure;
    }
    try {
      Files.writeString(outputDir.resolve("autosave.txt"), result);
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void save(String name) {
    int w = Gdx.graphics.getBackBufferWidth();
    int h = Gdx.graphics.getBackBufferHeight();
    byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
    ByteBuffer target = pixmap.getPixels();
    BufferUtils.copy(pixels, 0, target, pixels.length);
    PixmapIO.writePNG(
        new com.badlogic.gdx.files.FileHandle(outputDir.resolve(name).toFile()), pixmap);
    pixmap.dispose();
    System.out.println("saved " + outputDir.resolve(name));
  }

  @Override
  public void dispose() {
    // Deliberately skip MyGame.dispose(): it would save the player over the active save file.
    if (batch != null) batch.dispose();
    try (var walk = Files.walk(saveDir)) {
      walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
    } catch (Exception ignored) {
    }
  }
}
