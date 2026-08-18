package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0125 {
  private SpawnGroup0125() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Small Laby",
        20,
        40,
        2,
        5,
        List.of("Dark Spider", "Cursed Being", "Plague Rat"),
        List.of(
            new SpawnGroup.SpawnPoint(720, 76, 1),
            new SpawnGroup.SpawnPoint(783, 77, 1),
            new SpawnGroup.SpawnPoint(675, 92, 1),
            new SpawnGroup.SpawnPoint(744, 98, 1),
            new SpawnGroup.SpawnPoint(713, 101, 1),
            new SpawnGroup.SpawnPoint(755, 101, 1),
            new SpawnGroup.SpawnPoint(792, 103, 1),
            new SpawnGroup.SpawnPoint(689, 107, 1),
            new SpawnGroup.SpawnPoint(712, 126, 1),
            new SpawnGroup.SpawnPoint(739, 129, 1),
            new SpawnGroup.SpawnPoint(819, 136, 1),
            new SpawnGroup.SpawnPoint(653, 138, 1),
            new SpawnGroup.SpawnPoint(744, 139, 1),
            new SpawnGroup.SpawnPoint(706, 142, 1),
            new SpawnGroup.SpawnPoint(800, 143, 1),
            new SpawnGroup.SpawnPoint(800, 145, 1),
            new SpawnGroup.SpawnPoint(727, 151, 1),
            new SpawnGroup.SpawnPoint(820, 153, 1),
            new SpawnGroup.SpawnPoint(611, 155, 1),
            new SpawnGroup.SpawnPoint(781, 155, 1),
            new SpawnGroup.SpawnPoint(732, 166, 1),
            new SpawnGroup.SpawnPoint(675, 183, 1),
            new SpawnGroup.SpawnPoint(828, 183, 1),
            new SpawnGroup.SpawnPoint(639, 188, 1),
            new SpawnGroup.SpawnPoint(808, 194, 1),
            new SpawnGroup.SpawnPoint(700, 205, 1),
            new SpawnGroup.SpawnPoint(721, 206, 1),
            new SpawnGroup.SpawnPoint(767, 221, 1),
            new SpawnGroup.SpawnPoint(752, 235, 1),
            new SpawnGroup.SpawnPoint(748, 250, 1)));
  }
}
