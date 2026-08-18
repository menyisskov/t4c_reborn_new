package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemElvenSilversilkRobe {
  private ItemItemElvenSilversilkRobe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_silversilk_robe",
        "${item.elven_silversilk_robe}",
        BodyPart.BODY,
        "PupWhiteRobe",
        null,
        null,
        "64kInvWhiteRobe",
        1625L,
        5L,
        6.0d,
        25L,
        16L,
        0L,
        0L,
        0L,
        20L,
        73L,
        0.0d,
        false,
        false,
        false,
        41140,
        2,
        425,
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
        List.of(
            new ItemDefinition.ItemBoost(585, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(586, 4, "10", 0, 0),
            new ItemDefinition.ItemBoost(587, 22, "10", 0, 0)),
        List.of(),
        false);
  }
}
