package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class HealSerious {
  private HealSerious() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.heal_serious}",
        "${spell.description.heal_serious}",
        "4",
        0,
        16,
        34,
        13,
        false,
        true,
        "64kSpellIconLightHealSingle",
        "64kSpellEnergyBallWhite-",
        "HealingSpell-",
        0,
        0,
        "Healing.wav",
        "Healing.wav",
        0,
        "0",
        "0",
        8177,
        null,
        10033,
        5,
        3,
        2,
        "100",
        "1000+if((630-(self.level-13)*20)>=0?(630-(self.level-13)*20):0)",
        "750+if((630-(self.level-13)*20)>=0?(630-(self.level-13)*20):0)",
        "750+if((630-(self.level-13)*20)>=0?(630-(self.level-13)*20):0)",
        30096,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "(1d5+20+self.wis/18)*self.light/100)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
