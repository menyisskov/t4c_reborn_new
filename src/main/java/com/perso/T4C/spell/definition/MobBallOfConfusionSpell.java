package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobBallOfConfusionSpell {
  private MobBallOfConfusionSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_ball_of_confusion_spell}",
        "${spell.description.mob_ball_of_confusion_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkDrainArea",
        "StoneShard", "RockyFly-", 0, 0,
        "Small Projectile.wav", "Rocks Fly.wav", 0, "120000", "5000", 233,
        null, 10453, 6, 4, 2,
        "100", "0", "0", "0",
        30025, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "wis"), new SpellData.T4cEffect.EffectParam(3, "-(target.wis/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "attack"), new SpellData.T4cEffect.EffectParam(3, "-target.attack/5"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "skill 35"), new SpellData.T4cEffect.EffectParam(3, "-target.true_skill(35)/5")))));
  }
}
