package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemElvenbane {
  private ItemItemElvenbane() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elvenbane",
        "${item.elvenbane}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40747,
        1,
        274,
        "0",
        "600+1d300",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10216, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
