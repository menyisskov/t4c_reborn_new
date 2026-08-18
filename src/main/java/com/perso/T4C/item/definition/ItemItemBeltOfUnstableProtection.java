package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBeltOfUnstableProtection {
  private ItemItemBeltOfUnstableProtection() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.belt_of_unstable_protection",
        "${item.belt_of_unstable_protection}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        1L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41857,
        2,
        235,
        null,
        "0",
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
