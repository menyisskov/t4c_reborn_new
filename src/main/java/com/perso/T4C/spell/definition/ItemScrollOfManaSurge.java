package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfManaSurge {
  private ItemScrollOfManaSurge() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_mana_surge}",
        "${spell.description.item_scroll_of_mana_surge}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconNoneBoost",
        "Curse-",
        null,
        0,
        0,
        "Curse.wav",
        null,
        0,
        "180000",
        "0",
        233,
        null,
        10664,
        0,
        5,
        1,
        "100",
        "1000+if((1040-(self.level-54)*20)>=0?(1040-(self.level-54)*20):0)",
        "750+if((1040-(self.level-54)*20)>=0?(1040-(self.level-54)*20):0)",
        "750+if((1040-(self.level-54)*20)>=0?(1040-(self.level-54)*20):0)",
        30015,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10139"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "earth"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_earth/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "fire"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_fire/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "air"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_air/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "light"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_light/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "water"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_water/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dark"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_dark/3")))));
  }
}
