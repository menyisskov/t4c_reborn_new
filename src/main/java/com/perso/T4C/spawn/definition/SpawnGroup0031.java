package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0031 {
  private SpawnGroup0031() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Dark Warlord",
        200,
        400,
        1,
        5,
        List.of("Dark Warlord"),
        List.of(
            new SpawnGroup.SpawnPoint(502, 470, 1),
            new SpawnGroup.SpawnPoint(491, 494, 1),
            new SpawnGroup.SpawnPoint(465, 502, 1),
            new SpawnGroup.SpawnPoint(519, 507, 1),
            new SpawnGroup.SpawnPoint(559, 525, 1),
            new SpawnGroup.SpawnPoint(537, 528, 1),
            new SpawnGroup.SpawnPoint(467, 529, 1),
            new SpawnGroup.SpawnPoint(448, 536, 1),
            new SpawnGroup.SpawnPoint(477, 542, 1),
            new SpawnGroup.SpawnPoint(470, 554, 1),
            new SpawnGroup.SpawnPoint(528, 557, 1),
            new SpawnGroup.SpawnPoint(507, 560, 1),
            new SpawnGroup.SpawnPoint(498, 580, 1)));
  }
}
