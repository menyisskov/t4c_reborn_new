package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMace {
  private ItemItemMace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mace",
        "${item.mace}",
        BodyPart.WEAPON,
        "PupOgreClub",
        null,
        null,
        "64kInvOgreClub",
        1787L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        30L,
        0L,
        0L,
        34L,
        1.0d,
        false,
        false,
        false,
        40115,
        1,
        447,
        "1d8+10",
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
