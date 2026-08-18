package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobRabiesSpell {
  private MobRabiesSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_rabies_spell}",
        "${spell.description.mob_rabies_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconNoneMain",
        "PoisonArrow",
        "SmallPoisonCloud-",
        0,
        0,
        "Small Projectile.wav",
        "Ice Cloud.wav",
        0,
        "600000",
        "0",
        233,
        null,
        10352,
        0,
        4,
        1,
        "100",
        "2000",
        "0",
        "0",
        30024,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "str"),
                    new SpellData.T4cEffect.EffectParam(3, "target.str/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "int"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.int/5)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "wisdom"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.wis/5)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.dodge/5)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "max HP"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.maxhp/10)")))));
  }
}
