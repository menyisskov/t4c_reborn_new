package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobArmorMeltSpell {
  private MobArmorMeltSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_armor_melt_spell}",
        "${spell.description.mob_armor_melt_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconFireAttackSingle",
        "64kSpellEnergyBall-",
        "FireWipe-",
        0,
        0,
        "Healing.wav",
        "Mind Shield.wav",
        0,
        "30000",
        "3000",
        233,
        null,
        10391,
        1,
        4,
        1,
        "100",
        "2000",
        "0",
        "0",
        30070,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "ac"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.ac/4)"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10392"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "3000")))));
  }
}
