package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemScalemailProtector {
  private ItemItemScalemailProtector() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.scalemail_protector",
        "${item.scalemail_protector}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        2648L,
        3L,
        1.9d,
        4L,
        100L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40738,
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
        List.of(),
        List.of(),
        false);
  }
}
