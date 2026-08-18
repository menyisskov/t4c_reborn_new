package com.perso.T4C.monster;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpawnGroup {
  private final String name;
  private final int tmin;
  private final int tmax;
  private final int distance;
  private final int spawnCount;
  private final List<String> creatures;
  private final List<SpawnPoint> positions;

  @Getter
  @AllArgsConstructor
  public static final class SpawnPoint {
    private final int x;
    private final int y;
    private final int z;
  }
}
