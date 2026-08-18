package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfDetectHidden {
  private ItemScrollOfDetectHidden() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_detect_hidden}",
        "${spell.description.item_scroll_of_detect_hidden}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconFireBoost",
        "RedWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "600000",
        "0",
        233,
        null,
        10666,
        1,
        5,
        1,
        "100",
        "1000+if((850-(self.level-35)*20)>=0?(850-(self.level-35)*20):0)",
        "750+if((850-(self.level-35)*20)>=0?(850-(self.level-35)*20):0)",
        "750+if((850-(self.level-35)*20)>=0?(850-(self.level-35)*20):0)",
        30007,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10259"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                17,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, "30003")))));
  }
}
