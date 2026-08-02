package com.perso.T4C.monster;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.config.Paths;
import com.perso.T4C.entity.NameableEntityHandler;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.player.Player;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/**
 * Manages all monsters in the game world.
 */
@Slf4j
public class MonsterManager {
    private static final int LOCAL_TYPE_CAP = 4;
    private static final int DENSITY_RADIUS_TILES = 12;
    private static final long RESPAWN_MIN_MILLIS = 18_000L;
    private static final long RESPAWN_MAX_MILLIS = 30_000L;

    private final List<BaseMonster> monsters = new ArrayList<>();
    private final List<BaseMonster> readonlyMonsters = Collections.unmodifiableList(monsters);
    private final List<PendingSpawn> pendingSpawns = new ArrayList<>();
    private final ShaderProgram outlineShader;
    private DamageCallback playerDamageCallback;
    private java.util.function.Consumer<BaseMonster> deathCallback;
    private com.perso.T4C.helper.XpCurve xpCurve;
    private java.util.function.BiConsumer<Integer, Vector2> damageDealtCallback;
    private java.util.function.Consumer<BaseMonster> lootCallback;
    private java.util.function.Consumer<BaseMonster> playerKillCallback;
    private final Set<BaseMonster> notifiedPlayerKills =
            Collections.newSetFromMap(new IdentityHashMap<>());
    /** Notified when a player attack fails to land, so the UI can float a miss/dodge label. */
    private java.util.function.BiConsumer<com.perso.T4C.combat.CombatResult, Vector2> attackMissedCallback;
    private java.util.function.BiConsumer<BaseMonster, com.perso.T4C.combat.CombatResult> playerAttackHitCallback;
    /** Notified on every player attack attempt so an ally companion can join the fight. */
    private java.util.function.Consumer<BaseMonster> companionAttackNotifyCallback;
    private java.util.function.IntConsumer companionDamageCallback;

    /**
     * Create Monster manager.
     */
    public MonsterManager(ShaderProgram outlineShader) {
        this.outlineShader = outlineShader;
    }

    /** Permanently removes a monster without death rewards or respawn. */
    public boolean despawnMonster(BaseMonster monster) {
        if (monster == null) return false;
        notifiedPlayerKills.remove(monster);
        return monsters.remove(monster);
    }

    /**
     * Set the callback for applying damage to the player.
     */
    public void setDamageDealtCallback(java.util.function.BiConsumer<Integer, Vector2> callback) {
        this.damageDealtCallback = callback;
    }

    /**
     * Set the callback invoked when a player attack misses or is parried.
     */
    public void setAttackMissedCallback(java.util.function.BiConsumer<com.perso.T4C.combat.CombatResult, Vector2> callback) {
        this.attackMissedCallback = callback;
    }

    /**
     * Set the callback invoked when a monster is killed by the player, used to spawn loot.
     */
    public void setLootCallback(java.util.function.Consumer<BaseMonster> callback) {
        this.lootCallback = callback;
    }

    /** Set the callback used by progression systems for player-attributed kills. */
    public void setPlayerKillCallback(java.util.function.Consumer<BaseMonster> callback) {
        this.playerKillCallback = callback;
    }

    /** Set the callback letting the player's companion target what the player attacks. */
    public void setCompanionAttackNotifyCallback(java.util.function.Consumer<BaseMonster> callback) {
        this.companionAttackNotifyCallback = callback;
    }

    /** Invoked before primary damage when the player's physical attack successfully hits. */
    public void setPlayerAttackHitCallback(
            java.util.function.BiConsumer<BaseMonster, com.perso.T4C.combat.CombatResult> callback) {
        this.playerAttackHitCallback = callback;
    }

    /**
     * Publishes one player-attributed death to loot and progression systems.
     * Duplicate notifications for the same death are ignored until respawn.
     * Exposed so every kill path (melee, bow, spell and area effect) converges
     * here.
     */
    public void notifyKilledByPlayer(BaseMonster monster) {
        if (monster == null || !monster.isDead() || !notifiedPlayerKills.add(monster)) {
            return;
        }
        if (lootCallback != null) {
            lootCallback.accept(monster);
        }
        if (playerKillCallback != null) {
            playerKillCallback.accept(monster);
        }
    }

    public void setPlayerDamageCallback(DamageCallback callback) {
        this.playerDamageCallback = callback;
        // Update all existing monsters
        for (BaseMonster monster : monsters) {
            monster.setDamageCallback(callback);
        }
    }

    /** Wires the callback applying monster blows landed on the ally companion. */
    public void setCompanionDamageCallback(java.util.function.IntConsumer callback) {
        this.companionDamageCallback = callback;
        for (BaseMonster monster : monsters) {
            monster.setCompanionDamageCallback(callback);
        }
    }

    public void setDeathCallback(java.util.function.Consumer<BaseMonster> callback) {
        deathCallback = callback;
        for (BaseMonster monster : monsters) monster.setDeathCallback(callback);
    }

    /**
     * Add a monster to the manager.
     */
    public void addMonster(BaseMonster monster) {
        monsters.add(monster);
        // Set damage callback if available
        if (playerDamageCallback != null) {
            monster.setDamageCallback(playerDamageCallback);
        }
        if (companionDamageCallback != null) {
            monster.setCompanionDamageCallback(companionDamageCallback);
        }
        if (deathCallback != null) monster.setDeathCallback(deathCallback);
        log.debug("Added Monster: {} at position ({}, {})",
                 monster.getName(), monster.getTileX(), monster.getTileY());
    }

    /**
     * Update all monsters.
     */
    public void update(float delta, Vector2 playerPosition) {
        spawnAllPending();
        for (BaseMonster monster : monsters) {
            monster.update(delta, playerPosition, monsters);

            // Respawn monster if respawn time has passed
            if (monster.shouldRespawn() && canRespawn(monster)) {
                monster.respawn();
                notifiedPlayerKills.remove(monster);
                scheduleNextRespawn(monster);
            }
        }
    }

    public void setXpCurve(com.perso.T4C.helper.XpCurve xpCurve) {
        this.xpCurve = xpCurve;
    }

    public void updateVisible(float delta, Vector2 playerPosition, int startX, int endX, int startY, int endY, int margin) {
        spawnPendingInView(startX, endX, startY, endY, margin);
        for (BaseMonster monster : monsters) {
            int tileX = monster.getTileX();
            int tileY = monster.getTileY();
            if (tileX < startX - margin || tileX > endX + margin || tileY < startY - margin || tileY > endY + margin) {
                continue;
            }
            monster.update(delta, playerPosition, monsters);
            if (monster.shouldRespawn() && canRespawn(monster)) {
                monster.respawn();
                notifiedPlayerKills.remove(monster);
                scheduleNextRespawn(monster);
            }
        }
    }

    private boolean canRespawn(BaseMonster monster) {
        int nearby = 0;
        int x = monster.getTileX(), y = monster.getTileY();
        for (BaseMonster candidate : monsters) {
            if (candidate.isDead || !monster.getName().equalsIgnoreCase(candidate.getName())) continue;
            if (Math.max(Math.abs(candidate.getTileX() - x), Math.abs(candidate.getTileY() - y))
                    <= DENSITY_RADIUS_TILES) nearby++;
        }
        return nearby < LOCAL_TYPE_CAP;
    }

    private void scheduleNextRespawn(BaseMonster monster) {
        monster.setRespawnDelayMillis(ThreadLocalRandom.current().nextLong(
                RESPAWN_MIN_MILLIS, RESPAWN_MAX_MILLIS + 1));
    }

    /**
     * Instantiate pending spawns whose tile lies inside the viewport (plus margin).
     * Monsters outside the viewport are never created, so they cost nothing per frame.
     */
    private void spawnPendingInView(int startX, int endX, int startY, int endY, int margin) {
        if (pendingSpawns.isEmpty()) {
            return;
        }
        java.util.Iterator<PendingSpawn> it = pendingSpawns.iterator();
        while (it.hasNext()) {
            PendingSpawn spawn = it.next();
            if (spawn.tileX < startX - margin || spawn.tileX > endX + margin
                    || spawn.tileY < startY - margin || spawn.tileY > endY + margin) {
                continue;
            }
            it.remove();
            instantiateSpawn(spawn);
        }
    }

    /**
     * Instantiate every pending spawn regardless of viewport (used by the unculled update path).
     */
    private void spawnAllPending() {
        if (pendingSpawns.isEmpty()) {
            return;
        }
        List<PendingSpawn> toSpawn = new ArrayList<>(pendingSpawns);
        pendingSpawns.clear();
        for (PendingSpawn spawn : toSpawn) {
            instantiateSpawn(spawn);
        }
    }

    private void instantiateSpawn(PendingSpawn spawn) {
        try {
            BaseMonster monster = new DataMonster(spawn.def, spawn.tileX * GRID_W, spawn.tileY * GRID_H);
            monster.setStationary(spawn.stationary);
            if (spawn.aggressive != null) {
                monster.setAggressive(spawn.aggressive);
            }
            addMonster(monster);
        } catch (Exception ex) {
            log.warn("Failed to spawn monster type={} at ({}, {})", spawn.def.getName(), spawn.tileX, spawn.tileY, ex);
        }
    }

    public boolean spawnMonster(String name, float worldX, float worldY) {
        MonsterDef def = MonsterRegistry.findByName(name);
        if (def == null) {
            return false;
        }
        try {
            addMonster(new DataMonster(def, worldX, worldY));
            return true;
        } catch (Exception ex) {
            log.warn("Failed to summon monster type={} at ({}, {})", name, worldX, worldY, ex);
            return false;
        }
    }

    /**
     * Render all monsters.
     */
    public void render(SpriteBatch batch) {
        for (BaseMonster monster : monsters) {
            monster.render(batch, outlineShader);
        }
    }

    /**
     * Handle mouse movement for hover detection.
     */
    public void onMouseMove(float mouseX, float mouseY) {
        for (BaseMonster monster : monsters) {
            boolean wasHovered = monster.isHovered();
            boolean isNowHovered = monster.isMouseOver(mouseX, mouseY);
            monster.setHovered(isNowHovered);

            if (!wasHovered && isNowHovered) {
                log.debug("Mouse entered Monster: {} at position ({}, {}) - Mouse at ({}, {})",
                    monster.getName(),
                    String.format("%.0f", monster.getPosition().x),
                    String.format("%.0f", monster.getPosition().y),
                    String.format("%.0f", mouseX),
                    String.format("%.0f", mouseY));
            } else if (wasHovered && !isNowHovered) {
                log.debug("Mouse left Monster: {}", monster.getName());
            }
        }
    }

    /**
     * Handle click on monsters (attack).
     * Player must be within 1 tile range for melee attacks.
     */
    public boolean onClick(float mouseX, float mouseY) {
        for (BaseMonster monster : monsters) {
            if (monster.isMouseOver(mouseX, mouseY) && !monster.isDead()) {
                // Attack attempt - damage will be applied by callback if in range
                log.info("Player attempted to attack {}", monster.getName());
                return true;
            }
        }
        return false;
    }

    /**
     * Handle player attack on a specific monster.
     * Checks if player is within melee range (1 tile) before applying damage.
     *
     * @param monster The monster being attacked
     * @param player The player performing the attack
     * @return true if attack was successful (in range), false otherwise
     */
    public boolean attackMonster(BaseMonster monster, Player player) {
        if (monster == null || monster.isDead() || player == null
                || !monster.canBeAttackedByPlayer()) {
            return false;
        }

        Vector2 playerPos = player.getPositionVector();
        // Calculate distance between player and monster
        float distance = playerPos.dst(monster.getPosition());

        // Maximum melee attack range: 1 tile (using diagonal distance)
        // We use 2 tiles worth of pixels (tolerance) like before
        float maxMeleeRange = 2f * Math.max(GRID_W, GRID_H);

        if (distance > maxMeleeRange) {
            // Player is out of range
            log.info("Player is too far to attack {} (distance: {} pixels, max: {})",
                     monster.getName(), String.format("%.1f", distance), String.format("%.1f", maxMeleeRange));
            return false;
        }
        if (!com.perso.T4C.combat.CombatGeometry.hasLineOfSight(playerPos, monster.getPosition())) {
            log.info("No line of sight to attack {}", monster.getName());
            return false;
        }

        // Player is in range - resolve the complete physical attack.
        player.getMovement().faceToward(playerPos.x, playerPos.y, monster.getPosition().x, monster.getPosition().y);
        player.attack(player.getMovement());
        int rawDamage = com.perso.T4C.helper.CombatMath.computeMeleeDamage(player);
        int offHandDamage = com.perso.T4C.helper.CombatMath.computeOffHandDamage(player);
        com.perso.T4C.combat.CombatResult result = com.perso.T4C.combat.CombatResolver.resolve(
                new com.perso.T4C.combat.PhysicalAttackRequest(
                        com.perso.T4C.combat.CombatProfiles.fromPlayer(player),
                        com.perso.T4C.combat.CombatProfiles.fromMonster(monster), rawDamage, offHandDamage, false),
                java.util.concurrent.ThreadLocalRandom.current());
        player.setHidden(false);
        // Any attack attempt provokes retaliation, hit or miss (matches the original
        // client, where an attacked creature always fights back).
        monster.aggroOn(player.getPositionVector());
        // The companion joins whatever fight the player picks, hit or miss.
        if (companionAttackNotifyCallback != null) {
            companionAttackNotifyCallback.accept(monster);
        }
        int damage = result.damage();
        boolean wasDead = monster.isDead();
        int appliedPrimaryDamage = 0;
        if (result.hit()) {
            if (playerAttackHitCallback != null) {
                playerAttackHitCallback.accept(monster, result);
            }
            // An OnAttackHit effect (such as the seraph nova) may have killed
            // the primary target before the weapon blow is finally applied.
            if (!monster.isDead()) {
                if (monster.isStunned()) monster.clearStun();
                monster.stunFor(result.stunDurationMillis());
                if (damage > 0) {
                    monster.applyPlayerDamage(damage, player, xpCurve);
                    appliedPrimaryDamage = damage;
                }
            }
        }
        if (!wasDead && monster.isDead()) {
            // The callback owns loot for a kill caused by its effect. Avoid a
            // duplicate drop when the weapon blow was never applied.
            if (appliedPrimaryDamage > 0) {
                notifyKilledByPlayer(monster);
            }
        }
        if (damageDealtCallback != null && appliedPrimaryDamage > 0) {
            damageDealtCallback.accept(appliedPrimaryDamage, monster.getPosition());
        }
        if (!result.hit() && attackMissedCallback != null) {
            attackMissedCallback.accept(result, monster.getPosition());
        }
        log.info("Player attacked {}: hit={}, precision={}, damage={}, parried={} (distance: {} pixels)",
                 monster.getName(), result.hit(), result.precision(), appliedPrimaryDamage, result.parried(), String.format("%.1f", distance));
        return true;
    }

    /**
     * Handle right-click on monsters (show name above head).
     */
    public boolean onRightClick(float mouseX, float mouseY) {
        // Exclude dead monsters from name display
        return NameableEntityHandler.handleRightClick(mouseX, mouseY, monsters, monster -> !monster.isDead());
    }

    /**
     * Reload monster resources.
     */
    public void onResourcesReloaded() {
        for (BaseMonster monster : monsters) {
            monster.onResourcesReloaded();
        }
    }

    /**
     * Get number of monsters.
     */
    public int getMonsterCount() {
        return monsters.size();
    }

    /**
     * Get all monsters (for debugging/inspection).
     */
    public List<BaseMonster> getMonsters() {
        return readonlyMonsters;
    }

    /**
     * Clear all monsters.
     */
    public void clear() {
        monsters.clear();
        pendingSpawns.clear();
        notifiedPlayerKills.clear();
        log.info("Cleared all monsters");
    }

    /**
     * Load monsters from the global binary spawn file, falling back to map-side files, then instantiate types from
     * com.perso.T4C.monster.types.
     */
    public void initializeMonstersFromMap(String mapPath) throws GameException {
        if (mapPath == null || mapPath.isBlank()) {
            throw new GameException("Map path is null/empty for monster spawn loading");
        }
        clear();

        try {
            SpawnSource source = readSpawnsForMap(mapPath);
            if (source == null) {
                log.warn("Monster spawn file not found for map {}. No monsters loaded.", mapPath);
                return;
            }
            List<MonsterSpawnEntry> entries = source.entries();
            if (entries == null || entries.isEmpty()) {
                log.info("Monster spawn file is empty: {}", source.path());
                return;
            }

            int queued = 0;
            for (MonsterSpawnEntry entry : entries) {
                if (entry == null || entry.type == null || entry.type.isBlank()) {
                    continue;
                }
                MonsterDef def = MonsterRegistry.findByName(entry.type);
                if (def == null) {
                    // SUNDIAL is a static interactive object accidentally present in the legacy monster group export.
                    if ("SUNDIAL".equalsIgnoreCase(entry.type)) {
                        continue;
                    }
                    log.warn("No monster definition for spawn type={} at ({}, {}, {})", entry.type, entry.x, entry.y, entry.z);
                    continue;
                }
                // Lazy spawning: the monster is only instantiated once its tile enters the
                // player's viewport (see spawnPendingInView), to keep load time and per-frame
                // cost independent of the total number of spawns on the map.
                pendingSpawns.add(new PendingSpawn(def, entry.x, entry.y, entry.stationary, entry.aggressive));
                queued++;
            }
            log.info("Queued {} monster spawn(s) from {}", queued, source.path());
        } catch (Exception e) {
            throw new GameException("Failed to load monster spawns for " + mapPath, e);
        }
    }

    private SpawnSource readSpawnsForMap(String mapPath) throws Exception {
        File globalSpawnFile = new File(Paths.MONSTER_SPAWNS_BIN);
        if (!globalSpawnFile.exists()) {
            return null;
        }
        int z = resolveMapZ(mapPath);
        List<MonsterSpawnEntry> filtered = new ArrayList<>();
        for (MonsterSpawnEntry entry : readBinarySpawns(globalSpawnFile)) {
            if (entry.z == z) {
                filtered.add(entry);
            }
        }
        return new SpawnSource(globalSpawnFile.getPath(), filtered);
    }

    private int resolveMapZ(String mapPath) {
        String normalized = new File(mapPath).getPath().replace('\\', '/');
        for (MapDefinition map : MapDefinition.values()) {
            if (new File(map.getMapPath()).getPath().replace('\\', '/').equals(normalized)) {
                return map.getZ();
            }
        }
        String lower = normalized.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("/moontug/")) return 2;
        if (lower.contains("/ravensdust/")) return 4;
        return 0;
    }

    private List<MonsterSpawnEntry> readBinarySpawns(File spawnFile) throws Exception {
        List<SpawnBinaryIO.Entry> binaryEntries = SpawnBinaryIO.read(spawnFile);
        List<MonsterSpawnEntry> entries = new ArrayList<>(binaryEntries.size());
        for (SpawnBinaryIO.Entry binaryEntry : binaryEntries) {
            MonsterSpawnEntry entry = new MonsterSpawnEntry();
            entry.type = binaryEntry.type;
            entry.x = binaryEntry.x;
            entry.y = binaryEntry.y;
            entry.z = binaryEntry.z;
            entry.stationary = binaryEntry.stationary;
            entry.aggressive = binaryEntry.aggressive;
            entries.add(entry);
        }
        return entries;
    }

    /**
     * Class representing MonsterSpawnEntry.
     */
    private static final class MonsterSpawnEntry {
        String type;
        int x;
        int y;
        int z;
        boolean stationary;
        Boolean aggressive;
    }

    private record SpawnSource(String path, List<MonsterSpawnEntry> entries) {
    }

    /**
     * Spawn point waiting for the player's viewport to reach it before the monster is created.
     */
    private static final class PendingSpawn {
        final MonsterDef def;
        final int tileX;
        final int tileY;
        final boolean stationary;
        final Boolean aggressive;

        PendingSpawn(MonsterDef def, int tileX, int tileY, boolean stationary, Boolean aggressive) {
            this.def = def;
            this.tileX = tileX;
            this.tileY = tileY;
            this.stationary = stationary;
            this.aggressive = aggressive;
        }
    }
}
