package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * T4C-0060: spells with no level requirement are not spells a player can ever learn - no trainer
 * sells one, no reward grants one - so they have no business filling the spellbook, the spell
 * seller or the reference site.
 */
class UnlearnableSpellsTest {
  @Test
  void noSpellWithoutALevelRequirementIsOfferedToPlayers() {
    List<SpellData> offered = SpellRegistry.playerCastableSpells();
    assertFalse(offered.isEmpty());
    for (SpellData spell : offered) {
      assertTrue(
          spell.getMinLevel() > 0, () -> "Unlearnable spell offered to players: " + spell.getKey());
    }
  }

  @Test
  void theOldNoRequirementLeftoversAreGone() {
    for (String key :
        List.of(
            "spell.chaos_shield",
            "spell.essence_of_drake",
            "spell.tetrashock",
            "spell.vaporize",
            "spell.drake_s_blades_of_vengeance",
            "spell.lighthaven_improved_gateway",
            "spell.wrath_of_marc",
            "spell.avalon_gateway")) {
      assertFalse(
          SpellRegistry.playerCastableSpells().stream()
              .anyMatch(spell -> key.equals(spell.getKey())),
          () -> key + " should no longer be offered to players");
      // Still in the registry, because items and scripts cast some of these by direct lookup -
      // the Scroll of Avalon casts avalon_gateway this way.
      assertNotNull(SpellRegistry.findByName(key), () -> key + " must still exist to be cast");
    }
  }

  /** Light is a real spell anyone can learn; it carries a level requirement so it survives. */
  @Test
  void lightKeepsItsPlaceInTheSpellbook() {
    SpellData light = SpellRegistry.findByName("spell.light");
    assertNotNull(light);
    assertEquals(1, light.getMinLevel());
    assertTrue(
        SpellRegistry.playerCastableSpells().stream()
            .anyMatch(spell -> "spell.light".equals(spell.getKey())),
        "Light must stay in the spellbook");
  }

  /**
   * An unresolved formula variable reads as 0 rather than failing, so a buff spell that boosts
   * nothing is the symptom of a variable the evaluator does not know. Tranquility and Clear Thought
   * were in exactly that state.
   */
  @Test
  void selfReferentialBuffFormulasResolveToRealNumbers() {
    Player player = new Player();
    player.setLevel(50);
    // Both spells scale off the caster's own stats, so the caster needs some to scale off.
    player.setWisdom(120);
    player.setIntelligence(80);
    SpellEffectManager manager = new SpellEffectManager();
    for (String key : List.of("spell.tranquility", "spell.clear_thought")) {
      SpellData spell = SpellRegistry.findByName(key);
      assertNotNull(spell, key);
      List<SpellData.SpellEffect> effects = manager.resolvePlayerBuffEffects(spell, player);
      assertFalse(effects.isEmpty(), () -> key + " produced no effects");
      for (SpellData.SpellEffect effect : effects) {
        assertTrue(
            Integer.parseInt(effect.getAmount()) > 0,
            () -> key + " boosts " + effect.getAttribute() + " by " + effect.getAmount());
      }
    }
  }
}
