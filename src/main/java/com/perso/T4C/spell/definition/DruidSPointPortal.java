package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DruidSPointPortal {
  private DruidSPointPortal() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.druid_s_point_portal}",
        "${spell.description.druid_s_point_portal}",
        "self.maxmana", 0, 109, 106, 92,
        false, true, "64kSpellIconNoneMain",
        "64kSpellEnergyBallPurple-", null, 0, 0,
        "Healing.wav", null, 0, "0", "0", 268038,
        null, 10806, 0, 6, 1,
        "100", "20000", "1000", "1000",
        30049, 0, false, List.of(new SpellData.T4cEffect(6, List.of(new SpellData.T4cEffect.EffectParam(1, "NPC"), new SpellData.T4cEffect.EffectParam(2, "PORTALSPELLDRUIDPOINT")))));
  }
}
