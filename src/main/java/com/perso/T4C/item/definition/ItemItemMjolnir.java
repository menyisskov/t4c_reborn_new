package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMjolnir {
  private ItemItemMjolnir() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mjolnir",
        "${item.mjolnir}",
        BodyPart.WEAPON,
        "PupWarhammer",
        null,
        null,
        "64kInvWarhammer",
        0L,
        9L,
        0.0d,
        0L,
        0L,
        0L,
        200L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40867,
        1,
        5,
        "1d58+107",
        "if(937-self.agi/250*937/2<600?600:937-self.agi/250*937/2)+1d469",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10233, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(568, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
