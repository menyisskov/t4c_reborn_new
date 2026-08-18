package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemRunedStoneTablet {
  private ItemRunedStoneTablet() {}

  public static SpellData definition() {
    return new SpellData(
        "${item.runed_stone_tablet}",
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
        10749,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30004,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10756"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "if(self.viewflag(30509)=3?100:0)"),
                    new SpellData.T4cEffect.EffectParam(4, "1000"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10755"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "if(self.viewflag(30509)=2?100:0)"),
                    new SpellData.T4cEffect.EffectParam(4, "1000"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10754"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(
                        3,
                        "if(self.viewflag(30507)>4?if(self.viewflag(30475)>27?if(self.viewflag(30509)<=1?100:0):100):100)"),
                    new SpellData.T4cEffect.EffectParam(4, "1000")))));
  }
}
