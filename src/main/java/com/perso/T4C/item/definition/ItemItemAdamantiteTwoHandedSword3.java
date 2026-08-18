package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAdamantiteTwoHandedSword3 {
  private ItemItemAdamantiteTwoHandedSword3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_two_handed_sword_3",
        "${item.adamantite_two_handed_sword_3}",
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
        401L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40498,
        1,
        202,
        "1d150+257",
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
        List.of(new ItemDefinition.ItemBoost(333, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
