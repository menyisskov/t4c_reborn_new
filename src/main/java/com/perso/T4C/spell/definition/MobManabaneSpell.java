package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobManabaneSpell {
  private MobManabaneSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_manabane_spell}",
        "${spell.description.mob_manabane_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkDrainSingle",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "30000",
        "1000",
        233,
        null,
        10386,
        6,
        4,
        1,
        "100",
        "2000",
        "0",
        "0",
        0,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "mana"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.mana/5)"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10387"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "1000")))));
  }
}
