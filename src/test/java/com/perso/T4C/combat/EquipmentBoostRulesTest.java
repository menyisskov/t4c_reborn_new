package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EquipmentBoostRulesTest {
  private final WeaponDamageRules.Stats base = new WeaponDamageRules.Stats(40, 30, 25, 20, 10, 3);

  @Test void baseStatFormulasAndRequirementsDoNotCompoundBonuses() {
    var bonuses = EquipmentBoostRules.bonuses(base, Map.of("attack", 200, "archery", 80), List.of(
        new EquipmentBoostRules.Boost(1, 3, "self.str/2", 10, 20),
        new EquipmentBoostRules.Boost(2, 3, "self.str/2", 0, 0),
        new EquipmentBoostRules.Boost(3, 3, "999", 11, 0),
        new EquipmentBoostRules.Boost(4, 8, "self.true_attack/10+true_skill(35)/4", 0, 0),
        new EquipmentBoostRules.Boost(5, 100, "self.true_light", 0, 0)));
    assertEquals(40, bonuses.get(3));
    assertEquals(40, bonuses.get(8));
    assertEquals(100, bonuses.get(100));
    assertThrows(UnsupportedOperationException.class, bonuses::clear);
  }

  @Test void repeatedIdsReplaceEarlierBoostsAndTotalsSaturateAfterSummation() {
    var bonuses = EquipmentBoostRules.bonuses(base, Map.of(), List.of(
        new EquipmentBoostRules.Boost(1, 3, "999", 0, 0),
        new EquipmentBoostRules.Boost(1, 8, "4", 0, 0),
        new EquipmentBoostRules.Boost(2, 8, "100", 0, 21),
        new EquipmentBoostRules.Boost(3, 10, "2147483647", 0, 0),
        new EquipmentBoostRules.Boost(4, 10, "20", 0, 0)));
    assertFalse(bonuses.containsKey(3));
    assertEquals(4, bonuses.get(8));
    assertEquals(Integer.MAX_VALUE, bonuses.get(10));
  }
}
