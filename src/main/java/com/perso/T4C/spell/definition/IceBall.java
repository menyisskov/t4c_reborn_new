package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class IceBall {
  private IceBall() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ice_ball}",
        "${spell.description.ice_ball}",
        "11",
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
        82378,
        null,
        10077,
        4,
        11,
        1,
        "100",
        "1000+if((950-(self.level-45)*20)>=0?(950-(self.level-45)*20):0)",
        "750+if((950-(self.level-45)*20)>=0?(950-(self.level-45)*20):0)",
        "750+if((950-(self.level-45)*20)>=0?(950-(self.level-45)*20):0)",
        30023,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d61+53+self.int/11)*self.water/target.r_water)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
