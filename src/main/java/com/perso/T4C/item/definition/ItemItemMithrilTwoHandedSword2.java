package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilTwoHandedSword2 {
  private ItemItemMithrilTwoHandedSword2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_two_handed_sword_2",
        "${item.mithril_two_handed_sword_2}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvGlinting Sword",
        0L,
        12L,
        0.0d,
        0L,
        0L,
        0L,
        329L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40501,
        1,
        202,
        "1d110+203",
        "if(1125-self.agi/250*1125/2<600?600:1125-self.agi/250*1125/2)+1d563",
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
        List.of(new ItemDefinition.ItemBoost(323, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
