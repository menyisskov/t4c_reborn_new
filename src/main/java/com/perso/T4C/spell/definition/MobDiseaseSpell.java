package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobDiseaseSpell {
  private MobDiseaseSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_disease_spell}",
        "${spell.description.mob_disease_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkDrainSingle",
        "PoisonArrow", "SmallPoisonCloud-", 0, 0,
        "Small Projectile.wav", "Ice Cloud.wav", 0, "300000", "30000", 233,
        null, 10376, 6, 4, 1,
        "100", "2000", "0", "0",
        30024, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "end"), new SpellData.T4cEffect.EffectParam(3, "-(target.end*2/3)"))), new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10377"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
