package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/** New high-level tier, level 650 — a fire AOE nuke between {@code SanctumWard} (550) and the
 * capstone {@code CataclysmsHerald} (900). */
public final class EmberqueensWrath {
  private EmberqueensWrath() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.emberqueens_wrath}",
        "${spell.description.emberqueens_wrath}",
        "38",
        4,
        440,
        170,
        650,
        true,
        true,
        "64kSpellIconFireAttackArea",
        "64kSpellFireBall",
        "GreatExplosion-",
        0,
        0,
        "Healing.wav",
        "Explosion.wav",
        0,
        "0",
        "0",
        3100000,
        null,
        99909,
        1,
        19,
        1,
        "100",
        "1900",
        "1400",
        "1400",
        30124,
        30014,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-(((1d70+190+self.int/8)*self.fire/target.r_fire)*13)"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d70+190+self.int/8)*self.fire/target.r_fire)*13)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
