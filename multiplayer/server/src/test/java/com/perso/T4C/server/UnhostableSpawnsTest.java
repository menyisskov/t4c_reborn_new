package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/** No database. The bundled world-0 unhostable list is exactly what the real collision file and the foot/stand rules produce. */
class UnhostableSpawnsTest {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");

  @Test void theBundledListEqualsTheRecomputedAuditByteForByte() throws Exception {
    var recomputed = UnhostableSpawnAudit.audit(WORLD);
    var bundled = UnhostableSpawnAudit.bundled();
    assertEquals(recomputed, bundled, "run UnhostableSpawnAudit and commit the output when content or rules change");
    assertEquals(153, bundled.footBlocked(), "the design's count: anchors on foot-blocked tiles");
    assertEquals(bundled.entries().size(), bundled.footBlocked() + bundled.noSprite());
    var resource = new String(UnhostableSpawnAudit.class.getResourceAsStream(UnhostableSpawnAudit.RESOURCE).readAllBytes(),
        java.nio.charset.StandardCharsets.UTF_8);
    assertEquals(UnhostableSpawnAudit.toJson(recomputed), resource, "the committed file is the tool's exact output");
  }

  @Test void everyListedSpawnIsUnstandableAndEveryOtherWorldZeroSpawnIsHostedAndStandable() throws Exception {
    var plan = HostedRegionPlan.startingSlice(WORLD, true);
    var listed = new TreeSet<>(UnhostableSpawnAudit.bundledKeys());
    var map = plan.regions().getFirst().map();
    var monsters = HostedWorlds.withoutNpcOwnedSpawns(MonsterCatalog.loadBundled());
    var all = monsters.region(0, 0, 0, map.width, map.height).stream().filter(s -> s.definition() != null).toList();
    var excluded = new TreeSet<String>();
    for (var spawn : all) {
      var definition = monsters.monsters().get(spawn.definition());
      if (map.blocksFoot(spawn.x(), spawn.y()) || definition.getWalkPattern() == null || definition.getWalkPattern().isBlank())
        excluded.add(spawn.persistenceKey());
    }
    assertEquals(listed, excluded, "exactly the foot-blocked or sprite-less set, never a pattern");
    assertTrue(plan.unhostableSpawnKeys.containsAll(listed));
    try (var hosted = new HostedWorlds(plan, null, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled())) {
      var world = hosted.regions.getFirst().world;
      assertTrue(world.streamedCreatures());
      assertEquals(all.size() - listed.size(), world.hostedSpawns().size(), "every standable authored world-0 spawn is hosted");
      assertTrue(world.creatureSpawns().isEmpty(), "nothing is live before a player is near");
      for (var spawn : world.hostedSpawns()) assertFalse(map.blocksFoot(spawn.spawn().x(), spawn.spawn().y()));
    }
    // The shipped plan is unchanged by the list.
    assertTrue(HostedRegionPlan.startingSlice(WORLD).unhostableSpawnKeys.stream().noneMatch(listed::contains));
  }
}
