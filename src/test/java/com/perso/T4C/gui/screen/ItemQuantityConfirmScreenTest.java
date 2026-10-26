package com.perso.T4C.gui.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ItemQuantityConfirmScreenTest {
  @Test
  void quantityStaysWithinTheOwnedStack() {
    assertEquals(12, ItemQuantityConfirmScreen.clampQuantity("12", 47));
    assertEquals(47, ItemQuantityConfirmScreen.clampQuantity("100", 47));
    assertEquals(1, ItemQuantityConfirmScreen.clampQuantity("0", 47));
    assertEquals(1, ItemQuantityConfirmScreen.clampQuantity("", 47));
  }

  @Test
  void oversizedTypedNumberCannotOverflowIntoAnInvalidQuantity() {
    assertEquals(47, ItemQuantityConfirmScreen.clampQuantity("999999999999999999999", 47));
  }
}
