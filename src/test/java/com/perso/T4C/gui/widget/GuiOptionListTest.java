package com.perso.T4C.gui.widget;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class GuiOptionListTest {
  @Test
  void checkboxHitTargetFollowsTheResizedList() {
    AtomicBoolean enabled = new AtomicBoolean();
    float x = 40, y = 80, width = 300;
    var list =
        new GuiOptionList(
            x,
            y,
            width,
            100,
            null,
            null,
            null,
            null,
            null,
            null,
            List.of(new GuiOptionList.Entry(() -> "Setting", enabled::get, enabled::set)));
    list.onTouchDown(x + 178, y + 5);
    list.onTouchUp(x + 178, y + 5);
    assertFalse(enabled.get(), "The old checkbox position must not toggle an unrelated area.");
    list.onTouchDown(x + width - 14, y + 5);
    list.onTouchUp(x + width - 14, y + 5);
    assertTrue(enabled.get(), "The checkbox must respond at its rendered position.");
  }
}
