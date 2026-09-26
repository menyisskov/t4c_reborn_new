package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Buffs whose size is a fraction of the caster's own stat ({@code self.true_*}) used to resolve to
 * 0, because DiceFormula answers 0 for anything it cannot parse and those variables had nothing to
 * substitute. These spells must produce real numbers, and re-casting them must not compound.
 */
class SelfReferentialBuffFormulaTest {

  private static SpellData spell(String name) {
    return SpellRegistry.load().stream()
        .filter(candidate -> name.equals(candidate.getName()))
        .findFirst()
        .orElseThrow();
  }

  /**
   * A mid-game character: real stats, so a "quarter of your own X" buff has something to bite on.
   */
  private static Player caster() {
    Player player = new Player();
    player.setLevel(100);
    player.setStrength(200);
    player.setEndurance(180);
    player.setDexterity(160);
    player.setIntelligence(240);
    player.setWisdom(220);
    player.setSkillLevel("attack", 150);
    player.setSkillLevel("dodge", 120);
    return player;
  }

  private static int boost(List<SpellData.SpellEffect> effects, String attribute) {
    return effects.stream()
        .filter(effect -> attribute.equals(effect.getAttribute()))
        .mapToInt(effect -> Integer.parseInt(effect.getAmount()))
        .sum();
  }

  @Test
  void resistFireAndResistIceDoubleTheCastersOwnResistance() {
    Player player = caster();
    SpellEffectManager manager = new SpellEffectManager();
    for (Map.Entry<String, String> entry :
        Map.of("${spell.resist_fire}", "fire", "${spell.resist_ice}", "water").entrySet()) {
      SpellData buff = spell(entry.getKey());
      int before = player.getTrueElementResistance(entry.getValue());
      List<SpellData.SpellEffect> effects = manager.resolvePlayerBuffEffects(buff, player);
      assertFalse(effects.isEmpty(), entry.getKey());
      assertEquals(before, boost(effects, "resist:" + entry.getValue()), entry.getKey());
      assertTrue(before > 0, entry.getKey());
    }
  }

  @Test
  void nimblenessRaisesBothAgilityAndDodge() {
    Player player = caster();
    SpellEffectManager manager = new SpellEffectManager();
    List<SpellData.SpellEffect> effects =
        manager.resolvePlayerBuffEffects(spell("${spell.nimbleness}"), player);
    assertEquals(player.getTrueDexterity() / 4, boost(effects, "dex"));
    assertEquals(60, boost(effects, "skill:dodge"));
    assertEquals(40, boost(effects, "dex"));
    assertTrue(boost(effects, "dex") > 0);
  }

  @Test
  void tranquilityAndClearThoughtScaleWithTheCastersOwnStats() {
    Player player = caster();
    SpellEffectManager manager = new SpellEffectManager();
    assertEquals(
        player.getTrueWisdom() / 2,
        boost(manager.resolvePlayerBuffEffects(spell("${spell.tranquility}"), player), "wis"));
    assertEquals(
        player.getTrueIntelligence() / 4,
        boost(manager.resolvePlayerBuffEffects(spell("${spell.clear_thought}"), player), "int"));
    assertEquals(
        110, boost(manager.resolvePlayerBuffEffects(spell("${spell.tranquility}"), player), "wis"));
  }

  @Test
  void recastingAResistBuffRefreshesItInsteadOfCompounding() {
    Player player = caster();
    SpellEffectManager manager = new SpellEffectManager();
    SpellData resistFire = spell("${spell.resist_fire}");
    List<SpellData.SpellEffect> first = manager.resolvePlayerBuffEffects(resistFire, player);
    player.applyBuff(
        resistFire.getName(),
        resistFire.getDescription(),
        resistFire.getIconId(),
        manager.resolveDurationSeconds(resistFire, player),
        false,
        first);
    List<SpellData.SpellEffect> second = manager.resolvePlayerBuffEffects(resistFire, player);
    assertEquals(boost(first, "resist:fire"), boost(second, "resist:fire"));
  }

  @Test
  void noAttributeBoostFormulaSilentlyResolvesToZero() {
    Player player = caster();
    SpellEffectManager manager = new SpellEffectManager();
    for (SpellData buff : SpellRegistry.load()) {
      for (SpellData.T4cEffect effect : buff.getT4cEffects()) {
        if (effect == null || effect.getEffectType() != 2) continue;
        boolean selfReferential =
            effect.getParameters().stream()
                .anyMatch(
                    parameter ->
                        parameter != null
                            && parameter.getExpression() != null
                            && parameter.getExpression().contains("self.true_"));
        if (!selfReferential) continue;
        assertTrue(
            manager.resolvePlayerBuffEffects(buff, player).stream()
                .anyMatch(resolved -> Integer.parseInt(resolved.getAmount()) != 0),
            buff.getName());
      }
    }
  }
}
