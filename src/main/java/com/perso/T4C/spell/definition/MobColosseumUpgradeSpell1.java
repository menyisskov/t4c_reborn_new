package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobColosseumUpgradeSpell1 {
  private MobColosseumUpgradeSpell1() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_colosseum_upgrade_spell_1}",
        "${spell.description.mob_colosseum_upgrade_spell_1}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconEarthBoost",
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
        10713,
        0,
        5,
        1,
        "if(self.viewflag(30426)=1?100:0)",
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
                    new SpellData.T4cEffect.EffectParam(2, "strength"),
                    new SpellData.T4cEffect.EffectParam(3, "self.str/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(3, "(target.true_attack/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "skill 35"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_skill(35)/3"))),
            new SpellData.T4cEffect(
                3,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "30426"),
                    new SpellData.T4cEffect.EffectParam(2, "0")))));
  }
}
