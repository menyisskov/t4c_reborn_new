package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCrudeSkraugBow {
  private ItemItemCrudeSkraugBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.crude_skraug_bow",
        "${item.crude_skraug_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBowFancy",
        23310L,
        8L,
        0.0d,
        0L,
        35L,
        0L,
        40L,
        169L,
        20L,
        25L,
        900.0d,
        false,
        true,
        false,
        41463,
        9,
        450,
        "1d27+59+2*arrow_dmg",
        "900",
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
            new ItemDefinition.ItemBoost(778, 10029, "10", 0, 0),
            new ItemDefinition.ItemBoost(779, 3, "10", 0, 0)),
        List.of(),
        false);
  }
}
