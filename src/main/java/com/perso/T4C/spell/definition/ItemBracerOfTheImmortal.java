package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemBracerOfTheImmortal {
  private ItemBracerOfTheImmortal() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_bracer_of_the_immortal}",
        "${spell.description.item_bracer_of_the_immortal}",
        "self.level", 0, 0, 0, 0,
        false, false, "64kSpellIconNoneBoost",
        "64kSpellBless-", null, 0, 0,
        "Healing.wav", null, 0, "600000", "0", 233,
        null, 10785, 0, 5, 1,
        "if(equipcount(41863)=1?100:0)", "0", "0", "0",
        30028, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "skill 35"), new SpellData.T4cEffect.EffectParam(3, "self.level"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dodge"), new SpellData.T4cEffect.EffectParam(3, "self.level"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "strength"), new SpellData.T4cEffect.EffectParam(3, "self.level*0.2"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "attack"), new SpellData.T4cEffect.EffectParam(3, "self.level"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "agility"), new SpellData.T4cEffect.EffectParam(3, "self.level*0.2"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "wisdom"), new SpellData.T4cEffect.EffectParam(3, "self.level*0.2"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "endurance"), new SpellData.T4cEffect.EffectParam(3, "self.level*0.2"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "intelligence"), new SpellData.T4cEffect.EffectParam(3, "self.level*0.2")))));
  }
}
