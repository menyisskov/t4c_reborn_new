package com.perso.T4C.config;
/**
 * Class representing GameConstants.
 */

public class GameConstants {
    // Width of the game window in pixels.
    public final static int WINDOW_WIDTH = 1280;

    // Height of the game window in pixels.
    public final static int WINDOW_HEIGHT = 768;

    // Launches the game in fullscreen mode when enabled.
    public static final boolean FULLSCREEN = false;

    // Default opacity percentage used by in-game screen and HUD backgrounds.
    public static final float OVERLAY_PERCENTAGE_DEFAULT = 75f;

    // Width of one map tile in world pixels.
    public static final int GRID_W = 32;

    // Height of one map tile in world pixels.
    public static final int GRID_H = 16;

    // Player movement speed in world pixels per second.
    public static final float PLAYER_SPEED = 300f;

    // Unit::MoveUnit advances userSpeed=1 complete logical cell per request.
    public static final float PLAYER_MOVEMENT_RESERVATION_STEP_TILES = 1f;

    // The Java render anchor is at the puppet's feet: only the visible body
    // above that anchor needs an extra collision cell.
    public static final int PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES = 0;
    public static final int PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES = 1;
    public static final int PLAYER_COLLISION_FOOTPRINT_BELOW_TILES = 0;

    // Draws the border of GuiBoxedText zones so their position/size can be tuned.
    public static final boolean DEBUG_GUI_TEXT_BOUNDS = false;

    // Draws the planned paths for monsters and NPCs when enabled.
    public static final boolean DEBUG_ENTITY_PATHS = false;

    // Number of tiles monsters and NPCs keep away from collision tiles while pathfinding.
    public static final int ENTITY_COLLISION_CLEARANCE_TILES = 1;

    // Monster patrol/combat tuning.
    public static final float MONSTER_PATROL_RADIUS = 6f * GRID_W;
    public static final float MONSTER_SPEED = 45f;
    public static final float MONSTER_PATROL_PAUSE_MIN = 0.005f;
    public static final float MONSTER_PATROL_PAUSE_MAX = 0.05f;
    public static final float MONSTER_AGGRO_RANGE = 8f * GRID_W;
    public static final float MONSTER_AGGRO_LEASH_RANGE = 30f * GRID_W;
    // A monster hit by the player retaliates farther than its normal detection range.
    public static final float MONSTER_RETALIATION_LEASH_RANGE = 60f * GRID_W;
    public static final float MONSTER_ATTACK_RANGE = 2f * GRID_W;
    public static final float MONSTER_ATTACK_COOLDOWN = 1.5f;

    // NPC patrol/interaction tuning.
    public static final float NPC_PATROL_RADIUS = 15f * GRID_W;
    public static final float NPC_SPEED = 45f;
    public static final float NPC_PATROL_PAUSE_MIN = 1f;
    public static final float NPC_PATROL_PAUSE_MAX = 3f;
    /**
     * Squared conversation reach, in tiles.
     *
     * <p>{@code Character::StartAsyncDirectTalk} guards the whole talk path with
     * {@code if (Dist < 120)}, where {@code Dist} is the squared tile distance to the target. The
     * comparison is isotropic and made in tiles, so it is reproduced as-is rather than turned into
     * a world-unit radius: tiles are twice as wide as they are tall, and a world-unit radius would
     * reach twice as far vertically as horizontally.
     */
    public static final float NPC_INTERACTION_RANGE_TILES_SQUARED = 120f;

    // NPC hostile retaliation tuning (when a non-fleeing NPC is attacked in combat mode).
    public static final float NPC_HOSTILE_LEASH_RANGE = 20f * GRID_W;
    public static final int NPC_HOSTILE_DAMAGE_MIN = 2;
    public static final int NPC_HOSTILE_DAMAGE_MAX = 6;

    public static final float COMPANION_FOLLOW_START_DISTANCE = 2.5f * GRID_W;
    public static final float COMPANION_FOLLOW_STOP_DISTANCE = 1.2f * GRID_W;
    public static final float COMPANION_TRAIL_SAMPLE_INTERVAL = 0.3f;
    public static final int COMPANION_TRAIL_SAMPLES = 4;
    public static final float COMPANION_COMBAT_LEASH_RANGE = 15f * GRID_W;
    public static final float TAME_CHANNEL_SECONDS = 5f;
    public static final float HERB_HARVEST_SECONDS = 5f;
    public static final int HERB_INTERACTION_DISTANCE_TILES = 2;
    public static final float TAME_MAX_RANGE_TILES = 8f;
    public static final float TAME_MOVE_TOLERANCE = 4f;
    public static final long DEFAULT_CAST_MENTAL_EXHAUSTION_MS = 1000L;
    public static final long DEFAULT_CAST_PHYSICAL_EXHAUSTION_MS = 750L;
    public static final float COMPANION_AGGRESSIVE_DETECTION_RANGE = 8f * GRID_W;
    public static final float COMPANION_AGGRESSIVE_SCAN_INTERVAL = 0.5f;

    // Default level-one position from Server 1.68 Character.cpp: wlStartPos.
    public static final int NEW_CHARACTER_START_TILE_X = 2944;
    public static final int NEW_CHARACTER_START_TILE_Y = 1059;
    public static final int NEW_CHARACTER_START_TILE_Z = 0;

    // Player respawn tile coordinates.
    public static final int PLAYER_RESPAWN_TILE_X = 2951;
    public static final int PLAYER_RESPAWN_TILE_Y = 1038;
    public static final int PLAYER_RESPAWN_TILE_Z = 0;

    // Max tile distance allowed for object interactions.
    public static final int OBJECT_MAX_INTERACTION_DISTANCE = 8;

    // Probability (0.0–1.0) that an attack spell triggers a critical hit. 1.0 = 100% for testing.
    public static final float SPELL_CRIT_CHANCE = 1.0f;

    // Maximum range (world pixels) at which a bow weapon can hit a monster.
    public static final float BOW_ATTACK_RANGE = 15f * GRID_W;

    // Base sprite name for the bow arrow projectile. Directional frames exist for
    // angles 000/045/090/135/180 (e.g. "64kArrowNormal090-a"); 225/270/315 are
    // covered by horizontal flip (see computeFlipFallbackDirection).
    public static final String BOW_PROJECTILE_SPRITE = "64kArrowNormal";

    // Item structure id identifying a quiver in items.bin. Firing a bow requires
    // one equipped in the off-hand, as in GoN's Character::RangedAttack().
    public static final int QUIVER_STRUCTURE_ID = 8;

    // Rebirth ("remort") ritual tuning, read by NpcScriptEngine.
    /** Maximum number of rebirths, exposed to scripts as {@code ACK_MAXREMORTS}. */
    public static final long REBIRTH_MAX_REMORTS = 10L;
    /** Energy granted by each rebirth, matching the original server's allowance. */
    public static final int REBIRTH_REMORT_POINTS_PER_REBIRTH = 10;
    /**
     * Attribute floor a reborn character starts from. Betran (RemortNPC2) prices his upgrades
     * against {@code USER_TRUE_STR - (20 + remorts * 5)}, which pins the base to these two numbers.
     */
    public static final int REBIRTH_BASE_ATTRIBUTE = 20;
    public static final int REBIRTH_ATTRIBUTE_PER_REMORT = 5;
    // Elemental resistances and powers both sit at 100 for a fresh character.
    public static final int REBIRTH_ELEMENT_BASE = 100;

    /**
     * Where the rebirth ritual sends the player back into the world, at the end of Alphan's final
     * stage. Not to be confused with {@code REMORT_TO(1315, 920, 1)}, which is the ritual room
     * hosting Alphan and his associates.
     */
    public static final int REBIRTH_RETURN_TILE_X = 2939;
    public static final int REBIRTH_RETURN_TILE_Y = 1066;
    public static final int REBIRTH_RETURN_Z = 0;
}
