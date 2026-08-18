package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemGuardianRingOfVitality {
  private ItemGuardianRingOfVitality() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_guardian_ring_of_vitality}",
        "${spell.description.item_guardian_ring_of_vitality}",
        "self.level", 0, 0, 0, 0,
        false, false, "64kSpellIconLightDefense",
        "HealingSpell-", null, 0, 0,
        "Healing.wav", null, 0, "600000", "0", 233,
        null, 10784, 0, 5, 1,
        "if(equipcount(41862)=1?100:0)", "0", "0", "0",
        30001, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "max hp"), new SpellData.T4cEffect.EffectParam(3, "self.level*2.5")))));
  }
}
