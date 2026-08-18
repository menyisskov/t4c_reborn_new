package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobStoneSpell {
  private MobStoneSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_stone_spell}",
        "${spell.description.mob_stone_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthMain",
        "StoneShard", "RockyFly-", 0, 0,
        "Small Projectile.wav", "Rocks Fly.wav", 0, "if(target.end>=500?10000:20000-(target.end*20))", "0", 233,
        null, 10388, 2, 4, 1,
        "100", "2000", "0", "0",
        30025, 0, false, List.of(new SpellData.T4cEffect(14, List.of(new SpellData.T4cEffect.EffectParam(1, "if(target.end>=500?10000:20000-(target.end*20))"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "if(target.end>=500?10000:20000-(target.end*20))"), new SpellData.T4cEffect.EffectParam(4, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "ac"), new SpellData.T4cEffect.EffectParam(3, "if(self.ac<50?50:self.ac)")))));
  }
}
