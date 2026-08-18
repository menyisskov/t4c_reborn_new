package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemGemOfTheSun {
  private ItemGemOfTheSun() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_gem_of_the_sun}",
        "${spell.description.item_gem_of_the_sun}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconFireBoost",
        "BlueWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "10000+(5000*itemcount(41658))",
        "0",
        233,
        null,
        10679,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30004,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41661)>0?10*itemcount(41658):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41664)>0?10*itemcount(41658):0)")))));
  }
}
