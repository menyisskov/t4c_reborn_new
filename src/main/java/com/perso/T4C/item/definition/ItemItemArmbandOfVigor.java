package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemArmbandOfVigor {
  private ItemItemArmbandOfVigor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.armband_of_vigor",
        "${item.armband_of_vigor}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        1L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        60L,
        50L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41858,
        2,
        553,
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
        List.of(
            new ItemDefinition.ItemBoost(1018, 6, "25", 0, 0),
            new ItemDefinition.ItemBoost(1019, 3, "25", 0, 0)),
        List.of(),
        false);
  }
}
