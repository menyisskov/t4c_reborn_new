package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemBeltOfUnstableProtection {
  private ItemBeltOfUnstableProtection() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_belt_of_unstable_protection}",
        "${spell.description.item_belt_of_unstable_protection}",
        "self.level", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthDefense",
        "GreenWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "600000", "0", 233,
        null, 10781, 0, 5, 1,
        "if(equipcount(41857)=1?100:0)", "0", "0", "0",
        30003, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "(1d(self.level)/5)+(1d(self.level)/5)+(1d(self.level)/5)+(1d(self.level)/5)+(1d(self.level)/5)")))));
  }
}
