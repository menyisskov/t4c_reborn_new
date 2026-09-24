package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemCriticalHealingPotion {
  private ItemItemCriticalHealingPotion() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.critical_healing_potion",
        "${item.critical_healing_potion}",
        null,
        null,
        null,
        null,
        "64kInvPotion 2",
        25000L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40621,
        5,
        241,
        null,
        "0",
        0,
        1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        // T4C-0036: was 10208, a spell id nothing in SpellRegistry actually registers (dead
        // legacy macro id, see OriginalNpcScriptMacros.__SPELL_ITEM_POTION_OF_CRITICAL_HEALING) -
        // ItemUseService.useOnSelf silently no-ops (Failure.NO_EFFECT) when findById() misses, so
        // this potion did nothing when drunk. 10034 is spell/definition/HealCritical.java's real
        // registered id ("Critical Heal"), which is what this item's name and price were always
        // meant to trigger.
        List.of(new ItemDefinition.ItemSpell(10034, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
