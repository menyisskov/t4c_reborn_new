package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class IceShield {
  private IceShield() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ice_shield}",
        "${spell.description.ice_shield}",
        "100",
        0,
        191,
        53,
        84,
        false,
        false,
        "64kSpellIconWaterBoost",
        "BlueWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "300000",
        "0",
        230978,
        null,
        10812,
        4,
        5,
        1,
        "100",
        "1000+if((1340-(self.level-84)*20)>=0?(1340-(self.level-84)*20):0)",
        "750+if((1340-(self.level-84)*20)>=0?(1340-(self.level-84)*20):0)",
        "750+if((1340-(self.level-84)*20)>=0?(1340-(self.level-84)*20):0)",
        30004,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10813"),
                    new SpellData.T4cEffect.EffectParam(2, "OnHit"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "100")))));
  }
}
