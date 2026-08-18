package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobFoulSicknessSpell {
  private MobFoulSicknessSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_foul_sickness_spell}",
        "${spell.description.mob_foul_sickness_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkBoost",
        "PoisonArrow",
        "SmallPoisonCloud-",
        0,
        0,
        "Small Projectile.wav",
        "Ice Cloud.wav",
        0,
        "60000",
        "0",
        233,
        null,
        10320,
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
                    new SpellData.T4cEffect.EffectParam(2, "endurance"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.end-1)")))));
  }
}
