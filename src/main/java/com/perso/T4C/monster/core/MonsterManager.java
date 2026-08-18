package com.perso.T4C.monster.core;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.entity.NameableEntityHandler;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.SpawnRegistry;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

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
  private java.util.function.Consumer<com.perso.T4C.npc.script.MonsterScriptBridge.Effects>
      scriptEffectsCallback;
  private Player scriptPlayer;
  private final Set<BaseMonster> notifiedPlayerKills =
      Collections.newSetFromMap(new IdentityHashMap<>());
  private final Set<BaseMonster> notifiedSpawns =
      Collections.newSetFromMap(new IdentityHashMap<>());
  private java.util.function.BiConsumer<com.perso.T4C.combat.CombatResult, Vector2>
      attackMissedCallback;
  private java.util.function.BiConsumer<BaseMonster, com.perso.T4C.combat.CombatResult>
      playerAttackHitCallback;
  private java.util.function.Consumer<BaseMonster> companionAttackNotifyCallback;
  private java.util.function.IntConsumer companionDamageCallback;

  public MonsterManager(ShaderProgram outlineShader) {
    this.outlineShader = outlineShader;
  }

  public boolean despawnMonster(BaseMonster monster) {
    if (monster == null) return false;
    notifiedPlayerKills.remove(monster);
    return monsters.remove(monster);
  }

  public void setDamageDealtCallback(java.util.function.BiConsumer<Integer, Vector2> callback) {
    this.damageDealtCallback = callback;
  }

  public void setAttackMissedCallback(
      java.util.function.BiConsumer<com.perso.T4C.combat.CombatResult, Vector2> callback) {
    this.attackMissedCallback = callback;
  }

  public void setLootCallback(java.util.function.Consumer<BaseMonster> callback) {
    this.lootCallback = callback;
  }

  public void setPlayerKillCallback(java.util.function.Consumer<BaseMonster> callback) {
    this.playerKillCallback = callback;
  }

  public void setCompanionAttackNotifyCallback(java.util.function.Consumer<BaseMonster> callback) {
    this.companionAttackNotifyCallback = callback;
  }

  public void setPlayerAttackHitCallback(
      java.util.function.BiConsumer<BaseMonster, com.perso.T4C.combat.CombatResult> callback) {
    this.playerAttackHitCallback = callback;
  }

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
    for (BaseMonster monster : monsters) {
      monster.setDamageCallback(callback);
    }
  }

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

  public void setScriptEffectsCallback(
      java.util.function.Consumer<com.perso.T4C.npc.script.MonsterScriptBridge.Effects> callback) {
    scriptEffectsCallback = callback;
    for (BaseMonster monster : monsters) monster.setScriptEffectsCallback(callback);
  }

  public void addMonster(BaseMonster monster) {
    monsters.add(monster);
    monster.setScriptPlayer(scriptPlayer);
    if (playerDamageCallback != null) {
      monster.setDamageCallback(playerDamageCallback);
    }
    if (companionDamageCallback != null) {
      monster.setCompanionDamageCallback(companionDamageCallback);
    }
    if (deathCallback != null) monster.setDeathCallback(deathCallback);
    if (scriptEffectsCallback != null) monster.setScriptEffectsCallback(scriptEffectsCallback);
    emitSpawn(monster);
    log.debug(
        "Added Monster: {} at position ({}, {})",
        monster.getName(),
        monster.getTileX(),
        monster.getTileY());
  }

  public void update(float delta, Vector2 playerPosition) {
    spawnAllPending();
    for (BaseMonster monster : monsters) {
      monster.update(delta, playerPosition, monsters);
      if (monster.shouldRespawn() && canRespawn(monster)) {
        monster.respawn();
        notifiedPlayerKills.remove(monster);
        scheduleNextRespawn(monster);
      }
    }
    monsters.removeIf(BaseMonster::shouldRemoveAfterDeath);
  }

  public void setXpCurve(com.perso.T4C.helper.XpCurve xpCurve) {
    this.xpCurve = xpCurve;
  }

  public void updateVisible(
      float delta, Vector2 playerPosition, int startX, int endX, int startY, int endY, int margin) {
    spawnPendingInView(startX, endX, startY, endY, margin);
    for (BaseMonster monster : monsters) {
      int tileX = monster.getTileX();
      int tileY = monster.getTileY();
      if (tileX < startX - margin
          || tileX > endX + margin
          || tileY < startY - margin
          || tileY > endY + margin) {
        continue;
      }
      monster.update(delta, playerPosition, monsters);
      if (monster.shouldRespawn() && canRespawn(monster)) {
        monster.respawn();
        notifiedPlayerKills.remove(monster);
        scheduleNextRespawn(monster);
      }
    }
    monsters.removeIf(BaseMonster::shouldRemoveAfterDeath);
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
    monster.setRespawnDelayMillis(
        ThreadLocalRandom.current().nextLong(RESPAWN_MIN_MILLIS, RESPAWN_MAX_MILLIS + 1));
  }

  private void spawnPendingInView(int startX, int endX, int startY, int endY, int margin) {
    if (pendingSpawns.isEmpty()) {
      return;
    }
    java.util.Iterator<PendingSpawn> it = pendingSpawns.iterator();
    while (it.hasNext()) {
      PendingSpawn spawn = it.next();
      if (spawn.tileX < startX - margin
          || spawn.tileX > endX + margin
          || spawn.tileY < startY - margin
          || spawn.tileY > endY + margin) {
        continue;
      }
      it.remove();
      instantiateSpawn(spawn);
    }
  }

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
      BaseMonster monster =
          MonsterRegistry.create(spawn.def, spawn.tileX * GRID_W, spawn.tileY * GRID_H);
      monster.setStationary(spawn.stationary);
      if (spawn.aggressive != null) {
        monster.setAggressive(spawn.aggressive);
      }
      addMonster(monster);
    } catch (Exception ex) {
      log.warn(
          "Failed to spawn monster type={} at ({}, {})",
          spawn.def.getName(),
          spawn.tileX,
          spawn.tileY,
          ex);
    }
  }

  public boolean spawnMonster(String name, float worldX, float worldY) {
    return spawnMonster(name, worldX, worldY, true);
  }

  public void setScriptPlayer(com.perso.T4C.player.Player player) {
    this.scriptPlayer = player;
    for (BaseMonster monster : monsters) {
      monster.setScriptPlayer(player);
      emitSpawn(monster);
    }
  }

  private void emitSpawn(BaseMonster monster) {
    if (scriptPlayer == null || !notifiedSpawns.add(monster)) return;
    if (monster instanceof MonsterLifecycle lifecycle) emit(lifecycle.onSpawn(scriptPlayer));
    if (monster instanceof DataMonster data) emit(MonsterScriptBridge.spawn(data, scriptPlayer));
  }

  private void emit(BaseMonster monster, String event, Player player) {
    if (player == null) return;
    if (monster instanceof MonsterLifecycle lifecycle) {
      MonsterScriptBridge.Effects effects =
          switch (event) {
            case "OnAttacked" -> lifecycle.onAttacked(player);
            case "OnHit" -> lifecycle.onHit(player);
            case "OnAttackHit" -> lifecycle.onAttackHit(player);
            default -> MonsterScriptBridge.Effects.empty();
          };
      emit(effects);
    } else if (monster instanceof DataMonster data) {
      emit(
          switch (event) {
            case "OnAttacked" -> MonsterScriptBridge.attacked(data, player);
            case "OnHit" -> MonsterScriptBridge.hit(data, player);
            case "OnAttackHit" -> MonsterScriptBridge.attackHit(data, player);
            default -> MonsterScriptBridge.Effects.empty();
          });
    }
  }

  private void emit(MonsterScriptBridge.Effects effects) {
    if (scriptEffectsCallback != null && effects != null) scriptEffectsCallback.accept(effects);
  }

  public void notifyPlayerDamagedMonster(BaseMonster monster, Player player) {
    emit(monster, "OnAttacked", player);
    emit(monster, "OnHit", player);
  }

  public void notifyMonsterAttackHit(BaseMonster monster, Player player) {
    emit(monster, "OnAttackHit", player);
  }

  public boolean spawnMonster(String name, float worldX, float worldY, boolean respawn) {
    MonsterDef def = MonsterRegistry.findByName(name);
    if (def == null) {
      return false;
    }
    try {
      BaseMonster monster = MonsterRegistry.create(def, worldX, worldY);
      if (!respawn) monster.disableRespawn();
      addMonster(monster);
      return true;
    } catch (Exception ex) {
      log.warn("Failed to summon monster type={} at ({}, {})", name, worldX, worldY, ex);
      return false;
    }
  }

  public void render(SpriteBatch batch) {
    for (BaseMonster monster : monsters) {
      monster.render(batch, outlineShader);
    }
  }

  public void renderNameOverlay(SpriteBatch batch) {
    for (BaseMonster monster : monsters) {
      monster.renderNameOverlay(batch);
    }
  }

  public void onMouseMove(float mouseX, float mouseY) {
    for (BaseMonster monster : monsters) {
      boolean wasHovered = monster.isHovered();
      boolean isNowHovered = monster.isMouseOver(mouseX, mouseY);
      monster.setHovered(isNowHovered);
      if (!wasHovered && isNowHovered) {
        log.debug(
            "Mouse entered Monster: {} at position ({}, {}) - Mouse at ({}, {})",
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

  public boolean onClick(float mouseX, float mouseY) {
    for (BaseMonster monster : monsters) {
      if (monster.isMouseOver(mouseX, mouseY) && !monster.isDead()) {
        log.info("Player attempted to attack {}", monster.getName());
        return true;
      }
    }
    return false;
  }

  public boolean attackMonster(BaseMonster monster, Player player) {
    if (monster == null || monster.isDead() || player == null || !monster.canBeAttackedByPlayer()) {
      return false;
    }
    Vector2 playerPos = player.getPositionVector();
    float distance = playerPos.dst(monster.getPosition());
    float maxMeleeRange = 2f * Math.max(GRID_W, GRID_H);
    if (distance > maxMeleeRange) {
      log.info(
          "Player is too far to attack {} (distance: {} pixels, max: {})",
          monster.getName(),
          String.format("%.1f", distance),
          String.format("%.1f", maxMeleeRange));
      return false;
    }
    if (!com.perso.T4C.combat.CombatGeometry.hasLineOfSight(playerPos, monster.getPosition())) {
      log.info("No line of sight to attack {}", monster.getName());
      return false;
    }
    player
        .getMovement()
        .faceToward(playerPos.x, playerPos.y, monster.getPosition().x, monster.getPosition().y);
    player.attack(player.getMovement());
    int rawDamage = com.perso.T4C.helper.CombatMath.computeMeleeDamage(player);
    int offHandDamage = com.perso.T4C.helper.CombatMath.computeOffHandDamage(player);
    com.perso.T4C.combat.CombatResult result =
        com.perso.T4C.combat.CombatResolver.resolve(
            new com.perso.T4C.combat.PhysicalAttackRequest(
                com.perso.T4C.combat.CombatProfiles.fromPlayer(player),
                com.perso.T4C.combat.CombatProfiles.fromMonster(monster),
                rawDamage,
                offHandDamage,
                false),
            java.util.concurrent.ThreadLocalRandom.current());
    player.setHidden(false);
    monster.aggroOn(player.getPositionVector());
    if (companionAttackNotifyCallback != null) {
      companionAttackNotifyCallback.accept(monster);
    }
    int damage = result.damage();
    boolean wasDead = monster.isDead();
    int appliedPrimaryDamage = 0;
    if (result.hit()) {
      com.perso.T4C.item.ItemDurabilityService.damageEquipped(
          player,
          com.perso.T4C.player.BodyPart.WEAPON,
          com.perso.T4C.item.ItemDurabilityService.COMBAT_WEAR);
      if (offHandDamage > 0)
        com.perso.T4C.item.ItemDurabilityService.damageEquipped(
            player,
            com.perso.T4C.player.BodyPart.WEAPON2,
            com.perso.T4C.item.ItemDurabilityService.COMBAT_WEAR);
      if (playerAttackHitCallback != null) {
        playerAttackHitCallback.accept(monster, result);
      }
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
    log.info(
        "Player attacked {}: hit={}, precision={}, damage={}, parried={} (distance: {} pixels)",
        monster.getName(),
        result.hit(),
        result.precision(),
        appliedPrimaryDamage,
        result.parried(),
        String.format("%.1f", distance));
    return true;
  }

  public boolean onRightClick(float mouseX, float mouseY) {
    return NameableEntityHandler.handleRightClick(
        mouseX, mouseY, monsters, monster -> !monster.isDead());
  }

  public void onResourcesReloaded() {
    for (BaseMonster monster : monsters) {
      monster.onResourcesReloaded();
    }
  }

  public int getMonsterCount() {
    return monsters.size();
  }

  public List<BaseMonster> getMonsters() {
    return readonlyMonsters;
  }

  public void clear() {
    monsters.clear();
    pendingSpawns.clear();
    notifiedSpawns.clear();
    notifiedPlayerKills.clear();
    log.info("Cleared all monsters");
  }

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
          if ("SUNDIAL".equalsIgnoreCase(entry.type)) {
            continue;
          }
          log.warn(
              "No monster definition for spawn type={} at ({}, {}, {})",
              entry.type,
              entry.x,
              entry.y,
              entry.z);
          continue;
        }
        pendingSpawns.add(
            new PendingSpawn(def, entry.x, entry.y, entry.stationary, entry.aggressive));
        queued++;
      }
      log.info("Queued {} monster spawn(s) from {}", queued, source.path());
    } catch (Exception e) {
      throw new GameException("Failed to load monster spawns for " + mapPath, e);
    }
  }

  private SpawnSource readSpawnsForMap(String mapPath) throws Exception {
    String source = "java:SpawnRegistry";
    int z = resolveMapZ(mapPath);
    List<MonsterSpawnEntry> filtered = new ArrayList<>();
    for (MonsterSpawnEntry entry : readJavaSpawns()) {
      if (entry.z == z) {
        filtered.add(entry);
      }
    }
    return new SpawnSource(source, filtered);
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

  private List<MonsterSpawnEntry> readJavaSpawns() {
    List<com.perso.T4C.spawn.SpawnDefinition> definitions = SpawnRegistry.monsters();
    List<MonsterSpawnEntry> entries = new ArrayList<>(definitions.size());
    for (com.perso.T4C.spawn.SpawnDefinition definition : definitions) {
      MonsterSpawnEntry entry = new MonsterSpawnEntry();
      entry.type = definition.type();
      entry.x = definition.x();
      entry.y = definition.y();
      entry.z = definition.z();
      entry.stationary = definition.stationary();
      entry.aggressive = definition.aggressive();
      entries.add(entry);
    }
    return entries;
  }

  private static final class MonsterSpawnEntry {
    String type;
    int x;
    int y;
    int z;
    boolean stationary;
    Boolean aggressive;
  }

  private record SpawnSource(String path, List<MonsterSpawnEntry> entries) {}

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
