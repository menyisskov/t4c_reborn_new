package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobPiercingWormsSpell {
  private MobPiercingWormsSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_piercing_worms_spell}",
        "${spell.description.mob_piercing_worms_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkAttackSingle",
        "PoisonArrow",
        "SmallPoisonCloud-",
        0,
        0,
        "Small Projectile.wav",
        "Ice Cloud.wav",
        0,
        "120000",
        "10000",
        233,
        null,
        10379,
        6,
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
                    new SpellData.T4cEffect.EffectParam(2, "int"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.int/3)"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10380"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
