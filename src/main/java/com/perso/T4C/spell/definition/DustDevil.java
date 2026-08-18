package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DustDevil {
  private DustDevil() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.dust_devil}",
        "${spell.description.dust_devil}",
        "2",
        0,
        21,
        21,
        6,
        true,
        true,
        "64kSpellIconAirAttackSingle",
        "Lightning",
        "ElectricShield-",
        0,
        0,
        "Lightning.wav",
        "Electric Shield.wav",
        0,
        "0",
        "0",
        2388,
        null,
        10027,
        3,
        11,
        2,
        "100",
        "1000+if((560-(self.level-6)*20)>=0?(560-(self.level-6)*20):0)",
        "750+if((560-(self.level-6)*20)>=0?(560-(self.level-6)*20):0)",
        "750+if((560-(self.level-6)*20)>=0?(560-(self.level-6)*20):0)",
        30002,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d16+6+self.int/42+self.wis/42)*self.air/target.r_air)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
