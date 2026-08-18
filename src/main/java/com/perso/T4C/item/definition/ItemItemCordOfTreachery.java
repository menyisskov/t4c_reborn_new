package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCordOfTreachery {
  private ItemItemCordOfTreachery() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cord_of_treachery",
        "${item.cord_of_treachery}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        9750L,
        3L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        40L,
        17L,
        16L,
        0.0d,
        false,
        false,
        false,
        41462,
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
            new ItemDefinition.ItemBoost(769, 10015, "5", 0, 0),
            new ItemDefinition.ItemBoost(770, 10016, "5", 0, 0),
            new ItemDefinition.ItemBoost(771, 10014, "5", 0, 0)),
        List.of(),
        false);
  }
}
