package com.perso.T4C.screens;

import static com.perso.T4C.config.GameConstants.NEW_CHARACTER_START_TILE_X;
import static com.perso.T4C.config.GameConstants.NEW_CHARACTER_START_TILE_Y;
import static com.perso.T4C.config.GameConstants.WINDOW_HEIGHT;
import static com.perso.T4C.config.GameConstants.WINDOW_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perso.T4C.MyGame;
import com.perso.T4C.helper.GameCursorManager;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.npc.LighthavenSamaritan;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.GroundLootView;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.player.Player;
import com.perso.T4C.session.EntityTick;
import com.perso.T4C.session.RemoteWorldDriver;
import com.perso.T4C.session.WorldEntities;
import com.perso.T4C.session.WorldEntityInputs;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Developer preview for the remote world driver (T4C-0289, not shipped, not a golden): opens the
 * REAL single-player {@link MainGameScreen} on the golden fixture character, then replaces its
 * monster and NPC registries with the puppets {@link RemoteWorldDriver} builds from a hand-made
 * snapshot (no network, no server) and saves one PNG of what the real renderer draws: puppet
 * monsters (one hurt and one dead), a registered NPC and an appearance-only NPC, ground loot with
 * its names, and another player. Run from the repository root: {@code java -cp ...
 * com.perso.T4C.screens.RemoteWorldPuppetsPreview <output-directory>}; the image is {@code
 * remote-world-puppets.png}.
 */
public final class RemoteWorldPuppetsPreview extends MyGame {
  private final Path outputDir;
  private final Path saveDir;
  private MainGameScreen screen;
  private RemoteWorldDriver driver;
  private WorldEntities entities;
  private OrthographicCamera camera;
  private int frame;

  RemoteWorldPuppetsPreview(Path outputDir, Path saveDir) {
    this.outputDir = outputDir;
    this.saveDir = saveDir;
  }

  public static void main(String[] args) {
    Path out = Path.of(args.length > 0 ? args[0] : ".").toAbsolutePath();
    Path saves;
    try {
      Files.createDirectories(out);
      saves = Files.createTempDirectory("t4c-remote-world-save");
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Remote world puppets preview");
    config.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
    config.setResizable(false);
    config.disableAudio(true);
    config.useVsync(false);
    new Lwjgl3Application(new RemoteWorldPuppetsPreview(out, saves), config);
  }

  private static Object peek(Object target, String name) throws ReflectiveOperationException {
    Field field = MainGameScreen.class.getDeclaredField(name);
    field.setAccessible(true);
    return field.get(target);
  }

  private static void poke(Object target, String name, Object value)
      throws ReflectiveOperationException {
    Field field = MainGameScreen.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }

  /** Region-local tile coordinates of a tile offset from the fixture player, region origin below. */
  private static final int ORIGIN_X = NEW_CHARACTER_START_TILE_X - 20;

  private static final int ORIGIN_Y = NEW_CHARACTER_START_TILE_Y - 12;

  private static double rx(int dx) {
    return 20 + dx;
  }

  private static double ry(int dy) {
    return 12 + dy;
  }

  private OnlineConnection.View fakeView() {
    var map = new OnlineConnection.MapInfo(ORIGIN_X, ORIGIN_Y, 80, 60, true, 6, 0, 0, "avalon", 1L);
    var creatures =
        List.of(
            new CreatureView(1, "Skeleton", rx(5), ry(1), 40, 40, "walk", null, null),
            new CreatureView(2, "Skeleton", rx(-5), ry(3), 14, 40, "walk", null, null),
            new CreatureView(3, "Skeleton", rx(7), ry(-3), 0, 40, "walk", null, null));
    var npcs =
        List.of(
            new NpcView(
                LighthavenSamaritan.ID,
                "Samaritan",
                rx(-4),
                ry(-3),
                LighthavenSamaritan.SPRITE_BASE,
                Map.of()),
            new NpcView(
                "N0123456789abcdef",
                "Wanderer",
                rx(3),
                ry(-4),
                LighthavenSamaritan.SPRITE_BASE,
                Map.of()));
    var loot =
        List.of(
            new GroundLootView(UUID.randomUUID(), rx(1), ry(3), "item.dagger", null, 0),
            new GroundLootView(UUID.randomUUID(), rx(2), ry(4), "item.healing_potion", null, 0));
    var players =
        List.of(
            new OnlineConnection.Actor(7, 20.5, 12.5, 1, 0, 150, 180),
            new OnlineConnection.Actor(
                9, rx(-2) + 0.5, ry(2) + 0.5, 1, 0, 90, 120, "Brindle", "female",
                Map.of("BODY", "item.acolytes_dawnlit_robe", "SHIELD", "item.adamantite_shield_3")),
            new OnlineConnection.Actor(
                11, rx(-4) + 0.5, ry(2) + 0.5, 1, 0, 30, 120, "Garrick", "male",
                Map.of("BODY", "item.acolytes_dawnlit_robe")),
            new OnlineConnection.Actor(13, rx(-6) + 0.5, ry(2) + 0.5, 1, 0, 120, 120));
    var snapshot = new OnlineConnection.Snapshot(1, players, 1L, creatures, map.region(), npcs, loot);
    return new OnlineConnection.View(7, map, snapshot, null, List.of());
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
      PlayerStateStore.saveOrThrow("golden_player_state.json", MainGameScreenGoldenPreview.fixture());
      screen = new MainGameScreen(this);

      // The fake server snapshot, applied by the real remote world driver into the screen's own
      // ground-item registry; the screen then draws the resulting registries with its own code.
      var view = fakeView();
      driver =
          new RemoteWorldDriver(() -> view, (GroundItemManager) peek(screen, "groundItemManager"));
      Player player = (Player) peek(screen, "player");
      entities =
          driver.loadEntities(
              new WorldEntityInputs(null, null, null, () -> null, player));
      driver.tickEntities(
          new EntityTick(
              entities.npcs(),
              entities.monsters(),
              player,
              new Vector2(player.getPositionVector()),
              0.016f,
              0,
              100000,
              0,
              100000,
              5));
      poke(screen, "monsterManager", entities.monsters());
      poke(screen, "npcManager", entities.npcs());
      for (BaseMonster monster : entities.monsters().getMonsters()) monster.showNameFor(600_000L);
      for (BaseNPC npc : entities.npcs().getNPCs()) npc.showNameFor(600_000L);
      entities.monsters().getMonsters().get(1).setSelected(true);
      ((GroundItemManager) peek(screen, "groundItemManager")).showAllNames();
      camera = (OrthographicCamera) peek(screen, "camera");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    setScreen(screen);
  }

  @Override
  public void render() {
    screen.render(0f);
    // Other players have no slot in the screen yet (wiring comes later): draw the puppet players
    // with the screen's own camera so the preview shows them where they would stand.
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    // Two are drawn hovered (outline in the health colour, name above), as the screen does.
    var shader = (com.badlogic.gdx.graphics.glutils.ShaderProgram) peekQuiet(screen, "outlineShader");
    int index = 0;
    for (Player other : driver.otherPlayers()) {
      boolean hovered = index++ < 2;
      other.render(batch, shader, hovered);
      com.perso.T4C.entity.RemotePlayerNameplate.render(batch, other, hovered);
    }
    batch.end();
    frame++;
    if (frame == 10) {
      save("remote-world-puppets.png");
      Gdx.app.exit();
    }
  }

  private static Object peekQuiet(Object target, String field) {
    try {
      return peek(target, field);
    } catch (Exception e) {
      return null;
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
