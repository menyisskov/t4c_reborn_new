package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MassHealing {
  private MassHealing() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mass_healing}",
        "${spell.description.mass_healing}",
        "15",
        5,
        19,
        108,
        63,
        false,
        true,
        "64kSpellIconLightHealArea",
        "64kSpellEnergyBallWhite-",
        "HealSerious-",
        0,
        0,
        "Healing.wav",
        "Healing.wav",
        0,
        "0",
        "0",
        143966,
        null,
        10138,
        5,
        15,
        2,
        "100",
        "2*(1000+if((1130-(self.level-63)*20)>=0?(1130-(self.level-63)*20):0))",
        "2*(750+if((1130-(self.level-63)*20)>=0?(1130-(self.level-63)*20):0))",
        "2*(750+if((1130-(self.level-63)*20)>=0?(1130-(self.level-63)*20):0))",
        30098,
        30096,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "(1d7+81+self.wis/9)*self.light/100)"),
                    new SpellData.T4cEffect.EffectParam(2, "(1d7+81+self.wis/9)*self.light/100)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
