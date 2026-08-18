package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobArmorWeaknessSpell {
  private MobArmorWeaknessSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_armor_weakness_spell}",
        "${spell.description.mob_armor_weakness_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthAttackSingle",
        "StoneShard", "RockyFly-", 0, 0,
        "Small Projectile.wav", "Rocks Fly.wav", 0, "60000", "0", 233,
        null, 10381, 2, 4, 1,
        "100", "2000", "0", "0",
        30025, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "-(target.ac/2)")))));
  }
}
