package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class TurnUndead {
  private TurnUndead() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.turn_undead}",
        "${spell.description.turn_undead}",
        "5", 0, 16, 43, 19,
        true, true, "64kSpellIconLightAttackSingle",
        "64kSpellEnergyBallWhite-", "HealingSpell-", 0, 0,
        "Healing.wav", "Healing.wav", 0, "0", "0", 18843,
        null, 10145, 5, 11, 2,
        "100", "1000+if((690-(self.level-19)*20)>=0?(690-(self.level-19)*20):0)", "750+if((690-(self.level-19)*20)>=0?(690-(self.level-19)*20):0)", "750+if((690-(self.level-19)*20)>=0?(690-(self.level-19)*20):0)",
        30096, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-(((1d6+27+self.wis/16)*self.light/target.r_light)*self.wis/(20+2*self.level))"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
