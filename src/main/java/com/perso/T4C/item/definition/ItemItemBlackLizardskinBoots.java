package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBlackLizardskinBoots {
  private ItemItemBlackLizardskinBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.black_lizardskin_boots",
        "${item.black_lizardskin_boots}",
        BodyPart.FEET,
        "PupBlackLeatherBoots",
        null,
        null,
        "64kInvBlackLeatherBoots",
        2300L,
        3L,
        1.62d,
        2L,
        75L,
        0L,
        0L,
        0L,
        30L,
        25L,
        0.0d,
        false,
        false,
        false,
        40595,
        2,
        288,
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
            new ItemDefinition.ItemBoost(134, 6, "5", 0, 0),
            new ItemDefinition.ItemBoost(459, 9, "13", 0, 0)),
        List.of(),
        false);
  }
}
