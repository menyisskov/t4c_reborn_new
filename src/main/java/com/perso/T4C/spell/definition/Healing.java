package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Healing {
  private Healing() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.healing}",
        "${spell.description.healing}",
        "13",
        0,
        18,
        96,
        55,
        false,
        true,
        "64kSpellIconLightHealSingle",
        "64kSpellEnergyBallWhite-",
        "HealSerious-",
        0,
        0,
        "Healing.wav",
        "Healing.wav",
        0,
        "0",
        "0",
        115014,
        null,
        10137,
        5,
        3,
        2,
        "100",
        "1000+if((1050-(self.level-55)*20)>=0?(1050-(self.level-55)*20):0)",
        "750+if((1050-(self.level-55)*20)>=0?(1050-(self.level-55)*20):0)",
        "750+if((1050-(self.level-55)*20)>=0?(1050-(self.level-55)*20):0)",
        30098,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "(1d7+71+self.wis/10)*self.light/100)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
