package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Light {
  private Light() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.light}",
        "${spell.description.light}",
        "10", 0, 0, 0, 0,
        false, false, "64kSpellIconLightMain",
        "Flak1-", null, 0, 0,
        "Explosion.wav", null, 0, "600000", "0", 233,
        null, 10097, 1, 5, 1,
        "100", "1000+if((520-(self.level-1)*20)>=0?(520-(self.level-1)*20):0)", "750+if((520-(self.level-1)*20)>=0?(520-(self.level-1)*20):0)", "750+if((520-(self.level-1)*20)>=0?(520-(self.level-1)*20):0)",
        30012, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, "TRUE"), new SpellData.T4cEffect.EffectParam(2, "Radiance"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
