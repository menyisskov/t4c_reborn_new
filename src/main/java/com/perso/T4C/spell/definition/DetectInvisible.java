package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DetectInvisible {
  private DetectInvisible() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.detect_invisible}",
        "${spell.description.detect_invisible}",
        "60", 0, 162, 16, 50,
        false, true, "64kSpellIconFireBoost",
        "64kSpellEnergyBall-", "RedWipe-", 0, 0,
        "Healing.wav", "Mind Shield.wav", 0, "300000", "0", 92500,
        null, 10258, 1, 5, 1,
        "100", "1000+if((1000-(self.level-50)*20)>=0?(1000-(self.level-50)*20):0)", "750+if((1000-(self.level-50)*20)>=0?(1000-(self.level-50)*20):0)", "750+if((1000-(self.level-50)*20)>=0?(1000-(self.level-50)*20):0)",
        30111, 0, false, List.of(new SpellData.T4cEffect(16, List.of(new SpellData.T4cEffect.EffectParam(1, "100"), new SpellData.T4cEffect.EffectParam(2, "30020")))));
  }
}
