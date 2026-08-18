package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRustedShortSword2 {
  private ItemItemRustedShortSword2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rusted_short_sword_2",
        "${item.rusted_short_sword_2}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvShortSword",
        29L,
        6L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40353,
        1,
        1,
        "1d4+1",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d375",
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
        List.of(new ItemDefinition.ItemBoost(284, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
