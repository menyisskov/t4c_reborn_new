package com.perso.T4C.session;

import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single-player world driver: the entity registry creation that used to live in {@code
 * MainGameScreen}, moved unchanged.
 */
public final class LocalWorldDriver implements WorldDriver {
  private static final Logger log = LoggerFactory.getLogger(LocalWorldDriver.class);

  @Override
  public WorldEntities loadEntities(WorldEntityInputs inputs) {
    // The summon callback reads the NPC registry of the screen as it is at summon time, which is
    // the one created here once the screen has stored it; the holder stands in for that field.
    NPCManager[] npcHolder = new NPCManager[1];
    MonsterManager monsterManager = createMonsterManager(inputs, npcHolder);
    NPCManager npcManager = createNpcManager(inputs);
    npcHolder[0] = npcManager;
    npcManager.triggerPopupEvents(inputs.player());
    return new WorldEntities(monsterManager, npcManager);
  }

  @Override
  public void tickEntities(EntityTick tick) {
    if (tick.npcs() != null) {
      tick.npcs()
          .updateVisible(
              tick.delta(),
              tick.playerPosition(),
              tick.startX(),
              tick.endX(),
              tick.startY(),
              tick.endY(),
              tick.margin());
    }
    if (tick.monsters() != null) {
      tick.monsters().setScriptPlayer(tick.player());
      // GM peace mode: monsters get no player to chase, so they drop any fight with them.
      tick.monsters()
          .updateVisible(
              tick.delta(),
              tick.player().isGmPeace() ? null : tick.playerPosition(),
              tick.startX(),
              tick.endX(),
              tick.startY(),
              tick.endY(),
              tick.margin());
    }
  }

  private NPCManager createNpcManager(WorldEntityInputs inputs) {
    NPCManager manager = new NPCManager(inputs.outlineShader(), inputs.questService());
    try {
      MapDefinition currentMap = inputs.currentMap().get();
      String mapPath = currentMap != null ? currentMap.getMapPath() : Paths.MAP;
      manager.initializeNpcsFromMap(mapPath);
    } catch (GameException e) {
      log.error("Failed to initialize NPCs from map json", e);
    }
    return manager;
  }

  private MonsterManager createMonsterManager(WorldEntityInputs inputs, NPCManager[] npcHolder) {
    MonsterManager manager = new MonsterManager(inputs.outlineShader());
    NpcScriptRuntime.setSummonCallback(
        (name, x, y, z) -> {
          MapDefinition currentMap = inputs.currentMap().get();
          if (currentMap == null || currentMap.getZ() != z) return false;
          if (manager.spawnMonster(name, x, y, false)) return true;
          NPCManager npcManager = npcHolder[0];
          return npcManager != null && npcManager.spawnNPC(name, x, y);
        });
    manager.setXpCurve(inputs.xpCurve());
    try {
      MapDefinition currentMap = inputs.currentMap().get();
      String mapPath = currentMap != null ? currentMap.getMapPath() : Paths.MAP;
      manager.initializeMonstersFromMap(mapPath);
    } catch (GameException e) {
      log.error("Failed to initialize monsters from map json", e);
    }
    return manager;
  }
}
