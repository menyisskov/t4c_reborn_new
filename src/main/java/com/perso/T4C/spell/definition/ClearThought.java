package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ClearThought {
  private ClearThought() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.clear_thought}",
        "${spell.description.clear_thought}",
        "21",
        0,
        74,
        28,
        28,
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
        "180000",
        "0",
        36960,
        null,
        10149,
        4,
        5,
        1,
        "100",
        "1000+if((780-(self.level-28)*20)>=0?(780-(self.level-28)*20):0)",
        "750+if((780-(self.level-28)*20)>=0?(780-(self.level-28)*20):0)",
        "750+if((780-(self.level-28)*20)>=0?(780-(self.level-28)*20):0)",
        30004,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "int"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_int/4")))));
  }
}
