package com.perso.T4C.gui.screen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.i18n.I18n;
import org.junit.jupiter.api.Test;

class ControlsScreenTest {
  @Test
  void everyTopicHasATitleAndAtLeastOneCompleteLine() {
    for (String topic : ControlsScreen.TOPICS) {
      assertTrue(I18n.has("controls." + topic + ".title"), topic + " has no title");
      assertFalse(ControlsScreen.lines(topic).isEmpty(), topic + " lists no controls");
      for (int i = 1; I18n.has("controls." + topic + "." + i + ".keys"); i++) {
        assertTrue(
            I18n.has("controls." + topic + "." + i + ".action"),
            "controls." + topic + "." + i + " has keys but no action");
      }
    }
  }
}
