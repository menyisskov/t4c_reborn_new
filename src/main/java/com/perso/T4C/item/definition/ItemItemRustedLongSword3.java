package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRustedLongSword3 {
  private ItemItemRustedLongSword3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rusted_long_sword_3",
        "${item.rusted_long_sword_3}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        806L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        24L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40473,
        1,
        2,
        "1d8+8",
        "if(787-self.agi/250*787/2<600?600:787-self.agi/250*787/2)+1d394",
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
        List.of(new ItemDefinition.ItemBoost(287, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
