package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemIceBlade {
  private ItemItemIceBlade() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ice_blade",
        "${item.ice_blade}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        6398L,
        9L,
        0.0d,
        0L,
        0L,
        0L,
        95L,
        0L,
        30L,
        0L,
        1.0d,
        false,
        false,
        false,
        40196,
        1,
        2,
        "1d23+34",
        "if(787-self.agi/250*787/2<600?600:787-self.agi/250*787/2)+1d394",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10162, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
