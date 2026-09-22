package com.perso.T4C.teleport;

/**
 * A named, player-facing fast-travel destination shown in the Locations panel. {@code
 * unlockZoneId} is null for the original, always-available landmarks; when set, the entry only
 * appears for a player who has completed the quest that unlocks that zone (see {@code
 * QuestService.hasUnlockedZone}), so newly discovered zones are added to fast travel automatically
 * on completion and the access is never lost on rebirth (quest flags survive rebirth untouched).
 */
public record NamedLocation(String displayName, int tileX, int tileY, int worldZ, String unlockZoneId) {
  public NamedLocation(String displayName, int tileX, int tileY, int worldZ) {
    this(displayName, tileX, tileY, worldZ, null);
  }
}
