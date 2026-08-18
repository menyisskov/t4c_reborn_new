package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class HealingMist {
  private HealingMist() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.healing_mist}",
        "${spell.description.healing_mist}",
        "17",
        8,
        20,
        125,
        75,
        false,
        true,
        "64kSpellIconLightHealArea",
        "64kSpellEnergyBallWhite-",
        "HealSerious-",
        0,
        0,
        "Healing.wav",
        "Healing.wav",
        0,
        "0",
        "0",
        191811,
        null,
        10140,
        5,
        15,
        2,
        "100",
        "2*(1000+if((1250-(self.level-75)*20)>=0?(1250-(self.level-75)*20):0))",
        "2*(750+if((1250-(self.level-75)*20)>=0?(1250-(self.level-75)*20):0))",
        "2*(750+if((1250-(self.level-75)*20)>=0?(1250-(self.level-75)*20):0))",
        30098,
        30096,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "(1d8+95+self.wis/8)*self.light/100)"),
                    new SpellData.T4cEffect.EffectParam(2, "(1d8+95+self.wis/8)*self.light/100)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
