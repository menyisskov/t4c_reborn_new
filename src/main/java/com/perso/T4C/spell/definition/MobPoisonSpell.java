package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobPoisonSpell {
  private MobPoisonSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_poison_spell}",
        "${spell.description.mob_poison_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconWaterAttackSingle",
        "PoisonArrow",
        "SmallPoisonCloud-",
        0,
        0,
        "Small Projectile.wav",
        "Ice Cloud.wav",
        0,
        "if(target.end>120?60000:120000-(target.end*500))",
        "6000",
        233,
        null,
        10344,
        4,
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
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10345"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
