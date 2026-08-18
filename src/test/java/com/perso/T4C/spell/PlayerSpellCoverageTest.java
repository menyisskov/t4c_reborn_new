package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import com.perso.T4C.render.SpellRenderer;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class PlayerSpellCoverageTest {
  private static final Set<Integer> SUPPORTED_EFFECT_TYPES =
      Set.of(1, 2, 3, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17);

  @Test
  void everyPlayerSpellUsesOnlyImplementedEffectFamilies() {
    Set<Integer> unsupported = new TreeSet<>();
    for (SpellData spell : SpellRegistry.playerCastableSpells()) {
      for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
        if (effect != null && !SUPPORTED_EFFECT_TYPES.contains(effect.getEffectType())) {
          unsupported.add(effect.getEffectType());
        }
      }
    }
    assertTrue(unsupported.isEmpty(), "Unsupported player spell effect types: " + unsupported);
  }

  @Test
  void vaporizeIsRecognizedAsDestructive() {
    SpellData vaporize = SpellRegistry.findById(10210);
    assertTrue(new SpellEffectManager().hasVaporizeEffect(vaporize));
  }

  @Test
  void wordOfRecallUsesThePlayersRespawnLocation() {
    Player player = new Player();
    SpellEffectManager.PlayerUtility utility =
        new SpellEffectManager().applyPlayerUtilityEffects(SpellRegistry.findById(10029), player);
    assertEquals(player.resolveRespawnWorldZ(), utility.teleportWorldZ());
  }

  @Test
  void detectionSpellsEnableTheirRuntimeFlags() {
    Player player = new Player();
    SpellEffectManager manager = new SpellEffectManager();
    manager.applyPlayerUtilityEffects(SpellRegistry.findById(10258), player);
    manager.applyPlayerUtilityEffects(SpellRegistry.findById(10259), player);
    assertTrue(player.canDetectInvisible());
    assertTrue(player.canDetectHidden());
  }

  @Test
  void levelUpSpellCarriesTheAnimationAndIsLearnable() {
    SpellData spell = SpellRegistry.findByName("spell.level_up");
    assertNotNull(spell);
    assertEquals(SpellRenderer.LEVEL_UP_EFFECT, spell.getImpactSpell());
    assertEquals(0, Integer.parseInt(spell.getManaCost()), "LevelUp is free to cast");
    assertEquals(5, spell.getTargetType(), "LevelUp is cast on the caster itself");
    assertFalse(spell.isAttack());
    assertTrue(
        SpellRegistry.playerCastableSpells().stream()
            .anyMatch(candidate -> "spell.level_up".equals(candidate.getKey())),
        "LevelUp must be part of the learnable catalogue");
  }

  @Test
  void levelUpSpellBoostsEveryCoreAttributeByTenPercent() {
    SpellData spell = SpellRegistry.findByName("spell.level_up");
    assertNotNull(spell);
    Set<String> boostedAttributes = new TreeSet<>();
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect == null || effect.getEffectType() != 2) continue;
      String attribute = null;
      String amount = null;
      for (SpellData.T4cEffect.EffectParam param : effect.getParameters()) {
        if (param == null) continue;
        if (param.getParamId() == 2) attribute = param.getExpression();
        if (param.getParamId() == 3) amount = param.getExpression();
      }
      assertNotNull(attribute);
      assertNotNull(amount);
      assertTrue(
          amount.endsWith("/10"), "Expected a 10% formula for " + attribute + ", got " + amount);
      boostedAttributes.add(attribute);
    }
    assertEquals(
        Set.of("strength", "agility", "endurance", "intelligence", "wisdom"), boostedAttributes);
  }

  @Test
  void levelUpSpellDescriptionAnnouncesTheLevelGain() {
    SpellData spell = SpellRegistry.findByName("spell.level_up");
    assertNotNull(spell);
    String description = com.perso.T4C.i18n.I18n.resolve(spell.getDescription());
    assertTrue(
        description.startsWith("Gain de Niveau:"),
        "LevelUp's description must start with \"Gain de Niveau:\", was: " + description);
  }
}
