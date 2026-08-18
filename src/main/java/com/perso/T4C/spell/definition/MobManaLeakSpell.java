package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobManaLeakSpell {
  private MobManaLeakSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_mana_leak_spell}",
        "${spell.description.mob_mana_leak_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconNoneAttackSingle",
        "Lightning",
        "ElectricShield-",
        0,
        0,
        "Lightning.wav",
        "Electric Shield.wav",
        0,
        "30000",
        "1500",
        233,
        null,
        10382,
        0,
        4,
        1,
        "100",
        "2000",
        "0",
        "0",
        30002,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10383"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
