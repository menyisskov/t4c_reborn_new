package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGnarledWalkingStick {
  private ItemItemGnarledWalkingStick() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gnarled_walking_stick",
        "${item.gnarled_walking_stick}",
        BodyPart.WEAPON,
        "PupSimpleStaff",
        null,
        null,
        "64kInvSimpleStaff",
        3107L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        17L,
        0L,
        68L,
        0L,
        1.0d,
        false,
        false,
        false,
        41611,
        1,
        296,
        "1d13+20",
        "if(1687-self.agi/250*1687/2<1687/2?1687/2:1687-self.agi/250*1687/2)+1d844",
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
