package com.perso.T4C.teleport;

import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import java.util.ArrayList;
import java.util.List;

/**
 * Fast-travel destinations offered in the Locations panel (Ctrl+L). Coordinates are tile
 * positions, matching the ones used by the existing Gateway spells (e.g. Scroll of Lighthaven).
 * Add more entries here as new landmarks get coordinates confirmed.
 */
public final class NamedLocations {
  private NamedLocations() {}

  public static List<NamedLocation> all() {
    return List.of(
        new NamedLocation("Lighthaven", 2941, 1062, 0),
        new NamedLocation("Silversky", 1495, 2470, 0),
        new NamedLocation("Windhowl", 1812, 1293, 0),
        new NamedLocation("Colosseum", 1725, 1825, 0),
        new NamedLocation("Home", 2951, 1038, 0),
        new NamedLocation("Makrsh Ptangh", 2265, 295, 1),
        new NamedLocation("Stonecrest", 144, 737, 0),
        new NamedLocation("Tarantula Pond", 773, 1831, 0),
        new NamedLocation("Skraug Camp", 601, 172, 0),
        new NamedLocation("The Oracle", 2968, 2141, 2),
        // T4C-0019: the fork's new zones - each entry only appears for a player who has
        // completed that zone's own quest (see QuestDef.unlockZoneId / QuestService.complete()),
        // so fast travel to a newly discovered zone is granted automatically on completion and
        // is never lost on rebirth (quest flags are untouched by RebirthBehavior).
        new NamedLocation("The Sunken Chancel", 1750, 2300, 0, "sunken_chancel"),
        new NamedLocation("Cinderreach Hills", 1900, 1600, 0, "cinderreach_hills"),
        // T4C-0024: these five relocated onto Kraanhold, a new continent painted for this pass
        // (real ground art + collision, not just spawns on existing terrain) - see
        // docs/content-ideas for the placement rationale.
        new NamedLocation("Windhowl Marches", 2380, 2430, 0, "windhowl_marches"),
        new NamedLocation("The Hollow March", 2500, 2700, 0, "hollow_march"),
        new NamedLocation("Lesser Drake's Aerie", 2350, 2900, 0, "lesser_drakes_aerie"),
        new NamedLocation("Greater Drake's Bastion", 2650, 2880, 0, "greater_drakes_bastion"),
        new NamedLocation("Drake's Lair", 2850, 2780, 0, "drakes_lair"),
        new NamedLocation("Deep Ones Cave", 330, 2246, 0, "deep_ones_cave"),
        new NamedLocation("Avalon Sanctuary", 1340, 1477, 0, "avalon_sanctuary"),
        new NamedLocation("The Avalon Wilds", 1265, 1400, 0, "avalon_wilds"),
        new NamedLocation("The Fading Veil", 1420, 1560, 0, "fading_veil"));
  }

  /** {@link #all()} filtered to the entries a given player currently has access to: every
   * unconditional (original) landmark, plus any zone whose unlock quest they've completed. */
  public static List<NamedLocation> forPlayer(Player player) {
    List<NamedLocation> visible = new ArrayList<>();
    for (NamedLocation location : all()) {
      if (location.unlockZoneId() == null
          || QuestService.hasUnlockedZone(player, location.unlockZoneId())) {
        visible.add(location);
      }
    }
    return List.copyOf(visible);
  }
}
