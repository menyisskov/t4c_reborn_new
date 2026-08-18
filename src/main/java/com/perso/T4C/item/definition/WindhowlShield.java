package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class WindhowlShield {
  private WindhowlShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.windhowl_shield",
        "${item.windhowl_shield}",
        BodyPart.SHIELD,
        "PupBarossaShield",
        null,
        null,
        "64kInvBarossaShield",
        2837L,
        8L,
        3.0d,
        10L,
        60L,
        0L,
        0L,
        0L,
        26L,
        23L,
        0.0d,
        false,
        false,
        false,
        40018,
        2,
        273,
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
        List.of(new ItemDefinition.ItemBoost(501, 9, "19", 0, 0)),
        List.of(),
        false);
  }
}
