package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobColosseumUpgradeSpell3 {
  private MobColosseumUpgradeSpell3() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_colosseum_upgrade_spell_3}",
        "${spell.description.mob_colosseum_upgrade_spell_3}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconAirBoost",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "120000",
        "0",
        233,
        null,
        10715,
        0,
        5,
        1,
        "if(self.viewflag(30428)=1?100:0)",
        "0",
        "0",
        "0",
        0,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dark"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_dark/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "light"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_light/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "fire"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_fire/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "earth"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_earth/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "water"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_water/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "air"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_air/3"))),
            new SpellData.T4cEffect(
                3,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "30428"),
                    new SpellData.T4cEffect.EffectParam(2, "0")))));
  }
}
