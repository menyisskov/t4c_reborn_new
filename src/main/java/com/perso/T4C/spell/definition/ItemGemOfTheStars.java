package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemGemOfTheStars {
  private ItemGemOfTheStars() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_gem_of_the_stars}",
        "${spell.description.item_gem_of_the_stars}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconLightBoost",
        "BlueWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "10000+(5000*itemcount(41659))",
        "0",
        233,
        null,
        10680,
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
                    new SpellData.T4cEffect.EffectParam(2, "r_dark"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41665)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_earth"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41665)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "skill 9"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41662)>0?5*itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_air"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41665)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "skill 29"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41665)>0?5*itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41665)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "fire"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41662)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_fire"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41665)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "water"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41662)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dark"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41662)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "air"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41662)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "light"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41662)>0?itemcount(41659):0)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "earth"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(equipcount(41662)>0?itemcount(41659):0)")))));
  }
}
