package com.perso.T4C.online;

import static com.perso.T4C.config.GameConstants.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.AppearanceDefaultsCatalog;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.MonsterAnimations;
import com.perso.T4C.npc.core.NPCAnimations;
import com.perso.T4C.objects.GroundItem;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.GroundLootView;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.PlayerAnimations;
import com.perso.T4C.player.PlayerMovement;
import com.perso.T4C.render.ObjectRenderer;
import com.perso.T4C.screens.MapRenderer;
import com.perso.T4C.screens.OnlineLobbyScreen;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Native online entry point sharing the offline renderer and sprites, with no local player model.
 */
public final class OnlineClient extends ApplicationAdapter {
  private final URI endpoint;
  private OnlineConnection.Credentials credentials;
  private volatile boolean focused = true;
  private final java.util.Set<Integer> inputKeys = new HashSet<>();
  private OnlineConnection connection;
  private MapReader map;
  private MapRenderer renderer;
  private SpriteBatch ground, decor, hud;
  private BitmapFont font;
  private final OrthographicCamera camera = new OrthographicCamera();
  private final OrthographicCamera hudCamera = new OrthographicCamera();
  private final Map<Long, Avatar> avatars = new HashMap<>();
  private final Map<Long, CreatureAvatar> creatures = new HashMap<>();
  private final Map<String, NpcAvatar> npcs = new HashMap<>();
  private Set<String> npcFrameNames = Set.of();
  private OnlineConnection.Snapshot lastNpcSnapshot;
  /** Ground drops of the current snapshot, keyed by their server identity. Textures are shared. */
  private final Map<java.util.UUID, GroundItem> groundLoot = new HashMap<>();
  /** Same reach as offline pickup (OBJECT_MAX_INTERACTION_DISTANCE); the server enforces it again. */
  private static final float PICKUP_RANGE_TILES = 8f;
  private static final long MESSAGE_MS = 3000L;
  /** Render-thread state. Network callbacks only enqueue outcomes to {@link #staged}. */
  private String hudMessage;
  private long hudMessageUntil;
  private final ConcurrentLinkedQueue<Staged> staged = new ConcurrentLinkedQueue<>();
  private boolean talkOpen;
  private boolean inventoryOpen;
  /** Spell and potion lists (C / U); plain HUD text, closed with the other panels. */
  private final OnlineAbilityMenu abilityMenu = new OnlineAbilityMenu();
  private String talkNpc;
  private String talkName;
  private String talkText;
  private List<Integer> talkTopics = List.of();
  private long lastTick = -1;
  private long targetCreature;
  private String error;
  private int renderedWorld;
  private String renderedRegion;
  private long renderedEpoch=-1;
  /** Opt-in lobby (see {@link OnlineLobbyLaunch}); all null in the default launch. */
  private OnlineLobbyModel lobbyModel;
  /** The account key; never logged or drawn. Only handed to the account client and the model. */
  private String lobbyAccountKey;
  private OnlineLobbyScreen lobby;
  private Runnable deferred;
  private com.badlogic.gdx.InputProcessor gameInput;

  private static final class CreatureAvatar {
    final OnlineCreatureMotion motion = new OnlineCreatureMotion();
    final MonsterAnimations animation;
    CreatureView state;

    CreatureAvatar(CreatureView state) throws com.perso.T4C.exception.GameException {
      this.state = state;
      animation = new MonsterAnimations(state.walkPattern(), state.attackPattern(), state.deathPattern(), null, null);
    }
  }

  private static final class NpcAvatar {
    final NPCAnimations animation;
    final Vector2 position = new Vector2();
    final PlayerMovement facing = new PlayerMovement();
    final Rectangle bounds = new Rectangle();
    final String spriteBase;
    final Map<String, String> parts;
    boolean moving;

    NpcAvatar(NpcView view, Set<String> frameNames) throws com.perso.T4C.exception.GameException {
      spriteBase = view.spriteBase();
      parts = view.parts();
      if ("@invisible".equals(spriteBase)) {
        animation = null;
        return;
      }
      var typed = new java.util.EnumMap<BodyPart, String>(BodyPart.class);
      parts.forEach((part, base) -> typed.put(BodyPart.valueOf(part), base));
      if (!NPCAnimations.hasRequiredFrameNames(spriteBase, typed, frameNames))
        throw new IllegalArgumentException("Missing required online NPC sprite: " + view.id());
      var arguments = new ArrayList<Object>();
      typed.forEach((part, base) -> { arguments.add(part); arguments.add(base); });
      animation = new NPCAnimations(spriteBase, arguments.toArray());
    }

    boolean sameAppearance(NpcView view) {
      return java.util.Objects.equals(spriteBase, view.spriteBase()) && parts.equals(view.parts());
    }

    void position(float x, float y, boolean first) {
      moving = !first && (x != position.x || y != position.y);
      if (moving) facing.faceToward(position.x, position.y, x, y);
      position.set(x, y);
    }

    boolean objectAppearance() { return spriteBase != null && spriteBase.startsWith("@static:"); }
    void dispose() { if (animation != null) animation.dispose(); }
  }

  private static final class Avatar {
    final Vector2 position = new Vector2();
    final PlayerAnimations animation;
    final PlayerMovement facing = new PlayerMovement();
    boolean moving;

    Avatar(long id) {
      var parts = new ArrayList<Object>();
      AppearanceDefaultsCatalog.nakedParts(
              id % 2 == 0 ? AppearanceDefaultsCatalog.FEMALE : AppearanceDefaultsCatalog.MALE)
          .forEach(
              (part, sprite) -> {
                parts.add(part);
                parts.add(sprite);
              });
      animation = new PlayerAnimations(parts.toArray());
    }

    void position(float x, float y, boolean first) {
      moving = !first && (x != position.x || y != position.y);
      if (moving) facing.faceToward(position.x, position.y, x, y);
      position.set(x, y);
    }
  }

  public OnlineClient(URI endpoint) {
    this.endpoint = endpoint;
  }

  public static void main(String[] args) {
    OnlineNativeLibrary.configure();
    boolean lobbyRequested =
        OnlineLobbyLaunch.requested(
            System.getProperty(OnlineLobbyLaunch.PROPERTY), System.getenv(OnlineLobbyLaunch.ENVIRONMENT), args);
    args = OnlineLobbyLaunch.positional(args);
    URI endpoint = URI.create(args.length == 0 ? "http://127.0.0.1:18765" : args[0]);
    var client = new OnlineClient(endpoint);
    String profile = System.getenv("T4C_ONLINE_PROFILE");
    OnlineAccountProfile loadedProfile = null;
    if (profile != null && !profile.isBlank()) {
      try {
        loadedProfile = OnlineAccountProfile.load(java.nio.file.Path.of(profile));
      } catch (Exception failure) {
        throw new IllegalArgumentException("Could not read valid online profile");
      }
    }
    if (OnlineLobbyLaunch.select(lobbyRequested, loadedProfile != null) == OnlineLobbyLaunch.Mode.LOBBY) {
      client.lobbyAccountKey = loadedProfile.secret();
      client.lobbyModel = loadedProfile.lobbyModel();
      // Nothing connects until the player confirms a character.
    } else if (loadedProfile != null) {
      client.credentials = loadedProfile.directCredentials();
    }
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle(I18n.key("online.client.title"));
    config.setWindowedMode(960, 640);
    config.setResizable(true);
    config.useVsync(true);
    config.disableAudio(true);
    config.setWindowListener(
        new Lwjgl3WindowAdapter() {
          @Override
          public void focusLost() {
            client.focused = false;
            client.inputKeys.clear();
            if (client.connection != null) client.connection.stopInput();
          }

          @Override
          public void focusGained() {
            client.focused = true;
          }
        });
    new Lwjgl3Application(client, config);
  }

  @Override
  public void create() {
    ground = new SpriteBatch();
    decor = new SpriteBatch();
    hud = new SpriteBatch();
    font = new BitmapFont();
    try {
      var sprites = SpriteLoader.getInstance();
      sprites.loadSpriteBin(Paths.SPRITE_BIN);
      var availableNpcFrames = new HashSet<String>();
      for (var name : sprites.getNameToId().keySet()) {
        var metadata = sprites.getMetaByLowerName().get(name);
        if (metadata != null && metadata.pngData != null && metadata.pngData.length > 0)
          availableNpcFrames.add(name);
      }
      npcFrameNames = Set.copyOf(availableNpcFrames);
      map = new MapReader(new File(Paths.MAP));
      renderer = new MapRenderer(map, sprites, ground, decor, null, ModifSprites.empty(), 0);
      camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      camera.position.set(
          NEW_CHARACTER_START_TILE_X * GRID_W, NEW_CHARACTER_START_TILE_Y * GRID_H, 0);
      camera.update();
      if (lobbyModel == null) reconnect();
      gameInput =
          new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int code) {
              if (!focused || connection == null) return false;
              if (code == Input.Keys.T) {
                if (talkOpen) closePanels(); else { closePanels(); talkNearest(); }
                return true;
              }
              if (code == Input.Keys.I) { toggleInventory(); return true; }
              if (code == Input.Keys.C) { toggleAbilityMenu(OnlineAbilityMenu.Kind.SPELLS); return true; }
              if (code == Input.Keys.U) { toggleAbilityMenu(OnlineAbilityMenu.Kind.POTIONS); return true; }
              // Digits belong to an open panel only; with no panel open they fall through untouched.
              int digit = digitFor(code);
              if (digit > 0 && talkOpen) { chooseTopic(digit); return true; }
              if (digit > 0 && inventoryOpen) { chooseRow(digit); return true; }
              if (digit > 0 && abilityMenu.isOpen()) { chooseAbility(digit); return true; }
              if (code == Input.Keys.SPACE) { attackNearest(); return true; }
              if (code == Input.Keys.E) { recoverNearest(); return true; }
              if (code == Input.Keys.G) { pickUpNearest(); return true; }
              if (code != Input.Keys.W
                  && code != Input.Keys.A
                  && code != Input.Keys.S
                  && code != Input.Keys.D
                  && code != Input.Keys.UP
                  && code != Input.Keys.DOWN
                  && code != Input.Keys.LEFT
                  && code != Input.Keys.RIGHT) return false;
              inputKeys.add(code);
              int dx =
                  (inputKeys.contains(Input.Keys.D) || inputKeys.contains(Input.Keys.RIGHT) ? 1 : 0)
                      - (inputKeys.contains(Input.Keys.A) || inputKeys.contains(Input.Keys.LEFT)
                          ? 1
                          : 0);
              int dy =
                  (inputKeys.contains(Input.Keys.S) || inputKeys.contains(Input.Keys.DOWN) ? 1 : 0)
                      - (inputKeys.contains(Input.Keys.W) || inputKeys.contains(Input.Keys.UP)
                          ? 1
                          : 0);
              connection.tap(dx, dy);
              return true;
            }

            @Override
            public boolean keyUp(int code) {
              return inputKeys.remove(code);
            }
          };
      Gdx.input.setInputProcessor(gameInput);
      if (lobbyModel != null) openLobby();
    } catch (Exception failure) {
      error = I18n.key("online.client.load_error");
      Gdx.app.error("online", "Could not load online client", failure);
    }
  }

  /** Shows the lobby with a fresh account client (the screen closes it on dispose). */
  private void openLobby() {
    try {
      lobby =
          OnlineLobbyScreen.forAccountClient(
              hud,
              lobbyModel,
              new OnlineAccountClient(endpoint, lobbyAccountKey),
              new OnlineLobbyScreen.Listener() {
                @Override
                public void play(OnlineConnection.Credentials chosen) {
                  deferred = () -> startPlaying(chosen);
                }

                @Override
                public void back() {
                  deferred = Gdx.app::exit;
                }
              });
      lobby.show();
      lobby.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    } catch (Exception failure) {
      error = I18n.key("online.client.load_error");
      Gdx.app.error("online", "Could not open online lobby", failure); // Never includes the key.
    }
  }

  private void closeLobby() {
    if (lobby == null) return;
    lobby.hide();
    lobby.dispose();
    lobby = null;
  }

  private void startPlaying(OnlineConnection.Credentials chosen) {
    closeLobby();
    credentials = chosen;
    Gdx.input.setInputProcessor(gameInput);
    reconnect();
  }

  /**
   * Lobby mode only: end the session and show the lobby, which refuses an immediate rejoin until the
   * conservative client-side lease hold has passed.
   */
  private void returnToLobby() {
    lobbyModel.beginReturnFromGame();
    if (connection != null) connection.close();
    connection = null;
    credentials = null;
    avatars.values().forEach(a -> a.animation.dispose());
    avatars.clear();
    creatures.clear();
    groundLoot.clear();
    inputKeys.clear();
    closePanels();
    hudMessage = null;
    staged.clear();
    lastTick = -1;
    targetCreature = 0;
    error = null;
    lobbyModel.finishReturnFromGame(OnlineLobbyLaunch.leaseDeadline(System.currentTimeMillis()));
    openLobby();
  }

  private void reconnect() {
    if (connection != null) connection.close();
    avatars.values().forEach(a -> a.animation.dispose());
    avatars.clear();
    creatures.clear(); // Textures belong to the shared SpriteLoader.
    clearNpcs();
    groundLoot.clear();
    error = null;
    lastTick = -1;
    targetCreature = 0;
    closePanels();
    hudMessage = null;
    staged.clear();
    connection = new OnlineConnection(endpoint, credentials);
  }

  private void attackNearest() {
    var view=connection.view();
    if (view==null || view.character()==null || !"connected".equals(connection.status())) return;
    var own=view.snapshot().players().stream().filter(a -> a.id()==view.ownId()).findFirst().orElse(null);
    if (own==null) return;
    var nearest=view.snapshot().creatures().stream().filter(c -> c.health()>0)
        .min(java.util.Comparator.comparingDouble(c -> Math.hypot((c.x()-own.x())*GRID_W,(c.y()-own.y())*GRID_H)))
        .orElse(null);
    if (nearest==null) return;
    targetCreature=nearest.id();
    try {
      connection.attack(targetCreature)
          .thenAccept(result -> { if (!OnlineCommandRefusals.ATTACK_COOLDOWN.equals(result.messageKey())) stageCommand(result); })
          .exceptionally(failed -> null);
    }
    catch (IllegalStateException pendingOrDisconnected) { /* Single-flight input is intentionally bounded. */ }
  }

  private int key(int positive, int alternatePositive, int negative, int alternateNegative) {
    return (Gdx.input.isKeyPressed(positive) || Gdx.input.isKeyPressed(alternatePositive) ? 1 : 0)
        - (Gdx.input.isKeyPressed(negative) || Gdx.input.isKeyPressed(alternateNegative) ? 1 : 0);
  }

  private OnlineConnection.Corpse nearestCorpse(OnlineConnection.View view) {
    var own=view.snapshot().players().stream().filter(a -> a.id()==view.ownId()).findFirst().orElse(null);
    if (own==null) return null;
    return view.corpses().stream().filter(c -> c.world()==view.map().world())
        .min(java.util.Comparator.comparingDouble(c -> corpseDistance(view,own,c))).orElse(null);
  }

  private double corpseDistance(OnlineConnection.View view,OnlineConnection.Actor own,OnlineConnection.Corpse corpse) {
    return Math.hypot((corpse.x()-view.map().originX()-own.x())*GRID_W,
        (corpse.y()-view.map().originY()-own.y())*GRID_H);
  }

  private void recoverNearest() {
    var view=connection.view();
    if (view==null || view.character()==null || !"connected".equals(connection.status())) return;
    var nearest=nearestCorpse(view);
    if (nearest==null) return;
    try { connection.recover(nearest.id()).thenAccept(this::stageCommand).exceptionally(failed -> null); }
    catch (IllegalStateException pendingOrDisconnected) { /* Single-flight commands stay bounded. */ }
  }

  /** Nearest drop within reach. The server still decides ownership, capacity and the item instance. */
  private void pickUpNearest() {
    var view=connection.view();
    if (view==null || view.character()==null || !"connected".equals(connection.status())) return;
    var own=view.snapshot().players().stream().filter(a -> a.id()==view.ownId()).findFirst().orElse(null);
    if (own==null) return;
    var nearest=view.snapshot().loot().stream()
        .filter(drop -> lootDistance(own,drop) <= PICKUP_RANGE_TILES)
        .min(java.util.Comparator.comparingDouble(drop -> lootDistance(own,drop)))
        .orElse(null);
    if (nearest==null) return;
    try {
      connection.pickUp(nearest.id())
          .thenAccept(this::stageCommand)
          .exceptionally(failed -> null);
    }
    catch (IllegalStateException pendingOrDisconnected) { /* Single-flight commands stay bounded. */ }
  }

  /** Chebyshev tile distance, matching the server's reach check. Both sides are region-local tiles. */
  private static double lootDistance(OnlineConnection.Actor own, GroundLootView drop) {
    return Math.max(Math.abs(drop.x()-own.x()), Math.abs(drop.y()-own.y()));
  }

  private void showMessage(String key, Object... args) {
    hudMessage = args.length == 0 ? I18n.key(key) : I18n.message(key, args);
    hudMessageUntil = System.currentTimeMillis() + MESSAGE_MS;
  }

  private record Staged(boolean talk, boolean accepted, String npc, String messageKey, List<Integer> args) {}

  /** Network thread: enqueue only. The render thread applies the outcome in {@link #applyStaged()}. */
  private void stageCommand(OnlineConnection.CommandResult result) {
    staged.add(new Staged(false, false, null, result.messageKey(), result.args()));
  }

  private void applyStaged() {
    Staged outcome;
    while ((outcome = staged.poll()) != null) {
      if (outcome.talk() && outcome.accepted() && outcome.messageKey() != null)
        openTalk(outcome.npc(), outcome.messageKey(), outcome.args());
      else if (outcome.messageKey() != null) showMessage(outcome.messageKey(), outcome.args().toArray());
    }
  }

  private void closePanels() {
    talkOpen = false;
    inventoryOpen = false;
    abilityMenu.close();
    talkText = null;
    talkTopics = List.of();
  }

  private void openTalk(String npc, String messageKey, List<Integer> args) {
    talkText = I18n.message(messageKey, args.toArray());
    talkNpc = npc;
    talkTopics = OnlineNpcTopics.available(npc, I18n::has);
    talkOpen = true;
    inventoryOpen = false;
  }

  /** Nearest NPC inside the server's interaction range. The server still decides dialogue and proximity. */
  private void talkNearest() {
    var view = connection.view();
    if (view == null || view.character() == null || !"connected".equals(connection.status())) {
      showMessage("online.talk.not_connected");
      return;
    }
    var own = ownActor(view);
    if (own == null) return;
    var nearest = view.snapshot().npcs().stream()
        .filter(npc -> !"@invisible".equals(npc.spriteBase()))
        .filter(npc -> npcDistanceSquared(own, npc) < NPC_INTERACTION_RANGE_TILES_SQUARED)
        .min(Comparator.comparingDouble(npc -> npcDistanceSquared(own, npc)))
        .orElse(null);
    if (nearest == null) {
      showMessage("online.talk.none");
      return;
    }
    talkName = nearest.displayName();
    startTalk(nearest.id(), OptionalInt.empty());
  }

  private void chooseTopic(int digit) {
    var topic = OnlineNpcTopics.choose(talkTopics, digit);
    if (topic.isPresent() && talkNpc != null) startTalk(talkNpc, topic);
  }

  private void startTalk(String npc, OptionalInt topic) {
    try {
      connection
          .interact(npc, topic)
          .thenAccept(
              result ->
                  staged.add(
                      new Staged(true, result.accepted(), npc, result.messageKey(), result.args())))
          .exceptionally(failed -> null);
    } catch (IllegalStateException pendingOrDisconnected) {
      showMessage("online.npc.refused");
    }
  }

  private void toggleInventory() {
    boolean open = !inventoryOpen;
    closePanels();
    inventoryOpen = open;
  }

  private CharacterProgress ownProgress() {
    var view = connection == null ? null : connection.view();
    return view == null || view.character() == null ? null : view.character().progress();
  }

  /** C / U: open the spell or potion list. Ignored while dead or disconnected. */
  private void toggleAbilityMenu(OnlineAbilityMenu.Kind kind) {
    var progress = ownProgress();
    boolean available = OnlineAbilityMenu.available(connection.status(), progress);
    if (!available) { abilityMenu.close(); return; }
    boolean wasOpen = abilityMenu.kind() == kind;
    closePanels();
    if (!wasOpen) abilityMenu.toggle(kind, true);
  }

  /** Digit in an open ability list: cast or use that row. The server decides everything else. */
  private void chooseAbility(int digit) {
    var progress = ownProgress();
    boolean available = OnlineAbilityMenu.available(connection.status(), progress);
    var action = abilityMenu.choose(digit, progress, OnlineSpellAllowlist.bundled(), available).orElse(null);
    if (action == null) return;
    try {
      var pending = action.kind() == OnlineAbilityMenu.Kind.SPELLS
          ? connection.cast(action.spellKey())
          : connection.useItem(action.item());
      pending.thenAccept(this::stageCommand).exceptionally(failed -> null);
    } catch (IllegalStateException pendingOrDisconnected) {
      showMessage("online.ability.busy");
    }
  }

  /** Equip a carried item into its authored slot, or unequip an equipped slot. The server decides both. */
  private void chooseRow(int digit) {
    var view = connection.view();
    var progress = view == null || view.character() == null ? null : view.character().progress();
    if (progress == null || !"connected".equals(connection.status())) {
      showMessage("online.inventory.not_connected");
      return;
    }
    var row =
        OnlineInventoryRows.choose(OnlineInventoryRows.of(progress.inventory(), progress.equipment()), digit)
            .orElse(null);
    if (row == null) return;
    try {
      if (row.kind() == OnlineInventoryRows.Kind.EQUIPPED) {
        connection.unequip(BodyPart.valueOf(row.slot())).thenAccept(this::stageCommand).exceptionally(failed -> null);
      } else {
        var definition = ItemRegistry.findByKey(row.itemKey());
        if (definition == null || definition.getBodyPart() == null) {
          showMessage("online.equip.no_slot");
          return;
        }
        connection
            .equip(row.instance(), definition.getBodyPart())
            .thenAccept(this::stageCommand)
            .exceptionally(failed -> null);
      }
    } catch (IllegalStateException pendingOrDisconnected) {
      showMessage("online.equip.refused");
    }
  }

  private static OnlineConnection.Actor ownActor(OnlineConnection.View view) {
    return view.snapshot().players().stream().filter(a -> a.id() == view.ownId()).findFirst().orElse(null);
  }

  private static double npcDistanceSquared(OnlineConnection.Actor own, NpcView npc) {
    double dx = npc.x() - own.x(), dy = npc.y() - own.y();
    return dx * dx + dy * dy;
  }

  /** Top-row number keys 1-9 as digits 1-9; any other key is 0. */
  private static int digitFor(int code) {
    return code >= Input.Keys.NUM_1 && code <= Input.Keys.NUM_9 ? code - Input.Keys.NUM_1 + 1 : 0;
  }

  /** Talk and inventory are drawn as plain text in the HUD; only one panel is open at a time. */
  private void drawPanels(OnlineConnection.View view) {
    float width = Gdx.graphics.getWidth(), height = Gdx.graphics.getHeight();
    float line = font.getLineHeight();
    if (talkOpen && talkText != null) {
      float y = height - 130;
      font.draw(hud, I18n.message("online.talk.header", talkName == null ? "" : talkName), 16, y);
      y -= line * 1.5f;
      var layout = font.draw(hud, talkText, 16, y, width - 32, Align.left, true);
      y -= layout.height + line;
      for (int i = 0; i < talkTopics.size(); i++) {
        font.draw(hud, I18n.message("online.talk.topic", i + 1, talkTopics.get(i)), 16, y);
        y -= line;
      }
      font.draw(hud, I18n.key("online.talk.close"), 16, y);
    } else if (abilityMenu.isOpen()) {
      var progress = view == null || view.character() == null ? null : view.character().progress();
      OnlineAbilityMenu.draw(hud, font, abilityMenu.lines(progress, OnlineSpellAllowlist.bundled()), width - 360, height - 16);
    } else if (inventoryOpen) {
      float x = width - 360, y = height - 16;
      font.draw(hud, I18n.key("online.inventory.title"), x, y);
      y -= line * 1.5f;
      var progress = view == null || view.character() == null ? null : view.character().progress();
      if (progress == null) {
        font.draw(hud, I18n.key("online.inventory.not_connected"), x, y);
        y -= line;
      } else {
        var rows = OnlineInventoryRows.of(progress.inventory(), progress.equipment());
        if (rows.isEmpty()) {
          font.draw(hud, I18n.key("online.inventory.empty"), x, y);
          y -= line;
        }
        for (int i = 0; i < rows.size() && i < OnlineInventoryRows.MAX_CHOICES; i++) {
          var row = rows.get(i);
          String name = I18n.key(row.itemKey(), row.itemKey());
          String text = row.kind() == OnlineInventoryRows.Kind.EQUIPPED
              ? I18n.message("online.inventory.unequip_row", i + 1, name, row.slot())
              : I18n.message("online.inventory.equip_row", i + 1, name);
          font.draw(hud, text, x, y);
          y -= line;
        }
        if (rows.size() > OnlineInventoryRows.MAX_CHOICES) {
          font.draw(hud, I18n.message("online.inventory.more", rows.size() - OnlineInventoryRows.MAX_CHOICES), x, y);
          y -= line;
        }
      }
      font.draw(hud, I18n.key("online.inventory.close"), x, y - line * 0.5f);
    }
  }

  /** Keeps one render object per current drop; positions use the same region-to-world mapping as creatures. */
  private void syncGroundLoot(OnlineConnection.View view) {
    var present=new HashSet<java.util.UUID>();
    for (var drop : view.snapshot().loot()) {
      present.add(drop.id());
      var item=groundLoot.computeIfAbsent(drop.id(), ignored -> GroundItem.ofItem(drop.key(), 0f, 0f));
      item.getPosition().set((float) (view.map().originX()+drop.x())*GRID_W,
          (float) (view.map().originY()+drop.y())*GRID_H);
    }
    groundLoot.keySet().retainAll(present);
  }

  private void clearNpcs() {
    npcs.values().forEach(NpcAvatar::dispose);
    npcs.clear();
    lastNpcSnapshot = null;
  }

  /** Prepare authored appearances on the render thread before region readiness is acknowledged. */
  private void syncNpcs(OnlineConnection.View view) throws com.perso.T4C.exception.GameException {
    if (lastNpcSnapshot == view.snapshot()) return;
    var present = new HashSet<String>();
    for (var state : view.snapshot().npcs()) {
      present.add(state.id());
      var npc = npcs.get(state.id());
      boolean first = npc == null || !npc.sameAppearance(state);
      if (first) {
        var replacement = new NpcAvatar(state, npcFrameNames);
        npcs.put(state.id(), replacement);
        if (npc != null) npc.dispose();
        npc = replacement;
      }
      npc.position((float) (view.map().originX() + state.x()) * GRID_W,
          (float) (view.map().originY() + state.y()) * GRID_H, first);
    }
    npcs.entrySet().removeIf(entry -> {
      if (present.contains(entry.getKey())) return false;
      entry.getValue().dispose();
      return true;
    });
    lastNpcSnapshot = view.snapshot();
  }

  /** Install only allowlisted local assets, on the render thread, before acknowledging a region. */
  private void prepareRegion(OnlineConnection.View view) throws Exception {
    var region=view.map();
    if (renderedWorld!=region.world()) {
      var definition=java.util.Arrays.stream(com.perso.T4C.config.MapDefinition.values())
          .filter(candidate -> candidate.getZ()==region.world()).findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Unknown online world"));
      var nextMap=new MapReader(new File(definition.getMapPath()));
      MapRenderer nextRenderer;
      try { nextRenderer=new MapRenderer(nextMap,SpriteLoader.getInstance(),ground,decor,null,ModifSprites.empty(),region.world()); }
      catch (Exception failure) { nextMap.close(); throw failure; }
      if (renderer!=null) renderer.dispose();
      if (map!=null) map.close();
      renderer=nextRenderer; map=nextMap;
    }
    boolean changed = renderedWorld!=region.world() || !java.util.Objects.equals(renderedRegion,region.key()) || renderedEpoch!=region.locationEpoch();
    if (changed) {
      avatars.values().forEach(a -> a.animation.dispose()); avatars.clear(); creatures.clear(); groundLoot.clear(); clearNpcs();
      inputKeys.clear(); connection.stopInput(); lastTick=-1; targetCreature=0; closePanels();
      renderedWorld=region.world(); renderedRegion=region.key(); renderedEpoch=region.locationEpoch();
    }
    syncNpcs(view);
    if (changed) Gdx.app.log("online", "Prepared " + npcs.size() + " online NPC appearances for region " + region.key());
    connection.confirmRegionReady(region.locationEpoch());
  }

  @Override
  public void render() {
    if (deferred != null) {
      var action = deferred;
      deferred = null;
      action.run();
    }
    if (lobby != null) {
      lobby.render(Gdx.graphics.getDeltaTime());
      return;
    }
    Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    // Esc closes an open panel first; with no panel open it exits, or returns to the lobby in lobby mode.
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      if (talkOpen || inventoryOpen || abilityMenu.isOpen()) closePanels();
      else if (lobbyModel != null) { returnToLobby(); return; }
      else Gdx.app.exit();
    }
    applyStaged();
    if (connection != null
        && Gdx.input.isKeyJustPressed(Input.Keys.R)
        && !"connected".equals(connection.status())) reconnect();
    var view = connection == null ? null : connection.view();
    if (view!=null) {
      try { prepareRegion(view); }
      catch (Exception failure) {
        error=I18n.key("online.client.load_error");
        Gdx.app.error("online","Could not load online region",failure);
        clearNpcs();
        connection.close(); return;
      }
    }
    if (connection != null)
      connection.direction(
          focused ? key(Input.Keys.RIGHT, Input.Keys.D, Input.Keys.LEFT, Input.Keys.A) : 0,
          focused ? key(Input.Keys.DOWN, Input.Keys.S, Input.Keys.UP, Input.Keys.W) : 0);
    if (view != null && view.snapshot().tick() != lastTick) {
      lastTick = view.snapshot().tick();
      var present = new HashSet<Long>();
      for (var actor : view.snapshot().players()) {
        present.add(actor.id());
        boolean first = !avatars.containsKey(actor.id());
        var avatar = avatars.computeIfAbsent(actor.id(), ignored -> new Avatar(actor.appearance()));
        avatar.position(
            (float) (view.map().originX() + actor.x()) * GRID_W,
            (float) (view.map().originY() + actor.y()) * GRID_H,
            first);
      }
      avatars
          .entrySet()
          .removeIf(
              e -> {
                if (present.contains(e.getKey())) return false;
                e.getValue().animation.dispose();
                return true;
              });
      var own = avatars.get(view.ownId());
      if (own != null) camera.position.set(own.position.x, own.position.y, 0);
      var presentCreatures = new HashSet<Long>();
      try {
        for (var actor : view.snapshot().creatures()) {
          presentCreatures.add(actor.id());
          var creature = creatures.get(actor.id());
          if (creature == null) {
            creature = new CreatureAvatar(actor);
            creatures.put(actor.id(), creature);
          }
          creature.state = actor;
          creature.motion.snapshot((float) (view.map().originX() + actor.x()) * GRID_W,
              (float) (view.map().originY() + actor.y()) * GRID_H);
        }
        creatures.keySet().retainAll(presentCreatures);
        syncGroundLoot(view);
      } catch (Exception failure) {
        error = I18n.key("online.client.load_error");
        Gdx.app.error("online", "Could not load online creature", failure);
        connection.close();
        creatures.clear();
      }
    }
    if (view == null) {
      closePanels();
      avatars.values().forEach(a -> a.moving = false);
      creatures.values().forEach(c -> c.motion.stop());
      groundLoot.clear();
      clearNpcs();
    }
    camera.update();
    if (renderer != null) {
      int x = (int) (camera.position.x / GRID_W), y = (int) (camera.position.y / GRID_H);
      int rx = (int) camera.viewportWidth / GRID_W / 2 + 18,
          ry = (int) camera.viewportHeight / GRID_H / 2 + 18;
      int x0 = Math.max(0, x - rx), x1 = Math.min(map.getWidth() - 1, x + rx);
      int y0 = Math.max(0, y - ry), y1 = Math.min(map.getHeight() - 1, y + ry);
      renderer.warmCaches(x0, x1, y0, y1, 32, 2048, 512);
      renderer.renderGroundOnly(camera, x0, x1, y0, y1);
      var actors = new ArrayList<ObjectRenderer.RenderItem>();
      for (var avatar : avatars.values()) {
        avatar.animation.update(Math.min(Gdx.graphics.getDeltaTime(), 0.1f), avatar.moving);
        var item = new ObjectRenderer.RenderItem();
        item.y = (float) Math.floor(avatar.position.y / GRID_H) + 2f;
        item.renderAction =
            () ->
                avatar.animation.render(
                    decor,
                    avatar.position,
                    avatar.facing.getCurrentAngle(),
                    avatar.facing.isFlipX(),
                    avatar.moving);
        actors.add(item);
      }
      for (var creature : creatures.values()) {
        creature.animation.update(Math.min(Gdx.graphics.getDeltaTime(), 0.1f), creature.motion.moving());
        var item = new ObjectRenderer.RenderItem();
        item.y = (float) Math.floor(creature.motion.position().y / GRID_H) + 2f;
        item.renderAction = () -> creature.animation.render(decor, creature.motion.position(),
            creature.motion.angle(), creature.motion.flipX(), creature.motion.moving(),
            false, null, (float) creature.state.health() / creature.state.maxHealth());
        actors.add(item);
      }
      for (var npc : npcs.values()) {
        if (npc.animation == null) continue; // Authored invisible NPCs remain non-rendered.
        npc.animation.update(Math.min(Gdx.graphics.getDeltaTime(), 0.1f), npc.moving);
        String angle = npc.facing.getCurrentAngle();
        boolean flipX = npc.facing.isFlipX();
        npc.animation.getRenderBounds(npc.position, angle, flipX, npc.moving, npc.bounds);
        var item = new ObjectRenderer.RenderItem();
        item.y = (float) Math.floor(npc.position.y / GRID_H)
            + (npc.objectAppearance() ? 0.5f : 2f);
        item.renderAction = () -> npc.animation.renderComposite(
            decor, npc.position, angle, flipX, npc.moving);
        item.revealThroughDecor = true;
        item.opaqueOcclusionReveal = npc.objectAppearance();
        item.occlusionRevealAction = item.renderAction;
        item.revealX = npc.bounds.x;
        item.revealY = npc.bounds.y;
        item.revealW = npc.bounds.width;
        item.revealH = npc.bounds.height;
        actors.add(item);
      }
      for (var drop : groundLoot.values()) {
        var item = new ObjectRenderer.RenderItem();
        item.isObject = false;
        item.y = drop.getDepthY();
        item.renderAction = () -> drop.render(decor, null);
        actors.add(item);
      }
      renderer.renderEntitiesWithDecors(camera, -1, -1, x0, x1, y0, y1, actors);
    }
    hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    hud.setProjectionMatrix(hudCamera.combined);
    hud.begin();
    font.setColor(Color.WHITE);
    String state =
        error != null
            ? error
            : I18n.key(
                "online.client." + (connection == null ? "connecting" : connection.status()));
    if (error == null
        && view != null
        && view.map().persistent()
        && "connected".equals(connection.status())) state = I18n.key("online.client.persistent");
    font.draw(hud, state, 16, Gdx.graphics.getHeight() - 16);
    font.draw(hud, I18n.key(view!=null && view.map().persistent() ? "online.client.combat_controls" : "online.client.controls"), 16, 24);
    if (hudMessage!=null && System.currentTimeMillis()<hudMessageUntil)
      font.draw(hud, hudMessage, 16, 46);
    drawPanels(view);
    if (view != null)
      font.draw(
          hud,
          I18n.message("online.client.players", view.ownId(), avatars.size()),
          16,
          Gdx.graphics.getHeight() - 38);
    if (view != null && view.character() != null && view.character().progress() != null) {
      var character=view.character();
      font.draw(hud,OnlineHudText.progressLine(character.progress(),character.experience(),character.gold()),
          16,Gdx.graphics.getHeight()-60);
      view.snapshot().creatures().stream().filter(c -> c.id()==targetCreature).findFirst().ifPresent(c ->
          font.draw(hud,I18n.message("online.client.target_health",c.health(),c.maxHealth()),16,Gdx.graphics.getHeight()-82));
      var corpse=nearestCorpse(view);
      var own=view.snapshot().players().stream().filter(a -> a.id()==view.ownId()).findFirst().orElse(null);
      if (corpse!=null && own!=null)
        font.draw(hud,I18n.message("online.client.corpse",Math.round(corpseDistance(view,own,corpse)),corpse.itemCount(),corpse.gold()),
            16,Gdx.graphics.getHeight()-104);
    }
    hud.end();
  }

  @Override
  public void resize(int width, int height) {
    camera.viewportWidth = Math.max(1, width);
    camera.viewportHeight = Math.max(1, height);
    camera.update();
  }

  @Override
  public void dispose() {
    closeLobby();
    if (connection != null) connection.close();
    avatars.values().forEach(a -> a.animation.dispose());
    clearNpcs();
    if (renderer != null) renderer.dispose();
    creatures.clear();
    if (map != null)
      try {
        map.close();
      } catch (Exception ignored) {
      }
    if (font != null) font.dispose();
    if (ground != null) ground.dispose();
    if (decor != null) decor.dispose();
    if (hud != null) hud.dispose();
  }
}
