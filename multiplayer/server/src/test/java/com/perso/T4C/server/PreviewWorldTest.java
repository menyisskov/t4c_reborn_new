package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PreviewWorldTest {
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
    boolean[][] cells = new boolean[8][8];
    cells[3][4] = true;
    var world = new PreviewWorld(new PreviewMap(cells, 0, 0, 3, 3));
    var player = world.join();
    for (int i = 0; i < 20; i++) { world.input(player.token(), i, 1, 1); world.step(); }
    var stopped = world.snapshot(player.token()).players().getFirst();
    assertTrue(stopped.x() < 4 && stopped.y() < 4, "Must not cut across the blocked corner");
    world.input(player.token(), 20, -1, 0);
    for (int i = 0; i < PreviewWorld.INPUT_TIMEOUT_TICKS + 1; i++) world.step();
    var afterSilence = world.snapshot(player.token()).players().getFirst();
    for (int i = 0; i < 20; i++) world.step();
    assertEquals(afterSilence.x(), world.snapshot(player.token()).players().getFirst().x());
    assertTrue(afterSilence.x() >= 0);
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
}
