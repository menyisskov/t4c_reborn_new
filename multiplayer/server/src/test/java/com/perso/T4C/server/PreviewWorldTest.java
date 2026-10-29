package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.MonsterContent;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PreviewWorldTest {
  @Test void commandFloodIsBoundedPerSessionAndRefillsOnlyWithServerTicks() {
    var world = openWorld();
    var flood = world.join();
    var other = world.join();
    int admitted = 0;
    while (admitted < 1000) {
      try { world.admitRequest(flood.token()); admitted++; }
      catch (PreviewWorld.Rejected rejected) { assertEquals(429, rejected.status); break; }
    }
    assertTrue(admitted > 0 && admitted < 1000, "An unbounded same-tick flood must be rejected");
    assertThrows(PreviewWorld.Rejected.class, () -> world.admitRequest(flood.token()));
    assertDoesNotThrow(() -> world.admitRequest(other.token()), "One player's budget cannot consume another's");
    // Internal snapshots and logout remain available even while external commands are throttled.
    assertEquals(2, world.snapshot(flood.token()).players().size());
    for (int i = 0; i < PreviewWorld.TICK_RATE; i++) world.step();
    for (int i = 0; i < admitted; i++) world.admitRequest(flood.token());
    assertEquals(429, assertThrows(PreviewWorld.Rejected.class, () -> world.admitRequest(flood.token())).status);
    world.leave(flood.token());
    assertEquals(401, assertThrows(PreviewWorld.Rejected.class, () -> world.admitRequest(flood.token())).status);
    assertDoesNotThrow(() -> world.admitRequest(world.join().token()));
  }

  @Test void bothPlayersSeeTheSameAuthoredCreaturesAndInputsCannotMoveThem() throws Exception {
    var map = PreviewMap.load(java.nio.file.Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var world = new PreviewWorld(map, MonsterCatalog.loadBundled());
    var one = world.join();
    var two = world.join();
    var creatures = world.snapshot(one.token()).creatures();
    assertFalse(creatures.isEmpty());
    assertEquals(creatures, world.snapshot(two.token()).creatures());
    assertThrows(UnsupportedOperationException.class, creatures::clear);
    world.input(one.token(), 0, 1, 0);
    world.step();
    assertEquals(creatures, world.snapshot(two.token()).creatures());
    var replacement = new PreviewWorld(map, MonsterCatalog.loadBundled());
    assertEquals(creatures, replacement.snapshot(replacement.join().token()).creatures());
  }

  @Test void currentCreaturePositionIsSharedBySnapshotsAndPhysicalRetaliation() throws Exception {
    boolean[][] cells = new boolean[8][8];
    cells[3][4] = true;
    var map = new PreviewMap(cells, 0, 0, 1, 1);
    var definition = MonsterCatalog.loadBundled().monsters().get("Skeleton");
    var spawn = new MonsterContent.Spawn("test", definition.getName(), 2, 3, 0, false, true);
    var content = new MonsterContent(MonsterContent.VERSION,
        Map.of(definition.getName(), definition), List.of(spawn));
    var world = new PreviewWorld(map, content);
    var one = world.join();
    var two = world.join();
    var creature = world.creatureSpawns().getFirst();
    assertEquals(2, world.creaturePosition(creature.id()).x());
    assertEquals(3, world.creaturePosition(creature.id()).y());
    assertEquals(2, world.snapshot(one.token()).creatures().getFirst().x());
    var player = new PreviewWorld.PlayerView(1, 3.5, 3, 0, 0, 1, 1);
    assertFalse(OnlineRetaliation.canStrike(map, world, creature, player),
        "the authored anchor is beyond the one-tile physical reach");

    world.creaturePosition(creature.id(), 3, 3);
    assertEquals(world.snapshot(one.token()).creatures(), world.snapshot(two.token()).creatures());
    assertEquals(3, world.snapshot(one.token()).creatures().getFirst().x());
    assertTrue(OnlineRetaliation.canStrike(map, world, creature, player),
        "retaliation must use the published current position");
    world.step();
    assertEquals(3, world.creaturePosition(creature.id()).x(),
        "movement remains disabled until the cohesive pursuit integration");
    assertThrows(IllegalArgumentException.class, () -> world.creaturePosition(creature.id(), Double.NaN, 3));
    assertThrows(IllegalArgumentException.class, () -> world.creaturePosition(creature.id(), 8, 3));
    assertThrows(IllegalArgumentException.class, () -> world.creaturePosition(creature.id(), 4.5, 3.5));
    assertEquals(3, world.creaturePosition(creature.id()).x());
  }

  private PreviewWorld openWorld() { return new PreviewWorld(new PreviewMap(new boolean[60][80], 0, 0, 30, 30)); }

  @Test void packetFloodCannotAddSpeedAndOldInputCannotReplaceNewInput() {
    var world = openWorld();
    var one = world.join();
    var two = world.join();
    world.input(one.token(), 0, 1, 0);
    for (int i = 0; i < 1000; i++) world.input(two.token(), i, 1, 0);
    assertEquals(409, assertThrows(PreviewWorld.Rejected.class,
        () -> world.input(two.token(), 0, -1, 0)).status);
    world.step();
    var players = world.snapshot(one.token()).players();
    assertEquals(players.get(0).x(), players.get(1).x());
    assertTrue(players.get(0).x() > 30.5 && players.get(0).x() < 31);
    assertEquals(400, assertThrows(PreviewWorld.Rejected.class,
        () -> world.input(one.token(), 1, 100, 0)).status);
  }

  @Test void wallsAndDiagonalCornersRejectMovementAndSilenceStopsIt() {
    // Both movement rules (see ServerMovementParityTest) keep the player off the blocked tile and
    // out of the blocked corner; only the swept rule stops dead, the grid rule slides and finishes
    // its tile before standing still.
    for (var mode : com.perso.T4C.world.MovementRules.Mode.values()) {
      boolean[][] cells = new boolean[8][8];
      cells[3][4] = true;
      var world = new PreviewWorld(new PreviewMap(cells, 0, 0, 3, 3));
      world.movementMode(mode);
      var player = world.join();
      for (int i = 0; i < 20; i++) {
        world.input(player.token(), i, 1, 1);
        world.step();
        var now = world.snapshot(player.token()).players().getFirst();
        assertTrue(world.map().canStand(now.x(), now.y()), mode + " must never stand on a blocked tile");
        if (mode == com.perso.T4C.world.MovementRules.Mode.SWEPT_SUBSTEP)
          assertTrue(now.x() < 4 && now.y() < 4, "Must not cut across the blocked corner");
      }
      world.input(player.token(), 20, -1, 0);
      // A started tile is finished: one tile takes GRID_W / (speed per tick) ticks, so derive it from the shared speed.
      int finishTile = (int) Math.ceil(com.perso.T4C.config.GameConstants.GRID_W
          / (com.perso.T4C.config.GameConstants.PLAYER_SPEED / (double) PreviewWorld.TICK_RATE)) + 1;
      for (int i = 0; i < PreviewWorld.INPUT_TIMEOUT_TICKS + 1 + finishTile; i++) world.step();
      var afterSilence = world.snapshot(player.token()).players().getFirst();
      for (int i = 0; i < 20; i++) world.step();
      assertEquals(afterSilence.x(), world.snapshot(player.token()).players().getFirst().x());
      assertTrue(afterSilence.x() >= 0);
    }
  }

  @Test void sessionsArePrivateBoundedAndExpireWithoutHeartbeat() {
    var world = openWorld();
    var player = world.join();
    for (int i = 1; i < PreviewWorld.MAX_PLAYERS; i++) world.join();
    assertEquals(409, assertThrows(PreviewWorld.Rejected.class, world::join).status);
    assertEquals(401, assertThrows(PreviewWorld.Rejected.class, () -> world.snapshot("invalid")).status);
    world.leave(player.token());
    assertEquals(401, assertThrows(PreviewWorld.Rejected.class, () -> world.snapshot(player.token())).status);
    var replacement = world.join();
    assertNotEquals(player.token(), replacement.token());
    for (int i = 0; i <= PreviewWorld.SESSION_TIMEOUT_TICKS; i++) world.step();
    assertEquals(401, assertThrows(PreviewWorld.Rejected.class, () -> world.snapshot(replacement.token())).status);
    var fresh = world.join();
    assertEquals(1, world.snapshot(fresh.token()).players().size());
  }

  @Test void thePublishedLookIsOnlyWhatTheServerSetAndIsNotSharedMutableState() {
    var world = openWorld();
    var one = world.join();
    var two = world.join();
    assertNull(world.snapshot(two.token()).players().getFirst().name(), "no look until the server commits one");
    var worn = new java.util.HashMap<String,String>(Map.of("BODY","item.robe"));
    world.look(one.token(), "Brindle", "female", worn);
    worn.put("HEAD","item.secret_later_edit");
    var seen = world.snapshot(two.token()).players().stream().filter(p -> p.id() == one.id()).findFirst().orElseThrow();
    assertEquals("Brindle", seen.name());
    assertEquals("female", seen.gender());
    assertEquals(Map.of("BODY","item.robe"), seen.equipment());
    assertThrows(UnsupportedOperationException.class, () -> seen.equipment().clear());
    var gson = new com.google.gson.Gson().toJson(seen);
    assertTrue(gson.contains("\"equipment\":{\"BODY\":\"item.robe\"}"));
    // An actor with no look serialises exactly as before (no new keys): older clients see no change.
    var plain = new com.google.gson.Gson().toJson(world.snapshot(two.token()).players().stream()
        .filter(p -> p.id() == two.id()).findFirst().orElseThrow());
    assertFalse(plain.contains("name") || plain.contains("gender") || plain.contains("equipment"));
  }
}
