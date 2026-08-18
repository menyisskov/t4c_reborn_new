package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DiceFormulaTest {
  @Test
  void preservesSignedSpellDamageAndElementalResistance() {
    DiceFormula.Context context =
        new DiceFormula.Context(
            10, 10, 10, 50, 10, 50, 10, 20, 0, 200, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100);
    int value = DiceFormula.of("-((20+self.int/5)*self.fire/target.r_fire)").evaluate(context);
    assertEquals(-15, value);
  }
}
