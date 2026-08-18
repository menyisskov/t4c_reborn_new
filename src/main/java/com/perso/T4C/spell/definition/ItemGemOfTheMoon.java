package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemGemOfTheMoon {
  private ItemGemOfTheMoon() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_gem_of_the_moon}",
        "${spell.description.item_gem_of_the_moon}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkBoost",
        "BlueWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "10000+(5000*itemcount(41657))",
        "0",
        233,
        null,
        10678,
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
                    new SpellData.T4cEffect.EffectParam(2, "agility"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41663)>0?5*itemcount(41657):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "intelligence"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41660)>0?5*itemcount(41657):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "wisdom"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41660)>0?5*itemcount(41657):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "strength"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41663)>0?5*itemcount(41657):0)")))));
  }
}
