package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.death.DeathPenaltyService;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.definition.AvalonWildsVigil;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import java.io.File;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Checks the shipped map and spawn registry together, not a synthetic map fixture. */
class AvalonSanctuaryMapIntegrationTest {
  private record Tile(int x, int y) {}

  @Test
  void arrivalConnectsToSafeTownServicesChestAndHuntingLocations() throws Exception {
    CollisionReader map = new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
    Set<Tile> reachable = floodFill(map, new Tile(4040, 1477));
    List<SpawnDefinition> townNpcs =
        SpawnRegistry.npcs().stream()
            .filter(AvalonSanctuaryMapIntegrationTest::insideTown)
            .toList();
    assertEquals(9, townNpcs.size(), "all nine Avalon sanctuary services must remain in town");
    for (SpawnDefinition npc : townNpcs) {
      assertTrue(
          DeathPenaltyService.isSafeHaven(map.getCollision(npc.x(), npc.y())),
          npc.type() + " must stand on a protected tile");
      assertTrue(
          reachable.contains(new Tile(npc.x(), npc.y())),
          npc.type() + " must be reachable from the arrival point");
    }
    for (Tile destination :
        List.of(
            new Tile(4053, 1465), // sanctuary chest
            new Tile(3950, 1345), // northern Stalker glade
            new Tile(4000, 1390), // southern Stalker glade
            new Tile(3905, 1400), // Fey Warden grove
            new Tile(3965, 1460))) { // Caradoc's clearing
      assertTrue(reachable.contains(destination), "unreachable Avalon destination " + destination);
    }
    assertTrue(
        DeathPenaltyService.isSafeHaven(map.getCollision(4040, 1477)),
        "arrivals must begin protected");
    for (SpawnDefinition monster : SpawnRegistry.monsters()) {
      assertFalse(
          monster.aggressive() && insideTown(monster),
          "hostile spawn inside sanctuary: " + monster);
    }
  }

  @Test
  void allWildsStalkersCountForVigilButVerdantEscortsDoNot() throws Exception {
    QuestDef quest = AvalonWildsVigil.definition();
    CollisionReader map = new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
    Set<Tile> reachable = floodFill(map, new Tile(4040, 1477));
    List<SpawnDefinition> stalkers =
        SpawnRegistry.monsters().stream()
            .filter(
                s -> s.type().equals(quest.getTargetMonster()) && s.z() == quest.getTargetWorldZ())
            .toList();
    List<SpawnDefinition> wilds =
        stalkers.stream().filter(s -> s.x() < 4100 && s.y() < 1500).toList();
    List<SpawnDefinition> escorts =
        stalkers.stream().filter(s -> s.x() >= 4100 && s.y() >= 1500).toList();
    assertEquals(20, wilds.size(), "the two hunting glades provide twenty Stalker spawns");
    assertEquals(3, escorts.size(), "Verdant Warden escorts must stay separate from the Wilds");
    assertEquals(wilds.size() + escorts.size(), stalkers.size());
    for (SpawnDefinition spawn : wilds) {
      assertTrue(insideQuest(spawn, quest), "Wilds spawn outside Vigil objective: " + spawn);
      assertTrue(
          reachable.contains(new Tile(spawn.x(), spawn.y())), "unreachable Wilds spawn: " + spawn);
      assertFalse(
          DeathPenaltyService.isSafeHaven(map.getCollision(spawn.x(), spawn.y())),
          "hunting spawns must allow combat: " + spawn);
    }
    for (SpawnDefinition spawn : escorts) {
      assertFalse(
          insideQuest(spawn, quest), "escort must not count for the Wilds objective: " + spawn);
    }
  }

  private static boolean insideTown(SpawnDefinition spawn) {
    return spawn.z() == 0
        && spawn.x() >= 3995
        && spawn.x() <= 4090
        && spawn.y() >= 1440
        && spawn.y() <= 1545;
  }

  private static boolean insideQuest(SpawnDefinition spawn, QuestDef quest) {
    long dx = spawn.x() - quest.getAreaCenterX();
    long dy = spawn.y() - quest.getAreaCenterY();
    return dx * dx + dy * dy <= (long) quest.getAreaRadiusTiles() * quest.getAreaRadiusTiles();
  }

  private static Set<Tile> floodFill(CollisionReader map, Tile start) {
    assertFalse(map.hasCollision(start.x(), start.y()), "arrival must be walkable");
    Set<Tile> reached = new HashSet<>();
    ArrayDeque<Tile> queue = new ArrayDeque<>();
    reached.add(start);
    queue.add(start);
    while (!queue.isEmpty()) {
      Tile current = queue.removeFirst();
      for (Tile next :
          List.of(
              new Tile(current.x() - 1, current.y()),
              new Tile(current.x() + 1, current.y()),
              new Tile(current.x(), current.y() - 1),
              new Tile(current.x(), current.y() + 1))) {
        // A route must stay within the rebuilt sanctuary/Wilds region; leaving Avalon is no
        // shortcut.
        if (next.x() < 3850
            || next.x() > 4150
            || next.y() < 1300
            || next.y() > 1550
            || map.hasCollision(next.x(), next.y())
            || !reached.add(next)) continue;
        queue.addLast(next);
      }
    }
    return reached;
  }
}
