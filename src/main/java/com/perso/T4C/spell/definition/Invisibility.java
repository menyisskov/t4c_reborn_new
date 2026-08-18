package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Invisibility {
  private Invisibility() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.invisibility}",
        "${spell.description.invisibility}",
        "70",
        0,
        74,
        72,
        58,
        false,
        true,
        "64kSpellIconAirDefense",
        "64kSpellEnergyBallYellow-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "300000",
        "0",
        122612,
        null,
        10257,
        3,
        3,
        1,
        "100",
        "1000+if((1080-(self.level-58)*20)>=0?(1080-(self.level-58)*20):0)",
        "750+if((1080-(self.level-58)*20)>=0?(1080-(self.level-58)*20):0)",
        "750+if((1080-(self.level-58)*20)>=0?(1080-(self.level-58)*20):0)",
        30059,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                15,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, "30015")))));
  }
}
