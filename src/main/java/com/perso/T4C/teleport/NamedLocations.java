package com.perso.T4C.teleport;

import java.util.List;

/**
 * Fast-travel destinations offered in the Locations panel (Ctrl+L). Coordinates are tile
 * positions, matching the ones used by the existing Gateway spells (e.g. Scroll of Lighthaven).
 * Add more entries here as new landmarks get coordinates confirmed.
 */
public final class NamedLocations {
  private NamedLocations() {}

  public static List<NamedLocation> all() {
    return List.of(
        new NamedLocation("Lighthaven", 2941, 1062, 0),
        new NamedLocation("Silversky", 1495, 2470, 0),
        new NamedLocation("Windhowl", 1812, 1293, 0),
        new NamedLocation("Colosseum", 1730, 1830, 0),
        new NamedLocation("Home", 2951, 1038, 0),
        new NamedLocation("Makrsh Ptangh", 2265, 295, 1));
  }
}
