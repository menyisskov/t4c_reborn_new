package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemSteelReinforcedWarhammer {
  private ItemItemSteelReinforcedWarhammer() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.steel_reinforced_warhammer",
        "${item.steel_reinforced_warhammer}",
        BodyPart.WEAPON,
        "PupWarhammer",
        null,
        null,
        "64kInvWarhammer",
        1755L,
        9L,
        0.0d,
        0L,
        0L,
        0L,
        40L,
        0L,
        0L,
        41L,
        1.0d,
        false,
        false,
        false,
        40392,
        1,
        5,
        "if(target.r_dark=5025?1d13+19:1d12+16)",
        "if(937-self.agi/250*937/2<600?600:937-self.agi/250*937/2)+1d469",
        0,
        0,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(),
        List.of(),
        false);
  }
}
