package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemWizardBaneEffect {
  private ItemWizardBaneEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_wizard_bane_effect}",
        "${spell.description.item_wizard_bane_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkDrainArea",
        "ElectricShield-", null, 0, 0,
        "Electric Shield.wav", null, 0, "30000", "1000", 233,
        null, 10675, 6, 5, 1,
        "100", "0", "0", "0",
        30016, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "mana"), new SpellData.T4cEffect.EffectParam(3, "-(target.mana/5)"))), new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10676"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "1000")))));
  }
}
