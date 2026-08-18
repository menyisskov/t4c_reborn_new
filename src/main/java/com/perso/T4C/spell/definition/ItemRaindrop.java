package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemRaindrop {
  private ItemRaindrop() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_raindrop}",
        "${spell.description.item_raindrop}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconWaterBoost",
        "BlueWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "if(itemcount(41671)>24?130000:5000+(5000*(itemcount(41671))))",
        "0",
        233,
        null,
        10683,
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
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(
                        3,
                        "if(equipcount(41672)>0?if(itemcount(41671)>24?25:1*itemcount(41671)):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "water"),
                    new SpellData.T4cEffect.EffectParam(
                        3,
                        "if(equipcount(41672)>0?if(itemcount(41671)>24?25:1*itemcount(41671)):0)")))));
  }
}
