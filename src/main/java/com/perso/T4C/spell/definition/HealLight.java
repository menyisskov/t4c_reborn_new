package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class HealLight {
  private HealLight() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.heal_light}",
        "${spell.description.heal_light}",
        "2", 0, 15, 19, 3,
        false, true, "64kSpellIconLightHealSingle",
        "64kSpellEnergyBallWhite-", "HealingSpell-", 0, 0,
        "Healing.wav", "Healing.wav", 0, "0", "0", 897,
        null, 10032, 5, 3, 2,
        "100", "1000+if((530-(self.level-3)*20)>=0?(530-(self.level-3)*20):0)", "750+if((530-(self.level-3)*20)>=0?(530-(self.level-3)*20):0)", "750+if((530-(self.level-3)*20)>=0?(530-(self.level-3)*20):0)",
        30096, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "(1d5+8+self.wis/23)*self.light/100)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
