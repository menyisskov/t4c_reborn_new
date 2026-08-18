package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class FireShield {
  private FireShield() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.fire_shield}",
        "${spell.description.fire_shield}",
        "22",
        0,
        156,
        16,
        48,
        false,
        true,
        "64kSpellIconFireBoost",
        "RedWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "120000",
        "0",
        91738,
        null,
        10171,
        1,
        5,
        1,
        "100",
        "1000+if((980-(self.level-48)*20)>=0?(980-(self.level-48)*20):0)",
        "750+if((980-(self.level-48)*20)>=0?(980-(self.level-48)*20):0)",
        "750+if((980-(self.level-48)*20)>=0?(980-(self.level-48)*20):0)",
        30007,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10167"),
                    new SpellData.T4cEffect.EffectParam(2, "OnHit"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
