package com.perso.T4C.teleport;

public record TeleportDefinition(
    int id, int sourceZ, int sourceX, int sourceY, int targetZ, int targetX, int targetY) {}
