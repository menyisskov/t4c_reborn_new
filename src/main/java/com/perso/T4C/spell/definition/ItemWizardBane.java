package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemWizardBane {
  private ItemWizardBane() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_wizard_bane}",
        "${spell.description.item_wizard_bane}",
        "0", 0, 0, 0, 0,
        false, false, "0",
        null, null, 0, 0,
        null, null, 0, "0", "0", 233,
        null, 10674, 0, 4, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10675"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "3"), new SpellData.T4cEffect.EffectParam(4, "0")))));
  }
}
