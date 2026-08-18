package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemSkullDagger {
  private ItemItemSkullDagger() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.skull_dagger",
        "${item.skull_dagger}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        155L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        20L,
        0L,
        20L,
        0L,
        1.0d,
        false,
        false,
        false,
        40009,
        1,
        274,
        "1d7+3",
        "600+1d300",
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
            new ItemDefinition.ItemBoost(72, 8, "10", 0, 0),
            new ItemDefinition.ItemBoost(73, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(129, 1, "2", 0, 0)),
        List.of(),
        false);
  }
}
