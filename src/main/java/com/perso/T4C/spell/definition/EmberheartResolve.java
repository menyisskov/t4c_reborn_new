package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class EmberheartResolve {
  private EmberheartResolve() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.emberheart_resolve}",
        "${spell.description.emberheart_resolve}",
        "90",
        0,
        20,
        100,
        66,
        false,
        true,
        "64kSpellIconFireDefense",
        "64kSpellFireBall",
        null,
        0,
        0,
        "Healing.wav",
        null,
        0,
        "1800000",
        "0",
        140000,
        null,
        99913,
        1,
        3,
        1,
        "100",
        "1000+if((1150-(self.level-66)*20)>=0?(1150-(self.level-66)*20):0)",
        "750+if((1000-(self.level-66)*20)>=0?(1000-(self.level-66)*20):0)",
        "750+if((1000-(self.level-66)*20)>=0?(1000-(self.level-66)*20):0)",
        30124,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_fire"),
                    new SpellData.T4cEffect.EffectParam(3, "24+self.wis/6"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "max hp"),
                    new SpellData.T4cEffect.EffectParam(3, "1d(self.wis/6)+self.wis/2"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(3, "6+self.wis/25")))));
  }
}
