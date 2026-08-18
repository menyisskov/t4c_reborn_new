package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBanditBack {
  private ItemItemBanditBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bandit_back",
        "${item.bandit_back}",
        BodyPart.BACK,
        "PupRedCape__pal2",
        null,
        null,
        "64kInvRedCape__pal2",
        40000L,
        2L,
        15.0d,
        0L,
        50L,
        0L,
        80L,
        360L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3357,
        2,
        651,
        null,
        null,
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(),
        List.of(),
        false);
  }
}
