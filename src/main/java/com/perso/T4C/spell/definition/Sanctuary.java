package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Sanctuary {
  private Sanctuary() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.sanctuary}",
        "${spell.description.sanctuary}",
        "120",
        0,
        18,
        91,
        52,
        false,
        false,
        "64kSpellIconLightDefense",
        "64kSpellBless-",
        null,
        0,
        0,
        "Healing.wav",
        null,
        0,
        "60000",
        "0",
        99632,
        null,
        10148,
        5,
        5,
        1,
        "100",
        "if(120000-(self.level-52)*300<=0?0:120000-(self.level-52)*300)",
        "750+if((1020-(self.level-52)*20)>=0?(1020-(self.level-52)*20):0)",
        "if(120000-(self.level-52)*300<=0?0:120000-(self.level-52)*300)",
        30028,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_fire"),
                    new SpellData.T4cEffect.EffectParam(3, "50000"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(3, "50000"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_air"),
                    new SpellData.T4cEffect.EffectParam(3, "50000"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_earth"),
                    new SpellData.T4cEffect.EffectParam(3, "50000"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_dark"),
                    new SpellData.T4cEffect.EffectParam(3, "50000"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(3, "9000")))));
  }
}
