package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobDemonicPossessionSpell {
  private MobDemonicPossessionSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_demonic_possession_spell}",
        "${spell.description.mob_demonic_possession_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkBoost",
        "Pentacle-",
        null,
        0,
        0,
        "Vampire Dying.wav",
        null,
        0,
        "86400000",
        "250",
        233,
        null,
        10372,
        6,
        5,
        1,
        "100",
        "2000",
        "0",
        "0",
        30006,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10373"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "if(self.hp<self.maxhp/10?100:0)"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
