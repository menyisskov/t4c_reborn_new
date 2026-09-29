package com.perso.T4C.teleport;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import java.util.ArrayList;
import java.util.List;

/**
 * Fast-travel destinations offered in the Locations panel (Ctrl+L). Coordinates are tile positions,
 * matching the ones used by the existing Gateway spells (e.g. Scroll of Lighthaven). Add more
 * entries here as new landmarks get coordinates confirmed.
 */
public final class NamedLocations {
  private NamedLocations() {}

  public static List<NamedLocation> all() {
    return List.of(
        new NamedLocation("Lighthaven", 2941, 1062, 0),
        new NamedLocation("Windhowl", 1723, 1237, 0),
        new NamedLocation("Colosseum", 1725, 1825, 0),
        new NamedLocation("Home", 2951, 1038, 0),
        new NamedLocation("Tarantula Pond", 773, 1831, 0),
        new NamedLocation("Skraug Camp", 601, 172, 0),
        // T4C-00XX: Raven's Dust - confirmed reachable in-game today (__QUEST_ISLAND_ACCESS
        // reaches 1 via RenegadeOrcLeader). Silversky itself, and the two NPCs confirmed to
        // stand there in the original walkthrough text (Zhakar at the Tower of Sorcery, Elysana
        // Blackrose in Silversky town).
        new NamedLocation("Silversky", 1495, 2470, 0, 1),
        // T4C-00XX: Stoneheim/Oracle-tier entries below were previously unconditional, which let
        // a brand-new Arakas-only character fast-travel there. Left unconditional for now rather
        // than gated on __QUEST_ISLAND_ACCESS >= 2: nothing in this codebase currently sets that
        // flag past 1 (Boreas/Yolak's own shop logic checks for a 2 that never happens), so
        // gating on it would hide these permanently instead of progressively. Needs the real
        // Stoneheim-access trigger identified before this can be tightened - see
        // DESIGN_GUIDELINES.md.
        new NamedLocation("Makrsh Ptangh", 2265, 295, 1),
        new NamedLocation("Stonecrest", 144, 737, 0),
        // T4C-00XX: the canon Oracle walkthrough opens with "Once you have the Key of Artherk or
        // the Key of Ogrimar, head to the Chamber of Providence" - that's the real gate, not an
        // island tier. "Oracle access" drops the character at the Ivory Chest (the Oracle Realm's
        // own entrance, coordinates confirmed in-game by the owner); "The Oracle" is the NPC's
        // own room, further in. Both open on the same item check since nothing in this codebase
        // currently tracks "has walked the intervening dungeon" as a separate flag - see
        // DESIGN_GUIDELINES.md for how much of that dungeon is unbuilt.
        NamedLocation.requiringAnyItem(
            "Oracle access", 2660, 2610, 2, "key_of_artherk", "key_of_ogrimar"),
        NamedLocation.requiringAnyItem(
            "The Oracle", 2724, 2192, 2, "key_of_artherk", "key_of_ogrimar"),
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
        new NamedLocation("Avalon Sanctuary", 4040, 1477, 0, "avalon_sanctuary"),
        new NamedLocation("The Avalon Wilds", 3965, 1400, 0, "avalon_wilds"),
        new NamedLocation("The Fading Veil", 4120, 1560, 0, "fading_veil"),
        // T4C-0056: quality-of-life stops at NPCs a player ends up walking back to repeatedly -
        // either across several unrelated quests, or because a single quest bounces the player
        // to and from them multiple times. Unconditional like the original landmarks above:
        // nothing to unlock, these NPCs are already reachable, this just cuts the walk.
        new NamedLocation("Lord Sunrock", 1609, 1181, 0),
        new NamedLocation("Asarr", 2139, 1226, 0),
        new NamedLocation("Lance Silversmith", 2580, 690, 0),
        new NamedLocation("Zhakar", 55, 1769, 0, 1),
        new NamedLocation("Elysana Blackrose", 1561, 2471, 0, 1),
        // Stoneheim-tier - see the comment above The Oracle: unconditional for now, same
        // unreachable-flag reason.
        new NamedLocation("Araknor", 2981, 1035, 0),
        new NamedLocation("Dionysus Silverstream", 1025, 1000, 0),
        new NamedLocation("Grant Hornkeep", 315, 740, 0),
        new NamedLocation("Filandrius", 985, 1465, 0));
  }

  /**
   * {@link #all()} filtered to the entries a given player currently has access to: every
   * unconditional landmark, plus any zone whose unlock quest they've completed, plus any
   * original-island landmark whose island-access tier they've reached, plus any entry gated on
   * carrying one of a set of items.
   */
  public static List<NamedLocation> forPlayer(Player player) {
    List<NamedLocation> visible = new ArrayList<>();
    for (NamedLocation location : all()) {
      boolean zoneOk =
          location.unlockZoneId() == null
              || QuestService.hasUnlockedZone(player, location.unlockZoneId());
      boolean islandOk = player.getQuestFlag("__QUEST_ISLAND_ACCESS") >= location.minIslandAccess();
      boolean itemOk =
          location.requiresAnyItem().isEmpty()
              || location.requiresAnyItem().stream()
                  .anyMatch(key -> InventoryService.count(player, key) > 0);
      if (zoneOk && islandOk && itemOk) {
        visible.add(location);
      }
    }
    return List.copyOf(visible);
  }
}
