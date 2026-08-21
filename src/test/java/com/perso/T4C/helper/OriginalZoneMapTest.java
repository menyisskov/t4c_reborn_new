package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class OriginalZoneMapTest {
  @Test
  void lighthavenTempleTileIsNamedLighthaven() {
    assertEquals(0, OriginalZoneMap.zoneId(0, 2961, 1093));
    assertEquals("LIGHTHAVEN", OriginalZoneMap.displayName(0, 2961, 1093));
  }

  @Test
  void windhowlMarketTileIsNamedWindhowl() {
    assertEquals(3, OriginalZoneMap.zoneId(0, 210, 735));
    assertEquals("WINDHOWL", OriginalZoneMap.displayName(0, 210, 735));
  }

  @Test
  void unnamedWildernessDoesNotShowABanner() {
    assertEquals(OriginalZoneMap.EMPTY_ZONE, OriginalZoneMap.zoneId(0, 1725, 1835));
    assertNull(OriginalZoneMap.displayName(0, 1725, 1835));
  }
}
