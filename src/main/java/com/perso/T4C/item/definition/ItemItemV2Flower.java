package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemV2Flower {
  private ItemItemV2Flower() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.v2_flower",
        "${item.v2_flower}",
        BodyPart.WEAPON,
        "PupFlower",
        null,
        null,
        "InvFireFlail",
        0L,
        0L,
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
        3012,
        1,
        648,
        "0",
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
