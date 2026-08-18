package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobBrainBlastSpell {
  private MobBrainBlastSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_brain_blast_spell}",
        "${spell.description.mob_brain_blast_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkDrainArea",
        "PoisonArrow",
        "SmallPoisonCloud-",
        0,
        0,
        "Small Projectile.wav",
        "Ice Cloud.wav",
        0,
        "120000",
        "5000",
        233,
        null,
        10452,
        6,
        4,
        2,
        "100",
        "0",
        "0",
        "0",
        30024,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "int"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.int/4)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(3, "-target.attack/5"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "skill 35"),
                    new SpellData.T4cEffect.EffectParam(3, "-target.true_skill(35)/5")))));
  }
}
