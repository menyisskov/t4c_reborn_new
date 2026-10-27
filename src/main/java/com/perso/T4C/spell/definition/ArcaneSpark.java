package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Entry-level, non-elemental bolt every mage opens with. Deliberately a notch below Fire Dart (a
 * smaller die, a lower flat bonus, slower recovery) and with no element, so it has no resistance
 * or school power to scale off.
 */
public final class ArcaneSpark {
  private ArcaneSpark() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.arcane_spark}",
        "${spell.description.arcane_spark}",
        "1",
        0,
        0,
        0,
        1,
        true,
        true,
        "64kSpellIconNoneAttackSingle",
        "64kSpellEnergyBallBlue-",
        "SmallExplosion-",
        0,
        0,
        "Small Projectile.wav",
        "Explosion.wav",
        0,
        "0",
        "0",
        300,
        null,
        99960,
        0,
        11,
        2,
        "100",
        "1000+if((560-(self.level-1)*20)>=0?(560-(self.level-1)*20):0)",
        "750+if((560-(self.level-1)*20)>=0?(560-(self.level-1)*20):0)",
        "750+if((560-(self.level-1)*20)>=0?(560-(self.level-1)*20):0)",
        0,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "-(1d13+4+self.int/27)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
