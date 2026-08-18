package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobChillingTouchSpell {
  private MobChillingTouchSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_chilling_touch_spell}",
        "${spell.description.mob_chilling_touch_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconWaterAttackSingle",
        "IceShard",
        "IceCloud-",
        0,
        0,
        "Small Projectile.wav",
        "Ice Cloud.wav",
        0,
        "(20000+1d10000)",
        "0",
        233,
        null,
        10384,
        4,
        4,
        2,
        "100",
        "2000",
        "0",
        "0",
        30023,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "str"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.str/3)"))),
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1,
                        "-(((self.int-14)*5/8+1d((self.int-14)*5/8))*self.water/target.r_water)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
