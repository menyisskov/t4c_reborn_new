package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ManaSurge {
  private ManaSurge() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mana_surge}",
        "${spell.description.mana_surge}",
        "39", 0, 174, 17, 54,
        false, true, "64kSpellIconNoneBoost",
        "64kSpellEnergyBallPurple-", "BlueWipe-", 0, 0,
        "Healing.wav", "Mind Shield.wav", 0, "180000", "0", 107028,
        null, 10139, 0, 3, 1,
        "100", "1000+if((1040-(self.level-54)*20)>=0?(1040-(self.level-54)*20):0)", "750+if((1040-(self.level-54)*20)>=0?(1040-(self.level-54)*20):0)", "750+if((1040-(self.level-54)*20)>=0?(1040-(self.level-54)*20):0)",
        30055, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "earth"), new SpellData.T4cEffect.EffectParam(3, "target.true_earth/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "air"), new SpellData.T4cEffect.EffectParam(3, "target.true_air/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "fire"), new SpellData.T4cEffect.EffectParam(3, "target.true_fire/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "water"), new SpellData.T4cEffect.EffectParam(3, "target.true_water/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "light"), new SpellData.T4cEffect.EffectParam(3, "target.true_light/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dark"), new SpellData.T4cEffect.EffectParam(3, "target.true_dark/4")))));
  }
}
