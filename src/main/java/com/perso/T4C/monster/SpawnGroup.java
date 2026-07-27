package com.perso.T4C.monster;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * A named group of creature spawns, loaded from {@code assets/wda-json/groups.json}
 * and persisted in {@code assets/spawns/spawn_groups.bin}.
 */
@Getter
@AllArgsConstructor
public class SpawnGroup {
    private final String name;
    private final int tmin;
    private final int tmax;
    private final int distance;
    private final int spawnCount;
    /** codeIds of creature types that may spawn in this group. Never null. */
    private final List<String> creatures;
    /** Spawn point positions in T4C tile coordinates. Never null. */
    private final List<SpawnPoint> positions;

    @Getter
    @AllArgsConstructor
    public static final class SpawnPoint {
        private final int x;
        private final int y;
        private final int z;
    }
}
