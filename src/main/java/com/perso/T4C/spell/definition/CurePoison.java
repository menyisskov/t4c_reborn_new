package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class CurePoison {
  private CurePoison() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.cure_poison}",
        "${spell.description.cure_poison}",
        "2", 0, 26, 17, 5,
        false, true, "64kSpellIconWaterDefense",
        "64kSpellEnergyBallBlue-", "HealingSpell-", 0, 0,
        "Healing.wav", "Healing.wav", 0, "0", "0", 1825,
        null, 10236, 4, 4, 1,
        "100", "1000+if((550-(self.level-5)*20)>=0?(550-(self.level-5)*20):0)", "750+if((550-(self.level-5)*20)>=0?(550-(self.level-5)*20):0)", "750+if((550-(self.level-5)*20)>=0?(550-(self.level-5)*20):0)",
        30095, 0, false, List.of(new SpellData.T4cEffect(13, List.of(new SpellData.T4cEffect.EffectParam(1, "10020"), new SpellData.T4cEffect.EffectParam(2, "75"))), new SpellData.T4cEffect(13, List.of(new SpellData.T4cEffect.EffectParam(1, "10344"), new SpellData.T4cEffect.EffectParam(2, "75"))), new SpellData.T4cEffect(13, List.of(new SpellData.T4cEffect.EffectParam(1, "10649"), new SpellData.T4cEffect.EffectParam(2, "50")))));
  }
}
