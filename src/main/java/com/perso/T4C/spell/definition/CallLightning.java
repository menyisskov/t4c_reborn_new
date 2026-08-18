package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class CallLightning {
  private CallLightning() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.call_lightning}",
        "${spell.description.call_lightning}",
        "7", 0, 42, 41, 26,
        true, true, "64kSpellIconAirAttackSingle",
        "64kSpellEnergyBallYellow-", "GreatBolt-", 0, 0,
        "Healing.wav", "Spark.wav", 0, "0", "0", 32545,
        null, 10030, 3, 11, 2,
        "100", "1000+if((760-(self.level-26)*20)>=0?(760-(self.level-26)*20):0)", "750+if((760-(self.level-26)*20)>=0?(760-(self.level-26)*20):0)", "750+if((760-(self.level-26)*20)>=0?(760-(self.level-26)*20):0)",
        30087, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d37+20+self.int/30+self.wis/30)*self.air/target.r_air)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
