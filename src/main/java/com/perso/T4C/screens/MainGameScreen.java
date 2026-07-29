package com.perso.T4C.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.perso.T4C.MyGame;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.DisplayModeToggle;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.MusicZoneBinaryIO;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.helper.TeleportBinaryIO;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.helper.AppearanceDefaultsCatalog;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.input.*;
import com.perso.T4C.monster.BaseMonster;
import com.perso.T4C.monster.MonsterManager;
import com.perso.T4C.npc.BaseNPC;
import com.perso.T4C.npc.NPCManager;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.spell.SpellCastingService;
import com.perso.T4C.spell.SpellEffectManager;
import com.perso.T4C.combat.CombatProfile;
import com.perso.T4C.combat.CombatProfiles;
import com.perso.T4C.combat.CombatResolver;
import com.perso.T4C.combat.CombatResult;
import com.perso.T4C.combat.PhysicalAttackRequest;
import com.perso.T4C.combat.SeraphAuraService;
import com.perso.T4C.death.DeathPenaltyService;
import com.perso.T4C.render.ObjectRenderer;
import com.perso.T4C.render.SpellRenderer;
import com.perso.T4C.render.TeleportOverlayRenderer;
import com.perso.T4C.render.CollisionOverlayRenderer;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.Inventory;
import com.perso.T4C.gui.screen.SpellBook;
import com.perso.T4C.gui.screen.Statistics;
import com.perso.T4C.gui.screen.OptionsScreen;
import com.perso.T4C.gui.screen.QuestScreen;
import com.perso.T4C.gui.screen.MapScreen;
import com.perso.T4C.gui.widget.GuiMapZoneDisplay;
import com.perso.T4C.ui.FloatingDamage;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.GameChat;
import com.perso.T4C.ui.PlayerCoordsHud;
import com.perso.T4C.ui.PlayerHUD;
import com.perso.T4C.ui.SystemMessage;
import com.perso.T4C.entity.NameRenderer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.perso.T4C.config.GameConstants.*;

/**
 * Main gameplay screen: loads map and assets, contains player and orchestrates
 * rendering loop.
 */
public class MainGameScreen implements Screen {
    private static final Logger log = LoggerFactory.getLogger(MainGameScreen.class);
    private static final int INITIAL_WARMUP_BUFFER = 18;
    private static final int INITIAL_WARMUP_CHUNK_BUDGET = 128;
    private static final int INITIAL_WARMUP_TMPL_BUDGET = 4096; // préchauffer tous les TMPL visibles dès le départ
    private static final int INITIAL_WARMUP_DECOR_BUDGET = 512;
    private static final int FRAME_WARMUP_BUFFER = 14;
    private static final int FRAME_WARMUP_CHUNK_BUDGET = 64; // time-capped at 4ms in preloadChunks
    private static final int FRAME_WARMUP_TMPL_BUDGET = 128; // couvrir les nouveaux TMPL quand le joueur avance
    private static final int FRAME_WARMUP_DECOR_BUDGET = 24;
    // A decor is anchored to one tile but can extend several tiles beyond it.
    // Keep those anchors around the viewport so large sprites do not pop in/out.
    private static final int DECOR_RENDER_OVERHANG_TILES = 14;
    private final MyGame game;

    private final OrthographicCamera camera;
    private final OrthographicCamera hudCamera;
    private final ScreenViewport viewport;
    private final SpriteBatch batchSol;
    private final SpriteBatch batchDecor;
    private final SpriteBatch batch;
    private final SpriteLoader spriteLoader = SpriteLoader.getInstance();
    private GameInputHandler inputHandler;
    private PlayerHUD hud;
    private PlayerCoordsHud coordsHud;
    private SystemMessage systemMessage;
    private GameChat gameChat;
    private GuiMapZoneDisplay mapZoneDisplay;

    @Getter
    private Player player;
    private MapReader reader;
    private MapRenderer mapRenderer;
    private ShaderProgram outlineShader;
    private NPCManager npcManager;
    private MonsterManager monsterManager;
    private QuestService questService;
    private final com.perso.T4C.objects.GroundItemManager groundItemManager = new com.perso.T4C.objects.GroundItemManager();
    private final java.util.Random lootRandom = new java.util.Random();

    private final Stage stage;

    // Reusable objects to avoid allocations per frame
    private final Vector3 worldCoordsTemp = new Vector3();
    private final Vector3 clickWorldCoordsTemp = new Vector3();
    private final Vector2 playerPositionTemp = new Vector2();
    private final Rectangle playerRenderBoundsTemp = new Rectangle();
    private final Rectangle npcRenderBoundsTemp = new Rectangle();
    private final List<ObjectRenderer.RenderItem> entityItems = new ArrayList<>();
    private final java.util.ArrayDeque<ObjectRenderer.RenderItem> renderItemPool = new java.util.ArrayDeque<>();
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
    private final DeathPenaltyService deathPenaltyService = new DeathPenaltyService(DeathPenaltyService.Config.originalDefaults());
    private SpellData selectedTargetedSpell;
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
    /** Spell repeatedly cast on {@link #currentAttackTarget}; null means weapon auto-attack. */
    private SpellData currentAttackSpell = null;
    private long nextAutoSpellAttemptAtMs = 0L;
    private static final long AUTO_SPELL_RETRY_DELAY_MS = 100L;
    /** Monster highlighted with Tab. Purely a selection: it is never attacked on its own. */
    private BaseMonster selectedMonster = null;
    /** True while the NPC talk line is capturing keystrokes; suppresses game hotkeys such as Tab. */
    private java.util.function.BooleanSupplier textInputActiveSupplier;
    /** Maximum distance, in tiles, at which Tab will acquire a monster. */
    private static final float TAB_TARGET_RANGE_TILES = 12f;
    // ── Profiler JFR ─────────────────────────────────────────────────────────
    private final com.perso.T4C.profiler.GameProfiler gameProfiler = new com.perso.T4C.profiler.GameProfiler();
    private long profilerFrameIndex = 0;
    // ─────────────────────────────────────────────────────────────────────────

    private static final String MUSIC_ZONES_SUFFIX = ".musiczones.json";
    private static final String MUSIC_ZONES_BIN_SUFFIX = ".musiczones.bin";
    private final List<MusicZoneEntry> musicZones = new ArrayList<>();
    private final List<TeleportEntry> teleports = new ArrayList<>();
    private String currentAmbientMusic = null;
    private int lastAmbientMusicTileX = Integer.MIN_VALUE;
    private int lastAmbientMusicTileY = Integer.MIN_VALUE;
    private long teleportCooldownUntilMs = 0L;
    private long lastTeleportSourceKey = Long.MIN_VALUE;

    /**
     * Initializes the MainGameScreen with the given game instance.
     *
     * @param game The main game application instance.
     * @throws GameException If an error occurs during initialization.
     */
    public MainGameScreen(MyGame game) throws GameException {
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

        initialPlayerState = loadInitialPlayerState();
        currentMap = MapDefinition.fromZ(initialPlayerState != null ? initialPlayerState.z : 0);
        groundItemManager.setActiveWorld(currentMap.getZ());

        loadShader();
        loadXpCurve();
        loadMap(currentMap);
        loadMusicZones(currentMap);
        loadTeleports();
        loadCollisionMap(currentMap);
        CollisionManager.getInstance().setPlayerPassabilityProvider(this::isTeleportSourceTile);
        initializePlayer();
        questService = new QuestService(xpCurve, this::savePlayerState, this::showSystemMessage);
        updateAmbientMusicForPlayer();

        mapRenderer = createMapRenderer();
        inputHandler = new GameInputHandler(log, mapRenderer, player);

        npcManager = createNpcManager();
        monsterManager = createMonsterManager();

        configureCallbacks();
        registerReloadListener();
    }

    /**
     * Loads the outline shader used for highlighting entities.
     */
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

    /**
     * Loads the XP curve required for player progression.
     */
    private void loadXpCurve() {
        xpCurve = XpCurve.loadDefault();
    }

    /**
     * Loads the player's initial state from the store.
     *
     * @return The loaded PlayerStateDto, or null if loading fails.
     */
    private com.perso.T4C.helper.PlayerStateDto loadInitialPlayerState() {
        try {
            return PlayerStateStore.load();
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
            // fall through to default
        }
        return new com.perso.T4C.world.DayNightCycle();
    }

    /**
     * Loads the map definitions for the specified map.
     *
     * @param map The map definition to load.
     * @throws GameException If an error occurs during map loading.
     */
    private void loadMap(MapDefinition map) throws GameException {
        reader = game.getOrLoadMapReader(map);
    }

    /**
     * Loads the music zones associated with the current map.
     *
     * @param map The map definition whose music zones should be loaded.
     */
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
        String mapName = mapFile.getName().replace(".mapbin", "").replace(".map", "").replace(".json.gz", "");
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
            List<MusicZoneEntry> loaded = binary ? readBinaryMusicZones(zonesFile) : readJsonMusicZones(zonesFile);
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

    /**
     * Reads binary music zones from the given file.
     *
     * @param zonesFile The binary file containing music zones.
     * @return A list of MusicZoneEntry objects.
     * @throws Exception If an error occurs during reading.
     */
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

    /**
     * Reads JSON music zones from the given file.
     *
     * @param zonesFile The JSON file containing music zones.
     * @return A list of MusicZoneEntry objects.
     * @throws Exception If an error occurs during reading.
     */
    private List<MusicZoneEntry> readJsonMusicZones(File zonesFile) throws Exception {
        try (FileReader reader = new FileReader(zonesFile)) {
            Type type = new TypeToken<List<MusicZoneEntry>>() {}.getType();
            return new Gson().fromJson(reader, type);
        }
    }

    /**
     * Updates the ambient music based on the player's current position and music zones.
     */
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

    /**
     * Loads the teleport data from the binary teleports file.
     */
    private void loadTeleports() {
        teleports.clear();
        File file = new File(Paths.TELEPORTS_BIN);
        if (!file.exists()) {
            log.warn("Teleport binary file not found: {}", file.getPath());
            return;
        }
        try {
            for (TeleportBinaryIO.Entry entry : TeleportBinaryIO.read(file)) {
                TeleportEntry teleport = new TeleportEntry();
                teleport.id = entry.id;
                teleport.sourceZ = entry.sourceZ;
                teleport.sourceX = entry.sourceX;
                teleport.sourceY = entry.sourceY;
                teleport.targetZ = entry.targetZ;
                teleport.targetX = entry.targetX;
                teleport.targetY = entry.targetY;
                teleports.add(teleport);
            }
            log.info("Loaded {} teleport(s) from {}", teleports.size(), file.getPath());
        } catch (Exception e) {
            log.warn("Failed to load teleports from {}", file.getPath(), e);
        }
    }

    /**
     * Checks if the player is currently standing on a teleport tile and initiates teleportation if so.
     */
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

    /**
     * Teleports the player to the destination defined in the given TeleportEntry.
     *
     * @param teleport The teleport entry to use.
     */
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
        log.info("Teleport {}: ({}, {}, {}) -> ({}, {}, {})", teleport.id,
                teleport.sourceX, teleport.sourceY, teleport.sourceZ,
                teleport.targetX, teleport.targetY, teleport.targetZ);
    }

    /**
     * Packs the teleport coordinates into a single long key.
     *
     * @param z The Z coordinate.
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return The packed teleport key.
     */
    private static long packTeleportKey(int z, int x, int y) {
        return (((long) z & 0xFFFFL) << 48) | (((long) x & 0xFFFFFFL) << 24) | ((long) y & 0xFFFFFFL);
    }
/**
 * Class representing MusicZoneEntry.
 */

    private static class MusicZoneEntry {
        int x1;
        int y1;
        int x2;
        int y2;
        String music;

        /**
         * Normalizes the music zone coordinates so that (x1,y1) is the top-left and (x2,y2) is the bottom-right.
         */
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

        /**
         * Checks if the given tile coordinates are within this music zone.
         *
         * @param x The tile X coordinate.
         * @param y The tile Y coordinate.
         * @return True if the coordinates are within the zone, false otherwise.
         */
        boolean contains(int x, int y) {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2;
        }
    }
/**
 * Class representing TeleportEntry.
 */

    private static class TeleportEntry implements TeleportOverlayRenderer.Entry {
        int id;
        int sourceZ;
        int sourceX;
        int sourceY;
        int targetZ;
        int targetX;
        int targetY;
        public int sourceZ() { return sourceZ; } public int sourceX() { return sourceX; } public int sourceY() { return sourceY; }
        public int targetZ() { return targetZ; } public int targetX() { return targetX; } public int targetY() { return targetY; }
    }

    /**
     * Loads the collision map data for the specified map definition.
     *
     * @param map The map definition to load the collision for.
     */
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

    /**
     * Initializes the player entity, positioning it and applying equipment.
     *
     * @throws GameException If an error occurs during player initialization.
     */
    private void initializePlayer() throws GameException {
        player = new Player(resolvePlayerParts());
        applyEquippedItemsToPlayer();

        float startX = 2846 * GRID_W;
        float startY = 1126 * GRID_H;
        int startZ = currentMap.getZ();

        // Restore saved position if available
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

    /**
     * Resolves the player's body parts and visual equipment for initialization.
     *
     * @return An array of objects representing body parts and their corresponding visuals.
     */
    private Object[] resolvePlayerParts() {
        List<Object> parts = new ArrayList<>();
        Map<String, String> equipment = initialPlayerState == null ? null : initialPlayerState.equipment;
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

        for (Map.Entry<BodyPart, String> entry
                : AppearanceDefaultsCatalog.nakedParts(AppearanceDefaultsCatalog.MALE).entrySet()) {
            parts.add(entry.getKey());
            parts.add(entry.getValue());
        }
        return parts.toArray();
    }

    /**
     * Finds equipment in a map by its key ignoring case.
     *
     * @param equipment The map of equipment strings.
     * @param key       The key to find.
     * @return The equipment name or null if not found.
     */
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

    /**
     * Applies the loaded equipped items to the player instance.
     */
    private void applyEquippedItemsToPlayer() {
        if (player == null || initialPlayerState == null || initialPlayerState.equipment == null) {
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

    /**
     * Applies the XP curve values to the player.
     */
    private void applyXpCurve() {
        if (xpCurve == null || player == null) {
            return;
        }
        xpCurve.applyToPlayer(player);
    }

    /**
     * Creates and initializes a new MapRenderer instance.
     *
     * @return The created MapRenderer.
     * @throws GameException If an error occurs during renderer creation.
     */
    private MapRenderer createMapRenderer() throws GameException {
        ModifSprites modifSprites = ModifSprites.empty();
        MapRenderer renderer = new MapRenderer(reader, spriteLoader, batchSol, batchDecor, outlineShader,
                modifSprites);
        CollisionManager.getInstance().setDynamicProvider((worldX, worldY) -> renderer.isDoorBlockedAt(worldX, worldY));
        return renderer;
    }

    /**
     * Creates and initializes the NPCManager instance.
     *
     * @return The created NPCManager.
     */
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

    /**
     * Creates and initializes the MonsterManager instance.
     *
     * @return The created MonsterManager.
     */
    private MonsterManager createMonsterManager() {
        MonsterManager manager = new MonsterManager(outlineShader);
        manager.setXpCurve(xpCurve);
        try {
            String mapPath = currentMap != null ? currentMap.getMapPath() : Paths.MAP;
            manager.initializeMonstersFromMap(mapPath);
        } catch (GameException e) {
            log.error("Failed to initialize monsters from map json", e);
        }
        return manager;
    }

    /**
     * Configures all necessary callbacks for the game screen components.
     */
    private void configureCallbacks() {
        configureMonsterDamageCallback();
        configureNpcDamageCallback();
        configurePlayerMessageCallback();
        configurePlayerTeleportCallback();
        configurePlayerDeathCallback();
        player.setActionInterruptedCallback(this::clearCurrentAttackTarget);
        player.setHealingCallback(amount -> {
            Vector2 position = player.getPositionVector();
            floatingDamage.spawn(amount, position.x, position.y, FloatingDamage.Type.HEAL);
        });
        player.setManaRestoredCallback(amount -> {
            Vector2 position = player.getPositionVector();
            floatingDamage.spawn(amount, position.x, position.y, FloatingDamage.Type.MANA);
        });
        player.setItemDropCallback((index, itemKey) -> {
            if ("Gem of Destiny".equals(itemKey)) {
                showSystemMessage(I18n.key("message.gem_of_destiny_undroppable"));
                return false;
            }
            int remainingCharges = com.perso.T4C.item.InventoryService.chargesForNextInstance(player, itemKey);
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

    /**
     * Configures input handler callbacks and connections.
     */
    private void configureInputHandlerCallbacks() {
        if (inputHandler == null) {
            return;
        }
        inputHandler.setDebugOverlayToggle(this::toggleCollisionDebugOverlay);
        inputHandler.setTeleportOverlayToggle(this::toggleTeleportOverlay);
        inputHandler.setCoordsHudToggle(this::toggleCoordsHud);
        inputHandler.setHudSupplier(() -> hud);
    }

    /**
     * Configures the monster damage callback to deal damage to the player.
     */
    private void configureMonsterDamageCallback() {
        monsterManager.setDeathCallback(monster -> {
            if (monster instanceof com.perso.T4C.monster.DataMonster dataMonster
                    && dataMonster.usesHumanoidAnimations()) {
                Vector2 position = monster.getPosition();
                spellRenderer.triggerImpactSpell("GreatExplosion", position.x, position.y);
            }
        });
        monsterManager.setDamageDealtCallback((damage, pos) ->
                floatingDamage.spawn(damage, pos.x, pos.y, FloatingDamage.Type.MONSTER_RECEIVED));
        monsterManager.setAttackMissedCallback(this::showMissFeedback);
        monsterManager.setPlayerAttackHitCallback((target, result) -> handleSeraphAuraAttackHit());
        monsterManager.setPlayerDamageCallback(new com.perso.T4C.monster.DamageCallback() {
            @Override
            public void applyDamage(BaseMonster attacker, int rawDamage) {
            CombatProfile attackerProfile = CombatProfiles.fromMonster(attacker);
            CombatProfile playerProfile = CombatProfiles.fromPlayer(player);
            CombatResult result = CombatResolver.resolve(new PhysicalAttackRequest(
                    attackerProfile, playerProfile, rawDamage, 0,
                    attacker.isLastAttackRanged()),
                    ThreadLocalRandom.current());
            int damage = result.damage();
            if (!result.hit()) {
                log.info("{} misses player (precision={})", attacker.getName(), result.precision());
                return;
            }
            Vector2 impactPosition = player.getPositionVector().cpy();
            // Character::hit invokes OnHit after a successful hit and before
            // subtracting the incoming damage. A fully absorbed hit still procs.
            handleSeraphAuraOnHit(attacker, attackerProfile.armorClass());
            player.takeDamage(damage);
            int appliedDamage = player.getLastDamageTaken();
            if (appliedDamage > 0) {
                playRandomPlayerHitSound();
                floatingDamage.spawn(appliedDamage, impactPosition.x, impactPosition.y, FloatingDamage.Type.PLAYER_RECEIVED);
                log.info("Player took {} damage! HP: {}/{}", appliedDamage, player.getCurrentHp(), player.getMaxHp());
            } else {
                log.info("{} hit player, but armor absorbed all {} raw damage (AC={})",
                        attacker.getName(), rawDamage, CombatProfiles.fromPlayer(player).armorClass());
            }
            }

            @Override
            public void applySpell(BaseMonster attacker, int spellId, int rawDamage) {
                String element = spellId == 10120 ? "air" : "water";
                String projectile = spellId == 10120 ? "Lightning" : "PoisonArrow";
                int resistance = Math.max(1, player.getElementResistance(element));
                int damage = Math.max(0, rawDamage * 100 / resistance);
                Runnable impact = () -> {
                    spellRenderer.triggerImpactSpell(projectile + "000", player);
                    Vector2 impactPosition = player.getPositionVector().cpy();
                    player.takeDamage(damage);
                    int appliedDamage = player.getLastDamageTaken();
                    if (appliedDamage > 0) {
                        playRandomPlayerHitSound();
                        floatingDamage.spawn(appliedDamage, impactPosition.x, impactPosition.y,
                                FloatingDamage.Type.PLAYER_RECEIVED);
                    }
                    log.info("{} spell {} hits player for {} damage ({} resistance={})",
                            attacker.getName(), spellId, appliedDamage, element, resistance);
                };
                Vector2 origin = attacker.getPosition();
                ProjectileDirection direction = computeProjectileDirection(origin, player.getPositionVector());
                boolean launched = spellRenderer.launchProjectile(projectile + direction.angle, player,
                        origin.x, origin.y, direction.flipX, impact);
                if (!launched) impact.run();
            }
        });
        monsterManager.setLootCallback(monster -> {
            Vector2 pos = monster.getPosition();
            groundItemManager.spawnFromLoot(monster.getLootTable().roll(lootRandom), pos.x, pos.y, gold -> {
                player.addGold(gold);
                showSystemMessage(I18n.message("message.gold_gained",  gold));
            });
        });
        monsterManager.setPlayerKillCallback(monster -> questService.recordKill(
                player,
                monster.getCanonicalName(),
                currentMap.getZ(),
                monster.getTileX(),
                monster.getTileY()));
    }

    /**
     * Configures the hostile-NPC damage callback (guards/regular NPCs that fight
     * back when attacked via combat mode). Mirrors configureMonsterDamageCallback
     * but with a flat CombatProfile since NPCs carry no per-instance combat stats.
     */
    private void configureNpcDamageCallback() {
        if (npcManager == null) {
            return;
        }
        npcManager.setPlayerDamageCallback((attacker, rawDamage) -> {
            CombatProfile attackerProfile = CombatProfiles.fromNpc(attacker);
            CombatProfile playerProfile = CombatProfiles.fromPlayer(player);
            CombatResult result = CombatResolver.resolve(new PhysicalAttackRequest(
                    attackerProfile, playerProfile, rawDamage, 0, false),
                    ThreadLocalRandom.current());
            if (!result.hit()) {
                log.info("{} misses player (precision={})", attacker.getName(), result.precision());
                return;
            }
            Vector2 impactPosition = player.getPositionVector().cpy();
            player.takeDamage(result.damage());
            int appliedDamage = player.getLastDamageTaken();
            if (appliedDamage > 0) {
                playRandomPlayerHitSound();
                floatingDamage.spawn(appliedDamage, impactPosition.x, impactPosition.y, FloatingDamage.Type.PLAYER_RECEIVED);
                log.info("Player took {} damage from {}! HP: {}/{}", appliedDamage, attacker.getName(),
                        player.getCurrentHp(), player.getMaxHp());
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

    /**
     * Configures the player's system message callback.
     */
    private void configurePlayerMessageCallback() {
        player.setMessageCallback(message -> {
            showSystemMessage(message);
        });
    }

    /**
     * Configures the player's teleport callback used when crossing Z levels.
     */
    private void configurePlayerTeleportCallback() {
        player.setPositionCallback((x, y, z) -> {
            switchMapForZ(z);
            camera.position.set(x, y, 0);
            camera.update();
            log.info("Player teleported to ({}, {}, {})", (int) (x / GRID_W), (int) (y / GRID_H), z);
        });
    }

    /**
     * Switches the currently loaded map based on the targeted Z coordinate.
     *
     * @param z The Z coordinate (depth level) to switch to.
     */
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
            if (mapRenderer != null) {
                mapRenderer.dispose();
            }
            mapRenderer = createMapRenderer();
            inputHandler = new GameInputHandler(log, mapRenderer, player);
            if (npcManager != null) {
                npcManager.dispose();
            }
            npcManager = createNpcManager();
            monsterManager = createMonsterManager();
            groundItemManager.setActiveWorld(z);
            configureMonsterDamageCallback();
            configureNpcDamageCallback();
            configureInputHandlerCallbacks();
            player.setMapBounds(reader.getWidth() * GRID_W, reader.getHeight() * GRID_H);
            calculateRenderBounds();
            warmVisibleCaches(INITIAL_WARMUP_BUFFER, INITIAL_WARMUP_CHUNK_BUDGET, INITIAL_WARMUP_TMPL_BUDGET,
                    INITIAL_WARMUP_DECOR_BUDGET);
            if (hud != null) {
                initializeInputHandlers();
            }
            log.info("Switched to map {} at Z={}", currentMap.name(), currentMap.getZ());
            updateAmbientMusicForPlayer();
        } catch (Exception e) {
            log.error("Failed to switch map for Z={}", z, e);
            showSystemMessage(I18n.message("message.map_load_failed",  z));
        }
    }

    /**
     * Registers a listener to handle sprite loader reloads.
     */
    private void registerReloadListener() {
        SpriteLoader.getInstance().registerReloadListener(() -> {
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
        });
    }

    /**
     * Renders the game screen for the current frame.
     *
     * @param delta The time in seconds since the last render frame.
     */
    @Override
    public void render(float delta) {
        // ── JFR frame event ──────────────────────────────────────────────────
        com.perso.T4C.profiler.FrameEvent frameEvent = gameProfiler.isActive() ? new com.perso.T4C.profiler.FrameEvent() : null;
        if (frameEvent != null) frameEvent.begin();

        section("input", () -> inputHandler.handleInput(delta, player));
        section("teleport", this::updateTeleportForPlayer);
        section("entities", () -> updateEntities(delta));
        section("camera", () -> { updateCamera(); updateAmbientMusicForPlayer(); });
        section("dayNight", () -> {
            dayNightCycle.update(delta);
            dayNightCycle.forceDaylight(player != null && player.hasRadianceBuff());
            // Only the surface world (Z=0) is lit by the sun; everywhere else is dark.
            dayNightCycle.setUnlitWorld(player != null && player.getCoordinates().getZ() != 0);
        });
        section("bounds", () -> {
            calculateRenderBounds();
            warmVisibleCaches(FRAME_WARMUP_BUFFER, FRAME_WARMUP_CHUNK_BUDGET,
                    FRAME_WARMUP_TMPL_BUDGET, FRAME_WARMUP_DECOR_BUDGET);
        });

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        section("groundRender", () ->
                mapRenderer.renderGroundOnly(camera, renderStartX, renderEndX, renderStartY, renderEndY));

        section("entityRender", () -> {
            Vector3 worldCoords = getWorldCoords();
            List<ObjectRenderer.RenderItem> items = buildEntityRenderItems();
            mapRenderer.renderEntitiesWithDecors(camera, worldCoords.x, worldCoords.y,
                    decorRenderStartX, decorRenderEndX, decorRenderStartY, decorRenderEndY, items);
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

        section("overlays", () -> {
            renderCollisionDebugOverlay();
            renderTeleportOverlay();
            renderEntityPathDebugOverlay();
            renderDayNightOverlay();
            renderBrightnessOverlay();
        });

        section("hud", () -> {
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

        // ── commit frame event ───────────────────────────────────────────────
        if (frameEvent != null && frameEvent.shouldCommit()) {
            frameEvent.frameIndex = profilerFrameIndex;
            frameEvent.fps = Gdx.graphics.getFramesPerSecond();
            frameEvent.tileX = player != null ? (int)(player.getPositionVector().x / GRID_W) : 0;
            frameEvent.tileY = player != null ? (int)(player.getPositionVector().y / GRID_H) : 0;
            frameEvent.z     = player != null ? player.getCoordinates().getZ() : 0;
            frameEvent.composedCacheSize = (mapRenderer != null && mapRenderer.getGroundRenderer() != null)
                    ? mapRenderer.getGroundRenderer().getComposedCacheSize() : 0;
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

    // ── Profiler helpers ──────────────────────────────────────────────────────

    private void toggleProfilerEnabled() {
        gameProfiler.toggle(
            () -> showSystemMessage("JFR recording started"),
            () -> showSystemMessage("JFR recording stopped")
        );
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calculates the rendering boundaries based on the camera position.
     */
    private void calculateRenderBounds() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float halfW = camera.viewportWidth * 0.5f;
        float halfH = camera.viewportHeight * 0.5f;
        int buffer = 3;

        renderStartX = clamp(reader.getWidth() - 1, (int) Math.floor((camX - halfW) / GRID_W) - buffer);
        renderEndX = clamp(reader.getWidth() - 1, (int) Math.ceil((camX + halfW) / GRID_W) + buffer);
        renderStartY = clamp(reader.getHeight() - 1, (int) Math.floor((camY - halfH) / GRID_H) - buffer);
        renderEndY = clamp(reader.getHeight() - 1, (int) Math.ceil((camY + halfH) / GRID_H) + buffer);

        decorRenderStartX = clamp(reader.getWidth() - 1, renderStartX - DECOR_RENDER_OVERHANG_TILES);
        decorRenderEndX = clamp(reader.getWidth() - 1, renderEndX + DECOR_RENDER_OVERHANG_TILES);
        decorRenderStartY = clamp(reader.getHeight() - 1, renderStartY - DECOR_RENDER_OVERHANG_TILES);
        decorRenderEndY = clamp(reader.getHeight() - 1, renderEndY + DECOR_RENDER_OVERHANG_TILES);
    }

    /**
     * Warms up visible rendering caches.
     *
     * @param buffer      The buffer size in tiles.
     * @param chunkBudget The chunk budget.
     * @param tmplBudget  The template budget.
     * @param decorBudget The decor budget.
     */
    private void warmVisibleCaches(int buffer, int chunkBudget, int tmplBudget, int decorBudget) {
        int startX = clamp(reader.getWidth() - 1, renderStartX - buffer);
        int endX = clamp(reader.getWidth() - 1, renderEndX + buffer);
        int startY = clamp(reader.getHeight() - 1, renderStartY - buffer);
        int endY = clamp(reader.getHeight() - 1, renderEndY + buffer);
        mapRenderer.warmCaches(startX, endX, startY, endY, chunkBudget, tmplBudget, decorBudget);
    }

    /**
     * Clamps a value between 0 and max.
     *
     * @param max   The maximum value.
     * @param value The value to clamp.
     * @return The clamped value.
     */
    private int clamp(int max, int value) {
        return Math.max(0, Math.min(max, value));
    }

    /**
     * Handles interacting with a spell from the quickbar.
     *
     * @param spell      The spell data to cast or target.
     * @param slotNumber The quickbar slot number clicked.
     */
    private void handleQuickbarSpell(SpellData spell, int slotNumber) {
        if (spell == null) {
            return;
        }
        boolean targetedHeal = isTargetedHealSpell(spell);
        log.debug("Quickbar spell: name={}, slot={}, attack={}, targetedHeal={}, damage={}-{}, buff={}, effects={}",
                spell.getName(), slotNumber, spell.isAttack(), targetedHeal,
                spell.getMinDamage(), spell.getMaxDamage(), spell.getBuff(), spell.getT4cEffects());
        if (targetedHeal) {
            castDefensiveSpell(spell);
            return;
        }
        if (spell.isAttack() || isPositionTargetSpell(spell)) {
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

    private void handleQuickbarItem(String itemName, int slotNumber) {
        com.perso.T4C.item.ItemUseService.Result result = com.perso.T4C.item.ItemUseService.useOnSelf(
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
            // Quickbar entries deliberately remain assigned after their last
            // charge is consumed, so clearly explain why this click cannot run.
            showSystemMessage(I18n.message("message.item_not_owned"));
        } else {
            showSystemMessage(I18n.message("message.item_cannot_use",  I18n.resolve(itemName)));
        }
    }

    private boolean isPositionTargetSpell(SpellData spell) {
        if (spell == null) return false;
        return spell.getTargetType() == 6 || spell.getTargetType() == 16 || spell.getTargetType() == 19;
    }

    /**
     * Cancels the currently active targeted spell if there is one.
     *
     * @return True if a spell was cancelled, false otherwise.
     */
    private boolean cancelActiveTargetedSpell() {
        if (selectedTargetedSpell == null) {
            return false;
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

    /**
     * Checks if a given spell is a targeted healing spell.
     *
     * @param spell The spell to check.
     * @return True if the spell heals and is not an attack spell.
     */
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

    /**
     * Casts a defensive spell for the player.
     *
     * @param spell The defensive spell data.
     */
    private void castDefensiveSpell(SpellData spell) {
        if (spell == null || player == null) {
            return;
        }
        Gdx.app.log("CastDefensive", "Casting: " + spell.getName() + " impact=" + spell.getImpactSpell() + " sound=" + spell.getSound() + " soundImpact=" + spell.getSoundImpact() + " visualEffect=" + spell.getVisualEffect() + " visualEffectTarget=" + spell.getVisualEffectTarget());
        BaseMonster previousAutoCombatTarget = currentAttackTarget;
        BaseNPC previousAutoCombatNpcTarget = currentAttackNpcTarget;
        SpellData previousAutoCombatSpell = currentAttackSpell;
        long previousAutoSpellAttemptAtMs = nextAutoSpellAttemptAtMs;
        SpellCastingService.Result cast = SpellCastingService.begin(new SpellCastingService.Request(
                spell, player, SpellCastingService.TargetKind.SELF, 0f, true, false, true));
        if (!cast.success()) {
            showSystemMessage(SpellCastingService.message(cast.failure()));
            return;
        }
        int healthDelta = spellEffectManager.resolvePlayerHealthDelta(spell, player);
        if (healthDelta > 0) player.applyHeal(healthDelta, healthDelta);
        else if (healthDelta < 0) player.takeDamage(-healthDelta);
        List<SpellData.SpellEffect> originalEffects = spellEffectManager.resolvePlayerBuffEffects(spell, player);
        if (!originalEffects.isEmpty()) {
            player.applyBuff(spell.getName(), spell.getDescription(), spell.getIconId(),
                    spellEffectManager.resolveDurationSeconds(spell, player), false, originalEffects);
        } else {
            applyBuffIfNeeded(spell);
        }
        SpellEffectManager.PlayerUtility utility = spellEffectManager.applyPlayerUtilityEffects(spell, player);
        boolean teleported = false;
        if (utility.teleportTileX() != null && utility.teleportTileY() != null && utility.teleportWorldZ() != null) {
            player.setWorldPosition(utility.teleportTileX() * GRID_W, utility.teleportTileY() * GRID_H,
                    utility.teleportWorldZ());
            teleported = true;
            savePlayerState();
        }
        spellRenderer.playLaunchSound(spell.getSound());
        spellRenderer.playImpactSound(spell.getSoundImpact());
        // Original T4C self-target spells frequently store their cast animation in the
        // projectile slot even though no projectile is launched (Stone skin, Light,
        // Sanctuary, resistance buffs, scroll effects, etc.).
        String impact = spell.getImpactSpell();
        if (impact == null || impact.isEmpty()) {
            impact = spell.getProjectileSpell();
        }
        if (impact != null && !impact.isEmpty()) {
            spellRenderer.triggerImpactSpell(impact, player);
        }
        if (spell.getBuff() != null || !originalEffects.isEmpty()
                || utility.invisibilityApplied() || utility.dispelledEffects() > 0) {
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
        if (spellName.equals(lastClickedBuffSpellName) && now - lastBuffClickMillis <= BUFF_DOUBLE_CLICK_MILLIS) {
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

    /**
     * Tries to cast an attack spell on a target monster.
     *
     * @param monster The monster targeted by the attack spell.
     * @return True if a spell cast attempt was handled, false otherwise.
     */
    private boolean tryCastAttackSpell(BaseMonster monster) {
        if (monster == null || monster.isDead() || selectedTargetedSpell == null || !selectedTargetedSpell.isAttack() || player == null) {
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
            // SpellCastingService deliberately interrupts the previous combat
            // action on every cast. Re-arm this spell after that interruption so
            // it keeps firing at the same living target.
            currentAttackTarget = monster;
            currentAttackNpcTarget = null;
            currentAttackSpell = spell;
            nextAutoSpellAttemptAtMs = System.currentTimeMillis() + AUTO_SPELL_RETRY_DELAY_MS;
        } else {
            clearCurrentAttackTarget();
        }
        return true;
    }

    /** Handles an offensive spell click on a non-player NPC. */
    private boolean tryCastAttackSpell(BaseNPC npc) {
        if (npc == null || selectedTargetedSpell == null || !selectedTargetedSpell.isAttack() || player == null) {
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

    /** Performs one hostile spell attempt, including its projectile and impact. */
    private SpellCastingService.Result castAttackSpell(SpellData spell, BaseMonster monster) {
        float distanceTiles = player.getPositionVector().dst(monster.getPosition()) / Math.max(GRID_W, GRID_H);
        boolean sightClear = !spell.isLineOfSight() || hasLineOfSight(player.getPositionVector(), monster.getPosition());
        SpellCastingService.Result cast = SpellCastingService.begin(new SpellCastingService.Request(
                spell, player, SpellCastingService.TargetKind.HOSTILE_UNIT, distanceTiles,
                sightClear, false, true));
        if (!cast.success()) {
            return cast;
        }
        Vector2 playerPos = player.getPositionVector();
        Vector2 monsterPos = monster.getPosition();
        player.getMovement().faceToward(playerPos.x, playerPos.y, monsterPos.x, monsterPos.y);
        spellRenderer.playLaunchSound(spell.getSound());
        float startX = playerPos.x;
        float startY = playerPos.y;
        String projectileSpell = spell.getProjectileSpell();
        if (projectileSpell == null || projectileSpell.isEmpty()) {
            applySpellImpact(spell, monster);
        } else {
            boolean launched = false;
            if (spell.isLineOfSight()) {
                ProjectileDirection dir = computeProjectileDirection(player.getPositionVector(), monster.getPosition());
                String directionalName = projectileSpell + dir.angle;
                launched = spellRenderer.launchProjectile(directionalName, monster, startX, startY, dir.flipX, () -> applySpellImpact(spell, monster));
                if (!launched) {
                    ProjectileDirection fallback = computeFlipFallbackDirection(player.getPositionVector(), monster.getPosition());
                    String fallbackName = projectileSpell + fallback.angle;
                    launched = spellRenderer.launchProjectile(fallbackName, monster, startX, startY, fallback.flipX, () -> applySpellImpact(spell, monster));
                }
            }
            if (!launched) {
                launched = spellRenderer.launchProjectile(projectileSpell, monster, startX, startY, false, () -> applySpellImpact(spell, monster));
            }
            if (!launched) {
                applySpellImpact(spell, monster);
            }
        }
        return cast;
    }

    /** Performs one hostile spell attempt against an NPC. */
    private SpellCastingService.Result castAttackSpell(SpellData spell, BaseNPC npc) {
        Vector2 playerPos = player.getPositionVector();
        Vector2 npcPos = npc.getPosition();
        float distanceTiles = playerPos.dst(npcPos) / Math.max(GRID_W, GRID_H);
        boolean sightClear = !spell.isLineOfSight() || hasLineOfSight(playerPos, npcPos);
        SpellCastingService.Result cast = SpellCastingService.begin(new SpellCastingService.Request(
                spell, player, SpellCastingService.TargetKind.HOSTILE_UNIT, distanceTiles,
                sightClear, false, true));
        if (!cast.success()) {
            return cast;
        }

        playerPos = player.getPositionVector();
        npcPos = npc.getPosition();
        player.getMovement().faceToward(playerPos.x, playerPos.y, npcPos.x, npcPos.y);
        spellRenderer.playLaunchSound(spell.getSound());
        float startX = playerPos.x;
        float startY = playerPos.y;
        String projectileSpell = spell.getProjectileSpell();
        if (projectileSpell == null || projectileSpell.isEmpty()) {
            applyNpcSpellImpact(spell, npc);
            return cast;
        }

        boolean launched = false;
        if (spell.isLineOfSight()) {
            ProjectileDirection direction = computeProjectileDirection(playerPos, npcPos);
            launched = spellRenderer.launchProjectile(projectileSpell + direction.angle, npc,
                    startX, startY, direction.flipX, () -> applyNpcSpellImpact(spell, npc));
            if (!launched) {
                ProjectileDirection fallback = computeFlipFallbackDirection(playerPos, npcPos);
                launched = spellRenderer.launchProjectile(projectileSpell + fallback.angle, npc,
                        startX, startY, fallback.flipX, () -> applyNpcSpellImpact(spell, npc));
            }
        }
        if (!launched) {
            launched = spellRenderer.launchProjectile(projectileSpell, npc,
                    startX, startY, false, () -> applyNpcSpellImpact(spell, npc));
        }
        if (!launched) {
            applyNpcSpellImpact(spell, npc);
        }
        return cast;
    }

    private void applyNpcSpellImpact(SpellData spell, BaseNPC npc) {
        if (spell == null || npc == null) {
            return;
        }
        if (npcManager != null) {
            npcManager.onNpcAttacked(npc);
        }
        String impact = spell.getImpactSpell();
        if (impact != null && !impact.isEmpty()) {
            Vector2 position = npc.getPosition();
            spellRenderer.triggerImpactSpell(impact, position.x, position.y, spell.getSoundImpact());
        }
    }

    private static boolean isRetryableAutoSpellFailure(SpellCastingService.Failure failure) {
        return failure == SpellCastingService.Failure.COOLDOWN
                || failure == SpellCastingService.Failure.EXHAUSTED
                || failure == SpellCastingService.Failure.ACTIVATION_FAILED;
    }

    /**
     * Attempts a ranged bow attack on the clicked monster.
     *
     * <p>Returns {@code false} when no bow is equipped so the caller falls back to
     * the melee attack. When a bow is equipped this handles the click fully
     * (range check, line of sight, arrow projectile, damage on impact) and returns
     * {@code true}.</p>
     *
     * @param monster The clicked monster.
     * @return True if the bow path handled the click, false to fall back to melee.
     */
    private boolean tryBowAttack(BaseMonster monster) {
        if (monster == null || monster.isDead() || player == null) {
            return false;
        }
        String weaponName = player.getEquippedItems().get(BodyPart.WEAPON);
        ItemDefinition weapon = weaponName == null ? null : ItemDefinition.get(weaponName);
        if (weapon == null || !weapon.isBow()) {
            return false; // not a bow -> let melee handle it
        }
        // GoN's Character::RangedAttack() requires a bow AND a quiver, both
        // equipped, before any arrow can be fired.
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
        // Arm continuous auto-attack only after the target is valid for a bow shot.
        currentAttackTarget = monster;
        currentAttackNpcTarget = null;
        currentAttackSpell = null;
        nextAutoSpellAttemptAtMs = 0L;
        if (player.isAttackReady() && fireBow(monster, false)) {
            player.triggerAttackCooldown(resolveEquippedWeaponAttackSpeed());
        }
        return true;
    }

    /**
     * Fires the bow at the target if in range and with line of sight.
     *
     * @param monster  The target monster.
     * @param announce Whether to show "too far" / "no line of sight" messages.
     * @return True if an arrow was actually fired (damage will apply on impact).
     */
    private boolean fireBow(BaseMonster monster, boolean announce) {
        if (!canFireBowAt(monster, announce)) {
            return false;
        }
        Vector2 playerPos = player.getPositionVector();
        Vector2 monsterPos = monster.getPosition();
        player.getMovement().faceToward(playerPos.x, playerPos.y, monsterPos.x, monsterPos.y);
        float startX = playerPos.x;
        float startY = playerPos.y;
        // Arrow frames only exist for 000/045/090/135/180 (+ flip for the rest).
        ProjectileDirection dir = computeFlipFallbackDirection(playerPos, monsterPos);
        String projectileName = BOW_PROJECTILE_SPRITE + dir.angle;
        String fallbackProjectileName = BOW_PROJECTILE_SPRITE + "000";
        // GoN gates ranged attacks with the bow's exhaust only. Do not wait for
        // the previous arrow to reach the target: its travel time depends on
        // distance and would incorrectly make nearby targets fire faster.
        boolean launched = spellRenderer.launchProjectile(projectileName, monster, startX, startY, dir.flipX, () -> applyBowImpact(monster));
        if (!launched) {
            launched = spellRenderer.launchProjectile(fallbackProjectileName, monster, startX, startY, false, () -> applyBowImpact(monster));
        }
        if (!launched) return false;
        player.attack(player.getMovement(), true);
        return true;
    }

    /**
     * Whether a quiver is equipped in the off-hand.
     *
     * <p>Mirrors GoN's {@code Character::RangedAttack()}, which requires
     * {@code equipped[QUIVER_POS]} to hold a quiver-structured item.</p>
     */
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

    /**
     * Resolves the attack speed (attacks per second) of the equipped weapon,
     * defaulting to 1.0 for unarmed or weapons without a valid value.
     */
    private double resolveEquippedWeaponAttackSpeed() {
        if (player == null) {
            return 1.0;
        }
        String weaponName = player.getEquippedItems().get(BodyPart.WEAPON);
        ItemDefinition weapon = weaponName == null ? null : ItemDefinition.get(weaponName);
        if (weapon == null) {
            return 1.0;
        }
        // GoN's "delay between strikes" is an exhaust duration in milliseconds
        // (e.g. 900 means one shot every 900 ms), not attacks per second. Some
        // migrated definitions still carry that raw value in attackSpeed, which
        // would otherwise produce hundreds of attacks per second.
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

    /**
     * Per-frame tick that continues attacking the current target with the armed
     * weapon or spell until it is cleared.
     */
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
            attacked = monsterManager != null && monsterManager.attackMonster(currentAttackTarget, player);
        }
        if (attacked) {
            player.triggerAttackCooldown(resolveEquippedWeaponAttackSpeed());
        } else {
            player.triggerAttackCooldown(4.0d);
        }
    }

    /** Continues the armed hostile spell, cancelling it as soon as movement starts. */
    private void performAutoSpellTick() {
        if (player.getMovement().isMoving()) {
            clearCurrentAttackTarget();
            return;
        }
        SpellData spell = currentAttackSpell;
        BaseMonster target = currentAttackTarget;
        long now = System.currentTimeMillis();
        if (spell == null || target == null || now < nextAutoSpellAttemptAtMs
                || player.isSpellOnCooldown(spell.getName())
                || player.isMentallyExhausted() || player.isStunned()) {
            return;
        }

        SpellCastingService.Result cast = castAttackSpell(spell, target);
        nextAutoSpellAttemptAtMs = now + AUTO_SPELL_RETRY_DELAY_MS;
        if (cast.success() || isRetryableAutoSpellFailure(cast.failure())) {
            // begin() clears the active action on success (and on a failed
            // activation roll), so restore the loop unless the impact killed the
            // target immediately.
            if (!target.isDead()) {
                currentAttackTarget = target;
                currentAttackSpell = spell;
            }
            return;
        }

        showSystemMessage(SpellCastingService.message(cast.failure()));
        clearCurrentAttackTarget();
    }

    /** Continues a hostile spell against an NPC until movement or cancellation. */
    private void performAutoNpcSpellTick() {
        if (player.getMovement().isMoving()) {
            clearCurrentAttackTarget();
            return;
        }
        SpellData spell = currentAttackSpell;
        BaseNPC target = currentAttackNpcTarget;
        long now = System.currentTimeMillis();
        if (spell == null || target == null || now < nextAutoSpellAttemptAtMs
                || player.isSpellOnCooldown(spell.getName())
                || player.isMentallyExhausted() || player.isStunned()) {
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

    /**
     * Arms continuous auto-attack on the clicked monster and fires the first
     * attack immediately if the weapon is off cooldown.
     *
     * @param monster The clicked, attackable monster.
     */
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

    /**
     * Tab targeting: highlights the closest attackable monster without engaging it.
     * Selection is purely visual (outline + name); attacking still requires a click
     * or another explicit action. Pressing Tab again cycles to the next closest one.
     *
     * @return true if a monster was selected.
     */
    private boolean targetNearestMonster() {
        if (player == null || monsterManager == null) {
            return false;
        }
        Vector2 playerPos = player.getPositionVector();
        float maxDistance = TAB_TARGET_RANGE_TILES * Math.max(GRID_W, GRID_H);

        // Sorted by distance so "next" after the current target is well defined.
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
        // Nothing selected yet (index -1) → take the closest; otherwise wrap to the next.
        BaseMonster next = candidates.get((index + 1) % candidates.size());
        setSelectedMonster(next);
        return true;
    }

    /** Highlights a single monster, clearing any previous selection. */
    private void setSelectedMonster(BaseMonster monster) {
        clearSelectedMonster();
        selectedMonster = monster;
        if (monster != null) {
            monster.setSelected(true);
        }
    }

    /** Drops the selection once the monster dies or walks out of Tab range. */
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

    /** Drops the keyboard selection highlight, if any. */
    private void clearSelectedMonster() {
        if (selectedMonster != null) {
            selectedMonster.setSelected(false);
            selectedMonster = null;
        }
    }

    /** Clears the continuous auto-attack target (e.g. on ground click or Escape). */
    private void clearCurrentAttackTarget() {
        currentAttackTarget = null;
        currentAttackNpcTarget = null;
        currentAttackSpell = null;
        nextAutoSpellAttemptAtMs = 0L;
    }

    /**
     * Applies bow arrow impact: rolls damage, applies it, triggers loot on kill.
     *
     * @param monster The target monster.
     */
    private void applyBowImpact(BaseMonster monster) {
        if (monster == null || monster.isDead()) {
            return;
        }
        int rawDamage = com.perso.T4C.helper.CombatMath.computeBowDamage(player);
        CombatResult result = CombatResolver.resolve(new PhysicalAttackRequest(
                CombatProfiles.fromPlayer(player), CombatProfiles.fromMonster(monster), rawDamage, 0, true),
                ThreadLocalRandom.current());
        player.setHidden(false);
        // Any attack attempt provokes retaliation, hit or miss (matches the original
        // client, where an attacked creature always fights back).
        monster.aggroOn(player.getPositionVector());
        int dmg = result.damage();
        boolean wasDead = monster.isDead();
        int appliedPrimaryDamage = 0;
        if (result.hit()) {
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
            // Aura kills already notify the manager from their own damage path.
            if (appliedPrimaryDamage > 0) {
                monsterManager.notifyKilledByPlayer(monster);
            }
        }
        Vector2 pos = monster.getPosition();
        if (appliedPrimaryDamage > 0) {
            floatingDamage.spawn(appliedPrimaryDamage, pos.x, pos.y, FloatingDamage.Type.MONSTER_RECEIVED);
        } else if (!result.hit()) {
            showMissFeedback(result, pos);
        }
    }

    /**
     * Floats a "miss"/"parried" label where a damage number would have appeared, so a
     * failed blow gives the same visual feedback as a landed one.
     */
    private void showMissFeedback(CombatResult result, Vector2 position) {
        if (result == null || position == null) {
            return;
        }
        String label = result.parried()
                ? I18n.message("combat.parried")
                : I18n.message("combat.missed");
        floatingDamage.spawnText(label, position.x, position.y, FloatingDamage.Type.MISS);
    }

    /** Resolves the two independent effects bound to spell 10696's OnHit hook. */
    private void handleSeraphAuraOnHit(BaseMonster attacker, double attackerArmorClass) {
        if (player == null || attacker == null) {
            return;
        }
        SeraphAuraService.OnHitResult proc = SeraphAuraService.onHit(
                player, attacker.getElementResistance(1), attackerArmorClass, ThreadLocalRandom.current());
        Vector2 playerPosition = player.getPositionVector().cpy();

        if (proc.healingTriggered()) {
            applySeraphAuraHealing(proc.centralHealing(), playerPosition);
            applySeraphAuraHealing(proc.radialHealing(), playerPosition);
            spellRenderer.triggerImpactSpell(SeraphAuraService.HEAL_IMPACT,
                    playerPosition.x, playerPosition.y, SeraphAuraService.HEAL_SOUND);
            // The radius-10 group spell processes its center a second time and
            // broadcasts visual 30096 even when the only group member is self.
            spellRenderer.triggerImpactSpell(SeraphAuraService.HEAL_RADIAL_EFFECT,
                    playerPosition.x, playerPosition.y);
        }

        if (proc.retaliationTriggered() && !attacker.isDead()) {
            Runnable impact = () -> {
                Vector2 currentPosition = attacker.getPosition();
                spellRenderer.triggerImpactSpell(SeraphAuraService.SINGLE_IMPACT,
                        currentPosition.x, currentPosition.y, SeraphAuraService.EXPLOSION_SOUND);
            };
            boolean launched = spellRenderer.launchProjectile(SeraphAuraService.SINGLE_PROJECTILE,
                    attacker, playerPosition.x, playerPosition.y, false, impact);
            if (launched) {
                spellRenderer.playLaunchSound(SeraphAuraService.HEAL_SOUND);
            } else {
                impact.run();
            }
            applySeraphAuraDamage(attacker, proc.retaliationDamage());
        }
    }

    /** Applies one of the original aura's two independently rolled self-heals. */
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

    /** Resolves spell 10698 once, then its damage die independently per target. */
    private void handleSeraphAuraAttackHit() {
        if (player == null || monsterManager == null) {
            return;
        }
        SeraphAuraService.OnAttackHitResult proc = SeraphAuraService.onAttackHit(
                player, ThreadLocalRandom.current());
        if (!proc.triggered()) {
            return;
        }

        Vector2 center = player.getPositionVector().cpy();
        spellRenderer.triggerImpactSpell(SeraphAuraService.AREA_CENTER_IMPACT,
                center.x, center.y, SeraphAuraService.EXPLOSION_SOUND);
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

            int damage = SeraphAuraService.rollAreaDamage(
                    player, candidate.getElementResistance(1), candidate.rollCombatArmorClass(),
                    ThreadLocalRandom.current());
            ProjectileDirection direction = computeFlipFallbackDirection(center, targetPosition);
            String projectile = SeraphAuraService.AREA_PROJECTILE + direction.angle;
            Runnable impact = () -> {
                Vector2 currentPosition = candidate.getPosition();
                spellRenderer.triggerImpactSpell(SeraphAuraService.AREA_CENTER_IMPACT,
                        currentPosition.x, currentPosition.y, SeraphAuraService.EXPLOSION_SOUND);
            };
            boolean launched = spellRenderer.launchProjectile(projectile, candidate,
                    center.x, center.y, direction.flipX, impact);
            if (!launched) {
                launched = spellRenderer.launchProjectile(SeraphAuraService.AREA_PROJECTILE + "000",
                        candidate, center.x, center.y, false, impact);
            }
            if (launched) {
                spellRenderer.playLaunchSound(SeraphAuraService.FIREBALL_SOUND);
            } else {
                impact.run();
            }
            applySeraphAuraDamage(candidate, damage);
        }
    }

    /** Applies aura mental damage, bypassing ordinary AC after its immunity sentinel. */
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

    /**
     * Tries to cast a selected heal spell on the player if they click themselves.
     *
     * @param screenX The screen X coordinate clicked.
     * @param screenY The screen Y coordinate clicked.
     * @return True if the spell was cast, false otherwise.
     */
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

    /**
     * Applies the spell impact effects and damage to the given monster.
     *
     * @param spell   The spell being applied.
     * @param monster The monster target.
     */
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

    private void applyResolvedSpellImpact(SpellData spell, BaseMonster monster, double range, boolean installHooks) {
        SpellEffectManager.Impact impactResult = spellEffectManager.resolve(spell, player, monster, range);
        int healthDelta = impactResult.healthDelta();
        if (healthDelta < 0) {
            int dmg = -healthDelta;
            boolean wasDead = monster.isDead();
            monster.applyPlayerDamage(dmg, player, xpCurve);
            if (!wasDead && monster.isDead() && monsterManager != null) {
                monsterManager.notifyKilledByPlayer(monster);
            }
            Vector2 pos = monster.getPosition();
            floatingDamage.spawn(dmg, pos.x, pos.y, FloatingDamage.Type.MONSTER_RECEIVED);
        } else if (healthDelta > 0) {
            monster.heal(healthDelta);
        }
        if (impactResult.drainedHealth() > 0) {
            player.applyHeal(impactResult.drainedHealth(), impactResult.drainedHealth());
        }
        if (installHooks) spellEffectManager.installTimedHooks(spell, player, monster);
        applySummons(impactResult.summons(), monster.getPosition().x, monster.getPosition().y);
        String impact = spell.getImpactSpell();
        if (impact != null && !impact.isEmpty()) {
            Vector2 pos = monster.getPosition();
            spellRenderer.triggerImpactSpell(impact, pos.x, pos.y, spell.getSoundImpact());
        }
    }

    private void applySummons(List<SpellEffectManager.SummonRequest> summons, float worldX, float worldY) {
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
        boolean sightClear = !spell.isLineOfSight() || hasLineOfSight(player.getPositionVector(), targetPosition);
        SpellCastingService.Result cast = SpellCastingService.begin(new SpellCastingService.Request(
                spell, player, SpellCastingService.TargetKind.POSITION, distanceTiles, sightClear, false, true));
        if (!cast.success()) {
            showSystemMessage(SpellCastingService.message(cast.failure()));
            return true;
        }
        spellRenderer.playLaunchSound(spell.getSound());
        applySummons(spellEffectManager.resolvePositionSummons(spell), target.x, target.y);
        if (spell.getRadius() > 0 && monsterManager != null) {
            for (BaseMonster candidate : monsterManager.getMonsters()) {
                if (candidate.isDead()) continue;
                double range = targetPosition.dst(candidate.getPosition()) / Math.max(GRID_W, GRID_H);
                if (range >= spell.getRadius()) continue;
                if (spell.isLineOfSight() && !hasLineOfSight(targetPosition, candidate.getPosition())) continue;
                applyResolvedSpellImpact(spell, candidate, range, true);
            }
        }
        if (spell.getImpactSpell() != null && !spell.getImpactSpell().isEmpty()) {
            spellRenderer.triggerImpactSpell(spell.getImpactSpell(), target.x, target.y, spell.getSoundImpact());
        }
        return true;
    }

    /**
     * Checks if there is an unblocked line of sight between two points.
     *
     * @param from The starting point.
     * @param to   The destination point.
     * @return True if line of sight exists, false if blocked.
     */
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

    /**
     * Movement collisions and visual obstacles are not the same thing. Water and
     * its TMPL shoreline tiles must remain impassable, but spells can fly over
     * them to reach a target on the opposite bank.
     */
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

    /**
     * Toggles the visibility of the collision debug overlay.
     */
    private void toggleCollisionDebugOverlay() {
        collisionDebugVisible = !collisionDebugVisible;
        showSystemMessage("Collision debug: " + (collisionDebugVisible ? "ON" : "OFF"));
    }

    private boolean isTeleportSourceTile(int tileX, int tileY) {
        if (player == null) return false;
        int z = player.getCoordinates().getZ();
        for (TeleportEntry teleport : teleports) {
            if (teleport.sourceZ == z && teleport.sourceX == tileX && teleport.sourceY == tileY) return true;
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
        TeleportOverlayRenderer.render(debugShapeRenderer, teleports, currentZ,
                renderStartX, renderEndX, renderStartY, renderEndY);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Toggles the visibility of the player coordinates HUD.
     */
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

    /**
     * Renders the day/night ambient darkness overlay (full-screen alpha quad).
     */
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

    /** Applies the user brightness to the world, before the HUD and options are drawn. */
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

    /**
     * Renders the collision debug overlay on the screen.
     */
    private void renderCollisionDebugOverlay() {
        if (!collisionDebugVisible || !CollisionManager.getInstance().isInitialized()) {
            return;
        }

        debugShapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        CollisionOverlayRenderer.render(debugShapeRenderer, renderStartX, renderEndX, renderStartY, renderEndY,
                (x, y) -> CollisionManager.getInstance().hasStaticCollisionAtGrid(x, y),
                (x, y) -> new float[]{1f, 0.5f, 0f, 0.35f});
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        debugShapeRenderer.setColor(0f, 1f, 0f, 0.45f);
        var doorTiles = mapRenderer.getObjectRenderer().getClosedDoorBlockTiles(mapRenderer.getObjectPositions(), mapRenderer.getObjectMappings());
        if (doorTiles != null && !doorTiles.isEmpty()) {
            for (GridPoint2 tile : doorTiles) {
                if (tile.x < renderStartX || tile.x > renderEndX || tile.y < renderStartY || tile.y > renderEndY) {
                    continue;
                }
                debugShapeRenderer.rect(
                        tile.x * GRID_W,
                        tile.y * GRID_H,
                        GRID_W,
                        GRID_H
                );
            }
        }

        debugShapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Renders the entity path debug overlay if enabled.
     */
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
                renderDebugPath(monster.getPosition(), monster.getDebugPath(), monster.getDebugPathTarget());
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

    /**
     * Renders a debug path with lines and points.
     *
     * @param start  The starting position.
     * @param path   The list of path points.
     * @param target The final target position.
     */
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

    /**
     * Updates the cursor state depending on the context of what is hovered.
     */
    private void updateAttackCursor() {
        // The quick bar is a HUD control. An entity rendered underneath it must
        // not affect the world-targeting cursor while the mouse is over the bar.
        if (hud != null && hud.isQuickBarHit(Gdx.input.getX(), Gdx.input.getY())) {
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
                // An active offensive spell takes priority over the attack/bow cursor.
                if (selectedTargetedSpell != null && selectedTargetedSpell.isAttack()) {
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
                // Combat mode lets the player target any NPC, mirroring the original
                // client's crossed-sword cursor override (Objects.AttackMode()).
                if (selectedTargetedSpell != null && selectedTargetedSpell.isAttack()) {
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

    /**
     * Finds a monster that is currently being hovered over by the mouse.
     *
     * @return The hovered monster, or null if none.
     */
    private BaseMonster findHoveredMonster() {
        for (BaseMonster monster : monsterManager.getMonsters()) {
            if (monster.isHovered() && !monster.isDead()) {
                return monster;
            }
        }
        return null;
    }

    /**
     * Sets the mouse cursor to the attack state.
     */
    private void applyAttackCursor() {
        if (attackCursorApplied) return;
        if (game != null && game.cursorManager != null) {
            game.cursorManager.applyAttackCursor(game);
            markAttackCursorApplied();
        }
    }

    /**
     * Resets the mouse cursor to the default state.
     */
    private void applyDefaultCursor() {
        if (!attackCursorApplied && !talkCursorApplied && !spellCursorApplied && !bowCursorApplied) return;
        if (game != null && game.cursorManager != null) {
            game.cursorManager.applyDefaultCursor(game);
        }
        clearCursorState();
    }

    /**
     * Sets the mouse cursor to the talk state.
     */
    private void applyTalkCursor() {
        if (talkCursorApplied) {
            animateTalkCursor();
            return;
        }
        if (animateTalkCursor()) {
            markTalkCursorApplied();
        }
    }

    /**
     * Sets the mouse cursor to the spell targeting state.
     */
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

    /**
     * Sets the mouse cursor to the bow (ranged attack) state.
     */
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

    /**
     * Checks if any NPC is currently hovered by the mouse.
     *
     * @return True if an NPC is hovered, false otherwise.
     */
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

    /**
     * Computes the direction string for a projectile spell based on origin and destination.
     *
     * @param from The origin vector.
     * @param to   The destination vector.
     * @return A computed ProjectileDirection object.
     */
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

    /**
     * Computes a fallback direction with flip state for missing directional animations.
     *
     * @param from The origin vector.
     * @param to   The destination vector.
     * @return A fallback ProjectileDirection.
     */
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
/**
 * Class representing ProjectileDirection.
 */

    private static final class ProjectileDirection {
        private final String angle;
        private final boolean flipX;

        /**
         * Constructor for a ProjectileDirection record.
         *
         * @param angle The angle as a string (e.g., "090").
         * @param flipX Whether to flip the animation on the X axis.
         */
        private ProjectileDirection(String angle, boolean flipX) {
            this.angle = angle;
            this.flipX = flipX;
        }
    }

    /**
     * Tracks defensive spell buffs currently active on the player for HUD display.
     *
     * @param spell The casted spell.
     */
    private void applyBuffIfNeeded(SpellData spell) {
        if (spell == null || spell.isAttack() || spell.getBuff() == null || player == null) {
            return;
        }
        SpellData.SpellBuff buff = spell.getBuff();
        List<SpellData.SpellEffect> effects = buff.getEffects() != null ? buff.getEffects() : List.of();
        player.applyBuff(spell.getName(), spell.getDescription(), spell.getIconId(),
                buff.getDurationSeconds(), Boolean.TRUE.equals(buff.getUnlimited()), effects);
    }

    /**
     * Builds and sorts a list of renderable entity items for depth sorting.
     *
     * @return A list of entity RenderItem records.
     */
    private List<ObjectRenderer.RenderItem> buildEntityRenderItems() {
        entityItems.clear();
        groundItemManager.addRenderItems(entityItems, this::obtainRenderItem, batchDecor, outlineShader);
        addNpcRenderItems(entityItems);
        addMonsterRenderItems(entityItems);
        addPlayerRenderItem(entityItems);
        return entityItems;
    }

    /**
     * Appends NPC rendering items to the given list.
     *
     * @param entityItems The list of render items to populate.
     */
    private void addNpcRenderItems(List<ObjectRenderer.RenderItem> entityItems) {
        if (npcManager == null) {
            return;
        }
        for (BaseNPC npc : npcManager.getNPCs()) {
            npc.getRenderBounds(npcRenderBoundsTemp);
            ObjectRenderer.RenderItem item = addEntityRenderItem(
                    entityItems,
                    // NPCs use the same composite/player sprite footprint.  Their
                    // decor overlap therefore starts two logical rows after the
                    // anchor, just like the player.  Sorting on getTileY() alone
                    // leaves them behind tall foreground decors (e.g. the rock at
                    // the reported position).
                    playerRenderDepth(npc.getTileY() * GRID_H),
                    () -> npc.render(batchDecor, outlineShader));
            item.revealThroughDecor = true;
            item.occlusionRevealAction = () -> npc.renderOcclusionReveal(batchDecor);
            item.revealX = npcRenderBoundsTemp.x;
            item.revealY = npcRenderBoundsTemp.y;
            item.revealW = npcRenderBoundsTemp.width;
            item.revealH = npcRenderBoundsTemp.height;
        }
    }

    /**
     * Appends Monster rendering items to the given list.
     *
     * @param entityItems The list of render items to populate.
     */
    private void addMonsterRenderItems(List<ObjectRenderer.RenderItem> entityItems) {
        if (monsterManager == null) {
            return;
        }
        for (BaseMonster monster : monsterManager.getMonsters()) {
            addEntityRenderItem(entityItems, monster.getTileY(), () -> monster.render(batchDecor, outlineShader));
        }
    }

    /**
     * Appends the player's rendering item to the given list.
     *
     * @param entityItems The list of render items to populate.
     */
    private void addPlayerRenderItem(List<ObjectRenderer.RenderItem> entityItems) {
        if (player == null) {
            return;
        }
        Vector2 pos = player.getPositionVector();
        /*
         * GoN starts the player's decor-overlap band two rows after its logical
         * tile. Keep that fixed rendering anchor separate from movement
         * clearance; the selective translucent redraw below then reconstructs
         * the original wall/player overlap.
         */
        float depthTileY = playerRenderDepth(pos.y);
        player.getAnimations().getRenderBounds(pos, player.getMovement(), playerRenderBoundsTemp);
        Vector3 worldCoords = getWorldCoords();
        boolean hovered = playerRenderBoundsTemp.contains(worldCoords.x, worldCoords.y);
        ObjectRenderer.RenderItem item = addEntityRenderItem(
                entityItems,
                depthTileY,
                () -> player.render(batchDecor, outlineShader, hovered));
        /*
         * GoN draws MainObject, then composites the local DrawOverlapFct wall
         * fragment over it. ObjectRenderer's decor pass followed by the
         * translucent composite redraw produces the same 125/256 decor and
         * 131/256 player blend.
         */
        item.revealThroughDecor = true;
        item.occlusionRevealAction = () -> player.renderOcclusionReveal(batchDecor);
        item.revealX = playerRenderBoundsTemp.x;
        item.revealY = playerRenderBoundsTemp.y;
        item.revealW = playerRenderBoundsTemp.width;
        item.revealH = playerRenderBoundsTemp.height;
    }

    static float playerRenderDepth(float worldY) {
        /*
         * TileSet::DrawOverlapFct starts selecting foreground decor two rows
         * after the player's logical row. This rendering rule is fixed in the
         * original client and deliberately independent of collision lookahead.
         */
        return (float) Math.floor(worldY / GRID_H) + 2f;
    }

    private void applyPeriodicSpellImpact(SpellData spell, Player caster, BaseMonster target) {
        if (caster == player) applyResolvedSpellImpact(spell, target, 0d, false);
    }

    private void configurePlayerDeathCallback() {
        player.setDeathCallback(pvpDeath -> {
            Vector2 deathPosition = player.getPositionVector().cpy();
            DeathPenaltyService.Result penalties = deathPenaltyService.apply(player, pvpDeath, xpCurve,
                    ThreadLocalRandom.current());
            groundItemManager.spawnCorpse(penalties.droppedItems(), penalties.droppedItemCharges(), penalties.goldDropped(),
                    deathPosition.x, deathPosition.y);
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
            showSystemMessage(I18n.message("message.player_died", 
                    penalties.xpLost(), penalties.droppedItems().size()));
            savePlayerState();
        });
    }

    private ObjectRenderer.RenderItem addEntityRenderItem(List<ObjectRenderer.RenderItem> entityItems, float depthY, Runnable renderAction) {
        ObjectRenderer.RenderItem item = obtainRenderItem();
        item.isObject = false;
        item.y = depthY;
        item.renderAction = renderAction;
        entityItems.add(item);
        return item;
    }

    /**
     * Unprojects and caches world coordinates based on mouse input.
     *
     * @return A Vector3 representing world coordinates.
     */
    private Vector3 getWorldCoords() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        if (mouseX != lastMouseX || mouseY != lastMouseY || camera.position.x != lastCameraX || camera.position.y != lastCameraY) {
            worldCoordsTemp.set(mouseX, mouseY, 0);
            camera.unproject(worldCoordsTemp);
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;
            lastCameraX = camera.position.x;
            lastCameraY = camera.position.y;
        }
        return worldCoordsTemp;
    }

    /**
     * Updates the main camera position to follow the player.
     */
    private void updateCamera() {
        float px = snapToScreenPixel(player.getCoordinates().getX(), camera.viewportWidth, viewport.getScreenWidth());
        float py = snapToScreenPixel(player.getCoordinates().getY(), camera.viewportHeight, viewport.getScreenHeight());
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
        if (mapRenderer != null && mapRenderer.getObjectRenderer() != null) {
            mapRenderer.getObjectRenderer().renderNameOverlay(batch, mapRenderer.getObjectPositions(), mapRenderer.getObjectMappings());
        }
        renderPlayerTalkText(batch);
    }

    private void renderPlayerTalkText(SpriteBatch batch) {
        if (player == null) return;
        String text = player.getTalkText();
        if (text == null || text.isEmpty()) return;

        // Tileset.cpp anchors MainObject speech at its fixed logical position:
        // the horizontal tile centre and 67 pixels above the tile origin. It
        // deliberately does not use puppet/equipment bounds.
        float talkCenterX = player.getPositionVector().x + (GRID_W * 0.5f);
        float talkBottomY = player.getPositionVector().y - 67f;
        NameRenderer.renderNameWithKeyword(batch, text, null,
                talkCenterX, talkBottomY, 0f, 0f);
    }

    /**
     * Updates all game entities for the current frame loop.
     *
     * @param delta Time elapsed in seconds.
     */
    private void updateEntities(float delta) {
        // Previously iterated over a list of Updatable; now directly update known entities.
        floatingDamage.update(delta);
        if (player != null) {
            if (SeraphAuraService.synchronize(player)) {
                savePlayerState();
            }
            player.update(delta);
            playerPositionTemp.set(player.getPositionVector());
            int margin = 5;
            if (npcManager != null) {
                npcManager.updateVisible(delta, playerPositionTemp, renderStartX, renderEndX, renderStartY, renderEndY, margin);
            }
            if (monsterManager != null) {
                monsterManager.updateVisible(delta, playerPositionTemp, renderStartX, renderEndX, renderStartY, renderEndY, margin);
            }
            pruneSelectedMonster();
            performAttackTick();
            spellEffectManager.update(this::applyPeriodicSpellImpact);
        }
    }

    /**
     * Pools and retrieves an ObjectRenderer.RenderItem instance.
     *
     * @return A blank RenderItem object.
     */
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
        item.y = 0f;
        return item;
    }

    /**
     * Returns rendering items to the object pool.
     *
     * @param items The list of rendering items to release.
     */
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
            renderItemPool.addLast(item);
        }
    }

    /**
     * Resizes the main viewports when window dimension changes.
     *
     * @param w The new width.
     * @param h The new height.
     */
    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
        updateHudCamera(w, h);
        stage.getViewport().update(w, h, true);
    }

    /**
     * Adjusts HUD camera using system's current dimensions.
     */
    private void updateHudCamera() {
        updateHudCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Safely refits the HUD camera for dynamic resizing.
     *
     * @param width  The viewport width.
     * @param height The viewport height.
     */
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

    /**
     * Initializes HUDs; configures input multiplexer; sets caches.
     */
    @Override
    public void show() {
        initializeHud();
        initializeInputHandlers();
        updateCamera();
        calculateRenderBounds();
        warmVisibleCaches(INITIAL_WARMUP_BUFFER, INITIAL_WARMUP_CHUNK_BUDGET, INITIAL_WARMUP_TMPL_BUDGET,
                INITIAL_WARMUP_DECOR_BUDGET);
    }

    /**
     * Instantiates the graphical UI components on-screen.
     */
    private void initializeHud() {
        hud = new PlayerHUD(player, spriteLoader);
        hud.setBackpackAction(() -> {
            if (GuiManager.isCurrent(Inventory.class)) {
                GuiManager.close();
            } else {
                GuiManager.open(new Inventory(player, hud));
            }
        });
        hud.setCharacterAction(() -> {
            if (GuiManager.isCurrent(Statistics.class)) {
                GuiManager.close();
            } else {
                GuiManager.open(new Statistics(player));
            }
        });
        hud.setSpellBookAction(() -> {
            if (GuiManager.isCurrent(SpellBook.class)) {
                GuiManager.close();
            } else {
                GuiManager.open(new SpellBook(player, hud));
            }
        });
        hud.setQuestAction(() -> {
            if (GuiManager.isCurrent(QuestScreen.class)) {
                GuiManager.close();
            } else {
                GuiManager.open(new QuestScreen(player));
            }
        });
        hud.setMapAction(() -> {
            if (GuiManager.isCurrent(MapScreen.class)) {
                GuiManager.close();
            } else {
                GuiManager.open(new MapScreen());
            }
        });
        hud.setOptionsAction(() -> {
            if (GuiManager.isCurrent(OptionsScreen.class)) {
                GuiManager.close();
            } else {
                GuiManager.open(new OptionsScreen());
            }
        });
        coordsHud = new PlayerCoordsHud(player);
        systemMessage = new SystemMessage();
        gameChat = new GameChat(text -> {
            if (player != null) {
                player.showTalkText(text);
                if (npcManager != null && npcManager.hasActiveConversation()) {
                    npcManager.talkToActiveNpc(text, player);
                }
            }
            // This callback is also the future network send point.
        });
        gameChat.addSystemMessage(I18n.key("chat.help"));
        SystemMessage.setShared(systemMessage);
        SystemMessage.setChatSink(gameChat::addSystemMessage);
        com.perso.T4C.spell.NpcCastVfxHook.setShared(this::playNpcCastVfx);
        mapZoneDisplay = new GuiMapZoneDisplay(I18n.key("zone.lighthaven"));
    }

    /**
     * Plays the same sound/projectile/impact visuals as a self-cast player
     * spell, but travelling from the casting NPC to the player (an NPC
     * "casts on you" instead of casting on itself).
     */
    private void playNpcCastVfx(SpellData spell, Player castOn, Vector2 casterPosition) {
        if (spell == null || castOn == null) {
            return;
        }
        spellRenderer.playLaunchSound(spell.getSound());
        String impact = spell.getImpactSpell();
        if (impact == null || impact.isEmpty()) {
            impact = spell.getProjectileSpell();
        }
        if (impact == null || impact.isEmpty()) {
            return;
        }
        String projectileSpell = spell.getProjectileSpell();
        if (projectileSpell == null || projectileSpell.isEmpty() || casterPosition == null) {
            spellRenderer.playImpactSound(spell.getSoundImpact());
            spellRenderer.triggerImpactSpell(impact, castOn);
            return;
        }
        Vector2 playerPos = castOn.getPositionVector();
        final String impactEffect = impact;
        Runnable onImpact = () -> {
            spellRenderer.playImpactSound(spell.getSoundImpact());
            spellRenderer.triggerImpactSpell(impactEffect, castOn);
        };
        boolean launched = false;
        if (spell.isLineOfSight()) {
            ProjectileDirection direction = computeProjectileDirection(casterPosition, playerPos);
            launched = spellRenderer.launchProjectile(projectileSpell + direction.angle, castOn,
                    casterPosition.x, casterPosition.y, direction.flipX, onImpact);
            if (!launched) {
                ProjectileDirection fallback = computeFlipFallbackDirection(casterPosition, playerPos);
                launched = spellRenderer.launchProjectile(projectileSpell + fallback.angle, castOn,
                        casterPosition.x, casterPosition.y, fallback.flipX, onImpact);
            }
        }
        if (!launched) {
            launched = spellRenderer.launchProjectile(projectileSpell, castOn,
                    casterPosition.x, casterPosition.y, false, onImpact);
        }
        if (!launched) {
            onImpact.run();
        }
    }

    /**
     * Prepares the game's multiplexer chain catching inputs effectively.
     */
    private void initializeInputHandlers() {
        // Set up object click handler for clickAnimate objects
        ObjectClickHandler objectClickHandler = new ObjectClickHandler(mapRenderer.getObjectRenderer(), mapRenderer.getObjectPositions(), mapRenderer, camera, player, systemMessage, groundItemManager);

        // Set up NPC input handler for NPC interactions
        NPCInputHandler npcInputHandler = new NPCInputHandler(
                npcManager, camera, player, this::tryCastAttackSpell, systemMessage);

        // Set up Monster input handler for monster interactions (pass player for distance checking)
        MonsterInputHandler monsterInputHandler = new MonsterInputHandler(camera, monsterManager, player, this::tryCastAttackSpell, this::tryBowAttack, systemMessage);
        monsterInputHandler.setOnAttackTargetSelected(this::beginAttackTarget);
        monsterInputHandler.setOnClickedElsewhere(() -> {
            clearCurrentAttackTarget();
            cancelActiveTargetedSpell();
        });

        // Set up ground item pickup handler (loot dropped on the ground)
        GroundItemClickHandler groundItemClickHandler = new GroundItemClickHandler(camera, groundItemManager, player, systemMessage);

        // Set up click-to-move handler
        ClickToMoveHandler clickToMoveHandler = new ClickToMoveHandler(camera, reader, player, hud,
                this::handleQuickbarSpell, this::handleQuickbarItem);

        // Set up input processing
        InputMultiplexer multiplexer = new InputMultiplexer();
        InputAdapter guiAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (GuiManager.isOpen() && GuiManager.onKeyDown(keycode)) {
                    return true;
                }
                if (keycode == Input.Keys.TAB && !GuiManager.isOpen() && !isTextInputActive()) {
                    targetNearestMonster();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE && (currentAttackTarget != null || selectedMonster != null
                        || selectedTargetedSpell != null)) {
                    clearCurrentAttackTarget();
                    clearSelectedMonster();
                    cancelActiveTargetedSpell();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE && !isTextInputActive()) {
                    GuiManager.open(new OptionsScreen());
                    return true;
                }
                if (keycode == Input.Keys.F9) {
                    toggleProfilerEnabled();
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
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                boolean controlDown = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
                if (button == Input.Buttons.LEFT && hud != null
                        && hud.chatBarButtonsTouchDown(screenX, screenY, controlDown)) {
                    return true;
                }
                if (button == Input.Buttons.LEFT && gameChat != null && gameChat.boxedTouchDown(screenX, screenY)) return true;
                if (button == Input.Buttons.LEFT && hud != null
                        && hud.boxedTouchDown(screenX, screenY, controlDown)) {
                    return true;
                }
                if (button == Input.Buttons.RIGHT && hud != null && hud.showBuffTooltipAt(screenX, screenY)) {
                    return true;
                }
                if (button == Input.Buttons.LEFT && handleBuffBarLeftClick(screenX, screenY)) {
                    return true;
                }
                if (button == Input.Buttons.RIGHT && hud != null && hud.showQuickSlotTooltipAt(screenX, screenY)) {
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
                if (button == Input.Buttons.LEFT && hud != null
                        && hud.chatBarButtonsTouchUp(screenX, screenY)) {
                    return true;
                }
                if (button == Input.Buttons.LEFT && hud != null && hud.boxedTouchUp()) return true;
                if (button == Input.Buttons.LEFT && gameChat != null && gameChat.boxedTouchUp()) return true;
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
                return false;
            }
        };
        InputAdapter positionSpellHandler = new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return button == Input.Buttons.LEFT && tryCastPositionSpell(screenX, screenY);
            }
        };
        GmInputHandler gmInputHandler = new GmInputHandler(player, new GmCommandProcessor(xpCurve, npcManager, monsterManager));
        inputHandler.setGmInputHandler(gmInputHandler);
        inputHandler.setTextInputActiveSupplier(
                () -> gameChat != null && gameChat.isActive());
        this.textInputActiveSupplier =
                () -> gameChat != null && gameChat.isActive();
        multiplexer.addProcessor(gameChat);
        multiplexer.addProcessor(gmInputHandler);
        multiplexer.addProcessor(guiAdapter);
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(monsterInputHandler);  // Process monster interactions first
        multiplexer.addProcessor(npcInputHandler);  // Process NPC interactions
        multiplexer.addProcessor(groundItemClickHandler);  // Pick up loot before moving/object clicks
        multiplexer.addProcessor(objectClickHandler);  // Process object clicks before tile clicks
        multiplexer.addProcessor(positionSpellHandler);
        multiplexer.addProcessor(clickToMoveHandler);
        multiplexer.addProcessor(new TileClickHandler(reader, camera));
        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * Executes logic triggered when this screen is hidden.
     */
    @Override
    public void hide() {
    }

    /**
     * Executes logic triggered when the application is paused.
     */
    @Override
    public void pause() {
    }

    /**
     * Executes logic triggered when the application is resumed.
     */
    @Override
    public void resume() {
    }

    /**
     * Releases memory allocated by rendering batches and systems.
     */
    @Override
    public void dispose() {
        savePlayerState();
        gameProfiler.stop();
        SoundManager.stopAmbient();
        mapRenderer.dispose();
        batchSol.dispose();
        batchDecor.dispose();
        batch.dispose();
        debugShapeRenderer.dispose();

        if (systemMessage != null) {
            SystemMessage.setShared(null);
            com.perso.T4C.spell.NpcCastVfxHook.setShared(null);
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

        // Dispose all cached fonts
        FontManager.getInstance().dispose();
    }
}
