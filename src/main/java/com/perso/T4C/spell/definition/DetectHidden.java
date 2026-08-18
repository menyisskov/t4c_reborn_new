package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DetectHidden {
  private DetectHidden() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.detect_hidden}",
        "${spell.description.detect_hidden}",
        "90",
        0,
        118,
        16,
        35,
        false,
        true,
        "64kSpellIconFireBoost",
        "64kSpellEnergyBall-",
        "RedWipe-",
        0,
        0,
        "Healing.wav",
        "Mind Shield.wav",
        0,
        "600000",
        "0",
        54017,
        null,
        10259,
        1,
        5,
        1,
        "100",
        "1000+if((850-(self.level-35)*20)>=0?(850-(self.level-35)*20):0)",
        "750+if((850-(self.level-35)*20)>=0?(850-(self.level-35)*20):0)",
        "750+if((850-(self.level-35)*20)>=0?(850-(self.level-35)*20):0)",
        30111,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                17,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, "30003")))));
  }
}
