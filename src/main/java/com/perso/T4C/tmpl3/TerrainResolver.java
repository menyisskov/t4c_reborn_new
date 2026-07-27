package com.perso.T4C.tmpl3;

public interface TerrainResolver {
    String resolveTerrainName(int x, int y);

    String familyName(String terrainName);

    String extrapolateTerrainName(String terrainName, int targetX, int targetY);
}
