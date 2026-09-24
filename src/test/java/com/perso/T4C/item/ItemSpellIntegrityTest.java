package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.spell.SpellRegistry;
import org.junit.jupiter.api.Test;

/** T4C-0036: item/definition/ItemItemCriticalHealingPotion.java pointed its {@link
 * ItemDefinition.ItemSpell} at 10208, a legacy macro id nothing in {@link SpellRegistry}
 * registers (see OriginalNpcScriptMacros.__SPELL_ITEM_POTION_OF_CRITICAL_HEALING) - so
 * ItemUseService.useOnSelf silently no-oped (Failure.NO_EFFECT) whenever a player drank it. Fixed
 * to 10034, spell/definition/HealCritical.java's real id ("Critical Heal"). This same
 * dangling-spell-id pattern turned out to affect ~80 other pre-existing legacy items when checked
 * registry-wide (mana_elixir, serious_healing_potion, scroll_of_recall, several rings/weapons,
 * etc.) - a real, systemic, pre-existing gap, but far beyond this pass's scope to fix wholesale
 * without researching each item's intended effect individually; recorded as a known gap in
 * DESIGN_GUIDELINES.md instead of guarded here. This test only locks down the one item this pass
 * touched and made purchasable. */
class ItemSpellIntegrityTest {
  @Test
  void criticalHealingPotionResolvesToARealSpell() {
    ItemDefinition def = ItemRegistry.findByKey("item.critical_healing_potion");
    assertNotNull(def, "item.critical_healing_potion should be registered");
    assertNotNull(
        SpellRegistry.findById(def.getSpells().get(0).getSpellId()),
        "critical_healing_potion's spell id should resolve to a registered spell");
  }
}
