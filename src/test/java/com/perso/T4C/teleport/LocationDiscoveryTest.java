package com.perso.T4C.teleport;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.GmRank;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocationDiscoveryTest {
  private static final TravelCatalogue.Destination STOP =
      new TravelCatalogue.Destination("test.stop", "landmarks", "Test", "Test", 900, 900, 2);

  private static void position(Player player, double x, double y, int z) {
    player.setWorldPosition(
        (float) (x * GameConstants.GRID_W), (float) (y * GameConstants.GRID_H), z);
  }

  @Test
  void discoveryUsesAnInclusive50TileCircleOnTheSameMap() {
    Player player = new Player();
    position(player, 930, 940.01, 2);
    assertFalse(LocationDiscovery.discoverNearby(player, List.of(STOP)));
    assertFalse(LocationDiscovery.available(player, STOP));
    position(player, 930, 940, 1);
    assertFalse(LocationDiscovery.discoverNearby(player, List.of(STOP)));
    position(player, 930, 940, 2);
    assertTrue(LocationDiscovery.discoverNearby(player, List.of(STOP)));
    assertTrue(LocationDiscovery.available(player, STOP));
    assertFalse(LocationDiscovery.discoverNearby(player, List.of(STOP)));
    position(player, 0, 0, 0);
    assertTrue(LocationDiscovery.available(player, STOP));
  }

  @Test
  void GmsBypassDiscoveryWithoutPermanentlyGrantingEveryStop() {
    Player player = new Player();
    player.setGmRank(GmRank.GM);
    assertTrue(LocationDiscovery.available(player, STOP));
    player.setGmRank(GmRank.SUPER_GM);
    assertTrue(LocationDiscovery.available(player, STOP));
    player.setGmRank(GmRank.PLAYER);
    assertFalse(LocationDiscovery.available(player, STOP));
    var unmapped =
        new TravelCatalogue.Destination("unmapped", "landmarks", "Test", "Test", null, null, null);
    player.setGmRank(GmRank.GM);
    assertFalse(LocationDiscovery.available(player, unmapped));
  }

  @Test
  void discoverySurvivesSaveLoadAndRebirth() throws Exception {
    Player player = new Player();
    position(player, 900, 900, 2);
    LocationDiscovery.discoverNearby(player, List.of(STOP));
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(player), restored);
    assertTrue(LocationDiscovery.available(restored, STOP));
    com.perso.T4C.npc.behavior.RebirthBehavior.perform(restored);
    assertTrue(LocationDiscovery.available(restored, STOP));
  }

  @Test
  void kaelisQuestOpensTheIsolatedIslandAndReturnRouteAcrossSaveLoad() {
    var island =
        TravelCatalogue.all().stream()
            .filter(destination -> destination.id().equals("base.shattered_isle"))
            .findFirst()
            .orElseThrow();
    var returnStop =
        TravelCatalogue.all().stream()
            .filter(destination -> destination.id().equals("base.drakes_lair"))
            .findFirst()
            .orElseThrow();
    Player player = new Player();
    player.setQuestFlag(island.discoveryFlag(), 1);
    assertFalse(LocationDiscovery.available(player, island));
    assertFalse(LocationDiscovery.available(player, returnStop));

    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    assertNotNull(
        quests.giveOrReport("shattered_isle_passage", "OutriderKaelis", player));
    for (int i = 0; i < 3; i++)
      assertTrue(quests.recordKill(player, "Kraanian Dragonguard", 0, 2850, 2780));
    assertNotNull(quests.turnInReadyQuests("OutriderKaelis", player));
    assertTrue(QuestService.hasUnlockedZone(player, "shattered_isle"));
    assertTrue(LocationDiscovery.available(player, island));
    assertTrue(LocationDiscovery.available(player, returnStop));
    assertNotNull(TravelArrival.find(island));

    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(player), restored);
    assertTrue(LocationDiscovery.available(restored, island));
    assertTrue(LocationDiscovery.available(restored, returnStop));
    com.perso.T4C.npc.behavior.RebirthBehavior.perform(restored);
    assertTrue(LocationDiscovery.available(restored, island));
    assertTrue(LocationDiscovery.available(restored, returnStop));
  }

  @Test
  void theVisibleCatalogueHasUniqueIdsAndResolvableQuestLabels() {
    var ids = new HashSet<String>();
    var categories = new HashSet<String>();
    for (var destination : TravelCatalogue.all()) {
      assertTrue(ids.add(destination.id()), destination.id());
      categories.add(destination.category());
      assertTrue(TravelCatalogue.CATEGORIES.contains(destination.category()));
      assertNotNull(destination.name());
      if (destination.nameKey().startsWith("travel."))
        assertTrue(I18n.has(destination.nameKey()), destination.nameKey());
      if (destination.descriptionKey().startsWith("travel."))
        assertTrue(I18n.has(destination.descriptionKey()), destination.descriptionKey());
      if (destination.mapped()) assertFalse(destination.x() == 0 && destination.y() == 0);
      if (destination.mapped()) assertNotNull(TravelArrival.find(destination), destination.id());
    }
    assertTrue(categories.containsAll(TravelCatalogue.CATEGORIES));
  }

  @Test
  void arrivalsRequireClearFootprintsAndFailForBlockedAreas() {
    var landing = TravelArrival.find(10, 10, (x, y) -> x != 12 || y < 9 || y > 10);
    assertEquals(new TravelArrival.Tile(12, 10), landing);
    assertNull(TravelArrival.find(10, 10, (x, y) -> true));
  }
}
