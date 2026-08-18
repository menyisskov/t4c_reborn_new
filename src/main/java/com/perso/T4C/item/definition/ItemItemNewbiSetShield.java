package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemNewbiSetShield {
  private ItemItemNewbiSetShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.newbi_set_shield",
        "${item.newbi_set_shield}",
        BodyPart.SHIELD,
        "PupBarossaShield",
        null,
        null,
        "64kInvBarossaShield",
        2000L,
        0L,
        3.0d,
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
        true,
        3258,
        2,
        273,
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
