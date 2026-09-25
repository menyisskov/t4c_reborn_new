package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemBalance;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** T4C-0021: the 96 Ancient Celestial/Empyrean armor pieces (ArmorSetGenerator) shipped with no
 * shop listing and no monster drop at all - completely unobtainable. Every one of them - and every
 * themed set the generator writes since (see ItemBalance.GENERATED_SET_PREFIXES) - must appear in
 * at least one monster's real loot table. Scans assets/items directly (rather than
 * hard-coding ArmorSetGenerator's private tier/flavor/piece lists) so this stays correct if the
 * generator's own output changes. */
class ArmorSetLootCoverageTest {
  @Test
  void everyGeneratedArmorPieceHasAtLeastOneMonsterLootSource() {
    File dir = new File("assets/items");
    File[] files = dir.listFiles((d, n) ->
        n.endsWith(".json") && ItemBalance.isGeneratedSetPiece(n.replace(".json", "")));
    assertTrue(files != null && files.length > 0, "expected generated armor JSON files to exist");

    Set<String> droppedItemKeys = new HashSet<>();
    for (MonsterDef def : MonsterRegistry.load()) {
      if (def.getLoot() == null) continue;
      for (MonsterDef.LootDrop drop : def.getLoot()) {
        if (drop != null) droppedItemKeys.add(drop.getItem());
      }
    }

    Set<String> missing =
        java.util.Arrays.stream(files)
            .map(f -> f.getName().replace(".json", ""))
            .filter(key -> !droppedItemKeys.contains(key))
            .collect(Collectors.toSet());

    assertTrue(
        missing.isEmpty(),
        "these generated armor items have no monster loot source at all: " + missing);
  }
}
