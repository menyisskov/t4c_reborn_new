package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class RiptideSurge {
  private RiptideSurge() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.riptide_surge}",
        "${spell.description.riptide_surge}",
        "10",
        0,
        110,
        35,
        45,
        true,
        true,
        "64kSpellIconWaterAttackSingle",
        "IceShard",
        "IceCloud-",
        0,
        0,
        "Small Projectile.wav",
        "Ice Cloud.wav",
        0,
        "0",
        "0",
        68000,
        null,
        99910,
        4,
        11,
        1,
        "100",
        "1000+if((900-(self.level-45)*20)>=0?(900-(self.level-45)*20):0)",
        "750+if((820-(self.level-45)*20)>=0?(820-(self.level-45)*20):0)",
        "750+if((820-(self.level-45)*20)>=0?(820-(self.level-45)*20):0)",
        30023,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d44+48+self.int/13)*self.water/target.r_water)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
