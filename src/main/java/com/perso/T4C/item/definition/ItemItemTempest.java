package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTempest {
  private ItemItemTempest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.tempest",
        "${item.tempest}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        250L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40865,
        1,
        2,
        "1d68+129",
        "if(825-self.agi/250*825/2<600?600:825-self.agi/250*825/2)+1d413",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10230, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(566, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
