package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class WizardSValePortal {
  private WizardSValePortal() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.wizard_s_vale_portal}",
        "${spell.description.wizard_s_vale_portal}",
        "self.maxmana", 0, 109, 106, 92,
        false, true, "64kSpellIconNoneMain",
        "64kSpellEnergyBallPurple-", null, 0, 0,
        "Healing.wav", null, 0, "0", "0", 268038,
        null, 10811, 0, 6, 1,
        "100", "20000", "1000", "1000",
        30049, 0, false, List.of());
  }
}
