package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfInvisibility {
  private ItemScrollOfInvisibility() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_invisibility}",
        "${spell.description.item_scroll_of_invisibility}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconAirDefense",
        "Curse-",
        null,
        0,
        0,
        "Curse.wav",
        null,
        0,
        "300000",
        "0",
        233,
        null,
        10667,
        3,
        5,
        1,
        "100",
        "1000+if((1080-(self.level-58)*20)>=0?(1080-(self.level-58)*20):0)",
        "750+if((1080-(self.level-58)*20)>=0?(1080-(self.level-58)*20):0)",
        "750+if((1080-(self.level-58)*20)>=0?(1080-(self.level-58)*20):0)",
        30015,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10257"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                15,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, "30015")))));
  }
}
