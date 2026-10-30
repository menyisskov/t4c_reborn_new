package com.perso.T4C.screens;

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
import com.perso.T4C.session.RemoteGameSession;
import com.perso.T4C.session.RemoteLink;
import com.perso.T4C.session.RemotePlayerCommands;
import com.perso.T4C.session.WorldEntities;
import com.perso.T4C.session.WorldEntityInputs;
import com.perso.T4C.ui.SystemMessage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Developer preview for the remote commands (T4C-0292, not shipped, not a golden): opens the REAL
 * single-player {@link MainGameScreen}, swaps its session for a {@link RemoteGameSession} over a
 * scripted link (no network, no server), fills the world from a hand-made snapshot and then
 * "clicks" the far Skeleton through the screen's own attack callback. The scripted server refuses
 * the swing (out of reach); the refusal arrives through the commands and the screen's own system
 * message shows it. The other player of the snapshot is drawn by the screen's new render slot.
 * Run from the repository root: {@code java -cp ... com.perso.T4C.screens.RemoteClickAttackPreview
 * <output-directory>}; the image is {@code remote-click-attack-refusal.png}.
 */
public final class RemoteClickAttackPreview extends MyGame {
  private final Path outputDir;
  private final Path saveDir;
  private MainGameScreen screen;
  private RemoteGameSession session;
  private final List<String> sent = new ArrayList<>();
  private int frame;

  RemoteClickAttackPreview(Path outputDir, Path saveDir) {
    this.outputDir = outputDir;
    this.saveDir = saveDir;
  }

  public static void main(String[] args) {
    Path out = Path.of(args.length > 0 ? args[0] : ".").toAbsolutePath();
    Path saves;
    try {
      Files.createDirectories(out);
      saves = Files.createTempDirectory("t4c-remote-click-save");
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Remote click attack preview");
    config.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
    config.setResizable(false);
    config.disableAudio(true);
    config.useVsync(false);
    new Lwjgl3Application(new RemoteClickAttackPreview(out, saves), config);
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
            new CreatureView(1, "Skeleton", rx(6), ry(2), 40, 40, "walk", null, null),
            new CreatureView(2, "Skeleton", rx(-5), ry(3), 14, 40, "walk", null, null));
    var npcs =
        List.of(
            new NpcView(
                LighthavenSamaritan.ID,
                "Samaritan",
                rx(-4),
                ry(-3),
                LighthavenSamaritan.SPRITE_BASE,
                Map.of()));
    var loot = List.of(new GroundLootView(UUID.randomUUID(), rx(1), ry(3), "item.dagger", null, 0));
    var players =
        List.of(
            new OnlineConnection.Actor(7, 20.5, 12.5, 1, 0, 150, 180),
            new OnlineConnection.Actor(9, rx(-2) + 0.5, ry(2) + 0.5, 1, 0, 90, 120));
    var snapshot = new OnlineConnection.Snapshot(1, players, 1L, creatures, map.region(), npcs, loot);
    return new OnlineConnection.View(7, map, snapshot, null, List.of());
  }

  /** A scripted server: every swing is refused as out of reach. */
  private final class ScriptedLink implements RemoteLink {
    private final OnlineConnection.View view = fakeView();

    @Override
    public OnlineConnection.View view() {
      return view;
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> attack(long creature) {
      sent.add("attack " + creature);
      return CompletableFuture.completedFuture(
          new OnlineConnection.CommandResult(false, null, "online.attack.out_of_reach"));
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> pickUp(UUID loot) {
      sent.add("pickup " + loot);
      return CompletableFuture.completedFuture(
          new OnlineConnection.CommandResult(false, null, "online.loot.out_of_range"));
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> interact(String npc) {
      sent.add("interact " + npc);
      return CompletableFuture.completedFuture(new OnlineConnection.CommandResult(true, null));
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> equip(
        java.util.UUID instance, com.perso.T4C.player.BodyPart slot) {
      throw new IllegalStateException("Equipment routes are outside this preview");
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> unequip(
        com.perso.T4C.player.BodyPart slot) {
      throw new IllegalStateException("Equipment routes are outside this preview");
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> cast(String spellKey) {
      throw new IllegalStateException("Spell routes are outside this preview");
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> cast(String spellKey, long creature) {
      throw new IllegalStateException("Spell routes are outside this preview");
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> castToPlayer(String spellKey,long actor) {
      throw new IllegalStateException("Spell routes are outside this preview");
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> useItem(java.util.UUID itemInstance) {
      throw new IllegalStateException("Item-use routes are outside this preview");
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> shopOffers(String npc) {
      throw new IllegalStateException("Shop routes are outside this preview");
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> shopBuy(String npc, String itemKey, int quantity) {
      throw new IllegalStateException("Shop routes are outside this preview");
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> shopSell(String npc, String itemKey, int quantity) {
      throw new IllegalStateException("Shop routes are outside this preview");
    }

    @Override
    public void direction(int dx, int dy, long frame) {}

    @Override
    public long acknowledgedFrame(long sequence) {
      return -1;
    }
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

      // From here on the screen runs an online session: its world is the snapshot driver and its
      // attack callback reaches the remote commands (what the launcher will do in step R4).
      session =
          new RemoteGameSession(new ScriptedLink(), (GroundItemManager) peek(screen, "groundItemManager"));
      Player player = (Player) peek(screen, "player");
      SystemMessage systemMessage = (SystemMessage) peek(screen, "systemMessage");
      session
          .commands()
          .bind(
              new RemotePlayerCommands.Host() {
                @Override
                public Player player() {
                  return player;
                }

                @Override
                public void showMessage(String text) {
                  systemMessage.show(text);
                }
              });
      poke(screen, "session", session);
      WorldEntities entities =
          session
              .world()
              .loadEntities(new WorldEntityInputs(null, null, null, () -> null, player));
      session
          .world()
          .tickEntities(
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
      entities.monsters().getMonsters().get(0).setSelected(true);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    setScreen(screen);
  }

  @Override
  public void render() {
    screen.render(0f);
    frame++;
    try {
      if (frame == 5) {
        // The click: the monster input handler resolves a click to a monster and calls this.
        Method attack = MainGameScreen.class.getDeclaredMethod("beginAttackTarget", BaseMonster.class);
        attack.setAccessible(true);
        var monsters = ((com.perso.T4C.monster.core.MonsterManager) peek(screen, "monsterManager"));
        attack.invoke(screen, monsters.getMonsters().get(0));
        session.commands().pump();
      }
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
    if (frame == 12) {
      save("remote-click-attack-refusal.png");
      System.out.println("requests sent: " + sent);
      Gdx.app.exit();
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
