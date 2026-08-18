package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0123 {
  private SpawnGroup0123() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Ancient Temple",
        20,
        35,
        2,
        5,
        List.of("Scavenger Bat", "Cursed Being"),
        List.of(
            new SpawnGroup.SpawnPoint(496, 208, 1),
            new SpawnGroup.SpawnPoint(548, 216, 1),
            new SpawnGroup.SpawnPoint(519, 237, 1),
            new SpawnGroup.SpawnPoint(558, 238, 1),
            new SpawnGroup.SpawnPoint(463, 241, 1),
            new SpawnGroup.SpawnPoint(495, 261, 1),
            new SpawnGroup.SpawnPoint(497, 301, 1),
            new SpawnGroup.SpawnPoint(472, 303, 1),
            new SpawnGroup.SpawnPoint(511, 308, 1)));
  }
}
