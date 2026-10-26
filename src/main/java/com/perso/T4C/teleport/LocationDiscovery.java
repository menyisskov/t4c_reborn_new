package com.perso.T4C.teleport;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.player.GmRank;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import java.util.List;

/** A 100-coordinate diameter means a radius of 50 tiles in map coordinates. */
public final class LocationDiscovery {
  public static final int RADIUS = 50;

  private LocationDiscovery() {}

  public static boolean available(Player player, TravelCatalogue.Destination destination) {
    if (player == null || !destination.mapped()) return false;
    if (player.getGmRank().atLeast(GmRank.GM)) return true;
    if ("base.shattered_isle".equals(destination.id())
        || "base.drakes_lair".equals(destination.id())) {
      boolean islandUnlocked = QuestService.hasUnlockedZone(player, "shattered_isle");
      if ("base.shattered_isle".equals(destination.id())) return islandUnlocked;
      // Kaelis opens the return route as part of the same passage, even for characters who
      // reached him without discovering the Drake's Lair travel point.
      if (islandUnlocked) return true;
    }
    return player.getQuestFlag(destination.discoveryFlag()) == 1;
  }

  public static boolean discoverNearby(Player player) {
    return discoverNearby(player, TravelCatalogue.all());
  }

  public static boolean discoverNearby(
      Player player, List<TravelCatalogue.Destination> destinations) {
    if (player == null) return false;
    double x = player.getCoordinates().getX() / GameConstants.GRID_W;
    double y = player.getCoordinates().getY() / GameConstants.GRID_H;
    int z = player.getCoordinates().getZ();
    boolean changed = false;
    for (var destination : destinations) {
      if (!destination.mapped()
          || destination.z() != z
          || player.getQuestFlag(destination.discoveryFlag()) == 1) continue;
      double dx = x - destination.x(), dy = y - destination.y();
      if (dx * dx + dy * dy <= (double) RADIUS * RADIUS) {
        player.setQuestFlag(destination.discoveryFlag(), 1);
        changed = true;
      }
    }
    return changed;
  }
}
