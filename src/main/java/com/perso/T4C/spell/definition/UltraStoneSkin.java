package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class UltraStoneSkin {
  private UltraStoneSkin() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ultra_stone_skin}",
        "${spell.description.ultra_stone_skin}",
        "45",
        0,
        90,
        375,
        150,
        false,
        false,
        "64kSpellIconEarthDefense",
        "RockyFly-",
        null,
        0,
        0,
        "Rocks Fly.wav",
        null,
        0,
        "1200000",
        "0",
        1000000,
        null,
        99949,
        2,
        3,
        1,
        "100",
        "0",
        "0",
        "0",
        30021,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "(2*(10+self.int/25+self.wis/13))")))));
  }
}
