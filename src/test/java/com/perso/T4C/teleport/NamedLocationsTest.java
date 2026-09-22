package com.perso.T4C.teleport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.QuestDefinitions;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** T4C-0019: fast travel to a fork zone is granted only once its unlock quest is completed, and
 * every zone id referenced from either side (NamedLocations / QuestDef.unlockZoneId) must match
 * the other exactly - a typo here would silently and permanently hide a location. */
class NamedLocationsTest {
  @Test
  void unconditionalLocationsAreAlwaysVisible() {
    List<NamedLocation> visible = NamedLocations.forPlayer(new Player());
    assertTrue(visible.stream().anyMatch(l -> "Lighthaven".equals(l.displayName())));
    assertTrue(visible.stream().anyMatch(l -> "The Oracle".equals(l.displayName())));
  }

  @Test
  void zoneGatedLocationsOnlyAppearAfterTheirUnlockQuestIsCompleted() {
    Player player = new Player();
    assertFalse(
        NamedLocations.forPlayer(player).stream()
            .anyMatch(l -> "Windhowl Marches".equals(l.displayName())));

    player.setQuestFlag(QuestService.zoneUnlockFlag("windhowl_marches"), 1);

    assertTrue(
        NamedLocations.forPlayer(player).stream()
            .anyMatch(l -> "Windhowl Marches".equals(l.displayName())));
  }

  @Test
  void everyZoneGatedLocationMatchesARealQuestsUnlockZoneId() {
    Set<String> unlockedByQuests =
        QuestDefinitions.all().stream()
            .map(QuestDef::getUnlockZoneId)
            .filter(id -> id != null && !id.isBlank())
            .collect(Collectors.toSet());

    for (NamedLocation location : NamedLocations.all()) {
      if (location.unlockZoneId() != null) {
        assertTrue(
            unlockedByQuests.contains(location.unlockZoneId()),
            "No quest unlocks zone id '" + location.unlockZoneId() + "' ("
                + location.displayName() + ")");
      }
    }

    Set<String> zoneGatedLocationIds =
        NamedLocations.all().stream()
            .map(NamedLocation::unlockZoneId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
    for (String zoneId : unlockedByQuests) {
      assertTrue(
          zoneGatedLocationIds.contains(zoneId),
          "A quest unlocks zone id '" + zoneId + "' but no NamedLocation is gated on it");
    }
  }

  @Test
  void rebirthNeverClearsAZoneUnlockFlagOrItsFastTravelEntry() {
    Player player = new Player();
    player.setQuestFlag(QuestService.zoneUnlockFlag("avalon_sanctuary"), 1);

    com.perso.T4C.npc.behavior.RebirthBehavior.perform(player);

    assertTrue(QuestService.hasUnlockedZone(player, "avalon_sanctuary"));
    assertTrue(
        NamedLocations.forPlayer(player).stream()
            .anyMatch(l -> "Avalon Sanctuary".equals(l.displayName())));
  }
}
