package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.spell.SpellRegistry;
import org.junit.jupiter.api.Test;

/**
 * T4C-0036: item/definition/ItemItemCriticalHealingPotion.java pointed its {@link
 * ItemDefinition.ItemSpell} at 10208, a legacy macro id nothing in {@link SpellRegistry} registers
 * (see OriginalNpcScriptMacros.__SPELL_ITEM_POTION_OF_CRITICAL_HEALING) - so
 * ItemUseService.useOnSelf silently no-oped (Failure.NO_EFFECT) whenever a player drank it. Fixed
 * to 10034, spell/definition/HealCritical.java's real id ("Critical Heal"). This same
 * dangling-spell-id pattern turned out to affect ~80 other pre-existing legacy items when checked
 * registry-wide (mana_elixir, serious_healing_potion, scroll_of_recall, several rings/weapons,
 * etc.) - a real, systemic, pre-existing gap, but far beyond this pass's scope to fix wholesale
 * without researching each item's intended effect individually; recorded as a known gap in
 * DESIGN_GUIDELINES.md instead of guarded here. This test only locks down the one item this pass
 * touched and made purchasable.
 */
class ItemSpellIntegrityTest {
  @Test
  void protectionGemRequiresItsBeltAndAppliesThirtyMinutesThroughNormalItemUse() {
    var player = new com.perso.T4C.player.Player();
    player.setLevel(279);
    player.setMaxMana(10000);
    player.setMana(10000);
    player.getInventory().add("item.gem_of_unstable_protection");
    var macro = com.perso.T4C.config.MacroBinding.forItem("gem_of_unstable_protection");
    macro.setKeycode(com.badlogic.gdx.Input.Keys.R);
    macro.setModifiers(
        com.perso.T4C.config.MacroBinding.MOD_CTRL | com.perso.T4C.config.MacroBinding.MOD_SHIFT);
    player.getMacros().add(macro);
    var active =
        com.perso.T4C.input.GameInputHandler.resolveMacro(
            player, macro.getKeycode(), macro.getModifiers(), true, false);
    org.junit.jupiter.api.Assertions.assertSame(
        macro, active, "The protection item must be usable while inspecting the character sheet");
    var random = new java.util.Random(1);
    org.junit.jupiter.api.Assertions.assertFalse(
        ItemUseService.useOnSelf(player, "gem_of_unstable_protection", random).success());
    player
        .getEquippedItems()
        .put(com.perso.T4C.player.BodyPart.BELT, "item.belt_of_unstable_protection");
    double originalAc = com.perso.T4C.combat.ArmorClassRules.effectiveArmorClass(player);
    org.junit.jupiter.api.Assertions.assertTrue(
        ItemUseService.useOnSelf(player, active.getItemKey(), random).success());
    org.junit.jupiter.api.Assertions.assertEquals(
        1800000L, player.getActiveBuffs().getFirst().getDurationMillis());
    int rolledAc =
        Integer.parseInt(
            player.getActiveBuffs().getFirst().getEffects().stream()
                .filter(effect -> "armorClass".equals(effect.getAttribute()))
                .findFirst()
                .orElseThrow()
                .getAmount());
    org.junit.jupiter.api.Assertions.assertTrue(rolledAc >= Math.round(player.getLevel() * 0.8));
    org.junit.jupiter.api.Assertions.assertTrue(
        rolledAc <= player.getLevel(),
        "Unstable protection's random bonus is bounded by the player's level");
    org.junit.jupiter.api.Assertions.assertEquals(
        originalAc + rolledAc, com.perso.T4C.combat.ArmorClassRules.effectiveArmorClass(player));
    org.junit.jupiter.api.Assertions.assertEquals(10000 - player.getLevel(), player.getMana());
    var protection = player.getActiveBuffs().getFirst();
    player.setMana(player.getLevel() - 1);
    var failed = ItemUseService.useOnSelf(player, active.getItemKey(), random);
    org.junit.jupiter.api.Assertions.assertFalse(failed.success());
    org.junit.jupiter.api.Assertions.assertEquals(
        com.perso.T4C.spell.SpellCastingService.Failure.NOT_ENOUGH_MANA, failed.castFailure());
    org.junit.jupiter.api.Assertions.assertEquals(
        com.perso.T4C.i18n.I18n.message("message.not_enough_mana"),
        ItemUseService.failureMessage(failed, active.getItemKey()));
    org.junit.jupiter.api.Assertions.assertSame(protection, player.getActiveBuffs().getFirst());
    org.junit.jupiter.api.Assertions.assertEquals(player.getLevel() - 1, player.getMana());
    player.setMana(player.getLevel());
    org.junit.jupiter.api.Assertions.assertTrue(
        ItemUseService.useOnSelf(player, active.getItemKey(), random).success());
    org.junit.jupiter.api.Assertions.assertEquals(0, player.getMana());
    org.junit.jupiter.api.Assertions.assertEquals(1, player.getActiveBuffs().size());
    org.junit.jupiter.api.Assertions.assertEquals(
        1, InventoryService.count(player, "gem_of_unstable_protection"));
    player.getInventory().clear();
    org.junit.jupiter.api.Assertions.assertEquals(
        ItemUseService.Failure.NOT_OWNED,
        ItemUseService.useOnSelf(player, "gem_of_unstable_protection", random).failure());
  }

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.CsvSource({"199,1,199", "200,160,200", "279,223,279"})
  void protectionRollHasInclusiveBoundsAndNarrowsAtLevel200(int level, int minimum, int maximum) {
    var expression =
        com
            .perso
            .T4C
            .spell
            .definition
            .ItemBeltOfUnstableProtection
            .definition()
            .getT4cEffects()
            .getFirst()
            .getParameters()
            .stream()
            .filter(parameter -> parameter.getParamId() == 3)
            .findFirst()
            .orElseThrow()
            .getExpression();
    var formula = com.perso.T4C.helper.DiceFormula.of(expression);
    var context = new com.perso.T4C.helper.DiceFormula.Context(0, 0, 0, 0, 0, 0, 0, level);
    org.junit.jupiter.api.Assertions.assertEquals(minimum, formula.min(context));
    org.junit.jupiter.api.Assertions.assertEquals(maximum, formula.max(context));
    int rolled = formula.roll(context);
    org.junit.jupiter.api.Assertions.assertTrue(rolled >= minimum && rolled <= maximum);
  }

  @Test
  void criticalHealingPotionResolvesToARealSpell() {
    ItemDefinition def = ItemRegistry.findByKey("item.critical_healing_potion");
    assertNotNull(def, "item.critical_healing_potion should be registered");
    assertNotNull(
        SpellRegistry.findById(def.getSpells().get(0).getSpellId()),
        "critical_healing_potion's spell id should resolve to a registered spell");
  }
}
