package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Bless {
  private Bless() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.bless}",
        "${spell.description.bless}",
        "140",
        0,
        19,
        102,
        59,
        false,
        true,
        "64kSpellIconLightBoost",
        "64kSpellEnergyBallWhite-",
        "64kSpellBless-",
        0,
        0,
        "Healing.wav",
        "Healing.wav",
        0,
        "1800000",
        "0",
        126673,
        null,
        10267,
        5,
        3,
        1,
        "100",
        "1000+if((1090-(self.level-59)*20)>=0?(1090-(self.level-59)*20):0)",
        "750+if((1090-(self.level-59)*20)>=0?(1090-(self.level-59)*20):0)",
        "750+if((1090-(self.level-59)*20)>=0?(1090-(self.level-59)*20):0)",
        30051,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "max hp"),
                    new SpellData.T4cEffect.EffectParam(3, "1d(self.wis/4)+self.wis"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(3, "self.wis/2"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "skill 35"),
                    new SpellData.T4cEffect.EffectParam(3, "self.wis/2")))));
  }
}
