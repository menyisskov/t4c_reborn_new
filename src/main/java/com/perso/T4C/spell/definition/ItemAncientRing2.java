package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemAncientRing2 {
  private ItemAncientRing2() {}

  public static SpellData definition() {
    return new SpellData(
        "${item.ancient_ring}",
        "",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "0",
        "0",
        0,
        null,
        10688,
        0,
        5,
        1,
        "if(self.viewflag(30398)=0?100:0)",
        "0",
        "0",
        "0",
        30012,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                7,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "342"),
                    new SpellData.T4cEffect.EffectParam(2, "248"),
                    new SpellData.T4cEffect.EffectParam(3, "0"))),
            new SpellData.T4cEffect(
                3,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "30398"),
                    new SpellData.T4cEffect.EffectParam(2, "1")))));
  }
}
