package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class TestMassPlague {
  private TestMassPlague() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.test_mass_plague}",
        "${spell.description.test_mass_plague}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkMain",
        "PoisonArrow",
        "SmallPoisonCloud-",
        0,
        0,
        "Small Projectile.wav",
        "Ice Cloud.wav",
        0,
        "0",
        "0",
        233,
        null,
        10466,
        0,
        4,
        1,
        "100",
        "0",
        "0",
        "0",
        30024,
        0,
        false,
        List.of());
  }
}
