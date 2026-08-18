package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobKingsnakePoisonSpell {
  private MobKingsnakePoisonSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_kingsnake_poison_spell}",
        "${spell.description.mob_kingsnake_poison_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkDrainSingle",
        "64kSpellEnergyBallBlue-", "GreenWipe-", 0, 0,
        "Healing.wav", "Mind Shield.wav", 0, "if(self.end>250?60000:600000-(self.end*2400))", "2000", 233,
        null, 10649, 0, 4, 1,
        "100", "2500", "0", "0",
        30091, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10650"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
