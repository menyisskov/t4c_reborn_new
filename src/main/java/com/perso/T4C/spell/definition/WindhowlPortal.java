package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class WindhowlPortal {
  private WindhowlPortal() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.windhowl_portal}",
        "${spell.description.windhowl_portal}",
        "self.maxmana", 0, 109, 106, 92,
        false, true, "64kSpellIconNoneMain",
        "64kSpellEnergyBallPurple-", null, 0, 0,
        "Healing.wav", null, 0, "0", "0", 268038,
        null, 10810, 0, 6, 1,
        "100", "20000", "1000", "1000",
        30049, 0, false, List.of(new SpellData.T4cEffect(6, List.of(new SpellData.T4cEffect.EffectParam(1, "NPC"), new SpellData.T4cEffect.EffectParam(2, "PORTALSPELLWINDHOWL")))));
  }
}
