package com.perso.T4C.teleport;

import java.util.List;

/**
 * A named, player-facing fast-travel destination shown in the Locations panel. {@code
 * unlockZoneId} is null for the fork's own new zones' unconditional entries; when set, the entry
 * only appears for a player who has completed the quest that unlocks that zone (see {@code
 * QuestService.hasUnlockedZone}), so newly discovered zones are added to fast travel automatically
 * on completion and the access is never lost on rebirth (quest flags survive rebirth untouched).
 *
 * <p>{@code minIslandAccess} is the original-game equivalent for the classic islands (T4C-00XX):
 * 0 for Arakas (every character starts there, no gate), 1 once {@code __QUEST_ISLAND_ACCESS}
 * reaches 1 (Raven's Dust). Higher tiers are not wired here - see that flag's own callers before
 * gating anything on 2+, since nothing in this codebase currently sets it past 1.
 *
 * <p>{@code requiresAnyItem} (T4C-00XX) gates on carrying at least one of the listed items -
 * used for the Oracle entries, which the canon walkthrough gates on holding the Key of Artherk
 * or the Key of Ogrimar rather than on any zone or island tier. Empty means no item requirement.
 */
public record NamedLocation(
    String displayName,
    int tileX,
    int tileY,
    int worldZ,
    String unlockZoneId,
    int minIslandAccess,
    List<String> requiresAnyItem) {
  public NamedLocation(String displayName, int tileX, int tileY, int worldZ) {
    this(displayName, tileX, tileY, worldZ, null, 0, List.of());
  }

  public NamedLocation(String displayName, int tileX, int tileY, int worldZ, String unlockZoneId) {
    this(displayName, tileX, tileY, worldZ, unlockZoneId, 0, List.of());
  }

  public NamedLocation(String displayName, int tileX, int tileY, int worldZ, int minIslandAccess) {
    this(displayName, tileX, tileY, worldZ, null, minIslandAccess, List.of());
  }

  public static NamedLocation requiringAnyItem(
      String displayName, int tileX, int tileY, int worldZ, String... itemKeys) {
    return new NamedLocation(displayName, tileX, tileY, worldZ, null, 0, List.of(itemKeys));
  }
}
