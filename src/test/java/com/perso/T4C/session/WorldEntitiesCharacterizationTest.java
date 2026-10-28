package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Characterizes the single-player "create the monster and NPC registries for the current map"
 * block: what is queued (spawns are lazy, so nothing is alive yet), in which order, and how the
 * summon callback behaves. Written before the block moved behind {@code WorldDriver}; only the
 * {@link #load} helper changed when it moved.
 */
@ResourceLock("npc.summon")
class WorldEntitiesCharacterizationTest {
  private static MonsterManager monsters;
  private static NPCManager npcs;
  private static MapDefinition currentMap;

  @AfterEach
  void clearSummonCallback() {
    NpcScriptRuntime.setSummonCallback(null);
  }

  // --- the one helper that points at the code under test ---

  private static void load(MapDefinition map) {
    currentMap = map;
    WorldEntities entities =
        new LocalGameSession()
            .world()
            .loadEntities(
                new WorldEntityInputs(null, null, XpCurve.loadDefault(), () -> currentMap, null));
    monsters = entities.monsters();
    npcs = entities.npcs();
  }

  // --- behaviour ---

  @Test
  void spawnsAreQueuedLazilyAndNothingIsAliveAfterTheLoad() throws Exception {
    for (MapDefinition map : MapDefinition.values()) {
      load(map);
      assertTrue(monsters.getMonsters().isEmpty(), map + " monsters alive before any frame");
      assertTrue(npcs.getNPCs().isEmpty(), map + " npcs alive before any frame");
      assertEquals(map != MapDefinition.UNDERWORLD, pending(monsters).size() > 0, map + " queued monster spawns");
    }
  }

  @Test
  void queuedCountsPerMapArePinned() throws Exception {
    List<String> counts = new ArrayList<>();
    for (MapDefinition map : MapDefinition.values()) {
      load(map);
      counts.add(map + ":" + pending(monsters).size() + "/" + pending(npcs).size());
    }
    assertEquals(EXPECTED_COUNTS, counts);
  }

  @Test
  void queuedOrderIsTheRegistryOrder() throws Exception {
    load(MapDefinition.WORLDMAP);
    assertEquals(EXPECTED_FIRST_MONSTERS, firstMonsterNames(5));
    assertEquals(EXPECTED_FIRST_NPCS, firstNpcTypes(5));
  }

  @Test
  void loadingAgainBuildsFreshManagers() {
    load(MapDefinition.WORLDMAP);
    MonsterManager firstMonsters = monsters;
    NPCManager firstNpcs = npcs;
    load(MapDefinition.WORLDMAP);
    assertNotSame(firstMonsters, monsters);
    assertNotSame(firstNpcs, npcs);
  }

  @Test
  void summonOnAnotherFloorIsRefusedAndOnThisFloorSpawnsOneMonster() {
    load(MapDefinition.WORLDMAP);
    assertFalse(NpcScriptRuntime.summon("Skeleton", 10f, 10f, MapDefinition.DUNGEON.getZ()));
    assertTrue(monsters.getMonsters().isEmpty());
    assertTrue(NpcScriptRuntime.summon("Skeleton", 10f, 10f, MapDefinition.WORLDMAP.getZ()));
    assertEquals(1, monsters.getMonsters().size());
  }

  @Test
  void summonOfAnUnknownNameSpawnsNothing() {
    load(MapDefinition.WORLDMAP);
    assertFalse(
        NpcScriptRuntime.summon("No Such Creature", 10f, 10f, MapDefinition.WORLDMAP.getZ()));
    assertTrue(monsters.getMonsters().isEmpty());
  }

  @Test
  void summonFollowsTheCurrentMapOfTheScreen() {
    load(MapDefinition.WORLDMAP);
    currentMap = MapDefinition.DUNGEON;
    assertTrue(NpcScriptRuntime.summon("Skeleton", 10f, 10f, MapDefinition.DUNGEON.getZ()));
    assertFalse(NpcScriptRuntime.summon("Skeleton", 10f, 10f, MapDefinition.WORLDMAP.getZ()));
  }

  // --- reflection helpers for the private spawn queues ---

  private static List<?> pending(Object manager) throws Exception {
    Field f = manager.getClass().getDeclaredField("pendingSpawns");
    f.setAccessible(true);
    return (List<?>) f.get(manager);
  }

  private static List<String> firstMonsterNames(int n) throws Exception {
    List<String> names = new ArrayList<>();
    for (Object spawn : pending(monsters)) {
      if (names.size() == n) break;
      Field def = spawn.getClass().getDeclaredField("def");
      def.setAccessible(true);
      Object monsterDef = def.get(spawn);
      names.add((String) monsterDef.getClass().getMethod("getName").invoke(monsterDef));
    }
    return names;
  }

  private static List<String> firstNpcTypes(int n) throws Exception {
    List<String> types = new ArrayList<>();
    for (Object entry : pending(npcs)) {
      if (types.size() == n) break;
      Field type = entry.getClass().getDeclaredField("type");
      type.setAccessible(true);
      types.add((String) type.get(entry));
    }
    return types;
  }

  private static final List<String> EXPECTED_COUNTS =
      List.of("WORLDMAP:13229/1009", "DUNGEON:5456/148", "CAVERN:7794/1402", "UNDERWORLD:0/0");
  private static final List<String> EXPECTED_FIRST_MONSTERS =
      List.of("AARONBROWNBARK", "AARONBROWNBARK", "Animated Corpse", "Animated Corpse", "Animated Corpse");
  private static final List<String> EXPECTED_FIRST_NPCS =
      List.of("AaronBrownbark", "Adriana", "Amandra", "Amelia", "AnchoriteRowan");
}
