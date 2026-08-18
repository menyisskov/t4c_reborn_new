package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemAmuletOfRejuvenation {
  private ItemAmuletOfRejuvenation() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_amulet_of_rejuvenation}",
        "${spell.description.item_amulet_of_rejuvenation}",
        "50",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconLightBoost",
        "HealingSpell-",
        null,
        0,
        0,
        "Healing.wav",
        null,
        0,
        "600000",
        "6000",
        233,
        null,
        10788,
        0,
        5,
        1,
        "if(equipcount(41865)=1?100:0)",
        "0",
        "0",
        "0",
        30001,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10786"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10789"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "500")))));
  }
}
