package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemBowOfTheSpidersEffect {
  private ItemBowOfTheSpidersEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_bow_of_the_spiders_effect}",
        "${spell.description.item_bow_of_the_spiders_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconEarthAttackSingle",
        "64kSpellEntangle-",
        null,
        0,
        0,
        "Entangle.wav",
        null,
        0,
        "10000",
        "0",
        233,
        null,
        10682,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30029,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                14,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "10000"),
                    new SpellData.T4cEffect.EffectParam(4, "100"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(3, "-(self.dodge*3/4)")))));
  }
}
