package com.perso.T4C.monster.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import org.junit.jupiter.api.Test;

class MonsterSpellDamageRulesTest {
  @Test
  void preservesLegacyRollsAndRandomBounds() {
    var random = new MaximumRollRandom();

    assertEquals(20, MonsterSpellDamageRules.rollDamage(10091, 26, 10, random));
    assertEquals(10, random.lastBound);
    assertEquals(22, MonsterSpellDamageRules.rollDamage(10120, 30, 10, random));
    assertEquals(15, random.lastBound);
    assertEquals(19, MonsterSpellDamageRules.rollDamage(99999, 1, 10, random));
    assertEquals(10, random.lastBound);
  }

  @Test
  void clampsLegacyMinimumInputsWithoutChangingMinimumDamage() {
    var random = new MaximumRollRandom();

    assertEquals(1, MonsterSpellDamageRules.rollDamage(10091, 15, 0, random));
    assertEquals(1, random.lastBound);
    assertEquals(1, MonsterSpellDamageRules.rollDamage(10120, 0, -4, random));
    assertEquals(1, random.lastBound);
    assertEquals(1, MonsterSpellDamageRules.rollDamage(99999, 0, 0, random));
  }

  @Test
  void elementalBalanceAndResistanceKeepLegacyIntegerRounding() {
    assertEquals(5, MonsterSpellDamageRules.balanceElementalDamage(10, 20));
    assertEquals(5, MonsterSpellDamageRules.balanceElementalDamage(10, 100));
    assertEquals(2, MonsterSpellDamageRules.damageAfterResistance(5, 200));
    assertEquals(1800, MonsterSpellDamageRules.balanceElementalDamage(10095, 1, 2000, "Arch Drake"));
    assertEquals(5, MonsterSpellDamageRules.balanceElementalDamage(10095, 10, 2000, "Other monster"));
  }

  private static final class MaximumRollRandom extends Random {
    private int lastBound;

    @Override
    public int nextInt(int bound) {
      lastBound = bound;
      return bound - 1;
    }
  }
}
