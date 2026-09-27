package com.perso.T4C.quest;

import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.MONSTER_AGGRO_RANGE;
import static com.perso.T4C.config.GameConstants.MONSTER_PATROL_RADIUS;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_RADIUS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.quest.definition.QuestDefinitions;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.teleport.NamedLocation;
import com.perso.T4C.teleport.NamedLocations;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * T4C-0062: the compendium website answers "where do I find this quest-giver?" from two exported
 * facts - the giver's own {@code @Spawn} position and the fast-travel landmark list - rather than
 * from hand-written prose that can drift. Both of those are only as good as the data behind them,
 * so this pins the two ways they could quietly break: a quest naming a giver who is never spawned
 * anywhere (the site would then have nothing to say and would silently drop the directions), and a
 * giver sitting on a world level that carries no landmark at all (the site could name no route in).
 */
class QuestGiverLocationTest {

  private static List<SpawnDefinition> spawnsOf(String npcId) {
    List<SpawnDefinition> out = new ArrayList<>();
    for (SpawnDefinition s : SpawnRegistry.npcs()) {
      if (s.type().equals(npcId)) out.add(s);
    }
    return out;
  }

  @Test
  void everyQuestGiverIsSpawnedSomewhereInTheWorld() {
    List<String> missing = new ArrayList<>();
    for (QuestDef quest : QuestDefinitions.all()) {
      if (spawnsOf(quest.getGiverNpc()).isEmpty()) {
        missing.add(quest.getId() + " -> " + quest.getGiverNpc());
      }
    }
    assertTrue(
        missing.isEmpty(),
        "every quest's giver needs an @Spawn so the site can say where to find them; missing: "
            + missing);
  }

  @Test
  void everyQuestGiverSharesAWorldLevelWithAtLeastOneLandmark() {
    Set<Integer> levelsWithLandmarks = new HashSet<>();
    for (NamedLocation loc : NamedLocations.all()) levelsWithLandmarks.add(loc.worldZ());

    List<String> stranded = new ArrayList<>();
    for (QuestDef quest : QuestDefinitions.all()) {
      for (SpawnDefinition s : spawnsOf(quest.getGiverNpc())) {
        if (!levelsWithLandmarks.contains(s.z())) {
          stranded.add(quest.getGiverNpc() + " on world level " + s.z());
        }
      }
    }
    assertTrue(
        stranded.isEmpty(),
        "a quest-giver on a world level with no fast-travel landmark leaves the site unable to "
            + "give any route in; stranded: "
            + stranded);
  }

  @Test
  void landmarkNamesAreUniqueSoDirectionsAreUnambiguous() {
    Set<String> seen = new HashSet<>();
    List<String> duplicates = new ArrayList<>();
    for (NamedLocation loc : NamedLocations.all()) {
      if (!seen.add(loc.displayName())) duplicates.add(loc.displayName());
    }
    assertFalse(
        seen.isEmpty(), "NamedLocations.all() is empty, so no quest-giver can be given directions");
    assertTrue(
        duplicates.isEmpty(),
        "two landmarks sharing a name makes \"travel to X\" ambiguous; duplicates: " + duplicates);
  }

  /**
   * T4C-0065: two Tideworn Reaver spawns sat 16-24 tiles from HarbormasterRangor's dock - close
   * enough that, between the reaver's own 6-tile patrol radius and 8-tile aggro range and the
   * (non-stationary) giver's own 15-tile patrol radius, a reaver could reach and aggro a player
   * standing at the dock, interrupting the "Passage to Avalon" conversation before it could
   * finish. The safe distance below is exactly that worst case, in tiles; anything past it means
   * the two roaming ranges can no longer touch.
   *
   * <p>Scoped to this one giver/monster pair rather than every quest giver in the game: a
   * game-wide sweep turns up the same shape of problem at many other, unrelated NPCs, and fixing
   * those is a separate pass, not part of this one.
   */
  @Test
  void tidewornReaversCannotWanderIntoHarbormasterRangorsInteractionRange() {
    float safeDistanceTiles =
        NPC_PATROL_RADIUS / GRID_W + MONSTER_PATROL_RADIUS / GRID_W + MONSTER_AGGRO_RANGE / GRID_W;
    List<SpawnDefinition> givers = spawnsOf("HarbormasterRangor");
    assertFalse(givers.isEmpty(), "HarbormasterRangor must still be spawned somewhere");
    List<String> tooClose = new ArrayList<>();
    for (SpawnDefinition giver : givers) {
      for (SpawnDefinition monster : SpawnRegistry.monsters()) {
        if (!"Tideworn Reaver".equals(monster.type()) || monster.z() != giver.z()) continue;
        double distance = Math.hypot(monster.x() - giver.x(), monster.y() - giver.y());
        if (distance < safeDistanceTiles) {
          tooClose.add(
              "(" + monster.x() + "," + monster.y() + ") is " + Math.round(distance) + " tiles");
        }
      }
    }
    assertTrue(
        tooClose.isEmpty(),
        "a Tideworn Reaver can wander into HarbormasterRangor's dialogue and interrupt it: "
            + tooClose);
  }
}
