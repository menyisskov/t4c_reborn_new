package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBracersOfBattle {
  private ItemItemBracersOfBattle() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bracers_of_battle",
        "${item.bracers_of_battle}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        20994L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        25L,
        20L,
        45L,
        50L,
        0.0d,
        false,
        false,
        false,
        41460,
        2,
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
            new ItemDefinition.ItemBoost(773, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(774, 6, "10", 0, 0),
            new ItemDefinition.ItemBoost(775, 2, "10", 0, 0),
            new ItemDefinition.ItemBoost(776, 8, "25", 0, 0),
            new ItemDefinition.ItemBoost(878, 10035, "25", 0, 0)),
        List.of(),
        false);
  }
}
