package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemFocusOfTheEarthEmpyrean {
  private ItemItemFocusOfTheEarthEmpyrean() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.focus_of_the_earth_empyrean",
        "${item.focus_of_the_earth_empyrean}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "InvEmeraldFocus",
        0L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        200L,
        0L,
        0L,
        1000L,
        0.0d,
        false,
        false,
        true,
        900102,
        2,
        570,
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
            new ItemDefinition.ItemBoost(90111, 4, "300", 0, 0),
            new ItemDefinition.ItemBoost(90112, 19, "200", 0, 0),
            new ItemDefinition.ItemBoost(90113, 23, "100", 0, 0),
            new ItemDefinition.ItemBoost(90114, 12, "50", 0, 0),
            new ItemDefinition.ItemBoost(90115, 13, "50", 0, 0),
            new ItemDefinition.ItemBoost(90116, 14, "50", 0, 0),
            new ItemDefinition.ItemBoost(90117, 15, "50", 0, 0),
            new ItemDefinition.ItemBoost(90119, 22, "50", 0, 0),
            new ItemDefinition.ItemBoost(90120, 20, "50", 0, 0)),
        List.of(),
        false);
  }
}
