package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.helper.DiceFormula;
import org.junit.jupiter.api.Test;

/** T4C-0079 (owner's call): Renew Armor's mana cost is the sum of the mana costs of all ten
 * spells it can recast (Barrier/Protection/Stone Skin/Mana Shield/Mana Surge, base and Ultra tiers
 * alike), and it costs 20,000,000 gold to learn. */
class RenewArmorCostTest {
  @Test
  void manaCostIsTheSumOfEveryRecastSpellsManaCost() {
    SpellData renewArmor = SpellRegistry.findByName("${spell.renew_armor}");
    assertNotNull(renewArmor);
    int expected =
        sumManaCost("${spell.barrier}")
            + sumManaCost("${spell.protection}")
            + sumManaCost("${spell.stone_skin}")
            + sumManaCost("${spell.mana_shield}")
            + sumManaCost("${spell.mana_surge}")
            + sumManaCost("${spell.ultra_barrier}")
            + sumManaCost("${spell.ultra_protection}")
            + sumManaCost("${spell.ultra_stone_skin}")
            + sumManaCost("${spell.ultra_mana_shield}")
            + sumManaCost("${spell.ultra_mana_surge}");
    assertEquals(
        expected, DiceFormula.of(renewArmor.getManaCost()).evaluate(DiceFormula.Context.ZERO));
  }

  @Test
  void costsTwentyMillionGoldToLearn() {
    SpellData renewArmor = SpellRegistry.findByName("${spell.renew_armor}");
    assertNotNull(renewArmor);
    assertEquals(20_000_000, renewArmor.getPrice());
  }

  private static int sumManaCost(String key) {
    SpellData spell = SpellRegistry.findByName(key);
    assertNotNull(spell, key);
    return DiceFormula.of(spell.getManaCost()).evaluate(DiceFormula.Context.ZERO);
  }
}
