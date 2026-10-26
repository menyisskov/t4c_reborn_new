package com.perso.T4C.spell;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;

/** Spell tier metadata is not a minimum character level. */
public final class SpellLearningRules {
  private SpellLearningRules() {}

  public static String attributeBlockReason(Player player, SpellData spell) {
    if (player.getIntelligence() < spell.getMinInt())
      return I18n.message("message.learn_need_intelligence", spell.getMinInt());
    if (player.getWisdom() < spell.getMinWis())
      return I18n.message("message.learn_need_wisdom", spell.getMinWis());
    return null;
  }
}
