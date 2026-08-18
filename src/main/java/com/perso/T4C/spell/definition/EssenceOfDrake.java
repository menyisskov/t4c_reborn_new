package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class EssenceOfDrake {
  private EssenceOfDrake() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.essence_of_drake}",
        "${spell.description.essence_of_drake}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconLightHealSingle",
        "64kSpellEnergyBallWhite-", "HealSerious-", 0, 0,
        "Healing.wav", "Healing.wav", 0, "150000", "1000", 233,
        null, 10063, 2, 4, 2,
        "100", "0", "0", "0",
        30098, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "6d666"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
