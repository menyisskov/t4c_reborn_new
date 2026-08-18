package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBracersOfTheBarbarian {
  private ItemItemBracersOfTheBarbarian() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bracers_of_the_barbarian",
        "${item.bracers_of_the_barbarian}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        46355L,
        2L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        14L,
        16L,
        0.0d,
        false,
        false,
        false,
        41451,
        6,
        237,
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
            new ItemDefinition.ItemBoost(690, 8, "50", 0, 0),
            new ItemDefinition.ItemBoost(691, 9, "-50", 0, 0),
            new ItemDefinition.ItemBoost(692, 10001, "10", 0, 0),
            new ItemDefinition.ItemBoost(693, 10002, "10", 0, 0)),
        List.of(),
        false);
  }
}
