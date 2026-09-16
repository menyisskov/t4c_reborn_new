package com.perso.T4C.teleport;

/** A named, player-facing fast-travel destination shown in the Locations panel. */
public record NamedLocation(String displayName, int tileX, int tileY, int worldZ) {}
