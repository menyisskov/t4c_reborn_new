package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDaggerOfPiercing {
  private ItemItemDaggerOfPiercing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dagger_of_piercing",
        "${item.dagger_of_piercing}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        949L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        39L,
        0L,
        19L,
        21L,
        1.0d,
        false,
        false,
        false,
        40074,
        1,
        274,
        "1d8+8",
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
        List.of(new ItemDefinition.ItemBoost(33, 8, "10", 0, 0)),
        List.of(),
        false);
  }
}
