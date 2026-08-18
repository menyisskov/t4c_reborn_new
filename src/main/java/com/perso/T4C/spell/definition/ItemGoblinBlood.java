package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemGoblinBlood {
  private ItemGoblinBlood() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_goblin_blood}",
        "${spell.description.item_goblin_blood}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconEarthBoost",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "20000",
        "0",
        233,
        null,
        10103,
        2,
        5,
        2,
        "100",
        "0",
        "0",
        "0",
        0,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "-2d20"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "str"),
                    new SpellData.T4cEffect.EffectParam(3, "self.level/3")))));
  }
}
