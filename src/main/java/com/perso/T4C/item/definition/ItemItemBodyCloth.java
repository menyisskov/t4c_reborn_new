package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBodyCloth {
  private ItemItemBodyCloth() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.body_cloth",
        "${item.body_cloth}",
        BodyPart.BODY,
        "PupBodyClothSet1",
        null,
        null,
        "64kInvClothSet1Body",
        2L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40214,
        2,
        285,
        null,
        "0",
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
