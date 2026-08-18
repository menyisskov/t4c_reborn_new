package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ScalemailBoots {
  private ScalemailBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.scalemail_boots",
        "${item.scalemail_boots}",
        BodyPart.FEET,
        "PupBlackLeatherBoots",
        null,
        null,
        "64kInvBlackLeatherBoots",
        3407L,
        4L,
        2.565d,
        5L,
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
        40434,
        2,
        288,
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
