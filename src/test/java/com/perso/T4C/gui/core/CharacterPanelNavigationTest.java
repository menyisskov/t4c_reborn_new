package com.perso.T4C.gui.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CharacterPanelNavigationTest {
  @Test
  void inventoryTabUsesConfiguredRoute() {
    int[] routed = {0};
    int[] fallback = {0};

    CharacterPanel.inventoryTabAction(() -> routed[0]++, () -> fallback[0]++).run();

    assertEquals(1, routed[0]);
    assertEquals(0, fallback[0]);
  }

  @Test
  void inventoryTabUsesLocalFallbackWhenNoRouteIsConfigured() {
    int[] fallback = {0};

    CharacterPanel.inventoryTabAction(null, () -> fallback[0]++).run();

    assertEquals(1, fallback[0]);
  }
}
