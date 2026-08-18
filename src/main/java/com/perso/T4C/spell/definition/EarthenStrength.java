package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class EarthenStrength {
  private EarthenStrength() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.earthen_strength}",
        "${spell.description.earthen_strength}",
        "30",
        0,
        29,
        43,
        23,
        false,
        true,
        "64kSpellIconEarthBoost",
        "64kSpellEnergyBallGreen-",
        "RockyFly-",
        0,
        0,
        "Healing.wav",
        "Rocks Fly.wav",
        0,
        "300000",
        "0",
        26329,
        null,
        10124,
        2,
        3,
        1,
        "100",
        "1000+if((730-(self.level-23)*20)>=0?(730-(self.level-23)*20):0)",
        "750+if((730-(self.level-23)*20)>=0?(730-(self.level-23)*20):0)",
        "750+if((730-(self.level-23)*20)>=0?(730-(self.level-23)*20):0)",
        30113,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "TRUE"),
                    new SpellData.T4cEffect.EffectParam(2, "strength"),
                    new SpellData.T4cEffect.EffectParam(3, "(10+self.wis/15+self.int/20)")))));
  }
}
