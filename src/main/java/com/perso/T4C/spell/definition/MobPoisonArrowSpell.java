package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobPoisonArrowSpell {
  private MobPoisonArrowSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_poison_arrow_spell}",
        "${spell.description.mob_poison_arrow_spell}",
        "0", 0, 0, 0, 0, false, false,
        "64kSpellIconWaterAttackSingle",
        "PoisonArrow", "SmallPoisonCloud-", 0, 0,
        "Small Projectile.wav", "Ice Cloud.wav", 0,
        "0", "0", 0, null, 10091, 4, 4, 1,
        "100", "0", "0", "0", 30024, 0, false,
        List.of());
  }
}
