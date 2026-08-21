package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobMakrshPtanghMeteorSpell {
  private MobMakrshPtanghMeteorSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_makrsh_ptangh_meteor_spell}",
        "",
        "0",
        0,
        0,
        0,
        1,
        false,
        false,
        "npc",
        "64kSpellEnergyBall-",
        "64kSpellMeteor-",
        0,
        25,
        "Healing.wav",
        "Meteor.wav",
        0,
        null,
        null,
        0,
        null,
        10710,
        0,
        0,
        2,
        "100",
        null,
        null,
        null,
        30070,
        30102,
        false,
        List.of());
  }
}
