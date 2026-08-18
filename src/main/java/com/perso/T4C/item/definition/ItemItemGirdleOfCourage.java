package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGirdleOfCourage {
  private ItemItemGirdleOfCourage() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.girdle_of_courage",
        "${item.girdle_of_courage}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        375L,
        3L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        26L,
        28L,
        0.0d,
        false,
        false,
        false,
        40717,
        2,
        235,
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
            new ItemDefinition.ItemBoost(439, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(440, 6, "3", 0, 0)),
        List.of(),
        false);
  }
}
