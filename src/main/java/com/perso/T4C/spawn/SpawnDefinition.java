package com.perso.T4C.spawn;

public record SpawnDefinition(
    String type, int x, int y, int z, boolean stationary, boolean aggressive) {}
