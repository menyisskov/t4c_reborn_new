package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Protection {
  private Protection() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.protection}",
        "${spell.description.protection}",
        "9",
        0,
        20,
        25,
        8,
        false,
        true,
        "64kSpellIconEarthDefense",
        "64kSpellEnergyBallGreen-",
        "GreenWipe-",
        0,
        0,
        "Healing.wav",
        "Mind Shield.wav",
        0,
        "1200000",
        "0",
        3712,
        null,
        10115,
        2,
        3,
        1,
        "100",
        "1000+if((580-(self.level-8)*20)>=0?(580-(self.level-8)*20):0)",
        "750+if((580-(self.level-8)*20)>=0?(580-(self.level-8)*20):0)",
        "750+if((580-(self.level-8)*20)>=0?(580-(self.level-8)*20):0)",
        30092,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "TRUE"),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(3, "(3+self.int/100+self.wis/50)")))));
  }
}
