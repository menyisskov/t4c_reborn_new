package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DrownedWard {
  private DrownedWard() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.drowned_ward}",
        "${spell.description.drowned_ward}",
        "55",
        0,
        22,
        80,
        48,
        false,
        true,
        "64kSpellIconWaterDefense",
        "64kSpellEnergyBallBlue-",
        null,
        0,
        0,
        "Healing.wav",
        null,
        0,
        "1800000",
        "0",
        93000,
        null,
        99911,
        4,
        3,
        2,
        "100",
        "1000+if((950-(self.level-48)*20)>=0?(950-(self.level-48)*20):0)",
        "750+if((870-(self.level-48)*20)>=0?(870-(self.level-48)*20):0)",
        "750+if((870-(self.level-48)*20)>=0?(870-(self.level-48)*20):0)",
        30085,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(3, "20+self.wis/8"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_dark"),
                    new SpellData.T4cEffect.EffectParam(3, "12+self.wis/12"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(3, "8+self.wis/20")))));
  }
}
