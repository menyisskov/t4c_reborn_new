package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemScaleMail {
  private ItemItemScaleMail() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.scale_mail",
        "${item.scale_mail}",
        BodyPart.BODY,
        "PupChainMailBody",
        null,
        null,
        "64kInvScale",
        11801L,
        9L,
        8.65d,
        15L,
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
        40032,
        2,
        188,
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
