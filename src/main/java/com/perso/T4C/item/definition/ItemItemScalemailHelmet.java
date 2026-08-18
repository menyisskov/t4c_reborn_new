package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemScalemailHelmet {
  private ItemItemScalemailHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.scalemail_helmet",
        "${item.scalemail_helmet}",
        BodyPart.HEAD,
        "PupChainMailCoif",
        null,
        null,
        "64kInvChainMailHelm",
        3773L,
        4L,
        2.47d,
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
        40435,
        2,
        270,
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
