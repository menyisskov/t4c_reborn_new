package com.perso.T4C.screens;

import static com.perso.T4C.config.GameConstants.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.perso.T4C.MyGame;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.combat.CombatProfile;
import com.perso.T4C.combat.CombatProfiles;
import com.perso.T4C.combat.CombatResolver;
import com.perso.T4C.combat.CombatResult;
import com.perso.T4C.combat.PhysicalAttackRequest;
import com.perso.T4C.combat.SeraphArrivalAnimation;
import com.perso.T4C.combat.SeraphAuraService;
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.config.NativeTimingProfile;
import com.perso.T4C.config.Paths;
import com.perso.T4C.death.DeathPenaltyService;
import com.perso.T4C.entity.NameRenderer;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.Inventory;
import com.perso.T4C.gui.screen.MapScreen;
import com.perso.T4C.gui.screen.OptionsScreen;
import com.perso.T4C.gui.screen.QuestScreen;
import com.perso.T4C.gui.screen.SpellBook;
import com.perso.T4C.gui.screen.Statistics;
import com.perso.T4C.gui.widget.GuiBar;
import com.perso.T4C.gui.widget.GuiImage;
import com.perso.T4C.gui.widget.GuiMapZoneDisplay;
import com.perso.T4C.harvest.HarvestChannel;
import com.perso.T4C.harvest.HerbManager;
import com.perso.T4C.harvest.HerbNode;
import com.perso.T4C.helper.AppearanceDefaultsCatalog;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.helper.DisplayModeToggle;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.MusicZoneBinaryIO;
import com.perso.T4C.helper.OriginalZoneMap;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.input.ClickToMoveHandler;
import com.perso.T4C.input.GameInputHandler;
import com.perso.T4C.input.GmCommandProcessor;
import com.perso.T4C.input.GroundItemClickHandler;
import com.perso.T4C.input.HerbInputHandler;
import com.perso.T4C.input.MonsterInputHandler;
import com.perso.T4C.input.NPCInputHandler;
import com.perso.T4C.input.ObjectClickHandler;
import com.perso.T4C.input.TileClickHandler;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.companion.CompanionDef;
import com.perso.T4C.npc.companion.CompanionManager;
import com.perso.T4C.npc.companion.CompanionMode;
import com.perso.T4C.npc.companion.CompanionNPC;
import com.perso.T4C.npc.companion.CompanionRegistry;
import com.perso.T4C.npc.companion.TamedCompanionFactory;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.render.CollisionOverlayRenderer;
import com.perso.T4C.render.ObjectRenderer;
import com.perso.T4C.render.SpellRenderer;
import com.perso.T4C.render.TeleportOverlayRenderer;
import com.perso.T4C.spell.CompanionCastVfxHook;
import com.perso.T4C.spell.SpellCastingService;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellEffectManager;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.spell.SpellVisualResolver;
import com.perso.T4C.spell.TameChannel;
import com.perso.T4C.spell.TameValidator;
import com.perso.T4C.ui.FloatingDamage;
import com.perso.T4C.ui.GameChat;
import com.perso.T4C.ui.PlayerCoordsHud;
import com.perso.T4C.ui.PlayerHUD;
import com.perso.T4C.ui.SystemMessage;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainGameScreen implements Screen {
  private static final Logger log = LoggerFactory.getLogger(MainGameScreen.class);
  private static final int INITIAL_WARMUP_BUFFER = 18;
  private static final int INITIAL_WARMUP_CHUNK_BUDGET = 128;
  private static final int INITIAL_WARMUP_TMPL_BUDGET = 4096;
  private static final int INITIAL_WARMUP_DECOR_BUDGET = 512;
  private static final int FRAME_WARMUP_BUFFER = 14;
  private static final int FRAME_WARMUP_CHUNK_BUDGET = 64;
  private static final int FRAME_WARMUP_TMPL_BUDGET = 128;
  private static final int FRAME_WARMUP_DECOR_BUDGET = 24;
  private static final int DECOR_RENDER_OVERHANG_TILES = 14;
  private final MyGame game;
  private final OrthographicCamera camera;
  private final OrthographicCamera hudCamera;
  private final ScreenViewport viewport;
  private final SpriteBatch batchSol;
  private final SpriteBatch batchDecor;
  private final SpriteBatch batch;
  private final SpriteLoader spriteLoader = SpriteLoader.getInstance();
  private Runnable spriteReloadListener;
  private boolean switchingCharacter;
  private boolean disposed;
  private boolean playerStateSaved;
  private boolean displayInitialized;
  private int loadingStep;
  private GameInputHandler inputHandler;
  private PlayerHUD hud;
  private PlayerCoordsHud coordsHud;
  private SystemMessage systemMessage;
  private GameChat gameChat;
  private GuiMapZoneDisplay mapZoneDisplay;
  private int lastMapZoneWorld = Integer.MIN_VALUE;
  private int lastMapZoneId = Integer.MIN_VALUE;
  @Getter private Player player;
  private MapReader reader;
  private MapRenderer mapRenderer;
  private ShaderProgram outlineShader;
  private NPCManager npcManager;
  private MonsterManager monsterManager;
  private CompanionManager companionManager;
  private QuestService questService;
  private final com.perso.T4C.objects.GroundItemManager groundItemManager =
      new com.perso.T4C.objects.GroundItemManager();
  private final HerbManager herbManager = new HerbManager();
  private final java.util.Random lootRandom = new java.util.Random();
  private final Stage stage;
  private final Vector3 worldCoordsTemp = new Vector3();
  private final Vector3 clickWorldCoordsTemp = new Vector3();
  private final Vector2 playerPositionTemp = new Vector2();
  private final Rectangle playerRenderBoundsTemp = new Rectangle();
  private final Rectangle npcRenderBoundsTemp = new Rectangle();
  private final List<ObjectRenderer.RenderItem> entityItems = new ArrayList<>();
  private final java.util.ArrayDeque<ObjectRenderer.RenderItem> renderItemPool =
      new java.util.ArrayDeque<>();
  private int lastMouseX = Integer.MIN_VALUE;
  private int lastMouseY = Integer.MIN_VALUE;
  private float lastCameraX = Float.NaN;
  private float lastCameraY = Float.NaN;
  private int renderStartX, renderEndX, renderStartY, renderEndY;
  private int decorRenderStartX, decorRenderEndX, decorRenderStartY, decorRenderEndY;
  private MapDefinition currentMap;
  private com.perso.T4C.helper.PlayerStateDto initialPlayerState;
  private XpCurve xpCurve;
  private final SpellRenderer spellRenderer = new SpellRenderer(spriteLoader);
  private final FloatingDamage floatingDamage = new FloatingDamage();
  private final SpellEffectManager spellEffectManager = new SpellEffectManager();
  private final DeathPenaltyService deathPenaltyService =
      new DeathPenaltyService(DeathPenaltyService.Config.originalDefaults());
  private SpellData selectedTargetedSpell;
  private TameChannel tameChannel;
  private HarvestChannel harvestChannel;
  private HerbNode harvestTarget;
  private BaseMonster tameTarget;
  private SpellRenderer.ChannelHandle tameChannelVfx;
  private GuiBar tameProgressBar;
  private GuiImage tameProgressFrame;
  private float offensiveSpellProgressElapsed;
  private float offensiveSpellProgressDuration;
  private Runnable pendingOffensiveSpellLaunch;
  private int selectedTargetedSlot;
  private static final long BUFF_DOUBLE_CLICK_MILLIS = 350L;
  private String lastClickedBuffSpellName;
  private long lastBuffClickMillis;
  private final ShapeRenderer debugShapeRenderer = new ShapeRenderer();
  private final com.perso.T4C.world.DayNightCycle dayNightCycle = loadDayNightCycle();
  private boolean collisionDebugVisible = false;
  private boolean teleportOverlayVisible = false;
  private boolean coordsHudVisible = false;
  private boolean attackCursorApplied = false;
  private boolean talkCursorApplied = false;
  private boolean spellCursorApplied = false;
  private boolean bowCursorApplied = false;
  private BaseMonster currentAttackTarget = null;
  private BaseNPC currentAttackNpcTarget = null;
  private SpellData currentAttackSpell = null;
  private long nextAutoSpellAttemptAtMs = 0L;
  private static final long AUTO_SPELL_RETRY_DELAY_MS = 100L;
  private BaseMonster selectedMonster = null;
  private java.util.function.BooleanSupplier textInputActiveSupplier;
  private static final float TAB_TARGET_RANGE_TILES = 12f;
  private final com.perso.T4C.profiler.GameProfiler gameProfiler =
      new com.perso.T4C.profiler.GameProfiler();
  private long profilerFrameIndex = 0;
  private float simulationAccumulator;
  private static final String COMPANION_VANISH_SPELL = "spell.light";
  private static final String LEVEL_UP_SOUND = "Seraph.wav";
  private static final String MUSIC_ZONES_SUFFIX = ".musiczones.json";
  private static final String MUSIC_ZONES_BIN_SUFFIX = ".musiczones.bin";
  private final List<MusicZoneEntry> musicZones = new ArrayList<>();
  private final List<TeleportEntry> teleports = new ArrayList<>();
  private String currentAmbientMusic = null;
  private int lastAmbientMusicTileX = Integer.MIN_VALUE;
  private int lastAmbientMusicTileY = Integer.MIN_VALUE;
  private long teleportCooldownUntilMs = 0L;
  private long lastTeleportSourceKey = Long.MIN_VALUE;
  private SeraphArrivalAnimation.Kind seraphArrivalKind;
  private boolean seraphArrivalPlaying;

  public MainGameScreen(MyGame game) throws GameException {
    this(game, false);
    while (advanceLoading()) {}
  }

  private MainGameScreen(MyGame game, boolean progressive) {
    this.game = game;
    this.batch = game.batch;
    this.batchSol = new SpriteBatch();
    this.batchDecor = new SpriteBatch();
    camera = new OrthographicCamera();
    camera.setToOrtho(true, WINDOW_WIDTH, WINDOW_HEIGHT);
    viewport = new ScreenViewport(camera);
    hudCamera = new OrthographicCamera();
    updateHudCamera();
    stage = new Stage(new ScreenViewport());
  }

  static MainGameScreen createProgressive(MyGame game) {
    return new MainGameScreen(game, true);
  }

  boolean advanceLoading() throws GameException {
    if (loadingStep >= loadingStepCount()) return false;
    long stepStartedAt = System.nanoTime();
    int stepBeingLoaded = loadingStep;
    switch (loadingStep) {
      case 0 -> initializeCharacterAndWorld();
      case 1 -> initializeWorldData();
      case 2 -> initializePlayerAndServices();
      case 3 -> initializeMapRendering();
      case 4 -> initializeWorldEntities();
      case 5 -> initializeCompanionAndCallbacks();
      case 6 -> initializeDisplay();
      default -> throw new IllegalStateException("Unknown loading step: " + loadingStep);
    }
    loadingStep++;
    log.info(
        "Loading step {} completed in {}ms",
        stepBeingLoaded,
        (System.nanoTime() - stepStartedAt) / 1_000_000L);
    return loadingStep < loadingStepCount();
  }

  int getCompletedLoadingSteps() {
    return loadingStep;
  }

  static int loadingStepCount() {
    return 7;
  }

  private void initializeCharacterAndWorld() {
    initialPlayerState = loadInitialPlayerState();
    currentMap = MapDefinition.fromZ(initialPlayerState != null ? initialPlayerState.z : 0);
    groundItemManager.setActiveWorld(currentMap.getZ());
    herbManager.setActiveWorld(currentMap.getZ());
  }

  private void initializeWorldData() throws GameException {
    loadShader();
    loadXpCurve();
    loadMap(currentMap);
    loadMusicZones(currentMap);
    loadTeleports();
    loadCollisionMap(currentMap);
    CollisionManager.getInstance().setPlayerPassabilityProvider(this::isTeleportSourceTile);
    CollisionManager.getInstance().setTalkVisibilityProvider((tileX, tileY) -> false);
  }

  private void initializePlayerAndServices() throws GameException {
    initializePlayer();
    questService = new QuestService(xpCurve, this::savePlayerState, this::showSystemMessage);
  }

  private void initializeMapRendering() throws GameException {
    mapRenderer = createMapRenderer();
    inputHandler = new GameInputHandler(log, mapRenderer, player);
  }

  private void initializeWorldEntities() {
    monsterManager = createMonsterManager();
    npcManager = createNpcManager();
    npcManager.triggerPopupEvents(player);
  }

  private void initializeCompanionAndCallbacks() throws GameException {
    companionManager = new CompanionManager(npcManager);
    companionManager.setXpCurve(xpCurve);
    companionManager.setMonsterSupplier(
        () -> monsterManager == null ? null : monsterManager.getMonsters());
    npcManager.setCompanionManager(companionManager);
    PlayerStateStore.setCompanionSupplier(companionManager::getCompanion);
    configureCallbacks();
    restorePersistedCompanion();
    initializeTameProgressGui();
    registerReloadListener();
  }

  private void initializeDisplay() {
    initializeHud();
    initializeInputHandlers();
    updateCamera();
    calculateRenderBounds();
    warmVisibleCaches(
        INITIAL_WARMUP_BUFFER,
        INITIAL_WARMUP_CHUNK_BUDGET,
        INITIAL_WARMUP_TMPL_BUDGET,
        INITIAL_WARMUP_DECOR_BUDGET);
    game.startMapPreloadAsync();
    displayInitialized = true;
  }

  private void initializeTameProgressGui() throws GameException {
    tameProgressBar =
        new GuiBar(
            null,
            spriteLoader.getRegionFromSpriteName("GUI_BackChStat_XP"),
            0f,
            0f,
            314f,
            12f,
            this::getCastProgress);
    tameProgressFrame =
        new GuiImage(
            spriteLoader.getRegionFromSpriteName("64kTameProgressFrame"), 0f, 0f, 360f, 26f);
  }

  private void loadShader() {
    try {
      ShaderProgram.pedantic = false;
      String vert = Gdx.files.internal(Paths.SHADERS_OUTLINE_VERT).readString();
      String frag = Gdx.files.internal(Paths.SHADERS_OUTLINE_FRAG).readString();
      outlineShader = new ShaderProgram(vert, frag);
      if (!outlineShader.isCompiled()) {
        log.warn("Outline shader failed to compile: {}", outlineShader.getLog());
        outlineShader = null;
      }
    } catch (Throwable t) {
      log.warn("Failed to load outline shader, continuing without it", t);
      outlineShader = null;
    }
  }

  private void loadXpCurve() {
    xpCurve = XpCurve.loadDefault();
  }

  private com.perso.T4C.helper.PlayerStateDto loadInitialPlayerState() {
    try {
      com.perso.T4C.helper.LocalCharacterStore.CharacterSlot active =
          com.perso.T4C.helper.LocalCharacterStore.getActiveCharacter();
      com.perso.T4C.helper.PlayerStateDto state =
          active == null
              ? PlayerStateStore.load()
              : com.perso.T4C.helper.LocalCharacterStore.loadState(active);
      if (state != null && active != null) {
        if (state.name == null || state.name.isBlank()) state.name = active.name();
        if (state.gender == null || state.gender.isBlank()) state.gender = active.gender();
      }
      return state;
    } catch (Throwable ignored) {
      return null;
    }
  }

  private void savePlayerState() {
    PlayerStateStore.save(player, dayNightCycle.getHour());
  }

  private static com.perso.T4C.world.DayNightCycle loadDayNightCycle() {
    try {
      com.perso.T4C.helper.PlayerStateDto state = PlayerStateStore.load();
      if (state != null && state.dayNightHour > 0f) {
        return new com.perso.T4C.world.DayNightCycle(state.dayNightHour);
      }
    } catch (Throwable ignored) {
    }
    return new com.perso.T4C.world.DayNightCycle();
  }

  private void loadMap(MapDefinition map) throws GameException {
    reader = game.getOrLoadMapReader(map);
  }

  private void loadMusicZones(MapDefinition map) {
    musicZones.clear();
    currentAmbientMusic = null;
    lastAmbientMusicTileX = Integer.MIN_VALUE;
    lastAmbientMusicTileY = Integer.MIN_VALUE;
    if (map == null) {
      SoundManager.stopAmbient();
      return;
    }
    File mapFile = new File(map.getMapPath());
    File parent = mapFile.getParentFile();
    String baseDir = parent != null ? parent.getPath() : ".";
    String mapName =
        mapFile.getName().replace(".mapbin", "").replace(".map", "").replace(".json.gz", "");
    File zonesFile = new File(baseDir + File.separator + mapName + MUSIC_ZONES_BIN_SUFFIX);
    boolean binary = zonesFile.exists();
    if (!binary) {
      zonesFile = new File(baseDir + File.separator + mapName + MUSIC_ZONES_SUFFIX);
    }
    if (!zonesFile.exists()) {
      SoundManager.stopAmbient();
      return;
    }
    try {
      List<MusicZoneEntry> loaded =
          binary ? readBinaryMusicZones(zonesFile) : readJsonMusicZones(zonesFile);
      if (loaded != null) {
        for (MusicZoneEntry zone : loaded) {
          if (zone != null && zone.music != null && !zone.music.isBlank()) {
            zone.normalize();
            musicZones.add(zone);
          }
        }
      }
      log.info("Loaded {} music zone(s) from {}", musicZones.size(), zonesFile.getPath());
    } catch (Exception e) {
      log.warn("Failed to load music zones from {}", zonesFile.getPath(), e);
    }
  }

  private List<MusicZoneEntry> readBinaryMusicZones(File zonesFile) throws Exception {
    List<MusicZoneBinaryIO.Entry> binaryEntries = MusicZoneBinaryIO.read(zonesFile);
    List<MusicZoneEntry> zones = new ArrayList<>(binaryEntries.size());
    for (MusicZoneBinaryIO.Entry binaryEntry : binaryEntries) {
      MusicZoneEntry zone = new MusicZoneEntry();
      zone.x1 = binaryEntry.x1;
      zone.y1 = binaryEntry.y1;
      zone.x2 = binaryEntry.x2;
      zone.y2 = binaryEntry.y2;
      zone.music = binaryEntry.music;
      zones.add(zone);
    }
    return zones;
  }

  private List<MusicZoneEntry> readJsonMusicZones(File zonesFile) throws Exception {
    try (FileReader reader = new FileReader(zonesFile)) {
      Type type = new TypeToken<List<MusicZoneEntry>>() {}.getType();
      return new Gson().fromJson(reader, type);
    }
  }

  private void updateAmbientMusicForPlayer() {
    if (player == null || musicZones.isEmpty()) {
      if (currentAmbientMusic != null) {
        currentAmbientMusic = null;
        SoundManager.stopAmbient();
      }
      return;
    }
    Vector2 playerPosition = player.getPositionVector();
    int tileX = (int) (playerPosition.x / GRID_W);
    int tileY = (int) (playerPosition.y / GRID_H);
    if (tileX == lastAmbientMusicTileX && tileY == lastAmbientMusicTileY) {
      return;
    }
    lastAmbientMusicTileX = tileX;
    lastAmbientMusicTileY = tileY;
    String music = null;
    for (int i = musicZones.size() - 1; i >= 0; i--) {
      MusicZoneEntry zone = musicZones.get(i);
      if (zone.contains(tileX, tileY)) {
        music = zone.music;
        break;
      }
    }
    if (java.util.Objects.equals(currentAmbientMusic, music)) {
      return;
    }
    currentAmbientMusic = music;
    if (music == null) {
      SoundManager.stopAmbient();
    } else {
      SoundManager.playAmbient(music + ".wav");
    }
  }

  private void updateMapZoneDisplay() {
    if (player == null) return;
    int world = player.getCoordinates().getZ();
    int tileX = (int) (player.getPositionVector().x / GRID_W);
    int tileY = (int) (player.getPositionVector().y / GRID_H);
    int zoneId = OriginalZoneMap.zoneId(world, tileX, tileY);
    if (world == lastMapZoneWorld && zoneId == lastMapZoneId) return;
    lastMapZoneWorld = world;
    lastMapZoneId = zoneId;
    String name = OriginalZoneMap.displayName(world, tileX, tileY);
    mapZoneDisplay = name == null || name.isBlank() ? null : new GuiMapZoneDisplay(name);
  }

  private void loadTeleports() {
    teleports.clear();
    try {
      for (com.perso.T4C.teleport.TeleportDefinition entry :
          com.perso.T4C.teleport.TeleportRegistry.load()) {
        TeleportEntry teleport = new TeleportEntry();
        teleport.id = entry.id();
        teleport.sourceZ = entry.sourceZ();
        teleport.sourceX = entry.sourceX();
        teleport.sourceY = entry.sourceY();
        teleport.targetZ = entry.targetZ();
        teleport.targetX = entry.targetX();
        teleport.targetY = entry.targetY();
        teleports.add(teleport);
      }
      log.info("Loaded {} teleport(s) from Java definitions", teleports.size());
    } catch (Exception e) {
      log.warn("Failed to load teleports from Java definitions", e);
    }
  }

  private void updateTeleportForPlayer() {
    if (player == null || teleports.isEmpty()) {
      return;
    }
    long now = System.currentTimeMillis();
    if (now < teleportCooldownUntilMs) {
      return;
    }
    int tileX = (int) (player.getPositionVector().x / GRID_W);
    int tileY = (int) (player.getPositionVector().y / GRID_H);
    int z = player.getCoordinates().getZ();
    long sourceKey = packTeleportKey(z, tileX, tileY);
    if (sourceKey == lastTeleportSourceKey) {
      return;
    }
    for (TeleportEntry teleport : teleports) {
      if (teleport.sourceZ == z && teleport.sourceX == tileX && teleport.sourceY == tileY) {
        teleportPlayer(teleport);
        return;
      }
    }
    lastTeleportSourceKey = Long.MIN_VALUE;
  }

  private void teleportPlayer(TeleportEntry teleport) {
    lastTeleportSourceKey = packTeleportKey(teleport.sourceZ, teleport.sourceX, teleport.sourceY);
    teleportCooldownUntilMs = System.currentTimeMillis() + 350L;
    float x = teleport.targetX * GRID_W;
    float y = teleport.targetY * GRID_H;
    int previousZ = player.getCoordinates().getZ();
    player.setWorldPosition(x, y, teleport.targetZ);
    if (previousZ == teleport.targetZ) {
      camera.position.set(x, y, 0);
      camera.update();
    }
    log.info(
        "Teleport {}: ({}, {}, {}) -> ({}, {}, {})",
        teleport.id,
        teleport.sourceX,
        teleport.sourceY,
        teleport.sourceZ,
        teleport.targetX,
        teleport.targetY,
        teleport.targetZ);
  }

  private static long packTeleportKey(int z, int x, int y) {
    return (((long) z & 0xFFFFL) << 48) | (((long) x & 0xFFFFFFL) << 24) | ((long) y & 0xFFFFFFL);
  }

  private static class MusicZoneEntry {
    int x1;
    int y1;
    int x2;
    int y2;
    String music;

    void normalize() {
      int minX = Math.min(x1, x2);
      int maxX = Math.max(x1, x2);
      int minY = Math.min(y1, y2);
      int maxY = Math.max(y1, y2);
      x1 = minX;
      x2 = maxX;
      y1 = minY;
      y2 = maxY;
    }

    boolean contains(int x, int y) {
      return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }
  }

  private static class TeleportEntry implements TeleportOverlayRenderer.Entry {
    int id;
    int sourceZ;
    int sourceX;
    int sourceY;
    int targetZ;
    int targetX;
    int targetY;

    public int sourceZ() {
      return sourceZ;
    }

    public int sourceX() {
      return sourceX;
    }

    public int sourceY() {
      return sourceY;
    }

    public int targetZ() {
      return targetZ;
    }

    public int targetX() {
      return targetX;
    }

    public int targetY() {
      return targetY;
    }
  }

  private void loadCollisionMap(MapDefinition map) {
    if (map.getCollisionPath() == null) {
      CollisionManager.getInstance().clear();
      log.info("No collision map for Z={} ({})", map.getZ(), map.name());
      return;
    }
    try {
      String collisionFile = Gdx.files.internal(map.getCollisionPath()).file().getAbsolutePath();
      CollisionReader collisionReader = new CollisionReader(new File(collisionFile));
      CollisionManager.getInstance().initialize(collisionReader);
      log.info("Collision map loaded successfully");
    } catch (Exception e) {
      log.warn("Could not load collision map: {}. Collision detection disabled.", e.getMessage());
    }
  }

  private void initializePlayer() throws GameException {
    player = new Player(resolvePlayerParts());
    applyEquippedItemsToPlayer();
    float startX = NEW_CHARACTER_START_TILE_X * GRID_W;
    float startY = NEW_CHARACTER_START_TILE_Y * GRID_H;
    int startZ = NEW_CHARACTER_START_TILE_Z;
    try {
      if (initialPlayerState != null) {
        startX = initialPlayerState.x * com.perso.T4C.config.GameConstants.GRID_W;
        startY = initialPlayerState.y * com.perso.T4C.config.GameConstants.GRID_H;
        startZ = initialPlayerState.z;
      }
    } catch (Throwable ignored) {
    }
    player.setWorldPosition(startX, startY, startZ);
    try {
      PlayerStateStore.applyToPlayer(initialPlayerState, player);
    } catch (Throwable ignored) {
    }
    applyXpCurve();
    player.setMapBounds(reader.getWidth() * GRID_W, reader.getHeight() * GRID_H);
    if (initialPlayerState != null) {
      player.getMovement().recoverLoadedCollisionPosition(player);
    }
    Vector2 resolvedStart = player.getPositionVector();
    camera.position.set(resolvedStart.x, resolvedStart.y, 0);
    camera.update();
  }

  private Object[] resolvePlayerParts() {
    List<Object> parts = new ArrayList<>();
    Map<String, String> equipment =
        initialPlayerState == null ? null : initialPlayerState.equipment;
    if (equipment != null && !equipment.isEmpty()) {
      for (BodyPart part : BodyPart.values()) {
        String itemName = equipment.get(part.name());
        if (itemName == null) {
          itemName = findEquipmentByKey(equipment, part.getKey());
        }
        String base = PlayerAppearanceDefaults.getAppearanceFor(itemName, part);
        if (base != null) {
          parts.add(part);
          parts.add(base);
        }
      }
    }
    if (!parts.isEmpty()) {
      return parts.toArray();
    }
    for (Map.Entry<BodyPart, String> entry :
        AppearanceDefaultsCatalog.nakedParts(
                initialPlayerState != null
                    ? initialPlayerState.gender
                    : AppearanceDefaultsCatalog.MALE)
            .entrySet()) {
      parts.add(entry.getKey());
      parts.add(entry.getValue());
    }
    return parts.toArray();
  }

  private String findEquipmentByKey(Map<String, String> equipment, String key) {
    if (key == null || key.isEmpty()) {
      return null;
    }
    for (Map.Entry<String, String> entry : equipment.entrySet()) {
      if (key.equalsIgnoreCase(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private void applyEquippedItemsToPlayer() {
    if (player == null || initialPlayerState == null) {
      return;
    }
    player.setGender(initialPlayerState.gender);
    if (initialPlayerState.equipment == null) {
      return;
    }
    Map<BodyPart, String> equipped = new java.util.EnumMap<>(BodyPart.class);
    for (Map.Entry<String, String> entry : initialPlayerState.equipment.entrySet()) {
      try {
        BodyPart part = BodyPart.valueOf(entry.getKey());
        if (entry.getValue() != null && !entry.getValue().isEmpty()) {
          equipped.put(part, entry.getValue());
        }
      } catch (IllegalArgumentException ignored) {
      }
    }
    if (!equipped.isEmpty()) {
      player.setEquippedItems(equipped);
    }
    PlayerAppearanceDefaults.applyDefaults(player);
  }

  private void applyXpCurve() {
    if (xpCurve == null || player == null) {
      return;
    }
    xpCurve.applyToPlayer(player);
  }

  private MapRenderer createMapRenderer() throws GameException {
    ModifSprites modifSprites = ModifSprites.empty();
    MapRenderer renderer =
        new MapRenderer(
            reader,
            spriteLoader,
            batchSol,
            batchDecor,
            outlineShader,
            modifSprites,
            currentMap != null ? currentMap.getZ() : 0);
    CollisionManager.getInstance()
        .setDynamicProvider((worldX, worldY) -> renderer.isDoorBlockedAt(worldX, worldY));
    return renderer;
  }

  private NPCManager createNpcManager() {
    NPCManager manager = new NPCManager(outlineShader, questService);
    try {
      String mapPath = currentMap != null ? currentMap.getMapPath() : Paths.MAP;
      manager.initializeNpcsFromMap(mapPath);
    } catch (GameException e) {
      log.error("Failed to initialize NPCs from map json", e);
    }
    return manager;
  }

  private MonsterManager createMonsterManager() {
    MonsterManager manager = new MonsterManager(outlineShader);
    NpcScriptRuntime.setSummonCallback(
        (name, x, y, z) -> {
          if (currentMap == null || currentMap.getZ() != z) return false;
          if (manager.spawnMonster(name, x, y, false)) return true;
          return npcManager != null && npcManager.spawnNPC(name, x, y);
        });
    manager.setXpCurve(xpCurve);
    try {
      String mapPath = currentMap != null ? currentMap.getMapPath() : Paths.MAP;
      manager.initializeMonstersFromMap(mapPath);
    } catch (GameException e) {
      log.error("Failed to initialize monsters from map json", e);
    }
    return manager;
  }

  private void configureCallbacks() {
    configureMonsterDamageCallback();
    configureNpcDamageCallback();
    configurePlayerMessageCallback();
    configurePlayerTeleportCallback();
    configurePlayerDeathCallback();
    configurePlayerLevelUpCallback();
    player.setActionInterruptedCallback(this::clearCurrentAttackTarget);
    player.setHealingCallback(
        amount -> {
          Vector2 position = player.getPositionVector();
          floatingDamage.spawn(amount, position.x, position.y, FloatingDamage.Type.HEAL);
        });
    player.setManaRestoredCallback(
        amount -> {
          Vector2 position = player.getPositionVector();
          floatingDamage.spawn(amount, position.x, position.y, FloatingDamage.Type.MANA);
        });
    player.setItemDropCallback(
        (index, itemKey) -> {
          com.perso.T4C.item.ItemDefinition dropped =
              com.perso.T4C.item.ItemRegistry.findByKey(itemKey);
          if (dropped != null && dropped.isUndroppable()) {
            showSystemMessage(I18n.key("message.gem_of_destiny_undroppable"));
            return false;
          }
          int remainingCharges =
              com.perso.T4C.item.InventoryService.chargesForNextInstance(player, itemKey);
          com.perso.T4C.item.InventoryService.Result removed =
              com.perso.T4C.item.InventoryService.remove(player, index, itemKey);
          if (!removed.success()) return false;
          Vector2 position = player.getPositionVector();
          groundItemManager.dropItem(itemKey, remainingCharges, position.x, position.y);
          savePlayerState();
          return true;
        });
    configureInputHandlerCallbacks();
  }

  private void configureInputHandlerCallbacks() {
    if (inputHandler == null) {
      return;
    }
    inputHandler.setDebugOverlayToggle(this::toggleCollisionDebugOverlay);
    inputHandler.setTeleportOverlayToggle(this::toggleTeleportOverlay);
    inputHandler.setCoordsHudToggle(this::toggleCoordsHud);
    inputHandler.setMapToggle(this::toggleMapScreen);
    inputHandler.setHudSupplier(() -> hud);
  }

  private void configureMonsterDamageCallback() {
    monsterManager.setDeathCallback(
        monster -> {
          if (monster instanceof com.perso.T4C.monster.core.DataMonster dataMonster) {
            NpcScriptRuntime.Effects effects = NpcScriptRuntime.Effects.empty();
            if (monster instanceof com.perso.T4C.monster.core.MonsterLifecycle lifecycle) {
              effects = lifecycle.onDeath(player);
            } else {
              effects = NpcScriptRuntime.death(dataMonster, player);
            }
            effects.messages().forEach(this::showSystemMessage);
            for (String spell : effects.targetSpells()) {
              com.perso.T4C.spell.SpellData data =
                  com.perso.T4C.spell.SpellRegistry.findByName(spell);
              if (data != null)
                spellRenderer.triggerImpactSpell(
                    data.getName(), player.getPositionVector().x, player.getPositionVector().y);
            }
            for (String spell : effects.selfSpells()) {
              com.perso.T4C.spell.SpellData data =
                  com.perso.T4C.spell.SpellRegistry.findByName(spell);
              if (data != null)
                spellRenderer.triggerImpactSpell(
                    data.getName(), monster.getPosition().x, monster.getPosition().y);
            }
          }
          NpcScriptRuntime.DeathEffect summonEffect =
              NpcScriptRuntime.summonedMonsterDefeated(monster.getCanonicalName());
          if (summonEffect != null) {
            if (summonEffect.itemKey() != null && !summonEffect.itemKey().isBlank())
              com.perso.T4C.item.InventoryService.add(player, summonEffect.itemKey());
            if (summonEffect.message() != null && !summonEffect.message().isBlank())
              showSystemMessage(summonEffect.message());
          }
          if (monster instanceof com.perso.T4C.monster.core.DataMonster dataMonster
              && dataMonster.usesHumanoidAnimations()) {
            Vector2 position = monster.getPosition();
            spellRenderer.triggerImpactSpell("GreatExplosion", position.x, position.y);
          }
        });
    monsterManager.setScriptEffectsCallback(
        effects -> {
          effects.messages().forEach(this::showSystemMessage);
          Vector2 targetPosition = player.getPositionVector();
          for (String spell : effects.targetSpells()) {
            com.perso.T4C.spell.SpellData data = SpellRegistry.findByName(spell);
            if (data != null)
              spellRenderer.triggerImpactSpell(data.getName(), targetPosition.x, targetPosition.y);
          }
          for (String spell : effects.selfSpells()) {
            com.perso.T4C.spell.SpellData data = SpellRegistry.findByName(spell);
            if (data != null)
              spellRenderer.triggerImpactSpell(data.getName(), targetPosition.x, targetPosition.y);
          }
        });
    monsterManager.setDamageDealtCallback(
        (damage, pos) ->
            floatingDamage.spawn(damage, pos.x, pos.y, FloatingDamage.Type.MONSTER_RECEIVED));
    monsterManager.setAttackMissedCallback(this::showMissFeedback);
    monsterManager.setPlayerAttackHitCallback((target, result) -> handleSeraphAuraAttackHit());
    monsterManager.setCompanionAttackNotifyCallback(
        monster -> {
          if (companionManager != null) {
            companionManager.onPlayerAttacked(monster);
          }
        });
    monsterManager.setCompanionDamageCallback(
        damage -> {
          if (companionManager != null) {
            companionManager.damageCompanion(damage);
          }
        });
    monsterManager.setPlayerDamageCallback(
        new com.perso.T4C.monster.core.DamageCallback() {
          @Override
          public void applyDamage(BaseMonster attacker, int rawDamage) {
            CombatProfile attackerProfile = CombatProfiles.fromMonster(attacker);
            CombatProfile playerProfile = CombatProfiles.fromPlayer(player);
            CombatResult result =
                CombatResolver.resolve(
                    new PhysicalAttackRequest(
                        attackerProfile,
                        playerProfile,
                        rawDamage,
                        0,
                        attacker.isLastAttackRanged()),
                    ThreadLocalRandom.current());
            int damage = result.damage();
            if (!result.hit()) {
              log.info(
                  "{} misses player (precision={})",
                  attacker.getCanonicalName(),
                  result.precision());
              return;
            }
            monsterManager.notifyMonsterAttackHit(attacker, player);
            Vector2 impactPosition = player.getPositionVector().cpy();
            handleSeraphAuraOnHit(attacker, attackerProfile.armorClass());
            player.takeDamage(damage);
            int appliedDamage = player.getLastDamageTaken();
            if (appliedDamage > 0) {
              com.perso.T4C.item.ItemDurabilityService.wearArmorOnPhysicalHit(player);
              playRandomPlayerHitSound();
              floatingDamage.spawn(
                  appliedDamage,
                  impactPosition.x,
                  impactPosition.y,
                  FloatingDamage.Type.PLAYER_RECEIVED);
              log.info(
                  "Player took {} damage! HP: {}/{}",
                  appliedDamage,
                  player.getCurrentHp(),
                  player.getMaxHp());
            } else {
              log.info(
                  "{} hit player, but armor absorbed all {} raw damage (AC={})",
                  attacker.getCanonicalName(),
                  rawDamage,
                  playerProfile.armorClass());
            }
          }

          @Override
          public void applySpell(BaseMonster attacker, int spellId, int rawDamage) {
            if (spellId == 10596) {
              SpellData teleport = SpellRegistry.findById(spellId);
              Vector2 origin = attacker.getPosition().cpy();
              Vector2 target = player.getPositionVector();
              float angle = (float) (Math.random() * Math.PI * 2.0);
              float distance = 6f * GRID_W;
              playNpcSelfVfx(teleport, origin);
              attacker.teleportTo(
                  target.x + (float) Math.cos(angle) * distance,
                  target.y + (float) Math.sin(angle) * distance);
              playNpcSelfVfx(teleport, attacker.getPosition().cpy());
              return;
            }
            SpellData spell = SpellRegistry.findById(spellId);
            String element =
                spell == null
                    ? (spellId == 10120 ? "air" : "water")
                    : elementName(spell.getElement());
            int resistance = Math.max(1, player.getElementResistance(element));
            int damage = Math.max(0, rawDamage * 100 / resistance);
            Runnable impact =
                () -> {
                  Vector2 impactPosition = player.getPositionVector().cpy();
                  player.takeDamage(damage);
                  int appliedDamage = player.getLastDamageTaken();
                  if (appliedDamage > 0) {
                    playRandomPlayerHitSound();
                    floatingDamage.spawn(
                        appliedDamage,
                        impactPosition.x,
                        impactPosition.y,
                        FloatingDamage.Type.PLAYER_RECEIVED);
                  }
                  log.info(
                      "{} spell {} hits player for {} damage ({} resistance={})",
                      attacker.getCanonicalName(),
                      spellId,
                      appliedDamage,
                      element,
                      resistance);
                };
            if (spell != null) {
              playNpcCastVfx(spell, player, attacker.getPosition().cpy(), impact);
              return;
            }
            String legacyProjectile =
                spellId == 10086
                    ? "64kSpellEnergyBallBlue-"
                    : spellId == 10120 ? "Lightning" : "PoisonArrow";
            String legacyImpact = spellId == 10086 ? "SmallExplosion-" : legacyProjectile;
            spellRenderer.playLaunchSound("Small Projectile.wav");
            boolean launched =
                spellRenderer.launchProjectile(
                    legacyProjectile,
                    player,
                    attacker.getPosition().x,
                    attacker.getPosition().y,
                    false,
                    () -> {
                      spellRenderer.triggerImpactSpell(
                          legacyImpact,
                          player.getPositionVector().x,
                          player.getPositionVector().y,
                          "Explosion.wav");
                      impact.run();
                    });
            if (!launched) {
              spellRenderer.triggerImpactSpell(
                  legacyImpact,
                  player.getPositionVector().x,
                  player.getPositionVector().y,
                  "Explosion.wav");
              impact.run();
            }
          }
        });
    monsterManager.setLootCallback(
        monster -> {
          Vector2 pos = monster.getPosition();
          groundItemManager.spawnFromLoot(
              monster.getLootTable().roll(lootRandom),
              pos.x,
              pos.y,
              gold -> {
                player.addGold(gold);
                showSystemMessage(I18n.message("message.gold_gained", gold));
              });
        });
    monsterManager.setPlayerKillCallback(
        monster -> {
          questService.recordKill(
              player,
              monster.getCanonicalName(),
              currentMap.getZ(),
              monster.getTileX(),
              monster.getTileY());
          trackGoblinTrustProgress(monster);
        });
  }

  /**
   * Mirak Nira's "100 goblins for Windhowl trust" storyline (see {@code npc.mirak.trust.*} in
   * lang.json and {@code MirakNira.java}) reads this flag but nothing ever incremented it, so the
   * quest was silently unwinnable. Any player kill of a GOBLIN-clan monster, anywhere, counts.
   */
  private void trackGoblinTrustProgress(com.perso.T4C.monster.core.BaseMonster monster) {
    com.perso.T4C.monster.core.MonsterClan clan =
        com.perso.T4C.monster.core.MonsterClanRelations.resolveClan(
            monster.getClass().getSimpleName(), monster.getCanonicalName());
    if (clan == com.perso.T4C.monster.core.MonsterClan.GOBLIN) {
      player.setQuestFlag(
          "__GOBLINS_KILLED_BY_HERO", player.getQuestFlag("__GOBLINS_KILLED_BY_HERO") + 1);
    }
  }

  private static String elementName(int element) {
    return switch (element) {
      case 1 -> "fire";
      case 2 -> "earth";
      case 3 -> "air";
      case 4 -> "water";
      case 5 -> "light";
      case 6 -> "dark";
      default -> "water";
    };
  }

  private void configureNpcDamageCallback() {
    if (npcManager == null) {
      return;
    }
    npcManager.setPlayerDamageCallback(
        (attacker, rawDamage) -> {
          attacker.onAttack(player);
          if (attacker.getCurrentHp() <= 0) {
            npcManager.damageNpc(attacker, 1, player);
            return;
          }
          CombatProfile attackerProfile = CombatProfiles.fromNpc(attacker);
          CombatProfile playerProfile = CombatProfiles.fromPlayer(player);
          CombatResult result =
              CombatResolver.resolve(
                  new PhysicalAttackRequest(attackerProfile, playerProfile, rawDamage, 0, false),
                  ThreadLocalRandom.current());
          if (!result.hit()) {
            log.info("{} misses player (precision={})", attacker.getName(), result.precision());
            return;
          }
          Vector2 impactPosition = player.getPositionVector().cpy();
          player.takeDamage(result.damage());
          int appliedDamage = player.getLastDamageTaken();
          if (appliedDamage > 0) {
            com.perso.T4C.item.ItemDurabilityService.wearArmorOnPhysicalHit(player);
            playRandomPlayerHitSound();
            floatingDamage.spawn(
                appliedDamage,
                impactPosition.x,
                impactPosition.y,
                FloatingDamage.Type.PLAYER_RECEIVED);
            log.info(
                "Player took {} damage from {}! HP: {}/{}",
                appliedDamage,
                attacker.getName(),
                player.getCurrentHp(),
                player.getMaxHp());
          }
        });
  }

  private void playRandomPlayerHitSound() {
    if (ThreadLocalRandom.current().nextFloat() >= 0.4f) {
      return;
    }
    String sound = ThreadLocalRandom.current().nextBoolean() ? "Male Hit 1.wav" : "Male Hit 2.wav";
    SoundManager.animateSound(sound);
  }

  private void configurePlayerMessageCallback() {
    player.setMessageCallback(
        message -> {
          showSystemMessage(message);
        });
  }

  private static NpcScriptRuntime.Effects mergeMonsterEffects(
      NpcScriptRuntime.Effects first, NpcScriptRuntime.Effects second) {
    java.util.List<String> messages = new java.util.ArrayList<>(first.messages());
    messages.addAll(second.messages());
    java.util.List<String> selfSpells = new java.util.ArrayList<>(first.selfSpells());
    selfSpells.addAll(second.selfSpells());
    java.util.List<String> targetSpells = new java.util.ArrayList<>(first.targetSpells());
    targetSpells.addAll(second.targetSpells());
    return new NpcScriptRuntime.Effects(messages, selfSpells, targetSpells);
  }

  private void configurePlayerLevelUpCallback() {
    player.setLevelUpCallback(
        newLevel -> {
          spellRenderer.playImpactSound(LEVEL_UP_SOUND);
          spellRenderer.playLevelUpAnimation(player);
        });
  }

  private void configurePlayerTeleportCallback() {
    player.setPositionCallback(
        (x, y, z) -> {
          switchMapForZ(z);
          camera.position.set(x, y, 0);
          camera.update();
          log.info("Player teleported to ({}, {}, {})", (int) (x / GRID_W), (int) (y / GRID_H), z);
        });
  }

  private void switchMapForZ(int z) {
    MapDefinition targetMap = MapDefinition.fromZ(z);
    if (targetMap == currentMap) {
      return;
    }
    try {
      currentMap = targetMap;
      loadMap(currentMap);
      loadMusicZones(currentMap);
      loadCollisionMap(currentMap);
      CollisionManager.getInstance().setPlayerPassabilityProvider(this::isTeleportSourceTile);
      CollisionManager.getInstance().setTalkVisibilityProvider((tileX, tileY) -> false);
      if (mapRenderer != null) {
        mapRenderer.dispose();
      }
      mapRenderer = createMapRenderer();
      inputHandler = new GameInputHandler(log, mapRenderer, player);
      CompanionNPC survivingCompanion =
          companionManager != null ? companionManager.getCompanion() : null;
      if (npcManager != null) {
        if (survivingCompanion != null) {
          npcManager.removeNPC(survivingCompanion);
        }
        npcManager.dispose();
      }
      monsterManager = createMonsterManager();
      npcManager = createNpcManager();
      npcManager.triggerPopupEvents(player);
      if (companionManager != null) {
        companionManager.setNpcManager(npcManager);
        npcManager.setCompanionManager(companionManager);
        companionManager.onMapChanged(player.getPositionVector());
      }
      groundItemManager.setActiveWorld(z);
      cancelHarvest();
      herbManager.setActiveWorld(z);
      configureMonsterDamageCallback();
      configureNpcDamageCallback();
      configureInputHandlerCallbacks();
      player.setMapBounds(reader.getWidth() * GRID_W, reader.getHeight() * GRID_H);
      calculateRenderBounds();
      warmVisibleCaches(
          INITIAL_WARMUP_BUFFER,
          INITIAL_WARMUP_CHUNK_BUDGET,
          INITIAL_WARMUP_TMPL_BUDGET,
          INITIAL_WARMUP_DECOR_BUDGET);
      if (hud != null) {
        initializeInputHandlers();
      }
      log.info("Switched to map {} at Z={}", currentMap.name(), currentMap.getZ());
      lastAmbientMusicTileX = Integer.MIN_VALUE;
      lastAmbientMusicTileY = Integer.MIN_VALUE;
      lastMapZoneWorld = Integer.MIN_VALUE;
      lastMapZoneId = Integer.MIN_VALUE;
      updateAmbientMusicForPlayer();
      updateMapZoneDisplay();
    } catch (Exception e) {
      log.error("Failed to switch map for Z={}", z, e);
      showSystemMessage(I18n.message("message.map_load_failed", z));
    }
  }

  private void registerReloadListener() {
    spriteReloadListener =
        () -> {
          try {
            player.onResourcesReloaded();
            npcManager.onResourcesReloaded();
            monsterManager.onResourcesReloaded();
            if (game != null && game.cursorManager != null) {
              game.cursorManager.onSpriteReload(game);
            }
            clearCursorState();
          } catch (Throwable ignored) {
          }
        };
    SpriteLoader.getInstance().registerReloadListener(spriteReloadListener);
  }

  private void switchCharacter() {
    if (switchingCharacter || disposed) return;
    switchingCharacter = true;
    clearCurrentAttackTarget();
    clearSelectedMonster();
    cancelActiveTargetedSpell();
    GuiManager.close();
    savePlayerState();
    playerStateSaved = true;
    CharacterSelectionScreen next;
    try {
      next = new CharacterSelectionScreen(game);
    } catch (RuntimeException e) {
      switchingCharacter = false;
      log.error("Failed to return to character selection", e);
      showSystemMessage(I18n.key("character.roster.failed"));
      GuiManager.open(new OptionsScreen(this::switchCharacter));
      return;
    }
    dispose();
    game.setScreen(next);
  }

  @Override
  public void render(float delta) {
    com.perso.T4C.profiler.FrameEvent frameEvent =
        gameProfiler.isActive() ? new com.perso.T4C.profiler.FrameEvent() : null;
    if (frameEvent != null) frameEvent.begin();
    section(
        "input",
        () -> {
          if (seraphArrivalBlocksMovement()) {
            return;
          }
          inputHandler.handleInput(delta, player);
        });
    section("teleport", this::updateTeleportForPlayer);
    section("entities", () -> updateEntities(delta));
    section(
        "camera",
        () -> {
          updateCamera();
          updateAmbientMusicForPlayer();
          updateMapZoneDisplay();
        });
    section(
        "dayNight",
        () -> {
          dayNightCycle.update(delta);
          dayNightCycle.forceDaylight(player != null && player.hasRadianceBuff());
          dayNightCycle.setUnlitWorld(player != null && player.getCoordinates().getZ() != 0);
        });
    section(
        "bounds",
        () -> {
          calculateRenderBounds();
          updateHerbViewport();
          warmVisibleCaches(
              FRAME_WARMUP_BUFFER,
              FRAME_WARMUP_CHUNK_BUDGET,
              FRAME_WARMUP_TMPL_BUDGET,
              FRAME_WARMUP_DECOR_BUDGET);
        });
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    section(
        "groundRender",
        () ->
            mapRenderer.renderGroundOnly(
                camera, renderStartX, renderEndX, renderStartY, renderEndY));
    section(
        "entityRender",
        () -> {
          Vector3 worldCoords = getWorldCoords();
          List<ObjectRenderer.RenderItem> items = buildEntityRenderItems();
          mapRenderer.renderEntitiesWithDecors(
              camera,
              worldCoords.x,
              worldCoords.y,
              decorRenderStartX,
              decorRenderEndX,
              decorRenderStartY,
              decorRenderEndY,
              items);
          releaseEntityRenderItems(items);
          if (npcManager != null) {
            batchDecor.setProjectionMatrix(camera.combined);
            batchDecor.begin();
            npcManager.renderDialogOverlay(batchDecor);
            batchDecor.end();
          }
          batchDecor.setProjectionMatrix(camera.combined);
          batchDecor.begin();
          spellRenderer.render(batchDecor);
          renderNameOverlay(batchDecor);
          floatingDamage.render(batchDecor);
          batchDecor.end();
        });
    section(
        "overlays",
        () -> {
          renderCollisionDebugOverlay();
          renderTeleportOverlay();
          renderEntityPathDebugOverlay();
          renderDayNightOverlay();
          renderBrightnessOverlay();
          renderTameProgress();
        });
    section(
        "hud",
        () -> {
          updateHudCamera();
          batch.setProjectionMatrix(hudCamera.combined);
          batch.begin();
          if (gameChat != null) gameChat.render(batch, hudCamera);
          hud.render(batch, 20, 20);
          if (coordsHudVisible && coordsHud != null) coordsHud.render(batch, 10, 20);
          if (mapZoneDisplay != null) {
            if (!mapZoneDisplay.render(batch, delta)) mapZoneDisplay = null;
          }
          updateAttackCursor();
          GuiManager.render(batch);
          batch.end();
        });
    stage.act(delta);
    stage.draw();
    if (systemMessage != null) {
      systemMessage.update(delta);
      systemMessage.render(batch);
    }
    if (frameEvent != null && frameEvent.shouldCommit()) {
      frameEvent.frameIndex = profilerFrameIndex;
      frameEvent.fps = Gdx.graphics.getFramesPerSecond();
      frameEvent.tileX = player != null ? (int) (player.getPositionVector().x / GRID_W) : 0;
      frameEvent.tileY = player != null ? (int) (player.getPositionVector().y / GRID_H) : 0;
      frameEvent.z = player != null ? player.getCoordinates().getZ() : 0;
      frameEvent.composedCacheSize =
          (mapRenderer != null && mapRenderer.getGroundRenderer() != null)
              ? mapRenderer.getGroundRenderer().getComposedCacheSize()
              : 0;
      frameEvent.commit();
    }
    profilerFrameIndex++;
  }

  private void section(String name, Runnable body) {
    if (!gameProfiler.isActive()) {
      body.run();
      return;
    }
    com.perso.T4C.profiler.SectionEvent ev = new com.perso.T4C.profiler.SectionEvent();
    ev.begin();
    body.run();
    if (ev.shouldCommit()) {
      ev.section = name;
      ev.frameIndex = profilerFrameIndex;
      ev.commit();
    }
  }

  private void toggleProfilerEnabled() {
    gameProfiler.toggle(
        () -> showSystemMessage("JFR recording started"),
        () -> showSystemMessage("JFR recording stopped"));
  }

  private void calculateRenderBounds() {
    float camX = camera.position.x;
    float camY = camera.position.y;
    float halfW = camera.viewportWidth * 0.5f;
    float halfH = camera.viewportHeight * 0.5f;
    int buffer = 3;
    renderStartX = clamp(reader.getWidth() - 1, (int) Math.floor((camX - halfW) / GRID_W) - buffer);
    renderEndX = clamp(reader.getWidth() - 1, (int) Math.ceil((camX + halfW) / GRID_W) + buffer);
    renderStartY =
        clamp(reader.getHeight() - 1, (int) Math.floor((camY - halfH) / GRID_H) - buffer);
    renderEndY = clamp(reader.getHeight() - 1, (int) Math.ceil((camY + halfH) / GRID_H) + buffer);
    decorRenderStartX = clamp(reader.getWidth() - 1, renderStartX - DECOR_RENDER_OVERHANG_TILES);
    decorRenderEndX = clamp(reader.getWidth() - 1, renderEndX + DECOR_RENDER_OVERHANG_TILES);
    decorRenderStartY = clamp(reader.getHeight() - 1, renderStartY - DECOR_RENDER_OVERHANG_TILES);
    decorRenderEndY = clamp(reader.getHeight() - 1, renderEndY + DECOR_RENDER_OVERHANG_TILES);
  }

  private void warmVisibleCaches(int buffer, int chunkBudget, int tmplBudget, int decorBudget) {
    int startX = clamp(reader.getWidth() - 1, renderStartX - buffer);
    int endX = clamp(reader.getWidth() - 1, renderEndX + buffer);
    int startY = clamp(reader.getHeight() - 1, renderStartY - buffer);
    int endY = clamp(reader.getHeight() - 1, renderEndY + buffer);
    mapRenderer.warmCaches(startX, endX, startY, endY, chunkBudget, tmplBudget, decorBudget);
  }

  private int clamp(int max, int value) {
    return Math.max(0, Math.min(max, value));
  }

  private void handleQuickbarSpell(SpellData spell, int slotNumber) {
    if (spell == null) {
      return;
    }
    boolean targetedHeal = isTargetedHealSpell(spell);
    log.debug(
        "Quickbar spell: name={}, slot={}, attack={}, targetedHeal={}, damage={}-{}, buff={}, effects={}",
        spell.getName(),
        slotNumber,
        spell.isAttack(),
        targetedHeal,
        spell.getMinDamage(),
        spell.getMaxDamage(),
        spell.getBuff(),
        spell.getT4cEffects());
    if (targetedHeal) {
      castDefensiveSpell(spell);
      return;
    }
    if (isHostileUnitSpell(spell) || isPositionTargetSpell(spell) || isTameSpell(spell)) {
      if (selectedTargetedSpell != null && selectedTargetedSlot == slotNumber) {
        clearSelectedTargetedSpell(false);
        return;
      }
      selectedTargetedSpell = spell;
      selectedTargetedSlot = slotNumber;
      if (hud != null) {
        hud.setSelectedQuickSlot(slotNumber);
      }
      return;
    }
    castDefensiveSpell(spell);
  }

  private boolean tryFireMacro(int keycode) {
    int heldModifiers = 0;
    if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
      heldModifiers |= com.perso.T4C.config.MacroBinding.MOD_CTRL;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
      heldModifiers |= com.perso.T4C.config.MacroBinding.MOD_SHIFT;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
      heldModifiers |= com.perso.T4C.config.MacroBinding.MOD_ALT;
    }
    for (com.perso.T4C.config.MacroBinding macro : GamePreferencesStore.get().getMacros()) {
      if (macro.getKeycode() != keycode || macro.getModifiers() != heldModifiers) {
        continue;
      }
      SpellData spell = SpellRegistry.findByName(macro.getSpellName());
      if (spell == null
          || player.getSpells() == null
          || !player.getSpells().contains(macro.getSpellName())) {
        showSystemMessage(
            I18n.message("message.macro_spell_unknown", I18n.resolve(macro.getSpellName())));
        return true;
      }
      handleQuickbarSpell(spell, -1);
      return true;
    }
    return false;
  }

  private void handleQuickbarItem(String itemName, int slotNumber) {
    com.perso.T4C.item.ItemUseService.Result result =
        com.perso.T4C.item.ItemUseService.useOnSelf(
            player, itemName, java.util.concurrent.ThreadLocalRandom.current());
    if (result.success()) {
      savePlayerState();
      return;
    }
    if (result.failure() == com.perso.T4C.item.ItemUseService.Failure.NO_HEALING_NEEDED) {
      showSystemMessage(I18n.message("message.no_healing_needed"));
    } else if (result.failure() == com.perso.T4C.item.ItemUseService.Failure.NO_MANA_NEEDED) {
      showSystemMessage(I18n.message("message.no_mana_needed"));
    } else if (result.failure() == com.perso.T4C.item.ItemUseService.Failure.NOT_OWNED) {
      showSystemMessage(I18n.message("message.item_not_owned"));
    } else {
      showSystemMessage(I18n.message("message.item_cannot_use", I18n.resolve(itemName)));
    }
  }

  private boolean isPositionTargetSpell(SpellData spell) {
    if (spell == null) return false;
    return spell.getTargetType() == 6 || spell.getTargetType() == 16 || spell.getTargetType() == 19;
  }

  private boolean isHostileUnitSpell(SpellData spell) {
    if (spell == null) return false;
    if (spell.isAttack() || spellEffectManager.hasVaporizeEffect(spell)) return true;
    return switch (spell.getTargetType()) {
      case 2, 8, 9, 11 -> true;
      default -> false;
    };
  }

  private boolean cancelActiveTargetedSpell() {
    if (tameChannel != null) {
      tameChannel.cancel();
    }
    if (selectedTargetedSpell == null) {
      return tameChannel != null;
    }
    clearSelectedTargetedSpell(true);
    return true;
  }

  private void clearSelectedTargetedSpell(boolean resetCursor) {
    selectedTargetedSpell = null;
    selectedTargetedSlot = 0;
    if (hud != null) {
      hud.setSelectedQuickSlot(0);
    }
    if (resetCursor) {
      applyDefaultCursor();
    }
  }

  private boolean isTargetedHealSpell(SpellData spell) {
    if (spell == null || spell.isAttack()) return false;
    if (Math.max(spell.getMinDamage(), spell.getMaxDamage()) > 0) return true;
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect == null || effect.getEffectType() != 1) continue;
      for (SpellData.T4cEffect.EffectParam parameter : effect.getParameters()) {
        if (parameter != null && parameter.getParamId() == 1 && parameter.getExpression() != null) {
          return !parameter.getExpression().stripLeading().startsWith("-");
        }
      }
    }
    return false;
  }

  private void castDefensiveSpell(SpellData spell) {
    if (spell == null || player == null) {
      return;
    }
    Gdx.app.log(
        "CastDefensive",
        "Casting: "
            + spell.getName()
            + " impact="
            + spell.getImpactSpell()
            + " sound="
            + spell.getSound()
            + " soundImpact="
            + spell.getSoundImpact()
            + " visualEffect="
            + spell.getVisualEffect()
            + " visualEffectTarget="
            + spell.getVisualEffectTarget());
    BaseMonster previousAutoCombatTarget = currentAttackTarget;
    BaseNPC previousAutoCombatNpcTarget = currentAttackNpcTarget;
    SpellData previousAutoCombatSpell = currentAttackSpell;
    long previousAutoSpellAttemptAtMs = nextAutoSpellAttemptAtMs;
    SpellCastingService.Result cast =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                spell, player, SpellCastingService.TargetKind.SELF, 0f, true, false, true));
    if (!cast.success()) {
      showSystemMessage(SpellCastingService.message(cast.failure()));
      return;
    }
    int healthDelta = spellEffectManager.resolvePlayerHealthDelta(spell, player);
    if (healthDelta > 0) player.applyHeal(healthDelta, healthDelta);
    else if (healthDelta < 0) player.takeDamage(-healthDelta);
    List<SpellData.SpellEffect> originalEffects =
        spellEffectManager.resolvePlayerBuffEffects(spell, player);
    if (!originalEffects.isEmpty()) {
      player.applyBuff(
          spell.getName(),
          spell.getDescription(),
          spell.getIconId(),
          spellEffectManager.resolveDurationSeconds(spell, player),
          false,
          originalEffects);
    } else {
      applyBuffIfNeeded(spell);
    }
    SpellEffectManager.PlayerUtility utility =
        spellEffectManager.applyPlayerUtilityEffects(spell, player);
    boolean teleported = false;
    if (utility.teleportTileX() != null
        && utility.teleportTileY() != null
        && utility.teleportWorldZ() != null) {
      player.setWorldPosition(
          utility.teleportTileX() * GRID_W,
          utility.teleportTileY() * GRID_H,
          utility.teleportWorldZ());
      teleported = true;
      savePlayerState();
    }
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    spellRenderer.playLaunchSound(visuals.launchSound());
    spellRenderer.playImpactSound(visuals.impactSound());
    String impact = visuals.impact();
    if (impact != null && !impact.isEmpty()) {
      spellRenderer.triggerImpactSpell(impact, player);
    }
    if (spell.getBuff() != null
        || !originalEffects.isEmpty()
        || utility.invisibilityApplied()
        || utility.detectInvisibleApplied()
        || utility.detectHiddenApplied()
        || utility.dispelledEffects() > 0) {
      savePlayerState();
    }
    if (!teleported && previousAutoCombatTarget != null && !previousAutoCombatTarget.isDead()) {
      currentAttackTarget = previousAutoCombatTarget;
      currentAttackNpcTarget = null;
      currentAttackSpell = previousAutoCombatSpell;
      nextAutoSpellAttemptAtMs = previousAutoSpellAttemptAtMs;
    } else if (!teleported && previousAutoCombatNpcTarget != null) {
      currentAttackTarget = null;
      currentAttackNpcTarget = previousAutoCombatNpcTarget;
      currentAttackSpell = previousAutoCombatSpell;
      nextAutoSpellAttemptAtMs = previousAutoSpellAttemptAtMs;
    }
  }

  private boolean handleBuffBarLeftClick(int screenX, int screenY) {
    if (hud == null) {
      return false;
    }
    String spellName = hud.getBuffSpellNameAt(screenX, screenY);
    if (spellName == null || spellName.isEmpty()) {
      return false;
    }
    long now = System.currentTimeMillis();
    if (spellName.equals(lastClickedBuffSpellName)
        && now - lastBuffClickMillis <= BUFF_DOUBLE_CLICK_MILLIS) {
      SpellData spell = findSpellByName(spellName);
      if (spell != null) {
        castDefensiveSpell(spell);
      }
      lastClickedBuffSpellName = null;
      lastBuffClickMillis = 0L;
      return true;
    }
    lastClickedBuffSpellName = spellName;
    lastBuffClickMillis = now;
    return true;
  }

  private SpellData findSpellByName(String spellName) {
    if (spellName == null || spellName.isEmpty()) {
      return null;
    }
    for (SpellData spell : SpellRegistry.load()) {
      if (spell != null && spellName.equals(spell.getName())) {
        return spell;
      }
    }
    return null;
  }

  private boolean tryCastAttackSpell(BaseMonster monster) {
    if (monster == null
        || monster.isDead()
        || selectedTargetedSpell == null
        || !isHostileUnitSpell(selectedTargetedSpell)
        || player == null) {
      return false;
    }
    if (!monster.canBeAttackedByPlayer()) {
      return true;
    }
    SpellData spell = selectedTargetedSpell;
    SpellCastingService.Result cast = castAttackSpell(spell, monster);
    if (!cast.success()) {
      showSystemMessage(SpellCastingService.message(cast.failure()));
    }
    if (cast.success() || isRetryableAutoSpellFailure(cast.failure())) {
      currentAttackTarget = monster;
      currentAttackNpcTarget = null;
      currentAttackSpell = spell;
      nextAutoSpellAttemptAtMs = System.currentTimeMillis() + AUTO_SPELL_RETRY_DELAY_MS;
    } else {
      clearCurrentAttackTarget();
    }
    return true;
  }

  private boolean tryCastTargetedSpell(BaseMonster monster) {
    return isTameSpellSelected() ? tryStartTame(monster) : tryCastAttackSpell(monster);
  }

  private boolean isTameSpellSelected() {
    return isTameSpell(selectedTargetedSpell);
  }

  private boolean isTameSpell(SpellData spell) {
    if (spell == null) return false;
    String key = spell.getKey();
    return "tame_beast".equals(key) || "spell.tame_beast".equals(key);
  }

  private boolean tryStartTame(BaseMonster monster) {
    if (monster == null || player == null) return true;
    MonsterDef def = MonsterRegistry.findByName(monster.getCanonicalName());
    float distance =
        player.getPositionVector().dst(monster.getPosition()) / Math.max(GRID_W, GRID_H);
    TameValidator.Failure failure =
        TameValidator.check(
            def,
            monster.isDead(),
            player.getLevel(),
            companionManager.hasCompanion(),
            distance,
            tameChannel != null);
    if (failure != TameValidator.Failure.NONE) {
      String key =
          failure == TameValidator.Failure.ALREADY_HAS_PET
              ? "message.companion_already_present"
              : "message.tame_" + failure.name().toLowerCase(Locale.ROOT);
      showSystemMessage(I18n.message(key));
      return true;
    }
    SpellData spell = selectedTargetedSpell;
    boolean sightClear =
        !spell.isLineOfSight() || hasLineOfSight(player.getPositionVector(), monster.getPosition());
    SpellCastingService.Result cast =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                spell,
                player,
                SpellCastingService.TargetKind.HOSTILE_UNIT,
                distance,
                sightClear,
                false,
                true));
    if (!cast.success()) {
      showSystemMessage(SpellCastingService.message(cast.failure()));
      return true;
    }
    float duration = parseTameDuration(spell.getDuration());
    monster.aggroOn(player.getPositionVector());
    clearCurrentAttackTarget();
    tameTarget = monster;
    tameChannel =
        new TameChannel(
            duration,
            TAME_MOVE_TOLERANCE,
            player::getPositionVector,
            player::isStunned,
            () -> tameTarget == null || tameTarget.isDead());
    tameChannelVfx = spellRenderer.startChannel(spell.getImpactSpell(), player::getPositionVector);
    showSystemMessage(I18n.message("message.tame_channeling", I18n.resolve(monster.getName())));
    return true;
  }

  private float parseTameDuration(String value) {
    try {
      return Math.max(0f, Float.parseFloat(value));
    } catch (Exception ignored) {
      return TAME_CHANNEL_SECONDS;
    }
  }

  private void updateTameChannel(float delta) {
    if (tameChannel == null) return;
    TameChannel.Outcome outcome = tameChannel.update(delta);
    if (outcome == TameChannel.Outcome.RUNNING) return;
    if (outcome == TameChannel.Outcome.SUCCESS) completeTame();
    else showSystemMessage(I18n.message("message.tame_" + outcome.name().toLowerCase(Locale.ROOT)));
    spellRenderer.stopChannel(tameChannelVfx);
    tameChannelVfx = null;
    tameChannel = null;
    tameTarget = null;
  }

  private void updateHerbViewport() {
    if (player == null || reader == null || currentMap == null) return;
    Vector2 position = player.getPositionVector();
    herbManager.updateViewport(
        renderStartX,
        renderEndX,
        renderStartY,
        renderEndY,
        reader,
        (int) (position.x / GRID_W),
        (int) (position.y / GRID_H));
  }

  private boolean tryStartHarvest(HerbNode herb) {
    if (herb == null || player == null) return false;
    if (harvestChannel != null || tameChannel != null || offensiveSpellProgressDuration > 0f) {
      showSystemMessage(I18n.message("message.harvest_busy"));
      return true;
    }
    Vector2 playerPosition = player.getPositionVector();
    int playerTileX = (int) (playerPosition.x / GRID_W);
    int playerTileY = (int) (playerPosition.y / GRID_H);
    int distance =
        Math.max(Math.abs(herb.getTileX() - playerTileX), Math.abs(herb.getTileY() - playerTileY));
    if (distance > HERB_INTERACTION_DISTANCE_TILES) {
      showSystemMessage(I18n.message("message.harvest_too_far"));
      return true;
    }
    ItemDefinition definition = ItemDefinition.get(herb.getDefinition().getItemKey());
    if (definition == null) {
      showSystemMessage(I18n.message("message.harvest_unavailable"));
      return true;
    }
    long nextWeight =
        com.perso.T4C.item.InventoryService.currentWeight(player)
            + Math.max(0L, definition.getWeight());
    if (nextWeight > com.perso.T4C.item.InventoryService.maximumWeight(player)) {
      showSystemMessage(I18n.message("message.item_too_heavy"));
      return true;
    }
    herb.setState(HerbNode.State.HARVESTING);
    harvestTarget = herb;
    harvestChannel = new HarvestChannel(HERB_HARVEST_SECONDS, .5f, player::getPositionVector);
    showSystemMessage(I18n.message("message.harvest_started", I18n.resolve(definition.getName())));
    return true;
  }

  private void updateHarvest(float delta) {
    if (harvestChannel == null) return;
    boolean completed = harvestChannel.update(delta);
    if (harvestChannel.isCancelled()) {
      herbManager.cancel(harvestTarget);
      harvestChannel = null;
      harvestTarget = null;
      showSystemMessage(I18n.message("message.harvest_cancelled_moved"));
      return;
    }
    if (!completed) return;
    HerbNode target = harvestTarget;
    com.perso.T4C.item.InventoryService.Result result =
        target == null
            ? null
            : com.perso.T4C.item.InventoryService.add(player, target.getDefinition().getItemKey());
    if (result != null && result.success() && herbManager.complete(target)) {
      ItemDefinition harvested = ItemDefinition.get(target.getDefinition().getItemKey());
      showSystemMessage(
          I18n.message(
              "message.harvest_success",
              I18n.resolve(
                  harvested == null ? target.getDefinition().getItemKey() : harvested.getName())));
      savePlayerState();
    } else {
      herbManager.cancel(target);
      showSystemMessage(I18n.message("message.harvest_unavailable"));
    }
    harvestChannel = null;
    harvestTarget = null;
  }

  private void cancelHarvest() {
    if (harvestChannel != null) harvestChannel.cancel();
    herbManager.cancel(harvestTarget);
    harvestChannel = null;
    harvestTarget = null;
  }

  private void startOffensiveSpellProgress(SpellData spell, Runnable launch) {
    long durationMillis = SpellCastingService.evaluateCastDurationMillis(spell, player);
    if (durationMillis <= 0L) {
      offensiveSpellProgressElapsed = 0f;
      offensiveSpellProgressDuration = 0f;
      pendingOffensiveSpellLaunch = null;
      launch.run();
      return;
    }
    offensiveSpellProgressElapsed = 0f;
    offensiveSpellProgressDuration = durationMillis / 1000f;
    pendingOffensiveSpellLaunch = launch;
  }

  private void updateOffensiveSpellProgress(float delta) {
    if (offensiveSpellProgressDuration <= 0f) return;
    offensiveSpellProgressElapsed += Math.max(0f, delta);
    if (offensiveSpellProgressElapsed >= offensiveSpellProgressDuration) {
      Runnable launch = pendingOffensiveSpellLaunch;
      offensiveSpellProgressElapsed = 0f;
      offensiveSpellProgressDuration = 0f;
      pendingOffensiveSpellLaunch = null;
      if (launch != null) launch.run();
    }
  }

  private float getCastProgress() {
    if (harvestChannel != null) return harvestChannel.getProgress();
    if (tameChannel != null) return tameChannel.getProgress();
    if (offensiveSpellProgressDuration <= 0f) return hasQueuedOffensiveCast() ? 1f : 0f;
    return Math.min(1f, offensiveSpellProgressElapsed / offensiveSpellProgressDuration);
  }

  private boolean hasQueuedOffensiveCast() {
    if (currentAttackSpell == null
        || player == null
        || SpellCastingService.evaluateCastDurationMillis(currentAttackSpell, player) <= 0L) {
      return false;
    }
    if (currentAttackTarget != null) {
      return !currentAttackTarget.isDead() && currentAttackTarget.canBeAttackedByPlayer();
    }
    return currentAttackNpcTarget != null;
  }

  private void completeTame() {
    BaseMonster target = tameTarget;
    if (target == null) return;
    MonsterDef def = MonsterRegistry.findByName(target.getCanonicalName());
    CompanionDef companionDef = TamedCompanionFactory.fromMonster(def);
    if (companionDef == null) return;
    Vector2 where = new Vector2(target.getPosition());
    String name = I18n.resolve(target.getName());
    monsterManager.despawnMonster(target);
    CompanionCastVfxHook.playVanish(where.x, where.y);
    if (companionManager.spawnCompanionFromDef(player, companionDef, where, 0)) {
      savePlayerState();
      showSystemMessage(I18n.message("message.tame_success", name));
    }
  }

  private void restorePersistedCompanion() {
    if (initialPlayerState == null || initialPlayerState.companion == null) return;
    var saved = initialPlayerState.companion;
    CompanionDef def =
        saved.tamed
            ? TamedCompanionFactory.fromSpeciesName(saved.speciesName)
            : CompanionRegistry.findById(saved.speciesName);
    if (def == null) {
      log.warn("Persisted companion {} no longer exists", saved.speciesName);
      return;
    }
    if (!companionManager.spawnCompanionFromDef(
        player, def, player.getPositionVector(), saved.currentHp)) return;
    if (saved.mode != null)
      try {
        companionManager.getCompanion().setMode(CompanionMode.valueOf(saved.mode));
      } catch (IllegalArgumentException ex) {
        log.warn("Unknown persisted companion mode {}", saved.mode);
      }
  }

  private boolean tryCastAttackSpell(BaseNPC npc) {
    if (npc == null
        || selectedTargetedSpell == null
        || !isHostileUnitSpell(selectedTargetedSpell)
        || player == null) {
      return false;
    }
    SpellData spell = selectedTargetedSpell;
    SpellCastingService.Result cast = castAttackSpell(spell, npc);
    if (!cast.success()) {
      showSystemMessage(SpellCastingService.message(cast.failure()));
    }
    if (cast.success() || isRetryableAutoSpellFailure(cast.failure())) {
      currentAttackTarget = null;
      currentAttackNpcTarget = npc;
      currentAttackSpell = spell;
      nextAutoSpellAttemptAtMs = System.currentTimeMillis() + AUTO_SPELL_RETRY_DELAY_MS;
    } else {
      clearCurrentAttackTarget();
    }
    return true;
  }

  private SpellCastingService.Result castAttackSpell(SpellData spell, BaseMonster monster) {
    float distanceTiles =
        player.getPositionVector().dst(monster.getPosition()) / Math.max(GRID_W, GRID_H);
    boolean sightClear =
        !spell.isLineOfSight() || hasLineOfSight(player.getPositionVector(), monster.getPosition());
    SpellCastingService.Result cast =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                spell,
                player,
                SpellCastingService.TargetKind.HOSTILE_UNIT,
                distanceTiles,
                sightClear,
                false,
                true));
    if (!cast.success()) {
      return cast;
    }
    Vector2 playerPos = player.getPositionVector();
    Vector2 monsterPos = monster.getPosition();
    player.getMovement().faceToward(playerPos.x, playerPos.y, monsterPos.x, monsterPos.y);
    startOffensiveSpellProgress(spell, () -> launchAttackSpell(spell, monster));
    return cast;
  }

  private void launchAttackSpell(SpellData spell, BaseMonster monster) {
    if (monster == null || monster.isDead()) return;
    Vector2 playerPos = player.getPositionVector();
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    spellRenderer.playLaunchSound(visuals.launchSound());
    float startX = playerPos.x;
    float startY = playerPos.y;
    String projectileSpell = visuals.projectile();
    if (projectileSpell == null || projectileSpell.isEmpty()) {
      applySpellImpact(spell, monster);
    } else {
      boolean launched = false;
      if (spell.isLineOfSight()) {
        ProjectileDirection dir =
            computeProjectileDirection(player.getPositionVector(), monster.getPosition());
        String directionalName = projectileSpell + dir.angle;
        launched =
            spellRenderer.launchProjectile(
                directionalName,
                monster,
                startX,
                startY,
                dir.flipX,
                () -> applySpellImpact(spell, monster));
        if (!launched) {
          ProjectileDirection fallback =
              computeFlipFallbackDirection(player.getPositionVector(), monster.getPosition());
          String fallbackName = projectileSpell + fallback.angle;
          launched =
              spellRenderer.launchProjectile(
                  fallbackName,
                  monster,
                  startX,
                  startY,
                  fallback.flipX,
                  () -> applySpellImpact(spell, monster));
        }
      }
      if (!launched) {
        launched =
            spellRenderer.launchProjectile(
                projectileSpell,
                monster,
                startX,
                startY,
                false,
                () -> applySpellImpact(spell, monster));
      }
      if (!launched) {
        applySpellImpact(spell, monster);
      }
    }
  }

  private SpellCastingService.Result castAttackSpell(SpellData spell, BaseNPC npc) {
    Vector2 playerPos = player.getPositionVector();
    Vector2 npcPos = npc.getPosition();
    float distanceTiles = playerPos.dst(npcPos) / Math.max(GRID_W, GRID_H);
    boolean sightClear = !spell.isLineOfSight() || hasLineOfSight(playerPos, npcPos);
    SpellCastingService.Result cast =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                spell,
                player,
                SpellCastingService.TargetKind.HOSTILE_UNIT,
                distanceTiles,
                sightClear,
                false,
                true));
    if (!cast.success()) {
      return cast;
    }
    playerPos = player.getPositionVector();
    npcPos = npc.getPosition();
    player.getMovement().faceToward(playerPos.x, playerPos.y, npcPos.x, npcPos.y);
    startOffensiveSpellProgress(spell, () -> launchAttackSpell(spell, npc));
    return cast;
  }

  private void launchAttackSpell(SpellData spell, BaseNPC npc) {
    if (npc == null) return;
    Vector2 playerPos = player.getPositionVector();
    Vector2 npcPos = npc.getPosition();
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    spellRenderer.playLaunchSound(visuals.launchSound());
    float startX = playerPos.x;
    float startY = playerPos.y;
    String projectileSpell = visuals.projectile();
    if (projectileSpell == null || projectileSpell.isEmpty()) {
      applyNpcSpellImpact(spell, npc);
      return;
    }
    boolean launched = false;
    if (spell.isLineOfSight()) {
      ProjectileDirection direction = computeProjectileDirection(playerPos, npcPos);
      launched =
          spellRenderer.launchProjectile(
              projectileSpell + direction.angle,
              npc,
              startX,
              startY,
              direction.flipX,
              () -> applyNpcSpellImpact(spell, npc));
      if (!launched) {
        ProjectileDirection fallback = computeFlipFallbackDirection(playerPos, npcPos);
        launched =
            spellRenderer.launchProjectile(
                projectileSpell + fallback.angle,
                npc,
                startX,
                startY,
                fallback.flipX,
                () -> applyNpcSpellImpact(spell, npc));
      }
    }
    if (!launched) {
      launched =
          spellRenderer.launchProjectile(
              projectileSpell, npc, startX, startY, false, () -> applyNpcSpellImpact(spell, npc));
    }
    if (!launched) {
      applyNpcSpellImpact(spell, npc);
    }
  }

  private void applyNpcSpellImpact(SpellData spell, BaseNPC npc) {
    if (spell == null || npc == null) {
      return;
    }
    if (npcManager != null) {
      npcManager.onNpcAttacked(npc, player);
    }
    boolean vaporize = spellEffectManager.hasVaporizeEffect(spell);
    if (vaporize) {
      if (npcManager != null) {
        npcManager.damageNpc(npc, Math.max(1, npc.getCurrentHp()), player);
      }
    }
    int healthDelta = spellEffectManager.resolvePlayerHealthDelta(spell, player);
    if (!vaporize && healthDelta < 0 && npcManager != null) {
      int damage = -healthDelta;
      npcManager.damageNpc(npc, damage, player);
      Vector2 position = npc.getPosition();
      floatingDamage.spawn(damage, position.x, position.y, FloatingDamage.Type.MONSTER_RECEIVED);
    } else if (!vaporize && healthDelta > 0) {
      npc.setCurrentHp(Math.min(npc.getMaxHp(), npc.getCurrentHp() + healthDelta));
    }
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    String impact = visuals.impact();
    if (impact != null && !impact.isEmpty()) {
      Vector2 position = npc.getPosition();
      spellRenderer.triggerImpactSpell(impact, position.x, position.y, visuals.impactSound());
    }
  }

  private static boolean isRetryableAutoSpellFailure(SpellCastingService.Failure failure) {
    return failure == SpellCastingService.Failure.COOLDOWN
        || failure == SpellCastingService.Failure.EXHAUSTED
        || failure == SpellCastingService.Failure.ACTIVATION_FAILED;
  }

  private boolean tryBowAttack(BaseMonster monster) {
    if (monster == null || monster.isDead() || player == null) {
      return false;
    }
    String weaponName = player.getEquippedItems().get(BodyPart.WEAPON);
    ItemDefinition weapon = weaponName == null ? null : ItemDefinition.get(weaponName);
    if (weapon == null || !weapon.isBow()) {
      return false;
    }
    if (!hasQuiverEquipped()) {
      showSystemMessage(I18n.message("message.no_quiver_equipped"));
      clearCurrentAttackTarget();
      return true;
    }
    if (!monster.canBeAttackedByPlayer()) {
      return true;
    }
    if (!canFireBowAt(monster, true)) {
      clearCurrentAttackTarget();
      return true;
    }
    currentAttackTarget = monster;
    currentAttackNpcTarget = null;
    currentAttackSpell = null;
    nextAutoSpellAttemptAtMs = 0L;
    if (player.isAttackReady() && fireBow(monster, false)) {
      player.triggerAttackCooldown(resolveEquippedWeaponAttackSpeed());
    }
    return true;
  }

  private boolean fireBow(BaseMonster monster, boolean announce) {
    if (!canFireBowAt(monster, announce)) {
      return false;
    }
    Vector2 playerPos = player.getPositionVector();
    Vector2 monsterPos = monster.getPosition();
    player.getMovement().faceToward(playerPos.x, playerPos.y, monsterPos.x, monsterPos.y);
    float startX = playerPos.x;
    float startY = playerPos.y;
    ProjectileDirection dir = computeFlipFallbackDirection(playerPos, monsterPos);
    String projectileName = BOW_PROJECTILE_SPRITE + dir.angle;
    String fallbackProjectileName = BOW_PROJECTILE_SPRITE + "000";
    boolean launched =
        spellRenderer.launchProjectile(
            projectileName, monster, startX, startY, dir.flipX, () -> applyBowImpact(monster));
    if (!launched) {
      launched =
          spellRenderer.launchProjectile(
              fallbackProjectileName,
              monster,
              startX,
              startY,
              false,
              () -> applyBowImpact(monster));
    }
    if (!launched) return false;
    player.attack(player.getMovement(), true);
    return true;
  }

  private boolean hasQuiverEquipped() {
    if (player == null) {
      return false;
    }
    String key = player.getEquippedItems().get(BodyPart.WEAPON2);
    ItemDefinition quiver = key == null ? null : ItemDefinition.get(key);
    return quiver != null && quiver.getStructure() == QUIVER_STRUCTURE_ID;
  }

  private boolean canFireBowAt(BaseMonster monster, boolean announce) {
    if (monster == null || monster.isDead() || player == null) {
      return false;
    }
    if (!hasQuiverEquipped()) {
      if (announce) showSystemMessage(I18n.message("message.no_quiver_equipped"));
      return false;
    }
    Vector2 playerPos = player.getPositionVector();
    Vector2 monsterPos = monster.getPosition();
    if (playerPos.dst(monsterPos) > BOW_ATTACK_RANGE) {
      if (announce) showSystemMessage(I18n.message("message.target_too_far"));
      return false;
    }
    if (!hasLineOfSight(playerPos, monsterPos)) {
      if (announce) showSystemMessage(I18n.message("message.target_no_line_of_sight"));
      return false;
    }
    return true;
  }

  private double resolveEquippedWeaponAttackSpeed() {
    if (player == null) {
      return 1.0;
    }
    String weaponName = player.getEquippedItems().get(BodyPart.WEAPON);
    ItemDefinition weapon = weaponName == null ? null : ItemDefinition.get(weaponName);
    if (weapon == null) {
      return 1.0;
    }
    String attackDelay = weapon.getAtkDelay();
    if (attackDelay != null && !attackDelay.isBlank()) {
      int delayMillis = DiceFormula.of(attackDelay).roll();
      if (delayMillis > 0) {
        return 1000d / delayMillis;
      }
      log.warn("Invalid attack delay '{}' for weapon {}", attackDelay, weapon.getKey());
    }
    if (weapon.getAttackSpeed() <= 0) {
      return 1.0;
    }
    return weapon.getAttackSpeed();
  }

  private void performAttackTick() {
    if (player == null) {
      return;
    }
    if (currentAttackNpcTarget != null) {
      if (currentAttackSpell == null) {
        clearCurrentAttackTarget();
      } else {
        performAutoNpcSpellTick();
      }
      return;
    }
    if (currentAttackTarget == null) return;
    if (currentAttackTarget.isDead() || !currentAttackTarget.canBeAttackedByPlayer()) {
      clearCurrentAttackTarget();
      return;
    }
    if (companionManager != null) {
      companionManager.onPlayerAttacked(currentAttackTarget);
    }
    if (currentAttackSpell != null) {
      performAutoSpellTick();
      return;
    }
    if (!player.isAttackReady()) {
      return;
    }
    boolean attacked;
    if (PlayerAppearanceDefaults.hasBowEquipped(player)) {
      attacked = fireBow(currentAttackTarget, false);
    } else {
      attacked =
          monsterManager != null && monsterManager.attackMonster(currentAttackTarget, player);
    }
    if (attacked) {
      player.triggerAttackCooldown(resolveEquippedWeaponAttackSpeed());
    } else {
      player.triggerAttackCooldown(4.0d);
    }
  }

  private void performAutoSpellTick() {
    if (player.getMovement().isMoving()) {
      clearCurrentAttackTarget();
      return;
    }
    SpellData spell = currentAttackSpell;
    BaseMonster target = currentAttackTarget;
    long now = System.currentTimeMillis();
    if (spell == null
        || target == null
        || now < nextAutoSpellAttemptAtMs
        || player.isSpellOnCooldown(spell.getName())
        || player.isMentallyExhausted()
        || player.isStunned()) {
      return;
    }
    SpellCastingService.Result cast = castAttackSpell(spell, target);
    nextAutoSpellAttemptAtMs = now + AUTO_SPELL_RETRY_DELAY_MS;
    if (cast.success() || isRetryableAutoSpellFailure(cast.failure())) {
      if (!target.isDead()) {
        currentAttackTarget = target;
        currentAttackSpell = spell;
      }
      return;
    }
    showSystemMessage(SpellCastingService.message(cast.failure()));
    clearCurrentAttackTarget();
  }

  private void performAutoNpcSpellTick() {
    if (player.getMovement().isMoving()) {
      clearCurrentAttackTarget();
      return;
    }
    SpellData spell = currentAttackSpell;
    BaseNPC target = currentAttackNpcTarget;
    long now = System.currentTimeMillis();
    if (spell == null
        || target == null
        || now < nextAutoSpellAttemptAtMs
        || player.isSpellOnCooldown(spell.getName())
        || player.isMentallyExhausted()
        || player.isStunned()) {
      return;
    }
    SpellCastingService.Result cast = castAttackSpell(spell, target);
    nextAutoSpellAttemptAtMs = now + AUTO_SPELL_RETRY_DELAY_MS;
    if (cast.success() || isRetryableAutoSpellFailure(cast.failure())) {
      currentAttackTarget = null;
      currentAttackNpcTarget = target;
      currentAttackSpell = spell;
      return;
    }
    showSystemMessage(SpellCastingService.message(cast.failure()));
    clearCurrentAttackTarget();
  }

  private void beginAttackTarget(BaseMonster monster) {
    if (monster == null || player == null) {
      return;
    }
    if (PlayerAppearanceDefaults.hasBowEquipped(player) && !canFireBowAt(monster, true)) {
      clearCurrentAttackTarget();
      return;
    }
    currentAttackTarget = monster;
    currentAttackNpcTarget = null;
    currentAttackSpell = null;
    nextAutoSpellAttemptAtMs = 0L;
    if (player.isAttackReady()) {
      boolean attacked;
      if (PlayerAppearanceDefaults.hasBowEquipped(player)) {
        attacked = fireBow(monster, false);
      } else {
        attacked = monsterManager != null && monsterManager.attackMonster(monster, player);
      }
      if (attacked) {
        player.triggerAttackCooldown(resolveEquippedWeaponAttackSpeed());
      }
    }
  }

  private boolean isTextInputActive() {
    return textInputActiveSupplier != null && textInputActiveSupplier.getAsBoolean();
  }

  private boolean targetNearestMonster() {
    if (player == null || monsterManager == null) {
      return false;
    }
    Vector2 playerPos = player.getPositionVector();
    float maxDistance = TAB_TARGET_RANGE_TILES * Math.max(GRID_W, GRID_H);
    List<BaseMonster> candidates = new ArrayList<>();
    for (BaseMonster monster : monsterManager.getMonsters()) {
      if (monster == null || monster.isDead() || !monster.canBeAttackedByPlayer()) {
        continue;
      }
      if (playerPos.dst(monster.getPosition()) > maxDistance) {
        continue;
      }
      candidates.add(monster);
    }
    if (candidates.isEmpty()) {
      clearSelectedMonster();
      showSystemMessage(I18n.message("message.no_target_nearby"));
      return false;
    }
    candidates.sort(java.util.Comparator.comparingDouble(m -> playerPos.dst2(m.getPosition())));
    int index = candidates.indexOf(selectedMonster);
    BaseMonster next = candidates.get((index + 1) % candidates.size());
    setSelectedMonster(next);
    return true;
  }

  private void setSelectedMonster(BaseMonster monster) {
    clearSelectedMonster();
    selectedMonster = monster;
    if (monster != null) {
      monster.setSelected(true);
    }
  }

  private void pruneSelectedMonster() {
    if (selectedMonster == null) {
      return;
    }
    float maxDistance = TAB_TARGET_RANGE_TILES * Math.max(GRID_W, GRID_H);
    if (selectedMonster.isDead()
        || player.getPositionVector().dst(selectedMonster.getPosition()) > maxDistance) {
      clearSelectedMonster();
    }
  }

  private void clearSelectedMonster() {
    if (selectedMonster != null) {
      selectedMonster.setSelected(false);
      selectedMonster = null;
    }
  }

  private void clearCurrentAttackTarget() {
    currentAttackTarget = null;
    currentAttackNpcTarget = null;
    currentAttackSpell = null;
    nextAutoSpellAttemptAtMs = 0L;
  }

  private void applyBowImpact(BaseMonster monster) {
    if (monster == null || monster.isDead()) {
      return;
    }
    int rawDamage = com.perso.T4C.helper.CombatMath.computeBowDamage(player);
    CombatResult result =
        CombatResolver.resolve(
            new PhysicalAttackRequest(
                CombatProfiles.fromPlayer(player),
                CombatProfiles.fromMonster(monster),
                rawDamage,
                0,
                true),
            ThreadLocalRandom.current());
    player.setHidden(false);
    monster.aggroOn(player.getPositionVector());
    int dmg = result.damage();
    boolean wasDead = monster.isDead();
    int appliedPrimaryDamage = 0;
    if (result.hit()) {
      com.perso.T4C.item.ItemDurabilityService.damageEquipped(
          player,
          com.perso.T4C.player.BodyPart.WEAPON,
          com.perso.T4C.item.ItemDurabilityService.COMBAT_WEAR);
      handleSeraphAuraAttackHit();
      if (!monster.isDead()) {
        if (monster.isStunned()) monster.clearStun();
        monster.stunFor(result.stunDurationMillis());
        if (dmg > 0) {
          monster.applyPlayerDamage(dmg, player, xpCurve);
          appliedPrimaryDamage = dmg;
        }
      }
    }
    if (!wasDead && monster.isDead() && monsterManager != null) {
      if (appliedPrimaryDamage > 0) {
        monsterManager.notifyKilledByPlayer(monster);
      }
    }
    Vector2 pos = monster.getPosition();
    if (appliedPrimaryDamage > 0) {
      floatingDamage.spawn(
          appliedPrimaryDamage, pos.x, pos.y, FloatingDamage.Type.MONSTER_RECEIVED);
    } else if (!result.hit()) {
      showMissFeedback(result, pos);
    }
  }

  private void showMissFeedback(CombatResult result, Vector2 position) {
    if (result == null || position == null) {
      return;
    }
    String label =
        result.parried() ? I18n.message("combat.parried") : I18n.message("combat.missed");
    floatingDamage.spawnText(label, position.x, position.y, FloatingDamage.Type.MISS);
  }

  private void handleSeraphAuraOnHit(BaseMonster attacker, double attackerArmorClass) {
    if (player == null || attacker == null) {
      return;
    }
    SeraphAuraService.OnHitResult proc =
        SeraphAuraService.onHit(
            player,
            attacker.getElementResistance(1),
            attackerArmorClass,
            ThreadLocalRandom.current());
    Vector2 playerPosition = player.getPositionVector().cpy();
    boolean animate = com.perso.T4C.config.GamePreferencesStore.get().isSeraphAnimation();
    if (proc.healingTriggered()) {
      applySeraphAuraHealing(proc.centralHealing(), playerPosition);
      applySeraphAuraHealing(proc.radialHealing(), playerPosition);
      if (animate)
        spellRenderer.triggerImpactSpell(
            SeraphAuraService.HEAL_IMPACT,
            playerPosition.x,
            playerPosition.y,
            SeraphAuraService.HEAL_SOUND);
      if (animate)
        spellRenderer.triggerImpactSpell(
            SeraphAuraService.HEAL_RADIAL_EFFECT, playerPosition.x, playerPosition.y);
    }
    if (proc.retaliationTriggered() && !attacker.isDead()) {
      Runnable impact =
          () -> {
            Vector2 currentPosition = attacker.getPosition();
            spellRenderer.triggerImpactSpell(
                SeraphAuraService.SINGLE_IMPACT,
                currentPosition.x,
                currentPosition.y,
                SeraphAuraService.EXPLOSION_SOUND);
          };
      if (animate) {
        boolean launched =
            spellRenderer.launchProjectile(
                SeraphAuraService.SINGLE_PROJECTILE,
                attacker,
                playerPosition.x,
                playerPosition.y,
                false,
                impact);
        if (launched) spellRenderer.playLaunchSound(SeraphAuraService.HEAL_SOUND);
        else impact.run();
      }
      applySeraphAuraDamage(attacker, proc.retaliationDamage());
    }
  }

  private void applySeraphAuraHealing(int amount, Vector2 position) {
    if (amount <= 0) {
      return;
    }
    int healthBefore = player.getCurrentHp();
    player.applyHeal(amount, amount);
    int appliedHealing = player.getCurrentHp() - healthBefore;
    if (appliedHealing > 0) {
      floatingDamage.spawn(appliedHealing, position.x, position.y, FloatingDamage.Type.HEAL);
    }
  }

  private void handleSeraphAuraAttackHit() {
    if (player == null || monsterManager == null) {
      return;
    }
    SeraphAuraService.OnAttackHitResult proc =
        SeraphAuraService.onAttackHit(player, ThreadLocalRandom.current());
    if (!proc.triggered()) {
      return;
    }
    Vector2 center = player.getPositionVector().cpy();
    boolean animate = com.perso.T4C.config.GamePreferencesStore.get().isSeraphAnimation();
    if (animate)
      spellRenderer.triggerImpactSpell(
          SeraphAuraService.AREA_CENTER_IMPACT,
          center.x,
          center.y,
          SeraphAuraService.EXPLOSION_SOUND);
    for (BaseMonster candidate : monsterManager.getMonsters()) {
      if (candidate == null || candidate.isDead()) {
        continue;
      }
      Vector2 targetPosition = candidate.getPosition();
      double dxTiles = (targetPosition.x - center.x) / GRID_W;
      double dyTiles = (targetPosition.y - center.y) / GRID_H;
      if (Math.hypot(dxTiles, dyTiles) >= proc.radius()
          || !hasLineOfSight(center, targetPosition)) {
        continue;
      }
      int damage =
          SeraphAuraService.rollAreaDamage(
              player,
              candidate.getElementResistance(1),
              candidate.rollCombatArmorClass(),
              ThreadLocalRandom.current());
      ProjectileDirection direction = computeFlipFallbackDirection(center, targetPosition);
      String projectile = SeraphAuraService.AREA_PROJECTILE + direction.angle;
      Runnable impact =
          () -> {
            Vector2 currentPosition = candidate.getPosition();
            spellRenderer.triggerImpactSpell(
                SeraphAuraService.AREA_CENTER_IMPACT,
                currentPosition.x,
                currentPosition.y,
                SeraphAuraService.EXPLOSION_SOUND);
          };
      if (animate) {
        boolean launched =
            spellRenderer.launchProjectile(
                projectile, candidate, center.x, center.y, direction.flipX, impact);
        if (!launched)
          launched =
              spellRenderer.launchProjectile(
                  SeraphAuraService.AREA_PROJECTILE + "000",
                  candidate,
                  center.x,
                  center.y,
                  false,
                  impact);
        if (launched) spellRenderer.playLaunchSound(SeraphAuraService.FIREBALL_SOUND);
        else impact.run();
      }
      applySeraphAuraDamage(candidate, damage);
    }
  }

  private void applySeraphAuraDamage(BaseMonster target, int damage) {
    if (target == null || target.isDead() || damage <= 0) {
      return;
    }
    boolean wasDead = target.isDead();
    target.applyPlayerDamage(damage, player, xpCurve);
    Vector2 position = target.getPosition();
    floatingDamage.spawn(damage, position.x, position.y, FloatingDamage.Type.MONSTER_RECEIVED);
    if (!wasDead && target.isDead() && monsterManager != null) {
      monsterManager.notifyKilledByPlayer(target);
    }
  }

  private boolean tryCastSelectedHealOnPlayer(int screenX, int screenY) {
    if (!isTargetedHealSpell(selectedTargetedSpell) || player == null) {
      return false;
    }
    if (hud != null && hud.isQuickBarHit(screenX, screenY)) {
      return false;
    }
    Vector3 worldCoords = camera.unproject(clickWorldCoordsTemp.set(screenX, screenY, 0));
    if (!player.isMouseOver(worldCoords.x, worldCoords.y)) {
      return false;
    }
    castDefensiveSpell(selectedTargetedSpell);
    return true;
  }

  private void applySpellImpact(SpellData spell, BaseMonster monster) {
    if (spell == null || monster == null || monster.isDead()) {
      return;
    }
    applyResolvedSpellImpact(spell, monster, 0d, true);
    if (spell.getRadius() > 0 && monsterManager != null) {
      Vector2 center = monster.getPosition();
      for (BaseMonster candidate : monsterManager.getMonsters()) {
        if (candidate == monster || candidate.isDead()) continue;
        double distanceTiles = center.dst(candidate.getPosition()) / Math.max(GRID_W, GRID_H);
        if (distanceTiles >= spell.getRadius()) continue;
        if (spell.isLineOfSight() && !hasLineOfSight(center, candidate.getPosition())) continue;
        applyResolvedSpellImpact(spell, candidate, distanceTiles, true);
      }
    }
  }

  private void applyResolvedSpellImpact(
      SpellData spell, BaseMonster monster, double range, boolean installHooks) {
    SpellEffectManager.Impact impactResult =
        spellEffectManager.resolve(spell, player, monster, range);
    SpellEffectManager.TargetExhaustion explicitExhaustion =
        spellEffectManager.resolveExplicitTargetExhaustion(spell, player, monster);
    monster.applyExhaustion(
        explicitExhaustion.attackMillis(),
        explicitExhaustion.mentalMillis(),
        explicitExhaustion.moveMillis());
    if (impactResult.vaporize()) {
      monster.takeDamage(Math.max(1, monster.getHealth()));
    }
    int healthDelta = impactResult.healthDelta();
    if (!impactResult.vaporize() && healthDelta < 0) {
      int dmg = -healthDelta;
      boolean wasDead = monster.isDead();
      monster.applyPlayerDamage(dmg, player, xpCurve);
      if (!wasDead && monster.isDead() && monsterManager != null) {
        monsterManager.notifyKilledByPlayer(monster);
      }
      Vector2 pos = monster.getPosition();
      floatingDamage.spawn(dmg, pos.x, pos.y, FloatingDamage.Type.MONSTER_RECEIVED);
    } else if (!impactResult.vaporize() && healthDelta > 0) {
      monster.heal(healthDelta);
    }
    if (impactResult.drainedHealth() > 0) {
      player.applyHeal(impactResult.drainedHealth(), impactResult.drainedHealth());
    }
    if (installHooks) spellEffectManager.installTimedHooks(spell, player, monster);
    applySummons(impactResult.summons(), monster.getPosition().x, monster.getPosition().y);
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    String impact = visuals.impact();
    if (impact != null && !impact.isEmpty()) {
      Vector2 pos = monster.getPosition();
      spellRenderer.triggerImpactSpell(impact, pos.x, pos.y, visuals.impactSound());
    }
  }

  private void applySummons(
      List<SpellEffectManager.SummonRequest> summons, float worldX, float worldY) {
    for (SpellEffectManager.SummonRequest summon : summons) {
      if (("npc".equalsIgnoreCase(summon.type()) || "monster".equalsIgnoreCase(summon.type()))
          && monsterManager != null) {
        monsterManager.spawnMonster(summon.definitionKey(), worldX, worldY);
      } else if ("object".equalsIgnoreCase(summon.type())) {
        com.perso.T4C.item.InventoryService.add(player, summon.definitionKey());
      }
    }
  }

  private boolean tryCastPositionSpell(int screenX, int screenY) {
    SpellData spell = selectedTargetedSpell;
    if (spell == null || !isPositionTargetSpell(spell) || player == null) return false;
    if (hud != null && hud.isQuickBarHit(screenX, screenY)) return false;
    Vector3 target = camera.unproject(clickWorldCoordsTemp.set(screenX, screenY, 0f));
    Vector2 targetPosition = new Vector2(target.x, target.y);
    float distanceTiles = player.getPositionVector().dst(targetPosition) / Math.max(GRID_W, GRID_H);
    boolean sightClear =
        !spell.isLineOfSight() || hasLineOfSight(player.getPositionVector(), targetPosition);
    SpellCastingService.Result cast =
        SpellCastingService.begin(
            new SpellCastingService.Request(
                spell,
                player,
                SpellCastingService.TargetKind.POSITION,
                distanceTiles,
                sightClear,
                false,
                true));
    if (!cast.success()) {
      showSystemMessage(SpellCastingService.message(cast.failure()));
      return true;
    }
    if (spell.isAttack()) {
      Vector2 castTarget = new Vector2(targetPosition);
      startOffensiveSpellProgress(spell, () -> launchPositionSpell(spell, castTarget));
      return true;
    }
    launchPositionSpell(spell, targetPosition);
    return true;
  }

  private void launchPositionSpell(SpellData spell, Vector2 targetPosition) {
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    spellRenderer.playLaunchSound(visuals.launchSound());
    applySummons(
        spellEffectManager.resolvePositionSummons(spell), targetPosition.x, targetPosition.y);
    if (spell.getRadius() > 0 && monsterManager != null) {
      for (BaseMonster candidate : monsterManager.getMonsters()) {
        if (candidate.isDead()) continue;
        double range = targetPosition.dst(candidate.getPosition()) / Math.max(GRID_W, GRID_H);
        if (range >= spell.getRadius()) continue;
        if (spell.isLineOfSight() && !hasLineOfSight(targetPosition, candidate.getPosition()))
          continue;
        applyResolvedSpellImpact(spell, candidate, range, true);
      }
    }
    if (visuals.impact() != null && !visuals.impact().isEmpty()) {
      spellRenderer.triggerImpactSpell(
          visuals.impact(), targetPosition.x, targetPosition.y, visuals.impactSound());
    }
  }

  private boolean hasLineOfSight(Vector2 from, Vector2 to) {
    if (from == null || to == null) {
      return true;
    }
    if (!CollisionManager.getInstance().isInitialized()) {
      return true;
    }
    float dx = to.x - from.x;
    float dy = to.y - from.y;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);
    if (distance <= 0.001f) {
      return true;
    }
    float step = Math.min(GRID_W, GRID_H) * 0.5f;
    int steps = Math.max(1, (int) Math.ceil(distance / step));
    float stepX = dx / steps;
    float stepY = dy / steps;
    for (int i = 1; i < steps; i++) {
      float checkX = from.x + stepX * i;
      float checkY = from.y + stepY * i;
      if (CollisionManager.getInstance().blocksLineOfSight(checkX, checkY)) {
        return false;
      }
    }
    return true;
  }

  private boolean blocksSpellLineOfSight(float worldX, float worldY) {
    if (reader == null) {
      return true;
    }
    int gridX = (int) (worldX / GRID_W);
    int gridY = (int) (worldY / GRID_H);
    if (gridX < 0 || gridY < 0 || gridX >= reader.getWidth() || gridY >= reader.getHeight()) {
      return true;
    }
    String ground = normalizedGroundName(gridX, gridY);
    if (ground.contains("water")) {
      return false;
    }
    if (ground.startsWith("tmpl") && hasAdjacentWaterGround(gridX, gridY)) {
      return false;
    }
    return true;
  }

  private boolean hasAdjacentWaterGround(int gridX, int gridY) {
    return isWaterGround(gridX - 1, gridY)
        || isWaterGround(gridX + 1, gridY)
        || isWaterGround(gridX, gridY - 1)
        || isWaterGround(gridX, gridY + 1);
  }

  private boolean isWaterGround(int gridX, int gridY) {
    if (gridX < 0 || gridY < 0 || gridX >= reader.getWidth() || gridY >= reader.getHeight()) {
      return false;
    }
    return normalizedGroundName(gridX, gridY).contains("water");
  }

  private String normalizedGroundName(int gridX, int gridY) {
    String name = reader.getGroundSpriteName(gridX, gridY);
    if (name == null) {
      return "";
    }
    int metadataStart = name.indexOf('[');
    if (metadataStart >= 0) {
      name = name.substring(0, metadataStart);
    }
    return name.trim().toLowerCase(Locale.ROOT).replace(" ", "");
  }

  private void toggleCollisionDebugOverlay() {
    collisionDebugVisible = !collisionDebugVisible;
    showSystemMessage("Collision debug: " + (collisionDebugVisible ? "ON" : "OFF"));
  }

  private boolean isTeleportSourceTile(int tileX, int tileY) {
    if (player == null) return false;
    int z = player.getCoordinates().getZ();
    for (TeleportEntry teleport : teleports) {
      if (teleport.sourceZ == z && teleport.sourceX == tileX && teleport.sourceY == tileY)
        return true;
    }
    return false;
  }

  private void toggleTeleportOverlay() {
    teleportOverlayVisible = !teleportOverlayVisible;
    showSystemMessage("Teleport debug: " + (teleportOverlayVisible ? "ON" : "OFF"));
  }

  private void renderTeleportOverlay() {
    if (!teleportOverlayVisible || teleports.isEmpty() || player == null) return;
    int currentZ = player.getCoordinates().getZ();
    debugShapeRenderer.setProjectionMatrix(camera.combined);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    TeleportOverlayRenderer.render(
        debugShapeRenderer,
        teleports,
        currentZ,
        renderStartX,
        renderEndX,
        renderStartY,
        renderEndY);
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private void toggleCoordsHud() {
    coordsHudVisible = !coordsHudVisible;
  }

  private void showSystemMessage(String message) {
    if (systemMessage != null) {
      systemMessage.show(message);
    } else if (gameChat != null) {
      gameChat.addSystemMessage(message);
    }
  }

  private void renderDayNightOverlay() {
    com.badlogic.gdx.graphics.Color overlay = dayNightCycle.getOverlayColor();
    if (overlay.a <= 0f) {
      return;
    }
    debugShapeRenderer.setProjectionMatrix(hudCamera.combined);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    debugShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    debugShapeRenderer.setColor(overlay);
    debugShapeRenderer.rect(0, 0, hudCamera.viewportWidth, hudCamera.viewportHeight);
    debugShapeRenderer.end();
  }

  private void renderTameProgress() {
    if ((harvestChannel == null
            && tameChannel == null
            && offensiveSpellProgressDuration <= 0f
            && !hasQueuedOffensiveCast())
        || tameProgressBar == null
        || tameProgressFrame == null) return;
    updateHudCamera();
    float x = (hudCamera.viewportWidth - tameProgressFrame.getWidth()) * .5f;
    float y = hudCamera.viewportHeight * .72f;
    tameProgressBar.setPosition(x + 23f, y + 7f);
    tameProgressFrame.setPosition(x, y);
    batch.setProjectionMatrix(hudCamera.combined);
    batch.begin();
    tameProgressFrame.render(batch);
    tameProgressBar.render(batch);
    batch.end();
  }

  private void renderBrightnessOverlay() {
    float brightness = com.perso.T4C.config.GamePreferencesStore.get().getBrightness();
    if (Math.abs(brightness - 1f) < 0.001f) return;
    float alpha = brightness < 1f ? 1f - brightness : Math.min(0.25f, brightness - 1f);
    debugShapeRenderer.setProjectionMatrix(hudCamera.combined);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    debugShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    if (brightness < 1f) {
      debugShapeRenderer.setColor(0f, 0f, 0f, alpha);
    } else {
      debugShapeRenderer.setColor(1f, 1f, 1f, alpha);
    }
    debugShapeRenderer.rect(0, 0, hudCamera.viewportWidth, hudCamera.viewportHeight);
    debugShapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private void renderCollisionDebugOverlay() {
    if (!collisionDebugVisible || !CollisionManager.getInstance().isInitialized()) {
      return;
    }
    debugShapeRenderer.setProjectionMatrix(camera.combined);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    CollisionOverlayRenderer.render(
        debugShapeRenderer,
        renderStartX,
        renderEndX,
        renderStartY,
        renderEndY,
        (x, y) -> CollisionManager.getInstance().hasStaticCollisionAtGrid(x, y),
        (x, y) -> new float[] {1f, 0.5f, 0f, 0.35f});
    debugShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    debugShapeRenderer.setColor(0f, 1f, 0f, 0.45f);
    var doorTiles =
        mapRenderer
            .getObjectRenderer()
            .getClosedDoorBlockTiles(
                mapRenderer.getObjectPositions(), mapRenderer.getObjectMappings());
    if (doorTiles != null && !doorTiles.isEmpty()) {
      for (GridPoint2 tile : doorTiles) {
        if (tile.x < renderStartX
            || tile.x > renderEndX
            || tile.y < renderStartY
            || tile.y > renderEndY) {
          continue;
        }
        debugShapeRenderer.rect(tile.x * GRID_W, tile.y * GRID_H, GRID_W, GRID_H);
      }
    }
    debugShapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private void renderEntityPathDebugOverlay() {
    if (!DEBUG_ENTITY_PATHS) {
      return;
    }
    debugShapeRenderer.setProjectionMatrix(camera.combined);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    if (monsterManager != null) {
      debugShapeRenderer.setColor(1f, 0.1f, 0.1f, 0.9f);
      for (BaseMonster monster : monsterManager.getMonsters()) {
        renderDebugPath(
            monster.getPosition(), monster.getDebugPath(), monster.getDebugPathTarget());
      }
    }
    if (npcManager != null) {
      debugShapeRenderer.setColor(0.1f, 0.7f, 1f, 0.9f);
      for (BaseNPC npc : npcManager.getNPCs()) {
        renderDebugPath(npc.getPosition(), npc.getDebugPath(), npc.getDebugPathTarget());
      }
    }
    debugShapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private void renderDebugPath(Vector2 start, List<Vector2> path, Vector2 target) {
    if (start == null || ((path == null || path.isEmpty()) && target == null)) {
      return;
    }
    float fromX = start.x;
    float fromY = start.y;
    if (path != null) {
      for (Vector2 point : path) {
        debugShapeRenderer.line(fromX, fromY, point.x, point.y);
        debugShapeRenderer.circle(point.x, point.y, 3f);
        fromX = point.x;
        fromY = point.y;
      }
    }
    if (target != null) {
      debugShapeRenderer.line(fromX, fromY, target.x, target.y);
      debugShapeRenderer.circle(target.x, target.y, 5f);
    }
  }

  private void updateAttackCursor() {
    int screenX = Gdx.input.getX();
    int screenY = Gdx.input.getY();
    if (GuiManager.isPointerOver(screenX, screenY)) {
      applyDefaultCursor();
      return;
    }
    if (npcManager != null) {
      Vector3 pointer = getWorldCoords();
      npcManager.onMouseMove(pointer.x, pointer.y);
    }
    if (hud != null && hud.isHudHit(screenX, screenY)) {
      applyDefaultCursor();
      return;
    }
    if (monsterManager == null) {
      applyDefaultCursor();
      return;
    }
    BaseMonster hovered = findHoveredMonster();
    if (hovered != null) {
      if (hovered.canBeAttackedByPlayer()) {
        if (isHostileUnitSpell(selectedTargetedSpell)) {
          applySpellCursor();
        } else if (PlayerAppearanceDefaults.hasBowEquipped(player)) {
          applyBowCursor();
        } else {
          applyAttackCursor();
        }
      } else {
        applyDefaultCursor();
      }
      return;
    }
    if (isNpcHovered()) {
      if (player != null && player.isCombatMode()) {
        if (isHostileUnitSpell(selectedTargetedSpell)) {
          applySpellCursor();
        } else if (PlayerAppearanceDefaults.hasBowEquipped(player)) {
          applyBowCursor();
        } else {
          applyAttackCursor();
        }
      } else {
        applyTalkCursor();
      }
      return;
    }
    if (selectedTargetedSpell != null) {
      applySpellCursor();
      return;
    }
    applyDefaultCursor();
  }

  private BaseMonster findHoveredMonster() {
    for (BaseMonster monster : monsterManager.getMonsters()) {
      if (monster.isHovered() && !monster.isDead()) {
        return monster;
      }
    }
    return null;
  }

  private void applyAttackCursor() {
    if (attackCursorApplied) return;
    if (game != null && game.cursorManager != null) {
      game.cursorManager.applyAttackCursor(game);
      markAttackCursorApplied();
    }
  }

  private void applyDefaultCursor() {
    if (!attackCursorApplied && !talkCursorApplied && !spellCursorApplied && !bowCursorApplied)
      return;
    if (game != null && game.cursorManager != null) {
      game.cursorManager.applyDefaultCursor(game);
    }
    clearCursorState();
  }

  private void applyTalkCursor() {
    if (talkCursorApplied) {
      animateTalkCursor();
      return;
    }
    if (animateTalkCursor()) {
      markTalkCursorApplied();
    }
  }

  private void applySpellCursor() {
    if (spellCursorApplied) {
      animateSpellCursor();
      return;
    }
    if (animateSpellCursor()) {
      markSpellCursorApplied();
    }
  }

  private boolean animateTalkCursor() {
    if (game == null || game.cursorManager == null) {
      return false;
    }
    game.cursorManager.applyTalkCursor(game, Gdx.graphics.getDeltaTime());
    return true;
  }

  private boolean animateSpellCursor() {
    if (game == null || game.cursorManager == null) {
      return false;
    }
    game.cursorManager.applySpellCursor(game, Gdx.graphics.getDeltaTime());
    return true;
  }

  private void applyBowCursor() {
    if (bowCursorApplied) {
      animateBowCursor();
      return;
    }
    if (animateBowCursor()) {
      markBowCursorApplied();
    }
  }

  private boolean animateBowCursor() {
    if (game == null || game.cursorManager == null) {
      return false;
    }
    game.cursorManager.applyBowCursor(game, Gdx.graphics.getDeltaTime());
    return true;
  }

  private void markAttackCursorApplied() {
    attackCursorApplied = true;
    talkCursorApplied = false;
    spellCursorApplied = false;
    bowCursorApplied = false;
  }

  private void markTalkCursorApplied() {
    attackCursorApplied = false;
    talkCursorApplied = true;
    spellCursorApplied = false;
    bowCursorApplied = false;
  }

  private void markSpellCursorApplied() {
    attackCursorApplied = false;
    talkCursorApplied = false;
    spellCursorApplied = true;
    bowCursorApplied = false;
  }

  private void markBowCursorApplied() {
    attackCursorApplied = false;
    talkCursorApplied = false;
    spellCursorApplied = false;
    bowCursorApplied = true;
  }

  private void clearCursorState() {
    attackCursorApplied = false;
    talkCursorApplied = false;
    spellCursorApplied = false;
    bowCursorApplied = false;
  }

  private boolean isNpcHovered() {
    if (npcManager == null) {
      return false;
    }
    for (BaseNPC npc : npcManager.getNPCs()) {
      if (npc.isHovered()) {
        return true;
      }
    }
    return false;
  }

  private ProjectileDirection computeProjectileDirection(Vector2 from, Vector2 to) {
    if (from == null || to == null) {
      return new ProjectileDirection("000", false);
    }
    float dx = to.x - from.x;
    float dy = to.y - from.y;
    if (dx == 0f && dy == 0f) {
      return new ProjectileDirection("000", false);
    }
    double angleDeg = Math.toDegrees(Math.atan2(dy, dx));
    double mapped = (angleDeg - 90.0 + 360.0) % 360.0;
    int dir = (int) Math.round(mapped / 45.0) % 8;
    int angleValue = dir * 45;
    String angle = String.format("%03d", angleValue);
    return new ProjectileDirection(angle, false);
  }

  private ProjectileDirection computeFlipFallbackDirection(Vector2 from, Vector2 to) {
    if (from == null || to == null) {
      return new ProjectileDirection("000", false);
    }
    float dx = to.x - from.x;
    float dy = to.y - from.y;
    float worldDy = -dy;
    if (dx == 0f && worldDy > 0f) return new ProjectileDirection("180", false);
    if (dx == 0f && worldDy < 0f) return new ProjectileDirection("000", false);
    if (dx < 0f && worldDy == 0f) return new ProjectileDirection("090", false);
    if (dx > 0f && worldDy == 0f) return new ProjectileDirection("090", true);
    if (dx < 0f && worldDy > 0f) return new ProjectileDirection("135", false);
    if (dx > 0f && worldDy > 0f) return new ProjectileDirection("135", true);
    if (dx < 0f && worldDy < 0f) return new ProjectileDirection("045", false);
    if (dx > 0f && worldDy < 0f) return new ProjectileDirection("045", true);
    return new ProjectileDirection("000", false);
  }

  private static final class ProjectileDirection {
    private final String angle;
    private final boolean flipX;

    private ProjectileDirection(String angle, boolean flipX) {
      this.angle = angle;
      this.flipX = flipX;
    }
  }

  private void applyBuffIfNeeded(SpellData spell) {
    if (spell == null || spell.isAttack() || spell.getBuff() == null || player == null) {
      return;
    }
    SpellData.SpellBuff buff = spell.getBuff();
    List<SpellData.SpellEffect> effects = buff.getEffects() != null ? buff.getEffects() : List.of();
    player.applyBuff(
        spell.getName(),
        spell.getDescription(),
        spell.getIconId(),
        buff.getDurationSeconds(),
        Boolean.TRUE.equals(buff.getUnlimited()),
        effects);
  }

  private List<ObjectRenderer.RenderItem> buildEntityRenderItems() {
    entityItems.clear();
    herbManager.addRenderItems(entityItems, this::obtainRenderItem, batchDecor, outlineShader);
    groundItemManager.addRenderItems(
        entityItems, this::obtainRenderItem, batchDecor, outlineShader);
    addNpcRenderItems(entityItems);
    addMonsterRenderItems(entityItems);
    addPlayerRenderItem(entityItems);
    return entityItems;
  }

  private void addNpcRenderItems(List<ObjectRenderer.RenderItem> entityItems) {
    if (npcManager == null) {
      return;
    }
    for (BaseNPC npc : npcManager.getNPCs()) {
      npc.getRenderBounds(npcRenderBoundsTemp);
      ObjectRenderer.RenderItem item =
          addEntityRenderItem(
              entityItems,
              npc.hasObjectAppearance()
                  ? npc.getTileY() + 0.5f
                  : playerRenderDepth(npc.getTileY() * GRID_H),
              () -> npc.render(batchDecor, outlineShader));
      item.revealThroughDecor = true;
      item.opaqueOcclusionReveal = npc.hasObjectAppearance();
      item.occlusionRevealAction = () -> npc.renderOcclusionReveal(batchDecor);
      item.revealX = npcRenderBoundsTemp.x;
      item.revealY = npcRenderBoundsTemp.y;
      item.revealW = npcRenderBoundsTemp.width;
      item.revealH = npcRenderBoundsTemp.height;
    }
  }

  private void addMonsterRenderItems(List<ObjectRenderer.RenderItem> entityItems) {
    if (monsterManager == null) {
      return;
    }
    for (BaseMonster monster : monsterManager.getMonsters()) {
      addEntityRenderItem(
          entityItems, monster.getTileY(), () -> monster.render(batchDecor, outlineShader));
    }
  }

  private void addPlayerRenderItem(List<ObjectRenderer.RenderItem> entityItems) {
    if (player == null || !seraphArrivalShowsPlayer()) {
      return;
    }
    Vector2 pos = player.getPositionVector();
    float depthTileY = playerRenderDepth(pos.y);
    player.getAnimations().getRenderBounds(pos, player.getMovement(), playerRenderBoundsTemp);
    Vector3 worldCoords = getWorldCoords();
    boolean hovered = playerRenderBoundsTemp.contains(worldCoords.x, worldCoords.y);
    ObjectRenderer.RenderItem item =
        addEntityRenderItem(
            entityItems, depthTileY, () -> player.render(batchDecor, outlineShader, hovered));
    item.revealThroughDecor = true;
    item.occlusionRevealAction = () -> player.renderOcclusionReveal(batchDecor);
    item.revealX = playerRenderBoundsTemp.x;
    item.revealY = playerRenderBoundsTemp.y;
    item.revealW = playerRenderBoundsTemp.width;
    item.revealH = playerRenderBoundsTemp.height;
  }

  static float playerRenderDepth(float worldY) {
    return (float) Math.floor(worldY / GRID_H) + 2f;
  }

  private void applyPeriodicSpellImpact(SpellData spell, Player caster, BaseMonster target) {
    if (caster != player) return;
    SpellEffectManager.TargetExhaustion exhaustion =
        spellEffectManager.resolveTargetExhaustion(spell, caster, target);
    target.exhaustMovementFor(exhaustion.moveMillis());
    int durationSeconds =
        spellEffectManager.resolveDurationSeconds(
            spell,
            new com.perso.T4C.helper.DiceFormula.Context(
                target.getCombatStrength(),
                target.getCombatEndurance(),
                target.getCombatAgility(),
                target.getCombatIntelligence(),
                0,
                0,
                0,
                target.getCombatLevel()));
    target.applyTemporaryDodgeModifier(
        spellEffectManager.resolveTargetDodgeModifier(spell, caster, target),
        durationSeconds * 1_000L);
    applyResolvedSpellImpact(spell, target, 0d, true);
  }

  private int countSneakWitnesses() {
    if (monsterManager == null) return 0;
    Vector2 playerPosition = player.getPositionVector();
    float range = com.perso.T4C.combat.StealthRules.WITNESS_RANGE * Math.max(GRID_W, GRID_H);
    int witnesses = 0;
    for (BaseMonster monster : monsterManager.getMonsters()) {
      if (monster == null || monster.isDead()) continue;
      if (monster.getPosition().dst(playerPosition) <= range) witnesses++;
    }
    return witnesses;
  }

  private void configurePlayerDeathCallback() {
    player.setWitnessCountSupplier(this::countSneakWitnesses);
    player.setDeathCallback(
        pvpDeath -> {
          Vector2 deathPosition = player.getPositionVector().cpy();
          DeathPenaltyService.Result penalties =
              deathPenaltyService.apply(player, pvpDeath, xpCurve, ThreadLocalRandom.current());
          groundItemManager.spawnCorpse(
              penalties.droppedItems(),
              penalties.droppedItemCharges(),
              penalties.goldDropped(),
              deathPosition.x,
              deathPosition.y);
          clearCurrentAttackTarget();
          if (npcManager != null) {
            npcManager.resetAllHostility();
          }
          float respawnX = player.resolveRespawnWorldX();
          float respawnY = player.resolveRespawnWorldY();
          int respawnZ = player.resolveRespawnWorldZ();
          player.setWorldPosition(respawnX, respawnY, respawnZ);
          player.respawnAfterDeath();
          camera.position.set(respawnX, respawnY, 0f);
          camera.update();
          showSystemMessage(
              I18n.message(
                  "message.player_died", penalties.xpLost(), penalties.droppedItems().size()));
          savePlayerState();
        });
  }

  private ObjectRenderer.RenderItem addEntityRenderItem(
      List<ObjectRenderer.RenderItem> entityItems, float depthY, Runnable renderAction) {
    ObjectRenderer.RenderItem item = obtainRenderItem();
    item.isObject = false;
    item.y = depthY;
    item.renderAction = renderAction;
    entityItems.add(item);
    return item;
  }

  private Vector3 getWorldCoords() {
    float mouseX = Gdx.input.getX();
    float mouseY = Gdx.input.getY();
    if (mouseX != lastMouseX
        || mouseY != lastMouseY
        || camera.position.x != lastCameraX
        || camera.position.y != lastCameraY) {
      worldCoordsTemp.set(mouseX, mouseY, 0);
      camera.unproject(worldCoordsTemp);
      lastMouseX = (int) mouseX;
      lastMouseY = (int) mouseY;
      lastCameraX = camera.position.x;
      lastCameraY = camera.position.y;
    }
    return worldCoordsTemp;
  }

  private void updateCamera() {
    float px =
        snapToScreenPixel(
            player.getCoordinates().getX(), camera.viewportWidth, viewport.getScreenWidth());
    float py =
        snapToScreenPixel(
            player.getCoordinates().getY(), camera.viewportHeight, viewport.getScreenHeight());
    if (camera.position.x != px || camera.position.y != py) {
      camera.position.set(px, py, 0);
      camera.update();
    }
    viewport.apply();
  }

  private float snapToScreenPixel(float worldValue, float worldSize, int screenSize) {
    if (screenSize <= 0) {
      return Math.round(worldValue);
    }
    float worldUnitsPerScreenPixel = worldSize / screenSize;
    if (worldUnitsPerScreenPixel <= 0f) {
      return Math.round(worldValue);
    }
    return Math.round(worldValue / worldUnitsPerScreenPixel) * worldUnitsPerScreenPixel;
  }

  private void renderNameOverlay(SpriteBatch batch) {
    if (monsterManager != null) {
      monsterManager.renderNameOverlay(batch);
    }
    if (npcManager != null) {
      npcManager.renderNameOverlay(batch);
    }
    if (mapRenderer != null && mapRenderer.getObjectRenderer() != null) {
      mapRenderer
          .getObjectRenderer()
          .renderNameOverlay(
              batch, mapRenderer.getObjectPositions(), mapRenderer.getObjectMappings());
    }
    renderPlayerTalkText(batch);
  }

  private void renderPlayerTalkText(SpriteBatch batch) {
    if (player == null) return;
    String text = player.getTalkText();
    if (text == null || text.isEmpty()) return;
    float talkCenterX = player.getPositionVector().x + (GRID_W * 0.5f);
    float talkBottomY = player.getPositionVector().y - 67f;
    NameRenderer.renderNameWithKeyword(batch, text, null, talkCenterX, talkBottomY, 0f, 0f);
  }

  private void updateEntities(float delta) {
    NativeTimingProfile timing = NativeTimingProfile.current();
    simulationAccumulator += Math.max(0f, Math.min(delta, 0.25f));
    float simulationStep = 1f / timing.fps();
    if (simulationAccumulator < simulationStep) {
      return;
    }
    simulationAccumulator -= simulationStep;
    delta = simulationStep;
    floatingDamage.update(delta);
    if (player != null) {
      if (SeraphAuraService.synchronize(player)) {
        savePlayerState();
      }
      player.update(delta);
      playerPositionTemp.set(player.getPositionVector());
      int margin = 5;
      if (npcManager != null) {
        npcManager.updateVisible(
            delta, playerPositionTemp, renderStartX, renderEndX, renderStartY, renderEndY, margin);
      }
      if (monsterManager != null) {
        monsterManager.setScriptPlayer(player);
        monsterManager.updateVisible(
            delta, playerPositionTemp, renderStartX, renderEndX, renderStartY, renderEndY, margin);
      }
      pruneSelectedMonster();
      performAttackTick();
      spellEffectManager.update(this::applyPeriodicSpellImpact);
      updateTameChannel(delta);
      updateHarvest(delta);
      updateOffensiveSpellProgress(delta);
    }
  }

  private ObjectRenderer.RenderItem obtainRenderItem() {
    ObjectRenderer.RenderItem item = renderItemPool.pollFirst();
    if (item == null) {
      item = new ObjectRenderer.RenderItem();
    }
    item.pooled = false;
    item.isObject = false;
    item.objectInfo = null;
    item.renderAction = null;
    item.decorRegion = null;
    item.decorX = 0f;
    item.decorY = 0f;
    item.decorW = 0f;
    item.decorH = 0f;
    item.decorMirror = false;
    item.occludesEntities = false;
    item.revealThroughDecor = false;
    item.occlusionRevealAction = null;
    item.revealX = 0f;
    item.revealY = 0f;
    item.revealW = 0f;
    item.revealH = 0f;
    item.revealAfterDecor = null;
    item.opaqueOcclusionReveal = false;
    item.y = 0f;
    return item;
  }

  private void releaseEntityRenderItems(List<ObjectRenderer.RenderItem> items) {
    for (ObjectRenderer.RenderItem item : items) {
      item.objectInfo = null;
      item.renderAction = null;
      item.decorRegion = null;
      item.decorX = 0f;
      item.decorY = 0f;
      item.decorW = 0f;
      item.decorH = 0f;
      item.decorMirror = false;
      item.occludesEntities = false;
      item.revealThroughDecor = false;
      item.occlusionRevealAction = null;
      item.revealX = 0f;
      item.revealY = 0f;
      item.revealW = 0f;
      item.revealH = 0f;
      item.revealAfterDecor = null;
      item.opaqueOcclusionReveal = false;
      renderItemPool.addLast(item);
    }
  }

  @Override
  public void resize(int w, int h) {
    viewport.update(w, h, true);
    updateHudCamera(w, h);
    stage.getViewport().update(w, h, true);
  }

  private void updateHudCamera() {
    updateHudCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  private void updateHudCamera(int width, int height) {
    if (width <= 0 || height <= 0) {
      return;
    }
    if (hudCamera.viewportWidth == width && hudCamera.viewportHeight == height) {
      return;
    }
    hudCamera.setToOrtho(true, width, height);
    hudCamera.update();
  }

  @Override
  public void show() {
    if (!displayInitialized) initializeDisplay();
    lastAmbientMusicTileX = Integer.MIN_VALUE;
    lastAmbientMusicTileY = Integer.MIN_VALUE;
    lastMapZoneWorld = Integer.MIN_VALUE;
    lastMapZoneId = Integer.MIN_VALUE;
    updateAmbientMusicForPlayer();
    updateMapZoneDisplay();
    startSeraphArrivalIfNeeded();
  }

  private void startSeraphArrivalIfNeeded() {
    if (seraphArrivalPlaying || player == null) {
      return;
    }
    if (!GamePreferencesStore.get().isSeraphAnimation()) {
      return;
    }
    seraphArrivalKind = SeraphArrivalAnimation.kindOf(player);
    if (seraphArrivalKind == null) {
      return;
    }
    Vector2 position = player.getPositionVector();
    spellRenderer.triggerImpactSpell(
        seraphArrivalKind.spritePrefix(),
        position.x,
        position.y,
        seraphArrivalKind.sound());
    seraphArrivalPlaying = spellRenderer.hasActiveImpact(seraphArrivalKind.spritePrefix());
  }

  private boolean seraphArrivalActive() {
    return seraphArrivalPlaying
        && seraphArrivalKind != null
        && spellRenderer.hasActiveImpact(seraphArrivalKind.spritePrefix());
  }

  private int seraphArrivalFrame() {
    if (!seraphArrivalActive()) {
      return Integer.MAX_VALUE;
    }
    return Math.max(0, spellRenderer.currentImpactFrameIndex(seraphArrivalKind.spritePrefix()));
  }

  private boolean seraphArrivalShowsPlayer() {
    return SeraphArrivalAnimation.playerVisible(
        seraphArrivalKind, seraphArrivalFrame(), seraphArrivalActive());
  }

  private boolean seraphArrivalBlocksMovement() {
    return !SeraphArrivalAnimation.movementAllowed(
        seraphArrivalKind, seraphArrivalFrame(), seraphArrivalActive());
  }

  private void initializeHud() {
    hud = new PlayerHUD(player, spriteLoader);
    hud.setBackpackAction(
        () -> {
          if (GuiManager.isCurrent(Inventory.class)) {
            GuiManager.close();
          } else {
            GuiManager.open(new Inventory(player, hud));
          }
        });
    hud.setCharacterAction(
        () -> {
          if (GuiManager.isCurrent(Statistics.class)) {
            GuiManager.close();
          } else {
            GuiManager.open(new Statistics(player));
          }
        });
    hud.setSpellBookAction(
        () -> {
          if (GuiManager.isCurrent(SpellBook.class)) {
            GuiManager.close();
          } else {
            GuiManager.open(new SpellBook(player, hud));
          }
        });
    hud.setQuestAction(
        () -> {
          if (GuiManager.isCurrent(QuestScreen.class)) {
            GuiManager.close();
          } else {
            GuiManager.open(new QuestScreen(player));
          }
        });
    hud.setMapAction(this::toggleMapScreen);
    hud.setOptionsAction(
        () -> {
          if (GuiManager.isCurrent(OptionsScreen.class)) {
            GuiManager.close();
          } else {
            GuiManager.open(new OptionsScreen(this::switchCharacter));
          }
        });
    coordsHud = new PlayerCoordsHud(player);
    systemMessage = new SystemMessage();
    GmCommandProcessor gmCommands = new GmCommandProcessor(xpCurve, npcManager, monsterManager);
    gameChat =
        new GameChat(
            text -> {
              if (gmCommands.handleChatMessage(text, player)) {
                return true;
              }
              if (player != null) {
                player.showTalkText(text);
                if (npcManager != null && npcManager.hasActiveConversation()) {
                  npcManager.talkToActiveNpc(text, player);
                }
              }
              return false;
            });
    gameChat.setAutocompleteProvider(
        type ->
            "npc".equalsIgnoreCase(type)
                ? NpcFactoryRegistry.registrations().stream()
                    .map(NpcFactoryRegistry.Registration::id)
                    .toList()
                : MonsterRegistry.names());
    gameChat.addSystemMessage(I18n.key("chat.help"));
    SystemMessage.setShared(systemMessage);
    SystemMessage.setChatSink(gameChat::addSystemMessage);
    com.perso.T4C.spell.NpcCastVfxHook.setShared(this::playNpcCastVfx, this::playNpcSelfVfx);
    com.perso.T4C.spell.CompanionCastVfxHook.setShared(
        this::playCompanionAttackVfx, this::playCompanionHealVfx, this::playCompanionVanishVfx);
    lastMapZoneWorld = Integer.MIN_VALUE;
    lastMapZoneId = Integer.MIN_VALUE;
    updateMapZoneDisplay();
  }

  private void playNpcCastVfx(SpellData spell, Player castOn, Vector2 casterPosition) {
    playNpcCastVfx(spell, castOn, casterPosition, null);
  }

  private void playNpcSelfVfx(SpellData spell, Vector2 casterPosition) {
    if (spell == null || casterPosition == null) return;
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    spellRenderer.playLaunchSound(visuals.launchSound());
    String impact = visuals.impact();
    if (impact != null && !impact.isEmpty()) {
      spellRenderer.playImpactSound(visuals.impactSound());
      spellRenderer.triggerImpactSpell(impact, casterPosition.x, casterPosition.y);
    }
  }

  private void playNpcCastVfx(
      SpellData spell, Player castOn, Vector2 casterPosition, Runnable onLanded) {
    if (spell == null || castOn == null) {
      if (onLanded != null) {
        onLanded.run();
      }
      return;
    }
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    spellRenderer.playLaunchSound(visuals.launchSound());
    String impact = visuals.impact();
    if (impact == null || impact.isEmpty()) {
      if (onLanded != null) {
        onLanded.run();
      }
      return;
    }
    String projectileSpell = visuals.projectile();
    if (projectileSpell == null || projectileSpell.isEmpty() || casterPosition == null) {
      spellRenderer.playImpactSound(visuals.impactSound());
      spellRenderer.triggerImpactSpell(impact, castOn);
      if (onLanded != null) {
        onLanded.run();
      }
      return;
    }
    Vector2 playerPos = castOn.getPositionVector();
    final String impactEffect = impact;
    Runnable onImpact =
        () -> {
          spellRenderer.playImpactSound(visuals.impactSound());
          spellRenderer.triggerImpactSpell(impactEffect, castOn);
          if (onLanded != null) {
            onLanded.run();
          }
        };
    boolean launched = false;
    if (spell.isLineOfSight()) {
      ProjectileDirection direction = computeProjectileDirection(casterPosition, playerPos);
      launched =
          spellRenderer.launchProjectile(
              projectileSpell + direction.angle,
              castOn,
              casterPosition.x,
              casterPosition.y,
              direction.flipX,
              onImpact);
      if (!launched) {
        ProjectileDirection fallback = computeFlipFallbackDirection(casterPosition, playerPos);
        launched =
            spellRenderer.launchProjectile(
                projectileSpell + fallback.angle,
                castOn,
                casterPosition.x,
                casterPosition.y,
                fallback.flipX,
                onImpact);
      }
    }
    if (!launched) {
      launched =
          spellRenderer.launchProjectile(
              projectileSpell, castOn, casterPosition.x, casterPosition.y, false, onImpact);
    }
    if (!launched) {
      onImpact.run();
    }
  }

  private void playCompanionAttackVfx(
      SpellData spell, BaseMonster target, Vector2 casterPosition, Runnable onImpact) {
    if (spell == null || target == null || onImpact == null) {
      return;
    }
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    spellRenderer.playLaunchSound(visuals.launchSound());
    String impact = visuals.impact();
    final String impactEffect = impact;
    Runnable impactRunnable =
        () -> {
          onImpact.run();
          spellRenderer.playImpactSound(visuals.impactSound());
          if (impactEffect != null && !impactEffect.isEmpty()) {
            Vector2 hitPosition = target.getPosition();
            spellRenderer.triggerImpactSpell(impactEffect, hitPosition.x, hitPosition.y);
          }
        };
    String projectileSpell = visuals.projectile();
    if (projectileSpell == null || projectileSpell.isEmpty() || casterPosition == null) {
      impactRunnable.run();
      return;
    }
    Vector2 targetPosition = target.getPosition();
    boolean launched = false;
    if (spell.isLineOfSight()) {
      ProjectileDirection direction = computeProjectileDirection(casterPosition, targetPosition);
      launched =
          spellRenderer.launchProjectile(
              projectileSpell + direction.angle,
              target,
              casterPosition.x,
              casterPosition.y,
              direction.flipX,
              impactRunnable);
      if (!launched) {
        ProjectileDirection fallback = computeFlipFallbackDirection(casterPosition, targetPosition);
        launched =
            spellRenderer.launchProjectile(
                projectileSpell + fallback.angle,
                target,
                casterPosition.x,
                casterPosition.y,
                fallback.flipX,
                impactRunnable);
      }
    }
    if (!launched) {
      launched =
          spellRenderer.launchProjectile(
              projectileSpell, target, casterPosition.x, casterPosition.y, false, impactRunnable);
    }
    if (!launched) {
      impactRunnable.run();
    }
  }

  private void playCompanionVanishVfx(float worldX, float worldY) {
    SpellData light = SpellRegistry.findByName(COMPANION_VANISH_SPELL);
    if (light == null) {
      return;
    }
    String effect = light.getImpactSpell();
    if (effect == null || effect.isEmpty()) {
      effect = light.getProjectileSpell();
    }
    if (effect != null && !effect.isEmpty()) {
      spellRenderer.triggerImpactSpell(effect, worldX, worldY, light.getSoundImpact());
    }
  }

  private void playCompanionHealVfx(
      SpellData spell,
      Player castOn,
      Vector2 casterPosition,
      float worldX,
      float worldY,
      Runnable onImpact) {
    if (spell == null) {
      if (onImpact != null) {
        onImpact.run();
      }
      return;
    }
    if (castOn != null) {
      playNpcCastVfx(spell, castOn, casterPosition, onImpact);
      return;
    }
    SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
    spellRenderer.playLaunchSound(visuals.launchSound());
    String impact = visuals.impact();
    if (impact != null && !impact.isEmpty()) {
      spellRenderer.triggerImpactSpell(impact, worldX, worldY, visuals.impactSound());
    }
  }

  private void initializeInputHandlers() {
    ObjectClickHandler objectClickHandler =
        new ObjectClickHandler(
            mapRenderer.getObjectRenderer(),
            mapRenderer.getObjectPositions(),
            mapRenderer,
            camera,
            player,
            systemMessage,
            groundItemManager);
    NPCInputHandler npcInputHandler =
        new NPCInputHandler(npcManager, camera, player, this::tryCastAttackSpell, systemMessage);
    MonsterInputHandler monsterInputHandler =
        new MonsterInputHandler(
            camera,
            monsterManager,
            player,
            this::tryCastTargetedSpell,
            this::tryBowAttack,
            systemMessage);
    monsterInputHandler.setOnAttackTargetSelected(this::beginAttackTarget);
    monsterInputHandler.setOnClickedElsewhere(
        () -> {
          clearCurrentAttackTarget();
          cancelActiveTargetedSpell();
        });
    GroundItemClickHandler groundItemClickHandler =
        new GroundItemClickHandler(camera, groundItemManager, player, systemMessage);
    HerbInputHandler herbInputHandler =
        new HerbInputHandler(camera, herbManager, this::tryStartHarvest);
    ClickToMoveHandler clickToMoveHandler =
        new ClickToMoveHandler(
            camera, reader, player, hud, this::handleQuickbarSpell, this::handleQuickbarItem);
    InputMultiplexer multiplexer = new InputMultiplexer();
    InputAdapter guiAdapter =
        new InputAdapter() {
          @Override
          public boolean keyDown(int keycode) {
            if (GuiManager.isOpen() && GuiManager.onKeyDown(keycode)) {
              return true;
            }
            if (keycode == Input.Keys.TAB && !GuiManager.isOpen() && !isTextInputActive()) {
              targetNearestMonster();
              return true;
            }
            if (keycode == Input.Keys.M
                && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
                && !GuiManager.isOpen()
                && !isTextInputActive()) {
              GuiManager.open(new com.perso.T4C.gui.screen.MacrosScreen(player, hud));
              return true;
            }
            if (!GuiManager.isOpen() && !isTextInputActive() && tryFireMacro(keycode)) {
              return true;
            }
            if (keycode == Input.Keys.ESCAPE
                && (currentAttackTarget != null
                    || selectedMonster != null
                    || selectedTargetedSpell != null)) {
              clearCurrentAttackTarget();
              clearSelectedMonster();
              cancelActiveTargetedSpell();
              return true;
            }
            if (keycode == Input.Keys.ESCAPE && !isTextInputActive()) {
              GuiManager.open(new OptionsScreen(MainGameScreen.this::switchCharacter));
              return true;
            }
            if (keycode == Input.Keys.F9) {
              toggleProfilerEnabled();
              return true;
            }
            if (keycode == Input.Keys.F8) {
              herbManager.forceRefresh();
              updateHerbViewport();
              showSystemMessage(
                  herbManager.debugSummary(
                      renderStartX, renderEndX, renderStartY, renderEndY, reader));
              return true;
            }
            if (keycode == Input.Keys.F11) {
              boolean fullscreen = DisplayModeToggle.toggle();
              showSystemMessage(DisplayModeToggle.message(fullscreen));
              return true;
            }
            return false;
          }

          @Override
          public boolean keyTyped(char character) {
            if (GuiManager.isOpen() && GuiManager.onKeyTyped(character)) {
              return true;
            }
            return false;
          }

          @Override
          public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            boolean controlDown =
                Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
            if (button == Input.Buttons.LEFT
                && hud != null
                && hud.chatBarButtonsTouchDown(screenX, screenY, controlDown)) {
              return true;
            }
            if (button == Input.Buttons.LEFT
                && gameChat != null
                && gameChat.touchDown(screenX, screenY)) return true;
            if (button == Input.Buttons.LEFT
                && gameChat != null
                && gameChat.boxedTouchDown(screenX, screenY)) return true;
            if (button == Input.Buttons.LEFT
                && hud != null
                && hud.boxedTouchDown(screenX, screenY, controlDown)) {
              return true;
            }
            if (button == Input.Buttons.RIGHT
                && hud != null
                && hud.showBuffTooltipAt(screenX, screenY)) {
              return true;
            }
            if (button == Input.Buttons.LEFT && handleBuffBarLeftClick(screenX, screenY)) {
              return true;
            }
            if (button == Input.Buttons.RIGHT
                && hud != null
                && hud.showQuickSlotTooltipAt(screenX, screenY)) {
              return true;
            }
            if (button == Input.Buttons.RIGHT && cancelActiveTargetedSpell()) {
              return true;
            }
            if (button == Input.Buttons.RIGHT && currentAttackTarget != null) {
              clearCurrentAttackTarget();
              return true;
            }
            if (GuiManager.isOpen()) {
              GuiManager.onTouchDown(screenX, screenY, button);
              return true;
            }
            if (button == Input.Buttons.LEFT && tryCastSelectedHealOnPlayer(screenX, screenY)) {
              return true;
            }
            return false;
          }

          @Override
          public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT
                && hud != null
                && hud.chatBarButtonsTouchUp(screenX, screenY)) {
              return true;
            }
            if (button == Input.Buttons.LEFT && hud != null && hud.boxedTouchUp()) return true;
            if (button == Input.Buttons.LEFT && gameChat != null && gameChat.touchUp()) return true;
            if (button == Input.Buttons.LEFT && gameChat != null && gameChat.boxedTouchUp())
              return true;
            if (GuiManager.isOpen()) {
              GuiManager.onTouchUp(screenX, screenY);
              return true;
            }
            return false;
          }

          @Override
          public boolean mouseMoved(int screenX, int screenY) {
            if (hud != null && hud.chatBarButtonsMouseMoved(screenX, screenY)) {
              return true;
            }
            if (hud != null && hud.boxedMouseMoved(screenX, screenY)) return true;
            if (gameChat != null && gameChat.boxedMouseMoved(screenX, screenY)) return true;
            if (GuiManager.isOpen()) {
              GuiManager.onMouseMove(screenX, screenY);
              return true;
            }
            return false;
          }

          @Override
          public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (hud != null && hud.chatBarButtonsTouchDragged(screenX, screenY)) {
              return true;
            }
            if (gameChat != null && gameChat.touchDragged(screenX, screenY)) return true;
            if (hud != null && hud.boxedMouseMoved(screenX, screenY)) {
              return true;
            }
            if (gameChat != null && gameChat.boxedMouseMoved(screenX, screenY)) return true;
            if (GuiManager.isOpen()) {
              GuiManager.onMouseMove(screenX, screenY);
              return true;
            }
            return false;
          }

          @Override
          public boolean scrolled(float amountX, float amountY) {
            if (GuiManager.isOpen()) {
              GuiManager.onScroll(amountY, Gdx.input.getX(), Gdx.input.getY());
              return true;
            }
            camera.zoom = Math.max(0.6f, Math.min(1.8f, camera.zoom + amountY * 0.1f));
            camera.update();
            return true;
          }
        };
    InputAdapter positionSpellHandler =
        new InputAdapter() {
          @Override
          public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return button == Input.Buttons.LEFT && tryCastPositionSpell(screenX, screenY);
          }
        };
    java.util.function.BooleanSupplier typing =
        () -> (gameChat != null && gameChat.isActive()) || GuiManager.capturesKeyboard();
    inputHandler.setTextInputActiveSupplier(typing);
    this.textInputActiveSupplier = typing;
    multiplexer.addProcessor(gameChat);
    multiplexer.addProcessor(guiAdapter);
    multiplexer.addProcessor(stage);
    multiplexer.addProcessor(clickToMoveHandler);
    multiplexer.addProcessor(monsterInputHandler);
    multiplexer.addProcessor(npcInputHandler);
    multiplexer.addProcessor(herbInputHandler);
    multiplexer.addProcessor(groundItemClickHandler);
    multiplexer.addProcessor(objectClickHandler);
    multiplexer.addProcessor(positionSpellHandler);
    multiplexer.addProcessor(new TileClickHandler(reader, camera));
    Gdx.input.setInputProcessor(multiplexer);
  }

  @Override
  public void hide() {}

  @Override
  public void pause() {}

  @Override
  public void resume() {
    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();
    if (width > 1 && height > 1) {
      updateHudCamera(width, height);
      if (hud != null) {
        hud.recoverAfterDisplayChange();
      }
    }
  }

  @Override
  public void dispose() {
    if (disposed) return;
    disposed = true;
    NpcScriptRuntime.setSummonCallback(null);
    if (!playerStateSaved && player != null && displayInitialized) {
      savePlayerState();
      playerStateSaved = true;
    }
    GuiManager.close();
    if (Gdx.input.getInputProcessor() != null) {
      Gdx.input.setInputProcessor(null);
    }
    spriteLoader.unregisterReloadListener(spriteReloadListener);
    spriteReloadListener = null;
    PlayerStateStore.clearCompanionSupplier();
    gameProfiler.stop();
    SoundManager.stopAmbient();
    if (mapRenderer != null) mapRenderer.dispose();
    batchSol.dispose();
    batchDecor.dispose();
    debugShapeRenderer.dispose();
    if (systemMessage != null) {
      SystemMessage.setShared(null);
      com.perso.T4C.spell.NpcCastVfxHook.setShared(null);
      com.perso.T4C.spell.CompanionCastVfxHook.setShared(null, null, null);
      systemMessage.dispose();
    }
    if (gameChat != null) {
      gameChat.dispose();
    }
    if (hud != null) {
      hud.dispose();
    }
    if (outlineShader != null) {
      outlineShader.dispose();
    }
    if (player != null) {
      player.dispose();
    }
    if (npcManager != null) {
      npcManager.dispose();
    }
    stage.dispose();
  }

  private void toggleMapScreen() {
    if (GuiManager.isCurrent(MapScreen.class)) {
      GuiManager.close();
    } else {
      GuiManager.open(new MapScreen(player));
    }
  }
}
