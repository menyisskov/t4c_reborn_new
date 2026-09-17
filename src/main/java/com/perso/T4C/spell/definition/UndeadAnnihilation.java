package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class UndeadAnnihilation {
  private UndeadAnnihilation() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.undead_annihilation}",
        "${spell.description.undead_annihilation}",
        "30",
        0,
        100,
        260,
        120,
        true,
        true,
        "64kSpellIconLightAttackSingle",
        "64kSpellEnergyBallWhite-",
        "HealingSpell-",
        0,
        0,
        "Healing.wav",
        "Healing.wav",
        0,
        "0",
        "0",
        115000,
        null,
        99904,
        5,
        11,
        2,
        "100",
        "1200",
        "900",
        "900",
        30096,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1,
                        "-(((1d6+27+self.wis/16)*self.light/target.r_light)*self.wis/(20+2*self.level)*6)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
