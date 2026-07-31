package com.perso.T4C.screens;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.perso.T4C.MyGame;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.DecorLayerRuleBinaryIO;
import com.perso.T4C.helper.DisplayModeToggle;
import com.perso.T4C.helper.ClanRelationsBinaryIO;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.MusicZoneBinaryIO;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.ObjectPositionBinaryIO;
import com.perso.T4C.helper.ResolvedSprite;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.helper.SpriteNameParser;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.helper.TeleportBinaryIO;
import com.perso.T4C.helper.GroundMosaicCatalog;
import com.perso.T4C.tmpl3.Direction;
import com.perso.T4C.tmpl3.TerrainContext;
import com.perso.T4C.tmpl3.TerrainResolver;
import com.perso.T4C.tmpl3.Tmpl3Change;
import com.perso.T4C.tmpl3.Tmpl3Mask;
import com.perso.T4C.tmpl3.Tmpl3MaskLoader;
import com.perso.T4C.tmpl3.Tmpl3RegenerationResult;
import com.perso.T4C.tmpl3.Tmpl3Regenerator;
import com.perso.T4C.editor.ui.EditorButton;
import com.perso.T4C.editor.ui.EditorContextMenu;
import com.perso.T4C.editor.ui.EditorListBox;
import com.perso.T4C.editor.ui.EditorDialog;
import com.perso.T4C.editor.ui.EditorDropdownList;
import com.perso.T4C.editor.ui.EditorPanelChrome;
import com.perso.T4C.editor.ui.EditorTheme;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.render.ObjectMapping;
import com.perso.T4C.render.ObjectMappings;
import com.perso.T4C.objects.ObjectPos;
import com.perso.T4C.monster.BaseMonster;
import com.perso.T4C.monster.DataMonster;
import com.perso.T4C.monster.MonsterClan;
import com.perso.T4C.monster.MonsterClanRelations;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.monster.MonsterRegistry;
import com.perso.T4C.npc.BaseNPC;
import com.perso.T4C.npc.DataNpc;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.npc.NpcRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.PuppetBodyOrder;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * Standalone map editor allowing:
 * - Click on a tile to change its sprite via a graphical interface
 * - Move decorations between tiles (no free pixel offsets)
 * - Save modifications in modif_sprites.json
 */
@Slf4j
public class MapEditorScreen implements Screen {

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final SpriteBatch uiBatch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final boolean ownsBatch;

    // Map data
    private MapReader mapReader;
    private final SpriteLoader spriteLoader;
    private final GroundMosaicCatalog groundMosaicCatalog;
    private final SpriteBatch batchSol;
    private final SpriteBatch batchDecor;
    private MapRenderer mapRenderer;
    private ModifSprites modifSprites;
    private ShaderProgram outlineShader;
    private SpriteBatch outlineBatch;
    private boolean isLoading = true;
    private String loadingMessage = "Loading...";
    private float loadingProgress = 0f;
    private int loadingStep = 0;
    private int loadingStepCount = 6;
    private boolean mapSwitchLoading = false;
    private boolean exitSaving = false;
    private boolean exitSaveCompleted = false;
    private boolean exitSaveStepPrepared = false;
    private String pendingMapPath = null;
    private boolean loadingOverlayPresented = false;
    private int preloadSpriteIndex = 0;
    private List<SpriteLoader.Sprite> preloadSprites = null;
    private Set<String> cachedSpriteNames = null;
    private Thread wdaLoadThread = null;
    private volatile GameException wdaLoadError = null;
    private volatile boolean wdaLoadDone = false;
    private volatile float wdaLoadProgress = 0f;
    private volatile String wdaLoadStage = "Starting";
    private long lastSpritesBinModified = -1L;
    private long spritesBinWatchEnabledAt = 0L;
    private static final long SPRITES_WATCH_STARTUP_DELAY_MS = 2000L;
    private final Map<String, int[]> decorOffsetOverrides = new HashMap<>();
    private String pendingOffsetSprite = null;
    private boolean pendingOffsetDirty = false;
    private boolean webEditorReloadEnabled = true;
    private boolean allowManualSave = false;
    private static final long OFFSET_WRITE_DEBOUNCE_MS = 400L;
    private final Object offsetWriteLock = new Object();
    private final Map<String, int[]> pendingOffsetWrites = new HashMap<>();
    private final List<SaveStep> exitSaveSteps = new ArrayList<>();
    private int exitSaveStepIndex = 0;
    private final ScheduledExecutorService offsetWriteExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sprites-bin-writer");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService collisionGenerationExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "collision-generator");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService tmpl3RegenerationExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "tmpl3-regenerator");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService groundRecalculationExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ground-recalculator");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService teleportPreviewExecutor = Executors.newFixedThreadPool(
            Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2)),
            r -> {
                Thread t = new Thread(r, "teleport-preview-loader");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> offsetWriteFuture = null;
    private volatile boolean spriteHotReloading = false;
    private volatile boolean collisionRegenerating = false;
    private volatile float collisionRegenerationProgress = 0f;
    private volatile String collisionRegenerationStage = "";
    private volatile boolean tmpl3Regenerating = false;
    private volatile float tmpl3RegenerationProgress = 0f;
    private volatile String tmpl3RegenerationStage = "";
    private volatile Map<Long, String> smoothingTerrainOverlay = Map.of();
    private volatile String activeTmplRegenerationName = "Tmpl3";
    private static final int TMPL3_APPLY_BATCH_SIZE = 2500;
    private static final int GROUND_RECALC_APPLY_BATCH_SIZE = 2500;
    private List<Tmpl3RegenerationChange> pendingTmpl3RegenerationChanges = null;
    private UndoEntry pendingTmpl3RegenerationUndo = null;
    private int pendingTmpl3RegenerationIndex = 0;
    private int pendingTmpl3RegenerationScannedCount = 0;
    private int pendingTmpl3RegenerationChangedCount = 0;
    private volatile boolean groundRecalculating = false;
    private volatile float groundRecalculationProgress = 0f;
    private volatile String groundRecalculationStage = "";
    // Generic flag for other long-ish Tiles tools (e.g. Reorder House Walls) so they
    // can drive the same progress bar without overloading the Tmpl3/ground flags.
    private List<GroundRecalculationChange> pendingGroundRecalculationChanges = null;
    private UndoEntry pendingGroundRecalculationUndo = null;
    private int pendingGroundRecalculationIndex = 0;
    private int pendingGroundRecalculationChangedCount = 0;
    private int pendingGroundRecalculationScannedCount = 0;
    private int pendingGroundRecalculationSampleLogCount = 0;
    private int pendingGroundRecalculationNextLogPercent = 95;

    private String currentMapPath = Paths.MAP;
    private final List<String> availableMaps = new ArrayList<>();
    private int currentMapIndex = 0;
    private boolean groundSelectOpen = false;
    private int groundSelectHoverIndex = -1;
    private int groundSelectScrollOffset = 0;
    private int groundSelectVisibleRows = 1;
    private com.badlogic.gdx.math.Rectangle groundSelectBounds = null;
    private com.badlogic.gdx.math.Rectangle groundSelectDropdownBounds = null;
    private float groundSelectListTop = 0f;

    private static final int MAP_SELECT_ITEM_HEIGHT = 22;
    private static final int GROUND_SELECT_WIDTH = 260;
    private static final String MUSIC_ZONES_SUFFIX = ".musiczones.json";
    private static final String MUSIC_ZONES_BIN_SUFFIX = ".musiczones.bin";
    private static final String[] AMBIENT_MUSIC_TYPES = {
            "Sadness Music",
            "Dungeons Music",
            "Outdoors Music",
            "Caverns Music",
            "Forest Music",
            "Noises Music",
            "Boss Music"
    };

    // Camera controls
    private final Vector2 cameraPosition = new Vector2(1536 * GameConstants.GRID_W, 1536 * GameConstants.GRID_H);
    private static final float CAMERA_SPEED = 1000f;
    private static final float CAMERA_ACCELERATION_RATE = 1.2f;
    private static final float CAMERA_MAX_SPEED_MULTIPLIER = 3.0f;
    private static final float ZOOM_SPEED = 0.3f;
    private float zoom = 1f;
    private float cameraMoveHoldTime = 0f;

    // Camera position save file
    private static final String CAMERA_POSITION_FILE = Paths.EDITOR_CAMERA_POSITION_FILE;

    // Editor message system (simple implementation for inverted Y coordinates)
    private String editorMessage = null;
    private float editorMessageTimer = 0f;
    private static final float MESSAGE_DURATION = 3.0f;
    private static final float MESSAGE_FADE_DURATION = 0.5f;
    private final List<String> groundFillSprites = new ArrayList<>();
    private final Map<String, GroundFillPattern> groundFillPatterns = new HashMap<>();
    private static final float SCALE_MIN = 0.05f;
    private static final float SCALE_MAX = 4.0f;
    private static final float SCALE_STEP = 0.01f;
    private static final float DECOR_NUDGE_INITIAL_DELAY = 0.15f;
    private static final float DECOR_NUDGE_MIN_INTERVAL = 0.02f;
    private static final float DECOR_NUDGE_ACCEL = 0.06f;
    private static final long HOVER_UPDATE_INTERVAL_MS = 33L;
    private static final int HOVER_UPDATE_MIN_PIXEL_DELTA = 2;
    private static final int RENDER_TILE_MARGIN = 10;
    
    private static final java.util.regex.Pattern GRID_VARIANT_PATTERN = java.util.regex.Pattern
            .compile("^(.+?)\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\s*$");
    private long lastHoverUpdateMs = 0L;
    private int lastHoverScreenX = Integer.MIN_VALUE;
    private int lastHoverScreenY = Integer.MIN_VALUE;

    // Editor state
    private EditorMode editorMode = EditorMode.SELECT_TILE;
    private SpritePickerUI spritePicker;
    private CollisionRuleEditorUI collisionRuleEditor;
    private DecorLayerRuleEditorUI decorLayerRuleEditor;
    private ClanRelationsEditorUI clanRelationsEditor;
    private ObjectMappingsEditorUI objectMappingsEditor;
    private SpellEditorUI spellEditor;
    private MonsterDefEditorUI monsterDefEditor;
    private NpcDefEditorUI npcDefEditor;
    private ItemEditorUI itemEditor;
    private MonsterDef monsterClipboard;
    private Set<String> pendingDecorLayerRuleNames = null;
    private boolean decorLayerRulesDirty = false;
    private int selectedTileX = -1;
    private int selectedTileY = -1;
    private int lastClickedTileX = -1;
    private int lastClickedTileY = -1;
    private int hoveredTileX = -1;
    private int hoveredTileY = -1;

    // Cached sprite list for picker (loaded once)
    private List<SpritePickerUI.SpriteEntry> cachedSpriteList = null;
    private final Deque<String> recentSpriteHistory = new ArrayDeque<>();
    private static final int SPRITE_HISTORY_SIZE = 5;
    private EntityPickerUI entityPicker;

    // Drag mode for moving decors/objects
    private DragState dragState = null;
    private ObjectDragState objectDragState = null;
    private GroundPaintState groundPaintState = null;
    // Lazily-built regenerators shared by the Tmpl brushes (masks are loaded once per family).
    private final Map<String, Tmpl3Regenerator> tmplPaintRegenerators = new HashMap<>();
    private DecorInfo selectedDecorInfo = null;

    // Double-click detection for mirroring (removed)

    // Collision editor
    private byte[] collisionData = null;
    private int collisionMapWidth = 0;
    private int collisionMapHeight = 0;
    private static final int COLLISION_VALUE_RED = 1;
    private int selectedCollisionValue = CollisionType.ABSOLUTE.getValue();
    private boolean collisionOverlayVisible = false;
    private boolean teleportOverlayVisible = false;
    private long lastCollisionRulesModified = -1L;
    private long nextCollisionRulesWatchAt = 0L;
    private static final long COLLISION_RULES_WATCH_INTERVAL_MS = 500L;
    private static final byte[] COLLISION_RULES_MAGIC = new byte[] { 'T', '4', 'C', 'C', 'R', 'L' };
    private static final short COLLISION_RULES_BIN_VERSION = 2;

    // Copy/Paste system
    private CopiedTile copiedTile = null;
    private List<CopiedAreaTile> copiedAreaTiles = null;
    private RectangleSelection rectangleSelection = null;
    private boolean isSelecting = false;
    private Vector2 selectionStart = new Vector2();
    private final LinkedHashSet<Long> lassoSelectionTiles = new LinkedHashSet<>();
    private final List<Long> lassoSelectionPath = new ArrayList<>();
    private int lastLassoTileX = Integer.MIN_VALUE;
    private int lastLassoTileY = Integer.MIN_VALUE;
    private boolean autofillRectSelecting = false;

    // Minimap (always visible)
    private static final int MINIMAP_SIZE = 200;
    private static final int MINIMAP_MARGIN = 10;
    private static final Color COLLISION_COLOR_RED = new Color(1f, 0f, 0f, 0.6f);

    // Rendering toggles
    private boolean decorVisible = true;
    private boolean objectsVisible = true;
    private boolean groundOutlineEnabled = false;
    private boolean fillToolEnabled = false;
    private String fillToolSprite = null;
    // Autofill behaviour: whether automatic base-based filling is enabled
    private boolean autofillEnabled = false;
    // If true, autofill operations will persist (save) changes to disk; otherwise
    // changes stay in-memory until explicit save
    private boolean autofillPersist = false;
    private boolean scaleToolEnabled = false;
    private static final int MAX_UNDO = 30;
    private final Deque<UndoEntry> undoStack = new ArrayDeque<>();
    private boolean mapDirty = false;
    private boolean monstersDirty = false;
    private boolean npcsDirty = false;
    private boolean musicZonesDirty = false;
    private boolean teleportsDirty = false;
    private boolean objectPositionsDirty = false;
    private boolean musicZoneEditingEnabled = false;
    private int selectedAmbientMusicIndex = 0;
    private final List<MusicZoneEntry> musicZones = new ArrayList<>();
    private final Map<String, Set<Long>> cachedMusicTilesByMusic = new LinkedHashMap<>();
    private boolean musicTileIndexDirty = true;
    private int[] lastMusicOverlayBounds = null;
    private Texture minimapTexture = null;
    private boolean minimapDirty = true;
    // Tile-space centers of detected houses (clusters of wall tiles), shown as
    // yellow dots on the minimap. Recomputed when the minimap is rebuilt.
    private final List<float[]> houseCenters = new ArrayList<>();
    // Minimum number of connected wall tiles to count as a house (filters out
    // stray/isolated wall sprites).
    private static final int HOUSE_MIN_WALL_TILES = 4;
    private MusicZoneEntry selectedMusicZone = null;
    private final List<TeleportEntry> teleports = new ArrayList<>();
    private final List<ObjectPos> objectPositions = new ArrayList<>();
    private final List<String> objectPositionTypes = new ArrayList<>();
    private TeleportEntry selectedTeleport = null;
    private TeleportEntry draggedTeleport = null;
    private boolean draggedTeleportEndpointIsSource = true;
    private int draggedTeleportTargetX;
    private int draggedTeleportTargetY;
    private int selectedObjectPositionIndex = -1;
    private int hoveredObjectPositionIndex = -1;
    private int selectedObjectTypeIndex = 0;
    private int objectPositionScrollOffset = 0;
    private int objectPositionVisibleRows = 1;
    private com.badlogic.gdx.math.Rectangle objectPositionPanelBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionListBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionNewBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionDeleteBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionSaveBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionTypeBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionTypeDropdownBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionTypePreviewBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionTypeScrollTrackBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionTypeScrollThumbBounds = null;
    private final EditorDropdownList<String> objectPositionTypeDropdown = new EditorDropdownList<>();
    private boolean objectPositionTypeDropdownOpen = false;
    private int objectPositionTypeScrollOffset = 0;
    private int objectPositionTypeVisibleRows = 1;
    private com.badlogic.gdx.math.Rectangle objectPositionScrollTrackBounds = null;
    private com.badlogic.gdx.math.Rectangle objectPositionScrollThumbBounds = null;
    private boolean selectedTeleportEndpointIsSource = true;
    private boolean teleportContextOpen = false;
    private TeleportEntry teleportContextTarget = null;
    private boolean teleportContextTargetIsSource = true;
    private com.badlogic.gdx.math.Rectangle teleportContextBounds = null;
    private EditorContextMenu teleportContextMenu = null;
    private boolean objectContextOpen = false;
    private int objectContextTargetIndex = -1;
    private com.badlogic.gdx.math.Rectangle objectContextBounds = null;
    private EditorContextMenu objectContextMenu = null;
    private int teleportScrollOffset = 0;
    private int teleportEditorZ = 0;
    private com.badlogic.gdx.math.Rectangle teleportPanelBounds = null;
    private com.badlogic.gdx.math.Rectangle teleportListBounds = null;
    private com.badlogic.gdx.math.Rectangle teleportSourceMapBounds = null;
    private com.badlogic.gdx.math.Rectangle teleportTargetMapBounds = null;
    private final Map<Integer, Texture> teleportPreviewTextures = new HashMap<>();
    private final Map<Integer, int[]> teleportPreviewDimensions = new HashMap<>();
    private final Map<Integer, Future<TeleportPreviewData>> teleportPreviewFutures = new HashMap<>();
    private static final float TELEPORT_PREVIEW_MAX_ZOOM = 32f;
    private float teleportSourcePreviewZoom = 1f;
    private float teleportTargetPreviewZoom = 1f;
    private float teleportSourcePreviewCenterX = 0.5f;
    private float teleportSourcePreviewCenterY = 0.5f;
    private float teleportTargetPreviewCenterX = 0.5f;
    private float teleportTargetPreviewCenterY = 0.5f;
    private boolean musicZoneSelecting = false;
    private RectangleSelection pendingMusicZoneSelection = null;
    private int musicBrushRadius = 2;
    private float decorNudgeHoldTime = 0f;
    private float decorNudgeAccumulator = 0f;
    private int decorNudgeLastDx = 0;
    private int decorNudgeLastDy = 0;
    private boolean collisionDirty = false;
    private float lastSavedCameraX = Float.NaN;
    private float lastSavedCameraY = Float.NaN;
    private float lastSavedCameraZoom = Float.NaN;
    private String lastSavedCameraMapPath = null;

    // Editor chrome
    private static final int TOOLBAR_HEIGHT = 65;
    private static final int MENU_BAR_HEIGHT = 30;
    private static final int BOTTOM_INFO_HEIGHT = 38;
    private static final int BUTTON_SIZE = 50;
    private static final float PANEL_MARGIN = 12f;
    private static final float CONTEXT_BAR_HEIGHT = 34f;
    private static final Color UI_APP_BG = new Color(0.125f, 0.145f, 0.165f, 0.98f);
    private static final Color UI_SURFACE = new Color(0.925f, 0.94f, 0.955f, 0.98f);
    private static final Color UI_SURFACE_HOVER = new Color(0.82f, 0.88f, 0.94f, 0.98f);
    private static final Color UI_SURFACE_PRESSED = new Color(0.72f, 0.80f, 0.88f, 0.98f);
    private static final Color UI_PANEL_DARK = new Color(0.085f, 0.105f, 0.13f, 0.96f);
    private static final Color UI_PANEL_DARK_2 = new Color(0.13f, 0.155f, 0.185f, 0.96f);
    private static final Color UI_BORDER = new Color(0.35f, 0.40f, 0.47f, 0.90f);
    private static final Color UI_BORDER_ACTIVE = new Color(0.18f, 0.52f, 0.82f, 0.95f);
    private static final Color UI_TEXT = new Color(0.08f, 0.10f, 0.13f, 1f);
    private static final Color UI_TEXT_LIGHT = new Color(0.91f, 0.94f, 0.97f, 1f);
    private static final Color UI_TEXT_MUTED = new Color(0.40f, 0.45f, 0.52f, 1f);
    private static final Color UI_TEXT_FAINT = new Color(0.56f, 0.62f, 0.69f, 1f);
    private static final Color UI_BLUE = new Color(0.12f, 0.45f, 0.78f, 1f);
    private final List<ToolbarButton> toolbarButtons = new ArrayList<>();
    private final List<Integer> separatorPositions = new ArrayList<>();
    private final List<MenuTitle> menuTitles = new ArrayList<>();
    private String openMenu = null;
    private String openSubMenu = null;
    private String openNestedSubMenu = null;
    private String hoverInfo = null;
    private String toolbarTooltip = null;
    private boolean monsterPlacementEnabled = false;
    private final List<MonsterTypeEntry> monsterTypes = new ArrayList<>();
    private MonsterTypeEntry selectedMonsterType = null;
    private final List<MonsterSpawnEntry> monsterSpawns = new ArrayList<>();
    private MonsterSpawnEntry selectedMonsterSpawn = null;
    private boolean monsterStaticPlacement = false;
    private com.badlogic.gdx.math.Rectangle monsterStaticBounds = null;
    private boolean npcPlacementEnabled = false;
    private final List<MonsterTypeEntry> npcTypes = new ArrayList<>();
    private MonsterTypeEntry selectedNpcType = null;
    private final List<MonsterSpawnEntry> npcSpawns = new ArrayList<>();
    private MonsterSpawnEntry selectedNpcSpawn = null;
    private boolean npcStaticPlacement = false;
    private com.badlogic.gdx.math.Rectangle npcStaticBounds = null;
    private boolean spawnContextOpen = false;
    private MonsterSpawnEntry spawnContextTarget = null;
    private boolean spawnContextNpc = false;
    private TeleportGotoDialog teleportGotoDialog = null;
    private boolean pendingTeleportAfterMapSwitch = false;
    private int pendingTeleportTileX = 0;
    private int pendingTeleportTileY = 0;
    private com.badlogic.gdx.math.Rectangle spawnContextBounds = null;
    private EditorContextMenu spawnContextMenu = null;
    private float spawnContextX = 0f;
    private float spawnContextY = 0f;
    private boolean decorContextOpen = false;
    private DecorInfo decorContextTarget = null;
    private com.badlogic.gdx.math.Rectangle decorContextBounds = null;
    private EditorContextMenu decorContextMenu = null;
/**
 * Enumeration of EditorMode.
 */

    private enum EditorMode {
        SELECT_TILE, // Tile selection mode
        SPRITE_PICKER, // Sprite selection interface
        COLLISION_EDITOR, // Collision editing mode
        MUSIC_ZONE_EDITOR // Ambiance music zone editing mode
        , TELEPORT_EDITOR // Teleport list and visual editing mode
        , OBJECT_POSITION_EDITOR
    }
/**
 * Class representing CopiedTile.
 */

    private static class CopiedTile {
        String spriteName;
        boolean isMirrored;
        float scaleX;
        float scaleY;
        float offsetX;
        float offsetY;
        int zOrder;

        CopiedTile(String spriteName, boolean isMirrored, float scaleX, float scaleY, float offsetX, float offsetY,
                int zOrder) {
            this.spriteName = spriteName;
            this.isMirrored = isMirrored;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.zOrder = zOrder;
        }
    }
/**
 * Class representing TeleportEntry.
 */

    private static class TeleportEntry implements com.perso.T4C.render.TeleportOverlayRenderer.Entry {
        int id;
        int sourceZ;
        int sourceX;
        int sourceY;
        int targetZ;
        int targetX;
        int targetY;
        public int sourceZ() { return sourceZ; } public int sourceX() { return sourceX; } public int sourceY() { return sourceY; }
        public int targetZ() { return targetZ; } public int targetX() { return targetX; } public int targetY() { return targetY; }

        TeleportEntry(int id, int sourceZ, int sourceX, int sourceY, int targetZ, int targetX, int targetY) {
            this.id = id;
            this.sourceZ = sourceZ;
            this.sourceX = sourceX;
            this.sourceY = sourceY;
            this.targetZ = targetZ;
            this.targetX = targetX;
            this.targetY = targetY;
        }
    }
/**
 * Class representing TeleportJsonRoot.
 */

    private static class TeleportJsonRoot {
        List<TeleportJsonEntry> teleports;
    }
/**
 * Class representing TeleportJsonEntry.
 */

    private static class TeleportJsonEntry {
        int id;
        TeleportJsonPoint source;
        TeleportJsonPoint target;
    }
/**
 * Class representing TeleportJsonPoint.
 */

    private static class TeleportJsonPoint {
        int z;
        int x;
        int y;
    }
/**
 * Class representing SaveStep.
 */

    private static class SaveStep {
        final String label;
        final Runnable action;

        SaveStep(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }
/**
 * Class representing TeleportPreviewData.
 */

    private static class TeleportPreviewData {
        final int z;
        final int width;
        final int height;
        final Pixmap pixmap;

        TeleportPreviewData(int z, int width, int height, Pixmap pixmap) {
            this.z = z;
            this.width = width;
            this.height = height;
            this.pixmap = pixmap;
        }
    }
/**
 * Class representing RectangleSelection.
 */

    private static class RectangleSelection {
        int startX, startY, endX, endY;

        RectangleSelection(int startX, int startY, int endX, int endY) {
            this.startX = Math.min(startX, endX);
            this.startY = Math.min(startY, endY);
            this.endX = Math.max(startX, endX);
            this.endY = Math.max(startY, endY);
        }

        int getWidth() {
            return endX - startX + 1;
        }

        int getHeight() {
            return endY - startY + 1;
        }

        boolean contains(int x, int y) {
            return x >= startX && x <= endX && y >= startY && y <= endY;
        }
    }
/**
 * Class representing DragState.
 */

    private static class DragState {
        String spriteName;
        String originalSpriteName;
        boolean isMirrored;
        int tileX;
        int tileY;
        int targetTileX;
        int targetTileY;
        int startMouseTileX;
        int startMouseTileY;
        int lastPreviewTileX = Integer.MIN_VALUE;
        int lastPreviewTileY = Integer.MIN_VALUE;
        float originalScaleX;
        float originalScaleY;
        int originalZOrder;
        Vector2 startMouseWorld = new Vector2();
        Vector2 currentOffset = new Vector2();
        Vector2 originalOffset = new Vector2();
        // Store the initial click position relative to the sprite render position
        Vector2 initialClickOffset = new Vector2();
    }

    private static class ObjectDragState {
        int index;
        int originalTileX;
        int originalTileY;
        int originalZ;
        int startMouseTileX;
        int startMouseTileY;
        int targetTileX;
        int targetTileY;
        int targetZ;
    }

    private static class GroundPaintState {
        String spriteName;
        float scaleX;
        float scaleY;
        float offsetX;
        float offsetY;
        int zOrder;
        int lastTileX = Integer.MIN_VALUE;
        int lastTileY = Integer.MIN_VALUE;
        UndoEntry undoEntry;
    }
/**
 * Class representing DecorInfo.
 */

    private static class DecorInfo {
        String spriteName;
        boolean isMirrored;
        int tileX;
        int tileY;
        float renderX;
        float renderY;
        int width;
        int height;
        int deep;

        DecorInfo(String spriteName, boolean isMirrored, int tileX, int tileY, float renderX, float renderY, int width,
                int height, int deep) {
            this.spriteName = spriteName;
            this.isMirrored = isMirrored;
            this.tileX = tileX;
            this.tileY = tileY;
            this.renderX = renderX;
            this.renderY = renderY;
            this.width = width;
            this.height = height;
            this.deep = deep;
        }

        boolean contains(float x, float y) {
            return x >= renderX && x <= renderX + width && y >= renderY && y <= renderY + height;
        }
    }
/**
 * Class representing TileChange.
 */

    private static class TileChange {
        final int x;
        final int y;
        final String oldName;
        final float oldScaleX;
        final float oldScaleY;
        final float oldOffsetX;
        final float oldOffsetY;
        final int oldZOrder;
        final String newName;
        final float newScaleX;
        final float newScaleY;
        final float newOffsetX;
        final float newOffsetY;
        final int newZOrder;
        final boolean isDecor;
        // When true the change is confined to the GROUND layer of a tile that also carries
        // a decor: undo must restore it via setGroundSpriteName so the decor is preserved.
        final boolean groundUnderDecor;

        TileChange(int x, int y, String oldName, float oldScaleX, float oldScaleY, float oldOffsetX, float oldOffsetY,
                int oldZOrder, String newName, float newScaleX, float newScaleY, float newOffsetX, float newOffsetY,
                int newZOrder) {
            this(x, y, oldName, oldScaleX, oldScaleY, oldOffsetX, oldOffsetY, oldZOrder,
                    newName, newScaleX, newScaleY, newOffsetX, newOffsetY, newZOrder, false, false);
        }

        TileChange(int x, int y, String oldName, float oldScaleX, float oldScaleY, float oldOffsetX, float oldOffsetY,
                int oldZOrder, String newName, float newScaleX, float newScaleY, float newOffsetX, float newOffsetY,
                int newZOrder, boolean isDecor) {
            this(x, y, oldName, oldScaleX, oldScaleY, oldOffsetX, oldOffsetY, oldZOrder,
                    newName, newScaleX, newScaleY, newOffsetX, newOffsetY, newZOrder, isDecor, false);
        }

        TileChange(int x, int y, String oldName, float oldScaleX, float oldScaleY, float oldOffsetX, float oldOffsetY,
                int oldZOrder, String newName, float newScaleX, float newScaleY, float newOffsetX, float newOffsetY,
                int newZOrder, boolean isDecor, boolean groundUnderDecor) {
            this.x = x;
            this.y = y;
            this.oldName = oldName;
            this.oldScaleX = oldScaleX;
            this.oldScaleY = oldScaleY;
            this.oldOffsetX = oldOffsetX;
            this.oldOffsetY = oldOffsetY;
            this.oldZOrder = oldZOrder;
            this.newName = newName;
            this.newScaleX = newScaleX;
            this.newScaleY = newScaleY;
            this.newOffsetX = newOffsetX;
            this.newOffsetY = newOffsetY;
            this.newZOrder = newZOrder;
            this.isDecor = isDecor;
            this.groundUnderDecor = groundUnderDecor;
        }
    }
/**
 * Class representing UndoEntry.
 */

    private static class UndoEntry {
        final String label;
        final boolean persistOnUndo;
        final List<TileChange> changes = new ArrayList<>();

        UndoEntry(String label, boolean persistOnUndo) {
            this.label = label;
            this.persistOnUndo = persistOnUndo;
        }

        boolean hasChanges() {
            return !changes.isEmpty();
        }
    }

    /**
     * Create a new MapEditorScreen instance.
     * Initializes rendering resources, input handling and loads available maps.
     *
     * @param game reference to the parent game instance used to share resources
     * @throws GameException when initialization of map editor resources fails
     */
    public MapEditorScreen(MyGame game) throws GameException {
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true);

        this.viewport = new ScreenViewport(camera);
        boolean hasSharedBatch = game.batch != null;
        this.batch = hasSharedBatch ? game.batch : new SpriteBatch();
        this.ownsBatch = !hasSharedBatch;
        this.uiBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = createEditorFont(14);

        this.spriteLoader = SpriteLoader.getInstance();
        this.groundMosaicCatalog = GroundMosaicCatalog.load(
                spriteLoader.getSprites().stream().map(SpriteLoader.Sprite::getName).toList());
        this.batchSol = new SpriteBatch();
        this.batchDecor = new SpriteBatch();

        setupInputProcessor();

        initializeLoadingSteps();

    }

    private void buildGroundFillSprites() {
        groundFillSprites.clear();
        groundFillSprites.addAll(List.of(
                "64kDeadForestGroundA",
                "64kNormalGrass",
                "64kNormalGrassBreaker2",
                "64kTuftyGrass",
                "DesertTile",
                "Dgrass",
                "DungeonFloorOverHead2",
                "EarthTile",
                "Floor: Wooden",
                "Grass",
                "GreenGrass",
                "Ground_Water",
                "Hardrock",
                "RockFloor",
                "Rockflor",
                "Tmpl3",
                "Town Road: Dale",
                "Water"));
        for (String base : groundMosaicCatalog.baseNames()) {
            // Exact match: near-identical names (RockFloor and Rockflor) are separate
            // grounds with their own tiles, so an ignore-case dedup would silently drop
            // one of the pair from the list.
            if (!groundFillSprites.contains(base)) {
                groundFillSprites.add(base);
            }
        }
        groundFillSprites.sort(String::compareToIgnoreCase);
        groundFillPatterns.clear();
        groundSelectScrollOffset = 0;
        if (groundFillSprites.isEmpty()) {
            groundFillSprites.add("Ground_Grass");
        }
        if (fillToolSprite == null) {
            fillToolSprite = groundFillSprites.get(0);
        }
    }
/**
 * Class representing MonsterTypeEntry.
 */

    private static class MonsterTypeEntry {
        final String className;
        final String displayName;
        final String thumbnailSprite;
        MonsterTypeEntry(String className, String displayName, String thumbnailSprite) {
            this.className = className;
            this.displayName = displayName;
            this.thumbnailSprite = thumbnailSprite;
        }
    }
/**
 * Class representing MonsterSpawnEntry.
 */

    private static class MonsterSpawnEntry {
        String type;
        int x;
        int y;
        int z;
        boolean stationary;
        boolean aggressive;
        MonsterSpawnEntry(String type, int x, int y, boolean stationary) {
            this(type, x, y, stationary, false);
        }

        MonsterSpawnEntry(String type, int x, int y, boolean stationary, boolean aggressive) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = 0;
            this.stationary = stationary;
            this.aggressive = aggressive;
        }
    }

    private static class NpcPreviewPart {
        final TextureRegion region;
        final float offsetX;
        final float offsetY;

        NpcPreviewPart(TextureRegion region, float offsetX, float offsetY) {
            this.region = region;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    private BitmapFont createEditorFont(int size) {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Paths.FONT_JETBRAINS_MONO));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = size;
            parameter.color = Color.WHITE;
            parameter.flip = false;
            BitmapFont generated = generator.generateFont(parameter);
            generator.dispose();
            generated.setUseIntegerPositions(false);
            generated.getRegion().getTexture().setFilter(
                    com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
                    com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            return generated;
        } catch (Throwable t) {
            log.warn("Failed to load editor font, using default bitmap font", t);
            BitmapFont fallback = new BitmapFont();
            fallback.setUseIntegerPositions(false);
            return fallback;
        }
    }
/**
 * Class representing CollisionRules.
 */

    private static class CollisionRules {
        boolean defaultDecorCollision = false;
        int defaultCollisionValue = COLLISION_VALUE_RED;
        List<String> ignoredSprites = new ArrayList<>();
        Map<String, CollisionRule> exactSprites = new LinkedHashMap<>();
        List<CollisionNameRule> nameContainsRules = new ArrayList<>();

        Map<String, CollisionRule> normalizedExactSprites() {
            Map<String, CollisionRule> normalized = new HashMap<>();
            if (exactSprites == null) {
                return normalized;
            }
            for (Map.Entry<String, CollisionRule> entry : exactSprites.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    normalized.put(normalizeSpriteKey(entry.getKey()), entry.getValue());
                }
            }
            return normalized;
        }

        Set<String> normalizedIgnoredSprites() {
            Set<String> ignored = new HashSet<>();
            if (ignoredSprites == null) {
                return ignored;
            }
            for (String sprite : ignoredSprites) {
                if (sprite != null && !sprite.isBlank() && !sprite.contains("*")) {
                    ignored.add(normalizeSpriteKey(sprite));
                }
            }
            return ignored;
        }

        boolean isIgnoredSprite(String spriteName, Set<String> normalizedExactIgnoredSprites) {
            if (spriteName == null) {
                return false;
            }
            if (normalizedExactIgnoredSprites.contains(spriteName)) {
                return true;
            }
            if (ignoredSprites == null) {
                return false;
            }
            for (String ignoredSprite : ignoredSprites) {
                if (matchesWildcard(ignoredSprite, spriteName)) {
                    return true;
                }
            }
            return false;
        }

        private boolean matchesWildcard(String pattern, String value) {
            if (pattern == null || pattern.isBlank() || !pattern.contains("*")) {
                return false;
            }
            String normalizedPattern = normalizeSpriteKey(pattern);
            StringBuilder regex = new StringBuilder();
            for (int i = 0; i < normalizedPattern.length(); i++) {
                char c = normalizedPattern.charAt(i);
                if (c == '*') {
                    regex.append(".*");
                } else {
                    regex.append(java.util.regex.Pattern.quote(String.valueOf(c)));
                }
            }
            return value.matches(regex.toString());
        }
    }
/**
 * Class representing CollisionNameRule.
 */

    private static class CollisionNameRule {
        List<String> contains = new ArrayList<>();
        CollisionRule rule;

        boolean matches(String spriteName) {
            if (spriteName == null || contains == null || rule == null) {
                return false;
            }
            String normalizedSprite = normalizeSpriteKey(spriteName);
            String compactSprite = compactSpriteKey(normalizedSprite);
            for (String token : contains) {
                if (token == null || token.isBlank()) {
                    continue;
                }
                String normalizedToken = normalizeSpriteKey(token);
                if (normalizedToken.isBlank()) {
                    continue;
                }
                if (normalizedSprite.contains(normalizedToken)
                        || compactSprite.contains(compactSpriteKey(normalizedToken))) {
                    return true;
                }
            }
            return false;
        }
    }
/**
 * Class representing CollisionRule.
 */

    private static class CollisionRule {
        int value = COLLISION_VALUE_RED;
        List<int[]> tiles = new ArrayList<>();
        List<int[]> clearTiles = new ArrayList<>();

        List<int[]> normalizedTiles() {
            if (tiles == null || tiles.isEmpty()) {
                return List.of();
            }
            return tiles;
        }

        List<int[]> normalizedClearTiles() {
            if (clearTiles == null || clearTiles.isEmpty()) {
                return List.of();
            }
            return clearTiles;
        }
    }
/**
 * Class representing MusicZoneEntry.
 */

    private static class MusicZoneEntry {
        String name;
        int x1;
        int y1;
        int x2;
        int y2;
        String music;

        MusicZoneEntry(String name, int x1, int y1, int x2, int y2, String music) {
            this.name = name;
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
            this.music = music;
        }

        boolean contains(int x, int y) {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2;
        }

        int getWidth() {
            return x2 - x1 + 1;
        }

        int getHeight() {
            return y2 - y1 + 1;
        }
    }


    /**
     * Preload sprite regions and prepare a cached list used by the sprite picker
     * UI.
     * This improves responsiveness when opening the sprite picker by avoiding
     * on-demand texture lookups during interaction.
     */
    private void preloadSpriteList() {
        cachedSpriteList = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
            // Do NOT create textures here: creating ~40k textures makes startup/close slow.
            String name = sprite.getName();
            if (name != null && seenNames.add(name.toLowerCase(Locale.ROOT))) {
                cachedSpriteList.add(new SpritePickerUI.SpriteEntry(name, null, sprite.isGround()));
            }
        }
        // Sort by name only once
        cachedSpriteList.sort(Comparator.comparing(e -> e.name));
        log.info("Prepared {} sprites for sprite picker", cachedSpriteList.size());
    }

    /**
     * Prepare internal loading step state used during editor startup.
     * Resets progress counters, thread handles and status flags.
     */
    private void initializeLoadingSteps() {
        isLoading = true;
        loadingMessage = "Preparing editor...";
        loadingProgress = 0f;
        loadingStep = 0;
        loadingStepCount = 9;
        loadingOverlayPresented = false;
        preloadSpriteIndex = 0;
        preloadSprites = null;
        wdaLoadThread = null;
        wdaLoadError = null;
        wdaLoadDone = false;
        wdaLoadProgress = 0f;
        wdaLoadStage = "Starting";
    }

    /**
     * Scan the configured maps directory and populate the list of available
     * map file paths. Files are filtered by common map extensions and sorted.
     */
    private void loadAvailableMaps() {
        try {
            File mapsDir = new File(Paths.MAPS_DIR);
            if (!mapsDir.exists() || !mapsDir.isDirectory()) {
                log.warn("Maps directory not found: {}", mapsDir.getPath());
                return;
            }

            List<String> maps = new ArrayList<>();
            collectMapbinFiles(mapsDir, maps);

            maps.sort(Comparator.comparing(s -> getMapDisplayName(s).toLowerCase(Locale.ROOT)));
            String defaultMap = normalizeMapPath(Paths.MAP);
            if (maps.remove(defaultMap)) {
                maps.add(0, defaultMap);
            }

            availableMaps.clear();
            availableMaps.addAll(maps);

            log.info("Found {} maps in {}", availableMaps.size(), mapsDir.getPath());
        } catch (Exception e) {
            log.error("Failed to load available maps", e);
        }
    }

    private void collectMapbinFiles(File dir, List<String> maps) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectMapbinFiles(file, maps);
            } else if (isSelectableMapbin(file)) {
                maps.add(normalizeMapPath(file.getPath()));
            }
        }
    }

    private boolean isSelectableMapbin(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".mapbin") && !name.contains(".backup-") && !name.contains(".tmp.");
    }

    private String normalizeMapPath(String path) {
        if (path == null) {
            return null;
        }
        return path.replace('\\', '/');
    }

    /**
     * Initialize the editor toolbar UI and its buttons.
     * This sets up button objects and separator positions used by rendering.
     */
    private void initializeToolbar() {
        initializeToolbarButtons();
        log.info("Toolbar initialized");
    }

    /**
     * Create and configure the MapRenderer and supporting resources after
     * the map and sprite data have been loaded.
     *
     * @throws GameException when renderer initialization fails
     */
    private void initRendererFromLoadedData() throws GameException {
        modifSprites = ModifSprites.empty();
        outlineBatch = new SpriteBatch();
        mapRenderer = new MapRenderer(mapReader, spriteLoader, batchSol, batchDecor, outlineShader,
                modifSprites);

        // Apply initial rendering states
        mapRenderer.setDecorVisible(decorVisible);
        mapRenderer.setObjectsVisible(objectsVisible);
        mapRenderer.setGroundOutlineEnabled(groundOutlineEnabled);
        mapRenderer.setDecorUseTileOffsets(true);
        mapRenderer.setDecorOffsetOverrides(decorOffsetOverrides);
        mapRenderer.setDecorOffsetOverrides(decorOffsetOverrides);
    }

    /**
     * Load and compile the outline shader used for drag highlighting.
     * If compilation fails the editor continues without the shader.
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
     * Load a map file from disk into the MapReader.
     * The path may be an absolute file path or a packaged internal path.
     *
     * @param mapPath path to the map to load
     * @throws GameException if loading the map fails
     */
    private void loadMap(String mapPath) throws GameException {
        File mapFile = new File(mapPath);
        String resolvedPath = mapFile.exists()
                ? mapFile.getAbsolutePath()
                : Gdx.files.internal(mapPath).file().getAbsolutePath();
        mapReader = new MapReader(new File(resolvedPath), false);
        minimapDirty = true;
    }

    /**
     * Register a listener with the SpriteLoader to reload renderer state
     * when sprite assets are reloaded (for example during development).
     */
    private void registerReloadListener() {
        SpriteLoader.getInstance().registerReloadListener(() -> {
            try {
                if (mapRenderer != null) {
                    mapRenderer.reload(modifSprites);
                    mapRenderer.setDecorVisible(decorVisible);
                    mapRenderer.setObjectsVisible(objectsVisible);
                    mapRenderer.setGroundOutlineEnabled(groundOutlineEnabled);
                    mapRenderer.setDecorUseTileOffsets(true);
                    mapRenderer.setDecorOffsetOverrides(decorOffsetOverrides);
                }
            } catch (Throwable t) {
                log.warn("Failed to reload map renderer after sprite reload", t);
            }
        });
    }

    /**
     * Configure LibGDX input processing for the editor UI and tools.
     * Handles keyboard shortcuts, mouse picking, dragging and UI interactions.
     */
    private void setupInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (editorMode == EditorMode.SPRITE_PICKER && spritePicker != null) {
                    return spritePicker.handleKeyTyped(character);
                }
                if (entityPicker != null) {
                    return entityPicker.handleKeyTyped(character);
                }
                if (collisionRuleEditor != null) {
                    return collisionRuleEditor.handleKeyTyped(character);
                }
                if (decorLayerRuleEditor != null) {
                    return decorLayerRuleEditor.handleKeyTyped(character);
                }
                if (clanRelationsEditor != null) {
                    return clanRelationsEditor.handleKeyTyped(character);
                }
                if (objectMappingsEditor != null) {
                    return objectMappingsEditor.handleKeyTyped(character);
                }
                if (spellEditor != null) {
                    return spellEditor.handleKeyTyped(character);
                }
                if (monsterDefEditor != null) {
                    return monsterDefEditor.handleKeyTyped(character);
                }
                if (npcDefEditor != null) {
                    return npcDefEditor.handleKeyTyped(character);
                }
                if (itemEditor != null) {
                    return itemEditor.handleKeyTyped(character);
                }
                if (teleportGotoDialog != null) {
                    return teleportGotoDialog.handleKeyTyped(character);
                }
                if (editorMode == EditorMode.OBJECT_POSITION_EDITOR && handleObjectPositionEditorKeyTyped(character)) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                // Gérer le survol des boutons de la barre d'outils
                handleToolbarMouseMove(screenX, screenY);
                long now = TimeUtils.millis();
                int dx = Math.abs(screenX - lastHoverScreenX);
                int dy = Math.abs(screenY - lastHoverScreenY);
                boolean movedEnough = dx >= HOVER_UPDATE_MIN_PIXEL_DELTA || dy >= HOVER_UPDATE_MIN_PIXEL_DELTA;
                if (movedEnough && (now - lastHoverUpdateMs >= HOVER_UPDATE_INTERVAL_MS)) {
                    hoverInfo = null;
                    hoveredObjectPositionIndex = -1;
                    updateHoveredTileCoordinates(screenX, screenY);
                    updateObjectHoverTooltip(screenX, screenY);
                    updateDecorHoverTooltip(screenX, screenY);
                    updateGroundHoverTooltip(screenX, screenY);
                    lastHoverUpdateMs = now;
                    lastHoverScreenX = screenX;
                    lastHoverScreenY = screenY;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (collisionRegenerating || tmpl3Regenerating || groundRecalculating) {
                    return true;
                }
                if (entityPicker != null) {
                    return entityPicker.handleClick(screenX, screenY, button);
                }
                if (collisionRuleEditor != null) {
                    return collisionRuleEditor.handleClick(screenX, screenY, button);
                }
                if (decorLayerRuleEditor != null) {
                    return decorLayerRuleEditor.handleClick(screenX, screenY, button);
                }
                if (clanRelationsEditor != null) {
                    return clanRelationsEditor.handleClick(screenX, screenY, button);
                }
                if (objectMappingsEditor != null) {
                    return objectMappingsEditor.handleClick(screenX, screenY, button);
                }
                if (spellEditor != null) {
                    return spellEditor.handleClick(screenX, screenY, button);
                }
                if (monsterDefEditor != null) {
                    return monsterDefEditor.handleClick(screenX, screenY, button);
                }
                if (npcDefEditor != null) {
                    return npcDefEditor.handleClick(screenX, screenY, button);
                }
                if (itemEditor != null) {
                    return itemEditor.handleClick(screenX, screenY, button);
                }
                if (editorMode == EditorMode.SPRITE_PICKER && spritePicker != null) {
                    return spritePicker.handleClick(screenX, screenY, button);
                }
                if (teleportGotoDialog != null) {
                    return teleportGotoDialog.handleClick(screenX, screenY, button);
                }
                if (handleMenuClick(screenX, screenY)) {
                    return true;
                }
                if (handleGroundSelectClick(screenX, screenY)) {
                    return true;
                }
                if (editorMode == EditorMode.TELEPORT_EDITOR && handleTeleportEditorClick(screenX, screenY, button)) {
                    return true;
                }
                if (editorMode == EditorMode.OBJECT_POSITION_EDITOR && handleObjectPositionEditorClick(screenX, screenY, button)) {
                    return true;
                }
                if (handleTeleportContextClick(screenX, screenY, button)) {
                    return true;
                }
                if (handleSpawnContextClick(screenX, screenY, button)) {
                    return true;
                }
                if (handleObjectContextClick(screenX, screenY, button)) {
                    return true;
                }
                if (handleDecorContextClick(screenX, screenY, button)) {
                    return true;
                }
                // Vérifier si le clic est sur la barre d'outils
                if (handleToolbarClick(screenX, screenY)) {
                    return true;
                }
                if (handleMinimapClick(screenX, screenY, button)) {
                    return true;
                }

                if (button == Input.Buttons.LEFT) {
                    copyClickedTileInfoToClipboard(screenX, screenY);
                }

                // Collision Editor Mode
                if (editorMode == EditorMode.COLLISION_EDITOR) {
                    if (button == Input.Buttons.LEFT) {
                        paintCollisionAtMouse();
                        return true;
                    } else if (button == Input.Buttons.RIGHT) {
                        eraseCollisionAtMouse();
                        return true;
                    } else if (button == Input.Buttons.MIDDLE) {
                        pickCollisionAtMouse();
                        return true;
                    }
                    return false;
                }

                if (editorMode == EditorMode.MUSIC_ZONE_EDITOR) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    if (!isTileInMap(tileX, tileY)) {
                        return false;
                    }
                    if (button == Input.Buttons.LEFT) {
                        selectedMusicZone = null;
                        pendingMusicZoneSelection = null;
                        paintMusicBrush(tileX, tileY, getSelectedAmbientMusic());
                        return true;
                    }
                    if (button == Input.Buttons.RIGHT) {
                        eraseMusicBrush(tileX, tileY);
                        return true;
                    }
                    return false;
                }

                Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                if (mapReader != null) {
                    int clickedX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int clickedY = (int) (worldCoords.y / GameConstants.GRID_H);
                    if (clickedX >= 0 && clickedY >= 0
                            && clickedX < mapReader.getWidth()
                            && clickedY < mapReader.getHeight()) {
                        lastClickedTileX = clickedX;
                        lastClickedTileY = clickedY;
                    }
                }

                if (teleportOverlayVisible && handleTeleportOverlayMapClick(screenX, screenY, worldCoords, button)) {
                    return true;
                }

                if (button == Input.Buttons.LEFT) {
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    int clickedObjectIndex = findObjectAtWorld(worldCoords.x, worldCoords.y);
                    if (clickedObjectIndex >= 0) {
                        startDraggingObject(clickedObjectIndex, worldCoords.x, worldCoords.y);
                        return true;
                    }
                    if (selectedObjectPositionIndex >= 0) {
                        selectedObjectPositionIndex = -1;
                    }
                    if (isTileInMap(tileX, tileY) && handleSpawnMapClick(tileX, tileY)) {
                        return true;
                    }
                    if (npcPlacementEnabled) {
                        if (tileX >= 0 && tileY >= 0 && tileX < mapReader.getWidth() && tileY < mapReader.getHeight()) {
                            MonsterSpawnEntry clicked = findSpawnAt(npcSpawns, tileX, tileY);
                            if (clicked != null) {
                                selectedNpcSpawn = clicked;
                                npcStaticPlacement = clicked.stationary;
                                showEditorMessage("NPC selected: (" + tileX + ", " + tileY + ")");
                                return true;
                            }
                            if (selectedNpcSpawn != null) {
                                selectedNpcSpawn.x = tileX;
                                selectedNpcSpawn.y = tileY;
                                npcsDirty = true;
                                showEditorMessage("NPC moved: (" + tileX + ", " + tileY + ")");
                                return true;
                            }
                        }
                        if (selectedNpcType != null && tileX >= 0 && tileY >= 0 && tileX < mapReader.getWidth() && tileY < mapReader.getHeight()) {
                            npcSpawns.add(new MonsterSpawnEntry(selectedNpcType.className, tileX, tileY, false));
                            npcsDirty = true;
                            showEditorMessage("NPC placed: " + selectedNpcType.displayName + " (" + tileX + ", " + tileY + ")");
                            return true;
                        }
                    }
                    if (monsterPlacementEnabled) {
                        if (tileX >= 0 && tileY >= 0 && tileX < mapReader.getWidth() && tileY < mapReader.getHeight()) {
                            MonsterSpawnEntry clicked = findSpawnAt(monsterSpawns, tileX, tileY);
                            if (clicked != null) {
                                selectedMonsterSpawn = clicked;
                                monsterStaticPlacement = clicked.stationary;
                                showEditorMessage("Monster selected: (" + tileX + ", " + tileY + ")");
                                return true;
                            }
                            if (selectedMonsterSpawn != null) {
                                selectedMonsterSpawn.x = tileX;
                                selectedMonsterSpawn.y = tileY;
                                monstersDirty = true;
                                showEditorMessage("Monster moved: (" + tileX + ", " + tileY + ")");
                                return true;
                            }
                        }
                        if (selectedMonsterType != null && tileX >= 0 && tileY >= 0 && tileX < mapReader.getWidth() && tileY < mapReader.getHeight()) {
                            monsterSpawns.add(new MonsterSpawnEntry(selectedMonsterType.className, tileX, tileY, false, true));
                            monstersDirty = true;
                            showEditorMessage("Monster placed: " + selectedMonsterType.displayName + " (" + tileX + ", " + tileY + ")");
                            return true;
                        }
                    }
                    if (scaleToolEnabled) {
                        if (tileX >= 0 && tileY >= 0 && tileX < mapReader.getWidth() && tileY < mapReader.getHeight()) {
                            String currentName = mapReader.getSpriteName(tileX, tileY);
                            if (currentName != null && !currentName.isBlank()) {
                                selectedTileX = tileX;
                                selectedTileY = tileY;
                                showEditorMessage("Scale target: (" + tileX + ", " + tileY + ")");
                                return true;
                            }
                        }
                    }
                    if (autofillEnabled && (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
                            || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))) {
                        if (tileX >= 0 && tileY >= 0 && tileX < mapReader.getWidth()
                                && tileY < mapReader.getHeight()) {
                            autofillRectSelecting = true;
                            isSelecting = true;
                            selectionStart.set(tileX, tileY);
                            lassoSelectionTiles.clear();
                            lassoSelectionPath.clear();
                            lastLassoTileX = Integer.MIN_VALUE;
                            lastLassoTileY = Integer.MIN_VALUE;
                            rectangleSelection = new RectangleSelection(tileX, tileY, tileX, tileY);
                            return true;
                        }
                    }
                    if (fillToolEnabled) {
                        selectedTileX = tileX;
                        selectedTileY = tileY;
                        if (autofillEnabled) {
                            // perform autofill; do not persist by default unless autofillPersist is true
                            performBaseFloodFill(tileX, tileY, fillToolSprite, autofillPersist);
                        } else {
                            showEditorMessage("Autofill disabled");
                        }
                        return true;
                    }
                    // Check if Shift is pressed for rectangle selection
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                        if (!isSelecting) {
                            isSelecting = true;
                            selectionStart.set(tileX, tileY);
                            lassoSelectionTiles.clear();
                            lassoSelectionPath.clear();
                            addTileToLassoSelectionPath(tileX, tileY);
                        }
                        return true;
                    }

                    // Try to select a decor/object by clicking on its visible area
                    DecorInfo clickedDecor = decorVisible ? findDecorAtPosition(worldCoords.x, worldCoords.y) : null;

                    if (clickedDecor != null) {
                        commitPendingOffsetsIfDifferent(clickedDecor.spriteName);
                        startDraggingDecor(clickedDecor, worldCoords.x, worldCoords.y);
                        selectedDecorInfo = clickedDecor;
                    } else {
                        // No decor clicked, open sprite picker for the tile
                        commitPendingOffsetsIfDifferent(null);

                        selectedTileX = tileX;
                        selectedTileY = tileY;
                        selectedDecorInfo = null;
                        startGroundPaint(tileX, tileY);
                    }
                    return true;
                } else if (button == Input.Buttons.RIGHT) {
                    if (dragState != null) {
                        // Cancel drag
                        cancelDragDecor();
                    } else {
                        int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                        int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                        if (isTileInMap(tileX, tileY) && openSpawnContextMenu(tileX, tileY, screenX, screenY)) {
                            return true;
                        }
                        int clickedObjectIndex = findObjectAtWorld(worldCoords.x, worldCoords.y);
                        if (clickedObjectIndex >= 0) {
                            openObjectContextMenu(clickedObjectIndex, screenX, screenY);
                            return true;
                        }
                        DecorInfo clickedDecor = decorVisible ? findDecorAtPosition(worldCoords.x, worldCoords.y) : null;
                        if (clickedDecor != null) {
                            openDecorContextMenu(clickedDecor, screenX, screenY);
                        } else {
                            decorContextOpen = false;
                            if (isTileInMap(tileX, tileY)) {
                                selectedTileX = tileX;
                                selectedTileY = tileY;
                                selectedDecorInfo = null;
                                openSpritePicker();
                            }
                        }
                    }
                    return true;
                } else if (button == Input.Buttons.MIDDLE) {
                    DecorInfo clickedDecor = decorVisible ? findDecorAtPosition(worldCoords.x, worldCoords.y) : null;
                    DecorInfo targetDecor = clickedDecor != null ? clickedDecor : (decorVisible ? selectedDecorInfo : null);
                    if (targetDecor != null) {
                        snapClickedPixelToSelectedTile(targetDecor, worldCoords.x, worldCoords.y);
                        return true;
                    }
                    // Middle click on a ground tile: flood-fill the contiguous ground
                    // region under the cursor with the current fill tool sprite.
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    if (mapReader != null && isTileInMap(tileX, tileY)) {
                        if (fillToolSprite == null || fillToolSprite.isBlank()) {
                            showEditorMessage("No fill tile selected");
                            return true;
                        }
                        selectedTileX = tileX;
                        selectedTileY = tileY;
                        performBaseFloodFill(tileX, tileY, fillToolSprite, autofillPersist);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (collisionRegenerating || tmpl3Regenerating || groundRecalculating) {
                    return true;
                }
                if (collisionRuleEditor != null) {
                    return collisionRuleEditor.handleTouchUp(screenX, screenY, button);
                }
                if (draggedTeleport != null && button == Input.Buttons.LEFT) {
                    finishDraggingTeleport();
                    return true;
                }
                if (objectDragState != null && button == Input.Buttons.LEFT) {
                    finishDraggingObject();
                    return true;
                }
                if (groundPaintState != null && button == Input.Buttons.LEFT) {
                    finishGroundPaint();
                    return true;
                }
                if (dragState != null && button == Input.Buttons.LEFT) {
                    saveDraggedDecorOffset(screenX, screenY);
                    dragState = null;
                    return true;
                }

                if (autofillRectSelecting && button == Input.Buttons.LEFT) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    rectangleSelection = new RectangleSelection((int) selectionStart.x, (int) selectionStart.y, tileX,
                            tileY);
                    applyAutofillRectangle(rectangleSelection, fillToolSprite, autofillPersist);
                    rectangleSelection = null;
                    autofillRectSelecting = false;
                    isSelecting = false;
                    return true;
                }

                if (isSelecting && button == Input.Buttons.LEFT) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    addTileToLassoSelectionPath(tileX, tileY);
                    fillClosedLassoSelection();
                    rectangleSelection = buildSelectionBoundsFromLasso();
                    isSelecting = false;
                    int selectedCount = lassoSelectionTiles.isEmpty()
                            ? rectangleSelection.getWidth() * rectangleSelection.getHeight()
                            : lassoSelectionTiles.size();
                    showEditorMessage("Selected " + selectedCount + " tile" + (selectedCount == 1 ? "" : "s"));
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (collisionRegenerating || tmpl3Regenerating || groundRecalculating) {
                    return true;
                }
                if (collisionRuleEditor != null) {
                    return collisionRuleEditor.handleDrag(screenX, screenY);
                }
                if (draggedTeleport != null && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    if (isTileInMap(tileX, tileY)) {
                        draggedTeleportTargetX = tileX;
                        draggedTeleportTargetY = tileY;
                    }
                    return true;
                }
                if (objectDragState != null && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int mouseTileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int mouseTileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    int targetTileX = objectDragState.originalTileX + mouseTileX - objectDragState.startMouseTileX;
                    int targetTileY = objectDragState.originalTileY + mouseTileY - objectDragState.startMouseTileY;
                    if (mapReader != null) {
                        targetTileX = Math.max(0, Math.min(targetTileX, mapReader.getWidth() - 1));
                        targetTileY = Math.max(0, Math.min(targetTileY, mapReader.getHeight() - 1));
                    }
                    objectDragState.targetTileX = targetTileX;
                    objectDragState.targetTileY = targetTileY;
                    objectDragState.targetZ = getCurrentMapZ();
                    updateDraggedObjectPreview();
                    return true;
                }
                if (groundPaintState != null && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    paintGroundAt(tileX, tileY);
                    return true;
                }
                // Collision Editor Mode - paint while dragging
                if (editorMode == EditorMode.COLLISION_EDITOR && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    paintCollisionAtMouse();
                    return true;
                }
                if (editorMode == EditorMode.MUSIC_ZONE_EDITOR) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    if (!isTileInMap(tileX, tileY)) {
                        return false;
                    }
                    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                        paintMusicBrush(tileX, tileY, getSelectedAmbientMusic());
                        return true;
                    }
                    if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                        eraseMusicBrush(tileX, tileY);
                        return true;
                    }
                }

                if (autofillRectSelecting) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    rectangleSelection = new RectangleSelection((int) selectionStart.x, (int) selectionStart.y, tileX,
                            tileY);
                    return true;
                }

                if (isSelecting) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    addTileToLassoSelectionPath(tileX, tileY);
                    rectangleSelection = buildSelectionBoundsFromLasso();
                    return true;
                }

                if (dragState != null) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int mouseTileX = (int) (worldCoords.x / GameConstants.GRID_W);
                    int mouseTileY = (int) (worldCoords.y / GameConstants.GRID_H);
                    int targetTileX = dragState.tileX + mouseTileX - dragState.startMouseTileX;
                    int targetTileY = dragState.tileY + mouseTileY - dragState.startMouseTileY;
                    if (mapReader != null) {
                        targetTileX = Math.max(0, Math.min(targetTileX, mapReader.getWidth() - 1));
                        targetTileY = Math.max(0, Math.min(targetTileY, mapReader.getHeight() - 1));
                    }
                    if (dragState.lastPreviewTileX != targetTileX || dragState.lastPreviewTileY != targetTileY) {
                        if (dragState.lastPreviewTileX != Integer.MIN_VALUE && dragState.lastPreviewTileY != Integer.MIN_VALUE) {
                            invalidateGroundAround(dragState.lastPreviewTileX, dragState.lastPreviewTileY, dragState.originalSpriteName);
                        }
                        invalidateGroundAround(targetTileX, targetTileY, dragState.originalSpriteName);
                        dragState.lastPreviewTileX = targetTileX;
                        dragState.lastPreviewTileY = targetTileY;
                    }
                    dragState.targetTileX = targetTileX;
                    dragState.targetTileY = targetTileY;
                    updateDraggedDecorPreview();
                    return true;
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (collisionRegenerating || tmpl3Regenerating || groundRecalculating) {
                    return true;
                }
                if (editorMode == EditorMode.SPRITE_PICKER && spritePicker != null) {
                    return spritePicker.handleScroll(amountY);
                }
                if (entityPicker != null) {
                    return entityPicker.handleScroll(amountY);
                }
                if (collisionRuleEditor != null) {
                    return collisionRuleEditor.handleScroll(amountY);
                }
                if (decorLayerRuleEditor != null) {
                    return decorLayerRuleEditor.handleScroll(amountY);
                }
                if (clanRelationsEditor != null) {
                    return clanRelationsEditor.handleScroll(amountY);
                }
                if (objectMappingsEditor != null) {
                    return objectMappingsEditor.handleScroll(amountY);
                }
                if (spellEditor != null) {
                    return spellEditor.handleScroll(amountY);
                }
                if (monsterDefEditor != null) {
                    return monsterDefEditor.handleScroll(amountY);
                }
                if (npcDefEditor != null) {
                    return npcDefEditor.handleScroll(amountY);
                }
                if (itemEditor != null) {
                    return itemEditor.handleScroll(amountY);
                }
                if (groundSelectOpen && groundSelectDropdownBounds != null) {
                    int uiY = Gdx.graphics.getHeight() - Gdx.input.getY();
                    if (groundSelectDropdownBounds.contains(Gdx.input.getX(), uiY)) {
                        int maxOffset = Math.max(0, groundFillSprites.size() - groundSelectVisibleRows);
                        groundSelectScrollOffset = Math.max(0, Math.min(maxOffset,
                                groundSelectScrollOffset + (amountY > 0 ? 3 : -3)));
                        return true;
                    }
                }
                if (editorMode == EditorMode.MUSIC_ZONE_EDITOR) {
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                        adjustMusicBrushRadius(amountY > 0 ? -1 : 1);
                    } else {
                        cycleSelectedAmbientMusic(amountY > 0 ? 1 : -1);
                    }
                    return true;
                }
                if (editorMode == EditorMode.TELEPORT_EDITOR) {
                    if (handleTeleportEditorScroll(amountY)) {
                        return true;
                    }
                    int maxOffset = Math.max(0, teleports.size() - 1);
                    teleportScrollOffset = Math.max(0, Math.min(maxOffset, teleportScrollOffset + (amountY > 0 ? 3 : -3)));
                    return true;
                }
                if (editorMode == EditorMode.OBJECT_POSITION_EDITOR) {
                    if (objectPositionTypeDropdownOpen) {
                        objectPositionTypeVisibleRows = objectPositionTypeDropdownBounds == null ? 1
                                : Math.max(1, (int) (objectPositionTypeDropdownBounds.height / 22f));
                        int maxOffset = Math.max(0, objectPositionTypes.size() - objectPositionTypeVisibleRows);
                        objectPositionTypeScrollOffset = Math.max(0, Math.min(maxOffset,
                                objectPositionTypeScrollOffset + (amountY > 0 ? 3 : -3)));
                        return true;
                    }
                    int maxOffset = Math.max(0, objectPositions.size() - objectPositionVisibleRows);
                    objectPositionScrollOffset = Math.max(0, Math.min(maxOffset, objectPositionScrollOffset + (amountY > 0 ? 3 : -3)));
                    return true;
                }

                boolean altPressed = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
                if (altPressed) {
                    if (selectedDecorInfo != null) {
                        selectedTileX = selectedDecorInfo.tileX;
                        selectedTileY = selectedDecorInfo.tileY;
                        if (adjustScaleAtMouse(amountY)) {
                            return true;
                        }
                    } else if (selectedTileX >= 0 && selectedTileY >= 0) {
                        if (adjustScaleAtMouse(amountY)) {
                            return true;
                        }
                    }
                }

                if (scaleToolEnabled) {
                    if (adjustScaleAtMouse(amountY)) {
                        return true;
                    }
                }

                if (isScaleModifierPressed()) {
                    if (adjustScaleAtMouse(amountY)) {
                        return true;
                    }
                }

                Vector3 worldCoords = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
                int tileY = (int) (worldCoords.y / GameConstants.GRID_H);

                if (tileX >= 0 && tileX < mapReader.getWidth() && tileY >= 0 && tileY < mapReader.getHeight()) {
                    ResolvedSprite spriteInfo = resolveSpriteAt(tileX, tileY);
                    if (hasTmplSprite(spriteInfo)) {
                        cycleTmplSprite(tileX, tileY, amountY > 0);
                        refreshHoverInfoAtMouse();
                        return true;
                    }
                }

                zoom += amountY * ZOOM_SPEED;
                zoom = Math.max(0.25f, Math.min(zoom, 4f));
                return true;
            }

    @Override
    public boolean keyDown(int keycode) {
        if (collisionRegenerating || tmpl3Regenerating || groundRecalculating) {
            return true;
        }
        if (teleportGotoDialog != null) {
            return teleportGotoDialog.handleKeyDown(keycode);
        }
        if (clanRelationsEditor != null) {
            return clanRelationsEditor.handleKeyDown(keycode);
        }
        if (objectMappingsEditor != null) {
            return objectMappingsEditor.handleKeyDown(keycode);
        }
        if (spellEditor != null) {
            return spellEditor.handleKeyDown(keycode);
        }
        if (monsterDefEditor != null) {
            return monsterDefEditor.handleKeyDown(keycode);
        }
        if (npcDefEditor != null) {
            return npcDefEditor.handleKeyDown(keycode);
        }
        if (itemEditor != null) {
            return itemEditor.handleKeyDown(keycode);
        }
        if (keycode == Input.Keys.F11) {
            boolean fullscreen = DisplayModeToggle.toggle();
            showEditorMessage(DisplayModeToggle.message(fullscreen));
            return true;
        }
        if (keycode == Input.Keys.F8) {
            openMonsterPicker();
            return true;
        }
        if (keycode == Input.Keys.F9) {
            openNpcPicker();
            return true;
        }
        if (editorMode == EditorMode.COLLISION_EDITOR) {
            int collisionValue = switch (keycode) {
                case Input.Keys.NUM_0, Input.Keys.NUMPAD_0 -> 0;
                case Input.Keys.NUM_1, Input.Keys.NUMPAD_1 -> 1;
                case Input.Keys.NUM_2, Input.Keys.NUMPAD_2 -> 2;
                case Input.Keys.NUM_3, Input.Keys.NUMPAD_3 -> 3;
                case Input.Keys.NUM_4, Input.Keys.NUMPAD_4 -> 4;
                case Input.Keys.NUM_5, Input.Keys.NUMPAD_5 -> 5;
                case Input.Keys.NUM_6, Input.Keys.NUMPAD_6 -> 6;
                case Input.Keys.NUM_7, Input.Keys.NUMPAD_7 -> 7;
                case Input.Keys.NUM_8, Input.Keys.NUMPAD_8 -> 8;
                case Input.Keys.NUM_9, Input.Keys.NUMPAD_9 -> 9;
                default -> -1;
            };
            if (collisionValue >= 0) {
                selectCollisionValue(collisionValue);
                return true;
            }
            if (keycode == Input.Keys.LEFT_BRACKET) {
                selectCollisionValue((selectedCollisionValue + 15) & 0x0F);
                return true;
            }
            if (keycode == Input.Keys.RIGHT_BRACKET) {
                selectCollisionValue((selectedCollisionValue + 1) & 0x0F);
                return true;
            }
        }
        // ESC: close picker / cancel drag / clear selection
        if (keycode == Input.Keys.ESCAPE) {
            if (entityPicker != null) {
                entityPicker = null;
                openMenu = null;
            } else if (collisionRuleEditor != null) {
                collisionRuleEditor.close();
                openMenu = null;
            } else if (decorLayerRuleEditor != null) {
                decorLayerRuleEditor.close();
                openMenu = null;
            } else if (spellEditor != null) {
                spellEditor.close();
                openMenu = null;
            } else if (monsterDefEditor != null) {
                monsterDefEditor.close();
                openMenu = null;
            } else if (npcDefEditor != null) {
                npcDefEditor.close();
                openMenu = null;
            } else if (itemEditor != null) {
                itemEditor.close();
                openMenu = null;
            } else if (editorMode == EditorMode.SPRITE_PICKER) {
                closeSpritePicker();
            } else if (editorMode == EditorMode.TELEPORT_EDITOR) {
                if (teleportsDirty) {
                    saveTeleports();
                }
                editorMode = EditorMode.SELECT_TILE;
                selectedTeleport = null;
                showEditorMessage("Teleport editor closed");
            } else if (editorMode == EditorMode.OBJECT_POSITION_EDITOR) {
                if (objectPositionsDirty) {
                    saveObjectPositions();
                }
                editorMode = EditorMode.SELECT_TILE;
                selectedObjectPositionIndex = -1;
                showEditorMessage("Object editor closed");
            } else if (dragState != null) {
                cancelDragDecor();
            } else if (autofillEnabled) {
                autofillEnabled = false;
                fillToolEnabled = false;
                groundSelectOpen = false;
                autofillRectSelecting = false;
                isSelecting = false;
                rectangleSelection = null;
                lassoSelectionTiles.clear();
                lassoSelectionPath.clear();
                lastLassoTileX = Integer.MIN_VALUE;
                lastLassoTileY = Integer.MIN_VALUE;
                showInfoAndLog("Autofill: OFF", "Autofill toggled: {}", false);
            } else if (musicZoneSelecting) {
                musicZoneSelecting = false;
                isSelecting = false;
                rectangleSelection = null;
                lassoSelectionTiles.clear();
                lassoSelectionPath.clear();
                lastLassoTileX = Integer.MIN_VALUE;
                lastLassoTileY = Integer.MIN_VALUE;
            } else if (pendingMusicZoneSelection != null) {
                pendingMusicZoneSelection = null;
                rectangleSelection = null;
                lassoSelectionTiles.clear();
                lassoSelectionPath.clear();
                lastLassoTileX = Integer.MIN_VALUE;
                lastLassoTileY = Integer.MIN_VALUE;
                showEditorMessage("Music zone cancelled");
            } else if (rectangleSelection != null) {
                rectangleSelection = null;
                isSelecting = false;
                lassoSelectionTiles.clear();
                lassoSelectionPath.clear();
                lastLassoTileX = Integer.MIN_VALUE;
                lastLassoTileY = Integer.MIN_VALUE;
            } else if (selectedMusicZone != null) {
                selectedMusicZone = null;
            } else if (selectedMonsterSpawn != null || selectedNpcSpawn != null) {
                selectedMonsterSpawn = null;
                selectedNpcSpawn = null;
                showEditorMessage("Spawn selection cleared");
            } else if (selectedDecorInfo != null) {
                commitPendingOffsets();
                selectedDecorInfo = null;
            }
            return true;
        }

                if (keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                    if (objectMappingsEditor != null) {
                        return objectMappingsEditor.handleKeyDown(keycode);
                    }
                    if (editorMode == EditorMode.OBJECT_POSITION_EDITOR && selectedObjectPositionIndex >= 0
                            && selectedObjectPositionIndex < objectPositions.size()) {
                        objectPositions.remove(selectedObjectPositionIndex);
                        selectedObjectPositionIndex = -1;
                        objectPositionsDirty = true;
                        showEditorMessage("Object deleted");
                        return true;
                    }
                    if (selectedTeleport != null && (teleportOverlayVisible || editorMode == EditorMode.TELEPORT_EDITOR)) {
                        deleteTeleport(selectedTeleport);
                        return true;
                    }
                    if (selectedMusicZone != null) {
                        musicZones.remove(selectedMusicZone);
                        selectedMusicZone = null;
                        musicZonesDirty = true;
                        musicTileIndexDirty = true;
                        showEditorMessage("Music zone deleted");
                        return true;
                    }
                    if (selectedMonsterSpawn != null) {
                        monsterSpawns.remove(selectedMonsterSpawn);
                        selectedMonsterSpawn = null;
                        monstersDirty = true;
                        showEditorMessage("Monster spawn deleted");
                        return true;
                    }
                    if (selectedNpcSpawn != null) {
                        npcSpawns.remove(selectedNpcSpawn);
                        selectedNpcSpawn = null;
                        npcsDirty = true;
                        showEditorMessage("NPC spawn deleted");
                        return true;
                    }
                    if (deleteSelectedDecorsFromSelection()) {
                        return true;
                    }
                    if (selectedDecorInfo != null) {
                        commitPendingOffsets();
                        String replacement = inferGroundFromNeighbors(selectedDecorInfo.tileX, selectedDecorInfo.tileY);
                        UndoEntry undoEntry = newUndoEntry("Delete decor", false);
                        applyTileChangeWithOffsetAndOrder(selectedDecorInfo.tileX, selectedDecorInfo.tileY, replacement,
                                1f, 1f, 0f, 0f, 0, undoEntry);
                        pushUndoEntry(undoEntry);
                        invalidateGroundAround(selectedDecorInfo.tileX, selectedDecorInfo.tileY,
                                replacement != null ? replacement : selectedDecorInfo.spriteName);
                        showEditorMessage("Decor deleted: " + selectedDecorInfo.spriteName);
                        selectedDecorInfo = null;
                        return true;
                    }
                }

                if (keycode == Input.Keys.M && editorMode == EditorMode.OBJECT_POSITION_EDITOR
                        && selectedObjectPositionIndex >= 0 && selectedObjectPositionIndex < objectPositions.size()) {
                    ObjectPos old = objectPositions.get(selectedObjectPositionIndex);
                    ObjectPos toggled = new ObjectPos(old.name(), old.x(), old.y(), old.z(), !old.mirror());
                    objectPositions.set(selectedObjectPositionIndex, toggled);
                    objectPositionsDirty = true;
                    if (mapRenderer != null) {
                        mapRenderer.setObjectPositionsForEditor(objectPositions);
                    }
                    showEditorMessage("Object mirror: " + (toggled.mirror() ? "ON" : "OFF"));
                    return true;
                }

                if (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN
                        || keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
                    if (selectedDecorInfo != null) {
                        int tileX = selectedDecorInfo.tileX;
                        int tileY = selectedDecorInfo.tileY;
                        if (tileX >= 0 && tileY >= 0 && tileX < mapReader.getWidth() && tileY < mapReader.getHeight()) {
                            String currentName = mapReader.getSpriteName(tileX, tileY);
                            if (currentName != null && !currentName.isBlank()) {
                                boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                                        || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                                if (shift && (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN)) {
                                    int delta = keycode == Input.Keys.UP ? 1 : -1;
                                    int currentZ = mapReader.getZOrder(tileX, tileY);
                                    int newZ = currentZ + delta;
                                    UndoEntry undoEntry = newUndoEntry("Z-Order", false);
                                    applyTileChangeWithOffsetAndOrder(tileX, tileY, currentName,
                                            mapReader.getScaleX(tileX, tileY),
                                            mapReader.getScaleY(tileX, tileY),
                                            mapReader.getOffsetX(tileX, tileY),
                                            mapReader.getOffsetY(tileX, tileY),
                                            newZ,
                                            undoEntry);
                                    pushUndoEntry(undoEntry);
                                    showEditorMessage("Z-Order: " + newZ);
                                    invalidateGroundAround(tileX, tileY, currentName);
                                    return true;
                                }
                            }
                        }
                    }
                }

                // Ctrl+C / Ctrl+V
                if (keycode == Input.Keys.C && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
                    performCopy();
                    return true;
                }
                if (keycode == Input.Keys.V && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
                    performPaste();
                    return true;
                }
                if ((keycode == Input.Keys.Z || keycode == Input.Keys.W) && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
                    performUndo();
                    return true;
                }
                // Shortcuts
                if (keycode == Input.Keys.F2) {
                    toggleDecorVisibility();
                    return true;
                }
                if (keycode == Input.Keys.F3) {
                    toggleObjectsVisibility();
                    return true;
                }
                if (keycode == Input.Keys.F4) {
                    toggleCollisionEditor();
                    return true;
                }

                if (keycode == Input.Keys.F10) {
                    toggleMusicZoneEditor();
                    return true;
                }

                if (keycode == Input.Keys.F5 && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    forceRegenerateTmpl3AtCurrentTile();
                    return true;
                }
                if (keycode == Input.Keys.F5) {
                    toggleTeleportOverlay();
                    return true;
                }

                if (keycode == Input.Keys.F6) {
                    toggleAutofill();
                    return true;
                }
                if (keycode == Input.Keys.F7) {
                    recalculateViewportGroundTextures();
                    return true;
                }
                if (keycode == Input.Keys.F9) {
                    webEditorReloadEnabled = !webEditorReloadEnabled;
                    showEditorMessage("Web editor reload: " + (webEditorReloadEnabled ? "ON" : "OFF"));
                    return true;
                }
                if (keycode == Input.Keys.F12) {
                    repairMissingGroundUnderDecors();
                    return true;
                }

                return false;
            }
        });
    }

    /**
     * Calculate the tile coordinate range currently visible by the camera.
     *
     * @param margin number of extra tiles to include around the viewport
     * @return int[] {startX, endX, startY, endY} clamped to map bounds
     */
    private int[] calculateVisibleBounds(int margin) {
        if (mapReader == null) {
            return new int[] { 0, -1, 0, -1 };
        }
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;

        int startX = Math.max(0, (int) ((camX - viewW / 2) / GameConstants.GRID_W) - margin);
        int endX = Math.min(mapReader.getWidth() - 1, (int) ((camX + viewW / 2) / GameConstants.GRID_W) + margin);
        int startY = Math.max(0, (int) ((camY - viewH / 2) / GameConstants.GRID_H) - margin);
        int endY = Math.min(mapReader.getHeight() - 1, (int) ((camY + viewH / 2) / GameConstants.GRID_H) + margin);

        return new int[] { startX, endX, startY, endY };
    }

    /**
     * Return the top-most decor/object whose rendered bounds contain the
     * given world coordinates. Searches visible tiles for non-ground sprites.
     *
     * @param worldX world X coordinate
     * @param worldY world Y coordinate
     * @return DecorInfo of the hit decor or null if none found
     */
    private DecorInfo findDecorAtPosition(float worldX, float worldY) {
        if (mapReader == null || mapRenderer == null || spriteLoader == null) {
            return null;
        }
        int[] bounds = calculateVisibleBounds(5);
        int startX = bounds[0], endX = bounds[1], startY = bounds[2], endY = bounds[3];

        DecorInfo selectedDecor = null;
        float highestSortKey = Float.NEGATIVE_INFINITY;

        for (int y = endY; y >= startY; y--) {
            for (int x = startX; x <= endX; x++) {
                String decorName = mapReader.getDecorSpriteName(x, y);
                var resolved = SpriteNameParser.parse(decorName,
                        mapRenderer != null ? mapRenderer.getMetaByName() : null);

                if (resolved != null && resolved.name != null) {
                    var meta = mapRenderer.getMetaByName().get(resolved.name.toLowerCase(Locale.ROOT));
                    if (meta == null)
                        continue;

                    // Get sprite region
                    TextureRegion region = spriteLoader.getRegionFromSpriteName(resolved.name);
                    if (region == null)
                        continue;

                    // Calculate render position with offsets
                    ModifSprites.Offset off = getTileOffset(x, y, resolved.mirror);

                    float[] drawOffsets = getSpriteDrawOffsets(resolved.name, resolved.mirror);
                    float offX = drawOffsets[0] + off.x;
                    float offY = drawOffsets[1] + off.y;

                    float scaleX = mapReader.getScaleX(x, y);
                    float scaleY = mapReader.getScaleY(x, y);
                    int w = Math.round(region.getRegionWidth() * scaleX);
                    int h = Math.round(region.getRegionHeight() * scaleY);

                    float renderX = x * GameConstants.GRID_W + offX;
                    float renderY = y * GameConstants.GRID_H + offY;

                    int deep = mapReader.getZOrder(x, y);
                    DecorInfo info = new DecorInfo(resolved.name, resolved.mirror, x, y, renderX, renderY, w, h, deep);

                    // Check if click is within this decor's bounds
                    if (info.contains(worldX, worldY)) {
                        float sortKey = y + deep * 10000f;
                        if (sortKey > highestSortKey) {
                            highestSortKey = sortKey;
                            selectedDecor = info;
                        }
                    }
                }
            }
        }

        return selectedDecor;
    }

    /**
     * Initialize drag state for the specified decor so it can be moved with
     * the mouse. Stores original offsets and initial click delta.
     *
     * @param decorInfo   decor information to start dragging
     * @param mouseWorldX mouse X in world coordinates at drag start
     * @param mouseWorldY mouse Y in world coordinates at drag start
     */
    private void startDraggingDecor(DecorInfo decorInfo, float mouseWorldX, float mouseWorldY) {
        if (decorInfo == null)
            return;

        var meta = mapRenderer.getMetaByName().get(decorInfo.spriteName.toLowerCase(Locale.ROOT));
        if (meta != null && mapReader.getDecorSpriteName(decorInfo.tileX, decorInfo.tileY) != null) {
            // Explicit decor-layer placement wins over intrinsic sprite metadata.
            dragState = new DragState();
            dragState.spriteName = decorInfo.spriteName;
            dragState.isMirrored = decorInfo.isMirrored;
            dragState.tileX = decorInfo.tileX;
            dragState.tileY = decorInfo.tileY;
            dragState.targetTileX = decorInfo.tileX;
            dragState.targetTileY = decorInfo.tileY;
            dragState.startMouseTileX = (int) (mouseWorldX / GameConstants.GRID_W);
            dragState.startMouseTileY = (int) (mouseWorldY / GameConstants.GRID_H);
            dragState.lastPreviewTileX = decorInfo.tileX;
            dragState.lastPreviewTileY = decorInfo.tileY;
            dragState.startMouseWorld.set(mouseWorldX, mouseWorldY);
            dragState.originalSpriteName = mapReader.getSpriteName(decorInfo.tileX, decorInfo.tileY);
            dragState.originalScaleX = mapReader.getScaleX(decorInfo.tileX, decorInfo.tileY);
            dragState.originalScaleY = mapReader.getScaleY(decorInfo.tileX, decorInfo.tileY);
            dragState.originalZOrder = mapReader.getZOrder(decorInfo.tileX, decorInfo.tileY);

            float offsetX = mapReader.getOffsetX(decorInfo.tileX, decorInfo.tileY);
            float offsetY = mapReader.getOffsetY(decorInfo.tileX, decorInfo.tileY);
            dragState.originalOffset.set(offsetX, offsetY);
            dragState.currentOffset.set(0f, 0f);
            dragState.initialClickOffset.set(0f, 0f);

            // Hide the original decor while dragging, preserving the ground layer.
            mapReader.setDecorSpriteName(decorInfo.tileX, decorInfo.tileY, null);
            invalidateGroundAround(decorInfo.tileX, decorInfo.tileY, dragState.originalSpriteName);

            log.info("Started dragging decor: {} at ({}, {})", dragState.spriteName, decorInfo.tileX, decorInfo.tileY);
        }
    }

    private void startGroundPaint(int tileX, int tileY) {
        groundPaintState = null;
        if (mapReader == null || !isTileInMap(tileX, tileY)) {
            return;
        }
        // With decorations hidden, sample the GROUND layer even when a decor sits on top,
        // so the brush picks up the ground tile rather than the decor sprite.
        String spriteName = !decorVisible
                ? mapReader.getGroundSpriteName(tileX, tileY)
                : mapReader.getSpriteName(tileX, tileY);
        if (!isPaintableGroundTile(tileX, tileY, spriteName)) {
            return;
        }
        GroundPaintState state = new GroundPaintState();
        state.spriteName = spriteName;
        state.scaleX = mapReader.getScaleX(tileX, tileY);
        state.scaleY = mapReader.getScaleY(tileX, tileY);
        state.offsetX = mapReader.getOffsetX(tileX, tileY);
        state.offsetY = mapReader.getOffsetY(tileX, tileY);
        state.zOrder = mapReader.getZOrder(tileX, tileY);
        state.lastTileX = tileX;
        state.lastTileY = tileY;
        state.undoEntry = newUndoEntry("Paint ground", false);
        groundPaintState = state;
    }

    private boolean isPaintableGroundTile(int tileX, int tileY, String spriteName) {
        if (spriteName == null || spriteName.isBlank()) {
            return false;
        }
        return isGroundOrTmplSprite(mapReader.getGroundSpriteName(tileX, tileY));
    }

    private boolean isGroundOrTmplSprite(String spriteName) {
        if (spriteName == null || spriteName.isBlank()) {
            return false;
        }
        return startsWithIgnoreCase(spriteName.trim(), "Tmpl") || isGroundSpriteName(spriteName);
    }

    private void paintGroundAt(int tileX, int tileY) {
        if (groundPaintState == null || mapReader == null || !isTileInMap(tileX, tileY)) {
            return;
        }
        if (tileX == groundPaintState.lastTileX && tileY == groundPaintState.lastTileY) {
            return;
        }
        groundPaintState.lastTileX = tileX;
        groundPaintState.lastTileY = tileY;

        // Tmpl brush: preserve the picked template family, then compute its proper
        // transition variant from the surrounding terrains.
        String templateName = tmplFamilyName(groundPaintState.spriteName);
        if (templateName != null) {
            paintTmplAt(tileX, tileY, templateName);
            return;
        }

        // For patterned grounds (grid "Base (col, row)" or numeric "Base N") derive the
        // variant from the tile position so a drag tiles the pattern seamlessly instead of
        // stamping the exact same variant everywhere.
        String newName = patternedGroundNameAt(groundPaintState.spriteName, tileX, tileY);
        float newScaleX = groundPaintState.scaleX;
        float newScaleY = groundPaintState.scaleY;
        float newOffsetX = groundPaintState.offsetX;
        float newOffsetY = groundPaintState.offsetY;
        int newZOrder = groundPaintState.zOrder;

        if (isDecorTile(tileX, tileY)) {
            applyGroundLayerChange(tileX, tileY, newName, groundPaintState.undoEntry);
            invalidateGroundAround(tileX, tileY, newName);
            return;
        }

        applyTileChangeWithOffsetAndOrder(tileX, tileY, newName, newScaleX, newScaleY,
                newOffsetX, newOffsetY, newZOrder, groundPaintState.undoEntry);
        invalidateGroundAround(tileX, tileY, newName);
    }

    /**
     * Tmpl brush, one tile. The variant is not stamped: the proper family mask for this
     * position is computed from the directly neighbouring concrete terrains (same engine
     * as "Force regenerate"), and adjacent tiles of the same Tmpl family are recomputed since
     * painting this tile changes their context. When the position has no valid
     * transition context (fewer than two terrain families around), nothing is painted.
     */
    private void paintTmplAt(int tileX, int tileY, String templateName) {
        Tmpl3Regenerator regenerator = getTmplPaintRegenerator(templateName);
        if (regenerator == null) {
            return;
        }
        List<com.perso.T4C.tmpl3.Tmpl3TileSnapshot> positions = new ArrayList<>();
        positions.add(new com.perso.T4C.tmpl3.Tmpl3TileSnapshot(tileX, tileY));
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = tileX + dx;
                int ny = tileY + dy;
                if (!isTileInMap(nx, ny)) {
                    continue;
                }
                ResolvedSprite ground = SpriteNameParser.parse(mapReader.getGroundSpriteName(nx, ny),
                        mapRenderer != null ? mapRenderer.getMetaByName() : null);
                if (isResolvedTmpl(ground, templateName)) {
                    positions.add(new com.perso.T4C.tmpl3.Tmpl3TileSnapshot(nx, ny));
                }
            }
        }
        Tmpl3RegenerationResult result = regenerator.regenerateTmpl3(mapReader, positions);
        for (Tmpl3Change change : result.changes()) {
            applyComputedTmplChange(change.x(), change.y(), change.newName());
        }
    }

    private void applyComputedTmplChange(int x, int y, String newName) {
        if (Objects.equals(mapReader.getGroundSpriteName(x, y), newName)) {
            return;
        }
        if (isDecorTile(x, y)) {
            applyGroundLayerChange(x, y, newName, groundPaintState.undoEntry);
        } else {
            applyTileChangeWithOffsetAndOrder(x, y, newName, mapReader.getScaleX(x, y),
                    mapReader.getScaleY(x, y), mapReader.getOffsetX(x, y), mapReader.getOffsetY(x, y),
                    mapReader.getZOrder(x, y), groundPaintState.undoEntry);
        }
        invalidateGroundAround(x, y, newName);
    }

    private Tmpl3Regenerator getTmplPaintRegenerator(String templateName) {
        Tmpl3Regenerator cached = tmplPaintRegenerators.get(templateName);
        if (cached != null) {
            return cached;
        }
        try {
            List<Tmpl3Mask> masks = new Tmpl3MaskLoader(spriteLoader, templateName).load();
            if (masks.isEmpty()) {
                showEditorMessage("No " + templateName + " masks found");
                return null;
            }
            Tmpl3Regenerator regenerator = new Tmpl3Regenerator(masks, new TerrainResolver() {
                @Override
                public String resolveTerrainName(int tx, int ty) {
                    // See rebuildAllTmpl3: never borrow terrain through neighbouring Tmpl
                    // tiles, or the expected mask (and thus the chosen variant) is wrong.
                    return resolveDirectConcreteGroundTerrainName(tx, ty);
                }

                @Override
                public String familyName(String terrainName) {
                    return tmpl3TerrainFamilyName(terrainName);
                }

                @Override
                public String extrapolateTerrainName(String terrainName, int targetX, int targetY) {
                    return extrapolateTmpl3TerrainName(terrainName, targetX, targetY);
                }
            }, templateName);
            tmplPaintRegenerators.put(templateName, regenerator);
            return regenerator;
        } catch (Throwable t) {
            log.error("Failed to initialise {} paint regenerator", templateName, t);
            showEditorMessage("Error: failed to load " + templateName + " masks");
            return null;
        }
    }

    private String tmplFamilyName(String spriteName) {
        if (spriteName == null) {
            return null;
        }
        String trimmed = spriteName.trim();
        if (startsWithIgnoreCase(trimmed, "Tmpl1")) {
            return "Tmpl1";
        }
        if (startsWithIgnoreCase(trimmed, "Tmpl3")) {
            return "Tmpl3";
        }
        if (startsWithIgnoreCase(trimmed, "Tmpl4")) {
            return "Tmpl4";
        }
        return null;
    }

    private boolean isAnySmoothingTemplateName(String spriteName) {
        if (spriteName == null) {
            return false;
        }
        String trimmed = spriteName.trim();
        if (!startsWithIgnoreCase(trimmed, "Tmpl")) {
            return false;
        }
        int index = 4;
        if (index >= trimmed.length() || !Character.isDigit(trimmed.charAt(index))) {
            return false;
        }
        while (index < trimmed.length() && Character.isDigit(trimmed.charAt(index))) {
            index++;
        }
        return index == trimmed.length() || Character.isWhitespace(trimmed.charAt(index));
    }

    /**
     * Resolve the sprite name to paint at (tileX, tileY) for a patterned ground brush.
     * If the brush sprite belongs to a grid or numeric ground family, the proper variant
     * for this tile position is computed; otherwise the original name is returned unchanged
     * (Tmpl tiles, single-variant grounds, mirrored names, etc.).
     */
    private String patternedGroundNameAt(String brushName, int tileX, int tileY) {
        if (brushName == null || brushName.isBlank()) {
            return brushName;
        }
        String trimmed = brushName.trim();
        if (startsWithIgnoreCase(trimmed, "Tmpl")) {
            return brushName;
        }
        String base = extractBaseName(trimmed);
        if (base == null || base.isBlank()) {
            return brushName;
        }
        GroundFillPattern pattern = getGroundFillPattern(base);
        if (pattern == null || (!pattern.grid() && !pattern.numeric())) {
            return brushName;
        }
        return buildReplacementName(brushName, base, tileX, tileY, pattern);
    }

    private void applyGroundLayerChange(int tileX, int tileY, String groundName, UndoEntry undoEntry) {
        if (mapReader == null || groundName == null || groundName.isBlank()) {
            return;
        }
        String oldGroundName = mapReader.getGroundSpriteName(tileX, tileY);
        if (Objects.equals(oldGroundName, groundName)) {
            return;
        }
        if (undoEntry != null) {
            undoEntry.changes.add(new TileChange(tileX, tileY, oldGroundName,
                    mapReader.getScaleX(tileX, tileY), mapReader.getScaleY(tileX, tileY),
                    mapReader.getOffsetX(tileX, tileY), mapReader.getOffsetY(tileX, tileY),
                    mapReader.getZOrder(tileX, tileY), groundName,
                    mapReader.getScaleX(tileX, tileY), mapReader.getScaleY(tileX, tileY),
                    mapReader.getOffsetX(tileX, tileY), mapReader.getOffsetY(tileX, tileY),
                    mapReader.getZOrder(tileX, tileY), false, true));
        }
        mapReader.setGroundSpriteName(tileX, tileY, groundName);
        if (mapRenderer != null) {
            mapRenderer.getGroundRenderer().invalidateTileCache(tileX, tileY);
        }
        mapDirty = true;
    }

    private void finishGroundPaint() {
        if (groundPaintState == null) {
            return;
        }
        pushUndoEntry(groundPaintState.undoEntry);
        if (groundPaintState.undoEntry != null && groundPaintState.undoEntry.hasChanges()) {
            showEditorMessage("Painted ground: " + groundPaintState.undoEntry.changes.size() + " tile"
                    + (groundPaintState.undoEntry.changes.size() == 1 ? "" : "s"));
        }
        groundPaintState = null;
    }

    /**
     * Renders an outline around the currently selected decor object using a dedicated shader.
     * The outline helps to highlight the selected object.
     */
    private void renderSelectedDecorOutline() {
        if (selectedDecorInfo == null || mapRenderer == null || spriteLoader == null || outlineBatch == null) {
            return;
        }

        var meta = mapRenderer.getMetaByName().get(selectedDecorInfo.spriteName.toLowerCase(Locale.ROOT));
        if (meta == null) {
            return;
        }

        TextureRegion region = spriteLoader.getRegionFromSpriteName(selectedDecorInfo.spriteName);
        if (region == null) {
            return;
        }

        ModifSprites.Offset off = getTileOffset(selectedDecorInfo.tileX, selectedDecorInfo.tileY,
                selectedDecorInfo.isMirrored);

        float[] drawOffsets = getSpriteDrawOffsets(selectedDecorInfo.spriteName, selectedDecorInfo.isMirrored);
        float offX = drawOffsets[0] + off.x;
        float offY = drawOffsets[1] + off.y;

        float scaleX = mapReader.getScaleX(selectedDecorInfo.tileX, selectedDecorInfo.tileY);
        float scaleY = mapReader.getScaleY(selectedDecorInfo.tileX, selectedDecorInfo.tileY);
        float w = region.getRegionWidth() * scaleX;
        float h = region.getRegionHeight() * scaleY;

        float renderX = selectedDecorInfo.tileX * GameConstants.GRID_W + offX;
        float renderY = selectedDecorInfo.tileY * GameConstants.GRID_H + offY;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        outlineBatch.setProjectionMatrix(camera.combined);
        outlineBatch.setShader(outlineShader);
        outlineBatch.begin();

        outlineShader.setUniformf("u_outlineColor", 0.2f, 0.9f, 1.0f, 1.0f);
        float texelSizeX = 1.0f / region.getTexture().getWidth();
        float texelSizeY = 1.0f / region.getTexture().getHeight();
        outlineShader.setUniformf("u_texelSize", texelSizeX, texelSizeY);

        if (selectedDecorInfo.isMirrored) {
            outlineBatch.draw(region, renderX + w, renderY + h, -w, -h);
        } else {
            outlineBatch.draw(region, renderX, renderY + h, w, -h);
        }

        outlineBatch.end();
        outlineBatch.setShader(null);
    }

    private void renderObjectOutline(int index, float r, float g, float b, float a) {
        if (index < 0 || index >= objectPositions.size() || mapRenderer == null || spriteLoader == null || outlineBatch == null) {
            return;
        }
        ObjectPos pos = objectPositions.get(index);
        if ((int) pos.z() != getCurrentMapZ()) {
            return;
        }
        ObjectMapping mapping = mapRenderer.getObjectMappings().get(pos.name() == null ? "" : pos.name().toUpperCase(Locale.ROOT));
        if (mapping == null || mapping.sprite == null || mapping.sprite.isBlank()) {
            return;
        }
        String[] frames = computeObjectPreviewFrames(mapping.sprite);
        if (frames.length == 0) {
            return;
        }
        TextureRegion region = spriteLoader.getRegionFromSpriteName(frames[0]);
        if (region == null) {
            return;
        }

        boolean mirror = mapping.mirror ^ pos.mirror();
        float[] offsets = getSpriteDrawOffsets(frames[0], mirror);
        ModifSprites.Offset off = mirror ? modifSprites.getOffset(frames[0] + "M")
                : modifSprites.getOffset(frames[0]);
        float w = region.getRegionWidth();
        float h = region.getRegionHeight();
        float renderX = pos.x() * GameConstants.GRID_W + offsets[0] + off.x;
        float renderY = pos.y() * GameConstants.GRID_H + offsets[1] + off.y;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        outlineBatch.setProjectionMatrix(camera.combined);
        outlineBatch.setShader(outlineShader);
        outlineBatch.begin();
        outlineShader.setUniformf("u_outlineColor", r, g, b, a);
        outlineShader.setUniformf("u_texelSize", 1.0f / region.getTexture().getWidth(), 1.0f / region.getTexture().getHeight());
        if (mirror) {
            outlineBatch.draw(region, renderX + w, renderY + h, -w, -h);
        } else {
            outlineBatch.draw(region, renderX, renderY + h, w, -h);
        }
        outlineBatch.end();
        outlineBatch.setShader(null);
    }

    /**
     * Cycle the TMPL variant on a tile to the next or previous available
     * TMPL index. Preserves texture selection from the current tile.
     *
     * @param tileX   tile X coordinate
     * @param tileY   tile Y coordinate
     * @param forward true to advance, false to go backward
     */
    private void cycleTmplSprite(int tileX, int tileY, boolean forward) {
        try {
            ResolvedSprite currentSprite = resolveSpriteAt(tileX, tileY);
            String currentTmpl = getTmplSpriteName(currentSprite);

            if (currentTmpl == null) {
                return;
            }

            // Parse current TMPL: "TmplX Y"
            String[] parts = currentTmpl.split("\\s+");
            if (parts.length < 2)
                return;

            String tmplBase = parts[0]; // "Tmpl1", "Tmpl3", etc.
            int currentIndex = Integer.parseInt(parts[1]);

            // Collect available TMPL indices from sprite list
            List<Integer> indices = new ArrayList<>();
            for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
                String name = sprite.getName();
                if (name == null || !startsWithIgnoreCase(name, tmplBase + " ")) {
                    continue;
                }
                String[] nameParts = name.split("\\s+");
                if (nameParts.length < 2) {
                    continue;
                }
                try {
                    indices.add(Integer.parseInt(nameParts[1]));
                } catch (NumberFormatException ignored) {
                }
            }
            if (indices.isEmpty()) {
                return;
            }
            Collections.sort(indices);
            int pos = indices.indexOf(currentIndex);
            if (pos < 0) {
                pos = 0;
            } else {
                pos = forward ? (pos + 1) % indices.size() : (pos - 1 + indices.size()) % indices.size();
            }
            int nextIndex = indices.get(pos);
            String nextTmpl = tmplBase + " " + nextIndex;

            String rawName = mapReader.getSpriteName(tileX, tileY);
            String newSpriteName = nextTmpl;

            UndoEntry undoEntry = newUndoEntry("TMPL", false);
            if (isDecorTile(tileX, tileY)) {
                applyGroundLayerChange(tileX, tileY, newSpriteName, undoEntry);
            } else {
                applyTileChange(tileX, tileY, newSpriteName, mapReader.getScaleX(tileX, tileY),
                        mapReader.getScaleY(tileX, tileY), undoEntry);
            }
            pushUndoEntry(undoEntry);

            invalidateGroundAround(tileX, tileY, newSpriteName);

            showEditorMessage("TMPL: " + nextTmpl);
            log.info("Cycled TMPL at ({}, {}) to: {}", tileX, tileY, newSpriteName);

        } catch (Exception e) {
            log.error("Failed to cycle TMPL sprite", e);
            showEditorMessage("Error: Failed to cycle TMPL");
        }
    }

    private boolean hasTmplSprite(ResolvedSprite sprite) {
        return getTmplSpriteName(sprite) != null;
    }

    private String getTmplSpriteName(ResolvedSprite sprite) {
        if (sprite == null) {
            return null;
        }
        if (startsWithIgnoreCase(sprite.name, "Tmpl")) {
            return sprite.name;
        }
        return null;
    }

    /**
     * Toggle visibility of decorations (decors) in the renderer.
     * Shows a short editor message reflecting the new state.
     */
    private void toggleDecorVisibility() {
        decorVisible = !decorVisible;
        mapRenderer.setDecorVisible(decorVisible);
        if (!decorVisible) {
            commitPendingOffsets();
            selectedDecorInfo = null;
            dragState = null;
            decorContextOpen = false;
            decorContextTarget = null;
        }
        showEditorMessage("Decorations: " + (decorVisible ? "VISIBLE" : "HIDDEN"));
        log.info("Decorations visibility toggled: {}", decorVisible);
    }

    /**
     * Toggle visibility of objects in the renderer and display a message.
     */
    private void toggleObjectsVisibility() {
        objectsVisible = !objectsVisible;
        mapRenderer.setObjectsVisible(objectsVisible);
        showEditorMessage("Objects: " + (objectsVisible ? "VISIBLE" : "HIDDEN"));
        log.info("Objects visibility toggled: {}", objectsVisible);
    }

    /**
     * Resolve sprite information for the tile at the given coordinates.
     * Uses the MapReader raw name and the sprite metadata to return a
     * ResolvedSprite structure.
     *
     * @param x tile X
     * @param y tile Y
     * @return ResolvedSprite or null when no sprite is set
     */
    private ResolvedSprite resolveSpriteAt(int x, int y) {
        String name = mapReader.getSpriteName(x, y);
        return SpriteNameParser.parse(name, mapRenderer != null ? mapRenderer.getMetaByName() : null);
    }

    /**
     * Retrieves the rendering offset for a specific tile.
     * The map offset is an additive correction to the sprite metadata offset.
     * Legacy VSF mappings can use different offsets for the same packed image,
     * so this per-tile value is required for faithful placement.
     * @param tileX The x-coordinate of the tile.
     * @param tileY The y-coordinate of the tile.
     * @param mirror Whether the sprite is mirrored.
     * @return The offset for the tile.
     */
    private ModifSprites.Offset getTileOffset(int tileX, int tileY, boolean mirror) {
        if (mapReader == null) {
            return modifSprites.getOffset(null);
        }
        return new ModifSprites.Offset(mapReader.getOffsetX(tileX, tileY), mapReader.getOffsetY(tileX, tileY));
    }

    private float[] getSpriteDrawOffsets(String spriteName, boolean mirror) {
        if (spriteName == null || mapRenderer == null) {
            return new float[] { 0f, 0f };
        }
        String key = spriteName.toLowerCase(Locale.ROOT);
        int[] override = decorOffsetOverrides.get(key);
        if (override != null && override.length >= 4) {
            return mirror ? new float[] { override[2], override[3] } : new float[] { override[0], override[1] };
        }
        var meta = mapRenderer.getMetaByName().get(key);
        if (meta == null) {
            return new float[] { 0f, 0f };
        }
        return mirror ? new float[] { meta.getDrawOffset2X(), meta.getDrawOffset2Y() }
                : new float[] { meta.getDrawOffset1X(), meta.getDrawOffset1Y() };
    }

    private int[] applySpriteOffsetDeltaInternal(String spriteName, boolean mirror, int dx, int dy) {
        if (spriteName == null || mapRenderer == null) {
            return null;
        }
        String key = spriteName.toLowerCase(Locale.ROOT);
        var meta = mapRenderer.getMetaByName().get(key);
        if (meta == null) {
            return null;
        }
        int[] current = decorOffsetOverrides.get(key);
        int off1X = current != null ? current[0] : meta.getDrawOffset1X();
        int off1Y = current != null ? current[1] : meta.getDrawOffset1Y();
        int off2X = current != null ? current[2] : meta.getDrawOffset2X();
        int off2Y = current != null ? current[3] : meta.getDrawOffset2Y();
        if (mirror) {
            off2X += dx;
            off2Y += dy;
        } else {
            off1X += dx;
            off1Y += dy;
        }
        int[] updated = new int[] { off1X, off1Y, off2X, off2Y };
        decorOffsetOverrides.put(key, updated);
        return updated;
    }

    private void applySpriteOffsetDelta(String spriteName, boolean mirror, int dx, int dy) {
        int[] updated = applySpriteOffsetDeltaInternal(spriteName, mirror, dx, dy);
        if (updated == null) {
            return;
        }
        pendingOffsetSprite = spriteName;
        pendingOffsetDirty = true;
        if (mapRenderer != null) {
            mapRenderer.setDecorOffsetOverrides(decorOffsetOverrides);
        }
        int off1X = updated[0];
        int off1Y = updated[1];
        int off2X = updated[2];
        int off2Y = updated[3];
        showEditorMessage("Offset: (" + (mirror ? off2X : off1X) + ", " + (mirror ? off2Y : off1Y) + ")");
    }

    private void applySelectedObjectOffsetDelta(int dx, int dy) {
        if (selectedObjectPositionIndex < 0 || selectedObjectPositionIndex >= objectPositions.size()
                || mapRenderer == null) {
            return;
        }
        ObjectPos selected = objectPositions.get(selectedObjectPositionIndex);
        String logicalName = selected.name() != null ? selected.name().toUpperCase(Locale.ROOT) : "";
        ObjectMapping mapping = mapRenderer.getObjectMappings().get(logicalName);
        if (mapping == null) {
            showEditorMessage("No object mapping for " + selected.name());
            return;
        }

        commitPendingOffsets();
        String[] spriteNames = computeObjectSpriteNames(mapping.sprite);
        int updatedCount = 0;
        int lastX = 0;
        int lastY = 0;
        for (String spriteName : spriteNames) {
            int[] updated = applySpriteOffsetDeltaInternal(spriteName, mapping.mirror, dx, dy);
            if (updated == null) {
                continue;
            }
            queueOffsetWrite(spriteName, updated);
            lastX = mapping.mirror ? updated[2] : updated[0];
            lastY = mapping.mirror ? updated[3] : updated[1];
            updatedCount++;
        }
        if (updatedCount == 0) {
            showEditorMessage("No sprite offsets updated for " + selected.name());
            return;
        }
        mapRenderer.setDecorOffsetOverrides(decorOffsetOverrides);
        showEditorMessage("Object offsets: " + updatedCount + " sprites (" + lastX + ", " + lastY + ")");
    }

    private String[] computeObjectSpriteNames(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return new String[0];
        }
        if (pattern.contains("%d") && pattern.contains("$")) {
            int dollar = pattern.lastIndexOf('$');
            try {
                int frameCount = Integer.parseInt(pattern.substring(dollar + 1));
                String base = pattern.substring(0, dollar);
                String[] names = new String[frameCount];
                for (int i = 1; i <= frameCount; i++) {
                    names[i - 1] = base.replace("%d", String.valueOf(i));
                }
                return names;
            } catch (NumberFormatException e) {
                log.warn("Invalid object sprite pattern: {}", pattern, e);
            }
        }
        return new String[] { pattern };
    }

    private void commitPendingOffsetsIfDifferent(String nextSpriteName) {
        if (!pendingOffsetDirty || pendingOffsetSprite == null) {
            return;
        }
        if (nextSpriteName != null && nextSpriteName.equalsIgnoreCase(pendingOffsetSprite)) {
            return;
        }
        commitPendingOffsets();
    }

    private void commitPendingOffsets() {
        if (!pendingOffsetDirty || pendingOffsetSprite == null) {
            return;
        }
        String key = pendingOffsetSprite.toLowerCase(Locale.ROOT);
        int[] offsets = decorOffsetOverrides.get(key);
        if (offsets != null) {
            queueOffsetWrite(pendingOffsetSprite, offsets);
        }
        pendingOffsetDirty = false;
        pendingOffsetSprite = null;
    }

    private void queueOffsetWrite(String spriteName, int[] offsets) {
        if (spriteName == null || offsets == null || offsets.length < 4) {
            return;
        }
        String key = spriteName.toLowerCase(Locale.ROOT);
        synchronized (offsetWriteLock) {
            pendingOffsetWrites.put(key, Arrays.copyOf(offsets, 4));
            if (offsetWriteFuture != null) {
                offsetWriteFuture.cancel(false);
            }
            offsetWriteFuture = offsetWriteExecutor.schedule(this::flushPendingOffsetWritesSafe,
                    OFFSET_WRITE_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void flushPendingOffsetWritesSafe() {
        try {
            flushPendingOffsetWrites();
        } catch (Throwable t) {
            log.error("Failed to flush sprites.bin offsets", t);
        }
    }

    private void flushPendingOffsetWritesBlocking() {
        synchronized (offsetWriteLock) {
            if (offsetWriteFuture != null) {
                offsetWriteFuture.cancel(false);
                offsetWriteFuture = null;
            }
        }
        flushPendingOffsetWrites();
    }

    private void flushPendingOffsetWrites() {
        Map<String, int[]> batch;
        synchronized (offsetWriteLock) {
            if (pendingOffsetWrites.isEmpty()) {
                return;
            }
            batch = new HashMap<>(pendingOffsetWrites);
            pendingOffsetWrites.clear();
        }
        try {
            updateSpriteBinOffsetsBatch(batch);
            if (webEditorReloadEnabled) {
                reloadSpriteBinWebEditor();
            }
            if (Gdx.app != null) {
                Gdx.app.postRunnable(() -> showEditorMessage("Offsets saved"));
            }
            log.info("Saved {} sprite offset update(s) to sprites.bin", batch.size());
        } catch (IOException e) {
            log.error("Failed to update sprites.bin offsets", e);
        }
    }

    /**
     * Applies draw-offset corrections directly in the sprite library.
     *
     * <p>The library is split into shards; each shard is rewritten independently, and only those
     * actually containing an affected sprite are touched. PNG payloads are copied through as-is,
     * so only 4 integers change per corrected sprite.
     */
    private void updateSpriteBinOffsetsBatch(Map<String, int[]> updates) throws IOException {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        java.nio.file.Path dir = spriteBinDir();
        java.util.List<java.nio.file.Path> shards =
                SpriteBinIO.resolveShards(dir, Paths.SPRITE_BIN_BASE);
        if (shards.isEmpty()) {
            throw new FileNotFoundException("Sprite binary not found: " + Paths.SPRITE_BIN);
        }

        for (java.nio.file.Path shard : shards) {
            java.util.List<SpriteBinIO.Packed> sprites = new java.util.ArrayList<>();
            boolean[] touched = { false };
            SpriteBinIO.readShard(shard, packed -> {
                int[] override = updates.get(packed.name().toLowerCase(Locale.ROOT));
                if (override != null && override.length >= 4) {
                    touched[0] = true;
                    sprites.add(new SpriteBinIO.Packed(packed.name(), packed.width(), packed.height(),
                            override[0], override[1], override[2], override[3],
                            packed.type(), packed.png()));
                } else {
                    sprites.add(packed);
                }
            });
            if (!touched[0]) {
                continue;
            }
            java.nio.file.Path tempFile = shard.resolveSibling(shard.getFileName() + ".tmp");
            SpriteBinIO.writePayload(tempFile, sprites);
            java.nio.file.Files.move(tempFile, shard,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Sprite library directory, preferring the working copy over the one inside the JAR. */
    private static java.nio.file.Path spriteBinDir() {
        File dir = new File(Paths.SPRITE_DIR);
        if (!dir.isDirectory()) {
            File internal = Gdx.files.internal(Paths.SPRITE_DIR).file();
            if (internal.isDirectory()) {
                dir = internal;
            }
        }
        return dir.toPath();
    }

    private void reloadSpriteBinWebEditor() {
        try {
            java.net.URL url = new java.net.URL("http://localhost:8080/api/reload");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(500);
            conn.setReadTimeout(500);
            conn.getResponseCode();
            conn.disconnect();
        } catch (IOException ignored) {
        }
    }


    /**
     * Infers the most appropriate ground sprite for a given tile based on its neighbors.
     * This is used when deleting a decor to replace it with a suitable ground tile.
     * @param tileX The x-coordinate of the tile.
     * @param tileY The y-coordinate of the tile.
     * @return The name of the inferred ground sprite, or null if none could be determined.
     */
    private String inferGroundFromNeighbors(int tileX, int tileY) {
        return inferGroundFromNeighbors(tileX, tileY, Collections.emptySet(), Collections.emptyMap());
    }

    private String inferGroundFromNeighbors(int tileX, int tileY, Set<Long> ignoredTiles,
                                            Map<Long, String> virtualGrounds) {
        if (mapReader == null || mapRenderer == null) {
            return null;
        }
        Map<String, Integer> counts = new HashMap<>();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = tileX + dx;
                int ny = tileY + dy;
                if (nx < 0 || ny < 0 || nx >= mapReader.getWidth() || ny >= mapReader.getHeight()) {
                    continue;
                }
                long neighborKey = packTileKey(nx, ny);
                String neighborName = virtualGrounds != null ? virtualGrounds.get(neighborKey) : null;
                if (neighborName == null && ignoredTiles != null && ignoredTiles.contains(neighborKey)) {
                    continue;
                }
                if (neighborName == null) {
                    // Always inspect the explicit ground layer. getSpriteName()/resolveSpriteAt()
                    // prefers the decor layer and would hide a perfectly valid ground below it.
                    neighborName = mapReader.getGroundSpriteName(nx, ny);
                }
                if (!isUsableGroundForRepair(neighborName)) {
                    continue;
                }
                String base = extractBaseName(neighborName);
                if (base == null || base.isBlank()) {
                    continue;
                }
                int weight = dx == 0 || dy == 0 ? 3 : 2;
                counts.merge(base, weight, Integer::sum);
            }
        }
        if (counts.isEmpty()) {
            return null;
        }
        String best = null;
        int bestCount = -1;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            int count = entry.getValue();
            if (count > bestCount || (count == bestCount
                    && (best == null || entry.getKey().compareToIgnoreCase(best) < 0))) {
                bestCount = count;
                best = entry.getKey();
            }
        }
        return buildReplacementName(null, best, tileX, tileY, getGroundFillPattern(best));
    }

    private String inferGroundReplacement(int tileX, int tileY) {
        String replacement = inferGroundFromNeighbors(tileX, tileY);
        if (replacement != null && !replacement.isBlank()) {
            return replacement;
        }
        if (fillToolSprite != null && !fillToolSprite.isBlank()) {
            return fillToolSprite;
        }
        return groundFillSprites.isEmpty() ? null : groundFillSprites.get(0);
    }

    /**
     * Toggles the horizontal mirror state of the sprite at the given tile coordinates.
     * Appends or removes the 'M' suffix from the sprite name to reflect the mirror state.
     * @param tileX The x-coordinate of the tile.
     * @param tileY The y-coordinate of the tile.
     * @param decorInfo Optional information about the decor at this tile.
     */
    private void toggleMirrorAt(int tileX, int tileY, DecorInfo decorInfo) {
        if (mapReader == null) {
            return;
        }
        if (tileX < 0 || tileX >= mapReader.getWidth() || tileY < 0 || tileY >= mapReader.getHeight()) {
            return;
        }

        log.info("Right click tile: ({}, {})", tileX, tileY);
        boolean isDecorTile = decorInfo != null;
        String currentName = isDecorTile
                ? mapReader.getDecorSpriteName(tileX, tileY)
                : mapReader.getGroundSpriteName(tileX, tileY);
        if (currentName == null || currentName.isBlank()) {
            return;
        }

        String trimmed = currentName.trim();
        ResolvedSprite resolved = SpriteNameParser.parse(currentName, mapRenderer.getMetaByName());
        boolean wasMirrored = resolved != null && resolved.mirror;
        String baseName;
        if (trimmed.endsWith("M")) {
            baseName = trimmed.substring(0, trimmed.length() - 1).trim();
            wasMirrored = true;
        } else if (resolved != null && resolved.name != null && !resolved.name.isBlank()) {
            baseName = resolved.name;
        } else {
            baseName = trimmed;
        }
        String newName = wasMirrored ? baseName : baseName + "M";
        log.info("Mirror toggle at ({}, {}) [isDecor={}]: raw='{}' resolvedName='{}' resolvedMirror={} base='{}' new='{}'",
                tileX, tileY, isDecorTile, currentName,
                resolved != null ? resolved.name : null,
                resolved != null && resolved.mirror,
                baseName,
                newName);
        float oldOffsetX = mapReader.getOffsetX(tileX, tileY);
        float oldOffsetY = mapReader.getOffsetY(tileX, tileY);

        int oldZOrder = mapReader.getZOrder(tileX, tileY);
        UndoEntry undoEntry = newUndoEntry("Mirror", false);
        if (isDecorTile) {
            applyDecorChange(tileX, tileY, newName, mapReader.getScaleX(tileX, tileY),
                    mapReader.getScaleY(tileX, tileY), oldOffsetX, oldOffsetY, oldZOrder, undoEntry);
        } else {
            applyTileChangeWithOffsetAndOrder(tileX, tileY, newName, mapReader.getScaleX(tileX, tileY),
                    mapReader.getScaleY(tileX, tileY), oldOffsetX, oldOffsetY, oldZOrder, undoEntry);
        }
        pushUndoEntry(undoEntry);
        invalidateGroundAround(tileX, tileY, newName);

        if (decorInfo != null) {
            decorInfo.isMirrored = !wasMirrored;
            selectedDecorInfo = decorInfo;
        } else if (selectedDecorInfo != null
                && selectedDecorInfo.tileX == tileX
                && selectedDecorInfo.tileY == tileY) {
            selectedDecorInfo.isMirrored = !wasMirrored;
        }

        showEditorMessage("Mirror: " + (wasMirrored ? "OFF" : "ON"));
    }

    /**
     * Set a transient editor message shown on the UI for a short duration.
     *
     * @param message text to display
     */
    private void showEditorMessage(String message) {
        editorMessage = message;
        editorMessageTimer = MESSAGE_DURATION;
    }

    private void showInfoAndLog(String message, String logTemplate, Object... args) {
        showEditorMessage(message);
        log.info(logTemplate, args);
    }

    private boolean ensureMapLoaded() {
        if (mapReader != null) {
            return true;
        }
        showEditorMessage("No map loaded");
        return false;
    }

    private void copyClickedTileInfoToClipboard(int screenX, int screenY) {
        if (mapReader == null || Gdx.app == null) {
            return;
        }
        int mapZ = getCurrentMapZ();
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int) Math.floor(worldCoords.x / GameConstants.GRID_W);
        int tileY = (int) Math.floor(worldCoords.y / GameConstants.GRID_H);
        if (!isTileInMap(tileX, tileY)) {
            return;
        }
        // With decorations visible, prefer the decor whose drawn bounds contain the
        // click (decors are anchored on one tile but span several); report its anchor.
        if (decorVisible) {
            DecorInfo decor = findDecorAtPosition(worldCoords.x, worldCoords.y);
            if (decor != null) {
                String decorName = mapReader.getSpriteName(decor.tileX, decor.tileY);
                if (decorName == null || decorName.isBlank()) {
                    decorName = decor.spriteName;
                }
                Gdx.app.getClipboard().setContents(
                        "x=" + decor.tileX + ", y=" + decor.tileY + ", z=" + mapZ + ", name=" + decorName);
                return;
            }
        }
        // With decorations hidden, report the GROUND layer even when a decor sits on top.
        String tileName = !decorVisible
                ? mapReader.getGroundSpriteName(tileX, tileY)
                : mapReader.getSpriteName(tileX, tileY);
        if (tileName == null || tileName.isBlank()) {
            tileName = "<empty>";
        }
        Gdx.app.getClipboard().setContents(
                "x=" + tileX + ", y=" + tileY + ", z=" + mapZ + ", name=" + tileName);
    }

    /**
     * Update the editor message timer; clears the message when its time
     * elapses.
     *
     * @param delta seconds elapsed since last update
     */
    private void updateEditorMessage(float delta) {
        if (editorMessageTimer > 0f) {
            editorMessageTimer -= delta;
            if (editorMessageTimer <= 0f) {
                editorMessage = null;
            }
        }
    }

    /**
     * Render the currently active editor message in screen space with a
     * subtle outline and fade effect.
     *
     * @param batch SpriteBatch already configured for UI rendering
     */
    private void renderEditorMessage(SpriteBatch batch) {
        if (editorMessage == null || editorMessageTimer <= 0f) {
            return;
        }

        float screenWidth = Gdx.graphics.getWidth();
        float toastHeight = 34f;
        float toastPadding = 18f;

        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font,
                editorMessage);
        float toastWidth = Math.min(screenWidth - 24f, layout.width + toastPadding * 2f);
        float toastX = (screenWidth - toastWidth) / 2f;
        float toastY = Gdx.graphics.getHeight() - toastHeight - 12f;
        float x = toastX + (toastWidth - layout.width) / 2f;
        float y = toastY + 22f;

        float alpha = 1.0f;
        if (editorMessageTimer < MESSAGE_FADE_DURATION) {
            alpha = editorMessageTimer / MESSAGE_FADE_DURATION;
        }

        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UI_SURFACE.r, UI_SURFACE.g, UI_SURFACE.b, 0.96f * alpha);
        shapeRenderer.rect(toastX, toastY, toastWidth, toastHeight);
        shapeRenderer.setColor(UI_BLUE.r, UI_BLUE.g, UI_BLUE.b, 0.95f * alpha);
        shapeRenderer.rect(toastX, toastY, 4f, toastHeight);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UI_BORDER.r, UI_BORDER.g, UI_BORDER.b, 0.85f * alpha);
        shapeRenderer.rect(toastX, toastY, toastWidth, toastHeight);
        shapeRenderer.end();
        batch.begin();

        font.setColor(UI_TEXT.r, UI_TEXT.g, UI_TEXT.b, alpha);
        font.draw(batch, editorMessage, x, y);

        font.setColor(Color.WHITE);
    }

    /**
     * Render a centered progress bar while a long-running background operation is
     * active (Tmpl3 rebuild/regeneration, ground recalculation, collision
     * regeneration). Each operation reports a 0..1 fraction and a stage label.
     * Drawn in screen space, on top of the rest of the HUD.
     *
     * @param batch SpriteBatch already configured for UI rendering (begin()-state)
     */
    private void renderRegenerationProgress(SpriteBatch batch) {
        float progress;
        String stage;
        if (tmpl3Regenerating) {
            progress = tmpl3RegenerationProgress;
            stage = tmpl3RegenerationStage;
        } else if (groundRecalculating) {
            progress = groundRecalculationProgress;
            stage = groundRecalculationStage;
        } else if (collisionRegenerating) {
            progress = collisionRegenerationProgress;
            stage = collisionRegenerationStage;
        } else {
            return;
        }
        progress = Math.max(0f, Math.min(1f, progress));
        if (stage == null || stage.isBlank()) {
            stage = "Working...";
        }
        String label = stage + "  " + Math.round(progress * 100f) + "%";

        float screenWidth = Gdx.graphics.getWidth();
        float barWidth = Math.min(420f, screenWidth - 48f);
        float barHeight = 14f;
        float panelPadding = 12f;
        float labelHeight = 22f;
        float panelWidth = barWidth + panelPadding * 2f;
        float panelHeight = barHeight + labelHeight + panelPadding * 2f;
        float panelX = (screenWidth - panelWidth) / 2f;
        float panelY = (Gdx.graphics.getHeight() - panelHeight) / 2f;
        float barX = panelX + panelPadding;
        float barY = panelY + panelPadding;

        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Panel background.
        shapeRenderer.setColor(UI_SURFACE.r, UI_SURFACE.g, UI_SURFACE.b, 0.96f);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        // Bar track.
        shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        // Bar fill.
        shapeRenderer.setColor(UI_BLUE.r, UI_BLUE.g, UI_BLUE.b, 0.95f);
        shapeRenderer.rect(barX, barY, barWidth * progress, barHeight);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UI_BORDER.r, UI_BORDER.g, UI_BORDER.b, 0.85f);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
        batch.begin();

        com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, label);
        float textX = panelX + (panelWidth - layout.width) / 2f;
        float textY = barY + barHeight + labelHeight - 4f;
        font.setColor(UI_TEXT.r, UI_TEXT.g, UI_TEXT.b, 1f);
        font.draw(batch, label, textX, textY);
        font.setColor(Color.WHITE);
    }

    /**
     * Persist the current camera position, zoom and map path to disk so the
     * editor can restore view state on next launch.
     */
    private void saveCameraPosition() {
        try {
            if (cameraPosition.x == lastSavedCameraX
                    && cameraPosition.y == lastSavedCameraY
                    && zoom == lastSavedCameraZoom
                    && Objects.equals(currentMapPath, lastSavedCameraMapPath)) {
                return;
            }
            Map<String, Object> cameraData = new HashMap<>();
            cameraData.put("x", cameraPosition.x);
            cameraData.put("y", cameraPosition.y);
            cameraData.put("zoom", zoom);
            cameraData.put("mapPath", currentMapPath);

            try (FileWriter writer = new FileWriter(CAMERA_POSITION_FILE)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(cameraData, writer);
                lastSavedCameraX = cameraPosition.x;
                lastSavedCameraY = cameraPosition.y;
                lastSavedCameraZoom = zoom;
                lastSavedCameraMapPath = currentMapPath;
                log.info("Saved camera position: ({}, {}) zoom: {}",
                        (int) cameraPosition.x, (int) cameraPosition.y, zoom);
            }
        } catch (IOException e) {
            log.warn("Failed to save camera position", e);
        }
    }

    /**
     * Load saved camera position, zoom and map path from disk. If no saved
     * state exists the defaults are retained.
     */
    private void loadCameraPosition() {
        try (FileReader reader = new FileReader(CAMERA_POSITION_FILE)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> cameraData = gson.fromJson(reader, type);

            if (cameraData != null) {
                Number x = (Number) cameraData.get("x");
                Number y = (Number) cameraData.get("y");
                Number z = (Number) cameraData.get("zoom");
                Object mapPath = cameraData.get("mapPath");

                if (mapPath instanceof String mapPathString && !mapPathString.isEmpty()) {
                    currentMapPath = normalizeMapPath(mapPathString);
                    log.info("Loaded map path: {}", currentMapPath);
                }

                if (x != null && y != null) {
                    cameraPosition.set(x.floatValue(), y.floatValue());
                    log.info("Loaded camera position: ({}, {})", (int) cameraPosition.x, (int) cameraPosition.y);
                }

                if (z != null) {
                    zoom = z.floatValue();
                    log.info("Loaded camera zoom: {}", zoom);
                }
                lastSavedCameraX = cameraPosition.x;
                lastSavedCameraY = cameraPosition.y;
                lastSavedCameraZoom = zoom;
                lastSavedCameraMapPath = currentMapPath;
            }
        } catch (IOException e) {
            log.info("No saved camera position found, using default");
        }
    }

    /**
     * Persist the dragged decor position by moving its tile and updating the
     * sprite offset to match the drop world position.
     */
    private void saveDraggedDecorOffset(int screenX, int screenY) {
        if (dragState == null) {
            return;
        }

        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        int mouseTileX = (int) (worldCoords.x / GameConstants.GRID_W);
        int mouseTileY = (int) (worldCoords.y / GameConstants.GRID_H);
        int targetTileX = dragState.tileX + mouseTileX - dragState.startMouseTileX;
        int targetTileY = dragState.tileY + mouseTileY - dragState.startMouseTileY;
        if (mapReader != null) {
            targetTileX = Math.max(0, Math.min(targetTileX, mapReader.getWidth() - 1));
            targetTileY = Math.max(0, Math.min(targetTileY, mapReader.getHeight() - 1));
        }

        if (targetTileX < 0 || targetTileX >= mapReader.getWidth()
                || targetTileY < 0 || targetTileY >= mapReader.getHeight()) {
            cancelDragDecor();
            return;
        }

        var meta = mapRenderer.getMetaByName().get(dragState.spriteName.toLowerCase(Locale.ROOT));
        if (meta == null) {
            cancelDragDecor();
            return;
        }

        if (targetTileX == dragState.tileX && targetTileY == dragState.tileY) {
            mapReader.setDecorSpriteName(dragState.tileX, dragState.tileY, dragState.originalSpriteName);
            mapReader.setScale(dragState.tileX, dragState.tileY, dragState.originalScaleX, dragState.originalScaleY);
            mapReader.setOffset(dragState.tileX, dragState.tileY, dragState.originalOffset.x, dragState.originalOffset.y);
            mapReader.setZOrder(dragState.tileX, dragState.tileY, dragState.originalZOrder);
            invalidateGroundAround(dragState.tileX, dragState.tileY, dragState.originalSpriteName);
            return;
        }

        // Real drag-and-drop move: remove the source decor, then place it on the target ground.
        String sourceReplacement = mapReader.getGroundSpriteName(dragState.tileX, dragState.tileY);
        if (sourceReplacement == null || sourceReplacement.isBlank()) {
            sourceReplacement = inferGroundReplacement(dragState.tileX, dragState.tileY);
            mapReader.setGroundSpriteName(dragState.tileX, dragState.tileY, sourceReplacement);
        }
        mapReader.setDecorSpriteName(dragState.tileX, dragState.tileY, null);
        mapReader.setScale(dragState.tileX, dragState.tileY, 1f, 1f);
        mapReader.setOffset(dragState.tileX, dragState.tileY, 0f, 0f);
        mapReader.setZOrder(dragState.tileX, dragState.tileY, 0);

        mapReader.setDecorSpriteName(targetTileX, targetTileY, dragState.originalSpriteName);
        mapReader.setScale(targetTileX, targetTileY, dragState.originalScaleX, dragState.originalScaleY);
        mapReader.setOffset(targetTileX, targetTileY, dragState.originalOffset.x, dragState.originalOffset.y);
        mapReader.setZOrder(targetTileX, targetTileY, dragState.originalZOrder);

        mapDirty = true;
        invalidateGroundAround(dragState.tileX, dragState.tileY, dragState.originalSpriteName);
        invalidateGroundAround(targetTileX, targetTileY, dragState.originalSpriteName);

        if (selectedDecorInfo != null) {
            selectedDecorInfo.tileX = targetTileX;
            selectedDecorInfo.tileY = targetTileY;
            selectedDecorInfo.isMirrored = dragState.isMirrored;
        }

        showEditorMessage("Decor moved: " + dragState.originalSpriteName + " (" + targetTileX + ", " + targetTileY + ")");
        log.info("Moved decor {} from ({}, {}) to ({}, {})",
                dragState.originalSpriteName, dragState.tileX, dragState.tileY, targetTileX, targetTileY);
    }

    /**
     * Cancels the current drag-and-drop operation for a decor.
     * This restores the decor to its original position and state before the drag started.
     */
    private void cancelDragDecor() {
        if (dragState == null) {
            return;
        }

        mapReader.setDecorSpriteName(dragState.tileX, dragState.tileY, dragState.originalSpriteName);
        mapReader.setScale(dragState.tileX, dragState.tileY, dragState.originalScaleX, dragState.originalScaleY);
        mapReader.setOffset(dragState.tileX, dragState.tileY, dragState.originalOffset.x, dragState.originalOffset.y);
        mapReader.setZOrder(dragState.tileX, dragState.tileY, dragState.originalZOrder);
        if (selectedDecorInfo != null) {
            selectedDecorInfo.tileX = dragState.tileX;
            selectedDecorInfo.tileY = dragState.tileY;
            selectedDecorInfo.isMirrored = dragState.isMirrored;
        }
        dragState = null;
    }

    private void updateDraggedDecorPreview() {
        if (dragState == null || selectedDecorInfo == null) {
            return;
        }
        selectedDecorInfo.tileX = dragState.targetTileX;
        selectedDecorInfo.tileY = dragState.targetTileY;
        selectedDecorInfo.isMirrored = dragState.isMirrored;
    }

    private void snapClickedPixelToSelectedTile(DecorInfo decorInfo, float clickWorldX, float clickWorldY) {
        if (decorInfo == null || mapReader == null || mapRenderer == null || spriteLoader == null) {
            return;
        }
        if (selectedTileX < 0 || selectedTileY < 0 || selectedTileX >= mapReader.getWidth()
                || selectedTileY >= mapReader.getHeight()) {
            showEditorMessage("Select a target tile first");
            return;
        }
        TextureRegion region = spriteLoader.getRegionFromSpriteName(decorInfo.spriteName);
        if (region == null) {
            return;
        }
        String key = decorInfo.spriteName.toLowerCase(Locale.ROOT);
        var meta = mapRenderer.getMetaByName().get(key);
        if (meta == null) {
            return;
        }
        float[] drawOffsets = getSpriteDrawOffsets(decorInfo.spriteName, decorInfo.isMirrored);
        float scaleX = mapReader.getScaleX(decorInfo.tileX, decorInfo.tileY);
        float scaleY = mapReader.getScaleY(decorInfo.tileX, decorInfo.tileY);
        float width = region.getRegionWidth() * scaleX;
        float height = region.getRegionHeight() * scaleY;
        ModifSprites.Offset tileOffset = getTileOffset(decorInfo.tileX, decorInfo.tileY, decorInfo.isMirrored);
        float currentRenderX = decorInfo.tileX * GameConstants.GRID_W + drawOffsets[0] + tileOffset.x;
        float currentRenderY = decorInfo.tileY * GameConstants.GRID_H + drawOffsets[1] + tileOffset.y;
        float localClickX = clickWorldX - currentRenderX;
        float localClickY = clickWorldY - currentRenderY;
        float targetWorldX = selectedTileX * GameConstants.GRID_W + (GameConstants.GRID_W * 0.5f);
        float targetWorldY = selectedTileY * GameConstants.GRID_H + (GameConstants.GRID_H * 0.5f);
        float desiredRenderX = targetWorldX - localClickX;
        float desiredRenderY = targetWorldY - localClickY;
        int snapOffsetX = Math.round(desiredRenderX
                - (decorInfo.tileX * GameConstants.GRID_W + tileOffset.x));
        int snapOffsetY = Math.round(desiredRenderY
                - (decorInfo.tileY * GameConstants.GRID_H + tileOffset.y));

        int[] current = decorOffsetOverrides.get(key);
        int off1X = current != null ? current[0] : meta.getDrawOffset1X();
        int off1Y = current != null ? current[1] : meta.getDrawOffset1Y();
        int off2X = current != null ? current[2] : meta.getDrawOffset2X();
        int off2Y = current != null ? current[3] : meta.getDrawOffset2Y();
        if (decorInfo.isMirrored) {
            off2X = snapOffsetX;
            off2Y = snapOffsetY;
        } else {
            off1X = snapOffsetX;
            off1Y = snapOffsetY;
        }
        decorOffsetOverrides.put(key, new int[] { off1X, off1Y, off2X, off2Y });
        pendingOffsetSprite = decorInfo.spriteName;
        pendingOffsetDirty = true;
        mapRenderer.setDecorOffsetOverrides(decorOffsetOverrides);

        int updated = 0;
        for (int y = 0; y < mapReader.getHeight(); y++) {
            for (int x = 0; x < mapReader.getWidth(); x++) {
                String spriteAtTile = mapReader.getSpriteName(x, y);
                if (decorInfo.spriteName == null ? spriteAtTile != null : !decorInfo.spriteName.equals(spriteAtTile)) {
                    continue;
                }
                updated++;
            }
        }
        mapDirty = true;
        invalidateGroundAround(decorInfo.tileX, decorInfo.tileY, decorInfo.spriteName);
        showEditorMessage("Snapped " + updated + " occurrence(s) of " + decorInfo.spriteName + " to selected tile");
    }

    /**
     * Open the sprite picker UI for the currently selected tile.
     * The picker is initialized with the current tile sprite selection.
     */
    private void openSpritePicker() {
        if (selectedTileX < 0 || selectedTileY < 0)
            return;

        editorMode = EditorMode.SPRITE_PICKER;
        ensureCachedSpriteList();
        String currentTileName = getSelectableTileSpriteName(selectedTileX, selectedTileY);
        spritePicker = new SpritePickerUI(cachedSpriteList, currentTileName, selectedTileX, selectedTileY);
    }

    private void ensureCachedSpriteList() {
        if (cachedSpriteList != null) {
            return;
        }
        preloadSprites = null;
        preloadSpriteIndex = 0;
        preloadSpriteList();
    }

    /**
     * Close the sprite picker and return to the select mode.
     */
    private void closeSpritePicker() {
        editorMode = EditorMode.SELECT_TILE;
        spritePicker = null;
    }

    /**
     * Reload map-related data and reinitialize the renderer using the
     * current ModifSprites. Useful after external edits to sprites.
     *
     * @throws GameException if reloading fails
     */
    private void reloadMapData() throws GameException {
        modifSprites = ModifSprites.empty();

        if (mapRenderer != null) {
            mapRenderer.reload(modifSprites);
        } else {
            mapRenderer = new MapRenderer(mapReader, spriteLoader, batchSol, batchDecor, outlineShader,
                    modifSprites);
        }
        // Reapply rendering states
        mapRenderer.setDecorVisible(decorVisible);
        mapRenderer.setObjectsVisible(objectsVisible);
        mapRenderer.setGroundOutlineEnabled(groundOutlineEnabled);
        mapRenderer.setDecorUseTileOffsets(true);
        mapRenderer.setDecorOffsetOverrides(decorOffsetOverrides);

        log.info("Reloaded map data and renderer");
    }

    /**
     * Invalidate ground renderer caches for tiles around the specified
     * coordinate and clear TMPL caches when necessary.
     *
     * @param tileX      tile X coordinate
     * @param tileY      tile Y coordinate
     * @param spriteName sprite name used to decide cache clearing
     */
    private void invalidateGroundAround(int tileX, int tileY, String spriteName) {
        if (mapRenderer == null) {
            return;
        }
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (mapRenderer.getGroundRenderer() != null) {
                    mapRenderer.getGroundRenderer().invalidateTileCache(tileX + dx, tileY + dy);
                }
                mapRenderer.invalidateDecorTileCache(tileX + dx, tileY + dy);
            }
        }
        clearTmplCacheIfNeeded(spriteName);
    }

    /**
     * Invalidate broader ground renderer caches after a bulk modification
     * (for example a flood fill) so changes are reflected immediately.
     *
     * @param spriteName sprite name used to decide cache clearing
     */
    private void invalidateGroundAfterBulkChange(String spriteName) {
        if (mapRenderer == null) {
            return;
        }
        if (mapRenderer.getGroundRenderer() != null) {
            mapRenderer.getGroundRenderer().clearChunkCache();
        }
        mapRenderer.clearDecorTileCache();
        clearTmplCacheIfNeeded(spriteName);
        int globalTmplCleared = com.perso.T4C.render.TileCache.clearTmplRegionsOnly();
        log.info("Ground caches invalidated after bulk change '{}' (global tmpl regions cleared={})", spriteName,
                globalTmplCleared);
    }

    private void clearTmplCacheIfNeeded(String spriteName) {
        if (spriteName == null) {
            return;
        }
        String trimmed = spriteName.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("tmpl") || trimmed.contains("[")) {
            com.perso.T4C.render.TileCache.clearTmplRegionsOnly();
        }
    }

    private UndoEntry newUndoEntry(String label, boolean persistOnUndo) {
        return new UndoEntry(label, persistOnUndo);
    }

    private void pushUndoEntry(UndoEntry entry) {
        if (entry == null || !entry.hasChanges()) {
            return;
        }
        undoStack.addFirst(entry);
        while (undoStack.size() > MAX_UNDO) {
            undoStack.removeLast();
        }
    }

    private void applyTileChange(int x, int y, String newName, float newScaleX, float newScaleY, UndoEntry undoEntry) {
        applyTileChangeWithOffsetAndOrder(x, y, newName, newScaleX, newScaleY, 0f, 0f, 0, undoEntry);
    }

    private void applyTileChangeWithOffsetAndOrder(int x, int y, String newName, float newScaleX, float newScaleY,
                                                   float newOffsetX, float newOffsetY, int newZOrder,
                                                   UndoEntry undoEntry) {
        if (mapReader == null) {
            return;
        }
        String oldName = mapReader.getSpriteName(x, y);
        float oldScaleX = mapReader.getScaleX(x, y);
        float oldScaleY = mapReader.getScaleY(x, y);
        float oldOffsetX = mapReader.getOffsetX(x, y);
        float oldOffsetY = mapReader.getOffsetY(x, y);
        int oldZOrder = mapReader.getZOrder(x, y);
        boolean sameName = Objects.equals(oldName, newName);
        boolean sameScale = Float.compare(oldScaleX, newScaleX) == 0 && Float.compare(oldScaleY, newScaleY) == 0;
        boolean sameOffset = Float.compare(oldOffsetX, newOffsetX) == 0 && Float.compare(oldOffsetY, newOffsetY) == 0;
        boolean sameOrder = oldZOrder == newZOrder;
        if (sameName && sameScale && sameOffset && sameOrder) {
            return;
        }
        if (undoEntry != null) {
            undoEntry.changes.add(new TileChange(x, y, oldName, oldScaleX, oldScaleY, oldOffsetX, oldOffsetY,
                    oldZOrder, newName, newScaleX, newScaleY, newOffsetX, newOffsetY, newZOrder));
        }
        mapReader.setSpriteName(x, y, newName);
        mapReader.setScale(x, y, newScaleX, newScaleY);
        mapReader.setOffset(x, y, newOffsetX, newOffsetY);
        mapReader.setZOrder(x, y, newZOrder);
        mapDirty = true;
    }

    private void applyDecorChange(int x, int y, String newName, float newScaleX, float newScaleY,
                                  float newOffsetX, float newOffsetY, int newZOrder, UndoEntry undoEntry) {
        if (mapReader == null) {
            return;
        }
        String oldName = mapReader.getDecorSpriteName(x, y);
        float oldScaleX = mapReader.getScaleX(x, y);
        float oldScaleY = mapReader.getScaleY(x, y);
        float oldOffsetX = mapReader.getOffsetX(x, y);
        float oldOffsetY = mapReader.getOffsetY(x, y);
        int oldZOrder = mapReader.getZOrder(x, y);
        boolean sameName = Objects.equals(oldName, newName);
        boolean sameScale = Float.compare(oldScaleX, newScaleX) == 0 && Float.compare(oldScaleY, newScaleY) == 0;
        boolean sameOffset = Float.compare(oldOffsetX, newOffsetX) == 0 && Float.compare(oldOffsetY, newOffsetY) == 0;
        boolean sameOrder = oldZOrder == newZOrder;
        if (sameName && sameScale && sameOffset && sameOrder) {
            return;
        }
        if (undoEntry != null) {
            undoEntry.changes.add(new TileChange(x, y, oldName, oldScaleX, oldScaleY, oldOffsetX, oldOffsetY,
                    oldZOrder, newName, newScaleX, newScaleY, newOffsetX, newOffsetY, newZOrder, true));
        }
        mapReader.setDecorSpriteName(x, y, newName);
        mapReader.setScale(x, y, newScaleX, newScaleY);
        mapReader.setOffset(x, y, newOffsetX, newOffsetY);
        mapReader.setZOrder(x, y, newZOrder);
        mapDirty = true;
    }

    private void performUndo() {
        if (mapReader == null || undoStack.isEmpty()) {
            showEditorMessage("Nothing to undo");
            return;
        }
        UndoEntry entry = undoStack.removeFirst();
        boolean bulkInvalidate = entry.changes.size() > 64;
        String bulkInvalidateName = null;
        for (TileChange change : entry.changes) {
            if (change.groundUnderDecor) {
                mapReader.setGroundSpriteName(change.x, change.y, change.oldName);
            } else if (change.isDecor) {
                mapReader.setDecorSpriteName(change.x, change.y, change.oldName);
            } else {
                mapReader.setSpriteNameFast(change.x, change.y, change.oldName);
            }
            mapReader.setScale(change.x, change.y, change.oldScaleX, change.oldScaleY);
            mapReader.setOffset(change.x, change.y, change.oldOffsetX, change.oldOffsetY);
            mapReader.setZOrder(change.x, change.y, change.oldZOrder);
            String invalidateName = change.oldName != null ? change.oldName : change.newName;
            if (bulkInvalidate) {
                if (bulkInvalidateName == null && invalidateName != null) {
                    bulkInvalidateName = invalidateName;
                } else {
                    clearTmplCacheIfNeeded(invalidateName);
                }
            } else {
                invalidateGroundAround(change.x, change.y, invalidateName);
            }
        }
        if (bulkInvalidate) {
            invalidateGroundAfterBulkChange(bulkInvalidateName);
        }
        mapDirty = true;
        minimapDirty = true;
        showEditorMessage("Undo: " + entry.label + " (not saved)");
        log.info("Undo {} (persist={}, changes={})", entry.label, entry.persistOnUndo, entry.changes.size());
    }

    private void updateDecorNudge(float delta) {
        if (editorMode == EditorMode.SPRITE_PICKER || !hasOffsetNudgeTarget()) {
            decorNudgeHoldTime = 0f;
            decorNudgeAccumulator = 0f;
            decorNudgeLastDx = 0;
            decorNudgeLastDy = 0;
            return;
        }
        int dx = 0;
        int dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dy -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dy += 1;
        }
        if (dx == 0 && dy == 0) {
            decorNudgeHoldTime = 0f;
            decorNudgeAccumulator = 0f;
            decorNudgeLastDx = 0;
            decorNudgeLastDy = 0;
            return;
        }

        if (dx != decorNudgeLastDx || dy != decorNudgeLastDy) {
            decorNudgeHoldTime = 0f;
            decorNudgeAccumulator = 0f;
            decorNudgeLastDx = dx;
            decorNudgeLastDy = dy;
            applyOffsetNudgeDelta(dx, dy);
            return;
        }

        decorNudgeHoldTime += delta;
        if (decorNudgeHoldTime < DECOR_NUDGE_INITIAL_DELAY) {
            return;
        }
        float accel = (decorNudgeHoldTime - DECOR_NUDGE_INITIAL_DELAY) * DECOR_NUDGE_ACCEL;
        float interval = Math.max(DECOR_NUDGE_MIN_INTERVAL, 0.12f - accel);
        decorNudgeAccumulator += delta;
        while (decorNudgeAccumulator >= interval) {
            decorNudgeAccumulator -= interval;
            applyOffsetNudgeDelta(dx, dy);
        }
    }

    private boolean hasOffsetNudgeTarget() {
        if (editorMode == EditorMode.OBJECT_POSITION_EDITOR) {
            return selectedObjectPositionIndex >= 0 && selectedObjectPositionIndex < objectPositions.size();
        }
        return selectedDecorInfo != null;
    }

    private void applyOffsetNudgeDelta(int dx, int dy) {
        if (editorMode == EditorMode.OBJECT_POSITION_EDITOR) {
            applySelectedObjectOffsetDelta(dx, dy);
            return;
        }
        if (selectedDecorInfo == null) {
            return;
        }
        ResolvedSprite resolved = resolveSpriteAt(selectedDecorInfo.tileX, selectedDecorInfo.tileY);
        if (resolved != null && resolved.name != null) {
            applySpriteOffsetDelta(resolved.name, resolved.mirror, dx, dy);
        }
    }

    private void updateCamera(float delta) {
        if (isModalEditorOpen()) {
            cameraMoveHoldTime = 0f;
            camera.position.set(cameraPosition.x, cameraPosition.y, 0);
            camera.zoom = zoom;
            camera.update();
            return;
        }

        // If CTRL is pressed, do nothing
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            camera.position.set(cameraPosition.x, cameraPosition.y, 0);
            camera.zoom = zoom;
            camera.update();
            return;
        }

        boolean allowArrowMove = selectedDecorInfo == null;
        boolean moveUp = Gdx.input.isKeyPressed(Input.Keys.W)
                || Gdx.input.isKeyPressed(Input.Keys.Z)
                || (allowArrowMove && Gdx.input.isKeyPressed(Input.Keys.UP));
        boolean moveDown = Gdx.input.isKeyPressed(Input.Keys.S)
                || (allowArrowMove && Gdx.input.isKeyPressed(Input.Keys.DOWN));
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.Q)
                || (allowArrowMove && Gdx.input.isKeyPressed(Input.Keys.LEFT));
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D)
                || (allowArrowMove && Gdx.input.isKeyPressed(Input.Keys.RIGHT));
        boolean isMoving = moveUp || moveDown || moveLeft || moveRight;

        if (isMoving) {
            cameraMoveHoldTime += delta;
        } else {
            cameraMoveHoldTime = 0f;
        }

        float speedMultiplier = 1f
                + Math.min(cameraMoveHoldTime * CAMERA_ACCELERATION_RATE, CAMERA_MAX_SPEED_MULTIPLIER - 1f);
        float speed = CAMERA_SPEED * zoom * speedMultiplier;

        if (editorMode == EditorMode.SELECT_TILE || editorMode == EditorMode.COLLISION_EDITOR) {
            if (moveUp) {
                cameraPosition.y -= speed * delta;
            }
            if (moveDown) {
                cameraPosition.y += speed * delta;
            }
            if (moveLeft) {
                cameraPosition.x -= speed * delta;
            }
            if (moveRight) {
                cameraPosition.x += speed * delta;
            }
        }

        camera.position.set(cameraPosition.x, cameraPosition.y, 0);
        camera.zoom = zoom;
        camera.update();
    }

    @Override
    public void render(float delta) {
        if (exitSaving) {
            Gdx.gl.glClearColor(0.08f, 0.08f, 0.09f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            renderLoadingOverlay();
            if (!loadingOverlayPresented) {
                loadingOverlayPresented = true;
                return;
            }
            if (!exitSaveStepPrepared) {
                prepareExitSaveStepOverlay();
                return;
            }
            processExitSaveStep();
            return;
        }
        if (isLoading || spriteHotReloading) {
            if (!loadingOverlayPresented) {
                Gdx.gl.glClearColor(0.08f, 0.08f, 0.09f, 1f);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                renderLoadingOverlay();
                loadingOverlayPresented = true;
                return;
            }
            processLoadingSteps();
            if (isLoading || spriteHotReloading) {
                Gdx.gl.glClearColor(0.08f, 0.08f, 0.09f, 1f);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                renderLoadingOverlay();
                return;
            }
        }
        checkSpritesBinUpdates();
        processPendingTmpl3RegenerationApply();
        processPendingGroundRecalculationApply();
        updateDecorNudge(delta);
        updateCamera(delta);
        updateEditorMessage(delta);

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (dragState != null && mapReader != null) {
            String currentName = mapReader.getDecorSpriteName(dragState.tileX, dragState.tileY);
            if (currentName != null && !currentName.isBlank()) {
                mapReader.setDecorSpriteName(dragState.tileX, dragState.tileY, null);
            }
        }

        int[] bounds = calculateVisibleBounds(RENDER_TILE_MARGIN);
        int startX = bounds[0], endX = bounds[1], startY = bounds[2], endY = bounds[3];

        // Warm the ground/decor chunk caches for the currently visible area every frame.
        // The cache is built lazily per-frame (time-capped); without this continuous warming
        // only the chunks warmed during the initial load are ever drawn, leaving the rest black.
        mapRenderer.warmCaches(startX, endX, startY, endY, 64, 256, 1200);

        mapRenderer.renderGroundOnly(camera, startX, endX, startY, endY);
        mapRenderer.renderEntitiesWithDecors(camera, 0, 0, startX, endX, startY, endY, Collections.emptyList());

        // Render collision overlay
        renderCollisionOverlay();

        // Render ambiance music zones overlay
        lastMusicOverlayBounds = bounds;
        renderMusicZonesOverlay();
        renderMusicBrushPreview();
        renderTeleportsOverlay();
        renderTeleportMapOverlay();
        renderObjectPositionOverlay();

        // Render rectangle selection
        renderRectangleSelection();

        // Render selection highlight
        if (selectedTileX >= 0 && selectedTileY >= 0 && editorMode == EditorMode.SELECT_TILE) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            float x = selectedTileX * GameConstants.GRID_W;
            float y = selectedTileY * GameConstants.GRID_H;
            Color highlight = scaleToolEnabled ? new Color(0.2f, 1f, 0.4f, 1f) : Color.YELLOW;

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(highlight.r, highlight.g, highlight.b, 0.16f);
            shapeRenderer.rect(x, y, GameConstants.GRID_W, GameConstants.GRID_H);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0f, 0f, 0f, 0.85f);
            Gdx.gl.glLineWidth(3);
            shapeRenderer.rect(x - 1f, y - 1f, GameConstants.GRID_W + 2f, GameConstants.GRID_H + 2f);
            shapeRenderer.setColor(highlight);
            Gdx.gl.glLineWidth(2);
            shapeRenderer.rect(x, y, GameConstants.GRID_W, GameConstants.GRID_H);
            Gdx.gl.glLineWidth(1);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        renderMonsterSpawns();
        renderNpcSpawns();

        // Render drag indicator - show the decor with outline shader
        if (dragState != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);

            // First, draw the origin tile in red
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(new Color(1, 0, 0, 0.5f));
            float originX = dragState.tileX * GameConstants.GRID_W;
            float originY = dragState.tileY * GameConstants.GRID_H;
            shapeRenderer.rect(originX, originY, GameConstants.GRID_W, GameConstants.GRID_H);
            shapeRenderer.end();

            // Render the sprite with outline shader
            var meta = mapRenderer.getMetaByName().get(dragState.spriteName.toLowerCase(Locale.ROOT));
            if (meta != null) {
                // Calculate actual render position with offsets
                float[] drawOffsets = getSpriteDrawOffsets(dragState.spriteName, dragState.isMirrored);
                float offX = drawOffsets[0] + dragState.originalOffset.x;
                float offY = drawOffsets[1] + dragState.originalOffset.y;

                // Get sprite region
                TextureRegion region = spriteLoader.getRegionFromSpriteName(dragState.spriteName);
                if (region != null) {
                    float scaleX = dragState.originalScaleX;
                    float scaleY = dragState.originalScaleY;
                    float w = region.getRegionWidth() * scaleX;
                    float h = region.getRegionHeight() * scaleY;

                    float renderX = dragState.targetTileX * GameConstants.GRID_W + offX;
                    float renderY = dragState.targetTileY * GameConstants.GRID_H + offY;

                    // Enable blending for the outline
                    Gdx.gl.glEnable(GL20.GL_BLEND);
                    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                    // Use outline shader
                    outlineBatch.setProjectionMatrix(camera.combined);
                    outlineBatch.setShader(outlineShader);
                    outlineBatch.begin();

                    // Set outline color (yellow) and texel size
                    outlineShader.setUniformf("u_outlineColor", 1.0f, 1.0f, 0.0f, 1.0f);
                    float texelSizeX = 1.0f / region.getTexture().getWidth();
                    float texelSizeY = 1.0f / region.getTexture().getHeight();
                    outlineShader.setUniformf("u_texelSize", texelSizeX, texelSizeY);

                    // Draw the sprite
                    if (dragState.isMirrored) {
                        outlineBatch.draw(region, renderX + w, renderY + h, -w, -h);
                    } else {
                        outlineBatch.draw(region, renderX, renderY + h, w, -h);
                    }

                    outlineBatch.end();
                    outlineBatch.setShader(null);
                }
            }
        }

        // Render selected decor outline (click highlight)
        if (dragState == null && selectedDecorInfo != null && outlineShader != null && outlineShader.isCompiled()) {
            renderSelectedDecorOutline();
        }
        // Object outlines redraw the object sprite via the outline shader, so suppress them
        // when objects are hidden (except in the object editor, where they must stay editable).
        boolean showObjectOutlines = objectsVisible || editorMode == EditorMode.OBJECT_POSITION_EDITOR;
        if (outlineShader != null && outlineShader.isCompiled() && showObjectOutlines) {
            if (hoveredObjectPositionIndex >= 0) {
                renderObjectOutline(hoveredObjectPositionIndex, 0.2f, 0.9f, 1.0f, 1.0f);
            }
            if (selectedObjectPositionIndex >= 0) {
                renderObjectOutline(selectedObjectPositionIndex, 1.0f, 1.0f, 0.0f, 1.0f);
            }
        }

        // Render UI
        uiBatch.begin();

        // Render toolbar first
        uiBatch.end();
        renderToolbar();
        renderGroundSelect();
        renderMinimap();
        renderContextBar();
        renderTeleportEditorPanel();
        renderObjectPositionEditorPanel();
        renderTeleportContextMenu();
        renderSpawnContextMenu();
        renderObjectContextMenu();
        renderDecorContextMenu();
        // Render sprite picker UI
        if (editorMode == EditorMode.SPRITE_PICKER && spritePicker != null) {
            spritePicker.render(uiBatch, shapeRenderer);
        }
        if (entityPicker != null) {
            entityPicker.render(uiBatch, shapeRenderer);
        }
        if (collisionRuleEditor != null) {
            collisionRuleEditor.render(uiBatch, shapeRenderer);
        }
        if (decorLayerRuleEditor != null) {
            decorLayerRuleEditor.render(uiBatch, shapeRenderer);
        }
        if (clanRelationsEditor != null) {
            clanRelationsEditor.render(uiBatch, shapeRenderer);
        }
        if (objectMappingsEditor != null) {
            objectMappingsEditor.render(uiBatch, shapeRenderer);
        }
        if (spellEditor != null) {
            spellEditor.render(uiBatch, shapeRenderer);
        }
        if (monsterDefEditor != null) {
            monsterDefEditor.render(uiBatch, shapeRenderer);
        }
        if (npcDefEditor != null) {
            npcDefEditor.render(uiBatch, shapeRenderer);
        }
        if (itemEditor != null) {
            itemEditor.render(uiBatch, shapeRenderer);
        }

        // Render the background-operation progress bar, then the editor message
        // on top of everything.
        uiBatch.begin();
        renderRegenerationProgress(uiBatch);
        renderEditorMessage(uiBatch);
        uiBatch.end();
    }

    private boolean isModalEditorOpen() {
        return collisionRuleEditor != null
                || decorLayerRuleEditor != null
                || clanRelationsEditor != null
                || objectMappingsEditor != null
                || spellEditor != null
                || monsterDefEditor != null
                || npcDefEditor != null
                || itemEditor != null
                || teleportGotoDialog != null;
    }

    private void processLoadingSteps() {
        if (!isLoading && !spriteHotReloading) {
            return;
        }

        if (mapSwitchLoading) {
            processMapSwitchLoadingSteps();
            return;
        }
        if (spriteHotReloading) {
            processSpriteHotReloadSteps();
            return;
        }

        try {
            switch (loadingStep) {
                case 0 -> {
                    loadingMessage = "Discovering maps...";
                    loadAvailableMaps();
                    loadCameraPosition();
                    syncCurrentMapIndex();
                    advanceLoadingStep();
                }
                case 1 -> {
                    loadingMessage = "Loading sprites...";
                    spriteLoader.loadSpriteBin(Paths.SPRITE_BIN);
                    lastSpritesBinModified = getSpritesBinLastModified();
                    spritesBinWatchEnabledAt = TimeUtils.millis() + SPRITES_WATCH_STARTUP_DELAY_MS;
                    advanceLoadingStep();
                }
                case 2 -> {
                    loadingMessage = "Loading shaders...";
                    loadShader();
                    advanceLoadingStep();
                }
                case 3 -> {
                    loadingMessage = "Loading map...";
                    loadMap(currentMapPath);
                    advanceLoadingStep();
                }
                case 4 -> {
                    loadingMessage = "Initializing renderer...";
                    initRendererFromLoadedData();
                    registerReloadListener();
                    advanceLoadingStep();
                }
                case 5 -> {
                    loadingMessage = "Loading editor data...";
                    buildGroundFillSprites();
                    undoStack.clear();
                    mapDirty = false;
                    collisionDirty = false;
                    initializeToolbar();
                    loadMonsterTypes();
                    loadMonsterSpawns();
                    loadNpcTypes();
                    loadNpcSpawns();
                    monstersDirty = false;
                    npcsDirty = false;
                    loadMusicZones();
                    musicZonesDirty = false;
                    loadTeleports();
                    teleportsDirty = false;
                    loadObjectPositions();
                    objectPositionsDirty = false;
                    advanceLoadingStep();
                }
                case 6 -> {
                    loadingMessage = "Preloading teleport maps...";
                    processTeleportPreviewPreloadStep();
                }
                case 7 -> {
                    loadingMessage = "Preloading sprites...";
                    processSpritePreloadStep();
                }
                case 8 -> {
                    loadingMessage = "Preparing first view...";
                    processInitialViewWarmup();
                    advanceLoadingStep();
                }
                default -> {
                    loadingMessage = "Ready";
                    loadingProgress = 1f;
                    isLoading = false;
                    spriteHotReloading = false;
                }
            }
        } catch (GameException e) {
            log.error("Failed during loading step {}", loadingStep, e);
            showEditorMessage("Error: Failed to load editor");
            loadingMessage = e.getMessage();
            spriteHotReloading = false;
        }
    }

    private void processSpritePreloadStep() {
        if (preloadSprites == null) {
            preloadSprites = spriteLoader.getSprites();
            cachedSpriteList = new ArrayList<>(preloadSprites.size());
            cachedSpriteNames = new HashSet<>();
            preloadSpriteIndex = 0;
        }

        int total = preloadSprites.size();
        int budget = 3000;
        while (preloadSpriteIndex < total && budget-- > 0) {
            SpriteLoader.Sprite sprite = preloadSprites.get(preloadSpriteIndex);
            String name = sprite.getName();
            if (name != null && cachedSpriteNames.add(name.toLowerCase(Locale.ROOT))) {
                cachedSpriteList.add(new SpritePickerUI.SpriteEntry(name, null, sprite.isGround()));
            }
            preloadSpriteIndex++;
        }

        float stepProgress = total == 0 ? 1f : (preloadSpriteIndex / (float) total);
        updateLoadingProgress(stepProgress);

        if (preloadSpriteIndex >= total) {
            cachedSpriteList.sort(Comparator.comparing(e -> e.name));
            log.info("Prepared {} sprites for sprite picker", cachedSpriteList.size());
            cachedSpriteNames = null;
            advanceLoadingStep();
        }
    }

    private void processTeleportPreviewPreloadStep() {
        startTeleportPreviewPreload();
        int total = teleportPreviewFutures.size();
        if (total == 0) {
            advanceLoadingStep();
            return;
        }

        int completed = 0;
        List<Integer> ready = new ArrayList<>();
        for (Map.Entry<Integer, Future<TeleportPreviewData>> entry : teleportPreviewFutures.entrySet()) {
            if (teleportPreviewTextures.containsKey(entry.getKey())) {
                completed++;
                continue;
            }
            Future<TeleportPreviewData> future = entry.getValue();
            if (future.isDone()) {
                ready.add(entry.getKey());
            }
        }

        for (Integer z : ready) {
            Future<TeleportPreviewData> future = teleportPreviewFutures.get(z);
            try {
                TeleportPreviewData data = future.get();
                teleportPreviewDimensions.put(data.z, new int[] { data.width, data.height });
                Texture texture = new Texture(data.pixmap);
                data.pixmap.dispose();
                teleportPreviewTextures.put(data.z, texture);
                completed++;
            } catch (Exception e) {
                log.warn("Failed to preload teleport preview for z{}", z, e);
                teleportPreviewDimensions.put(z, new int[] { 0, 0 });
                Pixmap fallback = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                fallback.setColor(minimapColorForSprite(null));
                fallback.drawPixel(0, 0);
                Texture texture = new Texture(fallback);
                fallback.dispose();
                teleportPreviewTextures.put(z, texture);
                completed++;
            }
        }

        updateLoadingProgress(completed / (float) total);
        if (completed >= total) {
            advanceLoadingStep();
        }
    }

    private void startTeleportPreviewPreload() {
        if (!teleportPreviewFutures.isEmpty()) {
            return;
        }
        for (MapDefinition def : MapDefinition.values()) {
            int z = def.getZ();
            teleportPreviewFutures.put(z, teleportPreviewExecutor.submit(() -> loadTeleportPreviewData(def)));
        }
    }

    private TeleportPreviewData loadTeleportPreviewData(MapDefinition def) throws GameException {
        try (MapReader reader = new MapReader(new File(def.getMapPath()))) {
            Pixmap pixmap = createMapPreviewPixmap(reader, 260);
            return new TeleportPreviewData(def.getZ(), reader.getWidth(), reader.getHeight(), pixmap);
        }
    }

    private void startMapSwitchLoading(String nextMapPath, int index) {
        currentMapIndex = index;
        pendingMapPath = normalizeMapPath(nextMapPath);
        loadingMessage = "Switching map...";
        loadingProgress = 0f;
        loadingStep = 0;
        loadingStepCount = 5;
        loadingOverlayPresented = false;
        mapSwitchLoading = true;
        isLoading = true;
    }

    private void processMapSwitchLoadingSteps() {
        try {
            switch (loadingStep) {
                case 0 -> {
                    loadingMessage = "Saving current map data...";
                    saveAllState();
                    advanceLoadingStep();
                }
                case 1 -> {
                    loadingMessage = "Loading map: " + getMapDisplayName(pendingMapPath);
                    currentMapPath = pendingMapPath;
                    loadMap(currentMapPath);
                    advanceLoadingStep();
                }
                case 2 -> {
                    loadingMessage = "Initializing renderer...";
                    initRendererFromLoadedData();
                    advanceLoadingStep();
                }
                case 3 -> {
                    loadingMessage = "Loading map sidecars...";
                    buildGroundFillSprites();
                    undoStack.clear();
                    mapDirty = false;
                    collisionDirty = false;
                    collisionData = null;
                    loadMonsterTypes();
                    loadMonsterSpawns();
                    loadNpcTypes();
                    loadNpcSpawns();
                    monstersDirty = false;
                    npcsDirty = false;
                    loadMusicZones();
                    musicZonesDirty = false;
                    if (editorMode == EditorMode.COLLISION_EDITOR) {
                        loadCollisionData();
                    }
                    resetMapSelections();
                    if (pendingTeleportAfterMapSwitch) {
                        int tx = clampTileX(pendingTeleportTileX);
                        int ty = clampTileY(pendingTeleportTileY);
                        cameraPosition.set(tx * GameConstants.GRID_W, ty * GameConstants.GRID_H);
                        pendingTeleportAfterMapSwitch = false;
                        showEditorMessage("Teleported to (" + tx + ", " + ty + ", z" + getCurrentMapZ() + ")");
                    } else {
                        cameraPosition.set(mapReader.getWidth() * GameConstants.GRID_W / 2f,
                                mapReader.getHeight() * GameConstants.GRID_H / 2f);
                    }
                    advanceLoadingStep();
                }
                case 4 -> {
                    loadingMessage = "Preparing minimap...";
                    minimapDirty = true;
                    ensureMinimapTexture();
                    showEditorMessage("Switched to map: " + getMapDisplayName(currentMapPath));
                    advanceLoadingStep();
                }
                default -> finishMapSwitchLoading();
            }
        } catch (Exception e) {
            log.error("Failed to switch map", e);
            showEditorMessage("Error: Failed to switch map");
            finishMapSwitchLoading();
        }
    }

    private void finishMapSwitchLoading() {
        pendingMapPath = null;
        mapSwitchLoading = false;
        isLoading = false;
        loadingProgress = 1f;
        loadingOverlayPresented = false;
    }

    private void advanceLoadingStep() {
        loadingStep++;
        updateLoadingProgress(0f);
        if (loadingStep >= loadingStepCount) {
            loadingProgress = 1f;
            if (mapSwitchLoading) {
                finishMapSwitchLoading();
            } else {
                isLoading = false;
                spriteHotReloading = false;
            }
        }
    }

    private void updateLoadingProgress(float stepProgress) {
        float base = Math.min(loadingStep, loadingStepCount) / (float) loadingStepCount;
        float slice = 1f / loadingStepCount;
        loadingProgress = Math.min(1f, base + slice * stepProgress);
    }

    public void requestExitWithSaveScreen() {
        if (exitSaving) {
            return;
        }
        restoreTransientDragState();
        buildExitSaveSteps();
        if (exitSaveSteps.isEmpty()) {
            exitSaveCompleted = true;
            Gdx.app.exit();
            return;
        }
        exitSaving = true;
        exitSaveCompleted = false;
        exitSaveStepPrepared = false;
        exitSaveStepIndex = 0;
        loadingMessage = "Preparing save...";
        loadingProgress = 0f;
        loadingStep = 0;
        loadingStepCount = exitSaveSteps.size();
        loadingOverlayPresented = false;
    }

    public boolean isExitSaveCompleted() {
        return exitSaveCompleted;
    }

    private void buildExitSaveSteps() {
        exitSaveSteps.clear();
        if (isCameraSaveNeeded()) {
            exitSaveSteps.add(new SaveStep("Saving camera position...", this::saveCameraPosition));
        }
        if (collisionDirty) {
            exitSaveSteps.add(new SaveStep("Saving collision data...", this::saveCollisionData));
        }
        if (hasPendingOffsetWrites()) {
            exitSaveSteps.add(new SaveStep("Saving sprite offsets...", this::flushPendingOffsetWritesBlocking));
        }
        if (mapDirty) {
            exitSaveSteps.add(new SaveStep("Saving map data...", this::saveMapData));
        }
        if (monstersDirty) {
            exitSaveSteps.add(new SaveStep("Saving monster spawns...", this::saveMonsterSpawns));
        }
        if (npcsDirty) {
            exitSaveSteps.add(new SaveStep("Saving NPC spawns...", this::saveNpcSpawns));
        }
        if (musicZonesDirty) {
            exitSaveSteps.add(new SaveStep("Saving music zones...", this::saveMusicZones));
        }
        if (teleportsDirty) {
            exitSaveSteps.add(new SaveStep("Saving teleports...", this::saveTeleports));
        }
        if (objectPositionsDirty) {
            exitSaveSteps.add(new SaveStep("Saving object positions...", this::saveObjectPositions));
        }
        if (decorLayerRulesDirty) {
            exitSaveSteps.add(new SaveStep("Saving decor layer rules...", this::saveDecorLayerRules));
        }
    }

    private boolean isCameraSaveNeeded() {
        return cameraPosition.x != lastSavedCameraX
                || cameraPosition.y != lastSavedCameraY
                || zoom != lastSavedCameraZoom
                || !Objects.equals(currentMapPath, lastSavedCameraMapPath);
    }

    private boolean hasPendingOffsetWrites() {
        synchronized (offsetWriteLock) {
            return pendingOffsetDirty || !pendingOffsetWrites.isEmpty();
        }
    }

    private void prepareExitSaveStepOverlay() {
        if (exitSaveStepIndex >= exitSaveSteps.size()) {
            loadingMessage = "Save complete";
            loadingProgress = 1f;
        } else {
            SaveStep step = exitSaveSteps.get(exitSaveStepIndex);
            loadingMessage = step.label;
            float baseProgress = exitSaveStepIndex / (float) exitSaveSteps.size();
            float stepVisibleProgress = Math.min(0.98f, baseProgress + (0.18f / exitSaveSteps.size()));
            loadingProgress = Math.max(baseProgress, stepVisibleProgress);
        }
        exitSaveStepPrepared = true;
    }

    private void processExitSaveStep() {
        if (exitSaveStepIndex >= exitSaveSteps.size()) {
            loadingMessage = "Save complete";
            loadingProgress = 1f;
            exitSaving = false;
            exitSaveCompleted = true;
            Gdx.app.exit();
            return;
        }
        SaveStep step = exitSaveSteps.get(exitSaveStepIndex);
        try {
            step.action.run();
        } catch (Throwable t) {
            log.error("Failed while {}", step.label, t);
            loadingMessage = "Save failed: " + step.label;
        }
        exitSaveStepIndex++;
        loadingProgress = exitSaveStepIndex / (float) exitSaveSteps.size();
        exitSaveStepPrepared = false;
    }

    private void renderLoadingOverlay() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
        uiMatrix.setToOrtho2D(0, 0, screenWidth, screenHeight);
        shapeRenderer.setProjectionMatrix(uiMatrix);
        uiBatch.setProjectionMatrix(uiMatrix);

        float panelWidth = Math.min(560f, screenWidth - 48f);
        float panelHeight = 190f;
        float panelX = (screenWidth - panelWidth) / 2f;
        float panelY = (screenHeight - panelHeight) / 2f;
        float barWidth = panelWidth - 56f;
        float barHeight = 14f;
        float barX = (screenWidth - barWidth) / 2f;
        float barY = panelY + 58f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UI_PANEL_DARK);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.setColor(UI_PANEL_DARK_2);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.setColor(UI_BLUE);
        shapeRenderer.rect(panelX, panelY + panelHeight - 4f, panelWidth, 4f);
        shapeRenderer.setColor(0.04f, 0.055f, 0.07f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.setColor(UI_BLUE);
        shapeRenderer.rect(barX + 2, barY + 2, (barWidth - 4) * Math.max(0f, loadingProgress), barHeight - 4);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UI_BORDER);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.end();

        uiBatch.begin();
        font.setColor(UI_TEXT_LIGHT);
        GlyphLayout titleLayout = new GlyphLayout(font, "T4C Map Editor");
        font.draw(uiBatch, titleLayout, panelX + 28f, panelY + panelHeight - 34f);
        font.setColor(UI_TEXT_FAINT);
        String stage = exitSaving ? "Saving editor state"
                : mapSwitchLoading ? "Switching editor map"
                : (spriteHotReloading ? "Reloading sprite resources" : "Preparing editor workspace");
        font.draw(uiBatch, stage, panelX + 28f, panelY + panelHeight - 62f);
        font.setColor(UI_TEXT_LIGHT);
        String text = String.format(Locale.ROOT, "%s", loadingMessage);
        font.draw(uiBatch, text, panelX + 28f, barY + 42f);
        font.setColor(UI_TEXT_FAINT);
        String detail = getLoadingDetailText();
        font.draw(uiBatch, detail, panelX + 28f, panelY + 30f);
        font.setColor(UI_TEXT_FAINT);
        String pct = String.format(Locale.ROOT, "%.0f%%", loadingProgress * 100f);
        GlyphLayout pctLayout = new GlyphLayout(font, pct);
        font.draw(uiBatch, pct, panelX + panelWidth - pctLayout.width - 28f, barY + 42f);
        uiBatch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private String getLoadingDetailText() {
        if (mapSwitchLoading) {
            return switch (loadingStep) {
                case 0 -> "Saving pending edits before changing map";
                case 1 -> "Reading selected map tiles";
                case 2 -> "Rebuilding renderer caches";
                case 3 -> "Loading collisions, spawns, NPCs, monsters, and music zones";
                case 4 -> "Preparing minimap preview";
                default -> "Ready";
            };
        }
        return switch (loadingStep) {
            case 0 -> "Reading saved editor state and available map files";
            case 1 -> "Reading sprites.bin metadata and PNG payloads";
            case 2 -> "Compiling editor shaders";
            case 3 -> "Reading map tiles and metadata";
            case 4 -> "Building renderer pipelines and sprite mappings";
            case 5 -> "Loading spawns, NPCs, monsters, and music zones";
            case 6 -> "Preloading teleport map previews";
            case 7 -> "Indexing sprite library " + preloadSpriteIndex + " / "
                    + (preloadSprites != null ? preloadSprites.size() : spriteLoader.getSprites().size());
            case 8 -> "Warming visible terrain, decor, and minimap caches";
            default -> "Ready";
        };
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {
    }

    private void processInitialViewWarmup() {
        updateLoadingProgress(0.15f);
        if (mapRenderer == null || mapReader == null) {
            updateLoadingProgress(1f);
            return;
        }

        camera.position.set(cameraPosition.x, cameraPosition.y, 0f);
        camera.zoom = zoom;
        camera.update();

        int[] bounds = calculateVisibleBounds(4);
        int startX = bounds[0];
        int endX = bounds[1];
        int startY = bounds[2];
        int endY = bounds[3];

        loadingMessage = "Preparing terrain cache...";
        updateLoadingProgress(0.35f);
        mapRenderer.warmCaches(startX, endX, startY, endY, 64, 0, 0);

        loadingMessage = "Preparing decor cache...";
        updateLoadingProgress(0.70f);
        mapRenderer.warmCaches(startX, endX, startY, endY, 0, 0, 1200);

        loadingMessage = "Preparing minimap...";
        updateLoadingProgress(0.90f);
        ensureMinimapTexture();
        updateLoadingProgress(1f);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        saveCameraPosition();
    }

    public void saveAllState() {
        restoreTransientDragState();
        saveCameraPosition();
        if (collisionDirty) {
            saveCollisionData();
        }
        flushPendingOffsetWritesBlocking();
        if (mapDirty) {
            saveMapData();
        }
        if (monstersDirty) {
            saveMonsterSpawns();
        }
        if (npcsDirty) {
            saveNpcSpawns();
        }
        if (musicZonesDirty) {
            saveMusicZones();
        }
        if (teleportsDirty) {
            saveTeleports();
        }
        if (objectPositionsDirty) {
            saveObjectPositions();
        }
    }

    private void restoreTransientDragState() {
        if (dragState != null) {
            cancelDragDecor();
        }
    }

    @Override
    public void dispose() {
        saveCameraPosition();
        offsetWriteExecutor.shutdownNow();
        collisionGenerationExecutor.shutdownNow();
        tmpl3RegenerationExecutor.shutdownNow();
        groundRecalculationExecutor.shutdownNow();
        teleportPreviewExecutor.shutdownNow();
        if (mapReader != null) {
            mapReader.close();
        }
        if (minimapTexture != null) {
            minimapTexture.dispose();
            minimapTexture = null;
        }
        for (Texture texture : teleportPreviewTextures.values()) {
            texture.dispose();
        }
        teleportPreviewTextures.clear();
        teleportPreviewDimensions.clear();

        if (ownsBatch) {
            batch.dispose();
        }
        uiBatch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }

    /**
     * Sprite selection interface
     */
    private class SpritePickerUI extends EditorDialog {
        private static final int CELL_SIZE = 112;
        private static final int PREVIEW_SIZE = 90;
        private static final int PANEL_PADDING = 18;
        private static final int HEADER_HEIGHT = 58;
        private static final int HISTORY_HEIGHT = 92;
        private static final int HISTORY_GAP = 12;
        private static final int HISTORY_SLOT_GAP = 12;
        private static final int SEARCH_HEIGHT = 34;
        private static final int SEARCH_GAP = 10;
        private static final int GRID_TOP_PADDING = 8;
        private static final int FOOTER_HEIGHT = 48;

        private final List<SpriteEntry> allSprites;
        private List<SpriteEntry> filteredSprites;
        private int scrollOffset = 0;
        private int selectedIndex = -1;
        private final String currentTileName;
        private final int tileX;
        private final int tileY;
        private String searchFilter = "";
        private static final int PREVIEW_LOAD_BUDGET_PER_FRAME = 6;
/**
 * Class representing SpriteEntry.
 */

        static class SpriteEntry {
            String name;
            TextureRegion preview;
            boolean isGround;

            SpriteEntry(String name, TextureRegion preview, boolean isGround) {
                this.name = name;
                this.preview = preview;
                this.isGround = isGround;
            }
        }

        SpritePickerUI(List<SpriteEntry> preloadedSprites, String currentTileName, int tileX, int tileY) {
            super("Sprite Library");
            this.currentTileName = currentTileName;
            this.tileX = tileX;
            this.tileY = tileY;
            this.allSprites = preloadedSprites;

            filteredSprites = new ArrayList<>(allSprites);
        }

        public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
            int screenWidth = Gdx.graphics.getWidth();
            int screenHeight = Gdx.graphics.getHeight();
            Layout layout = layout(screenWidth, screenHeight);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(
                    shapeRenderer.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(shapeRenderer, screenWidth, screenHeight);
            EditorPanelChrome.panel(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.panelX, layout.panelY, layout.panelWidth,
                            layout.panelHeight),
                    EditorTheme.ORANGE, HEADER_HEIGHT);
            EditorPanelChrome.surface(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.innerX, layout.historyBottom, layout.innerWidth,
                            HISTORY_HEIGHT));
            EditorPanelChrome.textField(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.innerX, layout.searchBottom, layout.innerWidth,
                            SEARCH_HEIGHT),
                    true);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.panelX, layout.panelY, layout.panelWidth,
                            layout.panelHeight));
            EditorPanelChrome.border(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.innerX, layout.historyBottom, layout.innerWidth,
                            HISTORY_HEIGHT));
            EditorPanelChrome.border(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.innerX, layout.searchBottom, layout.innerWidth,
                            SEARCH_HEIGHT));
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < SPRITE_HISTORY_SIZE; i++) {
                float slotX = layout.innerX + i * (layout.historySlotSize + HISTORY_SLOT_GAP);
                float slotY = layout.historySlotY;
                EditorPanelChrome.surface(shapeRenderer,
                        new com.badlogic.gdx.math.Rectangle(slotX, slotY, layout.historySlotSize,
                                layout.historySlotSize));
            }

            int visibleRows = layout.visibleRows;
            int startIdx = scrollOffset * layout.gridCols;
            int endIdx = Math.min(startIdx + visibleRows * layout.gridCols, filteredSprites.size());

            for (int i = startIdx; i < endIdx; i++) {
                int col = (i - startIdx) % layout.gridCols;
                int row = (i - startIdx) / layout.gridCols;

                int cellX = layout.innerX + col * CELL_SIZE;
                int cellY = layout.gridTop - (row + 1) * CELL_SIZE;

                com.badlogic.gdx.math.Rectangle cellBounds = new com.badlogic.gdx.math.Rectangle(
                        cellX + 5, cellY + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                if (i == selectedIndex) {
                    EditorPanelChrome.selectedRow(shapeRenderer, cellBounds);
                } else {
                    EditorPanelChrome.surface(shapeRenderer, cellBounds);
                }
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            for (int i = startIdx; i < endIdx; i++) {
                int col = (i - startIdx) % layout.gridCols;
                int row = (i - startIdx) / layout.gridCols;
                int cellX = layout.innerX + col * CELL_SIZE;
                int cellY = layout.gridTop - (row + 1) * CELL_SIZE;
                EditorPanelChrome.border(shapeRenderer,
                        new com.badlogic.gdx.math.Rectangle(cellX + 5, cellY + 5, CELL_SIZE - 10,
                                CELL_SIZE - 10));
            }
            shapeRenderer.end();

            batch.setProjectionMatrix(batch.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
            batch.begin();
            batch.setColor(Color.WHITE);
            float oldScaleX = font.getData().scaleX;
            float oldScaleY = font.getData().scaleY;

            font.getData().setScale(1f);
            font.setColor(EditorTheme.TEXT_LIGHT);
            font.draw(batch, "Sprite Library", layout.innerX, layout.headerBottom + HEADER_HEIGHT - 16);
            String currentNameLabel = currentTileName == null || currentTileName.isBlank() ? "Empty" : currentTileName;
            font.setColor(EditorTheme.TEXT_MUTED);
            font.draw(batch, "Tile " + tileX + ", " + tileY + "  /  Current " + shorten(currentNameLabel, 44), layout.innerX,
                    layout.headerBottom + HEADER_HEIGHT - 35);

            font.setColor(EditorTheme.TEXT_MUTED);
            font.draw(batch, "HISTORY", layout.innerX + 4, layout.historyTop - 10);

            int historyIndex = 0;
            for (String rawName : recentSpriteHistory) {
                if (historyIndex >= SPRITE_HISTORY_SIZE)
                    break;
                String name = rawName == null ? null : rawName.trim();
                if (name == null || name.isBlank()) {
                    historyIndex++;
                    continue;
                }
                ResolvedSprite parsed = SpriteNameParser.parse(name,
                        mapRenderer != null ? mapRenderer.getMetaByName() : null);
                if (parsed == null || parsed.name == null) {
                    historyIndex++;
                    continue;
                }
                TextureRegion region = spriteLoader.getRegionFromSpriteName(parsed.name);
                if (region != null) {
                    float slotX = layout.innerX + historyIndex * (layout.historySlotSize + HISTORY_SLOT_GAP);
                    float slotY = layout.historySlotY;
                    float scale = Math.min((layout.historySlotSize - 10) / (float) region.getRegionWidth(),
                            (layout.historySlotSize - 10) / (float) region.getRegionHeight());
                    float w = region.getRegionWidth() * scale;
                    float h = region.getRegionHeight() * scale;
                    float px = slotX + (layout.historySlotSize - w) / 2;
                    float py = slotY + (layout.historySlotSize - h) / 2;
                    if (parsed.mirror) {
                        batch.draw(region, px + w, py, -w, h);
                    } else {
                        batch.draw(region, px, py, w, h);
                    }
                }
                historyIndex++;
            }

            // Draw sprite previews. Labels are intentionally omitted to keep the grid clean.
            int previewBudget = PREVIEW_LOAD_BUDGET_PER_FRAME;
            for (int i = startIdx; i < endIdx; i++) {
                int col = (i - startIdx) % layout.gridCols;
                int row = (i - startIdx) / layout.gridCols;

                int cellX = layout.innerX + col * CELL_SIZE;
                int cellY = layout.gridTop - (row + 1) * CELL_SIZE;
                float contentPadding = 8f;
                float contentX = cellX + 5f + contentPadding;
                float contentY = cellY + 5f + contentPadding;
                float contentSize = CELL_SIZE - 10f - contentPadding * 2f;

                SpriteEntry entry = filteredSprites.get(i);

                if (entry.preview == null && previewBudget > 0) {
                    try {
                        entry.preview = spriteLoader.getRegionFromSpriteName(entry.name);
                    } catch (Throwable ignored) {
                        entry.preview = null;
                    }
                    previewBudget--;
                }

                if (entry.preview != null) {
                    float scale = Math.min(contentSize / (float) entry.preview.getRegionWidth(),
                            contentSize / (float) entry.preview.getRegionHeight());
                    float w = entry.preview.getRegionWidth() * scale;
                    float h = entry.preview.getRegionHeight() * scale;
                    float px = contentX + (contentSize - w) * 0.5f;
                    float py = contentY + (contentSize - h) * 0.5f;

                    batch.draw(entry.preview, px, py, w, h);
                }
            }

            font.setColor(EditorTheme.TEXT_MUTED);
            font.draw(batch, "Type to search  /  Wheel to scroll  /  Esc to close", layout.innerX,
                    layout.panelY + 34);
            String searchText = searchFilter.isEmpty() ? "Search..." : searchFilter;
            font.setColor(searchFilter.isEmpty() ? EditorTheme.TEXT_FAINT : EditorTheme.TEXT);
            font.draw(batch, searchText, layout.innerX + 10, layout.searchBottom + 22);

            font.setColor(EditorTheme.TEXT_MUTED);
            font.draw(batch, "Showing " + filteredSprites.size() + " / " + allSprites.size() + " sprites",
                    layout.innerX, layout.panelY + 14);
            font.getData().setScale(oldScaleX, oldScaleY);

            batch.end();
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT)
                return false;

            int screenWidth = Gdx.graphics.getWidth();
            int screenHeight = Gdx.graphics.getHeight();
            Layout layout = layout(screenWidth, screenHeight);

            // Convert screen Y to libGDX coordinates (bottom-left origin)
            int y = screenHeight - screenY;

            // Check if click is inside panel
            if (screenX < layout.panelX || screenX > layout.panelX + layout.panelWidth || y < layout.panelY
                    || y > layout.panelY + layout.panelHeight) {
                closeSpritePicker();
                return true;
            }

            // History clicks
            if (y >= layout.historySlotY && y <= layout.historySlotY + layout.historySlotSize) {
                float relX = screenX - layout.innerX;
                int idx = (int) (relX / (layout.historySlotSize + HISTORY_SLOT_GAP));
                if (idx >= 0 && idx < SPRITE_HISTORY_SIZE) {
                    List<String> history = new ArrayList<>(recentSpriteHistory);
                    if (idx < history.size()) {
                        applySpriteName(history.get(idx));
                        return true;
                    }
                }
            }

            // Check if click is in grid area
            if (y < layout.gridBottom || y > layout.gridTop)
                return true;

            int visibleRows = layout.visibleRows;
            int startIdx = scrollOffset * layout.gridCols;

            // Calculate which cell was clicked
            int relX = screenX - layout.innerX;
            int relY = layout.gridTop - y; // Distance from top of grid
            int col = relX / CELL_SIZE;
            int row = relY / CELL_SIZE;

            if (col >= 0 && col < layout.gridCols && row >= 0 && row < visibleRows) {
                int idx = startIdx + row * layout.gridCols + col;
                if (idx >= 0 && idx < filteredSprites.size()) {
                    selectedIndex = idx;
                    applySpriteSelection();
                    return true;
                }
            }

            return true;
        }

        public boolean handleScroll(float amount) {
            scrollOffset += (int) amount;
            scrollOffset = Math.max(0, scrollOffset);

            Layout layout = layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int maxRows = (filteredSprites.size() + layout.gridCols - 1) / layout.gridCols;
            int visibleRows = layout.visibleRows;
            scrollOffset = Math.min(scrollOffset, Math.max(0, maxRows - visibleRows));

            return true;
        }

        public boolean handleKeyTyped(char character) {
            if (character == '\b') {
                if (!searchFilter.isEmpty()) {
                    searchFilter = searchFilter.substring(0, searchFilter.length() - 1);
                    updateFilter();
                }
                return true;
            } else if (character == '\r' || character == '\n') {
                if (!filteredSprites.isEmpty()) {
                    selectedIndex = 0;
                    applySpriteSelection();
                }
                return true;
            } else if (Character.isLetterOrDigit(character) || character == '_' || character == '-') {
                searchFilter += character;
                updateFilter();
                return true;
            }
            return false;
        }

        private void updateFilter() {
            if (searchFilter.isEmpty()) {
                filteredSprites = new ArrayList<>(allSprites);
            } else {
                filteredSprites = new ArrayList<>();
                String lowerFilter = searchFilter.toLowerCase();
                for (SpriteEntry entry : allSprites) {
                    if (entry.name.toLowerCase().contains(lowerFilter)) {
                        filteredSprites.add(entry);
                    }
                }
            }
            scrollOffset = 0;
            selectedIndex = -1;
        }

        private void applySpriteSelection() {
            if (selectedIndex < 0 || selectedIndex >= filteredSprites.size())
                return;

            SpriteEntry selected = filteredSprites.get(selectedIndex);
            applySpriteName(selected.name);
        }

        private void applySpriteName(String spriteName) {
            if (spriteName == null || spriteName.isBlank()) {
                return;
            }
            String trimmed = spriteName.trim();
            pushSpriteHistory(trimmed);

            UndoEntry undoEntry = newUndoEntry("Sprite", true);
            float scale = 1f;
            int[] dims = spriteLoader.getSpriteDimensions(trimmed);
            if (dims != null && dims.length == 2 && dims[0] == 1024 && dims[1] == 1024) {
                scale = 0.25f;
            }
            if (!decorVisible && isDecorTile(tileX, tileY) && !isGroundSpriteName(trimmed)) {
                showEditorMessage("Decorations hidden: select a ground sprite");
                return;
            }
            if (!decorVisible && isDecorTile(tileX, tileY)) {
                applyGroundLayerChange(tileX, tileY, trimmed, undoEntry);
                pushUndoEntry(undoEntry);
                log.info("Set ground layer ({}, {}) to sprite {}", tileX, tileY, trimmed);
                invalidateGroundAround(tileX, tileY, trimmed);
                closeSpritePicker();
                return;
            }
            if (!isGroundSpriteName(trimmed)) {
                if (!decorVisible) {
                    showEditorMessage("Decorations hidden: select a ground sprite");
                    return;
                }
                applyDecorChange(tileX, tileY, trimmed, scale, scale, 0f, 0f,
                        mapReader.getZOrder(tileX, tileY), undoEntry);
                pushUndoEntry(undoEntry);
                log.info("Set decor layer ({}, {}) to sprite {}", tileX, tileY, trimmed);
                invalidateGroundAround(tileX, tileY, trimmed);
                closeSpritePicker();
                return;
            }
            String appliedName = applyHiddenDecorGroundSelection(tileX, tileY, trimmed);
            if (!Objects.equals(appliedName, trimmed)) {
                applyTileChangeWithOffsetAndOrder(
                        tileX,
                        tileY,
                        appliedName,
                        mapReader.getScaleX(tileX, tileY),
                        mapReader.getScaleY(tileX, tileY),
                        mapReader.getOffsetX(tileX, tileY),
                        mapReader.getOffsetY(tileX, tileY),
                        mapReader.getZOrder(tileX, tileY),
                        undoEntry);
            } else {
                applyTileChange(tileX, tileY, appliedName, scale, scale, undoEntry);
            }
            pushUndoEntry(undoEntry);
            log.info("Set tile ({}, {}) to sprite {}", tileX, tileY, appliedName);
            invalidateGroundAround(tileX, tileY, appliedName);
            log.info("Applied sprite '{}' to tile ({}, {})", appliedName, tileX, tileY);
            closeSpritePicker();
        }

        private Layout layout(int screenWidth, int screenHeight) {
            Layout layout = new Layout();
            layout.panelX = 16;
            layout.panelY = 16;
            layout.panelWidth = Math.max(360, screenWidth - 32);
            layout.panelHeight = Math.max(360, screenHeight - 32);
            layout.innerX = layout.panelX + PANEL_PADDING;
            layout.innerWidth = layout.panelWidth - PANEL_PADDING * 2;

            layout.headerTop = layout.panelY + layout.panelHeight;
            layout.headerBottom = layout.headerTop - HEADER_HEIGHT;
            layout.historyTop = layout.headerBottom - HISTORY_GAP;
            layout.historyBottom = layout.historyTop - HISTORY_HEIGHT;
            layout.searchTop = layout.historyBottom - SEARCH_GAP;
            layout.searchBottom = layout.searchTop - SEARCH_HEIGHT;
            layout.gridTop = layout.searchBottom - GRID_TOP_PADDING;
            layout.gridBottom = layout.panelY + FOOTER_HEIGHT;

            float historySlotSize = (layout.innerWidth - (SPRITE_HISTORY_SIZE - 1) * HISTORY_SLOT_GAP)
                    / (float) SPRITE_HISTORY_SIZE;
            layout.historySlotSize = Math.min(historySlotSize, HISTORY_HEIGHT - 24f);
            layout.historySlotY = layout.historyBottom + (HISTORY_HEIGHT - layout.historySlotSize) / 2f;

            float gridHeight = layout.gridTop - layout.gridBottom;
            layout.gridCols = Math.max(3, layout.innerWidth / CELL_SIZE);
            layout.visibleRows = Math.max(1, (int) (gridHeight / CELL_SIZE));
            return layout;
        }
/**
 * Class representing Layout.
 */

        private class Layout {
            int panelX;
            int panelY;
            int panelWidth;
            int panelHeight;
            int innerX;
            int innerWidth;
            int headerTop;
            int headerBottom;
            int historyTop;
            int historyBottom;
            int searchTop;
            int searchBottom;
            int gridTop;
            int gridBottom;
            int visibleRows;
            float historySlotSize;
            float historySlotY;
            int gridCols;
        }

    }
/**
 * Class representing EntityPickerUI.
 */

    private class EntityPickerUI extends EditorDialog {
        private static final int CELL_SIZE = 132;
        private static final int PREVIEW_SIZE = 88;
        private static final int PANEL_PADDING = 18;
        private static final int HEADER_HEIGHT = 58;
        private static final int SEARCH_HEIGHT = 34;
        private static final int FOOTER_HEIGHT = 42;

        private final String title;
        private final boolean monsters;
        private final List<MonsterTypeEntry> allEntries;
        private List<MonsterTypeEntry> filteredEntries;
        private String searchFilter = "";
        private int scrollOffset = 0;
        private int selectedIndex = -1;
        private final Map<String, TextureRegion> previews = new HashMap<>();
        private final Map<String, BaseNPC> npcPreviewCache = new HashMap<>();

        EntityPickerUI(String title, boolean monsters, List<MonsterTypeEntry> entries) {
            super(title);
            this.title = title;
            this.monsters = monsters;
            this.allEntries = new ArrayList<>(entries);
            this.filteredEntries = new ArrayList<>(entries);
        }

        public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
            int screenWidth = Gdx.graphics.getWidth();
            int screenHeight = Gdx.graphics.getHeight();
            EntityLayout layout = layout(screenWidth, screenHeight);

            prepareUiProjection(screenWidth, screenHeight);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(shapeRenderer, screenWidth, screenHeight);
            EditorPanelChrome.panel(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.panelX, layout.panelY, layout.panelWidth,
                            layout.panelHeight),
                    EditorTheme.ORANGE, HEADER_HEIGHT);
            EditorPanelChrome.textField(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.innerX, layout.searchBottom, layout.innerWidth,
                            SEARCH_HEIGHT),
                    false);

            int startIdx = scrollOffset * layout.gridCols;
            int endIdx = Math.min(startIdx + layout.visibleRows * layout.gridCols, filteredEntries.size());
            for (int i = startIdx; i < endIdx; i++) {
                int col = (i - startIdx) % layout.gridCols;
                int row = (i - startIdx) / layout.gridCols;
                int cellX = layout.innerX + col * CELL_SIZE;
                int cellY = layout.gridTop - (row + 1) * CELL_SIZE;
                shapeRenderer.setColor(i == selectedIndex ? UI_SURFACE_HOVER : UI_SURFACE);
                shapeRenderer.rect(cellX + 5, cellY + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                if (i == selectedIndex) {
                    shapeRenderer.setColor(UI_BLUE);
                    shapeRenderer.rect(cellX + 5, cellY + 5, CELL_SIZE - 10, 4f);
                }
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.panelX, layout.panelY, layout.panelWidth,
                            layout.panelHeight));
            EditorPanelChrome.border(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.innerX, layout.searchBottom, layout.innerWidth,
                            SEARCH_HEIGHT));
            for (int i = startIdx; i < endIdx; i++) {
                int col = (i - startIdx) % layout.gridCols;
                int row = (i - startIdx) / layout.gridCols;
                int cellX = layout.innerX + col * CELL_SIZE;
                int cellY = layout.gridTop - (row + 1) * CELL_SIZE;
                shapeRenderer.rect(cellX + 5, cellY + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            }
            shapeRenderer.end();

            batch.setProjectionMatrix(batch.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
            batch.begin();
            batch.setColor(Color.WHITE);
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, title, layout.innerX, layout.headerBottom + HEADER_HEIGHT - 16f);
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, "Select an entry to place multiple spawns. Esc closes.", layout.innerX,
                    layout.headerBottom + HEADER_HEIGHT - 38f);

            String searchText = searchFilter.isEmpty() ? "Search..." : searchFilter;
            font.setColor(searchFilter.isEmpty() ? UI_TEXT_FAINT : UI_TEXT);
            font.draw(batch, searchText, layout.innerX + 10, layout.searchBottom + 23);

            for (int i = startIdx; i < endIdx; i++) {
                int col = (i - startIdx) % layout.gridCols;
                int row = (i - startIdx) / layout.gridCols;
                int cellX = layout.innerX + col * CELL_SIZE;
                int cellY = layout.gridTop - (row + 1) * CELL_SIZE;
                MonsterTypeEntry entry = filteredEntries.get(i);
                if (!monsters && drawNpcCompositePreview(batch, entry, cellX, cellY)) {
                    // Composite NPC preview drawn from its configured body parts.
                } else {
                    TextureRegion preview = previewFor(entry);
                    if (preview != null) {
                    float scale = Math.min(PREVIEW_SIZE / (float) preview.getRegionWidth(),
                            PREVIEW_SIZE / (float) preview.getRegionHeight());
                    float w = preview.getRegionWidth() * scale;
                    float h = preview.getRegionHeight() * scale;
                    batch.draw(preview, cellX + (CELL_SIZE - w) / 2f, cellY + 32f, w, h);
                    }
                }
                font.setColor(UI_TEXT_MUTED);
                font.draw(batch, shorten(entry.displayName, 17), cellX + 10f, cellY + 18f);
            }

            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, "Showing " + filteredEntries.size() + " / " + allEntries.size(), layout.innerX,
                    layout.panelY + 18f);
            batch.end();
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT) {
                return false;
            }
            EntityLayout layout = layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int y = Gdx.graphics.getHeight() - screenY;
            if (screenX < layout.panelX || screenX > layout.panelX + layout.panelWidth
                    || y < layout.panelY || y > layout.panelY + layout.panelHeight) {
                entityPicker = null;
                return true;
            }
            if (y < layout.gridBottom || y > layout.gridTop) {
                return true;
            }
            int col = (screenX - layout.innerX) / CELL_SIZE;
            int row = (layout.gridTop - y) / CELL_SIZE;
            if (col >= 0 && col < layout.gridCols && row >= 0 && row < layout.visibleRows) {
                int idx = scrollOffset * layout.gridCols + row * layout.gridCols + col;
                if (idx >= 0 && idx < filteredEntries.size()) {
                    selectedIndex = idx;
                    select(filteredEntries.get(idx));
                }
            }
            return true;
        }

        public boolean handleScroll(float amount) {
            EntityLayout layout = layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int maxRows = (filteredEntries.size() + layout.gridCols - 1) / layout.gridCols;
            scrollOffset = Math.max(0, Math.min(Math.max(0, maxRows - layout.visibleRows),
                    scrollOffset + (int) amount));
            return true;
        }

        public boolean handleKeyTyped(char character) {
            if (character == '\b') {
                if (!searchFilter.isEmpty()) {
                    searchFilter = searchFilter.substring(0, searchFilter.length() - 1);
                    updateFilter();
                }
                return true;
            }
            if (character == '\r' || character == '\n') {
                if (!filteredEntries.isEmpty()) {
                    select(filteredEntries.get(0));
                }
                return true;
            }
            if (Character.isLetterOrDigit(character) || character == '_' || character == '-') {
                searchFilter += character;
                updateFilter();
                return true;
            }
            return false;
        }

        private void select(MonsterTypeEntry entry) {
            if (monsters) {
                selectedMonsterType = entry;
                selectedMonsterSpawn = null;
                monsterPlacementEnabled = true;
                npcPlacementEnabled = false;
                showEditorMessage("Monster placement: " + entry.displayName);
            } else {
                selectedNpcType = entry;
                selectedNpcSpawn = null;
                npcPlacementEnabled = true;
                monsterPlacementEnabled = false;
                showEditorMessage("NPC placement: " + entry.displayName);
            }
            entityPicker = null;
        }

        private void updateFilter() {
            String lower = searchFilter.toLowerCase(Locale.ROOT);
            filteredEntries = new ArrayList<>();
            for (MonsterTypeEntry entry : allEntries) {
                if (lower.isBlank() || entry.displayName.toLowerCase(Locale.ROOT).contains(lower)
                        || entry.className.toLowerCase(Locale.ROOT).contains(lower)) {
                    filteredEntries.add(entry);
                }
            }
            scrollOffset = 0;
        }

        private TextureRegion previewFor(MonsterTypeEntry entry) {
            if (entry == null || entry.thumbnailSprite == null) {
                return null;
            }
            if (previews.containsKey(entry.thumbnailSprite)) {
                return previews.get(entry.thumbnailSprite);
            }
            TextureRegion region = null;
            try {
                region = spriteLoader.getRegionFromSpriteName(entry.thumbnailSprite);
            } catch (Throwable ignored) {
                region = null;
            }
            previews.put(entry.thumbnailSprite, region);
            return region;
        }

        private boolean drawNpcCompositePreview(SpriteBatch batch, MonsterTypeEntry entry, int cellX, int cellY) {
            BaseNPC npc = getNpcPreview(entry.className);
            if (npc == null || npc.getAnimations() == null || npc.getAnimations().getPartMap().isEmpty()) {
                return false;
            }

            String angle = "000";
            List<NpcPreviewPart> parts = new ArrayList<>();
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            float maxY = -Float.MAX_VALUE;

            java.util.Set<BodyPart> renderedParts = java.util.EnumSet.noneOf(BodyPart.class);
            for (BodyPart part : PuppetBodyOrder.getBodyPartsOrder(angle)) {
                if (!renderedParts.add(part)) {
                    continue;
                }
                String base = com.perso.T4C.npc.NPCAnimations.resolveCompositeBase(npc.getAnimations().getPartMap(), part);
                if (base == null || base.isBlank()) {
                    continue;
                }
                String spriteName = base + angle + "-a";
                TextureRegion region;
                try {
                    region = spriteLoader.getRegionFromSpriteName(spriteName);
                } catch (Throwable ignored) {
                    region = null;
                }
                if (region == null) {
                    continue;
                }
                SpriteLoader.Sprite meta = spriteLoader.getSprites().stream()
                        .filter(sprite -> sprite.getName() != null && sprite.getName().equalsIgnoreCase(spriteName))
                        .findFirst()
                        .orElse(null);
                float offX = meta != null ? meta.getDrawOffset1X() : 0f;
                float offY = meta != null ? meta.getDrawOffset1Y() : 0f;
                NpcPreviewPart previewPart = new NpcPreviewPart(region, offX, offY);
                parts.add(previewPart);
                minX = Math.min(minX, offX);
                minY = Math.min(minY, offY);
                maxX = Math.max(maxX, offX + region.getRegionWidth());
                maxY = Math.max(maxY, offY + region.getRegionHeight());
            }

            if (parts.isEmpty()) {
                return false;
            }

            float boundsW = Math.max(1f, maxX - minX);
            float boundsH = Math.max(1f, maxY - minY);
            float scale = Math.min(PREVIEW_SIZE / boundsW, PREVIEW_SIZE / boundsH);
            float previewX = cellX + (CELL_SIZE - PREVIEW_SIZE) / 2f;
            float previewY = cellY + 32f;
            float originX = previewX + (PREVIEW_SIZE - boundsW * scale) / 2f - minX * scale;

            for (NpcPreviewPart part : parts) {
                float w = part.region.getRegionWidth() * scale;
                float h = part.region.getRegionHeight() * scale;
                float x = originX + part.offsetX * scale;
                float y = previewY + (PREVIEW_SIZE - boundsH * scale) / 2f
                        + (maxY - (part.offsetY + part.region.getRegionHeight())) * scale;
                batch.draw(part.region, x, y, w, h);
            }
            return true;
        }

        private BaseNPC getNpcPreview(String className) {
            if (className == null || className.isBlank()) {
                return null;
            }
            if (npcPreviewCache.containsKey(className)) {
                return npcPreviewCache.get(className);
            }
            BaseNPC npc = null;
            try {
                com.perso.T4C.npc.NpcDef def = com.perso.T4C.npc.NpcRegistry.findByName(className);
                if (def != null) {
                    npc = new com.perso.T4C.npc.DataNpc(def);
                }
            } catch (Throwable ignored) {
                npc = null;
            }
            npcPreviewCache.put(className, npc);
            return npc;
        }

        private EntityLayout layout(int screenWidth, int screenHeight) {
            EntityLayout layout = new EntityLayout();
            layout.panelX = 16;
            layout.panelY = 16;
            layout.panelWidth = Math.max(360, screenWidth - 32);
            layout.panelHeight = Math.max(360, screenHeight - 32);
            layout.innerX = layout.panelX + PANEL_PADDING;
            layout.innerWidth = layout.panelWidth - PANEL_PADDING * 2;
            layout.headerTop = layout.panelY + layout.panelHeight;
            layout.headerBottom = layout.headerTop - HEADER_HEIGHT;
            layout.searchTop = layout.headerBottom - 14;
            layout.searchBottom = layout.searchTop - SEARCH_HEIGHT;
            layout.gridTop = layout.searchBottom - 10;
            layout.gridBottom = layout.panelY + FOOTER_HEIGHT;
            layout.gridCols = Math.max(3, layout.innerWidth / CELL_SIZE);
            layout.visibleRows = Math.max(1, (layout.gridTop - layout.gridBottom) / CELL_SIZE);
            return layout;
        }
/**
 * Class representing EntityLayout.
 */

        private class EntityLayout {
            int panelX;
            int panelY;
            int panelWidth;
            int panelHeight;
            int innerX;
            int innerWidth;
            int headerTop;
            int headerBottom;
            int searchTop;
            int searchBottom;
            int gridTop;
            int gridBottom;
            int gridCols;
            int visibleRows;
        }
    }

    private class ObjectMappingsEditorUI extends EditorDialog {
        private static final int ROW_HEIGHT = 24;
        private static final int FIELD_HEIGHT = 28;
        private static final float SPRITE_FILTER_HEADER_H = 24f;

        private final Map<String, ObjectMapping> mappings = new LinkedHashMap<>();
        private final List<String> names = new ArrayList<>();
        private final List<String> spriteNames = new ArrayList<>();
        private final List<SpriteGroup> spriteGroups = new ArrayList<>();
        private final List<SpriteGroup> filteredSpriteGroups = new ArrayList<>();
        private final List<String> soundNames = new ArrayList<>();
        private final StringBuilder logicalName = new StringBuilder();
        private final StringBuilder displayName = new StringBuilder();
        private final StringBuilder sprite = new StringBuilder();
        private final StringBuilder openSound = new StringBuilder();
        private final StringBuilder closeSound = new StringBuilder();
        private final StringBuilder depthTileOffsetY = new StringBuilder();
        private boolean clickAnimate;
        private boolean mirror;
        private boolean alwaysBehind;
        private boolean dirty;
        private int selectedIndex = -1;
        private int scrollOffset;
        private int spriteScrollOffset;
        private int soundScrollOffset;
        private final StringBuilder spriteFilter = new StringBuilder();
        private int selectedSpriteGroupIndex = -1;
        private int activeField;
        private int openDropdownField = -1;
        private final EditorListBox<String> objectList = new EditorListBox<String>()
                .rowHeight(ROW_HEIGHT)
                .labelProvider(s -> s)
                .colorProvider(i -> i == selectedIndex ? EditorTheme.BLUE : null);
        private final EditorDropdownList<String> omSoundDropdown = new EditorDropdownList<String>()
                .visibleRows(8)
                .labelProvider(v -> v == null || v.isEmpty() ? "(none)" : v)
                .onSelection(v -> {
                    if (openDropdownField == 3) setText(openSound, v == null ? "" : v);
                    else if (openDropdownField == 4) setText(closeSound, v == null ? "" : v);
                    activeField = openDropdownField;
                    dirty = true;
                });

        private com.badlogic.gdx.math.Rectangle panelBounds;
        private com.badlogic.gdx.math.Rectangle listBounds;
        private com.badlogic.gdx.math.Rectangle spriteGroupBounds;
        private com.badlogic.gdx.math.Rectangle spriteListBounds;
        private com.badlogic.gdx.math.Rectangle spriteListScrollTrackBounds;
        private com.badlogic.gdx.math.Rectangle spriteListScrollThumbBounds;
        private com.badlogic.gdx.math.Rectangle spritePreviewBounds;
        private com.badlogic.gdx.math.Rectangle soundListBounds;
        private com.badlogic.gdx.math.Rectangle soundListScrollTrackBounds;
        private com.badlogic.gdx.math.Rectangle soundListScrollThumbBounds;
        private com.badlogic.gdx.math.Rectangle[] fieldBounds;
        private final EditorButton btnAdd = new EditorButton("Add", () -> addOrUpdate(false));
        private final EditorButton btnUpdate = new EditorButton("Save", this::saveFromForm);
        private final EditorButton btnDelete = new EditorButton("Delete", this::deleteSelected);
        private com.badlogic.gdx.math.Rectangle clickAnimateBounds;
        private com.badlogic.gdx.math.Rectangle mirrorBounds;
        private com.badlogic.gdx.math.Rectangle behindBounds;

        private void refreshObjectList() {
            objectList.setItems(names);
        }

        ObjectMappingsEditorUI() {
            super("Object Definitions");
            mappings.putAll(ObjectMappings.load());
            refreshNames();
            refreshObjectList();
            for (SpriteLoader.Sprite entry : spriteLoader.getSprites()) {
                if (entry != null && entry.getName() != null) {
                    spriteNames.add(entry.getName());
                }
            }
            spriteNames.sort(String::compareToIgnoreCase);
            buildSpriteGroups();
            refreshSpriteGroupFilter();
            File soundsDir = new File(Paths.SOUNDS_DIR);
            File[] files = soundsDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".wav"));
            if (files != null) {
                for (File file : files) {
                    soundNames.add(file.getName());
                }
            }
            soundNames.sort(String::compareToIgnoreCase);
            omSoundDropdown.setItems(soundNames);
            if (!names.isEmpty()) {
                select(0);
            }
        }

        public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();
            layout(sw, sh);
            prepareUiProjection(sw, sh);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(shapeRenderer, sw, sh);
            EditorPanelChrome.panel(shapeRenderer, panelBounds, EditorTheme.ORANGE, 64f);
            for (int i = 0; i < fieldBounds.length; i++) {
                EditorPanelChrome.textField(shapeRenderer, fieldBounds[i], i == activeField);
            }
            EditorPanelChrome.dropdownTrigger(shapeRenderer, spriteGroupBounds, openDropdownField == 2);
            EditorPanelChrome.darkSurface(shapeRenderer, spritePreviewBounds);
            EditorPanelChrome.checkbox(shapeRenderer, clickAnimateBounds, clickAnimate);
            EditorPanelChrome.checkbox(shapeRenderer, mirrorBounds, mirror);
            EditorPanelChrome.checkbox(shapeRenderer, behindBounds, alwaysBehind);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(shapeRenderer, panelBounds);
            for (com.badlogic.gdx.math.Rectangle r : fieldBounds) EditorPanelChrome.border(shapeRenderer, r);
            EditorPanelChrome.border(shapeRenderer, spriteGroupBounds);
            EditorPanelChrome.border(shapeRenderer, spritePreviewBounds);
            shapeRenderer.end();

            batch.setProjectionMatrix(batch.getProjectionMatrix().idt().setToOrtho2D(0, 0, sw, sh));
            batch.begin();
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Object Definitions", panelBounds.x + 18f, panelBounds.y + panelBounds.height - 18f);
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, "Saved to object_mappings.bin. Object positions use these logical names.",
                    panelBounds.x + 18f, panelBounds.y + panelBounds.height - 42f);
            drawLabels(batch);
            drawSpritePreview(batch);
            batch.end();

            objectList.render(batch, shapeRenderer, font);
            btnAdd.withFont(font).render(batch, shapeRenderer);
            btnUpdate.withFont(font).render(batch, shapeRenderer);
            btnDelete.withFont(font).render(batch, shapeRenderer);

            if (openDropdownField == 2) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                drawDropdownBackground(shapeRenderer);
                drawDropdownScrollBar(shapeRenderer);
                shapeRenderer.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(UI_BORDER);
                drawDropdownBorder(shapeRenderer);
                shapeRenderer.end();
                batch.begin();
                drawDropdownLabels(batch);
                batch.end();
            } else if (openDropdownField == 3 || openDropdownField == 4) {
                omSoundDropdown.renderDropdown(batch, shapeRenderer, font);
            }
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT) return true;
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int y = Gdx.graphics.getHeight() - screenY;
            if (!panelBounds.contains(screenX, y)) return true;
            for (int i = 0; i < fieldBounds.length; i++) {
                if (fieldBounds[i].contains(screenX, y)) {
                    activeField = i;
                    openDropdownField = (i >= 2 && i <= 4 && openDropdownField == i) ? -1 : (i >= 2 && i <= 4 ? i : -1);
                    return true;
                }
            }
            if (spriteGroupBounds.contains(screenX, y)) {
                openDropdownField = 2;
                return true;
            }
            if (objectList.handleClick(screenX, y, button)) {
                if (objectList.selectedIndex() >= 0) select(objectList.selectedIndex());
                return true;
            }
            if (openDropdownField == 2 && spriteListBounds.contains(screenX, y)) {
                if (spriteListScrollTrackBounds != null && spriteListScrollTrackBounds.contains(screenX, y)) {
                    int maxOffset = Math.max(0, filteredSpriteGroups.size() - visibleRows(spriteListBounds));
                    if (maxOffset > 0) {
                        float ratioFromTop = (spriteListScrollTrackBounds.y + spriteListScrollTrackBounds.height - y)
                                / spriteListScrollTrackBounds.height;
                        spriteScrollOffset = Math.max(0, Math.min(maxOffset, Math.round(ratioFromTop * maxOffset)));
                    }
                    return true;
                }
                int index = spriteScrollOffset + rowAtSpriteList(y);
                if (index >= 0 && index < filteredSpriteGroups.size()) {
                    SpriteGroup chosen = filteredSpriteGroups.get(index);
                    selectedSpriteGroupIndex = spriteGroups.indexOf(chosen);
                    setText(sprite, chosen.pattern());
                    activeField = 1;
                    openDropdownField = -1;
                    dirty = true;
                }
                return true;
            }
            if ((openDropdownField == 3 || openDropdownField == 4) && omSoundDropdown.isOpen()) {
                omSoundDropdown.handleClick(screenX, y, button);
                openDropdownField = -1;
                return true;
            }
            openDropdownField = -1;
            if (clickAnimateBounds.contains(screenX, y)) clickAnimate = !clickAnimate;
            else if (mirrorBounds.contains(screenX, y)) mirror = !mirror;
            else if (behindBounds.contains(screenX, y)) alwaysBehind = !alwaysBehind;
            btnAdd.handleClick(screenX, y, button);
            btnUpdate.handleClick(screenX, y, button);
            btnDelete.handleClick(screenX, y, button);
            return true;
        }

        public boolean handleScroll(float amount) {
            int delta = amount > 0 ? 3 : -3;
            if (openDropdownField == 2) {
                int maxSprites = Math.max(0, filteredSpriteGroups.size() - visibleRows(spriteListBounds));
                spriteScrollOffset = Math.max(0, Math.min(maxSprites, spriteScrollOffset + delta));
            } else if (openDropdownField == 3 || openDropdownField == 4) {
                omSoundDropdown.setScrollOffset(omSoundDropdown.scrollOffset() + delta);
            } else {
                objectList.scroll(delta);
                scrollOffset = objectList.scrollOffset();
            }
            return true;
        }

        public boolean handleKeyTyped(char character) {
            if (openDropdownField == 2) {
                if (character == '\b') {
                    if (spriteFilter.length() > 0) {
                        spriteFilter.deleteCharAt(spriteFilter.length() - 1);
                        refreshSpriteGroupFilter();
                    }
                    return true;
                }
                if (character >= 32 && character != 127) {
                    spriteFilter.append(character);
                    refreshSpriteGroupFilter();
                    return true;
                }
                return true;
            }
            if ((openDropdownField == 3 || openDropdownField == 4) && omSoundDropdown.isOpen()) {
                return omSoundDropdown.handleKeyTyped(character);
            }
            if (character == '\b') {
                return true;
            }
            if (character >= 32 && character != 127) {
                activeText().append(character);
                return true;
            }
            return true;
        }

        public boolean handleKeyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                if (openDropdownField >= 0) {
                    openDropdownField = -1;
                } else {
                    close();
                }
            } else if ((openDropdownField == 3 || openDropdownField == 4) && omSoundDropdown.isOpen()) {
                if (keycode == Input.Keys.ENTER) {
                    openDropdownField = -1;
                }
                return true;
            } else if (keycode == Input.Keys.TAB) {
                activeField = (activeField + 1) % 6;
                openDropdownField = activeField >= 3 && activeField <= 4 ? activeField : -1;
            } else if (keycode == Input.Keys.ENTER) {
                addOrUpdate(selectedIndex >= 0);
            } else if (keycode == Input.Keys.BACKSPACE || keycode == Input.Keys.DEL) {
                if (openDropdownField == 2) {
                    if (spriteFilter.length() > 0) {
                        spriteFilter.deleteCharAt(spriteFilter.length() - 1);
                        refreshSpriteGroupFilter();
                    }
                } else {
                    deleteLastActiveCharacter();
                }
            } else if (keycode == Input.Keys.FORWARD_DEL) {
                deleteSelected();
            }
            return true;
        }

        private boolean addOrUpdate(boolean keepSelection) {
            String key = logicalName.toString().trim().toUpperCase(Locale.ROOT);
            if (key.isEmpty() || sprite.toString().trim().isEmpty()) {
                showEditorMessage("Object name and sprite are required");
                return false;
            }
            ObjectMapping existing = mappings.get(key);
            int objectId = existing != null ? existing.id : 0;
            Integer depthOffset = parseDepthTileOffset();
            if (depthOffset == null) {
                return false;
            }
            mappings.put(key, new ObjectMapping(objectId, sprite.toString().trim(), clickAnimate, mirror,
                    openSound.toString().trim(), closeSound.toString().trim(), alwaysBehind,
                    displayName.toString().trim(), depthOffset));
            refreshNames();
            selectedIndex = names.indexOf(key);
            if (!keepSelection) scrollOffset = Math.max(0, selectedIndex - 4);
            dirty = true;
            showEditorMessage("Object definition updated");
            return true;
        }

        private void deleteSelected() {
            if (selectedIndex < 0 || selectedIndex >= names.size()) return;
            mappings.remove(names.get(selectedIndex));
            refreshNames();
            selectedIndex = Math.min(selectedIndex, names.size() - 1);
            if (selectedIndex >= 0) select(selectedIndex);
            dirty = true;
        }

        private void save() {
            try {
                ObjectMappings.save(mappings);
                dirty = false;
                objectPositionTypes.clear();
                objectPositionTypes.addAll(mappings.keySet());
                objectPositionTypes.sort(String::compareToIgnoreCase);
                if (mapRenderer != null) mapRenderer.reloadObjectMappings();
                showEditorMessage("Object definitions saved");
            } catch (Exception e) {
                log.error("Failed to save object definitions", e);
                showEditorMessage("Error: object definitions not saved");
            }
        }

        public void close() {
            if (dirty) save();
            objectMappingsEditor = null;
        }

        private void select(int index) {
            selectedIndex = index;
            String key = names.get(index);
            ObjectMapping mapping = mappings.get(key);
            setText(logicalName, key);
            setText(displayName, mapping.displayName != null ? mapping.displayName : "");
            setText(sprite, mapping.sprite);
            setText(openSound, mapping.animateSound);
            setText(closeSound, mapping.reverseAnimateSound);
            setText(depthTileOffsetY, Integer.toString(mapping.depthTileOffsetY));
            clickAnimate = mapping.clickAnimate;
            mirror = mapping.mirror;
            alwaysBehind = mapping.alwaysBehindEntities;
            selectedSpriteGroupIndex = findSpriteGroupIndexForPattern(mapping.sprite);
        }

        private void selectByName(String name) {
            if (name == null) {
                return;
            }
            String key = name.toUpperCase(Locale.ROOT);
            int index = names.indexOf(key);
            if (index < 0) {
                return;
            }
            select(index);
            scrollOffset = Math.max(0, index - 4);
        }

        private void refreshNames() {
            names.clear();
            names.addAll(mappings.keySet());
            names.sort(String::compareToIgnoreCase);
            refreshObjectList();
        }

        private StringBuilder activeText() {
            return switch (activeField) {
                case 1 -> displayName;
                case 2 -> sprite;
                case 3 -> openSound;
                case 4 -> closeSound;
                case 5 -> depthTileOffsetY;
                default -> logicalName;
            };
        }

        private Integer parseDepthTileOffset() {
            String value = depthTileOffsetY.toString().trim();
            if (value.isEmpty()) {
                return 0;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                showEditorMessage("Depth Y must be an integer");
                return null;
            }
        }

        private void saveFromForm() {
            if (addOrUpdate(true)) {
                save();
            }
        }

        private void deleteLastActiveCharacter() {
            StringBuilder target = activeText();
            if (target.length() > 0) {
                target.deleteCharAt(target.length() - 1);
                dirty = true;
            }
        }

        private void setText(StringBuilder builder, String value) {
            builder.setLength(0);
            if (value != null) builder.append(value);
        }

        private void layout(int sw, int sh) {
            float w = Math.min(1040f, sw - 70f);
            float h = Math.min(680f, sh - 70f);
            float x = (sw - w) / 2f;
            float y = (sh - h) / 2f;
            panelBounds = new com.badlogic.gdx.math.Rectangle(x, y, w, h);
            float contentTop = y + h - 90f;
            listBounds = new com.badlogic.gdx.math.Rectangle(x + 18f, y + 58f, 245f, contentTop - y - 58f);
            objectList.setBounds(listBounds.x, listBounds.y, listBounds.width, listBounds.height);
            objectList.setScrollOffset(scrollOffset);
            float fieldX = x + 285f;
            fieldBounds = new com.badlogic.gdx.math.Rectangle[] {
                    new com.badlogic.gdx.math.Rectangle(fieldX, contentTop - FIELD_HEIGHT, 380f, FIELD_HEIGHT),
                    new com.badlogic.gdx.math.Rectangle(fieldX, contentTop - 58f - FIELD_HEIGHT, 380f, FIELD_HEIGHT),
                    new com.badlogic.gdx.math.Rectangle(fieldX, contentTop - 116f - FIELD_HEIGHT, 380f, FIELD_HEIGHT),
                    new com.badlogic.gdx.math.Rectangle(fieldX, contentTop - 174f - FIELD_HEIGHT, 380f, FIELD_HEIGHT),
                    new com.badlogic.gdx.math.Rectangle(fieldX, contentTop - 232f - FIELD_HEIGHT, 380f, FIELD_HEIGHT),
                    new com.badlogic.gdx.math.Rectangle(fieldX, contentTop - 290f - FIELD_HEIGHT, 150f, FIELD_HEIGHT)
            };
            clickAnimateBounds = new com.badlogic.gdx.math.Rectangle(fieldX, contentTop - 348f, 24f, 24f);
            mirrorBounds = new com.badlogic.gdx.math.Rectangle(fieldX + 140f, contentTop - 348f, 24f, 24f);
            behindBounds = new com.badlogic.gdx.math.Rectangle(fieldX + 260f, contentTop - 348f, 24f, 24f);
            btnAdd.setBounds(fieldX, y + 56f, 64f, 30f);
            btnUpdate.setBounds(fieldX + 74f, y + 56f, 76f, 30f);
            btnDelete.setBounds(fieldX + 160f, y + 56f, 72f, 30f);
            btnDelete.setEnabled(selectedIndex >= 0);
            spriteGroupBounds = new com.badlogic.gdx.math.Rectangle(x + w - 250f, y + h - 84f, 220f, FIELD_HEIGHT);
            spritePreviewBounds = new com.badlogic.gdx.math.Rectangle(spriteGroupBounds.x, y + 58f, spriteGroupBounds.width,
                    Math.max(90f, spriteGroupBounds.y - y - 72f));
            if (openDropdownField == 3 || openDropdownField == 4) {
                int soundFieldIndex = Math.max(3, Math.min(4, openDropdownField));
                com.badlogic.gdx.math.Rectangle sf = fieldBounds[soundFieldIndex];
                soundListBounds = new com.badlogic.gdx.math.Rectangle(sf.x, sf.y - 174f, 380f, 168f);
                omSoundDropdown.setBounds(sf.x, sf.y - 174f, 380f, 168f);
                omSoundDropdown.setOpen(true);
            } else {
                omSoundDropdown.setOpen(false);
            }
            spriteListBounds = new com.badlogic.gdx.math.Rectangle(spriteGroupBounds.x, spriteGroupBounds.y - 174f,
                    spriteGroupBounds.width, 168f);
            spriteListScrollTrackBounds = new com.badlogic.gdx.math.Rectangle(
                    spriteListBounds.x + spriteListBounds.width - 12f,
                    spriteListBounds.y + 3f,
                    8f,
                    spriteListBounds.height - 6f);
            int visibleSpriteRows = visibleRows(spriteListBounds);
            int maxSpriteOffset = Math.max(0, filteredSpriteGroups.size() - visibleSpriteRows);
            spriteScrollOffset = Math.max(0, Math.min(maxSpriteOffset, spriteScrollOffset));
            float thumbHeight = filteredSpriteGroups.isEmpty() ? spriteListScrollTrackBounds.height
                    : Math.max(24f, spriteListScrollTrackBounds.height
                            * (visibleSpriteRows / (float) Math.max(visibleSpriteRows, filteredSpriteGroups.size())));
            float thumbTravel = Math.max(0f, spriteListScrollTrackBounds.height - thumbHeight);
            float thumbY = spriteListScrollTrackBounds.y + spriteListScrollTrackBounds.height - thumbHeight;
            if (maxSpriteOffset > 0) {
                thumbY -= thumbTravel * (spriteScrollOffset / (float) maxSpriteOffset);
            }
            spriteListScrollThumbBounds = new com.badlogic.gdx.math.Rectangle(spriteListScrollTrackBounds.x,
                    thumbY, spriteListScrollTrackBounds.width, thumbHeight);
        }

        private int rowAt(com.badlogic.gdx.math.Rectangle bounds, int y) {
            return (int) ((bounds.y + bounds.height - y) / ROW_HEIGHT);
        }

        private int visibleRows(com.badlogic.gdx.math.Rectangle bounds) {
            return bounds == null ? 1 : Math.max(1, (int) (bounds.height / ROW_HEIGHT));
        }

        private void drawLabels(SpriteBatch batch) {
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Definitions " + names.size() + (dirty ? " *" : ""), listBounds.x, listBounds.y + listBounds.height + 18f);
            drawField(batch, 0, "Name", logicalName.toString());
            drawField(batch, 1, "Display Name", displayName.toString());
            drawField(batch, 2, "Sprite pattern", sprite.toString());
            drawField(batch, 3, "Open sound", openSound.toString());
            drawField(batch, 4, "Close sound", closeSound.toString());
            drawField(batch, 5, "Depth Y offset", depthTileOffsetY.toString());
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Animate", clickAnimateBounds.x + 32f, clickAnimateBounds.y + 18f);
            font.draw(batch, "Mirror", mirrorBounds.x + 32f, mirrorBounds.y + 18f);
            font.draw(batch, "Behind", behindBounds.x + 32f, behindBounds.y + 18f);
            font.draw(batch, "Sprite", spriteGroupBounds.x, spriteGroupBounds.y + spriteGroupBounds.height + 14f);
            font.setColor(UI_TEXT);
            String groupLabel = selectedSpriteGroupIndex >= 0 && selectedSpriteGroupIndex < spriteGroups.size()
                    ? spriteGroups.get(selectedSpriteGroupIndex).baseName : "";
            font.draw(batch, shorten(groupLabel, 23), spriteGroupBounds.x + 8f, spriteGroupBounds.y + 19f);
            font.draw(batch, "v", spriteGroupBounds.x + spriteGroupBounds.width - 18f, spriteGroupBounds.y + 19f);
        }

        private void drawField(SpriteBatch batch, int index, String label, String value) {
            com.badlogic.gdx.math.Rectangle r = fieldBounds[index];
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, label, r.x, r.y + r.height + 14f);
            font.setColor(UI_TEXT);
            font.draw(batch, shorten(value, 42), r.x + 8f, r.y + 19f);
            if (index >= 2 && index <= 4) {
                font.draw(batch, "v", r.x + r.width - 18f, r.y + 19f);
            }
        }

        private void drawStringList(SpriteBatch batch, List<String> values, int offset, com.badlogic.gdx.math.Rectangle bounds) {
            int end = Math.min(values.size(), offset + visibleRows(bounds));
            float y = bounds.y + bounds.height - 8f;
            for (int i = offset; i < end; i++) {
                font.setColor(UI_TEXT);
                font.draw(batch, shorten(values.get(i), 38), bounds.x + 8f, y);
                y -= ROW_HEIGHT;
            }
        }

        private void drawSpriteGroupList(SpriteBatch batch) {
            int end = Math.min(filteredSpriteGroups.size(), spriteScrollOffset + visibleRowsSpriteList());
            float y = spriteListBounds.y + spriteListBounds.height - SPRITE_FILTER_HEADER_H - 8f;
            for (int i = spriteScrollOffset; i < end; i++) {
                SpriteGroup group = filteredSpriteGroups.get(i);
                boolean selected = selectedSpriteGroupIndex >= 0 && selectedSpriteGroupIndex < spriteGroups.size()
                        && spriteGroups.get(selectedSpriteGroupIndex) == group;
                font.setColor(selected ? UI_BLUE : UI_TEXT);
                font.draw(batch, shorten(group.baseName, 20), spriteListBounds.x + 8f, y);
                y -= ROW_HEIGHT;
            }
        }

        private void drawSpritePreview(SpriteBatch batch) {
            if (selectedSpriteGroupIndex < 0 || selectedSpriteGroupIndex >= spriteGroups.size()) {
                return;
            }
            SpriteGroup group = spriteGroups.get(selectedSpriteGroupIndex);
            if (group.frames.isEmpty()) {
                return;
            }
            int frame = (int) ((TimeUtils.millis() / 140L) % group.frames.size());
            TextureRegion region;
            try {
                region = spriteLoader.getRegionFromSpriteName(group.frames.get(frame));
            } catch (Exception e) {
                return;
            }
            if (region == null) {
                return;
            }
            float scale = Math.min(spritePreviewBounds.width / Math.max(1f, region.getRegionWidth()),
                    spritePreviewBounds.height / Math.max(1f, region.getRegionHeight()));
            scale = Math.min(scale, 3f);
            float w = region.getRegionWidth() * scale;
            float h = region.getRegionHeight() * scale;
            float px = spritePreviewBounds.x + (spritePreviewBounds.width - w) * 0.5f;
            float py = spritePreviewBounds.y + (spritePreviewBounds.height - h) * 0.5f;
            batch.draw(region, px, py, w, h);
        }

        private void drawDropdownBackground(ShapeRenderer sr) {
            com.badlogic.gdx.math.Rectangle bounds = activeDropdownBounds();
            if (bounds == null) {
                return;
            }
            EditorPanelChrome.dropdownTrigger(sr, bounds, true);
            if (openDropdownField == 2) {
                EditorPanelChrome.textField(sr,
                        new com.badlogic.gdx.math.Rectangle(bounds.x + 1f,
                                bounds.y + bounds.height - SPRITE_FILTER_HEADER_H,
                                bounds.width - 2f,
                                SPRITE_FILTER_HEADER_H - 1f),
                        true);
            }
        }

        private void drawDropdownScrollBar(ShapeRenderer sr) {
            if (openDropdownField == 2 && filteredSpriteGroups.size() > visibleRowsSpriteList()
                    && spriteListScrollTrackBounds != null && spriteListScrollThumbBounds != null) {
                EditorPanelChrome.scrollbar(sr, spriteListScrollTrackBounds, spriteListScrollThumbBounds);
            }
        }

        private void drawDropdownBorder(ShapeRenderer sr) {
            com.badlogic.gdx.math.Rectangle bounds = activeDropdownBounds();
            if (bounds != null) {
                EditorPanelChrome.border(sr, bounds);
            }
        }

        private void drawDropdownLabels(SpriteBatch batch) {
            if (openDropdownField == 2) {
                font.setColor(UI_TEXT_MUTED);
                String filterLabel = spriteFilter.length() == 0 ? "Filter: (type to search)" : "Filter: " + spriteFilter;
                font.draw(batch, shorten(filterLabel, 34), spriteListBounds.x + 8f, spriteListBounds.y + spriteListBounds.height - 7f);
                drawSpriteGroupList(batch);
            }
        }

        private int rowAtSpriteList(int y) {
            float top = spriteListBounds.y + spriteListBounds.height - SPRITE_FILTER_HEADER_H;
            return (int) ((top - y) / ROW_HEIGHT);
        }

        private int visibleRowsSpriteList() {
            return Math.max(1, (int) ((spriteListBounds.height - SPRITE_FILTER_HEADER_H) / ROW_HEIGHT));
        }

        private com.badlogic.gdx.math.Rectangle activeDropdownBounds() {
            if (openDropdownField == 2) {
                return spriteListBounds;
            }
            if (openDropdownField == 3 || openDropdownField == 4) {
                return soundListBounds;
            }
            return null;
        }

        private void buildSpriteGroups() {
            Map<String, SpriteGroup> groups = new LinkedHashMap<>();
            for (String spriteName : spriteNames) {
                SpriteGroupKey key = spriteGroupKey(spriteName);
                SpriteGroup group = groups.computeIfAbsent(key.baseName, SpriteGroup::new);
                group.frames.add(spriteName);
                group.alphaSequence |= key.alphaSequence;
                group.numericSequence |= key.numericSequence;
            }
            spriteGroups.clear();
            spriteGroups.addAll(groups.values());
            for (SpriteGroup group : spriteGroups) {
                group.frames.sort(String::compareToIgnoreCase);
            }
            spriteGroups.sort(Comparator.comparing(group -> group.baseName.toLowerCase(Locale.ROOT)));
        }

        private void refreshSpriteGroupFilter() {
            String filter = normalizeSpriteKey(spriteFilter.toString());
            filteredSpriteGroups.clear();
            for (SpriteGroup group : spriteGroups) {
                if (filter.isBlank() || normalizeSpriteKey(group.baseName).contains(filter)) {
                    filteredSpriteGroups.add(group);
                }
            }
            int maxOffset = Math.max(0, filteredSpriteGroups.size() - visibleRows(spriteListBounds));
            spriteScrollOffset = Math.max(0, Math.min(maxOffset, spriteScrollOffset));
        }

        private SpriteGroupKey spriteGroupKey(String spriteName) {
            java.util.regex.Matcher alpha = java.util.regex.Pattern.compile("^(.+)-([a-z])$",
                    java.util.regex.Pattern.CASE_INSENSITIVE).matcher(spriteName);
            if (alpha.matches()) {
                return new SpriteGroupKey(alpha.group(1), true, false);
            }
            java.util.regex.Matcher numeric = java.util.regex.Pattern.compile("^(.*?)(\\d+)$").matcher(spriteName);
            if (numeric.matches()) {
                return new SpriteGroupKey(numeric.group(1), false, true);
            }
            return new SpriteGroupKey(spriteName, false, false);
        }

        private int findSpriteGroupIndexForPattern(String pattern) {
            if (pattern == null || pattern.isBlank()) {
                return -1;
            }
            for (int i = 0; i < spriteGroups.size(); i++) {
                SpriteGroup group = spriteGroups.get(i);
                if (pattern.equalsIgnoreCase(group.pattern())
                        || (!group.frames.isEmpty() && pattern.equalsIgnoreCase(group.frames.get(0)))) {
                    return i;
                }
            }
            return -1;
        }

        private final class SpriteGroup {
            final String baseName;
            final List<String> frames = new ArrayList<>();
            boolean alphaSequence;
            boolean numericSequence;

            SpriteGroup(String baseName) {
                this.baseName = baseName;
            }

            String pattern() {
                if (frames.size() <= 1) {
                    return frames.isEmpty() ? baseName : frames.get(0);
                }
                if (alphaSequence) {
                    return baseName + "-%s$" + frames.size();
                }
                if (numericSequence) {
                    return baseName + "%d$" + frames.size();
                }
                return frames.get(0);
            }
        }

        private final class SpriteGroupKey {
            final String baseName;
            final boolean alphaSequence;
            final boolean numericSequence;

            SpriteGroupKey(String baseName, boolean alphaSequence, boolean numericSequence) {
                this.baseName = baseName;
                this.alphaSequence = alphaSequence;
                this.numericSequence = numericSequence;
            }
        }
    }

    private class ClanRelationsEditorUI extends EditorDialog {
        private static final int PANEL_PADDING = 18;
        private static final int HEADER_HEIGHT = 58;
        private static final int ROW_HEIGHT = 28;
        private static final int BUTTON_HEIGHT = 32;

        private final MonsterClan[] clans;
        private List<ClanRelationsBinaryIO.Entry> relations;
        private int sourceIndex = 0;
        private int targetIndex = 0;
        private int selectedRelationIndex = -1;
        private int scrollOffset = 0;

        private com.badlogic.gdx.math.Rectangle panelBounds;
        private com.badlogic.gdx.math.Rectangle sourceBounds;
        private com.badlogic.gdx.math.Rectangle targetBounds;
        private com.badlogic.gdx.math.Rectangle relationBounds;
        private final EditorListBox<MonsterClan> sourceList = new EditorListBox<MonsterClan>()
                .rowHeight(ROW_HEIGHT).labelProvider(c -> c.name())
                .colorProvider(i -> i == sourceIndex ? EditorTheme.BLUE : null);
        private final EditorListBox<MonsterClan> targetList = new EditorListBox<MonsterClan>()
                .rowHeight(ROW_HEIGHT).labelProvider(c -> c.name())
                .colorProvider(i -> i == targetIndex ? EditorTheme.BLUE : null);
        private final EditorListBox<ClanRelationsBinaryIO.Entry> relationList = new EditorListBox<ClanRelationsBinaryIO.Entry>()
                .rowHeight(ROW_HEIGHT)
                .labelProvider(this::formatRelation)
                .colorProvider(i -> i == selectedRelationIndex ? EditorTheme.BLUE : null);
        private final EditorButton btnAdd = new EditorButton("Add", this::addSelectedRelation);
        private final EditorButton btnUpdate = new EditorButton("Update", this::updateSelectedRelation);
        private final EditorButton btnReverse = new EditorButton("Add Reverse", () -> addRelation(selectedTarget(), selectedSource()));
        private final EditorButton btnDelete = new EditorButton("Delete", this::deleteSelectedRelation);

        ClanRelationsEditorUI() {
            super("Clan Relations");
            this.clans = Arrays.stream(MonsterClan.values())
                    .filter(clan -> clan != MonsterClan.NEUTRAL)
                    .toArray(MonsterClan[]::new);
            this.relations = new ArrayList<>(MonsterClanRelations.getRelations());
            if (clans.length > 0) {
                sourceIndex = indexOfClan(MonsterClan.GOBLIN);
                targetIndex = indexOfClan(MonsterClan.HORSE);
                if (sourceIndex < 0) sourceIndex = 0;
                if (targetIndex < 0) targetIndex = Math.min(1, clans.length - 1);
            }
            sourceList.setItems(Arrays.asList(clans));
            targetList.setItems(Arrays.asList(clans));
            relationList.setItems(relations);
        }

        public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
            int screenWidth = Gdx.graphics.getWidth();
            int screenHeight = Gdx.graphics.getHeight();
            Layout layout = layout(screenWidth, screenHeight);

            prepareUiProjection(screenWidth, screenHeight);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(shapeRenderer, screenWidth, screenHeight);
            EditorPanelChrome.panel(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.panelX, layout.panelY, layout.panelWidth,
                            layout.panelHeight),
                    EditorTheme.ORANGE, HEADER_HEIGHT);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(layout.panelX, layout.panelY, layout.panelWidth,
                            layout.panelHeight));
            shapeRenderer.end();

            batch.setProjectionMatrix(batch.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
            batch.begin();
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Clan Relations Editor", layout.panelX + PANEL_PADDING,
                    layout.panelY + layout.panelHeight - 18f);
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, "Source clan attacks target clan. Saved to clan_relations.bin.",
                    layout.panelX + PANEL_PADDING, layout.panelY + layout.panelHeight - 40f);
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Source", sourceBounds.x, sourceBounds.y + sourceBounds.height + 18f);
            font.draw(batch, "Target", targetBounds.x, targetBounds.y + targetBounds.height + 18f);
            font.draw(batch, "Relations", relationBounds.x, relationBounds.y + relationBounds.height + 18f);
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, relations.size() + " relation(s)", relationBounds.x,
                    layout.panelY + PANEL_PADDING + 10f);
            batch.end();

            sourceList.render(batch, shapeRenderer, font);
            targetList.render(batch, shapeRenderer, font);
            relationList.render(batch, shapeRenderer, font);
            btnAdd.withFont(font).render(batch, shapeRenderer);
            btnUpdate.withFont(font).render(batch, shapeRenderer);
            btnReverse.withFont(font).render(batch, shapeRenderer);
            btnDelete.withFont(font).render(batch, shapeRenderer);
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT) {
                return true;
            }
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int y = Gdx.graphics.getHeight() - screenY;
            if (panelBounds == null || !panelBounds.contains(screenX, y)) {
                return true;
            }
            if (sourceList.handleClick(screenX, y, button)) {
                if (sourceList.selectedIndex() >= 0) sourceIndex = sourceList.selectedIndex();
                return true;
            }
            if (targetList.handleClick(screenX, y, button)) {
                if (targetList.selectedIndex() >= 0) targetIndex = targetList.selectedIndex();
                return true;
            }
            if (relationList.handleClick(screenX, y, button)) {
                int idx = relationList.selectedIndex();
                if (idx >= 0 && idx < relations.size()) {
                    selectedRelationIndex = idx;
                    ClanRelationsBinaryIO.Entry relation = relations.get(idx);
                    sourceIndex = Math.max(0, indexOfClan(relation.source));
                    targetIndex = Math.max(0, indexOfClan(relation.target));
                }
                return true;
            }
            btnAdd.handleClick(screenX, y, button);
            btnUpdate.handleClick(screenX, y, button);
            btnReverse.handleClick(screenX, y, button);
            btnDelete.handleClick(screenX, y, button);
            return true;
        }

        public boolean handleScroll(float amount) {
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            relationList.scroll(amount > 0 ? 1 : -1);
            scrollOffset = relationList.scrollOffset();
            return true;
        }

        public boolean handleKeyTyped(char character) {
            return false;
        }

        public boolean handleKeyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                close();
                return true;
            }
            if (keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                deleteSelectedRelation();
                return true;
            }
            if (keycode == Input.Keys.ENTER) {
                addSelectedRelation();
                return true;
            }
            return true;
        }

        private void addSelectedRelation() {
            addRelation(selectedSource(), selectedTarget());
        }

        private void addRelation(MonsterClan source, MonsterClan target) {
            if (source == null || target == null || source == target) {
                showEditorMessage("Invalid clan relation");
                return;
            }
            for (ClanRelationsBinaryIO.Entry relation : relations) {
                if (relation.source == source && relation.target == target) {
                    showEditorMessage("Relation already exists: " + formatRelation(relation));
                    return;
                }
            }
            relations.add(new ClanRelationsBinaryIO.Entry(source, target));
            selectedRelationIndex = relations.size() - 1;
            saveRelations();
        }

        private void updateSelectedRelation() {
            if (selectedRelationIndex < 0 || selectedRelationIndex >= relations.size()) {
                return;
            }
            MonsterClan source = selectedSource();
            MonsterClan target = selectedTarget();
            if (source == null || target == null || source == target) {
                showEditorMessage("Invalid clan relation");
                return;
            }
            for (int i = 0; i < relations.size(); i++) {
                if (i == selectedRelationIndex) {
                    continue;
                }
                ClanRelationsBinaryIO.Entry relation = relations.get(i);
                if (relation.source == source && relation.target == target) {
                    showEditorMessage("Relation already exists: " + formatRelation(relation));
                    return;
                }
            }
            relations.set(selectedRelationIndex, new ClanRelationsBinaryIO.Entry(source, target));
            saveRelations();
        }

        private void deleteSelectedRelation() {
            if (selectedRelationIndex < 0 || selectedRelationIndex >= relations.size()) {
                return;
            }
            relations.remove(selectedRelationIndex);
            selectedRelationIndex = Math.min(selectedRelationIndex, relations.size() - 1);
            saveRelations();
        }

        private void refreshRelationList() {
            relationList.setItems(relations);
        }

        private void saveRelations() {
            refreshRelationList();
            try {
                MonsterClanRelations.saveRelations(relations);
                relations = new ArrayList<>(MonsterClanRelations.getRelations());
                showEditorMessage("Clan relations saved");
            } catch (IOException e) {
                log.error("Failed to save clan relations", e);
                showEditorMessage("Error: clan relations not saved");
            }
        }

        public void close() {
            clanRelationsEditor = null;
        }

        private MonsterClan selectedSource() {
            return sourceIndex >= 0 && sourceIndex < clans.length ? clans[sourceIndex] : null;
        }

        private MonsterClan selectedTarget() {
            return targetIndex >= 0 && targetIndex < clans.length ? clans[targetIndex] : null;
        }

        private int indexOfClan(MonsterClan clan) {
            for (int i = 0; i < clans.length; i++) {
                if (clans[i] == clan) {
                    return i;
                }
            }
            return -1;
        }

        private int rowAt(com.badlogic.gdx.math.Rectangle bounds, int y) {
            return (int) ((bounds.y + bounds.height - y) / ROW_HEIGHT);
        }

        private String formatRelation(ClanRelationsBinaryIO.Entry relation) {
            if (relation == null || relation.source == null || relation.target == null) {
                return "-";
            }
            return relation.source.name() + " -> " + relation.target.name();
        }

        private int visibleRelationRows() {
            if (relationBounds == null) {
                return 1;
            }
            return Math.max(1, (int) (relationBounds.height / ROW_HEIGHT));
        }

        private Layout layout(int screenWidth, int screenHeight) {
            Layout layout = new Layout();
            layout.panelWidth = Math.min(900, Math.max(720, screenWidth - 80));
            layout.panelHeight = Math.min(600, Math.max(420, screenHeight - 80));
            layout.panelX = (screenWidth - layout.panelWidth) / 2;
            layout.panelY = (screenHeight - layout.panelHeight) / 2;
            panelBounds = new com.badlogic.gdx.math.Rectangle(layout.panelX, layout.panelY,
                    layout.panelWidth, layout.panelHeight);

            float contentTop = layout.panelY + layout.panelHeight - HEADER_HEIGHT - 44f;
            float contentBottom = layout.panelY + 82f;
            float listHeight = Math.max(120f, contentTop - contentBottom);
            float gap = 14f;
            float sideWidth = 180f;
            float relationWidth = layout.panelWidth - PANEL_PADDING * 2f - sideWidth * 2f - gap * 2f;
            sourceBounds = new com.badlogic.gdx.math.Rectangle(layout.panelX + PANEL_PADDING, contentBottom,
                    sideWidth, listHeight);
            targetBounds = new com.badlogic.gdx.math.Rectangle(sourceBounds.x + sideWidth + gap, contentBottom,
                    sideWidth, listHeight);
            relationBounds = new com.badlogic.gdx.math.Rectangle(targetBounds.x + sideWidth + gap, contentBottom,
                    relationWidth, listHeight);
            layout.visibleRelationRows = visibleRelationRows();
            sourceList.setBounds(sourceBounds.x, sourceBounds.y, sourceBounds.width, sourceBounds.height);
            targetList.setBounds(targetBounds.x, targetBounds.y, targetBounds.width, targetBounds.height);
            relationList.setBounds(relationBounds.x, relationBounds.y, relationBounds.width, relationBounds.height);
            relationList.setScrollOffset(scrollOffset);

            float buttonY = layout.panelY + 32f;
            float buttonW = 112f;
            btnAdd.setBounds(sourceBounds.x, buttonY, buttonW, BUTTON_HEIGHT);
            btnUpdate.setBounds(btnAdd.bounds().x + buttonW + 8f, buttonY, buttonW, BUTTON_HEIGHT);
            btnUpdate.setEnabled(selectedRelationIndex >= 0);
            btnReverse.setBounds(btnUpdate.bounds().x + buttonW + 8f, buttonY, buttonW + 28f, BUTTON_HEIGHT);
            btnDelete.setBounds(btnReverse.bounds().x + btnReverse.bounds().width + 8f, buttonY, buttonW, BUTTON_HEIGHT);
            btnDelete.setEnabled(selectedRelationIndex >= 0);
            return layout;
        }

        private class Layout {
            int panelX;
            int panelY;
            int panelWidth;
            int panelHeight;
            int visibleRelationRows;
        }
    }

    // ==================== NEW EDITOR FEATURES ====================

    /**
     * Copy current tile or selection
     */
    private void performCopy() {
        if (rectangleSelection != null) {
            List<CopiedAreaTile> filtered = buildWallSelectionSnapshot(rectangleSelection);
            if (!filtered.isEmpty()) {
                copiedAreaTiles = filtered;
                copiedTile = null;
                showEditorMessage("Copied house tiles: " + filtered.size());
                log.info("Copied filtered house selection: {} tiles from {}x{}",
                        filtered.size(), rectangleSelection.getWidth(), rectangleSelection.getHeight());
                return;
            }
            // No wall in rectangle: fallback to single-tile copy (e.g. Tree)
        }

        int[] source = resolveCopySourceTile();
        int tileX = source[0];
        int tileY = source[1];

        if (tileX < 0 || tileY < 0) {
            showEditorMessage("No tile selected");
            return;
        }

        if (!copySingleTileAt(tileX, tileY)) {
            showEditorMessage("Cannot copy empty tile");
        }
    }

    /**
     * Paste copied tile to current position
     */
    private void performPaste() {
        if (copiedTile == null && (copiedAreaTiles == null || copiedAreaTiles.isEmpty())) {
            showEditorMessage("Nothing to paste");
            return;
        }

        if (selectedTileX < 0 || selectedTileY < 0) {
            showEditorMessage("No destination tile selected");
            return;
        }

        if (copiedAreaTiles != null && !copiedAreaTiles.isEmpty()) {
            pasteCopiedAreaSelection();
            return;
        }

        // Paste single tile
        UndoEntry undoEntry = newUndoEntry("Paste", true);
        String pastedName = copiedTile.spriteName;
        float pastedScaleX = copiedTile.scaleX;
        float pastedScaleY = copiedTile.scaleY;
        float pastedOffsetX = copiedTile.offsetX;
        float pastedOffsetY = copiedTile.offsetY;
        int pastedZOrder = copiedTile.zOrder;
        if (isGroundSpriteName(copiedTile.spriteName) && isDecorTile(selectedTileX, selectedTileY)) {
            applyGroundLayerChange(selectedTileX, selectedTileY, copiedTile.spriteName, undoEntry);
            pushUndoEntry(undoEntry);
            invalidateGroundAround(selectedTileX, selectedTileY, copiedTile.spriteName);
            showEditorMessage("Pasted ground: " + copiedTile.spriteName + " at (" + selectedTileX + ", " + selectedTileY + ")");
            return;
        }
        // Decor sprites (trees, walls, ...) paste into the DECOR layer so the destination
        // keeps its ground. Writing them through setSpriteName would plant the decor into
        // the ground layer and leave the tile with no ground to render (black tile).
        if (!isGroundOrTmplSprite(pastedName)) {
            applyDecorChange(selectedTileX, selectedTileY, pastedName, pastedScaleX,
                    pastedScaleY, pastedOffsetX, pastedOffsetY, pastedZOrder, undoEntry);
            pushUndoEntry(undoEntry);
            invalidateGroundAround(selectedTileX, selectedTileY, pastedName);
            showEditorMessage("Pasted decor: " + pastedName + " at (" + selectedTileX + ", " + selectedTileY + ")");
            log.info("Pasted decor: {} at ({}, {})", pastedName, selectedTileX, selectedTileY);
            return;
        }
        applyTileChangeWithOffsetAndOrder(selectedTileX, selectedTileY, pastedName, pastedScaleX,
                pastedScaleY, pastedOffsetX, pastedOffsetY, pastedZOrder, undoEntry);
        pushUndoEntry(undoEntry);

        invalidateGroundAround(selectedTileX, selectedTileY, pastedName);
        showEditorMessage("Pasted: " + copiedTile.spriteName + " at (" + selectedTileX + ", " + selectedTileY + ")");
        log.info("Pasted tile: {} at ({}, {})", pastedName, selectedTileX, selectedTileY);
    }

    private boolean isDecorTile(int tileX, int tileY) {
        if (mapReader == null || tileX < 0 || tileY < 0 || tileX >= mapReader.getWidth()
                || tileY >= mapReader.getHeight()) {
            return false;
        }
        ResolvedSprite resolved = resolveSpriteAt(tileX, tileY);
        if (resolved == null || resolved.name == null) {
            return false;
        }
        return !isGroundSpriteName(resolved.name);
    }

    private boolean deleteSelectedDecorsFromSelection() {
        RectangleSelection selection = rectangleSelection != null
                ? rectangleSelection
                : (!lassoSelectionTiles.isEmpty() ? buildSelectionBoundsFromLasso() : null);
        if (selection == null || mapReader == null) {
            return false;
        }
        List<long[]> decors = new ArrayList<>();
        Set<Long> selectedDecorKeys = new HashSet<>();
        forEachSelectedTile(selection, (x, y) -> {
            if (isDecorTile(x, y)) {
                long key = packTileKey(x, y);
                if (selectedDecorKeys.add(key)) {
                    decors.add(new long[] { x, y });
                }
            }
        });
        if (decors.isEmpty()) {
            return false;
        }

        commitPendingOffsets();
        UndoEntry undoEntry = newUndoEntry(decors.size() == 1 ? "Delete decor" : "Delete decors", false);
        Map<Long, String> virtualGrounds = new HashMap<>();
        int deleted = 0;
        int skipped = 0;
        for (long[] pos : decors) {
            int x = (int) pos[0];
            int y = (int) pos[1];
            String replacement = inferGroundFromNeighbors(x, y, selectedDecorKeys, virtualGrounds);
            if (replacement == null || replacement.isBlank()) {
                skipped++;
                continue;
            }
            applyTileChangeWithOffsetAndOrder(x, y, replacement, 1f, 1f, 0f, 0f, 0, undoEntry);
            virtualGrounds.put(packTileKey(x, y), replacement);
            deleted++;
        }
        if (undoEntry.changes.isEmpty()) {
            showEditorMessage("No selected decor could be replaced from neighboring ground");
            return true;
        }

        pushUndoEntry(undoEntry);
        invalidateSelectionCaches(selection);
        if (selectedDecorInfo != null && selectedDecorKeys.contains(packTileKey(selectedDecorInfo.tileX, selectedDecorInfo.tileY))) {
            selectedDecorInfo = null;
        }
        minimapDirty = true;
        showEditorMessage(skipped == 0
                ? "Deleted " + deleted + " selected decor" + (deleted == 1 ? "" : "s")
                : "Deleted " + deleted + " selected decor" + (deleted == 1 ? "" : "s") + ", skipped " + skipped);
        return true;
    }

    private String getSelectableTileSpriteName(int tileX, int tileY) {
        if (mapReader == null || tileX < 0 || tileY < 0 || tileX >= mapReader.getWidth()
                || tileY >= mapReader.getHeight()) {
            return null;
        }
        if (decorVisible) {
            return mapReader.getSpriteName(tileX, tileY);
        }
        return mapReader.getGroundSpriteName(tileX, tileY);
    }

    private String applyHiddenDecorGroundSelection(int tileX, int tileY, String groundName) {
        if (decorVisible || groundName == null || groundName.isBlank() || !isDecorTile(tileX, tileY)) {
            return groundName;
        }
        return groundName;
    }

    private boolean isGroundSpriteName(String spriteName) {
        if (spriteName == null || mapRenderer == null) {
            return false;
        }
        ResolvedSprite resolved = SpriteNameParser.parse(spriteName, mapRenderer.getMetaByName());
        String name = resolved != null && resolved.name != null ? resolved.name : spriteName;
        var meta = mapRenderer.getMetaByName().get(name.toLowerCase(Locale.ROOT));
        return meta != null && meta.isGround();
    }

    private boolean isUsableGroundForRepair(String spriteName) {
        if (!isGroundSpriteName(spriteName)) {
            return false;
        }
        ResolvedSprite resolved = SpriteNameParser.parse(spriteName, mapRenderer.getMetaByName());
        String resolvedName = resolved != null && resolved.name != null ? resolved.name : spriteName;
        // "Black Tile" is the map's explicit void placeholder. Under a decor it produces
        // the same visible hole as an absent ground and must not influence terrain inference.
        return !"Black Tile".equalsIgnoreCase(resolvedName.trim());
    }

    private boolean isKnownDecorSpriteName(String spriteName) {
        if (spriteName == null || spriteName.isBlank() || mapRenderer == null) {
            return false;
        }
        ResolvedSprite resolved = SpriteNameParser.parse(spriteName, mapRenderer.getMetaByName());
        if (resolved == null || resolved.name == null) {
            return false;
        }
        var meta = mapRenderer.getMetaByName().get(resolved.name.toLowerCase(Locale.ROOT));
        return meta != null && !meta.isGround();
    }

    private boolean isHouseFloorName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return false;
        }
        return rawName.toLowerCase(Locale.ROOT).startsWith("floor:");
    }

    private boolean isWallSpriteName(String spriteName) {
        if (spriteName == null || spriteName.isBlank()) {
            return false;
        }
        String base = extractBaseName(spriteName);
        if (base == null || base.isBlank()) {
            return false;
        }
        return isHouseWallBaseName(base);
    }

    private boolean isHouseWallBaseName(String base) {
        if (base == null || base.isBlank()) {
            return false;
        }
        String lower = base.toLowerCase(Locale.ROOT);
        return (isCastleWallBaseName(lower) || lower.contains("wall") || lower.contains("rampart"))
                && !lower.startsWith("floor:");
    }

    private boolean isCastleWallBaseName(String lowerBase) {
        if (lowerBase == null || lowerBase.isBlank()) {
            return false;
        }
        return lowerBase.startsWith("castlewall");
    }

    private void invalidateSelectionCaches(RectangleSelection selection) {
        if (mapRenderer == null || selection == null) {
            return;
        }
        if (!lassoSelectionTiles.isEmpty()) {
            for (long key : lassoSelectionTiles) {
                int tileX = unpackTileX(key);
                int tileY = unpackTileY(key);
                for (int y = tileY - 1; y <= tileY + 1; y++) {
                    for (int x = tileX - 1; x <= tileX + 1; x++) {
                        if (mapRenderer.getGroundRenderer() != null) {
                            mapRenderer.getGroundRenderer().invalidateTileCache(x, y);
                        }
                        mapRenderer.invalidateDecorTileCache(x, y);
                    }
                }
            }
            return;
        }
        for (int y = selection.startY - 1; y <= selection.endY + 1; y++) {
            for (int x = selection.startX - 1; x <= selection.endX + 1; x++) {
                if (mapRenderer.getGroundRenderer() != null) {
                    mapRenderer.getGroundRenderer().invalidateTileCache(x, y);
                }
                mapRenderer.invalidateDecorTileCache(x, y);
            }
        }
    }

    private interface SelectedTileConsumer {
        void accept(int x, int y);
    }

    private void forEachSelectedTile(RectangleSelection selection, SelectedTileConsumer consumer) {
        if (selection == null || consumer == null || mapReader == null) {
            return;
        }
        if (!lassoSelectionTiles.isEmpty()) {
            for (long key : lassoSelectionTiles) {
                int x = unpackTileX(key);
                int y = unpackTileY(key);
                if (x >= 0 && y >= 0 && x < mapReader.getWidth() && y < mapReader.getHeight()) {
                    consumer.accept(x, y);
                }
            }
            return;
        }
        int startX = Math.max(0, selection.startX);
        int startY = Math.max(0, selection.startY);
        int endX = Math.min(mapReader.getWidth() - 1, selection.endX);
        int endY = Math.min(mapReader.getHeight() - 1, selection.endY);
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                consumer.accept(x, y);
            }
        }
    }

    private void addTileToLassoSelection(int tileX, int tileY) {
        if (!isTileInMap(tileX, tileY)) {
            return;
        }
        long key = packTileKey(tileX, tileY);
        lassoSelectionTiles.add(key);
        lassoSelectionPath.add(key);
    }

    private void addTileToLassoSelectionPath(int tileX, int tileY) {
        if (!isTileInMap(tileX, tileY)) {
            return;
        }
        if (lastLassoTileX == Integer.MIN_VALUE || lastLassoTileY == Integer.MIN_VALUE) {
            addTileToLassoSelection(tileX, tileY);
            lastLassoTileX = tileX;
            lastLassoTileY = tileY;
            return;
        }
        int dx = tileX - lastLassoTileX;
        int dy = tileY - lastLassoTileY;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps <= 0) {
            addTileToLassoSelection(tileX, tileY);
            return;
        }
        for (int i = 1; i <= steps; i++) {
            int x = Math.round(lastLassoTileX + dx * (i / (float) steps));
            int y = Math.round(lastLassoTileY + dy * (i / (float) steps));
            addTileToLassoSelection(x, y);
        }
        lastLassoTileX = tileX;
        lastLassoTileY = tileY;
    }

    private void fillClosedLassoSelection() {
        if (lassoSelectionPath.size() < 3 || mapReader == null) {
            return;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (long key : lassoSelectionPath) {
            int x = unpackTileX(key);
            int y = unpackTileY(key);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        maxX = Math.min(mapReader.getWidth() - 1, maxX);
        maxY = Math.min(mapReader.getHeight() - 1, maxY);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInsideLasso(x + 0.5f, y + 0.5f)) {
                    lassoSelectionTiles.add(packTileKey(x, y));
                }
            }
        }
    }

    private boolean isPointInsideLasso(float x, float y) {
        boolean inside = false;
        int count = lassoSelectionPath.size();
        for (int i = 0, j = count - 1; i < count; j = i++) {
            float xi = unpackTileX(lassoSelectionPath.get(i)) + 0.5f;
            float yi = unpackTileY(lassoSelectionPath.get(i)) + 0.5f;
            float xj = unpackTileX(lassoSelectionPath.get(j)) + 0.5f;
            float yj = unpackTileY(lassoSelectionPath.get(j)) + 0.5f;
            boolean intersects = ((yi > y) != (yj > y))
                    && (x < (xj - xi) * (y - yi) / ((yj - yi) == 0f ? 0.0001f : (yj - yi)) + xi);
            if (intersects) {
                inside = !inside;
            }
        }
        return inside;
    }

    private RectangleSelection buildSelectionBoundsFromLasso() {
        if (lassoSelectionTiles.isEmpty()) {
            return new RectangleSelection((int) selectionStart.x, (int) selectionStart.y,
                    (int) selectionStart.x, (int) selectionStart.y);
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (long key : lassoSelectionTiles) {
            int x = unpackTileX(key);
            int y = unpackTileY(key);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        return new RectangleSelection(minX, minY, maxX, maxY);
    }

    private int unpackTileX(long key) {
        return (int) (key >> 32);
    }

    private int unpackTileY(long key) {
        return (int) key;
    }

    private void pasteCopiedAreaSelection() {
        UndoEntry undoEntry = newUndoEntry("Paste house", true);
        int pasted = 0;
        for (CopiedAreaTile tile : copiedAreaTiles) {
            int dstX = selectedTileX + tile.dx;
            int dstY = selectedTileY + tile.dy;
            if (dstX < 0 || dstY < 0 || dstX >= mapReader.getWidth() || dstY >= mapReader.getHeight()) {
                continue;
            }
            applyCopiedAreaTile(dstX, dstY, tile, undoEntry);
            pasted++;
        }
        pushUndoEntry(undoEntry);
        showEditorMessage("Pasted house tiles: " + pasted);
        log.info("Pasted filtered house selection: {} tiles at ({}, {})", pasted, selectedTileX, selectedTileY);
    }

    private List<CopiedAreaTile> buildWallSelectionSnapshot(RectangleSelection selection) {
        List<CopiedAreaTile> filtered = new ArrayList<>();
        forEachSelectedTile(selection, (x, y) -> {
            String srcName = mapReader.getSpriteName(x, y);
            if (!isWallSpriteName(srcName)) {
                return;
            }
            filtered.add(new CopiedAreaTile(
                    x - selection.startX,
                    y - selection.startY,
                    srcName,
                    mapReader.getScaleX(x, y),
                    mapReader.getScaleY(x, y),
                    mapReader.getOffsetX(x, y),
                    mapReader.getOffsetY(x, y),
                    mapReader.getZOrder(x, y)));
        });
        return filtered;
    }

    private int[] resolveCopySourceTile() {
        if (decorVisible && selectedDecorInfo != null) {
            return new int[] { selectedDecorInfo.tileX, selectedDecorInfo.tileY };
        }
        return new int[] { selectedTileX, selectedTileY };
    }

    private boolean copySingleTileAt(int tileX, int tileY) {
        String rawName = getSelectableTileSpriteName(tileX, tileY);
        if ((rawName == null || rawName.isBlank()) && decorVisible && selectedDecorInfo != null) {
            rawName = selectedDecorInfo.spriteName;
        }
        if (rawName == null || rawName.isBlank()) {
            return false;
        }
        boolean copiedGroundLayer = !decorVisible && isDecorTile(tileX, tileY);
        copiedTile = new CopiedTile(
                rawName,
                rawName.trim().endsWith("M"),
                copiedGroundLayer ? 1f : mapReader.getScaleX(tileX, tileY),
                copiedGroundLayer ? 1f : mapReader.getScaleY(tileX, tileY),
                copiedGroundLayer ? 0f : mapReader.getOffsetX(tileX, tileY),
                copiedGroundLayer ? 0f : mapReader.getOffsetY(tileX, tileY),
                copiedGroundLayer ? 0 : mapReader.getZOrder(tileX, tileY));
        copiedAreaTiles = null;
        showEditorMessage("Copied: " + rawName);
        log.info("Copied tile: {}", rawName);
        return true;
    }

    private void applyCopiedAreaTile(int dstX, int dstY, CopiedAreaTile tile, UndoEntry undoEntry) {
        applyTileChangeWithOffsetAndOrder(
                dstX,
                dstY,
                tile.spriteName,
                tile.scaleX,
                tile.scaleY,
                tile.offsetX,
                tile.offsetY,
                tile.zOrder,
                undoEntry);
        invalidateGroundAround(dstX, dstY, tile.spriteName);
    }

    private void performBaseFloodFill(int startX, int startY, String replacementBase, boolean persist) {
        if (replacementBase == null || replacementBase.isBlank()) {
            showEditorMessage("No tile to fill with");
            return;
        }

        if (!ensureMapLoaded()) {
            return;
        }

        if (startX < 0 || startY < 0 || startX >= mapReader.getWidth() || startY >= mapReader.getHeight()) {
            return;
        }

        int seedX = startX;
        int seedY = startY;
        String startName = mapReader.getSpriteName(startX, startY);
        String targetBase = extractBaseName(startName);
        boolean targetVoid = startName == null || startName.isBlank();
        String targetExactName = null;
        if (!targetVoid) {
            if (isGroundSprite(startName)) {
                if (targetBase == null || targetBase.isBlank()) {
                    showEditorMessage("No target ground tile");
                    return;
                }
            } else {
                // Non-ground tiles: fill contiguous region by exact raw sprite name.
                targetExactName = startName;
            }
        }

        UndoEntry undoEntry = newUndoEntry("Autofill", persist);
        int fillCount = floodFillByBase(seedX, seedY, targetBase, replacementBase, targetVoid, targetExactName, undoEntry);
        if (fillCount <= 0) {
            showEditorMessage("Nothing to fill");
            return;
        }
        pushUndoEntry(undoEntry);

        // Persist to disk only when requested. In autofill mode we typically don't want
        // to save automatically.
        if (persist) {
            saveMapData();
        }

        // Update renderer and caches so the change is visible immediately even when not
        // persisted
        invalidateGroundAfterBulkChange(replacementBase);
        if (mapRenderer == null) {
            try {
                reloadMapData();
            } catch (GameException e) {
                log.error("Failed to reload after flood fill", e);
            }
        }
        showEditorMessage(
                "Flood filled " + fillCount + " tiles with " + replacementBase + (persist ? "" : " (not saved)"));
        log.info("Flood filled {} tiles (persist={})", fillCount, persist);
    }

    private void applyAutofillRectangle(RectangleSelection selection, String replacementBase, boolean persist) {
        if (selection == null) {
            return;
        }
        if (replacementBase == null || replacementBase.isBlank()) {
            showEditorMessage("No tile to fill with");
            return;
        }
        if (!ensureMapLoaded()) {
            return;
        }
        int width = mapReader.getWidth();
        int height = mapReader.getHeight();
        int startX = Math.max(0, selection.startX);
        int startY = Math.max(0, selection.startY);
        int endX = Math.min(width - 1, selection.endX);
        int endY = Math.min(height - 1, selection.endY);
        if (startX > endX || startY > endY) {
            return;
        }

        UndoEntry undoEntry = newUndoEntry("Autofill rectangle", persist);
        GroundFillPattern replacementPattern = getGroundFillPattern(replacementBase);
        int fillCount = 0;
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                String currentName = mapReader.getSpriteName(x, y);
                if (!isGroundSprite(currentName)) {
                    continue;
                }
                String newName = buildReplacementName(currentName, replacementBase, x, y, replacementPattern);
                if (newName == null) {
                    int dx = x - startX;
                    int dy = y - startY;
                    newName = buildFallbackGridReplacementName(replacementBase, dx, dy, replacementPattern);
                }
                applyGroundAutofillChange(x, y, currentName, newName, undoEntry);
                fillCount++;
            }
        }

        pushUndoEntry(undoEntry);
        invalidateGroundAfterBulkChange(replacementBase);
        if (mapRenderer == null) {
            try {
                reloadMapData();
            } catch (GameException e) {
                log.error("Failed to reload after autofill rectangle", e);
            }
        }
        showEditorMessage("Autofill " + fillCount + " tiles"
                + (persist ? "" : " (not saved)"));
        log.info("Autofill rectangle {} tiles (persist={})", fillCount, persist);
    }

    private void recalculateAllGroundTextures() {
        recalculateGroundTextures(null);
    }

    /** Recalculate (autofill) ground textures of the currently visible viewport only. */
    private void recalculateViewportGroundTextures() {
        if (mapReader == null) {
            showEditorMessage("Map not loaded");
            return;
        }
        recalculateGroundTextures(calculateVisibleBounds(0));
    }

    /**
     * Recalculate ground textures (autofill) for the given bounds, or the whole map
     * when {@code bounds} is null. Bounds are {startX, endX, startY, endY}.
     */
    private void recalculateGroundTextures(int[] bounds) {
        if (groundRecalculating) {
            showEditorMessage("Ground recalculation already running");
            log.info("Ground recalculation request ignored: already running at {}% ({})",
                    Math.round(groundRecalculationProgress * 100f), groundRecalculationStage);
            return;
        }
        if (mapReader == null || mapRenderer == null) {
            showEditorMessage("Map not loaded");
            return;
        }
        boolean viewportOnly = bounds != null;
        groundRecalculating = true;
        groundRecalculationProgress = 0f;
        groundRecalculationStage = viewportOnly ? "Scanning viewport" : "Scanning map";
        showEditorMessage("Recalculating ground" + (viewportOnly ? " (viewport)" : "") + ": 0%");
        log.info("Ground recalculation started for map {} ({}x{}), viewportOnly={}", currentMapPath,
                mapReader.getWidth(), mapReader.getHeight(), viewportOnly);

        groundRecalculationExecutor.submit(() -> {
            try {
                List<GroundRecalculationChange> changes = buildGroundRecalculationChangesWithProgress(bounds);
                Gdx.app.postRunnable(() -> beginGroundRecalculationApply(changes));
            } catch (Throwable t) {
                log.error("Ground recalculation failed at {}% ({})",
                        Math.round(groundRecalculationProgress * 100f), groundRecalculationStage, t);
                Gdx.app.postRunnable(() -> {
                    groundRecalculating = false;
                    groundRecalculationStage = "Failed";
                    showEditorMessage("Error: ground recalculation failed");
                });
            }
        });
    }

    /**
     * Repairs black tiles caused by a missing/invalid ground below a decor. It also migrates
     * legacy tiles where the decor itself was stored in the ground layer. Ground inference is
     * performed in rounds so contiguous holes can be filled from their valid perimeter.
     * Triggered from Tools > Autofill or with F12.
     */
    private void repairMissingGroundUnderDecors() {
        if (groundRecalculating) {
            showEditorMessage("Operation already running");
            return;
        }
        if (mapReader == null || mapRenderer == null) {
            showEditorMessage("Map not loaded");
            return;
        }
        groundRecalculating = true;
        groundRecalculationProgress = 0f;
        groundRecalculationStage = "Repairing missing ground";
        showEditorMessage("Repairing missing ground: 0%");
        log.info("Missing ground repair started for map {} ({}x{})", currentMapPath,
                mapReader.getWidth(), mapReader.getHeight());

        groundRecalculationExecutor.submit(() -> {
            try {
                Map<Long, String> targetDecors = new LinkedHashMap<>();
                Set<Long> misplacedDecors = new HashSet<>();
                int width = mapReader.getWidth();
                int height = mapReader.getHeight();
                int total = Math.max(1, width * height);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        String decorRaw = mapReader.getDecorSpriteName(x, y);
                        String groundRaw = mapReader.getGroundSpriteName(x, y);
                        boolean hasDecor = decorRaw != null && !decorRaw.isBlank();
                        boolean validGround = isGroundSpriteName(groundRaw);
                        boolean usableGround = isUsableGroundForRepair(groundRaw);
                        long key = packTileKey(x, y);
                        if (hasDecor && !usableGround) {
                            targetDecors.put(key, decorRaw);
                        } else if (!hasDecor && groundRaw != null && !groundRaw.isBlank() && !validGround) {
                            if (isKnownDecorSpriteName(groundRaw)) {
                                // Legacy corruption: a known decor occupies the ground slot.
                                targetDecors.put(key, groundRaw);
                                misplacedDecors.add(key);
                            } else {
                                // Unknown references such as legacy hexadecimal IDs (e.g. 0D58)
                                // are missing ground tiles, not decors to migrate.
                                targetDecors.put(key, null);
                            }
                        }
                    }
                    groundRecalculationProgress = (Math.min(total, (y + 1) * width) / (float) total) * 0.60f;
                }

                Set<Long> unresolved = new LinkedHashSet<>(targetDecors.keySet());
                Map<Long, String> inferredGrounds = new LinkedHashMap<>();
                while (!unresolved.isEmpty()) {
                    Map<Long, String> round = new LinkedHashMap<>();
                    for (long key : unresolved) {
                        int x = (int) (key >> 32);
                        int y = (int) key;
                        String fill = inferGroundFromNeighbors(x, y, targetDecors.keySet(), inferredGrounds);
                        if (fill != null && !fill.isBlank()) {
                            round.put(key, fill);
                        }
                    }
                    if (round.isEmpty()) {
                        break;
                    }
                    inferredGrounds.putAll(round);
                    unresolved.removeAll(round.keySet());
                }
                groundRecalculationProgress = 0.95f;
                Gdx.app.postRunnable(() -> applyMissingGroundRepair(
                        targetDecors, misplacedDecors, inferredGrounds, unresolved.size()));
            } catch (Throwable t) {
                log.error("Missing ground repair failed", t);
                Gdx.app.postRunnable(() -> {
                    groundRecalculating = false;
                    groundRecalculationStage = "Failed";
                    showEditorMessage("Error: missing ground repair failed");
                });
            }
        });
    }

    private void applyMissingGroundRepair(Map<Long, String> targetDecors, Set<Long> misplacedDecors,
                                          Map<Long, String> inferredGrounds, int unresolvedCount) {
        if (targetDecors == null || targetDecors.isEmpty()) {
            groundRecalculating = false;
            groundRecalculationProgress = 1f;
            groundRecalculationStage = "Done";
            showEditorMessage("No missing ground found");
            return;
        }
        if (inferredGrounds == null || inferredGrounds.isEmpty()) {
            groundRecalculating = false;
            groundRecalculationProgress = 1f;
            groundRecalculationStage = "Done";
            showEditorMessage("Ground repair: 0 repaired, " + unresolvedCount + " unresolved");
            log.info("Missing ground repair complete: 0 repaired, 0 migrated, {} unresolved", unresolvedCount);
            return;
        }
        UndoEntry undo = newUndoEntry("Repair missing ground", false);
        int migrated = 0;
        for (Map.Entry<Long, String> entry : inferredGrounds.entrySet()) {
            long key = entry.getKey();
            int x = (int) (key >> 32);
            int y = (int) key;
            String decorName = targetDecors.get(key);
            String fill = entry.getValue();

            float scaleX = mapReader.getScaleX(x, y);
            float scaleY = mapReader.getScaleY(x, y);
            float offsetX = mapReader.getOffsetX(x, y);
            float offsetY = mapReader.getOffsetY(x, y);
            int zOrder = mapReader.getZOrder(x, y);
            String oldGround = mapReader.getGroundSpriteName(x, y);

            if (misplacedDecors.contains(key)) {
                undo.changes.add(new TileChange(x, y, null, scaleX, scaleY, offsetX, offsetY, zOrder,
                        decorName, scaleX, scaleY, offsetX, offsetY, zOrder, true));
                mapReader.setDecorSpriteName(x, y, decorName);
                migrated++;
            }
            undo.changes.add(new TileChange(x, y, oldGround, scaleX, scaleY, offsetX, offsetY, zOrder,
                    fill, scaleX, scaleY, offsetX, offsetY, zOrder, false, true));

            mapReader.setGroundSpriteName(x, y, fill);
            if (mapRenderer != null) {
                mapRenderer.getGroundRenderer().invalidateTileCache(x, y);
                mapRenderer.invalidateDecorTileCache(x, y);
            }
        }
        pushUndoEntry(undo);
        mapDirty = true;
        minimapDirty = true;
        groundRecalculating = false;
        groundRecalculationProgress = 1f;
        groundRecalculationStage = "Done";
        showEditorMessage("Ground repair: " + inferredGrounds.size() + " repaired, "
                + unresolvedCount + " unresolved");
        log.info("Missing ground repair complete: {} repaired, {} migrated, {} unresolved",
                inferredGrounds.size(), migrated, unresolvedCount);
    }

    private List<GroundRecalculationChange> buildGroundRecalculationChangesWithProgress(int[] bounds) {
        List<GroundRecalculationChange> changes = new ArrayList<>();
        int width = mapReader.getWidth();
        int height = mapReader.getHeight();
        int startX = bounds != null ? Math.max(0, bounds[0]) : 0;
        int endX = bounds != null ? Math.min(width - 1, bounds[1]) : width - 1;
        int startY = bounds != null ? Math.max(0, bounds[2]) : 0;
        int endY = bounds != null ? Math.min(height - 1, bounds[3]) : height - 1;
        int rows = Math.max(1, endY - startY + 1);
        int cols = Math.max(1, endX - startX + 1);
        int total = Math.max(1, rows * cols);
        int skipped = 0;
        int nextLoggedPercent = 0;
        groundRecalculationStage = bounds != null ? "Scanning viewport" : "Scanning map";
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                // Recalculate the explicit ground layer. getSpriteName() resolves the
                // decor first and would therefore skip ground mosaics hidden below it.
                String rawName = mapReader.getGroundSpriteName(x, y);
                String groundName = resolveGroundNameForAutofill(rawName);
                if (groundName == null || groundName.isBlank()) {
                    skipped++;
                    continue;
                }
                String base = extractBaseName(groundName);
                if (base == null || base.isBlank()) {
                    skipped++;
                    continue;
                }
                GroundFillPattern pattern = getGroundFillPattern(base);
                if (pattern == null) {
                    skipped++;
                    continue;
                }
                String recalculated = groundMosaicCatalog.tileNameForExistingFrame(
                        groundName, x, y, this::groundSpriteNameAt);
                if (recalculated == null) {
                    recalculated = buildMapPositionReplacementName(base, x, y, pattern);
                }
                if (recalculated == null || recalculated.isBlank() || Objects.equals(groundName, recalculated)) {
                    continue;
                }
                changes.add(new GroundRecalculationChange(
                        x,
                        y,
                        rawName,
                        mapReader.getScaleX(x, y),
                        mapReader.getScaleY(x, y),
                        mapReader.getOffsetX(x, y),
                        mapReader.getOffsetY(x, y),
                        mapReader.getZOrder(x, y),
                        recalculated,
                        isDecorTile(x, y)));
            }
            int processed = Math.min(total, (y - startY + 1) * cols);
            groundRecalculationProgress = (processed / (float) total) * 0.95f;
            int percent = Math.round(groundRecalculationProgress * 100f);
            if (percent >= nextLoggedPercent) {
                log.info("Ground recalculation progress: {}% - scanned {}/{} rows, {} changes, {} skipped",
                        percent, y - startY + 1, rows, changes.size(), skipped);
                nextLoggedPercent += 5;
            }
        }
        groundRecalculationProgress = 0.95f;
        log.info("Ground recalculation scan complete: {} changes prepared, {} skipped", changes.size(), skipped);
        return changes;
    }

    private String groundSpriteNameAt(int x, int y) {
        if (mapReader == null || x < 0 || y < 0 || x >= mapReader.getWidth() || y >= mapReader.getHeight()) {
            return null;
        }
        return mapReader.getGroundSpriteName(x, y);
    }

    private void beginGroundRecalculationApply(List<GroundRecalculationChange> changes) {
        if (changes == null || changes.isEmpty()) {
            groundRecalculationProgress = 1f;
            groundRecalculationStage = "No changes";
            groundRecalculating = false;
            showEditorMessage("No ground textures recalculated");
            log.info("Ground recalculation finished: no changes");
            return;
        }
        groundRecalculationProgress = 0.95f;
        groundRecalculationStage = "Applying changes";
        pendingGroundRecalculationChanges = changes;
        pendingGroundRecalculationUndo = newUndoEntry("Recalculate ground textures", false);
        pendingGroundRecalculationIndex = 0;
        pendingGroundRecalculationChangedCount = 0;
        pendingGroundRecalculationScannedCount = changes.size();
        pendingGroundRecalculationSampleLogCount = 0;
        pendingGroundRecalculationNextLogPercent = 95;
        log.info("Ground recalculation progress: 95% - applying {} prepared changes", changes.size());
    }

    private void processPendingGroundRecalculationApply() {
        if (pendingGroundRecalculationChanges == null) {
            return;
        }
        int total = pendingGroundRecalculationChanges.size();
        int end = Math.min(total, pendingGroundRecalculationIndex + GROUND_RECALC_APPLY_BATCH_SIZE);
        for (int i = pendingGroundRecalculationIndex; i < end; i++) {
            GroundRecalculationChange change = pendingGroundRecalculationChanges.get(i);
            String newRawName = change.newGroundName;
            pendingGroundRecalculationUndo.changes.add(new TileChange(change.x, change.y, change.oldRawName,
                    change.oldScaleX, change.oldScaleY, change.oldOffsetX, change.oldOffsetY, change.oldZOrder,
                    newRawName, change.newScaleX(), change.newScaleY(), change.newOffsetX(), change.newOffsetY(),
                    change.newZOrder(), false, change.decorTile));
            if (change.decorTile) {
                mapReader.setGroundSpriteName(change.x, change.y, change.newGroundName);
            } else {
                mapReader.setSpriteNameFast(change.x, change.y, newRawName);
                mapReader.setScale(change.x, change.y, change.newScaleX(), change.newScaleY());
                mapReader.setOffset(change.x, change.y, change.newOffsetX(), change.newOffsetY());
                mapReader.setZOrder(change.x, change.y, change.newZOrder());
            }
            pendingGroundRecalculationChangedCount++;
            if (pendingGroundRecalculationSampleLogCount < 10) {
                log.info("Ground recalculation sample {} at ({}, {}): '{}' -> '{}'",
                        pendingGroundRecalculationSampleLogCount + 1, change.x, change.y, change.oldRawName,
                        newRawName);
                pendingGroundRecalculationSampleLogCount++;
            }
        }
        pendingGroundRecalculationIndex = end;
        float applyProgress = total == 0 ? 1f : pendingGroundRecalculationIndex / (float) total;
        groundRecalculationProgress = 0.95f + applyProgress * 0.05f;
        int percent = Math.round(groundRecalculationProgress * 100f);
        if (percent >= pendingGroundRecalculationNextLogPercent) {
            groundRecalculationStage = "Applying changes";
            log.info("Ground recalculation progress: {}% - applied {}/{} changes", percent,
                    pendingGroundRecalculationIndex, total);
            pendingGroundRecalculationNextLogPercent++;
        }
        if (pendingGroundRecalculationIndex < total) {
            return;
        }

        pushUndoEntry(pendingGroundRecalculationUndo);
        mapDirty = true;
        minimapDirty = true;
        invalidateGroundAfterBulkChange("Autofill map");
        groundRecalculationProgress = 1f;
        groundRecalculationStage = "Done";
        groundRecalculating = false;
        showEditorMessage("Recalculated " + pendingGroundRecalculationChangedCount + " ground texture"
                + (pendingGroundRecalculationChangedCount == 1 ? "" : "s"));
        log.info("Ground recalculation complete: applied {} changes", pendingGroundRecalculationChangedCount);

        pendingGroundRecalculationChanges = null;
        pendingGroundRecalculationUndo = null;
        pendingGroundRecalculationIndex = 0;
        pendingGroundRecalculationChangedCount = 0;
        pendingGroundRecalculationScannedCount = 0;
        pendingGroundRecalculationSampleLogCount = 0;
    }

    private String resolveGroundNameForAutofill(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return null;
        }
        if (startsWithIgnoreCase(rawName, "Tmpl")) {
            return null;
        }
        if (isGroundSprite(rawName)) {
            return rawName;
        }
        return null;
    }

    private String buildMapPositionReplacementName(String base, int x, int y, GroundFillPattern pattern) {
        if (base == null || base.isBlank() || pattern == null) {
            return base;
        }
        String mosaicName = groundMosaicCatalog.tileName(base, x, y);
        if (mosaicName != null) {
            return mosaicName;
        }
        if (pattern.numeric()) {
            int variant = predictFillVariant(pattern.numericType(), pattern.variants(), x, y);
            return String.format("%s %d", base, variant);
        }
        if (pattern.grid()) {
            return buildFallbackGridReplacementName(base, x, y, pattern);
        }
        return base;
    }
/**
 * Class representing CopiedAreaTile.
 */

    private static class CopiedAreaTile {
        int dx;
        int dy;
        String spriteName;
        float scaleX;
        float scaleY;
        float offsetX;
        float offsetY;
        int zOrder;

        CopiedAreaTile(int dx, int dy, String spriteName, float scaleX, float scaleY, float offsetX, float offsetY, int zOrder) {
            this.dx = dx;
            this.dy = dy;
            this.spriteName = spriteName;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.zOrder = zOrder;
        }
    }

    private int floodFillByBase(int startX, int startY, String targetBase, String replacementBase, boolean targetVoid,
            String targetExactName,
            UndoEntry undoEntry) {
        int width = mapReader.getWidth();
        int height = mapReader.getHeight();
        ArrayDeque<long[]> stack = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        List<long[]> matches = new ArrayList<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        stack.push(new long[] { startX, startY });

        while (!stack.isEmpty()) {
            long[] node = stack.pop();
            int x = (int) node[0];
            int y = (int) node[1];
            if (x < 0 || y < 0 || x >= width || y >= height) {
                continue;
            }

            long key = ((long) x << 32) | (y & 0xFFFFFFFFL);
            if (visited.contains(key)) {
                continue;
            }
            visited.add(key);

            String currentName = mapReader.getSpriteName(x, y);
            if (targetVoid) {
                if (currentName != null && !currentName.isBlank()) {
                    continue;
                }
            } else if (targetExactName != null) {
                if (!equalsIgnoreCaseSafe(currentName, targetExactName)) {
                    continue;
                }
            } else {
                if (!isGroundSprite(currentName)) {
                    continue;
                }
                String currentBase = extractBaseName(currentName);
                if (!equalsIgnoreCaseSafe(currentBase, targetBase)) {
                    continue;
                }
            }

            matches.add(new long[] { x, y });
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);

            stack.push(new long[] { x + 1, y });
            stack.push(new long[] { x - 1, y });
            stack.push(new long[] { x, y + 1 });
            stack.push(new long[] { x, y - 1 });
        }

        if (matches.isEmpty()) {
            return 0;
        }

        GroundFillPattern replacementPattern = getGroundFillPattern(replacementBase);
        for (long[] pos : matches) {
            int x = (int) pos[0];
            int y = (int) pos[1];
            String currentName = mapReader.getSpriteName(x, y);
            String newName = buildReplacementName(currentName, replacementBase, x, y, replacementPattern);
            if (newName == null) {
                int dx = x - minX;
                int dy = y - minY;
                newName = buildFallbackGridReplacementName(replacementBase, dx, dy, replacementPattern);
            }
            applyGroundAutofillChange(x, y, currentName, newName, undoEntry);
        }

        return matches.size();
    }

    private void applyGroundAutofillChange(int x, int y, String currentName, String newGroundName, UndoEntry undoEntry) {
        if (currentName != null && !currentName.isBlank() && isDecorTile(x, y)) {
            applyGroundLayerChange(x, y, newGroundName, undoEntry);
            return;
        }
        applyTileChange(x, y, newGroundName, 1f, 1f, undoEntry);
    }

    private boolean isGroundSprite(String spriteName) {
        if (spriteName == null || spriteName.isBlank() || mapRenderer == null) {
            return false;
        }
        var meta = mapRenderer.getMetaByName().get(spriteName.toLowerCase(Locale.ROOT));
        return meta != null && meta.isGround();
    }

    private String buildReplacementName(String sourceName, String replacementBase, int x, int y,
            GroundFillPattern replacementPattern) {
        if (replacementPattern == null) {
            return replacementBase;
        }
        String mosaicName = groundMosaicCatalog.tileName(replacementBase, x, y);
        if (mosaicName != null) {
            return mosaicName;
        }
        if (replacementPattern.numeric()) {
            int variant = predictFillVariant(replacementPattern.numericType(), replacementPattern.variants(), x, y);
            return String.format("%s %d", replacementBase, variant);
        }
        // Always derive the grid (col, row) from the tile position so neighbouring
        // tiles tile seamlessly, even when the source tile already carried a grid
        // variant (e.g. "EarthTile (9, 2)").
        int col = x + 1;
        int row = y + 1;
        if (replacementPattern.grid()) {
            col = wrapGridValue(col, replacementPattern.cols());
            row = wrapGridValue(row, replacementPattern.rows());
        }
        return formatGridReplacementName(replacementBase, col, row);
    }

    private String buildFallbackGridReplacementName(String replacementBase, int dx, int dy,
            GroundFillPattern replacementPattern) {
        int cols = replacementPattern != null && replacementPattern.cols() > 0 ? replacementPattern.cols() : 1;
        int rows = replacementPattern != null && replacementPattern.rows() > 0 ? replacementPattern.rows() : 1;
        int col = wrapGridValue(dx + 1, cols);
        int row = wrapGridValue(dy + 1, rows);
        return formatGridReplacementName(replacementBase, col, row);
    }

    private String formatGridReplacementName(String base, int col, int row) {
        return String.format("%s (%d, %d)", base, col, row);
    }

    private GroundFillPattern getGroundFillPattern(String base) {
        if (base == null || base.isBlank()) {
            return null;
        }
        String key = base.toLowerCase(Locale.ROOT);
        if (groundFillPatterns.containsKey(key)) {
            return groundFillPatterns.get(key);
        }
        GroundFillPattern pattern = detectGroundFillPattern(base);
        groundFillPatterns.put(key, pattern);
        return pattern;
    }

    private GroundFillPattern detectGroundFillPattern(String base) {
        GroundMosaicCatalog.Dimensions mosaicDimensions = groundMosaicCatalog.dimensions(base);
        if (mosaicDimensions != null) {
            return new GroundFillPattern(true, false, mosaicDimensions.width(), mosaicDimensions.height(), null, 0);
        }
        int maxCol = 0;
        int maxRow = 0;
        int maxVariant = 0;
        for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
            String name = sprite.getName();
            if (name == null || !sprite.isGround()) {
                continue;
            }
            GridVariant grid = parseGridVariant(name);
            if (grid != null && equalsIgnoreCaseSafe(grid.base(), base)) {
                maxCol = Math.max(maxCol, grid.col());
                maxRow = Math.max(maxRow, grid.row());
                continue;
            }
            NumericVariant numeric = parseNumericVariantName(name);
            if (numeric != null && equalsIgnoreCaseSafe(numeric.base(), base)) {
                maxVariant = Math.max(maxVariant, numeric.variant());
            }
        }
        if (maxCol > 0 && maxRow > 0) {
            return new GroundFillPattern(true, false, maxCol, maxRow, null, 0);
        }
        if (maxVariant > 0) {
            return new GroundFillPattern(false, true, 0, 0, inferFillPatternType(base, maxVariant), maxVariant);
        }
        return null;
    }

    private FillPatternType inferFillPatternType(String base, int variants) {
        if (mapReader == null || variants <= 0) {
            return FillPatternType.AT;
        }
        Map<FillPatternType, Integer> scores = new HashMap<>();
        for (FillPatternType type : FillPatternType.values()) {
            scores.put(type, 0);
        }
        for (int y = 0; y < mapReader.getHeight(); y++) {
            for (int x = 0; x < mapReader.getWidth(); x++) {
                NumericVariant numeric = parseNumericVariantName(mapReader.getSpriteName(x, y));
                if (numeric == null || !equalsIgnoreCaseSafe(numeric.base(), base)) {
                    continue;
                }
                for (FillPatternType type : FillPatternType.values()) {
                    if (predictFillVariant(type, variants, x, y) == numeric.variant()) {
                        scores.merge(type, 1, Integer::sum);
                    }
                }
            }
        }
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(FillPatternType.AT);
    }

    private int predictFillVariant(FillPatternType type, int n, int x, int y) {
        return switch (type) {
            case DOLLAR -> Math.floorMod(x, n) + 1;
            case AT -> Math.floorMod(y, n) + 1;
            case HASH -> Math.floorMod(x + y, n) + 1;
            case AMPERSAND -> Math.floorMod(Math.floorMod(x, n) + Math.floorMod(y, n), n) + 1;
        };
    }

    private NumericVariant parseNumericVariantName(String name) {
        if (name == null) {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^(.+?)\\s+(\\d+)\\s*$")
                .matcher(name.trim());
        if (!matcher.matches()) {
            return null;
        }
        return new NumericVariant(matcher.group(1).trim(), Integer.parseInt(matcher.group(2)));
    }

    private int wrapGridValue(int value, int max) {
        if (max <= 0) {
            return value;
        }
        int normalized = value <= 0 ? 1 : value;
        return ((normalized - 1) % max) + 1;
    }

    private String extractBaseName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String mosaicBase = groundMosaicCatalog.baseNameForFrame(trimmed);
        if (mosaicBase != null) {
            return mosaicBase;
        }
        GridVariant variant = parseGridVariant(trimmed);
        if (variant != null && variant.base != null) {
            return variant.base;
        }
        // Handle "BaseName N" variants like "DesertTile 1" and optional mirror suffixes
        // like "Name 2M"
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^(.*?)(?:\\s+\\d+M?)$").matcher(trimmed);
        if (matcher.matches()) {
            String base = matcher.group(1).trim();
            if (!base.isEmpty()) {
                return base;
            }
        }
        return trimmed;
    }

    private GridVariant parseGridVariant(String name) {
        if (name == null)
            return null;
        String trimmed = name.trim();
        if (trimmed.isEmpty())
            return null;
        java.util.regex.Matcher matcher = GRID_VARIANT_PATTERN.matcher(trimmed);
        if (!matcher.matches())
            return null;
        String base = matcher.group(1).trim();
        int col = Integer.parseInt(matcher.group(2));
        int row = Integer.parseInt(matcher.group(3));
        return new GridVariant(base, col, row);
    }

    private boolean equalsIgnoreCaseSafe(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.equalsIgnoreCase(right);
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        if (value == null || prefix == null || value.length() < prefix.length()) {
            return false;
        }
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }
/**
 * Record for GridVariant.
 */

    private record GridVariant(String base, int col, int row) {
    }
/**
 * Enumeration of FillPatternType.
 */

    private enum FillPatternType {
        DOLLAR,
        AT,
        HASH,
        AMPERSAND
    }

    private static class Tmpl3TileSnapshot {
        final int x;
        final int y;
        final String oldName;
        final float oldScaleX;
        final float oldScaleY;
        final float oldOffsetX;
        final float oldOffsetY;
        final int oldZOrder;
        final boolean decorTile;

        Tmpl3TileSnapshot(int x, int y, String oldName, float oldScaleX, float oldScaleY, float oldOffsetX,
                float oldOffsetY, int oldZOrder, boolean decorTile) {
            this.x = x;
            this.y = y;
            this.oldName = oldName;
            this.oldScaleX = oldScaleX;
            this.oldScaleY = oldScaleY;
            this.oldOffsetX = oldOffsetX;
            this.oldOffsetY = oldOffsetY;
            this.oldZOrder = oldZOrder;
            this.decorTile = decorTile;
        }
    }

    private static class Tmpl3RegenerationChange {
        final Tmpl3TileSnapshot tile;
        final String newName;

        Tmpl3RegenerationChange(Tmpl3TileSnapshot tile, String newName) {
            this.tile = tile;
            this.newName = newName;
        }
    }

    private static class GroundRecalculationChange {
        final int x;
        final int y;
        final String oldRawName;
        final float oldScaleX;
        final float oldScaleY;
        final float oldOffsetX;
        final float oldOffsetY;
        final int oldZOrder;
        final String newGroundName;
        final boolean decorTile;

        GroundRecalculationChange(int x, int y, String oldRawName, float oldScaleX, float oldScaleY,
                float oldOffsetX, float oldOffsetY, int oldZOrder, String newGroundName, boolean decorTile) {
            this.x = x;
            this.y = y;
            this.oldRawName = oldRawName;
            this.oldScaleX = oldScaleX;
            this.oldScaleY = oldScaleY;
            this.oldOffsetX = oldOffsetX;
            this.oldOffsetY = oldOffsetY;
            this.oldZOrder = oldZOrder;
            this.newGroundName = newGroundName;
            this.decorTile = decorTile;
        }

        float newScaleX() {
            return decorTile ? oldScaleX : 1f;
        }

        float newScaleY() {
            return decorTile ? oldScaleY : 1f;
        }

        float newOffsetX() {
            return decorTile ? oldOffsetX : 0f;
        }

        float newOffsetY() {
            return decorTile ? oldOffsetY : 0f;
        }

        int newZOrder() {
            return decorTile ? oldZOrder : 0;
        }
    }

/**
 * Record for NumericVariant.
 */

    private record NumericVariant(String base, int variant) {
    }
/**
 * Record for GroundFillPattern.
 */

    private record GroundFillPattern(boolean grid, boolean numeric, int cols, int rows, FillPatternType numericType,
            int variants) {
    }

    /**
     * Full rebuild of the map's Tmpl3 transitions, used when the game must rely on
     * Tmpl3 exclusively. Runs three phases in order on the regeneration executor,
     * awaiting each GL-thread apply before starting the next:
     *   1. Reset every existing Tmpl3 to its base variant ("Tmpl3 1"), in place.
     *   2. Trace fresh Tmpl3 border rows wherever two terrain families now touch.
     *   3. Regenerate Tmpl3 masks so the new borders follow the separation curve.
     *
     * @param viewportOnly when true, limit every phase to the visible viewport
     */
    private void rebuildAllTmpl3(boolean viewportOnly) {
        rebuildAllTmpl("Tmpl3", viewportOnly);
    }

    private void rebuildAllTmpl1(boolean viewportOnly) {
        rebuildAllTmpl("Tmpl1", viewportOnly);
    }

    private void rebuildAllTmpl4(boolean viewportOnly) {
        rebuildAllTmpl("Tmpl4", viewportOnly);
    }

    /**
     * Build only the smoothing transitions allowed by the terrain rules. Every
     * existing Tmpl family is neutralized, then eligible boundaries are rebuilt as
     * one deterministic row of Tmpl3 tiles.
     */
    private void buildSmoothingTiles(boolean viewportOnly) {
        if (tmpl3Regenerating) {
            showEditorMessage("Smoothing tile build already running");
            return;
        }
        if (mapReader == null || mapRenderer == null) {
            showEditorMessage("Map not loaded");
            return;
        }

        int[] bounds = viewportOnly ? calculateVisibleBounds(0) : null;
        tmpl3Regenerating = true;
        tmpl3RegenerationProgress = 0f;
        tmpl3RegenerationStage = "Scanning smoothing rules";
        showEditorMessage("Building smoothing tiles" + (viewportOnly ? " (viewport)" : "") + "...");

        tmpl3RegenerationExecutor.submit(() -> {
            try {
                tmpl3RegenerationStage = "Cleaning existing smoothing tiles";
                Set<Long> neutralTemplateTiles = collectSmoothingNeutralTileKeys(bounds);
                Map<Long, String> terrainOverlay = buildSmoothingTerrainOverlay(neutralTemplateTiles);

                tmpl3RegenerationStage = "Scanning smoothing rules";
                SmoothingBuildPlan plan;
                smoothingTerrainOverlay = terrainOverlay;
                try {
                    plan = collectSmoothingTileTargets(bounds);
                } finally {
                    smoothingTerrainOverlay = Map.of();
                }
                Map<String, List<int[]>> targets = plan.targets();
                int targetCount = targets.values().stream().mapToInt(List::size).sum();

                if (!plan.artifactCleanup().isEmpty()) {
                    tmpl3RegenerationStage = "Removing double-row artifacts";
                    applyTileWritesBatched(plan.artifactCleanup(), "Thin smoothing borders", 0.20f, 0.30f,
                            "Smoothing tiles");
                }

                for (String templateName : List.of("Tmpl3")) {
                    List<int[]> positions = targets.getOrDefault(templateName, List.of());
                    if (!positions.isEmpty()) {
                        tmpl3RegenerationStage = "Placing " + templateName;
                        applyBorderConversionBatched(positions, templateName);
                    }
                }

                int regenerated = 0;
                for (String templateName : List.of("Tmpl3")) {
                    List<Tmpl3TileSnapshot> tiles = collectTmplTilesWithProgress(bounds, templateName);
                    if (tiles.isEmpty()) {
                        continue;
                    }
                    tmpl3RegenerationStage = "Regenerating " + templateName;
                    List<Tmpl3RegenerationChange> changes = buildTmplRegenerationChangesWithProgress(tiles,
                            templateName);
                    applyTmplRegenerationChangesBatched(changes, templateName);
                    regenerated += changes.size();
                }

                int regeneratedCount = regenerated;
                Gdx.app.postRunnable(() -> {
                    invalidateGroundAfterBulkChange("Smoothing tiles");
                    tmpl3Regenerating = false;
                    tmpl3RegenerationProgress = 1f;
                    tmpl3RegenerationStage = "Done";
                    showEditorMessage("Built smoothing tiles: " + neutralTemplateTiles.size() + " cleaned, "
                            + targetCount + " placed, "
                            + regeneratedCount + " regenerated");
                    log.info("Smoothing tile build complete: {} cleaned, {} placed, {} regenerated",
                            neutralTemplateTiles.size(), targetCount, regeneratedCount);
                });
            } catch (Throwable t) {
                smoothingTerrainOverlay = Map.of();
                log.error("Build smoothing tiles failed at {}% ({})",
                        Math.round(tmpl3RegenerationProgress * 100f), tmpl3RegenerationStage, t);
                Gdx.app.postRunnable(() -> {
                    tmpl3Regenerating = false;
                    tmpl3RegenerationStage = "Failed";
                    showEditorMessage("Error: Failed to build smoothing tiles");
                });
            }
        });
    }

    /**
     * Mark every existing smoothing template as a neutral hole for the duration of
     * the scan. The map is not backfilled with concrete terrain before rebuilding:
     * doing so would move the boundary by one tile. Keeping this neutral set in
     * memory also prevents a temporary placeholder sprite from leaking into saves or
     * undo history if rebuilding fails.
     */
    private Set<Long> collectSmoothingNeutralTileKeys(int[] bounds) {
        Set<Long> neutralTiles = new HashSet<>();
        int startX = bounds != null ? bounds[0] : 0;
        int endX = bounds != null ? bounds[1] : mapReader.getWidth() - 1;
        int startY = bounds != null ? bounds[2] : 0;
        int endY = bounds != null ? bounds[3] : mapReader.getHeight() - 1;
        int rows = Math.max(1, endY - startY + 1);

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                ResolvedSprite resolved = SpriteNameParser.parse(mapReader.getGroundSpriteName(x, y),
                        mapRenderer != null ? mapRenderer.getMetaByName() : null);
                // Only Tmpl3 is rebuilt by this pass: neutralizing a Tmpl1/Tmpl4 tile here
                // would erase it and backfill it with concrete terrain since nothing later
                // regenerates those families, silently deleting existing smoothing tiles.
                if (resolved != null && "Tmpl3".equals(tmplFamilyName(resolved.name))) {
                    neutralTiles.add(tmpl3TileKey(x, y));
                }
            }
            tmpl3RegenerationProgress = ((y - startY + 1) / (float) rows) * 0.10f;
        }
        return neutralTiles;
    }

    /**
     * Infer the concrete tile hidden by each neutral template without writing it to
     * the map. A tile touching only one side inherits that side; a genuine one-row
     * transition touching both sides inherits the deterministic carrier side used by
     * the corresponding template family.
     */
    private Map<Long, String> buildSmoothingTerrainOverlay(Set<Long> neutralTiles) {
        Map<Long, String> overlay = new HashMap<>();
        for (long key : neutralTiles) {
            int x = (int) (key >> 32);
            int y = (int) key;
            SmoothingBoundary boundary = smoothingBoundaryAround(x, y);
            String replacementTerrain = null;
            if (boundary != null) {
                String recedingFamily = normalizedSmoothingFamily(boundary.recedingTerrain());
                int advancingTouches = countDirectConcreteFamilyAround(x, y, boundary.advancingFamily());
                int recedingTouches = countDirectConcreteFamilyAround(x, y, recedingFamily);
                if (advancingTouches > 0 && recedingTouches == 0) {
                    replacementTerrain = boundary.advancingTerrain();
                } else if (recedingTouches > 0 && advancingTouches == 0) {
                    replacementTerrain = boundary.recedingTerrain();
                } else {
                    replacementTerrain = boundary.advancingTerrain();
                }
            }
            if (replacementTerrain == null) {
                replacementTerrain = uniformDirectConcreteTerrain(x, y);
            }
            if (replacementTerrain == null) {
                replacementTerrain = pickNearbyReplacementTerrain(x, y, SMOOTHING_REPLACEMENT_RADIUS);
            }
            if (replacementTerrain != null) {
                overlay.put(key, extrapolateTmpl3TerrainName(replacementTerrain, x, y));
            } else {
                log.warn("Cannot neutralize smoothing tile at ({}, {}): no concrete terrain nearby", x, y);
            }
        }
        return overlay;
    }

    private record SmoothingBuildPlan(Map<String, List<int[]>> targets, List<String[]> artifactCleanup) {
    }

    private record SmoothingBoundary(String templateName, String recedingTerrain, String advancingFamily,
            String advancingTerrain) {
    }

    private SmoothingBuildPlan collectSmoothingTileTargets(int[] bounds) {
        Map<String, LinkedHashMap<Long, int[]>> targets = new LinkedHashMap<>();
        List<String[]> artifactCleanup = new ArrayList<>();
        for (String templateName : List.of("Tmpl3")) {
            targets.put(templateName, new LinkedHashMap<>());
        }

        int startX = bounds != null ? bounds[0] : 0;
        int endX = bounds != null ? bounds[1] : mapReader.getWidth() - 1;
        int startY = bounds != null ? bounds[2] : 0;
        int endY = bounds != null ? bounds[3] : mapReader.getHeight() - 1;
        int rows = Math.max(1, endY - startY + 1);
        int cols = Math.max(1, endX - startX + 1);
        int total = Math.max(1, rows * cols);
        int[][] cardinals = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        int[][] neighbors = {
                { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
                { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
        };

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                long tileKey = tmpl3TileKey(x, y);
                String rawGround = mapReader.getGroundSpriteName(x, y);
                ResolvedSprite resolved = SpriteNameParser.parse(rawGround,
                        mapRenderer != null ? mapRenderer.getMetaByName() : null);
                String existingTemplate = smoothingTerrainOverlay.containsKey(tileKey)
                        ? null
                        : (resolved != null ? tmplFamilyName(resolved.name) : null);
                if (existingTemplate != null) {
                    // Only Tmpl3 is rebuilt by this pass: an existing Tmpl1/Tmpl4 tile is left
                    // as a neutral hole (already backfilled with concrete terrain via the
                    // overlay) rather than reclassified into a family this pass does not
                    // regenerate, which used to silently turn every Tmpl1 into Tmpl3.
                    if (!"Tmpl3".equals(existingTemplate)) {
                        continue;
                    }
                    SmoothingBoundary boundary = smoothingBoundaryAround(x, y);
                    String classified = boundary != null ? boundary.templateName() : null;
                    if (boundary == null) {
                        String replacementTerrain = uniformDirectConcreteTerrain(x, y);
                        if (replacementTerrain != null) {
                            String replacement = extrapolateTmpl3TerrainName(replacementTerrain, x, y);
                            artifactCleanup.add(new String[] { Integer.toString(x), Integer.toString(y), replacement });
                            continue;
                        }
                    }
                    String templateName = classified != null ? classified : existingTemplate;
                    putSmoothingTarget(targets, templateName, x, y);
                    continue;
                }

                String terrain = resolveDirectConcreteGroundTerrainName(x, y);
                String family = normalizedSmoothingFamily(terrain);
                if (family == null) {
                    continue;
                }
                String bestTemplate = null;
                for (int directionIndex = 0; directionIndex < neighbors.length; directionIndex++) {
                    int[] direction = neighbors[directionIndex];
                    int nx = x + direction[0];
                    int ny = y + direction[1];
                    if (!isTileInMap(nx, ny)) {
                        continue;
                    }
                    String neighborFamily = normalizedSmoothingFamily(resolveDirectConcreteGroundTerrainName(nx, ny));
                    if (neighborFamily == null || sameTerrainFamily(family, neighborFamily)) {
                        continue;
                    }
                    String candidate = smoothingTemplateFor(family, neighborFamily);
                    // Diagonal-only contact is useful for completing solid-floor
                    // corners, but the dither/water families still require a cardinal
                    // boundary so they do not grow a second transition row.
                    if (directionIndex >= cardinals.length && !"Tmpl4".equalsIgnoreCase(candidate)) {
                        continue;
                    }
                    if (candidate != null && smoothingPriority(candidate) > smoothingPriority(bestTemplate)) {
                        bestTemplate = candidate;
                    }
                }
                if (bestTemplate != null && shouldCarrySmoothingTile(family, x, y, neighbors, bestTemplate)) {
                    putSmoothingTarget(targets, bestTemplate, x, y);
                }
            }
            int processed = Math.min(total, (y - startY + 1) * cols);
            tmpl3RegenerationProgress = (processed / (float) total) * 0.20f;
        }

        Set<Long> rebuiltKeys = new HashSet<>();
        targets.values().forEach(positions -> rebuiltKeys.addAll(positions.keySet()));
        smoothingTerrainOverlay.forEach((key, replacement) -> {
            if (!rebuiltKeys.contains(key)) {
                int x = (int) (key >> 32);
                int y = (int) (long) key;
                artifactCleanup.add(new String[] { Integer.toString(x), Integer.toString(y), replacement });
            }
        });

        Map<String, List<int[]>> result = new LinkedHashMap<>();
        targets.forEach((name, positions) -> result.put(name, new ArrayList<>(positions.values())));
        return new SmoothingBuildPlan(result, artifactCleanup);
    }

    private boolean shouldCarrySmoothingTile(String family, int x, int y, int[][] directions, String templateName) {
        for (int[] direction : directions) {
            int nx = x + direction[0];
            int ny = y + direction[1];
            if (!isTileInMap(nx, ny)) {
                continue;
            }
            String neighbor = normalizedSmoothingFamily(
                    resolveDirectConcreteGroundTerrainName(nx, ny));
            String neighborTemplate = smoothingTemplateFor(family, neighbor);
            if (neighbor == null || sameTerrainFamily(family, neighbor)
                    || !templateName.equalsIgnoreCase(neighborTemplate)) {
                continue;
            }
            String carrierFamily = smoothingCarrierFamily(family, neighbor);
            if (carrierFamily != null && carrierFamily.equalsIgnoreCase(family)) {
                return true;
            }
        }
        return false;
    }

    private SmoothingBoundary smoothingBoundaryAround(int tileX, int tileY) {
        int[][] cardinalDirections = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        int[][] directions = {
                { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
                { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
        };
        Map<String, String> directTerrainByFamily = new LinkedHashMap<>();
        for (int[] direction : cardinalDirections) {
            String terrain = resolveDirectConcreteGroundTerrainName(tileX + direction[0], tileY + direction[1]);
            String family = normalizedSmoothingFamily(terrain);
            if (family != null) {
                directTerrainByFamily.putIfAbsent(family.toLowerCase(Locale.ROOT), terrain);
            }
        }
        SmoothingBoundary directBoundary = bestSmoothingBoundary(directTerrainByFamily);
        if (directBoundary != null) {
            return directBoundary;
        }
        for (int directionIndex = cardinalDirections.length; directionIndex < directions.length; directionIndex++) {
            int[] direction = directions[directionIndex];
            String terrain = resolveDirectConcreteGroundTerrainName(tileX + direction[0], tileY + direction[1]);
            String family = normalizedSmoothingFamily(terrain);
            if (family != null) {
                directTerrainByFamily.putIfAbsent(family.toLowerCase(Locale.ROOT), terrain);
            }
        }
        directBoundary = bestSmoothingBoundary(directTerrainByFamily);
        if (directBoundary != null) {
            return directBoundary;
        }

        Map<String, String> terrainByFamily = new LinkedHashMap<>();
        for (int[] direction : directions) {
            String terrain = resolveConcreteGroundTerrainInDirection(tileX, tileY, direction[0], direction[1]);
            String family = normalizedSmoothingFamily(terrain);
            if (family != null) {
                terrainByFamily.putIfAbsent(family.toLowerCase(Locale.ROOT), terrain);
            }
        }
        return bestSmoothingBoundary(terrainByFamily);
    }

    private SmoothingBoundary bestSmoothingBoundary(Map<String, String> terrainByFamily) {
        List<String> families = terrainByFamily.keySet().stream().toList();
        SmoothingBoundary best = null;
        for (int i = 0; i < families.size(); i++) {
            for (int j = i + 1; j < families.size(); j++) {
                String first = families.get(i);
                String second = families.get(j);
                if (sameTerrainFamily(first, second)) {
                    continue;
                }
                String candidate = smoothingTemplateFor(first, second);
                if (candidate != null
                        && smoothingPriority(candidate) > smoothingPriority(best != null ? best.templateName() : null)) {
                    String advancing = smoothingCarrierFamily(first, second);
                    String receding = advancing.equals(first) ? second : first;
                    best = new SmoothingBoundary(candidate, terrainByFamily.get(receding), advancing,
                            terrainByFamily.get(advancing));
                }
            }
        }
        return best;
    }

    private String uniformDirectConcreteTerrain(int tileX, int tileY) {
        int[][] cardinals = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        String selectedTerrain = null;
        String selectedFamily = null;
        for (int[] direction : cardinals) {
            int x = tileX + direction[0];
            int y = tileY + direction[1];
            if (!isTileInMap(x, y)) {
                continue;
            }
            String terrain = resolveDirectConcreteGroundTerrainName(x, y);
            String family = normalizedSmoothingFamily(terrain);
            if (family == null) {
                continue;
            }
            if (selectedFamily == null) {
                selectedFamily = family;
                selectedTerrain = terrain;
            } else if (!sameTerrainFamily(selectedFamily, family)) {
                return null;
            }
        }
        return selectedTerrain;
    }

    private int countDirectConcreteFamilyAround(int tileX, int tileY, String expectedFamily) {
        if (expectedFamily == null) {
            return 0;
        }
        int count = 0;
        for (Direction direction : Direction.values()) {
            int x = tileX + direction.dx;
            int y = tileY + direction.dy;
            if (!isTileInMap(x, y)) {
                continue;
            }
            String family = normalizedSmoothingFamily(resolveDirectConcreteGroundTerrainName(x, y));
            if (family != null && family.equalsIgnoreCase(expectedFamily)) {
                count++;
            }
        }
        return count;
    }

    private String smoothingTemplateFor(String first, String second) {
        return first != null && second != null && !sameTerrainFamily(first, second) ? "Tmpl3" : null;
    }

    private boolean isSolidSmoothingFamily(String family) {
        return sameTerrainFamily(family, "RockFloor") || sameTerrainFamily(family, "Floor: Wooden");
    }

    private String smoothingCarrierFamily(String first, String second) {
        if (isSolidSmoothingFamily(first) != isSolidSmoothingFamily(second)) {
            return isSolidSmoothingFamily(first) ? first : second;
        }
        if (isProtectedTerrainFamily(first) != isProtectedTerrainFamily(second)) {
            return isProtectedTerrainFamily(first) ? second : first;
        }
        return first.compareToIgnoreCase(second) < 0 ? first : second;
    }

    private String normalizedSmoothingFamily(String terrainName) {
        String family = extractBaseName(terrainName);
        return family == null || family.isBlank() ? null : family.trim();
    }

    private int smoothingPriority(String templateName) {
        return "Tmpl3".equalsIgnoreCase(templateName) ? 1 : 0;
    }

    private void putSmoothingTarget(Map<String, LinkedHashMap<Long, int[]>> targets, String templateName, int x, int y) {
        targets.get(templateName).put(tmpl3TileKey(x, y), new int[] { x, y });
    }

    private void applyTmplRegenerationChangesBatched(List<Tmpl3RegenerationChange> changes, String templateName) {
        if (changes.isEmpty()) {
            return;
        }
        UndoEntry undo = newUndoEntry("Regenerate " + templateName, false);
        for (int start = 0; start < changes.size(); start += TMPL3_APPLY_BATCH_SIZE) {
            int batchStart = start;
            int batchEnd = Math.min(changes.size(), start + TMPL3_APPLY_BATCH_SIZE);
            runOnGlThreadAndWait(() -> {
                for (int i = batchStart; i < batchEnd; i++) {
                    Tmpl3RegenerationChange change = changes.get(i);
                    Tmpl3TileSnapshot tile = change.tile;
                    String newName = buildTmplRawName(tile, change.newName, templateName);
                    if (tile.decorTile) {
                        applyGroundLayerChange(tile.x, tile.y, newName, undo);
                    } else {
                        applyTileChangeWithOffsetAndOrder(tile.x, tile.y, newName, tile.oldScaleX, tile.oldScaleY,
                                0f, 0f, tile.oldZOrder, undo);
                    }
                }
            });
        }
        runOnGlThreadAndWait(() -> {
            pushUndoEntry(undo);
            invalidateGroundAfterBulkChange(templateName);
        });
    }

    private void rebuildAllTmpl(String templateName, boolean viewportOnly) {
        if (tmpl3Regenerating) {
            showEditorMessage("TMPL regeneration already running");
            return;
        }
        if (mapReader == null || mapRenderer == null) {
            showEditorMessage("Map not loaded");
            return;
        }
        int[] bounds = viewportOnly ? calculateVisibleBounds(0) : null;
        activeTmplRegenerationName = templateName;
        tmpl3Regenerating = true;
        tmpl3RegenerationProgress = 0f;
        tmpl3RegenerationStage = "Rebuild: resetting " + templateName;
        showEditorMessage("Rebuilding " + templateName + (viewportOnly ? " (viewport)" : "")
                + ": resetting " + templateName + "...");
        log.info("Rebuild all {} started: viewportOnly={}", templateName, viewportOnly);

        tmpl3RegenerationExecutor.submit(() -> {
            try {
                // Phase 1: reset existing transitions of the requested family in place.
                List<int[]> transitions = collectTmplTransitionTiles(bounds, templateName);
                applyTransitionEraseBatched(transitions, templateName);

                // Phase 2: trace new borders where terrain families touch.
                tmpl3RegenerationStage = "Rebuild: tracing borders";
                List<int[]> borderTiles = collectNewTmplBorderTiles(bounds, templateName);
                applyBorderConversionBatched(borderTiles, templateName);

                // Phase 3: regenerate masks on the resulting transition tiles.
                tmpl3RegenerationStage = "Rebuild: regenerating masks";
                List<Tmpl3TileSnapshot> tiles = collectTmplTilesWithProgress(bounds, templateName);
                if (tiles.isEmpty()) {
                    Gdx.app.postRunnable(() -> {
                        invalidateGroundAfterBulkChange(templateName);
                        tmpl3Regenerating = false;
                        tmpl3RegenerationProgress = 1f;
                        tmpl3RegenerationStage = "Done";
                        showEditorMessage("Rebuilt " + templateName + ": reset " + transitions.size()
                                + ", traced " + borderTiles.size() + " (no masks)");
                    });
                    return;
                }
                List<Tmpl3RegenerationChange> changes = buildTmplRegenerationChangesWithProgress(tiles, templateName);
                Gdx.app.postRunnable(() -> beginTmplRegenerationApply(tiles.size(), changes, templateName));
            } catch (Throwable t) {
                log.error("Rebuild all {} failed at {}% ({})", templateName,
                        Math.round(tmpl3RegenerationProgress * 100f), tmpl3RegenerationStage, t);
                Gdx.app.postRunnable(() -> {
                    tmpl3Regenerating = false;
                    tmpl3RegenerationStage = "Failed";
                    showEditorMessage("Error: Failed to rebuild " + templateName);
                });
            }
        });
    }

    /** Post a unit of work to the GL thread and block the caller until it finishes. */
    private void runOnGlThreadAndWait(Runnable work) {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Gdx.app.postRunnable(() -> {
            try {
                work.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for GL-thread work", e);
        }
    }

    /** Collect every tile of the requested transition family within the bounds. */
    private List<int[]> collectTmplTransitionTiles(int[] bounds, String templateName) {
        List<int[]> result = new ArrayList<>();
        int startX = bounds != null ? bounds[0] : 0;
        int endX = bounds != null ? bounds[1] : mapReader.getWidth() - 1;
        int startY = bounds != null ? bounds[2] : 0;
        int endY = bounds != null ? bounds[3] : mapReader.getHeight() - 1;
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                // Match Tmpl3 on the GROUND layer so transitions hidden under a decor
                // are reset and regenerated too (see collectTmpl3TilesWithProgress).
                ResolvedSprite groundResolved = SpriteNameParser.parse(
                        mapReader.getGroundSpriteName(x, y),
                        mapRenderer != null ? mapRenderer.getMetaByName() : null);
                if (isResolvedTmpl(groundResolved, templateName)) {
                    result.add(new int[] { x, y });
                }
            }
        }
        return result;
    }

    /**
     * Phase 1, batched. Normalises every transition tile to its provisional variant 1
     * in place so phase 3 can regenerate masks from a clean slate. The transition
     * stays exactly where it was; nothing is replaced with concrete terrain.
     * Writes are applied to the map in GL-thread batches of {@link #TMPL3_APPLY_BATCH_SIZE} so
     * the UI stays responsive and progress advances.
     */
    private void applyTransitionEraseBatched(List<int[]> transitions, String templateName) {
        // Precompute (x, y, newName) off the GL thread.
        List<String[]> ops = new ArrayList<>(transitions.size());
        int total = Math.max(1, transitions.size());
        for (int i = 0; i < transitions.size(); i++) {
            int x = transitions.get(i)[0];
            int y = transitions.get(i)[1];
            String newName = templateName + " 1";
            ops.add(new String[] { Integer.toString(x), Integer.toString(y), newName });
            tmpl3RegenerationProgress = ((i + 1) / (float) total) * 0.30f;
        }
        applyTileWritesBatched(ops, "Reset " + templateName, 0.30f, 0.45f, templateName);
    }

    /** Phase 2, batched. Converts the border row to provisional variant 1 tiles. */
    private void applyBorderConversionBatched(List<int[]> borderTiles, String templateName) {
        List<String[]> ops = new ArrayList<>(borderTiles.size());
        for (int[] pos : borderTiles) {
            int x = pos[0];
            int y = pos[1];
            String newName = templateName + " 1";
            ops.add(new String[] { Integer.toString(x), Integer.toString(y), newName });
        }
        applyTileWritesBatched(ops, "Trace " + templateName + " border", 0.45f, 0.55f, templateName);
    }

    /**
     * Apply a list of {x, y, newName} tile writes to the map in GL-thread batches,
     * grouped under a single undo entry, blocking the calling (regeneration) thread
     * between batches. progressFrom/progressTo bound the advertised progress range.
     */
    private void applyTileWritesBatched(List<String[]> ops, String undoLabel,
            float progressFrom, float progressTo, String templateName) {
        if (ops.isEmpty()) {
            tmpl3RegenerationProgress = progressTo;
            return;
        }
        UndoEntry undo = newUndoEntry(undoLabel, false);
        int total = ops.size();
        for (int start = 0; start < total; start += TMPL3_APPLY_BATCH_SIZE) {
            int end = Math.min(total, start + TMPL3_APPLY_BATCH_SIZE);
            int batchStart = start;
            int batchEnd = end;
            runOnGlThreadAndWait(() -> {
                for (int i = batchStart; i < batchEnd; i++) {
                    String[] op = ops.get(i);
                    int x = Integer.parseInt(op[0]);
                    int y = Integer.parseInt(op[1]);
                    if (isDecorTile(x, y)) {
                        applyGroundLayerChange(x, y, op[2], undo);
                    } else {
                        applyTileChangeWithOffsetAndOrder(x, y, op[2], mapReader.getScaleX(x, y),
                                mapReader.getScaleY(x, y), mapReader.getOffsetX(x, y), mapReader.getOffsetY(x, y),
                                mapReader.getZOrder(x, y), undo);
                    }
                }
            });
            tmpl3RegenerationProgress = progressFrom
                    + (end / (float) total) * (progressTo - progressFrom);
        }
        runOnGlThreadAndWait(() -> {
            if (undo.hasChanges()) {
                pushUndoEntry(undo);
            }
            invalidateGroundAfterBulkChange(templateName);
        });
    }

    /**
     * Scan the given bounds and return the positions of tiles to convert into a new
     * Tmpl3 border row. A concrete terrain tile is selected when it is directly
     * adjacent (4-neighbour) to a concrete terrain of a different family AND its own
     * family is the "receding" side of that boundary, chosen automatically as the
     * family whose base name sorts first (case-insensitive). This guarantees exactly
     * one of the two terrains recedes along each boundary, without any selection.
     */
    private List<int[]> collectNewTmplBorderTiles(int[] bounds, String templateName) {
        List<int[]> result = new ArrayList<>();
        int startX = bounds != null ? bounds[0] : 0;
        int endX = bounds != null ? bounds[1] : mapReader.getWidth() - 1;
        int startY = bounds != null ? bounds[2] : 0;
        int endY = bounds != null ? bounds[3] : mapReader.getHeight() - 1;
        int rows = Math.max(1, endY - startY + 1);
        int cols = Math.max(1, endX - startX + 1);
        int total = Math.max(1, rows * cols);
        int[][] cardinals = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                // Skip tiles that already carry a smoothing transition of any family
                // (including those hidden under a decor). A tile already resolved by
                // Tmpl3/Tmpl4 must not be overwritten by a Tmpl1 rebuild (or vice versa):
                // collectNewTmplBorderTiles has no notion of which family a given terrain
                // pair should use, so without this guard every rebuild re-claims borders
                // another family already legitimately owns.
                ResolvedSprite groundResolved = SpriteNameParser.parse(
                        mapReader.getGroundSpriteName(x, y),
                        mapRenderer != null ? mapRenderer.getMetaByName() : null);
                if (isAnySmoothingTemplateName(groundResolved != null ? groundResolved.name : null)) {
                    continue;
                }
                String terrain = resolveConcreteGroundTerrainName(x, y);
                if (terrain == null) {
                    continue;
                }
                String family = extractBaseName(terrain);
                if (family == null || family.isBlank()) {
                    continue;
                }
                // Protected families (roads, etc.) never recede: they are kept intact
                // and the adjacent terrain always carries the transition instead.
                if (isProtectedTerrainFamily(family)) {
                    continue;
                }
                boolean recede = false;
                for (int[] d : cardinals) {
                    int nx = x + d[0];
                    int ny = y + d[1];
                    if (!isTileInMap(nx, ny)) {
                        continue;
                    }
                    // Check the GROUND layer: a decor (e.g. a brick wall) can sit on top
                    // of a Tmpl tile, and resolveSpriteAt would return the decor name,
                    // letting the borrowed terrain leak across the existing transition.
                    ResolvedSprite nGround = SpriteNameParser.parse(
                            mapReader.getGroundSpriteName(nx, ny),
                            mapRenderer != null ? mapRenderer.getMetaByName() : null);
                    if (isAnySmoothingTemplateName(nGround != null ? nGround.name : null)) {
                        continue;
                    }
                    String nTerrain = resolveConcreteGroundTerrainName(nx, ny);
                    if (nTerrain == null) {
                        continue;
                    }
                    String nFamily = extractBaseName(nTerrain);
                    if (nFamily == null || sameTerrainFamily(nFamily, family)) {
                        // Same visual terrain (incl. decorative sub-variants): no border.
                        continue;
                    }
                    // Only claim this boundary for the family the real 1.25 map actually
                    // uses to smooth this exact terrain pair (see TERRAIN_PAIR_TMPL_FAMILY).
                    // Without this, e.g. a grass/RockFloor edge -- which the game always
                    // smooths with Tmpl4 -- would be claimed by a Tmpl1 rebuild simply
                    // because it is an unresolved boundary with no existing transition.
                    String verifiedFamily = verifiedTmplFamilyForPair(family, nFamily);
                    if (!templateName.equalsIgnoreCase(verifiedFamily)) {
                        continue;
                    }
                    // Against a protected neighbour (e.g. a road), this tile always
                    // recedes so the protected family keeps its exact footprint.
                    if (isProtectedTerrainFamily(nFamily)) {
                        recede = true;
                        break;
                    }
                    // Otherwise the family that sorts first recedes.
                    if (family.compareToIgnoreCase(nFamily) < 0) {
                        recede = true;
                        break;
                    }
                }
                if (recede) {
                    result.add(new int[] { x, y });
                }
            }
            int processed = Math.min(total, (y - startY + 1) * cols);
            tmpl3RegenerationProgress = (processed / (float) total) * 0.15f;
        }
        return result;
    }

    /**
     * Widened search (used only when a transition tile has no concrete ground in its
     * immediate 8-neighbourhood) to pick the majority terrain to replace it with.
     * Returns null when no concrete ground is found within the radius.
     */
    private String pickNearbyReplacementTerrain(int x, int y, int radius) {
        Map<String, Integer> terrainCounts = new HashMap<>();
        Map<String, Integer> protectedCounts = new HashMap<>();
        for (int ny = y - radius; ny <= y + radius; ny++) {
            for (int nx = x - radius; nx <= x + radius; nx++) {
                if ((nx == x && ny == y) || !isTileInMap(nx, ny)) {
                    continue;
                }
                String terrain = resolveConcreteGroundTerrainName(nx, ny);
                if (terrain == null) {
                    continue;
                }
                // Keep protected terrains (roads, etc.) out of the main pool so an
                // erased transition is never backfilled with road, which would grow it.
                if (isProtectedTerrainFamily(extractBaseName(terrain))) {
                    protectedCounts.merge(terrain, 1, Integer::sum);
                } else {
                    terrainCounts.merge(terrain, 1, Integer::sum);
                }
            }
        }
        Map<String, Integer> pool = terrainCounts.isEmpty() ? protectedCounts : terrainCounts;
        return pool.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** Radius used to infer terrain hidden by a neutralized smoothing tile. */
    private static final int SMOOTHING_REPLACEMENT_RADIUS = 3;
    /**
     * Families that must never be converted into a transition tile nor receive an
     * encroaching transition: along a boundary with one of these, the OTHER terrain
     * always recedes, so the protected family keeps its exact footprint across
     * repeated rebuilds. Matched case-insensitively as a prefix of the base family.
     */
    private static final String[] PROTECTED_TERRAIN_FAMILY_PREFIXES = {
        "town road",
        "road",
        "path",
    };

    private boolean isProtectedTerrainFamily(String family) {
        if (family == null) {
            return false;
        }
        String f = family.toLowerCase(Locale.ROOT);
        for (String prefix : PROTECTED_TERRAIN_FAMILY_PREFIXES) {
            if (f.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Which Tmpl family smooths a given pair of concrete terrain families, as
     * observed on the deployed 1.25 worldmap (every pair below is >=100% dominant
     * by one family across every occurrence with a meaningful sample size --
     * see the reverse-engineering session that produced this table). A pair
     * absent from this map has no verified precedent and is never claimed by
     * any Rebuild Tmpl menu; only "Rebuild Tmpl3" falls back to its own
     * per-tile classifier ({@link #smoothingBoundaryAround}) for those.
     */
    private static final Map<String, String> TERRAIN_PAIR_TMPL_FAMILY = Map.ofEntries(
            Map.entry("64knormalgrass|cavernfloor", "Tmpl1"),
            Map.entry("64knormalgrass|dgrass", "Tmpl1"),
            Map.entry("64knormalgrass|earthtile", "Tmpl1"),
            Map.entry("64knormalgrass|grass", "Tmpl1"),
            Map.entry("64knormalgrass|hardrock", "Tmpl1"),
            Map.entry("deserttile|hardrock", "Tmpl1"),
            Map.entry("dgrass|earthtile", "Tmpl1"),
            Map.entry("dgrass|grass", "Tmpl1"),
            Map.entry("dgrass|hardrock", "Tmpl1"),
            Map.entry("earthtile|grass", "Tmpl1"),
            Map.entry("earthtile|hardrock", "Tmpl1"),
            Map.entry("grass|hardrock", "Tmpl1"),
            Map.entry("64knormalgrass|deserttile", "Tmpl3"),
            Map.entry("64knormalgrass|floor: wooden", "Tmpl3"),
            Map.entry("64knormalgrass|floor: wooden separation", "Tmpl3"),
            Map.entry("64knormalgrass|ground_water", "Tmpl3"),
            Map.entry("cavernfloor|ground_water", "Tmpl3"),
            Map.entry("deserttile|earthtile", "Tmpl3"),
            Map.entry("deserttile|floor: wooden", "Tmpl3"),
            Map.entry("deserttile|grass", "Tmpl3"),
            Map.entry("deserttile|ground_water", "Tmpl3"),
            Map.entry("earthtile|ground_water", "Tmpl3"),
            Map.entry("floor: wooden|floor: wooden separation", "Tmpl3"),
            Map.entry("floor: wooden|grass", "Tmpl3"),
            Map.entry("floor: wooden separation|grass", "Tmpl3"),
            Map.entry("grass|ground_water", "Tmpl3"),
            Map.entry("ground_water|hardrock", "Tmpl3"),
            Map.entry("64knormalgrass|rockfloor", "Tmpl4"),
            Map.entry("64knormalgrass|town road dale", "Tmpl4"),
            Map.entry("hardrock|town road dale", "Tmpl4"));

    /**
     * The Tmpl family the observed worldmap uses to smooth this terrain pair, or
     * null when the pair has no verified precedent (never claimed by a rebuild).
     */
    private String verifiedTmplFamilyForPair(String familyA, String familyB) {
        if (familyA == null || familyB == null) {
            return null;
        }
        String a = familyA.toLowerCase(Locale.ROOT);
        String b = familyB.toLowerCase(Locale.ROOT);
        String key = a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
        return TERRAIN_PAIR_TMPL_FAMILY.get(key);
    }

    /**
     * True when two names describe the same visual terrain, including decorative
     * sub-variants such as "Floor: Wooden Separation".
     */
    private boolean sameTerrainFamily(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        String la = a.toLowerCase(Locale.ROOT);
        String lb = b.toLowerCase(Locale.ROOT);
        if (la.equals(lb)) {
            return true;
        }
        String shorter = la.length() <= lb.length() ? la : lb;
        String longer = la.length() <= lb.length() ? lb : la;
        return longer.startsWith(shorter + " ") || longer.startsWith(shorter + ":");
    }

    private String tmpl3TerrainFamilyName(String terrainName) {
        String family = extractBaseName(terrainName);
        if (family == null || family.isBlank()) {
            return terrainName;
        }
        String lower = family.toLowerCase(Locale.ROOT);
        if (lower.endsWith(" separation")) {
            return family.substring(0, family.length() - " separation".length()).trim();
        }
        return family;
    }

    private List<Tmpl3TileSnapshot> collectTmplTilesWithProgress(int[] bounds, String templateName) {
        Map<Long, Tmpl3TileSnapshot> tiles = new LinkedHashMap<>();
        int startX = bounds != null ? bounds[0] : 0;
        int endX = bounds != null ? bounds[1] : mapReader.getWidth() - 1;
        int startY = bounds != null ? bounds[2] : 0;
        int endY = bounds != null ? bounds[3] : mapReader.getHeight() - 1;
        int rows = Math.max(1, endY - startY + 1);
        int cols = Math.max(1, endX - startX + 1);
        int total = Math.max(1, rows * cols);
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                // Detect Tmpl3 on the GROUND layer, even when a decor sits on top: a
                // Tmpl3 transition can be hidden under a decor (e.g. cemetery gates),
                // and resolveSpriteAt would return the decor and skip it, leaving it
                // stuck at its reset value ("Tmpl3 1").
                ResolvedSprite groundResolved = SpriteNameParser.parse(
                        mapReader.getGroundSpriteName(x, y),
                        mapRenderer != null ? mapRenderer.getMetaByName() : null);
                boolean existingTmpl = isResolvedTmpl(groundResolved, templateName);
                if (existingTmpl) {
                    String rawName = mapReader.getGroundSpriteName(x, y);
                    boolean decorTile = isDecorTile(x, y);
                    tiles.put(tmpl3TileKey(x, y), new Tmpl3TileSnapshot(x, y, rawName, mapReader.getScaleX(x, y),
                            mapReader.getScaleY(x, y), mapReader.getOffsetX(x, y), mapReader.getOffsetY(x, y),
                            mapReader.getZOrder(x, y), decorTile));
                }
            }
            int processed = Math.min(total, (y - startY + 1) * cols);
            float phaseProgress = processed / (float) total;
            tmpl3RegenerationProgress = phaseProgress * 0.20f;
        }
        return new ArrayList<>(tiles.values());
    }

    private List<Tmpl3RegenerationChange> buildTmplRegenerationChangesWithProgress(List<Tmpl3TileSnapshot> tiles,
            String templateName) {
        tmpl3RegenerationStage = "Loading " + templateName + " masks";
        tmpl3RegenerationProgress = 0.22f;
        try {
            List<Tmpl3Mask> masks = new Tmpl3MaskLoader(spriteLoader, templateName).load();
            if (masks.isEmpty()) {
                return tiles.stream()
                        .map(tile -> new Tmpl3RegenerationChange(tile, fallbackTmplName(tile)))
                        .toList();
            }

            Tmpl3Regenerator regenerator = new Tmpl3Regenerator(masks, new TerrainResolver() {
                @Override
                public String resolveTerrainName(int x, int y) {
                    // Only report a terrain when the tile itself carries concrete (non-Tmpl)
                    // ground. A neighbouring Tmpl tile must read as "unknown" rather than
                    // borrowing terrain from across the boundary: borrowing can assign the
                    // wrong side to a direction and corrupt the expected-mask shape, which
                    // makes the matcher pick the wrong Tmpl3 variant.
                    return resolveDirectConcreteGroundTerrainName(x, y);
                }

                @Override
                public String familyName(String terrainName) {
                    return tmpl3TerrainFamilyName(terrainName);
                }

                @Override
                public String extrapolateTerrainName(String terrainName, int targetX, int targetY) {
                    return extrapolateTmpl3TerrainName(terrainName, targetX, targetY);
                }
            }, templateName);

            List<com.perso.T4C.tmpl3.Tmpl3TileSnapshot> positions = tiles.stream()
                    .map(tile -> new com.perso.T4C.tmpl3.Tmpl3TileSnapshot(tile.x, tile.y))
                    .toList();
            tmpl3RegenerationStage = "Computing " + templateName + " masks";
            tmpl3RegenerationProgress = 0.30f;
            // Map the regenerator's internal 0..1 progress onto our 0.30..0.95 band
            // so the bar advances during the long whole-map computation.
            regenerator.setProgressCallback(fraction ->
                    tmpl3RegenerationProgress = 0.30f + (float) fraction * 0.65f);
            Tmpl3RegenerationResult result = regenerator.regenerateTmpl3(mapReader, positions);
            Map<Long, String> regeneratedNames = new HashMap<>();
            for (Tmpl3Change change : result.changes()) {
                regeneratedNames.put(tmpl3TileKey(change.x(), change.y()), change.newName());
            }

            // Fast name-mapping pass (the heavy work already reported progress via
            // the regenerator callback above). Hold the bar at 0.95 for the GL-thread
            // apply phase that follows.
            List<Tmpl3RegenerationChange> changes = new ArrayList<>(tiles.size());
            for (Tmpl3TileSnapshot tile : tiles) {
                String newName = regeneratedNames.getOrDefault(tmpl3TileKey(tile.x, tile.y),
                        fallbackTmplName(tile));
                changes.add(new Tmpl3RegenerationChange(tile, newName));
            }
            tmpl3RegenerationProgress = 0.95f;
            return changes;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build " + templateName + " regeneration changes", e);
        }
    }

    private static long tmpl3TileKey(int x, int y) {
        return ((long) x << 32) ^ (y & 0xFFFFFFFFL);
    }

    private static String stripTmplOverrideName(String name) {
        if (name == null) {
            return null;
        }
        int openIdx = name.indexOf('[');
        int closeIdx = openIdx >= 0 ? name.indexOf(']', openIdx + 1) : -1;
        if (openIdx >= 0 && closeIdx > openIdx && name.substring(openIdx, closeIdx).contains("|")) {
            return name.substring(0, openIdx).trim();
        }
        return name;
    }

    private String fallbackTmplName(Tmpl3TileSnapshot tile) {
        if (tile == null) {
            return null;
        }
        return stripTmplOverrideName(tile.oldName);
    }

    private String buildTmplRawName(Tmpl3TileSnapshot tile, String tmplName, String templateName) {
        if (tile == null) {
            return tmplName;
        }
        if (tmplName == null || !startsWithIgnoreCase(tmplName, templateName)) {
            return tile.oldName;
        }
        return tmplName;
    }

    private boolean isResolvedTmpl3(ResolvedSprite resolved) {
        return isResolvedTmpl(resolved, "Tmpl3");
    }

    private boolean isResolvedTmpl(ResolvedSprite resolved, String templateName) {
        if (resolved == null) {
            return false;
        }
        if (resolved.name != null && startsWithIgnoreCase(resolved.name, templateName)) {
            return true;
        }
        return false;
    }

    private void beginTmpl3RegenerationApply(int scannedCount, List<Tmpl3RegenerationChange> changes) {
        beginTmplRegenerationApply(scannedCount, changes, "Tmpl3");
    }

    private void beginTmplRegenerationApply(int scannedCount, List<Tmpl3RegenerationChange> changes,
            String templateName) {
        activeTmplRegenerationName = templateName;
        tmpl3RegenerationProgress = 0.95f;
        tmpl3RegenerationStage = "Applying changes";
        pendingTmpl3RegenerationChanges = changes;
        pendingTmpl3RegenerationUndo = newUndoEntry("Regenerate " + templateName, false);
        pendingTmpl3RegenerationIndex = 0;
        pendingTmpl3RegenerationScannedCount = scannedCount;
        pendingTmpl3RegenerationChangedCount = 0;
    }

    private void processPendingTmpl3RegenerationApply() {
        if (pendingTmpl3RegenerationChanges == null) {
            return;
        }
        int total = pendingTmpl3RegenerationChanges.size();
        int end = Math.min(total, pendingTmpl3RegenerationIndex + TMPL3_APPLY_BATCH_SIZE);
        for (int i = pendingTmpl3RegenerationIndex; i < end; i++) {
            Tmpl3RegenerationChange change = pendingTmpl3RegenerationChanges.get(i);
            Tmpl3TileSnapshot tile = change.tile;
            String newName = buildTmplRawName(tile, change.newName, activeTmplRegenerationName);
            if (!Objects.equals(tile.oldName, newName)) {
                pendingTmpl3RegenerationChangedCount++;
            }
            if (!Objects.equals(tile.oldName, newName)
                    || Float.compare(tile.oldOffsetX, 0f) != 0
                    || Float.compare(tile.oldOffsetY, 0f) != 0) {
                // When a decor sits on top, the Tmpl3 lives in the GROUND layer: record the
                // change as ground-under-decor so undo restores the ground without disturbing
                // the decor.
                pendingTmpl3RegenerationUndo.changes.add(new TileChange(tile.x, tile.y, tile.oldName, tile.oldScaleX,
                        tile.oldScaleY, tile.oldOffsetX, tile.oldOffsetY, tile.oldZOrder, newName, tile.oldScaleX,
                        tile.oldScaleY, 0f, 0f, tile.oldZOrder, false, tile.decorTile));
            }
            if (tile.decorTile) {
                // Write only the ground layer; leave the decor sprite untouched.
                mapReader.setGroundSpriteName(tile.x, tile.y, newName);
            } else {
                mapReader.setSpriteNameFast(tile.x, tile.y, newName);
                mapReader.setScale(tile.x, tile.y, tile.oldScaleX, tile.oldScaleY);
                mapReader.setOffset(tile.x, tile.y, 0f, 0f);
                mapReader.setZOrder(tile.x, tile.y, tile.oldZOrder);
            }
        }
        pendingTmpl3RegenerationIndex = end;

        float applyProgress = total == 0 ? 1f : pendingTmpl3RegenerationIndex / (float) total;
        tmpl3RegenerationProgress = 0.95f + applyProgress * 0.05f;

        if (pendingTmpl3RegenerationIndex < total) {
            return;
        }

        pushUndoEntry(pendingTmpl3RegenerationUndo);
        mapDirty = true;
        minimapDirty = true;
        invalidateGroundAfterBulkChange(activeTmplRegenerationName);
        tmpl3RegenerationProgress = 1f;
        tmpl3RegenerationStage = "Done";
        tmpl3Regenerating = false;
        showEditorMessage("Regenerated " + pendingTmpl3RegenerationChangedCount + " "
                + activeTmplRegenerationName + " tiles");
        log.info("{} regeneration complete: regenerated {} tiles ({} scanned)", activeTmplRegenerationName,
                pendingTmpl3RegenerationChangedCount, pendingTmpl3RegenerationScannedCount);

        pendingTmpl3RegenerationChanges = null;
        pendingTmpl3RegenerationUndo = null;
        pendingTmpl3RegenerationIndex = 0;
        pendingTmpl3RegenerationScannedCount = 0;
        pendingTmpl3RegenerationChangedCount = 0;
    }

    private void forceRegenerateTmpl3AtCurrentTile() {
        int x = hoveredTileX;
        int y = hoveredTileY;
        if (x < 0 || y < 0 || x >= mapReader.getWidth() || y >= mapReader.getHeight()) {
            showEditorMessage("No tile hovered");
            return;
        }

        if (tmpl3Regenerating) {
            showEditorMessage("Tmpl3 regeneration already in progress");
            return;
        }

        log.info("Force regenerating Tmpl3 at ({}, {})", x, y);
        tmpl3Regenerating = true;
        tmpl3RegenerationProgress = 0.5f;
        tmpl3RegenerationStage = "Force regenerating single tile";
        showEditorMessage("Force regenerating Tmpl3 at (" + x + ", " + y + ")");

        tmpl3RegenerationExecutor.submit(() -> {
            try {
                List<Tmpl3Mask> masks = new Tmpl3MaskLoader(spriteLoader).load();
                if (masks.isEmpty()) {
                    Gdx.app.postRunnable(() -> {
                        tmpl3Regenerating = false;
                        showEditorMessage("No Tmpl3 masks found");
                    });
                    return;
                }

                Tmpl3Regenerator regenerator = new Tmpl3Regenerator(masks, new TerrainResolver() {
                    @Override
                    public String resolveTerrainName(int tx, int ty) {
                        // See rebuildAllTmpl3: never borrow terrain through neighbouring Tmpl
                        // tiles, or the expected mask (and thus the chosen variant) is wrong.
                        return resolveDirectConcreteGroundTerrainName(tx, ty);
                    }

                    @Override
                    public String familyName(String terrainName) {
                        return tmpl3TerrainFamilyName(terrainName);
                    }

                    @Override
                    public String extrapolateTerrainName(String terrainName, int targetX, int targetY) {
                        return extrapolateTmpl3TerrainName(terrainName, targetX, targetY);
                    }
                });

                String rawName = mapReader.getSpriteName(x, y);
                boolean decorTile = isDecorTile(x, y);
                List<com.perso.T4C.tmpl3.Tmpl3TileSnapshot> singleTile = List.of(
                        new com.perso.T4C.tmpl3.Tmpl3TileSnapshot(x, y));

                Tmpl3RegenerationResult result = regenerator.regenerateTmpl3(mapReader, singleTile);

                log.info("Force regen result: {} changes from {} scanned", result.changes().size(), result.scannedCount());
                for (Tmpl3Change change : result.changes()) {
                    log.info("  Change: ({}, {}) -> {}", change.x(), change.y(), change.newName());
                }

                List<Tmpl3RegenerationChange> changes = result.changes().stream()
                        .map(change -> new Tmpl3RegenerationChange(
                                new Tmpl3TileSnapshot(change.x(), change.y(), rawName,
                                        mapReader.getScaleX(change.x(), change.y()),
                                        mapReader.getScaleY(change.x(), change.y()),
                                        mapReader.getOffsetX(change.x(), change.y()),
                                        mapReader.getOffsetY(change.x(), change.y()),
                                        mapReader.getZOrder(change.x(), change.y()), decorTile),
                                change.newName()))
                        .toList();

                if (changes.isEmpty()) {
                    Gdx.app.postRunnable(() -> {
                        tmpl3Regenerating = false;
                        showEditorMessage("No Tmpl3 generated at (" + x + ", " + y + ") - invalid context?");
                    });
                } else {
                    Gdx.app.postRunnable(() -> beginTmpl3RegenerationApply(1, changes));
                }
            } catch (Throwable t) {
                log.error("Force regeneration failed", t);
                Gdx.app.postRunnable(() -> {
                    tmpl3Regenerating = false;
                    showEditorMessage("Error: Force regeneration failed");
                });
            }
        });
    }


    private List<Tmpl3TileSnapshot> findMissingTmpl3TransitionsWithProgress() {
        Map<Long, Tmpl3TileSnapshot> missingTiles = new LinkedHashMap<>();
        int width = mapReader.getWidth();
        int height = mapReader.getHeight();
        int total = Math.max(1, width * height);

        try {
            List<Tmpl3Mask> masks = new Tmpl3MaskLoader(spriteLoader).load();
            if (masks.isEmpty()) {
                return new ArrayList<>();
            }

            TerrainResolver resolver = new TerrainResolver() {
                @Override
                public String resolveTerrainName(int tx, int ty) {
                    // A missing transition must be justified by concrete terrain
                    // touching the candidate tile itself. Looking through an existing
                    // Tmpl border grows that border one tile into the neighbouring
                    // terrain on every rebuild (for example into an indoor wooden
                    // floor), because it discovers the terrain on the far side too.
                    return resolveDirectConcreteGroundTerrainName(tx, ty);
                }

                @Override
                public String familyName(String terrainName) {
                    return tmpl3TerrainFamilyName(terrainName);
                }

                @Override
                public String extrapolateTerrainName(String terrainName, int targetX, int targetY) {
                    return extrapolateTmpl3TerrainName(terrainName, targetX, targetY);
                }
            };

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    ResolvedSprite resolved = resolveSpriteAt(x, y);
                    boolean isTmpl3 = isResolvedTmpl3(resolved);

                    if (!isTmpl3) {
                        TerrainContext context = TerrainContext.analyze(x, y, width, height, resolver);
                        if (context.isValid()) {
                            String rawName = mapReader.getSpriteName(x, y);
                            boolean decorTile = isDecorTile(x, y);
                            missingTiles.put(tmpl3TileKey(x, y), new Tmpl3TileSnapshot(x, y, rawName,
                                    mapReader.getScaleX(x, y), mapReader.getScaleY(x, y),
                                    mapReader.getOffsetX(x, y), mapReader.getOffsetY(x, y),
                                    mapReader.getZOrder(x, y), decorTile));
                        }
                    }
                }
                int processed = Math.min(total, (y + 1) * width);
                float phaseProgress = processed / (float) total;
                tmpl3RegenerationProgress = phaseProgress * 0.20f;
            }
            return new ArrayList<>(missingTiles.values());
        } catch (Exception e) {
            log.error("Failed to analyze missing Tmpl3 transitions", e);
            return new ArrayList<>();
        }
    }

    /**
     * Like {@link #resolveConcreteGroundTerrainName} but never borrows terrain through
     * neighbouring Tmpl transition tiles: it returns a terrain name only when THIS tile
     * itself carries a concrete (non-Tmpl) ground sprite. Used by the artifact detector
     * so a Tmpl3 surrounded by other Tmpl3 tiles (which would otherwise borrow terrain
     * from across walls) is correctly seen as having a single concrete family touching it.
     */
    private String resolveDirectConcreteGroundTerrainName(int x, int y) {
        String overlayTerrain = smoothingTerrainOverlay.get(tmpl3TileKey(x, y));
        if (overlayTerrain != null) {
            return overlayTerrain;
        }
        String groundName = mapReader.getGroundSpriteName(x, y);
        ResolvedSprite resolved = SpriteNameParser.parse(groundName,
                mapRenderer != null ? mapRenderer.getMetaByName() : null);
        if (resolved == null || resolved.name == null) {
            return null;
        }
        if (!startsWithIgnoreCase(resolved.name, "tmpl") && isGroundSprite(resolved.name)) {
            return resolved.name;
        }
        return null;
    }

    private String resolveConcreteGroundTerrainName(int x, int y) {
        String overlayTerrain = smoothingTerrainOverlay.get(tmpl3TileKey(x, y));
        if (overlayTerrain != null) {
            return overlayTerrain;
        }
        String groundName = mapReader.getGroundSpriteName(x, y);
        ResolvedSprite resolved = SpriteNameParser.parse(groundName,
                mapRenderer != null ? mapRenderer.getMetaByName() : null);
        if (resolved == null || resolved.name == null) {
            return null;
        }
        if (!startsWithIgnoreCase(resolved.name, "tmpl") && isGroundSprite(resolved.name)) {
            return resolved.name;
        }
        if (startsWithIgnoreCase(resolved.name, "tmpl") || isDecorTile(x, y)) {
            String borrowed = resolveNearbyConcreteGroundTerrainName(x, y);
            if (borrowed != null) {
                return borrowed;
            }
        }
        return null;
    }

    private String resolveNearbyConcreteGroundTerrainName(int tileX, int tileY) {
        int[][] directions = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
        for (int[] direction : directions) {
            String terrain = resolveConcreteGroundTerrainInDirection(tileX, tileY, direction[0], direction[1]);
            if (terrain != null) {
                return terrain;
            }
        }
        return null;
    }

    private String resolveConcreteGroundTerrainInDirection(int tileX, int tileY, int dx, int dy) {
        for (int step = 1; step <= 8; step++) {
            int x = tileX + dx * step;
            int y = tileY + dy * step;
            if (!isTileInMap(x, y)) {
                return null;
            }
            String overlayTerrain = smoothingTerrainOverlay.get(tmpl3TileKey(x, y));
            if (overlayTerrain != null) {
                return extrapolateTmpl3TerrainName(overlayTerrain, tileX, tileY);
            }
            String groundName = mapReader.getGroundSpriteName(x, y);
            ResolvedSprite resolved = SpriteNameParser.parse(groundName,
                    mapRenderer != null ? mapRenderer.getMetaByName() : null);
            if (resolved == null || resolved.name == null) {
                continue;
            }
            if (startsWithIgnoreCase(resolved.name, "tmpl")) {
                continue;
            }
            if (isGroundSprite(resolved.name)) {
                return extrapolateTmpl3TerrainName(resolved.name, tileX, tileY);
            }
        }
        return null;
    }

    private String extrapolateTmpl3TerrainName(String terrainName, int targetX, int targetY) {
        GridVariant variant = parseGridVariant(terrainName);
        if (variant == null) {
            return terrainName;
        }
        GroundFillPattern pattern = getGroundFillPattern(variant.base);
        if (pattern == null || !pattern.grid()) {
            return terrainName;
        }
        int col = wrapGridValue(targetX + 1, pattern.cols());
        int row = wrapGridValue(targetY + 1, pattern.rows());
        String predicted = String.format("%s (%d, %d)", variant.base, col, row);
        return isGroundSprite(predicted) ? predicted : terrainName;
    }

    /**
     * Recursive flood fill implementation
     */
    private int floodFillRecursive(int x, int y, String targetName, String replacementName, float scaleX, float scaleY,
            Set<Long> visited, UndoEntry undoEntry) {
        if (x < 0 || x >= mapReader.getWidth() || y < 0 || y >= mapReader.getHeight()) {
            return 0;
        }

        long key = ((long) x << 32) | (y & 0xFFFFFFFFL);
        if (visited.contains(key)) {
            return 0;
        }
        visited.add(key);

        String currentName = mapReader.getSpriteName(x, y);
        if (!Objects.equals(currentName, targetName)) {
            return 0;
        }

        applyTileChange(x, y, replacementName, scaleX, scaleY, undoEntry);

        int count = 1;
        count += floodFillRecursive(x + 1, y, targetName, replacementName, scaleX, scaleY, visited, undoEntry);
        count += floodFillRecursive(x - 1, y, targetName, replacementName, scaleX, scaleY, visited, undoEntry);
        count += floodFillRecursive(x, y + 1, targetName, replacementName, scaleX, scaleY, visited, undoEntry);
        count += floodFillRecursive(x, y - 1, targetName, replacementName, scaleX, scaleY, visited, undoEntry);

        return count;
    }

    private void setFillToolSprite(String spriteName) {
        if (spriteName == null || spriteName.isBlank()) {
            return;
        }
        fillToolSprite = spriteName.trim();
        fillToolEnabled = true;
        showEditorMessage("Fill: ON (" + fillToolSprite + ")");
        log.info("Fill tool sprite set to {}", fillToolSprite);
    }

    private void toggleAutofill() {
        autofillEnabled = !autofillEnabled;
        if (autofillEnabled) {
            if (groundFillSprites.isEmpty()) {
                buildGroundFillSprites();
            }
            if (!groundFillSprites.isEmpty()
                    && (fillToolSprite == null || !groundFillSprites.contains(fillToolSprite))) {
                fillToolSprite = groundFillSprites.get(0);
            }
            fillToolEnabled = true;
            groundSelectOpen = true;
        } else {
            fillToolEnabled = false;
            groundSelectOpen = false;
        }
        showInfoAndLog("Autofill: " + (autofillEnabled ? "ON" : "OFF"), "Autofill toggled: {}", autofillEnabled);
    }

    private void triggerSpriteHotReload() {
        if (spriteHotReloading) {
            return;
        }
        spriteHotReloading = true;
        loadingMessage = "Reloading sprites...";
        loadingProgress = 0f;
        loadingStep = 0;
        loadingStepCount = 4;
        loadingOverlayPresented = false;
        isLoading = true;
        preloadSprites = null;
        preloadSpriteIndex = 0;
        cachedSpriteList = null;
        cachedSpriteNames = null;
    }

    private void processSpriteHotReloadSteps() {
        try {
            switch (loadingStep) {
                case 0 -> {
                    loadingMessage = "Reloading sprite metadata...";
                    spriteLoader.loadSpriteBin(Paths.SPRITE_BIN);
                    advanceLoadingStep();
                }
                case 1 -> {
                    loadingMessage = "Refreshing map renderer...";
                    clearSpriteReloadState();
                    if (mapRenderer != null) {
                        mapRenderer.reload(modifSprites);
                        mapRenderer.setDecorVisible(decorVisible);
                        mapRenderer.setObjectsVisible(objectsVisible);
                        mapRenderer.setGroundOutlineEnabled(groundOutlineEnabled);
                        mapRenderer.setDecorUseTileOffsets(true);
                        mapRenderer.setDecorOffsetOverrides(decorOffsetOverrides);
                    }
                    advanceLoadingStep();
                }
                case 2 -> {
                    loadingMessage = "Indexing sprite picker...";
                    processSpritePreloadStep();
                }
                case 3 -> {
                    loadingMessage = "Refreshing sprite tools...";
                    buildGroundFillSprites();
                    lastSpritesBinModified = getSpritesBinLastModified();
                    showEditorMessage("Sprites reloaded");
                    log.info("Sprites.bin reloaded");
                    advanceLoadingStep();
                }
                default -> {
                    loadingMessage = "Ready";
                    loadingProgress = 1f;
                    spriteHotReloading = false;
                    isLoading = false;
                }
            }
        } catch (GameException e) {
            showEditorMessage("Error: Failed to reload sprites");
            log.error("Failed to reload sprites.bin", e);
            loadingMessage = e.getMessage();
            loadingProgress = 1f;
            spriteHotReloading = false;
            isLoading = false;
        }
    }

    private void clearSpriteReloadState() {
        tmplPaintRegenerators.clear();
        decorOffsetOverrides.clear();
        pendingOffsetSprite = null;
        pendingOffsetDirty = false;
        synchronized (offsetWriteLock) {
            pendingOffsetWrites.clear();
            if (offsetWriteFuture != null) {
                offsetWriteFuture.cancel(false);
                offsetWriteFuture = null;
            }
        }
    }

    private void checkSpritesBinUpdates() {
        // Auto-watch disabled
    }

    /** Most recent modification time among the sprite shards, -1 when there is none. */
    private long getSpritesBinLastModified() {
        try {
            long newest = -1L;
            for (java.nio.file.Path shard : SpriteBinIO.resolveShards(spriteBinDir(), Paths.SPRITE_BIN_BASE)) {
                newest = Math.max(newest, shard.toFile().lastModified());
            }
            return newest;
        } catch (Throwable ignored) {
            return -1L;
        }
    }

    private boolean isScaleModifierPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

    private boolean adjustScaleAtMouse(float amountY) {
        if (mapReader == null) {
            return false;
        }
        if (selectedTileX < 0 || selectedTileY < 0) {
            return false;
        }
        int tileX = selectedTileX;
        int tileY = selectedTileY;
        if (tileX < 0 || tileY < 0 || tileX >= mapReader.getWidth() || tileY >= mapReader.getHeight()) {
            return false;
        }
        String currentName = mapReader.getSpriteName(tileX, tileY);
        if (currentName == null || currentName.isBlank()) {
            return false;
        }

        float delta = amountY * SCALE_STEP;
        float currentScale = mapReader.getScaleX(tileX, tileY);
        float newScale = clampScale(currentScale + delta);
        float currentOffsetX = mapReader.getOffsetX(tileX, tileY);
        float currentOffsetY = mapReader.getOffsetY(tileX, tileY);
        int currentZOrder = mapReader.getZOrder(tileX, tileY);
        UndoEntry undoEntry = newUndoEntry("Scale", false);
        applyTileChangeWithOffsetAndOrder(tileX, tileY, currentName, newScale, newScale, currentOffsetX, currentOffsetY,
                currentZOrder, undoEntry);
        pushUndoEntry(undoEntry);
        showEditorMessage(String.format("Scale: %.2fx%.2f at (%d, %d)", newScale, newScale, tileX, tileY));
        return true;
    }

    private float clampScale(float value) {
        return Math.max(SCALE_MIN, Math.min(SCALE_MAX, value));
    }

    /**
     * Save map data to file
     */
    private void saveMapData() {
        if (!mapDirty) {
            return;
        }
        try {
            File mapFile = new File(currentMapPath);
            File tempFile = new File(currentMapPath + ".tmp");
            File decorFile = MapReader.decorFileFor(mapFile);
            File tempDecorFile = MapReader.decorFileFor(tempFile);

            if (mapFile.getParentFile() != null && !mapFile.getParentFile().exists()) {
                mapFile.getParentFile().mkdirs();
            }

            mapReader.writeCompact(tempFile);

            try {
                java.nio.file.Files.move(
                        tempFile.toPath(),
                        mapFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFail) {
                java.nio.file.Files.move(
                        tempFile.toPath(),
                        mapFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            if (tempDecorFile.exists()) {
                try {
                    java.nio.file.Files.move(
                            tempDecorFile.toPath(),
                            decorFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicFail) {
                    java.nio.file.Files.move(
                            tempDecorFile.toPath(),
                            decorFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }

            mapDirty = false;
            showEditorMessage("Map saved successfully");
            log.info("Saved map to: {}", currentMapPath);
        } catch (IOException e) {
            log.error("Failed to save map", e);
            showEditorMessage("Error: Failed to save map");
            try {
                java.nio.file.Files.deleteIfExists(new File(currentMapPath + ".tmp").toPath());
                java.nio.file.Files.deleteIfExists(MapReader.decorFileFor(new File(currentMapPath + ".tmp")).toPath());
            } catch (IOException ignored) {
            }
        }
    }

    private static void writeShortLE(DataOutputStream out, short value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >>> 8) & 0xFF);
    }

    private static short readShortLE(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        return (short) ((b2 << 8) | b1);
    }

    private static int readIntLE(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    private static void writeIntLE(DataOutputStream out, int value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >>> 8) & 0xFF);
        out.writeByte((value >>> 16) & 0xFF);
        out.writeByte((value >>> 24) & 0xFF);
    }

    private static void writeFloatLE(DataOutputStream out, float value) throws IOException {
        writeIntLE(out, Float.floatToIntBits(value));
    }

    /**
     * Render minimap with actual map content
     */
    private void renderMinimap() {
        if (groundSelectOpen) {
            return;
        }

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        int minimapX = screenWidth - MINIMAP_SIZE - MINIMAP_MARGIN;
        int minimapY = BOTTOM_INFO_HEIGHT + MINIMAP_MARGIN;
        com.badlogic.gdx.math.Rectangle mapBounds = minimapMapBounds(minimapX, minimapY);

        // Background
        shapeRenderer.setProjectionMatrix(uiBatch.getProjectionMatrix());
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.9f);
        shapeRenderer.rect(minimapX, minimapY, MINIMAP_SIZE, MINIMAP_SIZE);
        shapeRenderer.end();

        ensureMinimapTexture();
        if (minimapTexture != null) {
            uiBatch.begin();
            uiBatch.setColor(1f, 1f, 1f, 1f);
            uiBatch.draw(minimapTexture, mapBounds.x, mapBounds.y, mapBounds.width, mapBounds.height);
            uiBatch.end();
        }

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        shapeRenderer.rect(minimapX, minimapY, MINIMAP_SIZE, MINIMAP_SIZE);
        shapeRenderer.setColor(0.72f, 0.72f, 0.72f, 1f);
        shapeRenderer.rect(mapBounds.x, mapBounds.y, mapBounds.width, mapBounds.height);
        shapeRenderer.end();

        // Camera position indicator
        float scaleX = mapBounds.width / (float) (mapReader.getWidth() * GameConstants.GRID_W);
        float scaleY = mapBounds.height / (float) (mapReader.getHeight() * GameConstants.GRID_H);

        // House markers (yellow dots): one per detected wall cluster.
        if (!houseCenters.isEmpty()) {
            float minimapPerTileX = mapBounds.width / (float) mapReader.getWidth();
            float minimapPerTileY = mapBounds.height / (float) mapReader.getHeight();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 1f, 0f, 1f);
            for (float[] center : houseCenters) {
                float hx = mapBounds.x + (center[0] + 0.5f) * minimapPerTileX;
                // The minimap pixmap is drawn bottom-up, so flip Y like the camera dot.
                float hy = mapBounds.y + (mapReader.getHeight() - 1 - center[1] + 0.5f) * minimapPerTileY;
                shapeRenderer.circle(hx, hy, 2.5f);
            }
            shapeRenderer.end();
        }

        float mapHeightWorld = mapReader.getHeight() * GameConstants.GRID_H;
        float camMinimapX = mapBounds.x + cameraPosition.x * scaleX;
        float camMinimapY = mapBounds.y + (mapHeightWorld - cameraPosition.y) * scaleY;

        float viewWidth = (camera.viewportWidth * camera.zoom) * scaleX;
        float viewHeight = (camera.viewportHeight * camera.zoom) * scaleY;
        float rectWidth = Math.min(mapBounds.width, viewWidth);
        float rectHeight = Math.min(mapBounds.height, viewHeight);
        float rectX = clampFloat(camMinimapX - rectWidth / 2f, mapBounds.x, mapBounds.x + mapBounds.width - rectWidth);
        float rectY = clampFloat(camMinimapY - rectHeight / 2f, mapBounds.y, mapBounds.y + mapBounds.height - rectHeight);
        float dotX = clampFloat(camMinimapX, mapBounds.x, mapBounds.x + mapBounds.width);
        float dotY = clampFloat(camMinimapY, mapBounds.y, mapBounds.y + mapBounds.height);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 0, 1);
        shapeRenderer.rect(rectX, rectY, rectWidth, rectHeight);
        shapeRenderer.end();

        // Current position dot
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.circle(dotX, dotY, 3);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private com.badlogic.gdx.math.Rectangle minimapMapBounds(float minimapX, float minimapY) {
        if (mapReader == null || mapReader.getWidth() <= 0 || mapReader.getHeight() <= 0) {
            return new com.badlogic.gdx.math.Rectangle(minimapX, minimapY, MINIMAP_SIZE, MINIMAP_SIZE);
        }
        float mapAspect = mapReader.getWidth() / (float) mapReader.getHeight();
        float width = MINIMAP_SIZE;
        float height = MINIMAP_SIZE;
        if (mapAspect >= 1f) {
            height = width / mapAspect;
        } else {
            width = height * mapAspect;
        }
        float x = minimapX + (MINIMAP_SIZE - width) * 0.5f;
        float y = minimapY + (MINIMAP_SIZE - height) * 0.5f;
        return new com.badlogic.gdx.math.Rectangle(x, y, width, height);
    }

    /**
     * Render rectangle selection
     */
    private void renderRectangleSelection() {
        if (rectangleSelection == null && !isSelecting)
            return;

        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (!lassoSelectionTiles.isEmpty()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 1, 1, 0.22f);
            for (long key : lassoSelectionTiles) {
                int tileX = unpackTileX(key);
                int tileY = unpackTileY(key);
                shapeRenderer.rect(
                        tileX * GameConstants.GRID_W,
                        tileY * GameConstants.GRID_H,
                        GameConstants.GRID_W,
                        GameConstants.GRID_H);
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0, 1, 1, 1);
            Gdx.gl.glLineWidth(2);
            for (long key : lassoSelectionTiles) {
                int tileX = unpackTileX(key);
                int tileY = unpackTileY(key);
                shapeRenderer.rect(
                        tileX * GameConstants.GRID_W,
                        tileY * GameConstants.GRID_H,
                        GameConstants.GRID_W,
                        GameConstants.GRID_H);
            }
            Gdx.gl.glLineWidth(1);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            return;
        }

        int startX, startY, endX, endY;

        if (isSelecting) {
            // Draw selection in progress
            Vector3 worldCoords = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            int currentX = (int) (worldCoords.x / GameConstants.GRID_W);
            int currentY = (int) (worldCoords.y / GameConstants.GRID_H);

            startX = Math.min((int) selectionStart.x, currentX);
            startY = Math.min((int) selectionStart.y, currentY);
            endX = Math.max((int) selectionStart.x, currentX);
            endY = Math.max((int) selectionStart.y, currentY);
        } else {
            // Draw completed selection
            startX = rectangleSelection.startX;
            startY = rectangleSelection.startY;
            endX = rectangleSelection.endX;
            endY = rectangleSelection.endY;
        }

        // Fill
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 1, 0.2f);
        shapeRenderer.rect(
                startX * GameConstants.GRID_W,
                startY * GameConstants.GRID_H,
                (endX - startX + 1) * GameConstants.GRID_W,
                (endY - startY + 1) * GameConstants.GRID_H);
        shapeRenderer.end();

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 1, 1);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.rect(
                startX * GameConstants.GRID_W,
                startY * GameConstants.GRID_H,
                (endX - startX + 1) * GameConstants.GRID_W,
                (endY - startY + 1) * GameConstants.GRID_H);
        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private float clampFloat(float value, float min, float max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private void ensureMinimapTexture() {
        if (!minimapDirty && minimapTexture != null) {
            return;
        }
        if (mapReader == null) {
            return;
        }
        if (minimapTexture != null) {
            minimapTexture.dispose();
            minimapTexture = null;
        }
        Pixmap pixmap = new Pixmap(MINIMAP_SIZE, MINIMAP_SIZE, Pixmap.Format.RGBA8888);
        int mapWidth = mapReader.getWidth();
        int mapHeight = mapReader.getHeight();
        float tilesPerPixelX = mapWidth / (float) MINIMAP_SIZE;
        float tilesPerPixelY = mapHeight / (float) MINIMAP_SIZE;

        for (int py = 0; py < MINIMAP_SIZE; py++) {
            int startY = Math.max(0, Math.min(mapHeight - 1, (int) Math.floor(py * tilesPerPixelY)));
            int endY = Math.max(startY, Math.min(mapHeight - 1, (int) Math.ceil((py + 1) * tilesPerPixelY) - 1));

            for (int px = 0; px < MINIMAP_SIZE; px++) {
                int startX = Math.max(0, Math.min(mapWidth - 1, (int) Math.floor(px * tilesPerPixelX)));
                int endX = Math.max(startX, Math.min(mapWidth - 1, (int) Math.ceil((px + 1) * tilesPerPixelX) - 1));

                int bestColor = minimapColorForSprite(null);
                int bestPriority = Integer.MIN_VALUE;
                for (int y = startY; y <= endY; y++) {
                    for (int x = startX; x <= endX; x++) {
                        String sprite = mapReader.getSpriteName(x, y);
                        int priority = minimapColorPriority(sprite);
                        if (priority > bestPriority) {
                            bestPriority = priority;
                            bestColor = minimapColorForSprite(sprite);
                        }
                    }
                }
                pixmap.drawPixel(px, py, bestColor);
            }
        }
        minimapTexture = new Texture(pixmap);
        pixmap.dispose();
        detectHouseCenters(mapWidth, mapHeight);
        minimapDirty = false;
    }

    /**
     * Detect houses by clustering connected wall tiles (8-neighbour flood fill) and
     * store each cluster's centroid (in tile space) when it is large enough to be a
     * house. Populates {@link #houseCenters}, consumed by {@link #renderMinimap()}.
     */
    private void detectHouseCenters(int mapWidth, int mapHeight) {
        houseCenters.clear();
        boolean[] visited = new boolean[mapWidth * mapHeight];
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int idx = y * mapWidth + x;
                if (visited[idx] || !isWallSpriteName(mapReader.getSpriteName(x, y))) {
                    continue;
                }
                // Flood-fill this wall cluster.
                long sumX = 0;
                long sumY = 0;
                int count = 0;
                stack.push(new int[] { x, y });
                visited[idx] = true;
                while (!stack.isEmpty()) {
                    int[] node = stack.pop();
                    int nx = node[0];
                    int ny = node[1];
                    sumX += nx;
                    sumY += ny;
                    count++;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) {
                                continue;
                            }
                            int ax = nx + dx;
                            int ay = ny + dy;
                            if (ax < 0 || ay < 0 || ax >= mapWidth || ay >= mapHeight) {
                                continue;
                            }
                            int aidx = ay * mapWidth + ax;
                            if (visited[aidx] || !isWallSpriteName(mapReader.getSpriteName(ax, ay))) {
                                continue;
                            }
                            visited[aidx] = true;
                            stack.push(new int[] { ax, ay });
                        }
                    }
                }
                if (count >= HOUSE_MIN_WALL_TILES) {
                    houseCenters.add(new float[] { sumX / (float) count, sumY / (float) count });
                }
            }
        }
    }

    private int minimapColorPriority(String spriteNameRaw) {
        if (spriteNameRaw == null || spriteNameRaw.isBlank()) return 0;
        String spriteName = spriteNameRaw.toLowerCase(Locale.ROOT);
        if (spriteName.contains("wall") || spriteName.contains("building")) return 8;
        if (spriteName.contains("tree") || spriteName.contains("bush")) return 7;
        if (spriteName.contains("water")) return 6;
        if (spriteName.contains("stone") || spriteName.contains("rock")) return 5;
        if (spriteName.contains("sand")) return 4;
        if (spriteName.contains("grass")) return 3;
        if (spriteName.contains("ground") || spriteName.contains("dirt") || spriteName.contains("tmpl")) return 2;
        return 1;
    }

    private int minimapColorForSprite(String spriteNameRaw) {
        if (spriteNameRaw == null || spriteNameRaw.isBlank()) return Color.rgba8888(0.1f, 0.1f, 0.1f, 1f);
        String spriteName = spriteNameRaw.toLowerCase(Locale.ROOT);
        if (spriteName.contains("water")) return Color.rgba8888(0.1f, 0.2f, 0.5f, 1f);
        if (spriteName.contains("grass")) return Color.rgba8888(0.2f, 0.4f, 0.2f, 1f);
        if (spriteName.contains("stone") || spriteName.contains("rock")) return Color.rgba8888(0.3f, 0.3f, 0.3f, 1f);
        if (spriteName.contains("sand")) return Color.rgba8888(0.6f, 0.5f, 0.3f, 1f);
        if (spriteName.contains("ground") || spriteName.contains("dirt") || spriteName.contains("tmpl")) return Color.rgba8888(0.25f, 0.35f, 0.25f, 1f);
        if (spriteName.contains("tree") || spriteName.contains("bush")) return Color.rgba8888(0.2f, 0.6f, 0.2f, 1f);
        if (spriteName.contains("wall") || spriteName.contains("building")) return Color.rgba8888(0.5f, 0.4f, 0.3f, 1f);
        return Color.rgba8888(0.4f, 0.5f, 0.4f, 1f);
    }

    // ==================== MUSIC ZONE EDITOR METHODS ====================

    private void toggleMusicZoneEditor() {
        if (editorMode == EditorMode.MUSIC_ZONE_EDITOR) {
            editorMode = EditorMode.SELECT_TILE;
            musicZoneEditingEnabled = false;
            musicZoneSelecting = false;
            pendingMusicZoneSelection = null;
            isSelecting = false;
            rectangleSelection = null;
            if (musicZonesDirty) {
                saveMusicZones();
            }
            return;
        }

        if (editorMode == EditorMode.COLLISION_EDITOR) {
            collisionOverlayVisible = false;
            if (collisionDirty) {
                saveCollisionData();
            }
        }
        closeSpritePicker();
        editorMode = EditorMode.MUSIC_ZONE_EDITOR;
        musicZoneEditingEnabled = true;
        pendingMusicZoneSelection = null;
        loadMusicZones();
    }

    private String getSelectedAmbientMusic() {
        if (selectedAmbientMusicIndex < 0 || selectedAmbientMusicIndex >= AMBIENT_MUSIC_TYPES.length) {
            selectedAmbientMusicIndex = 0;
        }
        return AMBIENT_MUSIC_TYPES[selectedAmbientMusicIndex];
    }

    private void cycleSelectedAmbientMusic(int delta) {
        selectedAmbientMusicIndex = Math.floorMod(selectedAmbientMusicIndex + delta, AMBIENT_MUSIC_TYPES.length);
    }

    private void adjustMusicBrushRadius(int delta) {
        musicBrushRadius = Math.max(0, Math.min(12, musicBrushRadius + delta));
    }

    private void paintMusicBrush(int centerX, int centerY, String music) {
        applyMusicBrush(centerX, centerY, music, false);
    }

    private void eraseMusicBrush(int centerX, int centerY) {
        applyMusicBrush(centerX, centerY, null, true);
    }

    private void applyMusicBrush(int centerX, int centerY, String music, boolean erase) {
        boolean changed = false;
        int radiusSquared = musicBrushRadius * musicBrushRadius;
        for (int y = centerY - musicBrushRadius; y <= centerY + musicBrushRadius; y++) {
            for (int x = centerX - musicBrushRadius; x <= centerX + musicBrushRadius; x++) {
                int dx = x - centerX;
                int dy = y - centerY;
                if (dx * dx + dy * dy > radiusSquared || !isTileInMap(x, y)) {
                    continue;
                }
                if (erase) {
                    changed |= removeMusicTile(x, y);
                } else {
                    changed |= setMusicTile(x, y, music);
                }
            }
        }
        if (changed) {
            musicZonesDirty = true;
        }
    }

    private boolean setMusicTile(int tileX, int tileY, String music) {
        MusicZoneEntry existing = findMusicZoneAt(tileX, tileY);
        if (existing != null && Objects.equals(existing.music, music)) {
            return false;
        }
        musicZones.add(new MusicZoneEntry(music + " tile", tileX, tileY, tileX, tileY, music));
        musicTileIndexDirty = true;
        return true;
    }

    private boolean removeMusicTile(int tileX, int tileY) {
        boolean removed = false;
        for (int i = musicZones.size() - 1; i >= 0; i--) {
            MusicZoneEntry zone = musicZones.get(i);
            if (zone != null && zone.contains(tileX, tileY)) {
                musicZones.remove(i);
                addMusicRectanglePart(zone, zone.x1, zone.y1, zone.x2, tileY - 1);
                addMusicRectanglePart(zone, zone.x1, tileY + 1, zone.x2, zone.y2);
                addMusicRectanglePart(zone, zone.x1, tileY, tileX - 1, tileY);
                addMusicRectanglePart(zone, tileX + 1, tileY, zone.x2, tileY);
                removed = true;
            }
        }
        if (removed) {
            musicTileIndexDirty = true;
        }
        return removed;
    }

    private void addMusicRectanglePart(MusicZoneEntry source, int x1, int y1, int x2, int y2) {
        if (x1 <= x2 && y1 <= y2) {
            musicZones.add(new MusicZoneEntry(source.name, x1, y1, x2, y2, source.music));
        }
    }

    private MusicZoneEntry findMusicZoneAt(int tileX, int tileY) {
        for (int i = musicZones.size() - 1; i >= 0; i--) {
            MusicZoneEntry zone = musicZones.get(i);
            if (zone != null && zone.contains(tileX, tileY)) {
                return zone;
            }
        }
        return null;
    }

    private boolean isTileInMap(int tileX, int tileY) {
        return mapReader != null && tileX >= 0 && tileY >= 0
                && tileX < mapReader.getWidth() && tileY < mapReader.getHeight();
    }

    private int clampTileX(int tileX) {
        if (mapReader == null) {
            return tileX;
        }
        return Math.max(0, Math.min(tileX, mapReader.getWidth() - 1));
    }

    private int clampTileY(int tileY) {
        if (mapReader == null) {
            return tileY;
        }
        return Math.max(0, Math.min(tileY, mapReader.getHeight() - 1));
    }

    private void renderMusicZonesOverlay() {
        if (mapReader == null || musicZones.isEmpty()) {
            return;
        }

        boolean active = editorMode == EditorMode.MUSIC_ZONE_EDITOR || musicZoneEditingEnabled;
        // Do not render ambient music overlay outside music editing mode:
        // with very large zone counts this is by far the biggest frame cost.
        if (!active) {
            return;
        }
        float alpha = active ? 0.22f : 0.08f;
        int[] bounds = lastMusicOverlayBounds != null ? lastMusicOverlayBounds : calculateVisibleBounds(1);
        int minX = bounds[0], maxX = bounds[1], minY = bounds[2], maxY = bounds[3];
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (MusicZoneEntry zone : musicZones) {
            if (zone == null) {
                continue;
            }
            if (zone.x2 < minX || zone.x1 > maxX || zone.y2 < minY || zone.y1 > maxY) {
                continue;
            }
            int zx1 = Math.max(zone.x1, minX);
            int zy1 = Math.max(zone.y1, minY);
            int zx2 = Math.min(zone.x2, maxX);
            int zy2 = Math.min(zone.y2, maxY);
            Color color = getMusicZoneColor(zone.music);
            float x = zx1 * GameConstants.GRID_W;
            float y = zy1 * GameConstants.GRID_H;
            float w = (zx2 - zx1 + 1) * GameConstants.GRID_W;
            float h = (zy2 - zy1 + 1) * GameConstants.GRID_H;

            shapeRenderer.setColor(color.r, color.g, color.b, zone == selectedMusicZone ? 0.34f : alpha);
            shapeRenderer.rect(x, y, w, h);
        }
        shapeRenderer.end();

        // Contours are expensive on dense zone maps; keep them only while editing music zones.
        renderMusicZoneContours();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderMusicBrushPreview() {
        if (editorMode != EditorMode.MUSIC_ZONE_EDITOR || mapReader == null) {
            return;
        }
        Vector3 worldCoords = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        int centerX = (int) (worldCoords.x / GameConstants.GRID_W);
        int centerY = (int) (worldCoords.y / GameConstants.GRID_H);
        if (!isTileInMap(centerX, centerY)) {
            return;
        }

        Color color = getMusicZoneColor(getSelectedAmbientMusic());
        int radiusSquared = musicBrushRadius * musicBrushRadius;
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color.r, color.g, color.b, 0.18f);
        for (int y = centerY - musicBrushRadius; y <= centerY + musicBrushRadius; y++) {
            for (int x = centerX - musicBrushRadius; x <= centerX + musicBrushRadius; x++) {
                int dx = x - centerX;
                int dy = y - centerY;
                if (dx * dx + dy * dy <= radiusSquared && isTileInMap(x, y)) {
                    shapeRenderer.rect(x * GameConstants.GRID_W, y * GameConstants.GRID_H,
                            GameConstants.GRID_W, GameConstants.GRID_H);
                }
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color.r, color.g, color.b, 0.95f);
        Gdx.gl.glLineWidth(1);
        for (int y = centerY - musicBrushRadius; y <= centerY + musicBrushRadius; y++) {
            for (int x = centerX - musicBrushRadius; x <= centerX + musicBrushRadius; x++) {
                int dx = x - centerX;
                int dy = y - centerY;
                if (dx * dx + dy * dy <= radiusSquared && isTileInMap(x, y)) {
                    shapeRenderer.rect(x * GameConstants.GRID_W, y * GameConstants.GRID_H,
                            GameConstants.GRID_W, GameConstants.GRID_H);
                }
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderMusicZoneContours() {
        if (editorMode != EditorMode.MUSIC_ZONE_EDITOR && !musicZoneEditingEnabled) {
            return;
        }
        Map<String, Set<Long>> tilesByMusic = buildMusicTileIndex();
        for (Map.Entry<String, Set<Long>> entry : tilesByMusic.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            renderMusicContourFor(entry.getKey(), entry.getValue(), getMusicZoneColor(entry.getKey()));
        }
    }

    private void renderMusicContourFor(String music, Set<Long> tiles, Color color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color.r, color.g, color.b, 0.92f);
        Gdx.gl.glLineWidth(2);

        for (long key : tiles) {
            int x = unpackTileX(key);
            int y = unpackTileY(key);
            float wx = x * GameConstants.GRID_W;
            float wy = y * GameConstants.GRID_H;
            if (!tiles.contains(packTileKey(x, y - 1))) {
                shapeRenderer.line(wx, wy, wx + GameConstants.GRID_W, wy);
            }
            if (!tiles.contains(packTileKey(x + 1, y))) {
                shapeRenderer.line(wx + GameConstants.GRID_W, wy, wx + GameConstants.GRID_W,
                        wy + GameConstants.GRID_H);
            }
            if (!tiles.contains(packTileKey(x, y + 1))) {
                shapeRenderer.line(wx + GameConstants.GRID_W, wy + GameConstants.GRID_H, wx,
                        wy + GameConstants.GRID_H);
            }
            if (!tiles.contains(packTileKey(x - 1, y))) {
                shapeRenderer.line(wx, wy + GameConstants.GRID_H, wx, wy);
            }
        }

        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();
    }

    private Map<String, Set<Long>> buildMusicTileIndex() {
        if (!musicTileIndexDirty) {
            return cachedMusicTilesByMusic;
        }
        cachedMusicTilesByMusic.clear();
        int[] b = lastMusicOverlayBounds;
        int minX = b != null ? b[0] : 0;
        int maxX = b != null ? b[1] : (mapReader != null ? mapReader.getWidth() - 1 : Integer.MAX_VALUE);
        int minY = b != null ? b[2] : 0;
        int maxY = b != null ? b[3] : (mapReader != null ? mapReader.getHeight() - 1 : Integer.MAX_VALUE);
        for (MusicZoneEntry zone : musicZones) {
            if (zone == null || zone.music == null) continue;
            int zx1 = Math.max(zone.x1, minX);
            int zx2 = Math.min(zone.x2, maxX);
            int zy1 = Math.max(zone.y1, minY);
            int zy2 = Math.min(zone.y2, maxY);
            if (zx1 > zx2 || zy1 > zy2) continue;
            Set<Long> tiles = cachedMusicTilesByMusic.computeIfAbsent(zone.music, ignored -> new HashSet<>());
            for (int y = zy1; y <= zy2; y++) {
                for (int x = zx1; x <= zx2; x++) {
                    tiles.add(packTileKey(x, y));
                }
            }
        }
        musicTileIndexDirty = false;
        return cachedMusicTilesByMusic;
    }

    private Color getMusicZoneColor(String music) {
        int index = 0;
        for (int i = 0; i < AMBIENT_MUSIC_TYPES.length; i++) {
            if (AMBIENT_MUSIC_TYPES[i].equals(music)) {
                index = i;
                break;
            }
        }
        return switch (index) {
            case 0 -> new Color(0.45f, 0.58f, 1f, 1f);
            case 1 -> new Color(0.65f, 0.42f, 0.95f, 1f);
            case 2 -> new Color(0.35f, 0.82f, 0.45f, 1f);
            case 3 -> new Color(0.42f, 0.78f, 0.78f, 1f);
            case 4 -> new Color(0.2f, 0.62f, 0.28f, 1f);
            case 5 -> new Color(0.88f, 0.78f, 0.32f, 1f);
            case 6 -> new Color(0.95f, 0.25f, 0.25f, 1f);
            default -> new Color(0.8f, 0.8f, 0.9f, 1f);
        };
    }

    private String getMusicZonesPath() {
        String mapName = getMapBaseName(currentMapPath);
        File parent = new File(currentMapPath).getParentFile();
        String baseDir = parent != null ? parent.getPath() : ".";
        return baseDir + File.separator + mapName + MUSIC_ZONES_SUFFIX;
    }

    private String getMusicZonesBinPath() {
        String mapName = getMapBaseName(currentMapPath);
        File parent = new File(currentMapPath).getParentFile();
        String baseDir = parent != null ? parent.getPath() : ".";
        return baseDir + File.separator + mapName + MUSIC_ZONES_BIN_SUFFIX;
    }

    private String getMapBaseName(String mapPath) {
        if (mapPath == null) {
            return "map";
        }
        String name = new File(mapPath).getName();
        return name.replace(".mapbin", "").replace(".map", "").replace(".json.gz", "");
    }

    private String getMapSidecarPath(String suffix) {
        File mapFile = new File(currentMapPath);
        File parent = mapFile.getParentFile();
        String baseDir = parent != null ? parent.getPath() : ".";
        return baseDir + File.separator + getMapBaseName(currentMapPath) + suffix;
    }

    private String getCollisionMapPath() {
        return getMapSidecarPath(".colbin");
    }

    private void loadMusicZones() {
        musicZones.clear();
        musicTileIndexDirty = true;
        selectedMusicZone = null;
        File bin = new File(getMusicZonesBinPath());
        if (bin.exists()) {
            try {
                for (MusicZoneBinaryIO.Entry entry : MusicZoneBinaryIO.read(bin)) {
                    addLoadedMusicZone(entry.name, entry.x1, entry.y1, entry.x2, entry.y2, entry.music);
                }
                musicZonesDirty = false;
                musicTileIndexDirty = true;
                return;
            } catch (Exception e) {
                log.warn("Failed to load binary music zones, falling back to JSON", e);
            }
        }
        File f = new File(getMusicZonesPath());
        if (!f.exists()) {
            musicZonesDirty = false;
            return;
        }
        try (FileReader r = new FileReader(f)) {
            Type t = new TypeToken<List<MusicZoneEntry>>() {}.getType();
            List<MusicZoneEntry> loaded = new Gson().fromJson(r, t);
            if (loaded != null) {
                for (MusicZoneEntry zone : loaded) {
                    if (zone == null) {
                        continue;
                    }
                    addLoadedMusicZone(zone.name, zone.x1, zone.y1, zone.x2, zone.y2, zone.music);
                }
            }
            musicTileIndexDirty = true;
        } catch (Exception e) {
            log.warn("Failed to load music zones", e);
        }
        musicZonesDirty = false;
    }

    private void saveMusicZones() {
        try {
            MusicZoneBinaryIO.write(new File(getMusicZonesBinPath()), toMusicZoneBinaryEntries(buildCompressedMusicZones()));
            musicZonesDirty = false;
            showEditorMessage("Music zones saved");
        } catch (Exception e) {
            log.error("Failed to save music zones", e);
            showEditorMessage("Error: Failed to save music zones");
        }
    }

    private void toggleTeleportEditor() {
        if (editorMode == EditorMode.TELEPORT_EDITOR) {
            editorMode = EditorMode.SELECT_TILE;
            selectedTeleport = null;
            return;
        }
        editorMode = EditorMode.TELEPORT_EDITOR;
        collisionOverlayVisible = false;
        musicZoneEditingEnabled = false;
        monsterPlacementEnabled = false;
        npcPlacementEnabled = false;
        if (teleports.isEmpty()) {
            loadTeleports();
        }
        showEditorMessage("Teleport editor: left click sets source, right click sets target");
    }

    private void toggleTeleportOverlay() {
        teleportOverlayVisible = !teleportOverlayVisible;
        teleportContextOpen = false;
        if (teleportOverlayVisible && teleports.isEmpty()) {
            loadTeleports();
        }
        showEditorMessage("Teleport overlay: " + (teleportOverlayVisible ? "ON" : "OFF"));
    }

    private void toggleObjectPositionEditor() {
        if (editorMode == EditorMode.OBJECT_POSITION_EDITOR) {
            if (objectPositionsDirty) {
                saveObjectPositions();
            }
            editorMode = EditorMode.SELECT_TILE;
            selectedObjectPositionIndex = -1;
            return;
        }
        editorMode = EditorMode.OBJECT_POSITION_EDITOR;
        collisionOverlayVisible = false;
        musicZoneEditingEnabled = false;
        monsterPlacementEnabled = false;
        npcPlacementEnabled = false;
        selectedDecorInfo = null;
        objectsVisible = true;
        if (mapRenderer != null) {
            mapRenderer.setObjectsVisible(true);
        }
        if (objectPositions.isEmpty()) {
            loadObjectPositions();
        }
        if (objectPositionTypes.isEmpty()) {
            objectPositionTypes.addAll(ObjectMappings.load().keySet());
            objectPositionTypes.sort(String::compareToIgnoreCase);
        }
        showEditorMessage("Object editor: left click places/moves selected object");
    }

    private void loadObjectPositions() {
        objectPositions.clear();
        selectedObjectPositionIndex = -1;
        File bin = new File(Paths.OBJECT_POSITIONS_BIN);
        if (!bin.exists()) {
            return;
        }
        try {
            objectPositions.addAll(ObjectPositionBinaryIO.read(bin));
        } catch (Exception e) {
            log.warn("Failed to load object positions", e);
            showEditorMessage("Error: object positions not loaded");
        }
        objectPositionTypes.clear();
        objectPositionTypes.addAll(ObjectMappings.load().keySet());
        objectPositionTypes.sort(String::compareToIgnoreCase);
        if (mapRenderer != null) {
            mapRenderer.setObjectPositionsForEditor(objectPositions);
        }
    }

    private void saveObjectPositions() {
        try {
            ObjectPositionBinaryIO.write(new File(Paths.OBJECT_POSITIONS_BIN), objectPositions);
            objectPositionsDirty = false;
            if (mapRenderer != null) {
                mapRenderer.reloadObjectPositions();
            }
            showEditorMessage("Object positions saved");
        } catch (Exception e) {
            log.error("Failed to save object positions", e);
            showEditorMessage("Error: object positions not saved");
        }
    }

    private boolean handleObjectPositionEditorClick(int screenX, int screenY, int button) {
        int uiY = Gdx.graphics.getHeight() - screenY;
        if (objectPositionPanelBounds != null && objectPositionPanelBounds.contains(screenX, uiY)) {
            return handleObjectPositionPanelClick(screenX, uiY, button);
        }
        if (button != Input.Buttons.LEFT || mapReader == null) {
            return true;
        }
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
        int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
        if (!isTileInMap(tileX, tileY)) {
            return true;
        }
        int z = getCurrentMapZ();
        int clickedObjectIndex = findObjectAtWorld(worldCoords.x, worldCoords.y);
        if (clickedObjectIndex >= 0 && clickedObjectIndex != selectedObjectPositionIndex) {
            selectObjectPosition(clickedObjectIndex, false);
            ObjectPos selected = objectPositions.get(clickedObjectIndex);
            showEditorMessage("Object selected: " + selected.name() + " - arrows adjust offsets");
            return true;
        }
        if (clickedObjectIndex < 0 && selectedObjectPositionIndex >= 0 && selectedObjectPositionIndex < objectPositions.size()) {
            selectedObjectPositionIndex = -1;
            showEditorMessage("Object deselected");
            return true;
        }
        if (selectedObjectPositionIndex >= 0 && selectedObjectPositionIndex < objectPositions.size()) {
            ObjectPos old = objectPositions.get(selectedObjectPositionIndex);
            objectPositions.set(selectedObjectPositionIndex, new ObjectPos(old.name(), tileX, tileY, z, old.mirror()));
            showEditorMessage("Object moved: " + old.name() + " (" + tileX + ", " + tileY + ")");
        } else {
            String type = objectPositionTypes.isEmpty() ? "CLOSED_WOODEN_DOOR" : objectPositionTypes.get(selectedObjectTypeIndex);
            objectPositions.add(new ObjectPos(type, tileX, tileY, z));
            selectedObjectPositionIndex = objectPositions.size() - 1;
            objectPositionScrollOffset = Math.max(0, selectedObjectPositionIndex - 10);
            showEditorMessage("Object created: " + type + " (" + tileX + ", " + tileY + ")");
        }
        objectPositionsDirty = true;
        if (mapRenderer != null) {
            mapRenderer.setObjectPositionsForEditor(objectPositions);
        }
        return true;
    }

    private boolean handleObjectPositionEditorKeyTyped(char character) {
        if (!objectPositionTypeDropdownOpen) {
            return false;
        }
        objectPositionTypeDropdown.setItems(objectPositionTypes)
                .visibleRows(objectPositionTypeVisibleRows)
                .rowHeight(22f);
        objectPositionTypeDropdown.setOpen(true);
        objectPositionTypeDropdown.setScrollOffset(objectPositionTypeScrollOffset);
        if (!objectPositionTypeDropdown.handleKeyTyped(character)) {
            return false;
        }
        selectedObjectTypeIndex = objectPositionTypeDropdown.selectedIndex();
        objectPositionTypeScrollOffset = objectPositionTypeDropdown.scrollOffset();
        if (selectedObjectTypeIndex >= 0 && selectedObjectTypeIndex < objectPositionTypes.size()) {
            renameSelectedObjectType();
        }
        objectPositionTypeDropdownOpen = objectPositionTypeDropdown.isOpen();
        return true;
    }

    private boolean handleObjectPositionPanelClick(int screenX, int uiY, int button) {
        if (button != Input.Buttons.LEFT || objectPositionPanelBounds == null) {
            return true;
        }
        if (objectPositionNewBounds != null && objectPositionNewBounds.contains(screenX, uiY)) {
            selectedObjectPositionIndex = -1;
            showEditorMessage("Click map to create object");
            return true;
        }
        if (objectPositionDeleteBounds != null && objectPositionDeleteBounds.contains(screenX, uiY)
                && selectedObjectPositionIndex >= 0 && selectedObjectPositionIndex < objectPositions.size()) {
            objectPositions.remove(selectedObjectPositionIndex);
            selectedObjectPositionIndex = -1;
            objectPositionsDirty = true;
            if (mapRenderer != null) {
                mapRenderer.setObjectPositionsForEditor(objectPositions);
            }
            objectPositionScrollOffset = Math.max(0, Math.min(objectPositionScrollOffset,
                    Math.max(0, objectPositions.size() - objectPositionVisibleRows)));
            return true;
        }
        if (objectPositionSaveBounds != null && objectPositionSaveBounds.contains(screenX, uiY)) {
            saveObjectPositions();
            return true;
        }
        if (objectPositionTypeDropdownOpen && objectPositionTypeDropdownBounds != null
                && objectPositionTypeDropdownBounds.contains(screenX, uiY)) {
            if (objectPositionTypeScrollTrackBounds != null && objectPositionTypeScrollTrackBounds.contains(screenX, uiY)) {
                int maxOffset = Math.max(0, objectPositionTypes.size() - objectPositionTypeVisibleRows);
                if (maxOffset > 0) {
                    float ratioFromTop = (objectPositionTypeScrollTrackBounds.y + objectPositionTypeScrollTrackBounds.height - uiY)
                            / objectPositionTypeScrollTrackBounds.height;
                    objectPositionTypeScrollOffset = Math.max(0, Math.min(maxOffset, Math.round(ratioFromTop * maxOffset)));
                }
                return true;
            }
            int row = (int) ((objectPositionTypeDropdownBounds.y + objectPositionTypeDropdownBounds.height - uiY) / 22f);
            int index = objectPositionTypeScrollOffset + row;
            if (index >= 0 && index < objectPositionTypes.size()) {
                selectedObjectTypeIndex = index;
                objectPositionTypeDropdownOpen = false;
                renameSelectedObjectType();
            }
            return true;
        }
        if (objectPositionTypeBounds != null && objectPositionTypeBounds.contains(screenX, uiY)) {
            objectPositionTypeDropdownOpen = !objectPositionTypeDropdownOpen;
            if (objectPositionTypeDropdownOpen) {
                objectPositionTypeScrollOffset = Math.max(0, Math.min(selectedObjectTypeIndex,
                        Math.max(0, objectPositionTypes.size() - objectPositionTypeVisibleRows)));
            }
            return true;
        }
        objectPositionTypeDropdownOpen = false;
        if (objectPositionScrollTrackBounds != null && objectPositionScrollTrackBounds.contains(screenX, uiY)) {
            int maxOffset = Math.max(0, objectPositions.size() - objectPositionVisibleRows);
            if (maxOffset > 0) {
                float ratioFromTop = (objectPositionScrollTrackBounds.y + objectPositionScrollTrackBounds.height - uiY)
                        / objectPositionScrollTrackBounds.height;
                objectPositionScrollOffset = Math.max(0, Math.min(maxOffset, Math.round(ratioFromTop * maxOffset)));
            }
            return true;
        }
        if (objectPositionListBounds != null && objectPositionListBounds.contains(screenX, uiY)) {
            float firstRowTop = objectPositionListBounds.y + objectPositionListBounds.height - 28f;
            int index = (int) ((firstRowTop - uiY) / 22f) + objectPositionScrollOffset;
            if (index >= 0 && index < objectPositions.size()) {
                selectObjectPosition(index, false);
                ObjectPos selected = objectPositions.get(index);
                goToObjectPosition(selected);
            }
        }
        return true;
    }

    private int findObjectAtWorld(float worldX, float worldY) {
        if (objectPositions.isEmpty() || mapRenderer == null || spriteLoader == null) {
            return -1;
        }
        // Don't hit-test hidden objects (except in the object editor, where they stay editable).
        if (!objectsVisible && editorMode != EditorMode.OBJECT_POSITION_EDITOR) {
            return -1;
        }
        int currentZ = getCurrentMapZ();
        for (int i = objectPositions.size() - 1; i >= 0; i--) {
            ObjectPos pos = objectPositions.get(i);
            if ((int) pos.z() != currentZ) {
                continue;
            }
            ObjectMapping mapping = mapRenderer.getObjectMappings().get(
                    pos.name() == null ? "" : pos.name().toUpperCase(Locale.ROOT));
            if (mapping == null || mapping.sprite == null || mapping.sprite.isBlank()) {
                continue;
            }
            String[] frames = computeObjectPreviewFrames(mapping.sprite);
            if (frames.length == 0) {
                continue;
            }
            TextureRegion region;
            try {
                region = spriteLoader.getRegionFromSpriteName(frames[0]);
            } catch (Exception e) {
                continue;
            }
            if (region == null) {
                continue;
            }
            boolean mirror = mapping.mirror ^ pos.mirror();
            float[] offsets = getSpriteDrawOffsets(frames[0], mirror);
            ModifSprites.Offset off = mirror ? modifSprites.getOffset(frames[0] + "M")
                    : modifSprites.getOffset(frames[0]);
            float w = region.getRegionWidth();
            float h = region.getRegionHeight();
            float px = pos.x() * GameConstants.GRID_W + offsets[0] + off.x;
            float py = pos.y() * GameConstants.GRID_H + offsets[1] + off.y;
            if (worldX >= px && worldX <= px + w && worldY >= py && worldY <= py + h) {
                return i;
            }
        }
        return -1;
    }

    private void startDraggingObject(int index, float mouseWorldX, float mouseWorldY) {
        if (index < 0 || index >= objectPositions.size()) {
            return;
        }
        selectObjectPosition(index, true);
        ObjectPos pos = objectPositions.get(index);
        objectDragState = new ObjectDragState();
        objectDragState.index = index;
        objectDragState.originalTileX = (int) pos.x();
        objectDragState.originalTileY = (int) pos.y();
        objectDragState.originalZ = (int) pos.z();
        objectDragState.startMouseTileX = (int) (mouseWorldX / GameConstants.GRID_W);
        objectDragState.startMouseTileY = (int) (mouseWorldY / GameConstants.GRID_H);
        objectDragState.targetTileX = objectDragState.originalTileX;
        objectDragState.targetTileY = objectDragState.originalTileY;
        objectDragState.targetZ = objectDragState.originalZ;
        objectContextOpen = false;
        showEditorMessage("Object selected: " + pos.name());
    }

    private void finishDraggingObject() {
        if (objectDragState == null) {
            return;
        }
        if (objectDragState.index >= 0 && objectDragState.index < objectPositions.size()) {
            ObjectPos old = objectPositions.get(objectDragState.index);
            if (objectDragState.originalTileX != objectDragState.targetTileX
                    || objectDragState.originalTileY != objectDragState.targetTileY
                    || objectDragState.originalZ != objectDragState.targetZ) {
                objectPositions.set(objectDragState.index, new ObjectPos(
                        old.name(), objectDragState.targetTileX, objectDragState.targetTileY, objectDragState.targetZ, old.mirror()));
                objectPositionsDirty = true;
                selectedObjectPositionIndex = objectDragState.index;
                saveObjectPositions();
                showEditorMessage("Object moved: " + old.name() + " (" + objectDragState.targetTileX + ", "
                        + objectDragState.targetTileY + ")");
            }
        }
        objectDragState = null;
    }

    private void updateDraggedObjectPreview() {
        if (objectDragState == null || objectDragState.index < 0 || objectDragState.index >= objectPositions.size()) {
            return;
        }
        ObjectPos old = objectPositions.get(objectDragState.index);
        objectPositions.set(objectDragState.index, new ObjectPos(
                old.name(), objectDragState.targetTileX, objectDragState.targetTileY, objectDragState.targetZ, old.mirror()));
        if (mapRenderer != null) {
            mapRenderer.setObjectPositionsForEditor(objectPositions);
        }
    }

    private void selectObjectPosition(int index, boolean scrollIntoView) {
        if (index < 0 || index >= objectPositions.size()) {
            return;
        }
        selectedObjectPositionIndex = index;
        ObjectPos selected = objectPositions.get(index);
        String name = selected.name();
        int typeIndex = objectPositionTypes.indexOf(name);
        if (typeIndex >= 0) {
            selectedObjectTypeIndex = typeIndex;
        }
        if (scrollIntoView) {
            objectPositionScrollOffset = Math.max(0, selectedObjectPositionIndex - 10);
        }
    }

    private void goToObjectPosition(ObjectPos pos) {
        int objectX = (int) pos.x();
        int objectY = (int) pos.y();
        int objectZ = (int) pos.z();
        int currentZ = getCurrentMapZ();
        if (objectZ != currentZ) {
            MapDefinition target = MapDefinition.fromZ(objectZ);
            if (target == null) {
                showEditorMessage("Object map z not found: " + objectZ);
                return;
            }
            pendingTeleportAfterMapSwitch = true;
            pendingTeleportTileX = objectX;
            pendingTeleportTileY = objectY;
            startMapSwitchLoading(target.getMapPath(), availableMaps.indexOf(normalizeMapPath(target.getMapPath())));
            return;
        }
        if (!isTileInMap(objectX, objectY)) {
            showEditorMessage("Object coordinates out of map bounds");
            return;
        }
        cameraPosition.set(objectX * GameConstants.GRID_W, objectY * GameConstants.GRID_H);
        camera.position.set(cameraPosition.x, cameraPosition.y, 0f);
        camera.zoom = zoom;
        camera.update();
        showEditorMessage("Object selected: " + pos.name() + " (" + objectX + ", " + objectY + ", z" + objectZ + ")");
    }

    private void renameSelectedObjectType() {
        if (selectedObjectPositionIndex < 0 || selectedObjectPositionIndex >= objectPositions.size() || objectPositionTypes.isEmpty()) {
            return;
        }
        ObjectPos old = objectPositions.get(selectedObjectPositionIndex);
        objectPositions.set(selectedObjectPositionIndex, new ObjectPos(
                objectPositionTypes.get(selectedObjectTypeIndex), old.x(), old.y(), old.z(), old.mirror()));
        objectPositionsDirty = true;
    }

    private int getCurrentMapZ() {
        String current = normalizeMapPath(currentMapPath);
        for (MapDefinition def : MapDefinition.values()) {
            if (Objects.equals(current, normalizeMapPath(def.getMapPath()))) {
                return def.getZ();
            }
        }
        return teleportEditorZ;
    }

    private void loadTeleports() {
        teleports.clear();
        selectedTeleport = null;
        File bin = new File(Paths.TELEPORTS_BIN);
        if (bin.exists()) {
            try {
                for (TeleportBinaryIO.Entry entry : TeleportBinaryIO.read(bin)) {
                    teleports.add(fromTeleportBinaryEntry(entry));
                }
                return;
            } catch (Exception e) {
                log.warn("Failed to load binary teleports", e);
            }
        }

        File json = new File("assets/teleports/teleports.json");
        if (!json.exists()) {
            return;
        }
        try (FileReader r = new FileReader(json)) {
            TeleportJsonRoot root = new Gson().fromJson(r, TeleportJsonRoot.class);
            if (root != null && root.teleports != null) {
                for (TeleportJsonEntry entry : root.teleports) {
                    if (entry != null && entry.source != null && entry.target != null) {
                        teleports.add(new TeleportEntry(entry.id,
                                entry.source.z, entry.source.x, entry.source.y,
                                entry.target.z, entry.target.x, entry.target.y));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load teleports JSON", e);
        }
    }

    private void saveTeleports() {
        try {
            TeleportBinaryIO.write(new File(Paths.TELEPORTS_BIN), toTeleportBinaryEntries());
            teleportsDirty = false;
            showEditorMessage("Teleports saved");
        } catch (Exception e) {
            log.error("Failed to save teleports", e);
            showEditorMessage("Error: Failed to save teleports");
        }
    }

    private List<TeleportBinaryIO.Entry> toTeleportBinaryEntries() {
        List<TeleportBinaryIO.Entry> entries = new ArrayList<>(teleports.size());
        for (TeleportEntry teleport : teleports) {
            TeleportBinaryIO.Entry entry = new TeleportBinaryIO.Entry();
            entry.id = teleport.id;
            entry.sourceZ = teleport.sourceZ;
            entry.sourceX = teleport.sourceX;
            entry.sourceY = teleport.sourceY;
            entry.targetZ = teleport.targetZ;
            entry.targetX = teleport.targetX;
            entry.targetY = teleport.targetY;
            entries.add(entry);
        }
        return entries;
    }

    private TeleportEntry fromTeleportBinaryEntry(TeleportBinaryIO.Entry entry) {
        return new TeleportEntry(entry.id, entry.sourceZ, entry.sourceX, entry.sourceY,
                entry.targetZ, entry.targetX, entry.targetY);
    }

    private boolean handleTeleportEditorClick(int screenX, int screenY, int button) {
        int uiY = Gdx.graphics.getHeight() - screenY;
        if (teleportPanelBounds != null && teleportPanelBounds.contains(screenX, uiY)) {
            return handleTeleportPanelClick(screenX, uiY, button);
        }
        return true;
    }

    private boolean setTeleportPointFromPreview(com.badlogic.gdx.math.Rectangle bounds, int uiX, int uiY, boolean source) {
        if (selectedTeleport == null || bounds == null || !bounds.contains(uiX, uiY)) {
            return false;
        }
        int z = source ? selectedTeleport.sourceZ : selectedTeleport.targetZ;
        MapDefinition mapDefinition = MapDefinition.fromZ(z);
        int width = getPreviewMapWidth(mapDefinition);
        int height = getPreviewMapHeight(mapDefinition);
        if (width <= 0 || height <= 0) {
            return false;
        }
        float normalizedX = previewNormalizedXAtScreen(bounds, uiX, source);
        float normalizedY = previewNormalizedYAtScreen(bounds, uiY, source);
        int tileX = Math.max(0, Math.min(width - 1, (int) (normalizedX * width)));
        int tileY = Math.max(0, Math.min(height - 1, (int) (normalizedY * height)));
        if (source) {
            selectedTeleport.sourceX = tileX;
            selectedTeleport.sourceY = tileY;
            showEditorMessage("Teleport source set: (" + tileX + ", " + tileY + ", z" + z + ")");
        } else {
            selectedTeleport.targetX = tileX;
            selectedTeleport.targetY = tileY;
            showEditorMessage("Teleport target set: (" + tileX + ", " + tileY + ", z" + z + ")");
        }
        teleportsDirty = true;
        return true;
    }

    private int getPreviewMapWidth(MapDefinition mapDefinition) {
        return getPreviewMapDimensions(mapDefinition)[0];
    }

    private int getPreviewMapHeight(MapDefinition mapDefinition) {
        return getPreviewMapDimensions(mapDefinition)[1];
    }

    private int[] getPreviewMapDimensions(MapDefinition mapDefinition) {
        int z = mapDefinition.getZ();
        int[] cached = teleportPreviewDimensions.get(z);
        if (cached != null) {
            return cached;
        }
        int[] dimensions;
        if (mapReader != null && normalizeMapPath(mapDefinition.getMapPath()).equals(normalizeMapPath(currentMapPath))) {
            dimensions = new int[] { mapReader.getWidth(), mapReader.getHeight() };
        } else {
            try (MapReader reader = new MapReader(new File(mapDefinition.getMapPath()))) {
                dimensions = new int[] { reader.getWidth(), reader.getHeight() };
            } catch (Exception e) {
                log.warn("Failed to read preview map dimensions for {}", mapDefinition.getMapPath(), e);
                dimensions = new int[] { 0, 0 };
            }
        }
        teleportPreviewDimensions.put(z, dimensions);
        return dimensions;
    }

    private boolean handleTeleportPanelClick(int screenX, int uiY, int button) {
        if (button != Input.Buttons.LEFT) {
            return true;
        }
        if (setTeleportPointFromPreview(teleportSourceMapBounds, screenX, uiY, true)) {
            return true;
        }
        if (setTeleportPointFromPreview(teleportTargetMapBounds, screenX, uiY, false)) {
            return true;
        }
        float x = teleportPanelBounds.x;
        float top = teleportPanelBounds.y + teleportPanelBounds.height;
        if (uiY >= top - 104 && uiY <= top - 76) {
            if (screenX >= x + 18 && screenX <= x + 82) {
                selectedTeleport = createTeleport(lastClickedTileX >= 0 ? lastClickedTileX : 0,
                        lastClickedTileY >= 0 ? lastClickedTileY : 0);
                teleports.add(selectedTeleport);
                teleportScrollOffset = Math.max(0, teleports.size() - 18);
                teleportsDirty = true;
                return true;
            }
            if (screenX >= x + 92 && screenX <= x + 156 && selectedTeleport != null) {
                teleports.remove(selectedTeleport);
                selectedTeleport = teleports.isEmpty() ? null : teleports.get(Math.max(0, Math.min(teleportScrollOffset, teleports.size() - 1)));
                teleportsDirty = true;
                return true;
            }
            if (screenX >= x + 166 && screenX <= x + 238) {
                saveTeleports();
                return true;
            }
            if (screenX >= x + 270 && screenX <= x + 300 && selectedTeleport != null) {
                selectedTeleport.sourceZ = Math.max(0, selectedTeleport.sourceZ - 1);
                teleportsDirty = true;
                return true;
            }
            if (screenX >= x + 306 && screenX <= x + 336 && selectedTeleport != null) {
                selectedTeleport.sourceZ++;
                teleportsDirty = true;
                return true;
            }
            if (screenX >= x + 392 && screenX <= x + 422 && selectedTeleport != null) {
                selectedTeleport.targetZ = Math.max(0, selectedTeleport.targetZ - 1);
                teleportsDirty = true;
                return true;
            }
            if (screenX >= x + 428 && screenX <= x + 458 && selectedTeleport != null) {
                selectedTeleport.targetZ++;
                teleportsDirty = true;
                return true;
            }
        }

        if (teleportListBounds == null || !teleportListBounds.contains(screenX, uiY)) {
            return true;
        }
        float firstRowTop = teleportListBounds.y + teleportListBounds.height - 34f;
        if (uiY > firstRowTop) {
            return true;
        }
        int index = (int) ((firstRowTop - uiY) / 22f) + teleportScrollOffset;
        if (index >= 0 && index < teleports.size()) {
            selectedTeleport = teleports.get(index);
            teleportEditorZ = selectedTeleport.sourceZ;
        }
        return true;
    }

    private boolean handleTeleportEditorScroll(float amountY) {
        int screenX = Gdx.input.getX();
        int uiY = Gdx.graphics.getHeight() - Gdx.input.getY();
        if (teleportSourceMapBounds != null && teleportSourceMapBounds.contains(screenX, uiY)) {
            zoomTeleportPreview(true, screenX, uiY, amountY);
            return true;
        }
        if (teleportTargetMapBounds != null && teleportTargetMapBounds.contains(screenX, uiY)) {
            zoomTeleportPreview(false, screenX, uiY, amountY);
            return true;
        }
        return false;
    }

    private void zoomTeleportPreview(boolean source, int uiX, int uiY, float amountY) {
        com.badlogic.gdx.math.Rectangle bounds = source ? teleportSourceMapBounds : teleportTargetMapBounds;
        if (bounds == null) {
            return;
        }
        float oldZoom = source ? teleportSourcePreviewZoom : teleportTargetPreviewZoom;
        float centerX = source ? teleportSourcePreviewCenterX : teleportTargetPreviewCenterX;
        float centerY = source ? teleportSourcePreviewCenterY : teleportTargetPreviewCenterY;
        float cursorX = (uiX - bounds.x) / bounds.width;
        float cursorY = (uiY - bounds.y) / bounds.height;
        float oldVisible = 1f / oldZoom;
        float worldUnderCursorX = centerX - oldVisible / 2f + cursorX * oldVisible;
        float worldUnderCursorY = centerY - oldVisible / 2f + cursorY * oldVisible;
        float newZoom = clampFloat(oldZoom * (amountY < 0 ? 1.25f : 0.8f), 1f, TELEPORT_PREVIEW_MAX_ZOOM);
        float newVisible = 1f / newZoom;
        centerX = clampPreviewCenter(worldUnderCursorX - cursorX * newVisible + newVisible / 2f, newVisible);
        centerY = clampPreviewCenter(worldUnderCursorY - cursorY * newVisible + newVisible / 2f, newVisible);

        if (source) {
            teleportSourcePreviewZoom = newZoom;
            teleportSourcePreviewCenterX = centerX;
            teleportSourcePreviewCenterY = centerY;
        } else {
            teleportTargetPreviewZoom = newZoom;
            teleportTargetPreviewCenterX = centerX;
            teleportTargetPreviewCenterY = centerY;
        }
    }

    private float clampPreviewCenter(float center, float visible) {
        float half = visible / 2f;
        return clampFloat(center, half, 1f - half);
    }

    private float previewNormalizedXAtScreen(com.badlogic.gdx.math.Rectangle bounds, int uiX, boolean source) {
        float zoom = source ? teleportSourcePreviewZoom : teleportTargetPreviewZoom;
        float center = source ? teleportSourcePreviewCenterX : teleportTargetPreviewCenterX;
        float visible = 1f / zoom;
        float min = clampFloat(center - visible / 2f, 0f, 1f - visible);
        return clampFloat(min + ((uiX - bounds.x) / bounds.width) * visible, 0f, 0.999999f);
    }

    private float previewNormalizedYAtScreen(com.badlogic.gdx.math.Rectangle bounds, int uiY, boolean source) {
        float zoom = source ? teleportSourcePreviewZoom : teleportTargetPreviewZoom;
        float center = source ? teleportSourcePreviewCenterY : teleportTargetPreviewCenterY;
        float visible = 1f / zoom;
        float min = clampFloat(center - visible / 2f, 0f, 1f - visible);
        return clampFloat(min + ((uiY - bounds.y) / bounds.height) * visible, 0f, 0.999999f);
    }

    private TeleportEntry createTeleport(int sourceX, int sourceY) {
        int nextId = 0;
        for (TeleportEntry teleport : teleports) {
            nextId = Math.max(nextId, teleport.id + 1);
        }
        return new TeleportEntry(nextId, teleportEditorZ, sourceX, sourceY, teleportEditorZ, sourceX, sourceY);
    }

    private TeleportHit findTeleportAtCurrentMapTile(int tileX, int tileY) {
        int z = getCurrentMapZ();
        for (TeleportEntry teleport : teleports) {
            if (teleport.sourceZ == z && teleport.sourceX == tileX && teleport.sourceY == tileY) {
                return new TeleportHit(teleport, true);
            }
            if (teleport.targetZ == z && teleport.targetX == tileX && teleport.targetY == tileY) {
                return new TeleportHit(teleport, false);
            }
        }
        return null;
    }
/**
 * Record for TeleportHit.
 */

    private record TeleportHit(TeleportEntry teleport, boolean source) {}

    private boolean handleTeleportOverlayMapClick(int screenX, int screenY, Vector3 worldCoords, int button) {
        if (mapReader == null || editorMode == EditorMode.TELEPORT_EDITOR) {
            return false;
        }
        int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
        int tileY = (int) (worldCoords.y / GameConstants.GRID_H);
        if (!isTileInMap(tileX, tileY)) {
            return false;
        }
        TeleportHit hit = findTeleportAtCurrentMapTile(tileX, tileY);
        if (button == Input.Buttons.RIGHT) {
            if (hit != null) {
                openTeleportContextMenu(hit.teleport(), hit.source(), screenX, screenY);
                return true;
            }
            teleportContextOpen = false;
            return false;
        }
        if (button != Input.Buttons.LEFT) {
            return false;
        }
        teleportContextOpen = false;
        if (hit != null) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                startDraggingTeleport(hit.teleport(), hit.source(), tileX, tileY);
                return true;
            }
            teleportToOtherEndpoint(hit.teleport(), hit.source());
            return true;
        }
        return false;
    }

    private void startDraggingTeleport(TeleportEntry teleport, boolean source, int tileX, int tileY) {
        draggedTeleport = teleport;
        draggedTeleportEndpointIsSource = source;
        draggedTeleportTargetX = tileX;
        draggedTeleportTargetY = tileY;
        selectedTeleport = teleport;
        selectedTeleportEndpointIsSource = source;
        teleportContextOpen = false;
        showEditorMessage("Dragging teleport " + (source ? "source" : "destination"));
    }

    private void finishDraggingTeleport() {
        if (draggedTeleport == null) return;
        if (draggedTeleportEndpointIsSource) {
            draggedTeleport.sourceX = draggedTeleportTargetX;
            draggedTeleport.sourceY = draggedTeleportTargetY;
        } else {
            draggedTeleport.targetX = draggedTeleportTargetX;
            draggedTeleport.targetY = draggedTeleportTargetY;
        }
        teleportsDirty = true;
        showEditorMessage("Teleport " + (draggedTeleportEndpointIsSource ? "source" : "destination")
                + " moved to (" + draggedTeleportTargetX + ", " + draggedTeleportTargetY + ")");
        draggedTeleport = null;
    }

    private void openTeleportContextMenu(TeleportEntry teleport, boolean source, int screenX, int screenY) {
        teleportContextTarget = teleport;
        teleportContextTargetIsSource = source;
        float menuWidth = 128f;
        float menuHeight = 82f;
        float uiY = Gdx.graphics.getHeight() - screenY;
        float x = Math.min(screenX, Gdx.graphics.getWidth() - menuWidth - 8f);
        float y = Math.max(8f, Math.min(uiY - menuHeight, Gdx.graphics.getHeight() - MENU_BAR_HEIGHT - menuHeight));
        teleportContextBounds = new com.badlogic.gdx.math.Rectangle(x, y, menuWidth, menuHeight);
        teleportContextMenu = new EditorContextMenu("Teleport", new Color(0.05f, 0.72f, 0.25f, 1f))
                .add("Teleport", () -> teleportToOtherEndpoint(teleportContextTarget, teleportContextTargetIsSource))
                .add("Edit", () -> openTeleportEditorFor(teleportContextTarget))
                .add("Delete", () -> deleteTeleport(teleportContextTarget))
                .onClose(() -> teleportContextOpen = false);
        teleportContextMenu.openAt(screenX, screenY, menuWidth, MENU_BAR_HEIGHT);
        teleportContextOpen = true;
    }

    /**
     * Navigate the editor to the opposite endpoint of the teleport. When the user
     * right-clicked the source tile, jump to the target coordinates (and vice
     * versa) so they can follow the teleport to where it leads. Reuses the same
     * camera/map-switch logic as "Go to Coordinates".
     */
    private void teleportToOtherEndpoint(TeleportEntry teleport, boolean clickedSource) {
        teleportContextOpen = false;
        if (teleport == null) {
            return;
        }
        int x;
        int y;
        int z;
        if (clickedSource) {
            x = teleport.targetX;
            y = teleport.targetY;
            z = teleport.targetZ;
        } else {
            x = teleport.sourceX;
            y = teleport.sourceY;
            z = teleport.sourceZ;
        }
        applyTeleportGotoDialog(x, y, z);
    }

    private boolean handleTeleportContextClick(int screenX, int screenY, int button) {
        if (!teleportContextOpen || teleportContextMenu == null) {
            return false;
        }
        return teleportContextMenu.handleClick(screenX, screenY, button);
    }

    private void openTeleportEditorFor(TeleportEntry teleport) {
        if (teleport == null) {
            return;
        }
        if (teleports.isEmpty()) {
            loadTeleports();
        }
        selectedTeleport = teleport;
        teleportEditorZ = teleport.sourceZ;
        editorMode = EditorMode.TELEPORT_EDITOR;
        int index = teleports.indexOf(teleport);
        if (index >= 0) {
            teleportScrollOffset = Math.max(0, index - 2);
        }
    }

    private void deleteTeleport(TeleportEntry teleport) {
        if (teleport == null) {
            return;
        }
        teleports.remove(teleport);
        if (selectedTeleport == teleport) {
            selectedTeleport = null;
        }
        if (teleportContextTarget == teleport) {
            teleportContextTarget = null;
            teleportContextOpen = false;
        }
        teleportsDirty = true;
        showEditorMessage("Teleport deleted");
    }

    private void renderTeleportMapOverlay() {
        if (!teleportOverlayVisible || teleports.isEmpty() || mapReader == null || editorMode == EditorMode.TELEPORT_EDITOR) {
            return;
        }
        int currentZ = getCurrentMapZ();
        int[] bounds = calculateVisibleBounds(1);
        int startX = bounds[0], endX = bounds[1], startY = bounds[2], endY = bounds[3];

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(0, BOTTOM_INFO_HEIGHT, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight() - BOTTOM_INFO_HEIGHT - MENU_BAR_HEIGHT);
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (TeleportEntry teleport : teleports) {
            if (teleport.sourceZ == currentZ
                    && teleport.sourceX >= startX && teleport.sourceX <= endX
                    && teleport.sourceY >= startY && teleport.sourceY <= endY) {
                boolean selected = teleport == selectedTeleport && selectedTeleportEndpointIsSource;
                shapeRenderer.setColor(0.05f, 0.95f, 0.25f, selected ? 0.62f : 0.42f);
                shapeRenderer.rect(teleport.sourceX * GameConstants.GRID_W, teleport.sourceY * GameConstants.GRID_H,
                        GameConstants.GRID_W, GameConstants.GRID_H);
            }
            if (teleport.targetZ == currentZ
                    && teleport.targetX >= startX && teleport.targetX <= endX
                    && teleport.targetY >= startY && teleport.targetY <= endY) {
                boolean selected = teleport == selectedTeleport && !selectedTeleportEndpointIsSource;
                shapeRenderer.setColor(0.05f, 0.95f, 0.25f, selected ? 0.62f : 0.42f);
                shapeRenderer.rect(teleport.targetX * GameConstants.GRID_W, teleport.targetY * GameConstants.GRID_H,
                        GameConstants.GRID_W, GameConstants.GRID_H);
            }
        }
        if (draggedTeleport != null) {
            shapeRenderer.setColor(1f, 0.82f, 0.12f, 0.72f);
            shapeRenderer.rect(draggedTeleportTargetX * GameConstants.GRID_W,
                    draggedTeleportTargetY * GameConstants.GRID_H, GameConstants.GRID_W, GameConstants.GRID_H);
        }
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.82f, 1f, 0.82f, 0.95f);
        for (TeleportEntry teleport : teleports) {
            if (teleport == selectedTeleport) {
                int x = selectedTeleportEndpointIsSource ? teleport.sourceX : teleport.targetX;
                int y = selectedTeleportEndpointIsSource ? teleport.sourceY : teleport.targetY;
                int z = selectedTeleportEndpointIsSource ? teleport.sourceZ : teleport.targetZ;
                if (z == currentZ) {
                    shapeRenderer.rect(x * GameConstants.GRID_W, y * GameConstants.GRID_H,
                            GameConstants.GRID_W, GameConstants.GRID_H);
                }
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    private void renderTeleportContextMenu() {
        if (!teleportContextOpen || teleportContextMenu == null || teleportContextTarget == null) {
            return;
        }
        prepareUiProjection(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        teleportContextMenu.render(uiBatch, shapeRenderer, font);
    }

    private void openObjectContextMenu(int objectIndex, int screenX, int screenY) {
        if (objectIndex < 0 || objectIndex >= objectPositions.size()) {
            objectContextOpen = false;
            objectContextTargetIndex = -1;
            return;
        }
        selectObjectPosition(objectIndex, true);
        objectContextTargetIndex = objectIndex;
        float menuWidth = 168f;
        float menuHeight = 58f;
        float uiY = Gdx.graphics.getHeight() - screenY;
        float x = Math.min(screenX, Gdx.graphics.getWidth() - menuWidth - 8f);
        float y = Math.max(8f, Math.min(uiY - menuHeight, Gdx.graphics.getHeight() - MENU_BAR_HEIGHT - menuHeight));
        objectContextBounds = new com.badlogic.gdx.math.Rectangle(x, y, menuWidth, menuHeight);
        objectContextMenu = new EditorContextMenu("Object", new Color(0.95f, 0.62f, 0.12f, 1f))
                .add("Edit definition", this::openObjectDefinitionForContextTarget)
                .add("Delete", this::deleteObjectContextTarget)
                .onClose(() -> objectContextOpen = false);
        objectContextMenu.openAt(screenX, screenY, menuWidth, MENU_BAR_HEIGHT);
        objectContextOpen = true;
    }

    private boolean handleObjectContextClick(int screenX, int screenY, int button) {
        if (!objectContextOpen || objectContextMenu == null) {
            return false;
        }
        return objectContextMenu.handleClick(screenX, screenY, button);
    }

    private void openObjectDefinitionForContextTarget() {
        if (objectContextTargetIndex < 0 || objectContextTargetIndex >= objectPositions.size()) {
            return;
        }
        ObjectPos pos = objectPositions.get(objectContextTargetIndex);
        objectMappingsEditor = new ObjectMappingsEditorUI();
        objectMappingsEditor.selectByName(pos.name());
        openMenu = null;
    }

    private void deleteObjectContextTarget() {
        if (objectContextTargetIndex < 0 || objectContextTargetIndex >= objectPositions.size()) {
            return;
        }
        ObjectPos removed = objectPositions.remove(objectContextTargetIndex);
        selectedObjectPositionIndex = -1;
        objectContextTargetIndex = -1;
        objectPositionsDirty = true;
        saveObjectPositions();
        showEditorMessage("Object deleted: " + removed.name());
    }

    private void renderObjectContextMenu() {
        if (!objectContextOpen || objectContextMenu == null || objectContextTargetIndex < 0
                || objectContextTargetIndex >= objectPositions.size()) {
            return;
        }
        prepareUiProjection(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        objectContextMenu.render(uiBatch, shapeRenderer, font);
    }

    private void renderObjectPositionOverlay() {
        if (objectPositions.isEmpty()) {
            return;
        }
        int currentZ = getCurrentMapZ();
        int[] bounds = calculateVisibleBounds(1);
        boolean showAllMarkers = editorMode == EditorMode.OBJECT_POSITION_EDITOR;
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (showAllMarkers) {
            for (int i = 0; i < objectPositions.size(); i++) {
                ObjectPos pos = objectPositions.get(i);
                if (pos.z() != currentZ || pos.x() < bounds[0] || pos.x() > bounds[1] || pos.y() < bounds[2] || pos.y() > bounds[3]) {
                    continue;
                }
                boolean selected = i == selectedObjectPositionIndex;
                shapeRenderer.setColor(0.98f, 0.62f, 0.08f, selected ? 0.72f : 0.48f);
                shapeRenderer.rect(pos.x() * GameConstants.GRID_W, pos.y() * GameConstants.GRID_H,
                        GameConstants.GRID_W, GameConstants.GRID_H);
            }
        }
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.94f, 0.22f, 0.95f);
        if (showAllMarkers) {
            for (int i = 0; i < objectPositions.size(); i++) {
                ObjectPos pos = objectPositions.get(i);
                if (pos.z() != currentZ || pos.x() < bounds[0] || pos.x() > bounds[1] || pos.y() < bounds[2] || pos.y() > bounds[3]) {
                    continue;
                }
                float tileX = pos.x() * GameConstants.GRID_W;
                float tileY = pos.y() * GameConstants.GRID_H;
                float centerX = tileX + GameConstants.GRID_W * 0.5f;
                float centerY = tileY + GameConstants.GRID_H * 0.5f;
                shapeRenderer.rect(tileX, tileY, GameConstants.GRID_W, GameConstants.GRID_H);
                shapeRenderer.line(centerX - 8f, centerY, centerX + 8f, centerY);
                shapeRenderer.line(centerX, centerY - 8f, centerX, centerY + 8f);
            }
        }
        if (selectedObjectPositionIndex >= 0 && selectedObjectPositionIndex < objectPositions.size()) {
            ObjectPos pos = objectPositions.get(selectedObjectPositionIndex);
            if (pos.z() == currentZ) {
                shapeRenderer.setColor(0.1f, 0.06f, 0.02f, 1f);
                shapeRenderer.rect(pos.x() * GameConstants.GRID_W - 2f, pos.y() * GameConstants.GRID_H - 2f,
                        GameConstants.GRID_W + 4f, GameConstants.GRID_H + 4f);
                shapeRenderer.setColor(1f, 0.95f, 0.2f, 1f);
                shapeRenderer.rect(pos.x() * GameConstants.GRID_W, pos.y() * GameConstants.GRID_H,
                        GameConstants.GRID_W, GameConstants.GRID_H);
            }
        }
        if (objectDragState != null && objectDragState.targetZ == currentZ) {
            shapeRenderer.setColor(0.1f, 0.06f, 0.02f, 1f);
            shapeRenderer.rect(objectDragState.targetTileX * GameConstants.GRID_W - 2f,
                    objectDragState.targetTileY * GameConstants.GRID_H - 2f,
                    GameConstants.GRID_W + 4f, GameConstants.GRID_H + 4f);
            shapeRenderer.setColor(0.2f, 0.75f, 1f, 1f);
            shapeRenderer.rect(objectDragState.targetTileX * GameConstants.GRID_W,
                    objectDragState.targetTileY * GameConstants.GRID_H,
                    GameConstants.GRID_W, GameConstants.GRID_H);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderObjectPositionEditorPanel() {
        if (editorMode != EditorMode.OBJECT_POSITION_EDITOR) {
            return;
        }
        prepareUiProjection(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float panelWidth = 500f;
        float panelHeight = Math.min(520f, Gdx.graphics.getHeight() - MENU_BAR_HEIGHT - 28f);
        float x = Gdx.graphics.getWidth() - panelWidth - 14f;
        float y = 14f;
        objectPositionPanelBounds = new com.badlogic.gdx.math.Rectangle(x, y, panelWidth, panelHeight);
        objectPositionListBounds = new com.badlogic.gdx.math.Rectangle(x + 14f, y + 18f, panelWidth - 44f, panelHeight - 124f);
        objectPositionNewBounds = new com.badlogic.gdx.math.Rectangle(x + 18f, y + panelHeight - 98f, 66f, 28f);
        objectPositionDeleteBounds = new com.badlogic.gdx.math.Rectangle(x + 92f, y + panelHeight - 98f, 66f, 28f);
        objectPositionSaveBounds = new com.badlogic.gdx.math.Rectangle(x + 166f, y + panelHeight - 98f, 72f, 28f);
        objectPositionTypeBounds = new com.badlogic.gdx.math.Rectangle(x + 282f, y + panelHeight - 98f, 180f, 28f);
        objectPositionTypeDropdownBounds = new com.badlogic.gdx.math.Rectangle(objectPositionTypeBounds.x,
                objectPositionTypeBounds.y - 176f, objectPositionTypeBounds.width, 168f);
        objectPositionTypePreviewBounds = new com.badlogic.gdx.math.Rectangle(objectPositionTypeBounds.x + objectPositionTypeBounds.width + 10f,
                objectPositionTypeDropdownBounds.y, 40f, 40f);
        objectPositionTypeScrollTrackBounds = new com.badlogic.gdx.math.Rectangle(
                objectPositionTypeDropdownBounds.x + objectPositionTypeDropdownBounds.width - 12f,
                objectPositionTypeDropdownBounds.y + 3f, 8f, objectPositionTypeDropdownBounds.height - 6f);
        objectPositionTypeVisibleRows = Math.max(1, (int) (objectPositionTypeDropdownBounds.height / 22f));
        objectPositionTypeDropdown.setBounds(objectPositionTypeDropdownBounds.x, objectPositionTypeDropdownBounds.y,
                objectPositionTypeDropdownBounds.width, objectPositionTypeDropdownBounds.height);
        objectPositionTypeDropdown.setItems(objectPositionTypes)
                .visibleRows(objectPositionTypeVisibleRows)
                .rowHeight(22f);
        objectPositionTypeDropdown.select(Math.max(0, Math.min(selectedObjectTypeIndex, objectPositionTypes.size() - 1)));
        objectPositionTypeDropdown.setScrollOffset(objectPositionTypeScrollOffset);
        objectPositionTypeDropdown.setOpen(objectPositionTypeDropdownOpen);
        int maxTypeOffset = Math.max(0, objectPositionTypes.size() - objectPositionTypeVisibleRows);
        objectPositionTypeScrollOffset = Math.max(0, Math.min(maxTypeOffset, objectPositionTypeScrollOffset));
        float typeThumbHeight = objectPositionTypes.isEmpty() ? objectPositionTypeScrollTrackBounds.height
                : Math.max(24f, objectPositionTypeScrollTrackBounds.height
                        * (objectPositionTypeVisibleRows / (float) Math.max(objectPositionTypeVisibleRows, objectPositionTypes.size())));
        float typeThumbTravel = Math.max(0f, objectPositionTypeScrollTrackBounds.height - typeThumbHeight);
        float typeThumbY = objectPositionTypeScrollTrackBounds.y + objectPositionTypeScrollTrackBounds.height - typeThumbHeight;
        if (maxTypeOffset > 0) {
            typeThumbY -= typeThumbTravel * (objectPositionTypeScrollOffset / (float) maxTypeOffset);
        }
        objectPositionTypeScrollThumbBounds = new com.badlogic.gdx.math.Rectangle(objectPositionTypeScrollTrackBounds.x,
                typeThumbY, objectPositionTypeScrollTrackBounds.width, typeThumbHeight);
        objectPositionScrollTrackBounds = new com.badlogic.gdx.math.Rectangle(x + panelWidth - 24f,
                objectPositionListBounds.y, 10f, objectPositionListBounds.height);
        objectPositionVisibleRows = Math.max(1, (int) ((objectPositionListBounds.height - 28f) / 22f));
        int maxOffset = Math.max(0, objectPositions.size() - objectPositionVisibleRows);
        objectPositionScrollOffset = Math.max(0, Math.min(maxOffset, objectPositionScrollOffset));
        float thumbHeight = objectPositions.isEmpty() ? objectPositionScrollTrackBounds.height
                : Math.max(28f, objectPositionScrollTrackBounds.height
                        * (objectPositionVisibleRows / (float) Math.max(objectPositionVisibleRows, objectPositions.size())));
        float thumbTravel = Math.max(0f, objectPositionScrollTrackBounds.height - thumbHeight);
        float thumbY = objectPositionScrollTrackBounds.y + objectPositionScrollTrackBounds.height - thumbHeight;
        if (maxOffset > 0) {
            thumbY -= thumbTravel * (objectPositionScrollOffset / (float) maxOffset);
        }
        objectPositionScrollThumbBounds = new com.badlogic.gdx.math.Rectangle(objectPositionScrollTrackBounds.x,
                thumbY, objectPositionScrollTrackBounds.width, thumbHeight);
        int end = Math.min(objectPositions.size(), objectPositionScrollOffset + objectPositionVisibleRows);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        EditorPanelChrome.panel(shapeRenderer, objectPositionPanelBounds, EditorTheme.ORANGE, 106f);
        EditorPanelChrome.darkSurface(shapeRenderer, objectPositionListBounds);
        for (int i = objectPositionScrollOffset; i < end; i++) {
            if (i == selectedObjectPositionIndex) {
                EditorPanelChrome.selectedRow(shapeRenderer, objectPositionListBounds,
                        i - objectPositionScrollOffset + 1, 22f);
            }
        }
        EditorPanelChrome.button(shapeRenderer, objectPositionNewBounds, true, false);
        EditorPanelChrome.button(shapeRenderer, objectPositionDeleteBounds, false,
                selectedObjectPositionIndex < 0 || selectedObjectPositionIndex >= objectPositions.size());
        EditorPanelChrome.button(shapeRenderer, objectPositionSaveBounds, objectPositionsDirty, false);
        EditorPanelChrome.surface(shapeRenderer, objectPositionTypeBounds);
        EditorPanelChrome.scrollbar(shapeRenderer, objectPositionScrollTrackBounds, objectPositionScrollThumbBounds);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        EditorPanelChrome.border(shapeRenderer, objectPositionPanelBounds);
        EditorPanelChrome.border(shapeRenderer, objectPositionListBounds);
        EditorPanelChrome.border(shapeRenderer, objectPositionNewBounds);
        EditorPanelChrome.border(shapeRenderer, objectPositionDeleteBounds);
        EditorPanelChrome.border(shapeRenderer, objectPositionSaveBounds);
        EditorPanelChrome.border(shapeRenderer, objectPositionTypeBounds);
        shapeRenderer.end();

        uiBatch.begin();
        font.setColor(UI_TEXT);
        font.draw(uiBatch, "Object Positions", x + 14f, y + panelHeight - 16f);
        font.setColor(UI_TEXT_LIGHT);
        String selectedType = objectPositionTypes.isEmpty() ? "" : objectPositionTypes.get(selectedObjectTypeIndex);
        font.setColor(UI_TEXT_LIGHT);
        EditorPanelChrome.buttonText(uiBatch, font, objectPositionNewBounds, "New", UI_TEXT_LIGHT);
        font.setColor(selectedObjectPositionIndex >= 0 && selectedObjectPositionIndex < objectPositions.size()
                ? UI_TEXT_LIGHT : UI_TEXT_MUTED);
        EditorPanelChrome.buttonText(uiBatch, font, objectPositionDeleteBounds, "Del",
                selectedObjectPositionIndex >= 0 && selectedObjectPositionIndex < objectPositions.size()
                        ? UI_TEXT_LIGHT : UI_TEXT_MUTED);
        EditorPanelChrome.buttonText(uiBatch, font, objectPositionSaveBounds, "Save", UI_TEXT);
        font.setColor(UI_TEXT);
        font.draw(uiBatch, shorten(selectedType, 17), objectPositionTypeBounds.x + 8f, objectPositionTypeBounds.y + 19f);
        font.draw(uiBatch, "v", objectPositionTypeBounds.x + objectPositionTypeBounds.width - 18f,
                objectPositionTypeBounds.y + 19f);

        float rowY = objectPositionListBounds.y + objectPositionListBounds.height - 30f;
        font.setColor(UI_TEXT_FAINT);
        font.draw(uiBatch, "Objects: " + objectPositions.size() + (objectPositionsDirty ? " *" : ""), x + 14f, y + panelHeight - 42f);
        for (int i = objectPositionScrollOffset; i < end; i++) {
            ObjectPos pos = objectPositions.get(i);
            font.setColor(i == selectedObjectPositionIndex ? UI_TEXT : UI_TEXT_LIGHT);
            font.draw(uiBatch, i + "  " + pos.name() + "  (" + pos.x() + "," + pos.y() + "," + pos.z() + ")",
                    objectPositionListBounds.x + 8f, rowY);
            rowY -= 22f;
        }
        uiBatch.end();

        renderObjectPositionTypeDropdown();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderObjectPositionTypeDropdown() {
        if (!objectPositionTypeDropdownOpen || objectPositionTypeDropdownBounds == null) {
            return;
        }
        objectPositionTypeDropdown.setBounds(objectPositionTypeDropdownBounds.x, objectPositionTypeDropdownBounds.y,
                objectPositionTypeDropdownBounds.width, objectPositionTypeDropdownBounds.height);
        objectPositionTypeDropdown.setItems(objectPositionTypes)
                .visibleRows(objectPositionTypeVisibleRows)
                .rowHeight(22f);
        objectPositionTypeDropdown.select(Math.max(0, Math.min(selectedObjectTypeIndex, objectPositionTypes.size() - 1)));
        objectPositionTypeDropdown.setScrollOffset(objectPositionTypeScrollOffset);
        objectPositionTypeDropdown.setOpen(true);
        objectPositionTypeDropdown.renderDropdown(uiBatch, shapeRenderer, font);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UI_PANEL_DARK);
        shapeRenderer.rect(objectPositionTypePreviewBounds.x, objectPositionTypePreviewBounds.y,
                objectPositionTypePreviewBounds.width, objectPositionTypePreviewBounds.height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UI_BORDER);
        shapeRenderer.rect(objectPositionTypePreviewBounds.x, objectPositionTypePreviewBounds.y,
                objectPositionTypePreviewBounds.width, objectPositionTypePreviewBounds.height);
        shapeRenderer.end();

        uiBatch.begin();
        drawObjectTypePreview(uiBatch);
        uiBatch.end();
    }

    private void drawObjectTypePreview(SpriteBatch batch) {
        if (selectedObjectTypeIndex < 0 || selectedObjectTypeIndex >= objectPositionTypes.size()
                || objectPositionTypePreviewBounds == null) {
            return;
        }
        ObjectMapping mapping = currentObjectTypeMapping();
        if (mapping == null || mapping.sprite == null || mapping.sprite.isBlank()) {
            return;
        }
        String[] frames = computeObjectPreviewFrames(mapping.sprite);
        if (frames.length == 0) {
            return;
        }
        String spriteName = frames[(int) ((TimeUtils.millis() / 140L) % frames.length)];
        TextureRegion region;
        try {
            region = spriteLoader.getRegionFromSpriteName(spriteName);
        } catch (Exception e) {
            return;
        }
        if (region == null) {
            return;
        }
        float scale = Math.min(objectPositionTypePreviewBounds.width / Math.max(1f, region.getRegionWidth()),
                objectPositionTypePreviewBounds.height / Math.max(1f, region.getRegionHeight()));
        float w = region.getRegionWidth() * scale;
        float h = region.getRegionHeight() * scale;
        float px = objectPositionTypePreviewBounds.x + (objectPositionTypePreviewBounds.width - w) * 0.5f;
        float py = objectPositionTypePreviewBounds.y + (objectPositionTypePreviewBounds.height - h) * 0.5f;
        batch.draw(region, px, py, w, h);
    }

    private ObjectMapping currentObjectTypeMapping() {
        if (selectedObjectTypeIndex < 0 || selectedObjectTypeIndex >= objectPositionTypes.size()) {
            return null;
        }
        Map<String, ObjectMapping> mappings = mapRenderer != null ? mapRenderer.getObjectMappings() : ObjectMappings.load();
        return mappings.get(objectPositionTypes.get(selectedObjectTypeIndex).toUpperCase(Locale.ROOT));
    }

    private String[] computeObjectPreviewFrames(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return new String[0];
        }
        if ((pattern.contains("%d") || pattern.contains("%s")) && pattern.contains("$")) {
            int dollar = pattern.lastIndexOf('$');
            int frameCount;
            try {
                frameCount = Math.max(1, Integer.parseInt(pattern.substring(dollar + 1)));
            } catch (NumberFormatException e) {
                return new String[] { pattern };
            }
            String base = pattern.substring(0, dollar);
            String[] frames = new String[frameCount];
            for (int i = 1; i <= frameCount; i++) {
                frames[i - 1] = base.contains("%s")
                        ? base.replace("%s", String.valueOf((char) ('a' + i - 1)))
                        : base.replace("%d", String.valueOf(i));
            }
            return frames;
        }
        return new String[] { pattern };
    }


    private void renderTeleportsOverlay() {
        if (editorMode != EditorMode.TELEPORT_EDITOR || teleports.isEmpty()) {
            return;
        }
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (TeleportEntry teleport : teleports) {
            if (teleport.sourceZ != teleportEditorZ && teleport.targetZ != teleportEditorZ) {
                continue;
            }
            boolean selected = teleport == selectedTeleport;
            if (teleport.sourceZ == teleportEditorZ) {
                shapeRenderer.setColor(0.65f, 0.2f, 0.95f, selected ? 0.55f : 0.28f);
                shapeRenderer.rect(teleport.sourceX * GameConstants.GRID_W, teleport.sourceY * GameConstants.GRID_H,
                        GameConstants.GRID_W, GameConstants.GRID_H);
            }
            if (teleport.targetZ == teleportEditorZ) {
                shapeRenderer.setColor(0.15f, 0.9f, 0.45f, selected ? 0.45f : 0.22f);
                shapeRenderer.rect(teleport.targetX * GameConstants.GRID_W + 4f, teleport.targetY * GameConstants.GRID_H + 4f,
                        GameConstants.GRID_W - 8f, GameConstants.GRID_H - 8f);
            }
        }
        shapeRenderer.end();

        if (selectedTeleport != null && selectedTeleport.sourceZ == teleportEditorZ && selectedTeleport.targetZ == teleportEditorZ) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.95f, 0.95f, 0.2f, 0.85f);
            shapeRenderer.line((selectedTeleport.sourceX + 0.5f) * GameConstants.GRID_W,
                    (selectedTeleport.sourceY + 0.5f) * GameConstants.GRID_H,
                    (selectedTeleport.targetX + 0.5f) * GameConstants.GRID_W,
                    (selectedTeleport.targetY + 0.5f) * GameConstants.GRID_H);
            shapeRenderer.end();
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderTeleportEditorPanel() {
        if (editorMode != EditorMode.TELEPORT_EDITOR) {
            teleportPanelBounds = null;
            teleportListBounds = null;
            teleportSourceMapBounds = null;
            teleportTargetMapBounds = null;
            return;
        }
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        float margin = 24f;
        float width = screenWidth - margin * 2f;
        float height = screenHeight - MENU_BAR_HEIGHT - BOTTOM_INFO_HEIGHT - margin * 2f;
        float x = margin;
        float y = BOTTOM_INFO_HEIGHT + margin;
        float top = y + height;
        teleportPanelBounds = new com.badlogic.gdx.math.Rectangle(x, y, width, height);

        prepareUiProjection(screenWidth, screenHeight);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        EditorPanelChrome.overlay(shapeRenderer, screenWidth, screenHeight);
        EditorPanelChrome.panel(shapeRenderer, teleportPanelBounds, EditorTheme.BLUE, 116f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        EditorPanelChrome.border(shapeRenderer, teleportPanelBounds);
        shapeRenderer.end();

        float contentTop = y + height - 148f;
        float listWidth = Math.min(430f, Math.max(330f, width * 0.30f));
        float gap = 18f;
        float previewWidth = (width - listWidth - gap * 3f) / 2f;
        float previewHeight = Math.max(220f, contentTop - y - 46f);
        float previewSize = Math.min(previewWidth, previewHeight);
        float previewY = y + 46f;
        teleportListBounds = new com.badlogic.gdx.math.Rectangle(x + 18f, previewY, listWidth, contentTop - previewY);
        teleportSourceMapBounds = new com.badlogic.gdx.math.Rectangle(
                x + 18f + listWidth + gap, previewY, previewSize, previewSize);
        teleportTargetMapBounds = new com.badlogic.gdx.math.Rectangle(
                teleportSourceMapBounds.x + previewSize + gap, previewY, previewSize, previewSize);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        EditorPanelChrome.darkSurface(shapeRenderer, teleportListBounds);
        EditorPanelChrome.darkSurface(shapeRenderer, teleportSourceMapBounds);
        EditorPanelChrome.darkSurface(shapeRenderer, teleportTargetMapBounds);
        float buttonY = top - 104f;
        EditorPanelChrome.button(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 18f, buttonY, 64f, 26f), true, false);
        EditorPanelChrome.button(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 92f, buttonY, 64f, 26f), true, false);
        EditorPanelChrome.button(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 166f, buttonY, 72f, 26f), true, false);
        EditorPanelChrome.button(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 270f, buttonY, 30f, 26f), true, false);
        EditorPanelChrome.button(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 306f, buttonY, 30f, 26f), true, false);
        EditorPanelChrome.button(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 392f, buttonY, 30f, 26f), true, false);
        EditorPanelChrome.button(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 428f, buttonY, 30f, 26f), true, false);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        EditorPanelChrome.border(shapeRenderer, teleportListBounds);
        EditorPanelChrome.border(shapeRenderer, teleportSourceMapBounds);
        EditorPanelChrome.border(shapeRenderer, teleportTargetMapBounds);
        EditorPanelChrome.border(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 18f, buttonY, 64f, 26f));
        EditorPanelChrome.border(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 92f, buttonY, 64f, 26f));
        EditorPanelChrome.border(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 166f, buttonY, 72f, 26f));
        EditorPanelChrome.border(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 270f, buttonY, 30f, 26f));
        EditorPanelChrome.border(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 306f, buttonY, 30f, 26f));
        EditorPanelChrome.border(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 392f, buttonY, 30f, 26f));
        EditorPanelChrome.border(shapeRenderer, new com.badlogic.gdx.math.Rectangle(x + 428f, buttonY, 30f, 26f));
        shapeRenderer.end();

        uiBatch.setProjectionMatrix(uiBatch.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
        uiBatch.begin();
        font.setColor(UI_TEXT_LIGHT);
        font.draw(uiBatch, "Teleport Editor", x + 18f, top - 18f);
        font.setColor(UI_TEXT_FAINT);
        font.draw(uiBatch, "Active teleports: " + teleports.size(), x + 18f, top - 38f);
        drawTeleportButton("Add", x + 18f, buttonY, 64f);
        drawTeleportButton("Del", x + 92f, buttonY, 64f);
        drawTeleportButton("Save", x + 166f, buttonY, 72f);
        font.setColor(UI_TEXT_FAINT);
        font.setColor(UI_TEXT_LIGHT);
        font.draw(uiBatch, "Source z", x + 270f, top - 48f);
        drawTeleportButton("-", x + 270f, buttonY, 30f);
        drawTeleportButton("+", x + 306f, buttonY, 30f);
        font.setColor(UI_TEXT_LIGHT);
        font.draw(uiBatch, "Target z", x + 392f, top - 48f);
        drawTeleportButton("-", x + 392f, buttonY, 30f);
        drawTeleportButton("+", x + 428f, buttonY, 30f);

        font.setColor(UI_TEXT_LIGHT);
        font.draw(uiBatch, "Active Teleports", teleportListBounds.x + 12f,
                teleportListBounds.y + teleportListBounds.height - 12f);
        int visibleRows = Math.max(1, (int) ((teleportListBounds.height - 40f) / 22f));
        int end = Math.min(teleports.size(), teleportScrollOffset + visibleRows);
        float rowY = teleportListBounds.y + teleportListBounds.height - 38f;
        for (int i = teleportScrollOffset; i < end; i++) {
            TeleportEntry teleport = teleports.get(i);
            if (teleport == selectedTeleport) {
                uiBatch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                EditorPanelChrome.selectedRow(shapeRenderer,
                        new com.badlogic.gdx.math.Rectangle(teleportListBounds.x + 8f, rowY - 15f,
                                teleportListBounds.width - 16f, 20f));
                shapeRenderer.end();
                uiBatch.begin();
            }
            font.setColor(teleport == selectedTeleport ? UI_TEXT : UI_TEXT_LIGHT);
            String label = String.format(Locale.ROOT, "#%d  (%d,%d,z%d) -> (%d,%d,z%d)",
                    teleport.id, teleport.sourceX, teleport.sourceY, teleport.sourceZ,
                    teleport.targetX, teleport.targetY, teleport.targetZ);
            font.draw(uiBatch, ellipsizeToWidth(label, teleportListBounds.width - 26f), teleportListBounds.x + 14f, rowY);
            rowY -= 22f;
        }
        uiBatch.end();
        renderTeleportListScrollbar(visibleRows);
        uiBatch.begin();
        if (selectedTeleport != null) {
            font.setColor(UI_TEXT_LIGHT);
            font.draw(uiBatch, "Source: " + mapLabelForZ(selectedTeleport.sourceZ),
                    teleportSourceMapBounds.x, teleportSourceMapBounds.y + teleportSourceMapBounds.height + 22f);
            font.draw(uiBatch, "Destination: " + mapLabelForZ(selectedTeleport.targetZ),
                    teleportTargetMapBounds.x, teleportTargetMapBounds.y + teleportTargetMapBounds.height + 22f);
            font.setColor(UI_TEXT_FAINT);
            font.draw(uiBatch, "Click a map preview to move its point. Esc closes.",
                    x + 18f, y + 22f);
        } else {
            font.setColor(UI_TEXT_FAINT);
            font.draw(uiBatch, "Select a teleport or add a new one.", x + 18f, y + 22f);
        }
        uiBatch.end();

        if (selectedTeleport != null) {
            renderTeleportMapPreview(teleportSourceMapBounds, selectedTeleport.sourceZ,
                    selectedTeleport.sourceX, selectedTeleport.sourceY, false);
            renderTeleportMapPreview(teleportTargetMapBounds, selectedTeleport.targetZ,
                    selectedTeleport.targetX, selectedTeleport.targetY, true);
            renderTeleportPathBetweenPreviews();
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private String mapLabelForZ(int z) {
        MapDefinition def = MapDefinition.fromZ(z);
        return getMapDisplayName(def.getMapPath()) + " (z" + z + ")";
    }

    private void renderTeleportListScrollbar(int visibleRows) {
        if (teleportListBounds == null || teleports.size() <= visibleRows) {
            return;
        }
        float trackX = teleportListBounds.x + teleportListBounds.width - 8f;
        float trackY = teleportListBounds.y + 8f;
        float trackHeight = teleportListBounds.height - 16f;
        float thumbHeight = Math.max(24f, trackHeight * visibleRows / (float) teleports.size());
        int maxOffset = Math.max(1, teleports.size() - visibleRows);
        float thumbY = trackY + (trackHeight - thumbHeight)
                * (1f - teleportScrollOffset / (float) maxOffset);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        EditorPanelChrome.scrollbar(shapeRenderer,
                new com.badlogic.gdx.math.Rectangle(trackX, trackY, 4f, trackHeight),
                new com.badlogic.gdx.math.Rectangle(trackX - 1f, thumbY, 6f, thumbHeight));
        shapeRenderer.end();
    }

    private void renderTeleportMapPreview(com.badlogic.gdx.math.Rectangle bounds, int z, int tileX, int tileY, boolean destination) {
        Texture texture = getTeleportPreviewTexture(z);
        if (texture != null) {
            float zoom = destination ? teleportTargetPreviewZoom : teleportSourcePreviewZoom;
            float centerX = destination ? teleportTargetPreviewCenterX : teleportSourcePreviewCenterX;
            float centerY = destination ? teleportTargetPreviewCenterY : teleportSourcePreviewCenterY;
            float visible = 1f / zoom;
            float minX = clampFloat(centerX - visible / 2f, 0f, 1f - visible);
            float minY = clampFloat(centerY - visible / 2f, 0f, 1f - visible);
            int srcX = Math.max(0, Math.min(texture.getWidth() - 1, Math.round(minX * texture.getWidth())));
            int srcY = Math.max(0, Math.min(texture.getHeight() - 1, Math.round(minY * texture.getHeight())));
            int srcW = Math.max(1, Math.min(texture.getWidth() - srcX, Math.round(visible * texture.getWidth())));
            int srcH = Math.max(1, Math.min(texture.getHeight() - srcY, Math.round(visible * texture.getHeight())));
            uiBatch.begin();
            uiBatch.setColor(1f, 1f, 1f, 1f);
            uiBatch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height, srcX, srcY, srcW, srcH, false, false);
            uiBatch.end();
        }

        MapDefinition mapDefinition = MapDefinition.fromZ(z);
        int mapWidth = getPreviewMapWidth(mapDefinition);
        int mapHeight = getPreviewMapHeight(mapDefinition);
        if (mapWidth <= 0 || mapHeight <= 0) {
            return;
        }
        float px = pointXInPreview(bounds, z, tileX, !destination ? true : false);
        float py = pointYInPreview(bounds, z, tileY, !destination ? true : false);
        if (px < bounds.x || px > bounds.x + bounds.width || py < bounds.y || py > bounds.y + bounds.height) {
            return;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(destination ? new Color(0.1f, 0.95f, 0.42f, 0.85f) : new Color(0.72f, 0.25f, 0.98f, 0.85f));
        shapeRenderer.circle(px, py, 6f);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(px, py, 9f);
        shapeRenderer.end();
    }

    private void renderTeleportPathBetweenPreviews() {
        if (selectedTeleport == null || teleportSourceMapBounds == null || teleportTargetMapBounds == null) {
            return;
        }
        float sx = pointXInPreview(teleportSourceMapBounds, selectedTeleport.sourceZ, selectedTeleport.sourceX);
        float sy = pointYInPreview(teleportSourceMapBounds, selectedTeleport.sourceZ, selectedTeleport.sourceY);
        float tx = pointXInPreview(teleportTargetMapBounds, selectedTeleport.targetZ, selectedTeleport.targetX);
        float ty = pointYInPreview(teleportTargetMapBounds, selectedTeleport.targetZ, selectedTeleport.targetY);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.95f, 0.9f, 0.18f, 0.95f);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.line(sx, sy, tx, ty);
        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();

        float angle = (float) Math.atan2(ty - sy, tx - sx);
        float arrowLength = 18f;
        float arrowWidth = 9f;
        float bx = tx - (float) Math.cos(angle) * arrowLength;
        float by = ty - (float) Math.sin(angle) * arrowLength;
        float nx = -(float) Math.sin(angle) * arrowWidth;
        float ny = (float) Math.cos(angle) * arrowWidth;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.95f, 0.9f, 0.18f, 0.95f);
        shapeRenderer.triangle(tx, ty, bx + nx, by + ny, bx - nx, by - ny);
        shapeRenderer.end();
    }

    private float pointXInPreview(com.badlogic.gdx.math.Rectangle bounds, int z, int tileX) {
        boolean source = selectedTeleport != null && bounds == teleportSourceMapBounds && z == selectedTeleport.sourceZ;
        return pointXInPreview(bounds, z, tileX, source);
    }

    private float pointXInPreview(com.badlogic.gdx.math.Rectangle bounds, int z, int tileX, boolean source) {
        int width = Math.max(1, getPreviewMapWidth(MapDefinition.fromZ(z)));
        float normalized = (tileX + 0.5f) / width;
        float visible = 1f / (source ? teleportSourcePreviewZoom : teleportTargetPreviewZoom);
        float center = source ? teleportSourcePreviewCenterX : teleportTargetPreviewCenterX;
        float min = clampFloat(center - visible / 2f, 0f, 1f - visible);
        return bounds.x + ((normalized - min) / visible) * bounds.width;
    }

    private float pointYInPreview(com.badlogic.gdx.math.Rectangle bounds, int z, int tileY) {
        boolean source = selectedTeleport != null && bounds == teleportSourceMapBounds && z == selectedTeleport.sourceZ;
        return pointYInPreview(bounds, z, tileY, source);
    }

    private float pointYInPreview(com.badlogic.gdx.math.Rectangle bounds, int z, int tileY, boolean source) {
        int height = Math.max(1, getPreviewMapHeight(MapDefinition.fromZ(z)));
        float normalized = (tileY + 0.5f) / height;
        float visible = 1f / (source ? teleportSourcePreviewZoom : teleportTargetPreviewZoom);
        float center = source ? teleportSourcePreviewCenterY : teleportTargetPreviewCenterY;
        float min = clampFloat(center - visible / 2f, 0f, 1f - visible);
        return bounds.y + ((normalized - min) / visible) * bounds.height;
    }

    private Texture getTeleportPreviewTexture(int z) {
        Texture cached = teleportPreviewTextures.get(z);
        if (cached != null) {
            return cached;
        }
        MapDefinition def = MapDefinition.fromZ(z);
        try (MapReader reader = new MapReader(new File(def.getMapPath()))) {
            teleportPreviewDimensions.put(def.getZ(), new int[] { reader.getWidth(), reader.getHeight() });
            Pixmap pixmap = createMapPreviewPixmap(reader, 260);
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            teleportPreviewTextures.put(z, texture);
            return texture;
        } catch (Exception e) {
            log.warn("Failed to create teleport map preview for z{}", z, e);
            return null;
        }
    }

    private Pixmap createMapPreviewPixmap(MapReader reader, int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        int mapWidth = reader.getWidth();
        int mapHeight = reader.getHeight();
        float tilesPerPixelX = mapWidth / (float) size;
        float tilesPerPixelY = mapHeight / (float) size;
        for (int py = 0; py < size; py++) {
            int startY = Math.max(0, Math.min(mapHeight - 1, (int) Math.floor(py * tilesPerPixelY)));
            int endY = Math.max(startY, Math.min(mapHeight - 1, (int) Math.ceil((py + 1) * tilesPerPixelY) - 1));
            for (int px = 0; px < size; px++) {
                int startX = Math.max(0, Math.min(mapWidth - 1, (int) Math.floor(px * tilesPerPixelX)));
                int endX = Math.max(startX, Math.min(mapWidth - 1, (int) Math.ceil((px + 1) * tilesPerPixelX) - 1));
                int bestColor = minimapColorForSprite(null);
                int bestPriority = Integer.MIN_VALUE;
                for (int y = startY; y <= endY; y++) {
                    for (int x = startX; x <= endX; x++) {
                        String sprite = reader.getSpriteName(x, y);
                        int priority = minimapColorPriority(sprite);
                        if (priority > bestPriority) {
                            bestPriority = priority;
                            bestColor = minimapColorForSprite(sprite);
                        }
                    }
                }
                pixmap.drawPixel(px, py, bestColor);
            }
        }
        return pixmap;
    }

    private void drawTeleportButton(String text, float x, float y, float width) {
        EditorPanelChrome.buttonText(uiBatch, font, new com.badlogic.gdx.math.Rectangle(x, y, width, 26f), text, UI_TEXT_LIGHT);
    }

    private void addLoadedMusicZone(String name, int x1, int y1, int x2, int y2, String music) {
        if (music == null || music.isBlank()) {
            return;
        }
        int minX = Math.min(clampTileX(x1), clampTileX(x2));
        int maxX = Math.max(clampTileX(x1), clampTileX(x2));
        int minY = Math.min(clampTileY(y1), clampTileY(y2));
        int maxY = Math.max(clampTileY(y1), clampTileY(y2));
        if (minX <= maxX && minY <= maxY) {
            musicZones.add(new MusicZoneEntry(
                    name == null || name.isBlank() ? music : name,
                    minX, minY, maxX, maxY, music));
        }
    }

    private List<MusicZoneBinaryIO.Entry> toMusicZoneBinaryEntries(List<MusicZoneEntry> zones) {
        List<MusicZoneBinaryIO.Entry> entries = new ArrayList<>(zones.size());
        for (MusicZoneEntry zone : zones) {
            MusicZoneBinaryIO.Entry entry = new MusicZoneBinaryIO.Entry();
            entry.name = zone.name;
            entry.x1 = zone.x1;
            entry.y1 = zone.y1;
            entry.x2 = zone.x2;
            entry.y2 = zone.y2;
            entry.music = zone.music;
            entries.add(entry);
        }
        return entries;
    }

    private List<MusicZoneEntry> buildCompressedMusicZones() {
        if (mapReader == null) {
            return new ArrayList<>(musicZones);
        }
        int width = mapReader.getWidth();
        int height = mapReader.getHeight();
        byte[] musicByTile = new byte[width * height];
        Arrays.fill(musicByTile, (byte) -1);
        for (MusicZoneEntry zone : musicZones) {
            if (zone == null || zone.music == null) {
                continue;
            }
            int musicIndex = ambientMusicIndex(zone.music);
            if (musicIndex < 0) {
                continue;
            }
            int x1 = Math.max(0, zone.x1);
            int x2 = Math.min(width - 1, zone.x2);
            int y1 = Math.max(0, zone.y1);
            int y2 = Math.min(height - 1, zone.y2);
            for (int y = y1; y <= y2; y++) {
                Arrays.fill(musicByTile, y * width + x1, y * width + x2 + 1, (byte) musicIndex);
            }
        }

        List<MusicZoneEntry> compressed = new ArrayList<>();
        Map<String, MusicZoneEntry> previousRow = new LinkedHashMap<>();
        int[] counters = new int[AMBIENT_MUSIC_TYPES.length];
        for (int y = 0; y < height; y++) {
            Map<String, MusicZoneEntry> currentRow = new LinkedHashMap<>();
            int x = 0;
            while (x < width) {
                int musicIndex = musicByTile[y * width + x];
                int x1 = x++;
                while (x < width && musicByTile[y * width + x] == musicIndex) {
                    x++;
                }
                if (musicIndex < 0) {
                    continue;
                }
                int x2 = x - 1;
                String key = musicIndex + ":" + x1 + ":" + x2;
                MusicZoneEntry rectangle = previousRow.remove(key);
                if (rectangle == null) {
                    String music = AMBIENT_MUSIC_TYPES[musicIndex];
                    rectangle = new MusicZoneEntry(
                            music + " " + ++counters[musicIndex],
                            x1, y, x2, y, music);
                }
                rectangle.y2 = y;
                currentRow.put(key, rectangle);
            }
            compressed.addAll(previousRow.values());
            previousRow = currentRow;
        }
        compressed.addAll(previousRow.values());
        return compressed;
    }

    private int ambientMusicIndex(String music) {
        for (int i = 0; i < AMBIENT_MUSIC_TYPES.length; i++) {
            if (AMBIENT_MUSIC_TYPES[i].equals(music)) {
                return i;
            }
        }
        return -1;
    }

    private long packTileKey(int x, int y) {
        return ((long) x << 32) ^ (y & 0xffffffffL);
    }

    // ==================== COLLISION EDITOR METHODS ====================

    /**
     * Toggle collision editor mode
     */
    private void toggleCollisionEditor() {
        if (editorMode == EditorMode.COLLISION_EDITOR) {
            editorMode = EditorMode.SELECT_TILE;
            collisionOverlayVisible = false;
            if (collisionData != null) {
                saveCollisionData();
            }
            showEditorMessage("Collision Editor: OFF");
        } else {
            editorMode = EditorMode.COLLISION_EDITOR;
            loadCollisionData();
            collisionOverlayVisible = true;
            showEditorMessage("Collision Editor: ON (0-9 or [ ] select type, left paints, right erases)");
        }
    }

    private void selectCollisionValue(int value) {
        selectedCollisionValue = value & 0x0F;
        CollisionType type = CollisionType.fromValue(selectedCollisionValue);
        showEditorMessage("Collision " + type.getValue() + ": " + type.getDisplayName());
    }

    private String getCurrentToolLabel() {
        if (dragState != null) {
            return "Move decor";
        }
        if (editorMode == EditorMode.COLLISION_EDITOR) {
            CollisionType type = CollisionType.fromValue(selectedCollisionValue);
            return "Collision " + type.getValue() + ": " + type.getDisplayName();
        }
        if (editorMode == EditorMode.MUSIC_ZONE_EDITOR) {
            return "Music zones";
        }
        if (editorMode == EditorMode.COLLISION_EDITOR) {
            CollisionType type = CollisionType.fromValue(selectedCollisionValue);
            return "Collision type " + type.getValue() + " - " + type.getDisplayName()
                    + " | left paints, right erases, middle picks, 0-9 selects, [ ] cycles";
        }
        if (editorMode == EditorMode.SPRITE_PICKER) {
            return "Sprite picker";
        }
        if (fillToolEnabled && autofillEnabled) {
            return "Ground fill";
        }
        if (scaleToolEnabled) {
            return "Scale tool";
        }
        return "Map editor";
    }

    private Color getCurrentToolAccent() {
        if (editorMode == EditorMode.COLLISION_EDITOR) {
            return new Color(0.95f, 0.25f, 0.38f, 1f);
        }
        if (editorMode == EditorMode.MUSIC_ZONE_EDITOR) {
            return new Color(0.42f, 0.55f, 0.95f, 1f);
        }
        if (fillToolEnabled && autofillEnabled) {
            return new Color(0.25f, 0.75f, 0.95f, 1f);
        }
        if (scaleToolEnabled) {
            return new Color(0.28f, 0.95f, 0.45f, 1f);
        }
        return new Color(0.7f, 0.75f, 0.82f, 1f);
    }

    private String getSaveStateLabel() {
        if (mapDirty && collisionDirty) {
            return "Map + collision modified";
        }
        if (mapDirty) {
            return "Map modified";
        }
        if (collisionDirty) {
            return "Collision modified";
        }
        if (musicZonesDirty) {
            return "Music zones modified";
        }
        return "Saved";
    }

    private String shorten(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        if (maxChars <= 3) {
            return value.substring(0, maxChars);
        }
        return value.substring(0, maxChars - 3) + "...";
    }

    private String ellipsizeToWidth(String value, float maxWidth) {
        if (value == null || value.isBlank() || maxWidth <= 0f) {
            return value;
        }
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, value);
        if (layout.width <= maxWidth) {
            return value;
        }
        String suffix = "...";
        layout.setText(font, suffix);
        float suffixWidth = layout.width;
        if (suffixWidth >= maxWidth) {
            return suffix;
        }
        int end = value.length();
        while (end > 0) {
            String candidate = value.substring(0, end).trim();
            layout.setText(font, candidate);
            if (layout.width + suffixWidth <= maxWidth) {
                return candidate + suffix;
            }
            end--;
        }
        return suffix;
    }

    private void renderContextBar() {
        if (mapReader == null) {
            return;
        }

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        if (screenWidth < 520) {
            return;
        }

        float barX = PANEL_MARGIN;
        float barY = PANEL_MARGIN * 0.5f;
        float barWidth = screenWidth - PANEL_MARGIN * 2f;

        prepareUiProjection(screenWidth, screenHeight);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UI_SURFACE);
        shapeRenderer.rect(barX, barY, barWidth, CONTEXT_BAR_HEIGHT);
        shapeRenderer.setColor(getCurrentToolAccent());
        shapeRenderer.rect(barX, barY, 3f, CONTEXT_BAR_HEIGHT);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UI_BORDER);
        shapeRenderer.rect(barX, barY, barWidth, CONTEXT_BAR_HEIGHT);
        shapeRenderer.end();

        uiBatch.begin();
        font.setColor(UI_TEXT);
        String contextText = buildContextText();
        String coordsText = getHoveredTileCoordinatesLabel();
        float contextTextX = barX + 12f;
        float contextMaxWidth = barWidth - 24f;
        if (coordsText != null) {
            GlyphLayout coordsLayout = new GlyphLayout(font, coordsText);
            float coordsX = barX + barWidth - coordsLayout.width - 12f;
            font.setColor(UI_TEXT_MUTED);
            font.draw(uiBatch, coordsText, coordsX, barY + 22f);
            font.setColor(UI_TEXT);
            contextMaxWidth = Math.max(24f, coordsX - contextTextX - 16f);
        }
        font.draw(uiBatch, ellipsizeToWidth(contextText, contextMaxWidth), contextTextX, barY + 22f);
        font.setColor(Color.WHITE);
        uiBatch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private String getHoveredTileCoordinatesLabel() {
        if (hoveredTileX < 0 || hoveredTileY < 0) {
            return null;
        }
        return "Tile: " + hoveredTileX + ", " + hoveredTileY;
    }

    private String buildContextText() {
        if (dragState != null) {
            return "Moving " + dragState.spriteName + " to " + dragState.targetTileX + ", " + dragState.targetTileY;
        }
        if (editorMode == EditorMode.MUSIC_ZONE_EDITOR) {
            String music = getSelectedAmbientMusic();
            if (selectedMusicZone != null) {
                return "Music zone: " + selectedMusicZone.music + " | " + selectedMusicZone.x1 + ","
                        + selectedMusicZone.y1 + " -> " + selectedMusicZone.x2 + "," + selectedMusicZone.y2
                        + " | Delete removes it";
            }
            return "Music brush | " + music + " | size " + (musicBrushRadius * 2 + 1)
                    + " | left paints, right erases, wheel changes music, Shift+wheel changes size";
        }
        if (rectangleSelection != null) {
            if (!lassoSelectionTiles.isEmpty()) {
                return "Lasso selection " + lassoSelectionTiles.size() + " tile"
                        + (lassoSelectionTiles.size() == 1 ? "" : "s");
            }
            return "Selection " + rectangleSelection.getWidth() + "x" + rectangleSelection.getHeight();
        }
        // Hover info takes priority so the tooltip keeps updating even with a copied tile pending.
        if (hoverInfo != null && !hoverInfo.isBlank()) {
            return hoverInfo;
        }
        if (copiedTile != null) {
            return "Copied " + copiedTile.spriteName;
        }
        return getCurrentToolLabel() + " | " + getSaveStateLabel();
    }

    private void prepareUiProjection(int screenWidth, int screenHeight) {
        com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
        uiMatrix.setToOrtho2D(0, 0, screenWidth, screenHeight);
        shapeRenderer.setProjectionMatrix(uiMatrix);
        uiBatch.setProjectionMatrix(uiMatrix);
    }

    /**
     * Load collision data from file or create new
     */
    private void loadCollisionData() {
        File collisionFile = new File(getCollisionMapPath());

        if (collisionFile.exists()) {
            try {
                CollisionMapIO.CollisionMap collisionMap = CollisionMapIO.read(collisionFile);
                collisionMapWidth = collisionMap.getWidth();
                collisionMapHeight = collisionMap.getHeight();
                collisionData = collisionMap.getData();
                if (collisionMapWidth != mapReader.getWidth() || collisionMapHeight != mapReader.getHeight()) {
                    throw new IOException("Collision dimensions do not match map dimensions");
                }
                showEditorMessage("Loaded collision data: " + collisionMapWidth + "x" + collisionMapHeight);
                log.info("Loaded collision map: {}x{}", collisionMapWidth, collisionMapHeight);
            } catch (IOException e) {
                log.warn("Could not load collision data: {}", e.getMessage());
                createNewCollisionData();
            }
        } else {
            createNewCollisionData();
        }
    }

    /**
     * Create new collision data from map dimensions
     */
    private void createNewCollisionData() {
        collisionMapWidth = mapReader.getWidth();
        collisionMapHeight = mapReader.getHeight();
        collisionData = new byte[collisionMapWidth * collisionMapHeight];
        Arrays.fill(collisionData, (byte) 0); // Default: no collision
        collisionDirty = true;

        showEditorMessage("Created new collision map: " + collisionMapWidth + "x" + collisionMapHeight);
        log.info("Created new collision map: {}x{}", collisionMapWidth, collisionMapHeight);
    }

    private void generateCollisionFromDecors() {
        if (mapReader == null) {
            return;
        }
        collisionMapWidth = mapReader.getWidth();
        collisionMapHeight = mapReader.getHeight();
        CollisionRules rules = loadCollisionRules();
        Map<String, SpriteLoader.Sprite> metaByName = buildCollisionMetaByName();
        CollisionGenerationResult result = buildCollisionDataFromRules(collisionMapWidth, collisionMapHeight, rules, metaByName);
        collisionData = result.data;
        showEditorMessage("Generated collisions from rules: " + result.count);
        log.info("Generated collision map from {}: {}", Paths.COLLISION_RULES_BIN, result.count);
        collisionDirty = true;
    }

    private CollisionGenerationResult buildCollisionDataFromRules(int width, int height, CollisionRules rules,
                                                                  Map<String, SpriteLoader.Sprite> metaByName) {
        return buildCollisionDataFromRules(width, height, rules, metaByName, false);
    }

    private CollisionGenerationResult buildCollisionDataFromRules(int width, int height, CollisionRules rules,
                                                                  Map<String, SpriteLoader.Sprite> metaByName,
                                                                  boolean reportProgress) {
        byte[] generated = new byte[width * height];
        Set<String> ignoredSprites = rules.normalizedIgnoredSprites();
        Map<String, CollisionRule> exactRules = rules.normalizedExactSprites();
        int count = 0;
        if (reportProgress) {
            collisionRegenerationProgress = 0f;
            collisionRegenerationStage = "Protecting TMPL edges next to water...";
        }
        // Must run first so decor/object no-collision tiles (e.g. Bridge2) can override it below.
        count += applyWaterAdjacentTmplCollisions(generated, width, height, metaByName);

        if (reportProgress) {
            collisionRegenerationStage = "Scanning map...";
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ResolvedSprite resolved = resolveSpriteAt(x, y, metaByName);
                if (resolved == null || resolved.name == null) {
                    continue;
                }
                String resolvedName = normalizeSpriteKey(resolved.name);
                int originX = x;
                int originY = y;
                SpriteLoader.Sprite meta = metaByName.get(resolved.name.toLowerCase(Locale.ROOT));
                if (meta != null) {
                    originX += Math.round(mapReader.getOffsetXFast(x, y) / GameConstants.GRID_W);
                    originY += Math.round((mapReader.getOffsetYFast(x, y) + meta.getHeight()
                            - GameConstants.GRID_H) / GameConstants.GRID_H);
                }
                if (resolved.mirror) {
                    int[] shift = getMirrorCollisionOriginShift(resolved.name, metaByName);
                    originX += shift[0];
                    originY += shift[1];
                }
                if (rules.isIgnoredSprite(resolvedName, ignoredSprites)) {
                    continue;
                }

                CollisionRule exactRule = exactRules.get(resolvedName);
                if (exactRule != null) {
                    count += applyCollisionRule(generated, width, height, originX, originY, exactRule, resolved.mirror);
                    continue;
                }

                boolean matchedNamedRule = false;
                if (rules.nameContainsRules != null) {
                    for (CollisionNameRule nameRule : rules.nameContainsRules) {
                        if (nameRule != null && nameRule.matches(resolvedName)) {
                            count += applyCollisionRule(generated, width, height, originX, originY, nameRule.rule, resolved.mirror);
                            matchedNamedRule = true;
                        }
                    }
                }
                if (matchedNamedRule) {
                    continue;
                }

            }
            if (reportProgress) {
                collisionRegenerationProgress = height == 0 ? 0.95f : ((y + 1) / (float) height) * 0.95f;
                collisionRegenerationStage = "Scanning map (" + (y + 1) + "/" + height + " rows)...";
            }
        }

        // Apply rules on WDA objects (objects/decor layer), not only on map tile names.
        if (mapRenderer != null && mapRenderer.getObjectPositions() != null) {
            Map<String, ObjectMapping> objectMappings = mapRenderer.getObjectMappings();
            List<ObjectPos> positions = mapRenderer.getObjectPositions();
            int processedObjects = 0;
            for (ObjectPos pos : positions) {
                processedObjects++;
                if (pos == null) {
                    updateCollisionObjectProgress(reportProgress, processedObjects, positions.size());
                    continue;
                }
                int x = (int) pos.x();
                int y = (int) pos.y();
                if (x < 0 || x >= width || y < 0 || y >= height) {
                    updateCollisionObjectProgress(reportProgress, processedObjects, positions.size());
                    continue;
                }

                String objectName = pos.name();
                String mappedSpriteName = null;
                if (objectMappings != null && objectName != null) {
                    ObjectMapping mapping = objectMappings.get(objectName.toUpperCase(Locale.ROOT));
                    if (mapping != null) {
                        mappedSpriteName = mapping.sprite;
                    }
                }

                count += applyRulesForSpriteNameAt(generated, width, height, x, y, objectName, false, rules, ignoredSprites, exactRules, metaByName);
                count += applyRulesForSpriteNameAt(generated, width, height, x, y, mappedSpriteName, false, rules, ignoredSprites, exactRules, metaByName);
                updateCollisionObjectProgress(reportProgress, processedObjects, positions.size());
            }
        }
        if (reportProgress) {
            collisionRegenerationProgress = 0.99f;
            collisionRegenerationStage = "Finalizing collisions...";
        }

        return new CollisionGenerationResult(generated, count);
    }

    /** Water cannot be walked into through a smoothing-template border tile. */
    private int applyWaterAdjacentTmplCollisions(byte[] generated, int width, int height,
                                                  Map<String, SpriteLoader.Sprite> metaByName) {
        int added = 0;
        int[][] cardinalNeighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String groundName = resolvedGroundNameAt(x, y, metaByName);
                if (groundName == null || !groundName.trim().toLowerCase(Locale.ROOT).startsWith("tmpl")) continue;
                for (int[] offset : cardinalNeighbors) {
                    int nx = x + offset[0];
                    int ny = y + offset[1];
                    if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                    String neighborGround = resolvedGroundNameAt(nx, ny, metaByName);
                    if (neighborGround != null && normalizeSpriteKey(neighborGround).contains("water")) {
                        added += setCollisionIfEmpty(generated, width, height, x, y, COLLISION_VALUE_RED);
                        break;
                    }
                }
            }
        }
        return added;
    }

    private String resolvedGroundNameAt(int x, int y, Map<String, SpriteLoader.Sprite> metaByName) {
        String rawName = mapReader.getGroundSpriteName(x, y);
        ResolvedSprite resolved = SpriteNameParser.parse(rawName, metaByName);
        return resolved == null ? rawName : resolved.name;
    }

    private void updateCollisionObjectProgress(boolean reportProgress, int processed, int total) {
        if (!reportProgress) {
            return;
        }
        float fraction = total == 0 ? 1f : processed / (float) total;
        collisionRegenerationProgress = 0.95f + fraction * 0.04f;
        collisionRegenerationStage = "Scanning objects (" + processed + "/" + total + ")...";
    }

    private int applyRulesForSpriteNameAt(byte[] generated, int width, int height, int x, int y, String sourceName, boolean mirrored,
                                          CollisionRules rules, Set<String> ignoredSprites,
                                          Map<String, CollisionRule> exactRules, Map<String, SpriteLoader.Sprite> metaByName) {
        if (sourceName == null || sourceName.isBlank()) {
            return 0;
        }
        String resolvedName = normalizeSpriteKey(sourceName);
        if (rules.isIgnoredSprite(resolvedName, ignoredSprites)) {
            return 0;
        }

        CollisionRule exactRule = exactRules.get(resolvedName);
        if (exactRule != null) {
            return applyCollisionRule(generated, width, height, x, y, exactRule, mirrored);
        }

        int count = 0;
        boolean matchedNamedRule = false;
        if (rules.nameContainsRules != null) {
            for (CollisionNameRule nameRule : rules.nameContainsRules) {
                if (nameRule != null && nameRule.matches(resolvedName)) {
                    count += applyCollisionRule(generated, width, height, x, y, nameRule.rule, mirrored);
                    matchedNamedRule = true;
                }
            }
        }
        if (matchedNamedRule) {
            return count;
        }

        return count;
    }

    private static String normalizeSpriteKey(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .replaceAll("\\s+", " ");
        return normalized;
    }

    private static String compactSpriteKey(String value) {
        return value == null ? "" : value.replace(" ", "");
    }

    private Map<String, SpriteLoader.Sprite> buildCollisionMetaByName() {
        Map<String, SpriteLoader.Sprite> metaByName = new HashMap<>();
        if (mapRenderer != null && mapRenderer.getMetaByName() != null) {
            metaByName.putAll(mapRenderer.getMetaByName());
        }
        for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
            if (sprite.getName() != null) {
                metaByName.putIfAbsent(sprite.getName().toLowerCase(Locale.ROOT), sprite);
            }
        }
        return metaByName;
    }

    private ResolvedSprite resolveSpriteAt(int x, int y, Map<String, SpriteLoader.Sprite> metaByName) {
        String name = mapReader.getSpriteName(x, y);
        return SpriteNameParser.parse(name, metaByName);
    }

    private int[] getMirrorCollisionOriginShift(String spriteName, Map<String, SpriteLoader.Sprite> metaByName) {
        if (spriteName == null || metaByName == null) {
            return new int[] { 0, 0 };
        }
        SpriteLoader.Sprite meta = metaByName.get(spriteName.toLowerCase(Locale.ROOT));
        if (meta == null) {
            return new int[] { 0, 0 };
        }
        int dx = Math.round((meta.getDrawOffset2X() - meta.getDrawOffset1X()) / (float) GameConstants.GRID_W);
        int dy = Math.round((meta.getDrawOffset2Y() - meta.getDrawOffset1Y()) / (float) GameConstants.GRID_H);
        return new int[] { dx, dy };
    }

    private CollisionRules loadCollisionRules() {
        File binaryRulesFile = new File(Paths.COLLISION_RULES_BIN);
        if (binaryRulesFile.exists()) {
            try {
                return loadCollisionRulesBinary(binaryRulesFile);
            } catch (Exception e) {
                log.warn("Could not load binary collision rules from {}: {}. Using empty collision rules.",
                        binaryRulesFile.getPath(), e.getMessage());
                return new CollisionRules();
            }
        }

        log.warn("Collision rules binary file not found: {}. Using empty collision rules.",
                binaryRulesFile.getPath());
        return new CollisionRules();
    }

    private int applyCollisionRule(byte[] data, int width, int height, int originX, int originY, CollisionRule rule, boolean mirrored) {
        if (rule == null) {
            return 0;
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        if (mirrored) {
            for (int[] tile : rule.normalizedTiles()) {
                if (tile != null && tile.length >= 2) {
                    minX = Math.min(minX, tile[0]);
                    maxX = Math.max(maxX, tile[0]);
                }
            }
            for (int[] tile : rule.normalizedClearTiles()) {
                if (tile != null && tile.length >= 2) {
                    minX = Math.min(minX, tile[0]);
                    maxX = Math.max(maxX, tile[0]);
                }
            }
            if (minX == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE) {
                mirrored = false;
            }
        }
        int count = 0;
        for (int[] tile : rule.normalizedTiles()) {
            if (tile == null || tile.length < 2) {
                continue;
            }
            int dx = mirrored ? (minX + maxX - tile[0]) : tile[0];
            count += setCollisionIfEmpty(data, width, height, originX + dx, originY + tile[1], rule.value);
        }
        for (int[] tile : rule.normalizedClearTiles()) {
            if (tile == null || tile.length < 2) {
                continue;
            }
            int dx = mirrored ? (minX + maxX - tile[0]) : tile[0];
            clearCollision(data, width, height, originX + dx, originY + tile[1]);
        }
        return count;
    }

    private int setCollisionIfEmpty(byte[] data, int width, int height, int x, int y, int value) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 0;
        }
        int index = y * width + x;
        if (data[index] != 0) {
            return 0;
        }
        data[index] = (byte) (value <= 0 ? COLLISION_VALUE_RED : value);
        return 1;
    }

    private void clearCollision(byte[] data, int width, int height, int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        int index = y * width + x;
        data[index] = 0;
    }

    private void regenerateCollisionsFromRulesAsync() {
        if (collisionRegenerating) {
            showEditorMessage("Collision regeneration already running");
            return;
        }
        if (mapReader == null) {
            showEditorMessage("No map loaded");
            return;
        }

        collisionRegenerating = true;
        collisionRegenerationProgress = 0f;
        collisionRegenerationStage = "Preparing collision rules...";
        collisionMapWidth = mapReader.getWidth();
        collisionMapHeight = mapReader.getHeight();
        collisionData = new byte[collisionMapWidth * collisionMapHeight];
        collisionDirty = true;
        showEditorMessage("Cleared collisions, regenerating from collision_rules.bin...");

        int width = collisionMapWidth;
        int height = collisionMapHeight;
        CollisionRules rules = loadCollisionRules();
        Map<String, SpriteLoader.Sprite> metaByName = buildCollisionMetaByName();

        collisionGenerationExecutor.submit(() -> {
            try {
                CollisionGenerationResult result = buildCollisionDataFromRules(width, height, rules, metaByName, true);
                Gdx.app.postRunnable(() -> {
                    collisionMapWidth = width;
                    collisionMapHeight = height;
                    collisionData = result.data;
                    collisionDirty = true;
                    collisionRegenerationProgress = 0.99f;
                    collisionRegenerationStage = "Saving collisions...";
                    saveCollisionData();
                    collisionRegenerationProgress = 1f;
                    collisionRegenerationStage = "Collisions saved";
                    collisionRegenerating = false;
                    showEditorMessage("Regenerated and saved collisions: " + result.count);
                    log.info("Regenerated collision map from {}: {}", Paths.COLLISION_RULES_BIN, result.count);
                });
            } catch (Throwable t) {
                log.error("Failed to regenerate collision map", t);
                Gdx.app.postRunnable(() -> {
                    collisionRegenerating = false;
                    collisionRegenerationProgress = 0f;
                    collisionRegenerationStage = "";
                    showEditorMessage("Error: Failed to regenerate collisions");
                });
            }
        });
    }

    private void openCollisionRuleEditor() {
        ensureCachedSpriteList();
        collisionRuleEditor = new CollisionRuleEditorUI(cachedSpriteList != null ? cachedSpriteList : List.of());
        openMenu = null;
    }

    private void openCollisionRuleEditorForSprite(String spriteName) {
        ensureCachedSpriteList();
        collisionRuleEditor = new CollisionRuleEditorUI(cachedSpriteList != null ? cachedSpriteList : List.of(), spriteName);
        openMenu = null;
    }

    private void openDecorLayerRuleEditor() {
        ensureCachedSpriteList();
        decorLayerRuleEditor = new DecorLayerRuleEditorUI(cachedSpriteList != null ? cachedSpriteList : List.of());
        openMenu = null;
    }

    private Set<String> getPendingDecorLayerRuleNames() {
        if (pendingDecorLayerRuleNames != null) {
            return pendingDecorLayerRuleNames;
        }
        File file = new File(Paths.DECOR_LAYER_RULES_BIN);
        if (!file.exists()) {
            pendingDecorLayerRuleNames = new LinkedHashSet<>();
            return pendingDecorLayerRuleNames;
        }
        try {
            pendingDecorLayerRuleNames = new LinkedHashSet<>(DecorLayerRuleBinaryIO.read(file));
        } catch (Exception e) {
            log.warn("Failed to load decor layer rules", e);
            showEditorMessage("Error: decor layer rules not loaded");
            pendingDecorLayerRuleNames = new LinkedHashSet<>();
        }
        return pendingDecorLayerRuleNames;
    }

    private void saveDecorLayerRules() {
        try {
            DecorLayerRuleBinaryIO.write(new File(Paths.DECOR_LAYER_RULES_BIN), getPendingDecorLayerRuleNames());
            decorLayerRulesDirty = false;
            if (mapRenderer != null) {
                mapRenderer.reload(modifSprites);
                mapRenderer.setDecorVisible(decorVisible);
                mapRenderer.setObjectsVisible(objectsVisible);
                mapRenderer.setGroundOutlineEnabled(groundOutlineEnabled);
                mapRenderer.setDecorUseTileOffsets(true);
                mapRenderer.setDecorOffsetOverrides(decorOffsetOverrides);
            }
            showEditorMessage("Decor layer rules saved");
        } catch (Exception e) {
            log.error("Failed to save decor layer rules", e);
            showEditorMessage("Error: decor layer rules not saved");
        }
    }

    private void saveCollisionRules(CollisionRules rules) {
        File file = new File(Paths.COLLISION_RULES_BIN);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try {
            saveCollisionRulesBinary(file, rules);
            lastCollisionRulesModified = file.lastModified();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save collision rules", e);
        }
    }

    private CollisionRules loadCollisionRulesBinary(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[COLLISION_RULES_MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, COLLISION_RULES_MAGIC)) {
                throw new IOException("wrong magic header");
            }
            short version = readShortLE(in);
            if (version < 1 || version > COLLISION_RULES_BIN_VERSION) {
                throw new IOException("unsupported version " + version);
            }

            CollisionRules rules = new CollisionRules();
            in.readBoolean();
            rules.defaultDecorCollision = false;
            rules.defaultCollisionValue = readIntLE(in);
            rules.ignoredSprites = readStringList(in);
            rules.exactSprites = new LinkedHashMap<>();
            int exactCount = readIntLE(in);
            for (int i = 0; i < exactCount; i++) {
                rules.exactSprites.put(readString(in), readCollisionRuleBinary(in, version));
            }
            rules.nameContainsRules = new ArrayList<>();
            int containsCount = readIntLE(in);
            for (int i = 0; i < containsCount; i++) {
                CollisionNameRule nameRule = new CollisionNameRule();
                nameRule.contains = readStringList(in);
                nameRule.rule = readCollisionRuleBinary(in, version);
                rules.nameContainsRules.add(nameRule);
            }
            return rules;
        }
    }

    private void saveCollisionRulesBinary(File file, CollisionRules rules) throws IOException {
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(COLLISION_RULES_MAGIC);
            writeShortLE(out, COLLISION_RULES_BIN_VERSION);
            out.writeBoolean(rules.defaultDecorCollision);
            writeIntLE(out, rules.defaultCollisionValue);
            writeStringList(out, rules.ignoredSprites);

            Map<String, CollisionRule> exact = rules.exactSprites != null ? rules.exactSprites : Map.of();
            writeIntLE(out, exact.size());
            for (Map.Entry<String, CollisionRule> entry : exact.entrySet()) {
                writeString(out, entry.getKey());
                writeCollisionRuleBinary(out, entry.getValue());
            }

            List<CollisionNameRule> contains = rules.nameContainsRules != null ? rules.nameContainsRules : List.of();
            writeIntLE(out, contains.size());
            for (CollisionNameRule nameRule : contains) {
                writeStringList(out, nameRule.contains);
                writeCollisionRuleBinary(out, nameRule.rule);
            }
        }
    }

    private CollisionRule readCollisionRuleBinary(DataInputStream in, short version) throws IOException {
        CollisionRule rule = new CollisionRule();
        rule.value = readIntLE(in);
        int tileCount = readIntLE(in);
        rule.tiles = new ArrayList<>(Math.max(0, tileCount));
        for (int i = 0; i < tileCount; i++) {
            rule.tiles.add(new int[] { readIntLE(in), readIntLE(in) });
        }
        rule.clearTiles = new ArrayList<>();
        if (version >= 2) {
            int clearTileCount = readIntLE(in);
            for (int i = 0; i < clearTileCount; i++) {
                rule.clearTiles.add(new int[] { readIntLE(in), readIntLE(in) });
            }
        }
        return rule;
    }

    private void writeCollisionRuleBinary(DataOutputStream out, CollisionRule rule) throws IOException {
        CollisionRule safeRule = rule != null ? rule : new CollisionRule();
        writeIntLE(out, safeRule.value);
        List<int[]> tiles = safeRule.tiles != null ? safeRule.tiles : List.of();
        writeIntLE(out, tiles.size());
        for (int[] tile : tiles) {
            writeIntLE(out, tile != null && tile.length > 0 ? tile[0] : 0);
            writeIntLE(out, tile != null && tile.length > 1 ? tile[1] : 0);
        }
        List<int[]> clearTiles = safeRule.clearTiles != null ? safeRule.clearTiles : List.of();
        writeIntLE(out, clearTiles.size());
        for (int[] tile : clearTiles) {
            writeIntLE(out, tile != null && tile.length > 0 ? tile[0] : 0);
            writeIntLE(out, tile != null && tile.length > 1 ? tile[1] : 0);
        }
    }

    private List<String> readStringList(DataInputStream in) throws IOException {
        int count = readIntLE(in);
        List<String> values = new ArrayList<>(Math.max(0, count));
        for (int i = 0; i < count; i++) {
            values.add(readString(in));
        }
        return values;
    }

    private void writeStringList(DataOutputStream out, List<String> values) throws IOException {
        List<String> safeValues = values != null ? values : List.of();
        writeIntLE(out, safeValues.size());
        for (String value : safeValues) {
            writeString(out, value);
        }
    }

    private String readString(DataInputStream in) throws IOException {
        int length = readIntLE(in);
        if (length < 0 || length > 1_000_000) {
            throw new IOException("invalid string length: " + length);
        }
        byte[] bytes = in.readNBytes(length);
        if (bytes.length != length) {
            throw new IOException("unexpected EOF while reading string");
        }
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    private void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = (value != null ? value : "").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        writeIntLE(out, bytes.length);
        out.write(bytes);
    }

    private void checkCollisionRulesUpdates() {
        long now = TimeUtils.millis();
        if (now < nextCollisionRulesWatchAt) {
            return;
        }
        nextCollisionRulesWatchAt = now + COLLISION_RULES_WATCH_INTERVAL_MS;

        File rulesFile = new File(Paths.COLLISION_RULES_BIN);
        long modified = rulesFile.exists() ? rulesFile.lastModified() : -1L;

        if (lastCollisionRulesModified == -1L) {
            lastCollisionRulesModified = modified;
            return;
        }
        if (modified == lastCollisionRulesModified) {
            return;
        }

        lastCollisionRulesModified = modified;
        if (collisionRegenerating) {
            return;
        }
        log.info("Detected change in {}. Auto-regenerating collisions.", Paths.COLLISION_RULES_BIN);
        regenerateCollisionsFromRulesAsync();
    }
/**
 * Record for CollisionGenerationResult.
 */

    private record CollisionGenerationResult(byte[] data, int count) {
    }

    /**
     * Save collision data to binary file
     */
    private void saveCollisionData() {
        if (collisionData == null) {
            showEditorMessage("Error: No collision data to save");
            return;
        }

        File collisionFile = new File(getCollisionMapPath());
        File parentDir = collisionFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            CollisionMapIO.write(collisionFile, collisionMapWidth, collisionMapHeight, collisionData);
            // Count collision tiles
            int collisionCount = 0;
            for (byte b : collisionData) {
                if (CollisionType.fromValue(b & 0xFF).isBlocksMovement())
                    collisionCount++;
            }

            showEditorMessage("Saved collision data: " + collisionCount + " collision tiles");
            log.info("Saved collision map to {}: {}x{} with {} collision tiles",
                    collisionFile.getAbsolutePath(), collisionMapWidth, collisionMapHeight, collisionCount);
            collisionDirty = false;
        } catch (IOException e) {
            log.error("Failed to save collision data", e);
            showEditorMessage("Error: Failed to save collision data");
        }
    }

    /**
     * Set collision value at grid position
     */
    private void setCollisionAt(int gridX, int gridY, int value) {
        if (collisionData == null)
            return;
        if (gridX < 0 || gridX >= collisionMapWidth || gridY < 0 || gridY >= collisionMapHeight)
            return;

        int index = gridY * collisionMapWidth + gridX;
        byte newValue = (byte) value;
        if (collisionData[index] != newValue) {
            collisionData[index] = newValue;
            collisionDirty = true;
        }
    }

    /**
     * Get collision value at grid position
     */
    private int getCollisionAt(int gridX, int gridY) {
        if (collisionData == null)
            return 0;
        if (gridX < 0 || gridX >= collisionMapWidth || gridY < 0 || gridY >= collisionMapHeight)
            return 0;

        int index = gridY * collisionMapWidth + gridX;
        return collisionData[index] & 0xFF;
    }

    /**
     * Paint collision value at mouse position
     */
    private void paintCollisionAtMouse() {
        if (collisionData == null)
            return;

        Vector3 worldPos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        int gridX = (int) (worldPos.x / GameConstants.GRID_W);
        int gridY = (int) (worldPos.y / GameConstants.GRID_H);

        setCollisionAt(gridX, gridY, selectedCollisionValue);
    }

    private void eraseCollisionAtMouse() {
        if (collisionData == null)
            return;

        Vector3 worldPos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        int gridX = (int) (worldPos.x / GameConstants.GRID_W);
        int gridY = (int) (worldPos.y / GameConstants.GRID_H);

        setCollisionAt(gridX, gridY, 0);
    }

    private void pickCollisionAtMouse() {
        if (collisionData == null) {
            return;
        }
        Vector3 worldPos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        int gridX = (int) (worldPos.x / GameConstants.GRID_W);
        int gridY = (int) (worldPos.y / GameConstants.GRID_H);
        selectCollisionValue(getCollisionAt(gridX, gridY));
    }

    /**
     * Render collision overlay
     */
    private void renderCollisionOverlay() {
        if (!collisionOverlayVisible || collisionData == null)
            return;

        // Clip editor overlays away from the menu and bottom info bar.
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(0, BOTTOM_INFO_HEIGHT, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight() - BOTTOM_INFO_HEIGHT - MENU_BAR_HEIGHT);

        int[] bounds = calculateVisibleBounds(1);
        int startX = bounds[0], endX = Math.min(collisionMapWidth - 1, bounds[1]);
        int startY = bounds[2], endY = Math.min(collisionMapHeight - 1, bounds[3]);

        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int collision = getCollisionAt(x, y);
                if (collision > 0) {
                    // Color based on collision value
                    Color color = getCollisionColor(collision);
                    shapeRenderer.setColor(color.r, color.g, color.b, 0.4f);
                    shapeRenderer.rect(
                            x * GameConstants.GRID_W,
                            y * GameConstants.GRID_H,
                            GameConstants.GRID_W,
                            GameConstants.GRID_H);
                }
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Désactiver le scissor test
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    /**
     * Get color for collision value
     */
    private Color getCollisionColor(int value) {
        return CollisionType.fromValue(value).getEditorColor();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
/**
 * Class representing CollisionRuleEditorUI.
 */

    private class CollisionRuleEditorUI extends EditorDialog {
        private static final int LIST_WIDTH = 330;
        private static final int PADDING = 22;
        private static final int HEADER_HEIGHT = 62;
        private static final int SEARCH_HEIGHT = 34;
        private static final int FOOTER_HEIGHT = 58;
        private static final int ROW_HEIGHT = 28;
        private static final int GRID_RADIUS = 5;
        private static final int GRID_SIZE = GRID_RADIUS * 2 + 1;

        private final List<SpritePickerUI.SpriteEntry> allSprites;
        private List<SpritePickerUI.SpriteEntry> filteredSprites;
        private final CollisionRules rules;
        private final Set<String> collisionTiles = new HashSet<>();
        private final Set<String> noCollisionTiles = new HashSet<>();
        private final GlyphLayout textLayout = new GlyphLayout();
        private SpritePickerUI.SpriteEntry selectedSprite;
        private String searchFilter = "";
        private int scrollOffset = 0;
        private String inheritedRuleLabel = null;
        private boolean dirty = false;
        private boolean filterExistingCollisions = false;
        private final EditorListBox<SpritePickerUI.SpriteEntry> spriteListBox = new EditorListBox<SpritePickerUI.SpriteEntry>()
                .rowHeight(ROW_HEIGHT)
                .labelProvider(e -> e == null ? "" : e.name)
                .colorProvider(i -> filteredSprites != null && i < filteredSprites.size()
                        && filteredSprites.get(i) == selectedSprite ? EditorTheme.BLUE : null);

        private final EditorButton btnModeCollision = new EditorButton("Red", () -> paintNoCollisionMode = false);
        private final EditorButton btnModeNoCollision = new EditorButton("Green", () -> paintNoCollisionMode = true);
        private final EditorButton btnClear = new EditorButton("Clear", this::clearCollisionRule);
        private final com.badlogic.gdx.math.Rectangle searchBounds = new com.badlogic.gdx.math.Rectangle();
        private final com.badlogic.gdx.math.Rectangle existingOnlyBounds = new com.badlogic.gdx.math.Rectangle();
        private final com.badlogic.gdx.math.Rectangle listBounds = new com.badlogic.gdx.math.Rectangle();
        private final com.badlogic.gdx.math.Rectangle gridBounds = new com.badlogic.gdx.math.Rectangle();
        private final com.badlogic.gdx.math.Rectangle hScrollBounds = new com.badlogic.gdx.math.Rectangle();
        private final com.badlogic.gdx.math.Rectangle vScrollBounds = new com.badlogic.gdx.math.Rectangle();
        private float viewOffsetX = 0f;
        private float viewOffsetY = 0f;
        private boolean paintNoCollisionMode = false;
        private String lastDraggedTileKey = null;
        private boolean dragEraseMode = false;

        CollisionRuleEditorUI(List<SpritePickerUI.SpriteEntry> sprites) {
            this(sprites, null);
        }

        CollisionRuleEditorUI(List<SpritePickerUI.SpriteEntry> sprites, String initialSpriteName) {
            super("Collision Rules");
            this.allSprites = new ArrayList<>(sprites);
            this.filteredSprites = new ArrayList<>(allSprites);
            spriteListBox.setItems(filteredSprites);
            this.rules = loadCollisionRules();
            if (rules.exactSprites == null) {
                rules.exactSprites = new LinkedHashMap<>();
            }
            if (initialSpriteName != null && !initialSpriteName.isBlank()) {
                SpritePickerUI.SpriteEntry match = null;
                for (SpritePickerUI.SpriteEntry entry : filteredSprites) {
                    if (entry != null && entry.name != null && entry.name.equalsIgnoreCase(initialSpriteName)) {
                        match = entry;
                        break;
                    }
                }
                if (match != null) {
                    selectSprite(match);
                }
            }
            if (selectedSprite == null && !filteredSprites.isEmpty()) {
                selectSprite(filteredSprites.get(0));
            }
        }

        public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
            int screenWidth = Gdx.graphics.getWidth();
            int screenHeight = Gdx.graphics.getHeight();
            computeLayout(screenWidth, screenHeight);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(
                    shapeRenderer.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(shapeRenderer, screenWidth, screenHeight);
            
            com.badlogic.gdx.math.Rectangle panelBounds = new com.badlogic.gdx.math.Rectangle(PADDING, PADDING, screenWidth - PADDING * 2f, screenHeight - PADDING * 2f);
            EditorPanelChrome.panel(shapeRenderer, panelBounds, EditorTheme.ORANGE, HEADER_HEIGHT);

            EditorPanelChrome.textField(shapeRenderer, searchBounds, true);
            EditorPanelChrome.darkSurface(shapeRenderer, gridBounds);

            float checkboxSize = 14f;
            float checkboxY = existingOnlyBounds.y + (existingOnlyBounds.height - checkboxSize) / 2f;
            EditorPanelChrome.checkbox(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(existingOnlyBounds.x, checkboxY, checkboxSize, checkboxSize),
                    filterExistingCollisions);
            
            drawGridCells(shapeRenderer);
            drawPreviewScrollbars(shapeRenderer);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(shapeRenderer, panelBounds);
            EditorPanelChrome.border(shapeRenderer, searchBounds);
            EditorPanelChrome.border(shapeRenderer, gridBounds);
            EditorPanelChrome.border(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(existingOnlyBounds.x, checkboxY, checkboxSize, checkboxSize));
            shapeRenderer.end();

            batch.setProjectionMatrix(batch.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
            batch.begin();
            batch.setColor(Color.WHITE);
            float oldScaleX = font.getData().scaleX;
            float oldScaleY = font.getData().scaleY;
            font.getData().setScale(1f);

            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Collision Rules Editor", panelBounds.x + 18f, panelBounds.y + panelBounds.height - 18f);
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, "Select a sprite, paint collision tiles, then save to collision_rules.bin",
                    panelBounds.x + 18f, panelBounds.y + panelBounds.height - 42f);

            font.setColor(searchFilter.isBlank() ? UI_TEXT_MUTED : UI_TEXT);
            font.draw(batch, searchFilter.isBlank() ? "Search sprite..." : searchFilter, searchBounds.x + 11f,
                    searchBounds.y + 22f);
            
            float checkTextY = existingOnlyBounds.y + existingOnlyBounds.height / 2f + 5f;
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, filterExistingCollisions ? "x" : "", existingOnlyBounds.x + 3f, checkTextY);
            font.setColor(UI_TEXT_MUTED);
            font.draw(batch, "Has collision", existingOnlyBounds.x + 22f, checkTextY);
            
            drawSelectedPreview(batch);
            font.getData().setScale(oldScaleX, oldScaleY);

            batch.end();

            spriteListBox.render(batch, shapeRenderer, font);
            btnModeCollision.withFont(font).render(batch, shapeRenderer);
            btnModeNoCollision.withFont(font).render(batch, shapeRenderer);
            btnClear.withFont(font).render(batch, shapeRenderer);

            drawSelectedCollisionTilesSolid(shapeRenderer, screenWidth, screenHeight);
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT) {
                return true;
            }
            float x = screenX;
            float y = Gdx.graphics.getHeight() - screenY;
            computeLayout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int ix = (int) x;
            int iy = (int) y;
            if (btnModeCollision.handleClick(ix, iy, button)) return true;
            if (btnModeNoCollision.handleClick(ix, iy, button)) return true;
            if (btnClear.handleClick(ix, iy, button)) return true;
            if (existingOnlyBounds.contains(x, y)) {
                filterExistingCollisions = !filterExistingCollisions;
                applyFilter();
                return true;
            }
            if (spriteListBox.handleClick((int) x, (int) y, button)) {
                int idx = spriteListBox.selectedItem() != null ? spriteListBox.selectedIndex() : -1;
                if (idx >= 0 && idx < filteredSprites.size()) selectSprite(filteredSprites.get(idx));
                scrollOffset = spriteListBox.scrollOffset();
                return true;
            }
            if (gridBounds.contains(x, y)) {
                paintGridPoint(x, y, true);
                return true;
            }
            if (hScrollBounds.contains(x, y) || vScrollBounds.contains(x, y)) {
                updateScrollFromPoint(x, y);
                return true;
            }
            return true;
        }

        public boolean handleDrag(int screenX, int screenY) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                return true;
            }
            float x = screenX;
            float y = Gdx.graphics.getHeight() - screenY;
            computeLayout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            if (gridBounds.contains(x, y)) {
                paintGridPoint(x, y, false);
                return true;
            }
            if (hScrollBounds.contains(x, y) || vScrollBounds.contains(x, y)) {
                updateScrollFromPoint(x, y);
                return true;
            }
            return true;
        }

        public boolean handleTouchUp(int screenX, int screenY, int button) {
            lastDraggedTileKey = null;
            dragEraseMode = false;
            return true;
        }

        public boolean handleScroll(float amount) {
            if (requiresHorizontalScroll()) {
                viewOffsetX = clampPreviewOffsetX(viewOffsetX + (amount > 0 ? 1f : -1f));
                return true;
            }
            if (requiresVerticalScroll()) {
                viewOffsetY = clampPreviewOffsetY(viewOffsetY + (amount > 0 ? -1f : 1f));
                return true;
            }
            spriteListBox.scroll(amount > 0 ? 3 : -3);
            scrollOffset = spriteListBox.scrollOffset();
            return true;
        }

        public boolean handleKeyTyped(char character) {
            if (character == '\b') {
                if (!searchFilter.isEmpty()) {
                    searchFilter = searchFilter.substring(0, searchFilter.length() - 1);
                    applyFilter();
                }
                return true;
            }
            if (character == '\r' || character == '\n') {
                if (!filteredSprites.isEmpty()) {
                    selectSprite(filteredSprites.get(0));
                }
                return true;
            }
            if (character >= 32 && character != 127) {
                searchFilter += character;
                applyFilter();
                return true;
            }
            return true;
        }

        private void computeLayout(int screenWidth, int screenHeight) {
            float panelLeft = PADDING;
            float panelBottom = PADDING;
            float panelRight = screenWidth - PADDING;
            float contentTop = screenHeight - PADDING - HEADER_HEIGHT - 16f;
            float footerTop = panelBottom + FOOTER_HEIGHT;

            searchBounds.set(panelLeft + 18f, contentTop - SEARCH_HEIGHT, LIST_WIDTH - 36f, SEARCH_HEIGHT);
            existingOnlyBounds.set(panelLeft + 18f, searchBounds.y - 32f, LIST_WIDTH - 36f, 24f);
            btnModeCollision.setBounds(panelLeft + 18f, existingOnlyBounds.y - 38f, 78f, 30f);
            btnModeNoCollision.setBounds(btnModeCollision.bounds().x + 78f + 8f, existingOnlyBounds.y - 38f, 78f, 30f);
            listBounds.set(panelLeft + 18f, footerTop + 14f, LIST_WIDTH - 36f,
                    btnModeCollision.bounds().y - footerTop - 24f);
            spriteListBox.setBounds(listBounds.x, listBounds.y, listBounds.width, listBounds.height);
            spriteListBox.setScrollOffset(scrollOffset);
            float editorLeft = panelLeft + LIST_WIDTH + 18f;
            float editorWidth = panelRight - editorLeft - 18f;
            float editorHeight = contentTop - footerTop - 14f;
            float gridWidth = Math.min(editorWidth, editorHeight * 2f);
            float gridHeight = gridWidth / 2f;
            gridBounds.set(editorLeft + (editorWidth - gridWidth) / 2f,
                    footerTop + 14f + (editorHeight - gridHeight) / 2f,
                    gridWidth,
                    gridHeight);
            hScrollBounds.set(gridBounds.x, gridBounds.y - 14f, gridBounds.width, 10f);
            vScrollBounds.set(gridBounds.x + gridBounds.width + 4f, gridBounds.y, 10f, gridBounds.height);
            btnClear.setBounds(panelRight - 118f, panelBottom + 15f, 82f, 30f);
        }

        private void clearCollisionRule() {
            if (!collisionTiles.isEmpty() || !noCollisionTiles.isEmpty() || inheritedRuleLabel != null) {
                collisionTiles.clear();
                noCollisionTiles.clear();
                inheritedRuleLabel = null;
                dirty = true;
                saveCurrentRule(false);
            }
        }

        private void drawPreviewScrollbars(ShapeRenderer shapeRenderer) {
            if (requiresHorizontalScroll()) {
                float range = getHorizontalScrollRange();
                float ratio = range <= 0f ? 0f : ((viewOffsetX + range) / (2f * range));
                float thumbW = Math.max(24f, hScrollBounds.width * 0.18f);
                float thumbX = hScrollBounds.x + (hScrollBounds.width - thumbW) * ratio;
                EditorPanelChrome.scrollbar(shapeRenderer, hScrollBounds,
                        new com.badlogic.gdx.math.Rectangle(thumbX, hScrollBounds.y, thumbW, hScrollBounds.height));
            }
            if (requiresVerticalScroll()) {
                float range = getVerticalScrollRange();
                float ratio = range <= 0f ? 0f : ((viewOffsetY + range) / (2f * range));
                float thumbH = Math.max(24f, vScrollBounds.height * 0.18f);
                float thumbY = vScrollBounds.y + (vScrollBounds.height - thumbH) * ratio;
                EditorPanelChrome.scrollbar(shapeRenderer, vScrollBounds,
                        new com.badlogic.gdx.math.Rectangle(vScrollBounds.x, thumbY, vScrollBounds.width, thumbH));
            }
        }

        private class PreviewTransform {
            final int minX;
            final int minY;
            final int cellsX;
            final int cellsY;
            final float originX;
            final float originY;
            final float cellW;
            final float cellH;

            PreviewTransform(int minX, int minY, int cellsX, int cellsY,
                    float originX, float originY, float cellW, float cellH) {
                this.minX = minX;
                this.minY = minY;
                this.cellsX = cellsX;
                this.cellsY = cellsY;
                this.originX = originX;
                this.originY = originY;
                this.cellW = cellW;
                this.cellH = cellH;
            }

            int maxY() {
                return minY + cellsY;
            }

            float width() {
                return cellsX * cellW;
            }

            float height() {
                return cellsY * cellH;
            }
        }

        private PreviewTransform previewTransform() {
            float minContentX = -GRID_RADIUS;
            float maxContentX = GRID_RADIUS + 1f;
            float minContentY = -GRID_RADIUS;
            float maxContentY = GRID_RADIUS + 1f;

            if (selectedSprite != null) {
                TextureRegion region = spriteLoader.getRegionFromSpriteName(selectedSprite.name);
                if (region != null) {
                    float[] bounds = spriteLogicalBounds(selectedSprite.name, region);
                    minContentX = bounds[0];
                    minContentY = bounds[1];
                    maxContentX = bounds[2];
                    maxContentY = bounds[3];
                }
            }

            float[] contentBounds = new float[] { minContentX, minContentY, maxContentX, maxContentY };
            includeSelectedTileBounds(contentBounds, collisionTiles);
            includeSelectedTileBounds(contentBounds, noCollisionTiles);

            float contentW = Math.max(1f, contentBounds[2] - contentBounds[0]);
            float contentH = Math.max(1f, contentBounds[3] - contentBounds[1]);
            int cellsX = Math.max(GRID_SIZE, (int) Math.ceil(contentW) + 2);
            int cellsY = Math.max(GRID_SIZE, (int) Math.ceil(contentH) + 2);
            float centerX = (contentBounds[0] + contentBounds[2]) * 0.5f;
            float centerY = (contentBounds[1] + contentBounds[3]) * 0.5f;
            int minX = (int) Math.floor(centerX - cellsX * 0.5f);
            int minY = (int) Math.floor(centerY - cellsY * 0.5f);

            float cellH = Math.min(gridBounds.height / cellsY, gridBounds.width / (cellsX * 2f));
            if (cellH <= 0f) {
                cellH = gridBounds.height / GRID_SIZE;
            }
            float cellW = cellH * 2f;
            float previewW = cellsX * cellW;
            float previewH = cellsY * cellH;
            float originX = gridBounds.x + (gridBounds.width - previewW) * 0.5f;
            float originY = gridBounds.y + (gridBounds.height - previewH) * 0.5f;
            return new PreviewTransform(minX, minY, cellsX, cellsY, originX, originY, cellW, cellH);
        }

        private void includeSelectedTileBounds(float[] bounds, Set<String> tiles) {
            for (String key : tiles) {
                int[] tile = parseTileKey(key);
                if (tile == null) {
                    continue;
                }
                bounds[0] = Math.min(bounds[0], tile[0]);
                bounds[1] = Math.min(bounds[1], tile[1]);
                bounds[2] = Math.max(bounds[2], tile[0] + 1f);
                bounds[3] = Math.max(bounds[3], tile[1] + 1f);
            }
        }

        private int[] parseTileKey(String key) {
            if (key == null) {
                return null;
            }
            String[] parts = key.split(",", 2);
            if (parts.length != 2) {
                return null;
            }
            try {
                return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) };
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        private void drawGridCells(ShapeRenderer shapeRenderer) {
            PreviewTransform t = previewTransform();
            for (int gy = 0; gy < t.cellsY; gy++) {
                for (int gx = 0; gx < t.cellsX; gx++) {
                    int dx = t.minX + gx;
                    int dy = t.minY + (t.cellsY - 1 - gy);
                    String key = tileKey(dx, dy);
                    boolean red = collisionTiles.contains(key);
                    boolean green = noCollisionTiles.contains(key);
                    float x = t.originX + gx * t.cellW;
                    float y = t.originY + gy * t.cellH;
                    if (red) {
                        shapeRenderer.setColor(1.0f, 0.05f, 0.02f, 0.18f);
                        shapeRenderer.rect(x + 1f, y + 1f, t.cellW - 2f, t.cellH - 2f);
                    } else if (green) {
                        shapeRenderer.setColor(0.12f, 0.75f, 0.18f, 0.22f);
                        shapeRenderer.rect(x + 1f, y + 1f, t.cellW - 2f, t.cellH - 2f);
                    } else if (dx == 0 && dy == 0) {
                        shapeRenderer.setColor(0.16f, 0.42f, 0.72f, 0.22f);
                        shapeRenderer.rect(x + 1f, y + 1f, t.cellW - 2f, t.cellH - 2f);
                    }
                }
            }
            shapeRenderer.setColor(UI_BORDER);
            for (int i = 0; i <= t.cellsX; i++) {
                float pos = t.originX + i * t.cellW;
                shapeRenderer.rectLine(pos, t.originY, pos, t.originY + t.height(), 1f);
            }
            for (int i = 0; i <= t.cellsY; i++) {
                float pos = t.originY + i * t.cellH;
                shapeRenderer.rectLine(t.originX, pos, t.originX + t.width(), pos, 1f);
            }
            shapeRenderer.setColor(1.0f, 0.96f, 0.88f, 0.95f);
            for (int gy = 0; gy < t.cellsY; gy++) {
                for (int gx = 0; gx < t.cellsX; gx++) {
                    int dx = t.minX + gx;
                    int dy = t.minY + (t.cellsY - 1 - gy);
                    if (collisionTiles.contains(tileKey(dx, dy)) || noCollisionTiles.contains(tileKey(dx, dy))) {
                        shapeRenderer.rect(t.originX + gx * t.cellW + 2f, t.originY + gy * t.cellH + 2f,
                                t.cellW - 4f, t.cellH - 4f);
                    }
                }
            }
        }

        private void drawSelectedCollisionTilesSolid(ShapeRenderer shapeRenderer, int screenWidth, int screenHeight) {
            if (collisionTiles.isEmpty() && noCollisionTiles.isEmpty()) {
                return;
            }
            shapeRenderer.setProjectionMatrix(
                    shapeRenderer.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
            PreviewTransform t = previewTransform();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int gy = 0; gy < t.cellsY; gy++) {
                for (int gx = 0; gx < t.cellsX; gx++) {
                    int dx = t.minX + gx;
                    int dy = t.minY + (t.cellsY - 1 - gy);
                    String key = tileKey(dx, dy);
                    if (collisionTiles.contains(key)) {
                        shapeRenderer.setColor(0.95f, 0.04f, 0.02f, 1f);
                        shapeRenderer.rect(t.originX + gx * t.cellW + 2f, t.originY + gy * t.cellH + 2f,
                                t.cellW - 4f, t.cellH - 4f);
                    } else if (noCollisionTiles.contains(key)) {
                        shapeRenderer.setColor(0.12f, 0.75f, 0.18f, 1f);
                        shapeRenderer.rect(t.originX + gx * t.cellW + 2f, t.originY + gy * t.cellH + 2f,
                                t.cellW - 4f, t.cellH - 4f);
                    }
                }
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 0.96f, 0.88f, 1f);
            for (int gy = 0; gy < t.cellsY; gy++) {
                for (int gx = 0; gx < t.cellsX; gx++) {
                    int dx = t.minX + gx;
                    int dy = t.minY + (t.cellsY - 1 - gy);
                    if (collisionTiles.contains(tileKey(dx, dy)) || noCollisionTiles.contains(tileKey(dx, dy))) {
                        shapeRenderer.rect(t.originX + gx * t.cellW + 2f, t.originY + gy * t.cellH + 2f,
                                t.cellW - 4f, t.cellH - 4f);
                    }
                }
            }
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        private void drawSelectedPreview(SpriteBatch batch) {
            float labelY = gridBounds.y + gridBounds.height + 44f;
            font.getData().setScale(1.0f);
            font.setColor(UI_TEXT_LIGHT);
            String name = selectedSprite == null ? "No sprite selected" : selectedSprite.name;
            textLayout.setText(font, name);
            font.draw(batch, name, gridBounds.x + (gridBounds.width - textLayout.width) / 2f, labelY);
            font.getData().setScale(0.78f);
            font.setColor(UI_TEXT_MUTED);
            String detail = inheritedRuleLabel != null ? inheritedRuleLabel
                    : (collisionTiles.size() + " collision tiles, " + noCollisionTiles.size() + " no-collision tiles");
            textLayout.setText(font, detail);
            font.draw(batch, detail, gridBounds.x + (gridBounds.width - textLayout.width) / 2f, labelY - 24f);

            if (selectedSprite == null) {
                return;
            }
            TextureRegion region = spriteLoader.getRegionFromSpriteName(selectedSprite.name);
            if (region == null) {
                return;
            }
            PreviewTransform t = previewTransform();
            float[] bounds = spriteLogicalBounds(selectedSprite.name, region);
            float w = (bounds[2] - bounds[0]) * t.cellW;
            float h = (bounds[3] - bounds[1]) * t.cellH;
            float drawX = t.originX + (bounds[0] - t.minX) * t.cellW;
            float drawY = t.originY + (t.maxY() - bounds[3]) * t.cellH;
            batch.flush();
            Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
            Gdx.gl.glScissor(
                    Math.max(0, (int) gridBounds.x),
                    Math.max(0, (int) gridBounds.y),
                    Math.max(0, (int) gridBounds.width),
                    Math.max(0, (int) gridBounds.height));
            batch.setColor(1f, 1f, 1f, 0.72f);
            batch.draw(region, drawX, drawY, w, h);
            batch.setColor(Color.WHITE);
            batch.flush();
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        }

        private float[] spriteLogicalBounds(String spriteName, TextureRegion region) {
            SpriteLoader.Sprite meta = findSpriteMeta(spriteName);
            float offX = meta != null ? meta.getDrawOffset1X() : 0f;
            float offY = meta != null ? meta.getDrawOffset1Y() : 0f;
            float logicalW = region.getRegionWidth() / (float) GameConstants.GRID_W;
            float logicalH = region.getRegionHeight() / (float) GameConstants.GRID_H;
            float logicalX = offX / GameConstants.GRID_W;
            float logicalY = offY / GameConstants.GRID_H + 1f - logicalH;
            return new float[] { logicalX, logicalY, logicalX + logicalW, logicalY + logicalH };
        }

        private SpriteLoader.Sprite findSpriteMeta(String name) {
            if (name == null) {
                return null;
            }
            String key = name.toLowerCase(Locale.ROOT);
            if (mapRenderer != null && mapRenderer.getMetaByName() != null) {
                SpriteLoader.Sprite meta = mapRenderer.getMetaByName().get(key);
                if (meta != null) {
                    return meta;
                }
            }
            for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
                if (sprite.getName() != null && sprite.getName().equalsIgnoreCase(name)) {
                    return sprite;
                }
            }
            return null;
        }

        private int[] tileFromGridPoint(float x, float y) {
            PreviewTransform t = previewTransform();
            int gx = (int) ((x - t.originX) / t.cellW);
            int gy = (int) ((y - t.originY) / t.cellH);
            if (gx < 0 || gx >= t.cellsX || gy < 0 || gy >= t.cellsY) {
                return null;
            }
            return new int[] { t.minX + gx, t.minY + (t.cellsY - 1 - gy) };
        }

        private void paintGridPoint(float x, float y, boolean toggle) {
            int[] tile = tileFromGridPoint(x, y);
            if (tile == null) {
                return;
            }
            String key = tileKey(tile[0], tile[1]);
            if (toggle) {
                dragEraseMode = paintNoCollisionMode ? noCollisionTiles.contains(key) : collisionTiles.contains(key);
            }
            if (!toggle && key.equals(lastDraggedTileKey)) {
                return;
            }
            lastDraggedTileKey = key;

            boolean changed;
            if (paintNoCollisionMode) {
                if (toggle) {
                    changed = toggleTile(noCollisionTiles, key);
                    changed |= collisionTiles.remove(key);
                } else if (dragEraseMode) {
                    changed = noCollisionTiles.remove(key);
                } else {
                    changed = noCollisionTiles.add(key);
                    changed |= collisionTiles.remove(key);
                }
            } else {
                if (toggle) {
                    changed = toggleTile(collisionTiles, key);
                    changed |= noCollisionTiles.remove(key);
                } else if (dragEraseMode) {
                    changed = collisionTiles.remove(key);
                } else {
                    changed = collisionTiles.add(key);
                    changed |= noCollisionTiles.remove(key);
                }
            }
            if (!changed) {
                return;
            }
            inheritedRuleLabel = null;
            dirty = true;
            saveCurrentRule(false);
        }

        private boolean toggleTile(Set<String> tiles, String key) {
            if (tiles.remove(key)) {
                return true;
            }
            return tiles.add(key);
        }

        private void applyFilter() {
            String filter = normalizeSpriteKey(searchFilter);
            filteredSprites = new ArrayList<>();
            for (SpritePickerUI.SpriteEntry entry : allSprites) {
                if ((!filterExistingCollisions || hasCollisionRule(entry.name))
                        && (filter.isBlank() || normalizeSpriteKey(entry.name).contains(filter))) {
                    filteredSprites.add(entry);
                }
            }
            scrollOffset = 0;
            spriteListBox.setItems(filteredSprites);
            spriteListBox.setScrollOffset(0);
        }

        private void selectSprite(SpritePickerUI.SpriteEntry entry) {
            if (entry == null || entry == selectedSprite) {
                return;
            }
            saveCurrentRule(false);
            selectedSprite = entry;
            viewOffsetX = 0f;
            viewOffsetY = 0f;
            collisionTiles.clear();
            noCollisionTiles.clear();
            inheritedRuleLabel = null;
            dirty = false;
            CollisionRule exact = findExactRule(entry.name);
            if (exact != null) {
                loadTiles(exact);
                return;
            }
            CollisionNameRule inherited = findInheritedRule(entry.name);
            if (inherited != null) {
                loadTiles(inherited.rule);
                inheritedRuleLabel = "Inherited from contains rule";
            }
        }

        private CollisionRule findExactRule(String name) {
            if (rules.exactSprites == null || name == null) {
                return null;
            }
            String normalized = normalizeSpriteKey(name);
            for (Map.Entry<String, CollisionRule> entry : rules.exactSprites.entrySet()) {
                if (normalizeSpriteKey(entry.getKey()).equals(normalized)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        private CollisionNameRule findInheritedRule(String name) {
            if (rules.nameContainsRules == null || name == null) {
                return null;
            }
            String normalized = normalizeSpriteKey(name);
            for (CollisionNameRule rule : rules.nameContainsRules) {
                if (rule != null && rule.matches(normalized)) {
                    return rule;
                }
            }
            return null;
        }

        private boolean hasCollisionRule(String name) {
            return findExactRule(name) != null || findInheritedRule(name) != null;
        }

        private void loadTiles(CollisionRule rule) {
            for (int[] tile : rule.normalizedTiles()) {
                if (tile != null && tile.length >= 2) {
                    collisionTiles.add(tileKey(tile[0], tile[1]));
                }
            }
            for (int[] tile : rule.normalizedClearTiles()) {
                if (tile != null && tile.length >= 2) {
                    noCollisionTiles.add(tileKey(tile[0], tile[1]));
                }
            }
        }

        public void close() {
            saveCurrentRule(false);
            collisionRuleEditor = null;
        }

        private void saveCurrentRule(boolean forceMessage) {
            if (selectedSprite == null) {
                if (forceMessage) {
                    showEditorMessage("No sprite selected");
                }
                return;
            }
            if (!dirty && !forceMessage) {
                return;
            }
            removeExactRule(selectedSprite.name);
            if (!collisionTiles.isEmpty() || !noCollisionTiles.isEmpty()) {
                CollisionRule rule = new CollisionRule();
                rule.value = COLLISION_VALUE_RED;
                rule.tiles = selectedTilesAsList(collisionTiles);
                rule.clearTiles = selectedTilesAsList(noCollisionTiles);
                rules.exactSprites.put(selectedSprite.name, rule);
            }
            try {
                saveCollisionRules(rules);
                dirty = false;
                if (forceMessage) {
                    showEditorMessage("Collision rule saved: " + selectedSprite.name);
                }
            } catch (RuntimeException e) {
                log.error("Failed to save collision rules", e);
                showEditorMessage("Error: collision rule not saved");
            }
        }

        private void removeExactRule(String name) {
            if (rules.exactSprites == null) {
                rules.exactSprites = new LinkedHashMap<>();
                return;
            }
            String normalized = normalizeSpriteKey(name);
            String existingKey = null;
            for (String key : rules.exactSprites.keySet()) {
                if (normalizeSpriteKey(key).equals(normalized)) {
                    existingKey = key;
                    break;
                }
            }
            if (existingKey != null) {
                rules.exactSprites.remove(existingKey);
            }
        }

        private List<int[]> selectedTilesAsList(Set<String> source) {
            List<int[]> tiles = new ArrayList<>();
            for (String key : source) {
                String[] parts = key.split(",", 2);
                if (parts.length == 2) {
                    tiles.add(new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) });
                }
            }
            tiles.sort(Comparator.<int[]>comparingInt(tile -> tile[1]).thenComparingInt(tile -> tile[0]));
            return tiles;
        }

        private String tileKey(int x, int y) {
            return x + "," + y;
        }

        private boolean requiresHorizontalScroll() {
            return false;
        }

        private boolean requiresVerticalScroll() {
            return false;
        }

        private float getHorizontalScrollRange() {
            TextureRegion region = spriteLoader.getRegionFromSpriteName(selectedSprite.name);
            if (region == null) return 0f;
            float[] b = spriteLogicalBounds(selectedSprite.name, region);
            return Math.max(0f, ((b[2] - b[0]) - GRID_SIZE) * 0.5f + 1f);
        }

        private float getVerticalScrollRange() {
            TextureRegion region = spriteLoader.getRegionFromSpriteName(selectedSprite.name);
            if (region == null) return 0f;
            float[] b = spriteLogicalBounds(selectedSprite.name, region);
            return Math.max(0f, ((b[3] - b[1]) - GRID_SIZE) * 0.5f + 1f);
        }

        private float clampPreviewOffsetX(float v) {
            float r = getHorizontalScrollRange();
            return Math.max(-r, Math.min(r, v));
        }

        private float clampPreviewOffsetY(float v) {
            float r = getVerticalScrollRange();
            return Math.max(-r, Math.min(r, v));
        }

        private void updateScrollFromPoint(float x, float y) {
            if (hScrollBounds.contains(x, y) && requiresHorizontalScroll()) {
                float ratio = (x - hScrollBounds.x) / Math.max(1f, hScrollBounds.width);
                float r = getHorizontalScrollRange();
                viewOffsetX = clampPreviewOffsetX((ratio * 2f - 1f) * r);
            }
            if (vScrollBounds.contains(x, y) && requiresVerticalScroll()) {
                float ratio = (y - vScrollBounds.y) / Math.max(1f, vScrollBounds.height);
                float r = getVerticalScrollRange();
                viewOffsetY = clampPreviewOffsetY((ratio * 2f - 1f) * r);
            }
        }
    }
/**
 * Class representing DecorLayerRuleEditorUI.
 */

    private class DecorLayerRuleEditorUI extends EditorDialog {
        private static final float PADDING = 42f;
        private static final float HEADER_HEIGHT = 64f;
        private static final float LIST_WIDTH = 390f;
        private static final float SEARCH_HEIGHT = 34f;
        private static final float ROW_HEIGHT = 24f;
        private final List<SpritePickerUI.SpriteEntry> allSprites;
        private List<SpritePickerUI.SpriteEntry> filteredSprites;
        private final Set<String> ruleNames;
        private final EditorButton btnToggle = new EditorButton("Enable", this::toggleSelected);
        private final com.badlogic.gdx.math.Rectangle searchBounds = new com.badlogic.gdx.math.Rectangle();
        private final com.badlogic.gdx.math.Rectangle enabledOnlyBounds = new com.badlogic.gdx.math.Rectangle();
        private final com.badlogic.gdx.math.Rectangle listBounds = new com.badlogic.gdx.math.Rectangle();
        private final com.badlogic.gdx.math.Rectangle toggleBounds = new com.badlogic.gdx.math.Rectangle();
        private final GlyphLayout ruleTextLayout = new GlyphLayout();
        private SpritePickerUI.SpriteEntry selectedSprite;
        private final EditorListBox<SpritePickerUI.SpriteEntry> decorSpriteListBox = new EditorListBox<SpritePickerUI.SpriteEntry>()
                .rowHeight(ROW_HEIGHT)
                .darkBackground()
                .labelProvider(e -> e == null ? "" : (hasRule(e.name) ? "[x] " : "[ ] ") + e.name)
                .colorProvider(i -> {
                    if (filteredSprites == null || i >= filteredSprites.size()) return null;
                    SpritePickerUI.SpriteEntry e = filteredSprites.get(i);
                    return e == selectedSprite ? EditorTheme.BLUE
                            : (hasRule(e.name) ? EditorTheme.TEXT_LIGHT : EditorTheme.TEXT_FAINT);
                });
        private String searchFilter = "";
        private int scrollOffset = 0;
        private boolean filterEnabledOnly = false;
        private boolean dirty = false;

        DecorLayerRuleEditorUI(List<SpritePickerUI.SpriteEntry> sprites) {
            super("Player Above Decor Rules");
            this.allSprites = new ArrayList<>(sprites);
            this.filteredSprites = new ArrayList<>(allSprites);
            this.ruleNames = getPendingDecorLayerRuleNames();
            if (!filteredSprites.isEmpty()) {
                selectedSprite = filteredSprites.get(0);
            }
            decorSpriteListBox.setItems(filteredSprites);
        }

        public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
            int screenWidth = Gdx.graphics.getWidth();
            int screenHeight = Gdx.graphics.getHeight();
            computeLayout(screenWidth, screenHeight);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(
                    shapeRenderer.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(shapeRenderer, screenWidth, screenHeight);
            EditorPanelChrome.panel(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(PADDING, PADDING, screenWidth - PADDING * 2f,
                            screenHeight - PADDING * 2f),
                    EditorTheme.ORANGE, HEADER_HEIGHT);
            EditorPanelChrome.surface(shapeRenderer, searchBounds);
            EditorPanelChrome.checkbox(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(enabledOnlyBounds.x, enabledOnlyBounds.y + 5f, 14f, 14f),
                    filterEnabledOnly);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(shapeRenderer, new com.badlogic.gdx.math.Rectangle(PADDING, PADDING,
                    screenWidth - PADDING * 2f, screenHeight - PADDING * 2f));
            EditorPanelChrome.border(shapeRenderer, searchBounds);
            EditorPanelChrome.border(shapeRenderer,
                    new com.badlogic.gdx.math.Rectangle(enabledOnlyBounds.x, enabledOnlyBounds.y + 5f, 14f, 14f));
            shapeRenderer.end();

            batch.setProjectionMatrix(batch.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight));
            batch.begin();
            float oldScaleX = font.getData().scaleX;
            float oldScaleY = font.getData().scaleY;
            font.getData().setScale(1f);
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Player Above Decor", PADDING + 18f, screenHeight - PADDING - 20f);
            font.setColor(UI_TEXT_MUTED);
            font.draw(batch, "Sprites checked here are always rendered behind the player/entities",
                    PADDING + 18f, screenHeight - PADDING - 42f);

            font.setColor(searchFilter.isBlank() ? UI_TEXT_MUTED : UI_TEXT);
            font.draw(batch, searchFilter.isBlank() ? "Search sprite..." : searchFilter, searchBounds.x + 11f,
                    searchBounds.y + 22f);
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, filterEnabledOnly ? "x" : "", enabledOnlyBounds.x + 3f, enabledOnlyBounds.y + 19f);
            font.setColor(UI_TEXT_MUTED);
            font.draw(batch, "Enabled only", enabledOnlyBounds.x + 22f, enabledOnlyBounds.y + 19f);
            drawRuleDetails(batch);
            font.getData().setScale(oldScaleX, oldScaleY);
            batch.end();

            decorSpriteListBox.render(batch, shapeRenderer, font);
            btnToggle.withFont(font).render(batch, shapeRenderer);
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT) {
                return true;
            }
            float x = screenX;
            float y = Gdx.graphics.getHeight() - screenY;
            computeLayout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int ix = (int) x;
            int iy = (int) y;
            if (btnToggle.handleClick(ix, iy, button)) return true;
            if (enabledOnlyBounds.contains(x, y)) {
                filterEnabledOnly = !filterEnabledOnly;
                applyFilter();
                return true;
            }
            if (decorSpriteListBox.handleClick(ix, iy, button)) {
                int idx = decorSpriteListBox.selectedIndex();
                if (idx >= 0 && idx < filteredSprites.size()) {
                    selectedSprite = filteredSprites.get(idx);
                    scrollOffset = decorSpriteListBox.scrollOffset();
                    if (x <= listBounds.x + 34f) {
                        toggleSelected();
                    }
                }
                return true;
            }
            return true;
        }

        public boolean handleScroll(float amount) {
            decorSpriteListBox.scroll(amount > 0 ? 3 : -3);
            scrollOffset = decorSpriteListBox.scrollOffset();
            return true;
        }

        public boolean handleKeyTyped(char character) {
            if (character == '\b') {
                if (!searchFilter.isEmpty()) {
                    searchFilter = searchFilter.substring(0, searchFilter.length() - 1);
                    applyFilter();
                }
                return true;
            }
            if (character == '\r' || character == '\n') {
                toggleSelected();
                return true;
            }
            if (character >= 32 && character != 127) {
                searchFilter += character;
                applyFilter();
                return true;
            }
            return true;
        }

        private void computeLayout(int screenWidth, int screenHeight) {
            float panelLeft = PADDING;
            float panelBottom = PADDING;
            float panelRight = screenWidth - PADDING;
            float contentTop = screenHeight - PADDING - HEADER_HEIGHT - 16f;
            float footerTop = panelBottom + 58f;
            searchBounds.set(panelLeft + 18f, contentTop - SEARCH_HEIGHT, LIST_WIDTH - 36f, SEARCH_HEIGHT);
            enabledOnlyBounds.set(panelLeft + 18f, searchBounds.y - 32f, LIST_WIDTH - 36f, 24f);
            listBounds.set(panelLeft + 18f, footerTop + 14f, LIST_WIDTH - 36f, enabledOnlyBounds.y - footerTop - 24f);
            decorSpriteListBox.setBounds(listBounds.x, listBounds.y, listBounds.width, listBounds.height);
            decorSpriteListBox.setScrollOffset(scrollOffset);
            float detailsLeft = panelLeft + LIST_WIDTH + 28f;
            btnToggle.setBounds(detailsLeft, contentTop - 170f, panelRight - detailsLeft - 28f, 70f);
            toggleBounds.set(btnToggle.bounds());
        }

        private void drawRuleDetails(SpriteBatch batch) {
            if (selectedSprite == null) {
                return;
            }
            font.getData().setScale(1.0f);
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, selectedSprite.name, toggleBounds.x, toggleBounds.y + toggleBounds.height + 34f);
            font.getData().setScale(0.9f);
            font.setColor(hasRule(selectedSprite.name) ? UI_TEXT_LIGHT : UI_TEXT_MUTED);
            font.draw(batch, hasRule(selectedSprite.name)
                    ? "Enabled: player and entities stay above this decor"
                    : "Disabled: normal Y sorting can draw this decor above the player",
                    toggleBounds.x + 14f, toggleBounds.y + 42f);
            btnToggle.withLabel(hasRule(selectedSprite.name) ? "Disable" : "Enable");
            TextureRegion region = spriteLoader.getRegionFromSpriteName(selectedSprite.name);
            if (region != null) {
                float maxW = Math.min(220f, toggleBounds.width);
                float maxH = 220f;
                float scale = Math.min(maxW / Math.max(1f, region.getRegionWidth()),
                        maxH / Math.max(1f, region.getRegionHeight()));
                float w = region.getRegionWidth() * scale;
                float h = region.getRegionHeight() * scale;
                batch.setColor(1f, 1f, 1f, 0.85f);
                batch.draw(region, toggleBounds.x, toggleBounds.y - h - 30f, w, h);
                batch.setColor(Color.WHITE);
            }
        }

        private void toggleSelected() {
            if (selectedSprite == null) {
                return;
            }
            boolean enabled = hasRule(selectedSprite.name);
            removeRule(selectedSprite.name);
            if (!enabled) {
                ruleNames.add(selectedSprite.name);
            }
            decorLayerRulesDirty = true;
            dirty = true;
            applyFilter();
        }

        private boolean hasRule(String name) {
            String normalized = normalizeSpriteKey(name);
            for (String ruleName : ruleNames) {
                if (normalizeSpriteKey(ruleName).equals(normalized)) {
                    return true;
                }
            }
            return false;
        }

        private void removeRule(String name) {
            String normalized = normalizeSpriteKey(name);
            ruleNames.removeIf(ruleName -> normalizeSpriteKey(ruleName).equals(normalized));
        }

        private void applyFilter() {
            String filter = normalizeSpriteKey(searchFilter);
            filteredSprites = new ArrayList<>();
            for (SpritePickerUI.SpriteEntry entry : allSprites) {
                if ((!filterEnabledOnly || hasRule(entry.name))
                        && (filter.isBlank() || normalizeSpriteKey(entry.name).contains(filter))) {
                    filteredSprites.add(entry);
                }
            }
            scrollOffset = 0;
            if (!filteredSprites.contains(selectedSprite)) {
                selectedSprite = filteredSprites.isEmpty() ? null : filteredSprites.get(0);
            }
            decorSpriteListBox.setItems(filteredSprites);
            decorSpriteListBox.setScrollOffset(0);
        }

        public void close() {
            decorLayerRuleEditor = null;
        }
    }
    /**
     * Initialiser les boutons de la toolbar
     */
    private void initializeToolbarButtons() {
        toolbarButtons.clear();
        separatorPositions.clear();
        menuTitles.clear();
        buildEditorMenus();
        return;
    }

    private void buildEditorMenus() {
        MenuTitle file = new MenuTitle("File");
        file.items.add(new MenuItem("New Map", this::createNewMap));
        file.items.add(new MenuItem("Save", () -> {
            showEditorMessage("Saving...");
            allowManualSave = true;
            saveAllState();
            allowManualSave = false;
            showEditorMessage("Saved");
        }));
        file.items.add(new MenuItem("Reload Sprites", this::triggerSpriteHotReload));
        file.items.add(new MenuItem("Exit", () -> {
            requestExitWithSaveScreen();
        }));

        MenuTitle edit = new MenuTitle("Edit");
        edit.items.add(new MenuItem("Undo", this::performUndo));
        edit.items.add(new MenuItem("Copy", this::performCopy));
        edit.items.add(new MenuItem("Paste", this::performPaste));

        MenuTitle view = new MenuTitle("View");
        view.items.add(new MenuItem("Decorations", this::toggleDecorVisibility, () -> decorVisible));
        view.items.add(new MenuItem("Objects", this::toggleObjectsVisibility, () -> objectsVisible));
        view.items.add(new MenuItem("Collision Editor", this::toggleCollisionEditor,
                () -> editorMode == EditorMode.COLLISION_EDITOR));
        MenuItem collisionTypes = new MenuItem("Collision Type");
        for (CollisionType type : CollisionType.values()) {
            collisionTypes.add(new MenuItem(type.getValue() + " - " + type.getDisplayName(),
                    () -> selectCollisionValue(type.getValue()),
                    () -> selectedCollisionValue == type.getValue()));
        }
        view.items.add(collisionTypes);
        view.items.add(new MenuItem("Music Zones", this::toggleMusicZoneEditor,
                () -> editorMode == EditorMode.MUSIC_ZONE_EDITOR));
        view.items.add(new MenuItem("Teleport", this::toggleTeleportOverlay, () -> teleportOverlayVisible));

        MenuTitle tools = new MenuTitle("Tools");
        tools.items.add(new MenuItem("Autofill")
                .add(new MenuItem("Toggle", this::toggleAutofill, () -> autofillEnabled))
                .add(new MenuItem("Recalculate Map", this::recalculateAllGroundTextures))
                .add(new MenuItem("Repair Missing Ground", this::repairMissingGroundUnderDecors)));
        tools.items.add(new MenuItem("Rebuild Tmpl3")
                .add(new MenuItem("Whole Map", () -> buildSmoothingTiles(false), () -> tmpl3Regenerating))
                .add(new MenuItem("Viewport", () -> buildSmoothingTiles(true), () -> tmpl3Regenerating)));
        tools.items.add(new MenuItem("Rebuild Tmpl1")
                .add(new MenuItem("Whole Map", () -> rebuildAllTmpl1(false), () -> tmpl3Regenerating))
                .add(new MenuItem("Viewport", () -> rebuildAllTmpl1(true), () -> tmpl3Regenerating)));
        tools.items.add(new MenuItem("Rebuild Tmpl4")
                .add(new MenuItem("Whole Map", () -> rebuildAllTmpl4(false), () -> tmpl3Regenerating))
                .add(new MenuItem("Viewport", () -> rebuildAllTmpl4(true), () -> tmpl3Regenerating)));

        MenuTitle map = new MenuTitle("Map");
        for (int i = 0; i < availableMaps.size(); i++) {
            final int mapIndex = i;
            map.items.add(new MenuItem(getMapDisplayName(availableMaps.get(i)),
                    () -> selectMapByIndex(mapIndex),
                    () -> currentMapIndex == mapIndex));
        }
        if (map.items.isEmpty()) {
            map.items.add(new MenuItem("No maps found", null));
        }

        MenuTitle teleport = new MenuTitle("Teleport");
        teleport.items.add(new MenuItem("Go to Coordinates...", this::openTeleportToCoordinatesDialog));

        menuTitles.add(file);
        menuTitles.add(edit);
        menuTitles.add(view);
        menuTitles.add(tools);
        menuTitles.add(map);
        menuTitles.add(teleport);
    }

    private void openTeleportToCoordinatesDialog() {
        if (mapReader == null) {
            showEditorMessage("Map not loaded");
            return;
        }
        teleportGotoDialog = new TeleportGotoDialog(getCurrentMapZ(), this::applyTeleportGotoDialog, () -> {
            teleportGotoDialog = null;
        });
        openMenu = null;
    }

    private void openClanRelationsEditor() {
        clanRelationsEditor = new ClanRelationsEditorUI();
        openMenu = null;
    }

    private void openObjectMappingsEditor() {
        objectMappingsEditor = new ObjectMappingsEditorUI();
        openMenu = null;
    }

    private void openSpellEditor() {
        spellEditor = new SpellEditorUI();
        openMenu = null;
    }

    private void openMonsterDefEditor() {
        monsterDefEditor = new MonsterDefEditorUI();
        openMenu = null;
    }

    private void openNpcDefEditor() {
        npcDefEditor = new NpcDefEditorUI();
        openMenu = null;
    }

    private void openItemEditor() {
        itemEditor = new ItemEditorUI();
        openMenu = null;
    }

    private class SpellEditorUI extends EditorDialog {
        private static final int ROW_HEIGHT = 24;
        private static final int FIELD_HEIGHT = 27;
        private final List<SpellData> spells = new ArrayList<>();
        private final List<String> spriteNames = new ArrayList<>();
        private final List<String> filteredSpriteNames = new ArrayList<>();
        private final List<String> animationBaseNames = new ArrayList<>();
        private final List<String> filteredAnimationBaseNames = new ArrayList<>();
        private final Map<String, String> animationPreviewFrames = new HashMap<>();
        private final Map<String, List<String>> animationFramesByBase = new HashMap<>();
        private final Map<String, Sound> previewSoundCache = new HashMap<>();
        private final List<String> soundNames = new ArrayList<>();
        private final StringBuilder spriteFilter = new StringBuilder();
        private static final String[] EFFECT_TYPES = {"ATTRIBUTE", "SKILL", "REGENERATION", "IMMUNITY", "RESISTANCE"};
        private static final java.util.Map<String, String[]> EFFECT_ATTRIBUTES;
        static {
            java.util.Map<String, String[]> m = new java.util.LinkedHashMap<>();
            m.put("ATTRIBUTE", new String[]{"strength", "dexterity", "constitution", "intelligence", "wisdom",
                    "max hp", "max mp", "attack", "defense", "speed"});
            m.put("SKILL", new String[]{"sword", "axe", "mace", "dagger", "staff", "archery",
                    "shield", "dodge", "magic", "alchemy", "crafting"});
            m.put("REGENERATION", new String[]{"hp", "mp"});
            m.put("IMMUNITY", new String[]{"fire", "cold", "poison", "lightning", "physical", "magic"});
            m.put("RESISTANCE", new String[]{"fire", "cold", "poison", "lightning", "physical", "magic"});
            EFFECT_ATTRIBUTES = java.util.Collections.unmodifiableMap(m);
        }
        private final StringBuilder[] fields = new StringBuilder[16];
        private final List<SpellData.SpellEffect> buffEffects = new ArrayList<>();
        // Buff effect editor state
        private final EditorListBox<SpellData.SpellEffect> buffEffectList = new EditorListBox<SpellData.SpellEffect>()
                .rowHeight(ROW_HEIGHT)
                .labelProvider(e -> {
                    String t = e.getType() == null ? "?" : e.getType();
                    String a = e.getAttribute() == null ? "" : e.getAttribute();
                    String v = e.getAmount() == null ? "" : e.getAmount();
                    return t + " | " + a + (v.isEmpty() ? "" : " | " + v);
                });
        private final EditorDropdownList<String> buffEffectTypeDropdown = new EditorDropdownList<String>().visibleRows(5);
        private final EditorDropdownList<String> buffEffectAttrDropdown = new EditorDropdownList<String>().visibleRows(6);
        private final StringBuilder buffEffectAmountField = new StringBuilder();
        private final StringBuilder buffEffectDescField = new StringBuilder();
        private int buffEffectActiveSubField = 2; // 0=type(dropdown),1=attr(dropdown),2=amount
        // Buff effect UI bounds
        private com.badlogic.gdx.math.Rectangle buffEffectPanelBounds;
        private com.badlogic.gdx.math.Rectangle buffEffectTypeBounds;
        private com.badlogic.gdx.math.Rectangle buffEffectAttrBounds;
        private com.badlogic.gdx.math.Rectangle buffEffectAmountBounds;
        private boolean attack;
        private boolean lineOfSight;
        private boolean hasBuff;
        private boolean buffUnlimited;
        private int selectedIndex = -1;
        private int selectionAnchorIndex = -1;
        private final Set<Integer> selectedSpellIndices = new LinkedHashSet<>();
        private int scrollOffset;
        private int iconScrollOffset;
        private int activeField;
        private boolean dirty;
        private int openSoundDropdownField = -1;
        private int openSpriteDropdownField = -1;
        private boolean previewTab = false;
        private com.badlogic.gdx.math.Rectangle panelBounds;
        private com.badlogic.gdx.math.Rectangle listBounds;
        private com.badlogic.gdx.math.Rectangle iconGridBounds;
        private com.badlogic.gdx.math.Rectangle[] fieldBounds;
        private final EditorListBox<SpellData> spellList = new EditorListBox<SpellData>()
                .rowHeight(ROW_HEIGHT)
                .labelProvider(s -> s.getName() == null ? "" : s.getName())
                .colorProvider(i -> isSpellSelected(i) ? EditorTheme.BLUE : null);
        private final EditorDropdownList<String> soundDropdown = new EditorDropdownList<String>()
                .visibleRows(8)
                .labelProvider(v -> v == null || v.isEmpty() ? "None" : v)
                .onSelection(v -> {
                    if (openSoundDropdownField >= 0 && openSoundDropdownField < fields.length) {
                        set(fields[openSoundDropdownField], v == null ? "" : v);
                        playSelectedSound(v);
                        activeField = openSoundDropdownField;
                        autoSaveCurrentSpell();
                    }
                });
        private final EditorButton btnNew = new EditorButton("New", this::newSpell);
        private final EditorButton btnDelete = new EditorButton("Delete", this::deleteSelected);
        private final EditorButton btnBfAdd = new EditorButton("+", this::addBuffEffect);
        private final EditorButton btnBfDel = new EditorButton("-", this::deleteBuffEffect);
        private com.badlogic.gdx.math.Rectangle attackBounds;
        private com.badlogic.gdx.math.Rectangle losBounds;
        private com.badlogic.gdx.math.Rectangle buffBounds;
        private com.badlogic.gdx.math.Rectangle unlimitedBounds;
        private com.badlogic.gdx.math.Rectangle tabEditBounds;
        private com.badlogic.gdx.math.Rectangle tabPreviewBounds;
        private com.badlogic.gdx.math.Rectangle previewAreaBounds;

        SpellEditorUI() {
            super("Spell Editor");
            for (int i = 0; i < fields.length; i++) {
                fields[i] = new StringBuilder();
            }
            buffEffectTypeDropdown.setItems(java.util.Arrays.asList(EFFECT_TYPES));
            buffEffectTypeDropdown.onSelection(type -> {
                String[] attrs = EFFECT_ATTRIBUTES.get(type);
                buffEffectAttrDropdown.setItems(attrs != null ? java.util.Arrays.asList(attrs) : List.of());
            });
            buffEffectList.onSelection(eff -> {
                selectTypeInDropdown(eff.getType());
                selectAttrInDropdown(eff.getAttribute());
                buffEffectAmountField.setLength(0);
                if (eff.getAmount() != null) buffEffectAmountField.append(eff.getAmount());
                activeField = -1;
            });
            spells.addAll(SpellRegistry.load());
            spells.sort(Comparator.comparing(SpellData::getName, String.CASE_INSENSITIVE_ORDER));
            spellList.setItems(spells);
            for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
                if (sprite != null && sprite.getName() != null) {
                    spriteNames.add(sprite.getName());
                }
            }
            spriteNames.sort(String::compareToIgnoreCase);
            buildAnimationBaseNames();
            refreshSpriteFilter();
            File soundsDir = new File(Paths.SOUNDS_DIR);
            File[] files = soundsDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".wav"));
            if (files != null) {
                for (File file : files) {
                    soundNames.add(file.getName());
                }
            }
            soundNames.sort(String::compareToIgnoreCase);
            List<String> soundOptions = new ArrayList<>();
            soundOptions.add("");
            soundOptions.addAll(soundNames);
            soundDropdown.setItems(soundOptions);
            if (!spells.isEmpty()) {
                select(0);
            }
        }

        public void render(SpriteBatch batch, ShapeRenderer sr) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();
            layout(sw, sh);
            prepareUiProjection(sw, sh);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(sr, sw, sh);
            EditorPanelChrome.panel(sr, panelBounds, EditorTheme.ORANGE, 64f);
            // Tab backgrounds
            drawTabBackground(sr, tabEditBounds, !previewTab);
            drawTabBackground(sr, tabPreviewBounds, previewTab);
            if (!previewTab) {
                if (openSpriteDropdownField >= 0) EditorPanelChrome.surface(sr, iconGridBounds);
                for (int i = 0; i < fieldBounds.length; i++) {
                    EditorPanelChrome.textField(sr, fieldBounds[i], i == activeField);
                }
                EditorPanelChrome.checkbox(sr, attackBounds, attack);
                EditorPanelChrome.checkbox(sr, losBounds, lineOfSight);
                EditorPanelChrome.checkbox(sr, buffBounds, hasBuff);
                EditorPanelChrome.checkbox(sr, unlimitedBounds, buffUnlimited);
                // Buff effects edit field shapes (list rendered separately via EditorListBox)
                if (buffEffectPanelBounds != null) {
                    EditorPanelChrome.textField(sr, buffEffectTypeBounds, buffEffectActiveSubField == 0 && activeField < 0);
                    EditorPanelChrome.textField(sr, buffEffectAttrBounds, buffEffectActiveSubField == 1);
                    EditorPanelChrome.textField(sr, buffEffectAmountBounds, buffEffectActiveSubField == 2);
                }
            } else {
                EditorPanelChrome.surface(sr, previewAreaBounds);
            }
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(sr, panelBounds);
            if (!previewTab) {
                if (openSpriteDropdownField >= 0) EditorPanelChrome.border(sr, iconGridBounds);
                for (com.badlogic.gdx.math.Rectangle r : fieldBounds) {
                    EditorPanelChrome.border(sr, r);
                }
                if (buffEffectPanelBounds != null) {
                    EditorPanelChrome.border(sr, buffEffectTypeBounds);
                    EditorPanelChrome.border(sr, buffEffectAttrBounds);
                    EditorPanelChrome.border(sr, buffEffectAmountBounds);
                }
            } else {
                EditorPanelChrome.border(sr, previewAreaBounds);
            }
            sr.end();

            batch.setProjectionMatrix(batch.getProjectionMatrix().idt().setToOrtho2D(0, 0, sw, sh));
            batch.begin();
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Spell Editor", panelBounds.x + 18f, panelBounds.y + panelBounds.height - 18f);
            font.draw(batch, "Spells " + spells.size() + (dirty ? " *" : ""), listBounds.x, listBounds.y + listBounds.height + 18f);
            drawTabLabels(batch);
            if (!previewTab) {
                drawFields(batch);
                drawBuffEffectsPanel(batch, sr);
                if (openSpriteDropdownField >= 0) drawIcons(batch);
            } else {
                drawPreview(batch);
            }
            batch.end();

            spellList.render(batch, sr, font);
            if (!previewTab) {
                btnNew.withFont(font).render(batch, sr);
                btnDelete.withFont(font).render(batch, sr);
                btnBfAdd.withFont(font).render(batch, sr);
                btnBfDel.withFont(font).render(batch, sr);
            }

            if (!previewTab) {
                if (soundDropdown.isOpen()) soundDropdown.renderDropdown(batch, sr, font);
                if (buffEffectPanelBounds != null) buffEffectList.render(batch, sr, font);
                if (buffEffectTypeDropdown.isOpen()) buffEffectTypeDropdown.renderDropdown(batch, sr, font);
                if (buffEffectAttrDropdown.isOpen()) buffEffectAttrDropdown.renderDropdown(batch, sr, font);
            }
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        private void drawTabBackground(ShapeRenderer sr, com.badlogic.gdx.math.Rectangle tab, boolean active) {
            if (tab == null) return;
            float alpha = active ? 0.85f : 0.45f;
            sr.setColor(EditorTheme.ORANGE.r, EditorTheme.ORANGE.g, EditorTheme.ORANGE.b, alpha);
            sr.rect(tab.x, tab.y, tab.width, tab.height);
        }

        private void drawTabLabels(SpriteBatch batch) {
            if (tabEditBounds == null || tabPreviewBounds == null) return;
            EditorPanelChrome.buttonText(batch, font, tabEditBounds, "Edit", !previewTab ? UI_TEXT_LIGHT : UI_TEXT);
            EditorPanelChrome.buttonText(batch, font, tabPreviewBounds, "Preview", previewTab ? UI_TEXT_LIGHT : UI_TEXT);
        }

        // Animation preview state: timer and frame index per animation base name
        private final java.util.Map<String, float[]> animState = new java.util.HashMap<>();
        private static final float ANIM_FRAME_DURATION = 0.10f;
        private static final float ANIM_DISPLAY_SIZE = 96f;

        private void drawPreview(SpriteBatch batch) {
            if (previewAreaBounds == null) return;
            SpellData spell = selectedIndex >= 0 && selectedIndex < spells.size() ? spells.get(selectedIndex) : null;
            if (spell == null) {
                font.setColor(UI_TEXT);
                font.draw(batch, "No spell selected", previewAreaBounds.x + 20f, previewAreaBounds.y + previewAreaBounds.height - 20f);
                return;
            }

            float delta = Gdx.graphics.getDeltaTime();
            float px = previewAreaBounds.x + 16f;
            float py = previewAreaBounds.y + previewAreaBounds.height - 16f;
            float contentWidth = previewAreaBounds.width - 32f;

            // --- Icon (large, top-left) ---
            float iconSize = 64f;
            TextureRegion iconRegion = null;
            if (spell.getIconId() != null && !spell.getIconId().isEmpty()) {
                try { iconRegion = spriteLoader.getRegionFromSpriteName(spell.getIconId()); } catch (Exception ignored) {}
            }
            if (iconRegion != null) {
                batch.draw(iconRegion, px, py - iconSize, iconSize, iconSize);
            } else {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "[no icon]", px + 8f, py - iconSize / 2f + 8f);
            }

            // --- Name (right of icon) ---
            float nameX = px + iconSize + 14f;
            font.setColor(UI_TEXT);
            font.draw(batch, spell.getName(), nameX, py - 10f);

            // Type badge
            float badgeY = py - 30f;
            String typeBadge = spell.isAttack() ? "Attack" : (spell.getBuff() != null ? "Buff" : "Utility");
            font.setColor(spell.isAttack() ? new com.badlogic.gdx.graphics.Color(0.8f, 0.2f, 0.1f, 1f)
                    : spell.getBuff() != null ? new com.badlogic.gdx.graphics.Color(0.1f, 0.55f, 0.2f, 1f)
                    : UI_TEXT_FAINT);
            font.draw(batch, typeBadge, nameX, badgeY);
            if (spell.isLineOfSight()) {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "  Line of Sight", nameX + 60f, badgeY);
            }

            py -= iconSize + 14f;

            // --- Description ---
            if (spell.getDescription() != null && !spell.getDescription().isEmpty()) {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "Description", px, py);
                py -= 18f;
                font.setColor(UI_TEXT);
                py = drawWrappedText(batch, spell.getDescription(), px + 8f, py, contentWidth - 8f);
                py -= 12f;
            }

            // --- Animations (animated previews) ---
            boolean hasProj   = spell.getProjectileSpell() != null && !spell.getProjectileSpell().isEmpty();
            boolean hasImpact = spell.getImpactSpell()    != null && !spell.getImpactSpell().isEmpty();
            if (hasProj || hasImpact) {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "Animations", px, py);
                py -= 22f;
                float blockGap = 24f;
                float blockWidth = hasProj && hasImpact ? (contentWidth - blockGap) * 0.5f : Math.min(260f, contentWidth);
                float blockHeight = 132f;
                if (hasProj) {
                    String baseName = spell.getProjectileSpell();
                    String shortName = baseName.startsWith("64kSpell") ? baseName.substring(8) : baseName;
                    drawAnimationBlock(batch, "Projectile", shortName, baseName, px + 8f, py, blockWidth, blockHeight, delta);
                }
                if (hasImpact) {
                    String baseName = spell.getImpactSpell();
                    String shortName = baseName.startsWith("64kSpell") ? baseName.substring(8) : baseName;
                    float ix = hasProj ? px + 8f + blockWidth + blockGap : px + 8f;
                    drawAnimationBlock(batch, "Impact", shortName, baseName, ix, py, blockWidth, blockHeight, delta);
                }
                py -= blockHeight + 12f;
            }

            // --- Buff Effects ---
            if (spell.getBuff() != null) {
                SpellData.SpellBuff buff = spell.getBuff();
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "Buff", px, py);
                py -= 18f;
                font.setColor(UI_TEXT);
                String durText = buff.getUnlimited() != null && buff.getUnlimited() ? "Unlimited"
                        : buff.getDurationSeconds() != null ? buff.getDurationSeconds() + "s" : "?";
                font.draw(batch, "Duration: " + durText, px + 8f, py);
                py -= 20f;
                if (buff.getEffects() != null) {
                    for (SpellData.SpellEffect eff : buff.getEffects()) {
                        if (py < previewAreaBounds.y + 10f) break;
                        String line = eff.getType() + " " + nvl(eff.getAttribute(), "") + " " + nvl(eff.getAmount(), "");
                        if (eff.getDescription() != null && !eff.getDescription().isEmpty()) {
                            line += "  — " + eff.getDescription();
                        }
                        font.setColor(UI_TEXT);
                        font.draw(batch, fitText(line.trim(), contentWidth - 16f), px + 8f, py);
                        py -= 20f;
                    }
                }
            }
        }

        /** Collects sorted frame names for an animation base, cached per base name. */
        private final java.util.Map<String, List<String>> animFrameCache = new java.util.HashMap<>();

        private static final java.util.regex.Pattern ANGLE_FRAME_PATTERN =
                java.util.regex.Pattern.compile("^(.+?)(\\d{2,3})-(\\w)$", java.util.regex.Pattern.CASE_INSENSITIVE);

        /** Returns all frames for baseName, trying multiple naming conventions. */
        private List<String> getAnimFrames(String baseName) {
            return animFrameCache.computeIfAbsent(baseName, base -> {
                String lower = base.toLowerCase(Locale.ROOT);
                List<String> frames = new ArrayList<>();

                // Build candidate prefixes to try, in priority order
                String nospace = lower.replace(" ", "");
                String[] prefixes = lower.startsWith("64kspell")
                    ? new String[]{ lower, lower.substring("64kspell".length()) }
                    : new String[]{ lower, "64kspell" + lower, nospace, "64kspell" + nospace };

                for (String prefix : prefixes) {
                    for (String s : spriteNames) {
                        String sl = s.toLowerCase(Locale.ROOT);
                        if (!sl.startsWith(prefix)) continue;
                        String rest = sl.substring(prefix.length());
                        // Format 1: base + "-" + number + letter  (e.g. "Base-1a")
                        // Format 2: base + NNN + "-" + letter      (e.g. "FireBolt000-a")
                        if (rest.matches("-\\d*[a-zA-Z]") || rest.matches("\\d{2,3}-[a-zA-Z]")) {
                            frames.add(s);
                        }
                    }
                    if (!frames.isEmpty()) break;
                }

                // Fallback: exact name match (static sprite)
                if (frames.isEmpty()) {
                    for (String s : spriteNames) {
                        if (s.equalsIgnoreCase(base)) { frames.add(s); break; }
                    }
                }

                frames.sort((a, b) -> {
                    int[] ka = parseFrameKey2(a);
                    int[] kb = parseFrameKey2(b);
                    int cmp = Integer.compare(ka[0], kb[0]);
                    return cmp != 0 ? cmp : Integer.compare(ka[1], kb[1]);
                });
                return frames;
            });
        }

        /** Parses sort key [angle, frameIndex] from both "Base-Na" and "BaseNNN-a" formats. */
        private int[] parseFrameKey2(String spriteName) {
            java.util.regex.Matcher m = ANGLE_FRAME_PATTERN.matcher(spriteName);
            if (!m.matches()) return new int[]{0, 0};
            int angle = 0;
            try { angle = Integer.parseInt(m.group(2)); } catch (NumberFormatException ignored) {}
            int frame = Character.toLowerCase(m.group(3).charAt(0)) - 'a';
            return new int[]{angle, frame};
        }

        /** Returns frames for a specific angle (e.g. 0, 45, 90...), or all frames if no angles exist. */
        private List<String> getFramesForAngle(String baseName, int angleDeg) {
            List<String> all = getAnimFrames(baseName);
            String angleStr = String.format("%03d", angleDeg);
            List<String> angled = new ArrayList<>();
            for (String s : all) {
                java.util.regex.Matcher m = ANGLE_FRAME_PATTERN.matcher(s);
                if (m.matches() && m.group(2).equals(angleStr)) angled.add(s);
            }
            return angled.isEmpty() ? all : angled;
        }

        /** Returns the set of distinct angles present in the frame list, sorted. */
        private List<Integer> getAngles(String baseName) {
            java.util.TreeSet<Integer> angles = new java.util.TreeSet<>();
            for (String s : getAnimFrames(baseName)) {
                java.util.regex.Matcher m = ANGLE_FRAME_PATTERN.matcher(s);
                if (m.matches()) {
                    try { angles.add(Integer.parseInt(m.group(2))); } catch (NumberFormatException ignored) {}
                }
            }
            return new ArrayList<>(angles);
        }

        private int[] parseFrameOrder(String base, String spriteName) {
            if (spriteName.length() <= base.length()
                    || !spriteName.regionMatches(true, 0, base, 0, base.length())) {
                return new int[]{0, 0};
            }
            String suffix = spriteName.substring(base.length()); // e.g. "-2a"
            if (suffix.length() < 2 || suffix.charAt(0) != '-') return new int[]{0, 0};
            int i = 1, num = 0;
            while (i < suffix.length() && Character.isDigit(suffix.charAt(i))) {
                num = num * 10 + (suffix.charAt(i) - '0'); i++;
            }
            int letter = i < suffix.length() ? Character.toLowerCase(suffix.charAt(i)) - 'a' : 0;
            return new int[]{num == 0 ? 1 : num, letter};
        }

        private void drawAnimationBlock(SpriteBatch batch, String label, String displayName, String baseName,
                                        float x, float y, float width, float height, float delta) {
            font.setColor(UI_TEXT);
            font.draw(batch, label + ": " + fitText(displayName, width - 8f), x, y);
            drawSingleAnimFrame(batch, baseName, x, y - 24f, width, height - 30f, delta);
        }

        private void drawSingleAnimFrame(SpriteBatch batch, String baseName, float x, float y,
                                         float width, float height, float delta) {
            List<String> frames = getAnimFrames(baseName);
            if (frames.isEmpty()) {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, fitText("No frames: " + baseName, width), x, y - 22f);
                return;
            }

            List<Integer> angles = getAngles(baseName);
            if (angles.isEmpty()) {
                // No angle format — single animation
                drawAnimCell(batch, baseName, frames, x + (width - ANIM_DISPLAY_SIZE) * 0.5f,
                        y - (height - ANIM_DISPLAY_SIZE) * 0.5f, ANIM_DISPLAY_SIZE, ANIM_DISPLAY_SIZE, delta);
                return;
            }

            // Grid: up to 4 columns, rows as needed
            int cols = Math.min(4, angles.size());
            int rows = (int) Math.ceil(angles.size() / (float) cols);
            float cellSize = Math.min(ANIM_DISPLAY_SIZE, Math.min(width / cols, height / rows));
            float gridW = cols * cellSize;
            float gridH = rows * cellSize;
            float startX = x + (width - gridW) * 0.5f;
            float startY = y - (height - gridH) * 0.5f;

            for (int i = 0; i < angles.size(); i++) {
                int angle = angles.get(i);
                int col = i % cols;
                int row = i / cols;
                float cx = startX + col * cellSize;
                float cy = startY - row * cellSize;
                List<String> angleFrames = getFramesForAngle(baseName, angle);
                drawAnimCell(batch, baseName + "_" + angle, angleFrames, cx, cy, cellSize, cellSize, delta);
            }
        }

        private void drawAnimCell(SpriteBatch batch, String stateKey, List<String> frames,
                                   float x, float y, float cellW, float cellH, float delta) {
            if (frames.isEmpty()) return;
            float[] state = animState.computeIfAbsent(stateKey, k -> new float[]{0f, 0f});
            state[0] += delta;
            if (state[0] >= ANIM_FRAME_DURATION) {
                state[0] -= ANIM_FRAME_DURATION;
                state[1] = (state[1] + 1) % frames.size();
            }
            String frameName = frames.get((int) state[1]);
            try {
                TextureRegion region = spriteLoader.getRegionFromSpriteName(frameName);
                if (region != null) {
                    float fw = region.getRegionWidth();
                    float fh = region.getRegionHeight();
                    float scale = Math.min(cellW / Math.max(fw, 1f), cellH / Math.max(fh, 1f));
                    float dw = fw * scale;
                    float dh = fh * scale;
                    batch.draw(region, x + (cellW - dw) * 0.5f, y - cellH + (cellH - dh) * 0.5f, dw, dh);
                }
            } catch (Exception ignored) {}
        }

        private float drawWrappedText(SpriteBatch batch, String text, float x, float y, float maxWidth) {
            if (text == null || text.isEmpty()) return y;
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                GlyphLayout gl = new GlyphLayout(font, candidate);
                if (gl.width > maxWidth && line.length() > 0) {
                    font.draw(batch, line.toString(), x, y);
                    y -= 18f;
                    line.setLength(0);
                    line.append(word);
                } else {
                    line.setLength(0);
                    line.append(candidate);
                }
            }
            if (line.length() > 0) {
                font.draw(batch, line.toString(), x, y);
                y -= 18f;
            }
            return y;
        }

        private String nvl(String value, String fallback) {
            return (value == null || value.isEmpty()) ? fallback : value;
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT) {
                return true;
            }
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int y = Gdx.graphics.getHeight() - screenY;
            if (!panelBounds.contains(screenX, y)) {
                return true;
            }
            // Tab clicks
            if (tabEditBounds != null && tabEditBounds.contains(screenX, y)) {
                previewTab = false;
                return true;
            }
            if (tabPreviewBounds != null && tabPreviewBounds.contains(screenX, y)) {
                previewTab = true;
                return true;
            }
            if (spellList.handleClick(screenX, y, button)) {
                scrollOffset = spellList.scrollOffset();
                int index = spellList.selectedIndex();
                if (index >= 0 && index < spells.size()) {
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                        selectRange(index);
                    } else {
                        select(index);
                    }
                }
                return true;
            }
            if (previewTab) return true;
            if (openSoundDropdownField >= 0 && soundDropdown.isOpen() && soundDropdown.handleClick(screenX, y, button)) {
                openSoundDropdownField = -1;
                return true;
            }
            for (int i = 0; i < fieldBounds.length; i++) {
                if (fieldBounds[i].contains(screenX, y)) {
                    activeField = i;
                    openSoundDropdownField = (isSoundField(i) && openSoundDropdownField == i) ? -1 : (isSoundField(i) ? i : -1);
                    if (isSpriteField(i)) {
                        openSpriteDropdownField = i;
                        spriteFilter.setLength(0);
                        refreshSpriteFilter();
                    } else {
                        openSpriteDropdownField = -1;
                    }
                    return true;
                }
            }
            openSoundDropdownField = -1;
            if (iconGridBounds.contains(screenX, y)) {
                int iconIndex = iconAt(screenX, y);
                if (iconIndex >= 0 && iconIndex < spriteOptionCount()) {
                    int targetField = openSpriteDropdownField >= 0 ? openSpriteDropdownField : 8;
                    String selectedSprite = spriteOptionValueAt(iconIndex);
                    set(fields[targetField], isAnimationSpriteField(targetField) ? animationBaseName(selectedSprite) : selectedSprite);
                    activeField = targetField;
                    openSpriteDropdownField = -1;
                    autoSaveCurrentSpell();
                }
                return true;
            }
            // Buff effect dropdowns
            if (buffEffectTypeDropdown.isOpen() && buffEffectTypeDropdown.contains(screenX, y)) {
                buffEffectTypeDropdown.handleClick(screenX, y, button);
                buffEffectTypeDropdown.setOpen(false);
                autoSaveCurrentSpell();
                return true;
            }
            if (buffEffectAttrDropdown.isOpen() && buffEffectAttrDropdown.contains(screenX, y)) {
                buffEffectAttrDropdown.handleClick(screenX, y, button);
                buffEffectAttrDropdown.setOpen(false);
                autoSaveCurrentSpell();
                return true;
            }
            buffEffectTypeDropdown.setOpen(false);
            buffEffectAttrDropdown.setOpen(false);
            // Buff effect list
            if (buffEffectList.handleClick(screenX, y, button)) {
                activeField = -1;
                return true;
            }
            // Buff effect edit fields
            if (buffEffectTypeBounds != null && buffEffectTypeBounds.contains(screenX, y)) {
                activeField = -1;
                buffEffectActiveSubField = 0;
                buffEffectTypeDropdown.setOpen(!buffEffectTypeDropdown.isOpen());
                return true;
            }
            if (buffEffectAttrBounds != null && buffEffectAttrBounds.contains(screenX, y)) {
                activeField = -1;
                buffEffectActiveSubField = 1;
                buffEffectAttrDropdown.setOpen(!buffEffectAttrDropdown.isOpen());
                return true;
            }
            if (buffEffectAmountBounds != null && buffEffectAmountBounds.contains(screenX, y)) {
                activeField = -1; buffEffectActiveSubField = 2; return true;
            }
            if (btnBfAdd.handleClick(screenX, y, button)) return true;
            if (btnBfDel.handleClick(screenX, y, button)) return true;
            if (attackBounds.contains(screenX, y)) {
                attack = !attack;
                autoSaveCurrentSpell();
            } else if (losBounds.contains(screenX, y)) {
                lineOfSight = !lineOfSight;
                autoSaveCurrentSpell();
            } else if (buffBounds.contains(screenX, y)) {
                hasBuff = !hasBuff;
                autoSaveCurrentSpell();
            } else if (unlimitedBounds.contains(screenX, y)) {
                buffUnlimited = !buffUnlimited;
                autoSaveCurrentSpell();
            }
            if (btnNew.handleClick(screenX, y, button)) return true;
            if (btnDelete.handleClick(screenX, y, button)) return true;
            return true;
        }

        public boolean handleScroll(float amount) {
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int mx = Gdx.input.getX();
            int my = Gdx.graphics.getHeight() - Gdx.input.getY();
            int delta = amount > 0 ? 3 : -3;
            if (openSoundDropdownField >= 0 && soundDropdown.isOpen() && soundDropdown.contains(mx, my)) {
                soundDropdown.setScrollOffset(soundDropdown.scrollOffset() + delta);
            } else if (buffEffectAttrDropdown.isOpen() && buffEffectAttrDropdown.contains(mx, my)) {
                buffEffectAttrDropdown.setScrollOffset(buffEffectAttrDropdown.scrollOffset() + delta);
            } else if (buffEffectTypeDropdown.isOpen() && buffEffectTypeDropdown.contains(mx, my)) {
                buffEffectTypeDropdown.setScrollOffset(buffEffectTypeDropdown.scrollOffset() + delta);
            } else if (buffEffectList.contains(mx, my)) {
                buffEffectList.scroll(delta);
            } else if (iconGridBounds.contains(mx, my)) {
                int max = Math.max(0, spriteOptionCount() - visibleIconCount());
                iconScrollOffset = Math.max(0, Math.min(max, iconScrollOffset + delta * 4));
            } else {
                spellList.scroll(delta);
                scrollOffset = spellList.scrollOffset();
            }
            return true;
        }

        public boolean handleKeyTyped(char character) {
            if (openSpriteDropdownField >= 0) {
                if (character >= 32 && character != 127) {
                    spriteFilter.append(character);
                    refreshSpriteFilter();
                }
                return true;
            }
            if (openSoundDropdownField >= 0 && soundDropdown.isOpen()) {
                boolean handled = soundDropdown.handleKeyTyped(character);
                if (handled) autoSaveCurrentSpell();
                return handled;
            }
            if (buffEffectTypeDropdown.isOpen()) {
                boolean handled = buffEffectTypeDropdown.handleKeyTyped(character);
                if (handled) autoSaveCurrentSpell();
                return handled;
            }
            if (buffEffectAttrDropdown.isOpen()) {
                boolean handled = buffEffectAttrDropdown.handleKeyTyped(character);
                if (handled) autoSaveCurrentSpell();
                return handled;
            }
            if (activeField < 0) {
                // Typing into a buff effect sub-field
                if (character >= 32 && character != 127) {
                    buffEffectSubField().append(character);
                    autoSaveCurrentSpell();
                }
                return true;
            }
            if (openSoundDropdownField >= 0 || isSoundField(activeField)) {
                return true;
            }
            if (character >= 32 && character != 127) {
                fields[activeField].append(character);
                autoSaveCurrentSpell();
            }
            return true;
        }

        private StringBuilder buffEffectSubField() {
            // 0=type(dropdown), 1=attr(dropdown), 2=amount(text), 3=desc(unused)
            return buffEffectAmountField;
        }

        public boolean handleKeyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                if (openSoundDropdownField >= 0) {
                    openSoundDropdownField = -1;
                } else if (openSpriteDropdownField >= 0) {
                    openSpriteDropdownField = -1;
                } else if (buffEffectTypeDropdown.isOpen()) {
                    buffEffectTypeDropdown.setOpen(false);
                } else if (buffEffectAttrDropdown.isOpen()) {
                    buffEffectAttrDropdown.setOpen(false);
                } else {
                    close();
                }
            } else if ((openSoundDropdownField >= 0 && soundDropdown.isOpen())
                    || buffEffectTypeDropdown.isOpen()
                    || buffEffectAttrDropdown.isOpen()) {
                if (keycode == Input.Keys.ENTER) {
                    openSoundDropdownField = -1;
                    buffEffectTypeDropdown.setOpen(false);
                    buffEffectAttrDropdown.setOpen(false);
                }
                return true;
            } else if (keycode == Input.Keys.TAB) {
                activeField = (activeField + 1) % fields.length;
                openSoundDropdownField = -1;
                openSpriteDropdownField = -1;
            } else if ((keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL)
                    && selectedSpellIndices.size() > 1) {
                deleteSelected();
            } else if (keycode == Input.Keys.BACKSPACE || keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                if (activeField < 0) {
                    StringBuilder sf = buffEffectSubField();
                    if (sf.length() > 0) {
                        sf.deleteCharAt(sf.length() - 1);
                        autoSaveCurrentSpell();
                    }
                    return true;
                }
                if (openSpriteDropdownField >= 0) {
                    if ((keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL)
                            && isAnimationSpriteField(openSpriteDropdownField)) {
                        fields[openSpriteDropdownField].setLength(0);
                        openSpriteDropdownField = -1;
                        autoSaveCurrentSpell();
                    } else if (spriteFilter.length() > 0) {
                        spriteFilter.deleteCharAt(spriteFilter.length() - 1);
                        refreshSpriteFilter();
                    }
                    return true;
                }
                if (activeField < 0 || activeField >= fields.length) return true;
                if (isAnimationSpriteField(activeField)) {
                    fields[activeField].setLength(0);
                    autoSaveCurrentSpell();
                    return true;
                }
                if (isSoundField(activeField)) {
                    fields[activeField].setLength(0);
                    autoSaveCurrentSpell();
                    return true;
                }
                StringBuilder field = fields[activeField];
                if (field.length() > 0) {
                    field.deleteCharAt(field.length() - 1);
                    autoSaveCurrentSpell();
                }
            }
            return true;
        }

        public void close() {
            disposePreviewSounds();
            spellEditor = null;
        }

        private void newSpell() {
            clearFields();
            set(fields[0], "New Spell");
            set(fields[2], "0");
            set(fields[8], spriteNames.isEmpty() ? "" : spriteNames.get(0));
            selectedIndex = -1;
            selectionAnchorIndex = -1;
            selectedSpellIndices.clear();
            activeField = 0;
            autoSaveCurrentSpell();
        }

        private void autoSaveCurrentSpell() {
            SpellData spell = fromForm();
            if (spell.getName().isBlank()) {
                dirty = true;
                return;
            }
            if (selectedIndex >= 0 && selectedIndex < spells.size()) {
                spells.set(selectedIndex, spell);
            } else {
                spells.add(spell);
            }
            spells.sort(Comparator.comparing(SpellData::getName, String.CASE_INSENSITIVE_ORDER));
            spellList.setItems(spells);
            selectedIndex = Math.max(0, spells.indexOf(spell));
            selectionAnchorIndex = selectedIndex;
            selectedSpellIndices.clear();
            if (selectedIndex >= 0) {
                selectedSpellIndices.add(selectedIndex);
                spellList.ensureVisible(selectedIndex);
            }
            save();
        }

        private void deleteSelected() {
            if (!hasSpellSelection()) {
                return;
            }
            List<Integer> indices = new ArrayList<>(selectedSpellIndices);
            if (indices.isEmpty() && selectedIndex >= 0) {
                indices.add(selectedIndex);
            }
            indices.removeIf(index -> index < 0 || index >= spells.size());
            indices.sort(Comparator.reverseOrder());
            for (int index : indices) {
                spells.remove(index);
            }
            spellList.setItems(spells);
            int nextIndex = indices.isEmpty() ? selectedIndex : Math.min(indices.get(indices.size() - 1), spells.size() - 1);
            selectedSpellIndices.clear();
            selectedIndex = nextIndex;
            if (selectedIndex >= 0) {
                select(selectedIndex);
            } else {
                selectionAnchorIndex = -1;
                clearFields();
            }
            save();
            showEditorMessage(indices.size() == 1 ? "Spell deleted" : indices.size() + " spells deleted");
        }

        private void save() {
            try {
                SpellRegistry.save(spells);
                dirty = false;
            } catch (Exception e) {
                log.error("Failed to save spells", e);
                showEditorMessage("Error: spells not saved");
            }
        }

        private void select(int index) {
            selectedIndex = index;
            selectionAnchorIndex = index;
            selectedSpellIndices.clear();
            selectedSpellIndices.add(index);
            loadSelectedSpellIntoForm(index);
        }

        private void selectRange(int index) {
            if (selectionAnchorIndex < 0 || selectionAnchorIndex >= spells.size()) {
                selectionAnchorIndex = selectedIndex >= 0 ? selectedIndex : index;
            }
            int start = Math.min(selectionAnchorIndex, index);
            int end = Math.max(selectionAnchorIndex, index);
            selectedSpellIndices.clear();
            for (int i = start; i <= end; i++) {
                selectedSpellIndices.add(i);
            }
            selectedIndex = index;
            loadSelectedSpellIntoForm(index);
        }

        private boolean hasSpellSelection() {
            return !selectedSpellIndices.isEmpty()
                    || (selectedIndex >= 0 && selectedIndex < spells.size());
        }

        private boolean isSpellSelected(int index) {
            return selectedSpellIndices.contains(index)
                    || (selectedSpellIndices.isEmpty() && index == selectedIndex);
        }

        private void loadSelectedSpellIntoForm(int index) {
            animState.clear();
            SpellData s = spells.get(index);
            set(fields[0], s.getName());
            set(fields[1], s.getDescription());
            set(fields[2], s.getManaCost());
            set(fields[3], s.getRadius());
            set(fields[4], s.getMinInt());
            set(fields[5], s.getMinWis());
            set(fields[6], s.getMinLevel());
            set(fields[7], s.getCooldownSeconds());
            set(fields[8], s.getIconId());
            set(fields[9], s.getProjectileSpell());
            set(fields[10], s.getImpactSpell());
            set(fields[11], s.getMinDamage());
            set(fields[12], s.getMaxDamage());
            set(fields[13], s.getSound());
            set(fields[14], s.getSoundImpact());
            set(fields[15], s.getDuration());
            attack = s.isAttack();
            lineOfSight = s.isLineOfSight();
            hasBuff = s.getBuff() != null;
            buffUnlimited = s.getBuff() != null && Boolean.TRUE.equals(s.getBuff().getUnlimited());
            buffEffects.clear();
            if (s.getBuff() != null && s.getBuff().getEffects() != null) {
                buffEffects.addAll(s.getBuff().getEffects());
            }
            buffEffectList.setItems(new ArrayList<>(buffEffects));
            buffEffectList.clearSelection();
            buffEffectList.setScrollOffset(0);
            clearBuffEffectEditFields();
        }

        private SpellData fromForm() {
            SpellData.SpellBuff buff = null;
            if (hasBuff) {
                Integer dur = fields[15].toString().isBlank() ? null : parseInt(fields[15].toString(), 0);
                buff = new SpellData.SpellBuff(dur, buffUnlimited, new ArrayList<>(buffEffects));
            }
            return new SpellData(fields[0].toString().trim(), fields[1].toString().trim(), fields[2].toString().trim(),
                    parseInt(fields[3].toString(), 0), parseInt(fields[4].toString(), 0), parseInt(fields[5].toString(), 0),
                    parseInt(fields[6].toString(), 0), attack, lineOfSight, fields[8].toString().trim(),
                    emptyToNull(fields[9].toString()), emptyToNull(fields[10].toString()), parseInt(fields[11].toString(), 0),
                    parseInt(fields[12].toString(), 0), emptyToNull(fields[13].toString()), emptyToNull(fields[14].toString()),
                    parseInt(fields[7].toString(), 0), emptyToNull(fields[15].toString()), buff);
        }

        private void layout(int sw, int sh) {
            float w = Math.min(1160f, sw - 54f);
            float h = Math.min(720f, sh - 54f);
            float x = (sw - w) / 2f;
            float y = (sh - h) / 2f;
            panelBounds = new com.badlogic.gdx.math.Rectangle(x, y, w, h);
            listBounds = new com.badlogic.gdx.math.Rectangle(x + 18f, y + 58f, 235f, h - 116f);
            spellList.setBounds(listBounds.x, listBounds.y, listBounds.width, listBounds.height);
            spellList.setScrollOffset(scrollOffset);
            float fx = x + 275f;
            // Tabs are placed in the title bar row, to the right of "Spell Editor"
            float tabH = 26f;
            float tabW = 68f;
            float tabY = y + h - 38f;
            tabEditBounds    = new com.badlogic.gdx.math.Rectangle(fx, tabY, tabW, tabH);
            tabPreviewBounds = new com.badlogic.gdx.math.Rectangle(fx + tabW + 4f, tabY, tabW + 14f, tabH);
            // Fields layout unchanged from original
            float top = y + h - 98f;
            // Fields 0-7 and 11-16 in the regular 3-col grid (skipping 8,9,10)
            // Fields 8 (Icon), 9 (Projectile), 10 (Impact) get their own wide row between the grid and the checkboxes
            fieldBounds = new com.badlogic.gdx.math.Rectangle[fields.length];
            iconGridBounds = new com.badlogic.gdx.math.Rectangle(x + w - 230f, y + 92f, 210f, h - 190f);
            // Logical positions for grid fields (0-7 → slots 0-7, 11-15 → slots 8-12); field 16 removed (replaced by buff effects panel)
            int slot = 0;
            for (int i = 0; i < fields.length; i++) {
                if (i == 8 || i == 9 || i == 10) continue;
                int col = slot / 6;
                int row = slot % 6;
                fieldBounds[i] = new com.badlogic.gdx.math.Rectangle(fx + col * 220f, top - row * 55f, 200f, FIELD_HEIGHT);
                slot++;
            }
            // Buff Effects panel: col 2, rows 1-5 (below "Duration" which is col 2 row 0)
            // top - row*55 gives the Y of the text-field for each row; panel starts at row 1
            float bfx = fx + 2 * 220f;
            float bfW = 200f;
            float bfTop = top - 1 * 55f;       // top of row 1 field (y of the text field)
            float bfBot = top - 5 * 55f - 4f;  // bottom of row 5 (below the last row baseline)
            float bfPanelH = bfTop - bfBot + FIELD_HEIGHT;
            float bfPanelY = bfBot;
            buffEffectPanelBounds = new com.badlogic.gdx.math.Rectangle(bfx, bfPanelY, bfW, bfPanelH);

            // Bottom editing area: two rows of fields + buttons
            float editRowH = FIELD_HEIGHT + 16f;
            float editAreaH = editRowH * 2 + 6f;
            float halfW = (bfW - 4f) / 2f;
            // Row 1: Type | Attribute
            float editRow1Y = bfPanelY + editRowH + 6f + FIELD_HEIGHT;
            // Row 2: Amount | + | -
            float editRow2Y = bfPanelY + 4f + FIELD_HEIGHT;
            float btnW = 28f;
            float amountW = bfW - btnW * 2 - 8f;

            buffEffectTypeBounds   = new com.badlogic.gdx.math.Rectangle(bfx,              editRow1Y, halfW,    FIELD_HEIGHT);
            buffEffectAttrBounds   = new com.badlogic.gdx.math.Rectangle(bfx + halfW + 4f, editRow1Y, halfW,    FIELD_HEIGHT);
            buffEffectAmountBounds = new com.badlogic.gdx.math.Rectangle(bfx,              editRow2Y, amountW,  FIELD_HEIGHT);
            btnBfAdd.setBounds(bfx + amountW + 4f,             editRow2Y, btnW, FIELD_HEIGHT);
            btnBfDel.setBounds(bfx + amountW + 4f + btnW + 4f, editRow2Y, btnW, FIELD_HEIGHT);

            // List occupies the space above the editing area
            float listY = bfPanelY + editAreaH + editRowH + 8f;
            float listH = bfPanelH - editAreaH - editRowH - 8f;
            buffEffectList.setBounds(bfx, listY, bfW, listH);

            // Position EditorDropdownList bounds (dropdown opens downward)
            float dropRowH = buffEffectTypeDropdown.visibleRows() * ROW_HEIGHT + 4f;
            float typeDropY = Math.max(bfPanelY, buffEffectTypeBounds.y - dropRowH - 2f);
            buffEffectTypeDropdown.setBounds(bfx, typeDropY, halfW, dropRowH);

            float attrDropRows = Math.max(1, buffEffectAttrDropdown.items().size());
            float attrDropH = Math.min(attrDropRows, buffEffectAttrDropdown.visibleRows()) * ROW_HEIGHT + 4f;
            float attrDropY = Math.max(bfPanelY, buffEffectAttrBounds.y - attrDropH - 2f);
            buffEffectAttrDropdown.setBounds(bfx, attrDropY, bfW, attrDropH);
            // Wide sprite fields below the grid, above the checkboxes
            float spriteRowY = y + 128f;
            float spriteFieldW = (iconGridBounds.x - fx - 18f) / 3f - 8f;
            fieldBounds[8]  = new com.badlogic.gdx.math.Rectangle(fx,                        spriteRowY, spriteFieldW, FIELD_HEIGHT);
            fieldBounds[9]  = new com.badlogic.gdx.math.Rectangle(fx + spriteFieldW + 8f,    spriteRowY, spriteFieldW, FIELD_HEIGHT);
            fieldBounds[10] = new com.badlogic.gdx.math.Rectangle(fx + (spriteFieldW + 8f)*2, spriteRowY, spriteFieldW, FIELD_HEIGHT);
            attackBounds = new com.badlogic.gdx.math.Rectangle(fx, y + 94f, 24f, 24f);
            losBounds = new com.badlogic.gdx.math.Rectangle(fx + 112f, y + 94f, 24f, 24f);
            buffBounds = new com.badlogic.gdx.math.Rectangle(fx + 252f, y + 94f, 24f, 24f);
            unlimitedBounds = new com.badlogic.gdx.math.Rectangle(fx + 352f, y + 94f, 24f, 24f);
            previewAreaBounds = new com.badlogic.gdx.math.Rectangle(fx, y + 55f, w - (fx - x) - 18f, tabY - (y + 55f) - 4f);
            if (openSoundDropdownField >= 0 && !previewTab) {
                com.badlogic.gdx.math.Rectangle source = fieldBounds[openSoundDropdownField];
                float dropdownY = Math.max(y + 58f, source.y - 180f);
                soundDropdown.setBounds(source.x, dropdownY, source.width, 168f);
                soundDropdown.setOpen(true);
            } else {
                soundDropdown.setOpen(false);
            }
            btnNew.setBounds(fx, y + 55f, 62f, 30f);
            btnDelete.setBounds(fx + 72f, y + 55f, 76f, 30f);
            btnDelete.setEnabled(hasSpellSelection());
            btnBfDel.setEnabled(buffEffectList.selectedIndex() >= 0);
        }

        private void drawFields(SpriteBatch batch) {
            String[] labels = {"Name", "Description", "Mana Cost", "Radius", "Min Int", "Min Wis", "Min Level",
                    "Cooldown", "Icon", "Projectile", "Impact", "Min Damage", "Max Damage", "Cast Sound",
                    "Impact Sound", "Duration"};
            for (int i = 0; i < fieldBounds.length; i++) {
                com.badlogic.gdx.math.Rectangle r = fieldBounds[i];
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, labels[i], r.x, r.y + r.height + 14f);
                font.setColor(UI_TEXT);
                boolean dropdown = isSoundField(i) || isSpriteField(i);
                font.draw(batch, fitText(fields[i].toString(), r.width - (dropdown ? 32f : 16f)), r.x + 8f, r.y + 19f);
                if (dropdown) {
                    font.draw(batch, "v", r.x + r.width - 18f, r.y + 19f);
                }
            }
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Attack", attackBounds.x + 32f, attackBounds.y + 18f);
            font.draw(batch, "Line of Sight", losBounds.x + 32f, losBounds.y + 18f);
            font.draw(batch, "Buff", buffBounds.x + 32f, buffBounds.y + 18f);
            font.draw(batch, "Unlimited", unlimitedBounds.x + 32f, unlimitedBounds.y + 18f);
        }

        private void drawBuffEffectsPanel(SpriteBatch batch, ShapeRenderer sr) {
            if (buffEffectPanelBounds == null) return;
            // Background surface + border drawn in main render via shape renderer pass
            // Label
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, "Buff Effects (" + buffEffects.size() + ")",
                    buffEffectPanelBounds.x, buffEffectPanelBounds.y + buffEffectPanelBounds.height + 14f);
            // List rendered separately via EditorListBox after batch.end()
            // Edit row labels — same offset as other fields (height + 14f)
            font.setColor(UI_TEXT_FAINT);
            if (buffEffectTypeBounds != null)
                font.draw(batch, "Type", buffEffectTypeBounds.x, buffEffectTypeBounds.y + buffEffectTypeBounds.height + 14f);
            if (buffEffectAttrBounds != null)
                font.draw(batch, "Attribute", buffEffectAttrBounds.x, buffEffectAttrBounds.y + buffEffectAttrBounds.height + 14f);
            if (buffEffectAmountBounds != null)
                font.draw(batch, "Amount", buffEffectAmountBounds.x, buffEffectAmountBounds.y + buffEffectAmountBounds.height + 14f);
            // Edit field values
            font.setColor(UI_TEXT);
            String typeLabel = buffEffectTypeDropdown.selectedItem() != null ? buffEffectTypeDropdown.selectedItem() : "";
            String attrLabel = buffEffectAttrDropdown.selectedItem() != null ? buffEffectAttrDropdown.selectedItem() : "";
            if (buffEffectTypeBounds != null) {
                font.draw(batch, fitText(typeLabel, buffEffectTypeBounds.width - 28f),
                        buffEffectTypeBounds.x + 8f, buffEffectTypeBounds.y + 19f);
                font.draw(batch, "v", buffEffectTypeBounds.x + buffEffectTypeBounds.width - 18f, buffEffectTypeBounds.y + 19f);
            }
            if (buffEffectAttrBounds != null) {
                font.draw(batch, fitText(attrLabel, buffEffectAttrBounds.width - 28f),
                        buffEffectAttrBounds.x + 8f, buffEffectAttrBounds.y + 19f);
                font.draw(batch, "v", buffEffectAttrBounds.x + buffEffectAttrBounds.width - 18f, buffEffectAttrBounds.y + 19f);
            }
            if (buffEffectAmountBounds != null)
                font.draw(batch, fitText(buffEffectAmountField.toString(), buffEffectAmountBounds.width - 8f),
                        buffEffectAmountBounds.x + 8f, buffEffectAmountBounds.y + 19f);
        }

        private void drawIcons(SpriteBatch batch) {
            font.setColor(UI_TEXT_LIGHT);
            boolean animationPicker = isAnimationSpriteField(openSpriteDropdownField);
            float delta = Gdx.graphics.getDeltaTime();
            String title = openSpriteDropdownField == 9 ? "Projectile Animation" : openSpriteDropdownField == 10 ? "Impact Animation" : "Icon Sprite";
            font.draw(batch, title, iconGridBounds.x, iconGridBounds.y + iconGridBounds.height + 18f);
            font.setColor(UI_TEXT);
            font.draw(batch, fitText("Search: " + spriteFilter, iconGridBounds.width - 16f),
                    iconGridBounds.x + 8f, iconGridBounds.y + iconGridBounds.height - 8f);
            float cellW = animationPicker ? 92f : 44f;
            float cellH = animationPicker ? 74f : 48f;
            int cols = Math.max(1, (int) (iconGridBounds.width / cellW));
            float gridTop = iconGridBounds.y + iconGridBounds.height - 28f;
            int rows = Math.max(1, (int) ((iconGridBounds.height - 28f) / cellH));
            int end = Math.min(spriteOptionCount(), iconScrollOffset + cols * rows);
            for (int i = iconScrollOffset; i < end; i++) {
                int local = i - iconScrollOffset;
                float x = iconGridBounds.x + (local % cols) * cellW + 6f;
                float y = gridTop - (local / cols + 1) * cellH + 8f;
                String option = spriteOptionValueAt(i);
                if (option.isEmpty()) {
                    font.setColor(UI_TEXT);
                    font.draw(batch, "None", x, y + 22f);
                    continue;
                }
                try {
                    String spriteName = animationPicker ? animationPreviewFrame(option) : option;
                    TextureRegion region = spriteLoader.getRegionFromSpriteName(spriteName);
                    if (region != null) {
                        if (animationPicker) {
                            drawAnimationThumbnail(batch, region, option, x, y, cellW - 12f, cellH - 12f, delta);
                        } else {
                            batch.draw(region, x, y, 32f, 32f);
                        }
                    }
                } catch (GameException ignored) {
                }
            }
        }

        private void drawAnimationThumbnail(SpriteBatch batch, TextureRegion region, String baseName,
                                            float x, float y, float width, float height, float delta) {
            float labelH = 14f;
            float imageH = height - labelH;
            String frameName = animationCurrentFrame(baseName, delta);
            try {
                TextureRegion frameRegion = spriteLoader.getRegionFromSpriteName(frameName);
                if (frameRegion != null) {
                    region = frameRegion;
                }
            } catch (GameException ignored) {
            }
            float fw = region.getRegionWidth();
            float fh = region.getRegionHeight();
            float scale = Math.min(imageH / Math.max(fh, 1f), width / Math.max(fw, 1f));
            float dw = fw * scale;
            float dh = fh * scale;
            batch.draw(region, x + (width - dw) * 0.5f, y + labelH + (imageH - dh) * 0.5f, dw, dh);
            font.setColor(UI_TEXT);
            font.draw(batch, fitText(animationDisplayName(baseName), width), x, y + 10f);
        }

        private int iconAt(int x, int y) {
            float cellW = isAnimationSpriteField(openSpriteDropdownField) ? 92f : 44f;
            float cellH = isAnimationSpriteField(openSpriteDropdownField) ? 74f : 48f;
            int cols = Math.max(1, (int) (iconGridBounds.width / cellW));
            float gridTop = iconGridBounds.y + iconGridBounds.height - 28f;
            if (y >= gridTop) {
                return -1;
            }
            int col = (int) ((x - iconGridBounds.x) / cellW);
            int row = (int) ((gridTop - y) / cellH);
            return iconScrollOffset + row * cols + col;
        }

        private int visibleIconCount() {
            if (iconGridBounds == null) {
                return 1;
            }
            float cellW = isAnimationSpriteField(openSpriteDropdownField) ? 92f : 44f;
            float cellH = isAnimationSpriteField(openSpriteDropdownField) ? 74f : 48f;
            int cols = Math.max(1, (int) (iconGridBounds.width / cellW));
            int rows = Math.max(1, (int) ((iconGridBounds.height - 28f) / cellH));
            return cols * rows;
        }

        private int rowAt(com.badlogic.gdx.math.Rectangle bounds, int y) {
            return (int) ((bounds.y + bounds.height - y) / ROW_HEIGHT);
        }

        private int visibleRows(com.badlogic.gdx.math.Rectangle bounds) {
            return bounds == null ? 1 : Math.max(1, (int) (bounds.height / ROW_HEIGHT));
        }

        private boolean isSoundField(int field) {
            return field == 13 || field == 14;
        }

        private boolean isSpriteField(int field) {
            return field == 8 || field == 9 || field == 10;
        }

        private boolean isAnimationSpriteField(int field) {
            return field == 9 || field == 10;
        }

        private boolean hasEmptySpriteOption() {
            return isAnimationSpriteField(openSpriteDropdownField);
        }

        private int spriteOptionCount() {
            List<String> options = currentSpriteOptions();
            return options.size() + (hasEmptySpriteOption() ? 1 : 0);
        }

        private String spriteOptionValueAt(int index) {
            List<String> options = currentSpriteOptions();
            if (hasEmptySpriteOption()) {
                return index <= 0 ? "" : options.get(index - 1);
            }
            return options.get(index);
        }

        private List<String> currentSpriteOptions() {
            return isAnimationSpriteField(openSpriteDropdownField) ? filteredAnimationBaseNames : filteredSpriteNames;
        }

        private String animationBaseName(String spriteName) {
            if (spriteName == null) {
                return "";
            }
            // Format: BaseName-123A  (tiret + chiffres + lettre)
            java.util.regex.Matcher frame = java.util.regex.Pattern
                    .compile("^(.+)-\\d*[A-Za-z]$")
                    .matcher(spriteName);
            if (frame.matches()) return frame.group(1);
            // Format: BaseName000  (suffix purement numérique >= 3 chiffres)
            java.util.regex.Matcher digits = java.util.regex.Pattern
                    .compile("^(.+?)\\d{3,}$")
                    .matcher(spriteName);
            return digits.matches() ? digits.group(1) : spriteName;
        }

        private String animationDisplayName(String baseName) {
            if (baseName == null) {
                return "";
            }
            return baseName.startsWith("64kSpell") ? baseName.substring(8) : baseName;
        }

        private String animationPreviewFrame(String baseName) {
            String preview = animationPreviewFrames.get(baseName);
            if (preview != null) {
                return preview;
            }
            List<String> frames = getAnimFrames(baseName);
            return frames.isEmpty() ? baseName : frames.get(0);
        }

        private String animationCurrentFrame(String baseName, float delta) {
            List<String> frames = animationFramesByBase.get(baseName);
            if (frames == null) {
                frames = getAnimFrames(baseName);
                animationFramesByBase.put(baseName, frames);
            }
            if (frames.isEmpty()) {
                return baseName;
            }
            float[] state = animState.computeIfAbsent("picker:" + baseName, k -> new float[]{0f, 0f});
            state[0] += delta;
            while (state[0] >= ANIM_FRAME_DURATION) {
                state[0] -= ANIM_FRAME_DURATION;
                state[1] = (state[1] + 1) % frames.size();
            }
            return frames.get((int) state[1]);
        }

        private void buildAnimationBaseNames() {
            LinkedHashSet<String> bases = new LinkedHashSet<>();
            animationPreviewFrames.clear();
            animationFramesByBase.clear();
            for (String name : spriteNames) {
                String base = animationBaseName(name);
                if (!base.equals(name)) {
                    bases.add(base);
                    animationPreviewFrames.putIfAbsent(base, name);
                }
            }
            animationBaseNames.clear();
            animationBaseNames.addAll(bases);
            animationBaseNames.sort(String::compareToIgnoreCase);
        }

        private void refreshSpriteFilter() {
            filteredSpriteNames.clear();
            filteredAnimationBaseNames.clear();
            String filter = spriteFilter.toString().trim().toLowerCase(Locale.ROOT);
            for (String name : spriteNames) {
                if (filter.isEmpty() || name.toLowerCase(Locale.ROOT).contains(filter)) {
                    filteredSpriteNames.add(name);
                }
            }
            for (String name : animationBaseNames) {
                if (filter.isEmpty()
                        || name.toLowerCase(Locale.ROOT).contains(filter)
                        || animationDisplayName(name).toLowerCase(Locale.ROOT).contains(filter)) {
                    filteredAnimationBaseNames.add(name);
                }
            }
            int max = Math.max(0, spriteOptionCount() - visibleIconCount());
            iconScrollOffset = Math.max(0, Math.min(max, iconScrollOffset));
        }

        private void playSelectedSound(String soundName) {
            if (soundName == null || soundName.isBlank()) {
                return;
            }
            try {
                Sound sound = previewSoundCache.computeIfAbsent(soundName, name ->
                        Gdx.audio.newSound(Gdx.files.internal(Paths.SOUNDS_DIR + "/" + name)));
                sound.play(1f);
            } catch (Exception e) {
                log.warn("Failed to preview sound {}", soundName, e);
                SoundManager.animateSound(soundName);
            }
        }

        private void disposePreviewSounds() {
            for (Sound sound : previewSoundCache.values()) {
                if (sound != null) {
                    sound.dispose();
                }
            }
            previewSoundCache.clear();
        }

        private void clearFields() {
            for (StringBuilder field : fields) {
                field.setLength(0);
            }
            attack = false;
            lineOfSight = false;
            hasBuff = false;
            buffUnlimited = false;
            buffEffects.clear();
            buffEffectList.setItems(new ArrayList<>(buffEffects));
            buffEffectList.clearSelection();
            buffEffectList.setScrollOffset(0);
            clearBuffEffectEditFields();
        }

        private void addBuffEffect() {
            String type = buffEffectTypeDropdown.selectedItem() != null ? buffEffectTypeDropdown.selectedItem() : EFFECT_TYPES[0];
            String attr = buffEffectAttrDropdown.selectedItem() != null ? buffEffectAttrDropdown.selectedItem() : "";
            buffEffects.add(new SpellData.SpellEffect(type, attr, buffEffectAmountField.toString().trim(), ""));
            buffEffectList.setItems(new ArrayList<>(buffEffects));
            buffEffectList.select(buffEffects.size() - 1);
            autoSaveCurrentSpell();
        }

        private void deleteBuffEffect() {
            int idx = buffEffectList.selectedIndex();
            if (idx < 0 || idx >= buffEffects.size()) return;
            buffEffects.remove(idx);
            buffEffectList.setItems(new ArrayList<>(buffEffects));
            if (!buffEffects.isEmpty()) {
                int next = Math.min(idx, buffEffects.size() - 1);
                buffEffectList.select(next);
            } else {
                buffEffectList.clearSelection();
                clearBuffEffectEditFields();
            }
            autoSaveCurrentSpell();
        }

        private void selectTypeInDropdown(String type) {
            if (type == null) return;
            List<String> items = buffEffectTypeDropdown.items();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).equalsIgnoreCase(type)) { buffEffectTypeDropdown.select(i); return; }
            }
        }

        private void selectAttrInDropdown(String attr) {
            if (attr == null) return;
            List<String> items = buffEffectAttrDropdown.items();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).equalsIgnoreCase(attr)) { buffEffectAttrDropdown.select(i); return; }
            }
        }

        private void clearBuffEffectEditFields() {
            buffEffectAmountField.setLength(0);
            buffEffectDescField.setLength(0);
            buffEffectActiveSubField = 2;
            buffEffectTypeDropdown.setOpen(false);
            buffEffectAttrDropdown.setOpen(false);
            buffEffectTypeDropdown.clearSelection();
            buffEffectAttrDropdown.clearSelection();
        }

        private void set(StringBuilder builder, Object value) {
            builder.setLength(0);
            if (value != null) {
                builder.append(value);
            }
        }

        private int parseInt(String value, int fallback) {
            try {
                return Integer.parseInt(value.trim());
            } catch (Exception ignored) {
                return fallback;
            }
        }

        private String emptyToNull(String value) {
            String trimmed = value == null ? "" : value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }

        private String shortenText(String value, int max) {
            if (value == null) {
                return "";
            }
            return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "...";
        }

        private String fitText(String value, float maxWidth) {
            if (value == null || value.isEmpty()) {
                return "";
            }
            GlyphLayout layout = new GlyphLayout(font, value);
            if (layout.width <= maxWidth) {
                return value;
            }
            String suffix = "...";
            int end = value.length();
            while (end > 0) {
                String candidate = value.substring(0, end) + suffix;
                layout.setText(font, candidate);
                if (layout.width <= maxWidth) {
                    return candidate;
                }
                end--;
            }
            return "";
        }
    }

    private class ItemEditorUI extends EditorDialog {
        private static final float HEADER_HEIGHT = 56f;
        private static final float ROW_HEIGHT = 22f;
        private static final float FIELD_HEIGHT = 26f;
        private static final float TAB_HEIGHT = 26f;

        private final String[] fieldLabels = {
                "Key", "Display name", "Equipped sprite", "Secondary sprite", "Inventory sprite",
                "Price", "Weight", "Armor class", "Dodge lost", "Min END", "Req attack",
                "Req STR", "Req AGI", "Min INT", "Min WIS", "Attack speed"
        };
        private final StringBuilder[] fields = new StringBuilder[16];
        private final com.badlogic.gdx.math.Rectangle[] fieldBounds = new com.badlogic.gdx.math.Rectangle[16];
        private final List<ItemDefinition> defs = new ArrayList<>();
        private ItemDefinition clipboard;
        private int selectedIndex = -1;
        private int activeField = -1;
        private boolean previewTab;
        private boolean suppressPartSelection;
        private boolean unique;
        private boolean bow;
        private boolean unlimitedUse;

        private com.badlogic.gdx.math.Rectangle panelBounds;
        private com.badlogic.gdx.math.Rectangle listBounds;
        private com.badlogic.gdx.math.Rectangle tabEditBounds;
        private com.badlogic.gdx.math.Rectangle tabPreviewBounds;
        private com.badlogic.gdx.math.Rectangle primaryPartBounds;
        private com.badlogic.gdx.math.Rectangle secondaryPartBounds;
        private com.badlogic.gdx.math.Rectangle uniqueBounds;
        private com.badlogic.gdx.math.Rectangle bowBounds;
        private com.badlogic.gdx.math.Rectangle unlimitedBounds;
        private com.badlogic.gdx.math.Rectangle previewAreaBounds;
        private final GlyphLayout glyph = new GlyphLayout();

        private final EditorListBox<ItemDefinition> list = new EditorListBox<ItemDefinition>()
                .rowHeight(ROW_HEIGHT)
                .labelProvider(d -> d == null ? "" : d.getKey())
                .colorProvider(i -> i == selectedIndex ? EditorTheme.BLUE : null);
        private final EditorButton btnNew = new EditorButton("New", this::newDef);
        private final EditorButton btnDelete = new EditorButton("Delete", this::deleteSelected);
        private final EditorButton btnCopy = new EditorButton("Copy", this::copySelected);
        private final EditorButton btnPaste = new EditorButton("Paste", this::pasteClipboard);
        private final EditorDropdownList<BodyPart> primaryPartDropdown = newBodyPartDropdown();
        private final EditorDropdownList<BodyPart> secondaryPartDropdown = newBodyPartDropdown();

        ItemEditorUI() {
            super("Item Editor");
            for (int i = 0; i < fields.length; i++) {
                fields[i] = new StringBuilder();
                fieldBounds[i] = new com.badlogic.gdx.math.Rectangle();
            }
            List<BodyPart> bodyParts = new ArrayList<>();
            bodyParts.add(null);
            bodyParts.addAll(Arrays.asList(BodyPart.values()));
            primaryPartDropdown.setItems(bodyParts);
            secondaryPartDropdown.setItems(bodyParts);
            defs.addAll(ItemRegistry.load());
            defs.sort(Comparator.comparing(ItemDefinition::getKey, String.CASE_INSENSITIVE_ORDER));
            list.setItems(defs);
            if (!defs.isEmpty()) {
                select(0);
            }
        }

        private EditorDropdownList<BodyPart> newBodyPartDropdown() {
            return new EditorDropdownList<BodyPart>()
                    .visibleRows(8)
                    .labelProvider(p -> p == null ? "None" : p.name())
                    .onSelection(p -> {
                        if (!suppressPartSelection) autoSave();
                    });
        }

        private void layout(int sw, int sh) {
            float pw = Math.min(980f, sw - 40f);
            float ph = Math.min(720f, sh - 40f);
            float px = (sw - pw) * 0.5f;
            float py = (sh - ph) * 0.5f;
            panelBounds = new com.badlogic.gdx.math.Rectangle(px, py, pw, ph);
            float pad = 18f;
            float listW = 230f;
            float top = py + ph - HEADER_HEIGHT - 10f;
            listBounds = new com.badlogic.gdx.math.Rectangle(px + pad, py + 92f, listW, top - (py + 92f));
            list.setBounds(listBounds.x, listBounds.y, listBounds.width, listBounds.height);
            btnNew.setBounds(px + pad, py + 56f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnDelete.setBounds(px + pad + listW * 0.5f + 4f, py + 56f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnCopy.setBounds(px + pad, py + 18f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnPaste.setBounds(px + pad + listW * 0.5f + 4f, py + 18f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnNew.withFont(font); btnDelete.withFont(font); btnCopy.withFont(font); btnPaste.withFont(font);

            float formX = px + pad + listW + 22f;
            float formW = px + pw - pad - formX;
            tabEditBounds = new com.badlogic.gdx.math.Rectangle(formX, top - TAB_HEIGHT + 6f, 110f, TAB_HEIGHT);
            tabPreviewBounds = new com.badlogic.gdx.math.Rectangle(formX + 116f, top - TAB_HEIGHT + 6f, 110f, TAB_HEIGHT);
            float colW = (formW - 16f) * 0.5f;
            float fy = tabEditBounds.y - 30f;
            final float rowPitch = 55f;
            for (int i = 0; i < fields.length; i++) {
                float fx = (i % 2 == 0) ? formX : formX + colW + 16f;
                if (i % 2 == 0 && i > 0) fy -= rowPitch;
                fieldBounds[i].set(fx, fy - 16f, colW, FIELD_HEIGHT);
            }
            fy -= rowPitch;
            primaryPartBounds = new com.badlogic.gdx.math.Rectangle(formX, fy - 16f, colW, FIELD_HEIGHT);
            secondaryPartBounds = new com.badlogic.gdx.math.Rectangle(formX + colW + 16f, fy - 16f, colW, FIELD_HEIGHT);
            fy -= rowPitch;
            uniqueBounds = new com.badlogic.gdx.math.Rectangle(formX, fy, 20f, 20f);
            bowBounds = new com.badlogic.gdx.math.Rectangle(formX + 160f, fy, 20f, 20f);
            unlimitedBounds = new com.badlogic.gdx.math.Rectangle(formX + 300f, fy, 20f, 20f);
            float ddRows = 8.5f * ROW_HEIGHT;
            primaryPartDropdown.setBounds(primaryPartBounds.x, primaryPartBounds.y + primaryPartBounds.height - ddRows,
                    primaryPartBounds.width, ddRows);
            secondaryPartDropdown.setBounds(secondaryPartBounds.x, secondaryPartBounds.y + secondaryPartBounds.height - ddRows,
                    secondaryPartBounds.width, ddRows);
            previewAreaBounds = new com.badlogic.gdx.math.Rectangle(formX, py + 60f, formW,
                    tabEditBounds.y - (py + 60f) - 14f);
        }

        public void render(SpriteBatch batch, ShapeRenderer sr) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();
            layout(sw, sh);
            prepareUiProjection(sw, sh);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(sr, sw, sh);
            EditorPanelChrome.panel(sr, panelBounds, EditorTheme.ORANGE, HEADER_HEIGHT);
            drawItemTab(sr, tabEditBounds, !previewTab);
            drawItemTab(sr, tabPreviewBounds, previewTab);
            if (previewTab) {
                EditorPanelChrome.surface(sr, previewAreaBounds);
            } else {
                for (com.badlogic.gdx.math.Rectangle r : fieldBounds) EditorPanelChrome.textField(sr, r, false);
                if (activeField >= 0 && activeField < fieldBounds.length) EditorPanelChrome.textField(sr, fieldBounds[activeField], true);
                EditorPanelChrome.dropdownTrigger(sr, primaryPartBounds, primaryPartDropdown.isOpen());
                EditorPanelChrome.dropdownTrigger(sr, secondaryPartBounds, secondaryPartDropdown.isOpen());
                EditorPanelChrome.checkbox(sr, uniqueBounds, unique);
                EditorPanelChrome.checkbox(sr, bowBounds, bow);
                EditorPanelChrome.checkbox(sr, unlimitedBounds, unlimitedUse);
            }
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(sr, panelBounds);
            if (previewTab) {
                EditorPanelChrome.border(sr, previewAreaBounds);
            } else {
                for (com.badlogic.gdx.math.Rectangle r : fieldBounds) EditorPanelChrome.border(sr, r);
                EditorPanelChrome.border(sr, primaryPartBounds);
                EditorPanelChrome.border(sr, secondaryPartBounds);
            }
            sr.end();

            batch.begin();
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Item Editor", panelBounds.x + 18f, panelBounds.y + panelBounds.height - 18f);
            font.draw(batch, "Items " + defs.size(), listBounds.x, listBounds.y + listBounds.height + 16f);
            EditorPanelChrome.buttonText(batch, font, tabEditBounds, "Edit", !previewTab ? UI_TEXT_LIGHT : UI_TEXT);
            EditorPanelChrome.buttonText(batch, font, tabPreviewBounds, "Preview", previewTab ? UI_TEXT_LIGHT : UI_TEXT);
            if (previewTab) {
                drawItemPreview(batch);
            } else {
                for (int i = 0; i < fields.length; i++) drawItemField(batch, fieldLabels[i], fieldBounds[i], fields[i].toString());
                drawItemValue(batch, "Primary body part", primaryPartBounds, partLabel(primaryPartDropdown.selectedItem()));
                drawItemValue(batch, "Secondary body part", secondaryPartBounds, partLabel(secondaryPartDropdown.selectedItem()));
                font.setColor(UI_TEXT_LIGHT);
                font.draw(batch, "Unique", uniqueBounds.x + 28f, uniqueBounds.y + 15f);
                font.draw(batch, "Bow", bowBounds.x + 28f, bowBounds.y + 15f);
                font.draw(batch, "Unlimited", unlimitedBounds.x + 28f, unlimitedBounds.y + 15f);
                drawItemCheckMark(batch, uniqueBounds, unique);
                drawItemCheckMark(batch, bowBounds, bow);
                drawItemCheckMark(batch, unlimitedBounds, unlimitedUse);
            }
            batch.end();

            list.render(batch, sr, font);
            btnNew.render(batch, sr);
            btnDelete.render(batch, sr);
            btnCopy.render(batch, sr);
            btnPaste.render(batch, sr);
            if (!previewTab) {
                if (primaryPartDropdown.isOpen()) primaryPartDropdown.renderDropdown(batch, sr, font);
                if (secondaryPartDropdown.isOpen()) secondaryPartDropdown.renderDropdown(batch, sr, font);
            }
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        private void drawItemTab(ShapeRenderer sr, com.badlogic.gdx.math.Rectangle tab, boolean active) {
            sr.setColor(EditorTheme.ORANGE.r, EditorTheme.ORANGE.g, EditorTheme.ORANGE.b, active ? 0.85f : 0.4f);
            sr.rect(tab.x, tab.y, tab.width, tab.height);
        }

        private void drawItemField(SpriteBatch batch, String label, com.badlogic.gdx.math.Rectangle r, String value) {
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, label, r.x + 2f, r.y + r.height + 14f);
            font.setColor(UI_TEXT);
            font.draw(batch, EditorPanelChrome.fitText(font, glyph, value, r.width - 12f),
                    r.x + 6f, r.y + r.height * 0.5f + 6f);
        }

        private void drawItemValue(SpriteBatch batch, String label, com.badlogic.gdx.math.Rectangle r, String value) {
            drawItemField(batch, label, r, value);
        }

        private void drawItemCheckMark(SpriteBatch batch, com.badlogic.gdx.math.Rectangle r, boolean checked) {
            if (!checked) return;
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "x", r.x + 6f, r.y + 16f);
        }

        private String partLabel(BodyPart part) {
            return part == null ? "None" : part.name();
        }

        private void drawItemPreview(SpriteBatch batch) {
            ItemDefinition def = selectedIndex >= 0 && selectedIndex < defs.size() ? defs.get(selectedIndex) : null;
            if (def == null) {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "No item selected", previewAreaBounds.x + 20f, previewAreaBounds.y + previewAreaBounds.height - 20f);
                return;
            }
            float x = previewAreaBounds.x + 20f;
            float y = previewAreaBounds.y + previewAreaBounds.height - 24f;
            font.setColor(UI_TEXT);
            font.draw(batch, def.getKey() + " - " + def.getName(), x, y);
            drawSpritePreview(batch, "Inventory", def.getAppearanceInventory(), false, x, y - 130f);
            drawSpritePreview(batch, "Equipped", def.getAppearanceEquippedPrimary(), true, x + 170f, y - 130f);
            drawSpritePreview(batch, "Secondary", def.getAppearanceEquippedSecondary(), true, x + 340f, y - 130f);
        }

        private void drawSpritePreview(SpriteBatch batch, String label, String spriteName, boolean equippedBase, float x, float y) {
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, label, x, y + 104f);
            String resolvedSprite = resolveItemPreviewSprite(spriteName, equippedBase);
            if (resolvedSprite != null && !resolvedSprite.isBlank()) {
                try {
                    TextureRegion region = spriteLoader.getRegionFromSpriteName(resolvedSprite);
                    if (region != null) {
                        batch.draw(region, x, y, 96f, 96f);
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, "[no sprite]", x, y + 46f);
        }

        private String resolveItemPreviewSprite(String spriteName, boolean equippedBase) {
            if (spriteName == null || spriteName.isBlank()) return null;
            String trimmed = spriteName.trim();
            if (hasSpriteNamed(trimmed)) return trimmed;
            if (!equippedBase) return trimmed;
            String lower = trimmed.toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
                if (sprite == null || sprite.getName() == null) continue;
                String name = sprite.getName();
                String nameLower = name.toLowerCase(Locale.ROOT);
                if (nameLower.startsWith(lower)
                        && (nameLower.matches(java.util.regex.Pattern.quote(lower) + "\\d{3}-[a-z]")
                        || nameLower.matches(java.util.regex.Pattern.quote(lower) + "\\d{3}[a-z]"))) {
                    matches.add(name);
                }
            }
            matches.sort(String::compareToIgnoreCase);
            if (!matches.isEmpty()) return matches.get(0);
            String[] candidates = {
                    trimmed + "000-a", trimmed + "000a", trimmed + "225-a", trimmed + "225a",
                    trimmed + "000-A", trimmed + "000A"
            };
            for (String candidate : candidates) {
                if (hasSpriteNamed(candidate)) return candidate;
            }
            return trimmed;
        }

        private boolean hasSpriteNamed(String spriteName) {
            for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
                if (sprite != null && spriteName.equalsIgnoreCase(sprite.getName())) return true;
            }
            return false;
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT) return true;
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int y = Gdx.graphics.getHeight() - screenY;
            if (primaryPartDropdown.isOpen() && primaryPartDropdown.contains(screenX, y)) {
                primaryPartDropdown.handleClick(screenX, y, button);
                primaryPartDropdown.setOpen(false);
                return true;
            }
            if (secondaryPartDropdown.isOpen() && secondaryPartDropdown.contains(screenX, y)) {
                secondaryPartDropdown.handleClick(screenX, y, button);
                secondaryPartDropdown.setOpen(false);
                return true;
            }
            if (!panelBounds.contains(screenX, y)) return true;
            if (tabEditBounds.contains(screenX, y)) { previewTab = false; return true; }
            if (tabPreviewBounds.contains(screenX, y)) { previewTab = true; activeField = -1; return true; }
            if (list.handleClick(screenX, y, button)) {
                int idx = list.selectedIndex();
                if (idx >= 0) select(idx);
                return true;
            }
            if (btnNew.handleClick(screenX, y, button)) return true;
            if (btnDelete.handleClick(screenX, y, button)) return true;
            if (btnCopy.handleClick(screenX, y, button)) return true;
            if (btnPaste.handleClick(screenX, y, button)) return true;
            if (!previewTab) {
                for (int i = 0; i < fieldBounds.length; i++) {
                    if (fieldBounds[i].contains(screenX, y)) { activeField = i; closePartDropdowns(); return true; }
                }
                if (primaryPartBounds.contains(screenX, y)) { togglePartDropdown(primaryPartDropdown); return true; }
                if (secondaryPartBounds.contains(screenX, y)) { togglePartDropdown(secondaryPartDropdown); return true; }
                if (uniqueBounds.contains(screenX, y)) { unique = !unique; autoSave(); return true; }
                if (bowBounds.contains(screenX, y)) { bow = !bow; autoSave(); return true; }
                if (unlimitedBounds.contains(screenX, y)) { unlimitedUse = !unlimitedUse; autoSave(); return true; }
            }
            activeField = -1;
            return true;
        }

        private void togglePartDropdown(EditorDropdownList<BodyPart> dropdown) {
            boolean open = dropdown.isOpen();
            closePartDropdowns();
            dropdown.setOpen(!open);
            activeField = -1;
        }

        private void closePartDropdowns() {
            primaryPartDropdown.setOpen(false);
            secondaryPartDropdown.setOpen(false);
        }

        public boolean handleScroll(float amount) {
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int mx = Gdx.input.getX();
            int my = Gdx.graphics.getHeight() - Gdx.input.getY();
            int delta = amount > 0 ? 3 : -3;
            if (primaryPartDropdown.isOpen() && primaryPartDropdown.contains(mx, my)) {
                primaryPartDropdown.setScrollOffset(primaryPartDropdown.scrollOffset() + delta);
            } else if (secondaryPartDropdown.isOpen() && secondaryPartDropdown.contains(mx, my)) {
                secondaryPartDropdown.setScrollOffset(secondaryPartDropdown.scrollOffset() + delta);
            } else {
                list.scroll(delta);
            }
            return true;
        }

        public boolean handleKeyTyped(char character) {
            if (primaryPartDropdown.isOpen()) return primaryPartDropdown.handleKeyTyped(character);
            if (secondaryPartDropdown.isOpen()) return secondaryPartDropdown.handleKeyTyped(character);
            if (character < 32 || character == 127) return true;
            if (activeField >= 0 && activeField < fields.length) {
                fields[activeField].append(character);
                autoSave();
            }
            return true;
        }

        public boolean handleKeyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                if (primaryPartDropdown.isOpen() || secondaryPartDropdown.isOpen()) closePartDropdowns();
                else close();
                return true;
            }
            boolean ctrl = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
            if (ctrl && keycode == Input.Keys.C) { copySelected(); return true; }
            if (ctrl && keycode == Input.Keys.V) { pasteClipboard(); return true; }
            if (keycode == Input.Keys.BACKSPACE && activeField >= 0 && activeField < fields.length && fields[activeField].length() > 0) {
                fields[activeField].deleteCharAt(fields[activeField].length() - 1);
                autoSave();
                return true;
            }
            // SPACE: keyTyped n'est pas toujours émis pour l'espace, on l'ajoute ici
            if (keycode == Input.Keys.SPACE && !ctrl && activeField >= 0 && activeField < fields.length) {
                fields[activeField].append(' ');
                autoSave();
                return true;
            }
            return true;
        }

        private void select(int index) {
            if (index < 0 || index >= defs.size()) return;
            selectedIndex = index;
            list.select(index);
            ItemDefinition d = defs.get(index);
            set(fields[0], d.getKey());
            set(fields[1], d.getName());
            set(fields[2], d.getAppearanceEquippedPrimary());
            set(fields[3], d.getAppearanceEquippedSecondary());
            set(fields[4], d.getAppearanceInventory());
            set(fields[5], String.valueOf(d.getPrice()));
            set(fields[6], String.valueOf(d.getWeight()));
            set(fields[7], String.valueOf(d.getArmorClass()));
            set(fields[8], String.valueOf(d.getDodgeLost()));
            set(fields[9], String.valueOf(d.getMinEnd()));
            set(fields[10], String.valueOf(d.getReqAttack()));
            set(fields[11], String.valueOf(d.getReqStr()));
            set(fields[12], String.valueOf(d.getReqAgi()));
            set(fields[13], String.valueOf(d.getMinInt()));
            set(fields[14], String.valueOf(d.getMinWis()));
            set(fields[15], String.valueOf(d.getAttackSpeed()));
            suppressPartSelection = true;
            selectPart(primaryPartDropdown, d.getBodyPart());
            selectPart(secondaryPartDropdown, d.getSecondaryBodyPart());
            suppressPartSelection = false;
            unique = d.isUnique();
            bow = d.isBow();
            unlimitedUse = d.isUnlimitedUse();
        }

        private void selectPart(EditorDropdownList<BodyPart> dropdown, BodyPart part) {
            List<BodyPart> items = dropdown.items();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) == part) {
                    dropdown.select(i);
                    return;
                }
            }
            dropdown.clearSelection();
        }

        private void newDef() {
            ItemDefinition d = new ItemDefinition(uniqueName("NewItem"), "New Item", null, null, null,
                    null, null, 0L, 0L, 0d, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1d, false, false, false);
            defs.add(d);
            sortAndRefresh(d);
            saveToDisk();
        }

        private void deleteSelected() {
            if (selectedIndex < 0 || selectedIndex >= defs.size()) return;
            defs.remove(selectedIndex);
            list.setItems(defs);
            selectedIndex = -1;
            if (!defs.isEmpty()) select(Math.min(defs.size() - 1, 0));
            saveToDisk();
        }

        private void copySelected() {
            if (selectedIndex < 0 || selectedIndex >= defs.size()) return;
            clipboard = defs.get(selectedIndex);
            showEditorMessage("Copied " + clipboard.getKey());
        }

        private void pasteClipboard() {
            if (clipboard == null) {
                showEditorMessage("Nothing to paste");
                return;
            }
            ItemDefinition s = clipboard;
            ItemDefinition d = copyOf(s, uniqueName(s.getKey() + "Copy"), s.getName() + " (copy)");
            defs.add(d);
            sortAndRefresh(d);
            saveToDisk();
        }

        private ItemDefinition copyOf(ItemDefinition s, String key, String name) {
            return new ItemDefinition(key, name, s.getBodyPart(), s.getAppearanceEquippedPrimary(),
                    s.getSecondaryBodyPart(), s.getAppearanceEquippedSecondary(), s.getAppearanceInventory(),
                    s.getPrice(), s.getWeight(), s.getArmorClass(), s.getDodgeLost(), s.getMinEnd(),
                    s.getReqAttack(), s.getReqStr(), s.getReqAgi(), s.getMinInt(), s.getMinWis(),
                    s.getAttackSpeed(), s.isUnique(), s.isBow(), s.isUnlimitedUse());
        }

        private void autoSave() {
            String key = fields[0].toString().trim();
            if (key.isEmpty()) return;
            ItemDefinition d = fromForm();
            if (selectedIndex >= 0 && selectedIndex < defs.size()) defs.set(selectedIndex, d);
            else defs.add(d);
            sortAndRefresh(d);
            saveToDisk();
        }

        private ItemDefinition fromForm() {
            return new ItemDefinition(fields[0].toString().trim(),
                    fields[1].toString().trim(),
                    primaryPartDropdown.selectedItem(),
                    emptyToNull(fields[2].toString()),
                    secondaryPartDropdown.selectedItem(),
                    emptyToNull(fields[3].toString()),
                    emptyToNull(fields[4].toString()),
                    parseLong(fields[5].toString(), 0L),
                    parseLong(fields[6].toString(), 0L),
                    parseDouble(fields[7].toString(), 0d),
                    parseLong(fields[8].toString(), 0L),
                    parseLong(fields[9].toString(), 0L),
                    parseLong(fields[10].toString(), 0L),
                    parseLong(fields[11].toString(), 0L),
                    parseLong(fields[12].toString(), 0L),
                    parseLong(fields[13].toString(), 0L),
                    parseLong(fields[14].toString(), 0L),
                    parseDouble(fields[15].toString(), 1d),
                    unique, bow, unlimitedUse);
        }

        private void sortAndRefresh(ItemDefinition selected) {
            defs.sort(Comparator.comparing(ItemDefinition::getKey, String.CASE_INSENSITIVE_ORDER));
            list.setItems(defs);
            select(defs.indexOf(selected));
        }

        private String uniqueName(String base) {
            String candidate = base;
            int n = 2;
            while (nameExists(candidate)) candidate = base + n++;
            return candidate;
        }

        private boolean nameExists(String key) {
            for (ItemDefinition d : defs) {
                if (d.getKey() != null && d.getKey().equalsIgnoreCase(key)) return true;
            }
            return false;
        }

        private void saveToDisk() {
            try {
                ItemRegistry.save(new ArrayList<>(defs));
                showEditorMessage("Items saved");
            } catch (Exception e) {
                log.error("Failed to save item definitions", e);
                showEditorMessage("Error: failed to save items");
            }
        }

        public void close() {
            itemEditor = null;
        }
    }

    /**
     * CRUD editor for monster definitions stored in {@code assets/monsters/monsters.bin}.
     * Left: scrollable list of defs; right: a form of editable fields. Auto-saves on edit.
     */
    private class MonsterDefEditorUI extends EditorDialog {
        private static final float HEADER_HEIGHT = 56f;
        private static final float ROW_HEIGHT = 22f;
        private static final float FIELD_HEIGHT = 26f;
        private static final float TAB_HEIGHT = 26f;
        private static final float PREVIEW_FRAME_DURATION = 0.08f;
        private static final float PREVIEW_CELL_SIZE = 78f;
        private static final String[] MONSTER_PREVIEW_ANGLES = {"000", "045", "090", "135", "180", "225", "270", "315"};

        // Text fields (sounds are handled by dropdowns, not here).
        // 0 key,1 display,2 health,3 mana,4 xp/hit,5 xp/death,6 dmgMin,7 dmgMax,
        // 8 respawn,9 walk,10 attack,11 death,12 goldMin,13 goldMax,14 idlePause
        private final String[] fieldLabels = {
                "Key (name)", "Display name", "Health", "Mana", "XP / hit", "XP / death",
                "Damage min", "Damage max", "Respawn (ms)", "Walk pattern", "Attack pattern",
                "Death pattern", "Gold min", "Gold max", "Idle anim pause (s)"
        };
        private final StringBuilder[] fields = new StringBuilder[15];
        private final com.badlogic.gdx.math.Rectangle[] fieldBounds = new com.badlogic.gdx.math.Rectangle[15];

        private final List<MonsterDef> defs = new ArrayList<>();
        private final List<MonsterDef.LootDrop> loot = new ArrayList<>();
        private final List<String> soundOptions = new ArrayList<>();
        private final Map<String, Sound> previewSoundCache = new HashMap<>();
        private boolean aggressive = true;
        private boolean animateWhileStationary;

        private int selectedIndex = -1;
        private int activeField = -1;
        private boolean dirty;
        private boolean loadingSelection;
        private boolean lootTab = false; // false=Stats tab, true=Loot tab
        private boolean previewTab = false;
        private boolean suppressSoundSelection = false;
        private final Map<String, float[]> previewAnimState = new HashMap<>();

        private final EditorListBox<MonsterDef> list = new EditorListBox<MonsterDef>()
                .rowHeight(ROW_HEIGHT)
                .labelProvider(d -> d == null ? "" : d.getName())
                .colorProvider(i -> i == selectedIndex ? EditorTheme.BLUE : null);
        private final EditorButton btnNew = new EditorButton("New", this::newDef);
        private final EditorButton btnDelete = new EditorButton("Delete", this::deleteSelected);
        private final EditorButton btnCopy = new EditorButton("Copy", this::copySelected);
        private final EditorButton btnPaste = new EditorButton("Paste", this::pasteClipboard);

        // Sound dropdowns: 0=attack, 1=death, 2=hit. Selecting a value plays it.
        private final EditorDropdownList<String> soundAttackDropdown = newSoundDropdown(0);
        private final EditorDropdownList<String> soundDeathDropdown = newSoundDropdown(1);
        private final EditorDropdownList<String> soundHitDropdown = newSoundDropdown(2);
        private com.badlogic.gdx.math.Rectangle soundAttackBounds;
        private com.badlogic.gdx.math.Rectangle soundDeathBounds;
        private com.badlogic.gdx.math.Rectangle soundHitBounds;

        // Loot tab.
        private final EditorListBox<MonsterDef.LootDrop> lootList = new EditorListBox<MonsterDef.LootDrop>()
                .rowHeight(ROW_HEIGHT)
                .darkBackground()
                .labelProvider(d -> d == null ? "" : d.getItem() + "   —   chance " + d.getChance());
        private final StringBuilder lootItemField = new StringBuilder();
        private final StringBuilder lootChanceField = new StringBuilder();
        private final StringBuilder goldMinField = new StringBuilder();
        private final StringBuilder goldMaxField = new StringBuilder();
        private final EditorButton btnLootAdd = new EditorButton("Add", this::addLoot);
        private final EditorButton btnLootDel = new EditorButton("Remove", this::deleteLoot);
        private com.badlogic.gdx.math.Rectangle lootItemBounds;
        private com.badlogic.gdx.math.Rectangle lootChanceBounds;
        private com.badlogic.gdx.math.Rectangle lootListBounds;
        private com.badlogic.gdx.math.Rectangle goldMinBounds;
        private com.badlogic.gdx.math.Rectangle goldMaxBounds;
        private int lootActiveField = -1; // 0=item, 1=chance, 2=goldMin, 3=goldMax

        private com.badlogic.gdx.math.Rectangle panelBounds;
        private com.badlogic.gdx.math.Rectangle listBounds;
        private com.badlogic.gdx.math.Rectangle tabStatsBounds;
        private com.badlogic.gdx.math.Rectangle tabLootBounds;
        private com.badlogic.gdx.math.Rectangle tabPreviewBounds;
        private com.badlogic.gdx.math.Rectangle previewAreaBounds;
        private com.badlogic.gdx.math.Rectangle aggressiveBounds;
        private com.badlogic.gdx.math.Rectangle stationaryBounds;
        private final GlyphLayout glyph = new GlyphLayout();
        MonsterDefEditorUI() {
            super("Monster Editor");
            for (int i = 0; i < fields.length; i++) {
                fields[i] = new StringBuilder();
                fieldBounds[i] = new com.badlogic.gdx.math.Rectangle();
            }
            soundOptions.add(""); // "None"
            File soundsDir = new File(Paths.SOUNDS_DIR);
            File[] wavs = soundsDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".wav"));
            if (wavs != null) {
                List<String> names = new ArrayList<>();
                for (File f : wavs) names.add(f.getName());
                names.sort(String::compareToIgnoreCase);
                soundOptions.addAll(names);
            }
            soundAttackDropdown.setItems(soundOptions);
            soundDeathDropdown.setItems(soundOptions);
            soundHitDropdown.setItems(soundOptions);
            defs.addAll(MonsterRegistry.load());
            defs.sort(Comparator.comparing(MonsterDef::getName, String.CASE_INSENSITIVE_ORDER));
            list.setItems(defs);
            if (!defs.isEmpty()) {
                select(0);
            }
        }

        private EditorDropdownList<String> newSoundDropdown(int which) {
            return new EditorDropdownList<String>()
                    .visibleRows(8)
                    .labelProvider(v -> v == null || v.isEmpty() ? "None" : v)
                    .onSelection(v -> {
                        if (suppressSoundSelection) return;
                        playSelectedSound(v);
                        autoSave();
                    });
        }

        private void layout(int sw, int sh) {
            float pw = Math.min(980f, sw - 40f);
            float ph = Math.min(720f, sh - 40f);
            float px = (sw - pw) * 0.5f;
            float py = (sh - ph) * 0.5f;
            panelBounds = new com.badlogic.gdx.math.Rectangle(px, py, pw, ph);
            float pad = 18f;
            float listW = 230f;
            float top = py + ph - HEADER_HEIGHT - 10f;
            listBounds = new com.badlogic.gdx.math.Rectangle(px + pad, py + 92f, listW, top - (py + 92f));
            list.setBounds(listBounds.x, listBounds.y, listBounds.width, listBounds.height);
            btnNew.setBounds(px + pad, py + 94f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnDelete.setBounds(px + pad + listW * 0.5f + 4f, py + 94f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnCopy.setBounds(px + pad, py + 56f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnPaste.setBounds(px + pad + listW * 0.5f + 4f, py + 56f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnNew.withFont(font); btnDelete.withFont(font);
            btnCopy.withFont(font); btnPaste.withFont(font);
            float formX = px + pad + listW + 22f;
            float formW = px + pw - pad - formX;
            float tabBarY = top - TAB_HEIGHT + 6f;
            float tabW = formW / 3f;
            tabStatsBounds   = new com.badlogic.gdx.math.Rectangle(formX,             tabBarY, tabW, TAB_HEIGHT);
            tabLootBounds    = new com.badlogic.gdx.math.Rectangle(formX + tabW,       tabBarY, tabW, TAB_HEIGHT);
            tabPreviewBounds = new com.badlogic.gdx.math.Rectangle(formX + tabW * 2f, tabBarY, tabW, TAB_HEIGHT);
            float colW = (formW - 16f) * 0.5f;
            float fy = tabStatsBounds.y - 30f;
            final float rowPitch = 55f;
            // fields[0..11] = key, display, health, mana, xp/hit, xp/death, dmgMin, dmgMax, respawn, walk, attack, death
            // fields[14]    = idle anim pause  (gold uses goldMinField/goldMaxField, not fields[])
            for (int i = 0; i <= 11; i++) {
                float fx = (i % 2 == 0) ? formX : formX + colW + 16f;
                if (i % 2 == 0 && i > 0) fy -= rowPitch;
                fieldBounds[i].set(fx, fy - 16f, colW, FIELD_HEIGHT);
            }
            fy -= rowPitch;
            soundAttackBounds = new com.badlogic.gdx.math.Rectangle(formX,           fy - 16f, colW, FIELD_HEIGHT);
            soundDeathBounds  = new com.badlogic.gdx.math.Rectangle(formX + colW + 16f, fy - 16f, colW, FIELD_HEIGHT);
            fy -= rowPitch;
            soundHitBounds    = new com.badlogic.gdx.math.Rectangle(formX,           fy - 16f, colW, FIELD_HEIGHT);
            fieldBounds[14].set(formX + colW + 16f, fy - 16f, colW, FIELD_HEIGHT);
            fy -= rowPitch;
            aggressiveBounds  = new com.badlogic.gdx.math.Rectangle(formX,           fy, 20f, 20f);
            stationaryBounds  = new com.badlogic.gdx.math.Rectangle(formX + 160f,    fy, 20f, 20f);
            // Sound dropdown open bounds (expand upward from trigger)
            float ddRows = 8.5f * ROW_HEIGHT;
            soundAttackDropdown.setBounds(soundAttackBounds.x, soundAttackBounds.y + soundAttackBounds.height - ddRows, soundAttackBounds.width, ddRows);
            soundDeathDropdown .setBounds(soundDeathBounds.x,  soundDeathBounds.y  + soundDeathBounds.height  - ddRows, soundDeathBounds.width,  ddRows);
            soundHitDropdown   .setBounds(soundHitBounds.x,    soundHitBounds.y    + soundHitBounds.height    - ddRows, soundHitBounds.width,    ddRows);
            // Loot tab
            goldMinBounds = new com.badlogic.gdx.math.Rectangle(formX,           top - 30f - 16f,             colW, FIELD_HEIGHT);
            goldMaxBounds = new com.badlogic.gdx.math.Rectangle(formX + colW + 16f, top - 30f - 16f,          colW, FIELD_HEIGHT);
            float lootListY = py + 92f;
            float lootListH = goldMinBounds.y - 16f - (FIELD_HEIGHT + 6f) - lootListY;
            lootListBounds = new com.badlogic.gdx.math.Rectangle(formX, lootListY, formW, lootListH);
            lootList.setBounds(lootListBounds.x, lootListBounds.y, lootListBounds.width, lootListBounds.height);
            float addRowY = lootListBounds.y - FIELD_HEIGHT - 6f;
            lootItemBounds   = new com.badlogic.gdx.math.Rectangle(lootListBounds.x, addRowY, formW * 0.5f, FIELD_HEIGHT);
            lootChanceBounds = new com.badlogic.gdx.math.Rectangle(lootListBounds.x + formW * 0.5f + 6f, addRowY, formW * 0.18f, FIELD_HEIGHT);
            btnLootAdd.setBounds(lootListBounds.x + formW * 0.70f, addRowY, formW * 0.14f, FIELD_HEIGHT);
            btnLootDel.setBounds(lootListBounds.x + formW * 0.85f, addRowY, formW * 0.15f, FIELD_HEIGHT);
            btnLootAdd.withFont(font); btnLootDel.withFont(font);
            // Preview area
            previewAreaBounds = new com.badlogic.gdx.math.Rectangle(formX, py + 60f, formW, tabStatsBounds.y - (py + 60f) - 14f);
        }

        public void render(SpriteBatch batch, ShapeRenderer sr) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();
            layout(sw, sh);
            prepareUiProjection(sw, sh);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            sr.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(sr, sw, sh);
            EditorPanelChrome.panel(sr, panelBounds, EditorTheme.ORANGE, HEADER_HEIGHT);
            drawTab(sr, tabStatsBounds, !lootTab && !previewTab);
            drawTab(sr, tabLootBounds, lootTab);
            drawTab(sr, tabPreviewBounds, previewTab);
            if (previewTab) {
                EditorPanelChrome.surface(sr, previewAreaBounds);
            } else if (!lootTab) {
                for (int i = 0; i < 12; i++) {
                    EditorPanelChrome.textField(sr, fieldBounds[i], i == activeField);
                }
                EditorPanelChrome.textField(sr, fieldBounds[14], activeField == 14);
                EditorPanelChrome.dropdownTrigger(sr, soundAttackBounds, soundAttackDropdown.isOpen());
                EditorPanelChrome.dropdownTrigger(sr, soundDeathBounds, soundDeathDropdown.isOpen());
                EditorPanelChrome.dropdownTrigger(sr, soundHitBounds, soundHitDropdown.isOpen());
                EditorPanelChrome.checkbox(sr, aggressiveBounds, aggressive);
                EditorPanelChrome.checkbox(sr, stationaryBounds, animateWhileStationary);
            } else {
                EditorPanelChrome.textField(sr, goldMinBounds, lootActiveField == 2);
                EditorPanelChrome.textField(sr, goldMaxBounds, lootActiveField == 3);
                EditorPanelChrome.textField(sr, lootItemBounds, lootActiveField == 0);
                EditorPanelChrome.textField(sr, lootChanceBounds, lootActiveField == 1);
            }
            sr.end();

            sr.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(sr, panelBounds);
            if (previewTab) {
                EditorPanelChrome.border(sr, previewAreaBounds);
            } else if (!lootTab) {
                for (int i = 0; i < 12; i++) EditorPanelChrome.border(sr, fieldBounds[i]);
                EditorPanelChrome.border(sr, fieldBounds[14]);
                EditorPanelChrome.border(sr, soundAttackBounds);
                EditorPanelChrome.border(sr, soundDeathBounds);
                EditorPanelChrome.border(sr, soundHitBounds);
                EditorPanelChrome.border(sr, aggressiveBounds);
                EditorPanelChrome.border(sr, stationaryBounds);
            } else {
                EditorPanelChrome.border(sr, goldMinBounds);
                EditorPanelChrome.border(sr, goldMaxBounds);
                EditorPanelChrome.border(sr, lootItemBounds);
                EditorPanelChrome.border(sr, lootChanceBounds);
            }
            sr.end();

            batch.begin();
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "Monster Editor" + (dirty ? " *" : ""), panelBounds.x + 18f,
                    panelBounds.y + panelBounds.height - 18f);
            font.draw(batch, "Monsters " + defs.size(), listBounds.x, listBounds.y + listBounds.height + 16f);
            EditorPanelChrome.buttonText(batch, font, tabStatsBounds, "Stats", !lootTab && !previewTab ? UI_TEXT_LIGHT : UI_TEXT);
            EditorPanelChrome.buttonText(batch, font, tabLootBounds, "Loot", lootTab ? UI_TEXT_LIGHT : UI_TEXT);
            EditorPanelChrome.buttonText(batch, font, tabPreviewBounds, "Preview", previewTab ? UI_TEXT_LIGHT : UI_TEXT);
            if (previewTab) {
                drawMonsterPreview(batch);
            } else if (!lootTab) {
                for (int i = 0; i < 12; i++) drawLabeledField(batch, fieldLabels[i], fieldBounds[i], fields[i].toString());
                drawLabeledField(batch, fieldLabels[14], fieldBounds[14], fields[14].toString());
                drawLabeledValue(batch, "Sound attack", soundAttackBounds, soundLabel(soundAttackDropdown));
                drawLabeledValue(batch, "Sound death", soundDeathBounds, soundLabel(soundDeathDropdown));
                drawLabeledValue(batch, "Sound hit", soundHitBounds, soundLabel(soundHitDropdown));
                font.setColor(UI_TEXT_LIGHT);
                font.draw(batch, "Aggressive", aggressiveBounds.x + 28f, aggressiveBounds.y + 15f);
                font.draw(batch, "Animate while idle", stationaryBounds.x + 28f, stationaryBounds.y + 15f);
                drawCheckMark(batch, aggressiveBounds, aggressive);
                drawCheckMark(batch, stationaryBounds, animateWhileStationary);
            } else {
                drawLabeledField(batch, "Gold min", goldMinBounds, goldMinField.toString());
                drawLabeledField(batch, "Gold max", goldMaxBounds, goldMaxField.toString());
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "Loot drops (" + loot.size() + ")", lootListBounds.x + 2f,
                        lootListBounds.y + lootListBounds.height + 14f);
                font.setColor(lootActiveField == 0 ? UI_TEXT : UI_TEXT_FAINT);
                font.draw(batch, lootItemField.length() > 0 ? lootItemField.toString() : "item name",
                        lootItemBounds.x + 6f, lootItemBounds.y + lootItemBounds.height * 0.5f + 6f);
                font.setColor(lootActiveField == 1 ? UI_TEXT : UI_TEXT_FAINT);
                font.draw(batch, lootChanceField.length() > 0 ? lootChanceField.toString() : "0.0 - 1.0",
                        lootChanceBounds.x + 6f, lootChanceBounds.y + lootChanceBounds.height * 0.5f + 6f);
            }
            batch.end();

            list.render(batch, sr, font);
            btnNew.render(batch, sr);
            btnDelete.render(batch, sr);
            btnCopy.render(batch, sr);
            btnPaste.render(batch, sr);
            if (lootTab) {
                lootList.render(batch, sr, font);
                btnLootAdd.render(batch, sr);
                btnLootDel.render(batch, sr);
            } else if (!previewTab) {
                if (soundAttackDropdown.isOpen()) soundAttackDropdown.renderDropdown(batch, sr, font);
                if (soundDeathDropdown.isOpen()) soundDeathDropdown.renderDropdown(batch, sr, font);
                if (soundHitDropdown.isOpen()) soundHitDropdown.renderDropdown(batch, sr, font);
            }
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        private void drawTab(ShapeRenderer sr, com.badlogic.gdx.math.Rectangle tab, boolean active) {
            sr.setColor(EditorTheme.ORANGE.r, EditorTheme.ORANGE.g, EditorTheme.ORANGE.b, active ? 0.85f : 0.4f);
            sr.rect(tab.x, tab.y, tab.width, tab.height);
        }

        private void drawLabeledField(SpriteBatch batch, String label, com.badlogic.gdx.math.Rectangle r, String value) {
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, label, r.x + 2f, r.y + r.height + 14f);
            font.setColor(UI_TEXT);
            font.draw(batch, EditorPanelChrome.fitText(font, glyph, value, r.width - 12f),
                    r.x + 6f, r.y + r.height * 0.5f + 6f);
        }

        private void drawLabeledValue(SpriteBatch batch, String label, com.badlogic.gdx.math.Rectangle r, String value) {
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, label, r.x + 2f, r.y + r.height + 14f);
            font.setColor(UI_TEXT);
            font.draw(batch, EditorPanelChrome.fitText(font, glyph, value, r.width - 12f),
                    r.x + 6f, r.y + r.height * 0.5f + 6f);
        }

        /** Draw a contrasting check glyph so the checkbox state is unambiguous. */
        private void drawCheckMark(SpriteBatch batch, com.badlogic.gdx.math.Rectangle box, boolean checked) {
            font.setColor(checked ? UI_TEXT_LIGHT : UI_TEXT_FAINT);
            font.draw(batch, checked ? "X" : "", box.x + 6f, box.y + box.height - 4f);
        }

        private String soundLabel(EditorDropdownList<String> dd) {
            String v = dd.selectedItem();
            return v == null || v.isEmpty() ? "None" : v;
        }

        private void drawMonsterPreview(SpriteBatch batch) {
            if (previewAreaBounds == null) return;
            float delta = Gdx.graphics.getDeltaTime();
            float pad = 16f;
            float x = previewAreaBounds.x + pad;
            float yTop = previewAreaBounds.y + previewAreaBounds.height - pad;
            float contentW = previewAreaBounds.width - pad * 2f;
            float gap = 18f;
            float halfW = (contentW - gap) * 0.5f;
            float topBlockH = Math.min(220f, previewAreaBounds.height * 0.45f);

            String title = selectedIndex >= 0 && selectedIndex < defs.size()
                    ? defs.get(selectedIndex).getName()
                    : fields[0].toString().trim();
            font.setColor(UI_TEXT);
            font.draw(batch, title == null || title.isBlank() ? "No monster selected" : title, x, yTop);

            float blockTop = yTop - 28f;
            drawMonsterAnimationBlock(batch, "Walk", fields[9].toString(), true,
                    x, blockTop, halfW, topBlockH, delta);
            drawMonsterAnimationBlock(batch, "Attack", fields[10].toString(), true,
                    x + halfW + gap, blockTop, halfW, topBlockH, delta);

            float deathTop = blockTop - topBlockH - 24f;
            drawMonsterAnimationBlock(batch, "Death", fields[11].toString(), false,
                    x, deathTop, contentW, Math.max(120f, deathTop - previewAreaBounds.y - pad), delta);
        }

        private void drawMonsterAnimationBlock(SpriteBatch batch, String label, String pattern, boolean directional,
                                               float x, float yTop, float width, float height, float delta) {
            String cleanPattern = pattern == null ? "" : pattern.trim();
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, label + ": " + (cleanPattern.isEmpty()
                    ? "(empty)"
                    : EditorPanelChrome.fitText(font, glyph, cleanPattern, width - 8f)), x, yTop);

            MonsterPreviewPattern parsed = parseMonsterPreviewPattern(cleanPattern);
            if (parsed == null) {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "No frames: " + cleanPattern, x + 8f, yTop - 28f);
                return;
            }

            float contentTop = yTop - 22f;
            if (directional && !parsed.angleless) {
                drawDirectionalMonsterPreview(batch, label, parsed, x, contentTop, width, height - 24f, delta);
            } else {
                List<String> frames = monsterPreviewFrames(parsed, null);
                if (frames.isEmpty()) {
                    font.setColor(UI_TEXT_FAINT);
                    font.draw(batch, "No frames: " + cleanPattern, x + 8f, contentTop - 18f);
                    return;
                }
                float size = Math.min(130f, Math.min(width, height - 30f));
                drawMonsterAnimCell(batch, label + ":" + cleanPattern + ":single", frames,
                        x + (width - size) * 0.5f, contentTop, size, size, delta, null, false);
            }
        }

        private void drawDirectionalMonsterPreview(SpriteBatch batch, String label, MonsterPreviewPattern pattern,
                                                   float x, float yTop, float width, float height, float delta) {
            int cols = 4;
            int rows = 2;
            float cellSize = Math.min(PREVIEW_CELL_SIZE, Math.min(width / cols, height / rows));
            float gridW = cols * cellSize;
            float gridH = rows * cellSize;
            float startX = x + (width - gridW) * 0.5f;
            float startY = yTop - (Math.max(0f, height - gridH) * 0.5f);
            boolean anyFrames = false;

            for (int i = 0; i < MONSTER_PREVIEW_ANGLES.length; i++) {
                MonsterPreviewAngle angle = resolveMonsterPreviewAngle(MONSTER_PREVIEW_ANGLES[i]);
                List<String> frames = monsterPreviewFrames(pattern, angle.sourceAngle);
                if (!frames.isEmpty()) anyFrames = true;
                int col = i % cols;
                int row = i / cols;
                drawMonsterAnimCell(batch, label + ":" + pattern.raw + ":" + angle.displayAngle, frames,
                        startX + col * cellSize, startY - row * cellSize, cellSize, cellSize, delta,
                        angle.displayAngle, angle.flipX);
            }

            if (!anyFrames) {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "No frames: " + pattern.raw, x + 8f, yTop - 18f);
            }
        }

        private MonsterPreviewAngle resolveMonsterPreviewAngle(String displayAngle) {
            if ("225".equals(displayAngle)) {
                return new MonsterPreviewAngle(displayAngle, "135", true);
            }
            if ("270".equals(displayAngle)) {
                return new MonsterPreviewAngle(displayAngle, "090", true);
            }
            if ("315".equals(displayAngle)) {
                return new MonsterPreviewAngle(displayAngle, "045", true);
            }
            return new MonsterPreviewAngle(displayAngle, displayAngle, false);
        }

        private List<String> monsterPreviewFrames(MonsterPreviewPattern pattern, String angle) {
            List<String> frames = new ArrayList<>();
            if (pattern == null) return frames;
            String prefix = pattern.angleless || angle == null
                    ? pattern.baseName + "-"
                    : pattern.baseName + angle + "-";
            for (char c = 'a'; c <= pattern.stopLetter; c++) {
                String frameName = prefix + c;
                try {
                    if (spriteLoader.getRegionFromSpriteName(frameName) == null) {
                        break;
                    }
                    frames.add(frameName);
                } catch (Exception ignored) {
                    break;
                }
            }
            return frames;
        }

        private void drawMonsterAnimCell(SpriteBatch batch, String stateKey, List<String> frames,
                                         float x, float yTop, float cellW, float cellH, float delta,
                                         String angleLabel, boolean flipX) {
            if (angleLabel != null) {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, angleLabel, x + 4f, yTop - 4f);
            }
            if (frames == null || frames.isEmpty()) {
                font.setColor(UI_TEXT_FAINT);
                font.draw(batch, "-", x + cellW * 0.5f - 3f, yTop - cellH * 0.5f);
                return;
            }

            float[] state = previewAnimState.computeIfAbsent(stateKey, k -> new float[]{0f, 0f});
            state[0] += delta;
            if (state[0] >= PREVIEW_FRAME_DURATION) {
                state[0] -= PREVIEW_FRAME_DURATION;
                state[1] = (state[1] + 1) % frames.size();
            }

            String frameName = frames.get((int) state[1]);
            try {
                TextureRegion region = spriteLoader.getRegionFromSpriteName(frameName);
                if (region == null) return;
                float fw = region.getRegionWidth();
                float fh = region.getRegionHeight();
                float topReserve = angleLabel == null ? 0f : 14f;
                float drawH = Math.max(1f, cellH - topReserve);
                float scale = Math.min(cellW / Math.max(fw, 1f), drawH / Math.max(fh, 1f));
                float dw = fw * scale;
                float dh = fh * scale;
                float drawX = x + (cellW - dw) * 0.5f;
                float drawY = yTop - topReserve - drawH + (drawH - dh) * 0.5f;
                if (flipX) {
                    batch.draw(region, drawX + dw, drawY, -dw, dh);
                } else {
                    batch.draw(region, drawX, drawY, dw, dh);
                }
            } catch (Exception ignored) {
            }
        }

        private final class MonsterPreviewAngle {
            private final String displayAngle;
            private final String sourceAngle;
            private final boolean flipX;

            private MonsterPreviewAngle(String displayAngle, String sourceAngle, boolean flipX) {
                this.displayAngle = displayAngle;
                this.sourceAngle = sourceAngle;
                this.flipX = flipX;
            }
        }

        private MonsterPreviewPattern parseMonsterPreviewPattern(String pattern) {
            if (pattern == null || pattern.isBlank()) {
                return null;
            }
            int separator = pattern.indexOf('#');
            if (separator < 0) {
                return new MonsterPreviewPattern(pattern, pattern, 'a', true);
            }
            if (separator == 0 || separator + 1 >= pattern.length()) {
                return null;
            }
            String baseName = pattern.substring(0, separator);
            char stopLetter = Character.toLowerCase(pattern.charAt(separator + 1));
            if (stopLetter < 'a' || stopLetter > 'z') {
                return null;
            }
            return new MonsterPreviewPattern(pattern, baseName, stopLetter, baseName.endsWith("000"));
        }

        private final class MonsterPreviewPattern {
            private final String raw;
            private final String baseName;
            private final char stopLetter;
            private final boolean angleless;

            private MonsterPreviewPattern(String raw, String baseName, char stopLetter, boolean angleless) {
                this.raw = raw;
                this.baseName = baseName;
                this.stopLetter = stopLetter;
                this.angleless = angleless;
            }
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT) {
                return true;
            }
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int y = Gdx.graphics.getHeight() - screenY;
            // Open dropdowns get first chance.
            if (!lootTab && !previewTab) {
                if (handleSoundDropdownClick(soundAttackDropdown, screenX, y, button)) return true;
                if (handleSoundDropdownClick(soundDeathDropdown, screenX, y, button)) return true;
                if (handleSoundDropdownClick(soundHitDropdown, screenX, y, button)) return true;
            }
            if (!panelBounds.contains(screenX, y)) {
                return true;
            }
            if (tabStatsBounds.contains(screenX, y)) { lootTab = false; previewTab = false; closeSoundDropdowns(); return true; }
            if (tabLootBounds.contains(screenX, y)) { lootTab = true; previewTab = false; closeSoundDropdowns(); return true; }
            if (tabPreviewBounds.contains(screenX, y)) { lootTab = false; previewTab = true; activeField = -1; closeSoundDropdowns(); return true; }
            if (list.handleClick(screenX, y, button)) {
                int idx = list.selectedIndex();
                if (idx >= 0) select(idx);
                return true;
            }
            if (btnNew.handleClick(screenX, y, button)) return true;
            if (btnDelete.handleClick(screenX, y, button)) return true;
            if (btnCopy.handleClick(screenX, y, button)) return true;
            if (btnPaste.handleClick(screenX, y, button)) return true;

            if (previewTab) {
                activeField = -1;
                lootActiveField = -1;
            } else if (!lootTab) {
                for (int i = 0; i < 12; i++) {
                    if (fieldBounds[i].contains(screenX, y)) { activeField = i; closeSoundDropdowns(); return true; }
                }
                if (fieldBounds[14].contains(screenX, y)) { activeField = 14; closeSoundDropdowns(); return true; }
                if (soundAttackBounds.contains(screenX, y)) { toggleSoundDropdown(soundAttackDropdown); return true; }
                if (soundDeathBounds.contains(screenX, y)) { toggleSoundDropdown(soundDeathDropdown); return true; }
                if (soundHitBounds.contains(screenX, y)) { toggleSoundDropdown(soundHitDropdown); return true; }
                if (aggressiveBounds.contains(screenX, y)) { aggressive = !aggressive; autoSave(); return true; }
                if (stationaryBounds.contains(screenX, y)) { animateWhileStationary = !animateWhileStationary; autoSave(); return true; }
                activeField = -1;
            } else {
                if (goldMinBounds.contains(screenX, y)) { lootActiveField = 2; return true; }
                if (goldMaxBounds.contains(screenX, y)) { lootActiveField = 3; return true; }
                if (lootItemBounds.contains(screenX, y)) { lootActiveField = 0; return true; }
                if (lootChanceBounds.contains(screenX, y)) { lootActiveField = 1; return true; }
                if (lootList.handleClick(screenX, y, button)) return true;
                if (btnLootAdd.handleClick(screenX, y, button)) return true;
                if (btnLootDel.handleClick(screenX, y, button)) return true;
                lootActiveField = -1;
            }
            return true;
        }

        private boolean handleSoundDropdownClick(EditorDropdownList<String> dd, int x, int y, int button) {
            if (dd.isOpen() && dd.contains(x, y)) {
                dd.handleClick(x, y, button);
                dd.setOpen(false);
                return true;
            }
            return false;
        }

        private void toggleSoundDropdown(EditorDropdownList<String> dd) {
            boolean wasOpen = dd.isOpen();
            closeSoundDropdowns();
            dd.setOpen(!wasOpen);
            activeField = -1;
        }

        private void closeSoundDropdowns() {
            soundAttackDropdown.setOpen(false);
            soundDeathDropdown.setOpen(false);
            soundHitDropdown.setOpen(false);
        }

        public boolean handleScroll(float amount) {
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int mx = Gdx.input.getX();
            int my = Gdx.graphics.getHeight() - Gdx.input.getY();
            int delta = amount > 0 ? 3 : -3;
            if (soundAttackDropdown.isOpen() && soundAttackDropdown.contains(mx, my)) {
                soundAttackDropdown.setScrollOffset(soundAttackDropdown.scrollOffset() + delta);
            } else if (soundDeathDropdown.isOpen() && soundDeathDropdown.contains(mx, my)) {
                soundDeathDropdown.setScrollOffset(soundDeathDropdown.scrollOffset() + delta);
            } else if (soundHitDropdown.isOpen() && soundHitDropdown.contains(mx, my)) {
                soundHitDropdown.setScrollOffset(soundHitDropdown.scrollOffset() + delta);
            } else if (lootTab && lootListBounds != null && lootListBounds.contains(mx, my)) {
                lootList.scroll(delta);
            } else {
                list.scroll(delta);
            }
            return true;
        }

        public boolean handleKeyTyped(char character) {
            if (soundAttackDropdown.isOpen()) return soundAttackDropdown.handleKeyTyped(character);
            if (soundDeathDropdown.isOpen()) return soundDeathDropdown.handleKeyTyped(character);
            if (soundHitDropdown.isOpen()) return soundHitDropdown.handleKeyTyped(character);
            if (character < 32 || character == 127) {
                return true;
            }
            if (lootTab) {
                StringBuilder f = lootField();
                if (f != null) { f.append(character); if (lootActiveField >= 2) autoSave(); }
                return true;
            }
            if (activeField >= 0 && activeField < fields.length) {
                fields[activeField].append(character);
                autoSave();
            }
            return true;
        }

        private StringBuilder lootField() {
            switch (lootActiveField) {
                case 0: return lootItemField;
                case 1: return lootChanceField;
                case 2: return goldMinField;
                case 3: return goldMaxField;
                default: return null;
            }
        }

        public boolean handleKeyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                if (soundAttackDropdown.isOpen() || soundDeathDropdown.isOpen() || soundHitDropdown.isOpen()) {
                    closeSoundDropdowns();
                } else {
                    close();
                }
                return true;
            }
            boolean ctrl = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
            if (ctrl && keycode == Input.Keys.C) { copySelected(); return true; }
            if (ctrl && keycode == Input.Keys.V) { pasteClipboard(); return true; }
            if (keycode == Input.Keys.BACKSPACE) {
                if (lootTab) {
                    StringBuilder f = lootField();
                    if (f != null && f.length() > 0) { f.deleteCharAt(f.length() - 1); if (lootActiveField >= 2) autoSave(); }
                } else if (activeField >= 0 && activeField < fields.length && fields[activeField].length() > 0) {
                    fields[activeField].deleteCharAt(fields[activeField].length() - 1);
                    autoSave();
                }
                return true;
            }
            return true;
        }

        public void close() {
            disposePreviewSounds();
            monsterDefEditor = null;
        }

        private void playSelectedSound(String soundName) {
            if (soundName == null || soundName.isBlank()) {
                return;
            }
            try {
                Sound sound = previewSoundCache.computeIfAbsent(soundName, name ->
                        Gdx.audio.newSound(Gdx.files.internal(Paths.SOUNDS_DIR + "/" + name)));
                sound.play(1f);
            } catch (Exception e) {
                log.warn("Failed to preview sound {}", soundName, e);
            }
        }

        private void disposePreviewSounds() {
            for (Sound sound : previewSoundCache.values()) {
                if (sound != null) sound.dispose();
            }
            previewSoundCache.clear();
        }

        private void selectSoundInDropdown(EditorDropdownList<String> dd, String value) {
            String target = value == null ? "" : value;
            for (int i = 0; i < soundOptions.size(); i++) {
                if (soundOptions.get(i).equals(target)) {
                    // Set selection without firing the play/save side effects of select().
                    dd.clearSelection();
                    dd.setItems(soundOptions);
                    dd.select(i);
                    return;
                }
            }
        }

        private void select(int index) {
            if (index < 0 || index >= defs.size()) return;
            selectedIndex = index;
            list.select(index);
            MonsterDef d = defs.get(index);
            set(fields[0], d.getName());
            set(fields[1], d.getDisplayName());
            set(fields[2], String.valueOf(d.getHealth()));
            set(fields[3], String.valueOf(d.getMana()));
            set(fields[4], String.valueOf(d.getXpPerHit()));
            set(fields[5], String.valueOf(d.getXpOnDeath()));
            set(fields[6], String.valueOf(d.getHitDamageMin()));
            set(fields[7], String.valueOf(d.getHitDamageMax()));
            set(fields[8], String.valueOf(d.getRespawnTime()));
            set(fields[9], d.getWalkPattern());
            set(fields[10], d.getAttackPattern());
            set(fields[11], d.getDeathPattern());
            set(fields[14], String.valueOf(d.getStationaryAnimationPauseSeconds()));
            // Select sounds without triggering playback.
            suppressSoundSelection = true;
            selectSoundInDropdown(soundAttackDropdown, d.getSoundAttack());
            selectSoundInDropdown(soundDeathDropdown, d.getSoundDeath());
            selectSoundInDropdown(soundHitDropdown, d.getSoundHit());
            suppressSoundSelection = false;
            set(goldMinField, String.valueOf(d.getGoldMin()));
            set(goldMaxField, String.valueOf(d.getGoldMax()));
            aggressive = d.isDefaultAggressive();
            animateWhileStationary = d.isAnimateWhileStationary();
            loot.clear();
            if (d.getLoot() != null) loot.addAll(d.getLoot());
            lootList.setItems(loot);
        }

        private void newDef() {
            MonsterDef d = new MonsterDef("NewMonster", "New Monster", 100, 0, 0, 0, 1, 1, 5000L,
                    "", null, null, null, null, null, 0, 0, new ArrayList<>(), false, 0f,
                    0, 0, 0, 0, 0, 0, 0, new int[12],
                    1, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    50, 0, 0, true, new ArrayList<>());
            defs.add(d);
            defs.sort(Comparator.comparing(MonsterDef::getName, String.CASE_INSENSITIVE_ORDER));
            list.setItems(defs);
            select(defs.indexOf(d));
            saveToDisk();
        }

        private void deleteSelected() {
            if (selectedIndex < 0 || selectedIndex >= defs.size()) return;
            defs.remove(selectedIndex);
            list.setItems(defs);
            selectedIndex = -1;
            if (!defs.isEmpty()) select(0);
            saveToDisk();
        }

        private void copySelected() {
            if (selectedIndex < 0 || selectedIndex >= defs.size()) return;
            monsterClipboard = defs.get(selectedIndex);
            showEditorMessage("Copied " + monsterClipboard.getName());
        }

        private void pasteClipboard() {
            if (monsterClipboard == null) {
                showEditorMessage("Nothing to paste");
                return;
            }
            MonsterDef src = monsterClipboard;
            String newName = uniqueName(src.getName() + "Copy");
            MonsterDef d = new MonsterDef(newName, src.getDisplayName() + " (copy)",
                    src.getHealth(), src.getMana(), src.getXpPerHit(), src.getXpOnDeath(),
                    src.getHitDamageMin(), src.getHitDamageMax(), src.getRespawnTime(),
                    src.getWalkPattern(), src.getAttackPattern(), src.getDeathPattern(),
                    src.getSoundAttack(), src.getSoundDeath(), src.getSoundHit(),
                    src.getGoldMin(), src.getGoldMax(), new ArrayList<>(src.getLoot()),
                    src.isAnimateWhileStationary(), src.getStationaryAnimationPauseSeconds(),
                    src.getStr(), src.getEnd(), src.getAgi(), src.getIntel(),
                    src.getWill(), src.getWis(), src.getLuck(),
                    src.getResists() != null ? src.getResists().clone() : new int[12],
                    src.getLevel(), src.getDodge(), src.getAcMin(), src.getAcMax(), src.getAppearance(),
                    src.getItemBody(), src.getItemFeet(), src.getItemHands(), src.getItemHead(),
                    src.getItemLegs(), src.getItemWeapon(), src.getItemShield(), src.getItemBack(),
                    src.getAggro(), src.getClan(), src.getSpeed(), src.isCanAttack(),
                    new ArrayList<>(src.getAttacks()));
            defs.add(d);
            defs.sort(Comparator.comparing(MonsterDef::getName, String.CASE_INSENSITIVE_ORDER));
            list.setItems(defs);
            select(defs.indexOf(d));
            saveToDisk();
            showEditorMessage("Pasted as " + newName);
        }

        private String uniqueName(String base) {
            String candidate = base;
            int n = 2;
            while (nameExists(candidate)) {
                candidate = base + n++;
            }
            return candidate;
        }

        private boolean nameExists(String name) {
            for (MonsterDef d : defs) {
                if (d.getName().equalsIgnoreCase(name)) return true;
            }
            return false;
        }

        private void addLoot() {
            String item = lootItemField.toString().trim();
            if (item.isEmpty()) return;
            float chance = parseFloat(lootChanceField.toString(), 0f);
            loot.add(new MonsterDef.LootDrop(item, chance));
            lootList.setItems(loot);
            lootItemField.setLength(0);
            lootChanceField.setLength(0);
            autoSave();
        }

        private void deleteLoot() {
            int idx = lootList.selectedIndex();
            if (idx >= 0 && idx < loot.size()) {
                loot.remove(idx);
                lootList.setItems(loot);
                autoSave();
            }
        }

        private void autoSave() {
            if (suppressSoundSelection) return; // mid-select; avoid recursive saves
            String key = fields[0].toString().trim();
            if (key.isEmpty()) {
                dirty = true;
                return;
            }
            MonsterDef d = fromForm();
            if (selectedIndex >= 0 && selectedIndex < defs.size()) {
                defs.set(selectedIndex, d);
            } else {
                defs.add(d);
                selectedIndex = defs.indexOf(d);
            }
            list.setItems(defs);
            saveToDisk();
        }

        private MonsterDef fromForm() {
            // Preserve v2 fields from the currently selected def (not editable via UI)
            MonsterDef src = (selectedIndex >= 0 && selectedIndex < defs.size()) ? defs.get(selectedIndex) : null;
            int str = src != null ? src.getStr() : 0;
            int end = src != null ? src.getEnd() : 0;
            int agi = src != null ? src.getAgi() : 0;
            int intel = src != null ? src.getIntel() : 0;
            int will = src != null ? src.getWill() : 0;
            int wis = src != null ? src.getWis() : 0;
            int luck = src != null ? src.getLuck() : 0;
            int[] resists = src != null && src.getResists() != null ? src.getResists().clone() : new int[12];
            int level = src != null ? src.getLevel() : 1;
            int dodge = src != null ? src.getDodge() : 0;
            int acMin = src != null ? src.getAcMin() : 0;
            int acMax = src != null ? src.getAcMax() : 0;
            int appearance = src != null ? src.getAppearance() : 0;
            int itemBody = src != null ? src.getItemBody() : 0;
            int itemFeet = src != null ? src.getItemFeet() : 0;
            int itemHands = src != null ? src.getItemHands() : 0;
            int itemHead = src != null ? src.getItemHead() : 0;
            int itemLegs = src != null ? src.getItemLegs() : 0;
            int itemWeapon = src != null ? src.getItemWeapon() : 0;
            int itemShield = src != null ? src.getItemShield() : 0;
            int itemBack = src != null ? src.getItemBack() : 0;
            int aggro = src != null ? src.getAggro() : (aggressive ? 50 : 0);
            int clan = src != null ? src.getClan() : 0;
            int speed = src != null ? src.getSpeed() : 0;
            boolean canAttack = src == null || src.isCanAttack();
            java.util.List<MonsterDef.Attack> attacks = src != null ? new ArrayList<>(src.getAttacks()) : new ArrayList<>();
            return new MonsterDef(
                    fields[0].toString().trim(),
                    fields[1].toString().trim(),
                    parseInt(fields[2].toString(), 0),
                    parseInt(fields[3].toString(), 0),
                    parseInt(fields[4].toString(), 0),
                    parseInt(fields[5].toString(), 0),
                    parseInt(fields[6].toString(), 0),
                    parseInt(fields[7].toString(), 0),
                    parseLong(fields[8].toString(), 0L),
                    emptyToNull(fields[9].toString()),
                    emptyToNull(fields[10].toString()),
                    emptyToNull(fields[11].toString()),
                    emptyToNull(soundAttackDropdown.selectedItem()),
                    emptyToNull(soundDeathDropdown.selectedItem()),
                    emptyToNull(soundHitDropdown.selectedItem()),
                    parseInt(goldMinField.toString(), 0),
                    parseInt(goldMaxField.toString(), 0),
                    new ArrayList<>(loot),
                    animateWhileStationary,
                    parseFloat(fields[14].toString(), 0f),
                    str, end, agi, intel, will, wis, luck, resists,
                    level, dodge, acMin, acMax, appearance,
                    itemBody, itemFeet, itemHands, itemHead, itemLegs, itemWeapon, itemShield, itemBack,
                    aggro, clan, speed, canAttack, attacks);
        }

        private void saveToDisk() {
            try {
                List<MonsterDef> sorted = new ArrayList<>(defs);
                sorted.sort(Comparator.comparing(MonsterDef::getName, String.CASE_INSENSITIVE_ORDER));
                MonsterRegistry.save(sorted);
                monsterTypes.clear();
                selectedMonsterType = null;
                dirty = false;
            } catch (Exception e) {
                log.error("Failed to save monster definitions", e);
                showEditorMessage("Error: failed to save monsters");
            }
        }
    }

    /**
     * CRUD editor for NPC definitions stored in {@code assets/npcs/npcs.bin}.
     */
    private class NpcDefEditorUI extends EditorDialog {
        private static final float HEADER_HEIGHT = 56f;
        private static final float ROW_HEIGHT = 22f;
        private static final float FIELD_HEIGHT = 26f;

        private final List<NpcDef> defs = new ArrayList<>();
        private final List<NpcDef.Part> parts = new ArrayList<>();
        private final List<NpcDef.TaughtSpell> taughtSpells = new ArrayList<>();

        // 0=key,1=display,2=spriteBase,3=greeting,5=patrolTiles
        // Field 3 mirrors the greeting topic read-only; conversations are edited
        // in the Content Studio dialogue graph, not here.
        private final StringBuilder[] fields = new StringBuilder[6];
        private final String[] fieldLabels = {
                "Key (name)", "Display name", "Single sprite base", "Greeting (read-only)", "",
                "Patrol radius (tiles)"
        };
        private final com.badlogic.gdx.math.Rectangle[] fieldBounds = new com.badlogic.gdx.math.Rectangle[6];
        private boolean dialogTab = false;
        private com.badlogic.gdx.math.Rectangle tabAppearanceBounds;
        private com.badlogic.gdx.math.Rectangle tabDialogBounds;
        private com.badlogic.gdx.math.Rectangle dialogTextBounds;

        private int selectedIndex = -1;
        private int activeField = -1;
        private boolean dirty;
        private boolean loadingSelection;

        private final EditorListBox<NpcDef> list = new EditorListBox<NpcDef>()
                .rowHeight(ROW_HEIGHT)
                .labelProvider(d -> d == null ? "" : d.getName())
                .colorProvider(i -> i == selectedIndex ? EditorTheme.BLUE : null);
        private final EditorButton btnNew = new EditorButton("New", this::newDef);
        private final EditorButton btnDelete = new EditorButton("Delete", this::deleteSelected);

        private final EditorDropdownList<String> singleSpriteDropdown = new EditorDropdownList<String>()
                .visibleRows(10)
                .labelProvider(s -> s == null || s.isEmpty() ? "(assembled body parts)" : s);
        private com.badlogic.gdx.math.Rectangle singleSpriteBounds;

        // Parts sub-editor.
        private final EditorListBox<NpcDef.Part> partList = new EditorListBox<NpcDef.Part>()
                .rowHeight(ROW_HEIGHT)
                .darkBackground()
                .labelProvider(p -> p == null ? "" : p.getBodyPart().name() + "  " + p.getSpriteBase());
        private final EditorDropdownList<BodyPart> partBodyDropdown = new EditorDropdownList<BodyPart>()
                .visibleRows(8)
                .labelProvider(b -> b == null ? "" : b.name());
        private final StringBuilder partSpriteField = new StringBuilder();
        private final EditorButton btnPartAdd = new EditorButton("+", this::addPart);
        private final EditorButton btnPartDel = new EditorButton("-", this::deletePart);
        private com.badlogic.gdx.math.Rectangle partBodyBounds;
        private com.badlogic.gdx.math.Rectangle partSpriteBounds;
        private com.badlogic.gdx.math.Rectangle partListBounds;
        private boolean partSpriteActive;

        // Taught spells sub-editor (TEACH action).
        private final EditorListBox<NpcDef.TaughtSpell> spellList = new EditorListBox<NpcDef.TaughtSpell>()
                .rowHeight(ROW_HEIGHT)
                .darkBackground()
                .labelProvider(s -> s == null ? "" : s.getSpellName() + "  " + s.getPrice());
        private final EditorDropdownList<String> spellNameDropdown = new EditorDropdownList<String>()
                .visibleRows(8)
                .labelProvider(s -> s == null ? "" : s);
        private final StringBuilder spellPriceField = new StringBuilder();
        private final EditorButton btnSpellAdd = new EditorButton("+", this::addTaughtSpell);
        private final EditorButton btnSpellDel = new EditorButton("-", this::deleteTaughtSpell);
        private com.badlogic.gdx.math.Rectangle spellNameBounds;
        private com.badlogic.gdx.math.Rectangle spellPriceBounds;
        private com.badlogic.gdx.math.Rectangle spellListBounds;
        private boolean spellPriceActive;

        private com.badlogic.gdx.math.Rectangle panelBounds;
        private com.badlogic.gdx.math.Rectangle listBounds;
        private final GlyphLayout glyph = new GlyphLayout();

        NpcDefEditorUI() {
            super("NPC Editor");
            for (int i = 0; i < fields.length; i++) {
                fields[i] = new StringBuilder();
                fieldBounds[i] = new com.badlogic.gdx.math.Rectangle();
            }
            singleSpriteDropdown.setItems(loadAnimatedSpriteBases());
            singleSpriteDropdown.onSelection(sprite -> {
                set(fields[2], sprite);
                if (!loadingSelection) autoSave();
            });
            partBodyDropdown.setItems(java.util.Arrays.asList(BodyPart.values()));
            List<String> spellNames = new ArrayList<>();
            for (com.perso.T4C.spell.SpellData s : com.perso.T4C.spell.SpellRegistry.load()) {
                if (s != null && s.getName() != null && !s.getName().isEmpty()) {
                    spellNames.add(s.getName());
                }
            }
            spellNames.sort(String.CASE_INSENSITIVE_ORDER);
            spellNameDropdown.setItems(spellNames);
            defs.addAll(NpcRegistry.load());
            defs.sort(Comparator.comparing(NpcDef::getName, String.CASE_INSENSITIVE_ORDER));
            list.setItems(defs);
            if (!defs.isEmpty()) {
                select(0);
            }
        }

        private void layout(int sw, int sh) {
            float pw = Math.min(980f, sw - 40f);
            float ph = Math.min(720f, sh - 40f);
            float px = (sw - pw) * 0.5f;
            float py = (sh - ph) * 0.5f;
            panelBounds = new com.badlogic.gdx.math.Rectangle(px, py, pw, ph);

            float pad = 18f;
            float listW = 230f;
            float top = py + ph - HEADER_HEIGHT - 10f;
            listBounds = new com.badlogic.gdx.math.Rectangle(px + pad, py + 56f, listW, top - (py + 56f));
            list.setBounds(listBounds.x, listBounds.y, listBounds.width, listBounds.height);
            btnNew.setBounds(px + pad, py + 18f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnDelete.setBounds(px + pad + listW * 0.5f + 4f, py + 18f, listW * 0.5f - 4f, EditorPanelChrome.BUTTON_HEIGHT);
            btnNew.withFont(font);
            btnDelete.withFont(font);

            float formX = px + pad + listW + 22f;
            float formW = px + pw - pad - formX;
            tabAppearanceBounds = new com.badlogic.gdx.math.Rectangle(formX, py + ph - HEADER_HEIGHT + 14f, 118f, 28f);
            tabDialogBounds = new com.badlogic.gdx.math.Rectangle(formX + 126f, py + ph - HEADER_HEIGHT + 14f, 98f, 28f);
            float fy = top - 24f;
            final float ROW_PITCH = 55f;
            final float TIGHT_ROW_PITCH = 42f;
            // key, display
            fieldBounds[0].set(formX, fy - 16f, formW * 0.5f - 8f, FIELD_HEIGHT);
            fieldBounds[1].set(formX + formW * 0.5f + 8f, fy - 16f, formW * 0.5f - 8f, FIELD_HEIGHT);
            fy -= ROW_PITCH;
            // monobloc sprite base
            fieldBounds[2].set(formX, fy - 16f, formW, FIELD_HEIGHT);
            singleSpriteBounds = new com.badlogic.gdx.math.Rectangle(fieldBounds[2]);
            fy -= TIGHT_ROW_PITCH;
            fieldBounds[3].set(formX, fy - 16f, formW, FIELD_HEIGHT);
            fy -= ROW_PITCH;
            dialogTextBounds = new com.badlogic.gdx.math.Rectangle(formX, top - 246f, formW, 190f);
            fy -= ROW_PITCH;
            // patrol
            fieldBounds[5].set(formX, fy - 16f, formW / 3f - 8f, FIELD_HEIGHT);
            fy -= ROW_PITCH;
            // parts editor
            partBodyBounds = new com.badlogic.gdx.math.Rectangle(formX, fy - 16f, formW * 0.35f, FIELD_HEIGHT);
            partSpriteBounds = new com.badlogic.gdx.math.Rectangle(formX + formW * 0.36f, fy - 16f, formW * 0.4f, FIELD_HEIGHT);
            btnPartAdd.setBounds(formX + formW * 0.78f, fy - 16f, formW * 0.1f, FIELD_HEIGHT);
            btnPartDel.setBounds(formX + formW * 0.89f, fy - 16f, formW * 0.1f, FIELD_HEIGHT);
            btnPartAdd.withFont(font);
            btnPartDel.withFont(font);
            fy -= FIELD_HEIGHT + 10f;
            float remaining = Math.max(140f, fy - (py + 60f));
            float partListH = Math.max(60f, remaining * 0.5f - (FIELD_HEIGHT + 30f));
            partListBounds = new com.badlogic.gdx.math.Rectangle(formX, fy - partListH, formW, partListH);
            partList.setBounds(partListBounds.x, partListBounds.y, partListBounds.width, partListBounds.height);
            fy -= partListH + 30f;
            // taught spells row
            spellNameBounds = new com.badlogic.gdx.math.Rectangle(formX, fy - 16f, formW * 0.35f, FIELD_HEIGHT);
            spellPriceBounds = new com.badlogic.gdx.math.Rectangle(formX + formW * 0.36f, fy - 16f, formW * 0.4f, FIELD_HEIGHT);
            btnSpellAdd.setBounds(formX + formW * 0.78f, fy - 16f, formW * 0.1f, FIELD_HEIGHT);
            btnSpellDel.setBounds(formX + formW * 0.89f, fy - 16f, formW * 0.1f, FIELD_HEIGHT);
            btnSpellAdd.withFont(font);
            btnSpellDel.withFont(font);
            fy -= FIELD_HEIGHT + 10f;
            float spellListH = Math.max(50f, fy - (py + 60f));
            spellListBounds = new com.badlogic.gdx.math.Rectangle(formX, fy - spellListH, formW, spellListH);
            spellList.setBounds(spellListBounds.x, spellListBounds.y, spellListBounds.width, spellListBounds.height);
            float ddRows = 5.5f * ROW_HEIGHT;
            singleSpriteDropdown.setBounds(singleSpriteBounds.x, singleSpriteBounds.y + singleSpriteBounds.height - 10f * ROW_HEIGHT,
                    singleSpriteBounds.width, 10f * ROW_HEIGHT);
            partBodyDropdown.setBounds(partBodyBounds.x, partBodyBounds.y + partBodyBounds.height - ddRows,
                    partBodyBounds.width, ddRows);
            spellNameDropdown.setBounds(spellNameBounds.x, spellNameBounds.y + spellNameBounds.height - ddRows,
                    spellNameBounds.width, ddRows);
        }

        public void render(SpriteBatch batch, ShapeRenderer sr) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();
            layout(sw, sh);
            prepareUiProjection(sw, sh);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            sr.begin(ShapeRenderer.ShapeType.Filled);
            EditorPanelChrome.overlay(sr, sw, sh);
            EditorPanelChrome.panel(sr, panelBounds, EditorTheme.ORANGE, HEADER_HEIGHT);
            drawNpcTab(sr, tabAppearanceBounds, !dialogTab);
            drawNpcTab(sr, tabDialogBounds, dialogTab);
            if (dialogTab) {
                EditorPanelChrome.textField(sr, dialogTextBounds, activeField == 3);
            } else {
                EditorPanelChrome.textField(sr, fieldBounds[0], activeField == 0);
                EditorPanelChrome.textField(sr, fieldBounds[1], activeField == 1);
                EditorPanelChrome.dropdownTrigger(sr, singleSpriteBounds, singleSpriteDropdown.isOpen());
                EditorPanelChrome.textField(sr, fieldBounds[5], activeField == 5);
                EditorPanelChrome.dropdownTrigger(sr, partBodyBounds, partBodyDropdown.isOpen());
                EditorPanelChrome.darkSurface(sr, partSpriteBounds);
                EditorPanelChrome.dropdownTrigger(sr, spellNameBounds, spellNameDropdown.isOpen());
                EditorPanelChrome.darkSurface(sr, spellPriceBounds);
            }
            sr.end();

            sr.begin(ShapeRenderer.ShapeType.Line);
            EditorPanelChrome.border(sr, panelBounds);
            if (dialogTab) {
                EditorPanelChrome.border(sr, dialogTextBounds);
            } else {
                EditorPanelChrome.border(sr, fieldBounds[0]);
                EditorPanelChrome.border(sr, fieldBounds[1]);
                EditorPanelChrome.border(sr, singleSpriteBounds);
                EditorPanelChrome.border(sr, fieldBounds[5]);
                EditorPanelChrome.border(sr, partBodyBounds);
                EditorPanelChrome.border(sr, partSpriteBounds);
                EditorPanelChrome.border(sr, spellNameBounds);
                EditorPanelChrome.border(sr, spellPriceBounds);
            }
            sr.end();

            batch.begin();
            font.setColor(UI_TEXT_LIGHT);
            font.draw(batch, "NPC Editor" + (dirty ? " *" : ""), panelBounds.x + 18f,
                    panelBounds.y + panelBounds.height - 18f);
            font.draw(batch, "NPCs " + defs.size(), listBounds.x, listBounds.y + listBounds.height + 16f);
            EditorPanelChrome.buttonText(batch, font, tabAppearanceBounds, "Appearance", !dialogTab ? UI_TEXT_LIGHT : UI_TEXT);
            EditorPanelChrome.buttonText(batch, font, tabDialogBounds, "Dialog", dialogTab ? UI_TEXT_LIGHT : UI_TEXT);
            if (dialogTab) {
                drawNpcDialogTab(batch);
            } else {
                drawNpcAppearanceTab(batch);
            }
            batch.end();

            list.render(batch, sr, font);
            btnNew.render(batch, sr);
            btnDelete.render(batch, sr);
            if (!dialogTab) {
                partList.render(batch, sr, font);
                spellList.render(batch, sr, font);
                btnPartAdd.render(batch, sr);
                btnPartDel.render(batch, sr);
                btnSpellAdd.render(batch, sr);
                btnSpellDel.render(batch, sr);
            }
            if (!dialogTab && singleSpriteDropdown.isOpen()) singleSpriteDropdown.renderDropdown(batch, sr, font);
            if (!dialogTab && partBodyDropdown.isOpen()) partBodyDropdown.renderDropdown(batch, sr, font);
            if (!dialogTab && spellNameDropdown.isOpen()) spellNameDropdown.renderDropdown(batch, sr, font);
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        private void drawNpcTab(ShapeRenderer sr, com.badlogic.gdx.math.Rectangle tab, boolean active) {
            if (active) {
                EditorPanelChrome.button(sr, tab, true, false);
            } else {
                EditorPanelChrome.surface(sr, tab);
            }
            EditorPanelChrome.border(sr, tab);
        }

        private void drawNpcAppearanceTab(SpriteBatch batch) {
            drawNpcField(batch, 0);
            drawNpcField(batch, 1);
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, fieldLabels[2], singleSpriteBounds.x + 2f, singleSpriteBounds.y + singleSpriteBounds.height + 14f);
            font.setColor(UI_TEXT);
            String spriteLabel = fields[2].length() == 0 ? "(assembled body parts)" : fields[2].toString();
            font.draw(batch, EditorPanelChrome.fitText(font, glyph, spriteLabel, singleSpriteBounds.width - 28f),
                    singleSpriteBounds.x + 6f, singleSpriteBounds.y + singleSpriteBounds.height * 0.5f + 6f);
            drawNpcField(batch, 5);
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, "Body parts", partListBounds.x + 2f, partListBounds.y + partListBounds.height + 14f);
            font.setColor(UI_TEXT);
            font.draw(batch, partBodyDropdown.selectedItem() == null ? "BODY" : partBodyDropdown.selectedItem().name(),
                    partBodyBounds.x + 6f, partBodyBounds.y + partBodyBounds.height * 0.5f + 6f);
            font.setColor(partSpriteActive ? UI_TEXT : UI_TEXT_FAINT);
            font.draw(batch, partSpriteField.length() > 0 ? partSpriteField.toString() : "sprite base",
                    partSpriteBounds.x + 6f, partSpriteBounds.y + partSpriteBounds.height * 0.5f + 6f);
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, "Taught spells (TEACH)", spellListBounds.x + 2f,
                    spellListBounds.y + spellListBounds.height + 14f);
            font.setColor(UI_TEXT);
            font.draw(batch, spellNameDropdown.selectedItem() == null ? "SPELL" : spellNameDropdown.selectedItem(),
                    spellNameBounds.x + 6f, spellNameBounds.y + spellNameBounds.height * 0.5f + 6f);
            font.setColor(spellPriceActive ? UI_TEXT : UI_TEXT_FAINT);
            font.draw(batch, spellPriceField.length() > 0 ? spellPriceField.toString() : "price (gold)",
                    spellPriceBounds.x + 6f, spellPriceBounds.y + spellPriceBounds.height * 0.5f + 6f);
        }

        private void drawNpcDialogTab(SpriteBatch batch) {
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, fieldLabels[3], dialogTextBounds.x + 2f, dialogTextBounds.y + dialogTextBounds.height + 14f);
            font.setColor(UI_TEXT);
            drawMultilineText(batch, fields[3].toString(), dialogTextBounds);
        }

        private void drawNpcField(SpriteBatch batch, int index) {
            font.setColor(UI_TEXT_FAINT);
            font.draw(batch, fieldLabels[index], fieldBounds[index].x + 2f,
                    fieldBounds[index].y + fieldBounds[index].height + 14f);
            font.setColor(UI_TEXT);
            font.draw(batch, EditorPanelChrome.fitText(font, glyph, fields[index].toString(), fieldBounds[index].width - 12f),
                    fieldBounds[index].x + 6f, fieldBounds[index].y + fieldBounds[index].height * 0.5f + 6f);
        }

        private void drawMultilineText(SpriteBatch batch, String value, com.badlogic.gdx.math.Rectangle bounds) {
            float x = bounds.x + 8f;
            float y = bounds.y + bounds.height - 10f;
            float lineHeight = font.getLineHeight();
            int maxLines = Math.max(1, (int) ((bounds.height - 12f) / lineHeight));
            int drawn = 0;
            String text = value == null || value.isEmpty() ? "" : value;
            for (String paragraph : text.split("\\n", -1)) {
                if (paragraph.isEmpty()) {
                    if (++drawn >= maxLines) return;
                    y -= lineHeight;
                    continue;
                }
                String[] words = paragraph.split("\\s+");
                StringBuilder line = new StringBuilder();
                for (String word : words) {
                    String candidate = line.length() == 0 ? word : line + " " + word;
                    glyph.setText(font, candidate);
                    if (glyph.width > bounds.width - 16f && line.length() > 0) {
                        font.draw(batch, line.toString(), x, y);
                        if (++drawn >= maxLines) return;
                        y -= lineHeight;
                        line.setLength(0);
                        line.append(word);
                    } else {
                        line.setLength(0);
                        line.append(candidate);
                    }
                }
                if (line.length() > 0) {
                    font.draw(batch, line.toString(), x, y);
                    if (++drawn >= maxLines) return;
                    y -= lineHeight;
                }
            }
        }

        public boolean handleClick(int screenX, int screenY, int button) {
            if (button != Input.Buttons.LEFT) {
                return true;
            }
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int y = Gdx.graphics.getHeight() - screenY;
            if (singleSpriteDropdown.isOpen() && singleSpriteDropdown.contains(screenX, y)) {
                singleSpriteDropdown.handleClick(screenX, y, button);
                singleSpriteDropdown.setOpen(false);
                return true;
            }
            if (partBodyDropdown.isOpen() && partBodyDropdown.contains(screenX, y)) {
                partBodyDropdown.handleClick(screenX, y, button);
                partBodyDropdown.setOpen(false);
                return true;
            }
            if (spellNameDropdown.isOpen() && spellNameDropdown.contains(screenX, y)) {
                spellNameDropdown.handleClick(screenX, y, button);
                spellNameDropdown.setOpen(false);
                return true;
            }
            singleSpriteDropdown.setOpen(false);
            partBodyDropdown.setOpen(false);
            spellNameDropdown.setOpen(false);
            if (!panelBounds.contains(screenX, y)) {
                return true;
            }
            if (list.handleClick(screenX, y, button)) {
                int idx = list.selectedIndex();
                if (idx >= 0) select(idx);
                return true;
            }
            if (btnNew.handleClick(screenX, y, button)) return true;
            if (btnDelete.handleClick(screenX, y, button)) return true;
            if (tabAppearanceBounds.contains(screenX, y)) {
                dialogTab = false;
                activeField = -1;
                partSpriteActive = false;
                spellPriceActive = false;
                return true;
            }
            if (tabDialogBounds.contains(screenX, y)) {
                dialogTab = true;
                activeField = -1;
                partSpriteActive = false;
                spellPriceActive = false;
                singleSpriteDropdown.setOpen(false);
                partBodyDropdown.setOpen(false);
                spellNameDropdown.setOpen(false);
                return true;
            }
            if (dialogTab) {
                // Field 3 is a read-only mirror of the greeting topic, so it never takes focus.
            } else {
                int[] visibleFields = {0, 1, 2, 5};
                for (int i : visibleFields) {
                    if (i == 2) {
                        continue;
                    }
                    if (fieldBounds[i].contains(screenX, y)) {
                        activeField = i;
                        partSpriteActive = false;
                        spellPriceActive = false;
                        return true;
                    }
                }
                if (singleSpriteBounds.contains(screenX, y)) {
                    activeField = -1;
                    partSpriteActive = false;
                    spellPriceActive = false;
                    singleSpriteDropdown.setOpen(true);
                    return true;
                }
                if (partBodyBounds.contains(screenX, y)) {
                    activeField = -1;
                    partBodyDropdown.setOpen(true);
                    return true;
                }
                if (partSpriteBounds.contains(screenX, y)) { partSpriteActive = true; activeField = -1; spellPriceActive = false; return true; }
                if (spellNameBounds.contains(screenX, y)) {
                    activeField = -1;
                    spellNameDropdown.setOpen(true);
                    return true;
                }
                if (spellPriceBounds.contains(screenX, y)) { spellPriceActive = true; partSpriteActive = false; activeField = -1; return true; }
                if (partList.handleClick(screenX, y, button)) return true;
                if (spellList.handleClick(screenX, y, button)) return true;
                if (btnPartAdd.handleClick(screenX, y, button)) return true;
                if (btnPartDel.handleClick(screenX, y, button)) return true;
                if (btnSpellAdd.handleClick(screenX, y, button)) return true;
                if (btnSpellDel.handleClick(screenX, y, button)) return true;
            }
            activeField = -1;
            partSpriteActive = false;
            spellPriceActive = false;
            return true;
        }

        public boolean handleScroll(float amount) {
            layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            int mx = Gdx.input.getX();
            int my = Gdx.graphics.getHeight() - Gdx.input.getY();
            int delta = amount > 0 ? 3 : -3;
            if (!dialogTab && singleSpriteDropdown.isOpen() && singleSpriteDropdown.contains(mx, my)) {
                singleSpriteDropdown.setScrollOffset(singleSpriteDropdown.scrollOffset() + delta);
            } else if (!dialogTab && partBodyDropdown.isOpen() && partBodyDropdown.contains(mx, my)) {
                partBodyDropdown.setScrollOffset(partBodyDropdown.scrollOffset() + delta);
            } else if (!dialogTab && spellNameDropdown.isOpen() && spellNameDropdown.contains(mx, my)) {
                spellNameDropdown.setScrollOffset(spellNameDropdown.scrollOffset() + delta);
            } else if (!dialogTab && partListBounds != null && partListBounds.contains(mx, my)) {
                partList.scroll(delta);
            } else if (!dialogTab && spellListBounds != null && spellListBounds.contains(mx, my)) {
                spellList.scroll(delta);
            } else {
                list.scroll(delta);
            }
            return true;
        }

        public boolean handleKeyTyped(char character) {
            if (singleSpriteDropdown.isOpen()) return singleSpriteDropdown.handleKeyTyped(character);
            if (partBodyDropdown.isOpen()) return partBodyDropdown.handleKeyTyped(character);
            if (spellNameDropdown.isOpen()) return spellNameDropdown.handleKeyTyped(character);
            if (character < 32 || character == 127) {
                return true;
            }
            if (partSpriteActive) { partSpriteField.append(character); return true; }
            if (spellPriceActive) {
                if (character >= '0' && character <= '9') spellPriceField.append(character);
                return true;
            }
            if (activeField >= 0 && activeField < fields.length) {
                fields[activeField].append(character);
                autoSave();
            }
            return true;
        }

        public boolean handleKeyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                if (singleSpriteDropdown.isOpen()) { singleSpriteDropdown.setOpen(false); return true; }
                if (partBodyDropdown.isOpen()) { partBodyDropdown.setOpen(false); return true; }
                if (spellNameDropdown.isOpen()) { spellNameDropdown.setOpen(false); return true; }
                close();
                return true;
            }
            if (keycode == Input.Keys.BACKSPACE) {
                if (partSpriteActive && partSpriteField.length() > 0) {
                    partSpriteField.deleteCharAt(partSpriteField.length() - 1);
                } else if (spellPriceActive && spellPriceField.length() > 0) {
                    spellPriceField.deleteCharAt(spellPriceField.length() - 1);
                } else if (activeField >= 0 && activeField < fields.length && fields[activeField].length() > 0) {
                    fields[activeField].deleteCharAt(fields[activeField].length() - 1);
                    autoSave();
                }
                return true;
            }
            if (keycode == Input.Keys.ENTER && activeField == 3) {
                fields[3].append('\n');
                autoSave();
                return true;
            }
            if (keycode == Input.Keys.TAB) {
                if (dialogTab) {
                    activeField = 3;
                } else {
                    int[] visibleFields = {0, 1, 2, 5};
                    int current = 0;
                    for (int i = 0; i < visibleFields.length; i++) {
                        if (visibleFields[i] == activeField) {
                            current = i + 1;
                            break;
                        }
                    }
                    activeField = visibleFields[current % visibleFields.length];
                }
                partSpriteActive = false;
                spellPriceActive = false;
                return true;
            }
            return true;
        }

        public void close() {
            npcDefEditor = null;
        }

        private void select(int index) {
            if (index < 0 || index >= defs.size()) return;
            loadingSelection = true;
            selectedIndex = index;
            list.select(index);
            NpcDef d = defs.get(index);
            set(fields[0], d.getName());
            set(fields[1], d.getDisplayName());
            set(fields[2], d.getSpriteBase());
            set(fields[3], d.getDialogNodes().stream()
                    .filter(NpcDef.DialogNode::isGreeting)
                    .map(NpcDef.DialogNode::getResponse)
                    .findFirst().orElse(""));
            set(fields[5], String.valueOf(d.getPatrolRadiusTiles()));
            selectSingleSpriteInDropdown(d.getSpriteBase());
            parts.clear();
            if (d.getParts() != null) parts.addAll(d.getParts());
            partList.setItems(parts);
            taughtSpells.clear();
            if (d.getTaughtSpells() != null) taughtSpells.addAll(d.getTaughtSpells());
            spellList.setItems(taughtSpells);
            loadingSelection = false;
        }

        private void selectSingleSpriteInDropdown(String spriteBase) {
            String wanted = spriteBase == null ? "" : spriteBase;
            for (int i = 0; i < singleSpriteDropdown.items().size(); i++) {
                if (wanted.equals(singleSpriteDropdown.items().get(i))) {
                    singleSpriteDropdown.select(i);
                    return;
                }
            }
            singleSpriteDropdown.clearSelection();
        }

        private List<String> loadAnimatedSpriteBases() {
            java.util.Set<String> bases = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            bases.add("");
            String[] angleSuffixes = {"000-", "045-", "090-", "135-", "180-", "225-", "270-", "315-"};
            for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
                if (sprite == null || sprite.getName() == null) continue;
                String name = sprite.getName();
                if (name.length() < 5) continue;
                char frame = name.charAt(name.length() - 1);
                if (frame < 'a' || frame > 'z') continue;
                for (String suffix : angleSuffixes) {
                    int idx = name.lastIndexOf(suffix);
                    if (idx > 0 && idx == name.length() - suffix.length() - 1) {
                        bases.add(name.substring(0, idx));
                        break;
                    }
                }
            }
            return new ArrayList<>(bases);
        }

        private void newDef() {
            NpcDef d = new NpcDef("NewNpc", "New NPC",
                    new ArrayList<>(List.of(new NpcDef.Part(BodyPart.BODY, ""))),
                    null, 0, List.of(), "", List.of());
            defs.add(d);
            defs.sort(Comparator.comparing(NpcDef::getName, String.CASE_INSENSITIVE_ORDER));
            list.setItems(defs);
            select(defs.indexOf(d));
            saveToDisk();
        }

        private void deleteSelected() {
            if (selectedIndex < 0 || selectedIndex >= defs.size()) return;
            defs.remove(selectedIndex);
            list.setItems(defs);
            selectedIndex = -1;
            if (!defs.isEmpty()) select(0);
            saveToDisk();
        }

        private void addPart() {
            BodyPart bp = partBodyDropdown.selectedItem();
            String sprite = partSpriteField.toString().trim();
            if (bp == null || sprite.isEmpty()) return;
            parts.add(new NpcDef.Part(bp, sprite));
            partList.setItems(parts);
            partSpriteField.setLength(0);
            autoSave();
        }

        private void deletePart() {
            int idx = partList.selectedIndex();
            if (idx >= 0 && idx < parts.size()) {
                parts.remove(idx);
                partList.setItems(parts);
                autoSave();
            }
        }

        private void addTaughtSpell() {
            String spellName = spellNameDropdown.selectedItem();
            if (spellName == null || spellName.isEmpty()) return;
            int price = parseInt(spellPriceField.toString(), 0);
            taughtSpells.removeIf(s -> s != null && spellName.equals(s.getSpellName()));
            taughtSpells.add(new NpcDef.TaughtSpell(spellName, price));
            spellList.setItems(taughtSpells);
            spellPriceField.setLength(0);
            autoSave();
        }

        private void deleteTaughtSpell() {
            int idx = spellList.selectedIndex();
            if (idx >= 0 && idx < taughtSpells.size()) {
                taughtSpells.remove(idx);
                spellList.setItems(taughtSpells);
                autoSave();
            }
        }

        private void autoSave() {
            String key = fields[0].toString().trim();
            if (key.isEmpty()) {
                dirty = true;
                return;
            }
            NpcDef d = fromForm();
            if (selectedIndex >= 0 && selectedIndex < defs.size()) {
                defs.set(selectedIndex, d);
            } else {
                defs.add(d);
                selectedIndex = defs.indexOf(d);
            }
            list.setItems(defs);
            saveToDisk();
        }

        private NpcDef fromForm() {
            // This form edits appearance only; shop, train, flee-shout and
            // conversation data live outside it and must be carried over, not
            // dropped. Field 3 is a read-only view of the greeting topic.
            String name = fields[0].toString().trim();
            NpcDef existing = NpcRegistry.findByName(name);
            return new NpcDef(
                    name,
                    fields[1].toString().trim(),
                    new ArrayList<>(parts),
                    emptyToNull(fields[2].toString()),
                    parseInt(fields[5].toString(), 0),
                    new ArrayList<>(taughtSpells),
                    existing == null ? List.of() : existing.getShopItems(),
                    existing == null ? List.of() : existing.getTrainableStats(),
                    existing == null ? List.of() : existing.getFleeShouts(),
                    existing == null ? List.of() : existing.getDialogNodes());
        }

        private void saveToDisk() {
            try {
                List<NpcDef> sorted = new ArrayList<>(defs);
                sorted.sort(Comparator.comparing(NpcDef::getName, String.CASE_INSENSITIVE_ORDER));
                NpcRegistry.save(sorted);
                npcTypes.clear();
                selectedNpcType = null;
                dirty = false;
            } catch (Exception e) {
                log.error("Failed to save NPC definitions", e);
                showEditorMessage("Error: failed to save NPCs");
            }
        }
    }

    // ---- Shared small helpers for the def editors ----
    private static void set(StringBuilder sb, String value) {
        sb.setLength(0);
        if (value != null) sb.append(value);
    }

    private static String emptyToNull(String value) {
        String t = value == null ? null : value.trim();
        return t == null || t.isEmpty() ? null : t;
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static long parseLong(String s, long fallback) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static float parseFloat(String s, float fallback) {
        try {
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static double parseDouble(String s, double fallback) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private void pushSpriteHistory(String spriteName) {
        if (spriteName == null || spriteName.isBlank()) {
            return;
        }
        String trimmed = spriteName.trim();
        recentSpriteHistory.removeIf(name -> name != null && name.equalsIgnoreCase(trimmed));
        recentSpriteHistory.addFirst(trimmed);
        while (recentSpriteHistory.size() > SPRITE_HISTORY_SIZE) {
            recentSpriteHistory.removeLast();
        }
    }

    /**
     * Rendre la toolbar
     */
    private void renderToolbar() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Configurer la projection matrix pour l'UI
        com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
        uiMatrix.setToOrtho2D(0, 0, screenWidth, screenHeight);
        shapeRenderer.setProjectionMatrix(uiMatrix);
        uiBatch.setProjectionMatrix(uiMatrix);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UI_APP_BG);
        shapeRenderer.rect(0, screenHeight - MENU_BAR_HEIGHT, screenWidth, MENU_BAR_HEIGHT);
        shapeRenderer.setColor(UI_BORDER);
        shapeRenderer.rect(0, screenHeight - MENU_BAR_HEIGHT, screenWidth, 1);
        shapeRenderer.end();

        float x = 10f;
        GlyphLayout menuMetrics = new GlyphLayout(font, "File");
        float menuTextY = screenHeight - MENU_BAR_HEIGHT + (MENU_BAR_HEIGHT + menuMetrics.height) / 2f - 1f;
        uiBatch.begin();
        font.setColor(UI_TEXT_LIGHT);
        for (MenuTitle menu : menuTitles) {
            GlyphLayout layout = new GlyphLayout(font, menu.name);
            float width = layout.width + 24f;
            menu.bounds.set(x, screenHeight - MENU_BAR_HEIGHT, width, MENU_BAR_HEIGHT);
            boolean active = menu.name.equals(openMenu);
            uiBatch.end();
            if (active) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(UI_PANEL_DARK_2);
                shapeRenderer.rect(menu.bounds.x, menu.bounds.y, menu.bounds.width, menu.bounds.height);
                shapeRenderer.end();
            }
            uiBatch.begin();
            font.setColor(active ? Color.WHITE : UI_TEXT_LIGHT);
            font.draw(uiBatch, menu.name, x + 12f, menuTextY);
            x += width + 2f;
        }
        font.setColor(UI_TEXT_FAINT);
        String mapName = mapReader != null ? "  " + getMapDisplayName(currentMapPath) : "";
        GlyphLayout mapLayout = new GlyphLayout(font, mapName);
        font.draw(uiBatch, mapName, screenWidth - mapLayout.width - 16f, menuTextY);
        uiBatch.end();

        renderOpenMenu(screenHeight);
        if (groundSelectBounds == null) {
            groundSelectBounds = new com.badlogic.gdx.math.Rectangle(10f, screenHeight - MENU_BAR_HEIGHT - BUTTON_SIZE - 8f,
                    GROUND_SELECT_WIDTH, BUTTON_SIZE);
        } else {
            groundSelectBounds.set(10f, screenHeight - MENU_BAR_HEIGHT - BUTTON_SIZE - 8f, GROUND_SELECT_WIDTH,
                    BUTTON_SIZE);
        }
        if (teleportGotoDialog != null) {
            teleportGotoDialog.render(uiBatch, shapeRenderer, font);
        }
    }

    private void applyTeleportGotoDialog(int tileX, int tileY, int z) {
        if (tileX == Integer.MIN_VALUE || tileY == Integer.MIN_VALUE || z == Integer.MIN_VALUE) {
            showEditorMessage("Invalid coordinates (use integers)");
            return;
        }
        try {
            int currentZ = getCurrentMapZ();
            if (z != currentZ) {
                MapDefinition target = MapDefinition.fromZ(z);
                if (target == null) {
                    showEditorMessage("Invalid Z: " + z);
                    return;
                }
                pendingTeleportAfterMapSwitch = true;
                pendingTeleportTileX = tileX;
                pendingTeleportTileY = tileY;
                teleportGotoDialog = null;
                startMapSwitchLoading(target.getMapPath(), availableMaps.indexOf(normalizeMapPath(target.getMapPath())));
                return;
            } else {
                if (!isTileInMap(tileX, tileY)) {
                    showEditorMessage("Coordinates out of map bounds");
                    return;
                }
            }
            cameraPosition.set(tileX * GameConstants.GRID_W, tileY * GameConstants.GRID_H);
            camera.position.set(cameraPosition.x, cameraPosition.y, 0f);
            camera.zoom = zoom;
            camera.update();
            teleportGotoDialog = null;
            showEditorMessage("Teleported to (" + tileX + ", " + tileY + ", z" + z + ")");
        } catch (NumberFormatException e) {
            showEditorMessage("Invalid coordinates (use integers)");
        }
    }

    private boolean handleGroundSelectClick(int screenX, int screenY) {
        if (!autofillEnabled) {
            return false;
        }
        int y = Gdx.graphics.getHeight() - screenY;

        if (groundSelectBounds != null && groundSelectBounds.contains(screenX, y)) {
            groundSelectOpen = !groundSelectOpen;
            if (groundSelectOpen) {
                int selectedIndex = Math.max(0, groundFillSprites.indexOf(fillToolSprite));
                int maxOffset = Math.max(0, groundFillSprites.size() - groundSelectVisibleRows);
                groundSelectScrollOffset = Math.max(0, Math.min(maxOffset,
                        selectedIndex - groundSelectVisibleRows / 2));
            }
            return true;
        }

        if (groundSelectOpen && groundSelectDropdownBounds != null && groundSelectDropdownBounds.contains(screenX, y)) {
            int visibleIndex = (int) ((groundSelectListTop - y) / MAP_SELECT_ITEM_HEIGHT);
            int index = groundSelectScrollOffset + visibleIndex;
            if (visibleIndex >= 0 && visibleIndex < groundSelectVisibleRows
                    && index >= 0 && index < groundFillSprites.size()) {
                setFillToolSprite(groundFillSprites.get(index));
                groundSelectOpen = false;
                return true;
            }
        }

        if (groundSelectOpen) {
            groundSelectOpen = false;
        }
        return false;
    }

    private void updateGroundSelectHover(int screenX, int y) {
        groundSelectHoverIndex = -1;
        if (!autofillEnabled) {
            return;
        }
        if (!groundSelectOpen || groundSelectDropdownBounds == null) {
            return;
        }

        if (groundSelectDropdownBounds.contains(screenX, y)) {
            int visibleIndex = (int) ((groundSelectListTop - y) / MAP_SELECT_ITEM_HEIGHT);
            int index = groundSelectScrollOffset + visibleIndex;
            if (visibleIndex >= 0 && visibleIndex < groundSelectVisibleRows
                    && index >= 0 && index < groundFillSprites.size()) {
                groundSelectHoverIndex = index;
            }
        }
    }

    private void renderGroundSelect() {
        if (groundSelectBounds == null) {
            return;
        }
        if (!autofillEnabled) {
            return;
        }
        if (groundFillSprites.isEmpty() && mapReader != null) {
            buildGroundFillSprites();
        }
        if (groundFillSprites.isEmpty()) {
            return;
        }

        String currentName = fillToolSprite != null ? fillToolSprite : groundFillSprites.get(0);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UI_SURFACE);
        shapeRenderer.rect(groundSelectBounds.x, groundSelectBounds.y, groundSelectBounds.width,
                groundSelectBounds.height);
        shapeRenderer.setColor(0.95f, 0.58f, 0.22f, 0.85f);
        shapeRenderer.rect(groundSelectBounds.x, groundSelectBounds.y, 3f, groundSelectBounds.height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UI_BORDER);
        shapeRenderer.rect(groundSelectBounds.x, groundSelectBounds.y, groundSelectBounds.width,
                groundSelectBounds.height);
        shapeRenderer.end();

        uiBatch.begin();
        font.setColor(UI_TEXT_MUTED);
        font.draw(uiBatch, "FILL", groundSelectBounds.x + 12, groundSelectBounds.y + 42);
        font.setColor(UI_TEXT);
        float currentTextWidth = groundSelectBounds.width - 42f;
        font.draw(uiBatch, ellipsizeToWidth(currentName, currentTextWidth), groundSelectBounds.x + 12,
                groundSelectBounds.y + 21);
        font.setColor(UI_TEXT_FAINT);
        font.draw(uiBatch, "v", groundSelectBounds.x + groundSelectBounds.width - 18, groundSelectBounds.y + 28);
        uiBatch.end();

        if (!groundSelectOpen) {
            return;
        }

        int maxVisibleRows = Math.max(1,
                (int) ((groundSelectBounds.y - BOTTOM_INFO_HEIGHT - 10f) / MAP_SELECT_ITEM_HEIGHT));
        groundSelectVisibleRows = Math.min(groundFillSprites.size(), maxVisibleRows);
        int maxOffset = Math.max(0, groundFillSprites.size() - groundSelectVisibleRows);
        groundSelectScrollOffset = Math.max(0, Math.min(maxOffset, groundSelectScrollOffset));
        float dropdownHeight = groundSelectVisibleRows * MAP_SELECT_ITEM_HEIGHT;
        float dropdownY = groundSelectBounds.y - dropdownHeight - 2f;
        groundSelectDropdownBounds = new com.badlogic.gdx.math.Rectangle(
                groundSelectBounds.x,
                dropdownY,
                groundSelectBounds.width,
                dropdownHeight);
        groundSelectListTop = dropdownY + dropdownHeight;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UI_SURFACE);
        shapeRenderer.rect(groundSelectDropdownBounds.x, groundSelectDropdownBounds.y, groundSelectDropdownBounds.width,
                groundSelectDropdownBounds.height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UI_BORDER);
        shapeRenderer.rect(groundSelectDropdownBounds.x, groundSelectDropdownBounds.y, groundSelectDropdownBounds.width,
                groundSelectDropdownBounds.height);
        shapeRenderer.end();

        uiBatch.begin();
        int endIndex = Math.min(groundFillSprites.size(), groundSelectScrollOffset + groundSelectVisibleRows);
        for (int i = groundSelectScrollOffset; i < endIndex; i++) {
            int visibleIndex = i - groundSelectScrollOffset;
            float itemY = groundSelectListTop - (visibleIndex + 1) * MAP_SELECT_ITEM_HEIGHT;
            if (i == groundSelectHoverIndex) {
                uiBatch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(UI_SURFACE_HOVER);
                shapeRenderer.rect(groundSelectDropdownBounds.x + 1, itemY + 1, groundSelectDropdownBounds.width - 2,
                        MAP_SELECT_ITEM_HEIGHT - 2);
                shapeRenderer.end();
                uiBatch.begin();
            }

            String name = groundFillSprites.get(i);
            // Case-sensitive: grounds whose names differ only in case are distinct, and an
            // ignore-case match highlights both rows for either selection.
            font.setColor(name.equals(currentName) ? UI_BLUE : UI_TEXT);
            font.draw(uiBatch, ellipsizeToWidth(name, groundSelectDropdownBounds.width - 20f),
                    groundSelectDropdownBounds.x + 10, itemY + 16);
        }
        uiBatch.end();

        if (groundFillSprites.size() > groundSelectVisibleRows) {
            float trackX = groundSelectDropdownBounds.x + groundSelectDropdownBounds.width - 7f;
            float trackY = groundSelectDropdownBounds.y + 3f;
            float trackHeight = groundSelectDropdownBounds.height - 6f;
            float thumbHeight = Math.max(24f,
                    trackHeight * groundSelectVisibleRows / (float) groundFillSprites.size());
            float travel = Math.max(0f, trackHeight - thumbHeight);
            float thumbY = trackY + travel * (1f - groundSelectScrollOffset / (float) maxOffset);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(UI_SURFACE_PRESSED);
            shapeRenderer.rect(trackX, trackY, 4f, trackHeight);
            shapeRenderer.setColor(UI_TEXT_FAINT);
            shapeRenderer.rect(trackX, thumbY, 4f, thumbHeight);
            shapeRenderer.end();
        }
    }

    /**
     * Replicates {@code GroundRenderer.renderGroundBelowDecorInternal} to find which ground
     * sprite is actually drawn underneath a decor tile, and where it comes from.
     * Returns {@code "<groundName> (from <source>)"} or null if no ground is borrowed.
     */
    private String describeBorrowedGround(int tileX, int tileY) {
        BorrowedGround bg = computeBorrowedGround(tileX, tileY);
        return bg == null ? null : bg.name + " (" + bg.source + ")";
    }

    /** Ground name borrowed for a decor tile plus a human-readable provenance label. */
    private static final class BorrowedGround {
        final String name;
        final String source;
        BorrowedGround(String name, String source) {
            this.name = name;
            this.source = source;
        }
    }

    /**
     * Computes a neighbor ground sprite for a decor tile.
     * Returns null when the tile is itself a ground or no ground can be borrowed.
     */
    private BorrowedGround computeBorrowedGround(int tileX, int tileY) {
        var resAt = resolveSpriteAt(tileX, tileY);
        if (resAt == null || resAt.name == null) {
            return null;
        }
        var metaAt = mapRenderer.getMetaByName().get(resAt.name.toLowerCase(Locale.ROOT));
        if (metaAt != null && metaAt.isGround()) {
            return null;
        }

        // Neighbor fallback: below, above, right, left.
        int[][] order = { {0, 1}, {0, -1}, {1, 0}, {-1, 0} };
        String[] labels = { "below", "above", "right", "left" };
        for (int i = 0; i < order.length; i++) {
            int nx = tileX + order[i][0];
            int ny = tileY + order[i][1];
            if (nx < 0 || ny < 0 || nx >= mapReader.getWidth() || ny >= mapReader.getHeight()) {
                continue;
            }
            var nRes = resolveSpriteAt(nx, ny);
            if (nRes == null || nRes.name == null) {
                continue;
            }
            var nMeta = mapRenderer.getMetaByName().get(nRes.name.toLowerCase(Locale.ROOT));
            if (nMeta != null && nMeta.isGround()) {
                return new BorrowedGround(nRes.name, "from " + labels[i] + " " + nx + "," + ny);
            }
        }
        return null;
    }

    private void updateDecorHoverTooltip(int screenX, int screenY) {
        if (!decorVisible || mapReader == null || spriteLoader == null || mapRenderer == null
                || editorMode == EditorMode.SPRITE_PICKER) {
            return;
        }
        int uiY = Gdx.graphics.getHeight() - screenY;
        if (uiY < TOOLBAR_HEIGHT) {
            return;
        }
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        DecorInfo hovered = findDecorAtPosition(worldCoords.x, worldCoords.y);
        if (hovered == null) {
            return;
        }
        float[] drawOffsets = getSpriteDrawOffsets(hovered.spriteName, hovered.isMirrored);
        float offX = drawOffsets[0];
        float offY = drawOffsets[1];
        float scaleX = mapReader.getScaleX(hovered.tileX, hovered.tileY);
        float scaleY = mapReader.getScaleY(hovered.tileX, hovered.tileY);
        String info = String.format(
                "Decor: %s%s | Deep: %d | Tile: (%d, %d) | Size: %dx%d | SpriteOffset: (%d, %d) | Scale: %.2fx%.2f",
                hovered.spriteName,
                hovered.isMirrored ? " [Mirrored]" : "",
                hovered.deep,
                hovered.tileX,
                hovered.tileY,
                hovered.width,
                hovered.height,
                (int) offX,
                (int) offY,
                scaleX,
                scaleY);
        String borrowedGround = describeBorrowedGround(hovered.tileX, hovered.tileY);
        if (borrowedGround != null) {
            info += " | Ground: " + borrowedGround;
        }
        hoverInfo = info;
    }

    private void updateGroundHoverTooltip(int screenX, int screenY) {
        if (hoverInfo != null || mapReader == null || spriteLoader == null || mapRenderer == null
                || editorMode == EditorMode.SPRITE_PICKER) {
            return;
        }

        int uiY = Gdx.graphics.getHeight() - screenY;
        if (uiY < TOOLBAR_HEIGHT) {
            return;
        }

        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int) (worldCoords.x / GameConstants.GRID_W);
        int tileY = (int) (worldCoords.y / GameConstants.GRID_H);

        if (tileX < 0 || tileY < 0 || tileX >= mapReader.getWidth() || tileY >= mapReader.getHeight()) {
            return;
        }

        ResolvedSprite resolved = resolveSpriteAt(tileX, tileY);
        if (resolved == null || resolved.name == null) {
            return;
        }

        var meta = mapRenderer.getMetaByName().get(resolved.name.toLowerCase(Locale.ROOT));
        if (meta == null || !meta.isGround()) {
            return;
        }

        TextureRegion region = spriteLoader.getRegionFromSpriteName(resolved.name);
        if (region == null) {
            return;
        }

        float[] drawOffsets = getSpriteDrawOffsets(resolved.name, resolved.mirror);
        float offX = drawOffsets[0];
        float offY = drawOffsets[1];
        float scaleX = mapReader.getScaleX(tileX, tileY);
        float scaleY = mapReader.getScaleY(tileX, tileY);

        String info = String.format(
                "Decor: %s%s | Tile: (%d, %d) | Size: %dx%d | SpriteOffset: (%d, %d) | Scale: %.2fx%.2f",
                resolved.name,
                resolved.mirror ? " [Mirrored]" : "",
                tileX,
                tileY,
                region.getRegionWidth(),
                region.getRegionHeight(),
                (int) offX,
                (int) offY,
                scaleX,
                scaleY);
        hoverInfo = info;
    }

    private void updateHoveredTileCoordinates(int screenX, int screenY) {
        hoveredTileX = -1;
        hoveredTileY = -1;
        if (mapReader == null || editorMode == EditorMode.SPRITE_PICKER) {
            return;
        }

        int uiY = Gdx.graphics.getHeight() - screenY;
        if (uiY < TOOLBAR_HEIGHT) {
            return;
        }

        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int) Math.floor(worldCoords.x / GameConstants.GRID_W);
        int tileY = (int) Math.floor(worldCoords.y / GameConstants.GRID_H);

        if (tileX < 0 || tileY < 0 || tileX >= mapReader.getWidth() || tileY >= mapReader.getHeight()) {
            return;
        }

        hoveredTileX = tileX;
        hoveredTileY = tileY;
    }

    private void refreshHoverInfoAtMouse() {
        hoverInfo = null;
        hoveredObjectPositionIndex = -1;
        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        updateHoveredTileCoordinates(mx, my);
        updateObjectHoverTooltip(mx, my);
        updateDecorHoverTooltip(mx, my);
        updateGroundHoverTooltip(mx, my);
        lastHoverUpdateMs = TimeUtils.millis();
        lastHoverScreenX = mx;
        lastHoverScreenY = my;
    }

    private void updateObjectHoverTooltip(int screenX, int screenY) {
        hoveredObjectPositionIndex = -1;
        if (mapReader == null || mapRenderer == null || spriteLoader == null || editorMode == EditorMode.SPRITE_PICKER) {
            return;
        }
        int uiY = Gdx.graphics.getHeight() - screenY;
        if (uiY < TOOLBAR_HEIGHT) {
            return;
        }
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        int index = findObjectAtWorld(worldCoords.x, worldCoords.y);
        if (index < 0 || index >= objectPositions.size()) {
            return;
        }
        hoveredObjectPositionIndex = index;
        ObjectPos pos = objectPositions.get(index);
        hoverInfo = "Object: " + pos.name() + " | Tile: (" + (int) pos.x() + ", " + (int) pos.y() + ", z" + (int) pos.z() + ")";
    }

    /**
     * Gérer les clics sur la toolbar
     */
    private boolean handleMenuClick(int screenX, int screenY) {
        int screenHeight = Gdx.graphics.getHeight();
        int y = screenHeight - screenY;

        if (y >= screenHeight - MENU_BAR_HEIGHT) {
            for (MenuTitle menu : menuTitles) {
                if (menu.bounds.contains(screenX, y)) {
                    openMenu = menu.name.equals(openMenu) ? null : menu.name;
                    openSubMenu = null;
                    openNestedSubMenu = null;
                    return true;
                }
            }
            openMenu = null;
            openSubMenu = null;
            openNestedSubMenu = null;
            return true;
        }

        MenuTitle open = getOpenMenu();
        if (open != null) {
            float menuWidth = getMenuDropdownWidth(open);
            float menuX = open.bounds.x;
            float menuTop = open.bounds.y;
            float itemHeight = 26f;
            float menuBottom = menuTop - open.items.size() * itemHeight;
            MenuItem subMenu = getOpenSubMenu(open);
            if (subMenu != null) {
                float subWidth = getMenuItemsWidth(subMenu.children);
                float subX = menuX + menuWidth - 1f;
                int parentIndex = open.items.indexOf(subMenu);
                float subTop = menuTop - parentIndex * itemHeight;
                float subBottom = subTop - subMenu.children.size() * itemHeight;
                MenuItem nestedSubMenu = getOpenNestedSubMenu(subMenu);
                if (nestedSubMenu != null) {
                    int nestedParentIndex = subMenu.children.indexOf(nestedSubMenu);
                    float nestedWidth = getMenuItemsWidth(nestedSubMenu.children);
                    float nestedX = subX + subWidth - 1f;
                    float nestedTop = subTop - nestedParentIndex * itemHeight;
                    float nestedBottom = nestedTop - nestedSubMenu.children.size() * itemHeight;
                    if (screenX >= nestedX && screenX <= nestedX + nestedWidth && y >= nestedBottom && y <= nestedTop) {
                        int nestedIndex = (int) ((nestedTop - y) / itemHeight);
                        if (nestedIndex >= 0 && nestedIndex < nestedSubMenu.children.size()) {
                            MenuItem nestedChild = nestedSubMenu.children.get(nestedIndex);
                            openMenu = null;
                            openSubMenu = null;
                            openNestedSubMenu = null;
                            if (nestedChild.action != null) {
                                nestedChild.action.run();
                            }
                            return true;
                        }
                    }
                }
                if (screenX >= subX && screenX <= subX + subWidth && y >= subBottom && y <= subTop) {
                    int childIndex = (int) ((subTop - y) / itemHeight);
                    if (childIndex >= 0 && childIndex < subMenu.children.size()) {
                        MenuItem child = subMenu.children.get(childIndex);
                        if (child.hasChildren()) {
                            openNestedSubMenu = child.label;
                            return true;
                        }
                        openMenu = null;
                        openSubMenu = null;
                        openNestedSubMenu = null;
                        if (child.action != null) {
                            child.action.run();
                        }
                        return true;
                    }
                }
            }
            if (screenX >= menuX && screenX <= menuX + menuWidth && y >= menuBottom && y <= menuTop) {
                int index = (int) ((menuTop - y) / itemHeight);
                if (index >= 0 && index < open.items.size()) {
                    MenuItem item = open.items.get(index);
                    if (item.hasChildren()) {
                        openSubMenu = item.label;
                        openNestedSubMenu = null;
                    } else if (item.action != null) {
                        openMenu = null;
                        openSubMenu = null;
                        openNestedSubMenu = null;
                        item.action.run();
                    }
                    return true;
                }
            }
            openMenu = null;
            openSubMenu = null;
            openNestedSubMenu = null;
            return true;
        }
        return false;
    }

    private boolean handleToolbarClick(int screenX, int screenY) {
        return false;
    }

    private boolean handleMinimapClick(int screenX, int screenY, int button) {
        if (button != Input.Buttons.LEFT || mapReader == null) {
            return false;
        }
        int screenWidth = Gdx.graphics.getWidth();
        int uiY = Gdx.graphics.getHeight() - screenY;
        int minimapX = screenWidth - MINIMAP_SIZE - MINIMAP_MARGIN;
        int minimapY = BOTTOM_INFO_HEIGHT + MINIMAP_MARGIN;
        com.badlogic.gdx.math.Rectangle mapBounds = minimapMapBounds(minimapX, minimapY);
        if (screenX < mapBounds.x || screenX > mapBounds.x + mapBounds.width
                || uiY < mapBounds.y || uiY > mapBounds.y + mapBounds.height) {
            return false;
        }

        float nx = (screenX - mapBounds.x) / mapBounds.width;
        float ny = (uiY - mapBounds.y) / mapBounds.height;
        nx = clampFloat(nx, 0f, 1f);
        ny = clampFloat(ny, 0f, 1f);

        float mapWidthWorld = mapReader.getWidth() * GameConstants.GRID_W;
        float mapHeightWorld = mapReader.getHeight() * GameConstants.GRID_H;
        float worldX = nx * mapWidthWorld;
        float worldY = mapHeightWorld - ny * mapHeightWorld;

        cameraPosition.set(worldX, worldY);
        camera.position.set(cameraPosition.x, cameraPosition.y, 0);
        camera.zoom = zoom;
        camera.update();
        return true;
    }

    private MenuTitle getOpenMenu() {
        if (openMenu == null) {
            return null;
        }
        for (MenuTitle menu : menuTitles) {
            if (menu.name.equals(openMenu)) {
                return menu;
            }
        }
        return null;
    }

    private float getMenuDropdownWidth(MenuTitle menu) {
        return getMenuItemsWidth(menu.items);
    }

    private float getMenuItemsWidth(List<MenuItem> items) {
        float width = 176f;
        for (MenuItem item : items) {
            GlyphLayout layout = new GlyphLayout(font, item.label);
            width = Math.max(width, layout.width + (item.hasChildren() ? 58f : 42f));
        }
        return width;
    }

    private MenuItem getOpenSubMenu(MenuTitle menu) {
        if (menu == null || openSubMenu == null) {
            return null;
        }
        for (MenuItem item : menu.items) {
            if (item.hasChildren() && item.label.equals(openSubMenu)) {
                return item;
            }
        }
        return null;
    }

    private MenuItem getOpenNestedSubMenu(MenuItem subMenu) {
        if (subMenu == null || openNestedSubMenu == null) {
            return null;
        }
        for (MenuItem item : subMenu.children) {
            if (item.hasChildren() && item.label.equals(openNestedSubMenu)) {
                return item;
            }
        }
        return null;
    }

    private void renderOpenMenu(int screenHeight) {
        MenuTitle open = getOpenMenu();
        if (open == null) {
            return;
        }
        float itemHeight = 26f;
        float width = getMenuDropdownWidth(open);
        float height = open.items.size() * itemHeight;
        float x = open.bounds.x;
        float top = open.bounds.y;
        float y = top - height;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UI_SURFACE);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(UI_BLUE);
        shapeRenderer.rect(x, y, 3f, height);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UI_BORDER);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        uiBatch.begin();
        for (int i = 0; i < open.items.size(); i++) {
            MenuItem item = open.items.get(i);
            float itemY = top - (i + 1) * itemHeight;
            boolean active = item.activeSupplier != null && item.activeSupplier.getAsBoolean();
            font.setColor(active ? UI_BLUE : UI_TEXT);
            font.draw(uiBatch, active ? "*" : "", x + 10f, itemY + 18f);
            font.draw(uiBatch, item.label, x + 24f, itemY + 18f);
            if (item.hasChildren()) {
                font.draw(uiBatch, ">", x + width - 20f, itemY + 18f);
            }
        }
        uiBatch.end();

        MenuItem subMenu = getOpenSubMenu(open);
        if (subMenu != null) {
            int parentIndex = open.items.indexOf(subMenu);
            float subWidth = getMenuItemsWidth(subMenu.children);
            float subHeight = subMenu.children.size() * itemHeight;
            float subX = x + width - 1f;
            float subTop = top - parentIndex * itemHeight;
            float subY = subTop - subHeight;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(UI_SURFACE);
            shapeRenderer.rect(subX, subY, subWidth, subHeight);
            shapeRenderer.setColor(UI_BLUE);
            shapeRenderer.rect(subX, subY, 3f, subHeight);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(UI_BORDER);
            shapeRenderer.rect(subX, subY, subWidth, subHeight);
            shapeRenderer.end();

            uiBatch.begin();
            for (int i = 0; i < subMenu.children.size(); i++) {
                MenuItem child = subMenu.children.get(i);
                float itemY = subTop - (i + 1) * itemHeight;
                boolean active = child.activeSupplier != null && child.activeSupplier.getAsBoolean();
                font.setColor(active ? UI_BLUE : UI_TEXT);
                font.draw(uiBatch, active ? "*" : "", subX + 10f, itemY + 18f);
                font.draw(uiBatch, child.label, subX + 24f, itemY + 18f);
                if (child.hasChildren()) {
                    font.draw(uiBatch, ">", subX + subWidth - 20f, itemY + 18f);
                }
            }
            uiBatch.end();

            MenuItem nestedSubMenu = getOpenNestedSubMenu(subMenu);
            if (nestedSubMenu != null) {
                int nestedParentIndex = subMenu.children.indexOf(nestedSubMenu);
                float nestedWidth = getMenuItemsWidth(nestedSubMenu.children);
                float nestedHeight = nestedSubMenu.children.size() * itemHeight;
                float nestedX = subX + subWidth - 1f;
                float nestedTop = subTop - nestedParentIndex * itemHeight;
                float nestedY = nestedTop - nestedHeight;

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(UI_SURFACE);
                shapeRenderer.rect(nestedX, nestedY, nestedWidth, nestedHeight);
                shapeRenderer.setColor(UI_BLUE);
                shapeRenderer.rect(nestedX, nestedY, 3f, nestedHeight);
                shapeRenderer.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(UI_BORDER);
                shapeRenderer.rect(nestedX, nestedY, nestedWidth, nestedHeight);
                shapeRenderer.end();

                uiBatch.begin();
                for (int i = 0; i < nestedSubMenu.children.size(); i++) {
                    MenuItem child = nestedSubMenu.children.get(i);
                    float itemY = nestedTop - (i + 1) * itemHeight;
                    boolean active = child.activeSupplier != null && child.activeSupplier.getAsBoolean();
                    font.setColor(active ? UI_BLUE : UI_TEXT);
                    font.draw(uiBatch, active ? "*" : "", nestedX + 10f, itemY + 18f);
                    font.draw(uiBatch, child.label, nestedX + 24f, itemY + 18f);
                }
                uiBatch.end();
            }
        }
    }

    /**
     * Gérer le survol de la toolbar
     */
    private void handleToolbarMouseMove(int screenX, int screenY) {
        int y = Gdx.graphics.getHeight() - screenY;
        toolbarTooltip = null;

        if (autofillEnabled && groundSelectBounds != null && groundSelectBounds.contains(screenX, y)) {
            toolbarTooltip = "Fill ground";
        }
        if (monsterPlacementEnabled && monsterStaticBounds != null && monsterStaticBounds.contains(screenX, y)) {
            toolbarTooltip = "Static monster: no patrol, chase, or attack";
        }
        if (npcPlacementEnabled && npcStaticBounds != null && npcStaticBounds.contains(screenX, y)) {
            toolbarTooltip = "Static NPC: no patrol movement";
        }

        for (ToolbarButton button : toolbarButtons) {
            if (button.bounds.contains(screenX, y)) {
                button.hovered = true;
                toolbarTooltip = button.tooltip;
            } else {
                button.hovered = false;
            }
        }

        updateGroundSelectHover(screenX, y);
    }

    private void syncCurrentMapIndex() {
        currentMapPath = normalizeMapPath(currentMapPath);
        int index = availableMaps.indexOf(currentMapPath);
        currentMapIndex = index >= 0 ? index : 0;
        if (index < 0 && !availableMaps.isEmpty()) {
            currentMapPath = availableMaps.get(0);
        }
    }

    private String getMapDisplayName(String mapPath) {
        if (mapPath == null) {
            return "Unknown";
        }
        File file = new File(mapPath);
        String name = file.getName().replace(".mapbin", "").replace(".map", "").replace(".json.gz", "");
        File parent = file.getParentFile();
        if (parent != null && parent.getName() != null && !parent.getName().equalsIgnoreCase("maps")) {
            return parent.getName() + "/" + name;
        }
        return name;
    }

    private void selectMapByIndex(int index) {
        if (index < 0 || index >= availableMaps.size()) {
            return;
        }

        startMapSwitchLoading(availableMaps.get(index), index);
    }

    private void resetMapSelections() {
        selectedTileX = -1;
        selectedTileY = -1;
        selectedDecorInfo = null;
        selectedMonsterSpawn = null;
        selectedNpcSpawn = null;
        selectedMusicZone = null;
        rectangleSelection = null;
        spawnContextOpen = false;
    }

    private void createNewMap() {
        File current = new File(currentMapPath);
        if (!current.exists()) {
            showEditorMessage("Error: Map file not found");
            return;
        }

        File mapsDir = current.getParentFile();
        String newName = "NewMap_" + System.currentTimeMillis() + ".mapbin";
        File newMap = new File(mapsDir, newName);

        try {
            Files.copy(current.toPath(), newMap.toPath());
            availableMaps.add(normalizeMapPath(newMap.getPath()));
            selectMapByIndex(availableMaps.size() - 1);
            showEditorMessage("New map created: " + getMapDisplayName(newName));
        } catch (IOException e) {
            log.error("Failed to create new map", e);
            showEditorMessage("Error: Failed to create new map");
        }
    }

    private void renderMonsterSpawns() {
        if (monsterSpawns.isEmpty()) return;
        for (MonsterSpawnEntry spawn : monsterSpawns) {
            renderSpawnMarker(spawn, spawn == selectedMonsterSpawn, new Color(0.95f, 0.25f, 0.18f, 0.85f), false);
        }
    }

    private void renderNpcSpawns() {
        if (npcSpawns.isEmpty()) return;
        for (MonsterSpawnEntry spawn : npcSpawns) {
            renderSpawnMarker(spawn, spawn == selectedNpcSpawn, new Color(0.2f, 0.8f, 0.45f, 0.85f), true);
        }
    }

    private void renderSpawnMarker(MonsterSpawnEntry spawn, boolean selected, Color color, boolean isNpc) {
        float x = spawn.x * GameConstants.GRID_W;
        float y = spawn.y * GameConstants.GRID_H;
        float cx = x + GameConstants.GRID_W * 0.5f;
        float markerPadding = 3f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x + markerPadding, y + markerPadding,
                GameConstants.GRID_W - markerPadding * 2f, GameConstants.GRID_H - markerPadding * 2f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(selected ? Color.YELLOW : Color.BLACK);
        shapeRenderer.rect(x + markerPadding, y + markerPadding,
                GameConstants.GRID_W - markerPadding * 2f, GameConstants.GRID_H - markerPadding * 2f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        Vector3 labelScreen = camera.project(new Vector3(cx, y + GameConstants.GRID_H + 3f, 0f));

        com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
        uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiBatch.setProjectionMatrix(uiMatrix);
        uiBatch.begin();
        String flags = (spawn.stationary ? "S" : "") + (spawn.aggressive ? "A" : "");
        String markerText = (flags.isBlank() ? "" : "[" + flags + "] ") + spawnDisplayName(spawn.type, isNpc);
        GlyphLayout textLayout = new GlyphLayout(font, markerText);
        float textX = labelScreen.x - textLayout.width / 2f;
        float textY = labelScreen.y + textLayout.height + 2f;
        font.setColor(0f, 0f, 0f, 0.82f);
        font.draw(uiBatch, markerText, textX + 1f, textY - 1f);
        font.setColor(Color.WHITE);
        font.draw(uiBatch, markerText, textX, textY);
        uiBatch.end();
    }

    /** Resolves a spawn's raw type/codeId (e.g. "Olin Haad Guard 3") to its player-facing displayName. */
    private String spawnDisplayName(String type, boolean isNpc) {
        if (type == null) {
            return null;
        }
        String displayName = isNpc
                ? (NpcRegistry.findByName(type) != null ? NpcRegistry.findByName(type).getDisplayName() : null)
                : (MonsterRegistry.findByName(type) != null ? MonsterRegistry.findByName(type).getDisplayName() : null);
        return displayName != null && !displayName.isEmpty() ? displayName : type;
    }

    private MonsterSpawnEntry findSpawnAt(List<MonsterSpawnEntry> spawns, int tileX, int tileY) {
        for (MonsterSpawnEntry s : spawns) {
            if (s.x == tileX && s.y == tileY) return s;
        }
        return null;
    }

    private boolean handleSpawnMapClick(int tileX, int tileY) {
        spawnContextOpen = false;
        MonsterSpawnEntry clickedNpc = findSpawnAt(npcSpawns, tileX, tileY);
        if (clickedNpc != null) {
            selectedNpcSpawn = clickedNpc;
            selectedMonsterSpawn = null;
            npcStaticPlacement = clickedNpc.stationary;
            showEditorMessage("NPC selected: " + spawnDisplayName(clickedNpc.type, true) + " (" + tileX + ", " + tileY + ")");
            return true;
        }

        MonsterSpawnEntry clickedMonster = findSpawnAt(monsterSpawns, tileX, tileY);
        if (clickedMonster != null) {
            selectedMonsterSpawn = clickedMonster;
            selectedNpcSpawn = null;
            monsterStaticPlacement = clickedMonster.stationary;
            showEditorMessage("Monster selected: " + spawnDisplayName(clickedMonster.type, false) + " (" + tileX + ", " + tileY + ")");
            return true;
        }

        if (selectedNpcSpawn != null && !npcPlacementEnabled && !monsterPlacementEnabled) {
            selectedNpcSpawn.x = tileX;
            selectedNpcSpawn.y = tileY;
            npcsDirty = true;
            showEditorMessage("NPC moved: (" + tileX + ", " + tileY + ")");
            return true;
        }

        if (selectedMonsterSpawn != null && !monsterPlacementEnabled && !npcPlacementEnabled) {
            selectedMonsterSpawn.x = tileX;
            selectedMonsterSpawn.y = tileY;
            monstersDirty = true;
            showEditorMessage("Monster moved: (" + tileX + ", " + tileY + ")");
            return true;
        }

        return false;
    }

    private boolean openSpawnContextMenu(int tileX, int tileY, int screenX, int screenY) {
        MonsterSpawnEntry npc = findSpawnAt(npcSpawns, tileX, tileY);
        MonsterSpawnEntry monster = findSpawnAt(monsterSpawns, tileX, tileY);
        if (npc == null && monster == null) {
            spawnContextOpen = false;
            return false;
        }

        spawnContextNpc = npc != null;
        spawnContextTarget = spawnContextNpc ? npc : monster;
        selectedNpcSpawn = spawnContextNpc ? spawnContextTarget : null;
        selectedMonsterSpawn = spawnContextNpc ? null : spawnContextTarget;
        if (spawnContextNpc) {
            npcStaticPlacement = spawnContextTarget.stationary;
        } else {
            monsterStaticPlacement = spawnContextTarget.stationary;
        }

        float menuWidth = 190f;
        float menuHeight = 78f;
        float y = Gdx.graphics.getHeight() - screenY;
        spawnContextX = Math.min(screenX, Gdx.graphics.getWidth() - menuWidth - 8f);
        spawnContextY = Math.max(8f, Math.min(y - menuHeight, Gdx.graphics.getHeight() - MENU_BAR_HEIGHT - menuHeight));
        spawnContextBounds = new com.badlogic.gdx.math.Rectangle(spawnContextX, spawnContextY, menuWidth, menuHeight);
        spawnContextMenu = new EditorContextMenu(spawnContextNpc ? "NPC" : "Monster",
                spawnContextNpc ? new Color(0.18f, 0.64f, 0.38f, 1f) : new Color(0.78f, 0.24f, 0.20f, 1f))
                .add((spawnContextTarget.stationary ? "[x] " : "[ ] ") + "Static", this::toggleSpawnContextStatic)
                .add((spawnContextTarget.aggressive ? "[x] " : "[ ] ") + "Aggressive", this::toggleSpawnContextAggressive)
                .onClose(() -> spawnContextOpen = false);
        spawnContextMenu.openAt(screenX, screenY, menuWidth, MENU_BAR_HEIGHT);
        spawnContextOpen = true;
        return true;
    }

    private boolean handleSpawnContextClick(int screenX, int screenY, int button) {
        if (!spawnContextOpen || spawnContextMenu == null) {
            return false;
        }
        return spawnContextMenu.handleClick(screenX, screenY, button);
    }

    private void toggleSpawnContextStatic() {
        if (spawnContextTarget != null) {
            if (spawnContextNpc && !"Darkfang".equals(spawnContextTarget.type)) {
                spawnContextTarget.stationary = false;
                npcStaticPlacement = false;
                npcsDirty = true;
                showEditorMessage("Only Darkfang can be static");
                return;
            }
            spawnContextTarget.stationary = !spawnContextTarget.stationary;
            if (spawnContextNpc) {
                npcStaticPlacement = spawnContextTarget.stationary;
                npcsDirty = true;
            } else {
                monsterStaticPlacement = spawnContextTarget.stationary;
                monstersDirty = true;
            }
            showEditorMessage((spawnContextNpc ? "NPC" : "Monster") + " static: "
                    + (spawnContextTarget.stationary ? "ON" : "OFF"));
        }
    }

    private void toggleSpawnContextAggressive() {
        if (spawnContextTarget != null) {
            spawnContextTarget.aggressive = !spawnContextTarget.aggressive;
            if (spawnContextNpc) {
                npcsDirty = true;
            } else {
                monstersDirty = true;
            }
            showEditorMessage((spawnContextNpc ? "NPC" : "Monster") + " aggressive: "
                    + (spawnContextTarget.aggressive ? "ON" : "OFF"));
        }
    }

    private void renderSpawnContextMenu() {
        if (!spawnContextOpen || spawnContextTarget == null || spawnContextMenu == null) {
            return;
        }
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        prepareUiProjection(screenWidth, screenHeight);
        spawnContextMenu.render(uiBatch, shapeRenderer, font);
    }

    private void openDecorContextMenu(DecorInfo decor, int screenX, int screenY) {
        if (decor == null) {
            decorContextOpen = false;
            return;
        }
        decorContextTarget = decor;
        float menuWidth = 260f;
        float menuHeight = 76f;
        float y = Gdx.graphics.getHeight() - screenY;
        float x = Math.min(screenX, Gdx.graphics.getWidth() - menuWidth - 8f);
        float menuY = Math.max(8f, Math.min(y - menuHeight, Gdx.graphics.getHeight() - MENU_BAR_HEIGHT - menuHeight));
        decorContextBounds = new com.badlogic.gdx.math.Rectangle(x, menuY, menuWidth, menuHeight);
        decorContextMenu = new EditorContextMenu("Decor", new Color(0.18f, 0.52f, 0.82f, 1f))
                .add("Mirror", () -> {
                    if (decorContextTarget != null) {
                        toggleMirrorAt(decorContextTarget.tileX, decorContextTarget.tileY, decorContextTarget);
                    }
                });
        decorContextMenu.addSubmenu("Deep")
                .add("+1", () -> adjustDecorDeep(decorContextTarget, 1))
                .add("0", () -> setDecorDeep(decorContextTarget, 0))
                .add("-1", () -> adjustDecorDeep(decorContextTarget, -1));
        decorContextMenu.add("Collision rule editor", () -> {
                    if (decorContextTarget != null) {
                        openCollisionRuleEditorForSprite(decorContextTarget.spriteName);
                    }
                })
                .onClose(() -> decorContextOpen = false);
        decorContextMenu.openAt(screenX, screenY, menuWidth, MENU_BAR_HEIGHT);
        decorContextOpen = true;
    }

    private void adjustDecorDeep(DecorInfo decor, int delta) {
        if (decor == null || mapReader == null) {
            return;
        }
        setDecorDeep(decor, mapReader.getZOrder(decor.tileX, decor.tileY) + delta);
    }

    private void setDecorDeep(DecorInfo decor, int deep) {
        if (decor == null || mapReader == null || mapRenderer == null) {
            return;
        }
        int oldDeep = mapReader.getZOrder(decor.tileX, decor.tileY);
        if (oldDeep == deep) {
            showEditorMessage("Deep: " + deep + " (" + decor.spriteName + ")");
            return;
        }
        UndoEntry undoEntry = newUndoEntry("Set decor deep", false);
        undoEntry.changes.add(new TileChange(
                decor.tileX,
                decor.tileY,
                mapReader.getSpriteName(decor.tileX, decor.tileY),
                mapReader.getScaleX(decor.tileX, decor.tileY),
                mapReader.getScaleY(decor.tileX, decor.tileY),
                mapReader.getOffsetX(decor.tileX, decor.tileY),
                mapReader.getOffsetY(decor.tileX, decor.tileY),
                oldDeep,
                mapReader.getSpriteName(decor.tileX, decor.tileY),
                mapReader.getScaleX(decor.tileX, decor.tileY),
                mapReader.getScaleY(decor.tileX, decor.tileY),
                mapReader.getOffsetX(decor.tileX, decor.tileY),
                mapReader.getOffsetY(decor.tileX, decor.tileY),
                deep));
        pushUndoEntry(undoEntry);
        mapReader.setZOrder(decor.tileX, decor.tileY, deep);
        decor.deep = deep;
        mapDirty = true;
        mapRenderer.invalidateDecorTileCache(decor.tileX, decor.tileY);
        invalidateGroundAround(decor.tileX, decor.tileY, decor.spriteName);
        showEditorMessage("Deep: " + deep + " (" + decor.spriteName + ")");
    }

    private boolean handleDecorContextClick(int screenX, int screenY, int button) {
        if (!decorContextOpen || decorContextMenu == null) {
            return false;
        }
        return decorContextMenu.handleClick(screenX, screenY, button);
    }

    private void renderDecorContextMenu() {
        if (!decorContextOpen || decorContextTarget == null || decorContextMenu == null) {
            return;
        }
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        prepareUiProjection(screenWidth, screenHeight);
        decorContextMenu.render(uiBatch, shapeRenderer, font);
    }

    private void openMonsterPicker() {
        loadMonsterTypes();
        if (monsterTypes.isEmpty()) {
            showEditorMessage("No monster types found");
            return;
        }
        entityPicker = new EntityPickerUI("Monster Placement", true, monsterTypes);
        openMenu = null;
    }

    private void loadMonsterTypes() {
        if (!monsterTypes.isEmpty()) return;
        try {
            for (MonsterDef def : MonsterRegistry.load()) {
                if (def == null || def.getName() == null) continue;
                String label = def.getDisplayName() != null && !def.getDisplayName().isEmpty()
                        ? def.getDisplayName() : def.getName();
                String thumb = firstMonsterSpriteFromPattern(def.getWalkPattern());
                monsterTypes.add(new MonsterTypeEntry(def.getName(), label,
                        thumb != null ? thumb : def.getName() + "000-a"));
            }
            monsterTypes.sort(Comparator.comparing(m -> m.displayName.toLowerCase(Locale.ROOT)));
            if (!monsterTypes.isEmpty() && selectedMonsterType == null) selectedMonsterType = monsterTypes.get(0);
        } catch (Exception e) {
            log.warn("Failed to load monster types", e);
        }
    }

    private String firstMonsterSpriteFromPattern(String pattern) {
        if (pattern == null || pattern.isBlank() || !pattern.contains("#")) {
            return null;
        }
        String baseName = pattern.split("#", 2)[0];
        if (baseName.isBlank()) {
            return null;
        }
        String angled = baseName.endsWith("000") ? baseName : baseName + "000";
        return angled + "-a";
    }

    private void loadMonsterSpawns() {
        monsterSpawns.clear();
        File bin = new File(Paths.MONSTER_SPAWNS_BIN);
        if (!bin.exists()) {
            return;
        }
        try {
            int z = getCurrentMapZ();
            for (SpawnBinaryIO.Entry entry : SpawnBinaryIO.read(bin)) {
                if (entry.z == z) {
                    monsterSpawns.add(fromSpawnBinaryEntry(entry));
                }
            }
        } catch (Exception e) {
            log.error("Failed to load monster spawns from {}", bin, e);
        }
        monstersDirty = false;
    }

    private void saveMonsterSpawns() {
        try {
            writeGlobalSpawns(new File(Paths.MONSTER_SPAWNS_BIN), monsterSpawns);
            monstersDirty = false;
            showEditorMessage("Monster spawns saved");
        } catch (Exception e) {
            log.error("Failed to save monster spawns", e);
            showEditorMessage("Error: Failed to save monster spawns");
        }
    }

    private void openNpcPicker() {
        loadNpcTypes();
        if (npcTypes.isEmpty()) {
            showEditorMessage("No NPC types found");
            return;
        }
        entityPicker = new EntityPickerUI("NPC Placement", false, npcTypes);
        openMenu = null;
    }

    private void loadNpcTypes() {
        if (!npcTypes.isEmpty()) return;
        try {
            for (NpcDef def : NpcRegistry.load()) {
                if (def == null || def.getName() == null) continue;
                String label = def.getDisplayName() != null && !def.getDisplayName().isEmpty()
                        ? def.getDisplayName() : def.getName();
                String previewBase = def.getSpriteBase();
                if (def.getParts() != null) {
                    for (NpcDef.Part part : def.getParts()) {
                        if (part != null && part.getBodyPart() == com.perso.T4C.player.BodyPart.BODY) {
                            if (previewBase == null || previewBase.isEmpty()) {
                                previewBase = part.getSpriteBase();
                            }
                            break;
                        }
                    }
                }
                npcTypes.add(new MonsterTypeEntry(def.getName(), label,
                        (previewBase != null && !previewBase.isEmpty() ? previewBase : def.getName()) + "000-a"));
            }
            npcTypes.sort(Comparator.comparing(n -> n.displayName.toLowerCase(Locale.ROOT)));
            if (!npcTypes.isEmpty() && selectedNpcType == null) selectedNpcType = npcTypes.get(0);
        } catch (Exception e) {
            log.warn("Failed to load NPC types", e);
        }
    }

    private void loadNpcSpawns() {
        npcSpawns.clear();
        File bin = new File(Paths.NPC_SPAWNS_BIN);
        if (!bin.exists()) {
            return;
        }
        try {
            int z = getCurrentMapZ();
            for (SpawnBinaryIO.Entry entry : SpawnBinaryIO.read(bin)) {
                if (entry.z == z) {
                    npcSpawns.add(fromSpawnBinaryEntry(entry));
                }
            }
        } catch (Exception e) {
            log.error("Failed to load NPC spawns from {}", bin, e);
        }
        npcsDirty = false;
    }

    private void saveNpcSpawns() {
        try {
            enforceNpcStationaryRule(npcSpawns);
            writeGlobalSpawns(new File(Paths.NPC_SPAWNS_BIN), npcSpawns);
            npcsDirty = false;
            showEditorMessage("NPC spawns saved");
        } catch (Exception e) {
            log.error("Failed to save NPC spawns", e);
            showEditorMessage("Error: Failed to save NPC spawns");
        }
    }

    private void enforceNpcStationaryRule(List<MonsterSpawnEntry> spawns) {
        if (spawns == null) {
            return;
        }
        for (MonsterSpawnEntry spawn : spawns) {
            if (spawn != null) {
                spawn.stationary = "Darkfang".equals(spawn.type);
            }
        }
    }

    private List<SpawnBinaryIO.Entry> toSpawnBinaryEntries(List<MonsterSpawnEntry> spawns) {
        List<SpawnBinaryIO.Entry> entries = new ArrayList<>(spawns.size());
        for (MonsterSpawnEntry spawn : spawns) {
            SpawnBinaryIO.Entry entry = new SpawnBinaryIO.Entry();
            entry.type = spawn.type;
            entry.x = spawn.x;
            entry.y = spawn.y;
            entry.z = spawn.z;
            entry.stationary = spawn.stationary;
            entry.aggressive = spawn.aggressive;
            entries.add(entry);
        }
        return entries;
    }

    private void writeGlobalSpawns(File file, List<MonsterSpawnEntry> currentMapSpawns) throws Exception {
        int z = getCurrentMapZ();
        List<SpawnBinaryIO.Entry> merged = new ArrayList<>();
        if (file.exists()) {
            for (SpawnBinaryIO.Entry entry : SpawnBinaryIO.read(file)) {
                if (entry.z != z) {
                    merged.add(entry);
                }
            }
        }
        for (SpawnBinaryIO.Entry entry : toSpawnBinaryEntries(currentMapSpawns)) {
            entry.z = z;
            merged.add(entry);
        }
        SpawnBinaryIO.write(file, merged);
    }

    private MonsterSpawnEntry fromSpawnBinaryEntry(SpawnBinaryIO.Entry entry) {
        MonsterSpawnEntry spawn = new MonsterSpawnEntry(entry.type, entry.x, entry.y, entry.stationary);
        spawn.z = entry.z;
        spawn.aggressive = entry.aggressive;
        return spawn;
    }

}
