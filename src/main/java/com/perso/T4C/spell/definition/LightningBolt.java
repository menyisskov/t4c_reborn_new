package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class LightningBolt {
  private LightningBolt() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.lightning_bolt}",
        "${spell.description.lightning_bolt}",
        "4",
        0,
        30,
        30,
        15,
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
        12348,
        null,
        10028,
        3,
        11,
        2,
        "100",
        "1000+if((650-(self.level-15)*20)>=0?(650-(self.level-15)*20):0)",
        "750+if((650-(self.level-15)*20)>=0?(650-(self.level-15)*20):0)",
        "750+if((650-(self.level-15)*20)>=0?(650-(self.level-15)*20):0)",
        30002,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d26+12+self.int/36+self.wis/36)*self.air/target.r_air)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
