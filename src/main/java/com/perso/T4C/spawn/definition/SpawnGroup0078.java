package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0078 {
  private SpawnGroup0078() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Chaos Spawn",
        120,
        200,
        1,
        5,
        List.of("Chaos Spawn"),
        List.of(
            new SpawnGroup.SpawnPoint(689, 414, 0),
            new SpawnGroup.SpawnPoint(722, 418, 0),
            new SpawnGroup.SpawnPoint(737, 422, 0),
            new SpawnGroup.SpawnPoint(648, 426, 0),
            new SpawnGroup.SpawnPoint(694, 427, 0),
            new SpawnGroup.SpawnPoint(638, 448, 0),
            new SpawnGroup.SpawnPoint(706, 454, 0),
            new SpawnGroup.SpawnPoint(664, 458, 0),
            new SpawnGroup.SpawnPoint(704, 483, 0),
            new SpawnGroup.SpawnPoint(730, 493, 0),
            new SpawnGroup.SpawnPoint(647, 494, 0),
            new SpawnGroup.SpawnPoint(675, 506, 0),
            new SpawnGroup.SpawnPoint(693, 509, 0),
            new SpawnGroup.SpawnPoint(620, 512, 0),
            new SpawnGroup.SpawnPoint(645, 526, 0),
            new SpawnGroup.SpawnPoint(710, 529, 0)));
  }
}
