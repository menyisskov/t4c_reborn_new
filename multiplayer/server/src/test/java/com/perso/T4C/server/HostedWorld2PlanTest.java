package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** T4C-0397: the cavern (world 2) can be hosted as a second streamed region, joined to world 0 by derived portals. */
class HostedWorld2PlanTest {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");

  @Test
  void worldTwoIsAStreamedRegionAndItsCrossWorldPortalsAreEnabled() throws Exception {
    var plan = HostedRegionPlan.startingSlice(WORLD, true, true);
    var cavern = plan.regions().stream().filter(r -> r.spec().world() == 2).toList();
    assertEquals(1, cavern.size());
    assertTrue(cavern.getFirst().spec().streamed());
    assertTrue(plan.canStand(new com.perso.T4C.online.model.CharacterProgress.Respawn(2, cavern.getFirst().spec().spawnX(), cavern.getFirst().spec().spawnY())));
    long cross = plan.enabledPortals.stream().filter(p -> p.sourceWorld() != p.targetWorld() && (p.sourceWorld() == 2 || p.targetWorld() == 2)).count();
    assertTrue(cross > 100, "world 0 <-> world 2 portals should be derived, got " + cross);
    assertTrue(plan.enabledPortals.stream().noneMatch(p -> p.sourceWorld() == 2 && p.targetWorld() == 2), "same-region routes stay refused");
    System.out.println("WORLD2 cross=" + cross + " unhostable=" + plan.unhostableSpawnKeys.size() + " refused=" + plan.refusedPortals.size());
  }

  @Test
  void worldTwoNeedsWorldZeroStreaming() {
    assertThrows(IllegalArgumentException.class, () -> HostedRegionPlan.startingSlice(WORLD, false, true));
  }
}
