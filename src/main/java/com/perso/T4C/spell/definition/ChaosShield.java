package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ChaosShield {
  private ChaosShield() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.chaos_shield}",
        "${spell.description.chaos_shield}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconFireBoost",
        "64kSpellEnergyBall-", "RedWipe-", 0, 0,
        "Healing.wav", "Mind Shield.wav", 0, "300000", "0", 233,
        null, 10060, 1, 4, 2,
        "100", "0", "0", "0",
        30111, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10061"), new SpellData.T4cEffect.EffectParam(2, "OnAttacked"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
