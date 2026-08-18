package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class HealCritical {
  private HealCritical() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.heal_critical}",
        "${spell.description.heal_critical}",
        "7",
        0,
        17,
        55,
        27,
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
        110825,
        null,
        10034,
        5,
        3,
        2,
        "100",
        "1000+if((770-(self.level-27)*20)>=0?(770-(self.level-27)*20):0)",
        "750+if((770-(self.level-27)*20)>=0?(770-(self.level-27)*20):0)",
        "750+if((770-(self.level-27)*20)>=0?(770-(self.level-27)*20):0)",
        30096,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "(1d6+37+self.wis/14)*self.light/100)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
