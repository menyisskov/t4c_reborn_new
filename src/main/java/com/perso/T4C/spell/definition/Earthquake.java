package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Earthquake {
  private Earthquake() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.earthquake}",
        "${spell.description.earthquake}",
        "11",
        7,
        43,
        71,
        47,
        true,
        false,
        "64kSpellIconEarthAttackArea",
        "64kSpellEnergyBallGreen-",
        "Pentacle-",
        0,
        0,
        "Healing.wav",
        "Vampire Dying.wav",
        0,
        "0",
        "0",
        88576,
        null,
        10082,
        2,
        18,
        1,
        "100",
        "2*(1000+if((970-(self.level-47)*20)>=0?(970-(self.level-47)*20):0))",
        "2*(750+if((970-(self.level-47)*20)>=0?(970-(self.level-47)*20):0))",
        "2*(750+if((970-(self.level-47)*20)>=0?(970-(self.level-47)*20):0))",
        30106,
        30056,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "0"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d29+72+self.wis/11)*self.earth/target.r_earth)*(20-r)/20)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
