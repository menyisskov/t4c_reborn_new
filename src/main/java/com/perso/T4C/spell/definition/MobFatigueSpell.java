package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobFatigueSpell {
  private MobFatigueSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_fatigue_spell}",
        "${spell.description.mob_fatigue_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkDrainSingle",
        "64kSpellEnergyBallBlack-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "10000",
        "10000",
        233,
        null,
        10652,
        0,
        4,
        1,
        "100",
        "2000",
        "0",
        "0",
        30062,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                14,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10000"),
                    new SpellData.T4cEffect.EffectParam(2, "0"),
                    new SpellData.T4cEffect.EffectParam(3, "10000"),
                    new SpellData.T4cEffect.EffectParam(4, "100"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10247"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
