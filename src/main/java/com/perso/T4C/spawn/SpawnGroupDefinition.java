package com.perso.T4C.spawn;

import com.perso.T4C.monster.SpawnGroup;
import java.util.List;

public record SpawnGroupDefinition(
    String name,
    int tmin,
    int tmax,
    int distance,
    int spawnCount,
    List<String> creatures,
    List<SpawnGroup.SpawnPoint> positions) {
  public SpawnGroup toSpawnGroup() {
    return new SpawnGroup(name, tmin, tmax, distance, spawnCount, creatures, positions);
  }
}
