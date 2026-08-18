package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpellEffectManagerBuffTest {
  @Test
  void manaShieldCreatesVisibleBuffAndRaisesElementalResistances() {
    SpellData manaShield =
        SpellRegistry.load().stream()
            .filter(spell -> "${spell.mana_shield}".equals(spell.getName()))
            .findFirst()
            .orElseThrow();
    Player player = new Player();
    SpellEffectManager manager = new SpellEffectManager();
    List<SpellData.SpellEffect> effects = manager.resolvePlayerBuffEffects(manaShield, player);
    assertFalse(effects.isEmpty());
    assertEquals(5, effects.size());
    assertNotNull(manaShield.getIconId());
    player.applyBuff(
        manaShield.getName(),
        manaShield.getDescription(),
        manaShield.getIconId(),
        manager.resolveDurationSeconds(manaShield, player),
        false,
        effects);
    assertEquals(1, player.getActiveBuffs().size());
    assertEquals(133, player.getElementResistance("fire"));
    assertEquals(133, player.getElementResistance("dark"));
  }

  @Test
  void elementalPowerAndRadianceSpellsAlsoProduceVisibleBuffEffects() {
    List<SpellData> spells = SpellRegistry.load();
    Player player = new Player();
    SpellEffectManager manager = new SpellEffectManager();
    for (String spellName : List.of("${spell.mana_surge}", "${spell.light}")) {
      SpellData spell =
          spells.stream()
              .filter(value -> spellName.equals(value.getName()))
              .findFirst()
              .orElseThrow();
      assertFalse(manager.resolvePlayerBuffEffects(spell, player).isEmpty(), spellName);
    }
  }

  @Test
  void everyKnownT4cAttributeBoostProducesAVisibleEffect() throws Exception {
    Player player = new Player();
    SpellEffectManager manager = new SpellEffectManager();
    for (SpellData spell : SpellRegistry.load()) {
      boolean containsAttributeBoost =
          spell.getT4cEffects().stream()
              .anyMatch(effect -> effect != null && effect.getEffectType() == 2);
      if (containsAttributeBoost && manager.resolveDurationSeconds(spell, player) > 0) {
        assertFalse(manager.resolvePlayerBuffEffects(spell, player).isEmpty(), spell.getName());
      }
    }
  }

  @Test
  void instantaneousManaRestoreProducesNoBuff() {
    SpellData potion =
        SpellRegistry.load().stream()
            .filter(spell -> "${spell.item_potion_of_mana}".equals(spell.getName()))
            .findFirst()
            .orElseThrow();
    Player player = new Player();
    SpellEffectManager manager = new SpellEffectManager();
    assertEquals(0, manager.resolveDurationSeconds(potion, player));
    assertTrue(manager.resolvePlayerBuffEffects(potion, player).isEmpty());
    assertEquals(25, manager.resolvePlayerManaDelta(potion, player));
  }

  @Test
  void timedManaEffectRemainsAVisibleBuff() {
    SpellData manabane =
        SpellRegistry.load().stream()
            .filter(spell -> "${spell.mob_manabane_spell}".equals(spell.getName()))
            .findFirst()
            .orElseThrow();
    Player player = new Player();
    SpellEffectManager manager = new SpellEffectManager();
    assertFalse(manager.resolvePlayerBuffEffects(manabane, player).isEmpty());
  }
}
