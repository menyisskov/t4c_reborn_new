package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class LesserDrain {
  private LesserDrain() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.lesser_drain}",
        "${spell.description.lesser_drain}",
        "3",
        0,
        50,
        15,
        12,
        true,
        true,
        "64kSpellIconDarkDrainSingle",
        "64kSpellEnergyBallBlack-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "0",
        "0",
        7152,
        null,
        10099,
        6,
        11,
        2,
        "100",
        "1000+if((620-(self.level-12)*20)>=0?(620-(self.level-12)*20):0)",
        "750+if((620-(self.level-12)*20)>=0?(620-(self.level-12)*20):0)",
        "750+if((620-(self.level-12)*20)>=0?(620-(self.level-12)*20):0)",
        30062,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                10,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-( ( 1d21 + 13 + self.int/18 ) * self.dark/target.r_dark )")))));
  }
}
