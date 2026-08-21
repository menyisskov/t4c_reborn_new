package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobStoneShardSpell {
  private MobStoneShardSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_stone_shard_spell}",
        "${spell.description.mob_stone_shard_spell}",
        "0",
        0,
        0,
        0,
        0,
        true,
        true,
        "64kSpellIconEarthAttackSingle",
        "StoneShard",
        "RockyFly-",
        0,
        0,
        "Small Projectile.wav",
        "Rocks Fly.wav",
        0,
        "0",
        "0",
        0,
        null,
        10094,
        2,
        4,
        1,
        "100",
        "0",
        "0",
        "0",
        30025,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((self.wis-12+1d((self.wis-12)*1/2))*self.earth/target.r_earth)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
