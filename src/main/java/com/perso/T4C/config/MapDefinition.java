package com.perso.T4C.config;

import lombok.Getter;

/**
 * Map metadata for world layers.
 */
@Getter
public enum MapDefinition {
    WORLDMAP(Paths.WORLDMAP_MAPBIN, Paths.COLLISION_MAP, 0),
    DUNGEON(Paths.DUNGEON_MAPBIN, "assets/maps/dungeon/DungeonMap.colbin", 1),
    CAVERN(Paths.CAVERN_MAPBIN, "assets/maps/cavern/CavernMap.colbin", 2),
    UNDERWORLD(Paths.UNDERWORLD_MAPBIN, "assets/maps/underworld/underworld.colbin", 3);

    private final String mapPath;
    private final String collisionPath;
    private final int z;

    MapDefinition(String mapPath, String collisionPath, int z) {
        this.mapPath = mapPath;
        this.collisionPath = collisionPath;
        this.z = z;
    }

    public static MapDefinition fromZ(int z) {
        for (MapDefinition def : values()) {
            if (def.z == z) {
                return def;
            }
        }
        return WORLDMAP;
    }
}
