package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Barrier {
  private Barrier() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.barrier}",
        "${spell.description.barrier}",
        "10",
        0,
        59,
        24,
        21,
        false,
        true,
        "64kSpellIconWaterDefense",
        "64kSpellEnergyBallBlue-",
        "BlueWipe-",
        0,
        0,
        "Healing.wav",
        "Mind Shield.wav",
        0,
        "120000",
        "0",
        18753,
        null,
        10123,
        4,
        3,
        1,
        "100",
        "1000+if((710-(self.level-21)*20)>=0?(710-(self.level-21)*20):0)",
        "750+if((710-(self.level-21)*20)>=0?(710-(self.level-21)*20):0)",
        "750+if((710-(self.level-21)*20)>=0?(710-(self.level-21)*20):0)",
        30052,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "TRUE"),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(3, "(5+self.int/50+self.wis/25)")))));
  }
}
