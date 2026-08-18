package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemBluestoneTalisman {
  private ItemBluestoneTalisman() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_bluestone_talisman}",
        "${spell.description.item_bluestone_talisman}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconWaterDefense",
        null, null, 0, 0,
        null, null, 0, "120000", "120000", 233,
        null, 10487, 4, 5, 2,
        "100", "1000", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "if(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560)<6?(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560))*5:50)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "if(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560)<6?(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560))*4:40)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "if(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560)<6?(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560))*4:40)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_air"), new SpellData.T4cEffect.EffectParam(3, "if(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560)<6?(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560))*4:40)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_earth"), new SpellData.T4cEffect.EffectParam(3, "if(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560)<6?(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560))*4:40)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_dark"), new SpellData.T4cEffect.EffectParam(3, "if(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560)<6?(equipcount(41555)+equipcount(41556)+equipcount(41557)+equipcount(41558)+equipcount(41559)+equipcount(41560))*4:40)"))), new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10488"), new SpellData.T4cEffect.EffectParam(2, "OnHit"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "250"))), new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10719"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "500"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "mana"), new SpellData.T4cEffect.EffectParam(3, "-(self.maxmana/10)")))));
  }
}
