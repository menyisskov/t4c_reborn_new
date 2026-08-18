package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Curse {
  private Curse() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.curse}",
        "${spell.description.curse}",
        "5",
        0,
        74,
        16,
        20,
        false,
        true,
        "64kSpellIconDarkAttackSingle",
        "64kSpellEnergyBallBlack-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "30000",
        "0",
        17200,
        null,
        10038,
        6,
        11,
        2,
        "100",
        "1000+if((700-(self.level-20)*20)>=0?(700-(self.level-20)*20):0)",
        "750+if((700-(self.level-20)*20)>=0?(700-(self.level-20)*20):0)",
        "750+if((700-(self.level-20)*20)>=0?(700-(self.level-20)*20):0)",
        30062,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "light"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.true_light*2/3)")))));
  }
}
