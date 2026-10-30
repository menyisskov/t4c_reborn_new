package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.online.OnlineInventoryPanel;
import org.junit.jupiter.api.Test;

class MainGameScreenInventoryNavigationTest {
  @Test
  void remoteInventoryNavigationOpensAndTogglesClosed() {
    OnlineInventoryPanel panel = new OnlineInventoryPanel();
    int[] closedGui = {0};
    int[] closedOtherPanels = {0};

    MainGameScreen.toggleRemoteInventory(
        panel, () -> closedGui[0]++, () -> closedOtherPanels[0]++);

    assertTrue(panel.isOpen());
    assertEquals(1, closedGui[0]);
    assertEquals(1, closedOtherPanels[0]);

    MainGameScreen.toggleRemoteInventory(
        panel, () -> closedGui[0]++, () -> closedOtherPanels[0]++);

    assertFalse(panel.isOpen());
    assertEquals(1, closedGui[0]);
    assertEquals(1, closedOtherPanels[0]);
  }
}
