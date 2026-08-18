package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobThirdMaxHpPoison {
  private MobThirdMaxHpPoison() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_third_max_hp_poison}",
        "${spell.description.mob_third_max_hp_poison}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkAttackSingle",
        "64kSpellEnergyBallGreen-",
        "SmallPoisonCloud-",
        0,
        0,
        "Healing.wav",
        "Ice Cloud.wav",
        0,
        "60000",
        "500",
        233,
        null,
        10747,
        0,
        0,
        1,
        "100",
        "0",
        "0",
        "0",
        30109,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10748"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "500")))));
  }
}
